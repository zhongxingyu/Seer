 package de.shop.Auftragsverwaltung.rest;
 
 
 import java.net.URI;
 import java.util.List;
 
 import javax.enterprise.context.ApplicationScoped;
 import javax.inject.Inject;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import de.shop.Artikelverwaltung.rest.UriHelperArtikel;
 import de.shop.Auftragsverwaltung.domain.Auftrag;
 import de.shop.Auftragsverwaltung.domain.Auftragsposition;
 import de.shop.Auftragsverwaltung.domain.Rechnung;
 import de.shop.Kundenverwaltung.domain.Kunde;
 import de.shop.Kundenverwaltung.rest.UriHelperKunde;
 import de.shop.Util.Log;
 
 
 @ApplicationScoped
 @Log
 public class UriHelperAuftrag {	
 	@Inject
 	private UriHelperKunde uriHelperKunde;
 	
 	@Inject
 	private UriHelperArtikel uriHelperArtikel;
 	
 	@Inject
 	private UriHelperRechnung uriHelperRechnung; 
 	
 	public void updateUriAuftrag(Auftrag auftrag, UriInfo uriInfo) {
 		// URL fuer Kunde setzen
 		final Kunde kunde = auftrag.getKunde();
 		if (kunde != null) {
 			
 			final URI kundeUri = uriHelperKunde.getUriKunde(auftrag.getKunde(), uriInfo);
 			auftrag.setKundeUri(kundeUri);
 		}
 		
 		// URLs fuer Artikel in den Bestellpositionen setzen
 		final List<Auftragsposition> auftragspositionen = auftrag.getAuftragspositionen();
 		if (auftragspositionen != null && !auftragspositionen.isEmpty()) {
 			for (Auftragsposition ap : auftragspositionen) {
 				final URI artikelUri = uriHelperArtikel.getUriArtikel(ap.getArtikel(), uriInfo);
 				ap.setArtikelUri(artikelUri);
 			}
 		}
 		
 		final Rechnung rechnung = auftrag.getRechnung();
 		if (rechnung != null) {
 			final URI rechnungUri = uriHelperRechnung.getUriRechnung(rechnung, uriInfo);
 			auftrag.setRechnungUri(rechnungUri);
 		}
 	}
 
 	public URI getUriAuftrag(Auftrag auftrag, UriInfo uriInfo) {
 		final UriBuilder ub = uriInfo.getBaseUriBuilder()
 		                             .path(AuftragResource.class)
 		                             .path(AuftragResource.class, "findAuftragById");
 		final URI uri = ub.build(auftrag.getId());
 		return uri;
 	}
 	
 	public URI getUriAuftragByKunde(Kunde kunde, UriInfo uriInfo) {
 		final UriBuilder ub = uriInfo.getBaseUriBuilder()
 		                             .path(AuftragResource.class)
		                             .queryParam("kundenr", kunde.getKundenNr());
		final URI uri = ub.build();
 		return uri;
 	}
 }
