 /*
  * This program is part of Zenoss Core, an open source monitoring platform.
  * Copyright (C) 2010, Zenoss Inc.
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 2 as published by
  * the Free Software Foundation.
  *
  * For complete information please visit: http://www.zenoss.com/oss/
  */
 package org.zenoss.zep.impl;
 
 import static org.easymock.EasyMock.*;
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.zenoss.protobufs.model.Model.ModelElementType;
 import org.zenoss.protobufs.zep.Zep;
 import org.zenoss.protobufs.zep.Zep.Event;
 import org.zenoss.protobufs.zep.Zep.EventActor;
 import org.zenoss.protobufs.zep.Zep.EventSummary;
 import org.zenoss.zep.ZepException;
 import org.zenoss.zep.dao.EventSignalSpool;
 import org.zenoss.zep.dao.EventSignalSpoolDao;
 
 
 public class TriggerPluginImplTest {
 
     public  TriggerPlugin triggerPlugin = null;
     private EventSignalSpoolDao spoolDaoMock;
 
     @Before
     public void testInit() throws IOException, ZepException {
         Map<String,String> props = new HashMap<String,String>();
         this.triggerPlugin = new TriggerPlugin();
         spoolDaoMock = createMock(EventSignalSpoolDao.class);
         expect(spoolDaoMock.findAllDue()).andReturn(Collections.<EventSignalSpool> emptyList()).anyTimes();
         replay(spoolDaoMock);
         this.triggerPlugin.setSignalSpoolDao(this.spoolDaoMock);
         this.triggerPlugin.init(props);
     }
     
     @After
     public void shutdown() throws InterruptedException {
         this.triggerPlugin.shutdown();
         verify(this.spoolDaoMock);
     }
 
     @Test
     public void testTriggerRules() throws IOException {
         
         EventActor.Builder actorBuilder = EventActor.newBuilder();
         actorBuilder.setElementTypeId(ModelElementType.DEVICE);
         actorBuilder.setElementIdentifier("BHM1000");
         actorBuilder.setElementSubTypeId(ModelElementType.COMPONENT);
         actorBuilder.setElementSubIdentifier("Fuse-10A");
 
         // build test Event to add to EventSummary as occurrence[0]
         Event.Builder evtBuilder = Event.newBuilder();
         evtBuilder.setActor(actorBuilder.build());
         evtBuilder.setMessage("TEST - 1-2-check");
         evtBuilder.setEventClass("/Defcon/1");
         evtBuilder.setSeverity(Zep.EventSeverity.SEVERITY_WARNING);
         Event evt = evtBuilder.build();
 
         // build test EventSummary
         EventSummary.Builder evtSumBuilder = EventSummary.newBuilder();
         evtSumBuilder.setCount(10);
         evtSumBuilder.setStatus(Zep.EventStatus.STATUS_NEW);
         evtSumBuilder.addOccurrence(evt);
         EventSummary evtSummary = evtSumBuilder.build();
 
         // test various rules
         String[] true_rules = {
                 "1 == 1",
                 "evt.message.startswith('TEST')",
                 "evt.severity == 'warning'",
                 "evt.event_class == '/Defcon/1'",
                 "evt.count > 5",
                 "dev.name == 'BHM1000'",
                 "component.name.lower().startswith('fuse')",
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
         };
 
         for(String rule: true_rules) {
             assertTrue(rule + " (should evaluate True)",
                     this.triggerPlugin.eventSatisfiesRule(evtSummary, rule));
         }
 
         for(String rule: false_rules) {
             assertFalse(rule + " (should evaluate False)",
                     this.triggerPlugin.eventSatisfiesRule(evtSummary, rule));
         }
     }
 }
 
