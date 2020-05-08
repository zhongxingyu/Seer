 package de.uni_hamburg.informatik.sep.zuul.server.befehle;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import de.uni_hamburg.informatik.sep.zuul.server.features.BeinStellen;
 
 public final class BefehlFactory
 {
 	private static final Map<String, Befehl> _map;
 
 	static
 	{
 		Befehl[] befehle = new Befehl[] { new BefehlGehe(), new BefehlHilfe(),
 				new BefehlNehmen(), new BefehlEssenBoden(),
 				new BefehlEssenTasche(), new BefehlEssenTascheGuterKruemel(),
 				new BefehlEssenTascheSchlechterKruemel(),
 				new BefehlEssenTascheUnbekannterKruemel(), new BefehlBeenden(),
 				new BefehlUntersuche(), new BefehlInventarAnzeigen(),
 				new BefehlFuettere(), new BefehlFuettereSchlechterKruemel(),
 				new BefehlFuettereGuterKruemel(),
 				new BefehlFuettereUnbekanntenKruemel(), new BefehlAblegen(),
 				new BefehlAblegenKruemel(), new BefehlAblegenGuterKruemel(),
 				new BefehlAblegenSchlechterKruemel(), new BefehlSchauen(),
 				new BefehlGibMirMehrLeben(), new BeinStellen(),
 				new BefehlDebug() };
 
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
 		for(Befehl befehl : _map.values())
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
 
 		for(int i = 0; i < geparsteZeile.size(); i++)
 			befehlsnamen.add("");
 
 		for(int i = 0; i < geparsteZeile.size(); i++)
 		{
 			for(int j = i; j < geparsteZeile.size(); j++)
 			{
 				String previous = befehlsnamen.get(j);
				if(!previous.isEmpty())
 					previous += " ";
 				befehlsnamen.set(j, previous + geparsteZeile.get(i));
 			}
 		}
 		Collections.reverse(befehlsnamen);
 		return befehlsnamen;
 	}
 
 	public static Befehl gibBefehl(String string)
 	{
 		return _map.get(string);
 	}
 
 	/**
 	 * @return the Map
 	 */
 	public static Map<String, Befehl> getMap()
 	{
 		return _map;
 	}
 }
