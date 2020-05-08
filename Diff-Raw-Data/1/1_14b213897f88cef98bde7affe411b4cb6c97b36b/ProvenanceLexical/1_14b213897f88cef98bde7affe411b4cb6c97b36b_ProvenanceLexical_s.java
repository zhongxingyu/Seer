 /**
  * 
  */
 
 package uk.ac.cam.eng.rulebuilding.features;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.FutureTask;
 
 import org.apache.hadoop.io.ArrayWritable;
 
 import uk.ac.cam.eng.extraction.datatypes.Rule;
 import uk.ac.cam.eng.extraction.hadoop.datatypes.PairWritable3;
 
 /**
  * @author jmp84
  */
 public class ProvenanceLexical implements Feature {
 
     private static class LexProbS2TTask implements
             Callable<Source2TargetLexicalProbability> {
 
         private String lexicalModelFile;
         private List<PairWritable3> rules;
 
         /**
          * @param lexicalModelFile
          * @param rules
          */
         public LexProbS2TTask(String lexicalModelFile, List<PairWritable3> rules) {
             super();
             this.lexicalModelFile = lexicalModelFile;
             this.rules = rules;
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see java.util.concurrent.Callable#call()
          */
         @Override
         public Source2TargetLexicalProbability call() throws Exception {
             // TODO Auto-generated method stub
             return new Source2TargetLexicalProbability(lexicalModelFile, rules);
         }
     }
 
     private static class LexProbS2TValueTask implements Callable<List<Double>> {
 
         private Source2TargetLexicalProbability lexModel;
         private Rule rule;
         private ArrayWritable mapReduceFeatures;
 
         /**
          * @param lexicalModelFile
          * @param rules
          */
         public LexProbS2TValueTask(Source2TargetLexicalProbability lexModel,
                 Rule rule, ArrayWritable mapReduceFeatures) {
             super();
             this.lexModel = lexModel;
             this.rule = rule;
             this.mapReduceFeatures = mapReduceFeatures;
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see java.util.concurrent.Callable#call()
          */
         @Override
         public List<Double> call() throws Exception {
             // TODO Auto-generated method stub
             return lexModel.value(rule, mapReduceFeatures);
         }
     }
 
     private static class LexProbT2STask implements
             Callable<Target2SourceLexicalProbability> {
 
         private String lexicalModelFile;
         private List<PairWritable3> rules;
 
         /**
          * @param lexicalModelFile
          * @param rules
          */
         public LexProbT2STask(String lexicalModelFile, List<PairWritable3> rules) {
             super();
             this.lexicalModelFile = lexicalModelFile;
             this.rules = rules;
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see java.util.concurrent.Callable#call()
          */
         @Override
         public Target2SourceLexicalProbability call() throws Exception {
             // TODO Auto-generated method stub
             return new Target2SourceLexicalProbability(lexicalModelFile, rules);
         }
     }
 
     private static class LexProbT2SValueTask implements Callable<List<Double>> {
 
         private Target2SourceLexicalProbability lexModel;
         private Rule rule;
         private ArrayWritable mapReduceFeatures;
 
         /**
          * @param lexicalModelFile
          * @param rules
          */
         public LexProbT2SValueTask(Target2SourceLexicalProbability lexModel,
                 Rule rule, ArrayWritable mapReduceFeatures) {
             super();
             this.lexModel = lexModel;
             this.rule = rule;
             this.mapReduceFeatures = mapReduceFeatures;
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see java.util.concurrent.Callable#call()
          */
         @Override
         public List<Double> call() throws Exception {
             // TODO Auto-generated method stub
             return lexModel.value(rule, mapReduceFeatures);
         }
     }
 
     /**
      * List of source-to-target and target-to-source models 1
      */
     private List<Source2TargetLexicalProbability> source2targetModels;
     private List<Target2SourceLexicalProbability> target2sourceModels;
 
     public ProvenanceLexical(String[] source2targetModelFiles,
             String[] target2sourceModelFiles, List<PairWritable3> rules)
             throws InterruptedException, ExecutionException {
         if (source2targetModelFiles.length != target2sourceModelFiles.length) {
             System.err.println("ERROR: different number of source-to-target "
                     + "and target-to-source lexical models: ");
             System.exit(1);
         }
         source2targetModels = new ArrayList<>();
         target2sourceModels = new ArrayList<>();
         // TODO remove hard code
         ExecutorService threadPool = Executors.newFixedThreadPool(22);
         List<FutureTask<Source2TargetLexicalProbability>> s2t =
                 new ArrayList<>();
         List<FutureTask<Target2SourceLexicalProbability>> t2s =
                 new ArrayList<>();
         for (int i = 0; i < source2targetModelFiles.length; i++) {
             FutureTask<Source2TargetLexicalProbability> s2tTask =
                     new FutureTask<>(new LexProbS2TTask(
                             source2targetModelFiles[i], rules));
             FutureTask<Target2SourceLexicalProbability> t2sTask =
                     new FutureTask<>(new LexProbT2STask(
                             target2sourceModelFiles[i], rules));
             threadPool.execute(s2tTask);
             threadPool.execute(t2sTask);
             s2t.add(s2tTask);
             t2s.add(t2sTask);
         }
         for (FutureTask<Source2TargetLexicalProbability> s2tTask : s2t) {
             source2targetModels.add(s2tTask.get());
         }
         for (FutureTask<Target2SourceLexicalProbability> t2sTask : t2s) {
             target2sourceModels.add(t2sTask.get());
         }
         threadPool.shutdown();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * uk.ac.cam.eng.rulebuilding.features.Feature#value(uk.ac.cam.eng.extraction
      * .datatypes.Rule, org.apache.hadoop.io.ArrayWritable)
      */
     @Override
     public List<Double> value(Rule r, ArrayWritable mapReduceFeatures)
             throws InterruptedException, ExecutionException {
         List<Double> res = new ArrayList<>();
         // TODO remove hard code
         ExecutorService threadPool = Executors.newFixedThreadPool(22);
         List<FutureTask<List<Double>>> s2t = new ArrayList<>();
         List<FutureTask<List<Double>>> t2s = new ArrayList<>();
         for (int i = 0; i < source2targetModels.size(); i++) {
             FutureTask<List<Double>> s2tTask =
                     new FutureTask<>(new LexProbS2TValueTask(
                             source2targetModels.get(i), r, mapReduceFeatures));
             FutureTask<List<Double>> t2sTask =
                     new FutureTask<>(new LexProbT2SValueTask(
                             target2sourceModels.get(i), r, mapReduceFeatures));
             threadPool.execute(s2tTask);
             threadPool.execute(t2sTask);
             s2t.add(s2tTask);
             t2s.add(t2sTask);
         }
         for (FutureTask<List<Double>> s2tTask : s2t) {
             res.addAll(s2tTask.get());
         }
         for (FutureTask<List<Double>> t2sTask : t2s) {
             res.addAll(t2sTask.get());
         }
         return res;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * uk.ac.cam.eng.rulebuilding.features.Feature#valueAsciiOovDeletion(uk.
      * ac.cam.eng.extraction.datatypes.Rule, org.apache.hadoop.io.ArrayWritable)
      */
     @Override
     public List<Double> valueAsciiOovDeletion(Rule r,
             ArrayWritable mapReduceFeatures) {
         List<Double> res = new ArrayList<>();
         for (int i = 0; i < source2targetModels.size(); i++) {
             res.addAll(source2targetModels.get(i).valueAsciiOovDeletion(r,
                     mapReduceFeatures));
             res.addAll(target2sourceModels.get(i).valueAsciiOovDeletion(r,
                     mapReduceFeatures));
         }
         return res;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see uk.ac.cam.eng.rulebuilding.features.Feature#valueGlue(uk.ac.cam.eng.
      * extraction.datatypes.Rule, org.apache.hadoop.io.ArrayWritable)
      */
     @Override
     public List<Double> valueGlue(Rule r, ArrayWritable mapReduceFeatures) {
         List<Double> res = new ArrayList<>();
         for (int i = 0; i < source2targetModels.size(); i++) {
             res.addAll(source2targetModels.get(i).valueGlue(r,
                     mapReduceFeatures));
             res.addAll(target2sourceModels.get(i).valueGlue(r,
                     mapReduceFeatures));
         }
         return res;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see uk.ac.cam.eng.rulebuilding.features.Feature#getNumberOfFeatures()
      */
     @Override
     public int getNumberOfFeatures() {
         return source2targetModels.size() + target2sourceModels.size();
     }
 }
