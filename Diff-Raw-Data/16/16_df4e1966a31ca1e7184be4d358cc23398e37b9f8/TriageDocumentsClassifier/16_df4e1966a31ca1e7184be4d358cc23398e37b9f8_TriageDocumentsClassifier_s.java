 package edu.isi.bmkeg.skm.triage.cleartk.bin;
 
 import java.io.File;
 import java.util.Date;
 
 import org.apache.uima.analysis_engine.AnalysisEngine;
 import org.apache.uima.collection.CollectionReader;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.cleartk.classifier.CleartkAnnotator;
 import org.cleartk.classifier.jar.DefaultDataWriterFactory;
 import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
 import org.cleartk.classifier.jar.GenericJarClassifierFactory;
 import org.cleartk.classifier.jar.JarClassifierBuilder;
 import org.cleartk.classifier.libsvm.LIBSVMBooleanOutcomeDataWriter;
 import org.cleartk.syntax.opennlp.SentenceAnnotator;
 import org.cleartk.token.stem.snowball.DefaultSnowballStemmer;
 import org.cleartk.token.tokenizer.TokenAnnotator;
 import org.cleartk.util.Options_ImplBase;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 import org.uimafit.factory.AggregateBuilder;
 import org.uimafit.factory.AnalysisEngineFactory;
 import org.uimafit.pipeline.JCasIterable;
 import org.uimafit.pipeline.SimplePipeline;
 import org.uimafit.util.JCasUtil;
 
 import edu.isi.bmkeg.skm.cleartk.type.CatorgorizedFtdText;
 import edu.isi.bmkeg.skm.triage.cleartk.annotators.TriageDocumentGoldDocumentCategoryAnnotator;
 import edu.isi.bmkeg.skm.triage.cleartk.annotators.Uni_and_BigramCountAnnotator;
 import edu.isi.bmkeg.skm.triage.cleartk.cr.TriageScoreCollectionReader;
 import edu.isi.bmkeg.skm.triage.controller.TriageEngine;
 import edu.isi.bmkeg.triage.uimaTypes.TriageScore;
 
 
 public class TriageDocumentsClassifier {
 
	public static class Options extends Options_ImplBase {
 		
 		@Option(name = "-triageCorpus", usage = "The triage corpus to be evaluated. It is required if -predict is used.",
 				required = false, metaVar = "NAME")
 		public String triageCorpus = "";
 
 		@Option(name = "-targetCorpus", usage = "The target corpus that we're linking to",
 				required = true, metaVar = "NAME")
 		public String targetCorpus = "";
 
 		@Option(name = "-homeDir", usage = "Directory where application data will be persisted", 
 				required  = false, metaVar = "DIR")
 		public File homeDir;
 		
 		@Option(name = "-l", usage = "Database login", required = true, metaVar = "LOGIN")
 		public String login = "";
 
 		@Option(name = "-p", usage = "Database password", required = true, metaVar = "PASSWD")
 		public String password = "";
 
 		@Option(name = "-db", usage = "Database name", required = true, metaVar  = "DBNAME")
 		public String dbName = "";
 
 		@Option(name = "-train", usage = "If present will train and generate model, if absent will compute and update prediction scores in Triage Document. Either -train or -predict should be specified.")
 		public boolean train = false;
 
 		@Option(name = "-predict", usage = "If present will compute and update prediction scores in Triage Document. Either -train or -predict should be specified.")
 		public boolean predict = false;
 	}
 	
 	  public static enum AnnotatorMode {
 		    TRAIN, CLASSIFY
 		  }
 	  
 	  public static String DATA_WRITER_NAME = LIBSVMBooleanOutcomeDataWriter.class.getName();
 	  
 	  public static String[] TRAINING_ARGS = new String[] {"-t", "0"};
 
 	  public String triageCorpus;
 	  public String targetCorpus;
 	  public File modelDir;
 	  public String login;
 	  public String password;
 	  public String dbName;
 
 	public TriageDocumentsClassifier(String triageCorpus,
 			String targetCorpus, File modelDir, 
 			String login, String password, String dbName) {
 
 		this.triageCorpus = triageCorpus;
 		this.targetCorpus = targetCorpus;
 		this.modelDir = modelDir;
 		this.login = login;
 		this.password = password;
 		this.dbName = dbName;
 	}
 	
 
 	public void train() throws Exception {		
 		
 		// TODO set TriageCorpus startTime, isTrining, modelFilePath, etc.
 		
 		CollectionReader collectionReader = TriageScoreCollectionReader.load(
 				triageCorpus, 
 				targetCorpus, 
 				login, 
 				password, 
 				dbName);
 		
 	    // Create and run the document processing pipeline
 	    AggregateBuilder builder = TriageDocumentsClassifier.createDocumentClassificationAggregate(
 	    		modelDir,
 	        AnnotatorMode.TRAIN);
 	    
 	    // Train and Write model
 	    SimplePipeline.runPipeline(collectionReader, builder.createAggregateDescription());
 //	    HideOutput hider = new HideOutput();
 	    JarClassifierBuilder.trainAndPackage(
 	        modelDir,
 	        TRAINING_ARGS);
 //	    hider.restoreOutput();
 	}
 	
 	public void predict() throws Exception {		
 		// TODO set TriageCorpus startTime, isTrining, modelFilePath, etc.
 		
 		TriageEngine te = new TriageEngine();
 		te.initializeVpdmfDao(login, password, dbName);				
 
 		TriageScoreCollectionReader collectionReader = 
 				(TriageScoreCollectionReader) TriageScoreCollectionReader.load(
 					triageCorpus, 
 					targetCorpus,
 					login, 
 					password, 
 					dbName);
 		
 	    // Create and run the document processing pipeline
 	    AggregateBuilder builder = TriageDocumentsClassifier.createDocumentClassificationAggregate(
 	    		modelDir,
 	        AnnotatorMode.CLASSIFY);
 
 	    // Get classifier's outcome and update db
 		AnalysisEngine engine = builder.createAggregate();
 
 		Date timestamp = new Date();
 		
 		for (JCas jCas : new JCasIterable(collectionReader, engine)) {
 
 			CatorgorizedFtdText document = JCasUtil.selectSingle(jCas,
 					CatorgorizedFtdText.class);
 			
 			TriageScore tdoc = JCasUtil.selectSingle(jCas, TriageScore.class);
 			long triageDocId = tdoc.getVpdmfId();
 			
 			te.updateInScore(triageDocId, document.getInScore(), timestamp);
 
 			// debuging statements
 //			String cat = document.getCategory();
 //			System.out.println("id, cat, score: " + triageDocId + ", " + cat +", " + document.getInScore());
 		}
 
 	}
 	
 	 public static AggregateBuilder createPreprocessingAggregate(
 		      AnnotatorMode mode) throws ResourceInitializationException {
 		    AggregateBuilder builder = new AggregateBuilder();
 		    
 			builder.add(SentenceAnnotator.getDescription());
 			builder.add(TokenAnnotator.getDescription());
 			builder.add(DefaultSnowballStemmer.getDescription("English"));
 
 		    // Now annotate documents with gold standard labels
 		    switch (mode) {
 		      case TRAIN:
 		        // If this is training, put the label categories directly into the default view
 		        builder.add(AnalysisEngineFactory.createPrimitiveDescription(TriageDocumentGoldDocumentCategoryAnnotator.class));
 		        break;
 
 		      case CLASSIFY:
 		      default:
 		        // In normal mode don't deal with gold labels
 		        break;
 		    }
 
 		    return builder;
 		  }
 
 		  /**
 		   * Creates the aggregate builder for the document classification pipeline
 		   */
 		  public static AggregateBuilder createDocumentClassificationAggregate(
 		      File modelDirectory,
 		      AnnotatorMode mode) throws ResourceInitializationException {
 
 		    AggregateBuilder builder = TriageDocumentsClassifier.createPreprocessingAggregate(
 		        mode);
 
 			switch (mode) {
 		      case TRAIN:
 					// Combined uni + bigram count annotator
 					builder.add(AnalysisEngineFactory.createPrimitiveDescription(
 							Uni_and_BigramCountAnnotator.class,
 				    		CleartkAnnotator.PARAM_IS_TRAINING, true,
 							DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
 							DATA_WRITER_NAME,
 							DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
 							modelDirectory));
 		        break;
 		      case CLASSIFY:
 		      default:
 		  		File classifierJarPath = new File(modelDirectory, "model.jar"); 
 				builder.add(AnalysisEngineFactory.createPrimitiveDescription(
 						Uni_and_BigramCountAnnotator.class, 
 						CleartkAnnotator.PARAM_IS_TRAINING,false, 
 						GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
 						classifierJarPath));
 		        break;
 		    }
 		    return builder;
 		  }
 
 
 	public static void main(String[] args) throws Exception {
 
 		Options options = new Options();
 		
 		try {
			options.parseOptions(args);
 		 

 			boolean error = false;
 			
 			if (options.train && options.predict) {	
 				String errString = "Only one of -predict or -train can be specified.";
 				throw new Exception(errString);
 			}
 	
 			if (!options.train && !options.predict) {
 				String errString = "One of -predict or -train should be specified.";
 				throw new Exception(errString);
 			}
 	
 			if ((options.triageCorpus == null || options.triageCorpus.length() == 0) &&
 					options.predict) {
 				String errString = "-triageCorpus is required if -predict is used.";
 				throw new Exception(errString);
 			}
 			
 		} catch (Exception e) {
			CmdLineParser parser = new CmdLineParser(options);
 			System.err.print("Usage: ");
 			parser.printSingleLineUsage(System.err);
			System.err.println("\n Options: \n");
 			parser.printUsage(System.err);
 			System.exit(-1);
 		}
 		
 		if (options.homeDir == null) {
 			System.out.println("homeDir parameter not specified. Using user's home directory instead.");
 			options.homeDir = new File(System.getProperty("user.home") + "/bmkeg");
 		}
 		
 		if (!options.homeDir.exists()) {
 			System.out.println("Home directory [" + options.homeDir.getAbsolutePath() + "] doesn't exists. Creating it ...");
 			options.homeDir.mkdirs();
 		}
 		
 		File modelDir = new File(options.homeDir,options.dbName + "/" + options.targetCorpus);
 		
 		if (!modelDir.exists()) modelDir.mkdirs();
 		
 
 		TriageDocumentsClassifier cl = new TriageDocumentsClassifier(
 				options.triageCorpus,
 				options.targetCorpus,
 				modelDir,
 				options.login, 
 				options.password, 
 				options.dbName);
 
 		if (options.train) {
 			
 			System.out.println("Training Triage Documents Classifier.");
 			System.out.println("TriageCorpus: " + options.triageCorpus);
 			System.out.println("TargetCorpus: " + options.targetCorpus);
 			System.out.println("Model dir: " + modelDir);
 
 			long startTime = System.currentTimeMillis();
 
 			cl.train();			
 			
 			long endTime = System.currentTimeMillis();
 			
 			System.out.println("Elapsed time in mins: " + (endTime - startTime) / 60000);
 
 		} else {
 			
 			System.out.println("Predicting Triage Documents Classifier.");
 			System.out.println("TriageCorpus: " + options.triageCorpus);
 			System.out.println("TargetCorpus: " + options.targetCorpus);
 			System.out.println("Model dir: " + modelDir);
 
 			long startTime = System.currentTimeMillis();
 
 			cl.predict();			
 
 			long endTime = System.currentTimeMillis();
 			
 			System.out.println("Elapsed time in mins: " + (endTime - startTime) / 60000);
 		
 		}
 		
 	}
 
 }
