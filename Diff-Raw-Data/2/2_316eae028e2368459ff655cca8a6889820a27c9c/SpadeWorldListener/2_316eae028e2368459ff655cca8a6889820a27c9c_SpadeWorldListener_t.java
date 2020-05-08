 package net.nexisonline.spade;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import net.nexisonline.spade.populators.DungeonPopulator;
 
 import org.bukkit.World;
 import org.bukkit.event.world.ChunkLoadEvent;
 import org.bukkit.event.world.WorldListener;
 import org.bukkit.event.world.WorldLoadEvent;
 import org.bukkit.event.world.WorldSaveEvent;
 import org.bukkit.generator.BlockPopulator;
 import org.yaml.snakeyaml.DumperOptions;
 import org.yaml.snakeyaml.Yaml;
 import org.yaml.snakeyaml.reader.UnicodeReader;
 
 public class SpadeWorldListener extends WorldListener {
     private SpadePlugin  spade;
     private Collection<String> worlds;
     private Map<String, Object> root;
     private Yaml yaml;
     
     public SpadeWorldListener(SpadePlugin plugin) {
         spade = plugin;
     }
     
     @Override
     public void onChunkLoad(ChunkLoadEvent e) {
         for (BlockPopulator bp : e.getWorld().getPopulators()) {
             if (bp instanceof DungeonPopulator) {
                 ((DungeonPopulator) bp).onChunkLoad(e.getChunk().getX(), e.getChunk().getZ());
             }
         }
     }
     
     @Override
     public void onWorldLoad(WorldLoadEvent e) {
     }
     
     @Override
     public void onWorldSave(WorldSaveEvent e) {
         saveWorlds();
     }
     
     @SuppressWarnings("unchecked")
     private void load() {
         DumperOptions options = new DumperOptions();
         
         options.setIndent(4);
         options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
         yaml = new Yaml(options);
         
         FileInputStream stream = null;
         
         File file = new File(spade.getDataFolder(), "Spade.yml");
         if(!file.exists())
             return;
         
         try {
             stream = new FileInputStream(file);
             root=(Map<String,Object>)yaml.load(new UnicodeReader(stream));
         } catch (IOException e) {
             root = new HashMap<String, Object>();
         } finally {
             try {
                 if (stream != null) {
                     stream.close();
                 }
             } catch (IOException e) {}
         }
     }
     
     @SuppressWarnings("unchecked")
     public void loadWorlds() {
         load();
         Object co = null;
         if(root!=null && root.containsKey("worlds")) {
             co = root.get("worlds");
             if(co instanceof Map<?,?>) {
                 Map<String,Object> worldMap = (Map<String, Object>) root.get("worlds");
                 worlds = worldMap.keySet();
                 for (String worldName : this.worlds) {
                     Map<String,Object> currWorld = (Map<String, Object>) worldMap.get(worldName);
                     Map<String,Object> limits = (Map<String, Object>) currWorld.get("limits");
                     Long seed = (Long) ((currWorld.get("seed")==null)?((new Random()).nextLong()):currWorld.get("seed"));
                     spade.genLimits.put(worldName.toLowerCase(), new GenerationLimits(limits));
                     
                     Map<String,Object> chunkManager = (Map<String, Object>) currWorld.get("chunk-manager");
                     if(chunkManager == null) {
                         chunkManager = new HashMap<String,Object>();
                         chunkManager.put("name", "stock");
                     }
                     Map<String,Object> chunkProvider = (Map<String, Object>) currWorld.get("chunk-provider");
                     if(chunkProvider == null) {
                         chunkProvider = new HashMap<String,Object>();
                         chunkProvider.put("name", "stock");
                     }
                     spade.loadWorld(worldName, seed, (String)chunkManager.get("name"), (String)chunkProvider.get("name"), (Map<String,Object>)chunkProvider.get("config"));
                 }
             }
         } else {
             for (World w : spade.getServer().getWorlds()) {
                 String worldName = w.getName();
                Map<String,Object> world = root = new HashMap<String,Object>();
                 {
                     Map<String,Object> chunkProvider = new HashMap<String,Object>();
                     chunkProvider.put("name", "stock");
                     chunkProvider.put("config", null);
                     world.put("chunk-provider",chunkProvider);
                 }
                 {
                     world.put("limits", (new GenerationLimits()).getConfig());
                 }
                 root.put(worldName, world);
             }
             save();
         }
     }
     
     private void save() {
         FileOutputStream stream = null;
         File file = new File(spade.getDataFolder(), "Spade.yml");
         
         File parent = file.getParentFile();
         
         if (parent != null) {
             parent.mkdirs();
         }
         
         try {
             stream = new FileOutputStream(file);
             OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
             writer.append("\r\n# Spade Terrain Generator Plugin");
             writer.append("\r\n#   Configuration File");
             writer.append("\r\n# ");
             writer.append("\r\n# AUTOMATICALLY GENERATED");
             writer.append("\r\n");
             
             yaml.dump(root, writer);
             return;
         } catch (IOException e) {} finally {
             try {
                 if (stream != null) {
                     stream.close();
                 }
             } catch (IOException e) {}
         }
         
         return;
     }
     
     public void saveWorlds() {
         
         for (World w : spade.getServer().getWorlds()) {
             String worldName = w.getName();
             Map<String,Object> world = new HashMap<String,Object>();
             {
                     Map<String,Object> chunkProvider = new HashMap<String,Object>();
                 if (w.getGenerator() instanceof SpadeChunkProvider) {
                     SpadeChunkProvider cp = (SpadeChunkProvider) w.getGenerator();
                     chunkProvider.put("name", spade.getNameForClass(cp));
                     chunkProvider.put("config", cp.getConfig());
                 } else {
                     chunkProvider.put("name", "stock");
                 }
                 world.put("chunk-provider",chunkProvider);
             }
             {
                 spade.genLimits.get(worldName.toLowerCase());
                 world.put("limits", (new GenerationLimits()).getConfig());
             }
             root.put(worldName, world);
         }
         save();
     }
 }
