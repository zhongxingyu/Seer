 package chordest.chord.comparison;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import chordest.io.lab.chordparser.ChordParser;
 import chordest.io.lab.chordparser.ParseException;
 import chordest.io.lab.chordparser.TokenMgrError;
 import chordest.model.Chord;
 import chordest.util.MapUtil;
 
 /**
  * To be used together with ChordListsComparison to accumulate statistics over
  * a number of tracks. Returns aggregated metrics and all errors sorted by
  * total time in descending orger.
  * @author Nikolay
  *
  */
 public class ComparisonAccumulator {
 
 	private double totalOverlap = 0;
 	private double totalWeightedOverlap = 0;
 	private double totalSegmentation = 0;
 	private int totalTracks = 0;
 	private double totalEffectiveLength = 0;
 	private final Map<String, Double> errors = new HashMap<String, Double>();
 
 	public void append(ChordListsComparison cmp) {
 		final double overlap = cmp.getOverlapMeasure();
 		final double effectiveSeconds = cmp.getEffectiveSeconds();
 		totalOverlap += overlap;
 		totalWeightedOverlap += (overlap * effectiveSeconds);
 		totalSegmentation += cmp.getSegmentation();
 		totalEffectiveLength += effectiveSeconds;
 		totalTracks++;
 		
 		Map<String, Double> errorsCurrent = cmp.getErrors();
 		for (Entry<String, Double> entry : errorsCurrent.entrySet()) {
 			String key = entry.getKey();
 			Double value = entry.getValue();
 			if (errors.containsKey(key)) {
 				value += errors.get(key);
 			}
 			errors.put(key, value);
 		}
 	}
 
 	public double getAOR() {
 		return totalOverlap / totalTracks;
 	}
 
 	public double getWAOR() {
 		return totalWeightedOverlap / totalEffectiveLength;
 	}
 
 	public double getAverageSegm() {
 		return totalSegmentation / totalTracks;
 	}
 
 	public List<Entry<String, Double>> getErrors() {
 		return MapUtil.sortMapByValue(errors, false);
 	}
 	
 	public Errors getAllErrors() {
 		return new Errors(errors);
 	}
 	
 	public static class Errors {
 		public double totalLengthInSeconds;
 		
 		public double majMin;
 		
 		public double minMaj;
 		
 		public double nChord;
 		
 		public double chordN;
 		
 		public double common2;
 		
 		public double rootFifth;
 		
 		public double others;
 		
 		public Errors(Map<String, Double> log) {
 			for (Entry<String, Double> entry : log.entrySet()) {
 				double time = entry.getValue();
 				totalLengthInSeconds += time;
 				String[] str = entry.getKey().split("-");
 				if (str.length == 2) {
 					String exp = str[0];
 					String act = str[1];
 					if (exp.contains(",") || act.contains(",")) {
 						others += time;
 					} else if ("N".equals(exp)) {
 						nChord += time;
 					} else if ("N".equals(act)) {
 						chordN += time;
 					} else {
 						try {
 							Chord expected = ChordParser.parseString(exp);
 							Chord actual = ChordParser.parseString(act);
 							if (expected.hasSameRootDifferentType(actual)) {
 								if (expected.isMajor() && actual.isMinor()) {
 									majMin += time;
 								} else if (expected.isMinor() && actual.isMajor()) {
 									minMaj += time;
 								}
							} else if (expected.getNumberOfCommonNotes(actual) == 2) {
								common2 += time;
							} else if (expected.getRoot().equals(actual.getNotes()[2]) ||
 									actual.getRoot().equals(expected.getNotes()[2])) {
								rootFifth += time;
 							} else {
 								others += time;
 							}
 						} catch (NumberFormatException | ParseException | TokenMgrError e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						
 					}
 				}
 			}
 		}
 	}
 }
