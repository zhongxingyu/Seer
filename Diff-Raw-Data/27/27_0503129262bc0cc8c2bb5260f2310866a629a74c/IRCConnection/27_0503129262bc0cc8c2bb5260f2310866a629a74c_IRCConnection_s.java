 package com.test9.irc.engine;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.ArrayList;
 import java.util.concurrent.ArrayBlockingQueue;
 
 import com.test9.irc.parser.Message;
 import com.test9.irc.parser.Parser;
 
 public class IRCConnection extends Thread {
 
 	private Socket socket;
 	protected byte regLevel = 0;
 	protected String host;
 	protected int port;
 	private BufferedReader in;
 	private BufferedWriter out;
 	protected String encoding;
 	private int timeout = 1000 * 60 * 15;
 	private String pass;
 	private String nick;
 	private String realname;
 	private String username;
 	private Parser p;
 	private IRCEventListener listener;
 	private ArrayList<User> users = new ArrayList<User>();
	private ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<String>(100);
	private QueueProcessing qp= new QueueProcessing(this);
 
 	public IRCConnection(String host, int port, String pass, String nick, 
 			String username, String realname, String encoding) {
		
 		p = new Parser();
 		this.host = host;
 		this.port = port;
 		this.pass = (pass != null && pass.length() == 0) ? null : pass;
 		this.nick = nick;
 		this.username = username;
 		this.realname = realname;
 		this.encoding = encoding;
 	}
 
 	public void addIRCEventListener(IRCEventListener listener) {
 		this.listener = listener;
 	}
 
 	@SuppressWarnings("unused")
 	public void connect() throws IOException {
 		if (regLevel != 0) // otherwise disconnected or connect
 			throw new SocketException("Socket closed or already open ("+ regLevel +")");
 		IOException exception = null;
 		Socket s = null;
 		try {
 			s = new Socket(host, port);
 			exception = null; 
 		} catch (IOException exc) {
 			if (s != null)
 				s.close();
 			s = null;
 			exception = exc; 
 		}
 		if (exception != null) 
 			throw exception; // connection wasn't successful at any port
 
 		prepare(s);
 	}
 
 	// ------------------------------
 
 	/**
 	 * Invoked by the <code>connect</code> method, this method prepares the 
 	 * connection. <br />
 	 * It initializes the class-vars for the inputstream and the outputstream of 
 	 * the socket, starts the registration of at the IRC server by calling 
 	 * <code>register()</code> and starts the receiving of lines from the server 
 	 * by starting the thread with the <code>start</code> method.<br /><br />
 	 * This method must be protected, because it is used by extending classes,
 	 * which override the <code>connect</code> method. 
 	 * @param s The socket which is used for the connection.
 	 * @throws IOException If an I/O error occurs.
 	 * @see #connect()
 	 * @see #run()
 	 */
 	protected void prepare(Socket s) throws IOException {
 		if (s == null)
 			throw new SocketException("Socket s is null, not connected");
 		socket = s;
 		regLevel = 1;
 		s.setSoTimeout(timeout);
 		in  = new BufferedReader(new InputStreamReader(s.getInputStream(), encoding));
 		out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), encoding));
 		start();
 		register();
 	}
 
 	private void register() {
 		System.out.println("register()");
 		if (pass != null)
 			send("PASS "+ pass); 
 		send("NICK "+ nick); 
 		send("USER "+ username +" "+ socket.getLocalAddress() +" "+ host 
 				+" :"+ realname); 
 	}
 
 	public void run() {
 		try {
 			String line;
 			while (!isInterrupted()) {
 				line = in.readLine();
 				if (line != null)
 					try {
 						queue.offer(line);
 					} catch (NullPointerException npe) {
 						System.out.println("failed message offer");
 					}
 				else
 					close();
 			}
 		} catch (IOException exc) {
 			close();
 		}
 	}
 	
 
 	// ------------------------------
 
 	/** 
 	 * Sends a String to the server. 
 	 * You should use this method only, if you must do it. For most purposes, 
 	 * there are <code>do*</code> methods (like <code>doJoin</code>). A carriage 
 	 * return line feed (<code>\r\n</code>) is appended automatically.
 	 * @param line The line which should be send to the server without the 
 	 *             trailing carriage return line feed (<code>\r\n</code>).
 	 */
	//public synchronized boolean send(String line) {
 	public boolean send(String line) {
 		System.out.println("send called");
 		System.out.println("sending: " + line);
 		try {
 			out.write(line +"\r\n");
 			out.flush();
 			if (regLevel == 1) { // not registered
 				Message m = p.parse(new StringBuffer(line));
 				if (m.getCommand().equalsIgnoreCase("NICK"))
 					nick = m.getParams()[0].trim();
 			}
 			return true;
 		} catch (Exception exc) {
 			exc.printStackTrace();
 			return false;
 		}
 	}
 
 	/** 
 	 * Just parses a String given as the only argument with the help of the 
 	 * <code>IRCParser</code> class. Then it controls the command and fires events
 	 * through the <code>IRCEventListener</code>.<br />
 	 * @param line The line which is sent from the server.
 	 */
 	void get(String line) {
 		Message m = p.parse(new StringBuffer(line));
 		String command = m.getCommand();
 
 		int reply; // 3-digit reply will be parsed in the later if-condition
 
 		if (command.equalsIgnoreCase("PRIVMSG")) { // MESSAGE
 			listener.onPrivmsg(host, m);
 		} else if (command.equalsIgnoreCase("MODE")) { // MODE
 			listener.onMode(m);
 		} else if (command.equalsIgnoreCase("PING")) { // PING
 			if (regLevel == 1) { // not registered
 				regLevel = 2; // first PING received -> connection
 			}
 			listener.onPing(line.substring(5));
 		} else if (command.equalsIgnoreCase("JOIN")) { // JOIN
 			listener.onJoin(host, m);
 		} else if (command.equalsIgnoreCase("NICK")) { // NICK
 			listener.onNick(m);
 		} else if (command.equalsIgnoreCase("QUIT")) { // QUIT
 			listener.onQuit(m);
 		} else if (command.equalsIgnoreCase("PART")) { // PART
 			listener.onPart(m);
 		} else if (command.equalsIgnoreCase("NOTICE")) { // NOTICE
 
 		} else if ((reply = IRCUtil.parseInt(command)) >= 1 && reply < 400) { // RPL
 			System.out.println(reply);
 			listener.onReply(m);
 			listener.onUnknown(host, line);
 			//} else if (reply >= 400 && reply < 600) { // ERROR
 
 		} else if (command.equalsIgnoreCase("KICK")) { // KICK
 
 		} else if (command.equalsIgnoreCase("INVITE")) { // INVITE
 
 		} else if (command.equalsIgnoreCase("TOPIC")) { // TOPIC
 			listener.onTopic(host, m);
 		} else if (command.equalsIgnoreCase("ERROR")) { // ERROR
 
 		} else {
 			listener.onUnknown(host, line);
 
 		}
 	}
 
 	public synchronized void close() {
 		try {
 			if (!isInterrupted())
 				interrupt();
 		} catch (Exception exc) {
 			exc.printStackTrace();
 		}
 		try {
 			if (socket != null)
 				socket.close();
 		} catch (Exception exc) {
 			exc.printStackTrace();
 		}
 		try {
 			if (out != null)
 				out.close();
 		} catch (Exception exc) {
 			exc.printStackTrace();
 		}
 		try {
 			if (in != null)
 				in.close();
 		} catch (Exception exc) {
 			exc.printStackTrace();
 		}
 		if (this.regLevel != -1) {
 			this.regLevel = -1;
 			//for (int i = listeners.length - 1; i >= 0; i--)
 			//listeners[i].onDisconnected(); 
 		}
 		socket = null;
 		in = null;
 		out = null;
 	}
 
 	/**
 	 * @return the host
 	 */
 	public String getHost() {
 		return host;
 	}
 
 	/**
 	 * @param host the host to set
 	 */
 	public void setHost(String host) {
 		this.host = host;
 	}
 
 	/**
 	 * @return the nick
 	 */
 	public String getNick() {
 		return nick;
 	}
 
 	/**
 	 * @param nick the nick to set
 	 */
 	public void setNick(String nick) {
 		this.nick = nick;
 	}
 
 	/**
 	 * @return the users
 	 */
 	public ArrayList<User> getUsers() {
 		return users;
 	}
 
 	/**
 	 * @param users the users to set
 	 */
 	public void setUsers(ArrayList<User> users) {
 		this.users = users;
 	}
 
 	public User getUser(String nick) {
 		User user = null;
 
 		for(User u : users)
 			if(u.getNick().equals(nick))
 				return(u);
 		
 		return user;
 	}
 
 	/**
 	 * @return the queue
 	 */
 	public ArrayBlockingQueue<String> getQueue() {
 		return queue;
 	}
 
 	/**
 	 * @param queue the queue to set
 	 */
 	public void setQueue(ArrayBlockingQueue<String> queue) {
 		this.queue = queue;
 	}
 
 }
