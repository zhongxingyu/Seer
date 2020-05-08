 package ru.spbau.bioinf.tagfinder;
 
 import edu.ucsd.msalign.align.prsm.PrSM;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import ru.spbau.bioinf.tagfinder.stat.PlaceStatistics;
 
 public class EValueChecker {
 
     private static NumberFormat df = NumberFormat.getInstance();
 
     static {
         df.setGroupingUsed(false);
         df.setMaximumFractionDigits(2);
     }
 
     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration(args);
         List<Protein> proteins = conf.getProteins();
         Map<Integer, Integer> msAlignResults = conf.getMSAlignResults();
         Map<Integer, Scan> scans = conf.getScans();
         List<Integer> keys = new ArrayList<Integer>();
         keys.addAll(scans.keySet());
         Collections.sort(keys);
 
         Map<Integer, Double> evalues = conf.getEvalues();
 
         EValueAdapter.init(conf);
 
         for (int key : keys) {
             Scan scan = scans.get(key);
             int scanId = scan.getId();
             if (msAlignResults.containsKey(scanId)) {
                 Integer proteinId = msAlignResults.get(scanId);
                PrSM prsm = EValueAdapter.getBestEValue(scan, proteinId);
                 double newEvalue =  999999999999999999999d;
                if (prsm != null) {
                    newEvalue = prsm.getEValue();
                 }
                 Double oldEvalue = evalues.get(scanId);
                 System.out.println(scanId + " " + proteinId + " " + oldEvalue + " " + newEvalue + " " + (1 - (newEvalue / oldEvalue)));
             }
         }
     }
 
 }
