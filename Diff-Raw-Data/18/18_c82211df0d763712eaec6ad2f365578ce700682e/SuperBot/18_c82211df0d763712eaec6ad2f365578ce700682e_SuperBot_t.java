 /** 
  Project "com.quui.chat.core" (C) 2006 Fabian Steeg
 
  This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 
  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 package com.quui.chat.ui.irc;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
 
 /**
  * Based on BasicIRCBot -- A framework for an IRC bot Creation date: (02/04/01)
  * by David Seager
  * http://www-128.ibm.com/developerworks/library/j-javabot/index.html
  * 
  * Superclass for Hollaka and Hollala
  * 
  * @author Fabian Steeg (fsteeg)
  */
 public class SuperBot {
 
     protected String server;
 
     protected String[] channels;
 
     private BufferedReader IRCir;
 
     private BufferedWriter IRCor;
 
     private Socket IRCServerS;
 
     public boolean running;
 
     // bot details
     private String botName;
 
     private String botDescription;
 
     private long timeOfLastPing = -1;
 
     /**
      * Connects to an IRC server
      */
     protected void connect(String serverHostname, int serverPort) {
         // connect a socket to the IRC server
         try {
             IRCServerS = new Socket(serverHostname, serverPort);
         } catch (Exception e) {
             System.err.println("error connecting to IRC server");
             e.printStackTrace();
             System.exit(0);
         }
 
         // get input and output streams from the IRC server
         InputStream IRCis = null;
         OutputStream IRCos = null;
         try {
             IRCis = IRCServerS.getInputStream();
             IRCos = IRCServerS.getOutputStream();
         } catch (Exception e) {
             System.err.println("error opening streams to IRC server");
             e.printStackTrace();
             System.exit(0);
         }
 
         IRCir = new BufferedReader(new InputStreamReader(IRCis));
         IRCor = new BufferedWriter(new OutputStreamWriter(IRCos));
 
         return;
 
     }
 
     /**
      * Disconnect from an IRC server
      */
     protected void disconnect() {
         // close the IO streams to the IRC server
         try {
             IRCir.close();
             IRCor.close();
         } catch (IOException e) {
             System.err.println("Error disconnecting from IRC server");
             e.printStackTrace();
         }
     }
 
     /**
      * Sends a raw string to the IRC server
      */
     protected boolean ircsend(String message) {
 
         // Log.logger.debug("[IRC] irc: '" + message + "'");
         System.out.println("[IRC] irc: '" + message + "'");
         try {
             IRCor.write(message);
             IRCor.newLine();
             IRCor.flush();
         } catch (IOException e) {
             return false;
         }
 
         return true;
 
     }
 
     /**
      * A Basic IRC Bot
      */
     public SuperBot() {
         // set the bot's nickname and description
         // this.botName = botName;
         // this.botDescription = botDescription;
     }
 
     /**
      * logs off from the IRC server
      */
     protected void logoff() {
         // BufferedReader br = IRCir;
         BufferedWriter bw = IRCor;
 
         try {
             if (!ircsend("quit terminating"))
                 ;
             bw.write("quit terminating");
             bw.newLine();
             bw.flush();
         } catch (Exception e) {
             System.out.println("[IRC] logoff error: " + e);
             System.exit(0);
         }
     }
 
     /**
      * Logs onto the IRC server as a user
      */
     protected void logon() {
         BufferedWriter bw = IRCor;
 
         try {
             // send user info
             bw.write("user " + botName + " ware2 irc :" + botDescription);
             bw.newLine();
             bw.write("nick " + botName);
             bw.newLine();
             bw.flush();
         } catch (Exception e) {
             System.out.println("[IRC] logon error: " + e);
             System.exit(0);
         }
 
         return;
     }
 
     /**
      * Parses commands sent as private messages and calls the processing method
      * 
      * @param msg
      *            java.lang.String the IRC message
      */
     private void parse_privmsg(String username, String params) {
         /*
          * params are in the form <my nick> :<message> or <my nick> <message>
          */
         System.out.println("[IRC] parse_privmsg passed '" + params + "' from '"
                 + username + "'");
 
         String message;
         String channel = params.split(" ")[0];
         // get my own nick
         // String me = params.substring(0, params.indexOf(' '));
         params = params.substring(params.indexOf(' ') + 1);
 
         // extract the command
         if (params.substring(0, 1).equals(":")) {
             message = params.substring(1);
         } else {
             message = params.substring(0);
         }
 
         // call the private message processing method
         srv_privmsg(username, message, channel);
 
         return;
     }
 
     /**
      * Checks for a ping and replys with a pong
      * 
      * @param msg
      *            java.lang.String the IRC message
      */
     private boolean pingpong(String msg) throws IOException {
         this.timeOfLastPing = System.currentTimeMillis();
         if (msg.substring(0, 4).equalsIgnoreCase("ping")) {
             // send a pong back
             String pongmsg = "pong " + msg.substring(5);
             IRCor.write(pongmsg);
             IRCor.newLine();
             IRCor.flush();
 
             System.out.println("[IRC] ping pong");
 
             return true;
         }
 
         return false;
     }
 
     /**
      * Send a notice to an IRC user
      * 
      * @param username
      *            java.lang.String
      * @param message
      *            java.lang.String
      */
     protected void send_notice(String username, String message) {
         String command = "notice " + username + " :" + message;
 
         ircsend(command);
 
         return;
     }
 
     /**
      * Send a private message to an IRC user
      * 
      * @param username
      *            java.lang.String
      * @param message
      *            java.lang.String
      */
     public void send_privmsg(String username, String message) {
         String command = "privmsg " + username + " :" + message;
 
         ircsend(command);
 
         return;
     }
 
     /**
      * Receives and handles IRC messages
      */
     protected void service() {
 
         try {
             // see if there's some input
             if (IRCir.ready()) {
                 String msg = IRCir.readLine();
                 System.out.println("input: " + msg);
                 // deal with pings
                 if (!pingpong(msg)) {
                     // check for a recognisable command
                     String prefix = null;
                     String command = null;
                     String params = null;
 
                     // check for the prefix
                     if (msg.substring(0, 1).equals(":")) {
                         prefix = msg.substring(1, msg.indexOf(' '));
                         msg = msg.substring(msg.indexOf(' ') + 1);
                     }
 
                     // extract the command
                     command = msg.substring(0, msg.indexOf(' '));
 
                     // get the parameters (the rest of the message)
                     params = msg.substring(msg.indexOf(' ') + 1);
 
                     System.out.println("[IRC] prefix: '" + prefix
                             + "' command: '" + command + "' params: '" + params
                             + "'");
 
                     // deal with privmsgs
                     if (command.equalsIgnoreCase("privmsg")) {
                         String username = null;
                         if (prefix.indexOf('!') != -1) {
                             username = prefix.substring(0, prefix.indexOf("!"));
                         } else {
                             username = prefix;
                         }
 
                         parse_privmsg(username, params);
                     }
 
                 }
 
             } else {
                 // sleep for a bit
                 try {
                     Thread.sleep(100);
                 } catch (InterruptedException e) {
                 }
 
             }
         } catch (IOException e) {
             System.out.println("[IRC] error: " + e);
             System.exit(0);
         }
     }
 
     /**
      * Process a private message
      * 
      * @param username
      *            java.lang.String nickname of the user who sent the message
      * @param message
      *            java.lang.String the command
      */
     protected void srv_privmsg(String username, String command, String channel) {
         return;
     }
 
     public String getBotDescription() {
         return botDescription;
     }
 
     public void setBotDescription(String botDescription) {
         this.botDescription = botDescription;
     }
 
     public String getBotName() {
         return botName;
     }
 
     public void setBotName(String botName) {
         this.botName = botName;
     }
 
     /**
      * Starts the bot: connect to server
      */
     public void initIRCBot() {
         new Thread(new Runnable() {
 
             public void run() {
                 connect(server, 6667);
                 logon();
                 for (String channel : channels) {
                     ircsend("join " + channel);
                 }
                init();
                 running = true;
                 while (running
                         && timeOfLastPing == -1
                         || (System.currentTimeMillis() - timeOfLastPing) < 240000) {
                     service();
                 }
                 timeOfLastPing = -1;
                 System.out.println("DONE RUNNING");
                 botName = botName.endsWith("-2") ? botName.substring(0, botName
                         .length() - 2) : botName + "-2";
                 logoff();
                 disconnect();
                 initIRCBot();
             }
 
         }).start();
     }

    protected void init() {
        // TODO Auto-generated method stub
        
    }
 }
