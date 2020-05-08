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
 
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTBlobConstantCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTCategoryCollectionV1;
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
 import org.jboss.pressgang.ccms.rest.v1.constants.RESTv1Constants;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTBlobConstantV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTCategoryV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTFilterV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTImageV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTIntegerConstantV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTProjectV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTPropertyCategoryV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTPropertyTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTRoleV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTStringConstantV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTranslatedTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTUserV1;
 import org.jboss.pressgang.ccms.rest.v1.expansion.ExpandDataTrunk;
 
 @Path("/1")
 public interface RESTBaseInterfaceV1 {
     /* SYSTEM FUNCTIONS */
 
     /**
      * Initiates a ReIndex of the lucene database.
      */
     @POST
     @Path("/database/reindex")
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public void reIndexLuceneDatabase();
 
     /**
      * @return An example of a ExpandDataTrunk object serialized to JSON. This is for convenience, and has no impact on the
      *         database or any other processing
      */
     @GET
     @Path("/expanddatatrunk/get/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public ExpandDataTrunk getJSONExpandTrunkExample();
 
     @POST
     @Path("/holdxml")
     @Produces(MediaType.WILDCARD)
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
     public Integer holdXML(final String xml);
 
     @GET
     @Path("/echoxml")
     @Produces(MediaType.APPLICATION_XML)
     @Consumes(MediaType.WILDCARD)
     public String echoXML(@QueryParam("id") final Integer id, @QueryParam("xml") final String xml);
 
     /* USER FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/" + RESTv1Constants.USER_URL_NAME + "/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPUser(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/user/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPUserRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/users/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPUsers(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/users/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPUsersWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/user/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTUserV1 getJSONUser(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/user/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTUserV1 getJSONUserRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/users/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTUserCollectionV1 getJSONUsers(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/users/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTUserCollectionV1 getJSONUsersWithQuery(@PathParam("query") final PathSegment query,
             @QueryParam("expand") final String expand);
 
     @POST
     @Path("/user/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     public RESTUserV1 updateJSONUser(@QueryParam("expand") final String expand, final RESTUserV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/users/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     public RESTUserCollectionV1 updateJSONUsers(@QueryParam("expand") final String expand, final RESTUserCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/user/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTUserV1 createJSONUser(@QueryParam("expand") final String expand, final RESTUserV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/users/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTUserCollectionV1 createJSONUsers(@QueryParam("expand") final String expand, final RESTUserCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/user/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTUserV1 deleteJSONUser(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/users/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTUserCollectionV1 deleteJSONUsers(@PathParam("ids") final PathSegment ids, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     /* STRINGCONSTANT FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/stringconstant/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPStringConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/stringconstant/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPStringConstantRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/stringconstants/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPStringConstants(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/stringconstants/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPStringConstantsWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/stringconstant/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTStringConstantV1 getJSONStringConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/stringconstant/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTStringConstantV1 getJSONStringConstantRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/stringconstants/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTStringConstantCollectionV1 getJSONStringConstants(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/stringconstants/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTStringConstantCollectionV1 getJSONStringConstantsWithQuery(@PathParam("query") final PathSegment query,
             @QueryParam("expand") final String expand);
 
     @POST
     @Path("/stringconstant/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     public RESTStringConstantV1 updateJSONStringConstant(@QueryParam("expand") final String expand, final RESTStringConstantV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/stringconstants/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     public RESTStringConstantCollectionV1 updateJSONStringConstants(@QueryParam("expand") final String expand,
             final RESTStringConstantCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/stringconstant/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTStringConstantV1 createJSONStringConstant(@QueryParam("expand") final String expand, final RESTStringConstantV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/stringconstants/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTStringConstantCollectionV1 createJSONStringConstants(@QueryParam("expand") final String expand,
             final RESTStringConstantCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/stringconstant/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTStringConstantV1 deleteJSONStringConstant(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/stringconstants/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTStringConstantCollectionV1 deleteJSONStringConstants(@PathParam("ids") final PathSegment ids,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId,
             @QueryParam("expand") final String expand);
 
     /* TRANSLATEDTOPIC FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/translatedtopic/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPTranslatedTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/translatedtopic/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPTranslatedTopicRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/translatedtopics/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPTranslatedTopics(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/translatedtopics/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPTranslatedTopicsWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/translatedtopic/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTTranslatedTopicV1 getJSONTranslatedTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/translatedtopic/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTTranslatedTopicV1 getJSONTranslatedTopicRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/translatedtopics/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTTranslatedTopicCollectionV1 getJSONTranslatedTopicsWithQuery(@PathParam("query") PathSegment query,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/translatedtopics/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTTranslatedTopicCollectionV1 getJSONTranslatedTopics(@QueryParam("expand") final String expand);
 
     @POST
     @Path("/translatedtopic/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     public RESTTranslatedTopicV1 updateJSONTranslatedTopic(@QueryParam("expand") final String expand,
             final RESTTranslatedTopicV1 dataObject, @QueryParam("message") final String message, @QueryParam("flag") final Integer flag,
             @QueryParam("userId") final String userId);
 
     @POST
     @Path("/translatedtopics/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     public RESTTranslatedTopicCollectionV1 updateJSONTranslatedTopics(@QueryParam("expand") final String expand,
             final RESTTranslatedTopicCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/translatedtopic/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTTranslatedTopicV1 createJSONTranslatedTopic(@QueryParam("expand") final String expand,
             final RESTTranslatedTopicV1 dataObject, @QueryParam("message") final String message, @QueryParam("flag") final Integer flag,
             @QueryParam("userId") final String userId);
 
     @POST
     @Path("/translatedtopics/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTTranslatedTopicCollectionV1 createJSONTranslatedTopics(@QueryParam("expand") final String expand,
             final RESTTranslatedTopicCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/translatedtopic/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTTranslatedTopicV1 deleteJSONTranslatedTopic(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/translatedtopics/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTTranslatedTopicCollectionV1 deleteJSONTranslatedTopics(@PathParam("ids") final PathSegment ids,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId,
             @QueryParam("expand") final String expand);
 
     /* ROLE FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/role/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPRole(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/role/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPRoleRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/roles/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPRoles(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/roles/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPRolesWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/role/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTRoleV1 getJSONRole(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/role/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTRoleV1 getJSONRoleRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/roles/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTRoleCollectionV1 getJSONRoles(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/roles/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTRoleCollectionV1 getJSONRolesWithQuery(@PathParam("query") final PathSegment query,
             @QueryParam("expand") final String expand);
 
     @POST
     @Path("/role/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     public RESTRoleV1 updateJSONRole(@QueryParam("expand") final String expand, final RESTRoleV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/roles/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     public RESTRoleCollectionV1 updateJSONRoles(@QueryParam("expand") final String expand, final RESTRoleCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/role/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTRoleV1 createJSONRole(@QueryParam("expand") final String expand, final RESTRoleV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/roles/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTRoleCollectionV1 createJSONRoles(@QueryParam("expand") final String expand, final RESTRoleCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/role/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTRoleV1 deleteJSONRole(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/roles/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTRoleCollectionV1 deleteJSONRoles(@PathParam("ids") final PathSegment ids, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     /* PROPERYTAG FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/propertytag/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPPropertyTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/propertytag/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPPropertyTagRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/propertytags/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPPropertyTags(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/propertytags/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPPropertyTagsWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/propertytag/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTPropertyTagV1 getJSONPropertyTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/propertytag/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTPropertyTagV1 getJSONPropertyTagRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/propertytags/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTPropertyTagCollectionV1 getJSONPropertyTags(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/propertytags/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTPropertyTagCollectionV1 getJSONPropertyTagsWithQuery(@PathParam("query") final PathSegment query,
             @QueryParam("expand") final String expand);
 
     @POST
     @Path("/propertytag/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     public RESTPropertyTagV1 updateJSONPropertyTag(@QueryParam("expand") final String expand, final RESTPropertyTagV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/propertytags/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     public RESTPropertyTagCollectionV1 updateJSONPropertyTags(@QueryParam("expand") final String expand,
             final RESTPropertyTagCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/propertytag/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTPropertyTagV1 createJSONPropertyTag(@QueryParam("expand") final String expand, final RESTPropertyTagV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/propertytags/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTPropertyTagCollectionV1 createJSONPropertyTags(@QueryParam("expand") final String expand,
             final RESTPropertyTagCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/propertytag/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTPropertyTagV1 deleteJSONPropertyTag(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/propertytags/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTPropertyTagCollectionV1 deleteJSONPropertyTags(@PathParam("ids") final PathSegment ids,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId,
             @QueryParam("expand") final String expand);
 
     /* PROPERTYCATEGORY FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/propertycategory/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPPropertyCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/propertycategory/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPPropertyCategoryRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/propertycategories/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPPropertyCategories(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/propertycategories/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPPropertyCategoriesWithQuery(@PathParam("query") final PathSegment query,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/propertycategory/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTPropertyCategoryV1 getJSONPropertyCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/propertycategory/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTPropertyCategoryV1 getJSONPropertyCategoryRevision(@PathParam("id") final Integer id,
             @PathParam("rev") final Integer revision, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/propertycategories/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTPropertyCategoryCollectionV1 getJSONPropertyCategories(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/propertycategories/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTPropertyCategoryCollectionV1 getJSONPropertyCategoriesWithQuery(@PathParam("query") final PathSegment query,
             @QueryParam("expand") final String expand);
 
     @POST
     @Path("/propertycategory/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     public RESTPropertyCategoryV1 updateJSONPropertyCategory(@QueryParam("expand") final String expand,
             final RESTPropertyCategoryV1 dataObject, @QueryParam("message") final String message, @QueryParam("flag") final Integer flag,
             @QueryParam("userId") final String userId);
 
     @POST
     @Path("/propertycategories/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     public RESTPropertyCategoryCollectionV1 updateJSONPropertyCategories(@QueryParam("expand") final String expand,
             final RESTPropertyCategoryCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/propertycategory/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTPropertyCategoryV1 createJSONPropertyCategory(@QueryParam("expand") final String expand,
             final RESTPropertyCategoryV1 dataObject, @QueryParam("message") final String message, @QueryParam("flag") final Integer flag,
             @QueryParam("userId") final String userId);
 
     @POST
     @Path("/propertycategories/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTPropertyCategoryCollectionV1 createJSONPropertyCategories(@QueryParam("expand") final String expand,
             final RESTPropertyCategoryCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/propertycategory/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTPropertyCategoryV1 deleteJSONPropertyCategory(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/propertycategories/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTPropertyCategoryCollectionV1 deleteJSONPropertyCategories(@PathParam("ids") final PathSegment ids,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId,
             @QueryParam("expand") final String expand);
 
     /* BLOBCONSTANTS FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/blobconstant/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPBlobConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/blobconstant/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPBlobConstantRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/blobconstants/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPBlobConstantsWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/blobconstants/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPBlobConstants(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/blobconstant/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTBlobConstantV1 getJSONBlobConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/blobconstant/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTBlobConstantV1 getJSONBlobConstantRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/blobconstants/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTBlobConstantCollectionV1 getJSONBlobConstants(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/blobconstants/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTBlobConstantCollectionV1 getJSONBlobConstantsWithQuery(@PathParam("query") final PathSegment query,
             @QueryParam("expand") final String expand);
 
     @POST
     @Path("/blobconstant/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     public RESTBlobConstantV1 updateJSONBlobConstant(@QueryParam("expand") final String expand, final RESTBlobConstantV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/blobconstants/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     public RESTBlobConstantCollectionV1 updateJSONBlobConstants(@QueryParam("expand") final String expand,
             final RESTBlobConstantCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/blobconstant/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTBlobConstantV1 createJSONBlobConstant(@QueryParam("expand") final String expand, final RESTBlobConstantV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/blobconstants/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTBlobConstantCollectionV1 createJSONBlobConstants(@QueryParam("expand") final String expand,
             final RESTBlobConstantCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/blobconstant/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTBlobConstantV1 deleteJSONBlobConstant(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/blobconstants/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTBlobConstantCollectionV1 deleteJSONBlobConstants(@PathParam("ids") final PathSegment ids,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId,
             @QueryParam("expand") final String expand);
 
     /* RAW FUNCTIONS */
     @GET
     @Path("/blobconstant/get/raw/{id}")
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     @Produces(MediaType.APPLICATION_OCTET_STREAM)
     public byte[] getRAWBlobConstant(@PathParam("id") final Integer id);
 
     @GET
     @Path("/blobconstant/get/raw/{id}/r/{rev}")
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     @Produces(MediaType.APPLICATION_OCTET_STREAM)
     public byte[] getRAWBlobConstantRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision);
 
     /* PROJECT FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/project/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPProject(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/project/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPProjectRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/projects/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPProjects(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/projects/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPProjectsWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/project/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTProjectV1 getJSONProject(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/project/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTProjectV1 getJSONProjectRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/projects/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTProjectCollectionV1 getJSONProjects(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/projects/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTProjectCollectionV1 getJSONProjectsWithQuery(@PathParam("query") final PathSegment query,
             @QueryParam("expand") final String expand);
 
     @POST
     @Path("/project/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     public RESTProjectV1 updateJSONProject(@QueryParam("expand") final String expand, final RESTProjectV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/projects/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     public RESTProjectCollectionV1 updateJSONProjects(@QueryParam("expand") final String expand, final RESTProjectCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/project/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTProjectV1 createJSONProject(@QueryParam("expand") final String expand, final RESTProjectV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/projects/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTProjectCollectionV1 createJSONProjects(@QueryParam("expand") final String expand, final RESTProjectCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/project/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTProjectV1 deleteJSONProject(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/projects/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTProjectCollectionV1 deleteJSONProjects(@PathParam("ids") final PathSegment ids, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     /* TAG FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/tag/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/tag/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPTagRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/tags/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPTags(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/tags/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPTagsWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/tag/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTTagV1 getJSONTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/tag/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTTagV1 getJSONTagRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/tags/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTTagCollectionV1 getJSONTags(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/tags/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTTagCollectionV1 getJSONTagsWithQuery(@PathParam("query") PathSegment query, @QueryParam("expand") final String expand);
 
     @POST
     @Path("/tag/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     public RESTTagV1 updateJSONTag(@QueryParam("expand") final String expand, final RESTTagV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/tags/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     public RESTTagCollectionV1 updateJSONTags(@QueryParam("expand") final String expand, final RESTTagCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/tag/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTTagV1 createJSONTag(@QueryParam("expand") final String expand, final RESTTagV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/tags/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTTagCollectionV1 createJSONTags(@QueryParam("expand") final String expand, final RESTTagCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/tag/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTTagV1 deleteJSONTag(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/tags/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTTagCollectionV1 deleteJSONTags(@PathParam("ids") final PathSegment ids, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     /* CATEGORY FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/category/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/category/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPCategoryRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/categories/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPCategories(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/categories/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPCategoriesWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/category/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTCategoryV1 getJSONCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/category/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTCategoryV1 getJSONCategoryRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/categories/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTCategoryCollectionV1 getJSONCategories(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/categories/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTCategoryCollectionV1 getJSONCategoriesWithQuery(@PathParam("query") PathSegment query,
             @QueryParam("expand") final String expand);
 
     @POST
     @Path("/category/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     public RESTCategoryV1 updateJSONCategory(@QueryParam("expand") final String expand, final RESTCategoryV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/categories/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     public RESTCategoryCollectionV1 updateJSONCategories(@QueryParam("expand") final String expand,
             final RESTCategoryCollectionV1 dataObjects, @QueryParam("message") final String message, @QueryParam("flag") final Integer flag,
             @QueryParam("userId") final String userId);
 
     @POST
     @Path("/category/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTCategoryV1 createJSONCategory(@QueryParam("expand") final String expand, final RESTCategoryV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/categories/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTCategoryCollectionV1 createJSONCategories(@QueryParam("expand") final String expand,
             final RESTCategoryCollectionV1 dataObjects, @QueryParam("message") final String message, @QueryParam("flag") final Integer flag,
             @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/category/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTCategoryV1 deleteJSONCategory(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/categories/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTCategoryCollectionV1 deleteJSONCategories(@PathParam("ids") final PathSegment ids,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId,
             @QueryParam("expand") final String expand);
 
     /* IMAGE FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/image/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPImage(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/image/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPImageRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/images/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPImages(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/images/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPImagesWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/image/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTImageV1 getJSONImage(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/image/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTImageV1 getJSONImageRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/images/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTImageCollectionV1 getJSONImages(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/images/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTImageCollectionV1 getJSONImagesWithQuery(@PathParam("query") final PathSegment query,
             @QueryParam("expand") final String expand);
 
     @POST
     @Path("/image/update/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTImageV1 updateJSONImage(@QueryParam("expand") final String expand, final RESTImageV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/images/update/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTImageCollectionV1 updateJSONImages(@QueryParam("expand") final String expand, final RESTImageCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/image/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTImageV1 createJSONImage(@QueryParam("expand") final String expand, final RESTImageV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/images/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTImageCollectionV1 createJSONImages(@QueryParam("expand") final String expand, final RESTImageCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/image/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTImageV1 deleteJSONImage(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/images/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTImageCollectionV1 deleteJSONImages(@PathParam("ids") final PathSegment ids, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     /* IMAGE */
     @GET
     @Path("/image/get/raw/{id}")
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     @Produces({"image/gif", "image/png", "image/jpeg", "image/svg+xml"})
     public Response getRAWImage(@PathParam("id") final Integer id, @QueryParam("lang") final String locale);
 
     @GET
     @Path("/image/get/raw/{id}/r/{rev}")
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     @Produces({"image/gif", "image/png", "image/jpeg", "image/svg+xml"})
     public Response getRAWImageRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("lang") final String locale);
 
     @GET
     @Path("/image/get/raw/{id}/thumbnail")
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     @Produces({"image/gif", "image/png", "image/jpeg", "image/svg+xml"})
     public Response getRAWImageThumbnail(@PathParam("id") final Integer id, @QueryParam("lang") final String locale);
 
     /* TOPIC FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/topics/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPTopicsWithQuery(@PathParam("query") PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/topics/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPTopics(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/topic/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/topic/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPTopicRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/topics/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTTopicCollectionV1 getJSONTopics(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/topics/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTTopicCollectionV1 getJSONTopicsWithQuery(@PathParam("query") PathSegment query, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/topic/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTTopicV1 getJSONTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/topic/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTTopicV1 getJSONTopicRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @POST
     @Path("/topic/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     public RESTTopicV1 updateJSONTopic(@QueryParam("expand") final String expand, final RESTTopicV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/topics/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     public RESTTopicCollectionV1 updateJSONTopics(@QueryParam("expand") final String expand, final RESTTopicCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/topic/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTTopicV1 createJSONTopic(@QueryParam("expand") final String expand, final RESTTopicV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/topics/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTTopicCollectionV1 createJSONTopics(@QueryParam("expand") final String expand, final RESTTopicCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/topic/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTTopicV1 deleteJSONTopic(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/topics/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTTopicCollectionV1 deleteJSONTopics(@PathParam("ids") final PathSegment ids, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     /* XML FUNCTIONS */
     @GET
     @Path("/topics/get/xml/all")
     @Produces(MediaType.APPLICATION_XML)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTTopicCollectionV1 getXMLTopics(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/topic/get/xml/{id}")
     @Produces(MediaType.APPLICATION_XML)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTTopicV1 getXMLTopic(@PathParam("id") final Integer id);
 
     @GET
     @Path("/topic/get/xml/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_XML)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTTopicV1 getXMLTopicRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision);
 
     @GET
     @Path("/topic/get/xml/{id}/xml")
     @Produces(MediaType.APPLICATION_XML)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getXMLTopicXML(@PathParam("id") final Integer id);
 
     @POST
     @Path("/topic/update/xml/{id}/xml")
     @Produces(MediaType.APPLICATION_XML)
     @Consumes({MediaType.APPLICATION_XML})
     public String updateXMLTopicXML(@PathParam("id") final Integer id, final String xml, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @GET
     @Path("/topic/get/xml/{id}/r/{rev}/xml")
     @Produces(MediaType.APPLICATION_XML)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getXMLTopicRevisionXML(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision);
 
     @GET
     @Path("/topic/get/xml/{id}/xmlContainedIn")
     @Produces(MediaType.APPLICATION_XML)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getXMLTopicXMLContained(@PathParam("id") final Integer id, @QueryParam("container") final String containerName);
 
     @GET
     @Path("/topic/get/xml/{id}/xmlNoContainer")
     @Produces(MediaType.TEXT_PLAIN)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getXMLTopicXMLNoContainer(@PathParam("id") final Integer id, @QueryParam("includeTitle") final Boolean includeTitle);
 
     /* HTML FUNCTIONS */
     @GET
     @Path("/topic/get/html/{id}/html")
     @Produces(MediaType.APPLICATION_XHTML_XML)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getHTMLTopicHTML(@PathParam("id") final Integer id);
 
     @GET
     @Path("/topic/get/html/{id}/r/{rev}/html")
     @Produces(MediaType.APPLICATION_XHTML_XML)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getHTMLTopicRevisionHTML(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision);
 
     /* CSV FUNCTIONS */
     @GET
     @Path("/topics/get/csv/all")
     @Produces(MediaType.TEXT_PLAIN)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getCSVTopics();
 
     @GET
     @Path("/topics/get/csv/{query}")
     @Produces(MediaType.TEXT_PLAIN)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getCSVTopicsWithQuery(@PathParam("query") PathSegment query);
 
     /* ZIP FUNCTIONS */
     @GET
     @Path("/topics/get/zip/all")
     @Produces(MediaType.APPLICATION_OCTET_STREAM)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public byte[] getZIPTopics();
 
     @GET
     @Path("/topics/get/zip/{query}")
     @Produces(MediaType.APPLICATION_OCTET_STREAM)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public byte[] getZIPTopicsWithQuery(@PathParam("query") PathSegment query);
 
     /* FILTERS FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/filter/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPFilter(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/filter/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPFilterRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/filters/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPFiltersWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/filters/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPFilters(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/filter/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTFilterV1 getJSONFilter(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/filter/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTFilterV1 getJSONFilterRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/filters/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTFilterCollectionV1 getJSONFilters(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/filters/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTFilterCollectionV1 getJSONFiltersWithQuery(@PathParam("query") final PathSegment query,
             @QueryParam("expand") final String expand);
 
     @POST
     @Path("/filter/update/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTFilterV1 updateJSONFilter(@QueryParam("expand") final String expand, final RESTFilterV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/filters/update/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTFilterCollectionV1 updateJSONFilters(@QueryParam("expand") final String expand, final RESTFilterCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/filter/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTFilterV1 createJSONFilter(@QueryParam("expand") final String expand, final RESTFilterV1 dataObject,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/filters/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTFilterCollectionV1 createJSONFilters(@QueryParam("expand") final String expand, final RESTFilterCollectionV1 dataObjects,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/filter/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTFilterV1 deleteJSONFilter(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/filters/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTFilterCollectionV1 deleteJSONFilters(@PathParam("ids") final PathSegment ids, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     /* INTEGERCONSTANT FUNCTIONS */
     /* JSONP FUNCTIONS */
     @GET
     @Path("/integerconstant/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPIntegerConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     @GET
     @Path("/integerconstant/get/jsonp/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPIntegerConstantRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/integerconstants/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPIntegerConstants(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback);
 
     @GET
     @Path("/integerconstants/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public String getJSONPIntegerConstantsWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand,
             @QueryParam("callback") final String callback);
 
     /* JSON FUNCTIONS */
     @GET
     @Path("/integerconstant/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTIntegerConstantV1 getJSONIntegerConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
 
     @GET
     @Path("/integerconstant/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTIntegerConstantV1 getJSONIntegerConstantRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision,
             @QueryParam("expand") final String expand);
 
     @GET
     @Path("/integerconstants/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTIntegerConstantCollectionV1 getJSONIntegerConstants(@QueryParam("expand") final String expand);
 
     @GET
     @Path("/integerconstants/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTIntegerConstantCollectionV1 getJSONIntegerConstantsWithQuery(@PathParam("query") final PathSegment query,
             @QueryParam("expand") final String expand);
 
     @POST
     @Path("/integerconstant/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     public RESTIntegerConstantV1 updateJSONIntegerConstant(@QueryParam("expand") final String expand,
             final RESTIntegerConstantV1 dataObject, @QueryParam("message") final String message, @QueryParam("flag") final Integer flag,
             @QueryParam("userId") final String userId);
 
     @POST
     @Path("/integerconstants/update/json")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces(MediaType.APPLICATION_JSON)
     public RESTIntegerConstantCollectionV1 updateJSONIntegerConstants(@QueryParam("expand") final String expand,
             final RESTIntegerConstantCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @POST
     @Path("/integerconstant/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTIntegerConstantV1 createJSONIntegerConstant(@QueryParam("expand") final String expand,
             final RESTIntegerConstantV1 dataObject, @QueryParam("message") final String message, @QueryParam("flag") final Integer flag,
             @QueryParam("userId") final String userId);
 
     @POST
     @Path("/integerconstants/create/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({MediaType.APPLICATION_JSON})
     public RESTIntegerConstantCollectionV1 createJSONIntegerConstants(@QueryParam("expand") final String expand,
             final RESTIntegerConstantCollectionV1 dataObjects, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId);
 
     @DELETE
     @Path("/integerconstant/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTIntegerConstantV1 deleteJSONIntegerConstant(@PathParam("id") final Integer id, @QueryParam("message") final String message,
             @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId, @QueryParam("expand") final String expand);
 
     @DELETE
     @Path("/integerconstants/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
     public RESTIntegerConstantCollectionV1 deleteJSONIntegerConstants(@PathParam("ids") final PathSegment ids,
             @QueryParam("message") final String message, @QueryParam("flag") final Integer flag, @QueryParam("userId") final String userId,
             @QueryParam("expand") final String expand);
 }
