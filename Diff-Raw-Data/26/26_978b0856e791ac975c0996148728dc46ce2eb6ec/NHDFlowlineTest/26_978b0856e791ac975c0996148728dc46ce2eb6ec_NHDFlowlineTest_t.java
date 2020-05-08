 package org.opengeo.usgs;
 
 import java.util.Arrays;
 import java.util.List;
 
 public class NHDFlowlineTest extends USGSTestSupport {
 
     public NHDFlowlineTest() {
         // load test pg data
         setRunDBSetup(true);
     }
     
     public void testInsertsFail() throws Exception {
         
         // nhdflowline MustNotCross rules
        List<String> files = Arrays.asList("xml/rule-7-fail.xml");
         
         for (String file : files) {
             String code = file.split("-")[1];
             assertViolatesRule(file, code, "js:MustNotCross");
         }
 
        // nhdflowline PipelineMustHaveVerticalRelationship rules
        files = Arrays.asList("xml/rule-6-fail.xml");
        
        for (String file : files) {
            String code = file.split("-")[1];
            assertViolatesRule(file, code, "js:PipelineMustHaveVerticalRelationship");
        }

     }
 
 }
