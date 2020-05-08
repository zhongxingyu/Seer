 /*
  * Copyright (C) 2013-2014 Dabo Ross <http://www.daboross.net/>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.daboross.bukkitdev.skywars.api.location;
 
 import java.util.HashMap;
 import java.util.Map;
 import lombok.EqualsAndHashCode;
 import lombok.ToString;
 import org.apache.commons.lang.Validate;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.configuration.serialization.SerializableAs;
 
 @ToString
 @EqualsAndHashCode
 @SerializableAs("SkyLocationRange")
 public class SkyBlockLocationRange implements ConfigurationSerializable {
 
     public final SkyBlockLocation min;
     public final SkyBlockLocation max;
     public final String world;
 
     /**
      * Creates a SkyBlockLocationRange.
      *
      * @param min   minimum position. min.world is expected to be equal to world,
      *              and will be changed if not world already.
      * @param max   maximum position. max.world is expected to be equal to world,
      *              and will be changed if not world already.
      * @param world world of the range.
      * @throws IllegalArgumentException if min == null || max == null || min.x >
      *                                  max.x || min.y > max.y || min.z > max.z
      * @throws NullPointerException     if min or max is null
      */
     public SkyBlockLocationRange(SkyBlockLocation min, SkyBlockLocation max, String world) {
         Validate.notNull(min, "Min cannot be null");
         Validate.notNull(max, "Max cannot be null");
        Validate.isTrue(min.x <= max.x && min.y <= max.y && min.z <= max.z, "Min position cannot be bigger than max position in any dimension");
         if (min.world == null ? world != null : !min.world.equals(world)) {
             min = min.changeWorld(world);
         }
         if (max.world == null ? world != null : !max.world.equals(world)) {
             max = max.changeWorld(world);
         }
         this.world = min.world;
         this.min = min;
         this.max = max;
     }
 
     public SkyBlockLocationRange add(SkyBlockLocation loc) {
         Validate.notNull(loc, "Location cannot be null");
         return new SkyBlockLocationRange(loc.add(min), loc.add(max), world);
     }
 
     public boolean isWithin(Location loc) {
         return loc.getX() <= max.x && loc.getX() >= min.x
                 && loc.getY() <= max.y && loc.getY() >= min.y
                 && loc.getZ() <= max.z && loc.getZ() >= min.z;
     }
 
     public boolean isWithin(Block block) {
         return block.getX() <= max.x && block.getX() >= min.x
                 && block.getY() <= max.y && block.getY() >= min.y
                 && block.getZ() <= max.z && block.getZ() >= min.z;
     }
 
     public boolean isWithin(SkyBlockLocation loc) {
         return loc.x <= max.x && loc.x >= min.x
                 && loc.y <= max.y && loc.y >= min.y
                 && loc.z <= max.z && loc.z >= min.z;
     }
 
     @Override
     public Map<String, Object> serialize() {
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("min", min.changeWorld(null));
         map.put("max", max.changeWorld(null));
         map.put("world", world);
         return map;
     }
 
     public void serialize(ConfigurationSection section) {
         Validate.notNull(section, "ConfigurationSection cannot be null");
         min.changeWorld(null).serialize(section.createSection("min"));
         max.changeWorld(null).serialize(section.createSection("max"));
         section.set("world", world);
     }
 
     public static SkyBlockLocationRange deserialize(Map<String, Object> map) {
         Validate.notNull(map, "Map cannot be null");
         Object minObject = map.get("min"), maxObject = map.get("max"),
                 worldObject = map.get("world");
         if (!(minObject instanceof SkyBlockLocation && maxObject instanceof SkyBlockLocation)) {
             return null;
         }
         SkyBlockLocation min = (SkyBlockLocation) minObject, max = (SkyBlockLocation) maxObject;
         String world = worldObject == null ? null : worldObject.toString();
         return new SkyBlockLocationRange(min, max, world);
     }
 
     public static SkyBlockLocationRange deserialize(ConfigurationSection configurationSection) {
         if (configurationSection == null) {
             return null;
         }
         Object worldObject = configurationSection.get("world");
         ConfigurationSection minSection = configurationSection.getConfigurationSection("min"),
                 maxSection = configurationSection.getConfigurationSection("max");
         if (minSection == null || maxSection == null) {
             return null;
         }
         SkyBlockLocation min = SkyBlockLocation.deserialize(minSection);
         SkyBlockLocation max = SkyBlockLocation.deserialize(maxSection);
         if (min == null || max == null) {
             return null;
         }
         String world = worldObject instanceof String ? (String) worldObject : worldObject == null ? null : worldObject.toString();
         return new SkyBlockLocationRange(min, max, world);
     }
 }
