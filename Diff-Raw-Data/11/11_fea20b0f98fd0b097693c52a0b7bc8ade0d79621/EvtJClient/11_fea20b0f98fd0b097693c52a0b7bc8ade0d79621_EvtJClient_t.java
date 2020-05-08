 package bytecrawl.evtj.utils;
 
 import java.io.IOException;
 import java.nio.channels.SocketChannel;
 
 import org.apache.log4j.Logger;
 
 public class EvtJClient {
 
 	private SocketChannel channel;
 	private String IP;
 	private Logger logger = Logger.getLogger("app");
 
 	public EvtJClient(SocketChannel channel) throws IOException {
 		this.channel = channel;
		this.IP = channel.socket().getRemoteSocketAddress().toString();
 	}
 
 	public SocketChannel getChannel() {
 		return channel;
 	}
 
 	public String getIP() {
 		return IP;
 	}
 
 	public void close() {
 		try {
 			channel.close();
 		} catch (IOException e) {
 			logger.error("Error closing channel", e);
 		}
 	}
 }
