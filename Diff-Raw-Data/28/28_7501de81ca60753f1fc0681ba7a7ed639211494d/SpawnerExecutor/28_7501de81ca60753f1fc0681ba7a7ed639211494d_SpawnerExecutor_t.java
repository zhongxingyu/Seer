 package com.github.thebiologist13;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.github.thebiologist13.commands.spawners.ActivateAllCommand;
 import com.github.thebiologist13.commands.spawners.ActiveCommand;
 import com.github.thebiologist13.commands.spawners.AddTypeCommand;
 import com.github.thebiologist13.commands.spawners.CloneCommand;
 import com.github.thebiologist13.commands.spawners.ConvertCommand;
 import com.github.thebiologist13.commands.spawners.CreateCommand;
 import com.github.thebiologist13.commands.spawners.DeactivateAllCommand;
 import com.github.thebiologist13.commands.spawners.ForceSpawnCommand;
 import com.github.thebiologist13.commands.spawners.HiddenCommand;
 import com.github.thebiologist13.commands.spawners.InfoCommand;
 import com.github.thebiologist13.commands.spawners.LightLevelCommand;
 import com.github.thebiologist13.commands.spawners.ListAllCommand;
 import com.github.thebiologist13.commands.spawners.ListNearCommand;
 import com.github.thebiologist13.commands.spawners.MaxMobsCommand;
 import com.github.thebiologist13.commands.spawners.NameCommand;
 import com.github.thebiologist13.commands.spawners.PerSpawnCommand;
 import com.github.thebiologist13.commands.spawners.PlayerDistanceCommand;
 import com.github.thebiologist13.commands.spawners.PositionCommand;
 import com.github.thebiologist13.commands.spawners.RemoveAllMobsCommand;
 import com.github.thebiologist13.commands.spawners.RemoveCommand;
 import com.github.thebiologist13.commands.spawners.RemoveMobsCommand;
 import com.github.thebiologist13.commands.spawners.SelectCommand;
 import com.github.thebiologist13.commands.spawners.SetLocationCommand;
 import com.github.thebiologist13.commands.spawners.SetRadiusCommand;
 import com.github.thebiologist13.commands.spawners.SetRateCommand;
 import com.github.thebiologist13.commands.spawners.SetRedstoneCommand;
 import com.github.thebiologist13.commands.spawners.SetTypeCommand;
 import com.github.thebiologist13.commands.spawners.SpawnAreaCommand;
 import com.github.thebiologist13.commands.spawners.SpawnOnPowerCommand;
 import com.github.thebiologist13.commands.spawners.SpawnerCommand;
 import com.github.thebiologist13.commands.spawners.ToggleWandCommand;
 
 /**
  * This executes commands related to /spawners command.
  * 
  * @author thebiologist13
  */
 public class SpawnerExecutor extends Executor implements CommandExecutor {
 	
 	public SpawnerExecutor(CustomSpawners plugin) {
 		super(plugin);
 		
 		SpawnerCommand hidden = new HiddenCommand(plugin, "customspawners.spawners.hidden");
 		SpawnerCommand activateAll = new ActivateAllCommand(plugin, "customspawners.spawners.activateall");
 		SpawnerCommand addType = new AddTypeCommand(plugin, "customspawners.spawners.addtype");
 		SpawnerCommand deactivateAll = new DeactivateAllCommand(plugin, "customspawners.spawners.deactivateall");
 		SpawnerCommand forceSpawn = new ForceSpawnCommand(plugin, "customspawners.spawners.forcespawn");
 		SpawnerCommand removeMobs = new RemoveMobsCommand(plugin, "customspawners.spawners.removemobs");
 		SpawnerCommand name = new NameCommand(plugin, "customspawners.spawners.setname");
 		SpawnerCommand listNear = new ListNearCommand(plugin, "customspawners.spawners.listnear");
 		SpawnerCommand listAll = new ListAllCommand(plugin, "customspawners.spawners.listall");
 		SpawnerCommand create = new CreateCommand(plugin, "customspawners.spawners.create");
 		SpawnerCommand select = new SelectCommand(plugin, "customspawners.spawners.select");
 		SpawnerCommand remove = new RemoveCommand(plugin, "customspawners.spawners.remove");
 		SpawnerCommand setType = new SetTypeCommand(plugin, "customspawners.spawners.settype");
 		SpawnerCommand lightLevel = new LightLevelCommand(plugin, "customspawners.spawners.setlightlevel");
 		SpawnerCommand maxMobs = new MaxMobsCommand(plugin, "customspawners.spawners.setmaxmobs");
 		SpawnerCommand perSpawn = new PerSpawnCommand(plugin, "customspawners.spawners.setmobsperspawn");
 		SpawnerCommand distance = new PlayerDistanceCommand(plugin, "customspawners.spawners.setdistance");
 		SpawnerCommand location = new SetLocationCommand(plugin, "customspawners.spawners.setlocation");
 		SpawnerCommand radius = new SetRadiusCommand(plugin, "customspawners.spawners.setradius");
 		SpawnerCommand rate = new SetRateCommand(plugin, "customspawners.spawners.setrate");
 		SpawnerCommand redstone = new SetRedstoneCommand(plugin, "customspawners.spawners.setredstone");
 		SpawnerCommand info = new InfoCommand(plugin, "customspawners.spawners.info");
 		SpawnerCommand active = new ActiveCommand(plugin, "customspawners.spawners.activation");
 		SpawnerCommand position = new PositionCommand(plugin, "customspawners.spawners.pos");
 		SpawnerCommand spawnArea = new SpawnAreaCommand(plugin, "customspawners.spawners.setspawnarea");
 		SpawnerCommand convert = new ConvertCommand(plugin, "customspawners.spawners.convert");
 		SpawnerCommand removeAllMobs = new RemoveAllMobsCommand(plugin, "customspawners.spawners.removeallmobs");
 		SpawnerCommand onPower = new SpawnOnPowerCommand(plugin, "customspawners.spawners.spawnonpower");
 		SpawnerCommand wand = new ToggleWandCommand(plugin, "customspawners.spawners.wand");
 		SpawnerCommand clone = new CloneCommand(plugin, "customspawners.spawners.clone");
 		
 		create.setNeedsObject(false);
 		select.setNeedsObject(false);
 		listAll.setNeedsObject(false);
 		listNear.setNeedsObject(false);
 		activateAll.setNeedsObject(false);
 		deactivateAll.setNeedsObject(false);
 		position.setNeedsObject(false);
 		removeAllMobs.setNeedsObject(false);
 		wand.setNeedsObject(false);
 		
 		addCommand("activateallspawners", activateAll, new String[] {
 				"activateall",
 				"allactive",
 				"allspawnersactive",
 				"turnallspawnerson",
 				"turnallon",
 				"allon"
 		});
 		addCommand("setactive", active, new String[] {
 				"active",
 				"activate",
 				"turnon",
 				"on"
 		});
 		addCommand("setinactive", active, new String[] {
 				"inactive",
 				"deactivate",
 				"turnoff",
 				"off"
 		});
 		addCommand("addentitytype", addType, new String[] {
 				"addtype",
 				"addt",
 				"addspawnableentity",
 				"addentity"
 		});
 		addCommand("convert", convert, new String[] {
 				"change",
 				"transmutate",
 				"makevanilla",
 				"makeblock"
 		});
 		addCommand("createspawner", create, new String[] {
 				"new",
 				"create",
 				"makenew",
 				"summon"
 		});
 		addCommand("deactivateallspawners", deactivateAll, new String[] {
 				"deactivateall",
 				"allinactive",
 				"allspawnersinactive",
 				"turnallspawnersoff",
 				"turnalloff",
 				"alloff"
 		});
 		addCommand("forcespawn", forceSpawn, new String[] {
 				"force",
 				"spawn",
 				"test"
 		});
 		addCommand("sethidden", hidden, new String[] {
 				"hidden",
 				"hide"
 		});
 		addCommand("info", info, new String[] {
 				"showinfo",
 				"displayinfo",
 				"show",
 				"display"
 		});
 		addCommand("setmaxlight", lightLevel, new String[] {
 				"setmaxlightlevel",
 				"setmaximumlightlevel",
 				"setmaxlight",
 				"maxlight",
 				"maxlightlevel",
 				"maxl"
 		});
 		addCommand("setminlight", lightLevel, new String[] {
 				"setminlightlevel",
 				"setminimumlightlevel",
 				"setminlight",
 				"minlight",
 				"minlightlevel",
 				"minl"
 		});
 		addCommand("listallspawners", listAll, new String[] {
 				"listall",
 				"list",
 				"showspawners",
 				"displayspawners"
 		});
 		addCommand("listnearbyspawners", listNear, new String[] {
 				"listnearspawners",
 				"listnearby",
 				"listnear",
 				"listn"
 		});
 		addCommand("setmaximummobs", maxMobs, new String[] {
 				"setmaxmobs",
 				"maxmobs",
 				"mobs",
 				"setmobs"
 		});
 		addCommand("setname", name, new String[] {
 				"name",
 				"callit",
 				"displayname"
 		});
 		addCommand("setmobsperspawn", perSpawn, new String[] {
 				"mobsperspawn",
 				"setperspawn",
 				"perspawn",
 				"mobsper",
 				"setmobsper",
 				"spawncount",
 				"spawnamount",
 				"setspawncount",
 				"setspawnamount",
 				"setmps",
 				"mps"
 		});
 		addCommand("setmaxdistance", distance, new String[] {
 				"maxdistance",
 				"maxdis",
 				"setmaxplayerdistance",
 				"maxplayerdistance",
 				"maxplayerdis",
 				"farthest"
 		});
 		addCommand("setmindistance", distance, new String[] {
 				"mindistance",
 				"mindis",
 				"setminplayerdistance",
 				"minplayerdistance",
 				"minplayerdis",
 				"closest"
 		});
 		addCommand("pos1", position, new String[] {
 				"setpos1",
 				"position1",
 				"setposition1",
 				"p1",
 				"setp1"
 		});
 		addCommand("pos2", position, new String[] {
 				"setpos2",
 				"position2",
 				"setposition2",
 				"p2",
 				"setp2"
 		});
 		addCommand("removespawner", remove, new String[] {
 				"remove",
 				"rem",
 				"deletespawner",
 				"delete",
 				"del"
 		});
 		addCommand("removeallmobs", removeAllMobs, new String[] {
 				"deleteallmobs",
 				"killallmobs",
 				"invadersmustdie"
 		});
 		addCommand("removemobs", removeMobs, new String[] {
 				"deletemobs",
 				"killmobs",
 				"destroythemwithlasers",
 				"remmobs"
 		});
 		addCommand("selectspawner", select, new String[] {
 				"select",
 				"sel",
 				"choose"
 		});
 		addCommand("setlocation", location, new String[] {
 				"location",
 				"loc",
 				"puthere"
 		});
 		addCommand("setradius", radius, new String[] {
 				"radius",
 				"rad",
 				"setspawnradius",
 				"spawnradius",
 				"spawnrad"
 		});
 		addCommand("setrate", rate, new String[] {
 				"rate",
 				"setspawnrate",
 				"spawnrate",
 				"sethowfast",
 				"howfast",
 				"settickrate",
 				"tickrate"
 		});
 		addCommand("setredstone", redstone, new String[] {
 				"redstone",
 				"setredstonetriggered",
 				"redstonetriggered",
 				"redstonepowered",
 				"setpower",
 				"power"
 		});
 		addCommand("setspawnonpower", onPower, new String[] {
 				"spawnonpower",
 				"setspawnwhentriggered",
 				"spawnontrigger",
 				"setpulsetriggered",
 				"pulsetriggered",
 				"setonpower",
 				"onpower",
 				"sot",
 				"sop",
 				"sor"
 		});
 		addCommand("setspawntype", setType, new String[] {
 				"spawntype",
 				"settype",
 				"type",
 				"setspawnedentity",
 				"setentity",
 				"entity",
 				"setspawnableentity",
 				"spawnableentity"
 		});
 		addCommand("setspawnarea", spawnArea, new String[] {
 				"spawnarea",
 				"setspawnzone",
 				"spawnzone"
 		});
 		addCommand("togglewand", wand, new String[] {
 				"wand",
 				"areaselect",
 				"toggleareaselect"
 		});
 		addCommand("clone", clone, new String[] {
 				"clonespawner",
 				"copy",
 				"copyspawner"
 		});
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
 		
 		if(arg1.getName().equalsIgnoreCase("spawners")) {
 			
 			Spawner spawnerRef = null;
 			String sub = arg3[0].toLowerCase();
 			String objId = "";
 			String[] params;
 			
 			if(arg3.length > 1)
 				objId = arg3[1];
 
 			if(arg3.length == 0) {
 				PLUGIN.sendMessage(arg0, ChatColor.RED + "You must enter a command.");
 				return true;
 			}
 			
 			SpawnerCommand cmd = (SpawnerCommand) super.getCommand(sub);
 			
 			if(cmd == null) {
 				PLUGIN.sendMessage(arg0, ChatColor.RED + "\"" + sub + "\" is not valid for the spawners command.");
 				return true;
 			}
 			
 			sub = cmd.getCommand(sub); //Aliases
 			
 			if(!cmd.permissible(arg0, null)) {
 				PLUGIN.sendMessage(arg0, cmd.NO_PERMISSION);
 				return true;
 			}
 			
 			if(cmd.needsObject()) {
 				
 				if(arg0 instanceof Player) {
 					Player p = (Player) arg0;
 					
 					if(!CustomSpawners.spawnerSelection.containsKey(p)) {
 						if(objId.startsWith("t:")) {
 							spawnerRef = CustomSpawners.getSpawner(objId.substring(2));
 							params = makeParams(arg3, 2);
 						} else {
 							spawnerRef = CustomSpawners.getSpawner(objId);
 							params = makeParams(arg3, 2);
 						}
 					} else {
 						if(objId.startsWith("t:")) { //If they want to target a specific object
 							spawnerRef = CustomSpawners.getSpawner(objId.substring(2));
 							params = makeParams(arg3, 2);
 						} else {
 							spawnerRef = CustomSpawners.getSpawner(CustomSpawners.spawnerSelection.get(p));
 							params = makeParams(arg3, 1);
 						}
 					}
 				} else {
 					if(CustomSpawners.consoleEntity == -1) {
 						if(objId.startsWith("t:")) {
 							spawnerRef = CustomSpawners.getSpawner(objId.substring(2));
 							params = makeParams(arg3, 2);
 						} else {
 							spawnerRef = CustomSpawners.getSpawner(objId);
 							params = makeParams(arg3, 2);
 						}
 					} else {
 						if(objId.startsWith("t:")) { //If they want to target a specific object
 							spawnerRef = CustomSpawners.getSpawner(objId.substring(2));
 							params = makeParams(arg3, 2);
 						} else {
 							spawnerRef = CustomSpawners.getSpawner(CustomSpawners.consoleSpawner);
 							params = makeParams(arg3, 1);
 						}
 					}
 				}
 				
 				if(spawnerRef == null) {
 					PLUGIN.sendMessage(arg0, cmd.NO_SPAWNER);
 					return true;
 				}
 			} else {
 				params = makeParams(arg3, 1);
 			}
 			
 			try {
 				cmd.run(spawnerRef, arg0, sub, params);
 			} catch(ArrayIndexOutOfBoundsException e) {
 				PLUGIN.sendMessage(arg0, ChatColor.RED + "You entered invalid parameters.");
 				return true;
 			} catch(Exception e) {
 				PLUGIN.printDebugTrace(e);
				PLUGIN.sendMessage(arg0, ChatColor.RED + "An error has occurred. Crash report saved to " + 
 						PLUGIN.getFileManager().saveCrash(cmd.getClass(), e));
 				PLUGIN.sendMessage(arg0, cmd.GENERAL_ERROR);
 				return true;
 			}
 			
 			if(CONFIG.getBoolean("data.autosave") && CONFIG.getBoolean("data.saveOnCommand")) {
 				PLUGIN.getFileManager().autosaveAll();
 			}
 			
 			return true;
 		}
 		
 		return false;
 		
 	}
 	
 }
