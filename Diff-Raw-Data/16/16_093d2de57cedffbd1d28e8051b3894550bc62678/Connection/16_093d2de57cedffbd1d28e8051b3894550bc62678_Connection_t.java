 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.ConnectException;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.StringTokenizer;
 
 public class Connection {
 	static final int TIMEOUT = 507;
 	private String host;
 	private int port;
 	private String loginName;
 
 	private Socket s;
 	private PrintStream w = null;
 	private BufferedReader r = null;
 
 	private boolean connected = false;
 	private String notConnectedInfo;
 
 	private ReconnectWorker rw;
 
 	Connection(String host, int port, String loginName) {
 		this.host = host;
 		this.port = port;
 		StringTokenizer izer = new StringTokenizer(loginName);
 		this.loginName = izer.nextToken();
 
 		if (host == null || loginName == null)
 			return;
 
 		connect();
 	}
 
 	public void send(String string) {
 		w.println(string.trim());
 		w.flush();
 	}
 
 	public String recv() throws IOException {
 		String lineRead = null;
 		try {
 			lineRead = r.readLine();
 		} catch (SocketException e) {
 
 		}
 
 		if (lineRead != null)
 			return lineRead.trim();
 
 		return null;
 	}
 
 	public boolean connected() {
 		return connected;
 	}
 
 	public String connectionInfo() {
 		if (connected())
 			return getHost() + ":" + getPort();
 		if (notConnectedInfo != null)
 			return notConnectedInfo;
 		return "---";
 	}
 
 	public String getHost() {
 		return host;
 	}
 
 	int getPort() {
 		return port;
 	}
 
 	String getLoginName() {
 		return loginName;
 	}
 
 	void disconnect() {
		if (rw != null)
			rw.end();
 
 		try {
 			if (w != null)
 				w.close();
 			if (r != null)
 				r.close();
 			if (s != null)
 				s.close();
 		} catch (IOException e) {
 			System.err.println("Disconnect: closing failed");
 		}
 
 		connected = false;
 	}
 
 	void reconnect() {
 		if (rw != null) {
 			rw.end();
 		}
 		
 		rw = new ReconnectWorker();
 		rw.start();
 	}
 
 	private void connect() {
 		try {
 			s = new Socket(host, port);
 			s.setSoTimeout(TIMEOUT);
 			w = new PrintStream(s.getOutputStream());
 			r = new BufferedReader(new InputStreamReader(s.getInputStream()));
 
 			send("LOGIN " + loginName);
 			connected = "OK".equals(recv());
 		} catch (ConnectException e) {
 			System.err.println("Connection refused");
 			notConnectedInfo = "Connection refused";
 		} catch (UnknownHostException e) {
 			System.err.println("Unknown host");
 			notConnectedInfo = "Unknown host";
 			return;
 		} catch (IOException e) {
 			System.err.println("Input/Output error");
 			notConnectedInfo = "Input/Output error";
 			return;
 		}
 	}
 
 	private class ReconnectWorker extends Thread {
 		private boolean doRun = true;
 		private final int RECONNECT_INTERVAL = 1000;
 
 		public void end() {
 			doRun = false;
 			interrupt();
 		}
 
 		public void run() {
 			while (doRun) {
 				connect();
 				if (connected)
 					break;
 
 				try {
 					Thread.sleep(RECONNECT_INTERVAL);
 				} catch (InterruptedException e) {
 				}
 			}
 		}
 	}
 }
