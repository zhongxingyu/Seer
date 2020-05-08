 package de.uni_hamburg.informatik.sep.zuul.befehle;
 
 import java.util.LinkedList;
 import java.util.Random;
 
 import de.uni_hamburg.informatik.sep.zuul.spiel.Item;
 import de.uni_hamburg.informatik.sep.zuul.spiel.RaumArt;
 import de.uni_hamburg.informatik.sep.zuul.spiel.SpielKontext;
 import de.uni_hamburg.informatik.sep.zuul.spiel.TextVerwalter;
 
 final class BefehlGive extends Befehl
 {
 
 	@Override
 	public void ausfuehren(SpielKontext kontext)
 	{
 
 		if(kontext.getAktuellerRaum().hasMaus())
 		{
 			if(!kontext.getInventar().hasAnyKuchen())
 			{
 				kontext.schreibeNL(TextVerwalter.MAUS_KEIN_KRUEMEL);
 				return;
 			}
 
 			Item kuchen = kontext.getInventar().getAnyKuchen();
 
 			String richtigeRichtung = kontext.getAktuellerRaum().getMaus()
 					.getRichtung();
 			
 			String[] moeglicheRichtungen = kontext.getAktuellerRaum().getMoeglicheAusgaenge();
 
 			String richtung = bestimmeRichtung(kuchen, richtigeRichtung, moeglicheRichtungen);
 
 			String richtungsangabe = String.format(
 					TextVerwalter.MAUS_RICHTUNGSANGABE, richtung);
 			kontext.schreibeNL(richtungsangabe);
 			return;
 		}
 		if(kontext.getAktuellerRaum().getRaumart() == RaumArt.Start)
 		{
 			if(!kontext.getInventar().hasAnyKuchen())
 			{
 				kontext.schreibeNL(TextVerwalter.LABOR_KEIN_KRUEMEL);
 				return;
 			}
 			
 
 			Item kuchen = kontext.getInventar().getAnyKuchen();
 			switch(kuchen)
 			{
 			case Kuchen:
 				kontext.schreibeNL(TextVerwalter.LABOR_GESUNDER_KUCHEN);
 				break;
 			case Giftkuchen:
				kontext.schreibeNL(TextVerwalter.LABOR_GIFTIGER_KUCHEN);
 				break;
 			}
 			kontext.getInventar().fuegeItemHinzu(kuchen);
 			return;
 		}
 		
 		kontext.schreibeNL(TextVerwalter.BEFEHL_GIB_KEIN_OBJEKT);
 		return;
 	}
 
 	/**
 	 * @param kuchen
 	 * @param richtigeRichtung
 	 * @return
 	 */
 	static String bestimmeRichtung(Item kuchen, String richtigeRichtung, String[] moeglicheRichtungen)
 	{
 		if(kuchen == Item.Kuchen)
 		{
 			return richtigeRichtung;
 		}
 		if(kuchen == Item.Giftkuchen)
 		{
 			LinkedList<String> richtungen = new LinkedList<String>();
 			
 			for(String richtung : moeglicheRichtungen)
 				richtungen.add(richtung);
 
 			richtungen.remove(richtigeRichtung);
 
 			int randomInt = new Random().nextInt(richtungen.size());
 
 			return richtungen.get(randomInt);
 		}
 		
 		return null;
 	}
 
 	@Override
 	public String getBefehlsname()
 	{
 		return TextVerwalter.BEFEHL_GIB;
 	}
 
 }
