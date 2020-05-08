 package chordest.util.metric;
 
 
 public class L1Metric implements IMetric {
 
 	@Override
 	public double distance(double[] from, double[] to) {
 		if (from.length != to.length) {
 			throw new RuntimeException("Vector have different lengths");
 		}
 		double sum = 0;
 		for (int i = 0; i < from.length; i++) {
 			sum += Math.abs(from[i] - to[i]);
 		}
 		return sum;
 	}
 
 	@Override
 	public double[] normalize(double[] array) {
 		double sum = Double.MIN_VALUE;
		for (double value : array) { sum += value; }
 		double[] result = new double[array.length];
 		for (int i = 0; i < array.length; i++) {
 			result[i] = array[i] / sum;
 		}
 		return result;
 	}
 
 }
