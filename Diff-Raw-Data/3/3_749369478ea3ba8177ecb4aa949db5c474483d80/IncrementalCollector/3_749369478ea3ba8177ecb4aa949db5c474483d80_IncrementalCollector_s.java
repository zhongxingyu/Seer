 /*
  * #%L
  * Bitrepository Audit Trail Service
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
 package org.bitrepository.audittrails.collector;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.bitrepository.access.getaudittrails.AuditTrailClient;
 import org.bitrepository.access.getaudittrails.AuditTrailQuery;
 import org.bitrepository.access.getaudittrails.BlockingAuditTrailClient;
 import org.bitrepository.access.getaudittrails.client.AuditTrailResult;
 import org.bitrepository.audittrails.store.AuditTrailStore;
 import org.bitrepository.bitrepositoryelements.AuditTrailEvents;
 import org.bitrepository.client.eventhandler.EventHandler;
 import org.bitrepository.client.eventhandler.OperationEvent;
 import org.bitrepository.client.exceptions.NegativeResponseException;
 import org.bitrepository.common.utils.TimeUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Will perform a single collection of audit trails, potential through multiple sequential getAuditTrail calls if the
  * set of new audit trails is large.
  */
 public class IncrementalCollector {
     private Logger log = LoggerFactory.getLogger(getClass());
     private final String clientID;
     private final BlockingAuditTrailClient client;
     private final AuditTrailStore store;
     private final int maxNumberOfResults;
 
     /** When no file id is wanted for the collecting of audit trails.*/
     private static final String NO_FILE_ID = null;
     /** When no delivery address is wanted for the collecting of audit trails.*/
     private static final String NO_DELIVERY_URL = null;
     /** Will be used in case of no MaxNumberOfResult are provided */
     public static final int DEFAULT_MAX_NUMBER_OF_RESULTS = 10000;
     private final String collectionID;
     private long collectedAudits = 0;
 
     /**
      * @param clientID The clientID to use for the requests.
      * @param client The client to use for the operations.
      * @param store Where to persist the received results.
      * @param maxNumberOfResults A optional limit on the number of audit trail events to request. If not set,
      * {}
      */
     public IncrementalCollector(String collectionID, String clientID, AuditTrailClient client, AuditTrailStore store,
                                 BigInteger maxNumberOfResults) {
         this.collectionID = collectionID;
         this.clientID = clientID;
         this.client = new BlockingAuditTrailClient(client);
         this.store = store;
         this.maxNumberOfResults = (maxNumberOfResults != null)?
             maxNumberOfResults.intValue() : DEFAULT_MAX_NUMBER_OF_RESULTS;
     }
 
     /**
      * Method to get the ID of the collection to get audit trails from
      * @return String The ID of the collection 
      */
     public String getCollectionID() {
         return collectionID;
     }
     
     public long getNumberOfCollectedAudits() {
         return collectedAudits;
     }
     
     /**
      * Setup and initiates the collection of audit trails through the client.
      * Adds one to the sequence number to request only newer audit trails.
      */
     public void performCollection(Collection<String> contributors) {
         collectedAudits = 0;
         List<AuditTrailQuery> queries = new ArrayList<AuditTrailQuery>();
         
         for(String contributorId : contributors) {
             int seq = store.largestSequenceNumber(contributorId, collectionID);
             queries.add(new AuditTrailQuery(contributorId, seq + 1, null, maxNumberOfResults));
         }
 
         AuditCollectorEventHandler handler = new AuditCollectorEventHandler();
         try {
             client.getAuditTrails(collectionID, queries.toArray(new AuditTrailQuery[queries.size()]), NO_FILE_ID,
                     NO_DELIVERY_URL, handler, clientID);
 
         } catch (NegativeResponseException e) {
             log.error("Problem in collecting audit trails, collection will not be complete", e);
         }
         if (!handler.contributorsWithPartialResults.isEmpty()) {
             performCollection(handler.contributorsWithPartialResults);
         }
     }
 
     /**
      * Event handler for the audit trail collector. The results of an audit trail operation will be ingested into the
      * audit trail store.
      */
     private class AuditCollectorEventHandler implements EventHandler {
         List<String> contributorsWithPartialResults = new LinkedList<String>();
         private final long startTime = System.currentTimeMillis();
 
         @Override
         public void handleEvent(OperationEvent event) {
             if(event instanceof AuditTrailResult) {
                 AuditTrailResult auditResult = (AuditTrailResult) event;
                 if (!auditResult.getCollectionID().equals(collectionID)) {
                     log.warn("Received bad collection id! Expected '" + collectionID + "', but got '"
                             + auditResult.getCollectionID() + "'.");
                     return;
                 }
                 if (auditResult.isPartialResult()) {
                     contributorsWithPartialResults.add(auditResult.getContributorID());
                 }
                 AuditTrailEvents events = auditResult.getAuditTrailEvents().getAuditTrailEvents();
                collectedAudits += events.getAuditTrailEvent().size();
                 store.addAuditTrails(events, collectionID);
                 if (events != null && events.getAuditTrailEvent() != null) {
                     log.debug("Collected and stored " + events.getAuditTrailEvent().size() +
                             " audit trail event from " + auditResult.getContributorID() + " in " +
                             TimeUtils.millisecondsToHuman(System.currentTimeMillis() - startTime) 
                             + " (PartialResult=" + auditResult.isPartialResult() + ".");
                 }
             } else if (event.getEventType() == OperationEvent.OperationEventType.COMPONENT_FAILED ||
                 event.getEventType() == OperationEvent.OperationEventType.FAILED ||
                 event.getEventType() == OperationEvent.OperationEventType.IDENTIFY_TIMEOUT) {
                 log.warn("Event: " + event.toString());
             } else {
                 log.debug("Event:" + event.toString());
             }
         }
     }
 }
