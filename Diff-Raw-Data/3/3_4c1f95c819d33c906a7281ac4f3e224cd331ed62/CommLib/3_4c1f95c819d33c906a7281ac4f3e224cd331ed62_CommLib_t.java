 package edu.vub.at.commlib;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import android.util.Log;
 
 import com.esotericsoftware.kryo.Kryo;
 import com.esotericsoftware.kryo.io.Input;
 import com.esotericsoftware.kryo.io.Output;
 
 import edu.vub.at.nfcpoker.comm.Message.ClientAction;
 
 public class CommLib {
 
 	public static final int DISCOVERY_PORT = 54333;
 	public static final int SERVER_PORT = 54334;
 	@SuppressWarnings("rawtypes")
 	public static Map<UUID, Future> futures = new HashMap<UUID, Future>();
 
 	public static CommLibConnectionInfo discover(Class<?> klass) throws IOException {
 		final String targetClass = klass.getCanonicalName();
 		Kryo k = new Kryo();
 		k.setRegistrationRequired(false);
 		k.register(CommLibConnectionInfo.class);
 		k.register(UUID.class, new UUIDSerializer());
 		
 		DatagramSocket ds = new DatagramSocket(DISCOVERY_PORT, InetAddress.getByName("192.168.1.255"));
 		ds.setBroadcast(true);
		ds.setReuseAddress(true);
 		DatagramPacket dp = new DatagramPacket(new byte[1024], 1024);
 		while (true) {
 			ds.receive(dp);
 			CommLibConnectionInfo clci = k.readObject(new Input(dp.getData()), CommLibConnectionInfo.class);
 			if (clci.serverType_.equals(targetClass)) {
 				ds.close();
 				return clci;
 			}
 		}
 	}
 	
 	public static void export(CommLibConnectionInfo clci) throws IOException {
 		Kryo k = new Kryo();
 		k.setRegistrationRequired(false);
 		k.register(CommLibConnectionInfo.class);
 		k.register(UUID.class, new UUIDSerializer());
 		Output o = new Output(1024);
 		k.writeObject(o, clci);
 		final byte[] buf = o.toBytes();
 		
 		DatagramSocket ds = new DatagramSocket();
 		ds.setBroadcast(true);
		ds.setReuseAddress(true);
 		DatagramPacket dp = new DatagramPacket(buf, buf.length);
 		ds.connect(new InetSocketAddress(InetAddress.getByName("192.168.1.255"), DISCOVERY_PORT));
 		while (true) {
 			ds.send(dp);
 			try {
 				Thread.sleep(2000);
 			} catch (InterruptedException e) {
 			}
 		}
 	}
 	
 	public static Future<ClientAction> createFuture() {
 		Future<ClientAction> f = new Future<ClientAction>(null);
 		futures.put(f.getFutureId(), f);
 		return f;
 	}
 
 	public static void resolveFuture(UUID futureId, Object futureValue) {
 		@SuppressWarnings("unchecked")
 		Future<Object> f = futures.remove(futureId);
 		if (f == null) {
 			Log.w("wePoker", "Future null!");
 			return;
 		}
 		f.resolve(futureValue);	
 	}
 }
