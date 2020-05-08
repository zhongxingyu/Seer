 package myagents;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import com.google.inject.AbstractModule;
 
 import uk.ac.imperial.presage2.core.simulator.InjectedSimulation;
 import uk.ac.imperial.presage2.core.simulator.Parameter;
 import uk.ac.imperial.presage2.core.simulator.Scenario;
 import uk.ac.imperial.presage2.core.util.random.Random;
 import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
 import uk.ac.imperial.presage2.util.location.Location;
 import uk.ac.imperial.presage2.util.location.MoveHandler;
 import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
 import uk.ac.imperial.presage2.util.location.area.Area;
 import uk.ac.imperial.presage2.util.network.NetworkModule;
 
 public class MySimulation extends InjectedSimulation {
 
 	@Parameter(name = "size")
 	public int size;
 	
 	@Parameter(name = "agents")
 	public int agents;
 	
 	public MySimulation(Set<AbstractModule> modules){
 		super(modules);
 	}
 	
 	@Override
 	protected Set<AbstractModule> getModules() {
 		Set<AbstractModule> modules = new HashSet<AbstractModule>();
 		
 		modules.add(Area.Bind.area2D(size, size));
 		
 		
 		//Here is where you define the enviroment by adding the required modules to it.
 		modules.add(new AbstractEnvironmentModule()
						.addActionHandler(MoveHandler.class));
 		
 		modules.add(NetworkModule.noNetworkModule());
 		
 		return modules;
 	}
 
 	@Override
 	protected void addToScenario(Scenario s) {
 		for(int i = 0; i < agents; i++){
 			int initialX = Random.randomInt(size);
 			int initialY = Random.randomInt(size);
 			Location startLoc = new Location(initialX, initialY);
 			s.addParticipant(new MyAgent(Random.randomUUID(), "agent" + i, startLoc));
 		}
 
 	}
 
 }
