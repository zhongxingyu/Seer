 /**
  * 
  */
 
 package uk.ac.cam.eng.rulebuilding.retrieval;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.GZIPOutputStream;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hbase.io.hfile.HFile;
 import org.apache.hadoop.hbase.io.hfile.HFileScanner;
 import org.apache.hadoop.io.ArrayWritable;
 import org.apache.hadoop.io.DataInputBuffer;
 import org.apache.hadoop.io.DoubleWritable;
 import org.apache.hadoop.io.Writable;
 
 import uk.ac.cam.eng.extraction.datatypes.Rule;
 import uk.ac.cam.eng.extraction.hadoop.datatypes.PairWritable3;
 import uk.ac.cam.eng.extraction.hadoop.datatypes.RuleWritable;
 import uk.ac.cam.eng.rulebuilding.features.FeatureCreator;
 
 /**
  * @author jmp84 This class reads a config file specifying an HFile and a test
  *         set and other configurations, retrieves the relevant rules and
  *         returns a rule file ready to be used by the decoder
  */
 public class RuleFileBuilder {
 
     private RuleFilter ruleFilter;
     private PatternInstanceCreator2 patternInstanceCreator;
     private String testFile;
     private HFileScanner hfileScanner;
     private FeatureCreator featureCreator;
     private String asciiConstraints;
 
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
         HFile.Reader hfileReader = new HFile.Reader(fs, new Path(hfile),
                 null, false);
         hfileReader.loadFileInfo();
         hfileScanner = hfileReader.getScanner();
         asciiConstraints = conf.get("ascii_constraints");
         String filterConfig = conf.get("filter_config");
         if (filterConfig == null) {
             System.err.println(
                     "Missing property 'filter_config' in the config");
             System.exit(1);
         }
         ruleFilter = new RuleFilter();
         ruleFilter.loadConfig(filterConfig);
         patternInstanceCreator = new PatternInstanceCreator2(conf);
     }
 
     private static byte[] object2ByteArray(Writable obj) throws IOException {
         ByteArrayOutputStream buffer = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream(buffer);
         obj.write(out);
         return buffer.toByteArray();
     }
 
     private static ArrayWritable convertValueBytes(ByteBuffer bytes) {
         DataInputBuffer in = new DataInputBuffer();
         in.reset(bytes.array(), bytes.arrayOffset(), bytes.limit());
         ArrayWritable value = new ArrayWritable(PairWritable3.class);
         try {
             value.readFields(in);
         }
         catch (IOException e) {
             // Byte buffer is memory backed so no exception is possible. Just in
             // case chain it to a runtime exception
             throw new RuntimeException(e);
         }
         return value;
     }
 
     Set<Rule> getSourceRuleInstances() throws FileNotFoundException,
             IOException {
         return patternInstanceCreator.createSourcePatternInstances(testFile);
     }
 
     // not used anymore as mapreduce is used for retrieval, but this function
     // can be used to get all the rules if we don't want to use mapreduce.
     private List<PairWritable3> getRules() throws IOException {
         List<PairWritable3> res = new ArrayList<PairWritable3>();
         Set<Rule> sourceRules = getSourceRuleInstances();
         System.err.println("source rule size: " + sourceRules.size());
         int counter = 0;
         for (Rule rule: sourceRules) {
             // don't include the unaligned word info which is not there anyway
             RuleWritable ruleWritable = RuleWritable
                     .makeSourceMarginal(rule, true);
             byte[] ruleBytes = object2ByteArray(ruleWritable);
             int success = hfileScanner.seekTo(ruleBytes);
             if (success == 0) { // found the source rule
                 List<PairWritable3> filteredRules = ruleFilter.filter(
                         ruleWritable,
                         convertValueBytes(hfileScanner.getValue()));
                 res.addAll(filteredRules);
             }
             counter++;
             if (counter % 50000 == 0) {
                 System.err.println("Processed " + counter + " sources");
             }
         }
         return res;
     }
 
     public List<PairWritable3> getRules(RuleWritable sourceRule)
             throws IOException {
         List<PairWritable3> res = new ArrayList<>();
         byte[] ruleBytes = object2ByteArray(sourceRule);
         int found = hfileScanner.seekTo(ruleBytes);
         if (found == 0) { // found the source rule
             List<PairWritable3> filteredRules = ruleFilter.filter(
                     sourceRule,
                     convertValueBytes(hfileScanner.getValue()));
             res.addAll(filteredRules);
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
                         if (i % patternInstanceCreator.MAX_SOURCE_PHRASE == 0
                                 && i > 0) {
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
 
     private Set<Integer> getTestVocab()
             throws FileNotFoundException, IOException {
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
     private List<PairWritable3> getAsciiOovDeletionRules() throws IOException {
         List<PairWritable3> res = new ArrayList<PairWritable3>();
         Set<Rule> asciiRules = getAsciiConstraints();
         Set<Integer> asciiVocab = getAsciiVocab();
         Set<Integer> testVocab = getTestVocab();
         // read the HFile and select the rules matching the source phrases
         for (Rule asciiRule: asciiRules) {
             res.add(new PairWritable3(new RuleWritable(asciiRule),
                     new ArrayWritable(DoubleWritable.class)));
         }
         for (Integer testWord: testVocab) {
             if (asciiVocab.contains(testWord)) {
                 continue;
             }
             List<Integer> source = new ArrayList<Integer>();
             source.add(testWord);
             Rule rule = new Rule(source, new ArrayList<Integer>());
             // don't include the unaligned word info which is not there anyway
             RuleWritable ruleWritable = RuleWritable
                     .makeSourceMarginal(rule, true);
             byte[] ruleBytes = object2ByteArray(ruleWritable);
             int success = hfileScanner.seekTo(ruleBytes);
             if (success != 0) { // did not found the source: add an oov rule
                 // TODO find a better way to represent an oov rule
                 Rule oovRule = new Rule(-1, source, new ArrayList<Integer>());
                 res.add(new PairWritable3(new RuleWritable(oovRule),
                         new ArrayWritable(DoubleWritable.class)));
             }
             else { // found it: add deletion rule
                 List<Integer> deletion = new ArrayList<Integer>();
                 // deletion is represented by a zero
                 deletion.add(0);
                 Rule deletionRule = new Rule(-1, source, deletion);
                 RuleWritable deletionRuleWritable =
                         new RuleWritable(deletionRule);
                 res.add(new PairWritable3(deletionRuleWritable,
                         new ArrayWritable(DoubleWritable.class)));
             }
         }
         return res;
     }
 
     public List<PairWritable3> getGlueRules() {
         List<PairWritable3> res = new ArrayList<PairWritable3>();
         List<Integer> sideGlueRule1 = new ArrayList<Integer>();
         sideGlueRule1.add(-4);
         sideGlueRule1.add(-1);
         Rule glueRule1 = new Rule(-4, sideGlueRule1, sideGlueRule1);
         res.add(new PairWritable3(new RuleWritable(glueRule1),
                 new ArrayWritable(DoubleWritable.class)));
         List<Integer> sideGlueRule2 = new ArrayList<Integer>();
         sideGlueRule2.add(-1);
         Rule glueRule2 = new Rule(-1, sideGlueRule2, sideGlueRule2);
         res.add(new PairWritable3(new RuleWritable(glueRule2),
                 new ArrayWritable(DoubleWritable.class)));
         List<Integer> startSentenceSide = new ArrayList<Integer>();
         startSentenceSide.add(1);
         Rule startSentence = new Rule(-1, startSentenceSide, startSentenceSide);
         res.add(new PairWritable3(new RuleWritable(startSentence),
                 new ArrayWritable(DoubleWritable.class)));
         List<Integer> endSentenceSide = new ArrayList<Integer>();
         endSentenceSide.add(2);
         Rule endSentence = new Rule(-1, endSentenceSide, endSentenceSide);
         res.add(new PairWritable3(new RuleWritable(endSentence),
                 new ArrayWritable(DoubleWritable.class)));
         // TODO add a missing glue here
         return res;
     }
 
     public List<PairWritable3> getRulesWithFeatures(
             Configuration conf, List<PairWritable3> rules)
            throws FileNotFoundException, IOException {
         List<PairWritable3> res = new ArrayList<>();
         // lazy initialization of featureCreator
         // call here rather than in the constructor because takes time to load
         // the lexical models
         featureCreator = new FeatureCreator(conf, rules);
         List<PairWritable3> regularRulesWithFeatures =
                 featureCreator.createFeatures(rules);
         List<PairWritable3> asciiOovDeletionRules = getAsciiOovDeletionRules();
         List<PairWritable3> asciiOovDeletionRulesWithFeatures =
                 featureCreator.createFeaturesAsciiOovDeletion(
                         asciiOovDeletionRules);
         List<PairWritable3> glueRules = getGlueRules();
         List<PairWritable3> glueRulesWithFeatures =
                 featureCreator.createFeaturesGlueRules(glueRules);
         // TODO should be called only once
         Set<Rule> asciiRules = getAsciiConstraints();
         for (PairWritable3 ruleWithFeatures: regularRulesWithFeatures) {
             // check if rule is not an ascii rule
             Rule checkNotAscii = new Rule(-1, ruleWithFeatures.first);
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
 
     public String printSetSpecificRuleFile(List<PairWritable3> rulesWithFeatures) {
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
 }
