 package net.catharos.cquest.cmd.cmds;
 
 import net.catharos.cquest.cQuest;
 import net.catharos.cquest.cmd.AbstractCommand;
 import net.catharos.cquest.util.MessageUtil;
 import net.catharos.cquest.util.PermissionUtil;
 
 import org.bukkit.command.CommandSender;
 
 public class ReloadCommand extends AbstractCommand {
 
 	public ReloadCommand(cQuest plugin) {
 		super("Reload", plugin);
 
 		setAliases("quest reload");
 		setDescription("Reloads all configuration files");
 		setPermission(PermissionUtil.ADMIN);
 	}
 
 	@Override
 	public boolean execute(CommandSender sender, String label, String[] args) {
 		getPlugin().reloadConfig();
 		getPlugin().getQuestManager().reloadConfig();
 		
 		sender.sendMessage(MessageUtil.parseColors("&6All configurations reloaded!"));
 		
 		return true;
 	}
	
	@Override
	public boolean isConsoleCmd() {
		return true;
	}
 }
