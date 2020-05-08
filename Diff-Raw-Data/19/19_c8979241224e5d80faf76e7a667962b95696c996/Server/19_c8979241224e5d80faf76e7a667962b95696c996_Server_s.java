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
     static Lobby lobby = new Lobby();
     public static AccountManager accMan;
 
     public static void main(String[] args)
     {
         String dir = AccountManager.defaultDir;
         if(args.length > 0) dir = args[0];
         accMan = new AccountManager(dir);
         
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
         UserSession oldSession = name2session.get(username);
        if(oldSession != null) purge(oldSession);
         name2session.put(username,userSession);
     }
 
     public static synchronized void purge(UserSession userSession)
     {
         String name = userSession.getUsername();
         if(name != null) name2session.remove(name);
         lobby.cancel(userSession);
         sessions.remove(userSession);
     }
 
 }
