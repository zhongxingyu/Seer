 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package service;
 
 import java.util.Collection;
 import javax.ws.rs.Path;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Produces;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.UriInfo;
 import com.sun.jersey.api.core.ResourceContext;
import javax.persistence.EntityManager;
import persistence.Event;
 import persistence.InterestArea;
import persistence.Organization;
import persistence.Filter;
import persistence.SourceInterestMap;
 import converter.InterestAreasConverter;
 import converter.InterestAreaConverter;
 import converter.InterestAreaListConverter;
 
 /**
  * 
  * @author dave
  */
 
 @Path("/interestAreas/")
 public class InterestAreasResource extends Base {
 	@Context
 	protected UriInfo			uriInfo;
 	@Context
 	protected ResourceContext	resourceContext;
 
 	/** Creates a new instance of InterestAreasResource */
 	public InterestAreasResource() {
 	}
 
 	/**
 	 * Get method for retrieving a collection of InterestArea instance in XML
 	 * format.
 	 * 
 	 * @return an instance of InterestAreasConverter
 	 */
 	@GET
 	@Produces( { "application/xml", "application/json" })
 	public InterestAreasConverter get(@QueryParam("start") @DefaultValue("0") int start,
 			@QueryParam("max") @DefaultValue("10") int max,
 			@QueryParam("expandLevel") @DefaultValue("1") int expandLevel,
 			@QueryParam("query") @DefaultValue("SELECT e FROM InterestArea e") String query) {
 		return new InterestAreasConverter(getEntities(start, max, query),
 				uriInfo.getAbsolutePath(), expandLevel);
 	}
 
 	/**
 	 * Post method for creating an instance of InterestArea using XML as the
 	 * input format.
 	 * 
 	 * @param data
 	 *            an InterestAreaConverter entity that is deserialized from an
 	 *            XML stream
 	 * @return an instance of InterestAreaConverter
 	 */
 	@POST
 	@Consumes( { "application/xml", "application/json" })
 	public Response post(InterestAreaConverter data) {
 		InterestArea entity = data.getEntity();
 		createEntity(entity);
 		return Response.created(uriInfo.getAbsolutePath().resolve(entity.getId() + "/")).build();
 	}
 
 	/**
 	 * Returns a dynamic instance of InterestAreaResource used for entity
 	 * navigation.
 	 * 
 	 * @return an instance of InterestAreaResource
 	 */
 	@Path("{id}/")
 	public service.InterestAreaResource getInterestAreaResource(@PathParam("id") String id) {
 		InterestAreaResource resource = resourceContext.getResource(InterestAreaResource.class);
 		resource.setId(id);
 		return resource;
 	}
 
 	@Path("list/")
 	@GET
 	@Produces( { "application/json" })
 	public InterestAreaListConverter list(@QueryParam("start") @DefaultValue("0") int start,
 			@QueryParam("max") @DefaultValue("10") int max,
 			@QueryParam("query") @DefaultValue("SELECT e FROM InterestArea e") String query) {
 		return new InterestAreaListConverter(getEntities(start, max, query), uriInfo
 				.getAbsolutePath(), uriInfo.getBaseUri());
 	}
 
 	/**
 	 * Returns all the entities associated with this resource.
 	 * 
 	 * @return a collection of InterestArea instances
 	 */
 	@Override
 	protected Collection<InterestArea> getEntities(int start, int max, String query) {
 		return (Collection<InterestArea>) super.getEntities(start, max, query);
 	}
 }
