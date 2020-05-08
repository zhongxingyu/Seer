 package syam.BoatRace.Race;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.potion.PotionEffectType;
 
 import syam.BoatRace.BoatRace;
 import syam.BoatRace.Util.Actions;
 import syam.BoatRace.Util.Cuboid;
 
 public class Race{
 	// Logger
 	public static final Logger log = BoatRace.log;
 	private static final String logPrefix = BoatRace.logPrefix;
 	private static final String msgPrefix = BoatRace.msgPrefix;
 
 	private final BoatRace plugin;
 
 	/* ***** レースデータ ***** */
 	private String GameID; // 一意なゲームID ログ用
 	private String fileName; // ゲームデータのファイル名
 	private String gameName; // ゲーム名
 	private int playerLimit = 16; // 最大プレイヤー数
 	private int gameTimeInSeconds = 600; // 1ゲームの制限時間
 	private int remainSec = gameTimeInSeconds; // 1ゲームの制限時間
 	private int timerThreadID = -1; // タイマータスクのID
 	private int starttimerInSec = 10;
 	private int starttimerThreadID = -1;
 	private boolean ready = false; // 待機状態フラグ
 	private boolean started = false; // 開始状態フラグ
 
 	private int award = 1000; // 賞金
 	private int entryFee = 100; // エントリー料
 
 	// 参加プレイヤー
 	private HashMap<String, BRBoat> players = new HashMap<String, BRBoat>();
 	// 開始ポジション :複数必要
 	private Set<Location> startPos = new HashSet<Location>();
 	// ゴールゾーン :1ゾーン
 	private Cuboid goalZone = null;
 	// チェックポイント :複数ゾーン可
 	private Set<Cuboid> checkpoints = new HashSet<Cuboid>();
 
 	/**
 	 * コンストラクタ
 	 * @param plugin
 	 * @param name
 	 */
 	public Race(final BoatRace plugin, final String name){
 		this.plugin = plugin;
 
 		// ゲームデータ設定
 		this.gameName = name;
 
 		// ファイル名設定
 		this.fileName = this.gameName + ".yml";
 
 		// ゲームをメインクラスに登録
 		plugin.races.put(this.gameName, this);
 	}
 
 	/**
 	 * このレースを初期化する
 	 */
 	public void init(){
 		// プレイヤーリスト初期化
 		players.clear();
 
 		// タイマー関係初期化
 		//cancelTimerTask();
 		timerThreadID = -1;
 		remainSec = gameTimeInSeconds;
 
 		// フラグ初期化
 		started = false;
 		ready = false;
 	}
 
 	/**
 	 * このゲームを開始待機中にする
 	 * @param sender
 	 */
 	public void ready(CommandSender sender){
 		if (started){
 			Actions.message(sender, null, "&cこのゲームは既に始まっています");
 			return;
 		}
 		if (ready){
 			Actions.message(sender, null, "&cこのゲームは既に参加受付中です");
 			return;
 		}
 
 		// プレイヤーリスト初期化
 		players.clear();
 
 		// エリアチェック
 		if (startPos.size() < 1 || goalZone == null){
 			Actions.message(sender, null, "&cスタートまたはゴール地点が正しく設定されていません");
 			return;
 		}
 
 		// 待機
 		ready = true;
 
 		// アナウンス
 		Actions.broadcastMessage(msgPrefix+"&2ボートレース'&6"+getName()+"&2'の参加受付が開始されました！");
 		Actions.broadcastMessage(msgPrefix+"&2 '&6/boat join "+getName()+"&2' コマンドで参加してください！");
 	}
 
 	/**
 	 * このゲームを開始する
 	 * @param sender
 	 */
 	public void start(CommandSender sender){
 		if (started){
 			Actions.message(sender, null, "&cこのゲームは既に始まっています");
 			return;
 		}
 
 		// 人数チェック
 		if (players.size() < 1){
 			Actions.message(sender, null, "&cプレイヤーが参加していません！");
			init();
 			return;
 		}
 
 		// スタート地点の再チェック
 		if (players.size() > startPos.size()){
 			Actions.message(sender, null, "&c参加プレイヤーがスタート地点設定数より多いので開始できません！");
			init();
 			return;
 		}
 
 		// チェストなどのロールバックをここで
 		// rollabckChests();
 
 		// 開始
 		timer();
 		started = true;
 
 		// アナウンス
 		Actions.broadcastMessage(msgPrefix+"&2ボートレース'&6"+getName()+"&2'が始まりました！");
 		Actions.broadcastMessage(msgPrefix+"&f &a制限時間: &f"+Actions.getTimeString(gameTimeInSeconds)+"&f | &b参加者: &f"+players.size()+"&b人");
 
 		// 参加者を回す
 		for (String name : players.keySet()){
 			if (name == null) continue;
 			Player player = Bukkit.getPlayer(name);
 			// オフラインプレイヤーをスキップ
 			if (player == null || !player.isOnline())
 				continue;
 
 			// アイテムクリア
 			player.getInventory().clear();
 			player.getInventory().setHelmet(null);
 			player.getInventory().setChestplate(null);
 			player.getInventory().setLeggings(null);
 			player.getInventory().setBoots(null);
 
 			// 回復
 			player.setHealth(20);
 			player.setFoodLevel(20);
 
 			// ポーション削除
 			clearPot(player);
 		}
 	}
 
 	/**
 	 * レースがタイムアウトした
 	 */
 	public void timeout(){
 		// アナウンス
 		Actions.broadcastMessage(msgPrefix+"&2ボートレース'&6"+getName()+"&2'は時間切れです！");
 
 		// ログの終わり
 		GameID = null;
 
 		// 参加者を回す
 		for (String name : players.keySet()){
 			if (name == null) continue;
 			Player player = Bukkit.getPlayer(name);
 			// オフラインプレイヤーをスキップ
 			if (player == null || !player.isOnline())
 				continue;
 
 			// アイテムクリア
 			player.getInventory().clear();
 			player.getInventory().setHelmet(null);
 			player.getInventory().setChestplate(null);
 			player.getInventory().setLeggings(null);
 			player.getInventory().setBoots(null);
 
 			// 回復
 			player.setHealth(20);
 			player.setFoodLevel(20);
 		}
 
 		// 初期化
 		init();
 	}
 
 
 	/* ***** 参加プレイヤー関係 ***** */
 
 	/**
 	 * このレースの参加者にメッセージを送る
 	 * @param message 送信するメッセージ
 	 */
 	public void message(String message){
 		for (String name : players.keySet()){
 			if (name == null) continue;
 			Player player = Bukkit.getPlayer(name);
 			if (player != null && player.isOnline())
 				Actions.message(null, player, message);
 		}
 	}
 	/**
 	 * プレイヤーリストを返す
 	 * @return プレイヤーのハッシュセット
 	 */
 	public Set<String> getPlayersSet(){
 		return players.keySet();
 	}
 	/**
 	 * このゲームにプレイヤーを追加する
 	 * @param player
 	 * @return
 	 */
 	public boolean addPlayer(Player player){
 		// 人数チェック
 		if (players.size() >= playerLimit || players.size() >= startPos.size()){
 			return false;
 		}
 
 		// 追加
 		players.put(player.getName(), null);
 		return true;
 	}
 
 	/**
 	 * 指定したプレイヤーが既に参加しているかどうかを返す
 	 * @param player
 	 * @return 参加していればtrue
 	 */
 	public boolean isJoined(Player player){
 		if (player == null) return false;
 		return isJoined(player.getName());
 	}
 	public boolean isJoined(String name){
 		if (players.containsKey(name))
 			return true;
 		else
 			return false;
 	}
 
 	/**
 	 * 効果のあるポーションを削除する
 	 * @param player
 	 */
 	private void clearPot(Player player){
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
 	}
 
 
 	/* ***** タイマー関係 ***** */
 
 	public void start_timer(final CommandSender sender){
 		// カウントダウン秒をリセット
 		starttimerInSec = plugin.getConfigs().startCountdownInSec;
 		if (starttimerInSec <= 0){
 			start(sender);
 			return;
 		}
 
 		Actions.broadcastMessage(msgPrefix+"&6まもなくレースゲーム'"+getName()+"'が始まります！");
 
 		// タイマータスク
 		starttimerThreadID = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable() {
 			public void run(){
 				/* 1秒ごとに呼ばれる */
 				message(msgPrefix+ "&aあと" +starttimerInSec+ "秒でこのレースが始まります！");
 
 				// 残り時間がゼロになった
 				if (starttimerInSec <= 0){
 					cancelTimerTask(); // タイマー停止
 					start(sender); // ゲーム開始
 					return;
 				}
 				starttimerInSec--;
 			}
 		}, 0L, 20L);
 	}
 	/**
 	 * メインのタイマータスクを開始する
 	 */
 	public void timer(){
 		// タイマータスク
 		timerThreadID = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable() {
 			public void run(){
 				/* 1秒ごとに呼ばれる */
 
 				// 残り時間がゼロになった
 				if (remainSec <= 0){
 					cancelTimerTask(); // タイマー停止
 					timeout(); // ゲーム終了
 					return;
 				}
 
 				// 15秒以下
 				if (remainSec <= 15){
 					message(msgPrefix+ "&aゲーム終了まで あと "+remainSec+" 秒です！");
 				}
 				// 30秒前
 				else if (remainSec == 30){
 					message(msgPrefix+ "&aゲーム終了まで あと "+remainSec+" 秒です！");
 				}
 				// 60秒間隔
 				else if ((remainSec % 60) == 0){
 					int remainMin = remainSec / 60;
 					message(msgPrefix+ "&aゲーム終了まで あと "+remainMin+" 分です！");
 				}
 
 				remainSec--;
 			}
 		}, 0L, 20L);
 	}
 	/**
 	 * タイマータスクが稼働中の場合停止する
 	 */
 	public void cancelTimerTask(){
 		if (ready && starttimerThreadID != -1){
 			plugin.getServer().getScheduler().cancelTask(starttimerThreadID);
 		}
 		if (started && timerThreadID != -1){
 			// タスクキャンセル
 			plugin.getServer().getScheduler().cancelTask(timerThreadID);
 		}
 	}
 	/**
 	 * このゲームの残り時間(秒)を取得する
 	 * @return 残り時間(秒)
 	 */
 	public int getRemainTime(){
 		return remainSec;
 	}
 
 	/* ***** 開始位置 ***** */
 	public void addStartPos(Location loc){
 		startPos.add(loc);
 	}
 	public void removeStartPos(Location loc){
 		if (startPos.contains(loc))
 			startPos.remove(loc);
 	}
 	public boolean isStartPos(Location loc){
 		if (startPos.contains(loc))
 			return true;
 		else
 			return false;
 	}
 	public Set<Location> getStartPos(){
 		return startPos;
 	}
 
 	/* ***** ゴールゾーン関係 ***** */
 	public void setGoal(Location pos1, Location pos2){
 		goalZone = new Cuboid(pos1, pos2);
 	}
 	public Cuboid getGoalZone(){
 		return goalZone;
 	}
 
 
 	/* ***** チェックポイント関係 ***** */
 	public void addCheckpoint(Location pos1, Location pos2){
 		checkpoints.add(new Cuboid(pos1, pos2));
 	}
 	public Set<Cuboid> getCheckpoints(){
 		return checkpoints;
 	}
 	public void setCheckpoints(Set<Cuboid> cuboids){
 		this.checkpoints.clear();
 		this.checkpoints.addAll(cuboids);
 	}
 
 	/* ***** ゲーム全般のgetterとsetter ***** */
 
 	/**
 	 * ファイル名を設定
 	 * @param filename
 	 */
 	public void setFileName(String filename){
 		this.fileName = filename;
 	}
 
 	/**
 	 * ファイル名を取得
 	 * @return
 	 */
 	public String getFileName(){
 		return fileName;
 	}
 
 	/**
 	 * ゲーム名を返す
 	 * @return このゲームの名前
 	 */
 	public String getName(){
 		return gameName;
 	}
 
 	/**
 	 * 開始待機中かどうか返す
 	 * @return
 	 */
 	public boolean isReady(){
 		return ready;
 	}
 	/**
 	 * 開始中かどうか返す
 	 * @return
 	 */
 	public boolean isStarting(){
 		return started;
 	}
 
 	/**
 	 * このゲームの制限時間(秒)を設定する
 	 * @param sec 制限時間(秒)
 	 */
 	public void setGameTime(int sec){
 		// もしゲーム開始中なら何もしない
 		if (!started){
 			cancelTimerTask(); // しなくてもいいかな…？
 			gameTimeInSeconds = sec;
 			remainSec = gameTimeInSeconds;
 		}
 	}
 	/**
 	 * このゲームの制限時間(秒)を返す
 	 * @return
 	 */
 	public int getGameTime(){
 		return gameTimeInSeconds;
 	}
 
 	/**
 	 * 人数上限を設定する
 	 * @param limit 人数上限
 	 */
 	public void setPlayerLimit(int limit){
 		this.playerLimit = limit;
 	}
 	/**
 	 * 人数上限を取得
 	 * @return 人数上限
 	 */
 	public int getPlayerLimit(){
 		return playerLimit;
 	}
 }
 
