 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package src;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.Scanner;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Ping
  */
 public class Client {
 
     private String name;
     private int port;
     private ServerSocket server;
     private ExecutorService exec = Executors.newCachedThreadPool();
     
     private static final String BASE_DIR = "C:\\Users\\Ping\\Downloads\\project\\";
     private static final Logger LOG = Logger.getLogger(Client.class.getName());
 
     public Client(String name, int port) throws IOException {
         this.name = name;
         this.port = port;
         server = new ServerSocket(port);
     }
 
     private class UserWorker implements Runnable {
 
         @Override
         public void run() {
 
             LOG.info("Spawning user service thread");
             try {
                 Socket s = new Socket(AppConstants.HOST, AppConstants.CSROUTER_PORT);
                 PrintWriter w = new PrintWriter(s.getOutputStream(), true);
                 BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
                 new File(BASE_DIR + name + "\\").mkdir();
 
                 Scanner scan = new Scanner(System.in);
                 String userRequest;
 
                 while (true) {
                     LOG.info("Getting next user command(format must be GET <filename> <fromclient>)");
                     userRequest = scan.nextLine();
                     if (userRequest.contentEquals("bye")) {
                         w.println(AppConstants.BYE + " " + name);
                         r.close();
                         w.close();
                         s.close();
                         server.close();
 
                         LOG.info("User quit");
                         exec.shutdownNow();
                         break;
                     }
 
                     String[] commands = userRequest.split("\\s");
 
                     if (commands.length != 3) {
                         LOG.warning("Insufficient number of parameters in GET command");
                         continue;
                     }
                     String fileName = commands[1];
                     String fromClient = commands[2];
                     
                     if(fromClient.contentEquals(name)){
                         LOG.warning("You can't request files from yourself.");
                         continue;
                     }
                     
                     long fileLookUpTimeStart = System.currentTimeMillis();
 
                     w.println(AppConstants.GET + " " + fileName + " " + fromClient);
 
                     String response = r.readLine();
 
                     switch (response) {
                         case AppConstants.REQUESTUNSUCCESSFULL:
                             long fileLookUpTime = System.currentTimeMillis() - fileLookUpTimeStart;
                             LOG.log(Level.INFO, "File was not available. Search took {0}ms", fileLookUpTime);
                             break;
                         case AppConstants.FILE:
                             fileLookUpTime = System.currentTimeMillis() - fileLookUpTimeStart;
                             
                             LOG.log(Level.INFO, "File was found in {0}ms. Receiving file", fileLookUpTime);
 
                             File f = new File(BASE_DIR + name + "\\" + fileName);
                             FileOutputStream fos = new FileOutputStream(f);
                             InputStream in = s.getInputStream();
                             byte[] buffer = new byte[s.getReceiveBufferSize()];
                             int count;
 
                             long start = System.currentTimeMillis();
                             
                             while ((count = in.read(buffer)) > 0) {
                                 fos.write(buffer, 0, count);
                                 fos.flush();
                             }
                            fos.close();
                            in.close();
                             long end = System.currentTimeMillis();
                             LOG.log(Level.INFO, "File received in {0}ms. File size was {1} bytes.", new Object[]{end-start, f.length()});
                             break;
                     }
 
                 }
 
             } catch (UnknownHostException ex) {
                 LOG.log(Level.SEVERE, null, ex);
             } catch (IOException ex) {
                 LOG.log(Level.SEVERE, null, ex);
             }
 
 
         }
     }
 
     private class ClientWorker implements Runnable {
 
         private Socket requester;
         private BufferedReader reader;
 
         public ClientWorker(Socket s) {
             requester = s;
         }
 
         @Override
         public void run() {
 
             LOG.log(Level.INFO, "Spawning request service thread from port {0}", requester.getPort());
             try {
 
                 reader = new BufferedReader(new InputStreamReader(requester.getInputStream()));
 
                 String request = reader.readLine();
                 String[] commands = request.split("\\s");
 
                 dispatch(commands, requester);
 
 
             } catch (IOException ex) {
                 LOG.log(Level.SEVERE, null, ex);
             } finally {
                 LOG.info("Request complete, ending thread");
                 try {
                     reader.close();
                     requester.close();
                 } catch (IOException ex) {
                     LOG.log(Level.SEVERE, null, ex);
                 }
 
             }
         }
     }
 
     public boolean register() throws UnknownHostException, IOException {
         try (Socket s = new Socket("localhost", AppConstants.CSROUTER_PORT)) {
             String response;
             try (PrintWriter w = new PrintWriter(s.getOutputStream(), true); BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
                 w.println(AppConstants.HELLO + " " + this.name + " " + this.port);
                 response = r.readLine();
             }
             return response.contentEquals(AppConstants.ACKNOWLEDGED) ? true : false;
 
         }
 
     }
 
     public void userService() {
         exec.execute(new UserWorker());
     }
 
     public void listen() throws IOException {
         LOG.info("Client is now listening");
 
         while (true) {
             exec.execute(new ClientWorker(server.accept()));
         }
     }
 
     private void dispatch(String[] commands, Socket s) throws IOException {
         PrintWriter writer = new PrintWriter(s.getOutputStream(), true);
 
         switch (commands[0]) {
             case AppConstants.GET:
                 if (commands.length != 2) {
                     LOG.warning("Insufficient parameters for GET command");
                     writer.println(AppConstants.FILENOTFOUND);
                     break;
                 }
 
                 String fileName = commands[1];
                 File f = new File(BASE_DIR + fileName);
 
                 if (!f.isFile()) {
                     writer.println(AppConstants.FILENOTFOUND);
                     LOG.info("File not found on client");
                 } else if (f.length() > Integer.MAX_VALUE) { //imposing a limit on the size of data that can be transferred
                     writer.println(AppConstants.FILENOTFOUND);
                     LOG.warning("File requested was too large to be transferred");
                 } else {
                     writer.println(AppConstants.FILE);
                     try (FileInputStream file = new FileInputStream(f); BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream())) {
                         byte[] buffer = new byte[1024];
                         int count;
 
                         LOG.info("Sending file");
                         
                         long uploadTimeStart = System.currentTimeMillis();
 
                         while ((count = file.read(buffer)) > 0) {
                             bos.write(buffer, 0, count);
                             bos.flush();
                         }
                         
                         long uploadTime = System.currentTimeMillis() - uploadTimeStart;
                         LOG.log(Level.INFO, "File sent in {0}ms", uploadTime);
                     }
                     
                 }
                 break;
             default:
                 LOG.log(Level.WARNING, "Unknown command: {0}", commands[0]);
         }
 
     }
 }
