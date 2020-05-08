 package client;
 /*
  * dJC: The dAmn Java Client
  * damnApp.java
  * ?2005 The dAmn Java Project
  *
  * This software and its source code are distributed under the terms and conditions of the GNU
  * General Public License, Version 2. A copy of this license has been provided.
  * If you do not agree with the terms of this license then please erase all copies
  * of this program and it's source. Thank you.
  */
 
 import javax.swing.*;
 
 /**
  * The main application class.
  * @version 0.2.2
  * @author MSF
  */
 public class damnApp {
     private int connected=0;
    private String version = "0.5";
     private damnProtocol protocol;
     private Thread socketThread;
     private Runnable commRunnable;
     private damnChatPage dCP;
     private damnProperties prop;
     private damnConfig conf;
     private damnAppGUI dJgui;
     private damnWhois dWho;
     
     
     /**
      * damnApp constructor.
      * Initilizes protocol and dCP.
      */
     public damnApp() {
         conf = damnConfig.getInstance();
         protocol = new damnProtocol(this, conf);
         prop = new damnProperties(conf);
         dCP = new damnChatPage(this, protocol);
         dJgui = new damnAppGUI(this, dCP);
         dWho = new damnWhois(protocol);
         dCP.setPane(dJgui.getTabbedPane());
     }
     
     /**
      * The function for printing information to the server terminal.
      * @param type Indicates the type of message being sent. 0 is for informational. 1 is for outgoing communication and 2 is for incoming.
      * @param data The actual information.
      */
     public synchronized void terminalEcho(int type, String data) {
         switch (type) {
             case 0:
                 dJgui.getServerTerminal().append("*** " + data + "\n");
                 break;
             case 1:
                 dJgui.getServerTerminal().append(">>> " + data + "\n");
                 break;
             case 2:
                 dJgui.getServerTerminal().append("<<< " + data + "\n");
                 break;
         }
         dJgui.getServerTerminal().setCaretPosition(dJgui.getServerTerminal().getDocument().getLength());
     }
 
     /**
      * Forwards messages to damnAppGUI handler.
      */
     public void actionPerformed(java.awt.event.ActionEvent e) {
         dJgui.doServerCommand(e);
     }
     
     /**
      * Connects to dAmn with the specified username and password.
      * @param username The username to connect with, if none is specified the data from the config file will be used.
      * @param password The password to connect with.
      */
     public void connectUserPass(String username, String password) {
         if(username.equalsIgnoreCase("")) {
             username = conf.getUser();
             password = conf.getPassword();
         }
         
         terminalEcho(0, "Fetching authtoken, please wait...");
         TokenFetcher tf = new TokenFetcher("www.deviantart.com", this);
         String authtoken = tf.doTokenFetch(username, password);
         if(authtoken == null) {
             terminalEcho(0, "Error fetching authtoken.");
             terminalEcho(0, "If you know what yours is use /tokenconnect to connect to dAmn.");
             return;
         }
         terminalEcho(0, "Connecting to " + conf.getHost() + ":" + String.format("%d", conf.getPort()));
         protocol.setUserInfo(username, authtoken);
         commRunnable = new damnComm(protocol, conf.getHost(), conf.getPort());
         socketThread = new Thread(commRunnable);
         socketThread.start();
         connected = 1;
     }
     
     /**
      * Connects to dAmn with the specified username and authtoken. Both must be specified.
      * @param username The username to connect with.
      * @param authtoken The authtoken to connect with.
      */
     public void connectUserAuth(String username, String authtoken) {
         terminalEcho(0, "Connecting to " + conf.getHost() + ":" + String.format("%d", conf.getPort()) + "\n");
         protocol.setUserInfo(username, authtoken);
         commRunnable = new damnComm(protocol, conf.getHost(), conf.getPort());
         socketThread = new Thread(commRunnable);
         socketThread.start();
         connected = 1;
     }
     
     /**
      * Disconnects from dAmn.
      */
     public void disconnect() {
         if(connected == 1) {
             damnComm dCtmp = (damnComm)commRunnable;
             dCtmp.shutdownComm();
             connected = 0;
             terminalEcho(0, "Disconnected.");
         }
     }
     
     /**
      * This function tells the properties system to show up.
      */
     public void showProperties() {
         prop.showProperties();
     }
     
     /**
      * Checks to see if we're connected, then joins a channel.
      * @param channel Channel to join.
      */
     public void joinChannel(String channel) {
         if(connected == 1) {
             protocol.doJoinChannel(channel);
         } else {
             terminalEcho(0, "Gotta connect first dumbass!");
         }
     }
     
     /**
      * Checks to see if we're connected, then joins a private chat.
      * @param user User to private chat with.
      */
     public void joinPrivateChat(String user) {
         if(connected == 1) {
             protocol.doJoinPrivateChat(user);
         } else {
             terminalEcho(0, "Not connected moron!");
         }
     }
     
     /**
      * Passes a whois request to the protocol object.
      * @param username The username to run a whois on.
      */
     public void runWhois(String username) {
         //dWho.show();
         dWho.showWhois(username);
         //protocol.doGetUserInformation(username);
     }
     
     /**
      * Checks to see if we are connected, then parts a channel.
      * @param channel Channel to part.
      */
     public void partChannel(String channel) {
         if(connected == 1) {
             protocol.doPartChannel(channel);
         } else {
             terminalEcho(0, "Gotta connect first you idiot.");
         }
     }
     
     /**
      * Checks to see if we're connected then parts a private chat.
      * @param user The user to part the private chat with.
      */
     public void partPrivateChat(String user) {
         if(connected == 1) {
             protocol.doPartPrivateChat(user);
         } else {
             terminalEcho(0, "Gotta connect first you idiot.");
         }
     }
     
     /**
      * Will write the about data to the server terminal.
      * Thought it wouldbe a good idea to make it to a method so it's easy to edit.
      */
     public void aboutMoi() {
         terminalEcho(0, "");
         terminalEcho(0, "dJC: The dAmn Java Client " + version);
         terminalEcho(0, "http://www.sourceforge.net/projects/damnjava");
         terminalEcho(0, "");
         terminalEcho(0, "Written by...");
         terminalEcho(0, "\tMSF - Lead Developer/Project Manager");
         terminalEcho(0, "\tEric Olander - Developer");
         terminalEcho(0, "\tMiklosi Attila - Developer");
         terminalEcho(0, "");
         terminalEcho(0, "If you are interested in getting involved: Let MSF know.");
         terminalEcho(0, "Now back to your regularly scheduled programming...");
         terminalEcho(0, "");
     }
     
     /**
      * Simple little built-in Help System.
      * @param command The command to print help info for. If blank prints command list.
      */
     public void showHelp(String command) {
         if(command == "") {
             terminalEcho(0, "");
             terminalEcho(0, "dJC Core Command List:");
             terminalEcho(0, "connect, tokenconnect, join, part, jpchat, ppchat, disconnect, away, back");
             terminalEcho(0, "Type \"/help [command]\" for more information about each command.");
             terminalEcho(0, "");
         } else if(command.equalsIgnoreCase("connect")) {
             terminalEcho(0, "");
             terminalEcho(0, "USAGE: /connect [username] [password]");
             terminalEcho(0, "\tThis command will connect you to the server with your username and password.");
             terminalEcho(0, "\tIt is reccomended to use the File->Connect menu option since that listens to");
             terminalEcho(0, "\tyour preferences.");
             terminalEcho(0, "");
         } else if(command.equalsIgnoreCase("tokenconnect")) {
             terminalEcho(0, "");
             terminalEcho(0, "USAGE: /tokenconnect [username] [authtoken]");
             terminalEcho(0, "\tIf the normal username and password connect system is unable to fetch your");
             terminalEcho(0, "\tauthtoken from deviantart.com for some reason, then you will have to know");
             terminalEcho(0, "\tyour authtoken to be able to get into dAmn. If you do then just use this");
             terminalEcho(0, "\tcommand and drop in the appropreate values and VOILA! You're in!");
             terminalEcho(0, "");
         } else if(command.equalsIgnoreCase("join")) {
             terminalEcho(0, "");
             terminalEcho(0, "USAGE: /join [channel]");
             terminalEcho(0, "\tUse this command to join a channel. Please do not include the # in front of");
             terminalEcho(0, "\tthe channel name. It will cause an error.");
             terminalEcho(0, "");
         } else if(command.equalsIgnoreCase("part")) {
             terminalEcho(0, "");
             terminalEcho(0, "USAGE: /part [channel]");
             terminalEcho(0, "\tUse this command to leave a channel. Please do not include the # in front of");
             terminalEcho(0, "\tthe channel name. It will cause an error.");
             terminalEcho(0, "");
         } else if(command.equalsIgnoreCase("jpchat")) {
             terminalEcho(0, "");
             terminalEcho(0, "USAGE: /jpchat [username]");
             terminalEcho(0, "\tUse this to join a private chat with the user specified in the username field.");
             terminalEcho(0, "\tIMPORTANT: You will have to tell the other user to join the private chat too or");
             terminalEcho(0, "\tthey will not see your messages. dAmn will not automatically show them the chat.");
             terminalEcho(0, "\tAlso, this command is unique to dJC. The other user will have to cunsult the");
             terminalEcho(0, "\tdocumentation for their client to figure out how to join private chats.");
             terminalEcho(0, "");
         } else if(command.equalsIgnoreCase("ppchat")) {
             terminalEcho(0, "");
             terminalEcho(0, "USAGE: /ppchat [username]");
             terminalEcho(0, "\tUse this to leave a private chat with the user specified.");
             terminalEcho(0, "\tThis command is unique to dJC. The other user will have to consult the");
             terminalEcho(0, "\tdocumentation for their client to figure out how to leave private chats.");
             terminalEcho(0, "");
         } else if(command.equalsIgnoreCase("away")) {
             terminalEcho(0, "");
             terminalEcho(0, "USAGE: /away [reason]");
             terminalEcho(0, "\tThis will set you to an away status on dAmn with the reason in the");
             terminalEcho(0, "\treason field. It is reccomended that you use the menu options to");
             terminalEcho(0, "\tchange away statuses.");
             terminalEcho(0, "");
         } else if(command.equalsIgnoreCase("back")) {
             terminalEcho(0, "");
             terminalEcho(0, "USAGE: /back");
             terminalEcho(0, "This will unset your away status.");
             terminalEcho(0, "");
         } else if(command.equalsIgnoreCase("disconnect")) {
             terminalEcho(0, "");
             terminalEcho(0, "USAGE: /disconnect");
             terminalEcho(0, "\tWill disconnect you from the server.");
             terminalEcho(0, "\tIt is reccomended you use the File->Disconnect option instead.");
             terminalEcho(0, "");
         }
     }
     
     /**
      * Passes along the kill command to protocol object.
      * @param user User to kill.
      * @param conn Connection to kill.
      * @param reason Reason for kill.
      */
     public void passKill(String user, String conn, String reason) {
         protocol.doKill(user, conn, reason);
     }
     
     /**
      * Will check if a chat page exists for a channel.
      * @param chat The chat.
      */
     public boolean chatExists(String chat) {
         if(dCP.findPages(chat) != -1) {
             return true;
         } else {
             return false;
         }
     }
     
     /**
      * Gets the current browser command.
      * @return The current browser command setting.
      */
     public String browserCommand() {
         return conf.getBrowsercommand();
     }
     
     /**
      * Gets the version setting.
      * @return A string containing the current version.
      */
     public String getVersion() {
         return version;
     }
     
     /**
      * This method preforms all startup operations for the program.
      */
     public void go() {
         try {
             conf.readConfig();
         } catch (InvalidXMLException ex) {
             ex.printStackTrace();
         } catch (java.io.FileNotFoundException ex) {
             ex.printStackTrace();
         }
     }
     
     /**
      * The starting point for the application.
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception e) { }
         damnApp dJ = new damnApp();
         dJ.go();
     }
     
 }
