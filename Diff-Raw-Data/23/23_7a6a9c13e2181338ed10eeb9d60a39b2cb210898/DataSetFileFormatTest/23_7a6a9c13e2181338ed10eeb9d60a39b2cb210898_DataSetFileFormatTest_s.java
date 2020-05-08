 package org.geworkbench.parsers;
 
 import junit.framework.TestCase;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSExprMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.parsers.DataSetFileFormat;

import java.io.File;
 
 /**
  * Tests loading of microarray sets, and makes loading services available to
  * other tests.
  * 
  * @author John Watkinson
  * @version $Id: DataSetFileFormatTest.java,v 1.1 2008-12-09 22:09:40 keshav Exp $
  */
 public class DataSetFileFormatTest extends TestCase {
 
 	public static final String DEFAULT_MICROARRAY_SET = "data/aTestDataSet.exp";
 
 	/**
 	 * 
 	 * @param fileName
 	 * @param fileFormat
 	 * @return
 	 * @throws Exception
 	 */
 	private DSDataSet<? extends DSBioObject> loadDataSet(String fileName, DataSetFileFormat fileFormat)
 			throws Exception {
 		System.out.println("Loading file...");
 		DSDataSet<? extends DSBioObject> dataSet = fileFormat.getDataFile(new File(fileName));
 		System.out.println("...file loaded.");
 		return dataSet;
 	}
 
 	/**
 	 * 
 	 * @return
 	 * @throws Exception
 	 */
	private CSExprMicroarraySet loadDefaultMicroarraySet() throws Exception {
 		String fileName = DEFAULT_MICROARRAY_SET;
 		DataSetFileFormat fileFormat = new org.geworkbench.parsers.ExpressionFileFormat();
 		DSDataSet<? extends DSBioObject> dataSet = loadDataSet(fileName, fileFormat);
		return (CSExprMicroarraySet) dataSet;
 	}
 
 	/**
 	 * Tests loading data.
 	 * 
 	 * @throws Exception
 	 */
 	public void testLoadData() throws Exception {
		CSExprMicroarraySet dataSet = loadDefaultMicroarraySet();
 		assertNotNull(dataSet);
 	}
 }
