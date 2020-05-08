 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.core.spring;
 
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import org.apache.camel.Exchange;
 import org.apache.camel.ProducerTemplate;
 import org.apache.camel.processor.aggregate.AggregationStrategy;
 import org.apache.camel.spring.SpringCamelContext;
 
 import org.junit.Test;
 import org.junit.Before;
 import org.junit.After;
 
 import static org.junit.Assert.assertThat;
 import static org.hamcrest.CoreMatchers.*;
 
 public class EventEndpointTest {
 
   private SpringCamelContext context;
 
   private ClassPathXmlApplicationContext beanFactory;
 
   @Before
   public void setUp() throws Exception {
     beanFactory = new ClassPathXmlApplicationContext(
        "com/globant/katari/core/spring/eventEndpointContext.xml");
     context = (SpringCamelContext) beanFactory.getBean("katari.eventBus");
   }
 
   @After
   public void tearDown() {
     beanFactory.close();
     beanFactory = null;
   }
 
   @Test
   public void testSend_withListeners() throws Exception {
     ProducerTemplate template = context.createProducerTemplate();
     String response = (String) template.requestBody("direct:e1", "message");
     assertThat(response, is("Response 1|Response 2|Response 3|"));
   }
 
   @Test
   public void testSend_withoutListeners() throws Exception {
     ProducerTemplate template = context.createProducerTemplate();
     String response = (String) template.requestBody("direct:e2", "message");
     assertThat(response, is("message"));
   }
 
   @Test
   public void testSend_withDefaultListener() throws Exception {
     ProducerTemplate template = context.createProducerTemplate();
     String response = (String) template.requestBody("direct:e3", "message");
     assertThat(response, is("message"));
   }
 
   // Some sample listeners
   public static class Listener1 {
     public String a(final String message) {
       return "Response 1";
     }
   }
   public static class Listener2 {
     public String a(final String userId) {
       return "Response 2";
     }
   }
   public static class Listener3 {
     public String a(final String userId) {
       return "Response 3";
     }
   }
   public static class NullListener {
     public Object a(Object o) {
       return o;
     }
   }
 
   // A sample aggregator, concatenates strings with a | terminator.
   public static class StringAggregator implements AggregationStrategy {
     public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
       String aggregatedMessage = "";
       if (oldExchange != null) {
         aggregatedMessage = oldExchange.getIn().getBody(String.class);
       }
       String message = newExchange.getIn().getBody(String.class);
       aggregatedMessage = aggregatedMessage + message + "|";
       newExchange.getIn().setBody(aggregatedMessage);
       return newExchange;
     }
   }
 }
 
