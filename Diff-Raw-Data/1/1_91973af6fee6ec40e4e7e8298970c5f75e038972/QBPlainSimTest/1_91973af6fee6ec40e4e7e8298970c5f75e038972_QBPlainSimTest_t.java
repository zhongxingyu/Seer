 /*
  * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
  * at the top-level directory of this distribution.
  * This file is part of the KyuPI project. It is subject to the license terms
  * in the LICENSE.md file found in the top-level directory of this distribution.
  * No part of the KyuPI project, including this file, may be copied, modified,
  * propagated, or distributed except according to the terms contained in the
  * LICENSE.md file.
  */
 package org.kyupi.sim;
 
 import java.io.File;
 
 
 import junit.framework.TestCase;
 
 import org.apache.log4j.Logger;
 import org.junit.Test;
 import org.kyupi.data.item.QBlock;
 import org.kyupi.data.item.QVector;
 import org.kyupi.data.source.QBSource;
 import org.kyupi.data.source.QVSource;
 import org.kyupi.graph.Graph;
 import org.kyupi.graph.Graph.Node;
 import org.kyupi.graph.GraphTools;
 import org.kyupi.graph.Library;
 import org.kyupi.graph.LibraryNangate;
 import org.kyupi.graph.LibrarySAED;
 import org.kyupi.misc.RuntimeTools;
 
 public class QBPlainSimTest extends TestCase {
 
 	protected static Logger log = Logger.getLogger(QBPlainSimTest.class);
 
 	@Test
 	public void testNorInv() {
 		Graph g = GraphTools.benchToGraph("input(a) input(b) output(nor) output(inv) nor=NOR(a,b) inv=NOT(a)");
 		int length = g.accessInterface().length;
 
 		QVSource sim = QVSource.from(new QBPlainSim(g, QBSource.random(length, 42)));
 		QVSource ref = QVSource.from(new QBSource(length) {
 			private QBSource rand = QBSource.random(length(), 42);
 
 			public void reset() {
 				rand.reset();
 			}
 
 			protected QBlock compute() {
 				long cv[] = new long[2];
 				long l = 0L;
 				long k = 0L;
 				long j = 0L;
 				QBlock output = rand.next();
 				for (int i = 0; i < 2; i++){
 					l |= ~output.getC(i) & output.getV(i);
 					k |= output.getC(i) & output.getV(i);
 					j |= ~output.getC(i) & ~output.getV(i);
 				}
 				cv[0] = -1L; cv[1] = -1L;
 				cv[0] &= ~j; cv[1] &= ~j;				
 				cv[0] |= k;	 cv[1] &= ~k;				
 				cv[0] &= ~l; cv[1] |= l;
 				
 				output.set(2, cv[1], cv[0]);
 				output.set(3, (output.getV(0) ^ output.getC(0)), output.getC(0));
 				return output;
 			}
 		});
 
 		// simulate 128 random patterns and compare the responses.
 		for (int i = 0; i < 128; i++) {
 			assertEqualsReport(ref.next(), sim.next(), i, g.accessInterface());
 		}
 	}
 
 	@Test
 	public void testC17Nangate() throws Exception {
 		Graph g_ref = GraphTools.loadGraph(new File(RuntimeTools.KYUPI_HOME, "testdata/c17.isc"), new Library());
 		Graph g_test = GraphTools.loadGraph(new File(RuntimeTools.KYUPI_HOME, "testdata/Nangate/c17.v"), new LibraryNangate());
 		assertEqualsByRandomSimulation(g_ref, g_test);
 	}
 
 	@Test
 	public void testC17Saed90() throws Exception {
 		Graph g_ref = GraphTools.loadGraph(new File(RuntimeTools.KYUPI_HOME, "testdata/c17.isc"), new Library());
 		Graph g_test = GraphTools.loadGraph(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/c17.v"), new LibrarySAED());
 		assertEqualsByRandomSimulation(g_ref, g_test);
 	}
 
 	@Test
 	public void testAllSaed90() throws Exception {
 		Graph g_ref = GraphTools.loadGraph(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/SAED90norinv.v"), new LibrarySAED());
 		Graph g_test = GraphTools.loadGraph(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/SAED90cells.v"), new LibrarySAED());
		GraphTools.splitMultiOutputCells(g_test);
 		assertEqualsByRandomSimulation(g_ref, g_test);
 	}
 
 	private void assertEqualsByRandomSimulation(Graph g_ref, Graph g_test) {
 		int length = g_ref.accessInterface().length;
 		assertEquals(length, g_test.accessInterface().length);
 
 		QVSource ref = QVSource.from(new QBPlainSim(g_ref, QBSource.random(length, 42)));
 		QVSource test = QVSource.from(new QBPlainSim(g_test, QBSource.random(length, 42)));
 
 		// simulate 128 random patterns and compare the responses.
 		for (int i = 0; i < 128; i++) {
 			assertEqualsReport(ref.next(), test.next(), i, g_ref.accessInterface());
 		}
 	}
 
 	private void assertEqualsReport(QVector expected, QVector actual, int pindex, Node[] intf) {
 		if (!expected.equals(actual)) {
 			int l = expected.length();
 			StringBuffer buf = new StringBuffer();
 			for (int i = 0; i < l; i++) {
 				char e = expected.getValue(i);
 				char a = actual.getValue(i);
 				if (e != a) {
 					buf.append(" " + intf[i].queryName() + "=" + a + "(exp:" + e + ")");
 				}
 			}
 			fail("Mismatched pattern " + pindex + ": " + actual + "(exp:" + expected + ")" + buf.toString());
 		}
 		expected.free();
 		actual.free();
 	}
 }
