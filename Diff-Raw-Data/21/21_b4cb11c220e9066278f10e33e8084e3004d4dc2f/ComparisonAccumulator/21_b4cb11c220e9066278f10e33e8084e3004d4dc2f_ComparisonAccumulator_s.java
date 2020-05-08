 package chordest.chord.comparison;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.lang3.ArrayUtils;
 import org.apache.commons.lang3.tuple.Pair;
 import org.slf4j.Logger;
 
 import chordest.model.Chord;
 
 /**
  * To be used together with ChordListsComparison to accumulate statistics over
  * a number of tracks. Returns aggregated metrics and all errors sorted by
  * total time in descending order.
  * @author Nikolay
  *
  */
 public class ComparisonAccumulator {
 
 	private double totalOverlap = 0;
 	private double totalWeightedOverlap = 0;
 	private double totalSegmentation = 0;
 	private int totalTracks = 0;
 	private double totalEffectiveLength = 0;
 	private double totalLength = 0;
 	private double totalErrorLength = 0;
 	
 	private final Map<Pair<Chord, Chord>, Double> errors;
 	private final IEvaluationMetric metric;
 
 	public ComparisonAccumulator(IEvaluationMetric metric) {
 		this.metric = metric;
 		this.errors = new HashMap<Pair<Chord, Chord>, Double>();
 	}
 
 	public void append(ChordListsComparison cmp) {
 		if (! cmp.getMetric().equals(metric)) {
 			throw new IllegalArgumentException(String.format(
 					"Comparison evaluation metric %s differs from the accumulator's one: %s", 
 					cmp.getMetric(),
 					metric));
 		}
 		final double overlap = cmp.getOverlapMeasure();
 		final double effectiveSeconds = cmp.getEffectiveSeconds();
 		totalOverlap += overlap;
 		totalWeightedOverlap += (overlap * effectiveSeconds);
 		totalSegmentation += cmp.getSegmentation();
 		totalEffectiveLength += effectiveSeconds;
 		totalLength += cmp.getTotalSeconds();
 		totalTracks++;
 		
 		Map<Pair<Chord, Chord>, Double> errorsCurrent = cmp.getErrors();
 		for (Entry<Pair<Chord, Chord>, Double> entry : errorsCurrent.entrySet()) {
 			Pair<Chord, Chord> key = entry.getKey();
 			Double value = entry.getValue();
 			totalErrorLength += value;
 			if (errors.containsKey(key)) {
 				value += errors.get(key);
 			}
 			errors.put(key, value);
 		}
 	}
 
 	public IEvaluationMetric getMetric() {
 		return metric;
 	}
 
 	private double getAOR() {
 		return totalOverlap / totalTracks;
 	}
 
 	private double getWAOR() {
 		return totalWeightedOverlap / totalEffectiveLength;
 	}
 
 	private double getAverageSegm() {
 		return totalSegmentation / totalTracks;
 	}
 
 	private Errors getAllErrors() {
 		return new Errors(errors, metric);
 	}
 	
 	public void printStatistics(Logger logger) {
 		logger.info( metric.toString());
 		logger.info(String.format("AOR: %.4f", getAOR()));
 		logger.info(String.format("WAOR: %.4f", getWAOR()));
 		logger.info(String.format("Average segmentation: %.4f", getAverageSegm()));
 		logger.info(String.format("Total length of tracks: %.2f s", totalLength));
 		logger.info(String.format("Effective length: %.2f s", totalEffectiveLength));
 		logger.info(String.format("Total erroneous segments length: %.2f s", totalErrorLength));
 	}
 	
 	public void printErrorStatistics(Logger logger) {
 		Errors err = getAllErrors();
 		String[] types = err.types;
 		for (int i = 0; i < types.length; i++) {
 			for (int j = 0; j < types.length; j++) {
 				printTypes(logger, types[i], types[j], err.aggregateTimes[i][j], ArrayUtils.subarray(err.times[i], j*12, (j+1)*12));
				printTypes(logger, types[i] + "_red", types[j], err.aggregateTimesReduced[i][j], ArrayUtils.subarray(err.timesReduced[i], j*12, (j+1)*12));
 			}
 		}
 		printTypes(logger, "chord", "N", err.chordN, new double[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 });
 		printTypes(logger, "N", "chord", err.nChord, new double[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 });
 		printTypes(logger, "others", err.others, new double[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 });
 		
 		double[] values = new double[12];
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < types.length; i++) {
 			values[i] = err.timesPerType[i];
 			sb.append(types[i]);
 			sb.append(',');
 		}
 		sb.append("N,others");
 		values[types.length] = err.timesPerType[types.length];
 		values[types.length + 1] = err.timesPerType[types.length + 1];
 		printTypes(logger, sb.toString(), totalErrorLength, values);
 	}
 	
 	private void printTypes(Logger logger, String expectedType, String actualType, double time, double[] values) {
 		printTypes(logger, expectedType + " - " + actualType, time, values);
 	}
 	
 	private void printTypes(Logger logger, String message, double time, double[] values) {
 		double sum = 0;
 		for (int i = 0; i < values.length; i++) {
 			sum += values[i];
 		}
 		if (sum > 0) {
 			StringBuilder sb = new StringBuilder();
 			sb.append(getMetric());
 			sb.append(";");
 			sb.append(message);
 			sb.append(";");
 			sb.append(time);
 			sb.append(";");
 			for (int i = 0; i < values.length - 1; i++) {
				sb.append(values[i] > 0 ? values[i] : 0.1);
 				sb.append(";");
 			}
 			sb.append(values[values.length - 1]);
 			logger.info(sb.toString());
 		}
 	}
 	
 	public static class Errors {
 		private final String[] types;
 		
 		// when the expected chord is a known chord
 		public final double[][] times;
 		public final double[][] aggregateTimes;
		
		// when the expected chord can be mapped to some known chord
		public final double[][] timesReduced;
		public final double[][] aggregateTimesReduced;
 
 		public final double[] timesPerType;
 
 		public double nChord;
 		
 		public double chordN;
 
 		public double others;
 		
 		public Errors(Map<Pair<Chord, Chord>, Double> log, IEvaluationMetric metric) {
 			if (metric.getOutputTypes() != null && metric.getOutputTypes().length > 0) {
 				types = metric.getOutputTypes();
 			} else {
 				types = new String[] { Chord.MAJ, Chord.MIN };
 			}
 			int totalChords = types.length * 12; // 12 entries per each type
 			times = new double[types.length][totalChords];
 			aggregateTimes = new double[types.length][types.length];
			timesReduced = new double[types.length][totalChords];
			aggregateTimesReduced = new double[types.length][types.length];
 			timesPerType = new double[types.length + 2]; // plus N and others
 			
 			for (Entry<Pair<Chord, Chord>, Double> entry : log.entrySet()) {
 				double time = entry.getValue();
 				Chord expected = entry.getKey().getLeft();
 				Chord actual = entry.getKey().getRight();
 				int index = getIndex(expected, actual);
 				boolean processed = false;
 				if (expected.isEmpty()) {
 					nChord += time;
 					timesPerType[timesPerType.length - 2] += time;
 					processed = true;
 				} else if (actual.isEmpty()) {
 					chordN += time;
 					timesPerType[timesPerType.length - 2] += time;
 					processed = true;
 				} else {
 					for (int i = 0; i < types.length && ! processed; i++) {
						if (expected.isOfType(types[i])) {
 							times[i][index] += time;
 							aggregateTimes[i][index / 12] += time;
 							timesPerType[i] += time;
 							processed = true;
						} else if (metric.map(expected).isOfType(types[i])) {
							timesReduced[i][index] += time;
							aggregateTimesReduced[i][index / 12] += time;
							timesPerType[i] += time;
							processed = true;
 						}
 					}
 				}
 				if (! processed) {
 					others += time;
 					timesPerType[timesPerType.length - 1] += time;
 				}
 			}
 		}
 
 		private boolean isMirexChord(Chord chord) {
 			for (int i = 0; i < types.length; i++) {
 				if (chord.isOfType(types[i])) {
 					return true;
 				}
 			}
 			return false;
 		}
 		
 		private int getIndex(Chord expected, Chord actual) {
 			if (expected == null || expected.isEmpty()) {
 				return -1;
 			}
 			if (isMirexChord(actual)) {
 				int diff = (expected.getRoot().ordinal() - actual.getRoot().ordinal() + 12) % 12;
 				for (int i = 0; i < types.length; i++) {
 					if (actual.isOfType(types[i])) {
 						return diff + 12 * i;
 					}
 				}
 			}
 			return -1;
 		}
 	}
 }
