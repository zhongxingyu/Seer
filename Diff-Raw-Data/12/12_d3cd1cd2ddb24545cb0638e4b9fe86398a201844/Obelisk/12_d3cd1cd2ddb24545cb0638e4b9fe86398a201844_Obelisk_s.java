 package com.censoredsoftware.demigods.greek.structure;
 
 import com.censoredsoftware.censoredlib.schematic.BlockData;
 import com.censoredsoftware.censoredlib.schematic.Schematic;
 import com.censoredsoftware.censoredlib.schematic.Selection;
 import com.censoredsoftware.censoredlib.util.Colors;
 import com.censoredsoftware.demigods.engine.data.DCharacter;
 import com.censoredsoftware.demigods.engine.data.DPlayer;
 import com.censoredsoftware.demigods.engine.data.DataManager;
 import com.censoredsoftware.demigods.engine.data.StructureData;
 import com.censoredsoftware.demigods.engine.mythos.Deity;
 import com.censoredsoftware.demigods.engine.mythos.Structure;
 import com.censoredsoftware.demigods.engine.util.Admins;
 import com.censoredsoftware.demigods.engine.util.Configs;
 import com.censoredsoftware.demigods.engine.util.Messages;
 import com.censoredsoftware.demigods.engine.util.Zones;
 import com.censoredsoftware.demigods.greek.language.English;
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import org.bukkit.*;
 import org.bukkit.block.Block;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.material.MaterialData;
 
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 public class Obelisk extends GreekStructure
 {
 	private static final String name = "Obelisk";
 	private static final Function<Location, GreekStructure.Design> getDesign = new Function<Location, Structure.Design>()
 	{
 		@Override
 		public Structure.Design apply(Location reference)
 		{
 			switch(reference.getBlock().getBiome())
 			{
 				case OCEAN:
 				case BEACH:
 				case DESERT:
 				case DESERT_HILLS:
 					return ObeliskDesign.DESERT;
 				case HELL:
 					return ObeliskDesign.NETHER;
 				default:
 					return ObeliskDesign.GENERAL;
 			}
 		}
 	};
 	private static final Function<GreekStructure.Design, StructureData> createNew = new Function<GreekStructure.Design, StructureData>()
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
 	private static final Structure.InteractFunction<Boolean> sanctify = new Structure.InteractFunction<Boolean>()
 	{
 		@Override
 		public Boolean apply(StructureData data, DCharacter character)
 		{
 			if(!DCharacter.Util.areAllied(character, DataManager.characters.get(data.getOwner())) || !data.getSanctifiers().contains(character.getId())) return false;
 			Location location = data.getReferenceLocation();
 			location.getWorld().playSound(location, Sound.CAT_PURR, 0.3F, 0.7F);
 			MaterialData colorData = Colors.getMaterial(character.getDeity().getColor());
 			location.getWorld().playEffect(location.clone().add(0, 1, 0), Effect.STEP_SOUND, colorData.getItemTypeId(), colorData.getData());
 			return true;
 		}
 	};
 	private static final Structure.InteractFunction<Boolean> corrupt = new Structure.InteractFunction<Boolean>()
 	{
 		@Override
 		public Boolean apply(StructureData data, DCharacter character)
 		{
 			if(DCharacter.Util.areAllied(character, DataManager.characters.get(data.getOwner()))) return false;
 			if(DataManager.characters.containsKey(data.getOwner()))
 			{
 				DPlayer dPlayer = DPlayer.Util.getPlayer(DataManager.characters.get(data.getOwner()).getOfflinePlayer());
 				long lastLogoutTime = dPlayer.getLastLogoutTime();
 				Calendar calendarHalfHour = Calendar.getInstance();
 				calendarHalfHour.add(Calendar.MINUTE, -30);
 				long thirtyMinutesAgo = calendarHalfHour.getTime().getTime();
 				Calendar calendarWeek = Calendar.getInstance();
 				calendarWeek.add(Calendar.WEEK_OF_YEAR, -3);
 				long threeWeeksAgo = calendarWeek.getTime().getTime();
 				if(!dPlayer.getOfflinePlayer().isOnline() && lastLogoutTime != -1 && (lastLogoutTime > System.currentTimeMillis() - thirtyMinutesAgo || lastLogoutTime < threeWeeksAgo))
 				{
 					character.getOfflinePlayer().getPlayer().sendMessage(ChatColor.YELLOW + "This obelisk currently immune to damage.");
 					return false;
 				}
 			}
 			Location location = data.getReferenceLocation();
 			location.getWorld().playSound(location, Sound.WITHER_HURT, 0.4F, 1.5F);
 			for(Location found : data.getLocations())
 				location.getWorld().playEffect(found, Effect.STEP_SOUND, Material.REDSTONE_BLOCK.getId());
 
 			character.getOfflinePlayer().getPlayer().sendMessage("Corruption: " + (data.getSanctity() - data.getCorruption()));
 
 			return true;
 		}
 	};
 	private static final Structure.InteractFunction<Boolean> birth = new Structure.InteractFunction<Boolean>()
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
 	private static final Structure.InteractFunction<Boolean> kill = new Structure.InteractFunction<Boolean>()
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
 	private static final Set<Structure.Flag> flags = new HashSet<Structure.Flag>()
 	{
 		{
 			add(Structure.Flag.DESTRUCT_ON_BREAK);
 			add(Structure.Flag.NO_GRIEFING);
 		}
 	};
 	private static final Listener listener = new Listener()
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
 
 				if(event.getAction() == Action.RIGHT_CLICK_BLOCK && !character.getDeity().getFlags().contains(Deity.Flag.NO_OBELISK) && character.getDeity().getClaimItems().keySet().contains(event.getPlayer().getItemInHand().getType()) && Util.validBlockConfiguration(event.getClickedBlock()))
 				{
 					if(Structure.Util.noOverlapStructureNearby(location))
 					{
 						player.sendMessage(ChatColor.YELLOW + "This location is too close to a no-pvp zone, please try again.");
 						return;
 					}
 
 					try
 					{
 						// Obelisk created!
 						Admins.sendDebug(ChatColor.RED + "Obelisk created by " + character.getName() + " at: " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ());
						StructureData save = inst().createNew(location, true);
 						save.setOwner(character.getId());
 						inst().birth(save, character);
 
 						player.sendMessage(ChatColor.GRAY + English.NOTIFICATION_OBELISK_CREATED.getLine());
 						event.setCancelled(true);
 					}
 					catch(Exception errored)
 					{
 						// Creation of shrine failed...
 						Messages.logException(errored);
 					}
 				}
 			}
 
 			if(Admins.useWand(player) && Structure.Util.partOfStructureWithType(location, "Obelisk"))
 			{
 				event.setCancelled(true);
 
 				StructureData save = Structure.Util.getStructureRegional(location);
 				DCharacter owner = DCharacter.Util.load(save.getOwner());
 
 				if(DataManager.hasTimed(player.getName(), "destroy_obelisk"))
 				{
 					// Remove the Obelisk
 					save.remove();
 					DataManager.removeTimed(player.getName(), "destroy_obelisk");
 
 					Admins.sendDebug(ChatColor.RED + "Obelisk owned by (" + owner.getName() + ") at: " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ() + " removed.");
 
 					player.sendMessage(ChatColor.GREEN + English.ADMIN_WAND_REMOVE_OBELISK_COMPLETE.getLine());
 				}
 				else
 				{
 					DataManager.saveTimed(player.getName(), "destroy_obelisk", true, 5);
 					player.sendMessage(ChatColor.RED + English.ADMIN_WAND_REMOVE_OBELISK.getLine());
 				}
 			}
 		}
 	};
 	private static final int radius = Configs.getSettingInt("zones.obelisk_radius");
 	private static final Predicate<CommandSender> allow = new Predicate<CommandSender>()
 	{
 		@Override
 		public boolean apply(CommandSender commandSender)
 		{
 			return true; // commandSender.hasPermission("demigods.greek.obelisk");
 		}
 	};
 	private static final float sanctity = /* 850F */150F, sanctityRegen = 1F;
 
 	private final static Schematic general = new Schematic("general", "HmmmQuestionMark", 3)
 	{
 		{
 			// Everything else.
 			add(new Selection(0, 0, -1, 0, 2, -1, BlockData.Preset.STONE_BRICK));
 			add(new Selection(0, 0, 1, 0, 2, 1, BlockData.Preset.STONE_BRICK));
 			add(new Selection(1, 0, 0, 1, 2, 0, BlockData.Preset.STONE_BRICK));
 			add(new Selection(-1, 0, 0, -1, 2, 0, BlockData.Preset.STONE_BRICK));
 			add(new Selection(0, 4, -1, 0, 5, -1, BlockData.Preset.STONE_BRICK));
 			add(new Selection(0, 4, 1, 0, 5, 1, BlockData.Preset.STONE_BRICK));
 			add(new Selection(1, 4, 0, 1, 5, 0, BlockData.Preset.STONE_BRICK));
 			add(new Selection(-1, 4, 0, -1, 5, 0, BlockData.Preset.STONE_BRICK));
 			add(new Selection(0, 0, 0, 0, 4, 0, Material.REDSTONE_BLOCK));
 			add(new Selection(0, 3, -1, Material.REDSTONE_LAMP_ON));
 			add(new Selection(0, 3, 1, Material.REDSTONE_LAMP_ON));
 			add(new Selection(1, 3, 0, Material.REDSTONE_LAMP_ON));
 			add(new Selection(-1, 3, 0, Material.REDSTONE_LAMP_ON));
 			add(new Selection(0, 5, 0, Material.REDSTONE_LAMP_ON));
 			add(new Selection(1, 5, -1, BlockData.Preset.VINE_1));
 			add(new Selection(-1, 5, -1, BlockData.Preset.VINE_1));
 			add(new Selection(1, 5, 1, BlockData.Preset.VINE_4));
 			add(new Selection(-1, 5, 1, BlockData.Preset.VINE_4));
 		}
 	};
 	private final static Schematic desert = new Schematic("desert", "HmmmQuestionMark", 3)
 	{
 		{
 			// Everything else.
 			add(new Selection(0, 0, -1, 0, 2, -1, Material.SANDSTONE));
 			add(new Selection(0, 0, 1, 0, 2, 1, Material.SANDSTONE));
 			add(new Selection(1, 0, 0, 1, 2, 0, Material.SANDSTONE));
 			add(new Selection(-1, 0, 0, -1, 2, 0, Material.SANDSTONE));
 			add(new Selection(0, 4, -1, 0, 5, -1, Material.SANDSTONE));
 			add(new Selection(0, 4, 1, 0, 5, 1, Material.SANDSTONE));
 			add(new Selection(1, 4, 0, 1, 5, 0, Material.SANDSTONE));
 			add(new Selection(-1, 4, 0, -1, 5, 0, Material.SANDSTONE));
 			add(new Selection(0, 0, 0, 0, 4, 0, Material.REDSTONE_BLOCK));
 			add(new Selection(0, 3, -1, Material.REDSTONE_LAMP_ON));
 			add(new Selection(0, 3, 1, Material.REDSTONE_LAMP_ON));
 			add(new Selection(1, 3, 0, Material.REDSTONE_LAMP_ON));
 			add(new Selection(-1, 3, 0, Material.REDSTONE_LAMP_ON));
 			add(new Selection(0, 5, 0, Material.REDSTONE_LAMP_ON));
 		}
 	};
 	private final static Schematic nether = new Schematic("nether", "HmmmQuestionMark", 3)
 	{
 		{
 			// Everything else.
 			add(new Selection(0, 0, -1, 0, 2, -1, Material.NETHER_BRICK));
 			add(new Selection(0, 0, 1, 0, 2, 1, Material.NETHER_BRICK));
 			add(new Selection(1, 0, 0, 1, 2, 0, Material.NETHER_BRICK));
 			add(new Selection(-1, 0, 0, -1, 2, 0, Material.NETHER_BRICK));
 			add(new Selection(0, 4, -1, 0, 5, -1, Material.NETHER_BRICK));
 			add(new Selection(0, 4, 1, 0, 5, 1, Material.NETHER_BRICK));
 			add(new Selection(1, 4, 0, 1, 5, 0, Material.NETHER_BRICK));
 			add(new Selection(-1, 4, 0, -1, 5, 0, Material.NETHER_BRICK));
 			add(new Selection(0, 0, 0, 0, 4, 0, Material.REDSTONE_BLOCK));
 			add(new Selection(0, 3, -1, Material.REDSTONE_LAMP_ON));
 			add(new Selection(0, 3, 1, Material.REDSTONE_LAMP_ON));
 			add(new Selection(1, 3, 0, Material.REDSTONE_LAMP_ON));
 			add(new Selection(-1, 3, 0, Material.REDSTONE_LAMP_ON));
 			add(new Selection(0, 5, 0, Material.REDSTONE_LAMP_ON));
 		}
 	};
 
 	private Obelisk()
 	{
 		super(name, ObeliskDesign.values(), getDesign, createNew, sanctify, corrupt, birth, kill, flags, listener, radius, allow, sanctity, sanctityRegen);
 	}
 
 	public static enum ObeliskDesign implements GreekStructure.Design
 	{
 		GENERAL("general", general), DESERT("desert", desert), NETHER("nether", nether);
 
 		private final String name;
 		private final Schematic schematic;
 
 		private ObeliskDesign(String name, Schematic schematic)
 		{
 			this.name = name;
 			this.schematic = schematic;
 		}
 
 		@Override
 		public String getName()
 		{
 			return name;
 		}
 
 		@Override
 		public Set<Location> getClickableBlocks(Location reference)
 		{
 			return getSchematic().getLocations(reference);
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
 			if(!block.getType().equals(Material.REDSTONE_BLOCK)) return false;
 			if(!block.getRelative(1, 0, 0).getType().equals(Material.STONE)) return false;
 			if(!block.getRelative(-1, 0, 0).getType().equals(Material.STONE)) return false;
 			if(!block.getRelative(0, 0, 1).getType().equals(Material.STONE)) return false;
 			if(!block.getRelative(0, 0, -1).getType().equals(Material.STONE)) return false;
 			if(block.getRelative(1, 0, 1).getType().isSolid()) return false;
 			return !block.getRelative(1, 0, -1).getType().isSolid() && !block.getRelative(-1, 0, 1).getType().isSolid() && !block.getRelative(-1, 0, -1).getType().isSolid();
 		}
 	}
 
 	private static final Structure INST = new Obelisk();
 
 	public static Structure inst()
 	{
 		return INST;
 	}
 }
