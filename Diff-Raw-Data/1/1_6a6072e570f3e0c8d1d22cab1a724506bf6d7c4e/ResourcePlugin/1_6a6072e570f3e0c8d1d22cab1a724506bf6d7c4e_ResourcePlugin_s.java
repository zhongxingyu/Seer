 package org.deepamehta.plugins.eduzen;
 
 import com.sun.jersey.api.view.Viewable;
 import com.sun.jersey.spi.container.ContainerResponse;
 import de.deepamehta.core.Association;
 import de.deepamehta.core.RelatedTopic;
 import de.deepamehta.core.Topic;
 import de.deepamehta.core.model.*;
 import de.deepamehta.core.service.ClientState;
 import de.deepamehta.core.service.Directives;
 import de.deepamehta.core.service.PluginService;
 import de.deepamehta.core.service.ResultList;
 import de.deepamehta.core.service.annotation.ConsumesService;
 import de.deepamehta.core.service.event.PreSendTopicListener;
 import de.deepamehta.core.service.event.ServiceResponseFilterListener;
 import de.deepamehta.core.storage.spi.DeepaMehtaTransaction;
 import de.deepamehta.plugins.accesscontrol.service.AccessControlService;
 import de.deepamehta.plugins.tags.service.TaggingService;
 import de.deepamehta.plugins.time.service.TimeService;
 import de.deepamehta.plugins.topicmaps.model.TopicViewmodel;
 import de.deepamehta.plugins.topicmaps.model.TopicmapViewmodel;
 import de.deepamehta.plugins.webactivator.WebActivatorPlugin;
 import java.text.DateFormat;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.ws.rs.*;
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 import org.deepamehta.plugins.eduzen.service.ResourceService;
 // import org.deepamehta.plugins.subscriptions.service.SubscriptionService;
 
 
 /**
  * Notizen-Timeline Web-Application
  * @version 0.2.4-SNAPSHOT
  * @author Malte Reißig <malte.reissig@tu-berlin.de>
  * @since 31 March 2014
  *
  * @see http://www.eduzen.tu-berlin.de
  */
 
 @Path("/")
 @Consumes("application/json")
 @Produces("text/html")
 public class ResourcePlugin extends WebActivatorPlugin implements ResourceService, PreSendTopicListener,
         ServiceResponseFilterListener {
 
     private Logger log = Logger.getLogger(getClass().getName());
 
     private final static String TAG_URI = "dm4.tags.tag";
     private final static String REVIEW_URI = "org.deepamehta.reviews.score";
 
     private final static String RESOURCE_URI = "org.deepamehta.resources.resource";
     private final static String RESOURCE_CONTENT_URI = "org.deepamehta.resources.content";
     private final static String RESOURCE_CREATED_AT_URI = "org.deepamehta.resources.created_at";
     private final static String RESOURCE_LAST_MODIFIED_URI = "org.deepamehta.resources.last_modified_at";
     private final static String RESOURCE_PUBLISHED_URI = "org.deepamehta.resources.is_published";
     private final static String RESOURCE_LOCKED_URI = "org.deepamehta.resources.blocked_for_edits";
     private final static String RESOURCE_LICENSE_URI = "org.deepamehta.resources.license";
     private final static String RESOURCE_LICENSE_UNSPECIFIED_URI = "org.deepamehta.licenses.unspecified";
     private final static String RESOURCE_LICENSE_UNKNOWN_URI = "org.deepamehta.licenses.unknown";
     private final static String RESOURCE_LICENSE_AREA_URI = "org.deepamehta.resources.license_jurisdiction";
 
     private static final String MOODLE_PARTICIPANT_EDGE = "org.deepamehta.moodle.course_participant";
 
     private static final String MOODLE_COURSE_URI = "org.deepamehta.moodle.course";
     private static final String MOODLE_SECTION_URI = "org.deepamehta.moodle.section";
 
     private static final String MOODLE_ITEM_URI = "org.deepamehta.moodle.item";
     private static final String MOODLE_ITEM_ICON_URI = "org.deepamehta.moodle.item_icon";
     private static final String MOODLE_ITEM_REMOTE_URL_URI = "org.deepamehta.moodle.item_url";
     private static final String MOODLE_ITEM_DESC_URI = "org.deepamehta.moodle.item_description";
     private static final String MOODLE_ITEM_HREF_URI = "org.deepamehta.moodle.item_href";
     // private static final String MOODLE_ITEM_TYPE_URI = "org.deepamehta.moodle.item_type";
     private static final String MOODLE_ITEM_MODIFIED_URI = "org.deepamehta.moodle.item_modified";
     // private static final String MOODLE_ITEM_CREATED_URI = "org.deepamehta.moodle.item_created";
     // private static final String MOODLE_ITEM_AUTHOR_URI = "org.deepamehta.moodle.item_author";
     // private static final String MOODLE_ITEM_LICENSE_URI = "org.deepamehta.moodle.item_license";
 
     private final static String CREATOR_EDGE_URI = "org.deepamehta.resources.creator_edge";
     private final static String CONTRIBUTOR_EDGE_URI = "org.deepamehta.resources.contributor_edge";
     private final static String CHILD_URI = "dm4.core.child";
     private final static String PARENT_URI = "dm4.core.parent";
     private final static String DEFAULT_ROLE_TYPE_URI = "dm4.core.default";
     private final static String COMPOSITION_TYPE_URI = "dm4.core.composition";
     private final static String AGGREGATION_TYPE_URI = "dm4.core.aggregation";
     private final static String ACCOUNT_TYPE_URI = "dm4.accesscontrol.user_account";
     private final static String IDENTITY_NAME_TYPE_URI = "org.deepamehta.identity.display_name";
     private final static String IDENTITY_INFOS_TYPE_URI = "org.deepamehta.identity.infos";
     private final static String PROFILE_PICTURE_EDGE_URI = "org.deepamehta.identity.profile_picture_edge";
     private final static String DEEPAMEHTA_FILE_URI = "dm4.files.file";
     private final static String DEEPAMEHTA_FILE_PATH_URI = "dm4.files.path";
 
     private final static String PROP_URI_CREATED  = "dm4.time.created";
     private final static String PROP_URI_MODIFIED = "dm4.time.modified";
 
     // private SubscriptionService notificationService = null;
     private AccessControlService aclService = null;
     private TaggingService taggingService = null;
     private TimeService timeService = null;
 
     @Override
     public void init() {
         initTemplateEngine();
     }
 
     @Override
     public void preSendTopic(Topic topic, ClientState clientState) {
         // enrich a single resource-topic about creator and modifiers
         if (topic.getTypeUri().equals(RESOURCE_URI)) {
             enrichTopicModelAboutCreator(topic); // called for topic-detail request but not for a topic in a collection
         }
     }
 
     @Override
     public void serviceResponseFilter(ContainerResponse response) {
         Object entity = response.getEntity();
         if (entity instanceof TopicmapViewmodel ){
             TopicmapViewmodel topicmap = (TopicmapViewmodel) entity;
             for (TopicViewmodel topic : topicmap.getTopics()) {
                 if (topic.getTypeUri().equals(RESOURCE_URI)) {
                     String timestamp = topic.getSimpleValue().toString();
                     if (!timestamp.isEmpty()) {
                         try {
                             long time = Long.parseLong(timestamp);
                             Date date = new Date(time);
                             String new_label = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT,
                                     new Locale("de", "DE")).format(date);
                             topic.setSimpleValue("Notiz vom " + new_label.toString());
                         } catch (NumberFormatException nex) {
                             log.warning("Resource label is no timestamp, skipping fancy label-rewrite.");
                         }
                     }
                 }
             }
         }
     }
 
     @GET
     @Produces("text/html")
     public Viewable getFrontView() {
         return getTimelineView();
     }
 
     @GET
     @Path("/notes")
     @Produces("text/html")
     public Viewable getTimelineView() {
         viewData("name", "Notizen Timeline");
         viewData("path", "/notes");
         viewData("style", "style.css");
         viewData("picture", "http://www.eduzen.tu-berlin.de/sites/default/files/eduzen_bright_logo.png");
         return view("index");
     }
 
     @GET
     @Path("/notes/info")
     @Produces("text/html")
     public Viewable getInfoView() {
         return view("info");
     }
 
     @GET
     @Path("/notes/tagged/{tags}")
     @Produces("text/html")
     public Viewable getFilteredeTimelineView(@PathParam("tags") String tagFilter,
         @HeaderParam("Cookie") ClientState clientState) {
         viewData("name", "Gefilterte Notizen Timeline, Tags: " + tagFilter);
         viewData("style", "style.css");
         viewData("path", "/notes/tagged/" + tagFilter);
         viewData("picture", "http://www.eduzen.tu-berlin.de/sites/default/files/eduzen_bright_logo.png");
         return view("index");
     }
 
     @GET
     @Path("/notes/user/{userId}")
     @Produces("text/html")
     public Viewable getPersonalTimelineView(@PathParam("userId") long userId,
         @HeaderParam("Cookie") ClientState clientState) {
         //
         String description = getUsersDescription(userId);
         String display_name = getUserDisplayName(userId);
         viewData("name", display_name + "'s Notizen Timeline");
         viewData("description", description);
         viewData("style", "style.css");
         String profile_picture = getUserProfilePicturePath(userId);
         viewData("picture", "http://www.eduzen.tu-berlin.de/sites/default/files/eduzen_bright_logo.png");
         if (profile_picture != null) {
             viewData("picture", "http://notizen.eduzen.tu-berlin.de/filerepo" + profile_picture);
         }
         viewData("path", "/notes/user" + userId);
 
         //
         return view("index");
     }
 
     @GET
     @Path("/notes/{id}")
     @Produces("text/html")
     public Viewable getDetailView(@PathParam("id") long resourceId, @HeaderParam("Cookie") ClientState clientState) {
         Topic resource = dms.getTopic(resourceId, true);
         long lastModified = resource.getModel().getCompositeValueModel().getLong(RESOURCE_LAST_MODIFIED_URI);
         viewData("name", "Notiz, zuletzt bearbeitet: " + new Date(lastModified).toString());
         viewData("style", "style.css");
         String description = "";
         if (resource.getCompositeValue().has(TAG_URI)) {
             for (Topic element : resource.getCompositeValue().getTopics(TAG_URI)) {
                 description += element.getSimpleValue();
             }
         }
         viewData("description", "Tagged: " + description);
         viewData("path", "/notes/" + resource.getId());
         viewData("picture", "http://www.eduzen.tu-berlin.de/sites/default/files/eduzen_bright_logo.png");
         return view("index");
     }
 
     @GET
     @Path("/notes/{id}/print")
     @Produces("text/html")
     public Viewable getDetailPrintView(@PathParam("id") long resourceId) {
         Topic resource = dms.getTopic(resourceId, true);
         long lastModified = resource.getModel().getCompositeValueModel().getLong(RESOURCE_LAST_MODIFIED_URI);
         viewData("name", "Notiz, zuletzt bearbeitet: " + new Date(lastModified).toString());
         viewData("style", "detail-print.css");
         viewData("path", "/notes/" + resource.getId());
         viewData("picture", "http://www.eduzen.tu-berlin.de/sites/default/files/eduzen_bright_logo.png");
         return view("index");
     }
 
     /**
      * Creates a new <code>Resource</code> instance based on the domain specific
      * REST call with a alternate JSON topic representation.
      */
 
     @POST
     @Path("/notes/resource/create")
     @Produces("application/json")
     @Override
     public Topic createResource(TopicModel topicModel, @HeaderParam("Cookie") ClientState clientState) {
         /// ### Wrap creation of the "Creator"-Relationship in this transaction too
         DeepaMehtaTransaction tx = dms.beginTx();
         Topic resource = null;
         // 0) Authorize create request
         Topic user = getAuthorizedUser();
         try {
             // 1) enrich new topic about timestamps and defaults
             long createdAt = new Date().getTime();
             topicModel.getCompositeValueModel().put(RESOURCE_CREATED_AT_URI, createdAt);
             topicModel.getCompositeValueModel().put(RESOURCE_LAST_MODIFIED_URI, createdAt);
             topicModel.getCompositeValueModel().put(RESOURCE_PUBLISHED_URI, true);
             topicModel.getCompositeValueModel().put(RESOURCE_LOCKED_URI, false);
             topicModel.getCompositeValueModel().putRef(RESOURCE_LICENSE_URI, RESOURCE_LICENSE_UNSPECIFIED_URI);
             topicModel.getCompositeValueModel().putRef(RESOURCE_LICENSE_AREA_URI, RESOURCE_LICENSE_UNKNOWN_URI);
             // topicModel.getCompositeValueModel().putRef(RESOURCE_AUTHOR_NAME_URI, RESOURCE_AUTHOR_ANONYMOUS_URI);
             resource = dms.createTopic(topicModel, clientState); // clientstate is for workspace-assignment
             Association edge = assignAuthorship(resource, user, clientState);
             if (edge == null) log.warning("Could not relate new resource ("
                     + resource.getId() + ") to author " + user.getSimpleValue());
             tx.success();
             return resource;
         } catch (Exception e) {
             throw new WebApplicationException(new RuntimeException("Something went wrong while creating resource", e));
         } finally {
             tx.finish();
             // if (notificationService != null) notificationService.notify("Notiz angelegt", "", user.getId(), resource);
         }
     }
 
     /**
      * Updates an existing <code>Resource</code> instance based on the domain specific
      * REST call with a alternate JSON topic representation.
      */
 
     @POST
     @Path("/notes/resource/update")
     @Produces("application/json")
     @Override
     public Topic updateResource(TopicModel topic, @HeaderParam("Cookie") ClientState clientState) {
         DeepaMehtaTransaction tx = dms.beginTx();
         Topic resource = null;
         Topic user = getAuthorizedUser();
         try {
             // check htmlContent for <script>-tag
             String value = topic.getCompositeValueModel().getString(RESOURCE_CONTENT_URI);
             // updated last_modified timestamp
             long modifiedAt = new Date().getTime();
             // is locked?
             boolean isLocked = topic.getCompositeValueModel().getBoolean(RESOURCE_LOCKED_URI);
             // update resource topic
             resource = dms.getTopic(topic.getId(), true);
             resource.setCompositeValue(new CompositeValueModel().put(RESOURCE_CONTENT_URI, value),
                     clientState, new Directives()); // why new Directives on an AttachedObject
             resource.setCompositeValue(new CompositeValueModel().put(RESOURCE_LAST_MODIFIED_URI, modifiedAt),
                     clientState, new Directives());
             resource.setCompositeValue(new CompositeValueModel().put(RESOURCE_LOCKED_URI, isLocked),
                     clientState, new Directives());
             Association contributor = assignCoAuthorship(resource, user, clientState);
             if (contributor == null) log.info("Skipped adding co-authorship for resource ("
                     + resource.getId() + ") to author " + user.getSimpleValue());
             dms.updateTopic(resource.getModel(), clientState);
             tx.success();
             return resource;
         } catch (Exception e) {
             throw new WebApplicationException(new RuntimeException("Something went wrong while updating resource", e));
         } finally {
             tx.finish();
             // if (notificationService != null) notificationService.notify("Beitrag bearbeitet", "", user.getId(), resource);
         }
     }
 
     /**
      * Initializes the public main-timeline, fetching all public \"Resource\"-Topics.
      */
 
     @GET
     @Path("/notes/fetch/{count}/{offset}")
     @Produces("application/json")
     @Override
     public String getResources(@PathParam("count") long size, @PathParam("offset") long from) {
         //
         JSONArray results = new JSONArray();
         try {
             // 1) Fetch Resultset of Resources
             ResultList<RelatedTopic> all_resources = dms.getTopics(RESOURCE_URI, false, 0);
             log.info("> ResourcePlugin fetched " +all_resources.getSize()+ " resources (" +from+ ", "+(from+size)+")");
             // 2) Sort and fetch resources
             ArrayList<RelatedTopic> in_memory_resources = getResultSetSortedByCreationTime(all_resources);
             // fixme: throw error if page is unexpected high or NaN
             int count = 0;
             for (RelatedTopic item : in_memory_resources) { // 2) prepare resource items
                 // 3) start preparing page results
                 if (count >= from) {
                     item.loadChildTopics(RESOURCE_CONTENT_URI);
                     item.loadChildTopics(TAG_URI);
                     item.loadChildTopics(REVIEW_URI);
                     enrichTopicModelAboutCreator(item);
                     //
                     enrichTopicModelAboutCreationTimestamp(item);
                     enrichTopicModelAboutModificationTimestamp(item);
                     //
                     results.put(item.toJSON());
                     if (results.length() == size) break;
                 }
                 count++;
             }
             // 4) Check if Moodle-Plugin is present
             if (dms.getPlugin("org.deepamehta.moodle-plugin") != null) {
                 // 5) Fetch Resultset of \"Moodle Items\"
                 ResultList<RelatedTopic> my_moodle_items = getAllMyMoodleItems(); // restrict to current user + courses
                 if (my_moodle_items == null) return results.toString(); // user is most probably not logged in
                 ArrayList<RelatedTopic> all_items = getMoodleResultSetSortedByModificationTime(my_moodle_items);
                 // 6) Sort and fetch moodle-items
                 count = 0;
                 for (RelatedTopic moodle_item : all_items) { // 7) prepare (some size*2) fetched moodle items
                     // if (count >= from) {
                         // prepare SIZE moodle items
                         moodle_item.loadChildTopics(MOODLE_ITEM_ICON_URI);
                         moodle_item.loadChildTopics(MOODLE_ITEM_MODIFIED_URI);
                         // moodle_item.loadChildTopics(MOODLE_ITEM_TYPE_URI);
                         // moodle_item.loadChildTopics(MOODLE_ITEM_CREATED_URI);
                         // moodle_item.loadChildTopics(MOODLE_ITEM_LICENSE_URI);
                         moodle_item.loadChildTopics(MOODLE_ITEM_HREF_URI);
                         moodle_item.loadChildTopics(MOODLE_ITEM_DESC_URI);
                         moodle_item.loadChildTopics(MOODLE_ITEM_REMOTE_URL_URI);
                         moodle_item.loadChildTopics(TAG_URI);
                         moodle_item.loadChildTopics(REVIEW_URI);
                         // enrichTopicModelAboutCreator(moodle_item);
                         results.put(moodle_item.toJSON());
                         if (results.length() == size * 3) break;
                     // }
                     // count++;
                 }
             }
         } catch (Exception e) { // e.g. a "RuntimeException" is thrown if the moodle-plugin is not installed
             throw new WebApplicationException(new RuntimeException("something went wrong", e));
         } finally {
             // Note: with or without moodle-plugin, return our json-array (resultset)
             return results.toString();
         }
     }
 
     /**
      * Fetching resources by time-range and time-value (created || modified).
      */
 
     @GET
     @Path("/notes/by_time/{time_value}/{from}/{to}")
     @Produces("application/json")
     // @Override
     public String getNotesInTimeRange(@PathParam("time_value") String type, @PathParam("from") long from,
             @PathParam("to") long to) {
         //
         JSONArray results = new JSONArray();
         try {
             // 1) Fetch Resultset of Resources
             log.info("> ResourcePlugin fetching resources in timerange from \"" + from + "\" to \"" + to + "\"");
             ArrayList<Topic> resources_in_range = new ArrayList<Topic>();
             Collection<Topic> topics_in_range = null;
             if (type.equals("created")) {
                 topics_in_range = timeService.getTopicsByCreationTime(from, to);
             } else if (type.equals("modified")) {
                 topics_in_range = timeService.getTopicsByModificationTime(from, to);
             } else {
                 return "Wrong parameter: set time_value to \"created\" or \"modified\"";
             }
             if (topics_in_range.isEmpty()) log.info("getNotesInTimeRange ("+type+") found no result.");
             log.info("Identified " + topics_in_range.size() + " topics in range");
             Iterator<Topic> resultset = topics_in_range.iterator();
             while (resultset.hasNext()) {
                 Topic in_question = resultset.next();
                 if (in_question.getTypeUri().equals(RESOURCE_URI)) {
                     resources_in_range.add(in_question);
                 } else if (in_question.getTypeUri().equals(MOODLE_ITEM_URI)) {
                     resources_in_range.add(in_question);
                 } else {
                     log.info("> Result \"" +in_question.getSimpleValue()+ "\" (" +in_question.getTypeUri()+ ")");
                 }
             }
             log.info("> ResourcePlugin fetched " +resources_in_range.size()+ " resources (" + from + ", " + to + ")"
                     + " by time");
             // 2) Sort and fetch resources
             // ArrayList<RelatedTopic> in_memory_resources = getResultSetSortedByCreationTime(all_resources);
             for (Topic item : resources_in_range) { // 2) prepare resource items
                 // 3) Prepare the notes page-results view-model
                 if (item.getTypeUri().equals(MOODLE_ITEM_URI)) {
                     item.loadChildTopics(MOODLE_ITEM_DESC_URI);
                     item.loadChildTopics(MOODLE_ITEM_REMOTE_URL_URI);
                     item.loadChildTopics(MOODLE_ITEM_HREF_URI);
                     item.loadChildTopics(MOODLE_ITEM_ICON_URI);
                 } else if (item.getTypeUri().equals(RESOURCE_URI)) {
                     item.loadChildTopics(RESOURCE_CONTENT_URI);
                 }
                 //
                 item.loadChildTopics(TAG_URI);
                 item.loadChildTopics(REVIEW_URI);
                 enrichTopicModelAboutCreator(item);
                 //
                 enrichTopicModelAboutCreationTimestamp(item);
                 enrichTopicModelAboutModificationTimestamp(item);
                 //
                 results.put(item.toJSON());
             }
             // TODO: Add \"Moodle Items\" as items in question
         } catch (Exception e) { // e.g. a "RuntimeException" is thrown if the moodle-plugin is not installed
             throw new WebApplicationException(new RuntimeException("something went wrong", e));
         } finally {
             // Note: with or without moodle-plugin, return our json-array (resultset)
             return results.toString();
         }
     }
 
     /**
      * Getting {"value", "type_uri", "id" and "dm4.time.created"} values of (interesting) topics in range.
      */
 
     @GET
     @Path("/notes/index/{from}/{to}")
     @Produces("application/json")
     public String getIndexForTimeRange(@PathParam("from") long from, @PathParam("to") long to) {
         //
         JSONArray results = new JSONArray();
         try {
             // 1) Fetch Resultset of Resources
             log.info("Fetching all topics in timerange from \"" + from + "\" to \"" + to + "\"");
             ArrayList<Topic> all_in_range = new ArrayList<Topic>();
             Collection<Topic> topics_in_range = timeService.getTopicsByCreationTime(from, to);
             log.info("Identified " + topics_in_range.size() + " topics in range");
             Iterator<Topic> resultset = topics_in_range.iterator();
             while (resultset.hasNext()) {
                 Topic in_question = resultset.next();
                 log.info("> " +in_question.getSimpleValue()+ " of type \"" +in_question.getTypeUri()+ "\"");
                 if (in_question.getTypeUri().equals(RESOURCE_URI) ||
                     in_question.getTypeUri().equals(MOODLE_ITEM_URI) ||
                     in_question.getTypeUri().equals(MOODLE_COURSE_URI) ||
                     in_question.getTypeUri().equals(TAG_URI) ||
                     in_question.getTypeUri().equals(DEEPAMEHTA_FILE_URI)) {
                     all_in_range.add(in_question);
                 }
             }
             log.info(">>> Fetched " +all_in_range.size()+ " resources (" + from + ", " + to + ")" + " by time");
             // 2) Sort and fetch resources
             // ArrayList<RelatedTopic> in_memory_resources = getResultSetSortedByCreationTime(all_resources);
             for (Topic item : all_in_range) { // 2) prepare resource items
                 // 3) Prepare the notes page-results view-model
                 enrichTopicModelAboutCreationTimestamp(item);
                 enrichTopicModelAboutModificationTimestamp(item);
                 results.put(item.toJSON());
             }
         } catch (Exception e) { // e.g. a "RuntimeException" is thrown if the moodle-plugin is not installed
             throw new WebApplicationException(new RuntimeException("something went wrong", e));
         } finally {
             // Note: with or without moodle-plugin, return our json-array (resultset)
             return results.toString();
         }
     }
 
     @GET
     @Path("/resources/{tagId}")
     @Produces("application/json")
     @Override
     public ArrayList<RelatedTopic> getResourcesByTagAndTypeURI(@PathParam("tagId") long tagId,
             @HeaderParam("Cookie") ClientState clientState) {
         ArrayList<RelatedTopic> response = new ArrayList<RelatedTopic>();
         try {
             // 1) Add all ordinary tagged \"Resources\" into our resultset
             for (RelatedTopic resource : taggingService.getTopicsByTagAndTypeURI(tagId, RESOURCE_URI, clientState)) {
                 response.add(resource);
             }
             // 2) Fetch all tagged "Moodle Items" for our resultset
             if (dms.getPlugin("org.deepamehta.moodle-plugin") != null) {
                 ArrayList<RelatedTopic> tagged_moodle_items = new ArrayList<RelatedTopic>();
                 // 1) Load all (personal) \"Moodle Items\" tagged with this tagId
                 ResultList<RelatedTopic> my_moodle_items = getAllMyMoodleItems();
                 // .. which are null if user is not logged in / has no moodle-security key/ moodle user id set
                 if (my_moodle_items != null) {
                     // 2) Check for each item if it has this (one) tag
                     for (RelatedTopic moodle_item : my_moodle_items) {
                         // 3) Load Tags of this composite
                         moodle_item.loadChildTopics(TAG_URI);
                         // 4) Now check for tags
                         if (moodle_item.getCompositeValue().has(TAG_URI)) {
                             List<Topic> tags = moodle_item.getCompositeValue().getTopics(TAG_URI);
                             for (Topic tag : tags) {
                                 if (tag.getId() == tagId) {
                                     // 5) prepare moodle items to become a prope notes result-list
                                     moodle_item.loadChildTopics(MOODLE_ITEM_ICON_URI);
                                     moodle_item.loadChildTopics(MOODLE_ITEM_MODIFIED_URI);
                                     moodle_item.loadChildTopics(MOODLE_ITEM_HREF_URI);
                                     moodle_item.loadChildTopics(MOODLE_ITEM_DESC_URI);
                                     moodle_item.loadChildTopics(MOODLE_ITEM_REMOTE_URL_URI);
                                     moodle_item.loadChildTopics(REVIEW_URI);
                                     tagged_moodle_items.add(moodle_item);
                                 }
                             }
                         }
                     }
                     response.addAll(tagged_moodle_items);
                 }
             }
         } catch (Exception e) {
             throw new WebApplicationException(new RuntimeException("Something went wrong fetching tagged resources "
                     + "(by one).", e));
         } finally {
             return response;
         }
     }
 
     @POST
     @Path("/resources/by_many")
     @Consumes("application/json")
     @Produces("application/json")
     public ArrayList<RelatedTopic> getResourcesByTagsAndTypeUri(String tag_body,
             @HeaderParam("Cookie") ClientState clientState) {
         ArrayList<RelatedTopic> response = new ArrayList<RelatedTopic>();
         try {
             ResultList<RelatedTopic> my_moodle_items = null;
             JSONObject tagList = new JSONObject(tag_body);
             // 0) Check parameters
             if (tagList.has("tags")) {
                 JSONArray all_tags = tagList.getJSONArray("tags");
                 // 1) in this case, pass the request on to a more simple implementation
                 if (all_tags.length() == 0) {
                     throw new WebApplicationException(new RuntimeException("no tags given"));
                 } else if (all_tags.length() == 1) {
                     // fixme: all_tags provided may be < 0
                     JSONObject tagOne = all_tags.getJSONObject(0);
                     long first_id = tagOne.getLong("id");
                     response = getResourcesByTagAndTypeURI(first_id, clientState);
                 // 2) if this is called with more than 1 tag, we accept the request
                 } else {
                     // 3) Add all ordinary tagged \"Resources\" into our resultset too.
                     ResultList<RelatedTopic> resources = taggingService.getTopicsByTagsAndTypeUri(tag_body,
                             RESOURCE_URI, clientState);
                     for (RelatedTopic resource : resources) {
                         response.add(resource);
                     }
                     // 4) Check if Moodle plugin is present
                     if (dms.getPlugin("org.deepamehta.moodle-plugin") != null) {
                         // 5) Load all (personal) \"Moodle Items\" associtaed with ALL the given tags
                         my_moodle_items = getAllMyMoodleItems();
                         if (my_moodle_items != null) {
                             Set<RelatedTopic> missmatches = new LinkedHashSet<RelatedTopic>();
                             Iterator<RelatedTopic> iterator = my_moodle_items.iterator();
                             while (iterator.hasNext()) {
                                 // mark each resource for removal which does not associate all given tags
                                 RelatedTopic resource = iterator.next();
                                 for (int i=1; i < all_tags.length(); i++) {
                                     JSONObject tag = all_tags.getJSONObject(i);
                                     long t_id = tag.getLong("id");
                                     if (!hasRelatedTopicTag(resource, t_id)) {
                                         // if just one tag is missing, mark for removal
                                         missmatches.add(resource);
                                     }
                                 }
                             }
                             // 6) build up the final result set through removing any non-matching item
                             for (Iterator<RelatedTopic> it = missmatches.iterator(); it.hasNext();) {
                                 RelatedTopic topic = it.next();
                                 my_moodle_items.getItems().remove(topic);
                             }
                             // 7) prepare moodle items to become a prope notes result-list
                             for (RelatedTopic moodle_item : my_moodle_items) {
                                 moodle_item.loadChildTopics(MOODLE_ITEM_ICON_URI);
                                 moodle_item.loadChildTopics(MOODLE_ITEM_MODIFIED_URI);
                                 moodle_item.loadChildTopics(MOODLE_ITEM_HREF_URI);
                                 moodle_item.loadChildTopics(MOODLE_ITEM_DESC_URI);
                                 moodle_item.loadChildTopics(MOODLE_ITEM_REMOTE_URL_URI);
                                 moodle_item.loadChildTopics(REVIEW_URI);
                                 response.add(moodle_item);
                             }
                         }
                     }
                 }
             }
         } catch (Exception e) {
             log.warning("" + e.getCause().toString() + ":" + e.getMessage().toString());
         } finally {
             return response;
         }
     }
 
     @GET
     @Path("/notes/fetch/contributions/{userId}")
     @Produces("application/json")
     @Override
     public String getContributedResources(@PathParam("userId") long userId,
             @HeaderParam("Cookie") ClientState clientState) {
         //
         JSONArray results = new JSONArray();
         try {
             Topic user  = dms.getTopic(userId, true);
             ResultList<RelatedTopic> all_results = fetchAllContributionsByUser(user);
             log.info("fetching " +all_results.getSize()+ " contributions by user " + user.getSimpleValue());
             for (RelatedTopic item : all_results) {
                 enrichTopicModelAboutCreator(item);
                 results.put(item.toJSON());
             }
             return results.toString();
         } catch (Exception e) {
             throw new WebApplicationException(new RuntimeException("something went wrong", e));
         }
     }
 
     @Override
     public ResultList<RelatedTopic> getAllMyMoodleItems() {
         String username = aclService.getUsername();
         if (username == null) return null;
         Topic user = dms.getTopic("dm4.accesscontrol.username", new SimpleValue(username), false);
         Topic account = user.getRelatedTopic(COMPOSITION_TYPE_URI, CHILD_URI, PARENT_URI,
                 "dm4.accesscontrol.user_account", false, false);
         // log.info("Fetching all Moodle Items for user \""+account.getSimpleValue()+"\" (" +account.getId()+ ")");
         ResultList<RelatedTopic> enroled_courses = account.getRelatedTopics(MOODLE_PARTICIPANT_EDGE,
                 DEFAULT_ROLE_TYPE_URI, DEFAULT_ROLE_TYPE_URI, MOODLE_COURSE_URI, false, false, 0);
         ResultList<RelatedTopic> resultset = new ResultList<RelatedTopic>();
         for (RelatedTopic course : enroled_courses) {
             // log.info(" in Moodle Course \""+course.getSimpleValue()+"\"");
             ResultList<RelatedTopic> sections = course.getRelatedTopics(AGGREGATION_TYPE_URI, PARENT_URI, CHILD_URI,
                     MOODLE_SECTION_URI, false, false, 0);
             if (sections != null) {
                 for (RelatedTopic section : sections) {
                     ResultList<RelatedTopic> items = section.getRelatedTopics(AGGREGATION_TYPE_URI, PARENT_URI,
                             CHILD_URI, MOODLE_ITEM_URI, false, false, 0);
                     if (items != null) resultset.addAll(items);
                 }
             }
         }
         //
         return resultset;
     }
 
 
 
     // --
     // --- Private Helper Methods
     // --
 
 
 
     private void enrichTopicModelAboutCreator (Topic resource) {
         // enriching our sorted resource-results on-the-fly about some minimal user-info
         // ### currently this method fails silently if resource has no creator-relationship
         CompositeValueModel compositeModel = resource.getCompositeValue().getModel();
         Topic creator = fetchCreator(resource);
         // if (creator == null) throw new RuntimeException("Resource (" +resource.getId()+ ") has NO CREATOR set!");
         if (creator == null) return;
         String display_name = creator.getSimpleValue().toString();
         if (creator.getCompositeValue().has(IDENTITY_NAME_TYPE_URI) &&
             !creator.getCompositeValue().getString(IDENTITY_NAME_TYPE_URI).equals("")) { // it may be present but empty?
             display_name = creator.getCompositeValue().getString(IDENTITY_NAME_TYPE_URI);
         }
         TopicModel identity;
         try {
             identity = new TopicModel(ACCOUNT_TYPE_URI, new CompositeValueModel(new JSONObject("{ "
                 + "\"" + IDENTITY_NAME_TYPE_URI + "\": \"" + display_name + "\", "
                 + "}")));
             //
             identity.setId(creator.getId());
             identity.setSimpleValue(display_name);
             compositeModel.put(ACCOUNT_TYPE_URI, identity);
         } catch (JSONException ex) {
             Logger.getLogger(ResourcePlugin.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private void enrichTopicModelAboutCreationTimestamp (Topic resource) {
         long created = timeService.getCreationTime(resource);
         CompositeValueModel resourceModel = resource.getCompositeValue().getModel();
         resourceModel.put(PROP_URI_CREATED, created);
     }
 
     private void enrichTopicModelAboutModificationTimestamp (Topic resource) {
         long created = timeService.getModificationTime(resource);
         CompositeValueModel resourceModel = resource.getCompositeValue().getModel();
         resourceModel.put(PROP_URI_MODIFIED, created);
     }
 
     private Topic fetchCreator(Topic resource) {
         //
         return resource.getRelatedTopic(CREATOR_EDGE_URI, PARENT_URI,
                 CHILD_URI, ACCOUNT_TYPE_URI, true, false);
     }
 
     /**  todo: provide this method as a service for the MoodleServiceClient..*/
     private Association assignAuthorship(Topic resource, Topic user, ClientState clientState) {
         if (associationExists(CREATOR_EDGE_URI, resource, user)) return null;
         return dms.createAssociation(new AssociationModel(CREATOR_EDGE_URI,
                 new TopicRoleModel(resource.getId(), PARENT_URI),
                 new TopicRoleModel(user.getId(), CHILD_URI)), clientState);
     }
 
     private Association assignCoAuthorship(Topic resource, Topic user, ClientState clientState) {
         if (associationExists(CREATOR_EDGE_URI, resource, user) ||
             associationExists(CONTRIBUTOR_EDGE_URI, resource, user)) return null;
         return dms.createAssociation(new AssociationModel(CONTRIBUTOR_EDGE_URI,
                 new TopicRoleModel(resource.getId(), PARENT_URI),
                 new TopicRoleModel(user.getId(), CHILD_URI)), clientState);
     }
 
     private ResultList<RelatedTopic> fetchAllContributionsByUser(Topic user) {
         //
         ResultList<RelatedTopic> all_resources = null;
         all_resources = user.getRelatedTopics(CREATOR_EDGE_URI, CHILD_URI,
                 PARENT_URI, RESOURCE_URI, true, false, 0);
         all_resources.addAll(user.getRelatedTopics(CONTRIBUTOR_EDGE_URI, CHILD_URI,
                 PARENT_URI, RESOURCE_URI, true, false, 0));
         return all_resources;
     }
 
     private ArrayList<RelatedTopic> getResultSetSortedByCreationTime (ResultList<RelatedTopic> all) {
         // build up sortable collection of all result-items
         ArrayList<RelatedTopic> in_memory = new ArrayList<RelatedTopic>();
         for (RelatedTopic obj : all) {
             in_memory.add(obj);
         }
         // sort all result-items
         Collections.sort(in_memory, new Comparator<RelatedTopic>() {
             public int compare(RelatedTopic t1, RelatedTopic t2) {
                 try {
                     Object one = t1.getProperty(PROP_URI_CREATED);
                     Object two = t2.getProperty(PROP_URI_CREATED);
                     if ( Long.parseLong(one.toString()) < Long.parseLong(two.toString()) ) return 1;
                     if ( Long.parseLong(one.toString()) > Long.parseLong(two.toString()) ) return -1;
                 } catch (Exception nfe) {
                     log.warning("Error while accessing timestamp of Topic 1: " + t1.getId() + " Topic2: "
                             + t2.getId() + " nfe: " + nfe.getMessage());
                     return 0;
                 }
                 return 0;
             }
         });
         return in_memory;
     }
 
     private ArrayList<RelatedTopic> getMoodleResultSetSortedByModificationTime (ResultList<RelatedTopic> all) {
         // build up sortable collection of all result-items
         ArrayList<RelatedTopic> in_memory = new ArrayList<RelatedTopic>();
         for (RelatedTopic obj : all) {
             obj.loadChildTopics(MOODLE_ITEM_MODIFIED_URI);
             in_memory.add(obj);
         }
         // sort all result-items
         Collections.sort(in_memory, new Comparator<RelatedTopic>() {
             public int compare(RelatedTopic t1, RelatedTopic t2) {
                 long one = t1.getCompositeValue().getLong(MOODLE_ITEM_MODIFIED_URI, 0);
                 long two = t2.getCompositeValue().getLong(MOODLE_ITEM_MODIFIED_URI, 0);
                 if ( one < two ) return 1;
                 if ( one > two ) return -1;
                 return 0;
             }
         });
         return in_memory;
     }
 
     private Topic getAuthorizedUser() {
         String logged_in_user = aclService.getUsername();
         if (logged_in_user.equals("")) throw new WebApplicationException(401);
         Topic username = aclService.getUsername(logged_in_user);
         return username.getRelatedTopic(COMPOSITION_TYPE_URI, CHILD_URI, PARENT_URI,
                 ACCOUNT_TYPE_URI, true, false);
     }
 
     private boolean hasRelatedTopicTag(RelatedTopic resource, long tagId) {
         CompositeValueModel topicModel = resource.getCompositeValue().getModel();
         if (topicModel.has(TAG_URI)) {
             List<TopicModel> tags = topicModel.getTopics(TAG_URI);
             for (int i = 0; i < tags.size(); i++) {
                 TopicModel resourceTag = tags.get(i);
                 if (resourceTag.getId() == tagId) return true;
             }
         }
         return false;
     }
 
     private boolean associationExists(String edge_type, Topic item, Topic user) {
         List<Association> results = dms.getAssociations(item.getId(), user.getId(), edge_type);
         return (results.size() > 0) ? true : false;
     }
 
     private String getUserDisplayName(long userId) {
         Topic user = dms.getTopic(userId, true);
         CompositeValueModel comp = user.getModel().getCompositeValueModel();
         if (comp.has(IDENTITY_NAME_TYPE_URI)) {
             return comp.getString(IDENTITY_NAME_TYPE_URI);
         }
         return user.getSimpleValue().toString();
     }
 
     private String getUserProfilePicturePath(long userId) {
         Topic user = dms.getTopic(userId, true);
         Topic picture = user.getRelatedTopic(PROFILE_PICTURE_EDGE_URI, DEFAULT_ROLE_TYPE_URI, DEFAULT_ROLE_TYPE_URI,
                 DEEPAMEHTA_FILE_URI, true, true);
         if (picture != null) return picture.getModel().getCompositeValueModel().getString(DEEPAMEHTA_FILE_PATH_URI);
         return null;
     }
 
     private String getUsersDescription(long userId) {
         Topic user = dms.getTopic(userId, true);
         CompositeValueModel comp = user.getModel().getCompositeValueModel();
         if (comp.has(IDENTITY_INFOS_TYPE_URI)) {
             return comp.getString(IDENTITY_INFOS_TYPE_URI);
         }
         return "&Ouml;ffentliche Notizen";
     }
 
     // --
     // --- Service Listeners
     // --
 
     //         "org.deepamehta.plugins.subscriptions.service.SubscriptionService",
     @Override
     @ConsumesService({
         "de.deepamehta.plugins.accesscontrol.service.AccessControlService",
         "de.deepamehta.plugins.tags.service.TaggingService",
         "de.deepamehta.plugins.time.service.TimeService"
     })
     public void serviceArrived(PluginService service) {
         if (service instanceof AccessControlService) {
             aclService = (AccessControlService) service;
         } else if (service instanceof TaggingService) {
             taggingService = (TaggingService) service;
         } else if (service instanceof TimeService) {
             timeService = (TimeService) service;
         } /**  else if (service instanceof SubscriptionService) {
             notificationService = (SubscriptionService) service;
         } **/
     }
 
     //        "org.deepamehta.plugins.subscriptions.service.SubscriptionService",
     @Override
     @ConsumesService({
         "de.deepamehta.plugins.accesscontrol.service.AccessControlService",
         "de.deepamehta.plugins.tags.service.TaggingService",
         "de.deepamehta.plugins.time.service.TimeService"
     })
     public void serviceGone(PluginService service) {
         if (service == aclService) {
             aclService = null;
         } else if (service == taggingService) {
             taggingService = null;
         } else if (service == timeService) {
             timeService = null;
         } /** else if (service == notificationService) {
             notificationService = null;
         } **/
     }
 
 }
