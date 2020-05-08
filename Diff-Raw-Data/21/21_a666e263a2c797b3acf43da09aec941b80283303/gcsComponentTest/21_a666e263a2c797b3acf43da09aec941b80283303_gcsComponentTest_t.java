 package org.talend;
 
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.component.mock.MockEndpoint;
 import org.apache.camel.test.junit4.CamelTestSupport;
 import org.junit.Test;
 
 public class gcsComponentTest extends CamelTestSupport {
 
     @Test
     public void testgcs() throws Exception {
         MockEndpoint mock = getMockEndpoint("mock:result");
         mock.expectedMinimumMessageCount(1);       
         
         assertMockEndpointsSatisfied();
     }
 
     @Override
     protected RouteBuilder createRouteBuilder() throws Exception {
         return new RouteBuilder() {
             public void configure() {
                 from("gcs://foo")
                  .setHeader("CamelFileParent", constant("/Users/bahaaldine/Talend/data/Roadshow/IN") )
                  .setHeader("CamelFileName", constant("personnes1164.xml") )
                  .to("gcs://bar?apiKey=GOOGLVRBWRFSXHXKU4MC&apiKeySecret=91it9olvPmsC3FvWLApNloq/FZVTChtLOmmzPDyz&bucket=talenddemo&url=commondatastorage.googleapis.com&contentType=text/xml")
                   .to("mock:result");
             }
         };
     }
 }
