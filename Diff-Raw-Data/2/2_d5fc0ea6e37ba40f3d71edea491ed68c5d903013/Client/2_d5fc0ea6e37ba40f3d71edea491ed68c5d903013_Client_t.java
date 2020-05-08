 import java.util.Scanner;
 import java.net.*;
 import java.io.*;
 import static java.lang.System.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class Client {
     private final boolean DEBUG = true;
     private Socket socket = null;
     private DataInputStream console = null;
     private DataOutputStream streamOut = null;
     private DataInputStream streamIn = null;
 
     public static void main(String args[]) throws UnknownHostException {
 //        Client client = new Client(19999);// = new Client("169.254.145.138",19999);
 //        client.send("Testing");
     }
     public Client(int serverPort){
         try {
             if (DEBUG)out.println("Scanning for servers...");
             String serverName = scan();
             if (DEBUG)out.println("Server ["+serverName+"] found! Connecting...");
             socket = new Socket(serverName, serverPort);
             if (DEBUG)out.println("Connected: " + socket);
             start();
         } catch (UnknownHostException uhe) {
             System.out.println("Host unknown: " + uhe.getMessage());
         } catch (IOException ioe) {
             System.out.println("Unexpected exception: " + ioe.getMessage());
         }
     }
     public Client(String serverName, int serverPort) {
         try {
             socket = new Socket(serverName, serverPort);
             System.out.println("Connected: " + socket);
             start();
         } catch (UnknownHostException uhe) {
             System.out.println("Host unknown: " + uhe.getMessage());
         } catch (IOException ioe) {
             System.out.println("Unexpected exception: " + ioe.getMessage());
         }
     }
     public String scan() throws UnknownHostException{
         String ip = InetAddress.getLocalHost().getHostAddress();
         String root = ip.substring(0,ip.lastIndexOf(46));
         int s = Integer.parseInt(ip.substring(ip.lastIndexOf(46)));
         for (int i=s-30;i<255;i++){
             boolean found = true;
             String p ="";
             try{
                 p = root+"."+i;
                 if (DEBUG)out.println(p);
                 Socket sock = new Socket();
                 sock.connect(new InetSocketAddress(p,19999),300);
             }catch (UnknownHostException e){
                 found = false;
             }catch (IOException e){
                 found = false;
             }
             if (found)
                 return p;
         }
         return null;
     }
     public void send(String w){
         try{
             //out.println("begin send");
             streamOut.writeUTF(w);
             //out.println("begin flush");
             streamOut.flush();
             //out.println("end send");
         }catch (IOException e){
             err.println("Sending error: "+e);
         }
     }
     public String read(){
         boolean done = false;
         String line = "";
         while (!done){
             try{
                 line = streamIn.readUTF();
                 done = true;
             }catch (IOException ioe) {
                 done = true;
             }
         }
         return line;
     }
     public void readType(){
         String line = "";
         while (!line.equals(".bye")){
             try {
                 line = console.readLine();
                 streamOut.writeUTF(line);
                 streamOut.flush();
             } catch (IOException ioe) {
                 System.out.println("Sending error: " + ioe.getMessage());
             }
         }
     }
     public void start() throws IOException {
         console = new DataInputStream(System.in);
         streamOut = new DataOutputStream(socket.getOutputStream());
         streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
     }
     public void stop() {
         try {
             if (console != null) {
                 console.close();
             }
             if (streamOut != null) {
                 streamOut.close();
             }
             if (socket != null) {
                 socket.close();
             }
         } catch (IOException ioe) {
             System.out.println("Error closing ...");
         }
     }
 }
