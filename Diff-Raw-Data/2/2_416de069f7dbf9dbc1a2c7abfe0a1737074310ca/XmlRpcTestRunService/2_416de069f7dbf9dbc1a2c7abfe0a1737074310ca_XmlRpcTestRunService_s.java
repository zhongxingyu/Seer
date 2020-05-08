 /*
  * The MIT License
  *
  * Copyright (c) <2012> <Bruno P. Kinoshita>
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.mozilla.testopia.service.xmlrpc;
 
 import java.util.Map;
 
 import org.apache.xmlrpc.client.XmlRpcClient;
 import org.mozilla.testopia.model.TestCase;
 import org.mozilla.testopia.model.TestRun;
 import org.mozilla.testopia.service.TestRunService;
 
 
 /**
  * XML-RPC implementation of Test Run.
  * @author Bruno P. Kinoshita - http://www.kinoshita.eti.br
  * @since 0.1
  */
 public class XmlRpcTestRunService implements TestRunService {
     // Attributes 
     // ------------------------------------------------------------------------
     private final XmlRpcClient client;
     // Methods
     // ------------------------------------------------------------------------
     /**
      * Create a XmlRpcTestRunService.
      * @param xmlRpcClient XML-RPC client
      */
     public XmlRpcTestRunService(XmlRpcClient xmlRpcClient) {
         client = xmlRpcClient;
     }
     /* (non-Javadoc)
      * @see org.mozilla.testopia.service.TestRunService#get(java.lang.Integer)
      */
     @SuppressWarnings("unchecked")
     public TestRun getTestRun(Integer id) {
         TestRun testRun = null;
         final Integer[] param = new Integer[]{id};
         try {
             Map<String, Object> r = (Map<String, Object>)this.client.execute("TestRun.get", param);
             testRun = new TestRun();
             testRun.setId(Integer.parseInt(""+r.get("run_id")));
             testRun.setSummary(""+r.get("summary"));
             testRun.setProductVersion(""+r.get("product_version"));
             testRun.setManager(""+r.get("manager_id"));
             testRun.setBuild(""+r.get("build_id"));
             testRun.setEnvironment(""+r.get("environment_id"));
             testRun.setPlanId(Integer.parseInt(""+r.get("plan_id")));
             testRun.setPlanTextVersion(Integer.parseInt(""+r.get("plan_text_version")));
             testRun.setCases(Integer.parseInt(""+r.get("case_count")));
             /*
              *  (java.util.HashMap<K,V>) {summary=123, product_version=unspecified, run_id=2, manager_id=2, case_count=1, build_id=2, environment_id=1, notes=, plan_text_version=1, start_date=2012-06-22 02:55:04, plan_id=1}
              */
         } catch (Throwable e) {
             e.printStackTrace();
         }
         return testRun;
     }
     
     /* (non-Javadoc)
      * @see org.mozilla.testopia.service.TestRunService#getTestCases(java.lang.Integer)
      */
     @SuppressWarnings("unchecked")
     public TestCase[] getTestCases(Integer id) {
         TestCase[] testCases = null;
         final Integer[] param = new Integer[]{id};
         try {
             Object[] r = (Object[])this.client.execute("TestRun.get_test_cases", param);
             if(r != null && r.length > 0) {
                 testCases = new TestCase[r.length];
                 for(int i = 0; i < testCases.length ; ++i) {
                     Object o = r[i];
                     if(o instanceof Map<?, ?>) {
                         final Map<String, Object> map = (Map<String, Object>)o;
                         final TestCase testCase = new TestCase();
                        testCase.setId(Integer.parseInt(""+map.get("case_status_id")));
                         testCase.setSummary(""+map.get("summary"));
                         testCase.setRequirement(""+map.get("requirement"));
                         testCase.setPriorityId(Integer.parseInt(""+map.get("priority_id")));
                         testCase.setAlias(""+map.get("alias"));
                         testCase.setAutomated((""+map.get("isautomated")).equals("1"));
                         testCase.setCategoryId(Integer.parseInt(""+map.get("category_id")));
                         testCase.setScript(""+map.get("script"));
                         testCase.setArguments(""+map.get("arguments"));
                         testCase.setDefaultTesterId(Integer.parseInt(""+map.get("default_tester_id")));
                         testCase.setAuthorId(Integer.parseInt(""+map.get("author_id")));
                         testCases[i] = testCase;
                         /*
                          * (java.util.HashMap<K,V>) {summary=My first test case, requirement=, priority_id=3, alias=myScript, case_status_id=2, isautomated=1, category_id=1, script=sample.Test, creation_date=2012-05-29 19:02:14, arguments=-Dparam=1, estimated_time=00:00:00, default_tester_id=1, case_id=1, author_id=1}
                          */
                     }
                 }
             }
             /*
              *  (java.util.HashMap<K,V>) [{summary=My first test case, requirement=, priority_id=3, alias=myScript, case_status_id=2, isautomated=1, category_id=1, script=sample.Test, creation_date=2012-05-29 19:02:14, arguments=-Dparam=1, estimated_time=00:00:00, default_tester_id=1, case_id=1, author_id=1}]
              */
         } catch (Throwable e) {
             e.printStackTrace();
         }
         return testCases;
     }
 
 }
