 package edu.uw.cs.lil.tiny.tempeval;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.TreeMap;
 
 import edu.uw.cs.lil.tiny.data.IDataItem;
 import edu.uw.cs.lil.tiny.data.sentence.Sentence;
 import edu.uw.cs.lil.tiny.mr.lambda.LogicalExpression;
 import edu.uw.cs.lil.tiny.mr.lambda.ccg.LogicalExpressionCategoryServices;
 import edu.uw.cs.lil.tiny.parser.AbstractParser;
 import edu.uw.cs.lil.tiny.parser.IParseResult;
 import edu.uw.cs.lil.tiny.parser.IParserOutput;
 import edu.uw.cs.lil.tiny.parser.Pruner;
 import edu.uw.cs.lil.tiny.parser.ccg.cky.AbstractCKYParser;
 import edu.uw.cs.lil.tiny.parser.ccg.lexicon.ILexicon;
 import edu.uw.cs.lil.tiny.parser.ccg.model.IDataItemModel;
 import edu.uw.cs.lil.tiny.parser.joint.IJointOutput;
 import edu.uw.cs.lil.tiny.parser.joint.IJointParse;
 import edu.uw.cs.lil.tiny.parser.joint.IJointParser;
 import edu.uw.cs.lil.tiny.parser.joint.JointOutput;
 import edu.uw.cs.lil.tiny.parser.joint.model.IJointDataItemModel;
 import edu.uw.cs.lil.tiny.tempeval.structures.TemporalMention;
 import edu.uw.cs.lil.tiny.tempeval.structures.TemporalResult;
 import edu.uw.cs.lil.tiny.tempeval.structures.TemporalSentence;
 import edu.uw.cs.lil.tiny.tempeval.types.TemporalISO;
 import edu.uw.cs.utils.composites.Pair;
 
 /**
  * @author Jesse Dodge and Yoav Artsi
  * @param <X>
  *            Sentence Type of the natural language structure (e.g., Sentence).
  * @param <W>
  *            String[] contains docID, complete sentence, refdate in the
  *            String[] phrase in the Sentence, and previous reference in the
  *            TemporalSentence Type of added input information (e.g., task,
  *            starting position, state of the world etc.).
  * @param <Y>
  *            LogicalExpression. Type of meaning representation (e.g.,
  *            LogicalExpression).
  * @param <Z>
  *            Pair<String, String>. pair.first() is the type, pair.second() is
  *            the value. Type of execution output.
  */
 
 public class TemporalJointParser extends AbstractParser<Sentence, LogicalExpression> implements IJointParser<Sentence, TemporalMention, LogicalExpression, LogicalExpression, TemporalResult> {
 
 	private AbstractCKYParser<LogicalExpression> baseParser;
 	private final LogicalExpressionCategoryServices categoryServices;
 	private TemporalISO prevISO;
 	private String prevDocID;
 
 	// Constructor takes the CKY parser.
 	TemporalJointParser(AbstractCKYParser<LogicalExpression> baseParser) {
 		this.baseParser = baseParser;
 		categoryServices = new LogicalExpressionCategoryServices();
 		prevISO = null;
 		prevDocID = "";
 	}
 
 	@Override
 	public IParserOutput<LogicalExpression> parse(IDataItem<Sentence> dataItem,
 			Pruner<Sentence, LogicalExpression> pruner,
 			IDataItemModel<LogicalExpression> model, boolean allowWordSkipping,
 			ILexicon<LogicalExpression> tempLexicon, Integer beamSize) {
 		return baseParser.parse(dataItem, pruner, model, allowWordSkipping,
 				tempLexicon, beamSize);
 	}
 
 	// To prune the parses that have lambdas. My logical expressions shouldn't
 	// have a complex type, or they wont execute.
 	private List<IParseResult<LogicalExpression>> pruneLogicWithLambdas(
 			List<IParseResult<LogicalExpression>> bestModelParses) {
 		List<IParseResult<LogicalExpression>> newBestModelParses = new ArrayList<IParseResult<LogicalExpression>>();
 		for (IParseResult<LogicalExpression> p : bestModelParses) {
 			if (!p.getY().getType().isComplex())
 				newBestModelParses.add(p);
 		}
 		return newBestModelParses;
 	}
 
 	/*
 	 * To perform the first step of the execution - getting all possible
 	 * context-dependent logical forms.
 	 */
 	private LogicalExpression[] getArrayOfLabels(LogicalExpression l,
 			boolean sameDocID) {
		int numOfFunctions = 4;
 		LogicalExpression[] newLogicArray = new LogicalExpression[numOfFunctions + 1];
 		LogicalExpression[] functionsS = new LogicalExpression[numOfFunctions];
 		LogicalExpression[] functionsD = new LogicalExpression[numOfFunctions];
 		// Making the Predicates to apply to the logical expressions for
 		// SEQUENCES
 		functionsS[0] = categoryServices
 				.parseSemantics("(lambda $0:s (previous:<s,<r,s>> $0 ref_time:r))");
 		functionsS[1] = categoryServices
 				.parseSemantics("(lambda $0:s (this:<s,<r,s>> $0 ref_time:r))");
 		functionsS[2] = categoryServices
 				.parseSemantics("(lambda $0:s (next:<s,<r,s>> $0 ref_time:r))");
 		if (sameDocID)
 			functionsS[3] = categoryServices
 			.parseSemantics("(lambda $0:d (temporal_ref:<d,s> $0))");
		else
			functionsS[3] = l;

 
 		// Making the Predicates to apply to the logical expressions for
 		// DURATIONS
 		// Or reall, don't, because this only confuses the system.
 		functionsD[0] = categoryServices
 				.parseSemantics("(lambda $0:d (previous:<d,<r,s>> $0 ref_time:r))");
 		functionsD[1] = categoryServices
 				.parseSemantics("(lambda $0:d (this:<d,<r,s>> $0 ref_time:r))");
 		functionsD[2] = categoryServices
 				.parseSemantics("(lambda $0:d (next:<d,<r,s>> $0 ref_time:r))");
 		if (sameDocID)
 			functionsD[3] = categoryServices
 			.parseSemantics("(lambda $0:d (temporal_ref:<d,s> $0))");
		else
			functionsD[3] = l;

 
 		// Looping over the predicates, applying them each to the given logical
 		// expression
 		for (int i = 0; i < functionsS.length; i++) {
 			if (!logicStartsWithContextDependentPredicate(l)){
 				newLogicArray[i + 1] = categoryServices.doSemanticApplication(
 						functionsS[i], l);
 			}
 			//if (newLogicArray[i + 1] == null && i == 3) {
 			//	newLogicArray[i + 1] = categoryServices.doSemanticApplication(
 			//			functionsD[i], l);
 			//}
 
 			if (newLogicArray[i + 1] == null)
 				newLogicArray[i + 1] = l;
 
 		}
		newLogicArray[0] = l;
 
 		return newLogicArray;
 	}
 
 	private boolean logicStartsWithContextDependentPredicate(LogicalExpression l){
 		return (l.toString().startsWith("(previous:") || 
 				l.toString().startsWith("(this:") ||
 				l.toString().startsWith("(next:"));
 	}
 
 	@Override
 	public IJointOutput<LogicalExpression, TemporalResult> parse(
 			IDataItem<Pair<Sentence, TemporalMention>> dataItem,
 			IJointDataItemModel<LogicalExpression, LogicalExpression> model) {
 		return parse(dataItem, model, false);
 	}
 
 	@Override
 	public IJointOutput<LogicalExpression, TemporalResult> parse(
 			IDataItem<Pair<Sentence, TemporalMention>> dataItem,
 			IJointDataItemModel<LogicalExpression, LogicalExpression> model,
 			boolean allowWordSkipping) {
 		return parse(dataItem, model, allowWordSkipping, null);
 	}
 
 	@Override
 	public IJointOutput<LogicalExpression, TemporalResult> parse(
 			IDataItem<Pair<Sentence, TemporalMention>> dataItem,
 			IJointDataItemModel<LogicalExpression, LogicalExpression> model,
 			boolean allowWordSkipping, ILexicon<LogicalExpression> tempLexicon) {
 		return parse(dataItem, model, allowWordSkipping, tempLexicon, -1);
 	}
 
 	// this is where the parsing happens. 
 	@Override
 	public IJointOutput<LogicalExpression, TemporalResult> parse(
 			IDataItem<Pair<Sentence, TemporalMention>> dataItem,
 			IJointDataItemModel<LogicalExpression, LogicalExpression> model,
 			boolean allowWordSkipping, ILexicon<LogicalExpression> tempLexicon,
 			Integer beamSize) {
 
 		TemporalSentence ts = dataItem.getSample().second().getSentence();
 		String docID = ts.getDocID();
		boolean hasPreviousObservation = prevDocID.equals(docID) && prevISO != null;
 		Sentence phrase = dataItem.getSample().first();
 		IParserOutput<LogicalExpression> CKYParserOutput = baseParser.parse(phrase, model);
 
 		final List<IParseResult<LogicalExpression>> CKYModelParses = pruneLogicWithLambdas(CKYParserOutput
 				.getBestParses());
 
 		List<IJointParse<LogicalExpression, TemporalResult>> allExecutedParses = 
 				new ArrayList<IJointParse<LogicalExpression, TemporalResult>>();
 
 		long startTime = System.currentTimeMillis();
 
 		// to store the temporalISOs, so i can choose the highest scoring one to keep in prevISO
 		TreeMap<Double, TemporalISO> ISOsByScore = new TreeMap<Double, TemporalISO>();
 
 		for (IParseResult<LogicalExpression> l : CKYModelParses) {
			LogicalExpression[] labels = getArrayOfLabels(l.getY(), !hasPreviousObservation);
 			for (int i = 0; i < labels.length; i++) {
 				// execute the logical form to get a final Pair<String, String>.
 				// score the logical form.
 				// create a TemporalExecResultWrapper class using the
 				// Pair<String, String> and the score.
 				// use that wrapper to create
 				String referenceTime = ts.getReferenceTime();
				if (!hasPreviousObservation)
					prevISO = null;
 
 				// TODO: Unsolved mystery: the CKY parser gives different parses depending on the dataset, even if one is a subset of another.
 				TemporalISO tmp = TemporalVisitor.of(labels[i], referenceTime, prevISO);
 
 				TemporalResult tr = new TemporalResult(labels[i], tmp.getType(), tmp.getVal(), l.getAllLexicalEntries(), model, l);
 				IJointParse<LogicalExpression, TemporalResult> jp = tr.getJointParse();
 
 
 				ISOsByScore.put(jp.getScore(), tmp);
 				allExecutedParses.add(jp);
 			}
 		}
 
 
 		if (ISOsByScore.size() > 0) {
 			prevISO = ISOsByScore.lastEntry().getValue();
 			prevDocID = docID;
 		}
 		else { 
 			prevISO = null;
 			prevDocID = "";
 		}
 
 		long parsingTime = System.currentTimeMillis() - startTime;
 		JointOutput<LogicalExpression, TemporalResult> out = new JointOutput<LogicalExpression, TemporalResult>(
 				CKYParserOutput, allExecutedParses, parsingTime);
 		return out;
 	}
 
 }
