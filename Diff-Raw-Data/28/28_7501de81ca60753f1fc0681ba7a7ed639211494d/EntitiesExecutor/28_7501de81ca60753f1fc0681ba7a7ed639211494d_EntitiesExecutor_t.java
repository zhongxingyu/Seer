 package com.github.thebiologist13;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.github.thebiologist13.commands.entities.*;
 
 /**
  * This executes commands related to the /entities command.
  * 
  * @author thebiologist13
  */
 public class EntitiesExecutor extends Executor implements CommandExecutor {
 	
 	public EntitiesExecutor(CustomSpawners plugin) {
 		super(plugin);
 		
 		EntityCommand effects = new EntityActiveEffectsCommand(plugin, "customspawners.entities.effects");
 		EntityCommand age = new EntityAgeCommand(plugin, "customspawners.entities.setage");
 		EntityCommand air = new EntityAirCommand(plugin, "customspawners.entities.setair");
 		EntityCommand angry = new EntityAngryCommand(plugin, "customspawners.entities.setangry");
 		EntityCommand cat = new EntityCatTypeCommand(plugin, "customspawners.entities.setcattype");
 		EntityCommand charged = new EntityChargedCommand(plugin, "customspawners.entities.setcharged");
 		EntityCommand color = new EntityColorCommand(plugin, "customspawners.entities.setcolor");
 		EntityCommand create = new EntityCreateCommand(plugin, "customspawners.entities.create");
 		EntityCommand ender = new EntityEnderBlockCommand(plugin, "customspawners.entities.setenderblock");
 		EntityCommand health = new EntityHealthCommand(plugin, "customspawners.entities.sethealth");
 		EntityCommand info = new EntityInfoCommand(plugin, "customspawners.entities.info");
 		EntityCommand jockey = new EntityJockeyCommand(plugin, "customspawners.entities.setjockey");
 		EntityCommand name = new EntityNameCommand(plugin, "customspawners.entities.setname");
 		EntityCommand profession = new EntityProfessionCommand(plugin, "customspawners.entities.setprofession");
 		EntityCommand remove = new EntityRemoveCommand(plugin, "customspawners.entities.remove");
 		EntityCommand saddled = new EntitySaddledCommand(plugin, "customspawners.entities.setsaddled");
 		EntityCommand select = new EntitySelectCommand(plugin, "customspawners.entities.select");
 		EntityCommand type = new EntitySetTypeCommand(plugin, "customspawners.entities.settype");
 		EntityCommand sitting = new EntitySittingCommand(plugin, "customspawners.entities.sitting");
 		EntityCommand slime = new EntitySlimeSizeCommand(plugin, "customspawners.entities.setslimesize");
 		EntityCommand tamed = new EntityTamedCommand(plugin, "customspawners.entities.settamed");
 		EntityCommand vector = new EntityVelocityCommand(plugin, "customspawners.entities.setvelocity");
 		EntityCommand list = new EntityListAllCommand(plugin, "customspawners.entities.listall");
 		EntityCommand passive = new EntityPassiveCommand(plugin, "customspawners.entities.setpassive");
 		EntityCommand fire = new EntityFireTicksCommand(plugin, "customspawners.entities.setfireticks");
 		EntityCommand blacklist = new EntityBlackListCommand(plugin, "customspawners.entities.blacklist");
 		EntityCommand whitelist = new EntityWhiteListCommand(plugin, "customspawners.entities.whitelist");
 		EntityCommand itemlist = new EntityItemListCommand(plugin, "customspawners.entities.itemlist");
 		EntityCommand damage = new EntityDamageCommand(plugin, "customspawners.entities.damage");
 		EntityCommand drops = new EntityDropsCommand(plugin, "customspawners.entities.drops");
 		EntityCommand experience = new EntityExpCommand(plugin, "customspawners.entities.setexp");
 		EntityCommand fuse = new EntityFuseCommand(plugin, "customspawners.entities.setfuseticks");
 		EntityCommand incendiary = new EntityIncendiaryCommand(plugin, "customspawners.entities.setincendiary");
 		EntityCommand yield = new EntityYieldCommand(plugin, "customspawners.entities.setyield");
 		EntityCommand itemType = new EntityItemTypeCommand(plugin, "customspawners.entities.setitemtype");
 		EntityCommand potionType = new EntityPotionTypeCommand(plugin, "customspawners.entities.setpotiontype");
 		EntityCommand invulnerable = new EntityInvulnerableCommand(plugin, "customspawners.entities.setinvulnerable");
 		EntityCommand inventory = new EntityInventoryCommand(plugin, "customspawners.entities.inventory");
 		EntityCommand wither = new EntityWitherCommand(plugin, "customspawners.entities.setwither");
 		EntityCommand villager = new EntityVillagerCommand(plugin, "customspawners.entities.setvillager");
 		
 		create.setNeedsObject(false);
 		select.setNeedsObject(false);
 		list.setNeedsObject(false);
 		
 		addCommand("addeffect", effects, new String[] {
 				"addeffects",
 				"neweffect",
 				"neweffects"
 		});
 		addCommand("seteffect", effects, new String[] {
 				"seteffects"
 		});
 		addCommand("cleareffect", effects, new String[] {
 				"cleareffects",
 				"noeffect",
 				"noeffects"
 		});
 		addCommand("setage", age, new String[] {
 				"age",
 				"howold",
 				"old"
 		});
 		addCommand("setair", air, new String[] {
 				"air",
 				"breath",
 				"oxygen"
 		});
 		addCommand("setangry", angry, new String[] {
 				"angry",
 				"mad",
 				"ticked"
 		});
 		addCommand("setuseblacklist", blacklist, new String[] {
 				"setusingblacklist",
 				"useblacklist",
 				"usingblacklist",
 				"useblack"
 		});
 		addCommand("setblacklist", blacklist, new String[] {
 				"blacklist",
 				"black",
 				"immuneto"
 		});
 		addCommand("addblacklist", blacklist, new String[] {
 				"addblacklistitem",
 				"addblack",
 				"addb",
 				"addimmuneto"
 		});
 		addCommand("clearblacklist", blacklist, new String[] {
 				"clearblack",
 				"noblack",
 				"clearimmuneto"
 		});
 		addCommand("setcattype", cat, new String[] {
 				"setcat",
 				"cat",
 				"cattype"
 		});
 		addCommand("setcharged", charged, new String[] {
 				"setcharge",
 				"charge",
 				"crepp"
 		});
 		addCommand("setcolor", color, new String[] {
 				"color"
 		});
 		addCommand("createentity", create, new String[] {
 				"create",
 				"new",
 				"makenew",
 				"summon"
 		});
 		addCommand("setcustomdamage", damage, new String[] {
 				"usedamage",
 				"setusingcustomdamage",
 				"usingcustomdamage",
 				"usecustomdamage",
 				"setusedamage",
 				"setusecustomdamage",
 				"norrismode"
 		});
 		addCommand("setdamageamount", damage, new String[] {
 				"setdamage",
 				"damage",
 				"damageamount",
 				"punishvalue",
 				"ownedfactor",
 				"kdquotient"
 		});
 		addCommand("adddrop", drops, new String[] {
 				"adddrops",
 				"addd"
 		});
 		addCommand("setdrop", drops, new String[] {
 				"setdrops",
 				"setd",
 				"dropthis"
 		});
 		addCommand("cleardrop", drops, new String[] {
 				"cleardrops",
 				"cleard",
 				"nodrop",
 				"nodrops"
 		});
 		addCommand("usedrop", drops, new String[] {
 				"usedrops",
 				"setusingdrops",
 				"setusingcustomdrops",
 				"dropstuff"
 		});
 		addCommand("setendermanblock", ender, new String[] {
 				"setenderblock",
 				"endermanblock",
 				"enderblock",
 				"endermanholds"
 		});
 		addCommand("setdroppedexperience", experience, new String[] {
 				"setdroppedexp",
 				"setdroppedxp",
 				"setexperience",
 				"setexp",
 				"setxp",
 				"exp",
 				"xp"
 		});
 		addCommand("setfireticks", fire, new String[] {
 				"setfire",
 				"fireticks",
 				"fire"
 		});
 		addCommand("setfuseticks", fuse, new String[] {
 				"setfuse",
 				"fuseticks",
 				"fuse"
 		});
 		addCommand("sethealth", health, new String[] {
 				"health",
 				"sethp",
 				"hp",
 				"setlifepoints",
 				"setlife",
 				"setlp",
 				"lp"
 		});
 		addCommand("setincendiary", incendiary, new String[] {
 				"incendiary",
 				"setnapalm",
 				"napalm",
 				"setfireexplosion",
 				"fireexplosion",
 				"setfirebomb",
 				"firebomb"
 		});
 		addCommand("info", info, new String[] {
 				"getinfo",
 				"geti",
 				"i"
 		});
 		addCommand("clearinventory", inventory, new String[] {
 				"clearinv",
 				"noinventory",
 				"noinv"
 		});
 		addCommand("addinventoryitem", inventory, new String[] {
 				"addinvitem",
 				"addinv",
 				"additem"
 		});
 		addCommand("setinventory", inventory, new String[] {
 				"setinv"
 		});
 		addCommand("sethand", inventory, new String[] {
 				"hand",
 				"setholding",
 				"holding",
 				"hold",
 				"setequippeditem",
 				"setequipped",
 				"setiteminhand"
 		});
 		addCommand("sethelmet", inventory, new String[] {
 				"helmet",
 				"sethat",
 				"hat",
 				"sethead",
 				"head",
 				"sethelm",
 				"helm"
 		});
 		addCommand("setchest", inventory, new String[] {
 				"chest",
 				"setshirt",
 				"shirt",
 				"settorso",
 				"torso",
 				"setchestplate",
 				"chestplate"
 		});
 		addCommand("setleggings", inventory, new String[] {
 				"leggings",
 				"setlegs",
 				"legs",
 				"setpants",
 				"pants"
 		});
 		addCommand("setboots", inventory, new String[] {
 				"boots",
 				"setshoes",
 				"shoes",
 				"setfeet",
 				"feet"
 		});
 		addCommand("setinvulnerable", invulnerable, new String[] {
 				"invulnerable",
 				"setinvul",
 				"invul",
 				"setinvincible",
 				"invincible",
 				"nohurt"
 		});
 		addCommand("additemdamage", itemlist, new String[] {
 				"additemlist"
 		});
 		addCommand("setitemdamage", itemlist, new String[] {
 				"setitemlist",
 				"itemlist"
 		});
 		addCommand("clearitemlist", itemlist, new String[] {
 				"clearitemdamage"
 		});
 		addCommand("setitemtype", itemType, new String[] {
 				"itemtype",
 				"item"
 		});
 		addCommand("setjockey", jockey, new String[] {
 				"jockey",
 				"spiderjockey",
 				"skeletonjockey",
 				"cowboyskeleton",
 				"ghostrider"
 		});
 		addCommand("listallentities", list, new String[] {
 				"listall",
 				"list",
 				"showentities",
 				"displayentities",
 				"show",
 				"display"
 		});
 		addCommand("setname", name, new String[] {
 				"name",
 				"callit",
 				"displayname"
 		});
 		addCommand("setpassive", passive, new String[] {
 				"passive",
 				"provokeattack",
 				"noattack"
 		});
 		addCommand("setpotiontype", potionType, new String[] {
 				"potiontype",
 				"potioneffect",
 				"setpotion",
 				"potion"
 		});
 		addCommand("setvillagerprofession", profession, new String[] {
 				"setprofession",
 				"profession",
 				"setvillagertype",
 				"setvillager",
 				"villagertype",
 				"villager",
 				"villagerjob",
 				"job"
 		});
 		addCommand("removeentity", remove, new String[] {
 				"remove",
 				"rem",
 				"deleteentity",
 				"delete",
 				"del"
 		});
 		addCommand("setsaddled", saddled, new String[] {
 				"saddled",
 				"setsaddle",
 				"saddle",
 				"cowboymode"
 		});
 		addCommand("selectentity", select, new String[] {
 				"select",
 				"sel",
 				"choose"
 		});
 		addCommand("setentitytype", type, new String[] {
 				"entitytype",
 				"settype",
 				"type",
 				"setentity",
 				"setmobtype",
 				"setmob",
 				"mobtype",
 				"mob",
 				"setmonstertype",
 				"setmonster",
 				"monster",
 				"setanimaltype",
 				"setanimal",
 				"animal"
 		});
 		addCommand("setsitting", sitting, new String[] {
 				"sitting",
 				"sit"
 		});
 		addCommand("setslimesize", slime, new String[] {
 				"slimesize",
 				"slime",
 				"setslime",
 				"setsize",
 				"size",
 				"howbig"
 		});
 		addCommand("settamed", tamed, new String[] {
 				"tamed",
 				"settame",
 				"tame",
 				"setdomesticated",
 				"domesticated",
 				"domesticate"
 		});
 		addCommand("setvelocity", vector, new String[] {
 				"velocity",
 				"velo",
 				"setvector",
 				"vector",
 				"vec",
 				"setdirection",
 				"direction",
 				"dir"
 		});
 		addCommand("setusewhitelist", whitelist, new String[] {
 				"setusingwhitelist",
 				"usewhitelist",
 				"usingwhitelist",
 				"usewhite"
 		});
 		addCommand("setwhitelist", whitelist, new String[] {
 				"whitelist",
 				"white",
 				"notimmuneto"
 		});
 		addCommand("addwhitelist", whitelist, new String[] {
 				"addwhitelistitem",
 				"addwhite",
 				"addb",
 				"addnotimmuneto"
 		});
 		addCommand("clearwhitelist", whitelist, new String[] {
 				"clearwhite",
 				"nowhite",
 				"clearnotimmuneto"
 		});
 		addCommand("setzombievillager", villager, new String[] {
 				"zombievillager",
 				"setzombienpc",
 				"zombienpc",
 				"setinfected",
 				"infected",
 				"setzombie",
 				"zombie",
 				"zombify",
 				"setvillagerzombie",
 				"villagerzombie"
 		});
 		addCommand("setwither", wither, new String[] {
 				"wither",
 				"setwitherskeleton",
 				"witherskeleton",
 				"setwitherskele",
 				"witherskele"
 		});
 		addCommand("setyield", yield, new String[] {
 				"yield",
 				"setexplosivepower",
 				"explosivepower",
 				"setexpower",
 				"expower"
 		});
 	}
 
 	@Override
 	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
 		
 		if(arg1.getName().equalsIgnoreCase("entities")) {
 			
 			SpawnableEntity entityRef = null;
 			String sub = arg3[0].toLowerCase();
 			String objId = "";
 			String[] params;
 			
 			if(arg3.length > 1)
 				objId = arg3[1];
 
 			if(arg3.length == 0) {
 				PLUGIN.sendMessage(arg0, ChatColor.RED + "You must enter a command.");
 				return true;
 			}
 			
 			EntityCommand cmd = (EntityCommand) super.getCommand(sub);
 			
 			if(cmd == null) {
 				PLUGIN.sendMessage(arg0, ChatColor.RED + "\"" + sub + "\" is not valid for the entities command.");
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
 					
 					if(!CustomSpawners.entitySelection.containsKey(p)) {
 						if(objId.startsWith("t:")) {
 							entityRef = CustomSpawners.getEntity(objId.substring(2));
 							params = makeParams(arg3, 2);
 						} else {
 							entityRef = CustomSpawners.getEntity(objId);
 							params = makeParams(arg3, 2);
 						}
 					} else {
 						if(objId.startsWith("t:")) { //If they want to target a specific object
 							entityRef = CustomSpawners.getEntity(objId.substring(2));
 							params = makeParams(arg3, 2);
 						} else {
 							entityRef = CustomSpawners.getEntity(CustomSpawners.entitySelection.get(p));
 							params = makeParams(arg3, 1);
 						}
 					}
 				} else {
 					if(CustomSpawners.consoleEntity == -1) {
 						if(objId.startsWith("t:")) {
 							entityRef = CustomSpawners.getEntity(objId.substring(2));
 							params = makeParams(arg3, 2);
 						} else {
 							entityRef = CustomSpawners.getEntity(objId);
 							params = makeParams(arg3, 2);
 						}
 					} else {
 						if(objId.startsWith("t:")) { //If they want to target a specific object
 							entityRef = CustomSpawners.getEntity(objId.substring(2));
 							params = makeParams(arg3, 2);
 						} else {
 							entityRef = CustomSpawners.getEntity(CustomSpawners.consoleEntity);
 							params = makeParams(arg3, 1);
 						}
 					}
 				}
 				
 				if(entityRef == null) {
 					PLUGIN.sendMessage(arg0, cmd.NO_ENTITY);
 					return true;
 				}
 			} else {
 				params = makeParams(arg3, 1);
 			}
 			
 			try {
 				cmd.run(entityRef, arg0, sub, params);
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
