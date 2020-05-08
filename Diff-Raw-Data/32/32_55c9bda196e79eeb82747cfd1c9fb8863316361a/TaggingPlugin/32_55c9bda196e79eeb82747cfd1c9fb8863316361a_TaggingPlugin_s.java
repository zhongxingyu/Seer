 package de.deepamehta.plugins.tags;
 
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
 
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 import de.deepamehta.core.osgi.PluginActivator;
 import de.deepamehta.plugins.tags.service.TaggingService;
 
 
 /**
  * A basic plugin-service for fetching topics in DeepaMehta 4 by type and one or many tags.
  *
  * @author Malte Reißig (<malte@mikromedia.de>)
  * @website http://github.com/mukil/dm4.tags
  * @version 1.1
  *
  */
 
 @Path("/tag")
 @Consumes("application/json")
 @Produces("text/html")
 public class TaggingPlugin extends PluginActivator implements TaggingService {
 
     private Logger log = Logger.getLogger(getClass().getName());
 
     private final static String CHILD_URI = "dm4.core.child";
     private final static String PARENT_URI = "dm4.core.parent";
     private final static String AGGREGATION = "dm4.core.aggregation";
 
     private final static String TAG_URI = "dm4.tags.tag";
 
 
 
     /**
      * Fetches all topics of given type "aggregating" the "Tag" with the given <code>tagId</code>.
      *
      * @param {tagId}               An id ot a "dm4.tags.tag"-Topic
      * @param {relatedTypeUri}      A type_uri of a composite (e.g. "org.deepamehta.resources.resource")
      *                              which aggregates one or many "dm4.tags.tag".
      *
      * Note:                        This method provides actually no real benefit for developers familiar with the
      *                              getRelatedTopics() of the deepamehta-core API. It's just a convenient call.
      */
 
     @GET
     @Path("/{tagId}/{relatedTypeUri}")
     @Produces("application/json")
     @Override
     public ResultSet<RelatedTopic> getTopicsByTagAndTypeURI(@PathParam("tagId") long tagId,
         @PathParam("relatedTypeUri") String relatedTopicTypeUri, @HeaderParam("Cookie") ClientState clientState) {
         ResultSet<RelatedTopic> all_results = null;
         try {
             Topic givenTag = dms.getTopic(tagId, true, clientState);
             all_results = givenTag.getRelatedTopics(AGGREGATION, CHILD_URI,
                     PARENT_URI, relatedTopicTypeUri, true, false, 0, clientState);
             return all_results;
         } catch (Exception e) {
             throw new WebApplicationException(new RuntimeException("Something went wrong fetching tagged topics", e));
         }
     }
 
     /**
      * Fetches all topics of given type "aggregating" all given "Tag"-<code>Topics</code>.
      *
      * @param {relatedTypeUri}      A type_uri of a composite (e.g. "org.deepamehta.resources.resource")
      *                              which aggregates one or many "dm4.tags.tag".
      * @param {body}                A JSONObject containing JSONArray ("tags") of "Tag"-Topics is expected
      *                              (e.g. { "tags": [] }).
      */
 
     @POST
     @Path("/by_many/{relatedTypeUri}")
     @Consumes("application/json")
     @Produces("application/json")
     @Override
     public ResultSet<RelatedTopic> getTopicsByTagsAndTypeUri(String tags, @PathParam("relatedTypeUri")
             String relatedTopicTypeUri, @HeaderParam("Cookie") ClientState clientState) {
         ResultSet<RelatedTopic> tag_resources = null;
         try {
             JSONObject tagList = new JSONObject(tags);
             if (tagList.has("tags")) {
                // OK, filterin all resources starting by each tag
                 JSONArray all_tags = tagList.getJSONArray("tags");
                if (all_tags.length() > 1) {
                    // if this is called with more than 1 tag, we accept the request
                     JSONObject tagOne = all_tags.getJSONObject(0);
                     long first_id = tagOne.getLong("id");
                    // get all resources by the first "Tag"-Topic in line
                     Topic givenTag = dms.getTopic(first_id, true, clientState);
                     tag_resources = givenTag.getRelatedTopics(AGGREGATION, CHILD_URI,
                         PARENT_URI, relatedTopicTypeUri, true, false, 0, clientState);
                     Set<RelatedTopic> missmatches = new LinkedHashSet<RelatedTopic>();
                     Iterator<RelatedTopic> iterator = tag_resources.getIterator();
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
                     // build up the final result set
                     for (Iterator<RelatedTopic> it = missmatches.iterator(); it.hasNext();) {
                         RelatedTopic topic = it.next();
                         tag_resources.getItems().remove(topic);
                     }
                     return tag_resources;
                 } else {
                     // take the one only tag
                     JSONObject tagOne = all_tags.getJSONObject(0);
                     long first_id = tagOne.getLong("id");
                    // and pass it on to the designated service-method for fetching topics by a single tag
                    return getTopicsByTagAndTypeURI(first_id, relatedTopicTypeUri, clientState);
                 }
             }
             throw new WebApplicationException(new RuntimeException("no tags given"));
         } catch (JSONException ex) {
             throw new WebApplicationException(new RuntimeException("error while parsing given parameters", ex));
         } catch (Exception e) {
             throw new WebApplicationException(new RuntimeException("something went wrong", e));
         }
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
