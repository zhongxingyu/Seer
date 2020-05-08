 package mel.fencing.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.List;
 import mel.security.Account;
 
 public class UserSession extends Thread
 {
     public static final int MAX_TRIES = 3;
     
     private Socket socket;
     private BufferedReader in;
     private PrintStream out;
     private String name = null;
     private List<String> challenges = new ArrayList<String>();
 
     private UserSession(Socket s) throws IOException
     {
         this.socket = s;
         in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         out = new PrintStream(socket.getOutputStream());
     }
 
     public static UserSession makeSession(Socket socket)
     {
         try
         {
             UserSession s = new UserSession(socket);
             s.start();
             return s;
         }
         catch (IOException e)
         {
             System.err.println(e.getMessage());
             return null;
         }
     }
     
     @Override
     public void run()
     {
         try
         {
             if(login()) readLoop();
             else Server.purge(this);
         }
         catch(Exception e)
         {
             Server.purge(this);
         }
         
     }
 
     private void readLoop() throws IOException
     {
         while(true)
         {
             // TODO RFI think about the tradeoff between switch-pattern and command-pattern
             String s = in.readLine();
             char c = s.charAt(0);
             switch(c)
             {
                 case 'N':
                     if(s.charAt(1) == 'T') challengeTarget(s.substring(2));
                     else if(s.charAt(1) == 'O') challengeOpen();
                     else badCommand(s);
                 break;
                 case 'C':
                     Server.lobby.cancel(this);
                 break;
                 case 'A':
                     Server.lobby.acceptChallenge(this);
                 break;
                 case 'R':
                     Server.lobby.rejectChallenge(this);
                 break;
                 default:
                     badCommand(s);
             }
         }
     }
 
     void addChallenge(String username)
     {
         challenges.add(username);
     }
     
     public String getUsername()
     {
         return name;
     }
     
     public void send(String s)
     {
         out.println(s);
         out.flush();
     }
     
     private void challengeTarget(String s)
     {
         Server.lobby.challengeTarget(this, Server.name2session.get(s));
     }
 
     private void challengeOpen()
     {
         Server.lobby.challengeOpen(this);
     }
 
     private void badCommand(String s2)
     {
         out.println("EU:"+s2);
         out.flush();
     }
 
     private boolean login() throws IOException
     {
         for(int i = 0; i < MAX_TRIES; i++)
         {
             name = in.readLine();
             String password = in.readLine();
             if(auth(name, password))
             {
                 sendSuccess();
                 Server.register(name,this);
                 return true;
             } else
             {
                 sendFailure();
             }
         }
         return false;
     }
 
     private void sendFailure()
     {
         out.println("E"+name);
         out.flush();
     }
 
     private void sendSuccess()
     {
         out.println("L"+name);
         out.flush();
     }
     
     private boolean auth(String name, String password)
     {
         try
         {
             Account a = Server.accMan.getAccount(name);
             a.authenticate(password);
             return true;
         }
         catch(SecurityException e)
         {
             return false;
         }
     }
 
     void rejectPending()
     {
         for(String challenger: challenges)
         {
             Server.lobby.rejectChallenge(this, Server.name2session.get(challenger));
             challenges.remove(challenger);
         }
     }
 
     void processNextChallenge()
     {
         if(challenges.isEmpty()) return;
         Server.lobby.challengeFromQueue(Server.name2session.get(challenges.remove(0)), this);
     }
 
     public void removeChallenge(String username)
     {
         challenges.remove(username);
     }
 }
