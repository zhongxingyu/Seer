 package org.geworkbench.parsers;
 
import java.io.File;

 import junit.framework.TestCase;

 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
 
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
	@SuppressWarnings("rawtypes")
	private CSMicroarraySet loadDefaultMicroarraySet() throws Exception {
 		String fileName = DEFAULT_MICROARRAY_SET;
 		DataSetFileFormat fileFormat = new org.geworkbench.parsers.ExpressionFileFormat();
 		DSDataSet<? extends DSBioObject> dataSet = loadDataSet(fileName, fileFormat);
		return (CSMicroarraySet) dataSet;
 	}
 
 	/**
 	 * Tests loading data.
 	 * 
 	 * @throws Exception
 	 */
	@SuppressWarnings("rawtypes")
 	public void testLoadData() throws Exception {
		CSMicroarraySet dataSet = loadDefaultMicroarraySet();
 		assertNotNull(dataSet);
 	}
 }
