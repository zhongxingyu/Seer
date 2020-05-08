 /*
  * Copyright (C) 2010-2012, Zenoss Inc.  All Rights Reserved.
  */
 package org.zenoss.zep.impl;
 
 import com.google.common.base.Splitter;
 import org.python.core.Py;
 import org.python.core.PyDictionary;
 import org.python.core.PyException;
 import org.python.core.PyFunction;
 import org.python.core.PyInteger;
 import org.python.core.PyList;
 import org.python.core.PyObject;
 import org.python.core.PyString;
 import org.python.core.PySyntaxError;
 import org.python.util.PythonInterpreter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.TaskScheduler;
 import org.springframework.scheduling.Trigger;
 import org.springframework.scheduling.TriggerContext;
 import org.zenoss.amqp.AmqpConnectionManager;
 import org.zenoss.amqp.AmqpException;
 import org.zenoss.amqp.ExchangeConfiguration;
 import org.zenoss.amqp.ZenossQueueConfig;
 import org.zenoss.protobufs.model.Model.ModelElementType;
 import org.zenoss.protobufs.zep.Zep.Event;
 import org.zenoss.protobufs.zep.Zep.EventActor;
 import org.zenoss.protobufs.zep.Zep.EventDetail;
 import org.zenoss.protobufs.zep.Zep.EventStatus;
 import org.zenoss.protobufs.zep.Zep.EventSummary;
 import org.zenoss.protobufs.zep.Zep.EventTrigger;
 import org.zenoss.protobufs.zep.Zep.EventTriggerSubscription;
 import org.zenoss.protobufs.zep.Zep.Signal;
 import org.zenoss.zep.UUIDGenerator;
 import org.zenoss.zep.ZepException;
 import org.zenoss.zep.ZepUtils;
 import org.zenoss.zep.dao.EventSignalSpool;
 import org.zenoss.zep.dao.EventSignalSpoolDao;
 import org.zenoss.zep.dao.EventStoreDao;
 import org.zenoss.zep.dao.EventSummaryDao;
 import org.zenoss.zep.dao.EventTriggerDao;
 import org.zenoss.zep.dao.EventTriggerSubscriptionDao;
 import org.zenoss.zep.plugins.EventPostIndexContext;
 import org.zenoss.zep.plugins.EventPostIndexPlugin;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import static org.zenoss.zep.ZepConstants.*;
 
 /**
  * Post processing plug-in used to determine if NEW  or CLEARED events match a specified trigger. If the
  * trigger doesn't match, no signal is sent. If the trigger matches and doesn't specify a delay or repeat
  * interval, then it is sent immediately. If the trigger matches and specifies a delay, then a signal is
  * sent only if the event is still in NEW state after the delay. If the trigger specifies a repeat
  * interval, then after the initial signal is sent, the event is checked again after the repeat interval. If
  * the event is still in NEW state after the repeat interval, then another signal is sent and the check is
  * repeated again at the repeat interval.
  *
  * If an event which previously triggered a signal is cleared by another event, a final signal is sent
  * with the clear attribute set to true.
  */
 public class TriggerPlugin extends EventPostIndexPlugin {
 
     private static final Logger logger = LoggerFactory.getLogger(TriggerPlugin.class);
 
     private EventTriggerDao triggerDao;
     private EventSignalSpoolDao signalSpoolDao;
     private EventStoreDao eventStoreDao;
     private EventSummaryDao eventSummaryDao;
     private EventTriggerSubscriptionDao eventTriggerSubscriptionDao;
     private UUIDGenerator uuidGenerator;
 
     private AmqpConnectionManager connectionManager;
 
     protected static final int MAX_RULE_CACHE_SIZE = 200;
     protected static final Map<String, PyFunction> ruleFunctionCache = Collections
             .synchronizedMap(ZepUtils
                     .<String, PyFunction> createBoundedMap(MAX_RULE_CACHE_SIZE));
 
     // The maximum amount of time to wait between processing the signal spool.
     private static final long MAXIMUM_DELAY_MS = TimeUnit.SECONDS.toMillis(60);
 
     private TaskScheduler scheduler;
     private ScheduledFuture<?> spoolFuture;
     PythonHelper pythonHelper = new PythonHelper();
 
     /**
      * Helper class to enable lazy-initialization of Jython. Initializing the runtime and compiling code is expensive
      * to perform on each startup - better to do it when we first need it for evaluating triggers.
      */
     static final class PythonHelper {
         private volatile boolean initialized = false;
 
         private PythonInterpreter python;
         private PyFunction toObject;
 
         private synchronized void initialize() {
             if (initialized) {
                 return;
             }
             logger.info("Initializing Jython");
             this.initialized = true;
             PythonInterpreter.initialize(System.getProperties(), new Properties(), new String[0]);
 
             this.python = new PythonInterpreter();
 
             // define basic infrastructure class for evaluating rules
             this.python.exec(
                 "class DictAsObj(object):\n" +
                 "  def __init__(self, **kwargs):\n" +
                 "    for k,v in kwargs.iteritems(): setattr(self,k,v)");
 
             // expose to Java a Python dict->DictAsObj conversion function
             this.toObject = (PyFunction)this.python.eval("lambda dd : DictAsObj(**dd)");
 
             // import some helpful modules from the standard lib
             this.python.exec("import string, re, datetime");
             logger.info("Completed Jython initialization");
         }
 
         public PythonInterpreter getPythonInterpreter() {
             if (!initialized) {
                 initialize();
             }
             return this.python;
         }
 
         public PyFunction getToObject() {
             if (!initialized) {
                 initialize();
             }
             return this.toObject;
         }
 
         public void cleanup() {
             if (initialized) {
                 this.python.cleanup();
             }
         }
     }
 
 
     public TriggerPlugin() {
     }
 
     @Autowired
     public void setTaskScheduler(TaskScheduler scheduler) {
         this.scheduler = scheduler;
     }
 
     @Autowired
     public void setUuidGenerator(UUIDGenerator uuidGenerator) {
         this.uuidGenerator = uuidGenerator;
     }
 
     @Override
     public void start(Map<String, String> properties) {
         super.start(properties);
         scheduleSpool();
     }
 
     @Override
     public void stop() {
         this.pythonHelper.cleanup();
         if (spoolFuture != null) {
             spoolFuture.cancel(true);
         }
     }
 
     private void scheduleSpool() {
         if (spoolFuture != null) {
             spoolFuture.cancel(false);
         }
         Trigger trigger = new Trigger() {
             @Override
             public Date nextExecutionTime(TriggerContext triggerContext) {
                 Date nextExecution = null;
                 try {
                     long nextFlushTime = signalSpoolDao.getNextFlushTime();
                     if (nextFlushTime > 0) {
                         nextExecution = new Date(nextFlushTime);
                         logger.debug("Next flush time: {}", nextExecution);
                     }
                 } catch (Exception e) {
                     logger.warn("Exception getting next flush time", e);
                 }
                 if (nextExecution == null) {
                     nextExecution = new Date(System.currentTimeMillis() + MAXIMUM_DELAY_MS);
                 }
                 return nextExecution;
             }
         };
         Runnable runnable = new ThreadRenamingRunnable(new Runnable() {
             @Override
             public void run() {
                 processSpool(System.currentTimeMillis());
             }
         }, "ZEP_TRIGGER_PLUGIN_SPOOL");
         spoolFuture = scheduler.schedule(runnable, trigger);
     }
 
     public void setTriggerDao(EventTriggerDao triggerDao) {
         this.triggerDao = triggerDao;
     }
 
     public void setSignalSpoolDao(EventSignalSpoolDao spoolDao) {
         this.signalSpoolDao = spoolDao;
     }
 
     public void setEventSummaryDao(EventSummaryDao eventSummaryDao) {
         this.eventSummaryDao = eventSummaryDao;
     }
 
     public void setEventTriggerSubscriptionDao(EventTriggerSubscriptionDao eventTriggerSubscriptionDao) {
         this.eventTriggerSubscriptionDao = eventTriggerSubscriptionDao;
     }
 
     public void setConnectionManager(AmqpConnectionManager connmgr) {
         this.connectionManager = connmgr;
     }
 
     public void setEventStoreDao(EventStoreDao eventStoreDao) {
         this.eventStoreDao = eventStoreDao;
     }
 
     /**
      * Local context class used to store the Python objects created from the event which are passed in to the
      * trigger's rule for evaluation.
      */
     static class RuleContext {
         PyObject event;
         PyObject device;
         PyObject element;
         PyObject subElement;
 
         private RuleContext() {
         }
 
         private static void putIdAndUuidInDict(PyDictionary dict, String id, String uuid) {
             if (id != null) {
                 dict.put("name", id);
             }
             if (uuid != null) {
                 dict.put("uuid", uuid);
             }
         }
 
         private static final Splitter ORGANIZER_SPLITTER = Splitter.on('/').omitEmptyStrings();
 
         /**
          * Given a list of organizers, returns a list containing those same organizers plus
          * any parent organizers. For example, ['/First/Second/Third','/OtherFirst/OtherSecond']
          * will return ['/First', '/First/Second', '/First/Second/Third', '/OtherFirst',
          * '/OtherFirst/OtherSecond'].
          *
          * @param baseOrganizers List of most-specific organizer names.
          * @return A list containing all of the organizers plus their parent organizers.
          */
         private static List<String> includeParentOrganizers(List<String> baseOrganizers) {
             Set<String> allOrganizers = new TreeSet<String>();
             for (String organizer : baseOrganizers) {
                 final StringBuilder sb = new StringBuilder(organizer.length());
                 for (String subOrganizer : ORGANIZER_SPLITTER.split(organizer)) {
                     sb.append('/').append(subOrganizer);
                     allOrganizers.add(sb.toString());
                 }
             }
             return new ArrayList<String>(allOrganizers);
         }
 
         /**
          * Creates a rule context from the toObject function and event summary.
          *
          * @param toObject The toObject function which converts a dictionary to the appropriate object.
          * @param evtsummary The event to convert to a context.
          * @return A rule context for the event.
          */
         public static RuleContext createContext(PyFunction toObject, EventSummary evtsummary) {
             // set up interpreter environment to evaluate the rule source
             PyDictionary eventdict = new PyDictionary();
             PyDictionary devdict = new PyDictionary();
             PyDictionary elemdict = new PyDictionary();
             PyDictionary subelemdict = new PyDictionary();
 
             // Match old behavior (pre-4.x)
             int prodState = 0;
             int devicePriority = DEVICE_PRIORITY_NORMAL;
             String ipAddress = "";
             List<String> systemsAndParents = Collections.emptyList();
             List<String> groupsAndParents = Collections.emptyList();
             String location = "";
             String deviceClass = "";
 
             // extract event data from most recent occurrence
             Event event = evtsummary.getOccurrence(0);
 
             // copy event data to eventdict
             eventdict.put("summary", new PyString(event.getSummary()));
             eventdict.put("message", new PyString(event.getMessage()));
             eventdict.put("event_class", new PyString(event.getEventClass()));
             eventdict.put("fingerprint", new PyString(event.getFingerprint()));
             eventdict.put("event_key", new PyString(event.getEventKey()));
             eventdict.put("agent", new PyString(event.getAgent()));
             eventdict.put("monitor", new PyString(event.getMonitor()));
             eventdict.put("severity", event.getSeverity().getNumber());
             eventdict.put("event_class_key", new PyString(event.getEventClassKey()));
             if (event.hasSyslogPriority()) {
                 eventdict.put("syslog_priority", new PyInteger(event.getSyslogPriority().getNumber()));
             }
             if (event.hasSyslogFacility()) {
                 eventdict.put("syslog_facility", event.getSyslogFacility());
             }
             if (event.hasNtEventCode()) {
                 eventdict.put("nt_event_code", event.getNtEventCode());
             }
            // Initialize to empty attributes on elem and subelem in case a rule references
            // it and they do not exist
            putIdAndUuidInDict(subelemdict, "", "");
            putIdAndUuidInDict(elemdict, "", "");
 
             EventActor actor = event.getActor();
 
             if (actor.hasElementTypeId()) {
                 if (actor.getElementTypeId() == ModelElementType.DEVICE) {
                     devdict = elemdict;
                 }
 
                 elemdict.put("type", actor.getElementTypeId().name());
                 final String id = (actor.hasElementIdentifier()) ? actor.getElementIdentifier() : null;
                 final String uuid = actor.hasElementUuid() ? actor.getElementUuid() : null;
 
                 putIdAndUuidInDict(elemdict, id, uuid);
             }
 
             if (actor.hasElementSubTypeId()) {
                 if (actor.getElementSubTypeId() == ModelElementType.DEVICE) {
                     devdict = subelemdict;
                 }
 
                 subelemdict.put("type", actor.getElementSubTypeId().name());
                 final String id = (actor.hasElementSubIdentifier()) ? actor.getElementSubIdentifier() : null;
                 final String uuid = actor.hasElementSubUuid() ? actor.getElementSubUuid() : null;
 
                 putIdAndUuidInDict(subelemdict, id, uuid);
             }
 
             for (EventDetail detail : event.getDetailsList()) {
                 final String detailName = detail.getName();
                 // This should never happen
                 if (detail.getValueCount() == 0) {
                     continue;
                 }
                 final String singleDetailValue = detail.getValue(0);
                 
                 if (DETAIL_DEVICE_PRODUCTION_STATE.equals(detailName)) {
                     try {
                         prodState = Integer.parseInt(singleDetailValue);
                     } catch (NumberFormatException e) {
                         logger.warn("Failed retrieving production state", e);
                     }
                 }
                 else if (DETAIL_DEVICE_PRIORITY.equals(detailName)) {
                     try {
                         devicePriority = Integer.parseInt(singleDetailValue);
                     } catch (NumberFormatException e) {
                         logger.warn("Failed retrieving device priority", e);
                     }
                 }
                 else if (DETAIL_DEVICE_CLASS.equals(detailName)) {
                     // expect that this is a single-value detail.
                     deviceClass = singleDetailValue;
                 }
                 else if (DETAIL_DEVICE_SYSTEMS.equals(detailName)) {
                     // expect that this is a multi-value detail.
                     systemsAndParents = includeParentOrganizers(detail.getValueList());
                 }
                 else if (DETAIL_DEVICE_GROUPS.equals(detailName)) {
                     // expect that this is a multi-value detail.
                     groupsAndParents = includeParentOrganizers(detail.getValueList());
                 }
                 else if (DETAIL_DEVICE_IP_ADDRESS.equals(detailName)) {
                     // expect that this is a single-value detail.
                     ipAddress = singleDetailValue;
                 }
                 else if (DETAIL_DEVICE_LOCATION.equals(detailName)) {
                     // expect that this is a single-value detail.
                     location = singleDetailValue;
                 }
             }
 
             devdict.put("device_class", new PyString(deviceClass));
             devdict.put("production_state", new PyInteger(prodState));
             devdict.put("priority", new PyInteger(devicePriority));
             devdict.put("groups", new PyList(groupsAndParents));
             devdict.put("systems", new PyList(systemsAndParents));
             devdict.put("ip_address", new PyString(ipAddress));
             devdict.put("location", new PyString(location));
 
             // add more data from the EventSummary itself
             eventdict.put("status", evtsummary.getStatus().getNumber());
             eventdict.put("count", new PyInteger(evtsummary.getCount()));
             eventdict.put("current_user_name", new PyString(evtsummary.getCurrentUserName()));
 
             RuleContext ctx = new RuleContext();
             // create vars to pass to rule expression function
             ctx.event = toObject.__call__(eventdict);
             ctx.device = toObject.__call__(devdict);
             ctx.element = toObject.__call__(elemdict);
             ctx.subElement = toObject.__call__(subelemdict);
             return ctx;
         }
     }
 
     protected boolean eventSatisfiesRule(RuleContext ruleContext, String ruleSource) {
         PyObject result;
         try {
             // use rule to build and evaluate a Python lambda expression
             PyFunction fn = ruleFunctionCache.get(ruleSource);
             if (fn == null) {
                 fn = (PyFunction)this.pythonHelper.getPythonInterpreter().eval(
                         "lambda evt, dev, elem, sub_elem : " + ruleSource
                 );
                 ruleFunctionCache.put(ruleSource, fn);
             }
 
             // evaluate the rule function
             result = fn.__call__(ruleContext.event, ruleContext.device, ruleContext.element, ruleContext.subElement);
         } catch (PySyntaxError pysynerr) {
             // evaluating rule raised an exception - treat as "False" eval
             String fmt = Py.formatException(pysynerr.type, pysynerr.value);
             logger.warn("syntax error exception raised while compiling rule: {}, {}", ruleSource, fmt);
             result = new PyInteger(0);
         } catch (PyException pyexc) {
             // evaluating rule raised an exception - treat as "False" eval
             // If it's an AttributeError it just means the event doesn't have a value for the field
             // and an eval of False is fine. Otherwise we should log in case there's a real issue.
             if (!pyexc.match(Py.AttributeError)) {
                 String fmt = Py.formatException(pyexc.type, pyexc.value);
                 logger.warn("exception raised while evaluating rule: {}, {}", ruleSource, fmt);
             }
             else if (logger.isDebugEnabled()) {
                 String fmt = Py.formatException(pyexc.type, pyexc.value);
                 logger.debug("AttributeError raised while evaluating rule: {}, {}", ruleSource, fmt);
             }
             result = new PyInteger(0);
         }
 
         // return result as a boolean, using Python __nonzero__
         // object-as-bool evaluator
         return result.__nonzero__();
     }
 
     @Override
     public void processEvent(EventSummary eventSummary, EventPostIndexContext context) throws ZepException {
         final EventStatus evtstatus = eventSummary.getStatus();
 
         if (OPEN_STATUSES.contains(evtstatus)) {
             processOpenEvent(eventSummary);
         }
         else {
             if (evtstatus == EventStatus.STATUS_CLEARED) {
                 List<EventSignalSpool> spools = this.signalSpoolDao.findAllByEventSummaryUuid(eventSummary.getUuid());
                 for (EventSignalSpool spool : spools) {
                     // Send clear signal
                     if (spool.isSentSignal()) {
                         logger.debug("sending clear signal for event: {}", eventSummary.getUuid());
                         final EventTriggerSubscription subscription = this.eventTriggerSubscriptionDao.findByUuid(spool
                                 .getSubscriptionUuid());
                         publishSignal(eventSummary, subscription);
                     } else {
                         logger.debug("Skipping sending of clear signal for event {} and subscription {} - !sentSignal",
                                 eventSummary.getUuid(), spool.getSubscriptionUuid());
                     }
                 }
             }
             this.signalSpoolDao.deleteByEventSummaryUuid(eventSummary.getUuid());
         }
     }
 
     /**
      * Returns all EventSignalSpool objects keyed by the subscription UUID and event summary UUID for the specified
      * event summary.
      *
      * @param eventSummary The event summary.
      * @return A map of EventSignalSpool objects (keyed by the subscription UUID and event summary UUID) for the
      * specified event summary.
      * @throws ZepException If a failure occurs querying the database.
      */
     private Map<String,EventSignalSpool> getSpoolsForEventBySubcription(EventSummary eventSummary) throws ZepException {
         final List<EventSignalSpool> spools = this.signalSpoolDao.findAllByEventSummaryUuid(eventSummary.getUuid());
         if (spools.isEmpty()) {
             return Collections.emptyMap();
         }
         final Map<String,EventSignalSpool> spoolsBySubscription = new HashMap<String, EventSignalSpool>(spools.size());
         for (EventSignalSpool spool : spools) {
             spoolsBySubscription.put(spool.getSubscriptionUuid() + '|' + spool.getEventSummaryUuid(), spool);
         }
         return spoolsBySubscription;
     }
 
     private void processOpenEvent(EventSummary eventSummary) throws ZepException {
         final long now = System.currentTimeMillis();
         List<EventTrigger> triggers = this.triggerDao.findAllEnabled();
 
         // iterate over all enabled triggers to see if any rules will match
         // for this event summary
         boolean rescheduleSpool = false;
 
         RuleContext ruleContext = null;
         Map<String,EventSignalSpool> spoolsBySubscription = null;
 
         if (!triggers.isEmpty()) {
             logger.debug("Event: {}", eventSummary);
         }
 
         for (EventTrigger trigger : triggers) {
 
             // verify trigger has a defined rule
             if (!(trigger.hasRule() && trigger.getRule().hasSource())) {
                 continue;
             }
             // confirm trigger has any subscriptions registered with it
             List<EventTriggerSubscription> subscriptions = trigger.getSubscriptionsList();
             if (subscriptions.isEmpty()) {
                 continue;
             }
 
             final String ruleSource = trigger.getRule().getSource();
 
             // Determine if event matches trigger rule
             if (ruleContext == null) {
                 ruleContext = RuleContext.createContext(this.pythonHelper.getToObject(), eventSummary);
             }
             final boolean eventSatisfiesRule = eventSatisfiesRule(ruleContext, ruleSource);
 
             if (eventSatisfiesRule) {
                 logger.debug("Trigger {} ({}) MATCHES", trigger.getName(), ruleSource);
             }
             else {
                 logger.debug("Trigger {} ({}) DOES NOT MATCH", trigger.getName(), ruleSource);
             }
 
             // handle interval evaluation/buffering
             for (EventTriggerSubscription subscription : subscriptions) {
                 final int delaySeconds = subscription.getDelaySeconds();
                 final int repeatSeconds = subscription.getRepeatSeconds();
 
                 // see if any signalling spool already exists for this trigger-eventSummary
                 // combination
                 if (spoolsBySubscription == null) {
                     spoolsBySubscription = getSpoolsForEventBySubcription(eventSummary);
                 }
                 EventSignalSpool currentSpool = spoolsBySubscription.get(subscription.getUuid() + '|' +
                         eventSummary.getUuid());
                 boolean spoolExists = (currentSpool != null);
                 boolean spoolModified = false;
 
                 if (eventSatisfiesRule) {
                     logger.debug("subscriber: {}, delay: {}, repeat: {}, existing spool: {}",
                             new Object[] { subscription.getSubscriberUuid(), delaySeconds, repeatSeconds, spoolExists });
                 }
 
                 boolean onlySendInitial = subscription.getSendInitialOccurrence();
 
                 // If the rule wasn't satisfied
                 if (!eventSatisfiesRule) {
                     // If the rule previously matched and now no longer matches, ensure that the
                     // repeated signaling will not occur again.
                     if (spoolExists && currentSpool.getFlushTime() < Long.MAX_VALUE) {
                         logger.debug("Event previously matched trigger - disabling repeats");
                         currentSpool.setFlushTime(Long.MAX_VALUE);
                         spoolModified = true;
                         rescheduleSpool = true;
                     }
                 }
                 // Send signal immediately if no delay
                 else if (delaySeconds <= 0) {
                     if (!onlySendInitial) {
                         logger.debug("delay <= 0 and !onlySendInitial, send signal");
                         this.publishSignal(eventSummary, subscription);
                         
                         if (!spoolExists) {
                             currentSpool = EventSignalSpool.buildSpool(subscription, eventSummary, this.uuidGenerator);
                             currentSpool.setSentSignal(true);
                             this.signalSpoolDao.create(currentSpool);
                             rescheduleSpool = true;
                         }
                         else if (!currentSpool.isSentSignal()) {
                             currentSpool.setSentSignal(true);
                             spoolModified = true;
                         }
                     }
                     else {
                         if (!spoolExists) {
                             logger.debug("delay <=0 and spool doesn't exist, send signal");
                             this.publishSignal(eventSummary, subscription);
                             
                             currentSpool = EventSignalSpool.buildSpool(subscription, eventSummary, this.uuidGenerator);
                             currentSpool.setSentSignal(true);
                             this.signalSpoolDao.create(currentSpool);
                             rescheduleSpool = true;
                         }
                         else {
                             if (repeatSeconds > 0 &&
                                 currentSpool.getFlushTime() > now + TimeUnit.SECONDS.toMillis(repeatSeconds)) {
                                 logger.debug("adjust spool flush time to reflect new repeat seconds");
                                 currentSpool.setFlushTime(now + TimeUnit.SECONDS.toMillis(repeatSeconds));
                                 spoolModified = true;
                                 rescheduleSpool = true;
                             }
                         }
                     }
                 }
                 else {
                     // delaySeconds > 0
                     if (!spoolExists) {
                         currentSpool = EventSignalSpool.buildSpool(subscription, eventSummary, this.uuidGenerator);
                         this.signalSpoolDao.create(currentSpool);
                         rescheduleSpool = true;
                     }
                     else {
                         if (repeatSeconds == 0) {
                             if (!onlySendInitial && currentSpool.getFlushTime() == Long.MAX_VALUE) {
                                 currentSpool.setFlushTime(now + TimeUnit.SECONDS.toMillis(delaySeconds));
                                 spoolModified = true;
                                 rescheduleSpool = true;
                             }
                         }
                         else {
                             if (currentSpool.getFlushTime() > now + TimeUnit.SECONDS.toMillis(repeatSeconds)) {
                                 currentSpool.setFlushTime(now + TimeUnit.SECONDS.toMillis(repeatSeconds));
                                 spoolModified = true;
                                 rescheduleSpool = true;
                             }
                         }
                     }
                 }
                 
                 if (spoolModified) {
                     this.signalSpoolDao.update(currentSpool);
                 }
             }
         }
         if (rescheduleSpool) {
             scheduleSpool();
         }
     }
 
     protected void publishSignal(EventSummary summary, EventTriggerSubscription subscription) throws ZepException {
         try {
             final Event occurrence = summary.getOccurrence(0);
             Signal.Builder signalBuilder = Signal.newBuilder();
             signalBuilder.setUuid(this.uuidGenerator.generate().toString());
             signalBuilder.setCreatedTime(System.currentTimeMillis());
             signalBuilder.setEvent(summary);
             signalBuilder.setSubscriberUuid(subscription.getSubscriberUuid());
             signalBuilder.setTriggerUuid(subscription.getTriggerUuid());
 
             final boolean cleared = (summary.getStatus() == EventStatus.STATUS_CLEARED);
             signalBuilder.setClear(cleared);
             if (cleared) {
                 // Look up event which cleared this one
                 EventSummary clearEventSummary = this.eventStoreDao.findByUuid(summary.getClearedByEventUuid());
                 if (clearEventSummary != null) {
                     signalBuilder.setClearEvent(clearEventSummary);
                 }
                 else {
                     logger.warn("Unable to look up clear event with UUID: {}", summary.getClearedByEventUuid());
                 }
             }
             signalBuilder.setMessage(occurrence.getMessage());
             final Signal signal = signalBuilder.build();
 
             ExchangeConfiguration destExchange = ZenossQueueConfig.getConfig().getExchange("$Signals");
             logger.debug("Publishing signal: {}", signal);
             this.connectionManager.publish(destExchange, "zenoss.signal", signal);
         } catch (IOException ioe) {
             throw new ZepException(ioe);
         } catch (AmqpException e) {
             throw new ZepException(e);
         }
     }
 
     protected synchronized void processSpool(long processCutoffTime) {
         logger.debug("Processing signal spool");
         try {
             // TODO: This should be refactored to have findAllDue return consistent set of spool, event, subscription,
             // and trigger.
 
             // get spools that need to be processed
             List<EventSignalSpool> spools = this.signalSpoolDao.findAllDue();
             List<String> spoolsToDelete = new ArrayList<String>(spools.size());
 
             for (EventSignalSpool spool : spools) {
                 EventSummary eventSummary = this.eventSummaryDao.findByUuid(spool.getEventSummaryUuid());
                 EventStatus status = (eventSummary != null) ? eventSummary.getStatus() : null;
 
                 // These should have been deleted when the event was run through the TriggerPlugin when the status
                 // changed, but just in case delete them as the event is now in a closed state.
                 if (!OPEN_STATUSES.contains(status)) {
                     spoolsToDelete.add(spool.getUuid());
                     continue;
                 }
 
                 EventTriggerSubscription trSub = this.eventTriggerSubscriptionDao
                         .findByUuid(spool.getSubscriptionUuid());
                 if (trSub == null) {
                     logger.debug("Current spool entry no longer valid (subscription deleted), skipping: {}",
                             spool.getUuid());
                     continue;
                 }
 
                 // Check to see if trigger is still enabled
                 EventTrigger trigger = this.triggerDao.findByUuid(trSub.getTriggerUuid());
                 if (trigger == null) {
                     logger.debug("Current spool entry no longer valid (trigger deleted), skipping: {}",
                             spool.getUuid());
                     continue;
                 }
 
                 if (trigger.getEnabled()) {
                     publishSignal(eventSummary, trSub);
                     if (!spool.isSentSignal()) {
                         spool.setSentSignal(true);
                     }
                 }
 
                 int repeatInterval = trSub.getRepeatSeconds();
 
                 // Schedule the next repeat
                 if (repeatInterval > 0 && repeatInterval != Long.MAX_VALUE) {
                     long nextFlush = processCutoffTime + TimeUnit.SECONDS.toMillis(repeatInterval);
                     spool.setFlushTime(nextFlush);
                 }
                 else {
                     // Update the existing spool entry to make sure it won't send again
                     spool.setFlushTime(Long.MAX_VALUE);
                 }
                 this.signalSpoolDao.update(spool);
             }
             if (!spoolsToDelete.isEmpty()) {
                 this.signalSpoolDao.delete(spoolsToDelete);
             }
 
         } catch (Exception e) {
             logger.warn("Failed to process signal spool", e);
         }
     }
 }
 
