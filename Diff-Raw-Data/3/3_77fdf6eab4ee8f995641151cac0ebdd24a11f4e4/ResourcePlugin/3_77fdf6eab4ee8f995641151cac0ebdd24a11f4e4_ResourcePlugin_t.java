 package org.deepamehta.plugins.eduzen;
 
 import java.util.logging.Logger;
 import java.util.List;
 import java.util.Date;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.WebApplicationException;
 
 import de.deepamehta.core.Topic;
 import de.deepamehta.core.model.TopicModel;
 import de.deepamehta.core.RelatedTopic;
 import de.deepamehta.core.model.CompositeValueModel;
 import de.deepamehta.core.service.ClientState;
 import de.deepamehta.core.service.Directives;
 import de.deepamehta.core.storage.spi.DeepaMehtaTransaction;
 import de.deepamehta.plugins.webactivator.WebActivatorPlugin;
 
 import org.deepamehta.plugins.eduzen.service.ResourceService;
 
 import com.sun.jersey.api.view.Viewable;
 import com.sun.jersey.spi.container.ContainerResponse;
 import de.deepamehta.core.Association;
 import de.deepamehta.core.model.AssociationModel;
 import de.deepamehta.core.model.TopicRoleModel;
 import de.deepamehta.core.service.PluginService;
 import de.deepamehta.core.service.ResultList;
 import de.deepamehta.core.service.annotation.ConsumesService;
 import de.deepamehta.core.service.event.PreSendTopicListener;
 import de.deepamehta.core.service.event.ServiceResponseFilterListener;
 import de.deepamehta.plugins.accesscontrol.service.AccessControlService;
 import de.deepamehta.plugins.topicmaps.model.TopicViewmodel;
 import de.deepamehta.plugins.topicmaps.model.TopicmapViewmodel;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.*;
 import java.util.logging.Level;
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 
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
 
     private final static String CREATOR_EDGE_URI = "org.deepamehta.resources.creator_edge";
     private final static String CONTRIBUTOR_EDGE_URI = "org.deepamehta.resources.contributor_edge";
     private final static String CHILD_TYPE_URI = "dm4.core.child";
     private final static String PARENT_TYPE_URI = "dm4.core.parent";
     private final static String DEFAULT_ROLE_TYPE_URI = "dm4.core.default";
     private final static String COMPOSITION_TYPE_URI = "dm4.core.composition";
     private final static String ACCOUNT_TYPE_URI = "dm4.accesscontrol.user_account";
     private final static String IDENTITY_NAME_TYPE_URI = "org.deepamehta.identity.display_name";
     private final static String IDENTITY_INFOS_TYPE_URI = "org.deepamehta.identity.infos";
     private final static String PROFILE_PICTURE_EDGE_URI = "org.deepamehta.identity.profile_picture_edge";
     private final static String DEEPAMEHTA_FILE_URI = "dm4.files.file";
     private final static String DEEPAMEHTA_FILE_PATH_URI = "dm4.files.path";
 
     private AccessControlService acService = null;
 
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
                             // log.info("Rewriting label just for Resources in all Topicmaps to: " + topic.getSimpleValue());
                         } catch (NumberFormatException nex) {
                             log.warning("Resource label is no timestamp, skipping fancy label-rewrite.");
                         }
                     }
                 }
             }
         }
     }
 
     @Override
     @ConsumesService("de.deepamehta.plugins.accesscontrol.service.AccessControlService")
     public void serviceArrived(PluginService service) {
         if (service instanceof AccessControlService) {
             acService = (AccessControlService) service;
         }
     }
 
     @Override
     @ConsumesService("de.deepamehta.plugins.accesscontrol.service.AccessControlService")
     public void serviceGone(PluginService service) {
         if (service == acService) {
             acService = null;
         }
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
         Topic user = getAuthorizedUser();
         try {
             String value = topicModel.getCompositeValueModel().getString(RESOURCE_CONTENT_URI);
             // skipping: check in htmlContent for <script>-tag
             // enrich new topic about content
             topicModel.getCompositeValueModel().put(RESOURCE_CONTENT_URI, value);
             // enrich new topic about timestamps
             long createdAt = new Date().getTime();
             topicModel.getCompositeValueModel().put(RESOURCE_CREATED_AT_URI, createdAt);
             topicModel.getCompositeValueModel().put(RESOURCE_LAST_MODIFIED_URI, createdAt);
             topicModel.getCompositeValueModel().put(RESOURCE_PUBLISHED_URI, true);
             topicModel.getCompositeValueModel().put(RESOURCE_LOCKED_URI, false);
             topicModel.getCompositeValueModel().putRef(RESOURCE_LICENSE_URI, RESOURCE_LICENSE_UNSPECIFIED_URI);
             topicModel.getCompositeValueModel().putRef(RESOURCE_LICENSE_AREA_URI, RESOURCE_LICENSE_UNKNOWN_URI);
             // topicModel.getCompositeValueModel().putRef(RESOURCE_AUTHOR_NAME_URI, RESOURCE_AUTHOR_ANONYMOUS_URI);
             // create new topic
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
             // Directives updateDirective = dms.updateTopic(topic, clientState);
             // dms.updateTopic() - most high-level model
             // resource.update(topic, clientState, updateDirective); // id, overriding model
             resource.setCompositeValue(new CompositeValueModel().put(RESOURCE_CONTENT_URI, value),
                     clientState, new Directives()); // why new Directives on an AttachedObject
             resource.setCompositeValue(new CompositeValueModel().put(RESOURCE_LAST_MODIFIED_URI, modifiedAt),
                     clientState, new Directives());
             resource.setCompositeValue(new CompositeValueModel().put(RESOURCE_LOCKED_URI, isLocked),
                     clientState, new Directives());
             Association contributor = assignCoAuthorship(resource, user, clientState);
             if (contributor == null) log.info("Skipped adding co-authorship for resource ("
                     + resource.getId() + ") to author " + user.getSimpleValue());
             // ### update timestamp of super-topic
             dms.updateTopic(resource.getModel(), clientState);
             tx.success();
             return resource;
         } catch (Exception e) {
             throw new WebApplicationException(new RuntimeException("Something went wrong while updating resource", e));
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Fetches all resources with one given <code>Tag</code>.
      *
      */
 
     @GET
     @Path("/notes/fetch/{count}/{offset}")
     @Produces("application/json")
     @Override
     public String getResources(@PathParam("count") long size, @PathParam("offset") long from,
             @HeaderParam("Cookie") ClientState clientState) {
         //
         JSONArray results = new JSONArray();
         try {
             ResultList<RelatedTopic> all_results = dms.getTopics(RESOURCE_URI, false, 0);
             log.info("> fetching " +all_results.getSize()+ " resources for getting " + from + " to " + (from + size) );
             ArrayList<RelatedTopic> in_memory = getResultSetSortedByCreationTime(all_results, clientState);
             // fixme: throw error if page is unexpected high or NaN
             int count = 0;
             for (RelatedTopic item : in_memory) {
                 // start of preparing page results
                 if (count >= from) {
                     item.loadChildTopics(RESOURCE_CONTENT_URI);
                     item.loadChildTopics(TAG_URI);
                     item.loadChildTopics(REVIEW_URI);
                     enrichTopicModelAboutCreator(item);
                     results.put(item.toJSON());
                     if (results.length() == size) break;
                 }
                 count++;
                 // finished preparing page results
             }
             return results.toString();
         } catch (Exception e) {
             throw new WebApplicationException(new RuntimeException("something went wrong", e));
         }
     }
 
     @GET
     @Path("/notes/fetch/contributions/{userId}")
     @Produces("application/json")
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
 
     private Topic fetchCreator(Topic resource) {
         //
         return resource.getRelatedTopic(CREATOR_EDGE_URI, PARENT_TYPE_URI,
                 CHILD_TYPE_URI, ACCOUNT_TYPE_URI, true, false);
     }
 
     private Association assignAuthorship(Topic resource, Topic user, ClientState clientState) {
         if (associationExists(CREATOR_EDGE_URI, resource, user)) return null;
         return dms.createAssociation(new AssociationModel(CREATOR_EDGE_URI,
                 new TopicRoleModel(resource.getId(), PARENT_TYPE_URI),
                 new TopicRoleModel(user.getId(), CHILD_TYPE_URI)), clientState);
     }
 
     private Association assignCoAuthorship(Topic resource, Topic user, ClientState clientState) {
         if (associationExists(CREATOR_EDGE_URI, resource, user) ||
             associationExists(CONTRIBUTOR_EDGE_URI, resource, user)) return null;
         return dms.createAssociation(new AssociationModel(CONTRIBUTOR_EDGE_URI,
                 new TopicRoleModel(resource.getId(), PARENT_TYPE_URI),
                 new TopicRoleModel(user.getId(), CHILD_TYPE_URI)), clientState);
     }
 
     private ResultList<RelatedTopic> fetchAllContributionsByUser(Topic user) {
         //
         ResultList<RelatedTopic> all_resources = null;
         all_resources = user.getRelatedTopics(CREATOR_EDGE_URI, CHILD_TYPE_URI,
                 PARENT_TYPE_URI, RESOURCE_URI, true, false, 0);
         all_resources.addAll(user.getRelatedTopics(CONTRIBUTOR_EDGE_URI, CHILD_TYPE_URI,
                 PARENT_TYPE_URI, RESOURCE_URI, true, false, 0));
         return all_resources;
     }
 
     @GET
     @Produces("text/html")
     public Viewable getFrontView() {
         return view("index");
     }
 
     @GET
     @Path("/notes")
     @Produces("text/html")
     public Viewable getTimelineView() {
         viewData("name", "Notizen Timeline");
         viewData("path", "/notes");
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
         viewData("resourceName", "Notiz, zuletzt bearbeitet: " + new Date(lastModified).toString());
         viewData("relativePath", "/notes/" + resource.getId());
         viewData("style", "style.css");
         // boolean isLocked = resource.getModel().getCompositeValueModel().getBoolean(RESOURCE_LOCKED_URI);
         // context.setVariable("isLocked", isLocked);
         viewData("resourceId", resource.getId());
         return view("resource");
     }
 
     @GET
     @Path("/notes/{id}/print")
     @Produces("text/html")
     public Viewable getDetailPrintView(@PathParam("id") long resourceId, @HeaderParam("Cookie") ClientState clientState) {
         Topic resource = dms.getTopic(resourceId, true);
         long lastModified = resource.getModel().getCompositeValueModel().getLong(RESOURCE_LAST_MODIFIED_URI);
         viewData("resourceName", "Notiz, zuletzt bearbeitet: " + new Date(lastModified).toString());
         viewData("relativePath", "/notes/" + resource.getId());
         viewData("style", "detail-print.css");
         // boolean isLocked = resource.getModel().getCompositeValueModel().getBoolean(RESOURCE_LOCKED_URI);
         // context.setVariable("isLocked", isLocked);
         viewData("resourceId", resource.getId());
         return view("resource");
     }
 
     private ArrayList<RelatedTopic> getResultSetSortedByCreationTime (ResultList<RelatedTopic> all, ClientState clientState) {
         // build up sortable collection of all result-items
         ArrayList<RelatedTopic> in_memory = new ArrayList<RelatedTopic>();
         for (RelatedTopic obj : all) {
            obj.loadChildTopics(RESOURCE_CREATED_AT_URI);
             in_memory.add(obj);
         }
         // sort all result-items
         Collections.sort(in_memory, new Comparator<RelatedTopic>() {
             public int compare(RelatedTopic t1, RelatedTopic t2) {
                 long one = t1.getCompositeValue().getLong(RESOURCE_CREATED_AT_URI, 0);
                 long two = t2.getCompositeValue().getLong(RESOURCE_CREATED_AT_URI, 0);
                 if ( one < two ) return 1;
                 if ( one > two ) return -1;
                 return 0;
             }
         });
         return in_memory;
     }
 
     private Topic getAuthorizedUser() {
         String logged_in_user = acService.getUsername();
         if (logged_in_user.equals("")) throw new WebApplicationException(401);
         Topic username = acService.getUsername(logged_in_user);
         return username.getRelatedTopic(COMPOSITION_TYPE_URI, CHILD_TYPE_URI, PARENT_TYPE_URI,
                 ACCOUNT_TYPE_URI, true, false);
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
 
 }
