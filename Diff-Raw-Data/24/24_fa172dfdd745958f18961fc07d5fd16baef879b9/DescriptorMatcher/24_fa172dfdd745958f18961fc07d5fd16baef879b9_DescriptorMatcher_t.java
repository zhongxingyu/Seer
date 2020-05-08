 package rgbdslam;
 
 import java.util.ArrayList;
 import java.util.List;
 import rgbdslam.KdTree.Entry;
 
 /**
  * Class that matches feature descriptors between two frames.
  * @author pdaquino
  */
 public class DescriptorMatcher {
 
     private List<ImageFeature> features1, features2;
     private KdTree.SqrEuclid<ImageFeature> kdtree1, kdtree2;
     public final static boolean ENFORCE_MARRIAGE = true;
     public final static boolean ENFORCE_DISTANT_2ND = true;
    // for a match to be valid, its descriptorDistance to be at most 0.8 * distance_of_second_best_match
     public static final double ALPHA = 0.8;
 
     /**
      * Represents a match between features in two images. We are matching
      * features in features2 to features1.
      */
     public static class Match {
 
         public ImageFeature feature1;
         public ImageFeature feature2;
        public double descriptorDistance;
        public double xyzDistance;
 
         public Match(ImageFeature f1, ImageFeature f2, double d) {
             this.feature1 = f1;
             this.feature2 = f2;
            this.descriptorDistance = d;
         }
     }
 
     /**
      * Constructs a DescriptorMatcher that matches features in features2 to features1.
      * @param features1 the features in the 1st image
      */
     public DescriptorMatcher(List<ImageFeature> features1, List<ImageFeature> features2) {
         this.features1 = features1;
         this.features2 = features2;
         this.kdtree1 = buildKdTree(features1);
         this.kdtree2 = buildKdTree(features2);
     }
 
     private KdTree.SqrEuclid<ImageFeature> buildKdTree(List<ImageFeature> features) {
         if (features.size() > 0) {
             int descriptorSize = features.get(0).getDescriptor().length;
             KdTree.SqrEuclid<ImageFeature> kdtree = new KdTree.SqrEuclid<ImageFeature>(descriptorSize, features.size());
 
             for (ImageFeature feature : features) {
                 kdtree.addPoint(feature.getDescriptor(), feature);
             }
             return kdtree;
 
         } else {
             return new KdTree.SqrEuclid<ImageFeature>(0, 0);
         }
     }
 
     /**
      * Return the matches of features1 in features2
      * @return a list of matches
      */
     public ArrayList<Match> match() {
         ArrayList<Match> matches = new ArrayList<Match>(features2.size());
 
         for (ImageFeature feature : features2) {
             Entry<ImageFeature> match = getBestMatch(kdtree1.nearestNeighbor(
                     feature.getDescriptor(), 2, true), feature);
 
             // getBestMatch returns null if no suitable match could be found
             if (match != null) {
                 matches.add(new Match(match.value, feature, match.distance));
             }
         }
 
         return matches;
     }
 
     protected Entry<ImageFeature> getBestMatch(List<Entry<ImageFeature>> matches, ImageFeature queryFeature) {
         // the list is sorted backwards; matches[1] is the best one
         if (matches.isEmpty()) {
             return null;
         }
         Entry<ImageFeature> bestMatch = matches.get(matches.size() - 1);
 
         if (ENFORCE_DISTANT_2ND) {
             // enforce best_distance < alpha * second_best_distance
             if (matches.size() > 1) {
                 Entry<ImageFeature> secondBest = matches.get(matches.size() - 2);
                 assert bestMatch.distance <= secondBest.distance;
                 if (bestMatch.distance >= ALPHA * secondBest.distance) {
                     return null;
                 }
             }
         }
 
         if (ENFORCE_MARRIAGE)  {
             // enforce marriage constraint: best match from 2->1 is the same as from 1->2
             // note: queryFeature (in features2) has been matched to bestMatch.value (in features1)
             // we want to check if bestMatch.value would also map to queryFeature
             List<Entry<ImageFeature>> matchesInFt1 = kdtree2.nearestNeighbor(
                     bestMatch.value.getDescriptor(), 1, true);
             if (matchesInFt1.isEmpty() || matchesInFt1.get(0).value != queryFeature) {
                 // using "==" is OK because we are comparing the same object
                 return null;
             }
         }
         
         return bestMatch;
     }
 }
