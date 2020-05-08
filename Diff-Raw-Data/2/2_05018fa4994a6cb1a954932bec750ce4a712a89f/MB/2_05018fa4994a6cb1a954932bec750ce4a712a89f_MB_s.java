 package net.mcforge.mb.commands;
 
 import net.mcforge.API.CommandExecutor;
 import net.mcforge.API.ManualLoad;
 import net.mcforge.API.action.Action;
 import net.mcforge.API.action.BlockChangeAction;
 import net.mcforge.API.plugin.PlayerCommand;
 import net.mcforge.chat.ChatColor;
 import net.mcforge.iomodel.Player;
 import net.mcforge.mb.blocks.MessageBlock;
 import net.mcforge.world.Block;
 
 @ManualLoad
 public class MB extends PlayerCommand {
 
 	private Block b;
 	@Override
 	public void execute(Player player, String[] arg1) {
 		if (arg1.length == 0) { help(player); return; }
 		int startindex = 0;
		if (Block.getBlock(arg1[0]) != Block.getBlock("UNKNOWN")) {
 			b = Block.getBlock(arg1[0]);
 			startindex++;
 		}
 		String message = "";
 		for (int i = startindex; i < arg1.length; i++) {
 			message += arg1[i - startindex];
 		}
 		message = message.trim();
 		player.sendMessage("Place a block where the message block will go!");
 		Thread t = new Run(player, message);
 		t.start();
 	}
 
 	@Override
 	public int getDefaultPermissionLevel() {
 		return 50;
 	}
 
 	@Override
 	public String getName() {
 		return "mb";
 	}
 
 	@Override
 	public String[] getShortcuts() {
 		return new String[] { };
 	}
 
 	@Override
 	public void help(CommandExecutor arg0) {
 		arg0.sendMessage("/mb <message> - Place a message in a block! When the block is hit, the message will display");
 		arg0.sendMessage("/mb [block] <message> - Place a message in a [block] block! When the block is hit, the message will display");
 	}
 
 	@Override
 	public boolean isOpCommandDefault() {
 		return false;
 	}
 	
 	private class Run extends Thread {
 		String message;
 		Player player;
 		public Run(Player player, String message) { this.player = player; this.message = message; }
 		
 		@Override
 		public void run() {
 			Action<BlockChangeAction> action = new BlockChangeAction();
 			action.setPlayer(player);
 			try {
 				BlockChangeAction response = action.waitForResponse();
 				MessageBlock mb;
 				if (b == null)
 					mb = new MessageBlock(message, Block.getBlock("White"));
 				else
 					mb = new MessageBlock(message, b);
 				Player.GlobalBlockChange((short)response.getX(), (short)response.getY(), (short)response.getZ(), mb, player.getLevel(), player.getServer());
 				player.sendMessage(ChatColor.Bright_Green + "Message Block placed!");
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 				player.sendMessage("An error has occured..");
 				return;
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 				player.sendMessage("An error has occured..");
 				return;
 			}
 		}
 	}
 }
