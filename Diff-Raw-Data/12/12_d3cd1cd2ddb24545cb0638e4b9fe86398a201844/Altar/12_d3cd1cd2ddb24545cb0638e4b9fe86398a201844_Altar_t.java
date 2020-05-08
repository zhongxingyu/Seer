 package com.censoredsoftware.demigods.greek.structure;
 
 import com.censoredsoftware.censoredlib.schematic.BlockData;
 import com.censoredsoftware.censoredlib.schematic.Schematic;
 import com.censoredsoftware.censoredlib.schematic.Selection;
 import com.censoredsoftware.censoredlib.util.Randoms;
 import com.censoredsoftware.demigods.engine.DemigodsPlugin;
 import com.censoredsoftware.demigods.engine.data.CLocationManager;
 import com.censoredsoftware.demigods.engine.data.DCharacter;
 import com.censoredsoftware.demigods.engine.data.DataManager;
 import com.censoredsoftware.demigods.engine.data.StructureData;
 import com.censoredsoftware.demigods.engine.mythos.Structure;
 import com.censoredsoftware.demigods.engine.util.Admins;
 import com.censoredsoftware.demigods.engine.util.Configs;
 import com.censoredsoftware.demigods.engine.util.Zones;
 import com.censoredsoftware.demigods.greek.language.English;
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Biome;
 import org.bukkit.block.Block;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.world.ChunkLoadEvent;
 
 import javax.annotation.Nullable;
 import java.util.HashSet;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 public class Altar extends GreekStructure
 {
 	private static final String name = "Altar";
 	private static final Function<Location, GreekStructure.Design> getDesign = new Function<Location, GreekStructure.Design>()
 	{
 		@Override
 		public GreekStructure.Design apply(Location reference)
 		{
 			switch(reference.getBlock().getBiome())
 			{
 				case ICE_PLAINS:
 					return AltarDesign.HOLY;
 				case DESERT:
 				case DESERT_HILLS:
 					return AltarDesign.OASIS;
 				default:
 					return AltarDesign.GENERAL;
 			}
 		}
 	};
 	private static final Function<GreekStructure.Design, StructureData> createNew = new Function<GreekStructure.Design, StructureData>()
 	{
 		@Override
 		public StructureData apply(GreekStructure.Design design)
 		{
 			return new StructureData();
 		}
 	};
 	private static final Structure.InteractFunction<Boolean> sanctify = new Structure.InteractFunction<Boolean>()
 	{
 		@Override
 		public Boolean apply(@Nullable StructureData data, @Nullable DCharacter unused)
 		{
 			return false;
 		}
 	};
 	private static final Structure.InteractFunction<Boolean> corrupt = new Structure.InteractFunction<Boolean>()
 	{
 		@Override
 		public Boolean apply(@Nullable StructureData data, @Nullable DCharacter unused)
 		{
 			return false;
 		}
 	};
 	private static final Structure.InteractFunction<Boolean> birth = new Structure.InteractFunction<Boolean>()
 	{
 		@Override
 		public Boolean apply(@Nullable StructureData data, @Nullable DCharacter unused)
 		{
 			return false;
 		}
 	};
 	private static final Structure.InteractFunction<Boolean> kill = new Structure.InteractFunction<Boolean>()
 	{
 		@Override
 		public Boolean apply(@Nullable StructureData data, @Nullable DCharacter unused)
 		{
 			return false;
 		}
 	};
 	private static final Set<Structure.Flag> flags = new HashSet<Structure.Flag>()
 	{
 		{
 			add(Structure.Flag.NO_PVP);
 			add(Structure.Flag.PRAYER_LOCATION);
 			add(Structure.Flag.PROTECTED_BLOCKS);
 			add(Structure.Flag.NO_OVERLAP);
 		}
 	};
 	private static final Listener listener = new Listener()
 	{
 		public double ALTAR_SPAWN = Configs.getSettingDouble("generation.altar_chance");
 
 		@EventHandler(priority = EventPriority.MONITOR)
 		public void onChunkLoad(final ChunkLoadEvent event)
 		{
 			if(Zones.inNoDemigodsZone(CLocationManager.randomChunkLocation(event.getChunk())) || !event.isNewChunk() || !Randoms.randomPercentBool(ALTAR_SPAWN)) return;
 
 			// Add to queue
 			Util.blocks.add(CLocationManager.randomChunkLocation(event.getChunk()).getBlock());
 		}
 
 		@EventHandler(priority = EventPriority.HIGHEST)
 		public void demigodsAdminWand(PlayerInteractEvent event)
 		{
 			if(event.getClickedBlock() == null || Zones.inNoDemigodsZone(event.getPlayer().getLocation())) return;
 
 			// Define variables
 			Block clickedBlock = event.getClickedBlock();
 			Location location = clickedBlock.getLocation();
 			Player player = event.getPlayer();
 
 			/**
 			 * Handle Altars
 			 */
 			String design = clickedBlock.getType().equals(Material.EMERALD_BLOCK) ? "general" : clickedBlock.getType().equals(Material.GOLD_BLOCK) ? "holy" : clickedBlock.getType().equals(Material.DIAMOND_BLOCK) ? "oasis" : "";
 			if(Admins.useWand(player) && AltarDesign.getByName(design) != null)
 			{
 				event.setCancelled(true);
 
 				// Remove clicked block
 				clickedBlock.setType(Material.AIR);
 
 				Admins.sendDebug(ChatColor.RED + "Altar generated by NO_BATTLE WAND at " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ());
 
 				player.sendMessage(ChatColor.GRAY + English.ADMIN_WAND_GENERATE_ALTAR.getLine());
 
 				// Generate the Altar based on the block given.
				StructureData save = inst().createNew(false, location);
 				save.setDesign(design);
 				save.save();
 				save.generate();
 
 				player.sendMessage(ChatColor.GREEN + English.ADMIN_WAND_GENERATE_ALTAR_COMPLETE.getLine());
 				return;
 			}
 
 			if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && Admins.useWand(player) && Structure.Util.partOfStructureWithType(location, "Altar"))
 			{
 				event.setCancelled(true);
 
 				StructureData altar = Structure.Util.getStructureRegional(location);
 
 				if(DataManager.hasTimed(player.getName(), "destroy_altar"))
 				{
 					Admins.sendDebug(ChatColor.RED + "Altar at " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ() + " removed by " + "NO_BATTLE WAND" + ".");
 
 					// Remove the Altar
 
 					altar.remove();
 
 					DataManager.removeTimed(player.getName(), "destroy_altar");
 
 					player.sendMessage(ChatColor.GREEN + English.ADMIN_WAND_REMOVE_ALTAR_COMPLETE.getLine());
 				}
 				else
 				{
 					DataManager.saveTimed(player.getName(), "destroy_altar", true, 5);
 					player.sendMessage(ChatColor.RED + English.ADMIN_WAND_REMOVE_ALTAR.getLine());
 				}
 			}
 		}
 	};
 	private static final int radius = Configs.getSettingInt("zones.altar_radius");
 	private static final Predicate<CommandSender> allow = new Predicate<CommandSender>()
 	{
 		@Override
 		public boolean apply(CommandSender commandSender)
 		{
 			return true; // commandSender.hasPermission("demigods.greek.altar");
 		}
 	};
 	private static final float sanctity = -1F, sanctityRegen = 1F;
 
 	private final static Schematic general = new Schematic("general", "_Alex", 3)
 	{
 		{
 			// Create roof
 			add(new Selection(2, 3, 2, Material.STEP, (byte) 13));
 			add(new Selection(-2, 3, -2, Material.STEP, (byte) 13));
 			add(new Selection(2, 3, -2, Material.STEP, (byte) 13));
 			add(new Selection(-2, 3, 2, Material.STEP, (byte) 13));
 			add(new Selection(2, 4, 2, BlockData.Preset.STONE_BRICK));
 			add(new Selection(-2, 4, -2, BlockData.Preset.STONE_BRICK));
 			add(new Selection(2, 4, -2, BlockData.Preset.STONE_BRICK));
 			add(new Selection(-2, 4, 2, BlockData.Preset.STONE_BRICK));
 			add(new Selection(2, 5, 2, Material.WOOD_STEP, (byte) 1));
 			add(new Selection(-2, 5, -2, Material.WOOD_STEP, (byte) 1));
 			add(new Selection(2, 5, -2, Material.WOOD_STEP, (byte) 1));
 			add(new Selection(-2, 5, 2, Material.WOOD_STEP, (byte) 1));
 			add(new Selection(0, 6, 0, Material.WOOD_STEP, (byte) 1));
 			add(new Selection(-1, 5, -1, 1, 5, 1, Material.WOOD, (byte) 1));
 
 			// Create the enchantment table
 			add(new Selection(0, 2, 0, Material.ENCHANTMENT_TABLE));
 
 			// Create magical table stand
 			add(new Selection(0, 1, 0, BlockData.Preset.STONE_BRICK));
 
 			// Create outer steps
 			add(new Selection(3, 0, 3, Material.STEP, (byte) 5));
 			add(new Selection(-3, 0, -3, Material.STEP, (byte) 5));
 			add(new Selection(3, 0, -3, Material.STEP, (byte) 5));
 			add(new Selection(-3, 0, 3, Material.STEP, (byte) 5));
 			add(new Selection(4, 0, -2, 4, 0, 2, Material.STEP, (byte) 5));
 			add(new Selection(-4, 0, -2, -4, 0, 2, Material.STEP, (byte) 5));
 			add(new Selection(-2, 0, -4, 2, 0, -4, Material.STEP, (byte) 5));
 			add(new Selection(-2, 0, 4, 2, 0, 4, Material.STEP, (byte) 5));
 
 			// Create inner steps
 			add(new Selection(3, 0, -1, 3, 0, 1, BlockData.Preset.STONE_BRICK));
 			add(new Selection(-1, 0, 3, 1, 0, 3, BlockData.Preset.STONE_BRICK));
 			add(new Selection(-3, 0, -1, -3, 0, 1, BlockData.Preset.STONE_BRICK));
 			add(new Selection(-1, 0, -3, 1, 0, -3, BlockData.Preset.STONE_BRICK));
 
 			// Create pillars
 			add(new Selection(3, 4, 2, Material.WOOD_STEP, (byte) 1));
 			add(new Selection(3, 4, -2, Material.WOOD_STEP, (byte) 1));
 			add(new Selection(2, 4, 3, Material.WOOD_STEP, (byte) 1));
 			add(new Selection(-2, 4, 3, Material.WOOD_STEP, (byte) 1));
 			add(new Selection(-3, 4, 2, Material.WOOD_STEP, (byte) 1));
 			add(new Selection(-3, 4, -2, Material.WOOD_STEP, (byte) 1));
 			add(new Selection(2, 4, -3, Material.WOOD_STEP, (byte) 1));
 			add(new Selection(-2, 4, -3, Material.WOOD_STEP, (byte) 1));
 			add(new Selection(3, 0, 2, 3, 3, 2, BlockData.Preset.STONE_BRICK));
 			add(new Selection(3, 0, -2, 3, 3, -2, BlockData.Preset.STONE_BRICK));
 			add(new Selection(2, 0, 3, 2, 3, 3, BlockData.Preset.STONE_BRICK));
 			add(new Selection(-2, 0, 3, -2, 3, 3, BlockData.Preset.STONE_BRICK));
 			add(new Selection(-3, 0, 2, -3, 3, 2, BlockData.Preset.STONE_BRICK));
 			add(new Selection(-3, 0, -2, -3, 3, -2, BlockData.Preset.STONE_BRICK));
 			add(new Selection(2, 0, -3, 2, 3, -3, BlockData.Preset.STONE_BRICK));
 			add(new Selection(-2, 0, -3, -2, 3, -3, BlockData.Preset.STONE_BRICK));
 
 			// Left beam
 			add(new Selection(1, 4, -2, -1, 4, -2, BlockData.Preset.STONE_BRICK).exclude(0, 4, -2));
 			add(new Selection(0, 4, -2, Material.SMOOTH_BRICK, (byte) 3));
 			add(new Selection(-1, 5, -2, 1, 5, -2, Material.WOOD_STEP, (byte) 1));
 
 			// Right beam
 			add(new Selection(1, 4, 2, -1, 4, 2, BlockData.Preset.STONE_BRICK).exclude(0, 4, 2));
 			add(new Selection(0, 4, 2, Material.SMOOTH_BRICK, (byte) 3));
 			add(new Selection(-1, 5, 2, 1, 5, 2, Material.WOOD_STEP, (byte) 1));
 
 			// Top beam
 			add(new Selection(2, 4, 1, 2, 4, -1, BlockData.Preset.STONE_BRICK).exclude(2, 4, 0));
 			add(new Selection(2, 4, 0, Material.SMOOTH_BRICK, (byte) 3));
 			add(new Selection(2, 5, -1, 2, 5, 1, Material.WOOD_STEP, (byte) 1));
 
 			// Bottom beam
 			add(new Selection(-2, 4, 1, -2, 4, -1, BlockData.Preset.STONE_BRICK).exclude(-2, 4, 0));
 			add(new Selection(-2, 4, 0, Material.SMOOTH_BRICK, (byte) 3));
 			add(new Selection(-2, 5, -1, -2, 5, 1, Material.WOOD_STEP, (byte) 1));
 
 			// Create main platform
 			add(new Selection(-2, 1, -2, 2, 1, 2, Material.STEP, (byte) 5).exclude(0, 1, 0));
 		}
 	};
 	private final static Schematic holy = new Schematic("holy", "HmmmQuestionMark", 3)
 	{
 		{
 			// Create roof
 			add(new Selection(2, 3, 2, Material.STEP, (byte) 15));
 			add(new Selection(-2, 3, -2, Material.STEP, (byte) 15));
 			add(new Selection(2, 3, -2, Material.STEP, (byte) 15));
 			add(new Selection(-2, 3, 2, Material.STEP, (byte) 15));
 			add(new Selection(2, 4, 2, Material.QUARTZ_BLOCK));
 			add(new Selection(-2, 4, -2, Material.QUARTZ_BLOCK));
 			add(new Selection(2, 4, -2, Material.QUARTZ_BLOCK));
 			add(new Selection(-2, 4, 2, Material.QUARTZ_BLOCK));
 			add(new Selection(2, 5, 2, Material.WOOD_STEP, (byte) 2));
 			add(new Selection(-2, 5, -2, Material.WOOD_STEP, (byte) 2));
 			add(new Selection(2, 5, -2, Material.WOOD_STEP, (byte) 2));
 			add(new Selection(-2, 5, 2, Material.WOOD_STEP, (byte) 2));
 			add(new Selection(0, 6, 0, Material.WOOD_STEP, (byte) 2));
 			add(new Selection(-1, 5, -1, 1, 5, 1, Material.WOOD, (byte) 2));
 
 			// Create the enchantment table
 			add(new Selection(0, 2, 0, Material.ENCHANTMENT_TABLE));
 
 			// Create magical table stand
 			add(new Selection(0, 1, 0, Material.QUARTZ_BLOCK, (byte) 1));
 
 			// Create outer steps
 			add(new Selection(3, 0, 3, Material.STEP, (byte) 7));
 			add(new Selection(-3, 0, -3, Material.STEP, (byte) 7));
 			add(new Selection(3, 0, -3, Material.STEP, (byte) 7));
 			add(new Selection(-3, 0, 3, Material.STEP, (byte) 7));
 			add(new Selection(4, 0, -2, 4, 0, 2, Material.STEP, (byte) 7));
 			add(new Selection(-4, 0, -2, -4, 0, 2, Material.STEP, (byte) 7));
 			add(new Selection(-2, 0, -4, 2, 0, -4, Material.STEP, (byte) 7));
 			add(new Selection(-2, 0, 4, 2, 0, 4, Material.STEP, (byte) 7));
 
 			// Create inner steps
 			add(new Selection(3, 0, -1, 3, 0, 1, Material.QUARTZ_BLOCK).exclude(3, 0, 0));
 			add(new Selection(-1, 0, 3, 1, 0, 3, Material.QUARTZ_BLOCK).exclude(0, 0, 3));
 			add(new Selection(-3, 0, -1, -3, 0, 1, Material.QUARTZ_BLOCK).exclude(-3, 0, 0));
 			add(new Selection(-1, 0, -3, 1, 0, -3, Material.QUARTZ_BLOCK).exclude(0, 0, -3));
 			add(new Selection(3, 0, 0, Material.QUARTZ_BLOCK, (byte) 1));
 			add(new Selection(0, 0, 3, Material.QUARTZ_BLOCK, (byte) 1));
 			add(new Selection(-3, 0, 0, Material.QUARTZ_BLOCK, (byte) 1));
 			add(new Selection(0, 0, -3, Material.QUARTZ_BLOCK, (byte) 1));
 
 			// Create pillars
 			add(new Selection(3, 4, 2, Material.WOOD_STEP, (byte) 2));
 			add(new Selection(3, 4, -2, Material.WOOD_STEP, (byte) 2));
 			add(new Selection(2, 4, 3, Material.WOOD_STEP, (byte) 2));
 			add(new Selection(-2, 4, 3, Material.WOOD_STEP, (byte) 2));
 			add(new Selection(-3, 4, 2, Material.WOOD_STEP, (byte) 2));
 			add(new Selection(-3, 4, -2, Material.WOOD_STEP, (byte) 2));
 			add(new Selection(2, 4, -3, Material.WOOD_STEP, (byte) 2));
 			add(new Selection(-2, 4, -3, Material.WOOD_STEP, (byte) 2));
 			add(new Selection(3, 0, 2, 3, 3, 2, Material.QUARTZ_BLOCK, (byte) 2));
 			add(new Selection(3, 0, -2, 3, 3, -2, Material.QUARTZ_BLOCK, (byte) 2));
 			add(new Selection(2, 0, 3, 2, 3, 3, Material.QUARTZ_BLOCK, (byte) 2));
 			add(new Selection(-2, 0, 3, -2, 3, 3, Material.QUARTZ_BLOCK, (byte) 2));
 			add(new Selection(-3, 0, 2, -3, 3, 2, Material.QUARTZ_BLOCK, (byte) 2));
 			add(new Selection(-3, 0, -2, -3, 3, -2, Material.QUARTZ_BLOCK, (byte) 2));
 			add(new Selection(2, 0, -3, 2, 3, -3, Material.QUARTZ_BLOCK, (byte) 2));
 			add(new Selection(-2, 0, -3, -2, 3, -3, Material.QUARTZ_BLOCK, (byte) 2));
 
 			// Left beam
 			add(new Selection(1, 4, -2, -1, 4, -2, Material.QUARTZ_BLOCK).exclude(0, 4, -2));
 			add(new Selection(0, 4, -2, Material.QUARTZ_BLOCK, (byte) 1));
 			add(new Selection(-1, 5, -2, 1, 5, -2, Material.WOOD_STEP, (byte) 2));
 
 			// Right beam
 			add(new Selection(1, 4, 2, -1, 4, 2, Material.QUARTZ_BLOCK).exclude(0, 4, 2));
 			add(new Selection(0, 4, 2, Material.QUARTZ_BLOCK, (byte) 1));
 			add(new Selection(-1, 5, 2, 1, 5, 2, Material.WOOD_STEP, (byte) 2));
 
 			// Top beam
 			add(new Selection(2, 4, 1, 2, 4, -1, Material.QUARTZ_BLOCK).exclude(2, 4, 0));
 			add(new Selection(2, 4, 0, Material.QUARTZ_BLOCK, (byte) 1));
 			add(new Selection(2, 5, -1, 2, 5, 1, Material.WOOD_STEP, (byte) 2));
 
 			// Bottom beam
 			add(new Selection(-2, 4, 1, -2, 4, -1, Material.QUARTZ_BLOCK).exclude(-2, 4, 0));
 			add(new Selection(-2, 4, 0, Material.QUARTZ_BLOCK, (byte) 1));
 			add(new Selection(-2, 5, -1, -2, 5, 1, Material.WOOD_STEP, (byte) 2));
 
 			// Create main platform
 			add(new Selection(-2, 1, -2, 2, 1, 2, Material.STEP, (byte) 7).exclude(0, 1, 0));
 		}
 	};
 	private final static Schematic oasis = new Schematic("oasis", "_Alex", 4)
 	{
 		{
 			// Enchantment Table
 			add(new Selection(0, 0, 0, Material.SANDSTONE, (byte) 2));
 			add(new Selection(0, 1, 0, Material.ENCHANTMENT_TABLE));
 
 			// PWETTY FLOWAS AND GWASS!
 			add(new Selection(-3, 0, -3, 3, 0, 3, BlockData.Preset.PRETTY_FLOWERS_AND_GRASS).exclude(-1, 0, -1, 1, 0, 1));
 
 			// Ground
 			add(new Selection(-3, -1, -3, 3, -1, 3, BlockData.Preset.SANDY_GRASS).exclude(-2, -1, -2, 2, -1, 2));
 			add(new Selection(-2, -1, -2, 2, -1, 2, Material.GRASS).exclude(-1, -1, -1, 1, -1, 1));
 			add(new Selection(-1, -1, -1, 1, -1, 1, Material.WATER).exclude(0, -1, 0));
 			add(new Selection(0, -1, 0, Material.SANDSTONE, (byte) 2));
 			add(new Selection(-3, -2, -3, 3, -2, 3, Material.SANDSTONE));
 
 			// Table
 			add(new Selection(-1, 0, 0, Material.SANDSTONE_STAIRS, (byte) 4));
 			add(new Selection(1, 0, 0, Material.SANDSTONE_STAIRS, (byte) 5));
 			add(new Selection(0, 0, -1, Material.SANDSTONE_STAIRS, (byte) 6));
 			add(new Selection(0, 0, 1, Material.SANDSTONE_STAIRS, (byte) 7));
 
 			// Tiki Torch
 			int rand1 = Randoms.generateIntRange(-3, -2);
 			int rand2 = Randoms.generateIntRange(-3, 3);
 			add(new Selection(-3, 1, -3, 3, 2, 3, Material.AIR).exclude(0, 1, 0));
 			add(new Selection(rand1, 0, rand2, rand1, 1, rand2, Material.FENCE)); // Fence
 			add(new Selection(rand1, 2, rand2, Material.TORCH)); // Torch
 		}
 	};
 
 	private Altar()
 	{
 		super(name, AltarDesign.values(), getDesign, createNew, sanctify, corrupt, birth, kill, flags, listener, radius, allow, sanctity, sanctityRegen);
 	}
 
 	public static enum AltarDesign implements GreekStructure.Design
 	{
 		GENERAL(general, new Selection(0, 2, 0)), HOLY(holy, new Selection(0, 2, 0)), OASIS(oasis, new Selection(0, 1, 0));
 
 		private Schematic schematic;
 		private Selection clickableBlocks;
 
 		private AltarDesign(Schematic schematic, Selection clickableBlocks)
 		{
 			this.schematic = schematic;
 			this.clickableBlocks = clickableBlocks;
 		}
 
 		@Override
 		public String getName()
 		{
 			return schematic.getName();
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
 
 		public static AltarDesign getByName(String name)
 		{
 			for(AltarDesign design : AltarDesign.values())
 				if(design.getName().equalsIgnoreCase(name)) return design;
 			return null;
 		}
 	}
 
 	public static class Util
 	{
 		private static ConcurrentLinkedQueue<Location> locations = new ConcurrentLinkedQueue<Location>();
 		private static ConcurrentLinkedQueue<Block> blocks = new ConcurrentLinkedQueue<Block>();
 		private static final int distance = Configs.getSettingInt("generation.min_blocks_between_altars");
 
 		public static void processNewChunks()
 		{
 			if(blocks.isEmpty()) return;
 			Set<Block> processing = Sets.newHashSet(blocks);
 			for(final Block block : processing)
 			{
 				final Location location = block.getLocation();
 
 				// No altars in hell or heaven for now
 				if(block.getBiome().equals(Biome.HELL) || block.getBiome().equals(Biome.SKY)) return;
 
 				// If another Altar doesn't exist nearby then make one
 				if(!isAltarNearby(location)) locations.add(location);
 
 				blocks.remove(block);
 			}
 		}
 
 		public static void generateAltars()
 		{
 			if(locations.isEmpty()) return;
 			Set<Location> processing = Sets.newHashSet(locations);
 			for(final Location location : processing)
 			{
 				// Check if it can generate, and check again if there are any altars nearby
 				if(Structure.Util.canGenerateStrict(location, 3) && !isAltarNearby(location))
 				{
 					Admins.sendDebug(ChatColor.RED + "Altar generated by SERVER at " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ());
 
					inst().createNew(true, location);
 
 					location.getWorld().strikeLightningEffect(location);
 					location.getWorld().strikeLightningEffect(location);
 
 					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(DemigodsPlugin.plugin(), new Runnable()
 					{
 						@Override
 						public void run()
 						{
 							for(Entity entity : location.getWorld().getEntities())
 								if(entity instanceof Player && entity.getLocation().distance(location) < 400) ((Player) entity).sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + English.ALTAR_SPAWNED_NEAR.getLine());
 						}
 					}, 1);
 				}
 
 				locations.remove(location);
 			}
 		}
 
 		public static boolean isAltarNearby(final Location location)
 		{
 			return Iterables.any(Structure.Util.getStructureWithType(name), new Predicate<StructureData>()
 			{
 				@Override
 				public boolean apply(StructureData save)
 				{
 					return save.getReferenceLocation().distance(location) <= distance;
 				}
 			});
 		}
 
 		public static StructureData getAltarNearby(final Location location)
 		{
 			try
 			{
 				return Iterables.find(Structure.Util.getStructureWithType(name), new Predicate<StructureData>()
 				{
 					@Override
 					public boolean apply(StructureData save)
 					{
 						return save.getReferenceLocation().distance(location) <= distance;
 					}
 				});
 			}
 			catch(NoSuchElementException ignored)
 			{}
 			return null;
 		}
 	}
 
 	private static final Structure INST = new Altar();
 
 	public static Structure inst()
 	{
 		return INST;
 	}
 }
