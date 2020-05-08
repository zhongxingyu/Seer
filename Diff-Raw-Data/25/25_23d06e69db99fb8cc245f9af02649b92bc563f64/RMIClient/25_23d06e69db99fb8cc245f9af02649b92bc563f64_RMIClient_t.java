 package clientRMI;
 
 import server.ClientOperations;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.server.UnicastRemoteObject;
 
 
 public class RMIClient extends UnicastRemoteObject implements ServerOperations{
 
 	private static final long serialVersionUID = 1L;
 
 	protected RMIClient() throws RemoteException {
 		super();
 	}
 	
 	public static void main(String args[]) {
 		try {
 			RMIClient client = new RMIClient();
 			client.run();
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	public void run() {
 		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));;
 		String [] stringSplitted = null;
 		String username="",password="";
 		String serverAnswer = "";
				
 		boolean loggedIn = false;
 		ClientOperations server = null;
 		RMIClient client = null;
 		
 		try {
 			server = (ClientOperations) LocateRegistry.getRegistry(12000).lookup("BetAndUinServer");
 		} catch (Exception e) {
 			System.out.println("Exception in main: " + e);
 			e.printStackTrace();
 		}
 		
 		while(true){

 			while (!loggedIn){
 				/* The user hasn't made a successful login yet. */
 				if (username.equals("") && password.equals("")){
 	                /* Reads and splits the input. */
 	                try {
	    	        	System.out.println("\nTo log in: login [user] [pass]\n" +
	        			"To register in: register [user] [pass] [email]");
	    	        	
 						stringSplitted = reader.readLine().split(" ");
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 	                	
 	                if(stringSplitted.length == 4 && stringSplitted[0].equals("register")){
 	                	username = stringSplitted[1];
 	                	password = stringSplitted[2];
 	                	String mail = stringSplitted[3];
 	                	
 	                	try {
 							serverAnswer = server.clientRegister(username, password, mail, (RMIClient) this);
 						} catch (RemoteException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 	                	
 	                } else if(stringSplitted.length == 3 && stringSplitted[0].equals("login")){
 	                	username = stringSplitted[1];
 	                	password = stringSplitted[2];
 		                
 	                	try {
 							serverAnswer = server.clientLogin(username, password, (RMIClient) this);
 						} catch (RemoteException e) {
							System.out.println("remote login function");
 							e.printStackTrace();
 						}
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
 		        	} else {
 		        		System.out.println("You're now logged in!");
 		        		loggedIn = true;
 		        	}
 		        	
 				}
 				/* The client is logged in already. */
 				else{
                 	try {
 						serverAnswer = server.clientLogin(username, password, (RMIClient) client);
 					} catch (RemoteException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			} //while(!loggedIn)
 			
 			try {
 				serverAnswer = parseFunction(username, reader.readLine(), server);
 				System.out.println(serverAnswer);
 			} catch (RemoteException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} //while(true)
 	}
 
 	public String printUserMessage(String msg) {
 		return msg;
 	}
 	
     public String parseFunction(String user, String input, ClientOperations server) throws RemoteException{
     	/* The answer from the server to the client. */
     	String answer = "";
     	
     	/* Splits the input. */
     	String [] stringSplitted = input.split(" ");
         
         /* The client has sent two keywords and the first is "show".*/
         if(stringSplitted.length == 2 && stringSplitted[0].equals("show")){        	
         	if(stringSplitted[1].equals("matches")){ //show all current matches
         		answer = server.clientShowMatches();
         	}
         	else if(stringSplitted[1].equals("credits")){ //show user's credits
         		answer = server.clientResetCredits(user);
         	}
         	else if(stringSplitted[1].equals("users")){ //show all active users
         		answer = server.clientShowUsers();
         	}
         	else{
         		answer = "Unknow Command";
         	}
         }
         else if(stringSplitted.length == 3 && stringSplitted[0].equals("send")){
         	if(stringSplitted[1].equals("all")){ //send a message to all users
         		answer = server.clientSendMsgAll(user, stringSplitted[2]);
         	}
         	/* We are sending a message to a user. */
         	else{
         		answer = server.clientSendMsgUser(user, stringSplitted[1], stringSplitted[2]);
         	}
         }
         else if(input.equals("reset")){ //resets user's credits to 100Cr
         	answer = server.clientResetCredits(user);
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
         	
         	server.clientMakeBet(user,gameNumber, resultBet, credits);
         }
         else if(stringSplitted.length > 0 && stringSplitted[0].equals("bet")){
         	answer =  "Wrong number of arguments: bet [game number] [bet] [credits]";
         }
         else {
         	answer = "Unknown command";
         }
 
 		return answer;
     }
 }
