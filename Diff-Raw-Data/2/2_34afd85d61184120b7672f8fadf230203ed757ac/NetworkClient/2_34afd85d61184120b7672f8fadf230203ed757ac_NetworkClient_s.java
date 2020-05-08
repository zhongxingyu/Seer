 /*
    Copyright 2011 James Cowgill
 
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
 package uk.org.cowgill.james.jircd.network;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.channels.SocketChannel;
 import java.nio.charset.CharacterCodingException;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.nio.charset.CharsetEncoder;
 import java.security.SecureRandom;
 import java.util.Arrays;
 
 import org.apache.log4j.Logger;
 
 import uk.org.cowgill.james.jircd.Client;
 import uk.org.cowgill.james.jircd.ConnectionClass;
 import uk.org.cowgill.james.jircd.IRCMask;
 import uk.org.cowgill.james.jircd.Message;
 import uk.org.cowgill.james.jircd.Server;
 
 /**
  * A networking client implementation
  * 
  * @author James
  */
 class NetworkClient extends Client
 {
 	private static final ConnectionClass DEFAULT_CONN_CLASS = new ConnectionClass();
 	
 	private static final SecureRandom randomGen = new SecureRandom();
 	
 	/**
 	 * Timeout after a ping has been sent to the client
 	 */
 	public static final int AFTER_PING_TIMEOUT = 5;
 	
 	//----------------------------------
 
 	private static final Logger logger = Logger.getLogger(NetworkClient.class);
 	
 	/**
 	 * UTF-8 character set encoder
 	 */
 	private static final CharsetEncoder cEncoder = Charset.forName("UTF-8").newEncoder();
 	
 	/**
 	 * UTF-8 character set decoder
 	 */
 	private static final CharsetDecoder cDecoder = Charset.forName("UTF-8").newDecoder();
 	
 	/**
 	 * ByteBuffer containing a carriage return then a line feed
 	 */
 	private static final byte[] CRLF = new byte[] { '\r', '\n' };
 
 	//-----------------------------------
 	
 	/**
 	 * Channel this client is connected to
 	 */
 	private SocketChannel channel;
 
 	/**
 	 * Data for byte buffer
 	 */
 	private byte[] localBufferData = new byte[1025];
 	
 	/**
 	 * Byte buffer to recent messages
 	 */
 	private ByteBuffer localBuffer = ByteBuffer.wrap(localBufferData);
 	
 	/**
 	 * Time of the last message to be received by the server
 	 */
 	private long lastMessageTime;
 	
 	/**
 	 * Timer used for the flood limiter
 	 */
 	private FloodTimer floodTimer = new FloodTimer(this);
 	
 	/**
 	 * Spoof check string
 	 */
 	String spoofCheckChars;
 	
 	/**
 	 * Default connection class
 	 */
 	private ConnectionClass defaultConnClass;
 	
 	/**
 	 * Current connection class
 	 */
 	private ConnectionClass connClass;
 	
 	//Static constructor
 	static
 	{
 		//Setup default connection class
 		DEFAULT_CONN_CLASS.maxLinks = Integer.MAX_VALUE;
 		DEFAULT_CONN_CLASS.pingFreq = 0;	//No "extra" ping frequency
 		DEFAULT_CONN_CLASS.readQueue = 1024;
 		DEFAULT_CONN_CLASS.sendQueue = 1024;
 	}
 
 	/**
 	 * Creates a new NetworkClient from a SocketChannel
 	 * 
 	 * You almost always want to call setup() after this
 	 * 
 	 * @param channel channel to setup from
 	 * @throws IOException thrown when an error occurs in setting socket options
 	 */
 	NetworkClient(SocketChannel channel) throws IOException
 	{
 		this(channel, 0);
 	}
 
 	/**
 	 * Creates a new NetworkClient from a SocketChannel
 	 * 
 	 * You almost always want to call setup() after this
 	 * 
 	 * @param channel channel to setup from
 	 * @param mode the initial mode of the client
 	 * @throws IOException thrown when an error occurs in setting socket options
 	 */
 	NetworkClient(SocketChannel channel, long mode) throws IOException
 	{
 		super(new IRCMask(), mode);
 		this.channel = channel;
 	}
 	
 	/**
 	 * Called to complete setting up a new connection
 	 * 
 	 * Only call immediately after creating the NetworkClient
 	 */
 	void setup() throws IOException
 	{
 		//Setup channel options
 		channel.configureBlocking(false);
 		channel.socket().setReceiveBufferSize(1024);
 		channel.socket().setSoLinger(true, 5);
 		changeClass(DEFAULT_CONN_CLASS, true);
 		
 		//Begin spoof check
 		final StringBuffer buffer = new StringBuffer(10);
 		for(int i = 0; i < 10; ++i)
 		{
 			buffer.append((char) (randomGen.nextInt('z' - 'A') + 'A'));
 		}
 		
 		spoofCheckChars = buffer.toString();
 		
 		send("PING :" + spoofCheckChars);
 	}
 	
 	/**
 	 * Called when a read event occurs
 	 */
 	void processReadEvent()
 	{
 		try
 		{
 			//Read data
 			switch(readWrapper(localBuffer))
 			{
 				case -1:
 					//Close client
 					close("Connection reset by peer");
 					return;
 					
 				case 0:
 					//Nothing received, so do nothing
 					return;
 			}
 		}
 		catch(IOException e)
 		{
 			logger.warn("Read error from socket", e);
 			close("Read error");
 			return;
 		}
 		
 		//Check exceeding ReadQ
 		if(localBuffer.remaining() <= 0)
 		{
 			//Close client
 			close("ReadQ Limit Exceeded");
 			return;
 		}
 		
 		//Check flood timer
 		if(!floodTimer.checkTimer())
 		{
 			return;
 		}
 		
 		//Read message into buffer
 		int endByte = localBuffer.position();
 		localBuffer.position(0);
 		
 		//Update message time
 		lastMessageTime = System.currentTimeMillis();
 		
 		//Find messages in buffer
 		for(int i = 1; i < endByte; i++)
 		{
 			//Check for end of message
 			if(localBufferData[i] == '\n' || localBufferData[i] == '\r')
 			{
 				//Set end of message + process buffer
 				localBuffer.limit(i);
 				processLocalBufferMessage();
 
 				//Reset position and limit
 				if(i != localBuffer.capacity() - 1)
 				{
 					localBuffer.limit(localBuffer.capacity());
 					localBuffer.position(i + 1);
 				}
 
 				//If we're now limited, break now
 				if(!floodTimer.checkTimer())
 					break;
 			}
 			else
 			{
 				//Check for msg size exceeded
 				if((i - localBuffer.position()) >= 512)
 				{
 					//Oversized message
 					close("Read error: Message size exceeded");
 					return;
 				}
 			}
 		}
 
 		//Copy data after position back to start
 		localBuffer.limit(endByte);
 		localBuffer.compact();
 
 		//Process closure queue
 		processCloseQueue();
 	}
 
 	private void processLocalBufferMessage()
 	{
 		//Ignore empty messages
 		if(localBuffer.remaining() == 0)
 			return;
 
 		//Decode message
 		Message msg;
 
 		try
 		{
 			msg = Message.parse(cDecoder.decode(localBuffer).toString());
 		}
 		catch(CharacterCodingException e)
 		{
 			//Drop message
 			return;
 		}
 
 		//Dispatch message
 		floodTimer.processMessage();
 		Server.getServer().getModuleManager().executeCommand(this, msg);
 	}
 
 	/**
 	 * Event which occurs when the ping timeouts need checking
 	 */
 	void pingCheckEvent()
 	{
 		//Check for ping timeout
 		long diffInSeconds = (System.currentTimeMillis() - lastMessageTime) / 1000;
 		
 		if(diffInSeconds >= connClass.pingFreq)
 		{
 			//Check if completely timed out
 			if(diffInSeconds >= connClass.pingFreq + AFTER_PING_TIMEOUT)
 			{
 				queueClose("Ping Timeout");
 			}
 			else if(isRegistered())
 			{
 				//Send ping
				send(Message.newStringFromServer("PING " + id.nick));
 			}
 		}
 	}
 	
 	@Override
 	public void send(Object data)
 	{
 		//Get string
 		CharSequence strData;
 		if(data instanceof CharSequence)
 		{
 			strData = (CharSequence) data;
 		}
 		else
 		{
 			strData = data.toString();
 		}
 		
 		try
 		{
 			//Encode object and write to socket with CRLF
 			ByteBuffer encoded = cEncoder.encode(CharBuffer.wrap(strData));
 			
 			if(!writeWrapper(encoded) || !writeWrapper(ByteBuffer.wrap(CRLF)))
 			{
 				queueClose("SendQ Limit Exceeded");
 			}
 		}
 		catch(CharacterCodingException e)
 		{
 			logger.warn("Error encoding message", e);
 		}
 		catch(IOException e)
 		{
 			//Error writing to message
 			queueClose("IO Error");
 		}
 	}
 
 	/**
 	 * Allows wrapping of the raw read operation
 	 * 
 	 * @param buffer buffer to read from
 	 * @return number of characters read
 	 */
 	protected int readWrapper(ByteBuffer buffer) throws IOException
 	{
 		return channel.read(buffer);
 	}
 	
 	/**
 	 * Allows wrapping of the raw write operation
 	 * 
 	 * @param buffer buffer to write
 	 * @return number of characters written
 	 */
 	protected boolean writeWrapper(ByteBuffer buffer) throws IOException
 	{
 		//Order of this comparison is important
 		return buffer.remaining() == channel.write(buffer);
 	}
 
 	@Override
 	protected boolean rawClose()
 	{
 		try
 		{
 			//Close channel
 			channel.socket().shutdownOutput();
 			channel.close();
 		}
 		catch(IOException e)
 		{
 		}
 		
 		return true;
 	}
 	
 	@Override
 	protected void registeredEvent()
 	{
 		//Purpose of this is to allow NetworkServer access to this method
 		super.registeredEvent();
 	}
 	
 	/**
 	 * Gets the remote address of this client
 	 * 
 	 * @return the remote address of this client
 	 */
 	InetAddress getRemoteAddress()
 	{
 		return channel.socket().getInetAddress();
 	}
 	
 	/**
 	 * Returns the ip address corresponding to a given channel
 	 * 
 	 * @param channel network channel to check
 	 * @return ip address string
 	 */
 	public static String getIpAddress(SocketChannel channel)
 	{
 		return channel.socket().getInetAddress().getHostAddress();
 	}
 	
 	@Override
 	public String getIpAddress()
 	{
 		return channel.socket().getInetAddress().getHostAddress();
 	}
 
 	private void forceChangeClass(ConnectionClass clazz)
 	{
 		//Check if already in class
 		if(connClass == clazz)
 		{
 			return;
 		}
 		
 		//Update link count
 		clazz.currentLinks++;
 		
 		if(connClass != null)
 		{
 			connClass.currentLinks--;
 		}
 		
 		//Update buffer sizes
 		try
 		{
 			localBufferData = Arrays.copyOf(localBufferData, clazz.readQueue + 1);
 			localBuffer = ByteBuffer.wrap(localBufferData);
 			
 			channel.socket().setSendBufferSize(clazz.sendQueue);
 		}
 		catch(IOException e)
 		{
 			logger.error("Error setting buffer sizes for client " + id.toString());
 		}
 		
 		//Class changing causes an update in last message time
 		lastMessageTime = System.currentTimeMillis();
 		
 		//Set class
 		connClass = clazz;
 	}
 	
 	@Override
 	protected boolean changeClass(ConnectionClass clazz, boolean defaultClass)
 	{
 		//Check link count
 		if(clazz.currentLinks >= clazz.maxLinks)
 		{
 			return false;
 		}
 		
 		//Force class change
 		forceChangeClass(clazz);
 		
 		//Copy default class
 		if(defaultClass)
 		{
 			this.defaultConnClass = clazz;
 		}
 		
 		return true;
 	}
 	
 	@Override
 	public void restoreClass()
 	{
 		forceChangeClass(defaultConnClass);
 	}
 	
 	@Override
 	public long getIdleTime()
 	{
 		return System.currentTimeMillis() - lastMessageTime;
 	}
 }
