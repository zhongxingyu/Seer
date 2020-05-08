 /*
  * #%L
  * Bitrepository Audit Trail Service
  * 
  * $Id$
  * $HeadURL$
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
 package org.bitrepository.audittrails.service;
 
 import java.util.Collection;
 import java.util.Date;
 
 import org.bitrepository.audittrails.collector.AuditTrailCollector;
 import org.bitrepository.audittrails.store.AuditTrailStore;
 import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
 import org.bitrepository.bitrepositoryelements.FileAction;
 import org.bitrepository.service.LifeCycledService;
 
 /**
  * Class to expose the functionality of the AuditTrailService. 
  * Aggregates the needed classes.   
  */
 public class AuditTrailService implements LifeCycledService {
     /** The storage of audit trail information.*/
     private final AuditTrailStore store;
     /** The collector of new audit trails.*/
     private final AuditTrailCollector collector;
     
     /**
      * Constructor.
      * @param store The store for the audit trail data.
      * @param collector The collector of new audit trail data.
      */
     public AuditTrailService(AuditTrailStore store, AuditTrailCollector collector) {
         
         this.store = store;
         this.collector = collector;
     }
     
     /**
      * Retrieve all AuditTrailEvents matching the criteria from the parameters.
      * All parameters are allowed to be null, meaning that the parameter imposes no restriction on the result
      * @param fromDate Restrict the results to only provide events after this point in time
      * @param toDate Restrict the results to only provide events up till this point in time
      * @param fileID Restrict the results to only be about this fileID
      * @param reportingComponent Restrict the results to only be reported by this component
      * @param Actor Restrict the results to only be events caused by this actor
      * @param Action Restrict the results to only be about this type of action
      */
     public Collection<AuditTrailEvent> queryAuditTrailEvents(Date fromDate, Date toDate, String fileID, 
             String reportingComponent, String actor, String action) {
         FileAction operation;
         if(action != null) {
             operation = FileAction.fromValue(action);
         } else {
             operation = null;
         }
         
         
        return store.getAuditTrails(fileID, actor, null, null, actor, operation, fromDate, toDate);
     }
     
     /**
      * Collects all the newest audit trails.
      */
     public void collectAuditTrails() {
         collector.collectNewestAudits();
     }
 
     @Override
     public void shutdown() {
         // Nothing to do here yet..
     }
 }
