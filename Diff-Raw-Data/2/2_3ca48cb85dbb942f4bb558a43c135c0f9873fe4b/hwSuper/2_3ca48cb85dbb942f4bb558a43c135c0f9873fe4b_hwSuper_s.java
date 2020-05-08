 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.lang.*;
 import java.awt.*;
 import java.math.*;
 
 class hwSuper
 {    
     boolean waiting = false;
     boolean done = false;
     boolean encrypted = false;
     boolean thisIsClient = false;
     PrintWriter out = null;
     BufferedReader in = null;
     
     BufferedReader fcin = null; // file cookie in
     static String COOKIEFILE = "cookie.txt";
     static String IDENT = "";
     
     Thread runner = null;
     
     DiffieHellmanExchange dhe = null;
     BigInteger sPubKey = null;
     BigInteger Secret = null;
     hwKarn kDE = null; // karn decryption encryption
     
     int sMsg = -1;
     String mMsg = "";
     
     public String GetMonitorMessage(boolean isEncrypted, boolean isClient, int threadID)
     {
         String msg = "";
         
         try
         {
             msg = in.readLine();
         }
         catch (Exception e){}
         
         if (!(msg == null) && !msg.trim().equals(""))
         {
             if (isEncrypted)
             {
                 // decrypt!
                 msg = kDE.decrypt(msg);
                 System.out.print("E>");
             }
             
             // Output message to screen so we know what's going on.
             if (isClient)
             {
                 System.out.println(msg);
             }
             else
             {
                System.out.format("––SERVER%d:MONITOR " + msg + "\n", threadID);
             }
         }
         else
         {
             waiting = true;
             done = true;
             sMsg = -10;
         }
         
         return msg;
     }
 }
