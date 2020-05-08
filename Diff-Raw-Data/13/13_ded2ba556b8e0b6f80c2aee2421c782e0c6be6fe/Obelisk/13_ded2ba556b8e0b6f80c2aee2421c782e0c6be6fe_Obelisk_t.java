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
 
 public class Obelisk implements Structure
 {
 	private final static Structure.Schematic general = new Structure.Schematic("general", "HmmmQuestionMark", 3)
 	{
 		{
 			// Clickable block.
 			add(new Selection(0, 0, 2, Material.SMOOTH_BRICK, (byte) 3));
 
 			// Everything else.
 			add(new Selection(0, 0, -1, 0, 2, -1, Selection.Preset.STONE_BRICK));
 			add(new Selection(0, 0, 1, 0, 2, 1, Selection.Preset.STONE_BRICK));
 			add(new Selection(1, 0, 0, 1, 2, 0, Selection.Preset.STONE_BRICK));
 			add(new Selection(-1, 0, 0, -1, 2, 0, Selection.Preset.STONE_BRICK));
 			add(new Selection(0, 4, -1, 0, 5, -1, Selection.Preset.STONE_BRICK));
 			add(new Selection(0, 4, 1, 0, 5, 1, Selection.Preset.STONE_BRICK));
 			add(new Selection(1, 4, 0, 1, 5, 0, Selection.Preset.STONE_BRICK));
 			add(new Selection(-1, 4, 0, -1, 5, 0, Selection.Preset.STONE_BRICK));
 			add(new Selection(0, 3, 0, Material.REDSTONE_BLOCK));
 			add(new Selection(0, 4, 0, Material.REDSTONE_BLOCK));
 			add(new Selection(0, 3, -1, Material.REDSTONE_LAMP_ON));
 			add(new Selection(0, 3, 1, Material.REDSTONE_LAMP_ON));
 			add(new Selection(1, 3, 0, Material.REDSTONE_LAMP_ON));
 			add(new Selection(-1, 3, 0, Material.REDSTONE_LAMP_ON));
 			add(new Selection(0, 5, 0, Material.REDSTONE_LAMP_ON));
 			add(new Selection(1, 5, -1, Selection.Preset.VINE_1));
 			add(new Selection(-1, 5, -1, Selection.Preset.VINE_1));
 			add(new Selection(1, 5, 1, Selection.Preset.VINE_4));
 			add(new Selection(-1, 5, 1, Selection.Preset.VINE_4));
 		}
 	};
 	private final static Structure.Schematic desert = new Structure.Schematic("desert", "HmmmQuestionMark", 3)
 	{
 		{
 			// Clickable block.
 			add(new Selection(0, 0, 2, Material.SANDSTONE, (byte) 1));
 
 			// Everything else.
 			add(new Selection(0, 0, -1, 0, 2, -1, Material.SANDSTONE));
 			add(new Selection(0, 0, 1, 0, 2, 1, Material.SANDSTONE));
 			add(new Selection(1, 0, 0, 1, 2, 0, Material.SANDSTONE));
 			add(new Selection(-1, 0, 0, -1, 2, 0, Material.SANDSTONE));
 			add(new Selection(0, 4, -1, 0, 5, -1, Material.SANDSTONE));
 			add(new Selection(0, 4, 1, 0, 5, 1, Material.SANDSTONE));
 			add(new Selection(1, 4, 0, 1, 5, 0, Material.SANDSTONE));
 			add(new Selection(-1, 4, 0, -1, 5, 0, Material.SANDSTONE));
 			add(new Selection(0, 3, 0, Material.REDSTONE_BLOCK));
 			add(new Selection(0, 4, 0, Material.REDSTONE_BLOCK));
 			add(new Selection(0, 3, -1, Material.REDSTONE_LAMP_ON));
 			add(new Selection(0, 3, 1, Material.REDSTONE_LAMP_ON));
 			add(new Selection(1, 3, 0, Material.REDSTONE_LAMP_ON));
 			add(new Selection(-1, 3, 0, Material.REDSTONE_LAMP_ON));
 			add(new Selection(0, 5, 0, Material.REDSTONE_LAMP_ON));
 		}
 	};
 
 	public static enum ObeliskDesign implements Structure.Design
 	{
 		GENERAL("general", general, new Selection(0, 0, 2)), DESERT("desert", desert, new Selection(0, 0, 2));
 
 		private final String name;
 		private final Structure.Schematic schematic;
 		private final Selection clickableBlocks;
 
 		private ObeliskDesign(String name, Structure.Schematic schematic, Selection clickableBlocks)
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
 		public Structure.Schematic getSchematic()
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
 				add(Flag.PROTECTED_BLOCKS);
 				add(Flag.NO_GRIEFING);
 			}
 		};
 	}
 
 	@Override
 	public String getStructureType()
 	{
 		return "Obelisk";
 	}
 
 	@Override
 	public Design getDesign(String name)
 	{
 		if(name.equals(general.toString())) return ObeliskDesign.GENERAL;
 		return ObeliskDesign.DESERT;
 	}
 
 	@Override
 	public int getRadius()
 	{
 		return Demigods.config.getSettingInt("zones.obelisk_radius");
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
 		save.setDesign(getDesign(reference).getName());
 		save.addFlags(getFlags());
 		save.setActive(true);
 		save.save();
 		if(generate) save.generate(false);
 		return save;
 	}
 
 	public Design getDesign(Location reference)
 	{
 		switch(reference.getBlock().getBiome())
 		{
 			case BEACH:
 			case DESERT:
 			case DESERT_HILLS:
 				return ObeliskDesign.DESERT;
 			default:
 				return ObeliskDesign.GENERAL;
 		}
 	}
 
 	public static boolean validBlockConfiguration(Block block)
 	{
 		if(!block.getType().equals(Material.REDSTONE_BLOCK)) return false;
 		if(!block.getRelative(1, 0, 0).getType().equals(Material.COBBLESTONE)) return false;
 		if(!block.getRelative(-1, 0, 0).getType().equals(Material.COBBLESTONE)) return false;
 		if(!block.getRelative(0, 0, 1).getType().equals(Material.COBBLESTONE)) return false;
 		if(!block.getRelative(0, 0, -1).getType().equals(Material.COBBLESTONE)) return false;
 		if(block.getRelative(1, 0, 1).getType().isSolid()) return false;
 		return !block.getRelative(1, 0, -1).getType().isSolid() && !block.getRelative(-1, 0, 1).getType().isSolid() && !block.getRelative(-1, 0, -1).getType().isSolid();
 	}
 
 	public static boolean noPvPStructureNearby(Location location)
 	{
 		for(Save structureSave : Structures.loadAll())
 		{
 			if(structureSave.getStructure().getFlags().contains(Flag.NO_PVP) && structureSave.getReferenceLocation().distance(location) <= (Demigods.config.getSettingInt("altar_radius") + Demigods.config.getSettingInt("obelisk_radius") + 6)) return true;
 		}
 		return false;
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
 
 				if(event.getAction() == Action.RIGHT_CLICK_BLOCK && character.getDeity().getClaimItems().contains(event.getPlayer().getItemInHand().getType()) && Obelisk.validBlockConfiguration(event.getClickedBlock()))
 				{
 					if(Obelisk.noPvPStructureNearby(location))
 					{
 						player.sendMessage(ChatColor.YELLOW + "This location is too close to a no-pvp zone, please try again.");
 						return;
 					}
 
 					try
 					{
 						// Obelisk created!
 						Admins.sendDebug(ChatColor.RED + "Obelisk created by " + character.getName() + " at: " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ());
 						Structure.Save save = Elements.Structures.OBELISK.getStructure().createNew(location, true);
 						save.setOwner(character);
 						location.getWorld().strikeLightningEffect(location);
 
 						player.sendMessage(ChatColor.GRAY + Demigods.language.getText(Translation.Text.CREATE_OBELISK));
 					}
 					catch(Exception e)
 					{
 						// Creation of shrine failed...
 						e.printStackTrace();
 					}
					finally
					{
						event.setCancelled(true);
					}
 				}
 			}
 
 			if(Admins.useWand(player) && Structures.partOfStructureWithType(location, "Obelisk"))
 			{
 				event.setCancelled(true);
 
 				Structure.Save save = Structures.getStructureRegional(location);
 				// DCharacter owner = save.getOwner();
 
 				if(DataManager.hasTimed(player.getName(), "destroy_obelisk"))
 				{
 					// Remove the Obelisk
 					save.remove();
 					DataManager.removeTimed(player.getName(), "destroy_obelisk");
 
 					// Admins.sendDebug(ChatColor.RED + "Obelisk owned by (" + owner.getName() + ") at: " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ() + " removed.");
 
 					player.sendMessage(ChatColor.GREEN + Demigods.language.getText(Translation.Text.ADMIN_WAND_REMOVE_SHRINE_COMPLETE));
 				}
 				else
 				{
 					DataManager.saveTimed(player.getName(), "destroy_obelisk", true, 5);
 					player.sendMessage(ChatColor.RED + Demigods.language.getText(Translation.Text.ADMIN_WAND_REMOVE_SHRINE));
 				}
 			}
 		}
 	}
 }
