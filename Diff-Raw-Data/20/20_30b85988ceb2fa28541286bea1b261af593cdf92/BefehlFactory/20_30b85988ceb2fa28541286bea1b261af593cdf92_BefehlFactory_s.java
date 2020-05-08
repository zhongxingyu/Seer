 package de.uni_hamburg.informatik.sep.zuul.server.befehle;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import de.uni_hamburg.informatik.sep.zuul.server.spiel.Spieler;
 import de.uni_hamburg.informatik.sep.zuul.server.util.ServerKontext;
 import de.uni_hamburg.informatik.sep.zuul.server.util.TextVerwalter;
 
 public final class BefehlFactory
 {
 	static final Map<String, Befehl> _map;
 
 	static
 	{
 		Befehl[] befehle = new Befehl[] { new BefehlGehe(), new BefehlHilfe(),
 				new BefehlNehmen(), new BefehlEssen(), new BefehlEssenBoden(),
 				new BefehlEssenTasche(), new BefehlLaden(),
 				new BefehlBeenden(), new BefehlGib(),
 				new BefehlInventarAnzeigen(), new BefehlFuettere(),
 				new BefehlFuettereKatze(), new BefehlFuettereMaus(),
				new BefehlAblegen() };
 
 		_map = new HashMap<String, Befehl>();
 		for(Befehl befehl : befehle)
 		{
 			for(String alias : befehl.getBefehlsnamen())
 			{
 				_map.put(alias, befehl);
 			}
 		}
 	}
 	
 	public static Befehl gibBefehl(Class<?> befehlsKlasse)
 	{
 		for(Befehl befehl: _map.values())
 		{
 			if(befehlsKlasse.isInstance(befehl))
 				return befehl;
 		}
 		return null;
 	}
 
 	/**
 	 * @param kontext
 	 * @param spieler
 	 * @param befehlszeile
 	 */
 	public static Befehl gibBefehl(Befehlszeile befehlszeile)
 	{
 		List<String> geparsteZeile = befehlszeile.getGeparsteZeile();
 		Collection<String> befehlsnamen = moeglicheBefehlsnamen(geparsteZeile);
 
 		for(String befehlsname : befehlsnamen)
 		{
 			Befehl befehl = _map.get(befehlsname);
 
 			if(befehl != null)
 			{
 				return befehl;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Gibt mögliche Befehle zurück. gib mir mehr leben => { gib mir mehr leben,
 	 * gib mir mehr, gib mir, gib }
 	 * 
 	 * @param geparsteZeile
 	 * @return
 	 */
 	private static Collection<String> moeglicheBefehlsnamen(
 			List<String> geparsteZeile)
 	{
 		ArrayList<String> befehlsnamen = new ArrayList<String>();
 
 		if(geparsteZeile.size() == 0)
 			return befehlsnamen;
 
 		befehlsnamen.add(geparsteZeile.get(0));
 
 		for(int i = 1; i < geparsteZeile.size(); i++)
 		{
 			String aktuellerToken = geparsteZeile.get(i);
 			ArrayList<String> letzteToken = (ArrayList<String>) befehlsnamen
 					.clone();
 			letzteToken.add(aktuellerToken);
 			befehlsnamen.add(Befehlszeile.join(letzteToken));
 		}
 		Collections.reverse(befehlsnamen);
 		return befehlsnamen;
 	}
 
 	public static void beendeSpielFuer(ServerKontext kontext, Spieler spieler,
 			String nachricht)
 	{
 
 	}
 
 	public static Befehl gibBefehl(String string)
 	{
 		return _map.get(string);
 	}
 }
