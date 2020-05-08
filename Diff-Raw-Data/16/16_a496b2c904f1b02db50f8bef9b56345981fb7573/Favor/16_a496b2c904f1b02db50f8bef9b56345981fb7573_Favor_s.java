 package me.Nekoyoubi.Blessings;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 public class Favor {
 
 	/**
 	 * The action to perform (e.g. "give", "spawn", "heal", "ill", etc...).
 	 */
 	public String action;
 	/**
 	 * The data (if any) associated with the action (e.g. "14" for the RED in wool).
 	 */
 	public String data;
 	/**
 	 * The targets to be effected by the action (i.e. "player", "random", "world). Stack values with a semicolon (e.g. "player;random;random").
 	 */
 	public String targets;
 	/**
 	 * The chance (in 1000) that this favor will be granted.
 	 */
 	public Integer level;
 	/**
 	 * The desired level that this favor be granted.
 	 */
 	public Integer chance;
 	/**
 	 * Represents a unit of favor from a god. Consider it a gift or blessing.
 	 * @param level The desired level that this favor be granted.
 	 * @param chance The chance (in 1000) that the favor will be granted.
 	 * @param action The action to perform (e.g. "give", "spawn", "heal", "ill", etc...)
 	 * @param targets The targets to be effected by the action (i.e. "player", "random", "world). Stack values with a semicolon (e.g. "player;random;random").
 	 * @param data The data (if any) associated with the action (e.g. "14" for the RED in wool).
 	 */
 	public Favor(Integer level, Integer chance, String action, String targets, String data) {
 		this.level = level;
 		this.chance = chance;
 		this.action = action;
 		this.targets = targets;
 		this.data = data;
 	}
 	/**
 	 * Processes a blessing's favor for a player (e.g. gives items, spawns mobs, etc...).
 	 * @param player The player receiving the favor.
 	 * @param shrine The shrine that the player just used.
 	 */
 	@SuppressWarnings("deprecation")
 	public void process(Player player, Block shrine, God god) {
 		// Determine targets and establish the list of players to effect.
 		ArrayList<Player> effectTargets = getTargets(player);
 		if (this.action.equalsIgnoreCase("give")) {
 			ArrayList<ItemStack> items = new ArrayList<ItemStack>();
 			Pattern ip = Pattern.compile("^(\\d+)(:(\\d+))?(([xr])((\\d+)-(\\d+)))?$");
 			for (String i : this.data.split(";")) {
 				Matcher im = ip.matcher(i);
 				if (im.find()) {
 					Integer id = Integer.parseInt(im.group(1));
 					Short data = im.group(3) != null
 						? Short.parseShort(im.group(3))
 						: 0;
 					Integer min = im.group(7) != null
 						? Integer.parseInt(im.group(7))
 						: 1;
 					Integer max = im.group(8) != null
 						? Integer.parseInt(im.group(8))
 						: 1;
 					boolean isRando = im.group(5) != null
 						? im.group(5) == "r"
 						: false;
 					Random rando = new Random();
 					Integer quantity = getQuantity(player, rando, min, max, isRando);
 					if (quantity>0) items.add(new ItemStack(id, quantity, data));
 				}
 			}
 			
 			for (Player target : effectTargets) {				
 				for (ItemStack itemStack : items) {
 					if (target.getInventory().firstEmpty()<0)
 						target.getWorld().dropItem(target.getLocation(), itemStack);
 					else {
 						target.getInventory().addItem(itemStack);
 						target.updateInventory();
 					}
					Nekoyoubi.sendMessage(player, god.colorName()+" has given you "+
 							itemStack.getAmount()+" "+
 							Nekoyoubi.titleCase(itemStack.getType().toString().replace("_"," ")).replace("Tnt", "TNT")+".");
 				}
 			}
 		} else if (this.action.equalsIgnoreCase("spawn")) {
 			Random rando = new Random();
 			Pattern sp = Pattern.compile("^(\\w+)(\\s(\\d+)(-(\\d+)(r?))?)?$");
 			Matcher sm = sp.matcher(this.data);
 			if (sm.find()) {
 				String mob = sm.group(1);
 				Integer min = sm.group(3) != null
 					? Integer.parseInt(sm.group(3))
 					: 1;
 				Integer max = sm.group(5) != null
 					? Integer.parseInt(sm.group(5))
 					: 1;
 				boolean isRando = sm.group(6) != null
 					? sm.group(6) == "r"
 					: false;
 				Integer quantity = getQuantity(player, rando, min, max, isRando);
 				
 				for (Player target : effectTargets) {
 					if (mob.equalsIgnoreCase("tamedwolf")) {
 						for (int i = 0; i < quantity; i++) {
 							Wolf wolf = (Wolf)target.getWorld().spawnCreature(target.getLocation().add(rando.nextInt(8)-4, 0, rando.nextInt(8)-4), CreatureType.WOLF);
 							wolf.setOwner(target);
 							wolf.setTamed(true);
 						}
 						if (target == player)
 							Nekoyoubi.sendMessage(target, god.colorName()+" has blessed you with companionship.");
 						else
 							Nekoyoubi.sendMessage(target, player.getDisplayName()+" has asked "+god.colorName()+" to send you aid.");
 					} else if (mob.equalsIgnoreCase("angrywolf")) {
 						for (int i = 0; i < quantity; i++) {
 							Wolf wolf = (Wolf)target.getWorld().spawnCreature(target.getLocation().add(rando.nextInt(16)-8, 0, rando.nextInt(16)-8), CreatureType.WOLF);
 							wolf.setTarget(player);
 							wolf.setAngry(true);
 						}
 						if (target == player)
 							Nekoyoubi.sendMessage(target, god.colorName()+" was not amused. Enjoy your company.");
 						else
 							Nekoyoubi.sendMessage(target, player.getDisplayName()+" has brought down the wrath of "+god.colorName()+".");
 					}
 				}
 			}
 		} else if (this.action.equalsIgnoreCase("heal")) {
 			for (Player target : effectTargets) {
 				target.setHealth(Integer.parseInt(this.data));
 				if (target == player)
 					Nekoyoubi.sendMessage(target, god.colorName()+" has healed your wounds.");
 				else
 					Nekoyoubi.sendMessage(target, player.getDisplayName()+" has asked "+god.colorName()+" to heal your wounds.");
 			}
 		} else if (this.action.equalsIgnoreCase("feed")) {
 			for (Player target : effectTargets) {
 				target.setFoodLevel(Integer.parseInt(this.data));
 				if (target == player)
 					Nekoyoubi.sendMessage(target, god.colorName()+" has soothed your hunger.");
 				else
 					Nekoyoubi.sendMessage(target, player.getDisplayName()+" has asked "+god.colorName()+" to soothe your hunger.");
 			}			
 		} else if (this.action.equalsIgnoreCase("hunger")) {
 			for (Player target : effectTargets) {
 				target.setFoodLevel(Integer.parseInt(this.data));
 				if (target == player)
 					Nekoyoubi.sendMessage(target, god.colorName()+" was not amused. Feast on your own famine.");
 				else
 					Nekoyoubi.sendMessage(target, player.getDisplayName()+" has angered "+god.colorName()+".");
 			}			
 		} else if (this.action.equalsIgnoreCase("hurt")) {
 			for (Player target : effectTargets) {
 				target.damage(Integer.parseInt(this.data), player);
 				if (target == player)
 					Nekoyoubi.sendMessage(target, god.colorName()+" was not amused. Your life was forfeit.");
 				else
 					Nekoyoubi.sendMessage(target, player.getDisplayName()+" has brought the wrath of "+god.colorName()+" upon you.");
 			}
 		} else if (this.action.equalsIgnoreCase("burn")) {
 			for (Player target : effectTargets) {
 				target.setFireTicks(Integer.parseInt(this.data)*20);
 				if (target == player)
 					Nekoyoubi.sendMessage(target, god.colorName()+" was not amused. Enjoy your rewards.");
 				else
 					Nekoyoubi.sendMessage(target, player.getDisplayName()+" has brought the flames of "+god.colorName()+" upon you.");
 			}
 		} else if (this.action.equalsIgnoreCase("dig")) {
 			for (Player target : effectTargets) {
 				int dist = (this.data.length()>0) ? Integer.parseInt(this.data) : 3;
 				Location tloc = target.getLocation();
 				for (int u = 0; u < dist; u++) {
 					Block under = tloc.getBlock().getRelative(0, -(u+1), 0);
 					if (	under.getType() != Material.AIR &&
 							under.getType() != Material.BEDROCK) {
 						target.getWorld().dropItem(under.getLocation(), new ItemStack(under.getType()));
 						under.setType(Material.AIR);
 					}
 				}
 					if (target == player)
 						Nekoyoubi.sendMessage(target, god.colorName()+" was not amused. To the underworld with you!");
 					else
 						Nekoyoubi.sendMessage(target, player.getDisplayName()+" has convinced "+god.colorName()+" to send you to the underworld.");
 			}
 		} else if (this.action.equalsIgnoreCase("stormy")) {
 			if (player.getWorld().getWeatherDuration() == 0) {
 				player.getWorld().setStorm(true);
 				for (Player target : effectTargets) {
 					if (target == player)
 						Nekoyoubi.sendMessage(target, god.colorName()+" was not amused. The skys rumble and riot above you.");
 					else
 						Nekoyoubi.sendMessage(target, player.getDisplayName()+" has brought the tempests of "+god.colorName()+" upon you.");
 				}
 			}
 		} else if (this.action.equalsIgnoreCase("sunny")) {
 			if (player.getWorld().getWeatherDuration() > 0) {
 				player.getWorld().setStorm(false);
 				for (Player target : effectTargets) {
 					if (target == player)
 						Nekoyoubi.sendMessage(target, god.colorName()+" has lifted the clouds in your honor.");
 					else
 						Nekoyoubi.sendMessage(target, player.getDisplayName()+" has asked "+god.colorName()+" to bring out the sun.");
 				}
 			}
 		} else if (this.action.equalsIgnoreCase("shock")) {
 			for (Player target : effectTargets) {
 				player.getWorld().strikeLightning(target.getLocation());
 				if (target == player)
 					Nekoyoubi.sendMessage(target, god.colorName()+" was not amused. Cower in the might of "+god.colorName()+"!");
 				else
 					Nekoyoubi.sendMessage(target, player.getDisplayName()+" has brought the wrath of "+god.colorName()+" upon you.");
 			}
 		} else if (this.action.equalsIgnoreCase("weaken")) {
 			for (Player target : effectTargets) {
 				PlayerInventory inv = target.getInventory();
 				HashMap<Integer,ItemStack> map = new HashMap<Integer, ItemStack>();
 				map.putAll(inv.all(Material.IRON_HELMET));
 				map.putAll(inv.all(Material.IRON_CHESTPLATE));
 				map.putAll(inv.all(Material.IRON_LEGGINGS));
 				map.putAll(inv.all(Material.IRON_BOOTS));
 				map.putAll(inv.all(Material.DIAMOND_HELMET));
 				map.putAll(inv.all(Material.DIAMOND_CHESTPLATE));
 				map.putAll(inv.all(Material.DIAMOND_LEGGINGS));
 				map.putAll(inv.all(Material.DIAMOND_BOOTS));
				Nekoyoubi.sendMessage(target, map.size()+"");
 				
 				Random rando = new Random();
 				Object[] values = map.values().toArray();
				ItemStack item = (ItemStack)values[rando.nextInt(values.length)];
				Nekoyoubi.sendMessage(target, "Before: "+item.getDurability());
				item.setDurability((short)(item.getDurability()/(short)2));
				Nekoyoubi.sendMessage(target, "After: "+item.getDurability());
				target.updateInventory();
 				if (target == player)
 					Nekoyoubi.sendMessage(target, god.colorName()+" was not amused. Your armor was your aegis.");
 				else
 					Nekoyoubi.sendMessage(target, player.getDisplayName()+" has brought the wrath of "+god.colorName()+" upon you.");
 			}
 		}
 	}
 	private int getQuantity(Player player, Random rando, Integer min,
 			Integer max, boolean isRando) {
 		return isRando
 			? rando.nextInt(max-min)+min
 			: (int)Math.round(((double)min+((double)(max-min)*((double)player.getLevel()/20.0))));
 	}
 	
 	/**
 	 * Determines the list of players to be effected by a blessing (or curse).
 	 * @param player The player instigating the blessing.
 	 * @return Returns an array of players to effect with a favor.
 	 */
 	private ArrayList<Player> getTargets(Player player) {
 		ArrayList<Player> effectTargets = new ArrayList<Player>();
 		for (String t : this.targets.split(";")) {
 			if (t.equalsIgnoreCase("player")) {
 				effectTargets.add(player);
 			} else if (t.equalsIgnoreCase("random")) {
 				Player rando = Nekoyoubi.randomPlayerInWorld(player.getWorld());
 				if (!effectTargets.contains(rando)) effectTargets.add(rando);
 			} else if (t.equalsIgnoreCase("world")) {
 				effectTargets.clear();
 				effectTargets.addAll(player.getWorld().getPlayers());
 			} else if (t.equalsIgnoreCase("server")) {
 				// TODO Think long and hard about this one... smells horribad.
 			}
 		}
 		return effectTargets;
 	}
 }
