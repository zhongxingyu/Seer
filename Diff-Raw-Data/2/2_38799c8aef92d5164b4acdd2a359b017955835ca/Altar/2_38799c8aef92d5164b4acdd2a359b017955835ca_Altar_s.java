 package com.censoredsoftware.Demigods.Episodes.Demo.Structure;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.world.ChunkLoadEvent;
 
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Object.Structure;
 import com.censoredsoftware.Demigods.Engine.Utility.*;
 import com.censoredsoftware.Demigods.Episodes.Demo.EpisodeDemo;
 
 public class Altar extends Structure
 {
 	private final static List<BlockData> enchantTable = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.ENCHANTMENT_TABLE));
 		}
 	};
 	private final static List<BlockData> stoneBrick = new ArrayList<BlockData>(3)
 	{
 		{
 			add(new BlockData(Material.SMOOTH_BRICK, 8));
 			add(new BlockData(Material.SMOOTH_BRICK, (byte) 1, 1));
 			add(new BlockData(Material.SMOOTH_BRICK, (byte) 2, 1));
 		}
 	};
 	private final static List<BlockData> quartz = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.QUARTZ_BLOCK));
 		}
 	};
 	private final static List<BlockData> pillarQuartz = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.QUARTZ_BLOCK, (byte) 2));
 		}
 	};
 	private final static List<BlockData> stoneBrickSlabBottom = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.getMaterial(44), (byte) 5));
 		}
 	};
 	private final static List<BlockData> stoneBrickSlabTop = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.getMaterial(44), (byte) 13));
 		}
 	};
 	private final static List<BlockData> quartzSlabBottom = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.getMaterial(44), (byte) 7));
 		}
 	};
 	private final static List<BlockData> quartzSlabTop = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.getMaterial(44), (byte) 15));
 		}
 	};
 	private final static List<BlockData> stoneBrickSpecial = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.getMaterial(98), (byte) 3));
 		}
 	};
 	private final static List<BlockData> quartzSpecial = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.QUARTZ_BLOCK, (byte) 1));
 		}
 	};
 	private final static List<BlockData> spruceWood = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.getMaterial(5), (byte) 1));
 		}
 	};
 	private final static List<BlockData> spruceSlab = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.getMaterial(126), (byte) 1));
 		}
 	};
 	private final static List<BlockData> birchWood = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.getMaterial(5), (byte) 2));
 		}
 	};
 	private final static List<BlockData> birchSlab = new ArrayList<BlockData>(1)
 	{
 		{
 			add(new BlockData(Material.getMaterial(126), (byte) 2));
 		}
 	};
 	private final static Schematic general = new Schematic("general", "_Alex")
 	{
 		{
 			// Create roof
 			add(new Cuboid(2, 3, 2, stoneBrickSlabTop));
 			add(new Cuboid(-2, 3, -2, stoneBrickSlabTop));
 			add(new Cuboid(2, 3, -2, stoneBrickSlabTop));
 			add(new Cuboid(-2, 3, 2, stoneBrickSlabTop));
 			add(new Cuboid(2, 4, 2, stoneBrick));
 			add(new Cuboid(-2, 4, -2, stoneBrick));
 			add(new Cuboid(2, 4, -2, stoneBrick));
 			add(new Cuboid(-2, 4, 2, stoneBrick));
 			add(new Cuboid(2, 5, 2, spruceSlab));
 			add(new Cuboid(-2, 5, -2, spruceSlab));
 			add(new Cuboid(2, 5, -2, spruceSlab));
 			add(new Cuboid(-2, 5, 2, spruceSlab));
 			add(new Cuboid(0, 6, 0, spruceSlab));
 			add(new Cuboid(-1, 5, -1, 1, 5, 1, spruceWood));
 
 			// Create the enchantment table
 			add(new Cuboid(0, 2, 0, enchantTable));
 
 			// Create magical table stand
 			add(new Cuboid(0, 1, 0, stoneBrick));
 
 			// Create outer steps
 			add(new Cuboid(3, 0, 3, stoneBrickSlabBottom));
 			add(new Cuboid(-3, 0, -3, stoneBrickSlabBottom));
 			add(new Cuboid(3, 0, -3, stoneBrickSlabBottom));
 			add(new Cuboid(-3, 0, 3, stoneBrickSlabBottom));
 			add(new Cuboid(4, 0, -2, 4, 0, 2, stoneBrickSlabBottom));
 			add(new Cuboid(-4, 0, -2, -4, 0, 2, stoneBrickSlabBottom));
 			add(new Cuboid(-2, 0, -4, 2, 0, -4, stoneBrickSlabBottom));
 			add(new Cuboid(-2, 0, 4, 2, 0, 4, stoneBrickSlabBottom));
 
 			// Create inner steps
 			add(new Cuboid(3, 0, -1, 3, 0, 1, stoneBrick));
 			add(new Cuboid(-1, 0, 3, 1, 0, 3, stoneBrick));
 			add(new Cuboid(-3, 0, -1, -3, 0, 1, stoneBrick));
 			add(new Cuboid(-1, 0, -3, 1, 0, -3, stoneBrick));
 
 			// Create pillars
 			add(new Cuboid(3, 4, 2, spruceSlab));
 			add(new Cuboid(3, 4, -2, spruceSlab));
 			add(new Cuboid(2, 4, 3, spruceSlab));
 			add(new Cuboid(-2, 4, 3, spruceSlab));
 			add(new Cuboid(-3, 4, 2, spruceSlab));
 			add(new Cuboid(-3, 4, -2, spruceSlab));
 			add(new Cuboid(2, 4, -3, spruceSlab));
 			add(new Cuboid(-2, 4, -3, spruceSlab));
 			add(new Cuboid(3, 0, 2, 3, 3, 2, stoneBrick));
 			add(new Cuboid(3, 0, -2, 3, 3, -2, stoneBrick));
 			add(new Cuboid(2, 0, 3, 2, 3, 3, stoneBrick));
 			add(new Cuboid(-2, 0, 3, -2, 3, 3, stoneBrick));
 			add(new Cuboid(-3, 0, 2, -3, 3, 2, stoneBrick));
 			add(new Cuboid(-3, 0, -2, -3, 3, -2, stoneBrick));
 			add(new Cuboid(2, 0, -3, 2, 3, -3, stoneBrick));
 			add(new Cuboid(-2, 0, -3, -2, 3, -3, stoneBrick));
 
 			// Left beam
 			add(new Cuboid(1, 4, -2, -1, 4, -2, stoneBrick).exclude(0, 4, -2));
 			add(new Cuboid(0, 4, -2, stoneBrickSpecial));
 			add(new Cuboid(-1, 5, -2, 1, 5, -2, spruceSlab));
 
 			// Right beam
 			add(new Cuboid(1, 4, 2, -1, 4, 2, stoneBrick).exclude(0, 4, 2));
 			add(new Cuboid(0, 4, 2, stoneBrickSpecial));
 			add(new Cuboid(-1, 5, 2, 1, 5, 2, spruceSlab));
 
 			// Top beam
 			add(new Cuboid(2, 4, 1, 2, 4, -1, stoneBrick).exclude(2, 4, 0));
 			add(new Cuboid(2, 4, 0, stoneBrickSpecial));
 			add(new Cuboid(2, 5, -1, 2, 5, 1, spruceSlab));
 
 			// Bottom beam
 			add(new Cuboid(-2, 4, 1, -2, 4, -1, stoneBrick).exclude(-2, 4, 0));
 			add(new Cuboid(-2, 4, 0, stoneBrickSpecial));
 			add(new Cuboid(-2, 5, -1, -2, 5, 1, spruceSlab));
 
 			// Create main platform
 			add(new Cuboid(0, 1, 0, stoneBrick));
 			add(new Cuboid(-2, 1, -2, 2, 1, 2, stoneBrickSlabBottom).exclude(0, 1, 0));
 		}
 	};
 	private final static Schematic holy = new Schematic("holy", "HmmmQuestionMark")
 	{
 		{
 			// Create roof
 			add(new Cuboid(2, 3, 2, quartzSlabTop));
 			add(new Cuboid(-2, 3, -2, quartzSlabTop));
 			add(new Cuboid(2, 3, -2, quartzSlabTop));
 			add(new Cuboid(-2, 3, 2, quartzSlabTop));
 			add(new Cuboid(2, 4, 2, quartz));
 			add(new Cuboid(-2, 4, -2, quartz));
 			add(new Cuboid(2, 4, -2, quartz));
 			add(new Cuboid(-2, 4, 2, quartz));
 			add(new Cuboid(2, 5, 2, birchSlab));
 			add(new Cuboid(-2, 5, -2, birchSlab));
 			add(new Cuboid(2, 5, -2, birchSlab));
 			add(new Cuboid(-2, 5, 2, birchSlab));
 			add(new Cuboid(0, 6, 0, birchSlab));
 			add(new Cuboid(-1, 5, -1, 1, 5, 1, birchWood));
 
 			// Create the enchantment table
 			add(new Cuboid(0, 2, 0, enchantTable));
 
 			// Create magical table stand
 			add(new Cuboid(0, 1, 0, quartzSpecial));
 
 			// Create outer steps
 			add(new Cuboid(3, 0, 3, quartzSlabBottom));
 			add(new Cuboid(-3, 0, -3, quartzSlabBottom));
 			add(new Cuboid(3, 0, -3, quartzSlabBottom));
 			add(new Cuboid(-3, 0, 3, quartzSlabBottom));
 			add(new Cuboid(4, 0, -2, 4, 0, 2, quartzSlabBottom));
 			add(new Cuboid(-4, 0, -2, -4, 0, 2, quartzSlabBottom));
 			add(new Cuboid(-2, 0, -4, 2, 0, -4, quartzSlabBottom));
 			add(new Cuboid(-2, 0, 4, 2, 0, 4, quartzSlabBottom));
 
 			// Create inner steps
 			add(new Cuboid(3, 0, -1, 3, 0, 1, quartz).exclude(3, 0, 0));
 			add(new Cuboid(-1, 0, 3, 1, 0, 3, quartz).exclude(0, 0, 3));
 			add(new Cuboid(-3, 0, -1, -3, 0, 1, quartz).exclude(-3, 0, 0));
 			add(new Cuboid(-1, 0, -3, 1, 0, -3, quartz).exclude(0, 0, -3));
 			add(new Cuboid(3, 0, 0, quartzSpecial));
 			add(new Cuboid(0, 0, 3, quartzSpecial));
 			add(new Cuboid(-3, 0, 0, quartzSpecial));
 			add(new Cuboid(0, 0, -3, quartzSpecial));
 
 			// Create pillars
 			add(new Cuboid(3, 4, 2, birchSlab));
 			add(new Cuboid(3, 4, -2, birchSlab));
 			add(new Cuboid(2, 4, 3, birchSlab));
 			add(new Cuboid(-2, 4, 3, birchSlab));
 			add(new Cuboid(-3, 4, 2, birchSlab));
 			add(new Cuboid(-3, 4, -2, birchSlab));
 			add(new Cuboid(2, 4, -3, birchSlab));
 			add(new Cuboid(-2, 4, -3, birchSlab));
 			add(new Cuboid(3, 0, 2, 3, 3, 2, pillarQuartz));
 			add(new Cuboid(3, 0, -2, 3, 3, -2, pillarQuartz));
 			add(new Cuboid(2, 0, 3, 2, 3, 3, pillarQuartz));
 			add(new Cuboid(-2, 0, 3, -2, 3, 3, pillarQuartz));
 			add(new Cuboid(-3, 0, 2, -3, 3, 2, pillarQuartz));
 			add(new Cuboid(-3, 0, -2, -3, 3, -2, pillarQuartz));
 			add(new Cuboid(2, 0, -3, 2, 3, -3, pillarQuartz));
 			add(new Cuboid(-2, 0, -3, -2, 3, -3, pillarQuartz));
 
 			// Left beam
 			add(new Cuboid(1, 4, -2, -1, 4, -2, quartz).exclude(0, 4, -2));
 			add(new Cuboid(0, 4, -2, quartzSpecial));
 			add(new Cuboid(-1, 5, -2, 1, 5, -2, birchSlab));
 
 			// Right beam
 			add(new Cuboid(1, 4, 2, -1, 4, 2, quartz).exclude(0, 4, 2));
 			add(new Cuboid(0, 4, 2, quartzSpecial));
 			add(new Cuboid(-1, 5, 2, 1, 5, 2, birchSlab));
 
 			// Top beam
 			add(new Cuboid(2, 4, 1, 2, 4, -1, quartz).exclude(2, 4, 0));
 			add(new Cuboid(2, 4, 0, quartzSpecial));
 			add(new Cuboid(2, 5, -1, 2, 5, 1, birchSlab));
 
 			// Bottom beam
 			add(new Cuboid(-2, 4, 1, -2, 4, -1, quartz).exclude(-2, 4, 0));
 			add(new Cuboid(-2, 4, 0, quartzSpecial));
 			add(new Cuboid(-2, 5, -1, -2, 5, 1, birchSlab));
 
 			// Create main platform
 			add(new Cuboid(0, 1, 0, quartz));
 			add(new Cuboid(-2, 1, -2, 2, 1, 2, quartzSlabBottom).exclude(0, 1, 0));
 		}
 	};
 
 	public static enum AltarDesign implements Design
 	{
 		GENERAL("general"), HOLY("holy");
 
 		private String name;
 
 		private AltarDesign(String name)
 		{
 			this.name = name;
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
 				add(Flag.NO_PVP);
 				add(Flag.PRAYER_LOCATION);
 				add(Flag.PROTECTED_BLOCKS);
 			}
 		};
 	}
 
 	@Override
 	public String getStructureType()
 	{
 		return "Altar";
 	}
 
 	@Override
 	public Schematic get(String name)
 	{
 		if(name.equals(general.toString())) return general;
 		return holy;
 	}
 
 	@Override
 	public int getRadius()
 	{
 		return Demigods.config.getSettingInt("zones.altar_radius");
 	}
 
 	@Override
 	public Location getClickableBlock(Location reference)
 	{
 		return reference.clone().add(0, 2, 0);
 	}
 
 	@Override
 	public Listener getUniqueListener()
 	{
 		return new AltarListener();
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
 		save.setDesign(getDesign(reference));
 		save.addFlags(getFlags());
 		save.save();
 		if(generate) save.generate();
 		return save;
 	}
 
 	public String getDesign(Location reference)
 	{
 		switch(reference.getBlock().getBiome())
 		{
 			case ICE_PLAINS:
 				return AltarDesign.HOLY.getName();
 			default:
 				return AltarDesign.GENERAL.getName();
 		}
 	}
 
 	public static boolean altarNearby(Location location)
 	{
 		for(Save structureSave : Util.findAll("type", "Altar"))
 		{
 			if(structureSave.getReferenceLocation().distance(location) <= Demigods.config.getSettingInt("generation.min_blocks_between_altars")) return true;
 		}
 		return false;
 	}
 }
 
 class AltarListener implements Listener
 {
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onChunkLoad(final ChunkLoadEvent event)
 	{
 		if(!event.isNewChunk()) return;
 
 		// Define variables
 		final Location location = LocationUtility.randomChunkLocation(event.getChunk());
 
 		// Check if it can generate
 		if(LocationUtility.canGenerateStrict(location, 3))
 		{
 			// Return a random boolean based on the chance of Altar generation
 			if(MiscUtility.randomPercentBool(Demigods.config.getSettingDouble("generation.altar_chance")))
 			{
 				// If another Altar doesn't exist nearby then make one
 				if(!Altar.altarNearby(location))
 				{
 					AdminUtility.sendDebug(ChatColor.RED + "Altar generated by SERVER at " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ());
 
 					EpisodeDemo.Structures.ALTAR.getStructure().createNew(location, true);
 
 					location.getWorld().strikeLightningEffect(location);
 					location.getWorld().strikeLightningEffect(location);
 
 					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Demigods.plugin, new Runnable()
 					{
 						@Override
 						public void run()
 						{
 							for(Entity entity : event.getWorld().getEntities())
 							{
 								if(entity instanceof Player)
 								{
 									if(entity.getLocation().distance(location) < 400)
 									{
 										((Player) entity).sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + Demigods.text.getText(TextUtility.Text.ALTAR_SPAWNED_NEAR));
 									}
 								}
 							}
 						}
 					}, 1);
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void demigodsAdminWand(PlayerInteractEvent event)
 	{
 		if(event.getClickedBlock() == null) return;
 
 		// Define variables
 		Block clickedBlock = event.getClickedBlock();
 		Location location = clickedBlock.getLocation();
 		Player player = event.getPlayer();
 
 		/**
 		 * Handle Altars
 		 */
 		boolean general = clickedBlock.getType().equals(Material.EMERALD_BLOCK);
 		boolean holy = clickedBlock.getType().equals(Material.COAL_BLOCK);
		if(AdminUtility.useWand(player) && general || holy)
 		{
 			event.setCancelled(true);
 
 			// Remove clicked block
 			clickedBlock.setType(Material.AIR);
 
 			AdminUtility.sendDebug(ChatColor.RED + "Altar generated by ADMIN WAND at " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ());
 
 			player.sendMessage(ChatColor.GRAY + Demigods.text.getText(TextUtility.Text.ADMIN_WAND_GENERATE_ALTAR));
 
 			// Generate the Altar based on the block given.
 			Structure.Save save = EpisodeDemo.Structures.ALTAR.getStructure().createNew(location, false);
 			if(holy) save.setDesign("holy");
 			if(general) save.setDesign("general");
 			save.generate();
 
 			player.sendMessage(ChatColor.GREEN + Demigods.text.getText(TextUtility.Text.ADMIN_WAND_GENERATE_ALTAR_COMPLETE));
 			return;
 		}
 
 		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && AdminUtility.useWand(player) && Structure.Util.partOfStructureWithType(location, "Altar"))
 		{
 			event.setCancelled(true);
 
 			Structure.Save altar = Structure.Util.getStructureSave(location);
 
 			if(DataUtility.hasTimed(player.getName(), "destroy_altar"))
 			{
 				AdminUtility.sendDebug(ChatColor.RED + "Altar at " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ() + " removed by " + "ADMIN WAND" + ".");
 
 				// Remove the Altar
 
 				altar.remove();
 
 				DataUtility.removeTimed(player.getName(), "destroy_altar");
 
 				player.sendMessage(ChatColor.GREEN + Demigods.text.getText(TextUtility.Text.ADMIN_WAND_REMOVE_ALTAR_COMPLETE));
 			}
 			else
 			{
 				DataUtility.saveTimed(player.getName(), "destroy_altar", true, 5);
 				player.sendMessage(ChatColor.RED + Demigods.text.getText(TextUtility.Text.ADMIN_WAND_REMOVE_ALTAR));
 			}
 		}
 	}
 }
