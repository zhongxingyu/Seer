 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.redhat.engineering.jenkins.report.plugin;
 
 import com.redhat.engineering.jenkins.testparser.results.MatrixBuildTestResults;
 import org.junit.*;
 
 /**
  *
  * @author Jan Rusnacko (jrusnack at redhat.com)
  */
 public class ReportPluginBuildActionTest {
     private ReportPluginBuildAction a;
     
     public ReportPluginBuildActionTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
     
     @Before
     public void setUp() {
 	
     }
     
     @After
     public void tearDown() {
     }
 
     
     /**
      * Test constructor
      */
     @Test(expected = IllegalArgumentException.class)
     public void testReportPluginBuildAction(){
 	MatrixBuildTestResults tr = new MatrixBuildTestResults(null);
	ReportPluginBuildAction a = new ReportPluginBuildAction(null, tr, null);
     }
     
     
     
 }
