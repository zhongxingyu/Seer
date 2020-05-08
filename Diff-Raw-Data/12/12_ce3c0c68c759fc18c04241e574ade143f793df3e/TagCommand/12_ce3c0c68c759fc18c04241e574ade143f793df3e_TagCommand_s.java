 package coolawesomeme.basics_plugin.commands;
 
 import java.util.List;
 import java.util.LinkedList;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import coolawesomeme.basics_plugin.Basics;
 import coolawesomeme.basics_plugin.MinecraftColors;
 
 public class TagCommand implements CommandExecutor{
 	
 	private static Player originalTagger;
 	private static List<String> taggedPlayers = new LinkedList<String>();
 	private static List<String> nonTaggedPlayers = new LinkedList<String>();
 	public static boolean isTagOn = false;
 	private Basics basics;
 	private int tagMinutes = Basics.tagMinutes;
 	
 	public TagCommand(Basics instance){
 		basics = instance;
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if(args.length > 1){
 			sender.sendMessage("Incorrect command syntax!");
 			return false;
 		}else if(args.length == 1){
 			if(args[0].equals("end")){
 				if(sender.isOp() || sender.hasPermission("basics.tag.end")){
 					if(isTagOn){
 						Bukkit.getServer().broadcastMessage("The game has ended!");
 						if(nonTaggedPlayers.size() > 1){
 							Bukkit.getServer().broadcastMessage("The winners are:");
 							Bukkit.getServer().broadcastMessage(nonTaggedPlayers.toString().replace("[", "").replace("]", ""));
 						}else{
 							Bukkit.getServer().broadcastMessage("The winner is:");
 							Bukkit.getServer().broadcastMessage(nonTaggedPlayers.toString().replace("[", "").replace("]", "").replace(", ", ""));
 						}
 						isTagOn = false;
 					}else{
 						sender.sendMessage("No current game of tag!");
 					}
 				}
 				return true;
 			}
 			return false;
 		}else{
 			Player[] onlinePlayers = Bukkit.getOnlinePlayers();
 			Random random = new Random();
			int playerIndex = random.nextInt(args.length);
 			originalTagger = onlinePlayers[playerIndex];
 			nonTaggedPlayers = getOnlinePlayers();
 			nonTaggedPlayers.remove(originalTagger);
 			isTagOn = true;
			Bukkit.getServer().broadcastMessage(MinecraftColors.red + "A game of tag has started!");
 			Bukkit.getServer().broadcastMessage(originalTagger.getName() + " is the tagger!");
 			originalTagger.sendMessage("Right click other players to tag them!");
 			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(basics, new Runnable() {
 				@Override 
 				public void run() {
 					Bukkit.getServer().broadcastMessage("The game has ended!");
 					if(nonTaggedPlayers.size() > 1){
 						Bukkit.getServer().broadcastMessage("The winners are:");
 						Bukkit.getServer().broadcastMessage(nonTaggedPlayers.toString().replace("[", "").replace("]", ""));
 					}else{
						Bukkit.getServer().broadcastMessage("The winner is:");
						Bukkit.getServer().broadcastMessage(nonTaggedPlayers.toString().replace("[", "").replace("]", "").replace(", ", ""));
 					}
 					isTagOn = false;
 				}
 			}, (long)(tagMinutes*20*60));
 			return true;
 		}
 	}
 
 	private List<String> getOnlinePlayers() {
 		List<String> onlinePlayers = new LinkedList<String>();
 		Player[] onlinePlayers1 = Bukkit.getOnlinePlayers();
 		for(int i = 0; i < onlinePlayers1.length; i++){
 			onlinePlayers.add(onlinePlayers1[i].getName());
 		}
 		return onlinePlayers;
 	}
 	
 	public static void tagPlayer(Player player){
 		if(isTagOn){
 			taggedPlayers.add(player.getName());
 			nonTaggedPlayers.remove(player.getName());
 			Bukkit.getServer().broadcastMessage(MinecraftColors.italics + "A player has been tagged!");
 			if(nonTaggedPlayers.size() != 0){
 				Bukkit.getServer().broadcastMessage(nonTaggedPlayers.size() + " non tagged players left!");
 			}else{
 				Bukkit.getServer().broadcastMessage(MinecraftColors.red + "Game over!");
 				Bukkit.getServer().broadcastMessage(originalTagger.getName() + " has won!");
 				isTagOn = false;
 			}
 		}
 	}
 	
 	public static List<Player> getNonTaggedPlayers(){
 		if(isTagOn){
 			List<Player> playerList = new LinkedList<Player>();
 			for(String x : nonTaggedPlayers){
 				playerList.add(Bukkit.getPlayer(x));
 			}
 			return playerList;
 		}else{
 			return null;
 		}
 	}
 	
 	public static List<Player> getTaggedPlayers(){
 		if(isTagOn){
 			List<Player> playerList = new LinkedList<Player>();
 			for(String x : taggedPlayers){
 				playerList.add(Bukkit.getPlayer(x));
 			}
 			return playerList;
 		}else{
 			return null;
 		}
 	}
 }
