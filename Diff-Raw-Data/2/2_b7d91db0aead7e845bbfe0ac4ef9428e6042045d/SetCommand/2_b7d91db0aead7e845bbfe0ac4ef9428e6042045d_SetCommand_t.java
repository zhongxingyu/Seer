 package syam.ProjectManager.Command;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.block.Block;
 
 import syam.ProjectManager.Project.Project;
 import syam.ProjectManager.Project.ProjectConfigManager;
 import syam.ProjectManager.Util.Actions;
 import syam.ProjectManager.Util.Util;
 import syam.ProjectManager.Util.WorldEditHandler;
 
 public class SetCommand extends BaseCommand{
 	public SetCommand(){
 		bePlayer = true;
 		name = "set";
 		argLength = 0;
 		usage = "<option> [value] <- set project option";
 	}
 
 	@Override
 	public boolean execute() {
 		if (args.size() <= 0){
 			Actions.message(null, player, "&c設定項目を指定してください！");
 			sendAvailableConf();
 			return true;
 		}
 
 		// プロジェクト取得
 		Project project = ProjectConfigManager.getSelectedProject(player.getName());
 		if (project == null){
 			Actions.message(null, player, "&c先に編集するプロジェクトを選択してください");
 			return true;
 		}
 
 		// 設定可能項目名を回す
 		Configables conf = null;
 		for (Configables check : Configables.values()){
 			if (check.name().equalsIgnoreCase(args.get(0))){
 				conf = check;
 				break;
 			}
 		}
 		if (conf == null){
 			Actions.message(sender, null, "&cその設定項目は存在しません！");
 			sendAvailableConf();
 			return true;
 		}
 
 		// Check Permission
 
 		// プロジェクトマネージャ または管理権限を持っているプレイヤー以外からの設定を拒否
 		if (!project.isManager(player.getName()) && !player.hasPermission("pm.admin.editAllProject")){
 			Actions.message(sender, null, "&cあなたはこのプロジェクトのマネージャではありません！");
 			return true;
 		}
 		// Permission OK
 
 
 		// 設定項目によって処理を分ける
 		switch (conf){
 			case SPAWN: // スポーン地点設定
 				return setSpawn(project);
 
 			case REGION: // エリア設定
 				if (!player.hasPermission("pm.admin.set.region")){
 					Actions.message(sender, null, "&cあなたはプロジェクト領域を変更する権限がありません！");
 					return true;
 				}
 				return setRegion(project);
 
 			case CREATIVE: // クリエイティブ設定
 				if (!player.hasPermission("pm.admin.set.creative")){
 					Actions.message(sender, null, "&cあなたはデフォルトモードの設定を変更する権限がありません！");
 					return true;
 				}
 				return setCreative(project);
 
 			// 定義漏れ
 			default:
 				Actions.message(sender, null, "&c設定項目が不正です 開発者にご連絡ください");
 				log.warning(logPrefix+ "Undefined configables! Please report this!");
 				break;
 		}
 
 		return true;
 	}
 
 	/* ***** ここから各設定関数 ****************************** */
 
 	private boolean setSpawn(Project project){
		if (project.getCreative() && !plugin.getConfigs().creativeWorlds.contains(player.getWorld().getName())){
 			Actions.message(null, player, "&cプロジェクトがクリエイティブモード設定なので、このワールドでスポーン地点設定できません！");
 			return true;
 		}
 
 		project.setSpawnLocation(player.getLocation());
 
 		Actions.message(null, player, "&aプロジェクトID'&6"+project.getID()+"&a'のスポーン地点を設定しました！");
 		return true;
 	}
 
 	private boolean setRegion(Project project){
 		// WorldEdit選択領域取得
 		Block[] corners = WorldEditHandler.getWorldEditRegion(player);
 		// エラー プレイヤーへのメッセージ送信はWorldEditHandlerクラスで処理
 		if (corners == null || corners.length != 2) return true;
 
 		Block block1 = corners[0];
 		Block block2 = corners[1];
 
 		// ワールドチェック
 		if (project.getCreative() && !plugin.getConfigs().creativeWorlds.contains(block1.getWorld().getName())){
 			Actions.message(null, player, "&cプロジェクトのモードがクリエイティブになっているため、このワールドでエリアを設定できません");
 			return true;
 		}
 
 		// 設定
 		project.setArea(block1.getLocation(), block2.getLocation());
 		Actions.message(null, player, "&aプロジェクトID'&6"+project.getID()+"&a'の領域を設定しました！");
 
 		// update dynmap
 		plugin.getDynmap().updateRegions();
 
 		return true;
 	}
 
 	private boolean setCreative(Project project){
 		if (args.size() <= 1){
 			Actions.message(sender, null, "&c true または false を指定してください！");
 			return true;
 		}
 
 		Boolean creative = false; // デフォルトfalse
 		String value = args.get(1).trim();
 
 		if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes")){
 			if (project.getSpawnLocation() != null && !plugin.getConfigs().creativeWorlds.contains(project.getSpawnLocation().getWorld().getName())){
 				Actions.message(null, player, "&cプロジェクトの拠点がクリエイティブモードになれるワールドではありません！");
 				project.setCreative(false);
 				return true;
 			}
 			creative = true;
 		}else if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("no")){
 			creative = false;
 		}else{
 			Actions.message(sender, null, "&c値が不正です！ true または false を指定してください！");
 			return true;
 		}
 
 		String result = "";
 		if (creative) result = "&a有効";
 		else result = "&c無効";
 
 		project.setCreative(creative);
 		Actions.message(sender, null, "&aプロジェクトID'&6"+project.getID()+"&a'のクリエイティブモードは"+result+"&aに設定されました！");
 
 		// update dynmap
 		plugin.getDynmap().updateRegions();
 
 		return true;
 	}
 
 	/* ***** ここまで **************************************** */
 
 	private void sendAvailableConf(){
 		List<String> col = new ArrayList<String>();
 		for (Configables conf : Configables.values()){
 			col.add(conf.name());
 		}
 
 		Actions.message(sender, null, "&6 " + Util.join(col, ", ").toLowerCase());
 	}
 
 	/**
 	 * 設定可能項目の列挙体
 	 * @author syam
 	 */
 	enum Configables{
 		SPAWN("スポーン地点"),
 		REGION("エリア"),
 		CREATIVE("クリエイティブ"),
 		;
 
 		private String name;
 
 		Configables(String name){
 			this.name = name;
 		}
 
 		public String getConfigName(){
 			return this.name;
 		}
 	}
 
 	@Override
 	public boolean permission() {
 		return sender.hasPermission("pm.user.set");
 	}
 }
