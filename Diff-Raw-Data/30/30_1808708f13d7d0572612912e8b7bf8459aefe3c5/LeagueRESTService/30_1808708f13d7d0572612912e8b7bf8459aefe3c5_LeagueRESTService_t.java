 package cz.muni.fi.pv243.sportleaguesystem.rest;
 
 import java.util.List;
 
import javassist.NotFoundException;

 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import cz.muni.fi.pv243.sportleaguesystem.controller.SecurityHelper;
 import cz.muni.fi.pv243.sportleaguesystem.entities.League;
 import cz.muni.fi.pv243.sportleaguesystem.entities.Match;
 import cz.muni.fi.pv243.sportleaguesystem.entities.Sport;
 import cz.muni.fi.pv243.sportleaguesystem.entities.User;
 import cz.muni.fi.pv243.sportleaguesystem.service.interfaces.LeagueService;
 import cz.muni.fi.pv243.sportleaguesystem.service.interfaces.PrincipalService;
 import cz.muni.fi.pv243.sportleaguesystem.service.interfaces.SportService;
 
 @Path("/leagues")
 @RequestScoped
 @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
 public class LeagueRESTService {
 
 	@Inject
 	private LeagueService leagueService;
 	
 	@Inject
 	private SportService sportService;
 	
 	@Inject
 	private SecurityHelper securityHelper;
 	
 	@Inject
 	private PrincipalService principalService;
 	
 	@GET
 	public List<League> findAllLeagues(@QueryParam("sport") @DefaultValue("") String sportName) {
 		List<League> leagues = null;
 		if (!"".equals(sportName)) {
 			List<Sport> sports = sportService.findSportsByName(sportName);
 			if (sports.size() != 1) {
 				throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
 						.entity("Sport with name " + sportName + " wasn't found or there are multiple sports with such a name")
						.type(MediaType.APPLICATION_JSON)
 						.build());
 			}
 			leagues = leagueService.findBySport(sports.get(0));
 		} else {
 			leagues = leagueService.findAll();
 		}
 		return leagues;
 	}
 	
 	@GET
 	@Path("/{id:[0-9][0-9]*}")
 	public League lookupLeagueById(@PathParam("id") Long id) {
 		League league = leagueService.getById(id);
 		if (league == null) {
 			throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
 					.entity("League with id " + id + " wasn't found")
					.type(MediaType.APPLICATION_JSON)
 					.build());
 		}
 		return league;
 	}
 	
 	@GET
 	@Path("/{id:[0-9][0-9]*}/players")
 	public List<User> findLeaguePlayers(@PathParam("id") Long id) {
 		League league = lookupLeagueById(id);
 		return league.getPlayers();
 	}
 	
 	@GET
 	@Path("/{id:[0-9][0-9]*}/matches")
 	public List<Match> findLeagueMatches(@PathParam("id") Long id) {
 		League league = lookupLeagueById(id);
 		return league.getMatches();
 	}
 	
 	@GET
 	@Path("/{id:[0-9][0-9]*}/matches/generate")
 	public Response generateMatches(@PathParam("id") Long id) {
 		League league = lookupLeagueById(id);
 		leagueService.generateMatches(league);
		return Response.ok("New matches generated")
					   .type(MediaType.APPLICATION_JSON)
					   .build();
 	}
 }
