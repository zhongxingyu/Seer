 package mayo.edu.cts2.editor.server.rest;
 
 import org.jboss.resteasy.client.ClientResponse;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 public interface Cts2Client {
 
 	/************************/
 	/* VALUE SET OPERATIONS */
 	/************************/
 	@GET
 	@Path("/valuesets")
 	@Produces(MediaType.APPLICATION_XML)
 	String getValueSets(@HeaderParam("Authorization") String auth,
 	                    @QueryParam("maxtoreturn") @DefaultValue("5000") int maxRecordsToReturn);
 
 	@GET
 	@Path("/valuesets")
 	@Produces(MediaType.APPLICATION_XML)
 	String getValueSets(@HeaderParam("Authorization") String auth,
 	                       @QueryParam("maxtoreturn") @DefaultValue("5000") int maxRecordsToReturn,
 	                       @QueryParam("matchvalue") String matchValue);
 
 	@GET
 	@Path("/valueset/{oid}")
 	@Produces(MediaType.APPLICATION_XML)
 	String getValueSet(@HeaderParam("Authorization") String auth,
 	                   @PathParam("oid") String oid);
 
 	/***********************************/
 	/* VALUE SET DEFINITION OPERATIONS */
 	/***********************************/
 	@GET
 	@Path("/valueset/{oid}/definitions")
 	@Produces(MediaType.APPLICATION_XML)
 	String getDefinitions(@HeaderParam("Authorization") String auth,
 	                      @PathParam("oid") String oid,
 	                      @QueryParam("maxtoreturn") @DefaultValue("5000") int maxRecordsToReturn);
 
 	@PUT
 	@Path("/valueset/{oid}/definition/{definitionId}")
 	@Produces(MediaType.APPLICATION_XML)
 	String updateValueSetDefinition(@HeaderParam("Authorization") String auth,
 	                                @PathParam("oid") String oid,
 	                                @PathParam("definitionId") String definitionId,
 	                                @QueryParam("changeseturi") String changeSetUri);
 
 	@GET
 	@Path("/valueset/{oid}/definition/{version}")
 	@Produces(MediaType.APPLICATION_XML)
 	String getValueSetDefinition(@HeaderParam("Authorization") String auth,
 	                             @PathParam("oid") String oid,
 	                             @PathParam("version") String version);
 
 	@GET
 	@Path("/valueset/{oid}/definition/{version}/resolution")
 	@Produces(MediaType.APPLICATION_XML)
 	String getResolvedValueSet(@HeaderParam("Authorization") String auth,
 	                           @PathParam("oid") String oid,
 	                           @PathParam("version") String version,
 	                           @QueryParam("maxtoreturn") @DefaultValue("5000") int maxRecordsToReturn);
 
 	@GET
 	@Path("/valueset/{oid}/definition/{version}/resolution")
 	@Produces(MediaType.APPLICATION_XML)
 	String getResolvedValueSet(@HeaderParam("Authorization") String auth,
 	                           @PathParam("oid") String oid,
 	                           @PathParam("version") String version,
 	                           @QueryParam("changesetcontext") String changeSetUri,
 	                           @QueryParam("maxtoreturn") @DefaultValue("5000") int maxRecordsToReturn);
 
 	@POST
 	@Path("/valuesetdefinition")
 	@Produces(MediaType.APPLICATION_XML)
 	String createValueSetDefinition(@HeaderParam("Authorization") String auth,
 	                                @QueryParam("changeseturi") String changeSetUri);
 
 	@GET
 	@Path("/valueset/{oid}/definition/{version}")
 	String getDefinition(@HeaderParam("Authorization") String auth,
 	                     @PathParam("oid") String oid,
 	                     @PathParam("version") String version,
	                     @QueryParam("changeseturi") String changeSetUri);
 
 	@GET
 	@Path("/valueset/{oid}/definition/{version}")
 	String getDefinition(@HeaderParam("Authorization") String auth,
 	                     @PathParam("oid") String oid,
 	                     @PathParam("version") String version);
 
 	@GET
 	@Path("/valueset/{oid}/definitions")
 	String getUserDefinitions(@HeaderParam("Authorization") String auth,
 	                          @PathParam("oid")String oid,
 	                          @QueryParam("filtercomponent") String filter,
 	                          @QueryParam("matchvalue") String creator,
 	                          @QueryParam("maxtoreturn") @DefaultValue("5000") int maxRecordsToReturn);
 
 	@DELETE
 	@Path("/valueset/{oid}/definition/{valuesetdefid}")
 	@Produces(MediaType.APPLICATION_XML)
 	String deleteValueSetDefinition(@HeaderParam("Authorization") String auth,
 	                                @PathParam("oid") String oid,
 	                                @PathParam("valuesetdefid") String valueSetDefId,
 	                                @QueryParam("changeseturi") String changeSetUri);
 
 	@DELETE
 	@Path("/valueset/{oid}/definition/{valuesetdefid}/entry/{definitionentry}")
 	@Produces(MediaType.APPLICATION_XML)
 	String deleteValueSetDefinitionEntry(@HeaderParam("Authorization") String auth,
 	                                     @PathParam("oid") String oid,
 	                                     @PathParam("valuesetdefid") String valueSetDefId,
 	                                     @PathParam("definitionentry") String definitionEntry);
 
 	/*************************/
 	/* CHANGE SET OPERATIONS */
 	/*************************/
 	@POST
 	@Path("/changeset")
 	ClientResponse<String> createChangeSet(@HeaderParam("Authorization") String auth);
 
 	@DELETE
 	@Path("/changeset/{uri}")
 	@Produces(MediaType.APPLICATION_XML)
 	String deleteChangeSet(@HeaderParam("Authorization") String auth,
 	                       @PathParam("uri") String changeSetUri);
 
 	@GET
 	@Path("/changeset/{uri}")
 	@Produces(MediaType.APPLICATION_XML)
 	String getChangeSet(@HeaderParam("Authorization") String auth,
 	                    @PathParam("uri") String changeSetUri);
 
 	@POST
 	@Path("/changeset/{uri}")
 	@Consumes(MediaType.APPLICATION_XML)
 	@Produces(MediaType.APPLICATION_XML)
 	String updateChangeSet(@HeaderParam("Authorization") String auth,
 	                       @PathParam("uri") String changeSetUri,
 	                       String metadataRequest);
 
 	@GET
 	@Path("/changesets")
 	@Produces(MediaType.APPLICATION_XML)
 	String getChangeSetsByCreator(@HeaderParam("Authorization") String auth,
 	                              @QueryParam("matchvalue") String creator,
 	                              @QueryParam("filtercomponent") @DefaultValue("creator") String creatorFilter);
 
 }
