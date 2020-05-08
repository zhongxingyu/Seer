 package com.norcode.bukkit.livestocklock;
 import com.norcode.bukkit.livestocklock.commands.*;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import java.util.*;
 
 public class LivestockLock extends JavaPlugin {
 
     private Map<UUID, OwnedAnimal> ownedAnimals = new HashMap<UUID, OwnedAnimal>();
     private HashMap<Short, ClaimableAnimal> allowedAnimals = new HashMap<Short, ClaimableAnimal>();
     private HashMap<String, List<String>> accessLists = new HashMap<String, List<String>>();
     private HashMap<String, List<UUID>> ownedAnimalsByPlayer = new HashMap<String, List<UUID>>();
     private org.bukkit.permissions.Permission wildcardPermission;
     private boolean debugMode = false;
     private Economy economy = null;
     private Permission vaultPerm = null;
     private DataStore datastore;
     private HashMap<String, BaseCommand> subCommands = new HashMap<String, BaseCommand>();
     private SortedSet<Map.Entry<String, Integer>> groupLimits;
 
     public void onEnable() {
         saveDefaultConfig();
         getConfig().options().copyDefaults(true);
         initializeVault();
         saveConfig();
        wildcardPermission = new org.bukkit.permissions.Permission("livestocklock.claim.*", PermissionDefault.TRUE);
         getServer().getPluginManager().addPermission(wildcardPermission);
         loadConfig();
         initializeDatastore();
         getServer().getPluginManager().registerEvents(new EntityListener(this), this);
         // setup commands
         new AbandonCommand(this);
         new AddPlayerCommand(this);
         new ClaimCommand(this);
         new ListCommand(this);
         new RemovePlayerCommand(this);
     }
 
     public void onDisable() {
         if (datastore != null) {
             datastore.saveAccessLists(accessLists);
             datastore.saveOwnedAnimals(ownedAnimals);
             datastore.onDisable();
         }
     }
 
     private void initializeVault() {
         if (getServer().getPluginManager().getPlugin("Vault") != null) {
             RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
             if (economyProvider != null) {
                 economy = economyProvider.getProvider();
             } else {
                 getLogger().warning("No appropriate economy plugin found.  Economy costs will not function.");
             }
             RegisteredServiceProvider<Permission> permProvider = getServer().getServicesManager().getRegistration(Permission.class);
             if (permProvider != null) {
                 vaultPerm = permProvider.getProvider();
             } else {
                 getLogger().warning("No appropriate permissions plugin found.  Group-based claim limits will not function.");
             }
         } else {
             getLogger().severe("Vault was not found.  Group based limits and economy costs will not function.");
         }
     }
 
     private void initializeDatastore() {
         this.datastore = new DataStore(this);
         this.datastore.onEnable();
         this.accessLists = this.datastore.getAccessLists();
         this.ownedAnimals = this.datastore.getOwnedAnimals();
         this.ownedAnimalsByPlayer =  new HashMap<String, List<UUID>>();
         for (OwnedAnimal oa: ownedAnimals.values()) {
             if (!ownedAnimalsByPlayer.containsKey(oa.getOwnerName())) {
                 ownedAnimalsByPlayer.put(oa.getOwnerName(), new LinkedList<UUID>());
             }
             ownedAnimalsByPlayer.get(oa.getOwnerName()).add(oa.getEntityId());
         }
     }
 
     private void loadConfig() {
         // Load set of allowed entities
         ConfigurationSection animalSection = getConfig().getConfigurationSection("allowed-entity-types");
         allowedAnimals.clear();
         ClaimableAnimal ca;
         ConfigurationSection sect;
         short eid;
         for (String s: animalSection.getKeys(false)) {
             sect = animalSection.getConfigurationSection(s);
             String eType = sect.getString("entity-type-id", "");
             eid = -1;
             try {
                 eid = Short.parseShort(eType);
             } catch (IllegalArgumentException ex) {
                 EntityType et = EntityType.valueOf(eType.toUpperCase());
                 if (et == null) {
                     getLogger().warning("Unknown EntityType: " + eType);
                     continue;
                 }
                 eid = et.getTypeId();
             }
             ca = new ClaimableAnimal(this, (short) eid);
             ca.setCostMoney(sect.getDouble("cost-money"));
             ca.setCostXP(sect.getInt("cost-xp"));
             ca.setCostItem(sect.getItemStack("cost-item"));
             allowedAnimals.put(eid, ca);
         }
         // Load permission based limits.
         ConfigurationSection groupSect = getConfig().getConfigurationSection("group-limits");
         HashMap<String, Integer> limits = new HashMap<String, Integer>();
         for (String groupName: groupSect.getKeys(false)) {
             limits.put(groupName, groupSect.getInt(groupName));
         }
         groupLimits = entriesSortedByValues(limits, true);
         debugMode = getConfig().getBoolean("debug");
     }
 
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         debug(label);
         if (args.length == 0) {
             return false;
         }
         LinkedList<String> params = new LinkedList<String>(Arrays.asList(args));
         String sub = params.pop().toLowerCase();
 
         if (!subCommands.containsKey(sub)) {
             debug("No such subcommand: " + sub);
             return false;
         }
         BaseCommand subcommand = subCommands.get(sub);
         if (!sender.hasPermission("livestocklock.command." + subcommand.getName())) {
             sender.sendMessage("You don't have permission for that.");
             return true;
         }
         return subcommand.onCommand(sender, label + " " + sub, params);
     }
 
     @Override
     public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
         LinkedList<String> params = new LinkedList<String>(Arrays.asList(args));
         if (params.size() == 1) {
             LinkedList<String> results = new LinkedList<String>();
             for (String k: subCommands.keySet()) {
                 if (k.toLowerCase().startsWith(params.peek()) && sender.hasPermission("livestocklock.command." + k.toLowerCase())) {
                     results.add(k);
                 }
             }
             return results;
         }
         String sub = params.pop().toLowerCase();
         if (!subCommands.containsKey(sub)) {
             return null;
         }
         BaseCommand subcommand = subCommands.get(sub);
         if (sender.hasPermission("livestocklock.command." + subcommand.getName().toLowerCase())) {
             return subcommand.onTabComplete(sender, label + " " + sub, params);
         }
         return null;
     }
 
     void debug(String s) {
         if (debugMode) {
             getLogger().info(s);
         }
     }
 
 
     Map<UUID, OwnedAnimal> getOwnedAnimals() {
         return ownedAnimals;
     }
 
     /**
      * get the max number of animals a player can claim.
      *
      * @param player
      * @return the max number of animals a player can claim.
      */
     public int getPlayerClaimLimit(Player player) {
         if (vaultPerm != null) {
             for (Map.Entry<String, Integer> e: groupLimits) {
                 if (vaultPerm.playerInGroup(player, e.getKey())) {
                     return e.getValue();
                 }
 
             }
         }
         return groupLimits.first().getValue();
     }
 
     /**
      * get the OwnedAnimal object if the given entity is owned.
      *
      * @param uniqueId the entity's unique id
      * @return an OwnedAnimal object representing this entity, or null if this entity is not owned.
      */
     public OwnedAnimal getOwnedAnimal(UUID uniqueId) {
         return ownedAnimals.get(uniqueId);
     }
 
     Map<Short, ClaimableAnimal> getClaimableAnimals() {
         return allowedAnimals;
     }
 
     Economy getEconomy() {
         return economy;
     }
 
     /**
      * get the specified player's access list.
      *
      * get a list of the names of all players who have access to the given player's animals.
      *
      * @param ownerName
      * @return
      */
     public List<String> getAccessList(String ownerName) {
         if (!accessLists.containsKey(ownerName)) {
             return new ArrayList<String>(0);
         }
         return accessLists.get(ownerName);
     }
 
     /**
      * save the specified OwnedAnimal to the datastore.
      *
      * @param ownedAnimal an OwnedAnimal instance to be saved.
      */
     public void saveOwnedAnimal(OwnedAnimal ownedAnimal) {
         this.ownedAnimals.put(ownedAnimal.getEntityId(), ownedAnimal);
         if (!ownedAnimalsByPlayer.containsKey(ownedAnimal.getOwnerName())) {
             ownedAnimalsByPlayer.put(ownedAnimal.getOwnerName(), new LinkedList<UUID>());
         }
         ownedAnimalsByPlayer.get(ownedAnimal.getOwnerName()).add(ownedAnimal.getEntityId());
     }
 
     /**
      * remove an OwnedAnimal from the datastore.
      *
      * @param ownedAnimal an OwnedAnimal instance to be removed from the datastore.
      */
     public void removeOwnedAnimal(OwnedAnimal ownedAnimal) {
         this.ownedAnimals.remove(ownedAnimal.getEntityId());
         this.ownedAnimalsByPlayer.get(ownedAnimal.getOwnerName()).remove(ownedAnimal.getEntityId());
     }
 
     /**
      * get a list of id's for all animals belonging to owner
      *
      * returns a list of the UniqueIDs of all animal's owned by the specified player.
      * @param owner the name of the player
      * @return a list of UUID's, one for each animal owned by this player.
      */
     public List<UUID> getOwnedAnimalIDs(String owner) {
         if (!ownedAnimalsByPlayer.containsKey(owner)) return new LinkedList<UUID>();
         return this.ownedAnimalsByPlayer.get(owner);
     }
 
 
     /**
      * get a list of all OwnedAnimals belonging to the given player.
      *
      * @param owner the name of the player
      * @return a list of OwnedAnimal instances
      */
     public List<OwnedAnimal> getOwnedAnimals(String owner) {
         List<UUID> ids = getOwnedAnimalIDs(owner);
         List<OwnedAnimal> results = new ArrayList<OwnedAnimal>(ids.size());
         for (UUID u: ids) {
             results.add(getOwnedAnimal(u));
         }
         return results;
     }
 
     /**
      * register a subcommand under the main /lsl command.
      *
      * @param name the name the command will be available under
      * @param baseCommand a BaseCommand instance
      */
     public void registerSubcommand(String name, BaseCommand baseCommand) {
         this.subCommands.put(name.toLowerCase(), baseCommand);
     }
 
 
     static <K,V extends Comparable<? super V>>
     SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map, final boolean reverse) {
         SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
                 new Comparator<Map.Entry<K,V>>() {
                     @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                         // never return equals or the group gets removed.
                         if (reverse) {
                             if (e2.getValue() == e1.getValue()) return 1;
                             return e2.getValue().compareTo(e1.getValue());
                         } else {
                             if (e2.getValue() == e1.getValue()) return 1;
                             return e1.getValue().compareTo(e2.getValue());
                         }
                     }
                 }
         );
         sortedEntries.addAll(map.entrySet());
         return sortedEntries;
     }
 
 
     org.bukkit.permissions.Permission getWildcardPermission() {
         return wildcardPermission;
     }
 }
