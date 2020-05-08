 package nl.eo.elasticsearch.plugin.twitter;
 
 import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
 import org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingResponse;
 import org.elasticsearch.action.ActionListener;
 import org.elasticsearch.action.get.GetRequest;
 import org.elasticsearch.action.get.GetResponse;
 import org.elasticsearch.action.update.UpdateRequest;
 import org.elasticsearch.action.update.UpdateResponse;
 import org.elasticsearch.action.delete.DeleteRequest;
 import org.elasticsearch.action.delete.DeleteResponse;
 import org.elasticsearch.action.search.SearchRequest;
 import org.elasticsearch.action.search.SearchResponse;
 import org.elasticsearch.action.search.SearchType;
 import org.elasticsearch.action.index.IndexRequest;
 import org.elasticsearch.action.index.IndexResponse;
 
 import org.elasticsearch.index.get.GetField;
 import org.elasticsearch.index.query.FilterBuilders;
 import org.elasticsearch.index.query.QueryBuilders;
 import org.elasticsearch.index.query.FilterBuilder;
 
 import org.elasticsearch.client.Client;
 
 import org.elasticsearch.common.Strings;
 import org.elasticsearch.common.unit.TimeValue;
 import org.elasticsearch.common.inject.Inject;
 import org.elasticsearch.common.settings.Settings;
 import org.elasticsearch.common.xcontent.XContentBuilder;
 import org.elasticsearch.common.xcontent.XContentHelper;
 import org.elasticsearch.common.xcontent.XContentBuilderString;
 import org.elasticsearch.common.xcontent.XContentFactory;
 
 import org.elasticsearch.search.SearchHit;
 import org.elasticsearch.search.sort.SortOrder;
 import org.elasticsearch.search.facet.Facet;
 import org.elasticsearch.search.facet.FacetBuilders;
 import org.elasticsearch.search.facet.datehistogram.DateHistogramFacet;
 import org.elasticsearch.search.facet.terms.TermsFacet;
 import org.elasticsearch.rest.*;
 
 import java.io.IOException;
 import java.util.*;
 import java.text.SimpleDateFormat;
 
 import static org.elasticsearch.rest.RestRequest.Method.GET;
 import static org.elasticsearch.rest.RestRequest.Method.POST;
 import static org.elasticsearch.rest.RestRequest.Method.PUT;
 import static org.elasticsearch.rest.RestRequest.Method.DELETE;
 import static org.elasticsearch.rest.RestStatus.NOT_FOUND;
 import static org.elasticsearch.rest.RestStatus.OK;
 import static org.elasticsearch.rest.action.support.RestXContentBuilder.restContentBuilder;
 
 public class TwitterSearchAction extends BaseRestHandler {
 
     public static String TYPE = "task";
 
     @Inject public TwitterSearchAction(Settings settings, Client client, RestController controller) {
         super(settings, client);
         logger.info("Initializing Twitter Search plugin");
         // Define REST endpoints
 
         // Get a list of all tasks, mapped to 'listTasks'
         controller.registerHandler(GET, "/_twittersearch/{index}", this);
 
         // Get a specific task, mapped to 'getTask'
         controller.registerHandler(GET, "/_twittersearch/{index}/{id}", this);
 
         // Get tweets for a specific task, mapped to 'getTweetTask'
         controller.registerHandler(GET, "/_twittersearch/{index}/{id}/{tweets}", this);
 
         // Delete a specific task, mapped to 'deleteTask'
         controller.registerHandler(DELETE, "/_twittersearch/{index}/{id}", this);
 
         // Add a task, mapped to 'addTask'
         controller.registerHandler(POST, "/_twittersearch/{index}/", this);
 
         // Update a specific task
         controller.registerHandler(POST, "/_twittersearch/{index}/{id}", this);
     }
 
     /**
      * Update the Twitter River (with given name), because apparently our configuration of hashtags or 
      * usernames has changed (by an update, delete or insert).
      */
     private void updateRiver(final String index) {
         logger.info("Updating river");
         GetResponse taskResponse = client.prepareGet(index, "twitterconfig", "_meta").execute().actionGet();
 
         final Map<String, Object> twitterConfig = taskResponse.getSource();
         final SearchRequest searchRequest = new SearchRequest(index);
 
         searchRequest.types(TYPE);
         searchRequest.listenerThreaded(false);
         SearchResponse searchResponse = client.prepareSearch(index).setTypes(TYPE).setSize(10000).execute().actionGet();
 
         Set<String> toFollow = new TreeSet<String>();
         Set<String> toTrack = new TreeSet<String>();
 
         for (SearchHit hit : searchResponse.hits()) {
             Map<String, Object> source = hit.getSource();
            String follow[] = Strings.commaDelimitedListToStringArray(((String)source.get("follow")).toLowerCase());
            String track[] = Strings.commaDelimitedListToStringArray(((String)source.get("track")).toLowerCase());
             toFollow.addAll(Arrays.asList(follow));
             for (int i=0; i<track.length; i++) {
                 toTrack.add("#" + track[i]);
             }
         }
         logger.info("Tracking userids {} and keywords {}", toFollow, toTrack);
         final boolean startRiver = toFollow.size() > 0 || toTrack.size() > 0;
 		
         TwitterUtil tu = new TwitterUtil(twitterConfig);
         List<String> toFollowIDs = Arrays.asList(tu.getUserIds(toFollow.toArray(new String[0])));
 
         Map t = new HashMap();
         t.put("tracks", Strings.collectionToCommaDelimitedString(toTrack));
         t.put("follow", Strings.collectionToCommaDelimitedString(toFollowIDs));
         twitterConfig.put("filter", t);
 
         ClusterStateResponse resp = client.admin().cluster().prepareState().execute().actionGet();
         Map mappings = resp.state().metaData().index("_river").mappings();
 
         if (mappings.containsKey("twitter")) {
             logger.info("Old twitter river exists, we will delete it");
             client.admin().indices().prepareDeleteMapping("_river")
                 .setType("twitter")
                 .setListenerThreaded(false)
                 .execute(new ActionListener<DeleteMappingResponse>() {
                 @Override public void onResponse(DeleteMappingResponse response) {
                     try {
                        logger.info("Removing old river");
                        // WARNING: At this point the "old river" is still in the process of being removed. 
                        // usually the removal takes approx less than 20 ms
                        // ... let's wait for 250 milliseconds, before we start a new river. 
                        Thread.sleep(250);
                         if (startRiver) {
                             addRiver(twitterConfig, index);
                         } else {
                             logger.info("No keywords to track and no people to follow: skipping river creation");
                         }
                     } catch (Exception e) {
                         onFailure(e);
                     }
                 }
 
                 @Override public void onFailure(Throwable e) {
                     logger.error("Error while removing river", e);
                 }
             });
         } else {
             logger.info("No old twitter river, so no need to delete the mapping");
             if (startRiver) {
                 addRiver(twitterConfig, index);
             } else {
                 logger.info("No keywords to track and no people to follow: skipping river creation");
             }
         }
 
     }
 
     private void addRiver(Map twitterConfig, String index) {
         // Put the entire twitter configuration (partially recovered from the _meta key, partially created by our logic)
         // into a hashmap
 
         try {
             XContentBuilder builder = XContentFactory.jsonBuilder()
               .startObject()
                 .field("type", "twitter")
                 .field("twitter", twitterConfig)
                 .startObject("index")
                   .field("index", index)
                   .field("type", "status")
                   .field("bulk_size", 1)
                 .endObject()
               .endObject();
 
             IndexResponse response2 = client.prepareIndex("_river", "twitter", "_meta")
                 .setSource(builder)
                 .setOperationThreaded(false)
                 .setListenerThreaded(false)
                 .execute().actionGet();
 
             logger.info("Created new river");
         } catch (IOException e) {
             logger.error("Exception while building new river: {}", e);
         }
 
     }
 
     private void historicSearch(String id) {
       // TODO: do a historic search for the given tas, so we can populate the index with some content
     }
 
     /**
      * Return a list of userids for the list of screennames
      */
     private String[] findUserIds(String screennames, Map<String, Object> config) {
         TwitterUtil tu = new TwitterUtil(config);
         return tu.getUserIds(Strings.commaDelimitedListToStringArray(screennames));
     }
 
     private void deleteTask(final RestRequest request, final RestChannel channel, final String index, final String id) {
         String type = "_meta".equals(id) ? "twitterconfig" : TYPE;
         DeleteResponse response = client.prepareDelete(index, type, id)
           .setRefresh(true)
           .execute().actionGet();
         try {
             XContentBuilder builder = restContentBuilder(request)
               .startObject()
                 .field("id", response.getId())
                 .field("version", response.getVersion())
               .endObject();
             channel.sendResponse(new XContentRestResponse(request, OK, builder));
         } catch (Exception e) {
             try {
                 channel.sendResponse(new XContentThrowableRestResponse(request, e));
             } catch (IOException e1) {
                 logger.error("Failed to send failure response", e1);
             }
         }
         updateRiver(index);
     }
 
     private void listTasks(final RestRequest request, final RestChannel channel, final String index) {
         final SearchRequest searchRequest = new SearchRequest(index);
         searchRequest.types(TYPE);
         searchRequest.listenerThreaded(false);
 
         SearchResponse searchResponse = client.prepareSearch(index).setTypes(TYPE).setSize(10000).execute().actionGet();
         try {
             XContentBuilder builder = restContentBuilder(request);
             builder.startObject();
             searchResponse.toXContent(builder, request);
             builder.endObject();
             channel.sendResponse(new XContentRestResponse(request, OK, builder));
         } catch (Throwable e) {
             try {
                 e.printStackTrace();
                 channel.sendResponse(new XContentThrowableRestResponse(request, e));
             } catch (IOException e1) {
                 logger.error("Failed to send failure response", e1);
             }
         }
     }
 
     private void addTask(final RestRequest request, final RestChannel channel, final String index) {
         Map<String,Object> source = XContentHelper.convertToMap(request.content().toBytes(), false).v2();
 
         GetResponse taskResponse = client.prepareGet(index, "twitterconfig", "_meta").execute().actionGet();
         Map<String, Object> twitterConfig = taskResponse.getSource();
 
         String follow = (String)source.get("follow");
         String[] ids = findUserIds(follow, twitterConfig);
 
         source.put("follow_ids", Strings.arrayToCommaDelimitedString(ids));
 
         IndexResponse response = client.prepareIndex(index, TYPE)
           .setSource(source)
           .setRefresh(true)
           .execute().actionGet();
         try {
             XContentBuilder builder = restContentBuilder(request)
               .startObject()
                 .field("id", response.getId())
               .endObject();
             channel.sendResponse(new XContentRestResponse(request, OK, builder));
         } catch (Exception e) {
             try {
                 channel.sendResponse(new XContentThrowableRestResponse(request, e));
             } catch (IOException e1) {
                 logger.error("Failed to send failure response", e1);
             }
         }
 
         historicSearch(response.getId());
         updateRiver(index);
     }
 
     private void updateTask(final RestRequest request, final RestChannel channel, final String index, final String id) {
         String type = "_meta".equals(id) ? "twitterconfig" : TYPE;
         IndexResponse response = client.prepareIndex(index, type, id)
           .setSource(request.content().toBytes())
           .setRefresh(true)
           .execute().actionGet();
         try {
             XContentBuilder builder = restContentBuilder(request)
               .startObject()
                 .field("id", response.getId())
                 .field("version", response.getVersion())
               .endObject();
             channel.sendResponse(new XContentRestResponse(request, OK, builder));
         } catch (Exception e) {
             try {
                 channel.sendResponse(new XContentThrowableRestResponse(request, e));
             } catch (IOException e1) {
                 logger.error("Failed to send failure response", e1);
             }
         }
 
         historicSearch(response.getId());
         updateRiver(index);
     }
 
     private void getTweets(final RestRequest request, final RestChannel channel, final String index, final String id, final String tweets) {
         GetResponse taskResponse = client.prepareGet(index, TYPE, id).execute().actionGet();
         Map<String, Object> task = taskResponse.getSource();
 
         String follow = (String)task.get("follow_ids");
         String track = (String)task.get("track");
         String block = (String)task.get("block");
 
         String interval = request.hasParam("interval") ? request.param("interval") : "day";
         long start = request.hasParam("start") ? Long.decode(request.param("start")).longValue() : 0;
         long stop = request.hasParam("stop") ? Long.decode(request.param("stop")).longValue() : 0;
         int seconds = request.hasParam("seconds") ? Integer.parseInt(request.param("seconds")) : 0;
         int size = request.hasParam("size") ? Integer.parseInt(request.param("size")) : 10;
         long max_id = request.hasParam("max_id") ? Long.decode(request.param("max_id")).longValue() : 0;
         long page = request.hasParam("page") ? Long.decode(request.param("page")).longValue() : 0;
 
         long currentTime = System.currentTimeMillis();
 
         try {
             // The elasticsearch query will be in the form:
             // ((user.screen_name IN follow) OR (hashtag.text IN track)) AND NOT (user.screen_name IN block)
             // So, we use a TermFilter for 'user.screen_name' and a TermFilter for 'hashtag.text'
             // These are added to the 'mayMatch' array, and added together with a OrFilter
             // Then we use an ANDFilter and a NOTFilter for the usernames we need to block
             ArrayList<FilterBuilder> mustMatch = new ArrayList<FilterBuilder>();
 
             // First: which hashtags & screennames do we follow?
             ArrayList<FilterBuilder> mayMatch = new ArrayList<FilterBuilder>();
             if (follow != null && !"".equals(follow)) {
               mayMatch.add(FilterBuilders.termFilter("user.id", Strings.commaDelimitedListToStringArray(follow)));
             }
             if (track != null && !"".equals(track)) {
               mayMatch.add(FilterBuilders.termFilter("hashtag.text", Strings.commaDelimitedListToStringArray(track)));
             }
             mustMatch.add(FilterBuilders.orFilter(mayMatch.toArray(new FilterBuilder[0])));
 
             // Second: which screennames do we remove from the searchresults?
             if (block != null && !"".equals(block)) {
                 logger.info("Adding blocklist: [{}]", block);
                 mustMatch.add(
                     FilterBuilders.notFilter(
                         FilterBuilders.termFilter("user.screen_name", Strings.commaDelimitedListToStringArray(block))
                     )
                 );
             }
 
             // Third: maybe we only need a specific timespan
             if (start > 0) {
                 mustMatch.add(FilterBuilders.rangeFilter("created_at").from(start));
             }
             if (stop > 0) {
                 mustMatch.add(FilterBuilders.rangeFilter("created_at").to(stop));
             }
 
 
             // If only the 'seconds' parameter is given, we want to return all tweets from the last X seconds
             // We max it on 10 tweets per second, to prevent interface overload
             if (seconds > 0) {
                 mustMatch.add(FilterBuilders.rangeFilter("created_at").from(currentTime - (1000 * seconds)));
                 size = seconds * 10;
             }
 
             // We do NOT support paging! So if a (legacy) widget provides a page-parameter, we just don't return any data
             if (page > 1) {
                 size = 0;
             }
 
             // Done: creating the filter
             FilterBuilder filter = FilterBuilders.andFilter(mustMatch.toArray(new FilterBuilder[0]));
 
             // Fetch the resulsts, ordered by 'created_at' descending (newest tweets first)
             SearchResponse response = client.prepareSearch(index)
                     .setTypes("status")
                     .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                     .setFilter(filter)
                     .setFrom(0).setSize(size)
                     .addFacet(FacetBuilders.termsFacet("tweeters").field("user.screen_name").size(10).facetFilter(filter))
                     .addFacet(FacetBuilders.termsFacet("hashtags").field("hashtag.text").size(10).facetFilter(filter))
                     .addFacet(FacetBuilders.termsFacet("mentions").field("mention.screen_name").size(10).facetFilter(filter))
                     .addFacet(FacetBuilders.termsFacet("retweetedusers").field("retweet.user_screen_name").size(10).facetFilter(filter))
                     .addFacet(FacetBuilders.termsFacet("retweetedstatus").field("retweet.id").size(10).facetFilter(filter))
                     .addFacet(FacetBuilders.dateHistogramFacet("histogram").field("created_at").interval(interval).facetFilter(filter))
                     .addSort("created_at", SortOrder.DESC)
                     .execute()
                     .actionGet();
 
 
             // Create the result JSON in the same format of the old 'search.twitter.com' API
             XContentBuilder builder = XContentFactory.jsonBuilder();
             builder.startObject();
             builder.field("completed_in", ((float)response.tookInMillis() / 1000));
             builder.field("current_time", currentTime);
 
             if (response.hits().getHits().length > 0) {
                 builder.field("max_id", Long.decode(response.hits().getAt(0).id()).longValue());
                 builder.field("max_id_str", response.hits().getAt(0).id());
             }
             builder.startArray("results");
             for (SearchHit hit : response.hits()) {
                 builder.startObject();
                 Map<String, Object> source = hit.getSource();
                 builder.field("created_at", source.get("created_at"));
                 builder.field("from_user", ((Map)source.get("user")).get("screen_name"));
                 builder.field("from_user_id", ((Map)source.get("user")).get("id"));
                 builder.field("from_user_id_str", "" + ((Map)source.get("user")).get("id"));
                 builder.field("from_user_name", ((Map)source.get("user")).get("name"));
                 builder.field("profile_image_url", ((Map)source.get("user")).get("profile_image_url"));
                 builder.field("profile_image_url_https", ((Map)source.get("user")).get("profile_image_url_https"));
                 builder.field("id", Long.parseLong(hit.id(), 10));
                 builder.field("id_str", hit.id());
                 builder.field("text", source.get("text"));
                 builder.field("source", source.get("source"));
                 builder.endObject();
             }
             builder.endArray();
 
             // We also add facet information: the 'search.twitter.api' doesn't return this, but ElasticSearch can
             builder.startObject("facets");
             for (Facet facet : response.getFacets()) {
                 builder.startObject(facet.getName());
                 builder.field("_type", facet.getType());
                 if (facet instanceof TermsFacet) {
                     builder.startArray("terms");
                     for (TermsFacet.Entry entry : (TermsFacet)facet) {
                         builder.startObject();
                         builder.field("term", entry.term());
                         builder.field("count", entry.count());
                         builder.endObject();
                     }
                     builder.endArray();
                 } else if (facet instanceof DateHistogramFacet) {
                     builder.startArray("entries");
                     for (DateHistogramFacet.Entry entry : (DateHistogramFacet)facet) {
                         builder.startObject();
                         builder.field("time", entry.getTime());
                         builder.field("count", entry.count());
                         builder.endObject();
                     }
                     builder.endArray();
                 }
                 builder.endObject();
             }
             builder.endObject();
             builder.endObject();
 
 
             RestResponse restResponse = new XContentRestResponse(request, OK, builder);
             /*
               This code requires the very latest ElasticSearch version; not usable right now
 
               int cacheTime = 10;
               SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
             
               restResponse.addHeader("Cache-Control", "max-age=" + cacheTime);
               restResponse.addHeader("Expires", format.format(new Date(currentTime + cacheTime * 1000)));
               restResponse.addHeader("Last-Modified", format.format(new Date(currentTime)));
               restResponse.addHeader("Pragma", "public");
             */
 
             channel.sendResponse(restResponse);
         } catch (Exception e) {
             e.printStackTrace();
             try {
                 channel.sendResponse(new XContentThrowableRestResponse(request, e));
             } catch (IOException e1) {
                 logger.error("Failed to send failure response", e1);
             }
         }
     }
 
     private void getTask(final RestRequest request, final RestChannel channel, final String index, final String id) {
         String type = "_meta".equals(id) ? "twitterconfig" : TYPE;
         GetResponse response = client.prepareGet(index, type, id).execute().actionGet();
         try {
             XContentBuilder builder = restContentBuilder(request);
             response.toXContent(builder, request);
 
             if (!response.exists()) {
                 channel.sendResponse(new XContentRestResponse(request, NOT_FOUND, builder));
             } else {
                 channel.sendResponse(new XContentRestResponse(request, OK, builder));
             }
         } catch (Exception e) {
             try {
                 channel.sendResponse(new XContentThrowableRestResponse(request, e));
             } catch (IOException e1) {
                 logger.error("Failed to send failure response", e1);
             }
         }
     }
 
     /**
      * Main entrypoint for this Rest Handler: it dispatches the request to the
      * corresponding method.
      */
     public void handleRequest(final RestRequest request, final RestChannel channel) {
         final String index = request.hasParam("index") ? request.param("index") : "";
         final String id = request.hasParam("id") ? request.param("id") : "";
         final String tweets = request.hasParam("tweets") ? request.param("tweets") : "";
         logger.info("Request: {} {} {} {}", request.method(), index, id, tweets);
 
         switch(request.method()) {
             case DELETE:
                 deleteTask(request, channel, index, id);
                 break;
             case GET:
                 if ("".equals(id)) {
                     listTasks(request, channel, index);
                 } else {
                     if ("".equals(tweets)) {
                         getTask(request, channel, index, id);
                     } else {
                         getTweets(request, channel, index, id, tweets);
                     }
                 }
                 break;
             case POST:
             case PUT:
                 if ("".equals(id)) {
                     addTask(request, channel, index);
                 } else if ("_search".equals(id)) {
                     listTasks(request, channel, index);
                 } else {
                     updateTask(request, channel, index, id);
                 }
                 break;
         }
 
     }
 }
