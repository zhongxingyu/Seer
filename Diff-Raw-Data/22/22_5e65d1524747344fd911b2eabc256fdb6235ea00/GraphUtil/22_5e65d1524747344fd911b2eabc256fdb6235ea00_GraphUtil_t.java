 package ru.spbau.bioinf.tagfinder;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 public class GraphUtil {

    public static final double EPSILON = 0.1;

     public static void generateEdges(Configuration conf, List<Peak> peaks) {
         int n = peaks.size();
         for (int i = 0; i < n; i++) {
             Peak peak = peaks.get(i);
             for (int j = i+1; j < n; j++) {
                 Peak next =  peaks.get(j);
                 double[] limits = conf.getEdgeLimits(peak, next);
                 for (Acid acid : Acid.values()) {
                     if (acid.match(limits)) {
                         peak.addNext(next);
                         break;
                     }
                 }
             }
         }
     }
 
     public static Peak[] findBestTag(List<Peak> peaks) {
         initMaxPrefix(peaks);
         Peak[] bestTag;
         bestTag = new Peak[]{};
         for (Peak peak : peaks) {
                 bestTag = findBestTag(peak, bestTag, 0, new Peak[500]);
             }
         return bestTag;
     }
 
     public static void initMaxPrefix(List<Peak> peaks) {
         for (Peak peak : peaks) {
             peak.setMaxPrefix(-1);
         }
     }
 
     public static Peak[] findBestTag(Peak peak, Peak[] best, int len, Peak[] prefix) {
         if (peak.getMaxPrefix() >= len) {
             return best;
         }
 
         prefix[len] = peak;
 
         if (len >= best.length) {
             best = new Peak[len +1];
             System.arraycopy(prefix, 0, best, 0, len + 1);
         }
 
         for (Peak next : peak.getNext()) {
             best = findBestTag(next, best, len + 1, prefix);
         }
 
         peak.setMaxPrefix(len);
 
         return best;
     }
 
     public static void generateGapEdges(Configuration conf, List<Peak> peaks, int allow) {
         int n = peaks.size();
         List<Double> masses = new ArrayList<Double>();
         Acid[] values = Acid.values();
         for (Acid acid : values) {
             masses.add(acid.getMass());
         }
         for (int i = 0; i < values.length; i++) {
             Acid a1 = values[i];
             for (int j = i + 1; j < values.length; j++) {
                 Acid a2 = values[j];
                 masses.add(a1.getMass() + a2.getMass());
                 if (allow > 2) {
                     for (int k = j + 1; k < values.length; k++) {
                         Acid a3 = values[k];
                         masses.add(a1.getMass() + a2.getMass() + a3.getMass());
                     }
                 }
             }
         }
         for (int i = 0; i < n; i++) {
             Peak peak = peaks.get(i);
             for (int j = i+1; j < n; j++) {
                 Peak next =  peaks.get(j);
                 double[] limits = conf.getEdgeLimits(peak, next);
                 for (double mass : masses) {
                     if (limits[0] < mass && limits[1] > mass) {
                         peak.addNext(next);
                         break;
                     }
                 }
             }
         }
     }
 
     public static Set<String> generateTags(Configuration conf, List<Peak> peaks) {
          Set<String> tags = new HashSet<String>();
          for (Peak peak : peaks) {
              generateTags(conf, tags, "", peak);
          }
          return tags;
      }
 
     public static void generateTags(Configuration conf, Set<String> tags, String prefix, Peak peak) {
         tags.add(prefix);
         for (Peak next : peak.getNext()) {
             for (Acid acid : Acid.values()) {
                 if (acid.match(conf.getEdgeLimits(peak, next))){
                     generateTags(conf, tags, prefix + acid.name(), next);
                 }
             }
         }
     }

    public static boolean tagStartsAtPos(double pos, String tag, List<Peak> peaks) {
        int i;
        for (i = 0; i < tag.length(); i++) {
            pos += Acid.getAcid(tag.charAt(i)).getMass();
            boolean found = false;
            for (Peak p2 : peaks) {
                if (Math.abs(p2.getValue() - pos) < EPSILON) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                break;
            }
        }
        return i == tag.length();
    }
 }
