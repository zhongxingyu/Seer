 /*
  * Copyright 2013 Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apacheextras.camel.component.rcode;
 
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.component.mock.MockEndpoint;
 import org.junit.Test;
 import static org.mockito.Mockito.when;
 import static org.mockito.Mockito.doAnswer;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 
 /**
  *
  * @author cemmersb
  */
 public class RCodeProducerVoidEvalTest extends RCodeProducerTest {
 
   // Path where the output file should be generated
   private final String path = RCodeProducerVoidEvalTest.class.getProtectionDomain()
       .getCodeSource().getLocation().getPath() + "/german_map.pdf";
   // RCode to generate a german map as pdf
   private final String command = "library(maps);\n"
       + "library(mapdata);\n"
       + "pdf(\"" + path + "\");\n"
       + "map('worldHires', 'germany');\n"
       + "dev.off()";
 
   @Test
   public void sendVoidEvalCmdMapTest() throws Exception {
     // Configure mock endpoint for assertions
    final Object obj = null;
     final MockEndpoint mockEndpoint = getMockEndpoint("mock:rcode");
    mockEndpoint.expectedBodiesReceived(obj); // void_eval returns a body with null value
 
     when(rConnection.isConnected()).thenReturn(Boolean.TRUE);
     doAnswer(new Answer<Void>() {
       @Override
       public Void answer(InvocationOnMock invocation) {
         return null;
       }
     }).when(rConnection).voidEval(command);
 
     // Send the command to the RCode component
     template.sendBody("direct:rcode", command);
     // Assert if the return value does not contain any body
     mockEndpoint.assertIsSatisfied();
   }
   
   @Test
   public void sendVoidEvalCmdHeaderTest() throws Exception {
     // Configure mock endpoint for assertions
     final MockEndpoint mockEndpoint = getMockEndpoint("mock:rcode");
    final Object obj = null;
    mockEndpoint.expectedBodiesReceived(obj); // void_eval returns a body with null value
 
     when(rConnection.isConnected()).thenReturn(Boolean.TRUE);
     doAnswer(new Answer<Void>() {
       @Override
       public Void answer(InvocationOnMock invocation) {
         return null;
       }
     }).when(rConnection).voidEval(command);
     
     template.sendBodyAndHeader("direct:rcode", command,
         RCodeConstants.RSERVE_OPERATION, RCodeOperation.VOID_EVAL);
     mockEndpoint.assertIsSatisfied();
   }
 
   @Override
   protected RouteBuilder createRouteBuilder() throws Exception {
     return new RouteBuilder() {
       @Override
       public void configure() throws Exception {
         // Handle exceptions by sending the exceptions to the mock endpoint
         onException(Exception.class)
             .handled(true)
             .to("mock:error");
         // Send commands to the RCode endpoint, operation is 'void_eval'
         from("direct:rcode")
             .to("rcode:localhost:6311/void_eval?user=test&password=test123")
             .to("mock:rcode");
       }
     };
   }
 }
