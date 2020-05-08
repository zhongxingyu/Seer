 package com.censoredsoftware.demigods.greek.structure;
 
 import com.censoredsoftware.censoredlib.schematic.Schematic;
 import com.censoredsoftware.censoredlib.schematic.Selection;
 import com.censoredsoftware.censoredlib.util.Colors;
 import com.censoredsoftware.demigods.engine.Demigods;
 import com.censoredsoftware.demigods.engine.data.DataManager;
 import com.censoredsoftware.demigods.engine.deity.Deity;
 import com.censoredsoftware.demigods.engine.language.Translation;
 import com.censoredsoftware.demigods.engine.player.DCharacter;
 import com.censoredsoftware.demigods.engine.player.DPlayer;
 import com.censoredsoftware.demigods.engine.structure.Structure;
 import com.censoredsoftware.demigods.engine.structure.StructureData;
 import com.censoredsoftware.demigods.engine.util.Admins;
 import com.censoredsoftware.demigods.engine.util.Configs;
 import com.censoredsoftware.demigods.engine.util.Zones;
 import com.google.common.base.Function;
 import org.bukkit.*;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 public class Shrine
 {
 	public static final String name = "Shrine";
 	public static final Function<Location, GreekStructure.Design> getDesign = new Function<Location, GreekStructure.Design>()
 	{
 		@Override
 		public GreekStructure.Design apply(Location reference)
 		{
 			switch(reference.getBlock().getBiome())
 			{
 				case HELL:
 					return ShrineDesign.NETHER;
 				default:
 					return ShrineDesign.GENERAL;
 			}
 		}
 	};
 	public static final Function<GreekStructure.Design, StructureData> createNew = new Function<GreekStructure.Design, StructureData>()
 	{
 		@Override
 		public StructureData apply(GreekStructure.Design design)
 		{
 			StructureData save = new StructureData();
 			save.setSanctifiers(new HashMap<String, Long>());
 			save.setCorruptors(new HashMap<String, Long>());
 			return save;
 		}
 	};
 	public static final Structure.InteractFunction<Boolean> sanctify = new Structure.InteractFunction<Boolean>()
 	{
 		@Override
 		public Boolean apply(StructureData data, DCharacter character)
 		{
 			if(!DCharacter.Util.areAllied(character, DataManager.characters.get(data.getOwner()))) return false;
 			Location location = data.getReferenceLocation();
			location.getWorld().playSound(location, Sound.CAT_PURREOW, 0.7F, 0.9F);
 			MaterialData colorData = Colors.getMaterial(character.getDeity().getColor());
 			location.getWorld().playEffect(location.clone().add(0, 1, 0), Effect.STEP_SOUND, colorData.getItemTypeId(), colorData.getData());
 			return true;
 		}
 	};
 	public static final Structure.InteractFunction<Boolean> corrupt = new Structure.InteractFunction<Boolean>()
 	{
 		@Override
 		public Boolean apply(StructureData data, DCharacter character)
 		{
 			if(DCharacter.Util.areAllied(character, DataManager.characters.get(data.getOwner()))) return false;
 			Location location = data.getReferenceLocation();
 			location.getWorld().playSound(location, Sound.WITHER_HURT, 0.4F, 1.5F);
 			location.getWorld().playEffect(location.clone().add(0, 1, 0), Effect.STEP_SOUND, Material.REDSTONE_BLOCK.getId());
 			character.getOfflinePlayer().getPlayer().sendMessage(ChatColor.RED + "This shrine has " + (data.getSanctity() - data.getCorruption()) + " sanctity left!");
 			return true;
 		}
 	};
 	public static final Structure.InteractFunction<Boolean> birth = new Structure.InteractFunction<Boolean>()
 	{
 		@Override
 		public Boolean apply(StructureData data, DCharacter character)
 		{
 			Location location = data.getReferenceLocation();
 			location.getWorld().strikeLightningEffect(location);
 			location.getWorld().strikeLightningEffect(character.getLocation());
 			return true;
 		}
 	};
 	public static final Structure.InteractFunction<Boolean> kill = new Structure.InteractFunction<Boolean>()
 	{
 		@Override
 		public Boolean apply(StructureData data, DCharacter character)
 		{
 			Location location = data.getReferenceLocation();
 			location.getWorld().playSound(location, Sound.WITHER_DEATH, 1F, 1.2F);
 			location.getWorld().createExplosion(location, 2F, false);
 			character.addKill();
 			return true;
 		}
 	};
 	public static final Set<Structure.Flag> flags = new HashSet<Structure.Flag>()
 	{
 		{
 			add(Structure.Flag.DELETE_WITH_OWNER);
 			add(Structure.Flag.DESTRUCT_ON_BREAK);
 			add(Structure.Flag.TRIBUTE_LOCATION);
 			add(Structure.Flag.NO_OVERLAP);
 		}
 	};
 	public static final Listener listener = new Listener()
 	{
 		@EventHandler(priority = EventPriority.HIGH)
 		public void createAndRemove(PlayerInteractEvent event)
 		{
 			if(event.getClickedBlock() == null) return;
 
 			if(Zones.inNoDemigodsZone(event.getPlayer().getLocation())) return;
 
 			// Define variables
 			Block clickedBlock = event.getClickedBlock();
 			Location location = clickedBlock.getLocation();
 			Player player = event.getPlayer();
 
 			if(DPlayer.Util.isImmortal(player))
 			{
 				DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 
 				if(event.getAction() == Action.RIGHT_CLICK_BLOCK && !character.getDeity().getFlags().contains(Deity.Flag.NO_SHRINE) && character.getDeity().getClaimItems().keySet().contains(event.getPlayer().getItemInHand().getType()) && Util.validBlockConfiguration(event.getClickedBlock()))
 				{
 					try
 					{
 						// Shrine created!
 						Admins.sendDebug(ChatColor.RED + "Shrine created by " + character.getName() + " (" + character.getDeity() + ") at: " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ());
 						StructureData save = GreekStructure.SHRINE.createNew(location, true);
 						save.setOwner(character.getId());
 						GreekStructure.SHRINE.birth(save, character);
 
 						// Consume item in hand
 						ItemStack item = player.getItemInHand();
 						if(item.getAmount() > 1)
 						{
 							player.getItemInHand().setAmount(item.getAmount() - 1);
 						}
 						else
 						{
 							player.setItemInHand(new ItemStack(Material.AIR));
 						}
 
 						for(String string : Demigods.LANGUAGE.getTextBlock(Translation.Text.NOTIFICATION_SHRINE_CREATED))
 							player.sendMessage(string.replace("{alliance}", character.getAlliance() + "s").replace("{deity}", character.getDeity().getName()));
 						event.setCancelled(true);
 					}
 					catch(Exception e)
 					{
 						// Creation of shrine failed...
 						e.printStackTrace();
 					}
 				}
 			}
 
 			if(Admins.useWand(player) && Structure.Util.partOfStructureWithType(location, "Shrine"))
 			{
 				event.setCancelled(true);
 
 				StructureData save = Structure.Util.getStructureRegional(location);
 				DCharacter owner = DCharacter.Util.load(save.getOwner());
 
 				if(DataManager.hasTimed(player.getName(), "destroy_shrine"))
 				{
 					// Remove the Shrine
 					save.remove();
 					DataManager.removeTimed(player.getName(), "destroy_shrine");
 
 					Admins.sendDebug(ChatColor.RED + "Shrine of (" + owner.getDeity() + ") at: " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ() + " removed.");
 
 					player.sendMessage(ChatColor.GREEN + Demigods.LANGUAGE.getText(Translation.Text.ADMIN_WAND_REMOVE_SHRINE_COMPLETE));
 				}
 				else
 				{
 					DataManager.saveTimed(player.getName(), "destroy_shrine", true, 5);
 					player.sendMessage(ChatColor.RED + Demigods.LANGUAGE.getText(Translation.Text.ADMIN_WAND_REMOVE_SHRINE));
 				}
 			}
 		}
 	};
 	public static final int radius = Configs.getSettingInt("zones.shrine_radius");
 	public static final float sanctity = 250F, sanctityRegen = 1F;
 
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
 	private final static Schematic nether = new Schematic("nether", "HmmmQuestionMark", 2)
 	{
 		{
 			// Create the main block
 			add(new Selection(0, 1, 0, Material.GOLD_BLOCK));
 
 			// Create the ender chest and the block below
 			add(new Selection(0, 0, 0, Material.ENDER_CHEST));
 			add(new Selection(0, -1, 0, Material.NETHER_BRICK));
 
 			// Create the rest
 			add(new Selection(-1, 0, 0, Material.NETHER_BRICK_STAIRS));
 			add(new Selection(1, 0, 0, Material.NETHER_BRICK_STAIRS, (byte) 1));
 			add(new Selection(0, 0, -1, Material.NETHER_BRICK_STAIRS, (byte) 2));
 			add(new Selection(0, 0, 1, Material.NETHER_BRICK_STAIRS, (byte) 3));
 		}
 	};
 
 	public static enum ShrineDesign implements GreekStructure.Design
 	{
 		GENERAL("general", general, new Selection(0, 1, 0)), NETHER("nether", nether, new Selection(0, 1, 0));
 
 		private final String name;
 		private final Schematic schematic;
 		private final Selection clickableBlocks;
 
 		private ShrineDesign(String name, Schematic schematic, Selection clickableBlocks)
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
 
 	public static class Util
 	{
 		public static boolean validBlockConfiguration(Block block)
 		{
 			if(!block.getType().equals(Material.GOLD_BLOCK)) return false;
 			if(!block.getRelative(1, 0, 0).getType().equals(Material.COBBLESTONE)) return false;
 			if(!block.getRelative(-1, 0, 0).getType().equals(Material.COBBLESTONE)) return false;
 			if(!block.getRelative(0, 0, 1).getType().equals(Material.COBBLESTONE)) return false;
 			if(!block.getRelative(0, 0, -1).getType().equals(Material.COBBLESTONE)) return false;
 			if(block.getRelative(1, 0, 1).getType().isSolid()) return false;
 			return !block.getRelative(1, 0, -1).getType().isSolid() && !block.getRelative(-1, 0, 1).getType().isSolid() && !block.getRelative(-1, 0, -1).getType().isSolid();
 		}
 	}
 }
