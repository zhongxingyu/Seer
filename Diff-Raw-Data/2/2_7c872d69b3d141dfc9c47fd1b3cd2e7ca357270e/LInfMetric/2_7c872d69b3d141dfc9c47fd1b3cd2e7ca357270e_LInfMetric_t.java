 package chordest.util.metric;
 
 public class LInfMetric implements IMetric {
 
 	@Override
 	public double distance(double[] from, double[] to) {
 		if (from.length != to.length) {
 			throw new RuntimeException("Vector have different lengths");
 		}
 		double max = 0;
 		for (int i = 0; i < from.length; i++) {
 			max = Math.max(max, Math.abs(from[i] - to[i]));
 		}
 		return max;
 	}
 
 	@Override
 	public double[] normalize(double[] array) {
 		double max = Double.MIN_VALUE;
		for (double value : array) { max = Math.max(max, Math.abs(value)); }
 		double[] result = new double[array.length];
 		for (int i = 0; i < array.length; i++) {
 			result[i] = array[i] / max;
 		}
 		return result;
 	}
 
 }
