 package edu.uw.cs.lil.tiny.tempeval;
 
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.List;
 
 import edu.uw.cs.lil.tiny.data.sentence.Sentence;
 import edu.uw.cs.lil.tiny.mr.lambda.LogicalExpression;
 import edu.uw.cs.lil.tiny.parser.joint.IJointOutput;
 import edu.uw.cs.lil.tiny.parser.joint.IJointParse;
 import edu.uw.cs.lil.tiny.parser.joint.model.*;
 import edu.uw.cs.lil.tiny.tempeval.structures.TemporalMention;
 import edu.uw.cs.lil.tiny.tempeval.structures.TemporalMentionDataset;
 import edu.uw.cs.lil.tiny.tempeval.structures.TemporalResult;
 import edu.uw.cs.lil.tiny.tempeval.util.Debug;
 import edu.uw.cs.lil.tiny.tempeval.util.Debug.Type;
 import edu.uw.cs.lil.tiny.tempeval.util.TemporalStatistics;
 import edu.uw.cs.lil.tiny.tempeval.util.TemporalUtil;
 
 public class TemporalAttributeTester {
 	private static final String DEBUG_PHRASE = "asdfsada year earlier";
 	private final TemporalMentionDataset testData;
 	private final TemporalJointParser jointParser;
 	private TemporalStatistics stats;
 	private JointModel<Sentence, TemporalMention, LogicalExpression, LogicalExpression> model;
 
 	public TemporalAttributeTester(TemporalMentionDataset testData, TemporalJointParser jointParser, JointModel<Sentence, TemporalMention, LogicalExpression, LogicalExpression> model, TemporalStatistics stats) {
 		this.testData = testData;
 		this.jointParser = jointParser;
 		this.model = model;
 		this.stats = stats;
 	}
 
 	public void test() {
 		Debug.printf(Type.PROGRESS, "Predicting attributes for %d observations\n", testData.size());
 		for (TemporalMention mention : testData)
 			testMention(mention);
 	}
 
 	private void testMention(TemporalMention mention) {
 		IJointDataItemModel<LogicalExpression, LogicalExpression> jointDataItemModel = new JointDataItemModel<Sentence, TemporalMention, LogicalExpression, LogicalExpression>(model, mention);
 		final IJointOutput<LogicalExpression, TemporalResult> parserOutput = jointParser.parse(mention, jointDataItemModel);
 		final List<? extends IJointParse<LogicalExpression, TemporalResult>> bestParses = parserOutput.getBestJointParses();
 		TemporalResult bestResult = bestParses.isEmpty() ? null : bestParses.get(0).getResult().second();
 		evaluateParses(mention, bestResult, parserOutput.getAllJointParses());
 	}
 
 	private void evaluateParses(TemporalMention mention, TemporalResult result, List<? extends IJointParse<LogicalExpression, TemporalResult>> allParses){
 		List<TemporalResult> correctParses = getCorrectParses(mention, allParses);
 		List<TemporalResult> incorrectParses = getIncorrectParses(mention, allParses);
 
 		stats.incrementTotalAttributes();
 		Debug.print(Type.ATTRIBUTE, formatResult(mention, result, correctParses, incorrectParses));
 
 		if(result == null) {
 			stats.incrementIncorrectClass("No parses");
 			Debug.print(Type.INCORRECT_ATTRIBUTE, formatResult(mention, result, correctParses, incorrectParses));
 		}
 		else {
 			String goldValue = mention.getValue();
 			String goldType = mention.getType();
 			String guessType = result.type;
 			String guessValue = result.value;
 
 			if (goldValue.equals(guessValue)) {
 				stats.incrementCorrectAttributes();
 				if (goldType.equals(guessType))
 					stats.incrementCorrectClass("Correct value and type");
 				if (guessValue.matches("[0-9][0-9][0-9][0-9]-W[0-9][0-9]"))
 					stats.incrementCorrectClass("Correct week reference");
 			}
 			else {
 				if (mention.getPhrase().toString().contains(DEBUG_PHRASE))
 					Debug.print(Type.DEBUG_ATTRIBUTE, formatResult(mention, result, correctParses, incorrectParses));
 
 				/*
 				 * Error analysis goes here.
 				 */
 				boolean hasFoundErrorClass = false;
 
 				Debug.print(Type.INCORRECT_ATTRIBUTE, formatResult(mention, result, correctParses, incorrectParses));
 				if (correctParses.size() > 0) {
 					stats.incrementIncorrectClass("Incorrect parse selection");
 					Debug.print(Type.PARSE_SELECTION, formatResult(mention, result, correctParses, incorrectParses));
 					hasFoundErrorClass = true;
 				}
 				else if(TemporalUtil.getCalendar(goldValue) != null && TemporalUtil.getCalendar(guessValue) != null) {
 					Calendar goldCalendar = TemporalUtil.getCalendar(goldValue);
 					Calendar guessCalendar = TemporalUtil.getCalendar(guessValue);
 					if ((goldCalendar.get(Calendar.DAY_OF_MONTH) == guessCalendar.get(Calendar.DAY_OF_MONTH))&&
 							(goldCalendar.get(Calendar.MONTH) == guessCalendar.get(Calendar.MONTH))) {
 						if (Math.abs(goldCalendar.get(Calendar.YEAR) - guessCalendar.get(Calendar.YEAR))== 1)
 							stats.incrementIncorrectClass("Off by one year");
 						else
 							stats.incrementIncorrectClass("Off by more than one year");
 						hasFoundErrorClass = true;
 					}
 					else if ((goldCalendar.get(Calendar.YEAR) == guessCalendar.get(Calendar.YEAR))&&
 							(goldCalendar.get(Calendar.MONTH) == guessCalendar.get(Calendar.MONTH))) {
 						if (Math.abs(goldCalendar.get(Calendar.DAY_OF_YEAR) - guessCalendar.get(Calendar.DAY_OF_YEAR))== 7)
 							stats.incrementIncorrectClass("Off by one week");
 						else
 							stats.incrementIncorrectClass("Off by more than one week");
 						hasFoundErrorClass = true;
 					}
 				}
				else if(guessValue.contains(goldValue)) {
					stats.incrementIncorrectClass("Not specific enough");
 					hasFoundErrorClass = true;
 				}
 				else if(goldValue.contains(guessValue)) {
 					stats.incrementIncorrectClass("Too specific");
 					hasFoundErrorClass = true;
 				}
 				else if(goldValue.replaceAll("[0-9]", "X").equals(guessValue.replaceAll("[0-9]", "X"))) {
 					stats.incrementIncorrectClass("Incorrect certainty");
 					hasFoundErrorClass = true;
 				}
				else if(guessValue.matches("[0-9][0-9][0-9][0-9]-W[0-9][0-9]")) {
					stats.incrementIncorrectClass("Incorrect week reference");
					hasFoundErrorClass = true;
				}
 				if (!hasFoundErrorClass) {
 					stats.incrementIncorrectClass("Unknown reason");
 					Debug.print(Type.UNKNOWN_INCORRECT, formatResult(mention, result, correctParses, incorrectParses));
 				}
 			}
 		}
 	}
 
 	private List<TemporalResult> getCorrectParses(TemporalMention mention, List<? extends IJointParse<LogicalExpression, TemporalResult>> allParses){
 		List<TemporalResult> correctParses = new LinkedList<TemporalResult>();
 		for (IJointParse<LogicalExpression, TemporalResult> parse : allParses){
 			if (parse.getResult().second().value.equals(mention.getValue()))
 				correctParses.add(parse.getResult().second());		}
 		return correctParses;
 	}
 
 	private List<TemporalResult> getIncorrectParses(TemporalMention mention, List<? extends IJointParse<LogicalExpression, TemporalResult>> allParses){
 		List<TemporalResult> incorrectParses = new LinkedList<TemporalResult>();
 		for (IJointParse<LogicalExpression, TemporalResult> parse : allParses){
 			if (!parse.getResult().second().value.equals(mention.getValue()))
 				incorrectParses.add(parse.getResult().second());		}
 		return incorrectParses;
 	}
 
 	private String formatResult(TemporalMention mention, TemporalResult result, List<TemporalResult> correctParses, List<TemporalResult> incorrectParses) {
 		String s = "";
 		s += "Phrase:                  " + mention.getPhrase().toString() + "\n";
 		s += "Sentence:                " + mention.getSentence().toString() + "\n";
 		s += "Reference time:          " + mention.getSentence().getReferenceTime() + "\n";
 		s += "Doc ID:                  " + mention.getSentence().getDocID() + "\n";
 		s += "Gold value:              " + mention.getValue() + "\n";
 		s += "Gold type:               " + mention.getType() + "\n";
 
 		if (result != null) {
 			s += "Guess value:             " + result.value + "\n";
 			s += "Guess type:              " + result.type + "\n";
 			s += "Chosen logical form:     " + result.expression + "\n";
 			s += "Correct logical forms:   " + correctParses + "\n";
 			s += "Incorrect logical forms: " + incorrectParses + "\n";
 			s += "Lexical entries:         " + result.lexicalEntries + "\n";
 		}
 		else
 			s += "No parses!";
 
 		s += "\n";
 
 		return s;
 	}
 }
