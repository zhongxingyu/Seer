 /**
  * 
  */
 package uk.ac.imperial.dws04.Presage2Experiments.Analysis;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.data.statistics.BoxAndWhiskerCalculator;
 import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
 import org.jfree.data.statistics.BoxAndWhiskerItem;
 import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
 import org.jfree.data.xy.XYDataset;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 
 import uk.ac.imperial.dws04.Presage2Experiments.RoadAgent.OwnChoiceMethod;
 import uk.ac.imperial.presage2.core.db.persistent.PersistentSimulation;
 
 /**
  * @author dws04
  *
  */
 public class DefaultBoxAndWhiskerChart implements Chart, Serializable {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 9191511854848960366L;
 	
 	final Long simId;
 	final BoxAndWhiskerCategoryDataset data;
 	final JFreeChart chart;
 	final ChartPanel panel;
 	final OwnChoiceMethod choiceMethod;
 
	public DefaultBoxAndWhiskerChart(Long simId, OwnChoiceMethod choiceMethod, BoxAndWhiskerCategoryDataset data, String title, String categoryLabel) {
 		super();
 		this.simId = simId;
 		this.data = data;
		this.chart = ChartFactory.createBoxAndWhiskerChart(title, categoryLabel, null, data, true);
 		panel = new ChartPanel(chart);
 		this.choiceMethod = choiceMethod;
 	}
 	
	public DefaultBoxAndWhiskerChart(Long simId, OwnChoiceMethod choiceMethod, XYSeriesCollection data, String title, String categoryLabel, Boolean stripNullAndNaNItems) {
 		super();
 		this.simId = simId;
 		this.data = BAWDatasetFromXYCollection(data, stripNullAndNaNItems);
		this.chart = ChartFactory.createBoxAndWhiskerChart(title, categoryLabel, null, this.data, true);
 		panel = new ChartPanel(chart);
 		this.choiceMethod = choiceMethod;
 	}
 
 	private DefaultBoxAndWhiskerCategoryDataset BAWDatasetFromXYCollection(XYSeriesCollection collection, Boolean stripNullAndNaNItems) {
 		DefaultBoxAndWhiskerCategoryDataset result = new DefaultBoxAndWhiskerCategoryDataset();
 		for (Object seriesObj : collection.getSeries()) {
 			XYSeries series = ((XYSeries)seriesObj);
 			result.add(xySeriesToBAW(series, stripNullAndNaNItems), "RowKey", series.getKey());
 		}
 		return result;
 	}
 	
 	public final static BoxAndWhiskerItem xySeriesToBAW(XYSeries series, Boolean stripNullAndNaNItems) {
 		ArrayList<Number> list = SeriesUtils.xySeriesToArrayList(series);
 		return BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(list, stripNullAndNaNItems);
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.ac.imperial.dws04.Presage2Experiments.Analysis.Chart#getPanel()
 	 */
 	@Override
 	public ChartPanel getPanel() {
 		return this.panel;
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.ac.imperial.dws04.Presage2Experiments.Analysis.Chart#getChart()
 	 */
 	@Override
 	public JFreeChart getChart() {
 		return this.chart;
 	}
 
 	@Override
 	public void hideLegend(boolean hide) {
 		if (hide) {
 			this.chart.removeLegend();
 		}
 	}
 
 	public static BoxAndWhiskerCategoryDataset combineDataToBAW(XYSeriesCollection collection, String key, Boolean stripNullAndNaNItems) {
 		ArrayList<Number> data = null;
 		for (Object serObj : collection.getSeries()) {
 			XYSeries series = (XYSeries)serObj;
 			ArrayList<Number> list = SeriesUtils.xySeriesToArrayList(series);
 			//System.out.println("Adding " + list + " to the combined BAW");
 			if (data==null) {
 				data = list;
 			}
 			else {
 				data.addAll(list);
 			}
 		}
 		//System.out.println("Data is " + data);
 		DefaultBoxAndWhiskerCategoryDataset result = new DefaultBoxAndWhiskerCategoryDataset();
 		BoxAndWhiskerItem apache = bawFromApache(data);
 		BoxAndWhiskerItem calculated = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(data, stripNullAndNaNItems);
 		result.add(calculated, key, key);
 		return result;
 	}
 
 	private static BoxAndWhiskerItem bawFromApache(ArrayList<Number> data) {
 		DescriptiveStatistics stats = new DescriptiveStatistics();
 		for (Number number : data) {
 			if (number!=null) {
 				stats.addValue(number.doubleValue());
 			}
 		}
 		double mean = stats.getMean();
 		double median = stats.getPercentile(50);
 		double q1 = stats.getPercentile(25);
 		double q3 = stats.getPercentile(75);
 		//System.out.println(stats);
 		//System.out.println("q1: " + q1);
 		//System.out.println("q3: " + q3);
 		return new BoxAndWhiskerItem(mean, median, q1, q3, null, null, null, null, null);
 	}
 
 	@Override
 	public Long getSimId() {
 	return this.simId;
 	}
 
 	@Override
 	public OwnChoiceMethod getChoiceMethod() {
 		return this.choiceMethod;
 	}
 
 }
