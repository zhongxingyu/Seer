 package net.milkbowl.combatevents.pvp;
 
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import net.milkbowl.combatevents.CombatEventsListener;
 import net.milkbowl.combatevents.events.EntityKilledByEntityEvent;
 import net.milkbowl.vault.economy.EconomyResponse;
 
 public class CombatListener extends CombatEventsListener {
	
 	CombatListener() {
 	}
	
 	@Override
 	public void onEntityKilledByEntityEvent(EntityKilledByEntityEvent event) {
 		if (event.getAttacker() instanceof Player && event.getKilled() instanceof Player) {
 			if (Config.isGlobalPvPMessage()) {
				int rand = new Random().nextInt(Config.getGlobalPvPMessages().size() - 1);
				String message = Config.getGlobalPvPMessages().get(rand);
 				for (Player player : Bukkit.getServer().getOnlinePlayers()) {
 					player.sendMessage(ChatColor.YELLOW + formatMessage(message, new String[] {"%ATTACKER%", "%KILLED%"}, new String[] {"", ""}));
 				}
 			}
 			Player attacker = (Player) event.getAttacker();
 			Player killed = (Player) event.getKilled();
 			EconomyResponse killedBalance = CombatEventsPvP.econ.getBalance(killed.getName());
 			double reward = Config.getReward();
 			if (Config.getRewardType().equals("flat")) {
 				//check to make sure player has enough money to pay out the player
 				if (killedBalance.balance < reward)
 					reward = killedBalance.balance;
 			} else if (Config.getRewardType().equals("percent")) {
 				reward = (reward / 100) * killedBalance.amount;
 			}
 			CombatEventsPvP.econ.withdrawPlayer(killed.getName(), reward);
 			CombatEventsPvP.econ.depositPlayer(attacker.getName(), reward);
 			//Send our messages
 			attacker.sendMessage(formatMessage(Config.getKillerMessage(), new String[] {"%PLAYER%", "%REWARD%"}, new String[] {killed.getName(), CombatEventsPvP.econ.format(reward)}));
 			killed.sendMessage(formatMessage(Config.getDeathMessage(), new String[] {"%PLAYER%", "%REWARD%"}, new String[] {attacker.getName(), CombatEventsPvP.econ.format(reward)}));
			
 		}
 	}
	
 	private String formatMessage(String message, String[] replaceText, String[] replacers) {
 		for (int i = 0; i < replaceText.length; i++) {
 			message.replace(replaceText[i], replacers[i]);
 		}
 		return message;
 	}
	
 }
