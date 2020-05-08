 package dsa.colourmylife.rest;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.SecurityContext;
 import javax.ws.rs.core.UriInfo;
 
 import dsa.colourmylife.rest.model.Event;
 import dsa.colourmylife.rest.util.APIErrorBuilder;
 import dsa.colourmylife.rest.util.DataSourceSAP;
 
 @Path("/artists/{artist}/events/{eventid}")
 public class ArtistEventResource {
 	// Recurso Event: ./artists/{artist}/events/{eventid}
 	// GET → Obtener evento. *
 	// PUT → Actualizar evento. *
 	// DELETE → Eliminar evento.
 	// id int(11) NOT NULL AUTO_INCREMENT,
 	// idkind int(11) NOT NULL,
 	// artist varchar(50) NOT NULL,
 	// date datetime default NULL,
 	// place varchar(128) NULL,
 	// city varchar(50) NOT NULL,
 	// country varchar(50) NOT NULL,
 	// info varchar(150),
 	// insertdate datetime default current_timestamp,
 
 	@Context
 	private UriInfo uri;
 
 	@Context
 	protected HttpServletRequest request;
 
 	@Context
 	private SecurityContext security;
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	// Entiendo que para hacer un GET basta con especificar el eventid
 	public Event getEventJSON(@PathParam("eventid") int eventid,
 			@PathParam("artist") String name) {
 		return getEvent(eventid, name);
 	}
 
 	@PUT
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response updateEventJson(@PathParam("eventid") int eventid,
 			Event event, @PathParam("artist") String name) {
 		updateEvent(eventid, event, name);
 		// Le paso un evento y un artista (string) que al introducirlo en un
 		// atributo del evento luego puedo obtenerlo invocándolo a partir del
 		// event.getArtist
 		Response response = null;
 		try {
 			response = Response
 					.status(204)
 					.location(
 							new URI("/artists/" + event.getArtist()
 									+ "/events/" + event.getEventId())).build();
 		} catch (URISyntaxException e) {
 			e.printStackTrace();
 		}
 		return response;
 	}
 
 	@DELETE
 	@Produces(MediaType.APPLICATION_JSON)
 	// Entiendo que para eliminar un evento basta con el eventid
 	public Response deleteEventJSON(@PathParam("eventid") int eventid,
 			@PathParam("artist") String name) {
 		deleteEvent(eventid, name);
 		return Response.status(204).build();
 	}
 
 	public Event getEvent(int eventid, String name) {
 		Connection connection = null;
 		try {
 			connection = DataSourceSAP.getInstance().getDataSource()
 					.getConnection();
 		} catch (SQLException e) {
 			throw new WebApplicationException(
 					Response.status(Response.Status.SERVICE_UNAVAILABLE)
 							.entity(APIErrorBuilder.buildError(
 									Response.Status.SERVICE_UNAVAILABLE
 											.getStatusCode(),
 									"Service unavailable.", request)).build());
 		}
 
 		try {
 			Statement stmt = connection.createStatement();
 			// select * from event where id = 1 and artist = "Florence";
 			ResultSet rs = stmt.executeQuery("SELECT * FROM event WHERE id="
 					+ eventid + " and artist='" + name + "';");
 			if (!rs.next()) {
 				throw new WebApplicationException(Response
 						.status(Response.Status.NOT_FOUND)
 						.entity(APIErrorBuilder.buildError(
 								Response.Status.NOT_FOUND.getStatusCode(),
 								"NOT FOUND", request)).build());
 			}
 
 			Event event = new Event();
 			event.setEventId(rs.getInt("id"));
 			event.setKindId(rs.getInt("idkind"));
 			String kind2 = obtainKind(rs.getInt("idkind"));
 			event.setKind(kind2);
 			event.setArtist(rs.getString("artist"));
 			event.setDate(rs.getString("date"));
 			event.setPlace(rs.getString("place"));
 			event.setCity(rs.getString("city"));
 			event.setCountry(rs.getString("country"));
 			event.setInfo(rs.getString("info"));
 			event.setInsertdate(rs.getString("insertdate"));
 			event.setLink(uri.getAbsolutePath().toString());
			System.out.println("Link: "+event.getLink());
 			// @Path("/artists/{artist}/events/{eventid}")
 			event.setSameKindLink(uri.getBaseUri().toString() + "/artists/"
 					+ name + "/events?idkind=" + event.getKindId());
 			event.setSameCountryLink(uri.getBaseUri().toString() + "/artists/"
 					+ name + "/events?country=" + event.getCountry());
 			// TODO OPTIONAL: Convert kindid into a kind, Maybe another stmt
 			// event.setKind("kind");
 			stmt.close();
 			connection.close();
 
 			return event;
 		} catch (SQLException e) {
 			throw new WebApplicationException(Response
 					.status(Response.Status.INTERNAL_SERVER_ERROR)
 					.entity(APIErrorBuilder.buildError(
 							Response.Status.INTERNAL_SERVER_ERROR
 									.getStatusCode(),
 							"Error accessing to database.", request)).build());
 		}
 	}
 
 	public void updateEvent(int eventid, Event event, String name) {
 		if (!security.isUserInRole("admin")) {
 			throw new WebApplicationException(Response
 					.status(Response.Status.FORBIDDEN)
 					.entity(APIErrorBuilder.buildError(
 							Response.Status.FORBIDDEN.getStatusCode(),
 							"FORBIDDEN", request)).build());
 		}
 		Connection connection = null;
 		try {
 			connection = DataSourceSAP.getInstance().getDataSource()
 					.getConnection();
 		} catch (SQLException e) {
 			throw new WebApplicationException(
 					Response.status(Response.Status.SERVICE_UNAVAILABLE)
 							.entity(APIErrorBuilder.buildError(
 									Response.Status.SERVICE_UNAVAILABLE
 											.getStatusCode(),
 									"Service unavailable.", request)).build());
 		}
 		try {
 			Statement stmt = connection.createStatement();
 			if (event.getCountry() == null) {
 				throw new WebApplicationException(Response
 						.status(Response.Status.BAD_REQUEST)
 						.entity(APIErrorBuilder.buildError(
 								Response.Status.BAD_REQUEST.getStatusCode(),
 								"Country camp mustn't be empty", request))
 						.build());
 			}
 			// UPDATE event SET date='2014-09-20 22:00:00', place='Palau
 			// Joventut', city='Badalona', country='Catalunya', info='new
			// Location' WHERE artist='Florence';
 			// TODO verificar campos NOT NULLs!!!!!
			StringBuilder sb = new StringBuilder("UPDATE event SET date='"
					+ event.getDate() + "', place='" + event.getPlace()
					+ "', city='" + event.getCity() + "', country='"
					+ event.getCountry() + "', info='" + event.getInfo()
					+ "' WHERE artist='" + name + "';");
 			System.out.println(sb);
 
 			int rs = stmt.executeUpdate(sb.toString());
 			if (rs == 0)
 				throw new WebApplicationException(Response
 						.status(Response.Status.NOT_FOUND)
 						.entity(APIErrorBuilder.buildError(
 								Response.Status.NOT_FOUND.getStatusCode(),
 								"Event NOT FOUND", request)).build());
 			stmt.close();
 			connection.close();
 		} catch (SQLException e) {
 			throw new WebApplicationException(Response
 					.status(Response.Status.INTERNAL_SERVER_ERROR)
 					.entity(APIErrorBuilder.buildError(
 							Response.Status.INTERNAL_SERVER_ERROR
 									.getStatusCode(),
 							"Error accessing to database.", request)).build());
 		}
 	}
 
 	public void deleteEvent(int eventid, String name) {
 		if (!security.isUserInRole("admin")) {
 			throw new WebApplicationException(Response
 					.status(Response.Status.FORBIDDEN)
 					.entity(APIErrorBuilder.buildError(
 							Response.Status.FORBIDDEN.getStatusCode(),
 							"FORBIDDEN", request)).build());
 		}
 		Connection connection = null;
 		try {
 			connection = DataSourceSAP.getInstance().getDataSource()
 					.getConnection();
 		} catch (SQLException e) {
 			throw new WebApplicationException(
 					Response.status(Response.Status.SERVICE_UNAVAILABLE)
 							.entity(APIErrorBuilder.buildError(
 									Response.Status.SERVICE_UNAVAILABLE
 											.getStatusCode(),
 									"Service unavailable.", request)).build());
 		}
 		try {
 			Statement stmt = connection.createStatement();
 			// DELETE FROM event WHERE id=2 and artist='Florence';
 			int rs = stmt.executeUpdate("DELETE FROM event WHERE id=" + eventid
 					+ " and artist='" + name + "';");
 			if (rs == 0)
 				throw new WebApplicationException(Response
 						.status(Response.Status.NOT_FOUND)
 						.entity(APIErrorBuilder.buildError(
 								Response.Status.NOT_FOUND.getStatusCode(),
 								"Event not found.", request)).build());
 			stmt.close();
 			connection.close();
 		} catch (SQLException e) {
 			throw new WebApplicationException(Response
 					.status(Response.Status.INTERNAL_SERVER_ERROR)
 					.entity(APIErrorBuilder.buildError(
 							Response.Status.INTERNAL_SERVER_ERROR
 									.getStatusCode(),
 							"Error accessing to database.", request)).build());
 		}
 	}
 	public String obtainKind(int kindid) {
 		Connection connection = null;
 		try {
 			connection = DataSourceSAP.getInstance().getDataSource()
 					.getConnection();
 		} catch (SQLException e) {
 			throw new WebApplicationException(
 					Response.status(Response.Status.SERVICE_UNAVAILABLE)
 							.entity(APIErrorBuilder.buildError(
 									Response.Status.SERVICE_UNAVAILABLE
 											.getStatusCode(),
 									"Service unavailable.", request)).build());
 		}
 
 		try {
 			Statement stmt = connection.createStatement();
 			// SELECT name FROM kind WHERE id=1;
 			StringBuilder sb = new StringBuilder(
 					"SELECT name FROM kind WHERE id=" + kindid + ";");
 			System.out.println(sb);
 			ResultSet rs = stmt.executeQuery(sb.toString());
 			if (!rs.next()) {
 				throw new WebApplicationException(Response
 						.status(Response.Status.NOT_FOUND)
 						.entity(APIErrorBuilder.buildError(
 								Response.Status.NOT_FOUND.getStatusCode(),
 								"Kind not found.", request)).build());
 			}
 			String kind = rs.getString("name");
 			System.out.println("kind: " + kind);
 			stmt.close();
 			connection.close();
 			return kind;
 		} catch (SQLException e) {
 			throw new WebApplicationException(Response
 					.status(Response.Status.INTERNAL_SERVER_ERROR)
 					.entity(APIErrorBuilder.buildError(
 							Response.Status.INTERNAL_SERVER_ERROR
 									.getStatusCode(),
 							"Error accessing to database.", request)).build());
 		}
 	}
 }
