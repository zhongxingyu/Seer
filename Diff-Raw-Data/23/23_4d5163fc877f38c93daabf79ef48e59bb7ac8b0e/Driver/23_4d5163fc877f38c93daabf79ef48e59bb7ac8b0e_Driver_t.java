 package planner;
 import java.util.ArrayList;
 import java.util.HashSet;
 import presentation.MainGUI;
 import auxparsers.ChallengeParser;
 import serviceparser.ServiceParser;
 import conceptparser.ConceptInstanceMap;
 
 public class Driver {
 
 	/**
 	 * @param args
 	 * @throws Exception 
 	 */
 	public static void main(String[] args) throws Exception 
 	{
 		MainGUI gui = new MainGUI();
 		gui.setVisible(true);
 	}
 	
 	public static void run(
 			String challengeFile
 			, String serviceFile
 			, String owlFile
 			, int slnWanted
 			, int prl) throws Exception
 	{
 
 		ChallengeParser cp = new ChallengeParser();
 		State initialState = new State();
 		State goalState = new State();
 		ArrayList<WebService> availableActions;
 		ConceptInstanceMap.reset();
 	
 		OutputManager.writeToFile("Loading challenge...");
 		cp.parse(challengeFile);
 		OutputManager.writeToFile("Parsing services...");
 		availableActions = ServiceParser.parse(serviceFile); 
 		
 		for (DataType t : cp.getInputs())
 			initialState.add(t);
 		for (DataType t : cp.getOuputs())
 			goalState.add(t);
 		
 		
 		// Ask the planner to find a way from the initial state to the goal state
 		ForwardChainReasoningPlanner planner = new ForwardChainReasoningPlanner();
 
 		OutputManager.outputStartInfo(initialState, goalState);
 		
 		int maxDepth = 1;
 		
 		HashSet<Long> solutionHashes = new HashSet<Long>();
 		
 		int solutionsWanted = slnWanted;
 		int parallelFactor = prl;
 		int solutionsFound = 0;
 		
 		while (solutionHashes.size() < solutionsWanted)
 		{
 			
 			ArrayList<Action> route = null;
 			while (route == null && maxDepth <= Math.min(256, availableActions.size()+1))
 			{
 				if (!ForwardChainReasoningPlanner.active)
 				{
 					OutputManager.writeToFile("Search stopped by the user.");
 					return;
 				}
 				
 				OutputManager.writeToFile("Attempting search with maxDepth=" + maxDepth + "\n");
 				route = planner.plan(initialState, goalState, availableActions, 1, maxDepth, solutionHashes, 0, parallelFactor);
 			
 				if (null == route)
 				{
 					OutputManager.writeToFile("Solution not possible with depth " + maxDepth + "\n");
 					maxDepth += 1;
 				}
 			}
 			
 			if (null == route)
 			{
 				OutputManager.writeToFile("Solution not found. Please check your requirements or increase the depth limit.");
 				return;
 			}
 			
 			long lastSolutionHash = ForwardChainReasoningPlanner.getSolutionHash(route, parallelFactor);
 			solutionHashes.add(lastSolutionHash);
 			
 			// Output the results:
 			StringBuilder sln = new StringBuilder();
 			sln.append("\nSolution #" + (++solutionsFound) + " found with maxDepth=" + maxDepth + ": \n");
 			for (int iter = 0; iter < route.size(); ++iter)
 			{
 				sln.append(route.get(iter).toString());
 				if (route.size()-1 == iter)
 					sln.append(".");
 				else
 					sln.append(", ");
 					
 			}
 			OutputManager.writeToFile(sln.toString());
 		}		
 	}
 
 }
