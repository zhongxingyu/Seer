 package facebook;
 
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.HashSet;
 
 import nodes.DistNode;
 
 
 import transactions.Command;
 import transactions.Transaction;
 
 public abstract class FacebookOperation {
 	protected Queue<String> cmds;
 	protected HashSet<User> users;
 	protected User user;
 	protected DistNode n;
 	
 	protected int commandId;
 	
 	public FacebookOperation(String[] commands, DistNode n, User u){
 		this.n = n;
 		this.user = u;
 		this.cmds = new LinkedList<String>();
 		for(String s : commands)
 			this.cmds.add(s);
 	}
 	
 	public int getCommandId() {
 		return this.commandId;
 	}
 	
 	public String nextCommand() {
 		return this.cmds.poll();
 	}
 	
 	public abstract void onCommandFinish(Command c);
 	public abstract void onAbort(Transaction txn);
 	public abstract void onCommit(Transaction txn);
 	public abstract void onStart(int txId);
 	
 	public static HashSet<User> loadUsers( String userString ) {
 		HashSet<User> userSet = new HashSet<User>();
 		
 		String[] allUsers = userString.split("\n");
 		for( String userPass : allUsers ) {
 			String[] user = userPass.split(" ");
 			userSet.add( new User(user[0], user[1]));
 		}
 		
 		return userSet;
 	}
 	
 	public void notLoggedIn() {
 		this.printError("You are not logged in.");
 		this.n.notLoggedIn();
 	}
 	
 	public void printError( String error ) {
 		System.out.println("ERROR: " + error );
 	}
 	
 	public static boolean isUserLoggedIn( User u, int nodeId, String logString ) {
 		String[] loggedUsers = logString.split("\n");
 		for( String userAddr : loggedUsers ) {
 			String[] tokens = userAddr.split(" ");
 			if( tokens[0].equals( u.getUsername() ) && tokens[1].equals( nodeId ) ) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public static boolean doesUserExist( User u, String existsString ) {
 		for( String line : existsString.split("\n") ) {
 			String[] toks = line.split(" ");
 			if( u.getUsername().equals(toks[0]) && ( u.getPassword() == null || u.getPassword().equals(toks[1]))) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	public static String replaceField( String command, String fieldName, String replacement ) {
		return command.replace("[" + fieldName + "]", replacement);
 	}
 	
 	public static String boxify(User u, String contents){
 		String[] lines = contents.split("\n");
 		int width = u.getUsername().length() + 6;
 		
 		for(String line : lines)
 			if(line.length() > width)
 				width = line.length();
 		
 		String bar = repeat("*", width + 4);
 		String rtn = bar + "\n";
 		rtn += "* From: " + u.getUsername() + repeat(" ", width - u.getUsername().length() - 6) + " *\n";
 		
 		for(String line : lines)
 			rtn += "* " + line + repeat(" ", width - line.length()) + " *\n";
 		
 		return rtn + bar;
 	}
 	
 	private static String repeat(String str, int count){
 		String rtn = "";
 		for(int i = 0; i < count; i++)
 			rtn += str;
 		return rtn;
 	}
 
 	
 }
