 package quiz;
 
 import java.sql.*;
 import java.util.*;
 
 import database.DBConnection;
 
 public class QuestionBank {
 
 	private DBConnection connection;
 
 	/*
 	 * Initializes the database Connection for later use.
 	 */
 	public QuestionBank() {
 		connection = DBConnection.getDBConnection();
 	}
 	
 	
 	/*
 	 * Checks to see if a question exists with the given question_id
 	 */
 	public boolean QuestionExists(int question_id) {
 		ResultSet rs = connection.ExecuteQuery("SELECT COUNT(*) FROM question WHERE id=" + question_id);
 		int size = 0;
 		try {
 			rs.next();
 			size = rs.getInt(1);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return (size == 1);
 	}
 	
 	public Question getQuestion(int question_id) {
 		//Get question type
 		Question question = null;
 		if (!QuestionExists(question_id)) return null;
 		int quizID = 0;
 		int question_num = 0;
 		int type = -1;
 		ResultSet rs = connection.ExecuteQuery("SELECT * FROM question WHERE id=" + question_id);
 		try {
 			rs.next();
 			quizID = rs.getInt("quizid");
 			type = rs.getInt("type");
 			question_num = rs.getInt("number");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		//Retrieve question based on type
 		if (type == 0) {
 			question = GetQuestionResponse(question_id, quizID, question_num);
 		} else if (type == 1) {
 			question = GetFillInTheBlank(question_id, quizID, question_num);
 		} else if (type == 2) {
 			question = GetPictureResponse(question_id, quizID, question_num);
 		} else if (type == 3) {
 			question = GetMultipleChoice(question_id, quizID, question_num);
 		}
 		return question;
 	}
 	
 	private Question GetQuestionResponse(int question_id, int quizID, int question_num) {
 		//Get Question
 		String questionText = "";
 		ResultSet rs = connection.ExecuteQuery("SELECT * FROM question_response WHERE id=" + question_id);
 		try {
 			rs.next();
 			questionText = rs.getString("question");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		//Get answers
 		Set<String> answers = getAnswers(question_id);
 
 		//Return new question of type question response
 		Question question = new QuestionResponse(question_id, quizID, question_num, questionText, answers);
 		return question;
 	}
 	
 	private Question GetFillInTheBlank(int question_id, int quizID, int question_num) {
 		//Get Question
 		String question1 = "";
 		String question2 = "";
 		ResultSet rs = connection.ExecuteQuery("SELECT * FROM fill_in_the_blank WHERE id=" + question_id);
 		try {
 			rs.next();
 			question1 = rs.getString("question1");
 			question2 = rs.getString("question2");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		//Get answers
 		Set<String> answers = getAnswers(question_id);
 		
 		//Return new question of type fill in the blank
 		Question question = new FillInTheBlank(question_id, quizID, question_num, question1, question2, answers);
 		return question;
 	}
 	
 	
 	private Question GetPictureResponse(int question_id, int quizID, int question_num) {
 		//Get Question
 		String questionText = "";
 		String imageText = "";
 		ResultSet rs = connection.ExecuteQuery("SELECT * FROM picture_response WHERE id=" + question_id);
 		try {
 			rs.next();
 			questionText = rs.getString("question");
 			imageText = rs.getString("image");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		//Get answers
 		Set<String> answers = getAnswers(question_id);
 
 		//Return new question of type picture response
 		Question question = new PictureResponse(question_id, quizID, question_num, imageText, questionText, answers);
 		return question;
 	}
 	
 		private Question GetMultipleChoice(int question_id, int quizID, int question_num) {
 		//Get Question
 		String prompt = "";
		int answer_id;
 		ResultSet rs = connection.ExecuteQuery("SELECT * FROM multiple_choice WHERE id=" + question_id);
 		try {
 			rs.next();
 			prompt = rs.getString("question");
 			answer_id = rs.getInt("answer");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		//Get Answer from choice table using answer_id
 		//Create new set of answers, put answer in it, and pass into question constructor
		String answer;
 		rs = connection.ExecuteQuery("SELECT * FROM choice WHERE id=" + answer_id);
 		try {
 			rs.next();
 			answer = rs.getString("answer");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		Set<String> answers = new HashSet<String>();
 		answers.add(answer);
 		
 		
 		//Get choices
 		List<String> choices = new ArrayList<String>();
		rs = connection.ExecuteQuery("SELECT * FROM choice WHERE id=" + question_id);
 		try {
 			while (rs.next()) {
 				String choice = rs.getString("answer");
 				choices.add(choice);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		//Return new question of type multiple choice
 		
 		Question question = new MultipleChoice(question_id, quizID, question_num, prompt, choices, answers);
 		return question;
 	}
 	
 	
 	private Set<String> getAnswers(int question_id) {
 		Set<String> answers = new HashSet<String>();
 		ResultSet rs = connection.ExecuteQuery("SELECT * FROM answer WHERE id=" + question_id);
 		try {
 			while (rs.next()) {
 				String answer = rs.getString("answer");
 				answers.add(answer);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return answers;
 	}
 }
