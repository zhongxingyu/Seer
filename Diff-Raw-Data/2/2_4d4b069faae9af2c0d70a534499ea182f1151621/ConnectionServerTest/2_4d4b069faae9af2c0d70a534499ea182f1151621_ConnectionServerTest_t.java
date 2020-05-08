 package tests;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.Calendar;
 
 import org.junit.*;
 import org.protocols.Netstring;
 
 import common.Coord;
 import common.Course;
 import common.UpdateData;
 import common.Vessel;
 import common.Vessel.VesselType;
 
 import vms.ConnectionServer;
 
 public class ConnectionServerTest {
 	
 	static ConnectionServer cs = null;
 	private class CSThread extends Thread {
 		@Override
 		public void run() {
 			
 			try {
 				InetSocketAddress addr = new InetSocketAddress("localhost", 11233);
 				cs.bind(addr);
 				cs.start();
 			} catch (IOException e) {
 				fail(e.getMessage());
 			}
 		}
 	}
 	
 	Calendar lastRefresh = null;
 	Vessel lastVessel = null;
 	
 	public class CSObserver implements ConnectionServer.Observer {
 
 		@Override
 		public void update(UpdateData data) {
 			lastVessel = new Vessel(data.Id, data.Type);
 			lastVessel.update(data.Coordinates, data.Course, data.Timestamp);
 		}
 
 		@Override
 		public void refresh(Calendar timestamp) {
 			lastRefresh = (Calendar)timestamp.clone();
 		}
 		
 	}
 	
 	@Before
 	public void setUp() {
 		cs = new ConnectionServer();
 		cs.setMinimumRefresh(1000);
 		cs.registerObserver(new CSObserver());
 	}
 	
 	@After
 	public void tearDown() throws IOException {
 		cs.close();
 		cs = null;
 	}
 
 	@Test
 	public void testManualRefresh() throws IOException {
 		assertNull(lastRefresh);
 		Calendar timestamp = Calendar.getInstance();
 		cs.refreshObservers(timestamp);
 		assertEquals(timestamp, lastRefresh);
 		timestamp = Calendar.getInstance();
 		cs.refreshObservers(timestamp);
 		assertEquals(timestamp, lastRefresh);
 	}
 	
 	@Test
 	public void testManualUpdate() throws IOException {
 		assertNull(lastVessel);
 		Calendar timestamp = Calendar.getInstance();
 		cs.updateObservers(new UpdateData("myid", VesselType.CARGO_BOAT, new Coord(10,10), new Course(20,20), timestamp));
 		assertEquals("myid", lastVessel.getId());
 		assertEquals(VesselType.CARGO_BOAT, lastVessel.getType());
 		
 		try{
 			assertEquals(new Coord(10,10), lastVessel.getCoord(timestamp));
 			assertEquals(new Course(20,20), lastVessel.getCourse(timestamp));
 		}catch(Exception e){
 			fail("Caught exception: " + e.getMessage());
 		}
 		
 		assertEquals(timestamp, lastVessel.getLastTimestamp());
 	}
 	
 	@Test
 	public void testAutomaticRefresh() throws IOException, InterruptedException {
 		CSThread csThread = new CSThread();
 		csThread.start();
 		Calendar timestamp;
 		try {
 			Thread.sleep(2000);
 			timestamp = lastRefresh;
 			assertNotNull(timestamp);
 			Thread.sleep(2000);
 			assertTrue(timestamp.before(lastRefresh));
 		}
 		finally {
 			cs.stop();
 			csThread.join();
 		}
 		try {
 			cs.stop();
 			Thread.sleep(2000); //Wait for CS to stop
 			timestamp = lastRefresh;
 			Thread.sleep(2000); //Check if we received anything else
 			assertEquals(timestamp, lastRefresh);
 		}
 		finally {
 			csThread.join();
 		}
 	}
 	
 	@Test
 	public void testSendData() throws UnknownHostException, IOException, InterruptedException {
 		CSThread csThread = new CSThread();
 		csThread.start();
 		//Wait for CS to bind socket...
 		Thread.sleep(1000);
 		Socket socket = new Socket();
 		try {
 			socket.connect(new InetSocketAddress("localhost", 11233));
 			OutputStream stream = socket.getOutputStream();
 			
 			//Try sending garbage
 			String msg = "gfenrwjgfe  fbd fbb fwwq";
 			stream.write(msg.getBytes());
 			Thread.sleep(1000); //Allow for overhead of network
 			assertNull(lastVessel); //Should NOT have called update()
 			
 			//Try sending netstring-encoded garbage
 			msg = "gfenrwjgfe  fbd fbb fwwq";
 			msg = msg.length() + ":" + msg; //Netstring encoding
 			stream.write(msg.getBytes());
 			Thread.sleep(1000); //Allow for overhead of network
 			assertNull(lastVessel); //Should NOT have called update()
 			
 			//Try sending bad vessel values
 			msg = "{\"id\":[1,2,3],\"type\":\"WRONGENUMVALUE\",\"coords\":0,\"course\":{}}";
 			msg = msg.length() + ":" + msg; //Netstring encoding
 			stream.write(msg.getBytes());
 			Thread.sleep(1000); //Allow for overhead of network
 			assertNull(lastVessel); //Should NOT have called update()
 			
 			//Try sending correct data
 			Calendar curTime = Calendar.getInstance();
			UpdateData ud = new UpdateData("myid", VesselType.CARGO_BOAT, new Coord(10, -10), new Course(20, -20), curTime);
 			msg = ud.toJSON();
 			Netstring.write(stream, msg.getBytes()); //Write with netstring encoding
 			Thread.sleep(1000); //Allow for overhead of network
 			assertNotNull(lastVessel);
 			Calendar timestamp = lastVessel.getLastTimestamp();
 			assertEquals(curTime, timestamp);
 			assertEquals("myid", lastVessel.getId());
 			assertEquals(VesselType.CARGO_BOAT, lastVessel.getType());
 			
 			try{
 				assertEquals(new Coord(10,-10), lastVessel.getCoord(timestamp));
 				assertEquals(new Course(20,-20), lastVessel.getCourse(timestamp));
 			}catch(Exception e){
 				fail("Caught exception: " + e.getMessage());
 			}
 		} finally {
 
 			cs.stop();
 			socket.close();
 			csThread.join(); //Wait for ConnectionServer to cleanly shut down
 		}
 	}
 
 }
