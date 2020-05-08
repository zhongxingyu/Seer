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
 
 import java.util.HashMap;
 import java.util.Map;
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.component.mock.MockEndpoint;
 import org.junit.Test;
 import static org.mockito.Mockito.doAnswer;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.rosuda.REngine.REXP;
 import org.rosuda.REngine.REXPString;
 
 /**
  *
  * @author cemmersb
  */
 public class RCodeProducerAssignExpressionTest extends RCodeProducerTest {
 
   @Test
   public void sendAssignExpressionTest() throws Exception {
     final Map<String, REXP> assignments = new HashMap<String, REXP>();
     assignments.put("x", new REXPString("2^2"));
    
     final MockEndpoint mockEndpoint = getMockEndpoint("mock:rcode");
    mockEndpoint.expectedBodiesReceived(null);
     
     for (Map.Entry<String, REXP> assignment : assignments.entrySet()) {
       doAnswer(new Answer<Void>() {
         @Override
         public Void answer(InvocationOnMock invocation) {
           return null;
         }
       }).when(rConnection).assign(assignment.getKey(), assignment.getValue());
     template.sendBody("direct:rcode", assignment);
     }
     
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
         // Send commands to the RCode endpoint, operation is 'assign_expression'
         from("direct:rcode")
             .to("rcode:localhost:6311/assign_expression?user=test&password=test123&bufferSize=10")
             .to("mock:rcode");
       }
     };
   }
 }
