 /**
  * HotButton Server by Dima Max and Tim Daniel Evert
  */
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.FileInputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.nio.channels.ClosedChannelException;
 
 import java.net.URI;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Map;
 import java.util.HashMap;
 
 import java.lang.InterruptedException;
 import java.lang.StringBuilder;
 
 import com.sun.net.httpserver.HttpServer;
 import com.sun.net.httpserver.HttpHandler;
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.Headers;
 
 /**
  * logging class
  */
 class Log
 {
 	static List<String> logs;
 	
 	/**
 	 * gerneral public log method
 	 */
 	public static void d(String section, String message)
 	{
 		// init
 		if(logs == null)
 			logs = new ArrayList<String>();
 			
 		String s = "[" + section.toUpperCase() + "] - " + message;
 		
 		// add to queue
 		System.out.println(s);
 		logs.add(s);
 	}
 }
 
 /**
  * admin interface
  */
 class AdminInterface implements HttpHandler
 {
 	public static Map<String, String> getQueryMap(String query)
 	{
 		String[] params = query.split("&");
 		Map<String, String> map = new HashMap<String, String>();
 		for (String param : params)
 		{
 			String name = param.split("=")[0];
 			String value = param.split("=")[1];
 			map.put(name, value);
 		}
 		return map;
 	}
 	
 	public void handle(HttpExchange exchange) throws IOException
 	{
 		// send headers
 		Headers responseHeaders = exchange.getResponseHeaders();
 		responseHeaders.set("Content-Type", "text/html");
 		exchange.sendResponseHeaders(200, 0);
 		
 		// preparation of html body
 		OutputStream responseBody = exchange.getResponseBody();
 		OutputStreamWriter out = new OutputStreamWriter(responseBody);
 		
 		// uri
 		URI requestedUri = exchange.getRequestURI();
 		String path = requestedUri.getPath();
 		
 		// unlock or lock
 		if(path.startsWith("/unlock"))
 			hotbutton.setLock(false);
 		else if(path.startsWith("/lock"))
 			hotbutton.setLock(true);
 			
 		// kick user
 		if(path.startsWith("/kick")) {
 			String query = requestedUri.getQuery();
 			Map<String, String> map = getQueryMap(query);
 			String strId = map.get("id");
 			int intId = Integer.parseInt(strId);
 			hotbutton.kickUserById(intId);
 		}
 		
 		// write header
 		writeHeader(out);
 		
 		// write log
 		Iterator<String> iterLogs = Log.logs.iterator();
 		out.write("<ul id=logs>");
 		while(iterLogs.hasNext())
 			out.write("<li>" + iterLogs.next() + "</li>");
 		out.write("</ul>");
 			
 		// write userlist
 		this.writeUserList(out);
 		
 		// write footer and end
 		writeFooter(out);
 		out.close();
 		responseBody.close();
 	}
 	
 	/**
 	 * writeHeader
 	 */
 	private void writeHeader(OutputStreamWriter out) throws IOException
 	{
 		out.write("<html>" +
 		"<head>" +
 		"<link href=css/style.css rel=stylesheet type=text/css>" +
 		"<title>Android Hotbutton Manager</title>" +
 		"</head>" +
 		"<body>" +
 		"<div id=header>" + 
 		"<h1>Android HotButton Manager</h1>" +
 		"</div>" +
 		"<div id=sidebar>" + 
 		"<a class=\"large magenta awesome\" href=/>Refresh</a>");
 		
 		// lock or unlock button ?
 		if(hotbutton.isLocked)
 			out.write("<a class=\"large orange awesome\" href=unlock>Unlock all Hotbuttons</a>");
 		else
 			out.write("<a class=\"large green awesome\" href=lock>Lock all Hotbuttons</a>");
 			
 		// status
 		out.write("<span><strong>Status:</strong> ");
 		if(hotbutton.isLocked)
 			out.write("<font color=red>locked</font>");
 		else
 			out.write("<font color=green>unlocked</font>");
 		out.write("</span>");
 		
 		// rest
 		out.write("</div>" +
 		"<div id=body>");
 	}
 	
 	/**
 	 * writeUserList
 	 */
 	private void writeUserList(OutputStreamWriter out) throws IOException
 	{
 		out.write("<ul id=users>" +
 		"<h2>Users</h2>");
 		
 		// get status of all clients
 		Set<SelectionKey> allKeys = hotbutton.selector.keys();
 		for(SelectionKey key : allKeys)
 		{
 			if(key == hotbutton.serverkey)
 				continue;
 				
 			Player player = (Player)key.attachment();
 			String username = player.getUsername();
 			String address = player.getAddress();
 			int id = player.getId();
 			out.write("<li><strong>" + username + "</strong> [" + address + "] <a href=/kick?id=" + id + " class=\"small awesome red\">kick</a></li>");
 		}
 	}
 	
 	/**
 	 * writeFooter
 	 */
 	private void writeFooter(OutputStreamWriter out) throws IOException
 	{
 		out.write("</ul>" +
 		"</div>" +
 		"</body>" +
 		"</html>");
 	}
 }
 
 /**
  * images handler
  */
 class FileController implements HttpHandler
 {
 	private String contentType;
 	private String chroot;
 	
 	/**
 	 * constructor
 	 */
 	FileController(String chroot, String contentType)
 	{
 		this.chroot = chroot;
 		this.contentType = contentType;
 	}
 	
 	/**
 	 * file handler
 	 */
 	public void handle(HttpExchange exchange) throws IOException
 	{
 		// fetch meta stuff
 		URI requestedUri = exchange.getRequestURI();
 		Headers responseHeaders = exchange.getResponseHeaders();
 		
 		try {
 			// uri extraction
 			String path = requestedUri.getPath();
 			int index = path.lastIndexOf('/');
 			String file = this.chroot + "/" + path.substring(index + 1);
 			
 			// open streams
 			FileInputStream in = new FileInputStream(file);
 			OutputStream out = exchange.getResponseBody();
 			
 			// send headers
 			responseHeaders.set("Content-Type", this.contentType);
 			exchange.sendResponseHeaders(200, 0);
 			
 			// copy contents of in to out
 			byte[] buf = new byte[1024];
 			int len;
 			while ((len = in.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 			in.close();
 			out.close();
 		} catch(Exception ex) {
 			System.out.println(ex.toString());
 			
 			responseHeaders.set("Content-Type", "text/plain");
 			exchange.sendResponseHeaders(404, 0);
 			
 			OutputStream out = exchange.getResponseBody();
 			out.write(ex.toString().getBytes());
 			out.close();
 		}
 	}
 }
 
 /**
  * Player
  */
 class Player
 {
 	// static member vars
 	private static int id_count = 1;
 	
 	// member vars
 	private String username;
 	private int id;
 	private StringBuilder strResponse;
 	private List<String> lstCommand;
 	private SocketChannel socket;
 	
 	/**
 	 * ctor new player
 	 */
 	Player(SocketChannel socket)
 	{
 		this.strResponse = new StringBuilder();
 		this.socket = socket;
 		this.id = id_count++;
 	}
 	
 	void setUsername(String username) { this.username = username; }
 	String getUsername() { return (username == null) ? "unbekannt" : this.username; }
 	String getAddress() { return this.socket.socket().getRemoteSocketAddress().toString(); }
 	int getId() { return this.id; }
 	
 	/**
 	 * enqueue token for client
 	 */
 	void send(String tok)
 	{
 		if(this.strResponse.length() > 0)
 			this.strResponse.append("-");
 			
 		this.strResponse.append(tok);
 	}
 	
 	/**
 	 * send all to client
 	 */
 	void commit() throws IOException
 	{
 		// send response
 		this.strResponse.append("\r\n");
 		ByteBuffer writeBuffer = ByteBuffer.wrap(strResponse.toString().getBytes());
 		this.socket.write(writeBuffer);
 		
 		// create new strbuilder
 		this.strResponse = new StringBuilder();
 	}
 	
 	/**
 	 * read - reads data from socket
 	 */
 	Boolean read() throws IOException
 	{
 		// read data to buffer
 		ByteBuffer readBuffer = ByteBuffer.allocate(512);
 		int bytesread = this.socket.read(readBuffer);
 		if (bytesread == -1) {
 			this.socket.close();
 			return false;
 		}
 		
 		// fetch command from buffer
 		String strCommand = new String(readBuffer.array()).trim();
 		this.lstCommand = Arrays.asList(strCommand.split("-"));
 		
 		// ditto
 		Log.d("player", strCommand);
 		return true;
 	}
 	
 	/**
 	 * get stuff
 	 */
 	public String get(int i)
 	{
 		return this.lstCommand.get(i);
 	}
 }
 
 /**
  * hotbutton main class
  */
 public class hotbutton
 {
 	/**
 	 * all clients
 	 */
 	static ServerSocketChannel server;
 	static SelectionKey serverkey;
 	static Selector selector;
 	static Boolean isLocked = true;
 
 	/**
 	 * main server function 
 	 */
 	public static void main(String[] args) throws IOException
 	{
 		// create logs
 		Log.d("main", "Starting HotButton Server....");
 		
 		// create admin interface
 		InetSocketAddress address = new InetSocketAddress(8888);
 		HttpServer httpServer = HttpServer.create(address, 0);
 		AdminInterface admin = new AdminInterface();
 		httpServer.createContext("/", admin);
 		httpServer.createContext("/images/", new FileController("./images", "image/png"));
 		httpServer.createContext("/css/", new FileController("./css", "text/css"));
 		httpServer.start();
 		
 		// listen server port 31337
 		InetSocketAddress addrServer = new InetSocketAddress(31337);
 		server = ServerSocketChannel.open();
 		selector = Selector.open();
 		server.configureBlocking(false);
 		server.socket().bind(addrServer);
 		serverkey = server.register(selector, SelectionKey.OP_ACCEPT);
 		serverkey.attach("Server");
 		
 		// main loop
 		Log.d("main", "Server started...");
 		for(;;)
 		{
 			// do select
 			int readyChannels = selector.select();
 			if(readyChannels < 0)
 				break;
 				
 			// call process events
 			processEvents(readyChannels);
 		}
 		
 	}
 	
 	/**
 	 * processCommand
 	 */
 	static void processCommand(Player player) throws IOException
 	{
 		// init
 		Log.d("processCommand", "processing command...");
 		
 		// hi
 		if(player.get(0).equals("hi")) {
 			player.send("hi");
 			player.send("hotbutton");
 			player.send("v0.0.1");
 			player.commit();
 		}
 		
 		// login
 		else if(player.get(0).equals("login")) {
 			String username = player.get(1);
 			Log.d("processCommand", player.getUsername() + " changed his username to " + username);
 			player.setUsername(username);
 			player.send("login");
 			player.send("okay");
 			player.commit();
 		}
 		
 		// buzz
 		else if(player.get(0).equals("buzz")) {
 			Log.d("processCommand", player.getUsername() + " sent a buzz!");
 			
 			
 			// the button must be unlocked to select a winner
 			if(!hotbutton.isLocked)
 			{
 				hotbutton.isLocked = true;
 				Set<SelectionKey> allKeys = hotbutton.selector.keys();
 				for(SelectionKey key : allKeys)
 				{
 					if(key == hotbutton.serverkey)
 						continue;
 					
 					try
 					{
 						SocketChannel client = (SocketChannel)key.channel();
 						Player other = (Player)key.attachment();
 						
 						// is he the one who pressed ?
 						other.send("buzz");
 						other.send(other.equals(player) ? "winner" : "looser");
 						other.commit();
 					} catch(IOException ex) {
 						Log.d("setLock", ex.toString());
 					}
 				}
 				
 				
 				
 				player.send("looser");
 			}
 			 
 			else {
 				player.send("winner");
 				
 			}
 			
 			player.send("buzz");
 			player.commit();
 			hotbutton.isLocked = true;
 		}
 		
 		// protocol violation
 		else
 			Log.d("processCommand", "protocol violation by " + player.getUsername());
 	}
 	 
 	 /**
 	  * processEvents
 	  */
 	static void processEvents(int eventCount) throws IOException
 	{
 		// initialisation stuff
 		Set<SelectionKey> readyKeys = selector.selectedKeys();
 		Iterator iterator = readyKeys.iterator();
 		
 		// iterate through selection
 		for(int i = 0; i < eventCount && iterator.hasNext();)
 		{
 			// fetch a key
 			SelectionKey key = (SelectionKey)iterator.next();
 			iterator.remove();
 			if (!key.isValid())
 				continue;
 			
 			// new client connected
 			if (key == serverkey && key.isAcceptable())
 			{
 				SocketChannel client = server.accept();
 				Player player = new Player(client);
 				client.configureBlocking(false);
 				client.register(selector, SelectionKey.OP_READ, player);
 				Log.d("processEvents", "Accepted connection from " + client);
 				
 				// login is not possible while the button is locked
				if(!hotbutton.isLocked) {
					Log.d("processEvents", "Connection canceled because round was already started!" + client);
 					player.send("error");
 					player.send("you can't join an active round!");
 					player.commit();
 					client.close();
 					continue;
 				}
 			}
 			
 			// message from client
 			else if(key.isReadable())
 			{
 				try
 				{
 					// fetch correct player
 					SocketChannel client = (SocketChannel)key.channel();
 					Player player = (Player)key.attachment();
 					
 					// closed connection
 					if(!client.isConnected()) {
 						Log.d("processEvents", player.getUsername() + " has disconnected.");
 						key.cancel();
 						continue;
 					}
 					
 					// need a string
 					if(!player.read()) {
 						Log.d("processEvents", "Ignoring unknown sequeenze from " + player.getAddress());
 						continue;
 					}
 					
 					// process Command
 					processCommand(player);
 				} catch(ClosedChannelException ex) {
 					System.out.println(ex.toString());
 				}
 			}
 		}
 	}
 	
 	/**
 	 * kickUserById
 	 */
 	public static void kickUserById(int id)
 	{
 		Set<SelectionKey> allKeys = hotbutton.selector.keys();
 		for(SelectionKey key : allKeys)
 		{
 			if(key == hotbutton.serverkey)
 				continue;
 			
 			SocketChannel client = (SocketChannel)key.channel();
 			Player player = (Player)key.attachment();
 			
 			try
 			{
 				if(player.getId() == id) {
 					player.send("kick");
 					player.commit();
 					client.close();
 					Log.d("kickUserById", "kicked " + player.getUsername() +  " from the server!");
 					break;
 				}
 			} catch(IOException ex) {
 				Log.d("kickUserById", ex.toString());
 			}
 		}
 	}
 	
 	/**
 	 * setLock updates the server's lock state
 	 */
 	public static void setLock(Boolean lock)
 	{
 		// send lock state to all users
 		hotbutton.isLocked = lock;
 		Set<SelectionKey> allKeys = hotbutton.selector.keys();
 		for(SelectionKey key : allKeys)
 		{
 			if(key == hotbutton.serverkey)
 				continue;
 			
 			try
 			{
 				SocketChannel client = (SocketChannel)key.channel();
 				Player player = (Player)key.attachment();
 				
 				if(hotbutton.isLocked)
 					player.send("lock");
 				else
 					player.send("unlock");
 				
 				player.commit();
 			} catch(IOException ex) {
 				Log.d("setLock", ex.toString());
 			}
 		}
 	}
 }
