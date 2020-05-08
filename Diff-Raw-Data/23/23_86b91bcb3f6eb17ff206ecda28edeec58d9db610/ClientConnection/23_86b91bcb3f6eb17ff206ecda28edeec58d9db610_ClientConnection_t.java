 package org.ukiuni.irc4j.server;
 
 import java.io.BufferedReader;
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import org.ukiuni.irc4j.ExceptionHandler;
 import org.ukiuni.irc4j.Log;
 import org.ukiuni.irc4j.User;
 import org.ukiuni.irc4j.util.IOUtil;
 
 public class ClientConnection implements Runnable, Closeable {
 	private Socket socket;
 	private IRCServer ircServer;
 	private List<ExceptionHandler> exceptionHandlerList = new ArrayList<ExceptionHandler>();
 	private ServerCommandFactory serverCommandFactory;
	private final OutputStream out;
	private final User user;
	private final HashMap<String, ServerChannel> joinedChannelMap;
 	private boolean reading;
 	private BufferedReader readIn;
 	private Thread readingThread;
 	private String encode = "UTF-8";
 	private Date lastRecievePongDate;
 	private Date lastSendPingDate;
 	private boolean serverHelloSended;
 
 	public ClientConnection(IRCServer ircServer, Socket socket, ExceptionHandler exceptionHandler) throws IOException {
 		this.socket = socket;
 		this.ircServer = ircServer;
 		this.serverCommandFactory = new ServerCommandFactory();
 		this.out = socket.getOutputStream();
 		this.user = new User();
 		this.user.setHostName(((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().getHostAddress());
 		this.joinedChannelMap = new HashMap<String, ServerChannel>();
 		this.exceptionHandlerList.add(exceptionHandler);
 	}
 
 	@Override
 	public void run() {
 		try {
 			reading = true;
 			readIn = new BufferedReader(new InputStreamReader(socket.getInputStream(), encode));
 			String line;
 			while (reading && null != (line = readIn.readLine())) {
 				ServerCommand command = (ServerCommand) this.serverCommandFactory.loadCommand(line);
 				Log.log("command accepted from[" + this.getNickName() + "] [" + command + "] " + command.getLine());
 				try {
 					command.execute(ircServer, this, null);
 				} catch (Throwable e) {
 					rizeException(e);
 				}
 			}
 		} catch (IOException e) {
 			rizeException(e);
 		} finally {
 			for (ServerChannel channel : joinedChannelMap.values()) {
 				try {
 					channel.part(this);
 				} catch (Throwable e) {
 				}
 			}
 			try {
 				ircServer.removeConnection(this);
 			} catch (Throwable e) {
 			}
 		}
 		rizeException(new ReadNullException());
 	}
 
 	private void rizeException(Throwable e) {
 		if (reading) {
 			for (ExceptionHandler exceptionHandler : exceptionHandlerList) {
 				exceptionHandler.handle(e);
 			}
 		}
 	}
 
 	@Override
 	public void close() throws IOException {
 		reading = false;
 		// readIn sometime cant close.
 		// IOUtil.close(readIn);
 		IOUtil.close(socket);
 		try {
 			readingThread.interrupt();
 			readingThread = null;
 		} catch (Throwable e) {
 		}
 	}
 
 	@SuppressWarnings("serial")
 	public class ReadNullException extends Exception {
 	}
 
 	public void sendNotice(String message) throws IOException {
 		send(":" + ircServer.getServerName() + " NOTICE " + user.getNickName() + " :" + message);
 	}
 
 	public synchronized void send(String lowCommand) throws IOException {
 		Log.log("send to [" + user.getNickName() + "]" + lowCommand);
 		try {
 			out.write((lowCommand + "\r\n").getBytes(encode));
 		} catch (UnsupportedEncodingException e) {
 			// never occure
 		}
 		out.flush();
 	}
 
 	public String getNickName() {
 		return user.getNickName();
 	}
 
 	public void setNickName(String nickName) {
 		this.user.setNickName(nickName);
 	}
 
 	public Collection<ServerChannel> getJoinedChannels() {
 		return new ArrayList<ServerChannel>(joinedChannelMap.values());
 	}
 
 	public void sendCommand(String command) throws IOException {
 		send(":" + ircServer.getServerName() + " " + command);
 	}
 
 	public void sendPrivateMessage(String type, String senderFQUN, String targetNickName, String message) throws IOException {
 		send(":" + senderFQUN + " " + type + " " + targetNickName + " :" + message);
 	}
 
 	public void sendMessage(String type, String senderFQUN, String targetChannel, String message) throws IOException {
 		send(":" + senderFQUN + " " + type + " " + targetChannel + " :" + message);
 	}
 
 	public void sendPrivateCommand(String command) throws IOException {
 		sendCommand("NOTICE " + getNickName() + " :" + command);
 	}
 
 	public void addExceptionHandler(ExceptionHandler exceptionHandler) {
 		exceptionHandlerList.add(exceptionHandler);
 	}
 
 	public void removeExceptionHandler(ExceptionHandler exceptionHandler) {
 		exceptionHandlerList.remove(exceptionHandler);
 	}
 
 	public void joinToChannel(ServerChannel serverChannel) {
 		joinedChannelMap.put(serverChannel.getName(), serverChannel);
 	}
 
 	public void partFromChannel(String channelName) {
 		joinedChannelMap.remove(channelName);
 	}
 
 	public User getUser() {
 		return user;
 	}
 
 	public void start() {
 		readingThread = new Thread(this);
 		readingThread.start();
 	}
 
 	public void sendPong(String message) throws IOException {
 		send(":" + ircServer.getServerName() + " PONG " + ircServer.getServerName() + " :" + message);
 	}
 
 	public void sendPing(String message) throws IOException {
 		lastSendPingDate = new Date();
 		send(":" + ircServer.getServerName() + " PING " + ircServer.getServerName() + " :" + message);
 	}
 
 	public Date getLastRecievePongDate() {
 		return lastRecievePongDate;
 	}
 
 	public Date getLastSendPingDate() {
 		return lastSendPingDate;
 	}
 
 	public boolean isServerHelloSended() {
 		return this.serverHelloSended;
 	}
 
 	public void setServerHelloSended(boolean serverHelloSended) {
 		this.serverHelloSended = serverHelloSended;
 	}
 
 	public void recievePong() {
 		this.lastRecievePongDate = new Date();
 	}
 
 	public void sendQuit(String message) throws IOException {
 		send(":" + user.getFQUN() + " QUIT :" + message);
 	}
 
 	public ServerChannel getJoinedChannel(String channelName) {
 		return joinedChannelMap.get(channelName);
 	}
 }
