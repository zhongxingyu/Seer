 /**
  * Project 7 -- Server
  * A server that interacts with the Request and Response GUI's.  The server itself has a CLI and can be 
  * terminated by typing exit into the console.  To run the server requires one argmuent, the port it is
  * to run on.  
  *
  * @author Alex Aralis
  *
  * @recitation 004 Julian James Stephen
  *
  * @date 11/4/12
  *
  */
 
 import edu.purdue.cs.cs180.channel.*;
 import java.util.LinkedList;
 import java.util.Scanner;
 
 class Server{
   public TCPChannel channel;//probably should have a get method for the request and responder channels but made them public insead because im lazy
   private LinkedList<Integer> requestersID;//list of backlogged requesters
   private LinkedList<Integer> respondersID;//list of backlogged responders
   private LinkedList<String> requestersUrgency;
   private LinkedList<String> requestersLocation;//list of places that the requesters are located at.  Corresponds to the requester ID list
   private LinkedList<String> respondersLocation;
   private LinkedList<String> respondersTeam;//list of teams that the responders are on.  Corresponds to the responder ID list.
   
   public Server(int port, final int MATCH_TYPE){
     channel = new TCPChannel(port);//init of all the object variables for the server
     requestersID = new LinkedList<Integer>();
     respondersID = new LinkedList<Integer>();
     requestersLocation = new LinkedList<String>();
     respondersLocation = new LinkedList<String>();
     respondersTeam = new LinkedList<String>();
     requestersUrgency = new LinkedList<String>();
     
     channel.setMessageListener(new MessageListener(){
       private int getRespondersIndex(String location, String urgency, int MATCH_TYPE){
         switch(MATCH_TYPE){
           case 0://FCFS
             return 0;
             
           case 1://Closest
           case 2://Urgency
             return getShortestDistance(location, respondersLocation);
             
           default://String mismatch
             return -1;
         }
       }
       
       private int getRequestersIndex(String helpTeam, String location, int MATCH_TYPE){
         switch(MATCH_TYPE){
           case 0://FCFS
           case 1://Closest
             return 0;
             
           case 2://Urgency
             if(requestersUrgency.indexOf("Emergency") != -1)
               return requestersUrgency.indexOf("Emergency");
             
             if(requestersUrgency.indexOf("Urgent") != -1)
               return requestersUrgency.indexOf("Urgent");
             
             if(requestersUrgency.indexOf("Normal") != -1)
               return requestersUrgency.indexOf("Normal");
           default://String mismatch
             return -1;
         }
       }
       
       private void requestInterpreter(String location, String urgency, int clientID){
         try{
           if(respondersID.size() == 0){//if there are no responders on call then...
             channel.sendMessage("Searching:", clientID); //tells the requester that it needs to wait
             requestersID.add(clientID); //adds the requester ID to the backlog
             requestersLocation.add(location);//adds the requester location to the backlog
             requestersUrgency.add(urgency);//adds the requesters urgency to the backlog
             
           }else{//if there are responders then...
             int respondersIndex = getRespondersIndex(location, urgency, MATCH_TYPE);
             
            channel.sendMessage("Assigned:" + respondersTeam.remove(respondersIndex) + ".  Time to your location is approximately " + distance(respondersLocation.remove(respondersIndex), location) + " minute(s).", clientID);//sends a message to the requester telling them a responder has been assigned and what team that responder is on
             channel.sendMessage("Assigned:" + location + " - " + urgency, respondersID.remove(respondersIndex));//tells the longest waiting responder they have been assigned and the location of the requester they are to help
           }
         }catch(ChannelException e){//catch for all that channel stuff
           System.out.println("Exception occured while sending a message after being contacted by a requester!!!");
         }
       }
       
       private void responseInterpreter(String helpTeam, String location, int clientID){
         try{
           if(requestersID.size() == 0){//if there are no backlogged requesters then...
             channel.sendMessage("Searching:", clientID);//tell the responder they need to wait
             respondersID.add(clientID);//log the responder ID
             respondersTeam.add(helpTeam);//log the responders team
             respondersLocation.add(location);//log the responders location
             
           }else{//if there are backlogged requesters then...
             int requestersIndex = getRequestersIndex(helpTeam, location, MATCH_TYPE);
             
             channel.sendMessage("Assigned:" + helpTeam + ".  Time to your location is approximately " + distance(location, requestersLocation.get(requestersIndex)) + " minute(s).", requestersID.remove(requestersIndex));//tell the longest waiting requester a responder has been assigned and the team of that responder
             channel.sendMessage("Assigned:" + requestersLocation.remove(requestersIndex) + " - " + requestersUrgency.remove(requestersIndex), clientID);//tell the responder they have been assigned and the location of the requester they are picking up
           }
         }catch(ChannelException e){//catch for all that channel stuff
           System.out.println("Exception occured while sending a message after being contacted by a responder!!!");
         }
       }
       
       public void messageReceived(String message, int clientID){//when a message is received by a requester then...
         String[] splitString = message.split(":");
         String clientType = splitString[0];
         
         if(clientType.equals("Request")){
           splitString = splitString[1].split("\\|");
           String location = splitString[0];
           String urgency = splitString[1];
           
           requestInterpreter(location, urgency, clientID);
         }else if(clientType.equals("Response")){
           splitString = splitString[1].split("\\|");
           String helpTeam = splitString[0];
           String location = splitString[1];
           
           responseInterpreter(helpTeam, location, clientID);
         }
       }
     });
   }
   
   public static int getShortestDistance(String start, LinkedList<String> endList){
     int min = Integer.MAX_VALUE;
     int minIndex = -1;
     
     for(int i = 0; i < endList.size(); i++){
       if (distance(start, endList.get(i)) < min){
         min = distance(start, endList.get(i));
         minIndex = i;
       }
       
       return minIndex;
     }
     
     return -1;
   }
   
   public static int distance(String start, String end){
     String[] place = {"CL50 - Class of 1950 Lecture Hall", "EE - Electrical Engineering Building", 
       "LWSN - Lawson Computer Science Building", "PMU - Purdue Memorial Union", 
       "PUSH - Purdue University Student Health Center"};
     
     int[][] distance = {
       {0,8,6,5,4},
       {8,0,4,2,5},
       {6,4,0,3,1},
       {5,2,3,0,7},
       {4,5,1,7,0}};
     
     int startNum = -1;
     int endNum = -1;
     
     for(int i = 0; i < place.length; i++){
       if(place[i].equals(start))
         startNum = i;
       
       if(place[i].equals(end))
         endNum = i;
     }
     
     return distance[startNum][endNum];
   }
   
   public static void main(String[] args){
     Scanner s = new Scanner(System.in);//to get input from the console
     final int MATCH_TYPE;
     if (args[1].equalsIgnoreCase("FCFS")){
       MATCH_TYPE = 0;
     } else if (args[1].equalsIgnoreCase("CLOSEST")) {
       MATCH_TYPE = 1;
     } else if (args[1].equalsIgnoreCase("URGENCY")) {
       MATCH_TYPE = 2;
     } else {
       MATCH_TYPE = -1;
       System.out.println("Second arg must be FCFS, Closest, or Urgency!!!");
       System.exit(1);
     }
     
     Server server = new Server(Integer.parseInt(args[0]), MATCH_TYPE);//creates a server object that uses a port passed from the comsole
     
     while(true){//a loop that runs until "exit" is typed into the console
       if(s.nextLine().equals("exit")){
         try{
           server.channel.close();
         }catch(ChannelException e){
           System.out.println("Channel failed to close");
           e.printStackTrace();
         }
         break;
       }
     }
   }
 }
