 package de.uni_hamburg.informatik.sep.zuul.server.befehle;
 
 import de.uni_hamburg.informatik.sep.zuul.server.spiel.Spieler;
 import de.uni_hamburg.informatik.sep.zuul.server.util.ServerKontext;
 import de.uni_hamburg.informatik.sep.zuul.server.util.TextVerwalter;
 
 final class BefehlHilfe implements Befehl
 {
 	@Override
 	public boolean ausfuehren(ServerKontext kontext, Spieler spieler,
 			Befehlszeile befehlszeile)
 	{
 
		kontext.schreibeAnSpieler(spieler, TextVerwalter.HILFETEXT);

 		if(befehlszeile.getGeparsteZeile().size() == 1)
 		{
 			kontext.schreibeAnSpieler(spieler, TextVerwalter.HILFETEXT);
 			return true;
 		}
 		Befehl befehl = BefehlFactory.gibBefehl(befehlszeile.getGeparsteZeile()
 				.get(1));
 
 		String hilfetext = TextVerwalter.HILFETEXT;
 		if(befehl != null)
 		{
 			hilfetext = befehl.getHilfe();
 		}
 		kontext.schreibeAnSpieler(spieler, hilfetext);
 
 		return true;
 	}
 
 	@Override
 	public boolean vorbedingungErfuellt(ServerKontext kontext, Spieler spieler,
 			Befehlszeile befehlszeile)
 	{
 		return true;
 	}
 
 	@Override
 	public void gibFehlerAus(ServerKontext kontext, Spieler spieler,
 			Befehlszeile befehlszeile)
 	{
 	}
 
 	@Override
 	public String[] getBefehlsnamen()
 	{
 		return new String[] { TextVerwalter.BEFEHL_HILFE };
 	}
 
 	@Override
 	public String getHilfe()
 	{
 		return TextVerwalter.HILFE_HELP;
 	}
 }
