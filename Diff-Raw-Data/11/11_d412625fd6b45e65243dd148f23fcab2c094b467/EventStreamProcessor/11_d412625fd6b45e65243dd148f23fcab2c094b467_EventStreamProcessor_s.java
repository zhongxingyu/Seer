 package pegasus.esp;
 
 //import org.apache.commons.logging.Log;
 //import org.apache.commons.logging.LogFactory;
 import java.util.Collection;
 import java.util.Date;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pegasus.eventbus.client.Envelope;
 import pegasus.eventbus.client.EnvelopeHandler;
 import pegasus.eventbus.client.EventManager;
 import pegasus.eventbus.client.EventResult;
 import pegasus.eventbus.client.Subscription;
 import pegasus.eventbus.client.SubscriptionToken;
 
 import com.espertech.esper.client.Configuration;
 import com.espertech.esper.client.EPAdministrator;
 import com.espertech.esper.client.EPException;
 import com.espertech.esper.client.EPRuntime;
 import com.espertech.esper.client.EPServiceProvider;
 import com.espertech.esper.client.EPServiceProviderManager;
 import com.espertech.esper.client.EPStatement;
 import com.espertech.esper.client.EventBean;
 import com.espertech.esper.client.UpdateListener;
 import com.google.common.annotations.VisibleForTesting;
 
 public class EventStreamProcessor {
 
     // TODO: consider making this a constructor parameter to allow for multiple instances
     public static final String engineURI = "EventStreamProcessor";
     private static final Logger LOG = LoggerFactory.getLogger(EventStreamProcessor.class);
 
     private EventManager eventManager;
     
     private PublishingService publishingService;
 
     private EPServiceProvider epService;
 
     private SubscriptionToken token;
 
     private EventMonitorRepository repository;
 
     private final String espKey = this.getClass().getCanonicalName();
 
     class EnvelopeListener implements UpdateListener {
 
         public EnvelopeListener(EventMonitor monitor) {
             super();
             this.monitor = monitor;
         }
 
         EventMonitor monitor;
 
         @Override
         public void update(EventBean[] newEvents, EventBean[] oldEvents) {
             try {
                 for (EventBean eventBean : newEvents) {
                     LOG.debug(Utils.beanString(eventBean) + "==> " + monitor + ".receive(" + ")");
                     processResult(monitor.receive(eventBean));
                 }
                 // resultingEvent = monitor.handle(newEvents, oldEvents);
             } catch (Exception e) {
                 e.printStackTrace();
                 throw new RuntimeException(e);
             }
 
         }
 
         private void processResult(InferredEvent resultingEvent) {
             if (resultingEvent == null) {
                 return;
             }
 
             EPRuntime epRuntime = epService.getEPRuntime();
             if (resultingEvent instanceof InferredEventList) {
                 for (InferredEvent ev : ((InferredEventList) resultingEvent).getList()) {
                     LOG.debug(monitor + " SENDS* " + ev);
                     epRuntime.sendEvent(ev);
                 }
             } else {
                 LOG.debug(monitor + " SENDS " + resultingEvent);
                 epRuntime.sendEvent(resultingEvent);
             }
         }
     }
 
     class EventbusListener implements EnvelopeHandler {
 
 
 
         private void addHeader(Envelope env, String label, String val) {
             env.getHeaders().put(label, val);
         }
 
         private void addHeader(Envelope env, String label, Date now) {
             addHeader(env, label, now.getTime() + "");
         }
 
         private EventStreamProcessor eventStreamProcessor;
 
         public EventbusListener(EventStreamProcessor eventStreamProcessor) {
             this.eventStreamProcessor = eventStreamProcessor;
         }
 
         @Override
         public EventResult handleEnvelope(Envelope envelope) {
             addHeader(envelope, espKey + ":" + "TimeReceived", new Date());
             eventStreamProcessor.sendEvent(envelope);
             return EventResult.Handled;
         }
 
         @Override
         public String getEventSetName() {
             return "All";
         }
     }
 
 
 
     public EventStreamProcessor() {
         epService = createEventProcessor();
     }
 
     public EventStreamProcessor(EventMonitorRepository repository) {
         this();
         setRepository(repository);
     }
 
     public void setRepository(EventMonitorRepository repository) {
         this.repository = repository;
         repository.registerWith(this);
     }
 
     private EPServiceProvider createEventProcessor() {
 
         //@todo - workaround for classloader issue
         // save off context class loader
         ClassLoader contextClassloader = Thread.currentThread().getContextClassLoader();
 
         // set new classloader
         Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
 
         Configuration configuration = new Configuration();
         configuration.addEventType("Envelope", Envelope.class);
         configuration.addEventType("InferredEvent", InferredEvent.class);
         EPServiceProvider epService = EPServiceProviderManager.getProvider(engineURI, configuration);
         epService.initialize();
 
         // reset old classloader
         Thread.currentThread().setContextClassLoader(contextClassloader);
 
         return epService;
     }
 
     public void setEventManager(EventManager eventManager) {
         try {
             attachToEventBus(eventManager);
         } catch (RuntimeException e) {
             System.err.println("@@@@ Error attaching to EB:");
             e.printStackTrace();
             throw e;
         }
     }
 
     public void attachToEventBus(EventManager eventManager) {
         if (this.eventManager != null) {
             detachFromEventBus();
         }
         this.eventManager = eventManager;
         EnvelopeHandler envelopeHandler = new EventbusListener(this);
         Subscription subscription = new Subscription(envelopeHandler);
         // EventHandler<?> evtmp = null;
         // Subscription subscription = new Subscription(evtmp );
         token = eventManager.subscribe(subscription);
     }
 
     public void detachFromEventBus() {
         if (eventManager != null) {
             eventManager.unsubscribe(token);
             token = null;
             eventManager = null;
         }
     }
     
     public void setPublishingService(PublishingService publishingService) {
         try {
             attachToPublishingService(publishingService);
         } catch (RuntimeException e) {
             System.err.println("@@@@ Error attaching to PS:");
             e.printStackTrace();
             throw e;
         }
     }
     
     public void attachToPublishingService(PublishingService publishingService) {
         if (this.publishingService != null) {
             detachFromPublishingService();
         }
         this.publishingService = publishingService;
     }
     
     public void detachFromPublishingService() {
         if (publishingService != null) {
             //@todo - remove the publishers registered by esp
         }
     }
     
     public void watchFor(EventMonitor monitor) {
         Collection<Publisher> publishers = monitor.registerPatterns(this);
        publishingService.addPublishers(publishers);
     }
 
     @VisibleForTesting
     public void sendEvent(Envelope envelope) {
         LOG.debug(" --> Event: " + envelope);
         epService.getEPRuntime().sendEvent(envelope);
 
     }
 
     public void monitor(boolean isEPL, String pattern, EventMonitor monitor) {
         EPAdministrator administrator = epService.getEPAdministrator();
         EPStatement stmt;
         LOG.debug("Creating " + (isEPL ? "EPL" : "pattern") + ": " + pattern);
         try {
             if (isEPL) {
                 stmt = administrator.createEPL(pattern);
             } else {
                 stmt = administrator.createPattern(pattern);
             }
         } catch (EPException e) {
             System.err.println("Error creating " + (isEPL ? "EPL" : "Pattern") + " statement: " + pattern);
             e.printStackTrace();
             throw e;
         }
         if (monitor != null) {
             stmt.addListener(new EnvelopeListener(monitor));
         }
         if (monitor == null) {
             UpdateListener intermediatePrinter = createIntermediatePrinter(pattern);
             if (intermediatePrinter != null) {
                 stmt.addListener(intermediatePrinter);
             }
         }
     }
 
     private UpdateListener createIntermediatePrinter(final String pattern) {
         boolean watchInt = false;
         if (!watchInt) {
             return null;
         }
 
         String displ = pattern;
         String insert = "insert into ";
         if (pattern.startsWith(insert)) {
             displ = pattern.substring(insert.length()).split("\\W")[0];
         }
         final String fdispl = displ;
         UpdateListener ip = new UpdateListener() {
             @Override
             public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                 for (EventBean eventBean : newEvents) {
                     LOG.debug("!!!! " + fdispl + ": " + Utils.beanString(eventBean));
                 }
             }
         };
 
         return ip;
     }
 }
