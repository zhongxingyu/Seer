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
 package org.gdms.gdmstopology.function;
 
 import java.util.Arrays;
 import org.gdms.data.DataSourceFactory;
 import org.gdms.data.values.Value;
 import org.gdms.driver.DataSet;
 import org.gdms.sql.function.FunctionException;
 import org.gdms.sql.function.FunctionSignature;
 import org.gdms.sql.function.ScalarArgument;
 import org.gdms.sql.function.executor.AbstractExecutorFunction;
 import org.gdms.sql.function.executor.ExecutorFunctionSignature;
 import org.gdms.sql.function.table.TableArgument;
 import org.gdms.sql.function.table.TableDefinition;
 import org.orbisgis.progress.ProgressMonitor;
 import org.gdms.data.types.Type;
 import org.gdms.driver.DriverException;
 import org.gdms.gdmstopology.model.GraphException;
 import org.gdms.gdmstopology.model.GraphSchema;
 import org.gdms.gdmstopology.process.GraphCentralityUtilities;
 
 /**
  * Calculates the closeness centrality indices of all nodes of a given graph.
  *
  * <p><i>Note</i>: This function uses the
  * <code>jgrapht-sna</code> implementation of Freeman closeness centrality,
  * which uses the Floyd-Warshall algorithm to calculate all possible shortest
  * paths. The calculations are intense and can take a long time to complete.
  *
  * <p> For the moment, only Freeman closeness centrality with all weights equal
  * to 1 has been implemented.
  *
  * <p> Example usage: <center> {@code SELECT * FROM ST_ClosenessCentrality(
  * input_table,
  * 'weights_column'
  * [,'output_table_prefix',
  * orientation]);} </center> or: <center> {@code SELECT * FROM ST_ClosenessCentrality(
  * input_table,
  * 1
  * [,'output_table_prefix',
  * orientation]);} </center>
  *
  * <p> Required parameters: <ul> <li> {@code input_table} - the input table.
  * Specifically, this is the {@code output_table_prefix.edges} table produced by
  * {@link ST_Graph}, except that an additional column specifying the weight of
  * each edge must be added (this is the 'weights_column'). <li>
  * {@code 'weights_column'} - a string specifying the name of the column of the
  * input table that gives the weight of each edge. </ul>
  *
  * <p> Optional parameters: <ul> <li> {@code 'output_table_prefix'} - a string
  * used to prefix the name of the output table. <li> {@code orientation} - an
  * integer specifying the orientation of the graph: <ul> <li> 1 if the graph is
  * directed, <li> 2 if it is directed and we wish to reverse the orientation of
  * the edges. </ul> If no orientation is specified, we assume the graph is
  * directed. </ul>
  *
  * @author Adam Gouge
  */
 public class ST_ClosenessCentrality extends AbstractExecutorFunction {
 
     // OPTIONAL ARGUMENTS
     /**
      * Specifies the orientation of the graph.
      */
     private int orientation = -1;
     /**
      * Specifies the prefix of the output table name.
      */
     private String outputTablePrefix;
     /**
      *
      */
     private String weightsColumn;
     /**
      * Default output table prefix;
      */
     private static final String DEFAULT_OUTPUT_TABLE_PREFIX = "output";
 
     /**
      * Evaluates the function to calculate the centrality indices.
      *
      * @param dsf    The {@link DataSourceFactory} used to parse the data set.
      * @param tables The input table. (This {@link DataSet} array will contain
      *               only one element since there is only one input table.)
      * @param values Array containing the other arguments.
      * @param pm     The progress monitor used to track the progress of the
      *               shortest path calculation.
      *
      * @return The {@link DataSet} containing the shortest path.
      *
      * @throws FunctionException
      */
     @Override
     public void evaluate(
             DataSourceFactory dsf,
             DataSet[] tables,
             Value[] values,
             ProgressMonitor pm)
             throws FunctionException {
         try {
             // REQUIRED PARAMETERS
 
             // FIRST PARAMETER: Recover the DataSet. There is only one table.
             final DataSet dataSet = tables[0];
 
             // SECOND PARAMETER: Either 1 or weight column name.
             parseRequiredArgument(values);
 
             // OPTIONAL PARAMETERS
             parseOptionalArguments(values);
 
             // Take all user-entered values into account when calculating
             // closeness centrality.
             registerClosenessAccordingToUserInput(dsf, dataSet, pm);
 
         } catch (Exception ex) {
             System.out.println(ex);
             throw new FunctionException(
                     "Cannot compute the closeness centrality indices.", ex);
         }
     }
 
     private void registerClosenessAccordingToUserInput(
             DataSourceFactory dsf,
             DataSet dataSet,
             ProgressMonitor pm) throws GraphException, DriverException {
        System.out.print("\n Orientation: ");
         if (weightsColumn == null) {
             if (orientation == -1) {
                 System.out.println("not specified.");
                 // We assume the graph is directed if no orientation
                 // is specified.
                 System.out.print("Output table prefix: ");
                 if (outputTablePrefix == null) {
                     System.out.println("not specified. \n");
                     // Use the default output table prefix if no prefix
                     // is specified.
                     GraphCentralityUtilities.
                             registerClosenessCentralityIndicesAllWeightsOne(
                             dsf,
                             dataSet,
                             DEFAULT_OUTPUT_TABLE_PREFIX,
                             GraphSchema.DIRECT,
                             pm);
                 } else {
                     System.out.println(outputTablePrefix + ". \n");
                     // Use the user-defined output table prefix.
                     GraphCentralityUtilities.
                             registerClosenessCentralityIndicesAllWeightsOne(
                             dsf,
                             dataSet,
                             outputTablePrefix,
                             GraphSchema.DIRECT,
                             pm);
                 }
             } else {
                 System.out.println(orientation + ".");
                 System.out.print("Output table prefix: ");
                 // Use the user-defined orientation.
                 if (outputTablePrefix == null) {
                     System.out.println("not specified. \n");
                     // Use the default output table prefix if no prefix
                     // is specified.
                     GraphCentralityUtilities.
                             registerClosenessCentralityIndicesAllWeightsOne(
                             dsf,
                             dataSet,
                             DEFAULT_OUTPUT_TABLE_PREFIX,
                             orientation,
                             pm);
                 } else {
                     System.out.println(outputTablePrefix + ". \n");
                     // Use the user-defined output table prefix.
                     GraphCentralityUtilities.
                             registerClosenessCentralityIndicesAllWeightsOne(
                             dsf,
                             dataSet,
                             outputTablePrefix,
                             orientation,
                             pm);
                 }
             }
         } else {
             throw new UnsupportedOperationException("For now, "
                     + "closeness centrality may only be calculated "
                     + "for all weights equal to 1.");
         }
     }
 
     private boolean orientationIsValid() {
         final Integer[] possibleOrientations = new Integer[]{1, 2, 3};
         if (Arrays.asList(possibleOrientations).
                 contains(orientation)) {
             return true;
         }
         return false;
     }
 
     private void parseRequiredArgument(Value[] values) throws FunctionException {
         final int secondSlotType = values[0].getType();
         if (secondSlotType == Type.INT) {
             final int allWeightsOneInt = values[0].getAsInt();
             if (allWeightsOneInt == 1) {
                 System.out.println("Setting all weights equal to 1.");
             } else { // We only accept 1 or a string.
                 throw new FunctionException(
                         "Either enter 1 to set all weights equal to 1 "
                         + "or specify the name of the weight column.");
             }
         } else if (secondSlotType == Type.STRING) {
             // Set the weights column name.
             weightsColumn = values[0].getAsString();
             System.out.println("Setting the weight column name "
                     + "to be \'" + weightsColumn
                     + "\'.");
         } else {
             throw new FunctionException(
                     "Either enter 1 to set all weights equal to 1 "
                     + "or specify the name of the weight column.");
         }
     }
 
     private void parseOptionalArgument(Value[] values, int index) throws
             FunctionException {
         final int slotType = values[index].getType();
         if (slotType == Type.INT) {
             orientation = values[index].getAsInt();
             System.out.println("Setting the orientation "
                     + "to be " + orientation + ".");
             if (!orientationIsValid()) {
                 throw new FunctionException(
                         "Please enter a valid orientation.");
             }
         } else if (slotType == Type.STRING) {
             // Set the output table prefix.
             outputTablePrefix = values[index].getAsString();
             System.out.println("Setting the output table prefix "
                     + "to be \'" + outputTablePrefix
                     + "\'.");
         } else {
             throw new FunctionException(
                     "Enter a valid orientation and/or output "
                     + "table prefix.");
         }
     }
 
     private void parseOptionalArguments(Value[] values) throws FunctionException {
         // If there is at least one optional parameter, then get
         // the first one.
         if (values.length > 1) {
             parseOptionalArgument(values, 1);
             // If there are two optional parameters, then get the second one.
             if (values.length > 2) {
                 parseOptionalArgument(values, 2);
             }
         }
     }
 
     /**
      * Returns the name of this function. This name will be used in SQL
      * statements.
      *
      * @return The name of this function.
      */
     @Override
     public String getName() {
         return "ST_ClosenessCentrality";
     }
 
     /**
      * Returns an example query using this function.
      *
      * @return An example query using this function.
      */
     @Override
     public String getSqlOrder() {
         return "EXECUTE ST_ClosenessCentrality("
                 + "input_table, "
                 + "'weights_column', "
                 + "'output_table_prefix'"
                 + "[,orientation]);";
     }
 
     /**
      * Returns a description of this function.
      *
      * @return A description of this function.
      */
     // TODO: Make the output_table_prefix parameter optional.
     @Override
     public String getDescription() {
         return "Calculates the closeness centrality indices of all nodes of a "
                 + "given graph. "
                 + "<p><i>Note</i>: This function uses the "
                 + "<code>jgrapht-sna</code> implementation of Freeman "
                 + "closeness centrality, which uses the Floyd-Warshall "
                 + "algorithm to calculate all possible shortest paths. "
                 + "The calculations are intense and can take a long time "
                 + "to complete."
                 + "<p> Required parameters: <ul> "
                 + "<li> <code>input_table</code> "
                 + "- the input table. Specifically, this is the "
                 + "<code>output_table_prefix.edges</code> table produced by "
                 + "<code>ST_Graph</code>, except that an additional column "
                 + "specifying the weight of each edge must be added "
                 + "(this is the <code>'weights_column'</code>). "
                 + "<li> <code>'weights_column'</code> - a string specifying the "
                 + "name of the column of the input table that gives the weight "
                 + "of each edge. "
                 + "<li> <code>'output_table_prefix'</code> - a string used to prefix "
                 + "the name of the output table. </ul>"
                 + "<p> Optional parameters: <ul> "
                 + "<li> <code>orientation</code> - an integer specifying the "
                 + "orientation of the graph: <ul> "
                 + "<li> 1 if the graph is directed, "
                 + "<li> 2 if it is directed and we wish to reverse the "
                 + "orientation of the edges, "
                 + "<li> 3 if the graph is undirected."
                 + " </ul> "
                 + "If no orientation is specified, we assume the graph is"
                 + "directed. </ul> ";
     }
 
 //    /**
 //     * Returns the {@link Metadata} of the result of this function without
 //     * executing the query.
 //     *
 //     * @param tables {@link Metadata} objects of the input tables.
 //     * @return The {@link Metadata} of the result.
 //     * @throws DriverException
 //     */
 //    @Override
 //    public Metadata getMetadata(Metadata[] tables) throws DriverException {
 //        return GraphMetadataFactory.createClosenessCentralityMetadata();
 //    }
     /**
      * Returns an array of all possible signatures of this function. Multiple
      * signatures arise from some arguments being optional.
      *
      * <p> Possible signatures: <OL> <li>
      * {@code (TABLE input_table, STRING 'weights_column')} <li>
      * {@code (TABLE input_table, STRING 'weights_column', STRING 'output_table_prefix')}
      * <li>
      * {@code (TABLE input_table, STRING 'weights_column', INT orientation)}
      * <li>
      * {@code (TABLE input_table, STRING 'weights_column', STRING 'output_table_prefix', INT orientation)}
      * </OL>
      *
      * @return An array of all possible signatures of this function.
      */
     // TODO: Make the output_table_prefix parameter optional:
     // EXECUTE ST_ClosenessCentrality(input_table, 'weights_column'[,'output_table_prefix',orientation]);
     @Override
     public FunctionSignature[] getFunctionSignatures() {
         return new FunctionSignature[]{
                     //                    // First possible signature: (TABLE input_table, STRING 'weights_column').
                     //                    new ExecutorFunctionSignature(
                     //                    new TableArgument(TableDefinition.GEOMETRY),
                     //                    ScalarArgument.STRING),
                     // Second possible signature: (TABLE input_table, STRING 'weights_column', STRING 'output_table_prefix').
                     new ExecutorFunctionSignature(
                     new TableArgument(TableDefinition.GEOMETRY),
                     ScalarArgument.STRING,
                     ScalarArgument.STRING),
                     //                    // Third possible signature: (TABLE input_table, STRING 'weights_column', INT orientation).
                     //                    new ExecutorFunctionSignature(
                     //                    new TableArgument(TableDefinition.GEOMETRY),
                     //                    ScalarArgument.STRING,
                     //                    ScalarArgument.INT),
                     // Fourth possible signature: (TABLE input_table, STRING 'weights_column', STRING 'output_table_prefix', INT orientation).
                     new ExecutorFunctionSignature(
                     new TableArgument(TableDefinition.GEOMETRY),
                     ScalarArgument.STRING,
                     ScalarArgument.STRING,
                     ScalarArgument.INT)
                 };
     }
 }
