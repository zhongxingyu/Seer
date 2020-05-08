 /**
  *
  */
 package stockcentral;
 
 /**
  * @author Jack's Dell
  *
  */
 public class MacD {
 
 	private double[] m_macd;
 	public double[] getMacD() { return m_macd; }
 
 	private double[] m_signal;
 	public double[] getSignal() { return m_signal; }
 
 	private double[] m_histogram;
 	public double[] getHistogram() { return m_histogram; }
 
 	private double m_averageHistogram;
 	public double getAverageHistogram() { return m_averageHistogram; }
 
 	public MacD(int smallEMA, int largeEMA, int signalPeriod, float[] closes) {
 
 		calculateMacD(smallEMA, largeEMA, signalPeriod, closes);
 
 	}	// ctor
 
 	private void calculateMacD(int smallEMAPeriod, int largeEMAPeriod, int signalPeriod, float[] closes) {
 
 		double[] smallEMA, largeEMA;
 
 		smallEMA = StockCentral.calculateEMA(closes, smallEMAPeriod);
 		largeEMA = StockCentral.calculateEMA(closes, largeEMAPeriod);
 
 			// Now, let's calculate the difference between the small EMA and the big one.  That's our MacD
 
 		double[] macdWithoutZeros = new double[closes.length - largeEMAPeriod];
 		double[] zeros = new double[largeEMAPeriod];
 
 			// The first part of the array is going to just be zero.
 		java.util.Arrays.fill(zeros, 0, zeros.length - 1, 0.0);
 
 			// Now we calculate the MacD in a separate array without the zeros, which we will then merge.
 		for (int countMACDs = 0; countMACDs < macdWithoutZeros.length; countMACDs++)
 			macdWithoutZeros[countMACDs] = smallEMA[countMACDs + largeEMAPeriod] - largeEMA[countMACDs + largeEMAPeriod];
 
 		m_macd = StockCentral.mergeDoubleArrays(zeros, macdWithoutZeros);
 
 			// Next, we'll calculate an EMA of the MacD.  We're going to calculate the EMA of the macdWithoutZeros array,
 			// and then we're going to merge that with the zeros array already created, and that's our signal line!
 		double[] signalWithoutZeros = StockCentral.calculateEMA(m_macd, signalPeriod);
 
 		m_signal = StockCentral.mergeDoubleArrays(zeros, signalWithoutZeros);
 
 			// Next, let's calculate the histogram.
		m_histogram = new double[closes.length];

 		for (int countHistograms = 0; countHistograms < closes.length; countHistograms++)
 			m_histogram[countHistograms] = m_macd[countHistograms] - m_signal[countHistograms];
 
 			// Finally, let's calculate the average of the absolute values of the histograms.
 		double totalOfHistograms = 0;
 		for (int countTotals = 0; countTotals < closes.length; countTotals++)
 			totalOfHistograms += Math.abs(m_histogram[countTotals]);
 
 		m_averageHistogram = totalOfHistograms / (closes.length - largeEMAPeriod);
 
 	}	// calculateMacD
 
 }	// class MacD
 
 
