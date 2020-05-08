 package de.philworld.bukkit.magicsigns.coloredsigns;
 
 import de.philworld.bukkit.magicsigns.MagicSigns;
 
 public class ColoredSigns {
 
 	public static final String COLOR_SIGNS_PERMISSION = "magicsigns.color";
 	private ColoredSignsListener listener;
 
 	public ColoredSigns(MagicSigns plugin) {
 		this.listener = new ColoredSignsListener();
 		plugin.getServer().getPluginManager().registerEvents(listener, plugin);
 	}
	
	public ColoredSignsListener getListener() {
		return listener;
	}
 
 }
