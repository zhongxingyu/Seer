 package mel.fencing.server;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.util.ArrayList;
 import java.util.HashMap;
 import mel.security.AccountManager;
 
 public class Server
 {
     public static final int PORT = 9738;
     static ArrayList<UserSession> sessions = new ArrayList<UserSession>();
     static HashMap<String,UserSession> name2session = new HashMap<String,UserSession>();
     public static final AccountManager accMan = new AccountManager(AccountManager.defaultDir);
 
     public static void main(String[] args)
     {
         try
         {
             ServerSocket server = new ServerSocket(PORT);
             while (true)
             {
                 UserSession s = UserSession.makeSession(server.accept());
                 if (s != null) addSession(s);
             }
         }
         catch (IOException e)
         {
             System.err.println(e.getMessage());
         }
     }
     
     public static synchronized void addSession(UserSession userSession)
     {
         sessions.add(userSession);
     }
     
     public static synchronized void register(String username, UserSession userSession)
     {
         name2session.put(username,userSession);
     }
 
     public static synchronized void purge(UserSession userSession)
     {
         String name = userSession.getName();
         if(name != null) name2session.remove(name);
         sessions.remove(userSession);
     }
 
     /**
      * Sends user <code>target</code> a challenge from user <code>challenger</code>
      * @param challenger session of the challenger
      * @param target user to send the challenge to
      */
     public static void challengeTarget(UserSession challenger, String target)
     {
         challenger.send("Challenge submitted against: "+target);
     }
 
     /**
      * Enters user <code>name</code> into the open challenge list for random match assignment.
      * @param challenger session of the user to be entered into the open challenge list
      */
     public static void challengeOpen(UserSession challenger)
     {
         challenger.send("Open challenge submitted.");
     }
 
 }
