 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 
 public class Player {
 	private List<Card> cardList;
 	private Map<Territory, Integer> unitMap;
 	public Player()
 	{
 		cardList = new ArrayList<Card>();
 		unitMap = new HashMap<Territory, Integer>();
 	}
 	public void placeReinforcements(int number)
 	{
 		//TODO: Be shitty and place everything in one territory randomly
 		Territory territory = getRandomControlledTerritory();
 		addToTerritory(territory, number);
 	}
 	public Territory getRandomControlledTerritory()
 	{
 		Set<Territory> territories = unitMap.keySet();
 		Random random = new Random();
 		int territoryID = random.nextInt(territories.size());
 		int i = 0;
 		for(Territory terr : territories)
 		{
 			if(i == territoryID)
 			{
 				return terr;
 			}
 			++i;
 		}
 		throw new RuntimeException("wat");
 	}
 	public void addToTerritory(Territory territory, int number)
 	{
 		unitMap.put(territory, unitMap.get(territory)+number);
 	}
 	public boolean isOwnerOf(Territory territory)
 	{
 		return unitMap.containsKey(territory);
 	}
 	public int getUnitsInTerritory(Territory territory)
 	{
 		return unitMap.get(territory);
 	}
 	public void attack(Territory from,Territory to)
 	{
		int unitsFrom = from.getUnitCount();
 		int unitsTo = to.getUnitCount();
 		
 		int countDiceFrom = Math.min(unitsFrom-1, 3);
 		int countDiceTo = Math.min(unitsTo, 2);
 		
 		List<Integer> diceFromList = new ArrayList<Integer>();
 		List<Integer> diceToList = new ArrayList<Integer>();
 		
 		Random random = new Random();
 		for(int i=0;i<countDiceFrom;++i)
 		{
 			//TODO: Roll attacking dice
 			diceFromList.add(random.nextInt(6)+1);
 		}
 		for(int i=0;i<countDiceTo;++i)
 		{
 			//TODO: Roll defending dice
 			diceToList.add(random.nextInt(6)+1);
 		}
 		
 		Collections.sort(diceFromList);
 		Collections.reverse(diceFromList);
 		Collections.sort(diceToList);
 		Collections.reverse(diceToList);
 		
 		for(int i=0;i<Math.min(countDiceFrom, countDiceTo);++i)
 		{
 			if(countDiceFrom > countDiceTo)
 			{
 				//attacker wins
 				to.getOwner().addToTerritory(to, -1);
 			}
 			else
 			{
 				//defender wins
				from.getOwner().addToTerritory(from, -1);
 			}
 		}
 	}
 
 }
