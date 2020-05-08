 package ru.spbau.bioinf.tagfinder;
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import ru.spbau.bioinf.tagfinder.util.ReaderUtil;
 
 public class ValidTags {
 
     public static final String VIRTUAL = "virtual";
     public static final String STANDARD = "standard";
    public static final String REFLEX = "reverse";
 
     private Configuration conf;
 
     private int gap;
     private static Map<Integer,List<Peak>> msAlignPeaks;
 
     public ValidTags(Configuration conf) {
         this.conf = conf;
     }
 
     private static NumberFormat df = NumberFormat.getInstance();
     static {
         df.setMaximumFractionDigits(2);
     }
 
     private boolean needReverseTag = false;
     private boolean monoTagsOnly = false;
     private boolean addOnes = false;
     private int maxTagLength = 90;
 
     public void setMonoTagsOnly(boolean monoTagsOnly) {
         this.monoTagsOnly = monoTagsOnly;
     }
 
     public void setAddOnes(boolean addOnes) {
         this.addOnes = addOnes;
     }
 
     public void setNeedReverseTag(boolean needReverseTag) {
         this.needReverseTag = needReverseTag;
     }
 
     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration(args);
         ValidTags validTags = new ValidTags(conf);
        String type = STANDARD;
        //validTags.process(type, 1);
        //validTags.process(type, 2);
         validTags.process(type, 3, 10);
     }
 
     private PrintWriter output;
 
     private void process(String type, int gap) throws Exception {
         process(type, gap, 100);
     }
 
     private void process(String type, int gap, int maxTagLength) throws Exception {
         this.gap = gap;
         this.maxTagLength = maxTagLength;
         List<Protein> proteins = conf.getProteins();
         Map<Integer,Integer> msAlignResults = conf.getMSAlignResults();
         Map<Integer, Scan> scans = conf.getScans();
         List<Integer> keys = new ArrayList<Integer>();
         keys.addAll(scans.keySet());
         Collections.sort(keys);
         Set<Integer> usedProteins = new HashSet<Integer>();
         msAlignPeaks = conf.getMSAlignPeaks();
 
         double[] global = new double[100];
         int[] count = new int[100];
 
 
         setNeedReverseTag(STANDARD.equals(type));
         System.out.println("type = " + type);
         System.out.println("gap = " + this.gap);
         System.out.println("monoTagsOnly = " + monoTagsOnly);
         System.out.println("addOnes = " + addOnes);
         System.out.println("needReverseTag = " + needReverseTag);
 
         String fileName = type + "_" + gap;
         if (monoTagsOnly) {
             fileName += "_mono";
         }
         if (addOnes) {
             fileName += "_add";
         }
         output = ReaderUtil.createOutputFile(new File("vt", fileName + ".txt"));
 
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
 
 
                 Protein protein = proteins.get(proteinId);
                 long[][] stat = process(scan, protein, type, gap);
                 for (int i = 1; i < stat.length; i++) {
                     long good = stat[i][0];
                     long total = good + stat[i][1];
                     if (total > 0) {
                         global[i] += (100d * good)/total;
                         count[i]++;
                     } else {
                         break;
                     }
                 }
 
                 printStat(stat, proteinId, scanId);
 
                 //break;
             }
         }
         output.close();
         for (int i = 1; i < count.length; i++) {
             int n = count[i];
             if (n > 0) {
                 System.out.print( df.format(global[i]/n) + " ");
             } else {
                 break;
             }
         }
         System.out.println();
     }
 
     public long[][] process(Scan scan, Protein protein, String type, int gap) {
         this.gap = gap;
         String sequence = protein.getSimplifiedAcids();
         List<Peak> peaks = null;
 
         if (VIRTUAL.equals(type)) {
             peaks = msAlignPeaks.get(scan.getId());
         } else if (STANDARD.equals(type)) {
             peaks = scan.createStandardSpectrum();
         } else if (REFLEX.equals(type)) {
            scan.createSpectrumWithYPeaks(PrecursorMassShiftFinder.getPrecursorMassShift(conf, scan));
         } else {
             throw new RuntimeException("Wrong spectrum type " +  type);
         }
 
         if (addOnes) {
             peaks = addOnes(peaks);
         }
 
         GraphUtil.generateGapEdges(conf, peaks, gap);
         if (monoTagsOnly) {
                 filterMonotags(peaks);
         }
 
         return printGappedTagInfo(peaks, sequence, getReverse(sequence));
 
     }
 
     private List<Peak> addOnes(List<Peak> peaks) {
         List<Peak> ans = new ArrayList<Peak>();
         for (Peak peak : peaks) {
             ans.add(new Peak(peak.getValue() - 1, 0 ,0));
             ans.add(new Peak(peak.getValue(), 0 ,0));
             ans.add(new Peak(peak.getValue() + 1, 0 ,0));
         }
         Collections.sort(ans);
         return ans;
     }
 
     private static void filterMonotags(List<Peak> peaks) {
         for (Peak peak : peaks) {
             for (Iterator<Peak> iterator = peak.getNext().iterator(); iterator.hasNext(); ) {
                 Peak next = iterator.next();
                 if (next.getPeakType() != peak.getPeakType()) {
                     iterator.remove();
                 }
             }
         }
     }
 
     private long[][] printGappedTagInfo(List<Peak> peaks, String sequence, String reverseSequence) {
         long[][] stat = new long[1000][2];
         for (Peak peak : peaks) {
             Set<Integer> starts = new HashSet<Integer>();
             Set<Integer> reverseStarts = new HashSet<Integer>();
             for (int i = 0; i < sequence.length(); i++) {
                 starts.add(i);
                 if (needReverseTag) {
                     reverseStarts.add(i);
                 }
             }
             processGappedTags(stat, peak, 0, sequence, reverseSequence, starts, reverseStarts);
         }
         return stat;
     }
 
     private void processGappedTags(long[][] stat, Peak peak, int prefix, String sequence, String reverseSequence, Set<Integer> starts, Set<Integer>reverseStarts) {
         if (prefix > 0) {
             stat[prefix][0]++;
         }
         if (prefix > maxTagLength) {
             return;
         }
 
         List<Peak> nextPeaks = peak.getNext();
         for (Peak next : nextPeaks) {
             double[] limits = conf.getEdgeLimits(peak, next);
             Set<Integer> nextStarts = getNextStarts(sequence, starts, limits, gap);
             Set<Integer> nextReverseStarts = getNextStarts(reverseSequence, reverseStarts, limits, gap);
             if (nextStarts.size() + nextReverseStarts.size() == 0) {
                 processWrongGappedTags(stat, next, prefix + 1);
             } else {
                 processGappedTags(stat, next, prefix + 1, sequence, reverseSequence, nextStarts, nextReverseStarts);
             }
         }
     }
 
     private void processWrongGappedTags(long[][] stat, Peak peak, int prefix) {
         List<Peak> nextPeaks = peak.getNext();
         stat[prefix][1]++;
         long[] statOrig = new long[stat.length];
         for (int i = 0; i < statOrig.length; i++) {
             statOrig[i] = stat[i][1];
         }
 
         if (prefix > maxTagLength) {
             return;
         }
         for (Peak next : nextPeaks) {
             processWrongGappedTags(stat, next, prefix + 1);
         }
     }
 
     public static Set<Integer> getNextStarts(String sequence, Set<Integer> starts, double[] limits, int gap) {
         Set<Integer> nextStarts = new HashSet<Integer>();
         for (int pos : starts) {
             if (pos >= sequence.length()) {
                 continue;
             }
             double m = Acid.getAcid(sequence.charAt(pos)).getMass();
             if (limits[0] < m && m < limits[1]) {
                 nextStarts.add(pos + 1);
             }
             if (gap > 1) {
                 if (pos  + 1 < sequence.length()) {
                     m += Acid.getAcid(sequence.charAt(pos + 1)).getMass();
                     if (limits[0] < m && m < limits[1]) {
                         nextStarts.add(pos + 2);
                     }
                 }
                 if (gap > 2) {
                     if (pos  + 2 < sequence.length()) {
                         m += Acid.getAcid(sequence.charAt(pos + 2)).getMass();
                         if (limits[0] < m && m < limits[1]) {
                             nextStarts.add(pos + 3);
                         }
                     }
                 }
             }
 
         }
         return nextStarts;
     }
 
     private void printStat(long[][] stat, Integer proteinId, int scanId) {
         output.print(scanId + " " +  proteinId);
         for (int i = 1; i < stat.length; i++) {
             long good = stat[i][0];
             long total = good + stat[i][1];
             if (total > 0) {
                 output.print(" " + df.format((100d * good)/total) + " " + stat[i][0] + " " + stat[i][1] + " ");
             } else {
                 break;
             }
         }
         output.println();
     }
 
     public static String getReverse(String tag) {
         return new StringBuilder(tag).reverse().toString();
     }
 }
