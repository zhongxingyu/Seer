 package ru.spbau.bioinf.tagfinder;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class UnmatchedScansGenerator {
     public static final String TAGS_MODE = "mod2";
     public static final String SHARED_MODE = "mod3";
     private Configuration conf;
 
     public UnmatchedScansGenerator(Configuration conf) {
         this.conf = conf;
     }
 
     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration(args);
         new UnmatchedScansGenerator(conf).generateUnmatchedScans(args, conf, SHARED_MODE);
     }
 
     public void generateUnmatchedScans(String[] args, Configuration conf, String mod) throws Exception {
         Configuration conf2 = new Configuration(args, mod);
 
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
                 int proteinId = msAlignResults.get(scanId);
 
                 List<Peak> peaks = scan.createStandardSpectrum();
 
                 String sequence = proteins.get(proteinId).getSimplifiedAcids();
                 Set<Peak> removed = null;
                 if (TAGS_MODE.equals(mod)) {
                     removed = getRemovedPeaksByTags(peaks, sequence);
                 } if (SHARED_MODE.equals(mod)) {
                     removed = getRemovedPeaksByShared(peaks, sequence, scan.getPrecursorMass());
                 }
 
 
                 if (removed.size() > 0) {
                     System.out.println(scanId + " removed.size() = " + removed.size());
 
                     for (Peak peak : peaks) {
                         if (peak.getIntensity() == 0) {
                             removed.add(peak);
                         }
                     }
                     peaks.removeAll(removed);
                     Scan filtered = new Scan(scan, peaks);
                     filtered.save(conf2.getModifiedScansDir());
                 }
             }
         }
     }
 
     private Set<Peak> getRemovedPeaksByShared(List<Peak> peaks, String sequence, double precursorMass) {
         Set<Peak> removed = new HashSet<Peak>();
         double[] protein = ShiftEngine.getSpectrum(sequence);
         List<Double> shifts = ShiftEngine.getShifts(peaks, precursorMass, protein);
         double bestShift = 0;
         double bestScore = 0;
         double[] spectrum = ShiftEngine.getSpectrum(peaks, precursorMass);
         for (Double shift : shifts) {
             double nextScore = ShiftEngine.getScore(spectrum, protein, shift);
             if (nextScore > bestScore) {
                 bestScore = nextScore;
                 bestShift = shift;
             }
         }
        double[] mod = new double[]{0, -1, +1, Consts.WATER, -Consts.WATER, Consts.AMMONIA};
         for (Peak peak : peaks) {
             for (double dv : mod) {
                 double modMass = peak.getMass() + dv;
                 double v1 = modMass + bestShift;
                 double v2 = precursorMass - modMass + bestShift;
                 if (ShiftEngine.contains(protein, v1, v2)) {
                     removed.add(peak);
                     break;
                 }
             }
         }
         return removed;
     }
     private Set<Peak> getRemovedPeaksByTags(List<Peak> peaks, String sequence) {
         String reverseSequence = ValidTags.getReverse(sequence);
 
         GraphUtil.generateEdges(conf, peaks);
         Set<Peak> removed = new HashSet<Peak>();
         for (Peak p1 : peaks) {
             for (Peak p2 : p1.getNext()) {
                 for (Acid a1 : Acid.values()) {
                     if (a1.match(conf.getEdgeLimits(p1, p2))) {
                         for (Peak p3 : p2.getNext()) {
                             for (Acid a2 : Acid.values()) {
                                 if (a2.match(conf.getEdgeLimits(p2, p3))) {
                                     for (Peak p4 : p3.getNext()) {
                                         for (Acid a3 : Acid.values()) {
                                             if (a3.match(conf.getEdgeLimits(p3, p4))) {
                                                 String tag = a1.name() + a2.name() + a3.name();
                                                 if (sequence.contains(tag) || reverseSequence.contains(tag)) {
                                                     removed.add(p1);
                                                     removed.add(p2);
                                                     removed.add(p3);
                                                     removed.add(p4);
                                                 }
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
         return removed;
     }
 }
