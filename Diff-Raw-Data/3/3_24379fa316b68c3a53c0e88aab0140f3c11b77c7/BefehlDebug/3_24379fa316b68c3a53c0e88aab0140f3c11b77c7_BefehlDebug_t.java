 package de.uni_hamburg.informatik.sep.zuul.server.befehle;
 
 import de.uni_hamburg.informatik.sep.zuul.server.raum.Raum;
 import de.uni_hamburg.informatik.sep.zuul.server.spiel.Spieler;
 import de.uni_hamburg.informatik.sep.zuul.server.util.ServerKontext;
 
 public class BefehlDebug implements Befehl
 {
 
 	@Override
 	public boolean vorbedingungErfuellt(ServerKontext kontext, Spieler spieler,
 			Befehlszeile befehlszeile)
 	{
 		return befehlszeile.getGeparsteZeile().size() > 1;
 	}
 
 	@Override
 	public boolean ausfuehren(ServerKontext kontext, Spieler spieler,
 			Befehlszeile befehlszeile)
 	{
 		String string = befehlszeile.getGeparsteZeile().get(1);
 		if("katze".equals(string))
 		{
 			kontext.schreibeAnSpieler(spieler, "Katzen:");
 			for(Raum raum : kontext.getRaeume())
 			{
 				if(raum.hasKatze())
 					kontext.schreibeAnSpieler(spieler, raum.getName());
 			}
 		}
 		if("maus".equals(string))
 		{
 			kontext.schreibeAnSpieler(spieler, "Mäuse:");
 			for(Raum raum : kontext.getRaeume())
 			{
 				if(raum.hasMaus())
 					kontext.schreibeAnSpieler(spieler, raum.getName());
 			}
 		}
 		if("spieler".equals(string))
 		{
 			kontext.schreibeAnSpieler(spieler, "Spieler:");
 			for(Spieler fremderSpieler : kontext.getSpielerListe())
 			{
 				kontext.schreibeAnSpieler(spieler, fremderSpieler.getName()
						+ ": "
						+ kontext.getAktuellenRaumZu(fremderSpieler).getName());
 			}
 		}
 		if("lebensenergie".equals(string))
 		{
 			kontext.schreibeAnSpieler(spieler, "Lebensenergie:");
 			kontext.schreibeAnSpieler(spieler, "" + spieler.getLebensEnergie());
 		}
 		return false;
 	}
 
 	@Override
 	public void gibFehlerAus(ServerKontext kontext, Spieler spieler,
 			Befehlszeile befehlszeile)
 	{
 
 	}
 
 	@Override
 	public String[] getBefehlsnamen()
 	{
 		return new String[] { "debug" };
 	}
 
 	@Override
 	public String getHilfe()
 	{
 		return "Debug me!";
 	}
 
 }
