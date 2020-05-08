 package de.uni_hamburg.informatik.sep.zuul.server.npcs;
 
 import java.util.ArrayList;
 
 import de.uni_hamburg.informatik.sep.zuul.server.features.Katze;
 import de.uni_hamburg.informatik.sep.zuul.server.raum.Raum;
 import de.uni_hamburg.informatik.sep.zuul.server.util.FancyFunction;
 import de.uni_hamburg.informatik.sep.zuul.server.util.PathFinder;
 import de.uni_hamburg.informatik.sep.zuul.server.util.ServerKontext;
 import de.uni_hamburg.informatik.sep.zuul.server.util.TextVerwalter;
 
 public class Maus
 {
 
 	private PathFinder _pathFinder;
 	private Raum _aktuellerRaum;
 	private Raum _endRaum;
 
 	public Maus(Raum aktuellerRaum, Raum endRaum)
 	{
 		_aktuellerRaum = aktuellerRaum;
 		_endRaum = endRaum;
 	}
 
 	/**
 	 * @return the _richtung
 	 */
 	public String getRichtung()
 	{
 		_pathFinder = new PathFinder()
 		{
 			@Override
 			protected boolean isZielRaum(Raum raum)
 			{
 				return raum == _endRaum;
 			}
 		};
 
		return PathFinder
 				.getRichtung(_pathFinder.findPath(_aktuellerRaum));
 	}
 
 	public void wirdVonKatzeVerjagt(ServerKontext kontext)
 	{
 		// TODO: Better selection of rooms ( No start and end point, ... )
 		ArrayList<Raum> moeglicheRaeumeFuerMaus = _aktuellerRaum.getAusgaenge();
 		
 		Raum neuerRaumFuerMaus = Katze.selectRaumOhneKatze(moeglicheRaeumeFuerMaus);
 		
 		_aktuellerRaum.setMaus(null);
 		if(neuerRaumFuerMaus == null)
 			return;
 			
		_aktuellerRaum = neuerRaumFuerMaus;
 		neuerRaumFuerMaus.setMaus(this);
 	
		if(kontext != null)
			kontext.schreibeAnAlleSpielerInRaum(_aktuellerRaum,
 				TextVerwalter.KATZE_VERJAGT_DIE_MAUS);
 	}
 
 }
