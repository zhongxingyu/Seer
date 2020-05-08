 package ru.kt15.finomen;
 
 import java.io.IOException;
 import java.net.Inet4Address;
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.net.StandardProtocolFamily;
 import java.net.StandardSocketOptions;
 import java.nio.ByteBuffer;
 import java.nio.channels.DatagramChannel;
 import java.nio.channels.SelectionKey;
import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Queue;
import java.util.Set;
 
 public class DatagramConnection extends WritablePacketConnection implements
 		SelectionListener {
 	private final DatagramChannel channel;
 	// private final IOService ioService;
 	private final Queue<Packet> sendQueue = new LinkedList<>();
 	private final SelectionKey key;
 
 	private static class Packet {
 		public Packet(InetSocketAddress destination, byte[] data) {
 			this.destination = destination;
 			this.data = data;
 		}
 
 		InetSocketAddress destination;
 		byte[] data;
 	}
 
 	public DatagramConnection(IOService ioService, boolean broadcast,
 			InetSocketAddress bindAddress) throws IOException {
 		// this.ioService = ioService;
 		channel = DatagramChannel
 				.open(bindAddress.getAddress() instanceof Inet4Address ? StandardProtocolFamily.INET
 						: StandardProtocolFamily.INET6);
 		channel.configureBlocking(false);
 		channel.setOption(StandardSocketOptions.SO_BROADCAST, broadcast);
 		channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
 		channel.bind(bindAddress);
 
 		key = ioService.add(channel, SelectionKey.OP_READ, this);
 	}
 
 	public DatagramConnection(IOService ioService, short port, boolean broadcast)
 			throws IOException {
 		this(ioService, broadcast, new InetSocketAddress(port));
 	}
 
 	public DatagramConnection(IOService ioService, short port)
 			throws IOException {
 		this(ioService, port, false);
 	}
 
 	public DatagramConnection(IOService ioService) throws IOException {
 		this(ioService, (short) 0, false);
 	}
 
 	@Override
 	public void send(InetSocketAddress destination, byte[] data) {
 		Packet packet = new Packet(destination, data);
 		synchronized (sendQueue) {
 			sendQueue.add(packet);
 		}
 		key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
 	}
 
 	// Got broadcast
 	@Override
 	public void handleRead(SelectionKey key) throws IOException {
 		ByteBuffer buf = ByteBuffer.allocate(65535);
 		SocketAddress source = channel.receive(buf);
 		buf.flip();
 		byte[] packet = new byte[buf.remaining()];
 		buf.get(packet);
 
 		onRecv((InetSocketAddress) source,
 				(InetSocketAddress) channel.getLocalAddress(), packet);
 	}
 
 	// Send broadcast
 	@Override
 	public void handleWrite(SelectionKey key) throws IOException {
 		Packet packet = null;
 		synchronized (sendQueue) {
 			if (!sendQueue.isEmpty()) {
 				packet = sendQueue.poll();
 			}
 		}
 
 		if (packet != null) {
 			channel.send(ByteBuffer.wrap(packet.data), packet.destination);
 		}
 
 		onSend((InetSocketAddress) channel.getLocalAddress(),
 				(InetSocketAddress) packet.destination, packet.data);
 
 		synchronized (sendQueue) {
 			if (sendQueue.isEmpty()) {
 				key.interestOps(SelectionKey.OP_READ);
 			}
 		}
 	}
 
 	@Override
 	public void handleAccept(SelectionKey key) {
 	}
 }
