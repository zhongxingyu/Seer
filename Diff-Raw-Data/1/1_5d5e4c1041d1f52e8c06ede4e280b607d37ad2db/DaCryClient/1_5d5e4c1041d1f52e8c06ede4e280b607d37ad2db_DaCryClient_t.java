 package ClientSide;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.net.Socket;
 import java.security.Key;
 import java.util.ArrayList;
 import java.util.Random;
 
 import javax.crypto.Cipher;
 import javax.crypto.spec.SecretKeySpec;
 
 import Client.Protocol.ChatSessionUser;
 import Client.Protocol.Protocol;
 import Client.Protocol.Types.ChatInvite;
 import Client.Protocol.Types.ChatKeyOffer;
 import Client.Protocol.Types.Chatopen;
 import Client.Protocol.Types.Message;
 import Client.Protocol.Types.Namerequest;
 import Client.UserHandling.ChatSession;
 import Client.UserHandling.User;
 import GUI.ClientNotify;
 
 public class DaCryClient extends Thread implements Runnable {
 
 	private long id;
 
 	private DataInputStream in;
 	private DataOutputStream out;
 	private Client.UserHandling.User[] onlineusers;
 	private ArrayList<ChatSession> openChats;
 
 	private Socket client;
 
 	private String realnick;
 
 	private ClientNotify notify;
 
 	private ChatSessionRegister register;
 
 	private Protocol protocol;
 
 	public DaCryClient(String url, int port, String nick, ClientNotify notify) {
 		// save notify interface
 		this.notify = notify;
 		// init register
 		register = new ChatSessionRegister();
 		// init write lock
 		openChats = new ArrayList<ChatSession>();
 		// start client and create the streams.
 		try {
 			client = new Socket(url, port);
 			out = new DataOutputStream(client.getOutputStream());
 			in = new DataInputStream(client.getInputStream());
 			protocol = new Protocol(in, out);
 			id = protocol.applyForID();
 			// sent your name
 			setClientName(nick);
 			// send command to get the online list
 			protocol.writeOnlineListApply();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void close() throws IOException {
 		protocol.writeCloseByte();
 	}
 
 	public long getClientID() {
 		return id;
 	}
 
 	public void sentMessage(ChatSession session, String msg) {
 		ChatSessionContainer cont = register.getChatSessionContainer(session
 				.getId());
 
 		try {
 			byte[] cryptedmsg = encrypt(msg, getFilledArray(cont.getKey()
 					.toByteArray()));
 			protocol.writeMessageToServer(new Message((int) session.getId(),
 					cryptedmsg));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public void run() {
 		try {
 			if (in.readByte() == Protocol.DACRY_SERVER_COMM_OPEN) {
 				byte m;
 				while (!isInterrupted()) {
 					// Thread.sleep(100);
 					// m = null;
 					m = in.readByte();
 					switch (m) {
 					case Protocol.NOTIFY_NEW_ONLINE_LIST: {
 						User[] tmponlineusers = protocol.readOnlineList()
 								.getOnline();
 						ArrayList<User> difflist = generateDiffList(tmponlineusers);
 						onlineusers = tmponlineusers;
 						checkConsistence(difflist);
 						notify.notifyOnlineUsers(onlineusers);
 					}
 						break;
 					case Protocol.NOTIFY_CHAT_INV: {
 						ChatInvite inv = protocol.readChatInvite();
 						// TODO debug
 						System.out.println("Got Invite from ("
 								+ inv.getInitiatorid() + "|,"
 								+ inv.getPartnerid() + ")");
 						User initiator = null;
 						User guest = null;
 						for (User user : onlineusers) {
 							if (user.getClientId() == inv.getInitiatorid()) {
 								initiator = user;
 								System.out.println("ID" + inv.getInitiatorid()
 										+ " got name " + user.getName());
 							}
 							if (user.getClientId() == inv.getPartnerid()) {
 								guest = user;
 								System.out.println("ID" + inv.getPartnerid()
 										+ " got name " + user.getName());
 							}
 						}
 						if (guest == null || initiator == null)
 							System.out
 									.println("Not all the requested Users where found");
 						ChatSession session = new ChatSession(
 								inv.getChatsessionid(), BigInteger.ZERO,
 								BigInteger.ZERO, inv.getQ(), inv.getP(),
 								new ChatSessionUser(initiator, null),
 								new ChatSessionUser(guest, null));
 
 						openChats.add(session);
 						Random rnd = new Random();
 						int privateSecret = rnd.nextInt(100);
 
 						BigInteger cry = calculateCry(privateSecret,
 								inv.getQ(), inv.getP());
 
 						register.addChatSession(new ChatSessionContainer(inv
 								.getChatsessionid(), privateSecret));
 
 						if (session.getInitiator().getUser().getClientId() == id) {
 							session.getInitiator().setCry(cry);
 						} else if (session.getPartner().getUser().getClientId() == id) {
 							session.getPartner().setCry(cry);
 						} else {
 							System.out
 									.println("ID this message should not be ended here, "
 											+ id);
 						}
 						protocol.writeChatKeyOfferToServer(new ChatKeyOffer(
 								(int) session.getId(), cry));
 						notify.notifyNewChat(session.getPartner(id).getUser()
 								.getName());
 					}
 						break;
 					case Protocol.NOTIFY_CHAT_SETTET_UP: {
 						ChatKeyOffer offer = protocol.readKeyOffer();
 						ChatSession s = null;
 						for (ChatSession sessions : openChats) {
 							if (sessions.getId() == offer.getChatsessionid())
 								s = sessions;
 						}
 						s.getPartner(id).setCry(offer.getKey());
 						register.getChatSessionContainer(
 								offer.getChatsessionid()).setKey(
 								calculateKey(
 										register.getChatSessionContainer(
 												offer.getChatsessionid())
 												.getPrivateSecret(), offer
 												.getKey(), s.getP()));
 						notify.notifyChatConnectionEstablished(s);
 					}
 						break;
 					case Protocol.NOTIFY_NAME_ALLREADY_IN_USE: {
 						// String tmp;
 						setClientName(notify.notifyInvalidNickName(realnick));
 					}
 						break;
 					case Protocol.NOTIFY_CHAT_MESSAGE:
 						Message message = protocol.readMessage();
 						ChatSession msgsession = null;
 						for (ChatSession session : openChats) {
 							if (session.getId() == message.getChatsessionid()) {
 								msgsession = session;
 							}
 						}
 						ChatSessionContainer cont = register
 								.getChatSessionContainer(message
 										.getChatsessionid());
 						notify.notifyNewMessage(
 								msgsession,
 								decrypt((message.getMsg()), getFilledArray(cont
 										.getKey().toByteArray())));
 
 						break;
 					}
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private ArrayList<User> generateDiffList(User[] newuserlist) {
 		ArrayList<User> removedusers = new ArrayList<User>();
 		if (onlineusers != null) {
 			for (User onlineuser : onlineusers) {
 				boolean found = false;
 				for (User user : newuserlist) {
 					if (user.getClientId() == onlineuser.getClientId()) {
 						found = true;
 					}
 				}
 				if (!found)
 					removedusers.add(onlineuser);
 			}
 			for (User user : removedusers) {
 				System.out.println("Removed:" + user.getName());
 			}
 		}
 		return removedusers;
 	}
 
 	private void checkConsistence(ArrayList<User> diffusers) {
 		ArrayList<ChatSession> drop = new ArrayList<ChatSession>();
 		for (User user : onlineusers) {
 			for (ChatSession chatsession : openChats) {
 				if (user.getClientId() == chatsession.getInitiator().getUser()
 						.getClientId()
 						|| user.getClientId() == chatsession.getPartner()
 								.getUser().getClientId()) {
 					drop.add(chatsession);
 				}
 			}
 		}
 		for (ChatSession chatSession : drop) {
 			notify.notifyChatSessionEnded(chatSession);
 			openChats.remove(chatSession);
 		}
 
 	}
 
 	public byte[] getFilledArray(byte[] insert) {
 		byte[] result;
 		if (insert.length < ChatSession.KEY_LENGHT) {
 			result = new byte[ChatSession.KEY_LENGHT];
 			for (int i = 0; i < insert.length; i++) {
 				result[i] = insert[i];
 			}
 		} else
 			result = insert;
 		return result;
 	}
 
 	public User[] getOnlineusers() {
 		return onlineusers;
 	}
 
 	/**
 	 * TODO ship to protocol.java
 	 * 
 	 * @param partner
 	 * @throws IOException
 	 */
 	public void invToChat(String partner) throws IOException {
 		if (!partner.equals(realnick))
 			for (User user : onlineusers) {
 				if (user.getName().equals(partner)) {
 					protocol.writeChatInvite(new Chatopen((int) user
 							.getClientId()));
 					return;
 				}
 			}
 		else
 			return;
 	}
 
 	public void setClientName(String nick) throws IOException {
 		realnick = nick;
 		protocol.writeNamerequest(new Namerequest(nick));
 	}
 
 	public Socket getSocket() {
 		return client;
 	}
 
 	/**
 	 * 1. Stage
 	 * 
 	 * @param privateSecret
 	 * @param q
 	 * @param p
 	 * @return
 	 */
 	public static BigInteger calculateCry(int privateSecret, BigInteger q,
 			BigInteger p) {
 		p = p.abs();
 		return q.pow(privateSecret).mod(p);
 	}
 
 	/**
 	 * 2. stage
 	 * 
 	 * @param privateSecret
 	 * @param cry
 	 * @param p
 	 * @return
 	 */
 	public static BigInteger calculateKey(int privateSecret, BigInteger cry,
 			BigInteger p) {
 		p = p.abs();
 		return cry.pow(privateSecret).mod(p);
 	}
 
 	public static byte[] encrypt(String Data, byte[] keybytes) throws Exception {
 		Key key = generateKey(keybytes);
 		Cipher c = Cipher.getInstance("AES");
 		c.init(Cipher.ENCRYPT_MODE, key);
 		byte[] encVal = c.doFinal(Data.getBytes());
 		return encVal;
 	}
 
 	public static String decrypt(byte[] encryptedData, byte[] keybytes)
 			throws Exception {
 		Key key = generateKey(keybytes);
 		Cipher c = Cipher.getInstance("AES");
 		c.init(Cipher.DECRYPT_MODE, key);
 		byte[] decValue = c.doFinal(encryptedData);
 		String decryptedValue = new String(decValue);
 		return decryptedValue;
 	}
 
 	private static Key generateKey(byte[] keyValue) throws Exception {
 		Key key = new SecretKeySpec(keyValue, "AES");
 		return key;
 	}
 
 }
