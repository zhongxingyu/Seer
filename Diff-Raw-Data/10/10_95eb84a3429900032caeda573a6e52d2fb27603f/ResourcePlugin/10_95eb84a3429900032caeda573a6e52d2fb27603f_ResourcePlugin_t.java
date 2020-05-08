 package org.deepamehta.plugins.eduzen;
 
 import java.util.logging.Logger;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.WebApplicationException;
 
 import de.deepamehta.core.Topic;
 import de.deepamehta.core.ResultSet;
 import de.deepamehta.core.model.TopicModel;
 import de.deepamehta.core.RelatedTopic;
 import de.deepamehta.core.model.CompositeValueModel;
 import de.deepamehta.core.service.ClientState;
 import de.deepamehta.core.service.Directives;
 import de.deepamehta.core.storage.spi.DeepaMehtaTransaction;
 import de.deepamehta.plugins.webactivator.WebActivatorPlugin;
 
 import org.deepamehta.plugins.eduzen.service.ResourceService;
 import org.deepamehta.plugins.eduzen.model.Resource;
 import org.deepamehta.plugins.eduzen.model.ResourceTopic;
 
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 import com.sun.jersey.api.view.Viewable;
 import de.deepamehta.core.service.Directive;
 import java.util.Date;
 /** import org.owasp.validator.html.AntiSamy;
 import org.owasp.validator.html.Policy;
 import org.owasp.validator.html.PolicyException; **/
 
 
 @Path("/notes")
 @Consumes("application/json")
 @Produces("text/html")
 public class ResourcePlugin extends WebActivatorPlugin implements ResourceService {
 
     private Logger log = Logger.getLogger(getClass().getName());
     // private AccessControlService acl;
     // private WorkspacesService ws;
 
     private final static String CHILD_URI = "dm4.core.child";
     private final static String PARENT_URI = "dm4.core.parent";
     private final static String AGGREGATION = "dm4.core.aggregation";
 
     private final static String RESOURCE_URI = "org.deepamehta.resources.resource";
     private final static String RESOURCE_CONTENT_URI = "org.deepamehta.resources.content";
     private final static String RESOURCE_CREATED_AT_URI = "org.deepamehta.resources.created_at";
     private final static String RESOURCE_LAST_MODIFIED_URI = "org.deepamehta.resources.last_modified_at";
     private final static String RESOURCE_PUBLISHED_URI = "org.deepamehta.resources.is_published";
     private final static String RESOURCE_LICENSE_URI = "org.deepamehta.resources.license";
     private final static String RESOURCE_LICENSE_UNSPECIFIED_URI = "org.deepamehta.licenses.unspecified";
     private final static String RESOURCE_LICENSE_UNKNOWN_URI = "org.deepamehta.licenses.unknown";
     private final static String RESOURCE_LICENSE_AREA_URI = "org.deepamehta.resources.license_jurisdiction";
     private final static String RESOURCE_AUTHOR_NAME_URI = "org.deepamehta.resources.author";
    private final static String RESOURCE_AUTHOR_ANONYMOUS_URI = "org.deepamehta.author.anonymous";
     private final static String TAG_URI = "dm4.tags.tag";
     private final static String SCORE_URI = "org.deepamehta.reviews.score";
 
     // private final static String POLICY_FILE_LOCATION = "antisamy-1.4.1.xml";
     // private AntiSamy antiSamy = null;
     // private Policy antiPolicy = null;
 
    // private static final String WS_EDUZEN_EDITORS = "de.workspaces.deepamehta";
     // private static final String WS_EDUZEN_USERS = "tub.eduzen.workspace_users";
 
     public ResourcePlugin() {
         log.info(".stdOut(\"Hello Zen-Content-Sharing-App!\")");
     }
 
     @Override
     public void init() {
         /** try {
             log.info("Hello World from ResourcePlugin trying to load an antiSamy.. Policy");
             // log.info("plugin is " + this.getResourceAsStream(POLICY_FILE_LOCATION));
             antiPolicy = Policy.getInstance(POLICY_FILE_LOCATION);
         } catch (PolicyException pe) {
             log.info(pe.getLocalizedMessage());
         }
         antiSamy = new AntiSamy(); **/
         setupRenderContext();
     }
 
     /**
      * Creates a new <code>Resource</code> instance based on the domain specific
      * REST call with a alternate JSON topic representation.
      */
 
     @POST
     @Path("/resource/create")
     @Produces("application/json")
     @Override
     public Topic createResource(TopicModel topicModel, @HeaderParam("Cookie") ClientState clientState) {
         DeepaMehtaTransaction tx = dms.beginTx();
         Topic resource = null;
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
             topicModel.getCompositeValueModel().putRef(RESOURCE_LICENSE_URI, RESOURCE_LICENSE_UNSPECIFIED_URI);
             topicModel.getCompositeValueModel().putRef(RESOURCE_LICENSE_AREA_URI, RESOURCE_LICENSE_UNKNOWN_URI);
            // topicModel.getCompositeValueModel().putRef(RESOURCE_AUTHOR_NAME_URI, RESOURCE_AUTHOR_ANONYMOUS_URI);
             // create new topic
             resource = dms.createTopic(topicModel, clientState); // clientstate is for workspace-assignment
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
     @Path("/resource/update")
     @Produces("application/json")
     @Override
     public Topic updateResource(TopicModel topic, @HeaderParam("Cookie") ClientState clientState) {
         DeepaMehtaTransaction tx = dms.beginTx();
         Topic resource = null;
         try {
             // check htmlContent for <script>-tag
             String value = topic.getCompositeValueModel().getString(RESOURCE_CONTENT_URI);
             // updated last_modified timestamp
             long modifiedAt = new Date().getTime();
             // update resource topic
             resource = dms.getTopic(topic.getId(), true, clientState);
             // Directives updateDirective = dms.updateTopic(topic, clientState);
             // dms.updateTopic() - most high-level model
             // resource.update(topic, clientState, updateDirective); // id, overriding model
             resource.setCompositeValue(new CompositeValueModel().put(RESOURCE_CONTENT_URI, value),
                     clientState, new Directives()); // why new Directives on an AttachedObject
             resource.setCompositeValue(new CompositeValueModel().put(RESOURCE_LAST_MODIFIED_URI, modifiedAt),
                     clientState, new Directives());
             tx.success();
             return resource;
         } catch (Exception e) {
             throw new WebApplicationException(new RuntimeException("Something went wrong while updating resource", e));
         } finally {
             tx.finish();
         }
     }
 
     /** Fetches all resources with one given <code>Tag</code> **/
 
     @GET
     @Path("/fetch/20/{from}")
     @Produces("application/json")
     @Override
     public ResultSet<RelatedTopic> getAllResources(@PathParam("from") long from, @HeaderParam("Cookie") ClientState clientState) {
         ResultSet<RelatedTopic> all_results = new ResultSet<RelatedTopic>();
         try {
             all_results = dms.getTopics(RESOURCE_URI, true, 0, clientState);
             log.info("tag has " +all_results.getSize()+ " resources..");
             return all_results;
         } catch (Exception e) {
             throw new WebApplicationException(new RuntimeException("something went wrong", e));
         }
     }
 
     /** Fetches all resources with one given <code>Tag</code> **/
 
     @GET
     @Path("/fetch/tag/{id}")
     @Produces("application/json")
     @Override
     public ResultSet<RelatedTopic> getResourcesByTag(@PathParam("id") long tagId, @HeaderParam("Cookie") ClientState clientState) {
         ResultSet<RelatedTopic> all_results = new ResultSet<RelatedTopic>();
         try {
             Topic givenTag = dms.getTopic(tagId, true, clientState);
             all_results = givenTag.getRelatedTopics(AGGREGATION, CHILD_URI,
                     PARENT_URI, RESOURCE_URI, true, false, 0, clientState);
             log.info("tag has " +all_results.getSize()+ " resources..");
             return all_results;
         } catch (Exception e) {
             throw new WebApplicationException(new RuntimeException("something went wrong", e));
         }
     }
 
 
     /** Fetches all resources with all given <code>Tag</code> **/
 
     @POST
     @Path("/fetch/tags/")
     @Consumes("application/json")
     @Produces("application/json")
     @Override
     public ResultSet<RelatedTopic> getResourcesByTags(String tags, @HeaderParam("Cookie") ClientState clientState) {
         // ResultSet<RelatedTopic> all_results = new ResultSet<RelatedTopic>();
         try {
             JSONObject tagList = new JSONObject(tags);
             if (tagList.has("tags")) {
                 // OK, filterin all resources starting by each tag
                 JSONArray all_tags = tagList.getJSONArray("tags");
                 if (all_tags.length() > 1) {
                     // if this is called with more than 1 tag, we accept the request
                     JSONObject tagOne = all_tags.getJSONObject(0);
                     long first_id = tagOne.getLong("id");
                     // get all resources for tag 1
                     // fixme: handle exception if the request gives us an assocId
                     Topic givenTag = dms.getTopic(first_id, true, clientState);
                     ResultSet<RelatedTopic> tag_resources = givenTag.getRelatedTopics(AGGREGATION, CHILD_URI,
                         PARENT_URI, RESOURCE_URI, true, false, 0, clientState);
                     Set<RelatedTopic> missmatches = new LinkedHashSet<RelatedTopic>();
                     Iterator<RelatedTopic> iterator = tag_resources.getIterator();
                     // ResultSet<RelatedTopic> removables = new LinkedHashSet<RelatedTopic>();
                     while (iterator.hasNext()) {
                         // mark each resource for later removal which does not contain all of the requested tags
                         RelatedTopic resource = iterator.next();
                         for (int i=1; i < all_tags.length(); i++) {
                             // check/reduce resultset .. // fixme: JSONArray is not an object.. should never happen!
                             JSONObject tag = all_tags.getJSONObject(i); // throws exception from time to time
                             long t_id = tag.getLong("id");
                             if (!hasRelatedTopicTag(resource, t_id)) {
                                 // if just one tag is missing, mark this resource for later removal
                                 missmatches.add(resource);
                             }
                         }
                     }
                     // log.info("overall the resultset of " + tag_resources.getSize() + "associated resources need "
                             // + "to be freed of " + missmatches.size() + " missmatched resources");
                     // build up the final result set
                     for (Iterator<RelatedTopic> it = missmatches.iterator(); it.hasNext();) {
                         RelatedTopic topic = it.next();
                         tag_resources.getItems().remove(topic);
                     }
                     return tag_resources;
                 } else {
                     // TODO: use getResourcesByTag() instead
                     throw new WebApplicationException(new RuntimeException("use /fetch/tag/{id} instead"));
                 }
             }
             throw new WebApplicationException(new RuntimeException("no tags given"));
         } catch (JSONException ex) {
             throw new WebApplicationException(new RuntimeException("error while parsing given parameters", ex));
         } catch (Exception e) {
             throw new WebApplicationException(new RuntimeException("something went wrong", e));
         }
     }
 
     /** Increments the score of the given resource. **/
 
     @GET
     @Path("/up/resource/{id}")
     @Produces("application/json")
     @Override
     public Topic upvoteResourceById(@PathParam("id") long resourceId, @HeaderParam("Cookie") ClientState clientState) {
         DeepaMehtaTransaction tx = dms.beginTx();
         Topic resource = null;
         try {
             resource = dms.getTopic(resourceId, true, clientState);
             int score = resource.getCompositeValue().getModel().getInt(SCORE_URI) + 1;
             resource.getCompositeValue().set(SCORE_URI, score, clientState, new Directives());
             tx.success();
         } catch (Exception e) {
             throw new WebApplicationException(new RuntimeException("something went wrong", e));
         } finally {
             tx.finish();
         }
         return resource;
     }
 
     /** Decrements the score of the given resource. **/
 
     @GET
     @Path("/down/resource/{id}")
     @Produces("application/json")
     @Override
     public Topic downvoteResourceById(@PathParam("id") long resourceId, @HeaderParam("Cookie") ClientState clientState) {
         DeepaMehtaTransaction tx = dms.beginTx();
         Topic resource = null;
         try {
             resource = dms.getTopic(resourceId, true, clientState);
             // check type definition if topics of any given type have a SCORE_URI
             int score = resource.getCompositeValue().getModel().getInt(SCORE_URI) - 1;
             resource.getCompositeValue().set(SCORE_URI, score, clientState, new Directives());
             tx.success();
         } catch (Exception e) {
             throw new WebApplicationException(new RuntimeException("something went wrong", e));
         } finally {
             tx.finish();
         }
         return resource;
     }
 
     @GET
     @Path("/")
     @Produces("text/html")
     public Viewable getFrontView() {
         return view("index");
     }
 
     @GET
     @Path("/info")
     @Produces("text/html")
     public Viewable getInfoView() {
         return view("info");
     }
 
     @GET
     @Path("/tagged/{tags}")
     @Produces("text/html")
     public Viewable getFilteredeTimelineView(@PathParam("tags") String tagFilter,
         @HeaderParam("Cookie") ClientState clientState) {
         context.setVariable("tags", tagFilter);
         // context.setVariable("posterText", "<p>Hallo Welt this is fat, sick and clickable yo!</p>");
         return view("index");
     }
 
     @GET
     @Path("/{id}")
     @Produces("text/html")
     public Viewable getDetailView(@PathParam("id") long resourceId, @HeaderParam("Cookie") ClientState clientState) {
         Topic resource = dms.getTopic(resourceId, true, clientState);
        String name = "" + resource.getId();
        context.setVariable("resourceName", name);
         context.setVariable("resourceId", resource.getId());
         return view("resource");
     }
 
 
 
     /** Private Helper Methods */
 
 
     private boolean hasRelatedTopicTag(RelatedTopic resource, long tagId) {
         CompositeValueModel topicModel = resource.getCompositeValue().getModel();
         if (topicModel.has(TAG_URI)) {
             List<TopicModel> tags = topicModel.getTopics(TAG_URI);
             for (int i = 0; i < tags.size(); i++) {
                 TopicModel resourceTag = tags.get(i);
                 // log.info("      searchedTag is " + tagId + " resourceTag is " + resourceTag.getId());
                 if (resourceTag.getId() == tagId) return true;
             }
         }
         return false;
     }
 
 }
