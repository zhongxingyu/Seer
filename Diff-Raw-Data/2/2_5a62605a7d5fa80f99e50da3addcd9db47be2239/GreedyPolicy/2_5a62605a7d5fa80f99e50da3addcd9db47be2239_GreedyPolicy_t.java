 
 package EECS545.target;
 
 import EECS545.State;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  *
  * @author Pedro
  */
 public class GreedyPolicy {
     public static class Choice {
         public double orientation;
         public double Q;
         public Choice(double orientation, double q) {
             this.orientation = orientation;
             this.Q = q;
         }
     }
     public static Choice chooseAction(WeightVector w, FeatureScaler scaler,
             ReducedState s) {
         double max = Double.NEGATIVE_INFINITY;
         double bestAngle = 0;
         
         double scaledAngle = 0;
         Double[] scaledFeatures = scaler.scale(s.getState());        
         while(scaledAngle < 1) {
             double q = w.transTimes(scaledFeatures, scaledAngle);
             if(q > max) {
                 max = q;
                 bestAngle = scaledAngle;
             }
             scaledAngle += 1 / Gun.ORIENTATION_ARCH;
         }
        Output.println("GreedyPolicy chose to fire at " + Gun.scaleToOrientation(bestAngle));
         return new Choice(Gun.scaleToOrientation(bestAngle), max);
     }
 }
