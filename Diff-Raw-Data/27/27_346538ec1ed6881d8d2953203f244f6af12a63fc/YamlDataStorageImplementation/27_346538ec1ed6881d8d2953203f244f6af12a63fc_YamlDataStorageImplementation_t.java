 package to.joe.decapitation.datastorage;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import to.joe.decapitation.Bounty;
import to.joe.decapitation.Bounty.BountyRewardComparator;
 import to.joe.decapitation.Decapitation;
 
 public class YamlDataStorageImplementation implements DataStorageInterface {
     
     private Decapitation plugin;
     private File configFile;
     private YamlConfiguration config;
     private int lastId;
     
     public YamlDataStorageImplementation (Decapitation plugin) throws IOException {
         this.plugin = plugin;
         
         this.configFile = new File(plugin.getDataFolder(), "bounties.yml");
         if (!(configFile.exists() && !configFile.isDirectory())) {
             configFile.createNewFile();
         }
         this.config = YamlConfiguration.loadConfiguration(configFile);
         if (!config.isConfigurationSection("bounties")) {
             config.createSection("bounties");
         }
         
         lastId = 0;
         Set<String> keys = this.config.getConfigurationSection("bounties").getKeys(false);
         for (String key : keys) {
             try {
                 int value = Integer.valueOf(key);
                 if (value < 0) {
                     this.plugin.getLogger().warning("Picked up an invalid id at bounties." + key);
                     continue;
                 }
                 if (value > lastId) {
                     this.lastId = value;
                 }
             } catch (NumberFormatException e) {
                 this.plugin.getLogger().warning("Picked up an invalid key at bounties." + key);
             }
         }
     }
     
     private void saveConfig() throws DataStorageException {
         try {
             this.config.save(configFile);
         } catch (IOException e) {
             throw new DataStorageException(e);
         }
     }
 
     @Override
     public int getNumBounties() throws DataStorageException {
         Set<String> keys = this.config.getConfigurationSection("bounties").getKeys(false);
         int bounties = 0;
         
         for (String key : keys) {
             try {
                 Integer.valueOf(key);
                 ConfigurationSection section = config.getConfigurationSection("bounties").getConfigurationSection(key);
                 if (section.getString("hunter") == null) {
                     bounties++;
                 }
             } catch (NumberFormatException e) {
                 this.plugin.getLogger().warning("Picked up an invalid key at bounties." + key);
                 continue;
             }
         }
         return bounties;
     }
 
     @Override
     public int getNumUnclaimedHeads(String issuer) throws DataStorageException {
         int count = 0;
         Set<String> keys = this.config.getConfigurationSection("bounties").getKeys(false);
         
         for (String key : keys) {
             try {
                 Integer.valueOf(key);
                 ConfigurationSection section = config.getConfigurationSection("bounties").getConfigurationSection(key);
                 if (issuer.equalsIgnoreCase(section.getString("issuer"))) {
                     if (section.get("turnedin") != null && section.get("reedemed") == null) {
                         count++;
                     }
                 }
             } catch (NumberFormatException e) {
                 this.plugin.getLogger().warning("Picked up an invalid key at bounties." + key);
                 continue;
             }
         }
         
         return count;
     }
 
     @Override
     public List<Bounty> getUnclaimedBounties(String issuer) throws DataStorageException {
         List<Bounty> bounties = new ArrayList<Bounty>();
         Set<String> keys = this.config.getConfigurationSection("bounties").getKeys(false);
         
         for (String key : keys) {
             try {
                 int keyValue = Integer.valueOf(key);
                 ConfigurationSection section = config.getConfigurationSection("bounties").getConfigurationSection(key);
                 if (issuer.equalsIgnoreCase(section.getString("issuer"))) {
                     if (section.getLong("turnedin") != 0 && section.getLong("reedemed") == 0) {
                         bounties.add(Bounty.fromConfigurationSection(keyValue, section));
                     }
                 }
             } catch (NumberFormatException e) {
                 this.plugin.getLogger().warning("Picked up an invalid key at bounties." + key);
                 continue;
             }
         }
         
         return bounties;
     }
 
     @Override
     public List<Bounty> getBounties(int min, int max) throws DataStorageException {
         List<Bounty> bounties = new ArrayList<Bounty>();
         Set<String> keys = this.config.getConfigurationSection("bounties").getKeys(false);
         
         List<Bounty> allBounties = new ArrayList<Bounty>();
         for (String key : keys) {
             try {
                 int keyValue = Integer.valueOf(key);
                 ConfigurationSection section = config.getConfigurationSection("bounties").getConfigurationSection(key);
                 
                if (section.getString("hunter") == null) {
                     allBounties.add(Bounty.fromConfigurationSection(keyValue, section));
                 }
             } catch (NumberFormatException e) {
                 this.plugin.getLogger().warning("Picked up an invalid key at bounties." + key);
                 continue;
             }
         }
         
        Collections.sort(allBounties, Collections.reverseOrder(new BountyRewardComparator()));
         if (allBounties.size() < max) {
             max = allBounties.size() - 1;
         }
         if (min > allBounties.size()) {
             return bounties;
         }
         
         for (int i = min; i < max; i++) {
             bounties.add(allBounties.get(i));
         }
         
         return bounties;
     }
 
     @Override
     public List<Bounty> getBounties(String hunted) throws DataStorageException {
         List<Bounty> bounties = new ArrayList<Bounty>();
         Set<String> keys = this.config.getConfigurationSection("bounties").getKeys(false);
         
         for (String key : keys) {
             try {
                 int keyValue = Integer.valueOf(key);
                 ConfigurationSection section = config.getConfigurationSection("bounties").getConfigurationSection(key);
                if (section.getString("hunted") != null && section.getString("hunted").matches(".*" + hunted + ".*") && section.getString("hunter") == null) {
                     bounties.add(Bounty.fromConfigurationSection(keyValue, section));
                 }
             } catch (NumberFormatException e) {
                 this.plugin.getLogger().warning("Picked up an invalid key at bounties." + key);
                 continue;
             }
         }
         
        Collections.sort(bounties, new BountyRewardComparator());
         
         return bounties;
     }
 
     @Override
     public List<Bounty> getOwnBounties(String issuer) throws DataStorageException {
         List<Bounty> bounties = new ArrayList<Bounty>();
         Set<String> keys = this.config.getConfigurationSection("bounties").getKeys(false);
         
         for (String key : keys) {
             try {
                 int keyValue = Integer.valueOf(key);
                 ConfigurationSection section = config.getConfigurationSection("bounties").getConfigurationSection(key);
                 if (issuer.equalsIgnoreCase(section.getString("issuer")) && section.getString("hunter") == null) {
                     bounties.add(Bounty.fromConfigurationSection(keyValue, section));
                 }
             } catch (NumberFormatException e) {
                 this.plugin.getLogger().warning("Picked up an invalid key at bounties." + key);
                 continue;
             }
         }
         
         return bounties;
     }
 
     @Override
     public Bounty getBounty(String hunted) throws DataStorageException {
         List<Bounty> bounties = new ArrayList<Bounty>();
         Set<String> keys = this.config.getConfigurationSection("bounties").getKeys(false);
         
         for (String key : keys) {
             try {
                 int keyValue = Integer.valueOf(key);
                 ConfigurationSection section = config.getConfigurationSection("bounties").getConfigurationSection(key);
                 if (hunted.equalsIgnoreCase(section.getString("hunted")) && section.getString("hunter") == null) {
                     bounties.add(Bounty.fromConfigurationSection(keyValue, section));
                 }
             } catch (NumberFormatException e) {
                 this.plugin.getLogger().warning("Picked up an invalid key at bounties." + key);
                 continue;
             }
         }
         
        Collections.sort(bounties, Collections.reverseOrder(new BountyRewardComparator()));
         
         return (bounties.size() == 0) ? null : bounties.get(0);
     }
 
     @Override
     public Bounty getBounty(String hunted, String issuer) throws DataStorageException {
         List<Bounty> bounties = new ArrayList<Bounty>();
         Set<String> keys = this.config.getConfigurationSection("bounties").getKeys(false);
         
         for (String key : keys) {
             try {
                 int keyValue = Integer.valueOf(key);
                 ConfigurationSection section = config.getConfigurationSection("bounties").getConfigurationSection(key);
                 if (hunted.equalsIgnoreCase(section.getString("hunted")) && issuer.equalsIgnoreCase(section.getString("issuer")) && section.getString("hunter") == null) {
                     bounties.add(Bounty.fromConfigurationSection(keyValue, section));
                 }
             } catch (NumberFormatException e) {
                 this.plugin.getLogger().warning("Picked up an invalid key at bounties." + key);
                 continue;
             }
         }
         
        Collections.sort(bounties, Collections.reverseOrder(new BountyRewardComparator()));
         
         return (bounties.size() == 0) ? null : bounties.get(0);
     }
 
     @Override
     public Bounty addBounty(Bounty bounty) throws DataStorageException {
         int newId = lastId + 1;
         this.config.getConfigurationSection("bounties").createSection(Integer.toString(newId));
         ConfigurationSection section = this.config.getConfigurationSection("bounties").getConfigurationSection(Integer.toString(newId));
         section.set("issuer", bounty.getIssuer());
         section.set("hunted", bounty.getHunted());
         section.set("reward", bounty.getReward());
         this.lastId = newId;
         this.saveConfig();
         return new Bounty(newId, bounty.getIssuer(), bounty.getHunted(), bounty.getReward(), null, null, null, null);
     }
 
     @Override
     public void updateBounty(Bounty bounty) throws DataStorageException {
         if (this.config.getConfigurationSection("bounties").getConfigurationSection(Integer.toString(bounty.getID())) == null) {
             throw new DataStorageException("Tried to update a bounty whose id didn't exist!");
         }
         ConfigurationSection section = this.config.getConfigurationSection("bounties").getConfigurationSection(Integer.toString(bounty.getID()));
         section.set("issuer", bounty.getIssuer());
         section.set("hunted", bounty.getHunted());
         section.set("reward", bounty.getReward());
         section.set("created", bounty.getCreated().getTime());
         section.set("hunter", bounty.getHunter());
         section.set("turnedin", bounty.getTurnedIn().getTime());
         section.set("redeemed", bounty.getRedeemed().getTime());
         this.saveConfig();
     }
 
     @Override
     public void deleteBounty(Bounty bounty) throws DataStorageException {
         if (this.config.getConfigurationSection("bounties").getConfigurationSection(Integer.toString(bounty.getID())) == null) {
             throw new DataStorageException("Tried to delete a bounty whose id didn't exist!");
         }
         this.config.set("bounties." + Integer.toString(bounty.getID()), null);
         if (bounty.getID() == this.lastId) {
             lastId--;
         }
         this.saveConfig();
     }
 
 }
