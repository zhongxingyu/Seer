 package org.jboss.pressgang.ccms.rest.v1.jaxrsinterfaces;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.PathSegment;
 import javax.ws.rs.core.Response;
 import java.util.Map;
 
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTBlobConstantCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTCategoryCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTFileCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTFilterCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTImageCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTIntegerConstantCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTProjectCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTPropertyCategoryCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTPropertyTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTRoleCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTStringConstantCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTopicCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTranslatedTopicCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTUserCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTCSNodeCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTContentSpecCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTTextContentSpecCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTTranslatedCSNodeCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTTranslatedContentSpecCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.constants.RESTv1Constants;
 import org.jboss.pressgang.ccms.rest.v1.elements.RESTSystemStatsV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.*;
 import org.jboss.pressgang.ccms.rest.v1.elements.RESTServerSettingsV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTCSNodeV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTContentSpecV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTTextContentSpecV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTTranslatedCSNodeV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTTranslatedContentSpecV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.wrapper.IntegerWrapper;
 import org.jboss.pressgang.ccms.rest.v1.expansion.ExpandDataTrunk;
 
 @Path("/1")
 public interface RESTBaseInterfaceV1 {
     /* CONSTANTS */
 
     /* UTILITY FUNCTIONS */
     @GET
     @Path("/sysinfo/get/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTSystemStatsV1 getJSONSysInfo();
 
     @POST
     @Path("/minhash/get/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.APPLICATION_XML)
     Map<Integer, Integer> getMinHashes(final String xml);
 
     @POST
     @Path("/minhashsimilar/get/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.APPLICATION_XML)
     RESTTopicCollectionV1 getSimilarTopics(final String xml, @QueryParam("expand") final String expand, @QueryParam("threshold") final Float threshold);
 
     @POST
     @Path("/minhash/recalculatexors")
     @Produces(MediaType.MEDIA_TYPE_WILDCARD)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     void recalculateMinHashXORs(final String confirmation);
 
     @POST
     @Path("/minhash/recalculate")
     @Produces(MediaType.MEDIA_TYPE_WILDCARD)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     void recalculateMinHash();
 
     @POST
     @Path("/minhash/recalculatemissing")
     @Produces(MediaType.MEDIA_TYPE_WILDCARD)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     void recalculateMissingMinHash();
 
     @POST
     @Path("/contenthash/recalculatemissing")
     @Produces(MediaType.MEDIA_TYPE_WILDCARD)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     void recalculateMissingContentHash();
 
     /* SYSTEM FUNCTIONS */
 
     /**
      * Initiates a ReIndex of the lucene database.
      */
     @POST
     @Path("/database/reindex")
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     void reIndexLuceneDatabase();
 
     /**
      * @return An example of a ExpandDataTrunk object serialized to JSON. This is for convenience, and has no impact on the
      *         database or any other processing
      */
     @GET
     @Path("/expanddatatrunk/get/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     ExpandDataTrunk getJSONExpandTrunkExample();
 
     @POST
     @Path("/holdxml")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.APPLICATION_XML)
     IntegerWrapper holdXML(final String xml);
 
     @GET
     @Path("/echoxml")
     @Produces(MediaType.APPLICATION_XML)
     @Consumes(MediaType.WILDCARD)
     String echoXML(@QueryParam("id") final Integer id, @QueryParam("xml") final String xml);
 
     /* APPLICATION SETTING FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/settings/get/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPApplicationSettings(@QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/settings/get/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTServerSettingsV1 getJSONServerSettings();
 
     @POST
     @Path("/settings/update/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTServerSettingsV1 updateJSONServerSettings(final RESTServerSettingsV1 settings);
 
     /* USER FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/" + RESTv1Constants.USER_URL_NAME + "/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPUser(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/user/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPUserRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/users/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPUsers(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/users/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPUsersWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/user/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTUserV1 getJSONUser(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/user/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTUserV1 getJSONUserRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/users/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTUserCollectionV1 getJSONUsers(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/users/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTUserCollectionV1 getJSONUsersWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand);
 
     @POST
     @Path("/user/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTUserV1 updateJSONUser(@QueryParam("expand") final String expand, final RESTUserV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/users/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTUserCollectionV1 updateJSONUsers(@QueryParam("expand") final String expand, final RESTUserCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/user/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTUserV1 createJSONUser(@QueryParam("expand") final String expand, final RESTUserV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/users/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTUserCollectionV1 createJSONUsers(@QueryParam("expand") final String expand, final RESTUserCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/user/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTUserV1 deleteJSONUser(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/users/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTUserCollectionV1 deleteJSONUsers(@PathParam("ids") final PathSegment ids, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     /* STRINGCONSTANT FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/stringconstant/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPStringConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/stringconstant/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPStringConstantRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/stringconstants/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPStringConstants(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/stringconstants/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPStringConstantsWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/stringconstant/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTStringConstantV1 getJSONStringConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/stringconstant/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTStringConstantV1 getJSONStringConstantRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/stringconstants/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTStringConstantCollectionV1 getJSONStringConstants(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/stringconstants/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTStringConstantCollectionV1 getJSONStringConstantsWithQuery(@PathParam("query") final PathSegment query,
             @QueryParam("expand") final String expand);
 
     @POST
     @Path("/stringconstant/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTStringConstantV1 updateJSONStringConstant(@QueryParam("expand") final String expand, final RESTStringConstantV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/stringconstants/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTStringConstantCollectionV1 updateJSONStringConstants(@QueryParam("expand") final String expand,
             final RESTStringConstantCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/stringconstant/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTStringConstantV1 createJSONStringConstant(@QueryParam("expand") final String expand, final RESTStringConstantV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/stringconstants/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTStringConstantCollectionV1 createJSONStringConstants(@QueryParam("expand") final String expand,
             final RESTStringConstantCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/stringconstant/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTStringConstantV1 deleteJSONStringConstant(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/stringconstants/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTStringConstantCollectionV1 deleteJSONStringConstants(@PathParam("ids") final PathSegment ids,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId,
             @QueryParam("expand") final String expand);
 
     /* TRANSLATEDTOPIC FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/translatedtopic/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPTranslatedTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/translatedtopic/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPTranslatedTopicRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/translatedtopics/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPTranslatedTopics(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/translatedtopics/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPTranslatedTopicsWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/translatedtopic/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTranslatedTopicV1 getJSONTranslatedTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/translatedtopic/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTranslatedTopicV1 getJSONTranslatedTopicRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/translatedtopics/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTranslatedTopicCollectionV1 getJSONTranslatedTopicsWithQuery(@PathParam("query") PathSegment query,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/translatedtopics/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTranslatedTopicCollectionV1 getJSONTranslatedTopics(@QueryParam("expand") final String expand);
 
     @POST
     @Path("/translatedtopic/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTTranslatedTopicV1 updateJSONTranslatedTopic(@QueryParam("expand") final String expand, final RESTTranslatedTopicV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/translatedtopics/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTTranslatedTopicCollectionV1 updateJSONTranslatedTopics(@QueryParam("expand") final String expand,
             final RESTTranslatedTopicCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/translatedtopic/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTTranslatedTopicV1 createJSONTranslatedTopic(@QueryParam("expand") final String expand, final RESTTranslatedTopicV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/translatedtopics/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTTranslatedTopicCollectionV1 createJSONTranslatedTopics(@QueryParam("expand") final String expand,
             final RESTTranslatedTopicCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/translatedtopic/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTranslatedTopicV1 deleteJSONTranslatedTopic(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/translatedtopics/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTranslatedTopicCollectionV1 deleteJSONTranslatedTopics(@PathParam("ids") final PathSegment ids,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId,
             @QueryParam("expand") final String expand);
 
     /* ROLE FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/role/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPRole(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/role/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPRoleRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/roles/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPRoles(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/roles/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPRolesWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/role/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTRoleV1 getJSONRole(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/role/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTRoleV1 getJSONRoleRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/roles/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTRoleCollectionV1 getJSONRoles(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/roles/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTRoleCollectionV1 getJSONRolesWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand);
 
     @POST
     @Path("/role/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTRoleV1 updateJSONRole(@QueryParam("expand") final String expand, final RESTRoleV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/roles/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTRoleCollectionV1 updateJSONRoles(@QueryParam("expand") final String expand, final RESTRoleCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/role/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTRoleV1 createJSONRole(@QueryParam("expand") final String expand, final RESTRoleV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/roles/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTRoleCollectionV1 createJSONRoles(@QueryParam("expand") final String expand, final RESTRoleCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/role/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTRoleV1 deleteJSONRole(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/roles/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTRoleCollectionV1 deleteJSONRoles(@PathParam("ids") final PathSegment ids, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     /* PROPERYTAG FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/propertytag/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPPropertyTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/propertytag/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPPropertyTagRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/propertytags/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPPropertyTags(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/propertytags/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPPropertyTagsWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/propertytag/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTPropertyTagV1 getJSONPropertyTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/propertytag/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTPropertyTagV1 getJSONPropertyTagRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/propertytags/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTPropertyTagCollectionV1 getJSONPropertyTags(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/propertytags/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTPropertyTagCollectionV1 getJSONPropertyTagsWithQuery(@PathParam("query") final PathSegment query,
             @QueryParam("expand") final String expand);
 
     @POST
     @Path("/propertytag/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTPropertyTagV1 updateJSONPropertyTag(@QueryParam("expand") final String expand, final RESTPropertyTagV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/propertytags/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTPropertyTagCollectionV1 updateJSONPropertyTags(@QueryParam("expand") final String expand,
             final RESTPropertyTagCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/propertytag/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTPropertyTagV1 createJSONPropertyTag(@QueryParam("expand") final String expand, final RESTPropertyTagV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/propertytags/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTPropertyTagCollectionV1 createJSONPropertyTags(@QueryParam("expand") final String expand,
             final RESTPropertyTagCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/propertytag/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTPropertyTagV1 deleteJSONPropertyTag(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/propertytags/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTPropertyTagCollectionV1 deleteJSONPropertyTags(@PathParam("ids") final PathSegment ids, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     /* PROPERTYCATEGORY FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/propertycategory/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPPropertyCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/propertycategory/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPPropertyCategoryRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/propertycategories/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPPropertyCategories(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/propertycategories/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPPropertyCategoriesWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/propertycategory/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTPropertyCategoryV1 getJSONPropertyCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/propertycategory/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTPropertyCategoryV1 getJSONPropertyCategoryRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/propertycategories/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTPropertyCategoryCollectionV1 getJSONPropertyCategories(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/propertycategories/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTPropertyCategoryCollectionV1 getJSONPropertyCategoriesWithQuery(@PathParam("query") final PathSegment query,
             @QueryParam("expand") final String expand);
 
     @POST
     @Path("/propertycategory/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTPropertyCategoryV1 updateJSONPropertyCategory(@QueryParam("expand") final String expand, final RESTPropertyCategoryV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/propertycategories/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTPropertyCategoryCollectionV1 updateJSONPropertyCategories(@QueryParam("expand") final String expand,
             final RESTPropertyCategoryCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/propertycategory/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTPropertyCategoryV1 createJSONPropertyCategory(@QueryParam("expand") final String expand, final RESTPropertyCategoryV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/propertycategories/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTPropertyCategoryCollectionV1 createJSONPropertyCategories(@QueryParam("expand") final String expand,
             final RESTPropertyCategoryCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/propertycategory/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTPropertyCategoryV1 deleteJSONPropertyCategory(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/propertycategories/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTPropertyCategoryCollectionV1 deleteJSONPropertyCategories(@PathParam("ids") final PathSegment ids,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId,
             @QueryParam("expand") final String expand);
 
     /* BLOBCONSTANTS FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/blobconstant/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPBlobConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/blobconstant/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPBlobConstantRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/blobconstants/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPBlobConstantsWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/blobconstants/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPBlobConstants(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/blobconstant/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTBlobConstantV1 getJSONBlobConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/blobconstant/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTBlobConstantV1 getJSONBlobConstantRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/blobconstants/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTBlobConstantCollectionV1 getJSONBlobConstants(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/blobconstants/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTBlobConstantCollectionV1 getJSONBlobConstantsWithQuery(@PathParam("query") final PathSegment query,
             @QueryParam("expand") final String expand);
 
     @POST
     @Path("/blobconstant/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTBlobConstantV1 updateJSONBlobConstant(@QueryParam("expand") final String expand, final RESTBlobConstantV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/blobconstants/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTBlobConstantCollectionV1 updateJSONBlobConstants(@QueryParam("expand") final String expand,
             final RESTBlobConstantCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/blobconstant/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTBlobConstantV1 createJSONBlobConstant(@QueryParam("expand") final String expand, final RESTBlobConstantV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/blobconstants/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTBlobConstantCollectionV1 createJSONBlobConstants(@QueryParam("expand") final String expand,
             final RESTBlobConstantCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/blobconstant/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTBlobConstantV1 deleteJSONBlobConstant(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/blobconstants/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTBlobConstantCollectionV1 deleteJSONBlobConstants(@PathParam("ids") final PathSegment ids,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId,
             @QueryParam("expand") final String expand);
 
     /* RAW FUNCTIONS */
     @GET
     @Path("/blobconstant/get/raw/{id}")
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     @Produces(MediaType.APPLICATION_OCTET_STREAM)
     byte[] getRAWBlobConstant(@PathParam("id") final Integer id);
 
     @GET
     @Path("/blobconstant/get/raw/{id}/r/{rev}")
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     @Produces(MediaType.APPLICATION_OCTET_STREAM)
     byte[] getRAWBlobConstantRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision);
 
     /* PROJECT FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/project/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPProject(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/project/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPProjectRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/projects/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPProjects(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/projects/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPProjectsWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/project/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTProjectV1 getJSONProject(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/project/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTProjectV1 getJSONProjectRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/projects/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTProjectCollectionV1 getJSONProjects(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/projects/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTProjectCollectionV1 getJSONProjectsWithQuery(@PathParam("query") final PathSegment query,
             @QueryParam("expand") final String expand);
 
     @POST
     @Path("/project/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTProjectV1 updateJSONProject(@QueryParam("expand") final String expand, final RESTProjectV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/projects/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTProjectCollectionV1 updateJSONProjects(@QueryParam("expand") final String expand, final RESTProjectCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/project/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTProjectV1 createJSONProject(@QueryParam("expand") final String expand, final RESTProjectV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/projects/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTProjectCollectionV1 createJSONProjects(@QueryParam("expand") final String expand, final RESTProjectCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/project/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTProjectV1 deleteJSONProject(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/projects/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTProjectCollectionV1 deleteJSONProjects(@PathParam("ids") final PathSegment ids, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     /* TAG FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/tag/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/tag/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPTagRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/tags/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPTags(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/tags/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPTagsWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/tag/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTagV1 getJSONTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/tag/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTagV1 getJSONTagRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/tags/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTagCollectionV1 getJSONTags(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/tags/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTagCollectionV1 getJSONTagsWithQuery(@PathParam("query") PathSegment query, @QueryParam("expand") final String expand);
 
     @POST
     @Path("/tag/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTTagV1 updateJSONTag(@QueryParam("expand") final String expand, final RESTTagV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/tags/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTTagCollectionV1 updateJSONTags(@QueryParam("expand") final String expand, final RESTTagCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/tag/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTTagV1 createJSONTag(@QueryParam("expand") final String expand, final RESTTagV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/tags/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTTagCollectionV1 createJSONTags(@QueryParam("expand") final String expand, final RESTTagCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/tag/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTagV1 deleteJSONTag(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/tags/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTagCollectionV1 deleteJSONTags(@PathParam("ids") final PathSegment ids, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     /* CATEGORY FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/category/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/category/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPCategoryRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/categories/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPCategories(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/categories/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPCategoriesWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/category/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTCategoryV1 getJSONCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/category/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTCategoryV1 getJSONCategoryRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/categories/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTCategoryCollectionV1 getJSONCategories(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/categories/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTCategoryCollectionV1 getJSONCategoriesWithQuery(@PathParam("query") PathSegment query, @QueryParam("expand") final String expand);
 
     @POST
     @Path("/category/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTCategoryV1 updateJSONCategory(@QueryParam("expand") final String expand, final RESTCategoryV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/categories/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTCategoryCollectionV1 updateJSONCategories(@QueryParam("expand") final String expand, final RESTCategoryCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/category/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTCategoryV1 createJSONCategory(@QueryParam("expand") final String expand, final RESTCategoryV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/categories/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTCategoryCollectionV1 createJSONCategories(@QueryParam("expand") final String expand, final RESTCategoryCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/category/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTCategoryV1 deleteJSONCategory(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/categories/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTCategoryCollectionV1 deleteJSONCategories(@PathParam("ids") final PathSegment ids, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     /* IMAGE FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/image/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPImage(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/image/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPImageRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/images/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPImages(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/images/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPImagesWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/image/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTImageV1 getJSONImage(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/image/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTImageV1 getJSONImageRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/images/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTImageCollectionV1 getJSONImages(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/images/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTImageCollectionV1 getJSONImagesWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand);
 
     @POST
     @Path("/image/update/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTImageV1 updateJSONImage(@QueryParam("expand") final String expand, final RESTImageV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/images/update/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTImageCollectionV1 updateJSONImages(@QueryParam("expand") final String expand, final RESTImageCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/image/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTImageV1 createJSONImage(@QueryParam("expand") final String expand, final RESTImageV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/images/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTImageCollectionV1 createJSONImages(@QueryParam("expand") final String expand, final RESTImageCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/image/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTImageV1 deleteJSONImage(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/images/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTImageCollectionV1 deleteJSONImages(@PathParam("ids") final PathSegment ids, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     /* IMAGE */
     @GET
     @Path("/image/get/raw/{id}")
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     @Produces({"image/gif", "image/png", "image/jpeg", "image/svg+xml"})
     Response getRAWImage(@PathParam("id") final Integer id, @QueryParam("lang") final String locale);
 
     @GET
     @Path("/image/get/raw/{id}/r/{rev}")
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     @Produces({"image/gif", "image/png", "image/jpeg", "image/svg+xml"})
     Response getRAWImageRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("lang") final String locale);
 
     @GET
     @Path("/image/get/raw/{id}/thumbnail")
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     @Produces({"image/gif", "image/png", "image/jpeg", "image/svg+xml"})
     Response getRAWImageThumbnail(@PathParam("id") final Integer id, @QueryParam("lang") final String locale);
 
     /* TOPIC FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/topics/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPTopicsWithQuery(@PathParam("query") PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/topics/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPTopics(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/topic/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/topic/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPTopicRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/topics/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTopicCollectionV1 getJSONTopics(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/topics/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTopicCollectionV1 getJSONTopicsWithQuery(@PathParam("query") PathSegment query, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/topics/get/svg/{query}/{chart}")
     @Produces("image/svg+xml")
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getSVGTopicsWithQuery(final @PathParam("query") PathSegment query, final @PathParam("chart") PathSegment chart);
 
     @GET
     @Path("/topic/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTopicV1 getJSONTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/topic/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTopicV1 getJSONTopicRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @POST
     @Path("/topic/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTTopicV1 updateJSONTopic(@QueryParam("expand") final String expand, final RESTTopicV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/topics/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTTopicCollectionV1 updateJSONTopics(@QueryParam("expand") final String expand, final RESTTopicCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/topic/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTTopicV1 createJSONTopic(@QueryParam("expand") final String expand, final RESTTopicV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/topics/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTTopicCollectionV1 createJSONTopics(@QueryParam("expand") final String expand, final RESTTopicCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/topic/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTopicV1 deleteJSONTopic(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/topics/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTopicCollectionV1 deleteJSONTopics(@PathParam("ids") final PathSegment ids, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @POST
     @Path("/topic/createormatch/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTMatchedTopicV1 createOrMatchJSONTopic(@QueryParam("expand") final String expand, final RESTTopicV1 dataObject,
                                 @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     /* XML FUNCTIONS */
     @GET
     @Path("/topics/get/xml/all")
     @Produces(MediaType.APPLICATION_XML)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTopicCollectionV1 getXMLTopics(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/topic/get/xml/{id}")
     @Produces(MediaType.APPLICATION_XML)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTopicV1 getXMLTopic(@PathParam("id") final Integer id);
 
     @GET
     @Path("/topic/get/xml/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_XML)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTopicV1 getXMLTopicRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision);
 
     @GET
     @Path("/topic/get/xml/{id}/xml")
     @Produces(MediaType.APPLICATION_XML)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getXMLTopicXML(@PathParam("id") final Integer id);
 
     @POST
     @Path("/topic/update/xml/{id}/xml")
     @Produces(MediaType.APPLICATION_XML)
     @Consumes({MediaType.APPLICATION_XML})
     String updateXMLTopicXML(@PathParam("id") final Integer id, final String xml, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @GET
     @Path("/topic/get/xml/{id}/r/{rev}/xml")
     @Produces(MediaType.APPLICATION_XML)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getXMLTopicRevisionXML(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision);
 
     @GET
     @Path("/topic/get/xml/{id}/xmlContainedIn")
     @Produces(MediaType.APPLICATION_XML)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getXMLTopicXMLContained(@PathParam("id") final Integer id, @QueryParam("container") final String containerName);
 
     @GET
     @Path("/topic/get/xml/{id}/xmlNoContainer")
     @Produces(MediaType.TEXT_PLAIN)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getXMLTopicXMLNoContainer(@PathParam("id") final Integer id, @QueryParam("includeTitle") final Boolean includeTitle);
 
     /* CSV FUNCTIONS */
     @GET
     @Path("/topics/get/csv/all")
     @Produces(MediaType.TEXT_PLAIN)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getCSVTopics();
 
     @GET
     @Path("/topics/get/csv/{query}")
     @Produces(MediaType.TEXT_PLAIN)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getCSVTopicsWithQuery(@PathParam("query") PathSegment query);
 
     /* ZIP FUNCTIONS */
     @GET
     @Path("/topics/get/zip/all")
     @Produces(MediaType.APPLICATION_OCTET_STREAM)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     byte[] getZIPTopics();
 
     @GET
     @Path("/topics/get/zip/{query}")
     @Produces(MediaType.APPLICATION_OCTET_STREAM)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     byte[] getZIPTopicsWithQuery(@PathParam("query") PathSegment query);
 
     /* FILTERS FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/filter/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPFilter(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/filter/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPFilterRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/filters/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPFiltersWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/filters/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPFilters(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/filter/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTFilterV1 getJSONFilter(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/filter/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTFilterV1 getJSONFilterRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/filters/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTFilterCollectionV1 getJSONFilters(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/filters/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTFilterCollectionV1 getJSONFiltersWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand);
 
     @POST
     @Path("/filter/update/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTFilterV1 updateJSONFilter(@QueryParam("expand") final String expand, final RESTFilterV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/filters/update/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTFilterCollectionV1 updateJSONFilters(@QueryParam("expand") final String expand, final RESTFilterCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/filter/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTFilterV1 createJSONFilter(@QueryParam("expand") final String expand, final RESTFilterV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/filters/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTFilterCollectionV1 createJSONFilters(@QueryParam("expand") final String expand, final RESTFilterCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/filter/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTFilterV1 deleteJSONFilter(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/filters/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTFilterCollectionV1 deleteJSONFilters(@PathParam("ids") final PathSegment ids, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     /* INTEGERCONSTANT FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/integerconstant/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPIntegerConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/integerconstant/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPIntegerConstantRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/integerconstants/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPIntegerConstants(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/integerconstants/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPIntegerConstantsWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/integerconstant/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTIntegerConstantV1 getJSONIntegerConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/integerconstant/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTIntegerConstantV1 getJSONIntegerConstantRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/integerconstants/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTIntegerConstantCollectionV1 getJSONIntegerConstants(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/integerconstants/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTIntegerConstantCollectionV1 getJSONIntegerConstantsWithQuery(@PathParam("query") final PathSegment query,
             @QueryParam("expand") final String expand);
 
     @POST
     @Path("/integerconstant/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTIntegerConstantV1 updateJSONIntegerConstant(@QueryParam("expand") final String expand, final RESTIntegerConstantV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/integerconstants/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTIntegerConstantCollectionV1 updateJSONIntegerConstants(@QueryParam("expand") final String expand,
             final RESTIntegerConstantCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/integerconstant/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTIntegerConstantV1 createJSONIntegerConstant(@QueryParam("expand") final String expand, final RESTIntegerConstantV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/integerconstants/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTIntegerConstantCollectionV1 createJSONIntegerConstants(@QueryParam("expand") final String expand,
             final RESTIntegerConstantCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/integerconstant/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTIntegerConstantV1 deleteJSONIntegerConstant(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/integerconstants/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTIntegerConstantCollectionV1 deleteJSONIntegerConstants(@PathParam("ids") final PathSegment ids,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId,
             @QueryParam("expand") final String expand);
 
     /* CONTENT SPEC FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/contentspec/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPContentSpec(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/contentspec/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPContentSpecRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/contentspecs/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPContentSpecs(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/contentspecs/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPContentSpecsWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/contentspec/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTContentSpecV1 getJSONContentSpec(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/contentspec/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTContentSpecV1 getJSONContentSpecRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/contentspecs/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTContentSpecCollectionV1 getJSONContentSpecs(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/contentspecs/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTContentSpecCollectionV1 getJSONContentSpecsWithQuery(@PathParam("query") final PathSegment query,
             @QueryParam("expand") final String expand);
 
     @POST
     @Path("/contentspec/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTContentSpecV1 updateJSONContentSpec(@QueryParam("expand") final String expand, final RESTContentSpecV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/contentspecs/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTContentSpecCollectionV1 updateJSONContentSpecs(@QueryParam("expand") final String expand,
             final RESTContentSpecCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/contentspec/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTContentSpecV1 createJSONContentSpec(@QueryParam("expand") final String expand, final RESTContentSpecV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/contentspecs/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTContentSpecCollectionV1 createJSONContentSpecs(@QueryParam("expand") final String expand,
             final RESTContentSpecCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/contentspec/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTContentSpecV1 deleteJSONContentSpec(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/contentspecs/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTContentSpecCollectionV1 deleteJSONContentSpecs(@PathParam("ids") final PathSegment ids, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     /* EXTRA CONTENT SPEC FUNCTIONS */
     @GET
     @Path("/contentspec/get/text/{id}")
     @Produces(MediaType.TEXT_PLAIN)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getTEXTContentSpec(@PathParam("id") final Integer id);
 
     @GET
     @Path("/contentspec/get/text/{id}/r/{rev}")
     @Produces(MediaType.TEXT_PLAIN)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getTEXTContentSpecRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision);
 
     @POST
     @Path("/contentspec/update/text/{id}")
     @Consumes({MediaType.TEXT_PLAIN})
     @Produces(MediaType.TEXT_PLAIN)
     String updateTEXTContentSpec(@PathParam("id") final Integer id, final String contentSpec,
             @QueryParam("strictTitles") final Boolean strictTitles, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/contentspec/create/text")
     @Consumes({MediaType.TEXT_PLAIN})
     @Produces(MediaType.TEXT_PLAIN)
     String createTEXTContentSpec(final String contentSpec, @QueryParam("strictTitles") final Boolean strictTitles,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     /* CONTENT SPEC JSON TEXT FUNCTIONS */
     @GET
     @Path("/contentspec/get/json+text/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTextContentSpecV1 getJSONTextContentSpec(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/contentspec/get/json+text/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTextContentSpecV1 getJSONTextContentSpecRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/contentspecs/get/json+text/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTextContentSpecCollectionV1 getJSONTextContentSpecs(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/contentspecs/get/json+text/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTextContentSpecCollectionV1 getJSONTextContentSpecsWithQuery(@PathParam("query") final PathSegment query,
             @QueryParam("expand") final String expand);
 
     @POST
     @Path("/contentspec/update/json+text")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTTextContentSpecV1 updateJSONTextContentSpec(@QueryParam("expand") final String expand, final RESTTextContentSpecV1 contentSpec,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/contentspec/create/json+text")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTTextContentSpecV1 createJSONTextContentSpec(@QueryParam("expand") final String expand, final RESTTextContentSpecV1 contentSpec,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     /* ZIP FUNCTIONS */
     @GET
     @Path("/contentspecs/get/zip/all")
     @Produces(MediaType.APPLICATION_OCTET_STREAM)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     byte[] getZIPContentSpecs();
 
     @GET
     @Path("/contentspecs/get/zip/{query}")
     @Produces(MediaType.APPLICATION_OCTET_STREAM)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     byte[] getZIPContentSpecsWithQuery(@PathParam("query") PathSegment query);
 
     /* CONTENT SPEC NODE FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/contentspecnode/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPContentSpecNode(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/contentspecnode/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPContentSpecNodeRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/contentspecnodes/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPContentSpecNodes(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/contentspecnodes/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPContentSpecNodesWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/contentspecnode/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTCSNodeV1 getJSONContentSpecNode(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/contentspecnode/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTCSNodeV1 getJSONContentSpecNodeRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/contentspecnodes/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTCSNodeCollectionV1 getJSONContentSpecNodes(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/contentspecnodes/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTCSNodeCollectionV1 getJSONContentSpecNodesWithQuery(@PathParam("query") final PathSegment query,
             @QueryParam("expand") final String expand);
 
 //    @POST
 //    @Path("/contentspecnode/update/json")
 //    @Consumes({MediaType.APPLICATION_JSON})
 //    @Produces(MediaType.APPLICATION_JSON)
 //    public RESTCSNodeV1 updateJSONContentSpecNode(@QueryParam("expand") final String expand, final RESTCSNodeV1 dataObject,
 //            @QueryParam("message") final String message, @QueryParam("flag") final Integer flag,
 // @QueryParam("userId") final String userId);
 //
 //    @POST
 //    @Path("/contentspecnodes/update/json")
 //    @Consumes({MediaType.APPLICATION_JSON})
 //    @Produces(MediaType.APPLICATION_JSON)
 //    public RESTCSNodeCollectionV1 updateJSONContentSpecNodes(@QueryParam("expand") final String expand,
 //            final RESTCSNodeCollectionV1 dataObjects, @QueryParam("message") final String message, @QueryParam("flag") final Integer flag,
 //            @QueryParam("userId") final String userId);
 //
 //    @POST
 //    @Path("/contentspecnode/create/json")
 //    @Produces(MediaType.APPLICATION_JSON)
 //    @Consumes({MediaType.APPLICATION_JSON})
 //    public RESTCSNodeV1 createJSONContentSpecNode(@QueryParam("expand") final String expand, final RESTCSNodeV1 dataObject,
 //            @QueryParam("message") final String message, @QueryParam("flag") final Integer flag,
 // @QueryParam("userId") final String userId);
 //
 //    @POST
 //    @Path("/contentspecnodes/create/json")
 //    @Produces(MediaType.APPLICATION_JSON)
 //    @Consumes({MediaType.APPLICATION_JSON})
 //    public RESTCSNodeCollectionV1 createJSONContentSpecNodes(@QueryParam("expand") final String expand,
 //            final RESTCSNodeCollectionV1 dataObjects, @QueryParam("message") final String message, @QueryParam("flag") final Integer flag,
 //            @QueryParam("userId") final String userId);
 //
 //    @DELETE
 //    @Path("/contentspecnode/delete/json/{id}")
 //    @Produces(MediaType.APPLICATION_JSON)
 //    @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
 //    public RESTCSNodeV1 deleteJSONContentSpecNode(@PathParam("id") final Integer id, @QueryParam("message") final String message,
 //            @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 //
 //    @DELETE
 //    @Path("/contentspecnodes/delete/json/{ids}")
 //    @Produces(MediaType.APPLICATION_JSON)
 //    @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
 //    public RESTCSNodeCollectionV1 deleteJSONContentSpecNodes(@PathParam("ids") final PathSegment ids,
 //            @QueryParam("message") final String message, @QueryParam("flag") final Integer flag,
 // @QueryParam("userId") final String userId,
 //            @QueryParam("expand") final String expand);
 
     /* TRANSLATED CONTENT SPEC FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/translatedcontentspec/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPTranslatedContentSpec(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/translatedcontentspec/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPTranslatedContentSpecRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/translatedcontentspecs/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPTranslatedContentSpecs(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/translatedcontentspecs/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPTranslatedContentSpecsWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/translatedcontentspec/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTranslatedContentSpecV1 getJSONTranslatedContentSpec(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/translatedcontentspec/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTranslatedContentSpecV1 getJSONTranslatedContentSpecRevision(@PathParam("id") final Integer id,
             @PathParam("rev") final Integer revision, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/translatedcontentspecs/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTranslatedContentSpecCollectionV1 getJSONTranslatedContentSpecs(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/translatedcontentspecs/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTranslatedContentSpecCollectionV1 getJSONTranslatedContentSpecsWithQuery(@PathParam("query") final PathSegment query,
             @QueryParam("expand") final String expand);
 
     @POST
     @Path("/translatedcontentspec/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTTranslatedContentSpecV1 updateJSONTranslatedContentSpec(@QueryParam("expand") final String expand,
             final RESTTranslatedContentSpecV1 dataObject, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/translatedcontentspecs/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTTranslatedContentSpecCollectionV1 updateJSONTranslatedContentSpecs(@QueryParam("expand") final String expand,
             final RESTTranslatedContentSpecCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/translatedcontentspec/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTTranslatedContentSpecV1 createJSONTranslatedContentSpec(@QueryParam("expand") final String expand,
             final RESTTranslatedContentSpecV1 dataObject, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/translatedcontentspecs/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTTranslatedContentSpecCollectionV1 createJSONTranslatedContentSpecs(@QueryParam("expand") final String expand,
             final RESTTranslatedContentSpecCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/translatedcontentspec/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTranslatedContentSpecV1 deleteJSONTranslatedContentSpec(@PathParam("id") final Integer id,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId,
             @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/translatedcontentspecs/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTranslatedContentSpecCollectionV1 deleteJSONTranslatedContentSpecs(@PathParam("ids") final PathSegment ids,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId,
             @QueryParam("expand") final String expand);
 
     /* TRANSLATED CONTENT SPEC NODE FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/translatedcontentspecnode/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPTranslatedContentSpecNode(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/translatedcontentspecnode/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPTranslatedContentSpecNodeRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/translatedcontentspecnodes/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPTranslatedContentSpecNodes(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/translatedcontentspecnodes/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPTranslatedContentSpecNodesWithQuery(@PathParam("query") final PathSegment query,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/translatedcontentspecnode/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTranslatedCSNodeV1 getJSONTranslatedContentSpecNode(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/translatedcontentspecnode/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTranslatedCSNodeV1 getJSONTranslatedContentSpecNodeRevision(@PathParam("id") final Integer id,
             @PathParam("rev") final Integer revision, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/translatedcontentspecnodes/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTranslatedCSNodeCollectionV1 getJSONTranslatedContentSpecNodes(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/translatedcontentspecnodes/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTranslatedCSNodeCollectionV1 getJSONTranslatedContentSpecNodesWithQuery(@PathParam("query") final PathSegment query,
             @QueryParam("expand") final String expand);
 
     @POST
     @Path("/translatedcontentspecnode/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTTranslatedCSNodeV1 updateJSONTranslatedContentSpecNode(@QueryParam("expand") final String expand,
             final RESTTranslatedCSNodeV1 dataObject, @QueryParam("message") final String message, @QueryParam("flag") final Integer flag,
             @QueryParam("userId") final String userId);
 
     @POST
     @Path("/translatedcontentspecnodes/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     RESTTranslatedCSNodeCollectionV1 updateJSONTranslatedContentSpecNodes(@QueryParam("expand") final String expand,
             final RESTTranslatedCSNodeCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/translatedcontentspecnode/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTTranslatedCSNodeV1 createJSONTranslatedContentSpecNode(@QueryParam("expand") final String expand,
             final RESTTranslatedCSNodeV1 dataObject, @QueryParam("message") final String message, @QueryParam("flag") final Integer flag,
             @QueryParam("userId") final String userId);
 
     @POST
     @Path("/translatedcontentspecnodes/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTTranslatedCSNodeCollectionV1 createJSONTranslatedContentSpecNodes(@QueryParam("expand") final String expand,
             final RESTTranslatedCSNodeCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/translatedcontentspecnode/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTranslatedCSNodeV1 deleteJSONTranslatedContentSpecNode(@PathParam("id") final Integer id,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId,
             @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/translatedcontentspecnodes/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTTranslatedCSNodeCollectionV1 deleteJSONTranslatedContentSpecNodes(@PathParam("ids") final PathSegment ids,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId,
             @QueryParam("expand") final String expand);
 
     /* FILE FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/file/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPFile(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/file/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPFileRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/files/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPFiles(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/files/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     String getJSONPFilesWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/file/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTFileV1 getJSONFile(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/file/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTFileV1 getJSONFileRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/files/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTFileCollectionV1 getJSONFiles(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/files/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTFileCollectionV1 getJSONFilesWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand);
 
     @POST
     @Path("/file/update/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTFileV1 updateJSONFile(@QueryParam("expand") final String expand, final RESTFileV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/files/update/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTFileCollectionV1 updateJSONFiles(@QueryParam("expand") final String expand, final RESTFileCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/file/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTFileV1 createJSONFile(@QueryParam("expand") final String expand, final RESTFileV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/files/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     RESTFileCollectionV1 createJSONFiles(@QueryParam("expand") final String expand, final RESTFileCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/file/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTFileV1 deleteJSONFile(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/files/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     RESTFileCollectionV1 deleteJSONFiles(@PathParam("ids") final PathSegment ids, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     /* RAW FUNCTIONS */
     @GET
     @Path("/file/get/raw/{id}")
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     @Produces(MediaType.APPLICATION_OCTET_STREAM)
     byte[] getRAWFile(@PathParam("id") final Integer id, @QueryParam("lang") final String locale);
 
     @GET
     @Path("/file/get/raw/{id}/r/{rev}")
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     @Produces(MediaType.APPLICATION_OCTET_STREAM)
     byte[] getRAWFileRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("lang") final String locale);
 }
