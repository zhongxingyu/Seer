 package siver.agents.boat;
 
 import static org.easymock.EasyMock.*;
 import static org.junit.Assert.*;
 
 import java.awt.geom.Point2D;
 
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import repast.simphony.space.continuous.ContinuousSpace;
 import repast.simphony.space.continuous.NdPoint;
 import siver.agents.boat.actions.LetBoatRun;
 import siver.river.lane.Lane;
 import siver.river.lane.LaneEdge;
 import siver.river.lane.LaneNode;
 
 public class CoxAgentTest {
 	
 	private CoxAgent cox;
 	private BoatAgent mockBoat;
 	private Lane mockLane;
 	private ContinuousSpace<Object> mockSpace;
 	
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	@Before
 	public void setUp() throws Exception {
 		mockBoat = createMock(BoatAgent.class);
 		mockLane = createMock(Lane.class);
 		mockSpace = createMock(ContinuousSpace.class);
 		cox = new CoxAgent();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testCoxAgent() {
 		assertNotNull(cox);
 		assertTrue(cox instanceof CoxAgent);
 	}
 
 	@Test
 	public void testLaunch() {
 		launchCox();
 		
 		verify(mockBoat);
 		verify(mockLane);
 		verify(mockSpace);
 	}
 	
 	private void launchCox() {
 		Point2D.Double expLoc = new Point2D.Double(10,30);
 		LaneNode expNode = new LaneNode(expLoc, null);
 		LaneNode nextNode = new LaneNode(30,30, null);
 		expect(mockLane.getStartNode()).andReturn(expNode).once();
 		mockBoat.launch(cox, expLoc);
 		expectLastCall().once();
 		expect(mockLane.getNextEdge(expNode, false)).andReturn(new LaneEdge<LaneNode>(expNode, nextNode)).once();
 		expect(mockBoat.getLocation()).andReturn(new NdPoint(10,30)).once();
 		
 		expect(mockBoat.getSpace()).andReturn(mockSpace).once();
 		expect(mockSpace.getDisplacement(new NdPoint(10,30), new NdPoint(30,30))).andReturn(new double[]{20,0}).once();
 		mockBoat.setAngle(0);
 		expectLastCall().once();
 		
 		replay(mockBoat);
 		replay(mockLane);
 		replay(mockSpace);
 		cox.launch(mockBoat, mockLane);
 	}
 
 	@Test
 	public void testStep() {
 		launchCox();
 		reset(mockBoat);
 		expect(mockBoat.getSpeed()).andReturn(5.0).once();
		mockBoat.move(5);
		expectLastCall().once();
 		replay(mockBoat);
 		cox.step();
 		assertTrue(cox.getAction() instanceof LetBoatRun);
 		verify(mockBoat);
 	}
 	
 	@Test
 	public void testGetAction() {
 		assertNull(cox.getAction());
 	}
 
 }
