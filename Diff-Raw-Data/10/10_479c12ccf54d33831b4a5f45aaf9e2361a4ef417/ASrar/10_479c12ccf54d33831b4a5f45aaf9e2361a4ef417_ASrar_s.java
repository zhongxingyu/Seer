 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.unikernel.lnu.ai.agents;
 
 import java.util.*;
 import org.unikernel.lnu.ai.graph.Graph;
 import org.unikernel.lnu.ai.graph.HeuristicsVertex;
 import org.unikernel.lnu.ai.graph.Vertex;
 
 /**
  *
  * @author uko
  */
 public class ASrar extends Algorithm
 {
 	public ASrar(Graph graph)
 	{
 		super(graph);
 	}
 	
 	@Override
 	public List<Vertex> search()
 	{
 		if(startVertex == null || endVertex == null)
 		{
 			return null;
 		}
 		reset();
 		if(aStarSearch(startVertex, endVertex))
 		{
 			return Collections.unmodifiableList(resultingWay);
 		}
 		return null;
 	}
 	
 	
 	private	boolean aStarSearch(Vertex start, Vertex goal)
 	{   
 		Set<Vertex> openset = new HashSet<Vertex>();    // The set of tentative nodes to be evaluated, initially containing the start node
 		openset.add(start);
 		Map<Vertex,Vertex> cameFrom = new HashMap();    // The map of navigated nodes.
 
 		Map<Vertex, Integer> gScore = new HashMap<Vertex, Integer>();
 		gScore.put(start, new Integer(0));    // Cost from start along best known path.
 		Map<Vertex, Integer> hScore = new HashMap<Vertex, Integer>();
 		hScore.put(start, ((HeuristicsVertex)start).getHeuristics());
 		final Map<Vertex, Integer> fScore = new HashMap<Vertex, Integer>();
 		fScore.put(start, gScore.get(start)+hScore.get(start));    // Estimated total cost from start to goal through y.
 
 		while (openset.size()>0)
 		{
 		
 			HeuristicsVertex current = (HeuristicsVertex)Collections.min(openset,
 					new Comparator<Vertex>()
 					{
 						@Override
 						public int compare(Vertex entry1, Vertex entry2)
 						{
 							return fScore.get(entry1).compareTo(fScore.get(entry2));
 						}
 					});//the node in openset having the lowest f_score[] value
 			
 			if(current.equals(goal))
 				return true; //reconstruct_path(cameFrom, cameFrom[goal])
  
 			openset.remove(current);
 			walkedVertices.add(current);
 			for (Vertex neighbor : graph.getConnectedVertices(current))
 			{
 				boolean tentativeIsBetter;
 				if(!walkedVertices.contains(neighbor))
 				{
 					Integer tentativeGScore = gScore.get(current) + new Double(graph.getWeightBetween(current, neighbor)).intValue();
  
 					if(!openset.contains(neighbor))
 					{
 						openset.add(neighbor);
 						walkedTrough.add(new StepData(graph.getConnectionBetween(current, neighbor)));
 						hScore.put(neighbor, ((HeuristicsVertex)neighbor).getHeuristics());
 						tentativeIsBetter = true;
 					}
 					else
 						if(tentativeGScore < gScore.get(neighbor))
 							tentativeIsBetter = true;
 						else
 							tentativeIsBetter = false;
 
 					if(tentativeIsBetter)
 					{
 						cameFrom.put(neighbor, current);
 						gScore.put(neighbor, tentativeGScore);
 						fScore.put(neighbor, gScore.get(neighbor)+hScore.get(neighbor));
 					}
 				}
 			}
 		}
 		return false;
 	}
 }
