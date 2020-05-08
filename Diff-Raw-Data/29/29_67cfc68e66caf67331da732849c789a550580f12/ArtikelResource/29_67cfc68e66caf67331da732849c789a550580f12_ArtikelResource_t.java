 package de.shop.artikelverwaltung.rest;
 
 import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
 
 import java.net.URI;
 
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import de.shop.artikelverwaltung.domain.Artikel;
 import de.shop.util.Mock;
 import de.shop.util.NotFoundException;
 
 
 @Path("/artikel")
 @Produces(APPLICATION_JSON)
 @Consumes
 @RequestScoped
 public class ArtikelResource {
 
 	@Context
 	private UriInfo uriInfo;
 	@Inject
 	private UriHelperArtikel uriHelperArtikel;
 	
 	@GET
 	@Path("{id:[1-9][0-9]*}")
 	
 	public Artikel findArtikelById(@PathParam("id") Long id) {
 		final Artikel artikel = Mock.findArtikelById(id);
 		if (artikel == null) {
 			throw new NotFoundException("Kein Artikel mit der ID " + id + " gefunden");
 		}
 		return artikel;
 		
 	}
 	@POST
 	@Consumes(APPLICATION_JSON)
 	@Produces
 	public Response createArtikel(Artikel artikel) {
 		
 		artikel = Mock.createArtikel(artikel);
 		
 		final URI artikelUri = uriHelperArtikel.getUriArtikel(artikel, uriInfo);
 		return Response.created(artikelUri).build();
 	}
 	
 	
 	
 }
