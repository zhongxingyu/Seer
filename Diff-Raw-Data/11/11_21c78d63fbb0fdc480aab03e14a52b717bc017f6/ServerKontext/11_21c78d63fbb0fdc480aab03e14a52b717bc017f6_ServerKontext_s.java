 package de.uni_hamburg.informatik.sep.zuul.server.util;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import de.uni_hamburg.informatik.sep.zuul.server.features.RaumGeaendertListener;
 import de.uni_hamburg.informatik.sep.zuul.server.raum.Raum;
 import de.uni_hamburg.informatik.sep.zuul.server.spiel.Spieler;
 
 /**
  * Hält alle angemeldeten Spieler und kennt den Spielzustand. Dabei hat er
  * Wissen über die Räume und den Levelaufbau (die Raumstruktur).
  * 
  * @author 0ortmann, 0Schlund
  * 
  */
 public class ServerKontext
 {
 
 	private Map<Spieler, String> _nachrichtenCache = new HashMap<Spieler, String>();
 	private Map<Spieler, Raum> _spielerPosition;
 	private Raum _startRaum;
 	private ArrayList<RaumGeaendertListener> _raumGeaendertListeners = new ArrayList<RaumGeaendertListener>();
 
 	public ServerKontext(Raum startRaum)
 	{
 
 		_startRaum = startRaum;
 		_spielerPosition = new HashMap<Spieler, Raum>();
 	}
 
 	/**
 	 * Fuege einen neuen Spieler hinzu. Er startet dabei immer im Startraum.
 	 * 
 	 * @param spieler
 	 *            der neue Spieler
 	 */
 	public void fuegeNeuenSpielerHinzu(Spieler spieler)
 	{
 		_spielerPosition.put(spieler, _startRaum);
 	}
 
 	/**
 	 * Entferne den angegebenen Spieler aus dem Spiel.
 	 * 
 	 * @param name
 	 *            name des zu entfernenden Spielers
 	 */
 	public void entferneSpieler(String name)
 	{
 		for(Spieler spieler : getSpielerListe())
 		{
 			if(name.equals(spieler.getName()))
 			{
 				_spielerPosition.remove(spieler);
 			}
 		}
 	}
 
 	/**
 	 * Gibt den Raum zurück, in dem der Spieler sich befindet.
 	 * 
 	 * @param spieler
 	 *            der Spieler zu dem wir den Raum suchen
 	 * @return der Raum in dem der Spieler ist
 	 */
 	public Raum getAktuellenRaumZu(Spieler spieler)
 	{
 		return _spielerPosition.get(spieler);
 	}
 
 	/**
 	 * Setze den aktuellen Raum des Spielers neu.
 	 * 
 	 * @param spieler
 	 *            der Spieler
 	 * @param neuerRaum
 	 *            der neu aktuelle aufenthalts Raum des Spielers
 	 */
 	public void setAktuellenRaumZu(Spieler spieler, Raum neuerRaum)
 	{
 		assert (_spielerPosition.containsKey(spieler));
 		_spielerPosition.remove(spieler);
 		_spielerPosition.put(spieler, neuerRaum);
 	}
 
 	/**
 	 * Gib alle registrierten Spieler in einer Liste aus.
 	 * 
 	 * @return Liste aller Spieler
 	 */
 	public List<Spieler> getSpielerListe()
 	{
 		return new ArrayList<Spieler>(_spielerPosition.keySet());
 	}
 
 	/**
 	 * Gibt eine Liste von Spielern aus dem aktuellen Raum.
 	 * 
 	 * @param aktuellerRaum
 	 *            der Raum
 	 * @return alle Spieler in dem Raum
 	 */
 	public List<String> getSpielerNamenInRaum(Raum aktuellerRaum)
 	{
 		ArrayList<String> result = new ArrayList<String>();
		assert _spielerPosition.containsValue(aktuellerRaum);
 
 		for(Spieler s : _spielerPosition.keySet())
 		{
 			Raum raum = _spielerPosition.get(s);
 			if(raum.equals(aktuellerRaum))
 			{
 				result.add(s.getName());
 			}
 		}
 		return result;
 
 	}
 
 	/**
 	 * Gibt die Nachricht für den Spieler und leert den NachrichtenCache.
 	 * 
 	 * @param spieler
 	 * @return
 	 */
 	public String getNachrichtFuer(Spieler spieler)
 	{
 
 		String nachricht = _nachrichtenCache.remove(spieler);
 
 		return nachricht;
 	}
 
 	/**
 	 * Speichert eine Nachricht für den Spieler zwischen.
 	 */
 	public void schreibeAnSpieler(Spieler spieler, String nachricht)
 	{
 		String s = "";
 		if(_nachrichtenCache.containsKey(spieler))
 			s = _nachrichtenCache.get(spieler);
 		s = s + nachricht + "\n";
 		_nachrichtenCache.put(spieler, s);
 	}
 
 	public Spieler getSpielerByName(String benutzerName)
 	{
 		for(Spieler s : _spielerPosition.keySet())
 		{
 			if(benutzerName.equals(s.getName()))
 			{
 				return s;
 			}
 		}
 		return null;
 	}
 
 	public List<Raum> getRaeumeInDemSichSpielerAufhalten()
 	{
 		return new ArrayList<Raum>(_spielerPosition.values());
 	}
 
 	public List<Spieler> getSpielerInRaum(Raum raum)
 	{
 		ArrayList<Spieler> spielers = new ArrayList<Spieler>();
 		for(Spieler spieler : _spielerPosition.keySet())
 		{
 			if(getAktuellenRaumZu(spieler) == raum)
 				spielers.add(spieler);
 		}
 		return spielers;
 	}
 
 	/**
 	 * Gehe mit dem Spieler in einen anderen Raum
 	 * 
 	 * @param spielLogik
 	 *            TODO
 	 * @param spieler
 	 *            der Spieler der Laufe soll
 	 * @param raum
 	 *            der Raum in den es gehen soll
 	 */
 	public void wechseleRaum(Spieler spieler, Raum raum)
 	{
 		Raum alterRaum = getAktuellenRaumZu(spieler);
 		setAktuellenRaumZu(spieler, raum);
 
 		fuehreRaumGeaendertListenerAus(spieler, alterRaum, raum);
 	}
 
 	public void fuehreRaumGeaendertListenerAus(Spieler spieler, Raum alterRaum,
 			Raum neuerRaum)
 	{
 		// Führe alle BefehlAusgefuehrtListener aus.
 		for(RaumGeaendertListener raumGeaendertListener : _raumGeaendertListeners)
 		{
 			raumGeaendertListener.raumGeaendert(this, spieler, alterRaum,
 					neuerRaum);
 		}
 	}
 
 	public ArrayList<RaumGeaendertListener> getRaumGeaendertListeners()
 	{
 		return _raumGeaendertListeners;
 	}
 
 	public void spielerGewinnt(Spieler spieler)
 	{
 		for(Spieler s : getSpielerListe())
 			s.die();
 		spieler.gewinnt();
 	}
 
 	public void schreibeAnAlleSpielerInRaum(Raum raum, String nachricht)
 	{
 		List<Spieler> spielerInRaum = getSpielerInRaum(raum);
 		for(Spieler spieler : spielerInRaum)
 		{
 			schreibeAnSpieler(spieler, nachricht);
 		}
 	}
 
 }
