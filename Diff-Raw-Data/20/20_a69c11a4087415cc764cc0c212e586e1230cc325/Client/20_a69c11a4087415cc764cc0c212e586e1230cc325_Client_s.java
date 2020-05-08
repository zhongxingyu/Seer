 package client;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.*;
 import java.util.regex.*;
 
 import javax.swing.DefaultListModel;
 
 public class Client {
     public final String username;
     public final String IPAddress;
     public final int port;
     private final Socket socket;
     private final PrintWriter out;
     private final BufferedReader in;
     
 //    private final DefaultListModel allUsers;
 //    private final HashMap<String, ChatRoomClient> connectedRoomsHistory;
 //    private final HashMap<String, ChatRoomClient> connectedRoomsCurrent;
 //    private final DefaultListModel allRooms;
     
 
     public Client(String username, String IPAddress, int port) throws IOException {
         this.username = username;
         this.IPAddress = IPAddress;
         this.port = port;
         this.socket = new Socket(IPAddress, port);
         System.err.println("Connected to server");
 
         this.in = new BufferedReader(new InputStreamReader(
                 socket.getInputStream()));
         this.out = new PrintWriter(socket.getOutputStream());
         System.err.println("IO Streams enabled");
 
         System.err.println("Waiting for Handshake");
         String prompt = in.readLine();
 
         System.err.println("Verifying Handshake");
         if (!prompt.equals("To connect type: \"connect [username]\""))
             throw new IOException("Server returned invalid handshake");
         System.err.println("Handshake Passed");
 
         System.err.println("Sending Username");
         out.println("connect " + this.username);
         out.flush();
 
         System.err.println("Verifying Username");
         prompt = in.readLine();
         if (!prompt.matches("Connected"))
             throw new IOException(prompt);
         
 //        this.allUsers = new DefaultListModel();
 //        this.allRooms = new DefaultListModel();
 //        this.connectedRoomsHistory = new HashMap<String,ChatRoomClient>();
 //        this.connectedRoomsCurrent = new HashMap<String, ChatRoomClient>();
 
         System.err.println("Client connected");
     }
 
 //    public ChatRoomClient joinRoom(String roomName)
 //    {
 //    	ChatRoomClient room;
 //    	if(connectedRoomsHistory.keySet().contains(roomName))
 //    		room =  connectedRoomsHistory.get(roomName);
 //    	else
 //    		room = new ChatRoomClient(roomName);
 //    	connectedRoomsHistory.put(roomName, room);
 //    	connectedRoomsCurrent.put(roomName, room);
 //    	return room;
 //    }
 
 
     public String readBuffer() throws IOException {
         try {
             return in.readLine();
         } catch (IOException e) {
             throw new IOException("Disconnected from Server");
         }
     }
 
     public void send(String output) {
         out.println(output);
         out.flush();
         System.err.println(output);
         return;
     }
     
     public String getUsername() {
         return username;
     }
 
 //    public DefaultListModel getRoomModel() {
 //        return allRooms;
 //    }
 //    
 //    public DefaultListModel getUsersModel() {
 //        return allUsers;
 //    }
 
     public void parseInput(String input) {
         // TODO: Sayeed - write stuff that interprets the string and updates the
         // proper jcomponent. this method will be called by the gui
         // in the swing worker's 'done' command. this way this method will be
         // called from the EDT instead of the secondary thread.
     }
     
     public void start(client.gui.MainWindow main) {
         try {
             for(String input = in.readLine(); input!=null; input = in.readLine()) {
                 // TODO Check if input is disconnect.  If it is, stop, break, etc.
                 // Otherwise, make the action event and pass it into MainWindow
                 ActionEvent e = new ActionEvent(input, 0, input);
                main.actionPerformed(e);
             }
         } catch(IOException e) {
             
         }
     }
 
     // just a method to test this rig... you shouldnt use it in your gui.
     public static void main(String[] args) {
         try {
             Client c = new Client("user2", "127.0.0.1", 10000);
 
             while (true)
                 System.out.println(c.readBuffer());
         } catch (IOException e) {
             System.err.println(e.getMessage() + "\n");
             e.printStackTrace();
         }
     }
 }
