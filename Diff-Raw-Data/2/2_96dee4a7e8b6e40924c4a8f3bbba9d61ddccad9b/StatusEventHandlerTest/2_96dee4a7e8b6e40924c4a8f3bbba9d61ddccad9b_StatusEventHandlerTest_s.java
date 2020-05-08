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
 import org.bitrepository.client.eventhandler.AbstractOperationEvent;
 import org.bitrepository.client.eventhandler.CompleteEvent;
 import org.bitrepository.client.eventhandler.DefaultEvent;
 import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
 import org.bitrepository.client.eventhandler.OperationFailedEvent;
 import org.bitrepository.monitoringservice.MockAlerter;
 import org.bitrepository.monitoringservice.MockStatusStore;
 import org.jaccept.structure.ExtendedTestCase;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 public class StatusEventHandlerTest extends ExtendedTestCase {
     @Test(groups = {"regressiontest"})
     public void testStatusEventHandler() throws Exception {
         addDescription("Test the GetStatusEventHandler handling of events");
         addStep("Setup", "");
         MockStatusStore store = new MockStatusStore();
         MockAlerter alerter = new MockAlerter();
         GetStatusEventHandler eventHandler = new GetStatusEventHandler(store, alerter);
         
         addStep("Validate initial calls to the mocks", "No calls expected");
         Assert.assertEquals(store.getCallsForGetStatusMap(), 0);
         Assert.assertEquals(store.getCallsForUpdateReplayCounts(), 0);
         Assert.assertEquals(store.getCallsForUpdateStatus(), 0);
         Assert.assertEquals(alerter.getCallsForCheckStatuses(), 0);
         
         addStep("Test an unhandled event.", "Should not make any calls.");
         AbstractOperationEvent event = new DefaultEvent();
         event.setEventType(OperationEventType.WARNING);
         eventHandler.handleEvent(event);
         
         Assert.assertEquals(store.getCallsForGetStatusMap(), 0);
         Assert.assertEquals(store.getCallsForUpdateReplayCounts(), 0);
         Assert.assertEquals(store.getCallsForUpdateStatus(), 0);
         Assert.assertEquals(alerter.getCallsForCheckStatuses(), 0);
         
         addStep("Test the Complete event", "Should make a call to the alerter");
         event = new CompleteEvent(null);
         eventHandler.handleEvent(event);
         Assert.assertEquals(store.getCallsForGetStatusMap(), 0);
         Assert.assertEquals(store.getCallsForUpdateReplayCounts(), 0);
         Assert.assertEquals(store.getCallsForUpdateStatus(), 0);
         Assert.assertEquals(alerter.getCallsForCheckStatuses(), 1);
         
         addStep("Test the Failed event", "Should make another call to the alerter");
         event = new OperationFailedEvent("info", null);
         eventHandler.handleEvent(event);
         Assert.assertEquals(store.getCallsForGetStatusMap(), 0);
         Assert.assertEquals(store.getCallsForUpdateReplayCounts(), 0);
         Assert.assertEquals(store.getCallsForUpdateStatus(), 0);
         Assert.assertEquals(alerter.getCallsForCheckStatuses(), 2);
         
         addStep("Test the component complete status", "Should attempt to update the store");
        event = new StatusCompleteContributorEvent("ContributorID", null);
         eventHandler.handleEvent(event);
         Assert.assertEquals(store.getCallsForGetStatusMap(), 0);
         Assert.assertEquals(store.getCallsForUpdateReplayCounts(), 0);
         Assert.assertEquals(store.getCallsForUpdateStatus(), 1);
         Assert.assertEquals(alerter.getCallsForCheckStatuses(), 2);
     }
 }
