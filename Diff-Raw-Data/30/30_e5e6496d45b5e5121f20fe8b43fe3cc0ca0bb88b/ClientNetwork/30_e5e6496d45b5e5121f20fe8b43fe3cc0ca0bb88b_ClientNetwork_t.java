 package ch.bfh.monopoly.net;
 
 import java.net.InetSocketAddress;
 
 import org.apache.mina.core.future.ConnectFuture;
 import org.apache.mina.core.service.IoConnector;
 import org.apache.mina.core.session.IoSession;
 import org.apache.mina.filter.codec.ProtocolCodecFilter;
 import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
 import org.apache.mina.filter.logging.LoggingFilter;
 import org.apache.mina.transport.socket.nio.NioSocketConnector;
 
 
 public class ClientNetwork {
 
 	private ClientHandler cliHandler;
 	private IoConnector ic;
 	private IoSession clientSession;
 
 	public ClientNetwork(ClientHandler cliHandler){
 		this.cliHandler = cliHandler;
 	}
 	
 	/**
 	 * Connect to a server 
 	 * @param ip the ip of the server
 	 * @param port the port of the server
 	 */
 	public void startClient(String ip, int port){
 		ic = new NioSocketConnector();
 		ic.getFilterChain().addLast("logger", new LoggingFilter());
 		ic.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
 		ic.setHandler(cliHandler);
 		ConnectFuture cf = ic.connect(new InetSocketAddress(ip, port));
 
 		// TODO what's this?? check the documentation
 		cf.awaitUninterruptibly();
 		
 		this.clientSession = cf.getSession();	
 	}
 	
 	/**
 	 * Send a netmessage to the server 
 	 * @param nm NetMessage
 	 * 			the NetMessage to send to the server
 	 */
 	public void sendMessage(NetMessage nm) {
 		try {
 			System.out.println("SENT MESSAGW FROM CLIENT NETWORK");
			
			long t1 = System.currentTimeMillis();
			
 			this.clientSession.write(nm).await();
			
			long t2 = System.currentTimeMillis();
			
			System.out.println("ELAPSED TIME TO SENT THE MESSAGE: " + (t2-t1));
			
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Close the currently active session
 	 */
 	public void closeConnection(){
 		this.clientSession.close(true);
 	}
 }
