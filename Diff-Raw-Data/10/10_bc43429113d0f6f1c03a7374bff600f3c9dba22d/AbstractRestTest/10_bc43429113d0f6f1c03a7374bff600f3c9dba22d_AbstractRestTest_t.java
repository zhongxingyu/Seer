 package rest;
 
 import com.google.gson.Gson;
 import nmd.rss.collector.error.ErrorCode;
 import nmd.rss.collector.rest.responses.*;
 import org.junit.After;
 
 import static com.jayway.restassured.RestAssured.given;
 import static org.junit.Assert.assertEquals;
 
 /**
  * User: igu
  * Date: 28.11.13
  */
 public abstract class AbstractRestTest {
 
     protected static final String CLEAR_SERVLET_URL = "/secure/v01/clear/";
     protected static final String FEEDS_SERVLET_URL = "/secure/v01/feeds/";
     protected static final String UPDATES_SERVLET_URL = "/secure/v01/updates/";
     protected static final String READS_SERVLET_URL = "/secure/v01/reads/";
     protected static final String EXPORTS_SERVLET_URL = "/v01/feeds/";
 
     protected static final String FIRST_FEED_URL = "http://localhost:8080/feed/feed_win_1251.xml";
     protected static final String FIRST_FEED_TITLE = "Bash.im";
     protected static final String SECOND_FEED_URL = "http://localhost:8080/feed/feed_win_1251_2.xml";
     protected static final String INVALID_FEED_URL = "http://localhost:8080/feed/not_exist.xml";
     protected static final String UNREACHABLE_FEED_URL = "http://localhost:8081/feed/not_exist.xml";
 
     private static final Gson GSON = new Gson();
 
     @After
     public void after() {
         clearState();
     }
 
     protected static void clearState() {
         given().body("").post(CLEAR_SERVLET_URL);
     }
 
     protected static FeedIdResponse addFirstFeed() {
         return GSON.fromJson(assertSuccessResponse(addFeed(FIRST_FEED_URL)), FeedIdResponse.class);
     }
 
     protected static FeedIdResponse addSecondFeed() {
         return GSON.fromJson(assertSuccessResponse(addFeed(SECOND_FEED_URL)), FeedIdResponse.class);
     }
 
     protected static String addFeed(final String url) {
         return given().body(url).post(FEEDS_SERVLET_URL).asString();
     }
 
     protected static FeedHeadersResponse getFeedHeaders() {
         final String response = given().get(FEEDS_SERVLET_URL).asString();
 
         return GSON.fromJson(assertSuccessResponse(response), FeedHeadersResponse.class);
     }
 
     protected static FeedHeaderHelper getFeedHeader(final String feedId) {
         final String response = getFeedHeaderAsString(feedId);
 
         return GSON.fromJson(assertSuccessResponse(response), FeedHeaderHelper.class);
     }
 
     protected static String getFeedHeaderAsString(String feedId) {
         return given().get(FEEDS_SERVLET_URL + feedId).asString();
     }
 
     protected static String deleteFeed(final String feedId) {
         return given().delete(FEEDS_SERVLET_URL + feedId).asString();
     }
 
     protected static String updateFeedTitle(final String feedId, final String title) {
         return given().body(title).put(FEEDS_SERVLET_URL + feedId).asString();
     }
 
     protected static String exportFeed(final String feedId) {
         return given().get(EXPORTS_SERVLET_URL + feedId).asString();
     }
 
     protected static String updateFeed(final String feedId) {
         return given().get(UPDATES_SERVLET_URL + feedId).asString();
     }
 
     protected static String updateCurrentFeed() {
         return updateFeed("");
     }
 
     protected static FeedMergeReportResponse updateCurrentFeedWithReport() {
         return GSON.fromJson(assertSuccessResponse(updateFeed("")), FeedMergeReportResponse.class);
     }
 
     protected String getReadsReportAsString() {
         return given().get(READS_SERVLET_URL).asString();
     }
 
     protected String getFeedItemsReportAsString(final String feedId) {
         return given().get(READS_SERVLET_URL + feedId).asString();
     }
 
     protected FeedReadReportsResponse getReadsReport() {
         return GSON.fromJson(assertSuccessResponse(getReadsReportAsString()), FeedReadReportsResponse.class);
     }
 
     protected FeedItemsReportResponse getFeedItemsReport(final String feedId) {
         return GSON.fromJson(assertSuccessResponse(getFeedItemsReportAsString(feedId)), FeedItemsReportResponse.class);
     }
 
     protected String markItem(final String feedId, String itemId, String markMode) {
        final String parameter = markMode.isEmpty() ? "" : "?mark-as=" + markMode;
 
         return given().put(READS_SERVLET_URL + feedId + "/" + itemId + parameter).asString();
     }
 
     protected String markItemAsRead(final String feedId, String itemId) {
         return markItem(feedId, itemId, "read");
     }
 
     protected String markItemAsReadLater(final String feedId, String itemId) {
         return markItem(feedId, itemId, "read-later");
     }
 
     protected static void assertErrorResponse(final String response, final ErrorCode errorCode) {
         final ErrorResponse errorResponse = GSON.fromJson(response, ErrorResponse.class);
 
         assertEquals(ResponseType.ERROR, errorResponse.getStatus());
         assertEquals(errorCode, errorResponse.getCode());
     }
 
     protected static String assertSuccessResponse(final String response) {
         final SuccessMessageResponse successResponse = GSON.fromJson(response, SuccessMessageResponse.class);
 
         assertEquals(ResponseType.SUCCESS, successResponse.getStatus());
 
         return response;
     }
 
 }
