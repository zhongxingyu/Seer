 package server;
 import java.io.*;
 import java.net.*;
 public class server extends Thread
 {
 BufferedReader br1,socketReader;
 PrintWriter socketWriter;
 String st1,st2;
 ServerSocket serverChannel;
 Socket connection;
 
 public server(int port)
 {
     try 
     {
         
         serverChannel=new ServerSocket(port);
         connection=serverChannel.accept();
         br1=new BufferedReader(new InputStreamReader(System.in));
         socketReader=new BufferedReader(new InputStreamReader(connection.getInputStream()));
         socketWriter=new PrintWriter(connection.getOutputStream(),true);
 
       
     }
 
     catch(Exception ee)
     {
         System.out.println(ee);
         System.out.println("after socket created");
     }
 }
 public void setupServer()
     {
         try 
         {   
             
             String passwd=null;
             passwd=readPassword();
             
 
             //check if password is present
             if(passwd==null)
             { 
                 //if no password, ask for one and save it
                 System.out.println("Not setup!");
                socketWriter.println("Please enter a password");
                 //read password and save
                 st2=socketReader.readLine();
                 savePassword(st2);
             }
             else
             {
                 //if password exists then check and start services
                 
                 socketWriter.println("Enter password");
                 st2=null;
                 st2=socketReader.readLine();
                 
                 
                 //pass is correct?
                 if(st2.equals(passwd.trim()))
                 {   
                    System.out.println("Client Authenticated!");
                    socketWriter.println("Starting services");
                    serverKeyboard kb=new serverKeyboard(5000);
                    kb.run();
                 }
                 else
                 {
 
                   socketWriter.println("Authentication Fail");
                 }
 
             }
 
         }
 
         catch(Exception ee) 
         {
             System.out.println(ee);
         }
     }
  
 
 String readPassword() throws IOException 
     {
 
       String content = null;
       File file = new File("passwd.txt"); //for ex foo.txt
       if(file.exists())
       {
         try 
       {
        FileReader reader = new FileReader(file);
        char[] chars = new char[(int) file.length()];
        reader.read(chars);
        content = new String(chars);
        reader.close();
       } 
       catch (IOException e) 
       {
        e.printStackTrace();
       }
       return content;
       }
       else
       {
         return content=null;
       }
     }   
 
 void savePassword(String password) throws IOException
 {
      PrintWriter out = new PrintWriter("passwd.txt");
      out.println(password);
      out.close();
 
 }
 
 public void run() {
   setupServer();
 }
 
 
 }
 
