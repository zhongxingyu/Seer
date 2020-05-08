 package wikipedia.http;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.joda.time.DateMidnight;
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import wikipedia.analysis.pagenetwork.CategoryLists;
 import wikipedia.database.DBUtil;
 import wikipedia.network.PageLinkInfo;
 
 import com.google.common.collect.Lists;
 
 /**
  * Downloads all link revisions of a given page
  */
 public final class PageHistoryFetcher {
 
     public static final int MAX_MONTHS = 18;
     public static final DateMidnight MOST_RECENT_DATE = new DateMidnight(2011, 6, 1);
 
     private static final int THREAD_SLEEP_MSEC = 1200;
     private static final int THREADPOOL_TERMINATION_WAIT_MINUTES = 1;
     private static final int NUM_THREADS = 8;
 
     private static final Logger LOG = LoggerFactory.getLogger(PageHistoryFetcher.class.getName());
 
     private final List<DateTime> allRelevantTimeStamps;
     private final DBUtil dataBaseUtil = new DBUtil();
 
     private final DefaultHttpClient httpClient = new DefaultHttpClient(
             new ThreadSafeClientConnManager());
 
     private final ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);
 
     public PageHistoryFetcher() {
         allRelevantTimeStamps = getAllDatesForHistory(MAX_MONTHS, MOST_RECENT_DATE.toDateTime());
     }
 
     public static List<DateTime> getAllDatesForHistory(final int numberOfRevisions,
                                                        final DateTime mostRecent) {
         List<DateTime> allDatesToFetch = Lists.newArrayList();
         LOG.info("Fetching the following Dates: ");
         for (int months = 0; months < numberOfRevisions; months++) {
             final DateTime fetchDate = mostRecent.toDateTime().minusMonths(months);
             LOG.info(fetchDate.toString());
             allDatesToFetch.add(fetchDate);
         }
         return allDatesToFetch;
     }
 
     public void fetchAllRecords(final int pageId,
                                 final String pageTitle,
                                 final String lang) {
         // get oldest revision of article, if it didnt exist yet, do not execute
         // http request!
         String storedCreationDate = dataBaseUtil.getFirstRevisionDate(pageId);
         DateTime firstRevisionDate;
         if (StringUtils.isEmpty(storedCreationDate)) {
             final WikiAPIClient wikiAPIClient = new WikiAPIClient(httpClient);
             try {
                 firstRevisionDate = new FirstRevisionFetcher(pageTitle, lang, wikiAPIClient)
                         .getFirstRevisionDate();
             } catch (Exception e) {
                 LOG.error("Error while fetching first revision date for: " + pageTitle);
                 return;
             }
         } else {
             firstRevisionDate = DBUtil.MYSQL_DATETIME_FORMATTER.parseDateTime(StringUtils.removeEnd(
                     storedCreationDate, ".0"));
         }
 
         final WikiAPIClient wikiAPIClient = new WikiAPIClient(httpClient);
         for (DateTime dateToFetch : allRelevantTimeStamps) {
             downloadLinkInfoIfNecessary(pageId, pageTitle, lang, firstRevisionDate, wikiAPIClient,
                     dateToFetch);
         }
     }
 
     private void downloadLinkInfoIfNecessary(final int pageId,
                                              final String pageTitle,
                                              final String lang,
                                              final DateTime firstRevisionDate,
                                              final WikiAPIClient wikiAPIClient,
                                              final DateTime dateToFetch) {
         if (dateToFetch.isAfter(firstRevisionDate.plusWeeks(1))) { // ignore
                                                                    // first week
                                                                    // of
                                                                    // article,
                                                                    // not stable
                                                                    // yet
             if (dataBaseUtil.localDataForRecordUnavailable(pageId, dateToFetch)) {
                 try {
                     Thread.sleep(THREAD_SLEEP_MSEC); // with a poolsize of 8,
                                                      // this should lead to ~7
                                                      // request pro second
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
                 PageLinkInfoFetcher plif = new PageLinkInfoFetcher(pageTitle, lang, dateToFetch,
                         wikiAPIClient);
                 PageLinkInfo linkInformation;
                 try {
                     linkInformation = plif.getLinkInformation();
                     dataBaseUtil.storePageLinkInfo(linkInformation, firstRevisionDate);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
 
             }
         }
     }
 
     private void shutdownThreadPool() {
         threadPool.shutdown();
         try {
             threadPool.awaitTermination(THREADPOOL_TERMINATION_WAIT_MINUTES, TimeUnit.MINUTES);
         } catch (InterruptedException e) {
             LOG.error("Error while shutting down Threadpool", e);
         }
         while (!threadPool.isTerminated()) {
             LOG.debug("Waiting for Thread-Pool termination");
         }
     }
 
     /**
      *
      */
     public static void main(final String[] args) {
         final String lang = "en";
         new PageHistoryFetcher().fetchCompleteCategories(lang);
     }
 
     private void fetchCompleteCategories(final String lang) {
         final Map<Integer, String> allPagesInAllCategories = new CategoryMemberFetcher(
                 CategoryLists.ENGLISH_MUSIC, lang, dataBaseUtil).getAllPagesInAllCategories();
         LOG.info("Total Number of Tasks: " + allPagesInAllCategories.size());
         int counter = 1;
         try {
             for (final Entry<Integer, String> pageEntry : allPagesInAllCategories.entrySet()) {
                 threadPool.execute(new ExecutorTask(this, lang, pageEntry, counter++));
             }
         } finally {
             shutdownThreadPool();
             httpClient.getConnectionManager().shutdown();
         }
     }
 
     /**
      * Runner for the download task of a pages records
      */
     private static final class ExecutorTask implements Runnable {
         private static final int MODULO_LOG = 100;
         private final PageHistoryFetcher pageHistoryFetcher;
         private final String lang;
         private final Entry<Integer, String> pageEntry;
         private final int taskCounter;
 
         private ExecutorTask(final PageHistoryFetcher pageHistoryFetcher, final String lang,
                 final Entry<Integer, String> pageEntry, final int taskCounter) {
             this.pageHistoryFetcher = pageHistoryFetcher;
             this.lang = lang;
             this.pageEntry = pageEntry;
             this.taskCounter = taskCounter;
         }
 
         @Override
         public void run() {
             if (taskCounter % MODULO_LOG == 0) {
                 LOG.info("Starting Thread for Page: " + pageEntry.getValue() + " (Task Number: "
                         + taskCounter + ")");
             }
             pageHistoryFetcher.fetchAllRecords(pageEntry.getKey(), pageEntry.getValue(), lang);
         }
     }
 
 }
