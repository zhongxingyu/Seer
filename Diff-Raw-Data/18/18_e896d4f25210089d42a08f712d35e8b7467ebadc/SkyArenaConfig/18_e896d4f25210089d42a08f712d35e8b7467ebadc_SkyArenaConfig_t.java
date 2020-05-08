 /*
  * Copyright (C) 2013 daboross
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
 package net.daboross.bukkitdev.skywars.api.arenaconfig;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import net.daboross.bukkitdev.skywars.api.Parentable;
 import net.daboross.bukkitdev.skywars.api.location.SkyPlayerLocation;
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.configuration.serialization.SerializableAs;
 
 /**
  *
  * @author daboross
  */
 @SerializableAs("SkyArenaConfig")
 public class SkyArenaConfig extends Parentable<SkyArenaConfig> implements ConfigurationSerializable {
 
     private List<SkyPlayerLocation> spawns;
     private Integer numPlayers;
     private final SkyBoundariesConfig boundaries = new SkyBoundariesConfig();
     private final SkyMessagesConfig messages = new SkyMessagesConfig();
 
     public SkyArenaConfig(SkyArenaConfig parent, List<SkyPlayerLocation> spawns, Integer numPlayers, SkyBoundariesConfig boundaries, SkyMessagesConfig messages) {
         super(parent);
         this.parent = parent;
         this.spawns = spawns;
         this.numPlayers = numPlayers;
         if (parent != null) {
             this.boundaries.setParent(parent.getBoundaries());
         }
         if (boundaries != null) {
             this.boundaries.copyDataFrom(boundaries);
         }
         if (messages != null) {
             this.messages.copyDataFrom(messages);
         }
     }
 
     public SkyArenaConfig(List<SkyPlayerLocation> spawns, Integer numPlayers, SkyBoundariesConfig boundaries, SkyMessagesConfig messages) {
         this.spawns = spawns;
         this.numPlayers = numPlayers;
         if (parent != null) {
             this.boundaries.setParent(parent.getBoundaries());
         }
         if (boundaries != null) {
             this.boundaries.copyDataFrom(boundaries);
         }
         if (messages != null) {
             this.messages.copyDataFrom(messages);
         }
     }
 
     @Override
     public void setParent(SkyArenaConfig parent) {
         super.setParent(parent);
         if (parent != null) {
             messages.setParent(parent.getMessages());
             boundaries.setParent(parent.getBoundaries());
         } else {
             messages.setParent(null);
             boundaries.setParent(null);
         }
     }
 
     public List<SkyPlayerLocation> getSpawns() {
         if (spawns == null) {
             if (parent == null) {
                 throw new IllegalStateException("Ultimate parent spawns not found.");
             } else {
                 return parent.getSpawns();
             }
         } else {
             return spawns;
         }
     }
 
     public void setSpawns(List<SkyPlayerLocation> spawns) {
         this.spawns = spawns;
     }
 
     public int getNumPlayers() {
         if (numPlayers == null) {
             if (parent == null) {
                 throw new IllegalStateException("Ultimate parent numPlayers not found.");
             } else {
                 return parent.getNumPlayers();
             }
         } else {
             return numPlayers.intValue();
         }
     }
 
     public void setNumPlayers(Integer numPlayers) {
         this.numPlayers = numPlayers;
     }
 
     public SkyBoundariesConfig getBoundaries() {
         return boundaries;
     }
 
     public SkyMessagesConfig getMessages() {
         return messages;
     }
 
     @Override
     public Map<String, Object> serialize() {
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("spawns", spawns);
         map.put("numPlayers", numPlayers);
         if (boundaries.definesAnything()) {
             map.put("boundaries", boundaries);
         }
         if (messages.definesAnything()) {
             map.put("messages", messages);
         }
         return map;
     }
 
     public static SkyArenaConfig deserialize(Map<String, Object> map) {
         Object spawnsObj = map.get("spawns"),
                 numPlayersObj = map.get("numPlayers"),
                 boundariesObj = map.get("boundaries"),
                 messagesObj = map.get("messages");
         List<?> spawns = spawnsObj instanceof List ? (List) spawnsObj : null;
        if (spawns != null) {
            for (Object obj : spawns) {
                if (!(obj instanceof SkyPlayerLocation)) {
                    Bukkit.getLogger().log(Level.WARNING, "[SkyWars] [SkyArenaConfig] Silently ignoring whole spawn list because one item in list is not a SkyPlayerLocation");
                    spawns = null;
                    break;
                }
             }
         }
         Integer numPlayers = numPlayersObj instanceof Integer ? (Integer) numPlayersObj : null;
         SkyBoundariesConfig boundaries = boundariesObj instanceof SkyBoundariesConfig ? (SkyBoundariesConfig) boundariesObj : null;
         SkyMessagesConfig messages = messagesObj instanceof SkyMessagesConfig ? (SkyMessagesConfig) messagesObj : null;
         return new SkyArenaConfig((List<SkyPlayerLocation>) spawns, numPlayers, boundaries, messages);
     }
 
     public static SkyArenaConfig deserialize(ConfigurationSection configurationSection) {
         Object numPlayersObj = configurationSection.get("numPlayers");
         ConfigurationSection boundariesSection = configurationSection.getConfigurationSection("boundaries"),
                 messagesSection = configurationSection.getConfigurationSection("messages");
         List<?> spawnsObjList = configurationSection.getList("spawns");
         List<SkyPlayerLocation> spawns = null;
         if (spawnsObjList != null) {
             spawns = new ArrayList<SkyPlayerLocation>(spawnsObjList.size());
             for (Object obj : spawnsObjList) {
                 if (obj instanceof Map) {
                    System.out.println("Teh list is a List<Map>");
                     SkyPlayerLocation loc = SkyPlayerLocation.deserialize((Map) obj);
                     spawns.add(loc);
                 } else if (obj instanceof ConfigurationSection) {// Not sure if map or configurationsection
                    System.out.println("Teh list is a ConfigurationSection");
                     SkyPlayerLocation loc = SkyPlayerLocation.deserialize((ConfigurationSection) obj);
                     spawns.add(loc);
                 }
             }
         }
         Integer numPlayers = numPlayersObj instanceof Integer ? (Integer) numPlayersObj : null;
         SkyBoundariesConfig boundaries = boundariesSection != null ? SkyBoundariesConfig.deserialize(boundariesSection) : null;
         SkyMessagesConfig messages = messagesSection != null ? SkyMessagesConfig.deserialize(messagesSection) : null;
         return new SkyArenaConfig(spawns, numPlayers, boundaries, messages);
     }
 
     @Override
     public String toString() {
         return "ArenaConfig{parent=" + parent + ",spawns=" + spawns + ",numPlayers=" + numPlayers + ",boundaries=" + boundaries + ",messages=" + messages + "}";
     }
 }
