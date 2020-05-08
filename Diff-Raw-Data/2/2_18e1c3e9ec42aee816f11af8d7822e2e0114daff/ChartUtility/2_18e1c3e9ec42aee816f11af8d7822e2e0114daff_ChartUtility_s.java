 package stockviewer.util;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import javax.swing.JPanel;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.DateAxis;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.labels.StandardXYToolTipGenerator;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
 import org.jfree.data.time.Day;
 import org.jfree.data.time.TimeSeries;
 import org.jfree.data.time.TimeSeriesCollection;
 import org.jfree.data.xy.XYDataset;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import stockviewer.stock.StockData;
 import stockviewer.stock.StockInfo;
 import stockviewer.stock.StockPriceType;
 
 public class ChartUtility {
 
 	private static final Logger LOG = LoggerFactory
 			.getLogger(ChartUtility.class);
 
 	private Calendar cal;
 	private final DateFormat df = new SimpleDateFormat("MM-dd-yyyy");
 	private static final String RANGE_AXIS_SUFFIX = " ($)";
 	private static final String DOMAIN_AXIS_TITLE = "Date";
 
 	public ChartUtility() {
 		cal = Calendar.getInstance();
 	}
 
 	public JPanel createChart(Date from, Date to, StockInfo stock1,
 			StockInfo stock2, Color color1, Color color2,
 			StockPriceType priceType) {
 
 		LOG.info("Generating chart for tickers:" + stock1.getTickerSymbol()
 				+ " & " + stock2.getTickerSymbol());
 
 		String chartTitle = df.format(from) + " to " + df.format(to) + " "
 				+ priceType;
 		XYDataset dataset1 = createDataset(stock1, priceType);
 		XYDataset dataset2 = createDataset(stock2, priceType);
 
 		JFreeChart chart = ChartFactory.createTimeSeriesChart(chartTitle,
 				DOMAIN_AXIS_TITLE,
 				stock1.getTickerSymbol() + RANGE_AXIS_SUFFIX, dataset1, true,
 				true, false);
 
 		XYPlot plot = chart.getXYPlot();
 		NumberAxis axis2 = new NumberAxis(stock2.getTickerSymbol()
 				+ RANGE_AXIS_SUFFIX);
 		Font tickLabelFont = axis2.getTickLabelFont().deriveFont(11.0F);
 		Font labelFont = axis2.getLabelFont().deriveFont(15.0F)
 				.deriveFont(Font.BOLD);
 		axis2.setTickLabelFont(tickLabelFont);
 		axis2.setLabelFont(labelFont);
 		axis2.setAutoRangeIncludesZero(false);
 		plot.setRangeAxis(1, axis2);
 		plot.setDataset(1, dataset2);
 		plot.mapDatasetToRangeAxis(1, 1);
 
 		StandardXYItemRenderer renderer = new StandardXYItemRenderer();
 		renderer.setSeriesPaint(0, color1);
 		renderer.setBaseShapesVisible(true);
 		renderer.setBaseToolTipGenerator(StandardXYToolTipGenerator
 				.getTimeSeriesInstance());
 		plot.setRenderer(renderer);
 
 		StandardXYItemRenderer renderer2 = new StandardXYItemRenderer();
 		renderer2.setSeriesPaint(0, color2);
 		renderer2.setBaseShapesVisible(true);
 		renderer2.setBaseToolTipGenerator(StandardXYToolTipGenerator
 				.getTimeSeriesInstance());
 		plot.setRenderer(1, renderer2);
 
 		DateAxis axis = (DateAxis) plot.getDomainAxis();
 		axis.setDateFormatOverride(df);
 
 		ChartPanel chartPanel = new ChartPanel(chart);
 
 		return chartPanel;
 	}
 
 	private XYDataset createDataset(StockInfo stock, StockPriceType priceType) {
 
 		TimeSeries timeSeries = new TimeSeries(stock.getTickerSymbol());
 		for (StockData sd : stock.getStockData()) {
 			cal.setTime(sd.getDate());
 			int month = cal.get(Calendar.MONTH);
 			int day = cal.get(Calendar.DAY_OF_MONTH);
 			int year = cal.get(Calendar.YEAR);
 			timeSeries.add(new Day(day, month + 1, year), priceType.get(sd));
 			// + 1 since calendar month starts at 0
 		}
 
 		TimeSeriesCollection dataset = new TimeSeriesCollection();
 		dataset.addSeries(timeSeries);
 
 		return dataset;
 	}
 
 }
