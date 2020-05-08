 package syam.ProjectManager.Command;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 
 import syam.ProjectManager.Project.Project;
 import syam.ProjectManager.Util.Actions;
 
 public class SpawnCommand extends BaseCommand{
 	public SpawnCommand(){
 		bePlayer = true;
 		name = "spawn";
 		argLength = 0;
 		usage = "[project] <- tp to specific project spawn";
 	}
 
 	@Override
 	public boolean execute() {
 		Project project = null;
 		if (args.size() == 0){
 			List<Project> joined = plugin.getJoinedProject(player.getName());
 			if (joined.size() == 1){
 				project = joined.get(0); // Set project
 				Actions.message(null, player, "&a参加中のプロジェクトID'"+project.getID()+"'にテレポートします！");
 			}
 			else if(joined.size() == 0){
 				Actions.message(null, player, "&c参加中のプロジェクトがありません！ /project tp <プロジェクトID>");
 			}
 			else{
 				Actions.message(null, player, "&c参加中のプロジェクトが複数あります！ /project tp <プロジェクトID>");
 			}
 		}
 		else{
 			Project t = plugin.getProject(args.get(0));
 			if (t == null){
 				Actions.message(null, player, "&cプロジェクトID'"+args.get(0)+"'が見つかりません！");
 			}else{
 				project = t; // Set project
 				Actions.message(null, player, "&aプロジェクトID'"+project.getID()+"'にテレポートします！");
 			}
 		}
 
 		// null return
 		if (project == null)
 			return true;
 
 		Location loc = project.getSpawnLocation();
 		if (loc == null){
 			Actions.message(null, player, "&cこのプロジェクトはスポーン地点が未設定です！");
 			return true;
 		}
 
 		// Teleport
 		player.teleport(loc, TeleportCause.PLUGIN);
 
 		// Check project gamemode
		if (project.isJoined(player.getName()) && project.getCreative() && plugin.getConfigs().creativeWorlds.contains(player.getWorld().getName())){

 			player.setGameMode(GameMode.CREATIVE);
 			Actions.message(null, player, "&aこのプロジェクトはクリエイティブモードが有効になっています！");
 		}else{
 			player.setGameMode(GameMode.SURVIVAL);
 			Actions.message(null, player, "&aプロジェクトのスポーン地点にテレポートしました！");
 		}
 
 		return true;
 	}
 
 	@Override
 	public boolean permission() {
 		return sender.hasPermission("pm.user.spawn");
 	}
 }
