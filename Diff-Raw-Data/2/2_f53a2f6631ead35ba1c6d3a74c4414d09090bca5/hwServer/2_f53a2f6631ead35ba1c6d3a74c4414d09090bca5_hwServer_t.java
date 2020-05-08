 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.lang.*;
 import java.awt.*;
 import java.math.*;
 
 public class hwServer implements Runnable
 {
     public static int MONITOR_PORT;
     public static int LOCAL_PORT;
     static String IDENT;
     ServerSocket s = null;
     ConnectionHandler myConHand = null;
     BufferedReader fcin = null; // file cookie in
 
     Thread runner = null;
     
     // int state
     // 0: ident
     // 1: password
     // 2: alive
     // 3: quit
     
     public hwServer(String ident, int monitor_port, int local_port)
     {
         try
         {
             MONITOR_PORT = monitor_port;
             LOCAL_PORT = local_port;
             s = new ServerSocket(LOCAL_PORT);
             IDENT = ident;
         }
         catch (Exception e)
         {
             System.out.println("Server [hwServer]: Error in Server: " + e);
             System.exit(5);
         }
     }
     
     public void start()
     {
         if (runner == null)
         {
             runner = new Thread(this);
             runner.start();
         }
     }
     
     public void run()
     {
         while(Thread.currentThread() == runner)
         {
             try
             {
                 int i = 0;
                 while(true)
                 {
                     System.out.println("--SERVER: Attempting accept.");
                     Socket incoming = s.accept();
                     System.out.println("--SERVER: Accept attempted.");
                     myConHand = new ConnectionHandler(incoming, IDENT, i);
                     myConHand.start();
                     i++;
                 }
             }
             catch (Exception e)
             {
                 System.out.println("Server [run]: Error in Server: "  + e);
             }
         }
     }
 }
 
 
 class ConnectionHandler extends hwSuper implements Runnable
 {
     Socket incoming = null;
     int threadID;
 
     // For Transfers
     private String Recipient;
     private String Sender;
     private int Amount;
     private BigInteger mN;
     private BigInteger mV;
     private int lengthOfAuthorizeSet;
     
     
     // Authentication States
     // 10: First step of authentication
     
     public ConnectionHandler(Socket sSocket, String id, int thID)
     {
         try
         {
             incoming = sSocket;
             out = new PrintWriter(incoming.getOutputStream(), true);
             in = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
             IDENT = id;
             threadID = thID;
             
             dhe = new DiffieHellmanExchange("DHKey");
         }
         catch (Exception e)
         {
             System.out.println("ConnectionHandler [ConnectionHandler]: Error in Server: " + e);
         }
     }
     
     public void start()
     {
         if (runner == null)
         {
             runner = new Thread(this);
             runner.start();
         }
     }
 
     public void run()
     {
         while(Thread.currentThread() == runner)
         {
             try
             {
                 while(!done)
                 {
                     waiting = false;
                     
                     while(!waiting)
                     {
                         mMsg = GetMonitorMessage(encrypted, thisIsClient, threadID);
                         
                         if (mMsg == null)
                         {
                             // Unlike in the client, where we have someone to fix thing,
                             // when things go blank here, we bail (this is usually the
                             // correct thing to do anyhow).
                             break;
                         }
                         
                         if (mMsg.trim().equals("REQUIRE: IDENT"))
                         {
                             sMsg = 0;
                         }
                         else if (mMsg.trim().equals("REQUIRE: PASSWORD"))
                         {
                             sMsg = 1;
                         }
                         else if (mMsg.trim().equals("REQUIRE: ALIVE"))
                         {
                             sMsg = 2;
                         }
                         else if (mMsg.trim().equals("REQUIRE: QUIT"))
                         {
                             sMsg = 3;
                         }
                         else if (mMsg.trim().equals("REQUIRE: ROUNDS"))
                         {
                             sMsg = 10;
                         }
                         else if (mMsg.trim().equals("REQUIRE: SUBSET_A"))
                         {
                             sMsg = 11;
                         }
                         else if (mMsg.trim().equals("REQUIRE: TRANSFER_RESPONSE"))
                         {
                             sMsg = 12;
                         }
                         else
                         {
                             String[] tokens = mMsg.split(" ");
                             
                             if (tokens[0].equals("RESULT:"))
                             {
                                 if (tokens[1].equals("IDENT"))
                                 {
                                     // This is the serverPubKey.
                                     mMsg = tokens[2];
                                     sPubKey = new BigInteger(mMsg, 32);
                                     
                                     // Compute the secret
                                     Secret = dhe.computeSecret(sPubKey);
                                     kDE = new hwKarn(Secret);
                                     encrypted = true;
                                 } else if (tokens [1].equals("PUBLIC_KEY"))
                                 {
                                     mV = new BigInteger(tokens[2]);
                                     mN = new BigInteger(tokens[3]);
                                 } else if (tokens [1].equals("AUTHORIZE_SET"))
                                 {
                                     // do we need to store the authorize set?
                                     // authorize_set = tokens[2 ... length-1];
                                     // YES, this is how we continue the check that the initiator knows s, I think.
                                     // TODO: save authorize_set
                                     
                                     int lengthOfAuthorizeSet = tokens.length - 2; // this is just number of rounds, or should be at least.
                                 } else if (tokens [1].equals("SUBSET_K"))
                                 {
                                     // save subset_k for testing
                                     
                                 } else if (tokens [1].equals("SUBSET_J"))
                                 {
                                     // save subset_j for testing
                                 }
                             }
                             else if (tokens[0].equals("TRANSFER:"))
                             {
                                 Recipient = tokens[1];
                                 Amount = Integer.parseInt(tokens[2]);
                                 Sender = tokens[4];
                                 
                                 System.out.format("E>--Server%d: Starting authentication.\n", threadID);
                                 
                                 //sMsg = 10;
                             }
                         }
                         
                         if (mMsg.trim().equals("WAITING:"))
                         {
                             waiting = true;
                         }
                         else if (sMsg > 9)
                         {
                             waiting = true;
                         }
                     }
                     
                   //  System.out.format("--SERVER%d: Server state: %d\n", threadID, sMsg);
                     
                     switch(sMsg)
                     {
                         case 0:
                             if (!encrypted)
                             {
                                 System.out.format("--SERVER%d: Returning ident.\n", threadID);
                                 out.println("IDENT " + IDENT + " " + dhe.x_pub.toString(32));
                             }
                             else
                             {
                                 System.out.format("E>--SERVER%d: Returning ident.\n", threadID);
                                 String thisMessage = "IDENT " + IDENT + " " + dhe.x_pub.toString(32);
                                 thisMessage = kDE.encrypt(thisMessage);
                                 out.println(thisMessage);
                             }
                             break;
                         case 1:
                             System.out.format("--SERVER%d: Password requested; error.\n", threadID);
                             break;
                         case 2:
                             if (!encrypted)
                             {
                                 System.out.format("--SERVER%d: Returning cookie.\n", threadID);
                                 fcin = new BufferedReader(new FileReader(COOKIEFILE));
                                 mMsg = fcin.readLine();
                                 System.out.format("--%d: " + mMsg + "\n", threadID);
                                 fcin.close();
                                 out.println("ALIVE " + mMsg);
                             }
                             else
                             {
                                 System.out.format("E>--SERVER%d: Returning cookie.\n", threadID);
                                 fcin = new BufferedReader(new FileReader(COOKIEFILE));
                                 mMsg = fcin.readLine();
                                 System.out.format("E>--%d: " + mMsg + "\n", threadID);
                                 fcin.close();
                                 mMsg = "ALIVE " + mMsg;
                                 out.println(kDE.encrypt(mMsg));
                             }
                         case 3:
                             if (!encrypted)
                             {
                                 System.out.format("--SERVER%d: Quitting.\n", threadID);
                                 out.println("QUIT");
                             }
                             else
                             {
                                 System.out.format("E>--SERVER%d: Quitting.\n", threadID);
                                 mMsg = "QUIT";
                                 out.println(kDE.encrypt(mMsg));
                             }
                             break;
                         case 10:
                             if (encrypted)
                             {
                                 // for now, rounds = 1;
                                 System.out.format("E>--SERVER%d: Returning rounds.\n", threadID);
                                 
                                 mMsg = "ROUNDS 7";
                                 out.println(kDE.encrypt(mMsg));
                             }
                             break;
                         case 11:
                             if (encrypted)
                             {
                                 System.out.format("E>--SERVER%d: Returning Subset_A.\n", threadID);
                                 
                                 mMsg = "SUBSET_A 2 4 5"; // just uses default from example for sake of brevity // TODO : fix this later
                                 
                                 out.println(kDE.encrypt(mMsg));
                             }
                             break;
                         case 12:
                             if (encrypted)
                             {
                                 // TODO: add testing for s_K and s_J
                                 // if pass tests, response is good
                                 
                                 // if fail, then not.
                                 
                                 // for now, just pass to look good !DANGEROUS BEHAVIOR, POINTS MAY BE STOLEN!
                                // or fail, if you want to use protection. 
                                 
                                 mMsg = "TRANSFER_RESPONSE ACCEPT";
 //                                mMsg = "TRANSFER_RESPONSE DECLINE";
                                 out.println(kDE.encrypt(mMsg));
                                 
                             }
                             break;
                     }
                     
                     if (done)
                     {
                         break;
                     }
                 }
                 
                 if (done)
                 {
                     break;
                 }
             }
             catch (Exception e)
             {
                 System.out.println("ConnectionHandler [run]: Error in Server: " + e);
                 System.exit(5);
             }
         }
         
         System.out.println("A server connection has exited.");
     }
 }
