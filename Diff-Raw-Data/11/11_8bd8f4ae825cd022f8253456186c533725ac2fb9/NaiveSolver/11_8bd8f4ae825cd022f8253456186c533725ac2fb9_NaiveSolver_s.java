 package solvers;
 
 import main.Graph;
 import main.Tour;
 
 
 /**
  * // TODO: NaiveSolver is a ...
  * 
  * @author Martin Nycander
  * @since 
  */
 public class NaiveSolver implements StartApproxer
 {
 
 	/**
 	 * 
 	 */
 	public NaiveSolver()
 	{
 		// TODO Auto-generated constructor stub
 	}
 
 	/* (non-Javadoc)
 	 * @see Solver#getSolution(Graph)
 	 */
 	public Tour getTour(Graph graph)
 	{
 		Tour tour = new Tour();
 		boolean[] used = new boolean[graph.countNodes()];
 		used[0] = true;
 		int previous = 0;
 		for (int i = 1; i < graph.countNodes(); i++)
 		{
 			int best = -1;
 			for (int j = 0; j < graph.countNodes(); j++)
 			{
 				if (used[j])
 					continue;
 
 				if (best == -1 || graph.distance(previous, j) < graph.distance(previous, best))
 				{
 					best = j;
 				}
 			}
 
 			tour.addEdge(graph.getEdge(previous, best));
 			used[best] = true;
 			previous = best;
 		}
 		return tour;
 	}
 
 }
