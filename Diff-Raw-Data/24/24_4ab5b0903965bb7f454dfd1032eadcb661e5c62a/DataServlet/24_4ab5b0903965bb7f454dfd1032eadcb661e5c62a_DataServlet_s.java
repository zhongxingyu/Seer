 package com.varnoss.rest;
 
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import com.varnoss.Station;
 import com.varnoss.Stations;
 import com.varnoss.Warning;
 import com.varnoss.Warnings;
 import com.varnoss.database.DBManager;
 
 @Path("/api")
 public class DataServlet {
 	DBManager manager = new DBManager();
 
 	@POST
 	@Path("/warning")
 	@Consumes("application/json")
 	public Response postWarning(Warning war) {
 		manager.insertWarning(war);
 		return Response.ok(200).build();
 	}
 
 	@GET
 	@Path("/warning/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getWarning(@QueryParam("id") String id) throws Exception {
 		Warning war = new Warning();
 		war = manager.getWarning(id);
 		return Response.ok(200).entity(war).build();
 	}
 
 	@GET
 	@Path("/warnings")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getWarnings(
 			@DefaultValue("10") @QueryParam("amount") int amount,
 			@DefaultValue("") @QueryParam("stations") String s,
 			@DefaultValue("") @QueryParam("created") String created)
 			throws Exception {
 		String[] _stations = null;
 		List<Warning> wars = new ArrayList<Warning>();
 		if (!s.equals("")) {
 			_stations = parseStations(s);
 			wars = manager.getWarnings(amount, _stations, created);
 		} else {
 			wars = manager.getWarnings(amount, null, created);
 		}
 		Warnings _warnings = new Warnings(wars);
 		return Response.ok(200).entity(_warnings).build();
 	}
 
 	@GET
 	@Path("/stations")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getStations(@QueryParam("name") String name,
 			@QueryParam("town") String town, @QueryParam("type") String type,
 			@QueryParam("line") String line,
 			@QueryParam("sort") String sort)
 			throws Exception {
 		Stations _stations = new Stations();
 		_stations = manager.getStations(name, town, type, line, sort);
 		return Response.ok(200).entity(_stations).build();
 	}
 
 	private String[] parseStations(String stations) {
 		String[] _stations = stations.split(",");
 		return _stations;
 	}
 
 }
