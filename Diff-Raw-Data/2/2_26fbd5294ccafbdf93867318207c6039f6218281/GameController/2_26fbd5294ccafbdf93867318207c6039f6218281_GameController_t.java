 package runner.game;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import runner.RunForMoney;
 import runner.event.AttackEvent;
 import runner.event.CommandEvent;
 import runner.event.PlayerDeadEvent;
 import runner.event.QuitJoinEvent;
 import runner.util.ChatUtil;
 
 /**
  * @author Jason
  * 
  */
 public class GameController {
 
 	static public enum GameStatus {
 		Standy, Waiting, Running
 	}
 
 	private final int TPS = 20;
 	private long totalTime = 60 * 10; // 6 mins at default
 	private long startTime;
 	private GameStatus gameStatus;
 	private List<RFMPlayer> runnerList;
 	private List<RFMPlayer> hunterList;
 	RunForMoney runForMoney;
 
 	public RunForMoney getRunForMoney() {
 		return runForMoney;
 	}
 
 	private PrizeDispatcher prizeDispatcher;
 	private InventoryManager invManager;
 
 	public GameController(RunForMoney runForMoney) {
 		runnerList = new ArrayList<RFMPlayer>();
 		hunterList = new ArrayList<RFMPlayer>();
 		this.runForMoney = runForMoney;
 		this.prizeDispatcher = new PrizeDispatcher(runForMoney);
 		this.invManager = new InventoryManager(this);
 		gameStatus = GameStatus.Standy;
 	}
 
 	public void addHunter(Player player) {
 		if (getRunner(player) != null) {
 			removeRunner(player);
 		}
 		hunterList.add(new RFMPlayer(player.getName(), PlayerType.HUNTER));
 		runForMoney.getTeleporter().moveToPortal(player, "RFMjoin");
 	}
 
 	public void addRunner(Player player) {
 		runnerList.add(new RFMPlayer(player.getName(), PlayerType.RUNNER));
 		runForMoney.getTeleporter().moveToPortal(player, "RFMjoin");
 	}
 
 	public void beginStatus() {
 		broadCastInGame(ChatColor.GOLD + "CAyHt״w30III");
 		for (RFMPlayer p : hunterList) {
 			Player hunter = (Bukkit.getServer().getPlayer(p.getName()));
 			if (hunter != null) {
 				hunter.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,
 						30 * TPS, 1));
 			}
 		}
 
 		// set time out
 		runForMoney.getServer().getScheduler()
 				.scheduleSyncDelayedTask(runForMoney, new Runnable() {
 					@Override
 					public void run() {
 						broadCastInGame(ChatColor.GOLD + "yHtצ^_III");
 
 					}
 				}, 30 * TPS);
 	}
 
 	public void lastStatus() {
 		broadCastInGame(ChatColor.GOLD + "CiJռƶqAyHtץ[֡III");
 		for (RFMPlayer p : hunterList) {
 			Player hunter = (Bukkit.getServer().getPlayer(p.getName()));
 			if (hunter != null) {
 				hunter.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
 						30 * TPS, 1));
 			}
 		}
 
 	}
 
 	public void blindStatus() {
 		double chance = 0.333;
 		Random r = new Random();
 		if (chance > r.nextDouble()) {
			broadCastInGame(ChatColor.GOLD + "]­H]I ҦH⤣30I");
 			for (RFMPlayer p : hunterList) {
 				Player hunter = (Bukkit.getServer().getPlayer(p.getName()));
 				if (hunter != null) {
 					hunter.addPotionEffect(new PotionEffect(
 							PotionEffectType.BLINDNESS, 30 * TPS, 1));
 				}
 			}
 
 			for (RFMPlayer p : runnerList) {
 				Player runner = (Bukkit.getServer().getPlayer(p.getName()));
 				if (runner != null) {
 					runner.addPotionEffect(new PotionEffect(
 							PotionEffectType.BLINDNESS, 30 * TPS, 1));
 				}
 			}
 
 			// set time out
 			runForMoney.getServer().getScheduler()
 					.scheduleSyncDelayedTask(runForMoney, new Runnable() {
 						@Override
 						public void run() {
 							broadCastInGame(ChatColor.GOLD + "·twhoAI");
 
 						}
 					}, 30 * TPS);
 		}
 	}
 
 	public void checkIfGameover() {
 		if (gameStatus == GameStatus.Running) {
 			if (getRunnerAlive() <= 0) {
 				ChatUtil.broadcast(ChatColor.RED + "Ҧk`̬ҳQIII");
 				stop();
 			}
 
 			if (getHunterAlive() <= 0) {
 				ChatUtil.broadcast(ChatColor.RED + "ҦyHCIII");
 				stop();
 			}
 		}
 	}
 
 	private void checkAllPlayerBeforeStart() {
 		// check if runners are online
 		for (RFMPlayer rfmPlayer : runnerList) {
 			Player player = getPlayer(rfmPlayer);
 			if (player == null) {
 				rfmPlayer.setAlive(false);
 				broadCastInGame(ChatColor.AQUA + rfmPlayer.getName()
 						+ " C}lɤbCϤAPwC!" + ChatColor.RESET);
 			}
 		}
 
 		// check if hunters are online
 		for (RFMPlayer rfmPlayer : hunterList) {
 			Player player = getPlayer(rfmPlayer);
 			if (player == null) {
 				rfmPlayer.setAlive(false);
 				broadCastInGame(ChatColor.AQUA + rfmPlayer.getName()
 						+ " C}lɤbCϤAPwC!" + ChatColor.RESET);
 			}
 		}
 
 	}
 
 	public void checkPlayerQuit(Player player) {
 
 		// Get RFMPlayer to make sure it's a in-game player
 		RFMPlayer rfmPlayer = getHunter(player);
 		if (rfmPlayer == null)
 			rfmPlayer = getRunner(player);
 
 		// when player join or quit from arena world, or it's a in-game player
 		// send to observer
 		if ((rfmPlayer != null)
 				|| (player != null && player.getWorld() != null && player
 						.getWorld().getName()
 						.equalsIgnoreCase(Constants.ARENA_WORLD_NAME))) {
 			// teleport to observer portal
 			runForMoney.getTeleporter().moveToPortal(player, "RFMobserver");
 		}
 
 		// send message and check alive if it's a in-game player
 		if (rfmPlayer != null) {
 			String type = null;
 			if (rfmPlayer.getType() == PlayerType.HUNTER) {
 				type = "yH";
 			} else {
 				type = "k`";
 			}
 
 			if (rfmPlayer.isAlive()) {
 				rfmPlayer.setAlive(false);
 				broadCastInGame(ChatColor.AQUA + rfmPlayer.getName() + "("
 						+ type + ")" + " FC!" + ChatColor.RESET);
 
 				checkIfGameover();
 			}
 
 		}
 	}
 
 	public void checkRunterKillsRunner(Player hunter, Player runner) {
 		RFMPlayer rfmRunner = getRunner(runner);
 		RFMPlayer rfmHunter = getHunter(hunter);
 		if (rfmRunner != null && rfmHunter != null && rfmRunner.isAlive()
 				&& rfmHunter.isAlive()) {
 			rfmRunner.setAlive(false);
 			rfmHunter.addKills();
 
 			// teleport to observer portal
 			runForMoney.getTeleporter().moveToPortal(runner, "RFMobserver");
 
 			broadCastInGame(ChatColor.LIGHT_PURPLE + "yH " + hunter.getName()
 					+ " Fk`  " + runner.getName() + " III (sHƳ"
 					+ getRunnerAlive() + " H)\n" + getTime());
 			checkIfGameover();
 		}
 	}
 
 	public void checkPlayerDeath(Player player) {
 		RFMPlayer rfmRunner = getRunner(player);
 
 		if (rfmRunner != null && rfmRunner.isAlive()) {
 			rfmRunner.setAlive(false);
 
 			// teleport to observer portal
 			runForMoney.getTeleporter().moveToPortal(player, "RFMobserver");
 
 			broadCastInGame(ChatColor.RED + "k`  " + player.getName()
 					+ " ƬG`III (sHƳ" + getRunnerAlive() + " H)\n"
 					+ getTime());
 
 			checkIfGameover();
 		}
 	}
 
 	public RFMPlayer getHunter(Player player) {
 		for (RFMPlayer p : hunterList) {
 			if (p.getName().equalsIgnoreCase(player.getName())) {
 				return p;
 			}
 		}
 		return null;
 	}
 
 	public int getHunterAlive() {
 		int alives = 0;
 		for (RFMPlayer p : hunterList) {
 			if (p.isAlive()) {
 				alives++;
 			}
 		}
 		return alives;
 	}
 
 	public List<RFMPlayer> getHunterList() {
 		return hunterList;
 	}
 
 	public boolean join(Player player) {
 		if (getRunner(player) == null && getHunter(player) == null) {
 			addRunner(player);
 
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public RFMPlayer getRunner(Player player) {
 		for (RFMPlayer p : runnerList) {
 			if (p.getName().equalsIgnoreCase(player.getName())) {
 				return p;
 			}
 		}
 		return null;
 	}
 
 	public int getRunnerAlive() {
 		int alives = 0;
 		for (RFMPlayer p : runnerList) {
 			if (p.isAlive()) {
 				alives++;
 			}
 		}
 		return alives;
 	}
 
 	public List<RFMPlayer> getRunnerList() {
 		return runnerList;
 	}
 
 	public String getStatus() {
 		StringBuffer statusString = new StringBuffer();
 
 		// prepare runner
 		statusString.append(ChatColor.AQUA + "ɪ (@ " + runnerList.size()
 				+ "H/sH" + getRunnerAlive() + ") ");
 
 		for (RFMPlayer p : runnerList) {
 			ChatColor color = ChatColor.RESET;
 			if (p.isAlive()) {
 				color = ChatColor.AQUA;
 			} else {
 				color = ChatColor.GRAY;
 			}
 			statusString.append(color + p.getName() + ChatColor.RESET + ", ");
 		}
 
 		statusString.append("\n");
 
 		// prepare hunter
 
 		statusString.append(ChatColor.GREEN + "yH (@ " + hunterList.size()
 				+ "H) ");
 
 		for (RFMPlayer p : hunterList) {
 			ChatColor color = ChatColor.RESET;
 			if (p.isAlive()) {
 				color = ChatColor.AQUA;
 			} else {
 				color = ChatColor.GRAY;
 			}
 			statusString.append(color + p.getName() + "(" + p.getKills() + ") "
 					+ ChatColor.RESET + ", ");
 		}
 
 		statusString.append("\n");
 
 		// prepare time
 		statusString.append(getTime());
 
 		return statusString.toString();
 	}
 
 	public void checkPlayerLeave(RFMPlayer rfmPlayer) {
 
 		Player player = getPlayer(rfmPlayer);
 		if (player != null) {
 			runForMoney.getTeleporter().moveToPortal(player, "RFMexit");
 		}
 	}
 
 	public String getTime() {
 		StringBuffer statusString = new StringBuffer();
 		if (gameStatus == GameStatus.Running) {
 
 			long gameTime = (System.currentTimeMillis() - startTime) / 1000;
 			statusString.append("CɶwgL: " + ChatUtil.secToString(gameTime)
 					+ ", |: " + ChatUtil.secToString(totalTime - gameTime));
 
 		} else {
 			statusString.append("Cɶ]w " + ChatUtil.secToString(totalTime));
 		}
 
 		return statusString.toString();
 	}
 
 	public void removeRunner(Player player) {
 		for (RFMPlayer p : runnerList) {
 			if (p.getName().equalsIgnoreCase(player.getName())) {
 				runnerList.remove(p);
 				return;
 			}
 		}
 	}
 
 	public void removeHunter(Player player) {
 		for (RFMPlayer p : hunterList) {
 			if (p.getName().equalsIgnoreCase(player.getName())) {
 				hunterList.remove(p);
 				return;
 			}
 		}
 	}
 
 	public void reset() {
 
 		EntityDamageEvent.getHandlerList().unregister(runForMoney);
 		PlayerCommandPreprocessEvent.getHandlerList().unregister(runForMoney);
 		PlayerQuitEvent.getHandlerList().unregister(runForMoney);
 		runForMoney.getServer().getScheduler().cancelTasks(runForMoney);
 		setGameStatus(GameStatus.Standy);
 		EntityDamageEvent.getHandlerList().unregister(runForMoney);
 		runnerList.clear();
 		hunterList.clear();
 	}
 
 	public void setTime(int secs) {
 		// must greater than 1 mins
 		if (secs < 60) {
 			secs = 60;
 		}
 		broadCastInGame("Cɶ]w " + ChatUtil.secToString(secs));
 		totalTime = secs;
 	}
 
 	public void start() {
 
 		ChatUtil.broadcast(ChatColor.GOLD + "==kC}lARkaIII==");
 
 		Bukkit.getPluginManager().registerEvents(new AttackEvent(this),
 				runForMoney);
 
 		Bukkit.getPluginManager().registerEvents(new CommandEvent(this),
 				runForMoney);
 
 		Bukkit.getPluginManager().registerEvents(new QuitJoinEvent(this),
 				runForMoney);
 
 		Bukkit.getPluginManager().registerEvents(new PlayerDeadEvent(this),
 				runForMoney);
 
 		setGameStatus(GameStatus.Running);
 		startTime = System.currentTimeMillis();
 
 		checkAllPlayerBeforeStart();
 		ChatUtil.broadcast(getStatus());
 
 		// show status each 30 secs
 		runForMoney.getServer().getScheduler()
 				.scheduleAsyncRepeatingTask(runForMoney, new Runnable() {
 					@Override
 					public void run() {
 						broadCastInGame(getStatus());
 					}
 				}, 30 * TPS, 30 * TPS);
 
 		// set time out
 		runForMoney.getServer().getScheduler()
 				.scheduleSyncDelayedTask(runForMoney, new Runnable() {
 					@Override
 					public void run() {
 						ChatUtil.broadcast(getStatus());
 						stop();
 					}
 				}, totalTime * TPS);
 
 		// speed up hunters at last 30 secs
 		runForMoney.getServer().getScheduler()
 				.scheduleSyncDelayedTask(runForMoney, new Runnable() {
 					@Override
 					public void run() {
 						lastStatus();
 					}
 				}, (totalTime - 30) * TPS);
 
 		// blindStatus
 		runForMoney.getServer().getScheduler()
 				.scheduleSyncDelayedTask(runForMoney, new Runnable() {
 					@Override
 					public void run() {
 						blindStatus();
 					}
 				}, (totalTime / 4 * 1) * TPS);
 
 		// blindStatus
 		runForMoney.getServer().getScheduler()
 				.scheduleSyncDelayedTask(runForMoney, new Runnable() {
 					@Override
 					public void run() {
 						blindStatus();
 					}
 				}, (totalTime / 4 * 2) * TPS);
 
 		// blindStatus
 		runForMoney.getServer().getScheduler()
 				.scheduleSyncDelayedTask(runForMoney, new Runnable() {
 					@Override
 					public void run() {
 						blindStatus();
 					}
 				}, (totalTime / 4 * 3) * TPS);
 
 		beginStatus();
 
 		checkIfGameover();
 
 	}
 
 	public void stop() {
 		ChatUtil.broadcast("CIII");
 		ChatUtil.broadcast(getStatus());
 
 		EntityDamageEvent.getHandlerList().unregister(runForMoney);
 		PlayerCommandPreprocessEvent.getHandlerList().unregister(runForMoney);
 		PlayerQuitEvent.getHandlerList().unregister(runForMoney);
 		PlayerLoginEvent.getHandlerList().unregister(runForMoney);
 		EntityDeathEvent.getHandlerList().unregister(runForMoney);
 
 		runForMoney.getServer().getScheduler().cancelTasks(runForMoney);
 
 		if (gameStatus == GameStatus.Running) {
 			// dispatch Prize to players
 			disPatchPrize();
 
 			// teleport all players out of arena
 			teleportAllPlayerOut();
 		}
 
 		setGameStatus(GameStatus.Standy);
 	}
 
 	private void disPatchPrize() {
 		try {
 			prizeDispatcher.dispatchToRunners(runnerList);
 			prizeDispatcher.dispatchToHunters(hunterList);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		ChatUtil.broadcast("~oeIII");
 	}
 
 	private Player getPlayer(RFMPlayer rfmPlayer) {
 		Player player = runForMoney.getServer().getPlayer(rfmPlayer.getName());
 		return player;
 	}
 
 	public void teleportAllPlayerOut() {
 		ChatUtil.broadcast("3 ǰeaXIII");
 		runForMoney.getServer().getScheduler()
 				.scheduleSyncDelayedTask(runForMoney, new Runnable() {
 
 					@Override
 					public void run() {
 						for (RFMPlayer rfmPlayer : runnerList) {
 							checkPlayerLeave(rfmPlayer);
 						}
 						for (RFMPlayer rfmPlayer : hunterList) {
 							checkPlayerLeave(rfmPlayer);
 						}
 						ChatUtil.broadcast("aǰeIII");
 					}
 				}, 3 * TPS);
 
 	}
 
 	public GameStatus getGameStatus() {
 		return gameStatus;
 	}
 
 	public void setGameStatus(GameStatus gameStatus) {
 		this.gameStatus = gameStatus;
 	}
 
 	public RFMPlayer getRFMPlayer(Player player) {
 		RFMPlayer rfmPlayer = null;
 		rfmPlayer = getRunner(player);
 		if (rfmPlayer == null) {
 			rfmPlayer = getHunter(player);
 			return rfmPlayer;
 		} else {
 			return rfmPlayer;
 		}
 	}
 
 	public void broadCastInGame(String message) {
 		for (RFMPlayer rfmPlayer : runnerList) {
 			ChatUtil.sendToSender(getPlayer(rfmPlayer), message);
 		}
 		for (RFMPlayer rfmPlayer : hunterList) {
 			ChatUtil.sendToSender(getPlayer(rfmPlayer), message);
 		}
 	}
 }
