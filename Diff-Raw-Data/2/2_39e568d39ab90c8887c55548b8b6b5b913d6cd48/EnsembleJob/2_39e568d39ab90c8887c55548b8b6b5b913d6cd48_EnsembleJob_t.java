 package de.tuberlin.dima.ml.pact.logreg.ensemble;
 
 import de.tuberlin.dima.ml.pact.types.PactVector;
 import eu.stratosphere.pact.common.contract.CrossContract;
 import eu.stratosphere.pact.common.contract.FileDataSink;
 import eu.stratosphere.pact.common.contract.FileDataSource;
 import eu.stratosphere.pact.common.contract.MapContract;
 import eu.stratosphere.pact.common.contract.ReduceContract;
 import eu.stratosphere.pact.common.io.RecordOutputFormat;
 import eu.stratosphere.pact.common.io.TextInputFormat;
 import eu.stratosphere.pact.common.plan.Plan;
 import eu.stratosphere.pact.common.plan.PlanAssembler;
 import eu.stratosphere.pact.common.plan.PlanAssemblerDescription;
 import eu.stratosphere.pact.common.type.base.PactInteger;
 import eu.stratosphere.pact.common.type.base.PactString;
 
 /**
  * Implements Ensemble Training for Stratosphere-ozone
  * 
  * Uses Mahout SGD to train the model for the individual partitions
  * 
  * Train -> Map -> Reduce -> [(1, i, wi)] -> Reduce(combine) -> (1, 3, w1, w2, w3)
  *  
  *        -> Cross-Eval -> (model-id, partial-total, partial-correct)
  *  Test  /
  *  
  *  -> Reduce-sum -> (model-id, total, correct)
  * 
  * How to run on a cluster:
  * .../bin/pact-client.sh -w -j job.jar -a 1 [job args]
  * 
  * @author André Hacker
  */
 public class EnsembleJob implements PlanAssembler, PlanAssemblerDescription {
   
   static final String CONF_KEY_NUM_PARTITIONS = "parameter.NUM_PARTITIONS";
   static final String CONF_KEY_NUM_FEATURES = "parameter.NUM_FEATURES";
   
   static final int ID_TRAIN_IN_PARTITION = 0;
   static final int ID_TRAIN_IN_VECTOR = 1;
   static final int ID_TRAIN_IN_LABEL = 2;
   
   static final int ID_COMBINE_IN_MODEL_ID = 0;
   static final int ID_COMBINE_IN_PARTITION = 1;
   static final int ID_COMBINE_IN_MODEL = 2;
   
   static final int ID_EVAL_IN_MODEL_ID = 0;
   static final int ID_EVAL_IN_NUM_MODELS = 1;
   static final int ID_EVAL_IN_FIRST_MODEL = 2;
   
   static final int ID_EVAL_OUT_MODEL_ID = 0;
   static final int ID_EVAL_OUT_TOTAL = 1;
   static final int ID_EVAL_OUT_CORRECT = 2;
 
   static final int ID_OUT_MODEL_ID = 0;
   static final int ID_OUT_TOTAL = 1;
   static final int ID_OUT_CORRECT = 2;
 
   @Override
   public String getDescription() {
     return "Parameters: [numPartitions] [inputPathTrain] [inputPathTest] [outputPath] [numFeatures] [runValidation (0 or 1)]";
   }
   
   @Override
   public Plan getPlan(String... args) {
     
     // parse job parameters
     if (args.length < 6) return null;
     int numPartitions  = Integer.parseInt(args[0]);
     String inputPathTrain = args[1];
     String inputPathTest = args[2];
     String outputPath = args[3];
     int numFeatures =  Integer.parseInt(args[4]);
    boolean runValidation = (args[5].equals("1")) ? true : false;
 
     FileDataSource source = new FileDataSource(TextInputFormat.class, inputPathTrain, "Train Input");
     source.setParameter(TextInputFormat.CHARSET_NAME, "ASCII");     // comment out this line for UTF-8 inputs
     
     MapContract mapRandomPartitioning = MapContract.builder(MapRandomPartitioning.class)
         .input(source)
         .name("Map: Random Partitioning")
         .build();
     mapRandomPartitioning.getParameters().setInteger(CONF_KEY_NUM_PARTITIONS, numPartitions);
     mapRandomPartitioning.getParameters().setInteger(CONF_KEY_NUM_FEATURES, numFeatures);
     
     ReduceContract reduceTrain = new ReduceContract.Builder(ReduceTrainPartition.class, PactInteger.class, 0)
         .input(mapRandomPartitioning)
         .name("Reduce: Train Partitions")
         .build();
     reduceTrain.getParameters().setInteger(CONF_KEY_NUM_FEATURES, numFeatures);
     
     ReduceContract reduceCombineModel = new ReduceContract.Builder(ReduceCombineModel.class, PactInteger.class, 0)
         .input(reduceTrain)
         .name("Reduce: Combine models to a single model")
         .build();
     
     FileDataSink out = null;
     
     if (runValidation) {
 
       FileDataSource sourceTest = new FileDataSource(TextInputFormat.class, inputPathTest, "Test Input");
       sourceTest.setParameter(TextInputFormat.CHARSET_NAME, "ASCII");
 
       CrossContract crossEval = CrossContract.builder(CrossEval.class)
           .input1(reduceCombineModel)
           .input2(sourceTest)
           .name("Cross: Evaluation")
           .build();
       crossEval.getParameters().setInteger(CONF_KEY_NUM_FEATURES, numFeatures);
       
       ReduceContract reduceEvalSum = new ReduceContract.Builder(ReduceEvalSum.class, PactString.class, ID_EVAL_OUT_MODEL_ID)
       .input(crossEval)
       .name("Reduce: Eval Sum Up")
       .build();
 
       out = new FileDataSink(RecordOutputFormat.class, outputPath, reduceEvalSum, "Ensemble Validation");
       RecordOutputFormat.configureRecordFormat(out)
       .recordDelimiter('\n')
       .fieldDelimiter(' ')
       .field(PactInteger.class, 0)
       .field(PactVector.class, 1);
       
     } else {    // if (runValidation)
       
       out = new FileDataSink(RecordOutputFormat.class, outputPath, reduceCombineModel, "Ensemble Models");
       RecordOutputFormat.configureRecordFormat(out)
       .recordDelimiter('\n')
       .fieldDelimiter(' ')
       .field(PactInteger.class, 0)      // Model id
       .field(PactInteger.class, 1)       // Total
       .field(PactInteger.class, 2);      // Correct
     }
     
     Plan plan = new Plan(out, "WordCount Example");
     plan.setDefaultParallelism(numPartitions);
     
     return plan;
   }
 
 }
