 package api;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.swing.ImageIcon;
 
 import casa.CASAProcess;
 import casa.ProcessOptions;
 import casa.TransientAgent;
 import casa.ui.AgentUI;
 import casa.ui.StandardOutAgentUI;
 
 public class API implements API_Interface {
 
 	/* 
 	 * CASA Object variables
 	 */
 	private CASAProcess CASA = null;
 	private TransientAgent Environment = null;
 	private AbstractRobot Robot = null;
 	private AgentUI UI = null;
 		
 	/*
 	 * User and Database Information
 	 */
 	private int UserID;
 	private Database DB;
 	
 	/*
 	 * Option variables
 	 */
 	private static String tracetags ="info5,warning,msg,iRobot,-boundSymbols,-policies9,-commitments,-eventqueue,-conversations";
 	private static String SerialLocation = "/dev/rfcomm0";
 	private static int EnvironmentPort = 5780;
 	private static int RobotPort = 5781;
 	
 	/*
 	 *  TESTING MAIN... SHOULD NOT BE USED IN PRODUCTION
 	 */
 	public static void main(String[] args) throws Exception {
 		
 		System.err.println("API runtime testing function called...");
 		API api = new API();
 		api.loadToSimulator("example.lisp");
 		
 	}
 	
 	/*
 	 *****************************
 	 * INITIALIZATION            *
 	 *****************************
 	 */
 	
 	/**
 	 * Constructor. Calls initialize() on itself.
 	 * @throws Exception 
 	 */
 	public API() throws Exception {
 		this.initialize();
 	}
 	
 	/**
 	 * Checks if the API has been initialized<br>
 	 * If not, it will be initialized.<br>
 	 * The CASA process will be stared.
 	 * @return true if the API is initialized, false if there is an error
 	 * @throws Exception 
 	 */
 	public boolean initialize() throws Exception{
 		try {
 			
 			CASA = CASAProcess.getInstance();
 			
 			ProcessOptions options = new ProcessOptions(CASA);
 			options.traceTags = tracetags;
 			options.tracing = true;
 			
 			CASA.setOptions(options);
 			
 			UI = new StandardOutAgentUI();
 			
 			DB = Database.getInstance();
 						
 			return true;
 		} catch (Exception e) 
 		{
 			e.printStackTrace();
 			throw new Exception("API Initialization Failed.");
 		}
 		
 	}
 
 	/*
 	 *****************************
 	 * DATABASE / USER FUNCTIONS *
 	 *****************************
 	 */
 	
 	@Override
 	public int authenticate_user(String user_name, String password) {
 		
 		UserID = Authenticator.auth(user_name, password);
 		
 		// Check is the user exists in the database, create an entry if not.
 		
 		return UserID;
 	}
 	
 	@Override
 	public int getUserType(int UserID){
 		
 		String query = "SELECT 'type' FROM sql24765.users WHERE 'student_ID'=" + String.valueOf(UserID);
 		String type = "";
 		
 		try {
 			ResultSet response = DB.query(query);
 			if(response.next() && response.getInt(1) == 0)
 				return 0;
 			type = response.getString(1);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		if(type.compareTo("teacher") == 0)
 			return 1;
 		else if(type.compareTo("student") == 0)
 			return 2;
 		else
 			return 0;
 	}
 	
 	@Override
 	public Map<String, String> getUserProgress(int UserID){
 		Map<String,String> m = new HashMap<String,String>();
 		
 		try {			
 			String q = "SELECT * FROM sql24765.users";
 			ResultSet r1 = DB.query(q);
 			r1.next();
 			String query = "SELECT * FROM sql24765.users WHERE 'student_ID' =" + String.valueOf(UserID);
 			ResultSet response = DB.query(query);
 			
 		
 			if(response.next())
 			{
 				if(response.getInt(1) == 0)
 					return null;
 			}	
 			else
 				return null;
 			
 			
 			int chpt = 0;
 			int cl = 0;
 			int size = 0;
 			
 			while(!r1.isAfterLast()){
 				
 				chpt+=r1.getInt(5);
 				cl+=r1.getInt(6);
 				size++;
 				r1.next();
 			}
 			
 			double avgchpt = chpt/size;
 			double avgcl = cl/size;
 			
 			
 			m.put("id",String.valueOf(response.getInt(3)));
 			m.put("name",response.getString(2));
 			m.put("chapter", String.valueOf((int)response.getInt(5)));
 			m.put("challenge", String.valueOf((int)response.getInt(6)));
 			m.put("avgchapter", String.valueOf((double)avgchpt));
 			m.put("avgchallenge", String.valueOf((double)avgcl));
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return m;
 	}
 	
 	@Override
 	public Map<Integer, Map<String, String>> getAllUserProgress(){
 		
 		Map<Integer, Map<String,String>> m = new HashMap<Integer, Map<String,String>>();
 		Map<String,String> average = new HashMap<String, String>();
 		
 		String query = "SELECT * FROM sql24765.users";
 		try {
 			ResultSet response = DB.query(query);
 			
 			if(response.next() && response.getInt(1) == 0)
 				return null;
 			
 			int chpt = 0;
 			int cl = 0;
 			int size = 0;
 		
 			while(!response.isAfterLast()){
 					
 				chpt+=response.getInt(5);
 				cl+=response.getInt(6);
 				size++;
 					
 				
 				Map<String,String> d = new HashMap<String, String>();
 					
 				d.put("name", response.getString(2));
 				d.put("chapter", String.valueOf((int)response.getInt(5)));			//d.put("chapter",lesson_complete)
 				d.put("challenge", String.valueOf((int)response.getInt(6)));		//d.put("challenge", challenge_complete)
 				
 				m.put(response.getInt(3), d);		//m.put(student_ID, Map object)
 				response.next();
 			}
 			
 			double avgchpt = chpt/size;
 			double avgcl = cl/size;
 			
 			average.put("name","average");
 			average.put("chapter",String.valueOf((double)avgchpt));
 			average.put("challenge",String.valueOf((double)avgcl));
 			
 			m.put(0,average);
 			
 		}catch (SQLException e){
 			e.printStackTrace();
 		}
 		
 		return m;
 	}
 	
 	@Override
 	public Vector<Integer> getAllUserIDs(){
 		
 		Vector<Integer> v = new Vector<Integer>();
 		v.add(0);
 		String query = "SELECT * FROM sql24765.users";
 		try {
 			ResultSet response = DB.query(query);
 			response.next();
 			if(response.getInt("student_ID") == 0)
 				return null;
 			
 			while(!response.isAfterLast()){
 				
 				v.add(response.getInt("student_ID"));
 				response.next();
 				
 			}
 			}catch (SQLException e){
 				e.printStackTrace();
 			}
 			
 		return v;
 	}
 	
 	@Override
 	public ImageIcon getLesson(int Chapter, int Lesson, int Slide){
 				
 		String imgStr = "Lessons/";
 		
 		if(Chapter == 0)
 			return new ImageIcon("Usermanual/UM " + Slide + ".png");
 		
 		if(Lesson == 0)
 			imgStr += "Chapter " + Chapter;
 		else if(Slide == 0)
 			imgStr += "Lesson " + Chapter + "-" + Lesson;
 		else
 			imgStr += "LessonSlides/Lesson "+ Chapter + "-" + Lesson + "-" + Slide;
 		
 		imgStr += ".png";
 		
 		System.out.println(Chapter + " " + Lesson + " " + Slide + " " +imgStr);
 		return new ImageIcon(imgStr);
 	}
 	@Override
 	public ImageIcon getUserManual(int Slide)
 	{
 		return null;
 		
 	}
 	@Override
 	public ImageIcon getChallenge(int tier, int number, boolean Slide){
 
 		String imgStr = "Challenges/";
 		
 		if(tier == 0)
 			return null;
 		
 		if(number == 0)
 			imgStr += "Tier " + tier;
 		else if(!Slide)
 			imgStr += "Challenge " + tier + "-" + number;
 		else
 			imgStr += "ChallengeSlides/Challenge " + number + "-" + Slide;
 		
 		imgStr += ".png";
 				
 		return new ImageIcon(imgStr);
 	}
 	
 	public void setUserChapter(int UserID, int progress) throws Exception{
 		
 		String query = "UPDATE sql24765.users SET 'lesson_complete'=" + String.valueOf(progress) + " WHERE 'student_ID'=" + String.valueOf(UserID);
 		ResultSet response = DB.query(query);
 		if(response.next() && response.getInt(1) == 0)
 			throw new Exception("No user with the ID " + UserID);
 	}
 	
 
 	public void setUserChallenge(int UserID, int progress) throws Exception{
 		
 		String query = "UPDATE sql24765.users SET 'challenge_complete'=" + String.valueOf(progress) + " WHERE 'student_ID'=" + String.valueOf(UserID);
 		ResultSet response = DB.query(query);
 		if(response.next() && response.getInt(1) == 0)
 			throw new Exception("No user with the ID " + UserID);
 	}
 
 	/*
 	 *****************************
 	 * ROBOT FUNCTIONS           *
 	 *****************************
 	 */
 	
 	@Override
 	public String loadToRobot(String filepath) {
 		
 		loadRobotAgent();
 		
 		if(filepath != null && filepath != "")
 			Robot.abclEval(fileRead(filepath), null);
 
 		return null;		
 	}
 
 	@Override
 	public String loadToSimulator(String filepath) {
 		
 		loadEnvironment();
 		loadSimulatorAgent();		
 		
 		// Run code found in file at file path, if one exists.
 		if(filepath != null && filepath != "")
 		{
 			Robot.abclEval(fileRead(filepath), null);
 		}
 			
 		return null;		
 	}	
 	
 	@Override
 	public String translateLoadToRobot(String filepath) {
 		
 		loadRobotAgent();
 		
 		if(filepath != null && filepath != "")
 			Robot.abclEval(fileRead(Translator.translateFile(filepath)), null);
 
 		
 		return "Translation not yet implemented";
 	}
 
 	@Override
 	public String translateLoadToSimulator(String filepath) {
 
 		loadEnvironment();
 		loadSimulatorAgent();
 		
 		if(filepath != null && filepath != "")
 			Robot.abclEval(fileRead(Translator.translateFile(filepath)), null);
 		
 		return "Translation not yet implemented";
 	}
 	
 	@Override
 	public RobotControl loadRobotController() {
 		loadRobotAgent_WithConsole();
 		return new RobotControl(Robot);
 	}
 	
 	@Override
 	public RobotControl loadSimulatorController() {
 		loadEnvironment();
 		loadSimulatorAgent();
 		return new RobotControl(Robot);
 	}
 	
 	
 	/*
 	 *****************************
 	 * PACKAGE FUNCTIONS         *
 	 *****************************
 	 */
 	
 	
 	/**
 	 * Reads the file at the file path and returns the contents as a string<br>
 	 * Accessible at the package level.
 	 * @param filepath
 	 * @return a String of the file contents
 	 */
 	static String fileRead(String filepath){
 				
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader(filepath));
 			StringBuilder builder = new StringBuilder();
 			String line = null;
 			
 			while((line = reader.readLine()) != null) { 
 				builder.append(line); 
 			}
 			
 			reader.close();
 			
 			return builder.toString();
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	
 		return null;
 		
 	}
 
 	
 	/*
 	 *****************************
 	 * PRIVATE FUNCTIONS         *
 	 *****************************
 	 */
 	
 	/**
 	 * Starts a robot agent with appropriate options.<br>
 	 * Access to the robot is provided through the 'Robot' variable.
 	 */
 	private void loadRobotAgent(){
 		
 		if(Robot == null)
 			Robot = (Robot) CASAProcess.startAgent(UI, Robot.class,
 				"Dayton",
 				RobotPort,
 				"LAC", "9000",
 				"PROCESS", "CURRENT",
 				"TRACETAGS", tracetags,
 				"TRACE", "10",
 				"MARKUP", "KQML",
 				"OUTSTREAM",SerialLocation, 
                 "INSTREAM", SerialLocation,
                 "INTERFACE", "NONE"
 				);
 	}
 	
 	/**
 	 * Starts a robot agent with appropriate options.<br>
 	 * Access to the robot is provided through the 'Robot' variable.
 	 */
 	private void loadRobotAgent_WithConsole(){
 		
 		if(Robot == null)
 			Robot = (Robot) CASAProcess.startAgent(UI, Robot.class,
 				"Dayton",
 				RobotPort,
 				"LAC", "9000",
 				"PROCESS", "CURRENT",
 				"TRACETAGS", tracetags,
 				"TRACE", "10",
 				"MARKUP", "KQML",
 				"OUTSTREAM",SerialLocation, 
                 "INSTREAM", SerialLocation
 				);
 	}
 	
 	/**
 	 * Starts a robot agent with appropriate options.<br>
 	 * Access to the robot is provided through the 'Robot' variable.
 	 */
 	private void loadSimulatorAgent(){
 		if(Robot == null)
 			Robot = (Simulator) CASAProcess.startAgent(UI, Simulator.class,
 				"Cutesy",
 				RobotPort,
 				"LAC", "9000",
 				"PROCESS", "CURRENT",
 				"TRACETAGS", tracetags,
 				"TRACE", "10",
 				"MARKUP", "KQML",
 				"OUTSTREAM","sim.out", 
 		        "INSTREAM", "sim.in",
 		        "INTERFACE", "NONE"
 				);
 	}
 	
 	/**
 	 * Starts a environment agent with appropriate options.<br>
 	 * Access to the robot is provided through the 'Environment' variable.
 	 */
 	private void loadEnvironment(){
 		
 		if(Environment == null)
 			Environment = CASAProcess.startAgent(UI, SimEnvironment.class,
 				"SimEnvironment",
 				EnvironmentPort,
 				"LAC", "9000",
 				"PROCESS", "CURRENT",
 				"TRACETAGS", tracetags,
 				"TRACE", "10",
 				"MARKUP", "KQML"
 				);
 	}
 	
 	
 	
 }
