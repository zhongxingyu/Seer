 package org.motechproject.care.reporting.migration.task;
 
 import com.google.gson.JsonArray;
 import org.motechproject.care.reporting.migration.MigratorArguments;
 import org.motechproject.care.reporting.migration.common.CommcareResponseWrapper;
 import org.motechproject.care.reporting.migration.common.MigrationType;
 import org.motechproject.care.reporting.migration.common.Page;
 import org.motechproject.care.reporting.migration.common.PaginatedResponse;
 import org.motechproject.care.reporting.migration.common.ResponseParser;
 import org.motechproject.care.reporting.migration.service.PaginationScheme;
 import org.motechproject.care.reporting.migration.service.Paginator;
 import org.motechproject.care.reporting.migration.statistics.MigrationStatisticsCollector;
 import org.motechproject.care.reporting.migration.util.CommcareAPIHttpClient;
 import org.motechproject.care.reporting.migration.util.MotechAPIHttpClient;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public abstract class MigrationTask {
 
     private static final Logger logger = LoggerFactory.getLogger(CaseMigrationTask.class);
     private static final Logger progressLogger = LoggerFactory.getLogger("migration-progress-logger");
 
     protected final CommcareAPIHttpClient commcareAPIHttpClient;
     protected final MotechAPIHttpClient motechAPIHttpClient;
     private ResponseParser responseParser;
     private MigrationType migrationType;
     private MigrationStatisticsCollector statisticsCollector;
 
     public MigrationTask(CommcareAPIHttpClient commcareAPIHttpClient, MotechAPIHttpClient motechAPIHttpClient,
                          ResponseParser responseParser, MigrationType migrationType,
                          MigrationStatisticsCollector statisticsCollector) {
         this.commcareAPIHttpClient = commcareAPIHttpClient;
         this.motechAPIHttpClient = motechAPIHttpClient;
         this.responseParser = responseParser;
         this.migrationType = migrationType;
         this.statisticsCollector = statisticsCollector;
     }
 
     public void migrate(MigratorArguments migratorArguments) {
        progressLogger.info("Starting new migration");
         Map<String, String> pairs = getNameValuePair(migratorArguments);
         Paginator paginator = getPaginator(pairs);
         PaginatedResponse paginatedResponse;
         while ((paginatedResponse = paginator.nextPage()) != null) {
             JsonArray response = paginatedResponse.getRecords();
             statisticsCollector.addRecordsDownloaded(response.size());
 
             logger.info(String.format("Response Meta:: %s", paginatedResponse.getMeta()));
             logger.info(String.format("Records Count: %s", response.size()));
 
             postToMotech(response);
         }
     }
 
     private void postToMotech(JsonArray request) {
         List<CommcareResponseWrapper> commcareResponseWrappers = convertToEntity(request);
         int totalCount = commcareResponseWrappers.size();
         String log = String.format("Started posting %d %s request(s) to motech", totalCount, migrationType);
         logger.info(log);
         progressLogger.info(log);
         int successCount = 0;
         try {
             for (CommcareResponseWrapper commcareResponseWrapper : commcareResponseWrappers) {
                 postToMotech(commcareResponseWrapper);
                 successCount++;
             }
         } finally {
             statisticsCollector.addRecordsUploaded(successCount);
 
             if(successCount != totalCount) {
                 log = String.format("Error posting %s request(s) to motech. Successful: %s, Failed: %s", migrationType, successCount, totalCount - successCount);
                 logger.error(log);
                 progressLogger.error(log);
             } else {
                 log = String.format("Successfully posted %d %s request(s) to motech", totalCount, migrationType);
                 logger.info(log);
                 progressLogger.info(log);
             }
         }
     }
 
 
     private Map<String,String> getNameValuePair(MigratorArguments migratorArguments) {
         Map<String, String> optionsToUrlMapper = getOptionsToUrlMapper();
 
         Map<String,String> pairs = new HashMap<>();
         for (Map.Entry<String, Object> entry : migratorArguments.getOptions().entrySet()) {
             String optionKey = entry.getKey();
 
             if (optionsToUrlMapper.containsKey(optionKey)) {
                 pairs.put(optionsToUrlMapper.get(optionKey), entry.getValue().toString());
             }
             else
                 pairs.put(optionKey,entry.getValue().toString());
         }
         return pairs;
     }
 
     protected Paginator getPaginator(Map<String, String> pairs) {
         PaginationScheme paginationScheme = new PaginationScheme() {
             @Override
             public String nextPage(Map<String, String> parameters, Page paginationOption) {
                 String log = String.format("Fetching %s records from commcare with offset: %s, limit: %s", migrationType, paginationOption.getOffset(), paginationOption.getLimit());
                 progressLogger.info(log);
                 logger.info(log);
                 return fetchCommcareRecords(parameters, paginationOption);
             }
         };
 
         return new Paginator(pairs, paginationScheme, responseParser);
     }
 
     protected abstract Map<String, String> getOptionsToUrlMapper();
 
     protected abstract List<CommcareResponseWrapper> convertToEntity(JsonArray request);
 
     protected abstract void postToMotech(CommcareResponseWrapper commcareResponseWrapper);
 
     protected abstract String fetchCommcareRecords(Map<String, String> parameters, Page paginationOption);
 }
