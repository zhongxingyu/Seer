 package de.uni_hamburg.informatik.sep.zuul.server.befehle;
 
 import de.uni_hamburg.informatik.sep.zuul.client.FileChooser;
 import de.uni_hamburg.informatik.sep.zuul.server.spiel.Spieler;
 import de.uni_hamburg.informatik.sep.zuul.server.util.ServerKontext;
 import de.uni_hamburg.informatik.sep.zuul.server.util.TextVerwalter;
 
 public class BefehlLaden implements Befehl
 {
 	@Override
 	public boolean vorbedingungErfuellt(ServerKontext kontext, Spieler spieler,
 			Befehlszeile befehlszeile)
 	{
 		return true;
 	}
 
 	@Override
 	public boolean ausfuehren(ServerKontext kontext, Spieler spieler,
 			Befehlszeile befehlszeile)
 	{
 		//TODO Level laden
 		if(befehlszeile.getGeparsteZeile().size() == 1)
 		{
 
			String level = FileChooser.oeffneDatei();
 			//Spiel.getInstance().spielen(level);
 		}
 		else
 		{
 			String level = befehlszeile.getGeparsteZeile().get(1);
 		}
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
 		return new String[] { TextVerwalter.BEFEHL_LADEN };
 	}
 
 	@Override
 	public String getHilfe()
 	{
 		return TextVerwalter.HILFE_LOAD;
 	}
 
 }
