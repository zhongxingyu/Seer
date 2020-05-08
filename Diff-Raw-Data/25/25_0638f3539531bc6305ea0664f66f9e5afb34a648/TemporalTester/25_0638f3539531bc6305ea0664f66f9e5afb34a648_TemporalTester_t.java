 package edu.uw.cs.lil.tiny.tempeval;
 
 import java.io.PrintStream;
 import java.util.*;
 
 import edu.uw.cs.lil.tiny.data.IDataCollection;
 import edu.uw.cs.lil.tiny.data.ILabeledDataItem;
 import edu.uw.cs.lil.tiny.data.sentence.Sentence;
 import edu.uw.cs.lil.tiny.mr.lambda.LogicalExpression;
 import edu.uw.cs.lil.tiny.parser.ccg.lexicon.LexicalEntry;
 import edu.uw.cs.lil.tiny.parser.joint.IJointOutput;
 import edu.uw.cs.lil.tiny.parser.joint.IJointParse;
 import edu.uw.cs.lil.tiny.parser.joint.model.*;
 import edu.uw.cs.lil.tiny.tempeval.structures.TemporalObservationDataset;
 import edu.uw.cs.lil.tiny.tempeval.structures.TemporalResult;
 import edu.uw.cs.lil.tiny.tempeval.util.GovernerVerbPOSExtractor;
 import edu.uw.cs.lil.tiny.tempeval.util.OutputData;
 import edu.uw.cs.lil.tiny.utils.hashvector.IHashVector;
 import edu.uw.cs.utils.composites.Pair;
 
 public class TemporalTester {
	private final boolean PRINTRESULTS = false;
 	private final boolean ONLYPRINTINCORRECT = false;
 	private final boolean ONLYPRINTTOOMANYPARSES = false;
 	private final boolean ONLYPRINTNOPARSES = false;
 	private final boolean ONLYPRINTONEPHRASE = false;
 	private final String PHRASE = "a year earlier";
 	private final TemporalObservationDataset test;
 	private final TemporalJointParser jointParser;
 	private PrintStream out;
 	OutputData outputData;
 
 
 	private TemporalTester(TemporalObservationDataset test,	TemporalJointParser jointParser) {
 		this.test = test;
 		this.jointParser = jointParser;
 	}
 
 	public void test(JointModel<Sentence, String[], LogicalExpression, LogicalExpression> model, 
 			PrintStream out, OutputData outData) {
 		this.out = out;
 		outputData = outData;
 		test(test, model);
 	}
 
 	private void test(
 			IDataCollection<? extends ILabeledDataItem<Pair<Sentence, String[]>, TemporalResult>> testData,
 			JointModel<Sentence, String[], LogicalExpression, LogicalExpression> model) {
 		// The number of phrases in total.
 		int counter = 0;
 		// Correctly parsed and executed, both type and val.
 		int correct = 0;
 		// Parsed and executed, but either type or val are wrong.
 		int incorrect = 0;
 		// Number with the correct type.
 		int correctType = 0;
 		// Number with the correct value.
 		int correctVal = 0;
 		// Too many parses.
 		int tooManyParses = 0;
 		// Not parsed.
 		int notParsed = 0;
 		for (final ILabeledDataItem<Pair<Sentence, String[]>, TemporalResult> item : testData) {
 			counter++;
 			int c = test(item, model);
 			// 0 == correct type and val
 			// 1 == correct type, not val
 			// 2 == incorrect type, correct val
 			// -1 == incorrect type and val
 			if (c == -1)
 				incorrect++;
 			else if (c == 0)
 				correct++;
 			else if (c == 1)
 				correctType++;
 			else if (c == 2)
 				correctVal++;
 			else if (c == 3)
 				tooManyParses++;
 			else// if (c == 4)
 				notParsed++;
 		}
 		int[] tmpOutput = {counter, correct, correctVal, correctType, incorrect, tooManyParses, notParsed};
 		outputData.setCounters(tmpOutput);
 		out.println(outputData.toString());
 	}
 
 	private int test(ILabeledDataItem<Pair<Sentence, String[]>, TemporalResult> dataItem,
 			JointModel<Sentence, String[], LogicalExpression, LogicalExpression> model) {
 		int output;
 		LogicalExpression guessLabel = null;
 		String guessType = "", guessVal = "", correctLogicalForms = "";
 		LinkedHashSet<LexicalEntry<LogicalExpression>> lexicalEntries = null;
 		IHashVector averageMaxFeatureVector = null;
 		IHashVector theta = model.getTheta();
 		
 		// To extract all of the information from the mention.
 		// The key to know how to unpack this is in TemporalSentence.java.
 		Sentence phrase = dataItem.getSample().first();
 		String sentence = dataItem.getSample().second()[0];
 		String ref_time = dataItem.getSample().second()[1];
 		String depParse = dataItem.getSample().second()[2];
 		String goldType = dataItem.getLabel().type;
 		String goldVal = dataItem.getLabel().val;
 		Pair<String, String>  govVerbPOSWithMod = GovernerVerbPOSExtractor.getGovVerbTag(dataItem.getSample().second());
 		String govVerbPOS = govVerbPOSWithMod.second();
 		String mod = govVerbPOSWithMod.first();
 		// TODO clean this up! I don't need the governor verb here.
 	
 		IJointDataItemModel<LogicalExpression, LogicalExpression> jointDataItemModel = 
 				new JointDataItemModel<Sentence, String[], LogicalExpression, LogicalExpression>(model, dataItem);
 		final IJointOutput<LogicalExpression, TemporalResult> parserOutput = jointParser.parse(dataItem, jointDataItemModel);
 		final List<? extends IJointParse<LogicalExpression, TemporalResult>> bestParses = parserOutput.getBestJointParses();
 		// there is more than one 
 		if (bestParses.size() > 0) {
 			
 			IJointParse<LogicalExpression, TemporalResult> topParse = bestParses.get(0);
 			
 			averageMaxFeatureVector = topParse.getAverageMaxFeatureVector();
 			
 			
 			guessType = topParse.getResult().second().type;
 			guessVal = topParse.getResult().second().val;
 			guessLabel = topParse.getResult().second().e;
 			lexicalEntries = topParse.getResult().second().lexicalEntries;
 			
 			output = oneParse(goldType, goldVal, guessType, guessVal);
 			correctLogicalForms = anyCorrectLogic(goldType, goldVal, parserOutput.getAllJointParses(), theta);
 			
 			
 		
 		// TODO: Check every parse to make sure a correct one is included.
 		//} else if (bestModelParses.size() > 1) {
 		//	output = 3;
 		} else { // zero parses
 			output = 4;
 		}
 		printing(guessLabel, goldType, goldVal, guessType, guessVal, phrase.toString(), ref_time, output,  
 				depParse, govVerbPOS, sentence, mod, correctLogicalForms,lexicalEntries,averageMaxFeatureVector, theta);
 		return output;
 	}
 	
 	private String anyCorrectLogic(String goldType, String goldVal, final List<? extends IJointParse<LogicalExpression, TemporalResult>> bestParses, IHashVector theta){
 		String correctLogicalForms = "";
 		for (IJointParse<LogicalExpression, TemporalResult> l : bestParses){
 			String guessType = l.getResult().second().type;
 			String guessVal = l.getResult().second().val;
 			LogicalExpression guessLabel = l.getResult().second().e;
 			if (oneParse(goldType, goldVal, guessType, guessVal) == 0)
 				correctLogicalForms += "\n\t[" + guessLabel.toString() + "=>" + "(" + guessType + "," + guessVal + ")" + "]" + theta.printValues(l.getAverageMaxFeatureVector());
 		}
 		return correctLogicalForms;
 	}
 	
 	// Case we have a single parse
 	// return a Pair<label, int>, where the int indicates if the label is correct.
 	// 0 == correct type and val
 	// 1 == correct type, not val
 	// 2 == incorrect type, correct val
 	// -1 == incorrect type and val
 	private int oneParse(String goldType, String goldVal, String guessType, String guessVal){
 		if (goldType.equals(guessType) && goldVal.equals(guessVal))
 			return 0;
 		else if (goldType.equals(guessType) && !goldVal.equals(guessVal))
 			return 1;
 		else if (!goldType.equals(guessType) && goldVal.equals(guessVal))
 			return 2;
 		else 
 			return -1;
 	}
 
 	// args: output is the output of the system
 	private void printing(LogicalExpression label, String goldType, String goldVal,
 			String guessType, String guessVal, String phrase, String ref_time,
 			int correct, String depParse, String govVerbPOS, String sentence, String mod, 
 			String correctLogicalForms, LinkedHashSet<LexicalEntry<LogicalExpression>> lexicalEntries, IHashVector averageMaxFeatureVector, IHashVector theta) {
		if(!PRINTRESULTS)
			return;
		if (!ONLYPRINTONEPHRASE || (ONLYPRINTONEPHRASE && phrase.contains(PHRASE))) {
 			if (correct >= -1 && correct <= 2) {
 				if ((ONLYPRINTINCORRECT && (correct == -1)) 
 						|| !ONLYPRINTINCORRECT && !ONLYPRINTTOOMANYPARSES && !ONLYPRINTNOPARSES) {
 					out.println();
 					out.println("Phrase:            " + phrase);
 					out.println("Lexical Entries:   " + lexicalEntries);
 					out.println("Sentence:          " + sentence);
 					out.println("Logic:             " + label);
 					
 					out.println("Average max feats: " + theta.printValues(averageMaxFeatureVector));
 					out.println("ref_time:          " + ref_time);
 					out.println("Gold type:         " + goldType);
 					out.println("gold val:          " + goldVal);
 					out.println("Guess type:        " + guessType);
 					out.println("Guess val:         " + guessVal);
 					out.println("Correct type?      " + (correct == 0 || correct == 1));
 					out.println("Correct val?       " + (correct == 0 || correct == 2));
 					out.println("Correct logics:    " + correctLogicalForms);
 					out.println("Governor verb POS: " + govVerbPOS);
 					out.println("Mod:               " + mod.equals("MD"));
 				}
 			} else if ((correct == 4 && !ONLYPRINTINCORRECT && !ONLYPRINTTOOMANYPARSES) || correct == 4 && ONLYPRINTNOPARSES) {
 				out.println();
 				out.println("Phrase:            " + phrase);
 				out.println("Sentence:          " + sentence);
 				out.println("ref_time:          " + ref_time);
 				out.println("Gold type:         " + goldType);
 				out.println("gold val:          " + goldVal);
 				out.println("No parses! Will implement something"
 						+ " to throw out words and try again.");
 			}
 		}
 	}
 
 	public static TemporalTester build(TemporalObservationDataset test,	TemporalJointParser parser) {
 		return new TemporalTester(test, parser);
 	}
 }
