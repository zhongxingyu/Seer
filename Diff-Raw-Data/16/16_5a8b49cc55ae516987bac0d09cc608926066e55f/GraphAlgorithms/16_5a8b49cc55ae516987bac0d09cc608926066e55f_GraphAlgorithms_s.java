 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is S23M.
  *
  * The Initial Developer of the Original Code is
  * The S23M Foundation.
  * Portions created by the Initial Developer are
  * Copyright (C) 2012 The S23M Foundation.
  * All Rights Reserved.
  *
  * Contributor(s):
  * Andrew Shewring
  * ***** END LICENSE BLOCK ***** */
 package org.s23m.cell.communication.xml;
 
 import static org.s23m.cell.S23MKernel.coreSets;
 
import java.util.ArrayList;
 import java.util.List;
 
 import org.s23m.cell.Set;
 import org.s23m.cell.api.Query;
 import org.s23m.cell.platform.api.Instantiation;
 
 public class GraphAlgorithms {
 	
 	/**
 	 * Given a connected subgraph, produce an ordered set of vertices
 	 * in dependency order based on visibility arrows.
 	 *  
 	 * See http://en.wikipedia.org/wiki/Topological_sorting
 	 */
 	public static Set topologicalVisibilitySort(Set connectedSubgraph) {
 		if (connectedSubgraph.category().isEqualTo(Query.orderedSet)) {
 			// error
 			return Instantiation.raiseError(coreSets.semanticErr_operationIsIllegalOnThisInstance, coreSets.semanticErr);
 		} else {
 			// L <- Empty list that will contain the sorted elements
			// TODO how do we create an empty orderedSet?
			ArrayList<Set> L = new ArrayList<Set>();
 			
 			Set allEdges = connectedSubgraph.filterArrows();
 			
 			Set allNodes = connectedSubgraph.filter(Query.vertex);
 			Set nodesWithIncomingVisibilityEdges = allNodes.filterByLinkedToVia(Query.visibility);
 			
 			// S <- Set of all nodes with no incoming visibility edges
 			Set S = allNodes.complement(nodesWithIncomingVisibilityEdges);
 			
 			List<Set> listOfS = S.asList();
 
 			Set remainingEdges = allEdges;
 			
 			// while S is non-empty do
 			while (!listOfS.isEmpty()) {
 				// remove a node n from S
 				Set n = listOfS.remove(listOfS.size() - 1);
 				
 				// insert n into L
				L.add(n);
 				// TODO use union operation
 
 				// for each node m with an edge e from n to m do
 				for (Set edge : remainingEdges.filterByLinkedFrom(n)) {
 					// remove edge e from the graph
 					remainingEdges = remainingEdges.complement(edge);
 					
 					Set m = edge.to();
 					// Remove edge from n
 					n = n.complement(edge);
 					
 					// Remove edge from m
 					m = m.complement(edge);
 					
 					// if m has no other incoming edges then insert m into S
 					if (m.filterByLinkedToVia(Query.visibility).isEmpty()) {
 						listOfS.add(m);
 					}
 				}
 			}
 			
 			// TODO check to see if all edges are removed
 			/*
 			boolean cycle = false;
 			for (Vertex n : allNodes) {
 				if (!n.incomingEdges.isEmpty()) {
 					cycle = true;
 					break;
 				}
 			}
 			if (cycle) {
 				System.out.println("Cycle present, topological sort not possible");
 			} else {
 				System.out.println("Topological Sort: "
 						+ Arrays.toString(L.toArray()));
 			}
 			*/
			
			// TODO need to turn List<Set> into OrderedSet
			// return L;
			return null;
 		}
 	}
 	
 	/**
 	 * Partitions a model into independent parts, based on its visibility arrows.
 	 * 
 	 * This is equivalent to determining the set of weakly connected components in
 	 * a directed graph.
 	 * 
 	 * See http://mathworld.wolfram.com/WeaklyConnectedComponent.html
 	 * 
 	 * 1. filter all the vertices that don't have any visibilities attached
 	 *  (the content of these vertices can be serialised first). These are 
 	 *  the "independent vertices" iVertices
 	 *  [look at edge-ends]
 	 *  
 	 * 2. If the model is called M, define C as the complement of iVertices in M
 	 * 
 	 * 3. Analyse C and partition C into as many disconnected subgraphs of
 	 *  vertices (connected via visibilities) as possible. The result will be
 	 *  subsets C1, .... Cn, with n >= 1
 	 * 
 	 * 4. Each Ci above is a directed acyclic graph consisting of vertices and
 	 *  visibilities. We need to serialise the root first and then the next level down
 	 *  from the root.
 	 *  
 	 */
 	public static Set partitionModelByVisibilityArrows(Set model) {
 		Set allEdges = model.filterArrows();
 		Set allVertices = model.filter(Query.vertex);
 
 		Set nodesIncidentToVisibilityEdges = allVertices.filterByLinkedToVia(Query.visibility);
 		Set independentVertices = allVertices.complement(nodesIncidentToVisibilityEdges);
 		
 		return null;
 	}
 	
 }
 
