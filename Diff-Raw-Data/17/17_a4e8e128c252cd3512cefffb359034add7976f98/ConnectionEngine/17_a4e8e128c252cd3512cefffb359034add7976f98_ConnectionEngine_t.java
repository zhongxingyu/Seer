 package com.test9.irc.engine;
 
 import java.io.IOException;
 
 import com.test9.irc.display.ChatWindow;
 import com.test9.irc.display.EventAdapter;
 
 public class ConnectionEngine {
 	
 	private IRCConnection connection;
 	private ChatWindow cw;
 	
 	public ConnectionEngine() throws IOException {
 
		connection = new IRCConnection("irc.ecsig.com", 6667, null, "jar-bot", 
 				"jared7-bot", "jared7-bot", "UTF-8");
 
 		//((SSLIRCConnection)connection).addTrustManager(new SSLTrustManager());
 		cw = new ChatWindow();
 		cw.addChatWindowListener(new EventAdapter(cw, cw.getUtil()));
 		connection.addIRCEventListener(new IRCEventAdapter(this));
 
 		connection.connect();
 
 		
 //		try {
 //			IRCConnection.sleep(2000);
 //		} catch (InterruptedException e) {
 //			e.printStackTrace();
 //		}
 		//connection.send("JOIN #ecsig");
 		//connection.send("JOIN #jircc");
 	}
 
 	/**
 	 * @return the connection
 	 */
 	public IRCConnection getConnection() {
 		return connection;
 	}
 
 	/**
 	 * @param connection the connection to set
 	 */
 	public void setConnection(IRCConnection connection) {
 		this.connection = connection;
 	}
 
 	/**
 	 * @return the cw
 	 */
 	public ChatWindow getCw() {
 		return cw;
 	}
 
 	/**
 	 * @param cw the cw to set
 	 */
 	public void setCw(ChatWindow cw) {
 		this.cw = cw;
 	}
 }
