 package uk.ac.imperial.dws04.Presage2Experiments;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.Set;
 import java.util.UUID;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import uk.ac.imperial.dws04.Presage2Experiments.LaneMoveHandler;
 import uk.ac.imperial.dws04.Presage2Experiments.ParticipantRoadLocationService;
 import uk.ac.imperial.dws04.Presage2Experiments.ParticipantSpeedService;
 import uk.ac.imperial.dws04.Presage2Experiments.RoadEnvironmentService;
 import uk.ac.imperial.dws04.Presage2Experiments.RoadLocation;
 import uk.ac.imperial.dws04.Presage2Experiments.ParticipantRoadLocationServiceTest.TestAgent;
 import uk.ac.imperial.presage2.core.Action;
 import uk.ac.imperial.presage2.core.IntegerTime;
 import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
 import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
 import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
 import uk.ac.imperial.presage2.core.event.EventBusModule;
 import uk.ac.imperial.presage2.core.messaging.Input;
 import uk.ac.imperial.presage2.core.simulator.SimTime;
 import uk.ac.imperial.presage2.core.util.random.Random;
 import uk.ac.imperial.presage2.util.environment.AbstractEnvironment;
 import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
 import uk.ac.imperial.presage2.util.location.CellMove;
 import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
 import uk.ac.imperial.presage2.util.location.area.Area;
 import uk.ac.imperial.presage2.util.location.area.Area.Edge;
 import uk.ac.imperial.presage2.util.location.area.WrapEdgeHandler;
 import uk.ac.imperial.presage2.util.participant.AbstractParticipant;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.name.Names;
 
 public class LaneMoveHandlerTest {
 
 	Injector injector;
 	AbstractEnvironment env;
 	LaneMoveHandler handler;
 	RoadEnvironmentService roadEnvironmentService;
 	
 	InputStream testInput;
 	InputStream old = System.in;
 	
 	IntegerTime time = new IntegerTime(0);
 	SimTime sTime = new SimTime(time);
 	
 	private final int lanes = 3;
 	private int length = 10;
 	private int junctionCount = 0;
 	
 	public void incrementTime(){
 		time.increment();
 		env.incrementTime();
 	}
 
 	public void setUp() throws Exception {
 		injector = Guice.createInjector(
 				new AbstractEnvironmentModule()
 					.addActionHandler(LaneMoveHandler.class)
 					.addParticipantEnvironmentService(ParticipantLocationService.class)
 					.addParticipantEnvironmentService(ParticipantRoadLocationService.class)
 					.addParticipantEnvironmentService(ParticipantSpeedService.class)
 					.addGlobalEnvironmentService(RoadEnvironmentService.class),
 				Area.Bind.area2D(lanes, length).addEdgeHander(Edge.Y_MAX,
 						WrapEdgeHandler.class), new EventBusModule(),
 				new AbstractModule() {
 					// add in params that are required
 					@Override
 					protected void configure() {
 						bind(Integer.TYPE).annotatedWith(Names.named("params.maxSpeed")).toInstance(10);
 						bind(Integer.TYPE).annotatedWith(Names.named("params.maxAccel")).toInstance(1);
 						bind(Integer.TYPE).annotatedWith(Names.named("params.maxDecel")).toInstance(1);
 						bind(Integer.TYPE).annotatedWith(Names.named("params.junctionCount")).toInstance(junctionCount);
 						bind(Integer.TYPE).annotatedWith(Names.named("params.lanes")).toInstance(lanes);
 						bind(Integer.TYPE).annotatedWith(Names.named("params.length")).toInstance(length);
 					}
 				});
 
 		env = injector.getInstance(AbstractEnvironment.class);
 		handler = injector.getInstance(LaneMoveHandler.class);
 		roadEnvironmentService = injector.getInstance(RoadEnvironmentService.class);
 		
 		// Replace stdin for the exceptions.
 		String data = "n";
 		testInput = new ByteArrayInputStream( data.getBytes("UTF-8") );
 		System.setIn(testInput);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		// put stdin back
 		System.setIn(old);
 	}
 	
 	public void assertCollisions(int n) {
 		try {
 			assertEquals(n, handler.checkForCollisions(null));
 		} catch (Exception e) {
 			System.out.println("Caught an exception:" + e);
 		}
 	}
 	
 	class TestAgent extends AbstractParticipant {
 
 		RoadLocation startLoc;
 		int startSpeed;
 		ParticipantLocationService locationService;
 		ParticipantSpeedService speedService;
 
 		public TestAgent(UUID id, String name, RoadLocation startLoc, int startSpeed) {
 			super(id, name);
 			this.startLoc = startLoc;
 			this.startSpeed = startSpeed;
 		}
 
 		@Override
 		protected Set<ParticipantSharedState> getSharedState() {
 			Set<ParticipantSharedState> ss = super.getSharedState();
 			ss.add(ParticipantLocationService.createSharedState(getID(),startLoc));
 			ss.add(ParticipantSpeedService.createSharedState(getID(), startSpeed));
 			return ss;
 		}
 
 		@Override
 		public void initialise() {
 			super.initialise();
 			try {
 				this.locationService = getEnvironmentService(ParticipantLocationService.class);
 			} catch (UnavailableServiceException e) {
 				logger.warn(e);
 			}
 			try {
 				this.speedService = getEnvironmentService(ParticipantSpeedService.class);
 			} catch (UnavailableServiceException e) {
 				logger.warn(e);
 			}
 		}
 
 		@Override
 		protected void processInput(Input in) {
 		}
 
 		public void performAction(Action a) throws ActionHandlingException {
 			environment.act(a, getID(), authkey);
 		}
 
 		public void assertLocation(int lane, int offset) {
 			RoadLocation current = (RoadLocation) this.locationService
 					.getAgentLocation(getID());
 			assertEquals(lane, current.getLane());
 			assertEquals(offset, current.getOffset());
 		}
 		
 		public void assertSpeed(int speed) {
 			int current = this.speedService.getAgentSpeed(getID());
 			assertEquals(speed, current);
 		}
 	}
 
 	private TestAgent createTestAgent(String name, RoadLocation startLoc, int startSpeed) {
 		TestAgent a = new TestAgent(Random.randomUUID(), name, startLoc, startSpeed);
 		injector.injectMembers(a);
 		a.initialise();
 		return a;
 	}
 
 	@Test
 	public void testValidMoves() throws Exception {
 		setUp();
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 2);
 
 		incrementTime();
 
 		a.assertLocation(1, 0);
 		a.assertSpeed(2);
 		assertCollisions(0);
 
 		// perform a series of valid moves.		
 		a.performAction(new CellMove(0, 2));
 		incrementTime();
 		a.assertLocation(1, 2);
 		a.assertSpeed(2);
 		assertCollisions(0);
 
 		a.performAction(new CellMove(0, 3));
 		incrementTime();
 		a.assertLocation(1, 5);
 		a.assertSpeed(3);
 		assertCollisions(0);
 
 		a.performAction(new CellMove(1, 3));
 		incrementTime();
 		a.assertLocation(2, 8);
 		a.assertSpeed(3);
 		assertCollisions(0);
 
 		a.performAction(new CellMove(0, 3));
 		incrementTime();
 		a.assertLocation(2, 1);
 		a.assertSpeed(3);
 		assertCollisions(0);
 
 		a.performAction(new CellMove(-1, 2));
 		incrementTime();
 		a.assertLocation(1, 3);
 		a.assertSpeed(2);
 		assertCollisions(0);
 		
 		//Testing max accel
 		int tempSpeed = 2+roadEnvironmentService.getMaxAccel();
 		int tempLocation = 3+tempSpeed;
 		a.performAction(new CellMove(-1, tempSpeed));
 		incrementTime();
 		a.assertLocation(0, tempLocation);
 		a.assertSpeed(tempSpeed);
 		assertCollisions(0);
 		
 		//Testing max decel
 		tempSpeed = tempSpeed - roadEnvironmentService.getMaxDecel();
 		a.performAction(new CellMove(0, tempSpeed));
 		incrementTime();
 		a.assertLocation(0, (tempLocation+tempSpeed));
 		a.assertSpeed(tempSpeed);
 		assertCollisions(0);
 		
 		// This won't pass the speed check
 		/*
 		 * //Testing moving very (legally) fast - this should probably loop...
 		 * a.performAction(new CellMove(0, (roadEnvironmentService.getMaxSpeed()-1) ));
 		 * incrementTime();
 		 * a.assertLocation(1, ((3+(roadEnvironmentService.getMaxSpeed()-1))%roadEnvironmentService.getLength()) );
 		 * assertCollisions(0);
 		 */
 		
 		
 	}
 
 	@Test
 	public void testInvalidLocationMove() throws Exception {
 		setUp();
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 0);
 		TestAgent b = createTestAgent("b", new RoadLocation(1, 5), 0);
 
 		incrementTime();
 
 		a.assertLocation(1, 0);
 		b.assertLocation(1, 5);
 		assertCollisions(0);
 
 		a.performAction(new CellMove(1, 1));
 		b.performAction(new CellMove(-1,1));
 		incrementTime();
 		a.assertLocation(2, 1);
 		b.assertLocation(0, 6);
 		assertCollisions(0);
 
 		try {
 			// move to non existent lane
 			a.performAction(new CellMove(1, 2));
 			fail();
 		} catch (ActionHandlingException e) {
 		}
 		try {
 			// move to non existent lane
 			b.performAction(new CellMove(-1, 2));
 			fail();
 		} catch (ActionHandlingException e) {
 		}
 		try {
 			// move multiple lanes at once.
 			a.performAction(new CellMove(-2, 2));
 			fail();
 		} catch (ActionHandlingException e) {
 		}
 	}
 	
 	@Test
 	public void testInvalidSpeedMove() throws Exception {
 		setUp();
 		int maxSpeed = roadEnvironmentService.getMaxSpeed();
 		TestAgent a = createTestAgent("a", new RoadLocation(2,0), 1);
 		TestAgent b = createTestAgent("b", new RoadLocation(1,0), maxSpeed);
 		
 		/*
 		 * FIXME the agent has a speed here but hasn't made a move action,
 		 * so its location hasn't been updated but neither has its speed.
 		 */
 		incrementTime();
 
 		a.assertLocation(2, 0);
 		b.assertLocation(1, 0);
 		assertCollisions(0);
 
 		a.performAction(new CellMove(0, 1));
 		b.performAction(new CellMove(0,maxSpeed));
 		incrementTime();
 		a.assertLocation(2, 1);
 		b.assertLocation(1, (length%maxSpeed));
 		a.assertSpeed(1);
 		b.assertSpeed(maxSpeed);
 		assertCollisions(0);
 		
 		try {
 			// accelerate too fast.
 			a.performAction(new CellMove(0, (1+roadEnvironmentService.getMaxAccel()+1) ));
 			fail();
 		} catch (ActionHandlingException e) {
 		}
 		try {
 			// move too fast.
 			b.performAction(new CellMove(0, (maxSpeed+1) ));
 			fail();
 		} catch (ActionHandlingException e) {
 		}
 		try {
 			// decelerate too fast.
 			b.performAction(new CellMove(0, (maxSpeed-(roadEnvironmentService.getMaxDecel()+1)) ));
 			fail();
 		} catch (ActionHandlingException e) {
 		}
 	}
 	
 	@Test
 	public void testReverseMove() throws Exception {
 		setUp();
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 0);
 
 		incrementTime();
 
 		a.assertLocation(1, 0);
 		assertCollisions(0);
 
 		a.performAction(new CellMove(1, 1));
 		incrementTime();
 		a.assertLocation(2, 1);
 		assertCollisions(0);
 
 		try {
 			// move backwards.
 			a.performAction(new CellMove(0, -1));
 			fail();
 		} catch (ActionHandlingException e) {
 		}
 	}
 
 	@Test
 	public void testMoveToSameCellOnSameLaneCollisions() throws Exception {
 		setUp();
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 3);
 		TestAgent b = createTestAgent("b", new RoadLocation(1, 3), 0);
 
 		incrementTime();
 
 		a.assertLocation(1, 0);
 		b.assertLocation(1, 3);
 
 		// b stationary, a move to b's cell
 		a.performAction(new CellMove(0, 3));
 		b.performAction(new CellMove(0, 0));
 		incrementTime();
 		// two collisions counted as both agents detect double occupied cell.
 		//assertEquals(2, handler.checkForCollisions(null));
 		assertCollisions(2);
 		// note their locations have still be updated.
 		a.assertLocation(1, 3);
 		b.assertLocation(1, 3);
 
 		// separate them again
 		a.performAction(new CellMove(0, 2));
 		b.performAction(new CellMove(0, 1));
 		incrementTime();
 		a.assertLocation(1, 5);
 		b.assertLocation(1, 4);
 
 		// both moving, land on same cell
 		a.performAction(new CellMove(0, 1));
 		b.performAction(new CellMove(0, 2));
 		incrementTime();
 		//assertEquals(2, handler.checkForCollisions(null));
 		assertCollisions(2);
 		a.assertLocation(1, 6);
 		b.assertLocation(1, 6);
 	}
 
 	@Test
 	public void testSameLaneCollision() throws Exception {
 		setUp();
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 5);
 		TestAgent b = createTestAgent("b", new RoadLocation(1, 1), 3);
 
 		incrementTime();
 
 		a.assertLocation(1, 0);
 		b.assertLocation(1, 1);
 
 		// a leapfrog b
 		/*   | | |      |a| |
 		 *   | | |      |b| |
 		 *   | | |      | | |
 		 *   | | |      | | |
 		 *   |b| | -->  | | |
 		 *   |a| |      | | |
 		 *  Collision: yes
 		 */
 		a.performAction(new CellMove(0, 5));
 		b.performAction(new CellMove(0, 3));
 		incrementTime();
		assertEquals(1, handler.checkForCollisions(null));
 		a.assertLocation(1, 5);
 		b.assertLocation(1, 4);
 
 		// valid moves
 		a.performAction(new CellMove(0, 4));
 		b.performAction(new CellMove(0, 4));
 		incrementTime();
 		assertCollisions(0);
 		a.assertLocation(1, 9);
 		b.assertLocation(1, 8);
 
 		// b leapfrog a over wrap-around point
 		a.performAction(new CellMove(0, 3));
 		b.performAction(new CellMove(0, 5));
 		incrementTime();
 		//assertEquals(1, handler.checkForCollisions(null));
 		assertCollisions(1);
 		a.assertLocation(1, 2);
 		b.assertLocation(1, 3);
 
 	}
 
 	@Test
 	public void testMultiLaneCollision1() throws Exception {
 		setUp();
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 3);
 		TestAgent b = createTestAgent("b", new RoadLocation(2, 1), 1);
 
 		incrementTime();
 		a.assertLocation(1, 0);
 		b.assertLocation(2, 1);
 
 		/*   | | |      | |a|
 		 *   | | |      | |b|
 		 *   | |b| -->  | | |
 		 *   |a| |      | | |
 		 *   Collision: yes
 		 */
 
 		a.performAction(new CellMove(1, 3));
 		b.performAction(new CellMove(0, 1));
 		incrementTime();
 		a.assertLocation(2, 3);
 		b.assertLocation(2, 2);
 		//assertEquals(1, handler.checkForCollisions(null));
 		assertCollisions(1);
 	}
 	
 	@Test
 	public void testMultiLaneCollision2() throws Exception {
 		setUp();
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 3);
 		TestAgent b = createTestAgent("b", new RoadLocation(2, 1), 1);
 
 		incrementTime();
 		a.assertLocation(1, 0);
 		b.assertLocation(2, 1);
 
 		/*   | | | |      | |a| |
 		 *   | | | |      |b| | |
 		 *   | |b| | -->  | | | |
 		 *   |a| | |      | | | |
 		 *   Collision: yes
 		 */
 
 		a.performAction(new CellMove(1, 3));
 		b.performAction(new CellMove(-1, 1));
 		incrementTime();
 		//assertEquals(1, handler.checkForCollisions(null));
 		assertCollisions(1);
 		b.assertLocation(1, 2);
 		a.assertLocation(2, 3);
 	}
 
 	@Test
 	public void testMultiLaneCollision3() throws Exception {
 		setUp();
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 0);
 		TestAgent b = createTestAgent("b", new RoadLocation(2, 1), 0);
 
 		incrementTime();
 		a.assertLocation(1, 0);
 		b.assertLocation(2, 1);
 
 		/*   | | | |      | | | |
 		 *   | | | |      |b| | |
 		 *   | |b| | -->  | |a| |
 		 *   |a| | |      | | | |
 		 *   Collision: no
 		 */
 
 		a.performAction(new CellMove(1, 1));
 		b.performAction(new CellMove(-1, 1));
 		incrementTime();
 		a.assertLocation(2, 1);
 		b.assertLocation(1, 2);
 		assertCollisions(0);
 	}
 
 	@Test
 	public void testMultiLaneCollision4() throws Exception {
 		setUp();
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 3);
 		TestAgent b = createTestAgent("b", new RoadLocation(2, 1), 1);
 
 		incrementTime();
 		a.assertLocation(1, 0);
 		b.assertLocation(2, 1);
 
 		/*   | | | |      |a| | |
 		 *   | | | |      |b| | |
 		 *   | |b| | -->  | | | |
 		 *   |a| | |      | | | |
 		 *   Collision: yes
 		 */
 
 		a.performAction(new CellMove(0, 3));
 		b.performAction(new CellMove(-1, 1));
 		incrementTime();
 		a.assertLocation(1, 3);
 		b.assertLocation(1, 2);
 		//assertEquals(1, handler.checkForCollisions(null));
 		assertCollisions(1);
 	}
 
 	@Test
 	public void testMultiLaneCollision5() throws Exception {
 		setUp();
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 0);
 		TestAgent b = createTestAgent("b", new RoadLocation(3, 1), 0);
 
 		incrementTime();
 		a.assertLocation(1, 0);
 		b.assertLocation(3, 1);
 
 		/*   | | | |      | | | |
 		 *   | | | |      | |b| |
 		 *   | | |b| -->  | |a| |
 		 *   |a| | |      | | | |
 		 *   Collision: no
 		 */
 
 		a.performAction(new CellMove(1, 1));
 		b.performAction(new CellMove(-1, 1));
 		incrementTime();
 		a.assertLocation(2, 1);
 		b.assertLocation(2, 2);
 		assertCollisions(0);
 	}
 
 	@Test
 	public void testMultiLaneCollision6() throws Exception {
 		setUp();
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 0);
 		TestAgent b = createTestAgent("b", new RoadLocation(2, 0), 0);
 
 		incrementTime();
 		a.assertLocation(1, 0);
 		b.assertLocation(2, 0);
 
 		/*   | | | |      | | | |
 		 *   | | | |      | | | |
 		 *   | | | | -->  |b|a| |
 		 *   |a|b| |      | | | |
 		 *   Collision: yes
 		 */
 
 		a.performAction(new CellMove(1, 1));
 		b.performAction(new CellMove(-1, 1));
 		incrementTime();
 		a.assertLocation(2, 1);
 		b.assertLocation(1, 1);
 		//assertEquals(2, handler.checkForCollisions(null));
 		assertCollisions(2);
 	}
 
 	@Test
 	public void testMultiLaneCollision7() throws Exception {
 		setUp();
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 2);
 		TestAgent b = createTestAgent("b", new RoadLocation(2, 0), 1);
 		TestAgent c = createTestAgent("a", new RoadLocation(3, 1), 1);
 		TestAgent d = createTestAgent("b", new RoadLocation(2, 2), 1);
 
 		incrementTime();
 
 		/*   | | | |      | |d| |
 		 *   | |d| |      |a|c| |
 		 *   | | |c| -->  | |b| |
 		 *   |a|b| |      | | | |
 		 *   Collision: no
 		 */
 
 		a.performAction(new CellMove(0, 2));
 		b.performAction(new CellMove(0, 1));
 		c.performAction(new CellMove(-1, 1));
 		d.performAction(new CellMove(0, 1));
 		incrementTime();
 		a.assertLocation(1, 2);
 		b.assertLocation(2, 1);
 		c.assertLocation(2, 2);
 		d.assertLocation(2, 3);
 		assertCollisions(0);
 	}
 
 	@Test
 	public void testMultiLaneCollision8() throws Exception {
 		setUp();
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 0);
 		TestAgent b = createTestAgent("b", new RoadLocation(1, 1), 0);
 
 		incrementTime();
 		a.assertLocation(1, 0);
 		b.assertLocation(1, 1);
 
 		/*   | | | |      | | | |
 		 *   | | | |      | | | |
 		 *   |b| | | -->  |b|a| |
 		 *   |a| | |      | | | |
 		 *   Collision: no
 		 */
 
 		a.performAction(new CellMove(1, 1));
 		b.performAction(new CellMove(0, 0));
 		incrementTime();
 		a.assertLocation(2, 1);
 		b.assertLocation(1, 1);
 		assertCollisions(0);
 	}
 	
 	@Test
 	public void testDomino1() throws Exception {
 		setUp();
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 1);
 		TestAgent b = createTestAgent("b", new RoadLocation(1, 1), 1);
 
 		incrementTime();
 		a.assertLocation(1, 0);
 		b.assertLocation(1, 1);
 		
 		/*   | | | |      | | | |
 		 *   | | | |      | |b| |
 		 *   | |b| | -->  | |a| |
 		 *   | |a| |      | | | |
 		 *   Collision: no
 		 */
 		
 		a.performAction(new CellMove(0, 1));
 		b.performAction(new CellMove(0, 1));
 		incrementTime();
 		a.assertLocation(1, 1);
 		b.assertLocation(1, 2);
 		assertCollisions(0);
 	}
 	
 	@Test
 	public void testDomino2() throws Exception {
 		setUp();
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 4);
 		TestAgent b = createTestAgent("b", new RoadLocation(1, 4), 5);
 
 		incrementTime();
 		a.assertLocation(1, 0);
 		b.assertLocation(1, 4);
 		
 		/*   | |b| |      | |a| |
 		 *   | |.| |      | | | |
 		 *   | |.| | -->  | | | |
 		 *   | |a| |      | | | |
 		 *   Collision: no
 		 */
 		
 		a.performAction(new CellMove(0, 4));
 		b.performAction(new CellMove(0, 5));
 		incrementTime();
 		a.assertLocation(1, 4);
 		b.assertLocation(1, 9);
 		assertCollisions(0);
 	}
 	
 	@Test
 	public void testDomino3() throws Exception {
 		setUp();
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 2);
 		TestAgent b = createTestAgent("b", new RoadLocation(1, 9), 1);
 
 		incrementTime();
 		a.assertLocation(1, 0);
 		b.assertLocation(1, 9);
 		
 		/*   | |b| |      | | | |
 		 *   | | | |      | |a| |
 		 *   | | | | -->  | | | |
 		 *   | |a| |      | |b| |
 		 *   Collision: no
 		 */
 		
 		a.performAction(new CellMove(0, 2));
 		b.performAction(new CellMove(0, 1));
 		incrementTime();
 		a.assertLocation(1, 2);
 		b.assertLocation(1, 0);
 		assertCollisions(0);
 	}
 
 	@Test
 	public void testDomino4() throws Exception {
 		setUp();
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 8), 2);
 		TestAgent b = createTestAgent("b", new RoadLocation(1, 9), 1);
 
 		incrementTime();
 		a.assertLocation(1, 8);
 		b.assertLocation(1, 9);
 
 		/*   | |b| |      | |a| |
 		 *   | |a| |      | | | |
 		 *   | | | | -->  | | | |
 		 *   | | | |      | |b| |
 		 *   Collision: no
 		 */
 
 		a.performAction(new CellMove(0, 1));
 		b.performAction(new CellMove(0, 1));
 		incrementTime();
 		a.assertLocation(1, 9);
 		b.assertLocation(1, 0);
 		assertCollisions(0);
 	}
 	
 	@Test
 	public void testJunctions() throws Exception {
 		length = 10;
 		junctionCount = 3;
 		setUp();
 		/*
 		 * |0|1|2|3|4|5|6|7|8|9|
 		 * |j| | |j| | |j| | | |
 		 * |a|b| | |c|d|g| | |e|
 		 * | | | | | | | | | |f|
 		 */
 		assertTrue(this.roadEnvironmentService.getJunctionLocations().contains(0));
 		assertTrue(this.roadEnvironmentService.getJunctionLocations().contains(3));
 		assertTrue(this.roadEnvironmentService.getJunctionLocations().contains(6));
 		assertFalse(this.roadEnvironmentService.getJunctionLocations().contains(8));
 
 		TestAgent a = createTestAgent("a", new RoadLocation(0, 0), 1);
 		TestAgent b = createTestAgent("b", new RoadLocation(0, 1), 1);
 		TestAgent c = createTestAgent("c", new RoadLocation(0, 4), 1);
 		TestAgent d = createTestAgent("d", new RoadLocation(0, 5), 1);
 		TestAgent e = createTestAgent("e", new RoadLocation(0, 9), 1);
 		TestAgent f = createTestAgent("f", new RoadLocation(1, 9), 1);
 		TestAgent g = createTestAgent("g", new RoadLocation(0, 6), 0);
 		
 		incrementTime();
 		// check it's all correct
 		a.assertLocation(0, 0);
 		b.assertLocation(0, 1);
 		c.assertLocation(0, 4);
 		d.assertLocation(0, 5);
 		e.assertLocation(0, 9);
 		f.assertLocation(1, 9);
 		g.assertLocation(0, 6);
 		
 		//try some valid moves
 		b.performAction(new CellMove(-1,2));
 		d.performAction(new CellMove(-1,1));
 		e.performAction(new CellMove(-1,1));
 		
 		// invalid moves
 		//a tries to turn off at 0 (0 speed)
 		try {
 			a.performAction(new CellMove(-1,0));
 			fail();
 		} catch (ActionHandlingException ex) {
 		}
 		try {
 			//c tries to turn off at 3 (backwards)
 			c.performAction(new CellMove(-1,-1));
 			fail();
 		} catch (ActionHandlingException ex) {
 		}
 		try {
 			//f tries to turn off at all (too many lanes)
 			f.performAction(new CellMove(-2,1));
 			fail();
 		} catch (ActionHandlingException ex) {
 		}
 		try {
 			//g tries to turn off at 0 (too far)
 			g.performAction(new CellMove(-1,-4));
 			fail();
 		} catch (ActionHandlingException ex) {
 		}	
 	}
 	
 	/**
 	 * There is (~) no possibility of crashing when turning off, due to sliproads being long
 	 * (and the crash-detection code happening just after the agent-removal code :P)
 	 * @throws Exception
 	 */
 	@Test
 	public void testJunctionNoCrashes() throws Exception {
 		length = 10;
 		junctionCount = 3;
 		setUp();
 		/*
 		 * |0|1|2|3|4|5|6|7|8|9|
 		 * |j| | |j| | |j| | | |
 		 * |a|b| | |c|d| | | |e|
 		 * | | | | | | | | | |f|
 		 */
 		assertTrue(this.roadEnvironmentService.getJunctionLocations().contains(0));
 		assertTrue(this.roadEnvironmentService.getJunctionLocations().contains(3));
 		assertTrue(this.roadEnvironmentService.getJunctionLocations().contains(6));
 		assertFalse(this.roadEnvironmentService.getJunctionLocations().contains(8));
 
 		TestAgent a = createTestAgent("a", new RoadLocation(0, 0), 2);
 		TestAgent b = createTestAgent("b", new RoadLocation(0, 1), 1);
 		TestAgent c = createTestAgent("c", new RoadLocation(0, 4), 1);
 		TestAgent d = createTestAgent("d", new RoadLocation(0, 5), 1);
 		TestAgent e = createTestAgent("e", new RoadLocation(0, 9), 1);
 		TestAgent f = createTestAgent("f", new RoadLocation(1, 9), 1);
 		
 		incrementTime();
 		// check it's all correct
 		a.assertLocation(0, 0);
 		b.assertLocation(0, 1);
 		c.assertLocation(0, 4);
 		d.assertLocation(0, 5);
 		e.assertLocation(0, 9);
 		f.assertLocation(1, 9);
 		
 		// check c can turn off if d doesnt
 		d.performAction(new CellMove(0,1));
 		c.performAction(new CellMove(-1,2));
 		assertCollisions(0);
 		// check f can move to 0,1 if e turns off
 		e.performAction(new CellMove(-1,1));
 		f.performAction(new CellMove(-1,1));
 		assertCollisions(0);
 		// check a can turn off if b doesn't (sliproads are LOOONG)
 		b.performAction(new CellMove(0,1));
 		a.performAction(new CellMove(-1,3));
 		assertCollisions(0);
 	}
 }	
 
