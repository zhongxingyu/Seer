 
 package server;
 
 import java.util.Calendar;
 
 import protocol.ISendable;
 import protocol.IServerConnection;
 import protocol.IServerHandler;
 import protocol.packets.ClientConnect;
 import protocol.packets.ClientReconnect;
 import protocol.packets.ConnectAck;
 import protocol.packets.CoreMessage;
 import protocol.packets.SendMessage;
 
 public class ClientProtocolHandler implements IServerHandler<ClientSession> 
 {
 	@Override
 	public void onClose(IServerConnection<ClientSession> connection) 
 	{
	    if (connection.getAttachment().getRoom() != null)
	        RingServer.RingHandler().removeClient(connection.getAttachment());
 	}
 
 	@Override
 	public void onConnect(IServerConnection<ClientSession> connection) 
 	{	
 	    // TODO : Load balancing decisions.
         // E.G. whether to reject clients
 		connection.setAttachment(new ClientSession(connection));
 	}
 
 	@Override
 	public void onPacket(IServerConnection<ClientSession> connection,
 			ISendable packet) {
 
 		ClientSession sess = connection.getAttachment();
 
 		// TODO what about FIND_ROOM? how does this get handled
 		switch(packet.getPacketType()) {
 		case CLIENT_CONNECT:
 			handleConnect(sess, (ClientConnect) packet);
 			break;
 
 		case CLIENT_RECONNECT:
 			handleReconnect(sess, (ClientReconnect) packet);
 			break;
 
 		case SEND_MESSAGE:
 			handleSendMessage(sess, (SendMessage) packet);
 			break;
 		}
 	}
 
 	private void handleConnect(ClientSession sess, ClientConnect cn) {
 		// init session and ack
 		sess.onConnect(cn);
 		ackConnect(sess, cn.getReplyCode());
 		
 		RingServer.RingHandler().addClient(sess);
 	}
 
 	/**
 	 * Handle a client reconnect request
 	 * 
 	 * @param connection
 	 * @param sess
 	 * @param crn
 	 */
 	private void handleReconnect(ClientSession sess, ClientReconnect crn) {
 		// init session and ack
 		sess.onReconnect(crn);
 		ackConnect(sess, crn.getReplyCode());
 		
 		RingServer.RingHandler().addClient(sess);
 	}
 	
 	/**
 	 * Acknowledge receipt of a ClientConnect or a ClientReconnect
 	 * object.
 	 * 
 	 * @param sess
 	 * @param replyCode
 	 */
 	private void ackConnect(ClientSession sess, long replyCode) {
 		// prepare the packet
 		ConnectAck cak = new ConnectAck(RingServer.Stats().getServerUpdate(sess.getRoom()),
 		        Calendar.getInstance().getTimeInMillis(), replyCode);
 		
 		// deliver
 		sess.deliverToClient(cak);
 	}
 
 	/**
 	 * Internal handler for SendMessage objects from Clients.
 	 * 
 	 * @param sess the ClientSession in question
 	 * @param snd the SendMessage
 	 */
 	private void handleSendMessage(ClientSession sess, SendMessage snd) {
 		// translate sent message into CoreMessage, adding timestamp
 		CoreMessage cm = new CoreMessage(snd);
 
 		// deliver message to clients on this machine and pass on to other nodes
 		RingServer.RingHandler().originateMessage(cm);
 	}
 }
