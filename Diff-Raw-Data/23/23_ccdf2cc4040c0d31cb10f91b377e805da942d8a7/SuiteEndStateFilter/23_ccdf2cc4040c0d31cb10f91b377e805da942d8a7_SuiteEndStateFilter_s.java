 package org.chorusbdd.chorus.tools.webagent.filter;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.chorusbdd.chorus.results.EndState;
import org.chorusbdd.chorus.tools.webagent.JmxManagementServerExporter;
 import org.chorusbdd.chorus.tools.webagent.WebAgentTestSuite;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: nick
  * Date: 23/01/13
  * Time: 09:22
  */
 public class SuiteEndStateFilter extends AbstractSuiteFilter {
 
     public static final String SUITE_END_STATE_HTTP_PARAMETER = "suiteEndState";
     private static final Log log = LogFactory.getLog(SuiteEndStateFilter.class);
 
     List<EndState> filterEndStates = new ArrayList<EndState>();
 
     public SuiteEndStateFilter(String[] endStates, TestSuiteFilter wrappedFilter) {
         super(wrappedFilter);
         for ( String s : endStates) {
             EndState e = EndState.valueOf(s.toUpperCase());
             if ( e != null ) {
                 filterEndStates.add(e);
             } else {
                 log.warn("Unsupported end state in suite end state filter [" + s + "] this filter will not be applied");
             }
         }
     }
 
 
     @Override
     protected boolean applyFilter(WebAgentTestSuite suite) {
         boolean accept = false;
         for (EndState s : filterEndStates) {
             if ( suite.getEndState() == s ) {
                 accept = true;
                 break;
             }
         }
         return accept;
     }
 
     @Override
     public void reset() {
     }
 }
