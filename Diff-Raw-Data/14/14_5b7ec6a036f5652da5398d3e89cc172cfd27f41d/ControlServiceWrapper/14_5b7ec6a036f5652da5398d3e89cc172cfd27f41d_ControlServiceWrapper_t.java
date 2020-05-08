 package nmd.rss.collector.rest;
 
 import nmd.rss.collector.Transactions;
 import nmd.rss.collector.controller.*;
 import nmd.rss.collector.error.ServiceError;
 import nmd.rss.collector.exporter.FeedExporterException;
 import nmd.rss.collector.feed.Feed;
 import nmd.rss.collector.feed.FeedHeader;
 import nmd.rss.collector.gae.fetcher.GaeUrlFetcher;
 import nmd.rss.collector.gae.persistence.GaeFeedHeadersRepository;
 import nmd.rss.collector.gae.persistence.GaeFeedItemsRepository;
 import nmd.rss.collector.gae.persistence.GaeFeedUpdateTaskRepository;
 import nmd.rss.collector.gae.persistence.GaeRootRepository;
 import nmd.rss.collector.gae.updater.GaeCacheFeedUpdateTaskSchedulerContextRepository;
 import nmd.rss.collector.rest.responses.*;
 import nmd.rss.collector.scheduler.CycleFeedUpdateTaskScheduler;
 import nmd.rss.collector.scheduler.FeedUpdateTaskRepository;
 import nmd.rss.collector.scheduler.FeedUpdateTaskScheduler;
 import nmd.rss.collector.scheduler.FeedUpdateTaskSchedulerContextRepository;
 import nmd.rss.collector.updater.FeedHeadersRepository;
 import nmd.rss.collector.updater.FeedItemsRepository;
 import nmd.rss.collector.updater.UrlFetcher;
 import nmd.rss.reader.ReadFeedItemsRepository;
 import nmd.rss.reader.gae.GaeReadFeedItemsRepository;
 
 import java.util.List;
 import java.util.UUID;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import static java.lang.String.format;
 import static nmd.rss.collector.exporter.FeedExporter.export;
 import static nmd.rss.collector.rest.ResponseBody.createErrorJsonResponse;
 import static nmd.rss.collector.rest.ResponseBody.createJsonResponse;
 import static nmd.rss.collector.rest.responses.FeedItemsReportResponse.convert;
 import static nmd.rss.collector.rest.responses.SuccessMessageResponse.create;
 
 /**
  * Author : Igor Usenko ( igors48@gmail.com )
  * Date : 22.06.13
  */
 public class ControlServiceWrapper {
 
     private static final Logger LOGGER = Logger.getLogger(ControlServiceWrapper.class.getName());
 
     private static final ControlService CONTROL_SERVICE = createControlService();
 
     public static ResponseBody addFeed(final String feedUrl) {
 
         try {
             final UUID feedId = CONTROL_SERVICE.addFeed(feedUrl);
 
             final FeedIdResponse feedIdResponse = FeedIdResponse.create(feedId);
 
             LOGGER.info(format("Feed [ %s ] added. Id is [ %s ]", feedUrl, feedId));
 
             return createJsonResponse(feedIdResponse);
         } catch (ControlServiceException exception) {
             LOGGER.log(Level.SEVERE, format("Error adding feed [ %s ]", feedUrl), exception);
 
             return createErrorJsonResponse(exception);
         }
     }
 
     public static ResponseBody removeFeed(final UUID feedId) {
         CONTROL_SERVICE.removeFeed(feedId);
 
         final SuccessMessageResponse successMessageResponse = create(format("Feed [ %s ] removed", feedId));
 
         LOGGER.info(format("Feed [ %s ] removed", feedId));
 
         return createJsonResponse(successMessageResponse);
     }
 
     public static ResponseBody getFeedHeaders() {
         final List<FeedHeader> headers = CONTROL_SERVICE.getFeedHeaders();
         final FeedHeadersResponse feedHeadersResponse = FeedHeadersResponse.convert(headers);
 
         LOGGER.info(format("[ %s ] feed headers found", headers.size()));
 
         return createJsonResponse(feedHeadersResponse);
     }
 
     public static ResponseBody getFeed(final UUID feedId) {
 
         try {
             final Feed feed = CONTROL_SERVICE.getFeed(feedId);
             final String feedAsXml = export(feed.header, feed.items);
 
             LOGGER.info(format("Feed [ %s ] link [ %s ] items exported. Items count [ %d ]", feedId, feed.header.feedLink, feed.items.size()));
 
             return new ResponseBody(ContentType.XML, feedAsXml);
         } catch (ControlServiceException exception) {
             LOGGER.log(Level.SEVERE, format("Error export feed [ %s ]", feedId), exception);
 
             return createErrorJsonResponse(exception);
         } catch (FeedExporterException exception) {
             LOGGER.log(Level.SEVERE, format("Error export feed [ %s ]", feedId), exception);
 
             return createErrorJsonResponse(ServiceError.feedExportError(feedId));
         }
     }
 
     public static ResponseBody updateCurrentFeed() {
 
         try {
             final FeedUpdateReport report = CONTROL_SERVICE.updateCurrentFeed();
             final FeedMergeReportResponse response = FeedMergeReportResponse.convert(report);
 
             LOGGER.info(format("Feed with id [ %s ] link [ %s ] updated. Added [ %d ] retained [ %d ] removed [ %d ] items", report.feedId, report.feedLink, report.mergeReport.added.size(), report.mergeReport.retained.size(), report.mergeReport.removed.size()));
 
             return createJsonResponse(response);
         } catch (ControlServiceException exception) {
            final ServiceError serviceError = exception.getError();

            LOGGER.log(Level.SEVERE, format("Error update current feed [ %s ]", serviceError), exception);
 
             return createErrorJsonResponse(exception);
         }
     }
 
     public static ResponseBody updateFeed(final UUID feedId) {
 
         try {
             final FeedUpdateReport report = CONTROL_SERVICE.updateFeed(feedId);
             final FeedMergeReportResponse response = FeedMergeReportResponse.convert(report);
 
             LOGGER.info(format("Feed with id [ %s ] link [ %s ] updated. Added [ %d ] retained [ %d ] removed [ %d ] items", report.feedId, report.feedLink, report.mergeReport.added.size(), report.mergeReport.retained.size(), report.mergeReport.removed.size()));
 
             return createJsonResponse(response);
         } catch (ControlServiceException exception) {
             LOGGER.log(Level.SEVERE, format("Error update feed [ %s ]", feedId), exception);
 
             return createErrorJsonResponse(exception);
         }
     }
 
     public static ResponseBody getFeedsReadReport() {
         final List<FeedReadReport> feedReadReport = CONTROL_SERVICE.getFeedsReadReport();
         final FeedReadReportsResponse response = FeedReadReportsResponse.convert(feedReadReport);
 
         LOGGER.info("Feed read report created");
 
         return createJsonResponse(response);
     }
 
     public static ResponseBody markItemAsRead(final UUID feedId, final String itemId) {
 
         try {
             CONTROL_SERVICE.markItemAsRead(feedId, itemId);
 
             LOGGER.info(format("Item [ %s ] from feed [ %s ] marked as read", itemId, feedId));
 
             final SuccessMessageResponse successMessageResponse = create(format("Item [ %s ] from feed [ %s ] marked as read", itemId, feedId));
 
             return createJsonResponse(successMessageResponse);
         } catch (ControlServiceException exception) {
             LOGGER.log(Level.SEVERE, format("Error update feed [ %s ]", feedId), exception);
 
             return createErrorJsonResponse(exception);
         }
     }
 
     public static ResponseBody getFeedItemsReport(final UUID feedId) {
 
         try {
             FeedItemsReport report = CONTROL_SERVICE.getFeedItemsReport(feedId);
             FeedItemsReportResponse response = convert(report);
 
             LOGGER.info(format("Feed [ %s ] items report created", feedId));
 
             return createJsonResponse(response);
         } catch (ControlServiceException exception) {
             LOGGER.log(Level.SEVERE, format("Error getting feed [ %s ] items report ", feedId), exception);
 
             return createErrorJsonResponse(exception);
         }
     }
 
     public static ResponseBody clear() {
         CONTROL_SERVICE.clear();
 
         LOGGER.info("Service cleared");
 
         final SuccessMessageResponse successMessageResponse = create("Service cleared");
 
         return createJsonResponse(successMessageResponse);
     }
 
     private static ControlService createControlService() {
         final Transactions transactions = new GaeRootRepository();
         final UrlFetcher urlFetcher = new GaeUrlFetcher();
 
         final FeedUpdateTaskRepository feedUpdateTaskRepository = new GaeFeedUpdateTaskRepository();
         final FeedItemsRepository feedItemsRepository = new GaeFeedItemsRepository();
         final FeedHeadersRepository feedHeadersRepository = new GaeFeedHeadersRepository();
         final ReadFeedItemsRepository readFeedItemsRepository = new GaeReadFeedItemsRepository();
         final FeedUpdateTaskSchedulerContextRepository feedUpdateTaskSchedulerContextRepository = new GaeCacheFeedUpdateTaskSchedulerContextRepository();
 
         final FeedUpdateTaskScheduler feedUpdateTaskScheduler = new CycleFeedUpdateTaskScheduler(feedUpdateTaskSchedulerContextRepository, feedUpdateTaskRepository, transactions);
 
         return new ControlService(feedHeadersRepository, feedItemsRepository, feedUpdateTaskRepository, readFeedItemsRepository, feedUpdateTaskSchedulerContextRepository, feedUpdateTaskScheduler, urlFetcher, transactions);
     }
 
 }
