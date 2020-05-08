 /**
  * 
  */
 
 package uk.ac.cam.eng.rulebuilding.retrieval;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.GZIPOutputStream;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hbase.io.hfile.HFile;
 import org.apache.hadoop.hbase.io.hfile.HFileScanner;
 import org.apache.hadoop.io.ArrayWritable;
 import org.apache.hadoop.io.DataInputBuffer;
 import org.apache.hadoop.io.DoubleWritable;
 import org.apache.hadoop.io.Writable;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 import uk.ac.cam.eng.extraction.datatypes.Rule;
 import uk.ac.cam.eng.extraction.hadoop.datatypes.PairWritable3;
 import uk.ac.cam.eng.extraction.hadoop.datatypes.RuleWritable;
 import uk.ac.cam.eng.extraction.hadoop.extraction.ExtractorJob;
 import uk.ac.cam.eng.rulebuilding.features.FeatureCreator;
 
 /**
  * @author jmp84 This class reads a config file specifying an HFile and a test
  *         set and other configurations, retrieves the relevant rules and
  *         returns a rule file ready to be used by the decoder
  */
 public class RuleFileBuilder extends Configured implements Tool{
 
     private RuleFilter ruleFilter;
     private PatternInstanceCreator2 patternInstanceCreator;
 
     public RuleFileBuilder(String filterConfig) throws FileNotFoundException,
             IOException {
         ruleFilter = new RuleFilter();
         ruleFilter.loadConfig(filterConfig);
         Properties p = new Properties();
         try {
             p.load(new FileInputStream(filterConfig));
         }
         catch (IOException e) {
             e.printStackTrace();
             System.exit(1);
         }
         //Configuration conf = getConf();
         Configuration conf = new Configuration();
         for (String prop: p.stringPropertyNames()) {
             conf.set(prop, p.getProperty(prop));
         }
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
 
     Set<Rule> getSourceRuleInstances(String patternFile, String testFile)
             throws FileNotFoundException, IOException {
         List<SidePattern> sidePatterns = patternInstanceCreator
                 .createSourcePatterns(patternFile);
         return patternInstanceCreator.createSourcePatternInstances(testFile,
                 sidePatterns);
     }
 
     private List<PairWritable3> getRules(String patternFile,
             String testFile, String hfile) throws IOException {
         List<PairWritable3> res = new ArrayList<PairWritable3>();
         Set<Rule> sourceRules = getSourceRuleInstances(patternFile, testFile);
         System.err.println("source rule size: " + sourceRules.size());
         // read the HFile and select the rules matching the source phrases
         Configuration conf = new Configuration();
         FileSystem fs = FileSystem.get(conf);
         HFile.Reader hfileReader = new HFile.Reader(fs, new Path(hfile),
                 null, false);
         hfileReader.loadFileInfo();
         HFileScanner hfileScanner = hfileReader.getScanner();
         int counter = 0;
         for (Rule rule: sourceRules) {
             RuleWritable ruleWritable = RuleWritable
                     .makeSourceMarginal(rule);
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
 
     private Set<Rule> getAsciiConstraints(String filename) throws IOException {
         Set<Rule> res = new HashSet<Rule>();
         try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
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
                                + filename);
                        System.exit(1);
                    }                    	
                     List<Integer> source = new ArrayList<Integer>();
                     List<Integer> target = new ArrayList<Integer>();
                    //for (String ss: sourceString) {
                    int i = 0;
                    while (i < sourceString.length) {
                    	if (i % patternInstanceCreator.MAX_SOURCE_PHRASE == 0 && i > 0) {
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
                             + filename);
                     System.exit(1);
                 }
             }
         }
         return res;
     }
     
     private Set<Integer> getAsciiVocab(String filename) throws IOException {
     	// TODO simplify all template writing
     	// TODO getAsciiVocab is redundant with getAsciiConstraints
         Set<Integer> res = new HashSet<>();
         try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
             String line;
             Pattern regex = Pattern.compile(".*: (.*) # (.*)");
             Matcher matcher;
             while ((line = br.readLine()) != null) {
                 matcher = regex.matcher(line);
                 if (matcher.matches()) {
                     String[] sourceString = matcher.group(1).split(" ");
                     List<Integer> source = new ArrayList<Integer>();
                     // only one word
                     if (sourceString.length == 1) {
                     	res.add(Integer.parseInt(sourceString[0]));
                     }
                 }
                 else {
                     System.err.println("Malformed ascii constraint file: "
                             + filename);
                     System.exit(1);
                 }
             }
         }
         return res;    	
     }
 
     private Set<Integer> getTestVocab(String testFile)
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
     private List<PairWritable3> getAsciiOovDeletionRules(String testFile,
             String hfile, String asciiConstraints) throws IOException {
         Set<Rule> asciiRules = getAsciiConstraints(asciiConstraints);
         Set<Integer> asciiVocab = getAsciiVocab(asciiConstraints);
         List<PairWritable3> res = new ArrayList<PairWritable3>();
         // read the HFile and select the rules matching the source phrases
         Configuration conf = new Configuration();
         FileSystem fs = FileSystem.get(conf);
         HFile.Reader hfileReader = new HFile.Reader(fs, new Path(hfile),
                 null, false);
         hfileReader.loadFileInfo();
         HFileScanner hfileScanner = hfileReader.getScanner();
         Set<Integer> testVocab = getTestVocab(testFile);
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
             //Rule asciiRule = new Rule(-1, source, source);
             //if (asciiRules.contains(asciiRule)) {
             //    continue;
             //}
             RuleWritable ruleWritable = RuleWritable
                     .makeSourceMarginal(rule);
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
 
     List<PairWritable3> getGlueRules() {
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
         return res;
     }
 
     /**
      * @param args
      * @throws IOException
      */
     public int run(String[] args) throws IOException {
         Properties p = new Properties();
         try {
             p.load(new FileInputStream(args[0]));
         }
         catch (IOException e) {
             e.printStackTrace();
             System.exit(1);
         }
         String testFile = p.getProperty("testfile");
         if (testFile == null) {
             System.err.println("Missing property 'testfile' in the config");
             System.exit(1);
         }
         Integer maxSourcePhrase = Integer.parseInt(p
                 .getProperty("max_source_phrase"));
         if (maxSourcePhrase == null) {
             System.err
                     .println("Missing property 'max_source_phrase' in the config");
         }
         String patternFile = p.getProperty("patternfile");
         if (patternFile == null) {
             System.err.println("Missing property 'patternfile' in the config");
             System.exit(1);
         }
         // read the HFile and select the rules matching the source phrases and
         // write them to a file
         String outRuleFile = p.getProperty("outrulefile");
         if (outRuleFile == null) {
             System.err.println("Missing property 'outrulefile' in the config");
             System.exit(1);
         }
         String hfile = p.getProperty("hfile");
         if (hfile == null) {
             System.err.println("Missing property 'hfile' in the config");
             System.exit(1);
         }
         List<PairWritable3> rules = getRules(patternFile, testFile, hfile);
         String source2TargetLexicalModel =
                 p.getProperty("source2target_lexical_model");
         if (source2TargetLexicalModel == null) {
             System.err.println("Missing property " +
                     "'source2target_lexical_model' in the config");
             System.exit(1);
         }
         String target2SourceLexicalModel =
                 p.getProperty("target2source_lexical_model");
         if (target2SourceLexicalModel == null) {
             System.err.println("Missing property " +
                     "'target2source_lexical_model' in the config");
             System.exit(1);
         }
         String selectedFeaturesString = p.getProperty("features");
         if (selectedFeaturesString == null) {
             System.err.println("Missing property 'features' in the config");
             System.exit(1);
         }
         String[] selectedFeatures = selectedFeaturesString.split(",");
         FeatureCreator featureCreator =
                 new FeatureCreator(source2TargetLexicalModel,
                         target2SourceLexicalModel, rules, selectedFeatures);
         List<PairWritable3> rulesWithFeatures =
                 featureCreator.createFeatures(rules);
         String asciiConstraints = p.getProperty("ascii_constraints");
         if (asciiConstraints == null) {
             System.err
                     .println("Missing property 'ascii_constraints' in the config");
             System.exit(1);
         }
         List<PairWritable3> asciiOovDeletionRules = getAsciiOovDeletionRules(testFile, hfile, asciiConstraints);
         List<PairWritable3> asciiOovDeletionRulesWithFeatures =
                 featureCreator
                         .createFeaturesAsciiOovDeletion(asciiOovDeletionRules);
         List<PairWritable3> glueRules = getGlueRules();
         List<PairWritable3> glueRulesWithFeatures =
                 featureCreator.createFeaturesGlueRules(glueRules);
         try (BufferedOutputStream bos =
                 new BufferedOutputStream(new GZIPOutputStream(
                         new FileOutputStream(outRuleFile)))) {
             // TODO should be called only once
             Set<Rule> asciiRules = getAsciiConstraints(asciiConstraints);
             for (PairWritable3 ruleWithFeatures: rulesWithFeatures) {
                 // bw.write((ruleWithFeatures.toString() + "\n").getBytes());
                 // check if rule is not an ascii rule
                 Rule checkNotAscii = new Rule(-1, ruleWithFeatures.first);
                 if (asciiRules.contains(checkNotAscii)) {
                     // this rule will be included as an ascii rule, don't
                     // include it here
                     System.err.println("Ascii rule has been extracted: "
                             + checkNotAscii.toString());
                     continue;
                 }
                 bos.write(ruleWithFeatures.first.toString().getBytes());
                 Writable[] features = ruleWithFeatures.second.get();
                 for (Writable w: features) {
                     bos.write((" " + w.toString()).getBytes());
                 }
                 bos.write("\n".getBytes());
             }
             for (PairWritable3 asciiOovDeletionRuleWithFeatures: asciiOovDeletionRulesWithFeatures) {
                 bos.write(asciiOovDeletionRuleWithFeatures.first.toString()
                         .getBytes());
                 Writable[] features =
                         asciiOovDeletionRuleWithFeatures.second.get();
                 for (Writable w: features) {
                     bos.write((" " + w.toString()).getBytes());
                 }
                 bos.write("\n".getBytes());
             }
             for (PairWritable3 glueRuleWithFeatures: glueRulesWithFeatures) {
                 bos.write(glueRuleWithFeatures.first.toString().getBytes());
                 Writable[] features = glueRuleWithFeatures.second.get();
                 for (Writable w: features) {
                     bos.write((" " + w.toString()).getBytes());
                 }
                 bos.write("\n".getBytes());
             }
         }
         return 0;
     }
     
     public static void main(String[] args) throws Exception {
         if (args.length != 1) {
             System.err.println("Usage args: configFile");
         }
         int res = ToolRunner.run(new RuleFileBuilder(args[0]), args);
         System.exit(res);
     }
 
 }
