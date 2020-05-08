 package org.dyndns.pamelloes.ExtraFurnaces.block;
 
 import java.lang.reflect.Method;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.event.Event;
 import org.bukkit.event.HandlerList;
 import org.bukkit.plugin.IllegalPluginAccessException;
 import org.bukkit.plugin.RegisteredListener;
 import org.dyndns.pamelloes.ExtraFurnaces.SpoutFurnaces;
 import org.dyndns.pamelloes.ExtraFurnaces.data.CustomFurnaceData;
 import org.dyndns.pamelloes.ExtraFurnaces.packet.OpenGUIServer;
 import org.dyndns.pamelloes.ExtraFurnaces.packet.OpenGUI.GUIType;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.block.design.GenericCubeBlockDesign;
 import org.getspout.spoutapi.block.design.Texture;
 import org.getspout.spoutapi.io.AddonPacket;
 import org.getspout.spoutapi.material.block.GenericCubeCustomBlock;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public abstract class CustomFurnace extends GenericCubeCustomBlock {
 	protected SpoutFurnaces plugin;
 	private Texture texture;
 	private int[] idson, idsoff;
 	private GenericCubeBlockDesign desoff, deson;
 	
 	public CustomFurnace(SpoutFurnaces plugin, String name, Texture tex, int[] idson,int[] idsoff) {
		super(plugin, name, Material.FURNACE.getId(), new GenericCubeBlockDesign(plugin, tex, idsoff));
 		desoff=(GenericCubeBlockDesign) getBlockDesign();
 		deson=new GenericCubeBlockDesign(plugin, tex, idson);
 		this.idson=idson;
 		this.idsoff=idsoff;
 		this.plugin=plugin;
 		texture=tex;
 	}
 	
 	public abstract GUIType getGUIType();
 
 	@Override
     public boolean onBlockInteract(World world, int x, int y, int z, SpoutPlayer player) {
 		if(!player.isSpoutCraftEnabled()) {
 			player.sendMessage(ChatColor.RED + "This is a special furnace and requires Spoutcraft for you to use it.");
 			player.sendMessage(ChatColor.RED + "You can get Spoutcraft at http://get.spout.org/");
 			return true;
 		}
 		AddonPacket packet = new OpenGUIServer(getGUIType());
 		packet.send(player);
 		CustomFurnaceData dat = (CustomFurnaceData) SpoutManager.getChunkDataManager().getBlockData("SpoutFurnaces", world, x, y, z);
 		dat.onPlayerOpenFurnace(player);
 		SpoutFurnaces.map.put(player, dat);
 		return true;
     }
 
 	@Override
     public void onBlockDestroyed(World world, int x, int y, int z) {
 		CustomFurnaceData dat = (CustomFurnaceData) SpoutManager.getChunkDataManager().getBlockData("SpoutFurnaces", world, x, y, z);
 		if(dat==null)  return;
 		OpenGUIServer close = new OpenGUIServer();
 		close.setType(GUIType.CloseGui.ordinal());
 		for(SpoutPlayer p : SpoutFurnaces.map.keySet()) if(SpoutFurnaces.map.get(p).equals(dat)) close.send(p);
 		SpoutManager.getChunkDataManager().removeBlockData("SpoutFurnaces", world, x, y, z);
 		Location loc = new Location(world,x,y,z);
 		for(int i = 0; i < dat.getSizeInventory(); i++) if(dat.getStackInSlot(i)!=null) world.dropItem(loc, dat.getStackInSlot(i));
         for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry : plugin.getPluginLoader().createRegisteredListeners(dat, plugin).entrySet()) {
            for(RegisteredListener r : entry.getValue()) getEventListeners(getRegistrationClass(entry.getKey())).unregister(r);
         }
 	}
 
     private Class<? extends Event> getRegistrationClass(Class<? extends Event> clazz) {
         try {
             clazz.getDeclaredMethod("getHandlerList");
             return clazz;
         } catch (NoSuchMethodException e) {
             if (clazz.getSuperclass() != null
                     && !clazz.getSuperclass().equals(Event.class)
                     && Event.class.isAssignableFrom(clazz.getSuperclass())) {
                 return getRegistrationClass(clazz.getSuperclass().asSubclass(Event.class));
             } else {
                 throw new IllegalPluginAccessException("Unable to find handler list for event " + clazz.getName());
             }
         }
     }
 
     private HandlerList getEventListeners(Class<? extends Event> type) {
         try {
             Method method = getRegistrationClass(type).getDeclaredMethod("getHandlerList");
             method.setAccessible(true);
             return (HandlerList) method.invoke(null);
         } catch (Exception e) {
             throw new IllegalPluginAccessException(e.toString());
         }
     }
 }
