 package com.mitsugaru.Karmiconomy.events;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.enchantment.EnchantItemEvent;
 import org.bukkit.event.entity.EntityCreatePortalEvent;
 import org.bukkit.event.entity.EntityPortalEnterEvent;
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.event.entity.EntityTameEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.CraftItemEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.painting.PaintingPlaceEvent;
 import org.bukkit.event.player.PlayerBedEnterEvent;
 import org.bukkit.event.player.PlayerBedLeaveEvent;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import org.bukkit.event.player.PlayerBucketFillEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerEggThrowEvent;
 import org.bukkit.event.player.PlayerGameModeChangeEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerToggleSneakEvent;
 import org.bukkit.event.player.PlayerToggleSprintEvent;
 //import org.bukkit.event.vehicle.VehicleEnterEvent;
 //import org.bukkit.event.vehicle.VehicleExitEvent;
 
 import com.mitsugaru.Karmiconomy.Item;
 import com.mitsugaru.Karmiconomy.Karmiconomy;
 import com.mitsugaru.Karmiconomy.config.Config;
 import com.mitsugaru.Karmiconomy.database.DatabaseHandler;
 import com.mitsugaru.Karmiconomy.database.Field;
 import com.mitsugaru.Karmiconomy.events.EventLogic.DenyType;
 
 public class KarmiconomyListener implements Listener
 {
 	private Karmiconomy plugin;
 	private DatabaseHandler db;
 	private Config config;
 
 	public KarmiconomyListener(Karmiconomy plugin)
 	{
 		this.plugin = plugin;
 		this.db = plugin.getDatabaseHandler();
 		this.config = plugin.getPluginConfig();
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void chatValid(final PlayerChatEvent event)
 	{
 		if (!event.isCancelled() && config.isEnabled(Field.CHAT)
 				&& event.getPlayer() != null)
 		{
 			final Player player = event.getPlayer();
 			if (config.checkWorld(Field.CHAT, player.getWorld().getName()))
 			{
 				if (EventLogic.deny(Field.CHAT, player, config, null, null))
 				{
 					// Cancel event
 					event.setCancelled(true);
 					if (config.debugEvents)
 					{
 						final Map<String, String> details = new HashMap<String, String>();
 						details.put("Player", player.getName());
 						details.put("Message", event.getMessage());
 						details.put("Cancelled", "true");
 						EventLogic.debugEvent(event, details);
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void chat(final PlayerChatEvent event)
 	{
 		if (!event.isCancelled() && config.isEnabled(Field.CHAT)
 				&& event.getPlayer() != null)
 		{
 			final Player player = event.getPlayer();
 			// TODO filter?
 			if (config.checkWorld(Field.CHAT, player.getWorld().getName()))
 			{
 				EventLogic.hitPayIncrement(Field.CHAT, player, config, null,
 						null);
 			}
 			if (config.debugEvents)
 			{
 				final Map<String, String> details = new HashMap<String, String>();
 				details.put("Player", player.getName());
 				details.put("Message", event.getMessage());
 				EventLogic.debugEvent(event, details);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void commandValid(final PlayerCommandPreprocessEvent event)
 	{
 		if (!event.isCancelled() && config.isEnabled(Field.COMMAND)
 				&& event.getPlayer() != null)
 		{
 			final Player player = event.getPlayer();
 			if (config.checkWorld(Field.COMMAND, player.getWorld().getName()))
 			{
 				if (EventLogic.deny(Field.COMMAND, player, config, null,
 						event.getMessage()))
 				{
 					// Deny
 					event.setCancelled(true);
 					if (config.debugEvents)
 					{
 						final Map<String, String> details = new HashMap<String, String>();
 						details.put("Player", player.getName());
 						details.put("Message", event.getMessage());
 						details.put("Cancelled", "true");
 						EventLogic.debugEvent(event, details);
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void command(final PlayerCommandPreprocessEvent event)
 	{
 		if (!event.isCancelled() && config.isEnabled(Field.COMMAND)
 				&& event.getPlayer() != null)
 		{
 			final Player player = event.getPlayer();
 			// Pay on command
 			// Check if hit limit
 			if (config.checkWorld(Field.COMMAND, player.getWorld().getName()))
 			{
 				EventLogic.hitPayIncrement(Field.COMMAND, player, config, null,
 						event.getMessage());
 			}
 			// TODO pay based on command match
 			if (config.debugEvents)
 			{
 				final Map<String, String> details = new HashMap<String, String>();
 				details.put("Player", player.getName());
 				details.put("Message", event.getMessage());
 				EventLogic.debugEvent(event, details);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void blockPlaceValid(final BlockPlaceEvent event)
 	{
 		if (!event.isCancelled() && config.isEnabled(Field.BLOCK_PLACE))
 		{
 			final Item placed = new Item(event.getBlockPlaced().getTypeId(),
 					event.getBlockPlaced().getData(), (short) 0);
 			final boolean denyPay = config.getDenyPay(Field.BLOCK_PLACE,
 					placed, null);
 			final boolean denyLimit = config.getDenyLimit(
 					Field.BLOCK_PLACE, placed, null);
 			if ((denyPay || denyLimit) && event.getPlayer() != null)
 			{
 				final Player player = event.getPlayer();
 				if (config.checkWorld(Field.BLOCK_PLACE, player.getWorld()
 						.getName()))
 				{
 					if (EventLogic.deny(Field.BLOCK_PLACE, player, config,
 							placed, null))
 					{
 						// Deny
 						event.setCancelled(true);
 						if (config.debugEvents)
 						{
 							final Map<String, String> details = new HashMap<String, String>();
 							details.put("Player", player.getName());
 							if (event.getBlock() != null)
 							{
 								details.put("Block", event.getBlock()
 										.toString());
 							}
 							if (event.getBlockAgainst() != null)
 							{
 								details.put("Block against", event
 										.getBlockAgainst().toString());
 							}
 							details.put("Cancelled", "true");
 							EventLogic.debugEvent(event, details);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void blockPlace(final BlockPlaceEvent event)
 	{
 		if (!event.isCancelled() && config.isEnabled(Field.BLOCK_PLACE)
 				&& event.getPlayer() != null)
 		{
 			// Pay on block place
 
 			final Player player = event.getPlayer();
 			if (config.checkWorld(Field.BLOCK_PLACE, player.getWorld()
 					.getName()))
 			{
 				final Item placed = new Item(
 						event.getBlockPlaced().getTypeId(), event
 								.getBlockPlaced().getData(), (short) 0);
 				EventLogic.hitPayIncrement(Field.BLOCK_PLACE, player, config,
 						placed, null);
 			}
 			if (config.debugEvents)
 			{
 				final Map<String, String> details = new HashMap<String, String>();
 				details.put("Player", player.getName());
 				if (event.getBlock() != null)
 				{
 					details.put("Block", event.getBlock().toString());
 				}
 				if (event.getBlockAgainst() != null)
 				{
 					details.put("Block against", event.getBlockAgainst()
 							.toString());
 				}
 				EventLogic.debugEvent(event, details);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void blockDestroyValid(final BlockBreakEvent event)
 	{
 		if (!event.isCancelled() && config.isEnabled(Field.BLOCK_DESTROY))
 		{
 			final Item destroyed = new Item(event.getBlock().getTypeId(), event
 					.getBlock().getData(), (short) 0);
 			final boolean denyPay = config.getDenyPay(Field.BLOCK_DESTROY,
 					destroyed, null);
 			final boolean denyLimit = config.getDenyLimit(
 					Field.BLOCK_DESTROY, destroyed, null);
 			if ((denyPay || denyLimit) && event.getPlayer() != null)
 			{
 				final Player player = event.getPlayer();
 				if (config.checkWorld(Field.BLOCK_DESTROY, player.getWorld()
 						.getName()))
 				{
 					if (EventLogic.deny(Field.BLOCK_DESTROY, player, config,
 							destroyed, null))
 					{
 						// Deny
 						event.setCancelled(true);
 						if (config.debugEvents)
 						{
 							final Map<String, String> details = new HashMap<String, String>();
 							details.put("Player", player.getName());
 							if (event.getBlock() != null)
 							{
 								details.put("Block", event.getBlock()
 										.toString());
 							}
 							details.put("Cancelled", "true");
 							EventLogic.debugEvent(event, details);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void blockDestroy(final BlockBreakEvent event)
 	{
 		if (!event.isCancelled() && config.isEnabled(Field.BLOCK_DESTROY)
 				&& event.getPlayer() != null)
 		{
 			// Pay on block break
 			final Player player = event.getPlayer();
 			if (config.checkWorld(Field.BLOCK_DESTROY, player.getWorld()
 					.getName()))
 			{
 				final Item destroyed = new Item(event.getBlock().getTypeId(),
 						event.getBlock().getData(), (short) 0);
 				EventLogic.hitPayIncrement(Field.BLOCK_DESTROY, player, config,
 						destroyed, null);
 			}
 			if (config.debugEvents)
 			{
 				final Map<String, String> details = new HashMap<String, String>();
 				details.put("Player", player.getName());
 				if (event.getBlock() != null)
 				{
 					details.put("Block", event.getBlock().toString());
 				}
 				EventLogic.debugEvent(event, details);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void craftItemValid(final CraftItemEvent event)
 	{
 		if (!event.isCancelled() && config.isEnabled(Field.ITEM_CRAFT))
 		{
 			final Item craft = new Item(event.getRecipe().getResult());
 			final boolean denyPay = config.getDenyPay(Field.ITEM_CRAFT,
 					craft, null);
 			final boolean denyLimit = config.getDenyLimit(Field.ITEM_CRAFT,
 					craft, null);
 			if ((denyPay || denyLimit) && event.getWhoClicked() != null)
 			{
 				if (event.getWhoClicked() instanceof Player)
 				{
 					final Player player = (Player) event.getWhoClicked();
 					if (config.checkWorld(Field.ITEM_CRAFT, player.getWorld()
 							.getName()))
 					{
 						if (EventLogic.deny(Field.ITEM_CRAFT, player, config,
 								craft, null))
 						{
 							// Deny
 							event.setCancelled(true);
 							if (config.debugEvents)
 							{
 								final Map<String, String> details = new HashMap<String, String>();
 								details.put("Player", player.getName());
 								if (event.getRecipe().getResult() != null)
 								{
 									details.put("Result", event.getRecipe()
 											.getResult().toString());
 								}
 								details.put("Cancelled", "true");
 								EventLogic.debugEvent(event, details);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void craftItem(final CraftItemEvent event)
 	{
 		if (!event.isCancelled() && config.isEnabled(Field.ITEM_CRAFT)
 				&& event.getWhoClicked() != null)
 		{
 			if (event.getWhoClicked() instanceof Player)
 			{
 				final Player player = (Player) event.getWhoClicked();
 				if (config.checkWorld(Field.ITEM_CRAFT, player.getWorld()
 						.getName()))
 				{
 					final Item craft = new Item(event.getRecipe().getResult());
 					// Pay on craft
 					EventLogic.hitPayIncrement(Field.ITEM_CRAFT, player,
 							config, craft, null);
 				}
 				if (config.debugEvents)
 				{
 					final Map<String, String> details = new HashMap<String, String>();
 					details.put("Player", player.getName());
 					if (event.getRecipe().getResult() != null)
 					{
 						details.put("Result", event.getRecipe().getResult()
 								.toString());
 					}
 					EventLogic.debugEvent(event, details);
 				}
 
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void enchantItemValid(final EnchantItemEvent event)
 	{
 		if (!event.isCancelled() && config.isEnabled(Field.ITEM_ENCHANT))
 		{
 			final Item enchant = new Item(event.getItem());
 			final boolean denyPay = config.getDenyPay(Field.ITEM_ENCHANT,
 					enchant, null);
 			final boolean denyLimit = config.getDenyLimit(
 					Field.ITEM_ENCHANT, enchant, null);
 			if ((denyPay || denyLimit) && event.getEnchanter() != null)
 			{
 				final Player player = (Player) event.getEnchanter();
 				if (config.checkWorld(Field.ITEM_ENCHANT, player.getWorld()
 						.getName()))
 				{
 					if (EventLogic.deny(Field.ITEM_ENCHANT, player, config,
 							enchant, null))
 					{
 						// Deny
 						event.setCancelled(true);
 						if (config.debugEvents)
 						{
 							final Map<String, String> details = new HashMap<String, String>();
 							details.put("Player", player.getName());
 							if (event.getEnchantsToAdd() != null)
 							{
 								for (Map.Entry<Enchantment, Integer> entry : event
 										.getEnchantsToAdd().entrySet())
 								{
 									details.put("Enchantment/Level", entry
 											.getKey().getName()
 											+ "/"
 											+ entry.getValue().toString());
 								}
 							}
 							details.put("Cancelled", "true");
 							EventLogic.debugEvent(event, details);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void enchantItem(final EnchantItemEvent event)
 	{
 		if (!event.isCancelled() && config.isEnabled(Field.ITEM_ENCHANT)
 				&& event.getEnchanter() != null)
 		{
 			final Player player = event.getEnchanter();
 			if (config.checkWorld(Field.ITEM_ENCHANT, player.getWorld()
 					.getName()))
 			{
 				// Pay on enchant
 				final Item enchant = new Item(event.getItem());
 				EventLogic.hitPayIncrement(Field.ITEM_ENCHANT, player, config,
 						enchant, null);
 			}
 			if (config.debugEvents)
 			{
 				final Map<String, String> details = new HashMap<String, String>();
 				details.put("Player", player.getName());
 				if (event.getEnchantsToAdd() != null)
 				{
 					for (Map.Entry<Enchantment, Integer> entry : event
 							.getEnchantsToAdd().entrySet())
 					{
 						details.put("Enchantment/Level", entry.getKey()
 								.getName() + "/" + entry.getValue().toString());
 					}
 				}
 				EventLogic.debugEvent(event, details);
 			}
 
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void createPortalValid(final EntityCreatePortalEvent event)
 	{
 		if (!event.isCancelled()
 				&& (config.portalCreateNether || config.portalCreateEnd || config.portalCreateCustom)
 				&& event.getEntity() != null)
 		{
 			if (event.getEntity() instanceof Player)
 			{
 				final Player player = (Player) event.getEntity();
 				// Deny based on portal type
 				boolean cancel = false;
 				switch (event.getPortalType())
 				{
 					case NETHER:
 					{
 						if (config.checkWorld(Field.PORTAL_CREATE_NETHER,
 								player.getWorld().getName()))
 						{
 							if (EventLogic.deny(Field.PORTAL_CREATE_NETHER,
 									player, config, null, null))
 							{
 								// Deny
 								cancel = true;
 							}
 						}
 						break;
 					}
 					case ENDER:
 					{
 						if (config.checkWorld(Field.PORTAL_CREATE_END, player
 								.getWorld().getName()))
 						{
 							if (EventLogic.deny(Field.PORTAL_CREATE_END,
 									player, config, null, null))
 							{
 								// Deny
 								cancel = true;
 							}
 						}
 						break;
 					}
 					case CUSTOM:
 					{
 						if (config.checkWorld(Field.PORTAL_CREATE_CUSTOM,
 								player.getWorld().getName()))
 						{
 							if (EventLogic.deny(Field.PORTAL_CREATE_CUSTOM,
 									player, config, null, null))
 							{
 								// Deny
 								cancel = true;
 							}
 						}
 						break;
 					}
 					default:
 					{
 						// Unknown
 						if (config.debugUnhandled)
 						{
 							plugin.getLogger().warning(
 									"Unhandled '" + event.getEventName()
 											+ "' for portal: "
 											+ event.getPortalType());
 						}
 						break;
 					}
 				}
 				if (cancel)
 				{
 					// Deny
 					event.setCancelled(true);
 					if (config.debugEvents)
 					{
 						final Map<String, String> details = new HashMap<String, String>();
 						details.put("Player", player.getName());
 						if (event.getPortalType() != null)
 						{
 							details.put("Portal", event.getPortalType()
 									.toString());
 						}
 						details.put("Cancelled", "true");
 						EventLogic.debugEvent(event, details);
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void createPortal(final EntityCreatePortalEvent event)
 	{
 		if (!event.isCancelled()
 				&& (config.portalCreateNether || config.portalCreateEnd || config.portalCreateCustom)
 				&& event.getEntity() != null)
 		{
 			if (event.getEntity() instanceof Player)
 			{
 				final Player player = (Player) event.getEntity();
 				// Pay on portal create, based on portal type
 				switch (event.getPortalType())
 				{
 					case NETHER:
 					{
 						if (config.portalCreateNether)
 						{
 							if (config.checkWorld(Field.PORTAL_CREATE_NETHER,
 									player.getWorld().getName()))
 							{
 								EventLogic.hitPayIncrement(
 										Field.PORTAL_CREATE_NETHER, player,
 										config, null, null);
 							}
 						}
 						break;
 					}
 					case ENDER:
 					{
 						if (config.portalCreateEnd)
 						{
 							if (config.checkWorld(Field.PORTAL_CREATE_END,
 									player.getWorld().getName()))
 							{
 								EventLogic.hitPayIncrement(
 										Field.PORTAL_CREATE_END, player,
 										config, null, null);
 							}
 						}
 						break;
 					}
 					case CUSTOM:
 					{
 						if (config.portalCreateCustom)
 						{
 							if (config.checkWorld(Field.PORTAL_CREATE_CUSTOM,
 									player.getWorld().getName()))
 							{
 								EventLogic.hitPayIncrement(
 										Field.PORTAL_CREATE_CUSTOM, player,
 										config, null, null);
 							}
 						}
 						break;
 					}
 					default:
 					{
 						// Unknown
 						if (config.debugUnhandled)
 						{
 							plugin.getLogger().warning(
 									"Unhandled '" + event.getEventName()
 											+ "' for portal: "
 											+ event.getPortalType());
 						}
 						break;
 					}
 				}
 				if (config.debugEvents)
 				{
 					final Map<String, String> details = new HashMap<String, String>();
 					details.put("Player", player.getName());
 					if (event.getPortalType() != null)
 					{
 						details.put("Portal", event.getPortalType().toString());
 					}
 					EventLogic.debugEvent(event, details);
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void enterPortal(final EntityPortalEnterEvent event)
 	{
 		if (config.isEnabled(Field.PORTAL_ENTER) && event.getEntity() != null)
 		{
 			if (event.getEntity() instanceof Player)
 			{
 				final Player player = (Player) event.getEntity();
 				// Pay on portal enter
 				if (config.checkWorld(Field.PORTAL_ENTER, player.getWorld()
 						.getName()))
 				{
 					EventLogic.hitPayIncrement(Field.PORTAL_ENTER, player,
 							config, null, null);
 				}
 				if (config.debugEvents)
 				{
 					final Map<String, String> details = new HashMap<String, String>();
 					details.put("Player", player.getName());
 					EventLogic.debugEvent(event, details);
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void shootBowValid(final EntityShootBowEvent event)
 	{
 		if (!event.isCancelled() && config.isEnabled(Field.BOW_SHOOT)
 				&& event.getEntity() != null)
 		{
 			if (event.getEntity() instanceof Player)
 			{
 				final Player player = (Player) event.getEntity();
 				boolean cancel = false;
 				if (config.checkWorld(Field.BOW_SHOOT, player.getWorld()
 						.getName()))
 				{
 					if (config.shootBowDenyForce
 							&& (event.getForce() < config.shootBowForce))
 					{
 						cancel = true;
 						EventLogic.sendLackMessage(player, DenyType.FORCE,
 								Field.BOW_SHOOT.name(), "" + event.getForce());
 					}
 
 					else if (EventLogic.deny(Field.BOW_SHOOT, player, config,
 							null, null))
 					{
 						cancel = true;
 					}
 				}
 				if (cancel)
 				{
 					// Deny
 					event.setCancelled(true);
 					if (config.debugEvents)
 					{
 						final Map<String, String> details = new HashMap<String, String>();
 						details.put("Player", player.getName());
 						details.put("Force", "" + event.getForce());
 						details.put("Cancelled", "true");
 						EventLogic.debugEvent(event, details);
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void shootBow(final EntityShootBowEvent event)
 	{
 		if (!event.isCancelled() && config.isEnabled(Field.BOW_SHOOT)
 				&& event.getEntity() != null
 				&& (event.getForce() >= config.shootBowForce))
 		{
 			if (event.getEntity() instanceof Player)
 			{
 				final Player player = (Player) event.getEntity();
 				if (config.checkWorld(Field.BOW_SHOOT, player.getWorld()
 						.getName()))
 				{
 					EventLogic.hitPayIncrement(Field.BOW_SHOOT, player, config,
 							null, null);
 				}
 				// TODO can pay based on force
 				// TODO can pay based on entity?
 				if (config.debugEvents)
 				{
 					final Map<String, String> details = new HashMap<String, String>();
 					details.put("Player", player.getName());
 					details.put("Force", "" + event.getForce());
 					EventLogic.debugEvent(event, details);
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void tameValid(final EntityTameEvent event)
 	{
 		// Move config check inside if
 		if (!event.isCancelled()
 				&& (config.isEnabled(Field.TAME_OCELOT) || config
 						.isEnabled(Field.TAME_WOLF))
 				&& event.getOwner() != null)
 		{
 			if (event.getOwner() instanceof Player)
 			{
 				boolean cancel = false;
 				final Player player = (Player) event.getOwner();
 				// Check entity tamed
 				switch (event.getEntityType())
 				{
 					case OCELOT:
 					{
 						// Ocelot
 						if (config.checkWorld(Field.TAME_OCELOT, player
 								.getWorld().getName()))
 						{
 							if (EventLogic.deny(Field.TAME_OCELOT, player,
 									config, null, null))
 							{
 								// Deny
 								cancel = true;
 							}
 						}
 						break;
 					}
 					case WOLF:
 					{
 						// Wolf
 						if (config.checkWorld(Field.TAME_WOLF, player
 								.getWorld().getName()))
 						{
 							if (EventLogic.deny(Field.TAME_WOLF, player,
 									config, null, null))
 							{
 								// Deny
 								cancel = true;
 							}
 						}
 						break;
 					}
 					default:
 					{
 						if (config.debugUnhandled)
 						{
 							plugin.getLogger().warning(
 									"Unhandled " + event.getEventName()
 											+ " for entity '"
 											+ event.getEntityType() + "'");
 						}
 						break;
 					}
 				}
 				// Cancel
 				if (cancel)
 				{
 					event.setCancelled(true);
 					if (config.debugEvents)
 					{
 						final Map<String, String> details = new HashMap<String, String>();
 						details.put("Player", player.getName());
 						if (event.getEntityType() != null)
 						{
 							details.put("EntityType", event.getEntityType()
 									.toString());
 						}
 						details.put("Cancelled", "true");
 						EventLogic.debugEvent(event, details);
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void tame(final EntityTameEvent event)
 	{
 		// Move config check inside if
 		if (!event.isCancelled() && event.getOwner() != null)
 		{
 			if (event.getOwner() instanceof Player)
 			{
 				// Pay on tame
 				final Player player = (Player) event.getOwner();
 				// Check entity tamed
 				//TODO add extra for defining what they tamed in the message
 				switch (event.getEntityType())
 				{
 					case OCELOT:
 					{
 						// Ocelot
 						if (config.isEnabled(Field.TAME_OCELOT))
 						{
 							if (config.checkWorld(Field.TAME_OCELOT, player
 									.getWorld().getName()))
 							{
 								EventLogic.hitPayIncrement(Field.TAME_OCELOT,
 										player, config, null, null);
 							}
 						}
 						break;
 					}
 					case WOLF:
 					{
 						// Wolf
 						if (config.isEnabled(Field.TAME_WOLF))
 						{
 							if (config.checkWorld(Field.TAME_WOLF, player
 									.getWorld().getName()))
 							{
 								EventLogic.hitPayIncrement(Field.TAME_WOLF,
 										player, config, null, null);
 							}
 						}
 						break;
 					}
 					default:
 					{
 						if (config.debugUnhandled)
 						{
 							plugin.getLogger().warning(
 									"Unhandled " + event.getEventName()
 											+ " for entity '"
 											+ event.getEntityType() + "'");
 						}
 						break;
 					}
 				}
 				if (config.debugEvents)
 				{
 					final Map<String, String> details = new HashMap<String, String>();
 					details.put("Player", player.getName());
 					if (event.getEntityType() != null)
 					{
 						details.put("EntityType", event.getEntityType()
 								.toString());
 					}
 					EventLogic.debugEvent(event, details);
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void paintingPlaceValid(final PaintingPlaceEvent event)
 	{
 		if (!event.isCancelled() && config.isEnabled(Field.PAINTING_PLACE)
 				&& event.getPlayer() != null)
 		{
 			final Player player = event.getPlayer();
 			if (config.checkWorld(Field.PAINTING_PLACE, player.getWorld()
 					.getName()))
 			{
 				if (EventLogic.deny(Field.PAINTING_PLACE, player, config, null,
 						null))
 				{
 					// Deny
 					event.setCancelled(true);
 					// TODO also need to get painting break
 					if (config.debugEvents)
 					{
 						final Map<String, String> details = new HashMap<String, String>();
 						details.put("Player", player.getName());
 						if (event.getPainting() != null)
 						{
 							details.put("Painting", event.getPainting()
 									.toString());
 						}
 						details.put("Cancelled", "true");
 						EventLogic.debugEvent(event, details);
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void paintingPlace(final PaintingPlaceEvent event)
 	{
 		if (!event.isCancelled() && config.isEnabled(Field.PAINTING_PLACE)
 				&& event.getPlayer() != null)
 		{
 			final Player player = event.getPlayer();
 			// Pay on place
 			if (config.checkWorld(Field.PAINTING_PLACE, player.getWorld()
 					.getName()))
 			{
 				EventLogic.hitPayIncrement(Field.PAINTING_PLACE, player,
 						config, null, null);
 			}
 			if (config.debugEvents)
 			{
 				final Map<String, String> details = new HashMap<String, String>();
 				details.put("Player", player.getName());
 				if (event.getPainting() != null)
 				{
 					details.put("Painting", event.getPainting().toString());
 				}
 				EventLogic.debugEvent(event, details);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void bedEnterValid(final PlayerBedEnterEvent event)
 	{
 		if (!event.isCancelled() && config.isEnabled(Field.BED_ENTER)
 				&& event.getPlayer() != null)
 		{
 			final Player player = event.getPlayer();
 			if (config.checkWorld(Field.BED_ENTER, player.getWorld().getName()))
 			{
 				if (EventLogic
 						.deny(Field.BED_ENTER, player, config, null, null))
 				{
 					// Deny
 					event.setCancelled(true);
 					if (config.debugEvents)
 					{
 						final Map<String, String> details = new HashMap<String, String>();
 						details.put("Player", player.getName());
 						if (event.getBed() != null)
 						{
 							details.put("Bed", event.getBed().toString());
 						}
 						details.put("Cancelled", "true");
 						EventLogic.debugEvent(event, details);
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void bedEnter(final PlayerBedEnterEvent event)
 	{
 		if (!event.isCancelled() && config.isEnabled(Field.BED_ENTER)
 				&& event.getPlayer() != null)
 		{
 			final Player player = event.getPlayer();
 			// Pay on enter
 			if (config.checkWorld(Field.BED_ENTER, player.getWorld().getName()))
 			{
 				EventLogic.hitPayIncrement(Field.BED_ENTER, player, config,
 						null, null);
 			}
 			if (config.debugEvents)
 			{
 				final Map<String, String> details = new HashMap<String, String>();
 				details.put("Player", player.getName());
 				if (event.getBed() != null)
 				{
 					details.put("Bed", event.getBed().toString());
 				}
 				EventLogic.debugEvent(event, details);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void bedLeave(final PlayerBedLeaveEvent event)
 	{
 		if (config.isEnabled(Field.BED_LEAVE) && event.getPlayer() != null)
 		{
 			final Player player = event.getPlayer();
 			// Pay on leave
 			if (config.checkWorld(Field.BED_LEAVE, player.getWorld().getName()))
 			{
 				EventLogic.hitPayIncrement(Field.BED_LEAVE, player, config,
 						null, null);
 			}
 			if (config.debugEvents)
 			{
 				final Map<String, String> details = new HashMap<String, String>();
 				details.put("Player", player.getName());
 				if (event.getBed() != null)
 				{
 					details.put("Bed", event.getBed().toString());
 				}
 				EventLogic.debugEvent(event, details);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void bucketEmptyValid(final PlayerBucketEmptyEvent event)
 	{
 		if (!event.isCancelled()
 				&& (config.isEnabled(Field.BUCKET_EMPTY_WATER) || config
 						.isEnabled(Field.BUCKET_EMPTY_LAVA))
 				&& event.getPlayer() != null)
 		{
 			final Player player = event.getPlayer();
 			boolean cancel = false;
 			// switch for type of bucket
 			switch (event.getBucket())
 			{
 				case LAVA_BUCKET:
 				{
 					if (config.checkWorld(Field.BUCKET_EMPTY_LAVA, player
 							.getWorld().getName()))
 					{
 						if (EventLogic.deny(Field.BUCKET_EMPTY_LAVA, player,
 								config, null, null))
 						{
 							// deny
 							cancel = true;
 						}
 					}
 					break;
 				}
 				case WATER_BUCKET:
 				{
 					if (config.checkWorld(Field.BUCKET_EMPTY_WATER, player
 							.getWorld().getName()))
 					{
 						if (EventLogic.deny(Field.BUCKET_EMPTY_WATER, player,
 								config, null, null))
 						{
 							// Deny
 							cancel = true;
 						}
 					}
 					break;
 				}
 				default:
 				{
 					if (config.debugUnhandled)
 					{
 						plugin.getLogger().warning(
 								"Unhandled " + event.getEventName()
 										+ " for type '"
 										+ event.getBucket().name() + "'");
 					}
 					break;
 				}
 			}
 			if (cancel)
 			{
 				event.setCancelled(true);
 				if (config.debugEvents)
 				{
 					final Map<String, String> details = new HashMap<String, String>();
 					details.put("Player", player.getName());
 					if (event.getBucket() != null)
 					{
 						details.put("Bucket", event.getBucket().toString());
 					}
 					details.put("Cancelled", "true");
 					EventLogic.debugEvent(event, details);
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void bucketEmpty(final PlayerBucketEmptyEvent event)
 	{
 		if (!event.isCancelled() && event.getPlayer() != null)
 		{
 			final Player player = event.getPlayer();
 			// Switch for type of bucket
 			switch (event.getBucket())
 			{
 				case LAVA_BUCKET:
 				{
 					// Pay on empty
 					if (config.isEnabled(Field.BUCKET_EMPTY_LAVA))
 					{
 						if (config.checkWorld(Field.BUCKET_EMPTY_LAVA, player
 								.getWorld().getName()))
 						{
 							EventLogic.hitPayIncrement(Field.BUCKET_EMPTY_LAVA,
 									player, config, null, null);
 						}
 					}
 					break;
 				}
 				case WATER_BUCKET:
 				{
 					// Pay on empty
 					if (config.isEnabled(Field.BUCKET_EMPTY_WATER))
 					{
 						if (config.checkWorld(Field.BUCKET_EMPTY_LAVA, player
 								.getWorld().getName()))
 						{
 							EventLogic.hitPayIncrement(
 									Field.BUCKET_EMPTY_WATER, player, config,
 									null, null);
 						}
 					}
 					break;
 				}
 				default:
 				{
 					if (config.debugUnhandled)
 					{
 						plugin.getLogger().warning(
 								"Unhandled " + event.getEventName()
 										+ " for type '"
 										+ event.getBucket().name() + "'");
 					}
 					break;
 				}
 			}
 			if (config.debugEvents)
 			{
 				final Map<String, String> details = new HashMap<String, String>();
 				details.put("Player", player.getName());
 				if (event.getBucket() != null)
 				{
 					details.put("Bucket", event.getBucket().toString());
 				}
 				EventLogic.debugEvent(event, details);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void bucketFillValid(final PlayerBucketFillEvent event)
 	{
 		if (!event.isCancelled()
 				&& (config.isEnabled(Field.BUCKET_FILL_WATER) || config
 						.isEnabled(Field.BUCKET_FILL_LAVA))
 				&& event.getPlayer() != null && event.getBlockClicked() != null)
 		{
 			final Player player = event.getPlayer();
 			boolean cancel = false;
 			// Switch for type of bucket
 			switch (event.getBlockClicked().getType())
 			{
 				case STATIONARY_LAVA:
 				{
 					if (config.checkWorld(Field.BUCKET_FILL_LAVA, player
 							.getWorld().getName()))
 					{
 						if (EventLogic.deny(Field.BUCKET_FILL_LAVA, player,
 								config, null, null))
 						{
 							// Deny
 							cancel = true;
 						}
 					}
 					break;
 				}
 				case STATIONARY_WATER:
 				{
 					if (config.checkWorld(Field.BUCKET_FILL_WATER, player
 							.getWorld().getName()))
 					{
 						if (EventLogic.deny(Field.BUCKET_FILL_WATER, player,
 								config, null, null))
 						{
 							// Deny
 							cancel = true;
 						}
 					}
 					break;
 				}
 				default:
 				{
 					if (config.debugUnhandled)
 					{
 						plugin.getLogger().warning(
 								"Unhandled " + event.getEventName()
 										+ " for type '"
 										+ event.getBucket().name() + "'");
 					}
 					break;
 				}
 			}
 			if (cancel)
 			{
 				// deny
 				if (config.debugEvents)
 				{
 					final Map<String, String> details = new HashMap<String, String>();
 					details.put("Player", player.getName());
 					if (event.getBucket() != null)
 					{
 						details.put("Bucket", event.getBucket().toString());
 					}
 					if (event.getBlockClicked() != null)
 					{
 						details.put("Block", event.getBlockClicked().getType()
 								.name());
 					}
 					details.put("Cancelled", "true");
 					EventLogic.debugEvent(event, details);
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void bucketFill(final PlayerBucketFillEvent event)
 	{
 		if (!event.isCancelled() && event.getPlayer() != null)
 		{
 			final Player player = event.getPlayer();
 			// Switch for type of bucket
 			switch (event.getBlockClicked().getType())
 			{
 				case STATIONARY_LAVA:
 				{
 					// Pay on fill
 					if (config.isEnabled(Field.BUCKET_FILL_LAVA))
 					{
 						if (config.checkWorld(Field.BUCKET_FILL_LAVA, player
 								.getWorld().getName()))
 						{
 							EventLogic.hitPayIncrement(Field.BUCKET_FILL_LAVA,
 									player, config, null, null);
 						}
 					}
 					break;
 				}
 				case STATIONARY_WATER:
 				{
 					// Pay on fill
 					if (config.isEnabled(Field.BUCKET_FILL_WATER))
 					{
 						if (config.checkWorld(Field.BUCKET_FILL_WATER, player
 								.getWorld().getName()))
 						{
 							EventLogic.hitPayIncrement(Field.BUCKET_FILL_WATER,
 									player, config, null, null);
 						}
 					}
 					break;
 				}
 				default:
 				{
 					if (config.debugUnhandled)
 					{
 						plugin.getLogger().warning(
 								"Unhandled " + event.getEventName()
 										+ " for type '"
 										+ event.getBucket().name() + "'");
 					}
 					break;
 				}
 			}
 			if (config.debugEvents)
 			{
 				final Map<String, String> details = new HashMap<String, String>();
 				details.put("Player", player.getName());
 				if (event.getBucket() != null)
 				{
 					details.put("Bucket", event.getBucket().toString());
 				}
 				if (event.getBlockClicked() != null)
 				{
 					details.put("Block", event.getBlockClicked().getType()
 							.name());
 				}
 				EventLogic.debugEvent(event, details);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void worldChange(final PlayerChangedWorldEvent event)
 	{
 		if (config.isEnabled(Field.WORLD_CHANGE) && event.getPlayer() != null)
 		{
 			final Player player = event.getPlayer();
 			// Pay on change
 			if (config.checkWorld(Field.WORLD_CHANGE, player.getWorld()
 					.getName()))
 			{
 				EventLogic.hitPayIncrement(Field.WORLD_CHANGE, player, config,
 						null, null);
 			}
 			if (config.debugEvents)
 			{
 				final Map<String, String> details = new HashMap<String, String>();
 				details.put("Player", player.getName());
 				if (event.getFrom() != null)
 				{
 					details.put("Past World", event.getFrom().toString());
 				}
 				EventLogic.debugEvent(event, details);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void playerDeath(final PlayerDeathEvent event)
 	{
 		if (config.isEnabled(Field.DEATH) && event.getEntity() != null)
 		{
 			final Player player = event.getEntity();
 			// Pay on death
 			if (config.checkWorld(Field.DEATH, player.getWorld().getName()))
 			{
 				EventLogic.hitPayIncrement(Field.DEATH, player, config, null,
 						null);
 			}
 			if (config.debugEvents)
 			{
 				final Map<String, String> details = new HashMap<String, String>();
 				details.put("Player", player.getName());
 				if (event.getDeathMessage() != null)
 				{
 					details.put("Message", event.getDeathMessage());
 
 				}
 				if (player.getLocation() != null)
 				{
 					details.put("Location", player.getLocation().toString());
 				}
 				EventLogic.debugEvent(event, details);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void playerRespawn(final PlayerRespawnEvent event)
 	{
 		if (config.isEnabled(Field.RESPAWN) && event.getPlayer() != null)
 		{
 			final Player player = event.getPlayer();
 			// Pay on respawn
 			if (config.checkWorld(Field.RESPAWN, player.getWorld().getName()))
 			{
 				EventLogic.hitPayIncrement(Field.RESPAWN, player, config, null,
 						null);
 			}
 			if (config.debugEvents)
 			{
 				final Map<String, String> details = new HashMap<String, String>();
 				details.put("Player", player.getName());
 				if (event.getRespawnLocation() != null)
 				{
 					details.put("Location", event.getRespawnLocation()
 							.toString());
 				}
 				EventLogic.debugEvent(event, details);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void itemPickupValid(final PlayerPickupItemEvent event)
 	{
 		if (!event.isCancelled() && config.isEnabled(Field.ITEM_PICKUP))
 		{
 			final Item pickup = new Item(event.getItem().getItemStack());
 			final boolean denyPay = config.getDenyPay(Field.ITEM_PICKUP,
 					pickup, null);
 			final boolean denyLimit = config.getDenyLimit(
 					Field.ITEM_PICKUP, pickup, null);
 			if ((denyPay || denyLimit) && event.getPlayer() != null)
 			{
 				final Player player = event.getPlayer();
 				if (config.checkWorld(Field.ITEM_PICKUP, player.getWorld()
 						.getName()))
 				{
 					if (EventLogic.deny(Field.ITEM_PICKUP, player, config,
 							pickup, null))
 					{
 						// Deny
 						event.setCancelled(true);
 						if (config.debugEvents)
 						{
 							final Map<String, String> details = new HashMap<String, String>();
 							details.put("Player", player.getName());
 							if (event.getItem() != null)
 							{
 								details.put("Item", event.getItem()
 										.getItemStack().toString());
 							}
 							details.put("Cancelled", "true");
 							EventLogic.debugEvent(event, details);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void itemPickup(final PlayerPickupItemEvent event)
 	{
 		if (!event.isCancelled() && config.isEnabled(Field.ITEM_DROP)
 				&& event.getPlayer() != null)
 		{
 			final Player player = event.getPlayer();
 			// Pay on drop
 			if (config.checkWorld(Field.ITEM_PICKUP, player.getWorld()
 					.getName()))
 			{
 				final Item pickup = new Item(event.getItem().getItemStack());
 				EventLogic.hitPayIncrement(Field.ITEM_PICKUP, player, config,
 						pickup, null);
 			}
 			if (config.debugEvents)
 			{
 				final Map<String, String> details = new HashMap<String, String>();
 				details.put("Player", player.getName());
 				if (event.getItem() != null)
 				{
 					details.put("Item", event.getItem().getItemStack()
 							.toString());
 				}
 				EventLogic.debugEvent(event, details);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void itemDropValid(final PlayerDropItemEvent event)
 	{
 		if (!event.isCancelled() && config.isEnabled(Field.ITEM_DROP))
 		{
 			final Item dropped = new Item(event.getItemDrop().getItemStack());
 			final boolean denyPay = config.getDenyPay(Field.ITEM_DROP,
 					dropped, null);
 			final boolean denyLimit = config.getDenyLimit(Field.ITEM_DROP,
 					dropped, null);
 			if ((denyPay || denyLimit) && event.getPlayer() != null)
 			{
 				final Player player = event.getPlayer();
 				if (config.checkWorld(Field.ITEM_DROP, player.getWorld()
 						.getName()))
 				{
 					if (EventLogic.deny(Field.ITEM_DROP, player, config,
 							dropped, null))
 					{
 						// Deny
 						event.setCancelled(true);
 						if (config.debugEvents)
 						{
 							final Map<String, String> details = new HashMap<String, String>();
 							details.put("Player", player.getName());
 							if (event.getItemDrop() != null)
 							{
 								details.put("Item", event.getItemDrop()
 										.getItemStack().toString());
 							}
 							details.put("Cancelled", "true");
 							EventLogic.debugEvent(event, details);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void itemDrop(final PlayerDropItemEvent event)
 	{
 		if (!event.isCancelled() && config.isEnabled(Field.ITEM_DROP)
 				&& event.getPlayer() != null)
 		{
 			final Player player = event.getPlayer();
 			// Pay on drop
 			final Item dropped = new Item(event.getItemDrop().getItemStack());
 			if (config.checkWorld(Field.ITEM_DROP, player.getWorld().getName()))
 			{
 				EventLogic.hitPayIncrement(Field.ITEM_DROP, player, config,
 						dropped, null);
 			}
 			if (config.debugEvents)
 			{
 				final Map<String, String> details = new HashMap<String, String>();
 				details.put("Player", player.getName());
 				if (event.getItemDrop() != null)
 				{
 					details.put("Item", event.getItemDrop().getItemStack()
 							.toString());
 				}
 				EventLogic.debugEvent(event, details);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void eggThrow(final PlayerEggThrowEvent event)
 	{
 		if (config.isEnabled(Field.EGG_THROW) && event.getPlayer() != null)
 		{
 			final Player player = event.getPlayer();
 			// Pay on throw
 			if (config.checkWorld(Field.EGG_THROW, player.getWorld().getName()))
 			{
 				EventLogic.hitPayIncrement(Field.EGG_THROW, player, config,
 						null, null);
 			}
 			if (config.debugEvents)
 			{
 				final Map<String, String> details = new HashMap<String, String>();
 				details.put("Player", player.getName());
 				if (event.getEgg() != null)
 				{
 					// TODO pay for specifics?
 					details.put("Egg", event.getEgg().toString());
 				}
 				EventLogic.debugEvent(event, details);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void gameModeValid(final PlayerGameModeChangeEvent event)
 	{
 		if (!event.isCancelled() && event.getPlayer() != null)
 		{
 			boolean cancel = false;
 			final Player player = event.getPlayer();
 			switch (event.getNewGameMode())
 			{
 				case CREATIVE:
 				{
 					if (config.isEnabled(Field.CREATIVE)
 							&& config.checkWorld(Field.CREATIVE, player
 									.getWorld().getName()))
 					{
 						if (EventLogic.deny(Field.CREATIVE, player, config,
 								null, null))
 						{
 							// Deny
 							cancel = true;
 						}
 					}
 					break;
 
 				}
 				case SURVIVAL:
 				{
 					if (config.isEnabled(Field.SURVIVAL)
 							&& config.checkWorld(Field.SURVIVAL, player
 									.getWorld().getName()))
 					{
 						if (EventLogic.deny(Field.SURVIVAL, player, config,
 								null, null))
 						{
 							// Deny
 							cancel = true;
 						}
 					}
 					break;
 				}
 				default:
 				{
 					if (config.debugUnhandled)
 					{
 						plugin.getLogger().warning(
 								"Unhandled " + event.getEventName()
 										+ " for game mode '"
 										+ event.getNewGameMode() + "'");
 					}
 					break;
 				}
 			}
 			// deny
 			if (cancel)
 			{
 				event.setCancelled(true);
 				if (config.debugEvents)
 				{
 					final Map<String, String> details = new HashMap<String, String>();
 					details.put("Player", player.getName());
 					if (event.getNewGameMode() != null)
 					{
 						details.put("GameMode", event.getNewGameMode()
 								.toString());
 					}
 					details.put("Cancelled", "true");
 					EventLogic.debugEvent(event, details);
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void gameMode(final PlayerGameModeChangeEvent event)
 	{
 		if (!event.isCancelled() && event.getPlayer() != null)
 		{
 			final Player player = event.getPlayer();
 			// Pay on mode
 			switch (event.getNewGameMode())
 			{
 				case CREATIVE:
 				{
 					if (config.isEnabled(Field.CREATIVE)
 							&& config.checkWorld(Field.CREATIVE, player
 									.getWorld().getName()))
 					{
 						EventLogic.hitPayIncrement(Field.CREATIVE, player,
 								config, null, null);
 					}
 					break;
 				}
 				case SURVIVAL:
 				{
 					if (config.isEnabled(Field.SURVIVAL)
 							&& config.checkWorld(Field.SURVIVAL, player
 									.getWorld().getName()))
 					{
 						EventLogic.hitPayIncrement(Field.SURVIVAL, player,
 								config, null, null);
 					}
 					break;
 				}
 				default:
 				{
 					if (config.debugUnhandled)
 					{
 						plugin.getLogger().warning(
 								"Unhandled " + event.getEventName()
 										+ " for game mode '"
 										+ event.getNewGameMode() + "'");
 					}
 					break;
 				}
 			}
 			if (config.debugEvents)
 			{
 				final Map<String, String> details = new HashMap<String, String>();
 				details.put("Player", player.getName());
 				if (event.getNewGameMode() != null)
 				{
 					details.put("GameMode", event.getNewGameMode().toString());
 				}
 				EventLogic.debugEvent(event, details);
 			}
 		}
 	}
 
 	/**
 	 * Event called when player joins the server
 	 * 
 	 * @param event
 	 */
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void join(final PlayerJoinEvent event)
 	{
 		if (event.getPlayer() != null)
 		{
 			final Player player = event.getPlayer();
 			// Add player if they don't exist to database
 			final boolean add = db.addPlayer(player.getName());
 			if (config.debugEvents && add)
 			{
 				plugin.getLogger().info(
 						"Added player '" + player.getName() + "' to database");
 			}
 			// Check their last on date for potential reset
 			if (!db.checkDateReset(player.getName()))
 			{
 				if (config.debugEvents)
 				{
 					plugin.getLogger().info(
 							"Reset values for player '" + player.getName()
 									+ "'");
 				}
 				db.resetAllValues(player.getName());
 			}
 			if (config.isEnabled(Field.JOIN))
 			{
 				// Pay on join
 				EventLogic.hitPayIncrement(Field.JOIN, player, config, null,
 						null);
 				if (config.debugEvents)
 				{
 					final Map<String, String> details = new HashMap<String, String>();
 					details.put("Player", player.getName());
 					if (event.getJoinMessage() != null)
 					{
 						details.put("Message", event.getJoinMessage());
 					}
 					EventLogic.debugEvent(event, details);
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void kick(final PlayerKickEvent event)
 	{
 		if (config.isEnabled(Field.KICK) && event.getPlayer() != null)
 		{
 			final Player player = event.getPlayer();
 			// Pay on kick
 			if (config.checkWorld(Field.KICK, player.getWorld().getName()))
 			{
 				EventLogic.hitPayIncrement(Field.KICK, player, config, null,
 						null);
 			}
 			if (config.debugEvents)
 			{
 				final Map<String, String> details = new HashMap<String, String>();
 				details.put("Player", player.getName());
 				if (event.getReason() != null)
 				{
 					details.put("Bucket", event.getReason());
 				}
 				EventLogic.debugEvent(event, details);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void quit(final PlayerQuitEvent event)
 	{
 		if (event.getPlayer() != null)
 		{
 			// Remove any message from cache
 			final Player player = event.getPlayer();
 			if (Karmiconomy.sentMessages.containsKey(player.getName()))
 			{
 				Karmiconomy.sentMessages.remove(player.getName());
 			}
 			if (config.isEnabled(Field.QUIT))
 			{
 				// Pay on quit
 				if (config.checkWorld(Field.QUIT, player.getWorld().getName()))
 				{
 					EventLogic.hitPayIncrement(Field.QUIT, player, config,
 							null, null);
 				}
 				if (config.debugEvents)
 				{
 					final Map<String, String> details = new HashMap<String, String>();
 					details.put("Player", player.getName());
 					if (event.getQuitMessage() != null)
 					{
 						details.put("Message", event.getQuitMessage());
 					}
 					EventLogic.debugEvent(event, details);
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void sneakValid(final PlayerToggleSneakEvent event)
 	{
 		if (!event.isCancelled() && event.isSneaking()
 				&& config.isEnabled(Field.SNEAK) && event.getPlayer() != null)
 		{
 			final Player player = event.getPlayer();
 			if (config.checkWorld(Field.SNEAK, player.getWorld().getName()))
 			{
 				if (EventLogic.deny(Field.SNEAK, player, config, null, null))
 				{
 					// Deny
 					event.setCancelled(true);
 					if (config.debugEvents)
 					{
 						final Map<String, String> details = new HashMap<String, String>();
 						details.put("Player", player.getName());
 						details.put("Cancelled", "true");
 						EventLogic.debugEvent(event, details);
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void sneak(final PlayerToggleSneakEvent event)
 	{
 		if (!event.isCancelled() && event.isSneaking()
 				&& config.isEnabled(Field.SNEAK) && event.getPlayer() != null)
 		{
 			final Player player = event.getPlayer();
 			// Pay on sneak
 			if (config.checkWorld(Field.SNEAK, player.getWorld().getName()))
 			{
 				EventLogic.hitPayIncrement(Field.SNEAK, player, config, null,
 						null);
 			}
 			if (config.debugEvents)
 			{
 				final Map<String, String> details = new HashMap<String, String>();
 				details.put("Player", player.getName());
 				EventLogic.debugEvent(event, details);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void sprintValid(final PlayerToggleSprintEvent event)
 	{
 		if (!event.isCancelled() && event.isSprinting()
 				&& config.isEnabled(Field.SPRINT) && event.getPlayer() != null)
 		{
 			final Player player = event.getPlayer();
 			if (config.checkWorld(Field.SPRINT, player.getWorld().getName()))
 			{
 				if (EventLogic.deny(Field.SPRINT, player, config, null, null))
 				{
 					// Deny
 					event.setCancelled(true);
 					if (config.debugEvents)
 					{
 						final Map<String, String> details = new HashMap<String, String>();
 						details.put("Player", player.getName());
 						details.put("Cancelled", "true");
 						EventLogic.debugEvent(event, details);
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void sprint(final PlayerToggleSprintEvent event)
 	{
 		if (!event.isCancelled() && event.isSprinting()
 				&& config.isEnabled(Field.SPRINT) && event.getPlayer() != null)
 		{
 			final Player player = event.getPlayer();
 			// Pay on sprint
 			if (config.checkWorld(Field.SPRINT, player.getWorld().getName()))
 			{
 				EventLogic.hitPayIncrement(Field.SPRINT, player, config, null,
 						null);
 			}
 			if (config.debugEvents)
 			{
 				final Map<String, String> details = new HashMap<String, String>();
 				details.put("Player", player.getName());
 				EventLogic.debugEvent(event, details);
 			}
 		}
 	}
 
 	//TODO implement
 	/*@EventHandler(priority = EventPriority.LOWEST)
 	public void vehicleEnterValid(final VehicleEnterEvent event)
 	{
 		if (!event.isCancelled() && config.vehicleEnter
 				&& event.getEntered() != null)
 		{
 			if (event.getEntered() instanceof Player)
 			{
 				final Player player = (Player) event.getEntered();
 				// TODO deny
 				if (config.debugEvents && event.isCancelled())
 				{
 					final Map<String, String> details = new HashMap<String, String>();
 					details.put("Player", player.getName());
 					if (event.getVehicle() != null)
 					{
 						details.put("Vehicle", event.getVehicle().toString());
 					}
 					details.put("Cancelled", "true");
 					EventLogic.debugEvent(event, details);
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void vehicleEnter(final VehicleEnterEvent event)
 	{
 		if (!event.isCancelled() && config.vehicleEnter
 				&& event.getEntered() != null)
 		{
 			if (event.getEntered() instanceof Player)
 			{
 				final Player player = (Player) event.getEntered();
 				// TODO pay on enter
 				if (config.debugEvents)
 				{
 					final Map<String, String> details = new HashMap<String, String>();
 					details.put("Player", player.getName());
 					if (event.getVehicle() != null)
 					{
 						details.put("Vehicle", event.getVehicle().toString());
 					}
 					EventLogic.debugEvent(event, details);
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void vehicleExitValid(final VehicleExitEvent event)
 	{
 		if (!event.isCancelled() && config.vehicleExit
 				&& event.getExited() != null)
 		{
 			if (event.getExited() instanceof Player)
 			{
 				final Player player = (Player) event.getExited();
 				// TODO deny
 				if (config.debugEvents && event.isCancelled())
 				{
 					final Map<String, String> details = new HashMap<String, String>();
 					details.put("Player", player.getName());
 					if (event.getVehicle() != null)
 					{
 						details.put("Vehicle", event.getVehicle().toString());
 					}
 					details.put("Cancelled", "true");
 					EventLogic.debugEvent(event, details);
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void vehicleExit(final VehicleExitEvent event)
 	{
 		if (!event.isCancelled() && config.vehicleExit
 				&& event.getExited() != null)
 		{
 			if (event.getExited() instanceof Player)
 			{
 				final Player player = (Player) event.getExited();
 				// TODO pay on enter
 				if (config.debugEvents)
 				{
 					final Map<String, String> details = new HashMap<String, String>();
 					details.put("Player", player.getName());
 					if (event.getVehicle() != null)
 					{
 						details.put("Vehicle", event.getVehicle().toString());
 					}
 					EventLogic.debugEvent(event, details);
 				}
 			}
 		}
 	}*/
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void inventoryClick(final InventoryClickEvent event)
 	{
 		if (config.debugEvents)
 		{
 			final Map<String, String> details = new HashMap<String, String>();
 			details.put("Player", event.getWhoClicked().getName());
 			EventLogic.debugEvent(event, details);
 		}
 	}
 
 	// TODO use all events, and perhaps make all players get affected due to
 	// event?
 
 	/**
 	 * Missing events: ExpChange Fish LevelChange Move Pickup Shear SignChange
 	 */
 }
