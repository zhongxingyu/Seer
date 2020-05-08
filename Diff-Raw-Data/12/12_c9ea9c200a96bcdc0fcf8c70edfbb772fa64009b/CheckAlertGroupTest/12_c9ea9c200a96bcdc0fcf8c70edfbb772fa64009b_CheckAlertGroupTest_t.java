 package com.hgst.checkalertgroup;
 
 import java.util.Arrays;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 public class CheckAlertGroupTest extends TestCase {
 
 	protected CheckAlertGroup checkAlertGroup = new CheckAlertGroup();
 	
 	public void testGenerateOneSQL() {
 		String[] conditions = new String[]{
 				"ibm/spcHMNY/area/FAB2A", 
 				"ibm/spcHMNY/operation/RBQST2", 
 				"ibm/spcHMNY/productcode/MARS-K+_R"
 			};
 		String sql = checkAlertGroup.generateOneSQL(conditions);
		System.out.println(sql);
 		assertTrue(true);
 	}
 	
 	public void testGetCheckSQL() {
 //		List<String> list = Arrays.asList(
 //				"ibm/spcHMNY/area/FAB2A", 
 //				"ibm/spcHMNY/operation/RBQST2", 
 //				"ibm/spcHMNY/productcode/MARS-K+_R"
 //			);
 		List<String> list = Arrays.asList(
 				"ibm/spcHMNY/area/HARMONY", 
 				"ibm/spcHMNY/operation/PTR_AFTR_ABSOC", 
 				"ibm/spcHMNY/parameter/DELTA_S2", 
 				"ibm/spcHMNY/charttype/Individual", 
 				"ibm/spcHMNY/chartfrequency/PER_LOT", 
 				"ibm/spcHMNY/family/MFG_KOV_MP"
 				);
 		String sql = checkAlertGroup.getCheckSQL(list);
 		System.out.println(sql);
 		assertTrue(true);
 	}
 }
