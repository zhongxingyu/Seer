 package de.fettlaus.thekraken.model;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.SocketException;
 
 import de.fettlaus.thekraken.events.EventBus;
 import de.fettlaus.thekraken.events.NewNotificationEvent;
 import de.fettlaus.thekraken.events.NewNotificationEvent.NotificationType;
 
 public class UDPConnection implements Runnable {
 	// BlockingQueue<Message> messages;
 	DatagramSocket socket;
 	// InetAddress address;
 	// int port;
 	ByteArrayOutputStream baos;
 	// Semaphore socketInUse;
 	// byte[] pingmessage;
 	// private ConnectionBundle parent;
 	DataOutputStream dos;
 
 	public UDPConnection() {
 		super();
 		baos = new ByteArrayOutputStream();
 	}
 
 	public void connect() throws SocketException {
 		socket = new DatagramSocket();
 		dos = new DataOutputStream(baos);
 	}
 
 	@Override
 	public void run() {
 
 		final byte[] buffer = new byte[Message.HEADER_LENGTH + Message.MAX_BODY_LENGTH];
 		ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
 		final DataInputStream dis = new DataInputStream(bais);
 		Message msg = new KrakenMessage();
 		DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
 		while (true) {
 			try {
 				dis.reset();
 				socket.receive(incoming);
 				long now = TimeKeeper.time();
 				msg.read(dis);
 				if (msg.getType() == MessageType.PONG) {
 					KrakenMessage out = new KrakenMessage(MessageType.STIM, now, "");
 					byte[] buf = out.toByteArray();
 					DatagramPacket dp = new DatagramPacket(buf, buf.length,incoming.getAddress(),incoming.getPort());
 					socket.send(dp);
 				}
 			} catch (final Exception e) {
 				EventBus.instance().post(new NewNotificationEvent(NotificationType.UDP_ERROR));
 			}
 		}
 
 	}
 
 	public synchronized void sendPing(Connection con) throws IOException {
 		// create high-precision ping
 		baos.reset();
 		final Message ping = new KrakenMessage(MessageType.PING, TimeKeeper.time(), "");
 		ping.write(dos);
 		final byte[] out = baos.toByteArray();
 		socket.send(new DatagramPacket(out, out.length, con.getAddress(), con.getPort()));
 
 	}
 
 }
