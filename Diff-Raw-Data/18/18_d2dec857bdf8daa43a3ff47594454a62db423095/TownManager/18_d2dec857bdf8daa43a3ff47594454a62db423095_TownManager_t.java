 package monopoly;
 
 import java.util.List;
 import java.util.ArrayList;
 
 import server.MMP;
 
 public class TownManager
 {
 
 	public List<Town> towns;
 
 	public TownManager()
 	{
 		towns = new ArrayList<Town>();
 	}
 
 	public Town get(int i)
 	{
 		return towns.get(i);
 	}
 
 	private boolean streetIsFull(Street s)
 	{
 		int count = 0;
 		for (int i = 0; i < towns.size(); i++)
 		{
 			if (((Town) towns.get(i)).street == s)
 			{
 				if (++count == 4)
 					return true;
 			}
 		}
 		return false;
 	}
 	
 	public int getPrice(int index)
 	{
 		return towns.get(index).getPrice();
 	}
 
 	public boolean isBuyable(int index)
 	{
		Town town = towns.get(index);
		if (!towns.get(index).hasOwner())
 		{
			SpecialTown t = town.getType();
			if(t == SpecialTown.Normaal || t == SpecialTown.Voorzieningen || t == SpecialTown.Station)
 				return true;
 			return false;
 		}
 		else
 			return false;
 	}
 
 	public int getCost(int index)
 	{
 		Town t = towns.get(index);
 		if (t.getType() == SpecialTown.Normaal)
 			return t.getCost();
 		else if (t.getType() == SpecialTown.Station) //TODO Improve this to 1250, 5000, 10000, 20000
 		{
 			int cost = 1250;
 			for (int i = 0; i < towns.size(); i++)
 			{
 				Town t2 = towns.get(i);
 				if (t2.getType() == SpecialTown.Station && t2.getOwner().equals(t.getOwner()))
 				{
 					cost *= 2;
 				}
 			}
 			return cost;
 		}
 		else
 		// SpecialTown.Voorziening
 		{
 			int voorzieningen = 0;
 			for (int i = 0; i < towns.size(); i++)
 			{
 				Town t2 = towns.get(i);
 				if (t2.getType() == SpecialTown.Voorzieningen && t2.getOwner().equals(t.getOwner()))
 				{
 					voorzieningen++;
 				}
 			}
 			return MMP.getServer().Monopoly().diceEyesCount() * voorzieningen * 500;
 		}
 	}
 
 	public void Add(String townName, Street streetName, int Price, int Cost)
 	{
 		Add(townName, streetName, Price, Cost, SpecialTown.Normaal);
 	}
 
 	public void Add(String townName, Street streetName, int Price, int Cost, SpecialTown Special)
 	{
 		if (streetIsFull(streetName))
 		{
 			MMP.Log("Can not Add \"" + townName + "\" in " + streetName.toString() + ": Street is full");
 			return;
 		}
 		towns.add(new Town(townName, streetName, Price, Cost, Special));
 	}
 
 }
