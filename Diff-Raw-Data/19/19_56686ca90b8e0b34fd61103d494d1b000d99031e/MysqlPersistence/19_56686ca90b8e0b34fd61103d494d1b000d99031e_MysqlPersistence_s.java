 package persistence;
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Scanner;
 
 import controller.ChangeQuizController;
 import controller.DeleteQuizController;
 import model.EnumerationExercise;
 import model.Exercise;
 import model.ExerciseCatalog;
 import model.MultipleChoiceExercise;
 import model.Quiz;
 import model.QuizCatalog;
 import model.QuizExercise;
 import model.QuizStatus;
 import model.SimpleExercise;
 import model.Teacher;
 import model.Exercise.ExerciseCategory;
 import utils.DateGC;
 import utils.DateQuiz;
 import view.ChangeQuizView;
 import view.CreateQuizView;
 import view.DeleteQuizView;
 
 /**
  * 
  * @author Emin
  *
  */
 public class MysqlPersistence implements IPersistencable {
 
 	/**
 	 * Method to get a connection
 	 * 
 	 * @return
 	 * @throws Exception
 	 */
 	public static Connection getConnection() throws Exception {
 		String driver = "org.gjt.mm.mysql.Driver";
 		String url = "jdbc:mysql://localhost/quizdb";
 		String username = "root";
 		String password = "root";
 
 		Class.forName(driver);
 		Connection conn = DriverManager.getConnection(url, username, password);
 		return conn;
 	}
 
 	/**
 	 *  Method to load exercises and quizzes
 	 */
 	@Override
 	public void load(ExerciseCatalog exModel, QuizCatalog quModel) {
 		try {
 			// Get Exercises
 			Connection con = getConnection();
 			con.setAutoCommit(false);
 
 			PreparedStatement ps = con.prepareStatement("select * from exercise");
 			ResultSet tempExsSet =  ps.executeQuery();
 
 			List<Exercise> tempExs = new ArrayList<Exercise>();
 
 			while (tempExsSet.next()) {
 				int exId = tempExsSet.getInt("exercise_id");
 				String descriminator = tempExsSet.getString("descriminator");
 
 				///////////System.out.println(descriminator);
 
 				// Create corresponding exercise based on discriminator
 				Exercise ex = (descriminator.equals("S")) ? new SimpleExercise() :
 					(descriminator.equals("M")) ? new MultipleChoiceExercise() : new EnumerationExercise();
 
 					// Add parameters
 					ex.setExerciseId(exId);
 					ex.setDiscriminator(descriminator.charAt(0));
 					ex.setQuestion(tempExsSet.getString("question"));
 					ex.setCorrectAnswer(tempExsSet.getString("correct_answer"));
 
 					// Get answer hints
 					Scanner scanner = new Scanner(tempExsSet.getString("answer_hints"));
 					scanner.useDelimiter("\\s*,\\s*");
 					// Temporary answerHints list 
 					List<String> tempHints = new ArrayList<String>();
 					// Add each String to temporary answerHints list
 					while (scanner.hasNext()){
 						tempHints.add(scanner.next());
 					}
 					if (scanner!=null){
 						scanner.close();
 					}
 					// Add result to answerHints parameter
 					ex.setAnswerHints(tempHints.toArray(new String[tempHints.size()]));
 
 					ex.setAuthor(Teacher.valueOf(tempExsSet.getString("author").toUpperCase()));
 					ex.setCategory(ExerciseCategory.valueOf(tempExsSet.getString("category").toUpperCase()));
 					ex.setMaxAnswerTime(tempExsSet.getInt("max_answer_time"));
 					ex.setMaxNumberOfAttempts(tempExsSet.getInt("max_number_of_attempts"));
 
 					DateGC tempDate = new DateGC(tempExsSet.getDate("date_registration"));
 					// Add result to dateRegistration parameter 
 					ex.setDateRegistration(tempDate);
 					// Add to exercises based on corresponding subclass
 					if (descriminator.equals("S")){
 						SimpleExercise exS = (SimpleExercise)ex;
 						tempExs.add(exS);
 					}
 					if (descriminator.equals("M")){
 						MultipleChoiceExercise exM = (MultipleChoiceExercise)ex;
 
 						// Get MultipleChoiceExercise
 						PreparedStatement ps2 = con.prepareStatement("select * from multiple_choice_exercise");
 						ResultSet tempMulExsSet =  ps2.executeQuery();
 
 						while (tempMulExsSet.next()) {
 							if (tempMulExsSet.getInt("exercise_id") == exId)
 								exM.setMultipleChoice(tempMulExsSet.getString("multiple_choice"));
 						}
 
 						tempExs.add(exM);
 					}
 					if (descriminator.equals("E")){
 						EnumerationExercise exE = (EnumerationExercise)ex;
 						exE.setInCorrectOrder(false);
 						tempExs.add(exE);
 					}
 			}
 
 			exModel.setExercises(tempExs);
 
 			ps.close();
 
 			// Get Quizzes
 			PreparedStatement ps3 = con.prepareStatement("select * from quiz");
 			ResultSet tempQusSet = ps3.executeQuery();
 
 			List<Quiz> tempQus= new ArrayList<Quiz>();
 
 			while (tempQusSet.next()) {
 
 				int quizId = tempQusSet.getInt("quiz_id");
 
 				Quiz qz = new Quiz();
 
 				// Add parameters
 				qz.setQuizId(quizId);
 				qz.setSubject(tempQusSet.getString("subject"));
 				qz.setLeerJaren(tempQusSet.getInt("grades"));
 				qz.setTeacher(Teacher.valueOf(tempQusSet.getString("teacher").toUpperCase()));
 				qz.setStatus(QuizStatus.valueOf(tempQusSet.getString("state").toUpperCase().replaceAll("\\s+","")));
 				qz.setTest(tempQusSet.getBoolean("is_test"));
 				qz.setUniqueParticipation(tempQusSet.getBoolean("is_unique_participation"));
 
 				// Get date
 				DateGC tempDate = new DateGC(tempQusSet.getDate("date"));
 				int year = tempDate.getGregCal().get(Calendar.YEAR);
 				int month = tempDate.getGregCal().get(Calendar.MONTH);
 				int day = tempDate.getGregCal().get(Calendar.DATE);
 				// Add result to dateRegistration parameter
 				qz.setDate(new DateQuiz(day, month, year));
 
 				tempQus.add(qz);
 			}
 
 			quModel.setQuizCatalogs(tempQus);
 
 			ps3.close();
 
 			// Get QuizExercises
 			PreparedStatement ps4 = con.prepareStatement("select * from quiz_exercise");
 			ResultSet tempQuExsSet = ps4.executeQuery();
 
 			while (tempQuExsSet.next()) {
 
 				int tempScore = tempQuExsSet.getInt("max_score");
 				int tempQuizId = tempQuExsSet.getInt("quiz_id");
 				int tempExerciseID= tempQuExsSet.getInt("exercise_id");
 
 				if (tempQuizId > quModel.getQuizCatalogs().size()){
 					QuizExercise qe = new QuizExercise(tempScore, quModel.getQuizCatalogs().get(tempQuizId - 1 
 							- (tempQuizId - quModel.getQuizCatalogs().size())), exModel.getExercises().get(tempExerciseID - 1));
 
 					quModel.getQuizCatalogs().get(tempQuizId - 1 
 							- (tempQuizId - quModel.getQuizCatalogs().size())).addQuizExercise(qe);
 					exModel.getExercises().get(tempExerciseID - 1).addQuizExercise(qe);
 				}
 				else{
 					QuizExercise qe = new QuizExercise(tempScore, quModel.getQuizCatalogs().get(tempQuizId - 1), 
 							exModel.getExercises().get(tempExerciseID - 1));
 
 					quModel.getQuizCatalogs().get(tempQuizId - 1).addQuizExercise(qe);
 					exModel.getExercises().get(tempExerciseID - 1).addQuizExercise(qe);
 				}
 			}
 
 			ps4.close();
 
 		} catch (IllegalArgumentException ex) {
 			System.out.println(ex);
 		} catch (SQLException ex) {
 			System.out.println(ex);
 		} catch (Exception ex) {
 			System.out.println(ex);
 		}
 	}
 
 	/**
 	 *  Method to add a quiz
 	 */
 	@Override
 	public void addQuiz(CreateQuizView view, ExerciseCatalog exModel,
 			QuizCatalog quModel) {
 		try{
 			Connection con = getConnection();
 			con.setAutoCommit(false);
 
 			if (view.getStatus().equals("Ready")){
 				if (view.getDataModel().getRowCount() == 0) 
 					throw new IllegalArgumentException("Voegtoe ten minste één opdracht aan de quiz!");
 			}
 
 			for (Quiz quizCheck : quModel.getQuizCatalogs()) {
 				checkSubject(quizCheck.getSubject(), view.getSubject());
 			}
 			
 			if (view.getSubject() == null) throw new IllegalArgumentException("Onderwerp is null!");
 			if (view.getSubject().isEmpty()) throw new IllegalArgumentException("Onderwerp is leeg!");
 
 			// Iterate through each row to check maxScore
 			for (int i = 0; i < view.getDataModel().getRowCount(); i++) {
 				if(!isNumeric(String.valueOf(view.getDataModel().getValueAt(i, 1)))) throw new NumberFormatException("MaxScore moet een numeriek waarde zijn!");
 				if(String.valueOf(view.getDataModel().getValueAt(i, 1)).isEmpty()) throw new IllegalArgumentException("MaxScore is leeg!");
 			}
 
 			Quiz quiz = new Quiz(view.getSubject());
 			quiz.setQuizId(quModel.getQuizCatalogs().get(quModel.getQuizCatalogs().size() - 1).getQuizId() + 1);
 			quiz.setLeerJaren(Integer.parseInt(view.getGrade()));
 			quiz.setTeacher(Teacher.valueOf(view.getAuthor().toUpperCase()));
 			quiz.setStatus(QuizStatus.valueOf(view.getStatus().toUpperCase().replaceAll("\\s+", "")));
 
 			PreparedStatement ps = con.prepareStatement("insert into quiz values(?,?,?,?,?,?,?,?)");
 			ps.setInt(1, quiz.getQuizId());
 			ps.setString(2, quiz.getSubject());
 			ps.setObject(3, quiz.getTeacher().toString());
 
 			String dateToConvert = quiz.getDate().getDateInEuropeanFormat();
 			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
 			java.util.Date date = formatter.parse(dateToConvert);
 			long dateToLong = date.getTime();
 			java.sql.Date finalDate = new Date(dateToLong);
 			ps.setDate(4, finalDate);
 
 			ps.setBoolean(5, quiz.isTest());		
 			ps.setBoolean(6, quiz.isUniqueParticipation());
 			ps.setInt(7, quiz.getLeerJaren());		
 			ps.setObject(8, quiz.getStatus().toString());
 			int y = ps.executeUpdate();
 			con.commit();
 
 			// Iterate through each row and add QuizExercises to corresponding quizzes and exercises
 			for (int i = 0; i < view.getDataModel().getRowCount(); i++) {
 				for (Exercise ex : exModel.getExercises()){
 					if (ex.getQuestion().equals(String.valueOf(view.getDataModel().getValueAt(i, 0)).substring(6))){
 
 						QuizExercise tempQE = new QuizExercise(
 								Integer.parseInt(String.valueOf(view.getDataModel().getValueAt(i, 1))), 
 								quiz, ex);
 
 						PreparedStatement ps2 = con.prepareStatement("insert into quiz_exercise values(?,?,?)");
 
 						ps2.setInt(1, tempQE.getQuiz().getQuizId());
 						ps2.setInt(2, tempQE.getExercise().getExerciseId());
 						ps2.setInt(3, tempQE.getMaxScore());
 
 						ps2.executeUpdate();
 						con.commit();
 						ps2.close();
 					}
 				}
 			}
 
 			if (y != 0){
 				view.displayErrorMessage("Quiz is toegevoegd");
 			} 
 			else {
 				view.displayErrorMessage("Quiz is niet toegevoegd");
 			}
 			ps.close();
 
 			view.reset();
 		}
 		catch (NumberFormatException ex) {
 			view.displayErrorMessage(ex.getMessage());
 		} catch(IllegalArgumentException ex){
 			view.displayErrorMessage(ex.getMessage());
 		} catch (Exception ex) {
 			view.displayErrorMessage(ex.getMessage());
 		}
 	}
 
 	/**
 	 *  Method to delete a quiz
 	 */
 	@Override
 	public void deleteQuiz(DeleteQuizView window,
 			DeleteQuizController controller, ExerciseCatalog exerciseCatalog,
 			QuizCatalog quizCatalog) {
 
 		try {
 			Connection con = getConnection();
 			con.setAutoCommit(false);
 
 			PreparedStatement ps1 = con.prepareStatement("delete from quiz_exercise where quiz_id=?");
 			ps1.setInt(1, Integer.parseInt((String)window.getJTableQuiz().getValueAt(window.getJTableQuiz().getSelectedRow(), 0)));
 
 			ps1.executeUpdate();
 			con.commit();
 			ps1.close();
 
 			PreparedStatement ps2 = con.prepareStatement("delete from quiz where quiz_id=?");
 
 			ps2.setInt(1, Integer.parseInt((String)window.getJTableQuiz().getValueAt(window.getJTableQuiz().getSelectedRow(), 0)));
 
 			ps2.executeUpdate();
 			con.commit();
 			ps2.close();
 
 			con.close();
 		} catch (SQLException ex) {
 			System.out.println(ex);
 		} catch (Exception ex) {
 			System.out.println(ex);
 		}
 	}
 
 	/**
 	 *  Method to update a quiz
 	 */
 	@Override
 	public void updateQuiz(ChangeQuizView view, ExerciseCatalog exerciseModel,
 			QuizCatalog quizModel, List<Quiz> tempQuizzes,
 			List<Exercise> tempExercises) {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 *  Method to delete an exercise from a quiz 
 	 */
 	@Override
 	public void deleteFromQuiz(ChangeQuizView view, List<Quiz> tempQuizzes,
 			List<Exercise> tempExercises) {
 		try {
 			Connection con = getConnection();
 			con.setAutoCommit(false);
 
 			PreparedStatement ps1 = con.prepareStatement("delete from quiz_exercise where quiz_id=? AND exercise_id=?");
 			ps1.setInt(1, view.getSelectedQuizValueFromList().getQuizId());
 			ps1.setInt(2, view.getSelectedQuizExerciseValueFromList().getExercise().getExerciseId());
 
 			ps1.executeUpdate();
 			con.commit();
 			ps1.close();
 
 			con.close();
 
 		} catch (SQLException ex) {
 			System.out.println(ex);
 		} catch (Exception ex) {
 			System.out.println(ex);
 		}
 	}
 
 	/**
 	 *  Method to save and close ChangeQuizView window
 	 */
 	@Override
 	public void saveAndClose(DeleteQuizView window,
 			DeleteQuizController controller, ExerciseCatalog exerciseCatalog,
 			QuizCatalog quizCatalog) {
 		window.dispose();
 	}
 
 	/**
 	 *  Method to add an exercise to a quiz
 	 */
 	@Override
 	public void addToQuiz(ChangeQuizView view, List<Quiz> tempQuizzes,
 			List<Exercise> tempExercises, ChangeQuizController controller) {
 		try{
 			//add to quiz
 			int maxScore = 5;   //Integer.parseInt(view.getMaxScore());
 
 			QuizExercise qe = new QuizExercise(maxScore, view.getSelectedQuizValueFromList(), 
 					view.getSelectedExerciseValueFromList());
 
 			for (QuizExercise qE : view.getSelectedQuizValueFromList().getQuizExercises()){
 				if (qE.getQuiz().getQuizId() == view.getSelectedQuizValueFromList().getQuizId() &&
 						qE.getExercise().getExerciseId() == view.getSelectedExerciseValueFromList().getExerciseId())
 					throw new IllegalArgumentException("Opdracht bestaat al.");
 			}
 
 			view.getSelectedQuizValueFromList().addQuizExercise(qe);
 
 			Connection con = getConnection();
 			con.setAutoCommit(false);
 
 			PreparedStatement ps1 = con.prepareStatement("insert into quiz_exercise values(?,?,?)");
 
 			ps1.setInt(1, qe.getQuiz().getQuizId());
 			ps1.setInt(2, qe.getExercise().getExerciseId());
 			ps1.setInt(3, qe.getMaxScore());
 
 			ps1.executeUpdate();
 			con.commit();
 			ps1.close();
 
 		} catch (SQLException ex) {
 			System.out.println(ex);
 		} catch (Exception ex) {
 			System.out.println(ex);
 		}
 	}
 
 	/**
 	 * Check numeric input
 	 * 
 	 * @param str
 	 * @return
 	 */
 	public boolean isNumeric(String str)  
 	{  
 		try{  
 			@SuppressWarnings("unused")
 			int i = Integer.parseInt(str);  
 		}  
 		catch(NumberFormatException ex){  
 			return false;  
 		}  
 		return true;  
 	}
 	
 	/**
 	 * Method to check if subject already exists
 	 * 
 	 * @param str1
 	 * @param str2
 	 */
 	public void checkSubject(String str1, String str2){
 		String tempS1 = str1.toLowerCase();
 		String tempS2 = str2.toLowerCase();
 		
 		// Remove ignored signs from both Strings
 		tempS1 = tempS1.replaceAll("[.]|[,]|[!]|[!?]", "");
 		tempS2 = tempS2.replaceAll("[.]|[,]|[!]|[!?]", "");
 		
 		// Remove ignored words from both Strings
 		tempS1 = tempS1.replaceAll("de\\s+|een\\s+|het\\s+|met\\s+|van\\s+|in\\s+", "");
 		tempS2 = tempS2.replaceAll("de\\s+|een\\s+|het\\s+|met\\s+|van\\s+|in\\s+", "");
 		
 		if (tempS1.equals(tempS2)) throw new IllegalArgumentException("Quiz bestaat al!");
 	}
 }
