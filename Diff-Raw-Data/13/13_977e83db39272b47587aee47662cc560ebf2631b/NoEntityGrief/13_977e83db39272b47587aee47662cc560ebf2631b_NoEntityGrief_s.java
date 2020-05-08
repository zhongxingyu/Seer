 package com.fishrock123.noentitygrief;
 import java.util.*;
 import org.bukkit.entity.*;
 import org.bukkit.event.*;
 import org.bukkit.event.block.*;
 import org.bukkit.event.entity.*;
 import org.bukkit.plugin.java.JavaPlugin;
public class NoEntityGrief extends JavaPlugin implements Listener {
 	private HashMap<Class<? extends Entity>, List<String>> EWs = new HashMap<Class<? extends Entity>, List<String>>();
 	@Override public void onEnable() {
 		if (getConfig().options().header() == null) {
 			getConfig().options().copyHeader();
 			getConfig().options().copyDefaults(true);
 			saveConfig();
 		}
 		EWs.put(Enderman.class, getConfig().getStringList("Endermen"));
 		EWs.put(Sheep.class, getConfig().getStringList("Sheep"));
 		EWs.put(Creeper.class, getConfig().getStringList("MobExplosion"));
 		EWs.put(TNTPrimed.class, getConfig().getStringList("TNT"));
 		EWs.put(Snowman.class, getConfig().getStringList("Snowman"));
 		EWs.put(Silverfish.class, getConfig().getStringList("Silverfish"));
 		getServer().getPluginManager().registerEvents(this, this);
 	}
 	@EventHandler public void onBlockChange(EntityChangeBlockEvent e) {
 		if (e.getEntity() instanceof Enderman && (EWs == null || !EWs.containsKey(Enderman.class) || !EWs.get(Enderman.class).contains(e.getBlock().getLocation().getWorld().getName()))) e.setCancelled(true);
		if (e.getEntity() instanceof Silverfish && (EWs == null || !EWs.containsKey(Silverfish.class) || !EWs.get(Silverfish.class).contains(e.getBlock().getLocation().getWorld().getName()))) e.setCancelled(true);
		if (e.getEntity() instanceof Sheep && (EWs == null || !EWs.containsKey(Sheep.class) || !EWs.get(Sheep.class).contains(e.getBlock().getLocation().getWorld().getName()))) {
 			e.setCancelled(true);
 			((Sheep)e.getEntity()).setSheared(false);
 		}
 	}
 	@EventHandler public void onExplosion(EntityExplodeEvent e) {
		if (e.getEntity() instanceof TNTPrimed && (EWs == null || !EWs.containsKey(TNTPrimed.class) || !EWs.get(TNTPrimed.class).contains(e.getLocation().getWorld().getName()))) e.blockList().clear();
		else if (EWs == null || !EWs.containsKey(Creeper.class) || !EWs.get(Creeper.class).contains(e.getLocation().getWorld().getName())) e.blockList().clear();
 	}
 	@EventHandler public void onEntityForm(EntityBlockFormEvent e) {
 		if (EWs == null || !EWs.containsKey(Snowman.class) || !EWs.get(Snowman.class).contains(e.getBlock().getLocation().getWorld().getName())) e.setCancelled(true);
 	}
 }
