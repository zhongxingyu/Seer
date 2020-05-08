 package net.nightpool.bukkit.paintor;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class PaintPlugin extends JavaPlugin {
 	
 	public Set<Player> painters;
 	public Set<Material> builds;
 	
 	public PaintCommand pc;
 	public PaintEvent pe;
 	
 	
 	@Override
 	public void onEnable(){
 		pc = new PaintCommand(this);
 		pe = new PaintEvent(this);
 		
 		painters = new HashSet<Player>();
 		
 		getCommand("paintor").setExecutor(pc);
 		getServer().getPluginManager().registerEvents(pe, this);
 
		getLogger().info("Enabled!");
		
 		for (int i : getConfig().getIntegerList("building-blocks")){
			builds.add(Material.getMaterial(i));
 		}
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 	}
 	
 	@Override
 	public void onDisable(){
 		save();
 		
 		for(Player p : painters){
 			p.sendMessage(ChatColor.DARK_RED+"Plugin disabled. You have stopped painting.");
 		}
 		getLogger().info("Disabled :(");
 	}
 
 	public void save() {
 		List<Integer> n = new ArrayList<Integer>();
 		for(Material m : builds){
 			n.add(m.getId());
 		}
 		getConfig().set("building-blocks", n);
 		
 		saveConfig();
 	}
 }
