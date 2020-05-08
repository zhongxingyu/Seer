 package monopoly;
 
 import java.awt.Graphics;
 
 import server.MMP;
 import server.game.PlayerMP;
 
 public class Town
 {
 
 	public Street street;
 	private String name;
 	private byte ownerId;
 	private int houseCount;
 	private int price;
 	private int cost;
 	private SpecialTown type;
 
 	public Town(String name, Street street, int price, int cost)
 	{
 		this.cost = cost;
 		this.name = name;
 		this.price = price;
 		this.street = street;
 		houseCount = 0;
 		ownerId = -1;
 		type = SpecialTown.Normaal;
 	}
 
 	public Town(String name, Street street, int price, int cost, SpecialTown special)
 	{
 		this(name, street, price, cost);
 		type = special;
 	}
 
 	public boolean isBuyable()
 	{
 		if (ownerId != -1)
 		{
 			if (type == SpecialTown.Normaal || type == SpecialTown.Station || type == SpecialTown.Voorzieningen)
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public int getPrice()
 	{
 		return price;
 	}
 
 	public int getCost()
 	{
 		return cost + ((((cost * 10) - cost) / 5) * houseCount);
 	}
 
 	public String getOwner()
 	{
 		return (ownerId == -1) ? "Bank" : getPlayer().Username();
 	}
 
 	public PlayerMP getPlayer()
 	{
 		return MMP.getServer().Monopoly().getPlayer(ownerId);
 	}
 
 	public void setOwner(byte pId)
 	{
 		ownerId = pId;
 	}
 
 	public void removeOwner()
 	{
 		ownerId = -1;
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public SpecialTown getType()
 	{
 		return type;
 	}
 
 }
