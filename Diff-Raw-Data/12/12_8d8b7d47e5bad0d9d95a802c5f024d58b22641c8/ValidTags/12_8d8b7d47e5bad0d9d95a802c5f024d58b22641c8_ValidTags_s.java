 package ru.spbau.bioinf.tagfinder;
 
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class ValidTags {
 
     private Configuration conf;
 
     public ValidTags(Configuration conf) {
         this.conf = conf;
     }
 
     private static NumberFormat df = NumberFormat.getInstance();
     static {
         df.setMaximumFractionDigits(2);
     }
 
     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration(args);
         List<Protein> proteins = conf.getProteins();
         Map<Integer,Integer> msAlignResults = conf.getMSAlignResults();
         Map<Integer, Scan> scans = conf.getScans();
         List<Integer> keys = new ArrayList<Integer>();
         keys.addAll(scans.keySet());
         Collections.sort(keys);
         for (int key : keys) {
             Scan scan = scans.get(key);
             int scanId = scan.getId();
             if (msAlignResults.containsKey(scanId)) {
                 Integer proteinId = msAlignResults.get(scanId);
                 String sequence = proteins.get(proteinId).getSimplifiedAcids();
                 String reverseSequence = getReverse(sequence);
                 KDStatistics kdStatistics = new KDStatistics(conf);
                 Set<String> tags = new HashSet<String>();
                 ValidTags validTags = new ValidTags(conf);
 
 
                 List<Peak> peaks =
                            //scan.getStandardSpectrum()
                            scan.createSpectrumWithYPeaks(PrecursorMassShiftFinder.getPrecursorMassShift(conf, scan))
                     ;
 
                 kdStatistics.generateEdges(peaks);
                 validTags.generateTags(tags, peaks);
 
                 Set<String> filteredTags = new HashSet<String>();
                 for (String tag : tags) {
                     String reverseTag = getReverse(tag);
                     if (!filteredTags.contains(reverseTag) || reverseTag.equals(tag)) {
                         filteredTags.add(tag);
                     }
                 }
                 int[][] stat = new int[1000][2];
                 int max = 0;
                 for (String tag : filteredTags) {
                     int len = tag.length();
                     if (len > max) {
                         max = len;
                     }
                     if (sequence.contains(tag) || reverseSequence.contains(tag)) {
                         stat[len][0]++;
                     } else {
                         stat[len][1]++;
                     }
                 }
                 System.out.print(scanId + " " +  proteinId);
                 for (int i = 1; i <= max; i++) {
                     int good = stat[i][0];
                     int total = good + stat[i][1];
                     if (total > 0) {
                         System.out.print(" " + df.format((100d * good)/total));
                     }
                 }
                 System.out.println();
             }
         }
     }
 
     private static String getReverse(String tag) {
         return new StringBuilder(tag).reverse().toString();
     }
 
 
     public void generateTags(Set<String> tags, List<Peak> peaks) {
          for (Peak peak : peaks) {
              generateTags(tags, "", peak);
          }
      }
 
     public void generateTags(Set<String> tags, String prefix, Peak peak) {
         tags.add(prefix);
         for (Peak next : peak.getNext()) {
             for (Acid acid : Acid.values()) {
                 if (acid.match(conf.getEdgeLimits(peak, next))){
                     generateTags(tags, prefix + acid.name(), next);
                 }
             }
         }
     }
 
 
 }
