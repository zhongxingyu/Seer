 package es.tid.cloud.tdaf.accounting.filtering;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.camel.EndpointInject;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.camel.Produce;
 import org.apache.camel.ProducerTemplate;
 import org.apache.camel.builder.AdviceWithRouteBuilder;
 import org.apache.camel.component.mock.MockEndpoint;
 import org.apache.camel.test.spring.CamelSpringTestSupport;
 import org.apache.commons.collections.map.MultiKeyMap;
 import org.junit.Test;
 import org.mockito.Mockito;
 import org.springframework.context.support.AbstractApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import pl.otros.logview.LogData;
 import es.tid.cloud.tdaf.accounting.filtering.csv.EventCSVParser;
 import es.tid.cloud.tdaf.accounting.model.EventBase.Mode;
 import es.tid.cloud.tdaf.accounting.model.EventEntry;
 import es.tid.cloud.tdaf.accounting.model.EventPattern;
 
 public class EventMatcherRouteTest extends CamelSpringTestSupport {
 
     /** The producer template. */
     @Produce(uri = "direct:eventMatcher")
     protected ProducerTemplate template;
 
     /** The mock endpoint.*/
     @EndpointInject(uri = "mock:processEntry")
     protected MockEndpoint mockProcessEntry;
 
     final LogData logData= new LogData();
 
     //Mock
     EventCSVParser eventCSVParser;
     List<EventPattern> eventPatterns;
 
     @Override
     protected AbstractApplicationContext createApplicationContext() {
         return new ClassPathXmlApplicationContext(
                 new String[] {
                         "classpath:META-INF/spring/filtering-context.xml",
                         "classpath:test-context.xml"
                 });
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public void setUp() throws Exception {
         super.setUp();
         context.getRouteDefinition("eventMatcherRoute").adviceWith(context, new AdviceWithRouteBuilder() {
             @Override
             public void configure() throws Exception {
                 interceptSendToEndpoint(
                         "direct:processEntry")
                         .to("mock:processEntry").skipSendToOriginalEndpoint();
             }
         });
 
         eventPatterns = new ArrayList<EventPattern>();
         EventPattern eventPattern = new EventPattern("proba", "proba", "proba", "proba", "(?<proba>[\\w]+)", Mode.OFFLINE);
         eventPatterns.add(eventPattern);
         MultiKeyMap multiKeyMap = Mockito.mock(MultiKeyMap.class);
         Mockito.when(multiKeyMap.values()).thenReturn(Arrays.asList(eventPatterns));
 
         eventCSVParser = getMandatoryBean(EventCSVParser.class, "eventCSVParser");
         Mockito.when(eventCSVParser.getEventPatterns()).thenReturn(multiKeyMap);
 
         logData.setDate(new Date());
     }
 
     @Test
     public void whenPatternMatches() throws Exception {
 
         //Given
         logData.setMessage("fkjlsflasdjflsjald");
 
         mockProcessEntry.expectedMessageCount(1);
 
         // When
         Exchange reply = template.send(new Processor() {
             @Override
             public void process(Exchange exchange) throws Exception {
                 exchange.getIn().setBody(logData);
             }
         });
 
         //Then
         mockProcessEntry.assertIsSatisfied();
         assertTrue(reply.getIn().getHeader(EventEntryMatcherProcessor.MATCHER_HEADER)!=null);
         assertTrue(reply.getIn().getBody(EventEntry.class) != null);
     }
 
     @Test
     public void whenPatternNoMatches() throws Exception {
 
         //Given
         logData.setMessage("");
 
         mockProcessEntry.expectedMessageCount(0);
 
         // When
         Exchange reply = template.send(new Processor() {
             @Override
             public void process(Exchange exchange) throws Exception {
                 exchange.getIn().setBody(logData);
             }
         });
 
         //Then
         mockProcessEntry.assertIsSatisfied();
         assertTrue(reply.getIn().getHeader(EventEntryMatcherProcessor.MATCHER_HEADER)==null);
         assertFalse(reply.isFailed());
     }
 
 }
