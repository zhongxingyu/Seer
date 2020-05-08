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
 
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.ACTOR_KEY;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.ACTOR_NAME;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.ACTOR_TABLE;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_ACTOR_KEY;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_AUDIT;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_CONTRIBUTOR_KEY;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_FILE_KEY;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_INFORMATION;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_OPERATION;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_OPERATION_DATE;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_SEQUENCE_NUMBER;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_TABLE;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.COLLECTION_ID;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.COLLECTION_KEY;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.COLLECTION_TABLE;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.CONTRIBUTOR_ID;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.CONTRIBUTOR_KEY;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.CONTRIBUTOR_TABLE;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.FILE_COLLECTION_KEY;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.FILE_FILEID;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.FILE_KEY;
 import static org.bitrepository.audittrails.store.AuditDatabaseConstants.FILE_TABLE;
 
 import java.math.BigInteger;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
 import org.bitrepository.bitrepositoryelements.FileAction;
 import org.bitrepository.common.ArgumentValidator;
 import org.bitrepository.common.utils.CalendarUtils;
 import org.bitrepository.service.database.DBConnector;
 import org.bitrepository.service.database.DatabaseUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Extractor for the audit trail events from the AuditTrailServiceDatabase.
  * 
  * Order of extraction:
  * FileId, ContributorId, SequenceNumber, SeqNumber, ActorName, Operation, OperationDate, AuditTrail, Information
  */
 public class AuditDatabaseExtractor {
     /** The log.*/
     private Logger log = LoggerFactory.getLogger(getClass());
     
     /** Position of the FileId in the extraction.*/
     private static final int POSITION_FILE_ID = 1;
     /** Position of the ContributorId in the extraction.*/
     private static final int POSITION_CONTRIBUTOR_ID = 2;
     /** Position of the SequenceNumber in the extraction.*/
     private static final int POSITION_SEQUENCE_NUMBER = 3;
     /** Position of the ActorName in the extraction.*/
     private static final int POSITION_ACTOR_NAME = 4;
     /** Position of the Operation in the extraction.*/
     private static final int POSITION_OPERATION = 5;
     /** Position of the OperationDate in the extraction.*/
     private static final int POSITION_OPERATION_DATE = 6;
     /** Position of the AuditTrail in the extraction.*/
     private static final int POSITION_AUDIT_TRAIL = 7;
     /** Position of the Information in the extraction.*/
     private static final int POSITION_INFORMATION = 8;
     
     /** The model containing the elements for the restriction.*/
     private final ExtractModel model;
     /** The connector to the database.*/
     private final DBConnector dbConnector;
     
     /**
      * Constructor.
      * @param model The model for the restriction for the extraction from the database.
      * @param dbConnector The connector to the database, where the audit trails are to be extracted.
      */
     public AuditDatabaseExtractor(ExtractModel model, DBConnector dbConnector) {
         ArgumentValidator.checkNotNull(model, "ExtractModel model");
         ArgumentValidator.checkNotNull(dbConnector, "DBConnector dbConnector");
         
         this.model = model;
         this.dbConnector = dbConnector;
     }
     
     /**
      * Extracts the requested audit trails.
      * @return The audit trails requested through the ExtractModel.
      */
     public List<AuditTrailEvent> extractAuditEvents() {
         String sql = createSelectString() + " FROM " + AUDITTRAIL_TABLE + joinWithFileTable() + joinWithActorTable() 
                 + joinWithContributorTable() + createRestriction();
         System.err.println(sql);
         try {
             Connection conn = null;
             PreparedStatement ps = null;
             ResultSet result = null;
             List<AuditTrailEvent> res = new ArrayList<AuditTrailEvent>();
             long starttime = System.currentTimeMillis();
             try {
                 conn = dbConnector.getConnection();
                 log.debug("Extracting sql '" + sql + "' with arguments '" + Arrays.asList(extractArgumentsFromModel()));
                 ps = DatabaseUtils.createPreparedStatement(conn, sql, extractArgumentsFromModel());
                 result = ps.executeQuery();
                 
                 int i = 0;
                 while(result.next() && i < model.getMaxCount()) {
                     res.add(extractEvent(result));
                     i++;
                 }
             } finally {
                 if(result != null) {
                     result.close();
                 }
                 if(ps != null) {
                     ps.close();
                 }
                 if(conn != null) {
                     conn.close();
                 }
             }
             log.debug("Read " + res.size() + " audit trails in " + (System.currentTimeMillis() - starttime) + " ms");
             
             return res;
         } catch (Exception e) {
             throw new IllegalStateException("Could not retrieve the wanted data from the database with the sql '" 
                     + sql + "'", e);
         }
     }
     
     /**
      * Method to extract the requested audit trails
      * @return {@link AuditEventIterator} Iterator for extracting the Audittrails 
      */
     public AuditEventIterator extractAuditEventsByIterator() {
         String sql = createSelectString() + " FROM " + AUDITTRAIL_TABLE + joinWithFileTable() + joinWithActorTable() 
                 + joinWithContributorTable() + createRestriction();
         try {
             PreparedStatement ps = DatabaseUtils.createPreparedStatement(dbConnector.getConnection(), 
                     sql, extractArgumentsFromModel());
             return new AuditEventIterator(ps, dbConnector);
         } catch (Exception e) {
             throw new IllegalStateException("Failed to retrieve the audit trails from the database", e);
         }
        
     }
     
     /**
      * Extracts a single AuditEvent from a single result set.
      * TODO this makes several calls to be database to extract the file id, actor name and contributor id. It could be
      * reduced by joining the tables in the database in the request.
      * @param resultSet The result set to extract the AuditEvent from.
      * @return The extracted AuditEvent.
      */
     private AuditTrailEvent extractEvent(ResultSet resultSet) throws SQLException {
         AuditTrailEvent event = new AuditTrailEvent();
         
         event.setActionDateTime(CalendarUtils.getFromMillis(resultSet.getTimestamp(POSITION_OPERATION_DATE).getTime()));
         event.setActionOnFile(FileAction.fromValue(resultSet.getString(POSITION_OPERATION)));
         event.setAuditTrailInformation(resultSet.getString(POSITION_AUDIT_TRAIL));
         event.setActorOnFile(resultSet.getString(POSITION_ACTOR_NAME));
         event.setFileID(resultSet.getString(POSITION_FILE_ID));
         event.setInfo(resultSet.getString(POSITION_INFORMATION));
         event.setReportingComponent(resultSet.getString(POSITION_CONTRIBUTOR_ID));
         event.setSequenceNumber(BigInteger.valueOf(resultSet.getLong(POSITION_SEQUENCE_NUMBER)));
         
         return event;
     }
     
     /**
      * NOTE: This is where the position of the constants come into play. 
      * E.g. POSITION_FILE_GUID = 1 refers to the first extracted element being the AUDITTRAIL_FILE_GUID.
      * @return Creates the SELECT string for the retrieval of the audit events.
      */
     private String createSelectString() {
         StringBuilder res = new StringBuilder();
         
         res.append("SELECT ");
         res.append(FILE_TABLE + "." + FILE_FILEID + ", ");
        res.append(AUDITTRAIL_TABLE + "." + AUDITTRAIL_CONTRIBUTOR_KEY + ", ");
         res.append(AUDITTRAIL_TABLE + "." + AUDITTRAIL_SEQUENCE_NUMBER + ", ");
         res.append(ACTOR_TABLE + "." + ACTOR_NAME + ", ");
         res.append(AUDITTRAIL_TABLE + "." + AUDITTRAIL_OPERATION + ", ");
         res.append(AUDITTRAIL_TABLE + "." + AUDITTRAIL_OPERATION_DATE + ", ");
         res.append(AUDITTRAIL_TABLE + "." + AUDITTRAIL_AUDIT + ", ");
         res.append(AUDITTRAIL_TABLE + "." + AUDITTRAIL_INFORMATION + " ");
         
         return res.toString();
     }
     
     /**
      * Joining the AuditTrail table with the File table.
      * 
      * @return The sql for joining the tables.
      */
     private String joinWithFileTable() {
         return " JOIN " + FILE_TABLE + " ON " + AUDITTRAIL_TABLE + "." + AUDITTRAIL_FILE_KEY + " = " + FILE_TABLE + "."
                 + FILE_KEY + " "; 
     }
 
     /**
      * Joining the AuditTrail table with the Actor table.
      * 
      * @return The sql for joining the tables.
      */
     private String joinWithActorTable() {
         return " JOIN " + ACTOR_TABLE + " ON " + AUDITTRAIL_TABLE + "." + AUDITTRAIL_ACTOR_KEY + " = " + ACTOR_TABLE 
                 + "." + ACTOR_KEY + " "; 
     }
     
     /**
      * Joining the AuditTrail table with the Contributor table.
      * 
      * @return The sql for joining the tables.
      */
     private String joinWithContributorTable() {
         return " JOIN " + CONTRIBUTOR_TABLE + " ON " + AUDITTRAIL_TABLE + "." + AUDITTRAIL_CONTRIBUTOR_KEY + " = " 
                 + CONTRIBUTOR_TABLE + "." + CONTRIBUTOR_KEY + " "; 
     }
     
     /**
      * Create the restriction part of the SQL statement for extracting the requested data from the database.
      * @return The restriction, or empty string if no restrictions.
      */
     private String createRestriction() {
         StringBuilder res = new StringBuilder();
         
         if(model.getFileId() != null) {
             nextArgument(res);
             res.append(FILE_TABLE + "."+ FILE_FILEID + " = ? ");
         }
 
         if(model.getCollectionId() != null) {
             nextArgument(res);
             res.append(FILE_TABLE + "." + FILE_COLLECTION_KEY + " = ( SELECT " + COLLECTION_KEY + " FROM " 
                     + COLLECTION_TABLE + " WHERE " + COLLECTION_ID + " = ? )");
         }
         
         if(model.getContributorId() != null) {
             nextArgument(res);
             res.append(AUDITTRAIL_TABLE + "." + AUDITTRAIL_CONTRIBUTOR_KEY + " = ( SELECT " + CONTRIBUTOR_KEY 
                     + " FROM " + CONTRIBUTOR_TABLE + " WHERE " + CONTRIBUTOR_ID + " = ? )");
         }
         
         if(model.getMinSeqNumber() != null) {
             nextArgument(res);
             res.append(AUDITTRAIL_TABLE + "." + AUDITTRAIL_SEQUENCE_NUMBER + " >= ?");
         }
         
         if(model.getMaxSeqNumber() != null) {
             nextArgument(res);
             res.append(AUDITTRAIL_TABLE + "." + AUDITTRAIL_SEQUENCE_NUMBER + " <= ?");
         }
         
         if(model.getActorName() != null) {
             nextArgument(res);
             res.append(AUDITTRAIL_TABLE + "." + AUDITTRAIL_ACTOR_KEY + " = ( SELECT " + ACTOR_KEY + " FROM " 
                     + ACTOR_TABLE + " WHERE " + ACTOR_NAME + " = ? )");
         }
         
         if(model.getOperation() != null) {
             nextArgument(res);
             res.append(AUDITTRAIL_TABLE + "." + AUDITTRAIL_OPERATION + " = ?");
         }
         
         if(model.getStartDate() != null) {
             nextArgument(res);
             res.append(AUDITTRAIL_TABLE + "." + AUDITTRAIL_OPERATION_DATE + " >= ?");
         }
         
         if(model.getEndDate() != null) {
             nextArgument(res);
             res.append(AUDITTRAIL_TABLE + "." + AUDITTRAIL_OPERATION_DATE + " <= ?");
         }
         
         return res.toString();
     }
     
     /**
      * Adds either ' AND ' or 'WHERE ' depending on whether it is the first restriction.
      * @param res The StringBuilder where the restrictions are combined.
      */
     private void nextArgument(StringBuilder res) {
         if(res.length() > 0) {
             res.append(" AND ");
         } else {
             res.append(" WHERE ");
         }            
     }
     
     /**
      * @return The list of elements in the model which are not null.
      */
     private Object[] extractArgumentsFromModel() {
         List<Object> res = new ArrayList<Object>();
         
         if(model.getFileId() != null) {
             res.add(model.getFileId());
         }
         
         if(model.getCollectionId() != null) {
             res.add(model.getCollectionId());
         }
         
         if(model.getContributorId() != null) {
             res.add(model.getContributorId());
         }
         
         if(model.getMinSeqNumber() != null) {
             res.add(model.getMinSeqNumber());
         }
         
         if(model.getMaxSeqNumber() != null) {
             res.add(model.getMaxSeqNumber());
         }
         
         if(model.getActorName() != null) {
             res.add(model.getActorName());
         }
         
         if(model.getOperation() != null) {
             res.add(model.getOperation().toString());
         }
         
         if(model.getStartDate() != null) {
             res.add(model.getStartDate());
         }
         
         if(model.getEndDate() != null) {
             res.add(model.getEndDate());
         }
         
         return res.toArray();
     }
     
     /**
      * Retrieves a id of a contributor based on the guid. 
      * @param contributorGuid The guid of the contributor.
      * @return The id of the contributor corresponding to guid.
      */
     private String retrieveContributorId(long contributorGuid) {
         String sqlRetrieve = "SELECT " + CONTRIBUTOR_ID + " FROM " + CONTRIBUTOR_TABLE + " WHERE " + CONTRIBUTOR_KEY 
                 + " = ?";
         
         return DatabaseUtils.selectStringValue(dbConnector, sqlRetrieve, contributorGuid);        
     }
     
     /**
      * Retrieves a id of a file based on the guid. 
      * @param fileGuid The guid of the file.
      * @return The id of the file corresponding to guid.
      */
     private String retrieveFileId(long fileGuid) {
         String sqlRetrieve = "SELECT " + FILE_FILEID + " FROM " + FILE_TABLE + " WHERE " + FILE_KEY + " = ?";
         
         return DatabaseUtils.selectStringValue(dbConnector, sqlRetrieve, fileGuid);        
     }
     
     /**
      * Retrieves a name of an actor based on the guid. 
      * @param actorGuid The guid of the actor.
      * @return The name of the actor corresponding to guid.
      */
     private String retrieveActorName(long actorGuid) {
         String sqlRetrieve = "SELECT " + ACTOR_NAME + " FROM " + ACTOR_TABLE + " WHERE " + ACTOR_KEY + " = ?";
         
         return DatabaseUtils.selectStringValue(dbConnector, sqlRetrieve, actorGuid);        
     }
 }
