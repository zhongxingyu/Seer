 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.atomhopper.restconfig.resources.impl;
 
 import javax.ws.rs.core.Response.Status;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.bind.JAXBException;
 import org.atomhopper.restconfig.jaxb.generated.Author;
 import org.atomhopper.restconfig.jaxb.generated.ObjectFactory;
 import org.atomhopper.restconfig.jaxb.generated.Configuration;
 import javax.ws.rs.core.Response;
 import org.atomhopper.restconfig.jaxb.generated.AdapterDescriptor;
 import org.atomhopper.restconfig.jaxb.generated.ConfigurationDefaults;
 import org.atomhopper.restconfig.jaxb.generated.FeedConfiguration;
 import org.atomhopper.restconfig.jaxb.generated.HostConfiguration;
 import org.atomhopper.restconfig.jaxb.generated.WorkspaceConfiguration;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 public class AtomHopperConfigurationImplTest {
     
    private static final Logger LOG = LoggerFactory.getLogger(AtomHopperConfigurationImplTest.class);
     
     public AtomHopperConfigurationImplTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     /**
      * Test of modifyConfiguration method, of class AtomHopperConfigurationImpl.
      */
     @Test
     public void shouldReturnCreatedHTTP201() throws JAXBException {
         LOG.info("Testing shouldReturnCreatedHTTP201");
         
         ObjectFactory factory = new ObjectFactory();
         
         Author author = factory.createAuthor();
         author.setName("author");        
         
         ConfigurationDefaults configDefaults = factory.createConfigurationDefaults();
         configDefaults.setAuthor(author);
         
         HostConfiguration hostConfiguration = factory.createHostConfiguration();
         hostConfiguration.setDomain("localhost");
         
         AdapterDescriptor feedSourceAdapterDescriptor = factory.createAdapterDescriptor();
         feedSourceAdapterDescriptor.setReference("hibernate-feed-source");
 
         AdapterDescriptor publisherAdapterDescriptor = factory.createAdapterDescriptor();
         publisherAdapterDescriptor.setReference("hibernate-feed-publisher");        
         
         FeedConfiguration feedConfiguration = factory.createFeedConfiguration();
         feedConfiguration.setResource("/feed/");
         feedConfiguration.setTitle("A Feed");
         feedConfiguration.setFeedSource(feedSourceAdapterDescriptor);
         feedConfiguration.setPublisher(publisherAdapterDescriptor);
         
         WorkspaceConfiguration workspaceConfiguration = factory.createWorkspaceConfiguration();
         workspaceConfiguration.setResource("/namespace/");
         workspaceConfiguration.setTitle("A Workspace");
         workspaceConfiguration.getFeed().add(feedConfiguration);
         
         Configuration config = factory.createConfiguration();
         config.setHost(hostConfiguration);
         config.getWorkspace().add(workspaceConfiguration);
         config.setDefaults(configDefaults);
 
         AtomHopperConfigurationImpl instance = new AtomHopperConfigurationImpl();
         Response result = instance.modifyConfiguration(config);
         
         assertEquals(HttpServletResponse.SC_CREATED, result.getStatus());
     }
     
     @Test
     public void shouldReturnPreconditionFailedHTTP412() throws JAXBException {
         LOG.info("Testing shouldReturnPreconditionFailedHTTP412");
         
         AtomHopperConfigurationImpl instance = new AtomHopperConfigurationImpl();
         Response result = instance.modifyConfiguration(null);
         
         assertEquals(Status.PRECONDITION_FAILED.getStatusCode(), result.getStatus());
     } 
 }
