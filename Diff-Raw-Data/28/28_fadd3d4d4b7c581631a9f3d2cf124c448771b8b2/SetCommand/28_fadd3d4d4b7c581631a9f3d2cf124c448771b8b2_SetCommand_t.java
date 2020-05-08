 package syam.BoatRace.Command;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 
 import syam.BoatRace.BoatRace;
 import syam.BoatRace.Enum.Configables;
 import syam.BoatRace.Race.Race;
 import syam.BoatRace.Race.RaceManager;
 import syam.BoatRace.Util.Actions;
 import syam.BoatRace.Util.WorldEditHandler;
 
 public class SetCommand extends BaseCommand {
 	public SetCommand(){
 		bePlayer = true;
 		name = "set";
 		argLength = 0;
 		usage = "<- show command help";
 	}
 
 	@Override
 	public boolean execute() {
 		// race set のみ (サブ引数なし)
 		if (args.size() <= 0){
 			if (RaceManager.getManager(player) != null){
 				RaceManager.setManager(player, null);
				Actions.message(null, player, "&a管理モードを解除しました！");
 			}else{
 				Actions.message(null, player, "&c設定項目を指定してください！");
 				sendAvailableConf();
 			}
 			return true;
 		}
 
 
 		// レース取得
 		Race race = RaceManager.getSelectedRace(player);
 		if (race == null){
 			Actions.message(null, player, "&c先に編集するゲームを選択してください");
 			return true;
 		}
 
 		// 設定可能項目名を回す
 		Configables conf = null;
 		for (Configables check : Configables.values()){
 			if (check.name().equalsIgnoreCase(args.get(0))){
 				conf = check; break;
 			}
 		}
 		// 列挙体にあったかチェック
 		if (conf == null){
 			Actions.message(null, player, "&cその設定項目は存在しません");
 			sendAvailableConf();
 			return true;
 		}
 
 		switch (conf){
 			case START:
 				return setStart(race);
 			case GOAL:
 				return setGoal(race);
 			case CHECKPOINT:
 				return setCheckpoint(race);
 		}
 
 		return true;
 	}
 
 	/**
 	 * スタート地点を設定する
 	 * @param race
 	 * @return
 	 */
 	private boolean setStart(Race race){
 		if (RaceManager.getManager(player) != null){
 			RaceManager.setManager(player, null);
 			Actions.message(null, player, "&a管理モードを終了しました。");
 		}else{
 			RaceManager.setManager(player, Configables.START);
 			String tool = Material.getMaterial(plugin.getConfigs().toolID).name();
 			Actions.message(null, player, "&aスタート地点管理モードを開始しました。選択ツール: "+ tool);
 		}
 		return true;
 	}
 
 	/**
 	 * ゴール地点を設定する
 	 * @param race 設定対象のレース
 	 * @return
 	 */
 	private boolean setGoal(Race race){
 		// WorldEdit選択領域取得
 		Block[] corners = WorldEditHandler.getWorldEditRegion(player);
 
 		// エラー プレイヤーへのメッセージ送信はWorldEditHandlerクラスで処理
 		if (corners == null || corners.length != 2) return true;
 
 		Block block1 = corners[0];
 		Block block2 = corners[1];
 
 		// ワールドチェック
 		if (block1.getWorld() != Bukkit.getWorld(plugin.getConfigs().gameWorld)){
 			Actions.message(null, player, "&c指定しているエリアはゲームワールドではありません！");
 			return true;
 		}
 
 		// 設定
 		race.setGoal(block1.getLocation(), block2.getLocation());
 
		Actions.message(null, player, "&aゴールエリアを設定しました！");
 		return true;
 	}
 
 	/**
 	 * チェックポイントを設定する
 	 * @param race 設定対象のレース
 	 * @return
 	 */
 	private boolean setCheckpoint(Race race){
 		// WorldEdit選択領域取得
 		Block[] corners = WorldEditHandler.getWorldEditRegion(player);
 
 		// エラー プレイヤーへのメッセージ送信はWorldEditHandlerクラスで処理
 		if (corners == null || corners.length != 2) return true;
 
 		Block block1 = corners[0];
 		Block block2 = corners[1];
 
 		// ワールドチェック
 		if (block1.getWorld() != Bukkit.getWorld(plugin.getConfigs().gameWorld)){
 			Actions.message(null, player, "&c指定しているエリアはゲームワールドではありません！");
 			return true;
 		}
 
 		// 設定
 		race.addCheckpoint(block1.getLocation(), block2.getLocation());
 
		Actions.message(null, player, "&aチェックポイントを設定しました！");
 		return true;
 	}
 
 	/**
 	 * 有効な設定可能項目名をプレイヤーに送る
 	 */
 	private void sendAvailableConf(){
 		Actions.message(null, player, "&7 START / GOAL / CHECKPOINT");
 	}
 
 	@Override
 	public boolean permission() {
 		return sender.hasPermission("boat.admin.set");
 	}
 
 }
