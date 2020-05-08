 package stv6.http;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.TimeZone;
 
 import stv6.http.pools.ClientPool;
 import stv6.http.request.RequestHandler;
 import stv6.http.request.RequestHandlerManager;
 import stv6.http.request.Response;
 
 public abstract class HttpServer implements Runnable {
 
 	private static SimpleDateFormat dateFormatter 
		= new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
 	
 	static {
 		// make sure it's in GMT
 		dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
 	}
 	
 	private boolean running;
 	private Thread thisThread;
 	
 	private ServerSocket serverskt;
 	private final ClientPool clients;
 	private final RequestHandlerManager responder;
 		
 	public HttpServer(int port) throws IOException {
 		running = false;
 		
 		setPort(port);
 		clients = new ClientPool();
 		responder = getHandlerManager();
 	}
 	
 	/**
 	 * Implement this to use your own client
 	 * 
 	 * @param socket
 	 * @param callback
 	 * @return
 	 */
 	public abstract Client createClient(HttpSocket socket, RequestHandlerManager callback);
 	
 	/**
 	 * Overwrite this if you want to extend RHM somehow
 	 */
 	public RequestHandlerManager getHandlerManager() {
 		return new RequestHandlerManager();
 	}
 
 	public static void generate404(Response resp) {		
 		resp.setStatus("404 Not Found");
 		resp.setContentType("text/html");
 		resp.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\""
 				+" \"http://www.w3.org/TR/html4/strict.dtd\">\n");
 		resp.append("<html>\n<head>\n<title>404 File not found</title>\n");
 		resp.append("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" >\n");
 		resp.append("<link rel=\"stylesheet\" href=\"files/css/default.css\" type=\"text/css\">\n");
 		resp.append("</head>\n<body>\n");
 		resp.append("<h1>404 File not found</h1>I could not find the page you requested.");
 		resp.append(" Please go back and try again.");
 		resp.append("\n</body>\n</html>");
 	}
 	
 	public int getPort() {
 		if (serverskt != null)
 			return serverskt.getLocalPort();
 		
 		return -1;
 	}
 	
 	private Socket getSocket() {
 		try {
 			return serverskt.accept();
 		} catch (IOException e) {
 			return null;
 		}
 	}
 	
 	public void registerHandler(RequestHandler handler) {
 		responder.registerHandler(handler);
 	}
 	
 	@Override
 	public void run() {
 		while (thisThread == Thread.currentThread() && running) {
 			Socket sock = getSocket();
 			if (sock == null)
 				continue;			
 			
 			// woo; a real client!
 			clients.add( createClient(new HttpSocket(sock), responder) );
 			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
 		}
 	}
 	
 	/**
 	 * Set the port we listen on. Note that this
 	 * 	will kill any existing connections!
 	 * @param port
 	 * @throws IOException 
 	 */
 	public void setPort(int port) throws IOException {
 		boolean wasRunning = running;
 		try {
 			stop();
 		} catch (Exception e) {} // whatever
 		serverskt = new ServerSocket(port);
 		
 		if (wasRunning)
 			start(); // as if nothing happened
 	}
 	
 	public void start() {
 		running = true;
 		thisThread = new Thread(this);
 		thisThread.start();
 	}
 	
 	public void stop() {		
 		running = false;
 		thisThread = null;
 		try {
 			serverskt.close();
 		} catch (IOException e) {}
 	}
 
 	public static String formatDate(Date date) {
		return dateFormatter.format( date );
 	}
 	
 	public static Date parseDate(String raw) throws ParseException {
	    try {
	        return dateFormatter.parse(raw);
	    } catch (ParseException e) {
	        System.out.println("Couldn't parse: " + raw);
	        throw e;
	    }
 	}
 }
