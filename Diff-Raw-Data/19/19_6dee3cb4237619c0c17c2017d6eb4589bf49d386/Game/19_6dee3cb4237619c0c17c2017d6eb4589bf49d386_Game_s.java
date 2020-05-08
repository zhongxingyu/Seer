 /**
  * TagGame - Package: syam.taggame.game
  * Created: 2012/11/07 19:11:58
  */
 package syam.taggame.game;
 
 import java.text.SimpleDateFormat;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffectType;
 
 import syam.taggame.TagGame;
 import syam.taggame.enums.GameResult;
 import syam.taggame.enums.GameState;
 import syam.taggame.enums.GameTeam;
 import syam.taggame.exception.GameStateException;
 import syam.taggame.exception.TagGameException;
 import syam.taggame.manager.GameManager;
 import syam.taggame.util.Actions;
 import syam.taggame.util.Util;
 
 /**
  * Game (Game.java)
  * @author syam(syamn)
  */
 public class Game {
 	// シングルトンインスタンス
 	//private Game game;
 
 	// Logger
 	private static final Logger log = TagGame.log;
 	private static final String logPrefix = TagGame.logPrefix;
 	private static final String msgPrefix = TagGame.msgPrefix;
 
 	// plugin instance
 	private final TagGame plugin;
 
 	private String gameID; // 一意なゲームID ログ用
 	// private Stage stage;
 
 	private int remainSec = 60 * 10; // 残り時間
 	private int timerThreadID = -1; // タイマータスクID
 	private int starttimeInSec = 10;
 	private int starttimerThreadID = -1;
 
 	private GameState state = GameState.WAITING;
 
 	// 参加プレイヤー
 	private Map<GameTeam, Set<String>> playersMap = new ConcurrentHashMap<GameTeam, Set<String>>();
 	private Set<String> taggers = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
 	private Set<String> runners = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
 
 	// スポーン地点
 	private Map<GameTeam, Location> spawnMap = new ConcurrentHashMap<GameTeam, Location>();
 
 	private Map<String, String> tabListMap = new ConcurrentHashMap<String, String>();
 
 	/**
 	 * コンストラクタ
 	 * @param plugin TagGameプラグイン
 	 */
 	public Game(final TagGame plugin){
 		this.plugin = plugin;
 
 		GameManager.setGame(this);
 	}
 
 	/**
 	 * このゲームを開始待機中にする
 	 * @param sender CommandSender
 	 */
 	public void ready(final CommandSender sender){
 		if (!this.state.equals(GameState.WAITING)){
 			throw new GameStateException("This game is already using!");
 		}
 
 		// init
 		taggers.clear();
 		runners.clear();
 		// mapping
 		playersMap.clear();
 		playersMap.put(GameTeam.TAGGER, taggers);
 		playersMap.put(GameTeam.RUNNER, runners);
 
 		// スポーン地点チェック
 		if (getSpawns().size() != 2){
 			throw new GameStateException("Team spawn area is not defined properly!");
 		}
 
 		// 待機
 		this.state = GameState.READYING;
 
 		// アナウンス
 		Actions.broadcastMessage(msgPrefix+"&2鬼ごっこの参加受付が開始されました！");
 		//Actions.broadcastMessage(msgPrefix+"&2 参加料:&6 "+entryFeeMsg+ "&2   賞金:&6 "+awardMsg);
		Actions.broadcastMessage(msgPrefix+"&2 '&6/tag join [tagger] コマンドで参加してください！");
 
 		// ロギング
 		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd-HHmmss");
 		this.gameID = sdf.format(new Date());
 
 		log("========================================");
 		log("Sender "+sender.getName()+" Ready to Game");
 		//log("Stage: "+stage.getName()+ " ("+stage.getFileName()+")");
 		//log("TeamPlayerLimit: "+stage.getTeamLimit()+" GameTime: "+stage.getGameTime()+" sec");
 		//log("Award: "+stage.getAward()+" EntryFee:"+stage.getEntryFee());
 		log("========================================");
 	}
 
 	/**
 	 * ゲームを開始する
 	 * @param sender
 	 */
 	public void start(final CommandSender sender){
 		//TODO call event here
 
 		if (this.state.equals(GameState.RUNNING)){
 			Actions.message(sender, "&cこのゲームは既に始まっています！");
 			return;
 		}
 
 		if (taggers.size() <= 0 || runners.size() <= 0){
 			Actions.message(sender, "&cプレイヤーが参加していないチームがあります！");
 			return;
 		}
 
 		// スポーン地点の再チェック
 		if (getSpawns().size() != 2){
 			Actions.message(sender, "&c各チームスポーン地点が正しく設定されていません");
 			return;
 		}
 
 
 		// アナウンス
 		Actions.broadcastMessage(msgPrefix+"&2鬼ごっこが始まりました！");
		Actions.broadcastMessage(msgPrefix+"&f &a制限時間: &f"+Actions.getTimeString(remainSec)+"&f | &b鬼: &f"+taggers.size()+"&b人&f | &c参加者: &f"+runners.size()+"&c人");
 
 		// 開始
 		timer(); // タイマースタート
 		this.state = GameState.RUNNING;
 
 		tpSpawnLocation();
 
 		tabListMap.clear();
 
 		// 全プレイヤーを回す
 		for (Map.Entry<GameTeam, Set<String>> entry : playersMap.entrySet()){
 			GameTeam team = entry.getKey();
 			for (String name : entry.getValue()){
 				Player player = Bukkit.getPlayer(name);
 				if (player == null || !player.isOnline())
 					continue;
 
 				// ゲームモード変更
 				player.setGameMode(GameMode.SURVIVAL);
 
 				// 頭を変える
 				player.getInventory().setHelmet(new ItemStack(team.getBlockID(), 1, (short)0, team.getBlockData()));
 
 				// 回復
 				player.setHealth(20);
 				player.setFoodLevel(20);
 
 				// 効果のあるポーションをすべて消す
 				if (player.hasPotionEffect(PotionEffectType.JUMP))
 					player.removePotionEffect(PotionEffectType.JUMP);
 				if (player.hasPotionEffect(PotionEffectType.SPEED))
 					player.removePotionEffect(PotionEffectType.SPEED);
 				if (player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE))
 					player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
 				if (player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE))
 					player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
 				if (player.hasPotionEffect(PotionEffectType.BLINDNESS))
 					player.removePotionEffect(PotionEffectType.BLINDNESS);
 				if (player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE))
 					player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
 
 				// Tabリスト名変更
 				tabListMap.put(player.getName(), player.getPlayerListName());
 				String tabname = ("§" + team.getColor().charAt(1) + player.getName());
 				if (tabname.length() > 16){
 					tabname = tabname.substring(0, 12) + ChatColor.WHITE + "..";
 				}
 				player.setPlayerListName(tabname);
 			}
 		}
 
 		log("========================================");
 		log("Sender "+sender.getName()+" Start Game");
 		log("Taggers("+taggers.size()+"): "+Util.join(taggers, ", "));
 		log("Runners("+runners.size()+"): "+Util.join(runners, ", "));
 		log("========================================");
 	}
 
 	public void finish(GameResult result, boolean timeout, String reason){
 		// アナウンス
 		Actions.broadcastMessage(msgPrefix+"&2鬼ごっこが終わりました！");
 
 		switch(result){
 			case WIN_TAGGER:
 				Actions.broadcastMessage(msgPrefix+"&2鬼チームの勝ちです！");
 				break;
 			case WIN_RUNNER:
 				Actions.broadcastMessage(msgPrefix+"&2鬼チームの負けです！");
 				break;
 			case STOP:
 				Actions.broadcastMessage(msgPrefix+"&2このゲームは&c無効&2になりました");
 				break;
 			default:
 				throw new TagGameException("Undefined GameResult! Please report this!");
 		}
 
 		if (reason != null && reason != ""){
 			Actions.broadcastMessage(msgPrefix+"&2理由: "+reason);
 		}
 
 		// Logging
 		log("========================================");
 		log(" * TagGame Finished");
 		log(" Result: "+result.name());
 		log(" Reason: "+reason);
 		log("========================================");
 
 		// ログの終わり
 		gameID = null;
 
 		// 参加プレイヤーをスポーン地点に移動させる
 		tpSpawnLocation();
 		// 同じゲーム参加者のインベントリをクリア
 		for (Set<String> names : playersMap.values()){
 			for (String name : names){
 				Player player = Bukkit.getPlayer(name);
 				// オンラインチェック
 				if (player != null && player.isOnline()){
 					// アイテムクリア
 					player.getInventory().setHelmet(null);
 
 					// TABリスト名を戻す
 					String tabname = tabListMap.get(player.getName());
 					if (tabname != null){
 						player.setPlayerListName(tabname);
 					}
 				}
 			}
 		}
 
 		// TODO call event here
 
 		// 初期化
 		init();
 	}
 
 	public void finish(GameResult result){
 		this.finish(result, false, null);
 	}
 
 	/**
 	 * タイマー終了によって呼ばれるゲーム終了処理
 	 */
 	public void timeout(){
 		this.finish(GameResult.WIN_RUNNER, true, null);
 	}
 
 	/**
 	 * 初期化
 	 */
 	private void init(){
 		cancelTimerTask();
 
 		this.state = GameState.WAITING;
 	}
 
 	/* ***** 参加プレイヤー関係 ***** */
 	public boolean addPlayer(Player player, GameTeam team){
 		// チームの存在確認
 		if (player == null || team == null || !playersMap.containsKey(team)){
 			return false;
 		}
 		// 追加
 		playersMap.get(team).add(player.getName());
 		log("+ Player "+player.getName()+" joined "+team.name()+" Team!");
 		return true;
 	}
 	public GameTeam getPlayerTeam(Player player){
 		String name = player.getName();
 		for(Map.Entry<GameTeam, Set<String>> ent : playersMap.entrySet()){
 			// すべてのチームセットを回す
 			if(ent.getValue().contains(name)){
 				return ent.getKey();
 			}
 		}
 		// 一致なし nullを返す
 		return null;
 	}
 	public Set<String> getPlayersSet() {
 		Set<String> ret = new HashSet<String>();
 		for (Set<String> teamSet : playersMap.values()){
 			ret.addAll(teamSet);
 		}
 		return ret;
 	}
 	public boolean isJoined(String playerName) {
 		return getPlayersSet().contains(playerName);
 	}
 	public boolean isJoined(Player player) {
 		if (player == null) return false;
 		return this.isJoined(player.getName());
 	}
 
 	/* ***** 参加しているプレイヤーへのアクション関係 ***** */
 	/**
 	 * ゲーム参加者全員にメッセージを送る
 	 * @param msg メッセージ
 	 */
 	public void message(String message){
 		// 全チームメンバーにメッセージを送る
 		for (Set<String> set : playersMap.values()){
 			for (String name : set){
 				if (name == null) continue;
 				Player player = Bukkit.getPlayer(name);
 				if (player != null && player.isOnline())
 					Actions.message(player, message);
 			}
 		}
 	}
 	/**
 	 * 特定チームにのみメッセージを送る
 	 * @param msg メッセージ
 	 * @param team 対象のチーム
 	 */
 	public void message(GameTeam team, String message){
 		if (team == null || !playersMap.containsKey(team))
 			return;
 
 		// チームメンバーでループさせてメッセージを送る
 		for (String name : playersMap.get(team)){
 			if (name == null) continue;
 			Player player = Bukkit.getServer().getPlayer(name);
 			if (player != null && player.isOnline())
 				Actions.message(player, message);
 		}
 	}
 
 	public void tpSpawnLocation(){
 		for (Map.Entry<GameTeam, Set<String>> entry : playersMap.entrySet()){
 			GameTeam team = entry.getKey();
 			Location loc = getSpawn(team);
 			if (loc == null) continue;
 
 			for (String name : entry.getValue()){
 				if (name == null) continue;
 				final Player player = Bukkit.getPlayer(name);
 				if (player != null && player.isOnline()){
 					// イスに座っているときにワープできない不具合修正
 					Entity vehicle = player.getVehicle();
 					if (vehicle != null){
 						// アイテムに座っている＝イスプラグインを使って座っている
 						if (vehicle instanceof Item){
 							vehicle.remove(); // アイテム削除
 						}else{
 							// その他、ボートやマインカートなら単にeject
 							//vehicle.eject();
 							player.leaveVehicle();
 						}
 					}
 
 					// 現在地点が別ワールドならプレイヤーデータに戻る地点を書き込む
 					/*if (!player.getWorld().equals(loc.getWorld())){
 						PlayerManager.getProfile(player.getName()).setTpBackLocation(player.getLocation());
 					}*/
 
 					player.teleport(loc, TeleportCause.PLUGIN);
 				}
 			}
 		}
 	}
 
 	/**
 	 * 指定したチームのプレイヤーセットを返す
 	 * @param team 取得するチーム
 	 * @return プレイヤーセット またはnull
 	 */
 	public Set<String> getPlayersSet(GameTeam team){
 		if (team == null || !playersMap.containsKey(team))
 			return null;
 
 		return playersMap.get(team);
 	}
 	public Map<GameTeam, Set<String>> getPlayersMap(){
 		return playersMap;
 	}
 
 	/* ***** タイマー関係 ***** */
 	/**
 	 * 開始時のカウントダウンタイマータスクを開始する
 	 */
 	public void start_timer(final CommandSender sender){
 		// カウントダウン秒をリセット
 		starttimeInSec = plugin.getConfigs().getStartCountdownInSec();
 		if (starttimeInSec <= 0){
 			start(sender);
 			return;
 		}
 
 		Actions.broadcastMessage(msgPrefix+"&2まもなく鬼ごっこが始まります！");
 
 		// タイマータスク起動
 		//starttimerThreadID = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable() {
 		starttimerThreadID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
 			public void run(){
 				/* 1秒ごとに呼ばれる */
 
 				// 残り時間がゼロになった
 				if (starttimeInSec <= 0){
 					cancelTimerTask(); // タイマー停止
 					start(sender); // ゲーム開始
 					return;
 				}
 
 				message(msgPrefix+ "&aあと" +starttimeInSec+ "秒でこのゲームが始まります！");
 				starttimeInSec--;
 			}
 		}, 0L, 20L);
 	}
 
 	/**
 	 * メインのタイマータスクを開始する
 	 */
  	public void timer(){
 		// タイマータスク
 		timerThreadID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new GameTimerTask(this.plugin, this), 0L, 20L);
 	}
 	/**
 	 * タイマータスクが稼働中の場合停止する
 	 */
 	public void cancelTimerTask(){
 		if (starttimerThreadID != -1){
 			plugin.getServer().getScheduler().cancelTask(starttimerThreadID);
 			starttimerThreadID = -1;
 		}
 		if (timerThreadID != -1){
 			// タスクキャンセル
 			plugin.getServer().getScheduler().cancelTask(timerThreadID);
 			timerThreadID = -1;
 		}
 	}
 
 	/**
 	 * このゲームの残り時間(秒)を取得する
 	 * @return 残り時間(秒)
 	 */
 	public int getRemainTime(){
 		return remainSec;
 	}
 	public void tickRemainTime(){
 		remainSec--;
 	}
 
 	// 残り時間設定
 	public void setGameTime(int sec){
 		this.remainSec = sec;
 	}
 
 	/* ***** スポーン地点 ***** */
 	public void setSpawn(GameTeam team, Location loc){
 		spawnMap.put(team, loc);
 	}
 	public Location getSpawn(GameTeam team){
 		if (team == null || !spawnMap.containsKey(team))
 			return null;
 		return spawnMap.get(team);
 	}
 	public Map<GameTeam, Location> getSpawns(){
 		return spawnMap;
 	}
 	public void setSpawns(Map<GameTeam, Location> spawns){
 		this.spawnMap.clear();
 		this.spawnMap.putAll(spawns);
 	}
 
 
 	/* getter / setter */
 	public GameState getState(){
 		return this.state;
 	}
 	public int getStarttimerThreadID(){
 		return starttimerThreadID;
 	}
 
 	/**
 	 * 各ゲームごとのログを取る
 	 * @param line ログ
 	 */
 	public void log(final String line){
 		if (gameID != null){
 			String filepath = plugin.getConfigs().getDetailDirectory() + gameID + ".log";
 			Actions.log(filepath, line);
 		}
 	}
 }
