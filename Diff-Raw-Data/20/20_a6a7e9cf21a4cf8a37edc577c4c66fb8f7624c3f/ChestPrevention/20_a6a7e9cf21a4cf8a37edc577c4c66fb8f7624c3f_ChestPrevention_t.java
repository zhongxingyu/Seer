 package de.codeinfection.quickwango.AntiGuest.Preventions.Bukkit;
 
 import de.codeinfection.quickwango.AntiGuest.AntiGuestBukkit;
 import de.codeinfection.quickwango.AntiGuest.Prevention;
import org.bukkit.Material;
 import org.bukkit.block.Chest;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
 import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.DoubleChestInventory;
 
 /**
  * Prevents chest access
  *
  * @author Phillip Schichtel
  */
 public class ChestPrevention extends Prevention
 {
     public ChestPrevention()
     {
         super("chest", AntiGuestBukkit.getInstance());
     }
 
     @Override
     public ConfigurationSection getDefaultConfig()
     {
         ConfigurationSection config = super.getDefaultConfig();
 
         config.set("message", "&4You are not allowed to use chests!");
 
         return config;
     }
 
     @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void handle(PlayerInteractEvent event)
    {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.CHEST)
        {
            if (prevent(event, event.getPlayer()))
            {
                event.setUseInteractedBlock(Result.DENY);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
     public void handle(InventoryOpenEvent event)
     {
         if (event.getInventory() != null)
         {
             if (event.getInventory() instanceof DoubleChestInventory || event.getInventory().getHolder() instanceof Chest)
             {
                 if (event.getPlayer() instanceof Player)
                 {
                     prevent(event, (Player)event.getPlayer());
                 }
             }
         }
     }
 }
