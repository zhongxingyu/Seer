 package ui.stats;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Stroke;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import model.GlobalSummary;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.DateAxis;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.axis.TickUnitSource;
 import org.jfree.chart.labels.StandardXYItemLabelGenerator;
 import org.jfree.chart.labels.StandardXYToolTipGenerator;
 import org.jfree.chart.labels.XYItemLabelGenerator;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.data.time.TimePeriodAnchor;
 import org.jfree.data.time.TimeSeries;
 import org.jfree.data.time.TimeSeriesCollection;
 
 import ui.DateRangeNullable;
 import ui.GuiController;
 import util.DateUtil;
 
 public class ComparativeConsultationPerWeek extends AbstractDateRangeStat {
 
 	private GuiController controller;
 
 	public ComparativeConsultationPerWeek(GuiController controller) {
 		super.setActionListener(new ProcessListener());
 		this.controller = controller;
 	}
 
 	public String getName() {
		return "Diagramme comparatif des consulations par semaine";
 	}
 
 	private List<GlobalSummary> processStats(DateRangeNullable dateRange) {
 		return controller.listAllSummaryInRange(dateRange, null, null);
 	}
 
 	private void processResult(List<GlobalSummary> summaryList) {
 		resultPanel.removeAll();
 
 		Map<Integer, Collection<GlobalSummary>> summaryMap = orderSummariesByYear(summaryList);
 
 		Set<Entry<Integer, Collection<GlobalSummary>>> entrySet = summaryMap.entrySet();
 		List<TimeSeriesCollection> dataSetList = new ArrayList<TimeSeriesCollection>();
 		for (Entry<Integer, Collection<GlobalSummary>> entry : entrySet) {
 			TimeSeriesCollection dataSet = new TimeSeriesCollection();
 			dataSet.setXPosition(TimePeriodAnchor.MIDDLE);
 			Integer year = entry.getKey();
 			Collection<GlobalSummary> col = entry.getValue();
 			TimeSeries series = new TimeSeries("Consultations " + year);
 			for (GlobalSummary summary : col) {
 				org.jfree.data.time.Week week = new org.jfree.data.time.Week(summary.getWeek().getWeekNbrInYear(), year);
 				series.add(week, summary.getTotalNbrConsultation());
 			}
 			dataSet.addSeries(series);
 			dataSetList.add(dataSet);
 		}
 
 		JFreeChart chart = createChart(dataSetList, "Consultations", "Date", "Consultations", "dd-MMM");
 		ChartPanel chartPanel = new ChartPanel(chart, false);
 		chartPanel.setDomainZoomable(false);
 		chartPanel.setRangeZoomable(false);
 		resultPanel.add(chartPanel);
 		resultPanel.updateUI();
 	}
 
 	private JFreeChart createChart(List<TimeSeriesCollection> dataSetList, String title, String domainTitle,
 			String rangeTitle, String dateFormat) {
 
 		// Y axis
 		NumberAxis valueAxis = new NumberAxis(rangeTitle);
 		TickUnitSource ticks = NumberAxis.createIntegerTickUnits();
 		valueAxis.setStandardTickUnits(ticks);
 		// The plot
 		XYPlot plot = new XYPlot(null, null, valueAxis, null);
 		int i = 0;
 		for (TimeSeriesCollection dataSet : dataSetList) {
 			// Create an X axis, not visible
 			DateAxis localDateAxis = new DateAxis(domainTitle);
 			localDateAxis.setVisible(false);
 			localDateAxis.setDateFormatOverride(new SimpleDateFormat(dateFormat));
 			plot.setDomainAxis(i, localDateAxis);
 			plot.setDataset(i, dataSet);
 			plot.mapDatasetToDomainAxis(i, i);
 
 			// Add a renderer
 			XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
 			renderer.setUseFillPaint(true);
 			renderer.setBaseFillPaint(Color.white);
 			renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{1}: {2}",
 					new SimpleDateFormat("MMM yyyy"), new DecimalFormat("0")));
 
 			// label the points
 			NumberFormat format = NumberFormat.getNumberInstance();
 			format.setMaximumFractionDigits(2);
 			XYItemLabelGenerator generator = new StandardXYItemLabelGenerator(
 					StandardXYItemLabelGenerator.DEFAULT_ITEM_LABEL_FORMAT, format, format);
 			renderer.setBaseItemLabelGenerator(generator);
 			renderer.setBaseItemLabelsVisible(true);
 			// A nice dot for each point
 			Stroke stroke = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
 			renderer.setBaseOutlineStroke(stroke);
 			plot.setRenderer(i, renderer);
 			i++;
 		}
 		// Set one axis visible and change it
 		plot.getDomainAxis().setVisible(true);
 		JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
 		ChartFactory.getChartTheme().apply(chart);
 		return chart;
 
 	}
 
 	private Map<Integer, Collection<GlobalSummary>> orderSummariesByYear(List<GlobalSummary> summaryList) {
 		Map<Integer, Collection<GlobalSummary>> summaryMap = new LinkedHashMap<Integer, Collection<GlobalSummary>>();
 
 		for (GlobalSummary summary : summaryList) {
 			Integer year = DateUtil.getYearForWeek(summary.getWeek().getStartDate());
 			if (!summaryMap.containsKey(year)) {
 				summaryMap.put(year, new HashSet<GlobalSummary>());
 			}
 			Collection<GlobalSummary> col = summaryMap.get(year);
 			col.add(summary);
 		}
 
 		return summaryMap;
 	}
 
 	private class ProcessListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			DateRangeNullable dateRange = dateRangePanel.getDateRange();
 			List<GlobalSummary> summaryList = processStats(dateRange);
 			processResult(summaryList);
 		}
 
 	}
 
 }
