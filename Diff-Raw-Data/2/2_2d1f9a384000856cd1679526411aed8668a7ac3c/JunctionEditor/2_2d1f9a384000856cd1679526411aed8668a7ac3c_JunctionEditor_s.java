 package com.gmail.emertens.pdxtrackrouter.listeners;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 
 import com.gmail.emertens.pdxtrackrouter.PdxTrackRouter;
 import com.gmail.emertens.pdxtrackrouter.events.PlayerUseCommandSignEvent;
 
 @SuppressWarnings("serial")
 final class JunctionEditException extends Exception {
 	private final String message;
 	public JunctionEditException(final String message) {
 		this.message = message;
 	}
 
 	public String getErrorMessage() {
 		return message;
 	}
 }
 
 final class Cursor {
 	private final int index;
 	private final Block block;
 
 	public Cursor(int index, Block block) {
 		this.index = index;
 		this.block = block;
 	}
 
 	public int getIndex() {
 		return index;
 	}
 
 	public Block getBlock() {
 		return block;
 	}
 
 }
 
 public final class JunctionEditor implements CommandExecutor, Listener {
 
 	private static final int MAX_SIGN_LENGTH = 15;
 	private final Map<Player,Block> selectedBlocks = new HashMap<Player,Block>();
 
 
 	@Override
 	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
 
 		// Obtain player
 		final Player player;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		} else {
 			sender.sendMessage(ChatColor.RED + "This command is for players");
 			return true;
 		}
 
 		// Obtain junction block
 		final Block selectedBlock = selectedBlocks.get(player);
 		if (selectedBlock == null || !checkJunctionBlock(selectedBlock)) {
 			player.sendMessage(ChatColor.RED + "No junction sign selected");
 			return true;
 		}
 
 		try {
 			// Dispatch sub-command
 			if (args.length >= 2) {
 				final int index = Integer.parseInt(args[1]);
 				final String text = ChatColor.translateAlternateColorCodes('&', concatenateArgs(args, 2));
 
 				if (text.length() > MAX_SIGN_LENGTH) {
 					throw new JunctionEditException("Line too long");
 				}
 
 				if (args[0].equalsIgnoreCase("insert")) {
 					insertOperation(selectedBlock, index, text);
 					return true;
 				} else if (args[0].equalsIgnoreCase("delete")) {
 					deleteOperation(player, selectedBlock, index);
 					return true;
 				} else if (args[0].equalsIgnoreCase("change")) {
 					changeOperation(selectedBlock, index, text);
 					return true;
 				}
 			}
 			return false;
 		} catch (NumberFormatException e) {
 			return false;
 		} catch (JunctionEditException e) {
 			player.sendMessage(ChatColor.RED + e.getErrorMessage());
 			return true;
 		}
 	}
 
 	private void changeOperation(Block selectedBlock, int index, final String text) throws JunctionEditException {
 		final Cursor cursor = normalizeIndex(selectedBlock, index);
 		index = cursor.getIndex();
 		selectedBlock = cursor.getBlock();
 		final Sign sign = (Sign) selectedBlock.getState();
 		sign.setLine(index, text);
 		sign.update();
 	}
 
 	private void deleteOperation(Player player, Block selectedBlock, int index) throws JunctionEditException {
 
 		final Cursor cursor = normalizeIndex(selectedBlock, index);
 		selectedBlock = cursor.getBlock();
 		index = cursor.getIndex();
 
 		Sign sign = null;
 
 		while (true) {
 			final BlockState state = selectedBlock.getState();
 
 			if (!(state instanceof Sign)) {
 
 				if (sign != null) {
 					sign.setLine(3, "");
 					sign.update();
 				}
 
 				return;
 			}
 
 			final Sign newSign = (Sign) state;
 
 			if (sign != null) {
 				sign.setLine(3, newSign.getLine(0));
 				sign.update();
 			}
 
 			sign = newSign;
 
 			for (int i = index; i < 3; i++) {
 				sign.setLine(i, sign.getLine(i + 1));
 			}
 
 			index = 0;
 			selectedBlock = selectedBlock.getRelative(BlockFace.DOWN);
 		}
 	}
 
 	private void insertOperation(Block block, int index, String text) throws JunctionEditException {
 		final Cursor cursor = normalizeIndex(block, index);
 		block = cursor.getBlock();
 		index = cursor.getIndex();
 
 		while (true) {
 			final BlockState state = block.getState();
 			if (state instanceof Sign) {
 				final Sign sign = (Sign) state;
 				for (int i = index; i < 4; i++) {
 					String temp = sign.getLine(i);
 					sign.setLine(i, text);
 					text = temp;
 				}
 				sign.update();
 
 				index = 0;
 				block = block.getRelative(BlockFace.DOWN);
 			} else {
 				return;
 			}
 		}
 	}
 
 	private Cursor normalizeIndex(final Block selectedBlock, int index)
 			throws JunctionEditException {
 
 		if (index < 1) {
 			throw new JunctionEditException("Index too large");
 		}
 
 		Block cursor = selectedBlock;
 		while (true) {
 			if (!(cursor.getState() instanceof Sign)) {
 				throw new JunctionEditException("Index too large");
 			}
 
 			if (index >= 4) {
 				cursor = cursor.getRelative(BlockFace.DOWN);
 				index -= 4;
 			} else {
 				return new Cursor(index, cursor);
 			}
 		}
 	}
 
 	private static String concatenateArgs(final String[] args, final int start) {
 		StringBuilder builder = new StringBuilder();
 		boolean first = true;
 		for (int i = start; i < args.length; i++) {
 			if (!first) {
 				builder.append(' ');
 			}
 			builder.append(args[i]);
 		}
 		return builder.toString();
 	}
 
 	@EventHandler(ignoreCancelled = true)
 	public void onPlayerUseCommandSign(PlayerUseCommandSignEvent event) {
 		if (PdxTrackRouter.isJunctionHeader(event.getSign().getLine(0))) {
 			final Player player = event.getPlayer();
 			if (PdxTrackRouter.playerCanEditJunctions(player)) {
 				selectBlock(player, event.getBlock());
 			}
 		}
 	}
 
 	private static boolean checkJunctionBlock(final Block block) {
 		final BlockState state = block.getState();
 		if (state instanceof Sign) {
 			final Sign sign = (Sign) state;
 			if (PdxTrackRouter.isJunctionHeader(sign.getLine(0))) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private void selectBlock(Player player, Block clickedBlock) {
 		selectedBlocks.put(player, clickedBlock);
 		player.sendMessage(ChatColor.GREEN + "Junction sign selected, use /junction to edit");
 	}
 }
