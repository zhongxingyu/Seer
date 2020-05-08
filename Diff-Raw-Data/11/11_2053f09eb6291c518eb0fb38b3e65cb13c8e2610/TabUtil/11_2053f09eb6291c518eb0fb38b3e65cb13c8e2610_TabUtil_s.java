 package me.asofold.bpl.archer.command;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import me.asofold.bpl.archer.Archer;
 import me.asofold.bpl.archer.core.Contest;
 import me.asofold.bpl.archer.core.ContestData;
 import me.asofold.bpl.archer.core.ContestManager;
 import me.asofold.bpl.archer.core.PlayerData;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class TabUtil {
 
 	/**
 	 * Convenience method getting all available contests for players or all for non-players.
 	 * @param sender
 	 * @param arg uses trim
 	 * @return A new list that may be modified.
 	 */
 	public static List<String> tabCompleteAvailableContests(final Archer access, final CommandSender sender, final String arg) {
 		boolean isPlayer = sender instanceof Player;
 		final Collection<Contest> available =  isPlayer ? access.getAvailableContests((Player) sender) : access.getAvailableContests(null);
 		return TabUtil.tabCompleteContests(access, available, arg);
 	}
 
 	/**
 	 * 
 	 * @param player
 	 * @param arg
 	 * @return A new list.
 	 */
 	public static List<String> tabCompleteActiveContests(final Archer access, final Player player, final String arg){
 		final PlayerData data = access.getPlayerData(player);
 		if (data == null || data.activeContests.isEmpty()) return new LinkedList<String>();
 		final List<Contest> contests = new LinkedList<Contest>();
 		for (final ContestData cd : data.activeContests.values()){
 			contests.add(cd.contest);
 		}
 		return TabUtil.tabCompleteContests(access, contests, arg);
 	}
 
 	/**
 	 * Tab completions for a given collection of Contest instances.
 	 * @param available
 	 * @param arg
 	 * @return
 	 */
 	public static List<String> tabCompleteContests(Archer access, Collection<Contest> available, String arg){
 		arg = arg == null ? "" : arg.trim().toLowerCase();
 		final List<String> choices = new ArrayList<String>(available.size());
 		for (final Contest ref : available){
 			// Might also check for the first letters to match like with '*** xyz ***'.
 			if (ref.name.toLowerCase().startsWith(arg)){
 				choices.add(ref.name);
 			}
 		}
 		if (!choices.isEmpty()){
 			Collections.sort(choices, String.CASE_INSENSITIVE_ORDER);
 		}
 		return choices;
 	}
 
 	/**
 	 * 
 	 * @param sender
 	 * @param arg
 	 * @param allowAll Adds '*' if set to true.
 	 * @return
 	 */
 	public static List<String> tabCompleteAllContests(Archer access, CommandSender sender, String arg, boolean allowAll)
 	{
 		arg = arg.trim().toLowerCase();
 		final Collection<Contest> contests = access.getContestManager().getAllContests();
 		final List<String> choices = new ArrayList<String>(contests.size());
 		for (final Contest contest : contests){
 			if (contest.name.toLowerCase().startsWith(arg)){
 				choices.add(contest.name);
 			}
 		}
 		Collections.sort(choices, String.CASE_INSENSITIVE_ORDER);
 		if (allowAll) choices.add("*");
 		return choices;
 	}
 
 	public static List<String> tabCompleteRandomNewContests(Archer access, CommandSender sender, String arg)
 	{
 		arg = arg.trim();
 		if (arg.isEmpty()) arg = "Contest";
 		final ContestManager cMan = access.getContestManager();
 		if (cMan.getContest(arg) == null){
 			return Arrays.asList(arg);
 		}
 		// Find a new name
 		final long time = System.currentTimeMillis();
 		for (int i = 1; i <= 12; i++){
 			int x = (int)(time % Math.pow(10, i));
			if (cMan.getContest(arg + "_" + x) != null){
				return Arrays.asList(arg);
 			}
 		}
 		return AbstractCommand.noTabChoices;
 	}
 
 }
