 package org.life.sl.routefinder;
 
 
 /*
 JMapMatcher
 
 Copyright (c) 2011 Bernhard Barkow, Hans Skov-Petersen, Bernhard Snizek and Contributors
 
 mail: bikeability@life.ku.dk
 web: http://www.bikeability.dk
 
 This program is free software; you can redistribute it and/or modify it under 
 the terms of the GNU General Public License as published by the Free Software 
 Foundation; either version 3 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful, but WITHOUT 
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License along with 
 this program; if not, see <http://www.gnu.org/licenses/>.
 */
 
 import gnu.trove.TIntProcedure;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Properties;
 
 import java.util.Stack;
 
 import org.apache.log4j.Logger;
 import org.life.sl.graphs.PathSegmentGraph;
 import org.life.sl.mapmatching.EdgeStatistics;
 import org.life.sl.routefinder.Label;
 import org.life.sl.utils.Timer;
 
 //import org.life.sl.shapefilereader.ShapeFileReader;
 
 //import cern.jet.random.Uniform;
 
 import com.infomatiq.jsi.Rectangle;
 import com.infomatiq.jsi.SpatialIndex;
 
 import com.infomatiq.jsi.test.SpatialIndexFactory;
 
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.Point;
 
 import com.vividsolutions.jts.operation.linemerge.LineMergeEdge;
 //import com.vividsolutions.jts.operation.linemerge.LineMergeGraph;
 import com.vividsolutions.jts.planargraph.DirectedEdge;
 import com.vividsolutions.jts.planargraph.Edge;
 import com.vividsolutions.jts.planargraph.Node;
 
 /**
  * A Routefinder. Finds all routes in a graph from origin to destination.
  * 
  * @author Pimin Kostas Kefaloukos
  * @author Bernhard Snizek
  * @author Bernhard Barkow
  */
 public class RouteFinder {
 	public static enum LabelTraversal {
 		None,			///< no special sorting
 		Shuffle,		///< shuffle labels before advancing in the iteration
 		ShuffleReset,	///< shuffle labels before advancing in the iteration, and reset the search after a route was found
 		BestFirst,		///< traverse label in reverse natural order (highest score first) - this should find the globally best route first
 		BestFirstDR,	///< like BestFirst, but including a Dead Reckoning fallback strategy
 		WorstFirst,		///< traverse label in natural order (highest score last)
 		BestLastEdge;	///< traverse label in reverse natural order, considering only the last edge
 	}
 
 	private int iShowProgressDetail = 2;	///< show a progress indicator while finding routes?
 
 	private LabelTraversal itLabelOrder = LabelTraversal.BestFirst;
 	private float kNearestEdgeDistance = 50.f;	// the larger, the slower
 	
 	// Initialization:
 	private RFParams rfParams;// = new Constraints();
 	
 	private Node startNode = null;	///< route start node (Origin)	
 	private Node endNode = null;	///< route end node (Destination)
 	private PathSegmentGraph network = null;	///< graph to search (all routes are in this network)
 	
 	// articulation points identified in graph
 //	private Collection<Node> articulationPoints = new ArrayList<Node>();
 	// bridges identified in graph
 //	private Collection<Edge> bridges = new ArrayList<Edge>();
 
 	private long numLabels = 0;				///< number of labels (states) generated when running algorithm
 	int numExpansions = 0;					///< number of expansions that were performed
 	private long numLabels_rejected = 0;	///< number of labels (states) that have been rejected due to constraints
 	private long rejectedLabelsLimit = 0;	///< limit for number of labels (states) that have been rejected
 	private long numLabels_overlap = 0;		///< number of labels that have overlapping nodes 
 	private long numLabels_psOverlap = 0;
 	private long maxLabels = 0;				///< maximum number of labels to compute
 	private double maxRuntime = 0;			///< maximum computation time per route, in seconds
 	private int nGenBack = 0;
 	private SpatialIndex si;
 	private HashMap<Integer, Edge> counter__edge;
 	
 	private double gpsPathLength = 0.;
 	private double maxPathLength = 0.;
 	private double maxPSOverlap = 0.;
 	private EdgeStatistics edgeStatistics = null;
 
 	ArrayList<Label> results = null;
 	private Logger logger = Logger.getLogger("RouteFinder");
 	private MatchStats stats = new MatchStats(); 
 	
 	/**
 	 * create a Routefinder from a PathSegmentGraph, using default parameters
 	 * @param network the input PathSegmentGraph
 	 */
 	public RouteFinder(PathSegmentGraph network) {
 		initDefaults();			// initialize algorithm parameters
 		this.network = network;	// set network for further use
 	}
 	
 	/**
 	 * create a Routefinder from a PathSegmentGraph, using a defined parameter set
 	 * @param network the input PathSegmentGraph
 	 */
 	public RouteFinder(PathSegmentGraph network, RFParams rfParams0) {
 		this(network, rfParams0, null);
 	}
 	
 	/**
 	 * create a Routefinder from a PathSegmentGraph, using a defined parameter set
 	 * @param network the input PathSegmentGraph
 	 */
 	public RouteFinder(PathSegmentGraph network, RFParams rfParams0, ArrayList<Label> labels0) {
 		this(network);
 		rfParams = rfParams0;
 		if (labels0 != null && !labels0.isEmpty()) this.results = labels0;
 		else this.results = new ArrayList<Label>();
 	}
 	
 	/**
 	 * create default parameter set for the algorithm 
 	 */
 	private void initDefaults() {
 		rfParams = new RFParams();
 
 		rfParams.setInt(RFParams.Type.MaximumNumberOfRoutes, 1000);	///< maximum number of routes to find (or 0 for infinite)
 		rfParams.setInt(RFParams.Type.BridgeOverlap, 1);
 		rfParams.setInt(RFParams.Type.EdgeOverlap, 1);		///< how often each edge may be used
 //		constraints.setInt(Constraints.Type.ArticulationPointOverlap, 2);
 		rfParams.setInt(RFParams.Type.NodeOverlap, 1);		///< how often each single node may be crossed
 		rfParams.setDouble(RFParams.Type.DistanceFactor, 1.1);		///< how much the route may deviate from the shortest possible
 		rfParams.setDouble(RFParams.Type.MinimumLength, 0.0);		///< minimum route length
 		rfParams.setDouble(RFParams.Type.MaximumLength, 1.e20);		///< maximum route length (quasi no limit here)
 		rfParams.setDouble(RFParams.Type.MaxPSOverlap, .8);			///< maximum allowed overlap between routes
 		rfParams.setDouble(RFParams.Type.NetworkBufferSize2, 0.);	///< initial buffer size in meters (!)
 		rfParams.setDouble(RFParams.Type.NetworkBufferSize, 100.);	///< buffer size in meters (!)
 		rfParams.setInt(RFParams.Type.RejectedLabelsLimit, 0);		///< limit for unsuccessful labels
 		rfParams.setInt(RFParams.Type.NoLabelsResizeNetwork, 0);	///< factor to resize network buffer if no routes were found
 		rfParams.set(RFParams.Type.LabelTraversal, LabelTraversal.BestFirst.toString());		///< way of label traversal
 		rfParams.set(RFParams.Type.LabelTraversal2, LabelTraversal.ShuffleReset.toString());		///< way of label traversal
 		rfParams.setInt(RFParams.Type.ShowProgressDetail, 2);		///< how often each edge may be used
 	}
 	
 	/**
 	 * external interface to set constraints for the algorithm
 	 * @param ic HashMap of integer constraints
 	 * @param dc HashMap of double constraints
 	 */
 	public void setConstraints(HashMap<RFParams.Type, Integer> ic, HashMap<RFParams.Type, Double> dc) {
 		rfParams.setConstraints(ic, dc);
 	}
 	
 	public void setEdgeStatistics(EdgeStatistics eStat) {
 		edgeStatistics = eStat;
 	}
 
 	public double getMaxPathLength() {
 		return maxPathLength;
 	}
 	
 	public double getGPSPathLength() {
 		return gpsPathLength;
 	}
 	
 	/**
 	 * Find all routes between startNode and endNode in the network this.network as list of labels; 
 	 * Label.getRouteAsEdges() can then be used to get a list of DirectedEdges that represent the route.
 	 * @param startNode Routes should start in this node
 	 * @param endNode Routes should end in this node
 	 * @return A list of the routes found (represented by a list of Labels) 
 	 */
 	public ArrayList<Label> findRoutes(Node startNode, Node endNode, double gpsPathLength0) {
 		//*** INITIALIZATION ***//
 
 		// check that startNode and endNode belong to same graph
 		if (network.findClosestNode(startNode.getCoordinate()) == null ||
 				network.findClosestNode(endNode.getCoordinate()) == null) {
 			logger.error("Origin and/or destination are not in network!");	// indicate failure
 			return new ArrayList<Label>();
 		}
 
 		if (rfParams.getBool(RFParams.Type.SwapOD)) {
 			this.startNode = endNode;
 			this.endNode = startNode;
 		} else {
 			this.startNode = startNode;
 			this.endNode = endNode;
 		}
 		this.gpsPathLength = gpsPathLength0;
 		numLabels = 0;
 		int numDeadEnds = 0;
 		numExpansions = 0;
 
 		this.itLabelOrder = LabelTraversal.valueOf(rfParams.getString(RFParams.Type.LabelTraversal));
 		this.nGenBack = rfParams.getInt(RFParams.Type.ShuffleResetNBack);
 		this.rejectedLabelsLimit = rfParams.getInt(RFParams.Type.RejectedLabelsLimit);
 		this.maxLabels = rfParams.getInt(RFParams.Type.MaxLabels);
 		this.maxRuntime = rfParams.getDouble(RFParams.Type.MaxRuntime);
 		this.iShowProgressDetail = rfParams.getInt(RFParams.Type.ShowProgressDetail);
 		// precalculate the minimum path length, for use as a constraint in label expansion:
 		maxPathLength = gpsPathLength * rfParams.getDouble(RFParams.Type.DistanceFactor);
 		maxPSOverlap = rfParams.getDouble(RFParams.Type.MaxPSOverlap);
 		// if there was a problem, use the given maximum length constraint:
 		double minDist = this.startNode.getCoordinate().distance(this.endNode.getCoordinate());	// compare to Euclidian distance
 		if (maxPathLength < minDist) {
 			System.err.println("Warning: invalid GPS data? referencePathLength < minDist (" + maxPathLength + " < " + minDist + ")");
 			maxPathLength = 0.;
 		}
 		// populate statistics
 		stats.status = MatchStats.Status.OK;
 		stats.sourceRouteID = network.getSourceRouteID();
 		stats.trackLength = gpsPathLength;
 		stats.ODDist = minDist;
 		stats.maxLength = maxPathLength;
 		stats.maxPSOverlap = maxPSOverlap;
 		stats.network_edges = network.getSize_Edges();
 		stats.network_nodes = network.getSize_Nodes();
 		stats.network_meanDegree = network.getMeanDegree();
 		stats.network_maxRoutesEst = Math.pow(stats.network_meanDegree-1., (double)stats.network_nodes/2.);
 		logger.info("Euclidian GPS Path length = " + gpsPathLength + ", path length limit = " + maxPathLength + "; O-D-Distance = " + minDist);
 		logger.info("Network size: " + stats.network_edges + " edges, " + stats.network_nodes + " nodes");
 		logger.info("Graph: max. route estimate: " + stats.network_maxRoutesEst +  " / " + network.getNCombinations());
 
 		System.gc();	// can't hurt...
 		Stack<Label> stack = new Stack<Label>();
 		//*** START OF ALGORITHM ***//
 		Label rootLabel = new Label(this.startNode);
 		stack.push(rootLabel);	// push start node to stack
 
 		// set up tree traversal strategy:
 		LabelTraversal itLabelOrder_orig = itLabelOrder;
 		int shuffleResetExtraRoutes = rfParams.getInt(RFParams.Type.ShuffleResetExtraRoutes);
 		if (itLabelOrder == LabelTraversal.ShuffleReset && shuffleResetExtraRoutes > 0) itLabelOrder = LabelTraversal.BestFirstDR; 
 		logger.info("Initial tree traversal strategy: " + itLabelOrder.toString());
 		Label.LastEdgeComparator lastEdgeComp = new Label.LastEdgeComparator(itLabelOrder);
 		
 		Timer timer = new Timer();	// timer to observe total runtime
 		timer.init();
 		
 		stackLoop:
 		while (!stack.empty()) {	// algorithm's main loop
 			// create label expansion (next generation):
 			Label expandingLabel = stack.pop();	// get last label from stack and expand it:
 			ArrayList<Label> expansion = expandLabel(expandingLabel);	// calculate the expansion of the label (continuation from last node along all available edges)
 			if (expansion.size() > 0) {
 				if (itLabelOrder == LabelTraversal.Shuffle || itLabelOrder == LabelTraversal.ShuffleReset) Collections.shuffle(expansion);				// randomize the order in which the labels are treated
 				// Attention: sorting the labels affects two parts:
 				// 1. the expansion array, which is processed linearly
 				// 2. the stack, which is effectively processed in reverse order!
 				/*else if (itLabelOrder == LabelTraversal.BestFirst) Collections.sort(expansion, lastEdgeComp);		// order labels in ascending order (lowest score is treated first), so that the highest score ends up on top of the stack
 				else if (itLabelOrder == LabelTraversal.WorstFirst) Collections.sort(expansion, lastEdgeCompRev);	// order labels in descending order (highest score first), so the best label ends up at the bottom of the stack
 					// Update: since the labels are identical up to the parent node, we compare only the last Edge
 				else if (itLabelOrder == LabelTraversal.BestLastEdge) Collections.sort(expansion, lastEdgeComp);*/
 				else Collections.sort(expansion, lastEdgeComp);
 		
 				// test the newly created labels:
 				for (Label currentLabel : expansion) {	// loop over all next-generation labels
 					numLabels++;
 					// is label a new valid route?
 					boolean bStoreRoute = isValidRoute(currentLabel);
 					if (bStoreRoute)	{	// valid route means: it ends in the destination node and fulfills the length constraints
						if (itLabelOrder == LabelTraversal.ShuffleReset) {	// reset search:
							// check if this route already exists:
 							for (Label l : results) {
								if (currentLabel.equals(l)) { bStoreRoute = false; break; }
 							}
							if (bStoreRoute) results.add(currentLabel);
						} //else without comparison - labels are all different due to search strategy
 						if (bStoreRoute) results.add(currentLabel);	// add the valid route to list of routes
 						
 						// check for shuffleResetExtraRoutes and switch to ShuffleReset mode, if applicable:
 						if (itLabelOrder != LabelTraversal.ShuffleReset && itLabelOrder_orig == LabelTraversal.ShuffleReset && results.size() >= shuffleResetExtraRoutes) {
 							itLabelOrder = itLabelOrder_orig;
 							logger.info("Switching tree traversal strategy: " + itLabelOrder.toString());
 						}
 						// in ShuffleReset mode, stop tree search and start again at the root node (Origin):
 						if (itLabelOrder == LabelTraversal.ShuffleReset) {
 							if (nGenBack == 0) {	// remove all elements except for the root node
 								stack.removeAllElements();	// faster: remove all, ...
 								stack.push(rootLabel);		// ... then return the root node to the stack
 							} else {	// step back a certain number of generations
 								Label backLabel = currentLabel.getNthParent(nGenBack);
 								while (stack.size() > 0 && stack.pop() != backLabel) {
 									stack.push(backLabel);	// put it back on stack and start from there
 								}
 							}
 						}
 						// check if maximum number of routes is reached
 						int nMaxRoutes = rfParams.getInt(RFParams.Type.MaximumNumberOfRoutes);
 						if (nMaxRoutes > 0 && results.size() >= nMaxRoutes) {	// stop after the defined max. number of routes
 							logger.warn("["+network.getSourceRouteID()+"] Maximum number of routes reached (Constraint.MaximumNumberOfRoutes = " + rfParams.getInt(RFParams.Type.MaximumNumberOfRoutes) + ")");
 							stats.status = MatchStats.Status.MAXROUTES;
 							break stackLoop;
 						}
 					}
 
 					if (!bStoreRoute) {	// check for maxLabels condition:
 						if (maxLabels > 0 && numLabels > maxLabels) {
 							logger.warn("["+network.getSourceRouteID()+"] Maximum number of labels reached (Constraint.MaxLabels = " + rfParams.getInt(RFParams.Type.MaxLabels) + ")");
 							stats.status = MatchStats.Status.MAXLABELS;
 							break stackLoop;
 						}
 						else stack.push(currentLabel);		// destination is not reached yet: store the label on stack for the next iteration
 					}
 				}
 				
 				if (iShowProgressDetail > 0) {	// some log output?
 					if (iShowProgressDetail > 1 && numLabels%50000 == 0) System.out.print(".");
 					if (numLabels%5000000 == 0) {
 						System.out.printf("[%d] dead ends: %d - overlaps: %d - PSOverlap: %d - rejected: %d\n", network.getSourceRouteID(), numDeadEnds, numLabels_overlap, numLabels_psOverlap, numLabels_rejected);
 						String s = "lbl: " + numLabels;
 						if (iShowProgressDetail > 1) s += " - l: " + (expandingLabel.getLength() / gpsPathLength);
 						s += " - exp: " + numExpansions + " - res: " + results.size();
 						System.out.println(s);
 					}
 					long freeMem = Runtime.getRuntime().freeMemory();
 					if (numLabels%500000 == 0 && freeMem / (double)Runtime.getRuntime().maxMemory() < .1) {
 						System.out.print("Mem: " + freeMem/(1024*1024) + "MB");
 						System.gc();
 						System.out.println(" / " + Runtime.getRuntime().freeMemory()/(1024*1024) + "MB");
 						// if there is really no memory left, just stop:
 						if (Runtime.getRuntime().freeMemory() < 250000000) {	// 10MB
 							logger.error("Stopping due to memory overload!");
 							stats.status = MatchStats.Status.MEMORY;
 							break stackLoop;
 						}
 					}
 				}
 				// check for rejectedLabelsLimit-constraint:
 				if (rejectedLabelsLimit > 0 && numLabels_rejected > 0 && numLabels_rejected%rejectedLabelsLimit == 0 && results.size() == 0) {
 					logger.warn("Reached rejectedLabelsLimit - increase network buffer size?");
 				}
 			} else {	// "dead end" - this label is invalid
 				numDeadEnds++;
 				expandingLabel = null;
 			}
 			
 			// check the runtime constraint (top priority):
 			if (maxRuntime > 0 && timer.getRunTime(false) > maxRuntime) {
 				logger.warn("Reached maximum runtime of "+maxRuntime+" s");
 				break stackLoop;
 			}
 		}
 		// some statistics on the computation:
 		System.out.printf("\nlabels analyzed:\t%14d\nvalid routes:\t\t%14d\ndead end-labels:\t%14d\t(%2.2f%%)\nlabels rejected:\t%14d\t(%2.2f%%)\nnodes in network:\t%14d\n\n",
 				numLabels, results.size(), numDeadEnds, (100.*numDeadEnds/numLabels), numLabels_rejected, (100.*numLabels_rejected/numLabels), 
 				network.getNodes().size());
 		// populate statistics container:
 		stats.nLabels = numLabels;
 		stats.nExpansions = numExpansions;
 		stats.nRoutes = results.size();
 		stats.nRejected_length = numLabels_rejected - numLabels_overlap;
 		stats.nRejected_overlap = numLabels_overlap;
 		stats.nRejected_psOverlap = numLabels_psOverlap;
 		stats.nDeadEnds = numDeadEnds;
 		
 		return results;
 	}
 
 	/**
 	 * Checks if the labeled route is valid, i.e. if it ends in the end node and fulfills the length constraints
 	 * @param label the label to check
 	 * @return true if the route is valid 
 	 */
 	private boolean isValidRoute(Label label) {
 		boolean b = (
 			label.getNode() == this.endNode &&
 			label.getLength() >= rfParams.getDouble(RFParams.Type.MinimumLength) &&
 			label.getLength() <= rfParams.getDouble(RFParams.Type.MaximumLength)
 		);
 		// check for overlap constraint:
 		if (b && maxPSOverlap > 0) {
 			b = (label.getOverlapWithSet(results, false, maxPSOverlap) <= maxPSOverlap);	// TODO: better control of useDir!!
 			if (!b) numLabels_psOverlap++;
 		}
 
 		return b;
 	}
 
 	/**
 	 * create the expansion from a label, i.e. the list of routes leaving from the current node (next generation labels);
 	 * ignore expansion along the backEdge! (BB)
 	 * @param parentLabel the label where from to expand
 	 * @return an ArrayList containing the valid labels found for the next generation 
 	 */
 	private ArrayList<Label> expandLabel(Label parentLabel) {
 		ArrayList<Label> expansion = new ArrayList<Label>();
 		Label newLabel;
 //		if ((int)(parentLabel.getNode().getCoordinate().x/10) == 72354) {
 //			System.out.println("I'm here!");
 //		}
 
 		//double maxLen = rfParams.getDouble(RFParams.Type.MaximumLength);	// use exact estimate for maximum path length
 		double parentLength = parentLabel.getLength();
 		
 		/////////////////////////////////////
 		// MAKE LABELS FROM NEIGHBORS
 		/////////////////////////////////////			
 
 		@SuppressWarnings("unchecked")
 		List<Label> labels = (List<Label>)parentLabel.getNode().getOutEdges().getEdges();
 		if (labels.size() > 1) numExpansions++;	// size() == 1 means that the only edge is the backedge 
 		for(Object obj : labels ) {	// loop over all edges leaving from the current node
 
 			DirectedEdge currentEdge = (DirectedEdge) obj;
 			// check if the current edge is identical to the parent node, where we just came from:
 			DirectedEdge pe = parentLabel.getBackEdge();
 			if (pe != null && currentEdge.getEdge() == pe.getEdge()) continue;	// going back the same edge where we come from is prohibited!
 
 			double lastEdgeLength = ((LineMergeEdge)currentEdge.getEdge()).getLine().getLength();	// length of new last edge
 			double length = parentLength + lastEdgeLength;	// new total length
 			newLabel = new Label(parentLabel, currentEdge.getToNode(), currentEdge, length, lastEdgeLength);
 			numLabels_rejected++;	// increment by default, decrement again if label is not rejected
 			
 			/////////////////////////////////////
 			// EDGE CONSTRAINTS
 			/////////////////////////////////////
 
 //			int edgeOccurances = newLabel.getOccurancesOfEdge(currentEdge);
 
 //			// bridge has been visited the maximum number of times already?
 //			if(this.bridges.contains(currentEdge) && (edgeOccurances > getIntegerConstraint(Constraint.BridgeOverlap))) {
 //				continue;
 //			}
 
 //			// edge has been visited the maximum number of times already?
 //			if(!this.bridges.contains(currentEdge) && edgeOccurances > getIntegerConstraint(Constraint.EdgeOverlap)) {
 //				continue;
 //			}
 
 			/////////////////////////////////////
 			// PATH-LENGTH CONSTRAINTS
 			/////////////////////////////////////			
 
 			// 1. path length exceeded maxLength constraint with this edge?
 			//if (checkPathLength(length, maxLen, "length")) continue;	// don't store the label at all, because the route is too long anyway
 
 			if (maxPathLength > 0.) {	// only if we have a valid path length
 				// 2. Length + Euclidian distance to endNode greater than referencePathLength? (can't reach endNode)
 				// (Using the Euclidian distance is a quick worst-case check; using the shortest path is exact, but slower.)
 				if (checkPathLength(length + newLabel.getDistanceTo(endNode), maxPathLength, "eucl.")) continue;
 	
 				/*// 3. try with the shortest realistic path: AStar
 				// TODO: get this value from database, after it has been precalculated and stored in a table
 				// TODO: check how this performs - it might be faster to calculate more labels instead of calculating the path distance every time!!
 				if (alwaysUseShortestPath) {
 					distanceToEndNode = shortestDistanceBetweenNodes(newLabel.getNode(), endNode);
 					if (checkPathLength(length + distanceToEndNode, referencePathLength, "AStar")) continue;
 				}*/
 			}
 
 			/////////////////////////////////////
 			// NODE CONSTRAINTS
 			/////////////////////////////////////						
 
 			// rules for node occurrences: 
 			// 1) the start-node is counted except in the beginning of the route
 			// 2) the end-node is counted except if at the end of the route
 			// 3) all other occurrences are counted
 
 			// get current total overlap for node
 			int nodeOccurrences = newLabel.getOccurrencesOfNode(newLabel.getNode());
 
 			// adjust the number of time the node occours, according to cases above
 			if (newLabel.getNode() == startNode) nodeOccurrences--;
 			if (newLabel.getNode() == endNode) nodeOccurrences--;
 
 			// Now, check if a constraint is broken:
 //			if(this.articulationPoints.contains(newLabel.getNode())) {	// node is an articulation point, so we check for a special max. overlap:
 //				if (nodeOccurances > getIntegerConstraint(Constraint.ArticulationPointOverlap)) continue;
 //			} else {	// node is not an articulation point, so we have to check for max. overlap:
 				if (nodeOccurrences > rfParams.getInt(RFParams.Type.NodeOverlap)) {
 					logger.trace("Constraint reached: NodeOccurrences = " + nodeOccurrences);
 					numLabels_overlap++;
 					continue;	// don't consider this label
 				}
 //			}
 				
 			// no constraints broken:
 			newLabel.calcScore(edgeStatistics);	// shall we do this here and sort the list in findRoutes(), or shall we rather shuffle?
 			expansion.add(newLabel);	// store new label (i.e., it is a valid expansion)
 			numLabels++;
 			numLabels_rejected--;		// inelegantly reset to previous state
 		}
 		return expansion;		
 	}
 	
 	/**
 	 * check if the given minimum path length exceeds the limit, and write out a corresponding log message
 	 * @param len minimum (or real) path length
 	 * @param maxLen path length limit
 	 * @param msg additional description for log message
 	 * @return true if the path is too long
 	 */
 	private boolean checkPathLength(double len, double maxLen, String msg) {
 		boolean b = (len > maxLen);
 		if (b) {
 			logger.trace("Constraint reached: distance too large (" + msg + "). " + len + " > " + maxLen);
 		}
 		return b;
 	}
 	
 	/**
 	 * @return Get the number of Labels generated when finding routes during the last call to findRoutes()
 	 */
 	public long getNumLabels() {
 		return numLabels;
 	}
 	
 	public MatchStats getStats() { return stats; }
 	
 	/**
 	 * ??
 	 * set up SpatialIndex, which is then used to calculate nearest edges (?)
 	 */
 	public void calculateNearest() {
 		Properties p = new Properties();
 	    p.setProperty("MinNodeEntries", "1");
 	    p.setProperty("MaxNodeEntries", "10");
 		si = SpatialIndexFactory.newInstance("rtree.RTree", p);
 		counter__edge = new HashMap<Integer, Edge>();
 		int counter = 0;
 		// loop over all edges in the network
 		for (Edge edge : network.getEdges()) {
 			counter++;
 			counter__edge.put(counter, edge);	// store edges in the hash map
 			LineMergeEdge de = (LineMergeEdge)edge;
 			Geometry env = de.getLine().getBoundary();
 			//  (minx, miny), (maxx, miny), (maxx, maxy), (minx, maxy), (minx, miny).
 			
 			if (env.getNumGeometries() > 0) {			
 				Point p1 = (Point) env.getGeometryN(0);
 				Point p2 = (Point) env.getGeometryN(1);
 	
 				si.add(new Rectangle((float) p1.getX(),
 									 (float) p1.getY(), 
 									 (float) p2.getX(), 
 									 (float) p2.getY()), 
 									 counter);	
 			}
 		}
 	}
 	
 	/**
 	 * Find the edge nearest to a given point
 	 * @param p point for which to calculate the nearest edge
 	 * @return the nearest edge to the point
 	 */
 	public Edge getNearestEdge(Point p) {
 		com.infomatiq.jsi.Point pp = new com.infomatiq.jsi.Point((float) p.getX(), (float) p.getY());
 
 		ReturnArray r = new ReturnArray();
 		this.si.nearestNUnsorted(pp, r, 8, kNearestEdgeDistance);	// TODO: decide how to choose value for furthestDistance? 10 meters is just a guess.
 		double dMin = Double.MAX_VALUE, d = 0;
 		Edge e0 = null;
 		for (Integer i : r.getResult()) {
 			Edge e = (Edge) this.counter__edge.get(i);
 			d = p.distance(((LineMergeEdge)e).getLine());
 			if (d < dMin) {
 				dMin = d;
 				e0 = e;
 			}
 		}
 		return e0;
 	}
 
 }
 
 /**
  * Result container; looks like a bit of overkill, but is required by SpatialIndex...
  */
 class Return implements TIntProcedure {
 
 	private int result;
 	
 	public boolean execute(int value) {
       this.result = value;
       return true;
     }
 	
 	int getResult() {
 		return this.result;
 	}
 }
 
 /**
  * Result container; looks like a bit of overkill, but is required by SpatialIndex...
  */
 class ReturnArray implements TIntProcedure {
 
 	private ArrayList<Integer> result = new ArrayList<Integer>();
 	
 	public boolean execute(int value) {
       this.result.add(value);
       return true;
     }
 	
 	ArrayList<Integer> getResult() {
 		return this.result;
 	}
 }
 
