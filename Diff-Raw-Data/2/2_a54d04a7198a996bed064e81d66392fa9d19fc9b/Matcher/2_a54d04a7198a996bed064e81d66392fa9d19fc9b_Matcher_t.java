 package edu.purdue.cs.cs180.server;
 
 import edu.purdue.cs.cs180.channel.*;
 import edu.purdue.cs.cs180.common.Message;
 /**
  * Project 8 -- SafeWalk 3.0
  * matches requesters to responders by checking and matching on a common interval
  * 
  * @author Ben Wencke
  * 
  * @recitation RM5 (Julian Stephen)
  * 
  * @date December 6, 2012
  *
  */
 public class Matcher extends Thread {
   private long sleepTime; // how long to wait between checking
   private DataFeeder feeder; // where to check for responders and requesters
   private String matchingType; // how to match (FCFS or mostRecent)
   private Channel channel; // the channel to send messages through
   
   public Matcher(DataFeeder f, long sleep, String matchingType, Channel channel) {
     // set values as received by the server
     sleepTime = sleep;
     feeder = f;
     this.matchingType = matchingType;
     this.channel = channel;
   }
   
   /**
    * what to execute when the thread is started
    */
   @Override
   public void run() {
     while(true){ // run indefinitely
       while (feeder.hasNextResponse() && feeder.hasNextRequest()){ // check if there are requesters AND responders waiting and continue matching until no matches exist
         synchronized(feeder){ // synchronize access to the data feeder
          if (matchingType.equalsIgnoreCase("FCFS")){ // first-come, first-serve
             messageSender(feeder.getFirstResponse(),feeder.getFirstRequest()); // send messages to clients
             feeder.removeFirstResponse(); // remove matched participants
             feeder.removeFirstRequest();
             
           }else if (matchingType.equalsIgnoreCase("mostRecent")){ // last-come, first serve
             messageSender(feeder.getLastResponse(),feeder.getLastRequest()); // send messages to client
             feeder.removeLastResponse(); // remove matched participants
             feeder.removeLastRequest();
           }else{
             System.out.println("Unknown mode!!!"); // error!
           }
         }
       }
       
       // sleep for a fixed interval
       try{
         sleep(sleepTime);
       }catch(Exception e){
         // something went wrong
         System.out.println("An exception occurred!");
       }
     }
   }
   
   /**
    * sends messages to clients
    * 
    * @param response the message to the responder
    * @param request the message to the requester
    */
   private void messageSender(Message response, Message request){
     try{
       channel.sendMessage("Assigned:" + response.getInfo(), request.getClientID());//sends a message to the requester telling them a responder has been assigned and what team that responder is on
       channel.sendMessage("Assigned:" + request.getInfo(), response.getClientID());//tells the longest waiting responder they have been assigned and the location of the requester they are to help
       
     }catch(ChannelException e){//catch for all that channel stuff
       System.out.println("Exception occured while sending a message after being contacted by a requester!!!");
     }
   }
 }
