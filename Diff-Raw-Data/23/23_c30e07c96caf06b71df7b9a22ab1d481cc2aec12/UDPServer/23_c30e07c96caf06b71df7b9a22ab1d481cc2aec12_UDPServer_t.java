 package macgyvers.mds04;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 import macgyvers.mds04.xml.Task;
 /**
  * This class listens to requests from a socket via UDP.
  * If a request is recieved in the correct syntax, a Task is
  * created and placed in the que.
  * @author MacGyvers
  *
  */
 public class UDPServer extends Thread {
 	 
     protected DatagramSocket socket = null;
     protected BufferedReader in = null;
     protected boolean looping = true;
     
     // Task details that are temporarily stored from each UDP request. 
     protected String user = null;
     protected String command = null;
     protected String id = null;
     protected String idNumber = null;
     protected String name = null;
     protected TaskType type = null;
     private TaskHandler handler = TaskHandler.getInstance();
     
     //constructor calls the constructor in the ancestor class. 
     public UDPServer() throws IOException {
     	this("ServerThread");
     }
     // overload constructor with custom name
     public UDPServer(String name) throws IOException {
         super(name);
         socket = new DatagramSocket(4445);
     }
     //inherited method from thread
     public void run() {
         while (looping) {
             try {
                 byte[] buf = new byte[256];
                 
                 String returnMessage = "";
                 // receive request
                 DatagramPacket packet = new DatagramPacket(buf, buf.length);
                 socket.receive(packet);
                 // if no data is received
                 if (packet.getData() == null)
                    returnMessage = "Usage: user command-id";
                 // new string created from input
                 String input = new String(packet.getData(), 0, packet.getLength());
                 // splitting the input string to isolate commands
                 String[] inputs = input.split(" ");
                for(String ino : inputs)
                        System.out.println(ino);
                //if all information is not entered from the client
                 if(inputs.length < 2)
                	returnMessage = "Usage: user command id";
                 else {
                 	user = inputs[0];
                	command = inputs[1]; //the command is currently not used, but can be implemented to support different commands.
                	id = inputs[2]; //id stored in format "tasktype-idnumber", as in "handin-01"
                 	//splitting the ID into type and id-number
                 	String[] strArr = id.split("-");
                 	//this line checks whether the task id is entered correctly
                 	if(strArr.length != 2) returnMessage = "please enter a valid assignment type and id, eg. handin-01";
                 	else {
                 		idNumber = strArr[1].trim();
                 		//the first part of ex. "handin-01", "handin" is used to return the correct TaskType enum.
                 		type = TaskType.getType(strArr[0].trim().toLowerCase());
                 	}
                 }
                 //if there are no errors so far (returnMessage not altered)
                 if(returnMessage == ""){
                 	System.out.println("Task Received, Id: "+ id);
                     returnMessage = "Task received, Id: "+id;
 	               // creates a new job and adds the relevant dependencies from the enum + timestamp etc..
 	               Task task = new Task(id, new Date(), idNumber);
 	               // adding the enum conditions
 	               task.conditions = type.conditions;
 	               //submit it for execution
                 	handler.submitTask(task);
                 }
                 buf = returnMessage.getBytes();
 
                 // send the response to the client at "address" and "port"
                 InetAddress address = packet.getAddress();
                 int port = packet.getPort();
                 packet = new DatagramPacket(buf, buf.length, address, port);
                 socket.send(packet);
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         socket.close();
     }
 }
