 package com.test9.irc.engine;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import java.util.ArrayList;
 import java.util.Properties;
 import java.util.prefs.Preferences;
 
 import javax.swing.JOptionPane;
 import javax.swing.JPasswordField;
 
 import com.test9.irc.display.ChatWindow;
 import com.test9.irc.display.EventAdapter;
 import com.test9.irc.engine.SSL.SSLDefaultTrustManager;
 import com.test9.irc.engine.SSL.SSLIRCConnection;
 
 
 public class ConnectionEngine {
 
 	private ArrayList<IRCConnection> connections = new ArrayList<IRCConnection>();
 	private ChatWindow cw;
 	private String host = "", pass="", nick="", username="", realname="", encoding="";
 	private int port;
 	private boolean ssl;
 
 	public ConnectionEngine() throws IOException {
 
 		cw = new ChatWindow();
 		cw.addChatWindowListener(new EventAdapter(cw, cw.getUtil()));
 
 		loadConnection();
 		//System.out.println(host);
 
 		if(ssl) {
 			SSLIRCConnection sslc = new SSLIRCConnection(host, port, pass, nick, 
 					username, realname, encoding);
 			connections.add(sslc);
 			sslc.addIRCEventListener(new IRCEventAdapter(this, sslc));
 			sslc.addTrustManager(new SSLDefaultTrustManager());
 			cw.getListener().onJoinServer(sslc.getHost());
 
 			sslc.connect();
 		} else {
 			IRCConnection temp0 = new IRCConnection(host, port, pass, nick, 
 					username, realname, encoding);
 			connections.add(temp0);
 			cw.getListener().onJoinServer(temp0.getHost());
 			temp0.addIRCEventListener(new IRCEventAdapter(this, temp0));
 			temp0.connect();
 		}
 		//public IRCConnection(String host, int port, String pass, String nick, 
 		//		String username, String realname, String encoding) 
 	}
 
 	private void loadConnection() {
 		Properties properties = new Properties();
 		//String userDir = ConnectionEngine.class.getClassLoader().getResourceAsStream("settings.properties").toString();
 		System.out.println(System.getProperty("user.home"));
 		String settingsDir = "";
 		String os = System.getProperty("os.name").toLowerCase();
 		String userHome = System.getProperty("user.home");
 		String fileSeparator = System.getProperty("file.separator");
 		
 		if(os.contains("mac os x")) {
 			settingsDir = userHome+"/Library/Application Support/JIRCC";
 		} else if (os.contains("windows")) {
 			settingsDir = userHome+"\\Documents\\JIRCC";
 		}
 		
 		System.out.println(settingsDir);
 		File dir = new File(settingsDir);
 		File settingsFile = new File(settingsDir + fileSeparator + "settings.txt");
 		if(settingsFile.exists()) {
 			FileInputStream in;
 			try {
 				in = new FileInputStream(settingsFile);
 				properties.load(in);
 			} catch (FileNotFoundException e) {
				System.err.println("Error creating fileInputStream");
 			} catch (IOException e) {
 				System.err.println("Error loading file into properties.");
 			}
 			
 			host = properties.getProperty("host", "");
 			pass = properties.getProperty("pass", "");
 			nick = properties.getProperty("nick", "");
 			username = properties.getProperty("username", "");
 			realname = properties.getProperty("realname", "");
 			encoding = properties.getProperty("encoding", "");
 			port = Integer.parseInt(properties.getProperty("port"));
 			ssl = Boolean.parseBoolean(properties.getProperty("ssl"));
 
 		}
 		if(!dir.exists()) {
 			dir.mkdir();
 		}
 
 		if(!settingsFile.exists()) {
 			try {
 				settingsFile.createNewFile();
 			} catch (IOException e) {
 				System.err.println("Error creating new settings file.");
 			}
 			host = JOptionPane.showInputDialog("What is the host?");
 		    JPasswordField pwd = new JPasswordField(30);  
 			JOptionPane.showConfirmDialog(null, pwd,"Enter Password",JOptionPane.OK_CANCEL_OPTION);  
 			pass = new String(pwd.getPassword());
 			nick = JOptionPane.showInputDialog("What is the nick?");
 			username = JOptionPane.showInputDialog("What is the username?");
 			realname = JOptionPane.showInputDialog("What is the realname?");
 			encoding = JOptionPane.showInputDialog("What is the encoding?(type in 'UTF-8' for now)");
 			port = Integer.parseInt(JOptionPane.showInputDialog("What port?"));
 			ssl = Boolean.parseBoolean(JOptionPane.showInputDialog("SSL?('true'/'false')"));
 
 			properties.put("host", host);
 			properties.put("pass", pass);
 			properties.put("nick", nick);
 			properties.put("username", username);
 			properties.put("realname", realname);
 			properties.put("encoding", encoding);
 			properties.put("port", Integer.toString(port));
 			properties.put("ssl", Boolean.toString(ssl));
 			
 
 			try {
 				FileOutputStream out = new FileOutputStream(settingsFile);
 				properties.store(out, "Program settings");
 			} catch (FileNotFoundException e) {
 				System.err.println("Error writing settings to new settings file.");
 			} catch (IOException e) {
 				System.err.println("Error storing the application settings.");
 			}
 		}
 	}
 	
 	/**
 	 * @return the connection
 	 */
 	public IRCConnection getConnection(String host) {
 		System.out.println("getconnectionshost:"+host);
 		for(IRCConnection temp : connections) {
 			if(temp.getHost().equals(host)) {
 				System.out.println("hostmatch");
 				return temp;
 			}
 		}
 		return null;
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
