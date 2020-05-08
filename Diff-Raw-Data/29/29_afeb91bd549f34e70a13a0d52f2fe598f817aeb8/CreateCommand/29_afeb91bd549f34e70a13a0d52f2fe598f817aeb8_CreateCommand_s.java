 package net.catharos.cquest.cmd.cmds;
 
 import net.catharos.cquest.cQuest;
 import net.catharos.cquest.cmd.AbstractCommand;
 import net.catharos.cquest.quest.QuestEntry;
 import net.catharos.cquest.quest.QuestManager;
 import net.catharos.cquest.util.MessageUtil;
 import net.catharos.cquest.util.PermissionUtil;
 
 import org.bukkit.command.CommandSender;
 
 public class CreateCommand extends AbstractCommand {
 
 	public CreateCommand(cQuest plugin) {
 		super("Create", plugin);
 
		setAliases("quest create");
 		setDescription("Creates a new quest");
 		setPermission(PermissionUtil.ADMIN);
		setArgumentRage(2, 3);
 	}
 
 	@Override
 	public boolean execute(CommandSender sender, String label, String[] args) {
 		QuestManager quest_mng = getPlugin().getQuestManager();
 		
		QuestEntry entry = new QuestEntry(args[0]);
		entry.setDescription(args[1]);
 		
		if(args.length > 2) entry.setLocation(args[2]);
 		
		quest_mng.addQuest(entry);
		
		sender.sendMessage(MessageUtil.parseColors("&7Quest created: &6" + entry.getName()));
 		
 		return true;
 	}
 
 }
