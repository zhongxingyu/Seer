 /*
  * ChatBot.java
  *
  * Created on May 19, 2006, 5:16 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package com.thelangenbergs.jabberbot;
 
 import java.net.MalformedURLException;
 import org.jivesoftware.smack.*;
 import org.jivesoftware.smack.packet.*;
 import org.apache.log4j.*;
 import java.io.*;
 import java.util.*;
 import java.text.*;
 import org.jivesoftware.smackx.muc.MultiUserChat;
 
 /**
  * connect up to a MUC room and handle requests
  *
  * @author davel
  */
 public class ChatBot implements PacketListener {
 	private static Category logger = Category.getInstance(ChatBot.class.getName());
 	
 	private XMPPConnection conn;
 	private MultiUserChat muc;
 	
 	private static String keywords[] = {"getinfo", "time", "sleep", "fortune", "mail"};
 	
 	/**
 	 * Creates a new chatbot listener.
 	 * @param c - connection back to server for sending messages
 	 * @param m - connection to Chatroom for doing various things
 	 */
 	public ChatBot(XMPPConnection c,MultiUserChat m) {
 		logger.debug("listener registered:");
 		conn = c; //for sending back messages
 		muc = m;
 	}
 
 	/**
 	 * Processes an incoming packet from the chatroom. Determines if it's a message 
 	 * for us or not.
 	 * @param packet The incoming message from the chatroom.
 	 */
 	public void processPacket(Packet packet) {
 		//cast the packet as a Message
 		Message msg = (Message) packet;
 		logger.debug("received message "+msg.getBody());
 
 		if(msg.getBody().startsWith(getMyNick()) && !(msg.getFrom().equals(muc.getNickname()))){
 			//we have something to deal with
 			String command = getCommand(msg);
 			try{
 				if(validateCommand(msg,command)){
 					//we have a valid command -- do something with it
 					//the reason we spawn off a thread to do the dirty work is because some of our commands will
 					//take quite a bit of time to complete and we don't want to block
 					new CommandHandler(muc,msg,command);
 				}
 			}
 			catch(XMPPException e){
 				logger.error(e.getMessage(),e);
 			}
 		}
 	}
 	
 	/**
 	 * Checks to see if the keyword in the message is recnogized or not.  If the command
 	 * is not recnogized we will return an error message to the chatroom.
 	 * @param msg The incoming message that we are processing
 	 * @param command The command we're evaluating to see if it is something we can handle
 	 * @return True if we can deal with this command
 	 * @throws org.jivesoftware.smack.XMPPException if we have problems reporting an unrecnogized command back to the user
 	 */
 	protected boolean validateCommand(Message msg,String command) throws XMPPException {
 		boolean found = false;
 		for(int i=0; i<keywords.length; i++){
 			if(command.equalsIgnoreCase(keywords[i])){
 				found = true;
 			}
 		}
 		
 		if(!found){
			muc.sendMessage(getFrom(msg)+": unrecnogized command");
 		}
 		return found;
 	}
 	
 	/**
 	 * Return the nickname of the individual who sent the message.
 	 * @param msg The message who we're trying to identify the sender on.
 	 * @return the nickname of the person who sent the message.
 	 */
 	public static String getFrom(Message msg){
 		return msg.getFrom().substring(msg.getFrom().indexOf('/')+1);
 	}
 	
 	/**
 	 * gets the command out of the message.  Commands of the form nickname: <command>
 	 * @param m The Message which we are trying to extract the command from
 	 * @return The command from the message
 	 */
 	protected String getCommand(Message m){
 		StringTokenizer st = new StringTokenizer(m.getBody());
 		st.nextToken(); //our nickname
 		return st.nextToken(); //the command
 	}
 	
 	/**
 	 * Sets the nickname for us in the chatroom we are listening to.
 	 * @param nick The new nickname
 	 * @throws org.jivesoftware.smack.XMPPException if there was a problem setting the new nickname
 	 */
 	public void setMyNick(String nick) throws XMPPException{
 		muc.changeNickname(nick);
 	}
 	
 	/**
 	 * Return the nickname that we are connected to the room with
 	 * @return Our nickname for the room we are listening in
 	 */
 	public String getMyNick(){
 		return muc.getNickname();
 	}
 }
 
 class CommandHandler implements Runnable {
 	Thread t;
 	
 	private MultiUserChat conn;
 	private Message mesg;
 	private String cmd;
 	private static Category logger = Category.getInstance(CommandHandler.class.getName());
 	
 	public CommandHandler(MultiUserChat c, Message m, String command){
 		t = new Thread(this);
 		conn = c;
 		mesg = m;
 		cmd = command;
 		t.setName(cmd+" handler");
 		t.start();
 	}
 	
 	/**
 	 * handle the command do the work
 	 */
 	public void run(){
 		//ok so if we are in here we have a valid command as defined in ChatBot.keywords
 		try{
 			if(cmd.equals("sleep")){
 				
 				try{
 					t.sleep(10000);
 					conn.sendMessage("slept for 10 seconds");
 				} catch(Exception e){
 					logger.error(e.getMessage(), e);
 				}
 			} else if (cmd.equals("time")){
 				TimeZone tz = TimeZone.getTimeZone("GMT:00");
 				SimpleDateFormat sdf = new SimpleDateFormat("EE MMM d yyyy HH:mm:ss z");
 				sdf.setTimeZone(tz);
 				conn.sendMessage("The current time is: "+sdf.format(new Date()));
 			} 
 			else if(cmd.equals("fortune")){
 				getFortune();
 			}
 		}
 		catch(Exception e){
 			logger.error(e.getMessage(),e);
 			try{
 				conn.sendMessage("I'm sorry but I'm unable to complete your request -- please see my log for more details");
 			}
 			catch(XMPPException ex){
 				logger.error(ex.getMessage(),ex);
 			}
 		}
 	}
 	
 	private void getFortune() throws XMPPException {
 		Runtime r = Runtime.getRuntime();
 		
 		String command = "fortune";
 		
 		logger.debug("execing command "+command);
 		
 		try{
 			Process p = r.exec(command);
 			
 			//get the output
 			logger.debug("opening output from command for reading");
 			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
 			
 			String message = "";
 			String line;
 			while((line = br.readLine()) != null){
 				message += line+"\n";
 			}
 			
 			br.close();
 			conn.sendMessage(message);
 		}
 		catch(IOException e){
 			logger.warn(e.getMessage(),e);
 			conn.sendMessage(mesg.getFrom()+": I'm sorry, but it looks like fortune is not installed on my machine");
 		}
 		
 	}
 }
