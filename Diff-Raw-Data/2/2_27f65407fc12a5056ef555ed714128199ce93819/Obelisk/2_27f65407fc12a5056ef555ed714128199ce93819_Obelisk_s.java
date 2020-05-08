 package com.censoredsoftware.Demigods.Episodes.Demo.Structure;
 
 import java.util.ArrayList;
 import java.util.HashSet;
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
 import com.censoredsoftware.Demigods.Engine.Object.Structure.StructureBlockData;
 import com.censoredsoftware.Demigods.Engine.Object.Structure.StructureInfo;
 import com.censoredsoftware.Demigods.Engine.Object.Structure.StructureSave;
 import com.censoredsoftware.Demigods.Engine.Object.Structure.StructureSchematic;
 import com.censoredsoftware.Demigods.Engine.Utility.AdminUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.DataUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.StructureUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.TextUtility;
 import com.censoredsoftware.Demigods.Episodes.Demo.EpisodeDemo;
 
 public class Obelisk implements StructureInfo
 {
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
 	public Set<StructureSchematic> getSchematics()
 	{
 		return new HashSet<StructureSchematic>()
 		{
 			{
 				// Clickable block.
 				add(new StructureSchematic(0, 0, 2, new ArrayList<StructureBlockData>()
 				{
 					{
 						add(new StructureBlockData(Material.SMOOTH_BRICK, (byte) 3));
 					}
 				}));
 
 				// Everything else.
 				add(new StructureSchematic(0, 0, -1, 1, 3, 0, new ArrayList<StructureBlockData>()
 				{
 					{
 						add(new StructureBlockData(Material.SMOOTH_BRICK));
 					}
 				}));
 				add(new StructureSchematic(0, 0, 1, 1, 3, 1, new ArrayList<StructureBlockData>()
 				{
 					{
 						add(new StructureBlockData(Material.SMOOTH_BRICK));
 					}
 				}));
 				add(new StructureSchematic(1, 0, 0, 1, 3, 1, new ArrayList<StructureBlockData>()
 				{
 					{
 						add(new StructureBlockData(Material.SMOOTH_BRICK));
 					}
 				}));
 				add(new StructureSchematic(-1, 0, 0, 0, 3, 1, new ArrayList<StructureBlockData>()
 				{
 					{
 						add(new StructureBlockData(Material.SMOOTH_BRICK));
 					}
 				}));
 			}
 		};
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
 		save.save();
 		if(generate) save.generate();
 		return save;
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
 
 		if(AdminUtility.useWand(player) && StructureUtility.partOfStructureWithType(location, "Obelisk"))
 		{
 			event.setCancelled(true);
 
 			StructureSave save = StructureUtility.getStructure(location);
 			PlayerCharacter owner = save.getOwner();
 
 			if(DataUtility.hasTimed(player.getName(), "destroy_obelisk"))
 			{
 				// Remove the Obelisk
 				save.remove();
 				DataUtility.removeTimed(player.getName(), "destroy_obelisk");
 
				AdminUtility.sendDebug(ChatColor.RED + "Obelisk owned by (" + owner.getName() + ") at: " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ() + " removed.");
 
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
