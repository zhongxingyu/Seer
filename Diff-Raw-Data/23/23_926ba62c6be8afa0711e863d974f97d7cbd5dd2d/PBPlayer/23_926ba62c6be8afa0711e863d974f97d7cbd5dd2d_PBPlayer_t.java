 package net.battlenexus.paintball.entities;
 
 import net.battlenexus.paintball.Paintball;
 import net.battlenexus.paintball.game.PaintballGame;
 import net.battlenexus.paintball.game.weapon.Weapon;
 import org.bukkit.*;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.inventory.meta.LeatherArmorMeta;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.util.Vector;
 
 import java.util.HashMap;
 
 public class PBPlayer {
     private static ItemStack[] lobby_items;
     private static HashMap<String, PBPlayer> players = new HashMap<String, PBPlayer>();
     private Player player;
     private boolean frozen;
     private Location frozen_location;
     private int kills;
     private int deaths;
     private boolean isSpectating;
     private Weapon weapon;
     private PaintballGame current_game;
     private double defaultMaxHealth = 18;
     private double maxHealth = 18;
 
     public HashMap<PBPlayer, Integer> kill_cache = new HashMap<PBPlayer, Integer>();
 
     private static void createLobbyItems() {
         if (lobby_items != null)
             return;
         lobby_items = new ItemStack[1];
 
         lobby_items[0] = new ItemStack(Material.EMERALD);
         ItemMeta meta = lobby_items[0].getItemMeta();
         meta.setDisplayName("Weapon Shop");
         lobby_items[0].setItemMeta(meta);
     }
 
     private PBPlayer(Player player) {
         this.player = player;
     }
 
     public Player getBukkitPlayer() {
         return player;
     }
 
     public Weapon getCurrentWeapon() {
         return weapon;
     }
 
     public void setWeapon(Weapon weapon) {
         this.weapon = weapon;
         ItemStack item = Weapon.WeaponUtils.toItemStack(weapon);
         if (player.getInventory().contains(item))
             return;
         player.getInventory().addItem(item);
 
         if (player.getInventory().getItem(0) != item) {
             Inventory i = player.getInventory();
             int item_index = i.first(item);
             while (item_index == -1) {
                 player.getInventory().addItem(item);
                 item_index = i.first(item);
             }
             ItemStack tomove = i.getItem(0);
             i.clear(0);
             i.clear(item_index);
             i.setItem(0, item);
             i.setItem(item_index, tomove);
 
             player.updateInventory();
         }
 
     }
 
     /**
      * Creates a new {@link PBPlayer} for a player
      *
      * @param player Player the player you would like to find
      * @return PBPlayer
      */
     public static PBPlayer newPlayer(Player player) {
         PBPlayer pbPlayer = new PBPlayer(player);
         if (pbPlayer.hasSaveData())
             pbPlayer.load();
         players.put(player.getName(), pbPlayer);
 
         return pbPlayer;
     }
 
     public void setCurrentGame(PaintballGame game) {
         if (current_game != null) {
             current_game.leaveGame(this);
         }
         current_game = game;
     }
 
     public PaintballGame getCurrentGame() {
         return current_game;
     }
 
     public Team getCurrentTeam() {
         return current_game != null ? current_game.getTeamForPlayer(this) : null;
     }
 
     public void handleFrozen() {
         if (!frozen || frozen_location == null)
             return;
         player.teleport(frozen_location);
     }
 
     public boolean isFrozen() {
         return frozen;
     }
 
     public void freeze() {
         frozen_location = player.getLocation();
         frozen = true;
     }
 
     public void unfreeze() {
         frozen = false;
         frozen_location = null;
     }
 
     /**
      * Finds and returns the {@link PBPlayer} for the given player
      *
      * @param player Player The player you would like to find
      * @return PBPlayer
      */
     public static PBPlayer getPlayer(Player player) {
         return PBPlayer.players.get(player.getName());
     }
 
     public static PBPlayer toPBPlayer(Player player) {
         if (players.containsKey(player.getName())) {
             return players.get(player.getName());
         } else {
             return newPlayer(player);
         }
     }
 
     public void hit(PBPlayer shooter) {
         if (shooter.getCurrentTeam() != null) {
             if (shooter.getCurrentTeam().contains(this)) {
                 shooter.sendMessage("Watch out! " + getBukkitPlayer().getDisplayName() + ChatColor.GRAY + " is on your team!");
             } else {
                 if(shooter.getCurrentWeapon().isOneHitKill() || wouldDie(shooter.getCurrentWeapon().damage())) {
                     refillHealth();
                     kill(shooter);
                 } else {
                     damagePlayer(shooter.getCurrentWeapon().damage());
                 }
             }
         }
     }
 
     public boolean isInGame() {
         return current_game != null && getCurrentTeam() != null && !current_game.hasEnded();
     }
 
     public void kill(final PBPlayer killer) {
         if (!isInGame())
             return;
         addDeath();
         if (killer != null)
             killer.addKill();
         getCurrentTeam().spawnPlayer(this);
         getCurrentGame().onPlayerKill(killer, this);
         for(Player p : Bukkit.getOnlinePlayers()) {
             p.playSound(p.getLocation(), Sound.AMBIENCE_THUNDER, 40, 0);
         }
     }
 
     public void showLobbyItems() {
         createLobbyItems();
         final Inventory inventory = getBukkitPlayer().getInventory();
 
         for (int i = 0; i < lobby_items.length; i++) {
             int index = 8 - i;
 
             inventory.remove(inventory.getItem(index));
             inventory.setItem(index, lobby_items[i]);
         }
 
         getBukkitPlayer().updateInventory();
     }
 
     public void hideLobbyItems() {
         createLobbyItems();
         final Inventory inventory = getBukkitPlayer().getInventory();
 
         for (int i = 0; i < lobby_items.length; i++) {
             int index = 8 - i;
 
             inventory.remove(inventory.getItem(index));
         }
 
         getBukkitPlayer().updateInventory();
     }
 
     public void save() {
         //TODO Save data
     }
 
     public boolean hasSaveData() {
         //TODO Check if save data is available
 
         return false;
     }
 
     public void load() {
         //TODO Load save data, if hasSaveData returns false, then dont do anything.
     }
 
 
     public void spectateGame(PaintballGame game) {
         Paintball.makePlayerGhost(player);
         player.setAllowFlight(true);
         player.setFlying(true);
 
         Vector firstTeam = game.getConfig().getBlueTeam().getSpawn().toVector();
         Vector secondTeam = game.getConfig().getRedTeam().getSpawn().toVector();
 
         Vector midpoint = firstTeam.midpoint(secondTeam);
         Location lmidpoint = midpoint.toLocation(game.getConfig().getBlueTeam().getSpawn().getWorld());
         while (lmidpoint.getBlock().getType() != Material.AIR) {
             lmidpoint.add(0, 1, 0);
         }
 
         player.teleport(lmidpoint);
         isSpectating = true;
 
     }
 
     public void stopSpectating() {
         if (!isSpectating)
             return;
         isSpectating = false;
         player.teleport(Paintball.INSTANCE.paintball_world.getSpawnLocation());
         player.setFlying(false);
         player.setAllowFlight(false);
         Paintball.makePlayerVisible(player);
     }
 
 
     public boolean isSpectating() {
         return isSpectating;
     }
 
 
     public void joinGame(PaintballGame game) {
         if (isInGame()) {
             leaveGame(getCurrentGame());
         }
         setCurrentGame(game);
         hideLobbyItems();
         game.joinNextOpenTeam(this);
         game.onPlayerJoin(this);
         Player bukkitP = getBukkitPlayer();
         refillHealth();
         bukkitP.setFoodLevel(20);
         ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
         ItemStack legs = new ItemStack(Material.LEATHER_LEGGINGS, 1);
         ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);
         LeatherArmorMeta chestIm = (LeatherArmorMeta) chest.getItemMeta();
         LeatherArmorMeta legsIm = (LeatherArmorMeta) chest.getItemMeta();
         LeatherArmorMeta bootsIm = (LeatherArmorMeta) chest.getItemMeta();
         char[] teamChars = getCurrentTeam().getName().toCharArray();
         boolean hasColor = teamChars[0] == ChatColor.COLOR_CHAR;
         if (hasColor && teamChars[1] == '9') {
             chestIm.setColor(Color.BLUE);
             legsIm.setColor(Color.BLUE);
             bootsIm.setColor(Color.BLUE);
         } else { //Current Team is red
             chestIm.setColor(Color.RED);
             legsIm.setColor(Color.RED);
             bootsIm.setColor(Color.RED);
         }
         chest.setItemMeta(chestIm);
         legs.setItemMeta(legsIm);
         boots.setItemMeta(bootsIm);
        chest.addUnsafeEnchantment(Enchantment.DURABILITY, 100);
        legs.addUnsafeEnchantment(Enchantment.DURABILITY, 100);
        boots.addUnsafeEnchantment(Enchantment.DURABILITY, 100);
         bukkitP.getInventory().setChestplate(chest);
         bukkitP.getInventory().setLeggings(legs);
         bukkitP.getInventory().setBoots(boots);
         bukkitP.setCanPickupItems(false);
         freeze();
         getBukkitPlayer().setGameMode(GameMode.ADVENTURE);
     }
 
     public void leaveGame(PaintballGame game) {
         if (!isInGame())
             return;
         if (!game.hasEnded() && !game.isEnding())
             game.onPlayerLeave(this);
         getCurrentTeam().leaveTeam(null);
         setCurrentGame(null);
         maxHealth = defaultMaxHealth;
         for (PotionEffect effect : player.getActivePotionEffects())
             player.removePotionEffect(effect.getType());
        clearInventory();
         if (getCurrentWeapon() != null) {
             weapon.emptyGun();
             weapon.setOneHitKill(false);
             setWeapon(weapon); //Give the player back there gun..
         }
         showLobbyItems();
        kill_cache.clear(); //Empty kills if they leave game or game ends
         player.setMaxHealth(20.0);
         player.setHealth(20.0);
         player.setFoodLevel(20);
         player.setCanPickupItems(true);
         player.teleport(Paintball.INSTANCE.paintball_world.getSpawnLocation());
     }
 
    public void clearInventory() {
        player.getInventory().clear();
        player.getInventory().setChestplate(new ItemStack(Material.AIR));
        player.getInventory().setLeggings(new ItemStack(Material.AIR));
        player.getInventory().setBoots(new ItemStack(Material.AIR));
    }

     public boolean wouldDie(int damage) {
         return getBukkitPlayer().getMaxHealth() - damage <= 0;
     }
 
     public void damagePlayer(int damage) {
         getBukkitPlayer().setMaxHealth((double) getBukkitPlayer().getMaxHealth() - damage);
         getBukkitPlayer().setHealth((double) getBukkitPlayer().getMaxHealth());
     }
 
     public void refillHealth() {
         getBukkitPlayer().setMaxHealth(maxHealth);
         getBukkitPlayer().setHealth(maxHealth);
     }
 
     public int getKills() {
         return kills;
     }
 
     public void setKills(int kills) {
         this.kills = kills;
     }
 
     public void addKill() {
         this.kills++;
     }
 
     public int getDeaths() {
         return deaths;
     }
 
     public void setDeaths(int deaths) {
         this.deaths = deaths;
     }
 
     public void addDeath() {
         this.deaths++;
     }
 
     public void increasMaxHealth(double halfHearts) {
         maxHealth  += halfHearts;
         player.setMaxHealth(player.getMaxHealth() + halfHearts);
         if(player.getHealth() + halfHearts <= player.getMaxHealth()) {
             player.setHealth(player.getHealth() + halfHearts);
         }
     }
 
     public void sendMessage(String s) {
         getBukkitPlayer().sendMessage(Paintball.formatMessage(s));
     }
 
     public void dispose() {
         save();
 
 
         players.remove(player.getName());
 
         if (isInGame()) {
             current_game.leaveGame(this);
         }
 
         player = null;
         weapon = null;
         current_game = null;
         frozen_location = null;
     }
 }
