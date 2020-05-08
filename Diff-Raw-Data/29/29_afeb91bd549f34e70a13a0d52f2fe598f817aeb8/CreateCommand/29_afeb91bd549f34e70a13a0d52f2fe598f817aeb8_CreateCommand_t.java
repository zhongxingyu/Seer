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
 
		setAliases("quest create", "quests create");
 		setDescription("Creates a new quest");
 		setPermission(PermissionUtil.ADMIN);
		setArgumentRage(1, 99);
 	}
 
 	@Override
 	public boolean execute(CommandSender sender, String label, String[] args) {
 		QuestManager quest_mng = getPlugin().getQuestManager();
 		
		StringBuilder name_builder = new StringBuilder(args[0]);
		for(int i = 1; i < args.length; i++) name_builder.append(' ').append(args[i]);
 		
		QuestEntry entry = new QuestEntry(name_builder.toString());
		int index = quest_mng.addQuest(entry);
 		
		sender.sendMessage(MessageUtil.parseColors("&2Success! &7Quest created: &6" + entry.getName() + " &d(ID: "+ index + ")"));
 		
 		return true;
 	}
 
 }
