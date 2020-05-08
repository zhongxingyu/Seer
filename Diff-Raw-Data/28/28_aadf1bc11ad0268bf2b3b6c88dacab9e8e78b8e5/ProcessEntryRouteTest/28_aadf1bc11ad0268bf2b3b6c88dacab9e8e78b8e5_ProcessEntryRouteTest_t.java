 package es.tid.cloud.tdaf.accounting.filtering;
 
 import java.util.Date;
 
 import org.apache.camel.CamelContext;
 import org.apache.camel.EndpointInject;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.camel.Produce;
 import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.NoErrorHandlerBuilder;
 import org.apache.camel.component.bean.validator.BeanValidationException;
 import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
 import org.apache.camel.test.spring.CamelSpringTestSupport;
 import org.junit.Test;
 import org.springframework.context.support.AbstractApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import es.tid.cloud.tdaf.accounting.model.EventEntry;
 
 public class ProcessEntryRouteTest extends CamelSpringTestSupport {
 
     /** The producer template. */
     @Produce(uri = "direct:processEntry")
     protected ProducerTemplate template;
 
     /** The result.*/
     @EndpointInject(uri = "vm:persist-events")
     private MockEndpoint toPersistRoute;
 
     @Override
     protected AbstractApplicationContext createApplicationContext() {
         return new ClassPathXmlApplicationContext(
                 new String[] {
                         "classpath:META-INF/spring/filtering-context.xml",
                         "classpath:test-context.xml"
                 });
     }
 
     @Override
     protected CamelContext createCamelContext() throws Exception {
        final DefaultCamelContext context = (DefaultCamelContext)super.createCamelContext();
         context.addComponent("vm", context.getComponent("mock"));
        context.getRouteDefinition("logFileRoute").autoStartup(false);
        context.setErrorHandlerBuilder(new NoErrorHandlerBuilder());
         return context;
     }
 
     @Test
     public void whenThereAreAllRequiredFieldsShouldOccurNoErrors() throws Exception {
 
         //Given
         final EventEntry eventEntry = new EventEntry();
         eventEntry.setServiceId("probaServiceId");
         eventEntry.setId("probaId");
         eventEntry.setConcept("probaConcept");
         eventEntry.setEvent("probaEvent");
         eventEntry.setTime(new Date());
 
         toPersistRoute.expectedMessageCount(1);
 
         // When
         Exchange reply = template.send(new Processor() {
             @Override
             public void process(Exchange exchange) throws Exception {
                 exchange.getIn().setBody(eventEntry);
                 exchange.getIn().setHeader(EventEntryMatcherProcessor.MATCHER_HEADER, true);
             }
         });
 
         //Then
         toPersistRoute.assertIsSatisfied();
         assertFalse(reply.isFailed());
         assertTrue(reply.getIn().getBody(EventEntry.class) != null);
     }
 
     @Test
     public void whenThereAreNoTimeFieldShouldOccurValidationError() throws Exception {
 
         //Given
         final EventEntry eventEntry = new EventEntry();
         eventEntry.setServiceId("probaServiceId");
         eventEntry.setId("probaId");
         eventEntry.setConcept("probaConcept");
         eventEntry.setEvent("probaEvent");
 
         toPersistRoute.expectedMessageCount(0);
 
         // When
         Exchange reply = template.send(new Processor() {
             @Override
             public void process(Exchange exchange) throws Exception {
                 exchange.getIn().setBody(eventEntry);
                 exchange.getIn().setHeader(EventEntryMatcherProcessor.MATCHER_HEADER, true);
             }
         });
 
         //Then
         toPersistRoute.assertIsSatisfied();
         assertTrue(reply.isFailed());
         BeanValidationException e = reply.getException(BeanValidationException.class);
         assertNotNull(e);
         assertTrue(e.getConstraintViolations().size() == 1);
     }
 
     @Test
     public void whenThereAreNoOthersRequiredFieldsShouldOccurValidationError() throws Exception {
 
         //Given
         final EventEntry eventEntry = new EventEntry();
 
         toPersistRoute.expectedMessageCount(0);
 
         // When
         Exchange reply = template.send(new Processor() {
             @Override
             public void process(Exchange exchange) throws Exception {
                 exchange.getIn().setBody(eventEntry);
                 exchange.getIn().setHeader(EventEntryMatcherProcessor.MATCHER_HEADER, true);
             }
         });
 
         //Then
         toPersistRoute.assertIsSatisfied();
         assertTrue(reply.isFailed());
         BeanValidationException e = reply.getException(BeanValidationException.class);
         assertNotNull(e);
         assertTrue(e.getConstraintViolations().size() > 1);
     }
 }
