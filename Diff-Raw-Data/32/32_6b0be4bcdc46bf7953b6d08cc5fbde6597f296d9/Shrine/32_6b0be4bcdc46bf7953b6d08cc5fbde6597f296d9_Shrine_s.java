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
 import com.censoredsoftware.Demigods.Engine.Object.DPlayer;
 import com.censoredsoftware.Demigods.Engine.Object.Structure;
 import com.censoredsoftware.Demigods.Engine.Utility.AdminUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.DataUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.TextUtility;
 import com.censoredsoftware.Demigods.Episodes.Demo.EpisodeDemo;
 
 public class Shrine extends Structure
 {
 	private final static List<BlockData> clickBlock = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.GOLD_BLOCK));
 		}
 	};
 	private final static List<BlockData> enderChest = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.ENDER_CHEST));
 		}
 	};
 	private final static List<BlockData> stoneBrick = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.SMOOTH_BRICK));
 		}
 	};
 	private final static List<BlockData> stoneBrickStairs = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.SMOOTH_STAIRS));
 		}
 	};
 	private final static List<BlockData> stoneBrickStairs1 = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.SMOOTH_STAIRS, (byte) 1));
 		}
 	};
 	private final static List<BlockData> stoneBrickStairs2 = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.SMOOTH_STAIRS, (byte) 2));
 		}
 	};
 	private final static List<BlockData> stoneBrickStairs3 = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.SMOOTH_STAIRS, (byte) 3));
 		}
 	};
 	private final static Schematic general = new Schematic("general", "_Alex")
 	{
 		{
 			// Create the main block
 			add(new Cuboid(0, 1, 0, clickBlock));
 
 			// Create the ender chest and the block below
 			add(new Cuboid(0, 0, 0, enderChest));
 			add(new Cuboid(0, -1, 0, stoneBrick));
 
 			// Create the rest
 			add(new Cuboid(-1, 0, 0, stoneBrickStairs));
 			add(new Cuboid(1, 0, 0, stoneBrickStairs1));
 			add(new Cuboid(0, 0, -1, stoneBrickStairs2));
 			add(new Cuboid(0, 0, 1, stoneBrickStairs3));
 		}
 	};
 
 	@Override
 	public Set<Flag> getFlags()
 	{
 		return new HashSet<Flag>()
 		{
 			{
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
 	public Schematic get(String name)
 	{
 		return general;
 	}
 
 	@Override
 	public int getRadius()
 	{
 		return Demigods.config.getSettingInt("zones.shrine_radius");
 	}
 
 	@Override
 	public Location getClickableBlock(Location reference)
 	{
 		return reference.clone().add(0, 1, 0);
 	}
 
 	@Override
 	public Listener getUniqueListener()
 	{
 		return new ShrineListener();
 	}
 
 	@Override
 	public Set<Save> getAll()
 	{
 		return Util.findAll("type", getStructureType());
 	}
 
 	@Override
 	public Save createNew(Location reference, boolean generate)
 	{
 		Save save = new Save();
 		save.setReferenceLocation(reference);
 		save.setType(getStructureType());
 		save.setDesign("general");
 		save.addFlags(getFlags());
 		save.save();
 		if(generate) save.generate();
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
 		if(block.getRelative(1, 0, -1).getType().isSolid()) return false;
 		return !block.getRelative(-1, 0, 1).getType().isSolid() && !block.getRelative(-1, 0, -1).getType().isSolid();
 	}
 }
 
 class ShrineListener implements Listener
 {
 	@EventHandler(priority = EventPriority.HIGH)
 	public void createAndRemove(PlayerInteractEvent event)
 	{
 		if(event.getClickedBlock() == null) return;
 
 		// Define variables
 		Block clickedBlock = event.getClickedBlock();
 		Location location = clickedBlock.getLocation();
 		Player player = event.getPlayer();
 
 		if(DPlayer.Util.isImmortal(player))
 		{
 			DPlayer.Character character = DPlayer.Util.getPlayer(player).getCurrent();
 
 			if(event.getAction() == Action.RIGHT_CLICK_BLOCK && character.getDeity().getInfo().getClaimItems().contains(event.getPlayer().getItemInHand().getType()) && Shrine.validBlockConfiguration(event.getClickedBlock()))
 			{
 				try
 				{
 					// Shrine created!
 					AdminUtility.sendDebug(ChatColor.RED + "Shrine created by " + character.getName() + " (" + character.getDeity() + ") at: " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ());
 					Structure.Save save = EpisodeDemo.Structures.SHRINE.getStructure().createNew(location, true);
 					save.setOwner(character);
 					location.getWorld().strikeLightningEffect(location);
 
 					player.sendMessage(ChatColor.GRAY + Demigods.text.getText(TextUtility.Text.CREATE_SHRINE_1).replace("{alliance}", "" + ChatColor.YELLOW + character.getAlliance() + "s" + ChatColor.GRAY));
 					player.sendMessage(ChatColor.GRAY + Demigods.text.getText(TextUtility.Text.CREATE_SHRINE_2).replace("{deity}", "" + ChatColor.YELLOW + character.getDeity().getInfo().getName() + ChatColor.GRAY));
 				}
 				catch(Exception e)
 				{
 					// Creation of shrine failed...
 					e.printStackTrace();
 				}
 			}
 		}
 
 		if(AdminUtility.useWand(player) && Structure.Util.partOfStructureWithType(location, "Shrine"))
 		{
 			event.setCancelled(true);
 
 			Structure.Save save = Structure.Util.getStructureSave(location);
 			DPlayer.Character owner = save.getOwner();
 
 			if(DataUtility.hasTimed(player.getName(), "destroy_shrine"))
 			{
 				// Remove the Shrine
 				save.remove();
 				DataUtility.removeTimed(player.getName(), "destroy_shrine");
 
 				AdminUtility.sendDebug(ChatColor.RED + "Shrine of (" + owner.getDeity() + ") at: " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ() + " removed.");
 
 				player.sendMessage(ChatColor.GREEN + Demigods.text.getText(TextUtility.Text.ADMIN_WAND_REMOVE_SHRINE_COMPLETE));
 			}
 			else
 			{
 				DataUtility.saveTimed(player.getName(), "destroy_shrine", true, 5);
 				player.sendMessage(ChatColor.RED + Demigods.text.getText(TextUtility.Text.ADMIN_WAND_REMOVE_SHRINE));
 			}
 		}
 	}
 }
