 package com.archmageinc.RandomEncounters;
 
 import com.sk89q.worldedit.CuboidClipboard;
 import com.sk89q.worldedit.EditSession;
 import com.sk89q.worldedit.MaxChangedBlocksException;
 import com.sk89q.worldedit.Vector;
 import com.sk89q.worldedit.bukkit.BukkitWorld;
 import com.sk89q.worldedit.data.DataException;
 import com.sk89q.worldedit.schematic.SchematicFormat;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Chest;
 import org.bukkit.inventory.ItemStack;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 /**
  * Represents a Structure to be placed in the world.
  * 
  * All hooks to the WorldEdit API should remain within this file.
  * 
  * 
  * @author ArchmageInc
  * @see com.sk89q.worldedit
  */
 public class Structure {
     
     /**
      * The unique name of the structure configuration.
      */
     protected String name;
     
     /**
      * This file name including directories of the schematic file.
      */
     protected String fileName;
     
     /**
      * The minimum spawn height of the structure.
      */
     protected Long minY;
     
     /**
      * The maximum spawn height of the structure.
      */
     protected Long maxY;
     
     /**
      * Has the structure been successfully loaded from the file system.
      */
     protected Boolean loaded                        =   false;
     
     /**
      * The set of materials the structure is allowed to overwrite.
      */
     protected HashSet<Material> trump               =   new HashSet();
     
     /**
      * The set of materials the structure is not allowed to stand on.
      */
     protected HashSet<Material> invalid             =   new HashSet();
     
     /** 
      * The singleton instances of structure configurations.
      */
     protected static HashSet<Structure> instances   =   new HashSet();
     
     /**
      * The WorldEdit session which keeps track of changes.
      */
     protected EditSession session;
     
     /**
      * The WorldEdit cuboid.
      */
     protected CuboidClipboard cuboid;
     
     /**
      * Get an instance of the Structure based on the name.
      * 
      * @param name The name of the structure configuration
      * @return Returns the Structure if found, null otherwise.
      */
     public static Structure getInstance(String name){
         for(Structure instance : instances){
             if(instance.getName().equals(name)){
                 return instance;
             }
         }
         return null;
     }
     
     /**
      * Get an instance of the Structure based on the JSON configuration
      * 
      * @param jsonConfiguration The JSON configuration
      * @return Returns the Structure based on the configuration 
      */
     public static Structure getInstance(JSONObject jsonConfiguration){
         return Structure.getInstance(jsonConfiguration, false);
     }
     
     /**
      * Get an instance of the Structure based on the JSON configuration with the option to force reload
      * @param jsonConfiguration The JSON configuration
      * @param force Should the structure be forced into reloading
      * @return 
      */
     public static Structure getInstance(JSONObject jsonConfiguration,Boolean force){
         Structure structure =   null;
         String name         =   (String) jsonConfiguration.get("name");
         for(Structure instance : instances){
             if(instance.getName().equalsIgnoreCase(name)){
                 structure   =   instance;
             }
         }
         if(structure==null){
             return new Structure(jsonConfiguration);
         }
         if(force){
             structure.reConfigure(jsonConfiguration);
         }
         return structure;
     }
     
     private void reConfigure(JSONObject jsonConfiguration){
         try{
             instances.remove(this);
             trump.clear();
             invalid.clear();
             loaded                  =   false;
             cuboid                  =   null;
             session                 =   null;
             name                    =   (String) jsonConfiguration.get("name");
             fileName                =   (String) jsonConfiguration.get("file");
             minY                    =   ((Number) jsonConfiguration.get("minY")).longValue();
             maxY                    =   ((Number) jsonConfiguration.get("maxY")).longValue();
             JSONArray jsonTrump     =   (JSONArray) jsonConfiguration.get("trump");
             JSONArray jsonInvalid   =   (JSONArray) jsonConfiguration.get("invalid");
             if(jsonTrump!=null){
                 for(int i=0;i<jsonTrump.size();i++){
                     trump.add(Material.getMaterial((String) jsonTrump.get(i)));
                 }
             }
             if(jsonInvalid!=null){
                 for(int i=0;i<jsonInvalid.size();i++){
                     invalid.add(Material.getMaterial((String) jsonInvalid.get(i)));
                 }
             }
             loaded  =   load();
             if(loaded){
                 instances.add(this);
             }
         }catch(ClassCastException e){
             RandomEncounters.getInstance().logError("Invalid Structure configuration: "+e.getMessage());
         }
     }
     
     /**
      * Constructor for the structure based on the JSON Configuration.
      * 
      * @param jsonConfiguration 
      */
     protected Structure(JSONObject jsonConfiguration){
         reConfigure(jsonConfiguration);
     }
     
     /**
      * Loads the structure from the schematic file on the file system.
      * @return Returns true if success, false otherwise.
      */
     protected final boolean load(){
         try{
             File file           =   new File(RandomEncounters.getInstance().getDataFolder()+"/"+fileName);
             SchematicFormat sf  =   SchematicFormat.getFormat(file);
             if(sf==null){
                 RandomEncounters.getInstance().logError("Unable to detect schematic format for file: "+fileName);
                 return false;
             }
             cuboid         =    sf.load(file);
             
         }catch(IOException e){
             RandomEncounters.getInstance().logError("Unable to load structure "+name+": "+e.getMessage());
             return false;
         } catch (DataException e) {
             RandomEncounters.getInstance().logError("Invalid structure schematic "+fileName+": "+e.getMessage());
             return false;
         }
         return true;
     }
     
     /**
      * Generate a new WorldEdit session for placement.
      * @param world The World where the session is
      * @TODO This is called when placed and flips the structure causing inaccurate width / length measurements.
      */
     private void newSession(World world){
         if(RandomEncounters.getInstance().getLogLevel()>8){
             RandomEncounters.getInstance().logMessage("Generating new WorldEdit session for structure "+name);
         }
         session        =    new EditSession((new BukkitWorld(world)),cuboid.getWidth()*cuboid.getLength()*cuboid.getHeight());
         session.enableQueue();
     }
     
     /**
      * Flip the structure randomly around x and z coordinates only.
      */
     private void flipRandom(){
         int angle   =  (int) Math.round(Math.random()*4)*90;
         if(RandomEncounters.getInstance().getLogLevel()>7){
             RandomEncounters.getInstance().logMessage("Flipping structure "+name+" "+angle+" degrees");
         }
         cuboid.rotate2D(angle);
     }
     
     /**
      * Parce the placed blocks looking for chests and place items in the inventory based on the Encounter's treasure 
      * 
      * @param encounter The encounter configuration.
      * @param location The location of the placed structure. 
      */
     private void placeTreasures(Encounter encounter,Location location){
         int x    =   location.getBlockX();
         int y    =   location.getBlockY();
         int z    =   location.getBlockZ();
         for(int cx=x-cuboid.getWidth();cx<x+cuboid.getWidth();cx++){
             for(int cy=y-cuboid.getHeight();cy<y+cuboid.getHeight();cy++){
                 for(int cz=z-cuboid.getLength();cz<z+cuboid.getLength();cz++){
                     BlockState state        =   location.getWorld().getBlockAt(cx, cy, cz).getState();
                     if(state instanceof Chest){
                         List<ItemStack> items   =   encounter.getTreasure();
                         Chest chest             =   (Chest) state;
                         for(ItemStack item : items){
                             chest.getInventory().addItem(item);
                         }
                     }
                 }
             }
         }
     }
     
     /**
      * Place the structure for a given encounter at a location.
      * 
      * Does not check if it is safe, just places it. The location comes from a block in the world.
      * The Origin of the cuboid lines up with this some how. I have no idea what I was doing here,
      * but it seemed to work so.... yeah.
      * 
      * @param encounter The encounter configuration for this structure.
      * @param location The location to place the structure.
      */
     public void place(Encounter encounter,Location location){
         if(!loaded){
             RandomEncounters.getInstance().logWarning("Attempted to place a non-loaded structure: "+name);
             return;
         }
         newSession(location.getWorld());
         try{
             Vector v    =   new Vector(location.getX(),location.getY(),location.getZ());
             cuboid.setOffset(new Vector(-Math.ceil(cuboid.getWidth()/2),0,-Math.ceil(cuboid.getLength()/2)));
             cuboid.paste(session, v, false);
             if(RandomEncounters.getInstance().getLogLevel()>5){
                 RandomEncounters.getInstance().logMessage("Placed structure "+name+": "+session.size());
             }
             session.flushQueue();
             placeTreasures(encounter,location);
            flipRandom();
         }catch(MaxChangedBlocksException e){
             RandomEncounters.getInstance().logWarning("Unable to place structure: Maximum number of blocks changed: "+e.getMessage());
         }
     }
     
     /**
      * Get the width of the structure
      * @return 
      * @TODO Structure width is not always accurate as when placed the structure is randomly flipped.
      */
     public int getWidth(){
         return loaded ? cuboid.getWidth() : 0;
     }
     
     /**
      * Get the height of the structure
      * @return 
      */
     public int getHeight(){
         return loaded ? cuboid.getHeight() : 0;
     }
     
     /**
      * Get the length of the structure.
      * @return 
      * @TODO Structure length is not always accurate as when placed the structure is randomly flipped.
      */
     public int getLength(){
         return loaded ? cuboid.getLength() : 0;
     }
     
     /**
      * Get the set of materials this structure can overwrite.
      * @return 
      */
     public Set<Material> getTrump(){
         return trump;
     }
     
     /**
      * Get the set of materials this structure cannot use as a base.
      * @return 
      */
     public Set<Material> getInvalid(){
         return invalid;
     }
     
     /**
      * Get the minimum spawn height of the structure.
      * @return 
      */
     public Long getMinY(){
         return minY;
     }
     
     /**
      * Get the maximum spawn height of the structure.
      * @return 
      */
     public Long getMaxY(){
         return maxY;
     }
     
     /**
      * Get the unique name of the structure configuration.
      * @return 
      */
     public String getName(){
         return name;
     }
 }
