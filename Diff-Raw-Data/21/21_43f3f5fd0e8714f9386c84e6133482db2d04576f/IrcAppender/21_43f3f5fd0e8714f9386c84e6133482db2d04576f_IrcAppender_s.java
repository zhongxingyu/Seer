 /*
    Copyright 2003-2011 IrcAppender project
 
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
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 
 import net.sf.ircappender.impl.Fifo;
 import net.sf.ircappender.impl.IrcConnection;
 
 import org.apache.log4j.AppenderSkeleton;
 import org.apache.log4j.spi.LoggingEvent;
 
 /**
  * appends log-events to an irc channel.
  */
 public class IrcAppender extends AppenderSkeleton {
 
 	private boolean debug = false;
 
 	private String host;
 
 	private int port = 6667;
 
 	private boolean ssl = false;
 
 	private String username;
 
 	private String password;
 
 	private String channel;
 
 	private String buffertype = "autopop";
 
 	private String nickname;
 
 	private long messageDelay = 1000;
 
 	private int buffersize = 1000;
 
 	private boolean isClosing;
 
	private Fifo eventQue = null;
 
 	private IrcConnection irc = null;
 
 	/**
 	 * Calls the superclass constructor.  No other logic at this time
 	 */
 	public IrcAppender() {
 		super();
		if (debug) {
			System.out.println("Irc Appender constructing");
		}
 	}
 
 
 	/**
 	 * Initializes the system after the options are set
 	 * @see org.apache.log4j.spi.OptionHandler#activateOptions()
 	 */
 	public void activateOptions() {
 
 		if (debug) {
 			System.out.println("Activate options");
 		}
 
 		if (buffertype.equalsIgnoreCase("autopop")) {
			eventQue = new Fifo(buffersize, Fifo.AUTOPOP);
 		} else if (buffertype.equalsIgnoreCase("refuse")) {
			eventQue = new Fifo(buffersize, Fifo.REFUSE);
 		} else {
			eventQue = new Fifo(buffersize, Fifo.AUTOPOP);
 		}
 
 		irc = doIrcBotInitialization();
		irc.setEventQueue(eventQue);
 		try {
 			irc.connect();
 			irc.login();
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Handles the initialization of the IrcBot object
 	 *
 	 * @return IrcAppenderBot the Initialized bot
 	 */
 	private IrcConnection doIrcBotInitialization() {
 		IrcConnection bot = new IrcConnection(host, port, ssl, username, password, nickname, channel);
 		bot.setDebug(debug);
 		bot.setMessageDelay(messageDelay);
 		return bot;
 	}
 
 
 
 	/**
 	 * Recieves a LoggingEvent for handling.  Ignores if the appender is in the
 	 * process of closing down.
 	 * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
 	 */
 	protected void append(LoggingEvent arg0) {
 		if (!isClosing) {
			eventQue.add(arg0);
 		}
 	}
 
 	/**
 	 * no layout required
 	 */
 	public boolean requiresLayout() {
 		return false;
 	}
 
 	/**
 	 * Shuts down the appender.  Handles telling the bot to close and waiting for the bot
 	 * to cleanly close its thread before continuing.
 	 *
 	 * @see org.apache.log4j.Appender#close()
 	 */
 	public void close() {
 		isClosing = true;
 		irc.setRunning(false);
 		irc.waitForForwardThreadToFinish();
 		irc.disconnect();
 	}
 
 	/**
 	 * Returns the configured buffersize
 	 *
 	 * @return double
 	 */
 	public int getBuffersize() {
 		return buffersize;
 	}
 
 	/**
 	 * Returns the configured channel
 	 *
 	 * @return String
 	 */
 	public String getChannel() {
 		if ((channel != null) && !channel.startsWith("#")) {
 			channel = "#" + channel;
 		}
 		return channel;
 	}
 
 	/**
 	 * is debugging enabled
 	 *
 	 * @return true, if debugging is enabled, false otherwise
 	 */
 	public boolean isDebug() {
 		return debug;
 	}
 
 	/**
 	 * enables or disables debugging messages
 	 *
 	 * @param debug should debug message be written to stdout?
 	 */
 	public void setDebug(boolean debug) {
 		this.debug = debug;
 	}
 
 
 	/**
 	 * Returns the configured host
 	 *
 	 * @return String
 	 */
 	public String getHost() {
 		return host;
 	}
 
 	/**
 	 * Returns the configured irc server port
 	 *
 	 * @return int
 	 */
 	public int getPort() {
 		return port;
 	}
 
 	/**
 	 * Is SSL used to secure the connection to the IRC server?
 	 *
 	 * @return ssl ussage
 	 */
 	public boolean isSsl() {
 		return ssl;
 	}
 
 	/**
 	 * Enables or disables the use of SSL for the IRC connection
 	 *
 	 * @param ssl true to enable SSL security
 	 */
 	public void setSsl(boolean ssl) {
 		this.ssl = ssl;
 	}
 
 
 	/**
 	 * gets the IRC password
 	 *
 	 * @return password
 	 */
 	public String getPassword() {
 		return password;
 	}
 
 	/**
 	 * gets the IRC username
 	 *
 	 * @return username
 	 */
 	public String getUsername() {
 		return username;
 	}
 
 	/**
 	 * Sets the buffersize.
 	 *
 	 * @param buffersize The buffersize to set
 	 */
 	public void setBuffersize(int buffersize) {
 		this.buffersize = buffersize;
 
 	}
 
 	/**
 	 * Sets the channel.
 	 *
 	 * @param channel The channel to set
 	 */
 	public void setChannel(String channel) {
 		this.channel = channel;
 	}
 
 	/**
 	 * Sets the host.
 	 *
 	 * @param host The host to set
 	 */
 	public void setHost(String host) {
 		this.host = host;
 	}
 
 	/**
 	 * Sets the irc server port
 	 *
 	 * @param port port
 	 */
 	public void setPort(int port) {
 		this.port = port;
 	}
 
 	/**
 	 * Sets the password.
 	 *
 	 * @param password The password to set
 	 */
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	/**
 	 * Sets the username.
 	 *
 	 * @param username The username to set
 	 */
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	/**
 	 * gets the name of the buffer strategy
 	 *
 	 * @return String
 	 */
 	public String getBuffertype() {
 		return buffertype;
 	}
 
 	/**
 	 * Sets the buffer strategy.
 	 *
 	 * @param buffertype The buffertype to set
 	 */
 	public void setBuffertype(String buffertype) {
 		this.buffertype = buffertype;
 	}
 
 	/**
 	 * gets the IRC nickname of the bot
 	 *
 	 * @return nickname
 	 */
 	public String getNickname() {
 		return nickname;
 	}
 
 	/**
 	 * Sets the nickname.
 	 *
 	 * @param nickname The nickname to set
 	 */
 	public void setNickname(String nickname) {
 		this.nickname = nickname;
 	}
 
 	/**
 	* Set the delay between messages. Note that some IRC servers have a rate limit which may be stricter.
 	*
 	* @param messageDelay in milliseconds
 	*/
 	public void setMessageDelay(long messageDelay) {
 		this.messageDelay = messageDelay;
 	}
 
 	/**
 	 * gets the delay between multiple messages
 	 *
 	 * @return long delay in milliseconds
 	 */
 	public long getMessageDelay() {
 		return messageDelay;
 	}
 }
