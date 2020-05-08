 package models;
 
 import java.sql.Date;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 public class QuizFactory {
 	private static QuizFactory sharedInstance;
 	public static QuizFactory sharedInstance() {
 		if (sharedInstance == null) {
 			sharedInstance = new QuizFactory();
 		}
 		return sharedInstance;
 	}
 	
 	public Quiz retrieveQuiz(int quizID) {
 		String name, description;
 		int creatorID;
 		boolean ordered, multipage;
 		Date dateCreated;
 		ArrayList<Question> questions = new ArrayList<Question>();
 		
 		DBConnection connection = new DBConnection();
 		ResultSet result = connection.performQuery("SELECT * FROM quizzes WHERE id=" + quizID);
 		
 		try {
 			result.next();
 			name = result.getString("name");
 			description = result.getString("description");
 			creatorID = result.getInt("creator");
 			ordered = result.getBoolean("ordered");
 			multipage = result.getBoolean("multi_page");
 			dateCreated = result.getDate("date_created");
 			questions = retrieveQuestions(quizID);
 			
 			Quiz quiz = new Quiz(quizID, name, description, creatorID, ordered, questions, multipage, dateCreated);
 			return quiz;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	private ArrayList<Question> retrieveQuestions(int quizID) {
 		ArrayList<Question> questions = new ArrayList<Question>();
 		
 		DBConnection connection = new DBConnection();
 		ResultSet rs = connection.performQuery("SELECT * FROM questions WHERE quizID=" + quizID);
 		QuestionFactory qf = QuestionFactory.sharedInstance();
 		
 		try {
 			while(rs.next()) {
 				int questionType = rs.getInt("quiz_type");
 				int specificID = rs.getInt("specific_questionID");
 				int order_index = rs.getInt("order_index");
 				int questionID = rs.getInt("id");
 				
 				Question question = qf.retrieveQuestion(questionID, Question.QuestionType(questionType), specificID);
 				question.setOrderIndex(order_index);
 				questions.add(question);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return questions;
 	}
 	
 	public void insertQuiz(Quiz quiz) {
 		DBConnection connection = new DBConnection();
 		
 		connection.insert("INSERT INTO quizzes (name, description, creator, ordered, multi_page) " +
 										"VALUES ('" + quiz.getName() + "', '" + quiz.getDescription() + "', '"
										 + quiz.getCreatorID() + "', '" + quiz.isOrdered() + "', '" + quiz.isMultipage() + "')");
 		for(Question q : quiz.getQuestions()) {
 			QuestionFactory factory = QuestionFactory.sharedInstance();
 			factory.insertQuestion(q);
 		}
 	}
 }
