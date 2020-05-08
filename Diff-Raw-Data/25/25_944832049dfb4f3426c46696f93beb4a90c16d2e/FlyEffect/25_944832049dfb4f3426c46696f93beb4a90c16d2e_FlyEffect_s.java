 package net.lordsofcode.zephyrus.effects;
 
import net.lordsofcode.zephyrus.Zephyrus;
 import net.lordsofcode.zephyrus.utils.Lang;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.entity.Player;
 
 /**
  * Zephyrus
  * 
  * @author minnymin3
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  * 
  */
 
 public class FlyEffect implements IEffect {
 
 	private int ID;
 	
 	public FlyEffect(int ID) {
 		this.ID = ID;
 	}
 	
 	@Override
 	public void onApplied(Player player) {
 		player.setAllowFlight(true);
		player.sendMessage(Lang.get("spells.fly.applied").replace("[TIME]",
				Zephyrus.getUser(player).getEffectTime(EffectType.FLY) + ""));
 	}
 
 	@Override
 	public void onRemoved(Player player) {
 		player.setAllowFlight(false);
 		player.sendMessage(ChatColor.GRAY + Lang.get("spells.fly.end"));
 	}
 
 	@Override
 	public void onTick(Player player) {
 	}
 
 	@Override
 	public void onWarning(Player player) {
 		player.sendMessage(ChatColor.GRAY + Lang.get("spells.fly.warning"));
 	}
 
 	@Override
 	public void onStartup(Player player) {
 		if (player.getGameMode() != GameMode.CREATIVE) {
 			player.setAllowFlight(false);
 		}
 	}
 
 	@Override
 	public int getTypeID() {
 		return this.ID;
 	}
 	
 	@Override
 	public int getTickTime() {
 		return 0;
 	}
 
 }
