 package cz.muni.fi.pv243.sportleaguesystem.rest;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import cz.muni.fi.pv243.sportleaguesystem.entities.League;
 import cz.muni.fi.pv243.sportleaguesystem.entities.Match;
 import cz.muni.fi.pv243.sportleaguesystem.service.interfaces.LeagueService;
 import cz.muni.fi.pv243.sportleaguesystem.service.interfaces.MatchService;
 
 @Path("/matches")
 @RequestScoped
 @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
 public class MatchRESTService {
 	
 	@Inject
 	private MatchService matchService;
 	
 	@Inject
 	private LeagueService leagueService;
 	
 	@GET
 	public List<Match> findAllMatches() {
 		List<League> leagues = leagueService.findAll();
 		List<Match> matches = new ArrayList<Match>();
 		for (League league : leagues) {
 			matches.addAll(league.getMatches());
 		}
 		return matches;
 	}
 
 	@GET
 	@Path("/{id:[0-9][0-9]*}")
 	public Match lookupMatchById(@PathParam("id") Long id) {
 		Match match = matchService.getById(id);
 		if (match == null) {
 			throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
 					.entity("Match with id " + id + " wasn't found")
					.type(MediaType.APPLICATION_JSON)
 					.build());
 		}
 		return match;
 	}
 }
