 package org.goodreturn.story.exporter;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.csv.CSVUtils;
 import org.goodreturn.model.Story;
 import org.goodreturn.model.TempBl;
 import org.goodreturn.service.StoryLocalServiceUtil;
 import org.goodreturn.service.TempBlLocalServiceUtil;
 
 import com.liferay.portal.kernel.exception.SystemException;
 import com.liferay.portal.kernel.workflow.WorkflowConstants;
 
 /**
  * Basic class which performs data loading and exporting as CSV data.
  * Operations performed in a static context.
  * 
  * @author Kyle Pink
  */
 public class StoryExporter {
 
 	private static final String YES_VALUE = "yes";
 	private static final String NO_VALUE = "no";
 	
 	private static String booleanDataStringValue(boolean value) {
 		return value ? YES_VALUE : NO_VALUE;
 	}
 
 	/**
 	 * Retrieves the data required for the csv data export.
 	 * 
 	 * @return a LinkedList<String[]> data structure which contains the desired
 	 */
 	public static LinkedList<String[]> loadData(boolean approvedOnly) {
 
 		LinkedList<String[]> data = new LinkedList<String[]>();
 		HashMap<Long, String> loans = new HashMap<Long, String>();
 		List<Story> stories;
 
 		// Data retrieval.
 		try {
 			List<TempBl> tmpLoans = TempBlLocalServiceUtil.getAllTempBls();
 			for (TempBl currentLoan : tmpLoans) {
 				loans.put(currentLoan.getBorrower_Loan_Id(), currentLoan.getBorrower_Name());
 			}
 		} catch (SystemException e) {
 			//Nothing gets added to loans.
 		}
 
 		try {
 			if (approvedOnly) {
 				stories = StoryLocalServiceUtil.getStoryByStatus(WorkflowConstants.STATUS_APPROVED);
 			} else {
 				stories = StoryLocalServiceUtil.getAllStorys();
 
 			}
 		} catch (SystemException e) {
 			stories = Collections.EMPTY_LIST;
 		}
 		
 		//Prepares data for csving
 		data.add(new String[]{
 				"Loan ID",
 				"Borrower Name",
 				"Story Type",
 				"Story Text",
 				"Video URL",
 				"Video Acceptable Marketing",
				"Video Acceptable Publishing"});
 		
 		for (Story story : stories) {
 			data.add(new String[]{
 					String.valueOf(story.getAbacus_Borrower_Loan_Id()),
 					loans.get(story.getAbacus_Borrower_Loan_Id()),
 					story.getStory_Type(),
 					story.getStory_Text().replace("\n", ""),
 					story.getVideo_Url(),
 					booleanDataStringValue(story.getIs_Good_Enough_For_Marketing()),
					booleanDataStringValue(story.getIs_Good_Enough_For_Story())					
 			});
 		}
 
 		return data;
 	}
 
 
 	/**
 	 * Creates a csv file with given data provided, returns String file name
 	 * which represents data which has been written.
 	 * 
 	 * @param disposableListData - data to be written in CSV file, will be discarded from list as used.
 	 * @return String as new LinkedList object ready to be written.
 	 * @throws IOException if data output fails.
 	 */
 	public static LinkedList<String> createCsvData(LinkedList<String[]> disposableListData) throws IOException {
 		//Holds converted data as whole CSV string.
 		LinkedList<String> csvData = new LinkedList<String>();
 
 		//Converts lines into csv data.
 		String[] currentLine;
 		while(disposableListData.size() != 0){
 			currentLine = disposableListData.removeFirst();
 			csvData.add(CSVUtils.printLine(currentLine));
 		}
 
 		//Returns CSV lines as string builder.
 		return csvData;
 	}
 } 
