 package me.exphc.BugTest1;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.UUID;
 import java.util.Iterator;
 import java.util.logging.Logger;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.Formatter;
 import java.lang.Byte;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.io.*;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.*;
 import org.bukkit.event.*;
 import org.bukkit.event.block.*;
 import org.bukkit.event.player.*;
 import org.bukkit.event.entity.*;
 import org.bukkit.Material.*;
 import org.bukkit.material.*;
 import org.bukkit.block.*;
 import org.bukkit.entity.*;
 import org.bukkit.command.*;
 import org.bukkit.inventory.*;
 import org.bukkit.configuration.*;
 import org.bukkit.configuration.file.*;
 import org.bukkit.scheduler.*;
 import org.bukkit.enchantments.*;
 import org.bukkit.*;
 
 import net.minecraft.server.CraftingManager;
 
public class BugTest1 extends JavaPlugin {
     Logger log = Logger.getLogger("Minecraft");
 
     final int ID_WOOD_FLOUR = 1006;     // PlasticCraft
     final int ID_PLANK = 5;             // vanilla, but I better name than Bukkit's Material
     final int ID_CLAY_BLOCK = 82;       // vanilla
     final int ID_DISH = 3704;           // Mo' Food and Crops
 
     public void onEnable() {
         /* TODO
         // PlasticCraft and Mo' Food and Crops recipe conflict for wooden plank -> wood flour / dish
         // Override wooden plank -> wood flour (TODO), and add clay block -> dish 
         // https://github.com/mushroomhostage/exphc/issues/2
         ShapelessRecipe dishRecipe = new ShapelessRecipe(new ItemStack(ID_DISH, 2));
         dishRecipe.addIngredient(Material.getMaterial(ID_CLAY_BLOCK));
 
         // TODO: this doesn't work, how can we fix?? old recipe still exists
         ShapelessRecipe flourRecipe = new ShapelessRecipe(new ItemStack(ID_WOOD_FLOUR, 1));
         flourRecipe.addIngredient(Material.getMaterial(ID_PLANK));
 
         Bukkit.addRecipe(dishRecipe);
         Bukkit.addRecipe(flourRecipe);
         */
     }
 
     public void onDisable() {
     }
 
     public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
         if (!cmd.getName().equalsIgnoreCase("chunk")) {
             return false;
         }
         if (!(sender instanceof Player)) {
             sender.sendMessage("Must be sent by player");
             return true;
         }
         Player player = (Player)sender;
 
         // See Bananachunk http://forums.bukkit.org/threads/fix-mech-bananachunk-v4-6-stuck-in-a-lag-hole-request-a-chunk-resend-1060.19232/page-7
         World world = player.getWorld();
         Chunk chunk = world.getChunkAt(player.getLocation());
         world.refreshChunk(chunk.getX(), chunk.getZ());
 
         sender.sendMessage("Chunk resent");
 
         return true;
     }
 }
