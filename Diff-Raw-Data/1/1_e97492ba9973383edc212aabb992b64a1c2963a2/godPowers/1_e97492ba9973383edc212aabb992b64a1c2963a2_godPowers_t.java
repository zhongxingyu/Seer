 package com.hydrasmp.godPowers;
 
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitTask;
 import org.bukkit.util.Vector;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 
 public class godPowers extends JavaPlugin {
     @SuppressWarnings("unused")
     private Logger log;
     public String title = "";
     private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
     public HashMap<String, Integer> curse = new HashMap<String, Integer>();
     public ArrayList<String> godmodeEnabled = new ArrayList<String>();
     public ArrayList<String> isJesus = new ArrayList<String>();
     public ArrayList<MedusaPlayer> isUnderMedusaInfluence = new ArrayList<MedusaPlayer>();
     public ArrayList<String> isInferno = new ArrayList<String>();
     public ArrayList<String> isHermes = new ArrayList<String>();
     public ArrayList<String> isPoseidon = new ArrayList<String>();
     public ArrayList<String> isMedusa = new ArrayList<String>();
     public ArrayList<String> superJumper = new ArrayList<String>();
     public ArrayList<String> arrowKill = new ArrayList<String>();
     public ArrayList<String> burn = new ArrayList<String>();
     public ArrayList<String> gaia = new ArrayList<String>();
     public ArrayList<String> isZeus = new ArrayList<String>();
     public ArrayList<String> isVulcan = new ArrayList<String>();
     public ArrayList<String> DemiGod = new ArrayList<String>();
     public ArrayList<String> hades = new ArrayList<String>();
     public ArrayList<Integer> shovelDrops = new ArrayList<Integer>();
     public ArrayList<Integer> pickDrops = new ArrayList<Integer>();
     public ArrayList<Integer> axeDrops = new ArrayList<Integer>();
     public HashMap<String, String> list = new HashMap<String, String>();
     public double DemiModifier = 0;
     public boolean godModeOnLogin = true;
     public boolean godTools = true;
     public int medusaFreezeTime = 10;
     public File file;
 
     public void loadConfig() {
         try {
             this.saveDefaultConfig();
             if (!this.getConfig().contains("GodModeTitle"))
                 this.getConfig().set("GodModeTitle", "[God] ");
             if (!this.getConfig().contains("GodModeOnLogin"))
                 this.getConfig().set("GodModeOnLogin", true);
             if (!this.getConfig().contains("DemiGodDamageModifier"))
                 this.getConfig().set("DemiGodDamageModifier", 0.2);
             if (!this.getConfig().contains("GodToolsEnabled"))
                 this.getConfig().set("GodToolsEnabled", true);
             if (!this.getConfig().contains("MedusaFreezeTime"))
                 this.getConfig().set("MedusaFreezeTime", 10);
             title = this.getConfig().getString("GodModeTitle", "");
             godModeOnLogin = this.getConfig().getBoolean("GodModeOnLogin", true);
             DemiModifier = this.getConfig().getDouble("DemiGodDamageModifier", 0.2);
             godTools = this.getConfig().getBoolean("GodToolsEnabled", true);
             medusaFreezeTime = this.getConfig().getInt("MedusaFreezeTime", 10);
             this.saveConfig();
         } catch (Exception e) {
            System.out.println("[GodPowers] Error loading config file.");
 
         }
     }
 
     public void onDisable() {
     }
 
     @Override
     public void onEnable() {
         file = this.getFile();
         loadConfig();
         @SuppressWarnings("unused")
         BukkitTask TaskName = new OnOneSecond(this).runTaskTimer(this, 25, 25);
         String error = "[GodPowers] ERROR another plugin has already taken the command ";
         try {
             getCommand("zeus").setExecutor(new ZeusCommand(this));
             list.put("zeus", "- Strike lightning with a swing of your arm!");
         } catch (Exception e) {
             System.out.println(error + "zeus.");
         }
         try {
             getCommand("godmode").setExecutor(new godModeCommand(this));
             list.put("godmode", "<Player> - Toggles godmode on and off.");
         } catch (Exception e) {
             System.out.println(error + "godmode.");
         }
         getCommand("godmodeon").setExecutor(new godModeCommand(this));
         getCommand("godmodeoff").setExecutor(new godModeCommand(this));
         try {
             getCommand("jesus").setExecutor(new JesusCommand(this));
             list.put("jesus", "<Player> - Allows you to walk on water and lava");
         } catch (Exception e) {
             System.out.println(error + "jesus.");
         }
         try {
             getCommand("die").setExecutor(new DieCommand(this));
             list.put("die", "- Causes you to die.");
         } catch (Exception e) {
             System.out.println(error + "die.");
         }
         try {
             getCommand("slay").setExecutor(new SlayCommand(this));
             list.put("slay", "[Player] <arrows/fire/drop> - Kills a player with/without the optional method.");
         } catch (Exception e) {
             System.out.println(error + "slay.");
             try {
                 getCommand("smite").setExecutor(new SlayCommand(this));
                 System.out.println(error + "slay. " + "Registered smite in place of slay.");
                 list.put("smite", "[Player] <arrows/fire/drop> - Kills a player with/without the optional method.");
             } catch (Exception e1) {
                 System.out.println(error + "smite in place of slay.");
             }
         }
         try {
             getCommand("maim").setExecutor(new MaimCommand(this));
             list.put("maim", "[Player] - Beat a player within an inch of their life!");
         } catch (Exception e) {
             System.out.println(error + "maim.");
         }
         try {
             getCommand("inferno").setExecutor(new InfernoCommand(this));
             list.put("inferno", "- Creates a trail of fire behind you.");
         } catch (Exception e) {
             System.out.println(error + "inferno.");
         }
         try {
             getCommand("superjump").setExecutor(new SuperJumpCommand(this));
             list.put("superjump", "- Be able to leap tall building in a single bound!");
         } catch (Exception e) {
             System.out.println(error + "superjump.");
         }
         try {
             getCommand("gaia").setExecutor(new GaiaCommand(this));
             list.put("gaia", "- Sprouts grass and flowers wherever you step.");
         } catch (Exception e) {
             System.out.println(error + "gaia.");
         }
         try {
             getCommand("heal").setExecutor(new HealCommand(this));
             list.put("heal", "<Player> - Heals either you or the specified player.");
         } catch (Exception e) {
             System.out.println(error + "heal.");
         }
         try {
             getCommand("godpowers").setExecutor(new GodPowersCommand(this));
             list.put("godpowers", "- Displays this message.");
         } catch (Exception e) {
             System.out.println(error + "godpowers. How dare they!");
         }
         try {
             getCommand("vulcan").setExecutor(new VulcanCommand(this));
             list.put("vulcan", "- Fling fireballs at those pesky mortals!");
         } catch (Exception e) {
             System.out.println(error + "vulcan.");
         }
         try {
             getCommand("demigod").setExecutor(new DemiGodCommand(this));
             list.put("demigod", "- Allows you to take a small fraction of the damage you'd normally take.");
         } catch (Exception e) {
             System.out.println(error + "demigod.");
         }
         try {
             getCommand("hades").setExecutor(new HadesCommand(this));
             list.put("hades", "- Corrupt the world as you walk through it.");
         } catch (Exception e) {
             System.out.println(error + "hades.");
         }
         try {
             getCommand("bless").setExecutor(new BlessCommand(this));
             list.put("bless [player]", "- Enchant your equipment with the power of gods!");
         } catch (Exception e) {
             System.out.println(error + "bless.");
         }
         try {
             getCommand("fusrodah").setExecutor(new FusRoDAH(this));
             list.put("FusRoDAH", "- Enchants item in hand with Knockback level 10!");
         } catch (Exception e) {
             System.out.println(error + "FusRoDAH.");
         }
         try {
             getCommand("plutus").setExecutor(new PlutusCommand(this));
             list.put("plutus", "- Enchants item in hand with Wealth level 10!");
         } catch (Exception e) {
             System.out.println(error + "plutus");
         }
         try {
             getCommand("dupe").setExecutor(new DupeCommand(this));
             list.put("dupe <amount>", "- Use your godly powers to create an exact replica of the item you hold!");
         } catch (Exception e) {
             System.out.println(error + "dupe.");
         }
         try {
             getCommand("medusa").setExecutor(new MedusaCommand(this));
             list.put("medusa", "- Become cursed by the gods, and turn anyone you look at to stone.");
         } catch (Exception e) {
             System.out.println(error + "medusa.");
         }
         try {
             getCommand("hermes").setExecutor(new hermesCommand(this));
             list.put("hermes", "- Gives you the speed and ability to send letters.");
         } catch (Exception e) {
             System.out.println(error + "hermes. ");
         }
         try {
             getCommand("poseidon").setExecutor(new poseidonCommand(this));
             list.put("poseidon", "- Gives you Poseidon like powers while in water.");
         } catch (Exception e) {
             System.out.println(error + "poseidon.");
         }
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvents(new godPowersEntityListener(this), this);
         pm.registerEvents(new godPowersPlayerListener(this), this);
         PluginDescriptionFile pdfFile = this.getDescription();
         System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
         populateLists();
     }
 
     public void populateLists() {
         shovelDrops.add(2);
         shovelDrops.add(3);
         shovelDrops.add(12);
         shovelDrops.add(13);
         shovelDrops.add(82);
         pickDrops.add(1);
         pickDrops.add(4);
         pickDrops.add(14);
         pickDrops.add(15);
         pickDrops.add(16);
         pickDrops.add(21);
         pickDrops.add(22);
         pickDrops.add(24);
         pickDrops.add(41);
         pickDrops.add(42);
         pickDrops.add(43);
         pickDrops.add(44);
         pickDrops.add(45);
         pickDrops.add(48);
         pickDrops.add(49);
         pickDrops.add(56);
         pickDrops.add(57);
         axeDrops.add(5);
         axeDrops.add(17);
 
     }
 
     public boolean isDebugging(final Player player) {
         if (debugees.containsKey(player)) {
             return debugees.get(player);
         } else {
             return false;
         }
     }
 
     public void setDebugging(final Player player, final boolean value) {
         debugees.put(player, value);
     }
 
     void dropDeadItems(Player player) {
         if (player.getInventory() != null) {
             ItemStack[] item = player.getInventory().getContents();
             Location position = new Location(player.getWorld(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
             for (int x = 0; x < item.length; x++) {
                 if (item[x] != null && item[x].getTypeId() != 0) {
                     player.getWorld().dropItemNaturally(position, item[x]);
                 }
             }
         }
     }
 
     public void recordEvent(PlayerLoginEvent event) {
 
     }
 
     public void arrowSlay(Location arrows, World world, Player player) {
         arrows = new Location(world, player.getLocation().getX() + 2, player.getLocation().getY() + 1, player.getLocation().getZ() + 2);
         Arrow arrow = world.spawnArrow(arrows, new Vector(5, 1, 5), 2.0f, 4.0f);
         arrow.setFireTicks(100);
         arrow.teleport((Entity) player);
     }
 
     public void bless(Player p) {
         for (ItemStack i : p.getInventory().getContents()) {
             if (i != null) {
                 switch (i.getTypeId()) {
                     // Iron Shovel
                     case 256:
                         i.addEnchantment(Enchantment.DIG_SPEED, 5);
                         i.addEnchantment(Enchantment.DURABILITY, 3);
                         i.addEnchantment(Enchantment.SILK_TOUCH, 1);
                         i.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3);
                         break;
                     // Iron Pickaxe
                     case 257:
                         i.addEnchantment(Enchantment.DIG_SPEED, 5);
                         i.addEnchantment(Enchantment.DURABILITY, 3);
                         i.addEnchantment(Enchantment.SILK_TOUCH, 1);
                         i.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3);
                         break;
                     // Iron Axe
                     case 258:
                         i.addEnchantment(Enchantment.DIG_SPEED, 5);
                         i.addEnchantment(Enchantment.DURABILITY, 3);
                         i.addEnchantment(Enchantment.SILK_TOUCH, 1);
                         i.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3);
                         break;
                     // Bow
                     case 261:
                         i.addEnchantment(Enchantment.ARROW_DAMAGE, 5);
                         i.addEnchantment(Enchantment.ARROW_FIRE, 1);
                         i.addEnchantment(Enchantment.ARROW_KNOCKBACK, 2);
                         i.addEnchantment(Enchantment.ARROW_INFINITE, 1);
                         break;
                     // Iron Sword
                     case 267:
                         i.addEnchantment(Enchantment.DAMAGE_ALL, 5);
                         i.addEnchantment(Enchantment.DAMAGE_ARTHROPODS, 5);
                         i.addEnchantment(Enchantment.DAMAGE_UNDEAD, 5);
                         i.addEnchantment(Enchantment.FIRE_ASPECT, 2);
                         i.addEnchantment(Enchantment.KNOCKBACK, 2);
                         i.addEnchantment(Enchantment.LOOT_BONUS_MOBS, 3);
                         break;
                     // Wooden Sword
                     case 268:
                         i.addEnchantment(Enchantment.DAMAGE_ALL, 5);
                         i.addEnchantment(Enchantment.DAMAGE_ARTHROPODS, 5);
                         i.addEnchantment(Enchantment.DAMAGE_UNDEAD, 5);
                         i.addEnchantment(Enchantment.FIRE_ASPECT, 2);
                         i.addEnchantment(Enchantment.KNOCKBACK, 2);
                         i.addEnchantment(Enchantment.LOOT_BONUS_MOBS, 3);
                         break;
                     // Wooden Shovel
                     case 269:
                         i.addEnchantment(Enchantment.DIG_SPEED, 5);
                         i.addEnchantment(Enchantment.DURABILITY, 3);
                         i.addEnchantment(Enchantment.SILK_TOUCH, 1);
                         i.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3);
                         break;
                     // Wooden Pickaxe
                     case 270:
                         i.addEnchantment(Enchantment.DIG_SPEED, 5);
                         i.addEnchantment(Enchantment.DURABILITY, 3);
                         i.addEnchantment(Enchantment.SILK_TOUCH, 1);
                         i.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3);
                         break;
                     // Wooden Axe
                     case 271:
                         i.addEnchantment(Enchantment.DIG_SPEED, 5);
                         i.addEnchantment(Enchantment.DURABILITY, 3);
                         i.addEnchantment(Enchantment.SILK_TOUCH, 1);
                         i.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3);
                         break;
                     // Stone Sword
                     case 272:
                         i.addEnchantment(Enchantment.DAMAGE_ALL, 5);
                         i.addEnchantment(Enchantment.DAMAGE_ARTHROPODS, 5);
                         i.addEnchantment(Enchantment.DAMAGE_UNDEAD, 5);
                         i.addEnchantment(Enchantment.FIRE_ASPECT, 2);
                         i.addEnchantment(Enchantment.KNOCKBACK, 2);
                         i.addEnchantment(Enchantment.LOOT_BONUS_MOBS, 3);
                         break;
                     // Stone Shovel
                     case 273:
                         i.addEnchantment(Enchantment.DIG_SPEED, 5);
                         i.addEnchantment(Enchantment.DURABILITY, 3);
                         i.addEnchantment(Enchantment.SILK_TOUCH, 1);
                         i.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3);
                         break;
                     // Stone Pickaxe
                     case 274:
                         i.addEnchantment(Enchantment.DIG_SPEED, 5);
                         i.addEnchantment(Enchantment.DURABILITY, 3);
                         i.addEnchantment(Enchantment.SILK_TOUCH, 1);
                         i.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3);
                         break;
                     // Stone Axe
                     case 275:
                         i.addEnchantment(Enchantment.DIG_SPEED, 5);
                         i.addEnchantment(Enchantment.DURABILITY, 3);
                         i.addEnchantment(Enchantment.SILK_TOUCH, 1);
                         i.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3);
                         break;
                     // Diamond Sword
                     case 276:
                         i.addEnchantment(Enchantment.DAMAGE_ALL, 5);
                         i.addEnchantment(Enchantment.DAMAGE_ARTHROPODS, 5);
                         i.addEnchantment(Enchantment.DAMAGE_UNDEAD, 5);
                         i.addEnchantment(Enchantment.FIRE_ASPECT, 2);
                         i.addEnchantment(Enchantment.KNOCKBACK, 2);
                         i.addEnchantment(Enchantment.LOOT_BONUS_MOBS, 3);
                         break;
                     // Diamond Shovel
                     case 277:
                         i.addEnchantment(Enchantment.DIG_SPEED, 5);
                         i.addEnchantment(Enchantment.DURABILITY, 3);
                         i.addEnchantment(Enchantment.SILK_TOUCH, 1);
                         i.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3);
                         break;
                     // Diamond Pickaxe
                     case 278:
                         i.addEnchantment(Enchantment.DIG_SPEED, 5);
                         i.addEnchantment(Enchantment.DURABILITY, 3);
                         i.addEnchantment(Enchantment.SILK_TOUCH, 1);
                         i.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3);
                         break;
                     // Diamond Axe
                     case 279:
                         i.addEnchantment(Enchantment.DIG_SPEED, 5);
                         i.addEnchantment(Enchantment.DURABILITY, 3);
                         i.addEnchantment(Enchantment.SILK_TOUCH, 1);
                         i.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3);
                         break;
                     // Gold Sword
                     case 283:
                         i.addEnchantment(Enchantment.DAMAGE_ALL, 5);
                         i.addEnchantment(Enchantment.DAMAGE_ARTHROPODS, 5);
                         i.addEnchantment(Enchantment.DAMAGE_UNDEAD, 5);
                         i.addEnchantment(Enchantment.FIRE_ASPECT, 2);
                         i.addEnchantment(Enchantment.KNOCKBACK, 2);
                         i.addEnchantment(Enchantment.LOOT_BONUS_MOBS, 3);
                         break;
                     // Gold Shovel
                     case 284:
                         i.addEnchantment(Enchantment.DIG_SPEED, 5);
                         i.addEnchantment(Enchantment.DURABILITY, 3);
                         i.addEnchantment(Enchantment.SILK_TOUCH, 1);
                         i.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3);
                         break;
                     // Gold Pickaxe
                     case 285:
                         i.addEnchantment(Enchantment.DIG_SPEED, 5);
                         i.addEnchantment(Enchantment.DURABILITY, 3);
                         i.addEnchantment(Enchantment.SILK_TOUCH, 1);
                         i.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3);
                         break;
                     // Gold Axe
                     case 286:
                         i.addEnchantment(Enchantment.DIG_SPEED, 5);
                         i.addEnchantment(Enchantment.DURABILITY, 3);
                         i.addEnchantment(Enchantment.SILK_TOUCH, 1);
                         i.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3);
                         break;
                     // Leather Helmet
                     case 298:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.OXYGEN, 3);
                         i.addEnchantment(Enchantment.WATER_WORKER, 1);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Leather Chestplate
                     case 299:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Leather Leggings
                     case 300:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Leather Boots
                     case 301:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FALL, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Chainmail Helmet
                     case 302:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.OXYGEN, 3);
                         i.addEnchantment(Enchantment.WATER_WORKER, 1);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Chainmail Chestplate
                     case 303:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Chainmail Leggings
                     case 304:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Chainmail Leggings
                     case 305:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FALL, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Iron Helmet
                     case 306:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.OXYGEN, 3);
                         i.addEnchantment(Enchantment.WATER_WORKER, 1);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Iron Chestplate
                     case 307:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Iron Leggings
                     case 308:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Iron Boots
                     case 309:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FALL, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Diamond Helmet
                     case 310:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.OXYGEN, 3);
                         i.addEnchantment(Enchantment.WATER_WORKER, 1);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Diamond Chestplate
                     case 311:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Diamond Leggings
                     case 312:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Diamond Boots
                     case 313:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FALL, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Gold Helmet
                     case 314:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.OXYGEN, 3);
                         i.addEnchantment(Enchantment.WATER_WORKER, 1);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Gold Chestplate
                     case 315:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Gold Leggings
                     case 316:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Gold Boots
                     case 317:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FALL, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                 }
             }
         }
         for (ItemStack i : p.getInventory().getArmorContents()) {
             if (i != null) {
                 switch (i.getTypeId()) {
                     // Leather Helmet
                     case 298:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.OXYGEN, 3);
                         i.addEnchantment(Enchantment.WATER_WORKER, 1);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Leather Chestplate
                     case 299:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Leather Leggings
                     case 300:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Leather Boots
                     case 301:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FALL, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Chainmail Helmet
                     case 302:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.OXYGEN, 3);
                         i.addEnchantment(Enchantment.WATER_WORKER, 1);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Chainmail Chestplate
                     case 303:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Chainmail Leggings
                     case 304:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Chainmail Boots
                     case 305:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FALL, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Iron Helmet
                     case 306:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.OXYGEN, 3);
                         i.addEnchantment(Enchantment.WATER_WORKER, 1);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Iron Chestplate
                     case 307:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Iron Leggings
                     case 308:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Iron Boots
                     case 309:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FALL, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Diamond Helmet
                     case 310:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.OXYGEN, 3);
                         i.addEnchantment(Enchantment.WATER_WORKER, 1);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Diamond Chestplate
                     case 311:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Diamond Leggings
                     case 312:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Diamond Boots
                     case 313:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FALL, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Gold Helmet
                     case 314:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.OXYGEN, 3);
                         i.addEnchantment(Enchantment.WATER_WORKER, 1);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Gold Chestplate
                     case 315:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Gold Leggings
                     case 316:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                     // Gold Boots
                     case 317:
                         i.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                         i.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                         i.addEnchantment(Enchantment.PROTECTION_FALL, 4);
                         i.addEnchantment(Enchantment.THORNS, 3);
                         break;
                 }
             }
         }
     }
 }
 
 
 
 
