 /*
   File: ExpressionDataTest.java
 
   Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
 
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
 
 // ExpressionDataTest.java
 
 //------------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //--------------------------------------------------------------------------------------
 package cytoscape.data;
 
 import cytoscape.AllTests;
 import cytoscape.Cytoscape;
 
 import cytoscape.data.ExpressionData;
 import cytoscape.data.mRNAMeasurement;
 
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import java.io.File;
 
 import java.util.Vector;
 
 
 /**
  * Tests the ExpressionData Object.
  *
  */
 public class ExpressionDataTest extends TestCase {
 	/**
 	 * Test Data Directory.
 	 */
 	private static String testDataDir = "testData";
 
 	/**
	 * Test File Name.
 	 */
	private static String testDataFilename = "/gal1.22x5.mRNA";
 
 	/**
 	 * Tests Loading of Sample Data.
 	 * @throws Exception    All Errors.
 	 */
 	public void testExpressionDataLoading() throws Exception {
 		//  Load the specified Expression Data File
		ExpressionData data = new ExpressionData(testDataDir + testDataFilename);
 		Vector measurements = data.getAllMeasurements();
 		assertTrue(data.getNumberOfGenes() == measurements.size());
 		assertTrue(data.getNumberOfGenes() > 0);
 
 		//  Validate the first row of data
 		String geneName = data.getGeneNames()[0];
 		assertTrue(data.getGeneNames().length == data.getNumberOfGenes());
 
 		//  Validate the Gene Name
 		assertEquals("YHR051W", geneName);
 
 		//  Validate the Gene Descriptor.
 		String geneDescriptor = data.getGeneDescriptors()[0];
		assertEquals("COX6", geneDescriptor);
 
 		//  Validate the 0th Experimental Condition
 		String conditionName = data.getConditionNames()[0];
 		assertEquals("gal1RG.sig", conditionName);
 		assertTrue(data.getConditionNames().length == data.getNumberOfConditions());
 
 		//  Validate data for the 0th Experimental Condition
 		Vector geneInfo = (Vector) measurements.get(0);
 		mRNAMeasurement measurement = (mRNAMeasurement) geneInfo.get(0);
 		assertEquals(-0.034, measurement.getRatio(), 0.001);
 		assertEquals(1.177, measurement.getSignificance(), 0.001);
 
 		//  Validate the extreme values
 		double[][] extremes = data.getExtremeValues();
 		assertEquals(-0.71, extremes[0][0], 0.01);
 		assertEquals(0.432, extremes[0][1], 0.01);
 		assertEquals(-0.717, extremes[1][0], 0.01);
 		assertEquals(27.075, extremes[1][1], 0.01);
 	}
 
 	/**
 	 * Tests Loading of Sample Data.
 	 * @throws Exception    All Errors.
 	 */
 	public void testGetMeasurement() throws Exception {
		ExpressionData data = new ExpressionData(testDataDir + testDataFilename);
 
 		//  Validate data for all rows.
 		for (int i = 0; i < data.getGeneNames().length; i++) {
 			String gene = data.getGeneNames()[i];
 			String condition = data.getConditionNames()[0];
 
 			mRNAMeasurement measurement = data.getMeasurement(gene, condition);
 			double ratio = measurement.getRatio();
 			double sig = measurement.getSignificance();
 
 			assertTrue(ratio > -100.0);
 			assertTrue(ratio < 1000.0);
 
 			assertTrue(sig >= -1);
 			assertTrue(sig < 10000.0);
 		}
 	}
 
 	/**
 	 * Main method, used for testing from the command line.
 	 * @param args Command Line Arguments.
 	 */
 	public static void main(String[] args) {
 		if (args.length == 1)
 			testDataDir = args[0];
 
 		File tester = new File(testDataDir);
 
 		if (!(tester.canRead() && tester.isDirectory())) {
 			System.err.println("error! ExpressionDataTest cannot read relative directory '"
 			                   + testDataDir + "'");
 			Cytoscape.exit(1);
 		}
 
 		junit.textui.TestRunner.run(new TestSuite(ExpressionDataTest.class));
 	}
 }
