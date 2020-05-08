 package cs4253;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Socket;
 
 public class ServerProcess implements Runnable {
     private UserList users;
     private IRCUser user;
     private BufferedReader in;
 
 
     public ServerProcess(Socket client, UserList users) throws IOException{
         in = new BufferedReader(new InputStreamReader(client.getInputStream()));
         this.users = users;
         user = new IRCUser(client, in.readLine());
         users.add(user);
     }
 
     public void run(){
     	System.out.println("Running.");
     	boolean connected = true;
         String inputLine, command = "", message = "";
         String[] inputSplit;
         char action;
 
         try {
             while(connected && (inputLine = in.readLine()) != null){
             	System.out.println(user.getUsername() + ": " + inputLine); // DEBUGGING
             	// parse input
                 inputSplit = inputLine.split(" ");
                 action = inputSplit[0].charAt(0);
                 if(inputSplit.length > 1)
                 	for(int i = 1; i < inputSplit.length; i++)
                 		message += inputSplit[i] + " ";
 
                 switch(action){
                     case '@':	// if @user, find appropriate IRCUser
                     	command = inputSplit[0].substring(1, inputSplit[0].length()-1);
                    	users.sendMessage(message, command);
                         break;
                     case '/':	// if /command, execute command
                     	command = inputSplit[0].substring(1, inputSplit[0].length());
                     	if(command.equalsIgnoreCase("disconnect")){
                     		connected = false;
                     		users.remove(user);
                     		user.sendMessage("/disconnect");
                     		in.close();
                     		user.close();
                     	}
                         break;
                     default:
                     	users.sendMessageToAllExcept(user.getUsername() + ": " + inputLine, user.getUsername());
                 }
             }
         } catch(IOException e){
             // PROBABLY INCORRECT! how to make a thread kill itself?
             System.out.println("Error: " + e);
             users.remove(user);
             return;
         }
     }
 }
