 package org.geworkbench.bison.datastructure.bioobjects.microarray;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 
 import junit.framework.TestCase;
 
 import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
 import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
 import org.geworkbench.bison.datastructure.bioobjects.markers.CSExpressionMarker;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
 
 /**
  * @author yc2480
  * @version $Id$
  */
 public class CSAnovaResultSetTest extends TestCase {
 
 	DSMicroarraySetView<DSGeneMarker, DSMicroarray> view = null;
 	int numMarkers = 9;
 	int numGroups = 3;
 	int numArrays = 10;
 	float data[][] = new float[numArrays][numMarkers];
 	String[] groupNames = new String[numGroups];
 	String[] markerNames = new String[numMarkers];
 	double[][] result2DArray = new double[3 + numGroups * 2 + 1][numMarkers];
 
 	/**
 	 * 
 	 * @param name
 	 */
 	public CSAnovaResultSetTest(String name) {
 		super(name);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see junit.framework.TestCase#setUp()
 	 * 
 	 * In this method, we initiate data[][], groupNames[], markerNames[],
 	 * result2DArray[][], view
 	 */
 	protected void setUp() throws Exception {
 		super.setUp();
 		// generate data
 		for (int i = 0; i < numArrays; i++) {
 			for (int j = 0; j < numMarkers; j++) {
 				// each cell we put in different values,
 				// so we can detect if it returns incorrect cell.
 				data[i][j] = i * result2DArray[0].length + j;
 			}
 		}
 
 		// init array
 		for (int i = 0; i < result2DArray.length; i++) {
 			for (int j = 0; j < result2DArray[0].length; j++) {
 				/*
 				 * each cell we put in different values, so we can detect if it
 				 * returns incorrect cell. in real case, this should be the
 				 * result from Anova analysis
 				 */
 				result2DArray[i][j] = i * result2DArray[0].length + j;
 			}
 		}
 
 		// generate arrays
 		DSMicroarraySet microarraySet = new CSMicroarraySet();
 		microarraySet.setLabel(this.getClass().getName());
 
 		for (int i = 0; i < numArrays; i++) {
 			DSMicroarray microarray = new CSMicroarray(numMarkers);
 			microarray.setLabel("Microarray " + i);
 			for (int j = 0; j < numMarkers; j++) {
 				CSExpressionMarkerValue markerVal = new CSExpressionMarkerValue(
 						data[i][j]);
 
 				microarray.setMarkerValue(j, markerVal);
 			}
 			microarraySet.add(microarray);
 		}
 		// generate markers
 		DSPanel<DSGeneMarker> markerPanel = view.getMarkerPanel();
 		for (int i = 0; i < numMarkers; i++) {
 			DSGeneMarker geneMarker = new CSExpressionMarker();
 			geneMarker.setGeneId(i);
 			geneMarker.setGeneName("gene_name_" + i);
 			geneMarker.setLabel("gene_label_" + i);
 			geneMarker.setSerial(i);
 			markerPanel.add(geneMarker);
 			markerNames[i] = geneMarker.getLabel();
 			microarraySet.getMarkers().add(geneMarker);
 		}
 
 		/* order here is important for marker labels */
 		view = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(microarraySet);
 		view.setMarkerPanel(markerPanel);
 
 		// groupNames will be G1, G2, G3,...
 		for (int i = 0; i < groupNames.length; i++) {
 			groupNames[i] = "G" + String.valueOf(i);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	/**
 	 * 
 	 */
 	public final void testGetDataSetFile() {
 		fail("Not yet implemented, since CSAnovaResultSet didn't use this method"); // TODO
 	}
 
 	/**
 	 * 
 	 */
 	public final void testSetDataSetFile() {
 		fail("Not yet implemented, since CSAnovaResultSet didn't use this method"); // TODO
 	}
 
 	/**
 	 * We'll put 8 markers in, but only 4 markers are significant, then save the
 	 * result as CVS, and then read the file, check if only (and exactly) 4
 	 * markers are saved.
 	 */
 	public final void testSaveToFile() {
 		DSAnovaResultSet<DSGeneMarker> anovaResultSet = new CSAnovaResultSet<DSGeneMarker>(
 				view, "Anova Analysis Result Set", groupNames, 0.05, markerNames,
 				result2DArray);
 		anovaResultSet.setSignificance(view.getMarkerPanel().get(0), 0.01);
 		anovaResultSet.setSignificance(view.getMarkerPanel().get(1), 0.02);
 		anovaResultSet.setSignificance(view.getMarkerPanel().get(2), 0.03);
 		anovaResultSet.setSignificance(view.getMarkerPanel().get(3), 0.04);
 		anovaResultSet.setSignificance(view.getMarkerPanel().get(4), 0.05);
 		anovaResultSet.setSignificance(view.getMarkerPanel().get(5), 0.06);
 		anovaResultSet.setSignificance(view.getMarkerPanel().get(6), 0.07);
 		anovaResultSet.setSignificance(view.getMarkerPanel().get(7), 0.08);
 		// expected answer in CVS format, expected four lines.
 		String[] expectedAnswer = new String[4];
 		expectedAnswer[0] = "gene_label_0	0.01";
 		expectedAnswer[1] = "gene_label_1	0.02";
 		expectedAnswer[2] = "gene_label_2	0.03";
 		expectedAnswer[3] = "gene_label_3	0.04";
 
 		anovaResultSet.saveToFile("testSTF.txt");
 		try {
 			FileInputStream fstream = new FileInputStream("testSTF.txt");
 			DataInputStream in = new DataInputStream(fstream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String strLine;
 			// Read File Line By Line
 			int cx = 0;
 			while ((strLine = br.readLine()) != null) {
 				assertEquals("test for expected answer", expectedAnswer[cx++],
 						strLine);
 			}
 			assertEquals("expect 4 significant markers", 4, cx);
 			in.close();
 		} catch (Exception e) {// Catch exception if any
 			System.err.println("Error: " + e.getMessage());
 		}
 		//TODO: delete the file
 	}
 
 	/**
 	 * 
 	 */
 	public final void testGetSignificantMarkers() {
 		DSAnovaResultSet<DSGeneMarker> anovaResultSet = new CSAnovaResultSet<DSGeneMarker>(
 				view, "Anova Analysis Result Set", groupNames, 0.05, markerNames,
 				result2DArray);
 		// we'll put three markers, two significant, one not.
 		anovaResultSet.setSignificance(view.getMarkerPanel().get(0), 0.01);
 		assertEquals(1, anovaResultSet.getSignificantMarkers().size());
 		anovaResultSet.setSignificance(view.getMarkerPanel().get(1), 0.02);
 		assertEquals(2, anovaResultSet.getSignificantMarkers().size());
 		// adding an insignificant marker shouldn't change the size
 		anovaResultSet.setSignificance(view.getMarkerPanel().get(2), 0.2);
 		assertEquals(2, anovaResultSet.getSignificantMarkers().size());
 		// only first two markers less then 0.05, so only first two markers
 		// should exist in the significant panel
 		assertTrue(anovaResultSet.getSignificantMarkers().contains(
 				view.getMarkerPanel().get(0)));
 		assertTrue(anovaResultSet.getSignificantMarkers().contains(
 				view.getMarkerPanel().get(1)));
 		assertFalse(anovaResultSet.getSignificantMarkers().contains(
 				view.getMarkerPanel().get(2)));
 	}
 
 	/**
 	 * 
 	 */
 	public final void testGetCriticalPValue() {
 		// test constructor for local
 		DSAnovaResultSet<DSGeneMarker> anovaResultSet = new CSAnovaResultSet<DSGeneMarker>(
 				view, "Anova Analysis Result Set", groupNames, 0.05, markerNames,
 				result2DArray);
 		assertEquals(0.05, anovaResultSet.getCriticalPValue());
 		// FIXME: 0.05 is hard coded in CSAnovaResultSet.java, we should have a
 		// setter for it.
 	}
 
 	/**
 	 * 
 	 */
 	public final void testGetLabels() {
 		// we'll put three group names and then get it out
 		DSAnovaResultSet<DSGeneMarker> anovaResultSet = new CSAnovaResultSet<DSGeneMarker>(
 				view, "Anova Analysis Result Set", groupNames, 0.05, markerNames,
 				result2DArray);
 		// in this version of implementation, the index is not important, I put
 		// 0 here.
 		String[] labels = anovaResultSet.getLabels(0);
 		// we put in labels (groupNames), should should be able to get labels.
 		assertNotNull(labels);
 		// if we put in 3 elements, we should get 3 elements back.
 		assertEquals(groupNames.length, labels.length);
 		// labels should equal to groupNames
 		assertEquals(groupNames[0], labels[0]);
 		assertEquals(groupNames[2], labels[2]);
 	}
 
 	/**
 	 * 
 	 */
 	public final void testSortMarkersBySignificance() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	/**
 	 * 
 	 */
 	public final void testGetParentDataSet() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	/**
 	 * 
 	 */
 	public final void testCSAnovaResultSetDSMicroarraySetViewStringStringArrayStringArrayDoubleArrayArray() {
 		// test constructor for local
 		DSAnovaResultSet<DSGeneMarker> anovaResultSet = new CSAnovaResultSet<DSGeneMarker>(
 				view, "Anova Analysis Result Set", groupNames, 0.05, markerNames,
 				result2DArray);
 		assertSame(null, anovaResultSet.getFile());
 		assertEquals("Anova Analysis Result Set", anovaResultSet.getLabel());
 		assertSame(groupNames, anovaResultSet.getLabels(0));
 		assertSame(markerNames, anovaResultSet.significantMarkerNamesGetter());
 		assertSame(result2DArray, anovaResultSet.getResult2DArray());
 	}
 
 	/**
 	 * 
 	 */
 	public final void testGetSignificanceDSGeneMarker() {
 		DSAnovaResultSet<DSGeneMarker> anovaResultSet = new CSAnovaResultSet<DSGeneMarker>(
 				view, "Anova Analysis Result Set", groupNames, 0.05, markerNames,
 				result2DArray);
 		anovaResultSet.setSignificance(view.getMarkerPanel().get(0), 0.01);
 		// make sure it returns back significance value as double
 		assertEquals(double.class, anovaResultSet.getSignificance(view
				.getMarkerPanel().get(0)));
 		// make sure it returns back significance value as we specified (0.01).
 		assertEquals(0.01, anovaResultSet.getSignificance(view.getMarkerPanel()
 				.get(0)));
 		// make sure we can specify significance value larger the 0.2.
 		anovaResultSet.setSignificance(view.getMarkerPanel().get(0), 0.2);
 		assertEquals(0.2, anovaResultSet.getSignificance(view.getMarkerPanel()
 				.get(0)));
 	}
 
 	/**
 	 * 
 	 */
 	public final void testSetSignificanceDSGeneMarkerDouble() {
 		// test constructor for local
 		DSAnovaResultSet<DSGeneMarker> anovaResultSet = new CSAnovaResultSet<DSGeneMarker>(
 				view, "Anova Analysis Result Set", groupNames, 0.05, markerNames,
 				result2DArray);
 		anovaResultSet.setSignificance(view.getMarkerPanel().get(0), 0.01);
 		assertEquals(0.01, anovaResultSet.getSignificance(view.getMarkerPanel()
 				.get(0)));
 		anovaResultSet.setSignificance(view.getMarkerPanel().get(0), 0.2);
 		assertEquals(0.2, anovaResultSet.getSignificance(view.getMarkerPanel()
 				.get(0)));
 	}
 
 	/**
 	 * 
 	 */
 	public final void testGetResult2DArray() {
 		// we'll put in a result2DArray and get it out.
 		// put it in
 		DSAnovaResultSet<DSGeneMarker> anovaResultSet = new CSAnovaResultSet<DSGeneMarker>(
 				view, "Anova Analysis Result Set", groupNames, 0.05, markerNames,
 				result2DArray);
 		// get it out
 		assertSame(result2DArray, anovaResultSet.getResult2DArray());
 	}
 
 	/**
 	 * 
 	 */
 	public final void testGetPValue() {
 		// we'll put in a result2DArray and get the pvalue out.
 		// put result2DArray in
 		DSAnovaResultSet<DSGeneMarker> anovaResultSet = new CSAnovaResultSet<DSGeneMarker>(
 				view, "Anova Analysis Result Set", groupNames, 0.05, markerNames,
 				result2DArray);
 		// get p-value out
 		assertEquals(result2DArray[0][0], anovaResultSet.getPValue(view
 				.getMarkerPanel().get(0)));
 		assertEquals(result2DArray[0][result2DArray[0].length - 1],
 				anovaResultSet.getPValue(view.getMarkerPanel().get(
 						result2DArray[0].length - 1)));
 	}
 
 	/**
 	 * 
 	 */
 	public final void testGetAdjPValue() {
 		// we'll put in a result2DArray and get the pvalue out.
 		// put result2DArray in
 		DSAnovaResultSet<DSGeneMarker> anovaResultSet = new CSAnovaResultSet<DSGeneMarker>(
 				view, "Anova Analysis Result Set", groupNames, 0.05, markerNames,
 				result2DArray);
 		// get p-value out
 		assertEquals(result2DArray[1][0], anovaResultSet.getAdjPValue(view
 				.getMarkerPanel().get(0)));
 		assertEquals(result2DArray[1][result2DArray[0].length - 1],
 				anovaResultSet.getAdjPValue(view.getMarkerPanel().get(
 						result2DArray[0].length - 1)));
 	}
 
 	/**
 	 * 
 	 */
 	public final void testGetFStatistic() {
 		// we'll put in a result2DArray and get the fvalue out.
 		// put result2DArray in
 		DSAnovaResultSet<DSGeneMarker> anovaResultSet = new CSAnovaResultSet<DSGeneMarker>(
 				view, "Anova Analysis Result Set", groupNames, 0.05, markerNames,
 				result2DArray);
 		// get f-value out
 		assertEquals(result2DArray[2][0], anovaResultSet.getFStatistic(view
 				.getMarkerPanel().get(0)));
 		assertEquals(result2DArray[2][result2DArray[0].length - 1],
 				anovaResultSet.getFStatistic(view.getMarkerPanel().get(
 						result2DArray[0].length - 1)));
 	}
 
 	/**
 	 * 
 	 */
 	public final void testGetMean() {
 		// we'll put in a result2DArray and get the mean out.
 		// put result2DArray in
 		DSAnovaResultSet<DSGeneMarker> anovaResultSet = new CSAnovaResultSet<DSGeneMarker>(
 				view, "Anova Analysis Result Set", groupNames, 0.05, markerNames,
 				result2DArray);
 		// get mean-value out
 		assertEquals(result2DArray[3][0], anovaResultSet.getMean(view
 				.getMarkerPanel().get(0), groupNames[0]));
 		// test mean of last group (ex:G3) for the last marker (ex:gene_label_8)
 		int groupIndex = groupNames.length - 1;
 		int markerIndex = result2DArray[0].length - 1;
 		assertEquals(result2DArray[3 + groupIndex * 2][markerIndex],
 				anovaResultSet.getMean(view.getMarkerPanel().get(markerIndex),
 						groupNames[groupIndex]));
 	}
 
 	/**
 	 * 
 	 */
 	public final void testGetDeviation() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	/**
 	 * 
 	 */
 	public final void testMicroarraySetViewSetter() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	/**
 	 * 
 	 */
 	public final void testSignificantMarkerNamesGetter() {
 		DSAnovaResultSet<DSGeneMarker> anovaResultSet = new CSAnovaResultSet<DSGeneMarker>(
 				view, "Anova Analysis Result Set", groupNames, 0.05,  markerNames,
 				result2DArray);
 		// make sure it returns back correct number of marker names
 		assertEquals(numMarkers,
 				anovaResultSet.significantMarkerNamesGetter().length);
 		// make sure it returns back marker names as array of string
 		assertEquals(String[].class, anovaResultSet
 				.significantMarkerNamesGetter().getClass());
 		// make sure it returns back the object we expect. (ex:markerNames)
 		assertSame(markerNames, anovaResultSet.significantMarkerNamesGetter());
 	}
 
 }
