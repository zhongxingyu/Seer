 /**
  * GDMS-Topology is a library dedicated to graph analysis. It is based on the
  * JGraphT library available at <http://www.jgrapht.org/>. It enables computing
  * and processing large graphs using spatial and alphanumeric indexes.
  *
  * This version is developed at French IRSTV Institute as part of the EvalPDU
  * project, funded by the French Agence Nationale de la Recherche (ANR) under
  * contract ANR-08-VILL-0005-01 and GEBD project funded by the French Ministry
  * of Ecology and Sustainable Development.
  *
  * GDMS-Topology is distributed under GPL 3 license. It is produced by the
  * "Atelier SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR
  * 2488.
  *
  * Copyright (C) 2009-2013 IRSTV (FR CNRS 2488)
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
  * directly: info_at_orbisgis.org
  */
 package org.gdms.gdmstopology.graphcreator;
 
 import com.vividsolutions.jts.io.ParseException;
 import org.gdms.data.DataSource;
 import org.gdms.data.DataSourceCreationException;
 import org.gdms.data.NoSuchTableException;
 import org.gdms.data.schema.DefaultMetadata;
 import org.gdms.data.types.Type;
 import org.gdms.data.types.TypeFactory;
 import org.gdms.data.values.Value;
 import org.gdms.data.values.ValueFactory;
 import org.gdms.driver.DataSet;
 import org.gdms.driver.DriverException;
 import org.gdms.driver.memory.MemoryDataSetDriver;
 import org.gdms.gdmstopology.TopologySetupTest;
 import org.gdms.gdmstopology.function.ST_Graph;
 import org.gdms.gdmstopology.model.GraphSchema;
 import org.gdms.sql.function.FunctionException;
 import org.javanetworkanalyzer.data.VBetw;
 import org.javanetworkanalyzer.data.VUBetw;
 import org.javanetworkanalyzer.data.VWBetw;
 import org.javanetworkanalyzer.model.Edge;
 import org.javanetworkanalyzer.model.KeyedGraph;
 import org.javanetworkanalyzer.model.UndirectedG;
 import org.javanetworkanalyzer.model.WeightedKeyedGraph;
 import org.junit.Test;
 import org.orbisgis.progress.NullProgressMonitor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import static org.junit.Assert.*;
 
 /**
  * Tests the graph creators under all possible configurations.
  *
  * @author Adam Gouge
  */
 public class GraphCreatorTest extends TopologySetupTest {
 
     private static final Logger LOGGER =
             LoggerFactory.getLogger(GraphCreatorTest.class);
     private static final int[] EDGE_ORIENTATIONS =
             new int[]{GraphCreator.DIRECTED_EDGE,
                       GraphCreator.REVERSED_EDGE,
                       GraphCreator.UNDIRECTED_EDGE};
     private static final double[] EDGE_WEIGHTS = new double[]{2.3, 4.2, 5.7};
     private static final double TOLERANCE = 0.0;
 
     @Test
     public void unweightedUndirected() throws Exception {
 
         KeyedGraph<VUBetw, Edge> graph =
                 new GraphCreator<VUBetw, Edge>(prepareEdges(),
                                                GraphSchema.UNDIRECT,
                                                VUBetw.class,
                                                Edge.class).prepareGraph();
 
         assertTrue(graph.vertexSet().size() == 4);
         assertTrue(graph.edgeSet().size() == 3);

         checkUndirectedOrientations(graph);
 
         for (Edge edge : graph.edgeSet()) {
             assertEquals(1.0, graph.getEdgeWeight(edge), TOLERANCE);
         }
 
         print(graph);
     }
 
     @Test
     public void unweightedDirected() throws Exception {
 
         DataSet newEdges = introduceOrientations(prepareEdges(),
                                                  EDGE_ORIENTATIONS);
         KeyedGraph<VUBetw, Edge> graph =
                 new GraphCreator<VUBetw, Edge>(newEdges,
                                                GraphSchema.DIRECT,
                                                VUBetw.class,
                                                Edge.class).prepareGraph();
 
        assertTrue(graph.vertexSet().size() == 4);
         assertTrue(graph.edgeSet().size() == 4);

         checkDirectedOrientations(graph);
 
         for (Edge edge : graph.edgeSet()) {
             assertEquals(1.0, graph.getEdgeWeight(edge), TOLERANCE);
         }
 
         print(graph);
     }
 
     @Test
     public void unweightedReversed() throws Exception {
 
         DataSet newEdges = introduceOrientations(prepareEdges(),
                                                  EDGE_ORIENTATIONS);
         KeyedGraph<VUBetw, Edge> graph =
                 new GraphCreator<VUBetw, Edge>(newEdges,
                                                GraphSchema.DIRECT_REVERSED,
                                                VUBetw.class,
                                                Edge.class).prepareGraph();
 
         assertTrue(graph.vertexSet().size() == 4);
         assertTrue(graph.edgeSet().size() == 4);

         checkReversedOrientations(graph);
 
         for (Edge edge : graph.edgeSet()) {
             assertEquals(1.0, graph.getEdgeWeight(edge), TOLERANCE);
         }
 
         print(graph);
     }
 
     @Test
     public void weightedUndirected() throws Exception {
 
         DataSet newEdges = introduceWeights(prepareEdges(),
                                             EDGE_WEIGHTS);
 
         WeightedKeyedGraph<VWBetw, Edge> graph =
                 new WeightedGraphCreator<VWBetw, Edge>(
                 newEdges,
                 GraphSchema.UNDIRECT,
                 VWBetw.class,
                 Edge.class,
                 GraphSchema.WEIGHT).prepareGraph();
 
         assertTrue(graph.vertexSet().size() == 4);
         assertTrue(graph.edgeSet().size() == 3);

         checkUndirectedOrientations(graph);
 
         assertEquals(EDGE_WEIGHTS[0],
                      graph.getEdgeWeight(graph.getEdge(graph.getVertex(1),
                                                        graph.getVertex(2))),
                      TOLERANCE);
         assertEquals(EDGE_WEIGHTS[0],
                      graph.getEdgeWeight(graph.getEdge(graph.getVertex(2),
                                                        graph.getVertex(1))),
                      TOLERANCE);
         assertEquals(EDGE_WEIGHTS[1],
                      graph.getEdgeWeight(graph.getEdge(graph.getVertex(2),
                                                        graph.getVertex(3))),
                      TOLERANCE);
         assertEquals(EDGE_WEIGHTS[1],
                      graph.getEdgeWeight(graph.getEdge(graph.getVertex(3),
                                                        graph.getVertex(2))),
                      TOLERANCE);
         assertEquals(EDGE_WEIGHTS[2],
                      graph.getEdgeWeight(graph.getEdge(graph.getVertex(3),
                                                        graph.getVertex(4))),
                      TOLERANCE);
         assertEquals(EDGE_WEIGHTS[2],
                      graph.getEdgeWeight(graph.getEdge(graph.getVertex(4),
                                                        graph.getVertex(3))),
                      TOLERANCE);
 
         print(graph);
     }
 
     @Test
     public void weightedDirected() throws Exception {
 
         DataSet newEdges =
                 introduceOrientations(introduceWeights(prepareEdges(),
                                                        EDGE_WEIGHTS),
                                       EDGE_ORIENTATIONS);
 
         WeightedKeyedGraph<VWBetw, Edge> graph =
                 new WeightedGraphCreator<VWBetw, Edge>(
                 newEdges,
                 GraphSchema.DIRECT,
                 VWBetw.class,
                 Edge.class,
                 GraphSchema.WEIGHT).prepareGraph();
 
         assertTrue(graph.vertexSet().size() == 4);
         assertTrue(graph.edgeSet().size() == 4);

         checkDirectedOrientations(graph);
 
         assertEquals(EDGE_WEIGHTS[0],
                      graph.getEdgeWeight(graph.getEdge(graph.getVertex(1),
                                                        graph.getVertex(2))),
                      TOLERANCE);
         assertEquals(EDGE_WEIGHTS[1],
                      graph.getEdgeWeight(graph.getEdge(graph.getVertex(3),
                                                        graph.getVertex(2))),
                      TOLERANCE);
         assertEquals(EDGE_WEIGHTS[2],
                      graph.getEdgeWeight(graph.getEdge(graph.getVertex(3),
                                                        graph.getVertex(4))),
                      TOLERANCE);
         assertEquals(EDGE_WEIGHTS[2],
                      graph.getEdgeWeight(graph.getEdge(graph.getVertex(4),
                                                        graph.getVertex(3))),
                      TOLERANCE);
 
         print(graph);
     }
 
     @Test
     public void weightedReversed() throws Exception {
 
         DataSet newEdges =
                 introduceOrientations(introduceWeights(prepareEdges(),
                                                        EDGE_WEIGHTS),
                                       EDGE_ORIENTATIONS);
 
         WeightedKeyedGraph<VWBetw, Edge> graph =
                 new WeightedGraphCreator<VWBetw, Edge>(
                 newEdges,
                 GraphSchema.DIRECT_REVERSED,
                 VWBetw.class,
                 Edge.class,
                 GraphSchema.WEIGHT).prepareGraph();
 
         assertTrue(graph.vertexSet().size() == 4);
         assertTrue(graph.edgeSet().size() == 4);

         checkReversedOrientations(graph);
 
         assertEquals(EDGE_WEIGHTS[0],
                      graph.getEdgeWeight(graph.getEdge(graph.getVertex(2),
                                                        graph.getVertex(1))),
                      TOLERANCE);
         assertEquals(EDGE_WEIGHTS[1],
                      graph.getEdgeWeight(graph.getEdge(graph.getVertex(2),
                                                        graph.getVertex(3))),
                      TOLERANCE);
         assertEquals(EDGE_WEIGHTS[2],
                      graph.getEdgeWeight(graph.getEdge(graph.getVertex(3),
                                                        graph.getVertex(4))),
                      TOLERANCE);
         assertEquals(EDGE_WEIGHTS[2],
                      graph.getEdgeWeight(graph.getEdge(graph.getVertex(4),
                                                        graph.getVertex(3))),
                      TOLERANCE);
 
         print(graph);
     }
 
     /**
      * Sets up a driver with geometry and gid columns ready to receive input
      * data.
      *
      * @return A newly created driver
      */
     private MemoryDataSetDriver initializeDriver() {
         final MemoryDataSetDriver data =
                 new MemoryDataSetDriver(
                 new String[]{"the_geom", "gid"},
                 new Type[]{TypeFactory.createType(Type.GEOMETRY),
                            TypeFactory.createType(Type.INT)});
         return data;
     }
 
     /**
      * Prints the given table for debugging.
      *
      * @param table The table
      *
      * @throws DriverException
      */
     private void print(DataSet table) throws DriverException {
         String metadata = "";
         for (String name : table.getMetadata().getFieldNames()) {
             metadata += name + "\t";
         }
         LOGGER.debug(metadata);
         for (int i = 0; i < table.getRowCount(); i++) {
             String row = "";
             for (Value v : table.getRow(i)) {
                 row += v + "\t";
             }
             LOGGER.debug(row);
         }
     }
 
     /**
      * Prints all edges of the graph.
      *
      * @param graph The graph.
      */
     private void print(KeyedGraph<? extends VBetw, Edge> graph) {
         LOGGER.debug("\tGRAPH");
         String leftArrow;
         if (graph instanceof UndirectedG) {
             leftArrow = "<";
         } else {
             leftArrow = "";
         }
         for (Edge edge : graph.edgeSet()) {
             LOGGER.debug("{} {}--> {} ({})",
                          graph.getEdgeSource(edge).getID(),
                          leftArrow,
                          graph.getEdgeTarget(edge).getID(),
                          graph.getEdgeWeight(edge));
         }
     }
 
     private DataSet prepareEdges() throws FunctionException, DriverException,
             DataSourceCreationException, NoSuchTableException, ParseException {
         MemoryDataSetDriver data = initializeDriver();
         data.addValues(new Value[]{
             ValueFactory.createValue(
             wktReader.read("LINESTRING(1 1, 2 2)")),
             ValueFactory.createValue(1)});
         data.addValues(new Value[]{
             ValueFactory.createValue(
             wktReader.read("LINESTRING(2 2, 2 3)")),
             ValueFactory.createValue(2)});
         data.addValues(new Value[]{
             ValueFactory.createValue(
             wktReader.read("LINESTRING(2 3, 3 3)")),
             ValueFactory.createValue(3)});
 
         DataSet[] tables = new DataSet[]{data};
 
         LOGGER.debug("\tDATA");
         print(data);
 
         // Evaluate ST_Graph.
         new ST_Graph().evaluate(dsf,
                                 tables,
                                 // Tolerance, orient by elevation, output
                                 new Value[]{ValueFactory.createValue(0),
                                             ValueFactory.createValue(false),
                                             ValueFactory.createValue("output")},
                                 new NullProgressMonitor());
 
         // Check the nodes table.
         DataSource nodes = dsf.getDataSource("output.nodes");
         nodes.open();
         LOGGER.debug("\tNODES");
         print(nodes);
         nodes.close();
 
         // Check the edges table.
         DataSource edges = dsf.getDataSource("output.edges");
         edges.open();
         LOGGER.debug("\tEDGES");
         print(edges);
 
         return edges;
     }
 
     private DataSet introduceOrientations(DataSet edges,
                                           int[] edgeOrientations)
             throws DriverException {
         DefaultMetadata newMetadata = new DefaultMetadata(edges.getMetadata());
         newMetadata.addField(GraphSchema.EDGE_ORIENTATION,
                              TypeFactory.createType(Type.INT));
         MemoryDataSetDriver newEdges =
                 new MemoryDataSetDriver(newMetadata);
         for (int i = 0; i < edges.getRowCount(); i++) {
             Value[] oldRow = edges.getRow(i);
             final Value[] newRow = new Value[newMetadata.getFieldCount()];
             System.arraycopy(oldRow, 0, newRow, 0,
                              newMetadata.getFieldCount() - 1);
             newRow[newMetadata.getFieldCount() - 1] =
                     ValueFactory.createValue(edgeOrientations[i]);
             newEdges.addValues(newRow);
         }
         LOGGER.debug("\tORIENTED EDGES");
         print(newEdges);
         return newEdges;
     }
 
     private DataSet introduceWeights(DataSet edges,
                                      double[] edgeWeights)
             throws DriverException {
         DefaultMetadata newMetadata = new DefaultMetadata(edges.getMetadata());
         newMetadata.addField(GraphSchema.WEIGHT,
                              TypeFactory.createType(Type.DOUBLE));
         MemoryDataSetDriver newEdges =
                 new MemoryDataSetDriver(newMetadata);
         for (int i = 0; i < edges.getRowCount(); i++) {
             Value[] oldRow = edges.getRow(i);
             final Value[] newRow = new Value[newMetadata.getFieldCount()];
             System.arraycopy(oldRow, 0, newRow, 0,
                              newMetadata.getFieldCount() - 1);
             newRow[newMetadata.getFieldCount() - 1] =
                     ValueFactory.createValue(edgeWeights[i]);
             newEdges.addValues(newRow);
         }
         LOGGER.debug("\tWEIGHTED EDGES");
         print(newEdges);
         return newEdges;
     }
 
     private void checkDirectedOrientations(KeyedGraph graph) {
         assertTrue(graph.containsEdge(graph.getVertex(1), graph.getVertex(2)));
         assertFalse(graph.containsEdge(graph.getVertex(2), graph.getVertex(1)));
         assertFalse(graph.containsEdge(graph.getVertex(2), graph.getVertex(3)));
         assertTrue(graph.containsEdge(graph.getVertex(3), graph.getVertex(2)));
         assertTrue(graph.containsEdge(graph.getVertex(3), graph.getVertex(4)));
         assertTrue(graph.containsEdge(graph.getVertex(4), graph.getVertex(3)));
     }
 
     private void checkUndirectedOrientations(KeyedGraph graph) {
         assertTrue(graph.containsEdge(graph.getVertex(1), graph.getVertex(2)));
         assertTrue(graph.containsEdge(graph.getVertex(2), graph.getVertex(1)));
         assertTrue(graph.containsEdge(graph.getVertex(2), graph.getVertex(3)));
         assertTrue(graph.containsEdge(graph.getVertex(3), graph.getVertex(2)));
         assertTrue(graph.containsEdge(graph.getVertex(3), graph.getVertex(4)));
         assertTrue(graph.containsEdge(graph.getVertex(4), graph.getVertex(3)));
     }
 
     private void checkReversedOrientations(KeyedGraph graph) {
         assertFalse(graph.containsEdge(graph.getVertex(1), graph.getVertex(2)));
         assertTrue(graph.containsEdge(graph.getVertex(2), graph.getVertex(1)));
         assertTrue(graph.containsEdge(graph.getVertex(2), graph.getVertex(3)));
         assertFalse(graph.containsEdge(graph.getVertex(3), graph.getVertex(2)));
         assertTrue(graph.containsEdge(graph.getVertex(3), graph.getVertex(4)));
         assertTrue(graph.containsEdge(graph.getVertex(4), graph.getVertex(3)));
     }
 }
