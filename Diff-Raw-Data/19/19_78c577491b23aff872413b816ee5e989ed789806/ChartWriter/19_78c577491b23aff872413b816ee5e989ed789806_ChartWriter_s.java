 package org.logparser.io;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartUtilities;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.data.category.DefaultCategoryDataset;
 import org.logparser.IStatsView;
 import org.logparser.Message;
 
 /**
  * Writes a .png file with a chart created from a collection of log entries.
  * 
  * TODO System.setProperty("java.awt.headless","true");
  * TODO customize chart type etc
  * @author jorge.decastro
  */
 public class ChartWriter {
 	private final Map<String, IStatsView<Message>> statsView;
 	private final ILogParser<Message> logParser;
 	private final int x;
 	private final int y;
 	private static final int DEFAULT_PIXELS_X = 1024;
 	private static final int DEFAULT_PIXELS_Y = 768;
 
 	public ChartWriter(final ILogParser<Message> logParser, final Map<String, IStatsView<Message>> keyStats) {
		this.logParser = logParser;
		this.statsView = keyStats;
		this.x = DEFAULT_PIXELS_X;
		this.y = DEFAULT_PIXELS_Y;
 	}
 
 	public ChartWriter(final ILogParser<Message> logParser, final Map<String, IStatsView<Message>> keyStats, final int x, final int y) {
 		this.logParser = logParser;
 		this.statsView = keyStats;
 		this.x = x;
 		this.y = y;
 	}
 
 	public void write(String filePath, String fileName) {
 		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
 
 		Set<Entry<String, IStatsView<Message>>> statsEntries = statsView.entrySet();
 
 		for (Entry<String, IStatsView<Message>> entries : statsEntries) {
 			for (Message message : entries.getValue().getEntries()) {
 				dataset.addValue(Integer.valueOf(message.getMilliseconds()), entries.getKey(), message.getDate());
 			}
 		}
 
 		JFreeChart jFreeChart = ChartFactory.createBarChart(fileName, String.format("%s entries, %s to %s", logParser.getTotalEntries(),
						logParser.getEarliestEntry().getTime(), logParser.getLatestEntry().getTime()), "Milliseconds", dataset, PlotOrientation.VERTICAL, true, false, false);
 
 		jFreeChart.getPlot().setForegroundAlpha(0.5f);
 		jFreeChart.getPlot().setBackgroundAlpha(0.0f);
 
 		String filenameAndPath = String.format("%s%s", filePath, makeFileNameSafe(fileName));
 
 		try {
 			ChartUtilities.saveChartAsPNG(new File(filenameAndPath), jFreeChart, x, y);
 		} catch (IOException ioe) {
 			throw new RuntimeException(String.format("Failed to save chart: %s", filenameAndPath), ioe);
 		}
 	}
 
	private String makeFileNameSafe(String fileName) {
 		String safeName = fileName.replaceAll("/", "-");
 		return String.format("%s.png", safeName);
 	}
 }
