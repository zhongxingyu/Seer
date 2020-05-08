 /*
  * Copyright (C) 2010 by Andreas Truszkowski <ATruszkowski@gmx.de>
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  * All we ask is that proper credit is given for our work, which includes
  * - but is not limited to - adding the above copyright notice to the beginning
  * of your source code files, and to any copyright notice that you may distribute
  * with programs based on this work.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
  */
 package org.openscience.cdk.applications.taverna.qsar;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import net.sf.taverna.t2.activities.testutils.ActivityInvoker;
 
 import org.junit.Assert;
 import org.openscience.cdk.ChemFile;
 import org.openscience.cdk.applications.taverna.AbstractCDKActivity;
 import org.openscience.cdk.applications.taverna.CDKActivityConfigurationBean;
 import org.openscience.cdk.applications.taverna.CDKTavernaConstants;
 import org.openscience.cdk.applications.taverna.CDKTavernaTestCases;
 import org.openscience.cdk.applications.taverna.basicutilities.CDKObjectHandler;
 import org.openscience.cdk.interfaces.IAtomContainer;
 import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
 
 /**
  * Test class for the MDL SD file reader activity.
  * 
  * @author Andreas Truszkowski
  * 
  */
 public class CSVToQSARVectorActivityTest extends CDKTavernaTestCases {
 
 	private CDKActivityConfigurationBean configBean;
 
 	private AbstractCDKActivity activity = new CSVToQSARVectorActivity();
 
 	public CSVToQSARVectorActivityTest() {
 		super(CSVToQSARVectorActivity.CSV_TO_QSAR_VECTOR_ACTIVITY);
 	}
 
 	public void makeConfigBean() throws Exception {
 		configBean = new CDKActivityConfigurationBean();
 		// TODO read resource
		File[] csvFile = new File[] { new File("src" + File.separator + "test" + File.separator + "resources" + File.separator
				+ "data" + File.separator + "qsar" + File.separator + "qsar.csv") };
 		configBean.addAdditionalProperty(CDKTavernaConstants.PROPERTY_FILE, csvFile);
 		configBean.setActivityName(CSVToQSARVectorActivity.CSV_TO_QSAR_VECTOR_ACTIVITY);
 	}
 
 	public void executeAsynch() throws Exception {
 		activity.configure(configBean);
 		// leave empty. No ports used
 		Map<String, Object> inputs = new HashMap<String, Object>();
 		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
 		expectedOutputTypes.put(activity.getRESULT_PORTS()[0], byte[].class);
 		expectedOutputTypes.put(activity.getRESULT_PORTS()[1], byte[].class);
 		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputTypes);
 		Assert.assertEquals("Unexpected outputs", 2, outputs.size());
 		byte[] objectData = (byte[]) outputs.get(activity.getRESULT_PORTS()[0]);
 		System.out.println();
 	}
 
 	@Override
 	protected void executeTest() {
 		try {
 			this.makeConfigBean();
 			this.executeAsynch();
 		} catch (Exception e) {
 			e.printStackTrace();
 			// This test causes an error
 			assertEquals(false, true);
 		}
 	}
 
 	/**
 	 * Method which returns a test suit with the name of this class
 	 * 
 	 * @return TestSuite
 	 */
 	public static Test suite() {
 		return new TestSuite(CSVToQSARVectorActivityTest.class);
 	}
 
 }
