 package me.asofold.bpl.archer.command.contest;
 
 import java.util.List;
 
 import me.asofold.bpl.archer.Archer;
 import me.asofold.bpl.archer.command.AbstractCommand;
 import me.asofold.bpl.archer.command.TabUtil;
 import me.asofold.bpl.archer.config.Permissions;
 import me.asofold.bpl.archer.core.Contest;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 
 public class ContestCreateCommand extends AbstractCommand<Archer> {
 
 	public ContestCreateCommand(Archer access) {
 		super(access, "create", Permissions.COMMAND_CONTEST_CREATE);
 	}
 	
 	@Override
 	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
 	{
 		if (args.length == 3){
			return TabUtil.tabCompleteRandomNewContests(access, sender, args.length == 3 ? args[2] : "");
 		}
 		else{
 			return noTabChoices;
 		}
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
 	{
 		if (args.length != 3) return false;
 		String newName = args[2].trim();
 		if (newName.isEmpty() || newName.equals("*")){
 			Archer.send(sender, "Invalid contest name: " + newName);
 			return true;
 		}
 		if (access.getContestManager().getContest(newName) != null) Archer.send(sender, "Contest already exists: " + args[2].trim().toLowerCase());
 		else{
 			Contest contest = new Contest(args[2], sender.getName());
 			if (sender instanceof Entity){
 				contest.world = ((Entity) sender).getWorld().getName();
 			}
 			access.getContestManager().addContest(contest);
 			Archer.send(sender, "Contest created: " + newName);
 		}
 		return true;
 	}
 
 }
