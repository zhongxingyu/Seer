 package site;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class CharliesTests {
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
 		*/
 //		ArrayList<Result> results = QuizResult.getWorstQuizTakers(63, 0);
 //		System.out.println(results);
 //		System.out.println(results.get(0).durationOfQuiz);
 //		System.out.println(results.get(0).durationString());
 //		
 		
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
 	//	AdminControl.removeAccount(15);
 		//	System.out.println(AdminControl.getAnnouncements(10));
 //		
 //		
 //		System.out.println(AdminControl.getStatistics());
 //		AdminControl.promoteToAdmin(1,5);
 //		if(!AdminControl.demoteFromAdmin(5,7)) System.out.println("failure");
 //		System.out.println(AdminControl.isAdmin(3));
 //		MyDB.close();
 //
//		System.out.println(AdminControl.clearQuizResults(137));
 	}
 	/*
 	@Test
 	public void testAnswerLog(){
 		new AnswerLog();
 		System.out.println(	AnswerLog.storeUserAnswer(29, 17,27, "THIS IS MY ANSWER"));
 		System.out.println("");
 		System.out.println(AnswerLog.getUserAnswers(17));
 	}*/
 	
 	@Test 
 	public void testCatTag(){
 		new CatTagManager();
 //		System.out.println(CatTagManager.correctTagFormat("love,have,suplex"));
 //		System.out.println(CatTagManager.correctTagFormat("qw asdfasd ,asdfafeq d,asdfasdf2,,,,"));
 //		System.out.println(CatTagManager.correctTagFormat("qw asdfasdasa;sduf98234qvnds8f"));
 //		System.out.println(CatTagManager.correctTagFormat(""));
 
 
 	//	System.out.println(CatTagManager.addStringOfTagsToQuiz(2, "love,have,suplex"));
 		
 		System.out.println(CatTagManager.parseTags("S&M"));
 //		System.out.println(CatTagManager.parseTags(" love , have, suplex "));
 //		System.out.println(CatTagManager.parseTags("qw asdfasd ,asdfafeq d,asdfasdf2,,,,"));
 //		System.out.println(CatTagManager.parseTags("qw asdfasdasa;sduf98234qvnds8f"));
 //		System.out.println(CatTagManager.getQuizzesFromCategory("Politics"));
 //		System.out.println(CatTagManager.getQuizzesFromTag("love"));
 //			
 //	}
 //		System.out.println(CatTagManager.getTagsFromQuiz(13));
 		
 //		System.out.println(CatTagManager.getQuizzesFromTag("love"));
 	
 //		System.out.println(CatTagManager.createCategory("FirstCatEver"));
 //		System.out.println(CatTagManager.createCategory("SecondCat"));
 //		
 //		System.out.println(CatTagManager.categorizeQuiz(2, "FirstCatEver"));
 //		System.out.println(CatTagManager.categorizeQuiz(1, "FirstCatEver"));
 //		System.out.println(CatTagManager.categorizeQuiz(3, "FirstCatEver"));
 //		
 //		System.out.println(CatTagManager.deleteCategory("FirstTagEver"));
 //		
 //		System.out.println(CatTagManager.removeQuizCatsTags(2));
 //		
 //		
 //		
 //		System.out.println("Get SecondCat: " + CatTagManager.getQuizzesFromCategory("SecondCat"));
 		
 //		System.out.println(CatTagManager.getCategoryFromQuiz(2));
 //		System.out.println(CatTagManager.getCategories());
 		
 //		String cats[] = {"Food", "Politics", "Techonology", "Lifestyle" , "Art", "Science", "People", "World", "Film", "Cars", "Music", "Style", "Religion", "Health", "Opinion", "Fashion", "Design", "Culture"};
 //		for (String cat: cats){
 //			System.out.println(CatTagManager.createCategory(cat));
 //		}
 //	}
 	
 	}
 	
 	@Test 
 	public void testReview(){
 		new ReviewManager();
 //		System.out.println(ReviewManager.tookQuiz(5, 27));
 //		System.out.println(ReviewManager.tookQuiz(213423, 2723432));
 //		System.out.println(ReviewManager.tookQuiz(5, -12312));
 		//System.out.println(ReviewManager.addReview(5, 27, "This quiz was the most mindblowingly amazing quiz ever!",1));
 //		System.out.println(ReviewManager.addReview(5, 27, "Worst quiz ever",4));
 		//System.out.println(ReviewManager.getReviews(27));
 		
 		
 		
 	}
 	
 	/*
 	@Test 
 	public void testREport(){
 		new ReportManager();
 		System.out.println(ReportManager.reportQuiz(5, 67, "This quiz makes fun of my mother"));
 		ReportManager.removeReport(57);	
 		System.out.println(ReportManager.getNumReports(67));
 		
 	}
 	*/
 	
 	@Test
 	public void testSalt(){
 		AccountManager manager = new AccountManager();
 		//System.out.println(manager.updatePasswordWithSalt(user_id, prevPassword));
 		
 	}
 	
 	
 	@After
 	public void after(){
 		
 	}
 	
 	
 }
