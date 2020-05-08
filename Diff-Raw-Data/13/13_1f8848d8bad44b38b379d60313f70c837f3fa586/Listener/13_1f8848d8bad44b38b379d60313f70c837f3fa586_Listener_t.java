 import java.util.*;
 import java.util.logging.*;
 
 public class Listener extends PluginListener {
 	
 	List<Criminal> wantedCriminals = new ArrayList<Criminal>();
 	Logger pluginLogger = Logger.getLogger("Minecraft");
 	CommandParser pluginParser = new CommandParser();
 	Settings pluginSettings = new Settings();
 
 	protected void finalize() throws Throwable {
 		saveSettings();
 		super.finalize();
 	}
 
 	public boolean onCommand(Player player, java.lang.String[] split) {
 		return parseCommand(player, split);
 	}
 
 	public boolean onConsoleCommand(java.lang.String[] split) {
 		return parseConsoleCommand(split);
 	}
 
 	public boolean parseCommand(Player player, java.lang.String[] chatCommand) {
 		return pluginParser.parseCommand(player.getName(), chatCommand);
 	}
 
 	public boolean parseConsoleCommand(java.lang.String[] consoleCommand) {
 		return pluginParser.parseConsoleCommand(consoleCommand);
 	}
 
 	public boolean onDamage(PluginLoader.DamageType type, BaseEntity attacker, BaseEntity defender, int amount) {
 		if ((defender != null) && (attacker != null)) {
 			if ((defender.isPlayer()) && (attacker.isPlayer())) {
 				if (defender.getPlayer().getHealth() <= amount) {
 					playerDefeated(defender.getPlayer(), attacker.getPlayer());
 				}
 			}
 		}
 		return false;
 	}
 
 	public void playerDefeated(Player victim, Player slayer) {
 		for (Criminal criminal : wantedCriminals) {
 			if (criminal.getUsername().equalsIgnoreCase(victim.getName())) {
 				slayer.sendMessage("f[eWanted!f] You have been awarded for your deed!");
 				victim.sendMessage("f[eWanted!f] You no longer have a bounty!");
 				sendGlobalMessage("f[eWanted!f] \"" + victim.getName() + "\" has been defeated!");
 				for (Reward reward : criminal.getRewards()) {
 					slayer.giveItem(reward.getRewardItemId(), reward.getRewardAmount());
 				}
				wantedCriminals.remove(criminal);
 				return;
 			} else {
 				break;
 			}
 		}
 	}
 
 	public void onLogin(Player player) {
 		if (wantedCriminals.size() > 0) {
 			for (Criminal wantedPlayer : wantedCriminals) {
 				if (wantedPlayer.getUsername().equalsIgnoreCase(player.getName())) {
 					alertPlayers(player);
					return;
 				}
 			}
 		}
 	}
 
 	public void alertPlayers(Player player) {
 		sendGlobalMessage("f[eWanted!f] \"" + player.getName() + "\" has a bounty!");
 	}
 
 	public void loadSettings() {
 		if (pluginSettings.fileExists()) {
 			pluginSettings.loadSettings();
 			wantedCriminals = pluginSettings.getCriminals();
 		} else {
 			pluginSettings.saveSettings(true);
 		}
 	}
 
 	public void saveSettings() {
 		if (pluginSettings.fileExists()) {
 			pluginSettings.saveSettings(wantedCriminals);
 		} else {
 			pluginSettings.saveSettings(true);
 		}
 	}
 
 	public void sendGlobalMessage(final String globalMessage) {
 		etc.getServer().addToServerQueue(new Runnable(){
 			public void run() {
 				for (Player player: etc.getServer().getPlayerList()) {
 					Player p = etc.getServer().getPlayer(player.getName());
 					p.sendMessage(globalMessage);
 				}
 			}
 		});
 	}
 
 }
