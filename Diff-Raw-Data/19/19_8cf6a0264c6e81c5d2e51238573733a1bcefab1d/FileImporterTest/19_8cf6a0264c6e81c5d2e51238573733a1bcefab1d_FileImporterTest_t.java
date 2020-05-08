 /*
  Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)
 
  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies
 
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
 
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 package cytoscape.data.readers;
 
 import cytoscape.AllTests;
 import cytoscape.CyNetwork;
 import cytoscape.Cytoscape;
 
 import cytoscape.data.readers.GraphReader;
 
 import cytoscape.dialogs.*;
 
 import cytoscape.view.NetworkPanel;
 
 //--------------------------------------------------------------------------------------
 import cytoscape.view.NetworkViewManager;
 
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 
 //-----------------------------------------------------------------------------------------
 /**
  *
  */
 public class FileImporterTest extends TestCase {
 	String location;
 	String title;
 	int nodeCount;
 	int edgeCount;
 	CyNetwork network;
 
 	//	VisualStyleBuilderDialog vsd;
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @throws Exception DOCUMENT ME!
 	 */
 	public void setUp() throws Exception {
 	}
 
 	// ------------------------------------------------------------------------------
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @throws Exception DOCUMENT ME!
 	 */
 	public void tearDown() throws Exception {
 	}
 
 	// ------------------------------------------------------------------------------
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @throws Exception DOCUMENT ME!
 	 */
 	public void testGMLImport() throws Exception {
 		location = "testData/gal.gml";
 		network = Cytoscape.createNetworkFromFile(location);
 
 		title = network.getTitle();
		assertEquals("gal", title);
 
 		nodeCount = network.getNodeCount();
 		assertEquals("number of nodes", 11, nodeCount);
 
 		edgeCount = network.getEdgeCount();
 		assertEquals("number of edges", 10, edgeCount);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @throws Exception DOCUMENT ME!
 	 */
 	public void testXGMMLImport() throws Exception {
 		location = "testData/galFiltered2.xgmml";
 		network = Cytoscape.createNetworkFromFile(location, false);
 
 		title = network.getTitle();
 		assertEquals("GAL Filtered (Yeast)", title);
 
 		nodeCount = network.getNodeCount();
 		assertEquals("num nodes", 331, nodeCount);
 
 		edgeCount = network.getEdgeCount();
 		assertEquals("num edges", 362, edgeCount);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @throws Exception DOCUMENT ME!
 	 */
 	public void testSIFImport() throws Exception {
 		location = "testData/galFiltered.sif";
 		network = Cytoscape.createNetworkFromFile(location, false);
 
 		title = network.getTitle();
		assertEquals("galFiltered", title);
 
 		nodeCount = network.getNodeCount();
 		assertEquals("num nodes", 331, nodeCount);
 
 		edgeCount = network.getEdgeCount();
 		assertEquals("num edge", 362, edgeCount);
 	}
 
 	//test sessions importer ?
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param args DOCUMENT ME!
 	 */
 	public static void main(String[] args) {
 		junit.textui.TestRunner.run(FileImporterTest.class);
 	}
 }
