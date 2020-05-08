 /*
  * Copyright (C) 2011 by Olivier Chafik (http://ochafik.com)
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package com.ochafik.math.bayes;
 
 import static com.ochafik.math.functions.Functions.*;
 
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.Map.Entry;
 
 import com.ochafik.math.functions.Function;
 import com.ochafik.math.functions.FunctionException;
 import com.ochafik.math.functions.Functions;
 import com.ochafik.math.functions.Variable;
 import com.ochafik.math.graph.Clique;
 import com.ochafik.math.graph.Graph;
 import com.ochafik.math.graph.GraphUtils;
 import com.ochafik.math.graph.NodeSet;
 import com.ochafik.util.CollectionAdapter;
 import com.ochafik.util.DefaultAdapter;
 import com.ochafik.util.listenable.Pair;
 import com.ochafik.util.string.StringUtils;
 
 
 public class JunctionTreeAlgorithmUtils {
 	@SuppressWarnings("serial")
 	static class SeparatorsPotentials extends TreeMap<Pair<Integer, Integer>, Function<Variable>>{
 		public Function<Variable> get(int iClique1, int iClique2) {
 			int smallest = iClique1 < iClique2 ? iClique1 : iClique2;
 			int biggest = iClique1 > iClique2 ? iClique1 : iClique2;
 			
 			return get(new Pair<Integer, Integer>(smallest, biggest));
 		}
 		public Function<Variable> put(int iClique1, int iClique2, Function<Variable> f) {
 			int smallest = iClique1 < iClique2 ? iClique1 : iClique2;
 			int biggest = iClique1 > iClique2 ? iClique1 : iClique2;
 			
 			return put(new Pair<Integer, Integer>(smallest, biggest), f);
 		}
 	};
 	private static final void passMessage(int iSource, int iDestination, List<NodeSet<Variable>> nodeSetList, Map<Integer, Function<Variable>> cliquePotentials, SeparatorsPotentials separatorsPotentials) throws FunctionException {
 		//System.out.println("Message\n\t" + nodeSetList.get(iSource) + "\n->\t" + nodeSetList.get(iDestination));
 		
 		Function<Variable> oldSepPotential = separatorsPotentials.get(iSource, iDestination);
 		
 		Collection<Variable> sourceNodes = nodeSetList.get(iSource).getNodes();
 		Collection<Variable> destNodes = nodeSetList.get(iDestination).getNodes();		
 		
 		Set<Variable> varsToMarginalizeOut = new TreeSet<Variable>(sourceNodes);
 		varsToMarginalizeOut.removeAll(destNodes);
 		
 		Function<Variable> sourcePotential = cliquePotentials.get(iSource);
 		Function<Variable> newSepPotential = Functions.cache(Functions.marginalize(sourcePotential, varsToMarginalizeOut));
 		
 		// TODO check this !!!
 		separatorsPotentials.put(iSource, iDestination, newSepPotential);
 		//separatorsPotentials.put(iSource, iDestination, multiply(constant(1/2D	), add(newSepPotential, separatorsPotentials.get(iSource, iDestination))));
 		
 		Function<Variable> oldDestPotential = cliquePotentials.get(iDestination);
 		
 		Function<Variable> ratioSep = oldSepPotential == null ? newSepPotential : multiply(newSepPotential, invert(oldSepPotential));
 		
 		Function<Variable> newDestPotential = oldDestPotential == null ? ratioSep : multiply(oldDestPotential, ratioSep);
 		//System.out.println(oldDestPotential + " * " + newSepPotential + " / " + oldSepPotential + " = " + newDestPotential);
 		cliquePotentials.put(iDestination, Functions.cache(newDestPotential));
 	}
 	private static final void collectEvidence(int iSource, int iCaller, boolean[] markedCliques, List<NodeSet<Variable>> nodeSetList, Map<Integer,Set<Integer>> cliquesNeighbours, Map<Integer, Function<Variable>> cliquePotentials, SeparatorsPotentials separatorsPotentials) throws FunctionException {
 		markedCliques[iSource] = true;
 		for (int iNeighbour : cliquesNeighbours.get(iSource)) {
 			if (!markedCliques[iNeighbour]) {
 				collectEvidence(iNeighbour, iSource, markedCliques, nodeSetList, cliquesNeighbours, cliquePotentials, separatorsPotentials);
 			}
 		}
 		if (iCaller >= 0) {
 			passMessage(iSource, iCaller, nodeSetList, cliquePotentials, separatorsPotentials);
 		}
 	}
 	private static final void distributeEvidence(int iSource, boolean[] markedCliques, List<NodeSet<Variable>> nodeSetList, Map<Integer,Set<Integer>> cliquesNeighbours, Map<Integer, Function<Variable>> cliquePotentials, SeparatorsPotentials separatorsPotentials) throws FunctionException {
 		markedCliques[iSource] = true;
 		for (int iNeighbour : cliquesNeighbours.get(iSource)) {
 			if (!markedCliques[iNeighbour]) {
 				passMessage(iSource, iNeighbour, nodeSetList, cliquePotentials, separatorsPotentials);
 			}
 		}
 		for (int iNeighbour : cliquesNeighbours.get(iSource)) {
 			if (!markedCliques[iNeighbour]) {
 				distributeEvidence(iNeighbour, markedCliques, nodeSetList, cliquesNeighbours, cliquePotentials, separatorsPotentials);
 			}
 		}
 	}
 
 	private static final void printCliquePotentials(String title, final List<NodeSet<Variable>> nodeSetList, Map<Integer, Function<Variable>> cliquePotentials) {
 		PrintStream out = System.out;
 		out.println(title+" : \n\t"+
 			StringUtils.implode(
 				new CollectionAdapter<Map.Entry<Integer,Function<Variable>>, String>(
 					cliquePotentials.entrySet(),
 					new DefaultAdapter<Map.Entry<Integer,Function<Variable>>, String>() {
 						public String adapt(Entry<Integer, Function<Variable>> value) {
 							return nodeSetList.get(value.getKey())+" = \n\t\t"+value.getValue();
 						}
 					}
 				), 
 				"\n\t"
 			)
 		);
 	}
 	private static final void globalPropagation(int iSource, List<NodeSet<Variable>> nodeSetList, Map<Integer,Set<Integer>> cliquesNeighbours, Map<Integer, Function<Variable>> cliquePotentials, SeparatorsPotentials separatorsPotentials) throws FunctionException {
 		boolean[] markedCliques = new boolean[nodeSetList.size()];
 		
 		//printCliquePotentials("Initial clique potentials", nodeSetList, cliquePotentials);
 		
 		//System.out.println("Collecting evidence");
 		
 		collectEvidence(iSource, -1, markedCliques, nodeSetList, cliquesNeighbours, cliquePotentials, separatorsPotentials);
 		//printCliquePotentials("Clique potentials after evidence collection", nodeSetList, cliquePotentials);
 		
 		Arrays.fill(markedCliques, false);
 		
 		//System.out.println("Distributing evidence");
 		distributeEvidence(iSource, markedCliques, nodeSetList, cliquesNeighbours, cliquePotentials, separatorsPotentials);
 		//printCliquePotentials("Clique potentials after evidence distribution", nodeSetList, cliquePotentials);
 		
 	}
 	
 	public static final Map<Variable, List<Function<Variable>>> junctionTreeInference(Graph<Variable> graph, Map<Variable, ? extends Function<Variable>> fusionedDefinitions) throws FunctionException {
 		Graph<NodeSet<Variable>> junctionTree = GraphUtils.createJunctionTree(graph);
 		
 		Map<Integer, Function<Variable>> cliquePotentials = new TreeMap<Integer, Function<Variable>>();
 		
 		SeparatorsPotentials separatorsPotentials = new SeparatorsPotentials();
 		
 		// Cliques and separator have an uniform indexing which refers to nodeSetList
 		List<NodeSet<Variable>> nodeSetList = junctionTree.getNodeList();
 		
 		List<Integer> cliqueIndexList = new ArrayList<Integer>(nodeSetList.size());
 		for (int iNodeSet = nodeSetList.size(); iNodeSet-- != 0;) {
 			NodeSet<Variable> nodeSet = nodeSetList.get(iNodeSet);
 			if (nodeSet instanceof Clique) {
 				cliqueIndexList.add(iNodeSet);
 			}
 		}
 		
 		long startTime = System.currentTimeMillis();
 		System.out.print("Computing potentials formulae...");
 		
 		List<Variable> variableList = graph.getNodeList();
 		Set<Variable> 
 			assignedVariables = new HashSet<Variable>(),
 			unassignedVariables = new HashSet<Variable>(variableList);
 		
 		// INITIALIZATION
 		for (int iClique : cliqueIndexList) {
 			Function<Variable> product = null;
 			Clique<Variable> clique = (Clique<Variable>)nodeSetList.get(iClique);
 			
 			Collection<Variable> cliqueNodes = clique.getNodes();
 			for (Variable cliqueNode : cliqueNodes) {
 				if (!assignedVariables.contains(cliqueNode)) {
 					Function<Variable> cond = fusionedDefinitions.get(cliqueNode);
 					if (cliqueNodes.containsAll(cond.getArgumentNames())) {
 						//System.out.println("Variable " + cliqueNode + " = f" + cond.getArgumentNames() +" assigned to clique " + nodeSetList.get(iClique));
 						assignedVariables.add(cliqueNode);
 						unassignedVariables.remove(cliqueNode);
 						product = product == null ? cond : Functions.multiply(product, cond);
 					}
 				}
 				
 			}
 			cliquePotentials.put(iClique, product == null ? Functions.constant(1) : product);
 		}
 		
 		if (!unassignedVariables.isEmpty()) {
 			throw new RuntimeException("Failed to assign all variables conditional probabilities to a node ! (remaining "+unassignedVariables+")");
 			//new RuntimeException("Failed to assign all variables conditional probabilities to a node ! (remaining "+unassignedVariables+")").printStackTrace();
 		}
 		
 		// Build connectivity of cliques + initialize the separators potentials
 		Map<Integer,Set<Integer>> cliquesNeighbours = new TreeMap<Integer, Set<Integer>>();
 		for (int iClique : cliqueIndexList) {
 			Set<Integer> neighbourCliques = new TreeSet<Integer>();
 			for (int iNeighbourSeparator : junctionTree.getLocalConnectivity().getNeighbours(iClique).toArray()) {
 				for (int iNeighbourClique : junctionTree.getLocalConnectivity().getNeighbours(iNeighbourSeparator).toArray()) {
 					if (iNeighbourClique != iClique) {
 						neighbourCliques.add(iNeighbourClique);
 						//separatorsPotentials.put(iClique, iNeighbourClique, Functions.constant(1));
 					}
 				}
 			}
 			cliquesNeighbours.put(iClique, neighbourCliques);
 		}
 		
 		Integer startingClique = cliqueIndexList.get(cliqueIndexList.size() - 1);
 		//System.out.println("Starting global propagation by clique " + nodeSetList.get(startingClique));
 		globalPropagation(startingClique, nodeSetList, cliquesNeighbours, cliquePotentials, separatorsPotentials);
 		//globalPropagation(cliqueIndexList.get(0), nodeSetList, cliquesNeighbours, cliquePotentials, separatorsPotentials);
 		
 		// Normalize each clique's potential
 		for (Map.Entry<Integer, Function<Variable>> e : cliquePotentials.entrySet())
			cliquePotentials.put(e.getKey(), Functions.normalize(e.getValue(), 1));//, "{clique" + nodeSetList.get(e.getKey()) + " = f" + e.getValue().getArgumentNames()+"}"));
 		
 		
 		// Get all the potentials by clique / separator and marginalize them for each variable. Store the result by variable
 		Map<Variable, List<Function<Variable>>> ret = new TreeMap<Variable, List<Function<Variable>>>();
 		for (Variable v : graph.getNodeList()) {
 			ret.put(v, new ArrayList<Function<Variable>>());
 		}
 		for (Map.Entry<Integer, Function<Variable>> entry : cliquePotentials.entrySet()) {
 			int iClique = entry.getKey();
 			Function<Variable> cliquePotential = entry.getValue();
 			
 			Collection<Variable> cliqueNodes = ((Clique<Variable>)nodeSetList.get(iClique)).getNodes();
 			
 			// Marginalize cliquePotential for each variable of the clique
 			for (Variable v : cliquePotential.getArgumentNames()) {
 //				Set<Variable> parameters = new TreeSet<Variable>(cliquePotential.getArgumentNames());
 //				parameters.remove(v);
 //				//System.out.println(new TabulatedFunction<Variable>(cliquePotential).toString(Arrays.asList(v)));
 				ret.get(v).add(//Functions.normalize(
 						//Functions.marginalize(
 						//Functions.normalize(
 								cliquePotential//, 
 								//cliquePotential.getArgumentNames()
 						//), 
 						//parameters)
 						//, 1, null)
 						);
 			}
 		}
 		for (Map.Entry<Variable, List<Function<Variable>>> e : ret.entrySet()) {
 			List<Function<Variable>> list = e.getValue();
 			Collections.sort(list, new Comparator<Function<Variable>>() {
 
 				@Override
 				public int compare(Function<Variable> o1, Function<Variable> o2) {
 					if (o1 == o2)
 						return 0;
 					
 					int s1 = o1.getArgumentNames().size(), s2 = o2.getArgumentNames().size();
 					if (s1 != s2)
 						return s2 - s1;
 					int c = o1.getArgumentNames().toString().compareTo(o2.getArgumentNames().toString());
 					if (c != 0)
 						return c;
 					
 					// TODO Auto-generated method stub
 					return 1;
 				}
 				
 			});
 			for (int i = 0, len = list.size(); i < len; i++) {
 				Function<Variable> cliquePotential = list.get(i);
 				Set<Variable> parameters = new TreeSet<Variable>(cliquePotential.getArgumentNames());
 				parameters.remove(e.getKey());
 				list.set(i, Functions.marginalize(cliquePotential, parameters));
 			}
 				
 		}
 		
 		
 		System.out.println(" " +(System.currentTimeMillis() - startTime) + "ms.");
 		
 		return ret;
 	}
 }
