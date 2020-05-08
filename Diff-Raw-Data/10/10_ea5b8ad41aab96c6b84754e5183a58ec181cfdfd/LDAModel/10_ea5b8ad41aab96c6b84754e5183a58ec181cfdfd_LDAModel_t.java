 package com.thomasdimson.wikipedia.lda.java;
 
 import cc.mallet.pipe.*;
 import cc.mallet.topics.ParallelTopicModel;
 import cc.mallet.types.Instance;
 import cc.mallet.types.InstanceList;
 import com.google.common.base.Joiner;
 import com.google.common.base.Splitter;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.thomasdimson.wikipedia.Data;
 
 import java.io.*;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 public class LDAModel {
     public static final Pipe INSTANCE_PIPE;
 
     static {
         List<Pipe> pipeList = Lists.newArrayList();
         Pattern splitPattern = Pattern.compile("[^ ]+");
         pipeList.add(new CharSequence2TokenSequence(splitPattern));
         pipeList.add(new TokenSequence2FeatureSequence());
         INSTANCE_PIPE = new SerialPipes(pipeList);
     }
 
     public static Instance malletInstanceFromDumpPage(Data.DumpPage page) {
         String target = Double.toString(page.getId());
         return new Instance(page.getText(), target, page.getTitle(), page.getTitle());
     }
 
     public static ParallelTopicModel initializeTopicModel(String filename, int numTopics) throws IOException {
         InstanceList instanceList = new InstanceList(INSTANCE_PIPE);
         Iterator<Data.DumpPage> it = WikipediaHandler.newStructuredDumpIterator(filename);
         System.err.println("Loading data");
         while(it.hasNext()) {
             Data.DumpPage p = it.next();
             instanceList.addThruPipe(malletInstanceFromDumpPage(p));
         }
 
         ParallelTopicModel model = new ParallelTopicModel(numTopics, 0.01 * numTopics, ParallelTopicModel.DEFAULT_BETA);
         model.addInstances(instanceList);
         model.setNumIterations(1000);
         model.setNumThreads(Runtime.getRuntime().availableProcessors());
         return model;
     }
 
     public static ParallelTopicModel createFromStructuredDump(String filename, int numTopics, String modelFilePrefix) throws IOException {
         ParallelTopicModel model = initializeTopicModel(filename, numTopics);
         if(modelFilePrefix != null) {
             model.setSaveSerializedModel(250, modelFilePrefix);
         }
         System.err.println("Done! Starting estimation");
         model.estimate();
 
         System.err.println("Topics:\n");
         Object[][] topicWords = model.getTopWords(50);
         for(int i = 0; i < topicWords.length; i++) {
             System.err.println("\tTopic" + i + ": " + Joiner.on(", ").join(topicWords[i]));
         }
         return model;
     }
 
     public static void writeFromModel(String modelFile, int numTopics, String documentTopicFile, String wordFile) throws Exception {
         System.err.println("Reading topic model");
         ParallelTopicModel model = ParallelTopicModel.read(new File(modelFile));
         System.err.println("Writing document topics");
         model.printDocumentTopics(new File(documentTopicFile));
         System.err.println("Writing topic words");
         model.printTopicWordWeights(new File(wordFile));
     }
 
     public static void writeAverageTokenProbability(String wordModelFile, String structuredDumpFile, String outputName, int numTopics) throws IOException {
         Pattern splitPattern = Pattern.compile("\\s+", Pattern.MULTILINE);
         BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputName), Charset.forName("UTF-8")));
         try {
             System.err.println("Reading word probabilities");
             Map<String, double []> wordProbability = readWordLDAMap(wordModelFile, numTopics);
             System.err.println("Calculating document likelihood");
             Iterator<Data.DumpPage> it = WikipediaHandler.newStructuredDumpIterator(structuredDumpFile);
             int num = 0;
             while(it.hasNext()) {
                 Data.DumpPage p = it.next();
                 String[] split = splitPattern.split(p.getText());
                 double []averages = new double[numTopics];
                 for(String word : split) {
                     double[] probs = wordProbability.get(word);
                     if(probs != null) {
                         for(int i = 0; i < numTopics; i++) {
                             averages[i] += probs[i];
                         }
                     }
                 }
 
                 if(split.length > 0) {
                     for(int i = 0; i <numTopics; i++) {
                         averages[i] /= split.length;
                     }
                 }
 
                 w.write(Integer.toString(num));
                w.write("\t");
                 w.write(p.getTitle());
                 for(int i = 0; i < numTopics; i++) {
                    w.write("\t");
                     w.write(Integer.toString(i));
                    w.write("\t");
                     w.write(Double.toString(averages[i]));
                 }
                w.write("\n");
 
                 num++;
             }
         } finally {
             w.close();
         }
     }
 
     public static Map<String, double[]> readWordLDAMap(String filename, int numTopics) throws IOException {
         BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(filename), Charset.forName("UTF-8")));
         String line;
         Splitter splitter = Splitter.on("\t").omitEmptyStrings().trimResults();
         Map<String, double[]> ret = Maps.newHashMapWithExpectedSize(1000000);
         int lineNum = 0;
         System.out.println();
 
         double []gibbsConstants = new double[numTopics];
 
         while((line = r.readLine()) != null) {
             if(line.startsWith("#"))  {
                 continue;
             }
 
             List<String> split = splitter.splitToList(line);
             if(split.size() != 3) {
                 System.out.println("Bad line: " + line);
                 continue;
             }
 
             int topic = Integer.valueOf(split.get(0));
             String word = split.get(1);
             double unnormalizedProbability = Double.valueOf(split.get(2));
 
             double[] probs = ret.get(word);
             if(probs == null) {
                 probs = new double[numTopics];
                 ret.put(word, probs);
             }
 
             probs[topic] += unnormalizedProbability;
             gibbsConstants[topic] += unnormalizedProbability;
 
 
             lineNum++;
             if(lineNum % 10000 == 0) {
                 System.out.print("Reached line " + lineNum + "      \r");
                 System.out.flush();
             }
         }
 
         for(Map.Entry<String, double[]> wordProbs : ret.entrySet()) {
             double[]probs = wordProbs.getValue();
             for(int i = 0; i < probs.length; i++) {
                 probs[i] /= gibbsConstants[i];
             }
         }
 
         return ret;
     }
 }
