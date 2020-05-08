 package org.irc4j.server;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.irc4j.Channel;
 import org.irc4j.Log;
 import org.irc4j.Message;
 
 public class ServerChannel extends Channel {
 
 	private IRCServer ircServer;
 	public List<ClientConnection> joinedConnectionList = new ArrayList<ClientConnection>();
 	private String password;
	private LimitedQueue<Message> messageQueue = new LimitedQueue<Message>(100);
 
 	public ServerChannel(IRCServer server, String name) {
 		super(name);
 		this.ircServer = server;
 	}
 
 	public void addConnection(ClientConnection clientConnection) {
 		joinedConnectionList.add(clientConnection);
 		clientConnection.joinToChannel(this);
 	}
 
 	public void removeConnection(ClientConnection clientConnection) {
 		joinedConnectionList.remove(clientConnection);
 		removeUser(clientConnection.getNickName());
 		if (joinedConnectionList.isEmpty()) {
 			ircServer.removeChannel(this.getName());
 		}
 	}
 
 	public void send(String lowCommand) throws IOException {
 		List<ClientConnection> sendClients = new ArrayList<ClientConnection>(joinedConnectionList);
 		for (ClientConnection clientConnection : sendClients) {
 			try {
 				clientConnection.send(lowCommand);
 			} catch (Exception e) {// TODO something do with exception???
 				ircServer.removeConnection(clientConnection.getNickName());
 				Log.log(e);
 			}
 		}
 	}
 
 	public void sendExcepFrom(String lowCommand, ClientConnection exeptClientConnection) throws IOException {
 		List<ClientConnection> sendClients = new ArrayList<ClientConnection>(joinedConnectionList);
 		for (ClientConnection clientConnection : sendClients) {
 			try {
 				if (exeptClientConnection != clientConnection) {
 					clientConnection.send(lowCommand);
 				}
 			} catch (Exception e) {// TODO something do with exception???
 				Log.log(e);
 			}
 		}
 	}
 
 	public void sendMessage(String type, String senderFQUN, String targetChannel, String message, ClientConnection exeptClientConnection) throws IOException {
 		List<ClientConnection> sendClients = new ArrayList<ClientConnection>(joinedConnectionList);
 		Message messageObj = new Message();
 		messageObj.setDate(new Date());
 		messageObj.setMessage(message);
 		messageObj.setSenderFQUN(senderFQUN);
 		messageObj.setSenderNickName(exeptClientConnection.getNickName());
 		messageObj.setTargetChannel(targetChannel);
 		messageObj.setType(type);
 		messageQueue.add(messageObj);
 
 		for (ClientConnection clientConnection : sendClients) {
 			try {
 				if (exeptClientConnection != clientConnection) {
 					clientConnection.sendMessage(type, senderFQUN, targetChannel, message);
 				}
 			} catch (Exception e) {// TODO something do with exception???
 				Log.log(e);
 			}
 		}
 	}
 
 	public void sendCommand(String lowCommand) throws IOException {
 		List<ClientConnection> sendClients = new ArrayList<ClientConnection>(joinedConnectionList);
 		for (ClientConnection clientConnection : sendClients) {
 			try {
 				clientConnection.sendCommand(lowCommand);
 			} catch (Exception e) {// TODO something do with exception???
 				Log.log(e);
 			}
 		}
 	}
 
 	public void sendPartCommand(String partUserFQCN, String channelName) throws IOException {
 		send(":" + partUserFQCN + " PART " + channelName);
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public boolean judgePassword(String password) {
 		if (this.password == null) {
 			return true;
 		}
 		return this.password.equals(password);
 	}
 
 	public void part(ClientConnection clientConnection) throws IOException {
 		this.sendExcepFrom(":" + clientConnection.getUser().getFQUN() + " PART " + this.getName(), clientConnection);
 		this.removeConnection(clientConnection);
 	}
 
 	public List<Message> getHistory(int length) {
		int start = messageQueue.size() - length;
		if (start < 0) {
			start = 0;
		}
		return new ArrayList<Message>(messageQueue.subList(start, messageQueue.size()));
 	}
 
	@SuppressWarnings("serial")
 	public class LimitedQueue<E> extends LinkedList<E> {
 		private int limit;
 
 		public LimitedQueue(int limit) {
 			this.limit = limit;
 		}
 
 		@Override
 		public boolean add(E o) {
 			boolean added = super.add(o);
 			while (added && size() > limit) {
 				super.remove();
 			}
 			return added;
 		}
 	}
 }
