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
 
 import org.gdms.data.DataSourceFactory;
 import org.gdms.data.values.Value;
 import org.gdms.driver.DataSet;
 import org.gdms.gdmstopology.model.GraphEdge;
 import org.gdms.gdmstopology.process.GraphConnectivityUtilities;
 import org.gdms.sql.function.FunctionException;
 import org.gdms.sql.function.FunctionSignature;
 import org.gdms.sql.function.ScalarArgument;
 import org.gdms.sql.function.executor.AbstractExecutorFunction;
 import org.gdms.sql.function.executor.ExecutorFunctionSignature;
 import org.gdms.sql.function.table.TableArgument;
 import org.gdms.sql.function.table.TableDefinition;
 import org.jgrapht.alg.ConnectivityInspector;
 import org.orbisgis.progress.ProgressMonitor;
 
 /**
  * Calculates the connected components of a given graph.
  *
  * <p> Creates a new table called
  * <code>connected_components</code> which lists all the vertices and to which
  * connected component they belong.
  *
  * <p> Example usage: <center>
  * <code>EXECUTE ST_ConnectedComponents(
  * input_table,
  * 'weights_column',
  * orientation);</code> </center>
  *
  * <p> Required parameters: <ul> <li>
  * <code>input_table</code> - the input table. Specifically, this is the
  * <code>output_table_prefix.edges</code> table produced by
  * <code>ST_Graph</code>, except that an additional column specifying the weight
  * of each edge must be added (this is the
  * <code>'weights_column'</code>). <li>
  * <code>'weights_column'</code> - a string specifying the name of the column of
  * the input table that gives the weight of each edge. <li>
  * <code>orientation</code> - an integer specifying the orientation of the
  * graph: <ul> <li> 1 if the graph is directed, <li> 2 if it is directed and we
  * wish to reverse the orientation of the edges, <li> 3 if the graph is
  * undirected. </ul> If no orientation is specified, we assume the graph is
  * directed. </ul>
  *
  * @author Adam Gouge
  */
 public class ST_ConnectedComponents extends AbstractExecutorFunction {
 
     /**
      * Evaluates the function to calculate the connected components of a graph.
      *
      * @param dsf The {@link DataSourceFactory} used to parse the data set.
      * @param tables The input table. (This {@link DataSet} array will contain
      * only one element since there is only one input table.)
      * @param values Array containing the other arguments.
      * @param pm The progress monitor used to track the progress of the
      * calculation.
      */
     @Override
     public void evaluate(
             DataSourceFactory dsf,
             DataSet[] tables,
             Value[] values,
             ProgressMonitor pm)
             throws FunctionException {
         try {
             // Recover the DataSet.
             final DataSet dataSet = tables[0];
             // Set the weights column name.
             String weightsColumn = values[0].getAsString();
             // Set the output table prefix.
 //            String outputTablePrefix = values[1].getAsString();
             // Get the orientation
             int orientation = values[1].getAsInt();
             // Create the ConnectivityInspector.
             ConnectivityInspector<Integer, GraphEdge> inspector = GraphConnectivityUtilities.
                     getConnectivityInspector(
                     dsf,
                     dataSet,
                     weightsColumn,
                     orientation,
                     pm);
             // Record a new table listing all the vertices and to which
             // connected component they belong.
             GraphConnectivityUtilities.registerConnectedComponents(dsf, inspector);
         } catch (Exception ex) {
             System.out.println(ex);
             throw new FunctionException("Cannot compute the connected components.", ex);
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
         return "ST_ConnectedComponents";
     }
 
     /**
      * Returns an example query using this function.
      *
      * @return An example query using this function.
      */
     @Override
     public String getSqlOrder() {
         return "EXECUTE ST_ConnectedComponents("
                 + "input_table, "
                 + "'weights_column', "
                + ",orientation);";
     }
 
     /**
      * Returns a description of this function.
      *
      * @return A description of this function.
      */
     @Override
     public String getDescription() {
         return "Calculates the connected components of a given graph. "
                 + "<p> Creates a new table called "
                 + "<code>connected_components</code> which lists all the "
                 + "vertices and to which connected component they belong. "
                 + "<p> Required parameters: <ul> "
                 + "<li> <code>input_table</code> - "
                 + "the input table. Specifically, this is the "
                 + "<code>output_table_prefix.edges</code> table produced by "
                 + "<code>ST_Graph</code>, except that an additional column "
                 + "specifying the weight of each edge must be added "
                 + "(this is the <code>'weights_column'</code>). "
                 + "<li> <code>'weights_column'</code> - "
                 + "a string specifying the "
                 + "name of the column of the input table that gives the weight "
                 + "of each edge. "
                 + "<li> <code>orientation</code> - "
                 + "an integer specifying the "
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
      * <code>(TABLE input_table, STRING 'weights_column', INT orientation)</code>
      * </OL>
      *
      * @return An array of all possible signatures of this function.
      */
     @Override
     public FunctionSignature[] getFunctionSignatures() {
         return new FunctionSignature[]{
                     new ExecutorFunctionSignature(
                     new TableArgument(TableDefinition.GEOMETRY),
                     ScalarArgument.STRING,
                     ScalarArgument.INT)
                 };
     }
 }
