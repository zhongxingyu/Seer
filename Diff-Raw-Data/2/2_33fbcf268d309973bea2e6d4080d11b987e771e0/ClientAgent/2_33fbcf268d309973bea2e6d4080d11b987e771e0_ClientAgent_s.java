 import java.awt.EventQueue;
 import java.net.Socket;
 import java.util.List;
 
 import com.google.protobuf.ByteString;
 import com.google.protobuf.InvalidProtocolBufferException;
 
 import protocols.ChatProtocol.*;
 
 public class ClientAgent implements ConnectionListener {
 
 	private static int idCount = 0;
 	private String name;
 	private Connection connection;
 	private boolean isVerified;
 	private boolean isInChat;
 	private boolean hasName;
 	private int id;
 	private ClientManager manager;
 
 	public ClientAgent(ClientManager m, Socket client) {
 		super();
 		manager = m;
 		isVerified = false;
 		isInChat = false;
 		hasName = false;
 		id = idCount++;
 		name = "NoName";
 		connection = new Connection(client);
 		connection.addConnectionListener(this);
 		connection.start();
 	}
 
 	public String getClientName() {
 		if (hasName)
 			return name;
 		else 
 			return "";
 	}
 
 	public void sendChatMessage(String name, String message) {
         NetMessage.Builder netMessage = NetMessage.newBuilder()
                 .setChatMessage(ChatMessage.newBuilder().setMessage(message));
         connection.send(netMessage.build().toByteArray());
 //        connection.send(new NetObject(NetObject.CHAT_MESSAGE, message));
 	}
 
 	@Override
 	public void objectReceived(Object o) {
 //		NetObject n = (NetObject) o;
 		NetMessage n;
 		try {
 			n = NetMessage.parseFrom((byte[])o);
 			switch (n.getType()) {
 			case AUTHENTICATION:
 				authenticate(n);
 				break;
 			case CHAT_MESSAGE:
 				chatMessageReceived(n);
 				break;
 			case NAME_AVAIL:
 				checkNameAvailability(n);
 				break;
 			case NAME_SET:
 				setName(n);
 				break;
 			case JOIN_CHAT:
 				joinChat();
 				break;
 			case LIST_UPDATE:
 				chatPersonListUpdate();
 				break;
 			default:
 				System.err.println("Unhandled NetMessage in ClientAgent");
 				break;
 			}
 		} catch (InvalidProtocolBufferException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void updateChatPersonList(List<ChatPerson> list) {
 //		List<ChatPerson> arrayList = new ArrayList<ChatPerson>(list.length);
 //		Collections.addAll(new ArrayList<ChatPerson>(list.length),list);
 		NetMessage.Builder n = NetMessage.newBuilder()
 				.setType(MessageType.LIST_UPDATE)
 				.setChatList(ChatList.newBuilder().addAllPerson(list));
 		connection.send(n.build().toByteArray());
 //		connection.send(new NetObject(NetObject.CHAT_PERSON_LIST_UPDATE,list));
 	}
 	
 	private void chatPersonListUpdate() {
 		updateChatPersonList(manager.getChatPersonList());
 //		connection.send(new NetObject(NetObject.CHAT_PERSON_LIST_UPDATE,chatList));
 	}
 	
 	private void joinChat() {
 		if (isVerified && hasName) {
 			isInChat = true;
 			
 			connection.send(NetMessage.newBuilder()
 					.setType(MessageType.REPLY)
 					.setReplyMessage(ReplyMessage.newBuilder()
 							.setType(MessageType.JOIN_CHAT)
 							.setStatus(true))
 					.build().toByteArray());
 //			connection.send(new NetObject(NetObject.ACKNOWLEDGE,NetObject.JOIN_CHAT,true));
 			manager.clientJoined(name);
 		} else 
 			connection.send(NetMessage.newBuilder()
 					.setType(MessageType.REPLY)
 					.setReplyMessage(ReplyMessage.newBuilder()
 							.setType(MessageType.JOIN_CHAT)
 							.setStatus(false))
 					.build().toByteArray());
 //			connection.send(new NetObject(NetObject.ACKNOWLEDGE,NetObject.JOIN_CHAT,false));
 	}
 	
 	private void setName(NetMessage n) {
 		String proposed = n.getString();
 		if (manager.isNameAvailable(proposed)) {
 			name = proposed;
 			connection.send(NetMessage.newBuilder()
 					.setType(MessageType.REPLY)
 					.setReplyMessage(ReplyMessage.newBuilder()
 							.setType(MessageType.NAME_SET)
 							.setStatus(true))
 					.build().toByteArray());
 //			connection.send(new NetObject(NetObject.ACKNOWLEDGE,NetObject.NAME_SET,true));
 			hasName = true;
 		} else {
 			connection.send(NetMessage.newBuilder()
 					.setType(MessageType.REPLY)
 					.setReplyMessage(ReplyMessage.newBuilder()
 							.setType(MessageType.NAME_SET)
 							.setStatus(false))
 					.build().toByteArray());
 //			connection.send(new NetObject(NetObject.ACKNOWLEDGE,NetObject.NAME_SET,false));
 		}
 	}
 	
 	private void checkNameAvailability(NetMessage n) {
 		connection.send(NetMessage.newBuilder()
 				.setType(MessageType.REPLY)
 				.setReplyMessage(ReplyMessage.newBuilder()
 						.setType(MessageType.NAME_AVAIL)
 						.setStatus(manager.isNameAvailable(n.getString()))
 						.setString(n.getString()))
 				.build().toByteArray());
 //		connection.send(new NetObject(NetObject.ACKNOWLEDGE,
 //				NetObject.NAME_AVAIL,
 //				manager.isNameAvailable(n.string),n.string));
 	}
 
 	private void close() {
 		manager.removeAgent(this);
 	}
 
 	private void authenticate(NetMessage n) {
 		Authentication a = n.getAuthentication();
 		if (a.getVersionID() == Parameters.VERSION_ID && a.getPassword().compareTo(Parameters.PASSWORD) == 0) {
 			isVerified = true;
 			manager.statusMessage(id, name, "Client "+id+" is verified");
 		} else {
 			connection.close();
 		}
 //		switch (n.type2) {
 //		case NetObject.VERSION_ID:
 //			if (n.decimal == Parameters.VERSION_ID)
 //				connection.send(new NetObject(NetObject.AUTHENTICATE,NetObject.PASSWORD));
 //			else
 //				connection.close();
 //			break;
 //		case NetObject.PASSWORD:
 //			if (n.string.compareTo(Parameters.PASSWORD) == 0) {
 //				isVerified = true;
 //				manager.statusMessage(id, name, "Client "+id+" is verified");
 //			} else
 //				connection.close();
 //			break;
 //		}
 	}
 
 	public boolean isInChat() {
 		return isInChat;
 	}
 	
 	public ChatPerson getChatPerson() {
 		return ChatPerson.newBuilder().setName(name).build();
 	}
 	
 	private void chatMessageReceived(NetMessage n) {
 		if (isInChat) {
			final String message = n.getString();
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					manager.addChatMessage(name, message);
 				}
 			});
 		}
 	}
 
 	@Override
 	public void statusMessage(String s) {
 		manager.statusMessage(id, name, s);
 	}
 
 	@Override
 	public void hasConnected() {
 		connection.send(NetMessage.newBuilder()
 				.setType(MessageType.AUTHENTICATION)
 				.build().toByteArray());
 //		connection.send(new NetObject(NetObject.AUTHENTICATE,NetObject.VERSION_ID));
 		if (manager.hasIconImage())
 			connection.send(NetMessage.newBuilder()
 					.setType(MessageType.ICON_IMAGE)
 					.setImage(Image.newBuilder()
 							.setImageData(ByteString.copyFrom(manager.getIconImageData())))
 					.build().toByteArray());
 //			connection.send(new NetObject(NetObject.ICON_IMAGE,manager.getIconImage()));
 	}
 
 	@Override
 	public void connectionClosed(String errorMessage) {
 		close();
 		if (isInChat)
 			manager.clientDisconnected(name,errorMessage);
 	}
 
 }
