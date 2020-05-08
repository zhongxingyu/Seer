 package syam.ProjectManager.Command;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import syam.ProjectManager.Project.Project;
 import syam.ProjectManager.Project.ProjectConfigManager;
 import syam.ProjectManager.Util.Actions;
 import syam.ProjectManager.Util.Util;
 
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
 		Project project = ProjectConfigManager.getSelectedProject(player);
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
 
 		// クリエイティブモードの設定は特殊権限が必要
 		if (conf.equals(Configables.CREATIVE) && !player.hasPermission("pm.admin.set.creative")){
 			Actions.message(sender, null, "&cあなたはクリエイティブモードの設定を変更する権限がありません！");
 			return true;
 		}
 
 		// Permission OK
 
 
 		// 設定項目によって処理を分ける
 		switch (conf){
 			case WARP: // ワープ地点設定
 				return setWarp(project);
 
 			case CREATIVE: // クリエイティブ設定
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
 
 	private boolean setWarp(Project project){
 		project.setWarpLocation(player.getLocation());
 
 		Actions.message(null, player, "&aプロジェクトID'"+project.getID()+"'のワープ地点を設定しました！");
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
 			creative = true;
 		}else if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("no")){
 			creative = false;
 		}else{
			Actions.message(sender, null, "&c値が不正です！true または false を指定してください！");
 			return true;
 		}
 
 		String result = "";
 		if (creative) result = "&a有効";
 		else result = "&c無効";
 
 		project.setCreative(creative);
		Actions.message(sender, null, "aプロジェクトID'"+project.getID()+"'のクリエイティブモードは"+result+"&aに設定されました！");
 
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
 		WARP("ワープ地点"),
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
 		return sender.hasPermission("pm.manager.set");
 	}
 }
