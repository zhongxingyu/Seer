 package org.ukiuni.irc4j.server;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.ukiuni.irc4j.Channel;
 import org.ukiuni.irc4j.ExceptionHandler;
 import org.ukiuni.irc4j.Log;
 import org.ukiuni.irc4j.User;
 import org.ukiuni.irc4j.server.worker.PingPongWorker;
 import org.ukiuni.irc4j.server.worker.WebWorker;
 import org.ukiuni.irc4j.server.worker.Worker;
 import org.ukiuni.irc4j.util.IOUtil;
 
 public class IRCServer implements Runnable {
 	private List<ClientConnection> connectionList = new ArrayList<ClientConnection>();
 	private Map<String, ServerChannel> channelMap = new HashMap<String, ServerChannel>();
 	private int portNum = 6667;
 	private ServerSocket serverSocket;
 	private boolean isRunning;
 	private Thread runningThread;
 	private ArrayList<Worker> workerList;
 
 	public static void main(String[] args) throws IOException {
 		final IRCServer server = new IRCServer();
 		server.start();
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 			@Override
 			public void run() {
 				try {
 					server.stop();
 				} catch (Throwable e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	public IRCServer() {
 	}
 
 	public IRCServer(int portNum) {
 		this.portNum = portNum;
 	}
 
 	public void start() {
 		runningThread = new Thread(this);
 		runningThread.start();
 		this.workerList = new ArrayList<Worker>();
 		this.workerList.add(new PingPongWorker());
 		this.workerList.add(new WebWorker());
 		for (Worker worker : workerList) {
 			worker.work(this);
 		}
 	}
 
 	public void stop() throws IOException {
 		Log.log(getServerName() + " shutdown....");
 		isRunning = false;
 		IOUtil.close(serverSocket);
 		Log.log("ServerSocket closed");
 		channelMap.clear();
 		List<ClientConnection> closeCollections = new ArrayList<ClientConnection>(connectionList);
 		Log.log("close clientCollections length " + closeCollections.size());
 		for (ClientConnection clientConnection : closeCollections) {
 			IOUtil.close(clientConnection);
 		}
 		Log.log("clientConnections closed");
 		connectionList.clear();
 		runningThread = null;
 		if (null != workerList) {
 			for (Worker worker : workerList) {
 				worker.stop();
 			}
 		}
 		Log.log("worker stoped");
 		Log.log(getServerName() + " shutdowned.");
 	}
 
 	@Override
 	public void run() {
 		try {
 			isRunning = true;
 			serverSocket = new ServerSocket(portNum);
 			while (isRunning) {
 				Socket socket = serverSocket.accept();
 				ClientConnectionExceptionHandler exceptionHandler = new ClientConnectionExceptionHandler();
 				ClientConnection clientConnection = new ClientConnection(this, socket, exceptionHandler);
 				exceptionHandler.setClinetConnection(clientConnection);
 				clientConnection.start();
 			}
 		} catch (Throwable e) {
 			if (isRunning) {
 				e.printStackTrace();// todo
 			}
 		}
 	}
 
 	public static class ClientConnectionExceptionHandler implements ExceptionHandler {
 
 		private ClientConnection clientConnection;
 
 		@Override
 		public void handle(Throwable e) {
 			Log.log("Exception with " + clientConnection.getNickName(), e);
 		}
 
 		public void setClinetConnection(ClientConnection clientConnection) {
 			this.clientConnection = clientConnection;
 		}
 	}
 
 	public String getServerName() {
 		return "AIR_IRC";
 	}
 
 	public void sendToSameChannelUser(ClientConnection selfClientConnection, String newNickCommand) throws IOException {
 		for (Channel channel : selfClientConnection.getJoinedChannels()) {
 			for (User user : channel.getCurrentUserList()) {
 				ClientConnection clientConnection = findConnection(user.getNickName());
 				if (null != clientConnection) {
 					clientConnection.send(newNickCommand);
 				}
 			}
 		}
 	}
 
 	public ClientConnection findConnection(String nickName) {
 		for (ClientConnection connection : connectionList) {
 			if (nickName.equals(connection.getNickName())) {
 				return connection;
 			}
 		}
 		return null;
 	}
 
 	public void joinToChannel(final ClientConnection con, String channelName) throws IOException {
 		joinToChannel(con, channelName, null);
 	}
 
 	public synchronized void joinToChannel(final ClientConnection con, String channelName, String password) throws IOException {
 		Log.log("joinToChannel start " + con.getNickName() + " to " + channelName);
 		if (null == con.getNickName()) {
 			con.sendCommand("431 :No nickname given. send JOIN command first.");
 			return;
 		}
 		if (Channel.wrongName(channelName)) {
 			con.sendCommand("Wrong channel name " + channelName);
 			return;
 		}
 		ServerChannel channel = channelMap.get(channelName);
 		boolean createdNew = false;
 		if (null == channel) {
 			createdNew = true;
 			channel = new ServerChannel(this, channelName);
 			channelMap.put(channelName, channel);
 			channel.setPassword(password);
 		} else {
 			if (!channel.judgePassword(password)) {
 				con.sendCommand("This channel has password " + channelName);
 				return;
 			}
 		}
 		if (channel.joins(con)) {
 			con.sendCommand("You are already a member of " + channelName);
 			return;
 		}
 		channel.joinTo(con);
 		if (createdNew) {
 			con.sendCommand("MODE " + channelName + " +nt");
 		}
 		if (channel.getTopic() != null) {
 			con.sendCommand("332 " + con.getUser().getNickName() + " " + channel.getName() + " :" + channel.getTopic());
 		} else {
 			con.sendCommand("331 " + con.getUser().getNickName() + " " + channel.getName() + " :No topic is set");
 		}
 		for (User user : channel.getCurrentUserList()) {
 			con.sendCommand("353 " + con.getUser().getNickName() + " = " + channel.getName() + " :" + user.getNickName());
 		}
 		con.sendCommand("366 " + con.getUser().getNickName() + " " + channelName + " :End of /NAMES list");
 		Log.log("joinToChannel end " + con.getNickName() + " to " + channelName);
 	}
 
 	public ServerChannel getChannel(String channelName) {
 		return channelMap.get(channelName);
 	}
 
 	public boolean hasChannel(String channelName) {
 		return channelMap.containsKey(channelName);
 	}
 
 	public void removeChannel(String name) {
 		channelMap.remove(name);
 	}
 
 	public synchronized void deleteOldChannelCache(ClientConnection clientConnection) {
 		connectionList.remove(clientConnection);
 	}
 
 	public synchronized void removeConnection(ClientConnection clientConnection) {
 		if (null == clientConnection) {
 			return;
 		}
 		connectionList.remove(clientConnection);
 		for (ServerChannel channel : clientConnection.getJoinedChannels()) {
 			try {
 				channel.part(clientConnection);
 			} catch (IOException e) {
 			}
 		}
 		IOUtil.close(clientConnection);
 
 		Log.log("connection removed [" + clientConnection.getNickName() + "]");
 	}
 
 	public synchronized void removeConnection(String nickName) {
 		ClientConnection clientConnection = findConnection(nickName);
 		removeConnection(clientConnection);
 	}
 
 	public void sendServerHello(ClientConnection clientConnection) throws IOException {
 		String nickName = clientConnection.getNickName();
 		clientConnection.setServerHelloSended(true);
 		clientConnection.sendCommand("001 " + nickName + " :Welcome to " + this.getServerName() + ", Multi-Communication server IRC interface. " + clientConnection.getNickName());
 		clientConnection.sendCommand("004 " + nickName + " " + this.getServerName() + " ");
 		clientConnection.sendCommand("375 " + nickName + " :- " + this.getServerName() + " Message of the Day -");
 		clientConnection.sendCommand("372 " + nickName + " :- Hello. Welcome to " + this.getServerName() + ", a test.");
 		clientConnection.sendCommand("372 " + nickName + " :- forsome " + "for more in.");
 		clientConnection.sendCommand("376 " + nickName + " :End of MOTD command. is what");
 	}
 
 	public void putConnection(ClientConnection clientConnection) {
 		this.connectionList.add(clientConnection);
 	}
 
 	public boolean hasConnection(String nickName) {
 		return null != findConnection(nickName);
 	}
 
 	public void dumpUsers() {
 		Log.log("start////////");
 		List<ClientConnection> connectionsForDump = new ArrayList<ClientConnection>(connectionList);
 		for (ClientConnection connection : connectionsForDump) {
 			String inChannel = "";
 			for (Channel channel : connection.getJoinedChannels()) {
				inChannel = channel.getName() + ",";
 			}
			System.out.println(connection.getUser().getFQUN() + " in " + inChannel);
 		}
 		Log.log("/////////////");
 
 	}
 
 	public Collection<ClientConnection> getConnectionList() {
 		return Collections.unmodifiableList(connectionList);
 	}
 }
