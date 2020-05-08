 package uk.ac.imperial.dws04;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.util.Set;
 import java.util.UUID;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import uk.ac.imperial.dws04.LaneMoveHandler;
 import uk.ac.imperial.dws04.RoadLocation;
 import uk.ac.imperial.presage2.core.Action;
 import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
 import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
 import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
 import uk.ac.imperial.presage2.core.event.EventBusModule;
 import uk.ac.imperial.presage2.core.messaging.Input;
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
 	
 	private final int lanes = 3;
 	private final int length = 10;
 
 	@Before
 	public void setUp() throws Exception {
 		injector = Guice.createInjector(
 				new AbstractEnvironmentModule().addActionHandler(
 						LaneMoveHandler.class)
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
 						bind(Integer.TYPE).annotatedWith(Names.named("params.maxDeccel")).toInstance(1);
 						bind(Integer.TYPE).annotatedWith(Names.named("params.junctionCount")).toInstance(0);
 						bind(Integer.TYPE).annotatedWith(Names.named("params.lanes")).toInstance(lanes);
 						bind(Integer.TYPE).annotatedWith(Names.named("params.length")).toInstance(length);
 					}
 				});
 
 		env = injector.getInstance(AbstractEnvironment.class);
 		handler = injector.getInstance(LaneMoveHandler.class);
 		roadEnvironmentService = injector.getInstance(RoadEnvironmentService.class);
 	}
 
 	@After
 	public void tearDown() throws Exception {
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
 	public void testValidMoves() throws ActionHandlingException {
 
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 2);
 
 		env.incrementTime();
 
 		a.assertLocation(1, 0);
 		a.assertSpeed(2);
 		assertEquals(0, handler.checkForCollisions(null));
 
 		// perform a series of valid moves.		
 		a.performAction(new CellMove(0, 2));
 		env.incrementTime();
 		a.assertLocation(1, 2);
 		a.assertSpeed(2);
 		assertEquals(0, handler.checkForCollisions(null));
 
 		a.performAction(new CellMove(0, 3));
 		env.incrementTime();
 		a.assertLocation(1, 5);
 		a.assertSpeed(3);
 		assertEquals(0, handler.checkForCollisions(null));
 
 		a.performAction(new CellMove(1, 3));
 		env.incrementTime();
 		a.assertLocation(2, 8);
 		a.assertSpeed(3);
 		assertEquals(0, handler.checkForCollisions(null));
 
 		a.performAction(new CellMove(0, 3));
 		env.incrementTime();
 		a.assertLocation(2, 1);
 		a.assertSpeed(3);
 		assertEquals(0, handler.checkForCollisions(null));
 
 		a.performAction(new CellMove(-1, 2));
 		env.incrementTime();
 		a.assertLocation(1, 3);
 		a.assertSpeed(2);
 		assertEquals(0, handler.checkForCollisions(null));
 		
 		//Testing max accel
 		int tempSpeed = 2+roadEnvironmentService.getMaxAccel();
 		int tempLocation = 3+tempSpeed;
 		a.performAction(new CellMove(-1, tempSpeed));
 		env.incrementTime();
 		a.assertLocation(0, tempLocation);
 		a.assertSpeed(tempSpeed);
 		assertEquals(0, handler.checkForCollisions(null));
 		
 		//Testing max deccel
 		tempSpeed = tempSpeed - roadEnvironmentService.getMaxDeccel();
 		a.performAction(new CellMove(0, tempSpeed));
 		env.incrementTime();
 		a.assertLocation(0, (tempLocation+tempSpeed));
 		a.assertSpeed(tempSpeed);
 		assertEquals(0, handler.checkForCollisions(null));
 		
 		// This won't pass the speed check
 		/*
 		 * //Testing moving very (legally) fast - this should probably loop...
 		 * a.performAction(new CellMove(0, (roadEnvironmentService.getMaxSpeed()-1) ));
 		 * env.incrementTime();
 		 * a.assertLocation(1, ((3+(roadEnvironmentService.getMaxSpeed()-1))%roadEnvironmentService.getLength()) );
 		 * assertEquals(0, handler.checkForCollisions(null));
 		 */
 		
 		
 	}
 
 	@Test
 	public void testInvalidLocationMove() throws ActionHandlingException {
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 0);
 		TestAgent b = createTestAgent("b", new RoadLocation(1, 5), 0);
 
 		env.incrementTime();
 
 		a.assertLocation(1, 0);
 		b.assertLocation(1, 5);
 		assertEquals(0, handler.checkForCollisions(null));
 
 		a.performAction(new CellMove(1, 1));
 		b.performAction(new CellMove(-1,1));
 		env.incrementTime();
 		a.assertLocation(2, 1);
 		b.assertLocation(0, 6);
 		assertEquals(0, handler.checkForCollisions(null));
 
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
 	public void testInvalidSpeedMove() throws ActionHandlingException {
 		int maxSpeed = roadEnvironmentService.getMaxSpeed();
 		TestAgent a = createTestAgent("a", new RoadLocation(2,0), 1);
 		TestAgent b = createTestAgent("b", new RoadLocation(1,0), maxSpeed);
 		
 		/*
 		 * FIXME the agent has a speed here but hasn't made a move action,
 		 * so its location hasn't been updated but neither has its speed.
 		 */
 		env.incrementTime();
 
 		a.assertLocation(2, 0);
 		b.assertLocation(1, 0);
 		assertEquals(0, handler.checkForCollisions(null));
 
 		a.performAction(new CellMove(0, 1));
 		b.performAction(new CellMove(0,maxSpeed));
 		env.incrementTime();
 		a.assertLocation(2, 1);
		b.assertLocation(1, (length%maxSpeed));
 		a.assertSpeed(1);
 		b.assertSpeed(maxSpeed);
 		assertEquals(0, handler.checkForCollisions(null));
 		
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
 			// deccelerate too fast.
 			b.performAction(new CellMove(0, (maxSpeed-(roadEnvironmentService.getMaxDeccel()+1)) ));
 			fail();
 		} catch (ActionHandlingException e) {
 		}
 	}
 	
 	@Test
 	public void testReverseMove() throws ActionHandlingException {
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 0);
 
 		env.incrementTime();
 
 		a.assertLocation(1, 0);
 		assertEquals(0, handler.checkForCollisions(null));
 
 		a.performAction(new CellMove(1, 1));
 		env.incrementTime();
 		a.assertLocation(2, 1);
 		assertEquals(0, handler.checkForCollisions(null));
 
 		try {
 			// move backwards.
 			a.performAction(new CellMove(0, -1));
 			fail();
 		} catch (ActionHandlingException e) {
 		}
 	}
 
 	@Test
 	public void testMoveToSameCellOnSameLaneCollisions()
 			throws ActionHandlingException {
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 3);
 		TestAgent b = createTestAgent("b", new RoadLocation(1, 3), 0);
 
 		env.incrementTime();
 
 		a.assertLocation(1, 0);
 		b.assertLocation(1, 3);
 
 		// b stationary, a move to b's cell
 		a.performAction(new CellMove(0, 3));
 		b.performAction(new CellMove(0, 0));
 		env.incrementTime();
 		// two collisions counted as both agents detect double occupied cell.
 		assertEquals(2, handler.checkForCollisions(null));
 		// note their locations have still be updated.
 		a.assertLocation(1, 3);
 		b.assertLocation(1, 3);
 
 		// separate them again
 		a.performAction(new CellMove(0, 2));
 		b.performAction(new CellMove(0, 1));
 		env.incrementTime();
 		a.assertLocation(1, 5);
 		b.assertLocation(1, 4);
 
 		// both moving, land on same cell
 		a.performAction(new CellMove(0, 1));
 		b.performAction(new CellMove(0, 2));
 		env.incrementTime();
 		assertEquals(2, handler.checkForCollisions(null));
 		a.assertLocation(1, 6);
 		b.assertLocation(1, 6);
 	}
 
 	@Test
 	public void testSameLaneCollision() throws ActionHandlingException {
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 5);
 		TestAgent b = createTestAgent("b", new RoadLocation(1, 1), 3);
 
 		env.incrementTime();
 
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
 		env.incrementTime();
 		assertEquals(1, handler.checkForCollisions(null));
 		a.assertLocation(1, 5);
 		b.assertLocation(1, 4);
 
 		// valid moves
 		a.performAction(new CellMove(0, 4));
 		b.performAction(new CellMove(0, 4));
 		env.incrementTime();
 		assertEquals(0, handler.checkForCollisions(null));
 		a.assertLocation(1, 9);
 		b.assertLocation(1, 8);
 
 		// b leapfrog a over wrap-around point
 		a.performAction(new CellMove(0, 3));
 		b.performAction(new CellMove(0, 5));
 		env.incrementTime();
 		assertEquals(1, handler.checkForCollisions(null));
 		a.assertLocation(1, 2);
 		b.assertLocation(1, 3);
 
 	}
 
 	@Test
 	public void testMultiLaneCollision1() throws ActionHandlingException {
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 3);
 		TestAgent b = createTestAgent("b", new RoadLocation(2, 1), 1);
 
 		env.incrementTime();
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
 		env.incrementTime();
 		a.assertLocation(2, 3);
 		b.assertLocation(2, 2);
 		assertEquals(1, handler.checkForCollisions(null));
 	}
 	
 	@Test
 	public void testMultiLaneCollision2() throws ActionHandlingException {
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 3);
 		TestAgent b = createTestAgent("b", new RoadLocation(2, 1), 1);
 
 		env.incrementTime();
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
 		env.incrementTime();
 		assertEquals(1, handler.checkForCollisions(null));
 		b.assertLocation(1, 2);
 		a.assertLocation(2, 3);
 	}
 
 	@Test
 	public void testMultiLaneCollision3() throws ActionHandlingException {
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 0);
 		TestAgent b = createTestAgent("b", new RoadLocation(2, 1), 0);
 
 		env.incrementTime();
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
 		env.incrementTime();
 		a.assertLocation(2, 1);
 		b.assertLocation(1, 2);
 		assertEquals(0, handler.checkForCollisions(null));
 	}
 
 	@Test
 	public void testMultiLaneCollision4() throws ActionHandlingException {
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 3);
 		TestAgent b = createTestAgent("b", new RoadLocation(2, 1), 1);
 
 		env.incrementTime();
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
 		env.incrementTime();
 		a.assertLocation(1, 3);
 		b.assertLocation(1, 2);
 		assertEquals(1, handler.checkForCollisions(null));
 	}
 
 	@Test
 	public void testMultiLaneCollision5() throws ActionHandlingException {
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 0);
 		TestAgent b = createTestAgent("b", new RoadLocation(3, 1), 0);
 
 		env.incrementTime();
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
 		env.incrementTime();
 		a.assertLocation(2, 1);
 		b.assertLocation(2, 2);
 		assertEquals(0, handler.checkForCollisions(null));
 	}
 
 	@Test
 	public void testMultiLaneCollision6() throws ActionHandlingException {
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 0);
 		TestAgent b = createTestAgent("b", new RoadLocation(2, 0), 0);
 
 		env.incrementTime();
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
 		env.incrementTime();
 		a.assertLocation(2, 1);
 		b.assertLocation(1, 1);
 		assertEquals(2, handler.checkForCollisions(null));
 	}
 
 	@Test
 	public void testMultiLaneCollision7() throws ActionHandlingException {
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 2);
 		TestAgent b = createTestAgent("b", new RoadLocation(2, 0), 1);
 		TestAgent c = createTestAgent("a", new RoadLocation(3, 1), 1);
 		TestAgent d = createTestAgent("b", new RoadLocation(2, 2), 1);
 
 		env.incrementTime();
 
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
 		env.incrementTime();
 		a.assertLocation(1, 2);
 		b.assertLocation(2, 1);
 		c.assertLocation(2, 2);
 		d.assertLocation(2, 3);
 		assertEquals(0, handler.checkForCollisions(null));
 	}
 
 	@Test
 	public void testMultiLaneCollision8() throws ActionHandlingException {
 		TestAgent a = createTestAgent("a", new RoadLocation(1, 0), 0);
 		TestAgent b = createTestAgent("b", new RoadLocation(1, 1), 0);
 
 		env.incrementTime();
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
 		env.incrementTime();
 		a.assertLocation(2, 1);
 		b.assertLocation(1, 1);
 		assertEquals(0, handler.checkForCollisions(null));
 	}
 }
