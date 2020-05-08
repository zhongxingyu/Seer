 package ru.spbau.bioinf.tagfinder;
 
 import edu.ucsd.msalign.align.idevalue.IdEValue;
 import edu.ucsd.msalign.align.prsm.PrSM;
 import edu.ucsd.msalign.seq.MsAlignSeq;
 import java.util.Map;
 import java.util.Properties;
 
 public class EValueAdapter {
 
     private static IdEValue eValueCalculator;
 
     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration(args);
         init(conf);
         Map<Integer,Scan> scans = conf.getScans();
         conf.getMSAlignResults();
         for (int i = 0; i < input.length; i += 2) {
             Scan scan = scans.get(input[i]);
             int proteinId = input[i + 1];
             System.out.println("Processing scan " + scan.getName() + " protein " + proteinId);
             output(calculateEValue(scan, proteinId));
         }
     }
 
     public static void init(Configuration conf) throws Exception {
         final Properties properties = new Properties();
         properties.setProperty("databaseFileName", conf.getProteinDatabaseFile().getCanonicalPath());
         properties.setProperty("useDecoyDb", "false");
         properties.setProperty("activation", "CID");
         properties.setProperty("cysteineProtection", "C57");
         properties.setProperty("reportNumber", "1");
         properties.setProperty("shiftNumber", "2");
         properties.setProperty("errorTolerance", "15");
         properties.setProperty("eValueThreshold", "0.01");
         eValueCalculator = new IdEValue();
         eValueCalculator.init(properties);
     }
 
     public static synchronized PrSM[][][] calculateEValue(Scan scan, int proteinId) throws Exception {
         PrSM[][][] prsms;
         synchronized (eValueCalculator) {
             MsAlignSeq msAlignSeq = eValueCalculator.getSeqs()[proteinId];
             prsms = eValueCalculator.getPrsms(scan.getMsDeconvPeaks(), msAlignSeq);
         }
         return prsms;
     }
 
     private static void output(PrSM prsms[][][]) {
         if (prsms != null) {
             for (int i = 0; i < prsms.length; i++) {
                 if (prsms[i] != null) {
                     for (int j = 0; j < 4; j++) {
                         if (prsms[i][j] != null) {
                             for (int k = 0; k < prsms[i][j].length; k++) {
                                 if (prsms[i][j][k] != null) {
                                     PrSM prsm = prsms[i][j][k];
                                     System.out.println("Shift " + i + " Alignment type " + j + " score " + prsm.getUniqueScr() + " evalue " + prsm.getEValue());
                                 }
                             }
                         }
                     }
                 }
             }
         }
     }
 
     public static PrSM getBestEValue(Scan scan, int proteinId) throws Exception  {
         PrSM prsms[][][] = calculateEValue(scan, proteinId);
         double best = 9E100;
         PrSM ans = null;
         if (prsms != null) {
             for (int i = 0; i < prsms.length; i++) {
                 if (prsms[i] != null) {
                     for (int j = 0; j < 4; j++) {
                         if (prsms[i][j] != null) {
                             for (int k = 0; k < prsms[i][j].length; k++) {
                                 if (prsms[i][j][k] != null) {
                                     PrSM prsm = prsms[i][j][k];
                                     double newValue = prsm.getEValue();
                                     if (newValue < best) {
                                         best = newValue;
                                         ans = prsm;
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
         return ans;
     }
 
     private static int[] input = new int[] {
 1082,  4375,
 1082, 392,
 1082, 1007,
 1082, 1573,
 1082, 2508,
 1082, 3392,
 1082, 3874,
 1082, 3949,
 
 1083, 4149,
 1083, 3949,
 
 1242, 1100,
 1242, 3302,
 1242, 3393,
 
 1251, 1100,
 1251, 1618,
 1251, 2013,
 1251, 3302,
 
 1252, 3302,
 
 1274, 3302,
 1274, 471,
 1274, 4098,
 
 1322, 3091,
 1322, 1453,
 
 1323, 2002,
 1323, 1453,
 
 1326, 152,
 1326, 1453,
 
 1375, 3307,
 1375, 381,
 1375, 1277,
 
 1378, 2272,
 1378, 2115,
 1378, 3734,
 1378, 4098,
 
 1711, 3311,
 2167, 4368,
 2167, 492
     };
 }
