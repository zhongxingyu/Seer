 package chatosaur.client;
 
 import java.net.*;
 import java.io.*;
 import java.util.ArrayList;
 
 import chatosaur.common.ConnectedServer;
 
 public class Client {
 
     private Socket socket;
     private DataOutputStream out;
     public BufferedReader in;
     public ArrayList<ConnectedServer> serverList;
 
     public Client() { }
 
     public void start() {
         
         System.out.print("\nServer host: ");
         String host = getUserInput();
 
         System.out.print("Server port: ");
         int port = Integer.parseInt(getUserInput());
 
         if (connectToServer(host, port)) {
             startMessageBuffer();
         } else {
             System.out.println("Could not connect to server.");
         }
     }
 
     public boolean connectToServer(String host, int port) {
 
         try {
             this.socket = new Socket(host, port);
             this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             this.out = new DataOutputStream(socket.getOutputStream());
 
             sendHandshake();
             receiveServerList();
 
         } catch (IOException e) { 
             return false;
         }
 
         return true;
     }
 
     private void startMessageBuffer() {
 
         try {
 
             new MessageBuffer(this);
 
             // just a blank line for formatting
             System.out.println("");
 
             while(true) {
 
                 BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
 
                 String input = userInput.readLine();
 
                 out.writeBytes(input + '\n');
             }
         } catch(IOException e) {
             e.printStackTrace();
         }
     }
 
     private void sendHandshake() {
         try {
             // let the server know we're a client
             out.writeBytes("client\n");
         } catch (IOException e) {
             System.out.println("Could not send handshake.");
             System.exit(0);
         }
     }
 
     public void receiveServerList() {
         try {
             ObjectInputStream oo = new ObjectInputStream(socket.getInputStream());
 
             // read in the new server list object
             serverList = (ArrayList<ConnectedServer>)oo.readObject();
 
            String list = serverList.get(0).getClientName();

            // build up a list of the servers so we can log this
            for (int i=1; i<serverList.size(); i++) {
                ConnectedServer s = serverList.get(i);
                list = list + ", " + s.getClientName();
            }

            System.out.println("Server list received: " + list);
         } catch (IOException e) {
             System.out.println("Could not receive server list.");
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         }
     }
 
     // nice compact method to take user input
     private static String getUserInput() {
         String input = "";
 
         try {
             BufferedReader UserInput = new BufferedReader(new InputStreamReader(System.in));
             input = UserInput.readLine();
         } catch(IOException e) {
             e.printStackTrace();
         }
         return input;
     }
 
     public static void main(String[] args) {
         Client client = new Client();
         client.start();
     }
 }
