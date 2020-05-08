 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.mahout.classifier.rbm.training;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import org.apache.commons.cli2.builder.DefaultOptionBuilder;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
 import org.apache.hadoop.util.ToolRunner;
 import org.apache.mahout.classifier.rbm.RBMClassifier;
 import org.apache.mahout.classifier.rbm.model.LabeledSimpleRBM;
 import org.apache.mahout.classifier.rbm.model.RBMModel;
 import org.apache.mahout.classifier.rbm.model.SimpleRBM;
 import org.apache.mahout.classifier.rbm.network.DeepBoltzmannMachine;
 import org.apache.mahout.common.AbstractJob;
 import org.apache.mahout.common.HadoopUtil;
 import org.apache.mahout.common.Pair;
 import org.apache.mahout.common.commandline.DefaultOptionCreator;
 import org.apache.mahout.common.iterator.sequencefile.PathFilters;
 import org.apache.mahout.common.iterator.sequencefile.PathType;
 import org.apache.mahout.common.iterator.sequencefile.SequenceFileDirIterable;
 import org.apache.mahout.common.iterator.sequencefile.SequenceFileIterable;
 import org.apache.mahout.math.DenseMatrix;
 import org.apache.mahout.math.DenseVector;
 import org.apache.mahout.math.Matrix;
 import org.apache.mahout.math.MatrixWritable;
 import org.apache.mahout.math.Vector;
 import org.apache.mahout.math.VectorWritable;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The Class RBMClassifierTrainingJob.
  */
 public class RBMClassifierTrainingJob extends AbstractJob{
 
 	/** The Constant WEIGHT_UPDATES. */
 	public static final String WEIGHT_UPDATES = "weightupdates";
 	
 	/** The Constant logger. */
 	private static final Logger logger = LoggerFactory.getLogger(RBMClassifierTrainingJob.class);
 	
 	/** The last update which is needed for use of the momentum. */
 	Matrix[] lastUpdate;
 	
 	/** The rbm classifier. */
 	RBMClassifier rbmCl=null;
 	
 	/** The number of iterations (epochs). */
 	int epochs;
     
     /** The learningrate. */
     double learningrate;
     
     /** The momentum used. */
     double momentum;
     
     /** monitor if true. */
     boolean monitor;
     
     /** initial biases if true. */
     boolean initbiases;
     
     /** train greedy if true. */
     boolean greedy;
     
     /** finetune if true. */
     boolean finetuning;
     
     /** The batches to train on. */
     Path[] batches = null;
     
     /** The labelcount. */
     int labelcount;
     
     /** The nr gibbs sampling. */
     int nrGibbsSampling;
     
     /** The rbm nr to train. */
     int rbmNrtoTrain;
 	
 	/**
 	 * The main method.
 	 *
 	 * @param args the arguments
 	 * @throws Exception the exception
 	 */
 	public static void main(String[] args) throws Exception {
 	    ToolRunner.run(new Configuration(), new RBMClassifierTrainingJob(), args);
 	  }
 	
 	/* (non-Javadoc)
 	 * @see org.apache.hadoop.util.Tool#run(java.lang.String[])
 	 */
 	@Override
 	public int run(String[] args) throws Exception {		
 		addInputOption();
 	    addOutputOption();
 	    addOption("epochs","e","number of training epochs through the trainingset",true);
 	    addOption("structure", "s", "comma-separated list of layer sizes", false);
 	    addOption("labelcount", "lc", "total count of labels existent in the training set", true);
 	    addOption("learningrate", "lr", "learning rate at the beginning of training", "0.005");
 	    addOption("momentum", "m", "momentum of learning at the beginning", "0.5");
 	    addOption("rbmnr", "nr", "rbm to train, < 0 means train all", "-1");
 	    addOption("nrgibbs", "gn", "number of gibbs sampling used in contrastive divergence", "5");
 	    addOption(new DefaultOptionBuilder()
 					.withLongName(DefaultOptionCreator.MAPREDUCE_METHOD)
 					.withRequired(false)
 					.withDescription("Run training with map/reduce")
 					.withShortName("mr").create());
 	    addOption(new DefaultOptionBuilder()
 					.withLongName("nogreedy")
 					.withRequired(false)
 					.withDescription("Don't run greedy pre training")
 					.withShortName("ng").create());
 	    addOption(new DefaultOptionBuilder()
 					.withLongName("nofinetuning")
 					.withRequired(false)
 					.withDescription("Don't run fine tuning at the end")
 					.withShortName("nf").create());
 	    addOption(new DefaultOptionBuilder()
 					.withLongName("nobiases")
 					.withRequired(false)
 					.withDescription("Don't initialize biases")
 					.withShortName("nb").create());
 	    addOption(new DefaultOptionBuilder()
         			.withLongName("monitor")
         			.withRequired(false)
         			.withDescription("If present, errors can be monitored in cosole")
         			.withShortName("mon").create());
 	    addOption(DefaultOptionCreator.overwriteOption().create());
 	    
 	    Map<String, String> parsedArgs = parseArguments(args);
 	    if (parsedArgs == null) {
 	      return -1;
 	    }
 
 	    Path input = getInputPath();
 	    Path output = getOutputPath();
 	    FileSystem fs = FileSystem.get(output.toUri(),getConf());
 	    labelcount = Integer.parseInt(getOption("labelcount"));
 
 	    boolean local = !hasOption("mapreduce");
 	    monitor = hasOption("monitor");
 	    initbiases = !hasOption("nobiases");
 	    finetuning = !hasOption("nofinetuning");
 	    greedy = !hasOption("nogreedy");
 	    
 	    if(fs.isFile(input))
 	    	batches = new Path[]{input};
 	    else {
 	    	FileStatus[] stati = fs.listStatus(input);
 	    	batches = new Path[stati.length];
 	    	for (int i = 0; i < stati.length; i++) {
 				batches[i] = stati[i].getPath();
 			}	    		
 	    }
 
 	    epochs = Integer.valueOf(getOption("epochs"));
     	learningrate = Double.parseDouble(getOption("learningrate"));	    
     	momentum = Double.parseDouble(getOption("momentum"));
     	rbmNrtoTrain = Integer.parseInt(getOption("rbmnr"));
     	nrGibbsSampling = Integer.parseInt(getOption("nrgibbs"));	    
 	    
     	boolean initialize = hasOption(DefaultOptionCreator.OVERWRITE_OPTION)||!fs.exists(output)||fs.listStatus(output).length<=0;
 	    
     	if (initialize) {
    	      String structure = getOption("structure");	      
 	      if(structure==null||structure.isEmpty())
 	    	  return -1;
 	      
 	      String[] layers = structure.split(",");	      
 	      if (layers.length<2) {
 			return -1;
 	      }
 	      
 	      int[] actualLayerSizes = new int[layers.length];			
 	      for (int i = 0; i < layers.length; i++) {
 	    	 actualLayerSizes[i] = Integer.parseInt(layers[i]);
 	      }
 	      
 	      rbmCl = new RBMClassifier(labelcount, actualLayerSizes);
 	      logger.info("New model initialized!");
 	    } else {
 	    	rbmCl = RBMClassifier.materialize(output, getConf());
 	    	logger.info("Model found and materialized!");
 	    }
 	    
 	    HadoopUtil.setSerializations(getConf());	    
 	    lastUpdate = new Matrix[rbmCl.getDbm().getRbmCount()];
 	    
 	    if(initbiases) {
 		    //init biases!
 		    Vector biases = null;
 		    int counter = 0;
 		    for(Path batch : batches) {
 		    	for (Pair<IntWritable, VectorWritable> record : new SequenceFileIterable<IntWritable, VectorWritable>(batch, getConf())) {
 		    		if(biases==null)
 		    			biases = record.getSecond().get().clone();
 		    		else
 		    			biases.plus(record.getSecond().get());
 		    		counter++;
 		    	}
 		    }
		    if(biases==null) {
			    logger.info("No training data found!");
			    return -1;
		    }
		    
 		    rbmCl.getDbm().getLayer(0).setBiases(biases.divide(counter));
 		    logger.info("Biases initialized");
 	    }
 	    
 	    //greedy pre training with gradually decreasing learningrates
 	    if(greedy) {	    	
 	    	if(!local)
 	    		rbmCl.serialize(output, getConf());
 	    		
 	    	double tempLearningrate = learningrate;
 	    	if(rbmNrtoTrain<0)
 			   //train all rbms
 			    for(int rbmNr=0; rbmNr<rbmCl.getDbm().getRbmCount(); rbmNr++) {
 			    	tempLearningrate = learningrate;
 			    	
 			    	//double weights if dbm was materialized, because it was halved after greedy pretraining
 			    	if(!initialize&&rbmNrtoTrain>0&&rbmNrtoTrain<rbmCl.getDbm().getRbmCount()-1) {
 			    		((SimpleRBM)rbmCl.getDbm().getRBM(rbmNr)).setWeightMatrix(
 			    				((SimpleRBM)rbmCl.getDbm().getRBM(rbmNr)).getWeightMatrix().times(2));
 			    	}
 			    	
 				    for (int j = 0; j < epochs; j++) {
 						logger.info("Greedy training, epoch "+(j+1)+"\nCurrent learningrate: "+tempLearningrate);
 				    	for(int b=0; b<batches.length;b++) {			
 					    	tempLearningrate -= learningrate/(epochs*batches.length+epochs);
 					    	if(local) {
 					    		if(!trainGreedySeq(rbmNr, batches[b], j, tempLearningrate))
 					    			return -1; 
 					    	}
 					    	else
 							    if(!trainGreedyMR(rbmNr, batches[b], j, tempLearningrate))
 							    	return -1;
 					    	if(monitor&&(batches.length>19)&&(b+1)%(batches.length/20)==0)
 					    		logger.info(rbmNr+"-RBM: "+Math.round(((double)b+1)/batches.length*100.0)+"% in epoch done!");
 					    }
 				    	logger.info(Math.round(((double)j+1)/epochs*100)+"% of training on rbm number "+rbmNr+" is done!");
 
 					    if(monitor) {
 							double error = rbmError(batches[0], rbmNr);
 							logger.info("Average reconstruction error on batch "+batches[0].getName()+": "+error);
 						}
 					    
 					    rbmCl.serialize(output, getConf());
 					}    	
 			    	
 			    	//weight normalization to avoid double counting
 			    	if(rbmNr>0&&rbmNr<rbmCl.getDbm().getRbmCount()-1) {
 			    		((SimpleRBM)rbmCl.getDbm().getRBM(rbmNrtoTrain)).setWeightMatrix(
 			    				((SimpleRBM)rbmCl.getDbm().getRBM(rbmNrtoTrain)).getWeightMatrix().times(0.5));
 			    	}
 			    }
 	    	else {
 	    		//double weights if dbm was materialized, because it was halved after greedy pretraining
 		    	if(!initialize&&rbmNrtoTrain>0&&rbmNrtoTrain<rbmCl.getDbm().getRbmCount()-1) {
 		    		((SimpleRBM)rbmCl.getDbm().getRBM(rbmNrtoTrain)).setWeightMatrix(
 		    				((SimpleRBM)rbmCl.getDbm().getRBM(rbmNrtoTrain)).getWeightMatrix().times(2));
 		    	}
 	    		//train just wanted rbm
 			    for (int j = 0; j < epochs; j++) {
 					logger.info("Greedy training, epoch "+(j+1)+"\nCurrent learningrate: "+tempLearningrate);
 		    		for(int b=0; b<batches.length;b++) {		
 				    	tempLearningrate -= learningrate/(epochs*batches.length+epochs);
 				    	if(local) {
 				    		if(!trainGreedySeq(rbmNrtoTrain, batches[b], j,tempLearningrate))
 						    	return -1; 
 				    	}
 				    	else
 						    if(!trainGreedyMR(rbmNrtoTrain, batches[b], j,tempLearningrate))
 						    	return -1;				
 				    	if(monitor&&(batches.length>19)&&(b+1)%(batches.length/20)==0)
 				    		logger.info(rbmNrtoTrain+"-RBM: "+Math.round(((double)b+1)/batches.length*100.0)+"% in epoch done!");
 					    }
 			    	logger.info(Math.round(((double)j+1)/epochs*100)+"% of training is done!");
 
 			    	if(monitor) {
 						double error = rbmError(batches[0], rbmNrtoTrain);
 						logger.info("Average reconstruction error on batch "+batches[0].getName()+": "+error);
 					}
 	    		}
 	    		
 			    //weight normalization to avoid double counting
 		    	if(rbmNrtoTrain>0&&rbmNrtoTrain<rbmCl.getDbm().getRbmCount()-1) {
 		    		((SimpleRBM)rbmCl.getDbm().getRBM(rbmNrtoTrain)).setWeightMatrix(
 		    				((SimpleRBM)rbmCl.getDbm().getRBM(rbmNrtoTrain)).getWeightMatrix().times(0.5));
 		    	}
 	    	}
 
 		    rbmCl.serialize(output, getConf());
 		    logger.info("Pretraining done and model written to output");
 	    }
 	    
 	    if(finetuning) {	    	
 	    	DeepBoltzmannMachine multiLayerDbm  = null;
 	    	
 	    	double tempLearningrate = learningrate;
 		    //finetuning job
 		    for (int j = 0; j < epochs; j++) {
 		    	for(int b=0; b<batches.length;b++) {
 		    		multiLayerDbm = rbmCl.initializeMultiLayerNN();
 		    		logger.info("Finetuning on batch "+batches[b].getName()+"\nCurrent learningrate: "+tempLearningrate);
 			    	tempLearningrate -= learningrate/(epochs*batches.length+epochs);
 			    	if(local) {
 			    		if(!finetuneSeq(batches[b], j, multiLayerDbm, tempLearningrate))
 			    			return -1;
 			    	}
 			    	else
 			    		if(!fintuneMR(batches[b], j, tempLearningrate))
 			    			return -1;		
 			    	logger.info("Finetuning: "+Math.round(((double)b+1)/batches.length*100.0)+"% in epoch done!");
 			    }			    
 		    	logger.info(Math.round(((double)j+1)/epochs*100)+"% of training is done!");
 
 
 			    if(monitor) {
 		    		double error = feedForwardError(multiLayerDbm, batches[0]);
 					logger.info("Average discriminative error on batch "+batches[0].getName()+": "+error);
 		    	}
 		    }
 		    //final serialization
 		    rbmCl.serialize(output, getConf());
 		    logger.info("RBM finetuning done and model written to output");
 	    }
 	    
 	    if(executor!=null)
 	    	executor.shutdownNow();
 	    
 		return 0;
 	}
 	
 	/**
 	 * The Class BackpropTrainingThread is the callable thread for the local backprop task.
 	 */
 	class BackpropTrainingThread implements Callable<Matrix[]> {
 
 		/** The dbm. */
 		private DeepBoltzmannMachine dbm;
 		
 		/** The input. */
 		private Vector input;
 		
 		/** The label. */
 		private Vector label;
 		
 		/** The trainer. */
 		private BackPropTrainer trainer;
 
 		/**
 		 * Instantiates a new backprop training thread.
 		 *
 		 * @param dbm the dbm
 		 * @param label the label
 		 * @param input the input
 		 * @param trainer the trainer
 		 */
 		public BackpropTrainingThread(DeepBoltzmannMachine dbm, Vector label, Vector input, BackPropTrainer trainer) {
 			this.dbm = dbm;
 			this.label = label;
 			this.input = input;
 			this.trainer = trainer;
 		}
 		
 		/* (non-Javadoc)
 		 * @see java.util.concurrent.Callable#call()
 		 */
 		@Override
 		public Matrix[] call() throws Exception {
 			Matrix[] result = trainer.calculateWeightUpdates(dbm, input, label);
 			Matrix[] weightUpdates =new Matrix[dbm.getRbmCount()-1];
 			
 			//write for each RBM i (key, number of rbm) the result and put together the last two
 			//matrices since they refer to just one labeled rbm, which was split to two for the training
 			for (int i = 0; i < result.length-1; i++) {
 				if(i==result.length-2) {
 					weightUpdates[i] = new DenseMatrix(result[i].rowSize()+result[i+1].columnSize(), result[i].columnSize());
 					for(int j = 0; j<weightUpdates[i].rowSize(); j++)
 						for(int k = 0; k<weightUpdates[i].columnSize(); k++) {
 							if(j<result[i].rowSize())
 								weightUpdates[i].set(j, k, result[i].get(j, k));
 							else
 								weightUpdates[i].set(j, k, result[i+1].get(k, j-result[i].rowSize()));
 						}
 				}
 				else
 					weightUpdates[i]= result[i];			
 			}
 			
 			return weightUpdates;
 		}
 		
 	}
 
 	/** The backprop training tasks. */
 	List<BackpropTrainingThread> backpropTrainingTasks;
 	
 	/**
 	 * Finetune locally.
 	 *
 	 * @param batch the batch
 	 * @param iteration the iteration
 	 * @param multiLayerDbm the multilayer dbm
 	 * @param learningrate the learningrate
 	 * @return true, if successful
 	 * @throws InterruptedException the interrupted exception
 	 * @throws ExecutionException the execution exception
 	 */
 	private boolean finetuneSeq(Path batch, int iteration, DeepBoltzmannMachine multiLayerDbm, double learningrate) throws InterruptedException, ExecutionException {
 		Vector label = new DenseVector(labelcount);
 		Map<Integer, Matrix> updates = new HashMap<Integer, Matrix>();
     	int batchsize = 0;
 		//maximum number of threads that are used, I think 20 is ok
     	int threadCount = 20;
     	Matrix[] weightUpdates;
     	//initialize the tasks, which are run by the executor
     	if(backpropTrainingTasks==null)
     		backpropTrainingTasks = new ArrayList<BackpropTrainingThread>();
     	//initialize the executor if not already done
     	if(executor==null)
     		executor = Executors.newFixedThreadPool(threadCount);
     	
 		for (Pair<IntWritable, VectorWritable> record : new SequenceFileIterable<IntWritable, VectorWritable>(batch, getConf())) {
 			for (int i = 0; i < label.size(); i++)
 				label.setQuick(i, 0);
 				
 			label.set(record.getFirst().get(), 1);
 			
 			BackPropTrainer trainer = new BackPropTrainer(learningrate);
 			
 			//prepare the tasks
 			if(backpropTrainingTasks.size()<threadCount)				
 				backpropTrainingTasks.add(new BackpropTrainingThread(multiLayerDbm.clone(), label.clone(), record.getSecond().get(), trainer));
 			else {
 				backpropTrainingTasks.get(batchsize%threadCount).input = record.getSecond().get();
 				backpropTrainingTasks.get(batchsize%threadCount).label = label.clone();
 				if(batchsize<threadCount){
 					backpropTrainingTasks.get(batchsize%threadCount).dbm = multiLayerDbm.clone();
 				}				
 			}
 			
 			//run the tasks and save results
 			if(batchsize%threadCount==threadCount-1) {
 				List<Future<Matrix[]>> futureUpdates = executor.invokeAll(backpropTrainingTasks);
 				for (int i = 0; i < futureUpdates.size(); i++) {
 					weightUpdates = futureUpdates.get(i).get();
 					for (int j = 0; j < weightUpdates.length; j++) {
 						if(updates.containsKey(j))
 							updates.put(j, weightUpdates[j].plus(updates.get(j)));
 						else
 							updates.put(j, weightUpdates[j]);						
 					}					
 				}
 			}		
 
 			batchsize++;
 		}
 		
 		//run remaining tasks
 		if(batchsize%20!=0) {
 			List<Future<Matrix[]>> futureUpdates = executor.invokeAll(backpropTrainingTasks.subList(0, (batchsize-1) %20));
 			for (int i = 0; i < futureUpdates.size(); i++) {
 				weightUpdates = futureUpdates.get(i).get();
 				for (int j = 0; j < weightUpdates.length; j++) {
 					if(updates.containsKey(j))
 						updates.put(j, weightUpdates[j].plus(updates.get(j)));
 					else
 						updates.put(j, weightUpdates[j]);						
 				}					
 			}
 		}
 	
 	    updateRbmCl(batchsize, (iteration==0)?0:momentum, updates);
 
 		return true;
 	}
 
 	/**
 	 * Fintune using map/reduce.
 	 *
 	 * @param batch the batch
 	 * @param iteration the iteration
 	 * @param learningrate the learningrate
 	 * @return true, if successful
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 * @throws InterruptedException the interrupted exception
 	 * @throws ClassNotFoundException the class not found exception
 	 */
 	private boolean fintuneMR(Path batch, int iteration, double learningrate)
 			throws IOException, InterruptedException, ClassNotFoundException {
 		//prepare and run finetune job
 		long batchsize;
 		HadoopUtil.delete(getConf(), getTempPath(WEIGHT_UPDATES));
 		HadoopUtil.cacheFiles(getOutputPath(), getConf());
 		
 		Job trainDBM = prepareJob(batch, getTempPath(WEIGHT_UPDATES), SequenceFileInputFormat.class, 
 									DBMBackPropTrainingMapper.class, IntWritable.class, MatrixWritable.class, 
 								    DBMBackPropTrainingReducer.class, IntWritable.class, MatrixWritable.class, 
 								    SequenceFileOutputFormat.class);
 		trainDBM.getConfiguration().set("labelcount", String.valueOf(labelcount));
 		trainDBM.getConfiguration().set("learningrate", String.valueOf(learningrate));
 
 		trainDBM.setCombinerClass(DBMBackPropTrainingReducer.class);
 		
 		if(!trainDBM.waitForCompletion(true))
 			return false;
 		
 		batchsize = trainDBM.getCounters().findCounter(DBMBackPropTrainingMapper.BATCHES.SIZE).getValue();
 		
 		changeAndSaveModel(getOutputPath(), batchsize, (iteration==0)?0:momentum);
 		return true;
 	}
 
 	/**
 	 * The Class GreedyTrainingThread.
 	 */
 	class GreedyTrainingThread implements Callable<Matrix> {
 
 		/** The dbm. */
 		private DeepBoltzmannMachine dbm;
 		
 		/** The input. */
 		private Vector input;
 		
 		/** The label. */
 		private Vector label;
 		
 		/** The trainer. */
 		private CDTrainer trainer;
 		
 		/** The rbm nr to train. */
 		int rbmNr;
 
 		/**
 		 * Instantiates a new greedy training thread.
 		 *
 		 * @param dbm the dbm
 		 * @param label the label
 		 * @param input the input
 		 * @param trainer the trainer
 		 * @param rbmNr the rbm nr
 		 */
 		public GreedyTrainingThread(DeepBoltzmannMachine dbm, Vector label, Vector input, CDTrainer trainer, int rbmNr) {
 			this.dbm = dbm;
 			this.label = label;
 			this.input = input;
 			this.trainer = trainer;
 			this.rbmNr = rbmNr;
 		}
 		
 		/* (non-Javadoc)
 		 * @see java.util.concurrent.Callable#call()
 		 */
 		@Override
 		public Matrix call() throws Exception {
 			Matrix updates = null;
 			
 			dbm.getRBM(0).getVisibleLayer().setActivations(input);
 			for(int i = 0; i<rbmNr; i++){
 				//double the bottom up connection for initialization
 				dbm.getRBM(i).exciteHiddenLayer(2, false);
 				if(i==rbmNr-1)
 					//probabilities as activation for the data the rbm should train on
 					dbm.getRBM(i).getHiddenLayer().setProbabilitiesAsActivation();
 				else
 					dbm.getRBM(i).getHiddenLayer().updateNeurons();
 			}
 						
 			if(rbmNr==dbm.getRbmCount()-1) {
 				((LabeledSimpleRBM)dbm.getRBM(rbmNr)).getSoftmaxLayer().setActivations(label);
 				updates = trainer.calculateWeightUpdates((LabeledSimpleRBM)dbm.getRBM(rbmNr), true, false);
 			}
 			else {
 				updates = trainer.calculateWeightUpdates((SimpleRBM)dbm.getRBM(rbmNr), false, rbmNr==0);
 			}
 			return updates;
 		}
 		
 	}
 	
 	/** The executor. */
 	private ExecutorService executor;
 	
 	/** The greedy training tasks. */
 	List<GreedyTrainingThread> greedyTrainingTasks;
 	
 	/**
 	 * Train greedy seq.
 	 *
 	 * @param rbmNr the rbm nr
 	 * @param batch the batch
 	 * @param iteration the iteration
 	 * @param learningrate the learningrate
 	 * @return true, if successful
 	 * @throws InterruptedException the interrupted exception
 	 * @throws ExecutionException the execution exception
 	 */
 	private boolean trainGreedySeq(int rbmNr, Path batch, int iteration, double learningrate) throws InterruptedException, ExecutionException {
     	int batchsize = 0;
     	DeepBoltzmannMachine dbm = rbmCl.getDbm();
     	Vector label = new DenseVector(labelcount);
     	Matrix updates = null;
     	//number of threads running the tasks
     	int threadCount =20;
     	if(executor==null)
     		executor = Executors.newFixedThreadPool(threadCount);
     	if(greedyTrainingTasks==null)
     		greedyTrainingTasks = new ArrayList<RBMClassifierTrainingJob.GreedyTrainingThread>();
     	
 		for (Pair<IntWritable, VectorWritable> record : new SequenceFileIterable<IntWritable, VectorWritable>(batch, getConf())) {
 			CDTrainer trainer = new CDTrainer(learningrate, nrGibbsSampling);
 			label.assign(0);
 			label.set(record.getFirst().get(), 1);
 			//prepare the tasks
 			if(greedyTrainingTasks.size()<threadCount)				
 				greedyTrainingTasks.add(new GreedyTrainingThread(dbm.clone(), label.clone(), record.getSecond().get(), trainer, rbmNr));
 			else {
 				greedyTrainingTasks.get(batchsize%threadCount).input = record.getSecond().get();
 				greedyTrainingTasks.get(batchsize%threadCount).label = label.clone();
 				if(batchsize<threadCount){
 					greedyTrainingTasks.get(batchsize%threadCount).dbm = dbm.clone();
 					greedyTrainingTasks.get(batchsize%threadCount).rbmNr = rbmNr;
 				}				
 			}
 			//run tasks
 			if(batchsize%threadCount==threadCount-1) {
 				List<Future<Matrix>> futureUpdates = executor.invokeAll(greedyTrainingTasks);
 				for (int i = 0; i < futureUpdates.size(); i++) {
 					if(updates==null)
 						updates = futureUpdates.get(i).get();
 					else
 						updates = updates.plus(futureUpdates.get(i).get());
 				}
 			}
 				
     		batchsize++;
     	}
 		
 		//run remaining tasks
 		if(batchsize%20!=0) {
 			List<Future<Matrix>> futureUpdates = executor.invokeAll(greedyTrainingTasks.subList(0, (batchsize-1) %20));
 			for (int i = 0; i < futureUpdates.size(); i++) {
 				if(updates==null)
 					updates = futureUpdates.get(i).get();
 				else
 					updates = updates.plus(futureUpdates.get(i).get());
 			}
 		}
 			
 		
 		Map<Integer,Matrix> updateMap = new HashMap<Integer,Matrix>();
 		updateMap.put(rbmNr, updates);
 		updateRbmCl(batchsize, (lastUpdate[rbmNr]==null)?0:momentum, updateMap);
 		
 		return true;
 	}
 
 	/**
 	 * Train greedy mr.
 	 *
 	 * @param rbmNr the rbm nr
 	 * @param batch the batch
 	 * @param iteration the iteration
 	 * @param learningrate the learningrate
 	 * @return true, if successful
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 * @throws InterruptedException the interrupted exception
 	 * @throws ClassNotFoundException the class not found exception
 	 */
 	private boolean trainGreedyMR(int rbmNr, Path batch, int iteration, double learningrate)
 			throws IOException, InterruptedException, ClassNotFoundException {
 		//run greedy pretraining as map reduce job
 		long batchsize;
 		HadoopUtil.delete(getConf(), getTempPath(WEIGHT_UPDATES));
 		HadoopUtil.cacheFiles(getOutputPath(), getConf());
 		
 		Job trainRBM = prepareJob(batch, getTempPath(WEIGHT_UPDATES), SequenceFileInputFormat.class, 
 									RBMGreedyPreTrainingMapper.class, IntWritable.class, MatrixWritable.class, 
 								    RBMGreedyPreTrainingReducer.class, IntWritable.class, MatrixWritable.class, 
 								    SequenceFileOutputFormat.class);
 		trainRBM.getConfiguration().set("rbmNr", String.valueOf(rbmNr));
 		trainRBM.getConfiguration().set("labelcount", String.valueOf(labelcount));
 		trainRBM.getConfiguration().set("learningrate", String.valueOf(learningrate));
 		trainRBM.getConfiguration().set("nrGibbsSampling", String.valueOf(nrGibbsSampling));
 
 		trainRBM.setCombinerClass(RBMGreedyPreTrainingReducer.class);
 		
 		if(!trainRBM.waitForCompletion(true))
 			return false;
 		
 		batchsize = trainRBM.getCounters().findCounter(RBMGreedyPreTrainingMapper.BATCH.SIZE).getValue();
 		
 		changeAndSaveModel(getOutputPath(), batchsize, (lastUpdate[rbmNr]==null)?0:momentum);
 		
 		return true;
 	}
 
 	/**
 	 * calculate classifiers error after 1 iteration of sampling.
 	 *
 	 * @param batch the batch
 	 * @return the error
 	 */
 	@SuppressWarnings("unused")
 	private double classifierError(Path batch) {
     	double error = 0;
     	int counter = 0;
     	Vector scores;
 		for (Pair<IntWritable, VectorWritable> record : new SequenceFileIterable<IntWritable, VectorWritable>(batch, getConf())) {
 			scores = rbmCl.classify(record.getSecond().get(),1);
     		error += 1-scores.get(record.getFirst().get());
 			counter++;
     	}
 		error /= counter;
 		return error;
 	}
 	
 	/**
 	 * Calculates error of fann.
 	 *
 	 * @param feedForwardNet the feed forward net
 	 * @param batch the batch
 	 * @return the error
 	 */
 	private double feedForwardError(DeepBoltzmannMachine feedForwardNet, Path batch) {
 		double error = 0;
     	int counter = 0;
 		RBMModel currentRBM =null;
 		for (Pair<IntWritable, VectorWritable> record : new SequenceFileIterable<IntWritable, VectorWritable>(batch, getConf())) {
 			feedForwardNet.getRBM(0).getVisibleLayer().setActivations(record.getSecond().get());
 			for(int i = 0; i< feedForwardNet.getRbmCount(); i++) {
 				currentRBM = feedForwardNet.getRBM(i);
 				currentRBM.exciteHiddenLayer(1, false);
 				currentRBM.getHiddenLayer().setProbabilitiesAsActivation();
 			}
 			error+= 1-currentRBM.getHiddenLayer().getActivations().get(record.getFirst().get());
 			counter++;
 		}
 		error /= counter;
 		return error;
 	}
 	
 	/**
 	 * Rbms reconstruction error.
 	 *
 	 * @param batch the batch
 	 * @param rbmNr the rbm nr
 	 * @return the error
 	 */
 	private double rbmError(Path batch, int rbmNr) {
 		DeepBoltzmannMachine dbm = rbmCl.getDbm();
 		Vector label = new DenseVector(((LabeledSimpleRBM)dbm.getRBM(dbm.getRbmCount()-1)).getSoftmaxLayer().getNeuronCount());
 		
 		double error = 0;
     	int counter = 0;
 		
 		for (Pair<IntWritable, VectorWritable> record : new SequenceFileIterable<IntWritable, VectorWritable>(batch, getConf())) {
 			dbm.getRBM(0).getVisibleLayer().setActivations(record.getSecond().get());
 			for(int i = 0; i<rbmNr; i++){		
 				//double the bottom up connection for initialization
 				dbm.getRBM(i).exciteHiddenLayer(2, false);
 				if(i==rbmNr-1)
 					dbm.getRBM(i).getHiddenLayer().setProbabilitiesAsActivation();
 				else
 					dbm.getRBM(i).getHiddenLayer().updateNeurons();
 			}
 			if(dbm.getRBM(rbmNr) instanceof LabeledSimpleRBM) {
 				label.assign(0);
 				label.set(record.getFirst().get(), 1);
 				((LabeledSimpleRBM)dbm.getRBM(rbmNr)).getSoftmaxLayer().setActivations(label);
 			}
 			error += dbm.getRBM(rbmNr).getReconstructionError();
 			counter++;
     	}
 		
 		error/=counter;
 		return error;
 	}
 
 	/**
 	 * Change and save model.
 	 *
 	 * @param output the output
 	 * @param batchsize the batchsize
 	 * @param momentum the momentum
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	private void changeAndSaveModel(Path output, long batchsize, double momentum) throws IOException {
 		Map<Integer,Matrix> updates = new HashMap<Integer,Matrix>();
 
 		for (Pair<IntWritable, MatrixWritable> record : new SequenceFileDirIterable<IntWritable, MatrixWritable>(
 				getTempPath(WEIGHT_UPDATES), PathType.LIST, PathFilters.partFilter(), getConf())) {
 			
 			if(!updates.containsKey(record.getFirst().get()))
 				updates.put(record.getFirst().get(), record.getSecond().get());
 			else
 				updates.put(record.getFirst().get(), 
 						record.getSecond().get().plus(updates.get(record.getFirst().get())));	      	  
 		}	
 		
 		updateRbmCl(batchsize, momentum, updates);
 		
 		//serialization for mappers to have actual version of the dbm
 		rbmCl.serialize(output, getConf());
 	}
 
 	/**
 	 * Update rbm classifier with given updates.
 	 *
 	 * @param batchsize the batchsize
 	 * @param momentum the momentum
 	 * @param updates the updates
 	 */
 	private void updateRbmCl(long batchsize, double momentum, Map<Integer, Matrix> updates) {
 		for(Integer rbmNr : updates.keySet()) {
 			if(momentum>0)
 				updates.put(rbmNr, (updates.get(rbmNr).divide(batchsize).times(1-momentum)).
 							plus(lastUpdate[rbmNr].times(momentum)) );
 			else
 				updates.put(rbmNr,updates.get(rbmNr).divide(batchsize));
 			
 			if(rbmNr<rbmCl.getDbm().getRbmCount()-1) {
 				SimpleRBM simpleRBM = (SimpleRBM)rbmCl.getDbm().getRBM(rbmNr);
 				simpleRBM.setWeightMatrix(
 		    		simpleRBM.getWeightMatrix().plus(updates.get(rbmNr)));
 	    	
 		    }else {
 		    	LabeledSimpleRBM lrbm = (LabeledSimpleRBM)rbmCl.getDbm().getRBM(rbmNr);
 		    	int rowSize = lrbm.getWeightMatrix().rowSize();
 				Matrix weightUpdates = updates.get(rbmNr).viewPart(0, rowSize, 0, updates.get(rbmNr).columnSize());
 		    	Matrix weightLabelUpdates = updates.get(rbmNr).viewPart(rowSize, updates.get(rbmNr).rowSize()-rowSize, 0, updates.get(rbmNr).columnSize());
 		    	
 		    	lrbm.setWeightMatrix(lrbm.getWeightMatrix().plus(weightUpdates));
 		    	lrbm.setWeightLabelMatrix(lrbm.getWeightLabelMatrix().plus(weightLabelUpdates));
 		    }			
 			lastUpdate[rbmNr] = updates.get(rbmNr);
 		}
 	}
 	
 }
