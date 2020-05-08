 package syam.ProjectManager.Command;
 
 import java.io.File;
 
 import syam.ProjectManager.Project.Project;
 import syam.ProjectManager.Util.Actions;
 
 public class DeleteCommand extends BaseCommand{
 	public DeleteCommand(){
 		bePlayer = false;
 		name = "delete";
 		argLength = 1;
 		usage = "<ID> <- delete exist project";
 	}
 
 	@Override
 	public boolean execute() {
 		Project project = plugin.getProject(args.get(0));
 		if (project == null){
 			Actions.message(sender, null, "&cそのプロジェクト名は存在しません！");
 			return true;
 		}
 
 		// プロジェクトリストから削除
 		plugin.projects.remove(args.get(0));
 
 		// プロジェクトデータファイルを削除
 		String fileDir = plugin.getDataFolder() + System.getProperty("file.separator") + "projectData";
 		boolean deleted = false;
 		try{
 			File file = new File(fileDir + System.getProperty("file.separator") + project.getFileName());
 			if (file.exists()){
 				deleted = file.delete();
 			}
 		}catch (Exception ex){
 			deleted = false;
 			ex.printStackTrace();
 		}
 
 		if (!deleted)
 			Actions.message(sender, null, "&cプロジェクト'"+args.get(0)+"'のプロジェクトデータファイル削除中にエラーが発生しました！");
 		else
			project.message(msgPrefix+"&c参加中のプロジェクト'&6"+project.getTitle()+"&c'は削除されました！");
 			Actions.message(sender, null, "&aプロジェクト'"+args.get(0)+"'を削除しました！");
 		return true;
 	}
 
 	@Override
 	public boolean permission() {
 		return sender.hasPermission("pm.admin.delete");
 	}
 }
