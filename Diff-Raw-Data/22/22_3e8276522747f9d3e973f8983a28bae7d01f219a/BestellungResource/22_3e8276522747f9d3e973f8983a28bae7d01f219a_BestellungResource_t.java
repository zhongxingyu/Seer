 package de.shop.bestellverwaltung.rest;
 
 import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
 import static javax.ws.rs.core.MediaType.APPLICATION_XML;
 import static javax.ws.rs.core.MediaType.TEXT_XML;
 
 import java.lang.invoke.MethodHandles;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Locale;
 
 import org.jboss.logging.Logger;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import de.shop.artikelverwaltung.domain.Artikel;
 import de.shop.artikelverwaltung.service.ArtikelService;
 import de.shop.bestellverwaltung.domain.Bestellposition;
 import de.shop.bestellverwaltung.domain.Bestellung;
 import de.shop.bestellverwaltung.domain.Lieferung;
 import de.shop.bestellverwaltung.service.BestellungService;
 import de.shop.kundenverwaltung.domain.Kunde;
 import de.shop.kundenverwaltung.rest.UriHelperKunde;
 import de.shop.kundenverwaltung.service.KundeService;
 import de.shop.kundenverwaltung.service.KundeService.FetchType;
 import de.shop.util.Log;
 import de.shop.util.NotFoundException;
 import de.shop.util.Transactional;
 import de.shop.util.LocaleHelper;
 
 
 @Path("/bestellungen")
 @Produces(APPLICATION_JSON)
 @Consumes
 @RequestScoped
 @Transactional
 @Log
 public class BestellungResource {
 	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
 	
 	@Inject
 	private BestellungService bs;
 	
 	@Inject
 	private KundeService ks;
 	
 	@Inject
 	private ArtikelService as;
 	
 	@Inject
 	private UriHelperBestellung uriHelperBestellung;
 	
 	@Inject
 	private UriHelperKunde uriHelperKunde;
 	
 	@Inject
 	private UriHelperLieferung uriHelperLieferung;
 	
 	@Inject
 	private LocaleHelper localeHelper;
 	
 	@PostConstruct
 	private void postConstruct() {
 		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
 	}
 	
 	@PreDestroy
 	private void preDestroy() {
 		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
 	}
 	
 	/**
 	 * Mit der URL /bestellungen/{id} eine Bestellung ermitteln
 	 * @param id ID der Bestellung
 	 * @return Objekt mit Bestelldaten, falls die ID vorhanden ist
 	 */
 	@GET
 	@Path("{id:[1-9][0-9]*}")
 	public Bestellung findeBestellungNachId(@PathParam("id") Long id, @Context UriInfo uriInfo) {
 		final Bestellung bestellung = bs.findeBestellungNachId(id);
 		if (bestellung == null) {
 			final String msg = "Keine Bestellung gefunden mit der ID " + id;
 			throw new NotFoundException(msg);
 		}
 
 		// URLs innerhalb der gefundenen Bestellung anpassen
 		uriHelperBestellung.updateUriBestellung(bestellung, uriInfo);
 		return bestellung;
 	}
 	
 	/**
 	 * Mit der URL /bestellungen/{id}/lieferungen die Lieferung ermitteln
 	 * zu einer bestimmten Bestellung ermitteln
 	 * @param id ID der Bestellung
 	 * @return Objekt mit Lieferdaten, falls die ID vorhanden ist
 	 */
 	@GET
 	@Path("{id:[1-9][0-9]*}/lieferungen")
 	public Collection<Lieferung> findeLieferungenNachBestellungId(@PathParam("id") Long id, @Context UriInfo uriInfo) {
 		
 		
 		final Collection<Lieferung> lieferungen = bs.findeLieferungenNachBestellungId(id);
 		if (lieferungen.isEmpty()) {
 			final String msg = "Keine Bestellung gefunden mit der ID " + id;
 			throw new NotFoundException(msg);
 		}
 		
 		// URLs innerhalb der gefundenen Lieferung anpassen
 		for (Lieferung lieferung : lieferungen) {
 			uriHelperLieferung.updateUriLieferung(lieferung, uriInfo);
 		}
 		
 		return lieferungen;
 	}
 	
 	/**
 	 * Mit der URL /bestellungen/{id}/kunde den Kunden einer Bestellung ermitteln
 	 * @param id ID der Bestellung
 	 * @return Objekt mit Kundendaten, falls die ID vorhanden ist
 	 */
 	@GET
 	@Path("{id:[1-9][0-9]*}/kunde")
 	public Kunde findeKundeNachBestellungId(@PathParam("id") Long id, @Context UriInfo uriInfo) {
 		final Kunde kunde = ks.findeKundeNachBestellung(id);		
 		if (kunde == null) {
 			final String msg = "Keine Bestellung gefunden mit der ID " + id;
 			throw new NotFoundException(msg);
 		}
 
 		// URLs innerhalb der gefundenen Bestellung anpassen
 		uriHelperKunde.updateUriKunde(kunde, uriInfo);
 		return kunde;
 	}
 	
 	
 	@GET
 	public Collection<Bestellung> findeAlleOffenenBestellungen(@QueryParam("offenAbgeschlossen") 
					@DefaultValue("") String offenAbgeschlossen, 
 					@Context UriInfo uriInfo) {			                                              
 		Collection<Bestellung> bestellungen = null;
		if ("".equals(offenAbgeschlossen)) {
 			bestellungen = bs.findeAlleBestellungen();
 			if (bestellungen.isEmpty()) {
 				final String msg = "Keine Bestellungen vorhanden";
 				throw new NotFoundException(msg);
 			}
 		}
		else if	("true".equals(offenAbgeschlossen)) {
 				bestellungen = bs.findeBestellungenOffen();
 					
 				if (bestellungen.isEmpty()) {
 					final String msg = "Keine offenen Bestellungen gefunden ";
 					throw new NotFoundException(msg);
 				}
		}
 		
		else if("false".equals(offenAbgeschlossen)) {
 				bestellungen = bs.findeBestellungenGeschlossen();
 			
 				if (bestellungen.isEmpty()) {
 					final String msg = "Keine geschlossenen Bestellungen gefunden ";
 					throw new NotFoundException(msg);
 				}
		}
		else {
			bestellungen = bs.findeAlleBestellungen();
			if (bestellungen.isEmpty()) {
				final String msg = "Keine Bestellungen vorhanden";
				throw new NotFoundException(msg);
 			}
 		}
 		// URLs innerhalb der gefundenen Kunden anpassen
 		for (Bestellung bestellung : bestellungen) {
 			uriHelperBestellung.updateUriBestellung(bestellung, uriInfo);
 		}
 		return bestellungen;
 	}
 	
 	/**
 	 * Mit der URL /bestellungen/lieferungen/{id} eine Lieferung ermitteln
 	 * @param id ID der Lieferung
 	 * @return Objekt mit Lieferdaten, falls die ID vorhanden ist
 	 */
 	@GET
 	@Path("lieferungen/{id:[1-9][0-9]*}")
 	public Lieferung findeLieferungNachId(@PathParam("id") Long id, @Context UriInfo uriInfo) {
 		final Lieferung lieferung = bs.findeLieferungNachId(id);
 		if (lieferung == null) {
 			final String msg = "Keine Lieferung gefunden mit der ID " + id;
 			throw new NotFoundException(msg);
 		}
 		//Uri fr Lieferung anpassen
 		uriHelperLieferung.updateUriLieferung(lieferung,  uriInfo);
 		
 		return lieferung;
 	}
 	
 	/**
 	 * Mit der URL /bestellungen/{id}/lieferungen die Lieferung ermitteln
 	 * zu einer bestimmten Bestellung ermitteln
 	 * @param id ID der Bestellung
 	 * @return Objekt mit Lieferdaten, falls die ID vorhanden ist
 	 */
 	@GET
 	@Path("lieferungen/{id:[1-9][0-9]*}/bestellungen")
 	public Collection<Bestellung> findeBestellungenNachLieferungId(@PathParam("id") Long id, @Context UriInfo uriInfo) {
 		
 		
 		final Collection<Bestellung> bestellungen = bs.findeBestellungenNachLieferungId(id);
 		if (bestellungen.isEmpty()) {
 			final String msg = "Keine Lieferung gefunden mit der ID " + id;
 			throw new NotFoundException(msg);
 		}
 		
 		// URLs innerhalb der gefundenen Bestellung anpassen
 		for (Bestellung bestellung : bestellungen) {
 			uriHelperBestellung.updateUriBestellung(bestellung, uriInfo);
 		}
 		
 		return bestellungen;
 	}
 	
 	/**
 	 * Mit der URL /bestellungen eine neue Bestellung anlegen
 	 * @param bestellung die neue Bestellung
 	 * @return Objekt mit Bestelldaten, falls die ID vorhanden ist
 	 */
 	@POST
 	@Consumes(APPLICATION_JSON)
 	@Produces
 	public Response createBestellung(Bestellung bestellung, @Context UriInfo uriInfo, @Context HttpHeaders headers) {
 		// Schluessel des Kunden extrahieren
 		final String kundeUriStr = bestellung.getKundeUri().toString();
 		int startPos = kundeUriStr.lastIndexOf('/') + 1;
 		final String kundeIdStr = kundeUriStr.substring(startPos);
 		Long kundeId = null;
 		try {
 			kundeId = Long.valueOf(kundeIdStr);
 		}
 		catch (NumberFormatException e) {
 			throw new NotFoundException("Kein Kunde vorhanden mit der ID " + kundeIdStr, e);
 		}
 		
 		// Kunde mit den vorhandenen ("alten") Bestellungen ermitteln
 		final Kunde kunde = ks.findeKundeNachId(kundeId, FetchType.MIT_BESTELLUNGEN, Locale.getDefault());
 				
 		// Implizites Nachladen innerhalb der Transaktion wuerde auch funktionieren
 		// final AbstractKunde kunde = kv.findKundeById(kundeId);
 		if (kunde == null) {
 			throw new NotFoundException("Kein Kunde vorhanden mit der ID " + kundeId);
 		}
 		
 		// persistente Artikel ermitteln
 		Collection<Bestellposition> bestellpositionen = bestellung.getBestellpositionen();
 		List<Long> artikelIds = new ArrayList<>(bestellpositionen.size());
 		for (Bestellposition bp : bestellpositionen) {
 			final String artikelUriStr = bp.getArtikelUri().toString();
 			startPos = artikelUriStr.lastIndexOf('/') + 1;
 			final String artikelIdStr = artikelUriStr.substring(startPos);
 			Long artikelId = null;
 			try {
 				artikelId = Long.valueOf(artikelIdStr);
 			}
 			catch (NumberFormatException e) {
 				// Ungueltige Artikel-ID: wird nicht beruecksichtigt
 				continue;
 			}
 			artikelIds.add(artikelId);
 		}
 		
 		if (artikelIds.isEmpty()) {
 			// keine einzige gueltige Artikel-ID
 			final StringBuilder sb = new StringBuilder("Keine Artikel vorhanden mit den IDs: ");
 			for (Bestellposition bp : bestellpositionen) {
 				final String artikelUriStr = bp.getArtikelUri().toString();
 				startPos = artikelUriStr.lastIndexOf('/') + 1;
 				sb.append(artikelUriStr.substring(startPos));
 				sb.append(" ");
 			}
 			throw new NotFoundException(sb.toString());
 		}
 
 		Collection<Artikel> gefundeneArtikel = as.findeArtikelNachIds(artikelIds);
 		if (gefundeneArtikel.isEmpty()) {
 			throw new NotFoundException("Keine Artikel vorhanden mit den IDs: " + artikelIds);
 		}
 		
 		// Bestellpositionen haben URLs fuer persistente Artikel.
 		// Diese persistenten Artikel wurden in einem DB-Zugriff ermittelt (s.o.)
 		// Fuer jede Bestellposition wird der Artikel passend zur Artikel-URL bzw. Artikel-ID gesetzt.
 		// Bestellpositionen mit nicht-gefundene Artikel werden eliminiert.
 		int i = 0;
 		final List<Bestellposition> neueBestellpositionen = new ArrayList<>(bestellpositionen.size());
 		for (Bestellposition bp : bestellpositionen) {
 			// Artikel-ID der aktuellen Bestellposition (s.o.):
 			// artikelIds haben gleiche Reihenfolge wie bestellpositionen
 			final long artikelId = artikelIds.get(i++);
 			
 			// Wurde der Artikel beim DB-Zugriff gefunden?
 			for (Artikel artikel : gefundeneArtikel) {
 				if (artikel.getId().longValue() == artikelId) {
 					// Der Artikel wurde gefunden
 					bp.setArtikel(artikel);
 					neueBestellpositionen.add(bp);
 					break;					
 				}
 			}
 		}
 		bestellung.setBestellpositionen(neueBestellpositionen);
 		
 		// Die neue Bestellung mit den aktualisierten persistenten Artikeln abspeichern.
 		// Die Bestellung darf dem Kunden noch nicht hinzugefuegt werden, weil dieser
 		// sonst in einer Transaktion modifiziert werden wuerde.
 		// Beim naechsten DB-Zugriff (auch lesend!) wuerde der EntityManager sonst
 		// erstmal versuchen den Kunden-Datensatz in der DB zu modifizieren.
 		// Dann wuerde aber der Kunde mit einer *transienten* Bestellung modifiziert werden,
 		// was zwangslaeufig zu einer Inkonsistenz fuehrt!
 		// Das ist die Konsequenz einer Transaktion (im Gegensatz zu den Action-Methoden von JSF!).
 		final Locale locale = localeHelper.getLocale(headers);
 	
 		// Schluessel der Lieferung extrahieren
 		final String lieferungUriStr = bestellung.getLieferungenUri().toString();
 		int startPos2 = lieferungUriStr.lastIndexOf('/') + 1;
 		final String lieferungIdStr = lieferungUriStr.substring(startPos2);
 		Long lieferungId = null;
 		try {
 			lieferungId = Long.valueOf(lieferungIdStr);
 		}
 		catch (NumberFormatException e) {
 			throw new NotFoundException("Keine Lieferung vorhanden mit der ID " + lieferungIdStr, e);
 		}
 				
 		// Lieferung mit den vorhandenen ("alten") Bestellungen ermitteln
 		final Lieferung lieferung = bs.findeLieferungNachId(lieferungId);
 					
 		// Implizites Nachladen innerhalb der Transaktion wuerde auch funktionieren
 		if (lieferung == null) {
 			throw new NotFoundException("Keine Lieferung vorhanden mit der ID " + lieferungId);
 		}
 		
 		bestellung = bs.createBestellung(bestellung, kunde, lieferung, locale);
 
 		final URI bestellungUri = uriHelperBestellung.getUriBestellung(bestellung, uriInfo);
 		final Response response = Response.created(bestellungUri).build();
 		LOGGER.debugf(bestellungUri.toString());
 		
 		return response;
 	}
 	
 	/**
 	 * Mit der URL /bestellungen{id} eine Bestellung per DELETE l&ouml;schen
 	 * @param bestellungId der zu l&ouml;schenden Bestellung
 	 */
 	
 	@Path("{id:[0-9]+}")
 	@DELETE
 	@Produces
 	public void deleteBestellung(@PathParam("id") Long bestellungId) {
 		final Bestellung bestellung = bs.findeBestellungNachId(bestellungId);
 		bs.deleteBestellung(bestellung);
 	}
 	
 	@PUT
 	@Consumes(APPLICATION_JSON)
 	@Produces
 	public void updateBestellung(Bestellung bestellung, @Context UriInfo uriInfo, @Context HttpHeaders headers) {
 		// Vorhandene Bestellung ermitteln
 		final Locale locale = localeHelper.getLocale(headers);
 		Bestellung origBestellung = bs.findeBestellungNachId(bestellung.getId());
 		if (origBestellung == null) {
 			final String msg = "Keine Bestellung gefunden mit der ID " + bestellung.getId();
 			throw new NotFoundException(msg);
 		}
 		LOGGER.debugf("Bestellung vorher: %s", origBestellung);
 	
 		// Daten des vorhandener Bestellung ueberschreiben
 		origBestellung.setWerte(bestellung);
 		LOGGER.debugf("Bestellung nachher: %s", origBestellung);
 		
 		// Update durchfuehren
 		bestellung = bs.updateBestellung(origBestellung, locale);
 		if (bestellung == null) {
 			final String msg = "Keine Bestellung gefunden mit der ID " + origBestellung.getId();
 			throw new NotFoundException(msg);
 		}
 	}
 }
