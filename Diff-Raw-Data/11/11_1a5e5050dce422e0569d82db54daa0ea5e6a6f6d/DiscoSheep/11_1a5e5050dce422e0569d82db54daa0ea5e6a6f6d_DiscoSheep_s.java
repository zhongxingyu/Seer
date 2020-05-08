 package gibstick.bukkit.discosheep;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public final class DiscoSheep extends JavaPlugin {
 
 	Map<String, DiscoParty> parties = new HashMap<String, DiscoParty>();
 	private BaaBaaBlockSheepEvents blockEvents = new BaaBaaBlockSheepEvents(this);
 
 	@Override
 	public void onEnable() {
 		getCommand("ds").setExecutor(new DiscoSheepCommandExecutor(this));
 		getServer().getPluginManager().registerEvents(blockEvents, this);
 	}
 
 	@Override
 	public void onDisable() {
 		this.stopAllParties();
 	}
 
 	public synchronized Map<String, DiscoParty> getPartyMap() {
 		return this.parties;
 	}
 
 	public synchronized List<DiscoParty> getParties() {
 		return new ArrayList(this.getPartyMap().values());
 	}
 
 	public void stopParty(String name) {
 		if (this.hasParty(name)) {
 			this.getParty(name).stopDisco();
 		}
 	}
 	
 	public void stopAllParties(){
 		for(DiscoParty party :this.getParties()){
 			party.stopDisco();
 		}
 	}
 
 	public boolean hasParty(String name) {
 		return this.getPartyMap().containsKey(name);
 	}
 
 	public DiscoParty getParty(String name) {
 		return this.getPartyMap().get(name);
 	}
 
 	public void removeParty(String name) {
 		if (this.hasParty(name)) {
 			this.getPartyMap().remove(name);
 		}
 	}
 
 	public void startParty(Player player, int duration, int sheepAmount, int radius, boolean fireworksEnabled) {
 		if (!hasParty(player.getName())) {
 			new DiscoParty(this, player).startDisco(duration, sheepAmount, radius, fireworksEnabled);
 		}
 	}
 }
