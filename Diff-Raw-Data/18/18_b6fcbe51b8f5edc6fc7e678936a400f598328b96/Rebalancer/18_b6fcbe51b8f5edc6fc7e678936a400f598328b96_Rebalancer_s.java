 package org.dcache.poolmanager;
 
 import java.util.Collection;
 import java.util.ArrayList;
 
 import org.dcache.cells.CellCommandListener;
 import org.dcache.cells.CellStub;
 import org.dcache.commons.util.Strings;
 import org.dcache.pool.migration.PoolMigrationJobCancelMessage;
 
 import diskCacheV111.poolManager.CostModule;
 import diskCacheV111.poolManager.PoolSelectionUnit;
 import diskCacheV111.poolManager.PoolSelectionUnit.SelectionPool;
 import diskCacheV111.pools.PoolCostInfo.PoolSpaceInfo;
 import diskCacheV111.util.CacheException;
 
 import dmg.cells.nucleus.CellPath;
 import dmg.util.Args;
 
 /**
  * Implements commands to generate migration jobs to rebalance pools.
  */
 public class Rebalancer
     implements CellCommandListener
 {
     private final static String JOB_NAME = "rebalance";
 
     private final static String METRIC_RELATIVE = "relative";
     private final static String METRIC_SPACE_COST = "sc";
 
     private PoolSelectionUnit _psu;
     private CostModule _cm;
     private CellStub _poolStub;
 
     public void setPoolSelectionUnit(PoolSelectionUnit psu)
     {
         _psu = psu;
     }
 
     public void setCostModule(CostModule cm)
     {
         _cm = cm;
     }
 
     public void setPoolStub(CellStub poolStub)
     {
         _poolStub = poolStub;
     }
 
     private void cancelAll(Collection<SelectionPool> pools)
         throws CacheException, InterruptedException
     {
         // TODO: Use SpreadAndWait
         for (SelectionPool pool: pools) {
             _poolStub.sendAndWait(new CellPath(pool.getName()),
                                   new PoolMigrationJobCancelMessage(JOB_NAME, true));
         }
     }
 
     public final static String hh_rebalance_pgroup =
         "[-metric=relative|sc] [-refresh=<period>] <pgroup>";
     public final static String fh_rebalance_pgroup =
         "Moves files between pools of a pool group to balance space usage.\n\n" +
 
         "A migration job will be submitted to each pool in the pool group.\n" +
         "The combined effect of these migration jobs is to move files until\n" +
         "either the relative space usage (used space relative to the total\n" +
        "size of the poo) is the same or the space cost is the same; which\n" +
         "depends on the metric used. The default is balance relative space\n" +
        "usage\n\n" +
 
         "A pool can only  be the source of  one rebalance  run at a time.\n" +
         "Previous  rebalancing jobs  will be cancelled.  The PoolManager\n" +
         "maintains no state for the rebalancing job, however migration jobs\n" +
         "created by the rebalancer have a well known name (rebalance).\n\n" +
 
         "Migration jobs periodically query PoolManager about how much space\n" +
         "is used on each pool and about the space cost. There will thus be\n" +
         "a delay between  files being moved between pools  and the metric\n" +
         "being updated. It is expected that rebalancing jobs will overshoot\n" +
         "the target slightly. For very small pools on test instances this\n" +
         "effect will be more profound than on large pools. The effect can\n" +
         "be reduced by reducing the refresh period. The default period is\n" +
         "30 seconds.\n\n" +
 
         "The migration jobs created by the rebalancer will not survive a\n" +
         "pool restart. If the lots of files are written, deleted or moved\n" +
         "while the rebalancing job runs, then the pool group may not be\n" +
         "completely balanced when the jobs terminate. Run the rebalancer\n" +
         "a second time to improve the balance further.\n\n" +
 
         "This feature is EXPERIMENTAL. Please monitor the system while\n" +
         "rebalancing is in progress.";
     public String ac_rebalance_pgroup_$_1(Args args)
         throws CacheException, InterruptedException
     {
         String metric = args.getOpt("metric");
         String refresh = args.getOpt("refresh");
         String poolGroup = args.argv(0);
         int period = (refresh == null) ? 30 : Integer.parseInt(refresh);
 
         if (metric == null) {
             metric = METRIC_RELATIVE;
         }
 
         long used = 0;
         long total = 0;
         Collection<SelectionPool> pools = new ArrayList<SelectionPool>();
         Collection<String> names = new ArrayList<String>();
         for (SelectionPool pool: _psu.getPoolsByPoolGroup(poolGroup)) {
            if (pool.getPoolMode().isEnabled()) {
                PoolSpaceInfo cost =
                    _cm.getPoolCostInfo(pool.getName()).getSpaceInfo();
                used += cost.getUsedSpace();
                total += cost.getTotalSpace();
                 pools.add(pool);
                 names.add(pool.getName());
             }
         }
 
         String command;
         if (metric.equals(METRIC_RELATIVE)) {
             double factor = (double) used / (double) total;
             command =
                 String.format("migration move -id=%s -include-when='target.used < %2$f * target.total' -stop-when='targets == 0 or source.used <= %2$f * source.total' -refresh=%3$d %4$s", JOB_NAME, factor, period, Strings.join(names, " "));
         } else if (metric.equals(METRIC_SPACE_COST)) {
             command =
                 String.format("migration move -id=%s -include-when='target.spaceCost < source.spaceCost' -stop-when='targets == 0' -refresh=%d %s", JOB_NAME, period, Strings.join(names, " "));
         } else {
             throw new IllegalArgumentException("Unsupported value for -metric: " + metric);
         }
 
         cancelAll(pools);
 
         boolean success = false;
         try {
             // TODO: Use SpreadAndWait
             for (SelectionPool pool: pools) {
                 _poolStub.sendAndWait(new CellPath(pool.getName()), command,
                                       String.class);
             }
             success = true;
         } finally {
             if (!success) {
                 cancelAll(pools);
             }
         }
 
         return "Rebalancing jobs have been submitted to " +
             Strings.join(names, ", ") + ".";
     }
 
     public final static String hh_rebalance_cancel_pgroup =
         "<pgroup>";
     public final static String fh_rebalance_cancel_pgroup =
         "Cancels migration jobs created by the rebalancer.";
     public String ac_rebalance_cancel_pgroup_$_1(Args args)
         throws CacheException, InterruptedException
     {
         cancelAll(_psu.getPoolsByPoolGroup(args.argv(0)));
         return "";
     }
 }
