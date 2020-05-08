 /**
  * 
  */
 package uk.ac.imperial.dws04.Presage2Experiments.IPCon;
 
 import static org.junit.Assert.*;
 
import java.lang.reflect.Array;
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.Collection;
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import uk.ac.imperial.dws04.Presage2Experiments.LaneMoveHandler;
 import uk.ac.imperial.dws04.Presage2Experiments.ParticipantRoadLocationService;
 import uk.ac.imperial.dws04.Presage2Experiments.ParticipantSpeedService;
 import uk.ac.imperial.dws04.Presage2Experiments.RoadAgent;
 import uk.ac.imperial.dws04.Presage2Experiments.RoadAgentGoals;
 import uk.ac.imperial.dws04.Presage2Experiments.RoadEnvironmentService;
 import uk.ac.imperial.dws04.Presage2Experiments.RoadLocation;
 import uk.ac.imperial.dws04.Presage2Experiments.SpeedService;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.Role;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.IPConAction;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.Request0A;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.HasRole;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent;
 import uk.ac.imperial.presage2.core.IntegerTime;
 import uk.ac.imperial.presage2.core.event.EventBusModule;
 import uk.ac.imperial.presage2.core.simulator.SimTime;
 import uk.ac.imperial.presage2.core.util.random.Random;
 import uk.ac.imperial.presage2.rules.RuleModule;
 import uk.ac.imperial.presage2.rules.RuleStorage;
 import uk.ac.imperial.presage2.util.environment.AbstractEnvironment;
 import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
 import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
 import uk.ac.imperial.presage2.util.location.area.Area;
 import uk.ac.imperial.presage2.util.location.area.WrapEdgeHandler;
 import uk.ac.imperial.presage2.util.location.area.Area.Edge;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.name.Names;
 
 /**
  * @author dws04
  *
  */
 public class IPConAgentTest {
 
 	final private Logger logger = Logger.getLogger(this.getClass());
 
 	Injector injector;
 	RuleStorage rules;
 	StatefulKnowledgeSession session;
 	
 	AbstractEnvironment env;
 	//RoadEnvironmentService roadEnvironmentService;
 	SpeedService globalSpeedService;
 	RoadEnvironmentService globalRoadEnvironmentService;
 	IPConService globalIPConService;
 	
 	private int lanes = 3;
 	private int length = 10;
 	private int maxSpeed = 10;
 	private int maxAccel = 1;
 	private int maxDecel = 1;
 	private int junctionCount = 0;
 	IntegerTime time = new IntegerTime(0);
 	SimTime sTime = new SimTime(time);
 	
 	
 	@Before
 	public void setUp() throws Exception {
 		injector = Guice.createInjector(
 				// rule module
 				new RuleModule()
 					.addClasspathDrlFile("IPConUtils.drl")
 					.addClasspathDrlFile("IPConPowPer.drl")
 					.addClasspathDrlFile("IPCon.drl")
 					.addClasspathDrlFile("IPConOblSan.drl"),
 				new AbstractEnvironmentModule()
 					.addActionHandler(LaneMoveHandler.class)
 					.addParticipantEnvironmentService(ParticipantLocationService.class)
 					.addParticipantEnvironmentService(ParticipantRoadLocationService.class)
 					.addParticipantEnvironmentService(ParticipantSpeedService.class)
 					.addParticipantEnvironmentService(ParticipantIPConService.class)
 					.addGlobalEnvironmentService(RoadEnvironmentService.class)
 					.setStorage(RuleStorage.class),
 				Area.Bind.area2D(lanes, length).addEdgeHandler(Edge.Y_MAX,
 						WrapEdgeHandler.class), new EventBusModule(),
 				new AbstractModule() {
 					// add in params that are required
 					@Override
 					protected void configure() {
 						bind(Integer.TYPE).annotatedWith(Names.named("params.maxSpeed")).toInstance(maxSpeed);
 						bind(Integer.TYPE).annotatedWith(Names.named("params.maxAccel")).toInstance(maxAccel);
 						bind(Integer.TYPE).annotatedWith(Names.named("params.maxDecel")).toInstance(maxDecel);
 						bind(Integer.TYPE).annotatedWith(Names.named("params.junctionCount")).toInstance(junctionCount);
 						bind(Integer.TYPE).annotatedWith(Names.named("params.lanes")).toInstance(lanes);
 						bind(Integer.TYPE).annotatedWith(Names.named("params.length")).toInstance(length);
 					}
 				});
 
 		env = injector.getInstance(AbstractEnvironment.class);
 		globalSpeedService = injector.getInstance(SpeedService.class);
 		globalRoadEnvironmentService = injector.getInstance(RoadEnvironmentService.class);
 		globalIPConService = injector.getInstance(IPConService.class);
 		
 		rules = injector.getInstance(RuleStorage.class);
 		session = injector.getInstance(StatefulKnowledgeSession.class);
 	}
 	
 	@After
 	public void tearDown() throws Exception {
 		session.dispose();
 	}
 	
 	private RoadAgent createAgent(String name, RoadLocation startLoc, int startSpeed) {
 		RoadAgent a = new RoadAgent(Random.randomUUID(), name, startLoc, startSpeed, new RoadAgentGoals((Random.randomInt(maxSpeed)+1), Random.randomInt(length), 0));
 		// FIXME TODO Not sure if this is needed...?
 		injector.injectMembers(a);
 		a.initialise();
 		//Call this if needed
 		//initAgent(a.getIPConHandle(), roles, 0, issue, cluster);
 		return a;
 	}
 	
 	/**
 	 * Artificially inserting facts without the service for sake of testing
 	 */
 	private void addRoles(IPConAgent agent, Role[] roles, Integer revision, String issue, UUID cluster) {
 		//Set roles
 		for (Role role : roles) {
 			session.insert(new HasRole(role, agent, revision, issue, cluster));
 		}
 		// Initially didn't vote
 		//session.insert(new Voted(agent, revision, 0, IPCNV.val(), issue, cluster));
 		// And the reportedvote for the initially
 		//session.insert(new ReportedVote(agent, revision, 0, IPCNV.val(), revision, 0, issue, cluster));
 	}
 	
 	@Test
 	public void testObligationInstantiation() throws Exception {
 		/**
 		 * This requires access to a fn which should be private, so calling public wrapper fn
 		 */
 		final Integer revision = 1;
 		final String issue = "IssueString";
 		final UUID cluster = Random.randomUUID();
 		Object value = "VALUE";
 		/*
 		 * Create agents
 		 */
 		RoadAgent a1 = createAgent("a1", new RoadLocation(0,0), 1);
 		addRoles(a1.getIPConHandle(), new Role[]{Role.LEADER, Role.ACCEPTOR}, revision, issue, cluster);
 		RoadAgent a2 = createAgent("a2", new RoadLocation(1,0), 1);
 		addRoles(a2.getIPConHandle(), new Role[]{Role.PROPOSER, Role.ACCEPTOR}, revision, issue, cluster);
 		RoadAgent a3 = createAgent("a3", new RoadLocation(2,0), 1);
 		addRoles(a3.getIPConHandle(), new Role[]{Role.ACCEPTOR}, revision, issue, cluster);
 		
 		/*
 		 * Set up cluster to ensure at least one agent-relative obligation and one agent-neutral obligation
 		 * where the agents are all permitted. Preferably the permissions should include examples of multiple
 		 * options, no options, single options, and unconstrained(-ish) options. 
 		 */
 		/*
 		 * A2 requests. A1 now should be obligated to prepare
 		 */
 		session.insert(new Request0A(a2.getIPConHandle(), revision, value, issue, cluster));
 		rules.incrementTime();
 		Collection<IPConAction> obl = globalIPConService.getActionQueryResultsForRIC("getObligations", "Prepare1A", a1.getIPConHandle(), revision, issue, cluster);
 		logger.info("A1 obligated to :" + obl);
		assertEquals(1,obl.size());
		Collection<IPConAction> per = globalIPConService.getActionQueryResultsForRIC("getPermissions", "Prepare1A", a1.getIPConHandle(), revision, issue, cluster);
		logger.info("A1 permitted to :" + per);
		assertEquals(1,per.size());
 		
 		ArrayList<IPConAction> a1Obl = a1.TESTgetInstantiatedObligatedActionQueue();
 		assertEquals(1, a1Obl.size());
		assertTrue(a1Obl.get(0).fulfils((IPConAction)obl.toArray()[0]));
 		logger.info("A1 instantiated: " + a1Obl);
 		
 		
 		/*
 		 * Check resulting actions
 		 */
 	}
 	
 	@Test
 	public void test() throws Exception {
 		/*
 		 * Create agents
 		 */
 		
 		/*
 		 * One of them arrogates
 		 */
 		
 		/*
 		 * Agents join
 		 */
 		
 		/*
 		 * Proposal for issues
 		 */
 		
 		/*
 		 * Prepare
 		 */
 		
 		/*
 		 * Response
 		 */
 		
 		/*
 		 * Submit
 		 */
 		
 		/*
 		 * Vote
 		 */
 	}
 }
