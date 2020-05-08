 package bixi.hbase.upload;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Put;
 
 import bixi.dataset.cluster.XQuadTreeClustering;
 import bixi.dataset.collection.XStation;
 import bixi.hbase.query.BixiConstant;
 
 public class TableInsertCluster {
 
 	  static Configuration conf = HBaseConfiguration.create();
 	  HTable table;
 	  static byte[] tableName = BixiConstant.SCHEMA2_CLUSTER_TABLE_NAME.getBytes();
 	  static byte[] idsFamily = BixiConstant.SCHEMA2_CLUSTER_FAMILY_NAME.getBytes();
 	 	  
 	  
 	  /**
 	   * @throws IOException
 	   */
 	  public TableInsertCluster() throws IOException {
 	    table = new HTable(conf, tableName);    
 	    table.setAutoFlush(true);
 	    
 	  }	  
 	 
 	  public static void main(String[] args) throws ParserConfigurationException, IOException { 		  
 		  TableInsertCluster inserter = new TableInsertCluster();
 		  inserter.insertRow();
 	  }	  
 	  
 	  
 	  public void insertRow(){
 		  
 		XQuadTreeClustering clustering = new XQuadTreeClustering();
 		File dir = new File("data2");
 		String filename = dir.getAbsolutePath()+"/01_10_2010__00_00_01.xml";
 		clustering.doClustering(filename);
 		clustering.aggreateCluster();
 		
 		Hashtable<String,List<String>> cluster_structure = clustering.getClusters();
 		int row = 0;
 	  try{
 			Set<String> keys = cluster_structure.keySet();
 			Iterator<String> ie = keys.iterator();				
 			while(ie.hasNext()){
 				String cluster = ie.next();				
 				
 				Put put = new Put(cluster.getBytes());
 				
 				Iterator<String> ids = cluster_structure.get(cluster).iterator();				
 				while(ids.hasNext()){					
 					String stationId = ids.next();	
					
 					put.add(idsFamily, stationId.getBytes(), clustering.getOneStation(stationId).getMetadata().getBytes());
 				}	
 			    //System.out.println(new String(put.getRow()));
 				row++;
 			    table.put(put);				
 			}
     
 	  }catch(Exception e){
 		  e.printStackTrace();
 	  }
 	  System.out.println("inserted row : "+row);
 	  
 	 }
 
 }
