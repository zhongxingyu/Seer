 package com.acme.integration.tests;
 
 import nz.ac.auckland.integration.testing.OrchestratedTestBuilder;
 import nz.ac.auckland.integration.testing.expectation.MockExpectation;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.processor.aggregate.AggregationStrategy;
 
 public class AcmeTest extends OrchestratedTestBuilder {
 
     //Each of the syncTest or asyncTest calls creates a new JUnit test
    @Override
    public void configure() {
         //Sends a request to pingService and validates the response
         syncTest("cxf:http://localhost:8090/services/pingService", "Simple WS PING test")
                 .requestBody(xml("<ns:pingRequest xmlns:ns=\"urn:com:acme:integration:wsdl:pingservice\">" +
                         "<request>PING</request>" +
                         "</ns:pingRequest>"))
                 .expectedResponseBody(xml("<ns:pingResponse xmlns:ns=\"urn:com:acme:integration:wsdl:pingservice\">" +
                         "<response>PONG</response>" +
                         "</ns:pingResponse>"));
 
         //Using WS-Security features of CXF (username/password) - note you need to specify the WSDL so
         //that CXF can grab the policy (it can be a remote reference if you wish)
         syncTest("cxf://http://localhost:8090/services/securePingService?wsdlURL=SecurePingService.wsdl&" +
                 "properties.ws-security.username=user" +
                 "&properties.ws-security.password=pass",
                 "Simple WS PING test with WS-Security")
                 .requestBody(xml("<ns:pingRequest xmlns:ns=\"urn:com:acme:integration:wsdl:pingservice\">" +
                         "<request>PING</request>" +
                         "</ns:pingRequest>"))
                 .expectedResponseBody(xml("<ns:pingResponse xmlns:ns=\"urn:com:acme:integration:wsdl:pingservice\">" +
                         "<response>PONG</response>" +
                         "</ns:pingResponse>"));
 
         //Using classpath resources instead
         syncTest("cxf:http://localhost:8090/services/pingService",
                 "Simple WS PING test with local resources")
                 .requestBody(xml(classpath("/data/pingRequest1.xml")))
                 .expectedResponseBody(xml(classpath("/data/pingResponse1.xml")));
 
         //Using a JSON service
         syncTest("http://localhost:8091/jsonPingService", "Simple JSON PING")
                 .requestBody(json("{\"request\":\"PING\"}"))
                 .expectedResponseBody(json("{\"response\":\"PONG\"}"));
 
         //Showing how expectations can create mock endpoints to validate the incoming request and provide a canned response
         syncTest("cxf:http://localhost:8090/services/pingServiceProxy",
                 "WS PING test with mock service expectation")
                 .requestBody(xml(classpath("/data/pingRequest1.xml")))
                 .expectedResponseBody(xml(classpath("/data/pingResponse1.xml")))
                 .addExpectation(syncExpectation("cxf:http://localhost:9090/services/targetWS?wsdlURL=PingService.wsdl")
                         .expectedBody(xml(classpath("/data/pingRequest1.xml")))
                         .responseBody(xml(classpath("/data/pingResponse1.xml"))));
 
         //Showing how we can string together expectations for multiple requests to the same (or different) endpoints
         syncTest("cxf:http://localhost:8090/services/pingServiceMultiProxy",
                 "WS PING test with multiple mock service expectations")
                 .requestBody(xml(classpath("/data/pingRequest1.xml")))
                 .expectedResponseBody(xml(classpath("/data/pingResponse1.xml")))
                 .addExpectation(syncExpectation("cxf:http://localhost:9090/services/targetWS?wsdlURL=PingService" +
                         ".wsdl")
                         .expectedBody(xml(classpath("/data/pingRequest1.xml")))
                         .responseBody(xml(classpath("/data/pingResponse1.xml"))))
                 .addExpectation(syncExpectation
                         ("cxf:http://localhost:9091/services/anotherTargetWS?wsdlURL=PingService.wsdl")
                         .expectedBody(xml(classpath("/data/pingRequest1.xml")))
                         .responseBody(xml(classpath("/data/pingResponse1.xml"))));
 
         //The same as above except showing support for weakly ordered expectations (i.e. multi-threaded call-outs)
         syncTest("cxf:http://localhost:8090/services/pingServiceMultiProxyUnordered",
                 "WS PING test with multiple unordered mock service expectations")
                 .requestBody(xml(classpath("/data/pingRequest1.xml")))
                 .expectedResponseBody(xml(classpath("/data/pingResponse1.xml")))
                 .addExpectation(syncExpectation("cxf:http://localhost:9090/services/targetWS?wsdlURL=PingService.wsdl")
                         .expectedBody(xml(classpath("/data/pingRequest1.xml")))
                         .responseBody(xml(classpath("/data/pingResponse1.xml")))
                         .ordering(MockExpectation.OrderingType.PARTIAL))
                 .addExpectation(syncExpectation("cxf:http://localhost:9091/services/anotherTargetWS?wsdlURL=PingService.wsdl")
                         .expectedBody(xml(classpath("/data/pingRequest1.xml")))
                         .responseBody(xml(classpath("/data/pingResponse1.xml")))
                         .ordering(MockExpectation.OrderingType.PARTIAL));
         /*
         Send an invalid message to the ESB which validates and rejects it,  meaning the target endpoint shouldn't
          receive it. The unreceivedExpectation is especially useful for message filtering where you want to
          ensure the message doesn't arrive at the endpoint.
          */
         syncTest("cxf:http://localhost:8090/services/pingServiceProxy",
                 "Test invalid message doesn't arrive at the endpoint and returns exception")
                 .requestBody(xml("<ns:pingRequest xmlns:ns=\"urn:com:acme:integration:wsdl:pingservice\">" +
                                                     "<request>PONG</request>" +
                                                  "</ns:pingRequest>"))
                 .expectsExceptionResponse()
                 .addExpectation(unreceivedExpectation("cxf:http://localhost:9090/services/targetWS?wsdlURL=PingService.wsdl"));
 
         //Send a message to a vm destination (like a JMS queue) to show asynchronous messaging with transformation
         asyncTest("vm:test.input", "Simple Asynchronous Canonicalizer Comparison")
                 .inputMessage(xml("<SystemField>foo</SystemField>"))
                 .addExpectation(asyncExpectation("vm:test.output")
                         .expectedBody(xml("<CanonicalField>foo</CanonicalField>")));

        //A test to show how we can set up an expectation to throw an HTTP exception and then
        //have it validated correctly
        syncTest("jetty:http://localhost:8090/testJSONAPI", "Simple JSON API proxy failure test with body")
                .requestBody(json("{\"hello\":\"home\"}"))
                .exceptionResponseValidator(httpExceptionResponse()
                        .responseBodyValidator(json("{\"error\":\"Should be hello:world\"}"))
                        .statusCode(501).build())
                .addExpectation(httpErrorExpectation("jetty:http://localhost:8090/targetJSONAPI")
                        .expectedBody(json("{\"hello\":\"home\"}"))
                        .responseBody(json("{\"error\":\"Should be hello:world\"}"))
                        .statusCode(501));
     }
 
     /**
      * If you're testing external routes (i.e. an application server service bus) then this wouldn't be here
      * - this just sets up a 'fake' Camel ESB.
      */
     @Override
     public RouteBuilder createRouteBuilder() {
         return new RouteBuilder() {
             @Override
             public void configure() throws Exception {
                 from("direct:pingServiceResponse")
                         .setBody(constant("<ns:pingResponse xmlns:ns=\"urn:com:acme:integration:wsdl:pingservice\">" +
                                 "<response>PONG</response>" +
                                 "</ns:pingResponse>"));
 
                 from("cxf:bean:pingService")
                     .to("direct:pingServiceResponse");
 
                 from("cxf:bean:securePingService")
                     .to("direct:pingServiceResponse");
 
                 from("cxf:bean:pingServiceProxy")
                     .to("direct:pingServiceProxy");
 
                 from("direct:pingServiceProxy")
                     //do some quick validation to show what happens on error
                     .process(new Processor() {
                         @Override
                         public void process(Exchange exchange) throws Exception {
                             //a poor man's attempt at validation
                             if (!exchange.getIn().getBody(String.class).contains("PING"))
                                 throw new Exception("INVALID BODY");
                         }
                     })
                     //send this through to the 'target' system
                     .to("cxf:http://localhost:9090/services/targetWS?dataFormat=PAYLOAD&wsdlURL=PingService.wsdl");
 
                 from("cxf:bean:pingServiceMultiProxy")
                         .multicast(new AggregationStrategy() {
                             @Override
                             public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
                                 return newExchange;
                             }
                         }).stopOnException()
                         .to("cxf:http://localhost:9090/services/targetWS?dataFormat=PAYLOAD&wsdlURL=PingService.wsdl",
                             "cxf:http://localhost:9091/services/anotherTargetWS?dataFormat=PAYLOAD&wsdlURL=PingService.wsdl");
 
                 //parallel processing will mean this can happen in any order
                 from("cxf:bean:pingServiceMultiProxyUnordered")
                     .multicast(new AggregationStrategy() {
                         @Override
                         public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
                             return newExchange;
                         }
                     })
                     .parallelProcessing()
                     .to("direct:targetWSSlowDown",
                     "cxf:http://localhost:9091/services/anotherTargetWS?dataFormat=PAYLOAD&wsdlURL=PingService.wsdl");
 
                 //ensure they arrive out of order by delaying the first one
                 from("direct:targetWSSlowDown")
                         .delay(5000)
                         .to("cxf:http://localhost:9090/services/targetWS?dataFormat=PAYLOAD&wsdlURL=PingService.wsdl");
 
                 //The JSON Service is the easiest :)
                 from("jetty:http://localhost:8091/jsonPingService")
                     .setBody(constant("{\"response\":\"PONG\"}"));
 
                 //Simple canonicalization service - the vm transport is like a JMS queue
                 from("vm:test.input")
                         .setBody(constant("<CanonicalField>foo</CanonicalField>"))
                         .to("vm:test.output");

                //a straight through proxy
                from("jetty:http://localhost:8090/testJSONAPI")
                        .to("jetty:http://localhost:8090/targetJSONAPI?bridgeEndpoint=true&throwExceptionOnFailure=false");

             }
         };
     }
 
     //setup some of the Spring details for the Camel 'fake' ESB
     @Override
     public String[] getSpringContextPaths() {
         return new String[] {"FakeESBContext.xml"};
     }
 
 }
