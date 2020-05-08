 package de.bananaco.orbconomy;
 
 import java.util.HashMap;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class OrbconomyManager {
 	
 	private final JavaPlugin p;
 	private final HashMap<String, OrbBalance> balances;
	
 	public OrbconomyManager(JavaPlugin p) {
 	this(p, true);
 	}
	
 	public OrbconomyManager(JavaPlugin p, boolean showMessage) {
 		if(showMessage)
 		System.out.println("[Orbconomy] "+p.getDescription().getName() + " hooked into Orbconomy");
 		this.p = p;
 		this.balances = new HashMap<String, OrbBalance>();
 	}
 	
 	public OrbBalance getBalance(Player player) {
 		if(balances.containsKey(player.getName()))
 			return balances.get(player.getName());
 		OrbBalance ob = new OrbBalance(player);
 		balances.put(player.getName(), ob);
 		return ob;
 	}
 	
 	public OrbBalance getBalance(String playerName) throws Exception {
 		if(balances.containsKey(playerName))
 			return balances.get(playerName);
 		Player player = p.getServer().getPlayer(playerName);
 		if(player != null)
 			return getBalance(player);
 		else throw new Exception("No player with that name!");
 	}
 
 }
