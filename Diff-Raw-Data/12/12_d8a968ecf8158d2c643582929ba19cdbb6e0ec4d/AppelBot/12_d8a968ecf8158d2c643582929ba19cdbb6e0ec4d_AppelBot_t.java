 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.jbserver.main;
 
 import java.io.FileInputStream;
 import java.util.Properties;
 import org.jbserver.pircbot.PircBot;
 import org.jbserver.pircbot.User;
 
 /**
  *
  * @author Jurgen
  */
 public class AppelBot extends PircBot {
     
     private boolean bAllowedToMessage = true;
     private AppelBot _bot = this;
     private boolean running = false;
     
     private String owner;
     private String ownerhost;
     
     public AppelBot() {
         Properties properties = new Properties();
         try {
             properties.load(new FileInputStream("./config.cfg"));
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         this.owner = properties.getProperty("owner");
         this.ownerhost = properties.getProperty("ownerhost");
         
         this.setName("Appel");
         this.changeNick("Appel");
        this.setLogin("Appel");
         try {
             this.setEncoding("UTF-8");
         } catch (Exception ex) {
             ex.printStackTrace();            
         }
         this.setVersion("AppelBot");
     } 
     
     public String test() {
         this.joinChannel("#test");
         return "hoi";
     }
     
     public Boolean isAllowedToMessage() {
         return this.bAllowedToMessage;
     }
     
     protected void onConnect() {
         this.changeNick("Appel");
         this.joinChannel("#3fm");
         this.joinChannel("#veronica");
         this.joinChannel("#xerox");
         this.joinChannel("#veronica");
         this.joinChannel("#studioupstairs");
         this.joinChannel("#appeltest");
     }
     
     private void reconnectThread() {
         Thread t = new Thread() {
             public void run() {
                 try {
                     while(_bot.running) {
                         try {
                             Thread.sleep(30000);
                             _bot.reconnect();
                             _bot.running = false;
                         } catch (Exception ex) {
                             ex.printStackTrace();                            
                         }
                     }                    
                 } catch (Exception ex) {
                     ex.printStackTrace();
                 }             
             }
         };
         t.start();        
     }
     
     protected void onDisconnect() {
         _bot.running = true;
         try {
             _bot.reconnectThread();
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
     
     protected void onMessage(String channel,
                          String sender,
                          String login,
                          String hostname,
                          String message) {
             
         if(channel.equalsIgnoreCase("#studioupstairs")) {
             User[] users = this.getUsers("#studioupstairs");
             for(User o : users) {
                 if(o.getNick().equalsIgnoreCase(sender)) {
                     if(o.isOp() || (sender.equalsIgnoreCase(this.owner) && hostname.contains(this.ownerhost))) {
                         if(message.equalsIgnoreCase("!aan")) {
                             this.bAllowedToMessage = true;         
                             this.sendMessage(channel, "Now playing staat nu aan!");
                         }
                         if(message.equalsIgnoreCase("!uit")) {
                             this.bAllowedToMessage = false;
                             this.sendMessage(channel, "Now playing staat nu uit!");
                         }
                     }                     
                 }
             }
         }
         
         /*if(message.equalsIgnoreCase("ditiseengrotetest")) {
             System.out.println("ok!");
             Runtime rtime = Runtime.getRuntime();
             rtime.gc();
         }*/
         
         
     }
     
     
 }
