 package com.maestrodev;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.schwering.irc.lib.IRCConnection;
 
 /**
  * Hello world!
  *
  */
 public class IrcWorker extends MaestroWorker {
 
     public IrcWorker() {
         super();
     }
 
     public void postMessage() throws Exception {
         try{
            
             final IRCConnection conn = new IRCConnection(
                     getField("server"),
                     Integer.parseInt(getField("port").toString()),
                     Integer.parseInt(getField("port").toString()) + 2,
                     null,
                     getField("nickname"),
                     getField("nickname"),
                     getField("nickname"));
             
            
             
             IrcEventListener eventListener = new IrcEventListener(conn, getField("channel"), getField("body"), this);
             conn.addIRCEventListener(eventListener);
             conn.setDaemon(true);
             conn.setColors(false);
             conn.setPong(true);
 
            
             conn.connect(); // Try to connect!!! Don't forget this!!!
             
 
            
             
             while (!eventListener.wasMessageSent() && !eventListener.wasError()) {
                 Thread.sleep(1000);
             }
             
             if(eventListener.wasError()){
                setError(eventListener.getError());
                 conn.doQuit();
                return;
             }
             
             
             //writeOutput("The Message Was Sent!");
 
             while (conn.isConnected()) {
                 
                 conn.doQuit();
                 Thread.sleep(5000);
             }
             
             writeOutput("Message " + getField("body") + " Sent");
             
         } catch (Exception e){
             if(this.getField("ignore_failure").toString().equals("false"))
                 setError("Error Posting Message " + e.getMessage());
             else {
                 this.writeOutput("Error Posting Message " + e.getMessage());
             }
         }
     }
 
     public void postMessageAndWaitForConfirmation() throws Exception {
         try{
             final IRCConnection conn = new IRCConnection(
                     getField("server"),
                     Integer.parseInt(getField("port").toString()),
                     Integer.parseInt(getField("port").toString()) + 2,
                     null,
                     getField("nickname"),
                     getField("nickname"),
                     getField("nickname"));
 
             IrcEventListener eventListener = new IrcEventListener(conn, getField("channel"), getField("body"), this);
             conn.addIRCEventListener(eventListener);
             conn.setDaemon(true);
             conn.setColors(false);
             conn.setPong(true);
 
             
             conn.connect(); // Try to connect!!! Don't forget this!!!
             
             while (!eventListener.wasMessageSent() && !eventListener.wasError()) {
                 Thread.sleep(1000);
             }
             
             if(eventListener.wasError()){
                setError(eventListener.getError());
                return;
             }
             
             writeOutput("Message " + getField("body") + " Sent");
             
             this.setWaiting(true);
 
             String message = null;
             while (message == null) {
                 //writeOutput("The Message Was Sent!");
                 while (!eventListener.wasMessageReceived()) {
                     Thread.sleep(1000);
                 }
 
                 String patternStr = "(" + getField("nickname") + "\\:\\s*((yes|no|y|n|proceed|cancel)))";
                 Pattern pattern = Pattern.compile(patternStr);
                 Matcher matcher = pattern.matcher(eventListener.getMessage());
                 System.out.println("received " + eventListener.getMessage());
 
                 if (matcher.find()) {
                     System.out.println("found a match");
                     //they're talking to me!
                     for (int groupIndex = 0; groupIndex < matcher.groupCount(); ++groupIndex) {
                         System.out.println("found a group " + matcher.group(groupIndex));
                         message = matcher.group(groupIndex);
                     }
                 }
 
                 if (eventListener.getMessage().contains(getField("nickname"))
                         && message == null) {
                     eventListener.putMessage("Sorry I was looking for something like yes or no, thanks.");
                 }
             }
 
             writeOutput("Received Response " + message);
             
             eventListener.putMessage("Great, thanks for your response, " + message);
 
             if (message.contains("yes")
                     || message.contains("y")
                     || message.contains("proceed")) {
                 
                 writeOutput("Continuing Composition");
                 
                 eventListener.putMessage("OK, let's continue shall we.");
                 this.setWaiting(false);
             } else if (message.contains("no")
                     || message.contains("n")
                     || message.contains("cancel")) {
                 
                 writeOutput("Canceling Composition");
                 
                 eventListener.putMessage("Sorry to here that, the composition will be canceled.");
                 this.cancel();
             }
 
             
             
             while (conn.isConnected()) {
                 conn.doQuit();
                 Thread.sleep(5000);
             }
             
         } catch (Exception e){
             if(this.getField("ignore_failure").toString().equals("false"))
                 setError("Error Posting Message " + e.getMessage());
             else {
                 this.writeOutput("Error Posting Message " + e.getMessage());
             }
         }
     }
 }
