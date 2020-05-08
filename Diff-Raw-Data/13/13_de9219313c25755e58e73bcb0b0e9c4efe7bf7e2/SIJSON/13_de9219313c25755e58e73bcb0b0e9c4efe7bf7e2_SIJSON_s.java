 package net.loadingchunks.plugins.SheepInfo;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.util.concurrent.Executor;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Boat;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.Egg;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.FallingSand;
 import org.bukkit.entity.Ghast;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Minecart;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.PoweredMinecart;
 import org.bukkit.entity.Slime;
 import org.bukkit.entity.Snowball;
 import org.bukkit.entity.Squid;
 import org.bukkit.entity.StorageMinecart;
 import org.bukkit.entity.Wolf;
 import org.bukkit.entity.Zombie;
 import org.bukkit.entity.TNTPrimed;
 import org.bukkit.inventory.ItemStack;
 import org.json.simple.*;
 import com.nijiko.coelho.iConomy.iConomy;
 
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.HttpHandler;
 import com.sun.net.httpserver.HttpServer;
 
 import net.loadingchunks.plugins.SheepInfo.SheepInfo;
 
 
 public class SIJSON {
     private final SheepInfo plugin;
 
     public SIJSON (SheepInfo plugin) {
         this.plugin = plugin;
     }
     
     public JSONObject Info(World w)
     {
     	JSONObject object = new JSONObject();
     	object.put("name", w.getName());
     	object.put("players", new Integer(w.getPlayers().size()));
     	object.put("chunks", new Integer(w.getLoadedChunks().length));
     	object.put("entities", new Integer(w.getLivingEntities().size()));
     	object.put("max_mem", new Long( Runtime.getRuntime().maxMemory()));
     	object.put("free_mem", new Long(Runtime.getRuntime().freeMemory()));
     	object.put("time", new Long(w.getTime()));
     	return object;
     }
     
     public JSONArray Players(World w, Boolean inventory)
     {
     	JSONArray object = new JSONArray();
     	JSONObject player;
     	
     	for ( Player p : w.getPlayers())
     	{
     		player = new JSONObject();
     		player.put("name", p.getName());
     		player.put("nickname", ChatColor.stripColor(p.getDisplayName()));
     		//if(this.plugin.siConfig.get("keys_ip").equalsIgnoreCase("yes"))
     			player.put("ip", p.getAddress().getHostName());
     		player.put("x", (double)p.getLocation().getX());
     		player.put("y", (double)p.getLocation().getY());
     		player.put("z", (double)p.getLocation().getZ());
     		player.put("holding", (int)p.getItemInHand().getTypeId());
     		player.put("health", (int)p.getHealth());
     		player.put("air", (int)p.getRemainingAir());
     		player.put("dead", (boolean)p.isDead());
    		if(this.plugin.iConomy != null)
     		{
     			System.out.print("Moneez: " + this.plugin.iConomy.getBank().getAccount(p.getName()).getBalance());
     			player.put("money", this.plugin.iConomy.getBank().getAccount(p.getName()).getBalance());
    		}
     		
     		if(inventory)
     		{
     			player.put("inventory", this.Inventories(p));
     		}
     		
     		object.add(player);
     	}
     	return object;
     }
     
     public JSONArray Inventories(Player p)
     {
     	JSONArray inventory = new JSONArray();
     	JSONObject item;
     	
     	ItemStack[] list = p.getInventory().getContents();
     	
     	for(int j = 0; j < list.length; j++)
    		{
     		if(list[j] != null)
     		{
     			item = new JSONObject();
     			if(list[j].getTypeId() > 0)
     			{
     				item.put("id", (int)list[j].getTypeId());
     				item.put("amount", (int)list[j].getAmount());
     				item.put("durability", (int)list[j].getDurability());
     				item.put("slot", (int)j);
     				inventory.add(item);
     			}
     		}
     	}
     	return inventory;
     }
     
     public JSONObject Entities(World w)
     {
     	JSONObject object = new JSONObject();
     	int creepers = 0,minecarts = 0,boats = 0,zombies = 0,pickups = 0,slimes = 0,arrows = 0,projectiles = 0,active_tnt = 0,falling = 0,squids = 0,wolves = 0,ghasts = 0;
    	
     	for(Entity e : w.getEntities())
     	{
     		if(e instanceof Creeper)
     			creepers++;
     		else if(e instanceof Minecart || e instanceof PoweredMinecart || e instanceof StorageMinecart)
     			minecarts++;
     		else if(e instanceof Boat)
     			boats++;
     		else if(e instanceof Zombie)
     			zombies++;
     		else if(e instanceof Item)
     			pickups++;
     		else if(e instanceof Slime)
     			slimes++;
     		else if(e instanceof Arrow)
     			arrows++;
     		else if(e instanceof Snowball || e instanceof Egg)
     			projectiles++;
     		else if(e instanceof TNTPrimed)
     			active_tnt++;
     		else if(e instanceof FallingSand || e instanceof FallingSand)
     			falling++;
     		else if(e instanceof Squid)
     			squids++;
     		else if(e instanceof Wolf)
     			wolves++;
     		else if(e instanceof Ghast)
     			ghasts++;
     		
     	}
     	
     	object.put("creepers", creepers);
     	object.put("minecarts", minecarts);
     	object.put("boats", boats);
     	object.put("zombies", zombies);
     	object.put("pickups", pickups);
     	object.put("slimes", slimes);
     	object.put("arrows", arrows);
     	object.put("projectiles", projectiles);
     	object.put("active_tnt", active_tnt);
     	object.put("falling", falling);
     	object.put("squid", squids);
     	object.put("wolves", wolves);
     	object.put("ghasts", ghasts);
     	return object;
     }
     
 }
