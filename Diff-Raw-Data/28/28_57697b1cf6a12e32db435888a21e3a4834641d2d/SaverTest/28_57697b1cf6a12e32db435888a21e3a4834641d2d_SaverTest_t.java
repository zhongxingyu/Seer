 package edu.wheaton.simulator.test.statistics;
 
 import java.io.File;
 import java.util.HashSet;
 import java.util.Set;
 
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableSet;
 
 import edu.wheaton.simulator.datastructure.ElementAlreadyContainedException;
 import edu.wheaton.simulator.datastructure.Grid;
 import edu.wheaton.simulator.entity.Agent;
 import edu.wheaton.simulator.entity.Prototype;
import edu.wheaton.simulator.simulation.Simulator;
 import edu.wheaton.simulator.simulation.end.SimulationEnder;
 import edu.wheaton.simulator.statistics.Saver;
 import edu.wheaton.simulator.statistics.TriggerSnapshot;
 
 public class SaverTest {
 
 	String s;
 	Agent agent;
 	Agent agentOther;
 	Grid grid;
 	Prototype prototypeOne;
 	Prototype prototypeTwo;
 	Integer step;
 	Set<TriggerSnapshot> triggers;
 	SimulationEnder simEnder; 
 	
 	@Before
 	public void setUp() throws ElementAlreadyContainedException {
		Simulator.getInstance().load("test", 10, 10, new SimulationEnder());
		Simulator.getInstance().setAtomicUpdate(); 
		
 		grid = new Grid(10, 10);
		grid.addField("Depth", "100");
		grid.setAtomicUpdater(); 
 		
 		prototypeOne = new Prototype("Prototype 1");
 		try {
 			prototypeOne.addField("Pig", "Tom");
 			prototypeOne.addField("Monkey", "Olly");
 			prototypeOne.addField("Cat", "Joomba");
 		} catch (ElementAlreadyContainedException e) {
 			e.printStackTrace();
 		}
 		agent = prototypeOne.createAgent(grid);
 		
 		prototypeTwo = new Prototype("Prototype 2");
 		try {
 			prototypeTwo.addField("Crayfish", "Paul");
 			prototypeTwo.addField("Meerkat", "Timon");
 			prototypeTwo.addField("Person", "John Charles");
 		} catch (ElementAlreadyContainedException e) {
 			e.printStackTrace();
 		}
 		agentOther = prototypeTwo.createAgent(grid);
 		
 		step = new Integer(23);
 		
 		//Create the ending conditions
 		simEnder = new SimulationEnder(); 
 		simEnder.setPopLimit(prototypeOne.getName(), 100); 
 		simEnder.setPopLimit(prototypeTwo.getName(), 100); 
 		simEnder.setStepLimit(20); 
 		
 		//Create the list of TriggerSnapshots
 		triggers = new HashSet<TriggerSnapshot>();
 		triggers.add(new TriggerSnapshot("trigger1", 1, "conditionExpression", "behaviorExpression"));
 	}
 
 	@After
 	public void tearDown() {
 		agent = null;
 		agentOther = null;
 		grid = null;
 		prototypeOne = null;
 		prototypeTwo = null;
 		step = null;
 	}
 
 	@Test
 	public void testSaveSimulation() {
 		//Create a Set of Agents				
 		Set<Agent> agents = new HashSet<Agent>(); 
 		agents.add(agent); 
 		agents.add(agentOther);
		grid.setLinearUpdater(); 
 		
 		// Creating a HashMap of PrototypeSnapshots
 		ImmutableSet.Builder<Prototype> builder = new ImmutableSet.Builder<Prototype>();
 		builder.add(prototypeOne);
 		builder.add(prototypeTwo); 
 		ImmutableSet<Prototype> prototypes = builder.build(); 
 		
 		Saver s = new Saver();
 		s.saveSimulation(new File("SimulationState2"), agents, prototypes, grid.getCustomFieldMap(), 
 				grid.getWidth(), grid.getHeight(), simEnder); 
 	}
 	
 	@Test
 	public void testSavePrototype(){
 		Saver s = new Saver();
 		System.out.println(""); 
 		s.savePrototype(prototypeOne);
 		System.out.println(""); 
 		s.savePrototype(prototypeTwo); 
 		
 		//Check the terminal output
 		Assert.assertTrue(true);
 	}
 }
