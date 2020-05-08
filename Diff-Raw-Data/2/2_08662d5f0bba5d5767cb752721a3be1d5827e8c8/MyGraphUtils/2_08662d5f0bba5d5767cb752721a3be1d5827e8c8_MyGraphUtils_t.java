 package it.unipd.dei.algorythms;
 
 /**
  * @author Marco Bressan
  */
 
 import it.unimi.dsi.big.webgraph.ImmutableGraph;
 import it.unimi.dsi.big.webgraph.LazyLongIterator;
 
 import java.security.InvalidAlgorithmParameterException;
 import java.util.*;
 
 public class MyGraphUtils {
     private static int verboseLevel = 0;
     private static int numOutOfBoundary=0;
     
     public static int getVerboseLevel() {
 		return verboseLevel;
 	}
 
 	public static void setVerboseLevel(int verboseLevel) {
 		MyGraphUtils.verboseLevel = verboseLevel;
 	}
 
 	/** Compute PageRank scores.
 	 * This method computes the PageRank scores of all the nodes of a given directed graph, according to the
      * specified value of the damping factor. The computation stops when either the maximum number of iterations
      * has been performed or the 1-norm of the difference between two successive PageRank vectors drops below
      * the specified threshold. The computation is performed "forwardly", i.e. at each iteration each node's 
      * score is propagated to its successors. Dangling nodes are managed separately for efficiency reasons.
      * @param G directed graph 
      * @param alpha damping factor, in (0,1)
      * @param epsilon 1-norm convergence threshold; set to 0 to disable the threshold checking.
      * @param maxIterations maximum number of iterations
      * @return an array P containing in P[i] the PageRank score of node i
      */
     public static double[] GetPageRank(ImmutableGraph G, double alpha, double epsilon, int maxIterations) throws InvalidAlgorithmParameterException {
     	if (alpha <= 0 || alpha >= 1)
     		throw new InvalidAlgorithmParameterException("The value of alpha must be between 0 and 1.");
 		int n =(int) G.numNodes();
 	    /* Create two n-entry PageRank score vectors, and initialize the first with the uniform distribution */
 		double[] oldPageRank = new double[n];
 		Arrays.fill(oldPageRank, (1d / n));
 		double[] newPageRank = new double[n];
 		/* Main loop */
 		for (int itr = 1; itr <= maxIterations; itr++) {
 		    Arrays.fill( newPageRank, 0d ); // make space for this iteration's result
 		    for (int nodeId = 0; nodeId < n; nodeId++) {
 				LazyLongIterator successors = G.successors(nodeId);
 				int outDeg =(int) G.outdegree(nodeId);
 				double invDeg = 1d/outDeg; // multiplication will be faster than division
 				while(outDeg-- != 0) {
 					int successor=(int) successors.nextLong();
 					if(successor<newPageRank.length && successor>=0){//added control
						newPageRank[successor] += alpha * oldPageRank[nodeId] * invDeg;
 					}
 					else {
 						System.err.println("["+ (++numOutOfBoundary) +"]"+successor + "will be discarded: too big, out of boundary! treating it as -1");
 					}
 				}
 		    }
 		    /* if there are dangling nodes, their contribution must be now redistributed "by hand" */
 		    double scoreSum = 0d;
 		    for (int nodeId = 0; nodeId < n; nodeId++) {
 		    	scoreSum += newPageRank[nodeId];
 		    }
 		    double sinkContrib = (alpha - scoreSum) / n; // contribution from the sinks to each node
 		    /* fix all the scores, prepare for next iteration, and verify if we should return */
 		    scoreSum = 0d;
 		    double scoreVariation = 0;
 		    for (int nodeId = 0; nodeId < n; nodeId++) { 
 		    	newPageRank[nodeId] += (1 - alpha)/n + sinkContrib; // each score's node misses (1-alpha)/n and the sinks' contribution
 				scoreVariation += Math.abs(newPageRank[nodeId] - oldPageRank[nodeId]);
 				oldPageRank[nodeId] = newPageRank[nodeId]; // prepare for next iteration
 				scoreSum += oldPageRank[nodeId];
 		    }
 		    if ( verboseLevel > 0 ) {
 		    	System.out.println( "iteration " + itr + ", scores sum up to " + ( scoreSum ) + " , delta = " + scoreVariation );
 		    }
 		    if (scoreVariation < epsilon) // exit if convergence reached
 		    	break;
 		}
 		System.err.println("Total OutOfBoundaryException discarded: "+numOutOfBoundary);
 		return oldPageRank;
     }
 	
 }
