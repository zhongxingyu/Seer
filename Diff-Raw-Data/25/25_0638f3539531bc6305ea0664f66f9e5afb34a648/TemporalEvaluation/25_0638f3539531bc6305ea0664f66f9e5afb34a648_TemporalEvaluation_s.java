 package edu.uw.cs.lil.tiny.tempeval;
 
 import edu.uw.cs.lil.tiny.ccg.categories.ICategoryServices;
 import edu.uw.cs.lil.tiny.ccg.categories.syntax.Syntax;
 import edu.uw.cs.lil.tiny.data.sentence.Sentence;
 import edu.uw.cs.lil.tiny.mr.lambda.FlexibleTypeComparator;
 import edu.uw.cs.lil.tiny.mr.lambda.LogicLanguageServices;
 import edu.uw.cs.lil.tiny.mr.lambda.LogicalExpression;
 import edu.uw.cs.lil.tiny.mr.lambda.ccg.LogicalExpressionCategoryServices;
 import edu.uw.cs.lil.tiny.mr.lambda.ccg.SimpleFullParseFilter;
 import edu.uw.cs.lil.tiny.mr.language.type.TypeRepository;
 import edu.uw.cs.lil.tiny.parser.ccg.cky.AbstractCKYParser;
 import edu.uw.cs.lil.tiny.parser.ccg.cky.CKYBinaryParsingRule;
 import edu.uw.cs.lil.tiny.parser.ccg.cky.single.CKYParser;
 import edu.uw.cs.lil.tiny.parser.ccg.features.basic.LexicalFeatureSet;
 import edu.uw.cs.lil.tiny.parser.ccg.features.basic.scorer.ExpLengthLexicalEntryScorer;
 import edu.uw.cs.lil.tiny.parser.ccg.features.basic.scorer.SkippingSensitiveLexicalEntryScorer;
 import edu.uw.cs.lil.tiny.parser.ccg.features.basic.scorer.UniformScorer;
 import edu.uw.cs.lil.tiny.parser.ccg.lexicon.ILexicon;
 import edu.uw.cs.lil.tiny.parser.ccg.lexicon.LexicalEntry;
 import edu.uw.cs.lil.tiny.parser.ccg.lexicon.Lexicon;
 import edu.uw.cs.lil.tiny.parser.ccg.rules.RuleSetBuilder;
 import edu.uw.cs.lil.tiny.parser.ccg.rules.primitivebinary.BackwardApplication;
 import edu.uw.cs.lil.tiny.parser.ccg.rules.primitivebinary.BackwardComposition;
 import edu.uw.cs.lil.tiny.parser.ccg.rules.primitivebinary.ForwardApplication;
 import edu.uw.cs.lil.tiny.parser.ccg.rules.primitivebinary.ForwardComposition;
 import edu.uw.cs.lil.tiny.parser.ccg.rules.skipping.BackwardSkippingRule;
 import edu.uw.cs.lil.tiny.parser.ccg.rules.skipping.ForwardSkippingRule;
 import edu.uw.cs.lil.tiny.tempeval.preprocessing.TemporalReader;
 import edu.uw.cs.lil.tiny.tempeval.structures.TemporalSentence;
 import edu.uw.cs.lil.tiny.tempeval.structures.TemporalDataset;
 import edu.uw.cs.lil.tiny.utils.string.StubStringFilter;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 public class TemporalEvaluation {
 	private static final String RESOURCES_DIR = "data/resources/";
 	final private static String DATASET_DIR = "data/TempEval3/TBAQ-cleaned/";
 	final private static String[] DATASETS =  {"AQUAINT", "TimeBank"};
 	//final private static String[] DATASETS =  {"debug_dataset"};
 
	private static final boolean FORCE_SERIALIZATION = true;
 	private static final boolean CROSS_VALIDATION = false;
 	private static final int CV_FOLDS = 10;
 	private static final int PERCEPTRON_ITERATIONS = 1;
 	private ICategoryServices<LogicalExpression> categoryServices;
 	private ILexicon<LogicalExpression> fixed;
 	private LexicalFeatureSet<Sentence, LogicalExpression> lexPhi;
 	private TemporalJointParser jointParser;
 	private TemporalDataset dataset;
 
 	public TemporalEvaluation(String datasetDirectory, String[] datasets) throws SAXException, IOException, ParserConfigurationException, ClassNotFoundException {
 		LogicLanguageServices.setInstance(new LogicLanguageServices.Builder(
 				new TypeRepository(new File(RESOURCES_DIR + "tempeval.types.txt"))).setNumeralTypeName("i")
 				.setTypeComparator(new FlexibleTypeComparator()).build());
 		categoryServices = new LogicalExpressionCategoryServices();
 		fixed = getFixedLexicon(categoryServices);
 		lexPhi = getLexPhi(categoryServices);
 		jointParser = new TemporalJointParser(getParser(categoryServices));
 		dataset = new TemporalReader().getDataset(datasetDirectory, datasets, FORCE_SERIALIZATION);
 	}
 
 
 	public static ILexicon<LogicalExpression> getFixedLexicon(ICategoryServices<LogicalExpression> categoryServices) {
 		final ILexicon<LogicalExpression> fixedInput = new Lexicon<LogicalExpression>();
 		fixedInput.addEntriesFromFile(new File(RESOURCES_DIR
 				+ "tempeval.lexicon.txt"), new StubStringFilter(),
 				categoryServices, LexicalEntry.Origin.FIXED_DOMAIN);
 
 		final ILexicon<LogicalExpression> fixed = new Lexicon<LogicalExpression>();
 		for (final LexicalEntry<LogicalExpression> lex : fixedInput.toCollection()){
 			fixed.add(lex);
 		}
 		return fixed;
 	}
 
 	public static LexicalFeatureSet<Sentence, LogicalExpression> getLexPhi(ICategoryServices<LogicalExpression> categoryServices) {
 		return new LexicalFeatureSet.Builder<Sentence,LogicalExpression>()
 				.setInitialFixedScorer(
 						new ExpLengthLexicalEntryScorer<LogicalExpression>(
 								10.0, 1.1))
 								.setInitialScorer(
 										new SkippingSensitiveLexicalEntryScorer<LogicalExpression>(
 												categoryServices,
 												-1.0,
 												new UniformScorer<LexicalEntry<LogicalExpression>>(
 														0.0)))
 														.build();
 	}
 
 	public static AbstractCKYParser<LogicalExpression> getParser (ICategoryServices<LogicalExpression> categoryServices) {
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
 
 		final Set<Syntax> syntaxSet = new HashSet<Syntax>();
 		syntaxSet.add(Syntax.NP);
 		final SimpleFullParseFilter<LogicalExpression> parseFilter = new SimpleFullParseFilter<LogicalExpression>(
 				syntaxSet);
 		return new CKYParser.Builder<LogicalExpression>(
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
 
 
 	}
 
 	public void evaluate() {
 		System.out.printf("Evaluating %d sentences...\n", dataset.size());
 		if (CROSS_VALIDATION){
 			List<List<TemporalSentence>> partitions = dataset.partition(CV_FOLDS);
 			TemporalEvaluationThread[] threads = new TemporalEvaluationThread[partitions.size()];
 			for (int i = 0; i < partitions.size(); i++){
 				TemporalDataset trainData = new TemporalDataset();
 				TemporalDataset testData = new TemporalDataset(partitions.get(i));
 				for (int j = 0; j < partitions.size(); j++)
 					if (j != i)
 						trainData.addSentences(partitions.get(j));
 
 				threads[i] = new TemporalEvaluationThread(trainData, testData, jointParser, fixed, lexPhi, PERCEPTRON_ITERATIONS, i);
 				threads[i].start();
 			}
 			for (int i = 0; i < threads.length; i++){
 				try{
 					threads[i].join();
 				} catch (InterruptedException e){
 					e.printStackTrace();
 					System.err.println("Some problems getting the threads to join again!");
 				}
 			}
 		} else {
 			// Train and test on the same dataset for debugging
 			new TemporalEvaluationThread(dataset, dataset, jointParser, fixed, lexPhi, PERCEPTRON_ITERATIONS, -1).run();
 		}
 		System.out.println("Done");
 	}
 	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, ClassNotFoundException {
 		new TemporalEvaluation(DATASET_DIR, DATASETS).evaluate();
 	}
 }
