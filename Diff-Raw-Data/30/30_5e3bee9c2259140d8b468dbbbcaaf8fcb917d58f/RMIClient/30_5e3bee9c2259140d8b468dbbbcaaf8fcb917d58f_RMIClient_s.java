 package clientRMI;
 
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.Serializable;
 import java.rmi.Naming;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 //import java.rmi.server.UnicastRemoteObject;
 import java.rmi.server.*;
 
 import server.ClientOperations;
 
 
 //TODO: O do send all tambm d problemas.
 
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
 		
 		while(true) {
 			try{
 				RMIClient client = new RMIClient();
 				server = (ClientOperations) Naming.lookup("rmi://localhost:12000/BetAndUinServer");
				
				//TODO: caso haja erro no comando nao o estamos a distinguir
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
 				
 			} catch (Exception e) {
 				//TODO Auto-gen exception
 				e.printStackTrace(); 
 			}
 		} //while(true)
 
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
