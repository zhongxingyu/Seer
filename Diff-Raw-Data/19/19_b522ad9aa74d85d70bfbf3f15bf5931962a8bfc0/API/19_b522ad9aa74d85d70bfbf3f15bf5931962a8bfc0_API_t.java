 package api;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
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
 	@SuppressWarnings("unused")
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
 		if(this.initialize() == false)
 		{
 			throw new Exception("API Initialization Failed.");
 		}
 	}
 	
 	/**
 	 * Checks if the API has been initialized<br>
 	 * If not, it will be initialized.<br>
 	 * The CASA process will be stared.
 	 * @return true if the API is initialized, false if there is an error
 	 */
 	private boolean initialize()	{
 		try {
 			
 			CASA = CASAProcess.getInstance();
 			
 			ProcessOptions options = new ProcessOptions(CASA);
 			options.traceTags = tracetags;
 			options.tracing = true;
 			
 			CASA.setOptions(options);
 			
 			UI = new StandardOutAgentUI();
 			
 			DB = Database.getInstance();
 						
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	/*
 	 *****************************
 	 * DATABASE / USER FUNCTIONS *
 	 *****************************
 	 */
 	
 	@Override
 	public int authenticate_user(String user_name, String password) {
 		
 		// TODO ITER 2: Database functions as needed.
 		UserID = Authenticator.auth(user_name, password);
 		
 		// Check is the user exists in the database, create an entry if not.
 		
 		return UserID;
 	}
 	
 	@Override
 	public int getUserType(int UserID){
 		
 		// TODO ITER 2: Add actual database functions.
 		
 		// FAKED OUTPUT
 		if(UserID == 0)
 			return 1;
 		else
 			return 2;
 	}
 	
 	@Override
 	public Map<String, String> getUserProgress(int UserID){
 		
 		// TODO ITER 2: Add actual database functions.
 		
 		// FAKED OUTPUT
 		Map<String,String> m = new HashMap<String, String>();
 		
 		if(UserID == 111111){
 			m.put("id","111111");
 			m.put("name","Alice");
 			m.put("chapter","3");
 			m.put("challenge","1");
 			m.put("avgchapter","2.5");
 			m.put("avgchallenge","1.5");
 		}
 		else if(UserID == 222222){
 			m.put("id","111111");
 			m.put("name","Bob");
 			m.put("chapter","2");
 			m.put("challenge","2");
 			m.put("avgchapter","2.5");
 			m.put("avgchallenge","1.5");
 		}
 		else {
 			m.put("id","000000");
 			m.put("name","average");
 			m.put("chapter","0");
 			m.put("challenge","0");
 			m.put("avgchapter","2.5");
 			m.put("avgchallenge","1.5");
 		}
 		
 		return m;
 	}
 	
 	@Override
 	public Map<Integer, Map<String, String>> getAllUserProgress(){
 		
 		// TODO ITER 2: Add actual database functions.
 		
 		// FAKED OUTPUT
 		Map<Integer, Map<String,String>> m = new HashMap<Integer, Map<String, String>>();
 		Map<String,String> a = new HashMap<String, String>();
 		Map<String,String> b = new HashMap<String, String>();
 		Map<String,String> c = new HashMap<String, String>();
 		
 		a.put("name","Alice");
 		a.put("chapter","3");
 		a.put("challenge","1");
 	
 		b.put("name","Bob");
 		b.put("chapter","2");
 		b.put("challenge","2");
 
 		c.put("name","average");
 		c.put("chapter","2.5");
 		c.put("challenge","1.5");
 		
 		m.put(111111, a);
 		m.put(222222, b);
 		m.put(0, c);
 		
 		return m;
 	}
 	
 	@Override
 	public Vector<Integer> getAllUserIDs(){
 		
 		// TODO ITER 2: Add actual database functions.
 		
 		// FAKED OUTPUT
 		Vector<Integer> v = new Vector<Integer>();
 		v.add(111111);
 		v.add(222222);
 			
 		return v;
 	}
 	
 	@Override
 	public ImageIcon getLesson(int Chapter, int Lesson){
 		// TODO ITER 2: Complete image retrieval.
 		return null;
 	}
 	
	@Override
	public ImageIcon getChallenge(int tier, int number){
		// TODO ITER 2: Complete image retrieval.
		return null;
	}
	
 	public void setUserChapter(int UserID, int progress){
 		// TODO ITER 2: Add setter functions.
 	}
 	
 
 	public void setUserChallenge(int UserID, int progress){
 		// TODO ITER 2: Add setter functions.
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
 			Robot.abclEval(fileRead(filepath), null);
 				
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
