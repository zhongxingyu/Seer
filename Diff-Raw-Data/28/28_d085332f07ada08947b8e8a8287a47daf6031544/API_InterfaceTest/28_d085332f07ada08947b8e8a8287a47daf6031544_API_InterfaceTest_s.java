 package api;
 
 
 import static org.junit.Assert.*;
 
 import java.util.Map;
 import java.util.Vector;
 
 import javax.swing.ImageIcon;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import api.API;
 import api.API_Interface;
 
 public class API_InterfaceTest {
 
 	public static API_Interface api = null;
 	
 	@BeforeClass
 	public static void beforeTestClassSetup() {
 		try {
 			api = new API();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * authenticate_user negative tests
 	 * @throws Exception 
 	 */
 	@Test
 	public void authenticate_userTestArg1Null() {
 		assertEquals(api.authenticate_user(null, "abcd"), 0);
 	}
 	
 	@Test
 	public void authenticate_userTestArg1Empty(){
 		assertEquals(api.authenticate_user("", "abcd"), 0);
 	}
 	@Test
 	public void authenticate_userTestArg2Null()  {
 		
 		assertEquals(api.authenticate_user("abcd", null), 0);
 	}
 	@Test
 	public void authenticate_userTestArg2Empty() {
 		assertEquals(api.authenticate_user("abcd",""), 0);
 	}
 	@Test
 	public void authenticate_userTestArgbothNull()  {
 
 		assertEquals(api.authenticate_user(null, null), 0);
 	}
 	@Test
 	public void authenticate_userTestArgbothEmpty()  {
 
 		assertEquals(api.authenticate_user("",""), 0);
 	}
 	@Test
 	public void authenticate_userTestArgNullEmpty(){
 
 		assertEquals(api.authenticate_user(null, ""), 0);
 	}
 	@Test
 	public void authenticate_userTestArgEmptyNull(){
 
 		assertEquals(api.authenticate_user("", null), 0);
 	}
 	
 	/**
 	 * loadToRobot negative tests
 	 */
 	@Test (expected=IllegalArgumentException.class)
 	public void loadToRobotTestArgNull() {
 		api.loadToRobot(null);
 	}
 	
 	@Test (expected=IllegalArgumentException.class)
 	public void loadToRobotTestArgEmpty(){
 		api.loadToRobot("");
 	}
 	
 	/**
 	 * loadToSimulator negative tests
 	 */
 	@Test (expected=IllegalArgumentException.class)
 	public void loadToSimulatorTestArgNull() {
 		api.loadToSimulator(null);
 	}
 	
 	@Test (expected=IllegalArgumentException.class)
 	public void loadToSimulatorTestArgEmpty() {
 		api.loadToSimulator("");
 	}
 	
 	
 	/**
 	 * Database access function Tests
 	 */
 	// get User Type tests
 	@Test
 	public void getUserTypeBadInput(){
 		
 		int result = api.getUserType(-1);	
 		assertSame(result, 0);
 	}
 	
 	// get User Progress tests
 	@Test
 	public void getUserProgressBadInput(){
 		
 		Map<String, String> result = api.getUserProgress(-1);	
 		assertSame(result, 0);
 	}
 	
 	// get All User Progress tests
 	@Test
 	public void getAllUserProgress(){
 		
 		Map<Integer, Map<String, String>> result = api.getAllUserProgress();	
 		assertNotNull(result);
 	}
 	
 	// get All User IDs tests
 	@Test
 	public void getAllUserIDs(){
 		
 		Vector<Integer> result = api.getAllUserIDs();	
 		assertNotNull(result);
 	}
 	
 	// get Lessons tests
 	@Test (expected=IllegalArgumentException.class)
 	public void getLessonAll3BadInput(){
 		
 		api.getLesson(-1, -2, -3);	
 	}
 	
 	@Test (expected=IllegalArgumentException.class)
 	public void getLesson1stBadInput(){
 		
 		api.getLesson(-1, 0, 0);	
 	}
 	
 	@Test (expected=IllegalArgumentException.class)
 	public void getLesson2ndBadInput(){
 	
 		api.getLesson(0, -1, 0);	
 	}
 	
 	@Test (expected=IllegalArgumentException.class)
 	public void getLesson3rdBadInput(){
 		
 		api.getLesson(0, 0, -1);	
 	}
 	
 	// get User Manual tests
 	@Test (expected=IllegalArgumentException.class)
 	public void getUserManualBadInput(){
 	
 		api.getUserManual(-1);	
 	}
 	
 	// get Challenge tests
 	@Test (expected=IllegalArgumentException.class)
 	public void getChallengeFirst2BadInput(){
 		
 		api.getChallenge(-1, -2, false);	
 	}
 	
 	@Test (expected=IllegalArgumentException.class)
 	public void getChallenge1stBadInput(){
 	
 		api.getChallenge(-1, 0, true);	
 	}
 	
 	@Test (expected=IllegalArgumentException.class)
 	public void getChallenge2ndBadInput(){
 		
 		api.getChallenge(0, -1, false);	
 	}
 	
 	// set User Chapter tests
 	@Test (expected=Exception.class)
 	public void setUserChapterFirst2BadInput() throws Exception{
 		
 		api.setUserChapter(-1, -2);	
 	}
 	
 	@Test (expected=Exception.class)
 	public void setUserChapter1stBadInput() throws Exception{
 		
 		api.setUserChapter(-1, 0);	
 	}
 	
 	@Test (expected=Exception.class)
 	public void setUserChapter2ndBadInput() throws Exception{
 		
 		api.setUserChapter(0, -1);	
 	}
 	
 	// set User Challenge tests
 	@Test (expected=Exception.class)
 	public void setUserChallengeFirst2BadInput() throws Exception{
 		
 		api.setUserChallenge(-1, -2);	
 	}
 	
 	@Test (expected=Exception.class)
 	public void setUserChallenge1stBadInput() throws Exception{
 		
 		api.setUserChallenge(-1, 0);	
 	}
 	
 	@Test (expected=Exception.class)
 	public void setUserChallenge2ndBadInput() throws Exception{
 		
 		api.setUserChallenge(0, -1);	
 	}
 
 	/**
 	 * Robot function Tests
 	 */
 	// load to robot tests
 	@Test (expected=IllegalArgumentException.class)
 	public void loadToRobotNullInput(){
 		
 		api.loadToRobot(null);	
 	}
 	
 	@Test (expected=IllegalArgumentException.class)
 	public void loadToRobotEmptyInput(){
 		
 		api.loadToRobot("");	
 	}
 	
 	// load to simulator tests
 	@Test (expected=IllegalArgumentException.class)
 	public void loadToSimulatorNullInput(){
		
 		api.loadToRobot(null);	
 	}
 	
 	@Test (expected=IllegalArgumentException.class)
 	public void loadToSimulatorEmptyInput(){
		
 		api.loadToRobot("");	
 	}
 	
 	/**
 	 * Package function Tests
 	 */
 	// translate Load To Simulator tests
 	@Test (expected=IllegalArgumentException.class)
 	public void fileReadNullInput(){
 		API.fileRead(null);	
 	}
 	
 	@Test (expected=IllegalArgumentException.class)
 	public void fileReadEmptyInput(){
 		API.fileRead("");	
 	}
 	
 	
 }
