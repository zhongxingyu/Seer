 package org.example.camel.four.two;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.naming.Context;
 
 import org.apache.camel.Body;
 import org.apache.camel.Header;
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.component.mock.MockEndpoint;
 import org.apache.camel.test.junit4.CamelTestSupport;
 import org.apache.camel.util.jndi.JndiContext;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class CamelTimerTest extends CamelTestSupport {
     static final transient Logger LOG = LoggerFactory.getLogger(CamelTimerTest.class);
 
     @Test
     public void testTimer() throws Exception {
        int expected = 10;
         MockEndpoint mock = getMockEndpoint("mock:result");
         mock.expectedMinimumMessageCount(expected);
 
         assertMockEndpointsSatisfied();
         assertTrue("Should have fired 2 or more times was: " + ExampleBean.COUNTER.get(), ExampleBean.COUNTER.get() >= expected);
 
     }
 
     @Override
     protected RouteBuilder createRouteBuilder() throws Exception {
         return new RouteBuilder() {
             @Override
             public void configure() throws Exception {
                 from("timer:foo")
                     .to("log:FOO?showBody=false&showBodyType=true&showHeaders=true")
                     .beanRef("myBean", "bodyAndHeader")
                     .to("mock:result");
             }
         };
     }
     
     @Override
     protected Context createJndiContext() throws Exception {
         JndiContext answer = new JndiContext();
         answer.bind("myBean", new ExampleBean());
         return answer;
     }
 
     public static class ExampleBean {
         public static AtomicInteger COUNTER = new AtomicInteger(0);
 
         public void bodyAndHeader(@Body String body, @Header(value = "foo") String arg) {
             LOG.info("Invoked someMethod()");
             COUNTER.incrementAndGet();
         }
 
         public void secondMethod() {
             LOG.info("Invoked secondMethod()");
             COUNTER.decrementAndGet();
         }
     }
 }
