 /*
  * Copyright 2013 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.openehealth.ipf.gazelle.validation.camel;
 
 
import java.io.IOException;

 import ca.uhn.hl7v2.HL7Exception;
 import ca.uhn.hl7v2.model.Message;
 import ca.uhn.hl7v2.parser.Parser;
 import ca.uhn.hl7v2.parser.PipeParser;
 import org.apache.camel.CamelContext;
 import org.apache.camel.ProducerTemplate;
 import org.apache.camel.impl.DefaultCamelContext;
 import org.apache.camel.impl.DefaultProducerTemplate;
 import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
 import org.openehealth.ipf.commons.core.modules.api.ValidationException;
 
import static org.junit.Assert.fail;
 
 /**
  * @author Boris Stanojevic
  */
 public class GazelleProfileValidatorsTest {
 
     CamelContext camelContext = new DefaultCamelContext();
 
     ProducerTemplate template;
 
     Parser parser = new PipeParser();
 
     @Before
     public void onBeforeClass() throws Exception {
         camelContext.addRoutes(new TestRouteBuilder());
         camelContext.start();
         template = new DefaultProducerTemplate(camelContext);
         template.start();
     }
 
     @After
     public void onAfterClass() throws Exception {
         template.stop();
         camelContext.stop();
     }
 
     @Test
     public void testIti8() {
         try {
             template.sendBody("direct:iti8", getParsedMessage("hl7/iti-8.hl7"));
             fail();
         } catch (Exception e){
             assert e.getCause().getMessage().contains("Message validation failed");
             assert e.getCause().getClass().isAssignableFrom(ValidationException.class);
         }
     }
 
     @Test
     public void testIti10() throws Exception {
         template.sendBody("direct:iti10", getParsedMessage("hl7/iti-10.hl7"));
     }
 
     @Test
     public void testIti21() throws Exception {
         template.sendBody("direct:iti21", getParsedMessage("hl7/iti-21.hl7"));
     }
 
     @Test
     public void testIti21WrongIHETransaction() {
         try {
             template.sendBody("direct:iti8", getParsedMessage("hl7/iti-21.hl7"));
             fail();
         } catch (Exception e){
             assert e.getCause().getMessage().contains("Gazelle profile not found, check your MSH9 and MSH12 values.");
         }
     }
 
     protected String getMessageAsString(String resourcePath){
         String message = null;
         try {
             message = IOUtils.toString(this.getClass().getClassLoader().getResource(resourcePath));
         } catch (IOException ioe){
             ioe.printStackTrace();
         }
         assert message != null;
         return message.replaceAll("\n", "\r");
     }
 
     protected Message getParsedMessage(String path) throws HL7Exception {
         return parser.parse(getMessageAsString(path));
     }
 }
