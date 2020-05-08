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
 package org.gdms.gdmstopology.function;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import org.gdms.data.DataSourceFactory;
 import org.gdms.data.schema.DefaultMetadata;
 import org.gdms.data.schema.Metadata;
 import org.gdms.data.types.Type;
 import org.gdms.data.types.TypeFactory;
 import org.gdms.data.values.Value;
 import org.gdms.data.values.ValueFactory;
 import org.gdms.driver.DataSet;
 import org.gdms.driver.DiskBufferDriver;
 import org.gdms.driver.DriverException;
 import org.gdms.gdmstopology.graphcreator.WeightedGraphCreator;
 import org.gdms.gdmstopology.model.GraphSchema;
 import org.gdms.gdmstopology.parse.GraphFunctionParser;
 import org.gdms.gdmstopology.utils.ArrayConcatenator;
 import org.gdms.sql.function.FunctionException;
 import org.gdms.sql.function.ScalarArgument;
 import org.gdms.sql.function.table.AbstractTableFunction;
 import org.gdms.sql.function.table.TableArgument;
 import org.gdms.sql.function.table.TableDefinition;
 import org.gdms.sql.function.table.TableFunctionSignature;
 import org.javanetworkanalyzer.alg.Dijkstra;
 import org.javanetworkanalyzer.data.VWBetw;
 import org.javanetworkanalyzer.model.Edge;
 import org.javanetworkanalyzer.model.KeyedGraph;
 import org.orbisgis.progress.ProgressMonitor;
 import org.slf4j.LoggerFactory;
 
 /**
  * Function for calculating distances (shortest path lengths).
  *
  * @author Adam Gouge
  */
 public class ST_ShortestPathLength extends AbstractTableFunction {
 
     /**
      * The name of this function.
      */
     private static final String NAME = "ST_ShortestPathLength";
     public static final String SOURCE = "source";
     public static final String DESTINATION = "destination";
     public static final String DISTANCE = "distance";
     public static final String DIRECTED = "directed";
     public static final String REVERSED = "reversed";
     public static final String UNDIRECTED = "undirected";
     public static final String EDGE_ORIENTATION_COLUMN = "edge_orientation_column";
    public static final String POSSIBLE_ORIENTATIONS =
             "[, '" + DIRECTED + " - " + EDGE_ORIENTATION_COLUMN + "' "
             + "| '" + REVERSED + " - " + EDGE_ORIENTATION_COLUMN + "' "
             + "| '" + UNDIRECTED + "']";
     public static final String SEPARATOR = "-";
     /**
      * The SQL order of this function.
      */
     private static final String SQL_ORDER =
             "-- Compute the distance from " + SOURCE
             + " to " + DESTINATION + "."
             + "\n(1) SELECT * FROM " + NAME + "("
             + "output.edges, "
             + "source, destination"
             + "[, 'weights_column']"
             + POSSIBLE_ORIENTATIONS + ");"
             + "\n-- Compute the distance from " + SOURCE
             + " to all reachable nodes."
             + "\n(2) SELECT * FROM " + NAME + "("
             + "output.edges, "
             + "source"
             + "[, 'weights_column']"
             + POSSIBLE_ORIENTATIONS + ");"
             + "\n-- Compute the distances from the " + SOURCE + "s to the "
             + DESTINATION + "s in the source-destination table."
             + "\n(3) SELECT * FROM " + NAME + "("
             + "output.edges, "
             + "source_dest_table"
             + "[, 'weights_column']"
             + POSSIBLE_ORIENTATIONS + ");";
     /**
      * Short description of this function.
      */
     private static final String SHORT_DESCRIPTION =
             "Calculates the distance from one or more sources to one or more "
             + "targets.";
     /**
      * Long description of this function.
      */
     private static final String LONG_DESCRIPTION =
             "<p><i>Note</i>: This function use Dijkstra's algorithm to "
             + "calculate the shortest path lengths from one or more sources "
             + "to one or more targets. We assume the graph is connected. "
             + "<p> Example usage: "
             + "<center> "
             + "<code>SELECT * FROM ST_Distance("
             + "edges, "
             + "source_dest_table | source[, destination]"
             + "[, 'weights_column']"
             + "[, orientation]);</code> </center> "
             + "<p> Required parameters: "
             + "<ul> "
             + "<li> <code>output.edges</code> - the input table. Specifically, "
             + "this is the <code>output.edges</code> table "
             + "produced by <code>ST_Graph</code>, with an additional "
             + "column specifying the weight of each edge. "
             + "<li> <code>source_dest_table OR source[, destination]</code> - "
             + "The user may specify "
             + "<ul> <li> a source and a destination"
             + "<li> a single source (implicitly towards all possible "
             + "destinations"
             + "<li> a table of sources (under column '" + SOURCE + "') "
             + "and destinations (under column '" + DESTINATION + "') </ul></ul>"
             + "<p> Optional parameters: "
             + "<ul> "
             + "<li> <code>'weights_column'</code> - a string specifying "
             + "the name of the column of the input table that gives the weight "
             + "of each edge. If omitted, the graph is considered to be unweighted. "
             + "<li> <code>orientation</code> - a string specifying the "
             + "orientation of the graph: "
             + "<ul> "
             + "<li> '" + DIRECTED + " - " + EDGE_ORIENTATION_COLUMN + "' "
             + "<li> '" + REVERSED + " - " + EDGE_ORIENTATION_COLUMN + "' "
             + "<li> '" + UNDIRECTED + "'."
             + "</ul> The default orientation is " + DIRECTED + " with edge "
             + "orientations given by the geometries, though edge orientations "
             + "should most definitely be provided by the user. </ul>";
     /**
      * Description of this function.
      */
     private static final String DESCRIPTION =
             SHORT_DESCRIPTION + LONG_DESCRIPTION;
     /**
      * Source node id.
      */
     private int source = -1;
     /**
      * Destination node id.
      */
     private int destination = -1;
     /**
      * Table of sources and destinations.
      */
     private DataSet sourceDestinationTable = null;
     /**
      * Weight column name.
      */
     private String weightsColumn = null;
     /**
      * Global orientation string.
      */
     private String globalOrientation = null;
     /**
      * Edge orientation string.
      */
     private String edgeOrientationColumnName = null;
     /**
      * Output metadata.
      */
     private static final Metadata md = new DefaultMetadata(
             new Type[]{TypeFactory.createType(Type.INT),
                        TypeFactory.createType(Type.INT),
                        TypeFactory.createType(Type.DOUBLE)},
             new String[]{SOURCE,
                          DESTINATION,
                          DISTANCE});
     /**
      * Logger.
      */
     private static final org.slf4j.Logger LOGGER =
             LoggerFactory.getLogger(ST_ShortestPathLength.class);
 
     @Override
     public DataSet evaluate(DataSourceFactory dsf, DataSet[] tables,
                             Value[] values, ProgressMonitor pm) throws
             FunctionException {
 
         // Recover the edges.
         final DataSet edges = tables[0];
 
         // Recover all other parameters.
         parseArguments(edges, tables, values);
 
         // Prepare the graph.
         KeyedGraph<VWBetw, Edge> graph = prepareGraph(edges);
 
         // Compute and return results.
         DiskBufferDriver results = null;
         try {
             results = compute(dsf, graph);
         } catch (DriverException ex) {
             LOGGER.error(ex.toString());
         }
         return results;
     }
 
     @Override
     public String getName() {
         return NAME;
     }
 
     /**
      * Returns an example query using this function.
      *
      * @return An example query using this function.
      */
     @Override
     public String getSqlOrder() {
         return SQL_ORDER;
     }
 
     /**
      * Returns a description of this function.
      *
      * @return A description of this function.
      */
     @Override
     public String getDescription() {
         return DESCRIPTION;
     }
 
     /**
      * Returns an array of all possible signatures of this function. Multiple
      * signatures arise from some arguments being optional.
      *
      * @return An array of all possible signatures of this function.
      */
     @Override
     public TableFunctionSignature[] getFunctionSignatures() {
         return ArrayConcatenator.
                 concatenate(sourceDestinationSignatures(),
                             sourceSignatures(),
                             sourceDestinationTableSignatures());
     }
 
     /**
      * Returns all possible function signatures for finding all distances from a
      * given source.
      *
      * @return Source signatures
      */
     private TableFunctionSignature[] sourceSignatures() {
         return new TableFunctionSignature[]{
             // (s)
             new TableFunctionSignature(TableDefinition.GEOMETRY,
                                        ScalarArgument.INT),
             // (s,w) OR (s,o)
             new TableFunctionSignature(TableDefinition.GEOMETRY,
                                        ScalarArgument.INT,
                                        ScalarArgument.STRING),
             // (s,w,o) OR (s,o,w)
             new TableFunctionSignature(TableDefinition.GEOMETRY,
                                        ScalarArgument.INT,
                                        ScalarArgument.STRING,
                                        ScalarArgument.STRING)
         };
     }
 
     /**
      * Returns all possible function signatures for finding the distance from a
      * given source to a given destination.
      *
      * @return Source-destination signatures
      */
     private TableFunctionSignature[] sourceDestinationSignatures() {
         return new TableFunctionSignature[]{
             // (s,d)
             new TableFunctionSignature(TableDefinition.GEOMETRY,
                                        ScalarArgument.INT,
                                        ScalarArgument.INT),
             // (s,d,w) OR (s,d,o)
             new TableFunctionSignature(TableDefinition.GEOMETRY,
                                        ScalarArgument.INT,
                                        ScalarArgument.INT,
                                        ScalarArgument.STRING),
             // (s,d,w,o) OR (s,d,o,w)
             new TableFunctionSignature(TableDefinition.GEOMETRY,
                                        ScalarArgument.INT,
                                        ScalarArgument.INT,
                                        ScalarArgument.STRING,
                                        ScalarArgument.STRING)
         };
     }
 
     /**
      * Returns all possible function signatures for finding the distances from
      * the given sources to the given destinations.
      *
      * @return Source-destination signatures
      */
     private TableFunctionSignature[] sourceDestinationTableSignatures() {
         return new TableFunctionSignature[]{
             // (s_d_t)
             new TableFunctionSignature(TableDefinition.GEOMETRY,
                                        TableArgument.ANY),
             // (s_d_t,w) OR (s_d_t,o)
             new TableFunctionSignature(TableDefinition.GEOMETRY,
                                        TableArgument.ANY,
                                        ScalarArgument.STRING),
             // (s_d_t,w,o) OR (s_d_t,o,w)
             new TableFunctionSignature(TableDefinition.GEOMETRY,
                                        TableArgument.ANY,
                                        ScalarArgument.STRING,
                                        ScalarArgument.STRING)
         };
     }
 
     @Override
     public Metadata getMetadata(Metadata[] tables) throws DriverException {
         return md;
     }
 
     /**
      * Parse all possible arguments for {@link ST_ShortestPathLength}.
      *
      * @param tables Input table(s)
      * @param values Arguments
      */
     private void parseArguments(DataSet edges, DataSet[] tables, Value[] values) {
         // (source_dest_table, ...)
         if (tables.length == 2) {
             sourceDestinationTable = tables[1];
             parseOptionalArguments(edges, values, 0);
         } else {
             source = GraphFunctionParser.parseSource(values[0]);
             if (values.length > 1) {
                 // (source, destination, ...)
                 if (values[1].getType() == Type.INT) {
                     destination = GraphFunctionParser.parseTarget(values[1]);
                     parseOptionalArguments(edges, values, 2);
                 } // (source, ...)
                 else {
                     parseOptionalArguments(edges, values, 1);
                 }
             }
         }
     }
 
     /**
      * Parse the optional arguments.
      *
      * @param values   Arguments array
      * @param argIndex Index of the first optional argument
      */
     private void parseOptionalArguments(DataSet edges, Value[] values,
                                         int argIndex) {
         if (values.length > argIndex) {
             while (values.length > argIndex) {
                 parseStringArguments(edges, values[argIndex++]);
             }
         }
     }
 
     /**
      * Parse possible String arguments for {@link ST_ShortestPathLength}, namely
      * weight and orientation.
      *
      * @param value A given argument to parse.
      */
     private void parseStringArguments(DataSet edges, Value value) {
         if (value.getType() == Type.STRING) {
             String v = value.getAsString();
             // See if this is a directed (or reversed graph.
             if ((v.toLowerCase().contains(DIRECTED)
                  && !v.toLowerCase().contains(UNDIRECTED))
                 || v.toLowerCase().contains(REVERSED)) {
                 if (!v.contains(SEPARATOR)) {
                     throw new IllegalArgumentException(
                             "You must specify the name of the edge orientation "
                             + "column. Enter '" + DIRECTED + " " + SEPARATOR
                             + " " + EDGE_ORIENTATION_COLUMN + "' or '"
                             + REVERSED + " " + SEPARATOR + " "
                             + EDGE_ORIENTATION_COLUMN + "'.");
                 } else {
                     // Extract the global and edge orientations.
                     String[] globalAndEdgeOrientations = v.split(SEPARATOR);
                     if (globalAndEdgeOrientations.length == 2) {
                         // And remove whitespace.
                         globalOrientation = globalAndEdgeOrientations[0]
                                 .replaceAll("\\s", "");
                         edgeOrientationColumnName = globalAndEdgeOrientations[1]
                                 .replaceAll("\\s", "");
                         try {
                             // Make sure this column exists.
                             if (!Arrays.asList(edges.getMetadata()
                                     .getFieldNames())
                                     .contains(edgeOrientationColumnName)) {
                                 throw new IllegalArgumentException(
                                         "Column '" + edgeOrientationColumnName
                                         + "' not found in the edges table.");
                             }
                         } catch (DriverException ex) {
                             LOGGER.error("Problem verifying existence of "
                                          + "column {}.",
                                          edgeOrientationColumnName);
                         }
                         LOGGER.info(
                                 "Global orientation = '{}', edge orientation "
                                 + "column name = '{}'.", globalOrientation,
                                 edgeOrientationColumnName);
                         // TODO: Throw an exception if no edge orientations are given.
                     } else {
                         throw new IllegalArgumentException(
                                 "You must specify both global and edge orientations for "
                                 + "directed or reversed graphs. Separate them by "
                                 + "a '" + SEPARATOR + "'.");
                     }
                 }
             } else if (v.toLowerCase().contains(UNDIRECTED)) {
                 globalOrientation = UNDIRECTED;
                 if (!v.equalsIgnoreCase(UNDIRECTED)) {
                     LOGGER.warn("Edge orientations are ignored for undirected "
                                 + "graphs.");
                 }
             } else {
                 LOGGER.info("Weights column name = '{}'.", v);
                 weightsColumn = v;
             }
         } else {
             throw new IllegalArgumentException("Weights and orientation "
                                                + "must be specified as strings.");
         }
     }
 
     /**
      * Prepare the JGraphT graph from the given edges table.
      *
      * @param edges Edges table
      *
      * @return JGraphT graph
      */
     private KeyedGraph<VWBetw, Edge> prepareGraph(final DataSet edges) {
         KeyedGraph<VWBetw, Edge> graph;
 
         // Get the graph orientation.
         int graphType = -1;
         if (globalOrientation != null) {
             graphType = globalOrientation.equalsIgnoreCase(DIRECTED)
                     ? GraphSchema.DIRECT
                     : globalOrientation.equalsIgnoreCase(REVERSED)
                     ? GraphSchema.DIRECT_REVERSED
                     : globalOrientation.equalsIgnoreCase(UNDIRECTED)
                     ? GraphSchema.UNDIRECT
                     : -1;
         } else if (graphType == -1) {
             LOGGER.warn("Assuming a directed graph.");
             graphType = GraphSchema.DIRECT;
         }
 
         // Create the graph.
         if (weightsColumn != null) {
             graph = new WeightedGraphCreator<VWBetw, Edge>(
                     edges,
                     graphType,
                     edgeOrientationColumnName,
                     VWBetw.class,
                     Edge.class,
                     weightsColumn).prepareGraph();
         } else {
             throw new UnsupportedOperationException(
                     "ST_Distance has not yet been implemented for "
                     + "unweighted graphs.");
         }
         return graph;
     }
 
     /**
      * Compute the distances and write them to a table.
      *
      * @param dsf   Data source factory
      * @param graph JGraphT graph
      *
      * @return The requested distances
      *
      * @throws DriverException
      */
     private DiskBufferDriver compute(DataSourceFactory dsf,
                                      KeyedGraph<VWBetw, Edge> graph)
             throws DriverException {
 
         // Initialize the output.
         DiskBufferDriver output = new DiskBufferDriver(dsf, getMetadata(null));
 
         if (graph == null) {
             LOGGER.error("Null graph.");
         } else {
             // Get a Dijkstra algo for the distance calculation.
             Dijkstra<VWBetw, Edge> dijkstra = new Dijkstra<VWBetw, Edge>(graph);
 
             // (source, destination, ...) (One-to-one)
             if (source != -1 && destination != -1) {
                 double distance =
                         dijkstra.oneToOne(graph.getVertex(source),
                                           graph.getVertex(destination));
                 storeValue(source, destination, distance, output);
             } // (source, ...) (One-to-ALL)
             else if (source != -1 && destination == -1) {
                 // TODO: Replace this by calculate().
                 Map<VWBetw, Double> distances =
                         dijkstra.oneToMany(graph.getVertex(source),
                                            graph.vertexSet());
                 storeValues(source, distances, output);
             } // (source_dest_table, ...) (Many-to-many)
             else if (sourceDestinationTable != null) {
                 // Make sure the source-destination table has columns named
                 // SOURCE and DESTINATION.
                 Metadata metadata = sourceDestinationTable.getMetadata();
                 int sourceIndex = metadata.getFieldIndex(SOURCE);
                 int targetIndex = metadata.getFieldIndex(DESTINATION);
                 if (sourceIndex == -1) {
                     throw new IllegalArgumentException(
                             "The source-destination table must contain "
                             + "a column named \'" + SOURCE + "\'.");
                 } else if (targetIndex == -1) {
                     throw new IllegalArgumentException(
                             "The source-destination table must contain "
                             + "a column named \'" + DESTINATION + "\'.");
                 } else {
 
                     // Prepare the source-destination map from the source-
                     // destination table.
                     Map<VWBetw, Set<VWBetw>> sourceDestinationMap =
                             prepareSourceDestinationMap(graph,
                                                         sourceIndex,
                                                         targetIndex);
                     if (sourceDestinationMap.isEmpty()) {
                         LOGGER.error(
                                 "No sources/destinations requested.");
                     }
 
                     // Do One-to-Many many times!
                     for (Entry<VWBetw, Set<VWBetw>> e
                          : sourceDestinationMap.entrySet()) {
                         Map<VWBetw, Double> distances =
                                 dijkstra.oneToMany(e.getKey(), e.getValue());
                         storeValues(e.getKey().getID(), distances, output);
                     }
                 }
             }
             // Clean-up
             output.writingFinished();
             output.open();
         }
         return output;
     }
 
     /**
      * Prepare the source-destination map (to which we will apply Dijkstra) from
      * the source-destination table.
      *
      * @param graph            JGraphT graph
      * @param sourceIndex      Index of the source column
      * @param destinationIndex Index of the destination column.
      *
      * @return The source-destination map
      *
      * @throws DriverException
      */
     private Map<VWBetw, Set<VWBetw>> prepareSourceDestinationMap(
             KeyedGraph<VWBetw, Edge> graph,
             int sourceIndex,
             int destinationIndex) throws DriverException {
         // Initialize the map.
         Map<VWBetw, Set<VWBetw>> map =
                 new HashMap<VWBetw, Set<VWBetw>>();
         // Go throught the source-destination table and insert each
         // pair into the map.
         for (int i = 0;
              i < sourceDestinationTable.getRowCount();
              i++) {
             Value[] row = sourceDestinationTable.getRow(i);
 
             VWBetw sourceVertex = graph.getVertex(
                     row[sourceIndex].getAsInt());
             VWBetw destinationVertex = graph.getVertex(
                     row[destinationIndex].getAsInt());
 
             Set<VWBetw> targets = map.get(sourceVertex);
             // Lazy initialize if the destinations set is null.
             if (targets == null) {
                 targets = new HashSet<VWBetw>();
                 map.put(sourceVertex, targets);
             }
             // Add the destination.
             targets.add(destinationVertex);
         }
         return map;
     }
 
     /**
      * Store the distances from the given source to each destination contained
      * in the map mapping each destination to its distance from the source.
      *
      * @param source Source
      * @param map    Distances map
      * @param output Driver
      *
      * @throws DriverException
      */
     private void storeValues(int source,
                              Map<VWBetw, Double> map,
                              DiskBufferDriver output) throws DriverException {
         for (Entry<VWBetw, Double> e : map.entrySet()) {
             storeValue(source, e.getKey().getID(), e.getValue(), output);
         }
     }
 
     /**
      * Store the distance from the given source to the given destination in the
      * given driver.
      *
      * @param source      Source
      * @param destination Destination
      * @param distance    d(source, destination)
      * @param output      Driver
      *
      * @throws DriverException
      */
     private void storeValue(int source, int destination, double distance,
                             DiskBufferDriver output) throws DriverException {
         output.addValues(ValueFactory.createValue(source),
                          ValueFactory.createValue(destination),
                          ValueFactory.createValue(distance));
     }
 }
