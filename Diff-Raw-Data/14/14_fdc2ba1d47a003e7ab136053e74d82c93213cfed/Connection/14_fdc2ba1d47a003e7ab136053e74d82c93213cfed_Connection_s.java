 package connection;
 
 import java.util.ArrayList;
 
 import org.jibble.pircbot.PircBot;
 import org.jibble.pircbot.TrustingSSLSocketFactory;
 
 import shared.GlobalSettings;
 import shared.Message;
import ui.MainWindow;
 
 public class Connection extends PircBot{
 	//this will have to be extended to allow more server connections, and the class will have to be renamed
 	GlobalSettings globals;
 	String server;
 	String nick;
 	//eventually we will pass a reference to the ctablist, or something..
 	public Connection(GlobalSettings g, String server, String nick){
 		
 		globals = g;
 		this.server = server;
 		this.nick = nick;
 		
 		setAutoNickChange(true);
 		setName(nick);
 		setVerbose(true);
 		try {
 			this.connect(server, 6697, new TrustingSSLSocketFactory());
 			//if I didn't need SSL, I could use the below
 			//this.connect(server);
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 		
 		this.joinChannel("#dgr");
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jibble.pircbot.PircBot#onMessage(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
 	 */
 	@Override
 	protected void onMessage(String channel, String sender, String login,
 			String hostname, String message) {
 
 		
 		Message newMessage = new Message(message);
 		if(globals==null) {
 			System.err.println("CAKE");
 		} else {
 			globals.messages.get(server).get(channel).add(newMessage);
 		}
 		
 		
 
 		super.onMessage(channel, sender, login, hostname, message);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jibble.pircbot.PircBot#joinChannel(java.lang.String)
 	 */
 	@Override
 	public void joinChannel(String channel) {
 
 		globals.messages.get(server).put(channel, new ArrayList<Message>());
 		
 		super.joinChannel(channel);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jibble.pircbot.PircBot#joinChannel(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public void joinChannel(String channel, String key) {
 		
 		globals.messages.get(server).put(channel, new ArrayList<Message>());
 		
 		super.joinChannel(channel, key);
 	}
 }
