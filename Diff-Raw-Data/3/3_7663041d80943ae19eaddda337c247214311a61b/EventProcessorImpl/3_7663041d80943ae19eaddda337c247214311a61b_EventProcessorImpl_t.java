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
 
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.transaction.annotation.Transactional;
 import org.zenoss.protobufs.zep.Zep.Event;
 import org.zenoss.protobufs.zep.Zep.EventStatus;
 import org.zenoss.protobufs.zep.Zep.RawEvent;
 import org.zenoss.protobufs.zep.Zep.ZepRawEvent;
 import org.zenoss.zep.EventContext;
 import org.zenoss.zep.EventPreProcessingPlugin;
 import org.zenoss.zep.EventProcessor;
 import org.zenoss.zep.PluginService;
 import org.zenoss.zep.ZepException;
 import org.zenoss.zep.dao.EventStoreDao;
 
 import com.google.protobuf.Descriptors.Descriptor;
 import com.google.protobuf.Descriptors.FieldDescriptor;
 
 /**
  * Default implementation of {@link EventProcessor} which uses
  * {@link PluginService} to load the appropriate plug-ins and process events.
  */
 public class EventProcessorImpl implements EventProcessor {
 
     private static final Logger logger = LoggerFactory.getLogger(EventProcessorImpl.class);
 
     private static final String EVENT_CLASS_UNKNOWN = "/Unknown";
 
     private PluginService pluginService;
 
     private EventStoreDao eventStoreDao;
 
     public void setEventStoreDao(EventStoreDao eventStoreDao) {
         this.eventStoreDao = eventStoreDao;
     }
 
     /**
      * Sets the plug-in service used to look up configured plug-ins.
      * 
      * @param pluginService
      *            The plug-in service to use to look up configured plug-ins.
      */
     public void setPluginService(PluginService pluginService) {
         this.pluginService = pluginService;
     }
 
     private static Event eventFromRawEvent(ZepRawEvent zepRawEvent) {
         Descriptor eventDesc = Event.getDescriptor();
         Event.Builder eventBuilder = Event.newBuilder();
         final RawEvent rawEvent = zepRawEvent.getRawEvent();
         Map<FieldDescriptor, Object> values = rawEvent.getAllFields();
         for (Map.Entry<FieldDescriptor, Object> entry : values.entrySet()) {
             FieldDescriptor eventFieldDesc = eventDesc.findFieldByName(entry.getKey().getName());
             if (eventFieldDesc != null) {
                 eventBuilder.setField(eventFieldDesc, entry.getValue());
             }
         }
         if (zepRawEvent.getTagsCount() > 0) {
             eventBuilder.addAllTags(zepRawEvent.getTagsList());
         }
        if (!zepRawEvent.getEventClassMappingUuid().isEmpty()) {
            eventBuilder.setEventClassMappingUuid(zepRawEvent.getEventClassMappingUuid());
        }
         // Default to event class unknown.
         if (eventBuilder.getEventClass().isEmpty()) {
             eventBuilder.setEventClass(EVENT_CLASS_UNKNOWN);
         }
         return eventBuilder.build();
     }
 
     @Override
     @Transactional
     public void processEvent(ZepRawEvent zepRawEvent) throws ZepException {
         logger.debug("processEvent: event={}", zepRawEvent);
 
         if (zepRawEvent.getStatus() == EventStatus.STATUS_DROPPED) {
             logger.debug("Event dropped: {}", zepRawEvent);
             return;
         } else if (zepRawEvent.getRawEvent().getUuid().isEmpty()) {
             logger.error("Could not process event, has no uuid: {}",
                     zepRawEvent);
             return;
         } else if (!zepRawEvent.getRawEvent().hasCreatedTime()) {
             logger.error("Could not process event, has no created_time: {}",
                     zepRawEvent);
             return;
         }
 
         EventContext ctx = new EventContextImpl(zepRawEvent);
 
         Event event = eventFromRawEvent(zepRawEvent);
 
         for (EventPreProcessingPlugin plugin : pluginService.getPreProcessingPlugins()) {
             Event modified = plugin.processEvent(event, ctx);
             if (ctx.getStatus() == EventStatus.STATUS_DROPPED) {
                 logger.debug("Event dropped by {}", plugin.getName());
                 return;
             }
 
             if (modified != null && !modified.equals(event)) {
                 logger.debug("Event modified by {} as {}", plugin.getName(), modified);
                 event = modified;
             }
         }
 
         this.eventStoreDao.create(event, ctx);
     }
 
 }
