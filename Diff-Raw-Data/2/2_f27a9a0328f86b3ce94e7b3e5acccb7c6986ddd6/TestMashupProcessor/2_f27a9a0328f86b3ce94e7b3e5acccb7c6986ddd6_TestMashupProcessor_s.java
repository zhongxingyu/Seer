 package org.apache.camel.processor.mashup.core;
 
 import org.apache.camel.Exchange;
 import org.apache.camel.Message;
 import org.apache.camel.impl.DefaultCamelContext;
 import org.apache.camel.impl.DefaultExchange;
 import org.junit.Ignore;
 import org.junit.Test;
 
 /**
  * Unit test on the mashup processor.
  */
 public class TestMashupProcessor {
     
     @Test
     public void process() throws Exception {
         Exchange exchange = new DefaultExchange(new DefaultCamelContext());
         Message in = exchange.getIn();
         in.setHeader("MASHUP_ID", "dummy");
         in.setHeader("MASHUP_STORE", "target/test-classes/model");
         in.setHeader("login", "user_test");
 
         MashupProcessor mashupProcessor = new MashupProcessor();
 
         mashupProcessor.process(exchange);
         
         Message out = exchange.getOut();
         System.out.println(out.getBody());
     }
 
     @Test
     public void real() throws Exception {
        System.out.println("REAL");
        
         Exchange exchange = new DefaultExchange(new DefaultCamelContext());
         Message in = exchange.getIn();
         in.setHeader("MASHUP_ID", "real");
         in.setHeader("MASHUP_STORE", "target/test-classes/model");
         in.setHeader("login", "user_test");
 
         MashupProcessor mashupProcessor = new MashupProcessor();
 
         mashupProcessor.process(exchange);
 
         Message out = exchange.getOut();
         System.out.println(out.getBody());
     }
     
 }
