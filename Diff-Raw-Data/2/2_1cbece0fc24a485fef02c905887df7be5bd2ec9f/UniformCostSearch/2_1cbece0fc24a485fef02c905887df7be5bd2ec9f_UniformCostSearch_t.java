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
 public class UniformCostSearch extends Algorithm
 {
 	
 	private MinimumArrayList<HeuristicsVertex> frontier = new MinimumArrayList<HeuristicsVertex>();
 	private Map<HeuristicsVertex, HeuristicsVertex> movementMap = new HashMap<HeuristicsVertex, HeuristicsVertex>();
 	
 	public UniformCostSearch(Graph graph)
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
 		if(uniformCostSearch(startVertex))
 		{
 			return Collections.unmodifiableList(resultingWay);
 		}
 		return null;
 	}
 	
 	private boolean uniformCostSearch(Vertex startVertex)
 	{
 		HeuristicsVertex startHeuristicsVertex = (HeuristicsVertex)startVertex;
 		frontier.add((HeuristicsVertex)startVertex);
 		while(frontier.size()>0)
 		{
 			HeuristicsVertex currentVertex = frontier.min();
 			frontier.remove(currentVertex);
 			if (currentVertex.equals(endVertex))
 			{
 				Vertex walker = currentVertex;
 				do
 				{
 					resultingWay.add(walker);
 					walker = movementMap.get(walker);
 				}
 				while(movementMap.containsKey(walker));
 				resultingWay.add(walker);
 				Collections.reverse(resultingWay);
 				return true;
 			}
 			walkedVertices.add(currentVertex);
 			for (Vertex nextVertex : graph.getConnectedVertices(currentVertex))
 			{
				if (!walkedVertices.contains(nextVertex))
 				{
 					HeuristicsVertex nextHeuristicsVertex = (HeuristicsVertex)nextVertex;
 					int pathWaightToNextVertex = currentVertex.getHeuristics()+(int)graph.getWeightBetween(currentVertex, nextVertex);
 					if(!frontier.contains(nextHeuristicsVertex))
 					{
 						nextHeuristicsVertex.setHeuristics(pathWaightToNextVertex);
 						movementMap.put(nextHeuristicsVertex, currentVertex);
 						frontier.add(nextHeuristicsVertex);
 						walkedTrough.add(new StepData(graph.getConnectionBetween(currentVertex, nextVertex)));
 					}
 					else
 					{
 						if(nextHeuristicsVertex.getHeuristics()>pathWaightToNextVertex)
 						{
 							nextHeuristicsVertex.setHeuristics(pathWaightToNextVertex);
 							movementMap.put(nextHeuristicsVertex, currentVertex);
 							walkedTrough.add(new StepData(graph.getConnectionBetween(currentVertex, nextVertex)));
 						}
 					}
 				}
 			}
 			
 		}
 		return false;
 	}
 	
 	private class MinimumArrayList<E extends Comparable> extends ArrayList<E>
 	{
 		public E min()
 		{
 			E min = this.get(0);
 			for (E i : this)
 				if (i.compareTo(min) < 0)
 					min = i;
 			return min;
 		}
 	}
 }
