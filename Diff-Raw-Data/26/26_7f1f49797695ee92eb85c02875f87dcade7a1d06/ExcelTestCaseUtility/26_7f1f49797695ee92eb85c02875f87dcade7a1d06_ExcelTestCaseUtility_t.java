 package edu.wustl.catissuecore.bizlogic.test;
 
 import java.util.Collection;
 import java.util.HashSet;
 
 import edu.wustl.catissuecore.domain.CollectionProtocol;
 import edu.wustl.catissuecore.domain.Site;
 import edu.wustl.catissuecore.domain.StorageContainer;
 import edu.wustl.catissuecore.domain.StorageType;
 
 public class ExcelTestCaseUtility extends CaTissueBaseTestCase {
 
 
 
 	/**
 	 * This method will read the source data from the excel sheet with
 	 * pre-decided format and will try to create participants
 	 * as specified in the data source
 	 *
 	 * @throws Exception
 	 */
 	public static void registerParticipants() throws Exception {
 		try {
			System.out.println("---------START ExcelTestCaseUtility.registerParticipants----------");
 			System.out.println("user.dir  " + System.getProperty("user.dir"));
 			String excelFilePath = System.getProperty("user.dir")
 			//+ "/excelFiles/PD-PartPmiRace-5.xls";
 			// + "/excelFiles/Data_Source.xls";
 			 + "/excelFiles/Workbook1.xls";
 			ExcelFileReader EX_CP = new ExcelFileReader();
 			String allexcel[][] = EX_CP.setInfo(excelFilePath);
                         int rowCount = EX_CP.getRowCount();
 			//new DataMigrationUtil().initParticipant(allexcel, rowCount);
 			new DataMigrationUtil().writeToCaTissue(allexcel,rowCount);
			System.out.println("---------END ExcelTestCaseUtility.registerParticipant-----------");
 		} catch (Exception e) {
			System.out.println("Exception in registerParticipants");
 			e.printStackTrace();
 			throw e;
 		}
 	}
 
 }
