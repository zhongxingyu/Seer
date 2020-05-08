 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package madsdf.ardrone.controller.templates;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Ordering;
 import java.util.List;
 import java.util.Map;
 import madsdf.ardrone.ActionCommand;
 
 /**
  *
  * @author julien
  */
 public class RepetitiveGestureDetector implements GestureDetector {
 
     private final static float STDDEV_THRESHOLD = 2000;
     private final static float NEAREST_DIST_THRESHOLD = 100000;
     private final static int NN_MIN_AGREE = (int) (KNNGestureController.KNN_K * 2. / 3.);
     
     // Minimum delay between two DIFFERENT actions
     private final static long INTER_ACTION_DELAY = 1000;
     
     private KNN knn;
     private float stddev;
     
     // Record last time given action was decided
     //private final Map<ActionCommand, Long> prevTimestampMS = Maps.newHashMap();
     private long prevDecidedMS = System.currentTimeMillis();;
     private ActionCommand prevDecided = ActionCommand.NOTHING;
 
     public RepetitiveGestureDetector() {
         /*for (ActionCommand a: ActionCommand.values()) {
             prevTimestampMS.put(a, System.currentTimeMillis());
         }*/
     }
     
     @Override
     public void addVotation(KNN knn, float stddev) {
         this.knn = knn;
         this.stddev = stddev;
     }
     
     @Override
     public long getDurationMS() {
         throw new IllegalStateException("no action duration, shouldn't be called");
     }
     
     @Override
     public boolean hasActionDuration() {
         return false;
     }
 
     @Override
     public ActionCommand decide() {
        // Check stddev above threshold
        if (stddev < STDDEV_THRESHOLD) {
            return ActionCommand.NOTHING;
        }

         // Check that nearest neighbor is of majority class
         List<Map.Entry<ActionCommand, Float>> l = Lists.newArrayList(
                 knn.votesPerClass.entrySet());
         final ActionCommand bestClass = l.get(0).getKey();
         //System.out.println("best : " + bestClass);
         if (!knn.getNeighborClass(0).equals(bestClass)) {
             return ActionCommand.NOTHING;
         }
         //System.out.println("1");
         // Check dist of nearest neighbour below threshold
         if (knn.getNeighborDist(0) > NEAREST_DIST_THRESHOLD) {
             return ActionCommand.NOTHING;
         }
         //System.out.println("2");
 
         // Check number of agreeing NN
         //System.out.println("votes for best : " + knn.votesPerClass.get(bestClass));
         //System.out.println("min agree : " + NN_MIN_AGREE);
         if (knn.votesPerClass.get(bestClass) < NN_MIN_AGREE) {
             return ActionCommand.NOTHING;
         }
         
         // Return here because we don't want to update prevDecided
         if (bestClass == ActionCommand.NOTHING){
             return ActionCommand.NOTHING;
         }
         
         final long now = System.currentTimeMillis();
         if (prevDecided == ActionCommand.NOTHING || 
             prevDecided == bestClass ||
             (now - prevDecidedMS) > INTER_ACTION_DELAY) {
             prevDecidedMS = now;
             prevDecided = bestClass;
             return bestClass;
         } else {
             return ActionCommand.NOTHING;
         }
     }
 }
