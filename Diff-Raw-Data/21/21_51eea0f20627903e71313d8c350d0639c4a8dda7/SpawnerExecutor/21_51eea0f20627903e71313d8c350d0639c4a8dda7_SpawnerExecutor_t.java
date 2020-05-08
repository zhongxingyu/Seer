 package com.github.thebiologist13;
 
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 
 import com.github.thebiologist13.commands.SpawnerCommand;
 import com.github.thebiologist13.commands.spawners.*;
 
 @SuppressWarnings("unused")
 public class SpawnerExecutor implements CommandExecutor {
 
 	private CustomSpawners plugin;
 	
 	private FileConfiguration config = null;
 	
 	private Logger log;
 	
 	//Sub-Commands
 	private ActivateAllCommand aac = null;
 	private AddTypeCommand atc = null;
 	private DeactivateAllCommand dac = null;
 	private ForceSpawnCommand fsc = null;
 	private RemoveMobsCommand rmc = null;
 	private NameCommand nac = null;
 	private ListNearCommand lnc = null;
 	private ListAllCommand lac = null;
 	private CreateCommand cc = null;
 	private SelectCommand sc = null;
 	private RemoveCommand rc = null;
 	private SetTypeCommand stc = null;
 	private ActiveCommand ac = null;
 	private HiddenCommand hic = null;
 	private LightLevelCommand llc = null;
 	private MaxMobsCommand mmc = null;
 	private PerSpawnCommand psc = null;
 	private PlayerDistanceCommand pdc = null;
 	private SetLocationCommand slc = null;
 	private SetRadiusCommand src = null;
 	private SetRateCommand srac = null;
 	private SetRedstoneCommand srec = null;
 	private InfoCommand ic = null;
 	
 	public SpawnerExecutor(CustomSpawners plugin) {
 		this.plugin = plugin;
 		this.log = plugin.log;
 		
 		aac = new ActivateAllCommand(plugin);
 		atc = new AddTypeCommand(plugin);
 		dac = new DeactivateAllCommand(plugin);
 		fsc = new ForceSpawnCommand(plugin);
 		rmc = new RemoveMobsCommand(plugin);
 		nac = new NameCommand(plugin);
 		lnc = new ListNearCommand(plugin);
 		lac = new ListAllCommand(plugin);
 		cc = new CreateCommand(plugin);
 		sc = new SelectCommand(plugin);
 		rc = new RemoveCommand(plugin);
 		stc = new SetTypeCommand(plugin);
 		llc = new LightLevelCommand(plugin);
 		mmc = new MaxMobsCommand(plugin);
 		psc = new PerSpawnCommand(plugin);
 		pdc = new PlayerDistanceCommand(plugin);
 		slc = new SetLocationCommand(plugin);
 		src = new SetRadiusCommand(plugin);
 		srac = new SetRateCommand(plugin);
 		srec = new SetRedstoneCommand(plugin);
 		ic = new InfoCommand(plugin);
 		ac = new ActiveCommand(plugin);
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
 		
 		Player p = null;
 		
 		if(arg0 instanceof Player) {
 			p = (Player) arg0;
 		}
 		
 		if(arg1.getName().equalsIgnoreCase("spawners")) {
 			
 			//Catch any unhandled errors with commands.
 			try {
 				
 				if(arg3.length == 1) {
 
 					if(arg3[0].equalsIgnoreCase("listnear")) { //List nearby spawners command
 
 						lnc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("listall")) { //List all spawners command
 
 						lac.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("remove")) {
 
 						rc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setactive")) {
 
 						ac.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setinactive")) {
 
 						ac.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("sethidden")) {
 
 						hic.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setunhidden")) {
 
 						hic.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("info")) {
 
 						ic.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setlocation")) {
 
 						slc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("activateall")) {
 
 						aac.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("deactivateall")) {
 
 						dac.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("forcespawn")) {
 
 						fsc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("removeallmobs")) {
 
 						rmc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("removemobs")) {
 
 						rmc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					}
 
 				/*
 				 * Commands with 2 Arguments
 				 */
 
 				} else if(arg3.length == 2) {
 
 					if(arg3[0].equalsIgnoreCase("create")) { //Create Command
 
 						cc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("select")) { //Selection command
 
 						sc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("remove")) {
 
 						rc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("settype")) {
 
 						stc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setactive")) {
 
 						ac.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setinactive")) {
 
 						ac.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("sethidden")) {
 
 						hic.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setunhidden")) {
 
 						hic.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("info")) {
 
 						ic.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setmaxlight")) {
 
 						llc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setminlight")) {
 
 						llc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setmaxmobs")) {
 
 						mmc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setmobsperspawn")) {
 
 						psc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setmaxdistance")) {
 
 						pdc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setmindistance")) {
 
 						pdc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setlocation")) {
 
 						slc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setredstone")) {
 
 						srec.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setradius")) {
 
 						src.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setrate")) {
 
 						srac.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("addtype")) {
 
 						atc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("forcespawn")) {
 
 						fsc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("removemobs")) {
 
 						rmc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setname")) {
 
 						nac.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					}
 
 				/*
 				 * Commands with 3 Arguments
 				 */
 
 				} else if(arg3.length == 3) {
 					if(arg3[0].equalsIgnoreCase("setmaxlight")) {
 
 						llc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setminlight")) {
 
 						llc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setmaxmobs")) {
 
 						mmc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setmobsperspawn")) {
 
 						psc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setmaxdistance")) {
 
 						pdc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setmindistance")) {
 
 						pdc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setlocation")) {
 
 						slc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setradius")) {
 
 						src.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setrate")) {
 
 						srac.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setredstone")) {
 
 						srec.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("settype")) {
 
 						stc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setradius")) {
 
 						src.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setrate")) {
 
 						srac.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("addtype")) {
 
 						atc.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					} else if(arg3[0].equalsIgnoreCase("setname")) {
 
 						nac.run(arg0, arg1, arg2, arg3);
 						return true;
 
 					}
 					
 				} else {
 					if(arg0 instanceof Player) {
 						p = (Player) arg0;
 						p.sendMessage(SpawnerCommand.GENERAL_ERROR);
 					} else {
 						log.info("An error has occured with this command. Did you type everything right?");
 					}
 					return false;
 				}
 				
 				return false;
 			} catch(Exception e) {
 				if(arg0 instanceof Player) {
 					p = (Player) arg0;
 					p.sendMessage(SpawnerCommand.GENERAL_ERROR);
 					e.printStackTrace();
 				} else {
 					log.info("An error has occured with this command. Did you type everything right?");
 				}
 			}
 			
			if(config.getBoolean("data.autosave", true) && config.getBoolean("data.saveOnCommand", false)) {
 				plugin.autosaveAll();
 			}
 			
 			return true;
 		}
 		return false;
 	}
 }
