 package com.censoredsoftware.demigods.structure;
 
 import com.censoredsoftware.demigods.Demigods;
 import com.censoredsoftware.demigods.Elements;
 import com.censoredsoftware.demigods.data.DataManager;
 import com.censoredsoftware.demigods.language.Translation;
 import com.censoredsoftware.demigods.player.DCharacter;
 import com.censoredsoftware.demigods.player.DPlayer;
 import com.censoredsoftware.demigods.util.Admins;
 import com.censoredsoftware.demigods.util.Structures;
 import com.google.common.base.Predicate;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import javax.annotation.Nullable;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 public class Shrine implements Structure
 {
 	private final static Schematic general = new Schematic("general", "_Alex", 2)
 	{
 		{
 			// Create the main block
 			add(new Selection(0, 1, 0, Material.GOLD_BLOCK));
 
 			// Create the ender chest and the block below
 			add(new Selection(0, 0, 0, Material.ENDER_CHEST));
 			add(new Selection(0, -1, 0, Material.SMOOTH_BRICK));
 
 			// Create the rest
 			add(new Selection(-1, 0, 0, Material.SMOOTH_STAIRS));
 			add(new Selection(1, 0, 0, Material.SMOOTH_STAIRS, (byte) 1));
 			add(new Selection(0, 0, -1, Material.SMOOTH_STAIRS, (byte) 2));
 			add(new Selection(0, 0, 1, Material.SMOOTH_STAIRS, (byte) 3));
 		}
 	};
 
 	public static enum ShrineDesign implements Design
 	{
 		GENERAL("general", general, new Selection(0, 1, 0));
 
 		private final String name;
 		private final Structure.Schematic schematic;
 		private final Selection clickableBlocks;
 
 		private ShrineDesign(String name, Structure.Schematic schematic, Selection clickableBlocks)
 		{
 			this.name = name;
 			this.schematic = schematic;
 			this.clickableBlocks = clickableBlocks;
 		}
 
 		@Override
 		public String getName()
 		{
 			return name;
 		}
 
 		@Override
 		public Set<Location> getClickableBlocks(Location reference)
 		{
 			return clickableBlocks.getBlockLocations(reference);
 		}
 
 		@Override
 		public Schematic getSchematic()
 		{
 			return schematic;
 		}
 	}
 
 	@Override
 	public Set<Flag> getFlags()
 	{
 		return new HashSet<Flag>()
 		{
 			{
 				add(Flag.DELETE_WITH_OWNER);
 				add(Flag.PROTECTED_BLOCKS);
 				add(Flag.TRIBUTE_LOCATION);
 			}
 		};
 	}
 
 	@Override
 	public String getStructureType()
 	{
 		return "Shrine";
 	}
 
 	@Override
 	public Design getDesign(String name)
 	{
 		return ShrineDesign.GENERAL;
 	}
 
 	@Override
 	public int getRadius()
 	{
 		return Demigods.config.getSettingInt("zones.shrine_radius");
 	}
 
 	@Override
 	public org.bukkit.event.Listener getUniqueListener()
 	{
 		return new Listener();
 	}
 
 	@Override
 	public Collection<Save> getAll()
 	{
 		return Structures.findAll(new Predicate<Save>()
 		{
 			@Override
 			public boolean apply(@Nullable Save save)
 			{
 				return save.getType().equals(getStructureType());
 			}
 		});
 	}
 
 	@Override
 	public Save createNew(Location reference, boolean generate)
 	{
 		Save save = new Save();
 		save.generateId();
 		save.setReferenceLocation(reference);
 		save.setType(getStructureType());
 		save.setDesign("general");
 		save.addFlags(getFlags());
 		save.setActive(true);
 		save.save();
 		if(generate) save.generate(false);
 		return save;
 	}
 
 	public static boolean validBlockConfiguration(Block block)
 	{
 		if(!block.getType().equals(Material.IRON_BLOCK)) return false;
 		if(!block.getRelative(1, 0, 0).getType().equals(Material.COBBLESTONE)) return false;
 		if(!block.getRelative(-1, 0, 0).getType().equals(Material.COBBLESTONE)) return false;
 		if(!block.getRelative(0, 0, 1).getType().equals(Material.COBBLESTONE)) return false;
 		if(!block.getRelative(0, 0, -1).getType().equals(Material.COBBLESTONE)) return false;
 		if(block.getRelative(1, 0, 1).getType().isSolid()) return false;
 		return !block.getRelative(1, 0, -1).getType().isSolid() && !block.getRelative(-1, 0, 1).getType().isSolid() && !block.getRelative(-1, 0, -1).getType().isSolid();
 	}
 
 	public static class Listener implements org.bukkit.event.Listener
 	{
 		@EventHandler(priority = EventPriority.HIGH)
 		public void createAndRemove(PlayerInteractEvent event)
 		{
 			if(event.getClickedBlock() == null) return;
 
 			if(Demigods.isDisabledWorld(event.getPlayer().getWorld())) return;
 
 			// Define variables
 			Block clickedBlock = event.getClickedBlock();
 			Location location = clickedBlock.getLocation();
 			Player player = event.getPlayer();
 
 			if(DPlayer.Util.isImmortal(player))
 			{
 				DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 
 				if(event.getAction() == Action.RIGHT_CLICK_BLOCK && character.getDeity().getClaimItems().contains(event.getPlayer().getItemInHand().getType()) && Shrine.validBlockConfiguration(event.getClickedBlock()))
 				{
 					try
 					{
 						// Shrine created!
 						Admins.sendDebug(ChatColor.RED + "Shrine created by " + character.getName() + " (" + character.getDeity() + ") at: " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ());
 						Structure.Save save = Elements.Structures.SHRINE.getStructure().createNew(location, true);
 						save.setOwner(character);
 						location.getWorld().strikeLightningEffect(location);
 
 						player.sendMessage(ChatColor.GRAY + Demigods.language.getText(Translation.Text.CREATE_SHRINE_1).replace("{alliance}", "" + ChatColor.YELLOW + character.getAlliance() + "s" + ChatColor.GRAY));
 						player.sendMessage(ChatColor.GRAY + Demigods.language.getText(Translation.Text.CREATE_SHRINE_2).replace("{deity}", "" + ChatColor.YELLOW + character.getDeity().getName() + ChatColor.GRAY));
						event.setCancelled(true);
 					}
 					catch(Exception e)
 					{
 						// Creation of shrine failed...
 						e.printStackTrace();
 					}
 				}
 			}
 
 			if(Admins.useWand(player) && Structures.partOfStructureWithType(location, "Shrine"))
 			{
 				event.setCancelled(true);
 
 				Structure.Save save = Structures.getStructureRegional(location);
 				DCharacter owner = save.getOwner();
 
 				if(DataManager.hasTimed(player.getName(), "destroy_shrine"))
 				{
 					// Remove the Shrine
 					save.remove();
 					DataManager.removeTimed(player.getName(), "destroy_shrine");
 
 					Admins.sendDebug(ChatColor.RED + "Shrine of (" + owner.getDeity() + ") at: " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ() + " removed.");
 
 					player.sendMessage(ChatColor.GREEN + Demigods.language.getText(Translation.Text.ADMIN_WAND_REMOVE_SHRINE_COMPLETE));
 				}
 				else
 				{
 					DataManager.saveTimed(player.getName(), "destroy_shrine", true, 5);
 					player.sendMessage(ChatColor.RED + Demigods.language.getText(Translation.Text.ADMIN_WAND_REMOVE_SHRINE));
 				}
 			}
 		}
 	}
 }
