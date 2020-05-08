 package ru.spbau.bioinf.tagfinder;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import ru.spbau.bioinf.tagfinder.util.ReaderUtil;
 
 public class GenerateMatchesTable {
 
     private static Configuration conf;
 
     public static DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance();
 
     static {
         df.applyPattern("0.##E0");
         DecimalFormatSymbols dfs = new DecimalFormatSymbols();
         dfs.setDecimalSeparator('.');
         df.setDecimalFormatSymbols(dfs);
     }
 
     public static void main(String[] args) throws Exception {
 
         conf = new Configuration(args//, UnmatchedScansGenerator.SHARED_MODE
         );
         List<Protein> proteins = conf.getProteins();
         Map<Integer, Integer> badMSAlignResults = conf.getBadMSAlignResults();
         Map<Integer, Scan> scans = conf.getScans();
         List<Integer> keys = new ArrayList<Integer>();
         keys.addAll(scans.keySet());
         Collections.sort(keys);
         List<String> sequences = new ArrayList<String>();
         for (Protein protein : proteins) {
             sequences.add(protein.getSimplifiedAcids());
         }
 
 
         System.out.println("\\begin{table}[h]\\footnotesize\n" +
                 "\\vspace{3mm}\\\n" +
                 "{\\centering\n" +
                 "\\begin{center}\n" +
                 "\\begin{tabular}{|c|c||c|c|c|c|}\n" +
                 "  \\hline\n" +
                 "  \\multicolumn{2}{|c||}{PrSM by MS-Align+} & \\multicolumn{4}{|c|}{tag-based matches} \\\\\n" +
                 "  \\hline\n" +
                 "  scan & protein & protein & tag & occurrences & E-Value\\\\\n"
         );
 
 
         BufferedReader in = ReaderUtil.getBufferedReader(new File("matches.txt"));
         String s;
         Map<Integer, List<MatchResult>> res = new HashMap<Integer, List<MatchResult>>();
 
         HashMap<String, Integer> stat = new HashMap<String, Integer>();
 
         while ((s = in.readLine()) != null) {
             String[] data = s.split(" ");
             int scanId = Integer.parseInt(data[0]);
             int proteinId = Integer.parseInt(data[2]);
             double eValue = Double.parseDouble(data[3]);
 
             if (eValue > 0.0024) {
                 continue;
             }
 
 
             Scan scan = scans.get(scanId);
             List<Peak> peaks = //scan.createStandardSpectrum();
                     scan.createSpectrumWithYPeaks(PrecursorMassShiftFinder.getPrecursorMassShiftForMoreEdges(conf, scan));
             GraphUtil.generateEdges(conf, peaks);
 
             List<Set<String>> allTags = new ArrayList<Set<String>>();
             for (int depth = 10; depth >= 5; depth--) {
                 Set<String> tags = TagProteinGenerator.getTags(peaks, depth);
                 if (tags.size() > 0) {
                     allTags.add(tags);
                 }
             }
 
 
             for (Set<String> tagSet : allTags) {
                 for (String tag : tagSet) {
                     if (!stat.containsKey(tag)) {
                         int count = 0;
                         for (int i = 0; i < sequences.size(); i++) {
                             String seq = sequences.get(i);
                             if (seq.contains(tag)) {
                                 count++;
                             }
                         }
                         stat.put(tag, count);
                     }
                 }
             }
 
 
             if (res.get(scanId) == null) {
                 res.put(scanId, new ArrayList<MatchResult>());
             }
             List<MatchResult> matches = res.get(scanId);
 
             String seq = sequences.get(proteinId);
             Set<String> found = new HashSet<String>();
             for (Set<String> tagSet : allTags) {
                 for (String tag : tagSet) {
                     boolean isSubTag = false;
                     for (String oldTag : found) {
                         if (oldTag.contains(tag)) {
                             isSubTag = true;
                             break;
                         }
                     }
                     if (isSubTag) {
                         continue;
                     }
                     if (seq.contains(tag)) {
                         found.add(tag);
                     }
                 }
             }
 
 
             MatchResult match = new MatchResult(proteinId, eValue);
             matches.add(match);
             for (String tag : found) {
                 match.addTag(tag);
             }
         }
 
         for (Map.Entry<Integer, List<MatchResult>> entry : res.entrySet()) {
             int scanId = entry.getKey();
             List<MatchResult> matchResults = entry.getValue();
             for (int i = 0; i < matchResults.size(); i++) {
                 Integer oldProteinId = badMSAlignResults.get(scanId);
                 MatchResult match = matchResults.get(i);
                     System.out.println(
                             "  \\hline\n" + scanId + "& " +
                                     (oldProteinId == null ? "" : oldProteinId) +
                                     " & " + match.proteinId + "& ");
 
                 List<String> tags = match.tags;
                 for (int j = 0; j < tags.size(); j++) {
                     if (j > 0) {
                         System.out.print(", ");
                     }
                     String tag = tags.get(j);
                     System.out.print(tag);
                 }
                 System.out.print(" & ");
                 for (int j = 0; j < tags.size(); j++) {
                     if (j > 0) {
                         System.out.print(", ");
                     }
                     String tag = tags.get(j);
                     System.out.print(stat.get(tag));
                 }
 
                 System.out.println(" & "  + df.format(match.eValue) + "\\\\");
             }
 
         }
 
 
         System.out.println("\\hline\\end{tabular}\n" +
                 "\\end{center}\n" +
                 "\\par}\n" +
                 "\\centering\n" +
                "\\caption{PrSMs indicated by MS-Align+ for spectra considered to be unidentified due to  large E-values of the matches, and alternatives suggested by a~tag-based analysis. Each candidate protein contains at least $3$ (possibly overlapping) tags of length $5$ from the respective spectrum. For each retrieved tag, the number of its occurrences in the database is indicated, and for each group of tags that occur in the same protein, the number of proteins containing all those tags simultaneously is provided.}\n" +
                 "\\vspace{3mm}\n" +
                 "\\label{table:unident-spectra}\n" +
                 "\\end{table}");
 
 
     }
 
 
     public static class MatchResult {
         int proteinId;
         List<String> tags = new ArrayList<String>();
         double eValue;
 
         public MatchResult(int proteinId, double eValue) {
             this.proteinId = proteinId;
             this.eValue = eValue;
         }
 
         public void addTag(String tag) {
             tags.add(tag);
         }
     }
 
 }
 
