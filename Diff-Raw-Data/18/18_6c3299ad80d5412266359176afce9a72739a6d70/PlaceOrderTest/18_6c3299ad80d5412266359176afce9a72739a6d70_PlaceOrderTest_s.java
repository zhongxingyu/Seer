 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.vikrant.org;
 
 import java.io.File;
 
 import org.apache.activemq.util.IOHelper;
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.component.mock.MockEndpoint;
 import org.apache.camel.test.junit4.CamelSpringTestSupport;
 import org.junit.Test;
 import org.springframework.context.support.AbstractXmlApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class PlaceOrderTest extends CamelSpringTestSupport{
 	 @Test
 	 public void testPlacingOrders() throws Exception {
 	        // Try placing two file orders
 	        MockEndpoint result = getMockEndpoint("mock:orders");
 	        result.reset();
 	        result.expectedMessageCount(2);
 	        
 	        // clear out any old orders
 	        deleteDirectory("target/placeorder");
 	        IOHelper.mkdirs(new File("target/placeorder"));      
 	        
 	        // place 2 file based orders
 	        IOHelper.copyFile(new File("src/data/message3.xml"), new File("target/placeorder/message3.xml"));
 	        IOHelper.copyFile(new File("src/data/message4.csv"), new File("target/placeorder/message4.csv"));
 	        
 	        result.assertIsSatisfied();
 
 	        // Now try placing an order via HTTP       
 	        result.reset();
 	        result.expectedMessageCount(1);
	        result.expectedBodiesReceived(new Order("gearbox", 5));
 	        
 	        String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
	            + "<order name=\"gearbox\" amount=\"5\"/>";
 	        Object response = template.requestBody("http://localhost:8888/placeorder", body);
 	        // convert the response to a String
 	        String responseString = context.getTypeConverter().convertTo(String.class, response);
 	        assertEquals("OK", responseString);
 	        
 	        // ensure that the order got through to the mock endpoint
 	        result.assertIsSatisfied();
 	    }       
 
 	 	@Override
 	    protected AbstractXmlApplicationContext createApplicationContext() {
 	        return new ClassPathXmlApplicationContext("META-INF/spring/camel-context.xml");
 	    }
 	    @Override
 	    protected RouteBuilder createRouteBuilder() throws Exception {
 	        return new OrderRouterExtension();
 	    }
 	
 }
 
 
 
