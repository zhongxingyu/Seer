 package bnb.tsp;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import bnb.lord.LordRunner;
 import bnb.lord.VassalProxy;
 import bnb.rpc.Ports;
 import bnb.vassal.LordProxy;
 import bnb.vassal.VassalRunner;
 
 public class TestOneVassal {
 	private static final Logger LOG = Logger.getLogger(TestOneVassal.class);
 	
 	public static void main(String[] args) throws IOException {
		int numCores = 1;//Runtime.getRuntime().availableProcessors() * 4;
 		LOG.info("numCores: " + numCores);
 
 		LordRunner lord = new LordRunner(Ports.DEFAULT_LORD_PORT);
 		lord.start();
 		LOG.info("started lord");
 		
 		LordProxy lordProxy = new LordProxy("localhost", Ports.DEFAULT_LORD_PORT); 
 		LOG.info("instantiated lord proxy");
 
 		VassalRunner vassal = new VassalRunner(lordProxy, numCores, 1, Ports.DEFAULT_VASSAL_PORT);
 		vassal.start();
 		LOG.info("started vassal");
 		
 		VassalProxy vassalProxy = new VassalProxy("localhost", Ports.DEFAULT_VASSAL_PORT);
 		LOG.info("started vassal proxy");
 		
		lord.registerVassal(vassalProxy, 1);
		
		final int numCities = 4;
 		
 		City[] cities = ProblemGen.genCities(numCities);
 		TspProblem problem = new TspProblem(cities);
 		List<City> remainingCities = new LinkedList<City>();
 		remainingCities.addAll(Arrays.asList(cities).subList(1, cities.length));
 		
 		TspNode root = new TspNode(cities[0], cities[0], 1, null, remainingCities, problem);
 
 		lord.runJob(root, problem, /*Double.MAX_VALUE*/300, Arrays.asList(vassalProxy), 1);
 	}	
 }
