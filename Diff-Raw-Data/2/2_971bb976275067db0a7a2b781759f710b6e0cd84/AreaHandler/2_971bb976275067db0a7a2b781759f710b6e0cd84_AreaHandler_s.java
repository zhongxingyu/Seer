 /*
  * Copyright (C) 2011 MineStar.de 
  * 
  * This file is part of DirectorPlugin.
  * 
  * DirectorPlugin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * DirectorPlugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with DirectorPlugin.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.director.area;
 
 import java.awt.Point;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.TreeMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Chunk;
 import org.bukkit.World;
 import org.bukkit.craftbukkit.CraftWorld;
 
 import de.minestar.director.Core;
 import de.minestar.director.database.DatabaseHandler;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 
 public class AreaHandler {
 
     private TreeMap<String, Area> areaList;
     private DatabaseHandler dbHandler;
 
     private File dataFolder;
 
     public AreaHandler(DatabaseHandler dbHandler, File dataFolder) {
         this.dbHandler = dbHandler;
         this.dataFolder = dataFolder;
         this.loadAllAreas();
     }
 
     private void loadAllAreas() {
         areaList = this.dbHandler.loadAreas();
     }
 
     /**
      * getArea(String areaName)
      * 
      * @param areaName
      *            : the name of the area
      * @return the Area,if there is an area with that name, otherwise null
      */
     public Area getArea(String areaName) {
         return this.areaList.get(areaName.toLowerCase());
     }
 
     /**
      * areaExists(String areaName)
      * 
      * @param areaName
      *            : the name of the area
      * @return <b>true</b> : if the area exists <br>
      *         <b>false</b> : if the area does not exist
      */
     public boolean areaExists(String areaName) {
         return (this.getArea(areaName.toLowerCase()) != null);
     }
 
     /**
      * getAreas()
      * 
      * @return a TreeMap with all Areas (Key : areaName, Value : Area)
      */
     public TreeMap<String, Area> getAreas() {
         return this.areaList;
     }
 
     /**
      * addArea(Area newArea)
      * 
      * @param newArea
      *            : Area to be added
      * @return <b>false</b> : if the areaname is already in use. <br>
      *         <b>true</b> : if the area was added.
      */
     public boolean addArea(Area newArea) {
         if (this.areaExists(newArea.getAreaName()))
             return false;
 
         this.areaList.put(newArea.getAreaName().toLowerCase(), newArea);
         return true;
     }
 
     /**
      * resetArea(String areaName)
      * 
      * @param areaName
      *            : Area to reset
      * @return The resultstring of what has happened
      */
     public String resetArea(String areaName) {
         if (!this.areaExists(areaName))
             return "Die Area existiert nicht!";
 
         return resetArea(this.getArea(areaName));
     }
 
     public String saveArea(String areaName, Chunk chunk1, Chunk chunk2) {
         areaName = areaName.toLowerCase();
         // CHECK CHUNKS
         if (chunk1 == null || chunk2 == null)
             return "You must select 2 Chunks.";
 
         if (!chunk1.getWorld().getName().equalsIgnoreCase(chunk2.getWorld().getName()))
             return "Both Chunks must be in the same world.";
 
         try {
             // GET VARS
             World world = chunk1.getWorld();
             Point minChunk = new Point(Math.min(chunk1.getX(), chunk2.getX()), Math.min(chunk1.getZ(), chunk2.getZ()));
             Point maxChunk = new Point(Math.max(chunk1.getX(), chunk2.getX()), Math.max(chunk1.getZ(), chunk2.getZ()));
             DirectorChunkSnapshot snapshot = null;
             File dir = new File(dataFolder, "Areas/");
             dir.mkdirs();
 
             // DELETE OLD FILE
             File file = new File(dir, areaName + ".dp");
             if (file.exists())
                 file.delete();
 
             // WRITE DATA TO FILE
             int count = 0;
             FileOutputStream fos = new FileOutputStream(file);
             for (int x = minChunk.x; x <= maxChunk.x; x++) {
                 for (int z = minChunk.y; z <= maxChunk.y; z++) {
                     snapshot = DirectorChunkSnapshot.getSnapshot(world.getChunkAt(x, z));
                     fos.write(snapshot.getAllData());
                     count++;
                 }
             }
             fos.close();
 
             // RETURN
             return "Area saved! ( " + count + " Chunks )";
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Error while writing the Area File!");
             return "Error while writing File!";
         }
     }
 
     public String resetArea(Area area) {
         return resetArea(area.getAreaName(), area.getWorldName(), area.getMinChunk(), area.getMaxChunk());
     }
 
     public String resetArea(String areaName, String worldName, Point chunkPos1, Point chunkPos2) {
         areaName = areaName.toLowerCase();
         // GET WORLD
         World world = Bukkit.getServer().getWorld(worldName);
         if (world == null)
             return "World '" + worldName + "' not found!";
 
         // GET FIRST CHUNK
         Chunk chunk1 = world.getChunkAt(chunkPos1.x, chunkPos1.y);
         if (chunk1 == null)
             return "There is no Chunk at " + chunkPos1.x + "/" + chunkPos1.y + "! (Chunk 1)";
 
         // GET SECOND CHUNK
         Chunk chunk2 = world.getChunkAt(chunkPos1.x, chunkPos1.y);
         if (chunk2 == null)
             return "There is no Chunk at " + chunkPos2.x + "/" + chunkPos2.y + "! (Chunk 2)";
 
         // CALL METHOD
         return resetArea(areaName, chunk1, chunk2);
     }
 
     public String resetArea(String areaName, Chunk chunk1, Chunk chunk2) {
         areaName = areaName.toLowerCase();
         // CHECK CHUNKS
         if (chunk1 == null || chunk2 == null)
             return "You must select 2 Chunks.";
 
         if (!chunk1.getWorld().getName().equalsIgnoreCase(chunk2.getWorld().getName()))
             return "Both Chunks must be in the same world.";
 
         try {
             // GET VARS
             World world = chunk1.getWorld();
             Point minChunk = new Point(Math.min(chunk1.getX(), chunk2.getX()), Math.min(chunk1.getZ(), chunk2.getZ()));
             Point maxChunk = new Point(Math.max(chunk1.getX(), chunk2.getX()), Math.max(chunk1.getZ(), chunk2.getZ()));
             ArrayList<DirectorChunkSnapshot> snapshot = new ArrayList<DirectorChunkSnapshot>();
             String worldname = world.getName();
             File dir = new File(dataFolder, "Areas/");
             dir.mkdirs();
 
             // CHECK FILE EXISTS
            if (areaExists(areaName))
                 return "No Area named '" + areaName + "' defined!";
 
             // READ DATA FROM FILE
             FileInputStream fos = new FileInputStream(new File(dir, areaName + ".dp"));
             for (int x = minChunk.x; x <= maxChunk.x; x++) {
                 for (int z = minChunk.y; z <= maxChunk.y; z++) {
                     byte[] data = new byte[81920];
                     fos.read(data);
                     snapshot.add(new DirectorChunkSnapshot(x, z, worldname, data));
                 }
             }
             fos.close();
 
             // RESET BLOCKS
             CraftWorld cworld = (CraftWorld) world;
             net.minecraft.server.World nativeWorld = cworld.getHandle();
             int count = 0;
             int chunkX, chunkZ;
             DirectorChunkSnapshot thisSnapshot = null;
             DirectorChunkSnapshot currentSnapshot;
             for (int x = minChunk.x; x <= maxChunk.x; x++) {
                 chunkX = 16 * x;
                 for (int z = minChunk.y; z <= maxChunk.y; z++) {
                     currentSnapshot = DirectorChunkSnapshot.getSnapshot(cworld.getChunkAt(x, z));
                     chunkZ = 16 * z;
                     thisSnapshot = snapshot.get(count);
                     count++;
                     for (int blockX = 0; blockX < 16; blockX++) {
                         for (int blockZ = 0; blockZ < 16; blockZ++) {
                             for (int blockY = 0; blockY < 128; blockY++) {
                                 if (thisSnapshot.getBlockTypeId(blockX, blockY, blockZ) != currentSnapshot.getBlockTypeId(blockX, blockY, blockZ) || thisSnapshot.getBlockData(blockX, blockY, blockZ) != currentSnapshot.getBlockData(blockX, blockY, blockZ)) {
                                     nativeWorld.setTypeIdAndData(blockX + chunkX, blockY, blockZ + chunkZ, thisSnapshot.getBlockTypeId(blockX, blockY, blockZ), thisSnapshot.getBlockData(blockX, blockY, blockZ));
                                 }
                             }
                         }
                     }
                 }
             }
 
             // RETURN
             return "Area loaded! ( " + snapshot.size() + " Chunks )";
         } catch (Exception e) {
             // CATCH ERROR
             e.printStackTrace();
             return "Error while loading File!";
         }
     }
 }
