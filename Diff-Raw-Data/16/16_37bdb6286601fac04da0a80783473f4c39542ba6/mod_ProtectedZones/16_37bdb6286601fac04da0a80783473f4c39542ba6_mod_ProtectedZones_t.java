 package btwmod.protectedzones;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.Block;
 import net.minecraft.src.Entity;
 import net.minecraft.src.EntityLiving;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.EntityVillager;
 import net.minecraft.src.ICommand;
 import net.minecraft.src.MathHelper;
 import net.minecraft.src.World;
 import btwmods.CommandsAPI;
 import btwmods.IMod;
 import btwmods.ModLoader;
 import btwmods.PlayerAPI;
 import btwmods.Util;
 import btwmods.WorldAPI;
 import btwmods.io.Settings;
 import btwmods.player.IPlayerActionListener;
 import btwmods.player.PlayerActionEvent;
 import btwmods.player.PlayerBlockEvent;
 import btwmods.player.IPlayerBlockListener;
 import btwmods.util.Area;
 import btwmods.world.BlockEvent;
 import btwmods.world.EntityEvent;
 import btwmods.world.IBlockListener;
 import btwmods.world.IEntityListener;
 
 public class mod_ProtectedZones implements IMod, IPlayerBlockListener, IBlockListener, IPlayerActionListener, IEntityListener {
 	
 	public enum ACTION { PLACE, DIG, BROKEN, ACTIVATE, EXPLODE, ATTACK_ENTITY, USE_ENTITY };
 	private Map<String, Area<ZoneSettings>> areasByName = new TreeMap<String, Area<ZoneSettings>>();
 	private ProtectedZones[] zones;
 	
 	private Set<ICommand> commands = new LinkedHashSet<ICommand>();
 	
 	private Settings data;
 	
 	private Set ops;
 	
 	private boolean alwaysAllowOps = true;
 
 	@Override
 	public String getName() {
 		return "Protected Zones";
 	}
 
 	@Override
 	public void init(Settings settings, Settings data) throws Exception {
 		MinecraftServer server = MinecraftServer.getServer();
 		
 		PlayerAPI.addListener(this);
 		WorldAPI.addListener(this);
 		registerCommand(new CommandZoneAdd(this));
 		registerCommand(new CommandZoneDelete(this));
 		registerCommand(new CommandZoneList(this));
 		registerCommand(new CommandZoneSet(this));
 		registerCommand(new CommandZonePlayer(this));
 		registerCommand(new CommandZoneInfo(this));
 		
 		alwaysAllowOps = settings.getBoolean("alwaysAllowOps", alwaysAllowOps);
 		
 		ops = server.getConfigurationManager().getOps();
 		
 		this.data = data;
 		
 		zones = new ProtectedZones[server.worldServers.length];
 		for (int i = 0; i < server.worldServers.length; i++) {
 			zones[i] = new ProtectedZones();
 		}
 		
 		int zoneCount = data.getInt("count", 0);
 		for (int i = 1; i <= zoneCount; i++) {
 			if (data.hasSection("zone" + i) && !add(new ZoneSettings(data.getSectionAsSettings("zone" + i)), false)) {
 				ModLoader.outputError(getName() + " failed to load zone " + i + " as it has a duplicate name or has invalid dimensions.");
 			}
 		}
 	}
 
 	@Override
 	public void unload() throws Exception {
 		PlayerAPI.removeListener(this);
 		WorldAPI.removeListener(this);
 		unregisterCommands();
 	}
 
 	@Override
 	public IMod getMod() {
 		return this;
 	}
 	
 	private void registerCommand(ICommand command) {
 		commands.add(command);
 		CommandsAPI.registerCommand(command, this);
 	}
 	
 	private void unregisterCommands() {
 		for (ICommand command : commands) {
 			CommandsAPI.unregisterCommand(command);
 		}
 	}
 	
 	public boolean add(ZoneSettings zoneSettings) {
 		return add(zoneSettings, true);
 	}
 	
 	private boolean add(ZoneSettings zoneSettings, boolean doSave) {
 		if (zoneSettings != null && zoneSettings.isValid() && !areasByName.containsKey(zoneSettings.name.toLowerCase())) {
 			Area<ZoneSettings> area = zoneSettings.toArea();
 			areasByName.put(zoneSettings.name.toLowerCase(), area);
 			zones[Util.getWorldIndexFromDimension(zoneSettings.dimension)].add(area);
 			
 			if (doSave)
 				saveAreas();
 			
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean remove(String name) {
 		if (name != null) {
 			Area<ZoneSettings> area = areasByName.get(name.toLowerCase());
 			if (area != null) {
 				areasByName.remove(name.toLowerCase());
 				zones[Util.getWorldIndexFromDimension(area.data.dimension)].remove(area);
 				saveAreas();
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	public Area<ZoneSettings> get(String name) {
 		return areasByName.get(name.toLowerCase());
 	}
 	
 	public List<String> getZoneNames() {
 		ArrayList names = new ArrayList(areasByName.keySet());
 		Collections.sort(names);
 		return names;
 	}
 	
 	public static boolean isProtectedEntityType(ACTION action, Entity entity) {
 		if (entity instanceof EntityLiving) {
 			
 			if (entity instanceof EntityVillager && action != ACTION.USE_ENTITY)
 				return true;
 			
 			return false;
 		}
 		
 		return true;
 	}
 	
 	public boolean isPlayerGloballyAllowed(String username) {
 		return alwaysAllowOps && ops.contains(username.trim().toLowerCase());
 	}
 	
 	protected boolean isPlayerZoneAllowed(String username, ZoneSettings settings) {
 		if (settings.allowOps && ops.contains(username.trim().toLowerCase()))
 			return true;
 		
 		if (settings.isPlayerAllowed(username))
 			return true;
 		
 		return false;
 	}
 	
 	public boolean isProtectedEntity(ACTION action, EntityPlayer player, Entity entity, int x, int y, int z) {
 		if (isProtectedEntityType(action, entity) && (player == null || !isPlayerGloballyAllowed(player.username))) {
 			List<Area<ZoneSettings>> areas = zones[Util.getWorldIndexFromDimension(entity.worldObj.provider.dimensionId)].get(x, y, z);
 			
 			for (Area<ZoneSettings> area : areas) {
				if (area.data != null && area.data.protectEntities) {
 					if (player != null && isPlayerZoneAllowed(player.username, area.data))
 						return false;
					
					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	public boolean isProtectedBlock(ACTION action, EntityPlayer player, Block block, World world, int x, int y, int z) {
 		if (player == null || !isPlayerGloballyAllowed(player.username)) {
 			List<Area<ZoneSettings>> areas = zones[Util.getWorldIndexFromDimension(world.provider.dimensionId)].get(x, y, z);
 			
 			for (Area<ZoneSettings> area : areas) {
 				//player.sendChatToPlayer("Checking against area: " + area.data.name);
				if (area.data != null && area.data.protectBlocks) {
					
 					if (player != null && isPlayerZoneAllowed(player.username, area.data))
 						return false;
 					
 					if (action == ACTION.ACTIVATE && area.data.allowDoors && (block == Block.doorWood || block == Block.trapdoor))
 						return false;
					
					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	public boolean isProtectedBlock(ACTION action, EntityPlayer player, Block block, World world, int x, int y, int z, int direction) {
 		
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
 		
 		return isProtectedBlock(action, player, block, world, x, y, z);
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
 				//event.getPlayer().sendChatToPlayer("Activate attempt " + event.getBlock().getBlockName() + " on D" + event.getDirection() + " of " + event.getX() + "/" + event.getY() + "/" + event.getZ());
 				break;
 			case REMOVE_ATTEMPT:
 				action = ACTION.DIG;
 				//event.getPlayer().sendChatToPlayer("Remove attempt " + (event.getBlock() == null ? null : event.getBlock().getBlockName()) + " at " + event.getX() + "/" + event.getY() + "/" + event.getZ());
 				break;
 			case PLACE_ATTEMPT:
 				action = ACTION.PLACE;
 				checkDirectionAdjusted = true;
 				//event.getPlayer().sendChatToPlayer("Place attempt " + event.getItemStack().getItemName() + " on D" + event.getDirection() + " of " + event.getX() + "/" + event.getY() + "/" + event.getZ());
 				break;
 		}
 		
 		if (action != null && (
 				isProtectedBlock(action, event.getPlayer(), event.getBlock(), event.getWorld(), event.getX(), event.getY(), event.getZ())
 				|| (checkDirectionAdjusted && isProtectedBlock(action, event.getPlayer(), event.getBlock(), event.getWorld(), event.getX(), event.getY(), event.getZ(), event.getDirection()))
 			)) {
 			
 			//event.getPlayer().sendChatToPlayer("Not allowed!");
 			
 			if (event.getType() == PlayerBlockEvent.TYPE.ACTIVATION_ATTEMPT)
 				event.markHandled();
 			else
 				event.markNotAllowed();
 		}
 	}
 
 	@Override
 	public void onBlockAction(BlockEvent event) {
 		if (event.getType() == BlockEvent.TYPE.EXPLODE_ATTEMPT) {
 			if (isProtectedBlock(ACTION.EXPLODE, null, event.getBlock(), event.getWorld(), event.getX(), event.getY(), event.getZ())) {
 				event.markNotAllowed();
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
 		if (event.getType() == EntityEvent.TYPE.EXPLODE_ATTEMPT) {
 			if (isProtectedEntity(ACTION.EXPLODE, null, event.getEntity(), MathHelper.floor_double(event.getEntity().posX), MathHelper.floor_double(event.getEntity().posY), MathHelper.floor_double(event.getEntity().posZ))) {
 				event.markNotAllowed();
 			}
 		}
 	}
 	
 	public void saveAreas() {
 		data.clear();
 		
 		int size = areasByName.size(), i = 1;
 		data.setInt("count", size);
 		for (Map.Entry<String, Area<ZoneSettings>> entry : areasByName.entrySet()) {
 			entry.getValue().data.saveToSettings(data, "zone" + i);
 			i++;
 		}
 		
 		try {
 			data.saveSettings();
 		} catch (IOException e) {
 			ModLoader.outputError(e, getName() + " failed (" + e.getClass().getSimpleName() + ") to save to the data file: " + e.getMessage());
 		}
 	}
 }
