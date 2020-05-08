 import java.util.ArrayList;
 import java.util.List;
 
 public class TestPricePredictor {
 	
 	public static void main (String args[]) {
 		// Setup price predictor options
 		int no_agents = 8;
 		int no_auctions = 5;
 		int nth_price = 1;
 		double ask_price = 1;
 		double ask_epsilon = 1;
 		int no_per_iteration = 256;
 		int max_iterations = 10;
 		int avg_iterations = 10;
 		double ks_threshold = 0.10;
 		double precision = 1.0;
 	
 		PricePredictorSAASched pp = new PricePredictorSAASched(no_agents, no_auctions, nth_price, ask_price, ask_epsilon,				
 				no_per_iteration, max_iterations, avg_iterations, ks_threshold, precision);
 		
 		ArrayList<DiscreteDistribution> pp_data = pp.predict();
 		
 		System.out.println("");
 		
 		for (int i = 0; i<pp_data.size(); i++) {
			System.out.println("ITEM " + i + ", EFP: " + pp_data.get(i).getExpectedFinalPrice(0));
 			pp_data.get(i).print(0);
 			System.out.println("");
 		}
 		
 		// Now that we have price predictions, play a game
 		// NOTE: I use a shortcut here and give all agents get the exact same prediction.
 		
 		System.out.println("");
 		System.out.println("------PLAYING SIMULATION-----");
 		
 		List<Agent> agents = new ArrayList<Agent>(no_agents);
 		// Create distribution agents
 		for (int i = 0; i<no_agents; i++)
 			agents.add(new DistributionPPAgent(i, new SchedulingValuation(no_auctions), pp_data));
 		
 		// Create one auction per good
 		List<SBAuction> auctions = new ArrayList<SBAuction>(no_auctions);
 		for (int i = 0; i<no_auctions; i++)
 			auctions.add(new SBNPAuction(i, 0, ask_price, ask_epsilon, agents, nth_price));
 		
 		// Play the auction
 		SimAscSimulation s = new SimAscSimulation(agents, auctions);
 		s.play();
 	}
 }
