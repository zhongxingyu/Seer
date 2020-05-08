 package nmd.rss.collector.error;
 
 import java.util.UUID;
 
 import static java.lang.String.format;
 import static nmd.rss.collector.util.Assert.assertNotNull;
 import static nmd.rss.collector.util.Assert.assertStringIsValid;
 
 /**
  * Author : Igor Usenko ( igors48@gmail.com )
  * Date : 18.06.13
  */
 public class ServiceError {
 
     public final ErrorCode code;
     public final String message;
     public final String hints;
 
     private ServiceError(final ErrorCode code, final String message, final String hints) {
         this.code = code;
         this.message = message;
         this.hints = hints;
     }
 
     public static ServiceError urlFetcherError(final String link) {
 
         return new ServiceError(ErrorCode.URL_FETCH_ERROR,
                 format("Error fetching url [ %s ]", link),
                 "Invalid url or host unreachable. Check the url and try again.");
     }
 
     public static ServiceError feedExportError(final UUID feedId) {
         assertNotNull(feedId);
 
         return new ServiceError(ErrorCode.FEED_EXPORT_ERROR,
                 format("Unable to export feed with id [ %s ]", feedId),
                 "Looks like feed data corrupted. Try to recreate this feed.");
     }
 
     public static ServiceError feedParseError(final String link) {
         assertStringIsValid(link);
 
         return new ServiceError(ErrorCode.FEED_PARSE_ERROR,
                 format("Unable parse feed from [ %s ]", link),
                 "Possibly feed data corrupted. Check feed data.");
     }
 
     public static ServiceError wrongFeedId(final UUID feedId) {
         assertNotNull(feedId);
 
         return new ServiceError(ErrorCode.WRONG_FEED_ID,
                 format("Unable to find feed with id [ %s ]", feedId),
                 "Possibly feed id incorrect. Check feed identifier.");
     }
 
     public static ServiceError invalidFeedId(final String feedId) {
 
         return new ServiceError(ErrorCode.INVALID_FEED_ID,
                 format("Feed id [ %s ] is invalid", feedId),
                 "Feed id cannot be parsed. Check feed identifier.");
     }
 
     public static ServiceError invalidFeedOrItemId(final String pathInfo) {
 
         return new ServiceError(ErrorCode.INVALID_FEED_OR_ITEM_ID,
                 format("Feed id or item id cannot be parsed from [ %s ] ", pathInfo),
                 "Item id cannot be parsed. Check item identifier.");
     }
 
     public static ServiceError wrongFeedTaskId(final UUID feedId) {
         assertNotNull(feedId);
 
         return new ServiceError(ErrorCode.WRONG_FEED_TASK_ID,
                 format("Unable to find task for feed with id [ %s ]", feedId),
                 "Possibly feed id incorrect. Check feed identifier.");
     }
 
     public static ServiceError noScheduledTask() {
         return new ServiceError(ErrorCode.NO_SCHEDULED_TASK,
                 "There is no task scheduled for update",
                 "Possibly feed update schedule is empty. Check registered feeds list.");
     }
 
 }
