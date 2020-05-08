 /*
  * Copyright 2010 Softgress - http://www.softgress.com/
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package eu.larkc.iris.rules.compiler;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.deri.iris.api.basics.IPredicate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import cascading.flow.Flow;
 import cascading.flow.FlowConnector;
 import cascading.pipe.Pipe;
 import cascading.scheme.Scheme;
 import cascading.scheme.SequenceFile;
 import cascading.tap.Hfs;
 import cascading.tap.MultiSourceTap;
 import cascading.tap.Tap;
 import cascading.tuple.Fields;
 import cascading.tuple.TupleEntryIterator;
 import eu.larkc.iris.Configuration;
 import eu.larkc.iris.Utils;
 import eu.larkc.iris.evaluation.EvaluationContext;
 import eu.larkc.iris.indexing.DistributedFileSystemManager;
 import eu.larkc.iris.indexing.PredicateCount;
 
 /**
  * The pipes for a rule are evaluated using this flow assembly.
  * The evaluation consist in setting the right sources and taps for the pipes.
  * A fllow is created and completed to get the inferences.
  * 
  * @author valer.roman@softgress.com
  *
  */
 public class FlowAssembly {
 
 	private static final Logger logger = LoggerFactory.getLogger(FlowAssembly.class);
 	
 	//private static final String RESULT_TAIL = "resultTail";
 	
 	private Configuration mConfiguration;
 	private DistributedFileSystemManager distributedFileSystemManager;
 	private RuleStreams ruleStreams;
 	private Fields fields;
 	private Pipe pipe;
 	
 	private Flow flow = null;
 	private String path = null;
 	
 	public FlowAssembly (Configuration configuration, RuleStreams ruleStreams, Fields fields, Pipe pipe) {
 		this.mConfiguration = configuration;
 		this.ruleStreams = ruleStreams;
 		this.fields = fields;
 		this.pipe = pipe;
 	}
 	
 	/*
 	 * creates a pipe for predicate counts
 	 */
 	private void setupPredicateCounts(Pipe pipe, Map<String, Tap> sinks, List<Pipe> pipes) throws IOException {
 		String predicateGroupsTempPath = distributedFileSystemManager.getPredicateGroupsTempPath(mConfiguration.resultsName);
 
 		FileSystem fs = FileSystem.get(mConfiguration.hadoopConfiguration);
 		if (fs.exists(new Path(predicateGroupsTempPath))) {
 			fs.delete(new Path(predicateGroupsTempPath), true);
 		}
 
 		Tap predicatesSink = new Hfs(new Fields(0, 1), predicateGroupsTempPath);
 		Pipe predicatesPipe = Utils.buildPredicateCountPipe(pipe);
 		
 		sinks.put("predicatesPipe", predicatesSink);
 		pipes.add(predicatesPipe);
 	}
 	
 	/*
 	 * creates and processes a flow identified by {@code flowIdentificator}
 	 * results are stored at {@code output} under the result named {@code resultName}
 	 */
 	private boolean processFlow(String resultName, String flowIdentificator, String output) throws IOException {
 		boolean hasNewInferences = false;
 		String flowName = resultName + flowIdentificator;
 		Map<String, Tap> sources = prepareSourceTaps();
 		
 		SequenceFile sinkScheme = new SequenceFile(fields);
 		sinkScheme.setNumSinkParts(1);
 		Tap headSink = new Hfs(sinkScheme, output, true );
 
 		Map<String, Tap> sinks = new HashMap<String, Tap>();
 		List<Pipe> pipes = new ArrayList<Pipe>();
 		if (mConfiguration.doPredicateIndexing) {
 			//calculate the count of the result and write it in the configuration
 			//if the predicate is a variable then we have to split also the result and put it in the right location
 			sinks.put(pipe.getName(), headSink);
 			pipes.add(pipe);
 			setupPredicateCounts(pipe, sinks, pipes);
 		}
 		
 		Flow flow = null;
 		if (sinks.isEmpty()) {
 			flow = new FlowConnector(mConfiguration.flowProperties).connect(flowName, sources, headSink, pipe);
 		} else {
 			flow = new FlowConnector(mConfiguration.flowProperties).connect(flowName, sources, sinks, pipes.toArray(new Pipe[0]));
 		}
 		if(flow != null) {
 			flow.writeDOT("flow.dot");
 		}
 		flow.complete();
 		
 		try {
 			TupleEntryIterator iterator = flow.openSink();
 			if(iterator.hasNext()) {
 				hasNewInferences = true;
 			}
 			iterator.close();
 		} catch (IOException e) {
 			logger.error("io exception", e);
 			throw new RuntimeException("io exception", e);
 		}
 		if (!hasNewInferences) {
 			deleteResults(new Path(path));
 		}
 
 		if (hasNewInferences && mConfiguration.doPredicateIndexing) {
 			FileSystem fs = FileSystem.get(mConfiguration.hadoopConfiguration);
 			
 			//update counts in configuration
 			List<PredicateCount> predicateCounts = Utils.readPredicateCounts(flow, "predicatesPipe");
 			
 			distributedFileSystemManager.addPredicates(predicateCounts);
 			
 			if (ruleStreams.getHeadStream().getPredicate() == null) {
 				//split result to the right locations (for variable predicate)
 				Tap source = new Hfs(sinkScheme, output, true );
 				Utils.splitStreamPerPredicates(mConfiguration, distributedFileSystemManager, source, predicateCounts, resultName, flowIdentificator);
 				
 				fs.delete(new Path(output), true);
 			}
 			
 			distributedFileSystemManager.savePredicateConfig();
 			String predicateGroupsTempPath = distributedFileSystemManager.getPredicateGroupsTempPath(mConfiguration.resultsName);
 			fs.delete(new Path(predicateGroupsTempPath), true);
 		}
 				
 		return hasNewInferences;
 	}
 	
 	/**
 	 * Evaluates this flow assembly
 	 * 
 	 * @param evaluationContext the evaluation context, stratum, iteration, rule number
 	 * @return true if new inferences were stored, false otherwise
 	 */
 	public boolean evaluate(EvaluationContext evaluationContext) {
 		this.distributedFileSystemManager = new DistributedFileSystemManager(mConfiguration);
 
 		String flowIdentificator = "_" + evaluationContext.getStratumNumber() + "_" + evaluationContext.getIterationNumber() + "_" + evaluationContext.getRuleNumber();
 		String resultName = mConfiguration.resultsName != null ? mConfiguration.resultsName : "inference";
		if (ruleStreams.getHeadStream().getPredicate() != null || !mConfiguration.doPredicateIndexing) {
 			path = distributedFileSystemManager.getInferencesPath(ruleStreams.getHeadStream(), resultName, flowIdentificator);
 		} else {
 			path = distributedFileSystemManager.getTempInferencesPath(resultName, flowIdentificator);
 		}
 		
 		try {
 			return processFlow(resultName, flowIdentificator, path);
 		} catch (IOException e) {
 			logger.error("io exception creating flow", e);
 			throw new RuntimeException("io exception creating flow", e);
 		}
 	}
 	
 	/**
 	 * Opens the sink the flow. Only valid after evaluation was completed
 	 * 
 	 * @return a tuple entry iterator
 	 * @throws IOException
 	 */
 	public TupleEntryIterator openSink() throws IOException {
 		if (flow == null) {
 			return null;
 		}
 		return flow.openSink();
 		//Hfs hfs = new Hfs(Fields.ALL, path);
 		//return hfs.openForRead(mConfiguration.jobConf);
 	}
 	
 	/*
 	 * Put into the sources the taps for and predicate indexed storage
 	 */
 	private void prepareIndexedSource(SequenceFile sourceScheme, Map<String, List<Tap>> sources, LiteralFields fields) {
 		IPredicate predicate = fields.getPredicate();
 		String literalId = fields.getId().toString();
 		sources.put(literalId, new ArrayList<Tap>());
 		if (predicate == null) {
 			sources.get(literalId).add(new Hfs(sourceScheme, distributedFileSystemManager.getFactsPath()));
 		} else {
 			sources.get(literalId).add(new Hfs(sourceScheme, distributedFileSystemManager.getFactsPath(fields)));
 		}
 		Map<String, Tap> inferencesTaps = getInferencesTap(sourceScheme);
 		if (inferencesTaps.containsKey(literalId)) {
 			sources.get(literalId).add(inferencesTaps.get(literalId));
 		}
 	}
 	
 	/*
 	 * Prepares the source taps
 	 */
 	private Map<String, Tap> prepareSourceTaps() {
 		SequenceFile sourceScheme = new SequenceFile(fields);
 		Map<String, List<Tap>> sources = new HashMap<String, List<Tap>>();
 		if (mConfiguration.doPredicateIndexing) {
 			LiteralFields headStream = ruleStreams.getHeadStream();
 			prepareIndexedSource(sourceScheme, sources, headStream);
 			for (LiteralFields fields : ruleStreams.getBodyStreams()) {
 				prepareIndexedSource( sourceScheme, sources, fields);
 			}
 		} else {
 			Tap factsTap = new Hfs(sourceScheme, distributedFileSystemManager.getFactsPath());
 			sources.put("main", new ArrayList<Tap>());
 			sources.get("main").add(factsTap);
 			Map<String, Tap> inferencesTaps = getInferencesTap(sourceScheme);
 			if (inferencesTaps.containsKey("main")) {
 				sources.get("main").add(inferencesTaps.get("main"));
 			}
 		}
 		Map<String, Tap> sourceTaps = new HashMap<String, Tap>();
 		for (String name : sources.keySet()) {
 			sourceTaps.put(name, new MultiSourceTap(sources.get(name).toArray(new Tap[0]))); //we can assume that the number of fields are the same as the head;s tuple size + 1 (the predicate)
 		}
 		return sourceTaps;		
 	}
 	
 	/*
 	 * Returns the tap for inferences
 	 */
 	private Map<String, Tap> getInferencesTap(Scheme scheme) {
 		Map<String, Tap> inferencesTap = new HashMap<String, Tap>();
 		try {
 			String path = null;
 			FileSystem fs = FileSystem.get(mConfiguration.hadoopConfiguration);
 			if (mConfiguration.doPredicateIndexing) {
 				LiteralFields headStream = ruleStreams.getHeadStream();
 				path = distributedFileSystemManager.getInferencesPath(headStream);
 				if (fs.exists(new Path(path))) {
 					inferencesTap.put(headStream.getId().toString(), new Hfs(scheme, path));
 				}
 				for (LiteralFields fields : ruleStreams.getBodyStreams()) {
 					path = distributedFileSystemManager.getInferencesPath(fields);
 					if (fs.exists(new Path(path))) {
 						inferencesTap.put(fields.getId().toString(), new Hfs(scheme, path));
 					}
 				}
 			} else {
 				path = distributedFileSystemManager.getInferencesPath();
 				if (fs.exists(new Path(path))) {
 					inferencesTap.put("main", new Hfs(scheme, path));
 				}
 			}
 		} catch (IOException e) {
 			logger.error("io exception", e);
 			throw new RuntimeException("io exception", e);
 		}
 		return inferencesTap;
 	}
 
 	/*
 	 * Delete the folder created for a result
 	 */
 	private void deleteResults(Path resultsPath) {
 		try {
 			//delete inference folder if no result found, to minimize number of input splits for next evaluation
 			FileSystem fs = FileSystem.get(mConfiguration.hadoopConfiguration);
 			if (fs.exists(resultsPath)) {
 				logger.info("delete path : " + resultsPath);
 				fs.delete(resultsPath, true);
 			}
 		} catch (IOException e) {
 			logger.error("io exception", e);
 			throw new RuntimeException("io exception", e);
 		}
 	}
 }
