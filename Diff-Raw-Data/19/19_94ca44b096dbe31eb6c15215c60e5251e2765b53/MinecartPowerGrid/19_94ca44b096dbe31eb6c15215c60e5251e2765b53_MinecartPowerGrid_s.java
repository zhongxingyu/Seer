 package me.Kruithne.MinecartsMod;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
import no.runsafe.framework.IScheduler;
 
 import org.bukkit.Server;
 import org.bukkit.entity.PoweredMinecart;
 
 public class MinecartPowerGrid {
 
 	IScheduler timedEventHandler;
 	HashMap<Integer, Integer> carts;
 	Server server;
 	
 	public MinecartPowerGrid(IScheduler timedEventHandler, Server server)
 	{
 		this.timedEventHandler = timedEventHandler;
 		this.carts = new HashMap<Integer, Integer>();
 		this.server = server;
 		this.registerNewCheck();
 	}
 	
 	private void registerNewCheck()
 	{
 		this.timedEventHandler.setTimedEvent(new Runnable() {
 			public void run() {
 				checkFuels();
 			}
 		}, 1L);
 	}
 	
 	public void addFuel(PoweredMinecart theCart, int fuel)
 	{
 		int cartID = theCart.getEntityId();
 		if (this.carts.containsKey(cartID))
 			this.carts.put(cartID, this.carts.get(cartID) + fuel);
 		else
 			this.carts.put(cartID, fuel);
 	}
 	
 	public int getCartFuel(PoweredMinecart checkCart)
 	{
 		int cartID = checkCart.getEntityId();
 		if (this.carts.containsKey(cartID))
 			return this.carts.get(cartID);
 		return -1;
 	}
 	
 	public void checkFuels()
 	{
 		Iterator<Map.Entry<Integer, Integer>> cartIterator = this.carts.entrySet().iterator();
 		while (cartIterator.hasNext())
 		{
 			Map.Entry<Integer, Integer> cart = (Map.Entry<Integer, Integer>) cartIterator.next();
 			
 			if (cart.getValue() < 0)
 			{
 				this.carts.remove(cart.getKey());
 			}
 			else
 			{
 				this.carts.put(cart.getKey(), this.carts.get(cart.getKey()) - 1);
 			}
 		}
 		this.registerNewCheck();
 	}
 	
 }
