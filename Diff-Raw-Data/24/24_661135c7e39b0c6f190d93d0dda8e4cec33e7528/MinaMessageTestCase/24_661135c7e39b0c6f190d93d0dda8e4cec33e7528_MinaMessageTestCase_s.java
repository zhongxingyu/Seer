 package ch.ethz.iks.r_osgi.test;
 
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.mina.common.ConnectFuture;
 import org.apache.mina.common.IdleStatus;
 import org.apache.mina.common.IoHandler;
 import org.apache.mina.common.IoSession;
 import org.apache.mina.filter.LoggingFilter;
 import org.apache.mina.filter.codec.ProtocolCodecFilter;
 import org.apache.mina.transport.socket.nio.SocketAcceptor;
 import org.apache.mina.transport.socket.nio.SocketConnector;
 
 import ch.ethz.iks.r_osgi.messages.DeliverServiceMessage;
import ch.ethz.iks.r_osgi.messages.FetchServiceMessage;
import ch.ethz.iks.r_osgi.messages.InvokeMethodMessage;
 import ch.ethz.iks.r_osgi.messages.LeaseMessage;
 import ch.ethz.iks.r_osgi.messages.LeaseUpdateMessage;
 import ch.ethz.iks.r_osgi.messages.RemoteOSGiMessage;
 import ch.ethz.iks.r_osgi.transport.mina.codec.RemoteOSGiProtocolCodecFactory;
 
 import junit.framework.TestCase;
 
 public class MinaMessageTestCase extends TestCase implements IoHandler {
 
 	private SocketAcceptor acceptor;
 
 	private SocketConnector connector;
 
 	private IoSession clientSession;
 
 	private List queue = new ArrayList();
 
 	private String serviceID = "99";
 
 	private byte[] bytes;
 
 	public MinaMessageTestCase() {
 		bytes = new byte[80000];
 		Arrays.fill(bytes, (byte) 0x42);
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		acceptor = new SocketAcceptor();
 		// acceptor.getFilterChain().addLast("logger", new LoggingFilter());
 		acceptor.getFilterChain().addLast("protocol",
 				new ProtocolCodecFilter(new RemoteOSGiProtocolCodecFactory()));
 		acceptor.bind(new InetSocketAddress(10000), this);
 
 		connector = new SocketConnector();
 		// connector.getFilterChain().addLast("logger", new LoggingFilter());
 		connector.getFilterChain().addLast("protocol",
 				new ProtocolCodecFilter(new RemoteOSGiProtocolCodecFactory()));
 		final ConnectFuture connectFuture = connector.connect(
 				new InetSocketAddress("localhost", 10000), this);
 		while (!connectFuture.isConnected()) {
 
 		}
 		clientSession = connectFuture.getSession();
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		acceptor.unbindAll();
 	}
 
 	// message tests
 
 	public void testDeliverServiceMessage() throws InterruptedException {
 		final DeliverServiceMessage msg = new DeliverServiceMessage();
 
 		final String[] interfaceNames = new String[] { "interface2",
 				"interface1", "interface8", "interfaceA" };
 		final String exports = "export1, export2, export4";
 		final String imports = "import1, import2";
 		final String smartProxyName = "smart.Proxy.Class";
 		final HashMap injections = new HashMap();
 		final byte[] inj1 = new byte[100];
 		Arrays.fill(inj1, (byte) 1);
 		final byte[] inj2 = new byte[10000];
 		Arrays.fill(inj2, (byte) 24);
 		injections.put("inj1", inj1);
 		injections.put("inj2", inj2);
 
 		msg.setXID((short) 100);
 		msg.setServiceID(serviceID);
 		msg.setExports(exports);
 		msg.setImports(imports);
 		msg.setInterfaceNames(interfaceNames);
 		msg.setSmartProxyName(smartProxyName);
 		msg.setInjections(injections);
 		long l = System.nanoTime();
 		clientSession.write(msg);
 
 		final RemoteOSGiMessage msg2 = waitForMessage();
 		System.out.println("TIME " + (System.nanoTime() - l) / 1000);
 
 		assertTrue(msg2 instanceof DeliverServiceMessage);
 		final DeliverServiceMessage rcv = (DeliverServiceMessage) msg2;
 
 		assertEquals(rcv.getXID(), 100);
 		assertEquals(rcv.getServiceID(), serviceID);
 		assertEquals(rcv.getExports(), exports);
 		assertEquals(rcv.getImports(), imports);
 		assertEquals(rcv.getInterfaceNames(), interfaceNames);
 		assertEquals(rcv.getSmartProxyName(), smartProxyName);
 		final Map rcvinj = rcv.getInjections();
 
 		final String[] keys = (String[]) injections.keySet().toArray(
 				new String[injections.size()]);
 
 		assertEquals(injections.size(), rcvinj.size());
 		for (int i = 0; i < keys.length; i++) {
 			assertEquals((byte[]) rcvinj.get(keys[i]), (byte[]) injections
 					.get(keys[i]));
 		}
 	}
 
	public void testFetchServiceMessage() throws InterruptedException {
		final FetchServiceMessage msg = new FetchServiceMessage();
 		msg.setXID((short) 100);
 		msg.setServiceID(serviceID);
 		long l = System.nanoTime();
 		clientSession.write(msg);
 
 		final RemoteOSGiMessage msg2 = waitForMessage();
 		System.out.println("TIME " + (System.nanoTime() - l) / 1000);
 
		assertTrue(msg2 instanceof FetchServiceMessage);
		final FetchServiceMessage rcv = (FetchServiceMessage) msg2;
 
 		assertEquals(rcv.getXID(), 100);
 		assertEquals(rcv.getServiceID(), msg.getServiceID());
 	}
 
	public void testInvokeMethodMessage() throws InterruptedException {
		final InvokeMethodMessage msg = new InvokeMethodMessage();
 		final Object[] arguments = new Object[] { new Integer(10), "TEST" };
 		final String methodSignature = "call(Ljava/lang/Integer,Ljava/lang/String)V";
 
 		msg.setXID((short) 100);
 		msg.setServiceID(serviceID);
 		msg.setArgs(arguments);
 		msg.setMethodSignature(methodSignature);
 
 		long l = System.nanoTime();
 		clientSession.write(msg);
 
 		final RemoteOSGiMessage msg2 = waitForMessage();
 		System.out.println("TIME " + (System.nanoTime() - l) / 1000);
 
		assertTrue(msg2 instanceof InvokeMethodMessage);
		final InvokeMethodMessage rcv = (InvokeMethodMessage) msg2;
 
 		assertEquals(rcv.getXID(), 100);
 		assertEquals(rcv.getServiceID(), serviceID);
 		assertEquals(rcv.getMethodSignature(), methodSignature);
 		assertEquals(rcv.getArgs(), arguments);
 	}
 
 	public void testLeaseMessage() throws InterruptedException {
 		final LeaseMessage msg = new LeaseMessage();
 		final String[] serviceIDs = new String[] { "service1", "service2" };
 		final String[][] serviceInterfaces = new String[][] {
 				new String[] { "iface1", "iface2", "iface3" },
 				new String[] { "aaa", "bbb", "ccc", "ddd", "eee" } };
 		final Dictionary ht1 = new Hashtable();
 		final Dictionary ht2 = new Hashtable();
 		ht1.put("foo", "FOO");
 		ht2.put("bar", "BAR");
 		final Dictionary[] serviceProperties = new Dictionary[] { ht1, ht2 };
 		final String[] topics = new String[] { "topic1", "topic2", "topic3" };
 
 		msg.setXID((short) 100);
 		msg.setServiceIDs(serviceIDs);
 		msg.setServiceInterfaces(serviceInterfaces);
 		msg.setServiceProperties(serviceProperties);
 		msg.setTopics(topics);
 
 		long l = System.nanoTime();
 		clientSession.write(msg);
 
 		final RemoteOSGiMessage msg2 = waitForMessage();
 		System.out.println("TIME " + (System.nanoTime() - l) / 1000);
 
 		assertTrue(msg2 instanceof LeaseMessage);
 		final LeaseMessage rcv = (LeaseMessage) msg2;
 
 		assertEquals(rcv.getXID(), 100);
 		assertEquals(rcv.getServiceIDs(), serviceIDs);
 		assertEquals(rcv.getServiceInterfaces(), serviceInterfaces);
 		assertEquals(rcv.getServiceProperties(), serviceProperties);
 		assertEquals(rcv.getTopics(), topics);
 		assertEquals(rcv.getServiceProperties()[0].get("foo"), "FOO");
 		assertEquals(rcv.getServiceProperties()[1].get("bar"), "BAR");
 	}
 
 	public void testLeaseUpdateMessage() throws InterruptedException {
 		final LeaseUpdateMessage msg = new LeaseUpdateMessage();
 		final Object[] payload = new Object[] { new String[] { "test" },
 				new String[] { "another", "test" } };
 		msg.setXID((short) 100);
 		msg.setServiceID(serviceID);
 		msg.setType((short) 0);
 		msg.setPayload(payload);
 
 		long l = System.nanoTime();
 		clientSession.write(msg);
 
 		final RemoteOSGiMessage msg2 = waitForMessage();
 		System.out.println("TIME " + (System.nanoTime() - l) / 1000);
 
 		assertTrue(msg2 instanceof LeaseUpdateMessage);
 		final LeaseUpdateMessage rcv = (LeaseUpdateMessage) msg2;
 
 		assertEquals(rcv.getXID(), 100);
 		assertEquals(rcv.getServiceID(), serviceID);
 		assertEquals(rcv.getType(), (short) 0);
 		assertEquals((String[]) rcv.getPayload()[0], (String[]) payload[0]);
 		assertEquals((String[]) rcv.getPayload()[1], (String[]) payload[1]);
 	}
 
 	// helpers
 
 	public static void assertEquals(String[] sa1, String[] sa2) {
 		assertEquals(sa1.length, sa2.length);
 		for (int i = 0; i < sa1.length; i++) {
 			assertEquals(sa1[i], sa2[i]);
 		}
 	}
 
 	public static void assertEquals(byte[] b1, byte[] b2) {
 		assertEquals(b1.length, b2.length);
 		for (int i = 0; i < b1.length; i++) {
 			assertEquals(b1[i], b2[i]);
 		}
 	}
 
 	public static void assertEquals(String[][] sa1, String[][] sa2) {
 		assertEquals(sa1.length, sa2.length);
 		for (int i = 0; i < sa1.length; i++) {
 			assertEquals(sa1[i], sa2[i]);
 		}
 	}
 
 	public static void assertEquals(Object[] o1, Object[] o2) {
 		assertEquals(o1.length, o2.length);
 		for (int i = 0; i < o1.length; i++) {
 			assertEquals(o1[i], o2[i]);
 		}
 	}
 
 	public void exceptionCaught(IoSession session, Throwable cause)
 			throws Exception {
 		cause.printStackTrace();
 		fail(cause.getMessage());
 	}
 
 	public void messageReceived(IoSession session, Object message)
 			throws Exception {
 		System.out.println("RECEIVED " + message);
 		synchronized (queue) {
 			queue.add(message);
 			queue.notifyAll();
 		}
 
 	}
 
 	public void messageSent(IoSession session, Object message) throws Exception {
 		System.out.println("SENT MESSAGE " + message);
 	}
 
 	public void sessionClosed(IoSession session) throws Exception {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void sessionCreated(IoSession session) throws Exception {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void sessionIdle(IoSession session, IdleStatus status)
 			throws Exception {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void sessionOpened(IoSession session) throws Exception {
 		// TODO Auto-generated method stub
 
 	}
 
 	private final RemoteOSGiMessage waitForMessage()
 			throws InterruptedException {
 		synchronized (queue) {
 			while (queue.isEmpty()) {
 				queue.wait();
 			}
 			return (RemoteOSGiMessage) queue.remove(0);
 		}
 	}
 
 }
