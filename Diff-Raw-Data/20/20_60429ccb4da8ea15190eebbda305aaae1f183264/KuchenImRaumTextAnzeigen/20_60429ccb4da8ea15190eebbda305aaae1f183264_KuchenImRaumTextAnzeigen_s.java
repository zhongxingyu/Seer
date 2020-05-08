 package de.uni_hamburg.informatik.sep.zuul.server.features;
 
 import de.uni_hamburg.informatik.sep.zuul.server.befehle.Befehl;
import de.uni_hamburg.informatik.sep.zuul.server.inventar.Item;
 import de.uni_hamburg.informatik.sep.zuul.server.spiel.Spieler;
 import de.uni_hamburg.informatik.sep.zuul.server.util.ServerKontext;
 import de.uni_hamburg.informatik.sep.zuul.server.util.TextVerwalter;
 
 public final class KuchenImRaumTextAnzeigen implements Feature,
 		BefehlAusgefuehrtListener
 {
 	@Override
 	public boolean befehlAusgefuehrt(ServerKontext kontext, Spieler spieler,
 			Befehl befehl, boolean hasRoomChanged)
 	{
 		if(hasRoomChanged)
 		{
			Item item = kontext.getAktuellenRaumZu(spieler).getNaechstesItem();
			if(item.isAnyKuchen())
 			{
 				kontext.schreibeAnSpieler(spieler,
 						TextVerwalter.KUCHENIMRAUMTEXT);
 			}
 		}
 
 		return true;
 	}
 }
