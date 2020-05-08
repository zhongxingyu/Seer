 package org.bahmni.feed.openerp.job;
 
 
 import org.ict4h.atomfeed.jdbc.JdbcConnectionProvider;
 import org.ict4h.atomfeed.server.repository.AllEventRecords;
 import org.ict4h.atomfeed.server.repository.AllEventRecordsOffsetMarkers;
 import org.ict4h.atomfeed.server.repository.ChunkingEntries;
 import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsJdbcImpl;
 import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsOffsetMarkersJdbcImpl;
 import org.ict4h.atomfeed.server.repository.jdbc.ChunkingEntriesJdbcImpl;
 import org.ict4h.atomfeed.server.service.NumberOffsetMarkerServiceImpl;
 import org.ict4h.atomfeed.server.service.OffsetMarkerService;
 
import java.sql.Connection;
import java.sql.SQLException;

 public class OpenERPEventsOptimizerJob {
     private static final int OFFSET_BY_NUMBER_OF_RECORDS_PER_CATEGORY = 1000;
     private JdbcConnectionProvider connectionProvider;
 
     public OpenERPEventsOptimizerJob(JdbcConnectionProvider connectionProvider) {
         this.connectionProvider = connectionProvider;
     }
 
     public void execute() {
        connectionProvider.startTransaction();
        try {
            AllEventRecords allEventRecords = new AllEventRecordsJdbcImpl(connectionProvider);
            AllEventRecordsOffsetMarkers eventRecordsOffsetMarkers = new AllEventRecordsOffsetMarkersJdbcImpl(connectionProvider);
            ChunkingEntries chunkingEntries = new ChunkingEntriesJdbcImpl(connectionProvider);
            OffsetMarkerService markerService = new NumberOffsetMarkerServiceImpl(allEventRecords, chunkingEntries, eventRecordsOffsetMarkers);
            markerService.markEvents(OFFSET_BY_NUMBER_OF_RECORDS_PER_CATEGORY);
            connectionProvider.commit();
        } catch (Exception e) {
            connectionProvider.rollback();
        } finally {
            try {
                Connection connection = connectionProvider.getConnection();
                if (connection != null && !connection.isClosed()) {
                    connectionProvider.closeConnection(connection);
                }
            } catch (SQLException e) {
                throw  new RuntimeException("Couldn't close the jdbc connection",e);
            }
        }

     }
 
 }
