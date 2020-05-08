 
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Set;
 
 import edu.berkeley.cs.cs162.BaseUser;
 import edu.berkeley.cs.cs162.ChatLog;
 import edu.berkeley.cs.cs162.ChatServer;
 import edu.berkeley.cs.cs162.Message;
 import edu.berkeley.cs.cs162.User;
 
 public class TestMessageOrder {
 	static ChatServer server;
 	public static void main(String[] args){
 		server = new ChatServer();
 		server.start();
 		basicChatGroupTest();
 		server.shutdown();
 	}
 	
 	/**USE: press enter after receiving message printing stop
 	 * checks whether size of chatlogs are right (everyone received all messages?)
 	 * if yes should print size matches: int
 	 * checks whether chatlogs match with other members in group, printing true if yes
 	 * FOR: only testing 20 people, each with one group, 5 in a group, all in group and no one leaves
 	**/
 	public static void basicChatGroupTest(){
 		int[] groupMessageCounts = new int[4]; //counting messages sent in group
 		String[] groupLogs = new String[4];
 		for(int i = 0; i < 20; i++){ //logs users in and have each user i join group i/5
 			String newUser = "user" + i;
 			server.login(newUser);
 			BaseUser u = server.getUser(newUser);
 			server.joinGroup(u, "group" + i/5);
 		}
 		for(int n = 0; n < 10; n++){ //each user may send to group up to 1k times
 			for(int i = 0; i < 20; i++){
 				if(Math.random() > 0.5){
					server.getUser("user"+i).send("group" + i/5, "lala");
					groupMessageCounts[i/5]++;
 				}
 			}
 		}
 		
 		try {
 			System.in.read();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		//checks if all messages received for all users
 		//then checks if users in same group have same chatlog
 		for(int n = 0; n < 20; n+=5){
 			User toCompareto = (User)server.getUser("user" + n);
 			for(int i = 0; i < 5; i++){
 				int group = (i+n)/5;
 				User u = (User)server.getUser("user" + (i+n));
 				ChatLog log = u.getLog("group" + group);
 				System.out.println(u.getUsername() + ": "); 
 				testCount(log,groupMessageCounts[group]);
 				if(groupLogs[group] == null){
 					groupLogs[group] = log.toString();
 					System.out.println(true);
 				}else{
 					System.out.println(u.getLog("group" + group).isSuperLogOf(toCompareto.getLog("group" + group)));
 					System.out.println(u.getLog("group" + group).toString().equals(toCompareto.getLog("group"+group).toString()));
 				}
 					
 			}
 		}
 	}
 	
 	public static void testCount(ChatLog log, int count){
 		int size = log.getLog().size();
 		if(size == count)
 			System.out.println("size matches: " + count);
 		else
 			System.out.println("size doesn't match: " + count + size);
 	}
 	
 	/**
 	 * consistency = for every group log both a and b have, one log is a 
 	 * subsequence of messages of the other
 	 * @param a
 	 * @param b
 	 */
 	public static boolean testConsistency(User a, User b){
 		Set<String> aLogs = a.getLogs().keySet();
 		Set<String> bLogs = b.getLogs().keySet();
 		for(String s: aLogs){
 			if(bLogs.contains(s)){
 				
 			}
 		}
 		return true;
 	}
 	
 	//Jason putting this in chatlogs
 	private static boolean isOneSubOfOther(LinkedList<Message> a, LinkedList<Message> b){
 		LinkedList<Message> small;
 		LinkedList<Message> big;
 		if(a.size()>b.size()){
 			big = a;
 			small = b;
 		}else{
 			big = b;
 			small = a;
 		}
 		
 		Message next = big.getFirst();
 		
 		
 		return true;
 	}
 
 }
