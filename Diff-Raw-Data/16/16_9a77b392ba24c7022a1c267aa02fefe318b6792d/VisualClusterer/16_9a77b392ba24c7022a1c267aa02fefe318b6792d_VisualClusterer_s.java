 package eu.socialsensor.framework.multimedia.visual.clustering;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.solr.client.solrj.impl.HttpSolrServer;
 import org.apache.solr.common.SolrInputDocument;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.BasicDBObjectBuilder;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.MongoClient;
 
 import edu.uci.ics.jung.graph.Graph;
 import edu.uci.ics.jung.graph.UndirectedSparseGraph;
 import eu.socialsensor.framework.common.domain.MediaItem;
 import eu.socialsensor.framework.common.factories.ItemFactory;
import eu.socialsensor.framework.client.search.visual.NearestImage;
 import eu.socialsensor.framework.client.search.visual.VisualIndexHandler;
 
 
 public class VisualClusterer {
 	
 	private VisualIndexHandler visualIndex;
 	
 	// Parameters for NDD : 0.65, 2, 0.25
 	private static final double SCAN_EPSILON = 0.65;
 	private static final int SCAN_MU = 4;
 	
 	private static final Double THRESHOLD = 0.7;
 	
 	public VisualClusterer(String webServiceHost, String collection) {
 		visualIndex = new VisualIndexHandler(webServiceHost, collection);
 	}
 	
 	public List<List<String>> cluster(List<MediaItem> mediaItemsList) {
 		int m=0;
 		//Map<String, String> mediaItemsRefs = new HashMap<String, String>();
 		Graph<String, GraphLink> mediaItemsGraph = new UndirectedSparseGraph<String, GraphLink>();
 		for(MediaItem mediaItem : mediaItemsList) {
 			if((m++)%2000==0){
 				System.out.print(".");
 			}
 			String id = mediaItem.getId();
 			//if(mediaItemsRefs.containsKey(id))
 			//	continue;
 			
 			//mediaItemsRefs.put(id, mediaItem.getRef());
 			mediaItemsGraph.addVertex(id);
 			
			NearestImage[] similarImages = visualIndex.getSimilarImages(id, THRESHOLD);
 			if(similarImages != null) {
				for(NearestImage nearestImage : similarImages) {
					if(!mediaItemsGraph.containsVertex(nearestImage.id))
						mediaItemsGraph.addVertex(nearestImage.id);
					mediaItemsGraph.addEdge(new GraphLink(nearestImage.similarity), id, nearestImage.id);
 				}
 			}
 		}
 		
 		System.out.println("\nScan community");
 		ScanCommunityDetector<String, GraphLink> detector = new ScanCommunityDetector<String, GraphLink>(SCAN_EPSILON, SCAN_MU);
 		
 		ScanCommunityStructure<String, GraphLink> structure = 
 				detector.getCommunityStructure(mediaItemsGraph);
 		
 		int n_communities = structure.getNumberOfCommunities();
 		List<List<String>> clusters = new ArrayList<List<String>>(n_communities);
 		for(int i=0; i<n_communities; i++) {
 			Community<String, GraphLink> community = structure.getCommunity(i);
 			if(community != null) {
 				int n_members = community.getNumberOfMembers();
 				List<String> cluster = new ArrayList<String>(n_members);
 				for(int j=0;j<n_members;j++) {
 					String member = community.getMembers().get(j);
 					//String ref = mediaItemsRefs.get(member);
 					cluster.add(member);
 				}
 				clusters.add(cluster);
 			}
 		}	
 		
 		return clusters;
 	}
 	
 	private static void saveClusteringResults(List<List<String>> clusters, Map<String, DBObject> ids, 
 			HttpSolrServer solr, DBCollection output) {	
 		
 		long ts = System.currentTimeMillis();
 		//insert.add("ts", ts);
 		//for(String key : metadata.keySet()) {
 		//	insert.add(key, metadata.get(key));
 		//}
 			
 		//insert.add("SCAN_EPSILON", SCAN_EPSILON);
 		//insert.add("SCAN_MU", SCAN_MU);
 		
 		int c_id = 1; 
 		for(List<String> cluster : clusters) {
 			List<DBObject> temp = new ArrayList<DBObject>();
 			for(String member : cluster) {
 				
 				DBObject item = ids.get(member);
 				if(item == null)
 					continue;
 				
 				item.removeField("width");
 				item.removeField("height");
 				item.removeField("location");
 				item.removeField("indexed");
 				item.removeField("_id");
 				
 				temp.add(item);
 			}
 			
 			rank(temp);
 			
 			if(temp.size()>1) {
 				String id = ts + Integer.toString(c_id++);
 				
 				BasicDBObjectBuilder insert = BasicDBObjectBuilder.start();
 				
 				insert.add("_id", id);
 				insert.add("items", temp);
 				insert.add("timestamp", ts);
 				
 				output.insert(insert.get());
 				
 				String text = aggregateText(temp);
 				
 				if(solr != null) {
 					try {
 						SolrInputDocument doc = new SolrInputDocument();
 						doc.addField("id", id);
 						doc.addField("time", ts);
 						doc.addField("text", text);
 						solr.add(doc);
 					} catch (Exception e) {
 						System.out.println("Exception in Solr: " + e.getMessage());
 					} 
 				}
 				
 			}
 		}
 
 		if(solr != null) {
 			try {
 				solr.commit();
 			} catch (Exception e) {
 				System.out.println("Exception in Solr commit: " + e.getMessage());
 			} 
 		}
 	}
 
 	private static void rank(List<DBObject> list) {
 		Collections.sort(list, new Ranker());
 	}
 
 	private static class Ranker implements Comparator<DBObject> {
 
 		@Override
 		public int compare(DBObject obj1, DBObject obj2) {
 			Long t1, t2;
 			try{
 				 t1 = (Long) obj1.get("publicationTime");
 			}
 			catch(Exception e) {
 				t1 = ((Integer) obj1.get("publicationTime")).longValue();
 			}
 			try{
 				t2 = (Long) obj2.get("publicationTime");
 			}
 			catch(Exception e){
 				t2 = ((Integer) obj2.get("publicationTime")).longValue();
 			}
 			return (int) (t2 - t1);
 		}
 		
 	}
 	
 	private static String aggregateText(List<DBObject> items) {
 	
 		StringBuffer aggregated_text = new StringBuffer();
 	
 		for(DBObject item : items) {
 			String text = (String) item.get("title");
 			if(text != null) {
 				aggregated_text.append(text + " ");
 			}
 		}
 		return aggregated_text.toString();
 	}
 
 	/**
 	 * @param args
 	 * @throws UnknownHostException 
 	 */
 	public static void main(String[] args) throws UnknownHostException {
 		MongoClient m = new MongoClient("160.40.50.207");
 		DB database = m.getDB("MMdemoStreams");
 		
 		DBCollection output = database.getCollection("visual_clusters");
 		DBCollection input = database.getCollection("MediaItems");
 		
 
 		HttpSolrServer solr = new HttpSolrServer("http://160.40.50.207:8080/solr/VisualDyscos2");
 		
 		VisualClusterer visualClusterer = new VisualClusterer("http://160.40.50.207:8080/VisualIndexService", "mmdemo");
 		while(true) {
 			
 			
 			List<MediaItem> mItems = new ArrayList<MediaItem>();
 			Map<String, DBObject> map = new HashMap<String, DBObject>();
 			
 			DBCursor c = input.find(new BasicDBObject("indexed", true)).sort(new BasicDBObject("publicationTime", -1))
 					.limit(40000);
 			System.out.println(c.size() + " media items to cluster");
 			while(c.hasNext()) {
 				DBObject obj = c.next();
 				MediaItem mi = ItemFactory.createMediaItem(obj.toString());
 //				if(mi.getScreenname()==null){
 //					mi.setScreenname(mi.getAuthor());
 //				}
 				map.put(mi.getId(), obj);
 				mItems.add(mi);
 			}
 			
 			
 			List<List<String>> clusters = visualClusterer.cluster(mItems);
 			
 			System.out.println("Save clustering results: "+clusters.size());
 			saveClusteringResults(clusters, map, solr, output);
 			
 			try {
 				Thread.sleep(1000*60*20);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 				continue;
 			}
 		}
 	}
 
 }
