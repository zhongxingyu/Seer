 package models;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 public class QuestionFactory {
 	public static QuestionFactory sharedInstance;
 	public static QuestionFactory sharedInstance() {
 		if (sharedInstance == null) {
 			sharedInstance = new QuestionFactory();
 		}
 		
 		return sharedInstance;
 	}
 	
 	public Question retrieveQuestion(int questionID) throws Exception {
 		DBConnection connection = DBConnection.sharedInstance();
 		ResultSet rs = connection.performQuery("SELECT * FROM questions WHERE id=" + questionID);
 		
 		try {
 			if (rs.next()) {
 				int questionType = rs.getInt("question_type");
 				int specificID = rs.getInt("specific_questionID");
 				
 				return retrieveQuestion(questionID, Question.QuestionType(questionType), specificID);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public Question retrieveQuestion(int questionID, Question.QuestionType questionType, int specificID) {
 		switch (questionType) {
 		case QUESTION_RESPONSE:
 			return retrieveQuestionResponseQuestion(questionID, specificID);
 		case FILL_IN:
 			return retrieveFillInQuestion(questionID, specificID);
 		case MULTIPLE_CHOICE:
 			return retrieveMultipleChoiceQuestion(questionID, specificID);
 		case PICTURE_RESPONSE:
 			return retrievePictureResponseQuestion(questionID, specificID);
 		default:
 			return null;
 		}
 	}
 	
 	private ArrayList<String> retrieveAnswers(int questionID) {
 		DBConnection connection = DBConnection.sharedInstance();
 		ResultSet rs = connection.performQuery("SELECT * FROM answers WHERE questionID=" + questionID);
 		ArrayList<String> answers = new ArrayList<String>();
 		
 		try {
 			while(rs.next()) {
 				answers.add(rs.getString("answer"));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return answers;
 	}
 	
 	private PictureResponseQuestion retrievePictureResponseQuestion(int questionID,
 			int specificID) {
 		DBConnection connection = DBConnection.sharedInstance();
 		
 		ResultSet rs = connection.performQuery("SELECT * FROM picture_response_questions WHERE id=" + specificID);
 		String question_text = "";
 		
 		try {
 			rs.next();
 			question_text = rs.getString("question_text");	
  		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		ArrayList<String> answers = retrieveAnswers(questionID);
 		
 		PictureResponseQuestion question = new PictureResponseQuestion(questionID, question_text, answers);
 		return question;
 	}
 	private MultipleChoiceQuestion retrieveMultipleChoiceQuestion(int questionID,
 			int specificID) {
 		DBConnection connection = DBConnection.sharedInstance();
 		
 		ResultSet rs = connection.performQuery("SELECT * FROM multiple_choice_questions WHERE id=" + specificID);
 		String question_text = "";
 		
 		try {
 			rs.next();
 			question_text = rs.getString("question_text");	
  		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		rs = connection.performQuery("SELECT * FROM multiple_choice_choices WHERE specific_questionID=" + specificID);
 		ArrayList<String> choices = new ArrayList<String>();
 		
 		try {
 			while(rs.next()) {
 				String choice = rs.getString("choice");
 				choices.add(choice);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		ArrayList<String> answers = retrieveAnswers(questionID);
 		
 		MultipleChoiceQuestion question = new MultipleChoiceQuestion(questionID, question_text, choices, answers);
 		return question;
 	}
 	private FillInQuestion retrieveFillInQuestion(int questionID, int specificID) {
 		DBConnection connection = DBConnection.sharedInstance();
 		
 		ResultSet rs = connection.performQuery("SELECT * FROM fill_in_questions WHERE id=" + specificID);
 		String question_text = "";
 		
 		try {
 			rs.next();
 			question_text = rs.getString("question_text");	
  		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		ArrayList<String> answers = retrieveAnswers(questionID);
 		
 		FillInQuestion question = new FillInQuestion(questionID, question_text, answers);
 		return question;
 	}
 	private QuestionResponseQuestion retrieveQuestionResponseQuestion(int questionID, int specificID) {
 		DBConnection connection = DBConnection.sharedInstance();
 		
 		ResultSet rs = connection.performQuery("SELECT * FROM question_response_questions WHERE id=" + specificID);
 		String question_text = "";
 		
 		try {
 			rs.next();
 			question_text = rs.getString("question_text");	
  		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		ArrayList<String> answers = retrieveAnswers(questionID);
 		
 		QuestionResponseQuestion question = new QuestionResponseQuestion(questionID, question_text, answers);
 		return question;	
 	}
 	
 	public void insertQuestion(Question q) {
 		long specificID;
 		switch (q.type) {
 		case QUESTION_RESPONSE:
 			specificID = insertQuestionResponseQuestion(q);
 			break;
 		case FILL_IN:
 			specificID = insertFillInQuestion(q);
 			break;
 		case MULTIPLE_CHOICE:
 			specificID = insertMultipleChoiceQuestion((MultipleChoiceQuestion)q);
 			break;
 		case PICTURE_RESPONSE:
 			specificID = insertPictureResponseQuestion(q);
 			break;
 		default:
 			specificID = -1;
 		}
 		if (specificID != -1) {
 			DBConnection connection = DBConnection.sharedInstance();
 			
 			int id;
 			try {
 				id = connection.insert("INSERT INTO questions (quizID, question_type, specific_questionID, order_index) VALUES " +
 											"('" + q.getQuizID() + "', '" + q.getTypeInt() + "', '" + specificID + "', '" + q.getOrderIndex() + "')");
 				q.setId(id);
 				for(String answer : q.getCorrectAnswers()) {
 					connection.insert("INSERT INTO answers (questionID, answer) VALUES ('" + id + "', '" + answer + "')");
 				}
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	private long insertPictureResponseQuestion(Question q) {
 		DBConnection connection = DBConnection.sharedInstance();
 		
 		int specific_id = connection.insert("INSERT INTO picture_response_questions (question_text) VALUES ('" + q.getQuestion() + "')");
 		return specific_id;
 		
 	}
 	private long insertMultipleChoiceQuestion(MultipleChoiceQuestion q) {
 		DBConnection connection = DBConnection.sharedInstance();
 		
 		int specific_id = connection.insert("INSERT INTO multiple_choice_questions (question_text) VALUES ('" + q.getQuestion() + "')");
 		
 		ArrayList<String> choices = q.getChoices();
 		for(int i = 0; i < choices.size(); i++) {
 			connection.insert("INSERT INTO multiple_choice_choices (specific_questionID, choice) VALUES (" + specific_id + ", '" + choices.get(i) + "')");
 		}
 		
 		return specific_id;
 	}
 	private long insertFillInQuestion(Question q) {
 		DBConnection connection = DBConnection.sharedInstance();
 		
 		int specific_id = connection.insert("INSERT INTO fill_in_questions (question_text) VALUES ('" + q.getQuestion() + "')");
 		return specific_id;		
 	}
 	private long insertQuestionResponseQuestion(Question q) {
 		DBConnection connection =DBConnection.sharedInstance();
 		
 		int specific_id = connection.insert("INSERT INTO question_response_questions (question_text) VALUES ('" + q.getQuestion() + "')");
 		return specific_id;
 	}
 
 }
