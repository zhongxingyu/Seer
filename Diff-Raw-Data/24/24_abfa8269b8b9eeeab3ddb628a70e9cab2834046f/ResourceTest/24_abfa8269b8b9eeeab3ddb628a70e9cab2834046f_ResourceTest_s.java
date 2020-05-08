 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.smartitengineering.generetor.engine.webservice.client;
 
 import com.google.inject.AbstractModule;
 import com.smartitengineering.cms.api.common.MediaType;
 import com.smartitengineering.generator.engine.webservice.domain.ReportConfig;
 import com.smartitengineering.generator.engine.webservice.domain.SourceCode;
 import com.smartitengineering.generator.engine.webservice.domain.SourceCodeType;
 import com.smartitengineering.generetor.engine.webservice.client.api.ConfigResource;
 import com.smartitengineering.generetor.engine.webservice.client.api.ConfigsResource;
 import com.smartitengineering.generetor.engine.webservice.client.api.Impl.RootResourceImpl;
 import com.smartitengineering.generetor.engine.webservice.client.api.RootResource;
 import com.smartitengineering.util.bean.guice.GuiceUtil;
 import com.smartitengineering.util.rest.client.ApplicationWideClientFactoryImpl;
 import com.smartitengineering.util.rest.client.ConnectionConfig;
 import com.smartitengineering.util.rest.client.jersey.cache.CacheableClient;
 import com.sun.jersey.api.client.Client;
 import java.io.File;
 import java.net.URI;
 import java.util.Properties;
 import javax.ws.rs.core.HttpHeaders;
 import junit.framework.Assert;
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.HandlerList;
 import org.eclipse.jetty.webapp.WebAppContext;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author saumitra
  */
 public class ResourceTest {
 
   private static Server jettyServer;
   private static final Logger LOGGER = LoggerFactory.getLogger(ResourceTest.class);
   private static final String CONTEXT_PATH = "/generator-engine";
   private static final String HOST = "localhost";
   private static final int PORT = 20080;
   private static final int SLEEP_DURATION = 3000;
   private static final URI ROOT_URI = URI.create("http://" + HOST + ":" + PORT + CONTEXT_PATH);
 
   @BeforeClass
   public static void setUp() throws Exception {
    Properties properties = new Properties();
    properties.setProperty(GuiceUtil.CONTEXT_NAME_PROP,
                           "com.smartitengineering.user.client");
    properties.setProperty(GuiceUtil.IGNORE_MISSING_DEP_PROP, Boolean.TRUE.toString());
    properties.setProperty(GuiceUtil.MODULES_LIST_PROP, ConfigurationModule.class.getName());
    GuiceUtil.getInstance(properties).register();
     /*
      * Start web application container
      */
     jettyServer = new Server(PORT);
     HandlerList handlerList = new HandlerList();
     /*
      * The following is for solr for later, when this is to be used it
      */
    System.setProperty("solr.solr.home", "./target/sample-conf/");
    Handler solr = new WebAppContext("./target/solr/", "/solr");
    handlerList.addHandler(solr);
     final String webapp = "../engine-webservice/src/main/webapp/";
     if (!new File(webapp).exists()) {
       throw new IllegalStateException("WebApp file/dir does not exist!");
     }
     WebAppContext webAppHandler = new WebAppContext(webapp, CONTEXT_PATH);
     handlerList.addHandler(webAppHandler);
     jettyServer.setHandler(handlerList);
     jettyServer.setSendDateHeader(true);
     jettyServer.start();
 
     /*
      * Setup client properties
      */
     System.setProperty(ApplicationWideClientFactoryImpl.TRACE, "true");
 
     Client client = CacheableClient.create();
     client.resource("http://localhost:10080/hub/api/channels/test").header(HttpHeaders.CONTENT_TYPE,
                                                                       MediaType.APPLICATION_JSON).put(
         "{\"name\":\"test\"}");
     LOGGER.info("Created test channel!");
   }
 
  public static class ConfigurationModule extends AbstractModule {

    @Override
    protected void configure() {
      ConnectionConfig config = new ConnectionConfig();
      config.setBasicUri("");
      config.setContextPath(CONTEXT_PATH);
      config.setHost(HOST);
      config.setPort(PORT);
      bind(ConnectionConfig.class).toInstance(config);
    }
  }

   @AfterClass
   public static void tearDown() throws Exception {
     jettyServer.stop();
   }
 
   @Test
   public void testRootResource() {
     RootResource rootResource = RootResourceImpl.getRoot(ROOT_URI);
     Assert.assertNotNull(rootResource);
     Assert.assertNotNull(rootResource.getConfigsResource());
   }
 
   @Test
   public void testReportConfig() {
     ReportConfig reportConfig = new ReportConfig();
     reportConfig.setId("id");
     reportConfig.setName("testName");
     reportConfig.setCronExpression("10 1 0 ? * *");
     SourceCode code = new SourceCode();
     code.setCodeType(SourceCodeType.GROOVY);
     final String someCode = "SomeCode";
     code.setCode(someCode);
     reportConfig.setCode(code);
     createConfig(reportConfig, someCode);
     reportConfig.setId("id2");
     createConfig(reportConfig, someCode);
     sleep();
   }
 
   protected void createConfig(ReportConfig reportConfig, String code) {
     ConfigsResource configsResource = RootResourceImpl.getRoot(ROOT_URI).getConfigsResource();
     long start = System.currentTimeMillis();
     ConfigResource result = configsResource.createConfig(reportConfig);
     long wend = System.currentTimeMillis();
     ReportConfig storedConfig = result.getConfig();
     long rend = System.currentTimeMillis();
     Assert.assertEquals(code, storedConfig.getCode().getCode());
     Assert.assertEquals(SourceCodeType.GROOVY, storedConfig.getCode().getCodeType());
     LOGGER.warn("Duration for writing and reading a config " + (rend - start));
     LOGGER.warn("Duration for writing a config " + (wend - start));
     LOGGER.warn("Duration for reading a config " + (rend - wend));
   }
 
   private static void sleep() {
     try {
       Thread.sleep(SLEEP_DURATION);
     }
     catch (Exception ex) {
       LOGGER.warn("Sleep broken!", ex);
     }
   }
 }
