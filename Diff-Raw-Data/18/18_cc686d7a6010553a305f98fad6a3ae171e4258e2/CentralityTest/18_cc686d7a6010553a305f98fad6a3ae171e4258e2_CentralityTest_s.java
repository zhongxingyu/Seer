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
 
 import org.gdms.data.DataSource;
 import org.gdms.driver.DataSet;
 import org.gdms.driver.DiskBufferDriver;
 import org.gdms.driver.DriverException;
 import org.gdms.gdmstopology.TopologySetUpTest;
 import org.gdms.gdmstopology.model.WMultigraphDataSource;
 import org.gdms.gdmstopology.process.GraphCentralityUtilities;
 import static org.junit.Assert.*;
 import org.junit.Test;
 import org.orbisgis.progress.NullProgressMonitor;
 import org.orbisgis.progress.ProgressMonitor;
 
 /**
  * Several tests of centrality metrics.
  *
  * @author Adam Gouge
  */
 public class CentralityTest extends TopologySetUpTest {
 
     /**
      * Test the closeness centrality calculation on a 2D graph considered as an
      * undirected graph with all weights equal to one.
      *
      * @throws Exception
      */
     @Test
     public void testClosenessCentrality2DUndirected() throws Exception {
         // Graph type: Undirected.
 
         // Setup.
         int testNumber = 1;
         String weightColumnName = "length";
         DataSource dataSource = dsf.getDataSource(GRAPH2D_EDGES);
         dataSource.open();
         DataSet[] tables = new DataSet[]{dataSource};
         ProgressMonitor pm = new NullProgressMonitor();
         WMultigraphDataSource wMultigraphDataSource =
                 new WMultigraphDataSource(
                 dsf,
                 tables[0],
                 pm);
         wMultigraphDataSource.setWeightFieldIndex(weightColumnName);
 
         // Calculate the closeness centrality indices.
         DiskBufferDriver closenessCentralityDriver =
                 GraphCentralityUtilities.
                 calculateClosenessCentralityIndices(
                 dsf,
                 wMultigraphDataSource,
                 pm);
         closenessCentralityDriver.open();
 
         // We should have a centrality index for all 6 vertices.
         assertTrue(closenessCentralityDriver.getRowCount() == 6);
         // Note that we recover the geometries in an SQL request, so we cannot 
         // check them here in a unit test.
 
         // Check the most central node.
         // TODO: The numbering of the most central node in OrbisGIS is 4, 
         // but in this test, it is 6.
 //        assertTrue(closenessCentralityDriver.getFieldValue(0, 0).getAsInt() == 4);
        assertTrue(
                closenessCentralityDriver.
                getFieldValue(0, 1).
                getAsDouble() == 0.0055753940798198886);
 
         // Print out the result.
        printResult(closenessCentralityDriver, testNumber);
         dataSource.close();
     }
 
     private void printResult(DataSet result, int testNumber) throws DriverException {
         System.out.println("TEST " + testNumber + " - Closeness centrality indices: ");
         for (int i = 0; i < result.getRowCount(); i++) {
            System.out.println("(" + result.getFieldValue(i, 0)
                    + "," + result.getFieldValue(i, 1) + "), ");
         }
     }
 }
