 package mel.fencing.server;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.util.ArrayList;
 import mel.security.AccountManager;
 
 public class Server
 {
     public static final int PORT = 9738;
     static ArrayList<UserSession> sessions = new ArrayList<UserSession>();
    public static final String dir = "C:/Users/Public/Desktop/fileStore";
    public static final AccountManager accMan = new AccountManager(dir);
 
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
 
     public static synchronized void purge(UserSession userSession)
     {
         sessions.remove(userSession);
     }
 
 }
