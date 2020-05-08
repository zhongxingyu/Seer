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
 		
 		// write header
 		writeHeader(out);
 		
 		// uri
 		URI requestedUri = exchange.getRequestURI();
 		String path = requestedUri.getPath();
 		
 		// unlock or lock
 		if(path.startsWith("/unlock") || path.startsWith("/lock")) {
 			// fill buffer with data
 			ByteBuffer buffer;
 			if(path.startsWith("/unlock"))
 				buffer = ByteBuffer.wrap("unlock\r\n".getBytes("US-ASCII"));
 			else
 				buffer = ByteBuffer.wrap("lock\r\n".getBytes("US-ASCII"));
 			
 			// send buffer to all users
 			Set<SelectionKey> allKeys = hotbutton.selector.keys();
 			for(SelectionKey key : allKeys)
 			{
 				if(key == hotbutton.serverkey)
 					continue;
 			
 				SocketChannel client = (SocketChannel)key.channel();
 				client.write(buffer);
 			}
 		}
 		
 		// kick user
 		if(path.startsWith("/kick")) {
 			String query = requestedUri.getQuery();
 			Map<String, String> map = getQueryMap(query);
 			String strId = map.get("id");
 			int intId = Integer.parseInt(strId);
 			Set<SelectionKey> allKeys = hotbutton.selector.keys();
 			for(SelectionKey key : allKeys)
 			{
 				if(key == hotbutton.serverkey)
 					continue;
 					
 				if(--intId == 0) {
 					SocketChannel client = (SocketChannel)key.channel();
 					String username = (String)key.attachment();
 					client.close();
 					key.cancel();
 					Log.d("admin", "kicked " + username +  " from the server!");
 					break;
 				}
 			}
 		}
 		
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
 		"<a class=\"large orange awesome\" href=unlock>Unlock all Hotbuttons</a>" +
 		"<a class=\"large green awesome\" href=lock>Lock all Hotbuttons</a>" + 
 		"<a class=\"large magenta awesome\" href=/>Refresh</a>" +
 		"</div>" +
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
 		int i = 0;
 		Set<SelectionKey> allKeys = hotbutton.selector.keys();
 		for(SelectionKey key : allKeys)
 		{
 			if(key == hotbutton.serverkey)
 				continue;
 				
 			String username = (String)key.attachment();
 			SocketChannel client = (SocketChannel)key.channel();
 			i++;
 			out.write("<li><strong>" + username + "</strong> [" + client.socket().getRemoteSocketAddress() + "] <a href=/kick?id=" + i + " class=\"small awesome red\">kick</a></li>");
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
 	static List<String> processCommand(SelectionKey key, List<String> request)
 	{
 		List<String> response = new ArrayList<String>();
 		
 		// hi
 		if(request.get(0).equals("hi")) {
 			response.add("hi");
 			response.add("hotbutton");
 			response.add("v0.0.1");
 		}
 		
 		// login
 		else if(request.get(0).equals("login")) {
 			String new_username = request.get(1);
 			String old_username = (String)key.attachment();
 			key.attach(new_username);
 			response.add("login");
 			response.add("okay");
 			Log.d("network", old_username + " changed his username to " + new_username);
 		}
 		
 		// protocol violation
 		else
 			Log.d("network", "protocol violation by " + (String)key.attachment());
 		
 		// return response
 		return response;
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
 				client.configureBlocking(false);
				client.register(selector, SelectionKey.OP_READ | );
 				Log.d("network", "Accepted connection from " + client);
 			}
 			
 			// message from client
 			else if(key.isReadable())
 			{
 				try
 				{
 					// read data to buffer
 					SocketChannel client = (SocketChannel)key.channel();
 					ByteBuffer readBuffer = ByteBuffer.allocate(512);
 					int bytesread = client.read(readBuffer);
 					if (bytesread == -1) {
 						key.cancel();
 						client.close();
 					}
 					
 					// need a string
 					if(!readBuffer.hasArray()) {
 						Log.d("network", "Ignoring unknown sequeenze from " + client);
 						continue;
 					}
 					
 					// fetch command from buffer
 					String strCommand = new String(readBuffer.array()).trim();
 					List<String> lstCommand = Arrays.asList(strCommand.split("-"));
 					Log.d("network", strCommand);
 					
 					// process Command
 					List<String> lstResponse = processCommand(key, lstCommand);
 					
 					Iterator<String> itResponse = lstResponse.iterator();
 					StringBuilder strResponse = new StringBuilder();
 					while(itResponse.hasNext()) {
 						strResponse.append(itResponse.next());
 						if(itResponse.hasNext())
 							strResponse.append("-");
 					}
 					strResponse.append("\r\n");
 					
 					// send response
 					ByteBuffer writeBuffer = ByteBuffer.wrap(strResponse.toString().getBytes());
 					client.write(writeBuffer);
 					
 				} catch(ClosedChannelException ex) {
 					System.out.println(ex.toString());
 				}
 			}
 		}
 	}
 }
