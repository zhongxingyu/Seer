 package dk.dtu.imm.distributedsystems.projecs.sensornetwork.common.components.listener.udp;
 
 import java.io.IOException;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.channels.Channel;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.components.listener.AbstractPortListener;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.components.listener.udp.SensorDataUdpPortListener;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.components.sender.AbstractPortSender;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.components.transceiver.AbstractTransceiver;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.packet.Packet;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.packet.PacketType;
 
 public class SensorDataUdpPortListenerTest {
 
 	private static final String ID = "0";
 	
 	private static final int MAX_GUESS_PORT_ATTEMPTS = 5;
 
 	private static final int START_PORT_NUMBER = 21200;
 	
 	private static int portNumber = 9998;
 	
 	private Channel[] channels;
 	
 	private DatagramSocket socket;
 	
 	private DummyTransceiver transceiver;
 	
 	private TestableSensorDataUdpPortListener listener;
 	
 	
 	@BeforeClass
 	public static void guessPort() {
 		
 		DatagramSocket dummySocket = null;
 		for(int i=0; i < MAX_GUESS_PORT_ATTEMPTS; ++i) {
 			try {
 				dummySocket = new DatagramSocket(START_PORT_NUMBER + i);
 			} catch (SocketException e) {
 				continue;
 			}
 			portNumber = START_PORT_NUMBER + i;
 			dummySocket.close();
 			break;
 		}
 		
 	}
 	
 	@Before
 	public void setUp() {
 		
 		channels = new Channel[] {new Channel("21", "localhost", 9900) };
 		
 		try {
 			socket = new DatagramSocket(portNumber);
 		} catch (SocketException e) {
 			Assert.fail("Couldn't create test sockets - please skip these tests");
 		}
 		
 		// setting up a dummy transceiver with no listeners nor sender
 		this.transceiver = new DummyTransceiver(ID, null, null);
 		
 		this.listener = new TestableSensorDataUdpPortListener(ID, transceiver, socket, channels);
 		
 	}
 	
 	@After
 	public void cleanUp() {
 		this.listener.interrupt();
 		this.transceiver.close();
 		
 		this.socket.close();
 	}
 	
 	@Test
 	public void testHandleDataPacket() {
 		
 		List<Packet> packetList = new ArrayList<Packet>();
 		
 		packetList.add(new Packet("21", PacketType.DAT, "15"));
 		
 		packetList.add(new Packet("11", PacketType.DAT, "-20"));
 		
 		try {
 			packetList.add(new Packet("12", PacketType.DAT));
 			Assert.fail();
 		} catch (IllegalStateException e) {
 			// should be caught - this packet is illegal.
 		}
 		
 		for (Packet p : packetList) {
 			try {
 				listener.testHandleIncomingPacket(p);
 			} catch (IOException e) {
 				Assert.fail();
 			}
 			
 			Assert.assertEquals(p, transceiver.getLastPacket());
 		}
 		
 		Assert.assertEquals(packetList.size(), transceiver.getCallCounter());
 		
 	}
 	
 }
 
 class TestableSensorDataUdpPortListener extends SensorDataUdpPortListener {
 
 	static final int TEST_PACKET_PORT = 9999;
 	
 	static final String TEST_PACKET_HOST = "localhost";
 	
 	public TestableSensorDataUdpPortListener(String nodeId,
 			AbstractTransceiver relatedTransceiver, DatagramSocket socket,
 			Channel[] associatedChannels) {
 		super(nodeId, relatedTransceiver, socket, associatedChannels);
 		// TODO Auto-generated constructor stub
 	}
 	
 	public void testHandleIncomingPacket(Packet p) throws IOException {
 		super.handleIncomingPacket(p, InetAddress.getByName(TEST_PACKET_HOST), TEST_PACKET_PORT);
 	}
 	
 }
 
 class DummyTransceiver extends AbstractTransceiver {
 
 	private int callCounter;
 
 	private Packet lastPacket;
 
 	protected DummyTransceiver(String id, AbstractPortListener[] listeners,
 			AbstractPortSender sender) {
 		super(id, listeners, sender);
 
 		this.callCounter = 0;
 		this.lastPacket = null;
 	}
 
 	public int getCallCounter() {
 		return callCounter;
 	}
 
 	public Packet getLastPacket() {
 		return lastPacket;
 	}
 
 	@Override
 	public void handlePacket(Packet packet) {
 		this.lastPacket = packet;
 		++callCounter;
 	}
 
 	@Override
 	public void close() {
 		// do nothing
 
 	}
 
 }
