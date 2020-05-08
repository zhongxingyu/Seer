 package syam.ProjectManager.Command;
 
 import syam.ProjectManager.Project.Project;
 import syam.ProjectManager.Project.ProjectConfigManager;
 import syam.ProjectManager.Util.Actions;
 
 public class SelectCommand extends BaseCommand{
 	public SelectCommand(){
 		bePlayer = false;
 		name = "select";
 		argLength = 0;
 		usage = "[ID] <- select exist project";
 	}
 
 	@Override
 	public boolean execute() {
 		if (args.size() >= 1){
 			// project select (プロジェクト名) - 選択
 			Project project = plugin.getProject(args.get(0));
 			if (project != null){
 				ProjectConfigManager.setSelectedProject(sender.getName(), project);
 				Actions.message(sender, null, "&aプロジェクト'&6"+project.getID()+"&a'を選択しました！");
 			}else{
 				Actions.message(sender, null, "&cプロジェクト'&6"+args.get(0)+"&a'が見つかりません！");
 				return true;
 			}
 		}else{
 			// project select - 選択解除
 			if (ProjectConfigManager.getSelectedProject(sender.getName()) != null){
 				ProjectConfigManager.setSelectedProject(sender.getName(), null);
 			}
 			Actions.message(sender, null, "&aプロジェクトの選択を解除しました！");
 		}
 		return true;
 	}
 
 	@Override
 	public boolean permission() {
		return sender.hasPermission("pm.admin.select");
 	}
 }
