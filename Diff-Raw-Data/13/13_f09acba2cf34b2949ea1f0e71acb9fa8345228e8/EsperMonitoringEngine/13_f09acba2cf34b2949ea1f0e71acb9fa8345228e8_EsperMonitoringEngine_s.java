 package eu.indenica.monitoring.esper;
 
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageListener;
 
 import org.osoa.sca.annotations.Destroy;
 import org.osoa.sca.annotations.Init;
 import org.osoa.sca.annotations.Property;
 import org.slf4j.Logger;
 
 import com.espertech.esper.client.EPServiceProvider;
 import com.espertech.esper.client.EPServiceProviderManager;
 import com.espertech.esper.client.EPStatement;
 import com.espertech.esper.client.EventBean;
 import com.espertech.esper.client.StatementAwareUpdateListener;
 
 import eu.indenica.common.LoggerFactory;
 import eu.indenica.common.PubSub;
 import eu.indenica.common.PubSubFactory;
 import eu.indenica.events.Event;
 import eu.indenica.messaging.ManagementClient;
 import eu.indenica.messaging.ManagementNameProvider;
 import eu.indenica.monitoring.MonitoringEngine;
 import eu.indenica.monitoring.MonitoringQuery;
 import eu.indenica.monitoring.MonitoringQueryImpl;
 
 /**
  * A monitoring engine implementation using the Esper CEP engine.
  * 
  * @author Christian Inzinger
  */
 // @EagerInit
 // @Scope("COMPOSITE")
 // @XmlSeeAlso({ MonitoringQueryImpl.class })
 public class EsperMonitoringEngine implements MonitoringEngine,
         StatementAwareUpdateListener {
     private final static Logger LOG = LoggerFactory.getLogger();
     private PubSub pubSub;
     private ManagementClient mgmtClient;
     private EPServiceProvider epService;
 
     private String nodeName;
     private MonitoringQuery[] queries;
 
     /**
      * @param queries
      *            the queries to set
      */
     @Property
     public void setQueries(MonitoringQueryImpl[] queries) {
         this.queries = queries;
     }
 
     /**
      * @param nodeName
      *            the nodeName to set
      */
     public void setHostName(String nodeName) {
         this.nodeName = nodeName;
     }
 
     /**
      * @see eu.indenica.common.RuntimeComponent#init()
      */
     @Init
     @Override
     public void init() throws Exception {
         LOG.debug("Starting {}", this.getClass().getSimpleName());
         if(nodeName == null) {
             nodeName = java.net.InetAddress.getLocalHost().getHostName();
             LOG.warn("Node name not set, using hostname ({}).", nodeName);
         }
         pubSub = PubSubFactory.getPubSub();
         mgmtClient = new ManagementClient(nodeName, SERVICE_NAME);
         epService = EPServiceProviderManager.getDefaultProvider();
         if(queries != null)
             for(MonitoringQuery q : queries)
                 addQuery(q);
 
         registerManagementListener();
         LOG.info("{} started", getClass().getSimpleName());
     }
 
     /**
      * @see eu.indenica.common.RuntimeComponent#destroy()
      */
     @Destroy
     @Override
     public void destroy() throws Exception {
         LOG.debug("Stopping Monitoring Engine...");
         mgmtClient.stop();
         pubSub.destroy();
         epService.removeAllServiceStateListeners();
         epService.removeAllStatementStateListeners();
         epService.destroy();
         LOG.info("Monitoring Engine stopped.");
     }
 
     /**
      * Registers a control interface listener with the messaging fabric.
      * 
      * <p>
      * This allows for the addition and removal of queries at runtime.
      * 
      * <p>
      * Control messages must be sent to the appropriate queue. The queue name is
      * designed as follows:
      * 
      * <pre>
      *   {@code <prefix>.<node-name>.<service-name>}
      * </pre>
      * 
      * <ul>
      * <li> {@code <prefix>} is the control infrastructure queue name prefix,
      * {@link ManagementNameProvider#MANAGEMENT_PREFIX}
      * <li> {@code <node-name>} is the node name assigned by the runtime
      * configuration, or the current machine's host name if no node name was set
      * <li> {@code <service-name>} is {@code monitoring}
      * </ul>
      * 
      * @throws JMSException
      *             if something goes wrong
      */
     private void registerManagementListener() throws JMSException {
         LOG.debug("Connecting management listener...");
         mgmtClient.registerListener(new MessageListener() {
             @Override
             public void onMessage(Message message) {
                 String command = null;
                 String queryName = null;
                 try {
                     LOG.trace("Received message {}", message);
                     command = message.getStringProperty("command");
                     queryName = message.getStringProperty("queryName");
                 } catch(JMSException e) {
                     LOG.error("Error processing message!", e);
                 }
 
                 if("startQuery".equals(command)) {
                     startQuery(queryName);
                 } else if("stopQuery".equals(command)) {
                     stopQuery(queryName);
                 } else {
                     LOG.error("Could not understand message {}", message);
                 }
             }
         });
     }
 
     /**
      * @see eu.indenica.common.EventListener#eventReceived(java.lang.String,
      *      eu.indenica.events.Event)
      */
     @Override
     public void eventReceived(String source, Event event) {
         LOG.debug("Event {} received from {}", event, source);
         epService.getEPRuntime().sendEvent(event);
     }
 
     /**
      * @see eu.indenica.monitoring.MonitoringEngine#addQuery(eu.indenica.monitoring.MonitoringQuery)
      */
     @Override
     public void addQuery(MonitoringQuery query) {
         LOG.info("Adding query {}", query);
         registerInputEventTypes(query);
         addStatement(query);
     }
 
     /**
      * @see eu.indenica.monitoring.MonitoringEngine#startQuery(java.lang.String)
      */
     @Override
     public void startQuery(String queryName) {
         EPStatement statement =
                 epService.getEPAdministrator().getStatement(queryName);
         if(statement == null)
             throw new IllegalArgumentException("No such query: " + queryName);
         LOG.debug("Starting query '{}'", queryName);
         statement.start();
     }
 
     /**
      * @see eu.indenica.monitoring.MonitoringEngine#stopQuery(java.lang.String)
      */
     @Override
     public void stopQuery(String queryName) throws IllegalArgumentException {
         EPStatement statement =
                 epService.getEPAdministrator().getStatement(queryName);
         if(statement == null)
             throw new IllegalArgumentException("No such query: " + queryName);
         LOG.debug("Stopping query '{}'", queryName);
         statement.stop();
     }
 
     /**
      * Add the given {@link MonitoringQuery} to the monitoring engine
      * 
      * @param query
      *            the query to be added
      */
     private void addStatement(MonitoringQuery query) {
        epService.getEPAdministrator()
                .createEPL(query.getStatement(), query.getName())
                .addListener(this);
     }
 
     /**
      * Register all input event types of the given {@link MonitoringQuery}.
      * 
      * @param query
      *            the query to register input event types for
      */
     private void registerInputEventTypes(MonitoringQuery query) {
         for(String eventType : query.getInputEventTypes()) {
             String source = null;
             if(eventType.contains(",")) {
                 String[] split = eventType.split(",", 2);
                 eventType = split[1].trim();
                 source = split[0].trim();
                 LOG.trace("Found source: {}", source);
             }
 
             try {
                 Class<?> eventTypeClass = Class.forName(eventType);
                 LOG.info("Loaded class {}", eventTypeClass);
                 epService.getEPAdministrator().getConfiguration()
                         .addEventType(eventTypeClass);
                 epService.getEPAdministrator().getConfiguration()
                         .addImport(eventTypeClass);
 
                 pubSub.registerListener(this, source,
                         ((Event) eventTypeClass.newInstance()).getEventType());
             } catch(Exception e) {
                 LOG.warn("Error registering event type {}!", eventType);
                 LOG.warn("Something went wrong!", e);
             }
         }
     }
 
     private Event previousEvent = null;
 
     @Override
     public void update(EventBean[] newEvents, EventBean[] oldEvents,
             EPStatement statement, EPServiceProvider epServiceProvider) {
         // TODO: Create proper event type from rule, result.
         /**
          * Rule: + list of attributes/how to re-create event transform rule!.
          * 
          * http://esper.codehaus.org/esper-4.6.0/doc/reference/en-US/html_single
          * /index.html#functionreference-transpose
          */
         LOG.trace("{} new events, {} old events",
                 newEvents != null ? newEvents.length : 0,
                 oldEvents != null ? oldEvents.length : 0);
         LOG.trace("newEvents: {}, oldEvents: {}", newEvents, oldEvents);
         if(newEvents == null) {
             LOG.info("No new events received.");
             return;
         }
 
         EventBean event = newEvents[0];
         // LOG.debug("event: {}", event.getUnderlying());
         // if(event.getUnderlying().equals(previousEvent))
         // return;
         previousEvent = (Event) event.getUnderlying();
         LOG.info("Publishing event {} (previous event: {})",
                 event.getUnderlying(), previousEvent);
         pubSub.publish(statement.getName(), (Event) event.getUnderlying());
     }
 
 }
