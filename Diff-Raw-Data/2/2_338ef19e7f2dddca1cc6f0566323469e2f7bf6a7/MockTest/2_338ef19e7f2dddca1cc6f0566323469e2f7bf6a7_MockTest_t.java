 package mock;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import junit.framework.TestCase;
 
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 
 import simulations.BasicSimulation;
 import uk.ac.imperial.presage2.rules.RuleModule;
 import uk.ac.imperial.presage2.rules.RuleStorage;
 import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
 import uk.ac.imperial.presage2.util.network.NetworkModule;
 import agents.NomicAgent;
 
 import com.google.inject.AbstractModule;
 
 public class MockTest extends TestCase {
 	Mockery context = new Mockery();
 	
 	public void mockTestInitialTest() {
 		final NomicAgent agent = context.mock(NomicAgent.class);
 		
 		Set<AbstractModule> modules = new HashSet<AbstractModule>();
 		
 		modules.add(new AbstractEnvironmentModule()
 				.setStorage(RuleStorage.class));
 		
 		modules.add(new RuleModule());
 		
 		modules.add(NetworkModule.noNetworkModule());
 		
 		BasicSimulation simulation = new BasicSimulation(modules);
 		
 		simulation.agents = 1;
 		simulation.finishTime = 10;
 		
 		context.checking(new Expectations() {{
 			exactly(10).of(agent).incrementTime();
 		}});
 		
 		simulation.run();
		
		context.assertIsSatisfied();
 	}
 }
