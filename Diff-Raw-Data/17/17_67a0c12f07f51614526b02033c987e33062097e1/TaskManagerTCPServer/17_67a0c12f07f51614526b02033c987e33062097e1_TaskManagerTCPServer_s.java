 package itu.dk.smds.e2012.common;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jgroups.*;
 
 /**
  * Class handling the server
  */
 public class TaskManagerTCPServer extends ReceiverAdapter{
     /*
     private static Socket socket;
     private ServerSocket serverSocket;
     private static DataInputStream dis;
     */
     private static Cal cal = CalSerializer.getCal();
     
     private static JChannel channel;
     
     /**
      * Main method for initializing the server
      * @param args the command line arguments
      */
     public void start(String[] args) throws Exception {
                 
                 channel = new JChannel();
                 channel.setReceiver(this);
                 //System.out.println("Channel (Name): " + channel.getName());
                 //System.out.println("Channel (Address):" + channel.getAddressAsString());                   
                 channel.connect("ServerCluster1");
                 eventLoop();
                 channel.close();
     }
         
     private void eventLoop(){    
         BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
 
         while(true) {
             try {
             System.out.print("> "); System.out.flush();
             String line=in.readLine().toLowerCase();
                 if(line.startsWith("end") || line.startsWith("close")) {
                     break;
                 }
 
             Message msg=new Message(null, null, line);
 
             channel.send(msg);
 
             } catch(Exception e) {
             }
 
         }
     }
         
     @Override
     public void viewAccepted(View new_view){
         System.out.println("** view: " + new_view);
     }
     
     @Override
     public void receive(Message msg){
         try{
             try{
                 String rec = (String) msg.getObject();
                 System.out.println("REC "+ rec);
                 if("deleteall".equals(rec)){
                     deleteAll(msg);
                 }
             } catch(Exception e) {
                 //Do nothing, internal command for reseting task list
             }
             Operation operation = new Operation(msg);
             Thread operationThread = new Thread(operation);
             operationThread.start();
         } catch (Exception e){
             System.out.println("Error while parsing command");
             // Send message back to client using "send"
         }
     }    
     
     private class Operation implements Runnable {
         
         Message msg;
         String type;
         
         public Operation(Message msg) {
             this.msg = msg;
             
             try{
                 Object[] receiver = (Object[]) msg.getObject();
                 type = receiver[0].toString();
             } catch (Exception e){
                 type="NULL";
             }
         }
         
         public void run() {
             if (type.equals("POST")) {
                 post(msg);
             } else if (type.equals("PUT")) {
                 put(msg);
             } else if (type.equals("GET")) {
                 get(msg);
             } else if (type.equals("DELETE")) {
                 delete(msg);
             } else if(type.equals("NULL")){
                 //Do nothing, not a request
             }
         }
         
         private void post(Message msg){
 
             // Internal logic for creating a task
             try {
                 Object[] arg = (Object[]) msg.getObject();
                 Task task = (Task) arg[1];
                 synchronized(task) {
                     cal.POST(task);
                 }
             } catch (ClassCastException ex) {
                 Logger.getLogger(TaskManagerTCPServer.class.getName()).log(Level.SEVERE, null, ex);
                 System.out.println("Task creation failed");
             }
         }
 
         private void put(Message msg){
             // Internal logic for creating a task
             try {
                 Object[] arg = (Object[]) msg.getObject();
                 Task task = (Task) arg[1];
                synchronized(task) {
                    cal.PUT(task);
                }
             } catch (ClassCastException ex) {
                 Logger.getLogger(TaskManagerTCPServer.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         private void get(Message msg){
             List<Task> list = cal.GET();
             Object[] arg = new Object[]{"SENT",list};
             try{
                 channel.send(new Message(null, null, arg));
             } catch (Exception e){
                 System.out.println("Unable to send task list");
             }
         }
 
         private void delete(Message msg){
             try{
                 Object[] arg = (Object[]) msg.getObject();
                 String str = (String) arg[1];
                 synchronized(str) {
                     cal.DELETE(str);
                 }
             } catch (ClassCastException e){
                 System.out.println("Couldn't delete task");
             }
         }
     }
     
     private void deleteAll(Message msg){
             try{
                 System.out.println(msg.getScope());
                 cal.deleteAllTask();
             } catch (ClassCastException e){
                 System.out.println("Couldn't delete task");
             }
     }
     
     /**
      * Creates an user object
      * no longer used.
      * @param name, the name of the user
      * @param password, the password of the user
      */
     private static void createUser(String name, String password){
         cal.addUser(new User(name, password));
     }
     
     /**
      * Method for creating an xml file
      * no longer used
      */
     private static void calToXml(){
         try{
             CalSerializer.makeXmlFile(cal);
         } catch(IOException e) {
             System.out.println("No file printed");
         }
     }
     
     
     public static void main(String[] args) throws Exception{
         new TaskManagerTCPServer().start(args);
     }
 }
