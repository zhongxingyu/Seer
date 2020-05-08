 package site;
 
 import org.junit.After;
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
 /*
 	@Test
 	public void test1(){
 		Result rs = QuizResult.getResultFromID(4);
 		Result rs2 = QuizResult.getResultFromID(2);
 		System.out.println(rs.toString());
 		System.out.println(rs2.toString());
 		
 		System.out.println(DateFormat.getDateTimeInstance().format(rs.timeStamp));
 		System.out.println(DateFormat.getDateTimeInstance().format(rs2.timeStamp));
 	}
 	*/
 	@Test
 	public void test2(){
 		/*assertTrue(!QuizResult.isEmpty());
 		QuizResult.getUserPerformanceOnQuiz(1, 3);
 		QuizResult.getUserPerformanceOnQuiz(2, 3);
 		System.out.println(QuizResult.getUserPerformanceOnQuiz(2, 3,"BY_SCORE"));
 		System.out.println(QuizResult.getBestQuizTakers(3, 0));
 		System.out.println(QuizResult.getBestQuizTakers(3, 3));
 		
 		System.out.println(QuizResult.getWorstQuizTakers(3, 0));
 		*/
 		/*double score = 3/(double)7;
 		DecimalFormat df = new DecimalFormat ("0%");
 		System.out.println(df.format(score));
 		double score2 = 323423.0;
 		System.out.println("score: " + score2);
 		DecimalFormat df2 = new DecimalFormat ("#");
 		System.out.println(df2.format(score2));
 		*/
 	}
 	
 	@Test
 	public void test3(){
 		new AdminControl();
 	/*	System.out.println(AdminControl.getAnnouncements(10));
 		
		AdminControl.getStatistics();
	*/	//AdminControl.demoteFromAdmin(1,9);
 //		if(!AdminControl.demoteFromAdmin(5,7)) System.out.println("failure");
 	//	System.out.println(AdminControl.isAdmin(3));
 		//MyDB.close();
 
 		System.out.println(AdminControl.clearQuizResults(13));
 	}
 	
 	@Test
 	public void testAnswerLog(){
 		new AnswerLog();
 //		System.out.println(	AnswerLog.storeUserAnswer(29, 17,27, "THIS IS MY ANSWER"));
 //		System.out.println("");
 //		System.out.println(AnswerLog.getUserAnswers(17));
 	}
 	
 	@After
 	public void after(){
 		
 	}
 	
 	
 }
