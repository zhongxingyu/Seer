 package com.censoredsoftware.demigods.engine.command;
 
 import com.censoredsoftware.censoredlib.helper.WrappedCommand;
 import com.censoredsoftware.censoredlib.util.Images;
 import com.censoredsoftware.demigods.engine.Demigods;
 import com.censoredsoftware.demigods.engine.battle.Battle;
 import com.censoredsoftware.demigods.engine.data.DataManager;
 import com.censoredsoftware.demigods.engine.player.DCharacter;
 import com.censoredsoftware.demigods.engine.player.DPlayer;
 import com.censoredsoftware.demigods.engine.structure.Structure;
 import com.censoredsoftware.demigods.engine.structure.StructureData;
 import com.censoredsoftware.demigods.greek.structure.Altar;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterators;
 import com.google.common.collect.Sets;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.Player;
 
 import javax.imageio.ImageIO;
 import java.awt.image.BufferedImage;
 import java.net.URL;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 public class DevelopmentCommands extends WrappedCommand
 {
 	public DevelopmentCommands()
 	{
 		super(Demigods.PLUGIN, false);
 	}
 
 	@Override
 	public Set<String> getCommands()
 	{
 		return Sets.newHashSet("obelisk", "test2", "test3"); // "test1", "hspawn", "nearestaltar"
 	}
 
 	@Override
 	public boolean processCommand(CommandSender sender, Command command, String[] args)
 	{
 		// if(command.getName().equalsIgnoreCase("test1")) return test1(sender, args);
 		if(command.getName().equalsIgnoreCase("test2")) return test2(sender, args);
 		else if(command.getName().equalsIgnoreCase("test3")) return test3(sender, args);
 		// else if(command.getName().equalsIgnoreCase("hspawn")) return hspawn(sender);
 		// else if(command.getName().equalsIgnoreCase("nearestaltar")) return nearestAltar(sender);
 		else if(command.getName().equalsIgnoreCase("obelisk")) return obelisk(sender, args);
 		return false;
 	}
 
 	private static boolean test1(CommandSender sender, final String[] args)
 	{
 		Player player = (Player) sender;
 
 		for(Battle battle : Battle.Util.getAllActive())
 			battle.end();
 
 		return true;
 	}
 
 	private static boolean test2(CommandSender sender, final String[] args)
 	{
 		Player player = (Player) sender;
 
 		try
 		{
 			player.sendMessage("  ");
 
 			URL doge = new URL(args[0]);
 
 			BufferedImage veryImage = ImageIO.read(doge);
 
 			veryImage = Images.getScaledImage(veryImage, 128, 128);
 
 			if(player.isOp()) Images.convertImageToSchematic(veryImage).generate(player.getLocation());
 
 			player.sendMessage("  ");
 		}
 		catch(Throwable suchError)
 		{
 			player.sendMessage(ChatColor.RED + "many problems. " + suchError.getMessage());
 		}
 
 		return true;
 
 		// Player player = (Player) sender;
 
 		// StructureData obelisk = Structure.Util.getInRadiusWithFlag(player.getLocation(), Structure.Flag.NO_GRIEFING);
 		// if(obelisk != null)
 		// {
 		// Get all of the connected obelisks
 		// for(StructureData save : Structure.Util.getStructureWeb(obelisk, Structure.Flag.NO_GRIEFING, 20))
 		// {
 		// if(save == obelisk) continue;
 		// player.sendMessage(save.getId().toString());
 		// }
 		// }
 		// else player.sendMessage(ChatColor.RED + "No Obelisk found.");
 
 		// return true;
 
 		// Player player = (Player) sender;
 
 		// Messages.broadcast(ChatColor.RED + "Removing all non-altar structures.");
 
 		// for(StructureData save : Collections2.filter(StructureData.Util.loadAll(), new Predicate<StructureData>()
 		// {
 		// @Override
 		// public boolean apply(StructureData structure)
 		// {
 		// return !structure.getType().equals(GreekStructure.ALTAR);
 		// }
 		// }))
 		// save.remove();
 
 		// Messages.broadcast(ChatColor.RED + "All non-altar structures have been removed.");
 
 		// if(Demigods.ERROR_NOISE) Errors.triggerError(ChatColor.GREEN + player.getName(), new ColoredStringBuilder().gray(" " + Unicodes.getRightwardArrow() + " ").red("Test error.").build());
 
 		// return true;
 	}
 
 	private static boolean test3(CommandSender sender, final String[] args)
 	{
 		Player player = (Player) sender;
 
 		Images.sendMapImage(player, Images.getPlayerHead(player.getName()));
 
 		return true;
 	}
 
 	private static boolean hspawn(CommandSender sender)
 	{
 		Player player = (Player) sender;
 
 		// This SHOULD happen automatically, but bukkit doesn't do this, so we need to.
 
 		if(player.isInsideVehicle() && player.getVehicle() instanceof Horse)
 		{
 			Horse horse = (Horse) player.getVehicle();
 			horse.eject();
 			horse.teleport(player.getWorld().getSpawnLocation());
 			horse.setPassenger(player);
 			player.sendMessage(ChatColor.YELLOW + "Teleported to spawn...");
 		}
 
 		return true;
 	}
 
 	private static boolean nearestAltar(CommandSender sender)
 	{
 		Player player = (Player) sender;
 
 		if(Altar.Util.isAltarNearby(player.getLocation()))
 		{
 			StructureData save = Altar.Util.getAltarNearby(player.getLocation());
 			player.teleport(save.getReferenceLocation().clone().add(2.0, 1.5, 0));
 			player.sendMessage(ChatColor.YELLOW + "Nearest Altar found.");
 		}
 		else player.sendMessage(ChatColor.YELLOW + "There is no alter nearby.");
 
 		return true;
 	}
 
 	/**
 	 * Temp command while testing obelisks.
 	 */
 	private static boolean obelisk(CommandSender sender, final String[] args)
 	{
 		Player player = (Player) sender;
 
 		if(args.length != 3)
 		{
 			player.sendMessage(ChatColor.RED + "Not enough arguments.");
 			return false;
 		}
 
 		StructureData obelisk = Structure.Util.getInRadiusWithFlag(player.getLocation(), Structure.Flag.NO_GRIEFING);
 		if(obelisk != null)
 		{
 			DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 			if(!obelisk.getOwner().equals(character.getId()))
 			{
 				player.sendMessage(ChatColor.RED + "You don't control this Obelisk.");
 				return true;
 			}
 
 			DCharacter workWith = obeliskGetCharacter(args[1], args[2]);
 
 			if(workWith == null)
 			{
 				player.sendMessage(ChatColor.RED + "Character/Player (" + args[2] + ") not found.");
 				return true;
 			}
 
 			if(!DCharacter.Util.areAllied(workWith, character))
 			{
 				player.sendMessage(ChatColor.RED + "You are not allied with " + workWith.getDeity().getColor() + character.getName() + ChatColor.RED + ".");
 				return true;
 			}
 
 			if(args[0].equalsIgnoreCase("add"))
 			{
 				if(!obelisk.getMembers().contains(workWith.getId()))
 				{
 					obelisk.addMember(workWith.getId());
 					player.sendMessage(workWith.getDeity().getColor() + workWith.getName() + ChatColor.YELLOW + " has been added to the Obelisk!");
 				}
 				else player.sendMessage(ChatColor.RED + "Already a member.");
 			}
 			else if(args[0].equalsIgnoreCase("remove"))
 			{
 				if(obelisk.getMembers().contains(workWith.getId()))
 				{
 					obelisk.removeMember(workWith.getId());
 					player.sendMessage(workWith.getDeity().getColor() + workWith.getName() + ChatColor.YELLOW + " has been removed from the Obelisk!");
 				}
 				else player.sendMessage(ChatColor.RED + "Not a member.");
 			}
 		}
 		else player.sendMessage(ChatColor.RED + "No Obelisk found.");
 
 		return true;
 	}
 
 	private static DCharacter obeliskGetCharacter(String type, final String name)
 	{
 		if(type.equalsIgnoreCase("character")) return DCharacter.Util.getCharacterByName(name);
 		if(!type.equalsIgnoreCase("player")) return null;
 		try
 		{
 			return Iterators.find(DataManager.players.values().iterator(), new Predicate<DPlayer>()
 			{
 				@Override
 				public boolean apply(DPlayer dPlayer)
 				{
 					return dPlayer.getPlayerName().equals(name);
 				}
 			}).getCurrent();
 		}
 		catch(NoSuchElementException ignored)
 		{}
 		return null;
 	}
 }
