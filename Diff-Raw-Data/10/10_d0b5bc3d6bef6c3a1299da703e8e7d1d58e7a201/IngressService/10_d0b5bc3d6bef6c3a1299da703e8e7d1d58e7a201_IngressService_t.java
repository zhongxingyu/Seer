 package pl.swiatowy.ingress;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import pl.swiatowy.ingress.actions.Action;
 import pl.swiatowy.ingress.actions.RawActionResult;
 import pl.swiatowy.ingress.actions.comm.CommAction;
 import pl.swiatowy.ingress.actions.comm.CommCallParam;
 import pl.swiatowy.ingress.actions.comm.CommCallResult;
 import pl.swiatowy.ingress.actions.comm.PlayerAP;
 import pl.swiatowy.ingress.actions.stats.PlayerStatsParam;
 
 import java.util.*;
 import java.util.concurrent.ForkJoinPool;
 import java.util.concurrent.RecursiveAction;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Created: 25.08.1323:59
  *
  * @author swiatek25
  */
 public class IngressService {
 
     private static final int MAX_INTERVAL_MIN = 10;
     private static final int MAX_ENTRIES = 500;
     private static final long MAX_INTERVAL_MILLIS = TimeUnit.MINUTES.toMillis(MAX_INTERVAL_MIN);
     private ForkJoinPool worker = new ForkJoinPool();
 
     public Map<CommAction, Integer> getPlayerStatistics(String player, Date from, Date to) {
         List<CommCallResult.CommEntry> commEntries = getCommEntries(from, to);
         Map<CommAction, Integer> actionSummary = new HashMap<>();
         worker.invoke(new CommEntrySummaryCompute(player, commEntries, actionSummary));
         return actionSummary;
     }
 
     public PlayerAP getPlayerAP(String player, Date from, Date to) {
         long totalPoints = 0;
         Map<CommAction, Integer> playerStatistics = getPlayerStatistics(player, from, to);
         for (Map.Entry<CommAction, Integer> statEntry : playerStatistics.entrySet()) {
             totalPoints += statEntry.getValue() * statEntry.getKey().getPoints();
         }
         return new PlayerAP(playerStatistics, totalPoints);
     }
 
     public RawActionResult getPlayerStatisticsGlobal(String player) {
         PlayerStatsParam params = new PlayerStatsParam(player);
         return (RawActionResult) Action.GET_PLAYER_STATS_CALL.execute(params);
     }
 
     public List<CommCallResult.CommEntry> getCommEntries(Date from, Date to) {
         List<CommCallResult.CommEntry> results = new ArrayList<>();
         worker.invoke(new CommEntriesCompute(from, to, results));
         Collections.sort(results);
         return results;
     }
 
     public List<CommCallResult.CommEntry> getCommEntries(Date from) {
         return getCommEntries(from, new Date());
     }
 
     private final class CommEntriesCompute extends RecursiveAction {
 
         private final Date from;
         private final Date to;
         private final List<CommCallResult.CommEntry> results;
 
         private CommEntriesCompute(Date from, Date to, List<CommCallResult.CommEntry> results) {
             this.from = from;
             this.to = to;
             this.results = results;
         }
 
         @Override
         protected void compute() {
             if (to != null && to.getTime() - from.getTime() > MAX_INTERVAL_MILLIS) {
                 //fork
                 Calendar fromCal = Calendar.getInstance();
                 fromCal.setTime(from);
                 Date from = fromCal.getTime();
                 fromCal.add(Calendar.MINUTE, MAX_INTERVAL_MIN);
                 Date to = fromCal.getTime();
                 CommEntriesCompute task1 = new CommEntriesCompute(from, to, results);
                 fromCal.add(Calendar.SECOND, 1);
                 invokeAll(task1, new CommEntriesCompute(fromCal.getTime(), this.to, results));
                 //fork end
             } else {
                 computeDirectly();
             }
         }
 
         private void computeDirectly() {
             results.addAll(getComm(new CommCallParam(from, to)));
         }
 
         private List<CommCallResult.CommEntry> getComm(CommCallParam params) {
             params.setNumberOfEntries(MAX_ENTRIES);
             CommCallResult commCallResult = (CommCallResult) Action.COMM_CALL.execute(params);
             List<CommCallResult.CommEntry> commLines = commCallResult.getCommLines();
             return commLines;
         }
     }
 
     private class CommEntrySummaryCompute extends RecursiveAction {
         private String player;
         private final List<CommCallResult.CommEntry> commEntries;
         private final Map<CommAction, Integer> actionSummary;
 
         public CommEntrySummaryCompute(String player, List<CommCallResult.CommEntry> commEntries, Map<CommAction, Integer> actionSummary) {
             this.player = player;
             this.commEntries = commEntries;
             this.actionSummary = actionSummary;
         }
 
         @Override
         protected void compute() {
             Iterable<CommCallResult.CommEntry> playerEntries = Iterables.filter(commEntries, new Predicate<CommCallResult.CommEntry>() {
                 @Override
                 public boolean apply(CommCallResult.CommEntry commEntry) {
                     return commEntry.getPlayer().equals(player);
                 }
             });
             for (CommCallResult.CommEntry playerEntry : playerEntries) {
                CommAction action = CommAction.fromActionName(playerEntry.getAction());
                if (actionSummary.containsKey(action)) {
                    Integer currentCount = actionSummary.get(action);
                    actionSummary.put(action, currentCount + 1);
                 } else {
                    actionSummary.put(action, 1);
                 }
             }
         }
     }
 }
