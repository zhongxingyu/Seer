 package com.github.kaitoyuuki.MagicArrows;
 
 import java.io.IOException;
 
 import org.bukkit.event.HandlerList;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
import com.turt2live.metrics.EMetrics;
import com.turt2live.metrics.tracker.BasicTracker;
 
 @SuppressWarnings("unused")
 public class MagicArrows extends JavaPlugin {
 	public BasicTracker torch, fire, tnt;
 	@Override
 	public void onEnable() {
 		this.saveDefaultConfig();
 		getConfig();
 		PluginManager pm = this.getServer().getPluginManager();
 		pm.registerEvents(new BowListener(this), this);
 		try {
 			String graph = "Arrow Usage";
 			EMetrics metrics = new EMetrics(this);
 			
 			torch = EMetrics.createBasicTracker(graph, "torch");
 			fire = EMetrics.createBasicTracker(graph, "fire");
 			tnt = EMetrics.createBasicTracker(graph, "TNT");
 			
 			metrics.addTracker(torch);
 			metrics.addTracker(fire);
 			metrics.addTracker(tnt);
 			
 			metrics.startMetrics();
 		} catch (IOException e) {
 			
 		}
 	}
 	@Override
 	public void onDisable() {
 		HandlerList.unregisterAll(this);
 	}
 }
