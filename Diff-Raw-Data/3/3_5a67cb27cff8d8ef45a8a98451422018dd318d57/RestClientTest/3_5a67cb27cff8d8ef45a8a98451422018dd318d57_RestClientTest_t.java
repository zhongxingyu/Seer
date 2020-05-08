 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.spt.rms.rep.rest;
 
 import com.spt.rms.rep.domain.Report;
 import com.spt.rms.rep.domain.ReportParameter;
 import org.junit.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Date;
 import java.util.List;
 
 import static org.junit.Assert.*;
 
 /**
  *
  * Panupong Chantaklang
  * @since 31/01/2011
  */
 public class RestClientTest {
     private static Logger logger = LoggerFactory.getLogger(RestClientTest.class);
     RestClient instance = new RestClient();
 
     public RestClientTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
         logger.info("-= Setup class =-");
         RestClient instance = new RestClient();
 
         Report report = new Report();
         report.setCode("TT3");
         report.setDescription("Test Report 3");
         report.setLocalDescription("ทดสอบ 3");
         report.setReportType(Integer.parseInt("3"));
         report.setReportChange(Integer.parseInt("1"));
         report.setCreUsr("test");
         report.setLastUsr("test");
         report.setCreDate(new Date());
         report.setLastDate(new Date());
         report.setCriteriaPrint(Integer.parseInt("2"));
 
         instance.addReport(report);
         logger.info("-= Add Report TT3 =-");
 
         report = new Report();
         report.setCode("TT4");
         report.setDescription("Test Report 4");
         report.setLocalDescription("ทดสอบ 4");
         report.setReportType(Integer.parseInt("3"));
         report.setReportChange(Integer.parseInt("1"));
         report.setCreUsr("test");
         report.setLastUsr("test");
         report.setCreDate(new Date());
         report.setLastDate(new Date());
         report.setCriteriaPrint(Integer.parseInt("0"));
 
         instance.addReport(report);
         logger.info("-= Add Report TT4 =-");
 
         ReportParameter param = new ReportParameter();
         param.setCode("param3_1");
         param.setDescription("3aa");
         param.setLocalDescription("3กก");
         param.setParamType("1");
         param.setParamRequire(Integer.parseInt("1"));
         param.setParamLength(Integer.parseInt("4"));
         param.setHidden(Integer.parseInt("0"));
         param.setDefaultValue("3xxxx");
         param.setCreUsr("test");
         param.setLastUsr("test");
         param.setCreDate(new Date());
         param.setLastDate(new Date());
         param.setQuery("SELECT RPPPARAM,RPPEDESC FROM RPTPARAM");
         param.setReport(instance.getReportByCode("TT3"));
 
         instance.addReportParameter(param);
         logger.info("======================Add Report Parameter aaa Finished==================================");
 
         param = new ReportParameter();
         param.setCode("param3_2");
         param.setDescription("3bb");
         param.setLocalDescription("3ขข");
         param.setParamType("1");
         param.setParamRequire(Integer.parseInt("1"));
         param.setParamLength(Integer.parseInt("5"));
         param.setHidden(Integer.parseInt("1"));
         param.setDefaultValue("xxxxx");
         param.setCreUsr("test");
         param.setLastUsr("test");
         param.setCreDate(new Date());
         param.setLastDate(new Date());
         param.setReport(instance.getReportByCode("TT3"));
         param.setQuery("SELECT RPPPARAM,RPPEDESC FROM RPTPARAM");
 
         instance.addReportParameter(param);
         logger.info("======================Add Report Parameter bbb Finished==================================");
 
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
         logger.info("-= tearDownClass() =-");
         RestClient instance = new RestClient();
 
         ReportParameter parameter = instance.getReportParameterByCode("param3_1");
         logger.info("-= start delete Report Parameter {}=-",parameter);
         instance.deleteReportParameter(parameter);
         logger.info("-= finish delete Report parameter 1 =-");
         parameter = instance.getReportParameterByCode("param3_2");
         logger.info("-= start delete Report Parameter {}=-",parameter);
         instance.deleteReportParameter(parameter);
         logger.info("-= finish delete Report parameter 2 =-");
 
         Report report = instance.getReportByCode("TT4");
         logger.info("-= start delete Report {}=-",report);
         instance.deleteReport(report);
         logger.info("-= finish delete Report TT4 =-");
 
         report = instance.getReportByCode("TT3");
         logger.info("-= start delete Report {}=-",report);
         instance.deleteReport(report);
         logger.info("-= finish delete Report TT3 =-");
 
     }
 
     @Before
     public void setUp() {
         logger.info("-= Setup =-");
         
     }
 
     @After
     public void tearDown() {
         logger.info("-= TearDown =-");
     }
 
 
     /**
      * Test of listAllReport method, of class RestClient.
      */
     @Test
     public void testListAllReport() {
         logger.info("listAllReport");
         List result = instance.listAllReport();
         assertEquals(3, result.size());
     }
 
     /**
      * Test of listReportByCriteria method, of class RestClient.
      */
     @Test
     public void testListReportByCriteria() {
         logger.info("listReportByCriteria");
         String reportCode = "%TT%";
         String reportDescription = "%";
         List<Report> result = instance.listReportByCriteria(reportCode, reportDescription);
         assertEquals(2, result.size());
     }
 
     /**
      * Test of addReport method, of class RestClient.
      */
     @Test
     public void testAddReport() {
         logger.info("addReport");
         Report report = new Report();
         report.setCode("TT5");
         report.setDescription("Test Report 5");
         report.setLocalDescription("ทดสอบ 5");
         report.setReportType(Integer.parseInt("5"));
         report.setReportChange(Integer.parseInt("1"));
         report.setCreUsr("test");
         report.setLastUsr("test");
         report.setCreDate(new Date());
         report.setLastDate(new Date());
         report.setCriteriaPrint(Integer.parseInt("0"));
         boolean expResult = true;
         boolean result = instance.addReport(report);
         assertEquals(expResult, result);
         assertEquals(4, instance.listAllReport().size());
     }
 
     /**
      * Test of updateReport method, of class RestClient.
      */
     @Test
     public void testUpdateReport() {
         logger.info("updateReport");
         Report report = instance.getReportByCode("TT5");
        report.setCriteriaPrint(Integer.parseInt("9"));
        report.setReportType(Integer.parseInt("9"));
        report.setReportChange(Integer.parseInt("9"));
         report.setLastUsr("update");
         boolean expResult = true;
         boolean result = instance.updateReport(report);
         assertEquals(expResult, result);
         logger.info("Updated Report :{}",instance.getReportByCode("TT5"));
         assertEquals("update", instance.getReportByCode("TT5").getLastUsr());
     }
 
     /**
      * Test of deleteReport method, of class RestClient.
      */
     @Test
     public void testDeleteReport() {
         logger.info("deleteReport");
         Report report = instance.getReportByCode("TT5");
         boolean expResult = true;
         boolean result = instance.deleteReport(report);
         assertEquals(expResult, result);
         assertEquals(3, instance.listAllReport().size());
     }
 
     /**
      * Test of listReportParameterByReport method, of class RestClient.
      */
     @Test
     public void testListReportParameterByReport() {
         logger.info("listReportParameterByReport");
         List result = instance.listReportParameterByReport(instance.getReportByCode("TT3"));
         logger.info("Result : {}",result);
         assertEquals(2, result.size());
     }
 
     /**
      * Test of getReportParameter method, of class RestClient.
      */
     @Test
     public void testGetReportParameter() {
         logger.info("getReportParameter");
         ReportParameter result = instance.getReportParameterById(instance.getReportParameterByCode("param3_1").getId());
         assertEquals("param3_1", result.getCode());
     }
 
     /**
      * Test of addReportParameter method, of class RestClient.
      */
     @Test
     public void testAddReportParameter() {
         logger.info("addReportParameter");
         ReportParameter reportParameter = new ReportParameter();
         reportParameter.setCode("param4_1");
         reportParameter.setDescription("2zz");
         reportParameter.setLocalDescription("2ฮฮ");
         reportParameter.setParamType("1");
         reportParameter.setParamRequire(Integer.parseInt("1"));
         reportParameter.setParamLength(Integer.parseInt("4"));
         reportParameter.setHidden(Integer.parseInt("0"));
         reportParameter.setDefaultValue("xzxzxz");
         reportParameter.setCreUsr("test");
         reportParameter.setLastUsr("test");
         reportParameter.setCreDate(new Date());
         reportParameter.setLastDate(new Date());
         reportParameter.setQuery("SELECT RPPPARAM,RPPDESC FROM RPTPARAM");
         reportParameter.setReport(instance.getReportByCode("TT4"));
         boolean expResult = true;
         boolean result = instance.addReportParameter(reportParameter);
         assertEquals(expResult, result);
         assertNotNull(instance.listReportParameterByReport(instance.getReportByCode("TT4")));
     }
 
     /**
      * Test of updateReportParameter method, of class RestClient.
      */
     @Test
     public void testUpdateReportParameter() {
         logger.info("updateReportParameter");
         ReportParameter reportParameter = instance.getReportParameterByCode("param4_1");
 
         reportParameter.setDescription("test colon");
         reportParameter.setLocalDescription("ทดสอบ ทดสอบ");
          reportParameter.setHidden(Integer.parseInt("10"));
         reportParameter.setDefaultValue("yyyyyy");
         reportParameter.setLastUsr("update");
         reportParameter.setLastDate(new Date());
 
         boolean expResult = true;
         boolean result = instance.updateReportParameter(reportParameter);
         assertEquals(expResult, result);
         assertEquals("update", instance.getReportParameterByCode("param4_1").getLastUsr());
     }
 
     /**
      * Test of deleteReportParameter method, of class RestClient.
      */
     @Test
     public void testDeleteReportParameter() {
         logger.info("deleteReportParameter");
         ReportParameter reportParameter = instance.getReportParameterByCode("param4_1");
         boolean expResult = true;
         boolean result = instance.deleteReportParameter(reportParameter);
         assertEquals(expResult, result);
     }
 
 
 
     /**
      * Test of checkSQLStatement method, of class RestClient.
      */
     @Test
     public void testCheckSQLStatement() {
         logger.info("checkSQLStatement");
         String sql = instance.getReportParameterByCode("param3_1").getQuery();
         List result = instance.checkSQLStatement(sql);
         logger.info("result :{}",result);
         assertNotSame(0,result.size());
     }
 
 
 
 
 }
