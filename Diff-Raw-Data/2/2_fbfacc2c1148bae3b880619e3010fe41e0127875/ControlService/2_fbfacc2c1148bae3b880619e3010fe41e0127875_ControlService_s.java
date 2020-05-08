 package nmd.rss.collector.controller;
 
 import com.google.appengine.api.datastore.Transaction;
 import nmd.rss.collector.Transactions;
 import nmd.rss.collector.error.ServiceException;
 import nmd.rss.collector.feed.*;
 import nmd.rss.collector.scheduler.FeedUpdateTask;
 import nmd.rss.collector.scheduler.FeedUpdateTaskRepository;
 import nmd.rss.collector.scheduler.FeedUpdateTaskScheduler;
 import nmd.rss.collector.scheduler.FeedUpdateTaskSchedulerContextRepository;
 import nmd.rss.collector.updater.FeedHeadersRepository;
 import nmd.rss.collector.updater.FeedItemsRepository;
 import nmd.rss.collector.updater.UrlFetcher;
 import nmd.rss.collector.updater.UrlFetcherException;
 import nmd.rss.reader.FeedItemsComparisonReport;
 import nmd.rss.reader.ReadFeedItems;
 import nmd.rss.reader.ReadFeedItemsRepository;
 
 import java.util.*;
 
 import static nmd.rss.collector.controller.FeedItemReport.asNotRead;
 import static nmd.rss.collector.controller.FeedItemReport.asRead;
 import static nmd.rss.collector.error.ServiceError.*;
 import static nmd.rss.collector.feed.TimestampDescendingComparator.TIMESTAMP_DESCENDING_COMPARATOR;
 import static nmd.rss.collector.util.Assert.assertNotNull;
 import static nmd.rss.collector.util.Assert.assertStringIsValid;
 import static nmd.rss.collector.util.TransactionTools.rollbackIfActive;
 import static nmd.rss.collector.util.UrlTools.normalizeUrl;
 import static nmd.rss.reader.FeedItemsComparator.compare;
 
 /**
  * Author : Igor Usenko ( igors48@gmail.com )
  * Date : 22.05.13
  */
 public class ControlService {
     //TODO consider split this service on two parts. for collector and reader
 
     private static final int MAX_FEED_ITEMS_COUNT = 300;
 
     private final Transactions transactions;
 
     private final FeedHeadersRepository feedHeadersRepository;
     private final FeedItemsRepository feedItemsRepository;
     private final FeedUpdateTaskRepository feedUpdateTaskRepository;
     private final ReadFeedItemsRepository readFeedItemsRepository;
     private final FeedUpdateTaskSchedulerContextRepository feedUpdateTaskSchedulerContextRepository;
 
     private final FeedUpdateTaskScheduler scheduler;
     private final UrlFetcher fetcher;
 
     public ControlService(final FeedHeadersRepository feedHeadersRepository, final FeedItemsRepository feedItemsRepository, final FeedUpdateTaskRepository feedUpdateTaskRepository, final ReadFeedItemsRepository readFeedItemsRepository, final FeedUpdateTaskSchedulerContextRepository feedUpdateTaskSchedulerContextRepository, final FeedUpdateTaskScheduler scheduler, final UrlFetcher fetcher, final Transactions transactions) {
         assertNotNull(feedHeadersRepository);
         this.feedHeadersRepository = feedHeadersRepository;
 
         assertNotNull(feedItemsRepository);
         this.feedItemsRepository = feedItemsRepository;
 
         assertNotNull(feedUpdateTaskRepository);
         this.feedUpdateTaskRepository = feedUpdateTaskRepository;
 
         assertNotNull(readFeedItemsRepository);
         this.readFeedItemsRepository = readFeedItemsRepository;
 
         assertNotNull(feedUpdateTaskSchedulerContextRepository);
         this.feedUpdateTaskSchedulerContextRepository = feedUpdateTaskSchedulerContextRepository;
 
         assertNotNull(scheduler);
         this.scheduler = scheduler;
 
         assertNotNull(fetcher);
         this.fetcher = fetcher;
 
         assertNotNull(transactions);
         this.transactions = transactions;
     }
 
     public UUID addFeed(final String feedUrl) throws ServiceException {
         assertStringIsValid(feedUrl);
 
         Transaction transaction = null;
 
         final String feedUrlInLowerCase = normalizeUrl(feedUrl);
         final Feed feed = fetchFeed(feedUrlInLowerCase);
 
         try {
             transaction = this.transactions.beginOne();
 
             FeedHeader feedHeader = this.feedHeadersRepository.loadHeader(feedUrlInLowerCase);
 
             if (feedHeader == null) {
                 feedHeader = feed.header;
                 this.feedHeadersRepository.storeHeader(feedHeader);
             }
 
             final List<FeedItem> olds = getFeedOldItems(feedHeader);
 
             createFeedUpdateTask(feedHeader);
 
             final FeedItemsMergeReport mergeReport = FeedItemsMerger.merge(olds, feed.items, MAX_FEED_ITEMS_COUNT);
             this.feedItemsRepository.mergeItems(feedHeader.id, mergeReport);
 
             transaction.commit();
 
             return feedHeader.id;
         } finally {
             rollbackIfActive(transaction);
         }
     }
 
     public void removeFeed(final UUID feedId) {
         assertNotNull(feedId);
 
         Transaction transaction = null;
 
         try {
             transaction = this.transactions.beginOne();
 
             this.feedUpdateTaskRepository.deleteTaskForFeedId(feedId);
             this.feedHeadersRepository.deleteHeader(feedId);
             this.feedItemsRepository.deleteItems(feedId);
             this.readFeedItemsRepository.delete(feedId);
 
             transaction.commit();
         } finally {
             rollbackIfActive(transaction);
         }
     }
 
     public List<FeedHeader> getFeedHeaders() {
         Transaction transaction = null;
 
         try {
             transaction = this.transactions.beginOne();
 
             final List<FeedHeader> headers = this.feedHeadersRepository.loadHeaders();
 
             transaction.commit();
 
             return headers;
         } finally {
             rollbackIfActive(transaction);
         }
     }
 
     public Feed getFeed(final UUID feedId) throws ServiceException {
         assertNotNull(feedId);
 
         Transaction transaction = null;
 
         try {
             transaction = this.transactions.beginOne();
 
             final FeedHeader header = loadFeedHeader(feedId);
 
             List<FeedItem> items = this.feedItemsRepository.loadItems(feedId);
             items = items == null ? new ArrayList<FeedItem>() : items;
 
             transaction.commit();
 
             return new Feed(header, items);
         } finally {
             rollbackIfActive(transaction);
         }
     }
 
     public FeedUpdateReport updateFeed(final UUID feedId) throws ServiceException {
         assertNotNull(feedId);
 
         Transaction getFeedHeaderAndTaskTransaction = null;
 
         final FeedHeader header;
         final FeedUpdateTask updateTask;
 
         try {
             getFeedHeaderAndTaskTransaction = this.transactions.beginOne();
 
             header = loadFeedHeader(feedId);
 
             updateTask = this.feedUpdateTaskRepository.loadTaskForFeedId(feedId);
 
             if (updateTask == null) {
                 throw new ServiceException(wrongFeedTaskId(feedId));
             }
 
             getFeedHeaderAndTaskTransaction.commit();
         } finally {
             rollbackIfActive(getFeedHeaderAndTaskTransaction);
         }
 
         final Feed feed = fetchFeed(header.feedLink);
 
         Transaction updateFeedTransaction = null;
 
         try {
             updateFeedTransaction = this.transactions.beginOne();
 
             List<FeedItem> olds = getFeedOldItems(header);
 
             final FeedItemsMergeReport mergeReport = FeedItemsMerger.merge(olds, feed.items, updateTask.maxFeedItemsCount);
             this.feedItemsRepository.mergeItems(header.id, mergeReport);
 
             updateFeedTransaction.commit();
 
             return new FeedUpdateReport(header.feedLink, feedId, mergeReport);
         } finally {
             rollbackIfActive(updateFeedTransaction);
         }
     }
 
     public FeedUpdateReport updateCurrentFeed() throws ServiceException {
         final FeedUpdateTask currentTask = this.scheduler.getCurrentTask();
 
         if (currentTask == null) {
             throw new ServiceException(noScheduledTask());
         }
 
         return updateFeed(currentTask.feedId);
     }
 
     public List<FeedReadReport> getFeedsReadReport() {
         Transaction transaction = null;
 
         try {
             transaction = this.transactions.beginOne();
 
             final List<FeedHeader> headers = this.feedHeadersRepository.loadHeaders();
             final List<FeedReadReport> report = new ArrayList<>();
 
             for (final FeedHeader header : headers) {
                 final List<FeedItem> items = this.feedItemsRepository.loadItems(header.id);
 
                 final Set<String> storedGuids = getStoredGuids(items);
                 final ReadFeedItems readFeedItems = this.readFeedItemsRepository.load(header.id);
 
                 final FeedItemsComparisonReport comparisonReport = compare(readFeedItems.itemIds, storedGuids);
 
                 final FeedItem topItem = findLastNotReadFeedItem(items, readFeedItems.itemIds);
                 final String topItemId = topItem == null ? null : topItem.guid;
                 final String topItemLink = topItem == null ? null : topItem.link;
 
                 final int addedFromLastVisit = countYoungerItems(items, readFeedItems.lastUpdate);
                 final FeedReadReport feedReadReport = new FeedReadReport(header.id, header.title, comparisonReport.readItems.size(), comparisonReport.newItems.size(), addedFromLastVisit, topItemId, topItemLink);
 
                 report.add(feedReadReport);
             }
 
             transaction.commit();
 
             return report;
         } finally {
             rollbackIfActive(transaction);
         }
     }
 
     public FeedItemsReport getFeedItemsReport(final UUID feedId) throws ServiceException {
         assertNotNull(feedId);
 
         Transaction transaction = null;
 
         try {
             transaction = this.transactions.beginOne();
 
             final FeedHeader header = loadFeedHeader(feedId);
 
             final ArrayList<FeedItemReport> feedItemReports = new ArrayList<>();
 
             final List<FeedItem> feedItems = this.feedItemsRepository.loadItems(feedId);
             Collections.sort(feedItems, TIMESTAMP_DESCENDING_COMPARATOR);
 
             final ReadFeedItems readFeedItems = this.readFeedItemsRepository.load(feedId);
 
             int read = 0;
             int notRead = 0;
 
             for (final FeedItem feedItem : feedItems) {
                 final boolean readItem = readFeedItems.itemIds.contains(feedItem.guid);
 
                 feedItemReports.add(readItem ? asRead(feedId, feedItem) : asNotRead(feedId, feedItem));
 
                 if (readItem) {
                     ++read;
                 } else {
                     ++notRead;
                 }
             }
 
             return new FeedItemsReport(header.title, read, notRead, feedItemReports);
         } finally {
             rollbackIfActive(transaction);
         }
     }
 
     public void markItemAsRead(final UUID feedId, final String itemId) throws ServiceException {
         assertNotNull(feedId);
         assertStringIsValid(itemId);
 
         Transaction transaction = null;
 
         try {
             transaction = this.transactions.beginOne();
 
             loadFeedHeader(feedId);
 
             final Set<String> storedGuids = getStoredGuids(feedId);
             final ReadFeedItems readFeedItems = this.readFeedItemsRepository.load(feedId);
 
             final Set<String> readGuids = new HashSet<>();
             readGuids.addAll(readFeedItems.itemIds);
             readGuids.add(itemId);
 
             final FeedItemsComparisonReport comparisonReport = compare(readGuids, storedGuids);
 
             final ReadFeedItems updatedReadFeedItems = new ReadFeedItems(new Date(), comparisonReport.readItems);
             this.readFeedItemsRepository.store(feedId, updatedReadFeedItems);
 
             transaction.commit();
         } finally {
             rollbackIfActive(transaction);
         }
     }
 
     public void clear() {
 
         Transaction transaction = null;
 
         try {
             transaction = this.transactions.beginOne();
 
             final List<FeedHeader> headers = this.feedHeadersRepository.loadHeaders();
             final List<FeedHeader> backup = new ArrayList<>(headers);
 
             for (final FeedHeader header : backup) {
                 removeFeed(header.id);
             }
 
             this.feedUpdateTaskSchedulerContextRepository.clear();
 
             transaction.commit();
         } finally {
             rollbackIfActive(transaction);
         }
     }
 
     private FeedHeader loadFeedHeader(final UUID feedId) throws ServiceException {
         FeedHeader header = this.feedHeadersRepository.loadHeader(feedId);
 
         if (header == null) {
             throw new ServiceException(wrongFeedId(feedId));
         }
 
         return header;
     }
 
     private Set<String> getStoredGuids(final List<FeedItem> items) {
         final Set<String> storedGuids = new HashSet<>();
 
         for (final FeedItem item : items) {
             storedGuids.add(item.guid);
         }
 
         return storedGuids;
     }
 
     private Set<String> getStoredGuids(final UUID feedId) {
         final List<FeedItem> items = this.feedItemsRepository.loadItems(feedId);
 
         return getStoredGuids(items);
     }
 
     private void createFeedUpdateTask(final FeedHeader feedHeader) {
         FeedUpdateTask feedUpdateTask = this.feedUpdateTaskRepository.loadTaskForFeedId(feedHeader.id);
 
         if (feedUpdateTask == null) {
             feedUpdateTask = new FeedUpdateTask(feedHeader.id, MAX_FEED_ITEMS_COUNT);
             this.feedUpdateTaskRepository.storeTask(feedUpdateTask);
         }
     }
 
     private List<FeedItem> getFeedOldItems(final FeedHeader feedHeader) {
         final List<FeedItem> feedItems = feedHeader == null ? new ArrayList<FeedItem>() : this.feedItemsRepository.loadItems(feedHeader.id);
 
         return feedItems == null ? new ArrayList<FeedItem>() : feedItems;
     }
 
     private Feed fetchFeed(final String feedUrl) throws ServiceException {
 
         try {
             final String data = this.fetcher.fetch(feedUrl);
 
             return FeedParser.parse(feedUrl, data);
         } catch (final UrlFetcherException exception) {
             throw new ServiceException(urlFetcherError(feedUrl), exception);
         } catch (FeedParserException exception) {
             throw new ServiceException(feedParseError(feedUrl), exception);
         }
     }
 
     public static FeedItem findLastNotReadFeedItem(final List<FeedItem> items, final Set<String> readGuids) {
         assertNotNull(items);
         assertNotNull(readGuids);
 
         Collections.sort(items, TIMESTAMP_DESCENDING_COMPARATOR);
 
         for (final FeedItem candidate : items) {
 
             if (!readGuids.contains(candidate.guid)) {
                 return candidate;
             }
         }
 
         return null;
     }
 
     private static int countYoungerItems(final List<FeedItem> items, final Date lastUpdate) {
         int count = 0;
 
         for (final FeedItem item : items) {
 
             if (item.date.compareTo(lastUpdate) > 0) {
                 ++count;
             }
         }
 
         return count;
     }
 
 }
