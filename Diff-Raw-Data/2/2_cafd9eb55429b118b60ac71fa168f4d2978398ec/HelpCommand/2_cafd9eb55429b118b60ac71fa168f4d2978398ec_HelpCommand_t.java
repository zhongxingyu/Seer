 package syam.ThemeCreative.Command;
 
 import syam.ThemeCreative.ThemeCreative;
 import syam.ThemeCreative.Util.Actions;
 
 public class HelpCommand extends BaseCommand{
 	public HelpCommand(){
 		bePlayer = false;
 		name = "help";
 		argLength = 0;
 		usage = "<- show command help";
 	}
 
 	@Override
 	public boolean execute() {
 		Actions.message(sender, null, "&c===================================");
		Actions.message(sender, null, "&bThemeCreative Plugin version &3%version &bby syamn");
 		Actions.message(sender, null, " &b<>&f = required, &b[]&f = optional");
 		// 全コマンドをループで表示
 		for (BaseCommand cmd : ThemeCreative.commands.toArray(new BaseCommand[0])){
 			cmd.sender = this.sender;
 			if (cmd.permission()){
 				Actions.message(sender, null, "&8-&7 /"+command+" &c" + cmd.name + " &7" + cmd.usage);
 			}
 		}
 		Actions.message(sender, null, "&c===================================");
 
 		return true;
 	}
 
 	@Override
 	public boolean permission() {
 		return true;
 	}
 }
