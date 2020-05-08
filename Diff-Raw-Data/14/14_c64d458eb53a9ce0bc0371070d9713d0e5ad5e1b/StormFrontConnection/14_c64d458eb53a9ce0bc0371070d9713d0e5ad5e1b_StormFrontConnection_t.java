 /*
  * Created on Jan 11, 2005
  */
 package cc.warlock.network;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.util.ArrayList;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import cc.warlock.client.IWarlockClient;
 import cc.warlock.client.stormfront.IStormFrontClient;
 import cc.warlock.stormfront.internal.StormFrontProtocolHandler;
 
 /**
  * @author Sean Proctor
  * @author Marshall
  *
 * The Internal Storm Front protocol handler. Not meant to be instantiated outside of Warlock.
  */
 public class StormFrontConnection implements IConnection {
 	protected String host;
 	protected int port;
 	protected IStormFrontClient client;
 	protected Socket socket;
 	protected String key;
 	private ArrayList<IConnectionListener> listeners = new ArrayList<IConnectionListener>();
 	
 	public StormFrontConnection (IStormFrontClient client, String key) {
 		this.client = client;
 		this.key = key;
 	}
 	
 	public void connect(String host, int port)
 	throws IOException {
 		this.host = host;
 		this.port = port;
 		
 		new Thread(new XmlParser()).start();
 	}
 	
 	public void disconnect()
 	throws IOException {
 		// Stub Function
 	}
 	
 	public void addConnectionListener (IConnectionListener listener) {
 		listeners.add(listener);
 	}
 	
 	public void send (String toSend)
 	throws IOException {
 		send(toSend.getBytes());
 	}
 	
 	public void send (byte[] bytes)
 	throws IOException {
 		socket.getOutputStream().write(bytes);
 	}
 	
 	public void sendLine (String line)
 	throws IOException {
 		send ("<c>" + line + "\n");
 	}
 	
 	public IWarlockClient getClient() {
 		return client;
 	}
 	
 	public void dataReady (String data)
 	{
 		for (IConnectionListener listener : listeners) listener.dataReady(this, data);
 	}
 	
 	class XmlParser implements Runnable {
 		public void run() {
 			try {
 				socket = new Socket(host, port);
 				
 				sendLine(key);
 				sendLine("/FE:WARLOCK /VERSION:1.0.1.22 /XML");
 				
 				StormFrontStream inputStream = new StormFrontStream(StormFrontConnection.this, socket.getInputStream());
 
 				SAXParserFactory factory = SAXParserFactory.newInstance();
 				SAXParser saxParser = factory.newSAXParser();
 				saxParser.parse(inputStream, new StormFrontProtocolHandler(client));
 			} catch (Throwable t) {
 				t.printStackTrace();
 			}
 		}
 	}
 
 	public String getHost() {
 		return host;
 	}
 
 	public int getPort() {
 		return port;
 	}
 
 }
