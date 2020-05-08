import java.util.ArrayList;
import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 /**
 * TODO
  * Author: evan.pollan
  */
 public final class MedianThreshold {
 
     private MedianThreshold() {
     }
 
     public interface Measurable<T> extends Comparable<T> {
         int getMetric();
     }
 
    public static <T extends Measurable> List<T> belowMedianThreshold(List<T> candidates, float threshold) {
         Collections.sort(candidates);
         int medianPos = (int) Math.floor(candidates.size() / 2);
         int endPos = 0;
         if (candidates.size() > 0) {
             int medianMetric = candidates.get(medianPos).getMetric();
             int thresholdMetric = (int) Math.ceil(medianMetric * threshold);
             int i = 0;
             for (; i < candidates.size(); i++) {
                 if (candidates.get(i).getMetric() > thresholdMetric) {
                     break;
                 }
             }
             endPos = Math.max(i - 1, 0);
         }
 
         return candidates.subList(0, endPos);
     }
 
    public static <T extends Measurable> List<T> aboveMedianThreshold(List<T> candidates, float threshold) {
         Collections.sort(candidates);
         int medianPos = (int) Math.floor(candidates.size() / 2);
         int startPos = 0;
         if (candidates.size() > 0) {
             int medianMetric = candidates.get(medianPos).getMetric();
             int thresholdMetric = (int) Math.ceil(medianMetric * threshold);
             int i = candidates.size() - 1;
             for (; i >= 0; i--) {
                 if (candidates.get(i).getMetric() < thresholdMetric) {
                     break;
                 }
             }
             startPos = Math.min(candidates.size() - 1, i + 1);
         }
 
         return candidates.subList(startPos, candidates.size() - 1);
     }
 }
