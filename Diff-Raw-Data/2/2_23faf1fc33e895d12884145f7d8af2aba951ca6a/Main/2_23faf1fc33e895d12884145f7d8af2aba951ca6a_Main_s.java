 /**
  * Main file to start the JACK CLIMA application
  * 
  * This file basically creates all the agents involved in the application
  * 
  * Authors: Sebastian Sardina and Dave Scerri
  * Date: May 2006 - June 2008
  * 
  * Run the application: 
  * 	We run Main with each agent's name as an argument and the corresponding
  * 	system properties for each agent to connect to the game server:
  * 
  *		java <sys properties> Main <agent name 1> .... <agent name n> 	(to create n agents)
  * 
  * where <sys properties> should include the system properties for each agent with the
  * corresponding information (login/password) to connect to the game server and 
  * the address of the game server itself, namely:
  * 
  *		clima.host=<game server hostname or ip>			
  *		clima.port=<game server port number>
  *		clima.agent.<name player>.username=<login name>	// for each agent 
  *		clima.agent.<name player>.password=<password>	// for each agent
  *
  *
  * So, a full example to create 6 agents named "playeri" would be as follows:
  *
  * 	java -Xmx90m -Dclima.host=localhost -Dclima.port=12300 
  * 		-Dclima.agent.player1.username=participant1 -Dclima.agent.player1.password=1 
  * 		-Dclima.agent.player2.username=participant2 -Dclima.agent.player2.password=2 
  * 		-Dclima.agent.player3.username=participant3 -Dclima.agent.player3.password=3 
  * 		-Dclima.agent.player4.username=participant4 -Dclima.agent.player4.password=4  
  * 		-Dclima.agent.player5.username=participant5 -Dclima.agent.player5.password=5 
  * 		-Dclima.agent.player6.username=participant6 -Dclima.agent.player6.password=6 
  * 			Main player1 player2 player3 player4 player5 player6
  *  
  *  To run the application the following jar/class files should be set:
  *  	jack.jar : thea actual JACK kernel
  *  	climacomms.jar:	implementing the ClimaTalking capability used to do low-level communication with game server
  *  	grid.jar : library tool to manipulate grids
  */
 import rmit.ai.clima.jackagt.agents.*;
 
 public class Main {
 
 	// Set application options (should be moved to command line options)
 	public static int maxAgents = 6;	// How many total player agents should we support? 
     public static int stepsPerSave = 5; // How many steps before saving to disk
 
     // Define possible optional properties to be given as -D<prop name>=<value>
     public final static String DEBUGON = "debug";
     public final static String GUION = "gui";
     public final static String SAVESTEPS = "stepsPerSave";
 
     
     
     public static void main (String args[]) {
     	aos.jack.Kernel.init( args );	// Initialize the JACK kernel
 
     	// Get the optional properties
     		// This is whether ClimaTalking should be allowed to print-out its debug messages
     	boolean debugOnClimaTalking = Boolean.parseBoolean(	 // debug=<value> property
         		System.getProperty( DEBUGON, "false" ) );
     	boolean showGUI = Boolean.parseBoolean(	 // gui=<value> property
         		System.getProperty( GUION, "true" ) );
     	int stepsPerSave = Integer.parseInt(	 // stepsPerSave=<value> property
         		System.getProperty( SAVESTEPS, "5" ) );
     	
         // Next create coordinator, named "boss"
         new Coordinator("boss");	
         
         // Finally, create the GUI agent named "gui"
         new GUIAgent("gui", false, showGUI, maxAgents, stepsPerSave);
 
     	
     	//  Create each agent. The agent name is passed to the constructor
         Player players[] = new Player[args.length];
         for(int i=1; i <= args.length; i++) {
             players[i-1] = new Player( args[i-1], debugOnClimaTalking);
         }
         
     }
         
 }
