 package pcms2.timer;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.NetworkInterface;
 import java.net.SocketAddress;
 import java.net.StandardProtocolFamily;
 import java.net.StandardSocketOptions;
 import java.nio.ByteBuffer;
 import java.nio.channels.DatagramChannel;
 import java.nio.channels.MembershipKey;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.rmi.RemoteException;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import pcms2.framework.BaseComponent;
 import pcms2.framework.IThread;
 import pcms2.framework.config.ConfigurationException;
 import pcms2.services.site.Clock;
 import pcms2.services.site.ClockService;
 import pcms2.services.site.NoSuchClockException;
 
 
 public class TimerNotifier extends BaseComponent implements ITimerNotifier {
 	private IThread worker;
 	private ClockService cs;
 	private String clockId;
 	private DatagramChannel udpChannel = null;
 	private Set<MembershipKey> multicastKey = new HashSet<MembershipKey>();
 	private SelectionKey channelKey = null;
 	private SocketAddress multicastAddress;
 	private SocketAddress broadcastAddress = null;
 	private Set<SocketAddress> clients = new HashSet<SocketAddress>();
 	private long timeout;
 	private Selector selector;
 
 	public void activate() throws ConfigurationException, IOException {
 		registerService("TimerNotifier", ITimerNotifier.class, this);
 		log.info("Timer notifier service registered");
 		try {
 			cs = (ClockService) lookupService(ClockService.class);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 
 		clockId = config.getString("clock-id");
 		timeout = config.getInt("sync-timeout");
 		
 
 		udpChannel = DatagramChannel.open(StandardProtocolFamily.INET)
 				.setOption(StandardSocketOptions.SO_REUSEADDR, true);
 
 		udpChannel.configureBlocking(false);
 		
 		selector = Selector.open();
 		channelKey = udpChannel.register(selector, SelectionKey.OP_READ, null);
 
 		if (config.hasAttribute("udp-listen-port")) {
 			int port = config.getInt("udp-listen-port");
 			log.info("Begin listening for datagrams on port " + port + "...");
 			udpChannel.bind(new InetSocketAddress(port));
 		}
 
 		int port = config.getInt("udp-port");
 
 		if (config.hasAttribute("multicast-group")) {
 			log.info("Joining multicast group...");
 			String group = config.getString("multicast-group");
 			InetAddress groupAddr = InetAddress.getByName(group);
 			for (NetworkInterface interf : Collections.list(NetworkInterface.getNetworkInterfaces())) {
 				try {
 					udpChannel.setOption(StandardSocketOptions.IP_MULTICAST_IF, interf);
 					multicastKey.add(udpChannel.join(groupAddr, interf));
 					log.info("Joined group " + config.get("multicast-group") + " on interface " + interf.getDisplayName() + "[" + interf.getName() + "]");
 				} catch (IOException e)
 				{
 					log.warning("Failed to join group " + config.get("multicast-group") + " on interface " + interf.getDisplayName());
 				}
 			}
 						
 			multicastAddress = new InetSocketAddress(groupAddr, port);
 			log.info("Multicast notifier initiated");
 		}
 
 		if (config.hasAttribute("broadcast-addr")) {
 			log.warning("Broadcast is very bad idea");
 			String broadcastAddr = config.getString("broadcast-addr");
 			broadcastAddress = new InetSocketAddress(
 					InetAddress.getByName(broadcastAddr), port);
 			udpChannel.setOption(StandardSocketOptions.SO_BROADCAST, true);
 		}
 
 		if (config.hasAttribute("tcp-port")) {
 			// log.info("Initiating TCP notifier");
 			// int port = config.getInt("tcp-port");
 			log.error("TCP notifier not implemented");
 			// log.info("TCP notifier initiated");
 		}
 
 		worker = startThread(this, "timer-notifier-thread");
 	}
 
 	public void passivate() {
 		worker.interrupt();
 		try {
 			worker.join();
 		} catch (InterruptedException e) {
 			Thread.currentThread().interrupt();
 		}
 	}
 
 	private void sendSync() throws NoSuchClockException, IOException {
 		if (cs == null) {
 			log.warning("Sync without clock service");
 			return;
 		}
 		Clock cl = cs.getClock(clockId);
 		ByteBuffer buf = ByteBuffer.allocate(18);
 		buf.put((byte) 0x01);
 		buf.put((byte) cl.getStatus());
 		buf.putLong(cl.getTime());
 		buf.putLong(cl.getLength());
 		buf.flip();
 
 		for (MembershipKey key : multicastKey) {
 			DatagramChannel channel = (DatagramChannel) key.channel();
 			try {
 				buf.rewind();
 				channel.send(buf, multicastAddress);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		if (broadcastAddress != null) {
 			try {
 				buf.rewind();
 				udpChannel.send(buf, broadcastAddress);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	
 		for (SocketAddress sa : clients) {
 			buf.rewind();
 			udpChannel.send(buf, sa);
 		}
 	}
 
 	@Override
 	public void run() {
 		long lastSync = 0;
 		while (!Thread.interrupted()) {
 			long cTime = (new Date()).getTime();		
 			
 			if (cTime - lastSync >= timeout) {
 				channelKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
 			}
 			
 			try {
 				if (selector.select(Math.max(timeout - (cTime - lastSync), 10)) == 0) {
 					continue;
 				}
 				
 				Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
 				while (keyIter.hasNext()) {
 					SelectionKey key = keyIter.next();
 					if (key.isReadable()) {
 						ByteBuffer buf = ByteBuffer.allocate(16);
 						InetSocketAddress sa = (InetSocketAddress)udpChannel.receive(buf);
 						buf.flip();
 						if (buf.limit() == 1 && buf.get() == 0x01) {
 							clients.add(sa);
 							log.info("Register timer " + sa.getHostString() + ":" + sa.getPort() + " [" + clients.size() + "]");
 						} else if (buf.limit() == 1 && buf.get() == 0x02) {
 							log.info("Remove timer " + sa.getHostString());
 							clients.remove(sa);
 						} else if (buf.limit() == 9 && buf.get() == 0x03) {
 							log.warning("NOT IMPLEMENTED! Echo from " + sa.getHostString());
 						}						
 					}
 
 					if (key.isValid() && key.isWritable()) {
 						sendSync();
 						channelKey.interestOps(SelectionKey.OP_READ);
 					}
 
 					keyIter.remove();
 				}
 				
 			} catch (IOException e2) {
 				log.error("IOException: " + e2.getLocalizedMessage());
 			} catch (NoSuchClockException e) {
 				log.error("Clock " + clockId + " does not exists!");
 			}
			
			try {
				synchronized (this) {
					this.wait(400);
				}
			} catch (InterruptedException e) {
				return;
			}
 		}
 	}
 
 }
