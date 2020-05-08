 package model;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import Accounts.Account;
 import Servlets.MyDB;
 
 public class Quiz {
 	
 	private String quizName;
 	private ArrayList<Question> questions;
 
 
 	private boolean random;
 	private boolean onePageMultiPage; //is this true for one page and false for multi-page?
 	private boolean immediateCorrection;
 	private boolean practiceMode;
 	private int currentQuestionInteger;
 	private Account creator;
 	private String description;
 	private ArrayList<QuizAttempts> history;
 	private String category;
 	private int quiz_id;
 	private Connection con;
 	
 
 	//creating a quiz
 	public Quiz(Connection con, ArrayList<Question> q, boolean random, boolean onePage, boolean immediateCorrect, boolean practice, Account creator, String quizName, String description, String category){
 		this.con =con; // should probably get passed in when the site is ready
 		
 		this.quizName = quizName;
 		this.questions = q;
 		this.random = random;
 		this.onePageMultiPage = onePage; //is this true for one page and false for multi-page?
 		this.immediateCorrection = immediateCorrect;
 		this.practiceMode = practice;
 		this.description = description;
 		this.category = category;
 		this.creator = creator;
 		
 	}
 	
 	public void addQuestion(Question q){
 		
 		
 		this.questions.add(q);
 		
 	}
 	// inserting a quiz into the database
 	public void finishAndStoreQuizInDatabase() throws SQLException{
 		
 		PreparedStatement stat = con.prepareStatement("insert into quiz values(null, ?, ?, ?, ?, ?, ?, CURDATE(), ?, ?)");
 		
 		stat.setString(1, quizName);
 		stat.setBoolean(2, random);
 		stat.setBoolean(3, onePageMultiPage);
 		stat.setBoolean(4, immediateCorrection);
 		stat.setBoolean(5, practiceMode);
 		//stat.setInt(6, creator.getId());
 		stat.setInt(6, creator.getId());
 		//stat.setString(7, "");
 		stat.setString(7, category);
 		stat.setString(8, description);
 		
 		System.out.println(stat.toString());
 		
 		stat.executeUpdate();
 		
 		PreparedStatement prep = con.prepareStatement("select * from quiz where name = ?");
 		prep.setString(1, quizName);
 		
 		System.out.println(prep.toString());
 		ResultSet resultSet = prep.executeQuery();
 		
 		while (resultSet.next()) {
 			this.setQuiz_id(resultSet.getInt("quiz_id")); // will always be the last one
 		}
 		
 		for(Question q : questions) {
 			q.pushToDB(con);
 			
 			PreparedStatement ps = con.prepareStatement("insert into quiz_question_mapping values(?, ?, ?)");
 			ps.setInt(1, quiz_id);
 			ps.setInt(2, q.getqID());
 			ps.setInt(3, q.getType());
 			ps.executeUpdate();
 		}
 	}
 	
 	public void randomizeQuestions() {
 		if(random) {
 			Collections.shuffle(questions);
 		}
 	}
 	
 	public Account getCreatorFromID(int id){
 		
 		try {
 			PreparedStatement query = con.prepareStatement("select * from user where user_id = ?");
 			
 			query.setInt(1, id);
 			ResultSet rs = query.executeQuery();
 			rs.next();
 			System.out.println("user id is "+rs.getInt("user_id"));
 		return new Account(rs);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public Quiz(int id, Connection con) throws SQLException {
 		this.quiz_id = id;
 		if (this.con==null) {
 			this.con = con;
 		}
 		
 		
 		PreparedStatement quizQuery = con.prepareStatement("select * from quiz where quiz_id = ?");
 		
 		quizQuery.setInt(1, id);
 		ResultSet rs = quizQuery.executeQuery();
 		
 		int creatorID=0; 
 		
 		while(rs.next()) {
 			setQuizName(rs.getString("name"));
 			setRandom(rs.getBoolean("random"));
 			setOnePageMultiPage(rs.getBoolean("one_page"));
 			setImmediateCorrection(rs.getBoolean("immediate_correction"));
 			setPracticeMode(rs.getBoolean("practice_mode"));
 			creatorID = rs.getInt("creator_id");
 			category = rs.getString("category");
 			description = rs.getString("description");
 		}
 		this.creator = getCreatorFromID(creatorID);
 		
 		
 		// get history of quiz
 		PreparedStatement historyQuery = con.prepareStatement("select * from history where quiz_id = ?");
 		historyQuery.setInt(1, id);
 		ResultSet set = historyQuery.executeQuery();
 		history = new ArrayList<QuizAttempts>();
 		while(set.next()) {
 			QuizAttempts qa = new QuizAttempts(set.getInt("user_id"), id, set.getInt("score"), set.getDate("date"), set.getInt("time_took"));
 			history.add(qa);
 		}
 		
 		// get questions from table
 		PreparedStatement questionQuery = con.prepareStatement("select * from quiz_question_mapping where quiz_id = ?");
 		questionQuery.setInt(1, id);
 		ResultSet resultSet = questionQuery.executeQuery();
 		questions = new ArrayList<Question>();
 		while(resultSet.next()) {
 			int questionType = resultSet.getInt("question_type");
 			int questionID = resultSet.getInt("question_id");
 			switch(questionType) {
 				case 1: 
 					QuestionResponse qr = new QuestionResponse(questionID, con);
 					questions.add(qr);
 					break;
 				
 				case 2:
 					FillInTheBlank fb = new FillInTheBlank(questionID, con);
 					questions.add(fb);
 					break;
 					
 				case 3:
 					MultipleChoice mc = new MultipleChoice(questionID, con);
 					questions.add(mc);
 					break;
 					
 				case 4:
 					PictureResponse pr = new PictureResponse(questionID, con);
 					questions.add(pr);
 					break;
 					
 				case 5:
 					MultipleAnswer ma = new MultipleAnswer(questionID, con);
 					questions.add(ma);
 					break;
 					
 				case 6:
 					MultipleChoiceMultipleAnswer mcma = new MultipleChoiceMultipleAnswer(questionID, con );
 					questions.add(mcma);
 					break;
 					
 				case 7:
 					Matching m = new Matching(questionID, con);
 					questions.add(m);
 					break;
 			}
 		}
 		
 		currentQuestionInteger = -1;
 		
 	}
 
 	// for scoring the entire quiz in one shot
 	public double score(ArrayList<ArrayList<String>> answer){
 		double score = 0;
 		
 		for(int i = 0; i < questions.size(); i++) {
 			//TODO fix this to get user answer for each question.
 			score += scoreQuestion(i,answer.get(i));
 		}
 		
 		return score;
 	}
 	
 	// good idea to do this in case we want to score a quiz question by question
 	public double scoreQuestion(int questionNumberInThisQuiz, ArrayList<String> answer){
 		double score = 0;
 		
 		score += questions.get(questionNumberInThisQuiz).solve(answer);
 		return score;
 	}
 	
 	
 	
 	
 	public void generate(ArrayList<Question> q, boolean random, boolean onePage, 
 			boolean immediateCorrect, boolean practice, int userID, String description, 
 			ArrayList<QuizAttempts> history, String category){
 		for(Question quest : q) {
 			questions.add(quest);
 		}
 		
 		this.setRandom(random);
 		
 		setOnePageMultiPage(onePage);
 		
 		setImmediateCorrection(immediateCorrect);
 		
 		setPracticeMode(practice);
 		
 		this.creator =getCreatorFromID(userID);
 		
 		this.description = description;
 		
 		for(QuizAttempts qa : history) {
 			this.history.add(qa);
 		}
 		
 		this.category = category;
 		
 		currentQuestionInteger = -1;
 	}
 	
 	public Question getNextQuestion(){
 		currentQuestionInteger++;
 		return questions.get(currentQuestionInteger);
 	}
 	
 	public String getCategory() {
 		return category;
 	}
 	
 	public String getDescription() {
 		return description;
 	}
 
 	public String getQuizName() {
 		return quizName;
 	}
 
 	public void setQuizName(String quizName) {
 		this.quizName = quizName;
 	}
 
 	public boolean isRandom() {
 		return random;
 	}
 
 	public void setRandom(boolean random) {
 		this.random = random;
 	}
 
 	public boolean isOnePageMultiPage() {
 		return onePageMultiPage;
 	}
 
 	public void setOnePageMultiPage(boolean onePageMultiPage) {
 		this.onePageMultiPage = onePageMultiPage;
 	}
 
 	public boolean isImmediateCorrection() {
 		return immediateCorrection;
 	}
 
 	public void setImmediateCorrection(boolean immediateCorrection) {
 		this.immediateCorrection = immediateCorrection;
 	}
 
 	public boolean isPracticeMode() {
 		return practiceMode;
 	}
 
 	public void setPracticeMode(boolean practiceMode) {
 		this.practiceMode = practiceMode;
 	}
 
 	public Account getCreator() {
 		return creator;
 	}
 
 	public void setCreator(Account creator) {
 		this.creator = creator;
 	}
 	
 
 	public int getQuiz_id() {
 		return quiz_id;
 	}
 
 	public void setQuiz_id(int quiz_id) {
 		this.quiz_id = quiz_id;
 	}
 
 	public ArrayList<Question> getQuestions() {
 		return questions;
 	}
 	public void setQuestions(ArrayList<Question> questions) {
 		this.questions = questions;
 	}
 }
