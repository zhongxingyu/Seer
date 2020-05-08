 package net.minecore.minepermit.miner;
 
 import java.util.Map;
 import java.util.TreeMap;
 
 import net.minecore.Miner;
 import net.minecore.minepermit.permits.Permit;
 import net.minecore.minepermit.world.PermitArea;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 
 public class PermitMiner {
 
 	private final Miner miner;
 	private final Map<String, PermitHolder> permits;
 
 	public PermitMiner(Miner miner) {
 		this.miner = miner;
 		permits = new TreeMap<String, PermitHolder>();
 		ConfigurationSection cs = miner.getConfigurationSection("minepermit.permits");
 
 		for (String key : cs.getKeys(false)) {
 
 			if (cs.isConfigurationSection(key)) {
 				PermitHolder ph = new PermitHolder();
 				permits.put(key, ph);
 				PermitHolder.createPermitHolderFromConfigurationSection(cs
 						.getConfigurationSection(key));
 			}
 
 		}
 	}
 
 	public String getPlayer() {
 		return miner.getPlayerName();
 	}
 
 	public boolean hasPermit(PermitArea pa, Material m) {
 		return getPermit(pa, m) != null;
 	}
 
 	public boolean addPermit(PermitArea pa, Permit p) {
 
 		return getPermitHolder(pa).addPermit(p);
 
 	}
 
 	public boolean addUniversalPermit(PermitArea pa, Permit p) {
 		return getPermitHolder(pa).addUniversalPermit(p);
 	}
 
 	public Map<String, PermitHolder> getPermits() {
 		return permits;
 	}
 
 	public boolean removePermit(PermitArea pa, Material m) {
 		return getPermitHolder(pa).removePermit(m);
 	}
 
 	public Permit getPermit(PermitArea pa, Material m) {
 		return getPermitHolder(pa).getPermit(m);
 	}
 
 	public PermitHolder getPermitHolder(PermitArea pa) {
 		PermitHolder ph = permits.get(pa.getStringRepresentation());
 
 		if (ph == null)
 			permits.put(pa.getStringRepresentation(), ph = new PermitHolder());
 
 		return ph;
 	}
 
 	public void save() {
 		ConfigurationSection cs = miner.getConfigurationSection("permits");
 		for (String key : permits.keySet()) {
 			String tmp = key.replace('.', '-');
 			ConfigurationSection child = cs.getConfigurationSection(tmp);
 			if (child == null)
 				child = cs.createSection(tmp);
 			permits.get(key).saveTo(child);
 		}
 	}
 
 }
