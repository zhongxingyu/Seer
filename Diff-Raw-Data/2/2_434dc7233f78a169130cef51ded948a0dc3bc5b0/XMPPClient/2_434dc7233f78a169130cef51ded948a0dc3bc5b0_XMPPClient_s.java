 package net.hermeto.android.main;
 
 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.MessageListener;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.packet.Message;
 
 import android.util.Log;
 
 public class XMPPClient implements MessageListener {
 	protected int port;
 	protected String messagesDestination;
 	protected String serverAddress;
 	protected String username;
 	protected String password;
 	
 	protected XMPPConnection connection;
 
 	public XMPPClient(int port, String messagesDestination,
 			String serverAddress, String username, String password, String serviceName) {
 		super();
 		this.port = port;
 		this.messagesDestination = messagesDestination;
 		this.serverAddress = serverAddress;
 		this.username = username;
 		this.password = password;
 		try {
 			this.login(this.username, this.password, this.serverAddress, this.port, serviceName);
 		} catch (XMPPException e) {
 			Log.e("hermeto", "Oooops! An error ocurred while initiating XMPP server connection!");
 			e.printStackTrace();
 		}
 	}
 	
 	public XMPPClient(int port, String messagesDestination,
 			String serverAddress, String username, String password) {
 		super();
 		this.port = port;
 		this.messagesDestination = messagesDestination;
 		this.serverAddress = serverAddress;
 		this.username = username;
 		this.password = password;
 		try {
 			this.login(this.username, this.password, this.serverAddress, this.port);
 		} catch (XMPPException e) {
 			Log.e("hermeto", "Oooops! An error ocurred while initiating XMPP server connection!");
 			e.printStackTrace();
 		}
 	}	
 	
 	protected synchronized void login(String userName, String password, String serverAddress, int port) throws XMPPException
 	{
 		ConnectionConfiguration config = new ConnectionConfiguration(serverAddress, port);
 		connection = new XMPPConnection(config);
 		connection.connect();
 		connection.login(userName, password);
 	}
 	
 	protected synchronized void login(String userName, String password, String serverAddress, int port, String serviceName) throws XMPPException
 	{
		ConnectionConfiguration config = new ConnectionConfiguration(serverAddress, port, "Hermeto");
 		connection = new XMPPConnection(config);
 		connection.connect();
 		connection.login(userName, password);
 	}	
 
 	public void sendMessage(String message) throws XMPPException
 	{
 		Chat chat = connection.getChatManager().createChat(this.messagesDestination, this);
 		chat.sendMessage(message);
 	}
 
 	public void disconnect()
 	{
 		connection.disconnect();
 	}
 
 	@Override
 	public void processMessage(Chat chat, Message message)
 	{
 		if(message.getType() == Message.Type.chat)
 			System.out.println(chat.getParticipant() + " says: " + message.getBody());
 	}
 
 }
