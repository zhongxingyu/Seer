 package org.chorusbdd.chorus.tools.webagent.httpconnector;
 
 import junit.framework.TestSuite;
 import org.chorusbdd.chorus.ChorusJUnitRunner;
 
 /**
  * User: nick
  * Date: 26/12/12
  * Time: 12:19
  */
 public class TestHttpConnector {
 
     public static TestSuite suite() {
         return ChorusJUnitRunner.suite("" +
             "-f src/test/java/org/chorusbdd/chorus/tools/webagent/httpconnector/HttpConnector.feature " +
             "-h org.chorusbdd.chorus.tools.webagent" +
            " -l warn"// +
             //" -t @ThisOne"
         );
     }
 }
