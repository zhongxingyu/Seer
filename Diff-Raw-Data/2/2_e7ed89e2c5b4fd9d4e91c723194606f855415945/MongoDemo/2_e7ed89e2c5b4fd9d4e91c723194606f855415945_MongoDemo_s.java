 package no.steria.quizzical;
 
 import java.net.UnknownHostException;
 
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 import com.mongodb.MongoClient;
 
 public class MongoDemo {
 	
 	private static DBCollection quizzesInDB;
 	
 	public static void main(String[] args) {
 		Quiz[] quizzesToAdd = new Quiz[2];
 		quizzesToAdd[0] = new Quiz(1,"Geography Quiz","This is a quiz about Norwegian geography", "Thank you for taking the quiz. The winner will be announced on 2. august at 4 PM.", null);
 		quizzesToAdd[1] = new Quiz(2,"SecondQuiz","QuizDesc2","QuizMsg2",null);		
 		insertDataIntoDB(quizzesToAdd);
 	}
 	
 	public static void insertDataIntoDB(Quiz[] quiz) {
 		MongoClient client = null;
 		try {
 			client = new MongoClient();
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 		DB db = client.getDB("quizzical");
 
 		quizzesInDB = db.getCollection("quizzes");
 		quizzesInDB.drop();
 		
 		// Quiz number one		
 		if(quiz[0].getQuestions() == null){
 			BasicDBList quiz1 = new BasicDBList();
 			
 			BasicDBList alternatives11 = new BasicDBList();
 			alternatives11.add(new BasicDBObject().append("aid", 1).append("atext", "Oslo"));
 			alternatives11.add(new BasicDBObject().append("aid", 2).append("atext", "Bergen"));
 			alternatives11.add(new BasicDBObject().append("aid", 3).append("atext", "Trondheim"));
 			alternatives11.add(new BasicDBObject().append("aid", 4).append("atext", "Kristiansand"));
 			quiz1.add(createQuestionHelper(1, "What is the capital of Norway?", alternatives11, 1));
 
 			BasicDBList alternatives12 = new BasicDBList();
 			alternatives12.add(new BasicDBObject().append("aid", 1).append("atext", "Sognsvann"));
 			alternatives12.add(new BasicDBObject().append("aid", 2).append("atext", "Tyrifjorden"));
 			alternatives12.add(new BasicDBObject().append("aid", 3).append("atext", "Mjosa"));
 			alternatives12.add(new BasicDBObject().append("aid", 4).append("atext", "Burudvann"));
 			quiz1.add(createQuestionHelper(2, "What is the largest lake in Norway?", alternatives12, 3));
 
 			quizzesInDB.insert(createQuizHelper(quiz[0].getQuizId(), quiz[0].getQuizName(), quiz[0].getQuizDesc(), quiz[0].getSubmitMsg(), quiz1));			
 
 		}else{
 			quizzesInDB.insert(createQuizHelper(quiz[0].getQuizId(), quiz[0].getQuizName(), quiz[0].getQuizDesc(), quiz[0].getSubmitMsg(), quiz[0].getQuestions()));			
 		}
 
 
 		// Quiz number two
 		if(quiz[0].getQuestions() == null){
 			BasicDBList quiz2 = new BasicDBList();
 			
 			BasicDBList alternatives21 = new BasicDBList();
 			alternatives21.add(new BasicDBObject().append("aid", 1).append("atext", "Oslo"));
 			alternatives21.add(new BasicDBObject().append("aid", 2).append("atext", "Bergen"));
 			alternatives21.add(new BasicDBObject().append("aid", 3).append("atext", "Trondheim"));
 			alternatives21.add(new BasicDBObject().append("aid", 4).append("atext", "Kristiansand"));
 			quiz2.add(createQuestionHelper(1, "What is the capital of Norway?", alternatives21, 1));
 	
 			BasicDBList alternatives22 = new BasicDBList();
 			alternatives22.add(new BasicDBObject().append("aid", 1).append("atext", "Sognsvann"));
 			alternatives22.add(new BasicDBObject().append("aid", 2).append("atext", "Tyrifjorden"));
 			alternatives22.add(new BasicDBObject().append("aid", 3).append("atext", "Mjosa"));
 			alternatives22.add(new BasicDBObject().append("aid", 4).append("atext", "Burudvann"));
 			quiz2.add(createQuestionHelper(2, "What is the largest lake in Norway?", alternatives22, 3));
 	
			quizzesInDB.insert(createQuizHelper(quiz[0].getQuizId(), quiz[0].getQuizName(), quiz[0].getQuizDesc(), quiz[0].getSubmitMsg(), quiz2));
 		}else{
 			quizzesInDB.insert(createQuizHelper(quiz[1].getQuizId(), quiz[1].getQuizName(), quiz[1].getQuizDesc(), quiz[1].getSubmitMsg(), quiz[1].getQuestions()));			
 		}
 	}
 	
 	private static BasicDBObject createQuestionHelper(int idValue, String textValue, BasicDBList alternativeValues, int answerValue){
 		BasicDBObject document = new BasicDBObject();
 		document.put("id", idValue);
 		document.put("text", textValue);
 		document.put("alternatives", alternativeValues);
 		document.put("answer", answerValue);
 		return document;
 	}
 	
 	private static BasicDBObject createQuizHelper(int quizId, String quizName, String quizDescription, String quizSubmittedMsg, BasicDBList questions){
 		BasicDBObject quiz = new BasicDBObject();
 		quiz.put("quizid", quizId);
 		quiz.put("name", quizName);
 		quiz.put("desc", quizDescription);
 		quiz.put("submitMsg", quizSubmittedMsg);
 		quiz.put("questions", questions);
 		return quiz;
 	}
 	
 	public static Quiz getQuizHelper(int quizId) {
 		BasicDBObject whereQuery = new BasicDBObject();
 		whereQuery.put("quizid", quizId);
 		
 		DBObject quizObject = quizzesInDB.findOne(whereQuery);
 		
 		String quizName = (String) quizObject.get("name");
 		String quizDesc = (String) quizObject.get("desc");
 		String submitMsg = (String) quizObject.get("submitMsg");
 		BasicDBList questions = (BasicDBList) quizObject.get("questions");
 		
 		Quiz quiz = new Quiz(quizId, quizName, quizDesc, submitMsg, questions);
 		return quiz;
 	}
 	
 }
