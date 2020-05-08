 package org.jboss.pressgangccms.rest.v1.jaxrsinterfaces;
 
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.PathSegment;
 
 import org.jboss.pressgangccms.rest.v1.collections.RESTBlobConstantCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTCategoryCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTFilterCategoryCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTFilterCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTFilterFieldCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTFilterLocaleCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTFilterTagCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTImageCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTProjectCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTPropertyTagCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTRoleCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTStringConstantCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTTagCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTTopicCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTTranslatedTopicCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTTranslatedTopicStringCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTUserCollectionV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTBlobConstantV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTCategoryV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTFilterCategoryV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTFilterFieldV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTFilterLocaleV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTFilterTagV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTFilterV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTImageV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTProjectV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTPropertyTagV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTRoleV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTStringConstantV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTTagV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTTopicV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTTranslatedTopicStringV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTTranslatedTopicV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTUserV1;
 import org.jboss.pressgangccms.rest.v1.entities.base.RESTLogDetailsV1;
 import org.jboss.pressgangccms.rest.v1.exceptions.InternalProcessingException;
 import org.jboss.pressgangccms.rest.v1.exceptions.InvalidParameterException;
 import org.jboss.pressgangccms.rest.v1.expansion.ExpandDataTrunk;
 
 @Path("/1")
 public interface RESTInterfaceV1
 {
 	String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
 	String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
 	String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
 	String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
 	String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
 	
 	/* SYSTEM FUNCTIONS */
 
 	/**
 	 * Sets the value of the topicindex.rerenderTopic system property, which in
 	 * turn defines if topics are rerendered when they are updated.
 	 * 
 	 * @param enalbed
 	 *            true if rendering to to be enabled, false otherwise
 	 */
 	@PUT
 	@Path("/settings/rerenderTopic")
 	@Consumes({ "*" })
 	public void setRerenderTopic(@QueryParam("enabled") final Boolean enalbed);
 
 	/**
 	 * @return An example of a ExpandDataTrunk object serialized to JSON. This
 	 *         is for convenience, and has no impact on the database or any
 	 *         other processing
 	 */
 	@GET
 	@Path("/expanddatatrunk/get/json")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public ExpandDataTrunk getJSONExpandTrunkExample() throws InvalidParameterException, InternalProcessingException;
 
 	/* TRANSLATEDTOPICSTING FUNCTIONS */
 	/**
 	 * @param id
 	 *            The RESTTranslatedTopicStringV1 ID
 	 * @param expand
 	 *            The expansion options
 	 * @return A JSON representation of the requested
 	 *         RESTTranslatedTopicStringV1 object
 	 * @HTTP 400 if the id is not valid
 	 * @HTTP 500 if there was an unexpected internal error
 	 */
 	@GET
 	@Path("/translatedtopicstring/get/json/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTTranslatedTopicStringV1 getJSONTranslatedTopicString(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 	
 	@GET
 	@Path("/translatedtopicstring/get/jsonp/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String getJSONPTranslatedTopicString(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	/**
 	 * @param expand
 	 * @return JSON representations of all the RESTTranslatedTopicStringV1
 	 *         entities that could be found in the database
 	 * @HTTP 500 if there was an unexpected internal error
 	 */
 	@GET
 	@Path("/translatedtopicstrings/get/json/all")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTTranslatedTopicStringCollectionV1 getJSONTranslatedTopicStrings(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 	
 	@GET
 	@Path("/translatedtopicstrings/get/jsonp/all")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String getJSONPTranslatedTopicStrings(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	/**
 	 * Updates a single RESTTranslatedTopicStringV1 entity.
 	 * 
 	 * @param expand
 	 *            The expansion options
 	 * @param dataObject
 	 *            The new details of the RESTTranslatedTopicStringV1
 	 *            entity. The id property of the entity needs to be set. In
 	 *            addition to setting the properties of the
 	 *            RESTTranslatedTopicStringV1 entity, the
 	 *            configuredParameters variable needs to also needs to reflect
 	 *            those properties that are to be updated. This is to
 	 *            distinguish between an unset property (which is ignored), and
 	 *            a property that might specifically be set to null.
 	 * @return A JSON representation of the RESTTranslatedTopicStringV1
 	 *         after is updated
 	 * @HTTP 400 if the id is not valid
 	 * @HTTP 500 if there was an unexpected internal error
 	 */
 	@PUT
 	@Path("/translatedtopicstring/put/json")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public RESTTranslatedTopicStringV1 updateJSONTranslatedTopicString(@QueryParam("expand") final String expand, final RESTTranslatedTopicStringV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
     @Path("/translatedtopicstring/put/json")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public RESTTranslatedTopicStringV1 updateJSONTranslatedTopicString(@QueryParam("expand") final String expand, final RESTTranslatedTopicStringV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/translatedtopicstring/put/jsonp")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public String updateJSONPTranslatedTopicString(@QueryParam("expand") final String expand, final RESTTranslatedTopicStringV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
     @Path("/translatedtopicstring/put/jsonp")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public String updateJSONPTranslatedTopicString(@QueryParam("expand") final String expand, final RESTTranslatedTopicStringV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@POST
     @Path("/translatedtopicstring/post/json")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public RESTTranslatedTopicStringV1 createJSONTranslatedTopicString(@QueryParam("expand") final String expand, final RESTTranslatedTopicStringV1 dataObject) throws InvalidParameterException, InternalProcessingException;
     
     @POST
     @Path("/translatedtopicstring/post/json")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public RESTTranslatedTopicStringV1 createJSONTranslatedTopicString(@QueryParam("expand") final String expand, final RESTTranslatedTopicStringV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
     
     @POST
     @Path("/translatedtopicstring/post/jsonp")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public String createJSONPTranslatedTopicString(@QueryParam("expand") final String expand, final RESTTranslatedTopicStringV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/translatedtopicstring/post/jsonp")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public String createJSONPTranslatedTopicString(@QueryParam("expand") final String expand, final RESTTranslatedTopicStringV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	/**
 	 * Updates a collection of RESTTranslatedTopicStringV1 entities.
 	 * 
 	 * @param expand
 	 *            The expansion options
 	 * @param dataObjects
 	 *            A collection of RESTTranslatedTopicStringV1 entities,
 	 *            each with their id property set, each with the new properties
 	 *            to be saved, and each with their configuredParameters property
 	 *            set.
 	 * @return The details of the RESTTranslatedTopicStringV1 entities after they have been updated.
 	 * @HTTP 400 if the id is not valid
 	 * @HTTP 500 if there was an unexpected internal error
 	 */
 	@PUT
 	@Path("/translatedtopicstrings/put/json")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public RESTTranslatedTopicStringCollectionV1 updateJSONTranslatedTopicStrings(@QueryParam("expand") final String expand, final RESTTranslatedTopicStringCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
     @Path("/translatedtopicstrings/put/json")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public RESTTranslatedTopicStringCollectionV1 updateJSONTranslatedTopicStrings(@QueryParam("expand") final String expand, final RESTTranslatedTopicStringCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
     
     @PUT
     @Path("/translatedtopicstrings/put/jsonp")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public String updateJSONPTranslatedTopicStrings(@QueryParam("expand") final String expand, final RESTTranslatedTopicStringCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/translatedtopicstrings/put/jsonp")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public String updateJSONPTranslatedTopicStrings(@QueryParam("expand") final String expand, final RESTTranslatedTopicStringCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
     @Path("/translatedtopicstrings/post/json")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public RESTTranslatedTopicStringCollectionV1 createJSONTranslatedTopicStrings(@QueryParam("expand") final String expand, final RESTTranslatedTopicStringCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
     
     @POST
     @Path("/translatedtopicstrings/post/json")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public RESTTranslatedTopicStringCollectionV1 createJSONTranslatedTopicStrings(@QueryParam("expand") final String expand, final RESTTranslatedTopicStringCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
     
     @POST
     @Path("/translatedtopicstrings/post/jsonp")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public String createJSONPTranslatedTopicStrings(@QueryParam("expand") final String expand, final RESTTranslatedTopicStringCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
     
     @POST
     @Path("/translatedtopicstrings/post/jsonp")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public String createJSONPTranslatedTopicStrings(@QueryParam("expand") final String expand, final RESTTranslatedTopicStringCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	
 	/**
 	 * Deletes a single RESTTranslatedTopicStringV1 entity. 
 	 * @param id The id of the entity to be deleted.
 	 * @param expand The expansion options.
 	 * @return The details of the deleted RESTTranslatedTopicStringV1 entity.
 	 * @HTTP 400 if the id is not valid
 	 * @HTTP 500 if there was an unexpected internal error
 	 */
 	@DELETE
 	@Path("/translatedtopicstring/delete/json/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTTranslatedTopicStringV1 deleteJSONTranslatedTopicString(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 	
 	@DELETE
     @Path("/translatedtopicstring/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTTranslatedTopicStringV1 deleteJSONTranslatedTopicString(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
     
     @DELETE
     @Path("/translatedtopicstring/delete/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPTranslatedTopicString(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@DELETE
 	@Path("/translatedtopicstring/delete/jsonp/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String deleteJSONPTranslatedTopicString(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	/**
 	 * Deletes a collection of RESTTranslatedTopicStringV1 entities. 
 	 * @param ids A semicolon separated list of ids to be deleted, starting with the prefix "ids;" e.g. ids;1;13;652
 	 * @param expand The expansion options
 	 * @return The details of the deleted RESTTranslatedTopicStringV1 entities. 
 	 * @HTTP 400 if any of the ids are not valid
 	 * @HTTP 500 if there was an unexpected internal error
 	 */
 	@DELETE
 	@Path("/translatedtopicstrings/delete/json/{ids}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTTranslatedTopicStringCollectionV1 deleteJSONTranslatedTopicStrings(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 	
 	@DELETE
     @Path("/translatedtopicstrings/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTTranslatedTopicStringCollectionV1 deleteJSONTranslatedTopicStrings(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
     
     @DELETE
     @Path("/translatedtopicstrings/delete/jsonp/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPTranslatedTopicStrings(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@DELETE
 	@Path("/translatedtopicstrings/delete/jsonp/{ids}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String deleteJSONPTranslatedTopicStrings(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	/* USER FUNCTIONS */
 	/*		JSONP FUNCTIONS */
 	@GET
 	@Path("/user/get/jsonp/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String getJSONPUser(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/users/get/jsonp/all")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String getJSONPUsers(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
     @Path("/users/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String getJSONPUsersWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/user/put/jsonp")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public String updateJSONPUser(@QueryParam("expand") final String expand, final RESTUserV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
     @Path("/user/put/jsonp")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public String updateJSONPUser(@QueryParam("expand") final String expand, final RESTUserV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/users/put/jsonp")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public String updateJSONPUsers(@QueryParam("expand") final String expand, final RESTUserCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
 	@Path("/users/put/jsonp")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public String updateJSONPUsers(@QueryParam("expand") final String expand, final RESTUserCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/user/post/jsonp")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public String createJSONPUser(@QueryParam("expand") final String expand, final RESTUserV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
     @Path("/user/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPUser(@QueryParam("expand") final String expand, final RESTUserV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/users/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPUsers(@QueryParam("expand") final String expand, final RESTUserCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@POST
 	@Path("/users/post/jsonp")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public String createJSONPUsers(@QueryParam("expand") final String expand, final RESTUserCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/user/delete/jsonp/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String deleteJSONPUser(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/user/delete/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPUser(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
     
     @DELETE
     @Path("/users/delete/jsonp/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPUsers(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@DELETE
 	@Path("/users/delete/jsonp/{ids}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String deleteJSONPUsers(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	/*		JSON FUNCTIONS */
 	@GET
 	@Path("/user/get/json/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTUserV1 getJSONUser(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/users/get/json/all")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTUserCollectionV1 getJSONUsers(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
     @Path("/users/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTUserCollectionV1 getJSONUsersWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/user/put/json")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public RESTUserV1 updateJSONUser(@QueryParam("expand") final String expand, final RESTUserV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
     @Path("/user/put/json")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public RESTUserV1 updateJSONUser(@QueryParam("expand") final String expand, final RESTUserV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/users/put/json")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public RESTUserCollectionV1 updateJSONUsers(@QueryParam("expand") final String expand, final RESTUserCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/users/put/json")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public RESTUserCollectionV1 updateJSONUsers(@QueryParam("expand") final String expand, final RESTUserCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/user/post/json")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public RESTUserV1 createJSONUser(@QueryParam("expand") final String expand, final RESTUserV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
     @Path("/user/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTUserV1 createJSONUser(@QueryParam("expand") final String expand, final RESTUserV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/users/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTUserCollectionV1 createJSONUsers(@QueryParam("expand") final String expand, final RESTUserCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 	
 	@POST
 	@Path("/users/post/json")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public RESTUserCollectionV1 createJSONUsers(@QueryParam("expand") final String expand, final RESTUserCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/user/delete/json/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTUserV1 deleteJSONUser(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
     @Path("/user/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTUserV1 deleteJSONUser(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/users/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTUserCollectionV1 deleteJSONUsers(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 	
 	@DELETE
 	@Path("/users/delete/json/{ids}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTUserCollectionV1 deleteJSONUsers(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	/* STRINGCONSTANT FUNCTIONS */
 	/*		JSONP FUNCTIONS */
 	@GET
 	@Path("/stringconstant/get/jsonp/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String getJSONPStringConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/stringconstants/get/jsonp/all")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String getJSONPStringConstants(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
     @Path("/stringconstants/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String getJSONPStringConstantsWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/stringconstant/put/jsonp")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public String updateJSONPStringConstant(@QueryParam("expand") final String expand, final RESTStringConstantV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
     @Path("/stringconstant/put/jsonp")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public String updateJSONPStringConstant(@QueryParam("expand") final String expand, final RESTStringConstantV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/stringconstants/put/jsonp")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public String updateJSONPStringConstants(@QueryParam("expand") final String expand, final RESTStringConstantCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/stringconstants/put/jsonp")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public String updateJSONPStringConstants(@QueryParam("expand") final String expand, final RESTStringConstantCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/stringconstant/post/jsonp")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public String createJSONPStringConstant(@QueryParam("expand") final String expand, final RESTStringConstantV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
     @Path("/stringconstant/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPStringConstant(@QueryParam("expand") final String expand, final RESTStringConstantV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/stringconstants/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPStringConstants(@QueryParam("expand") final String expand, final RESTStringConstantCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/stringconstants/post/jsonp")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public String createJSONPStringConstants(@QueryParam("expand") final String expand, final RESTStringConstantCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/stringconstant/delete/jsonp/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String deleteJSONPStringConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
     @Path("/stringconstant/delete/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPStringConstant(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/stringconstants/delete/jsonp/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPStringConstants(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/stringconstants/delete/jsonp/{ids}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String deleteJSONPStringConstants(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	/*		JSON FUNCTIONS */
 	@GET
 	@Path("/stringconstant/get/json/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTStringConstantV1 getJSONStringConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/stringconstants/get/json/all")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTStringConstantCollectionV1 getJSONStringConstants(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
    @Path("/stringconstants/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTStringConstantCollectionV1 getJSONStringConstantsWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
 	@Path("/stringconstant/put/json")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public RESTStringConstantV1 updateJSONStringConstant(@QueryParam("expand") final String expand, final RESTStringConstantV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
     @Path("/stringconstant/put/json")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public RESTStringConstantV1 updateJSONStringConstant(@QueryParam("expand") final String expand, final RESTStringConstantV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/stringconstants/put/json")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public RESTStringConstantCollectionV1 updateJSONStringConstants(@QueryParam("expand") final String expand, final RESTStringConstantCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/stringconstants/put/json")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public RESTStringConstantCollectionV1 updateJSONStringConstants(@QueryParam("expand") final String expand, final RESTStringConstantCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/stringconstant/post/json")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public RESTStringConstantV1 createJSONStringConstant(@QueryParam("expand") final String expand, final RESTStringConstantV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
     @Path("/stringconstant/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTStringConstantV1 createJSONStringConstant(@QueryParam("expand") final String expand, final RESTStringConstantV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/stringconstants/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTStringConstantCollectionV1 createJSONStringConstants(@QueryParam("expand") final String expand, final RESTStringConstantCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/stringconstants/post/json")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public RESTStringConstantCollectionV1 createJSONStringConstants(@QueryParam("expand") final String expand, final RESTStringConstantCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/stringconstant/delete/json/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTStringConstantV1 deleteJSONStringConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
     @Path("/stringconstant/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTStringConstantV1 deleteJSONStringConstant(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/stringconstants/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTStringConstantCollectionV1 deleteJSONStringConstants(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
     
 	@DELETE
 	@Path("/stringconstants/delete/json/{ids}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTStringConstantCollectionV1 deleteJSONStringConstants(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	/* TRANSLATEDTOPIC FUNCTIONS */
 	/*		JSONP FUNCTIONS */	
 	@GET
 	@Path("/translatedtopic/get/jsonp/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String getJSONPTranslatedTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/translatedtopics/get/jsonp/all")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String getJSONPTranslatedTopics(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
     @Path("/translatedtopics/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String getJSONPTranslatedTopicsWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/translatedtopic/put/jsonp")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public String updateJSONPTranslatedTopic(@QueryParam("expand") final String expand, final RESTTranslatedTopicV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
     @Path("/translatedtopic/put/jsonp")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public String updateJSONPTranslatedTopic(@QueryParam("expand") final String expand, final RESTTranslatedTopicV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/translatedtopics/put/jsonp")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public String updateJSONPTranslatedTopics(@QueryParam("expand") final String expand, final RESTTranslatedTopicCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
 	@Path("/translatedtopics/put/jsonp")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public String updateJSONPTranslatedTopics(@QueryParam("expand") final String expand, final RESTTranslatedTopicCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/translatedtopic/post/jsonp")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public String createJSONPTranslatedTopic(@QueryParam("expand") final String expand, final RESTTranslatedTopicV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
     @Path("/translatedtopic/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPTranslatedTopic(@QueryParam("expand") final String expand, final RESTTranslatedTopicV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/translatedtopics/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPTranslatedTopics(@QueryParam("expand") final String expand, final RESTTranslatedTopicCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@POST
 	@Path("/translatedtopics/post/jsonp")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public String createJSONPTranslatedTopics(@QueryParam("expand") final String expand, final RESTTranslatedTopicCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/translatedtopic/delete/jsonp/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String deleteJSONPTranslatedTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
     @Path("/translatedtopic/delete/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPTranslatedTopic(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/translatedtopics/delete/jsonp/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPTranslatedTopics(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@DELETE
 	@Path("/translatedtopics/delete/jsonp/{ids}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String deleteJSONPTranslatedTopics(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	/*		JSON FUNCTIONS */	
 	@GET
 	@Path("/translatedtopic/get/json/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTTranslatedTopicV1 getJSONTranslatedTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/translatedtopics/get/json/{query}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTTranslatedTopicCollectionV1 getJSONTranslatedTopicsWithQuery(@PathParam("query") PathSegment query, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 	
 	@GET
 	@Path("/translatedtopics/get/json/all")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTTranslatedTopicCollectionV1 getJSONTranslatedTopics(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
 	@Path("/translatedtopic/put/json")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public RESTTranslatedTopicV1 updateJSONTranslatedTopic(@QueryParam("expand") final String expand, final RESTTranslatedTopicV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
     @Path("/translatedtopic/put/json")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public RESTTranslatedTopicV1 updateJSONTranslatedTopic(@QueryParam("expand") final String expand, final RESTTranslatedTopicV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/translatedtopics/put/json")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public RESTTranslatedTopicCollectionV1 updateJSONTranslatedTopics(@QueryParam("expand") final String expand, final RESTTranslatedTopicCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/translatedtopics/put/json")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public RESTTranslatedTopicCollectionV1 updateJSONTranslatedTopics(@QueryParam("expand") final String expand, final RESTTranslatedTopicCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/translatedtopic/post/json")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public RESTTranslatedTopicV1 createJSONTranslatedTopic(@QueryParam("expand") final String expand, final RESTTranslatedTopicV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
     @Path("/translatedtopic/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTTranslatedTopicV1 createJSONTranslatedTopic(@QueryParam("expand") final String expand, final RESTTranslatedTopicV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/translatedtopics/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTTranslatedTopicCollectionV1 createJSONTranslatedTopics(@QueryParam("expand") final String expand, final RESTTranslatedTopicCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 	
 	@POST
 	@Path("/translatedtopics/post/json")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public RESTTranslatedTopicCollectionV1 createJSONTranslatedTopics(@QueryParam("expand") final String expand, final RESTTranslatedTopicCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/translatedtopic/delete/json/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTTranslatedTopicV1 deleteJSONTranslatedTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
     @Path("/translatedtopic/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTTranslatedTopicV1 deleteJSONTranslatedTopic(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/translatedtopics/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTTranslatedTopicCollectionV1 deleteJSONTranslatedTopics(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/translatedtopics/delete/json/{ids}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTTranslatedTopicCollectionV1 deleteJSONTranslatedTopics(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 	
 	/* ROLE FUNCTIONS */
 	/*		JSONP FUNCTIONS */	
 	@GET
 	@Path("/role/get/jsonp/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String getJSONPRole(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/roles/get/jsonp/all")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String getJSONPRoles(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
     @Path("/roles/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String getJSONPRolesWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/role/put/jsonp")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public String updateJSONPRole(@QueryParam("expand") final String expand, final RESTRoleV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
     @Path("/role/put/jsonp")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public String updateJSONPRole(@QueryParam("expand") final String expand, final RESTRoleV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/roles/put/jsonp")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public String updateJSONPRoles(@QueryParam("expand") final String expand, final RESTRoleCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
 	@Path("/roles/put/jsonp")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public String updateJSONPRoles(@QueryParam("expand") final String expand, final RESTRoleCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/role/post/jsonp")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public String createJSONPRole(@QueryParam("expand") final String expand, final RESTRoleV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
     @Path("/role/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPRole(@QueryParam("expand") final String expand, final RESTRoleV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/roles/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPRoles(@QueryParam("expand") final String expand, final RESTRoleCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@POST
 	@Path("/roles/post/jsonp")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public String createJSONPRoles(@QueryParam("expand") final String expand, final RESTRoleCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/role/delete/jsonp/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String deleteJSONPRole(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
     @Path("/role/delete/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPRole(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/roles/delete/jsonp/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPRoles(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/roles/delete/jsonp/{ids}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String deleteJSONPRoles(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	/*		JSON FUNCTIONS */	
 	@GET
 	@Path("/role/get/json/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTRoleV1 getJSONRole(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/roles/get/json/all")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTRoleCollectionV1 getJSONRoles(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
     @Path("/roles/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTRoleCollectionV1 getJSONRolesWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/role/put/json")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public RESTRoleV1 updateJSONRole(@QueryParam("expand") final String expand, final RESTRoleV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
     @Path("/role/put/json")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public RESTRoleV1 updateJSONRole(@QueryParam("expand") final String expand, final RESTRoleV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/roles/put/json")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public RESTRoleCollectionV1 updateJSONRoles(@QueryParam("expand") final String expand, final RESTRoleCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/roles/put/json")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public RESTRoleCollectionV1 updateJSONRoles(@QueryParam("expand") final String expand, final RESTRoleCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/role/post/json")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public RESTRoleV1 createJSONRole(@QueryParam("expand") final String expand, final RESTRoleV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
     @Path("/role/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTRoleV1 createJSONRole(@QueryParam("expand") final String expand, final RESTRoleV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/roles/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTRoleCollectionV1 createJSONRoles(@QueryParam("expand") final String expand, final RESTRoleCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 	
 	@POST
 	@Path("/roles/post/json")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public RESTRoleCollectionV1 createJSONRoles(@QueryParam("expand") final String expand, final RESTRoleCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/role/delete/json/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTRoleV1 deleteJSONRole(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
     @Path("/role/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTRoleV1 deleteJSONRole(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/roles/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTRoleCollectionV1 deleteJSONRoles(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 	
 	@DELETE
 	@Path("/roles/delete/json/{ids}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTRoleCollectionV1 deleteJSONRoles(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	/* PROPERYTAG FUNCTIONS */
 	/*		JSONP FUNCTIONS */	
 	@GET
 	@Path("/propertytag/get/jsonp/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String getJSONPPropertyTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/propertytags/get/jsonp/all")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String getJSONPPropertyTags(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
     @Path("/propertytags/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String getJSONPPropertyTagsWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/propertytag/put/jsonp")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public String updateJSONPPropertyTag(@QueryParam("expand") final String expand, final RESTPropertyTagV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
     @Path("/propertytag/put/jsonp")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public String updateJSONPPropertyTag(@QueryParam("expand") final String expand, final RESTPropertyTagV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/propertytags/put/jsonp")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public String updateJSONPPropertyTags(@QueryParam("expand") final String expand, final RESTPropertyTagCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/propertytags/put/jsonp")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public String updateJSONPPropertyTags(@QueryParam("expand") final String expand, final RESTPropertyTagCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/propertytag/post/jsonp")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public String createJSONPPropertyTag(@QueryParam("expand") final String expand, final RESTPropertyTagV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
     @Path("/propertytag/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPPropertyTag(@QueryParam("expand") final String expand, final RESTPropertyTagV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/propertytags/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPPropertyTags(@QueryParam("expand") final String expand, final RESTPropertyTagCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@POST
 	@Path("/propertytags/post/jsonp")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public String createJSONPPropertyTags(@QueryParam("expand") final String expand, final RESTPropertyTagCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/propertytag/delete/jsonp/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String deleteJSONPPropertyTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
     @Path("/propertytag/delete/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPPropertyTag(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/propertytags/delete/jsonp/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPPropertyTags(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@DELETE
 	@Path("/propertytags/delete/jsonp/{ids}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String deleteJSONPPropertyTags(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	/*		JSON FUNCTIONS */	
 	@GET
 	@Path("/propertytag/get/json/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTPropertyTagV1 getJSONPropertyTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/propertytags/get/json/all")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTPropertyTagCollectionV1 getJSONPropertyTags(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 	
 	@GET
     @Path("/propertytags/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTPropertyTagCollectionV1 getJSONPropertyTagsWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
 	@Path("/propertytag/put/json")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public RESTPropertyTagV1 updateJSONPropertyTag(@QueryParam("expand") final String expand, final RESTPropertyTagV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
     @Path("/propertytag/put/json")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public RESTPropertyTagV1 updateJSONPropertyTag(@QueryParam("expand") final String expand, final RESTPropertyTagV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/propertytags/put/json")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public RESTPropertyTagCollectionV1 updateJSONPropertyTags(@QueryParam("expand") final String expand, final RESTPropertyTagCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/propertytags/put/json")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public RESTPropertyTagCollectionV1 updateJSONPropertyTags(@QueryParam("expand") final String expand, final RESTPropertyTagCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/propertytag/post/json")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public RESTPropertyTagV1 createJSONPropertyTag(@QueryParam("expand") final String expand, final RESTPropertyTagV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
     @Path("/propertytag/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTPropertyTagV1 createJSONPropertyTag(@QueryParam("expand") final String expand, final RESTPropertyTagV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/propertytags/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTPropertyTagCollectionV1 createJSONPropertyTags(@QueryParam("expand") final String expand, final RESTPropertyTagCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 	
 	@POST
 	@Path("/propertytags/post/json")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public RESTPropertyTagCollectionV1 createJSONPropertyTags(@QueryParam("expand") final String expand, final RESTPropertyTagCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/propertytag/delete/json/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTPropertyTagV1 deleteJSONPropertyTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
     @Path("/propertytag/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTPropertyTagV1 deleteJSONPropertyTag(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/propertytags/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTPropertyTagCollectionV1 deleteJSONPropertyTags(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 	
 	@DELETE
 	@Path("/propertytags/delete/json/{ids}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTPropertyTagCollectionV1 deleteJSONPropertyTags(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	/* BLOBCONSTANTS FUNCTIONS */
 	/*		JSONP FUNCTIONS */	
 	@GET
 	@Path("/blobconstant/get/jsonp/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String getJSONPBlobConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/blobconstants/get/jsonp/{query}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String getJSONPBlobConstantsWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
     @Path("/blobconstants/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String getJSONPBlobConstants(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/blobconstant/put/jsonp")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public String updateJSONPBlobConstant(@QueryParam("expand") final String expand, final RESTBlobConstantV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
     @Path("/blobconstant/put/jsonp")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public String updateJSONPBlobConstant(@QueryParam("expand") final String expand, final RESTBlobConstantV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/blobconstants/put/jsonp")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public String updateJSONPBlobConstants(@QueryParam("expand") final String expand, final RESTBlobConstantCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/blobconstants/put/jsonp")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public String updateJSONPBlobConstants(@QueryParam("expand") final String expand, final RESTBlobConstantCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/blobconstant/post/jsonp")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public String createJSONPBlobConstant(@QueryParam("expand") final String expand, final RESTBlobConstantV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
     @Path("/blobconstant/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPBlobConstant(@QueryParam("expand") final String expand, final RESTBlobConstantV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/blobconstants/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPBlobConstants(@QueryParam("expand") final String expand, final RESTBlobConstantCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@POST
 	@Path("/blobconstants/post/jsonp")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public String createJSONPBlobConstants(@QueryParam("expand") final String expand, final RESTBlobConstantCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/blobconstant/delete/jsonp/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String deleteJSONPBlobConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
     @Path("/blobconstant/delete/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPBlobConstant(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/blobconstants/delete/jsonp/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPBlobConstants(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/blobconstants/delete/jsonp/{ids}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String deleteJSONPBlobConstants(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	/*		JSON FUNCTIONS */	
 	@GET
 	@Path("/blobconstant/get/json/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTBlobConstantV1 getJSONBlobConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/blobconstants/get/json/all")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTBlobConstantCollectionV1 getJSONBlobConstants(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
     @Path("/blobconstants/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTBlobConstantCollectionV1 getJSONBlobConstantsWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/blobconstant/put/json")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public RESTBlobConstantV1 updateJSONBlobConstant(@QueryParam("expand") final String expand, final RESTBlobConstantV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
     @Path("/blobconstant/put/json")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public RESTBlobConstantV1 updateJSONBlobConstant(@QueryParam("expand") final String expand, final RESTBlobConstantV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/blobconstants/put/json")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public RESTBlobConstantCollectionV1 updateJSONBlobConstants(@QueryParam("expand") final String expand, final RESTBlobConstantCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/blobconstants/put/json")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public RESTBlobConstantCollectionV1 updateJSONBlobConstants(@QueryParam("expand") final String expand, final RESTBlobConstantCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/blobconstant/post/json")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public RESTBlobConstantV1 createJSONBlobConstant(@QueryParam("expand") final String expand, final RESTBlobConstantV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
     @Path("/blobconstant/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTBlobConstantV1 createJSONBlobConstant(@QueryParam("expand") final String expand, final RESTBlobConstantV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/blobconstants/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTBlobConstantCollectionV1 createJSONBlobConstants(@QueryParam("expand") final String expand, final RESTBlobConstantCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/blobconstants/post/json")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public RESTBlobConstantCollectionV1 createJSONBlobConstants(@QueryParam("expand") final String expand, final RESTBlobConstantCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/blobconstant/delete/json/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTBlobConstantV1 deleteJSONBlobConstant(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
     @Path("/blobconstant/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTBlobConstantV1 deleteJSONBlobConstant(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/blobconstants/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTBlobConstantCollectionV1 deleteJSONBlobConstants(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/blobconstants/delete/json/{ids}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTBlobConstantCollectionV1 deleteJSONBlobConstants(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	/* PROJECT FUNCTIONS */
 	/*		JSONP FUNCTIONS */
 	@GET
 	@Path("/project/get/jsonp/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String getJSONPProject(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/projects/get/jsonp/all")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String getJSONPProjects(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
     @Path("/projects/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String getJSONPProjectsWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/project/put/jsonp")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public String updateJSONPProject(@QueryParam("expand") final String expand, final RESTProjectV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
     @Path("/project/put/jsonp")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public String updateJSONPProject(@QueryParam("expand") final String expand, final RESTProjectV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/projects/put/jsonp")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public String updateJSONPProjects(@QueryParam("expand") final String expand, final RESTProjectCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
 	@Path("/projects/put/jsonp")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public String updateJSONPProjects(@QueryParam("expand") final String expand, final RESTProjectCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/project/post/jsonp")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public String createJSONPProject(@QueryParam("expand") final String expand, final RESTProjectV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
     @Path("/project/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPProject(@QueryParam("expand") final String expand, final RESTProjectV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/projects/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPProjects(@QueryParam("expand") final String expand, final RESTProjectCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/projects/post/jsonp")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public String createJSONPProjects(@QueryParam("expand") final String expand, final RESTProjectCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
 	@Path("/project/delete/jsonp/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String deleteJSONPProject(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/project/delete/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPProject(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/projects/delete/jsonp/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPProjects(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/projects/delete/jsonp/{ids}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String deleteJSONPProjects(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	/*		JSON FUNCTIONS */	
 	@GET
 	@Path("/project/get/json/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTProjectV1 getJSONProject(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/projects/get/json/all")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTProjectCollectionV1 getJSONProjects(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
     @Path("/projects/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTProjectCollectionV1 getJSONProjectsWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/project/put/json")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public RESTProjectV1 updateJSONProject(@QueryParam("expand") final String expand, final RESTProjectV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
     @Path("/project/put/json")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public RESTProjectV1 updateJSONProject(@QueryParam("expand") final String expand, final RESTProjectV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/projects/put/json")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public RESTProjectCollectionV1 updateJSONProjects(@QueryParam("expand") final String expand, final RESTProjectCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
 	@Path("/projects/put/json")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public RESTProjectCollectionV1 updateJSONProjects(@QueryParam("expand") final String expand, final RESTProjectCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/project/post/json")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public RESTProjectV1 createJSONProject(@QueryParam("expand") final String expand, final RESTProjectV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
     @Path("/project/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTProjectV1 createJSONProject(@QueryParam("expand") final String expand, final RESTProjectV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/projects/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTProjectCollectionV1 createJSONProjects(@QueryParam("expand") final String expand, final RESTProjectCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/projects/post/json")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public RESTProjectCollectionV1 createJSONProjects(@QueryParam("expand") final String expand, final RESTProjectCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/project/delete/json/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTProjectV1 deleteJSONProject(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
     @Path("/project/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTProjectV1 deleteJSONProject(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/projects/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTProjectCollectionV1 deleteJSONProjects(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/projects/delete/json/{ids}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTProjectCollectionV1 deleteJSONProjects(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	/* TAG FUNCTIONS */
 	/*		JSONP FUNCTIONS */	
 	@GET
 	@Path("/tag/get/jsonp/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String getJSONPTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/tags/get/jsonp/all")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String getJSONPTags(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
     @Path("/tags/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String getJSONPTagsWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/tag/put/jsonp")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public String updateJSONPTag(@QueryParam("expand") final String expand, final RESTTagV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
     @Path("/tag/put/jsonp")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public String updateJSONPTag(@QueryParam("expand") final String expand, final RESTTagV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/tags/put/jsonp")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public String updateJSONPTags(@QueryParam("expand") final String expand, final RESTTagCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
 	@Path("/tags/put/jsonp")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public String updateJSONPTags(@QueryParam("expand") final String expand, final RESTTagCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/tag/post/jsonp")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public String createJSONPTag(@QueryParam("expand") final String expand, final RESTTagV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
     @Path("/tag/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPTag(@QueryParam("expand") final String expand, final RESTTagV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/tags/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPTags(@QueryParam("expand") final String expand, final RESTTagCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/tags/post/jsonp")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public String createJSONPTags(@QueryParam("expand") final String expand, final RESTTagCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/tag/delete/jsonp/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String deleteJSONPTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
     @Path("/tag/delete/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPTag(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/tags/delete/jsonp/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPTags(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/tags/delete/jsonp/{ids}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String deleteJSONPTags(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	/*		JSON FUNCTIONS */	
 	@GET
 	@Path("/tag/get/json/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTTagV1 getJSONTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/tags/get/json/all")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTTagCollectionV1 getJSONTags(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 	
 	@GET
     @Path("/tags/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTTagCollectionV1 getJSONTagsWithQuery(@PathParam("query") PathSegment query, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
 	@Path("/tag/put/json")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public RESTTagV1 updateJSONTag(@QueryParam("expand") final String expand, final RESTTagV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
     @Path("/tag/put/json")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public RESTTagV1 updateJSONTag(@QueryParam("expand") final String expand, final RESTTagV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/tags/put/json")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public RESTTagCollectionV1 updateJSONTags(@QueryParam("expand") final String expand, final RESTTagCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
 	@Path("/tags/put/json")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public RESTTagCollectionV1 updateJSONTags(@QueryParam("expand") final String expand, final RESTTagCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/tag/post/json")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public RESTTagV1 createJSONTag(@QueryParam("expand") final String expand, final RESTTagV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
     @Path("/tag/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTTagV1 createJSONTag(@QueryParam("expand") final String expand, final RESTTagV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/tags/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTTagCollectionV1 createJSONTags(@QueryParam("expand") final String expand, final RESTTagCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 	
 	@POST
 	@Path("/tags/post/json")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public RESTTagCollectionV1 createJSONTags(@QueryParam("expand") final String expand, final RESTTagCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/tag/delete/json/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTTagV1 deleteJSONTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
     @Path("/tag/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTTagV1 deleteJSONTag(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/tags/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTTagCollectionV1 deleteJSONTags(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/tags/delete/json/{ids}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTTagCollectionV1 deleteJSONTags(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	/* CATEGORY FUNCTIONS */
 	/*		JSONP FUNCTIONS */
 	@GET
 	@Path("/category/get/jsonp/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String getJSONPCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/categories/get/jsonp/all")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String getJSONPCategories(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
     @Path("/categories/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String getJSONPCategoriesWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/category/put/jsonp")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public String updateJSONPCategory(@QueryParam("expand") final String expand, final RESTCategoryV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
     @Path("/category/put/jsonp")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public String updateJSONPCategory(@QueryParam("expand") final String expand, final RESTCategoryV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/categories/put/jsonp")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public String updateJSONPCategories(@QueryParam("expand") final String expand, final RESTCategoryCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
 	@Path("/categories/put/jsonp")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public String updateJSONPCategories(@QueryParam("expand") final String expand, final RESTCategoryCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/category/post/jsonp")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public String createJSONPCategory(@QueryParam("expand") final String expand, final RESTCategoryV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
     @Path("/category/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPCategory(@QueryParam("expand") final String expand, final RESTCategoryV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/categories/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPCategories(@QueryParam("expand") final String expand, final RESTCategoryCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@POST
 	@Path("/categories/post/jsonp")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public String createJSONPCategories(@QueryParam("expand") final String expand, final RESTCategoryCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/category/delete/jsonp/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String deleteJSONPCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
     @Path("/category/delete/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPCategory(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/categories/delete/jsonp/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPCategories(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/categories/delete/jsonp/{ids}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String deleteJSONPCategories(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	/*		JSON FUNCTIONS */	
 	@GET
 	@Path("/category/get/json/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTCategoryV1 getJSONCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/categories/get/json/all")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTCategoryCollectionV1 getJSONCategories(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
     @Path("/categories/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTCategoryCollectionV1 getJSONCategoriesWithQuery(@PathParam("query") PathSegment query, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/category/put/json")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public RESTCategoryV1 updateJSONCategory(@QueryParam("expand") final String expand, final RESTCategoryV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
     @Path("/category/put/json")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public RESTCategoryV1 updateJSONCategory(@QueryParam("expand") final String expand, final RESTCategoryV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/categories/put/json")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public RESTCategoryCollectionV1 updateJSONCategories(@QueryParam("expand") final String expand, final RESTCategoryCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
 	@Path("/categories/put/json")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public RESTCategoryCollectionV1 updateJSONCategories(@QueryParam("expand") final String expand, final RESTCategoryCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/category/post/json")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public RESTCategoryV1 createJSONCategory(@QueryParam("expand") final String expand, final RESTCategoryV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
     @Path("/category/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTCategoryV1 createJSONCategory(@QueryParam("expand") final String expand, final RESTCategoryV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/categories/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTCategoryCollectionV1 createJSONCategories(@QueryParam("expand") final String expand, final RESTCategoryCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/categories/post/json")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public RESTCategoryCollectionV1 createJSONCategories(@QueryParam("expand") final String expand, final RESTCategoryCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/category/delete/json/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTCategoryV1 deleteJSONCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
     @Path("/category/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTCategoryV1 deleteJSONCategory(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/categories/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTCategoryCollectionV1 deleteJSONCategories(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/categories/delete/json/{ids}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTCategoryCollectionV1 deleteJSONCategories(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	/* IMAGE FUNCTIONS */
 	/*		JSONP FUNCTIONS */	
 	@GET
 	@Path("/image/get/jsonp/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String getJSONPImage(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/images/get/jsonp/all")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String getJSONPImages(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @GET
     @Path("/images/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String getJSONPImagesWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/image/put/jsonp")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public String updateJSONPImage(@QueryParam("expand") final String expand, final RESTImageV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
     @Path("/image/put/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String updateJSONPImage(@QueryParam("expand") final String expand, final RESTImageV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/images/put/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String updateJSONPImages(@QueryParam("expand") final String expand, final RESTImageCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
 	@Path("/images/put/jsonp")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public String updateJSONPImages(@QueryParam("expand") final String expand, final RESTImageCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/image/post/jsonp")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public String createJSONPImage(@QueryParam("expand") final String expand, final RESTImageV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
     @Path("/image/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPImage(@QueryParam("expand") final String expand, final RESTImageV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/images/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPImages(@QueryParam("expand") final String expand, final RESTImageCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/images/post/jsonp")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public String createJSONPImages(@QueryParam("expand") final String expand, final RESTImageCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/image/delete/jsonp/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String deleteJSONPImage(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
     @Path("/image/delete/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPImage(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/images/delete/jsonp/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPImages(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/images/delete/jsonp/{ids}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String deleteJSONPImages(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	/*		JSON FUNCTIONS */	
 	@GET
 	@Path("/image/get/json/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTImageV1 getJSONImage(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/image/get/json/{id}/r/{rev}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTImageV1 getJSONImageRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/images/get/json/all")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTImageCollectionV1 getJSONImages(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 	
 	@GET
     @Path("/images/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTImageCollectionV1 getJSONImagesWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
 	@Path("/image/put/json")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public RESTImageV1 updateJSONImage(@QueryParam("expand") final String expand, final RESTImageV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
     @Path("/image/put/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTImageV1 updateJSONImage(@QueryParam("expand") final String expand, final RESTImageV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/images/put/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTImageCollectionV1 updateJSONImages(@QueryParam("expand") final String expand, final RESTImageCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
 	@Path("/images/put/json")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public RESTImageCollectionV1 updateJSONImages(@QueryParam("expand") final String expand, final RESTImageCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/image/post/json")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public RESTImageV1 createJSONImage(@QueryParam("expand") final String expand, final RESTImageV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
     @Path("/image/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTImageV1 createJSONImage(@QueryParam("expand") final String expand, final RESTImageV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/images/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTImageCollectionV1 createJSONImages(@QueryParam("expand") final String expand, final RESTImageCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/images/post/json")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public RESTImageCollectionV1 createJSONImages(@QueryParam("expand") final String expand, final RESTImageCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/image/delete/json/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTImageV1 deleteJSONImage(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
     @Path("/image/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTImageV1 deleteJSONImage(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/images/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTImageCollectionV1 deleteJSONImages(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
     
 	@DELETE
 	@Path("/images/delete/json/{ids}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTImageCollectionV1 deleteJSONImages(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 	
 	/* TOPIC FUNCTIONS */
 	/*		JSONP FUNCTIONS */	
 	@GET
 	@Path("/topics/get/jsonp/{query}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String getJSONPTopicsWithQuery(@PathParam("query") PathSegment query, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	@GET
 	@Path("/topics/get/jsonp/all")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String getJSONPTopics(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/topic/get/jsonp/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String getJSONPTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
 	@Path("/topic/put/jsonp")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public String updateJSONPTopic(@QueryParam("expand") final String expand, final RESTTopicV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
     @Path("/topic/put/jsonp")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public String updateJSONPTopic(@QueryParam("expand") final String expand, final RESTTopicV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/topics/put/jsonp")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public String updateJSONPTopics(@QueryParam("expand") final String expand, final RESTTopicCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
 	@Path("/topics/put/jsonp")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public String updateJSONPTopics(@QueryParam("expand") final String expand, final RESTTopicCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/topic/post/jsonp")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public String createJSONPTopic(@QueryParam("expand") final String expand, final RESTTopicV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
     @Path("/topic/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPTopic(@QueryParam("expand") final String expand, final RESTTopicV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/topics/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPTopics(@QueryParam("expand") final String expand, final RESTTopicCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/topics/post/jsonp")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public String createJSONPTopics(@QueryParam("expand") final String expand, final RESTTopicCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/topic/delete/jsonp/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String deleteJSONPTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
     @Path("/topic/delete/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPTopic(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/topics/delete/jsonp/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPTopics(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/topics/delete/jsonp/{ids}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public String deleteJSONPTopics(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 	
 	/*		JSON FUNCTIONS */	
 	@GET
 	@Path("/topics/get/json/all")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTTopicCollectionV1 getJSONTopics(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/topics/get/json/{query}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTTopicCollectionV1 getJSONTopicsWithQuery(@PathParam("query") PathSegment query, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 	
 	@GET
 	@Path("/topics/get/xml/all")
 	@Produces(MediaType.APPLICATION_XML)
 	@Consumes({ "*" })
 	public RESTTopicCollectionV1 getXMLTopics(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/topic/get/json/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTTopicV1 getJSONTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/topic/get/json/{id}/r/{rev}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTTopicV1 getJSONTopicRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 	
 	@GET
 	@Path("/topic/get/xml/{id}")
 	@Produces(MediaType.APPLICATION_XML)
 	@Consumes({ "*" })
 	public RESTTopicV1 getXMLTopic(@PathParam("id") final Integer id) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/topic/get/xml/{id}/r/{rev}")
 	@Produces(MediaType.APPLICATION_XML)
 	@Consumes({ "*" })
 	public RESTTopicV1 getXMLTopicRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision) throws InvalidParameterException, InternalProcessingException;
 	
 	@GET
 	@Path("/topic/get/xml/{id}/xml")
 	@Produces(MediaType.APPLICATION_XML)
 	@Consumes({ "*" })
 	public String getXMLTopicXML(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/topic/get/xml/{id}/xmlContainedIn")
 	@Produces(MediaType.APPLICATION_XML)
 	@Consumes({ "*" })
 	public String getXMLTopicXMLContained(@PathParam("id") final Integer id, @QueryParam("container") final String containerName) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/topic/get/xml/{id}/xmlNoContainer")
 	@Produces(MediaType.TEXT_PLAIN)
 	@Consumes({ "*" })
 	public String getXMLTopicXMLNoContainer(@PathParam("id") final Integer id, @QueryParam("includeTitle") final Boolean includeTitle) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/topic/get/html/{id}/html")
 	@Produces(MediaType.APPLICATION_XHTML_XML)
 	@Consumes({ "*" })
 	public String getHTMLTopicHTML(@PathParam("id") final Integer id) throws InvalidParameterException, InternalProcessingException;
 
 	@GET
 	@Path("/topic/get/html/{id}/r/{rev}/html")
 	@Produces(MediaType.APPLICATION_XHTML_XML)
 	@Consumes({ "*" })
 	public String getHTMLTopicRevisionHTML(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision) throws InvalidParameterException, InternalProcessingException;
 	
 	@PUT
 	@Path("/topic/put/json")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public RESTTopicV1 updateJSONTopic(@QueryParam("expand") final String expand, final RESTTopicV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
     @Path("/topic/put/json")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public RESTTopicV1 updateJSONTopic(@QueryParam("expand") final String expand, final RESTTopicV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/topics/put/json")
     @Consumes({ MediaType.APPLICATION_JSON })
     @Produces(MediaType.APPLICATION_JSON)
     public RESTTopicCollectionV1 updateJSONTopics(@QueryParam("expand") final String expand, final RESTTopicCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 
 	@PUT
 	@Path("/topics/put/json")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces(MediaType.APPLICATION_JSON)
 	public RESTTopicCollectionV1 updateJSONTopics(@QueryParam("expand") final String expand, final RESTTopicCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
 	@Path("/topic/post/json")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public RESTTopicV1 createJSONTopic(@QueryParam("expand") final String expand, final RESTTopicV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
 	@POST
     @Path("/topic/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTTopicV1 createJSONTopic(@QueryParam("expand") final String expand, final RESTTopicV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/topics/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTTopicCollectionV1 createJSONTopics(@QueryParam("expand") final String expand, final RESTTopicCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 	
 	@POST
 	@Path("/topics/post/json")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public RESTTopicCollectionV1 createJSONTopics(@QueryParam("expand") final String expand, final RESTTopicCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/topic/delete/json/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTTopicV1 deleteJSONTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
     @Path("/topic/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTTopicV1 deleteJSONTopic(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/topics/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTTopicCollectionV1 deleteJSONTopics(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
 	@DELETE
 	@Path("/topics/delete/json/{ids}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTTopicCollectionV1 deleteJSONTopics(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 	
 	/*     FILTERS FUNCTIONS */
 	/*     JSONP FUNCTIONS */  
     @GET
     @Path("/filter/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String getJSONPFilter(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @GET
     @Path("/filters/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String getJSONPFilters(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @GET
     @Path("/filters/get/jsonp/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String getJSONPFiltersWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException; 
     
     @PUT
     @Path("/filter/put/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String updateJSONPFilter(@QueryParam("expand") final String expand, final RESTFilterV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filter/put/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String updateJSONPFilter(@QueryParam("expand") final String expand, final RESTFilterV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filters/put/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String updateJSONPFilters(@QueryParam("expand") final String expand, final RESTFilterCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filters/put/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String updateJSONPFilters(@QueryParam("expand") final String expand, final RESTFilterCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filter/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPFilter(@QueryParam("expand") final String expand, final RESTFilterV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filter/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPFilter(@QueryParam("expand") final String expand, final RESTFilterV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filters/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPFilters(@QueryParam("expand") final String expand, final RESTFilterCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filters/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPFilters(@QueryParam("expand") final String expand, final RESTFilterCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filter/delete/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPFilter(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filter/delete/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPFilter(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filters/delete/jsonp/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPFilters(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filters/delete/jsonp/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPFilters(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
     
     /*      JSON FUNCTIONS */   
     @GET
     @Path("/filter/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterV1 getJSONFilter(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @GET
     @Path("/filter/get/json/{id}/r/{rev}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterV1 getJSONFilterRevision(@PathParam("id") final Integer id, @PathParam("rev") final Integer revision, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @GET
     @Path("/filters/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterCollectionV1 getJSONFilters(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
     
     @GET
     @Path("/filters/get/json/{query}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterCollectionV1 getJSONFiltersWithQuery(@PathParam("query") final PathSegment query, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
     
     @PUT
     @Path("/filter/put/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterV1 updateJSONFilter(@QueryParam("expand") final String expand, final RESTFilterV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filter/put/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterV1 updateJSONFilter(@QueryParam("expand") final String expand, final RESTFilterV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filters/put/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterCollectionV1 updateJSONFilters(@QueryParam("expand") final String expand, final RESTFilterCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filters/put/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterCollectionV1 updateJSONFilters(@QueryParam("expand") final String expand, final RESTFilterCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filter/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterV1 createJSONFilter(@QueryParam("expand") final String expand, final RESTFilterV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filter/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterV1 createJSONFilter(@QueryParam("expand") final String expand, final RESTFilterV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filters/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterCollectionV1 createJSONFilters(@QueryParam("expand") final String expand, final RESTFilterCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filters/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterCollectionV1 createJSONFilters(@QueryParam("expand") final String expand, final RESTFilterCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filter/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterV1 deleteJSONFilter(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filter/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterV1 deleteJSONFilter(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filters/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterCollectionV1 deleteJSONFilters(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
     
     @DELETE
     @Path("/filters/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterCollectionV1 deleteJSONFilters(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
     
     /*     FILTER CATEGORY FUNCTIONS */
     /*     JSONP FUNCTIONS */  
     @GET
     @Path("/filtercategory/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String getJSONPFilterCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @GET
     @Path("/filtercategories/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String getJSONPFilterCategories(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filtercategory/put/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String updateJSONPFilterCategory(@QueryParam("expand") final String expand, final RESTFilterCategoryV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filtercategory/put/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String updateJSONPFilterCategory(@QueryParam("expand") final String expand, final RESTFilterCategoryV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filtercategories/put/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String updateJSONPFilterCategories(@QueryParam("expand") final String expand, final RESTFilterCategoryCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filtercategories/put/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String updateJSONPFilterCategories(@QueryParam("expand") final String expand, final RESTFilterCategoryCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filtercategory/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPFilterCategory(@QueryParam("expand") final String expand, final RESTFilterCategoryV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filtercategory/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPFilterCategory(@QueryParam("expand") final String expand, final RESTFilterCategoryV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filtercategories/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPFilterCategories(@QueryParam("expand") final String expand, final RESTFilterCategoryCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filtercategories/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPFilterCategories(@QueryParam("expand") final String expand, final RESTFilterCategoryCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filtercategory/delete/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPFilterCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filtercategory/delete/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPFilterCategory(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filtercategories/delete/jsonp/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPFilterCategories(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filtercategories/delete/jsonp/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPFilterCategories(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
     
     /*      JSON FUNCTIONS */   
     @GET
     @Path("/filtercategory/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterCategoryV1 getJSONFilterCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @GET
     @Path("/filtercategories/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterCategoryCollectionV1 getJSONFilterCategories(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filtercategory/put/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterCategoryV1 updateJSONFilterCategory(@QueryParam("expand") final String expand, final RESTFilterCategoryV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filtercategory/put/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterCategoryV1 updateJSONFilterCategory(@QueryParam("expand") final String expand, final RESTFilterCategoryV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filtercategories/put/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterCategoryCollectionV1 updateJSONFilterCategories(@QueryParam("expand") final String expand, final RESTFilterCategoryCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filtercategories/put/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterCategoryCollectionV1 updateJSONFilterCategories(@QueryParam("expand") final String expand, final RESTFilterCategoryCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filtercategory/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterCategoryV1 createJSONFilterCategory(@QueryParam("expand") final String expand, final RESTFilterCategoryV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filtercategory/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterCategoryV1 createJSONFilterCategory(@QueryParam("expand") final String expand, final RESTFilterCategoryV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filtercategories/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterCategoryCollectionV1 createJSONFilterCategories(@QueryParam("expand") final String expand, final RESTFilterCategoryCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filtercategories/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterCategoryCollectionV1 createJSONFilterCategories(@QueryParam("expand") final String expand, final RESTFilterCategoryCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filtercategory/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterCategoryV1 deleteJSONFilterCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filtercategory/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterCategoryV1 deleteJSONFilterCategory(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filters/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterCategoryCollectionV1 deleteJSONFilterCategories(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
     
     @DELETE
     @Path("/filters/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterCategoryCollectionV1 deleteJSONFilterCategories(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 	
     /*     FILTER TAG FUNCTIONS */
     /*     JSONP FUNCTIONS */  
     @GET
     @Path("/filtertag/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String getJSONPFilterTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @GET
     @Path("/filtertags/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String getJSONPFilterTags(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filtertag/put/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String updateJSONPFilterTag(@QueryParam("expand") final String expand, final RESTFilterTagV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filtertag/put/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String updateJSONPFilterTag(@QueryParam("expand") final String expand, final RESTFilterTagV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filtertags/put/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String updateJSONPFilterTags(@QueryParam("expand") final String expand, final RESTFilterTagCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filtertags/put/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String updateJSONPFilterTags(@QueryParam("expand") final String expand, final RESTFilterTagCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filtertag/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPFilterTag(@QueryParam("expand") final String expand, final RESTFilterTagV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filtertag/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPFilterTag(@QueryParam("expand") final String expand, final RESTFilterTagV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filtertags/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPFilterTags(@QueryParam("expand") final String expand, final RESTFilterTagCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filtertags/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPFilterTags(@QueryParam("expand") final String expand, final RESTFilterTagCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filtertag/delete/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPFilterTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filtertag/delete/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPFilterTag(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filtertags/delete/jsonp/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPFilterTags(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filtertags/delete/jsonp/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPFilterTags(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
     
     /*      JSON FUNCTIONS */   
     @GET
     @Path("/filtertag/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterTagV1 getJSONFilterTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
     
     @GET
     @Path("/filtertags/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterTagCollectionV1 getJSONFilterTags(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filtertag/put/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterTagV1 updateJSONFilterTag(@QueryParam("expand") final String expand, final RESTFilterTagV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filtertag/put/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterTagV1 updateJSONFilterTag(@QueryParam("expand") final String expand, final RESTFilterTagV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filtertags/put/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterTagCollectionV1 updateJSONFilterTags(@QueryParam("expand") final String expand, final RESTFilterTagCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filtertags/put/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterTagCollectionV1 updateJSONFilterTags(@QueryParam("expand") final String expand, final RESTFilterTagCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filtertag/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterTagV1 createJSONFilterTag(@QueryParam("expand") final String expand, final RESTFilterTagV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filtertag/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterTagV1 createJSONFilterTag(@QueryParam("expand") final String expand, final RESTFilterTagV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filtertags/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterTagCollectionV1 createJSONFilterTags(@QueryParam("expand") final String expand, final RESTFilterTagCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filtertags/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterTagCollectionV1 createJSONFilterTags(@QueryParam("expand") final String expand, final RESTFilterTagCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filtertag/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterTagV1 deleteJSONFilterTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filtertag/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterTagV1 deleteJSONFilterTag(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filtertags/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterTagCollectionV1 deleteJSONFilterTags(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
     
     @DELETE
     @Path("/filtertags/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterTagCollectionV1 deleteJSONFilterTags(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
     
     /*     FILTER FIELD FUNCTIONS */
     /*     JSONP FUNCTIONS */
     @GET
     @Path("/filterfield/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String getJSONPFilterField(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @GET
     @Path("/filterfields/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String getJSONPFilterFields(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filterfield/put/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String updateJSONPFilterField(@QueryParam("expand") final String expand, final RESTFilterFieldV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filterfield/put/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String updateJSONPFilterField(@QueryParam("expand") final String expand, final RESTFilterFieldV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filterfields/put/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String updateJSONPFilterFields(@QueryParam("expand") final String expand, final RESTFilterFieldCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filterfields/put/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String updateJSONPFilterFields(@QueryParam("expand") final String expand, final RESTFilterFieldCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filterfield/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPFilterField(@QueryParam("expand") final String expand, final RESTFilterFieldV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filterfield/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPFilterField(@QueryParam("expand") final String expand, final RESTFilterFieldV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filterfields/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPFilterFields(@QueryParam("expand") final String expand, final RESTFilterFieldCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filterfields/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPFilterFields(@QueryParam("expand") final String expand, final RESTFilterFieldCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filterfield/delete/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPFilterField(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filterfield/delete/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPFilterField(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filterfields/delete/jsonp/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPFilterFields(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filterfields/delete/jsonp/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPFilterFields(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
     
     /*      JSON FUNCTIONS */   
     @GET
     @Path("/filterfield/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterFieldV1 getJSONFilterField(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @GET
     @Path("/filterfields/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterFieldCollectionV1 getJSONFilterFields(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
     @PUT
     @Path("/filterfield/put/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterFieldV1 updateJSONFilterField(@QueryParam("expand") final String expand, final RESTFilterFieldV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filterfield/put/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterFieldV1 updateJSONFilterField(@QueryParam("expand") final String expand, final RESTFilterFieldV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filterfields/put/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterFieldCollectionV1 updateJSONFilterFields(@QueryParam("expand") final String expand, final RESTFilterFieldCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filterfields/put/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterFieldCollectionV1 updateJSONFilterFields(@QueryParam("expand") final String expand, final RESTFilterFieldCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filterfield/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterFieldV1 createJSONFilterField(@QueryParam("expand") final String expand, final RESTFilterFieldV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filterfield/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterFieldV1 createJSONFilterField(@QueryParam("expand") final String expand, final RESTFilterFieldV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filterfields/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterFieldCollectionV1 createJSONFilterFields(@QueryParam("expand") final String expand, final RESTFilterFieldCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filterfields/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterFieldCollectionV1 createJSONFilterFields(@QueryParam("expand") final String expand, final RESTFilterFieldCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filterfield/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterFieldV1 deleteJSONFilterField(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filterfield/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterFieldV1 deleteJSONFilterField(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filterfields/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterFieldCollectionV1 deleteJSONFilterFields(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
     
     @DELETE
     @Path("/filterfields/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterFieldCollectionV1 deleteJSONFilterFields(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
     
     /*     FILTER LOCALE FUNCTIONS */
     /*     JSONP FUNCTIONS */  
     @GET
     @Path("/filterlocale/get/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String getJSONPFilterLocale(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @GET
     @Path("/filterlocales/get/jsonp/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String getJSONPFilterLocales(@QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filterlocale/put/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String updateJSONPFilterLocale(@QueryParam("expand") final String expand, final RESTFilterLocaleV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filterlocale/put/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String updateJSONPFilterLocale(@QueryParam("expand") final String expand, final RESTFilterLocaleV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filterlocales/put/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String updateJSONPFilterLocales(@QueryParam("expand") final String expand, final RESTFilterLocaleCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filterlocales/put/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String updateJSONPFilterLocales(@QueryParam("expand") final String expand, final RESTFilterLocaleCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filterlocale/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPFilterLocale(@QueryParam("expand") final String expand, final RESTFilterLocaleV1 dataObject, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filterlocale/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPFilterLocale(@QueryParam("expand") final String expand, final RESTFilterLocaleV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filterlocales/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPFilterLocales(@QueryParam("expand") final String expand, final RESTFilterLocaleCollectionV1 dataObjects, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filterlocales/post/jsonp")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public String createJSONPFilterLocales(@QueryParam("expand") final String expand, final RESTFilterLocaleCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filterlocale/delete/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPFilterLocale(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filterlocale/delete/jsonp/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPFilterLocale(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filterlocales/delete/jsonp/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPFilterLocales(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filterlocales/delete/jsonp/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public String deleteJSONPFilterLocales(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand, @QueryParam("callback") final String callback) throws InvalidParameterException, InternalProcessingException;
     
     /*      JSON FUNCTIONS */   
     @GET
     @Path("/filterlocale/get/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterLocaleV1 getJSONFilterLocale(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
     
     @GET
     @Path("/filterlocales/get/json/all")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterLocaleCollectionV1 getJSONFilterLocales(@QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
     
     @PUT
     @Path("/filterlocale/put/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterLocaleV1 updateJSONFilterLocale(@QueryParam("expand") final String expand, final RESTFilterLocaleV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filterlocale/put/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterLocaleV1 updateJSONFilterLocale(@QueryParam("expand") final String expand, final RESTFilterLocaleV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filterlocales/put/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterLocaleCollectionV1 updateJSONFilterLocales(@QueryParam("expand") final String expand, final RESTFilterLocaleCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 
     @PUT
     @Path("/filterlocales/put/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterLocaleCollectionV1 updateJSONFilterLocales(@QueryParam("expand") final String expand, final RESTFilterLocaleCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filterlocale/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterLocaleV1 createJSONFilterLocale(@QueryParam("expand") final String expand, final RESTFilterLocaleV1 dataObject) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filterlocale/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterLocaleV1 createJSONFilterLocale(@QueryParam("expand") final String expand, final RESTFilterLocaleV1 dataObject, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filterlocales/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterLocaleCollectionV1 createJSONFilterLocales(@QueryParam("expand") final String expand, final RESTFilterLocaleCollectionV1 dataObjects) throws InvalidParameterException, InternalProcessingException;
 
     @POST
     @Path("/filterlocales/post/json")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ MediaType.APPLICATION_JSON })
     public RESTFilterLocaleCollectionV1 createJSONFilterLocales(@QueryParam("expand") final String expand, final RESTFilterLocaleCollectionV1 dataObjects, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filterlocale/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterLocaleV1 deleteJSONFilterLocale(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filterlocale/delete/json/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterLocaleV1 deleteJSONFilterLocale(@PathParam("id") final Integer id, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
 
     @DELETE
     @Path("/filterlocales/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterLocaleCollectionV1 deleteJSONFilterLocales(@PathParam("ids") final PathSegment ids, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
     
     @DELETE
     @Path("/filterlocales/delete/json/{ids}")
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes({ "*" })
     public RESTFilterLocaleCollectionV1 deleteJSONFilterLocales(@PathParam("ids") final PathSegment ids, @QueryParam("logDetails") final RESTLogDetailsV1 logDetails, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;
     
     
 	/*@GET
 	@Path("/contentspec/get/json/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes({ "*" })
 	public RESTContentSpecV1 getJSONContentSpec(@PathParam("id") final Integer id, @QueryParam("expand") final String expand) throws InvalidParameterException, InternalProcessingException;*/
 }
