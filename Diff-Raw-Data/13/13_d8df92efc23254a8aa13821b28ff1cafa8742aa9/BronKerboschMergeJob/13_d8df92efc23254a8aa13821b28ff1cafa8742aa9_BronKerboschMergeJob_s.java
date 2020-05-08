 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
  * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  * 
  * @author dmyersturnbull
  */
 package org.structnetalign.merge;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NavigableMap;
 import java.util.NavigableSet;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.concurrent.Callable;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.structnetalign.CleverGraph;
 import org.structnetalign.HomologyEdge;
 import org.structnetalign.util.NetworkUtils;
 
 public class BronKerboschMergeJob implements Callable<List<NavigableSet<Integer>>> {
 
 	private static final Logger logger = LogManager.getLogger("org.structnetalign");
 
 	private CleverGraph graph;
 
 	private int index;
 
 	private static String hashVertexInteractions(Collection<Integer> vertexInteractionNeighbors) {
 		return NetworkUtils.hash(vertexInteractionNeighbors);
 	}
 
 	public BronKerboschMergeJob(CleverGraph graph, int index) {
 		super();
 		this.graph = graph;
 		this.index = index;
 	}
 
 	@Override
 	public List<NavigableSet<Integer>> call() throws Exception {
 
 		logger.info("Searching for cliques on job " + index + " containing " + graph.getVertexCount()
 				+ " vertices and " + graph.getHomologyCount() + " homology edges");
 
 		// find the cliques
 		BronKerboschCliqueFinder<Integer, HomologyEdge> finder = new BronKerboschCliqueFinder<>();
 
 		// these cliques are ordered from largest to smallest
 		Collection<Set<Integer>> cliques = finder.transform(graph.getHomology());
 
 		// just report the cliques we're using
 		logger.info("Job " + index + ": " + "Found " + cliques.size() + " maximal cliques");
 		int i = 1;
 		for (Set<Integer> clique : cliques) {
 			logger.debug("Job " + index + ": " + "Clique " + i + ": " + clique);
 			i++;
 		}
 
 		// partition the cliques by sets of interactions
 		// we call these (maximal) degenerate sets
		NavigableMap<String, NavigableSet<Integer>> degenerateSetMap = new TreeMap<>();
 		for (Set<Integer> clique : cliques) {
 			for (int v : clique) {
 				Collection<Integer> neighbors = graph.getInteractionNeighbors(v);
 				String hash = hashVertexInteractions(neighbors);
 				NavigableSet<Integer> degenerateSet = degenerateSetMap.get(hash);
 				if (degenerateSet == null) {
 					degenerateSet = new TreeSet<>();
 					degenerateSetMap.put(hash, degenerateSet);
 				}
 				degenerateSet.add(v);
 				logger.trace("Job " + index + ": " + "Found " + hash + " --> " + degenerateSetMap.get(hash));
 			}
 		}
 
 		/*
 		 * Now sort the degenerate sets from largest to smallest.
 		 * Take into account the edge case where the sizes are the same.
 		 */
 		Comparator<NavigableSet<Integer>> comparator = new Comparator<NavigableSet<Integer>>() {
 			@Override
 			public int compare(NavigableSet<Integer> clique1, NavigableSet<Integer> clique2) {
 				if (CollectionUtils.isEqualCollection(clique1, clique2)) return 0;
 				if (clique1.size() < clique2.size()) {
 					return 1;
 				} else if (clique1.size() > clique2.size()) {
 					return -1;
 				} else {
 					Iterator<Integer> iter1 = clique1.iterator();
 					Iterator<Integer> iter2 = clique2.iterator();
 					while (iter1.hasNext()) { // we know they're the same size
 						int v1 = iter1.next();
 						int v2 = iter2.next();
 						if (v1 < v2) {
 							return 1;
 						} else if (v1 > v2) {
 							return -1;
 						}
 					}
 				}
 				// they're the same throughout, so they're equal
 				return 0;
 			}
 		};
		List<NavigableSet<Integer>> sortedDegenerateSets = new ArrayList<>(degenerateSetMap.values().size());
		sortedDegenerateSets.addAll(degenerateSetMap.values());
 		Collections.sort(sortedDegenerateSets, comparator);
 
 		/*
 		 * Now we want to return only the maximal maximal degenerate sets.
 		 */
 
 		TreeSet<String> verticesAlreadyUsed = new TreeSet<String>();
 
 		List<NavigableSet<Integer>> finalDegenerateSets = new ArrayList<>(sortedDegenerateSets.size());
 
 		int nTrivial = 0;
 		int nWeak = 0; // a degenerate set is weak if it contains a vertex that is added first
 
 		forcliques: for (NavigableSet<Integer> set : sortedDegenerateSets) {
 
 			// discard trivial degenerate sets
 			if (set.size() < 2) {
 				nTrivial++;
 				continue;
 			}
 
 			// verify that we haven't already used any vertex in this degenerate set
 			for (int v : set) {
 				String hash = NetworkUtils.hash(v); // use MD5 for safety
 				if (verticesAlreadyUsed.contains(hash)) {
 					// discard this degenerate set and do NOT say we've used any of these vertices
 					nWeak++;
 					continue forcliques;
 				}
 			}
 
 			// we haven't used any vertex in this degenerate set
 			// now add all of these vertices
 			// do NOT add before, or we'll add vertices we haven't used yet
 			for (int v : set) {
 				String hash = NetworkUtils.hash(v);
 				verticesAlreadyUsed.add(hash);
 			}
 			finalDegenerateSets.add(set); // keep this degenerate set
 		}
 
 		logger.info("Job " + index + ": " + "Found " + finalDegenerateSets.size()
 				+ " strong nontrivial maximal degenerate sets found (" + nTrivial + " trivial and " + nWeak + " weak)");
 
 		return finalDegenerateSets;
 	}
 
 }
