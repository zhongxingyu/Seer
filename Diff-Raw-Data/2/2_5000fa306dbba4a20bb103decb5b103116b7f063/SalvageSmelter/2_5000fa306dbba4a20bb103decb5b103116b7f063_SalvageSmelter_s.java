 package com.norcode.bukkit.salvagesmelter;
 
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import net.h31ix.updater.Updater;
 import net.h31ix.updater.Updater.UpdateType;
 
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
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.inventory.FurnaceBurnEvent;
 import org.bukkit.event.inventory.FurnaceSmeltEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryMoveItemEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SalvageSmelter extends JavaPlugin implements Listener {
     private Updater updater;
     @Override
     public void onEnable() {
         saveDefaultConfig();
         loadConfig();
         getServer().getPluginManager().registerEvents(this, this);
     }
     private BlockFace[] fourSides = new BlockFace[] { BlockFace.SOUTH, BlockFace.NORTH, BlockFace.WEST, BlockFace.EAST };
     private EnumSet<Material> signMaterials = EnumSet.of(Material.WALL_SIGN, Material.SIGN_POST);
     private HashMap<FurnaceBurnEvent, Integer> burnTimes = new HashMap<FurnaceBurnEvent, Integer>();
     private HashMap<Material, SmeltRecipe> recipeMap = new HashMap<Material, SmeltRecipe>();
     private boolean worldWhitelist = true; // blacklist if false
     private HashSet<String> worldList = new HashSet<String>();
     private boolean debugMode = false;
     private boolean alwaysYieldFullAmt = false;
 
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
 
     public boolean enabledInWorld(World w) {
         boolean enabled = ((worldWhitelist && worldList.contains(w.getName().toLowerCase())) || 
                 (!worldWhitelist && !worldList.contains(w.getName().toLowerCase())));
         return enabled;
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
         String listtype = getConfig().getString("world-selection", "whitelist").toLowerCase();
         debugMode = getConfig().getBoolean("debug", false);
         alwaysYieldFullAmt = getConfig().getBoolean("always-yield-full-amount", false);
         if (listtype.equals("blacklist")) {
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
         doUpdater();
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command,
             String label, String[] args) {
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
         return false;
     }
 
     @EventHandler(ignoreCancelled=true)
     public void onInventoryMoveItem(InventoryMoveItemEvent event) {
         if (event.getDestination().getHolder() instanceof Furnace) {
             Furnace f = (Furnace) event.getDestination().getHolder();
             if (recipeMap.containsKey(event.getItem().getType())) {
                 if (!enabledInWorld(f.getWorld())) {
                     event.setCancelled(true);
                 } else if (getConfig().getBoolean("require-signs", false)) {
                     if (!isSalvageSmelter(f.getBlock())) {
                         event.setCancelled(true);
                     }
                 }
             }
         }
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
             if (enabledInWorld(event.getBlock().getWorld())) {
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
 
     public boolean hasDD() {
         return getServer().getPluginManager().getPlugin("DiabloDrops") != null;
     }
 
     @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGH)
     public void onSmelt(FurnaceSmeltEvent event) {
         ItemStack orig = event.getSource();
         if (debugMode) {
             getLogger().info("SmeltEvent::Source: " + orig);
         }
 
         if (recipeMap.containsKey(orig.getType())) {
             if (!enabledInWorld(event.getBlock().getWorld())) {
                 event.setCancelled(true);
                 return;
             } else if (getConfig().getBoolean("require-signs", false)) {
                 if (!isSalvageSmelter(event.getBlock())) {
                     event.setCancelled(true);
                 }
             }
         } else {
             return;
         }
 
         double percentage = (orig.getType().getMaxDurability() - orig.getDurability()) / (double) orig.getType().getMaxDurability();
         if (debugMode) {
             getLogger().info("SmeltEvent::Damage:" + orig);
         }
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
             boolean needsUpdate = event.isShiftClick();
             if (event.getRawSlot() == 0 && !event.isShiftClick()) {
                 if (recipeMap.containsKey(event.getCursor().getType())) {
                     if (!enabledInWorld(((Furnace) event.getInventory().getHolder()).getWorld())) {
                         if (debugMode) {
                             getLogger().info("disabled in this world");
                         }
                         event.setCancelled(true);
                         needsUpdate = true;
                     } else if (getConfig().getBoolean("require-signs", false)) {
                         if (!isSalvageSmelter(((Furnace) event.getInventory().getHolder()).getBlock())) {
                             event.setCancelled(true);
                             needsUpdate = true;
                         }
                     }
 
                 }
             }
             if (event.isShiftClick()) {
                 if (recipeMap.containsKey(event.getCurrentItem().getType())) {
                     if (!enabledInWorld(((Furnace) event.getInventory().getHolder()).getWorld())) {
                         if (debugMode) {
                             getLogger().info("disabled in this world");
                         }
                         event.setCancelled(true);
                         needsUpdate = true;
                     } else if (getConfig().getBoolean("require-signs", false)) {
                         if (!isSalvageSmelter(((Furnace) event.getInventory().getHolder()).getBlock())) {
                             event.setCancelled(true);
                             needsUpdate = true;
                         }
                     }
                 }
             }
             if (needsUpdate) {
                 final Player p = (Player) event.getWhoClicked();
                 getServer().getScheduler().runTaskLater(this, new Runnable() {
                     public void run() {
                         p.updateInventory();
                     }
                 }, 0);
                 
             }
         }
     }
 
     private boolean isSalvageSmelter(Block b) {
         BlockFace attachedFace;
         for (BlockFace bf: fourSides) {
             if (signMaterials.contains(b.getRelative(bf).getType())) {
                Sign sign = (Sign) b.getState();
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
 
     @EventHandler(ignoreCancelled=true)
     private void onSignChange(SignChangeEvent event) {
         if (event.getLine(0).equalsIgnoreCase("[SALVAGE]")) {
             if (event.getPlayer().hasPermission("salvagesmelter.createsign")) {
                 event.setLine(0, ChatColor.DARK_BLUE + event.getLine(0));
             }
         }
     }
 
     public ItemStack getSalvage(Material product, Material raw, double damagePct) {
         if (debugMode) {
             getLogger().info("getSalvage(" + product + ", " + raw + ", " + damagePct + ")");
         }
         SmeltRecipe recipe = recipeMap.get(product);
         if (raw.equals(recipe.getResult().getType())) {
             int amt = recipe.getResult().getAmount();
             if (!alwaysYieldFullAmt) {
                 int max = amt;
                 amt = (int)(amt * damagePct);
                 if (debugMode) {
                     getLogger().info("getSalvage::Mathification:" + max + " * " + damagePct + " = " + amt);
                 }
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
 }
