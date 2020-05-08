 package eu.dm2e.ws.tests.integration.services.workflow;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 
 import java.net.URI;
 
 import javax.ws.rs.client.Entity;
 import javax.ws.rs.core.Response;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import eu.dm2e.logback.LogbackMarkers;
 import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.api.FilePojo;
 import eu.dm2e.ws.api.JobPojo;
 import eu.dm2e.ws.api.WebserviceConfigPojo;
 import eu.dm2e.ws.api.WebservicePojo;
 import eu.dm2e.ws.model.JobStatus;
 import eu.dm2e.ws.services.xslt.XsltService;
import eu.dm2e.ws.tests.OmnomTestCase;
import eu.dm2e.ws.tests.OmnomTestResources;
 
 /**
  * This file was created within the DM2E project.
  * http://dm2e.eu
  * http://github.com/dm2e
  * <p/>
  * Author: Kai Eckert, Konstantin Baierer
  */
 public class StepByStepIngestionITCase extends OmnomTestCase {
 
     String XSLT_SERVICE_URI;
     String PUBLISH_SERVICE_URI;
     private String XSLTZIP_SERVICE_URI;
 
     @Before
     public void setUp() throws Exception {
         PUBLISH_SERVICE_URI = URI_BASE + "publish";
         XSLT_SERVICE_URI = URI_BASE + "service/xslt";
        XSLTZIP_SERVICE_URI = URI_BASE + "service/xslt";
 //    	SERVICE_POJO = new XsltZipService().getWebServicePojo();
     }
 
     @After
     public void tearDown() {
     }
 
 
     protected String doXSLTZIP() throws Exception {
         String XSLTZIP_URI_1;
         String XML_URI_1;
 
 
        FilePojo xsltzip_fp = new FilePojo();
        xsltzip_fp.setFileType(NS.OMNOM_TYPES.ZIP_XSLT);
        XSLTZIP_URI_1 = client.publishFile(configFile.get(OmnomTestResources.TEI2DM2E_20130605), xsltzip_fp);
         if (null == XSLTZIP_URI_1) { fail("Couldn't store test file."); }
         log.info("XSLTZIP_URI_1: " + XSLTZIP_URI_1);
         XML_URI_1 = client.publishFile(configFile.get(OmnomTestResources.XML_DTA_GRIMM));
         if (null == XML_URI_1) { fail("Couldn't store test file."); }
         log.info("XML_URI_1: " + XML_URI_1);
 
 //        Map<String, String> templMap = new HashMap<String,String>();
 //            templMap.put(XsltZipService.PARAM_XML_IN, XML_URI_1);
 //            templMap.put("xsltZipInput", XSLTZIP_URI_1);
         WebservicePojo ws = new WebservicePojo();
         ws.loadFromURI(XSLTZIP_SERVICE_URI);
         WebserviceConfigPojo conf = new WebserviceConfigPojo();
         conf.setWebservice(ws);
        conf.addParameterAssignment(XsltService.PARAM_XML_IN, XML_URI_1);
        conf.addParameterAssignment(XsltService.PARAM_XSLT_IN, XSLTZIP_URI_1);
        conf.addParameterAssignment(XsltService.PARAM_XSLT_PARAM_DATASET, "IngestionTest");
        conf.addParameterAssignment(XsltService.PARAM_XSLT_PARAM_DATAPROVIDER, "dm2edev");
         conf.publishToService(client.getConfigWebTarget());
 
 
              Response confGETresp = client.target(conf.getId()).request().get();
             assertEquals(200, confGETresp.getStatus());
 //		GrafeoImpl g = new GrafeoImpl(confGETresp.getEntityInputStream());
 //		assertEquals(g.getCanonicalNTriples(), conf.getCanonicalNTriples());
 //		assertTrue(g.isGraphEquivalent(conf.getGrafeo()));
 
             Response resp = client.target(XSLTZIP_SERVICE_URI)
                     .request(DM2E_MediaType.TEXT_TURTLE)
                     .put(Entity.text(conf.getId()));
             log.info(resp.readEntity(String.class));
             assertEquals(202, resp.getStatus());
             log.info("JOB uri: " + resp.getLocation());
 
             JobPojo jobPojo = new JobPojo();
 
             for (
                     jobPojo.loadFromURI(resp.getLocation())
                     ;
                     ! jobPojo.getJobStatus().equals(JobStatus.FINISHED.toString())
                             &&
                             ! jobPojo.getJobStatus().equals(JobStatus.FAILED.toString())
                     ;
                     jobPojo.loadFromURI(resp.getLocation())) {
                 log.info(jobPojo.toLogString());
                 try {
                     Thread.sleep(2000);
                 } catch (InterruptedException e) {
                     throw new RuntimeException("An exception occurred: " + e, e);
                 }
 
             }
        return jobPojo.getOutputParameterValueByName(XsltService.PARAM_XML_OUT);
     }
 
 
 
 
     protected String doXSL() throws Exception {
         OmnomTestResources xmlRes = OmnomTestResources.INGESTION_XML;
         OmnomTestResources xslRes = OmnomTestResources.INGESTION_XSL;
         String xmlUri = client.publishFile(configFile.get(xmlRes));
         String xsltUri = client.publishFile(configFile.get(xslRes));
         assertNotNull(xmlUri);
         assertNotNull(xsltUri);
 
         WebservicePojo ws = new WebservicePojo();
         ws.loadFromURI(XSLT_SERVICE_URI);
         WebserviceConfigPojo tC = new WebserviceConfigPojo();
         assertThat(tC.getId(), is(nullValue()));
         tC.publishToService(client.getConfigWebTarget());
         assertThat(tC.getId(), not(nullValue()));
         log.info("config uri: " + tC.getId());
         tC.setWebservice(ws);
         tC.addParameterAssignment(XsltService.PARAM_XML_IN, xmlUri);
         tC.addParameterAssignment(XsltService.PARAM_XSLT_IN, xsltUri);
         tC.publishToService();
 
          Response resp = client.putPojoToService(tC, XSLT_SERVICE_URI);
         log.info(tC.getTurtle());
         log.info(resp.readEntity(String.class));
         assertEquals(202, resp.getStatus());
         assertNotNull(resp.getLocation());
         URI jobUri = resp.getLocation();
 
         JobPojo job = new JobPojo();
         job.loadFromURI(jobUri);
 
         int maxWait = 10;
         int i = 0;
         while (!(job.isFinished() || job.isFailed())) {
             if (i++ == maxWait) {
                 break;
             }
             try {
                 Thread.sleep(1000);
             } catch (InterruptedException e) {
 
             }
             job.loadFromURI(jobUri);
             log.info(job.toLogString());
         }
         String resultUri = job.getOutputParameterValueByName(XsltService.PARAM_XML_OUT);
         assertNotNull(resultUri);
         log.info("Job finished. Result is at " + resultUri );
 
         String xmlContent = client.target(resultUri).request().get(String.class);
         log.info(xmlContent);
         return resultUri;
     }
 
 
     protected void doPublish(String rdfUri) throws Exception {
 
         WebservicePojo ws = new WebservicePojo();
         ws.loadFromURI(PUBLISH_SERVICE_URI);
         WebserviceConfigPojo config = new WebserviceConfigPojo();
         try {
             config.setWebservice(ws);
             config.addParameterAssignment("to-publish", rdfUri);
             config.addParameterAssignment("dataset-id", "IngestionTest");
             config.addParameterAssignment("provider-id", "dm2edev");
             config.addParameterAssignment("label", "Test-Dataset (from StepByStepIngestionITCase)");
             config.addParameterAssignment("comment", "This dataset can safely be deleted.");
             // config.addParameterAssignment("endpoint-update", "http://lelystad.informatik.uni-mannheim.de:3030/dm2etest/update");
             // config.addParameterAssignment("endpoint-select", "http://lelystad.informatik.uni-mannheim.de:3030/dm2etest/sparql");
             config.publishToService(client.getConfigWebTarget());
 
             log.info("Configuration created for Test: " + config.getTurtle());
 
             Response response = client
                     .target(PUBLISH_SERVICE_URI)
                     .request()
                     .put(Entity.text(config.getId()));
             log.info("JOB STARTED WITH RESPONSE: " + response.getStatus() + " / Location: " + response.getLocation() + " / Content: " + response.readEntity(String.class));
             URI joburi = response.getLocation();
 
             /**
              * WAIT FOR JOB TO BE FINISHED
              */
             long i = 0 ,
                     maxTries = 100,
                     sleeptime = 1500;
             JobPojo job = new JobPojo(joburi);
             do {
                 if (i++ >= maxTries) {
                     fail("Publishing took more than" + (maxTries * sleeptime / 1000) + " seconds.");
                     break;
                 }
 
                 log.info("Check for status: " + job.getJobStatus());
                 log.info("Loop [# " + i + "]");
                 log.info(LogbackMarkers.DATA_DUMP, "JOB SO FAR: {}", job.getTerseTurtle());
                 try {
                     Thread.sleep(sleeptime);
                 } catch (InterruptedException e) {
                     throw new RuntimeException("An exception occurred: " + e, e);
                 }
                 job.loadFromURI(joburi);
             } while ( ! (job.isFinished() || job.isFailed()) );
 
             /**
              * CHECK IF JOB IS FINISHED
              */
             log.info("Status: " + job.getJobStatus());
             assertEquals(JobStatus.FINISHED.name(), job.getJobStatus());
             log.info("Log: " + job.toLogString());
 
         } catch (Exception e) {
             log.info("" + e);
             throw new RuntimeException("An exception occurred: " + e, e);
         }
     }
 
     @Test
     public void testIngestion() throws Exception {
         String result = doXSLTZIP();
        assertNotNull(result);
         doPublish(result);
     }
 
 }
