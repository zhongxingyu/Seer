 package ru.spbau.bioinf.tagfinder;
 
 import java.util.Collections;
 import ru.spbau.bioinf.tagfinder.util.StatisticsUtil;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 public class PrecursorMassShiftFinder {
 
     public static void main(String[] args) throws IOException {
         Configuration conf = new Configuration(args);
         Map<Integer, Scan> scans = conf.getScans();
         int none = 0;
         for (Scan scan : scans.values()) {
             double v = getPrecursorMassShift(conf, scan);
             if (v == 0) {
                 none++;
             }
         }
         System.out.println("none = " + none);
     }
 
     public static List<Double> getAllPossibleShifts(Configuration conf, Scan scan) {
         List<Peak> peaks = scan.getPeaks();
         double precursorMass = scan.getPrecursorMass();
         int n = peaks.size();
         List<Double> values = new ArrayList<Double>();
         for (int i = 0; i < n; i++) {
             Peak peak = peaks.get(i);
             for (int j = i+1; j < n; j++) {
                 Peak next =  peaks.get(j);
                 double sum = peak.getValue() + next.getValue();
                 double delta = sum - precursorMass;
                 double absDelta = Math.abs(delta);
                 for (Acid acid : Acid.values()) {
                     double error = conf.getPpmCoef() * 3 * sum;
                     double errorU = conf.getPpmCoef() * sum/2;
                     if (acid.match(new double[]{absDelta - error,  absDelta + error})) {
                         if (acid.match(new double[]{absDelta - errorU,  absDelta + errorU})) {
                             continue;
                         }
                         if (delta > 0) {
                             values.add(delta - acid.getMass());
                         } else {
                             values.add(delta + acid.getMass());
                         }
                     }
                 }
             }
         }
         return values;
     }
 
     public static List<Double> getAllPossibleShifts2(Configuration conf, Scan scan) {
         List<Peak> peaks = scan.getPeaks();
         double precursorMass = scan.getPrecursorMass();
         double error = precursorMass * conf.getPpmCoef() * 3;
         int n = peaks.size();
         List<Double> values = new ArrayList<Double>();
         for (int i = 0; i < n; i++) {
             Peak peak = peaks.get(i);
             for (int j = i+1; j < n; j++) {
                 Peak next =  peaks.get(j);
                 double sum = peak.getValue() + next.getValue();
                 double delta = sum - precursorMass;
                 double absDelta = Math.abs(delta);
                 for (Acid acid : Acid.values()) {
                     if (acid.match(new double[]{absDelta - error,  absDelta + error})) {
                         if (delta > 0) {
                             values.add(delta - acid.getMass());
                         } else {
                             values.add(delta + acid.getMass());
                         }
                     }
                 }
             }
         }
         return values;
     }
 
     public static double getPrecursorMassShiftForMoreEdges(Configuration conf, Scan scan) {
         List<Peak> peaks = scan.getPeaks();
         double precursorMass = scan.getPrecursorMass();
         double error = precursorMass * conf.getPpmCoef() * 3;
         int n = peaks.size();
         List<double[]> limits = new ArrayList<double[]>();
         List<Double> points = new ArrayList<Double>();
         for (int i = 0; i < n; i++) {
             Peak peak = peaks.get(i);
             for (int j = i+1; j < n; j++) {
                 Peak next =  peaks.get(j);
                 double sum = peak.getValue() + next.getValue();
                 double dError = sum * conf.getPpmCoef() / 2;
                 double delta = sum - precursorMass;
                 double absDelta = Math.abs(delta);
                 for (Acid acid : Acid.values()) {
                     if (acid.match(new double[]{absDelta - error,  absDelta + error})) {
                         double opt = delta > 0 ? delta - acid.getMass() : delta + acid.getMass();
                        points.add(opt - dError);
                        points.add(opt + dError);
                        limits.add(new double[] {opt - dError, opt + dError});
                     }
                 }
             }
         }
         Collections.sort(points);
         double ans = 0;
         int score = 0000;
        points.clear();
        double min = - precursorMass * conf.getPpmCoef() * 3;
        double delta = -min/100000;
        for (int i = 0; i < 200000; i++) {
            double p = min + delta * i;
        //for (int i = 0; i < points.size() - 1; i++) {
        //    double p = (points.get(i) + points.get(i + 1)) / 2;
             if (Math.abs(p) > conf.getPpmCoef() * 3 * precursorMass) {
                 continue;
             }
             int nextScore = 0;
             for (double[] limit : limits) {
                 if (limit[0] < p && p < limit[1]) {
                     nextScore++;
                 }
             }
             if (nextScore > 0) {
                 if (nextScore > score || (nextScore == score && (Math.abs(ans) > Math.abs(p)))) {
                     ans = p;
                     score = nextScore;
                 }
             }
         }
         return ans;
     }
 
     public static double getPrecursorMassShift(Configuration conf, Scan scan) {
         List<Peak> peaks = scan.getPeaks();
         double precursorMass = scan.getPrecursorMass();
         int n = peaks.size();
         List<Double> values = new ArrayList<Double>();
         for (int i = 0; i < n; i++) {
             Peak peak = peaks.get(i);
             for (int j = i+1; j < n; j++) {
                 Peak next =  peaks.get(j);
                 double sum = peak.getValue() + next.getValue();
                 double delta = sum - precursorMass;
                 double error = conf.getPpmCoef() * 3 * sum / 2;
                 if (-error < delta && error > delta) {
                     values.add(delta);
                 }
             }
         }
         //int scanId = scan.getId();
         /*
         if (scanId == 591 ||scanId == 1562 || scanId == 583 || scanId == 2875 || scanId == 576 || scanId == 1102 ) {
             for (double value : values) {
                 System.out.println(value);
             }
         }
         */
 
         double delta = StatisticsUtil.getAverage(values);
         if (delta != 0) {
             //System.out.println(scanId + " " + delta + " " + " " + precursorMass + " " + values.size());
         }
         return delta;
     }
 }
