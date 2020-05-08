 package mel.fencing.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.Socket;
 import mel.security.Account;
 
 public class UserSession extends Thread
 {
     public static final int MAX_TRIES = 3;
     
     private Socket socket;
     private BufferedReader in;
     private PrintStream out;
     private String name = null;
 
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
             //TODO respond to commands
             String s = in.readLine();
             char c = s.charAt(0);
             switch(c)
             {
                 case 'N':
                     if(s.charAt(1) == 'T') challengeTarget(s.substring(2));
                     else if(s.charAt(1) == 'O') challengeOpen();
                     else badCommand(s);
                     break;
                 default:
                     badCommand(s);
             }
         }
     }
 
     public void send(String s)
     {
         out.println(s);
         out.flush();
     }
     
     private void challengeTarget(String s)
     {
        // TODO make a Lobby module rather than trying to do this in the server
        Server.challengeTarget(this,s);
         
     }
 
     private void challengeOpen()
     {
         // TODO move this to the Lobby module
         Server.challengeOpen(this);
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
        Account a = Server.accMan.getAccount(name);
         try
         {
             a.authenticate(password);
             return true;
         }
         catch(SecurityException e)
         {
             return false;
         }
     }
 }
