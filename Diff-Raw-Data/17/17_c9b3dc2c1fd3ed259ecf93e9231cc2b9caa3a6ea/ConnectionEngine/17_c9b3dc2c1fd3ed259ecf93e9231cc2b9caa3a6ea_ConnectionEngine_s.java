 package com.test9.irc.engine;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import java.util.ArrayList;
 import java.util.Properties;
 
 import javax.swing.JOptionPane;
 import javax.swing.JPasswordField;
 
 import com.test9.irc.display.ChatWindow;
 import com.test9.irc.display.EventAdapter;
 import com.test9.irc.engine.SSL.SSLDefaultTrustManager;
 import com.test9.irc.engine.SSL.SSLIRCConnection;
 
 
 public class ConnectionEngine {
 
 	private ArrayList<IRCConnection> connections = new ArrayList<IRCConnection>();
 	private ChatWindow cw;
 
 	public static String settingsDir = ""; 
 	public static final String fileSeparator = System.getProperty("file.separator");
 	private String host = "", pass="", nick="", username="", realname="", encoding="";
 	private int port;
 	private boolean ssl;
 	private boolean loadedConnection = false;
 
 	public ConnectionEngine() throws IOException {
 
 		System.out.println(System.getProperty("user.home"));
 		String os = System.getProperty("os.name").toLowerCase();
 		String userHome = System.getProperty("user.home");
 
 		if(os.contains("mac os x")) {
 			settingsDir = userHome+"/Library/Application Support/JIRCC";
 		} else if (os.contains("windows")) {
 			settingsDir = userHome+"\\Documents\\JIRCC";
 		} else if (os.contains("linux")) {
 			settingsDir = userHome + "/JIRCC/connections";
 		} else if (os.contains("bsd")) {
 			settingsDir = userHome +"/JIRCC/connections";
 		}
 
 		File dir = new File(settingsDir);
 		System.out.println(dir.getAbsolutePath());
 
 		if(!dir.exists()) {
 			System.out.println("making new directory");
 			dir.mkdir();
 		}		
 
 		cw = new ChatWindow();
 		cw.addChatWindowListener(new EventAdapter(cw, cw.getUtil()));
 
 		loadConnection();
 
 		//public IRCConnection(String host, int port, String pass, String nick, 
 		//		String username, String realname, String encoding) 
 	}
 
 	private void beginSSLIRCConnection(String host, int port, String pass, 
 			String nick, String username, String realname, String encoding) {
 		SSLIRCConnection sslc = new SSLIRCConnection(host, port, pass, nick, 
 				username, realname, encoding);
 		connections.add(sslc);
 		sslc.addIRCEventListener(new IRCEventAdapter(this, sslc));
 		sslc.addTrustManager(new SSLDefaultTrustManager());
 		cw.getListener().onJoinServer(sslc.getHost());
 
 		try {
 			sslc.connect();
 		} catch (IOException e) {
 			System.err.println("Could not start connection ["+host+"]");
 		}
 	}
 
 	private void beginIRCConnection(String host, int port, String pass, String nick, 
 			String username, String realname, String encoding) {
 		IRCConnection irc = new IRCConnection(host, port, pass, nick, 
 				username, realname, encoding);
 		connections.add(irc);
 		cw.getListener().onJoinServer(irc.getHost());
 		irc.addIRCEventListener(new IRCEventAdapter(this, irc));
 		try {
 			irc.connect();
 		} catch (IOException e) {
 			System.err.println("Could not start connection ["+host+"]");
 		}
 	}
 
 	private void loadConnection() {
 
 
 		Properties properties = new Properties();
 
 		File connectionsDir = new File(settingsDir+fileSeparator+"connections");
 
 		if(!connectionsDir.exists()){
 			System.out.println("making new connecitons directory");
 			connectionsDir.mkdir();
 		}
 
 		if((new File(connectionsDir.getPath()).list().length) < 1) {
 			createNewConnectionSettings(properties, connectionsDir);
 
 		}
 
 		File[] files = new File(connectionsDir.getPath()).listFiles();
 		for(File n : files) {
			if((Character.isAlphabetic(n.getName().charAt(0)) || Character.isDigit(n.getName().charAt(0)))) {
 				loadedConnection = true;
 				System.out.println(n.getAbsolutePath());
 				FileInputStream in;
 				try {
 					in = new FileInputStream(n);
 					properties.load(in);
 					in.close();
 				} catch (FileNotFoundException e) {
 					System.err.println("You seem to not have a settings file. Please create one.");
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
 
 				if(ssl) {
 					beginSSLIRCConnection(host, port, pass, nick, 
 							username, realname, encoding);
 				} else {
 					beginIRCConnection(host, port, pass, nick, 
 							username, realname, encoding);
 				}
 				try {
 					IRCConnection.sleep(4000);
 				} catch (InterruptedException e) {
 
 				}
 			}
 		}
 		if(!loadedConnection) {
 			createNewConnectionSettings(properties, connectionsDir);
 		}
 	}
 	
 	private void createNewConnectionSettings(Properties properties, File connectionsDir) {
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
 
 		File settingsFile = new File(connectionsDir.getPath() + fileSeparator + host+".txt");
 		try {
 			settingsFile.createNewFile();			
 			FileOutputStream out = new FileOutputStream(settingsFile);
 			properties.store(out, "Program settings");
 			out.close();
 		} catch (FileNotFoundException e) {
 		} catch (IOException e) {}
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
