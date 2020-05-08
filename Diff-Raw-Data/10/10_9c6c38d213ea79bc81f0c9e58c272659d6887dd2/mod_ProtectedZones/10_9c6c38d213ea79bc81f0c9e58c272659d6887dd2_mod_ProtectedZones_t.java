 package btwmod.protectedzones;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import net.minecraft.src.Block;
 import net.minecraft.src.Entity;
 import net.minecraft.src.EntityLiving;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.ICommand;
 import net.minecraft.src.MathHelper;
 import btwmods.CommandsAPI;
 import btwmods.IMod;
 import btwmods.ModLoader;
 import btwmods.PlayerAPI;
 import btwmods.WorldAPI;
 import btwmods.io.Settings;
 import btwmods.player.IPlayerActionListener;
 import btwmods.player.PlayerActionEvent;
 import btwmods.player.PlayerBlockEvent;
 import btwmods.player.IPlayerBlockListener;
 import btwmods.util.Area;
 import btwmods.util.Zones;
 import btwmods.world.BlockEvent;
 import btwmods.world.IBlockListener;
 
 public class mod_ProtectedZones implements IMod, IPlayerBlockListener, IBlockListener, IPlayerActionListener {
 	
 	public enum ACTION { PLACE, DIG, BROKEN, ACTIVATE, EXPLODE, USE_ENTITY };
 	private Map<String, Area<ZoneSettings>> areasByName = new TreeMap<String, Area<ZoneSettings>>();
 	private Zones<ZoneSettings> zones = new Zones<ZoneSettings>();
 	
 	private Set<ICommand> commands = new LinkedHashSet<ICommand>();
 	
 	private Settings data;
 
 	@Override
 	public String getName() {
 		return "Protected Zones";
 	}
 
 	@Override
 	public void init(Settings settings, Settings data) throws Exception {
 		PlayerAPI.addListener(this);
 		WorldAPI.addListener(this);
 		registerCommand(new CommandZoneAdd(this));
 		registerCommand(new CommandZoneDelete(this));
 		registerCommand(new CommandZoneList(this));
 		
 		this.data = data;
 		
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
 			zones.add(area);
			
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
 				zones.remove(area);
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
 	
 	protected boolean isProtectedEntity(ACTION action, EntityPlayer player, Entity entity, int x, int y, int z) {
 		if (!(entity instanceof EntityLiving)) {
 			List<Area<ZoneSettings>> areas = zones.get(x, y, z);
 			
 			for (Area<ZoneSettings> area : areas) {
 				if (area.data != null) {
 					// TODO: additional checks.
 				}
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	protected boolean isProtectedBlock(ACTION action, EntityPlayer player, Block block, int x, int y, int z) {
 		List<Area<ZoneSettings>> areas = zones.get(x, y, z);
 		
 		for (Area<ZoneSettings> area : areas) {
 			if (area.data != null) {
 				// TODO: additional checks.
 			}
 			return true;
 		}
 		
 		return false;
 	}
 	
 	protected boolean isProtectedBlock(ACTION action, EntityPlayer player, Block block, int x, int y, int z, int direction) {
 		
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
 		
 		return isProtectedBlock(action, player, block, x, y, z);
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
 		}
 		
 		if (action != null && (
 				isProtectedBlock(action, event.getPlayer(), event.getBlock(), event.getX(), event.getY(), event.getZ())
 				|| (checkDirectionAdjusted && isProtectedBlock(action, event.getPlayer(), event.getBlock(), event.getX(), event.getY(), event.getZ(), event.getDirection()))
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
 			if (isProtectedBlock(ACTION.EXPLODE, null, event.getBlock(), event.getX(), event.getY(), event.getZ())) {
 				event.markNotAllowed();
 			}
 		}
 	}
 
 	@Override
 	public void onPlayerAction(PlayerActionEvent event) {
 		if (event.getType() == PlayerActionEvent.TYPE.PLAYER_USE_ENTITY_ATTEMPT) {
 			if (isProtectedEntity(ACTION.USE_ENTITY, event.getPlayer(), event.getEntity(), MathHelper.floor_double(event.getEntity().posX), MathHelper.floor_double(event.getEntity().posY), MathHelper.floor_double(event.getEntity().posZ))) {
 				event.markNotAllowed();
 			}
 		}
 	}
 	
 	private void saveAreas() {
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
