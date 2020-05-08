 package ru.kt15.finomen;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.net.StandardSocketOptions;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class ServerConnection extends WritablePacketConnection implements
 		SelectionListener {
 	private final StreamProtocolDefinition protocol;
 	private final IOService ioService;
 	private final Map<SocketAddress, StreamConnection> clients = new HashMap<>();
 	private final ServerSocketChannel channel;
 
 	private final DataListener recvListener = new DataListener() {
 
 		@Override
 		public void handlePacket(PacketConnection conn,
 				InetSocketAddress source, InetSocketAddress dest, List<Token<?>> packet) {
 			onRecv(conn, source, dest, packet);
 		}
 	};
 	
 	private final PacketListener recvListenerRaw = new PacketListener() {
 
 		@Override
 		public void handlePacket(PacketConnection conn,
 				InetSocketAddress source, InetSocketAddress dest, byte[] packet) {
 			onRecv(conn, source, dest, packet);
 		}
 	};
 
 	private final PacketListener sendListener = new PacketListener() {
 
 		@Override
 		public void handlePacket(PacketConnection conn,
 				InetSocketAddress source, InetSocketAddress dest,  byte[] packet) {
 			onSend(conn, source, dest, packet);
 		}
 	};
 
 	public ServerConnection(StreamProtocolDefinition protocol,
 			IOService ioService, int port) throws IOException {
 		this.protocol = protocol;
 		this.ioService = ioService;
 		channel = ServerSocketChannel.open();
 		channel.configureBlocking(false);
 		channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
 		channel.bind(new InetSocketAddress(port));
 		ioService.add(channel, SelectionKey.OP_ACCEPT, this);
 	}
 
 	@Override
 	public void handleRead(SelectionKey key) throws IOException {
 	}
 
 	@Override
 	public void handleWrite(SelectionKey key) throws IOException {
 	}
 
 	@Override
 	public void handleAccept(SelectionKey key) throws IOException {
 		SocketChannel client = channel.accept();
 		StreamConnection conn = new StreamConnection(client, protocol.clone(),
 				ioService);
 		conn.addRecvListener(recvListener);
 		conn.addSendListener(sendListener);
 		clients.put(client.getRemoteAddress(), conn);
 	}
 
 	@Override
 	public void send(InetSocketAddress dest, byte[] data) {
 		if (clients.containsKey(dest)) {
 			clients.get(dest).Send(data);
 		}
 	}
 
 }
