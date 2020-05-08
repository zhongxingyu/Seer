 
 package net.slipcor.pvparena.modules.vaultsupport;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 import net.milkbowl.vault.economy.Economy;
 import net.slipcor.pvparena.arena.ArenaPlayer;
 import net.slipcor.pvparena.arena.ArenaTeam;
 import net.slipcor.pvparena.arena.ArenaPlayer.Status;
 import net.slipcor.pvparena.classes.PACheck;
 import net.slipcor.pvparena.core.Language;
 import net.slipcor.pvparena.core.Config.CFG;
 import net.slipcor.pvparena.core.Language.MSG;
 import net.slipcor.pvparena.core.StringParser;
 import net.slipcor.pvparena.loadables.ArenaModule;
 import net.slipcor.pvparena.loadables.ArenaModuleManager;
 
 public class VaultSupport extends ArenaModule {
 
 	private static Economy economy = null;
 	private HashMap<String, Double> playerBetMap = null;
 	private HashMap<String, Double> playerJoinMap = null;
 	private double pot = 0;
 
 	public VaultSupport() {
 		super("Vault");
 	}
 
 	@Override
 	public String version() {
		return "v1.0.1.59";
 	}
 
 	@Override
 	public boolean checkCommand(String cmd) {
 		try {
 			double amount = Double.parseDouble(cmd);
 			debug.i("parsing join bet amount: " + amount);
 			return true;
 		} catch (Exception e) {
 			return cmd.equalsIgnoreCase("bet");
 		}
 
 	}
 
 	@Override
 	public PACheck checkJoin(CommandSender sender,
 			PACheck res, boolean join) {
 		
 		if (res.hasError() || !join) {
 			return res;
 		}
 		
 		if (arena.getArenaConfig().getInt(CFG.MODULES_VAULT_ENTRYFEE) > 0) {
 			if (economy != null) {
 				if (!economy.hasAccount(sender.getName())) {
 					debug.i("Account not found: " + sender.getName(), sender);
 					res.setError(this, "account not found: " + sender.getName());
 					return res;
 				}
 				if (!economy.has(sender.getName(),
 						arena.getArenaConfig().getInt(CFG.MODULES_VAULT_ENTRYFEE))) {
 					// no money, no entry!
 
 					res.setError(this, Language.parse(MSG.MODULE_VAULT_NOTENOUGH, economy
 							.format(arena.getArenaConfig().getInt(CFG.MODULES_VAULT_ENTRYFEE))));
 					return res;
 				}
 			}
 		}
 		return res;
 	}
 
 	@Override
 	public void commitCommand(CommandSender sender, String[] args) {
 		if (!(sender instanceof Player)) { //TODO move to new parseCommand
 			Language.parse(MSG.ERROR_ONLY_PLAYERS);
 			return;
 		}
 
 		Player player = (Player) sender;
 
 		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
 
 		// /pa bet [name] [amount]
 		if (ap.getArenaTeam() != null) {
 			arena.msg(player, Language.parse(MSG.MODULE_VAULT_BETNOTYOURS));
 			return;
 		}
 
 		if (economy == null)
 			return;
 
 		if (args[0].equalsIgnoreCase("bet")) {
 
 			Player p = Bukkit.getPlayer(args[1]);
 			if (p != null) {
 				ap = ArenaPlayer.parsePlayer(p.getName());
 			}
 			if ((p == null) && (arena.getTeam(args[1]) == null)
 					&& (ap.getArenaTeam() == null)) {
 				arena.msg(player, Language.parse(MSG.MODULE_VAULT_BETOPTIONS));
 				return;
 			}
 
 			double amount = 0;
 
 			try {
 				amount = Double.parseDouble(args[2]);
 			} catch (Exception e) {
 				arena.msg(player,
 						Language.parse(MSG.MODULE_VAULT_INVALIDAMOUNT, args[2]));
 				return;
 			}
 			if (!economy.hasAccount(player.getName())) {
 				debug.i("Account not found: " + player.getName(), sender);
 				return;
 			}
 			if (!economy.has(player.getName(), amount)) {
 				// no money, no entry!
 				arena.msg(player,
 						Language.parse(MSG.MODULE_VAULT_NOTENOUGH, economy.format(amount)));
 				return;
 			}
 
 			if (amount < arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_MINIMUMBET)
 					|| (amount > arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_MAXIMUMBET))) {
 				// wrong amount!
 				arena.msg(player, Language.parse(MSG.ERROR_INVALID_VALUE,
 						economy.format(arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_MINIMUMBET)),
 						economy.format(arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_MAXIMUMBET))));
 				return;
 			}
 
 			economy.withdrawPlayer(player.getName(), amount);
 			arena.msg(player, Language.parse(MSG.MODULE_VAULT_BETPLACED, args[1]));
 			getPlayerBetMap().put(player.getName() + ":" + args[1], amount);
 			return;
 		} else {
 
 			double amount = 0;
 
 			try {
 				amount = Double.parseDouble(args[0]);
 			} catch (Exception e) {
 				return;
 			}
 			if (!economy.hasAccount(player.getName())) {
 				debug.i("Account not found: " + player.getName(), sender);
 				return;
 			}
 			if (!economy.has(player.getName(), amount)) {
 				// no money, no entry!
 				arena.msg(player,
 						Language.parse(MSG.MODULE_VAULT_NOTENOUGH, economy.format(amount)));
 				return;
 			}
 			PACheck res = new PACheck();
 			checkJoin(sender, res, true);
 			
 			if (res.hasError()) {
 				arena.msg(sender, res.getError());
 				return;
 			}
 
 			economy.withdrawPlayer(player.getName(), amount);
			arena.msg(player, Language.parse(MSG.MODULE_VAULT_BETPLACED, args[1]));
 			getPlayerJoinMap().put(player.getName(), amount);
 			commitCommand(player, null);
 		}
 	}
 
 	@Override
 	public boolean commitEnd(ArenaTeam aTeam) {
 
 		if (economy != null) {
 			debug.i("eConomy set, parse bets");
 			for (String nKey : getPlayerBetMap().keySet()) {
 				debug.i("bet: " + nKey);
 				String[] nSplit = nKey.split(":");
 
 				if (arena.getTeam(nSplit[1]) == null
 						|| arena.getTeam(nSplit[1]).getName()
 								.equals("free"))
 					continue;
 
 				if (nSplit[1].equalsIgnoreCase(aTeam.getName())) {
 					double teamFactor = arena.getArenaConfig()
 							.getDouble(CFG.MODULES_VAULT_BETWINTEAMFACTOR)
 							* arena.getTeamNames().size();
 					if (teamFactor <= 0) {
 						teamFactor = 1;
 					}
 					teamFactor *= arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_BETWINFACTOR);
 
 					double amount = getPlayerBetMap().get(nKey) * teamFactor;
 
 					if (!economy.hasAccount(nSplit[0])) {
 						debug.i("Account not found: " + nSplit[0]);
 						return true;
 					}
 					economy.depositPlayer(nSplit[0], amount);
 					try {
 						arena.msg(Bukkit.getPlayer(nSplit[0]), Language
 								.parse(MSG.MODULE_VAULT_YOUWON, economy.format(amount)));
 					} catch (Exception e) {
 						// nothing
 					}
 				}
 			}
 		}
 		return false;
 	}
 	
 	@Override
 	public void onThisLoad() {
 		if (economy == null && Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
 			setupEconomy();
 		}
 	}
 	
 	/**
 	 * bettingPlayerName:betGoal => betAmount
 	 * @return
 	 */
 	private Map<String, Double> getPlayerBetMap() {
 		if (playerBetMap == null) {
 			playerBetMap = new HashMap<String, Double>();
 		}
 		return playerBetMap;
 	}
 	
 	/**
 	 * bettingPlayerName => joinBetAmount
 	 * @return
 	 */
 	private Map<String, Double> getPlayerJoinMap() {
 		if (playerJoinMap == null) {
 			playerJoinMap = new HashMap<String, Double>();
 		}
 		return playerJoinMap;
 	}
 	
 	@Override
 	public void giveRewards(Player player) {
 		debug.i("giving rewards to player " + player.getName(), player);
 		debug.i("", player);
 
 		int winners = 0;
 		debug.i("giving Vault rewards to Player " + player, player);
 		for (ArenaPlayer p : arena.getFighters()) {
 			debug.i("- checking fighter " + p.getName(), p.getName());
 			if (p.getStatus() != null && p.getStatus().equals(Status.FIGHT)) {
 				debug.i("-- added!", p.getName());
 				winners++;
 			}
 		}
 		debug.i("winners: " + winners, player);
 		
 		if (economy != null) {
 			debug.i("checking on bet amounts!", player);
 			for (String nKey : getPlayerBetMap().keySet()) {
 				String[] nSplit = nKey.split(":");
 
 				if (nSplit[1].equalsIgnoreCase(player.getName())) {
 					double playerFactor = arena.getFighters().size()
 							* arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_BETWINPLAYERFACTOR);
 
 					if (playerFactor <= 0) {
 						playerFactor = 1;
 					}
 
 					playerFactor *= arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_BETWINFACTOR);
 
 					double amount = getPlayerBetMap().get(nKey) * playerFactor;
 
 					economy.depositPlayer(nSplit[0], amount);
 					try {
 						
 						ArenaModuleManager.announce(
 								arena,
 								Language.parse(MSG.NOTICE_PLAYERAWARDED,
 										economy.format(amount)), "PRIZE");
 						arena.msg(Bukkit.getPlayer(nSplit[0]), Language
 								.parse(MSG.MODULE_VAULT_YOUWON, economy.format(amount)));
 					} catch (Exception e) {
 						// nothing
 					}
 				}
 			}
 			
 			if (arena.getArenaConfig().getBoolean(CFG.MODULES_VAULT_WINPOT)) {
 				debug.i("calculating win pot!", player);
 				double amount = winners > 0 ? pot / winners : 0;
 				
 				economy.depositPlayer(player.getName(), amount);
 				arena.msg(player, Language.parse(MSG.NOTICE_AWARDED,
 						economy.format(amount)));
 			} else if (arena.getArenaConfig().getInt(CFG.MODULES_VAULT_WINREWARD, 0) > 0) {
 
 				double amount = arena.getArenaConfig().getInt(CFG.MODULES_VAULT_WINREWARD, 0);
 				debug.i("calculating win reward: " + amount, player);
 				
 				economy.depositPlayer(player.getName(), amount);
 				arena.msg(player, Language.parse(MSG.NOTICE_AWARDED,
 						economy.format(amount)));
 
 			} 	
 
 			for (String nKey : getPlayerJoinMap().keySet()) {
 
 				if (nKey.equalsIgnoreCase(player.getName())) {
 					double playerFactor = arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_WINFACTOR);
 
 					double amount = getPlayerJoinMap().get(nKey) * playerFactor;
 
 					economy.depositPlayer(nKey, amount);
 					try {
 						
 						ArenaModuleManager.announce(
 								arena,
 								Language.parse(MSG.NOTICE_PLAYERAWARDED,
 										economy.format(amount)), "PRIZE");
 						arena.msg(Bukkit.getPlayer(nKey), Language
 								.parse(MSG.MODULE_VAULT_YOUWON, economy.format(amount)));
 					} catch (Exception e) {
 						// nothing
 					}
 				}
 			}
 		}
 	}
 
 	private void killreward(Player p, Entity damager) {
 		Player player = null;
 		if (damager instanceof Player) {
 			player = (Player) damager;
 		}
 		if (player == null) {
 			return;
 		}
 		double amount = arena.getArenaConfig()
 				.getDouble(CFG.MODULES_VAULT_KILLREWARD);
 
 		if (amount < 0.01) {
 			return;
 		}
 		
 		if (!economy.hasAccount(player.getName())) {
 			debug.i("Account not found: " + player.getName(), player);
 			return;
 		}
 		economy.depositPlayer(player.getName(), amount);
 		try {
 			arena.msg(Bukkit.getPlayer(player.getName()), Language
 					.parse(MSG.MODULE_VAULT_YOUWON, economy.format(amount)));
 		} catch (Exception e) {
 			// nothing
 		}
 	}
 
 	@Override
 	public void displayInfo(CommandSender player) {
 		player.sendMessage("entryfee: "
 				+ StringParser.colorVar(arena.getArenaConfig().getInt(CFG.MODULES_VAULT_ENTRYFEE))
 				+ " || reward: "
 				+ StringParser.colorVar(arena.getArenaConfig().getInt(CFG.MODULES_VAULT_WINREWARD))
 				+ " || killreward: "
 				+ StringParser.colorVar(arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_KILLREWARD))
 				+ " || winFactor: "
 				+ StringParser.colorVar(arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_WINFACTOR)));
 
 		player.sendMessage("minbet: "
 				+ StringParser.colorVar(arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_MINIMUMBET))
 				+ " || maxbet: "
 				+ StringParser.colorVar(arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_MAXIMUMBET))
 				+ " || betWinFactor: "
 				+ StringParser.colorVar(arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_BETWINFACTOR)));
 
 		player.sendMessage("betTeamWinFactor: "
 				+ StringParser.colorVar(arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_BETWINTEAMFACTOR))
 				+ " || betPlayerWinFactor: "
 				+ StringParser.colorVar(arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_BETWINPLAYERFACTOR)));
 
 		player.sendMessage(StringParser.colorVar(
 				"bet pot",arena.getArenaConfig().getBoolean(
 						CFG.MODULES_VAULT_BETPOT))
 				+ " || "
 				+ StringParser.colorVar(
 				"win pot",arena.getArenaConfig().getBoolean(
 						CFG.MODULES_VAULT_WINPOT)));
 	}
 
 	@Override
 	public void parseJoin(CommandSender sender, ArenaTeam team) {
 		int entryfee = arena.getArenaConfig().getInt(CFG.MODULES_VAULT_ENTRYFEE, 0);
 		if (entryfee > 0) {
 			if (economy != null) {
 				economy.withdrawPlayer(sender.getName(), entryfee);
 				arena.msg(sender,
 						Language.parse(MSG.MODULE_VAULT_JOINPAY, economy.format(entryfee)));
 				pot += entryfee;
 			}
 		}
 	}
 	
 	public void parsePlayerDeath(Player p,
 			EntityDamageEvent cause) {
		killreward(p,ArenaPlayer.getLastDamagingPlayer(cause));
 	}
 
 	protected void pay(HashSet<String> result) {
 		if (result == null || result.size() == arena.getTeamNames().size()) {
 			return;
 		}
 		debug.i("Paying winners: " + StringParser.joinSet(result, ", "));
 		
 		if (economy != null) {
 			
 			double pot = 0;
 			double winpot = 0;
 			
 			for (String s : getPlayerBetMap().keySet()) {
 				String[] nSplit = s.split(":");
 				
 				pot += getPlayerBetMap().get(s);
 				
 				if (result.contains(nSplit)) {
 					winpot += getPlayerBetMap().get(s);
 				}
 			}
 			
 			for (String nKey : getPlayerBetMap().keySet()) {
 				String[] nSplit = nKey.split(":");
 				ArenaTeam team = arena.getTeam(nSplit[1]);
 				if (team == null || team.getName().equals("free")) {
 					if (Bukkit.getPlayerExact(nSplit[1]) == null) {
 						continue;
 					}
 				}
 
 				if (result.contains(nSplit[1])) {
 					double amount = 0;
 					
 					if (arena.getArenaConfig().getBoolean(CFG.MODULES_VAULT_BETPOT)) {
 						if (winpot > 0) {
 							amount = pot * getPlayerBetMap().get(nKey) / winpot;
 						}
 					} else {
 						double teamFactor = arena.getArenaConfig()
 								.getDouble(CFG.MODULES_VAULT_BETWINTEAMFACTOR)
 								* arena.getTeamNames().size();
 						if (teamFactor <= 0) {
 							teamFactor = 1;
 						}
 						teamFactor *= arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_BETWINFACTOR);
 						amount = getPlayerBetMap().get(nKey) * teamFactor;
 					}
 
 					if (!economy.hasAccount(nSplit[0])) {
 						debug.i("Account not found: " + nSplit[0]);
 						continue;
 					}
 					economy.depositPlayer(nSplit[0], amount);
 					try {
 						arena.msg(Bukkit.getPlayer(nSplit[0]), Language
 								.parse(MSG.MODULE_VAULT_YOUWON, economy.format(amount)));
 					} catch (Exception e) {
 						// nothing
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public void reset(boolean force) {
 		getPlayerBetMap().clear();
 		getPlayerJoinMap().clear();
 		pot = 0;
 	}
 	
 	@Override
 	public void resetPlayer(Player player, boolean force) {
 		if (player == null) {
 			return;
 		}
 		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
 		if (ap == null) {
 			return;
 		}
 		if (ap.getStatus() == null || force) {
 			return;
 		}
 		if (ap.getStatus().equals(Status.LOUNGE) ||
 				ap.getStatus().equals(Status.READY)) {
 			int entryfee = arena.getArenaConfig().getInt(CFG.MODULES_VAULT_ENTRYFEE);
 			if (entryfee < 1) {
 				return;
 			}
 			arena.msg(player, Language.parse(MSG.MODULE_VAULT_REFUNDING, economy.format(entryfee)));
 			if (!economy.hasAccount(player.getName())) {
 				debug.i("Account not found: " + player.getName(), player);
 				return;
 			}
 			economy.depositPlayer(player.getName(), entryfee);
 			pot -= entryfee;
 		}
 	}
 
 	private boolean setupEconomy() {
 		RegisteredServiceProvider<Economy> economyProvider = Bukkit
 				.getServicesManager().getRegistration(
 						net.milkbowl.vault.economy.Economy.class);
 		if (economyProvider != null) {
 			economy = economyProvider.getProvider();
 		}
 
 		return (economy != null);
 	}
 	
 	public void timedEnd(HashSet<String> result) {
 		pay(result);
 	}
 }
