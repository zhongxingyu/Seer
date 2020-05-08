 package com.censoredsoftware.demigods.engine.element;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Tameable;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.util.BlockIterator;
 
 import redis.clients.johm.*;
 
 import com.censoredsoftware.core.util.Randoms;
 import com.censoredsoftware.demigods.engine.Demigods;
 import com.censoredsoftware.demigods.engine.battle.Battle;
 import com.censoredsoftware.demigods.engine.language.TranslationManager;
 import com.censoredsoftware.demigods.engine.player.DCharacter;
 import com.censoredsoftware.demigods.engine.player.DItemStack;
 import com.censoredsoftware.demigods.engine.player.DPlayer;
 import com.censoredsoftware.demigods.engine.player.Pet;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 public abstract class Ability
 {
 	private final Info info;
 	private final Listener listener;
 	private final Runnable runnable;
 	private static final int TARGET_OFFSET = 5;
 
 	public Ability(Info info, Listener listener, Runnable runnable)
 	{
 		this.info = info;
 		this.listener = listener;
 		this.runnable = runnable;
 	}
 
 	public Info getInfo()
 	{
 		return info;
 	}
 
 	public Listener getListener()
 	{
 		return listener;
 	}
 
 	public Runnable getRunnable()
 	{
 		return runnable;
 	}
 
 	public static class Info
 	{
 		private final String deity;
 		private final String name;
 		private final String command;
 		private final String permission;
 		private final int cost;
 		private final int delay;
 		private final int repeat;
 		private final List<String> details;
 		private final Devotion.Type type;
 		private Material weapon;
 
 		public Info(String deity, String name, String command, String permission, int cost, int delay, int repeat, List<String> details, Devotion.Type type)
 		{
 			this.deity = deity;
 			this.name = name;
 			this.command = command;
 			this.permission = permission;
 			this.cost = cost;
 			this.delay = delay;
 			this.repeat = repeat;
 			this.details = details;
 			this.type = type;
 		}
 
 		public Info(String deity, String name, String command, String permission, int cost, int delay, int repeat, List<String> details, Devotion.Type type, Material weapon)
 		{
 			this.deity = deity;
 			this.name = name;
 			this.command = command;
 			this.permission = permission;
 			this.cost = cost;
 			this.delay = delay;
 			this.repeat = repeat;
 			this.details = details;
 			this.type = type;
 			this.weapon = weapon;
 		}
 
 		public String getDeity()
 		{
 			return deity;
 		}
 
 		public String getName()
 		{
 			return name;
 		}
 
 		public String getCommand()
 		{
 			return command;
 		}
 
 		public String getPermission()
 		{
 			return permission;
 		}
 
 		public int getCost()
 		{
 			return cost;
 		}
 
 		public int getDelay()
 		{
 			return delay;
 		}
 
 		public int getRepeat()
 		{
 			return repeat;
 		}
 
 		public List<String> getDetails()
 		{
 			return details;
 		}
 
 		public Devotion.Type getType()
 		{
 			return type;
 		}
 
 		public Material getWeapon()
 		{
 			return weapon;
 		}
 
 		public boolean hasWeapon()
 		{
 			return weapon != null;
 		}
 	}
 
 	@Model
 	public static class Bind
 	{
 		@Id
 		private Long id;
 		@Attribute
 		@Indexed
 		private String identifier;
 		@Attribute
 		private String ability;
 		@Attribute
 		private Integer slot;
 		@Reference
 		private DItemStack item;
 
 		void setIdentifier(String identifier)
 		{
 			this.identifier = identifier;
 		}
 
 		void setAbility(String ability)
 		{
 			this.ability = ability;
 		}
 
 		void setSlot(Integer slot)
 		{
 			this.slot = slot;
 		}
 
 		public void setItem(ItemStack item)
 		{
 			this.item = DItemStack.Util.create(item);
 			JOhm.save(this);
 		}
 
 		public Long getId()
 		{
 			return this.id;
 		}
 
 		public ItemStack getRawItem()
 		{
 			return new ItemStack(this.item.toItemStack().getType());
 		}
 
 		public ItemStack getItem()
 		{
 			return this.item.toItemStack();
 		}
 
 		public String getAbility()
 		{
 			return this.ability;
 		}
 
 		public String getIdentifier()
 		{
 			return this.identifier;
 		}
 
 		public int getSlot()
 		{
 			return this.slot;
 		}
 	}
 
 	@Model
 	public static class Devotion
 	{
 		@Id
 		private Long Id;
 		@Attribute
 		@Indexed
 		private String type;
 		@Attribute
 		private Integer exp;
 		@Attribute
 		private Integer level;
 
 		public enum Type
 		{
 			OFFENSE, DEFENSE, STEALTH, SUPPORT, PASSIVE, ULTIMATE
 		}
 
 		void setType(Type type)
 		{
 			this.type = type.toString();
 		}
 
 		void setExp(Integer exp)
 		{
 			this.exp = exp;
 		}
 
 		void setLevel(Integer level)
 		{
 			this.level = level;
 		}
 
 		public Type getType()
 		{
 			return Type.valueOf(this.type);
 		}
 
 		public Integer getLevel()
 		{
 			return this.level;
 		}
 
 		public Integer getExp()
 		{
 			return this.exp;
 		}
 
 		public Integer getExpGoal()
 		{
 			return (int) Math.ceil(500 * Math.pow(this.level + 1, 2.02)); // TODO: Will need to be tweaked and will possibly be different for each Type.
 		}
 
 		@Override
 		public Object clone() throws CloneNotSupportedException
 		{
 			throw new CloneNotSupportedException();
 		}
 	}
 
 	public static class Util
 	{
 		public static Bind createBind(String ability, int slot)
 		{
 			Bind bind = new Bind();
 			bind.setIdentifier(Randoms.generateString(6));
 			bind.setAbility(ability);
 			bind.setSlot(slot);
 			save(bind);
 			return bind;
 		}
 
 		public static Bind createBind(String ability, int slot, ItemStack item)
 		{
 			Bind bind = new Bind();
 			bind.setIdentifier(Randoms.generateString(6));
 			bind.setAbility(ability);
 			bind.setSlot(slot);
 			bind.setItem(item);
 			save(bind);
 			return bind;
 		}
 
 		public static Devotion createDevotion(Devotion.Type type)
 		{
 			Devotion devotion = new Devotion();
 			devotion.setType(type);
 			devotion.setLevel(Demigods.config.getSettingInt("character.defaults." + type.name().toLowerCase()));
 			save(devotion);
 			return devotion;
 		}
 
 		public static void save(Bind bind)
 		{
 			JOhm.save(bind);
 		}
 
 		public static void save(Devotion devotion)
 		{
 			JOhm.save(devotion);
 		}
 
 		public static Devotion loadDevotion(long id)
 		{
 			return JOhm.get(Devotion.class, id);
 		}
 
 		public static Set<Devotion> loadAllDevotion()
 		{
 			return JOhm.getAll(Devotion.class);
 		}
 
 		private static boolean doAbilityPreProcess(Player player, int cost)
 		{
 			DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 
 			if(!Battle.Util.canTarget(character))
 			{
 				player.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
 				return false;
 			}
 			else if(character.getMeta().getFavor() < cost)
 			{
 				player.sendMessage(ChatColor.YELLOW + "You do not have enough favor.");
 				return false;
 			}
 			else return true;
 		}
 
 		/**
 		 * Returns true if the ability for <code>player</code>, called <code>name</code>,
 		 * with a cost of <code>cost</code>, that is Type <code>type</code>, has
 		 * passed all pre-process tests.
 		 * 
 		 * @param player the player doing the ability
 		 * @param name the name of the ability
 		 * @param cost the cost (in favor) of the ability
 		 * @param info the Info object
 		 * @return true/false depending on if all pre-process tests have passed
 		 */
 		public static boolean doAbilityPreProcess(Player player, String name, int cost, Info info)
 		{
 			// DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 			return doAbilityPreProcess(player, cost); // TODO callAbilityEvent(name, character, cost, info);
 		}
 
 		/**
 		 * Returns true if the ability for <code>player</code>, called <code>name</code>,
 		 * with a cost of <code>cost</code>, that is Type <code>type</code>, that
 		 * is doTargeting the LivingEntity <code>target</code>, has passed all pre-process tests.
 		 * 
 		 * @param player the Player doing the ability
 		 * @param target the LivingEntity being targeted
 		 * @param name the name of the ability
 		 * @param cost the cost (in favor) of the ability
 		 * @param info the Info object
 		 * @return true/false depending on if all pre-process tests have passed
 		 */
 		public static boolean doAbilityPreProcess(Player player, LivingEntity target, String name, int cost, Info info)
 		{
 			DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 
 			if(doAbilityPreProcess(player, cost)) // TODO callAbilityEvent(name, character, cost, info))
 			{
 				if(target == null)
 				{
 					player.sendMessage(ChatColor.YELLOW + "No target found.");
 					return false;
 				}
 				else if(Battle.Util.canParticipate(target) && !Battle.Util.canTarget(Battle.Util.defineParticipant(target)))
 				{
 					player.sendMessage(ChatColor.YELLOW + "Target is in a no-PVP zone.");
 					return false;
 				}
 				else if(target instanceof Player)
 				{
 					DCharacter attacked = DPlayer.Util.getPlayer(((Player) target)).getCurrent();
 					if(attacked != null && DCharacter.Util.areAllied(character, attacked)) return false;
 				}
 				else if(target instanceof Tameable)
 				{
 					Pet attacked = Pet.Util.getTameable(target);
 					if(attacked != null && DCharacter.Util.areAllied(character, attacked.getOwner())) return false;
 				}
 				// TODO Bukkit.getServer().getPluginManager().callEvent(new AbilityTargetEvent(character, target, info));
 				return true;
 			}
 			return false;
 		}
 
 		/**
 		 * Returns true if the callAbilityEvent <code>callAbilityEvent</code> is caused by a left click.
 		 * 
 		 * @param event the interact callAbilityEvent
 		 * @return true/false depending on if the callAbilityEvent is caused by a left click or not
 		 */
 		public static boolean isLeftClick(PlayerInteractEvent event)
 		{
 			Action action = event.getAction();
 			return action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;
 		}
 
 		/**
 		 * Returns the LivingEntity that <code>player</code> is doTargeting.
 		 * 
 		 * @param player the interact callAbilityEvent
 		 * @return the targeted LivingEntity
 		 */
 		public static LivingEntity autoTarget(Player player)
 		{
 			// TODO: Test this for lag.
 
 			// Define variables
 			int range = Demigods.config.getSettingInt("caps.target_range") > 140 ? 140 : Demigods.config.getSettingInt("caps.target_range");
 			int correction = 3;
 			Location target = player.getTargetBlock(null, range).getLocation();
 			BlockIterator iterator = new BlockIterator(player, range);
 			Set<LivingEntity> entities = Sets.newHashSet();
 			List<LivingEntity> targets = Lists.newArrayList();
 
 			// Save the nearby living entities
 			for(Entity entity : player.getNearbyEntities(range, range, range))
 			{
 				if(entity instanceof LivingEntity) entities.add((LivingEntity) entity);
 			}
 
 			// Iterate through the blocks and find the target
 			while(iterator.hasNext())
 			{
 				Block block = iterator.next();
 
 				for(LivingEntity entity : entities)
 				{
 					if(entity.getLocation().distance(block.getLocation()) <= correction)
 					{
 						if(entity instanceof Tameable && ((Tameable) entity).isTamed() && Pet.Util.getTameable(entity) != null)
 						{
 							Pet wrapper = Pet.Util.getTameable(entity);
 							if(DCharacter.Util.areAllied(DPlayer.Util.getPlayer(player).getCurrent(), wrapper.getOwner())) continue;
 						}
 						else
 						{
 							targets.add(entity);
 						}
 					}
 				}
 			}
 
 			// Attempt to return the closest entity to the cursor
 			for(LivingEntity entity : targets)
 			{
 				if(entity.getLocation().distance(target) <= correction) return entity;
 			}
 
 			// If it failed to do that then just return the first entity
 			try
 			{
 				return targets.get(0);
 			}
 			catch(Exception ignored)
 			{}
 
 			return null;
 		}
 
 		public static Location directTarget(Player player)
 		{
 			return player.getTargetBlock(null, Demigods.config.getSettingInt("caps.target_range")).getLocation();
 		}
 
 		/**
 		 * Returns true if the <code>player</code> ability hits <code>target</code>.
 		 * 
 		 * @param player the player using the ability
 		 * @param target the targeted LivingEntity
 		 * @return true/false depending on if the ability hits or misses
 		 */
 		public static boolean doTargeting(Player player, Location target, boolean notify)
 		{
 			DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 			Location toHit = adjustedAimLocation(character, target);
 			if(isHit(target, toHit)) return true;
 			if(notify) player.sendMessage(ChatColor.RED + "Missed..."); // TODO Better message.
 			return false;
 		}
 
 		/**
 		 * Returns the location that <code>character</code> is actually aiming
 		 * at when doTargeting <code>target</code>.
 		 * 
 		 * @param character the character triggering the ability callAbilityEvent
 		 * @param target the location the character is doTargeting at
 		 * @return the aimed at location
 		 */
 		public static Location adjustedAimLocation(DCharacter character, Location target)
 		{
 			// TODO: This needs major work.
 
 			int ascensions = character.getMeta().getAscensions();
 			if(ascensions < 3) ascensions = 3;
 
 			int offset = (int) (TARGET_OFFSET + character.getOfflinePlayer().getPlayer().getLocation().distance(target));
 			int adjustedOffset = offset / ascensions;
 			if(adjustedOffset < 1) adjustedOffset = 1;
 			Random random = new Random();
 			World world = target.getWorld();
 
 			int randomInt = random.nextInt(adjustedOffset);
 			int sampleSpace = random.nextInt(3);
 
 			double X = target.getX();
 			double Z = target.getZ();
 			double Y = target.getY();
 
 			if(sampleSpace == 0)
 			{
 				X += randomInt;
 				Z += randomInt;
 			}
 			else if(sampleSpace == 1)
 			{
 				X -= randomInt;
 				Z -= randomInt;
 			}
 			else if(sampleSpace == 2)
 			{
 				X -= randomInt;
 				Z += randomInt;
 			}
 			else if(sampleSpace == 3)
 			{
 				X += randomInt;
 				Z -= randomInt;
 			}
 
 			return new Location(world, X, Y, Z);
 		}
 
 		/**
 		 * Returns true if <code>target</code> is hit at <code>hit</code>.
 		 * 
 		 * @param target the LivingEntity being targeted
 		 * @param hit the location actually hit
 		 * @return true/false if <code>target</code> is hit
 		 */
 		public static boolean isHit(Location target, Location hit)
 		{
 			return hit.distance(target) <= 2;
 		}
 
 		public static List<Ability> getLoadedAbilities()
 		{
 			return new ArrayList<Ability>()
 			{
 				{
 					for(Deity deity : Demigods.getLoadedDeities().values())
 					{
 						addAll(deity.getAbilities());
 					}
 				}
 			};
 		}
 
 		public static boolean invokeAbilityCommand(Player player, String command)
 		{
 			DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 			for(Ability ability : character.getDeity().getAbilities())
 			{
 				if(ability.getInfo().getType().equals(Devotion.Type.PASSIVE)) continue;
 
 				if(ability.getInfo().getCommand() != null && ability.getInfo().getCommand().equalsIgnoreCase(command))
 				{
 					// Ensure that the deity can be used, permission allows it, etc
 					if(!Deity.Util.canUseDeity(player, ability.getInfo().getDeity())) return true;
 					if(!player.hasPermission(ability.getInfo().getPermission())) return true;
 
 					// Handle enabling the command
 					final Info abilityInfo = ability.getInfo();
 					String abilityName = abilityInfo.getName();
 
 					if(!character.getMeta().isBound(ability.getInfo().getName()))
 					{
 						if(!abilityInfo.hasWeapon() && player.getItemInHand() != null && !player.getItemInHand().getType().equals(Material.AIR))
 						{
 							// Slot must be empty
 							player.sendMessage(ChatColor.RED + Demigods.text.getText(TranslationManager.Text.ERROR_BIND_TO_SLOT));
 							return true;
 						}
 						else if(abilityInfo.hasWeapon())
 						{
 							// Weapon required
 							player.sendMessage(ChatColor.RED + Demigods.text.getText(TranslationManager.Text.ERROR_BIND_WEAPON_REQUIRED).replace("{weapon}", abilityInfo.getWeapon().name().toLowerCase().replace("_", " ")).replace("{ability}", abilityName.toLowerCase()));
 							return true;
 						}
 
 						// Create the bind
 						final Bind bind = createBind(ability.getInfo().getName(), player.getInventory().getHeldItemSlot());
 
 						// Handle the item
 						ItemStack item = abilityInfo.hasWeapon() ? player.getItemInHand() : new ItemStack(Material.STICK);
 						ItemMeta itemMeta = item.getItemMeta();
 						itemMeta.setDisplayName(ChatColor.RESET + abilityName);
 
 						itemMeta.setLore(new ArrayList<String>()
 						{
 							{
 								add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Consumes " + abilityInfo.getCost() + " favor per use.");
 								add("");
 								for(String detail : abilityInfo.getDetails())
 								{
 									add(ChatColor.AQUA + detail);
 								}
 								add("");
 								add(ChatColor.BLACK + "" + ChatColor.STRIKETHROUGH + "Identifier: " + ChatColor.MAGIC + bind.getIdentifier());
 							}
 						});
 
 						// Set the item meta
 						item.setItemMeta(itemMeta);
 
						// Set the bind item
 						bind.setItem(item);
 
 						// Save the bind and give the item
 						player.getInventory().setItemInHand(item);
 						character.getMeta().addBind(bind);
 
 						// Let them know
 						player.sendMessage(ChatColor.GREEN + Demigods.text.getText(TranslationManager.Text.SUCCESS_ABILITY_BOUND).replace("{ability}", StringUtils.capitalize(abilityName)).replace("{slot}", "" + (player.getInventory().getHeldItemSlot() + 1)));
 
 						return true;
 					}
 					else
 					{
 						// Get the bind for info
 						Bind bind = character.getMeta().getBind(abilityName);
 
 						// Remove the bind and item
 						character.getMeta().removeBind(bind);
 						player.getInventory().setItem(bind.getSlot(), new ItemStack(Material.AIR));
 
 						// Let them know
 						player.sendMessage(ChatColor.GREEN + Demigods.text.getText(TranslationManager.Text.SUCCESS_ABILITY_UNBOUND).replace("{ability}", StringUtils.capitalize(abilityName)));
 
 						return true;
 					}
 				}
 			}
 			return false;
 		}
 
 		public static void dealDamage(LivingEntity source, LivingEntity target, double amount, EntityDamageEvent.DamageCause cause)
 		{
 			if(source instanceof Player)
 			{
 				DPlayer owner = DPlayer.Util.getPlayer(((Player) source));
 				if(owner != null)
 				{
 					if(target instanceof Player)
 					{
 						DCharacter targetChar = DPlayer.Util.getPlayer(((Player) target)).getCurrent();
 						if(targetChar != null && DCharacter.Util.areAllied(owner.getCurrent(), targetChar)) return;
 					}
 					else if(target instanceof Tameable && ((Tameable) target).isTamed())
 					{
 						Pet wrapper = Pet.Util.getTameable(target);
 						if(wrapper != null && DCharacter.Util.areAllied(owner.getCurrent(), wrapper.getOwner())) return;
 					}
 				}
 			}
 			EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(source, target, cause, amount);
 			target.setLastDamageCause(event);
 			if(amount >= 1 && !event.isCancelled()) target.damage(amount);
 		}
 	}
 }
