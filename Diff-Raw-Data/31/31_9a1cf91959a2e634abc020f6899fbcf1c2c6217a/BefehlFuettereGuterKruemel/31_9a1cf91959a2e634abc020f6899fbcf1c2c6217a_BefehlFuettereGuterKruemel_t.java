 package de.uni_hamburg.informatik.sep.zuul.server.befehle;
 
 import java.util.LinkedList;
 
 import de.uni_hamburg.informatik.sep.zuul.server.features.Katze;
 import de.uni_hamburg.informatik.sep.zuul.server.inventar.Item;
 import de.uni_hamburg.informatik.sep.zuul.server.raum.Raum;
 import de.uni_hamburg.informatik.sep.zuul.server.spiel.Spiel;
 import de.uni_hamburg.informatik.sep.zuul.server.spiel.Spieler;
 import de.uni_hamburg.informatik.sep.zuul.server.util.FancyFunction;
 import de.uni_hamburg.informatik.sep.zuul.server.util.ServerKontext;
 import de.uni_hamburg.informatik.sep.zuul.server.util.TextVerwalter;
 
 public class BefehlFuettereGuterKruemel implements Befehl
 {
 
 	public static final String BEFEHLSNAME = TextVerwalter.BEFEHL_FUETTERE + " "
 			+ "maus";
 
 	/**
 	 * Bestimmt die Richtung, die die Maus empfiehlt abhängig davon, ob der
 	 * Kuchen giftig ist.
 	 */
 	static String bestimmeRichtung(Item kuchen, String richtigeRichtung,
 			String[] moeglicheRichtungen)
 	{
 		if(kuchen == Item.UKuchen || kuchen == Item.IKuchen)
 		{
 			return richtigeRichtung;
 		}
 		if(kuchen == Item.UGiftkuchen || kuchen == Item.IGiftkuchen)
 		{
 			LinkedList<String> richtungen = new LinkedList<String>();
 
 			for(String richtung : moeglicheRichtungen)
 				richtungen.add(richtung);
 
 			richtungen.remove(richtigeRichtung);
 
 			String falscheRichtung = FancyFunction.getRandomEntry(richtungen);
 
 			// Falls der Raum nur einen Ausgang hat.
 			if(falscheRichtung == null)
 				falscheRichtung = richtigeRichtung;
 
 			return falscheRichtung;
 		}
 
 		return null;
 	}
 	
 	@Override
 	public boolean vorbedingungErfuellt(ServerKontext kontext, Spieler spieler,
 			Befehlszeile befehlszeile)
 	{
 		// Wenn eine Katze oder eine Maus gefüttert werden könnte
 		Raum raum = kontext.getAktuellenRaumZu(spieler);
		return (raum.hasKatze() || raum.hasMaus()) && spieler.getInventar().has(Item.IKuchen);
 	}
 
 	@Override
 	public boolean ausfuehren(ServerKontext kontext, Spieler spieler,
 			Befehlszeile befehlszeile)
 	{
 		{
 			// Versuche eine Katze oder eine Maus zu füttern
 
 			Raum raum = kontext.getAktuellenRaumZu(spieler);
 			
 			//wenn katze im raum
 			
 			if(raum.hasKatze())
 			{
 				
 				Katze katze = raum.getKatze();
 
 				if(katze.isSatt())
 				{
 					kontext.schreibeAnSpieler(spieler, TextVerwalter.KATZE_HAT_KEINEN_HUNGER);
 					return false;
 				}
 				Item kuchen = spieler.getInventar().getAnyKuchen();
 				katze.fuettere(kontext, spieler, kuchen);
 
 				return true;
 			}
 			else if(raum.hasMaus())
 			{
 				Item kuchen = spieler.getInventar().getKuchen(Item.IKuchen);
 				Raum aktuellerRaum = kontext.getAktuellenRaumZu(spieler);
 
 				String richtigeRichtung = aktuellerRaum.getMaus().getRichtung();
 
 				String[] moeglicheRichtungen = aktuellerRaum.getMoeglicheAusgaenge();
 
 				String richtung = bestimmeRichtung(kuchen, richtigeRichtung,
 						moeglicheRichtungen);
 
 				String richtungsangabe = String.format(
 						TextVerwalter.MAUS_RICHTUNGSANGABE, richtung);
 
 				kontext.schreibeAnSpieler(spieler, richtungsangabe);
 
 				return true;
 			}
 			return false;
 		}
 	}
 
 	@Override
 	public void gibFehlerAus(ServerKontext kontext, Spieler spieler,
 			Befehlszeile befehlszeile)
 	{
 		Raum raum = kontext.getAktuellenRaumZu(spieler);
 
		if(!spieler.getInventar().has(Item.IKuchen))
 		{
 			kontext.schreibeAnSpieler(spieler, TextVerwalter.MAUS_KEIN_KRUEMEL);
 		}
 		else if(!raum.hasKatze() && !raum.hasMaus())
 		{
 			//TODO: Keine Katze zum Füttern!
 			kontext.schreibeAnSpieler(spieler, TextVerwalter.BEFEHL_FUETTERE_NICHTS_DA_ZUM_FUETTERN);
 		}
 	}
 
 	@Override
 	public String[] getBefehlsnamen()
 	{
 		return new String[] { TextVerwalter.BEFEHL_FUETTERE_GUT };
 	}
 
 	@Override
 	public String getHilfe()
 	{
 		return TextVerwalter.HILFE_FEED;
 	}
 
 }
