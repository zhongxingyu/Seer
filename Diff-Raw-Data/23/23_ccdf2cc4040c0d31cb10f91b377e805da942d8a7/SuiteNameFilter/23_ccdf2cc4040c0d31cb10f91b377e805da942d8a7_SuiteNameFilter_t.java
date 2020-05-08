 package org.chorusbdd.chorus.tools.webagent.filter;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.chorusbdd.chorus.tools.webagent.WebAgentTestSuite;
 
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * User: nick
  * Date: 23/01/13
  * Time: 09:22
  */
 public class SuiteNameFilter extends AbstractSuiteFilter {
 
     public static final String SUITE_NAME_HTTP_PARAMETER = "suiteName";
     private static final Log log = LogFactory.getLog(SuiteNameFilter.class);
 
     List<Pattern> suiteNamePatterns = new ArrayList<Pattern>();
 
     public SuiteNameFilter(String[] suiteNames, TestSuiteFilter wrappedFilter) {
         super(wrappedFilter);
        addSuiteNames(suiteNames);
    }

    private void addSuiteNames(String... suiteNames) {
         for ( String suiteName : suiteNames) {
             try {
                 Pattern p = Pattern.compile(suiteName, Pattern.CASE_INSENSITIVE);
                 suiteNamePatterns.add(p);
             } catch (Exception e) {
                 log.warn("Failed to compile pattern for suite name filter: [" + suiteName + "] this filter will not be used", e);
             }
         }
     }
 
     protected boolean applyFilter(WebAgentTestSuite suite) {
         boolean accept = false;
         for (Pattern p : suiteNamePatterns) {
             Matcher m = p.matcher(suite.getTestSuiteName());
             if ( m.find()) {
                 accept = true;
                 break;
             }
         }
         return accept;
     }
 
     @Override
     public void reset() {
        super.reset();
        suiteNamePatterns.clear();
     }
 }
