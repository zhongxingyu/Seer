 package logic;
 
 import java.util.HashMap;
 
 import org.jgrapht.Graphs;
 
 
 public class MarceloAlgorithm extends Algorithm {
 	
 	
 	public MarceloAlgorithm(Network network, int nSteps) {
 		super(network, "Marcelo", nSteps);
 	}
 
 	@Override
 	public Network optimize(Scorer scorer) {
 		HashMap<Vertex, Integer> moduleAssignments = new HashMap<Vertex, Integer>();
		int my_V = 0; //TODO: who's this?
 		int NClusters;
		NClusters = network.clustersCount();
 		for (Vertex v : network.vertexSet()) {
 			moduleAssignments.put(v, my_V++ % NClusters);
 		}
 		
 		int time = 0;
 		Network changedNetwork = (Network) network.clone();
 		while (time < nSteps) {
 			changedNetwork = change(changedNetwork, moduleAssignments);
 			time++;
 		}
 		
 		return null;
 	}
 
 	protected Network change(Network changedNetwork, HashMap<Vertex, Integer> moduleAssignments) {
 		Vertex rndVertex = network.getRandomVertex(rnd);
 		
 		// Count the number of dependencies to the same module as well as to other modules
 		HashMap<Integer, Integer> moduleDependencies = new HashMap<Integer, Integer>();
 		for (int i = 0; i < network.clustersCount(); i++)
 			moduleDependencies.put(i, 0);
 		for (Vertex v : Graphs.neighborListOf(network, rndVertex)) {
 			int moduleAss = moduleAssignments.get(v);
 			moduleDependencies.put(moduleAss, moduleDependencies.get(v) + 1);
 		}
 		
 		HashMap<Integer, Double> moduleProbs = new HashMap<Integer, Double>();
 		
 		// Find maximum count
 		int nmax = -Integer.MAX_VALUE;
 		for (int i = 0; i < changedNetwork.clustersCount(); i++)
 			if (nmax < moduleDependencies.get(i))
 				nmax = moduleDependencies.get(i);
 		double gamma = (double)nmax / T;
 
 		//TODO who the hell is 'c'?
 		double c = 0;
		for (int i = 0; i < changedNetwork.clustersCount(); i++) {
 			moduleProbs.put(i, Math.exp((double) moduleDependencies.get(i) / T - gamma));
 			c += Math.exp((double) moduleDependencies.get(i) / T - gamma);
 		}
 		
 		double rand = rnd.nextDouble() * c;
 		int pos = 0;
 		int oldpos = 0;
 		double acc = moduleProbs.get(0);
 		
		while (acc < rand) {
 			pos++;
 			acc += moduleProbs.get(pos);			
 		}
 		
 		oldpos = moduleAssignments.get(rndVertex); 
		moduleAssignments.put(rndVertex, pos);
 
 		if (pos != oldpos)
 			changedNetwork.changeClusterAssignment(rndVertex, oldpos, pos);
 		
 		return changedNetwork;
 	}
 	
 }
