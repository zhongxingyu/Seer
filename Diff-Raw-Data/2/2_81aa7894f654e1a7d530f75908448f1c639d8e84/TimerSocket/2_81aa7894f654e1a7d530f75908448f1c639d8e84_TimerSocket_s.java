 package ru.kt15.finomen.neerc.timer;
 
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
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicLong;
 
 import ru.kt15.finomen.neerc.core.Log;
 
 public class TimerSocket implements Runnable {
 	private final TimerWindow window;
 	private final DatagramChannel channel;
 	private MembershipKey memberKey = null;
 	private final Thread worker, watch;
 	private final SocketAddress server;
 	private AtomicLong lastSync;
 	
 	public TimerSocket(TimerWindow wnd, Map<String, Object> config) throws IOException {
 		this.window = wnd;
 		channel = DatagramChannel.open(StandardProtocolFamily.INET);
 		channel.configureBlocking(true);
 		channel.setOption(StandardSocketOptions.SO_BROADCAST, true);
 		channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
 		
 		channel.bind(new InetSocketAddress((Integer)config.get("udp-port")));
 		
 		if (config.containsKey("multicast-group")) {
 			InetAddress groupAddr = InetAddress.getByName((String)config.get("multicast-group"));
 			NetworkInterface interf = NetworkInterface.getByName((String)config.get("multicast-iface"));
 			try {
 				channel.setOption(StandardSocketOptions.IP_MULTICAST_IF, interf);
 				memberKey = channel.join(groupAddr, interf);
 				Log.writeInfo("Joined group " + config.get("multicast-group") + " on interface " + interf.getDisplayName() + "[" + interf.getName() + "]");				
 			} catch (IOException e)
 			{
 				Log.writeError("Failed to join group " + config.get("multicast-group") + " on interface " + interf.getDisplayName());
 				Log.writeInfo("Available interfaces");
 				for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
 					Log.writeInfo(iface.getName() + ": " + iface.getDisplayName());
 				}
 			}
 		}
 		
 		if (config.containsKey("server-host")) {
 			server = new InetSocketAddress((String)config.get("server-host"), (Integer)config.get("server-port"));
 		} else {
 			server = null;
 		}
 		
 		worker = new Thread(this);
 		worker.start();
 		lastSync = new AtomicLong(new Date().getTime());
 		
 		watch = new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				while (true) {
 					long cTime = new Date().getTime();
 					long lSync = lastSync.get();
 					if (cTime - lSync > 1000) {
 						ByteBuffer buf = ByteBuffer.allocate(1);
 						buf.put((byte) 0x01);
 						buf.flip();
 						try {
 							Log.writeInfo("Register as unicast client");
 							channel.send(buf, server);
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 					}
 					
 					synchronized (this) {
 						try {
 							wait(5000);
 						} catch (InterruptedException e) {
 							return;
 						}
 					}
 				}
 			}
 		});
 		
 		if (server != null) {
 			watch.start();
 		}
 	}
 
 	@Override
 	public void run() {
		while (true) {
 			try {
 				ByteBuffer buf = ByteBuffer.allocate(512);
 				/*SocketAddress remote = */channel.receive(buf);
 				buf.flip();
 				byte cmd = buf.get();
 				switch (cmd) {
 				case 0x01:
 					int status = buf.get();
 					long time = buf.getLong();
 					long duration = buf.getLong();
 					window.Sync(TimerStatus.getById(status), duration, duration - time);
 					lastSync.set(new Date().getTime());
 					break;
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			//buf.put((byte) 0x01);
 			//buf.put((byte) cl.getStatus());
 			//buf.putLong(cl.getTime());
 			//buf.putLong(cl.getLength());
 			//buf.flip();
 		}
 	}
 
 	public void stop() {
 		worker.interrupt();
 		watch.interrupt();
 		try {
 			worker.join();
 			watch.join();
 		} catch (InterruptedException e) {
 			//FIXME:
 		}
 	}
 }
