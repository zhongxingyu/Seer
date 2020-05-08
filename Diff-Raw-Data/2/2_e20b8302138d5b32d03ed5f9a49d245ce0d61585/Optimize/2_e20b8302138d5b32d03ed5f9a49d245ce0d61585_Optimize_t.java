 package mage.Core;
 
 import java.util.ArrayList;
 import java.util.Stack;
 
 import utils.Plot;
 
 public class Optimize {
 
 	public static ArrayList< ArrayList<Double[]> > bo_plots; 
 	public static ArrayList< ArrayList<String  > > plot_names;
 	
 	public static void optimize(ArrayList<Oligo> pool) throws Exception {
 		
 		// Create new array list for the plot names etc.
 		if ( mage.Switches.Flags.plot) {
 
 			// Set up the plot List stuff
 			bo_plots = new ArrayList< ArrayList<Double[]>>();
 			plot_names = new ArrayList< ArrayList<String>>();
 			for (Oligo ol : pool) {
 				bo_plots.add(new ArrayList<Double[]>(ol.getMarginLength()));
 				plot_names.add(new ArrayList<String>(ol.getMarginLength()));
 			}
 
 		}
 
 
 		// Optimize the pool
 		OptimizeHeuristic(pool);
 
 		if (mage.Switches.Flags.plot) {
 
 			// Plot the BO Values over time
 			plotBO();
 
 			for (Oligo ol: pool) {
 				plotBG_DG(ol); 
 			}
 		}
 	}
 
 
 	/**
 	 * Heurisitic for optimization
 	 * 
 	 * @param pool			An a list of Oligos 
 	 * @throws Exception
 	 */
 	private static void OptimizeHeuristic(ArrayList<Oligo> pool) throws Exception {
 
 		System.out.println("\n# Calculating Genome Homology and Free Energy Calculation [BG & DG]");
 		// For each oligo, calculate the Blast Genome and Free Energy Values
 		for ( Oligo ol : pool) {
 
 			ol.calc_bg();			// Calculate Blast Genome for all positions on margins
 			ol.calc_dg();			// Calculate Free Energy score for all positions on margins
 
 			System.out.print(ol.getOligoId()+" ");
 		}
 
 		// Blast all the oligos against each other
 		System.out.println("\n\n# Calculating Oligo to Oligo Homology [BO]");
 		for (int ii = 0; ii<(pool.size()-1); ii++ ) {
 
 			// Create a list of query oligos
 			ArrayList<Oligo> queries = new ArrayList<Oligo>(pool.size()-ii-1);
 
 			// Generate the batch of queries
 			for (int jj = ii+1; jj<pool.size(); jj++ ) {
 				queries.add(pool.get(jj));
 			}
 
 			// Blast the queries against subject ii
 			Oligo.BlastOligo(pool.get(ii),queries);
 
 			System.out.print((ii+1)+" ");
 		}
 
 		// Heuristic Optimization
 		System.out.println("\n\n# Heurisitic Approach\n##################");
 
 
 		// Choose the oligo with the smallest mistarget score
 		// Then sort by mistarget score and then repeat	
 		Stack<Oligo> stack = new Stack<Oligo>();
 		stack.addAll(pool);
 
 		int iteration = 1;
 
 		while (stack.size() > 0) {
 
 			System.err.print("\n# Iteration "+ (iteration) +  "");
 
 			// Re/Calculate BO for the entire stack
 			for (Oligo ol: stack){
 				ol.calc_bo();
 
 				if (mage.Switches.Flags.plot)
 				addPlot( ol.boList().toArray( new Double[ol.bgList().size()])
 						, iteration ,ol.getOligoId() );
 
 				System.err.println("Oligo " + ol.getOligoId() + ":\t"+ol.scoreAt(ol.getGreedyChoice()).toString());
 			}
 
 			// Sort by whatever greedy-score
 			Oligo.sort(stack);
 
 			// Select the best choice and the repeat until the stack is empty
 			Oligo greedyChoice = stack.pop();
 			System.out.println("Oligo "+ greedyChoice.getOligoId() + ":\t"+greedyChoice.scoreAt(greedyChoice.getGreedyChoice()).toString() ); 
 			greedyChoice.select();
 			iteration++;
 		}
 
 		// Print the final configuration
 		System.out.println("\n# Heuristic Choice");
 		for (Oligo ol: pool) {
 
 			// Re-calculates the overlapping BO scores for the final configuration
 			ol.finalize();
 
 			// Print out the results
 			System.out.println("Oligo "+ ol.getOligoId() + ":\t"+ ol.currentScore().toString() );
 		}
 
 	} 
 
 
 
 	private static void plotBO () {
 		for (int ii = 0; ii<bo_plots.size() ;ii++){
 			Plot pl = new Plot();
 			pl.addGraph(bo_plots.get(ii), plot_names.get(ii));
 			pl.setToLines();
 			pl.title("Oligo " + ii );
			pl.draw("Oligo_" + (ii+1) +"_BO" );
 		}
 	}
 
 	/**
 	 *  Plot BO Scores for a given iteration as a function of position
 	 * 
 	 */
 	public static void plotAllBO() {
 
 		// Create a new plot
 		utils.Plot pp = new utils.Plot();
 
 		// Add all the graphs
 		for (int ii = 0; ii<bo_plots.size() ;ii++){
 			pp.addGraph(bo_plots.get(ii), plot_names.get(ii));
 		}
 
 		// Set plotsytle to lines, set title and then draw
 		pp.setToLines();
 		pp.title("Blast Oligo Variation");
 		pp.draw();
 	}
 
 	/**
 	 * Plot the Blast Genome and Free Energy Scores as a function of position
 	 * @param ol
 	 */
 
 	public static void plotBG_DG(Oligo ol){
 
 		Double[] bgScores = ol.bgList().toArray(new Double[ol.bgList().size()]);
 		Double[] dgScores  = ol.dgList().toArray(new Double[ol.dgList().size()]);
 
 		Plot pl = new Plot();
 		pl.addGraph(bgScores, "Blast Genome");
 		pl.addGraph(dgScores, "Free Energy Scores"); 
 		pl.setToLines();
 		pl.title("Oligo : "+ol.getOligoId());
 		pl.draw("Oligo_"+ol.getOligoId()+"_BG_DG");
 	}
 
 
 	/**
 	 * Helper function for capturing the plotting
 	 * 
 	 * @param array
 	 * @param iteration
 	 * @param oligoID
 	 */
 	private static void addPlot(Double[] array, int iteration, int oligoID) {
 
 		// Take array and create plot set and store
 		String name = "Iteration "+iteration;
 		int id = oligoID -1;
 		bo_plots.get(id).add(array);
 		plot_names.get(id).add(name);
 
 	}
 }
