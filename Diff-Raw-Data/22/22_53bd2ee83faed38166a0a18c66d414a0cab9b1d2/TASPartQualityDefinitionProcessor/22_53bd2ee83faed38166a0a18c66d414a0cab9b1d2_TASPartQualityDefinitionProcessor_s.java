 package com.thales.palma.csvgenmigration.processors;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.List;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 
 import au.com.bytecode.opencsv.CSVReader;
 import au.com.bytecode.opencsv.CSVWriter;
 
 import com.thales.palma.csvgenmigration.beans.SuperTASBean;
 import com.thales.palma.csvgenmigration.beans.TASItemBean;
 
 public class TASPartQualityDefinitionProcessor extends AbstractCsvGenProcessor {
 
 	private static final String RELEASED_STATE = "RELEASED";
 	
 	@Override
 	public void processOneFile(File csvFile, CSVWriter loadActionWriter)
 			throws FileNotFoundException {
 		
 		List<? extends SuperTASBean> tasBeanList = null;
 		
		 CSVReader reader = new CSVReader(new FileReader(csvFile), '|', '\"', 1);
 		 
 		 
 		 if(StringUtils.contains(csvFile.getName(), prtPatternStr)) {
 			 
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
 		
 		List<TASItemBean> tasItemBeanList = (List<TASItemBean>)tasBeanList;
 
 		/* Proceed with all the lines that match the condition (state RELEASED) */
 		for(TASItemBean tasBean : tasItemBeanList) {
 	        
 			 if( RELEASED_STATE.equals(tasBean.get_95lifeCycleState())
 					 && StringUtils.isNotEmpty(tasBean.get_908pdmPart()) ) {
 				 
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
