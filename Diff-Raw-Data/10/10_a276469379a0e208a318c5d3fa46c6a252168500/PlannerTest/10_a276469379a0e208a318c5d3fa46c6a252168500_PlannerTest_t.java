 package test.device;
 
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.logging.Logger;
 
 import junit.framework.JUnit4TestAdapter;
 import static org.junit.Assert.*;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import data.Host;
 import data.Position;
 import device.Device;
 import device.DeviceNode;
 import device.IDevice;
 import device.IPlannerListener;
 import device.Localize;
 import device.Planner;
 import device.Simulation;
 
 public class PlannerTest
 {	
 	static Planner planner;
 	static Localize localizer;
 	static DeviceNode deviceNode;
 	static Simulation simu;
 
 	/** Logging support */
     Logger logger = Logger.getLogger (PlannerTest.class.getName ());
 	static boolean isDone;
     static boolean plannerTO;
 
 	@BeforeClass public static void setUpBeforeClass() throws Exception
 	{
 	    int port = 6665;
         String host = "localhost";
         
         /** Device list */
         CopyOnWriteArrayList<Device> devList = new CopyOnWriteArrayList<Device>();
         devList.add( new Device(IDevice.DEVICE_POSITION2D_CODE,host,port,0) );
         devList.add( new Device(IDevice.DEVICE_RANGER_CODE,host,port,1) );
         devList.add( new Device(IDevice.DEVICE_SIMULATION_CODE,host,port,-1) );
         devList.add( new Device(IDevice.DEVICE_PLANNER_CODE,host,port+1,0) );
         devList.add( new Device(IDevice.DEVICE_LOCALIZE_CODE,host,port+1,0) );
 
         /** Host list */
         CopyOnWriteArrayList<Host> hostList = new CopyOnWriteArrayList<Host>();
         hostList.add(new Host(host,port));
         hostList.add(new Host(host,port+1));
         
         /** Get the device node */
         deviceNode = new DeviceNode(hostList.toArray(new Host[hostList.size()]), devList.toArray(new Device[devList.size()]));
         deviceNode.runThreaded();
                 
         planner = (Planner) deviceNode.getDevice(new Device(IDevice.DEVICE_PLANNER_CODE, null, -1, -1));
         localizer = (Localize) deviceNode.getDevice(new Device(IDevice.DEVICE_LOCALIZE_CODE, null, -1, -1));
         simu = (Simulation) deviceNode.getDevice(new Device(IDevice.DEVICE_SIMULATION_CODE, null, -1, -1));
         
         assertNotNull(planner);
         assertEquals(planner.getClass(),Planner.class);
         assertEquals(planner.isRunning(), true);
         assertEquals(planner.isThreaded(), true);
 	}
 
     @AfterClass public static void tearDownAfterClass() throws Exception
     {
         while (isDone == false) {
             try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
         }
 
         deviceNode.shutdown();      
         assertFalse(planner.isRunning());
     }
 
 	@Test public void testSetPosition()
 	{
 		Position pose = new Position(-6,-5,Math.toRadians(90));
 		
 		simu.setPositionOf("r0", pose);
 		while(simu.getPositionOf("r0").isNearTo(pose, 2, Math.toRadians(10)) != true);
 		
 		// Planner does not currently provide set position service.
 		assertTrue(localizer.setPosition(pose));		
 //		assertTrue(localizer.getPosition().distanceTo(pose) < 2.0);
 	}
 	@Test public void testGetPosition()
 	{
 		try { Thread.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }
 		
 		Position curPose = planner.getPosition();
 		
 		boolean isNear = curPose.distanceTo(new Position(-6,-5,Math.toRadians(90))) < 2.0; 
 		
 		if ( isNear == false )
 		{
 			logger.info("Planner position: "+curPose.toString());
 			logger.info("Localize position: "+localizer.getPosition().toString());
 		}
 		
 		assertTrue(isNear);
 	}
 
 	@Test public void testSetGoal()
 	{
 
 		Position pose = new Position(-6.5,-2,Math.toRadians(75));
 		isDone = false;
 		
 		/** Add isDone listener */
 		planner.addIsDoneListener(new IPlannerListener()
 		{
 			@Override public void callWhenIsDone()
 			{
 				isDone = true;
 				logger.info("Planner is done.");
 				planner.removeIsDoneListener(this);
 			}
 
             @Override public void callWhenAbort()
             {
                 logger.info("Planner abort");
                 planner.setGoal(new Position(-6.5,-2,Math.toRadians(75)));
             }
 
             @Override public void callWhenNotValid()
             {
                 logger.info("No valid path");
             }
 		});
 		
 		assertTrue(planner.setGoal(pose));
 	}
 //	@Test public void testIsValid(){
 //		assertTrue(planner.isValidGoal());
 //	}
 	@Test public void testIsDoneFalse() {
 		assertFalse(planner.isDone());
 	}
 	@Test public void testGetGoal() {
 		assertTrue(planner.getGoal().equals(new Position(-6.5,-2,Math.toRadians(75))));
 	}
 	@Test public void testWPCount() {
 		assertNotNull(planner.getWayPointCount());
 	}
 	@Test public void testWPIndex() {
 		assertNotNull(planner.getWayPointIndex());
 	}
 //	@Test public void testGetCost() {
 //		double cost = planner.getCost();
 //		logger.info("Cost: "+cost);
 //		assertTrue(cost > 0);
 //	}
 //	@Test public void testIsActive() {
 //		assertTrue(planner.isActive());
 //	}
 	@Test public void testIsDone()
 	{
 	    Timer timer = new Timer();
 	    plannerTO = false;
 	    timer.schedule(new TimerTask()
 	    {
             @Override public void run()
             {
                 if (isDone == false)
                 {
                     plannerTO = true;
                 }
             }
 	    }, 15000);
 	    
 		while (isDone == false)
 		{
 		    if (plannerTO == true)
 		    {
 		        plannerTO = false;
 		        fail("Planner timeout");
 		        break;
 		    }
 			try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
 		}
 		timer.cancel();
 	}
 	@Test public void testNotActive() {
 		assertFalse(planner.isActive());
 	}
 	@Test public void testCancel()
 	{
//		Position pose = new Position(0,-6,Math.toRadians(75));
	    Position pose = new Position(-6,6,Math.toRadians(75));
 
 		isDone = false;
 		assertTrue(planner.setGoal(pose));
 
 		try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
 
 		assertTrue(planner.stop());
 		// TODO assert condition
 	}
 //	@Test public void testGetCostPosition()
 //	{
 //		Position pose = new Position(-7,1.5,0);
 //		double cost;
 //		
 //		for (int i=0; i<5; i++) {
 //			cost = planner.getCost(pose);
 //			logger.info("Cost: "+cost+" to pose "+pose.toString());
 //			assertTrue(cost > 0);
 //			pose.setX(pose.getX()+1);
 //			try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
 //		}
 //	}
 //	@Test public void testGetCostInvalidPosition()
 //	{
 //		Position pose = new Position(-90,-90,0);
 //		double cost;
 //
 //		cost = planner.getCost(pose);
 //		logger.info("Cost: "+cost+" to pose "+pose.toString());
 //		
 ////		assertFalse(cost > 0);
 //	}
 	@Test public void testSetFarGoal()
 	{
 		planner.stop();
 		
 		// Add isDone listener
 		planner.addIsDoneListener(new IPlannerListener()
 		{
 			@Override public void callWhenIsDone() {
 			    isDone = true;
                 logger.info("Planner is done.");
 			}
 
 			@Override public void callWhenAbort()
             {
                 logger.info("Planner abort");
                 planner.setGoal(new Position(0,5,0));
             }
 
             @Override public void callWhenNotValid()
             {
                 logger.info("No valid path");
             }
 		});
 
 		isDone = false;
 		planner.setGoal(new Position(0,5,0));
 	}
 
 	/** To use JUnit  test suite */
     public static JUnit4TestAdapter suite()
     { 
        return new JUnit4TestAdapter(PlannerTest.class); 
     }
 }
