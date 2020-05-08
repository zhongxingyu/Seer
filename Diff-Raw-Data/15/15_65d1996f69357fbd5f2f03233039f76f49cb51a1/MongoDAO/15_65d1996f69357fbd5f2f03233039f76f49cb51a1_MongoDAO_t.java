 package com.cloudbees.service;
 
 import java.util.List;
 
 import org.bson.types.ObjectId;
 
 import com.cloudbees.model.Game;
 import com.cloudbees.model.Move;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 import com.mongodb.MongoURI;
 import com.mongodb.WriteConcern;
 import com.mongodb.WriteResult;
 
 
 public class MongoDAO {
 	
	protected String envMongoURI = "";
 	protected String strURI = "mongodb://cloudbees:6dce0b9d30f52ac73bfa74c492aa3382@alex.mongohq.com:10064/ELSfmlamgpGNTqD6jFEw";
 	//protected String strURI = "mongodb://guest:welcome1@localhost:27017/mydb";
 
 	protected DB mongoDB = null;
 	protected Mongo mongo = null;
 	protected DBCollection games = null;
 	protected DBCollection moves = null;
 	protected String collGames = "games";
 	protected String collMoves = "moves";
 	
 	public DB getMongoDB() {
 		return mongoDB;
 	}
 
 	public Mongo getMongo() {
 		return mongo;
 	}
 	
 	public DBCollection getGamesCollection() {
 		return games;
 	}
 
 	public DBCollection getMovesCollection() {
 		return moves;
 	}
 
 	public void connect() throws Exception {
 		try {
			// Get MongoURI from system environment if defined
			envMongoURI = System.getProperty( "mongochess.mongoURI" );
			if ( ! (envMongoURI == null) ) {
				System.out.println( "Using MongoURI from system environment: " + envMongoURI);
				strURI = envMongoURI;
			}
			else {
				System.out.println( "MongoURI system environment not set - " + "Using default: " + strURI);
 			}
 
 			// Connect to Mongo and Authenticate
 		    MongoURI mongoURI = new MongoURI( strURI );
 		    mongo = new Mongo( mongoURI );
 		    mongoDB = mongo.getDB( mongoURI.getDatabase() );
 		    mongoDB.authenticate(mongoURI.getUsername(), mongoURI.getPassword());
 		    
 		    // Get Mongo collections and set WriteConcern
 			games = getMongoDB().getCollection(collGames);
 			moves = getMongoDB().getCollection(collMoves);
 			mongoDB.setWriteConcern(WriteConcern.SAFE);
 		} 
 		catch ( Exception e) {
 			e.printStackTrace();
 			throw e;
 		}
 	}
 	
 	public String getGame( String idString ) {
 		DBObject theGame = null;
 		String strReturn = null;
 		
 		try {
 			DBCollection games = getGamesCollection(); 
 		
 			// Search by object id
 			BasicDBObject searchObject = new BasicDBObject();
 			searchObject.put("_id", new ObjectId(idString));
 		
 			// Do not include _id field
 			BasicDBObject skipFields = new BasicDBObject();
 			skipFields.put("_id",0);
 		
 			// Retrieve game object and return
 			theGame = games.findOne(searchObject,skipFields);
 			strReturn = theGame.toString();
 		}
 		catch ( NullPointerException e ) {
 			e.printStackTrace();
 			
 			// Not Found: Servlet should return 400 Bad Request
 			strReturn = null;			
 		}
 		catch ( IllegalArgumentException e ) {
 			e.printStackTrace();
 			
 			// Invalid id: Servlet should return 400 Bad Request
 			strReturn = null;
 		}
 
 		return strReturn;
 	}
 	
 	public String newGame( Game game ) {
 		
 		DBCollection games = getGamesCollection(); 
 		
 		DBObject obj = new BasicDBObject();
 		obj.put("white", game.getWhite());
 		obj.put("black", game.getBlack());
 		obj.put("description", game.getDescription());
 		obj.put("result", game.getResult());
 		obj.put("next", game.getNext());
 		obj.put("move", game.getMove());		
 		games.insert(obj);
 		
 		return obj.get("_id").toString();	
 	}
 	
 	public WriteResult updateBlackToMove( String idString ) {
 
 		DBCollection games = getGamesCollection(); 
 		
 		// Update game { $set:{next:"B"} }
 		BasicDBObject query = new BasicDBObject();
 		query.put("_id", new ObjectId(idString));
 		BasicDBObject set = new BasicDBObject("$set", new BasicDBObject("next", "B")); 
 		return games.update( query, set );
 	}
 	
 	public WriteResult updateWhiteToMove( String idString ) {
 
 		DBCollection games = getGamesCollection(); 
 		
 		// Update game { $set:{next:"W"}, $inc:{move : 1} }
 		BasicDBObject query = new BasicDBObject();
 		query.put("_id", new ObjectId(idString));
 		BasicDBObject changes = new BasicDBObject();
 		changes.put( "$set", new BasicDBObject("next", "W"));
 		changes.put( "$inc", new BasicDBObject("move", 1));
 		return games.update( query, changes );
 	}
 	
 	public String getNext( String idString ) {
 		
 		DBCollection games = getGamesCollection(); 
 		
 		// Search by object id
 		BasicDBObject searchObject = new BasicDBObject();
 		searchObject.put("_id", new ObjectId(idString));
 		DBObject theGame = games.findOne(searchObject);	
 		
 		return theGame.get("next").toString();	
 	}
 	
 	public long getMoveNo( String idString ) {
 		
 		DBCollection games = getGamesCollection(); 
 		
 		// Search by object id
 		BasicDBObject searchObject = new BasicDBObject();
 		searchObject.put("_id", new ObjectId(idString));
 		DBObject theGame = games.findOne(searchObject);	
 		
 		return (Long.parseLong( theGame.get("move").toString() ) );	
 	}
 	
 	public String getMoves( String idString ) {
 		
 		DBCollection moves = getMovesCollection(); 
 		 
 		// Omit object id, game and move from result
 		BasicDBObject omits = new BasicDBObject();
 		omits.put("_id",0);
 		omits.put("game",0);
 		omits.put("move", 0);
 		
 		// Search by game id
 		BasicDBObject findMoves = new BasicDBObject();
 		findMoves.put("game", new ObjectId(idString));			
 		List<DBObject> listMoves = moves.find(findMoves, omits).limit(200).toArray();
 
 		return( listMoves.toString() );		
 	}
 
 	public String newMove( Move move ) {
 
 		DBCollection moves = getMovesCollection();
 		DBObject moveObj = new BasicDBObject();
 		
 		if ( ! move.getWhite().isEmpty() ){
 			moveObj.put( "white", move.getWhite() );
 		}
 		else {
 			moveObj.put( "black", move.getBlack() );
 		}
 		moveObj.put( "game", new ObjectId(move.getGame()) );
 		moveObj.put( "move", move.getMove() );
 		moves.save(moveObj);
 
 		return(moveObj.toString());
 	}
 }
