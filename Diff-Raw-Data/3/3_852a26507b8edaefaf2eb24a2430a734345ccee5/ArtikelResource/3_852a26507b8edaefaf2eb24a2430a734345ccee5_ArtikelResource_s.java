 package de.webshop.artikelverwaltung.rest;
 
 import java.net.URI;
 
 import javax.inject.Inject;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.NotFoundException;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Link;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import de.webshop.artikelverwaltung.domain.Artikel;
 import de.webshop.artikelverwaltung.service.ArtikelServiceMock;
 import de.webshop.util.rest.UriHelper;
import de.webshop.util.Constants;

 
 /**
  * @author Mario Reinholdt
  */
 @Path("/artikel")
 @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML + ";qs=0.75", MediaType.TEXT_XML + ";qs=0.5" })
 @Consumes
 public class ArtikelResource {
 	// This method is called if XMLis request
 
 
 	@Inject
 	private ArtikelServiceMock as;
 	
 	@Inject
 	private UriHelper uriHelper;
 	
 	@GET
 	@Path("{id:[1-9][0-9]{0,7}}")
 	public Response findArtikelById(@PathParam("id") Long id, @Context UriInfo uriInfo) {
 		final Artikel artikel = as.findArtikelByID(id);
 		if (artikel == null) {
 			// TODO Sprachabhngige Fehlermeldung
 			throw new NotFoundException("Der angegebene Artikel konnte leider nicht gefunden werden. Bitte berprfen Sie die ArtikelID.");
 		}
 
 		return Response.ok(artikel)
                        .links(getTransitionalLinks(artikel, uriInfo))
                        .build();
 	}
 	
 	private Link[] getTransitionalLinks(Artikel artikel, UriInfo uriInfo) {
 		final Link self = Link.fromUri(getUriArtikel(artikel, uriInfo))
                               .rel(SELF_LINK)
                               .build();
 
 		return new Link[] { self };
 	}
 	
 	public URI getUriArtikel(Artikel artikel, UriInfo uriInfo) {
 		return uriHelper.getUri(ArtikelResource.class, "findArtikelById", artikel.getID(), uriInfo);
 	}
 }
