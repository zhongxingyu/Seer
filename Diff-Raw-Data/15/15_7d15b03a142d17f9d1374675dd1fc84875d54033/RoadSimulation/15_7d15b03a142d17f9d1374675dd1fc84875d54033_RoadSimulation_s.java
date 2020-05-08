 /**
  * See http://www.presage2.info/ for more details on Presage2
  */
 package uk.ac.imperial.dws04;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import com.google.inject.AbstractModule;
 
 import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
 import uk.ac.imperial.presage2.core.simulator.InjectedSimulation;
 import uk.ac.imperial.presage2.core.simulator.Parameter;
 import uk.ac.imperial.presage2.core.simulator.Scenario;
 import uk.ac.imperial.presage2.core.util.random.Random;
 import uk.ac.imperial.presage2.core.event.EventBusModule;
 import uk.ac.imperial.presage2.core.event.EventListener;
 import uk.ac.imperial.presage2.core.plugin.PluginModule;
 import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
 import uk.ac.imperial.presage2.util.location.LocationStoragePlugin;
 import uk.ac.imperial.presage2.util.location.area.Area;
 import uk.ac.imperial.presage2.util.location.area.WrapEdgeHandler;
 import uk.ac.imperial.presage2.util.location.area.Area.Edge;
 import uk.ac.imperial.presage2.util.network.NetworkModule;
 
 /**
  * run with uk.ac.imperial.dws04.RoadSimulation finishTime=10 length=10 lanes=4 initialAgents=2 maxSpeed=3 maxAccel=1 maxDecel=1 junctionCount=0
  * CLI add -classname uk.ac.imperial.dws04.RoadSimulation -finish 10 -name RoadSim  -P length=10 -P lanes=4 -P initialAgents=2 -P maxSpeed=3 -P maxAccel=1 -P maxDecel=1 -P junctionCount=0
  * 
  * @author dws04
  *
  */
 public class RoadSimulation extends InjectedSimulation {
 
 	@Parameter(name="length")
 	public int length;
 	
 	/**
 	 * We expect this to be 4(or more): lane0 is the "onramp" with lane1+ being the actual lanes.
 	 */
 	@Parameter(name="lanes")
 	public int lanes;
 	 
 	@Parameter(name="initialAgents")
 	public int initialAgents;
 	 
 	@Parameter(name="maxSpeed")
 	public int maxSpeed;
 	 
 	@Parameter(name="maxAccel")
 	public int maxAccel;
 	 
 	@Parameter(name="maxDecel")
 	public int maxDecel;
 	 
 	@Parameter(name="junctionCount")
 	public int junctionCount;
 	
 	/**
 	 * @param modules
 	 */
 	public RoadSimulation(Set<AbstractModule> modules) {
 		super(modules);
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.ac.imperial.presage2.core.simulator.InjectedSimulation#addToScenario(uk.ac.imperial.presage2.core.simulator.Scenario)
 	 */
 	@Override
 	protected void addToScenario(Scenario s) {
 		for (int i = 0; i < initialAgents; i++) {
 			int initialX = Random.randomInt(lanes);
 			int initialY = Random.randomInt(length);
 			RoadLocation startLoc = new RoadLocation(initialX, initialY);
			int startSpeed = Random.randomInt(maxSpeed);
			RoadAgentGoals goals = new RoadAgentGoals((Random.randomInt(maxSpeed-1)+1), 0, 1);
 			s.addParticipant(new RoadAgent(Random.randomUUID(), "agent"+ i, startLoc, startSpeed, goals));
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.ac.imperial.presage2.core.simulator.InjectedSimulation#getModules()
 	 */
 	@Override
 	protected Set<AbstractModule> getModules() {
 		Set<AbstractModule> modules = new HashSet<AbstractModule>();
 	 
 		// 2D area that wraps at the top
 		modules.add(Area.Bind.area2D(lanes, length).addEdgeHander(Edge.Y_MAX, WrapEdgeHandler.class));
 		// Environment with MoveHandler and ParticipantLocationService
 		modules.add(new AbstractEnvironmentModule()
 			.addActionHandler(LaneMoveHandler.class)
 			.addParticipantEnvironmentService(ParticipantRoadLocationService.class)
 			.addParticipantEnvironmentService(ParticipantSpeedService.class)
 			//.addGlobalEnvironmentService(SpeedService.class)
 			.addGlobalEnvironmentService(RoadEnvironmentService.class));
 		// No network
 		modules.add(NetworkModule.noNetworkModule());
 		// Location plugin
 		// TODO need to modify the plugin
 		modules.add(new PluginModule().addPlugin(LocationStoragePlugin.class));
 	 
 		return modules;
 	}
 	
 	/**
 	 * Allows new agents to be inserted at the onramps at the end of a timecycle based on a function
 	 *  TODO 
 	 * @param e
 	 * @return
 	 */
 	@EventListener
 	public int makeNewAgent(EndOfTimeCycle e) {
 		if (false) {
 			this.scenario.addParticipant(new RoadAgent(null, null, null, 0, null));
 		}
 		return 0;
 	}
 
 }
