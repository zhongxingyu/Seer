 /**
  * 
  */
 package test.device;
 
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import junit.framework.JUnit4TestAdapter;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import data.Host;
 import device.Device;
 import device.DeviceNode;
 import device.Gripper;
 import device.IDevice;
 import device.IGripperListener;
 
 /**
  * @author sebastian
  *
  */
 public class GripperTest
 {
 	static DeviceNode deviceNode;
 	static Gripper gripper;
 	IGripperListener cb;
     boolean isOpen;
     boolean isClosed;
     boolean isClosedLifted;
     boolean isReleasedOpen;
     boolean isLifted;
     boolean isReleased;
 
     @BeforeClass public static void setUpBeforeClass() throws Exception
     {
        int port = 6671;
         String host = "localhost";
         
         /** Device list */
         CopyOnWriteArrayList<Device> devList = new CopyOnWriteArrayList<Device>();
         devList.add( new Device(IDevice.DEVICE_GRIPPER_CODE,host,port,0) );
         devList.add( new Device(IDevice.DEVICE_ACTARRAY_CODE,host,port,0) );
         devList.add( new Device(IDevice.DEVICE_DIO_CODE,host,port,0) );
         
         /** Host list */
         CopyOnWriteArrayList<Host> hostList = new CopyOnWriteArrayList<Host>();
         hostList.add(new Host(host,port));
                 
         /** Get the device node */
         deviceNode = new DeviceNode(hostList.toArray(new Host[hostList.size()]), devList.toArray(new Device[devList.size()]));
         assertNotNull(deviceNode);
         
         deviceNode.runThreaded();
         
         gripper = (Gripper) deviceNode.getDevice(new Device(IDevice.DEVICE_GRIPPER_CODE, null, -1, -1));
         assertNotNull(gripper);
 
         try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
     }
     @AfterClass public static void tearDownAfterClass() throws Exception
     {
         deviceNode.shutdown();
     }
     @Before public void setUp()
     {
         cb = new IGripperListener()
         {
             @Override public void whenOpened() { doneOpen(); }
             @Override public void whenClosed() { doneClose(); }
             @Override public void whenLifted() { doneLift(); }
             @Override public void whenReleased() { doneRelease(); }
             @Override public void whenClosedLifted() { doneCL(); }
             @Override public void whenReleasedOpened() { doneRO(); }
             @Override public void whenError() { }
         };
         gripper.addIsDoneListener(cb);
     }
     @After public void tearDown()
     {
         gripper.removeIsDoneListener(cb);
     }
     void doneOpen()
     {
         isOpen = true; 
         System.out.println(" open.");
     }
     void doneClose()
     {
         isClosed = true; 
         System.out.println(" closed.");
     }
     void doneLift()
     {
         isLifted = true; 
         System.out.println(" lifted.");
     }
     void doneRelease()
     {
         isReleased = true;
         System.out.println(" released.");
     }
     void doneCL()
     {
         isClosedLifted = true; 
         System.out.println(" closed and lifted.");
     }
     void doneRO()
     {
         isReleasedOpen = true;
         System.out.println(" released and opened.");
     }
     /**
 	 * Test method for {@link device.Gripper#stop()}.
 	 */
 	@Test public void testStop()
 	{
 	    System.out.println("Test stop..");
 		gripper.stop();
 	}
 
 	/**
 	 * Test method for {@link device.Gripper#open()}.
 	 */
 	@Test public void testOpen()
 	{
 	    isOpen = false;
 	    System.out.print("Test open..");
 
 	    gripper.open(null);
 	    try { Thread.sleep(5000); } catch (InterruptedException e) { e.printStackTrace(); }
 
 		assertTrue( isOpen == true );
 		System.out.println();
 	}
 
 	/**
 	 * Test method for {@link device.Gripper#close()}.
 	 */
 	@Test public void testClose()
 	{
 	    isClosed = false;
 	    System.out.print("Test close..");
 	    gripper.close(null);
 	    try { Thread.sleep(5000); } catch (InterruptedException e) { e.printStackTrace(); }
 
         assertTrue( isClosed == true );
         System.out.println();
 	}
 
 	/**
 	 * Test method for {@link device.Gripper#lift()}.
 	 */
 	@Test public void testLift()
 	{
 	    isLifted = false;
 	    System.out.print("Test lift..");
 	    gripper.lift(null);
 		try { Thread.sleep(5000); } catch (InterruptedException e) { e.printStackTrace(); }
 		
 		assertEquals(isLifted, true);
         System.out.println();
 	}
 
 	/**
 	 * Test method for {@link device.Gripper#release()}.
 	 */
 	@Test public void testRelease()
 	{
 	    isReleased = false;
 	    System.out.print("Test release..");
 	    gripper.release(null);
 		try { Thread.sleep(6000); } catch (InterruptedException e) { e.printStackTrace(); }
 		
 		assertEquals(isReleased, true);
         System.out.println();
 	}
 	/**
 	 * Test method for {@link device.Gripper#lift()}.
 	 */
 	@Test public void testLift2()
 	{
 	    isLifted = false;
 	    System.out.print("Test lift..");
 	    gripper.lift(null);
 		try { Thread.sleep(5000); } catch (InterruptedException e) { e.printStackTrace(); }
 		
 		assertEquals(isLifted, true);
 	    System.out.println();
 	}
 	@Test public void testReleaseOpen()
 	{
 	    isReleasedOpen = false;
         System.out.print("Test release and open..");
 	    gripper.releaseOpen(null);
 	    try { Thread.sleep(10000); } catch (InterruptedException e) { e.printStackTrace(); }
         
         assertEquals(isReleasedOpen, true);
         System.out.println();
 	}
 	@Test public void testcloseLift()
 	{
 	    isClosedLifted = false;
         System.out.print("Test close and lift..");
 	    gripper.closeLift(null);
 	    try { Thread.sleep(10000); } catch (InterruptedException e) { e.printStackTrace(); }
         
         assertEquals(isClosedLifted, true);
         System.out.println();
 	}
 	/**
 	 * Test method for {@link device.Gripper#getState()}.
 	 */
 	public Gripper.stateType getState()
 	{
 		Gripper.stateType state = gripper.getState();
 		
 		System.out.println("Gripper state: "+state);
 		
 		return state;
 	}
 //	@Test public void testLiftWithObject()
 //	{
 //		gripper.open();
 //		gripper.liftWithObject();
 //	}
 
 	/** To use JUnit  test suite */
     public static JUnit4TestAdapter suite()
     { 
        return new JUnit4TestAdapter(GripperTest.class); 
     }
 }
