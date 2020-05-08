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
 package eu.larkc.iris;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.util.GenericOptionsParser;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 import org.deri.iris.EvaluationException;
 import org.deri.iris.api.basics.IQuery;
 import org.deri.iris.api.basics.IRule;
 import org.deri.iris.api.terms.IVariable;
 import org.deri.iris.compiler.Parser;
 import org.deri.iris.compiler.ParserException;
 import org.deri.iris.evaluation.IEvaluationStrategy;
 import org.deri.iris.storage.IRelation;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import at.sti2.rif4j.parser.xml.XmlParser;
 import at.sti2.rif4j.rule.Document;
 import at.sti2.rif4j.translator.iris.RifToIrisTranslator;
 import cascading.flow.FlowConnector;
 import cascading.flow.MultiMapReducePlanner;
 import cascading.operation.DebugLevel;
 import cascading.tap.Hfs;
 import cascading.tap.Tap;
 import cascading.tuple.Fields;
 import cascading.tuple.TupleEntry;
 import cascading.tuple.TupleEntryIterator;
 import eu.larkc.iris.evaluation.bottomup.DistributedBottomUpEvaluationStrategyFactory;
 import eu.larkc.iris.evaluation.bottomup.naive.DistributedNaiveEvaluatorFactory;
 import eu.larkc.iris.exports.Exporter;
 import eu.larkc.iris.imports.Importer;
 import eu.larkc.iris.indexing.DistributedFileSystemManager;
 import eu.larkc.iris.indexing.PredicateData;
 
 /**
  * @author valer
  *
  */
 @SuppressWarnings("deprecation")
 public class Main extends Configured implements Tool {
 
 	private static final Logger logger = LoggerFactory.getLogger(Main.class);
 	
 	public enum RULES_TYPE {
 		DATALOG,
 		RIF
 	}
 	
 	private String project;
 	
 	private String importName = null;
 	
 	private boolean rdfImporter = false;
 	private boolean ntripleImporter = false;
 	private boolean tester = false;
 	private boolean processer = false;
 	private boolean rdfExporter = false;
 	private boolean ntripleExporter = false;
 	private boolean viewConfig = false;
 	
 	//rdf importer args
 	private String storageId = null;
 
 	//ntriple importer
 	private String inPath = null;
 
 	//ntriple exporter
 	private String outPath = null;
 
 	//test args
 	private String sourcePath = null;
 	
 	private RULES_TYPE rulesType;
 	private String rulesFile;
 	private boolean keepResults = false;
 	private String resultsName;
 	private String outputStorageId;
 	
 	private eu.larkc.iris.Configuration defaultConfiguration;
 	//transient private static Map<Object, Object> properties = new HashMap<Object, Object>();
 	
 	protected List<IRule> rules;
 	
 	private void printUsage() {
 		System.out.println("<project_name> <-importRdf storage_id import_name | " +
 				"-importNTriple path_to_file import_name | " + 
 				"-process rules_type:<DATALOG|RIF> rules_file_path keep_results:<true:false> results_name output_storage_id > | " +
 				"-exportRdf storage_id results_name | " + 
 				"-exportNTriple path_to_export_file results_name ");
 	}
 	
 	private void processUserArguments(String[] args) {
 		if (args.length == 0) {
 			printUsage();
 			return;
 		}
 		
 		project = args[0];
 		
 		String operation = args[1];
 		if (operation.equalsIgnoreCase("-importRdf")) {
 			rdfImporter = true;
 			storageId = args[2];
 			importName = args[3];
 		} else if (operation.equalsIgnoreCase("-importNTriple")) {
 			ntripleImporter = true;
 			inPath = args[2];
 			importName = args[3];
 		} else if (operation.equalsIgnoreCase("-process")) {
 			processer = true;
 			rulesType = RULES_TYPE.valueOf(args[2].toUpperCase());
 			rulesFile = args[3];
 			if (args[4] != null && !"".equals(args[4])) {
 				//keepResults = Boolean.valueOf(args[4]);
 				resultsName = args[4];
 				if (args.length > 5) {
 					outputStorageId = args[5];
 				}
 			}
 		} else if (operation.equalsIgnoreCase("-exportRdf")) {
 			rdfExporter = true;
 			storageId = args[2];
 			resultsName = args[3];
 		} else if (operation.equalsIgnoreCase("-exportNTriple")) {
 			ntripleExporter = true;
 			outPath = args[2];
 			resultsName = args[3];
 		} else if (operation.equals("-test")) {
 			tester = true;
 			sourcePath = args[2];
 		} else if (operation.equals("-viewConfig")) {
 			viewConfig = true;
 		}
 	}
 	
 	private JobConf setupJob(Configuration conf) {
 		JobConf jobConf = new JobConf(conf, Main.class);
 		
 	    // run the job here.
 		
 		jobConf.setBoolean("mapred.input.dir.recursive", true);
 		
 		jobConf.set("cascading.serialization.tokens", "130=eu.larkc.iris.storage.IRIWritable,131=eu.larkc.iris.storage.StringTermWritable");
 		defaultConfiguration.flowProperties.put("cascading.serialization.tokens", "130=eu.larkc.iris.storage.IRIWritable,131=eu.larkc.iris.storage.StringTermWritable");
 		
 	    if( System.getProperty("log4j.logger") != null )
 	    	defaultConfiguration.flowProperties.put( "log4j.logger", System.getProperty("log4j.logger") );
 
 		jobConf.set("mapred.child.java.opts", "-Xms64m -Xmx512m");
 		jobConf.setMapSpeculativeExecution(false);
 		jobConf.setReduceSpeculativeExecution(false);
 
 		jobConf.setNumMapTasks(8);
 		jobConf.setNumReduceTasks(2);
 
 		FlowConnector.setDebugLevel(defaultConfiguration.flowProperties, DebugLevel.VERBOSE);
 		MultiMapReducePlanner.setJobConf( defaultConfiguration.flowProperties, jobConf );
 		
 		//Flow.setJobPollingInterval(defaultConfiguration.flowProperties, 500);
 		
 		return jobConf;
 	}
 	
 	public int doRdfImport(eu.larkc.iris.Configuration configuration) {
 		new Importer(configuration).importFromRdf(storageId, importName);
 		return 0;
 	}
 
 	public int doNTripleImport(eu.larkc.iris.Configuration configuration) {
 		new Importer(configuration).importFromFile(inPath, importName);
 		return 0;
 	}
 
 	public int doRdfExport(eu.larkc.iris.Configuration configuration) {
 		new Exporter(configuration).exportToRdf(storageId, resultsName);
 		return 0;
 	}
 
 	public int doNTripleExport(eu.larkc.iris.Configuration configuration) {
 		new Exporter(configuration).exportToFile(outPath, resultsName);
 		return 0;
 	}
 
 	public int doTester(eu.larkc.iris.Configuration configuration) {
 		logger.info("do tester ...");
 		
 		//JobConf jobConf = new JobConf();
 		//jobConf.set("cascading.serialization.tokens", "130=eu.larkc.iris.storage.IRIWritable,131=eu.larkc.iris.storage.PredicateWritable,132=eu.larkc.iris.storage.StringTermWritable");
 		//jobConf.setBoolean("mapred.input.dir.recursive", true);
 		
 		Tap source = new Hfs(Fields.ALL, project + "/" + sourcePath);
 		TupleEntryIterator tei = null;
 		try {
 			tei = source.openForRead(configuration.jobConf);
 		} catch (IOException e) {
 			logger.error("io exception", e);
 			return -1;
 		}
 		
 		int i = 0;
 		while (tei.hasNext() && i < 10) {
 			TupleEntry te = tei.next();
 			logger.info(te.toString());
 			i++;
 		}
 		
 		return 0;
 	}
 
 	public int doViewConfig(eu.larkc.iris.Configuration configuration) {
 		DistributedFileSystemManager distributedFileSystemManager = new DistributedFileSystemManager(configuration);
 		List<PredicateData> predicatesData = distributedFileSystemManager.getPredicateData();
 		for (PredicateData predicateData : predicatesData) {
 			logger.info(predicateData.toString());
 		}
 		return 0;
 	}
 
 	public int doProcess() {
 		defaultConfiguration.keepResults = keepResults;
 		defaultConfiguration.resultsName = resultsName;
 		defaultConfiguration.outputStorageId = outputStorageId;
 		try {
 			evaluate(null, new ArrayList<IVariable>(), defaultConfiguration);
 		} catch (EvaluationException e) {
 			logger.error("evaluation exception", e);
 			return -1;
 		}
 
 	    return 0; 		
 	}
 	
 	@Override
 	public int run(String[] args) throws Exception {
 		GenericOptionsParser gop = new GenericOptionsParser(getConf(), new org.apache.commons.cli.Options(), args);
 
 		processUserArguments(gop.getRemainingArgs());
 		
 		rules = createRules();
 		
 		Configuration hadoopConf = gop.getConfiguration();
 		defaultConfiguration.hadoopConfiguration = hadoopConf;
 		defaultConfiguration.jobConf = setupJob(hadoopConf);
 		defaultConfiguration.project = project;
 		
 		if (rdfImporter) {
 			return doRdfImport(defaultConfiguration);
 		} else if (ntripleImporter) {
 			return doNTripleImport(defaultConfiguration);
 		} else if (tester) {
 			return doTester(defaultConfiguration);
 		} else if (processer) {
 			return doProcess();
 		} else if (rdfExporter) {
 			return doRdfExport(defaultConfiguration);
 		} else if (ntripleExporter) {
 			return doNTripleExport(defaultConfiguration);
 		} else if (viewConfig) {
 			return doViewConfig(defaultConfiguration);
 		}
 		
 		return -1;
 	}
 
 	protected List<IRule> createRules() {
 		List<IRule> rules = null;
 		if (rulesType == RULES_TYPE.DATALOG) {
 			Collection<String> expressions = new ArrayList<String>();
 			try {
 				BufferedReader br = new BufferedReader(new FileReader(new File(rulesFile)));
 				String line = null;
 				while ((line = br.readLine()) != null) {
 					logger.info(line);
 					expressions.add(line);
 				}
 			} catch (FileNotFoundException e) {
 				logger.error("the rules files can not be located", e);
 				throw new RuntimeException("the rules files can not be located", e);
 			} catch (IOException e) {
 				logger.error("io exception reading the rules files", e);
 				throw new RuntimeException("io exception reading the rules files", e);
 			}
 			//expressions.add("subClassOf( ?X, ?Z ) :- subClassOf( ?X, ?Y ), subClassOf( ?Y, ?Z ).");
 			//expressions.add("type( ?X, ?Z ) :- type( ?X, ?Y ), subClassOf( ?Y, ?Z ).");
 			
 			Parser parser = new Parser();
 			StringBuffer buffer = new StringBuffer();
 
 			for (String expression : expressions) {
 				buffer.append(expression);
 			}
 
 			try {
 				parser.parse(buffer.toString());
 			} catch (ParserException e) {
 				logger.error("rules parser exception", e);
 				throw new RuntimeException("rules parser exception", e);
 			}
 			
 			rules = parser.getRules();
 		}
 		
 		if (rulesType == RULES_TYPE.RIF) {
 			XmlParser parser = new XmlParser(true);
 			Document rifDocument;
 			try {
 				rifDocument = parser.parseDocument(new FileReader(new File(rulesFile)));
 			} catch (Exception e) {
 				logger.error("exception reading/parsing rules RIF file!", e);
 				throw new RuntimeException("exception reading/parsing rules RIF file!", e);
 			}
 
 			RifToIrisTranslator translator = new RifToIrisTranslator();
 			translator.translate(rifDocument);
 			rules = translator.getRules();
 		}
 		
 		return rules;
 	}
 
 	public Main() {
 		logger.info("start iris distributed reasoner ...");
 		
 		org.apache.hadoop.conf.Configuration configuration = new org.apache.hadoop.conf.Configuration();
 		setConf(configuration);
 		
 		defaultConfiguration = new eu.larkc.iris.Configuration();
 		defaultConfiguration.evaluationStrategyFactory = new DistributedBottomUpEvaluationStrategyFactory(new DistributedNaiveEvaluatorFactory());
 	}
 	
 	public static void main(String[] args) throws Exception {
 		int ret = ToolRunner.run(new Main(), args); // calls your run() method. 
 	    System.exit(ret);
 	}
 	
 	private void evaluate(IQuery query, List<IVariable> outputVariables,
 			eu.larkc.iris.Configuration configuration) throws EvaluationException {
 		//IRelation relation = evaluate(FactsFactory.getInstance("default"), "?- p(?X, ?Y).");
 		
 		IEvaluationStrategy strategy = configuration.evaluationStrategyFactory
 			.createEvaluator(rules, configuration); 
 		
 		@SuppressWarnings("unused")
 		IRelation relation = strategy.evaluateQuery(query, outputVariables);		
 	}
 	
 }
