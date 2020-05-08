 package net.incredibles.brtaquiz.util;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.model.CategorySeries;
 import org.achartengine.renderer.DefaultRenderer;
 import org.achartengine.renderer.SimpleSeriesRenderer;
 
 import android.content.Context;
 import android.content.Intent;
 
 public class PieChart {
 	private double[] chartValues;
 	private int[] chartColors;
 	private String[] COMPUTING_PARAMETER;
 	private String graphTitle;
 
 	public double[] getChartValues() {
 		return chartValues;
 	}
 
 	public void setChartValues(double[] chartValues) {
 		this.chartValues = chartValues;
 	}
 
 
 	public int[] getChartColors() {
 		return chartColors;
 	}
 
 	public void setChartColors(int[] chartColors) {
 		this.chartColors = chartColors;
 	}
 
 	public String[] getCOMPUTING_PARAMETER() {
 		return COMPUTING_PARAMETER;
 	}
 
 	public void setCOMPUTING_PARAMETER(String[] cOMPUTING_PARAMETER) {
 		COMPUTING_PARAMETER = cOMPUTING_PARAMETER;
 	}
 
 	public String getGraphTitle() {
 		return graphTitle;
 	}
 
 	public void setGraphTitle(String graphTitle) {
 		this.graphTitle = graphTitle;
 	}
 
 	public DefaultRenderer buildCategoryRenderer(int[] colors) {
 		DefaultRenderer renderer = new DefaultRenderer();
 		renderer.setLabelsTextSize(15);
 		renderer.setLegendTextSize(15);
 		renderer.setMargins(new int[] { 20, 30, 15, 0 });
 		for (int color : colors) {
 			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
 			r.setColor(color);
 			renderer.addSeriesRenderer(r);
 		}
 		return renderer;
 	}
 
 	/**
 	 * Builds a category series using the provided values.
 	 *
	 * @param title the series titles
 	 * @param values the values
 	 * @return the category series
 	 */
 	public CategorySeries buildCategoryDataset(String title, double[] values, String[] graphTitles) {
 		CategorySeries series = new CategorySeries(title);
 		int k = 0;
 		for (double value : values) {
 			series.add(graphTitles[k++], value);
 		}
 
 		return series;
 	}
 
 	/**
 	 * Executes the chart demo.
 	 *
 	 * @param context the context
 	 * @return the built intent
 	 */
 	public Intent execute(Context context) {
 		DefaultRenderer renderer = buildCategoryRenderer(chartColors);
 		renderer.setZoomButtonsVisible(true);
 		renderer.setZoomEnabled(true);
 		renderer.setChartTitleTextSize(20);
 		return ChartFactory.getPieChartIntent(context, buildCategoryDataset(graphTitle, chartValues, COMPUTING_PARAMETER),
 				renderer, graphTitle);
 	}
 }
