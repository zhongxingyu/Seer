 /*
  * 
  */
 package edu.brown.cs32.atian.crassus.gui;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.util.Date;
 import java.util.List;
 
 import org.jfree.chart.JFreeChart;
 import org.jfree.data.time.TimeSeries;
 
 import edu.brown.cs32.atian.crassus.indicators.IndicatorDatum;
 
 /**
  * 
  * Container for two {@link JFreeChart} objects, one of which is the main chart (the prices of the stock
  * at different times) and the other of which is the relative strength chart, which contains relative strengths
  * 
  * @author Matthew
  *
  */
 public interface StockPlot {
 
 	/*
 	 * This function allows retrieval of the primary chart, so that it can be changed or drawn to the screen
 	 * @return the primary chart, which contains the prices of the stock over time
 	 */
 	//public JFreeChart getPrimaryChart();
 	
 	/*
 	 * This function allows retrieval of the relative strength chart, so that it can be changed or drawn to the screen.
 	 * IMPORTANT, in many cases, 
 	 * @return the relative strength chart, which contains the relative strength of the stock over time
 	 */
 	//public JFreeChart getRelativeStrengthChart();
 	
 	public void setTimeFrame(TimeFrame timeFrame);
 	
 	public void addSeries(SeriesWrapper series);
 
 	public void addRsSeries(SeriesWrapper series);
 	
 	/**
 	 * Turn RS on or off
 	 */
 	public void setRS(boolean isRsOn) throws CantTurnRsOnAfterChartsRetreivedException;
 	
 	/**
 	 * 
 	 * @return whether the relative strength chart is currently separate from the primary chart.
 	 */
 	public boolean isRsOn();
 	
 	/**
 	 * 
 	 * @param width - the desired width of the output
 	 * @param height - the desired hegith of the output
 	 * @return - a buffered image showing the chart. 
 	 */
 	public BufferedImage getPrimaryBufferedImage(int width, int height);
 	
 	public BufferedImage getRsBufferedImage(int width, int height);
 	
 	public SeriesWrapper getTimeSeries(List<IndicatorDatum> indicatorPoints, String seriesName, Date startTime, Date endTime, Color seriesColor);
 
	public void addRsSeries(SeriesWrapper series);
 	
 }
