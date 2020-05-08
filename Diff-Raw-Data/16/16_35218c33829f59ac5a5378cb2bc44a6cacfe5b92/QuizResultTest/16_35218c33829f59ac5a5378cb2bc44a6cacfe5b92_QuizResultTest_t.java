 package site;
 
 import static org.junit.Assert.assertTrue;
 
import java.text.DateFormat;

 import org.junit.Before;
 import org.junit.Test;
 
 public class QuizResultTest {
 	QuizResult qr;
 
 	@Before
 	public void setup(){
 		qr = new QuizResult();
 	}
 
 	
 	
 	/**The field that bothers me the most is the date field. How do I deal with the date field and going from 
 	 * Java date to mysql date and back?
 	 * 
 	 * */
	
 	@Test
 	public void test1(){
 		Result rs = QuizResult.getResultFromID(4);
 		Result rs2 = QuizResult.getResultFromID(2);
 		System.out.println(rs.toString());
 		System.out.println(rs2.toString());
 		
 		System.out.println(DateFormat.getDateTimeInstance().format(rs.timeStamp));
 		System.out.println(DateFormat.getDateTimeInstance().format(rs2.timeStamp));
	}
 	
 	@Test
 	public void test2(){
 		assertTrue(!QuizResult.isEmpty());
 		QuizResult.getUserPerformanceOnQuiz(1, 3);
 		QuizResult.getUserPerformanceOnQuiz(2, 3);
 		System.out.println(QuizResult.getUserPerformanceOnQuiz(2, 3,"BY_SCORE"));
 		System.out.println(QuizResult.getBestQuizTakers(3, 0));
 		System.out.println(QuizResult.getBestQuizTakers(3, 3));
 		
 		System.out.println(QuizResult.getWorstQuizTakers(3, 0));
 		
 		
 	}
 }
