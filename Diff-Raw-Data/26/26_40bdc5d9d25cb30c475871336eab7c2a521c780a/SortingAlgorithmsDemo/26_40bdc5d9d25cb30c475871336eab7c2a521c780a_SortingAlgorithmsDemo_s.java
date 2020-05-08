 package pe.edu.pucp.algorithms.sorting;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JPanel;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.DateAxis;
 import org.jfree.chart.axis.DateTickMarkPosition;
 import org.jfree.chart.labels.StandardXYToolTipGenerator;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.XYBarRenderer;
 import org.jfree.chart.title.TextTitle;
 import org.jfree.data.time.TimeSeries;
 import org.jfree.data.time.TimeSeriesCollection;
 import org.jfree.data.time.TimeSeriesDataItem;
 import org.jfree.data.time.Year;
 import org.jfree.data.xy.IntervalXYDataset;
 import org.jfree.ui.ApplicationFrame;
 import org.jfree.ui.RefineryUtilities;
 
 import pe.edu.pucp.algorithms.sorting.algs.ArrayChangeListener;
 import pe.edu.pucp.algorithms.sorting.algs.BaseSorter;
 import pe.edu.pucp.algorithms.sorting.algs.SorterFactory;
 import pe.edu.pucp.algorithms.sorting.algs.SortingAlgorithm;
 
 public class SortingAlgorithmsDemo extends ApplicationFrame implements
 		ArrayChangeListener<CustomTimeSeriesDataItem> {
 
 	private static final long serialVersionUID = -6451446062734426445L;
 
 	private static final String TITLE = "Algoritmos de Ordenamiento";
 	private static final String SUB_TITLE = "PUCP - Maestra en Informtica";
 	private static final String X_LABEL = "ndice";
 	private static final String Y_LABEL = "Tamao";
 	private static final String BAR_LABEL = "Elemento en el arreglo";
 
 	private static TimeSeries timeSeries;
 
 	public SortingAlgorithmsDemo(String s, TimeSeriesDataItem[] dataToSort) {
 		super(s);
 		JPanel jpanel = createDemoPanel(dataToSort);
 		jpanel.setPreferredSize(new Dimension(500, 270));
 		setContentPane(jpanel);
 	}
 
 	private static JFreeChart createChart(IntervalXYDataset intervalxydataset) {
 		JFreeChart jfreechart = ChartFactory.createXYBarChart(TITLE, X_LABEL,
 				true, Y_LABEL, intervalxydataset, PlotOrientation.VERTICAL,
 				true, false, false);
 		jfreechart.addSubtitle(new TextTitle(SUB_TITLE, new Font("Dialog", 2,
 				10)));
 		jfreechart.setBackgroundPaint(Color.white);
 		XYPlot xyplot = (XYPlot) jfreechart.getPlot();
 		XYBarRenderer xybarrenderer = (XYBarRenderer) xyplot.getRenderer();
 		StandardXYToolTipGenerator standardxytooltipgenerator = new StandardXYToolTipGenerator(
 				"{1} = {2}", new SimpleDateFormat("yyyy"), new DecimalFormat(
 						"0"));
 		xybarrenderer.setBaseToolTipGenerator(standardxytooltipgenerator);
 		xybarrenderer.setMargin(0.10000000000000001D);
 		xyplot.setBackgroundPaint(Color.lightGray);
 		xyplot.setRangeGridlinePaint(Color.white);
 		DateAxis dateaxis = (DateAxis) xyplot.getDomainAxis();
 		dateaxis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
 		dateaxis.setLowerMargin(0.01D);
 		dateaxis.setUpperMargin(0.01D);
 		return jfreechart;
 	}
 
 	private static IntervalXYDataset startDataSet(
 			TimeSeriesDataItem[] dataAsArray) {
 		timeSeries = new TimeSeries(BAR_LABEL, X_LABEL, "Count");
 		for (TimeSeriesDataItem dataItem : dataAsArray) {
 			timeSeries.add(dataItem);
 		}
 		TimeSeriesCollection timeseriescollection = new TimeSeriesCollection(
 				timeSeries);
 		return timeseriescollection;
 	}
 
 	private static void updateDataSet(TimeSeries timeSeries,
 			CustomTimeSeriesDataItem[] dataAsArray) {
 		timeSeries.clear();
 		int currentItem = 1;
 		for (CustomTimeSeriesDataItem dataItem : dataAsArray) {
 			timeSeries.addOrUpdate(new Year(currentItem), dataItem.getValue());
 			currentItem++;
 		}
 	}
 
 	public static JPanel createDemoPanel(TimeSeriesDataItem[] dataAsArray) {
 		IntervalXYDataset dataSet = startDataSet(dataAsArray);
 		JFreeChart chart = createChart(dataSet);
 		ChartPanel chartPanel = new ChartPanel(chart);
 		return chartPanel;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static void main(String args[]) {
 		CustomTimeSeriesDataItem[] dataToSort = getDataToSort();
 		SortingAlgorithmsDemo sortingAlgorithmDemo = new SortingAlgorithmsDemo(
 				TITLE, dataToSort);
 		sortingAlgorithmDemo.pack();
 		RefineryUtilities.centerFrameOnScreen(sortingAlgorithmDemo);
 		sortingAlgorithmDemo.setVisible(true);
 		BaseSorter<CustomTimeSeriesDataItem> sorter = SorterFactory.getSorter(
				dataToSort, SortingAlgorithm.BUBBLE);
 		sorter.setArrayChangeListener(sortingAlgorithmDemo);
 		sorter.sort();
 		updateDataSet(timeSeries, sorter.getDataArray());
		System.out.println("You should see sorted input now");
 	}
 
 	@Override
 	public void arrayChanged(CustomTimeSeriesDataItem[] dataArray) {

		System.out.println("In arrayChanged");
		updateDataSet(timeSeries, dataArray);
 		try {
			Thread.sleep(1000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}

 	}
 
 	private static CustomTimeSeriesDataItem[] getDataToSort() {
 		List<CustomTimeSeriesDataItem> dataToSort = new ArrayList<CustomTimeSeriesDataItem>();
 		dataToSort
 				.add(new CustomTimeSeriesDataItem(new Year(1), new Integer(0)));
 		dataToSort
 				.add(new CustomTimeSeriesDataItem(new Year(2), new Integer(1)));
 		dataToSort
 				.add(new CustomTimeSeriesDataItem(new Year(3), new Integer(0)));
 		dataToSort
 				.add(new CustomTimeSeriesDataItem(new Year(4), new Integer(2)));
 		dataToSort
 				.add(new CustomTimeSeriesDataItem(new Year(5), new Integer(0)));
 		dataToSort
 				.add(new CustomTimeSeriesDataItem(new Year(6), new Integer(1)));
 		dataToSort
 				.add(new CustomTimeSeriesDataItem(new Year(7), new Integer(2)));
 		dataToSort
 				.add(new CustomTimeSeriesDataItem(new Year(8), new Integer(5)));
 		dataToSort.add(new CustomTimeSeriesDataItem(new Year(9),
 				new Integer(21)));
 		dataToSort.add(new CustomTimeSeriesDataItem(new Year(10), new Integer(
 				18)));
 		dataToSort.add(new CustomTimeSeriesDataItem(new Year(11), new Integer(
 				18)));
 		dataToSort.add(new CustomTimeSeriesDataItem(new Year(12), new Integer(
 				25)));
 		dataToSort.add(new CustomTimeSeriesDataItem(new Year(13), new Integer(
 				11)));
 		dataToSort.add(new CustomTimeSeriesDataItem(new Year(14), new Integer(
 				16)));
 		dataToSort.add(new CustomTimeSeriesDataItem(new Year(15), new Integer(
 				23)));
 		dataToSort.add(new CustomTimeSeriesDataItem(new Year(16), new Integer(
 				14)));
 		dataToSort.add(new CustomTimeSeriesDataItem(new Year(17), new Integer(
 				31)));
 		dataToSort.add(new CustomTimeSeriesDataItem(new Year(18), new Integer(
 				38)));
 		dataToSort.add(new CustomTimeSeriesDataItem(new Year(19), new Integer(
 				31)));
 		dataToSort.add(new CustomTimeSeriesDataItem(new Year(20), new Integer(
 				56)));
 		dataToSort.add(new CustomTimeSeriesDataItem(new Year(21), new Integer(
 				45)));
 		dataToSort.add(new CustomTimeSeriesDataItem(new Year(22), new Integer(
 				74)));
 		dataToSort.add(new CustomTimeSeriesDataItem(new Year(23), new Integer(
 				68)));
 		dataToSort.add(new CustomTimeSeriesDataItem(new Year(24), new Integer(
 				98)));
 		dataToSort.add(new CustomTimeSeriesDataItem(new Year(25), new Integer(
 				85)));
 		dataToSort.add(new CustomTimeSeriesDataItem(new Year(26), new Integer(
 				66)));
 		dataToSort.add(new CustomTimeSeriesDataItem(new Year(27), new Integer(
 				71)));
 		dataToSort.add(new CustomTimeSeriesDataItem(new Year(28), new Integer(
 				65)));
 		dataToSort.add(new CustomTimeSeriesDataItem(new Year(29), new Integer(
 				59)));
 		dataToSort.add(new CustomTimeSeriesDataItem(new Year(30), new Integer(
 				60)));
 		return dataToSort.toArray(new CustomTimeSeriesDataItem[dataToSort
 				.size()]);
 	}
 
 }
