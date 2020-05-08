 package syam.Honeychest;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import syam.Honeychest.config.MessageManager;
 
 public class HoneychestCommand implements CommandExecutor {
 	public final static Logger log = Honeychest.log;
 	private static final String logPrefix = Honeychest.logPrefix;
 	private static final String msgPrefix = Honeychest.msgPrefix;
 
 	private Honeychest plugin;
 	public HoneychestCommand(Honeychest instance){
 		this.plugin = instance;
 	}
 
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
 		// 引数無しはハニーチェスト管理モード
 		if (args.length == 0){
 			// コンソールチェック
 			if (!(sender instanceof Player)){
 				Actions.message(sender, null, MessageManager.getString("Commands.notFromConsole"));
 				return true;
 			}
 
 			Player player = (Player) sender;
 			// 権限チェック
 			if (!player.hasPermission("honeychest.admin")){
 				Actions.message(null, player, MessageManager.getString("Commands.permissionDenied"));
 				return true;
 			}
 
 			if (HoneyData.isCreator(player)){
 				// 管理モード終了
 				HoneyData.setCreator(player, false);
 				Actions.message(null, player, MessageManager.getString("Commands.manageFinish"));
 			}else{
 				// 管理モード開始
 				HoneyData.setCreator(player, true);
 
 				String tool = Material.getMaterial(plugin.getHCConfig().getToolId()).name();
 
 				Actions.message(null, player, MessageManager.getString("Commands.manageStart", tool));
 			}
 			return true;
 		}
 
 		// /honeychest save - ハニーチェストデータ保存
 		if (args.length >= 1 && (args[0].equalsIgnoreCase("save") || args[0].equalsIgnoreCase("s"))){
 			// 権限チェック
 			if (!sender.hasPermission("honeychest.admin")){
 				Actions.message(sender, null, MessageManager.getString("Commands.permissionDenied"));
 				return true;
 			}
 			if(!HoneyData.saveData()){
 				Actions.message(sender, null, MessageManager.getString("Commands.dataSaveError"));
 			}else{
 				Actions.message(sender, null, MessageManager.getString("Commands.dataSaved"));
 			}
 			return true;
 		}
 
 		// /honeychest reload - ハニーチェストデータを読み込み
		if (args.length >= 1 && (args[0].equalsIgnoreCase("load") || args[0].equalsIgnoreCase("l"))){
 			// 権限チェック
 			if (!sender.hasPermission("honeychest.admin")){
 				Actions.message(sender, null, MessageManager.getString("Commands.permissionDenied"));
 				return true;
 			}
 			if(!HoneyData.saveData()){
 				Actions.message(sender, null, MessageManager.getString("Commands.dataLoadError"));
 			}else{
 				Actions.message(sender, null, MessageManager.getString("Commands.dataLoaded"));
 			}
 			return true;
 		}
 
 		// /honeychest help - ヘルプを表示
 		if (args.length >= 1 && (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("h"))){
 			Actions.sendHelp(sender);
 			return true;
 		}
 
 		return false;
 	}
 }
