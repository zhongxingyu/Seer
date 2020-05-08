 package twpx.core;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 
 public class TWPClient {
 	
 	TWPConnection con;
 	int currentProtocolId;
 	
 	public TWPClient(String host, int port) throws UnknownHostException, IOException {
 		 this(new TWPConnection(host, port));
 	}
 
 	public TWPClient(TWPConnection con) throws UnknownHostException, IOException {
 		this.con = con;
 		con.connect();
 	}
 
 	public Message createMessage(Message message) {
 		message.setConnection(con);
 		return message;
 	}
 
	// todo protocol id switch, check whether to send header or not based on current protocol
 	public TWPClient send(Message message) throws IOException {
 		createMessage(message).send(true);
 		currentProtocolId = message.getProtocolId();
 		return this;
 	}
 
 	// todo protocol/header
 	public Message receive() throws IOException {
 		return MessageManager.receive(con, false, currentProtocolId);
 	}
 
 	public Message sendAndReceive(Message message) throws IOException {
 		return send(message).receive();
 	}
 
 	public void register(Protocol protocol) {
 		MessageManager.register(protocol);
 	}
 }
