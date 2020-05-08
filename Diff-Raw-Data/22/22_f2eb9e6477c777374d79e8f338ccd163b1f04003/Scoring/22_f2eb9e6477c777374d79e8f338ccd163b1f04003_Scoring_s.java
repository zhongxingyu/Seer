 package ru.spbau.bioinf.pfind;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import ru.spbau.bioinf.tagfinder.Acid;
 import ru.spbau.bioinf.tagfinder.Consts;
 import ru.spbau.bioinf.tagfinder.Protein;
 import ru.spbau.bioinf.tagfinder.Scan;
 import ru.spbau.bioinf.tagfinder.ShiftEngine;
 
 public enum Scoring {
 
     YSCORE {
         public double getScore(Scan scan, Protein protein) {
             double[] peaks = scan.getMasses();
             double[] yEnds = protein.getYEnds();
             double v = 3 * ShiftEngine.getScore(peaks, yEnds, -Consts.WATER) + ShiftEngine.getScore(peaks, yEnds, 0);
             return v / Math.sqrt(peaks.length);
         }
     },
 
 
     BSCORE {
         public double getScore(Scan scan, Protein protein) {
             double[] p = scan.getMasses();
             double[] bEnds = protein.getBEnds();
             double v = ShiftEngine.getScore(p, bEnds, -Consts.WATER) + 3 * ShiftEngine.getScore(p, bEnds, 0);
             return v / Math.sqrt(p.length);
         }
     },
     BMSCORE {
         public double getScore(Scan scan, Protein protein) {
             double[] p = scan.getMasses();
             double[] bEnds = protein.getBEnds();
             double v = 3 * ShiftEngine.getScore(p, bEnds, -Acid.M.getMass()) + ShiftEngine.getScore(p, bEnds, -Acid.M.getMass() - Consts.AMMONIA);
             return v / Math.sqrt(p.length);
         }
     },
 
     BNSCORE {
         public double getScore(Scan scan, Protein protein) {
             double[] p = scan.getMasses();
             double[] bEnds = protein.getBEnds();
             double v = 3 * ShiftEngine.getScore(p, bEnds, -Consts.N);
             return v / Math.sqrt(p.length);
         }
     },
     
     RESEARCH_SCORE {
         @Override
         public double getScore(Scan scan, Protein protein) {
             double[] masses = scan.getMasses();
             double[] suffixes = protein.getYEnds();
             List<Double> diffs = new ArrayList<Double>();
             int pStart = 0;
             for (int i = 0; i < masses.length; i++) {
                 double mass = masses[i];
                 while(suffixes[pStart] + 200 <= mass) {
                     pStart++;
                     if (pStart == suffixes.length) {
                         break;
                     }
                 }
                 if (pStart == suffixes.length) {
                     break;
                 }
                 int cur = pStart;
                 while (suffixes[cur] - 200 < mass) {
                     diffs.add(mass - suffixes[cur]);
                     cur++;
                     if (cur == suffixes.length) {
                         break;
                     }
                 }
             }
             Collections.sort(diffs);
             int score = 0;
             for (int i = 0; i < diffs.size(); i++) {
                 int newScore = 0;
                 double v = diffs.get(i);
                 for (int j = i + 1; j < diffs.size(); j++) {
                     if (diffs.get(j) < v + 0.2) {
                         newScore++;
                     } else {
                         break;
                     }
                 }
                 if (newScore > score) {
                     score = newScore;
                 }
             }
             return score * 2/Math.sqrt(masses.length);
         }        
     }
     ;
 
     public abstract double getScore(Scan scan, Protein protein);
 }
