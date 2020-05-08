 import java.util.ArrayList;
 import java.util.Scanner;
 
 /**
 * <b>ConsoleThread Class</b>
 * <p>
 * Represents the console thread on the server that the server administrator
 * can interact with.
 * As of version 1.0, only single phrase (no spaces or arguments/flags yet) commands
 * are implemented.
 * Type 'help' to see all the commands and short descriptions.
 * @author James Wen - jrw2175
 */
 public class ConsoleThread extends Thread{
 	private Server server;
 	private String currentCommand;
 	private Scanner consoleScanner;
 	private String[] commandList;
 	
 	/**
 	* <b>ConsoleThread constructor</b>
 	* <p>
 	* Constructs a console thread.
 	* @param server - the server that the console operates on
 	*/
 	public ConsoleThread(Server server){
 		this.server = server;
 		//List of currently implemented commands
 		this.commandList = new String[]{"help", "broadcast", "blocked_ips", 
 			"unblock_all", "accounts", "view_blocktime","change_blocktime",
 			"view_number_login_attempts", "change_number_login_attempts",
 			"number_threads", "current_ips","last_hr", "view_all_messages", 
			"version", "exit"};
 	}
 	
 	/**
 	* <b>run</b>
 	* <p>
 	* Runs the console thread and greets the server admin and starts the prompt.
 	*/
 	public void run(){
 		consoleScanner = new Scanner(System.in);
 		consolePrint("Welcome to SimpleServer v1.0! Enter 'help' for a list of commands.");
 		consolePrint("Please also remember to shut the logger ('shut_logger') before you shut down the server!");
 		prompt();
 	}
 	
 	/**
 	* <b>prompt</b>
 	* <p>
 	* Presents a "> " prompt to the user and takes in commands.
 	* Unsuccessfully executed or non-implemented commands will output a brief
 	* error notice and reprompt.
 	*/
 	private void prompt(){
 		System.out.print("> ");
 		currentCommand = consoleScanner.nextLine();
 		if(!properCommand(currentCommand.trim())){
 			System.out.println("Not a recognized command.");
 		}
 		else if(!completeCommand(currentCommand.trim())){
 			System.out.println("That command cannot be completed at this time.");
 		}
 		prompt();
 	}
 	
 	/**
 	* <b>properCommand</b>
 	* <p>
 	* Returns whether the inputed command is implemented by the console.
 	* @param query - command
 	* @return commandCorrect - whether the command is implemented
 	*/
 	private boolean properCommand(String query){
 		boolean commandCorrect = false;
 		for(String command : commandList){
 			if(query.equals(command)){
 				commandCorrect = true;
 			}
 		}
 		return commandCorrect;		
 	}
 	
 	/**
 	* <b>completeCommand</b>
 	* <p>
 	* Carries out a command and returns whether the command is successfully 
 	* completed.
 	* @param command - the command to be completed
 	* @return commandComplete - whether the command was successfully carried out
 	*/
 	private boolean completeCommand(String command){
 		boolean commandComplete = true;
 		try{
 			if(command.equals("help")){
 				help();
 			}
 			else if(command.equals("broadcast")){
 				broadcast();
 			}
 			else if(command.equals("blocked_ips")){
 				viewBlockedIPs();
 			}
 			else if(command.equals("unblock_all")){
 				unblockAllIPs();
 			}
 			else if(command.equals("accounts")){
 				viewAccounts();
 			}
 			else if(command.equals("view_blocktime")){
 				viewBlockTime();
 			}
 			else if(command.equals("change_blocktime")){
 				changeBlockTime();
 			}
 			else if(command.equals("view_number_login_attempts")){
 				viewNumberLoginAttempts();
 			}
 			else if(command.equals("change_number_login_attempts")){
 				changeNumberLoginAttempts();
 			}
 			else if(command.equals("number_threads")){
 				numberThreads();
 			}
 			else if(command.equals("current_ips")){
 				viewCurrentIPs();
 			}
 			else if(command.equals("last_hr")){
 				viewLastHr();
 			}
 			else if(command.equals("view_all_messages")){
 				viewAllMessages();
 			}
 			else if(command.equals("version")){
 				version();
 			}
 			else if(command.equals("shut_logger")){
 				shutLogger();
 			}
 		}
 		catch (Exception e){
 			commandComplete = false;
 		}
 		return commandComplete;
 	}
 	
 	/**
 	* <b>help</b>
 	* <p>
 	* Shows the available commands and short descriptions.
 	*/
 	private void help(){
 		consolePrint("help");
 		consolePrint("- View all commands and what they do.");
 		consolePrint("broadcast");
 		consolePrint("- Broadcast a message to all currently active clients.");
 		consolePrint("blocked_ips");
 		consolePrint("- View all currently blocked IPs.");
 		consolePrint("unblock_all");
 		consolePrint("- Unblock all currently blocked IPs.");
 		consolePrint("accounts");
 		consolePrint("- View all usernames that clients can login to this server with.");
 		consolePrint("view_blocktime");
 		consolePrint("- View how long IPs are blocked for after " + server.getNumLoginAttempts() + " attempts");
 		consolePrint("change_blocktime");
 		consolePrint("- Change how long IPs are blocked for after " + server.getNumLoginAttempts() + " attempts");
 		consolePrint("view_number_login_attempts");
 		consolePrint("- View how many failed login attempts are allowed before blocking IP for " + server.viewBlocktime() + " seconds");
 		consolePrint("change_number_login_attempts");
 		consolePrint("- Change how many failed login attempts are allowed before blocking IP for " + server.viewBlocktime() + " seconds");
 		consolePrint("number_threads");
 		consolePrint("- View the current number of threads running on this server.");
 		consolePrint("current_ips");
 		consolePrint("- View all currently connected IPs.");
 		consolePrint("last_hr");
 		consolePrint("- View the usernames used by clients who connected within the last hour.");
 		consolePrint("view_all_messages");
 		consolePrint("- View all the messages on this server that have been sent by users.");
 		consolePrint("version");
 		consolePrint("- View the version of this server.");
 		consolePrint("shut_logger");
 		consolePrint("- Safely close the logging stream.");
 	}
 	
 	/**
 	* <b>broadcast</b>
 	* <p>
 	* Broadcast a message to all currently active clients.
 	*/
 	private void broadcast(){
 		consolePrint("Please enter in the broadcast message you wish to send.");
 		server.broadcast(consoleScanner.nextLine(), "Server Admin");
 		consolePrint("Your broadcast was sent.");
 	}
 	/**
 	* <b>viewBlockedIPs</b>
 	* <p>
 	* View all currently blocked IPs.
 	*/
 	private void viewBlockedIPs(){
 		ArrayList<BlockedIP> blockedIPs = server.getBlockedIPs();
 		for(BlockedIP ip : blockedIPs){
 			consolePrint(ip.getIP().toString());
 		}
 	}
 	/**
 	* <b>unblockAllIPs</b>
 	* <p>
 	* Unblock all currently blocked IPs.
 	*/
 	private void unblockAllIPs(){
 		server.removeAllBlockedIPs();
 		consolePrint("All IPs are now unblocked.");
 	}
 	/**
 	* <b>viewAccounts</b>
 	* <p>
 	* View all usernames that clients can login to this server with.
 	*/
 	private void viewAccounts(){
 		ArrayList<Account> accounts = server.getAccounts();
 		for(Account account : accounts){
 			consolePrint(account.getUserName());
 		}
 	}	
 	/**
 	* <b>viewNumberLoginAttempts</b>
 	* <p>
 	* View how many failed login attempts are allowed before blocking IP for a set time.
 	*/
 	private void viewNumberLoginAttempts(){
 		consolePrint(Integer.toString(server.getNumLoginAttempts()) + " attempts allowed");
 	}
 	/**
 	* <b>changeNumberLoginAttempts</b>
 	* <p>
 	* Change how many failed login attempts are allowed before blocking IP for a set time.
 	*/
 	private void changeNumberLoginAttempts(){
 		consolePrint("What is the new number of allowed login attempts?");
 		int newNumber = Integer.parseInt(consoleScanner.nextLine());
 		System.out.print("    > ");
 		server.changeNumLoginAttempts(newNumber);
 		consolePrint("The number of login attempts allowed is now " + newNumber);
 	}
 	/**
 	* <b>viewBlockTime</b>
 	* <p>
 	* View how long IPs are blocked for after too many login attempts.
 	*/
 	private void viewBlockTime(){
 		consolePrint(Integer.toString(server.viewBlocktime()) + " seconds");
 	}
 	/**
 	* <b>changeBlockTime</b>
 	* <p>
 	* Change how long IPs are blocked for after too many login attempts.
 	*/
 	private void changeBlockTime(){
 		consolePrint("What is the new block time in seconds?");
 		int newBlockTime = Integer.parseInt(consoleScanner.nextLine());
 		System.out.print("    > ");
 		server.changeBlockTime(newBlockTime);
 		consolePrint("The block time is now " + newBlockTime + " seconds");
 	}
 	/**
 	* <b>numberThreads</b>
 	* <p>
 	* View the current number of threads running on this server.
 	*/
 	private void numberThreads(){
 		consolePrint(Thread.activeCount() + " threads currently running");
 	}
 	/**
 	* <b>viewCurrentIPs</b>
 	* <p>
 	* View all currently connected IPs.
 	*/
 	private void viewCurrentIPs(){
 		ArrayList<ServerThread> conns = server.getCurrentClients();
 		for(ServerThread client : conns){
 			consolePrint(client.getSocket().getInetAddress().toString());
 		}
 	}	
 	/**
 	* <b>viewLastHr</b>
 	* <p>
 	* View the usernames used by clients who connected within the last hour.
 	*/
 	private void viewLastHr(){
 		ArrayList<String> usersLastHr = server.getUsersLastHr();
 		for(String user : usersLastHr){
 			consolePrint(user);
 		}
 	}
 	/**
 	* <b>viewAllMessages</b>
 	* <p>
 	* Runs the console thread and greets the user and starts the prompt.
 	*/
 	private void viewAllMessages(){
 		ArrayList<Message> allMessages = server.getAllMessages();
 		for(Message message : allMessages ){
 			consolePrint("Message ID: " + message.getID());
 			consolePrint("Sent at " + message.getTimeSent() + " from user " +
 					     message.getSender().getUserName() + " to user " + 
 					     message.getRecipient().getUserName());
 			consolePrint("    Subject: " + message.getSubject());
 			consolePrint("    Content: " + message.getBody());
 		}
 	}
 	/**
 	* <b>version</b>
 	* <p>
 	* View the version of this server.
 	*/
 	private void version(){
 		consolePrint("Simple Server version is 1.0");
 	}
 	/**
 	* <b>shutLogger</b>
 	* <p>
 	* Safely close the logging stream.
 	*/
 	private void shutLogger(){
 		consolePrint("Logger was safely closed");
 		server.closeLogger();
 		consolePrint("You may now close the server");
 	}
 	/**
 	* <b>consolePrint</b>
 	* <p>
 	* Print to the console.
 	* @param print - String to print
 	*/
 	private void consolePrint(String print){
 		System.out.println("    " + print);
 	}
 }
