 package net.aufdemrand.denizen.objects;
 
 import net.aufdemrand.denizen.tags.Attribute;
 import net.aufdemrand.denizen.utilities.DenizenAPI;
 import net.aufdemrand.denizen.utilities.Utilities;
 import net.aufdemrand.denizen.utilities.blocks.SafeBlock;
 import net.aufdemrand.denizen.utilities.debugging.dB;
 import net.aufdemrand.denizen.utilities.depends.Depends;
 import net.aufdemrand.denizen.utilities.depends.WorldGuardUtilities;
 import net.aufdemrand.denizen.utilities.entity.Rotation;
 import net.minecraft.server.v1_6_R3.*;
 import org.bukkit.*;
 import org.bukkit.Chunk;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Sign;
 import org.bukkit.craftbukkit.v1_6_R3.CraftChunk;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.InventoryHolder;
 
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class dChunk extends CraftChunk implements dObject {
 
     //////////////////
     //    OBJECT FETCHER
     ////////////////
 
     /**
      * Gets a Chunk Object from a string form of x,y,z,world.
      * This is not to be confused with the 'x,y,world' of a
      * location, which is a finer grain of unit in Worlds..
      *
      * @param string  the string or dScript argument String
      * @return  a dChunk, or null if incorrectly formatted
      *
      */
     @ObjectFetcher("ch")
     public static dChunk valueOf(String string) {
         if (string == null) return null;
 
         string = string.toLowerCase().replace("ch@", "");
 
         ////////
         // Match location formats
 
         // Get a location to fetch its chunk, return if null
         String[] parts = string.split(",");
         if (parts.length == 3) {
             try {
                 return new dChunk((CraftChunk) dWorld.valueOf(parts[2]).getWorld()
                         .getChunkAt(Integer.valueOf(parts[0]), Integer.valueOf(parts[1])));
             } catch (Exception e) {
                 dB.log("valueOf dChunk returning null: " + "ch@" + string);
                 return null;
             }
 
         } else
             dB.log("valueOf dChunk unable to handle malformed format: " + "ch@" + string);
 
         return null;
     }
 
 
     public static boolean matches(String string) {
         if (string.toLowerCase().startsWith("ch@"))
             return true;
 
         else return false;
     }
 
 
     /**
      * dChunk can be constructed with a CraftChunk
      *
      * @param chunk
      */
     public dChunk(CraftChunk chunk) {
         super(chunk.getHandle());
     }
 
     /**
      * dChunk can be constructed with a Location (or dLocation)
      *
      * @param location
      */
     public dChunk(Location location) {
         super (((CraftChunk) location.getChunk()).getHandle());
     }
 
     String prefix = "Chunk";
 
     @Override
     public String getObjectType() {
         return "Chunk";
     }
 
     @Override
     public String getPrefix() {
         return prefix;
     }
 
     @Override
     public dChunk setPrefix(String prefix) {
         this.prefix = prefix;
         return this;
     }
 
     @Override
     public String debug() {
         return ("<G>" + prefix + "='<Y>" + identify() + "<G>'  ");
     }
 
     @Override
     public boolean isUnique() {
         return true;
     }
 
     @Override
     public String identify() {
         return "ch@" + ',' + getX() + ',' + getZ() + ',' + getWorld().getName();
     }
 
     @Override
     public String toString() {
         return identify();
     }
 
     @Override
     public String getAttribute(Attribute attribute) {
         if (attribute == null) return null;
 
         // <--[tag]
         // @attribute <ch@chunk.is_loaded>
        // @returns Element(boolean)
         // @description
        // returns true if the chunk is actively loaded into memory.
         // -->
         if (attribute.startsWith("is_loaded"))
             return new Element(this.isLoaded()).getAttribute(attribute.fulfill(1));
 
         // <--[tag]
         // @attribute <ch@chunk.x>
        // @returns Element(number)
         // @description
         // returns the x coordinate of the chunk.
         // -->
         if (attribute.startsWith("x"))
             return new Element(this.getX()).getAttribute(attribute.fulfill(1));
 
         // <--[tag]
         // @attribute <ch@chunk.z>
        // @returns Element(number)
         // @description
         // returns the z coordinate of the chunk.
         // -->
         if (attribute.startsWith("z"))
             return new Element(this.getZ()).getAttribute(attribute.fulfill(1));
 
         // <--[tag]
         // @attribute <ch@chunk.world>
         // @returns dWorld
         // @description
         // returns the world associated with the chunk.
         // -->
         if (attribute.startsWith("world"))
             return dWorld.mirrorBukkitWorld(getWorld()).getAttribute(attribute.fulfill(1));
 
         // <--[tag]
         // @attribute <ch@chunk.entities>
         // @returns dList(dEntity)
         // @description
         // returns a list of entities in the chunk.
         // -->
         if (attribute.startsWith("entities")) {
             dList entities = new dList();
             for (Entity ent : this.getEntities())
                 entities.add(new dEntity(ent).identify());
 
             return entities.getAttribute(attribute.fulfill(1));
         }
 
         // <--[tag]
         // @attribute <ch@chunk.living_entities>
         // @returns dList(dEntity)
         // @description
         // returns a list of living entities in the chunk. This includes Players, mobs, NPCs, etc., but excludes
         // dropped items, experience orbs, etc.
         // -->
         if (attribute.startsWith("living_entities")) {
             dList entities = new dList();
             for (Entity ent : this.getEntities())
                 if (ent instanceof LivingEntity)
                     entities.add(new dEntity(ent).identify());
 
             return entities.getAttribute(attribute.fulfill(1));
         }
 
         // <--[tag]
         // @attribute <ch@chunk.height_map>
        // @returns dList
         // @description
         // returns a list of the height of each block in the chunk.
         // -->
         if (attribute.startsWith("height_map")) {
             List<String> height_map = new ArrayList<String>(this.getHandle().heightMap.length);
             for (int i : this.getHandle().heightMap)
                 height_map.add(String.valueOf(i));
             return new dList(height_map).getAttribute(attribute.fulfill(1));
         }
 
         // <--[tag]
         // @attribute <ch@chunk.average_height>
        // @returns Element(number)
         // @description
         // returns the average height of the blocks in the chunk.
         // -->
         if (attribute.startsWith("average_height")) {
             int sum = 0;
             for (int i : this.getHandle().heightMap) sum += i;
             return new Element(((double) sum)/getHandle().heightMap.length).getAttribute(attribute.fulfill(1));
         }
 
         // <--[tag]
         // @attribute <ch@chunk.is_flat[#]>
        // @returns dList
         // @description
         // scans the heights of the blocks to check variance between them. If no number is supplied, is_flat will return
         // true if all the blocks are less than 2 blocks apart in height. Specifying a number will modify the number
         // criteria for determining if it is flat.
         // -->
         if (attribute.startsWith("is_flat")) {
             int tolerance = attribute.hasContext(1) && aH.matchesInteger(attribute.getContext(1)) ?
                     Integer.valueOf(attribute.getContext(1)) : 2;
             int x = this.getHandle().heightMap[0];
             for (int i : this.getHandle().heightMap)
                 if (Math.abs(x - i) > tolerance)
                     return Element.FALSE.getAttribute(attribute.fulfill(1));
 
             return Element.TRUE.getAttribute(attribute.fulfill(1));
         }
 
         // <--[tag]
         // @attribute <ch@chunk.surface_blocks>
         // @returns dList(dLocation)
         // @description
         // returns a list of the highest non-air surface blocks in the chunk.
         // -->
         if (attribute.startsWith("surface_blocks")) {
             dList surface_blocks = new dList();
             for (int x = 0; x < 16; x++)
                 for (int z = 0; z < 16; z++)
                     surface_blocks.add(new dLocation(getBlock(x, getChunkSnapshot().getHighestBlockYAt(x, z) - 1, z).getLocation()).identify());
 
             return surface_blocks.getAttribute(attribute.fulfill(1));
         }
 
         return new Element(identify()).getAttribute(attribute);
     }
 
 }
