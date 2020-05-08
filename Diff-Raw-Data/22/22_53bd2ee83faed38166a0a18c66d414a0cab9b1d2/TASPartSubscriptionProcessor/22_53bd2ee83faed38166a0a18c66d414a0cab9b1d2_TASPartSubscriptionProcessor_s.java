 package com.thales.palma.csvgenmigration.processors;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.List;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 
 import com.thales.palma.csvgenmigration.beans.SuperTASBean;
 
 import au.com.bytecode.opencsv.CSVReader;
 import au.com.bytecode.opencsv.CSVWriter;
 
 public class TASPartSubscriptionProcessor extends AbstractCsvGenProcessor {
 
 	private static final String MDP_RELEASED_STATE = "MDP_RELEASED";
 	
 	@Override
 	public void processOneFile(File csvFile, CSVWriter loadActionWriter)
 			throws FileNotFoundException {
 		
 		
 		List<? extends SuperTASBean> tasBeanList = null;
 		
		 CSVReader reader = new CSVReader(new FileReader(csvFile), '|', '\"', 1);
 		 
 		 if(StringUtils.contains(csvFile.getName(), _3ePatternStr)
 				 || StringUtils.contains(csvFile.getName(), n3ePatternStr)) {
 			 
 			 tasBeanList = obtainComponentTASBeans(reader);
 		 }
 		 else if(StringUtils.contains(csvFile.getName(), prtPatternStr)) {
 			 
 			 tasBeanList = obtainItemTASBeans(reader);
 		 }
 		 
            
 		 /* Proceed with the list lines */
 		 if(CollectionUtils.isNotEmpty(tasBeanList)) {
 			 
 			 printCsvFile(tasBeanList, loadActionWriter);
 			 
 			 
 		 }
 
 	}
 
 	
 	@Override
 	protected void printCsvFile(List<? extends SuperTASBean> tasBeanList,
 			CSVWriter loadActionWriter) {
 
 		/* Proceed with all the lines that match the condition (state MDP_RELEASED) */
 		for(SuperTASBean tasBean : tasBeanList) {
 	        
 			 if(MDP_RELEASED_STATE.equals( tasBean.get_95lifeCycleState())) {
 				 
 				 StringBuilder entriesStr = new StringBuilder( getCsvLoadAction() );
 				 entriesStr.append(ENTRIES_SEP_STR);
 				 entriesStr.append(tasBean.get_05number());
 				 entriesStr.append(ENTRIES_SEP_STR);
 				 entriesStr.append(tasBean.get_07view());
 				 entriesStr.append(ENTRIES_SEP_STR);
 				 entriesStr.append(tasBean.get_11version());
 				
 				 
 				 String[] entries = entriesStr.toString().split(ENTRIES_SEP_STR);
 				 
 				 loadActionWriter.writeNext(entries);
 				 
 			 }
 	        	
 	      }
 		
 	}
 
 }
