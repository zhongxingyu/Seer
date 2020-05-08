 package su.mvnlab1;
 import java.net.UnknownHostException;
 
 import net.sf.json.JSONObject;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 import com.mongodb.MongoException;
 import com.mongodb.util.JSON;
 
 /**
  * Hello world!
  *
  */
 public class App 
 {
     public static void main( String[] args ) throws Exception
     {
     	App app = new App();
     	//app.dbInsert();
     	PeopleData people = new PeopleData();
     	people.setName("Arvind");
     	people.setAge(29);
     	people.setType("Lead");
     	System.out.println(app.getJSON(people));
     	app.dbInsert("people", (DBObject)JSON.parse(app.getJSON(people)));
     	
     }
 
     
 	private void dbInsert(String mdbCollectionName, DBObject dbObject) {
 		try {
 			DB db = getDBConnection();
 			DBCollection dbCollection = db.getCollection(mdbCollectionName);
 			dbCollection.insert(dbObject);
 			
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (MongoException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 
 	private DB getDBConnection() throws UnknownHostException {
 		Mongo mongo = new Mongo("localhost", 27017);
 		DB db = mongo.getDB("test");
 		return db;
 	}
 
 	public String getJSON(Object obj){
 		JSONObject jsonObject = JSONObject.fromObject(obj);
 		return jsonObject.toString();
 		}
     
 }
