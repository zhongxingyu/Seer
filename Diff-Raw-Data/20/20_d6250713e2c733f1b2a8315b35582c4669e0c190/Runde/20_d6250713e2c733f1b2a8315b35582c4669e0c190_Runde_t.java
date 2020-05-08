 package applikation.server.pd;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Vector;
 
 import pd.karten.Karte;
 import pd.karten.KartenGeber;
 import applikation.nachrichten.KartenTausch;
 
 public class Runde {
 	private HashMap<Spieler, RundenTeilnahme> teilnahmen;
 	private Vector<Spieler> spieler;
 	private int aktuellerSpieler;
 	public final int nummer;
 
 	public Runde(int nummer, List<Spieler> spielers, KartenGeber kartenGeber) {
 		this.teilnahmen = new HashMap<Spieler, RundenTeilnahme>();
 		this.nummer = nummer;
 		this.spieler = new Vector<Spieler>(spielers);
 
 		int anzahlKarten = getAnzahlKartenProSpieler();
 
 		for (Spieler spieler : spielers) {
 			List<Karte> karten = kartenGeber.getKarten(anzahlKarten);
 			RundenTeilnahme rt = new RundenTeilnahme(spieler, karten);
 			teilnahmen.put(spieler, rt);
 		}
 	}
 
 	public void entferneSpieler(Spieler aktuellerSpieler) {
		RundenTeilnahme res_map = teilnahmen.remove(aktuellerSpieler);
		boolean res_vector= spieler.remove(aktuellerSpieler);

		if (res_map == null || !res_vector) {
			throw new RuntimeException("Konnte Spieler " + spieler + " nicht aus der aktuellen Runde entfernen");
		}
 	}
 
 	public boolean isFertigGetauscht() {
 		/* Pruefen ob irgend ein Spieler noch nicht getauscht hat */
 		for (RundenTeilnahme teilname : teilnahmen.values()) {
 			if (!teilname.hatGetauscht()) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * Markiert den nächsten Spieler als 'aktuellerSpieler'. Methode darf erst
 	 * aufgerufen ewrden nachdem mindestens ein Spieler hinzugefügt wurde.
 	 */
 	public void rotiereSpieler() {
 		int anzahlSpieler = teilnahmen.size();
 
		if (anzahlSpieler > 0) {
 			aktuellerSpieler = (aktuellerSpieler + 1) % anzahlSpieler;
		} else
 			throw new RuntimeException("Kann nicht rotieren, es gibt ja noch gar keine Spieler");
 	}
 
 	/**
 	 * Liefert aktuellen Spieler
 	 *
 	 * @return der zurzeit spielende Spieler
 	 */
 	public Spieler getAktuellerSpieler() {
 		return spieler.get(aktuellerSpieler);
 	}
 
 	/*
 	 * Anzahl Karten mit aufsteigender Rundennummer: 6, 5, 4, 3, 2, 6, 5, ...
 	 */
 	public int getAnzahlKartenProSpieler() {
 		return 6 - (nummer % 5);
 	}
 
 	public boolean isFertig() {
 		return teilnahmen.isEmpty();
 	}
 
 	public Collection<RundenTeilnahme> getTeilnamhmen() {
 		return teilnahmen.values();
 	}
 
 	public void tausche(Spieler spieler, KartenTausch tausch) {
 		RundenTeilnahme rt_spieler = teilnahmen.get(spieler);
 		RundenTeilnahme rt_partner = teilnahmen.get(spieler.partner);
 
 		if (rt_spieler.hatGetauscht())
 			throw new RuntimeException("Spieler " + spieler
 			                           + "versucht ein zweites mal zu tauschen");
 
 		rt_spieler.nimmWeg(tausch.karte);
 		rt_spieler.setHatGetauscht();
 		/* Transferiere Karte zum Partner */
 		rt_partner.setKarteVomPartner(tausch.karte);
 
 	}
 
 	public void tauscheKarten() {
 		for (RundenTeilnahme rt : teilnahmen.values()) {
 			rt.tasuchteKarte();
 		}
 	}
 
 	public RundenTeilnahme getTeilname(Spieler spieler) {
 		return teilnahmen.get(spieler);
 	}
 }
