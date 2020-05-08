 /**
  * GDMS-Topology is a library dedicated to graph analysis. It is based on the
  * JGraphT library available at <http://www.jgrapht.org/>. It enables computing
  * and processing large graphs using spatial and alphanumeric indexes.
  *
  * This version is developed at French IRSTV institut as part of the EvalPDU
  * project, funded by the French Agence Nationale de la Recherche (ANR) under
  * contract ANR-08-VILL-0005-01 and GEBD project funded by the French Ministery
  * of Ecology and Sustainable Development.
  *
  * GDMS-Topology is distributed under GPL 3 license. It is produced by the
  * "Atelier SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR
  * 2488.
  *
  * Copyright (C) 2009-2012 IRSTV (FR CNRS 2488)
  *
  * GDMS-Topology is free software: you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  *
  * GDMS-Topology is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * GDMS-Topology. If not, see <http://www.gnu.org/licenses/>.
  *
  * For more information, please consult: <http://wwwc.orbisgis.org/> or contact
  * directly: info_at_ orbisgis.org
  */
 package org.gdms.gdmstopology.process;
 
 import com.graphhopper.storage.Graph;
 import com.graphhopper.storage.GraphStorage;
 import com.graphhopper.storage.RAMDirectory;
 import java.util.Iterator;
 import org.gdms.data.schema.Metadata;
 import org.gdms.data.values.Value;
 import org.gdms.driver.DataSet;
 import org.gdms.driver.DriverException;
 import org.gdms.gdmstopology.model.GraphException;
 import org.gdms.gdmstopology.model.GraphSchema;
 
 /**
  * Loads a GraphHopper graph from a data set.
  *
  * <p> It is possible to load a graph with all edges of weight 1. Otherwise, it
  * is necessary to provide a String specifying the data set column containing
  * the weight of each edge.
  *
  * @author Adam Gouge
  */
 public class GraphLoaderUtilities {
 
     /**
      * Used to make sure all edges receive a weight of exactly 1 when
      * appropriate.
      */
     private final static double ALL_WEIGHTS_ONE = 1.0;
     /**
      * Used to allocate enough space for the GraphHopper graph.
      */
     // TODO: How big does this need to be?
     private final static int ALLOCATE_GRAPH_SPACE = 10;
     /**
      * An error message given when a user inputs an erroneous graph orientation.
      */
     private final static String GRAPH_TYPE_ERROR = "Only three types of graphs "
             + "are allowed: enter 1 if the graph is "
             + "directed, 2 if it is directed and you wish to reverse the "
             + "orientation of the edges, and 3 if the graph is undirected. "
             + "If no orientation is specified, the graph is assumed "
             + "to be directed.";
 
     /**
      * Create a GraphHopper graph from the data set with all weights equal to 1.
      * For now, we assume that we only store the graph in memory and have no
      * option to save it to disk for future use.
      *
      * @param dataSet   The graph data set.
      * @param graphType An integer specifying the type of graph: 1 if directed,
      *                  2 if directed and we wish to reverse the direction of the
      *                  edges, 3 if undirected.
      *
      * @return The newly generated graph.
      *
      * @throws DriverException
      * @throws GraphException
      */
    public static GraphStorage loadGraphFromDataSetAllWeightsOne(
             DataSet dataSet,
             int graphType) throws DriverException,
             GraphException {
 
         // Initialize the graph.
         GraphStorage graph = new GraphStorage(new RAMDirectory());
         graph.createNew(ALLOCATE_GRAPH_SPACE);
 
         // INSERT THE DATA INTO THE GRAPH
 
         // Recover the edge Metadata.
         // TODO: Add a check to make sure the metadata was loaded correctly.
         Metadata edgeMetadata = dataSet.getMetadata();
 
         // Recover the indices of the start node, end node, and weight.
         int startNodeIndex = edgeMetadata.getFieldIndex(GraphSchema.START_NODE);
         int endNodeIndex = edgeMetadata.getFieldIndex(GraphSchema.END_NODE);
 
         // Get an iterator on the data set.
         Iterator<Value[]> iterator = dataSet.iterator();
 
         // Add the edges according to the given graph type.
 //        System.out.print("Adding edges to ");
         if (graphType == GraphSchema.DIRECT) {
 //            System.out.println("directed graph.");
             while (iterator.hasNext()) {
                 Value[] row = iterator.next();
                 graph.edge(row[startNodeIndex].getAsInt(),
                         row[endNodeIndex].getAsInt(),
                         ALL_WEIGHTS_ONE,
                         false);
             }
         } else if (graphType == GraphSchema.DIRECT_REVERSED) {
 //            System.out.println("reversed graph.");
             while (iterator.hasNext()) {
                 Value[] row = iterator.next();
                 graph.edge(row[endNodeIndex].getAsInt(),
                         row[startNodeIndex].getAsInt(),
                         ALL_WEIGHTS_ONE,
                         false);
             }
         } else if (graphType == GraphSchema.UNDIRECT) {
 //            System.out.println("undirected graph.");
             while (iterator.hasNext()) {
                 Value[] row = iterator.next();
                 graph.edge(row[startNodeIndex].getAsInt(),
                         row[endNodeIndex].getAsInt(),
                         ALL_WEIGHTS_ONE,
                         true);
             }
         } else {
             throw new GraphException(GRAPH_TYPE_ERROR);
         }
         return graph;
     }
 
     /**
      * Create a weighted GraphHopper graph from the data set. For now, we assume
      * that we only store the graph in memory and have no option to save it to
      * disk for future use.
      *
      * @param dataSet          The graph data set.
      * @param weightColumnName The string specifying the name of the weight
      *                         column giving the weight of each edge.
      * @param graphType        An integer specifying the type of graph: 1 if
      *                         directed, 2 if directed and we wish to reverse the
      *                         direction of the edges, 3 if undirected.
      *
      * @return The newly generated graph.
      *
      * @throws DriverException
      * @throws GraphException
      */
     public static Graph loadGraphFromDataSet(
             DataSet dataSet,
             String weightColumnName,
             int graphType) throws DriverException,
             GraphException {
 
         // Initialize the graph.
         GraphStorage graph = new GraphStorage(new RAMDirectory());
         graph.createNew(ALLOCATE_GRAPH_SPACE);
 
         // INSERT THE DATA INTO THE GRAPH
 
         // Recover the edge Metadata.
         // TODO: Add a check to make sure the metadata was loaded correctly.
         Metadata edgeMetadata = dataSet.getMetadata();
 
         // Recover the indices of the start node, end node, and weight field.
         int startNodeIndex = edgeMetadata.getFieldIndex(GraphSchema.START_NODE);
         int endNodeIndex = edgeMetadata.getFieldIndex(GraphSchema.END_NODE);
         int weightFieldIndex = edgeMetadata.getFieldIndex(weightColumnName);
 
         // Get an iterator on the data set.
         Iterator<Value[]> iterator = dataSet.iterator();
 
         // Add the edges according to the given graph type.
 //        System.out.print("Adding edges to ");
         if (graphType == GraphSchema.DIRECT) {
 //            System.out.println("directed graph.");
             while (iterator.hasNext()) {
                 Value[] row = iterator.next();
                 graph.edge(row[startNodeIndex].getAsInt(),
                         row[endNodeIndex].getAsInt(),
                         row[weightFieldIndex].getAsDouble(),
                         false);
             }
         } else if (graphType == GraphSchema.DIRECT_REVERSED) {
 //            System.out.println("reversed graph.");
             while (iterator.hasNext()) {
                 Value[] row = iterator.next();
                 graph.edge(row[endNodeIndex].getAsInt(),
                         row[startNodeIndex].getAsInt(),
                         row[weightFieldIndex].getAsDouble(),
                         false);
             }
         } else if (graphType == GraphSchema.UNDIRECT) {
 //            System.out.println("undirected graph.");
             while (iterator.hasNext()) {
                 Value[] row = iterator.next();
                 graph.edge(row[startNodeIndex].getAsInt(),
                         row[endNodeIndex].getAsInt(),
                         row[weightFieldIndex].getAsDouble(),
                         true);
             }
         } else {
             throw new GraphException(GRAPH_TYPE_ERROR);
         }
         return graph;
     }
 }
