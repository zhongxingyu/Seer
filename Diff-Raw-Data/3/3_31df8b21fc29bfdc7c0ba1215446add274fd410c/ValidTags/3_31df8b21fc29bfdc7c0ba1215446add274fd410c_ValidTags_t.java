 package ru.spbau.bioinf.tagfinder;
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import ru.spbau.bioinf.tagfinder.util.ReaderUtil;
 
 public class ValidTags {
 
     public static final double TEN_PPM = 0.00001d;
 
     public static final String INPUT_EXP = "exp";
     public static final String INPUT_VIRT = "virt";
     public static final String TARGET_BASE = "base";
     public static final String TARGET_ANNOTATED = "annotated";
     public static final String MATCH_CORRECT = "correct";
     public static final String MATCH_PROPER = "proper";
 
     public static final String BY_NONE = "none";
     public static final String BY_ZERO = "zero";
     public static final String BY_MORE = "more";
 
     public static final int FULL = 1;
     public static final int BAR = 0;
 
     private Configuration conf;
 
     private int gap;
     private String inputType;
     private String targetType;
     private String monoType;
     private String matchType;
     private static Map<Integer, List<Peak>> msAlignPeaks;
     private Map<KD, Integer> kdStat;
     private int k;
     private int d;
     private Map<Integer, double[]> annotatedSpectrums;
 
     public static String parentDir = "res";
 
     public ValidTags(Configuration conf) throws Exception {
         this.conf = conf;
         try {
             msAlignPeaks = conf.getMSAlignPeaks(conf.getScans());
         } catch (Throwable e) {
             e.printStackTrace();
         }
         annotatedSpectrums = conf.getAnnotatedSpectrums();
     }
 
     public static DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance();
 
     static {
         df.setMaximumFractionDigits(2);
         DecimalFormatSymbols dfs = new DecimalFormatSymbols();
         dfs.setDecimalSeparator('.');
         df.setDecimalFormatSymbols(dfs);
     }
 
     private boolean addOnes = false;
     private boolean needIntencity = false;
 
 
     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration(args);
         ValidTags validTags = new ValidTags(conf);
 
        validTags.process(INPUT_EXP, TARGET_BASE, MATCH_CORRECT, BY_NONE, FULL, false, false);
        validTags.process(INPUT_EXP, TARGET_BASE, MATCH_CORRECT, BY_NONE, BAR, false, false);

         validTags.process(INPUT_EXP, TARGET_ANNOTATED, MATCH_PROPER, BY_NONE, BAR, false, true);
         validTags.process(INPUT_EXP, TARGET_ANNOTATED, MATCH_CORRECT, BY_NONE, BAR, false, true);
 
 
         validTags.process(INPUT_EXP, TARGET_BASE, MATCH_PROPER, BY_NONE, FULL, false, false);
         validTags.process(INPUT_EXP, TARGET_BASE, MATCH_PROPER, BY_NONE, BAR, false, false);
 
         validTags.process(INPUT_EXP, TARGET_ANNOTATED, MATCH_CORRECT, BY_NONE, BAR, false, false);
         validTags.process(INPUT_VIRT, TARGET_ANNOTATED, MATCH_CORRECT, BY_ZERO, BAR, false, false);
 
         validTags.process(INPUT_EXP, TARGET_ANNOTATED, MATCH_PROPER, BY_NONE, BAR, false, false);
         validTags.process(INPUT_VIRT, TARGET_ANNOTATED, MATCH_PROPER, BY_ZERO, BAR, false, false);
 
         validTags.process(INPUT_EXP, TARGET_ANNOTATED, MATCH_CORRECT, BY_NONE, BAR, true, false);
         validTags.process(INPUT_EXP, TARGET_ANNOTATED, MATCH_PROPER, BY_NONE, BAR, true, false);
 
         validTags.process(INPUT_EXP, TARGET_ANNOTATED, MATCH_CORRECT, BY_MORE, BAR, false, false);
         validTags.process(INPUT_EXP, TARGET_ANNOTATED, MATCH_PROPER, BY_MORE, BAR, false, false);
 
         validTags.process(INPUT_VIRT, TARGET_ANNOTATED, MATCH_CORRECT, BY_NONE, BAR, false, false);
         validTags.process(INPUT_VIRT, TARGET_ANNOTATED, MATCH_CORRECT, BY_ZERO, BAR, false, false);
 
         validTags.process(INPUT_VIRT, TARGET_ANNOTATED, MATCH_CORRECT, BY_NONE, BAR, false, false);
     }
 
 
     private PrintWriter output;
     private PrintWriter outputKD;
     private PrintWriter outputIntencity;
 
 
     private void process(String inputType, String targetType, String matchType, String monoType, int datasetType, boolean addOnes, boolean needIntencity) throws Exception {
         for (int gap = 1; gap < 4; gap++) {
             process(inputType, targetType, matchType, monoType, datasetType, gap, addOnes, needIntencity);
         }
     }
 
     private double intencityLevel;
 
     public void process(String inputType, String targetType, String matchType, String monoType, int datasetType, int gap, boolean addOnes, boolean needIntencity) throws Exception {
         this.gap = gap;
         this.addOnes = addOnes;
         this.inputType = inputType;
         this.targetType = targetType;
         this.matchType = matchType;
         this.monoType = monoType;
         this.needIntencity = needIntencity;
         List<Protein> proteins = conf.getProteins();
         Map<Integer, Integer> msAlignResults = conf.getMSAlignResults();
         Map<Integer, Scan> scans = conf.getScans();
         List<Integer> keys = new ArrayList<Integer>();
         keys.addAll(scans.keySet());
         Collections.sort(keys);
         Set<Integer> usedProteins = new HashSet<Integer>();
 
         double[] global = new double[100];
         int[] count = new int[100];
 
         String fileName = datasetType == FULL ? "full" : "bar";
 
         fileName += "_" + inputType + "_" + targetType + "_" + matchType + "_" + monoType;
         if (addOnes) {
             fileName += "_add";
         }
         if (needIntencity) {
             fileName += "_intencity";
         }
         fileName += "_" + gap;
 
         output = ReaderUtil.createOutputFile(new File(parentDir, "share_" + fileName + ".txt"));
         outputKD = ReaderUtil.createOutputFile(new File(parentDir, "kd_" + fileName + ".txt"));
         //outputIntencity = ReaderUtil.createOutputFile(new File("res", "intencity_" + fileName + ".txt"));
 
         System.out.println("%fileName = " + fileName);
         kdStat = new HashMap<KD, Integer>();
 
         //keys.clear(); keys.add(694);
         for (int key : keys) {
             Scan scan = scans.get(key);
             int scanId = scan.getId();
 
             if (msAlignResults.containsKey(scanId)) {
                 Integer proteinId = msAlignResults.get(scanId);
                 if (usedProteins.contains(proteinId) && datasetType == BAR) {
                     continue;
                 }
                 double[] proteinSpectrum = TARGET_ANNOTATED.equals(targetType) ? annotatedSpectrums.get(scanId) : ShiftEngine.getSpectrum(proteins.get(proteinId).getSimplifiedAcids());
                 long[][] stat = new long[1000][2];
                 if (needIntencity) {
                     boolean[] done = new boolean[stat.length];
                     List<Double> intencities = new ArrayList<Double>();
                     for (Peak p : scan.getPeaks()){
                         intencities.add(p.getIntensity());
                     }
                     Collections.sort(intencities);
                     for (int i =  intencities.size() - 1; i>=0; i--) {
                         intencityLevel =  intencities.get(i);
                         long[][] curStat = process(scan, proteinSpectrum, proteinId);
                         for (int len = 1; len < stat.length; len++) {
                             if (!done[len]) {
                                 if (curStat[len][0] + curStat[len][1] > 0) {
                                     /*
                                     System.out.println(len + " " + intencityLevel + " " +  curStat[len][0] + " " + curStat[len][1] );
                                     for (Peak p : scan.getPeaks()){
                                         if (p.getIntensity() >= intencityLevel) {
                                             if (p.getIntensity() == intencityLevel) {
                                                 System.out.println("*");
                                             }
                                             System.out.println(p.getMass());
                                             
                                         }
                                     }
                                     */
                                     done[len] = true;
                                     stat[len] = curStat[len];
                                 }
                             }
                         }
                     }
                 } else {
                     stat = process(scan, proteinSpectrum, proteinId);
                 }
 
 
                 for (int i = 1; i < stat.length; i++) {
                     long good = stat[i][0];
                     long total = good + stat[i][1];
                     if (total != 0) {
                         global[i] += (100d * good) / total;
                         count[i]++;
                     } else {
                         break;
                     }
                 }
 
                 printStat(stat, proteinId, scanId);
                 usedProteins.add(proteinId);
             }
         }
 
         for (int i = 1; i < count.length; i++) {
             int n = count[i];
             String text = " ";
             if (n > 0) {
                 text += df.format(global[i] / n);
             }
 
             System.out.print(text);
             output.print(text);
 
         }
         List<KD> values = new ArrayList<KD>();
         values.addAll(kdStat.keySet());
         Collections.sort(values);
         for (KD value : values) {
             String text = value.toString() + " - " + kdStat.get(value);
             outputKD.println(text);
         }
 
         output.close();
         outputKD.close();
         System.out.println("\\\\");
     }
 
     public long[][] process(Scan scan, double[] proteinSpectrum, int proteinId) {
         //List<Double> precursorMassShifts = new ArrayList<Double>();
         double shift = BY_MORE.equals(monoType) ? PrecursorMassShiftFinder.getPrecursorMassShiftForMoreEdges(conf, scan) : 0;
         SpectrumResult spectrumResult = getSpectrumResult(scan, proteinSpectrum, shift);
         //System.out.println("shift = " + shift);
         //getSpectrumResult(scan, protein, 0);
         //precursorMassShifts.add(-1d);
         //precursorMassShifts.add(1d);
 
         //precursorMassShifts.add(PrecursorMassShiftFinder.getPrecursorMassShift(conf, scan));
         //precursorMassShifts.addAll(PrecursorMassShiftFinder.getAllPossibleShifts2(conf, scan));
         //Collections.sort(precursorMassShifts);
         //int len = precursorMassShifts.size();
         //if (len > 0) {
         //System.out.println("scan + " + scan.getId() + " " + precursorMassShifts.get(0) + " " + precursorMassShifts.get(len - 1) + " " + len + " " + scan.getPrecursorMass());
         //}
         /*
     long score = getScore(spectrumResult);
     for (Double precursorMassShift : precursorMassShifts) {
         SpectrumResult anotherResult = getSpectrumResult(scan, protein, precursorMassShift);
         long nextScore = getScore(anotherResult);
         if (nextScore> score) {
             if (nextScore - score > 1) {
                 //System.out.println("WOW!");
             }
             if (nextScore - score > 2) {
                // System.out.println("WOW!WOW!");
             }
 
             System.out.println("For " + scan.getId() + " precursor mass shift " + +precursorMassShift + " provieds score " + nextScore + " instead of " + score);
             spectrumResult = anotherResult;
             score = nextScore;
         }
 
         if (score - nextScore > 0) {
             //System.out.println("bad" +  (score - nextScore));
         }
         */
         /*
         KD newKD = anotherResult.kd;
         if (newKD.compareTo(spectrumResult.kd) < 0) {
             System.out.println("For " + scan.getId() + " precursor mass shift " + +precursorMassShift + " provieds kd " + newKD.toString() + " instead of " + spectrumResult.kd);
             spectrumResult = anotherResult;
         }
         */
         /*}*/
 
         spectrumResult.output(kdStat, outputKD, scan.getId(), proteinId);
         return spectrumResult.stat;
     }
 
     private long getScore(SpectrumResult spectrumResult) {
         long score = 0;
         /*
         long[][] stat = spectrumResult.stat;
         for (int i = 0; i < stat.length; i++) {
             score += stat[i][0] * i * i * i;
             score += stat[i][1] * i * i * i;
         } */
         score = spectrumResult.kd.getK() * 1000 + spectrumResult.kd.getD();
 
         return score;
     }
 
     private SpectrumResult getSpectrumResult(Scan scan, double[] proteinSpectrum, double precursorMassShift) {
         wrongCache.clear();
         correctCache.clear();
 
         List<Peak> peaks;
 
         int scanId = scan.getId();
         if (INPUT_VIRT.equals(inputType)) {
             peaks = new ArrayList<Peak>();
             peaks.addAll(msAlignPeaks.get(scanId));
         } else {
             peaks = scan.createSpectrumWithYPeaks(precursorMassShift);
         }
         if (addOnes) {
             peaks = addOnes(peaks);
         }
 
         //System.out.println("peaks.size() before duplicates " + peaks.size());
         if (needIntencity) {
             for (Iterator<Peak> peakIterator = peaks.iterator(); peakIterator.hasNext(); ) {
                 Peak next = peakIterator.next();
                 if (next.getIntensity() < intencityLevel) {
                     peakIterator.remove();
                 }
             }
         }
 
         GraphUtil.generateGapEdges(conf, peaks, gap);
 
         if (BY_NONE.equals(monoType)) {
             filterMonotags(peaks);
         }
 
         //System.out.println("peaks.size() before duplicates= " + peaks.size());
         peaks = GraphUtil.filterDuplicates(conf, peaks);
         //System.out.println("peaks.size() before after= " + peaks.size());
 
 
         SpectrumResult spectrumResult = printGappedTagInfo(peaks, proteinSpectrum, scan.getPrecursorMass());
         return spectrumResult;
     }
 
     private List<Peak> addOnes(List<Peak> peaks) {
         List<Peak> ans = new ArrayList<Peak>();
         for (Peak peak : peaks) {
             Peak p1 = new Peak(peak.getValue() - 1, peak.getIntensity(), 0);
             Peak p2 = new Peak(peak.getValue(), peak.getIntensity(), 0);
             Peak p3 = new Peak(peak.getValue() + 1, peak.getIntensity(), 0);
             if (peak.getPeakType() == PeakType.Y) {
                 double precursorMass = peak.getMass() + peak.getValue();
                 p1.convertToY(precursorMass);
                 p2.convertToY(precursorMass);
                 p3.convertToY(precursorMass);
             }
 
             ans.add(p1);
             ans.add(p2);
             ans.add(p3);
         }
         Collections.sort(ans);
         return ans;
     }
 
     private static void filterMonotags(List<Peak> peaks) {
         for (Peak peak : peaks) {
             for (Iterator<Peak> iterator = peak.getNext().iterator(); iterator.hasNext(); ) {
                 Peak next = iterator.next();
                 if (next.getPeakType() != peak.getPeakType()) {
                     //if (peak.getIntensity() != 0 && next.getIntensity() != 0) {
                     iterator.remove();
                     //System.out.println("removed");
                     //}
                 }
             }
         }
     }
 
     private SpectrumResult printGappedTagInfo(List<Peak> peaks, double[] proteinSpectrum, double precursorMass) {
         long[][] stat = new long[1000][2];
         KD kd = new KD(0, 0);
 
         List<List<Peak>> components = GraphUtil.getComponentsFromGraph(peaks);
         for (List<Peak> component : components) {
             k = 0;
             d = 0;
             for (Peak peak : component) {
                 Set<Integer> starts = new HashSet<Integer>();
                 for (int i = 0; i < proteinSpectrum.length; i++) {
                     if (MATCH_CORRECT.equals(matchType)) {
                         double mass = proteinSpectrum[i];
                         double limit = peak.getPeakType() == PeakType.Y ? TEN_PPM * 1.5 * precursorMass : TEN_PPM * peak.getMass();
                         if (Math.abs(mass - peak.getValue()) < limit) {
                             starts.add(i);
                         }
                     } else {
                         starts.add(i);
                     }
                 }
                 processGappedTags(stat, peak, 0, proteinSpectrum, starts);
             }
             KD newKD = new KD(k, d);
             if (newKD.compareTo(kd) < 0) {
                 kd = newKD;
             }
         }
 
         return new SpectrumResult(stat, kd);
     }
 
     private Map<Peak, List<CorrectResult>> correctCache = new HashMap<Peak, List<CorrectResult>>();
 
     private void processGappedTags(long[][] stat, Peak peak, int prefix, double[] proteinSpectrum, Set<Integer> starts) {
         //System.out.println(prefix + " " + peak.getValue() + " " + starts.iterator().next());
         List<CorrectResult> results = correctCache.get(peak);
         if (results != null) {
             for (CorrectResult result : results) {
                 if (result.check(starts)) {
                     long[] deltaGood = result.getDeltaGood();
                     long[] deltaBad = result.getDeltaBad();
                     for (int i = 0; i < deltaGood.length; i++) {
                         int pos = prefix + i;
                         stat[pos][0] += deltaGood[i];
                         stat[pos][1] += deltaBad[i];
                         if (deltaGood[i] > 0) {
                             if (pos > k) {
                                 k = pos;
                             }
                             if (pos > d) {
                                 d = pos;
                             }
                         }
                         if (deltaBad[i] > 0) {
                             if (pos > k) {
                                 k = pos;
                             }
                         }
                     }
                     return;
                 }
             }
         }
         long[] oldGood = new long[50];
         for (int i = 0; i < oldGood.length; i++) {
             oldGood[i] = stat[i + prefix][0];
         }
 
         long[] oldBad = new long[50];
         for (int i = 0; i < oldBad.length; i++) {
             oldBad[i] = stat[i + prefix][1];
         }
 
         if (prefix > 0) {
             //if (prefix == 6) {
             //    System.out.println("increment");
             //}
             stat[prefix][0]++;
             if (prefix > k) {
                 k = prefix;
             }
             if (prefix > d) {
                 d = prefix;
             }
         }
 
         List<Peak> nextPeaks = peak.getNext();
         for (Peak next : nextPeaks) {
             double[] limits = conf.getEdgeLimits(peak, next);
             Set<Integer> nextStarts = getNextStarts(proteinSpectrum, starts, limits, gap);
             if (nextStarts.size() == 0) {
                 processWrongGappedTags(stat, next, prefix + 1);
             } else {
                 processGappedTags(stat, next, prefix + 1, proteinSpectrum, nextStarts);
             }
         }
 
         long[] deltaGood = new long[50];
         long[] deltaBad = new long[50];
         for (int i = 0; i < oldGood.length; i++) {
             deltaGood[i] = stat[i + prefix][0] - oldGood[i];
             deltaBad[i] = stat[i + prefix][1] - oldBad[i];
         }
         if (!correctCache.containsKey(peak)) {
             correctCache.put(peak, new ArrayList<CorrectResult>());
         }
         correctCache.get(peak).add(new CorrectResult(starts, deltaGood, deltaBad));
     }
 
     private Map<Peak, long[]> wrongCache = new HashMap<Peak, long[]>();
 
     private void processWrongGappedTags(long[][] stat, Peak peak, int prefix) {
         if (wrongCache.containsKey(peak)) {
             long[] delta = wrongCache.get(peak);
             for (int i = 0; i < delta.length; i++) {
                 int pos = prefix + i;
                 stat[pos][1] += delta[i];
                 if (delta[i] > 0) {
                     if (pos > k) {
                         k = pos;
                     }
                 }
             }
             return;
         }
 
         long[] old = new long[50];
         for (int i = 0; i < old.length; i++) {
             old[i] = stat[i + prefix][1];
         }
         List<Peak> nextPeaks = peak.getNext();
         stat[prefix][1]++;
         if (prefix > k) {
             k = prefix;
         }
 
         for (Peak next : nextPeaks) {
             processWrongGappedTags(stat, next, prefix + 1);
         }
         long[] delta = new long[50];
         for (int i = 0; i < old.length; i++) {
             delta[i] = stat[i + prefix][1] - old[i];
         }
         wrongCache.put(peak, delta);
     }
 
     public static Set<Integer> getNextStarts(double[] proteinSpectrum, Set<Integer> starts, double[] limits, int gap) {
         Set<Integer> nextStarts = new HashSet<Integer>();
         for (int pos : starts) {
             for (int i = 1; i <= gap; i++) {
                 if (pos + i < proteinSpectrum.length) {
                     if (proteinSpectrum[pos + i] < 0) {
                         break;
                     }
                     double m = proteinSpectrum[pos + i] - proteinSpectrum[pos];
                     if (limits[0] < m && m < limits[1]) {
                         nextStarts.add(pos + i);
                     }
                 }
             }
         }
         return nextStarts;
     }
 
     /*
 public void researchIntencity(List<Peak> peaks, double[] proteinSpectrum) {
 double[] bad = new double[100];
 double[] good = new double[100];
 int[] badCount = new int[100];
 int[] goodCount = new int[100];
 generateTags(peaks, proteinSpectrum);
 
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
   System.out.print(" " + df.format(100d * (goodCount[i] + 0.0d) / (goodCount[i] + badCount[i])));
   if (goodCount[i] == 0) {
       //System.out.println(" bb" + i + " ");
   }
 
 }
 System.out.println();
 }
 
 public void generateTags(List<Peak> peaks, double[] proteinSpectrum) {
 for (Peak peak : peaks) {
   generateTags("", peak, proteinSpectrum,  getIntencity(peak));
 }
 }
 
 private double getIntencity(Peak peak) {
 if (peak.getIntensity() > 0) {
   return peak.getIntensity();
 }
 return Integer.MAX_VALUE;
 }
 
 public void generateTags(String prefix, Peak peak, double[] proteinSpectrum, int i, double intencity) {
 int d = prefix.length();
 boolean isGood = i>=0;
 if (isGood) {
   if (good[d] == intencity) {
       goodCount[d]++;
   } else if (good[d] < intencity) {
       goodCount[d] = 1;
       good[d] = intencity;
   }
 } else {
   if (bad[d] == intencity) {
       badCount[d]++;
   } else if (bad[d] < intencity) {
       badCount[d] = 1;
       bad[d] = intencity;
   }
 }
 
 for (Peak next : peak.getNext()) {
   for (Acid acid : Acid.values()) {
       if (acid.match(conf.getEdgeLimits(peak, next))) {
           generateTags(prefix + acid.name(), next, proteinSpectrum, i+1, Math.min(intencity, getIntencity(peak)));
       }
   }
 }
 }
     */
     private void printStat(long[][] stat, Integer proteinId, int scanId) {
         output.print(scanId + " " + proteinId);
         for (int i = 1; i < stat.length; i++) {
             long good = stat[i][0];
             long total = good + stat[i][1];
             if (total > 0) {
                 //output.print(" " + df.format((100d * good)/total) + " " + stat[i][0] + " " + stat[i][1] + " ");
                 output.print(" " + stat[i][0] + " " + stat[i][1]);
             } else {
                 break;
             }
         }
         output.println();
         output.flush();
     }
 
     public static String getReverse(String tag) {
         return new StringBuilder(tag).reverse().toString();
     }
 
     public static class CorrectResult {
         private Set<Integer> starts;
         private long[] deltaGood;
         private long[] deltaBad;
 
         public CorrectResult(Set<Integer> starts, long[] deltaGood, long[] deltaBad) {
             this.starts = starts;
             this.deltaGood = deltaGood;
             this.deltaBad = deltaBad;
         }
 
         public boolean check(Set<Integer> startsNew) {
             return starts.equals(startsNew);
         }
 
         public long[] getDeltaGood() {
             return deltaGood;
         }
 
         public long[] getDeltaBad() {
             return deltaBad;
         }
     }
 
     public static class SpectrumResult {
         private long[][] stat;
         private KD kd;
 
         public SpectrumResult(long[][] stat, KD kd) {
             this.stat = stat;
             this.kd = kd;
         }
 
         public void output(Map<KD, Integer> kdStat, PrintWriter outputKD, int scanId, int proteinId) {
             if (kdStat.containsKey(kd)) {
                 kdStat.put(kd, 1 + kdStat.get(kd));
             } else {
                 kdStat.put(kd, 1);
             }
             outputKD.println(scanId + " " + kd.toString() + " - " + proteinId);
         }
     }
 }
