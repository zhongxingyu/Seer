 package ru.spbau.bioinf.tagfinder;
 
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import ru.spbau.bioinf.tagfinder.util.ReaderUtil;
 
 public class FastSearch {
 
     private static List<Protein> proteins;
     private static Map<Integer, Scan> scans;
 
     private static Map<String, Double> evalues = new HashMap<String, Double>();
     private static PrintWriter cacheOut;
     private static Map<Integer, Integer> ans = new HashMap<Integer, Integer>();
     private static final Set<Integer> unmatchedScans = new HashSet<Integer>();
     private static final Set<Integer> discoveredProteins = new HashSet<Integer>();
 
     private  static Set<Long> processed = new HashSet<Long>();
     private static long finish;
     private static long start;
 
     public static void main(String[] args) throws Exception {
         //initCache();
 
         int count = 0;
         Configuration conf = new Configuration(args);
         Map<Integer, Integer> msAlignResults = conf.getMSAlignResults();
         System.out.println("MS-Align results: " + msAlignResults.keySet().size());
         proteins = conf.getProteins();
         scans = conf.getScans();
         List<Integer> keys = new ArrayList<Integer>();
         keys.addAll(scans.keySet());
         Collections.sort(keys);
         EValueAdapter.init(conf);
 
         try {
             doSearch(count, conf, keys);
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         //cacheOut.println("end matches");
         //cacheOut.close();
         System.out.println("PrSM found " + ans.keySet().size());
         Map<Integer, Double> evaluesOld = conf.getEvalues();
         System.out.println("MS-Align only");
         for (Integer scanId : msAlignResults.keySet()) {
             if (!ans.containsKey(scanId)) {
                 int proteinId = msAlignResults.get(scanId);
                 Scan scan = scans.get(scanId);
                 System.out.println(scanId + " " + proteinId + " " + evaluesOld.get(scanId) + " " + getEValue(scanId, proteinId) + " "
                         + scan.getPeaks().size() + " " + scan.getPrecursorMass() + " " + proteins.get(proteinId).getSimplifiedAcids().length() + " " + discoveredProteins.contains(proteinId));
             }
         }
         System.out.println("New matches ");
         for (Integer scanId : ans.keySet()) {
             if (!msAlignResults.containsKey(scanId)) {
                 int proteinId = ans.get(scanId);
                 System.out.println(scanId + " " + proteinId + " " + getEValue(scanId, proteinId));
             }
         }
 
     }
 
     private static void initCache() throws IOException {
         File cacheFile = new File("cache.txt");
         BufferedReader in = ReaderUtil.createInputReader(cacheFile);
         cacheOut = new PrintWriter(new OutputStreamWriter(
                 new FileOutputStream(cacheFile, true), "UTF-8"));
         do {
             String s = in.readLine();
             if (s == null) {
                 break;
             }
             String[] data = s.split(" ");
             evalues.put(data[0] + "_" + data[1], Double.parseDouble(data[2]));
         } while (true);
     }
 
     private static void doSearch(int count, Configuration conf, List<Integer> keys) throws Exception {
         start = System.currentTimeMillis();
         unmatchedScans.addAll(keys);
 
         List<int[]>[] candidates = doEdgeSearch(keys, 0);
 
         for (int score = 26; score > 12; score--) {
             for (int[] pair : candidates[score]) {
                 getEValueWrapper(pair[0], pair[1]);
             }
             System.out.println("results for score " + score + ": " + goodRequest + " " + badRequest);
         }
 
         List<int[]>[] candidates2 = doEdgeSearch(keys, 1);
         for (int score = 26; score > 12; score--) {
             for (int[] pair : candidates2[score]) {
                 //getEValueWrapper(pair[0], pair[1]);
             }
             System.out.println("results for score " + score + ": " + goodRequest + " " + badRequest);
         }
 
         for (int len = 10; len >= 5; len--) {
             checkTags(conf, len);
 
         }
 
 
         researchAttempt();
 
         checkTags(conf, 4);
         checkTags(conf, 3);
 
 
         long finish2 = System.currentTimeMillis();
         System.out.println(count + " matches  for plus " + (finish2 - finish));
 
         List<Integer> unmatchedScans2 = new ArrayList<Integer>();
         for (int scanId : unmatchedScans) {
             if (checkScanAgainstProteins(scanId, discoveredProteins)) {
                 count++;
             } else {
                 unmatchedScans2.add(scanId);
             }
         }
 
 
         long finish3 = System.currentTimeMillis();
         System.out.println(count + " matches  for plus " + (finish3 - finish2));
     }
 
     private static List<int[]>[] doEdgeSearch(List<Integer> keys, int depth) throws Exception {
         List<int[]>[] candidates = new List[27];
         for (int i = 0; i < candidates.length; i++) {
             candidates[i] = new ArrayList<int[]>();
         }
 
         for (int scanId : keys) {
             Scan scan = scans.get(scanId);
             List<Peak> peaks = scan.getPeaks();
             Collections.sort(peaks);
             double[] p = new double[peaks.size()];
             for (int i = 0; i < p.length; i++) {
                 p[i] = peaks.get(i).getMass(); //+ shift;
             }
             int[] ans = getBestProtein(p, depth);
             int proteinId = ans[0];
             getEValueWrapper(scanId, proteinId);
             if (ans[1] >= 27) {
                 double v = getEValueWrapper(scanId, proteinId);
                 if (v > Configuration.EVALUE_LIMIT) {
                     //System.out.println(scan.getId() + " " + proteinId + " " + ans[1] + " " + v + " " + proteins.get(proteinId).getName());
                 }
             } else {
                 candidates[ans[1]].add(new int[]{scanId, ans[0]});
             }
         }
         finish = System.currentTimeMillis();
         System.out.println("results for first stage " + goodRequest + " " + badRequest + " time : " + (finish - start));
         return candidates;
     }
 
     private static void researchAttempt() throws Exception {
         List<Integer> forResearch = new ArrayList<Integer>();
         forResearch.addAll(unmatchedScans);
         for (int scanId : forResearch) {
             Scan scan = scans.get(scanId);
             List<Peak> peaks = scan.getPeaks();
             Collections.sort(peaks);
             double[] p = new double[peaks.size()];
             for (int i = 0; i < p.length; i++) {
                 p[i] = peaks.get(i).getMass();
             }
             int[] ans = getBestProteinResearch(p);
             int proteinId = ans[0];
             if (ans[1] >= 6) {
                 getEValueWrapper(scanId, proteinId);
             }
         }
         System.out.println("results for research: " + goodRequest + " " + badRequest);
     }
 
     private static void checkTags(Configuration conf, int len) throws Exception {
         System.out.println("processing tags of length " + len);
         Map<String, List<TagProtein>> tagsMap = new HashMap<String, List<TagProtein>>();
         for (Protein protein : proteins) {
             int proteinId = protein.getProteinId();
             if (proteinId == 1518 || proteinId == 3889 || proteinId == 4122) {
                 continue; //Strange symbols
             }
             String sequence = protein.getSimplifiedAcids();
             double mass = 0;
             double total = 0;
             for (int i = 0; i < sequence.length(); i++) {
                 total += Acid.getAcid(sequence.charAt(i)).getMass();
             }
             for (int i = 0; i < sequence.length() - len; i++) {
                 mass += Acid.getAcid(sequence.charAt(i)).getMass();
                 String tag = sequence.substring(i, i + len);
                 if (!tagsMap.containsKey(tag)) {
                     tagsMap.put(tag, new ArrayList<TagProtein>());
                 }
                 tagsMap.get(tag).add(new TagProtein(protein.getProteinId(), mass, total - mass));
             }
         }
         List<Integer> scansForProcess = new ArrayList<Integer>();
         scansForProcess.addAll(unmatchedScans);
         for (int scanId : scansForProcess) {
             Scan scan = scans.get(scanId);
             List<Peak> spectrum = scan.createSpectrumWithYPeaks(0);
             GraphUtil.generateEdges(conf, spectrum);
             Map<String, Peak> tags = GraphUtil.generateTagsWithStarts(conf, spectrum);
             List<Integer> proteinsForCheck = new ArrayList<Integer>();
             for (String tag : tags.keySet()) {
                 List<TagProtein> tagProteins = tagsMap.get(tag);
                 if (tagProteins != null) {
                     for (TagProtein tagProtein : tagProteins) {
                         Peak startPeak = tags.get(tag);
                         if (startPeak.getPeakType() == PeakType.Y) {
                             if (tagProtein.suffix + 200 < startPeak.getMass()) {
                                 //if (scanId == 2843) {
                                 //    System.out.println(tagProtein.suffix + " " + startPeak.getMass());
                                 //}
                                 continue;
                             }
                         } else {
                             if (tagProtein.prefix + 200 < startPeak.getMass()) {
                                 continue;
                             }
                         }
 
                         int proteinId = tagProtein.proteinId;
                         if (!proteinsForCheck.contains(proteinId)) {
                             proteinsForCheck.add(proteinId);
                             if (proteinsForCheck.size() > 100) {
                                 break;
                             }
                         }
                     }
                 }
             }
             checkScanAgainstProteins(scanId, proteinsForCheck);
         }
         System.out.println("results  " + goodRequest + " " + badRequest);
     }
 
     private static boolean checkScanAgainstProteins(int scanId, Collection<Integer> proteins) throws Exception {
         int size = proteins.size();
         for (int proteinId : proteins) {
             if (size < 5 || discoveredProteins.contains(proteinId)) {
                 double eValue = getEValueWrapper(scanId, proteinId);
                 if (eValue < Configuration.EVALUE_LIMIT) {
                     System.out.println(scanId + " " + proteinId + " " + eValue + " " + size);
                     return true;
                 }
             }
         }
         return false;
     }
 
     private static int goodRequest = 3000;
     private static int badRequest = 0;
 
     private static double getEValueWrapper(int scanId, int proteinId) throws Exception {
         double ret = getEValue(scanId, proteinId);
         long key = scanId * 1024L * 1024L + proteinId;
         if (processed.contains(key)) {
             return ret;
         }
         processed.add(key);
 
         if (ret < Configuration.EVALUE_LIMIT) {
             ans.put(scanId, proteinId);
             unmatchedScans.remove(scanId);
             discoveredProteins.add(proteinId);
             System.out.println(scanId + " " + proteinId + " " + ret);
             goodRequest++;
         } else {
             badRequest++;
         }
         if (badRequest > goodRequest) {
             throw new Exception(badRequest + " > " + goodRequest);
         }
         return ret;
     }
 
     private static double getEValue(int scanId, int proteinId) throws Exception {
         String key = scanId + "_" + proteinId;
         double ans = Integer.MAX_VALUE;
         //if (evalues.containsKey(key)) {
         //    return evalues.get(key);
         //} else {
             URL server = new URL("http://127.0.0.1:8080/evalue?scanId=" + scanId + "&proteinId="+proteinId);
             URLConnection conn = server.openConnection();
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(
                             conn.getInputStream()));
             try {
                 return Double.parseDouble(in.readLine());
             } finally {
                 in.close();
             }
             /*
             String response = ""; 
             String inputLine;
             while ((inputLine = in.readLine()) != null) {
                 System.out.println(inputLine);
             }
             in.close();            
             PrSM prsm = EValueAdapter.getBestEValue(scans.get(scanId), proteinId);
             if (prsm != null) {
                 ans = prsm.getEValue();
             }
             evalues.put(key, ans);
 
         }
 
         cacheOut.println(scanId + " " + proteinId + " " + ans);
         cacheOut.flush();
         return ans;
         */
     }
 
     public static int[] getBestProtein(double[] p, int pos) {
         int[][] ans = new int[pos+2][2];
         for (Protein protein : proteins) {
             if (protein.getProteinId() == 2535) {
                 continue;
             }
             double[] yEnds = protein.getYEnds();
             double[] bEnds = protein.getBEnds();
             if (yEnds.length < 1 || p.length < 1) {
                 continue;
             }
             int score = max(
                     getScoreY(p, yEnds),
                     getScoreB(p, bEnds),
                     getScoreBM(p, bEnds),
                     getScoreBN(p, bEnds)
             );
 
             if (score > 0) {
                 ans[ans.length - 1] = new int[] {protein.getProteinId(), score};
                 Arrays.sort(ans, new Comparator<int[]>() {
                     public int compare(int[] o1, int[] o2) {
                         return o2[1] - o1[1];
                     }
                 });
             }
 
         }
         return ans[pos];
     }
 
     public static int[] getBestProteinResearch(double[] spectrum) {
         int[] ans = new int[]{0, 0};
         int bestScore = 0;
         for (Protein protein : proteins) {
             if (protein.getProteinId() == 2535) {
                 continue;
             }
             double[] yEnds = protein.getYEnds();
             double[] bEnds = protein.getBEnds();
             if (yEnds.length < 1 || spectrum.length < 1) {
                 continue;
             }
             int score = max(getResearchScore(yEnds, spectrum), getResearchScore(bEnds, spectrum));
 
             if (score > bestScore) {
                 bestScore = score;
                 ans[0] = protein.getProteinId();
                 ans[1] = score;
             }
         }
         return ans;
     }
 
     private static int max(int... scores) {
         int ans = 0;
         for (int score : scores) {
             if (score > ans) {
                 ans = score;
             }
         }
         return ans;
     }
 
     private static int getScoreY(double[] p, double[] yEnds) {
         return (int) Math.round(3 * ShiftEngine.getScore(p, yEnds, -Consts.WATER) + ShiftEngine.getScore(p, yEnds, 0));
     }
 
     private static int getScoreB(double[] p, double[] bEnds) {
         return (int) Math.round(ShiftEngine.getScore(p, bEnds, -Consts.WATER) + 3 * ShiftEngine.getScore(p, bEnds, 0));
     }
 
     private static int getScoreBM(double[] p, double[] bEnds) {
         return (int) Math.round(3 * ShiftEngine.getScore(p, bEnds, -Acid.M.getMass()) + ShiftEngine.getScore(p, bEnds, -Acid.M.getMass() - Consts.AMMONIA));
     }
 
     private static int getScoreBN(double[] p, double[] bEnds) {
         return (int) Math.round(3 * ShiftEngine.getScore(p, bEnds, -Consts.N));
     }
 
     private static int getResearchScore(double[] suffixes, double[] masses) {
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
         return score;
     }
     
     public static class TagProtein {
         int proteinId;
         double prefix;
         double suffix;
 
         public TagProtein(int proteinId, double place, double suffix) {
             this.proteinId = proteinId;
             this.prefix = place;
             this.suffix = suffix;
         }
     }
 
 }
