 package muCkk.DeathAndRebirth.otherPlugins;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 import com.gmail.nossr50.mcMMO;
 import com.gmail.nossr50.datatypes.SkillType;
 
 import muCkk.DeathAndRebirth.DAR;
 import muCkk.DeathAndRebirth.messages.Messages;
 
 public class DARmcMMO {
 
 	private DAR plugin;
 	private mcMMO mcmmo = null;
 	
 	public DARmcMMO(DAR plugin, Plugin mcmmoPlug) {
 		this.plugin = plugin;
 		this.mcmmo = (mcMMO) mcmmoPlug;
 	}
 	
 	public void xpPenality(Player player, String type, int value) {
 		if (mcmmo == null) return;
 		SkillType skillType = SkillType.valueOf(type);
		mcMMO.getPlayerProfile(player).removeXP(skillType, value);
 		plugin.message.sendSkill(player, Messages.skillDropped, skillType.name());
 	}
 }
