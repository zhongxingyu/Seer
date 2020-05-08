 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 
 import Base.CardPlayer;
 
 
 public class BlockListener extends PlayerListener{
 	Main plugin;
 
 	public BlockListener(Main instance){
 		plugin = instance;
 	}
 
 	public void onPlayerInteract(PlayerInteractEvent event){
 		Action action = event.getAction();
 		Player player = event.getPlayer();
 		CardPlayer cardplayer = plugin.blackjack.match(player);
 		Block block = event.getClickedBlock();
		if(block == null){
			return;
		}
 		if(plugin.blackjack.getPlayers().contains(player)){
 			if(!plugin.betting && plugin.turn != null && cardplayer.equals(plugin.turn)){
 				if(action.equals(Action.LEFT_CLICK_BLOCK)){
 					if(block.getState() instanceof Sign){
 						Sign sign = (Sign) block.getState();
 						if(sign.getLine(1).equalsIgnoreCase("[HIT]")){
 							player.sendMessage(ChatColor.GOLD + "hit");
 							cardplayer.giveCard(plugin.blackjack.hit());
 							plugin.playerTurn(cardplayer);
 						}
 						else if(sign.getLine(1).equalsIgnoreCase("[STAY]")){
 							player.sendMessage(ChatColor.GOLD + "stay");
 							plugin.blackjack.stay(cardplayer);
 							plugin.game();
 						}
 					}
 				}
 				else if(action.equals(Action.RIGHT_CLICK_BLOCK)){
 					if(block.getState() instanceof Sign){
 						Sign sign = (Sign) block.getState();
 						if(sign.getLine(1).equalsIgnoreCase("[HIT]")){
 							int bet = plugin.blackjack.bets.get(cardplayer);
 							if(cardplayer.getCash() >= bet){
 								player.sendMessage(ChatColor.GOLD + "double down");
 								cardplayer.takeCash(bet);
 								plugin.blackjack.bets.put(cardplayer,bet*2);
 								cardplayer.giveCard(plugin.blackjack.hit());
 								plugin.blackjack.stay(cardplayer);
 								plugin.game();
 							}
 							else{
 								plugin.turn.getPlayer().sendMessage(ChatColor.RED + "You don't have enough money to do that!");
 							}
 						}
 					}
 				}
 			}
 		}
 		else if(plugin.betting && plugin.better != null && plugin.better.equals(cardplayer)){
 			
 		}
 	}
 }
