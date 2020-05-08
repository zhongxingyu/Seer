 package couk.Adamki11s.Regios.Commands;
 
 import java.io.File;
 import java.io.IOException;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.getspout.spoutapi.player.SpoutPlayer;
 import couk.Adamki11s.Regios.CustomEvents.RegionCommandEvent;
 import couk.Adamki11s.Regios.Data.OldRegiosPatch;
 import couk.Adamki11s.Regios.Listeners.RegiosPlayerListener;
 import couk.Adamki11s.Regios.Main.Regios;
 import couk.Adamki11s.Regios.Mutable.Zippable;
 import couk.Adamki11s.Regios.Permissions.PermissionsCore;
 import couk.Adamki11s.Regios.RBF.RBF_Core;
 import couk.Adamki11s.Regios.RBF.ShareData;
 import couk.Adamki11s.Regios.Regions.GlobalRegionManager;
 import couk.Adamki11s.Regios.SpoutGUI.RegionScreenManager;
 import couk.Adamki11s.Regios.SpoutGUI.ScreenHolder;
 import couk.Adamki11s.Regios.SpoutInterface.SpoutInterface;
 
 public class CommandCore implements CommandExecutor {
 
 	final AdministrationCommands admin = new AdministrationCommands();
 	final AuthenticationCommands auth = new AuthenticationCommands();
 	final CreationCommands creation = new CreationCommands();
 	final DebugCommands debug = new DebugCommands();
 	final EconomyCommands eco = new EconomyCommands();
 	final ExceptionCommands excep = new ExceptionCommands();
 	final FunCommands fun = new FunCommands();
 	final HelpCommands help = new HelpCommands();
 	final InfoCommands info = new InfoCommands();
 	final MessageCommands msg = new MessageCommands();
 	final MiscCommands miscCmd = new MiscCommands();
 	final MobCommands mobs = new MobCommands();
 	final ModeCommands mode = new ModeCommands();
 	final ModificationCommands mod = new ModificationCommands();
 	final InventoryCommands invent = new InventoryCommands();
 	final PermissionsCommands perms = new PermissionsCommands();
 	final ProtectionCommands protect = new ProtectionCommands();
 	final ProtectionMiscCommands misc = new ProtectionMiscCommands();
 	final SpoutCommands spout = new SpoutCommands();
 	final WarpCommands warps = new WarpCommands();
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 
 		if (label.equalsIgnoreCase("regios") || label.equalsIgnoreCase("reg") || label.equalsIgnoreCase("r")) {
 			
 			if (!(sender instanceof Player)) {
 				System.out.println("[Regios] Regios doesn't support console commands yet. Please log in and excute your command as a player.");
 				return true;
 			}
 
 			Player p = (Player) sender;
 			
 			if (args.length == 0) {
 				if (SpoutInterface.doesPlayerHaveSpout(p)) {
 					ScreenHolder sh = ScreenHolder.getScreenHolder((SpoutPlayer) p);
 					sh.addScreenHolder((SpoutPlayer)p, sh);
 					new SpoutHelp().getSpoutHelp((SpoutPlayer) p, sh);
 					return true;
 				} else {
 					help.getStandardHelp(p, args);
 					return true;
 				}
 			}
 
 			if (args.length >= 1 && args[0].equalsIgnoreCase("help")) {
 				if (SpoutInterface.doesPlayerHaveSpout(p)) {
 					if(!SpoutInterface.global_spoutEnabled){
 						p.sendMessage(ChatColor.RED + "The Spout server plugin is required for this feature!");
 						return true;
 					}
 					ScreenHolder sh = ScreenHolder.getScreenHolder((SpoutPlayer) p);
 					sh.addScreenHolder((SpoutPlayer)p, sh);
 					new SpoutHelp().getSpoutHelp((SpoutPlayer) p, sh);
 					return true;
 				} else {
 					help.getStandardHelp(p, args);
 					return true;
 				}
 			}
 
 			if (args.length == 1 && args[0].equalsIgnoreCase("set")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.data.create")) {
 					creation.giveTool(p);
 					return true;
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 					return true;
 				}
 			}
 			
 			if (args.length == 2 && args[0].equalsIgnoreCase("plot")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.data.plot")) {
 					miscCmd.createAllotment(p, args[1], GlobalRegionManager.getRegion(args[1]));
 					return true;
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 					return true;
 				}
 			}
 
 			if (args.length == 2 && args[0].equalsIgnoreCase("edit")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.data.edit")) {
 					if(!SpoutInterface.global_spoutEnabled){
 						p.sendMessage(ChatColor.RED + "The Spout server plugin is required for this feature!");
 						return true;
 					}
 					if (SpoutInterface.doesPlayerHaveSpout(p)) {
 						if (GlobalRegionManager.getRegion(args[1]) == null) {
 							p.sendMessage(ChatColor.RED + "[Regios] This region does not exist!");
 							return true;
 						} else {
 							ScreenHolder sh = ScreenHolder.getScreenHolder((SpoutPlayer) p);
 							sh.addScreenHolder((SpoutPlayer)p, sh);
 							((SpoutPlayer) p).sendNotification("Editing Region", ChatColor.GREEN + args[1], Material.FENCE);
 							RegionScreenManager.drawPanelFramework((SpoutPlayer) p, GlobalRegionManager.getRegion(args[1]), sh);
 							return true;
 						}
 					} else {
 						p.sendMessage(ChatColor.RED + "[Regios] The Spoutcraft launcher is required for this feature!");
 						return true;
 					}
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 					return true;
 				}
 			}
 
 			if (args.length == 1 && args[0].equalsIgnoreCase("patch")) {
 				if (p.isOp()) {
 					OldRegiosPatch.runPatch(p);
 					return true;
 				} else {
 					p.sendMessage(ChatColor.RED + "You must be an OP to do this!");
 					return true;
 				}
 			}
 
 			if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.data.create")) {
 					if (args[1].equalsIgnoreCase("all") || args[1].equalsIgnoreCase("placeholder") || args[1].equalsIgnoreCase("confirm")) {
 						p.sendMessage(ChatColor.RED + "[Regios] " + ChatColor.BLUE + args[1] + ChatColor.RED + " is a reserved word!");
 						return true;
 					}
 					creation.createRegion(p, args[1]);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 					return true;
 				}
 			}
 
 			if (args.length == 1 && args[0].equalsIgnoreCase("cancel")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.data.create")) {
 					CreationCommands.clearAll(p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && args[0].equalsIgnoreCase("auth")) {
 				auth.sendPassword(p, args[1], RegiosPlayerListener.regionBinding.get(p));
 			}
 
 			if (args.length >= 2 && (args[0].equalsIgnoreCase("warpto") || args[0].equalsIgnoreCase("warp-to") || args[0].equalsIgnoreCase("warp"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.fun.warpto")) {
 					warps.warpToRegion(args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			/*
 			 * Info
 			 */
 
 			if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.data.info")) {
 					info.showInfo(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			/*
 			 * Info
 			 */
 
 			/*
 			 * Messages
 			 */
 
 			if (args.length >= 3 && args[0].equalsIgnoreCase("set-welcome")) {
 
 				if (PermissionsCore.doesHaveNode(p, "regios.messages.set-welcome")) {
 					msg.setWelcome(GlobalRegionManager.getRegion(args[1]), args[1], args, p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length >= 3 && args[0].equalsIgnoreCase("set-leave")) {
 
 				if (PermissionsCore.doesHaveNode(p, "regios.messages.set-leave")) {
 					msg.setLeave(GlobalRegionManager.getRegion(args[1]), args[1], args, p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length >= 3 && args[0].equalsIgnoreCase("set-prevent-exit")) {
 
 				if (PermissionsCore.doesHaveNode(p, "regios.messages.set-prevent-exit")) {
 					msg.setPreventExit(GlobalRegionManager.getRegion(args[1]), args[1], args, p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length >= 3 && args[0].equalsIgnoreCase("set-prevent-entry")) {
 
 				if (PermissionsCore.doesHaveNode(p, "regios.messages.set-prevent-entry")) {
 					msg.setPreventEntry(GlobalRegionManager.getRegion(args[1]), args[1], args, p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length >= 3 && args[0].equalsIgnoreCase("set-protection")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.messages.set-protection")) {
 					msg.setProtection(GlobalRegionManager.getRegion(args[1]), args[1], args, p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("show-welcome")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.messages.set-welcome")) {
 					msg.setShowWelcome(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("show-leave")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.messages.set-leave")) {
 					msg.setShowLeave(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("show-prevent-entry")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.messages.set-prevent-entry")) {
 					msg.setShowPreventEntry(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("show-prevent-exit")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.messages.set-prevent-exit")) {
 					msg.setShowPreventExit(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("show-protection")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.messages.set-protection")) {
 					msg.setShowProtection(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("show-pvp")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.messages.set-pvp-toggle")) {
 					msg.setShowPvpWarning(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			/*
 			 * Messages
 			 */
 
 			/*
 			 * Mobs
 			 */
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("set-creature-spawns")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.other.creature-spawns")) {
 					mobs.setMobSpawn(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("set-monster-spawns")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.other.monster-spawns")) {
 					mobs.setMonsterSpawn(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			/*
 			 * Mobs
 			 */
 
 			/*
 			 * Protection
 			 */
 
 			if (args.length == 2 && args[0].equalsIgnoreCase("protect")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.protection.protection")) {
 					protect.setProtected(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 			
 			if (args.length == 2 && (args[0].equalsIgnoreCase("protectall") || args[0].equalsIgnoreCase("protect-all"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.protection.protection")) {
 					protect.setProtectedAll(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 			
 			if (args.length == 2 && args[0].equalsIgnoreCase("protect-bb")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.protection.protection")) {
 					protect.setProtectedBB(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 			
 			if (args.length == 2 && args[0].equalsIgnoreCase("protect-bp")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.protection.protection")) {
 					protect.setProtectedBP(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && (args[0].equalsIgnoreCase("unprotect") || args[0].equalsIgnoreCase("un-protect"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.protection.protection")) {
 					protect.setUnProtected(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 			
 			if (args.length == 2 && (args[0].equalsIgnoreCase("unprotectall") || args[0].equalsIgnoreCase("un-protect-all") || args[0].equalsIgnoreCase("unprotect-all"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.protection.protection")) {
 					protect.setUnProtectAll(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 			
 			if (args.length == 2 && args[0].equalsIgnoreCase("unprotect-bb")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.protection.protection")) {
 					protect.setUnProtectedBB(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 			
 			if (args.length == 2 && args[0].equalsIgnoreCase("unprotect-bp")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.protection.protection")) {
 					protect.setUnProtectedBP(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && args[0].equalsIgnoreCase("prevent-exit")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.protection.exit-control")) {
 					protect.setPreventExit(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && args[0].equalsIgnoreCase("prevent-entry")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.protection.entry-control")) {
 					protect.setPreventEntry(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && args[0].equalsIgnoreCase("allow-exit")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.protection.exit-control")) {
 					protect.setAllowExit(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && args[0].equalsIgnoreCase("allow-entry")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.protection.entry-control")) {
 					protect.setAllowEntry(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			/*
 			 * Protection
 			 */
 
 			/*
 			 * Region Modification
 			 */
 
 			if (args.length == 2 && (args[0].equalsIgnoreCase("modify") && !args[1].equalsIgnoreCase("confirm"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.modify.modify")) {
 					mod.startModification(GlobalRegionManager.getRegion(args[1]), args[1], args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && (args[0].equalsIgnoreCase("modify") && args[1].equalsIgnoreCase("confirm"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.modify.modify")) {
 					mod.setModifyPoints(CreationCommands.mod1.get(p), CreationCommands.mod2.get(p), p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.data.delete")) {
 					mod.setDelete(GlobalRegionManager.getRegion(args[1]), args[1], args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("expand-down")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.modify.expand")) {
 					mod.setExpandDown(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && (args[0].equalsIgnoreCase("expand-max") || args[0].equalsIgnoreCase("expandmax"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.modify.expand")) {
 					mod.setExpandMax(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 			
 			if (args.length == 1 && (args[0].equalsIgnoreCase("expand-max") || args[0].equalsIgnoreCase("expandmax"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.modify.expand")) {
 					creation.expandMaxSelection(p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("expand-up")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.modify.expand")) {
 					mod.setExpandUp(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("expand-out")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.modify.expand")) {
 					mod.setExpandOut(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("rename")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.data.rename")) {
					if (args[3].equalsIgnoreCase("all") || args[1].equalsIgnoreCase("placeholder") || args[1].equalsIgnoreCase("confirm")) {
						p.sendMessage(ChatColor.RED + "[Regios] " + ChatColor.BLUE + args[1] + ChatColor.RED + " is a reserved word!");
 						return true;
 					}
 					mod.setRename(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("shrink-down")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.modify.shrink")) {
 					mod.setShrinkDown(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("shrink-up")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.modify.shrink")) {
 					mod.setShrinkUp(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("shrink-in")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.modify.shrink")) {
 					mod.setShrinkIn(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			/*
 			 * Region Modification
 			 */
 
 			/*
 			 * Exceptions
 			 */
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("addex") || args[0].equalsIgnoreCase("add-ex"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.exceptions.players")) {
 					excep.addPlayerException(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("remex") || args[0].equalsIgnoreCase("rem-ex"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.exceptions.players")) {
 					excep.removePlayerException(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("additemex") || args[0].equalsIgnoreCase("add-item-ex"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.exceptions.items")) {
 					excep.addItemException(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("remitemex") || args[0].equalsIgnoreCase("rem-item-ex"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.exceptions.items")) {
 					excep.removeItemException(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("addnodeex") || args[0].equalsIgnoreCase("add-node-ex"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.modify.nodes")) {
 					excep.addNodeException(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("remnodeex") || args[0].equalsIgnoreCase("rem-node-ex"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.modify.nodes")) {
 					excep.removeNodeException(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("addsubex") || args[0].equalsIgnoreCase("add-sub-ex"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.modify.players")) {
 					excep.addSubOwner(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("remsubex") || args[0].equalsIgnoreCase("rem-sub-ex"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.modify.players")) {
 					excep.removeSubowner(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && (args[0].equalsIgnoreCase("erase-player-exceptions") || args[0].equalsIgnoreCase("erase-player-ex"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.modify.players")) {
 					excep.erasePlayerExceptions(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && (args[0].equalsIgnoreCase("erase-node-exceptions") || args[0].equalsIgnoreCase("erase-node-ex"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.modify.nodes")) {
 					excep.erasePlayerExceptions(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && (args[0].equalsIgnoreCase("erase-item-exceptions") || args[0].equalsIgnoreCase("erase-item-ex"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.modify.items")) {
 					excep.eraseItemExceptions(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && (args[0].equalsIgnoreCase("erase-sub-exceptions") || args[0].equalsIgnoreCase("erase-sub-ex"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.modify.players")) {
 					excep.eraseSubOwnerExceptions(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && (args[0].equalsIgnoreCase("list-player-exceptions") || args[0].equalsIgnoreCase("list-player-ex"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.modify.players")) {
 					excep.listPlayerExceptions(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && (args[0].equalsIgnoreCase("list-node-exceptions") || args[0].equalsIgnoreCase("list-node-ex"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.modify.nodes")) {
 					excep.listNodeExceptions(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && ((args[0].equalsIgnoreCase("list-item-exceptions") || args[0].equalsIgnoreCase("list-item-ex")))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.modify.items")) {
 					excep.listItemExceptions(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && ((args[0].equalsIgnoreCase("list-sub-exceptions") || args[0].equalsIgnoreCase("list-item-ex")))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.modify.players")) {
 					excep.listSubOwnerExceptions(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			/*
 			 * Exceptions
 			 */
 
 			/*
 			 * Economy
 			 */
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("for-sale")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.fun.sell")) {
 					eco.setForSale(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("set-price")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.fun.sell")) {
 					eco.setSalePrice(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && (args[0].equalsIgnoreCase("buy") || args[0].equalsIgnoreCase("buy-region"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.fun.buy")) {
 					eco.buyRegion(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 1 && (args[0].equalsIgnoreCase("list-for-sale") || args[0].equalsIgnoreCase("list-forsale"))) {
 				eco.listRegionsForSale(p);
 			}
 
 			/*
 			 * Economy
 			 */
 
 			/*
 			 * Fun
 			 */
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("lsps")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.fun.lsps")) {
 					fun.setLSPS(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("pvp")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.fun.pvp")) {
 					fun.setPvP(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("healthregen") || args[0].equalsIgnoreCase("health-regen"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.fun.health-regen")) {
 					fun.setHealthRegen(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("health") || args[0].equalsIgnoreCase("health-enabled"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.fun.health")) {
 					fun.setHealthEnabled(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("vel-warp") || args[0].equalsIgnoreCase("velocity-warp"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.fun.vel-warp")) {
 					fun.setVelocityWarp(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 1 && (args[0].equalsIgnoreCase("set-warp") || args[0].equalsIgnoreCase("setwarp"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.fun.set-warp")) {
 					fun.setWarp(p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && (args[0].equalsIgnoreCase("reset-warp") || args[0].equalsIgnoreCase("resetwarp"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.fun.set-warp")) {
 					fun.resetWarp(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			/*
 			 * Fun
 			 */
 
 			/*
 			 * Misc. Protection
 			 */
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("preventinteraction") || args[0].equalsIgnoreCase("prevent-interaction"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.protection.prevent-interaction")) {
 					misc.setInteraction(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("doorslocked") || args[0].equalsIgnoreCase("doors-locked"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.protection.doors-locked")) {
 					misc.setDoorsLocked(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("chestslocked") || args[0].equalsIgnoreCase("chests-locked"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.protection.chests-locked")) {
 					misc.setChestsLocked(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("setpassword") || args[0].equalsIgnoreCase("set-password"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.protection.set-password")) {
 					misc.setPassword(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("usepassword") || args[0].equalsIgnoreCase("use-password"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.protection.set-password")) {
 					misc.setPasswordEnabled(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("fireprotection") || args[0].equalsIgnoreCase("fire-protection"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.protection.fire-protection")) {
 					misc.setFireProtection(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("playercap") || args[0].equalsIgnoreCase("player-cap"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.protection.player-cap")) {
 					misc.setPlayerCap(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("blockform") || args[0].equalsIgnoreCase("block-form"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.protection.block-form")) {
 					misc.setBlockForm(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			/*
 			 * Misc. Protection
 			 */
 
 			/*
 			 * Modes
 			 */
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("protection-mode") || args[0].equalsIgnoreCase("protectionmode"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.mode.protection")) {
 					mode.setProtectionMode(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("prevent-entry-mode") || args[0].equalsIgnoreCase("prevententry"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.mode.prevent-entry")) {
 					mode.setPreventEntryMode(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("prevent-exit-mode") || args[0].equalsIgnoreCase("preventexit"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.mode.prevent-exit")) {
 					mode.setPreventExitMode(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("item-mode") || args[0].equalsIgnoreCase("itemmode"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.mode.items")) {
 					mode.setItemControlMode(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			/*
 			 * Modes
 			 */
 
 			/*
 			 * Inventory
 			 */
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("perm-wipe-entry") || args[0].equalsIgnoreCase("perm-wipe-enter"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.inventory.perm-wipe")) {
 					invent.setPermWipeOnEntry(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("perm-wipe-exit") || args[0].equalsIgnoreCase("perm-wipe-leave"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.inventory.perm-wipe")) {
 					invent.setPermWipeOnExit(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("cache-wipe-entry") || args[0].equalsIgnoreCase("cache-wipe-enter"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.inventory.cache-wipe")) {
 					invent.setWipeAndCacheOnEntry(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("cache-wipe-exit") || args[0].equalsIgnoreCase("cache-wipe-leave"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.inventory.cache-wipe")) {
 					invent.setWipeAndCacheOnExit(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			/*
 			 * Inventory
 			 */
 
 			/*
 			 * Permissions
 			 */
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("perm-cache-add"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.permissions.cache")) {
 					perms.addToTempCache(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("perm-add-add"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.permissions.perm-add")) {
 					perms.addToPermAddCache(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("perm-rem-add"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.permissions.perm-rem")) {
 					perms.addToPermRemCache(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("perm-cache-rem"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.permissions.cache")) {
 					perms.removeFromTempCache(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("perm-add-rem"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.permissions.perm-add")) {
 					perms.removeFromPermAddCache(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("perm-rem-rem"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.permissions.perm-rem")) {
 					perms.removeFromPermRemCache(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && (args[0].equalsIgnoreCase("perm-cache-reset"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.permissions.cache")) {
 					perms.resetTempAddCache(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && (args[0].equalsIgnoreCase("perm-add-reset"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.permissions.perm-add")) {
 					perms.resetPermAddCache(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && (args[0].equalsIgnoreCase("perm-rem-reset"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.permissions.perm-rem")) {
 					perms.resetPermRemCache(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && (args[0].equalsIgnoreCase("perm-rem-list"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.permissions.perm-rem")) {
 					perms.listPermRemCache(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && (args[0].equalsIgnoreCase("perm-add-list"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.permissions.perm-add")) {
 					perms.listPermAdd(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && (args[0].equalsIgnoreCase("perm-rem-list"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.permissions.perm-rem")) {
 					perms.listPermRemCache(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && (args[0].equalsIgnoreCase("perm-cache-list"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.permissions.cache")) {
 					perms.listTempAddCache(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			/*
 			 * Permissions
 			 */
 
 			/*
 			 * Spout
 			 */
 
 			if (args.length >= 3 && args[0].equalsIgnoreCase("set-spout-welcome")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.spout.messages")) {
 					spout.setWelcome(GlobalRegionManager.getRegion(args[1]), args[1], args, p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 			
 			if (args.length >= 3 && args[0].equalsIgnoreCase("show-spout-welcome")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.spout.messages")) {
 					spout.setUseWelcome(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length >= 3 && args[0].equalsIgnoreCase("set-spout-leave")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.spout.messages")) {
 					spout.setLeave(GlobalRegionManager.getRegion(args[1]), args[1], args, p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 			
 			if (args.length >= 3 && args[0].equalsIgnoreCase("show-spout-leave")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.spout.messages")) {
 					spout.setUseLeave(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("spout-welcome-id")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.spout.messages")) {
 					spout.setWelcomeMaterial(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("spout-leave-id")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.spout.messages")) {
 					spout.setLeaveMaterial(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("spout-texture-url")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.spout.texture")) {
 					spout.setTexturePackURL(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("use-texture-url")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.spout.texture")) {
 					spout.setUseTextures(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("use-music-url")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.spout.music")) {
 					spout.setUseMusic(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("add-music-url")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.spout.music")) {
 					spout.setAddMusic(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("rem-music-url")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.spout.music")) {
 					spout.setRemoveMusic(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && args[0].equalsIgnoreCase("reset-music-url")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.spout.music")) {
 					spout.setResetMusic(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			/*
 			 * Spout
 			 */
 
 			/*
 			 * Misc. Cmd
 			 */
 
 			if (args.length == 1 && args[0].equalsIgnoreCase("check")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.other.check")) {
 					miscCmd.checkRegion(p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length >= 3 && args[0].equalsIgnoreCase("add-cmd-set")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.other.cmd-set")) {
 					miscCmd.addToCommandSet(GlobalRegionManager.getRegion(args[1]), args[1], args, p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length >= 3 && args[0].equalsIgnoreCase("rem-cmd-set")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.other.cmd-set")) {
 					miscCmd.removeFromCommandSet(GlobalRegionManager.getRegion(args[1]), args[1], args, p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && args[0].equalsIgnoreCase("reset-cmd-set")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.other.cmd-set")) {
 					miscCmd.resetCommandSet(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && args[0].equalsIgnoreCase("list-cmd-set")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.other.cmd-set")) {
 					miscCmd.listCommandSet(GlobalRegionManager.getRegion(args[1]), args[1], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("use-cmd-set")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.other.cmd-set")) {
 					miscCmd.setForceCommand(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			/*
 			 * Misc. Cmd
 			 */
 
 			/*
 			 * Administration
 			 */
 
 			if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.data.reload")) {
 					admin.reloadAll(p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 1 && args[0].equalsIgnoreCase("reload-regions")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.data.reload")) {
 					admin.reloadRegions(p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 1 && args[0].equalsIgnoreCase("reload-config")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.data.reload")) {
 					admin.reloadConfig(p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 1 && args[0].equalsIgnoreCase("version")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.data.version")) {
 					p.sendMessage(ChatColor.GREEN + "[Regios] Running version : " + ChatColor.BLUE + Regios.version);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("set-owner")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.data.set-owner")) {
 					admin.setOwner(GlobalRegionManager.getRegion(args[1]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.data.list")) {
 					admin.listRegions(p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && args[0].equalsIgnoreCase("inherit")) {
 				if (PermissionsCore.doesHaveNode(p, "regios.data.inherit")) {
 					admin.inherit(GlobalRegionManager.getRegion(args[1]), GlobalRegionManager.getRegion(args[2]), args[1], args[2], p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 2 && (args[0].equalsIgnoreCase("list-backups") || args[0].equalsIgnoreCase("list-backup"))) {
 				admin.listRegionBackups(GlobalRegionManager.getRegion(args[1]), args[1], p);
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("backup-region") || args[0].equalsIgnoreCase("save-region"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.data.backup-region")) {
 					RBF_Core.rbf_save.startSave(GlobalRegionManager.getRegion(args[1]), null, null, args[2], p, false);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 			
 			if (args.length == 2 && (args[0].equalsIgnoreCase("save-blueprint") || args[0].equalsIgnoreCase("saveblueprint"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.data.save-blueprint")) {
 					creation.createTerrain(p, args[1]);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 			
 			if (args.length == 2 && (args[0].equalsIgnoreCase("load-blueprint") || args[0].equalsIgnoreCase("loadblueprint"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.data.load-blueprint")) {
 					File f = new File("plugins" + File.separator + "Regios" + File.separator + "Blueprints" + File.separator + args[1] + ".blp");
 
 					if (!f.exists()) {
 						p.sendMessage(ChatColor.RED + "[Regios] A Blueprint file with the name " + ChatColor.BLUE + args[1] + ChatColor.RED + " does not exist!");
 						return true;
 					}
 					RegiosPlayerListener.loadingTerrain.put(p, new ShareData(args[1], p));
 					p.sendMessage(ChatColor.GREEN + "[Regios] Click the block where you wish to paste the blueprint.");
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 			
 			if (args.length == 1 && (args[0].equalsIgnoreCase("undo"))) {
 				if (PermissionsCore.doesHaveNode(p, "regios.data.load-blueprint")) {
 					RBF_Core.rbf_load_share.undoLoad(p);
 				} else {
 					PermissionsCore.sendInvalidPerms(p);
 				}
 			}
 
 			if (args.length == 3 && (args[0].equalsIgnoreCase("restore-region") || args[0].equalsIgnoreCase("load-region"))) {
 				try {
 					if (PermissionsCore.doesHaveNode(p, "regios.data.restore-region")) {
 						RBF_Core.rbf_load.loadRegion(GlobalRegionManager.getRegion(args[1]), args[2], p);
 					} else {
 						PermissionsCore.sendInvalidPerms(p);
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 
 			if (args.length == 2 && args[0].equalsIgnoreCase("backup-database")) {
 				try {
 					if (PermissionsCore.doesHaveNode(p, "regios.data.backup-database")) {
 						Zippable.zipDir(new File("plugins" + File.separator + "Regios" + File.separator + "Database"), new File("plugins" + File.separator + "Regios"
 								+ File.separator + "Backups" + File.separator + args[1] + ".zip"), args[1], p);
 					} else {
 						PermissionsCore.sendInvalidPerms(p);
 					}
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			
 			RegionCommandEvent event = new RegionCommandEvent("RegionCommandEvent");
 			event.setProperties(sender, label, args);
 			Bukkit.getServer().getPluginManager().callEvent(event);
 
 			return true;
 		}
 
 		return true;
 	}
 }
