 package com.censoredsoftware.Demigods.Episodes.Demo.Structure;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerCharacter;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerWrapper;
 import com.censoredsoftware.Demigods.Engine.Object.Structure.*;
 import com.censoredsoftware.Demigods.Engine.Utility.AdminUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.DataUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.TextUtility;
import com.censoredsoftware.Demigods.Episodes.Demo.EpisodeDemo;
 
 public class Obelisk extends Structure
 {
 	final static List<StructureBlockData> specialStoneBrick = new ArrayList<StructureBlockData>()
 	{
 		{
 			add(new StructureBlockData(Material.SMOOTH_BRICK, (byte) 3));
 		}
 	};
 	final static List<StructureBlockData> specialSandstone = new ArrayList<StructureBlockData>()
 	{
 		{
 			add(new StructureBlockData(Material.SANDSTONE, (byte) 1));
 		}
 	};
 	final static List<StructureBlockData> sandstone = new ArrayList<StructureBlockData>()
 	{
 		{
 			add(new StructureBlockData(Material.SANDSTONE));
 		}
 	};
 	final static List<StructureBlockData> stoneBrick = new ArrayList<StructureBlockData>()
 	{
 		{
 			add(new StructureBlockData(Material.SMOOTH_BRICK, 9));
 			add(new StructureBlockData(Material.SMOOTH_BRICK, (byte) 2, 1));
 		}
 	};
 	final static List<StructureBlockData> redstoneBlock = new ArrayList<StructureBlockData>()
 	{
 		{
 			add(new StructureBlockData(Material.REDSTONE_BLOCK));
 		}
 	};
 	final static List<StructureBlockData> redstoneLamp = new ArrayList<StructureBlockData>()
 	{
 		{
 			add(new StructureBlockData(Material.REDSTONE_LAMP_ON));
 		}
 	};
 	final static List<StructureBlockData> vine1 = new ArrayList<StructureBlockData>()
 	{
 		{
 			add(new StructureBlockData(Material.VINE, (byte) 1, 4));
 			add(new StructureBlockData(Material.AIR, 6));
 		}
 	};
 	final static List<StructureBlockData> vine4 = new ArrayList<StructureBlockData>()
 	{
 		{
 			add(new StructureBlockData(Material.VINE, (byte) 4, 4));
 			add(new StructureBlockData(Material.AIR, 6));
 		}
 	};
 	final static StructureSchematic general = new StructureSchematic("general", "HmmmQuestionMark")
 	{
 		{
 			// Clickable block.
 			add(new StructureCuboid(0, 0, 2, specialStoneBrick));
 
 			// Everything else.
 			add(new StructureCuboid(0, 0, -1, 0, 2, -1, stoneBrick));
 			add(new StructureCuboid(0, 0, 1, 0, 2, 1, stoneBrick));
 			add(new StructureCuboid(1, 0, 0, 1, 2, 0, stoneBrick));
 			add(new StructureCuboid(-1, 0, 0, -1, 2, 0, stoneBrick));
 			add(new StructureCuboid(0, 4, -1, 0, 5, -1, stoneBrick));
 			add(new StructureCuboid(0, 4, 1, 0, 5, 1, stoneBrick));
 			add(new StructureCuboid(1, 4, 0, 1, 5, 0, stoneBrick));
 			add(new StructureCuboid(-1, 4, 0, -1, 5, 0, stoneBrick));
 			add(new StructureCuboid(0, 3, 0, redstoneBlock));
 			add(new StructureCuboid(0, 4, 0, redstoneBlock));
 			add(new StructureCuboid(0, 3, -1, redstoneLamp));
 			add(new StructureCuboid(0, 3, 1, redstoneLamp));
 			add(new StructureCuboid(1, 3, 0, redstoneLamp));
 			add(new StructureCuboid(-1, 3, 0, redstoneLamp));
 			add(new StructureCuboid(0, 5, 0, redstoneLamp));
 			add(new StructureCuboid(1, 5, -1, vine1));
 			add(new StructureCuboid(-1, 5, -1, vine1));
 			add(new StructureCuboid(1, 5, 1, vine4));
 			add(new StructureCuboid(-1, 5, 1, vine4));
 		}
 	};
 	final static StructureSchematic desert = new StructureSchematic("desert", "HmmmQuestionMark")
 	{
 		{
 			// Clickable block.
 			add(new StructureCuboid(0, 0, 2, specialSandstone));
 
 			// Everything else.
 			add(new StructureCuboid(0, 0, -1, 0, 2, -1, sandstone));
 			add(new StructureCuboid(0, 0, 1, 0, 2, 1, sandstone));
 			add(new StructureCuboid(1, 0, 0, 1, 2, 0, sandstone));
 			add(new StructureCuboid(-1, 0, 0, -1, 2, 0, sandstone));
 			add(new StructureCuboid(0, 4, -1, 0, 5, -1, sandstone));
 			add(new StructureCuboid(0, 4, 1, 0, 5, 1, sandstone));
 			add(new StructureCuboid(1, 4, 0, 1, 5, 0, sandstone));
 			add(new StructureCuboid(-1, 4, 0, -1, 5, 0, sandstone));
 			add(new StructureCuboid(0, 3, 0, redstoneBlock));
 			add(new StructureCuboid(0, 4, 0, redstoneBlock));
 			add(new StructureCuboid(0, 3, -1, redstoneLamp));
 			add(new StructureCuboid(0, 3, 1, redstoneLamp));
 			add(new StructureCuboid(1, 3, 0, redstoneLamp));
 			add(new StructureCuboid(-1, 3, 0, redstoneLamp));
 			add(new StructureCuboid(0, 5, 0, redstoneLamp));
 		}
 	};
 	final static List<StructureSchematic> obelisk = new ArrayList<StructureSchematic>()
 	{
 		{
 			add(general);
 			add(desert);
 		}
 	};
 
 	public static enum ObeliskDesign implements StructureSchematic.StructureDesign
 	{
 		GENERAL(0, "general"), DESERT(1, "desert");
 
 		private int index;
 		private String name;
 
 		private ObeliskDesign(int index, String name)
 		{
 			this.index = index;
 			this.name = name;
 		}
 
 		@Override
 		public int getIndex()
 		{
 			return index;
 		}
 
 		@Override
 		public String getName()
 		{
 			return name;
 		}
 	}
 
 	@Override
 	public Set<Flag> getFlags()
 	{
 		return new HashSet<Flag>()
 		{
 			{
 				add(Flag.HAS_OWNER);
 				add(Flag.NO_GRIEFING_ZONE);
 				add(Flag.PROTECTED_BLOCKS);
 			}
 		};
 	}
 
 	@Override
 	public String getStructureType()
 	{
 		return "Obelisk";
 	}
 
 	@Override
 	public List<StructureSchematic> getSchematics()
 	{
 		return obelisk;
 	}
 
 	@Override
 	public int getRadius()
 	{
 		return Demigods.config.getSettingInt("zones.obelisk_radius");
 	}
 
 	@Override
 	public Location getClickableBlock(Location reference)
 	{
 		return reference.clone().add(0, 0, 2);
 	}
 
 	@Override
 	public Listener getUniqueListener()
 	{
 		return new ObeliskListener();
 	}
 
 	@Override
 	public Set<StructureSave> getAll()
 	{
 		return new HashSet<StructureSave>()
 		{
 			{
 				for(StructureSave saved : StructureSave.loadAll())
 				{
 					if(saved.getStructureInfo().getStructureType().equals(getStructureType())) add(saved);
 				}
 			}
 		};
 	}
 
 	@Override
 	public StructureSave createNew(Location reference, boolean generate)
 	{
 		StructureSave save = new StructureSave();
 		save.setReferenceLocation(reference);
 		save.setStructureType(getStructureType());
 		save.setStructureDesign(getDesign(reference));
 		save.save();
 		if(generate) save.generate();
 		return save;
 	}
 
 	public int getDesign(Location reference)
 	{
 		switch(reference.getBlock().getBiome())
 		{
 			case BEACH:
 			case DESERT:
 			case DESERT_HILLS:
 				return ObeliskDesign.DESERT.getIndex();
 			default:
 				return ObeliskDesign.GENERAL.getIndex();
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
 		if(block.getRelative(1, 0, -1).getType().isSolid()) return false;
 		return !block.getRelative(-1, 0, 1).getType().isSolid() && !block.getRelative(-1, 0, -1).getType().isSolid();
 	}
 
 	public static boolean noPvPStructureNearby(Location location)
 	{
 		for(StructureSave structureSave : StructureSave.loadAll())
 		{
 			if(structureSave.getStructureInfo().getFlags().contains(Flag.NO_PVP_ZONE) && structureSave.getReferenceLocation().distance(location) <= (Demigods.config.getSettingInt("altar_radius") + Demigods.config.getSettingInt("obelisk_radius") + 6)) return true;
 		}
 		return false;
 	}
 }
 
 class ObeliskListener implements Listener
 {
 	@EventHandler(priority = EventPriority.HIGH)
 	public void createAndRemove(PlayerInteractEvent event)
 	{
 		if(event.getClickedBlock() == null) return;
 
 		// Define variables
 		Block clickedBlock = event.getClickedBlock();
 		Location location = clickedBlock.getLocation();
 		Player player = event.getPlayer();
 
 		if(PlayerWrapper.isImmortal(player))
 		{
 			PlayerCharacter character = PlayerWrapper.getPlayer(player).getCurrent();
 
 			if(event.getAction() == Action.RIGHT_CLICK_BLOCK && character.getDeity().getInfo().getClaimItems().contains(event.getPlayer().getItemInHand().getType()) && Obelisk.validBlockConfiguration(event.getClickedBlock()))
 			{
 				if(Obelisk.noPvPStructureNearby(location))
 				{
 					player.sendMessage(ChatColor.YELLOW + "This location is too close to a no-pvp zone, please try again.");
 					return;
 				}
 
 				try
 				{
 					// Obelisk created!
 					AdminUtility.sendDebug(ChatColor.RED + "Obelisk created by " + character.getName() + " at: " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ());
					StructureSave save = EpisodeDemo.Structures.OBELISK.getStructure().createNew(location, true);
					save.setOwner(character);
 					location.getWorld().strikeLightningEffect(location);
 
 					player.sendMessage(ChatColor.GRAY + Demigods.text.getText(TextUtility.Text.CREATE_OBELISK));
 				}
 				catch(Exception e)
 				{
 					// Creation of shrine failed...
 					e.printStackTrace();
 				}
 			}
 		}
 
 		if(AdminUtility.useWand(player) && Structure.partOfStructureWithType(location, "Obelisk"))
 		{
 			event.setCancelled(true);
 
 			StructureSave save = Structure.getStructure(location);
 			PlayerCharacter owner = save.getOwner();
 
 			if(DataUtility.hasTimed(player.getName(), "destroy_obelisk"))
 			{
 				// Remove the Obelisk
 				save.remove();
 				DataUtility.removeTimed(player.getName(), "destroy_obelisk");
 
 				// AdminUtility.sendDebug(ChatColor.RED + "Obelisk owned by (" + owner.getName() + ") at: " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ() + " removed.");
 
 				player.sendMessage(ChatColor.GREEN + Demigods.text.getText(TextUtility.Text.ADMIN_WAND_REMOVE_SHRINE_COMPLETE));
 			}
 			else
 			{
 				DataUtility.saveTimed(player.getName(), "destroy_obelisk", true, 5);
 				player.sendMessage(ChatColor.RED + Demigods.text.getText(TextUtility.Text.ADMIN_WAND_REMOVE_SHRINE));
 			}
 		}
 	}
 }
