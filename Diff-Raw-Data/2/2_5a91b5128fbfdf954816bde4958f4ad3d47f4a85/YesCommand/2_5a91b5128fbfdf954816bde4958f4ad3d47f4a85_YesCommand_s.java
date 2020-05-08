 package org.melonbrew.fee.commands;
 
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Furnace;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.material.Door;
 import org.bukkit.material.Gate;
 import org.bukkit.material.TrapDoor;
 import org.melonbrew.fee.Fee;
 import org.melonbrew.fee.Phrase;
 import org.melonbrew.fee.Session;
 
 public class YesCommand implements CommandExecutor {
 	private final Fee plugin;
 	
 	private final Random random;
 	
 	public YesCommand(Fee plugin){
 		this.plugin = plugin;
 		
 		random = new Random();
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		if (!(sender instanceof Player)){
 			if (random.nextFloat() <= 0.50F){
 				Phrase.YOU_ARE_NOT_A_PLAYER.sendWithPrefix(sender);
 			}else {
 				Phrase.YOU_ARE_NOT_A_PLAYER_TWO.sendWithPrefix(sender);
 			}
 			
 			return true;
 		}
 		
 		Player player = (Player) sender;
 		
 		Session session = plugin.getSession(player);
 		
 		if (session == null){
 			Phrase.NO_PENDING_COMMAND.sendWithPrefix(sender);
 			
 			return true;
 		}
 		
 		String command = session.getCommand();
 		
 		Block block = session.getBlock();
 		
 		double money;
 		
 		if (command == null){
			Block signBlock = block.getRelative(BlockFace.DOWN);
 			
 			if (signBlock == null || !(signBlock.getState() instanceof Sign)){
 				return true;
 			}
 			
 			Sign sign = (Sign) signBlock.getState();
 			
 			String firstLine = ChatColor.stripColor(sign.getLine(0));
 			
 			String noColorSign = ChatColor.stripColor(Phrase.SIGN_START.parse());
 			
 			if (!(firstLine.equalsIgnoreCase(noColorSign))){
 				return true;
 			}
 			
 			money = Double.parseDouble(sign.getLine(1));
 		}else {
 			money = plugin.getKeyMoney(command);
 		}
 		
 		if (money == -1){
 			plugin.removeSession(player);
 
 			Phrase.NO_PENDING_COMMAND.sendWithPrefix(sender);
 			
 			return true;
 		}
 		
 		if (!plugin.getEconomy().has(player.getName(), money)){
 			Phrase.NEED_MONEY.sendWithPrefix(sender, plugin.getEconomy().format(money));
 			
 			return true;
 		}
 		
 		plugin.getEconomy().withdrawPlayer(player.getName(), money);
 		
 		String reciever = plugin.getConfig().getString("serveraccount");
 		
 		if (plugin.getEconomy().hasAccount(reciever)){
 			plugin.getEconomy().depositPlayer(reciever, money);
 		}
 		
 		if (command == null){
 			session.setNextCommandFree(true);
 			
 			player.chat(command);
 		}else {
 			BlockState state = block.getState();
 			
 			if (state instanceof Door){
 				((Door) state).setOpen(true);
 			}else if (state.getData() instanceof TrapDoor){
 				((TrapDoor) state.getData()).setOpen(true);
 			} else if (state instanceof Furnace){
 				player.openInventory(((Furnace) state).getInventory());
 			} else if (state instanceof Chest){
 				player.openInventory(((Chest) state).getInventory());
 			} else if (state.getData() instanceof Gate){
 				((Gate) state.getData()).setOpen(true);
 			}
 		}
 		
 		return true;
 	}
 
 }
