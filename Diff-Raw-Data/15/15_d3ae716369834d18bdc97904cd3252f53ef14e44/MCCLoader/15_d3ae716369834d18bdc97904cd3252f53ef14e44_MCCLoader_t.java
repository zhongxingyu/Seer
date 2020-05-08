 package me.DDoS.MCCasino;
 
 import me.DDoS.MCCasino.util.MCCSerializableLocation;
 import me.DDoS.MCCasino.bet.MCCBetProvider;
 import me.DDoS.MCCasino.slotmachine.MCCReel;
 import me.DDoS.MCCasino.slotmachine.MCCReelValue;
 import me.DDoS.MCCasino.slotmachine.MCCReward;
 import me.DDoS.MCCasino.slotmachine.MCCSlotMachine;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import me.DDoS.MCCasino.bet.MCCItemBetProvider;
 import me.DDoS.MCCasino.bet.MCCMoneyBetProvider;
 import net.milkbowl.vault.economy.Economy;
 import org.bukkit.Location;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 /**
  *
  * @author DDoS
  */
 @SuppressWarnings("unchecked")
 public class MCCLoader {
 
     private Map<String, List<MCCSerializableLocation>> machines;
     private MCCasino plugin;
     private FileConfiguration config;
     
     public MCCLoader(MCCasino plugin, FileConfiguration config) {
         
         this.plugin = plugin;
         this.config = config;
         
     }
     
     public MCCLoader(MCCasino plugin) {
         
         this.plugin = plugin;
         
     }
 
     public void loadSlotMachines() {
 
         loadSlotMachinesFile();
 
         try {
 
             config.load("plugins/MCCasino/config.yml");
 
         } catch (Exception e) {
 
             MCCasino.log.info("[MCCasino] Couldn't load the config.");
             MCCasino.log.info("[MCCasino] Error: " + e.getMessage());
             
         }
 
         Set<String> machineNames = config.getConfigurationSection("Machines").getKeys(false);
 
         for (String machineName : machineNames) {
 
             List<Location> signs = getMachineSigns(machineName);
             List<MCCReel> reels = loadReels(machineName);
             List<MCCReward> rewards = loadRewards(machineName);
             MCCBetProvider betHandler = loadBetProvider(machineName);
 
             boolean active = true;
 
             if (signs == null || signs.size() < reels.size()) {
 
                 active = false;
 
             }
 
             MCCasino.log.info("[MCCasino] Loaded slot machine: " + machineName);
             plugin.addMachine(machineName, new MCCSlotMachine(signs, reels, rewards, betHandler, active, plugin));
 
         }
     }
 
     public List<MCCReel> loadReels(String machineName) {
 
         int numOfReels = config.getConfigurationSection("Machines." + machineName + ".reels").getKeys(false).size();
         List<MCCReel> reels = new ArrayList<MCCReel>();
 
         for (int i2 = 0; i2 < numOfReels; i2++) {
 
            List<String> reelValues = config.getStringList("Machines." + machineName + ".reels." + (i2 + 1));
             List<MCCReelValue> rvs = new ArrayList<MCCReelValue>();
 
             for (String reelValue : reelValues) {
 
                 String[] values = reelValue.split("-");
                 rvs.add(new MCCReelValue(new ItemStack(Integer.parseInt(values[0]), 0), Integer.parseInt(values[1])));
 
             }
 
             if (!rvs.isEmpty()) {
 
                 reels.add(new MCCReel(rvs));
 
             }
         }
 
         return reels;
 
     }
 
     private List<MCCReward> loadRewards(String machineName) {
 
        List<String> rewards = config.getStringList("Machines." + machineName + ".rewards");
         List<MCCReward> rewardsList = new ArrayList<MCCReward>();
 
         for (String reward : rewards) {
 
             String[] rewardSplitted = reward.split(":");
             String[] resultsString = rewardSplitted[0].split(",");
             int multiplier = Integer.parseInt(rewardSplitted[1]);
             List<Integer> results = new ArrayList<Integer>();
 
             for (String resultString : resultsString) {
 
                 int ID = (resultString.equalsIgnoreCase("x")) ? -1 : Integer.parseInt(resultString);
                 results.add(ID);
 
             }
 
             rewardsList.add(new MCCReward(results, multiplier));
 
         }
 
         return rewardsList;
 
     }
 
     private MCCBetProvider loadBetProvider(String machineName) {
 
         if (config.getBoolean("Machines." + machineName + ".economy.use_economy") && plugin.hasEconomy()) {
             
             RegisteredServiceProvider<Economy> economyProvider =
                     plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
             return new MCCMoneyBetProvider(config.getInt("Machines." + machineName + ".economy.cost"), economyProvider.getProvider());
 
         } else {
 
             List<ItemStack> limitedItems = new ArrayList<ItemStack>();
             
             if (config.getBoolean("Machines." + machineName + ".bet_limits.enabled")) {
 
                List<String> limits = config.getStringList("Machines." + machineName + ".bet_limits.allowed");
 
                 for (String limit : limits) {
 
                     String[] s1 = limit.split("-");
                     limitedItems.add(new ItemStack(Integer.parseInt(s1[0]), Integer.parseInt(s1[1])));
 
                 }
             }
 
             return new MCCItemBetProvider(limitedItems);
 
         }
     }
 
     public void loadSlotMachinesFile() {
 
         checkSlotMachineFile();
 
         try {
 
             ObjectInputStream ois = new ObjectInputStream(new FileInputStream("plugins/MCCasino/slotMachines.dat"));
             Object dispensersLoad = ois.readObject();
             machines = (HashMap<String, List<MCCSerializableLocation>>) dispensersLoad;
 
         } catch (Exception e) {
 
             MCCasino.log.info("[MCCasino] Couldn't load the slot machine file.");
             MCCasino.log.info("[MCCasino] Error: " + e.getMessage());
             machines = new HashMap<String, List<MCCSerializableLocation>>();
 
         }
     }
 
     private List<Location> getMachineSigns(String machineName) {
 
         List<MCCSerializableLocation> sLocs = machines.get(machineName);
 
         if (sLocs == null) {
 
             return new ArrayList<Location>();
 
         }
 
         List<Location> locs = new ArrayList<Location>();
 
         for (MCCSerializableLocation sLoc : sLocs) {
 
             Location location = sLoc.getLocation(plugin.getServer());
             locs.add(location);
 
         }
 
         return locs;
 
     }
 
     public void checkSlotMachineFile() {
 
         File dipensersFile = new File("plugins/MCCasino/slotMachines.dat");
 
         if (!dipensersFile.exists()) {
 
             try {
 
                 dipensersFile.createNewFile();
 
             } catch (Exception e) {
 
                 MCCasino.log.info("[MCCasino] Couldn't create the slot machine file.");
                 MCCasino.log.info("[MCCasino] Error: " + e.getMessage());
                 
             }
         }
     }
 
     public void saveMachines() {
 
         Map<String, List<MCCSerializableLocation>> signs = new HashMap<String, List<MCCSerializableLocation>>();
 
         for (Entry<String, MCCSlotMachine> entry : plugin.getMachineEntries()) {
 
             List<Location> locs = entry.getValue().getReels();
             List<MCCSerializableLocation> fLocs = new ArrayList<MCCSerializableLocation>();
 
             for (Location loc : locs) {
 
                 fLocs.add(new MCCSerializableLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName()));
 
             }
 
             signs.put(entry.getKey(), fLocs);
 
         }
 
         try {
 
             ObjectOutputStream hashfile = new ObjectOutputStream(new FileOutputStream("plugins/MCCasino/slotMachines.dat"));
             hashfile.writeObject(signs);
             hashfile.flush();
             hashfile.close();
             MCCasino.log.info("[MCCasino] Machines saved.");
 
         } catch (Exception e) {
 
             MCCasino.log.info("[MCCasino] Couldn't save the slot machine file.");
             MCCasino.log.info("[MCCasino] Error: " + e.getMessage());
 
         }
     }
 }
