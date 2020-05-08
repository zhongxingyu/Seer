 package de.craftlancer.recycler;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.inventory.FurnaceRecipe;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import de.craftlancer.recycler.metrics.Metrics;
 
 public class Recycler extends JavaPlugin
 {
     private RecyclerListener listener;
     private HashMap<Integer, Recycleable> map = new HashMap<Integer, Recycleable>();
     private FileConfiguration config;
     protected boolean preventHoppers = true;
     
     @Override
     public void onEnable()
     {
         listener = new RecyclerListener(this);
         loadMap();
         getServer().getPluginManager().registerEvents(listener, this);
         
         try
         {
             Metrics metrics = new Metrics(this);
             metrics.start();
         }
         catch (IOException e)
         {
         }
     }
     
     @Override
     public void onDisable()
     {
         map.clear();
         config = null;
     }
     
     private void loadMap()
     {
         reloadConfig();
         config = getConfig();
         map.clear();
         
         preventHoppers = config.getBoolean("disableHopper", true);
         
         for (String key : config.getKeys(false))
             if (!key.equals("disableHopper"))
                 map.put(config.getInt(key + ".id", 0), new Recycleable(config.getInt(key + ".id", 0), config.getInt(key + ".rewardid", 0), config.getInt(key + ".rewardamount", 0), config.getInt(key + ".maxdura", 0), config.getInt(key + ".extradura", 0), config.getBoolean(key + ".calcdura", true)));
         
         getLogger().info(map.size() + " recycleables loaded.");
         
         for (Recycleable rec : map.values())
             getServer().addRecipe(new FurnaceRecipe(new ItemStack(rec.rewardid), Material.getMaterial(rec.id)));
     }
     
     public HashMap<Integer, Recycleable> getRecyleMap()
     {
         return map;
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
     {
        if (sender.hasPermission("recycler.admin"))
             loadMap();
         
         return true;
     }
 }
