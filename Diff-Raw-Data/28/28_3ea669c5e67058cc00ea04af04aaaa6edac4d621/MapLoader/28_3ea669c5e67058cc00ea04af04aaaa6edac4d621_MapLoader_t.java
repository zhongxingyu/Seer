 package jk_5.nailed.map;
 
 import com.google.common.collect.Lists;
 import cpw.mods.fml.common.FMLCommonHandler;
 import jk_5.nailed.NailedLog;
 import jk_5.nailed.api.IMappackRegistrar;
 import jk_5.nailed.event.PlayerChangedDimensionEvent;
 import jk_5.nailed.map.event.MapCreatedEvent;
 import jk_5.nailed.map.event.MapRemovedEvent;
 import jk_5.nailed.map.mappack.DirectoryMappack;
 import jk_5.nailed.map.mappack.Mappack;
 import jk_5.nailed.map.mappack.ZipMappack;
 import lombok.Getter;
 import net.minecraft.world.World;
 import net.minecraftforge.common.DimensionManager;
 import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.EventPriority;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.world.WorldEvent;
 
 import java.io.*;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 public class MapLoader implements IMappackRegistrar {
 
     private static final MapLoader INSTANCE = new MapLoader();
     @SuppressWarnings("unused") @Getter private static final File mappackFolder = new File("mappacks");
     @SuppressWarnings("unused") @Getter private static final File mapsFolder = new File("maps");
 
     @Getter private final List<Map> maps = Lists.newArrayList();
     @Getter private final List<Mappack> mappacks = Lists.newArrayList();
 
     @Getter private Map lobby;
 
     public static MapLoader instance(){
         return INSTANCE;
     }
 
     public MapLoader(){
         MinecraftForge.EVENT_BUS.register(this);
     }
 
     public void loadMappacks(){
         NailedLog.info("Loading mappacks...");
         this.mappacks.clear();
         if(!mappackFolder.exists()) mappackFolder.mkdirs();
         File[] list = mappackFolder.listFiles();
         if(list == null) return;
         for(File file : list){
             try{
                 if(file.isFile() && (file.getName().endsWith(".zip") || file.getName().endsWith(".mappack"))){
                     this.mappacks.add(ZipMappack.create(file));
                     NailedLog.info("Successfully loaded mappack " + file.getName());
                 }else if(file.isDirectory()){
                     this.mappacks.add(DirectoryMappack.create(file));
                     NailedLog.info("Successfully loaded mappack " + file.getName());
                 }
             }catch (DiscardedMappackInitializationException e){
                 //Discard!
                 NailedLog.warning("An error was thrown while loading mappack " + file.getName() + ", skipping it!");
             }catch (MappackInitializationException e){
                 NailedLog.severe(e, "Error while loading mappack " + file.getName() + ", skipping it!");
             }
         }
         NailedLog.info("Successfully loaded %d mappacks!", this.mappacks.size());
     }
 
     public File unzipMapFromMapPack(File mapPack, File destDir){
         try{
             ZipFile zipFile = new ZipFile(mapPack);
             Enumeration e = zipFile.entries();
             File worldDir = null;
             while(e.hasMoreElements()){
                 ZipEntry entry = (ZipEntry)e.nextElement();
                 if(entry.getName().equals("mappack.cfg")) continue;
                 if(entry.getName().equals("gameinstructions.cfg")) continue;
                 if(entry.getName().contains("##MCEDIT.TEMP##")) continue;
                 if(entry.getName().startsWith("__MACOSX/")) continue;
                 File destinationFilePath = new File(destDir.getParentFile(), entry.getName());
                 destinationFilePath.getParentFile().mkdirs();
                 if(!entry.isDirectory()){
                     BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
 
                     int b;
                     byte buffer[] = new byte[1024];
                     FileOutputStream fos = new FileOutputStream(destinationFilePath);
                     BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);
                     while ((b = bis.read(buffer, 0, 1024)) != -1) {
                         bos.write(buffer, 0, b);
                     }
                     bos.flush();
                     bos.close();
                     bis.close();
                 }else if(entry.getName().equals("world/")){
                     worldDir = destinationFilePath;
                 }
             }
             if(worldDir == null){
                 System.err.println("Invalid or corrupt mappack file");
                 System.exit(1);
             }
             worldDir.renameTo(destDir);
             return destDir;
         }catch(IOException ioe){
             NailedLog.severe(ioe, "Error while unpacking file");
         }
         return null;
     }
 
     public Mappack getMappack(String mappackID){
         for(Mappack pack : this.mappacks){
             if(pack.getMappackID().equals(mappackID)){
                 return pack;
             }
         }
         return null;
     }
 
     public void addMap(Map map){
         if(map.getID() == 0) this.lobby = map;
         this.maps.add(map);
         NailedLog.info("Registered " + map.getSaveFileName());
     }
 
     public Map newMapServerFor(Mappack pack){
         PotentialMap potentialMap = new PotentialMap(pack);
         pack.prepareWorld(potentialMap.getSaveFolder());
         Map map = pack.createMap(potentialMap);
         map.initMapServer();
         MinecraftForge.EVENT_BUS.post(new MapCreatedEvent(map));
         return map;
     }
 
     public Map getMapFromName(String name){
         for(Map map : this.maps){
             if(map.getSaveFileName().equalsIgnoreCase(name)){
                 return map;
             }
         }
         return null;
     }
 
     public Map getMap(int id){
         for(Map map : this.maps){
             if(map.getID() == id){
                 return map;
             }
         }
         if(DimensionManager.isDimensionRegistered(id)){
             return new WrappedMap(id);
         }else{
             return null;
         }
     }
 
     public Map getMap(World world){
         if(world == null) return null;
         return this.getMap(world.provider.dimensionId);
     }
 
    @ForgeSubscribe(priority = EventPriority.HIGH)
     @SuppressWarnings("unused")
     public void onWorldLoad(WorldEvent.Load event){
         if(FMLCommonHandler.instance().getEffectiveSide().isClient()) return;
         Map map = this.getMap(event.world);
         if(map != null) map.setWorld(event.world);
     }
 
     @ForgeSubscribe
     @SuppressWarnings("unused")
     public void onChangeDimension(PlayerChangedDimensionEvent event){
         if(FMLCommonHandler.instance().getEffectiveSide().isClient()) return;
         event.oldMap.onPlayerLeft(event.player);
         event.newMap.onPlayerJoined(event.player);
     }
 
     @Override
     public void registerMappack(Mappack mappack) {
         this.mappacks.add(mappack);
     }
 
     public void removeMap(Map map){
         DimensionManager.unloadWorld(map.getID());
         this.maps.remove(map);
         MinecraftForge.EVENT_BUS.post(new MapRemovedEvent(map));
         NailedLog.info("Unloaded map " + map.getSaveFileName());
     }
 }
