 package uk.nhs.kch.rassyeyanie.framework.route;
 
 import static org.apache.camel.component.hl7.HL7.terser;
 
 import java.util.Map;
 
 import org.apache.camel.CamelContext;
 import org.apache.camel.Exchange;
 import org.apache.camel.ExchangePattern;
 import org.apache.camel.LoggingLevel;
 import org.apache.camel.Processor;
 import org.apache.camel.Route;
import org.apache.camel.builder.PredicateBuilder;
 import org.apache.camel.component.hl7.HL7Constants;
 import org.apache.camel.component.mina.MinaConstants;
 import org.apache.camel.model.ChoiceDefinition;
 import org.apache.camel.model.RouteDefinition;
 import org.apache.commons.lang.StringUtils;
 import org.apache.mina.common.IoSession;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.nhs.kch.rassyeyanie.framework.HL7AdditionalConstants;
 
 public class GenericListener
     extends AbstractListener
 {
     
     private static final Logger logger = LoggerFactory
         .getLogger(GenericListener.class);
     
     private String queue;
     private int workerTotal;
     
     public String getQueue()
     {
         return this.queue;
     }
     
     public void setQueue(String queue)
     {
         this.queue = queue;
     }
     
     @Override
     protected void createRoute(ListenerConfig listenerConfig)
     {
         String endpoint = listenerConfig.getEndpoint();
         boolean autoStart = listenerConfig.isAutoStart();
         final String routeName = listenerConfig.getName();
         
         if (StringUtils.isEmpty(routeName)) { throw new IllegalArgumentException(
             "routeName"); }
         
         if (StringUtils.isEmpty(endpoint)) { throw new IllegalArgumentException(
             "endpoint"); }
         
         logger.info("Attempting to create route {}", routeName);
         
         RouteDefinition route = this.from(endpoint);
         route.routeId(routeName);
         route.autoStartup(autoStart);
         route.process(new Processor() {
             
             @Override
             public void process(Exchange exchange)
                 throws Exception
             {
                 String connectionStatus = "Connection Status";
                 CamelContext camelContext = exchange.getContext();
                 Route camelRoute = camelContext.getRoute(routeName);
                 Map<String, Object> routeProperties =
                     camelRoute.getProperties();
                 
                 IoSession session =
                     (IoSession) exchange.getIn().getHeader(
                         MinaConstants.MINA_IOSESSION);
                 
                 routeProperties.put(connectionStatus, new IoSessionStatus(
                     session));
                 
             }
         });
         
         this
             .createExceptionBlock(route, listenerConfig.getExceptionProcessor());
         
         if (listenerConfig.getPreFormatter() != null)
         {
             route.unmarshal(listenerConfig.getPreFormatter());
         }
         
         if (listenerConfig.getIcmAckCleaner() != null)
         {
             route.process(listenerConfig.getIcmAckCleaner());
         }
         
         route.unmarshal().custom("hl7dataformat");
         route
             .choice()
            .when(PredicateBuilder.in(
            		this.header(HL7Constants.HL7_MESSAGE_TYPE).isEqualTo("ORM"),
            		this.header(HL7Constants.HL7_MESSAGE_TYPE).isEqualTo("ORU"),
            		this.header(HL7Constants.HL7_MESSAGE_TYPE).isEqualTo("ORG"),
            		this.header(HL7Constants.HL7_MESSAGE_TYPE).isEqualTo("OMG"),
                    PredicateBuilder.and(this.header(HL7Constants.HL7_MESSAGE_TYPE).isEqualTo("ADT"),
                    		this.header(HL7Constants.HL7_TRIGGER_EVENT).isNotEqualTo("ACK"))

            		)
            		)
             .setHeader(
                 HL7AdditionalConstants.HL7_EXTERNAL_PATIENT_ID,
                 terser("/.PID-2"))
             .setHeader(
                 HL7AdditionalConstants.HL7_INTERNAL_PATIENT_ID,
                 terser("/.PID-3"))
             .end();
         route.convertBodyTo(String.class);
         route.process(new HeaderOverrideProcessor(listenerConfig, this
             .getWorkerTotal()));
         
         ChoiceDefinition workerChoice = route.choice();
         for (int i = 0; i < this.getWorkerTotal(); i++)
         {
             workerChoice
                 .when(
                     this
                         .header(HL7AdditionalConstants.HL7_PATIENT_GROUP)
                         .isEqualTo(i + ""))
                 .setExchangePattern(ExchangePattern.InOnly)
                 .toF(this.queue, i)
                 .log(
                     String
                         .format(
                             "Processing MsgId: ${header.%s} / ${id} / %s / ${header.%s} / ${header.%s}",
                             HL7Constants.HL7_MESSAGE_CONTROL,
                             i,
                             HL7AdditionalConstants.HL7_INTERNAL_PATIENT_ID,
                             HL7AdditionalConstants.HL7_EXTERNAL_PATIENT_ID));
         }
         
         workerChoice
             .otherwise()
             .setExchangePattern(ExchangePattern.InOnly)
             .toF(this.queue, 0)
             .log(
                 String
                     .format(
                         "Processing MsgId: ${header.%s} / ${id} / %s / ${header.%s} / ${header.%s}",
                         HL7Constants.HL7_MESSAGE_CONTROL,
                         "default",
                         HL7AdditionalConstants.HL7_INTERNAL_PATIENT_ID,
                         HL7AdditionalConstants.HL7_EXTERNAL_PATIENT_ID));
         
         // log message
         route.log(String.format(
             "Processing MsgId: ${header.%s} / ${id}",
             HL7Constants.HL7_MESSAGE_CONTROL));
         route.log("body");
         route.unmarshal().custom("hl7dataformat");
         // acknowledge
         this.createAcknowledgementBlock(
             route,
             listenerConfig.getAcknowledgementProcessor());
         
         // log acknowledgement
         route.log("body");
     }
     
     private void createAcknowledgementBlock(RouteDefinition route,
                                             Processor acknowledgementProcessor)
     {
         route.process(acknowledgementProcessor);
     }
     
     private void createExceptionBlock(RouteDefinition route,
                                       Processor exceptionHandler)
     {
         route
             .onException(Exception.class)
             .log(LoggingLevel.ERROR, this.exceptionMessage().toString())
             .to(ERROR_LOG)
             .handled(true)
             .process(exceptionHandler)
             .end();
     }
     
     public int getWorkerTotal()
     {
         return this.workerTotal;
     }
     
     public void setWorkerTotal(int workerTotal)
     {
         this.workerTotal = workerTotal;
     }
     
     private class IoSessionStatus
     {
         private final IoSession session;
         
         public IoSessionStatus(IoSession session)
         {
             this.session = session;
         }
         
         @Override
         public String toString()
         {
             return this.session.isConnected() ? "Connected" : "Disconnected";
         }
         
     }
 }
