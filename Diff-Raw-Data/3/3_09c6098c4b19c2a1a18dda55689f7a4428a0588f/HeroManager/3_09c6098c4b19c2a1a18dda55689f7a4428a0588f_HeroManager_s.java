 package com.herocraftonline.dev.heroes.persistence;
 
 import java.io.File;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.logging.Level;
 
 import com.herocraftonline.dev.heroes.party.HeroParty;
 import com.herocraftonline.dev.heroes.party.PartyManager;
 import com.herocraftonline.dev.heroes.ui.MapAPI;
 import com.herocraftonline.dev.heroes.ui.MapInfo;
 import com.herocraftonline.dev.heroes.ui.TextRenderer;
 import com.herocraftonline.dev.heroes.ui.TextRenderer.CharacterSprite;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.config.Configuration;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.HeroRegainManaEvent;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.command.Command;
 import com.herocraftonline.dev.heroes.effects.Effect;
 import com.herocraftonline.dev.heroes.effects.Expirable;
 import com.herocraftonline.dev.heroes.effects.Periodic;
 import com.herocraftonline.dev.heroes.skill.OutsourcedSkill;
 import com.herocraftonline.dev.heroes.skill.PassiveSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 /**
  * Player management
  * 
  * @author Herocraft's Plugin Team
  */
 public class HeroManager {
 
     protected Heroes plugin;
     private Set<Hero> heroes;
     protected Map<Creature, Set<Effect>> creatureEffects;
     private File playerFolder;
     private final static int effectInterval = 2;
     private final static int manaInterval = 5;
     private final static int partyUpdateInterval = 5;
 
     public HeroManager(Heroes plugin) {
         this.plugin = plugin;
         this.heroes = new HashSet<Hero>();
         this.creatureEffects = new HashMap<Creature, Set<Effect>>();
         playerFolder = new File(plugin.getDataFolder(), "players"); // Setup our Player Data Folder
         playerFolder.mkdirs(); // Create the folder if it doesn't exist.
 
         Runnable effectTimer = new EffectUpdater(this);
         plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, effectTimer, 0, effectInterval);
 
         Runnable manaTimer = new ManaUpdater(this);
         plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, manaTimer, 0, manaInterval);
 
         Runnable partyUpdater = new PartyUpdater(this, plugin, plugin.getPartyManager());
         plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, partyUpdater, 0, partyUpdateInterval);
     }
 
     public boolean addHero(Hero hero) {
         return heroes.add(hero);
     }
 
     public boolean containsPlayer(Player player) {
         return getHero(player) != null;
     }
 
     public Hero createNewHero(Player player) {
         Hero hero = new Hero(plugin, player, plugin.getClassManager().getDefaultClass());
         hero.setMana(100);
         hero.setHealth(hero.getMaxHealth());
         hero.syncHealth();
         addHero(hero);
         return hero;
     }
 
     public Hero getHero(Player player) {
         for (Hero hero : getHeroes()) {
             if (hero == null || hero.getPlayer() == null) {
                 removeHero(hero); // Seeing as it's null we might as well remove it.
                 continue;
             }
             if (player.getName().equalsIgnoreCase(hero.getPlayer().getName()))
                 return hero;
         }
         // If it gets to this stage then clearly the HeroManager doesn't have it so we create it...
         return loadHero(player);
     }
 
     public Set<Hero> getHeroes() {
         return new HashSet<Hero>(heroes);
     }
 
     /**
      * Load the given Players Data file.
      * 
      * @param player
      * @return
      */
     public Hero loadHero(Player player) {
         File playerFile = new File(playerFolder, player.getName() + ".yml"); // Setup our Players Data File.
         // Check if it already exists, if so we load the data.
         if (playerFile.exists()) {
             Configuration playerConfig = new Configuration(playerFile); // Setup the Configuration
             playerConfig.load(); // Load the Config File
 
             HeroClass playerClass = loadClass(player, playerConfig);
             if (playerClass == null) {
                 Heroes.log(Level.INFO, "Invalid class found for " + player.getName() + ". Resetting player.");
                 return createNewHero(player);
             }
             Hero playerHero = new Hero(plugin, player, playerClass);
 
             loadCooldowns(playerHero, playerConfig);
             loadExperience(playerHero, playerConfig);
             loadRecoveryItems(playerHero, playerConfig);
             loadBinds(playerHero, playerConfig);
             loadSkillSettings(playerHero, playerConfig);
             playerHero.setMana(playerConfig.getInt("mana", 0));
             playerHero.setHealth(playerConfig.getDouble("health", playerClass.getBaseMaxHealth()));
             playerHero.setVerbose(playerConfig.getBoolean("verbose", true));
             playerHero.suppressedSkills = new HashSet<String>(playerConfig.getStringList("suppressed", null));
             addHero(playerHero);
             playerHero.syncHealth();
 
             performSkillChecks(playerHero);
 
             Heroes.log(Level.INFO, "Loaded hero: " + player.getName());
             return playerHero;
         } else {
             // Create a New Hero with the Default Setup.
             Heroes.log(Level.INFO, "Created hero: " + player.getName());
             return createNewHero(player);
         }
     }
 
     public boolean removeHero(Hero hero) {
         if (hero != null && hero.hasParty()) {
             HeroParty party = hero.getParty();
             party.removeMember(hero);
             if (party.getMembers().size() == 0) {
                 this.plugin.getPartyManager().removeParty(party);
             }
         }
 
         return heroes.remove(hero);
     }
 
     /**
      * Save the given Players Data to a file.
      * 
      * @param player
      */
     public void saveHero(Player player) {
         File playerFile = new File(playerFolder, player.getName() + ".yml");
         Configuration playerConfig = new Configuration(playerFile);
         // Save the players stuff
         Hero hero = getHero(player);
         playerConfig.setProperty("class", hero.getHeroClass().toString());
         playerConfig.setProperty("verbose", hero.isVerbose());
         playerConfig.setProperty("suppressed", new ArrayList<String>(hero.getSuppressedSkills()));
         playerConfig.setProperty("mana", hero.getMana());
         playerConfig.removeProperty("itemrecovery");
         playerConfig.setProperty("health", hero.getHealth());
 
         saveSkillSettings(hero, playerConfig);
         saveCooldowns(hero, playerConfig);
         saveExperience(hero, playerConfig);
         saveRecoveryItems(hero, playerConfig);
         saveBinds(hero, playerConfig);
 
         playerConfig.save();
         Heroes.log(Level.INFO, "Saved hero: " + player.getName());
     }
 
     public void stopTimers() {
         plugin.getServer().getScheduler().cancelTasks(plugin);
     }
 
     private void loadBinds(Hero hero, Configuration config) {
         Map<Material, String[]> binds = new HashMap<Material, String[]>();
         List<String> bindKeys = config.getKeys("binds");
         if (bindKeys != null && bindKeys.size() > 0) {
             for (String material : bindKeys) {
                 try {
                     Material item = Material.valueOf(material);
                     String bind = config.getString("binds." + material, "");
                     if (bind.length() > 0) {
                         binds.put(item, bind.split(" "));
                     }
                 } catch (IllegalArgumentException e) {
                     this.plugin.debugLog(Level.WARNING, material + " isn't a valid Item to bind a Skill to.");
                     continue;
                 }
             }
         }
         hero.binds = binds;
     }
 
     private HeroClass loadClass(Player player, Configuration config) {
         HeroClass playerClass = null;
 
         if (config.getString("class") != null) {
             playerClass = plugin.getClassManager().getClass(config.getString("class"));
             if (Heroes.Permissions != null && playerClass != plugin.getClassManager().getDefaultClass()) {
                 if (!Heroes.Permissions.has(player, "heroes.classes." + playerClass.getName().toLowerCase())) {
                     playerClass = plugin.getClassManager().getDefaultClass();
                 }
             }
         } else {
             playerClass = plugin.getClassManager().getDefaultClass();
         }
         return playerClass;
     }
 
     private void loadSkillSettings(Hero hero, Configuration config) {
         String path = "skill-settings";
 
         if (config.getKeys(path) != null) {
             for (String skill : config.getKeys(path)) {
                 if (config.getNode(path).getKeys(skill) != null) {
                     for (String node : config.getNode(path).getKeys(skill)) {
                         hero.setSkillSetting(skill, node, config.getNode(path).getNode(skill).getString(node));
                     }
                 }
             }
         }
     }
 
     private void loadCooldowns(Hero hero, Configuration config) {
         HeroClass heroClass = hero.getHeroClass();
 
         String path = "cooldowns";
         List<String> storedCooldowns = config.getKeys(path);
         if (storedCooldowns != null) {
             long time = System.currentTimeMillis();
             Map<String, Long> cooldowns = new HashMap<String, Long>();
             for (String skillName : storedCooldowns) {
                 long cooldown = (long) config.getDouble(path + "." + skillName, 0);
                 if (heroClass.hasSkill(skillName) && cooldown > time) {
                     cooldowns.put(skillName, cooldown);
                 }
             }
             hero.cooldowns = cooldowns;
         }
     }
 
     HashMap<Creature, Set<Effect>> getCreatureEffects() {
         return new HashMap<Creature, Set<Effect>>(creatureEffects);
     }
 
     private void loadExperience(Hero hero, Configuration config) {
         if (hero == null || hero.getClass() == null || config == null)
             return;
 
         String root = "experience";
         List<String> expList = config.getKeys(root);
         if (expList != null) {
             for (String className : expList) {
                 double exp = config.getDouble(root + "." + className, 0);
                 HeroClass heroClass = plugin.getClassManager().getClass(className);
                 if (heroClass != null) {
                     if (hero.getExperience(heroClass) == 0) {
                         hero.setExperience(heroClass, exp);
                         if (!heroClass.isPrimary() && exp > 0) {
                             hero.setExperience(heroClass.getParent(), plugin.getConfigManager().getProperties().maxExp);
                         }
                     }
                 }
             }
         }
     }
 
     private void loadRecoveryItems(Hero hero, Configuration config) {
         List<ItemStack> itemRecovery = new ArrayList<ItemStack>();
         List<String> itemKeys = config.getKeys("itemrecovery");
         if (itemKeys != null && itemKeys.size() > 0) {
             for (String item : itemKeys) {
                 try {
                     Short durability = Short.valueOf(config.getString("itemrecovery." + item, "0"));
                     Material type = Material.valueOf(item);
                     itemRecovery.add(new ItemStack(type, 1, durability));
                 } catch (IllegalArgumentException e) {
                     this.plugin.debugLog(Level.WARNING, "Either '" + item + "' doesn't exist or the durability is of an incorrect value!");
                 }
             }
         }
         hero.setRecoveryItems(itemRecovery);
     }
 
     private void performSkillChecks(Hero hero) {
         HeroClass playerClass = hero.getHeroClass();
 
         List<Command> commands = plugin.getCommandHandler().getCommands();
         if (Heroes.Permissions != null) {
             for (Command cmd : commands) {
                 if (cmd instanceof OutsourcedSkill) {
                     OutsourcedSkill skill = (OutsourcedSkill) cmd;
                     if (playerClass.hasSkill(skill.getName())) {
                         skill.tryLearningSkill(hero);
                     }
                 }
             }
         }
 
         for (Command cmd : commands) {
             if (cmd instanceof PassiveSkill) {
                 PassiveSkill skill = (PassiveSkill) cmd;
                 if (playerClass.hasSkill(skill.getName())) {
                     skill.tryApplying(hero);
                 }
             }
         }
     }
 
     private void saveBinds(Hero hero, Configuration config) {
         config.removeProperty("binds");
         Map<Material, String[]> binds = hero.getBinds();
         for (Material material : binds.keySet()) {
             String[] bindArgs = binds.get(material);
             StringBuilder bind = new StringBuilder();
             for (String arg : bindArgs) {
                 bind.append(arg).append(" ");
             }
             config.setProperty("binds." + material.toString(), bind.toString().substring(0, bind.toString().length() - 1));
         }
     }
 
     private void saveSkillSettings(Hero hero, Configuration config) {
         String path = "skill-settings";
         for (Entry<String, Map<String, String>> entry : hero.skillSettings.entrySet()) {
             for (Entry<String, String> node : entry.getValue().entrySet()) {
                 config.setProperty(path + "." + entry.getKey() + "." + node.getKey(), node.getValue());
             }
         }
 
     }
 
     private void saveCooldowns(Hero hero, Configuration config) {
         String path = "cooldowns";
         long time = System.currentTimeMillis();
         Map<String, Long> cooldowns = hero.getCooldowns();
         for (Map.Entry<String, Long> entry : cooldowns.entrySet()) {
             String skillName = entry.getKey();
             long cooldown = entry.getValue();
             if (cooldown > time) {
                 System.out.println(path + "." + skillName);
                 config.setProperty(path + "." + skillName, cooldown);
             }
         }
     }
 
     private void saveExperience(Hero hero, Configuration config) {
         if (hero == null || hero.getClass() == null || config == null)
             return;
 
         String root = "experience";
         for (Map.Entry<String, Double> entry : hero.experience.entrySet()) {
             config.setProperty(root + "." + entry.getKey(), (double) entry.getValue());
         }
     }
 
     private void saveRecoveryItems(Hero hero, Configuration config) {
         for (ItemStack item : hero.getRecoveryItems()) {
             String durability = Short.toString(item.getDurability());
             config.setProperty("itemrecovery." + item.getType().toString(), durability);
         }
     }
 
     /**
      * Adds a new effect to the specific creature
      * 
      * @param creature
      * @param effect
      */
     public void addCreatureEffect(Creature creature, Effect effect) {
         Set<Effect> cEffects = creatureEffects.get(creature);
         if (cEffects == null) {
             cEffects = new HashSet<Effect>();
             creatureEffects.put(creature, cEffects);
         }
         cEffects.add(effect);
         effect.apply(creature);
     }
 
     /**
      * Removes an effect from a creature
      * 
      * @param creature
      * @param effect
      */
     public void removeCreatureEffect(Creature creature, Effect effect) {
         Set<Effect> cEffects = creatureEffects.get(creature);
         if (cEffects != null) {
             effect.remove(creature);
             cEffects.remove(effect);
             // If the creature has no effects left
             if (cEffects.isEmpty()) {
                 creatureEffects.remove(creature);
             }
         }
     }
 
     /**
      * Clears all effects from the creature
      * 
      * @param creature
      */
     public void clearCreatureEffects(Creature creature) {
         if (creatureEffects.containsKey(creature)) {
             Iterator<Effect> iter = creatureEffects.get(creature).iterator();
             while (iter.hasNext()) {
                 iter.next().remove(creature);
                 iter.remove();
             }
             creatureEffects.remove(creature);
         }
     }
 
     /**
      * Checks if a creature has the effect
      * 
      * @param creature
      * @param effect
      * @return
      */
     public boolean creatureHasEffect(Creature creature, String name) {
         if (!creatureEffects.containsKey(creature))
             return false;
         for (Effect effect : creatureEffects.get(creature)) {
             if (effect.getName().equalsIgnoreCase(name)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Gets a set view of all effects currently applied to the specified creature
      * 
      * @param creature
      * @return
      */
     public Set<Effect> getCreatureEffects(Creature creature) {
         return creatureEffects.get(creature);
     }
 
     public Effect getCreatureEffect(Creature creature, String name) {
         if (creatureEffects.get(creature) == null)
             return null;
 
         for (Effect effect : creatureEffects.get(creature)) {
             if (effect.getName().equals(name)) {
                 return effect;
             }
         }
         return null;
     }
 }
 
 class EffectUpdater implements Runnable {
 
     private final HeroManager heroManager;
 
     EffectUpdater(HeroManager heroManager) {
         this.heroManager = heroManager;
     }
 
     public void run() {
         for (Hero hero : heroManager.getHeroes()) {
             for (Effect effect : hero.getEffects()) {
                 if (effect instanceof Expirable) {
                     Expirable expirable = (Expirable) effect;
                     if (expirable.isExpired()) {
                         hero.removeEffect(effect);
                         continue;
                     }
                 }
 
                 if (effect instanceof Periodic) {
                     Periodic periodic = (Periodic) effect;
                     if (periodic.isReady()) {
                         periodic.tick(hero);
                     }
                 }
             }
         }
         
         for (Entry<Creature, Set<Effect>> cEntry : heroManager.getCreatureEffects().entrySet()) {
             for (Effect effect : cEntry.getValue()) {
                 if (effect instanceof Expirable) {
                     Expirable expirable = (Expirable) effect;
                     if (expirable.isExpired()) {
                         heroManager.removeCreatureEffect(cEntry.getKey(), effect);
                         continue;
                     }
                 }
                 if (effect instanceof Periodic) {
                     Periodic periodic = (Periodic) effect;
                     if (periodic.isReady()) {
                         periodic.tick(cEntry.getKey());
                     }
                 }
             }
         }
     }
 }
 
 class ManaUpdater implements Runnable {
 
     private final HeroManager manager;
     private final long updateInterval = 5000;
     private long lastUpdate = 0;
 
     ManaUpdater(HeroManager manager) {
         this.manager = manager;
     }
 
     public void run() {
         long time = System.currentTimeMillis();
         if (time < lastUpdate + updateInterval) {
             return;
         }
         lastUpdate = time;
 
         Set<Hero> heroes = manager.getHeroes();
         for (Hero hero : heroes) {
             if (hero == null) {
                 continue;
             }
 
             int mana = hero.getMana();
             if (mana == 100)
                 continue;
             
             HeroRegainManaEvent hrmEvent = new HeroRegainManaEvent(hero, 5, null);
             manager.plugin.getServer().getPluginManager().callEvent(hrmEvent);
             if (hrmEvent.isCancelled())
                 continue;
             
             hero.setMana(mana + hrmEvent.getAmount());
             if (hero.isVerbose()) {
                 Messaging.send(hero.getPlayer(), ChatColor.BLUE + "MANA " + Messaging.createManaBar(hero.getMana()));
             }
         }
     }
 }
 
 class PartyUpdater implements Runnable {
 
     private final HeroManager manager;
     private final Heroes plugin;
     private final PartyManager partyManager;
 
     PartyUpdater(HeroManager manager, Heroes plugin, PartyManager partyManager) {
         this.manager = manager;
         this.plugin = plugin;
         this.partyManager = partyManager;
     }
 
     public void run() {
         if (!this.plugin.getConfigManager().getProperties().mapUI)
             return;
 
         // System.out.print("Size - " + partyManager.getParties().size() + " Tick - " +
         // Bukkit.getServer().getWorlds().get(0).getTime());
 
         if (partyManager.getParties().size() == 0)
             return;
 
         for (HeroParty party : partyManager.getParties()) {
             if (party.updateMapDisplay()) {
                 party.setUpdateMapDisplay(false);
                 Player[] players = new Player[party.getMembers().size()];
                 int count = 0;
                 for (Hero heroes : party.getMembers()) {
                     players[count] = heroes.getPlayer();
                     count++;
                 }
                 updateMapView(players);
             }
         }
     }
 
     private void updateMapView(Player[] players) {
         MapAPI mapAPI = new MapAPI();
         short mapId = this.plugin.getConfigManager().getProperties().mapID;
 
         TextRenderer text = new TextRenderer(this.plugin);
         CharacterSprite sword = CharacterSprite.make("      XX", "     XXX", "    XXX ", "X  XXX  ", " XXXX   ", "  XX    ", " X X    ", "X   X   ");
         CharacterSprite crown = CharacterSprite.make("        ", "        ", "XX XX XX", "X XXXX X", "XX XX XX", " XXXXXX ", " XXXXXX ", "        ");
         CharacterSprite shield = CharacterSprite.make("   XX   ", "X  XX  X", "XXXXXXXX", "XXXXXXXX", "XXXXXXXX", " XXXXXX ", "  XXXX  ", "   XX   ");
         CharacterSprite heal = CharacterSprite.make("        ", "  XXX   ", "  XXX   ", "XXXXXXX ", "XXXXXXX ", "XXXXXXX ", "  XXX   ", "  XXX   ");
         CharacterSprite bow = CharacterSprite.make("XXXX   X", "X  XX X ", " X   X  ", "  X X X ", "   X  XX", "  X X  X", "XX   X X", " X    XX");
         text.setChar('\u0001', crown);
         text.setChar('\u0002', sword);
         text.setChar('\u0003', shield);
         text.setChar('\u0004', heal);
         text.setChar('\u0005', bow);
 
         MapInfo info = mapAPI.loadMap(Bukkit.getServer().getWorlds().get(0), mapId);
         mapAPI.getWorldMap(Bukkit.getServer().getWorlds().get(0), mapId).map = (byte) 9;
 
         info.setData(new byte[128 * 128]);
 
         String map = "§22;Party Members -\n";
 
         for (int i = 0; i < players.length; i++) {
             Hero hero = this.manager.getHero(players[i]);
             if (hero.getParty().getLeader().equals(hero)) {
                 map += "§42;\u0001";
             } else {
                 map += "§27;\u0002";
             }
             boolean damage = plugin.getConfigManager().getProperties().damageSystem;
             double currentHP;
             double maxHP;
             if (damage) {
                 currentHP = hero.getHealth();
                 maxHP = hero.getMaxHealth();
             } else {
                 currentHP = hero.getPlayer().getHealth();
                 maxHP = 20;
             }
             map += " §12;" + players[i].getName() + "\n" + createHealthBar(currentHP, maxHP) + "\n";
         }
 
         text.fancyRender(info, 10, 3, map);
 
         for (int i = 0; i < players.length; i++) {
             mapAPI.sendMap(players[i], mapId, info.getData(), this.plugin.getConfigManager().getProperties().mapPacketInterval);
         }
     }
 
     private static String createHealthBar(double health, double maxHealth) {
         String manaBar = com.herocraftonline.dev.heroes.ui.MapColor.DARK_RED + "[" + com.herocraftonline.dev.heroes.ui.MapColor.DARK_GREEN;
         int bars = 40;
         int progress = (int) (health / maxHealth * bars);
         for (int i = 0; i < progress; i++) {
             manaBar += "|";
         }
         manaBar += com.herocraftonline.dev.heroes.ui.MapColor.DARK_GRAY;
         for (int i = 0; i < bars - progress; i++) {
             manaBar += "|";
         }
         manaBar += com.herocraftonline.dev.heroes.ui.MapColor.RED + "]";
         double percent = (health / maxHealth * 100);
         DecimalFormat df = new DecimalFormat("#.##");
         return manaBar + " - " + com.herocraftonline.dev.heroes.ui.MapColor.GREEN + df.format(percent) + "%";
     }
 }
