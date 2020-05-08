 package main;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 
 import main.commands.cmdA;
 import main.commands.cmdBAN;
 import main.commands.cmdCENZURA;
 import main.commands.cmdCHAT;
 import main.commands.cmdCLEAR;
 import main.commands.cmdFOE;
 import main.commands.cmdGRAMATIKA;
 import main.commands.cmdHELP;
 import main.commands.cmdINF;
 import main.commands.cmdINV;
 import main.commands.cmdKICK;
 import main.commands.cmdTP;
 import main.commands.cmdUNBAN;
 import main.commands.cmdZPRAVA;
 import main.events.EntityDeath;
 import main.events.onChat;
 import main.events.onHoldingsUpdate;
 import main.events.onInventoryClick;
 import main.events.onInventoryDrag;
 import main.events.onJoin;
 import main.events.onKick;
 import main.events.onPlayerDeath;
 import main.events.onPlayerLogin;
 import main.events.onQuit;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Score;
 import org.bukkit.scoreboard.Scoreboard;
 
 import com.iCo6.system.Account;
 import com.iCo6.system.Accounts;
 
 public class FoE extends JavaPlugin implements Listener {
 	
 	public File						configFile						= new File("plugins/FoE/config.yml");
 	public YamlConfiguration		config							= YamlConfiguration.loadConfiguration(configFile);
 	public File						uzivFile;
 	public YamlConfiguration		uziv;
 	public HashMap<String, Long>	nahranyCas						= new HashMap<String, Long>();
 	public MySQL					mysql;
 	public List<String>				vtipy							= new ArrayList<String>();
 	public int						minutesLeft						= 0;
 	public int						minutesLeft2					= 0;
 	public int						minutesLeft3					= 0;
 	public int						minutesLeft4					= 0;
 	public int						AntiSpamCas						= 0;
 	public int						vyhledavatAktualizaceCas		= 0;
 	public int						mysqlCas						= 0;
 	public int						vtipyInterval					= 0;
 	public boolean					Chat							= true;
 	public boolean					mysqlPovolit					= false;
 	public boolean					oznameniPovolit					= false;
 	public boolean					kdyzHracSePripojiPovolit		= false;
 	public boolean					kdyzHracSeOdpojiPovolit			= false;
 	public boolean					kdyzHracSeVyhodiPovolit			= false;
 	public boolean					nahranostPovolit				= false;
 	public boolean					nahranostPrivitaciZpravaPovolit	= false;
 	public boolean					antiReklamaPovolit				= false;
 	public boolean					cenzuraPovolit					= false;
 	public boolean					capsLockPovolit					= false;
 	public boolean					gramatikaPovolit				= false;
 	public boolean					vypnoutChatPovolit				= false;
 	public boolean					adminChatPovolit				= false;
 	public boolean					zpravaAdminum					= false;
 	public boolean					teleportPovolit					= false;
 	public boolean					guiPovolit						= false;
 	public boolean					guiTydny						= false;
 	public boolean					guiDny							= false;
 	public boolean					guiHodiny						= false;
 	public boolean					guiPocetHracu					= false;
 	public boolean					guiIconomy						= false;
 	public boolean					antiSpamPovolit					= false;
 	public boolean					antiSpamDuplikacePovolit		= false;
 	public boolean					rezervacePovolit				= false;
 	public boolean					inventarPovolit					= false;
 	public boolean					managerBan						= false;
 	public boolean					vtipyPovolit					= false;
 	public boolean					clearChat						= false;
 	public boolean					umrtiZpravyPovolit				= false;
 	public boolean					whiteListPovolit				= false;
 	
 	@Override
 	public void onEnable() {
 		kontrolaConfigu();
 		System.out.println("Registruji event 'onPlayerLogin'");
 		Bukkit.getPluginManager().registerEvents(new onPlayerLogin(this), this);
 		System.out.println("Registruji event 'onJoin'");
 		Bukkit.getPluginManager().registerEvents(new onJoin(this), this);
 		System.out.println("Registruji event 'onQuit'");
 		Bukkit.getPluginManager().registerEvents(new onQuit(this), this);
 		System.out.println("Registruji event 'onKick'");
 		Bukkit.getPluginManager().registerEvents(new onKick(this), this);
 		System.out.println("Registruji event 'onChat'");
 		Bukkit.getPluginManager().registerEvents(new onChat(this), this);
 		System.out.println("Registruji event 'onInventoryClick'");
 		Bukkit.getPluginManager().registerEvents(new onInventoryClick(this), this);
 		System.out.println("Registruji event 'onInventoryDrag'");
 		Bukkit.getPluginManager().registerEvents(new onInventoryDrag(this), this);
 		System.out.println("Registruji event 'EntityDeath'");
 		Bukkit.getPluginManager().registerEvents(new EntityDeath(this), this);
 		System.out.println("Registruji event 'onPlayerDeath'");
 		Bukkit.getPluginManager().registerEvents(new onPlayerDeath(this), this);
 		Bukkit.getPluginManager().registerEvents(this, this);
 		if (Status(config, "Ostatni.Nahranost.GUI.iConomy-Povolit")) {
 			System.out.println("Registruji event 'onHoldingsUpdate'");
 			Bukkit.getPluginManager().registerEvents(new onHoldingsUpdate(this), this);
 		}
 		Bukkit.getServer().getPluginCommand("FoE").setExecutor(new cmdFOE(this));
 		vtipyInterval = config.getInt("Vtipy.Interval");
 		if (Status(config, "Nahranost.Povolit")) {
 			System.out.println("Registruji prikaz '" + config.getString("Prikazy.Nahranost") + "'");
 			Bukkit.getServer().getPluginCommand("infcmd").setExecutor(new cmdINF(this));
 			nahranostPovolit = true;
 			if (Status(config, "Nahranost.PrivitaciZprava.Povolit")) {
 				nahranostPrivitaciZpravaPovolit = true;
 			}
 			if (Status(config, "Ostatni.Nahranost.GUI.Tydny-Povolit")) {
 				guiTydny = true;
 			}
 			if (Status(config, "Ostatni.Nahranost.GUI.Dny-Povolit")) {
 				guiDny = true;
 			}
 			if (Status(config, "Ostatni.Nahranost.GUI.Hodiny-Povolit")) {
 				guiHodiny = true;
 			}
 			if (Status(config, "Ostatni.Nahranost.GUI.PocetHracu-Povolit")) {
 				guiPocetHracu = true;
 			}
 			if (Status(config, "Ostatni.Nahranost.GUI.iConomy-Povolit")) {
 				guiIconomy = true;
 			}
 			for (Player p : Bukkit.getOnlinePlayers()) {
 				registrovatHrace(p.getName());
 			}
 		}
 		if (Status(config, "Oznameni.Povolit")) {
 			System.out.println("Registruji prikaz '" + config.getString("Prikazy.Oznameni") + "'");
 			Bukkit.getServer().getPluginCommand("zpravacmd").setExecutor(new cmdZPRAVA(this));
 			oznameniPovolit = true;
 		}
 		if (Status(config, "VypnoutChat.Povolit")) {
 			System.out.println("Registruji prikaz '" + config.getString("Prikazy.VypnoutChat") + "'");
 			Bukkit.getServer().getPluginCommand("chatcmd").setExecutor(new cmdCHAT(this));
 			vypnoutChatPovolit = true;
 		}
 		if (Status(config, "Gramatika.Povolit")) {
 			System.out.println("Registruji prikaz '" + config.getString("Prikazy.Gramatika") + "'");
 			Bukkit.getServer().getPluginCommand("gramatikacmd").setExecutor(new cmdGRAMATIKA(this));
 			gramatikaPovolit = true;
 		}
 		if (Status(config, "Cenzura.Povolit")) {
 			System.out.println("Registruji prikaz '" + config.getString("Prikazy.Cenzura") + "'");
 			Bukkit.getServer().getPluginCommand("cenzuracmd").setExecutor(new cmdCENZURA(this));
 			cenzuraPovolit = true;
 		}
 		if (Status(config, "AdminChat.Povolit")) {
 			System.out.println("Registruji prikaz '" + config.getString("Prikazy.AdminChat") + "'");
 			Bukkit.getServer().getPluginCommand("acmd").setExecutor(new cmdA(this));
 			adminChatPovolit = true;
 		}
 		if (Status(config, "TP.Povolit")) {
 			System.out.println("Registruji prikaz '" + config.getString("Prikazy.Teleport") + "'");
 			Bukkit.getServer().getPluginCommand("tpcmd").setExecutor(new cmdTP(this));
 			teleportPovolit = true;
 		}
 		if (Status(config, "Inventar.Povolit")) {
 			System.out.println("Registruji prikaz '" + config.getString("Prikazy.Inventar") + "'");
 			Bukkit.getServer().getPluginCommand("invcmd").setExecutor(new cmdINV(this));
 			inventarPovolit = true;
 		}
 		if (Status(config, "capsLock.Povolit")) {
 			capsLockPovolit = true;
 		}
 		if (Status(config, "Manager.Povolit")) {
 			System.out.println("Registruji prikaz '" + config.getString("Prikazy.Manager.Ban") + "'");
 			Bukkit.getServer().getPluginCommand("bancmd").setExecutor(new cmdBAN(this));
 			System.out.println("Registruji prikaz '" + config.getString("Prikazy.Manager.Unban") + "'");
 			Bukkit.getServer().getPluginCommand("unbancmd").setExecutor(new cmdUNBAN(this));
 			System.out.println("Registruji prikaz '" + config.getString("Prikazy.Manager.Kick") + "'");
 			Bukkit.getServer().getPluginCommand("kickcmd").setExecutor(new cmdKICK(this));
 			managerBan = true;
 		}
 		if (Status(config, "clearChat.Povolit")) {
 			System.out.println("Registruji prikaz '" + config.getString("Prikazy.Clear") + "'");
 			Bukkit.getServer().getPluginCommand("clearcmd").setExecutor(new cmdCLEAR(this));
 			clearChat = true;
 		}
 		if (Status(config, "zpravaAdminum.Povolit")) {
 			zpravaAdminum = true;
 			System.out.println("Registruji prikaz '" + config.getString("Prikazy.Help") + "'");
 			Bukkit.getServer().getPluginCommand("helpcmd").setExecutor(new cmdHELP(this));
 		}
 		if (Status(config, "Ostatni.Upgrade")) {
 			Upgrade();
 			config.set("Ostatni.Upgrade", "ne");
 			saveConfig(config, configFile);
 			zkontrolovatPluginy();
 		}
 		if (Status(config, "AntiReklama.Povolit")) {
 			antiReklamaPovolit = true;
 		}
 		
 		if (Status(config, "KdyzHracSe.Pripoji.Povolit")) {
 			kdyzHracSePripojiPovolit = true;
 		}
 		
 		if (Status(config, "KdyzHracSe.Vyhodi.Povolit")) {
 			kdyzHracSeVyhodiPovolit = true;
 		}
 		
 		if (Status(config, "KdyzHracSe.Odpoji.Povolit")) {
 			kdyzHracSeOdpojiPovolit = true;
 		}
 		
 		if (Status(config, "MySQL.Povolit")) {
 			mysql = new MySQL(this);
 			mysqlCas = config.getInt("MySQL.Cas");
 			mysql.open();
 			mysqlPovolit = true;
 		}
 		if (Status(config, "Ostatni.Nahranost.GUI.Povolit")) {
 			guiPovolit = true;
 		}
 		
 		if (Status(config, "AntiSpam.Povolit")) {
 			AntiSpamCas = config.getInt("AntiSpam.PockatSekund");
 			antiSpamPovolit = true;
 		}
 		if (Status(config, "AntiSpam.Duplikace.Povolit"))
 			antiSpamDuplikacePovolit = true;
 		if (Status(config, "Rezervace.Povolit")) {
 			rezervacePovolit = true;
 		}
 		if (Status(config, "umrtiZpravy.Povolit")) {
 			umrtiZpravyPovolit = true;
 		}
 		if (Status(config, "whiteList.Povolit")) {
 			whiteListPovolit = true;
 		}
 		if (Status(config, "Vtipy.Povolit")) {
 			vtipyPovolit = true;
 			try {
 				BufferedReader br = new BufferedReader(new InputStreamReader(new URL("http://www.foe.frelania.eu/vtipy.txt").openStream()));
 				StringBuilder sb = new StringBuilder();
 				String line = br.readLine();
 				while (line != null) {
 					sb.append(line);
 					sb.append("\n");
 					line = br.readLine();
 				}
 				String vysledek = sb.toString();
 				for (String vtip : vysledek.split("#")) {
 					vtipy.add(vtip);
 				}
 			} catch (Exception e) {
 				Writer writer = new StringWriter();
 				PrintWriter printWriter = new PrintWriter(writer);
 				e.printStackTrace(printWriter);
 				Error(writer.toString());
 			}
 			startLoop4(vtipyInterval);
 		}
 		Plugin updater = Bukkit.getPluginManager().getPlugin("FoE-Updater");
 		if (updater != null) {
 			if (!updater.isEnabled()) {
 				if (Status(config, "VyhledavatAktualizace.Povolit")) {
 					vyhledavatAktualizaceCas = config.getInt("VyhledavatAktualizace.Cas");
 					zkontrolovatVerziPluginu();
 					startLoop2(vyhledavatAktualizaceCas);
 				}
 			}
 		}
 		statistiky();
 		System.out.println("[FoE] byl uspesne zapnut.");
 	}
 	
 	public void aktualizovatMySQL(String playerName) {
 		try {
 			uzivatel(playerName);
 			Long nahranost = (System.currentTimeMillis() - nahranyCas.get(playerName)) + uziv.getLong("Nahrano");
 			ResultSet rs = mysql.query("SELECT `hrac` FROM `FoE_Uzivatele` WHERE `hrac` = '" + playerName + "'");
 			if (rs.next()) {
 				mysql.query("UPDATE `FoE_Uzivatele` SET `nahranost` = '" + nahranost + "' WHERE `hrac` = '" + playerName + "'");
 			} else {
 				mysql.query("INSERT INTO `FoE_Uzivatele` (hrac,nahranost) VALUES ('" + playerName + "', '" + nahranost + "') ON DUPLICATE KEY UPDATE `nahranost` = VALUES(nahranost)");
 			}
 		} catch (SQLException e) {
 			Writer writer = new StringWriter();
 			PrintWriter printWriter = new PrintWriter(writer);
 			e.printStackTrace(printWriter);
 			Error(writer.toString());
 		}
 	}
 	
 	public void vytvoritGUI(Player player) {
 		try {
 			if (player != null) {
 				if (nahranyCas.containsKey(player.getName())) {
 					uzivatel(player.getName());
 					long[] cas = spravnyFormat((System.currentTimeMillis() - nahranyCas.get(player.getName())) + uziv.getLong("Nahrano"));
 					Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
 					Objective objective = board.registerNewObjective(player.getName(), "dummy");
 					objective.setDisplaySlot(DisplaySlot.SIDEBAR);
 					objective.setDisplayName(nahradit(config.getString("Ostatni.Nahranost.GUI.Nadpis"), player.getName()));
 					Score score = objective.getScore(Bukkit.getOfflinePlayer(config.getString("Ostatni.Nahranost.GUI.Tydny")));
 					Score score2 = objective.getScore(Bukkit.getOfflinePlayer(config.getString("Ostatni.Nahranost.GUI.Dny")));
 					Score score3 = objective.getScore(Bukkit.getOfflinePlayer(config.getString("Ostatni.Nahranost.GUI.Hodiny")));
 					Score score4 = objective.getScore(Bukkit.getOfflinePlayer(config.getString("Ostatni.Nahranost.GUI.PocetHracu")));
 					Score score5 = objective.getScore(Bukkit.getOfflinePlayer(config.getString("Ostatni.Nahranost.GUI.iConomy")));
 					
 					if (guiTydny)
 						score.setScore((int) cas[4]);
 					if (guiDny)
 						score2.setScore((int) cas[3]);
 					if (guiHodiny)
 						score3.setScore((int) cas[2]);
 					if (guiPocetHracu)
 						score4.setScore(Bukkit.getOnlinePlayers().length);
 					if (guiIconomy) {
 						Account account = new Accounts().get(player.getName());
 						Double money = account.getHoldings().getBalance();
 						int intMoney = money.intValue();
 						score5.setScore(intMoney);
 					}
 					player.setScoreboard(board);
 				} else {
 					uzivatel(player.getName());
 					long[] cas = spravnyFormat(uziv.getLong("Nahrano"));
 					Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
 					Objective objective = board.registerNewObjective(player.getName(), "dummy");
 					objective.setDisplaySlot(DisplaySlot.SIDEBAR);
 					objective.setDisplayName(nahradit(config.getString("Ostatni.Nahranost.GUI.Nadpis"), player.getName()));
 					Score score = objective.getScore(Bukkit.getOfflinePlayer(config.getString("Ostatni.Nahranost.GUI.Tydny")));
 					Score score2 = objective.getScore(Bukkit.getOfflinePlayer(config.getString("Ostatni.Nahranost.GUI.Dny")));
 					Score score3 = objective.getScore(Bukkit.getOfflinePlayer(config.getString("Ostatni.Nahranost.GUI.Hodiny")));
 					Score score4 = objective.getScore(Bukkit.getOfflinePlayer(config.getString("Ostatni.Nahranost.GUI.PocetHracu")));
 					Score score5 = objective.getScore(Bukkit.getOfflinePlayer(config.getString("Ostatni.Nahranost.GUI.iConomy")));
 					
 					if (guiTydny)
 						score.setScore((int) cas[4]);
 					if (guiDny)
 						score2.setScore((int) cas[3]);
 					if (guiHodiny)
 						score3.setScore((int) cas[2]);
 					if (guiPocetHracu)
 						score4.setScore(Bukkit.getOnlinePlayers().length);
 					if (guiIconomy) {
 						Account account = new Accounts().get(player.getName());
 						Double money = account.getHoldings().getBalance();
 						int intMoney = money.intValue();
 						score5.setScore(intMoney);
 					}
 					player.setScoreboard(board);
 				}
 			}
 		} catch (Exception e) {
 			Writer writer = new StringWriter();
 			PrintWriter printWriter = new PrintWriter(writer);
 			e.printStackTrace(printWriter);
 			Error(writer.toString());
 		}
 	}
 	
 	public void ulozitPozici(Player player) {
 		try {
 			String playerName = player.getName();
 			Double X = player.getLocation().getX();
 			Double Y = player.getLocation().getY();
 			Double Z = player.getLocation().getZ();
 			uzivatel(playerName);
			uziv.set("Svet", player.getLocation().getWorld().getName());
 			uziv.set("X", X);
 			uziv.set("Y", Y);
 			uziv.set("Z", Z);
 			saveConfig(uziv, uzivFile);
 		} catch (Exception e) {
 			Writer writer = new StringWriter();
 			PrintWriter printWriter = new PrintWriter(writer);
 			e.printStackTrace(printWriter);
 			Error(writer.toString());
 		}
 	}
 	
 	public void aktualizovatGUI(Player player) {
 		try {
 			if (player != null) {
 				uzivatel(player.getName());
 				long[] cas = spravnyFormat((System.currentTimeMillis() - nahranyCas.get(player.getName())) + uziv.getLong("Nahrano"));
 				Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
 				Objective objective = board.registerNewObjective(player.getName(), "dummy");
 				objective.setDisplaySlot(DisplaySlot.SIDEBAR);
 				objective.setDisplayName(nahradit(config.getString("Ostatni.Nahranost.GUI.Nadpis"), player.getName()));
 				Score score = objective.getScore(Bukkit.getOfflinePlayer(config.getString("Ostatni.Nahranost.GUI.Tydny")));
 				Score score2 = objective.getScore(Bukkit.getOfflinePlayer(config.getString("Ostatni.Nahranost.GUI.Dny")));
 				Score score3 = objective.getScore(Bukkit.getOfflinePlayer(config.getString("Ostatni.Nahranost.GUI.Hodiny")));
 				Score score4 = objective.getScore(Bukkit.getOfflinePlayer(config.getString("Ostatni.Nahranost.GUI.PocetHracu")));
 				Score score5 = objective.getScore(Bukkit.getOfflinePlayer(config.getString("Ostatni.Nahranost.GUI.iConomy")));
 				score.setScore((int) cas[4]);
 				score2.setScore((int) cas[3]);
 				score3.setScore((int) cas[2]);
 				score4.setScore(Bukkit.getOnlinePlayers().length);
 				if (guiIconomy) {
 					Account account = new Accounts().get(player.getName());
 					Double money = account.getHoldings().getBalance();
 					int intMoney = money.intValue();
 					score5.setScore(intMoney);
 				}
 			}
 		} catch (Exception e) {
 			Writer writer = new StringWriter();
 			PrintWriter printWriter = new PrintWriter(writer);
 			e.printStackTrace(printWriter);
 			Error(writer.toString());
 		}
 	}
 	
 	public void aktualizovatGUI(String playerName) {
 		try {
 			Player player = Bukkit.getPlayer(playerName);
 			if (player != null) {
 				uzivatel(player.getName());
 				long[] cas = spravnyFormat((System.currentTimeMillis() - nahranyCas.get(player.getName())) + uziv.getLong("Nahrano"));
 				Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
 				Objective objective = board.registerNewObjective(player.getName(), "dummy");
 				objective.setDisplaySlot(DisplaySlot.SIDEBAR);
 				objective.setDisplayName(nahradit(config.getString("Ostatni.Nahranost.GUI.Nadpis"), player.getName()));
 				Score score = objective.getScore(Bukkit.getOfflinePlayer(config.getString("Ostatni.Nahranost.GUI.Tydny")));
 				Score score2 = objective.getScore(Bukkit.getOfflinePlayer(config.getString("Ostatni.Nahranost.GUI.Dny")));
 				Score score3 = objective.getScore(Bukkit.getOfflinePlayer(config.getString("Ostatni.Nahranost.GUI.Hodiny")));
 				Score score4 = objective.getScore(Bukkit.getOfflinePlayer(config.getString("Ostatni.Nahranost.GUI.PocetHracu")));
 				Score score5 = objective.getScore(Bukkit.getOfflinePlayer(config.getString("Ostatni.Nahranost.GUI.iConomy")));
 				score.setScore((int) cas[4]);
 				score2.setScore((int) cas[3]);
 				score3.setScore((int) cas[2]);
 				score4.setScore(Bukkit.getOnlinePlayers().length);
 				if (guiIconomy) {
 					Account account = new Accounts().get(player.getName());
 					Double money = account.getHoldings().getBalance();
 					int intMoney = money.intValue();
 					score5.setScore(intMoney);
 				}
 			}
 		} catch (Exception e) {
 			Writer writer = new StringWriter();
 			PrintWriter printWriter = new PrintWriter(writer);
 			e.printStackTrace(printWriter);
 			Error(writer.toString());
 		}
 	}
 	
 	public void Error(String message) {
 		try {
 			File u = new File("plugins/FoE/errors.log");
 			FileWriter fw = new FileWriter(u, true);
 			PrintWriter pw = new PrintWriter(fw);
 			Date date = new Date();
 			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
 			String time = sdf.format(date);
 			pw.println("================== " + time + " - FoE: " + getDescription().getVersion() + "\n" + "CB: " + Bukkit.getVersion() + "\n" + message + "\n==================\n");
 			pw.flush();
 			pw.close();
 			System.out.println("[FoE] ERROR!");
 			System.out.println("===========================");
 			System.out.println("Prekopirujte obsah souboru errors.log do prispevku.");
 			System.out.println("===========================");
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 	}
 	
 	public void minute2() {
 		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 			@Override
 			public void run() {
 				minutesLeft2 -= 1;
 				if (minutesLeft2 > 0) {
 					minute2();
 				}
 				
 				if (minutesLeft2 == 0) {
 					zkontrolovatVerziPluginu();
 					startLoop2(vyhledavatAktualizaceCas);
 				}
 			}
 		}, 1200L);
 	}
 	
 	public void minute4() {
 		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 			@Override
 			public void run() {
 				minutesLeft4 -= 1;
 				if (minutesLeft4 > 0) {
 					minute4();
 				}
 				
 				if (minutesLeft4 == 0) {
 					try {
 						Bukkit.broadcastMessage(nahraditVtip(config.getString("Vtipy.Format")));
 						startLoop4(vtipyInterval);
 					} catch (Exception e) {
 						Writer writer = new StringWriter();
 						PrintWriter printWriter = new PrintWriter(writer);
 						e.printStackTrace(printWriter);
 						Error(writer.toString());
 					}
 				}
 			}
 		}, 1200L);
 	}
 	
 	public String nahraditVtip(String message) {
 		if (message.matches(".*\\{VTIP}.*")) {
 			Random rnd = new Random();
 			message = message.replaceAll("(&([a-fk-or0-9]))", "$2");
 			return message = message.replaceAll("\\{VTIP}", vtipy.get(rnd.nextInt(vtipy.size())));
 		}
 		return "ERROR(nahraditVtip)";
 	}
 	
 	public void startLoop4(int length) {
 		minutesLeft4 = length;
 		minute4();
 	}
 	
 	public void startLoop2(int length) {
 		minutesLeft2 = length;
 		minute2();
 	}
 	
 	public void registrovatHrace(String jmenoHrace) {
 		try {
 			nahranyCas.put(jmenoHrace, System.currentTimeMillis());
 		} catch (Exception e) {
 			Writer writer = new StringWriter();
 			PrintWriter printWriter = new PrintWriter(writer);
 			e.printStackTrace(printWriter);
 			Error(writer.toString());
 		}
 	}
 	
 	public void odRegistrovatHrace(String jmenoHrace) {
 		try {
 			if (nahranyCas.containsKey(jmenoHrace)) {
 				uzivatel(jmenoHrace);
 				long casPripojeni = nahranyCas.get(jmenoHrace), vConfigu = uziv.getLong("Nahrano"), vysledek = System.currentTimeMillis() - casPripojeni + vConfigu;
 				uziv.set("Nahrano", vysledek);
 				saveConfig(uziv, uzivFile);
 				if (mysqlPovolit)
 					aktualizovatMySQL(jmenoHrace);
 			}
 		} catch (Exception e) {
 			Writer writer = new StringWriter();
 			PrintWriter printWriter = new PrintWriter(writer);
 			e.printStackTrace(printWriter);
 			Error(writer.toString());
 		}
 	}
 	
 	@Override
 	public void onDisable() {
 		for (Player p : Bukkit.getOnlinePlayers()) {
 			odRegistrovatHrace(p.getName());
 		}
 		Bukkit.getScheduler().cancelAllTasks();
 	}
 	
 	public void zkontrolovatPluginy() {
 		try {
 			Plugin nahrano = Bukkit.getPluginManager().getPlugin("Nahrano"), cestinator = Bukkit.getPluginManager().getPlugin("Cestinator"), antireklama = Bukkit.getPluginManager().getPlugin("AntiReklama"), verejnazprava = Bukkit.getPluginManager().getPlugin("VerejnaZprava"), vypnoutchat = Bukkit.getPluginManager().getPlugin("VypnoutChat");
 			File nahranoFile = new File("plugins/Nahrano.jar"), cestinatorFile = new File("plugins/Cestinator.jar"), antireklamaFile = new File("plugins/AntiReklama.jar"), verejnazpravaFile = new File("plugins/VerejnaZprava.jar"), vypnoutchatFile = new File("plugins/VypnoutChat.jar");
 			if (nahrano != null) {
 				Bukkit.getPluginManager().disablePlugin(nahrano);
 				nahranoFile.delete();
 			}
 			if (cestinator != null) {
 				Bukkit.getPluginManager().disablePlugin(cestinator);
 				cestinatorFile.delete();
 			}
 			if (antireklama != null) {
 				Bukkit.getPluginManager().disablePlugin(antireklama);
 				antireklamaFile.delete();
 			}
 			if (verejnazprava != null) {
 				Bukkit.getPluginManager().disablePlugin(verejnazprava);
 				verejnazpravaFile.delete();
 			}
 			if (vypnoutchat != null) {
 				Bukkit.getPluginManager().disablePlugin(vypnoutchat);
 				vypnoutchatFile.delete();
 			}
 		} catch (Exception e) {
 			Writer writer = new StringWriter();
 			PrintWriter printWriter = new PrintWriter(writer);
 			e.printStackTrace(printWriter);
 			Error(writer.toString());
 		}
 	}
 	
 	public String nahraditCas(String zprava, String hrac) {
 		if (zprava != null && hrac != null && nahranyCas.containsKey(hrac)) {
 			uzivatel(hrac);
 			long[] cas = spravnyFormat(System.currentTimeMillis() - nahranyCas.get(hrac) + uziv.getLong("Nahrano"));
 			String s = String.valueOf(cas[0]), m = String.valueOf(cas[1]), h = String.valueOf(cas[2]), d = String.valueOf(cas[3]), t = String.valueOf(cas[4]);
 			if (zprava.matches(".*\\{TYDEN}.*")) {
 				zprava = zprava.replaceAll("\\{TYDEN}", t);
 			}
 			if (zprava.matches(".*\\{DEN}.*")) {
 				zprava = zprava.replaceAll("\\{DEN}", d);
 			}
 			if (zprava.matches(".*\\{HODIN}.*")) {
 				zprava = zprava.replaceAll("\\{HODIN}", h);
 			}
 			if (zprava.matches(".*\\{MINUT}.*")) {
 				zprava = zprava.replaceAll("\\{MINUT}", m);
 			}
 			if (zprava.matches(".*\\{SEKUND}.*")) {
 				zprava = zprava.replaceAll("\\{SEKUND}", s);
 			}
 		} else {
 			zprava = null;
 		}
 		return zprava;
 	}
 	
 	public long[] spravnyFormat(long Long) {
 		long sekundy = Long / 1000L;
 		long minuty = 0L;
 		long hodiny = 0L;
 		long dny = 0L;
 		long tydny = 0L;
 		
 		while (sekundy > 60L) {
 			minuty += 1L;
 			sekundy -= 60L;
 		}
 		
 		while (minuty > 60L) {
 			hodiny += 1L;
 			minuty -= 60L;
 		}
 		
 		while (hodiny > 24L) {
 			dny += 1L;
 			hodiny -= 24L;
 		}
 		
 		while (dny > 7L) {
 			tydny += 1L;
 			dny -= 7L;
 		}
 		return new long[] { sekundy, minuty, hodiny, dny, tydny };
 	}
 	
 	public void uzivatel(String jmenoHrace) {
 		try {
 			if (jmenoHrace.length() != 0) {
 				uzivFile = new File("plugins/FoE/uzivatele/" + jmenoHrace + ".yml");
 				uziv = YamlConfiguration.loadConfiguration(uzivFile);
 				if (!uzivFile.exists())
 					saveConfig(uziv, uzivFile);
 				if (!uziv.contains("Nahrano"))
 					uziv.set("Nahrano", 0);
 				if (!uziv.contains("isBanned"))
 					uziv.set("isBanned", false);
 				saveConfig(uziv, uzivFile);
 			}
 		} catch (Exception e) {
 			Writer writer = new StringWriter();
 			PrintWriter printWriter = new PrintWriter(writer);
 			e.printStackTrace(printWriter);
 			Error(writer.toString());
 		}
 	}
 	
 	public void Upgrade() {
 		try {
 			File nahrano = new File("plugins/Nahrano/config.yml"), cestinator = new File("plugins/Cestinator/config.yml"), antireklama = new File("plugins/AntiReklama/config.yml"), verejnazprava = new File("plugins/VerejnaZprava/config.yml"), vypnoutchat = new File("plugins/VypnoutChat/config.yml");
 			String nahranoDir = "plugins/Nahrano/", cestinatorDir = "plugins/Cestinator/", antireklamaDir = "plugins/AntiReklama/", verejnazpravaDir = "plugins/VerejnaZprava/", vypnoutchatDir = "plugins/VypnoutChat/";
 			Boolean b = false, c = false, d = false, a = false;
 			if (nahrano.exists()) {
 				YamlConfiguration aa = YamlConfiguration.loadConfiguration(nahrano);
 				for (String hrac : aa.getConfigurationSection("Nahrano").getKeys(false)) {
 					Long cas = aa.getLong("Nahrano." + hrac);
 					uzivatel(hrac);
 					if (!uzivFile.exists()) {
 						uziv.set("Nahrano", cas);
 						System.out.println(hrac + " - " + cas);
 						saveConfig(config, configFile);
 					} else {
 						Long nahr = Long.valueOf(uziv.getLong("Nahrano"));
 						uziv.set("Nahrano", nahr + cas);
 						System.out.println(hrac + " - " + nahr + cas);
 						saveConfig(config, configFile);
 					}
 				}
 				System.out.println("Upgrade: Nahrano - Hotovo");
 				b = true;
 				deleteFolder(nahrano);
 				DeleteFileFolder(nahranoDir);
 			} else {
 				b = true;
 			}
 			if (b) {
 				if (cestinator.exists()) {
 					YamlConfiguration aa = YamlConfiguration.loadConfiguration(cestinator);
 					List<String> m = new ArrayList<String>();
 					List<String> n = new ArrayList<String>();
 					List<String> o = new ArrayList<String>();
 					for (String l : aa.getStringList("Slova.vsude")) {
 						m.add(l);
 						System.out.println(l);
 					}
 					for (String l : aa.getStringList("Slova.cele")) {
 						n.add(l);
 						System.out.println(l);
 					}
 					for (String l : aa.getStringList("Cenzura.slova")) {
 						o.add(l);
 						System.out.println(l);
 					}
 					config.set("Gramatika.Vsude", m);
 					config.set("Gramatika.Cele", n);
 					config.set("Cenzura.slova", o);
 					saveConfig(config, configFile);
 					System.out.println("Upgrade: Gramatika, Cenzura - Hotovo");
 					c = true;
 					deleteFolder(cestinator);
 					DeleteFileFolder(cestinatorDir);
 				} else {
 					c = true;
 				}
 			}
 			if (c) {
 				if (antireklama.exists()) {
 					YamlConfiguration aa = YamlConfiguration.loadConfiguration(antireklama);
 					List<String> n = new ArrayList<String>();
 					List<String> m = new ArrayList<String>();
 					for (String l : aa.getStringList("IP.povoleno")) {
 						n.add(l);
 						System.out.println(l);
 					}
 					for (String l : aa.getStringList("WEB.povoleno")) {
 						m.add(l);
 						System.out.println(l);
 					}
 					config.set("AntiReklama.WEB.Zprava", aa.getString("WEB.verejnaZprava"));
 					config.set("AntiReklama.WEB.Akce", aa.getString("WEB.akce"));
 					config.set("AntiReklama.WEB.Whitelist", m);
 					config.set("AntiReklama.IP.Akce", aa.getString("IP.akce"));
 					config.set("AntiReklama.IP.Zprava", aa.getString("IP.verejnaZprava"));
 					config.set("AntiReklama.IP.Whitelist", n);
 					saveConfig(config, configFile);
 					System.out.println("Upgrade: AntiReklama - Hotovo");
 					d = true;
 					deleteFolder(antireklama);
 					DeleteFileFolder(antireklamaDir);
 				} else {
 					d = true;
 				}
 			}
 			if (d) {
 				if (verejnazprava.exists()) {
 					YamlConfiguration aa = YamlConfiguration.loadConfiguration(verejnazprava);
 					config.set("Oznameni.Prefix", aa.getString("Prefix"));
 					config.set("Oznameni.Suffix", aa.getString("Suffix"));
 					saveConfig(config, configFile);
 					System.out.println("Upgrade: Oznameni - Hotovo");
 					a = true;
 					deleteFolder(verejnazprava);
 					DeleteFileFolder(verejnazpravaDir);
 				} else {
 					a = true;
 				}
 			}
 			if (a) {
 				if (vypnoutchat.exists()) {
 					YamlConfiguration aa = YamlConfiguration.loadConfiguration(vypnoutchat);
 					config.set("VypnoutChat.KdyzJeVypnutyChat", aa.getString("Zpravy.Chat"));
 					saveConfig(config, configFile);
 					System.out.println("Upgrade: VypnoutChat - Hotovo");
 					deleteFolder(vypnoutchat);
 					DeleteFileFolder(vypnoutchatDir);
 				}
 			}
 		} catch (Exception e) {
 			Writer writer = new StringWriter();
 			PrintWriter printWriter = new PrintWriter(writer);
 			e.printStackTrace(printWriter);
 			Error(writer.toString());
 		}
 	}
 	
 	private void delete(File file) {
 		if (file.isDirectory()) {
 			String[] fileList = file.list();
 			if (fileList.length == 0) {
 				file.delete();
 			} else {
 				int size = fileList.length;
 				for (int i = 0; i < size; i++) {
 					String fileName = fileList[i];
 					String fullPath = file.getPath() + "/" + fileName;
 					File fileOrFolder = new File(fullPath);
 					delete(fileOrFolder);
 				}
 			}
 		} else {
 			file.delete();
 		}
 	}
 	
 	public void DeleteFileFolder(String path) {
 		File file = new File(path);
 		if (file.exists()) {
 			do
 				delete(file);
 			while (file.exists());
 		}
 	}
 	
 	public boolean isBanned(String playerName) {
 		uzivatel(playerName);
 		if (uziv.getBoolean("isBanned"))
 			return true;
 		return false;
 	}
 	
 	public void kickPlayer(String sender, String playerName, String reason) {
 		Player pl = Bukkit.getPlayer(playerName);
 		if (pl != null) {
 			Bukkit.broadcastMessage(replaceNicknamesInBan(config.getString("Manager.Kick.Zprava"), sender, playerName, reason));
 			pl.kickPlayer(reason);
 			if (mysqlPovolit)
 				aktualizovatMySQLBan(sender, playerName, reason, "KICK");
 		} else {
 			Player s = Bukkit.getPlayer(sender);
 			s.sendMessage(playerName + " je ji zabanovn!");
 		}
 	}
 	
 	public void banPlayer(String sender, String playerName, String reason) {
 		if (!isBanned(playerName)) {
 			Player pl = Bukkit.getPlayer(playerName);
 			uzivatel(playerName);
 			uziv.set("isBanned", true);
 			uziv.set("banReason", reason);
 			saveConfig(uziv, uzivFile);
 			if (pl != null) {
 				pl.kickPlayer(reason);
 			}
 			if (mysqlPovolit)
 				aktualizovatMySQLBan(sender, playerName, reason, "BAN");
 			Bukkit.broadcastMessage(replaceNicknamesInBan(config.getString("Manager.Ban.Zprava"), sender, playerName, reason));
 		} else {
 			Player p = Bukkit.getPlayer(sender);
 			p.sendMessage(playerName + " je ji zabanovn!");
 		}
 	}
 	
 	public void unbanPlayer(String sender, String playerName, String reason) {
 		if (isBanned(playerName)) {
 			uzivatel(playerName);
 			uziv.set("isBanned", false);
 			Bukkit.broadcastMessage(replaceNicknamesInBan(config.getString("Manager.Unban.Zprava"), sender, playerName, reason));
 			if (mysqlPovolit)
 				aktualizovatMySQLBan(sender, playerName, reason, "UNBAN");
 			saveConfig(uziv, uzivFile);
 		} else {
 			Player p = Bukkit.getPlayer(sender);
 			p.sendMessage(playerName + " nem ban!");
 		}
 	}
 	
 	public String replaceNicknamesInBan(String message, String playerName, String targetName, String reason) {
 		if (message != null) {
 			if (message.matches(".*\\{JMENO}.*")) {
 				message = message.replaceAll("\\{JMENO}", playerName);
 			}
 			if (message.matches(".*\\{TARGET}.*")) {
 				message = message.replaceAll("\\{TARGET}", targetName);
 			}
 			if (message.matches(".*\\{DUVOD}.*")) {
 				message = message.replaceAll("\\{DUVOD}", reason);
 			}
 			message = message.replaceAll("(&([a-fk-or0-9]))", "$2");
 			return message;
 		} else {
 			return "Messsage = null";
 		}
 	}
 	
 	public void aktualizovatMySQLBan(String playerName, String targetName, String reason, String typ) {
 		if (playerName != null && targetName != null && reason != null && typ != null)
 			mysql.query("INSERT INTO `FoE_Banlist` (hrac, admin, duvod, datum, typ) VALUES (" + "'" + targetName + "'," + " '" + playerName + "'," + " '" + reason + "'," + " '" + System.currentTimeMillis() + "'," + " '" + typ + "')");
 		else
 			System.out.println("Nekde je chyba, null: " + playerName + "|" + targetName + "|" + reason + "|" + typ);
 	}
 	
 	public void deleteFolder(File folder) {
 		try {
 			File[] files = folder.listFiles();
 			if (files != null) {
 				for (File f : files) {
 					if (f.isDirectory())
 						deleteFolder(f);
 					else {
 						f.delete();
 					}
 				}
 			}
 			folder.delete();
 		} catch (Exception e) {
 			Writer writer = new StringWriter();
 			PrintWriter printWriter = new PrintWriter(writer);
 			e.printStackTrace(printWriter);
 			Error(writer.toString());
 		}
 	}
 	
 	public void kontrolaConfigu() {
 		try {
 			if (!config.contains("Prikazy.AdminChat"))
 				config.set("Prikazy.AdminChat", "/a");
 			
 			if (!config.contains("Prikazy.Manager.Ban"))
 				config.set("Prikazy.Manager.Ban", "/ban");
 			
 			if (!config.contains("Prikazy.Manager.Unban"))
 				config.set("Prikazy.Manager.Unban", "/unban");
 			
 			if (!config.contains("Prikazy.Manager.Kick"))
 				config.set("Prikazy.Manager.Kick", "/kick");
 			
 			if (!config.contains("Prikazy.Cenzura"))
 				config.set("Prikazy.Cenzura", "/cenzura");
 			
 			if (!config.contains("Prikazy.Gramatika"))
 				config.set("Prikazy.Gramatika", "/gramatika");
 			
 			if (!config.contains("Prikazy.Help"))
 				config.set("Prikazy.Help", "/help");
 			
 			if (!config.contains("Prikazy.VypnoutChat"))
 				config.set("Prikazy.VypnoutChat", "/chat");
 			
 			if (!config.contains("Prikazy.Nahranost"))
 				config.set("Prikazy.Nahranost", "/inf");
 			
 			if (!config.contains("Prikazy.Inventar"))
 				config.set("Prikazy.Inventar", "/inv");
 			
 			if (!config.contains("Prikazy.Teleport"))
 				config.set("Prikazy.Teleport", "/tp");
 			
 			if (!config.contains("Prikazy.Oznameni"))
 				config.set("Prikazy.Oznameni", "/zprava");
 			
 			if (!config.contains("Prikazy.Clear"))
 				config.set("Prikazy.Clear", "/clear");
 			
 			if (!config.contains("MySQL.Povolit"))
 				config.set("MySQL.Povolit", "ne");
 			
 			if (!config.contains("MySQL.hostname"))
 				config.set("MySQL.hostname", "localhost");
 			
 			if (!config.contains("MySQL.database"))
 				config.set("MySQL.database", "gs_xxxxx_1");
 			
 			if (!config.contains("MySQL.username"))
 				config.set("MySQL.username", "gs_xxxxx_1");
 			
 			if (!config.contains("MySQL.password"))
 				config.set("MySQL.password", "heslo");
 			
 			if (!config.contains("MySQL.port"))
 				config.set("MySQL.port", 3306);
 			
 			if (!config.contains("MySQL.Cas"))
 				config.set("MySQL.Cas", 30);
 			
 			if (!config.contains("Oznameni.Povolit"))
 				config.set("Oznameni.Povolit", "ano");
 			
 			if (!config.contains("Oznameni.Prefix"))
 				config.set("Oznameni.Prefix", "&8[&4FoE&8]");
 			
 			if (!config.contains("Oznameni.Suffix"))
 				config.set("Oznameni.Suffix", "&4");
 			
 			if (!config.contains("KdyzHracSe.Pripoji.Povolit"))
 				config.set("KdyzHracSe.Pripoji.Povolit", "ano");
 			
 			if (!config.contains("KdyzHracSe.Pripoji.Zprava"))
 				config.set("KdyzHracSe.Pripoji.Zprava", "&4{JMENO}&8 se pipojil do hry!");
 			
 			if (!config.contains("KdyzHracSe.Odpoji.Povolit"))
 				config.set("KdyzHracSe.Odpoji.Povolit", "ano");
 			
 			if (!config.contains("KdyzHracSe.Odpoji.Zprava"))
 				config.set("KdyzHracSe.Odpoji.Zprava", "&4{JMENO}&8 se odpojil ze hry!");
 			
 			if (!config.contains("KdyzHracSe.Vyhodi.Povolit"))
 				config.set("KdyzHracSe.Vyhodi.Povolit", "ano");
 			
 			if (!config.contains("KdyzHracSe.Vyhodi.Zprava"))
 				config.set("KdyzHracSe.Vyhodi.Zprava", "&4{JMENO}&8 byl vyhozen!");
 			
 			if (!config.contains("Nahranost.Povolit"))
 				config.set("Nahranost.Povolit", "ano");
 			
 			if (!config.contains("Nahranost.PrivitaciZprava.Povolit"))
 				config.set("Nahranost.PrivitaciZprava.Povolit", "ano");
 			
 			if (!config.contains("Nahranost.Zprava"))
 				config.set("Nahranost.Zprava", "&4{JMENO}&8 na serveru jste nahral &4{TYDEN}&8 tydnu, &4{DEN} &8dn, &4{HODIN} &8hodin, &4{MINUT} &8minut, &4{SEKUND}&8 sekund");
 			
 			if (!config.contains("AntiReklama.Povolit"))
 				config.set("AntiReklama.Povolit", "ano");
 			
 			if (!config.contains("AntiReklama.WEB.Zprava"))
 				config.set("AntiReklama.WEB.Zprava", "&4{JMENO}&8 byl banovan za reklamu na web!");
 			
 			if (!config.contains("AntiReklama.WEB.Akce"))
 				config.set("AntiReklama.WEB.Akce", "ban {JMENO} REKLAMA!");
 			
 			if (!config.contains("AntiReklama.WEB.Whitelist")) {
 				List<String> d = new ArrayList<String>();
 				d.add("www.frelania.eu");
 				config.set("AntiReklama.WEB.Whitelist", d);
 			}
 			
 			if (!config.contains("AntiReklama.IP.Akce"))
 				config.set("AntiReklama.IP.Akce", "ipban {JMENO} REKLAMA!");
 			
 			if (!config.contains("AntiReklama.IP.Zprava"))
 				config.set("AntiReklama.IP.Zprava", "&4{JMENO}&8 byl IPbanovan za reklamu!");
 			
 			if (!config.contains("AntiReklama.IP.Whitelist")) {
 				List<String> e = new ArrayList<String>();
 				e.add("93.91.250.111:27887");
 				config.set("AntiReklama.IP.Whitelist", e);
 			}
 			
 			if (!config.contains("Cenzura.Povolit"))
 				config.set("Cenzura.Povolit", "ano");
 			
 			if (!config.contains("Cenzura.Nahrada"))
 				config.set("Cenzura.Nahrada", "******");
 			
 			if (!config.contains("Cenzura.Zprava"))
 				config.set("Cenzura.Zprava", "{JMENO} nadvat se zde nesm!");
 			
 			if (!config.contains("Cenzura.Akce"))
 				config.set("Cenzura.Akce", "kick {JMENO} Poruovn pravidel: Nadvn.");
 			
 			if (!config.contains("Cenzura.Slova")) {
 				List<String> a = new ArrayList<String>();
 				a.add("debil");
 				a.add("kokot");
 				a.add("curak");
 				config.set("Cenzura.Slova", a);
 			}
 			
 			if (!config.contains("Gramatika.Povolit"))
 				config.set("Gramatika.Povolit", "ano");
 			
 			if (!config.contains("Gramatika.Duvody"))
 				config.set("Gramatika.Duvody.mislet", "&4Vyjmenovan Slovo - Vdycky tvrd Y! ' m&fY&4slet '");
 			
 			if (!config.contains("Gramatika.Vsude")) {
 				List<String> b = new ArrayList<String>();
 				b.add("kdiz,kdyz");
 				b.add("mislet,myslet");
 				config.set("Gramatika.Vsude", b);
 			}
 			
 			if (!config.contains("Gramatika.Cele")) {
 				List<String> c = new ArrayList<String>();
 				c.add("us,uz");
 				config.set("Gramatika.Cele", c);
 			}
 			
 			if (!config.contains("VypnoutChat.Povolit"))
 				config.set("VypnoutChat.Povolit", "ano");
 			
 			if (!config.contains("VypnoutChat.KdyzJeVypnutyChat"))
 				config.set("VypnoutChat.KdyzJeVypnutyChat", "&4Chat je vypnut, oprvnn pst maj jen &8opertoi&4!");
 			
 			if (!config.contains("VypnoutChat.KdyzSeVypne"))
 				config.set("VypnoutChat.KdyzSeVypne", "&4{JMENO} &8zakzal chat!");
 			
 			if (!config.contains("VypnoutChat.KdyzSeZapne"))
 				config.set("VypnoutChat.KdyzSeZapne", "&4{JMENO} &8povolil chat!");
 			
 			if (!config.contains("Ostatni.KdyzNemaOpravneni"))
 				config.set("Ostatni.KdyzNemaOpravneni", "&4Na tuto akci nemte &8oprvnn&4!");
 			
 			if (!config.contains("Ostatni.Upgrade"))
 				config.set("Ostatni.Upgrade", "ano");
 			
 			if (!config.contains("VyhledavatAktualizace.Povolit"))
 				config.set("VyhledavatAktualizace.Povolit", "ano");
 			
 			if (!config.contains("VyhledavatAktualizace.Cas"))
 				config.set("VyhledavatAktualizace.Cas", 10);
 			
 			if (!config.contains("AdminChat.Povolit"))
 				config.set("AdminChat.Povolit", "ano");
 			
 			if (!config.contains("AdminChat.Zprava"))
 				config.set("AdminChat.Zprava", "&8[&4AdminChat&8] &e{JMENO}:&4{ZPRAVA}");
 			
 			if (!config.contains("Ostatni.Nahranost.GUI.Povolit"))
 				config.set("Ostatni.Nahranost.GUI.Povolit", "ano");
 			
 			if (!config.contains("Ostatni.Nahranost.GUI.Tydny-Povolit"))
 				config.set("Ostatni.Nahranost.GUI.Tydny-Povolit", "ano");
 			
 			if (!config.contains("Ostatni.Nahranost.GUI.Dny-Povolit"))
 				config.set("Ostatni.Nahranost.GUI.Dny-Povolit", "ano");
 			
 			if (!config.contains("Ostatni.Nahranost.GUI.Hodiny-Povolit"))
 				config.set("Ostatni.Nahranost.GUI.Hodiny-Povolit", "ano");
 			
 			if (!config.contains("Ostatni.Nahranost.GUI.PocetHracu-Povolit"))
 				config.set("Ostatni.Nahranost.GUI.PocetHracu-Povolit", "ano");
 			
 			if (!config.contains("Ostatni.Nahranost.GUI.iConomy-Povolit"))
 				config.set("Ostatni.Nahranost.GUI.iConomy-Povolit", "ne");
 			
 			if (!config.contains("Ostatni.Nahranost.GUI.Nadpis"))
 				config.set("Ostatni.Nahranost.GUI.Nadpis", "&4F&8o&4E");
 			
 			if (!config.contains("Ostatni.Nahranost.GUI.Tydny"))
 				config.set("Ostatni.Nahranost.GUI.Tydny", "Nahrno Tdn:");
 			
 			if (!config.contains("Ostatni.Nahranost.GUI.Dny"))
 				config.set("Ostatni.Nahranost.GUI.Dny", "Nahrno Dn:");
 			
 			if (!config.contains("Ostatni.Nahranost.GUI.Hodiny"))
 				config.set("Ostatni.Nahranost.GUI.Hodiny", "Nahrno Hodin:");
 			
 			if (!config.contains("Ostatni.Nahranost.GUI.PocetHracu"))
 				config.set("Ostatni.Nahranost.GUI.PocetHracu", "Online:");
 			
 			if (!config.contains("Ostatni.Nahranost.GUI.iConomy"))
 				config.set("Ostatni.Nahranost.GUI.iConomy", "Penize:");
 			
 			if (!config.contains("TP.Povolit"))
 				config.set("TP.Povolit", "ano");
 			
 			if (!config.contains("TP.Zprava.Uspesne"))
 				config.set("TP.Zprava.Uspesne", "&4spn &8jste se teleportoval k &4{JMENO}");
 			
 			if (!config.contains("TP.Zprava.Offline"))
 				config.set("TP.Zprava.Offline", "&4Hr {JMENO} &8nen online!");
 			
 			if (!config.contains("AntiSpam.Povolit"))
 				config.set("AntiSpam.Povolit", "ano");
 			
 			if (!config.contains("AntiSpam.Zprava"))
 				config.set("AntiSpam.Zprava", "&4{JMENO} &8Muste pokat &4{SEKUND} &8sekundy.");
 			
 			if (!config.contains("AntiSpam.PockatSekund"))
 				config.set("AntiSpam.PockatSekund", 3);
 			
 			if (!config.contains("AntiSpam.Duplikace.Povolit"))
 				config.set("AntiSpam.Duplikace.Povolit", "ano");
 			
 			if (!config.contains("AntiSpam.Duplikace.Zprava"))
 				config.set("AntiSpam.Duplikace.Zprava", "&4Nemete poslat 2x stejnou zprvu&8!");
 			
 			if (!config.contains("Rezervace.Povolit"))
 				config.set("Rezervace.Povolit", "ano");
 			
 			if (!config.contains("Rezervace.Zprava"))
 				config.set("Rezervace.Zprava", "&4{JMENO} &8se pipojil a vy jste byl vyhozen ze hry.");
 			
 			if (!config.contains("Inventar.Povolit"))
 				config.set("Inventar.Povolit", "ano");
 			
 			if (!config.contains("Manager.Povolit"))
 				config.set("Manager.Povolit", "ano");
 			
 			if (!config.contains("Manager.Ban.Zprava"))
 				config.set("Manager.Ban.Zprava", "&4{TARGET} &8byl zabanovn &4{JMENO}&8 z dvodu &4{DUVOD}&8.");
 			
 			if (!config.contains("Manager.Unban.Zprava"))
 				config.set("Manager.Unban.Zprava", "&4{TARGET} &8byl unbanovn &4{JMENO}&8 z dvodu &4{DUVOD}&8.");
 			
 			if (!config.contains("Manager.Kick.Zprava"))
 				config.set("Manager.Kick.Zprava", "&4{TARGET} &8byl vyhozen &4{JMENO}&8 z dvodu &4{DUVOD}&8.");
 			
 			if (!config.contains("Vtipy.Povolit"))
 				config.set("Vtipy.Povolit", "ano");
 			
 			if (!config.contains("Vtipy.Interval"))
 				config.set("Vtipy.Interval", 1);
 			
 			if (!config.contains("Vtipy.Format"))
 				config.set("Vtipy.Format", "\n&e{VTIP}&f\n");
 			
 			if (!config.contains("zpravaAdminum.Povolit"))
 				config.set("zpravaAdminum.Povolit", "ano");
 			
 			if (!config.contains("capsLock.Povolit"))
 				config.set("capsLock.Povolit", "ano");
 			
 			if (!config.contains("capsLock.Zprava"))
 				config.set("capsLock.Zprava", "&4Zachovej klidnou hlavu a pi malma psmenkama.");
 			
 			if (!config.contains("clearChat.Povolit"))
 				config.set("clearChat.Povolit", "ano");
 			
 			if (!config.contains("umrtiZpravy.Povolit"))
 				config.set("umrtiZpravy.Povolit", "ano");
 			
 			if (!config.contains("umrtiZpravy.Creeper"))
 				config.set("umrtiZpravy.Creeper", "&4{JMENO} &8byl zabit &4{MOB}&8.");
 			
 			if (!config.contains("umrtiZpravy.Zombie"))
 				config.set("umrtiZpravy.Zombie", "&4{JMENO} &8byl zabit &4{MOB}&8.");
 			
 			if (!config.contains("umrtiZpravy.Skeleton"))
 				config.set("umrtiZpravy.Skeleton", "&4{JMENO} &8byl zabit &4{MOB}&8.");
 			
 			if (!config.contains("umrtiZpravy.Spider"))
 				config.set("umrtiZpravy.Spider", "&4{JMENO} &8byl zabit &4{MOB}&8.");
 			
 			if (!config.contains("umrtiZpravy.Wither"))
 				config.set("umrtiZpravy.Wither", "&4{JMENO} &8byl zabit &4{MOB}&8.");
 			
 			if (!config.contains("umrtiZpravy.Wolf"))
 				config.set("umrtiZpravy.Wolf", "&4{JMENO} &8byl zabit &4{MOB}&8.");
 			
 			if (!config.contains("umrtiZpravy.Ghast"))
 				config.set("umrtiZpravy.Ghast", "&4{JMENO} &8byl zabit &4{MOB}&8.");
 			
 			if (!config.contains("umrtiZpravy.Explosive"))
 				config.set("umrtiZpravy.Explosive", "&4{JMENO} &8byl zabit &4{MOB}&8.");
 			
 			if (!config.contains("umrtiZpravy.PigZombie"))
 				config.set("umrtiZpravy.PigZombie", "&4{JMENO} &8byl zabit &4{MOB}&8.");
 			
 			if (!config.contains("umrtiZpravy.Slime"))
 				config.set("umrtiZpravy.Slime", "&4{JMENO} &8byl zabit &4{MOB}&8.");
 			
 			if (!config.contains("umrtiZpravy.SmallFireball"))
 				config.set("umrtiZpravy.SmallFireball", "&4{JMENO} &8byl zabit &4{MOB}&8.");
 			
 			if (!config.contains("umrtiZpravy.Witch"))
 				config.set("umrtiZpravy.Witch", "&4{JMENO} &8byl zabit &4{MOB}&8.");
 			
 			if (!config.contains("umrtiZpravy.Enderman"))
 				config.set("umrtiZpravy.Enderman", "&4{JMENO} &8byl zabit &4{MOB}&8.");
 			
 			if (!config.contains("umrtiZpravy.EnderDragon"))
 				config.set("umrtiZpravy.EnderDragon", "&4{JMENO} &8byl zabit &4{MOB}&8.");
 			
 			if (!config.contains("umrtiZpravy.Blaze"))
 				config.set("umrtiZpravy.Blaze", "&4{JMENO} &8byl zabit &4{MOB}&8.");
 			
 			if (!config.contains("umrtiZpravy.Player"))
 				config.set("umrtiZpravy.Player", "&4{JMENO} &8byl zabit &4{MOB}&8.");
 			
 			if (!config.contains("umrtiZpravy.Silverfish"))
 				config.set("umrtiZpravy.Silverfish", "&4{JMENO} &8byl zabit &4{MOB}&8.");
 			
 			if (!config.contains("umrtiZpravy.Giant"))
 				config.set("umrtiZpravy.Giant", "&4{JMENO} &8byl zabit &4{MOB}&8.");
 			
 			if (!config.contains("whiteList.Povolit"))
 				config.set("whiteList.Povolit", "ano");
 			
 			if (!config.contains("whiteList.Zprava"))
 				config.set("whiteList.Zprava", "&4Nejste na Whitelistu!!");
 			
 			saveConfig(config, configFile);
 		} catch (Exception e) {
 			Writer writer = new StringWriter();
 			PrintWriter printWriter = new PrintWriter(writer);
 			e.printStackTrace(printWriter);
 			Error(writer.toString());
 		}
 	}
 	
 	public void helpMessageToLog(String playerName, String Message) {
 		try {
 			File u = new File("plugins/FoE/help.log");
 			FileWriter fw = new FileWriter(u, true);
 			PrintWriter pw = new PrintWriter(fw);
 			Date date = new Date();
 			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
 			String time = sdf.format(date);
 			pw.println("# " + playerName + " - " + time + "\n" + Message + "\n");
 			pw.flush();
 			pw.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public String nahraditMezery(String zCeho) {
 		return zCeho = zCeho.replaceAll(" ", "_");
 	}
 	
 	public void statistiky() {
 		try {
 			String serverName = Bukkit.getServerName();
 			String name;
 			if (serverName.equals("Unknown Server")) {
 				Random rnd = new Random();
 				name = "US_" + rnd.nextInt(10000);
 			} else {
 				name = serverName;
 			}
 			URL url = new URL("http://www.foe.frelania.eu/servers/post.php?ip=" + this.getServer().getIp() + "&port=" + this.getServer().getPort() + "&jmeno=" + nahraditMezery(name) + "&verze=" + this.getDescription().getVersion());
 			if (url != null) {
 				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 				connection.setRequestMethod("POST");
 				connection.setDoOutput(true);
 				connection.setDoInput(true);
 				connection.setUseCaches(false);
 				connection.connect();
 				OutputStreamWriter w = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
 				w.write("ip=" + URLEncoder.encode(this.getServer().getIp(), "UTF-8") + "&port=" + URLEncoder.encode(String.valueOf(this.getServer().getPort()), "UTF-8") + "&jmeno=" + URLEncoder.encode(nahraditMezery(name), "UTF-8") + "&verze=" + URLEncoder.encode(this.getDescription().getVersion(), "UTF-8"));
 				w.flush();
 				BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 				w.close();
 				rd.close();
 			}
 		} catch (IOException e) {
 			Writer writer = new StringWriter();
 			PrintWriter printWriter = new PrintWriter(writer);
 			e.printStackTrace(printWriter);
 			Error(writer.toString());
 		}
 	}
 	
 	public void zkontrolovatVerziPluginu() {
 		try {
 			URL url = new URL("http://www.frelania.eu/MyImages/FoE.txt");
 			URLConnection connection = url.openConnection();
 			
 			if (connection.getContentLength() == -1) {
 				System.out.println("Neni pripojeni k internetu, moznost overit novou verzi je nemozne.");
 			} else {
 				BufferedReader br = new BufferedReader(new InputStreamReader(new URL("http://www.frelania.eu/MyImages/FoE.txt").openStream()));
 				String newVersion = br.readLine();
 				String nowVersion = getDescription().getVersion();
 				String newVersionWithoutDots = removeDots(newVersion);
 				String nowVersionWithoutDots = removeDots(nowVersion);
 				int intNew = Integer.valueOf(newVersionWithoutDots).intValue();
 				int intNow = Integer.valueOf(nowVersionWithoutDots).intValue();
 				BufferedReader br2 = new BufferedReader(new InputStreamReader(new URL("http://www.frelania.eu/MyImages/FoEv" + intNew + ".txt").openStream(), "UTF8"));
 				if (intNew > intNow) {
 					for (Player p : Bukkit.getOnlinePlayers()) {
 						if (p.isOp()) {
 							p.sendMessage("FoE - Je dostupn nov verze " + ChatColor.RED + newVersion);
 							p.sendMessage(nahraditBarvy(br2.readLine().split("\\n").toString()));
 						}
 					}
 					System.out.println("FoE - Je dostupna nova verze " + newVersion);
 				}
 				br.close();
 			}
 		} catch (IOException e) {
 			Writer writer = new StringWriter();
 			PrintWriter printWriter = new PrintWriter(writer);
 			e.printStackTrace(printWriter);
 			Error(writer.toString());
 		}
 	}
 	
 	public String nahraditBarvy(String zprava) {
 		return zprava = zprava.replaceAll("(&([a-fk-or0-9]))", "$2");
 	}
 	
 	public String removeDots(String fromWhat) {
 		fromWhat = fromWhat.replace(".", "");
 		return fromWhat;
 	}
 	
 	public String nahradit(String zprava, String hrac) {
 		try {
 			uzivatel(hrac);
 			if (zprava.matches(".*\\{JMENO}.*")) {
 				zprava = zprava.replaceAll("\\{JMENO}", hrac);
 			}
 			zprava = zprava.replaceAll("(&([a-fk-or0-9]))", "$2");
 		} catch (Exception e) {
 			Writer writer = new StringWriter();
 			PrintWriter printWriter = new PrintWriter(writer);
 			e.printStackTrace(printWriter);
 			Error(writer.toString());
 		}
 		return zprava;
 	}
 	
 	public void saveConfig(YamlConfiguration config, File configFile) {
 		try {
 			config.save(configFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public boolean Status(YamlConfiguration config, String node) {
 		if (node != null && config != null) {
 			if (config.getString(node).equalsIgnoreCase("ano"))
 				return true;
 		} else {
 			System.out.println(node + " nebyl nalezen, aktualizujte config.");
 		}
 		return false;
 	}
 	
 	@EventHandler
 	public void onPlayerPreProcessCommand(PlayerCommandPreprocessEvent event) {
 		String vysledek = "";
 		String args[] = event.getMessage().split(" ");
 		for (int i = 1; i < args.length; i++) {
 			vysledek = (vysledek + (i > 1 ? " " : "") + args[i]);
 		}
 		
 		if (adminChatPovolit) {
 			if (args[0].equals(config.getString("Prikazy.AdminChat"))) {
 				Bukkit.getServer().dispatchCommand(event.getPlayer(), "acmd " + vysledek);
 				event.setCancelled(true);
 			}
 		}
 		if (managerBan) {
 			if (args[0].equals(config.getString("Prikazy.Manager.Ban"))) {
 				Bukkit.getServer().dispatchCommand(event.getPlayer(), "bancmd " + vysledek);
 				event.setCancelled(true);
 			}
 			if (args[0].equals(config.getString("Prikazy.Manager.Unban"))) {
 				Bukkit.getServer().dispatchCommand(event.getPlayer(), "unbancmd " + vysledek);
 				event.setCancelled(true);
 			}
 			if (args[0].equals(config.getString("Prikazy.Manager.Kick"))) {
 				Bukkit.getServer().dispatchCommand(event.getPlayer(), "kickcmd " + vysledek);
 				event.setCancelled(true);
 			}
 		}
 		if (cenzuraPovolit) {
 			if (args[0].equals(config.getString("Prikazy.Cenzura"))) {
 				Bukkit.getServer().dispatchCommand(event.getPlayer(), "cenzuracmd " + vysledek);
 				event.setCancelled(true);
 			}
 		}
 		if (gramatikaPovolit) {
 			if (args[0].equals(config.getString("Prikazy.Gramatika"))) {
 				Bukkit.getServer().dispatchCommand(event.getPlayer(), "gramatikacmd " + vysledek);
 				event.setCancelled(true);
 			}
 		}
 		if (zpravaAdminum) {
 			if (args[0].equals(config.getString("Prikazy.Help"))) {
 				Bukkit.getServer().dispatchCommand(event.getPlayer(), "helpcmd " + vysledek);
 				event.setCancelled(true);
 			}
 		}
 		if (vypnoutChatPovolit) {
 			if (args[0].equals(config.getString("Prikazy.VypnoutChat"))) {
 				Bukkit.getServer().dispatchCommand(event.getPlayer(), "chatcmd " + vysledek);
 				event.setCancelled(true);
 			}
 		}
 		if (nahranostPovolit) {
 			if (args[0].equals(config.getString("Prikazy.Nahranost"))) {
 				Bukkit.getServer().dispatchCommand(event.getPlayer(), "infcmd " + vysledek);
 				event.setCancelled(true);
 			}
 		}
 		if (inventarPovolit) {
 			if (args[0].equals(config.getString("Prikazy.Inventar"))) {
 				Bukkit.getServer().dispatchCommand(event.getPlayer(), "invcmd " + vysledek);
 				event.setCancelled(true);
 			}
 		}
 		if (teleportPovolit) {
 			if (args[0].equals(config.getString("Prikazy.Teleport"))) {
 				Bukkit.getServer().dispatchCommand(event.getPlayer(), "tpcmd " + vysledek);
 				event.setCancelled(true);
 			}
 		}
 		if (oznameniPovolit) {
 			if (args[0].equals(config.getString("Prikazy.Oznameni"))) {
 				Bukkit.getServer().dispatchCommand(event.getPlayer(), "zpravacmd " + vysledek);
 				event.setCancelled(true);
 			}
 		}
 		if (clearChat) {
 			if (args[0].equals(config.getString("Prikazy.Clear"))) {
 				Bukkit.getServer().dispatchCommand(event.getPlayer(), "clearcmd " + vysledek);
 				event.setCancelled(true);
 			}
 		}
 	}
 }
