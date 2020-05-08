 package us.Myles.DP;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Minecart;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Plug extends JavaPlugin implements Listener{
 	public void onEnable(){
 		Bukkit.getPluginManager().registerEvents(this, this);
 	}
 	@EventHandler
 	public void onOpen(InventoryOpenEvent e){
		System.out.println(e.getInventory().getHolder().getClass());
 		if(e.getPlayer().isInsideVehicle() && e.getInventory().getHolder() instanceof Minecart){
 			((Player) e.getPlayer()).sendMessage(ChatColor.RED + "You may not open Minecart Inventories while in a vehicle.");
 			System.out.println("[Alert] " + e.getPlayer().getName() + " just tried to use a duplication glitch.");
 			e.setCancelled(true);
 		}
 	}
 }
