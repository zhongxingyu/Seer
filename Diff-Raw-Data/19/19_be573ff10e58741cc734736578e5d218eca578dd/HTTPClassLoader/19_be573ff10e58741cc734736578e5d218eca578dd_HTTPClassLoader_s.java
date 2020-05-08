 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dynamicNetworking.calculator.operationsServer;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.PrintWriter;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author steffen
  */
 public class HTTPClassLoader extends ClassLoader {
     
     private Socket server;
     
     private String host;
     private String rootDirectory;
     
     private PrintWriter writer;
     private BufferedReader reader;
     private DataInputStream inputStream;
     
     private ObjectInputStream is = null;
     
 	private ObjectOutputStream os = null;
     
     public HTTPClassLoader(String rootDirectory, String host, int port) {
         this.rootDirectory = rootDirectory;
         this.host = host;
         
         try {
             server = new Socket(host, port);
             
             writer = new PrintWriter(server.getOutputStream(), true);
             reader = new BufferedReader(
                     new InputStreamReader(server.getInputStream()));
             inputStream = new DataInputStream(server.getInputStream());
         } catch (UnknownHostException ex) {
             System.err.println("Unknown host: " + host);
         } catch (IOException ex) {
             System.err.println("IOException creating socket");
         }
     }
 
     @Override
     public Class findClass(String className) {
         byte[] bytes = loadClassData(className);
         return defineClass(className, bytes, 0, bytes.length);
     }
 
     public byte[] loadClassData(String className) {
         byte[] bytes = null;
         
         sendRequest(className);
         bytes = readResponse();
         /*os.reset();
         os.writeObject(new ResourceRequest(className, "CLASS"));
         os.flush();
         
         ResourcePacket resourcePacket = (ResourcePacket) is.readObject();
         bytes = resourcePacket.getResourceBytes();*/
         
         return bytes;
     }
     
     private void sendRequest(String className) {
         writer.print("GET /" + rootDirectory + className + " HTTP/1.0\r\n");
         writer.print(host + "\r\n");
         writer.print("\r\n");
     }
     
     private byte[] readResponse() {
         byte[] bytes = null;
         int size;
         try {
             if (!reader.readLine().startsWith("HTTP/1.0 200")) {
                 return null;
                 // should probably do something other than return null
             }
             //date
            reader.readLine();
             //server
             reader.readLine();
             // should theoretically be the content length
             String contentLength = reader.readLine();
             StringTokenizer tokenizer = new StringTokenizer(contentLength);
             if (!tokenizer.nextToken().equals("Content-Length")) {
                 System.err.println("Something went wrong with the response");
             }
             size = Integer.parseInt(tokenizer.nextToken());
             // content-type
             reader.readLine();
             
             bytes = new byte[size];
             
             int result = inputStream.read(bytes);
             if (result == -1) {
                 System.err.println("Failed to read bytes");
             }
         } catch (IOException ex) {
             System.err.println("Failed to readLine");
         }
         
         return bytes;
     }
 }
