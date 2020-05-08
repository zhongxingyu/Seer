 package clientRMI;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.server.*;
 
 import server.ClientOperations;
 
 public class RMIClient extends UnicastRemoteObject implements ServerOperations{
 
 	private static final long serialVersionUID = 1L;
 	private static int defaultCredits = 100;
 	
 	public RMIClient() throws RemoteException {
 		super();
 	}
 	
 	public static void main(String args[]) {
 		
 		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
 		String [] stringSplitted = null;
 		String username="",password="";
 		String serverAnswer = "";
 				
 		boolean loggedIn = false;
 		ClientOperations server = null;
 		
 		/*Set to true if you want the program to display debugging messages.*/
 		boolean debugging = false;
 		
 		/* This is for knowing if we are connecting for the first time or instead, we
 		 * are trying to reconnect. It's only use is given a few lines down when we want
 		 * to display a message and so it is not necessary for the correct functioning of
 		 * the program.
 		 */
 		boolean firstConnection = false;
 		
 		/*The variables related to the reconnection.*/
 		int retries = 0; //The numbers of times we reconnected already to a given port.
 		int retrying = 0; //Tests if we are retrying for the first or second time.
 		int WAITING_TIME = 1000; //The time the thread sleeps.
 		int NO_RETRIES = 10; //The maximum amount of retries for a given port.
 		
 		/*The variables related to the server ports available.*/
 		int []serverPorts = new int[2]; //The array with the two different ports.
 		int serverPos = 0; //The position array, which corresponds to active port.
 		int noServerPorts = serverPorts.length; //Total number of possible servers ports.
 		//Places the two ports in the array.
 		serverPorts[0] = 12000;
 		serverPorts[1] = 13000;
 		
 		while (retries < NO_RETRIES){
 			try {			
 				/* We haven't retried yet, so, it's useless to sleep for WAITING_TIME milliseconds. */
 				if (retrying > 0){
 				    try {
 						Thread.sleep(WAITING_TIME);
 					} catch (InterruptedException e) {
 						System.out.println("This thread was interrupted while sleeping.\n");
 						System.exit(0);
 					}
 				}
 
 				RMIClient client = new RMIClient();
 				server = (ClientOperations) Naming.lookup("rmi://localhost:" + serverPorts[serverPos] +"/BetAndUinServer");
 			
 				if (!firstConnection){
 					System.out.println("Connected to server in port " + serverPorts[serverPos] + ".");
 					firstConnection = true;
 				}
 				else{
 					System.out.println("Has successfully reconnected to server, now in port " + serverPorts[serverPos] + ".");
 				}
 				
 				while (!loggedIn){
 					/* The user hasn't made a successful login yet. */
 					if (username.equals("") && password.equals("")){
 		                /* Reads and splits the input. */
 						System.out.println("\nTo log in: login [user] [pass]\n" +
 	        			"To register in: register [user] [pass] [email]");
 						stringSplitted = reader.readLine().split(" ");
 		                	
 		                if(stringSplitted.length == 4 && stringSplitted[0].equals("register")){
 		                	username = stringSplitted[1];
 		                	password = stringSplitted[2];
 		                	String mail = stringSplitted[3];
 		                	
 		                	serverAnswer = server.clientRegister(username, password, mail, (ServerOperations) client);
 		                	
 		                } else if(stringSplitted.length == 3 && stringSplitted[0].equals("login")){
 		                	username = stringSplitted[1];
 		                	password = stringSplitted[2];
 			                
 		                	serverAnswer = (String) server.clientLogin(username, password, client);
 		                }
 		                
 			        	if (!serverAnswer.equals("log successful")){
 			        		username = "";
 			        		password = "";
 			        		
 			        		/* This client isn't registered in the system. */
 			        		if (serverAnswer.equals("log error")){
 			        			System.out.println("\nUsername or password incorrect. Please try again...\n");
 			        		}
 			        		/* This client is logged in in another machine. */
 			        		else if (serverAnswer.equals("log repeated")){
 			        			System.out.println("\nSorry, but this user is already logged in...\n");
 			        		}
 			        		/* This username is already taken by another client. */
 			        		else if (serverAnswer.equals("log taken")){
 			        			System.out.println("\nSorry, but this username isn't available, choose another.\n");
 			        		}
 			        		else if (serverAnswer.equals("username all")){
 			        			System.out.println("Sorry, but the keyword 'all' is reserved, pick another name.");
 			        		}
 			        		else if(serverAnswer.equals("user not registed")){
 			        			System.out.println("Sorry, but you aren't registed yet. Please use command" +
 			        					"register [user] [pass] [mail]");
 			        		}
 			        	} else {
 			        		System.out.println("You're now logged in!");
 			        		/* Shows the main menu. */
 			        		System.out.println(server.clientShowMenu());
 			        		loggedIn = true;
 			        	}
 			        	
 					}
 					/* The client is logged in already. */
 					else{
 						serverAnswer = server.clientLogin(username, password, (ServerOperations) client);
 						
 					}
 				} //while(!loggedIn)
 				
				System.out.println("\n>> ");
				serverAnswer = client.parseFunction(username, reader.readLine(), server, reader);
				System.out.println(serverAnswer);
				
 				retries = 0;
 				retrying = 0;
				loggedIn = false;
 			    
 			/* The list of possible exceptions to be handled. */
 			} catch (NotBoundException e) {
 				if (debugging){
 					System.out.println("NotBoundException:" + e.getMessage());
 				}
 			}catch (RemoteException e) {
 				if (debugging){
 					System.out.println("RemoteException:" + e.getMessage());
 				}
 			    retries++;
 			    /* In this case, it's the first connection, and the server is already down.
 			     * So, we will pass immediately to the next one and retry this one later.
 			     */
 			    if (retries == 1 && retrying == 0){
 			    	serverPos = (++serverPos)%noServerPorts;
 			    	if (debugging){
			    		System.out.println("Trying to connect to server in port " + serverPorts[serverPos] + ".");
 			    	}
 			    }
 			    /* We have retried the connection at least once.
 			     * Consequently, the thread shall wait WAITING_TIME milliseconds 
 			     * when restarts the while cycle.
 			     */
 			    else if (retrying == 0){
 			    	retrying = 1;
 			    }
 			    /* We have completed one round of retries for one server.
 			     * Therefore, we shall try now the second server.
 			     */
 			    else if (retries == 10 && retrying == 1){
 			    	//Resets the number of retries and passes to the serverPort of the other server.
 			    	retries = 0;
 			    	serverPos = (++serverPos)%noServerPorts;
 			    	System.out.println("Trying to connect to server in port " + serverPorts[serverPos] + ".");
 			    	retrying++;
 			    }
 			} catch (IOException e) {
 				if (debugging){
 					System.out.println("IOException:" + e.getMessage());
 				}
 			}
 		}
 		
 		//TODO: Perguntar porque  que sem isto o programa no termina.
 		System.out.println("Exited");
 		System.exit(0);
 	}
 
 	public void printUserMessage(String msg) throws java.rmi.RemoteException{
 		System.out.println(msg);
 	}
 	
     public String parseFunction(String user, String input, ClientOperations server, BufferedReader reader) throws RemoteException{
     	/* The answer from the server to the client. */
     	String answer = "";
     	String [] stringSplitted = null;
     	
     	/* Splits the input. */
     	try{
     		stringSplitted = input.split(" ");
     	} catch(Exception e){
     		return "";
     	}
         
         /* The client has sent two keywords and the first is "show".*/
         if(stringSplitted.length == 2 && stringSplitted[0].equals("show")){        	
         	if(stringSplitted[1].equals("matches")){ //show all current matches
         		answer = server.clientShowMatches();
         	}
         	else if(stringSplitted[1].equals("credits")){ //show user's credits
         		answer = server.clientShowCredits(user);
         	}
         	else if(stringSplitted[1].equals("users")){ //show all active users
         		answer = server.clientShowUsers();
         	}
         	else if(stringSplitted[1].equals("menu")){//show the main menu.
         		answer = server.clientShowMenu();
         	}
         	else{
         		answer = "Unknow Command";
         	}
         }
         else if(stringSplitted.length >= 3 && stringSplitted[0].equals("send")){
         	if(stringSplitted[1].equals("all")){ //send a message to all users
         		/* We have to use again the input because the user may send the message with
         		 * white spaces.
         		 * 'send all ' has 9 characters. So, the message is from there till the end of
         		 * input string.
         		 */
         		answer = server.clientSendMsgAll(user, input.substring(9));
         	}
         	else if (stringSplitted[1].equals(user)){
         		answer = "What's the point of sending messages to yourself?";
         	}
         	/* We are sending a message to a user. */
         	else{
         		/* 'send ' has 5 characters. Then, we have to sum the size of the receiver name
         		 * as well as a white space that separates this name from the message.
         		 */
         		answer = server.clientSendMsgUser(user, stringSplitted[1],input.substring(6 + stringSplitted[1].length()));
         	}
         }
         else if(stringSplitted.length == 4 && stringSplitted[0].equals("bet")){
         	/* Variables to save the values inserted by the client. */
         	String resultBet;
         	int gameNumber,credits;
         	
         	try {
         		gameNumber = Integer.parseInt(stringSplitted[1]);
         		resultBet = stringSplitted[2];
         	    credits = Integer.parseInt(stringSplitted[3]);
         	    
         	} catch(NumberFormatException nFE) {
         		/* The user hasn't inserted a number for one of the required arguments. */
         		System.out.println("Not an integer.");
         		
         		answer = "Invalid game number or amount of credits!";
         	    return answer;
         	}
         	
         	/* The user has made a bet of 0 credits. */
         	if (credits == 0){
         		return "Are you kidding?! You have bet no credits!";
         	}
         	
         	answer = server.clientMakeBet(user,gameNumber, resultBet, credits);
         }
         else if(stringSplitted.length > 0 && stringSplitted[0].equals("bet")){
         	answer =  "Wrong number of arguments: bet [game number] [bet] [credits]";
         }
         else if (input.equals("reset")){
         	/* First, we verify if the client has more or less than the default value of credits,
         	 * make sure he/she won't lose credits accidentally.
         	 */
     		int userCredits = Integer.parseInt(server.clientShowCredits(user));
 
     		if (userCredits > defaultCredits){
     			String finalAnswer = "";
     			System.out.printf("In this moment, you have %d, which means you are going to lose %d credits.\n" +
     					"Are you sure you want to continue with the process (Y/N)?\n", userCredits, userCredits - defaultCredits);
     			do{
     				try{
     					finalAnswer = reader.readLine().toUpperCase();
     				}catch (Exception e){
     					return "";
     				}
     			}
     			while (!finalAnswer.equals("Y") && !finalAnswer.equals("N"));
     			
     			if (finalAnswer.equals("Y")){
     				answer = server.clientResetCredits(user);
     			}
     			else{
     				answer = "Operation cancelled. You still have " + userCredits+ " credits.\n";
     			}
         	}
     		else{
     			answer = server.clientResetCredits(user);
     		}
         }
         else if(stringSplitted.length == 1 && stringSplitted[0].equals("exit")){
         	server.clientLeave(user);
         	System.out.println("Thank you for using the BetAndUin service!\n"
     				+ "Have a nice day!");
     		System.exit(0);
         }
         else {
         	answer = "Unknown command";
         }
 
 		return answer;
     }
     
     @Override
     public boolean testUser() throws java.rmi.RemoteException{
     	return true;
     }
 }
