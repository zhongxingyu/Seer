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
 
	private String _richtung;
 	private PathFinder _pathFinder;
 	private Raum _aktuellerRaum;
 	private Raum _endRaum;
 
 	public Maus(Raum aktuellerRaum, Raum endRaum)
 	{
 		_aktuellerRaum = aktuellerRaum;
 		_endRaum = endRaum;

		berechneNeuenWeg();
 	}
 
 	/**
 	 * @return the _richtung
 	 */
 	public String getRichtung()
 	{
		return _richtung;
	}

	public void berechneNeuenWeg()
	{
 		_pathFinder = new PathFinder()
 		{

 			@Override
 			protected boolean isZielRaum(Raum raum)
 			{
 				return raum == _endRaum;
 			}
 		};
 
		_richtung = PathFinder
 				.getRichtung(_pathFinder.findPath(_aktuellerRaum));
 	}
 
	public void setNeuerRaum(Raum neuerRaum)
	{
		_aktuellerRaum = neuerRaum;
		berechneNeuenWeg();
	}

 	public void wirdVonKatzeVerjagt(ServerKontext kontext)
 	{
 		// TODO: Better selection of rooms ( No start and end point, ... )
 		ArrayList<Raum> moeglicheRaeumeFuerMaus = _aktuellerRaum.getAusgaenge();
 		
 		Raum neuerRaumFuerMaus = Katze.selectRaumOhneKatze(moeglicheRaeumeFuerMaus);
 		
 		_aktuellerRaum.setMaus(null);
 		if(neuerRaumFuerMaus == null)
 			return;
 			
 		neuerRaumFuerMaus.setMaus(this);
 	
		kontext.schreibeAnAlleSpielerInRaum(_aktuellerRaum,
 				TextVerwalter.KATZE_VERJAGT_DIE_MAUS);
 	}
 
 }
