 package danger_zone;
 import java.io.*;
 import java.util.ArrayList;
 
 /**
 *@author Ethan Eldridge <ejayeldridge @ gmail.com>
 *@version 0.0
 *@since 2012-11-05
 *
 * The driver program for all the Java classes. This class will be ran once from the command line with some arguments and will
 * handle all incoming requests to the Java side of the server.
 * 
 * 
 */
 public class JavaDriver{
 
 	DangerControl dcUDP;
 
 	/**
 	*Debug variable, if specified as true, output messages will be displayed. 
 	*/
 	static boolean debugOn = false;
 
 	/**
 	*The variable to control how long the DangerControlUDP instance will listen for. If continous running isn't specified
 	*/
 	static int timeout = 30;
 
 	/**
 	*Variable to determine if the DangerControlUDP should continue running until it recieves a Kill Command.
 	*/
 	static boolean continous = false;
 
 	static boolean usingUDP = false;
 
 	public JavaDriver(){
 		
 	}
 
 	/**
 	*Initializes the Naive Bayers Runner and UDP Danger Control. 
 	*Let the danger control class run both the tree and the bayes trainer.
 	*The danger control should take commands involving both geo neighbors and for 
 	*bayes to be trained via command line.
 	*/
 	public void run(String password, String username){
 
 		//Initalize Control
 		System.out.print("Creating Danger Control Process");
 		try{ 
 			if(usingUDP){
 				System.out.println(" using UDP");
 				dcUDP = new DangerControlUDP();
 			}else{
 				System.out.println(" using TCP");
 				dcUDP = new DangerControlTCP();
 			}
 		}catch(Exception e){
 			System.out.println("Could not create the control structure for client server interaction. Printing Trace:");
 			System.out.println(e.getStackTrace());
 			System.out.println("ERROR MESSAGE: " + e.getMessage());
 			System.out.println("Aborting setup");
 			return;
 		}
 		//Create the tree for neighbors
 		System.out.println("Creating Danger Zone Tree");
 		try{
 			ArrayList<DangerNode> nodes =  DangerNode.fetchDangers(username,password);
 			dcUDP.setRootNode(DangerNode.makeTree(nodes));
 			dcUDP.dangerZones.printTree();
 			System.out.println("Tree created with " + nodes.size() + " nodes and root node of " + dcUDP.dangerZones);
 		}catch(Exception e){
 			System.out.println("Could not create K-D Tree from the database. Printing Trace:");
 			System.out.println(e.getStackTrace());
 			System.out.println("ERROR MESSAGE: " + e.getMessage());
 			System.out.println("Aborting setup");
			for(StackTraceElement element : e.getStackTrace()){
				System.out.println("Trace: " + element.toString());
			}
 			return;	
 		}
 		//Initial training of the naive bayes clasifier
 		System.out.println("Creating Clasifier");
 		dcUDP.trainBayes(password,debugOn);
 
 		//Run the dcUDP and let it handle all incoming events and such.
 		System.out.println("Running the Java Server Component");
 		try{ 
 			if(JavaDriver.continous){
 				System.out.println("DEBUG: " + dcUDP.dangerZones);
 				dcUDP.run(JavaDriver.continous);
 			}else{
 				DangerControl.int_timeout = timeout;
 				DangerControlUDP.int_timeout = timeout;
 				dcUDP.run();
 			}
 		}catch(Exception e){
 			System.out.println("ERROR");
 			System.out.println(e.getMessage());
 			System.out.println(e.getStackTrace());
 			return;
 		}
 	}
 
 
 	public static void main(String[] args) {
 		//Arguments:
 		// -p passwordbetweenqoutes
 		// -n portnumber
 		// Must have spaces between arguments 
 		String password = "";
 		String username = "";
 		int portnumber = DangerControlUDP.port_number;
 		if(!usingUDP){
 			portnumber = DangerControlTCP.port_number;
 		}
 
 		//Parse out the arguments from the command line
 		for(int i = 0; i < args.length; i++){
 			if(args[i].trim().equals("-p")){
 				//Get the password
 				try{
 					password = args[i+1];	
 				}catch(java.lang.IndexOutOfBoundsException out){
 					System.out.println("Password expected if -p specifier is given.");
 					return;
 				}
 			}else if(args[i].trim().equals("-n")){
 				//Get the port number
 				try{
 					portnumber = Integer.parseInt(args[i+1]);
 				}catch(java.lang.IndexOutOfBoundsException out){
 					System.out.println("Port number expected if -n specifier is given.");
 					return;
 				}catch(java.lang.NumberFormatException nfe){
 					System.out.println("Must specify an integer value for the port number");
 					return;
 				}
 			}else if(args[i].trim().equals("-u")){
 				//Get the user for the database
 				try{
 					username = args[i+1];
 				}catch(java.lang.IndexOutOfBoundsException out){
 					System.out.println("Username expected if -u specifier is given");
 					return;
 				}
 			}else if(args[i].trim().equals("-d")){
 				try{
 					int debug = Integer.parseInt(args[i+1]);
 					switch (debug) {
 						case 1:
 							JavaDriver.debugOn = true;
 							break;
 						default:
 							JavaDriver.debugOn = false;
 							break;
 					}
 				}catch(java.lang.IndexOutOfBoundsException out){
 					System.out.println("[1|0] expected if -d specifier is used");
 					return;
 				}catch(java.lang.NumberFormatException nm){
 					System.out.println("Could not parse entered arguments, please enter 0 or 1 for -d argument");
 					return;
 				}
 			}else if(args[i].trim().equals("-c")){
 				try{
 					switch (Integer.parseInt(args[i+1])) {
 						case 1:
 							DangerControlTCP.continous = true;
 							DangerControlUDP.continous = true;
 							JavaDriver.continous = true;
 							break;
 						default:
 							DangerControlUDP.continous = false;
 							DangerControlTCP.continous = false;
 							break;
 					}
 
 				}catch(java.lang.IndexOutOfBoundsException out){
 					System.out.println("[1|0] expected if -c specifier is used");
 					return;
 				}catch(java.lang.NumberFormatException nm){
 					System.out.println("Could not parse entered arguments, please enter 0 or 1 for -d argument");
 					return;
 				}
 
 			}else if (args[i].trim().equals("-proc")) {
 				try{
 					if(args[i+1].trim().equals("T")){
 						usingUDP = false;
 					}else{
 						usingUDP = true;
 					}
 				}catch(java.lang.IndexOutOfBoundsException out){
 					System.out.println("Protocol expected if -proc specifier given");
 				}
 
 			}else if(args[i].trim().equals("-help")){
 				//Print out the help for this.
 				System.out.println("usage: java JavaDriver -p password -u username [-n integer port number][-d [1|0] debug messages on or off][-c [1|0] run the server without a timeout (1) or just by time out 0][-proc [U|T]] ");
 				return;
 			}
 		}
 
 		//Make sure we have the least amount of arguments we need:
 		if(password.equals("") || username.equals("")){
 			System.out.println("Required arguments user and password. Please see the help for this program.");
 		}
 
 		//Set the port number
 		DangerControlTCP.port_number = portnumber;
 		DangerControlUDP.port_number = portnumber;
 
 		//Construct the Driver given the arguments and run the program
 		JavaDriver jd = new JavaDriver();
 		jd.run(password,username);
 	}
 }
