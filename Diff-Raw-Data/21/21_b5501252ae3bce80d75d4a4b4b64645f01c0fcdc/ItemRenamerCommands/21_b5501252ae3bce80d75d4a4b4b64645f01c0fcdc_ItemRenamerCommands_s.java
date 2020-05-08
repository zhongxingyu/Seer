 /* This Source Code Form is subject to the terms of the Mozilla Public
  * License, v. 2.0. If a copy of the MPL was not distributed with this
  * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
 
 package org.shininet.bukkit.itemrenamer;
 
 import java.util.Arrays;
 import java.util.Deque;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.annotation.Nullable;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.shininet.bukkit.itemrenamer.configuration.ConfigParsers;
 import org.shininet.bukkit.itemrenamer.configuration.DamageLookup;
 import org.shininet.bukkit.itemrenamer.configuration.DamageValues;
 import org.shininet.bukkit.itemrenamer.configuration.ItemRenamerConfiguration;
 import org.shininet.bukkit.itemrenamer.configuration.RenameRule;
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Ranges;
 
 public class ItemRenamerCommands implements CommandExecutor {
 	// Different permissions
 	private static final String PERM_GET = "itemrenamer.config.get";
 	private static final String PERM_SET= "itemrenamer.config.set";
 	
 	// The super command
 	private static final Object COMMAND_ITEMRENAMER = "ItemRenamer";
 	
 	// Recognized sub-commands
 	public enum Commands {
 		GET_AUTO_UPDATE,
 		GET_CREATIVE_DISABLE, 
 		SET_AUTO_UPDATE,
 		SET_CREATIVE_DISABLE,
 		GET_WORLD_PACK, 
 		SET_WORLD_PACK,
 		GET_ITEM,
 		DELETE_PACK,
 		SET_NAME, 
 		ADD_LORE, 
 		DELETE_LORE,
		RELOAD
 	}
 	
 	private ItemRenamer plugin;
 	private ItemRenamerConfiguration config;
 	
 	private CommandMatcher<Commands> matcher;
 	
 	public ItemRenamerCommands(ItemRenamer plugin, ItemRenamerConfiguration config) {
 		this.plugin = plugin;
 		this.matcher = registerCommands();
 		this.config = config;
 	}
 	
 	private CommandMatcher<Commands> registerCommands() {
 		CommandMatcher<Commands> output = new CommandMatcher<Commands>();
 		output.registerCommand(Commands.GET_AUTO_UPDATE, PERM_GET, "get", "setting", "autoupdate");
 		output.registerCommand(Commands.GET_CREATIVE_DISABLE, PERM_GET, "get", "setting", "creativedisable");
 		output.registerCommand(Commands.SET_AUTO_UPDATE, PERM_SET, "set", "setting", "autoupdate");
 		output.registerCommand(Commands.SET_CREATIVE_DISABLE, PERM_SET, "set", "setting", "creativedisable");
 		output.registerCommand(Commands.GET_WORLD_PACK, PERM_GET, "get", "world");
 		output.registerCommand(Commands.SET_WORLD_PACK, PERM_SET, "set", "world");
 		output.registerCommand(Commands.DELETE_PACK, PERM_SET, "delete", "pack");
 		output.registerCommand(Commands.GET_ITEM, PERM_GET, "get", "item");
 		output.registerCommand(Commands.SET_NAME, PERM_SET, "set", "name");
 		output.registerCommand(Commands.ADD_LORE, PERM_SET, "add", "lore");
 		output.registerCommand(Commands.DELETE_LORE, PERM_SET, "delete", "lore");
 		output.registerCommand(Commands.RELOAD, PERM_SET, "reload");
 		return output;
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] arguments) {
 		if (cmd.getName().equals(COMMAND_ITEMRENAMER)) {
 			LinkedList<String> input = Lists.newLinkedList(Arrays.asList(arguments));
 			
 			// See which node is closest
 			CommandMatcher<Commands>.CommandNode node = matcher.matchClosest(input);
 			
 			if (node.isCommand()) {
 				try {
 					sender.sendMessage(ChatColor.GOLD + performCommand(node.getCommand(), input));
 				} catch (CommandErrorException e) {
 					sender.sendMessage(ChatColor.RED + e.getMessage());
 				}
 			} else {
 				sender.sendMessage(ChatColor.RED + "Sub commands: " + 
 									Joiner.on(", ").join(node.getChildren()));
 			}
 			
 			// It's still somewhat correct
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	private String performCommand(Commands command, Deque<String> args) {
 		try {
 			switch (command) {
 				case GET_AUTO_UPDATE: 
 					expectCommandCount(args, 0, "No arguments needed.");
 					return formatBoolean("Auto update is %s.", config.isAutoUpdate()); 
 				case GET_CREATIVE_DISABLE: 
 					expectCommandCount(args, 0, "No arguments needed.");
 					return formatBoolean("Creative disable is %s.", config.isCreativeDisabled());
 				case SET_AUTO_UPDATE:
 					expectCommandCount(args, 1, "Need a yes/no argument.");
 					config.setAutoUpdate(parseBoolean(args.poll()));
 					return "Updated auto update.";
 				case SET_CREATIVE_DISABLE:
 					expectCommandCount(args, 1, "Need a yes/no argument.");
 					config.setCreativeDisabled(parseBoolean(args.poll()));
 					return "Updated creative disable.";
 				case GET_WORLD_PACK: 
 					expectCommandCount(args, 1, "Need a world name.");
 					return getWorldPack(args);
 				case SET_WORLD_PACK:
 					expectCommandCount(args, 2, "Need a world name and a world pack name.");
 					return setWorldPack(args);
 				case DELETE_PACK:
 					expectCommandCount(args, 1, "Need a world pack name.");
 					return deleteWorldPack(args);
 				case GET_ITEM:
 					return getItem(args);
 				case SET_NAME:
 					return setItemName(args);
 				case ADD_LORE:
 					return addLore(args);
 				case DELETE_LORE:
 					return clearLore(args);
 				case RELOAD:
 					config.reload();
 			}
 			
 		} catch (IllegalArgumentException e) {
 			throw new CommandErrorException(e.getMessage(), e);
 		}
 		throw new CommandErrorException("Unrecognized sub command: " + command);
 	}
 	
 	private String getItem(Deque<String> args) {
 		// Get all the arguments before we begin
 		final DamageLookup lookup = getLookup(args);
 		final DamageValues damage = getDamageValues(args);
 		
 		if (damage == DamageValues.ALL)
 			return "Rename: " + lookup.getAllRule();
 		if (damage == DamageValues.ALL)
 			return "Rename: " + lookup.getAllRule();
 		else if (damage.getRange().lowerEndpoint() == damage.getRange().upperEndpoint()) 
 			return "Rename: " + lookup.getDefinedRule(damage.getRange().lowerEndpoint());
 		else
 			throw new CommandErrorException("Cannot parse damage. Must be a single value, ALL or OTHER.");
 	}
 	
 	private String setItemName(Deque<String> args) {
 		// Get all the arguments before we begin
 		final DamageLookup lookup = getLookup(args);
 		final DamageValues damage = getDamageValues(args);
 		final String name = Joiner.on(" ").join(args);
 		
 		lookup.setTransform(damage, new Function<RenameRule, RenameRule>() {
 			@Override
 			public RenameRule apply(@Nullable RenameRule input) {
 				return input.withName(name);
 			}
 		});
 		
 		return String.format("Set the name of every item %s.", name);
 	}
 	
 	private String addLore(Deque<String> args) {
 		// Get all the arguments before we begin
 		final DamageLookup lookup = getLookup(args);
 		final DamageValues damage = getDamageValues(args);
 		final String lore = Joiner.on(" ").join(args);
 		
 		// Apply the change
 		lookup.setTransform(damage, new Function<RenameRule, RenameRule>() {
 			@Override
 			public RenameRule apply(@Nullable RenameRule input) {
 				return input.withAdditionalLore(Arrays.asList(lore));
 			}
 		});
 		
		return String.format("Set the lore of every item to %s.", lore);
 	}
 	
 	private String clearLore(Deque<String> args) {
 		// Get all the arguments before we begin
 		final DamageLookup lookup = getLookup(args);
 		final DamageValues damage = getDamageValues(args);
 		
 		// Apply the change
 		lookup.setTransform(damage, new Function<RenameRule, RenameRule>() {
 			@Override
 			public RenameRule apply(@Nullable RenameRule input) {
 				return new RenameRule(input.getName(), null);
 			}
 		});
 		return "Reset the lore of every item.";
 	}
 
 	/**
 	 * Retrieve the damage lookup based on the item pack and item ID in the parameter stack.
 	 * @param args - the parameter stack.
 	 * @return The corresponding damage lookup.
 	 */
 	private DamageLookup getLookup(Deque<String> args) {
 		String pack = args.pollFirst();
 		Integer itemID = getItemID(args);
 		
 		return config.getRenameConfig().getLookup(pack, itemID);
 	}
 	
 	private DamageValues getDamageValues(Deque<String> args) {
 		try {
 			return DamageValues.parse(args);
 		} catch (IllegalArgumentException e) {
 			// Wrap it in a more specific exception
 			throw new CommandErrorException(e.getMessage(), e);
 		}
 	}
 	
 	private int getItemID(Deque<String> args) {
 		try {
 			List<Integer> result = ConfigParsers.getIntegers(args, 1, Ranges.closed(0, 4096));
 			
 			if (result.size() == 1) {
 				return result.get(0);
 			} else {
 				throw new CommandErrorException("Cannot find item ID.");
 			}
 		} catch (IllegalArgumentException e) {
 			throw new CommandErrorException(e.getMessage(), e);
 		}
 	}
 	
 	private String deleteWorldPack(Deque<String> args) {
 		String pack = args.poll();
 		
 		config.getRenameConfig().removePack(pack);
 		return "Deleted pack " + pack;
 	}
 
 	private void expectCommandCount(Deque<String> args, int expected, String error) {
 		if (expected != args.size())
 			throw new CommandErrorException("Error: " + error);
 	}
 	
 	private String getWorldPack(Deque<String> args) {
 		String world = args.poll();
 		
 		// Retrieve world pack
 		return "Item pack for " + world + ": " + config.getWorldPack(world);
 	}
 
 	/**
 	 * Set the world pack we will use based on the input arguments.
 	 * @param args - the input arguments.
 	 * @return The message to return to the player.
 	 */
 	public String setWorldPack(Deque<String> args) {
 		String world = checkWorld(args.poll()), pack = args.poll();
 		
 		config.setWorldPack(world, pack);
 		return "Set the item pack in world " + world + " to " + pack;
 	}
 	
 	/**
 	 * Determine if the given world actually exists.
 	 * @param world - the world to test.
 	 * @return The name of the world.
 	 * @throws CommandErrorException If the world doesn't exist.
 	 */
 	private String checkWorld(String world) {
 		// Ensure the world exists
 		if (plugin.getServer().getWorld(world) == null)
 			throw new CommandErrorException("Cannot find world " + world);
 		return world;
 	}
 	
 	private String formatBoolean(String format, boolean value) {
 		return String.format(format, value ? "enabled" : "disabled");
 	}
 	
 	// Simple boolean parsing
 	private boolean parseBoolean(String value) {
 		if (Arrays.asList("true", "yes", "enabled", "on", "1").contains(value))
 			return true;
 		else if (Arrays.asList("false", "no", "disabled", "off", "0").contains(value)) {
 			return false;
 		} else {
 			throw new CommandErrorException("Cannot parse " + value + " as a boolean (yes/no)");
 		}
 	}
 }
