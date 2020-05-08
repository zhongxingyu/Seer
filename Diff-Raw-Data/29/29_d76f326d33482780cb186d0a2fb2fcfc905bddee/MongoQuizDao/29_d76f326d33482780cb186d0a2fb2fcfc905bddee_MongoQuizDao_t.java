 package no.steria.quizzical;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.MongoClient;
 
 public class MongoQuizDao implements QuizDao {
 	
 	private DB db;
 	private DBCollection collection;
 	
 	public MongoQuizDao() {
 		try {
 			MongoClient client = new MongoClient();
 			db = client.getDB("quizzical");
 		} catch (UnknownHostException e) {
 			throw new RuntimeException(e);
 		}
 		collection = db.getCollection("quizzes");
 	}
 	
 	@Override
 	public ArrayList<Quiz> getQuizzes() {
 		DBCursor cursor = collection.find();
 		ArrayList<Quiz> quizzes = new ArrayList<Quiz>();
 		while(cursor.hasNext()){
 			DBObject next = cursor.next();
 			Integer quizId = (Integer) next.get("quizid");
 			String quizName = (String) next.get("name");
 			String quizDesc = (String) next.get("desc");
 			String submitMsg = (String) next.get("submitMsg");
 			
 			BasicDBList questions = (BasicDBList) next.get("questions");
 			
 			Quiz quiz = new Quiz(quizId, quizName, quizDesc, submitMsg, questions);
 			quizzes.add(quiz);
 		}
 		
 		return quizzes;
 	}
 
 	@Override
 	public Quiz getQuiz(int quizId) {
 		BasicDBObject whereQuery = new BasicDBObject();
 		whereQuery.put("quizid", quizId);
 		
 		DBObject quizObject = collection.findOne(whereQuery);
 		
		if (quizObject == null){
			throw new IllegalArgumentException("The requested quiz (quizId=" + quizId + ") is not available.");
		}
		
 		String quizName = (String) quizObject.get("name");
 		String quizDesc = (String) quizObject.get("desc");
 		String submitMsg = (String) quizObject.get("submitMsg");
 		BasicDBList questions = (BasicDBList) quizObject.get("questions");
 		
 		Quiz quiz = new Quiz(quizId, quizName, quizDesc, submitMsg, questions);
 		return quiz;
 	}
 	
 }
