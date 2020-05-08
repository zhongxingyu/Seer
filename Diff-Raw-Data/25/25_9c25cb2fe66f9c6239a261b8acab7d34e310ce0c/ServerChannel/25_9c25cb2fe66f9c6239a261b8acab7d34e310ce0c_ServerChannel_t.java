 package org.ukiuni.irc4j.server;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.ukiuni.irc4j.Channel;
 import org.ukiuni.irc4j.Log;
 import org.ukiuni.irc4j.db.Database;
 import org.ukiuni.irc4j.entity.Message;
 
 public class ServerChannel extends Channel {
 
 	private IRCServer ircServer;
 	public Set<ClientConnection> joinedConnectionList = new HashSet<ClientConnection>();
 	private String password;
 
 	public ServerChannel(IRCServer server, String name) {
 		super(name);
 		this.ircServer = server;
 	}
 
 	public boolean joins(ClientConnection clientConnection) {
 		return joinedConnectionList.contains(clientConnection);
 	}
 
 	public void addConnection(ClientConnection clientConnection) {
 		joinedConnectionList.add(clientConnection);
 		clientConnection.joinToChannel(this);
 		addUser(clientConnection.getUser());
 	}
 
 	public void removeConnection(ClientConnection clientConnection) {
 		joinedConnectionList.remove(clientConnection);
 		removeUser(clientConnection.getUser());
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
 
 	public void sendJoin(ClientConnection joinerConnection) throws IOException {
 		List<ClientConnection> sendClients = new ArrayList<ClientConnection>(joinedConnectionList);
 		for (ClientConnection clientConnection : sendClients) {
 			try {
 				clientConnection.sendJoin(joinerConnection, this);
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
 
 	public void sendMessage(String type, ClientConnection senderClientConnection, String message) throws IOException {
 		List<ClientConnection> sendClients = new ArrayList<ClientConnection>(joinedConnectionList);
 		Message messageObj = new Message();
 		messageObj.setCreatedAt(new Date());
 		messageObj.setMessage(message);
 		messageObj.setSenderFQUN(senderClientConnection.getUser().getFQUN());
 		messageObj.setSenderNickName(senderClientConnection.getNickName());
 		messageObj.setTargetChannel(getName());
 		messageObj.setType(type);
 		Database.getInstance().regist(messageObj);
 
 		for (ClientConnection clientConnection : sendClients) {
 			try {
 				if (senderClientConnection != clientConnection) {
 					clientConnection.sendMessage(type, senderClientConnection, this, message);
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
 
	private void sendPartCommand(ClientConnection partConnection) throws IOException {
 		List<ClientConnection> sendClients = new ArrayList<ClientConnection>(joinedConnectionList);
 		for (ClientConnection clientConnection : sendClients) {
 			try {
 				clientConnection.sendPartCommand(partConnection, this);
 			} catch (Exception e) {// TODO something do with exception???
 				ircServer.removeConnection(clientConnection.getNickName());
 				Log.log(e);
 			}
 		}
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public boolean judgePassword(String password) {
 		if (this.password == null || "".equals(this.password)) {
 			return true;
 		}
 		return this.password.equals(password);
 	}
 
 	public void part(ClientConnection clientConnection) throws IOException {
		this.sendPartCommand(clientConnection);
 		this.removeConnection(clientConnection);
 	}
 
 	public List<Message> getHistory(int length) {
 		return Database.getInstance().loadMessage(getName(), length);
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
 
 	public long getMessageMaxId() {
 		return Database.getInstance().loadMaxId(getName());
 	}
 
 	public void joinTo(ClientConnection con) throws IOException {
 		addConnection(con);
 		sendJoin(con);
 	}
 
 }
