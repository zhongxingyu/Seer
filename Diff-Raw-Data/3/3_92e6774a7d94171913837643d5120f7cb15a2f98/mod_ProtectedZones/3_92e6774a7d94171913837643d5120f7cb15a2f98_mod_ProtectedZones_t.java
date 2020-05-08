 package btwmod.protectedzones;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.Block;
 import net.minecraft.src.BlockBed;
 import net.minecraft.src.BlockButton;
 import net.minecraft.src.BlockContainer;
 import net.minecraft.src.BlockEnchantmentTable;
 import net.minecraft.src.BlockEnderChest;
 import net.minecraft.src.BlockLever;
 import net.minecraft.src.BlockRail;
 import net.minecraft.src.BlockWorkbench;
 import net.minecraft.src.Entity;
 import net.minecraft.src.EntityHanging;
 import net.minecraft.src.EntityLiving;
 import net.minecraft.src.EntityMooshroom;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.EntityVillager;
 import net.minecraft.src.FCBlockAnvil;
 import net.minecraft.src.FCBlockBloodMoss;
 import net.minecraft.src.FCBlockInfernalEnchanter;
 import net.minecraft.src.FCEntityCanvas;
 import net.minecraft.src.Facing;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemMinecart;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.MathHelper;
 import net.minecraft.src.ServerCommandManager;
 import net.minecraft.src.World;
 import btwmods.CommandsAPI;
 import btwmods.IMod;
 import btwmods.ModLoader;
 import btwmods.PlayerAPI;
 import btwmods.Util;
 import btwmods.WorldAPI;
 import btwmods.events.APIEvent;
 import btwmods.io.Settings;
 import btwmods.player.IPlayerActionListener;
 import btwmods.player.PlayerActionEvent;
 import btwmods.player.PlayerBlockEvent;
 import btwmods.player.IPlayerBlockListener;
 import btwmods.util.Area;
 import btwmods.world.BlockEvent;
 import btwmods.world.BlockEventBase;
 import btwmods.world.EntityEvent;
 import btwmods.world.IBlockListener;
 import btwmods.world.IEntityListener;
 
 public class mod_ProtectedZones implements IMod, IPlayerBlockListener, IBlockListener, IPlayerActionListener, IEntityListener {
 	
 	public enum ACTION {
 		PLACE, DIG, ACTIVATE, EXPLODE, ATTACK_ENTITY, USE_ENTITY,
 		CHECK_PLAYER_EDIT, IS_ENTITY_INVULNERABLE, BURN, IS_FLAMMABLE, FIRE_SPREAD_ATTEMPT, CAN_PUSH, TRAMPLE_FARMLAND
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
 			if (data.hasSection("zone" + i) && !add(new ZoneSettings(data.getSectionAsSettings("zone" + i)))) {
 				ModLoader.outputError(getName() + " failed to load zone " + i + " as it has a duplicate name or has invalid dimensions.");
 			}
 		}
 	}
 
 	@Override
 	public void unload() throws Exception {
 		PlayerAPI.removeListener(this);
 		WorldAPI.removeListener(this);
 		CommandsAPI.unregisterCommand(commandZone);
 	}
 
 	@Override
 	public IMod getMod() {
 		return this;
 	}
 	
 	public boolean add(ZoneSettings zoneSettings) {
 		return zoneSettings != null && zonesByDimension[Util.getWorldIndexFromDimension(zoneSettings.dimension)].add(zoneSettings);
 	}
 	
 	public boolean remove(int dimension, String name) {
 		return name != null && zonesByDimension[Util.getWorldIndexFromDimension(dimension)].removeZone(name);
 	}
 	
 	public boolean remove(ZoneSettings zoneSettings) {
 		return zoneSettings != null && remove(zoneSettings.dimension, zoneSettings.name);
 	}
 	
 	public ZoneSettings get(int dimension, String name) {
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
 			
 			if (block instanceof BlockWorkbench)
 				return false;
 			
 			if (block instanceof FCBlockAnvil)
 				return false;
 			
 			if (block instanceof BlockLever)
 				return false;
 			
 			if (block instanceof BlockButton)
 				return false;
 			
 			if (block instanceof BlockEnderChest)
 				return false;
 			
 			if (block instanceof BlockEnchantmentTable)
 				return false;
 			
 			if (block instanceof BlockBed)
 				return false;
 			
 			if (block instanceof FCBlockInfernalEnchanter)
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
 		
 		List<Area<ZoneSettings>> areas = zonesByDimension[Util.getWorldIndexFromDimension(entity.worldObj.provider.dimensionId)].get(x, y, z);
 		
 		for (Area<ZoneSettings> area : areas) {
 			ZoneSettings settings = area.data;
 			if (settings != null && settings.protectEntities != ZoneSettings.PERMISSION.OFF) {
 				
 				boolean isProtected = true;
 				
 				if (entity instanceof EntityMooshroom) {
 					if (settings.allowMooshroom)
 						isProtected = false;
 					
 					else if (player != null && action == ACTION.USE_ENTITY) {
 						ItemStack heldItem = player.getHeldItem();
 						if (heldItem != null && heldItem.getItem() == Item.bowlEmpty) {
 							isProtected = false;
 						}
 					}
 				}
 				else if (entity instanceof EntityVillager && settings.allowVillagers) {
 					isProtected = false;
 				}
 				
 				if (isProtected && player != null && settings.protectEntities == ZoneSettings.PERMISSION.WHITELIST && settings.isPlayerWhitelisted(player.username))
 					isProtected = false;
 				
 				if (isProtected) {
 					if (settings.sendDebugMessages)
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
 		
 		List<Area<ZoneSettings>> areas = zonesByDimension[Util.getWorldIndexFromDimension(world.provider.dimensionId)].get(x, y, z);
 		
 		for (Area<ZoneSettings> area : areas) {
 			ZoneSettings settings = area.data;
 			ItemStack itemStack = null;
 			
 			if (settings != null) {
 				
 				boolean isProtected = false;
 				
 				switch (action) {
 					case EXPLODE:
 						if (settings.protectExplosions)
 							isProtected = true;
 						break;
 						
 					case IS_FLAMMABLE:
 					case BURN:
 					case FIRE_SPREAD_ATTEMPT:
 						if (settings.protectBurning)
 							isProtected = true;
 						break;
 						
 					case CAN_PUSH:
 						// Protect against pistons from outside the area.
 						if (event instanceof BlockEvent) {
 							BlockEvent blockEvent = (BlockEvent)event;
 							
 							// Check all areas for the ZoneSettings.
 							for (Area zoneArea : area.data.areas) {
 								if (!zoneArea.isWithin(blockEvent.getPistonX(), blockEvent.getPistonY(), blockEvent.getPistonZ())) {
 									isProtected = true;
 									break;
 								}
 							}
 						}
 						break;
 						
 					default:
 						if (settings.protectEdits != ZoneSettings.PERMISSION.OFF) {
 							isProtected = true;
 							
 							// Allow immature bloodmoss to be destroyed.
 							if ((action == ACTION.DIG || action == ACTION.CHECK_PLAYER_EDIT) && block instanceof FCBlockBloodMoss && event instanceof BlockEventBase && (((BlockEventBase)event).getMetadata() & 7) < 7) {
 								isProtected = false;
 							}
 							
 							if (isProtected && player != null) {
 								// Allow minecarts to be placed on rails.
 								if (isProtected && action == ACTION.PLACE && block instanceof BlockRail && event instanceof PlayerBlockEvent && (itemStack = ((PlayerBlockEvent)event).getItemStack()) != null && itemStack.getItem() instanceof ItemMinecart)
 									isProtected = false;
 								
 								if (isProtected && action == ACTION.ACTIVATE) {
 									if ((block == Block.doorWood || block == Block.trapdoor || block == Block.fenceGate)
 											&& settings.isPlayerAllowed(player.username, settings.allowDoors))
 										isProtected = false;
 									
 									else if (block instanceof BlockContainer && settings.isPlayerAllowed(player.username, settings.allowContainers))
 										isProtected = false;
 								}
								
								if (isProtected && settings.allowOps && isOp(player.username))
									isProtected = false;
 							
 								if (isProtected && settings.protectEdits == ZoneSettings.PERMISSION.WHITELIST && settings.isPlayerWhitelisted(player.username))
 									isProtected = false;
 							}
 						}
 						break;
 					
 				}
 				
 				if (isProtected) {
 					if (settings.sendDebugMessages) {
 						if (itemStack != null && event instanceof PlayerBlockEvent) {
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
 	
 	protected boolean isProtectedBlock(APIEvent event, ACTION action, EntityPlayer player, Block block, World world, int x, int y, int z, int direction) {
 		
 		switch (direction) {
 			case 0:
 				y--;
 				break;
 			case 1:
 				y++;
 				break;
 			case 2:
 				z--;
 				break;
 			case 3:
 				z++;
 				break;
 			case 4:
 				x--;
 			case 5:
 				x++;
 				break;
 		}
 		
 		return isProtectedBlock(event, action, player, block, world, x, y, z);
 	}
 
 	@Override
 	public void onPlayerBlockAction(PlayerBlockEvent event) {
 		ACTION action = null;
 		boolean checkDirectionAdjusted = false;
 		
 		switch (event.getType()) {
 			case ACTIVATED:
 				break;
 			case ACTIVATION_ATTEMPT:
 				action = ACTION.ACTIVATE;
 				break;
 			case REMOVE_ATTEMPT:
 				action = ACTION.DIG;
 				break;
 			case PLACE_ATTEMPT:
 				action = ACTION.PLACE;
 				checkDirectionAdjusted = true;
 				break;
 			case CHECK_PLAYEREDIT:
 				action = ACTION.CHECK_PLAYER_EDIT;
 				break;
 			case GET_ENDERCHEST_INVENTORY:
 				break;
 		}
 		
 		if (action != null && (
 				isProtectedBlock(event, action, event.getPlayer(), event.getBlock(), event.getWorld(), event.getX(), event.getY(), event.getZ())
 				|| (checkDirectionAdjusted && isProtectedBlock(event, action, event.getPlayer(), event.getBlock(), event.getWorld(), event.getX(), event.getY(), event.getZ(), event.getDirection()))
 			)) {
 			
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
 			for (ZoneSettings zoneSettings : zones.getZones()) {
 				zoneSettings.saveToSettings(data, "zone" + count);
 				count++;
 			}
 		}
 		data.setInt("count", count);
 		
 		data.saveSettings(this);
 	}
 }
