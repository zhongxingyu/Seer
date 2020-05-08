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
 package org.bitrepository.audittrails.store;
 
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_CONTRIBUTOR_GUID;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_SEQUENCE_NUMBER;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_TABLE;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.CONTRIBUTOR_GUID;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.CONTRIBUTOR_ID;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.CONTRIBUTOR_PRESERVATION_SEQ;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.CONTRIBUTOR_TABLE;
 
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.List;
 
 import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
 import org.bitrepository.bitrepositoryelements.AuditTrailEvents;
 import org.bitrepository.bitrepositoryelements.FileAction;
 import org.bitrepository.common.ArgumentValidator;
 import org.bitrepository.common.database.DBConnector;
 import org.bitrepository.common.database.DBSpecifics;
 import org.bitrepository.common.database.DatabaseSpecificsFactory;
 import org.bitrepository.common.database.DatabaseUtils;
 import org.bitrepository.common.settings.Settings;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Audit trail storages backed by a database for preserving
  */
 public class AuditTrailServiceDAO implements AuditTrailStore {
     /** The log.*/
     private Logger log = LoggerFactory.getLogger(getClass());
     /** The connection to the database.*/
     private DBConnector dbConnector;
     
     /** 
      * Constructor.
      * @param settings The settings.
      */
     public AuditTrailServiceDAO(Settings settings) {
         ArgumentValidator.checkNotNull(settings, "settings");
         
         DBSpecifics dbSpecifics = DatabaseSpecificsFactory.retrieveDBSpecifics(
                 settings.getReferenceSettings().getAuditTrailServiceSettings().getAuditServiceDatabaseSpecifics());
         dbConnector = new DBConnector(dbSpecifics, 
                 settings.getReferenceSettings().getAuditTrailServiceSettings().getAuditTrailServiceDatabaseUrl());
         
         dbConnector.getConnection();
     }
     
     @Override
     public List<AuditTrailEvent> getAuditTrails(String fileId, String contributorId, Long minSeqNumber, 
             Long maxSeqNumber, String actorName, FileAction operation, Date startDate, Date endDate) {
         ExtractModel model = new ExtractModel();
         model.setFileId(fileId);
         model.setContributorId(contributorId);
         model.setMinSeqNumber(minSeqNumber);
         model.setMaxSeqNumber(maxSeqNumber);
         model.setActorName(actorName);
         model.setOperation(operation);
         model.setStartDate(startDate);
         model.setEndDate(endDate);
 
         AuditDatabaseExtractor extractor = new AuditDatabaseExtractor(model, dbConnector.getConnection());
         return extractor.extractAuditEvents();
     }
     
     @Override
     public void addAuditTrails(AuditTrailEvents newAuditTrails) {
         ArgumentValidator.checkNotNull(newAuditTrails, "AuditTrailEvents newAuditTrails");
         
         AuditDatabaseIngestor ingestor = new AuditDatabaseIngestor(dbConnector.getConnection());
         for(AuditTrailEvent event : newAuditTrails.getAuditTrailEvent()) {
             ingestor.ingestAuditEvents(event);
         }
     }
     
     @Override
     public int largestSequenceNumber(String contributorId) {
         ArgumentValidator.checkNotNullOrEmpty(contributorId, "String contributorId");
         String sql = "SELECT " + AUDITTRAIL_SEQUENCE_NUMBER + " FROM " + AUDITTRAIL_TABLE + " WHERE " 
                 + AUDITTRAIL_CONTRIBUTOR_GUID + " = ( SELECT " + CONTRIBUTOR_GUID + " FROM " + CONTRIBUTOR_TABLE 
                 + " WHERE " + CONTRIBUTOR_ID + " = ? ) ORDER BY " + AUDITTRAIL_SEQUENCE_NUMBER + " DESC";
         
         Long seq = DatabaseUtils.selectFirstLongValue(dbConnector.getConnection(), sql, contributorId);
         if(seq != null) {
             return seq.intValue();
         }
         return 0;
     }    
 
     @Override
     public long getPreservationSequenceNumber(String contributorId) {
         ArgumentValidator.checkNotNullOrEmpty(contributorId, "String contributorId");
         String sql = "SELECT " + CONTRIBUTOR_PRESERVATION_SEQ + " FROM " + CONTRIBUTOR_TABLE + " WHERE " 
                + CONTRIBUTOR_ID + " = ? ) ";
         
         Long seq = DatabaseUtils.selectLongValue(dbConnector.getConnection(), sql, contributorId);
         if(seq != null) {
             return seq.intValue();
         }
         return 0;
     }
 
     @Override
     public void setPreservationSequenceNumber(String contributorId, long seqNumber) {
         ArgumentValidator.checkNotNullOrEmpty(contributorId, "String contributorId");
         ArgumentValidator.checkNotNegative(seqNumber, "int seqNumber");
         String sqlUpdate = "UPDATE " + CONTRIBUTOR_TABLE + " SET " + CONTRIBUTOR_PRESERVATION_SEQ + " = ? WHERE " 
                 + CONTRIBUTOR_ID + " = ? ";
         DatabaseUtils.executeStatement(dbConnector.getConnection(), sqlUpdate, seqNumber, contributorId);
     }
 
     @Override
     public void close() {
         try {
             dbConnector.getConnection().close();
         } catch (SQLException e) {
             log.warn("Cannot close the database properly.", e);
         }
     }
 }
