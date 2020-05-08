 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.xhawk87.Coinage;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 import java.util.logging.Level;
 import me.xhawk87.Coinage.commands.CoinListCommand;
 import me.xhawk87.Coinage.commands.CoinReloadCommand;
 import me.xhawk87.Coinage.commands.CoinValueCommand;
 import me.xhawk87.Coinage.commands.CombineCoinsCommand;
 import me.xhawk87.Coinage.commands.ConvertCoinsCommand;
 import me.xhawk87.Coinage.commands.CreateCurrencyCommand;
 import me.xhawk87.Coinage.commands.CreateDenominationCommand;
 import me.xhawk87.Coinage.commands.DeleteCurrencyCommand;
 import me.xhawk87.Coinage.commands.DeleteDenominationCommand;
 import me.xhawk87.Coinage.commands.GiveCoinsCommand;
 import me.xhawk87.Coinage.commands.GiveCurrency;
 import me.xhawk87.Coinage.commands.MintCoinsCommand;
 import me.xhawk87.Coinage.commands.SetDefaultCurrencyCommand;
 import me.xhawk87.Coinage.commands.SpendCoinsCommand;
 import me.xhawk87.Coinage.commands.SplitCoinsCommand;
 import me.xhawk87.Coinage.listeners.CoinListener;
 import me.xhawk87.Coinage.listeners.MoneyBagListener;
 import me.xhawk87.Coinage.moneybags.MoneyBag;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.Recipe;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitRunnable;
 import org.bukkit.scheduler.BukkitTask;
 
 /**
  *
  * @author XHawk87
  */
 public class Coinage extends JavaPlugin {
 
     private Map<String, Currency> currencies = new HashMap<>();
     private Map<String, MoneyBag> moneybags = new HashMap<>();
     private List<Recipe> moneyBagTypes = new ArrayList<>();
 
     @Override
     /**
      * This method was not meant for you. Using it puts you at serious risk of a
      * slap around the face.
      */
     public void onEnable() {
         saveDefaultConfig();
         reloadConfig();
 
         // Load money bags
         loadMoneyBags();
 
         // Register commands
         getCommand("CoinList").setExecutor(new CoinListCommand(this));
         getCommand("CoinReload").setExecutor(new CoinReloadCommand(this));
         getCommand("CoinValue").setExecutor(new CoinValueCommand(this));
         getCommand("CombineCoins").setExecutor(new CombineCoinsCommand(this));
         getCommand("ConvertCoins").setExecutor(new ConvertCoinsCommand(this));
         getCommand("CreateCurrency").setExecutor(new CreateCurrencyCommand(this));
         getCommand("CreateDenomination").setExecutor(new CreateDenominationCommand(this));
         getCommand("DeleteCurrency").setExecutor(new DeleteCurrencyCommand(this));
         getCommand("DeleteDenomination").setExecutor(new DeleteDenominationCommand(this));
         getCommand("GiveCoins").setExecutor(new GiveCoinsCommand(this));
         getCommand("GiveCurrency").setExecutor(new GiveCurrency(this));
         getCommand("MintCoins").setExecutor(new MintCoinsCommand(this));
         getCommand("SetDefaultCurrency").setExecutor(new SetDefaultCurrencyCommand(this));
         getCommand("SpendCoins").setExecutor(new SpendCoinsCommand(this));
         getCommand("SplitCoins").setExecutor(new SplitCoinsCommand(this));
 
         // Register events
         new CoinListener().registerEvents(this);
         new MoneyBagListener().registerEvents(this);
     }
 
     @Override
     public void onDisable() {
         if (moneyBagSaveTask != null) {
             moneyBagSaveTask.cancel();
             try {
                 moneyBagData.save(moneyBagFile);
             } catch (IOException ex) {
                 getLogger().log(Level.SEVERE, "Could not save moneybags.yml before closing down: \n" + moneyBagData.saveToString(), ex);
             }
         }
     }
 
     /**
      * Gets the default currency to be used in commands
      *
      * @return The default currency
      */
     public Currency getDefaultCurrency() {
         String name = getConfig().getString("default");
         if (name == null) {
             if (!currencies.isEmpty()) {
                 Currency currency = currencies.values().iterator().next();
                 setDefaultCurrency(currency);
                 return currency;
             } else {
                 getLogger().warning("You must create at least one currency!");
                 return null;
             }
         }
         return currencies.get(name);
     }
 
     /**
      * Sets the default currency to be used in commands by its name.
      *
      * @param name The name of the currency
      * @return True if the default was set, False if the currency did not exist
      */
     public boolean setDefaultCurrency(String name) {
         Currency currency = getCurrency(name);
         if (currency == null) {
             return false;
         }
         setDefaultCurrency(currency);
         return true;
     }
 
     /**
      * Sets the default currency to be used in commands.
      *
      * @param currency The currency to use as default
      */
     public void setDefaultCurrency(Currency currency) {
         getConfig().set("default", currency.getName());
         saveConfig();
     }
 
     /**
      * <p>Creates a new currency with the given name and alias.</p>
      *
      * <p>The name must not contain spaces and is used to reference the currency
      * in Coinage commands.</p>
      *
      * <p>The alias may contain spaces and colour codes and is displayed in the
      * item lore before the denomination.</p>
      *
      * @param name The currency name
      * @param alias The currency alias
      * @return The currency created or null if a currency with this name already
      * exists
      */
     public Currency createCurrency(String name, String alias) {
         if (currencies.containsKey(name)) {
             return null;
         }
 
         ConfigurationSection currencySection = getConfig().createSection("currencies." + name);
         currencySection.set("alias", alias);
         Currency currency = new Currency(this, currencySection);
         currencies.put(name, currency);
         saveConfig();
         return currency;
     }
 
     /**
      * Deletes an existing currency by its name. This will cause all
      * denominations of this currency to cease being considered legal tender
      *
      * @param name The currency name
      * @return True if successfully deleted, False if it did not exist
      */
     public boolean deleteCurrency(String name) {
         if (!currencies.containsKey(name)) {
             return false;
         }
 
         currencies.remove(name);
         getConfig().set("currencies." + name, null);
         saveConfig();
         return true;
     }
 
     /**
      * Deletes an existing currency. This will cause all denominations of this
      * currency to cease being considered legal tender
      *
      * @param currency The currency to delete
      */
     public void deleteCurrency(Currency currency) {
         String name = currency.getName();
         currencies.remove(name);
         getConfig().set("currencies." + name, null);
         saveConfig();
     }
 
     /**
      * Gets a currency by its name. This will return null if the currency does
      * not exist
      *
      * @param name The name of the currency
      * @return The currency, or null if it does not exist
      */
     public Currency getCurrency(String name) {
         return currencies.get(name);
     }
 
     /**
      * Gets a currency by the lore on a coin. This will return null if there is
      * no matching currency
      *
      * @param lore The lore of the coin
      * @return The currency, or null if there was no match
      */
     public Currency getCurrencyByLore(String lore) {
         for (Currency currency : currencies.values()) {
             if (currency.matches(lore)) {
                 return currency;
             }
         }
         return null;
     }
 
     /**
      * Returns a list of all currencies. Modifying the list will not affect the
      * registered currencies.
      *
      * @return A list of all currencies
      */
     public List<Currency> getAllCurrencies() {
         return new ArrayList<>(currencies.values());
     }
 
     /**
      * Find the denomination for a given coin
      *
      * @param item The item to check
      * @return The denomination of the coin or null if it is not a coin
      */
     public Denomination getDenominationOfCoin(ItemStack item) {
         if (item == null || item.getTypeId() == 0) {
             return null;
         }
         if (!item.hasItemMeta()) {
             return null;
         }
 
         ItemMeta meta = item.getItemMeta();
 
         if (!meta.hasDisplayName() || !meta.hasLore()) {
             return null;
         }
 
         if (meta.getLore().size() != 1) {
             return null;
         }
 
         String lore = meta.getLore().get(0);
         Currency currency = getCurrencyByLore(lore);
         if (currency == null) {
             return null;
         }
         return currency.getDenominationByLore(lore);
     }
 
     @Override
     public void reloadConfig() {
         super.reloadConfig();
 
         // Clear previous data
         currencies.clear();
         Iterator<Recipe> it = getServer().recipeIterator();
         while (it.hasNext()) {
             if (moneyBagTypes.contains(it.next())) {
                 it.remove();
             }
         }
         moneyBagTypes.clear();
 
         // Load currencies
         ConfigurationSection currencySection = getConfig().getConfigurationSection("currencies");
         for (String key : currencySection.getKeys(false)) {
             Currency currency = new Currency(this, currencySection.getConfigurationSection(key));
             currencies.put(key, currency);
         }
 
         // Load money bag types
         ConfigurationSection moneybagSection = getConfig().getConfigurationSection("moneybags");
         for (String key : moneybagSection.getKeys(false)) {
             Recipe recipe = MoneyBag.loadMoneyBagType(moneybagSection.getConfigurationSection(key));
             getServer().addRecipe(recipe);
             moneyBagTypes.add(recipe);
         }
 
         // Save any formatting changes
         saveConfig();
     }
 
     public boolean isMoneyBag(ItemStack item) {
         if (!item.hasItemMeta()) {
             return false;
         }
         ItemMeta itemMeta = item.getItemMeta();
         if (!itemMeta.hasDisplayName() || !itemMeta.hasLore()) {
             return false;
         }
         for (Recipe moneyBagType : moneyBagTypes) {
             ItemStack result = moneyBagType.getResult();
 
             if (result.getTypeId() == item.getTypeId()
                     && result.getDurability() == item.getDurability()) {
                 ItemMeta resultMeta = result.getItemMeta();
                 if (resultMeta.getDisplayName().equals(itemMeta.getDisplayName())) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     public MoneyBag getMoneyBag(ItemStack item) {
         if (!isMoneyBag(item)) {
             return null;
         }
         ItemMeta meta = item.getItemMeta();
         List<String> loreStrings = meta.getLore();
         if (loreStrings.size() == 1) {
             String lore = loreStrings.get(0);
             String data = MoneyBag.decodeLore(lore);
             if (data.startsWith("moneybag")) { // This is a brand new money bag
                 String[] parts = data.split(",");
                 int size;
                 try {
                     size = Integer.parseInt(parts[1]);
                 } catch (NumberFormatException ex) {
                     return null; // Invalid, no size
                 }
                 String title = meta.getDisplayName();
 
                 String key = UUID.randomUUID().toString();
                 while (moneybags.containsKey(key)) {
                     key = UUID.randomUUID().toString();
                 }
 
                 MoneyBag moneybag = new MoneyBag(this, key, size, title);
                 moneybags.put(key, moneybag);
                 saveMoneyBag(moneybag);
                 return moneybag;
             } else {
                 return moneybags.get(data);
             }
         }
         return null;
     }
     private File moneyBagFile;
     private FileConfiguration moneyBagData;
 
     public void loadMoneyBags() {
         moneyBagFile = new File(getDataFolder(), "moneybags.yml");
         moneyBagData = new YamlConfiguration();
         try {
             moneyBagData.load(moneyBagFile);
         } catch (FileNotFoundException ex) {
             // Ignore
         } catch (IOException | InvalidConfigurationException ex) {
             getLogger().log(Level.SEVERE, "Could not load moneybags.yml", ex);
         }
         for (String key : moneyBagData.getKeys(false)) {
             MoneyBag moneyBag = new MoneyBag(this, key);
             moneyBag.load(moneyBagData.getConfigurationSection(key));
             moneybags.put(key, moneyBag);
         }
     }
     private BukkitTask moneyBagSaveTask;
 
     public void saveMoneyBag(MoneyBag moneybag) {
         // Save changes into memory immediately
         moneybag.save(moneyBagData.createSection(moneybag.getId()));
 
         // If there is currently no save task running
         if (moneyBagSaveTask == null) {
             final Coinage thisPlugin = this;
             // Wait 5 seconds before setting up a save to disk
             moneyBagSaveTask = new BukkitRunnable() {
                 @Override
                 public void run() {
                     // Synchronously copy the data to a string to avoid conflicts
                     final String dataString = moneyBagData.saveToString();
                     new BukkitRunnable() {
                         @Override
                         public void run() {
                             // Save the data string to disk asynchronously
                             if (!moneyBagFile.exists()) {
                                 getDataFolder().mkdirs();
                             }
                             try (FileWriter writer = new FileWriter(moneyBagFile)) {
                                 writer.write(dataString);
                             } catch (IOException ex) {
                                 getLogger().log(Level.SEVERE, "Could not save to moneybags.yml", ex);
                             }
 
                             // Allow a new save task to be started
                             moneyBagSaveTask = null;
                         }
                     }.runTaskAsynchronously(thisPlugin);
                 }
             }.runTaskLater(this, 100);
         }
     }
 }
