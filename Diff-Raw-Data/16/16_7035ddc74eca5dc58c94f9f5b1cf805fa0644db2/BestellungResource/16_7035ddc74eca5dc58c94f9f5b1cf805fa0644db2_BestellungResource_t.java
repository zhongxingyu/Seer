 package de.webshop.bestellverwaltung.rest;
 
 import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
 import static javax.ws.rs.core.MediaType.APPLICATION_XML;
 import static javax.ws.rs.core.MediaType.TEXT_XML;
 import static de.webshop.util.Constants.FIRST_LINK;
 import static de.webshop.util.Constants.LAST_LINK;
 import static de.webshop.util.Constants.SELF_LINK;
 import java.io.Serializable;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Locale;
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.validation.Valid;
 import javax.validation.constraints.NotNull;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.NotFoundException;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.GenericEntity;
 import javax.ws.rs.core.Link;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import de.webshop.artikelverwaltung.domain.Artikel;
 import de.webshop.artikelverwaltung.rest.ArtikelResource;
 import de.webshop.artikelverwaltung.service.ArtikelService;
 import de.webshop.bestellverwaltung.domain.Bestellung;
 import de.webshop.bestellverwaltung.domain.Position;
 import de.webshop.bestellverwaltung.service.BestellungService;
 import de.webshop.bestellverwaltung.service.PositionService;
 import de.webshop.kundenverwaltung.domain.Kunde;
 import de.webshop.kundenverwaltung.rest.KundeResource;
 import de.webshop.util.interceptor.Log;
 import de.webshop.util.rest.UriHelper;
 
 @Path("/bestellung")
 @Produces({ APPLICATION_JSON, APPLICATION_XML + ";qs=0.75", TEXT_XML + ";qs=0.5" })
 @Consumes
 @RequestScoped
 @Log
 public class BestellungResource implements Serializable {
 	
 	private static final long	serialVersionUID	= 7649051701994670170L;
 	
 	@Context
 	private UriInfo				uriInfo;
 	
 	@Inject
 	private ArtikelService as;
 	
 	@Inject
 	private BestellungService bs;
 	
 	@Inject
 	private PositionService ps;
 	
 	@Inject
 	private ArtikelResource artikelResource;
 	
 	@Inject
 	private KundeResource kundeResource;
 	
 	@Inject
 	private PositionResource positionResource;
 	
 	@Inject
 	private UriHelper uriHelper;
 	
 	@GET
 	@Path("{id:[1-9][0-9]*}")
 	public Response findBestellungById(@PathParam("id") long id) {
 		
 		final Bestellung bestellung = bs.findBestellungById(id);
 		
 		if (bestellung == null) {
			// TODO throw new NotFoundException("bestellung.notFound.id",id));
 		}
 		setStructuralLinks(bestellung, uriInfo);
 		
 		// Link-Header setzen
 		final Response response = Response.ok(bestellung)
 											.links(getTransitionalLinks(bestellung, uriInfo))
 											.build();
 		
 		return response;
 	}
 	
 	@GET
 	@Path("{id:[1-9][0-9]*}/positionen")
 	public Response findPositionenByBestellungId(@PathParam("id") long id) {
 		
 		final Bestellung bestellung = bs.findBestellungById(id);
 		
 		// TODO: bestellung.getID() etwas anderes als id?
 		final List<Position> positionen = ps.findPositionenByBestellungId(id);
 		
 		if (positionen.isEmpty()) {
 			throw new NotFoundException("Zur ID " + id + " wurden keine Position gefunden");
 		}
 		
 		// URIs innerhalb der gefundenen Positionen anpassen
 		for (Position position : positionen) {
 			positionResource.setStructuralLinks(position, uriInfo);
 		}
 		
 		return Response.ok(new GenericEntity<List<Position>>(positionen) {  })
 						.links(getTransitionalLinksPositionen(positionen, bestellung, uriInfo))
 						.build();
 	}
 	
 	public void setStructuralLinks(Bestellung bestellung, UriInfo uriInfo) {
 		// URI fuer Kunde setzen
 		final Kunde kunde = bestellung.getKunde();
 		if (kunde != null) {
 			final URI kundeUri = kundeResource.getUriKunde(bestellung.getKunde(), uriInfo);
 			bestellung.setKundeUri(kundeUri);
 		}
 		
 		// URI fuer positionen setzen
 		final List<Position> positionen = bestellung.getPositionen();
 		
 		if (positionen != null && !positionen.isEmpty()) {
 			for (Position p : positionen) {
 				final URI artikelUri = artikelResource.getUriArtikel(p.getArtikel(), uriInfo);
 				p.setArtikelUri(artikelUri);
 			}
 			
 		}
 	}
 	
 	private Link[] getTransitionalLinks(Bestellung bestellung, UriInfo uriInfo) {
 		final Link self = Link.fromUri(getUriBestellung(bestellung, uriInfo)).rel(SELF_LINK)
 								.build();
 		return new Link[] {self};
 	}
 	
 	private Link[] getTransitionalLinksPositionen(List<Position> positionen, Bestellung bestellung,
 			UriInfo uriInfo) {
 		if (positionen == null || positionen.isEmpty()) {
 			return new Link[0];
 		}
 		
 		final Link self = Link.fromUri(getUriPositionen(bestellung, uriInfo)).rel(SELF_LINK)
 								.build();
 		
 		final Link first = Link.fromUri(positionResource.getUriPosition(positionen.get(0), uriInfo))
 								.rel(FIRST_LINK).build();
 		final int lastPos = positionen.size() - 1;
 		
 		final Link last = Link.fromUri(positionResource.getUriPosition(positionen.get(lastPos),
 																		uriInfo)).rel(LAST_LINK)
 								.build();
 		
 		return new Link[] {self, first, last};
 	}
 	
 	private URI getUriPositionen(Bestellung bestellung, UriInfo uriInfo) {
 		return uriHelper.getUri(BestellungResource.class, "findPositionenByBestellungId",
 								bestellung.getID(), uriInfo);
 	}
 	
 	public URI getUriBestellung(Bestellung bestellung, UriInfo uriInfo) {
 		return uriHelper.getUri(BestellungResource.class, "findBestellungById", bestellung.getID(),
 								uriInfo);
 	}
 	
 	@POST
 	@Consumes({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
 	@Produces
 	public Response createBestellung(@Valid Bestellung bestellung) {
 		// Kunden-ID ermitteln
 		// TODO: eingeloggter Kunde
 		
 		final String kundeUriStr = bestellung.getKundeUri().toString();
 		int startPos = kundeUriStr.lastIndexOf('/') + 1;
 		final String kundeIdStr = kundeUriStr.substring(startPos);
 		Long kundeId = null;
 		try {
 			kundeId = Long.valueOf(kundeIdStr);
 		}
 		catch (NumberFormatException e) {
 			kundeIdInvalid();
 		}
 		
 		// IDs der Artikel ermitteln
 		final Collection<Position> positionen = bestellung.getPositionen();
 		final List<Long> artikelIds = new ArrayList<>(positionen.size());
 		for (Position p : positionen) {
 			final URI artikelUri = p.getArtikelUri();
 			if (artikelUri == null) {
 				continue;
 			}
 			final String artikelUriStr = artikelUri.toString();
 			startPos = artikelUriStr.lastIndexOf('/') + 1;
 			final String artikelIdStr = artikelUriStr.substring(startPos);
 			Long artikelId = null;
 			try {
 				artikelId = Long.valueOf(artikelIdStr);
 			}
 			catch (NumberFormatException e) {
 				continue;
 			}
 			artikelIds.add(artikelId);
 		}
 		
 		if (artikelIds.isEmpty()) {
 			// keine einzige Artikel-ID als gueltige Zahl
 			artikelIdsInvalid();
 		}
 
 		final Collection<Artikel> gefundeneArtikel = new ArrayList<Artikel>();
 		for(Long artikelId : artikelIds)
 			gefundeneArtikel.add(as.findArtikelById(artikelId));
 		
 		int i = 0;
 		final List<Position> neuePositionen = new ArrayList<Position>();
 		for (Position p : positionen) {
 			// Artikel-ID der aktuellen Bestellposition (s.o.):
 			// artikelIds haben gleiche Reihenfolge wie bestellpositionen
 			final long artikelId = artikelIds.get(i++);
 			
 			// Wurde der Artikel beim DB-Zugriff gefunden?
 			for (Artikel artikel : gefundeneArtikel) {
 				if (artikel.getID().longValue() == artikelId) {
 					// Der Artikel wurde gefunden
 					p.setArtikel(artikel);
 					neuePositionen.add(p);
 					break;
 				}
 			}
 		}
 		
 		bestellung.setPositionen(neuePositionen);
 		
 		if (kundeId == null) {
 			// TODO: Kann kundeId == null sein?
 			kundeId = -1L;
 		}
 		
 		bestellung = bs.createBestellung(bestellung, kundeId, Locale.GERMANY);
 		
 		final URI bestellungUri = getUriBestellung(bestellung, uriInfo);
 		return Response.created(bestellungUri).build();
 	}
 	
 	@NotNull(message = "{bestellung.artikelIds.invalid}")
 	public List<Long> artikelIdsInvalid() {
 		return null;
 	}
 	
 	@NotNull(message = "{bestellung.kunde.id.invalid}")
 	public Long kundeIdInvalid() {
 		return null;
 	}
 }
