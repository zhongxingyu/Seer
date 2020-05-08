 package edu.mit.cci.amtprojects;
 
 import edu.mit.cci.amtprojects.HitManager;
 import edu.mit.cci.amtprojects.DbProvider;
 import edu.mit.cci.amtprojects.kickball.KickballProcessMonitor;
 import edu.mit.cci.amtprojects.kickball.cayenne.Batch;
 import edu.mit.cci.amtprojects.kickball.cayenne.TurkerLog;
 import edu.mit.cci.amtprojects.solver.Solution;
 import edu.mit.cci.amtprojects.solver.SolutionRank;
 import edu.mit.cci.amtprojects.solver.SolverHitCreator;
 import edu.mit.cci.amtprojects.solver.SolverProcessMonitor;
 import edu.mit.cci.amtprojects.solver.SolverTaskModel;
 import edu.mit.cci.amtprojects.solver.SolverTaskStatus;
 import edu.mit.cci.amtprojects.util.CayenneUtils;
 import edu.mit.cci.amtprojects.util.MturkUtils;
 import jsc.datastructures.MatchedData;
 import jsc.descriptive.MeanVar;
 import jsc.relatedsamples.FriedmanTest;
 import org.apache.cayenne.DataObjectUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.log4j.Logger;
 import org.apache.wicket.ajax.json.JSONException;
 
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.*;
 
 /**
  * User: jintrone
  * Date: 10/15/12
  * Time: 2:55 PM
  */
 public abstract class BatchProcessMonitor  {
 
      private final static Map<Long, BatchProcessMonitor> monitorMap = new HashMap<Long, BatchProcessMonitor>();
    private static Logger logger = Logger.getLogger(KickballProcessMonitor.class);
     protected long batchId;
     protected Timer t;
     boolean running = false;
 
     public BatchProcessMonitor(Batch b) {
         batchId = b.getId();
     }
 
     public static BatchProcessMonitor get(Batch b,Class<? extends BatchProcessMonitor> clz) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
         if (!(monitorMap.containsKey(b.getId()))) {
             Constructor ctor = clz.getConstructor(Batch.class);
             BatchProcessMonitor monitor = (BatchProcessMonitor) ctor.newInstance(b);
             monitorMap.put(b.getId(), monitor);
         }
         return monitorMap.get(b.getId());
     }
 
     public boolean isRunning() {
         return running;
     }
 
     public void restart() {
         if (running) {
             logger.warn("Task is already running; please use halt to stop if you wish to restart");
             return;
 
         }
 
         t = new Timer() {
             public void cancel() {
                 super.cancel();
                 running = false;
                 cleanup();
 
 
             }
         };
 
         t.schedule(new TimerTask() {
 
             public boolean cancel() {
                 boolean result = super.cancel();
                 t.cancel();
                 return result;
 
             }
 
             @Override
             public void run() {
                 running = true;
                 try {
                     update();
                 } catch (Exception e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     t.cancel();
                 }
 
 
             }
        }, 0, 30000);
     }
 
     public void halt() {
         if (running) {
             t.cancel();
         }
 
     }
 
     public void cleanup() {
          t = null;
         logger.info("Would be cleaning up");
     }
 
     public abstract void update() throws UnsupportedEncodingException, JSONException;
 
 
 
 
 
 
 
 }
