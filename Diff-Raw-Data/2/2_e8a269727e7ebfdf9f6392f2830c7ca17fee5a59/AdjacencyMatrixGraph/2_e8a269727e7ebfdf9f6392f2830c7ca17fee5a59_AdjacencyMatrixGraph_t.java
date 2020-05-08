 /**
  * AdjacencyMatrixGraph.java
  * 
  * Copyright 2009 Jeffrey Finkelstein
  * 
  * This file is part of jmona.
  * 
  * jmona is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * jmona is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * jmona. If not, see <http://www.gnu.org/licenses/>.
  */
 package jmona.example.ga.tsp;
 
 /**
  * An DirectedGraph backed by an adjacency matrix containing edge weights for
  * use in the traveling salesman problem evolution.
  * 
  * @author jfinkels
  */
 public class AdjacencyMatrixGraph implements DirectedGraph<Integer, Double> {
 
   /**
    * The adjacency matrix which represents this graph.
    * 
    * The row indexes the source vertex and column indexes the target vertex. For
    * example, to get the weight of the edge from vertex 1 to vertex 2, use
    * {@code adjacencyMatrix[1][2]}. To get the weight of the edge from vertex 2
    * to vertex 0, use {@code adjacencyMatrx[2][0]}.
    */
   private double[][] adjacencyMatrix = null;
 
   /**
    * Instantiate this graph with the specified adjacency matrix representation.
    * 
    * @param initialAdjacencyMatrix
    *          The adjacency matrix which represents this graph.
    */
   public AdjacencyMatrixGraph(final double[][] initialAdjacencyMatrix) {
     this.adjacencyMatrix = initialAdjacencyMatrix.clone();
   }
 
   /**
    * Get the weight of the edge between the two specified vertices, directed
    * from the source vertex to the target vertex.
    * 
    * @param sourceVertex
    *          The source vertex.
    * @param targetVertex
    *          The target vertex.
    * @return The weight of the edge incident to both specified vertices,
    *         directed from the source vertex to the target vertex.
   * @see jmona.example.ga.tsp.DirectedGraph#edgeBetween(java.lang.Object,
    *      java.lang.Object)
    */
   @Override
   public Double edgeBetween(final Integer sourceVertex,
       final Integer targetVertex) {
     return this.adjacencyMatrix[sourceVertex][targetVertex];
   }
 
 }
