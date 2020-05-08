 package de.uni_hamburg.informatik.sep.zuul.server.features;
 
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import de.uni_hamburg.informatik.sep.zuul.server.befehle.Befehl;
 import de.uni_hamburg.informatik.sep.zuul.server.befehle.Befehlszeile;
 import de.uni_hamburg.informatik.sep.zuul.server.spiel.Spiel;
 import de.uni_hamburg.informatik.sep.zuul.server.spiel.Spieler;
 import de.uni_hamburg.informatik.sep.zuul.server.util.ServerKontext;
 import de.uni_hamburg.informatik.sep.zuul.server.util.TextVerwalter;
 
 public class BeinStellen implements BefehlAusfuehrenListener, Befehl, Feature
 {
 
 	public static final int INAKTIV_ZEIT = 10;
 
 	@Override
 	public boolean befehlSollAusgefuehrtWerden(ServerKontext kontext,
 			Spieler spieler, Befehl befehl)
 	{
 		if(!spieler.getAktiv())
 		{
 			kontext.schreibeAnSpieler(
 					spieler,
 					spieler.getName()
 							+ TextVerwalter.BEINSTELLEN_GEFALLEN_INAKTIV);
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public boolean vorbedingungErfuellt(ServerKontext kontext, Spieler spieler,
 			Befehlszeile befehlszeile)
 	{
 		return kontext.getSpielerInRaum(kontext.getAktuellenRaumZu(spieler))
 				.size() > 1;
 	}
 
 	@Override
 	public boolean ausfuehren(final ServerKontext kontext, Spieler spieler,
 			Befehlszeile befehlszeile)
 	{
 		List<Spieler> spielerInRaum = kontext.getSpielerInRaum(kontext
 				.getAktuellenRaumZu(spieler));
 		spielerInRaum.remove(spieler);
 		for(final Spieler fremderSpieler : spielerInRaum)
 		{
 			kontext.schreibeAnSpieler(spieler,TextVerwalter.beinstellenAnderemSpieler(fremderSpieler.getName()));
 			kontext.schreibeAnSpieler(fremderSpieler, TextVerwalter.beinstellenBekommen(spieler.getName()));
 			fremderSpieler.setAktiv(false);
 
 			new Timer().schedule(new TimerTask()
 			{
 
 				@Override
 				public void run()
 				{
 					fremderSpieler.setAktiv(true);
 					kontext.schreibeAnSpieler(fremderSpieler, TextVerwalter.BEINSTELLEN_AUFSTEHEN);
 
 				}
 			}, INAKTIV_ZEIT * Spiel.ONE_SECOND);
 		}
 		return true;
 	}
 
 	@Override
 	public void gibFehlerAus(ServerKontext kontext, Spieler spieler,
 			Befehlszeile befehlszeile)
 	{
 		kontext.schreibeAnSpieler(spieler, TextVerwalter.BEINSTELLEN_KEINER_DA);
 	}
 
 	@Override
 	public String[] getBefehlsnamen()
 	{
 		return new String[] { TextVerwalter.BEFEHL_BEINSTELLEN, "bs" };
 	}
 
 	@Override
 	public String getHilfe()
 	{
 		// TODO Hilfetext zu Bein Stellen
 		return "";
 	}
 
 }
