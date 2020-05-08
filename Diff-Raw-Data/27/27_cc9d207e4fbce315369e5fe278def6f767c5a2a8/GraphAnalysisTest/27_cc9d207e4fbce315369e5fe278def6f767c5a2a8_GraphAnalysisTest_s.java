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
 
import java.util.LinkedList;
import org.gdms.data.DataSourceIterator;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.DirectedGraph;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.types.Type;
import org.junit.Test;
 import org.gdms.data.DataSource;
 import org.gdms.data.values.Value;
 import org.gdms.data.values.ValueFactory;
 import org.gdms.driver.DataSet;
 import org.gdms.driver.DriverException;
 import org.gdms.driver.memory.MemoryDataSetDriver;
 import org.gdms.gdmstopology.TopologySetUpTest;
 import org.jgrapht.traverse.ClosestFirstIterator;
import org.orbisgis.progress.NullProgressMonitor;


 import static org.junit.Assert.*;
 
 /**
  *
  * @author Erwan Bocher
  */
 public class GraphAnalysisTest extends TopologySetUpTest {
 
     /**
      * Tests finding the shortest path between neighboring vertices of a
      * directed graph (GRAPH2D) in the right direction.
      *
      * @throws Exception
      */
     @Test
     public void testST_ShortestPath() throws Exception {
         ST_ShortestPath sT_ShortestPath = new ST_ShortestPath();
         DataSource ds = dsf.getDataSource(GRAPH2D_EDGES);
         ds.open();
         DataSet[] tables = new DataSet[]{ds};
         int source = 3;
         int target = 5;
         DataSet result = sT_ShortestPath.evaluate(
                 dsf,
                 tables,
                 new Value[]{
                     ValueFactory.createValue(source),
                     ValueFactory.createValue(target),
                     ValueFactory.createValue("length")
                 },
                 new NullProgressMonitor());
         assertTrue(result.getRowCount() == 1);
         assertTrue(result.getFieldValue(0, 0).getAsGeometry().equals(wktReader.read("LINESTRING ( 222 242, 335 313 )")));
         printResult(result, 1, source, target);
         ds.close();
     }
 
     /**
      * Tests finding the shortest path between neighboring vertices of a
      * directed graph (GRAPH2D) in the wrong direction.
      *
      * @throws Exception
      */
     @Test
     public void testST_ShortestPath2() throws Exception {
         ST_ShortestPath sT_ShortestPath = new ST_ShortestPath();
         DataSource ds = dsf.getDataSource(GRAPH2D_EDGES);
         ds.open();
         DataSet[] tables = new DataSet[]{ds};
         int source = 5;
         int target = 3;
         sT_ShortestPath = new ST_ShortestPath();
         DataSet result = sT_ShortestPath.evaluate(
                 dsf,
                 tables,
                 new Value[]{
                     ValueFactory.createValue(source),
                     ValueFactory.createValue(target),
                     ValueFactory.createValue("length")
                 },
                 new NullProgressMonitor());
         // This is a directed graph, and the edge goes from 3 to 5, 
         // not the other way around.
         assertTrue(result.getRowCount() == 0);
         printResult(result, 2, source, target);
         ds.close();
 
     }
 
     /**
      * Tests finding the shortest path between neighboring vertices of a
      * directed graph (GRAPH2D) with the direction reversed and in the right
      * (new) direction.
      *
      * @throws Exception
      */
     @Test
     public void testST_ShortestPath3() throws Exception {
         ST_ShortestPath sT_ShortestPath = new ST_ShortestPath();
         DataSource ds = dsf.getDataSource(GRAPH2D_EDGES);
         ds.open();
         DataSet[] tables = new DataSet[]{ds};
         int source = 5;
         int target = 3;
         DataSet result = sT_ShortestPath.evaluate(
                 dsf,
                 tables,
                 new Value[]{
                     ValueFactory.createValue(source),
                     ValueFactory.createValue(target),
                     ValueFactory.createValue("length"),
                     ValueFactory.createValue(2) // Edge-reversed
                 },
                 new NullProgressMonitor());
         assertTrue(result.getRowCount() == 1);
         assertTrue(result.getFieldValue(0, 0).getAsGeometry().equals(wktReader.read("LINESTRING ( 222 242, 335 313 )")));
         printResult(result, 3, source, target);
         ds.close();
     }
 
     /**
      * Tests finding the shortest path between non-neighboring vertices of an
      * undirected graph (GRAPH2D).
      *
      * @throws Exception
      */
     @Test
     public void testST_ShortestPath4() throws Exception {
         ST_ShortestPath sT_ShortestPath = new ST_ShortestPath();
         DataSource ds = dsf.getDataSource(GRAPH2D_EDGES);
         ds.open();
         DataSet[] tables = new DataSet[]{ds};
         int source = 3;
         int target = 4;
         DataSet result = sT_ShortestPath.evaluate(
                 dsf,
                 tables,
                 new Value[]{
                     ValueFactory.createValue(source),
                     ValueFactory.createValue(target),
                     ValueFactory.createValue("length"),
                     ValueFactory.createValue(3) // Undirected
                 },
                 new NullProgressMonitor());
         assertTrue(result.getRowCount() == 3);
         printResult(result, 4, source, target);
         ds.close();
     }
 
     /**
      * Tests finding the shortest path between non-neighboring vertices of a
      * directed graph (entered manually).
      *
      * @throws Exception
      */
     @Test
     public void testST_ShortestPath5() throws Exception {
         ST_ShortestPath sT_ShortestPath = new ST_ShortestPath();
 
         MemoryDataSetDriver mdsd = new MemoryDataSetDriver(
                 new String[]{
                     "geom",
                     "start_node",
                     "end_node",
                     "weigth"}, // TODO: weight is misspelled.
                 new Type[]{
                     TypeFactory.createType(Type.GEOMETRY),
                     TypeFactory.createType(Type.INT),
                     TypeFactory.createType(Type.INT),
                     TypeFactory.createType(Type.DOUBLE)});
 
         mdsd.addValues(
                 new Value[]{
                     ValueFactory.createValue(wktReader.read("LINESTRING(0 0, 2 2)")),
                     ValueFactory.createValue(1),
                     ValueFactory.createValue(2),
                     ValueFactory.createValue(0)
                 });
         mdsd.addValues(
                 new Value[]{
                     ValueFactory.createValue(wktReader.read("LINESTRING(2 2, 4 4 , 6 2)")),
                     ValueFactory.createValue(2),
                     ValueFactory.createValue(3),
                     ValueFactory.createValue(0)
                 });
         mdsd.addValues(
                 new Value[]{
                     ValueFactory.createValue(wktReader.read("LINESTRING(2 2, 4 1 , 6 2)")),
                     ValueFactory.createValue(2),
                     ValueFactory.createValue(3),
                     ValueFactory.createValue(10)
                 });
         mdsd.addValues(
                 new Value[]{
                     ValueFactory.createValue(wktReader.read("LINESTRING(6 2  , 10 2)")),
                     ValueFactory.createValue(3),
                     ValueFactory.createValue(4),
                     ValueFactory.createValue(0)
                 });
 
         DataSet[] tables = new DataSet[]{mdsd};
         int source = 1;
         int target = 4;
         DataSet result = sT_ShortestPath.evaluate(
                 dsf,
                 tables,
                 new Value[]{
                     ValueFactory.createValue(source),
                     ValueFactory.createValue(target),
                     ValueFactory.createValue("weigth") // TODO: weight is misspelled.
                 },
                 new NullProgressMonitor());
         assertTrue(result.getRowCount() == 3);
         assertTrue(result.getFieldValue(0, 0).getAsGeometry().equals(wktReader.read("LINESTRING(6 2  , 10 2)")));
         assertTrue(result.getFieldValue(1, 0).getAsGeometry().equals(wktReader.read("LINESTRING(2 2, 4 4 , 6 2)")));
         assertTrue(result.getFieldValue(2, 0).getAsGeometry().equals(wktReader.read("LINESTRING(0 0, 2 2)")));
         printResult(result, 5, source, target);
     }
 
     /**
      * Tests finding the shortest path between non-neighboring vertices of a
      * directed graph (entered manually).
      *
      * This is the same as Test 5 with a weight changed (and thus a new shortest
      * path).
      *
      * @throws Exception
      */
     @Test
     public void testST_ShortestPath6() throws Exception {
         ST_ShortestPath sT_ShortestPath = new ST_ShortestPath();
 
         MemoryDataSetDriver mdsd = new MemoryDataSetDriver(
                 new String[]{
                     "geom",
                     "start_node",
                     "end_node",
                     "weigth" // TODO: weight is misspelled.
                 },
                 new Type[]{
                     TypeFactory.createType(Type.GEOMETRY),
                     TypeFactory.createType(Type.INT),
                     TypeFactory.createType(Type.INT),
                     TypeFactory.createType(Type.DOUBLE)
                 });
 
         mdsd.addValues(
                 new Value[]{
                     ValueFactory.createValue(wktReader.read("LINESTRING(0 0, 2 2)")),
                     ValueFactory.createValue(1),
                     ValueFactory.createValue(2),
                     ValueFactory.createValue(0)
                 });
         mdsd.addValues(
                 new Value[]{
                     ValueFactory.createValue(wktReader.read("LINESTRING(2 2, 4 4 , 6 2)")),
                     ValueFactory.createValue(2),
                     ValueFactory.createValue(3),
                     ValueFactory.createValue(10) // changed the weight
                 });
         mdsd.addValues(
                 new Value[]{
                     ValueFactory.createValue(wktReader.read("LINESTRING(2 2, 4 1 , 6 2)")),
                     ValueFactory.createValue(2),
                     ValueFactory.createValue(3),
                     ValueFactory.createValue(0)}); // changed the weight
         mdsd.addValues(
                 new Value[]{
                     ValueFactory.createValue(wktReader.read("LINESTRING(6 2  , 10 2)")),
                     ValueFactory.createValue(3),
                     ValueFactory.createValue(4),
                     ValueFactory.createValue(0)});
 
         DataSet[] tables = new DataSet[]{mdsd};
         int source = 1;
         int target = 4;
         DataSet result = sT_ShortestPath.evaluate(
                 dsf,
                 tables,
                 new Value[]{
                     ValueFactory.createValue(source),
                     ValueFactory.createValue(target),
                     ValueFactory.createValue("weigth") // TODO: weight is misspelled.
                 },
                 new NullProgressMonitor());
         assertTrue(result.getRowCount() == 3);
         assertTrue(result.getFieldValue(0, 0).getAsGeometry().equals(wktReader.read("LINESTRING(6 2  , 10 2)")));
         assertTrue(result.getFieldValue(1, 0).getAsGeometry().equals(wktReader.read("LINESTRING(2 2, 4 1 , 6 2)"))); // new choice
         assertTrue(result.getFieldValue(2, 0).getAsGeometry().equals(wktReader.read("LINESTRING(0 0, 2 2)")));
         printResult(result, 6, source, target);
     }
 
     @Test
     public void testST_ShortestPathLength() throws Exception {
         ST_ShortestPathLength sT_ShortestPathLength = new ST_ShortestPathLength();
         DataSource ds = dsf.getDataSource(GRAPH2D_EDGES);
         ds.open();
         DataSet[] tables = new DataSet[]{ds};
         int source = 3;
         DataSet result = sT_ShortestPathLength.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source),
                     ValueFactory.createValue("length")}, new NullProgressMonitor());
         assertTrue(result.getRowCount() == 4);
         HashMap<Integer, Double> results = new HashMap<Integer, Double>();
         results.put(5, 133.4541119636259);
         results.put(6, 51.35172830587107);
         results.put(1, (211.6687715105811 + 51.35172830587107));
         results.put(4, (211.6687715105811 + 51.35172830587107) + 56.32051136131489);
         for (int i = 0; i < result.getRowCount(); i++) {
             int key = result.getInt(i, 0);
             double cost = result.getDouble(i, 1);
             assertTrue((results.get(key) - cost) == 0);
 
         }
         ds.close();
     }
 
     @Test
     public void testST_ShortestPathLengthReverse() throws Exception {
         ST_ShortestPathLength sT_ShortestPathLength = new ST_ShortestPathLength();
         DataSource ds = dsf.getDataSource(GRAPH2D_EDGES);
         ds.open();
         DataSet[] tables = new DataSet[]{ds};
         int source = 3;
         DataSet result = sT_ShortestPathLength.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source),
                     ValueFactory.createValue("length"), ValueFactory.createValue(2)}, new NullProgressMonitor());
         assertTrue(result.getRowCount() == 1);
         HashMap<Integer, Double> results = new HashMap<Integer, Double>();
         results.put(2, 129.63024338479042);
         for (int i = 0; i < result.getRowCount(); i++) {
             int key = result.getInt(i, 0);
             double cost = result.getDouble(i, 1);
             assertTrue((results.get(key) - cost) == 0);
         }
         ds.close();
     }
 
     @Test
     public void testST_ShortestPathLengthMultiGraph() throws Exception {
         ST_ShortestPathLength sT_ShortestPathLength = new ST_ShortestPathLength();
         DataSource ds = dsf.getDataSource(GRAPH2D_EDGES);
         ds.open();
         DataSet[] tables = new DataSet[]{ds};
         int source = 3;
         DataSet result = sT_ShortestPathLength.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source),
                     ValueFactory.createValue("length"),
                     ValueFactory.createValue(3)}, new NullProgressMonitor());
         assertTrue(result.getRowCount() == 5);
         HashMap<Integer, Double> results = new HashMap<Integer, Double>();
         results.put(2, 129.63024338479042);
         results.put(5, 133.4541119636259);
         results.put(6, 51.35172830587107);
         results.put(1, (211.6687715105811 + 51.35172830587107));
         results.put(4, (211.6687715105811 + 51.35172830587107) + 56.32051136131489);
         for (int i = 0; i < result.getRowCount(); i++) {
             int key = result.getInt(i, 0);
             double cost = result.getDouble(i, 1);
             assertTrue((results.get(key) - cost) == 0);
         }
         ds.close();
     }
 
     @Test
     public void testST_MShortestPath() throws Exception {
         ST_MShortestPath sT_MShortestPath = new ST_MShortestPath();
         DataSource ds = dsf.getDataSource(GRAPH2D_EDGES);
         ds.open();
 
         MemoryDataSetDriver nodes = new MemoryDataSetDriver(new String[]{"id", "source", "target"},
                 new Type[]{
                     TypeFactory.createType(Type.INT),
                     TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT)});
 
         nodes.addValues(new Value[]{
                     ValueFactory.createValue(1),
                     ValueFactory.createValue(6),
                     ValueFactory.createValue(1)});
 
         DataSet[] tables = new DataSet[]{ds, nodes};
         DataSet result = sT_MShortestPath.evaluate(dsf, tables, new Value[]{ValueFactory.createValue("length")}, new NullProgressMonitor());
         assertTrue(result.getRowCount() == 1);
         assertTrue(result.getGeometry(0, 0).equals(wktReader.read("LINESTRING ( 228 191, 313 110, 223 82 )")));
         ds.close();
     }
 
     @Test
     public void testST_MShortestPath1() throws Exception {
         ST_MShortestPath sT_MShortestPath = new ST_MShortestPath();
         DataSource ds = dsf.getDataSource(GRAPH2D_EDGES);
         ds.open();
 
         MemoryDataSetDriver nodes = new MemoryDataSetDriver(new String[]{"id", "source", "target"},
                 new Type[]{
                     TypeFactory.createType(Type.INT),
                     TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT)});
 
         nodes.addValues(new Value[]{
                     ValueFactory.createValue(1),
                     ValueFactory.createValue(2),
                     ValueFactory.createValue(1)});
         nodes.addValues(new Value[]{
                     ValueFactory.createValue(2),
                     ValueFactory.createValue(2),
                     ValueFactory.createValue(5)});
         nodes.addValues(new Value[]{
                     ValueFactory.createValue(3),
                     ValueFactory.createValue(6),
                     ValueFactory.createValue(4)});
         nodes.addValues(new Value[]{
                     ValueFactory.createValue(3),
                     ValueFactory.createValue(6),
                     ValueFactory.createValue(1)});
 
         DataSet[] tables = new DataSet[]{ds, nodes};
         DataSet result = sT_MShortestPath.evaluate(dsf, tables, new Value[]{ValueFactory.createValue("length")},
                 new NullProgressMonitor());
         assertTrue(result.getRowCount() == 8);
         ds.close();
     }
 
     @Test
     public void testST_MShortestPath2() throws Exception {
         ST_MShortestPath sT_MShortestPath = new ST_MShortestPath();
         DataSource ds = dsf.getDataSource(GRAPH2D_EDGES);
         ds.open();
         MemoryDataSetDriver nodes = new MemoryDataSetDriver(new String[]{"id", "source", "target"},
                 new Type[]{
                     TypeFactory.createType(Type.INT),
                     TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT)});
 
         nodes.addValues(new Value[]{
                     ValueFactory.createValue(1),
                     ValueFactory.createValue(2),
                     ValueFactory.createValue(1)});
         nodes.addValues(new Value[]{
                     ValueFactory.createValue(2),
                     ValueFactory.createValue(1),
                     ValueFactory.createValue(5)});
         nodes.addValues(new Value[]{
                     ValueFactory.createValue(3),
                     ValueFactory.createValue(6),
                     ValueFactory.createValue(4)});
         nodes.addValues(new Value[]{
                     ValueFactory.createValue(3),
                     ValueFactory.createValue(5),
                     ValueFactory.createValue(1)});
 
         DataSet[] tables = new DataSet[]{ds, nodes};
         DataSet result = sT_MShortestPath.evaluate(dsf, tables, new Value[]{ValueFactory.createValue("length")},
                 new NullProgressMonitor());
         assertTrue(result.getRowCount() == 5);
         ds.close();
     }
 
     @Test
     public void testST_MShortestPathLength() throws Exception {
         ST_MShortestPathLength sT_MShortestPathLength = new ST_MShortestPathLength();
         DataSource ds = dsf.getDataSource(GRAPH2D_EDGES);
         ds.open();
         MemoryDataSetDriver nodes = new MemoryDataSetDriver(new String[]{"id", "source", "target"},
                 new Type[]{
                     TypeFactory.createType(Type.INT),
                     TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT)});
 
         nodes.addValues(new Value[]{
                     ValueFactory.createValue(1),
                     ValueFactory.createValue(2),
                     ValueFactory.createValue(4)});
 
         DataSet[] tables = new DataSet[]{ds, nodes};
         DataSet result = sT_MShortestPathLength.evaluate(dsf, tables, new Value[]{ValueFactory.createValue("length")},
                 new NullProgressMonitor());
         assertTrue(result.getRowCount() == 1);
 
         DataSourceIterator iter = ds.iterator();
 
         HashSet<Integer> pathList = new HashSet<Integer>();
         pathList.add(1);
         pathList.add(3);
         pathList.add(4);
         pathList.add(6);
 
         double sum = 0;
         while (iter.hasNext()) {
             Value[] values = iter.next();
             if (pathList.contains(values[1].getAsInt())) {
                 sum += values[0].getAsGeometry().getLength();
             }
         }
         assertEquals(result.getDouble(0, 1), sum, 0.000001);
 
         ds.close();
     }
 
     @Test
     public void JGraphtMPath() {
         DirectedGraph<Integer, DefaultEdge> g =
                 new DefaultDirectedGraph<Integer, DefaultEdge>(DefaultEdge.class);
         g.addVertex(1);
         g.addVertex(2);
         g.addVertex(3);
         g.addVertex(4);
         g.addVertex(5);
         g.addVertex(6);
         g.addEdge(2, 3);
         g.addEdge(3, 5);
         g.addEdge(3, 6);
         g.addEdge(6, 1);
         g.addEdge(6, 1);
         g.addEdge(1, 4);
 
         //Graph<Integer, DefaultEdge> g = RandomGraphCreator.createRandomDirectGraph(10000, 2000);
 
         ArrayList<Integer> sources = new ArrayList<Integer>();
         sources.add(2);
         sources.add(3);
 
 
         int sourceNode = 3;
         LinkedList<Integer> targets = new LinkedList<Integer>();
         targets.add(1);
         targets.add(3);
         targets.add(5);
         targets.add(5);
         targets.add(4);
 
         ClosestFirstIterator<Integer, DefaultEdge> cl = new ClosestFirstIterator<Integer, DefaultEdge>(g, sourceNode);
 
         while (cl.hasNext()) {
             int vertex = cl.next();
             if (vertex != sourceNode) {
                 if (targets.contains(vertex)) {
                     int v = vertex;
                     int k = 0;
                     StringBuffer sb = new StringBuffer();
                     while (true) {
                         DefaultEdge edge = cl.getSpanningTreeEdge(v);
                         if (edge == null) {
                             break;
                         }
                         k++;
                         sb.append(edge.toString());
                         v = Graphs.getOppositeVertex(g, edge, v);
                     }
                     System.out.println("Source " + sourceNode + " --> Target " + vertex + sb.toString());
                 }
             }
         }
         assertTrue(true);
     }
 
     private void printResult(DataSet result, int testNumber, int source, int target) throws DriverException {
         String resultString = "TEST " + testNumber + " - Shortest path from " + source + " to " + target + ": ";
         for (int i = 0; i < result.getRowCount(); i++) {
             resultString += "(" + result.getFieldValue(i, 3)
                     + "," + result.getFieldValue(i, 4)
                     + ") weight " + result.getFieldValue(i, 5) + ", ";
         }
         System.out.println(resultString);
     }
 }
