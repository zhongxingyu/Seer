 package de.htwg.lpn.wgbuddy.random;
 
 
 
 import java.util.SortedSet;
 import java.util.TreeMap;
 
 
 
 public class RandomUser {
 	
 	private Double totalPoints;
 	
 	/**
 	 * berechnet wieviel prozent von der gesamtpunktzahl ein User hat
 	 * 
 	 * @param points
 	 * @return
 	 */
 	private Double getProzent(Double points)
 	{
 		return points / totalPoints;
 	}
 		
 	/**
 	 * Bekommt eine TreeMap mit Usern bergeben und gibt einen zuflligen User zurck
 	 * 
 	 * @param userlist
 	 * @return
 	 */
 	public String getRandomUser(TreeMap<String, Double> userlist)
 	{
 		//TODO key und value tauschen und bei gleicher punktzahl vorauswahltreffen
 		TreeMap<Double, String> randomTreeMap = createRandomTreeMap(createPreselectionMap(userlist));
 		findRandomItem(randomTreeMap);
 		return randomTreeMap.firstEntry().getValue();
 	}
 	
 	/**
 	 * erstellt eine TreeMap mit den Pints als key. Bei gleicher Punktzahl wird per Zufall 
 	 * entschieden welcher User in die TreeMap eingetragen wird
 	 * 
 	 * @param userlist
 	 * @return
 	 */
 	private TreeMap<Double, String> createPreselectionMap(TreeMap<String, Double> userlist) 
 	{
 		TreeMap<Double, String> ret = new TreeMap<Double, String>();
 		for(String key : userlist.keySet())
 		{
 			if(!ret.containsKey(userlist.get(key)))
 			{
 				ret.put(userlist.get(key), key);
 			}
 			else
 			{
 				if(Math.random() > 0.5)
 				{
 					ret.remove(userlist.get(key));
 					ret.put(userlist.get(key), key);
 				}
 			}
 		}
 		return ret;
 	}
 
 	/**
 	 * erstellt eine TreeMap in welcher die User in der reihenfolge nach ihren punkten eingetragen sind
 	 * 
 	 * @param userlist
 	 * @return
 	 */
 	private TreeMap<Double, String> createRandomTreeMap(TreeMap<Double, String> userlist) 
 	{
 		totalPoints = calcTotalPoints(userlist);
 		TreeMap<Double, String> ret = new TreeMap<Double, String>();
 		fillMap(userlist, ret);
 
 		return ret;
 	}
 
 	/**
 	 * berechnet die gesamtanzahl der Punkte aller User
 	 * 
 	 * @param userlist
 	 * @return
 	 */
 	private Double calcTotalPoints(TreeMap<Double, String> userlist) 
 	{
 		Double ret = 0.00;
 		for(Double key : userlist.keySet())
 		{
 			ret += key; 
 		}
 		return ret;
 	}
 
 	/**
 	 * befhlt die TreeMap mit Usern als value und den Punkten jeweils aufaddiert mit den Punkten des vorherigen 
 	 * Users beginnend bei dem User mit den meiten Punkten
 	 * 
 	 * @param userlist
 	 * @param ret
 	 */
 	private void fillMap(TreeMap<Double, String> userlist, TreeMap<Double, String> ret) 
 	{			
 		SortedSet<Double> sortedKeys = (SortedSet<Double>) userlist.keySet();
 		Double prozent = getProzent(Double.valueOf(sortedKeys.toArray()[sortedKeys.size() - 1].toString()));
 		ret.put(prozent, userlist.get(Double.valueOf(sortedKeys.toArray()[sortedKeys.size() - 1].toString())));
 		userlist.remove(Double.valueOf(sortedKeys.toArray()[sortedKeys.size() - 1].toString()));
 		
 		for(int i = userlist.keySet().size() - 1; i >= 0; i--)
 		{	
 			ret.put(getProzent(Double.valueOf(sortedKeys.toArray()[i].toString())) + prozent, userlist.get(Double.valueOf(sortedKeys.toArray()[i].toString())));
 			prozent += getProzent(Double.valueOf(sortedKeys.toArray()[i].toString()));
 			userlist.remove(Double.valueOf(sortedKeys.toArray()[sortedKeys.size() - 1].toString()));
 		}
 	}
 
 	/**
 	 * whlt zufllig einen user aus und lscht ihn aus der TreeMap bis nur noch ein User in der Treemap vorhanden ist
 	 * 
 	 * @param randomTreeMap
 	 */
 	private void findRandomItem(TreeMap<Double, String> randomTreeMap)
 	{
 		if(randomTreeMap.size() > 1)
 		{
			randomTreeMap.remove(randomTreeMap.ceilingEntry(Math.random()).getKey());
 			findRandomItem(randomTreeMap);
 		}
 		else
 		{
 			return;
 		}
 	}
 }
