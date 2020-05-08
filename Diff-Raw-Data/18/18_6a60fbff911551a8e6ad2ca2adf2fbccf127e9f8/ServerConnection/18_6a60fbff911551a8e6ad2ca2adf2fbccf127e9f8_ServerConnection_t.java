 package objectIO.connections.p2pServer.server;
 
 import java.io.IOException;
 import java.net.Socket;
 
 import objectIO.connections.StreamCon;
 import objectIO.connections.p2pServer.P2PMsg;
 
 public class ServerConnection extends StreamCon{
 	private P2PServer server;
 	private Socket socket;
 	
 	public String getAddress() { return socket.getInetAddress().getHostAddress(); }
 	
 	public ServerConnection(Socket socket, P2PServer server) throws IOException {
 		super(socket);
 		this.server = server;
 		this.socket = socket;
 		socket.setKeepAlive(true);
		socket.setTcpNoDelay(true);
 		myId = server.id;
 		if (sendMeetAndGreet(3000) == false) {
 			close();
 			throw new IOException("Communication could not be established!");
 		}
 		super.parser = this.parser;
 		startListening();
 	}
 	
 	private InputParser parser = new InputParser() {
 		@Override
 		public void parseInput(String input) {
 			if (input != null) {
 				P2PMsg msg = new P2PMsg(input, 0);
 				server.forwardMsg(msg);
 			} else {
 				disconnect();
 			}
 		}
 	};
 	
 	public void disconnect() {
 		close();
 		server.disconnect(this);
 	}
 	
 	public void close() {
 		try {
 			socket.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		if (isClosed() == false)
 			super.close();
 	}
 	
 }
