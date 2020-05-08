 package de.fhkoeln.gm.serientracker.webservice.resources;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import de.fhkoeln.gm.serientracker.jaxb.Serie;
 import de.fhkoeln.gm.serientracker.jaxb.Series;
 import de.fhkoeln.gm.serientracker.utils.Hasher;
 import de.fhkoeln.gm.serientracker.utils.Logger;
 import de.fhkoeln.gm.serientracker.webservice.Config;
 import de.fhkoeln.gm.serientracker.webservice.data.SeriesDataHandler;
 
 
 /**
  * Service for:
  * GET     /series
  * POST    /series
  * GET     /series/{serieID}
  * DELETE  /series/{serieID}
  * PUT     /series/{serieID}
  * GET     /series/{serieID}/seaons
  * GET     /series/{serieID}/seaons/{seaonID}
  * GET     /series/{serieID}/seaons/{seaonID}/episodes
  * GET     /series/{serieID}/seaons/{seaonID}/episodes/{episodeID}
  */
 
 @Path( "/series" )
 public class SeriesService {
 
 	private SeriesDataHandler dh = new SeriesDataHandler();
 
 	@GET
 	@Produces( MediaType.APPLICATION_XML )
 	public Response getSeries() {
 		Logger.log( "GET series called" );
 
 		Series series = dh.getSeries();
 
 		if ( series == null )
 			return Response.status( 404 ).build();
 
 		return Response.ok().entity( series ).build();
 	}
 
 	@POST
 	@Consumes( MediaType.APPLICATION_XML )
 	public Response addSerie( Serie newSerie ) {
 		Logger.log( newSerie.getTitle() );
 
 		String id = "ss_" + Hasher.createHash( newSerie.getTitle() );
 
 		if ( dh.SerieExistsByID( id ) )
 			return Response.status( 409 ).build();
 
 		newSerie.setSerieID( id );
 
 		if ( ! dh.addSerie( newSerie ) )
 			return Response.status( 500 ).build();
 
 		URI location = null;
 		try {
 			location = new URI( Config.getServerURL() + "/series/" + id );
 		} catch (URISyntaxException e) {
 			e.printStackTrace();
 		}
 
 		return Response.created( location ).build();
 	}
 
 	@Path( "{serieID}" )
 	@GET
 	@Produces( MediaType.APPLICATION_XML )
 	public Response getSerie( @PathParam( "serieID" ) String id) {
 		Logger.log( id );
 
		Serie serie = dh.getSerieByID( id );
 
 		if ( serie == null )
 			return Response.status( 404 ).build();
 		else
 			return Response.ok().entity( serie ).build();
 	}
 
 	@Path( "{serieID}" )
 	@PUT
 	@Consumes( MediaType.APPLICATION_XML )
 	public Response updateSerie( @PathParam( "serieID" ) String id, Serie newSerie ) {
 		Logger.log( id );
 
 		if ( ! dh.SerieExistsByID( id ) )
 			return Response.status( 404 ).build();
 
 		if ( ! id.equals( newSerie.getSerieID() ) )
 			return Response.status( 400 ).build();
 
 		if ( ! dh.updateSerie( newSerie ) )
 			return Response.status( 500 ).build();
 
 		return Response.noContent().build();
 	}
 
 	@Path( "{serieID}" )
 	@DELETE
 	public Response deleteSerie( @PathParam( "serieID" ) String id ) {
 		Logger.log( id );
 
 		if ( ! dh.SerieExistsByID( id ) )
 			return Response.status( 404 ).build();
 
 		if ( ! dh.removeSerie( id ) )
 			return Response.status( 500 ).build();
 
 		return Response.noContent().build();
 	}
 
 	@Path ( "{serieID}/seaons")
 	@GET
 	@Produces( MediaType.APPLICATION_XML )
 	public Response getSeasonsOfSerie(
 			@PathParam( "serieID" ) String serieID
 		) {
 		Logger.log( String.format( "Serien ID: %s", serieID ) );
 
 		return Response.noContent().build();
 	}
 
 	@Path ( "{serieID}/seaons/{seasonID}")
 	@GET
 	@Produces( MediaType.APPLICATION_XML )
 	public Response getSeasonOfSerie(
 			@PathParam( "serieID" ) String serieID,
 			@PathParam( "seasonID" ) String seasonID
 		) {
 		Logger.log( String.format( "Serien ID: %s | Season ID: %s\n", serieID, seasonID ) );
 
 		return Response.noContent().build();
 	}
 
 	@Path ( "{serieID}/seaons/{seasonID}/episodes")
 	@GET
 	@Produces( MediaType.APPLICATION_XML )
 	public Response getEpisodesOfSeasonOfSerie(
 			@PathParam( "serieID" ) String serieID,
 			@PathParam( "seasonID" ) String seasonID
 		) {
 		Logger.log( String.format( "Serien ID: %s | Season ID: %s\n", serieID, seasonID ) );
 
 		return Response.noContent().build();
 	}
 
 	@Path ( "{serieID}/seaons/{seasonID}/episodes/{episodeID}")
 	@GET
 	@Produces( MediaType.APPLICATION_XML )
 	public Response getEpisodeOfSeasonOfSerie(
 			@PathParam( "serieID" ) String serieID,
 			@PathParam( "seasonID" ) String seasonID,
 			@PathParam( "episodeID" ) String episodeID
 		) {
 		Logger.log( String.format( "Serien ID: %s | Season ID: %s | Episode ID: %s\n", serieID, seasonID, episodeID ) );
 
 		return Response.noContent().build();
 	}
 
 }
