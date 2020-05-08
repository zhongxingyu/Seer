 package cz.muni.fi.pv243.sportleaguesystem.rest;
 
 import java.util.List;
 
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import cz.muni.fi.pv243.sportleaguesystem.entities.Sport;
 import cz.muni.fi.pv243.sportleaguesystem.service.interfaces.SportService;
 
 @Path("/sports")
 @RequestScoped
 @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
 public class SportRESTService {
 	
 	@Inject
 	private SportService sportService;
 	
 	@GET
 	public List<Sport> listAllSports(@QueryParam("name") @DefaultValue("") String name) {
 		List<Sport> sports = null;
 		if (!"".equals(name)) {
 			sports = sportService.findSportsByName(name);
 		} else {
 			sports = sportService.getAll();
 		}
 		return sports;
 	}
 	
 	@GET
 	@Path("/{id:[0-9][0-9]*}")
 	public Sport lookupSportById(@PathParam("id") Long id) {
 		Sport sport = sportService.getById(id);
 		if (sport == null) {
 			throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
 					.entity("Sport with id " + id + " wasn't found")
 					.build());
 		}
 		return sport;
 	}
 }
