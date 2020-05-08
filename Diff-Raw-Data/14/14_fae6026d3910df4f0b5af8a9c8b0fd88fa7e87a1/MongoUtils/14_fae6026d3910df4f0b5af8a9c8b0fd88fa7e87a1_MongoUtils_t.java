 package edu.stanford.eightyfriends;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 import com.mongodb.MongoException;
 
 /* Interface to Mongo DB */
 public class MongoUtils {
 	static Logger log = Logger.getLogger(MongoUtils.class);
 
 	/* note: friends info is kept asymmetrical (deliberately).
 	 * i.e. <p1, p2> is stored as well as <p2, p1> in case in future we want
 	 * assumed that X is interested in all people Y for whom there exists a tuple <X, Y>
 	 */
 	static Mongo m;
 	static DB db;
 	static {
 		try { m = new Mongo( "localhost", 27017); 		
 			db = m.getDB("test");
 
 			//ensure indexes on the cols that are perf critical
			BasicDBObject o;
 
			o = (BasicDBObject) com.mongodb.util.JSON.parse("{f1:1,f2:1}");	db.getCollection("friends").ensureIndex(o);
			o = (BasicDBObject) com.mongodb.util.JSON.parse("{id:1}"); db.getCollection("needs").ensureIndex(o); // needs doesn't need index on code i think?
			o = (BasicDBObject) com.mongodb.util.JSON.parse("{id:1,code:1}"); db.getCollection("locations").ensureIndex(o);
			o = (BasicDBObject) com.mongodb.util.JSON.parse("{id:1}"); db.getCollection("names").ensureIndex(o);

		} catch (Exception e) { System.err.println ("Unable to initialize Mongo"); }
 	}
 
 	/** #friends this user has */
 	public static int getNumFriends(String id)
 	{
 		DBCollection coll = db.getCollection("friends");	
 		String json = "{'f1':'" + id + "'}"; // id has to be in quotes
 		Set<String> friends = readFieldFromCollForSelector("friends", "f1", id, "f2");
 		return friends.size();
 	}
 		
 	public static boolean haveIdInFriendsTable(String id)
 	{
 		DBCollection coll = db.getCollection("friends");	
 		// remove existing friendships for this user first
 		String json = "{'f1':'" + id + "'}"; // id has to be in quotes
 		BasicDBObject o = (BasicDBObject) com.mongodb.util.JSON.parse(json);		
 		return coll.findOne(o) != null;
 	}
 	
 	public static void setName(String id, String name) throws JSONException
 	{
 		DBCollection coll = db.getCollection("names");	
 		
 		// remove existing name for this user first
 		String json = "{'id':'" + id + "'}"; // id has to be in quotes
 		BasicDBObject o = (BasicDBObject) com.mongodb.util.JSON.parse(json);		
 		coll.remove(o);
 
 		// safer to create json obj - name might have quotes etc
 		JSONObject j = new JSONObject();
 		j.put("id", id);
 		j.put("name", name);
 		o = (BasicDBObject) com.mongodb.util.JSON.parse(j.toString());		
 		coll.insert(o);
 	}
 	
 
 	public static void clearFriends(String id) throws UnknownHostException, MongoException
 	{
 		DBCollection coll = db.getCollection("friends");	
 		// remove existing friendships for this user first
 		String json = "{'f1':'" + id + "'}"; // id has to be in quotes
 		BasicDBObject o = (BasicDBObject) com.mongodb.util.JSON.parse(json);		
 		coll.remove(o);
 		json = "{'f2':'" + id + "'}"; // id has to be in quotes
 		o = (BasicDBObject) com.mongodb.util.JSON.parse(json);		
 		coll.remove(o);
 	}
 	
 	public static void addFriend(String f1_id, String f2_id) throws UnknownHostException, MongoException
 	{
 		DBCollection coll = db.getCollection("friends");	
 		String json = "{f1:'" + f1_id + "',f2:'" + f2_id + "'}";		
 		BasicDBObject o = (BasicDBObject) com.mongodb.util.JSON.parse(json);		
         coll.insert(o);
 	}
 
 	public static void clearLocations(String id)
 	{
 		DBCollection coll = db.getCollection("locations");		
 
 		// remove existing locations for this user first
 		String json = "{'id':'" + id + "'}"; // id has to be in quotes
 		BasicDBObject o = (BasicDBObject) com.mongodb.util.JSON.parse(json);		
 		coll.remove(o);
 	}
 
 	public static void addLocation(String id, String code) throws UnknownHostException, MongoException
 	{
 		DBCollection coll = db.getCollection("locations");		
 
 		String json = "{id:'" + id + "',code:'" + code + "'}";
 		BasicDBObject o = (BasicDBObject) com.mongodb.util.JSON.parse(json);		
         coll.insert(o);
 	}
 
 	public static int howManyNeeds(String id)
 	{
 		Set<String> needs = readFieldFromCollForSelector("needs", "id", id, "code");
 		return needs.size();
 	}
 
 	/* automatically clears previous needs */
 	public static void addNeeds(String id, String[] codes) throws UnknownHostException, MongoException
 	{
 		DBCollection coll = db.getCollection("needs");
 
 		// remove existing needs for this user first
 		String json = "{'id':'" + id + "'}"; // id has to be in quotes
 		BasicDBObject o = (BasicDBObject) com.mongodb.util.JSON.parse(json);		
 		coll.remove(o);
 		
 		for (String code: codes)
 		{
 			json = "{'id':'" + id + "', \"code\":'" + code + "'}";
 			BasicDBObject o1 = (BasicDBObject) com.mongodb.util.JSON.parse(json);		
 			coll.insert(o1);
 		}
 	}
 
 	/** return pairs of people who need each other */
 	public static void generateAllMatches()
 	{
 		DBCollection coll = db.getCollection("needs");
 		List<String> ids = (List) coll.distinct("id");
 		for (String id: ids)
 			generateMatches(id);
 	}	
 
 	/** returns sorted list of person info's for the leaderboard */
 	public static List<PersonInfo> generateLeaderboard(String id)
 	{
 		Map<String, Collection<String>> result = new LinkedHashMap<String, Collection<String>>();
 
 		// read all my friends' locs
 		Set<String> friends = readFieldFromCollForSelector("friends", "f1", id, "f2");
 		friends.addAll(readFieldFromCollForSelector("friends", "f2", id, "f1"));
 		
 		List<PersonInfo> list = new ArrayList<PersonInfo>();
 		PersonInfo me = PersonInfo.computePersonInfo(id);
 		list.add(me);
 		
 		// and all my friends needs
 		for (String friendID: friends) 
 		{
 			if (!haveIdInFriendsTable(friendID))
 				continue;
 			
 			PersonInfo p = PersonInfo.computePersonInfo(friendID);
 			list.add(p);
 		}			
 		Collections.sort(list);
 		return list;
 	}
 	
 	public static void wipeDataForId(String id)
 	{
 		log.info ("wiping data for id " + id);
 		String json = "{'id':'" + id + "'}"; // id has to be in quotes
 		BasicDBObject o = (BasicDBObject) com.mongodb.util.JSON.parse(json);		
 
 		db.getCollection("needs").remove(o);
 		db.getCollection("locations").remove(o);
 		db.getCollection("names").remove(o);
 		
 		json = "{'f1':'" + id + "'}"; // id has to be in quotes
 		o = (BasicDBObject) com.mongodb.util.JSON.parse(json);
 		db.getCollection("friends").remove(o);
 	}
 
 	public static String getNameForId(String id)
 	{
 		DBCollection dbc = db.getCollection("names");		
 		BasicDBObject query = new BasicDBObject("id", id);
 		DBObject o = dbc.findOne(query);
 		if (o == null)
 			return "";
 		return (String) o.get("name");
 	}
 	
 	/* Pair up people */
 	public static Collection<Match> generateMatches(String id)
 	{
 		Set<String> myLocs = readFieldFromCollForSelector("locations", "id", id, "code");
 		Set<String> myNeeds = readFieldFromCollForSelector("needs", "id", id, "code");
 		Set<Match> result = new LinkedHashSet<Match>();
 		for (String need: myNeeds)
 		{
 			// who has this need in his locs?
 			Set<String> idsMatchingNeed = readFieldFromCollForSelector("locations", "code", need, "id");
 			for (String other_id: idsMatchingNeed)
 			{
 				// what are his needs?
 				Set<String> otherNeeds = readFieldFromCollForSelector("needs", "id", other_id, "code");
 				// intersect his needs with my locs
 				otherNeeds.retainAll(myLocs);
 				if (otherNeeds.size() > 0)
 				{
 					Set<String> otherLocs = readFieldFromCollForSelector("locations", "id", other_id, "code");
 					Match match = new Match();
 					match.id1 = id; match.name1 = getNameForId(id);
 					match.id2 = other_id; match.name2 = getNameForId(other_id);
 					match.id1_locs = myLocs;
 					match.id2_locs = otherLocs;
 					if (!result.contains(match))
 						result.add(match);
 					break; // no need of further intersections between id and other_id
 				}
 			}
 		}
 		return result;
 	}
 	
 	/** util function -- 
 	 * returns a set of strings in the retunrField for the given collection for the rows that match selectorField = selectorVal */
 	public static Set<String> readFieldFromCollForSelector(String coll, String selectorField, String selectorVal, String returnField)
 	{
 		DBCollection dbc = db.getCollection(coll);		
 		BasicDBObject query = new BasicDBObject(selectorField, selectorVal);
 
 		DBCursor cursor = dbc.find(query);
 		Set<String> set = new LinkedHashSet<String>();
 		while (cursor.hasNext())
 		{
 			DBObject doc = cursor.next();
 			String code = (String) doc.get(returnField);
 			set.add(code);
 		}
 		return set;
 	}
 
 }
