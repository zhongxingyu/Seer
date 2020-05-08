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
 package org.bitrepository.monitoringservice.collector;
 
 import org.bitrepository.access.getstatus.conversation.StatusCompleteContributorEvent;
 import org.bitrepository.client.eventhandler.EventHandler;
 import org.bitrepository.client.eventhandler.OperationEvent;
 import org.bitrepository.monitoringservice.alarm.MonitorAlerter;
 import org.bitrepository.monitoringservice.status.StatusStore;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The eventhandler for handling the events by the GetStatusClient.
  */
 public class GetStatusEventHandler implements EventHandler {
     /** The log.*/
     private final Logger log = LoggerFactory.getLogger(GetStatusEventHandler.class);
     /** The store for the status results. */
     private final StatusStore statusStore;
     /** The alarm dispatcher*/
     private final MonitorAlerter alerter;
 
     /**
      * Constructor.
      * @param statusStore The store for the status results.
      * @param alarmDispatcher The alarm dispatcher.
      */
     public GetStatusEventHandler(StatusStore statusStore, MonitorAlerter alarmDispatcher) {
         this.statusStore = statusStore;
         this.alerter = alarmDispatcher;
     }
     
     @Override
     public void handleEvent(OperationEvent event) {
         log.debug("Got event: " + event);
         
         switch(event.getType()) {
             case COMPONENT_COMPLETE:
                 StatusCompleteContributorEvent statusEvent = (StatusCompleteContributorEvent) event; 
                 statusStore.updateStatus(statusEvent.getContributorID(), statusEvent.getStatus());
                 break;
             case COMPLETE:
             case FAILED:
                 alerter.checkStatuses();
                 break;
             default:
                 break;
         }  
     }
 }
