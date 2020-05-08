 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.bson.BasicBSONObject;
 import org.bson.types.ObjectId;
 
 
 import com.mongodb.*;
 
 
 public class Interprete {
 
 static DB db;
 
 	static {
 		try {
 			System.out.print("Ouverture de la base....");
 			Mongo mongo = new Mongo( "138.195.76.136"  , 80 );
 			db = mongo.getDB( "seewa1" );
 			System.out.println("[done]");
 		}
 		catch (UnknownHostException ex) {
 			ExceptionRecoNotValid erreur = new ExceptionRecoNotValid(ExceptionRecoNotValid.ERR_CONNECTION_DB);
 			System.out.println("Erreur :"+erreur.getCode());
 			System.exit(1);
 		}
 	}
 	
 	static protected DataUserNode db2DataUserNodeHard(ObjectId mongoID) {
 		DBCollection coll = db.getCollection("users");
 		BasicDBObject query = new BasicDBObject("_id",mongoID);
 		DBObject user = coll.findOne(query);
 		
 		
 		BasicDBObject recommendersMongo = (BasicDBObject) user.get("recommenders");
 		ArrayList<DataUserRelation> recommenders = new ArrayList<DataUserRelation>();
 		
 		//TODO : la suite peut ptet etre amélioré en regroupant tout dans une requête
 		
 		for (String recommender : recommendersMongo.keySet()) {
 			ObjectId _id = (ObjectId) ((BasicDBObject) recommendersMongo.get(recommender)).get("_id");
 			double crossProbability = (Double) ((BasicDBObject) recommendersMongo.get(recommender)).get("crossProbability");
 			int posFeedback = (Integer) ((BasicDBObject) recommendersMongo.get(recommender)).get("posFeedback");
 			int negFeedback = (Integer) ((BasicDBObject) recommendersMongo.get(recommender)).get("negFeedback");
 			
 			DataUserNode usernode = db2DataUserNodeSimple(_id);
 			DataUserRelation userrelation = new DataUserRelation(usernode,crossProbability,posFeedback,negFeedback);
 			recommenders.add(userrelation);
 		}
 		
 		DataUserNode usernode = db2DataUserNodeSimple(mongoID);
 		usernode.setFriends(recommenders);	
 		return usernode;
 	}
 	
 	
 	
 	static protected DataUserNode db2DataUserNodeSimple(ObjectId userId) {
 		
 		
 		DBCollection upages = db.getCollection("upages");
 		BasicDBObject query = new BasicDBObject("user",userId);
 		DBCursor pageviewedbyuser = upages.find(query);
 
 		ArrayList<DataUPage> userupages = new ArrayList<DataUPage>();
 		
 		
 		while (pageviewedbyuser.hasNext()) {
 			DBObject upage = pageviewedbyuser.next();
 			double pagerank = (Double) upage.get("pageRank");
 			ObjectId id = (ObjectId) upage.get("_id");
 			String url =(String) upage.get("url");
 			DataUPage dataupage = new DataUPage(id, userId, pagerank,url);
 			userupages.add(dataupage);
 		}
 		
 		DataUserNode usernode = new DataUserNode(userId,userupages);
 		return usernode;
 	}
 	
 	static protected void modifyFeedback(ObjectId recommender_id , ObjectId receiver_id , boolean feedback) {
 		DBCollection coll = db.getCollection("users");
 		BasicDBObject query = new BasicDBObject("_id", receiver_id);
 		DBObject user = coll.findOne(query);
 		
 		BasicDBObject recommenders = (BasicDBObject) user.get("recommenders");
 		BasicDBObject recommender = (BasicDBObject) recommenders.get(recommender_id.toString());
 		if (recommender ==null ) {
			//System.out.println("on a un feebdback qui parle dutlisateur qui ne sont pas en relation...cest moche (ou alors ce sont des données de test)");
 			return;
 		}
 		
 		if (feedback == true) {
 			int posFeedback = (Integer) recommender.get("posFeedback");
 			
 			recommender.put("posFeedback",posFeedback+1);
 		}
 		
 		else {
 			int negFeedback = (Integer) recommender.get("negFeedback");
 			
 			recommender.put("negFeedback",negFeedback+1);
 		}
 		recommenders.put(recommender_id.toString(), recommender);
 		user.put("recommenders",recommenders);
 		
 		coll.update(query, user,true,false);
 		
 	}
 	
 	
 
 
 	
 	static protected void DataUserNode2db(DataUserNode user) {
 		//cette fonction ecrase tous les fields qui sont present dans lobjet user
 		DBCollection coll = db.getCollection("users");
 		BasicDBObject query = new BasicDBObject("_id", user.getMongoId());
 		DBObject userMongo = coll.findOne(query);
 		
 		if (userMongo==null) {
 			System.out.println("You are creating a new user in the database... are you really sure ?");
 			System.out.println("his Objectid is "+user.getMongoId());
 			System.out.println("you should not ignore that warning, unless you are creating some random data");
 			userMongo = new BasicDBObject();
 			userMongo.put("_id", user.getMongoId());
 		}
 		
 		
 		BasicDBObject updatedRecommenders = new BasicDBObject();
 
 		for (DataUserRelation friend : user.getFriends()) {
 			BasicDBObject recommender = new BasicDBObject();
 			recommender.put("_id", friend.getFriend().getMongoId());
 			recommender.put("crossProbability", friend.getCrossProbability());
 			recommender.put("posFeedback", friend.getPosFeedback());
 			recommender.put("negFeedback", friend.getNegFeedback());
 			updatedRecommenders.put(friend.getFriend().getId().toString(), recommender);
 		}
 		if (! (updatedRecommenders.size()==0)) { 
 			userMongo.put("recommenders", updatedRecommenders);
 		}
 		coll.update(query, userMongo,true,false);
 	}
 	
 
 	public static ArrayList<ObjectId> getUserList() {
 		DBCollection coll = db.getCollection("users");
 		BasicDBObject keys = new BasicDBObject("_id",1);
 		
 		DBCursor cursor = coll.find(new BasicDBObject(), keys);
 		ArrayList<ObjectId> results = new ArrayList<ObjectId>();
 		DBObject user= null;
 		while(cursor.hasNext()) {
 			user = cursor.next();
 			results.add((ObjectId)user.get("_id") );
 		}
 		return results;
 			
 	
 	}
 
 
 
 	public static void setCrossProbability(ObjectId user_Id, ObjectId recommander_id, double crossProbability) {
 
 		DBCollection coll = db.getCollection("users");
 		
 
 		BasicDBObject fields = new BasicDBObject();
 		fields.put("recommenders", 1);
 		
 		BasicDBObject query = new BasicDBObject();
 		query.put("_id", user_Id);
 		
 		BasicDBObject user = (BasicDBObject)coll.findOne(query,fields);
 		if (user==null) {System.out.println("Something went wrong ! le user est null");} //TODO lever une exception
 		((BasicDBObject)((BasicDBObject)user.get("recommenders")).get(recommander_id.toString())).put("crossProbability",crossProbability);
 		coll.update(query, user,true,false);
 		//TODO verifier quon ecrase pas les donnees
 		
 	}
 	
 	public static void DataUPage2db(DataUPage p) {
 		System.out.println("Attention : on ecrase les pages deja existantes ?");
 		DBCollection coll = db.getCollection("upages");
 		BasicDBObject o = new BasicDBObject();
 		o.put("_id", p.getMongoId());
 		o.put("pageRank", p.pageRank);
 		o.put("url", p.getUrl());
 		o.put("user", p.getUserId());
 		DBObject obj = new BasicDBObject();
 		obj.put("_id", p.getMongoId());
 		
 		coll.update(
 				obj,
 				(DBObject)o, 
 				true, /* i want to create the object if it doesnt exist*/
 				false /* do i want to update multiple items : no */
 				);
 	}
 
 
 
 	public static ArrayList<DataFeedBack> getFeedback() {
 		ArrayList<DataFeedBack> feedbackList = new ArrayList<DataFeedBack>();
 		DBCollection coll = db.getCollection("feedback");
 		
 		DBCursor cursor = coll.find(new BasicDBObject());
 		DBObject feedback= null;
 		while(cursor.hasNext()) {
 			feedback = cursor.next();
 			ObjectId objectid = (ObjectId)feedback.get("_id");
 			ObjectId recoGiver = (ObjectId)feedback.get("recoGiver");
 			ObjectId recoReceiver = (ObjectId)feedback.get("recoReceiver");
 			Boolean clicked = (Boolean)feedback.get("clicked");
 			feedbackList.add(new DataFeedBack(objectid, clicked, recoGiver, recoReceiver));
 		}
 		coll.drop();
 		return feedbackList;
 	}
 
 
 	public static void setFeedBack(DataFeedBack f) {
 		DBCollection coll = db.getCollection("feedback");
 		BasicDBObject o = new BasicDBObject();
 		o.put("_id", f.getMongoId());
 		o.put("recoGiver", f.recoGiver());
 		o.put("recoReceiver", f.recoReceiver());
 		o.put("clicked", f.clicked());
 		
 		coll.insert(o);
 		
 	}
 	
 	public static void generateRandomBDD(int nbUser, int nbUPages)
 	{
 		ArrayList<DataUserNode> users = new ArrayList<DataUserNode>();
 		Date t = new Date();
 		for (int i=0; i<nbUser; i++) {
 			String nom = "user"+i;
 			users.add( new DataUserNode(new ObjectId(t, i),new ArrayList<DataUPage>()));
 		}
 		
 		int var;
 		for (int i=0; i<nbUser; i++){
 			for (int j=0; j< 2 /*nbFriends*/ ; j++)
 			{
 				var= (int) Math.floor(Math.random()*(nbUser-1));//nbUser-1 possible!
 				if (var>=i) var++;//pr eviter d'etre ami avec soi-meme
 				users.get(i).addFriend(new DataUserRelation(users.get(var), 0, 0, 0));
 			}
 			System.out.println(users.get(i));
 			DataUserNode2db(users.get(i));
 		}
 	
 		ArrayList<String> urls = new ArrayList<String>();
 		for (int i=0; i<nbUPages/3; i++)
 		{
 			urls.add( "www.page"+i+".com");
 		}
 
 		//Create upages :
 		
 		int var1;
 		int var2;
 		t = new Date();
 		for (int i = 0; i<nbUPages; i++) {
 			var1 = (int) Math.floor(Math.random()*nbUser);
 			var2 = (int) (Math.floor(Math.random()*Math.floor(nbUPages/3)));
 			System.out.println(var1+"/" +users.size()+" -- "+ var2 +"/" +urls.size());
 			System.out.println(users.get(var1)+" // "+ urls.get(var2));
 			DataUPage2db( new DataUPage(new ObjectId(t, i),users.get(var1).getId(), Math.random(),urls.get(var2)));
 		}	
 
 	}
 	
 }
 	
