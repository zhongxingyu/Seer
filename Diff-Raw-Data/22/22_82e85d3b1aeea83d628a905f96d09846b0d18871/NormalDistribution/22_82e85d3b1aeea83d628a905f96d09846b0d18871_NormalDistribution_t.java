 
 public class NormalDistribution
 {
 	private final static int PRECISION = 100;
 	private double average;
 	private double deviation;
 
 	public NormalDistribution(double average, double deviation)
 	{
 		this.average = average;
 		this.deviation = deviation;
 	}
 
 	public double getExpectedValues(double from, double to, int numberOfTries)
 	{
 		double step = (to - from) / PRECISION;
 		double currFrom = from;
 		double currTo = from + step;
 		double integral = 0;
 		for (int i = 0; i < PRECISION; ++i) {
			double integralFrom = - (currFrom - average) * (currFrom - average) / (2 * deviation * deviation);
			double integralTo = - (currTo - average) * (currTo - average) / (2 * deviation * deviation);
			double bottom = Math.pow(Math.E, integralFrom) / (Math.sqrt(2 * Math.PI) * deviation);
			double top = Math.pow(Math.E, integralTo) / (Math.sqrt(2 * Math.PI) * deviation);
			integral += (top + bottom) * step / 2.0;
 		}
		double prob = integral;
 		return prob * numberOfTries;
 	}
 }
