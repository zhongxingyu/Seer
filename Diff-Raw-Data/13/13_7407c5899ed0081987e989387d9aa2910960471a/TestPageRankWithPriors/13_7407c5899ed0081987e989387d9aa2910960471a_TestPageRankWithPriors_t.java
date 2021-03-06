 /*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
 package edu.uci.ics.jung.algorithms.importance;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import junit.framework.Assert;
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;

import org.apache.commons.collections15.Factory;

 import edu.uci.ics.graph.DirectedGraph;
 import edu.uci.ics.graph.Graph;
 import edu.uci.ics.graph.UndirectedGraph;
 import edu.uci.ics.graph.util.Pair;
 import edu.uci.ics.jung.algorithms.util.NumericalPrecision;
 import edu.uci.ics.jung.graph.SimpleDirectedSparseGraph;
 import edu.uci.ics.jung.graph.SimpleUndirectedSparseGraph;
 
 
 /**
  * @author Scott White
  */
 public class TestPageRankWithPriors extends TestCase {
 
 //	private Map<Integer,Number> edgeWeights;
 	private DirectedGraph<Integer,Integer> graph;
 	private Factory<Integer> edgeFactory;
 
     public static Test suite() {
         return new TestSuite(TestPageRankWithPriors.class);
     }
 
     protected void setUp() {
 //    	edgeWeights = new HashMap<Integer,Number>();
     	edgeFactory = new Factory<Integer>() {
     		int i=0;
 			public Integer create() {
 				return i++;
 			}};
     }
 
     private void addEdge(Graph G, Integer v1, Integer v2)
     {
     	Integer edge = edgeFactory.create();
     	graph.addEdge(edge, v1, v2);
 //    	edgeWeights.put(edge, weight);
     }
 
     public void testRanker() {
     	graph = new SimpleDirectedSparseGraph<Integer,Integer>();
 
     	for(int i=0; i<4; i++) {
     		graph.addVertex(i);
     	}
         addEdge(graph,0,1);
         addEdge(graph,1,2);
         addEdge(graph,2,3);
         addEdge(graph,3,0);
         addEdge(graph,2,1);
 
         Set<Integer> priors = new HashSet<Integer>();
         priors.add(2);
 
         PageRankWithPriors<Integer,Integer> ranker = 
         	new PageRankWithPriors<Integer,Integer>(graph,0.3,priors,null);
         ranker.setRemoveRankScoresOnFinalize(false);
         ranker.setMaximumIterations(500);
 
         for (Integer v : graph.getVertices()) {
             double totalSum = 0;
             for (Integer e : graph.getOutEdges(v)) {
                 Number weightVal = ranker.getEdgeWeight(e);//(Number) e.getUserDatum(ranker.getEdgeWeightKeyName());
                 totalSum += weightVal.doubleValue();
             }
             Assert.assertTrue(NumericalPrecision.equal(1.0,totalSum,.0001));
 
         }
 
         ranker.evaluate();
         
         ranker.printRankings(true, true);
 
         Assert.assertTrue(NumericalPrecision.equal(ranker.getVertexRankScore(0),0.1157,.001));
         Assert.assertTrue(NumericalPrecision.equal(ranker.getVertexRankScore(1),0.2463,.001));
         Assert.assertTrue(NumericalPrecision.equal(ranker.getVertexRankScore(2),0.4724,.001));
         Assert.assertTrue(NumericalPrecision.equal(ranker.getVertexRankScore(3),0.1653,.001));
 
     }
 
     public void test2() {
 
         UndirectedGraph<Integer,Integer> graph = 
         	new SimpleUndirectedSparseGraph<Integer,Integer>();
         for(int i=0; i<10; i++) {
         	graph.addVertex(i);
         }
         graph.addEdge(edgeFactory.create(), 0, 1);
         graph.addEdge(edgeFactory.create(), 1, 2);
         graph.addEdge(edgeFactory.create(), 2, 3);
         graph.addEdge(edgeFactory.create(), 3, 0);
         graph.addEdge(edgeFactory.create(), 4, 5);
         graph.addEdge(edgeFactory.create(), 5, 6);
         graph.addEdge(edgeFactory.create(), 6, 7);
 
         DirectedGraph<Integer,Integer> dg = new SimpleDirectedSparseGraph<Integer,Integer>();
         for(Integer v : graph.getVertices()) {
         	dg.addVertex(v);
         }
         for(Integer e : graph.getEdges()) {
         	Pair<Integer> ep = graph.getEndpoints(e);
         	dg.addEdge(e, ep.getFirst(), ep.getSecond());
         }
 
         Set<Integer> priors = new HashSet<Integer>();
         priors.add(2);
         priors.add(3);
 
         PageRankWithPriors<Integer,Integer> ranker = 
         	new PageRankWithPriors<Integer,Integer>(dg, 0.3, priors, null);
         ranker.setRemoveRankScoresOnFinalize(false);
         ranker.setMaximumIterations(500);
 
         for (Integer v : dg.getVertices()) {
             double totalSum = 0;
             for (Integer e : graph.getOutEdges(v)) {
                 Number weightVal = ranker.getEdgeWeight(e);
                 totalSum += weightVal.doubleValue();
             }
 //            Assert.assertTrue(NumericalPrecision.equal(1.0,totalSum,.0001));
 
         }
 
         ranker.evaluate();
         //ranker.printRankings(true,true);
     }
 }
