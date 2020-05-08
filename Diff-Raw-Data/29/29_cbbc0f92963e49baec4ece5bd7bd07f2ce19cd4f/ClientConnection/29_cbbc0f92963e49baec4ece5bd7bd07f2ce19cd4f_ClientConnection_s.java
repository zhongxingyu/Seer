 package client;
 
 import java.net.Socket;
 import java.util.Calendar;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JList;
 
 import server.Server;
 import main.User;
 
 import main.Connection;
 import main.Packet;
 import main.Connection.Command;
 
 
 
 public class ClientConnection extends Connection {
     Client client;
     private MainApp gui;
     public ClientConnection(Socket socket, Client client) {
         super(socket);
         this.client = client;
     }
     public void join(String room) {
         Packet m = new Packet(Command.JOIN, room, Calendar.getInstance(), "", "");
         processMessage(m);
     }
     public void login(String userName) {
         System.out.println("Connection's got this shit");
         Packet m = new Packet(Command.LOGIN, "", Calendar.getInstance(), userName, "");
         processMessage(m);
     }
     public void message(String message, String room) {
         Packet m = new Packet(Command.MESSAGE, room, Calendar.getInstance(), message,  "");
         processMessage(m);
     }
     public String getUsername() {
         return client.getUser();
     }
     public List<String> getRoomMessages(String roomName) {
         return client.roomMessages.get(roomName);
     }
     public void processMessage(Packet message){
         System.out.println("Message received and processing in progress");
         System.out.println(message.getCommand().name() + ": " + message.getChannelName() + " " + message.getMessageText());
         Packet response;
         switch(message.getCommand()){
         case LIST_CHANNELS:
             break;
         case REPLY_SUCCESS:
             break;
         case LOGIN:
            
             break;
         case JOIN:
             response = new Packet(Command.LOGIN, "", Calendar.getInstance(), client.getUser(), "");
             sendMessage(response);
             break;
         case REPLY_FAILURE:
             break;
         case LIST_USERS:
             break;
         case MESSAGE:
             String mess = client.getUser()+": "+ message.getMessageText();
             if (gui.roomLabel.getText().equals(message.getChannelName())) {
                 synchronized(gui.chatList) {
                     JList chatList = gui.chatList;
                     DefaultListModel model = (DefaultListModel) chatList.getModel();
                     model.addElement(mess);
                 }
             }
             client.roomMessages.get(message.getChannelName()).add(mess);
             break;
         case QUIT:
             break;
         default:
             System.out.println("Fell through");
             break;
             
         }
     }
     
 
 
 }
