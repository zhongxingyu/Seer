 package edu.uw.cs.lil.tiny.tempeval;
 
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.PriorityQueue;
 import java.util.Set;
 
 import edu.uw.cs.lil.tiny.data.sentence.Sentence;
 import edu.uw.cs.lil.tiny.mr.lambda.LogicalExpression;
 import edu.uw.cs.lil.tiny.parser.IParseResult;
 import edu.uw.cs.lil.tiny.parser.IParserOutput;
 import edu.uw.cs.lil.tiny.parser.joint.model.JointDataItemModel;
 import edu.uw.cs.lil.tiny.parser.joint.model.JointModel;
 import edu.uw.cs.lil.tiny.tempeval.structures.TemporalDataset;
 import edu.uw.cs.lil.tiny.tempeval.structures.TemporalObservationDataset;
 import edu.uw.cs.lil.tiny.tempeval.structures.TemporalSentence;
 import edu.uw.cs.lil.tiny.tempeval.structures.TemporalMention;
 import edu.uw.cs.lil.tiny.tempeval.util.Debug;
 import edu.uw.cs.lil.tiny.tempeval.util.Debug.Type;
 import edu.uw.cs.lil.tiny.tempeval.util.TemporalStatistics;
 import edu.uw.cs.utils.composites.Pair;
 
 public class TemporalDetectionTester {
 	final private int MAX_MENTION_LENGTH = 5;
 	private TemporalDataset testData;
 	private TemporalJointParser jointParser;
 	private TemporalObservationDataset correctObservations;
 	private JointModel<Sentence, String[], LogicalExpression, LogicalExpression> model;
 	public TemporalDetectionTester(TemporalDataset testData, TemporalJointParser jointParser, JointModel<Sentence, String[], LogicalExpression, LogicalExpression> model) {
 		this.testData = testData;
 		this.jointParser = jointParser;
 		this.model = model;
 		correctObservations = new TemporalObservationDataset();
 	}
 
 	public TemporalStatistics test(TemporalStatistics stats) {
 		int sentenceCount = 0;
 		for (TemporalSentence ts : testData) {
 			sentenceCount++;
			if (sentenceCount % 100 == 0)
 				Debug.printf(Type.PROGRESS, "Detecting mentions from %d/%d sentences...\n", sentenceCount, testData.size());
 			stats.addGold(ts.getMentions().size());
 			List<TemporalMention> predictedMentions = getPredictedMentions(ts);
 			stats.addPredicted(predictedMentions.size());
 			List<TemporalMention> correctMentions = getCorrectMentions(ts.getMentions(), predictedMentions);
 			stats.addCorrect(correctMentions.size());
 			if (correctMentions.size() < ts.getMentions().size()) {
 				Debug.printf(Type.DETECTION, "False negative from '%s':\n", ts.prettyString());
 				for(TemporalMention fn: getFalseNegatives(correctMentions, ts.getMentions()))
 					Debug.printf(Type.DETECTION, "[%s]", fn);
 				Debug.print(Type.DETECTION, "\n\n");
 			}
 			if (correctMentions.size() < predictedMentions.size()) {
 				Debug.printf(Type.DETECTION, "False positive from '%s':\n", ts.prettyString());
 				for(TemporalMention fp : getFalsePositives(correctMentions, predictedMentions))
 					Debug.printf(Type.DETECTION, "[%s]", fp);
 				Debug.print(Type.DETECTION, "\n\n");
 			}
 			if (!correctMentions.isEmpty())
 				correctObservations.addObservations(ts.getObservations(correctMentions));
 		}
 		return stats;
 	}
 
 	private List<TemporalMention> getCorrectMentions(List<TemporalMention> goldMentions, List<TemporalMention> predictedMentions) {
 		List<TemporalMention> correctMentions = new LinkedList<TemporalMention>();
 		for (TemporalMention gm : goldMentions) {
 			for (TemporalMention pm : predictedMentions) {
 				if (gm.matches(pm)) {
 					// Fill in gold information from gold mention for evaluation
 					pm.mergeWith(gm);
 					correctMentions.add(pm);
 					break;
 				}
 			}
 		}
 		return correctMentions;
 	}
 
 	private List<TemporalMention> getFalseNegatives(List<TemporalMention> correctMentions, List<TemporalMention> goldMentions) {
 		List<TemporalMention> falseNegatives = new LinkedList<TemporalMention>(goldMentions);
 		for (TemporalMention gm : goldMentions) {
 			for(TemporalMention cm : correctMentions) {
 				if (gm.matches(cm))
 					falseNegatives.remove(gm);
 			}
 		}
 		return falseNegatives;
 	}
 
 	private List<TemporalMention> getFalsePositives(List<TemporalMention> correctMentions, List<TemporalMention> predictedMentions) {
 		List<TemporalMention> falsePositives = new LinkedList<TemporalMention>(predictedMentions);
 		for (TemporalMention pm : predictedMentions) {
 			for(TemporalMention cm : correctMentions) {
 				if (pm.matches(cm))
 					falsePositives.remove(pm);
 			}
 		}
 		return falsePositives;
 	}
 
 	private List<TemporalMention> getPredictedMentions(TemporalSentence ts) {
 		PriorityQueue<Pair<TemporalMention, Double>> allPossibleMentions = new PriorityQueue<Pair<TemporalMention, Double>>(1,
 				new Comparator<Pair<TemporalMention, Double>>() {
 			public int compare(Pair<TemporalMention, Double> p1, Pair<TemporalMention, Double> p2) {
 				// Higher score goes first
 				return p2.second().compareTo(p1.second());
 			}
 		});
 		for (int i = 0 ; i < ts.getTokens().size(); i++) {	
 			for (int span = Math.min(MAX_MENTION_LENGTH, ts.getTokens().size() - i)  ; span > 0 ; span--) {
 				Sentence mentionCandidate = new Sentence(ts.getTokens().subList(i, i + span));
 				JointDataItemModel<Sentence, String[], LogicalExpression, LogicalExpression> dataItemModel = new JointDataItemModel<Sentence, String[], LogicalExpression, LogicalExpression>(model, ts.getPossibleObservation(i, i + span));		
 				IParserOutput<LogicalExpression> parserOutput = jointParser.parse(mentionCandidate, dataItemModel);
 				List<IParseResult<LogicalExpression>> bestParses = parserOutput.getBestParses();
 				if (bestParses.size() > 0) 
 					allPossibleMentions.add(Pair.of(new TemporalMention(i, i + span, mentionCandidate.toString()), bestParses.get(0).getScore()));
 			}
 		}	
 		List<TemporalMention> predictedMentions = new LinkedList<TemporalMention>();
 		Set<TemporalMention> conflictingMentions = new HashSet<TemporalMention>();
 		for(Pair<TemporalMention, Double> p: allPossibleMentions) {
 			//Debug.printf(Type.DEBUG, "Mention: %s, Score: %f\n", p.first(), p.second());
 			if(!conflictingMentions.contains(p.first())) {
 				predictedMentions.add(p.first());
 				for(Pair<TemporalMention, Double> pConflict: allPossibleMentions) {
 					if (pConflict.first().overlapsWith(p.first()))
 						conflictingMentions.add(pConflict.first());
 				}
 			}
 		}
 		return predictedMentions;
 	}
 
 	public TemporalObservationDataset getCorrectObservations() {
 		return correctObservations;
 	}
 }
