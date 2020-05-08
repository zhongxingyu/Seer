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
 import com.censoredsoftware.Demigods.Engine.Object.Structure.*;
 import com.censoredsoftware.Demigods.Engine.Utility.*;
 
 public class Altar extends Structure
 {
 	final static List<StructureBlockData> clickBlock = new ArrayList<StructureBlockData>()
 	{
 		{
 			add(new StructureBlockData(Material.ENCHANTMENT_TABLE));
 		}
 	};
 	final static List<StructureBlockData> stoneBrick = new ArrayList<StructureBlockData>()
 	{
 		{
 			add(new StructureBlockData(Material.SMOOTH_BRICK, 8));
 			add(new StructureBlockData(Material.SMOOTH_BRICK, (byte) 1, 1));
 			add(new StructureBlockData(Material.SMOOTH_BRICK, (byte) 2, 1));
 		}
 	};
 	final static List<StructureBlockData> stoneBrickSlabBottom = new ArrayList<StructureBlockData>()
 	{
 		{
 			add(new StructureBlockData(Material.getMaterial(44), (byte) 5));
 		}
 	};
 	final static List<StructureBlockData> stoneBrickSlabTop = new ArrayList<StructureBlockData>()
 	{
 		{
 			add(new StructureBlockData(Material.getMaterial(44), (byte) 13));
 		}
 	};
 	final static List<StructureBlockData> stoneBrickSpecial = new ArrayList<StructureBlockData>()
 	{
 		{
 			add(new StructureBlockData(Material.getMaterial(98), (byte) 3));
 		}
 	};
 	final static List<StructureBlockData> wood = new ArrayList<StructureBlockData>()
 	{
 		{
 			add(new StructureBlockData(Material.getMaterial(5), (byte) 1));
 		}
 	};
 	final static List<StructureBlockData> woodSlab = new ArrayList<StructureBlockData>()
 	{
 		{
 			add(new StructureBlockData(Material.getMaterial(126), (byte) 1));
 		}
 	};
 	final static StructureSchematic general = new StructureSchematic("general", "_Alex")
 	{
 		{
 			// Create roof
 			add(new StructureCuboid(2, 3, 2, stoneBrickSlabTop));
 			add(new StructureCuboid(-2, 3, -2, stoneBrickSlabTop));
 			add(new StructureCuboid(2, 3, -2, stoneBrickSlabTop));
 			add(new StructureCuboid(-2, 3, 2, stoneBrickSlabTop));
 			add(new StructureCuboid(2, 4, 2, stoneBrick));
 			add(new StructureCuboid(-2, 4, -2, stoneBrick));
 			add(new StructureCuboid(2, 4, -2, stoneBrick));
 			add(new StructureCuboid(-2, 4, 2, stoneBrick));
 			add(new StructureCuboid(2, 5, 2, woodSlab));
 			add(new StructureCuboid(-2, 5, -2, woodSlab));
 			add(new StructureCuboid(2, 5, -2, woodSlab));
 			add(new StructureCuboid(-2, 5, 2, woodSlab));
 			add(new StructureCuboid(0, 6, 0, woodSlab));
 			add(new StructureCuboid(-1, 5, -1, 1, 5, 1, wood));
 
 			// Create the enchantment table
 			add(new StructureCuboid(0, 2, 0, clickBlock));
 
 			// Create magical table stand
 			add(new StructureCuboid(0, 1, 0, stoneBrick));
 
 			// Create outer steps
 			add(new StructureCuboid(3, 0, 3, stoneBrickSlabBottom));
 			add(new StructureCuboid(-3, 0, -3, stoneBrickSlabBottom));
 			add(new StructureCuboid(3, 0, -3, stoneBrickSlabBottom));
 			add(new StructureCuboid(-3, 0, 3, stoneBrickSlabBottom));
 			add(new StructureCuboid(4, 0, -2, 4, 0, 2, stoneBrickSlabBottom));
 			add(new StructureCuboid(-4, 0, -2, -4, 0, 2, stoneBrickSlabBottom));
 			add(new StructureCuboid(-2, 0, -4, 2, 0, -4, stoneBrickSlabBottom));
 			add(new StructureCuboid(-2, 0, 4, 2, 0, 4, stoneBrickSlabBottom));
 
 			// Create inner steps
 			add(new StructureCuboid(3, 0, -1, 3, 0, 1, stoneBrick));
 			add(new StructureCuboid(-1, 0, 3, 1, 0, 3, stoneBrick));
 			add(new StructureCuboid(-3, 0, -1, -3, 0, 1, stoneBrick));
 			add(new StructureCuboid(-1, 0, -3, 1, 0, -3, stoneBrick));
 
 			// Create pillars
 			add(new StructureCuboid(3, 4, 2, woodSlab));
 			add(new StructureCuboid(3, 4, -2, woodSlab));
 			add(new StructureCuboid(2, 4, 3, woodSlab));
 			add(new StructureCuboid(-2, 4, 3, woodSlab));
 			add(new StructureCuboid(-3, 4, 2, woodSlab));
 			add(new StructureCuboid(-3, 4, -2, woodSlab));
 			add(new StructureCuboid(2, 4, -3, woodSlab));
 			add(new StructureCuboid(-2, 4, -3, woodSlab));
 			add(new StructureCuboid(3, 0, 2, 3, 3, 2, stoneBrick));
 			add(new StructureCuboid(3, 0, -2, 3, 3, -2, stoneBrick));
 			add(new StructureCuboid(2, 0, 3, 2, 3, 3, stoneBrick));
 			add(new StructureCuboid(-2, 0, 3, -2, 3, 3, stoneBrick));
 			add(new StructureCuboid(-3, 0, 2, -3, 3, 2, stoneBrick));
 			add(new StructureCuboid(-3, 0, -2, -3, 3, -2, stoneBrick));
 			add(new StructureCuboid(2, 0, -3, 2, 3, -3, stoneBrick));
 			add(new StructureCuboid(-2, 0, -3, -2, 3, -3, stoneBrick));
 
 			// Left beam
 			add(new StructureCuboid(1, 4, -2, stoneBrick));
 			add(new StructureCuboid(-1, 4, -2, stoneBrick));
 			add(new StructureCuboid(0, 4, -2, stoneBrickSpecial));
 			add(new StructureCuboid(-1, 5, -2, 1, 5, -2, woodSlab));
 
 			// Right beam
 			add(new StructureCuboid(1, 4, 2, stoneBrick));
 			add(new StructureCuboid(-1, 4, 2, stoneBrick));
 			add(new StructureCuboid(0, 4, 2, stoneBrickSpecial));
 			add(new StructureCuboid(-1, 5, 2, 1, 5, 2, woodSlab));
 
 			// Top beam
 			add(new StructureCuboid(2, 4, 1, stoneBrick));
 			add(new StructureCuboid(2, 4, -1, stoneBrick));
 			add(new StructureCuboid(2, 4, 0, stoneBrickSpecial));
 			add(new StructureCuboid(2, 5, -1, 2, 5, 1, woodSlab));
 
 			// Bottom beam
 			add(new StructureCuboid(-2, 4, 1, stoneBrick));
 			add(new StructureCuboid(-2, 4, -1, stoneBrick));
 			add(new StructureCuboid(-2, 4, 0, stoneBrickSpecial));
 			add(new StructureCuboid(-2, 5, -1, -2, 5, 1, woodSlab));
 
 			// Create main platform
 			add(new StructureCuboid(0, 1, 0, stoneBrick));
 			add(new StructureCuboid(-2, 1, -2, 2, 1, 2, stoneBrickSlabBottom).exclude(0, 1, 0));
 		}
 	};
 	final static List<StructureSchematic> altar = new ArrayList<StructureSchematic>()
 	{
 		{
 			add(general);
 		}
 	};
 
 	public static enum AltarDesign implements StructureSchematic.StructureDesign
 	{
 		GENERAL(0, "general");
 
 		private int index;
 		private String name;
 
 		private AltarDesign(int index, String name)
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
 				add(Structure.Flag.NO_PVP_ZONE);
 				add(Structure.Flag.PROTECTED_BLOCKS);
 				add(Structure.Flag.PRAYER_LOCATION);
 			}
 		};
 	}
 
 	@Override
 	public String getStructureType()
 	{
 		return "Altar";
 	}
 
 	@Override
 	public List<StructureSchematic> getSchematics()
 	{
 		return altar;
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
 		save.setStructureDesign(0);
 		save.save();
 		if(generate) save.generate();
 		return save;
 	}
 
 	public static boolean altarNearby(Location location)
 	{
 		for(StructureSave structureSave : StructureSave.loadAll())
 		{
			// if(structureSave.getReferenceLocation().distance(location) <= Demigods.config.getSettingInt("generation.min_blocks_between_altars") && structureSave.getStructureInfo().getStructureType().equals(EpisodeDemo.Structures.ALTAR.getStructure().getStructureType())) return true;
 		}
 		return false;
 	}
 }
 
 class AltarListener implements Listener
 {
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onChunkLoad(final ChunkLoadEvent event)
 	{
 		// If it's a new chunk then we'll generate structures
 		if(event.isNewChunk())
 		{
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
 
						// EpisodeDemo.Structures.ALTAR.getStructure().createNew(location, true);
 
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
 	}
 
 	@EventHandler(priority = EventPriority.HIGH)
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
 		if(AdminUtility.useWand(player) && clickedBlock.getType().equals(Material.EMERALD_BLOCK))
 		{
 			event.setCancelled(true);
 
 			// Remove clicked block
 			clickedBlock.setType(Material.AIR);
 
 			AdminUtility.sendDebug(ChatColor.RED + "Altar generated by ADMIN WAND at " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ());
 
 			player.sendMessage(ChatColor.GRAY + Demigods.text.getText(TextUtility.Text.ADMIN_WAND_GENERATE_ALTAR));
			// EpisodeDemo.Structures.ALTAR.getStructure().createNew(location, true);
 			player.sendMessage(ChatColor.GREEN + Demigods.text.getText(TextUtility.Text.ADMIN_WAND_GENERATE_ALTAR_COMPLETE));
 		}
 
 		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && AdminUtility.useWand(player) && Structure.partOfStructureWithType(location, "Altar"))
 		{
 			event.setCancelled(true);
 
 			StructureSave altar = Structure.getStructure(location);
 
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
