 package wikipedia.database;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.xml.XmlBeanFactory;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.dao.DataIntegrityViolationException;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 import org.springframework.jdbc.datasource.DataSourceTransactionManager;
 import org.springframework.transaction.TransactionStatus;
 import org.springframework.transaction.support.TransactionCallbackWithoutResult;
 import org.springframework.transaction.support.TransactionTemplate;
 
 import wikipedia.analysis.drilldown.NumberOfRecentEditsFetcher;
 import wikipedia.http.FirstRevisionFetcher;
 import wikipedia.http.WikiAPIClient;
 import wikipedia.network.PageLinkInfo;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 /**
  * Util for all DB read/write ops
  */
 public final class DBUtil {
 
     private static final int SLEEP_BETWEEN_REQUESTS = 400;
     private static final int MAX_TITLE_LENGTH = 256;
     public static final String MYSQL_DATETIME = "YYYY-MM-dd HH:mm:ss";
     public static final DateTimeFormatter MYSQL_DATETIME_FORMATTER = DateTimeFormat
             .forPattern(MYSQL_DATETIME);
 
     private static final Logger LOG = LoggerFactory.getLogger(DBUtil.class.getName());
 
     private final SimpleJdbcTemplate jdbcTemplate;
     private final TransactionTemplate transactionTemplate;
 
     public DBUtil() {
         XmlBeanFactory beanFactory = new XmlBeanFactory(new ClassPathResource("context.xml"));
         final BasicDataSource bean = (BasicDataSource) beanFactory.getBean("dataSource");
         jdbcTemplate = new SimpleJdbcTemplate(bean);
         DataSourceTransactionManager dstm = new DataSourceTransactionManager(
                 bean);
         transactionTemplate = new TransactionTemplate(dstm);
     }
 
     public void storePageLinkInfo(final PageLinkInfo pliToBeStored,
                                   final DateTime firstRevisionDate) {
         // if page entry already there, only store revision
         final int numberOfPageEntries = jdbcTemplate.queryForInt(
                 "SELECT count(0) FROM pages WHERE page_id = ?",
                 new Object[] {pliToBeStored.getPageID() });
         final String timeStamp = pliToBeStored.getTimeStamp().toString(MYSQL_DATETIME_FORMATTER);
         final String firstRevisionDateTime = firstRevisionDate.toString(MYSQL_DATETIME_FORMATTER);
 
         if (numberOfPageEntries == 0) {
             jdbcTemplate.update("INSERT INTO pages (page_id, page_title, creation_date) "
                     + "VALUES (?, ?, ?)",
                     new Object[] {pliToBeStored.getPageID(), pliToBeStored.getPageTitle(),
                             firstRevisionDateTime });
         }
         storeAllOutGoingLinksInTransaction(pliToBeStored, timeStamp);
     }
 
     private void storeAllOutGoingLinksInTransaction(final PageLinkInfo pliToBeStored,
                                                     final String timeStamp) {
         transactionTemplate.execute(new TransactionCallbackWithoutResult() {
             @Override
             protected void doInTransactionWithoutResult(final TransactionStatus status) {
                 for (String outgoingLink : pliToBeStored.getLinks()) {
                     try {
                         getStoredLink(pliToBeStored, timeStamp, outgoingLink);
                     } catch (EmptyResultDataAccessException e) {
                         storeLink(pliToBeStored, timeStamp, outgoingLink);
                     }
                 }
             }
 
             private void storeLink(final PageLinkInfo pliToBeStored,
                                    final String timeStamp,
                                    final String outgoingLink) {
                 if (outgoingLink.length() < MAX_TITLE_LENGTH) {
                     jdbcTemplate.update(
                             "INSERT INTO outgoing_links "
                                     + "(src_page_id, target_page_title, revision_date) "
                                     + "VALUES (?, ?, ?)", new Object[] {pliToBeStored.getPageID(),
                                     outgoingLink, timeStamp });
                 }
             }
 
             private void getStoredLink(final PageLinkInfo pliToBeStored,
                                        final String timeStamp,
                                        final String outgoingLink) {
                 jdbcTemplate.queryForInt("SELECT src_page_id FROM outgoing_links "
                         + "WHERE target_page_title = ? AND revision_date = ? "
                         + "AND src_page_id = ?",
                         new Object[] {outgoingLink, timeStamp, pliToBeStored.getPageID() });
             }
 
         });
     }
 
     public String getFirstRevisionDate(final int pageId
     /* final String lang */) {
         try {
             return jdbcTemplate.queryForObject("SELECT creation_date FROM pages WHERE page_id = ?",
                     String.class, new Object[] {pageId });
         } catch (EmptyResultDataAccessException e) {
             return "";
         }
     }
 
     public boolean localDataForRecordUnavailable(final int pageId,
                                                  final DateTime revisionDate) {
         int numRows = jdbcTemplate.queryForInt(
                 "SELECT COUNT(0) FROM outgoing_links WHERE revision_date = ? AND src_page_id = ?",
                 new Object[] {
                         revisionDate.toString(DateTimeFormat.forPattern(DBUtil.MYSQL_DATETIME)),
                         pageId });
         return numRows == 0;
     }
 
     public Collection<String> getAllLinksForRevision(final int pageId,
                                                      final String dateTime) {
         try {
             List<Map<String, Object>> allLinksString = jdbcTemplate.queryForList(
                     "SELECT target_page_title FROM outgoing_links "
                             + "WHERE src_page_id = ? AND revision_date = ?", pageId, dateTime);
             return Collections2.transform(allLinksString,
                     new Function<Map<String, Object>, String>() {
                         @Override
                         public String apply(final Map<String, Object> input) {
                             return input.get("target_page_title").toString();
                         }
                     });
         } catch (EmptyResultDataAccessException e) {
             LOG.info("NO LINKS! -- PageID : " + pageId + " -- Date: " + dateTime);
             return Lists.newArrayList();
         }
     }
 
     public void storeAllCategoryMemberPages(final String categoryName,
                                             final String lang,
                                             final Map<Integer, String> allPageTitles) {
         int categoryID = getCategoryID(categoryName);
 
         for (Entry<Integer, String> entry : allPageTitles.entrySet()) {
             // make sure page entry exists!
             final Integer pageId = entry.getKey();
             final String pageTitle = entry.getValue();
             int pageSearchResults = jdbcTemplate.queryForInt(
                     "SELECT COUNT(0) FROM pages WHERE page_id = ?", new Object[] {pageId });
             if (pageSearchResults == 0) {
                 try {
                     Thread.sleep(SLEEP_BETWEEN_REQUESTS); // reduce serverload
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
                 storePageEntry(lang, pageId, pageTitle);
             }
 
             try {
                 jdbcTemplate.update(
                         "INSERT INTO pages_in_categories (page_id, category_id) VALUES (?, ?)",
                         new Object[] {pageId, categoryID });
             } catch (DataIntegrityViolationException e) {
                 e.printStackTrace();
                 LOG.error("IGNORING ARTICLE: " + entry + " (probably too new!)");
             }
         }
     }
 
     private void storePageEntry(final String lang,
                                 final Integer pageId,
                                 final String pageTitle) {
         final FirstRevisionFetcher firstRevisionFetcher = new FirstRevisionFetcher(pageTitle,
                 lang, new WikiAPIClient(new DefaultHttpClient()));
         DateTime firstRevisionDate = firstRevisionFetcher.getFirstRevisionDate();
         String dateString = firstRevisionDate.toString(DateTimeFormat
                 .forPattern(DBUtil.MYSQL_DATETIME));
         jdbcTemplate.update(
                 "INSERT INTO pages (page_id, page_title, creation_date) VALUES (?, ?, ?)",
                 pageId, pageTitle, dateString);
         LOG.info("NEW STORAGE: " + pageTitle);
     }
 
     /**
      * If category does not exist in DB yet, the record will be created
      */
     private int getCategoryID(final String categoryName) {
         int categoryID = -1;
         try {
             categoryID = jdbcTemplate.queryForInt(
                     "SELECT category_id FROM categories WHERE category_name = ?",
                     new Object[] {categoryName });
         } catch (EmptyResultDataAccessException e) {
             jdbcTemplate.update("INSERT INTO categories (category_name) VALUES (?)",
                     new Object[] {categoryName });
         }
         categoryID = jdbcTemplate.queryForInt(
                 "SELECT category_id FROM categories WHERE category_name = ?",
                 new Object[] {categoryName });
         return categoryID;
     }
 
     public boolean categoryMembersInDatabase(final String categoryName) {
         try {
             int categoryID = jdbcTemplate.queryForInt(
                     "SELECT category_id FROM categories WHERE category_name = ?",
                     new Object[] {categoryName });
             return categoryID > 0;
         } catch (EmptyResultDataAccessException e) {
             return false;
         }
     }
 
     public Map<Integer, String> getCategoryMembersByCategoryName(final String categoryName) {
         String sqlStmt = "SELECT pages.page_title, pages.page_id FROM pages_in_categories "
                 + "JOIN pages ON pages.page_id = pages_in_categories.page_id "
                 + "JOIN categories ON categories.category_id = pages_in_categories.category_id "
                 + "WHERE categories.category_name = ?";
         List<Map<String, Object>> sqlResult = jdbcTemplate.queryForList(sqlStmt, categoryName);
         Map<Integer, String> categoryMembers = Maps.newHashMap();
         for (Map<String, Object> resultRow : sqlResult) {
             categoryMembers.put((Integer) resultRow.get("page_id"),
                     (String) resultRow.get("page_title"));
         }
         return categoryMembers;
     }
 
     public int getPageIDFromCache(final String pageTitle, final String lang) {
         try {
             int pageId = jdbcTemplate.queryForInt("SELECT page_id FROM page_id_cache WHERE page_title = ?", new Object[] {pageTitle});
             return pageId;
         } catch (EmptyResultDataAccessException e) {
             NumberOfRecentEditsFetcher fetcher = new NumberOfRecentEditsFetcher(lang);
             int pageID = fetcher.getPageID(pageTitle);
            if(pageID != 0) {
                jdbcTemplate.update("INSERT INTO page_id_cache VALUES (?, ?);", new Object[] {pageID, pageTitle});
            }
             return pageID;
         }
     }
 
 }
