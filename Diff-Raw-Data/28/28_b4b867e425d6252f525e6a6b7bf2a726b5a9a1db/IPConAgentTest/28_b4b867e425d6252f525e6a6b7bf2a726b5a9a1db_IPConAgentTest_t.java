 /**
  * 
  */
 package uk.ac.imperial.dws04.Presage2Experiments.IPCon;
 
 import org.apache.log4j.Logger;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.junit.After;
 import org.junit.Before;
 
 import uk.ac.imperial.dws04.Presage2Experiments.IPConService;
 import uk.ac.imperial.dws04.Presage2Experiments.LaneMoveHandler;
 import uk.ac.imperial.dws04.Presage2Experiments.ParticipantRoadLocationService;
 import uk.ac.imperial.dws04.Presage2Experiments.ParticipantSpeedService;
import uk.ac.imperial.dws04.Presage2Experiments.RoadAgent;
import uk.ac.imperial.dws04.Presage2Experiments.RoadAgentGoals;
 import uk.ac.imperial.dws04.Presage2Experiments.RoadEnvironmentService;
import uk.ac.imperial.dws04.Presage2Experiments.RoadLocation;
 import uk.ac.imperial.dws04.Presage2Experiments.SpeedService;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConProtocol.Role;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.IPCNV;
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
 
 	final private Logger logger = Logger.getLogger(IPConDrlsTest.class);
 
 	Injector injector;
 	RuleStorage rules;
 	StatefulKnowledgeSession session;
 	
 	AbstractEnvironment env;
 	//RoadEnvironmentService roadEnvironmentService;
 	SpeedService globalSpeedService;
 	RoadEnvironmentService globalRoadEnvironmentService;
 	
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
 					.addParticipantEnvironmentService(IPConService.class)
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
 		
 		rules = injector.getInstance(RuleStorage.class);
 		session = injector.getInstance(StatefulKnowledgeSession.class);
 	}
 	
 	@After
 	public void tearDown() throws Exception {
 		session.dispose();
 	}
	
	private RoadAgent createAgent(String name, RoadLocation startLoc, int startSpeed, RoadAgentGoals goals) {
		RoadAgent a = new RoadAgent(Random.randomUUID(), name, startLoc, startSpeed, goals);
		injector.injectMembers(a);
		a.initialise();
		return a;
	}
 }
