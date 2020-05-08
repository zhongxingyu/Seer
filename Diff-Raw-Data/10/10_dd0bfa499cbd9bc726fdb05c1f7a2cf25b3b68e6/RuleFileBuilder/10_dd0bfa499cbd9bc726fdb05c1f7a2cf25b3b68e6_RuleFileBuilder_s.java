 /**
  * 
  */
 
 package uk.ac.cam.eng.rulebuilding.retrieval;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.GZIPOutputStream;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hbase.io.hfile.CacheConfig;
 import org.apache.hadoop.hbase.io.hfile.HFile;
 import org.apache.hadoop.hbase.io.hfile.HFileScanner;
 import org.apache.hadoop.hbase.util.BloomFilter;
 import org.apache.hadoop.hbase.util.BloomFilterFactory;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.hadoop.io.ArrayWritable;
 import org.apache.hadoop.io.SortedMapWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.io.Writable;
 
 import uk.ac.cam.eng.extraction.datatypes.Rule;
 import uk.ac.cam.eng.extraction.hadoop.datatypes.GeneralPairWritable3;
 import uk.ac.cam.eng.extraction.hadoop.datatypes.PairWritable3;
 import uk.ac.cam.eng.extraction.hadoop.datatypes.RuleWritable;
 import uk.ac.cam.eng.extraction.hadoop.util.Util;
 import uk.ac.cam.eng.rulebuilding.features.FeatureCreator;
 
 /**
  * @author jmp84 This class reads a config file specifying an HFile and a test
  *         set and other configurations, retrieves the relevant rules and
  *         returns a rule file ready to be used by the decoder
  */
 public class RuleFileBuilder {
 
     private static final Comparator<byte[]> COMPARATOR =
             new Bytes.ByteArrayComparator();
 
     private RuleFilter ruleFilter;
     private String testFile;
     private HFile.Reader hfileReader;
     private HFileScanner hfileScanner;
     private FeatureCreator featureCreator;
     private String asciiConstraints;
     private int MAX_SOURCE_PHRASE;
 
     public RuleFileBuilder(Configuration conf) throws IOException {
         testFile = conf.get("testfile");
         if (testFile == null) {
             System.err.println("Missing property 'testfile' in the config");
             System.exit(1);
         }
         String hfile = conf.get("hfile");
         if (hfile == null) {
             System.err.println("Missing property 'hfile' in the config");
             System.exit(1);
         }
         FileSystem fs = FileSystem.get(conf);
         hfileReader =
                 HFile.createReader(fs, new Path(hfile), new CacheConfig(conf));
         hfileReader.loadFileInfo();
         // 1st false: do not cache blocks (TODO check if true is better)
         // 2nd true: use positional read (better for random reads), TODO check
         // if false is better
         // 3rd false: do not use for compaction
         hfileScanner = hfileReader.getScanner(false, true, false);
         asciiConstraints = conf.get("ascii_constraints");
         String filterConfig = conf.get("filter_config");
         if (filterConfig == null) {
             System.err
                     .println("Missing property 'filter_config' in the config");
             System.exit(1);
         }
         ruleFilter = new RuleFilter(conf);
         ruleFilter.loadConfig(filterConfig);
         MAX_SOURCE_PHRASE = conf.getInt("max_source_phrase", 5);
     }
 
     public List<GeneralPairWritable3> getRules(RuleWritable sourceRule)
             throws IOException {
         byte[] ruleBytes = Util.object2ByteArray(sourceRule);
         int found = hfileScanner.seekTo(ruleBytes);
         if (found == 0) { // found the source rule
             return ruleFilter.filter(sourceRule,
                     Util.bytes2ArrayWritable(hfileScanner.getValue()));
         }
         return new ArrayList<GeneralPairWritable3>();
     }
 
     /**
      * Given a list of queries, retrieve rules in an HFile. The queries may be
      * filtered with a Bloom filter.
      * 
      * @param sourcePatternInstances
      *            The list of queries
      * @return The rules retrieved in the HFile
      * @throws IOException
      */
     public List<GeneralPairWritable3>
             getRules(Set<Rule> sourcePatternInstances)
                     throws IOException {
         List<GeneralPairWritable3> res = new ArrayList<>();
         List<byte[]> relevantSourcePatternInstances = new ArrayList<>();
         BloomFilter filter = null;
         if (hfileReader.getBloomFilterMetadata() != null) {
             filter =
                     BloomFilterFactory.createFromMeta(
                             hfileReader.getBloomFilterMetadata(), hfileReader);
         }
         for (Rule sourcePatternInstance: sourcePatternInstances) {
             RuleWritable sourcePatternInstanceWritable =
                     RuleWritable.makeSourceMarginal(sourcePatternInstance);
             byte[] ruleBytes =
                     Util.object2ByteArray(sourcePatternInstanceWritable);
             if (filter == null) {
                 relevantSourcePatternInstances.add(ruleBytes);
             }
             else if (filter.contains(ruleBytes, 0, ruleBytes.length, null)) {
                 relevantSourcePatternInstances.add(ruleBytes);
             }
         }
         // sort the queries
         Collections.sort(relevantSourcePatternInstances, COMPARATOR);
         for (byte[] relevantSourcePatternInstance: relevantSourcePatternInstances) {
             int found = hfileScanner.seekTo(relevantSourcePatternInstance);
             if (found == 0) { // found the source rule
                 res.addAll(ruleFilter.filter(Util
                         .byteArray2RuleWritable(relevantSourcePatternInstance),
                         Util.bytes2ArrayWritable(hfileScanner.getValue())));
             }
         }
         return res;
     }
 
     private Set<Rule> getAsciiConstraints() throws IOException {
         Set<Rule> res = new HashSet<Rule>();
         try (BufferedReader br =
                 new BufferedReader(new FileReader(asciiConstraints))) {
             String line;
             Pattern regex = Pattern.compile(".*: (.*) # (.*)");
             Matcher matcher;
             while ((line = br.readLine()) != null) {
                 matcher = regex.matcher(line);
                 if (matcher.matches()) {
                     String[] sourceString = matcher.group(1).split(" ");
                     String[] targetString = matcher.group(2).split(" ");
                     if (sourceString.length != targetString.length) {
                         System.err.println("Malformed ascii constraint file: "
                                 + asciiConstraints);
                         System.exit(1);
                     }
                     List<Integer> source = new ArrayList<Integer>();
                     List<Integer> target = new ArrayList<Integer>();
                     int i = 0;
                     while (i < sourceString.length) {
                         if (i % MAX_SOURCE_PHRASE == 0 && i > 0) {
                             Rule rule = new Rule(-1, source, target);
                             res.add(rule);
                             source.clear();
                             target.clear();
                         }
                         source.add(Integer.parseInt(sourceString[i]));
                         target.add(Integer.parseInt(targetString[i]));
                         i++;
                     }
                     Rule rule = new Rule(-1, source, target);
                     res.add(rule);
                 }
                 else {
                     System.err.println("Malformed ascii constraint file: "
                             + asciiConstraints);
                     System.exit(1);
                 }
             }
         }
         return res;
     }
 
     private Set<Integer> getAsciiVocab() throws IOException {
         // TODO simplify all template writing
         // TODO getAsciiVocab is redundant with getAsciiConstraints
         Set<Integer> res = new HashSet<>();
         try (BufferedReader br =
                 new BufferedReader(new FileReader(asciiConstraints))) {
             String line;
             Pattern regex = Pattern.compile(".*: (.*) # (.*)");
             Matcher matcher;
             while ((line = br.readLine()) != null) {
                 matcher = regex.matcher(line);
                 if (matcher.matches()) {
                     String[] sourceString = matcher.group(1).split(" ");
                     // only one word
                     if (sourceString.length == 1) {
                         res.add(Integer.parseInt(sourceString[0]));
                     }
                 }
                 else {
                     System.err.println("Malformed ascii constraint file: "
                             + asciiConstraints);
                     System.exit(1);
                 }
             }
         }
         return res;
     }
 
     private Set<Integer> getTestVocab() throws FileNotFoundException,
             IOException {
         Set<Integer> res = new HashSet<Integer>();
         try (BufferedReader br = new BufferedReader(new FileReader(testFile))) {
             String line;
             while ((line = br.readLine()) != null) {
                 String[] parts = line.split("\\s+");
                 for (String part: parts) {
                     res.add(Integer.parseInt(part));
                 }
             }
         }
         return res;
     }
 
     /**
      * @param testFile
      * @param hfile
      * @return
      * @throws IOException
      */
     private List<GeneralPairWritable3> getAsciiOovDeletionRules()
             throws IOException {
         List<GeneralPairWritable3> res = new ArrayList<>();
         Set<Rule> asciiRules = getAsciiConstraints();
         Set<Integer> asciiVocab = getAsciiVocab();
         Set<Integer> testVocab = getTestVocab();
         // read the HFile and select the rules matching the source phrases
         for (Rule asciiRule: asciiRules) {
             RuleWritable ruleWritable =
                     RuleWritable.makeSourceMarginal(asciiRule);
             ruleWritable.setLeftHandSide(new Text("0"));
             byte[] ruleBytes = Util.object2ByteArray(ruleWritable);
             int success = hfileScanner.seekTo(ruleBytes);
             if (success != 0) { // did not found the source: add empty features
                 res.add(new GeneralPairWritable3(new RuleWritable(asciiRule),
                         new SortedMapWritable()));
             }
             else { // found the source, look through the targets
                 ArrayWritable targetsAndFeatures =
                         Util.bytes2ArrayWritable(hfileScanner.getValue());
                 RuleWritable target =
                         RuleWritable.makeTargetMarginal(asciiRule);
                 target.setLeftHandSide(new Text("0"));
                 boolean found = false;
                 for (int i = 0; i < targetsAndFeatures.get().length; i++) {
                     GeneralPairWritable3 currentTargetAndFeatures =
                             (GeneralPairWritable3) targetsAndFeatures.get()[i];
                     RuleWritable currentTarget =
                             currentTargetAndFeatures.getFirst();
                     if (target.equals(currentTarget)) { // found the rule
                         res.add(new GeneralPairWritable3(new RuleWritable(
                                 asciiRule), currentTargetAndFeatures
                                 .getSecond()));
                         found = true;
                         break;
                     }
                 }
                 // did not found the rule, add empty features
                 if (!found) {
                     res.add(new GeneralPairWritable3(
                             new RuleWritable(asciiRule),
                             new SortedMapWritable()));
                 }
             }
         }
         for (Integer testWord: testVocab) {
             if (asciiVocab.contains(testWord)) {
                 continue;
             }
             List<Integer> source = new ArrayList<Integer>();
             source.add(testWord);
             Rule rule = new Rule(source, new ArrayList<Integer>());
             RuleWritable ruleWritable = RuleWritable.makeSourceMarginal(rule);
             byte[] ruleBytes = Util.object2ByteArray(ruleWritable);
             int success = hfileScanner.seekTo(ruleBytes);
             if (success != 0) { // did not found the source: add an oov rule
                 // TODO find a better way to represent an oov rule
                 Rule oovRule = new Rule(-1, source, new ArrayList<Integer>());
                 res.add(new GeneralPairWritable3(new RuleWritable(oovRule),
                         new SortedMapWritable()));
             }
             else { // found it: add deletion rule
                 List<Integer> deletion = new ArrayList<Integer>();
                 // deletion is represented by a zero
                 deletion.add(0);
                 Rule deletionRule = new Rule(-1, source, deletion);
                 RuleWritable deletionRuleWritable =
                         new RuleWritable(deletionRule);
                 res.add(new GeneralPairWritable3(deletionRuleWritable,
                         new SortedMapWritable()));
             }
         }
         return res;
     }
 
     public List<GeneralPairWritable3> getGlueRules() {
         List<GeneralPairWritable3> res = new ArrayList<>();
         List<Integer> sideGlueRule1 = new ArrayList<Integer>();
         sideGlueRule1.add(-4);
         sideGlueRule1.add(-1);
         Rule glueRule1 = new Rule(-4, sideGlueRule1, sideGlueRule1);
         res.add(new GeneralPairWritable3(new RuleWritable(glueRule1),
                 new SortedMapWritable()));
         List<Integer> sideGlueRule2 = new ArrayList<Integer>();
         sideGlueRule2.add(-1);
         Rule glueRule2 = new Rule(-1, sideGlueRule2, sideGlueRule2);
         res.add(new GeneralPairWritable3(new RuleWritable(glueRule2),
                 new SortedMapWritable()));
         List<Integer> startSentenceSide = new ArrayList<Integer>();
         startSentenceSide.add(1);
         Rule startSentence = new Rule(-1, startSentenceSide, startSentenceSide);
         res.add(new GeneralPairWritable3(new RuleWritable(startSentence),
                 new SortedMapWritable()));
         List<Integer> endSentenceSide = new ArrayList<Integer>();
         endSentenceSide.add(2);
         Rule endSentence = new Rule(-1, endSentenceSide, endSentenceSide);
         res.add(new GeneralPairWritable3(new RuleWritable(endSentence),
                 new SortedMapWritable()));
         // TODO add a missing glue here
         return res;
     }
 
     public List<GeneralPairWritable3> getRulesWithFeatures(Configuration conf,
             List<GeneralPairWritable3> rules) throws IOException {
         List<GeneralPairWritable3> res = new ArrayList<>();
         featureCreator = new FeatureCreator(conf);
         List<GeneralPairWritable3> regularRulesWithFeatures =
                 featureCreator.createFeatures(rules);
         List<GeneralPairWritable3> asciiOovDeletionRules =
                 getAsciiOovDeletionRules();
         List<GeneralPairWritable3> asciiOovDeletionRulesWithFeatures =
                 featureCreator
                         .createFeaturesAsciiOovDeletion(asciiOovDeletionRules);
         List<GeneralPairWritable3> glueRules = getGlueRules();
         List<GeneralPairWritable3> glueRulesWithFeatures =
                 featureCreator.createFeaturesGlueRules(glueRules);
         // TODO should be called only once
         Set<Rule> asciiRules = getAsciiConstraints();
         for (GeneralPairWritable3 ruleWithFeatures: regularRulesWithFeatures) {
             // check if rule is not an ascii rule
             Rule checkNotAscii = new Rule(-1, ruleWithFeatures.getFirst());
             if (asciiRules.contains(checkNotAscii)) {
                 // this rule will be included as an ascii rule, don't
                 // include it here
                 System.err.println("Ascii rule has been extracted: "
                         + checkNotAscii.toString());
                 continue;
             }
             res.add(ruleWithFeatures);
         }
         res.addAll(asciiOovDeletionRulesWithFeatures);
         res.addAll(glueRulesWithFeatures);
         return res;
     }
 
     public String
             printSetSpecificRuleFile(List<PairWritable3> rulesWithFeatures) {
         StringBuilder sb = new StringBuilder();
         for (PairWritable3 ruleWithFeatures: rulesWithFeatures) {
             sb.append(ruleWithFeatures.first);
             Writable[] features = ruleWithFeatures.second.get();
             for (Writable w: features) {
                 sb.append(" " + w.toString());
             }
             sb.append("\n");
         }
         return sb.toString();
     }
 
     public void writeSetSpecificRuleFile(
             List<GeneralPairWritable3> rulesWithFeatures,
             String outRuleFile) throws FileNotFoundException, IOException {
         try (BufferedOutputStream bos =
                 new BufferedOutputStream(new GZIPOutputStream(
                         new FileOutputStream(outRuleFile)))) {
             for (GeneralPairWritable3 ruleWithFeatures: rulesWithFeatures) {
                 bos.write(ruleWithFeatures.getFirst().toString().getBytes());
                 SortedMapWritable features = ruleWithFeatures.getSecond();
                 // TODO format better
                 for (Writable featureIndex: features.keySet()) {
                     bos.write((" " + features.get(featureIndex).toString()
                             + "@" + featureIndex).getBytes());
                 }
                 bos.write("\n".getBytes());
             }
         }
     }
 }
