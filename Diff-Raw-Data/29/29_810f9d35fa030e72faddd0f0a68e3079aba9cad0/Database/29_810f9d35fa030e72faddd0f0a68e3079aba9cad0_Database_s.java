 import java.util.*;
 
 import org.openjena.atlas.iterator.Filter;
 import org.openjena.atlas.lib.Tuple;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.query.Dataset;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.ResultSetFormatter;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.sparql.core.Quad;
 import com.hp.hpl.jena.sparql.sse.SSE;
 import com.hp.hpl.jena.sparql.vocabulary.FOAF;
 import com.hp.hpl.jena.tdb.TDB;
 import com.hp.hpl.jena.tdb.TDBFactory;
 import com.hp.hpl.jena.tdb.nodetable.NodeTable;
 import com.hp.hpl.jena.tdb.store.DatasetGraphTDB;
 import com.hp.hpl.jena.tdb.store.NodeId;
 import com.hp.hpl.jena.tdb.sys.SystemTDB;
 import com.hp.hpl.jena.vocabulary.VCARD;
 
 /**
  * Example of how to filter quads as they are accessed at the lowest level. Can
  * be used to exclude daat from specific graphs. This mechanism is not limited
  * to graphs - it works for properties or anything where the visibility of
  * otherwise is determined by the elements of the quad. See <a
  * href="http://openjena.org/wiki/TDB/QuadFilter">TDB/QuadFilter</a> for further
  * details.
  */
 
 public class Database {
 	private static String graphToHide = "http://example/g2";
 	
 	Map<String,Resource> loc_to_res;
 	Map<String,Resource> tcl_to_res;
 	Map<String,Resource> per_to_res;
 	List<Person> celebs;
 	Model model;
 	
 	public Database(List<Person> celebs){
 		this.celebs = celebs;
 		loc_to_res = new HashMap<String,Resource>();
 		tcl_to_res = new HashMap<String,Resource>();
 		per_to_res = new HashMap<String,Resource>();
 		String directory = "../TDB-0.8.10/work/data/project2";
 		model = TDBFactory.createModel(directory);
 		model.removeAll();
 	}
 	
 	private Resource setLocation(Resource zzz, Location l){
 		if(l != null){
 			Resource locres = null;
 			if(loc_to_res.containsKey(l.name)){
 				locres = loc_to_res.get(l.name);
 			}
 			else{
 				Resource r = model.createResource(Ctalkology.Location)
 					.addLiteral(Ctalkology.latitude, l.latitude)
 					.addLiteral(Ctalkology.longitude, l.longitude);
 				if(l.populationTotal > 0)
 					r.addLiteral(Ctalkology.population, l.populationTotal);
 				if(l.areaTotal > 0)
 					r.addLiteral(Ctalkology.landArea, l.areaTotal);
 				if(l.city != null && l.city.length() > 0)
 					r.addProperty(Ctalkology.city, l.city);
 				if(l.country != null && l.country.length() > 0)
 					r.addProperty(Ctalkology.country, l.country);
 					
 				loc_to_res.put(l.name,r);
 				locres = r;
 			}
 			zzz.addProperty(Ctalkology.hasLocation, locres);
 		}
 		return zzz;
 	}
 	
 	private Resource setTwitterClient(Resource zzz, Person p){
 		if(p.twitterClient != null && p.twitterClient.length() > 0){
 			Resource tcl = null;
 			if(tcl_to_res.containsKey(p.twitterClient)){
 				tcl = tcl_to_res.get(p.twitterClient);
 			}
 			else{
 				tcl = model.createResource(Ctalkology.TwitterClient)
 					.addLiteral(FOAF.name, p.twitterClient);
 				tcl_to_res.put(p.twitterClient, tcl);
 			}
 			zzz.addProperty(Ctalkology.hasTwitterClient, tcl);
 		}
 		return zzz;
 	}
 	
 	public void run(){
 		Resource loc = model.createResource(Ctalkology.Location)
 		.addProperty(Ctalkology.city, "New York")
 		.addLiteral(Ctalkology.latitude, 40.7141667)
 		.addLiteral(Ctalkology.longitude, -74.0063889)
 		.addLiteral(Ctalkology.population, 8);
 		for (Person d : celebs) {
 			Celeb c = (Celeb) d;
 			// Create Celebrity
 			Resource celeb_resource = model.createResource(Ctalkology.Celebrity)
 				.addLiteral(FOAF.name, c.actualName)
 				.addProperty(Ctalkology.twitterId, c.twitterID);
 			System.out.println(c.actualName);
 			System.out.println(c.twitterID);
 			
 			
 			setTwitterClient(celeb_resource, c);
 			setLocation(celeb_resource, c.location);
 			
 			// Add Events
 			for (Evt e : c.events) {
 				System.out.println("Adding an event!");
 				Resource ev = model.createResource(Ctalkology.Event)
 					.addLiteral(FOAF.name, e.title);
 				setLocation(ev, e.location);
 				celeb_resource.addProperty(Ctalkology.hasEvent, ev);
 			}
 			
 			// Add Mentioners
 			for (Person p : c.talkers){
 				Resource ps = null;
 				if(per_to_res.containsKey(p.screenName)){
 					ps = per_to_res.get(p.screenName);
 				}
 				else{
 					ps = model.createResource(Ctalkology.TwitterUser);
 					if(p.actualName.length() > 0)
 						ps.addLiteral(FOAF.name, p.actualName);
 					if(p.twitterID != null)
 						ps.addProperty(Ctalkology.twitterId, p.twitterID);
 					setLocation(ps, p.location);
 					setTwitterClient(ps, p);
 					per_to_res.put(p.screenName, ps);
 				}
 				ps.addProperty(Ctalkology.mentioned, celeb_resource);
 			}
 			
 			// Add Followers
 			for (Person p : c.talkers){
 				Resource ps = null;
 				if(per_to_res.containsKey(p.screenName)){
 					ps = per_to_res.get(p.screenName);
 				}
 				else{
 					ps = model.createResource(Ctalkology.TwitterUser);
 					if(p.actualName.length() > 0)
 						ps.addLiteral(FOAF.name, p.actualName);
 					if(p.twitterID != null)
 						ps.addProperty(Ctalkology.twitterId, p.twitterID);
 					setLocation(ps, p.location);
 					setTwitterClient(ps, p);
 					per_to_res.put(p.screenName, ps);
 				}
 				ps.addProperty(Ctalkology.follows, celeb_resource);
 			}
 			
 			// HACK Set some random Twitter client if doesn't exist
 			if(c.twitterClient == null && tcl_to_res.size() > 0){
				Resource[] values = (Resource[]) tcl_to_res.values().toArray();
				celeb_resource.addProperty(Ctalkology.hasTwitterClient, values[new Random().nextInt(values.length)]);
 			}
 		}
 		
 		model.close();
 	}
 	
 	public static void main(String... args) {
 		String directory = "../TDB-0.8.10/work/data/project2";
 		Model model = TDBFactory.createModel(directory);
 		model.removeAll();
 		
 		Resource loc = model.createResource(Ctalkology.Location)
 		.addProperty(Ctalkology.city, "New York")
 		.addLiteral(Ctalkology.latitude, 40.7141667)
 		.addLiteral(Ctalkology.longitude, -74.0063889)
 		.addLiteral(Ctalkology.population, 8);
 	
 	Resource loc2 = model.createResource(Ctalkology.Location)
 		.addProperty(Ctalkology.city, "San Francisco")
 		.addLiteral(Ctalkology.latitude, 37.775)
 		.addLiteral(Ctalkology.longitude, -122.4183333)
 		.addLiteral(Ctalkology.population, 100);
 	
 	Resource tc = model.createResource(Ctalkology.TwitterClient)
 	.addLiteral(FOAF.name, "Tweetie"); 
 	
 	Resource ev = model.createResource(Ctalkology.Event)
 		.addLiteral(FOAF.name, "Calevent")
 		.addProperty(Ctalkology.hasLocation, loc2);
 		
 		Resource lg = model.createResource(Ctalkology.Celebrity)
 		.addLiteral(FOAF.name, "Lady Gaga")
 		.addProperty(Ctalkology.hasTwitterClient, tc)
 		.addProperty(Ctalkology.hasEvent, ev)
 		.addProperty(Ctalkology.hasLocation, loc)
 		.addLiteral(Ctalkology.twitterId, "buggy")
 		.addLiteral(Ctalkology.city, "Sanny");
 		
 		model.close();
 		
 //		Database db = new Database(LIST OF CELEBRITIES);
 //		db.run();
 //		
 //		// Sample Code
 //		Resource loc = model.createResource(Ctalkology.Location)
 //			.addProperty(Ctalkology.city, "New York")
 //			.addLiteral(Ctalkology.latitude, 40.7141667)
 //			.addLiteral(Ctalkology.longitude, -74.0063889)
 //			.addLiteral(Ctalkology.population, 8);
 //		
 //		Resource loc2 = model.createResource(Ctalkology.Location)
 //			.addProperty(Ctalkology.city, "San Francisco")
 //			.addLiteral(Ctalkology.latitude, 37.775)
 //			.addLiteral(Ctalkology.longitude, -122.4183333)
 //			.addLiteral(Ctalkology.population, 100);
 //		
 //		Resource ev = model.createResource(Ctalkology.Event)
 //			.addLiteral(FOAF.name, "Calevent")
 //			.addProperty(Ctalkology.hasLocation, loc2);
 //		
 //		Resource tc = model.createResource(Ctalkology.TwitterClient)
 //			.addLiteral(FOAF.name, "Tweetie"); 
 //		Resource lg = model.createResource(Ctalkology.Celebrity)
 //			.addLiteral(FOAF.name, "Lady Gaga")
 //			.addProperty(Ctalkology.hasTwitterClient, tc)
 //			.addProperty(Ctalkology.hasEvent, ev)
 //			.addProperty(Ctalkology.hasLocation, loc);
 //		Resource lgfan = model.createResource(Ctalkology.TwitterUser)
 //			.addLiteral(FOAF.name, "Gagaga") .addProperty(Ctalkology.follows, lg)
 //			.addProperty(Ctalkology.hasLocation, loc2);
 //		Resource lgfan2 = model.createResource(Ctalkology.TwitterUser)
 //			.addLiteral(FOAF.name, "Kakaka")
 //			.addProperty(Ctalkology.mentioned, lg)
 //			.addProperty(Ctalkology.hasLocation, loc)
 //			.addProperty(Ctalkology.hasTwitterClient, tc);
 		
 	}
 
 }
