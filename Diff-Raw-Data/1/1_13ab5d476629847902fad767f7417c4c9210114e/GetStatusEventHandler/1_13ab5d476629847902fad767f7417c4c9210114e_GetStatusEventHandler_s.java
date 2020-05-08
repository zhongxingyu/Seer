 /*
  * #%L
  * Bitrepository Monitoring Service
  * %%
  * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.monitoringservice;
 
 import org.bitrepository.access.getstatus.conversation.StatusCompleteContributorEvent;
 import org.bitrepository.client.eventhandler.EventHandler;
 import org.bitrepository.client.eventhandler.OperationEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class GetStatusEventHandler implements EventHandler {
 
     private final ComponentStatusStore statusStore;
     private final MonitoringServiceAlerter alerter;
     private final Logger log = LoggerFactory.getLogger(GetStatusEventHandler.class);
 
     public GetStatusEventHandler(ComponentStatusStore statusStore, MonitoringServiceAlerter alerter) {
         this.statusStore = statusStore;
         this.alerter = alerter;
         
     }
     
     @Override
     public void handleEvent(OperationEvent event) {
         log.debug("Got event: " + event);
         
         switch(event.getType()) {
         case IDENTIFY_REQUEST_SENT:
             break;
         case COMPONENT_IDENTIFIED:
             break;
         case IDENTIFICATION_COMPLETE:
             break;
         case REQUEST_SENT:
             break;
         case PROGRESS:
             break;
         case COMPONENT_COMPLETE:
             StatusCompleteContributorEvent statusEvent = (StatusCompleteContributorEvent) event; 
             statusStore.updateStatus(statusEvent.getContributorID(), statusEvent.getStatus());
             break;
         case COMPLETE:
             alerter.checkStatuses();
             break;
         case COMPONENT_FAILED:
             break;
         case FAILED:
             break;
         case NO_COMPONENT_FOUND:
             break;
         case IDENTIFY_TIMEOUT: 
             break;
         case WARNING:
             break;
         }  
         
     }
 
 }
