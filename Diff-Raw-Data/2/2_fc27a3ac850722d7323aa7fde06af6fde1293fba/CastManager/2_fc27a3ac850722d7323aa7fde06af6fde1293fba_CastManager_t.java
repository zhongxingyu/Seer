 package com.herocraftonline.heromagic;
 
 import java.util.HashMap;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class CastManager {
 	private HeroMagic plugin;
 	private Spells spells;
 	private HashMap<String,Long> coolDowns;
 	
 	CastManager(HeroMagic instance) {
 		this.plugin = instance;
 		this.spells = plugin.spells;
 		this.coolDowns = new HashMap<String, Long>();
 	}
 	
 	/**
 	 * Initializes a new cooldown for the given spell.
 	 * @param player
 	 * @param spellname
 	 * @param cooldowntime
 	 */
     public void startCooldown(Player player, String spellname) {
     	String str = player.getName() + spellname;
     	
     	if(!coolDowns.containsKey(str)) {
     		coolDowns.put(str, System.currentTimeMillis());
     	}
     }
     
     /**
      * Returns if the player has a cooldown on the spell.
      * @param player
      * @param spellname
      * @param cooldown
      * @return
      */
     public boolean isOnCooldown(Player player, String spellName) {
     	String str = player.getName() + spellName;
     	int coolDown = spells.getSpellByName(spellName).getCoolDown();
     	
 		if (!coolDowns.containsKey(str)) {
 			return false;
 		} else if (System.currentTimeMillis() - coolDowns.get(str) > coolDown * 1000) {
 			coolDowns.remove(str);
 			return false;
 		} else {
 			return true;
 		}
 	}
     
     public int getCoolDownRemaining(Player player, String spellName) {
     	String str = player.getName() + spellName;
     	if (coolDowns.containsKey(str)) {
    		return (int)(((System.currentTimeMillis() - coolDowns.get(str)) / 1000) / 60);
     	}
     	return 0;
     }
     
     /**
      * Adds the ability to cast a spell.
      * @param player
      * @param spellname
      */
     public void addSpell(Player player, String spellName) {
     	PlayerSpell playerSpell = plugin.getDatabase().find(PlayerSpell.class).where().ieq("player", player.getName()).ieq("spell", spellName).findUnique();
     	if (playerSpell == null) {
     		playerSpell = new PlayerSpell();
     		playerSpell.setPlayer(player.getName());
     		playerSpell.setSpell(spellName);
     	}
 		playerSpell.setLearned(true);
 		plugin.getDatabase().save(playerSpell);
     }
     
     /**
      * Removes the ability to cast a spell.
      * @param player
      * @param spellName
      */
     public void removeSpell(Player player, String spellName) {
     	PlayerSpell playerSpell = plugin.getDatabase().find(PlayerSpell.class).where().ieq("player", player.getName()).ieq("spell", spellName).findUnique();
     	if (playerSpell != null) {
     		playerSpell.setLearned(false);
     		plugin.getDatabase().save(playerSpell);
     	}
     }
     
     /**
      * Returns if the player has permissions to cast the spell.
      * @param player
      * @param spellname
      * @return
      */
     public boolean canCastSpell(Player player, String spellName) {
     	if(spells.blackList.contains(player.getWorld().getName())) {
     		return false;
     	}
     	PlayerSpell playerSpell = plugin.getDatabase().find(PlayerSpell.class).where().ieq("player", player.getName()).ieq("spell", spellName).findUnique();
     	if (playerSpell == null && !player.isOp()) return false;
     	return (playerSpell.isLearned() || player.isOp());
     }
     
     /**
      * Removes the used regents from the players inventory.
      * @param player
      * @param regents
      * @return
      */
 	public boolean removeRegents(Player player, String spellName) {
     	Spell spell = spells.getSpellByName(spellName);
     	if (player.isOp()) return true;
     	if (spell != null) {
 	    	Inventory inv = player.getInventory();
 	    	ItemStack[] items = inv.getContents();
 	    	int item1 = 0;
 	    	int item2 = 0;
 	    	
 	    	for(int i=0; i<items.length; i++) {
 	    		if (items[i] != null) {
 	    			if (items[i].getTypeId() == spell.getReagent1() && items[i].getAmount() >= spell.getReagent1_amount()) {
 	    				item1 = i;
 	    			} else if (spell.getReagent2() != 0 && items[i].getTypeId() == spell.getReagent2() && items[i].getAmount() >= spell.getReagent2_amount()) {
 	    				item2 = i;
 	    			}
 	    		}
 	    	}
 	    	
 	    	if (item1 != 0 && (item2 != 0 || spell.getReagent2() == 0)) {
 	    		if (items[item1].getAmount() > spell.getReagent1_amount()) {
 	    			items[item1].setAmount(items[item1].getAmount() - spell.getReagent1_amount());
 	    		} else {
 	    			inv.remove(items[item1]);
 	    		}
 	    		if (spell.getReagent2() != 0) {
 		    		if (items[item2].getAmount() > spell.getReagent2_amount()) {
 		    			items[item2].setAmount(items[item2].getAmount() - spell.getReagent2_amount());
 		    		} else {
 		    			inv.remove(items[item2]);
 		    		}
 	    		}
 	    		return true;
 	    	}
     	}
     	return false;
     }
     
     /**
      * Marks a location for further use.
      * @param player
      * @param loc
      */
     public void setPlayerMark(Player player) {
     	Location loc = player.getLocation();
     	PlayerMark playerMark = plugin.getDatabase().find(PlayerMark.class).where().ieq("player", player.getName()).findUnique();
     	if (playerMark == null) {
     		playerMark = new PlayerMark();
     		playerMark.setPlayer(player.getName());
     	}
     	playerMark.setWorld(loc.getWorld().getName());
     	playerMark.setX(loc.getX());
     	playerMark.setY(loc.getY());
     	playerMark.setZ(loc.getZ());
     	
     	plugin.getDatabase().save(playerMark);
     }
     
     /**
      * Returns the marked location of a player.
      * @param player
      * @return
      */
     public Location getPlayerMark(Player player) {
     	String world = "";
     	double x = 0.0;
     	double y = 0.0;
     	double z = 0.0;
     	
     	PlayerMark playerMark = plugin.getDatabase().find(PlayerMark.class).where().ieq("player", player.getName()).findUnique();
     	if (playerMark != null) {
     		world = playerMark.getWorld();
     		x = playerMark.getX();
     		y = playerMark.getY();
     		z = playerMark.getZ();
     	}
     	if (plugin.getServer().getWorld(world) == null) world = plugin.getServer().getWorlds().get(0).getName();
    
     	return new Location(plugin.getServer().getWorld(world), x, y, z);
     }
     
     /**
      * Returns the distance between the player and a block.
      * @param pl
      * @param tg
      * @return
      */
 	public double getDistance(Player pl, Block tg) {
 		Location player = pl.getLocation();
 		Location target = tg.getLocation();
 		return Math.sqrt(Math.pow(player.getX()-target.getX(),2) + Math.pow(player.getY()-target.getY(),2) + Math.pow(player.getZ()-target.getZ(),2));
 	}
 	
 	/**
 	 * Returns the location of all spell-bookshelfs.
 	 * @param player
 	 * @return
 	 */
     public HashMap<Location,String> getSpellLocations(Player player)
     {
     	HashMap<Location,String> map = new HashMap<Location,String>();
     	
     	for(Spell spell : spells.spellList) {
     		map.put(spell.getLocation(), spell.getName());
     	}
     	
     	return map;
     }
 }
