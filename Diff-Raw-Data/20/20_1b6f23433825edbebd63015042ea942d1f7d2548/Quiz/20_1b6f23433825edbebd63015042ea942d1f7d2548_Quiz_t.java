 package site;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 
 import com.mysql.jdbc.Connection;
 
 /**	quiz_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 	user_id INT,
 	practice_mode BOOLEAN,
 	max_score INT,
 	description TEXT,
 	title TEXT,
 	random_question BOOLEAN,
 	one_page BOOLEAN,
 	immediate_correction BOOLEAN,
     created_timestamp TIMESTAMP*/
 
 public class Quiz {
 	
 	public void populateQuiz() {
 		try{
 			Connection con = (Connection) MyDB.getConnection();
 			Connection con2 = (Connection) MyDB.getConnection();
 			Statement stmt = con.createStatement();
 			Statement stmt2 = con2.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM questions WHERE quiz_id="+quiz_id);
 			ArrayList<Question> questions = new ArrayList<Question>();
 			while(rs.next()) {
 				int type = rs.getInt("question_type");
 				int question_id = rs.getInt("question_id");
 				if(type == 1) {
 					String getFromQRDB = "SELECT * FROM question_responses WHERE question_id = "
 							+ question_id;
 					
 					ResultSet rss = null;
 					try{
 						rss = stmt2.executeQuery(getFromQRDB);
					} catch(Exception e) { System.out.println(e); }
 					Answer answer = new Answer();
 					Question question = null;
 					while(rss.next()) {
 						question = new QuestionResponseQuestion(rss.getInt("question_id"), 
 							quiz_id, 1, type, answer, rss.getInt("question_responses_id"), rss.getString("string"));
 					}
 					String getFromAnswer = "SELECT * FROM answers WHERE question_id = " + question_id;
 					while(rss.next()) {
 						rss = stmt2.executeQuery(getFromAnswer);
 						answer.addAnswer(rss.getString("string"));
 					}
 					questions.add(question);
 				}
 				else if(type == 2) {
 					String getFromFITBDB = "SELECT * FROM fill_in_the_blanks WHERE question_id = "
 							+ question_id;
 					ResultSet rss = stmt2.executeQuery(getFromFITBDB);
 					Answer answer = new Answer();
 					Question question = null;
 					while(rss.next()) {
 						question = new FillInTheBlankQuestion(rss.getInt("question_id"),
 							quiz_id, 1, type, answer, rss.getInt("fill_in_the_blanks_id"), 
 							rss.getString("string_1"), rss.getString("string_2"));
 					}
 					String getFromAnswer = "SELECT * FROM answers WHERE question_id = " + question_id;
 					rss = stmt2.executeQuery(getFromAnswer);
 					while(rss.next()) {
 						answer.addAnswer(rss.getString("string"));
 					}
 					questions.add(question);	
 				}
 				else if(type == 3) {
 					String getFromMCDB = "SELECT * FROM multiple_choices WHERE question_id = "
 							+ question_id;
 					ResultSet rss = stmt2.executeQuery(getFromMCDB);
 					Answer answer = new Answer();
 					ArrayList<MultipleChoiceChoices> choices = new ArrayList<MultipleChoiceChoices>();
 					Question question = null;
 					int mc_id = 0;
 					while(rss.next()) {
 						question = new MultipleChoiceQuestion(rss.getInt("question_id"),
 							quiz_id, 1, type, answer, rss.getInt("multiple_choice_id"), 
 							rss.getString("string"), choices);
 						mc_id = rss.getInt("multiple_choice_id");
 					}
 					String getFromMCCDB = "SELECT * FROM multiple_choices_choices WHERE multiple_choices_id = "
 							+ mc_id;
 					
 					rss = stmt2.executeQuery(getFromMCCDB);
 					while(rss.next()) {
 						choices.add(new MultipleChoiceChoices(rss.getString("string")));
 					}
 					String getFromAnswer = "SELECT * FROM answers WHERE question_id = " + question_id;
 					rss = stmt2.executeQuery(getFromAnswer);
 					while(rss.next()) {
 						answer.addAnswer(rss.getString("string"));
 					}
 					questions.add(question);
 				}
 				else {
 					String getFromPRDB = "SELECT * FROM picture_responses WHERE question_id = "
 							+ question_id;
 					ResultSet rss = stmt2.executeQuery(getFromPRDB);
 					Answer answer = new Answer();
 					Question question = null;
 					while(rss.next()) {
 						question = new PictureResponseQuestion(rss.getInt("question_id"),
 							quiz_id, 1, type, answer, rss.getInt("picture_responses_id"), 
 							rss.getString("string"));
 					}
 					String getFromAnswer = "SELECT * FROM answers WHERE question_id = " + question_id;
 					rss = stmt2.executeQuery(getFromAnswer);
 					while(rss.next()) {
 						answer.addAnswer(rss.getString("string"));
 					}
 					questions.add(question);
 				}	
 			}
 			this.setQuestions(questions);
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public int getUser_id() {
 		return user_id;
 	}
 
 	public void setUser_id(int user_id) {
 		this.user_id = user_id;
 	}
 
 	public boolean isPractice_mode() {
 		return practice_mode;
 	}
 
 	public void setPractice_mode(boolean practice_mode) {
 		this.practice_mode = practice_mode;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public boolean isRandom_question() {
 		return random_question;
 	}
 
 	public void setRandom_question(boolean random_question) {
 		this.random_question = random_question;
 	}
 
 	public boolean isOne_page() {
 		return one_page;
 	}
 
 	public void setOne_page(boolean one_page) {
 		this.one_page = one_page;
 	}
 
 	public boolean isImmediate_correction() {
 		return immediate_correction;
 	}
 
 	public void setImmediate_correction(boolean immediate_correction) {
 		this.immediate_correction = immediate_correction;
 	}
 
 	public Timestamp getCreated_timestamp() {
 		return created_timestamp;
 	}
 
 	public void setCreated_timestamp(Timestamp created_timestamp) {
 		this.created_timestamp = created_timestamp;
 	}
 
 	private ArrayList<Question> questions;
 	private int quiz_id;
 	private int user_id;
 	private int max_score;
 	private boolean practice_mode;
 	private String description;
 	private String title;
 	private boolean random_question;
 	private boolean one_page;
 	private boolean immediate_correction;
 	private Timestamp created_timestamp;
 	
 	public Quiz(ArrayList<Question> questions) {
 		this.setQuestions(questions);
 	}
 	
 	public Quiz() {
 		
 	}
 	
 	public Quiz(int quiz_id, int user_id, int max_score,
 			boolean practice_mode, String description, String title, 
 			boolean random_question, boolean one_page, boolean immediate_correction, 
 			Timestamp created_timestamp) {
 		this.setQuiz_id(quiz_id);
 		this.setUser_id(user_id);
 		this.setMax_score(max_score);
 		this.setPractice_mode(practice_mode);
 		this.setDescription(description);
 		this.setTitle(title);
 		this.setRandom_question(random_question);
 		this.setOne_page(one_page);
 		this.setImmediate_correction(immediate_correction);
 		this.setCreated_timestamp(created_timestamp);
 	}
 	
 	public Quiz(int user_id,
 			boolean practice_mode, String description, String title, 
 			boolean random_question, boolean one_page, boolean immediate_correction) {
 		this.setUser_id(user_id);
 		this.setPractice_mode(practice_mode);
 		this.setDescription(description);
 		this.setTitle(title);
 		this.setRandom_question(random_question);
 		this.setOne_page(one_page);
 		this.setImmediate_correction(immediate_correction);
 		this.questions = new ArrayList<Question>();
 	}
 	
 	public void addQuestion(Question question) {
 		this.questions.add(question);
 	}
 	
 	public ArrayList<Question> getQuestions() {
 		return questions;
 	}
 	
 	public void setQuestions(ArrayList<Question> questions) {
 		this.questions = questions;
 	}
 
 	public int getQuiz_id() {
 		return quiz_id;
 	}
 
 	public void setQuiz_id(int quiz_id) {
 		this.quiz_id = quiz_id;
 	}
 
 	public int getMax_score() {
 		return max_score;
 	}
 
 	public void setMax_score(int max_score) {
 		this.max_score = max_score;
 	}
 
 	public static Quiz getQuiz(int quiz) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
