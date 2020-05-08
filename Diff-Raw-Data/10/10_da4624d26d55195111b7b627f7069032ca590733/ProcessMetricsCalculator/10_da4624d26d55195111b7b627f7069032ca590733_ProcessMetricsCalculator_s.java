 /**
  * PromniCAT - Collection and Analysis of Business Process Models
  * Copyright (C) 2012 Cindy Fähnrich, Tobias Hoppe, Andrina Mascher
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.uni_potsdam.hpi.bpt.promnicat.analysisModules.metrics;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 
 import org.jbpt.graph.abs.AbstractMultiGraphFragment;
 import org.jbpt.graph.algo.CombinationGenerator;
 import org.jbpt.graph.algo.GraphAlgorithms;
 import org.jbpt.graph.algo.TransitiveClosure;
 import org.jbpt.hypergraph.abs.IVertex;
 import org.jbpt.pm.Activity;
 import org.jbpt.pm.AlternativGateway;
 import org.jbpt.pm.AndGateway;
 import org.jbpt.pm.ControlFlow;
 import org.jbpt.pm.DataNode;
 import org.jbpt.pm.Event;
 import org.jbpt.pm.FlowNode;
 import org.jbpt.pm.Gateway;
 import org.jbpt.pm.OrGateway;
 import org.jbpt.pm.ProcessModel;
 import org.jbpt.pm.XorGateway;
 import org.jbpt.pm.bpmn.Bpmn;
 import org.jbpt.pm.bpmn.BpmnControlFlow;
 import org.jbpt.pm.bpmn.EndEvent;
 import org.jbpt.pm.bpmn.StartEvent;
 import org.jbpt.pm.bpmn.Subprocess;
 
 import de.uni_potsdam.hpi.bpt.promnicat.util.ProcessMetricConstants;
 import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.IUnitDataProcessMetrics;
 
 
 /**
  * This class provides some methods to calculate several process model metrics for a jBPT {@link ProcessModel}.
  * 
  * @author Tobias Hoppe
  * 
  */
 public class ProcessMetricsCalculator {
 
 	/**
 	 * Calculates all available process metrics for the {@link ProcessModel} given 
 	 * by the {@link IUnitDataProcessMetrics} and sets
 	 * all metric values in the given {@link IUnitDataProcessMetrics}.
 	 * @param unitData the {@link IUnitDataProcessMetrics} to use for calculation
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 */
 	public void calculateAllProcessMetrics(IUnitDataProcessMetrics<Object> unitData, boolean includeSubProcesses) {
 		if (!(unitData.getValue() instanceof ProcessModel)) {
 			throw new IllegalArgumentException("The given UnitData value must be an instance of a jBPT ProcessModel!");
 		}
 		ProcessModel model = (ProcessModel) unitData.getValue();
 		//calculate each metric and add the result to the corresponding unitData field
 		unitData.setNumberOfNodes(this.getNumberOfNodes(model, includeSubProcesses));
 		unitData.setNumberOfEdges(this.getNumberOfEdges(model, includeSubProcesses));
 		unitData.setNumberOfActivities(this.getNumberOfElementsFromClass(model, Activity.class, includeSubProcesses));
 		unitData.setNumberOfAndJoins(this.getNumberOfAndJoins(model, includeSubProcesses));
 		unitData.setNumberOfAndSplits(this.getNumberOfAndSplits(model, includeSubProcesses));
 		unitData.setNumberOfDataNodes(this.getNumberOfDataNodes(model, includeSubProcesses));
 		unitData.setNumberOfEndEvents(this.getNumberOfEndEvents(model, includeSubProcesses));
 		unitData.setNumberOfEvents(this.getNumberOfElementsFromClass(model, Event.class, includeSubProcesses));
 		unitData.setNumberOfGateways(this.getNumberOfGateways(model, includeSubProcesses));
 		unitData.setNumberOfInternalEvents(this.getNumberOfInternalEvents(model, includeSubProcesses));
 		unitData.setNumberOfOrJoins(this.getNumberOfOrJoins(model, includeSubProcesses));
 		unitData.setNumberOfOrSplits(this.getNumberOfOrSplits(model, includeSubProcesses));
 		unitData.setNumberOfRoles(this.getNumberOfRoles(model, includeSubProcesses));
 		unitData.setNumberOfStartEvents(this.getNumberOfStartEvents(model, includeSubProcesses));
 		unitData.setNumberOfXorJoins(this.getNumberOfXorJoins(model, includeSubProcesses));
 		unitData.setNumberOfXorSplits(this.getNumberOfXorSplits(model, includeSubProcesses));
 		unitData.setAverageConnectorDegree(this.getAverageConnectorDegree(model, includeSubProcesses));
 		unitData.setCoefficientOfConnectivity(this.getCoefficientOfConnectivity(model, includeSubProcesses));
 		unitData.setCoefficientOfNetworkComplexity(this.getCoefficientOfNetworkComplexity(model, includeSubProcesses));
 		unitData.setControlFlowComplexity(this.getControlFlowComplexity(model, includeSubProcesses));
 		unitData.setCrossConnectivity(this.getCrossConnectivity(model, includeSubProcesses));
 		unitData.setCycling(this.getCycling(model, includeSubProcesses));
 		unitData.setCyclomaticNumber(this.getCyclomaticNumber(model, includeSubProcesses));
 		unitData.setSeparability(this.getSeparability(model, includeSubProcesses));
 		unitData.setDensity(this.getDensity(model, includeSubProcesses));
 		unitData.setDensityRelatedToNumberOfGateways(this.getDensityRelatedToNumberOfGateways(model, includeSubProcesses));
 		unitData.setDepth(this.getDepth(model, includeSubProcesses));
 		unitData.setDiameter(this.getDiameter(model, includeSubProcesses));
 		unitData.setMaxConnectorDegree(this.getMaxConnectorDegree(model, includeSubProcesses));
 	}
 
 	/**
 	 * Calculates the given metric for the {@link ProcessModel} given by the {@link IUnitDataProcessMetrics} and sets
 	 * the metric value in the given {@link IUnitDataProcessMetrics}.
 	 * @param metricToCalculate the metric to calculate
 	 * @param unitData the {@link IUnitDataProcessMetrics} to use for calculation
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 */
 	public void calculateProcessMetric(ProcessMetricConstants.METRICS metricToCalculate, IUnitDataProcessMetrics<Object> unitData, boolean includeSubProcesses) {
 		metricToCalculate.calculateAttribute(unitData, includeSubProcesses);
 	}
 
 	/**
 	 * Calculates the given metrics for the {@link ProcessModel} given by the {@link IUnitDataProcessMetrics} and sets
 	 * the metric values in the given {@link IUnitDataProcessMetrics}.
 	 * @param metricsToCalculate a list of the metrics to calculate
 	 * @param unitData the {@link IUnitDataProcessMetrics} to use for calculation
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 */
 	public void calculateProcessMetrics(Collection<ProcessMetricConstants.METRICS> metricsToCalculate, IUnitDataProcessMetrics<Object> unitData, boolean includeSubProcesses) {
 		for (ProcessMetricConstants.METRICS metric : metricsToCalculate) {
 			calculateProcessMetric(metric, unitData, includeSubProcesses);
 		}
 	}
 
 	/**
 	 * The Average Connector Degree relates the number of incoming and outgoing edges of 
 	 * all {@link Gateway}s to the total number of {@link Gateway}s.<br/>
 	 * If the given {@link ProcessModel} does not contain any {@link Gateway}, the
 	 * Average Connector Degree is zero.
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the average connector degree of the given {@link ProcessModel}.
 	 */
 	public double getAverageConnectorDegree(ProcessModel model, boolean includeSubProcesses) {
 		Set<FlowNode> gateways = new HashSet<FlowNode>(this.getGateways(model, includeSubProcesses));
 		if (gateways.isEmpty()) {
 			return 0.0;
 		}
 		int gatewayEdges = 0;
 		for (FlowNode node : gateways) {
 			gatewayEdges += model.getEdges(node).size();
 		}
 		return gatewayEdges / (double) gateways.size();
 	}
 
 	/**
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the coefficient of connectivity of the given {@link ProcessModel}.
 	 */
 	public double getCoefficientOfConnectivity(ProcessModel model, boolean includeSubProcesses) {
 		int numberOfNodes = this.getNumberOfNodes(model, includeSubProcesses);
 		return numberOfNodes > 0 ? this.getNumberOfEdges(model, includeSubProcesses) / (double)numberOfNodes : 0;
 	}
 
 	/**
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the coefficient of network complexity of the given {@link ProcessModel}.
 	 */
 	public double getCoefficientOfNetworkComplexity(ProcessModel model, boolean includeSubProcesses) {
 		int numberOfEdges = this.getNumberOfEdges(model, includeSubProcesses);
 		int numberOfNodes = this.getNumberOfNodes(model, includeSubProcesses);
 		return numberOfNodes > 0 ? (numberOfEdges * numberOfEdges) / (double)numberOfNodes : 0;
 	}
 
 	/**
 	 * Calculates the sum of all split {@link Gateway}s weighted by the number of possible
 	 * states after the split.
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the control flow complexity of the given {@link ProcessModel}.
 	 */
 	public int getControlFlowComplexity(ProcessModel model, boolean includeSubProcesses) {		
 		int ccOfAnd = this.getNumberOfAndSplits(model, includeSubProcesses);
 		int ccOfXor = 0;
 		for (FlowNode xorSplit : this.getXorSplits(model, includeSubProcesses)) {
 			ccOfXor += model.getOutgoingControlFlow(xorSplit).size();
 		}		
 		int ccOfOrAndAlternative = 0;
 		Collection<FlowNode> orAndAlternativeSplits = this.getOrSplits(model, includeSubProcesses);
 		orAndAlternativeSplits.addAll(this.getAlternativeSplits(model, includeSubProcesses));
 		for (FlowNode split : orAndAlternativeSplits) {
 			ccOfOrAndAlternative += Math.pow(model.getOutgoingControlFlow(split).size(), 2) - 1;
 		}
 		return ccOfAnd + ccOfXor + ccOfOrAndAlternative;
 	}
 
 	/**
 	 * Calculates the average strength of connection between
 	 * all pairs of {@link FlowNode}s of the given {@link ProcessModel}.
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the cross connectivity of the given {@link ProcessModel}.
 	 */
 	public double getCrossConnectivity(ProcessModel model, boolean includeSubProcesses) {
 		//models with less than two nodes have a cross-connectivity of zero
 		if (model.getVertices().size() < 2) {
 			return 0.0;
 		}
 		//get node weights
 		Map<FlowNode, Double> nodeWeights = getNodeWeights(model);
 		//get arc weights
 		Map<ControlFlow<FlowNode>, Double> edgeWeights = getEdgeWeights(model, nodeWeights);
 		Collection<FlowNode> nodes = this.getNodes(model, includeSubProcesses);
 		//sum up connections
 		double connectionValues = this.getConnectionValues(model, edgeWeights);
 		//handle sub processes if needed
 		if (includeSubProcesses){
 			for (FlowNode node : nodes) {
 				if (node instanceof Subprocess) {
 					Bpmn<BpmnControlFlow<FlowNode>, FlowNode> subProcess = ((Subprocess) node).getSubProcess();
 					//get node weights
 					nodeWeights = getNodeWeights(subProcess);
 					//get arc weights
 					edgeWeights = getEdgeWeights(subProcess, nodeWeights);
 					connectionValues += this.getNodesOnCylce(subProcess, includeSubProcesses);
 				}
 			}
 		}
 		return connectionValues / (nodes.size() * nodes.size() - 1);
 	}
 
 	/**
 	 * @param model to analyze
 	 * @param edgeWeights mapping for each {@link ControlFlow} edge of the given {@link ProcessModel} to its corresponding weigth
 	 * @return the sum of all connection values of the given {@link ProcessModel}
 	 */
 	private double getConnectionValues(ProcessModel model, Map<ControlFlow<FlowNode>, Double> edgeWeights) {
 		double connectionValues = 0.0;
 		Collection<FlowNode> nodes = model.getVertices();
 		TransitiveClosure<ControlFlow<FlowNode>, FlowNode> transitiveClosure = new TransitiveClosure<ControlFlow<FlowNode>, FlowNode>(model);
 		for(FlowNode node1 : nodes) {
 			for(FlowNode node2 : nodes){
 				if (node1 == node2){
 					continue;
 				}
 				if (transitiveClosure.hasPath(node1, node2)) {
 					//get all paths from start to end
 					Vector<Vector<FlowNode>> paths = getAllPaths(node1, node2, transitiveClosure, new Vector<FlowNode>(), new Vector<Vector<FlowNode>>());
 					double maxPathValue = 0.0;
 					//calculate maximum value of connection from all paths
 					for (Vector<FlowNode> path : paths) {
 						double pathValue = 1.0;
 						for (int i = 0; i < path.size() - 1; i++) {
 							pathValue *= edgeWeights.get(model.getEdgesWithSourceAndTarget(path.get(i), path.get(i + 1)).iterator().next());
 						}
 						maxPathValue = Math.max(maxPathValue, pathValue);
 					}
 					connectionValues += maxPathValue;
 				}
 			}
 		}
 		return connectionValues;
 	}
 
 	/**
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the the number of nodes on a loop of the given {@link ProcessModel}.
 	 */
 	private int getNodesOnCylce(ProcessModel model, boolean includeSubProcesses) {
 		Collection<FlowNode> nodes = model.getVertices();
 		if (nodes.isEmpty()) {
 			return 0;
 		}
 		int nodesOnCycle = 0;
 		TransitiveClosure<ControlFlow<FlowNode>, FlowNode> transitivClosure = new TransitiveClosure<ControlFlow<FlowNode>, FlowNode>(model);
 		for (FlowNode node : nodes){
 			if (transitivClosure.isInLoop(node)) {
 				nodesOnCycle++;
 			}
 		}
 		int result = nodesOnCycle;
 		//handle sub processes if needed
 		if (includeSubProcesses){
 			for (FlowNode node : nodes) {
 				if (node instanceof Subprocess) {
 					result += this.getNodesOnCylce(((Subprocess) node).getSubProcess(), includeSubProcesses);
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return list of {@link FlowNode}s of the given {@link ProcessModel} including the {@link FlowNode}s
 	 * of {@link Subprocess}s if includeSubProcesses is <code>true</code>.
 	 */
 	private Collection<FlowNode> getNodes(ProcessModel model, boolean includeSubProcesses) {
 		Collection<FlowNode> result = model.getVertices();
 		//handle sub processes if needed
 		if (includeSubProcesses){
 			for (FlowNode node : model.getVertices()) {
 				if (node instanceof Subprocess) {
 					result.addAll(this.getNodes(((Subprocess) node).getSubProcess(), includeSubProcesses));
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Relates the number of nodes on a loop of the given {@link ProcessModel} 
 	 * to the total number of nodes of the given {@link ProcessModel}.
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the cycling of the given {@link ProcessModel}.
 	 */
 	public double getCycling(ProcessModel model, boolean includeSubProcesses) {
 		Collection<FlowNode> nodes = this.getNodes(model, includeSubProcesses);
 		int nodesCount = nodes.size();
 		if (nodesCount == 0) {
 			return 0;
 		}
 		return (double) this.getNodesOnCylce(model, includeSubProcesses) / (double) nodesCount;
 	}
 
 	/**
 	 * TODO verify calculation of this metric
 	 * Calculates the number of execution paths by summing up the possible states 
 	 * after each splitting Gateway.
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the cyclomatic number of the given {@link ProcessModel}.
 	 */
 	public int getCyclomaticNumber(ProcessModel model, boolean includeSubProcesses) {
 		Collection<FlowNode> orAndAlternativeSplits = this.getOrSplits(model, false);
 		orAndAlternativeSplits.addAll(this.getAlternativeSplits(model, false));
 		Map<FlowNode, Integer> numberOfTokens = numberOfTokens(model, orAndAlternativeSplits);
 
 		int cyclomaticNumber = this.getNumberOfModelParts(model, false);
 		//add number of outgoing edges minus one for each xor split
 		for (FlowNode node : this.getXorSplits(model, false)) {
 			cyclomaticNumber += numberOfTokens.get(node) * (model.getOutgoingControlFlow(node).size() - 1);
 		}
 		//add number of all possible combination of outgoing edges minus one
 		//		for each alternative split and each or split
 		for (FlowNode node : orAndAlternativeSplits) {
 			cyclomaticNumber += numberOfTokens.get(node) * (2^model.getOutgoingControlFlow(node).size() - 2);
 		}
 		int result = cyclomaticNumber;
 		//handle sub processes if needed
 		if (includeSubProcesses){
 			for (FlowNode node : model.getVertices()) {
 				if (node instanceof Subprocess) {
 					result += this.getCyclomaticNumber(((Subprocess) node).getSubProcess(), includeSubProcesses);
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * The density is the ratio of edges to the maximum number of edges for the same number of nodes.
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the density of the given {@link ProcessModel}.
 	 */
 	public double getDensity(ProcessModel model, boolean includeSubProcesses) {
 		int numberOfNodes = this.getNumberOfNodes(model, includeSubProcesses);
 		if (numberOfNodes <= 1) {
 			return 0.0;
 		}
 		return this.getNumberOfEdges(model, includeSubProcesses) / (double) (numberOfNodes * (numberOfNodes - 1));
 	}
 
 	/**
 	 * The density is calculated depending on the number of {@link Gateway}s of the given {@link ProcessModel}.
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the density of the given {@link ProcessModel}.
 	 */
 	public double getDensityRelatedToNumberOfGateways(ProcessModel model, boolean includeSubProcesses) {
 		Set<FlowNode> gateways = new HashSet<FlowNode>(this.getGateways(model, includeSubProcesses));
 		int gatewayCount = gateways.size();
 		if (gatewayCount <= 1) {
 			return 0.0;
 		}
 		double gatewayWeight = 0.0;
 		if (gatewayCount % 2 == 0){
 			gatewayWeight = Math.pow(gatewayCount / 2.0 + 1, 2);
 		} else {
 			double intermediateResult = (gatewayCount - 1) / 2.0 + 1;
 			gatewayWeight = intermediateResult + Math.pow(intermediateResult, 2);
 		}
 		int minimumArcs = this.getNumberOfNodes(model, includeSubProcesses) - this.getNumberOfModelParts(model, includeSubProcesses);
 
 		double intermediateResult = gatewayWeight + 2 * (this.getNumberOfElementsFromClass(model, Activity.class, includeSubProcesses) 
 				+ this.getNumberOfElementsFromClass(model, Event.class, includeSubProcesses)) - minimumArcs;
 		if (intermediateResult == 0.0) {
 			return 0.0;
 		}
 		return (this.getNumberOfEdges(model, includeSubProcesses) - minimumArcs) / intermediateResult;
 	}
 
 	/**
 	 * Calculates the maximum depth of the given {@link ProcessModel}.
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the depth of the given {@link ProcessModel}.
 	 */
 	public int getDepth(ProcessModel model, boolean includeSubProcesses) {
 		TransitiveClosure<ControlFlow<FlowNode>, FlowNode> transitiveClosure = new TransitiveClosure<ControlFlow<FlowNode>, FlowNode>(model);
 		Collection<FlowNode> startNodes = model.getEntries();
 		Collection<FlowNode> endNodes = model.getExits();
 		Map<FlowNode, Integer> inDepths = new HashMap<FlowNode, Integer>(model.countVertices());
 		Map<FlowNode, Integer> outDepths = new HashMap<FlowNode, Integer>(model.countVertices());
 		//initialize in and out depth for each node
 		for (FlowNode node : model.getVertices()) {
 			inDepths.put(node, 0);
 			outDepths.put(node, 0);
 		}
 		//get in and out depth for each node on each possible path from a start node to an end node
 		for (FlowNode startNode : startNodes) {
 			for (FlowNode endNode : endNodes) {
 				Vector<Vector<FlowNode>> paths = getAllPaths(startNode, endNode, transitiveClosure, new ArrayList<FlowNode>(), new Vector<Vector<FlowNode>>());
 				for (Vector<FlowNode> path : paths) {
 					inDepths = calculateInDepthsFor(path, inDepths, model);
 					outDepths = calculateOutDepthsFor(path, outDepths, model);
 				}
 			}
 		}
 		int depth = 0;
 		for (FlowNode node : model.getVertices()) {
 			depth = Math.max(depth, Math.min(inDepths.get(node), outDepths.get(node)));
 		}
 		//handle sub processes if needed
 		if (includeSubProcesses){
 			for (FlowNode node : model.getVertices()) {
 				if (node instanceof Subprocess) {
 					depth = Math.max(depth, this.getDepth(((Subprocess) node).getSubProcess(), includeSubProcesses));
 				}
 			}
 		}
 		return depth;
 	}
 
 	/**
 	 * Calculates the maximum path length (number of edges)
 	 * from a start node, if one exists, otherwise from any node of the given {@link ProcessModel}
 	 * to an end node, if one exists, otherwise to any other node without visiting a node twice.
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the diameter of the given {@link ProcessModel}.
 	 */
 	public int getDiameter(ProcessModel model, boolean includeSubProcesses) {
 		int diameter = 0;
 		GraphAlgorithms<ControlFlow<FlowNode>, FlowNode> graphAlgorithms = new GraphAlgorithms<ControlFlow<FlowNode>, FlowNode>();
 		Collection<FlowNode> excludedNodes = new ArrayList<FlowNode>();
 		//add all single node model parts to exclusion list
 		for (FlowNode node : model.getVertices()) {
 			if (model.getEdges(node).isEmpty()){
 				excludedNodes.add(node);
 			}
 		}
 		ProcessModel extractedFragment = model;
 		//calculate maximum diameter of each independent model part
 		while (true) {
 			//get next model part
 			AbstractMultiGraphFragment<ControlFlow<FlowNode>, FlowNode> fragment = graphAlgorithms.getConnectedFragment(model, excludedNodes);
 			if (fragment.getVertices().isEmpty()){
 				break;
 			}
 			extractedFragment = getExtractedModelPart(model, fragment);
 			excludedNodes.addAll(extractedFragment.getVertices());
 
 			//calculate start and end nodes
 			Collection<FlowNode> startNodes = extractedFragment.getEntries();
 			if (startNodes.isEmpty()) {
 				startNodes = extractedFragment.getVertices();
 			}
 			Collection<FlowNode> endNodes = extractedFragment.getExits();
 
 			for (FlowNode startNode : startNodes) {
 				for (ControlFlow<FlowNode> edge : extractedFragment.getOutgoingControlFlow(startNode)) {
 					Collection<FlowNode> visitedNodes = new ArrayList<FlowNode>();
 					visitedNodes.add(startNode);
 					if (endNodes.isEmpty()) {
 						diameter = Math.max(diameter, longestPath(edge.getTarget(), 0, visitedNodes, false));
 					} else {
 						diameter = Math.max(diameter, longestPath(edge.getTarget(), 0, visitedNodes, true));
 					}
 				}
 			}		
 		}	
 		//handle sub processes if needed
 		if (includeSubProcesses){
 			for (FlowNode node : model.getVertices()) {
 				if (node instanceof Subprocess) {
 					diameter = Math.max(diameter, this.getDiameter(((Subprocess) node).getSubProcess(), includeSubProcesses));
 				}
 			}
 		}
 		return diameter;
 	}
 
 	/**
 	 * The Maximum Connector Degree is the maximum number of edges of a {@link Gateway}.<br/>
 	 * This metric is zero, if the given {@link ProcessModel} does not contain any {@link Gateway}s.
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the Maximum Connector Degree of the given {@link ProcessModel}.
 	 */
 	public int getMaxConnectorDegree(ProcessModel model, boolean includeSubProcesses) {
 		Set<FlowNode> gateways = new HashSet<FlowNode>(this.getGateways(model, includeSubProcesses));
 		if (gateways.isEmpty()) {
 			return 0;
 		}
 		int maxGatewayEdges = 0;
 		for (FlowNode node : gateways) {
 			int currentEdges = model.getEdges(node).size();
 			maxGatewayEdges = currentEdges > maxGatewayEdges ? currentEdges : maxGatewayEdges;
 		}
 		return maxGatewayEdges;
 	}
 
 	/**
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the number of {@link AndGateway}s joining the control flow of the given {@link ProcessModel}.
 	 * Thus, all {@link AndGateway} with several incoming edges.
 	 */
 	public int getNumberOfAndJoins(ProcessModel model, boolean includeSubProcesses) {
 		return this.getAndJoins(model, includeSubProcesses).size();
 	}
 
 	/**
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the number of {@link AndGateway}s splitting the control flow of the given {@link ProcessModel}.
 	 * Thus, all {@link AndGateway}s with several outgoing edges and all {@link Activity}s with multiple outgoing
 	 * unconditional edges.
 	 */
 	public int getNumberOfAndSplits(ProcessModel model, boolean includeSubProcesses) {
 		return this.getAndSplits(model, includeSubProcesses).size();
 	}
 
 	/**
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the number of {@link DataNode}s (e.g. documents) of the given {@link ProcessModel}.
 	 */
 	public int getNumberOfDataNodes(ProcessModel model, boolean includeSubProcesses) {
 		//FIXME return only connected ones?
 		int result = model.filter(DataNode.class).size();
 		//sub process handling if needed
 		if (includeSubProcesses) {
 			for (FlowNode node : model.getVertices()) {
 				if (node instanceof Subprocess) {
 					result += this.getNumberOfDataNodes(((Subprocess) node).getSubProcess(), includeSubProcesses);
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the number of edges (=arcs) of the given {@link ProcessModel}
 	 */
 	public int getNumberOfEdges(ProcessModel model, boolean includeSubProcesses) {
 		int result = model.countEdges();
 		//sub process handling if needed
 		if (includeSubProcesses) {
 			for (FlowNode node : model.getVertices()) {
 				if (node instanceof Subprocess) {
 					result += this.getNumberOfEdges(((Subprocess) node).getSubProcess(), includeSubProcesses);
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @param elementClass class of the model elements to extract
 	 * @return The number of elements from the given class 
 	 * and all of its sub-types in the given {@link ProcessModel}.
 	 */
 	public int getNumberOfElementsFromClass(ProcessModel model, Class<?> elementClass, boolean includeSubProcesses) {
 		int result = model.filter(elementClass).size();
 		//sub process handling if needed
 		if (includeSubProcesses) {
 			for (FlowNode node : model.getVertices()) {
 				if (node instanceof Subprocess) {
 					result += this.getNumberOfElementsFromClass(((Subprocess) node).getSubProcess(), elementClass, includeSubProcesses);
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the number of {@link EndEvent}s of the given {@link ProcessModel}.
 	 */
 	public int getNumberOfEndEvents(ProcessModel model, boolean includeSubProcesses) {
 		int result = model.filter(EndEvent.class).size();
 		//sub process handling if needed
 		if (includeSubProcesses) {
 			for (FlowNode node : model.getVertices()) {
 				if (node instanceof Subprocess) {
 					result += this.getNumberOfEndEvents(((Subprocess) node).getSubProcess(), includeSubProcesses);
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the number of {@link Gateway}s (AND-, OR- and XOR-Gateway as well as their sub-types)
 	 * of the given {@link ProcessModel}.
 	 */
 	public int getNumberOfGateways(ProcessModel model, boolean includeSubProcesses) {
 		return this.getGateways(model, includeSubProcesses).size();
 	}
 
 	/**
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the number of events of the given {@link ProcessModel} being neither start event nor end event.
 	 */
 	public int getNumberOfInternalEvents(ProcessModel model, boolean includeSubProcesses) {
 		int numberOfinternalEvents = this.getNumberOfElementsFromClass(model, Event.class, includeSubProcesses)
 				- this.getNumberOfEndEvents(model, includeSubProcesses)
 				- this.getNumberOfStartEvents(model, includeSubProcesses);
 		return numberOfinternalEvents < 0 ? 0 : numberOfinternalEvents;
 	}
 
 	/**
 	 * Calculates the number of not connected {@link ProcessModel} parts.
 	 * 
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the number of model parts of the given {@link ProcessModel}.
 	 */
 	public int getNumberOfModelParts(ProcessModel model, boolean includeSubProcesses) {
 		int modelParts = 0;
 
 		//add all nodes without at least one incoming or outgoing edge to the exclusion list,
 		// because these nodes are separate model parts
 		Collection<FlowNode> excludedNodes = getNodesWithoutEdges(model, includeSubProcesses);
 		modelParts += excludedNodes.size();
 		modelParts += getNumberOfModelParts(model, excludedNodes, includeSubProcesses);
 
 		return modelParts;
 	}
 
 	/**
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the number of nodes of the given {@link ProcessModel}
 	 */
 	public int getNumberOfNodes(ProcessModel model, boolean includeSubProcesses) {
 		int result = model.countVertices();
 		//sub process handling if needed
 		if (includeSubProcesses) {
 			for (FlowNode node : model.getVertices()) {
 				if (node instanceof Subprocess) {
 					result += this.getNumberOfNodes(((Subprocess) node).getSubProcess(), includeSubProcesses);
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the number of {@link OrGateway}s joining the control flow of the given {@link ProcessModel}.
 	 * Thus, all {@link OrGateway} with several incoming edges.
 	 */
 	public int getNumberOfOrJoins(ProcessModel model, boolean includeSubProcesses) {
 		return this.getOrJoins(model, includeSubProcesses).size();
 	}
 
 	/**
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the number of {@link OrGateway}s splitting the control flow of the given {@link ProcessModel}.
 	 * Thus, all {@link OrGateway}s with several outgoing edges and all {@link Activity}s with multiple
 	 * outgoing conditional edges.
 	 */
 	public int getNumberOfOrSplits(ProcessModel model, boolean includeSubProcesses) {
 		return getOrSplits(model, includeSubProcesses).size();
 	}
 
 	/**
 	 * TODO extend metric by information about the number of hand overs<br/>
 	 * and split up into calculation of #lanes and #pools?
 	 * 
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the number of all roles (e.g. pools, lanes) of the given {@link ProcessModel}.
 	 */
 	public int getNumberOfRoles(ProcessModel model, boolean includeSubProcesses) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/**
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the number of {@link StartEvent}s of the given {@link ProcessModel}.
 	 */
 	public int getNumberOfStartEvents(ProcessModel model, boolean includeSubProcesses) {
 		int result = model.filter(StartEvent.class).size();
 		//sub process handling if needed
 		if (includeSubProcesses) {
 			for (FlowNode node : model.getVertices()) {
 				if (node instanceof Subprocess) {
 					result += this.getNumberOfStartEvents(((Subprocess) node).getSubProcess(), includeSubProcesses);
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the number of {@link XorGateway}s joining the control flow of the given {@link ProcessModel}.
 	 * Thus, all {@link XorGateway} with several incoming edges and all {@link Activity}s with multiple 
 	 * incoming edges.
 	 */
 	public int getNumberOfXorJoins(ProcessModel model, boolean includeSubProcesses) {
 		return this.getXorJoins(model, includeSubProcesses).size();
 	}
 
 	/**
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the number of {@link XorGateway}s splitting the control flow of the given {@link ProcessModel}.
 	 * Thus, all {@link XorGateway}s with several outgoing edges.
 	 */
 	public int getNumberOfXorSplits(ProcessModel model, boolean includeSubProcesses) {
 		return this.getXorSplits(model, includeSubProcesses).size();
 	}
 
 	/**
 	 * Calculate the ratio between the number of nodes, whose deletion separates the given
 	 * {@link ProcessModel} into multiple components and the total number of nodes minus 
 	 * number of start and end nodes.
 	 * If the given model is not connected, the separability is the average of all
 	 * 
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the separability of the given {@link ProcessModel}.
 	 */
 	public double getSeparability(ProcessModel model, boolean includeSubProcesses) {
 		int numberOfNodes = this.getNumberOfNodes(model, includeSubProcesses);
 		int numberOfStartAndEndNodes = getNumberOfStartAndEndNodes(model, includeSubProcesses);
 		if (numberOfNodes <= numberOfStartAndEndNodes) {
 			return 0.0;
 		}
 		if (!model.isConnected()){
 			Collection<FlowNode> excludedNodes = getNodesWithoutEdges(model, false);
 			double separabilitySum = getSeparabilitySum(model, excludedNodes, includeSubProcesses);
 			return separabilitySum / getNumberOfModelParts(model, includeSubProcesses);
 		} else {
 			int numberOfCutVerticies = getNumberOfCutVerticies(model, includeSubProcesses);
 			return numberOfCutVerticies / (double)(numberOfNodes - numberOfStartAndEndNodes);			
 		}
 	}
 	
 	/**
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the number of start nodes and end nodes
 	 */
 	private int getNumberOfStartAndEndNodes(ProcessModel model, boolean includeSubProcesses) {
 		int result = model.getEntries().size() + model.getExits().size();
 		//handle sub processes if needed
 		if (includeSubProcesses){
 			for (FlowNode node : model.getVertices()) {
 				if (node instanceof Subprocess) {
 					result += this.getNumberOfStartAndEndNodes(((Subprocess) node).getSubProcess(), includeSubProcesses);
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Modifies the given in-depth mapping by adjusting the in-depth of all {@link FlowNode}s
 	 * of the given path if necessary.
 	 * @param path the path to analyze
 	 * @param inDepths the current in-depth to node mapping
 	 * @param model to analyze
 	 * @return the given in-depth mapping with the actualizations of the path to analyze
 	 */
 	private Map<FlowNode, Integer> calculateInDepthsFor(Vector<FlowNode> path,
 			Map<FlowNode, Integer> inDepths, ProcessModel model) {
 		//adjust in-depth from first to last node of the given path
 		for (FlowNode node : path) {
 			int currentDepth = 0;
 			boolean hasSplitAsPredecessor = false;
 			//get maximum predecessor depth and check for split as predecessor
 			for (FlowNode pre : model.getDirectPredecessors(node)) {
 				currentDepth = Math.max(currentDepth, inDepths.get(pre));
 				if (model.getOutgoingEdges(pre).size() > 1) {
 					hasSplitAsPredecessor = true;
 				}
 			}
 			//depth of current node must be increased if any of the predecessors is a split
 			if (hasSplitAsPredecessor) {
 				currentDepth++;
 			}
 			//depth of current node must be decreased if it is a join, but must be at least zero 
 			if (model.getIncomingControlFlow(node).size() > 1 && currentDepth > 0) {
 				currentDepth--;
 			}
 			//set new depth
 			inDepths.put(node, currentDepth);
 		}
 		return inDepths;
 	}
 
 	/**
 	 * Modifies the given out-depth mapping by adjusting the out-depth of all {@link FlowNode}s
 	 * of the given path if necessary.
 	 * @param path the path to analyze
 	 * @param outDepths the current out-depth to node mapping
 	 * @param model to analyze
 	 * @return the given out-depth mapping with the actualizations of the path to analyze
 	 */
 	private Map<FlowNode, Integer> calculateOutDepthsFor(Vector<FlowNode> path,
 			Map<FlowNode, Integer> outDepths, ProcessModel model) {
 		for (int i = path.size() - 1; i >= 0; i--) {
 			FlowNode node = path.get(i);
 			int currentDepth = 0;
 			boolean hasSplitAsSuccessor = false;
 			//get maximum successor depth and check for split as successor
 			for (FlowNode succ : model.getDirectSuccessors(node)) {
 				currentDepth = Math.max(currentDepth, outDepths.get(succ));
 				if (model.getOutgoingEdges(succ).size() > 1) {
 					hasSplitAsSuccessor = true;
 				}
 			}
 			//depth of current node must be increased if it is a join
 			if (model.getIncomingControlFlow(node).size() > 1) {
 				currentDepth++;
 			}
 			//depth of current node must be decreased if any of the successors is a split,
 			//but must be at least zero
 			if (hasSplitAsSuccessor && currentDepth > 0) {
 				currentDepth--;
 			}
 			//set new node depth
 			outDepths.put(node, currentDepth);
 		}
 		return outDepths;
 	}
 
 	/**
 	 * Calculates a {@link Collection} of all paths from the startNode to the endNode.
 	 * @param startNode {@link FlowNode} to start from
 	 * @param endNode {@link FlowNode} to end with
 	 * @param transitiveClosure the {@link TransitiveClosure} of the {@link ProcessModel} of the given {@link FlowNode}s.
 	 * @param visitedNodes already visited {@link FlowNode}s
 	 * @param currentPaths contains all currently found paths
 	 * @return all paths from the start node to the end node.
 	 */
 	private Vector<Vector<FlowNode>> getAllPaths(FlowNode startNode, FlowNode endNode,
 			TransitiveClosure<ControlFlow<FlowNode>, FlowNode> transitiveClosure,
 			Collection<FlowNode> visitedNodes,
 			Vector<Vector<FlowNode>> currentPaths) {
 		if (startNode == endNode) {
 			return currentPaths;
 		}
 		if (visitedNodes.contains(startNode)) {
 			return currentPaths;
 		}
 		visitedNodes.add(startNode);
 		Collection<ControlFlow<FlowNode>> outgoingEdges = startNode.getModel().getOutgoingControlFlow(startNode);
 		for (ControlFlow<FlowNode> edge : outgoingEdges) {
 			if (edge.getTarget() == endNode) {
 				Vector<FlowNode> nodeList = new Vector<FlowNode>(visitedNodes);
 				nodeList.add(endNode);
 				currentPaths.add(nodeList);
 			}
 			if (transitiveClosure.hasPath(edge.getTarget(), endNode)) {
 				getAllPaths(edge.getTarget(), endNode, transitiveClosure, new Vector<FlowNode>(visitedNodes), currentPaths);
 			}
 		}
 		return currentPaths;
 	}
 
 	/**
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return a {@link Collection} of all alternative joins
 	 * ({@link AlternativGateway}s with at least two incoming edges) of the given {@link ProcessModel}.
 	 */
 	private Collection<FlowNode> getAlternativeJoins(ProcessModel model, boolean includeSubProcesses) {
 		Collection<FlowNode> alternativeSplit = new ArrayList<FlowNode>();
 		Collection<? extends IVertex> alternativeGateways = model.filter(AlternativGateway.class);
 		for (IVertex g : alternativeGateways) {
 			if (model.getIncomingControlFlow((FlowNode) g).size() > 1) {
 				alternativeSplit.add((FlowNode) g);
 			}
 		}		
 		Collection<FlowNode> result = alternativeSplit;
 		//handle sub processes if needed
 		if (includeSubProcesses){
 			for (FlowNode node : model.getVertices()) {
 				if (node instanceof Subprocess) {
 					result.addAll(this.getAlternativeJoins(((Subprocess) node).getSubProcess(), includeSubProcesses));
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return a {@link Collection} of all alternative splits
 	 * ({@link AlternativGateway}s with at least two outgoing edges) of the given {@link ProcessModel}.
 	 */
 	private Collection<FlowNode> getAlternativeSplits(ProcessModel model, boolean includeSubProcesses) {
 		Collection<FlowNode> alternativeSplit = new ArrayList<FlowNode>();
 		Collection<? extends IVertex> alternativeGateways = model.filter(AlternativGateway.class);
 		for (IVertex g : alternativeGateways) {
 			if (model.getOutgoingControlFlow((FlowNode) g).size() > 1) {
 				alternativeSplit.add((FlowNode) g);
 			}
 		}		
 		Collection<FlowNode> result = alternativeSplit;
 		//handle sub processes if needed
 		if (includeSubProcesses){
 			for (FlowNode node : model.getVertices()) {
 				if (node instanceof Subprocess) {
 					result.addAll(this.getAlternativeSplits(((Subprocess) node).getSubProcess(), includeSubProcesses));
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return a {@link Collection} of all and joins ({@link AndGateway}s with at least two incoming edges)
 	 * from the given {@link ProcessModel}.
 	 */
 	private Collection<FlowNode> getAndJoins(ProcessModel model, boolean includeSubProcesses) {
 		Collection<FlowNode> andJoins = new ArrayList<FlowNode>();
 		Collection<? extends IVertex> andGateways = model.filter(AndGateway.class);
 		for (IVertex g : andGateways) {
 			if (model.getIncomingControlFlow((FlowNode) g).size() > 1) {
 				andJoins.add((FlowNode) g);
 			}
 		}
 		Collection<FlowNode> result = andJoins;
 		//handle sub processes if needed
 		if (includeSubProcesses){
 			for (FlowNode node : model.getVertices()) {
 				if (node instanceof Subprocess) {
 					result.addAll(this.getAndJoins(((Subprocess) node).getSubProcess(), includeSubProcesses));
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Extracts all and splits from the given {@link ProcessModel}. An and split is either an {@link AndGateway}
 	 * with at least two outgoing edges or an {@link Activity} with multiple outgoing unconditional edges
 	 * (at least one edge does not have a condition).
 	 * 
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return a {@link Collection} of all and splits
 	 */
 	private Collection<FlowNode> getAndSplits(ProcessModel model, boolean includeSubProcesses) {
 		Collection<FlowNode> andSplits = new ArrayList<FlowNode>();
 		Collection<? extends IVertex> andGateways = model.filter(AndGateway.class);
 		for (IVertex g : andGateways) {
 			if (model.getOutgoingControlFlow((FlowNode) g).size() > 1) {
 				andSplits.add((FlowNode) g);
 			}
 		}
 		//increase counter for each activity with multiple outgoing unconditional edges
 		Collection<? extends IVertex> activities = model.filter(Activity.class);
 		for (IVertex a : activities) {
 			Collection<ControlFlow<FlowNode>> edges = model.getOutgoingControlFlow((FlowNode) a);
 			if (edges.size() > 1) {
 				int conditionalEdges = 0;
 				for (ControlFlow<FlowNode> e : edges){
 					if (e instanceof BpmnControlFlow<?> && ((BpmnControlFlow<FlowNode>)e).hasCondition()) {
 						conditionalEdges++;
 					}
 				}
 				if (conditionalEdges != edges.size()) {
 					andSplits.add((FlowNode) a);
 				}
 			}
 		}
 		Collection<FlowNode> result = andSplits;
 		//handle sub processes if needed
 		if (includeSubProcesses){
 			for (FlowNode node : model.getVertices()) {
 				if (node instanceof Subprocess) {
 					result.addAll(this.getAndSplits(((Subprocess) node).getSubProcess(), includeSubProcesses));
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Calculates the weights for each {@link ControlFlow} edge according to the 
 	 * cross-connectivity metric algorithm(w(src(e)) * w(tgt(e))).
 	 * @param model to analyze
 	 * @param nodeWeights the weights for each {@link FlowNode} of the given {@link ProcessModel}
 	 * @return the weights for each {@link ControlFlow} edge of the given {@link ProcessModel}
 	 */
 	private Map<ControlFlow<FlowNode>, Double> getEdgeWeights(ProcessModel model, Map<FlowNode, Double> nodeWeights) {
 		Collection<ControlFlow<FlowNode>> edges = model.getEdges();
 		Map<ControlFlow<FlowNode>, Double> edgeWeights = new HashMap<ControlFlow<FlowNode>, Double>(edges.size());
 		//calculate weight for edges by multiplying weight of source node with the weight of the target node
 		for(ControlFlow<FlowNode> edge : edges){
 			edgeWeights.put(edge, nodeWeights.get(edge.getSource()) * nodeWeights.get(edge.getTarget()));
 		}
 		return edgeWeights;
 	}
 
 	/**
 	 * @param model the {@link ProcessModel} to build the sub set of
 	 * @param fragment containing the {@link FlowNode}s that should be considered in the sub set
 	 * @return a sub set of the given {@link ProcessModel} containing only the {@link FlowNode}s of the given fragment
 	 */
 	private ProcessModel getExtractedModelPart(ProcessModel model, AbstractMultiGraphFragment<ControlFlow<FlowNode>, FlowNode> fragment) {
		ProcessModel modelPart = model.clone();
		Collection<FlowNode> fragmentVerticies = fragment.getVertices();
		for (FlowNode node : modelPart.getVertices()) {
			if (!fragmentVerticies.contains(node)) {
				modelPart.removeVertex(node);
 			}
 		}
 		return modelPart;
 	}
 
 	/**
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return a {@link Collection} of all {@link Gateway}s as well as all {@link Activity}s
 	 *  with multiple incoming or outgoing edges of the given {@link ProcessModel}.
 	 */
 	private Collection<FlowNode> getGateways(ProcessModel model, boolean includeSubProcesses) {
 		Collection<FlowNode> gateways = new ArrayList<FlowNode>(this.getOrSplits(model, false));
 		gateways.addAll(this.getOrJoins(model, false));
 		gateways.addAll(this.getXorJoins(model, false));
 		gateways.addAll(this.getXorSplits(model, false));
 		gateways.addAll(this.getAndJoins(model, false));
 		gateways.addAll(this.getAndSplits(model, false));
 		gateways.addAll(this.getAlternativeJoins(model, false));
 		gateways.addAll(this.getAlternativeSplits(model, false));
 		//add all Gateways that either Split nor Join, because they have no incoming and outgoing edges
 		for (IVertex node : model.filter(Gateway.class)) {
 			if (!gateways.contains(node)) {
 				gateways.add((FlowNode) node);
 			}
 		}
 		//sub process handling if needed
 		if (includeSubProcesses) {
 			for (FlowNode node : model.getVertices()) {
 				if (node instanceof Subprocess) {
 					gateways.addAll(this.getGateways(((Subprocess) node).getSubProcess(), includeSubProcesses));
 				}
 			}
 		}
 		return gateways;
 	}
 
 	/**
 	 * @param model the {@link ProcessModel} to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return a {@link Collection} of {@link FlowNode}s without at least one incoming or outgoing edge.
 	 *  If none was found, an empty {@link Collection} is returned.
 	 */
 	private Collection<FlowNode> getNodesWithoutEdges(ProcessModel model, boolean includeSubProcesses) {
 		Collection<FlowNode> nodesWithoutEdges = new ArrayList<FlowNode>();
 		for (FlowNode node : model.getVertices()) {
 			if (model.getEdges(node).isEmpty()) {
 				nodesWithoutEdges.add(node);
 			}
 		}
 		Collection<FlowNode> result = nodesWithoutEdges;
 		//sub process handling if needed
 		if (includeSubProcesses) {
 			for (FlowNode node : model.getVertices()) {
 				if (node instanceof Subprocess) {
 					result.addAll(this.getNodesWithoutEdges(((Subprocess) node).getSubProcess(), includeSubProcesses));
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Calculates the node weights according to the cross-connectivity metric algorithm.
 	 * @param model to analyze
 	 * @return a {@link Map} from each node of the given {@link ProcessModel} to its weight.
 	 */
 	private Map<FlowNode, Double> getNodeWeights(ProcessModel model) {
 		Collection<FlowNode> nodes = model.getVertices();
 		Map<FlowNode, Double> nodeWeights = new HashMap<FlowNode, Double>(nodes.size());
 		//initialize with one for each node
 		for(FlowNode node : nodes) {
 			nodeWeights.put(node, 1.0);
 		}
 		//set XOR Gateways to 1 / number of edges of gateway
 		Set<FlowNode> xorGateways = new HashSet<FlowNode>(this.getXorJoins(model, false));
 		xorGateways.addAll(this.getXorSplits(model, false));
 		for(FlowNode node : xorGateways) {
 			nodeWeights.put(node, 1 / (double) model.getEdges(node).size());
 		}
 		//set OR and Alternative Gateways to (1/2^d - 1) + ((2^d - 2)/((2^d - 1) * d)) with d = number of edges of gateway
 		Set<FlowNode> orAndAlternativeGateways = new HashSet<FlowNode>(this.getOrJoins(model, false));
 		orAndAlternativeGateways.addAll(this.getOrSplits(model, false));
 		orAndAlternativeGateways.addAll(this.getAlternativeJoins(model, false));
 		orAndAlternativeGateways.addAll(this.getAlternativeSplits(model, false));
 		for(FlowNode node : orAndAlternativeGateways) {
 			double edges = (double) model.getEdges(node).size();
 			double intermediateResult = (double) Math.pow(2, edges);
 			double weight = (1 / (intermediateResult - 1)) + ((intermediateResult - 2) / ((intermediateResult - 1) * edges));
 			nodeWeights.put(node, weight);
 		}
 		return nodeWeights;
 	}
 
 	/**
 	 * <b>Attention:</b> The given {@link ProcessModel} must be connected.
 	 * 
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the number of nodes whose deletion splits the given {@link ProcessModel} into multiple parts.
 	 */
 	private int getNumberOfCutVerticies(ProcessModel model, boolean includeSubProcesses) {
 		Collection<FlowNode> modelNodes = model.getVertices();
 		int numberOfCutVerticies = 0;
 		if (modelNodes.size() > 1) {
 			CombinationGenerator<FlowNode> combinationGenerator = new CombinationGenerator<FlowNode>(modelNodes,1);
 			GraphAlgorithms<ControlFlow<FlowNode>, FlowNode> graphAlgorithms = new GraphAlgorithms<ControlFlow<FlowNode>, FlowNode>();
 			AbstractMultiGraphFragment<ControlFlow<FlowNode>,FlowNode> graphFragment = new AbstractMultiGraphFragment<ControlFlow<FlowNode>,FlowNode>(model);
 			while (combinationGenerator.hasMore()) {
 				Collection<FlowNode> combinationVerticies = combinationGenerator.getNextCombination();
 				
 				graphFragment.copyOriginalGraph();
 				graphFragment.removeVertices(combinationVerticies);
 				
 				if (!graphAlgorithms.isConnected(graphFragment)) {
 					numberOfCutVerticies++;
 				}
 			}
 		}
 		//handle sub processes if needed
 		if (includeSubProcesses){
 			for (FlowNode node : modelNodes) {
 				if (node instanceof Subprocess) {
 					numberOfCutVerticies += this.getNumberOfCutVerticies(((Subprocess) node).getSubProcess(), includeSubProcesses);
 				}
 			}
 		}
 		return numberOfCutVerticies;
 	}
 
 	/**
 	 * Calculates the number of not connected {@link ProcessModel} parts, excluding the given {@link FlowNode} list.
 	 * 
 	 * @param model to analyze
 	 * @param excludedNodes a {@link Collection} of {@link FlowNode}s that should not be considered in result calculation
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the number of model parts of the given {@link ProcessModel} apart of the given exclusion list.
 	 */
 	private int getNumberOfModelParts(ProcessModel model, Collection<FlowNode> excludedNodes, boolean includeSubProcesses) {
 		int modelParts = 1;
 		GraphAlgorithms<ControlFlow<FlowNode>, FlowNode> graphAlgorithms = new GraphAlgorithms<ControlFlow<FlowNode>, FlowNode>();		
 		AbstractMultiGraphFragment<ControlFlow<FlowNode>, FlowNode> fragment = graphAlgorithms.getConnectedFragment(model, excludedNodes);
 
 		if (fragment.getVertices().isEmpty()) {
 			return 0;
 		}
 		excludedNodes.addAll(fragment.getVertices());
 
 		modelParts += getNumberOfModelParts(model, excludedNodes, includeSubProcesses);
 
 		//handle sub processes if needed
 		if (includeSubProcesses){
 			for (FlowNode node : model.getVertices()) {
 				if (node instanceof Subprocess) {
 					modelParts += this.getNumberOfModelParts(((Subprocess) node).getSubProcess(), includeSubProcesses);
 				}
 			}
 		}
 		return modelParts;
 	}
 
 	/**
 	 * @param model too analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return a {@link Collection} of all or joins ({@link OrGateway}s with at least two incoming edges)
 	 * extracted from the given {@link ProcessModel}.
 	 */
 	private Collection<FlowNode> getOrJoins(ProcessModel model, boolean includeSubProcesses) {
 		Collection<FlowNode> orJoins = new ArrayList<FlowNode>();
 		Collection<? extends IVertex> orGateways = model.filter(OrGateway.class);
 		for (IVertex g : orGateways) {
 			if (model.getIncomingControlFlow((FlowNode) g).size() > 1) {
 				orJoins.add((FlowNode) g);
 			}
 		}
 		Collection<FlowNode> result = orJoins;
 		//handle sub processes if needed
 		if (includeSubProcesses){
 			for (FlowNode node : model.getVertices()) {
 				if (node instanceof Subprocess) {
 					result.addAll(this.getOrJoins(((Subprocess) node).getSubProcess(), includeSubProcesses));
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Extracts the or splits of the given model. An or split can be either an {@link OrGateway} with more
 	 * than one outgoing edge or an {@link Activity} with multiple outgoing conditional edges.
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return a {@link Collection} of all or splits.
 	 */
 	private Collection<FlowNode> getOrSplits(ProcessModel model, boolean includeSubProcesses) {
 		Collection<FlowNode> orSplits = new ArrayList<FlowNode>();
 		for (IVertex g : model.filter(OrGateway.class)) {
 			if (model.getOutgoingControlFlow((FlowNode) g).size() > 1) {
 				orSplits.add((FlowNode) g);
 			}
 		}
 		//increase counter for each activity with multiple outgoing conditional edges
 		Collection<? extends IVertex> activities = model.filter(Activity.class);
 		for (IVertex a : activities) {
 			Collection<ControlFlow<FlowNode>> edges = model.getOutgoingControlFlow((FlowNode) a);
 			if (edges.size() > 1) {
 				int conditionalEdges = 0;
 				for (ControlFlow<FlowNode> e : edges){
 					if (e instanceof BpmnControlFlow<?> && ((BpmnControlFlow<FlowNode>)e).hasCondition()) {
 						conditionalEdges++;
 					}
 				}
 				if (conditionalEdges == edges.size()) {
 					orSplits.add((FlowNode) a);
 				}
 			}
 		}
 		Collection<FlowNode> result = orSplits;
 		//handle sub processes if needed
 		if (includeSubProcesses){
 			for (FlowNode node : model.getVertices()) {
 				if (node instanceof Subprocess) {
 					result.addAll(this.getOrSplits(((Subprocess) node).getSubProcess(), includeSubProcesses));
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @param model the disconnected {@link ProcessModel} to analyze
 	 * @param totalNumberOfModelParts the currently handled number of parts of the {@link ProcessModel}
 	 * @param excludedNodes a {@link Collection} of all nodes of the given model without incoming and outgoing edges
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return the sum of all separability values.
 	 */
 	private double getSeparabilitySum(ProcessModel model, Collection<FlowNode> excludedNodes, boolean includeSubProcesses) {
 		double separabilitySum = 0.0;
 
 		GraphAlgorithms<ControlFlow<FlowNode>, FlowNode> graphAlgorithms = new GraphAlgorithms<ControlFlow<FlowNode>, FlowNode>();		
 		AbstractMultiGraphFragment<ControlFlow<FlowNode>, FlowNode> fragment = graphAlgorithms.getConnectedFragment(model, excludedNodes);
 
 		//if the model only consists of single not connected activities return with separability of zero and
 		// add the number of excluded nodes to the total number of model parts minus two
 		if (fragment.getVertices().isEmpty()) {
 			return 0.0;
 		}
 
 		ProcessModel extractedFragment = getExtractedModelPart(model, fragment);
 		excludedNodes.addAll(fragment.getVertices());
 
 		//calculate separability for each model part recursively
 		if (model.isConnected()){
 			separabilitySum += getSeparability(model, includeSubProcesses);
 		} else {
 			separabilitySum +=  getSeparabilitySum(model, excludedNodes, includeSubProcesses);
 		}
 
 		separabilitySum += getSeparability(extractedFragment, includeSubProcesses);
 		//handle sub processes if needed
 		if (includeSubProcesses){
 			for (FlowNode node : model.getVertices()) {
 				if (node instanceof Subprocess) {
 					Bpmn<BpmnControlFlow<FlowNode>, FlowNode> subProcess = ((Subprocess) node).getSubProcess();
 					if (subProcess.isConnected()){
 						separabilitySum += getSeparability(subProcess, includeSubProcesses);
 					} else {
 						separabilitySum +=  getSeparabilitySum(subProcess, this.getNodesWithoutEdges(subProcess, false), includeSubProcesses);
 					}
 				}
 			}
 		}
 		return separabilitySum;
 	}
 
 	/**
 	 * Extracts all xor joins from the given {@link ProcessModel}. A xor join is either a {@link XorGateway} with
 	 * at least two incoming edges or an {@link Activity} with at least two incoming edges.
 	 * 
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return a {@link Collection} of xor joins
 	 */
 	private Collection<FlowNode> getXorJoins(ProcessModel model, boolean includeSubProcesses) {
 		Collection<FlowNode> xorJoins = new ArrayList<FlowNode>();
 		Collection<? extends IVertex> xorGateways = model.filter(XorGateway.class);
 		for (IVertex g : xorGateways) {
 			if (model.getIncomingControlFlow((FlowNode) g).size() > 1) {
 				xorJoins.add((FlowNode) g);
 			}
 		}
 		//increase counter for each activity with multiple incoming edges
 		Collection<? extends IVertex> activities = model.filter(Activity.class);
 		for (IVertex a : activities) {
 			if (model.getIncomingControlFlow((FlowNode) a).size() > 1) {
 				xorJoins.add((FlowNode) a);
 			}
 		}
 		Collection<FlowNode> result = xorJoins;
 		//handle sub processes if needed
 		if (includeSubProcesses){
 			for (FlowNode node : model.getVertices()) {
 				if (node instanceof Subprocess) {
 					result.addAll(this.getXorJoins(((Subprocess) node).getSubProcess(), includeSubProcesses));
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Extracts a {@link Collection} of all xor splits from the given {@link ProcessModel}.
 	 * A xor split is a {@link XorGateway} with at least two outgoing edges.
 	 * 
 	 * @param model to analyze
 	 * @param includeSubProcesses flag indicates whether to include all available sub process or not
 	 * @return a {@link Collection} of all xor splits extracted from the given {@link ProcessModel}.
 	 */
 	private Collection<FlowNode> getXorSplits(ProcessModel model, boolean includeSubProcesses) {
 		Collection<FlowNode> xorSplits = new ArrayList<FlowNode>();
 		Collection<? extends IVertex> xorGateways = model.filter(XorGateway.class);
 		for (IVertex g : xorGateways) {
 			if (model.getOutgoingControlFlow((FlowNode) g).size() > 1) {
 				xorSplits.add((FlowNode) g);
 			}
 		}
 		Collection<FlowNode> result = xorSplits;
 		//handle sub processes if needed
 		if (includeSubProcesses){
 			for (FlowNode node : model.getVertices()) {
 				if (node instanceof Subprocess) {
 					result.addAll(this.getXorSplits(((Subprocess) node).getSubProcess(), includeSubProcesses));
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Calculates the longest path from the given {@link FlowNode} to a {@link FlowNode}
 	 * without an outgoing edge. If an already visited node is reached minus one is returned.
 	 * @param node the {@link FlowNode} to start with
 	 * @param weights a map from each {@link FlowNode} to it's maximum path length
 	 * @param currentWeight the actual path length
 	 * @param visitedNodes a {@link Collection} of {@link FlowNode}s, that has already been visited
 	 * @param endNodes a {@link Collection} of {@link FlowNode}s of the given model, without an outgoing edge
 	 * @return the maximum path length
 	 */
 	private int longestPath(FlowNode node, int currentWeight, Collection<FlowNode> visitedNodes, boolean hasEndNodes) {
 		currentWeight++;
 		if (visitedNodes.contains(node)) {
 			return -1;
 		}
 		visitedNodes.add(node);
 		//find longest path of all outgoing edges
 		int maxLength = hasEndNodes ? -1 : currentWeight;
 		Collection<ControlFlow<FlowNode>> outgoingEdges = node.getModel().getOutgoingControlFlow(node);
 		for (ControlFlow<FlowNode> edge : outgoingEdges) {
 			int pathLength = longestPath(edge.getTarget(), currentWeight, new ArrayList<FlowNode>(visitedNodes), hasEndNodes);
 			maxLength = Math.max(maxLength, pathLength);
 		}
 		return outgoingEdges.isEmpty() ? currentWeight : maxLength;
 	}
 
 	/**
 	 * Calculates the number of maximum tokens for each node of the given {@link ProcessModel}.
 	 * @param model to analyze
 	 * @param orAndAlternativeSplits list with all or splits and alternative splits to consider
 	 * @return a mapping for each {@link FlowNode} of the given {@link ProcessModel} to it's number of tokens
 	 */
 	private Map<FlowNode, Integer> numberOfTokens(ProcessModel model, Collection<FlowNode> orAndAlternativeSplits) {
 		Collection<FlowNode> modelNodes = model.getVertices();
 		Map<FlowNode, Integer> numberOfTokens = new HashMap<FlowNode, Integer>(modelNodes.size());
 		//fill with initial value
 		for (FlowNode node : modelNodes) {
 			numberOfTokens.put(node, 1);
 		}
 		//increment number of tokens for each node following an or split, a xor split or an alternative split
 		for (FlowNode split : orAndAlternativeSplits) {
 			for(FlowNode node : model.getAllSuccessors(split)) {
 				numberOfTokens.put(node, numberOfTokens.get(node) + model.getOutgoingEdges(split).size() - 1);
 			}
 		}
 		//decrement number of tokens for each node following an or join, a xor join or an alternative join
 		Collection<FlowNode> joins = this.getOrJoins(model, false);
 		joins.addAll(this.getAlternativeJoins(model, false));
 		for (FlowNode join : joins) {
 			for(FlowNode node : model.getAllSuccessors(join)) {
 				//number of tokens must be at least one
 				int tokens = numberOfTokens.get(node) - 1;
 				tokens = tokens < 1 ? 1 : tokens;
 				numberOfTokens.put(node, tokens);
 			}
 		}
 		return numberOfTokens;
 	}
 }
