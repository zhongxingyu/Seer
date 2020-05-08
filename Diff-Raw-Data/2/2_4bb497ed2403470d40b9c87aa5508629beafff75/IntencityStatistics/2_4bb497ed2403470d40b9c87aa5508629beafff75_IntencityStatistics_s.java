 package ru.spbau.bioinf.tagfinder;
 
 
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class IntencityStatistics {
     private Configuration conf;
 
 
     public IntencityStatistics(Configuration conf) {
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
         IntencityStatistics statistics = new IntencityStatistics(conf);
         Set<Integer> usedProteins = new HashSet<Integer>();
         for (int key : keys) {
             Scan scan = scans.get(key);
             int scanId = scan.getId();
             if (msAlignResults.containsKey(scanId)) {
                 Integer proteinId = msAlignResults.get(scanId);
                 if (usedProteins.contains(proteinId)) {
                     continue;
                 }
                 usedProteins.add(proteinId);
                 if (scanId == 1946) {
                     continue;
                 }
 
                 statistics.analyzeScan(scan, proteins.get(proteinId).getSimplifiedAcids(), proteinId);
 
 
             }
         }
     }
 
     public  void analyzeScan(Scan scan, String sequence, int proteinId) {
         String reverseSequence = ValidTags.getReverse(sequence);
         List<Peak> peaks = scan.getPeaks();
         GraphUtil.generateEdges(conf, peaks);
         double[] bad = new double[100];
         double[] good = new double[100];
         generateTags(peaks, bad, good, sequence, reverseSequence);
         System.out.print(scan.getId() + " " + proteinId);
         for (int i = 1; i < good.length; i++) {
             if (bad[i] + good[i] == 0) {
                 break;
             }
             int v = 0;
             if (good[i] == bad[i]) {
                 v = 1;
             }
             if (good[i] > bad[i]) {
                 v = 2;
             }
            System.out.print(" " + v);
 
         }
         System.out.println();
     }
 
     public void generateTags(List<Peak> peaks, double[] bad, double[] good, String sequence, String reverseSequence) {
          for (Peak peak : peaks) {
              generateTags("", peak, bad, good, sequence, reverseSequence, peak.getIntensity());
          }
      }
 
     public void generateTags(String prefix, Peak peak, double[] bad, double[] good, String sequence, String reverseSequence, double intencity) {
         int d = prefix.length();
         if (bad[d] < intencity || good[d] < intencity) {
             boolean isGood = sequence.contains(prefix) || reverseSequence.contains(prefix);
             if (isGood) {
                 good[d] = Math.max(good[d], intencity);
             } else {
                 bad[d] = Math.max(bad[d], intencity);
             }
         }
         for (Peak next : peak.getNext()) {
             for (Acid acid : Acid.values()) {
                 if (acid.match(conf.getEdgeLimits(peak, next))){
                     generateTags(prefix + acid.name(), next, bad, good, sequence, reverseSequence, Math.min(intencity, next.getIntensity()));
                 }
             }
         }
     }
 }
