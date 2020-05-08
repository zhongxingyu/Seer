 package org.cloudifysource.quality.iTests.framework.utils;
 
 import junit.framework.Assert;
 
 import org.openspaces.admin.gsc.GridServiceContainer;
 import org.openspaces.admin.gsm.GridServiceManager;
 
 
 import com.gigaspaces.log.LogEntries;
 import com.gigaspaces.log.LogEntry;
 import com.gigaspaces.log.LogEntryMatcher;
 import com.gigaspaces.log.LogEntryMatchers;
 
 import org.cloudifysource.quality.iTests.framework.testng.SGTestNGReporter;
 import org.cloudifysource.quality.iTests.framework.utils.AssertUtils.RepetitiveConditionProvider;
 
import java.util.List;

 public class LogUtils {
     
     public static void log(String message) {
         log(message, null);
     }
 
     public static void log(String message, Throwable t) {
         SGTestNGReporter.log(message, t, true);
     }
     
     /**
      * scans the logs of the GSC for a specific error message
      * prints the corresponding log entries of the error
      * @param container - the container to scan
      * @param error - witch error to look for
      */
     
     public static void scanContainerLogsFor(GridServiceContainer container , String error) {
     	
     	log("Scanning container " + container.getUid() + "for text : " + error);
     	
     	LogEntryMatcher matcher = LogEntryMatchers.containsString(error);
 
     	LogEntries logEntriesContainer1 = container.logEntries(matcher);
        List<LogEntry> entries = logEntriesContainer1.getEntries();
        if (entries.size() > 1) {
    		LogUtils.log("Found " + entries.size() + " entries containing " + error);
    		for (LogEntry logEntry : entries) {
    			LogUtils.log(logEntry.getText());
     		}
     		Assert.fail("Severe Errors during Deployment");
     	}
 
     }
     
     /**
      * scans the logs of the GSM for a specific error message
      * prints the corresponding log entries of the error
      * @param container - the container to scan
      * @param error - witch error to look for
      */
     
     public static void scanManagerLogsFor(GridServiceManager gsm , String error) {
     	
     	LogEntryMatcher matcher = LogEntryMatchers.containsString(error);
     	LogEntries logEntriesGsm = gsm.logEntries(matcher);
     	if (logEntriesGsm.getEntries().size() > 1) {
 			for (LogEntry logEntry : logEntriesGsm.logEntries()) {
 				log(logEntry.getText());		
 			}
 			Assert.fail("Severe Errors during Deployment");
 		}
     }
     
     
     /**
      * scans the logs of the GSM repetatively for a specific text message
      * assert fails if text is not found in logs after timeout passes
      * @param gsm - the gsm to scan
      * @param text - the text to look for
      */
     
     public static void repetativeScanManagerLogsFor(final GridServiceManager gsm , final String text, long timeoutInMillis) {
         RepetitiveConditionProvider condition = new RepetitiveConditionProvider() {
             public boolean getCondition() {
                 LogEntryMatcher matcher = LogEntryMatchers.containsString(text);
                 LogEntries logEntriesGsm = gsm.logEntries(matcher);
                 return logEntriesGsm.getEntries().size() > 1;
             }
         };
         AssertUtils.repetitiveAssertTrue("Failed finding: " + text + " in the gsm logs", condition, timeoutInMillis);       
     }
     
     /**
      * scans the logs of the GSC repetatively for a specific text message
      * assert fails if text is not found in logs after timeout passes
      * @param gsc - the gsm to scan
      * @param text - the text to look for
      */
     
     public static void repetativeScanContainerLogsFor(final GridServiceContainer gsc , final String text, long timeoutInMillis) {
         RepetitiveConditionProvider condition = new RepetitiveConditionProvider() {
             public boolean getCondition() {
                 LogEntryMatcher matcher = LogEntryMatchers.containsString(text);
                 LogEntries logEntriesGSC = gsc.logEntries(matcher);
                 return logEntriesGSC.getEntries().size() > 1;
             }
         };
         AssertUtils.repetitiveAssertTrue("Failed finding: " + text + " in the gsc logs", condition, timeoutInMillis);       
     }
     
 }
