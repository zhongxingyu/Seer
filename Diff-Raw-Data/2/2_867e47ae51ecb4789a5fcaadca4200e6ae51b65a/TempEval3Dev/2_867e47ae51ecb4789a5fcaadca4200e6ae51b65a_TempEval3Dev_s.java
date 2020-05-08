 package edu.uw.cs.lil.tiny.tempeval;
 
 import edu.uw.cs.lil.learn.simple.joint.JointSimplePerceptron;
 import edu.uw.cs.lil.tiny.ccg.categories.ICategoryServices;
 import edu.uw.cs.lil.tiny.ccg.categories.syntax.ComplexSyntax;
 import edu.uw.cs.lil.tiny.ccg.categories.syntax.Slash;
 import edu.uw.cs.lil.tiny.ccg.categories.syntax.Syntax;
 import edu.uw.cs.lil.tiny.data.IDataCollection;
 import edu.uw.cs.lil.tiny.data.ILabeledDataItem;
 import edu.uw.cs.lil.tiny.data.sentence.Sentence;
 import edu.uw.cs.lil.tiny.learn.ILearner;
 import edu.uw.cs.lil.tiny.mr.lambda.FlexibleTypeComparator;
 import edu.uw.cs.lil.tiny.mr.lambda.LogicLanguageServices;
 import edu.uw.cs.lil.tiny.mr.lambda.LogicalExpression;
 import edu.uw.cs.lil.tiny.mr.lambda.Ontology;
 import edu.uw.cs.lil.tiny.mr.lambda.ccg.LogicalExpressionCategoryServices;
 import edu.uw.cs.lil.tiny.mr.lambda.ccg.SimpleFullParseFilter;
 import edu.uw.cs.lil.tiny.mr.language.type.TypeRepository;
 import edu.uw.cs.lil.tiny.parser.ccg.cky.AbstractCKYParser;
 import edu.uw.cs.lil.tiny.parser.ccg.cky.CKYBinaryParsingRule;
 import edu.uw.cs.lil.tiny.parser.ccg.cky.multi.MultiCKYParser;
 import edu.uw.cs.lil.tiny.parser.ccg.cky.single.CKYParser;
 import edu.uw.cs.lil.tiny.parser.ccg.cky.single.CKYUnaryParsingRule;
 import edu.uw.cs.lil.tiny.parser.ccg.factoredlex.FactoredLexicon;
 import edu.uw.cs.lil.tiny.parser.ccg.factoredlex.Lexeme;
 import edu.uw.cs.lil.tiny.parser.ccg.factoredlex.features.LexemeFeatureSet;
 import edu.uw.cs.lil.tiny.parser.ccg.factoredlex.features.LexicalTemplateFeatureSet;
 import edu.uw.cs.lil.tiny.parser.ccg.factoredlex.features.scorers.LexemeCooccurrenceScorer;
 import edu.uw.cs.lil.tiny.parser.ccg.features.basic.LexicalFeatureSet;
 import edu.uw.cs.lil.tiny.parser.ccg.features.basic.scorer.ExpLengthLexicalEntryScorer;
 import edu.uw.cs.lil.tiny.parser.ccg.features.basic.scorer.ScalingScorer;
 import edu.uw.cs.lil.tiny.parser.ccg.features.basic.scorer.SkippingSensitiveLexicalEntryScorer;
 import edu.uw.cs.lil.tiny.parser.ccg.features.basic.scorer.UniformScorer;
 import edu.uw.cs.lil.tiny.parser.ccg.features.lambda.LogicalExpressionCoordinationFeatureSet;
 import edu.uw.cs.lil.tiny.parser.ccg.features.lambda.LogicalExpressionTypeFeatureSet;
 import edu.uw.cs.lil.tiny.parser.ccg.lexicon.ILexicon;
 import edu.uw.cs.lil.tiny.parser.ccg.lexicon.LexicalEntry;
 //import edu.uw.cs.lil.tiny.parser.ccg.lexicon.LexicalEntry.EntryOrigin;
 import edu.uw.cs.lil.tiny.parser.ccg.lexicon.Lexicon;
 import edu.uw.cs.lil.tiny.parser.ccg.model.Model;
 import edu.uw.cs.lil.tiny.parser.ccg.rules.RuleSetBuilder;
 import edu.uw.cs.lil.tiny.parser.ccg.rules.primitivebinary.BackwardApplication;
 import edu.uw.cs.lil.tiny.parser.ccg.rules.primitivebinary.BackwardComposition;
 import edu.uw.cs.lil.tiny.parser.ccg.rules.primitivebinary.ForwardApplication;
 import edu.uw.cs.lil.tiny.parser.ccg.rules.primitivebinary.ForwardComposition;
 import edu.uw.cs.lil.tiny.parser.ccg.rules.skipping.BackwardSkippingRule;
 import edu.uw.cs.lil.tiny.parser.ccg.rules.skipping.ForwardSkippingRule;
 import edu.uw.cs.lil.tiny.parser.joint.model.JointDataItemModel;
 import edu.uw.cs.lil.tiny.parser.joint.model.JointModel;
 import edu.uw.cs.lil.tiny.tempeval.featureSets.TemporalContextFeatureSet;
 import edu.uw.cs.lil.tiny.tempeval.featureSets.TemporalDayOfWeekFeatureSet;
 import edu.uw.cs.lil.tiny.tempeval.featureSets.TemporalReferenceFeatureSet;
 import edu.uw.cs.lil.tiny.tempeval.featureSets.TemporalTypeFeatureSet;
 import edu.uw.cs.lil.tiny.utils.concurrency.TinyExecutorService;
 import edu.uw.cs.lil.tiny.utils.string.StubStringFilter;
 import edu.uw.cs.utils.composites.Pair;
 import edu.uw.cs.utils.log.ILogger;
 import edu.uw.cs.utils.log.Log;
 import edu.uw.cs.utils.log.LogLevel;
 import edu.uw.cs.utils.log.Logger;
 import edu.uw.cs.utils.log.LoggerFactory;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 public class TempEval3Dev {
 	private static final ILogger LOG = LoggerFactory.create(TempEval3Dev.class);
 
 	public static void main(String[] args) throws IOException, ClassNotFoundException {
 		boolean readSerializedDatasets = false; // this takes precedence over booleans testingDataset, timebank, and crossVal.
 		boolean serializeDatasets = false;
 		boolean testingDataset = true; // testing dataset takes precidenence over timebank
 		// options for dataSetName: "tempeval3.aquaintAndTimebank.txt", "tempeval3.aquaint.txt", "tempeval3.timebank.txt"
 		String dataSetName = "tempeval3.aquaintAndTimebank.txt";  
		boolean crossVal = false;
 		int numIterations = 1;
 		
 		
 		
 		
 		Logger.DEFAULT_LOG = new Log(System.out);
 		Logger.setSkipPrefix(true);
 		LogLevel.INFO.set();
 
 		//long startTime = System.currentTimeMillis();
 
 		// Relative path for data directories
 		String datasetDir = "./data/dataset/";
 		String resourcesDir = "./data/resources/";
 
 		
 		// Init the logical expression type system
 		// LogicLanguageServices.setInstance(new LogicLanguageServices(
 		//		new TypeRepository(
 		//				new File(resourcesDir + "tempeval.types.txt")), "i"));
 		
 		LogicLanguageServices.setInstance(new LogicLanguageServices.Builder(
 				new TypeRepository(new File(resourcesDir + "tempeval.types.txt"))).setNumeralTypeName("i")
 				.setTypeComparator(new FlexibleTypeComparator()).build());
 
 		
 		final ICategoryServices<LogicalExpression> categoryServices = new LogicalExpressionCategoryServices();
 		
 
 		// Load the ontology
 		List<File> ontologyFiles = new LinkedList<File>();
 		ontologyFiles.add(new File(resourcesDir + "tempeval.predicates.txt"));
 		ontologyFiles.add(new File(resourcesDir + "tempeval.constants.txt"));
 		try {
 			// Ontology is currently not used, so we are just reading it, not
 			// storing
 			new Ontology(ontologyFiles);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		
 		// When running on testing dataset, testingDataset = true
 		String dataLoc;
 		if (testingDataset)
 			dataLoc = "tempeval3.testing.txt";
 		else{
 			dataLoc = dataSetName;			
 		}
 		
 			//dataLoc = "tempeval.dataset.corrected.txt";
 		// these train and test should be of type
 		// IDataCollection<? extends ILabeledDataItem<Pair<Sentence, String[]>, Pair<String, String>>> 
 		TemporalSentenceDataset train = null;
 		TemporalSentenceDataset test = null;
 		// reading in the serialized datasets so we don't have to run the dependency parser again.
 		if (readSerializedDatasets){
 			train = TemporalSentenceDataset.readSerialized("data/serialized_data/trainingData.ser");
 			test = TemporalSentenceDataset.readSerialized("data/serialized_data/testingData.ser");
 		} else {
 			train = TemporalSentenceDataset
 					.read(new File(datasetDir + dataLoc),
 							new StubStringFilter(), true);
 			test = TemporalSentenceDataset
 					.read(new File(datasetDir + dataLoc),
 							new StubStringFilter(), true);
 		}
 		
 		if (serializeDatasets){
 			System.out.print("Serializing the training data... ");
 			TemporalSentenceDataset.save("data/serialized_data/trainingData.ser", train);
 			System.out.println("Done!");
 			System.out.print("Serializing the testing data... ");
 			TemporalSentenceDataset.save("data/serialized_data/testingData.ser", test);
 			System.out.println("Done!");
 		}
 		LOG.info("Train Size: " + train.size());
 		LOG.info("Test Size: " + test.size());
 
 		// Below is the code from Geo880DevParameterEstimation. I am replacing this with code from Geo880Dev.
 		/*
 		// Init the lexicon
 		final ILexicon<LogicalExpression> lexicon = new Lexicon<LogicalExpression>();
 		lexicon.addEntriesFromFile(
 				new File(
 						resourcesDir + "tempeval.lexicon.txt"),
 				new StubStringFilter(), categoryServices,
 				EntryOrigin.FIXED_DOMAIN);
 
 		LOG.info("Size of lexicon: %d", lexicon.size());
 
 		LOG.info("Size of lexicon: %d", lexicon.size());
 
 		final LexicalFeatureSet<LogicalExpression> lexPhi = new LexicalFeatureSetBuilder<LogicalExpression>()
 				.setInitialFixedScorer(
 						new ExpLengthLexicalEntryScorer<LogicalExpression>(
 								10.0, 1.1))
 				.setInitialScorer(
 						new SkippingSensitiveLexicalEntryScorer<LogicalExpression>(
 								categoryServices,
 								-1.0,
 								new UniformScorer<LexicalEntry<LogicalExpression>>(
 										0.0))).build();
 
 		// Create the model
 		final Model<Sentence, LogicalExpression> model = new Model.Builder<Sentence, LogicalExpression>()
 				.addParseFeatureSet(
 						new LogicalExpressionCoordinationFeatureSet<Sentence>())
 				.addParseFeatureSet(
 						new LogicalExpressionTypeFeatureSet<Sentence>())
 				.addLexicalFeatureSet(lexPhi)
 				.setLexicon(new Lexicon<LogicalExpression>()).build();
 		*/
 		
 		// Init the lexicon
 				// final Lexicon<LogicalExpression> fixed = new
 				// Lexicon<LogicalExpression>();
 		final ILexicon<LogicalExpression> fixedInput = new Lexicon<LogicalExpression>();
 		fixedInput.addEntriesFromFile(new File(resourcesDir
 				+ "tempeval.lexicon.txt"), new StubStringFilter(),
 				categoryServices, LexicalEntry.Origin.FIXED_DOMAIN);
 		
 		final ILexicon<LogicalExpression> fixed = new Lexicon<LogicalExpression>();
 		
 		// factor the fixed lexical entries
 		//for (final LexicalEntry<LogicalExpression> lex : fixedInput
 		//		.toCollection()) {
 		//	fixed.add(FactoredLexicon.factor(lex));
 		//}
 		//System.out.println(fixed);
 		
 		// try two at factoring the fixed lexical entries:
 		for (final LexicalEntry<LogicalExpression> lex : fixedInput.toCollection()){
 		    fixed.add(lex);
 		}
 		
 		final LexicalFeatureSet<Sentence, LogicalExpression> lexPhi = new LexicalFeatureSet.Builder<Sentence,LogicalExpression>()
 				.setInitialFixedScorer(
 						new ExpLengthLexicalEntryScorer<LogicalExpression>(
 								10.0, 1.1))
 				.setInitialScorer(
 						new SkippingSensitiveLexicalEntryScorer<LogicalExpression>(
 								categoryServices,
 								-1.0,
 								new UniformScorer<LexicalEntry<LogicalExpression>>(
 										0.0)))
 				// .setInitialWeightScorer(gizaScores)
 				.build();
 				
 				// Create the lexeme feature set
 				//final LexemeCooccurrenceScorer gizaScores;
 				// final DecoderHelper<LogicalExpression> decoderHelper = new
 				// DecoderHelper<LogicalExpression>(
 				// categoryServices);
 				//try {
 				//	gizaScores = new LexemeCooccurrenceScorer(new File(resourcesDir
 				//			+ "/geo600.dev.giza_probs"));
 				//} catch (final IOException e) {
 				//	System.err.println(e);
 				//	throw new RuntimeException(e);
 				//}
 				//final LexemeFeatureSet lexemeFeats = new LexemeFeatureSet.Builder()
 				//		.setInitialFixedScorer(new UniformScorer<Lexeme>(0.0))
 				//		.setInitialScorer(new ScalingScorer<Lexeme>(10.0, gizaScores))
 				//		.build();
 				
 				// This was used for initializing the factored lexicon
 				final LexicalTemplateFeatureSet templateFeats = new LexicalTemplateFeatureSet.Builder()
 						.setScale(0.1)
 						// .setInitialWeightScorer(new LexicalSyntaxPenaltyScorer(-0.1))
 						.build();
 				
 				// Create the entire feature collection
 				// Adjusted to move away from a factored lexicon
 
 		// Parsing rules
 		final RuleSetBuilder<LogicalExpression> ruleSetBuilder = new RuleSetBuilder<LogicalExpression>();
 
 		// Binary rules
 		ruleSetBuilder.add(new ForwardComposition<LogicalExpression>(
 				categoryServices));
 		ruleSetBuilder.add(new BackwardComposition<LogicalExpression>(
 				categoryServices));
 		ruleSetBuilder.add(new ForwardApplication<LogicalExpression>(
 				categoryServices));
 		ruleSetBuilder.add(new BackwardApplication<LogicalExpression>(
 				categoryServices));
 
 		// Executor for multi-threading
 		final TinyExecutorService executor = new TinyExecutorService(Runtime
 				.getRuntime().availableProcessors());
 
 		LOG.info("Using %d threads", Runtime.getRuntime().availableProcessors());
 
 		final Set<Syntax> syntaxSet = new HashSet<Syntax>();
 		syntaxSet.add(Syntax.NP);
 		
 		final SimpleFullParseFilter<LogicalExpression> parseFilter = new SimpleFullParseFilter<LogicalExpression>(
 				syntaxSet);
 		
 		final AbstractCKYParser<LogicalExpression> parser = new CKYParser.Builder<LogicalExpression>(
 				categoryServices, parseFilter)//, executor)
 				.addBinaryParseRule(
 						new CKYBinaryParsingRule<LogicalExpression>(
 								ruleSetBuilder.build()))
 				.addBinaryParseRule(
 						new CKYBinaryParsingRule<LogicalExpression>(
 								new ForwardSkippingRule<LogicalExpression>(
 										categoryServices)))
 				.addBinaryParseRule(
 						new CKYBinaryParsingRule<LogicalExpression>(
 								new BackwardSkippingRule<LogicalExpression>(
 										categoryServices)))
 				.setMaxNumberOfCellsInSpan(100).build();
 
 		
 		
 		
 		// Crossvalidation starts here.
 		if (crossVal){
 			double numberOfPartitions = 3.0;
 			// make a list
 			// use the constructor with TemporalSentenceDataset to make a new dataset. 
 			System.out.println("Splitting the data...");
 			List<List<TemporalSentence>> splitData = 
 					new LinkedList<List<TemporalSentence>>();
 			Iterator<TemporalSentence> iter = train.iterator();
 			int sentenceCount = 1;
 			List<TemporalSentence> tmp = 
 					new LinkedList<TemporalSentence>();
 			
 			while (iter.hasNext()){
 				tmp.add(iter.next());
 				// for testing:
 				if (sentenceCount % Math.round(train.size() / numberOfPartitions)== 0){
 					splitData.add(tmp);
 					tmp = new LinkedList<TemporalSentence>();
 					System.out.println();
 					System.out.println("sentenceCount: " + sentenceCount);
 					System.out.println("Train size: " + train.size());
 					System.out.println("size / " + numberOfPartitions + ": " + Math.round(train.size() / numberOfPartitions));
 					
 				}
 
 				sentenceCount++;
 			}
 			System.out.println(" done.");
 			OutputData[] outList = new OutputData[splitData.size()];
 			
 			// to make the threads
 			TemporalThread[] threads = new TemporalThread[splitData.size()];
 			
 			for (int i = 0; i < splitData.size(); i++){
 				// to make the training and testing corpora
 				List<TemporalSentence> newTrainList = new LinkedList<TemporalSentence>();
 				List<TemporalSentence> newTestList = new LinkedList<TemporalSentence>();
 				for (int j = 0; j < splitData.size(); j++){
 					if (i == j)
 						newTestList.addAll(splitData.get(i));
 					else
 						newTrainList.addAll(splitData.get(j));
 				}
 				// to make them into IDataCollection items:
 				IDataCollection<? extends ILabeledDataItem<Pair<Sentence, String[]>, TemporalResult>> newTest = 
 						new TemporalSentenceDataset(newTestList);
 				IDataCollection<? extends ILabeledDataItem<Pair<Sentence, String[]>, TemporalResult>> newTrain = 
 						new TemporalSentenceDataset(newTrainList);
 				
 				// Creating a joint parser.
 				final TemporalJointParser jParser = new TemporalJointParser(parser);
 
 				final TemporalTesterSmall tester = TemporalTesterSmall.build(newTest, jParser);
 				final ILearner<Sentence, LogicalExpression, JointModel<Sentence, String[], LogicalExpression, LogicalExpression>> learner =
 						new JointSimplePerceptron<Sentence, String[], LogicalExpression, LogicalExpression, TemporalResult>(
 								numIterations, newTrain, jParser);
 
 				
 				final JointModel<Sentence, String[], LogicalExpression, LogicalExpression> model
 				= new JointModel.Builder<Sentence, String[], LogicalExpression, LogicalExpression>()
 						//.addParseFeatureSet(
 						//		new LogicalExpressionCoordinationFeatureSet<Sentence>(true, true, true))
 						//.addParseFeatureSet(
 						//		new LogicalExpressionTypeFeatureSet<Sentence>())
 						.addJointFeatureSet(new TemporalContextFeatureSet())
 						.addJointFeatureSet(new TemporalReferenceFeatureSet())
 						.addJointFeatureSet(new TemporalTypeFeatureSet())
 						.addJointFeatureSet(new TemporalDayOfWeekFeatureSet())
 						.addLexicalFeatureSet(lexPhi)//.addLexicalFeatureSet(lexemeFeats)
 						//.addLexicalFeatureSet(templateFeats)
 						.setLexicon(new Lexicon<LogicalExpression>()).build();
 				// Initialize lexical features. This is not "natural" for every lexical
 				// feature set, only for this one, so it's done here and not on all
 				// lexical feature sets.
 				model.addFixedLexicalEntries(fixed.toCollection());
 
 
 				OutputData outputData = new OutputData();
 				
 				threads[i] = new TemporalThread(learner, tester, i, outputData, model);
 				threads[i].start();
 				outList[i] = outputData;
 			}
 			for (int i = 0; i < threads.length; i++){
 				try{
 					threads[i].join();
 				} catch (InterruptedException e){
 					e.printStackTrace();
 					System.err.println("Some problems getting the threads to join again!");
 				}
 			}
 			PrintStream out = new PrintStream(new File("output/totals.txt"));
 			OutputData averaged = OutputData.average(outList);
 			out.println(averaged);
 			out.close();
 		// Not crossval
 		} else {
 			// Creating a joint parser.
 			final TemporalJointParser jParser = new TemporalJointParser(parser);
 
 			final JointModel<Sentence, String[], LogicalExpression, LogicalExpression> model
 			= new JointModel.Builder<Sentence, String[], LogicalExpression, LogicalExpression>()
 					//.addParseFeatureSet(
 					//		new LogicalExpressionCoordinationFeatureSet<Sentence>(true, true, true))
 					//.addParseFeatureSet(
 					//		new LogicalExpressionTypeFeatureSet<Sentence>())
 					.addJointFeatureSet(new TemporalContextFeatureSet())
 					.addJointFeatureSet(new TemporalReferenceFeatureSet())
 					.addJointFeatureSet(new TemporalTypeFeatureSet())
 					.addJointFeatureSet(new TemporalDayOfWeekFeatureSet())
 					.addLexicalFeatureSet(lexPhi)//.addLexicalFeatureSet(lexemeFeats)
 					//.addLexicalFeatureSet(templateFeats)
 					.setLexicon(new Lexicon<LogicalExpression>()).build();
 			// Initialize lexical features. This is not "natural" for every lexical
 			// feature set, only for this one, so it's done here and not on all
 			// lexical feature sets.
 			model.addFixedLexicalEntries(fixed.toCollection());
 			
 			final TemporalTesterSmall tester = TemporalTesterSmall.build(test, jParser);
  
 		
 			final ILearner<Sentence, LogicalExpression, JointModel<Sentence, String[], LogicalExpression, LogicalExpression>> learner = new
 					JointSimplePerceptron<Sentence, String[], LogicalExpression, LogicalExpression, TemporalResult>(
 							numIterations, train, jParser);
 			
 			learner.train(model);
 
 		// Within this tester, I should go through each example and use the
 		// visitor on each logical expression!
 			OutputData o = new OutputData();
 			tester.test(model, System.out,o);
 		}
 		//LOG.info("Total runtime %.4f seconds", Double.valueOf(System
 		//		.currentTimeMillis() - startTime / 1000.0D));
 	}
 }
