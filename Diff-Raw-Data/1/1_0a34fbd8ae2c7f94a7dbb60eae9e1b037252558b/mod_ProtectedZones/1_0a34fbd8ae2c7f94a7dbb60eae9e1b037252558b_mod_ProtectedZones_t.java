 package btwmod.protectedzones;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.Block;
 import net.minecraft.src.BlockButton;
 import net.minecraft.src.BlockContainer;
 import net.minecraft.src.BlockRail;
 import net.minecraft.src.Entity;
 import net.minecraft.src.EntityHanging;
 import net.minecraft.src.EntityLiving;
 import net.minecraft.src.EntityMooshroom;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.EntityVillager;
 import net.minecraft.src.FCBlockBloodMoss;
 import net.minecraft.src.FCEntityCanvas;
 import net.minecraft.src.Facing;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.MathHelper;
 import net.minecraft.src.ServerCommandManager;
 import net.minecraft.src.TileEntity;
 import net.minecraft.src.TileEntitySkull;
 import net.minecraft.src.World;
 import net.minecraft.src.mod_FCBetterThanWolves;
 import btwmods.CommandsAPI;
 import btwmods.EntityAPI;
 import btwmods.IMod;
 import btwmods.ModLoader;
 import btwmods.PlayerAPI;
 import btwmods.Util;
 import btwmods.WorldAPI;
 import btwmods.entity.EntityEvent;
 import btwmods.entity.IEntityListener;
 import btwmods.events.APIEvent;
 import btwmods.io.Settings;
 import btwmods.player.IPlayerActionListener;
 import btwmods.player.PlayerActionEvent;
 import btwmods.player.PlayerBlockEvent;
 import btwmods.player.IPlayerBlockListener;
 import btwmods.util.Area;
 import btwmods.world.BlockEvent;
 import btwmods.world.BlockEventBase;
 import btwmods.world.IBlockListener;
 
 public class mod_ProtectedZones implements IMod, IPlayerBlockListener, IBlockListener, IPlayerActionListener, IEntityListener {
 	
 	public enum ACTION {
 		DIG, ACTIVATE, EXPLODE, ATTACK_ENTITY, USE_ENTITY,
 		ITEM_USE_CHECK_EDIT, IS_ENTITY_INVULNERABLE, BURN, IS_FLAMMABLE, FIRE_SPREAD_ATTEMPT, CAN_PUSH, TRAMPLE_FARMLAND
 	};
 
 	private Settings data;
 	private ProtectedZones[] zonesByDimension;
 	private CommandZone commandZone;
 	
 	private MinecraftServer server;
 	private ServerCommandManager commandManager;
 	private Set ops;
 	
 	private boolean alwaysAllowOps = true;
 
 	@Override
 	public String getName() {
 		return "Protected Zones";
 	}
 
 	@Override
 	public void init(Settings settings, Settings data) throws Exception {
 		server = MinecraftServer.getServer();
 		commandManager = (ServerCommandManager)server.getCommandManager();
 		
 		PlayerAPI.addListener(this);
 		WorldAPI.addListener(this);
 		EntityAPI.addListener(this);
 		CommandsAPI.registerCommand(commandZone = new CommandZone(this), this);
 		
 		alwaysAllowOps = settings.getBoolean("alwaysAllowOps", alwaysAllowOps);
 		
 		ops = server.getConfigurationManager().getOps();
 		
 		this.data = data;
 		
 		zonesByDimension = new ProtectedZones[server.worldServers.length];
 		for (int i = 0; i < server.worldServers.length; i++) {
 			zonesByDimension[i] = new ProtectedZones();
 		}
 		
 		int zoneCount = data.getInt("count", 0);
 		for (int i = 1; i <= zoneCount; i++) {
 			if (data.hasSection("zone" + i) && !add(new Zone(data, "zone" + i))) {
 				ModLoader.outputError(getName() + " failed to load zone " + i + " as it has a duplicate name or has invalid dimensions.");
 			}
 		}
 	}
 
 	@Override
 	public void unload() throws Exception {
 		PlayerAPI.removeListener(this);
 		WorldAPI.removeListener(this);
 		EntityAPI.removeListener(this);
 		CommandsAPI.unregisterCommand(commandZone);
 	}
 
 	@Override
 	public IMod getMod() {
 		return this;
 	}
 	
 	public boolean add(Zone zone) {
 		return zone != null && zonesByDimension[Util.getWorldIndexFromDimension(zone.dimension)].add(zone);
 	}
 	
 	public boolean remove(int dimension, String name) {
 		return name != null && zonesByDimension[Util.getWorldIndexFromDimension(dimension)].removeZone(name);
 	}
 	
 	public boolean remove(Zone zone) {
 		return zone != null && remove(zone.dimension, zone.name);
 	}
 	
 	public Zone get(int dimension, String name) {
 		return zonesByDimension[Util.getWorldIndexFromDimension(dimension)].getZone(name);
 	}
 	
 	public List<String> getZoneNames() {
 		ArrayList names = new ArrayList();
 		for (ProtectedZones zones : zonesByDimension) {
 			names.addAll(zones.getZoneNames());
 		}
 		return names;
 	}
 	
 	public List<String> getZoneNames(int dimension) {
 		return zonesByDimension[Util.getWorldIndexFromDimension(dimension)].getZoneNames();
 	}
 	
 	public static boolean isProtectedBlockType(ACTION action, Block block) {
 		if (action == ACTION.ACTIVATE) {
 			if (block instanceof BlockRail)
 				return false;
 			
 			if (block == Block.workbench)
 				return false;
 			
 			if (block == mod_FCBetterThanWolves.fcAnvil)
 				return false;
 			
 			if (block == Block.lever)
 				return false;
 			
 			if (block instanceof BlockButton)
 				return false;
 			
 			if (block == Block.enderChest)
 				return false;
 			
 			if (block == Block.enchantmentTable)
 				return false;
 			
 			if (block == Block.bed)
 				return false;
 			
 			if (block == mod_FCBetterThanWolves.fcInfernalEnchanter)
 				return false;
 		}
 		
 		return true;
 	}
 	
 	public static boolean isProtectedEntityType(ACTION action, Entity entity) {
 		if (entity instanceof EntityLiving) {
 			if (entity instanceof EntityVillager && action != ACTION.USE_ENTITY)
 				return true;
 			
 			if (entity instanceof EntityMooshroom)
 				return true;
 		}
 		else if (entity instanceof EntityHanging) {
 			return true;
 		}
 		else if (entity instanceof FCEntityCanvas) {
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public boolean isOp(String username) {
 		return ops.contains(username.trim().toLowerCase());
 	}
 	
 	public boolean isPlayerGloballyAllowed(String username) {
 		return alwaysAllowOps && isOp(username);
 	}
 	
 	protected boolean isProtectedEntity(ACTION action, EntityPlayer player, Entity entity, int x, int y, int z) {
 		if (!isProtectedEntityType(action, entity))
 			return false;
 		
 		if (player != null && isPlayerGloballyAllowed(player.username))
 			return false;
 		
 		List<Area<Zone>> areas = zonesByDimension[Util.getWorldIndexFromDimension(entity.worldObj.provider.dimensionId)].get(x, y, z);
 		
 		for (Area<Zone> area : areas) {
 			Zone zone = area.data;
 			if (zone != null && zone.permissions.protectEntities != Permission.OFF) {
 				
 				boolean isProtected = true;
 				
 				if (entity instanceof EntityMooshroom) {
 					if (zone.permissions.allowMooshroom)
 						isProtected = false;
 					
 					else if (player != null && action == ACTION.USE_ENTITY) {
 						ItemStack heldItem = player.getHeldItem();
 						if (heldItem != null && heldItem.getItem() == Item.bowlEmpty) {
 							isProtected = false;
 						}
 					}
 				}
 				else if (entity instanceof EntityVillager && zone.permissions.allowVillagers) {
 					isProtected = false;
 				}
 				
 				if (isProtected && player != null && zone.permissions.protectEntities == Permission.WHITELIST && zone.whitelist.contains(player.username))
 					isProtected = false;
 				
 				if (isProtected) {
 					if (zone.permissions.sendDebugMessages)
 						commandManager.notifyAdmins(server, 0, "Protect " + entity.getEntityName() + " " + action + " " + x + "," + y + "," + z + (player == null ? "" : " from " + player.username + " by " + area.data.name + "#" + area.data.getAreaIndex(area)), new Object[0]);
 					
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	protected boolean isProtectedBlock(APIEvent event, ACTION action, EntityPlayer player, Block block, World world, int x, int y, int z) {
 		if (!isProtectedBlockType(action, block))
 			return false;
 		
 		if (player != null && isPlayerGloballyAllowed(player.username))
 			return true;
 		
 		List<Area<Zone>> areas = zonesByDimension[Util.getWorldIndexFromDimension(world.provider.dimensionId)].get(x, y, z);
 		
 		for (Area<Zone> area : areas) {
 			Zone zone = area.data;
 			ItemStack itemStack = null;
 			
 			if (zone != null) {
 				
 				boolean isProtected = false;
 				
 				switch (action) {
 					case EXPLODE:
 						if (zone.permissions.protectExplosions)
 							isProtected = true;
 						break;
 						
 					case IS_FLAMMABLE:
 					case BURN:
 					case FIRE_SPREAD_ATTEMPT:
 						if (zone.permissions.protectBurning)
 							isProtected = true;
 						break;
 						
 					case CAN_PUSH:
 						// Protect against pistons from outside the area.
 						if (event instanceof BlockEvent && zone.permissions.protectEdits != Permission.OFF) {
 							BlockEvent blockEvent = (BlockEvent)event;
 							isProtected = true;
 							
 							// Check all areas for the ZoneSettings.
 							for (Area zoneArea : area.data.areas) {
 								if (zoneArea.isWithin(blockEvent.getPistonX(), blockEvent.getPistonY(), blockEvent.getPistonZ())) {
 									isProtected = false;
 									break;
 								}
 							}
 						}
 						break;
 						
 					default:
 						if (zone.permissions.protectEdits != Permission.OFF) {
 							isProtected = true;
 							
 							// Allow immature bloodmoss to be destroyed.
 							if ((action == ACTION.DIG) && block instanceof FCBlockBloodMoss && event instanceof BlockEventBase && (((BlockEventBase)event).getMetadata() & 7) < 7) {
 								isProtected = false;
 							}
 							
 							if (isProtected && player != null) {
 								if (isProtected && action == ACTION.ACTIVATE) {
 									if ((block == Block.doorWood || block == Block.trapdoor || block == Block.fenceGate)
 											&& zone.isPlayerAllowed(player.username, zone.permissions.allowDoors))
 										isProtected = false;
 									
 									else if (block instanceof BlockContainer && zone.isPlayerAllowed(player.username, zone.permissions.allowContainers))
 										isProtected = false;
 								}
 								
 								if (isProtected && zone.permissions.allowOps && isOp(player.username))
 									isProtected = false;
 							
 								if (isProtected && action == ACTION.DIG && block == Block.skull && zone.isPlayerAllowed(player.username, zone.permissions.allowHeads)) {
 									TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
 									if (tileEntity instanceof TileEntitySkull) {
 										TileEntitySkull tileEntitySkull = (TileEntitySkull)tileEntity;
 										if (tileEntitySkull.getSkullType() == 3 && player.username.equalsIgnoreCase(tileEntitySkull.getExtraType())) {
 											isProtected = false;
 										}
 									}
 								}
 								
 								if (isProtected && action == ACTION.ITEM_USE_CHECK_EDIT) {
 									PlayerBlockEvent playerBlockEvent = (PlayerBlockEvent)event;
 									if ((itemStack = playerBlockEvent.getItemStack()) != null
 											&& playerBlockEvent.getDirection() == 1
 											&& itemStack.getItem() == Item.skull
 											&& itemStack.getItemDamage() == 3
											&& zone.isPlayerAllowed(player.username, zone.permissions.allowHeads)
 											&& itemStack.hasTagCompound()
 											&& itemStack.getTagCompound().hasKey("SkullOwner")
 											&& player.username.equalsIgnoreCase(itemStack.getTagCompound().getString("SkullOwner"))
 											&& playerBlockEvent.getBlockId() == 0
 											&& playerBlockEvent.getY() > 1
 											&& playerBlockEvent.getWorld().getBlockId(playerBlockEvent.getX(), playerBlockEvent.getY() - 1, playerBlockEvent.getZ()) != 0) {
 										
 										isProtected = false;
 									}
 								}
 							
 								if (isProtected && zone.permissions.protectEdits == Permission.WHITELIST && zone.whitelist.contains(player.username))
 									isProtected = false;
 							}
 						}
 						break;
 					
 				}
 				
 				if (isProtected) {
 					if (zone.permissions.sendDebugMessages) {
 						if (itemStack == null && event instanceof PlayerBlockEvent) {
 							itemStack = ((PlayerBlockEvent)event).getItemStack();
 						}
 						
 						String message = "Protect" 
 								+ " " + action
 								+ (block == null ? "" : " " + block.getBlockName())
 								+ (itemStack == null ? "" : " " + itemStack.getItemName())
 								+ " " + x + "," + y + "," + z + " by " + area.data.name + "#" + area.data.getAreaIndex(area);
 						
 						if (player == null)
 							commandManager.notifyAdmins(server, 0, message + (player == null ? "" : " from " + player.username), new Object[0]);
 						else
 							player.sendChatToPlayer(message);
 					}
 					
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 
 	@Override
 	public void onPlayerBlockAction(PlayerBlockEvent event) {
 		ACTION action = null;
 		
 		switch (event.getType()) {
 			case ACTIVATION_ATTEMPT:
 				action = ACTION.ACTIVATE;
 				break;
 			case ACTIVATED:
 				break;
 			case REMOVE_ATTEMPT:
 				action = ACTION.DIG;
 				break;
 			case REMOVED:
 				break;
 			case ITEM_USE_ATTEMPT:
 				break;
 			case ITEM_USE_CHECK_EDIT:
 				action = ACTION.ITEM_USE_CHECK_EDIT;
 				break;
 			case ITEM_USED:
 				break;
 			case GET_ENDERCHEST_INVENTORY:
 				break;
 		}
 		
 		if (action != null && isProtectedBlock(event, action, event.getPlayer(), event.getBlock(), event.getWorld(), event.getX(), event.getY(), event.getZ())) {
 			if (event.getType() == PlayerBlockEvent.TYPE.ACTIVATION_ATTEMPT)
 				event.markHandled();
 			else
 				event.markNotAllowed();
 		}
 	}
 
 	@Override
 	public void onBlockAction(BlockEvent event) {
 		if (event.getType() == BlockEvent.TYPE.EXPLODE_ATTEMPT) {
 			if (isProtectedBlock(event, ACTION.EXPLODE, null, event.getBlock(), event.getWorld(), event.getX(), event.getY(), event.getZ())) {
 				event.markNotAllowed();
 			}
 		}
 		else if (event.getType() == BlockEvent.TYPE.BURN_ATTEMPT) {
 			if (isProtectedBlock(event, ACTION.BURN, null, event.getBlock(), event.getWorld(), event.getX(), event.getY(), event.getZ())) {
 				event.markNotAllowed();
 			}
 		}
 		else if (event.getType() == BlockEvent.TYPE.IS_FLAMMABLE_BLOCK) {
 			if (isProtectedBlock(event, ACTION.IS_FLAMMABLE, null, event.getBlock(), event.getWorld(), event.getX(), event.getY(), event.getZ())) {
 				event.markNotFlammable();
 			}
 		}
 		else if (event.getType() == BlockEvent.TYPE.FIRE_SPREAD_ATTEMPT) {
 			if (isProtectedBlock(event, ACTION.FIRE_SPREAD_ATTEMPT, null, event.getBlock(), event.getWorld(), event.getX(), event.getY(), event.getZ())) {
 				event.markNotAllowed();
 			}
 		}
 		else if (event.getType() == BlockEvent.TYPE.CAN_PUSH_BLOCK) {
 			if (isProtectedBlock(event, ACTION.CAN_PUSH, null, event.getBlock(), event.getWorld(), event.getX(), event.getY(), event.getZ())) {
 				event.markNotAllowed();
 			}
 			else {
 				int nextX = event.getX() + Facing.offsetsXForSide[event.getPistonOrientation()];
 				int nextY = event.getY() + Facing.offsetsYForSide[event.getPistonOrientation()];
 				int nextZ = event.getZ() + Facing.offsetsZForSide[event.getPistonOrientation()];
 				int nextBlockId = event.getWorld().getBlockId(nextX, nextY, nextZ);
 				
 				if (isProtectedBlock(event, ACTION.CAN_PUSH, null, nextBlockId > 0 ? Block.blocksList[nextBlockId] : null, event.getWorld(), nextX, nextY, nextZ)) {
 					event.markNotAllowed();
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onPlayerAction(PlayerActionEvent event) {
 		if (event.getType() == PlayerActionEvent.TYPE.PLAYER_USE_ENTITY_ATTEMPT) {
 			if (isProtectedEntity(event.isLeftClick() ? ACTION.ATTACK_ENTITY : ACTION.USE_ENTITY, event.getPlayer(), event.getEntity(), MathHelper.floor_double(event.getEntity().posX), MathHelper.floor_double(event.getEntity().posY), MathHelper.floor_double(event.getEntity().posZ))) {
 				event.markNotAllowed();
 			}
 		}
 	}
 
 	@Override
 	public void onEntityAction(EntityEvent event) {
 		if (event.getType() == EntityEvent.TYPE.IS_ENTITY_INVULNERABLE) {
 			if (isProtectedEntity(ACTION.IS_ENTITY_INVULNERABLE, null, event.getEntity(), event.getX(), event.getY(), event.getZ())) {
 				event.markIsInvulnerable();
 			}
 		}
 		else if (event.getType() == EntityEvent.TYPE.TRAMPLE_FARMLAND_ATTEMPT) {
 			if (isProtectedBlock(event, ACTION.TRAMPLE_FARMLAND, null, Block.tilledField, event.getWorld(), event.getBlockX(), event.getBlockY(), event.getBlockZ())) {
 				event.markNotAllowed();
 			}
 		}
 		/*else if (event.getType() == EntityEvent.TYPE.EXPLODE_ATTEMPT) {
 			if (isProtectedEntity(ACTION.EXPLODE, null, event.getEntity(), MathHelper.floor_double(event.getEntity().posX), MathHelper.floor_double(event.getEntity().posY), MathHelper.floor_double(event.getEntity().posZ))) {
 				event.markNotAllowed();
 			}
 		}*/
 	}
 	
 	public void saveAreas() {
 		data.clear();
 		
 		int count = 1;
 		for (ProtectedZones zones : zonesByDimension) {
 			for (Zone zone : zones.getZones()) {
 				zone.saveToSettings(data, "zone" + count);
 				count++;
 			}
 		}
 		data.setInt("count", count);
 		
 		data.saveSettings(this);
 	}
 }
