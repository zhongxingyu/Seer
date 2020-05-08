 /*
    Copyright 2003-2009 IrcAppender project
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 
  */
 package net.sf.ircappender;
 
 import org.apache.log4j.spi.LoggingEvent;
 import org.jibble.pircbot.*;
 
 /**
  * @author W B Chmura
  */
 public class IrcAppenderBot extends PircBot implements Runnable {
 
 	private boolean isRunning;
 	private boolean isChannelNotEmpty = true;
 	
 	private Fifo eventQue = null;
 	private String channel;
 
 
 	private LoggingEvent le;
 
 	/**
 	 * Constructs the PIRCBOT then calls setName
 	 * @param varNickname The name the bot should use
 	 */
 	public IrcAppenderBot(String varNickname) {
 		super();
 		this.setName(varNickname);
 		System.out.println("Irc Appender Bot: constructing");
 	}
 	
 	
 	/**
 	 * Constructs the PIRCBOT then calls setName with a default of Log4JChatBot 
 	 * @param varNickname The name the bot should use
 	 */
 	public IrcAppenderBot() {
 		super();
 		this.setName("Log4JChatBot");
 		System.out.println("Irc Appender Bot: constructing");
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Runnable#run()
 	 */
 	public void run() {
 
 		setRunning(true);
 				
 		while (isRunning) {
 		
 		 if(channelNotEmpty(channel)) {
 			
 			if( ! (eventQue == null) && ! eventQue.isEmpty() && (this.getOutgoingQueueSize() == 0) ) {
 				transferEntry();
 			}
 			else
 			{
 				if (! (eventQue == null)) System.out.println("Outgoing que: " + this.getOutgoingQueueSize() + " Event Que:" + eventQue.size());
 				try {
 					Thread.sleep(1000);
 				}
 				catch (Exception e) {
 					System.out.println("IrcAppenderBot exception " + e.getMessage());
 				} //End try
 			} // End If
 		  } // Usercheck
 		} // Running loop
 		
 		while (! eventQue.isEmpty() && isChannelNotEmpty) {
 			transferEntry();
 		}
 				
 		while ( this.getOutgoingQueueSize() > 0  && isChannelNotEmpty) {
 						
 			System.out.println("waiting for buffer to drain");
 			try {
 				Thread.sleep(1000);
 			}
 			catch (Exception e) {
 				System.out.println("IrcAppenderBot exception " + e.getMessage());
 			} //End try			
 
 		}
 			
 		System.out.println("IrcAppenderBot shutting down");
 		this.quitServer();
 		
 	}
 
   /**
    * Transfers one LoggingEvent Entry from the eventQue to the ircMessage que
   */
 	private void transferEntry () {
		le = (LoggingEvent) eventQue.pop();
 		sendMessage (channel,(String) le.getMessage());
 	}
 
 
 	public void onMessage(String channel, String sender,
 							 String login, String hostname, String message) {
 		 if (message.equalsIgnoreCase("time")) {
 			  String time = new java.util.Date().toString();
 			  sendMessage(channel, sender + ": The time is now " + time);
 		 }
 	}
 
 
 	/**
 	 * Checks the channel user list to see if there are less than two users registered. 
 	 * @param channel The channel name to check
 	 * @return boolean true if there are more than one users in the channel
 	 */
 
 
 	public boolean channelNotEmpty(String channel) {
 		boolean state;
 		User[] users = this.getUsers(channel);
 		
 		if (users.length < 2) {
 			System.out.println("We are alone");
 			state = false;
 			try {
 				Thread.sleep(2000);
 			}
 			catch (Exception e) {
 				System.out.println("IrcAppenderBot exception " + e.getMessage());
 			} //End try
 
 		}
 		else
 		{
 			state = true;
 		}
 				
 		isChannelNotEmpty = state;
 		
 		
 		return (state);
 	}
 
 
 
 	/**
 	 * Returns the running state of the thread
 	 * @return boolean
 	 */
 	public boolean isRunning() {
 		return isRunning;
 	}
 
 	/**
 	 * Sets the isRunning.
 	 * @param isRunning The isRunning to set
 	 */
 	public void setRunning(boolean isRunning) {
 		this.isRunning = isRunning;
 	}
 
 	/**
 	 * Sets the eventQue.
 	 * @param eventQue The eventQue to set
 	 */
 	public void setEventQue(Fifo vareventQue) {
 		this.eventQue = vareventQue;
 	}
 
 	/**
 	 * @return String
 	 */
 	public String getChannel() {
 		return channel;
 	}
 
 	/**
 	 * Sets the channel.
 	 * @param channel The channel to set
 	 */
 	public void setChannel(String channel) {
 		this.channel = channel;
 	}
 
 }
