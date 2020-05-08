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
 package org.bitrepository.audittrails.preserver;
 
 import java.util.Map;
 
 import org.bitrepository.audittrails.store.AuditTrailStore;
 import org.bitrepository.client.eventhandler.EventHandler;
 import org.bitrepository.client.eventhandler.OperationEvent;
 import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The event handler for the preservation of audit trail data.
  * When the PutFile operation has completed, then the store will be updated with the results.
  * 
  * It is not necessary to wait until all the components are complete. Just the first.
  * This eventhandler can only be used for handling the PutFile operation of a single AuditTrail package.
  */
 public class AuditPreservationEventHandler implements EventHandler {
     /** The log.*/
     private Logger log = LoggerFactory.getLogger(getClass());
     /** The map between the contributors and their sequence number.*/
     private final Map<String, Long> seqNumbers;
     /** The store for the audit trails. Where the new preservation sequence numbers should be inserted.*/
     private final AuditTrailStore store;
     /** Whether the store has been updated with the values.*/
     private boolean updated;
     
     /**
      * Constructor.
      * @param preservationSequenceNumber The map between the contributor ids and their respective sequence number.
      * @param store The store which should be updated with these sequence numbers.
      */
     public AuditPreservationEventHandler(Map<String, Long> preservationSequenceNumber, AuditTrailStore store) {
         this.seqNumbers = preservationSequenceNumber;
         this.store = store;
         this.updated = false;
     }
     
     @Override
     public void handleEvent(OperationEvent event) {
         if(event.getEventType() == OperationEventType.COMPLETE) {
             updateStoreWithResults(event.getCollectionID());
         } else {
             log.debug("Event for preservation of audit trails: " + event.toString());
         }
     }
     
     /**
      * Update the store with the results.
      * @param collectionId The id of the collection.
      */
     private void updateStoreWithResults(String collectionId) {
         if(updated) {
             log.debug("Have already updated the store with the new preservation sequence number.");
             return;
         }
         updated = true;
         
         for(Map.Entry<String, Long> entry : seqNumbers.entrySet()) {
            store.setPreservationSequenceNumber(entry.getKey(), collectionId, entry.getValue());
         }
     }
 }
