 package edu.wm.werewolf.dao;
 
 import java.net.UnknownHostException;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.annotation.AnnotationConfigApplicationContext;
 import org.springframework.data.mongodb.core.MongoOperations;
 import org.springframework.data.mongodb.core.mapping.Document;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 
 import edu.wm.werewolf.domain.Game;
 import edu.wm.werewolf.mongoDB.SpringMongoConfig;
 import java.util.Date;
 
 //@Document(collection = "game")
 public class MongoGameDAO implements IGameDAO{
 	
 //	private MongoOperations mongoOperation;
 //	
 //	public MongoGameDAO() throws UnknownHostException {
 //	    ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringMongoConfig.class);
 //	    mongoOperation = (MongoOperations)ctx.getBean("mongoTemplate");
 //	}
 	
 	@Autowired DB db;
 	
 	@Override
 	public void createGame(Game game) {
 		DBCollection table = db.getCollection("game");
 		BasicDBObject documentDetail = new BasicDBObject();
 		documentDetail.put("createdDate", game.getCreatedDate());
 		documentDetail.put("dayNightFrequency", game.getDayNightFreq());
 		documentDetail.put("isRunning", game.getIsRunning());
 		documentDetail.put("Timer", game.getTimer());
 	 
 		table.insert(documentDetail);
 	}
 	
 	@Override
 	public boolean getIsRunning() {
 		DBCollection table = db.getCollection("game");
 		BasicDBObject object =  (BasicDBObject) table.findOne();
		System.out.println(object);
 		return (boolean) object.get("isRunning");
 	}
 
 	@Override
 	public boolean isNight() {
 		DBCollection table = db.getCollection("game");
 		BasicDBObject object =  (BasicDBObject) table.findOne();
 		Game game = new Game((int)object.get("dayNightFreq"), (Date)object.get("createdDate"));
 		return game.isNight();
 	}
 
 	@Override
 	public void endGame() {
 		DBCollection table = db.getCollection("game");
 		
 		BasicDBObject newDocument = new BasicDBObject();
 		newDocument.put("isRunning", false);
 		
 		BasicDBObject searchQuery = (BasicDBObject) table.findOne();
 		table.update(searchQuery, newDocument);
 	}
 }
