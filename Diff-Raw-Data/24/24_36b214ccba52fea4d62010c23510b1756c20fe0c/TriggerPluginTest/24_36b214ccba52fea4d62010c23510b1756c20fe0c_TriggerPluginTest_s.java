 /*
  * Copyright (C) 2010, Zenoss Inc.  All Rights Reserved.
  */
 package org.zenoss.zep.impl;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.scheduling.TaskScheduler;
 import org.springframework.scheduling.Trigger;
 import org.zenoss.protobufs.model.Model.ModelElementType;
 import org.zenoss.protobufs.zep.Zep;
 import org.zenoss.protobufs.zep.Zep.Event;
 import org.zenoss.protobufs.zep.Zep.EventActor;
 import org.zenoss.protobufs.zep.Zep.EventActorOrBuilder;
 import org.zenoss.protobufs.zep.Zep.EventDetail;
 import org.zenoss.protobufs.zep.Zep.EventOrBuilder;
 import org.zenoss.protobufs.zep.Zep.EventSummary;
 import org.zenoss.protobufs.zep.Zep.SyslogPriority;
 import org.zenoss.zep.ZepConstants;
 import org.zenoss.zep.ZepException;
 import org.zenoss.zep.dao.EventSignalSpool;
 import org.zenoss.zep.dao.EventSignalSpoolDao;
 import org.zenoss.zep.impl.TriggerPlugin.RuleContext;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ScheduledFuture;
 
 import static org.easymock.EasyMock.*;
 import static org.junit.Assert.*;
 
 
 public class TriggerPluginTest {
 
     public TriggerPlugin triggerPlugin = null;
     private EventSignalSpoolDao spoolDaoMock;
     private TaskScheduler schedulerMock;
     private ScheduledFuture futureMock;
 
     @Before
     public void testInit() throws IOException, ZepException {
         Map<String,String> props = new HashMap<String,String>();
         this.triggerPlugin = new TriggerPlugin();
         this.spoolDaoMock = createMock(EventSignalSpoolDao.class);
         expect(spoolDaoMock.findAllDue()).andReturn(Collections.<EventSignalSpool> emptyList()).anyTimes();
         this.schedulerMock = createMock(TaskScheduler.class);
         this.futureMock = createNiceMock(ScheduledFuture.class);
         expect(schedulerMock.schedule(isA(Runnable.class), isA(Trigger.class))).andReturn(futureMock);
         replay(spoolDaoMock, schedulerMock, futureMock);
         this.triggerPlugin.setSignalSpoolDao(this.spoolDaoMock);
         this.triggerPlugin.setTaskScheduler(this.schedulerMock);
         this.triggerPlugin.start(props);
     }
     
     @After
     public void shutdown() throws InterruptedException {
         this.triggerPlugin.stop();
         verify(this.spoolDaoMock, this.schedulerMock, this.futureMock);
     }
 
     private EventActor.Builder createActor() {
         EventActor.Builder actorBuilder = EventActor.newBuilder();
         actorBuilder.setElementTypeId(ModelElementType.DEVICE);
         actorBuilder.setElementIdentifier("BHM1000");
         actorBuilder.setElementTitle("BHM TITLE");
         actorBuilder.setElementSubTypeId(ModelElementType.COMPONENT);
         actorBuilder.setElementSubIdentifier("Fuse-10A");
         actorBuilder.setElementSubTitle("Fuse-10A Title");
         return actorBuilder;
     }
     
     private Event.Builder createEventOccurrence(EventActor actor) {
         Event.Builder evtBuilder = Event.newBuilder();
         evtBuilder.setActor(actor);
         evtBuilder.setMessage("TEST - 1-2-check");
         evtBuilder.setEventClass("/Defcon/1");
         evtBuilder.setSeverity(Zep.EventSeverity.SEVERITY_WARNING);
         evtBuilder.setSyslogPriority(SyslogPriority.SYSLOG_PRIORITY_DEBUG);
 
         EventDetail.Builder groupBuilder = evtBuilder.addDetailsBuilder().setName(ZepConstants.DETAIL_DEVICE_GROUPS);
         groupBuilder.addValue("/US/Texas/Austin");
 
         EventDetail.Builder systemsBuilder = evtBuilder.addDetailsBuilder().setName(ZepConstants.DETAIL_DEVICE_SYSTEMS);
         systemsBuilder.addValue("/Production/Infrastructure");
 
         return evtBuilder;
     }
     
     private EventSummary.Builder createEvent(Event event) {
         EventSummary.Builder evtSumBuilder = EventSummary.newBuilder();
         evtSumBuilder.setCount(10);
         evtSumBuilder.setStatus(Zep.EventStatus.STATUS_NEW);
         evtSumBuilder.addOccurrence(event);
         return evtSumBuilder;
     }
 
     @Test
     public void testTriggerRules() throws IOException {
         EventActor.Builder actorBuilder = createActor();
 
         // build test Event to add to EventSummary as occurrence[0]
         Event.Builder eventBuilder = createEventOccurrence(actorBuilder.build());
 
         // build test EventSummary
         EventSummary evtSummary = createEvent(eventBuilder.build()).build();
 
         // test various rules
         String[] true_rules = {
                 "1 == 1",
                 "evt.message.startswith('TEST')",
                 "evt.severity == 3",
                 "evt.event_class == '/Defcon/1'",
                 "evt.count > 5",
                 "dev.name == 'BHM1000'",
                 "elem.name == 'BHM1000'",
                 "elem.type == 'DEVICE'",
                 "sub_elem.type == 'COMPONENT'",
                 "sub_elem.name.lower().startswith('fuse')",
                 "evt.syslog_priority == 7",
                 "\"/US\" in dev.groups",
                 "\"/US/Texas\" in dev.groups",
                 "\"/US/Texas/Austin\" in dev.groups",
                 "\"/Production\" in dev.systems",
                 "\"/Production/Infrastructure\" in dev.systems"
         };
         String[] false_rules = {
                 "1 = 0", // try a syntax error
                 "", // try empty string
                 "evt.msg == 'fail!'", // nonexistent attribute
                 "1 == 0",
                 "evt.message.startswith('BEST')",
                 "evt.count > 15",
                 "evt.severity = 'critical'",
                 "dev.name == 'BHM1001'",
                 "evt.syslog_priority == 5",
                 "\"/US/Tex\" in dev.groups",
                 "\"/US/TexasTheLoneStarState\" in dev.groups",
                 "\"/US/Texas/Austin/Bridge Point\" in dev.groups",
                 "\"/Texas\" in dev.groups",
                 "\"/Austin\" in dev.groups",
                 "\"/Prod\" in dev.systems",
                 "\"/Infrastructure\" in dev.systems"
         };
 
         RuleContext ctx = RuleContext.createContext(triggerPlugin.pythonHelper.getToObject(), evtSummary);
         for(String rule: true_rules) {
             assertTrue(rule + " (should evaluate True)",
                     this.triggerPlugin.eventSatisfiesRule(ctx, rule));
         }
 
         for(String rule: false_rules) {
             assertFalse(rule + " (should evaluate False)",
                     this.triggerPlugin.eventSatisfiesRule(ctx, rule));
         }
     }
 
     @Test
     public void testEmptyGroups() {
         EventSummary.Builder evtSummary = createEvent(createEventOccurrence(createActor().build()).build());
         // Remove the group detail
         evtSummary.getOccurrenceBuilder(0).removeDetails(0);
         
         String rule = "\"/Production/Infrastructure\" not in dev.groups";
 
         RuleContext ctx = RuleContext.createContext(triggerPlugin.pythonHelper.getToObject(), evtSummary.build());
         assertEquals(0, ctx.device.__getattr__("groups").__len__());
         assertTrue(rule + " (should evaluate True)", this.triggerPlugin.eventSatisfiesRule(ctx, rule));
     }
 
     @Test
     public void testEmptySystems() {
         EventSummary.Builder evtSummary = createEvent(createEventOccurrence(createActor().build()).build());
         // Remove the systems detail
         evtSummary.getOccurrenceBuilder(0).removeDetails(1);
 
         String rule = "\"/Production/Infrastructure\" not in dev.systems";
 
         RuleContext ctx = RuleContext.createContext(triggerPlugin.pythonHelper.getToObject(), evtSummary.build());
         assertEquals(0, ctx.device.__getattr__("systems").__len__());
         assertTrue(rule + " (should evaluate True)", this.triggerPlugin.eventSatisfiesRule(ctx, rule));
     }
 }
 
