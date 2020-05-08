 import java.io.*;
 import java.net.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 public class Server extends JFrame{
    
    private JTextField userText;
    private JTextArea chatWindow;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private ServerSocket server;
    private Socket connection;
    
    //constructor
    public Server(){
       super("WilTecIM Server");
       userText = new JTextField();
       userText.setEditable(false);
       userText.addActionListener(
          new ActionListener(){
             public void actionPerformed(ActionEvent event){
                sendMessage(event.getActionCommand());
                userText.setText("");
             }
          }
       );
       add(userText, BorderLayout.NORTH);
       chatWindow = new JTextArea();
       add(new JScrollPane(chatWindow));
       setSize(300,150);
       setVisible(true);
    }
    
   //512
    //set up and run the server
    public void startRunning(){
       try{
          server = new ServerSocket(6789, 100);
          while(true){
             try{
                waitForConnection();
                setupStreams();
                whileChatting();
             }catch(EOFException eofException){
               showMessage("\n Error: 512. Server ended the connection! ");
             }finally{
                closeRubbish();
             }
          }
       }catch(IOException ioException){
          ioException.printStackTrace();
       }
    }
    
 
    private void waitForConnection() throws IOException{
       showMessage(" Waiting for someone to connect... \n");
       connection = server.accept();
       showMessage(" Now connected to " + connection.getInetAddress().getHostName());
    }
    
    private void setupStreams() throws IOException{
       output = new ObjectOutputStream(connection.getOutputStream());
       output.flush();
       input = new ObjectInputStream(connection.getInputStream());
       showMessage("\n Streams Are Setup! \n");
    }
 //303
    private void whileChatting() throws IOException{
       String message = " You Are Connected! ";
       sendMessage(message);
       ableToType(true);
       do{
          try{
             message = (String) input.readObject();
             showMessage("\n" + message);
          }catch(ClassNotFoundException classNotFoundException){
             showMessage("\n Error: 303");
          }
       }while(!message.equals("CLIENT - END"));
    }
   
    private void closeRubbish(){
       showMessage("\n Closing connections... \n");
       ableToType(false);
       try{
          output.close();
          input.close();
          connection.close();
       }catch(IOException ioException){
          ioException.printStackTrace();
       }
    }
 
    private void sendMessage(String message){
       try{
          output.writeObject("SERVER - " + message);
          output.flush();
          showMessage("\nSERVER - " + message);
       }catch(IOException ioException){
          chatWindow.append("\n ERROR: Cannot Send");
       }
    }
   
    private void showMessage(final String text){
       SwingUtilities.invokeLater(
          new Runnable(){
             public void run(){
                chatWindow.append(text);
             }
          }
       );
    }
    
    private void ableToType(final boolean tof){
       SwingUtilities.invokeLater(
          new Runnable(){
             public void run(){
                userText.setEditable(tof);
             }
          }
       );
    }
    
}
