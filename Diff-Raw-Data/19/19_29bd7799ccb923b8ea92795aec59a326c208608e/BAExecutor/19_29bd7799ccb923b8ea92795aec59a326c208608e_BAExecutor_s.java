 package mc.alk.arena.executors;
 
 import mc.alk.arena.BattleArena;
 import mc.alk.arena.Defaults;
 import mc.alk.arena.Permissions;
 import mc.alk.arena.competition.Competition;
 import mc.alk.arena.competition.events.Event;
 import mc.alk.arena.competition.match.Match;
 import mc.alk.arena.competition.util.TeamJoinHandler;
 import mc.alk.arena.controllers.ArenaAlterController;
 import mc.alk.arena.controllers.ArenaAlterController.ChangeType;
 import mc.alk.arena.controllers.ArenaClassController;
 import mc.alk.arena.controllers.BAEventController;
 import mc.alk.arena.controllers.CombatTagInterface;
 import mc.alk.arena.controllers.CompetitionController;
 import mc.alk.arena.controllers.DuelController;
 import mc.alk.arena.controllers.EventController;
 import mc.alk.arena.controllers.MoneyController;
 import mc.alk.arena.controllers.ParamAlterController;
 import mc.alk.arena.controllers.ParamController;
 import mc.alk.arena.controllers.PlayerController;
 import mc.alk.arena.controllers.RoomController;
 import mc.alk.arena.controllers.StatController;
 import mc.alk.arena.controllers.TeamController;
 import mc.alk.arena.controllers.TeleportController;
 import mc.alk.arena.controllers.containers.LobbyContainer;
 import mc.alk.arena.controllers.containers.RoomContainer;
 import mc.alk.arena.controllers.messaging.MessageHandler;
 import mc.alk.arena.controllers.plugins.EssentialsController;
 import mc.alk.arena.controllers.plugins.HeroesController;
 import mc.alk.arena.controllers.plugins.MobArenaInterface;
 import mc.alk.arena.events.arenas.ArenaCreateEvent;
 import mc.alk.arena.events.arenas.ArenaDeleteEvent;
 import mc.alk.arena.events.players.ArenaPlayerJoinEvent;
 import mc.alk.arena.events.players.ArenaPlayerLeaveEvent;
 import mc.alk.arena.listeners.PlayerHolder;
 import mc.alk.arena.listeners.competition.InArenaListener;
 import mc.alk.arena.objects.ArenaClass;
 import mc.alk.arena.objects.ArenaLocation;
 import mc.alk.arena.objects.ArenaPlayer;
 import mc.alk.arena.objects.ArenaSize;
 import mc.alk.arena.objects.CompetitionSize;
 import mc.alk.arena.objects.ContainerState;
 import mc.alk.arena.objects.Duel;
 import mc.alk.arena.objects.LocationType;
 import mc.alk.arena.objects.MatchParams;
 import mc.alk.arena.objects.MatchState;
 import mc.alk.arena.objects.MatchTransitions;
 import mc.alk.arena.objects.arenas.Arena;
 import mc.alk.arena.objects.arenas.ArenaControllerInterface;
 import mc.alk.arena.objects.arenas.ArenaType;
 import mc.alk.arena.objects.exceptions.InvalidOptionException;
 import mc.alk.arena.objects.messaging.AnnouncementOptions;
 import mc.alk.arena.objects.messaging.Channel;
 import mc.alk.arena.objects.options.DuelOptions;
 import mc.alk.arena.objects.options.DuelOptions.DuelOption;
 import mc.alk.arena.objects.options.EventOpenOptions;
 import mc.alk.arena.objects.options.JoinOptions;
 import mc.alk.arena.objects.options.TransitionOption;
 import mc.alk.arena.objects.options.TransitionOptions;
 import mc.alk.arena.objects.pairs.JoinResult;
 import mc.alk.arena.objects.queues.TeamJoinObject;
 import mc.alk.arena.objects.teams.ArenaTeam;
 import mc.alk.arena.objects.teams.FormingTeam;
 import mc.alk.arena.objects.teams.TeamFactory;
 import mc.alk.arena.util.Log;
 import mc.alk.arena.util.MessageUtil;
 import mc.alk.arena.util.MinMax;
 import mc.alk.arena.util.PermissionsUtil;
 import mc.alk.arena.util.ServerUtil;
 import mc.alk.arena.util.TeamUtil;
 import mc.alk.arena.util.TimeUtil;
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  *
  * @author alkarin
  *
  */
 public class BAExecutor extends CustomCommandExecutor {
     Map<String, Location> visitors = new HashMap<String, Location>();
     Set<String> disabled = new HashSet<String>();
 
     final TeamController teamc;
     final EventController ec;
     final DuelController dc;
 
     public BAExecutor() {
         super();
         this.ec = BattleArena.getEventController();
         this.teamc = BattleArena.getTeamController();
         this.dc = BattleArena.getDuelController();
     }
 
     @MCCommand(cmds = { "enable" }, admin = true, perm = "arena.enable", usage = "enable")
     public boolean arenaEnable(CommandSender sender, MatchParams mp,
                                String[] args) {
         if (args.length > 1 && args[1].equalsIgnoreCase("all")) {
             Set<String> set = new HashSet<String>(); // / Since some commands
             // have aliases.. we
             // just want the
             // original name
             for (MatchParams param : ParamController.getAllParams()) {
                 disabled.remove(param.getName());
                 set.add(param.getName());
             }
             for (String s : set) {
                 sendSystemMessage(sender, "type_enabled", s);
             }
 
             return true;
         }
         disabled.remove(mp.getName());
         return sendSystemMessage(sender, "type_enabled", mp.getName());
     }
 
     @MCCommand(cmds = { "disable" }, admin = true, perm = "arena.enable", usage = "disable")
     public boolean arenaDisable(CommandSender sender, MatchParams mp,
                                 String[] args) {
         if (args.length > 1 && args[1].equalsIgnoreCase("all")) {
             Set<String> set = new HashSet<String>(); // / Since some commands
             // have aliases.. we
             // just want the
             // original name
             for (MatchParams param : ParamController.getAllParams()) {
                 disabled.add(param.getName());
                 set.add(param.getName());
             }
             for (String s : set) {
                 sendSystemMessage(sender, "type_disabled", s);
             }
             return true;
         }
         disabled.add(mp.getName());
         return sendSystemMessage(sender, "type_disabled", mp.getName());
     }
 
     public static boolean sendSystemMessage(CommandSender sender, String node, Object... args) {
         return sendMessage(sender, MessageHandler.getSystemMessage(node, args));
     }
 
     public static boolean sendSystemMessage(ArenaTeam team, String node, Object... args) {
         team.sendMessage(MessageHandler.getSystemMessage(node, args));
         return true;
     }
 
     public static boolean sendSystemMessage(ArenaPlayer sender, String node, Object... args) {
         return sendMessage(sender, MessageHandler.getSystemMessage(node, args));
     }
 
     @MCCommand(cmds = { "enabled" }, admin = true)
     public boolean arenaCheckArenaTypes(CommandSender sender) {
         String types = ArenaType.getValidList();
         sendMessage(sender, "&e valid types are = &6" + types);
         return sendMessage(
                 sender,
                 "&5Enabled types = &6 "
                         + ParamController.getAvaibleTypes(disabled));
     }
 
     @MCCommand(cmds = { "join", "j" }, usage = "add [options]", helpOrder = 1)
     public boolean join(ArenaPlayer player, MatchParams mp, String args[]) {
         return join(player, mp, args, false);
     }
 
     public boolean join(ArenaPlayer player, MatchParams mp, String args[], boolean adminJoin) {
         /// Make sure we have Match Options
         final MatchTransitions ops = mp.getTransitionOptions();
         if (ops == null) {
             return sendMessage(player,"&cThis match type has no valid options, contact an admin to fix");}
 
         /// Check if this match type is disabled
         if (isDisabled(player, mp) && !player.hasPermission(Permissions.ADMIN_NODE)) {
             return true;}
 
         /// Check Perms
         if (!adminJoin && !PermissionsUtil.hasMatchPerm(player, mp, "join")) {
             return sendSystemMessage(player, "no_join_perms", mp.getCommand());}
         /// Can the player add this match/event at this moment?
         if (!canJoin(player)) {
             return true;}
 
         /// Call the joining event
         ArenaPlayerJoinEvent event = new ArenaPlayerJoinEvent(player);
         event.callEvent();
         if (event.isCancelled()) {
             //noinspection SimplifiableIfStatement
             if (event.getMessage() != null && !event.getMessage().isEmpty()) {
                 return sendMessage(player, event.getMessage());}
             return true;
         }
 
         /// Get or Make a team for the Player
         ArenaTeam t = teamc.getSelfFormedTeam(player);
         if (t == null) {
             t = TeamController.createTeam(mp, player);}
 
         if (!canJoin(t, true)) {
             sendSystemMessage(player, "teammate_cant_join", mp.getName());
             return sendMessage(player, "&6/team leave: &cto leave the team");
         }
 
         JoinOptions jp;
         try {
             jp = JoinOptions.parseOptions(mp, t, player,Arrays.copyOfRange(args, 1, args.length));
         } catch (InvalidOptionException e) {
             return sendMessage(player, e.getMessage());
         } catch (Exception e) {
             Log.printStackTrace(e);
             return sendMessage(player, e.getMessage());
         }
         /// Check to make sure at least one arena can be joined at some time
         Arena arena = ac.getArenaByMatchParams(mp, jp);
         if (arena == null) {
             if (!jp.hasWantedTeamSize()) {
                 arena = ac.getArenaByNearbyMatchParams(mp, jp);
                 if (arena != null) {
                     mp.setMinTeamSize(arena.getParams().getMinTeamSize());
                     mp.setMaxTeamSize(arena.getParams().getMaxTeamSize());
                 }
             }
             if (arena == null) {
                 Map<Arena, List<String>> reasons = ac.getNotMachingArenaReasons(mp, jp);
                 if (!reasons.isEmpty()) {
                     for (Arena a : reasons.keySet()) {
                         List<String> rs = reasons.get(a);
                         if (!rs.isEmpty())
                             return sendMessage(player, "&c" + rs.get(0));
                     }
                 }
                 return sendSystemMessage(player, "valid_arena_not_built", mp.getName());
             }
         }
 
         if (!arena.isJoinable(mp)) {
             return MessageUtil.sendMessage(player,
                     "&c" + arena.getName() + " can't be joined at this time.\n"
                             + arena.getNotJoinableReasons(mp));
         }
 
         // BTInterface bti = BTInterface.getInterface(mp);
         // if (bti.isValid()){
         // bti.updateRanking(t);
         // }
 
         /// Check for lobbies
         if (ops.hasAnyOption(TransitionOption.TELEPORTLOBBY)
                 && !RoomController.hasLobby(mp.getType())) {
             return sendMessage(player,
                     "&cThis match has no lobby and needs one! contact an admin to fix");
         }
 
         /// Check if the team is ready
         if (!ops.teamReady(t, null)) {
             t.sendMessage(ops.getRequiredString(MessageHandler
                     .getSystemMessage("need_the_following") + "\n"));
             return true;
         }
 
         /// Check entrance fee
         if (!checkAndRemoveFee(mp, t)) {
             return true;
         }
 
         TeamJoinObject tqo = new TeamJoinObject(t, mp, jp);
         JoinResult jr;
         try{
             /// Add them to the queue
             jr = ac.wantsToJoin(tqo);
         } catch (IllegalStateException e){
             return sendMessage(player,"&c"+e.getMessage());
         }
         AnnouncementOptions ao = mp.getAnnouncementOptions();
 
         /// switch over to the joined params
         mp = jr.params;
         /// Annouce to the server if they have the option set
 
         Channel channel = ao != null ? ao.getChannel(true, MatchState.ONENTERQUEUE)
                 : AnnouncementOptions.getDefaultChannel(true,
                 MatchState.ONENTERQUEUE);
         String neededPlayers = jr.maxPlayers == CompetitionSize.MAX ? "inf" : jr.maxPlayers + "";
         List<Object> vars = new ArrayList<Object>();
         vars.add(mp);
         vars.add(t);
         channel.broadcast(MessageHandler.getSystemMessage(
                 vars, "server_joined_the_queue", mp.getPrefix(),
                 player.getDisplayName(), jr.playersInQueue, neededPlayers));
         String sysmsg;
         switch (jr.status) {
             case ADDED_TO_EXISTING_MATCH:
                 if (t.size() == 1) {
                     t.sendMessage(MessageHandler.getSystemMessage(
                             "you_joined_event", mp.getName()));
                 } else {
                     t.sendMessage(MessageHandler
                             .getSystemMessage("you_added_to_team"));
                 }
                 break;
             case ADDED_TO_QUEUE:
                 sysmsg = MessageHandler.getSystemMessage("joined_the_queue",
                         mp.getName(), jr.pos, neededPlayers);
 
                 StringBuilder msg = new StringBuilder(sysmsg != null ? sysmsg
                         : "&eYou joined the &6"+mp.getName()+"&e queue");
                 if (jr.maxPlayers != CompetitionSize.MAX) {
                     String posmsg = MessageHandler.getSystemMessage(
                             "position_in_queue", jr.pos, neededPlayers);
                     msg.append(posmsg != null ? posmsg : "");
                 }
                 if (jr.time != null) {
                     Long time = jr.time - System.currentTimeMillis();
                     msg.append(constructMessage(jr.params, time, jr.playersInQueue, jr.pos));
                 }
 
                 t.sendMessage(msg.toString());
                 break;
             default:
                 break;
         }
 
         return true;
     }
 
     public static String constructMessage(MatchParams mp, long millisRemaining, int playersInQ, Integer position) {
         StringBuilder msg = new StringBuilder();
         if (millisRemaining <= 0){
             String max = mp.getMaxPlayers() == ArenaSize.MAX ? "\u221E" : mp.getMaxPlayers()+"";
             msg.append("\n").append(MessageHandler.getSystemMessage("match_starts_immediately",
                     mp.getMinPlayers() - playersInQ, playersInQ, max));
         } else {
             if (mp.getMaxPlayers()==CompetitionSize.MAX){
                 if (playersInQ < mp.getMinPlayers()) {
                     msg.append("\n").append(MessageHandler.getSystemMessage(
                             "match_starts_players_or_time2",
                             TimeUtil.convertMillisToString(millisRemaining),
                             mp.getMinPlayers() - playersInQ));
                 } else if (playersInQ < mp.getMaxPlayers()) {
                     msg.append("\n").append(MessageHandler.getSystemMessage(
                             "match_starts_when_time",
                             TimeUtil.convertMillisToString(millisRemaining)));
                 } else {
                     msg.append("\n").append(MessageHandler.getSystemMessage("you_start_when_free"));
                 }
             }
             else if(mp.getMinPlayers().equals(mp.getMaxPlayers())){
                 if (playersInQ < mp.getMinPlayers()) {
                     msg.append("\n").append(MessageHandler.getSystemMessage(
                             "match_starts_immediately",
                             mp.getMaxPlayers() - playersInQ,
                             playersInQ,
                             mp.getMinPlayers()));
                 } else {
                     msg.append("\n").append(MessageHandler.getSystemMessage("you_start_when_free"));
                 }
             } else {
                 if (playersInQ < mp.getMinPlayers()) {
                     msg.append("\n").append(MessageHandler.getSystemMessage(
                             "match_starts_players_or_time",
                             mp.getMaxPlayers() - playersInQ,
                             TimeUtil.convertMillisToString(millisRemaining),
                             mp.getMinPlayers()));
                 } else if (playersInQ < mp.getMaxPlayers()) {
                     msg.append("\n").append(MessageHandler.getSystemMessage(
                             "match_starts_players_or_time3",
                             mp.getMaxPlayers() - playersInQ,
                             TimeUtil.convertMillisToString(millisRemaining)));
                 } else {
                     if (position == null){
                         msg.append("\n").append(MessageHandler.getSystemMessage("you_start_when_free"));
                     } else {
                         msg.append("\n").append(MessageHandler.getSystemMessage("you_start_when_free_pos", position));
                     }
                 }
             }
 
         }
 
         return msg.toString();
     }
 
     protected boolean isDisabled(ArenaPlayer player, MatchParams mp) {
         if (disabled.contains(mp.getName())) {
             sendSystemMessage(player, "match_disabled", mp.getName());
             final String enabled = ParamController.getAvaibleTypes(disabled);
             if (enabled.isEmpty()) {
                 return sendSystemMessage(player, "all_disabled");
             } else {
                 return sendSystemMessage(player, "currently_enabled", enabled);
             }
         }
         return false;
     }
 
     @MCCommand(cmds = { "leave", "l" }, usage = "leave", helpOrder = 2)
     public boolean leave(ArenaPlayer p, MatchParams mp) {
         return leave(p, mp, false);
     }
 
     @MCCommand(cmds = { "switch"}, perm = "arena.switch")
     public boolean switchTeam(ArenaPlayer p, MatchParams mp, String teamStr) {
         Integer index = TeamUtil.getFromHumanTeamIndex(teamStr);
         if (index == null){
             return MessageUtil.sendMessage(p, "&cBad team index");}
 
         ArenaLocation loc = p.getCurLocation();
         PlayerHolder ph = loc.getPlayerHolder();
         Competition c = p.getCompetition();
         if (c == null && (ph == null || loc.getType() == LocationType.HOME)){
             if (ac.isInQue(p)) {
                 JoinOptions jo = p.getMetaData().getJoinOptions();
                 if (jo != null) {
                     jo.setOption(JoinOptions.JoinOption.TEAM, index);
                     return MessageUtil.sendMessage(p, "&eSwitched to team &6" + index);
                 }
             }
         } else if (c == null){
             if (ph instanceof RoomContainer){
                 /// they are not in a match, but are in the waitroom beforehand
 
             } else {
                 /// They aren't in anywhere
             }
         } else { /// they are in a competition
             if (c instanceof Match) {
                 TeamJoinHandler tjh = ((Match) c).getTeamJoinHandler();
                 tjh.switchTeams(p, index);
             } else {
                 /// Not a match (like a tournament), they can't switch
             }
         }
         return true;
     }
 
 
     public boolean leave(ArenaPlayer p, MatchParams mp, boolean adminLeave) {
         if (!adminLeave && !p.hasPermission("arena.leave") && !PermissionsUtil.hasMatchPerm(p,mp,"leave"))
             return true;
         if (!canLeave(p)) {
             return true;
         }
         ArenaPlayerLeaveEvent event = new ArenaPlayerLeaveEvent(p, p.getTeam(),
                 ArenaPlayerLeaveEvent.QuitReason.QUITCOMMAND);
         event.callEvent();
         if (event.getMessages() != null && !event.getMessages().isEmpty()) {
             MessageUtil.sendMessage(event.getPlayer(), event.getMessages());
         } else {
             sendSystemMessage(p, "you_not_in_queue");
         }
         return true;
     }
 
     // @MCCommand(cmds={"ready","r"}, inGame=true)
     // public boolean ready(ArenaPlayer player) {
     // boolean wasReady = player.isReady();
     // if (wasReady){
     // return sendMessage(player,"&cYou are already ready");
     // }
     // player.setReady(true);
     // sendMessage(player,"&2You are now ready");
     // new PlayerReadyEvent(player,player.isReady()).callEvent();
     // return true;
     // }
 
     @MCCommand(cmds = { "cancel" }, admin = true, usage = "cancel <arenaname or player>")
     public boolean arenaCancel(CommandSender sender, MatchParams params,
                                String[] args) {
         if (args.length > 1 && args[1].equalsIgnoreCase("all")) {
             return cancelAll(sender);}
         List<Match> matches = ac.getRunningMatches(params);
         if (!matches.isEmpty()) {
             for (Match m : matches) {
                 m.cancelMatch();
                 if (m.getState() == MatchState.ONCANCEL) {
                     Arena arena = m.getArena();
                     ac.removeArena(arena);
                     ac.addArena(arena);
                 }
             }
             return sendMessage(sender, "&2You have canceled the matches for &6"
                     + params.getType());
         }
         if (args.length < 2)
             return sendMessage(sender, "cancel <arenaname or player>");
         Player player = ServerUtil.findPlayer(args[1]);
         if (player != null) {
             ArenaPlayer ap = PlayerController.toArenaPlayer(player);
             if (ac.cancelMatch(ap)) {
                 return sendMessage(
                         sender,
                         "&2You have canceled the match for &6"
                                 + player.getName());
             } else {
                 return sendMessage(sender, "&cMatch couldnt be found for &6"
                         + player.getName());
             }
         }
         String arenaName = args[1];
         Arena arena = ac.getArena(arenaName);
         if (arena == null) {
             return sendMessage(sender, "&cArena " + arenaName + " not found");
         }
         if (ac.cancelMatch(arena)) {
             return sendMessage(sender,
                     "&2You have canceled the match in arena &6" + arenaName);
         } else {
             return sendMessage(sender, "&cError cancelling arena match");
         }
     }
 
     private boolean cancelAll(CommandSender sender) {
         Collection<ArenaTeam> teams = ac.purgeQueue();
         for (ArenaTeam t : teams) {
             t.sendMessage("&cYou have been removed from the queue");
         }
         ac.cancelAllArenas();
         ec.cancelAll();
         RoomController.cancelAll();
         return sendMessage(sender,
                 "&2You have cancelled all matches/events and cleared the queue");
     }
 
     @MCCommand(cmds = { "status" }, admin = true, min = 2, usage = "status <arena or player>")
     public boolean arenaStatus(CommandSender sender, String[] args) {
         Match am;
         String pormatch = args[1];
         Arena a = ac.getArena(pormatch);
         Player player;
         if (a == null) {
             player = ServerUtil.findPlayer(pormatch);
             if (player == null)
                 return sendMessage(sender, "&eCouldnt find arena or player="
                         + pormatch);
             ArenaPlayer ap = PlayerController.toArenaPlayer(player);
             am = ac.getMatch(ap);
             if (am == null) {
                 return sendMessage(sender, "&ePlayer " + pormatch
                         + " is not in a match");
             }
         } else {
             am = ac.getArenaMatch(a);
             if (am == null) {
                 return sendMessage(sender, "&earena " + pormatch
                         + " is not being used in a match");
             }
         }
         return sendMessage(sender, am.getMatchInfo());
     }
 
     @MCCommand(cmds = { "winner" }, admin = true, min = 2, usage = "winner <player>")
     public boolean arenaSetVictor(CommandSender sender, ArenaPlayer ap) {
         Match am = ac.getMatch(ap);
         if (am == null) {
             return sendMessage(sender, "&ePlayer " + ap.getName()
                     + " is not in a match");
         }
         am.setVictor(ap);
         return sendMessage(sender, "&6" + ap.getName()
                 + " has now won the match!");
     }
 
     @MCCommand(cmds = { "resetElo" }, op = true, usage = "resetElo")
     public boolean resetElo(CommandSender sender, MatchParams mp) {
         if (!StatController.hasInterface(mp)) {
             return sendMessage(sender,
                     "&eThere is no tracking for " + mp.getName());
         }
         StatController sc = new StatController(mp);
         sc.resetStats();
         return sendMessage(sender, mp.getPrefix() + " &2Elo's and stats for &6"
                 + mp.getName() + "&2 now reset");
     }
 
     @MCCommand(cmds = { "setRating" }, admin = true, usage = "setRating <player> <rating>")
     public boolean setElo(CommandSender sender, MatchParams mp,
                           OfflinePlayer player, int rating) {
         if (!StatController.hasInterface(mp)) {
             return sendMessage(sender,
                     "&eThere is no tracking for " + mp.getName());
         }
         StatController sc = new StatController(mp);
         if (sc.setRating(player, rating))
             return sendMessage(sender, "&6" + player.getName()
                     + "&e now has &6" + rating + "&e rating");
         else
             return sendMessage(sender, "&6Error setting rating");
     }
 
     @MCCommand(cmds = { "rank" }, helpOrder = 3)
     public boolean rank(Player sender, MatchParams mp) {
         if (!StatController.hasInterface(mp)) {
             return sendMessage(sender,
                     "&cThere is no tracking for &6" + mp.getName());
         }
         StatController sc = new StatController(mp);
         String rankMsg = sc.getRankMessage(sender);
         return MessageUtil.sendMessage(sender, rankMsg);
     }
 
     @MCCommand(cmds = { "rank" }, helpOrder = 4)
     public boolean rankOther(CommandSender sender, MatchParams mp,
                              OfflinePlayer player) {
         if (!StatController.hasInterface(mp)) {
             return sendMessage(sender,
                     "&cThere is no tracking for " + mp.getName());
         }
         StatController sc = new StatController(mp);
         String rankMsg = sc.getRankMessage(player);
         return MessageUtil.sendMessage(sender, rankMsg);
     }
 
     @MCCommand(cmds = { "top" }, helpOrder = 5)
     public boolean top(CommandSender sender, MatchParams mp, String[] args) {
         final int length = args.length;
         int teamSize = 1;
         int x = 5;
         if (length > 1)
             try {
                 x = Integer.valueOf(args[1]);
             } catch (Exception e) {
                 return sendMessage(sender, "&e top length " + args[1]
                         + " is not a number");
             }
         if (length > 2)
             try {
                 teamSize = Integer.valueOf(args[length - 1]);
             } catch (Exception e) {
                 return sendMessage(sender, "&e team size " + args[length - 1]
                         + " is not a number");
             }
         mp.setTeamSize(teamSize);
         return getTop(sender, x, mp);
     }
 
     public boolean getTop(CommandSender sender, int x, MatchParams mp) {
         if (x < 1 || x > 100) {
             x = 5;
         }
         if (!StatController.hasInterface(mp)) {
             return sendMessage(sender,
                     "&eThere is no tracking for " + mp.getName());
         }
 
         final String teamSizeStr = (mp.getMinTeamSize() > 1 ? "teamSize=&6"
                 + mp.getMinTeamSize() : "");
         final String arenaString = mp.getType().toPrettyString(
                 mp.getMinTeamSize(), mp.getMaxTeamSize());
 
         final String headerMsg = "&4Top {x} Gladiators in &6" + arenaString
                 + "&e " + teamSizeStr;
         final String bodyMsg = "&e#{rank}&4 {name} - {wins}:{losses}&6[{ranking}]";
 
         StatController sc = new StatController(mp);
         sc.printTopX(sender, x, mp.getMinTeamSize(), headerMsg, bodyMsg);
         return true;
     }
 
 //	@MCCommand(cmds = { "check" }, usage = "check")
 //	public boolean arenaCheck(ArenaPlayer p) {
 //		if (ac.isInQue(p)) {
 //			JoinResult qpp = ac.getCurrentQuePos(p);
 //			if (qpp != null) {
 //				return sendMessage(p, "&e" + qpp.params.getName()
 //						+ "&e Queue Position: " + " &6" + (qpp.pos + 1)
 //						+ "&e. &6" + qpp.playersInQueue + " &eplayers in queue");
 //			}
 //		}
 //		return sendMessage(p, "&eYou are currently not in any arena queues.");
 //	}
 
     @MCCommand(cmds = { "auto" }, admin = true, perm = "arena.auto")
     public boolean arenaAuto(CommandSender sender, MatchParams params,String args[]) {
         try {
             EventOpenOptions eoo = EventOpenOptions.parseOptions(args, null,params);
 
             Arena arena = eoo.getArena(params, null);
             Arena a = BattleArena.getBAController().reserveArena(arena);
             if (a == null){
             } else {
                 return sendMessage(sender,"[BattleArena] auto args="+Arrays.toString(args) +
                         " can't be started. Arena "+arena+" is not there or in use");
             }
 
             ac.createAndAutoMatch(arena, eoo);
             final int max = arena.getParams().getMaxPlayers();
             final String maxPlayers = max == ArenaSize.MAX ? "&6any&2 number of players"
                     : max + "&2 players";
             sendMessage(sender,
                     "&2You have " + args[0] + "ed a &6" + params.getName()
                             + "&2 inside &6" + arena.getName()
                             + " &2TeamSize=&6"
                             + arena.getParams().getTeamSizeRange()
                             + "&2 #Teams=&6"
                             + arena.getParams().getNTeamRange()
                             + "&2 supporting " + maxPlayers + "&2 at &5"
                             + arena.getName());
         } catch (InvalidOptionException e) {
             sendMessage(sender, e.getMessage());
         } catch (Exception e) {
             sendMessage(sender, e.getMessage());
             Log.printStackTrace(e);
         }
         return true;
     }
 
     @MCCommand(cmds = { "open" }, admin = true, exact = 2, perm = "arena.open")
     public boolean arenaOpen(CommandSender sender, MatchParams mp,
                              String arenaName) {
         if (arenaName.equalsIgnoreCase("all")) {
             ac.openAll(mp);
             return sendMessage(sender, "&6Arenas for " + mp.getName()
                     + ChatColor.YELLOW + " are now &2open");
         } else if (arenaName.equalsIgnoreCase("lobby")) {
             LobbyContainer lc = RoomController.getLobby(mp.getType());
             if (lc == null) {
                 return sendMessage(sender, "&cYou need to set a lobby for "
                         + mp.getName());
             }
             lc.setContainerState(ContainerState.OPEN);
             return sendMessage(sender, "&6 Lobby for " + mp.getName()
                     + ChatColor.YELLOW + " is now &2open");
         } else {
             Arena arena = ac.getArena(arenaName);
             if (arena == null) {
                 return sendMessage(sender, "&cArena " + arenaName
                         + " could not be found");
             }
 
             arena.setAllContainerState(ContainerState.OPEN);
             return sendMessage(sender, "&6" + arena.getName()
                     + ChatColor.YELLOW + " is now &2open");
         }
     }
 
     @MCCommand(cmds = { "open" }, admin = true, perm = "arena.open")
     public boolean arenaOpenContainer(CommandSender sender, Arena arena,
                                       ChangeType type) {
         try {
             if (type == ChangeType.LOBBY) {
                 LobbyContainer lc = RoomController.getLobby(arena
                         .getArenaType());
                 if (lc == null) {
                     return sendMessage(sender, "&cYou need to set a lobby for "
                             + arena.getArenaType().getName());
                 }
                 lc.setContainerState(ContainerState.OPEN);
             } else {
                 arena.setContainerState(type, ContainerState.OPEN);
             }
         } catch (IllegalStateException e) {
             return sendMessage(sender, "&c" + e.getMessage());
         }
         return sendMessage(sender, "&6" + arena.getName() + ChatColor.YELLOW
                 + " is now &2open");
     }
 
     @MCCommand(cmds = { "close" }, admin = true, exact = 2, perm = "arena.close")
     public boolean arenaClose(CommandSender sender, MatchParams mp,
                               String arenaName) {
         if (arenaName.equals("all")) {
             for (Arena arena : ac.getArenas(mp)) {
                 arena.setAllContainerState(ContainerState.CLOSED);
             }
             return sendMessage(sender, "&6Arenas for " + mp.getName()
                     + ChatColor.YELLOW + " are now &4closed");
         } else if (arenaName.equalsIgnoreCase("lobby")) {
             LobbyContainer lc = RoomController.getLobby(mp.getType());
             if (lc == null) {
                 return sendMessage(sender, "&cYou need to set a lobby for "
                         + mp.getName());
             }
             lc.setContainerState(ContainerState.CLOSED);
             return sendMessage(sender, "&6 Lobby for " + mp.getName()
                     + ChatColor.YELLOW + " is now &4closed");
         } else {
             Arena arena = ac.getArena(arenaName);
             if (arena == null) {
                 return sendMessage(sender, "&cArena " + arenaName
                         + " could not be found");
             }
             arena.setAllContainerState(ContainerState.CLOSED);
             return sendMessage(sender, "&6" + arena.getName()
                     + ChatColor.YELLOW + " is now &4closed");
         }
     }
 
     @MCCommand(cmds = { "close" }, admin = true, perm = "arena.close")
     public boolean arenaCloseContainer(CommandSender sender, Arena arena,
                                        ChangeType closeLocation) {
         try {
             arena.setContainerState(closeLocation, ContainerState.CLOSED);
         } catch (IllegalStateException e) {
             return sendMessage(sender, "&c" + e.getMessage());
         }
         return sendMessage(sender, "&6" + arena.getName() + ChatColor.YELLOW
                 + " " + closeLocation + " is now &4closed");
     }
 
     @MCCommand(cmds = { "delete" }, admin = true, perm = "arena.delete")
     public boolean arenaDelete(CommandSender sender, Arena arena) {
         new ArenaDeleteEvent(arena).callEvent();
         ac.deleteArena(arena);
         BattleArena.saveArenas(arena.getArenaType().getPlugin());
         return sendMessage(sender, ChatColor.GREEN
                 + "You have deleted the arena &6" + arena.getName());
     }
 
     @MCCommand(cmds = { "save" }, admin = true, perm = "arena.save")
     public boolean arenaSave(CommandSender sender) {
         BattleArena.saveArenas(true);
         return sendMessage(sender, "&eArenas saved");
     }
 
     @MCCommand(cmds = { "reload" }, admin = true, perm = "arena.reload")
     public boolean arenaReload(CommandSender sender, MatchParams mp) {
         Plugin plugin = mp.getType().getPlugin();
         BAEventController baec = BattleArena.getBAEventController();
         if (ac.hasRunningMatches() || !ac.isQueueEmpty() || baec.hasOpenEvent()) {
             sendMessage(
                     sender,
                     "&cYou can't reload the config while matches are running or people are waiting in the queue");
             return sendMessage(sender,
                     "&cYou can use &6/arena cancel all&c to cancel all matches and clear queues");
         }
 
         ac.stop();
         BattleArena.getSelf().reloadConfig();
 
         // / Get rid of any current players
         PlayerController.clearArenaPlayers();
         CompetitionController.reloadCompetition(plugin, mp);
         ac.resume();
         return sendMessage(sender, "&6" + plugin.getName()
                 + "&e configuration reloaded");
     }
 
     @MCCommand(cmds = { "info" }, exact = 1, usage = "info")
     public boolean arenaInfo(CommandSender sender, MatchParams mp) {
         String info = TransitionOptions.getInfo(mp, mp.getName());
         return sendMessage(sender, info);
     }
 
     @MCCommand(cmds = { "info" }, admin = true, usage = "info <arenaname>", order = 1, helpOrder = 6)
     public boolean info(CommandSender sender, Arena arena) {
         sendMessage(sender, arena.toDetailedString());
         Match match = ac.getMatch(arena);
         if (match != null) {
             List<String> strs = new ArrayList<String>();
             for (ArenaTeam t : match.getTeams()) {
                 strs.add("&5 -&e" + t.getDisplayName());
             }
             sendMessage(sender, "Teams: " + StringUtils.join(strs, ", "));
         }
         // final BAEventController controller =
         // BattleArena.getBAEventController();
         // Event event = controller.getEvent(arena);
         return true;
     }
 
     public boolean watch(CommandSender sender, String args[]) {
         if (args.length != 1) {
             sendMessage(sender, ChatColor.YELLOW + "Usage: /watch <arena name>");
             sendMessage(sender, ChatColor.YELLOW
                     + "Usage: /watch leave : to exit");
             return true;
         }
         if (!(sender instanceof Player))
             return true;
         Player p = (Player) sender;
 
         String value = args[0];
         String pname = p.getName();
         if (value.equalsIgnoreCase("leave")) {
             if (visitors.containsKey(pname)) {
                 Location loc = visitors.get(pname);
                 TeleportController.teleport(p, loc);
                 visitors.remove(pname);
                 sendMessage(sender, ChatColor.YELLOW
                         + "You have stopped watching the Arena");
                 return true;
             } else {
                 sendMessage(sender, ChatColor.YELLOW
                         + "You aren't waching an Arena");
                 return true;
             }
         } else {
             Arena arena = ac.getArena(value.toLowerCase());
             if (arena == null) {
                 sendMessage(sender, ChatColor.YELLOW
                         + "That arena doesnt exist!");
                 return true;
             }
             // if (arena.getVisitorLoc() == null){
             // sendMessage(sender,ChatColor.YELLOW +
             // "That arena doesnt allow visitors!");
             // return true;
             // }
             // if (visitors.containsKey(pname)){
             // /// Already have their old location.. dont store it
             // } else {
             // visitors.put(pname, p.getLocation());
             // }
             // TeleportController.teleport(p, arena.getVisitorLoc());
             // sendMessage(sender,ChatColor.YELLOW + "You are now watching " +
             // arena.getName() +" /watch leave : to leave");
         }
         return true;
     }
 
     @MCCommand(cmds = { "create" }, admin = true, perm = "arena.create", usage = "create <arena name> [team size] [# teams]")
     public boolean arenaCreate(Player sender, MatchParams mp, String name, String[] args) {
         if (ac.getArena(name) != null) {
             return sendMessage(sender, "&cThere is already an arena named &6" + name);
         }
         if (ParamController.getMatchParams(name) != null) {
             return sendMessage(sender, "&cYou can't choose an arena type as an arena name");
         }
         try {
             int i = Integer.valueOf(name);
             return sendMessage(sender, "&cYou can't choose a number as the arena name! Arena name was &e"+i);
         } catch (Exception e){
             /* good, it's not an integer*/
         }
         if (ParamController.getMatchParams(name) != null) {
             return sendMessage(sender, "&cYou can't choose an arena type as an arena name");
         }
 
         MatchParams ap = new MatchParams(mp.getType());
         try {
             if (args.length > 2)
                 ap.setTeamSizes(MinMax.valueOf(args[2]));
             if (args.length > 3)
                 ap.setNTeams(MinMax.valueOf(args[3]));
         } catch (Exception e) {
             return sendMessage(sender,"That size not recognized.  Examples: 1 or 2 or 1-5 or 2+");
         }
         ParamController.addArenaParams(name, ap);
 
         Arena arena = ArenaType.createArena(name, ap, false);
         if (arena == null) {
             return sendMessage(sender, "&cCouldn't create the arena " + name
                     + " of type " + ap.getType());
         }
 
         arena.setSpawnLoc(0, sender.getLocation());
         ac.addArena(arena);
         ArenaControllerInterface aci = new ArenaControllerInterface(arena);
         aci.create();
         new ArenaCreateEvent(arena).callEvent();
         aci.init();
 
         sendMessage(sender, "&2You have created the arena &6" + arena);
         sendMessage(sender,
                 "&2A spawn point has been created where you are standing");
         sendMessage(sender,
                 "&2You can add/change spawn points using &6/arena alter "
                         + arena.getName() + " <1,2,...,x : which spawn>");
         BattleArena.saveArenas(arena.getArenaType().getPlugin());
         return BattleArena.getSelf().getArenaEditorExecutor().arenaSelect(sender, arena);
     }
 
     @MCCommand(cmds = { "setArenaOption", "setOption", "alter", "edit" }, admin = true, perm = "arena.alter")
     public boolean arenaSetOption(CommandSender sender, Arena arena, String[] args) {
         try {
             ArenaAlterController.setArenaOption(sender, arena, false, args);
         } catch (IllegalStateException e) {
             return sendMessage(sender, "&c" + e.getMessage());
         }
         return true;
     }
 
     @MCCommand(cmds = { "select", "sel" }, admin = true, perm = "arena.alter")
     public boolean arenaSelect(CommandSender sender, Arena arena) {
         try {
             ArenaEditorExecutor aee = BattleArena.getSelf().getArenaEditorExecutor();
             return aee.arenaSelect(sender, arena);
         } catch (IllegalStateException e) {
             return sendMessage(sender, "&c" + e.getMessage());
         }
     }
 
     //	@MCCommand(cmds = {"dao", "deleteArenaOption" }, admin = true, perm = "arena.alter")
     //	public boolean arenaDeleteOption(CommandSender sender, MatchParams params, Arena arena, String[] args) {
     //		try {
     //			ArenaAlterController.deleteArenaOption(sender, params, arena, args);
     //		} catch (IllegalStateException e) {
     //			return sendMessage(sender, "&c" + e.getMessage());
     //		}
     //		return true;
     //	}
 
     @MCCommand(cmds = { "setOption", "setGameOption" }, admin = true, perm = "arena.alter")
     public boolean setOption(CommandSender sender, MatchParams params,String[] args) {
         ParamAlterController pac = new ParamAlterController(params);
         pac.setOption(sender, args);
         return true;
     }
 
     @MCCommand(cmds = { "deleteOption", "deleteGameOption" }, admin = true, perm = "arena.alter")
     public boolean deleteOption(CommandSender sender, MatchParams params,String[] args) {
         ParamAlterController pac = new ParamAlterController(params);
         pac.deleteOption(sender, args);
         return true;
     }
 
     @MCCommand(cmds = { "restoreDefaultOptions" }, admin = true, perm = "arena.alter")
     public boolean restoreDefaultOptions(CommandSender sender, Arena arena) {
         try {
             ArenaAlterController.restoreDefaultArenaOptions(arena);
         } catch (IllegalStateException e) {
             return sendMessage(sender, "&c" + e.getMessage());
         }
         return sendMessage(sender,"&2Arena &6"+arena.getName()+"set back to default game options");
     }
 
     @MCCommand(cmds = { "restoreDefaultArenaOptions" }, admin = true, perm = "arena.alter")
     public boolean restoreDefaultOptions(CommandSender sender, MatchParams params) {
         try {
             ArenaAlterController.restoreDefaultArenaOptions(params);
         } catch (IllegalStateException e) {
             return sendMessage(sender, "&c" + e.getMessage());
         }
         return sendMessage(sender,"&2Game type &6"+params.getType()+" set back to default game options");
     }
 
     @MCCommand(cmds = { "addLobby", "al" }, admin = true, perm = "arena.alter")
     public boolean addLobby(Player sender, MatchParams params, String[] args) {
         ArenaAlterController.alterLobby(sender, params, args);
         return true;
     }
 
     @MCCommand(cmds = { "rescind" }, helpOrder = 13)
     public boolean duelRescind(ArenaPlayer player) {
         if (!dc.hasChallenger(player)) {
             return sendMessage(player, "&cYou haven't challenged anyone!");
         }
         Duel d = dc.rescind(player);
         ArenaTeam t = d.getChallengerTeam();
         t.sendMessage("&4[Duel] &6" + player.getDisplayName()
                 + "&2 has cancelled the duel challenge!");
         for (ArenaPlayer ap : d.getChallengedPlayers()) {
             sendMessage(ap, "&4[Duel] &6" + player.getDisplayName()
                     + "&2 has cancelled the duel challenge!");
         }
         return true;
     }
 
     @MCCommand(cmds = { "reject" }, helpOrder = 12)
     public boolean duelReject(ArenaPlayer player) {
         if (!dc.isChallenged(player)) {
             return sendMessage(player, "&cYou haven't been invited to a duel!");
         }
         Duel d = dc.reject(player);
         ArenaTeam t = d.getChallengerTeam();
         String timeRem = TimeUtil
                 .convertSecondsToString(Defaults.DUEL_CHALLENGE_INTERVAL);
         t.sendMessage("&4[Duel] &cThe duel was cancelled as &6"
                 + player.getDisplayName() + "&c rejected your offer");
         t.sendMessage("&4[Duel] &cYou can challenge them again in " + timeRem);
         for (ArenaPlayer ap : d.getChallengedPlayers()) {
             if (ap == player)
                 continue;
             sendMessage(
                     ap,
                     "&4[Duel] &cThe duel was cancelled as &6"
                             + player.getDisplayName() + "&c rejected the duel");
         }
         sendMessage(player,
                 "&4[Duel] &cYou rejected the duel, you can't be challenged again for&6 "
                         + timeRem);
         return true;
     }
 
     @MCCommand(cmds = { "accept" }, helpOrder = 11)
     public boolean duelAccept(ArenaPlayer player) {
         if (!canJoin(player)) {
             return true;
         }
         Duel d = dc.getDuel(player);
         if (d == null) {
             return sendMessage(player, "&cYou haven't been invited to a duel!");
         }
         Double wager = (Double) d.getDuelOptionValue(DuelOption.MONEY);
         if (wager != null) {
             if (MoneyController.balance(player.getName()) < wager) {
                 sendMessage(player,
                         "&4[Duel] &cYou don't have enough money to accept the wager!");
                 dc.cancelFormingDuel(d, "&4[Duel]&6" + player.getDisplayName()
                         + " didn't have enough money for the wager");
                 return true;
             }
         }
         for (ArenaPlayer ap : d.getAllPlayers()) {
             if (!d.getOptions().matches(ap, d.getMatchParams())) {
                 dc.cancelFormingDuel(d,
                         "&4[Duel]&6"
                                 + player.getDisplayName()
                                 + " wasn't within "
                                 + d.getMatchParams().getTransitionOptions()
                                 .getOptions(MatchState.PREREQS)
                                 .getWithinDistance()
                                 + "&c blocks of an arena");
                 return true;
             }
         }
         if (dc.accept(player) == null) {
             return true;
         }
         ArenaTeam t = d.getChallengerTeam();
         t.sendMessage("&4[Duel] &6" + player.getDisplayName()
                 + "&2 has accepted your duel offer!");
         for (ArenaPlayer ap : d.getChallengedPlayers()) {
             if (ap == player)
                 continue;
             sendMessage(ap, "&4[Duel] &6" + player.getDisplayName()
                     + "&2 has accepted the duel offer");
         }
         return sendMessage(player, "&cYou have accepted the duel!");
     }
 
     @MCCommand(cmds = { "duel", "d" })
     public boolean duel(ArenaPlayer player, String args[]) {
         MatchParams mp = ParamController.getMatchParamCopy("duel");
         return mp == null || duel(player, mp, args);
     }
 
     @MCCommand(cmds = { "duel", "d" }, helpOrder = 10)
     public boolean duel(ArenaPlayer player, MatchParams mp, String args[]) {
         if (!PermissionsUtil.hasMatchPerm(player, mp, "duel")) {
             return sendMessage(
                     player,
                     "&cYou don't have permission to duel in a &6"+ mp.getCommand());
         }
         if (isDisabled(player, mp)) {
             return true;
         }
 
         if (dc.isChallenged(player)) {
             sendMessage(player,
                     "&4[Duel] &cYou have already been challenged to a duel!");
             return sendMessage(player, "&4[Duel] &6/" + mp.getCommand()
                     + " reject&c to cancel the duel before starting your own");
         }
 
         // / Can the player add this match/event at this moment?
         if (!canJoin(player)) {
             return true;
         }
         if (EventController.isEventType(mp.getName())) {
             return sendMessage(player,
                     "&4[Duel] &cYou can't duel someone in an Event type!");
         }
 
         // / Parse the duel options
         DuelOptions duelOptions;
         try {
             duelOptions = DuelOptions.parseOptions(mp, player,
                     Arrays.copyOfRange(args, 1, args.length));
         } catch (InvalidOptionException e1) {
             return sendMessage(player, e1.getMessage());
         }
         Double wager = (Double) duelOptions.getOptionValue(DuelOption.MONEY);
         if (wager != null) {
             if (MoneyController.balance(player.getName()) < wager) {
                 return sendMessage(player,
                         "&4[Duel] You can't afford that wager!");
             }
         }
         if (!duelOptions.matches(player, mp)) {
             return sendMessage(player, "&cYou need to be within &6"
                     + mp.getTransitionOptions().getOptions(MatchState.PREREQS)
                     .getWithinDistance() + "&c blocks of an arena");
         }
         // / Announce warnings
         for (ArenaPlayer ap : duelOptions.getChallengedPlayers()) {
             if (!canJoin(ap)) {
                 return sendMessage(player, "&4[Duel] &6" + ap.getDisplayName()
                         + "&c is in a match, event, or queue");
             }
             if (!duelOptions.matches(ap, mp)) {
                 return sendMessage(
                         player,
                         "&6"
                                 + ap.getDisplayName()
                                 + "&c needs to be within "
                                 + mp.getTransitionOptions()
                                 .getOptions(MatchState.PREREQS)
                                 .getWithinDistance()
                                 + "&c blocks of an arena");
             }
 
             final MatchTransitions ops = mp.getTransitionOptions();
             if (ops != null) {
                 ArenaTeam t = TeamController.createTeam(mp, ap);
                 // / Check ready
                 if (!ops.teamReady(t, null)) {
                     sendMessage(player, "&c" + t.getDisplayName()
                             + "&c doesn't have the prerequisites for this duel");
                     return true;
                 }
             }
 
             if (dc.isChallenged(ap)) {
                 return sendMessage(player, "&4[Duel] &6" + ap.getDisplayName()
                         + "&c already has been challenged!");
             }
             if (!PermissionsUtil.hasMatchPerm(ap, mp, "duel")) {
                 return sendMessage(player,
                         "&6" + ap.getDisplayName()
                                 + "&c doesn't have permission to duel in a &6"
                                 + mp.getCommand());
             }
 
             Long grace = dc.getLastRejectTime(ap);
             if (grace != null
                     && System.currentTimeMillis() - grace < Defaults.DUEL_CHALLENGE_INTERVAL * 1000) {
                 return sendMessage(
                         player,
                         "&4[Duel] &6"
                                 + ap.getDisplayName()
                                 + "&c can't be challenged for &6"
                                 + TimeUtil
                                 .convertMillisToString(Defaults.DUEL_CHALLENGE_INTERVAL
                                         * 1000
                                         - (System.currentTimeMillis() - grace)));
             }
             if (wager != null) {
                 if (MoneyController.balance(ap.getName()) < wager) {
                     return sendMessage(player,
                             "&4[Duel] &6" + ap.getDisplayName()
                                     + "&c can't afford that wager!");
                 }
             }
         }
 
         // / Get our team1
         ArenaTeam t = TeamController.getTeam(player);
         if (t == null) {
             t = TeamFactory.createCompositeTeam(0, mp, player);
         } else {
             t.setIndex(0);
         }
         for (ArenaPlayer ap : t.getPlayers()) {
             if (!duelOptions.matches(ap, mp)) {
                 return sendMessage(
                         player,
                         "&6"
                                 + ap.getDisplayName()
                                 + "&c needs to be within "
                                 + mp.getTransitionOptions()
                                 .getOptions(MatchState.PREREQS)
                                 .getWithinDistance());
             }
 
             if (wager != null) {
                 if (MoneyController.balance(ap.getName()) < wager) {
                     return sendMessage(player,
                             "&4[Duel] Your teammate &6" + ap.getDisplayName()
                                     + "&c can't afford that wager!");
                 }
             }
         }
 
         mp.setNTeams(new MinMax(2));
 
         int size = duelOptions.getChallengedPlayers().size();
         mp.setMinTeamSize(Math.min(t.size(), size));
         mp.setMaxTeamSize(Math.max(t.size(), size));
         // / set our default rating
         mp.setRated(Defaults.DUEL_ALLOW_RATED && mp.isRated());
         // / allow specified options to overrule
         if (duelOptions.hasOption(DuelOption.RATED))
             mp.setRated(true);
         else if (duelOptions.hasOption(DuelOption.UNRATED))
             mp.setRated(false);
 
         // / Check to make sure at least one arena can be joined at some time
         Arena arena = ac.getArenaByMatchParams(mp, null);
         if (arena == null) {
             Map<Arena, List<String>> reasons = ac.getNotMachingArenaReasons(mp, null);
             if (!reasons.isEmpty()) {
                 for (Arena a : reasons.keySet()) {
                     List<String> rs = reasons.get(a);
                     if (!rs.isEmpty())
                         return sendMessage(player, "&c" + rs.get(0));
                 }
             }
             return sendSystemMessage(player, "valid_arena_not_built", mp.getName());
         }
         final MatchTransitions ops = mp.getTransitionOptions();
         if (ops == null) {
             return sendMessage(player,
                     "&cThis match type has no valid options, contact an admin to fix ");
         }
 
         Duel duel = new Duel(mp, t, duelOptions);
 
         // / Announce to the 2nd team
         String t2 = duelOptions.getChallengedTeamString();
         for (ArenaPlayer ap : duelOptions.getChallengedPlayers()) {
             String other = duelOptions.getOtherChallengedString(ap);
             if (!other.isEmpty()) {
                 other = "and " + other + " ";
             }
             sendMessage(
                     ap,
                     "&4[" + mp.getName() + " Duel] &6" + t.getDisplayName()
                             + "&2 " + MessageUtil.hasOrHave(t.size())
                             + " challenged you " + other + "to a &6"
                             + mp.getName() + " &2duel!");
             sendMessage(ap,
                     "&4[Duel] &2Options: &6" + duelOptions.optionsString(mp));
             sendMessage(ap, "&4[Duel] &6/" + mp.getCommand()
                     + " accept &2: to accept. &6" + mp.getCommand()
                     + " reject &e: to reject");
         }
 
         sendMessage(player, "&4[Duel] &2You have sent a challenge to &6" + t2);
         sendMessage(player,
                 "&4[Duel] &2You can rescind by typing &6/" + mp.getCommand()
                         + " rescind");
         dc.addOutstandingDuel(duel);
         return true;
     }
 
     @MCCommand(cmds = { "start" }, admin = true, perm = "arena.start")
     public boolean arenaStart(CommandSender sender, MatchParams mp) {
         List<Match> matches = ac.getRunningMatches(mp);
         if (matches.isEmpty()) {
             return sendMessage(sender, "&cThere are no open &6" + mp.getType());
         } else if (matches.size() > 1) {
             return sendMessage(sender, "&cThere are multiple &6" + mp.getType()
                     + "&c open.  Specify which arena.\n&e/" + mp.getCommand()
                     + " cancel <arena>");
         }
         ac.startMatch(matches.get(0));
         return sendMessage(sender, "&2" + mp.getType() + " has been started");
     }
 
     @MCCommand(cmds = { "forceStart" }, admin = true, perm = "arena.forcestart")
     public boolean arenaForceStart(CommandSender sender, MatchParams mp) {
         if (ac.forceStart(mp, false)) {
             return sendMessage(sender, "&2" + mp.getType()+ " has been started");
         } else {
             return sendMessage(sender, "&c" + mp.getType()+ " could not be started");
         }
     }
 
     @MCCommand(cmds = { "choose", "class" })
     public boolean chooseClass(ArenaPlayer sender, String arenaClass) {
         ArenaClass ac = ArenaClassController.getClass(arenaClass);
         if (ac == null) {
             return sendMessage(sender, "&cThere is no class called &6"+ arenaClass);}
        if (sender.getCompetition() == null) {
            return sendMessage(sender, "&cYou aren't in a match&6");}
         ArenaClassController.changeClass(sender.getPlayer(), sender.getCompetition(), ac);
         return true;
     }
 
     @MCCommand(cmds = { "list" })
     public boolean arenaList(CommandSender sender, MatchParams mp, String[] args) {
         boolean all = args.length > 1 && (args[1]).equals("all");
 
         Collection<Arena> arenas = ac.getArenas().values();
         HashMap<ArenaType, Collection<Arena>> arenasbytype = new HashMap<ArenaType, Collection<Arena>>();
         for (Arena arena : arenas) {
             Collection<Arena> as = arenasbytype.get(arena.getArenaType());
             if (as == null) {
                 as = new ArrayList<Arena>();
                 arenasbytype.put(arena.getArenaType(), as);
             }
             as.add(arena);
         }
         if (arenasbytype.isEmpty()) {
             sendMessage(sender, "&cThere are no &6" + mp.getName()
                     + "&c arenas");
         }
         for (ArenaType at : arenasbytype.keySet()) {
             if (!all && !at.matches(mp.getType()))
                 continue;
             Collection<Arena> as = arenasbytype.get(at);
             if (!as.isEmpty()) {
                 sendMessage(sender, "&e------ Arenas for &6" + at.toString()
                         + "&e ------");
                 for (Arena arena : as) {
                     sendMessage(sender, arena.toSummaryString());
                 }
             }
         }
         if (!all)
             sendMessage(sender, "&6/arena list all&e: to see all arenas");
         return sendMessage(sender,
                 "&6/arena info <arenaname>&e: for details on an arena");
     }
 
     public boolean canLeave(ArenaPlayer p) {
         return true;
     }
 
     public boolean canJoin(ArenaTeam t) {
         return canJoin(t, true);
     }
 
     public boolean canJoin(ArenaTeam t, boolean showMessages) {
         for (ArenaPlayer ap : t.getPlayers()) {
             if (!_canJoin(ap, showMessages, true))
                 return false;
         }
         return true;
     }
 
     public boolean canJoin(ArenaPlayer player) {
         return canJoin(player, true);
     }
 
     public boolean canJoin(ArenaPlayer player, boolean showMessages) {
         return _canJoin(player, showMessages, false);
     }
 
     private boolean _canJoin(ArenaPlayer player, boolean showMessages, boolean teammate) {
         /// Check for any competition
         if (player.getCompetition() != null) {
             if (showMessages)
                 sendMessage(player, "&cYou are still in the "+ player.getCompetition().getName()+ ". &6/arena leave");
             return false;
         }
         /// Inside the queue waiting for a match?
         if (InArenaListener.inQueue(player.getName())){
             sendMessage(player, "&eYou are in the queue.");
             if (showMessages)
                 sendMessage(player, "&eType &6/arena leave");
             return false;
         }
         /// Inside MobArena?
         if (MobArenaInterface.hasMobArena()
                 && MobArenaInterface.insideMobArena(player)) {
             if (showMessages)
                 sendMessage(player, "&cYou need to finish with MobArena first!");
             return false;
         }
 
         /// Check for player in combat
         if (CombatTagInterface.isTagged(player.getPlayer()) ||
                 (HeroesController.enabled() && HeroesController.isInCombat(player.getPlayer()))) {
             if (showMessages)
                 sendMessage(player, "&cYou are in combat!");
             return false;
         }
 
         /// Inside an Event?
         Event ae = insideEvent(player);
         if (ae != null) {
             if (showMessages)
                 sendMessage(player, "&eYou need to leave the Event first. &6/"+ ae.getCommand() + " leave");
             return false;
         }
 
         // / Inside a match?
         Match am = ac.getMatch(player);
         if (am != null) {
             ArenaTeam t = am.getTeam(player);
             if (am.isHandled(player)
                     || (!t.hasLeft(player) && t.hasAliveMember(player))) {
                 if (showMessages)
                     sendMessage(player, "&eYou are already in a match.");
                 return false;
             } else {
                 return true;
             }
         }
         if (!teammate) {
             // / Inside a forming team?
             if (teamc.inFormingTeam(player)) {
                 FormingTeam ft = teamc.getFormingTeam(player);
                 if (ft.isJoining(player)) {
                     if (showMessages)
                         sendMessage(player,"&eYou have been invited to the team. "+ ft.getDisplayName());
                     if (showMessages)
                         sendMessage(player, "&eType &6/team add|decline");
                 } else if (!ft.hasAllPlayers()) {
                     if (showMessages)
                         sendMessage(player,"&eYour team is not yet formed. &6/team disband&e to leave");
                     if (showMessages)
                         sendMessage(player,"&eYou are still missing "+ MessageUtil.joinPlayers(
                                         ft.getUnjoinedPlayers(), ", ")+ " !!");
                 }
                 return false;
             }
             // / Make a team for the new Player
             ArenaTeam t = teamc.getSelfFormedTeam(player);
             if (t != null) {
                 for (ArenaPlayer p : t.getPlayers()) {
                     if (p == player) {
                         continue;
                     }
                     if (!_canJoin(p, true, true)) {
                         sendSystemMessage(player, "teammate_cant_join");
                         sendMessage(player, "&6/team leave: &cto leave the team");
                         return false;
                     }
                 }
             }
         }
 
         if (dc.hasChallenger(player)) {
             if (showMessages)
                 sendMessage(player,
                         "&cYou need to rescind your challenge first! &6/arena rescind");
             return false;
         }
 
         if (EssentialsController.enabled()
                 && EssentialsController.inJail(player)) {
             if (showMessages)
                 sendMessage(player, "&cYou are still in jail!");
             return false;
         }
         return true;
     }
 
     public Event insideEvent(ArenaPlayer p) {
         return EventController.insideEvent(p);
     }
 
     public boolean checkAndRemoveFee(MatchParams pi, ArenaTeam t) {
         if (pi.hasEntranceFee()) {
             Double fee = pi.getEntranceFee();
             if (fee == null || fee <= 0)
                 return true;
             boolean hasEnough = true;
             for (ArenaPlayer player : t.getPlayers()) {
                 boolean has = MoneyController.hasEnough(player.getName(), fee);
                 hasEnough &= has;
                 if (!has)
                     sendMessage(player, "&eYou need &6" + fee + "&e to compete");
             }
             if (!hasEnough) {
                 t.sendMessage("&eYour team does not have enough money to compete");
                 return false;
             }
             for (ArenaPlayer player : t.getPlayers()) {
                 MoneyController.subtract(player.getName(), fee);
                 sendMessage(player, "&6" + fee+ " has been subtracted from your account");
             }
         }
         return true;
     }
 
     public static boolean refundFee(MatchParams pi, ArenaTeam t) {
         final MatchTransitions tops = pi.getTransitionOptions();
         if (tops.hasEntranceFee()) {
             Double fee = tops.getEntranceFee();
             if (fee == null || fee <= 0)
                 return true;
             for (ArenaPlayer player : t.getPlayers()) {
                 MoneyController.add(player.getName(), fee);
                 sendMessage(player,
                         "&eYou have been refunded the entrance fee of &6" + fee);
             }
         }
         return true;
     }
 
     protected Arena getArena(String name) {
         return ac.getArena(name);
     }
 
     public static boolean checkPlayer(CommandSender sender) {
         if (!(sender instanceof Player)) {
             sendMessage(sender, "&cYou need to be online for this command!");
             return false;
         }
         return true;
     }
 
     public void setDisabled(List<String> disabled) {
         this.disabled.addAll(disabled);
     }
 
     public Collection<String> getDisabled() {
         return this.disabled;
     }
 }
