 package edu.brown.cs32.atian.crassus.indicators;
 
 import java.awt.Color;
 import java.util.*;
 
 import org.jfree.data.time.Second;
 import org.jfree.data.time.TimeSeries;
 
 import edu.brown.cs32.atian.crassus.backend.StockEventType;
 import edu.brown.cs32.atian.crassus.backend.StockTimeFrameData;
 import edu.brown.cs32.atian.crassus.gui.SeriesWrapper;
 import edu.brown.cs32.atian.crassus.gui.StockPlot;
 
 /**
  * @author atian
  * 
  * Bollinger Bands can be used to measure price action volatility.
  * 
  * Typical value of period is 20 days with bands 2 * standard deviations
  * above and below the moving average.
  * 
  * Bandwidth is the number of standard deviations above and below SMA; 
  * typical value is 2.
  *
  * Bands are calculated using close prices.
  */
 public class BollingerBands implements Indicator {
 
 	private List<IndicatorDatum> middleBand;		// oldest data first
 	private List<IndicatorDatum> lowerBand;			
 	private List<IndicatorDatum> upperBand;			
 	private int period;
 	private List<StockTimeFrameData> data;
 	private int bandWidth;
 	private boolean isActive;
 	private boolean isVisible;
 	private final double START_AMT = 10000;
 	private double percentMade;
 	private Date startTime;
 	
 	public BollingerBands(List<StockTimeFrameData> data, int period, int bandWidth,
 			Date startTime) throws IllegalArgumentException {
 		if (period == 0) throw new IllegalArgumentException("ERROR: " + period + " is not a valid period");
 		
 		this.data = data;
 		this.period = period;
 		this.bandWidth = bandWidth;
 		this.startTime = startTime;
 		middleBand = new ArrayList<IndicatorDatum>();
 		upperBand = new ArrayList<IndicatorDatum>();
 		lowerBand = new ArrayList<IndicatorDatum>();
 		refresh(data, startTime);
 	}
 	
 	/**
 	 * Creates new Bollinger Band stock event.
 	 * 
 	 * @param period	int period of SMA
 	 * @param k			int multiplier of standard deviation; determines
 	 * 					width of the bands.
 	 */
 	public BollingerBands(int period, int bandWidth) {
 		this.period = period;
 		this.lowerBand = new ArrayList<IndicatorDatum>();
 		this.upperBand = new ArrayList<IndicatorDatum>();
 		this.bandWidth = bandWidth;
 		updateBollingerBands();
 	}
 	
 	@Override
 	public boolean getVisible() {
 		return isVisible;
 	}
 	@Override
 	public void setVisible(boolean isVisible) {
 		this.isVisible = isVisible;
 	}
 	@Override
 	public boolean getActive() {
 		return isActive;
 	}
 	@Override
 	public void setActive(boolean isActive) {
 		this.isActive = isActive;
 	}
 	
 	@Override
 	public String getName() {
 		return "Bollinger Bands";
 	}
 	
 	/**
 	 * Calculates the standard deviation given start and end index of data inclusive.
 	 * 
 	 * Uses formula stdDev = sqrt(1/N * sum((x(i) - movingAvg))^2) for i = 1:N 
 	 * 
 	 * @param startIndex						int start index
 	 * @param endIndex							int end index
 	 * @param movingAvg							double moving average
 	 * @return									double standard deviation of given close values
 	 * @throws ArrayIndexOutOfBoundsException	if array index of data is out of bounds
 	 */
 	double calcStdDev(int startIndex, int endIndex, double movingAvg) 
 			throws ArrayIndexOutOfBoundsException {
 		
 		double sum = 0;
 		for (int i = startIndex; i <= endIndex; i++) {
 			double diff = data.get(i).getClose() - movingAvg;
 			sum +=  (diff * diff);
 		}
 		
 		return Math.sqrt(sum / (endIndex - startIndex + 1));
 	}
 	
 	/**
 	 * Calculates the SMA from start index to end index of data
 	 * inclusively.
 	 * 
 	 * @param startIndex	int start index 
 	 * @param endIndex		int end index
 	 * @return				double simple moving average of given close values
 	 * @throws ArrayIndexOutOfBoundsException	if array index of data is out of bounds
 	 */
 	double calcSMA(int startIndex, int endIndex) throws ArrayIndexOutOfBoundsException {
 		
 		double sum = 0;
 		for (int i = startIndex; i <= endIndex; i++) {
 			sum += data.get(i).getClose();
 		}
 		
 		return sum / (endIndex - startIndex + 1);
 	}
 
 	@Override
 	public void addToPlot(StockPlot stockPlot) {
 
 		SeriesWrapper upperSeries = stockPlot.getTimeSeries(upperBand, "Upper Band", startTime, Color.red);
 		SeriesWrapper middleSeries = stockPlot.getTimeSeries(middleBand, "Middle Band", startTime, Color.blue);
 		SeriesWrapper lowerSeries = stockPlot.getTimeSeries(lowerBand, "Lower Band", startTime, Color.GREEN);
 		
 		stockPlot.addSeries(upperSeries);
 		stockPlot.addSeries(middleSeries);
 		stockPlot.addSeries(lowerSeries);
 	}
 	
 	/**
 	 * Updates the Bollinger Bands.
 	 */
 	private void updateBollingerBands() {
 		
 		StockEventType currEvent = StockEventType.NONE;
 		double currAmt = START_AMT;
 		double epsilon = 0.1;
 		double numStocks = 0;
 		for (int i = 0; (i + period - 1) < data.size(); i++) {
 			double avg = calcSMA(i, i + period - 1);
 			double stdDev = calcStdDev(i, i + period - 1, avg);
 			
 			double upperBandValue = avg + (bandWidth * stdDev);
 			double lowerBandValue = avg - (bandWidth * stdDev);
 			double currClose = data.get(i).getClose();
 			
 			if ((currClose > upperBandValue - epsilon) || (currClose < upperBandValue + epsilon) || (i == data.size() - 1)) {
 				if (currEvent.equals(StockEventType.BUY)) {		// if we have already bought then sell now or sell at whatever price is 
 					currAmt += numStocks * currClose;			// last price
 					currEvent = StockEventType.SELL;
 				}
 			}
 			
 			if ((currClose > lowerBandValue - epsilon) || (currClose < lowerBandValue + epsilon)) {
 				numStocks += Math.floor(currAmt/currClose);		// buy whole number of stocks
 				currAmt = currAmt%currClose;					// keep amount left over
 				currEvent = StockEventType.BUY;
 			}
 
 			middleBand.add(new IndicatorDatum(data.get(i + period - 1).getTime(), data.get(i + period - 1).getTimeInNumber(), avg));
 			upperBand.add(new IndicatorDatum(data.get(i + period - 1).getTime(), data.get(i + period - 1).getTimeInNumber(), upperBandValue));
 			lowerBand.add(new IndicatorDatum(data.get(i + period - 1).getTime(), data.get(i + period - 1).getTimeInNumber(), lowerBandValue));
 		}
 		
 		percentMade = ((currAmt - START_AMT) / START_AMT);
 
 	}
 	
 	
 	List<IndicatorDatum> getUpperBand() {
 		return upperBand;
 	}
 	
 	List<IndicatorDatum> getMiddleBand() {
 		return middleBand;
 	}
 	
 	List<IndicatorDatum> getLowerBand() {
 		return lowerBand;
 	}
 
 	@Override
 	public void refresh(List<StockTimeFrameData> data, Date startTime) {
 		this.data = data;
		this.startTime = startTime;
 		updateBollingerBands();
 	}
 
 	@Override
 	public StockEventType isTriggered() {
 		// TODO Auto-generated method stub
 		/*		for (i = period - 1; i < data.size(); i++) {
 		
 		}*/
 		return null;
 	}
 
 	@Override
 	public double getTestResults() {
 		return percentMade;
 	}
 }
