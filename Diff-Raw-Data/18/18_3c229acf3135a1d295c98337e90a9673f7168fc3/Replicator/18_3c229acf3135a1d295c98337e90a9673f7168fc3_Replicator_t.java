 /*  Craft Inc. Replicator
     Copyright (C) 2013  Paul Schulze, Maximilian Häckel, Moritz Kaltofen
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package de.craftinc.replicator;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.BlockFace;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 public class Replicator implements ConfigurationSerializable
 {
     /**
      * Filename where to store the replicators.
      */
     private static final String dataFileName = "replicators.yml";
 
     private static final String keyReplicators = "replicators";
     private static final String keyName        = "name";
     private static final String keyCenter      = "center";
     private static final String keyOwners      = "owners";
     private static final String keyUsers       = "users";
 
     /**
      * List of owners. An owner is able to use the replicator and is able to add other users/owners.
      */
     private ArrayList<String> owners;
 
     /**
      * List of users. A user is able to use the replicator.
      */
     private ArrayList<String> users;
 
     /**
      * Name of the replicator. It will always be in the format "world,x,y,z".
      */
     private String name;
 
     /**
      * Center location of the replicator.
      */
     private Location center;
 
     /**
      * List of all replicators accessible by center location.
      */
    private static HashMap<Location, Replicator> allReplicators = new HashMap<Location, Replicator>();
 
     private static File              replicatorsFile     = new File(Plugin.instance.getDataFolder(), dataFileName);
     private static FileConfiguration replicatorsFileConf = YamlConfiguration.loadConfiguration(replicatorsFile);
 
 
     public Replicator( String firstOwner, Location center )
     {
         this.owners = new ArrayList<String>();
         this.users = new ArrayList<String>();
         this.owners.add(firstOwner);
         this.center = center;
         name = center.getWorld() + "," + center.getBlockX() + "," + center.getBlockY() + "," + center.getBlockZ();
     }
 
     @SuppressWarnings("unchecked unused")
     public Replicator( Map<String, Object> map )
     {
         try
         {
             name = (String) map.get(keyName);
             center = LocationSerializer.deserializeLocation((Map<String, Object>) map.get(keyCenter));
             owners = (ArrayList<String>) map.get(keyOwners);
             users = (ArrayList<String>) map.get(keyUsers);
 
             allReplicators.put(center, this);
         }
         catch ( Exception e )
         {
             Plugin.instance.getLogger().severe(e.getMessage());
         }
     }
 
     public ArrayList<String> getOwners()
     {
         return owners;
     }
 
     public ArrayList<String> getUsers()
     {
         return users;
     }
 
     public void addUser( String user )
     {
         this.users.add(user);
     }
 
     public void addOwner( String owner )
     {
         this.users.add(owner);
     }
 
     public boolean rmUser( String user )
     {
         if ( this.users.remove(user) )
             return true;
         else
             return false;
     }
 
     public boolean rmOwner( String owner )
     {
         if ( this.owners.remove(owner) )
             return true;
         else
             return false;
     }
 
     public boolean isOwner( String player )
     {
         for ( String owner : owners )
         {
             if ( owner.equals(player) )
             {
                 return true;
             }
         }
         return false;
     }
 
     public boolean isUser( String player )
     {
         for ( String user : users )
         {
             if ( user.equals(player) )
             {
                 return true;
             }
         }
         return false;
     }
 
     public void setName( String newName )
     {
         this.name = newName;
         //TODO: Save List
     }
 
     public String getName()
     {
         return name;
     }
 
     public static ArrayList<Location> getReplicators( Location currentBlock )
     {
         ArrayList<Location> replicators = new ArrayList<Location>();
         ArrayList<Location> centers = getCenters(currentBlock);
         for ( Location center : centers )
         {
             if ( isValid(center) )
             {
                 replicators.add(center);
             }
         }
         return replicators;
     }
 
     private static Material[][][] getPattern( Location center )
     {
         if ( center.getBlock().getRelative(BlockFace.NORTH).getType().equals(Material.AIR) )
             return Pattern.getNorth();
         if ( center.getBlock().getRelative(BlockFace.SOUTH).getType().equals(Material.AIR) )
             return Pattern.getSouth();
         if ( center.getBlock().getRelative(BlockFace.WEST).getType().equals(Material.AIR) )
             return Pattern.getWest();
         if ( center.getBlock().getRelative(BlockFace.EAST).getType().equals(Material.AIR) )
             return Pattern.getEast();
         return null;
     }
 
     private static boolean isValid( Location center )
     {
         Material[][][] pattern = getPattern(center);
         if ( pattern == null )
         {
             return false;
         }
         for ( int x = 0; x <= 2; x++ )
         {
             for ( int y = 0; y <= 2; y++ )
             {
                 for ( int z = 0; z <= 2; z++ )
                 {
                     if ( ( pattern[x][y][z] != center.getBlock().getRelative(x - 1, y - 1, z - 1).getType() ) &&
                          ( ( pattern[x][y][z] != null ) ) )
                     {
                         return false;
                     }
                 }
             }
         }
         return true;
     }
 
     private static ArrayList<Location> getCenters( Location currentBlock )
     {
         ArrayList<Location> centers = new ArrayList<Location>();
         Location nextBlock;
         for ( int x = -1; x <= 1; x++ )
         {
             for ( int y = -1; y <= 1; y++ )
             {
                 for ( int z = -1; z <= 1; z++ )
                 {
                     nextBlock = currentBlock.getBlock().getRelative(x, y, z).getLocation();
                     if ( nextBlock.getBlock().getType().equals(Pattern.getCenter()) )
                         centers.add(nextBlock);
                 }
             }
         }
         return centers;
     }
 
     /**
      * Gets a replicator with the given location. If no replicator exists a new one is created.
      * Returns null if player is not owner or user of the replicator.
      *
      * @param loc        center of the replicator
      * @param playerName name of the player
      * @return Replicator
      */
     public static Replicator getOrCreate( Location loc, String playerName )
     {
         Replicator rep = allReplicators.get(loc);
 
         // replicator already exists
         if ( rep != null )
         {
             if ( rep.isOwner(playerName) || rep.isUser(playerName) )
             {
                 return rep;
             }
             else
             {
                 return null;
             }
         }
         // replicator does not exist, create one
         else
         {
             rep = new Replicator(playerName, loc);
             allReplicators.put(loc, rep);
             try {
                 Replicator.saveReplicators();
             }
             catch ( IOException e )
             {
                 Plugin.instance.getServer().getPlayer(playerName).sendMessage(Messages.couldNotSave);
                 Plugin.instance.getLogger().severe("Could not save replicators to file: " + e.getMessage());
             }
             return rep;
         }
     }
 
     /**
      * Get a replicator with the specified name. Returns null if player
      * is not owner or user or if replicator does not exist.
      *
      * @param repName    name of the replicator
      * @param playerName name of the player
      * @return Replicator
      */
     public static Replicator getByName( String repName, String playerName )
     {
         for ( Replicator rep : allReplicators.values() )
         {
             if ( rep.getName().equals(repName) )
             {
                 if ( rep.isOwner(playerName) || rep.isUser(playerName) )
                 {
                     return rep;
                 }
             }
         }
         return null;
     }
 
     public static ArrayList<Replicator> getReplicatorsByOwner( String playerName )
     {
         ArrayList<Replicator> reps = new ArrayList<Replicator>();
         for ( Replicator rep : allReplicators.values() )
         {
             if ( rep.isOwner(playerName) )
             {
                 reps.add(rep);
             }
         }
         return reps;
     }
 
     public static ArrayList<Replicator> getReplicatorsByUser( String playerName )
     {
         ArrayList<Replicator> reps = new ArrayList<Replicator>();
         for ( Replicator rep : allReplicators.values() )
         {
             if ( rep.isUser(playerName) )
             {
                 reps.add(rep);
             }
         }
         return reps;
     }
 
     @SuppressWarnings("unused")
     public Map<String, Object> serialize()
     {
         Map<String, Object> map = new HashMap<String, Object>();
 
         map.put(keyName, name);
         map.put(keyCenter, LocationSerializer.serializeLocation(center));
         map.put(keyOwners, owners);
         map.put(keyUsers, users);
 
         return map;
     }
 
     public static void loadReplicators()
     {
         replicatorsFileConf.getList(keyReplicators);
     }
 
     public static void saveReplicators() throws IOException
     {
         replicatorsFileConf.set(keyReplicators, new ArrayList<Object>(allReplicators.values()));
         replicatorsFileConf.save(replicatorsFile);
     }
 }
