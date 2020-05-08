 package quizweb.record;
 
 import static org.junit.Assert.*;
 
 import java.util.*;
 
 import org.junit.*;
 
 import quizweb.*;
 import quizweb.achievement.*;
 
 public class RecordTest {
 	User stanford;
 	User google;
 	User facebook;
 
 	@Before
 	public void setUp() throws Exception {
 		XMLReader.loadAllXML();
 		stanford = User.getUserByUsername("stanford");
 		google = User.getUserByUsername("google");
 		facebook = User.getUserByUsername("facebook");		
 	}
 	
 	@Test
 	public void testTakenRecord() {
 		Quiz quiz = Quiz.getQuizByQuizName("Bunny Quiz");
 		quiz = Quiz.getQuizByQuizID(quiz.quizID);
 		assertEquals(quiz.name, "Bunny Quiz");
 		
 		ArrayList<QuizTakenRecord> googleRecords = QuizTakenRecord.getQuizHistoryByQuizIDUserID(quiz.quizID, google.userID);
 		assertEquals(googleRecords.size(), 1);
 		
 		QuizTakenAchievement.updateAchievement(facebook);
 		quiz = Quiz.getQuizByQuizName("Cities");
 		ArrayList<Object> userAnswer = new ArrayList<Object>();
 		userAnswer.add(new String("London"));
 		userAnswer.add(new String("Paris"));
 		userAnswer.add(new String("Rome"));
 		userAnswer.add(new String("Tokyo"));
 		userAnswer.add(new String("Hong Kong"));
 		userAnswer.add(new String("Stanford, California"));
 		assertEquals(quiz.getScore(userAnswer), 60, 1e-6);
		
		ArrayList<Achievement> achievements = facebook.getAchievements();
		assertEquals(achievements.size(), 2);
	}
 
 }
