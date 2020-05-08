 package uk.ac.cam.md481.fjava.tick2;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 
 import uk.ac.cam.cl.fjava.messages.DynamicObjectInputStream;
 import uk.ac.cam.cl.fjava.messages.Message;
 
 public class ChatClient {
   private String host;
   private Integer port;
   private Socket connection;
   
   public ChatClient(String host, Integer port){
     this.host = host;
     this.port = port;
   }
   
   public void connect() throws IOException {
     this.connection = new Socket(this.host, this.port);
     new ClientMessage("Connected to " + this.host + " on port " + this.port).print();
   }
   
   public void disconnect(){
     try {
       this.connection.close();
     } catch(IOException e){}
   }
   
   public void run() throws UserQuitException {
     output();
     input();
   }
   
   private void input() throws UserQuitException {
     try {
       BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
       ObjectOutputStream stream = new ObjectOutputStream(this.connection.getOutputStream());
       
       String line = null;
       while((line = reader.readLine()) != null){
         try {
           stream.writeObject((new InputMessage(line)).getMessage());
           stream.flush();
         } catch(UnknownCommandException e){
           new ClientMessage("Unknown command \"" + e.getCommand() + "\"").print();
         }
       }
       
       stream.close();
     } catch(IOException e){}
   }
   
   private void output(){
     Thread output = new Thread(){
       public void run(){
         try {
           DynamicObjectInputStream stream = new DynamicObjectInputStream(connection.getInputStream());
           
           Message message = null;
           while((message = (Message) stream.readObject()) != null){
             try {
               System.out.println(new ServerMessage(message));
             } catch(NewMessageTypeException e){
               stream.addClass(e.getName(), e.getClassData());
             }
           }
         } catch(IOException e){
         } catch(ClassNotFoundException e){}
       }
     };
     output.start();
   }
   
   public static void main(String[] args) {
     String host;
     Integer port;
     
     try {
       if(args.length != 2) throw new IllegalArgumentException();
       host = args[0];
       port = Integer.parseInt(args[1]);
     } catch(IllegalArgumentException e){
       System.err.println("This application requires two arguments: <machine> <port>");
       return;
     }
     
     ChatClient client = new ChatClient(host, port);
     
     try {
       client.connect();
       client.run();
      
     } catch(IOException e){
       System.err.println("Cannot connect to " + args[0] + " on port " + args[1]);
     } catch(UserQuitException e){
       new ClientMessage("Connection terminated.").print();
     } finally {
       client.disconnect();
     }
   }
 
 }
