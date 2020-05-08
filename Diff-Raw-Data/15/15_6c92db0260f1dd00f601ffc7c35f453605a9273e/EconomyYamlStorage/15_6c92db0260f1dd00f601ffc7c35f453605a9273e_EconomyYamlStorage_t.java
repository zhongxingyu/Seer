 /*
  */
 package com.github.omwah.SDFEconomy;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Set;
 import java.util.ArrayList;
import java.util.Collections;
 import java.util.logging.Logger;
 import java.util.Observer;
 import java.util.Observable;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.configuration.ConfigurationSection;
 
 /**
  * Implements an Economy storage through a YAML file
  */
 public class EconomyYamlStorage implements EconomyStorage, Observer {
     private final String player_prefix = "player_account";
     private final String bank_prefix = "bank_account";
     
     private final File accounts_file;
     private final boolean save_on_update;
     YamlConfiguration storage;
 
     private static final Logger log = Logger.getLogger(EconomyYamlStorage.class.getName());
 
     public EconomyYamlStorage(File accounts_file, boolean save_on_update) {
         this.accounts_file = accounts_file;
         this.save_on_update = save_on_update;
         this.storage = YamlConfiguration.loadConfiguration(accounts_file);
     }
 
     private ConfigurationSection getPlayerSection(String playerName, String location, boolean createIfMissing) {
         // Store and search for account names in lower case to make names case insensitive
         ConfigurationSection section = 
                 this.storage.getConfigurationSection(location.toLowerCase() + "." + this.player_prefix + "." + playerName.toLowerCase());
         if (section == null && createIfMissing) {
             section = this.storage.createSection(location.toLowerCase() + "." + this.player_prefix + "." + playerName.toLowerCase());
         }
         return section;
     }
 
     private ConfigurationSection getPlayerSection(String playerName, String location) {
         return getPlayerSection(playerName, location, false);
     }
     
     public List<String> getPlayerNames(String location) {
         Set<String> namesSet = this.storage.getConfigurationSection(location.toLowerCase() + "." + this.player_prefix).getKeys(false);
         List<String> nameList = new ArrayList<String>();
         nameList.addAll(namesSet);
         return nameList;
     }
     
     public boolean hasPlayerAccount(String playerName, String location) {
         return getPlayerSection(playerName, location) != null;
     }
 
     public PlayerAccount getPlayerAccount(String playerName, String location) {
         if (!hasPlayerAccount(playerName, location)) {
             this.log.severe("Player " + playerName + " @ " + location + " does not exist.");
             return null; 
         } else {
             ConfigurationSection section = getPlayerSection(playerName, location);
             PlayerAccount account = new PlayerAccount(playerName, location);
             account.setBalance(section.getDouble("balance"));
             account.addObserver((Observer) this);
             return account;
         }
     }
     
     public PlayerAccount createPlayerAccount(String playerName, String location, double begBalance) {
         PlayerAccount newAccount = new PlayerAccount(playerName, location);
         newAccount.setBalance(begBalance);
         updateAccount(newAccount);
         newAccount.addObserver((Observer) this);
         return newAccount;
     }
     
     public List<String> getBankNames() {
        ConfigurationSection bank_section = this.storage.getConfigurationSection(this.bank_prefix);
        if (bank_section != null) {
            Set<String> namesSet = bank_section.getKeys(false);
            List<String> nameList = new ArrayList<String>();
            nameList.addAll(namesSet);
            return nameList;
        } else {
            return Collections.<String>emptyList();
        }
     }
     
     private ConfigurationSection getBankSection(String accountName, boolean createIfMissing) {
         // Store and search for account names in lower case to make names case insensitive
         ConfigurationSection section = 
                 this.storage.getConfigurationSection(this.bank_prefix + "." + accountName.toLowerCase());
         if (section == null && createIfMissing) {
             section = this.storage.createSection(this.bank_prefix + "." + accountName.toLowerCase());
         }
         return section;
     }
 
     private ConfigurationSection getBankSection(String accountName) {
         return getBankSection(accountName, false);
     }
 
     public boolean hasBankAccount(String accountName) {
         if(accountName == null) {
             return false;
         } else {
             ConfigurationSection section = getBankSection(accountName);
             return section != null;
         }
     }
 
     public BankAccount getBankAccount(String accountName) {
         ConfigurationSection section = getBankSection(accountName);
         if (section == null) {
             this.log.severe("Bank account " + accountName + " does not exist");
             return null;
         }
         BankAccount account = new BankAccount(accountName, section.getString("owner"), section.getString("location"));
         account.setBalance(section.getDouble("balance"));
         account.setMembers(section.getStringList("members"));
         account.addObserver((Observer) this);
         return account;
      }
     
     public BankAccount createBankAccount(String accountName, String owner, String location, double begBalance) {
         if (this.hasBankAccount(accountName)) {
             this.log.severe("Bank account " + accountName + " already exists");
             return null;
         }
         BankAccount account = new BankAccount(accountName, owner, location);
         account.setBalance(begBalance);
         account.addObserver((Observer) this);
         updateAccount(account);
         return account;
     }
     
     public void deleteBankAccount(String accountName) {
         this.storage.set(this.bank_prefix + "." + accountName, null);
         if (this.save_on_update) {
             this.commit();
         }
     }
     
     public void update(Observable o, Object arg) {
         if (o instanceof Account) {
             updateAccount((Account) o);
         }
     }
             
     @Override
     public void updateAccount(Account account) {
         ConfigurationSection section;
         if(account instanceof PlayerAccount) {
             section = getPlayerSection(account.getName(), account.getLocation(), true);
         } else if (account instanceof BankAccount) {
             section = getBankSection(account.getName(), true);
         } else {
             throw new IllegalArgumentException("Account passed was not an instance of PlayerAccount or BankAccount");
         }
         section.set("balance", account.getBalance());
         section.set("location", account.getLocation());
         if (account instanceof BankAccount) {
             section.set("members", ((BankAccount) account).getMembers());
             section.set("owner", ((BankAccount) account).getOwner());
         }
         if (this.save_on_update) {
             this.commit();
         }
     }
    
     @Override
     public void reload() {
         this.storage = YamlConfiguration.loadConfiguration(this.accounts_file);
     }
 
     @Override
     public void commit() {
         try {
             this.storage.save(this.accounts_file);
         } catch(IOException e) {
             this.log.severe("Error saving YamlStorage to: " + this.accounts_file.getPath() + "\n" + e);
         }
     }
 
     /*
      * Intended to be used for debug purposes only
      */
     @Override
     public String toString() {
         return this.storage.saveToString();
     }
 
 }
