 package ru.spbau.bioinf.tagfinder;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class KDStatistics {
 
     private Configuration conf;
 
     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration(args);
         List<Protein> proteins = conf.getProteins();
         Map<Integer,Integer> msAlignResults = conf.getMSAlignResults();
         Map<Integer, Scan> scans = conf.getScans();
         KDStatistics kdStat = new KDStatistics(conf);
         Map<KD, Integer> stat = new HashMap<KD, Integer>();
         List<Integer> keys = new ArrayList<Integer>();
         keys.addAll(scans.keySet());
         Set<Integer> usedProteins = new HashSet<Integer>();
         Collections.sort(keys);
         for (int key : keys) {
             Scan scan = scans.get(key);
             int scanId = scan.getId();
             if (msAlignResults.containsKey(scanId)) {
                 Integer proteinId = msAlignResults.get(scanId);
                 if (usedProteins.contains(proteinId)) {
                     continue;
                 }
                 usedProteins.add(proteinId);
                 String sequence = proteins.get(proteinId).getSimplifiedAcids();
                 KD kd =
                         kdStat.
                                 findKd
                                 //findKdBetweenBYAll
                                 //findKdBetweenBY
                         (scan, sequence);
                 System.out.println(scanId + " " + kd.toString() + " " + proteinId);
                 if (stat.containsKey(kd)) {
                     stat.put(kd, 1 + stat.get(kd));
                 } else {
                     stat.put(kd, 1);
                 }
             }
         }
         List<KD> values  = new ArrayList<KD>();
         values.addAll(stat.keySet());
         Collections.sort(values);
         for (KD value : values) {
             System.out.println(value.toString() + " - " + stat.get(value));
         }
     }
 
     public KDStatistics(Configuration conf) {
         this.conf = conf;
     }
 
     public KD findKd(Scan scan, String sequence) {
         List<Peak> peaks = scan.getPeaks();
         peaks.add(new Peak(0, 0, 0));
         peaks.add(new Peak(scan.getPrecursorMass(), 0, 0));
         return findKd(peaks, sequence);
     }
 
     public KD findKdBetweenBY(Scan scan, String sequence) {
         List<Peak> peaks = new ArrayList<Peak>();
         peaks.addAll(scan.getPeaks());
         double precursorMassShift = PrecursorMassShiftFinder.getPrecursorMassShift(conf, scan);
         peaks.add(new Peak(0, 0, 0));
         double newPrecursorMass = scan.getPrecursorMass() + precursorMassShift;
         peaks.add(new Peak(newPrecursorMass, 0, 0));
         for (Peak peak : scan.getPeaks()) {
              peaks.add(peak.getYPeak(newPrecursorMass));
         }
         return findKd(peaks, sequence);
     }
 
     public KD findKdBetweenBYAll(Scan scan, String sequence) {
         List<Double> values = PrecursorMassShiftFinder.getAllPossibleShifts(conf, scan);
         values.add(0d);
         values.add(PrecursorMassShiftFinder.getPrecursorMassShift(conf, scan));
 
         Collections.sort(values);
         KD ans = new KD(0, 0);
         double prev = -1000;
         int tries = 0;
         for (double precursorMassShift : values) {
             if (precursorMassShift - prev < 0.0001) {
                 continue;
             }
             prev = precursorMassShift;
             tries++;
             List<Peak> peaks = new ArrayList<Peak>();
             peaks.addAll(scan.getPeaks());
             for (Peak peak : peaks) {
                peak.clearEdges();
             }
             peaks.add(new Peak(0, 0, 0));
             double newPrecursorMass = scan.getPrecursorMass() + precursorMassShift;
             peaks.add(new Peak(newPrecursorMass, 0, 0));
             for (Peak peak : scan.getPeaks()) {
                  peaks.add(peak.getYPeak(newPrecursorMass));
             }
             KD newKD = findKd(peaks, sequence);
             if (newKD.compareTo(ans) < 0) {
                 ans = newKD;
             }
         }
         //System.out.println("tries = " + tries + " " + values.size());
         return ans;
     }
 
     private KD findKd(List<Peak> peaks, String sequence) {
         Collections.sort(peaks);
 
         int n = peaks.size();
         for (int i = 0; i < n; i++) {
             peaks.get(i).setComponentId(i);
         }
 
         generateEdges(peaks);
 
         boolean done;
 
         do {
             done = true;
             for (Peak peak : peaks) {
                 if (peak.updateComponentId()) {
                     done = false;
                 }
             }
         } while (!done);
 
         int[] kValues = new int[n];
         for (Peak peak : peaks) {
             peak.setMaxPrefix(-1);
         }
 
         for (Peak peak : peaks) {
             searchK(kValues, 0, peak, new Peak[500] );
         }
 
         int k = 0;
 
         for (int kValue : kValues) {
             if (kValue > k) {
                 k = kValue;
             }
         }
 
         int d = 0;
 
         String sequenceReversed = new StringBuffer(sequence).reverse().toString();
 
         for (int i = 0; i < n; i++) {
             Peak peak =  peaks.get(i);
             if (kValues[peak.getComponentId()] == k) {
                 int nextD = getD(peak, sequence);
                 if (nextD > d) {
                     d = nextD;
                 }
                 nextD = getD(peak, sequenceReversed);
                 if (nextD > d) {
                     d = nextD;
                 }
             }
         }
 
         return new KD(k, d);
     }
 
     public void generateEdges(List<Peak> peaks) {
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
 
     private void searchK(int[] kValues, int len, Peak peak, Peak[] prefix) {
         if (peak.getMaxPrefix() >= len) {
             return;
         }
 
         prefix[len] = peak;
 
         int componentId = peak.getComponentId();
         if (kValues[componentId] < len) {
             kValues[componentId] = len;
 
             //printPrefix(prefix, len);
 
         }
 
         for (Peak next : peak.getNext()) {
             searchK(kValues, len + 1, next, prefix);
         }
 
         peak.setMaxPrefix(len);
     }
 
     private void printPrefix(Peak[] prefix, int len) {
         if (len < 25) {
             return;
         }
         System.out.print(len + " ");
         for (int i = 0; i < len; i++) {
             Peak p = prefix[i];
             System.out.print(p.getValue() + " ");
             System.out.print(Acid.getAcid(prefix[i + 1].diff(p)).name() + " ");
         }
         System.out.println(prefix[len].getValue());
     }
 
 
     private int maxD;
 
     private int getD(Peak peak, String sequence) {
         int ans = 0;
         Peak[] prefix = new Peak[500];
         maxD = 0;
         for (Peak next : peak.getNext()) {
             double[] limits = conf.getEdgeLimits(peak, next);
             for (Acid acid : Acid.values()) {
                 if (acid.match(limits)) {
                     String pst = acid.name();
                     int cur = sequence.indexOf(pst);
                     while (cur >=0) {
                         prefix[0] = peak;
                         int nextAns = getD(next, sequence.substring(cur+1), 1, prefix);
                         if (nextAns > ans) {
                             ans = nextAns;
                         }
                         cur = sequence.indexOf(pst, cur + 1);
                     }
                 }
             }
         }
         return ans;
     }
 
     private int getD(Peak peak, String sequence, int matched, Peak[] prefix) {
         prefix[matched] = peak;
         if (matched > maxD) {
             //printPrefix(prefix, matched);
         }
         if (sequence.length() == 0) {
             return matched;
         }
         int ans = matched;
         for (Peak next : peak.getNext()) {
             double[] limits = conf.getEdgeLimits(peak, next);
             Acid acid = Acid.getAcid(sequence.charAt(0));
             if (acid.match(limits)) {
                 int nextAns = getD(next, sequence.substring(1), matched + 1, prefix);
                 if (nextAns > ans) {
                     ans = nextAns;
                 }
             }
         }
 
         return ans;
     }
 }
