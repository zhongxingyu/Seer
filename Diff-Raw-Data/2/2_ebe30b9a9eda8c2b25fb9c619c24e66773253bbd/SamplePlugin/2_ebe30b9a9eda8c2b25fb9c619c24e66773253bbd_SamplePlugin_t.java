 
 package agaricus.plugins.IncompatiblePlugin;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.HashMap;
 
 import com.google.common.io.CharStreams;
 import net.minecraft.server.v1_5_R2.*;
 import net.minecraft.v1_5_R2.org.bouncycastle.asn1.bc.BCObjectIdentifiers;
 import org.bukkit.*;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Biome;
 import org.bukkit.block.Block;
 import org.bukkit.craftbukkit.libs.com.google.gson.Gson;
 import org.bukkit.craftbukkit.libs.com.google.gson.GsonBuilder;
 import org.bukkit.craftbukkit.v1_5_R2.CraftChunk;
 import org.bukkit.craftbukkit.v1_5_R2.CraftWorld;
 import org.bukkit.craftbukkit.v1_5_R2.inventory.RecipeIterator;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.FurnaceRecipe;
 import org.bukkit.inventory.Recipe;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.inventory.ShapelessRecipe;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import sun.misc.IOUtils;
 
 import java.io.*;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 /**
  * Sample plugin for Bukkit
  *
  * @author Dinnerbone
  */
 public class SamplePlugin extends JavaPlugin {
     private final SamplePlayerListener playerListener = new SamplePlayerListener(this);
     private final SampleBlockListener blockListener = new SampleBlockListener();
     private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
 
     @Override
     public void onDisable() {
         // TODO: Place any custom disable code here
 
         // NOTE: All registered events are automatically unregistered when a plugin is disabled
 
         // EXAMPLE: Custom code, here we just output some info so we can check all is well
         getLogger().info("Goodbye world!");
     }
 
     @Override
     public void onEnable() {
         // TODO: Place any custom enable code here including the registration of any events
 
         System.out.println("IncompatiblePlugin");
 
         // show enums for https://github.com/MinecraftPortCentral/MCPC-Plus/issues/417
         StringBuffer sb = new StringBuffer();
         for (Biome biome : Biome.values()) {
             sb.append(biome.ordinal()+"="+biome.toString()+"("+biome.name()+") ");
         }
         System.out.println("Biome ("+Biome.values().length+"): " + sb.toString());
 
         sb = new StringBuffer();
         for (EntityType entityType : EntityType.values()) {
             sb.append(entityType.ordinal()+"="+entityType.toString()+"("+entityType.name()+") ");
         }
         System.out.println("EntityType ("+EntityType.values().length+"): " + sb.toString());
 
         // demonstrate https://github.com/MinecraftPortCentral/MCPC-Plus/issues/75
         try {
             System.out.println("codeSource URI="+getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
             System.out.println(" file = ="+new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()));
             System.out.println("new canonical file = ="+(new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI())).getCanonicalFile());
         } catch (Throwable t) {
             System.out.println("codeSource URI exception="+t);
             t.printStackTrace();
         }
 
         // from sqlite NestedDB.java:63 _open https://github.com/MinecraftPortCentral/MCPC-Plus/issues/218
         // make sure we don't break SQLite by ASM choking on its excessive classfile size
         System.out.println("forName SQLite");
         try {
             Object sqlite = Class.forName("org.sqlite.SQLite").newInstance();
             System.out.println("org.sqlite.SQLite newInstance="+sqlite);
         } catch (Throwable t) {
             System.out.println("t="+t);
             t.printStackTrace();
         }
 
         // recipe sorting error - java.lang.IllegalArgumentException: Comparison method violates its general contract!
         // https://github.com/MinecraftPortCentral/MCPC-Plus/issues/238
         ShapelessRecipe shapelessRecipe = new ShapelessRecipe(new org.bukkit.inventory.ItemStack(Material.DIAMOND));
         shapelessRecipe.addIngredient(Material.DIRT);
         org.bukkit.Bukkit.addRecipe(shapelessRecipe);
 
         // reflection remapping https://github.com/MinecraftPortCentral/MCPC-Plus/issues/13
         try {
            Field field = TileEntityChest.class.getDeclaredField("items"); // MCP csv chestContents, srg field_70428_i, obf i
             System.out.println("field="+field);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
 
         // null EntityType test - Bukkit wrappers for mobs https://github.com/MinecraftPortCentral/MCPC-Plus/issues/16
         // see https://github.com/xGhOsTkiLLeRx/SilkSpawners/blob/master/src/main/java/de/dustplanet/silkspawners/SilkSpawners.java#L157
         try {
             // https://github.com/Bukkit/mc-dev/blob/master/net/minecraft/server/EntityTypes.java#L21
             // f.put(s, Integer.valueOf(i)); --> Name of ID
             Field field = EntityTypes.class.getDeclaredField("f");
             field.setAccessible(true);
             Map<String, Integer> map = (Map<String, Integer>) field.get(null);
             for (Map.Entry<String,Integer> entry: map.entrySet()) {
                 String mobID = entry.getKey();
                 int entityID = entry.getValue();
                 EntityType bukkitEntityType = EntityType.fromId(entityID);
 
                 if (bukkitEntityType == null) {
                     System.out.println("Missing Bukkit EntityType for entityID="+entityID+", mobID="+mobID);
                 } else {
                     Class bukkitEntityClass = bukkitEntityType.getEntityClass();
                     if (bukkitEntityClass == null) {
                         System.out.println("Missing Bukkit getEntityClass() for entityID="+entityID+", mobID="+mobID+", bukkitEntityType="+bukkitEntityType);
                     }
                 }
             }
         } catch (Exception e) {
             Bukkit.getServer().getLogger().severe("Failed to dump entity map: " + e);
             e.printStackTrace();
         }
 
 
         // method naming conflict test https://github.com/MinecraftPortCentral/MCPC-Plus/issues/169
         net.minecraft.server.v1_5_R2.PlayerConnection playerConnection = null;
         try {
             System.out.println("getPlayer = "+playerConnection.getPlayer());
         } catch (NoSuchMethodError ex) {
             System.out.println("failed to call playerConnection.getPlayer()");
             ex.printStackTrace();
         } catch (NullPointerException ex) {
             System.out.println("playerConnection.getPlayer successful");
         }
 
 
         // null Material test https://github.com/MinecraftPortCentral/MCPC-Plus/issues/172
         for (int i = 0; i < Item.byId.length; i++) {
             Item nmsItem = Item.byId[i];
             net.minecraft.server.v1_5_R2.Block nmsBlock = i < 4096 ? net.minecraft.server.v1_5_R2.Block.byId[i] : null;
             org.bukkit.Material bukkitMaterial = org.bukkit.Material.getMaterial(i);
 
             if (nmsItem == null && nmsBlock == null && bukkitMaterial == null) continue; // must not exist
 
             if (bukkitMaterial == null)
             System.out.println("Item "+i+" = item="+nmsItem+", block="+nmsBlock+", bukkit Material="+bukkitMaterial);
         }
 
         // null recipe output test for https://github.com/MinecraftPortCentral/MCPC-Plus/issues/139
         System.out.println("recipe iterator..");
         RecipeIterator recipeIterator = new RecipeIterator();
         int nulls = 0, nonVanillaRecipes = 0;
         while(recipeIterator.hasNext()) {
             Recipe recipe = recipeIterator.next();
             if (recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe || recipe instanceof FurnaceRecipe) continue; // skip vanilla
             if (recipe == null) {
                 nulls += 1;
             }
             nonVanillaRecipes += 1;
         }
         System.out.println("null recipes? " + nulls + ", non-vanilla=" + nonVanillaRecipes);
 
 
         // test un-renamed map
         System.out.println("net.minecraft.server.v1_5_R2.MinecraftServer.currentTick = "+MinecraftServer.currentTick);
 
         // test bouncycastle is available
         System.out.println("bouncycastle="+net.minecraft.v1_5_R2.org.bouncycastle.asn1.bc.BCObjectIdentifiers.class);
 
 
 
         System.out.println("SNOW.id="+net.minecraft.server.v1_5_R2.Block.SNOW.id);
 
    
         // test tasks
         Block b = Bukkit.getServer().getWorlds().get(0).getBlockAt(0, 100, 0);
         System.out.println("a="+((CraftChunk)b.getChunk()).getHandle().a(b.getX() & 15, b.getY(), b.getZ() & 15, 48, 0));
 
         Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
             public void run() {
                 Block b = Bukkit.getServer().getWorlds().get(0).getBlockAt(0, 99, 0);
                 System.out.println("a(task)="+((CraftChunk)b.getChunk()).getHandle().a(b.getX() & 15, b.getY(), b.getZ() & 15, 48, 0));
             }
         }, 0);
 
         // test nms inheritance remapping
         net.minecraft.server.v1_5_R2.WorldServer worldServer = ((CraftWorld)Bukkit.getServer().getWorlds().get(0)).getHandle();
         System.out.println("calling getTileEntity on nms World");
         // if this breaks - Caused by: java.lang.NoSuchMethodError: in.getTileEntity(III)Lany;
         // because WorldServer inherits from World, but isn't in mc-dev to obf mappings (since is added by CB)
         worldServer.getTileEntity(0, 0, 0);
         System.out.println("nms inheritance successful");
 
         // test plugin inheritance remapping
         getLogger().info("creating class inheriting from NMS...");
         IInventory iInventory = new SamplePluginNMSInheritor();
         getLogger().info("iInventory= "+iInventory);
         // if subclass/implementator not remapped: java.lang.AbstractMethodError: SamplePluginNMSInheritor.k_()I
         getLogger().info("getSize="+iInventory.getSize());
 
         // Register our events
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvents(playerListener, this);
         pm.registerEvents(blockListener, this);
 
         // Register our commands
         getCommand("pos").setExecutor(new SamplePosCommand());
         getCommand("debug").setExecutor(new SampleDebugCommand(this));
 
         // EXAMPLE: Custom code, here we just output some info so we can check all is well
         PluginDescriptionFile pdfFile = this.getDescription();
         getLogger().info( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
     }
 
     public boolean isDebugging(final Player player) {
         if (debugees.containsKey(player)) {
             return debugees.get(player);
         } else {
             return false;
         }
     }
 
     public void setDebugging(final Player player, final boolean value) {
         debugees.put(player, value);
     }
 }
