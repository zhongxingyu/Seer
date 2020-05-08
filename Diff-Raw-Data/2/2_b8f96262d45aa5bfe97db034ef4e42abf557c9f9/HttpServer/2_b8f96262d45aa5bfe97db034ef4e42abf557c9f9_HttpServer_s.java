 /*
 Copyright 2009 Samuel Marshall
 http://www.leafdigital.com/software/hawthorn/
 
 This file is part of Hawthorn.
 
 Hawthorn is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 Hawthorn is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with Hawthorn.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.leafdigital.hawthorn.server;
 
 import java.io.*;
 import java.net.*;
 import java.nio.ByteBuffer;
 import java.nio.channels.*;
 import java.util.*;
 import java.util.regex.*;
 
 import com.leafdigital.hawthorn.util.Auth;
 
 /** Server that accepts incoming HTTP requests and dispatches them as events. */
 public final class HttpServer extends HawthornObject
 	implements Statistics.InstantStatisticHandler
 {
 	private final static int CONNECTION_TIMEOUT = 90000, CLEANUP_EVERY = 30000,
 		LOGTIME_EVERY = 10000;
 	private final static String STATISTIC_CONNECTION_COUNT = "CONNECTION_COUNT";
 	/** Content type for UTF-8 JavaScript */
 	final static String CONTENT_TYPE_JAVASCRIPT = "application/javascript; charset=UTF-8";
 	/** Content type for UTF-8 HTML */
 	final static String CONTENT_TYPE_HTML = "text/html; charset=UTF-8";
 
 	/** Statistic: request time for all HTTP events from users */
 	final static String STATISTIC_USER_REQUEST_TIME = "USER_REQUEST_TIME";
 	/** Statistic: request time for all HTTP events from servers */
 	final static String STATISTIC_SERVER_REQUEST_TIME = "SERVER_REQUEST_TIME";
 	/** Statistic: size of close queue */
 	final static String STATISTIC_CLOSE_QUEUE_SIZE = "CLOSE_QUEUE_SIZE";
 	/** Statistic: request time for specific type */
 	final static String STATISTIC_SPECIFIC_REQUEST = "REQUEST_TIME_";
 	/** Statistic: main thread busy percentage */
 	final static String STATISTIC_MAIN_THREAD_BUSY_PERCENT = "MAIN_THREAD_BUSY_PERCENT";
 
 	private final static int BACKLOG = 256;
 
 	private final static Pattern REGEXP_HTTPREQUEST =
 		Pattern.compile("GET (.+) HTTP/1\\.[01]");
 	private final static Pattern REGEXP_SERVERAUTH =
 		Pattern.compile("\\*([0-9]{1,18})\\*([a-f0-9]{40})");
 
 	private Selector selector;
 	private ServerSocketChannel server;
 
 	private HashMap<SelectionKey, Connection> connections =
 		new HashMap<SelectionKey, Connection>();
 
 	private LinkedList<SelectionKey> keysToClose =
 		new LinkedList<SelectionKey>();
 
 	private LinkedList<SelectableChannel> channelsToClose =
 		new LinkedList<SelectableChannel>();
 
 	private Object closeSynch = new Object(), timeLogSynch = new Object();
 
 	private boolean close, closed, closeThreadClosed;
 
 	private int timeBusy, timeInSelect;
 
 	/**
 	 * @param app Main app object
 	 * @throws StartupException If there is a problem binding the socket
 	 */
 	public HttpServer(Hawthorn app) throws StartupException
 	{
 		super(app);
 		getStatistics().registerInstantStatistic(STATISTIC_CONNECTION_COUNT, this);
 		getStatistics().registerTimeStatistic(STATISTIC_USER_REQUEST_TIME);
 		if(getConfig().getOtherServers().length > 0)
 		{
 			getStatistics().registerTimeStatistic(STATISTIC_SERVER_REQUEST_TIME);
 		}
 		if(getConfig().isDetailedStats())
 		{
 			getStatistics().registerTimeStatistic(STATISTIC_SPECIFIC_REQUEST +
 				HttpEvent.SAY);
 			getStatistics().registerTimeStatistic(STATISTIC_SPECIFIC_REQUEST +
 				HttpEvent.BAN);
 			getStatistics().registerTimeStatistic(STATISTIC_SPECIFIC_REQUEST +
 				HttpEvent.LEAVE);
 			getStatistics().registerTimeStatistic(STATISTIC_SPECIFIC_REQUEST +
 				HttpEvent.POLL);
 			getStatistics().registerTimeStatistic(STATISTIC_SPECIFIC_REQUEST +
 				HttpEvent.WAIT);
 			getStatistics().registerTimeStatistic(STATISTIC_SPECIFIC_REQUEST +
 				HttpEvent.RECENT);
 			getStatistics().registerTimeStatistic(STATISTIC_SPECIFIC_REQUEST +
 				HttpEvent.LOG);
 			getStatistics().registerTimeStatistic(STATISTIC_SPECIFIC_REQUEST +
 				HttpEvent.STATISTICS);
 		}
 		getStatistics().registerInstantStatistic(STATISTIC_CLOSE_QUEUE_SIZE,
 			new Statistics.InstantStatisticHandler()
 			{
 				public int getValue()
 				{
 					synchronized(connections)
 					{
 						return channelsToClose.size() + keysToClose.size();
 					}
 				}
 			});
 		getStatistics().registerInstantStatistic(STATISTIC_MAIN_THREAD_BUSY_PERCENT,
 			new Statistics.InstantStatisticHandler()
 			{
 				public int getValue()
 				{
 					synchronized(timeLogSynch)
 					{
 						int total = timeBusy + timeInSelect;
						int percent = (timeBusy*100 + total/2) / total;
 						timeBusy = 0;
 						timeInSelect = 0;
 						return percent;
 					}
 				}
 			});
 
 		try
 		{
 			selector = Selector.open();
 			server = ServerSocketChannel.open();
 			server.configureBlocking(false);
 			server.socket().bind(
 				new InetSocketAddress(getConfig().getThisServer().getAddress(),
 					getConfig().getThisServer().getPort()), BACKLOG);
 			server.register(selector, SelectionKey.OP_ACCEPT);
 		}
 		catch(IOException e)
 		{
 			throw new StartupException(ErrorCode.STARTUP_CANNOTBIND,
 				"Failed to initialise server socket.", e);
 		}
 
 		Thread t = new Thread(new Runnable()
 		{
 			public void run()
 			{
 				serverThread();
 			}
 		}, "Main server thread");
 
 		Thread t2 = new Thread(new Runnable()
 		{
 			public void run()
 			{
 				closeThread();
 			}
 		}, "Connection closer thread");
 
 		// Increase the main server thread priority (this is because there is only
 		// one main server thread versus numerous event threads; and I want the
 		// recorded time from connect to HTTP result handling to be as accurate
 		// as possible).
 		t.setPriority(Thread.MAX_PRIORITY);
 		t2.setPriority(Thread.MAX_PRIORITY);
 
 		t.start();
 		t2.start();
 	}
 
 	/**
 	 * A single connection to the HTTP server.
 	 */
 	public class Connection
 	{
 		private final static int BUFFERSIZE = 8192;
 
 		private SelectionKey key;
 		private SocketChannel channel;
 		private ByteBuffer buffer;
 
 		private long lastAction;
 		private String hostAddress;
 		private boolean otherServer, serverAuthenticated;
 
 		private final static String CRLF = "\r\n";
 
 		/**
 		 * @param key Selection key
 		 */
 		private Connection(SelectionKey key)
 		{
 			this.key = key;
 			this.channel = (SocketChannel)key.channel();
 			lastAction = System.currentTimeMillis();
 			buffer = ByteBuffer.allocate(BUFFERSIZE);
 			hostAddress = channel.socket().getInetAddress().getHostAddress();
 		}
 
 		/** Closes the connection */
 		public void close()
 		{
 			closeChannel(key);
 		}
 
 		/**
 		 * Sends an HTTP response on this connection and closes it. Note that all
 		 * responses, even errors, use HTTP 200 OK. This is because we want the
 		 * JavaScript, not browser, to handle the error.
 		 *
 		 * @param data Data to send (will be turned into UTF-8)
 		 */
 		public void send(String data)
 		{
 			send(200, data, CONTENT_TYPE_JAVASCRIPT);
 		}
 
 		/**
 		 * Sends an HTTP response on this connection and closes it.
 		 *
 		 * @param code HTTP code. Use 200 except for fatal errors where we don't
 		 *        know which callback function to call
 		 * @param data Data to send (will be turned into UTF-8)
 		 * @param contentType Content type to send
 		 * @throws IllegalArgumentException If the HTTP code isn't supported
 		 */
 		public void send(int code, String data, String contentType) throws IllegalArgumentException
 		{
 			try
 			{
 				// Get data
 				byte[] dataBytes = data.getBytes("UTF-8");
 
 				// Get header
 				StringBuilder header = new StringBuilder();
 
 				String codeText;
 				switch(code)
 				{
 				case 200:
 					codeText = "OK";
 					break;
 				case 403:
 					codeText = "Access denied";
 					break;
 				case 404:
 					codeText = "Not found";
 					break;
 				case 500:
 					codeText = "Internal server error";
 					break;
 				default:
 					throw new IllegalArgumentException("Unsupported HTTP code " + code);
 				}
 
 				header.append("HTTP/1.1 ");
 				header.append(code);
 				header.append(' ');
 				header.append(codeText);
 				header.append(CRLF);
 
 				header.append("Connection: close");
 				header.append(CRLF);
 
 				header.append("Content-Type: ");
 				header.append(contentType);
 				header.append(CRLF);
 
 				header.append("Content-Length: ");
 				header.append(dataBytes.length);
 				header.append(CRLF);
 
 				header.append(CRLF);
 				byte[] headerBytes = header.toString().getBytes("US-ASCII");
 
 				// Combine the two
 				ByteBuffer response =
 					ByteBuffer.allocate(dataBytes.length + headerBytes.length);
 				response.put(headerBytes);
 				response.put(dataBytes);
 				response.flip();
 
 				// Send data
 				while(true)
 				{
 					try
 					{
 						channel.write(response);
 					}
 					catch(IOException e)
 					{
 						getLogger().log(Logger.SYSTEM_LOG, Logger.Level.NORMAL,
 							this + ": Error writing data");
 						close();
 						return;
 					}
 
 					// Close connection if needed
 					if(!response.hasRemaining())
 					{
 						close();
 						return;
 					}
 
 					// Still some data? OK, wait a bit and try again (yay polling -
 					// but this should probably never really happen)
 					try
 					{
 						System.err.println("Doing the sleep thing");
 						Thread.sleep(50);
 					}
 					catch(InterruptedException ie)
 					{
 					}
 				}
 			}
 			catch(UnsupportedEncodingException e)
 			{
 				throw new Error("Basic encoding not supported?!", e);
 			}
 		}
 
 		private void read()
 		{
 			if(buffer == null)
 			{
 				close();
 				return;
 			}
 
 			int read;
 			try
 			{
 				read = channel.read(buffer);
 			}
 			catch(IOException e)
 			{
 				// Connection got closed, or something else went wrong
 				close();
 				return;
 			}
 			if(read == -1)
 			{
 				close();
 				return;
 			}
 			if(read == 0)
 			{
 				return;
 			}
 			lastAction = System.currentTimeMillis();
 
 			byte[] array = buffer.array();
 			int bufferPos = buffer.position();
 
 			// Might this be another server introducing itself?
 			if(!otherServer && bufferPos > 0 && array[0] == '*')
 			{
 				if(!getConfig().isOtherServer(channel.socket().getInetAddress()))
 				{
 					getLogger().log(Logger.SYSTEM_LOG, Logger.Level.NORMAL,
 						this + ": Remote server connection from disallowed IP");
 					close();
 					return;
 				}
 
 				otherServer = true;
 			}
 
 			if(otherServer)
 			{
 				handleServer(array, bufferPos);
 			}
 			else
 			{
 				handleUser(array, bufferPos);
 			}
 		}
 
 		/**
 		 * User communication follows HTTP.
 		 *
 		 * @param array Data buffer
 		 * @param bufferPos Length of buffer that is filled
 		 */
 		private void handleUser(byte[] array, int bufferPos)
 		{
 			if(array[bufferPos - 1] == '\n' && array[bufferPos - 2] == '\r'
 				&& array[bufferPos - 3] == '\n' && array[bufferPos - 4] == '\r')
 			{
 				// Obtain GET/POST line
 				int i;
 				for(i = 0; array[i] != '\r'; i++)
 				{
 					;
 				}
 				try
 				{
 					String firstLine = new String(array, 0, i, "US-ASCII");
 					Matcher m = REGEXP_HTTPREQUEST.matcher(firstLine);
 					if(!m.matches())
 					{
 						getLogger().log(Logger.SYSTEM_LOG, Logger.Level.NORMAL,
 							this + ": Invalid request line: " + firstLine);
 						close();
 						return;
 					}
 
 					String ipHeader = getConfig().getIpHeader();
 					if(ipHeader != null)
 					{
 						// Get rest of header in lower-case
 						String remainingHeader = new String(array, i+2, bufferPos-(i+2),
 							"US-ASCII").toLowerCase();
 						// Find header
 						int pos = remainingHeader.indexOf(ipHeader.toLowerCase()+":");
 						if(pos != -1)
 						{
 							int cr = remainingHeader.indexOf('\r',
 								pos + ipHeader.length() + 1);
 							if(cr != -1)
 							{
 								hostAddress = remainingHeader.substring(
 									pos + ipHeader.length() + 1, cr).trim();
 							}
 						}
 					}
 
 					buffer = null;
 					receivedRequest(m.group(1));
 					return;
 				}
 				catch(UnsupportedEncodingException e)
 				{
 					throw new Error("Missing US-ASCII support", e);
 				}
 			}
 			else
 			{
 				// Not received valid request yet. If we've received the full buffer,
 				// give up on it.
 				if(bufferPos == BUFFERSIZE)
 				{
 					getLogger().log(Logger.SYSTEM_LOG, Logger.Level.NORMAL,
 						this + ": Received large invalid request");
 					close();
 					return;
 				}
 			}
 		}
 
 		/**
 		 *
 		 * @param array Data buffer
 		 * @param bufferPos Length of buffer that is filled
 		 */
 		private void handleServer(byte[] array, int bufferPos)
 		{
 			int pos = 0;
 			while(true)
 			{
 				int linefeed;
 				for(linefeed = pos; linefeed < bufferPos; linefeed++)
 				{
 					if(array[linefeed] == '\n')
 					{
 						break;
 					}
 				}
 				// If there are no more lines, exit
 				if(linefeed == bufferPos)
 				{
 					// Clean up the buffer to remove used data.
 					System.arraycopy(array, pos, array, 0, bufferPos - pos);
 					buffer.position(bufferPos - pos);
 
 					// Exit
 					return;
 				}
 
 				// Process line [UTF-8]
 				try
 				{
 					String line = new String(array, pos, linefeed - pos, "UTF-8");
 					pos = linefeed + 1;
 					if(serverAuthenticated)
 					{
 						// Pass this to event-handler
 						getEventHandler().addEvent(new ServerEvent(getApp(), line, this));
 					}
 					else
 					{
 						// This must be authentication method
 						Matcher m = REGEXP_SERVERAUTH.matcher(line);
 						if(m.matches())
 						{
 							// Check time. This is there both to ensure the security check
 							// isn't easily reproducible - which it's a bit crap for, since
 							// I didn't make sure that times aren't reused - and to ensure
 							// that clocks are in synch, because if they aren't, behaviour
 							// will be weird.
 							long time = Long.parseLong(m.group(1));
 							if(Math.abs(time - System.currentTimeMillis()) > 5000)
 							{
 								getLogger()
 									.log(
 										Logger.SYSTEM_LOG,
 										Logger.Level.ERROR,
 										this
 											+ ": Remote server reports incorrect time (>5 seconds "
 											+ "out). You must use network time synchronization for all "
 											+ "servers.");
 								close();
 								return;
 							}
 
 							// Build hash using time and IP address
 							String valid = getApp().getValidKey("remote server", toString(),
 								"", "", Auth.getPermissionSet(""), time);
 							if(!valid.equals(m.group(2)))
 							{
 								getLogger().log(Logger.SYSTEM_LOG, Logger.Level.ERROR,
 									this + ": Invalid remote server authorisation key: " + line);
 							}
 
 							serverAuthenticated = true;
 							getLogger().log(Logger.SYSTEM_LOG, Logger.Level.NORMAL,
 								this + ": Successful remote server login");
 						}
 						else
 						{
 							getLogger().log(Logger.SYSTEM_LOG, Logger.Level.ERROR,
 								this + ": Invalid remote server auth line: " + line);
 							close();
 							return;
 						}
 
 					}
 				}
 				catch(UnsupportedEncodingException e)
 				{
 					throw new Error("Missing UTF-8 support", e);
 				}
 			}
 		}
 
 		@Override
 		/**
 		 * @return Internet address (numeric) of this connection
 		 */
 		public String toString()
 		{
 			return hostAddress;
 		}
 
 		private void receivedRequest(String request)
 		{
 			getLogger().log(Logger.SYSTEM_LOG, Logger.Level.DETAIL,
 				this + ": Requested " + request);
 			getEventHandler().addEvent(new HttpEvent(getApp(), request, this));
 		}
 
 		private boolean checkTimeout(long now)
 		{
 			if(!serverAuthenticated && now - lastAction > CONNECTION_TIMEOUT)
 			{
 				getLogger().log(Logger.SYSTEM_LOG, Logger.Level.NORMAL,
 					channel.socket().getInetAddress().getHostAddress() + " (timeout)");
 				close();
 				return true;
 			}
 			else
 			{
 				return false;
 			}
 		}
 	}
 
 
 	private void serverThread()
 	{
 		long lastCleanup = System.currentTimeMillis();
 		long lastTime = lastCleanup;
 		int localTimeInSelect = 0, localTimeBusy = 0;
 
 		try
 		{
 			while(true)
 			{
 				cancelKeys();
 
 				long beforeSelect = System.currentTimeMillis();
 				localTimeBusy += (int)(beforeSelect - lastTime);
 
 				selector.select(5000);
 
 				lastTime = System.currentTimeMillis();
 				localTimeInSelect += (int)(lastTime - beforeSelect);
 				if(close)
 				{
 					closed = true;
 					return;
 				}
 
 				for(SelectionKey key : selector.selectedKeys())
 				{
 					if((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT)
 					{
 						try
 						{
 							Socket newSocket = server.socket().accept();
 							newSocket.getChannel().configureBlocking(false);
 							SelectionKey newKey =
 								newSocket.getChannel().register(selector, SelectionKey.OP_READ);
 							Connection newConnection = new Connection(newKey);
 							synchronized (connections)
 							{
 								connections.put(newKey, newConnection);
 							}
 						}
 						catch(IOException e)
 						{
 							getLogger().log(Logger.SYSTEM_LOG, Logger.Level.ERROR,
 								"Failed to accept connection", e);
 						}
 					}
 					if((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ)
 					{
 						Connection c;
 						synchronized (connections)
 						{
 							c = connections.get(key);
 							if(c == null)
 							{
 								continue;
 							}
 						}
 						c.read();
 					}
 				}
 				selector.selectedKeys().clear();
 
 				if(lastTime - lastCleanup > CLEANUP_EVERY)
 				{
 					lastCleanup = lastTime;
 					LinkedList<Connection> consider;
 					synchronized (connections)
 					{
 						consider = new LinkedList<Connection>(connections.values());
 					}
 					for(Connection connection : consider)
 					{
 						connection.checkTimeout(lastTime);
 					}
 				}
 
 				if(localTimeBusy + localTimeInSelect > LOGTIME_EVERY)
 				{
 					synchronized (timeLogSynch)
 					{
 						timeBusy += localTimeBusy;
 						timeInSelect += localTimeInSelect;
 						localTimeBusy = 0;
 						localTimeInSelect = 0;
 					}
 				}
 			}
 		}
 		catch(Throwable t)
 		{
 			getLogger().log(Logger.SYSTEM_LOG, Logger.Level.FATAL_ERROR,
 				"Fatal error in main server thread", t);
 			// If the main thread crashed, better exit the whole server
 			closed = true;
 			getApp().close();
 		}
 	}
 
 	private void cancelKeys()
 	{
 		boolean some = false;
 		synchronized (connections)
 		{
 			while(!keysToClose.isEmpty())
 			{
 				SelectionKey key = keysToClose.removeFirst();
 				key.cancel();
 				channelsToClose.add(key.channel());
 				some = true;
 			}
 		}
 		if(some)
 		{
 			synchronized (closeSynch)
 			{
 				closeSynch.notify();
 			}
 		}
 	}
 
 	private void closeThread()
 	{
 		try
 		{
 			while(true)
 			{
 				// Wait for notification
 				synchronized (closeSynch)
 				{
 					try
 					{
 						if(!close)
 						{
 							closeSynch.wait();
 						}
 					}
 					catch(InterruptedException e)
 					{
 					}
 
 					if(close)
 					{
 						return;
 					}
 				}
 
 				// Close all channels
 				SelectableChannel[] channels;
 				synchronized (connections)
 				{
 					channels = channelsToClose.toArray(
 						new SelectableChannel[channelsToClose.size()]);
 					channelsToClose.clear();
 				}
 				for(SelectableChannel channel : channels)
 				{
 					try
 					{
 						((SocketChannel)channel).socket().shutdownOutput();
 						channel.close();
 					}
 					catch(Throwable t)
 					{
 					}
 				}
 			}
 		}
 		finally
 		{
 			synchronized(closeSynch)
 			{
 				closeThreadClosed = true;
 				closeSynch.notifyAll();
 			}
 		}
 	}
 
 	/**
 	 * Adds the key to a list which the selector thread will cancel. Once it
 	 * is cancelled, the channel will be closed in a separate thread (in case
 	 * it blocks).
 	 * @param key Key to cancel
 	 */
 	private void closeChannel(SelectionKey key)
 	{
 		synchronized (connections)
 		{
 			keysToClose.add(key);
 			connections.remove(key);
 		}
 	}
 
 	/**
 	 * Closes the HTTP server. Note that this will block for a little while.
 	 */
 	public void close()
 	{
 		close = true;
 		while(!closed)
 		{
 			try
 			{
 				Thread.sleep(100);
 			}
 			catch(InterruptedException ie)
 			{
 			}
 		}
 
 		synchronized (closeSynch)
 		{
 			closeSynch.notifyAll();
 			while(!closeThreadClosed)
 			{
 				try
 				{
 					closeSynch.wait();
 				}
 				catch(InterruptedException e)
 				{
 				}
 			}
 		}
 	}
 
 	public int getValue()
 	{
 		synchronized (connections)
 		{
 			return connections.size();
 		}
 	}
 }
