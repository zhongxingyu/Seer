 package org.fusesource.camel.component.salesforce;
 
 import org.apache.camel.Exchange;
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.component.mock.MockEndpoint;
 import org.apache.camel.test.junit4.CamelTestSupport;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.fusesource.camel.component.salesforce.api.dto.*;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.Properties;
 import java.util.concurrent.TimeUnit;
 
 public class SalesforceComponentTest extends CamelTestSupport {
 
     private static final Logger LOG = LoggerFactory.getLogger(SalesforceComponentTest.class);
    private static final String TEST_LOGIN_PROPERTIES = "/test-login.properties";
 
     private ObjectMapper objectMapper;
     private static final long TEST_TIMEOUT = 30;
 
     @Test
     public void testGetVersions() throws Exception {
         MockEndpoint mock = getMockEndpoint("mock:testGetVersions");
         mock.expectedMinimumMessageCount(1);
 
         // test versions doesn't need a body
         template().sendBody("direct:testGetVersions", null);
         assertMockEndpointsSatisfied(TEST_TIMEOUT, TimeUnit.SECONDS);
 
         // assert expected result
         Exchange ex = mock.getExchanges().get(0);
         List<Version> versions = ex.getIn().getBody(List.class);
         for (Version version : versions) {
             LOG.trace(String.format("Version: %s, %s, %s", version.getVersion(), version.getLabel(), version.getUrl()));
         }
 
         // test for xml response
         mock = getMockEndpoint("mock:testGetVersionsXml");
         mock.expectedMinimumMessageCount(1);
         template().sendBody("direct:testGetVersionsXml", null);
         assertMockEndpointsSatisfied(TEST_TIMEOUT, TimeUnit.SECONDS);
 
         // assert expected result
         ex = mock.getExchanges().get(0);
         Versions versions1 = ex.getIn().getBody(Versions.class);
         for (Version version : versions1.getVersions()) {
             LOG.trace(String.format("Version: %s, %s, %s", version.getVersion(), version.getLabel(), version.getUrl()));
         }
     }
 
     @Test
     public void testGetResources() throws Exception {
         MockEndpoint mock = getMockEndpoint("mock:testGetResources");
         mock.expectedMinimumMessageCount(1);
 
         template().sendBody("direct:testGetResources", null);
         assertMockEndpointsSatisfied(TEST_TIMEOUT, TimeUnit.SECONDS);
 
         // assert expected result
         Exchange ex = mock.getExchanges().get(0);
         RestResources resources = ex.getIn().getBody(RestResources.class);
         assertNotNull(resources);
         LOG.trace("Resources: " + resources);
 
         mock = getMockEndpoint("mock:testGetResourcesXml");
         mock.expectedMinimumMessageCount(1);
 
         template().sendBody("direct:testGetResourcesXml", null);
         assertMockEndpointsSatisfied(TEST_TIMEOUT, TimeUnit.SECONDS);
 
         // assert expected result
         ex = mock.getExchanges().get(0);
         resources = ex.getIn().getBody(RestResources.class);
         assertNotNull(resources);
         LOG.trace("Resources: " + resources);
     }
 
     @Test
     public void testGetGlobalObjects() throws Exception {
         MockEndpoint mock = getMockEndpoint("mock:testGetGlobalObjects");
         mock.expectedMinimumMessageCount(1);
 
         template().sendBody("direct:testGetGlobalObjects", null);
         assertMockEndpointsSatisfied(TEST_TIMEOUT, TimeUnit.SECONDS);
 
         // assert expected result
         Exchange ex = mock.getExchanges().get(0);
         GlobalObjects globalObjects = ex.getIn().getBody(GlobalObjects.class);
         assertNotNull(globalObjects);
         LOG.trace("GlobalObjects: " + globalObjects);
 
         mock = getMockEndpoint("mock:testGetGlobalObjectsXml");
         mock.expectedMinimumMessageCount(1);
 
         template().sendBody("direct:testGetGlobalObjectsXml", null);
         assertMockEndpointsSatisfied(TEST_TIMEOUT, TimeUnit.SECONDS);
 
         // assert expected result
         ex = mock.getExchanges().get(0);
         globalObjects = ex.getIn().getBody(GlobalObjects.class);
         assertNotNull(globalObjects);
         LOG.trace("GlobalObjects: " + globalObjects);
     }
 
     @Test
     public void testGetSObjectBasicInfo() throws Exception {
         MockEndpoint mock = getMockEndpoint("mock:testGetSObjectBasicInfo");
         mock.expectedMinimumMessageCount(1);
 
         template().sendBody("direct:testGetSObjectBasicInfo", null);
         assertMockEndpointsSatisfied(TEST_TIMEOUT, TimeUnit.SECONDS);
 
         // assert expected result
         Exchange ex = mock.getExchanges().get(0);
         SObjectBasicInfo objectBasicInfo = ex.getIn().getBody(SObjectBasicInfo.class);
         assertNotNull(objectBasicInfo);
         LOG.trace("SObjectBasicInfo: " + objectBasicInfo);
 
         mock = getMockEndpoint("mock:testGetSObjectBasicInfoXml");
         mock.expectedMinimumMessageCount(1);
 
         template().sendBody("direct:testGetSObjectBasicInfoXml", null);
         assertMockEndpointsSatisfied(TEST_TIMEOUT, TimeUnit.SECONDS);
 
         // assert expected result
         ex = mock.getExchanges().get(0);
         objectBasicInfo = ex.getIn().getBody(SObjectBasicInfo.class);
         assertNotNull(objectBasicInfo);
         LOG.trace("SObjectBasicInfo: " + objectBasicInfo);
     }
 
     @Test
     public void testGetSObjectDescription() throws Exception {
         MockEndpoint mock = getMockEndpoint("mock:testGetSObjectDescription");
         mock.expectedMinimumMessageCount(1);
 
         template().sendBody("direct:testGetSObjectDescription", null);
         assertMockEndpointsSatisfied(TEST_TIMEOUT, TimeUnit.SECONDS);
 
         // assert expected result
         Exchange ex = mock.getExchanges().get(0);
         SObjectDescription sObjectDescription = ex.getIn().getBody(SObjectDescription.class);
         assertNotNull(sObjectDescription);
         LOG.trace("SObjectDescription: " + sObjectDescription);
 
         mock = getMockEndpoint("mock:testGetSObjectDescriptionXml");
         mock.expectedMinimumMessageCount(1);
 
         template().sendBody("direct:testGetSObjectDescriptionXml", null);
         assertMockEndpointsSatisfied(TEST_TIMEOUT, TimeUnit.SECONDS);
 
         // assert expected result
         ex = mock.getExchanges().get(0);
         sObjectDescription = ex.getIn().getBody(SObjectDescription.class);
         assertNotNull(sObjectDescription);
         LOG.trace("SObjectDescription: " + sObjectDescription);
     }
 
     @Test
     public void testGetSObjectById() throws Exception {
         MockEndpoint mock = getMockEndpoint("mock:testGetSObjectById");
         mock.expectedMinimumMessageCount(1);
 
         template().sendBody("direct:testGetSObjectById", "a00E0000003hyI4IAI");
         assertMockEndpointsSatisfied(TEST_TIMEOUT, TimeUnit.SECONDS);
 
         // assert expected result
         Exchange ex = mock.getExchanges().get(0);
         AbstractSObjectBase sObjectBase = ex.getIn().getBody(AbstractSObjectBase.class);
         assertNotNull(sObjectBase);
         LOG.trace("SObjectById: " + sObjectBase);
 
         mock = getMockEndpoint("mock:testGetSObjectByIdXml");
         mock.expectedMinimumMessageCount(1);
 
         template().sendBody("direct:testGetSObjectByIdXml", "a00E0000003hyI4IAI");
         assertMockEndpointsSatisfied(TEST_TIMEOUT, TimeUnit.SECONDS);
 
         // assert expected result
         ex = mock.getExchanges().get(0);
         sObjectBase = ex.getIn().getBody(AbstractSObjectBase.class);
         assertNotNull(sObjectBase);
         LOG.trace("SObjectById: " + sObjectBase);
     }
 
     @Override
     protected RouteBuilder createRouteBuilder() throws Exception {
 
         // create a json mapper
         objectMapper = new ObjectMapper();
 
         // create the component
         SalesforceComponent component = new SalesforceComponent();
         setLoginProperties(component);
 
         // default component level payload format
         component.setFormat("json");
         // default api version
         component.setApiVersion("25.0");
 
         // add it to context
         context().addComponent("force", component);
 
         // create test route
         return new RouteBuilder() {
             public void configure() {
 
                 // testGetVersion
                 from("direct:testGetVersions")
                     .to("force://getVersions")
                     .to("mock:testGetVersions");
 
                 // allow overriding format per endpoint
                 from("direct:testGetVersionsXml")
                     .to("force://getVersions?format=xml")
                     .to("mock:testGetVersionsXml");
 
                 // testGetResources
                 from("direct:testGetResources")
                     .to("force://getResources")
                     .to("mock:testGetResources");
 
                 from("direct:testGetResourcesXml")
                     .to("force://getResources?format=xml")
                     .to("mock:testGetResourcesXml");
 
                 // testGetGlobalObjects
                 from("direct:testGetGlobalObjects")
                     .to("force://getGlobalObjects")
                     .to("mock:testGetGlobalObjects");
 
                 from("direct:testGetGlobalObjectsXml")
                     .to("force://getGlobalObjects?format=xml")
                     .to("mock:testGetGlobalObjectsXml");
 
                 // testGetSObjectBasicInfo
                 from("direct:testGetSObjectBasicInfo")
                     .to("force://getSObjectBasicInfo?sObjectName=Merchandise__c")
                     .to("mock:testGetSObjectBasicInfo");
 
                 from("direct:testGetSObjectBasicInfoXml")
                     .to("force://getSObjectBasicInfo?format=xml&sObjectName=Merchandise__c")
                     .to("mock:testGetSObjectBasicInfoXml");
 
                 // testGetSObjectDescription
                 from("direct:testGetSObjectDescription")
                     .to("force://getSObjectDescription?sObjectName=Merchandise__c")
                     .to("mock:testGetSObjectDescription");
 
                 from("direct:testGetSObjectDescriptionXml")
                     .to("force://getSObjectDescription?format=xml&sObjectName=Merchandise__c")
                     .to("mock:testGetSObjectDescriptionXml");
 
                 // testGetSObjectById
                 from("direct:testGetSObjectById")
                     .to("force://getSObjectById?sObjectName=Merchandise__c&sObjectClass=org.fusesource.camel.component.salesforce.Merchandise__c")
                     .to("mock:testGetSObjectById");
 
                 from("direct:testGetSObjectByIdXml")
                     .to("force://getSObjectById?format=xml&sObjectName=Merchandise__c&sObjectClass=org.fusesource.camel.component.salesforce.Merchandise__c")
                     .to("mock:testGetSObjectByIdXml");
             }
         };
     }
 
     private void setLoginProperties(SalesforceComponent component) throws IllegalAccessException, IOException {
         // load test-login properties
         Properties properties = new Properties();
         InputStream stream = getClass().getResourceAsStream(TEST_LOGIN_PROPERTIES);
         if (null == stream) {
             throw new IllegalAccessException("Create a properties file named " +
                 TEST_LOGIN_PROPERTIES + " with clientId, clientSecret, userName and password" +
                 " for a Salesforce account with the Merchandise object from Salesforce Guides.");
         }
         properties.load(stream);
         component.setClientId(properties.getProperty("clientId"));
         component.setClientSecret(properties.getProperty("clientSecret"));
         component.setUserName(properties.getProperty("userName"));
         component.setPassword(properties.getProperty("password"));
     }
 
 }
