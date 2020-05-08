 package com.norcode.bukkit.salvagesmelter;
 
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import net.h31ix.updater.Updater;
 import net.h31ix.updater.Updater.UpdateType;
 
 import org.apache.commons.lang.Validate;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Furnace;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.inventory.*;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SalvageSmelter extends JavaPlugin implements Listener {
 
     private Updater updater;
     private BlockFace[] fourSides = new BlockFace[] { BlockFace.SOUTH, BlockFace.NORTH, BlockFace.WEST, BlockFace.EAST };
     private EnumSet<Material> signMaterials = EnumSet.of(Material.WALL_SIGN, Material.SIGN_POST);
     private HashMap<FurnaceBurnEvent, Integer> burnTimes = new HashMap<FurnaceBurnEvent, Integer>();
     private HashMap<Material, SmeltRecipe> recipeMap = new HashMap<Material, SmeltRecipe>();
     private boolean worldWhitelist = true; // blacklist if false
     private HashSet<String> worldList = new HashSet<String>();
     private boolean debugMode = false;
     private boolean alwaysYieldFullAmt = false;
     private boolean requireSigns;
 
     @Override
     public void onEnable() {
         saveDefaultConfig();
         loadConfig();
         getServer().getPluginManager().registerEvents(this, this);
     }
 
     public void doUpdater() {
         String autoUpdate = getConfig().getString("auto-update", "notify-only").toLowerCase();
         if (autoUpdate.equals("true")) {
             updater = new Updater(this, "salvagesmelter", this.getFile(), UpdateType.DEFAULT, true);
         } else if (autoUpdate.equals("false")) {
             getLogger().info("Auto-updater is disabled.  Skipping check.");
         } else {
             updater = new Updater(this, "salvagesmelter", this.getFile(), UpdateType.NO_DOWNLOAD, true);
         }
     }
 
     public ItemStack parseResultStack(String s) {
         String[] parts = s.split(":");
         Material mat = Material.valueOf(parts[0].toUpperCase());
         short data = 0;
         int qty = 1;
         if (parts.length == 3) {
             data = Short.parseShort(parts[1]);
             qty = Integer.parseInt(parts[2]);
         } else {
             qty = Integer.parseInt(parts[1]);
         }
         if (qty > mat.getMaxStackSize()) {
             getLogger().warning("Recipe Result Cannot exceed Max stack size, setting to " + mat.getMaxStackSize());
             qty = mat.getMaxStackSize();
         }
         return new ItemStack(mat, qty, data);
     }
 
     @EventHandler(ignoreCancelled=true)
     public void onPlayerLogin(PlayerLoginEvent event) {
         if (updater == null) {
             // Auto updater is disabled, no need to notify.
             return;
         }
         if (event.getPlayer().hasPermission("salvagesmelter.admin")) {
             final String playerName = event.getPlayer().getName();
             getServer().getScheduler().runTaskLaterAsynchronously(this, new Runnable() {
                 public void run() {
                     Player player = getServer().getPlayer(playerName);
                     if (player != null && player.isOnline()) {
                         switch (updater.getResult()) {
                         case UPDATE_AVAILABLE:
                             player.sendMessage("A new version of SalvageSmelter is available at http://dev.bukkit.org/bukkit-mods/salvagesmelter/");
                             break;
                         case SUCCESS:
                             player.sendMessage("A new version of SalvageSmelter has been downloaded and will take effect when the server restarts.");
                             break;
                         default:
                             // nothing
                         }
                     }
                 }
             }, 20);
         }
     }
 
     public void loadConfig() {
         if (!getConfig().contains("auto-update")) {
             getConfig().set("auto-update", true);
             saveConfig();
         }
         if (!getConfig().contains("require-signs")) {
             getConfig().set("require-signs", false);
             saveConfig();
         }
 
         String listType = getConfig().getString("world-selection", "whitelist").toLowerCase();
         debugMode = getConfig().getBoolean("debug", false);
         alwaysYieldFullAmt = getConfig().getBoolean("always-yield-full-amount", false);
         requireSigns = getConfig().getBoolean("require-signs", false);
 
         if (listType.equals("blacklist")) {
             this.worldWhitelist = false;
         } else {
             this.worldWhitelist = true;
         }
         this.worldList.clear();
         for (String wn: getConfig().getStringList("world-list")) {
             this.worldList.add(wn.toLowerCase());
         }
         ConfigurationSection cfg = getConfig().getConfigurationSection("recipes");
         for (String key: cfg.getKeys(true)) {
             Material mat = Material.valueOf(key);
             ItemStack result = parseResultStack(cfg.getString(key));
             if (mat != null && result != null) {
                 SmeltRecipe sr = new SmeltRecipe(mat, result);
                 sr.installFurnaceRecipe(this);
                 recipeMap.put(sr.getSmeltable(), sr);
             }
         }
         //Optional: Recipes which have a group assigned and require the groups permission to be used
         if (getConfig().contains("recipe-groups")) {
             ConfigurationSection recipeGroups = getConfig().getConfigurationSection("recipe-groups");
             for (String group : recipeGroups.getKeys(true)) {
                 for (String matStr: recipeGroups.getStringList(group)) {
                     Material mat = Material.valueOf(matStr);
                     if (mat != null) {
                         //Add the group to the existing recipe
                         if (recipeMap.containsKey(mat)) {
                             recipeMap.get(mat).setGroup(group);
                         }
                     }
                 }
             }
         }
         doUpdater();
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (args.length > 0) {
             if (args[0].equalsIgnoreCase("reload")) {
                 reloadConfig();
                 loadConfig();
                 sender.sendMessage(ChatColor.GOLD + "[SalvageSmelter] " + ChatColor.WHITE + "Configuration Reloaded.");
                 return true;
             } else if (args[0].equalsIgnoreCase("debug")) {
                 debugMode = !debugMode;
                 sender.sendMessage(ChatColor.GOLD + "[SalvageSmelter] " + ChatColor.WHITE + "Debug mode is now " + (debugMode ? ChatColor.DARK_GREEN + "on" : ChatColor.DARK_RED + "off") + ".");
                 return true;
             }
         }
         return false;
     }
 
     @EventHandler(ignoreCancelled=true)
     public void onInventoryMoveItem(InventoryMoveItemEvent event) {
         if (event.getDestination().getHolder() instanceof Furnace) {
             Furnace f = (Furnace) event.getDestination().getHolder();
             if (recipeMap.containsKey(event.getItem().getType())) {
                 if (recipeMap.get(event.getItem().getType()).hasGroup() || !isSalvageSmelter(f.getBlock())) {
                     event.setCancelled(true);
                     return;
                 }
             }
         }
     }
 
     /**
      * Check if the server is currently Running DiabloDrops
      *
      * DiabloDrops (http://dev.bukkit.org/bukkit-plugins/diablodrops/) does strange
      * things with the FurnaceBurnEvent which prevent SalvageSmelter from doing it's
      * business without the strange work-around seen in onEarlyBurn and onLateBurn
      *
      * @return true if DiabloDrops is installed, false otherwise.
      */
     public boolean hasDD() {
         return getServer().getPluginManager().getPlugin("DiabloDrops") != null;
     }
 
 
     @EventHandler(ignoreCancelled=true, priority=EventPriority.LOWEST)
     public void onEarlyBurn(FurnaceBurnEvent event) {
         if (hasDD() && !event.isCancelled()) {
             burnTimes.put(event, event.getBurnTime());
         }
     }
 
     @EventHandler(ignoreCancelled=false, priority=EventPriority.HIGHEST)
     public void onLateBurn(FurnaceBurnEvent event) {
         Integer burnTime = burnTimes.remove(event);
         if (burnTime != null && event.isCancelled()) {
             if (isSalvageSmelter(event.getBlock())) {
                 Furnace furnace  = (Furnace) event.getBlock().getState();
                 ItemStack stack = furnace.getInventory().getSmelting();
                 if (recipeMap.containsKey(stack.getType())) {
                     event.setCancelled(false);
                     event.setBurning(true);
                     event.setBurnTime(burnTime);
                 }
             }
         }
     }
     @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGH)
     public void onSmelt(FurnaceSmeltEvent event) {
         ItemStack orig = event.getSource();
 
         if (!isSalvageSmelter(event.getBlock())) {
             return;
         }
 
         if (!recipeMap.containsKey(orig.getType())) {
             return;
         }
 
         double percentage = (orig.getType().getMaxDurability() - orig.getDurability()) / (double) orig.getType().getMaxDurability();
         if (Double.isNaN(percentage)) {
            percentage = 1.0D;
         }
         debug("SmeltEvent::Damage:" + percentage);
         ItemStack result = getSalvage(orig.getType(), event.getResult().getType(), percentage);
         if (result == null || result.getAmount() == 0) {
             event.setResult(new ItemStack(Material.COAL, 1, (short)1));
         } else {
             event.setResult(result);
         }
     }
 
     @EventHandler(ignoreCancelled=true)
     public void onInventoryClick(InventoryClickEvent event) {
         if (event.getInventory().getType().equals(InventoryType.FURNACE)) {
             if (event.isShiftClick() || event.getRawSlot() == 0) {
                 Material item = event.isShiftClick() ? event.getCurrentItem().getType() : event.getCursor().getType();
                 boolean needsUpdate = event.isShiftClick();
                 boolean cancelled = !canInsert(item, event.getWhoClicked(), ((Furnace) event.getInventory().getHolder()).getBlock());
                 event.setCancelled(cancelled);
 
                 if (needsUpdate || cancelled) {
                     final Player p = (Player) event.getWhoClicked();
                     getServer().getScheduler().runTaskLater(this, new Runnable() {
                         public void run() {
                             p.updateInventory();
                         }
                     }, 0);
 
                 }
             }
         }
     }
 
 
     /**
      * Called when a Player drags an item in his inventory (since 1.5.2-1.0).
      * When this Event is called InventoryClickEvent won't be called.
      */
     @EventHandler(ignoreCancelled = true)
     public void onInventoryDrag(InventoryDragEvent event){
         if (event.getInventory().getType().equals(InventoryType.FURNACE)) {
             Set<Integer> affectedSlots = event.getRawSlots();
             if (affectedSlots.contains(0)) {
                 boolean cancelled = !canInsert(event.getNewItems().get(0).getType(), event.getWhoClicked(), ((Furnace) event.getInventory().getHolder()).getBlock());
                 event.setCancelled(cancelled);
                 if (cancelled) {
                     final Player p = (Player) event.getWhoClicked();
                     getServer().getScheduler().runTaskLater(this, new Runnable() {
                         public void run() {
                             p.updateInventory();
                         }
                     }, 0);
 
                 }
             }
         }
 
     }
 
     /**
      * Check if SalvageSmelter is enabled in this world.
      *
      * @param world the world to check.
      * @return true if SalvageSmelter is enabled in this world, false otherwise.
      */
     public boolean enabledInWorld(World world) {
         return ((worldWhitelist && worldList.contains(world.getName().toLowerCase())) ||
                 (!worldWhitelist && !worldList.contains(world.getName().toLowerCase())));
     }
 
     /**
      * Can a material be inserted into a furnace
      *
      * @param item         item to check for
      * @param human        HumanEntity that tried to insert an item. Can be null if not inserted by a HumanEntity.
      * @param furnaceBlock actual furnace block. We check for signs in the near vicinity.
      *
      * @return if the item can be inserted
      */
     private boolean canInsert(Material item, HumanEntity human, Block furnaceBlock) {
         Validate.notNull(item); Validate.notNull(furnaceBlock);
         if (isSalvageSmelter(furnaceBlock)) {
             if (recipeMap.containsKey(item) && recipeMap.get(item).hasGroup()) {
                 if (!human.hasPermission("salvagesmelter.group." + recipeMap.get(item).getGroup())) {
                     return false;
                 }
             }
             return true;
         } else {
             return !recipeMap.containsKey(item);
         }
     }
 
     /**
      * Check if the given block is a SalvageSmelter.
      *
      * @param furnaceBlock a block to test.
      *
      * @return if the item can be inserted
      */
     private boolean isSalvageSmelter(Block furnaceBlock) {
         if (!enabledInWorld(furnaceBlock.getWorld())) {
             return false;
         } else if (requireSigns) {
             BlockFace attachedFace;
             for (BlockFace bf: fourSides) {
                 if (signMaterials.contains(furnaceBlock.getRelative(bf).getType())) {
                     Sign sign = (Sign) furnaceBlock.getRelative(bf).getState();
                     attachedFace = ((org.bukkit.material.Sign)sign.getData()).getAttachedFace();
                     if (attachedFace.equals(bf.getOppositeFace())) {
                         if (sign.getLine(0).equalsIgnoreCase(ChatColor.DARK_BLUE + "[SALVAGE]")) {
                             return true;
                         }
                     }
                 }
             }
             return false;
         }
         return true;
     }
 
     @EventHandler(ignoreCancelled=true)
     private void onSignChange(SignChangeEvent event) {
         if (event.getLine(0).equalsIgnoreCase("[SALVAGE]")) {
             if (event.getPlayer().hasPermission("salvagesmelter.createsign")) {
                 event.setLine(0, ChatColor.DARK_BLUE + event.getLine(0));
             }
         }
     }
 
     /**
      * Calculate the results of a salvage.
      *
      * @param originalMaterial - The material placed in the top furnace slot.
      * @param rawMaterial - the material of the result of the smelting event
      * @param damagePct - the percentage of damage done to the item.
      * @return the exact results of smelting originalMaterial into rawMaterial
      */
     public ItemStack getSalvage(Material originalMaterial, Material rawMaterial , double damagePct) {
         debug("getSalvage(" + originalMaterial + ", " + rawMaterial + ", " + damagePct + ")");
         SmeltRecipe recipe = recipeMap.get(originalMaterial);
         if (rawMaterial.equals(recipe.getResult().getType())) {
             int amt = recipe.getResult().getAmount();
             if (!alwaysYieldFullAmt) {
                 int max = amt;
                 amt = (int)(amt * damagePct);
                 debug("getSalvage::Mathification:" + max + " * " + damagePct + " = " + amt);
             }
             ItemStack stack = recipe.getResult().clone();
             if (amt == 0) {
                 stack = new ItemStack(Material.COAL,1,(short)1);
             } else {
                 stack.setAmount(amt);
             }
             return stack;
         }
         return null;
     }
 
     public void debug(String s) {
         if (debugMode) {
             getLogger().info(s);
         }
     }
 }
