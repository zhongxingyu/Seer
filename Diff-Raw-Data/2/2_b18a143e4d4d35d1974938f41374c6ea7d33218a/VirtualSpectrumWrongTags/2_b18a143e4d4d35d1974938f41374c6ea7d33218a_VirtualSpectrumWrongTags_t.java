 package ru.spbau.bioinf.tagfinder;
 
 import java.util.*;
 
 public class VirtualSpectrumWrongTags {
 
     private Configuration conf;
 
     public VirtualSpectrumWrongTags(Configuration conf) {
         this.conf = conf;
     }
 
     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration(args);
         List<Protein> proteins = conf.getProteins();
         Map<Integer, Integer> msAlignResults = conf.getMSAlignResults();
         Map<Integer, Scan> scans = conf.getScans();
         List<Integer> keys = new ArrayList<Integer>();
         keys.addAll(scans.keySet());
         Collections.sort(keys);
         VirtualSpectrumWrongTags vswt = new VirtualSpectrumWrongTags(conf);
         Map<Integer, List<Peak>> msAlignPeaks = conf.getMSAlignPeaks();
         for (int key : keys) {
             Scan scan = scans.get(key);
             int scanId = scan.getId();
             if (msAlignResults.containsKey(scanId)) {
                 Integer proteinId = msAlignResults.get(scanId);
                 String sequence = proteins.get(proteinId).getSimplifiedAcids();
                 List<Peak> peaks = msAlignPeaks.get(scanId);
                 GraphUtil.generateEdges(conf, peaks);
                 vswt.checkTags(peaks, sequence, scanId, proteinId);
             }
         }
     }
 
     private Set<String> used = new HashSet<String>();
 
     public void checkTags(List<Peak> peaks, String sequence, int scanId, int proteinId) {
         used.clear();
         for (Peak peak : peaks) {
             checkTags(peak, "", sequence, scanId, proteinId);
         }
     }
 
     public void checkTags(Peak peak, String prefix, String sequences, int scanId, int proteinId) {
         if (used.contains(prefix)) {
             return;
         }
 
         if (!check(prefix.toCharArray(), 0, "", sequences)) {
             System.out.println(scanId + " " + proteinId + " " + prefix + " " + peak.getValue());
         }
 
         used.add(prefix);
 
         for (Peak next : peak.getNext()) {
             for (Acid acid : Acid.values()) {
                 if (acid.match(conf.getEdgeLimits(peak, next))) {
                     checkTags(next, prefix + acid.name(), sequences, scanId, proteinId);
                 }
             }
         }
     }
 
     private char[] keys = new char[] {'K', 'K', 'Q', 'Q', 'W','W', 'W','W', 'R', 'R', 'N'};
     private String[] subst = new String[] {"AG", "GA", "AG", "GA", "AD", "DA", "EG", "GE", "SV", "VS", "GV", "VG", "GG"};
 
     private boolean check(char[] tag, int start, String prefix, String sequences) {
 
         if (start == tag.length) {
             return sequences.contains(prefix);
         }
 
         if (sequences.contains(prefix + new String(tag, start, tag.length - start)))
             return true;
 
         char old = tag[start];
 
         for (int i = 0; i < keys.length; i++) {
             char key = keys[i];
             if (key == old) {
                 if (check(tag, start + 1, prefix + subst[i], sequences)) {
                     return true;
                 }
             }
         }
         return false;
     }
 }
