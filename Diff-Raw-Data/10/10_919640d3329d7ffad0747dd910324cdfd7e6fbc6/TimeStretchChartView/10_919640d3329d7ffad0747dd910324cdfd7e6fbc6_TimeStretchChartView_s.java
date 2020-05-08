 /**
 	This file is part of Personal Trainer.
 
     Personal Trainer is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     any later version.
 
     Personal Trainer is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Personal Trainer.  If not, see <http://www.gnu.org/licenses/>.
 
     (C) Copyright 2012: Daniel Kvist, Henrik Hugo, Gustaf Werlinder, Patrik Thitusson, Markus Schutzer
  */
 package se.team05.view;
 
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.GraphicalView;
 import org.achartengine.chart.AbstractChart;
 import org.achartengine.chart.PointStyle;
 import org.achartengine.model.TimeSeries;
 import org.achartengine.model.XYMultipleSeriesDataset;
 import org.achartengine.renderer.SimpleSeriesRenderer;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 import org.achartengine.renderer.XYSeriesRenderer;
 
 import se.team05.content.Result;
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.Paint.Align;
 
 /**
  * This class uses the library achartengine and draws a time chart with the time
  * passed on each result.
  * 
  * @author Patrik Thituson
  * 
  */
 public class TimeStretchChartView extends GraphicalView
 {
 	private static final String DATE_FORMAT = "yyyy-MM-dd";
 	private static int maxTime = 60;
 	private static int minTime = 0;
 	private static long minTimeStamp;
 	private static long maxTimeStamp;
 	private static XYMultipleSeriesRenderer renderer;
 
 	/**
 	 * The Constructor which is not used for now
 	 * 
 	 * @param context
 	 * @param abstractChart
 	 */
 	private TimeStretchChartView(Context context, AbstractChart abstractChart)
 	{
 		super(context, abstractChart);
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * This method returns a new graphical view as a time chart view. It uses
 	 * the time passed and Date to create the chart.
 	 * 
 	 * @param context
 	 * @param results
 	 *            the list of results that should be drawn on the chart
 	 * @param nameOfRoute
 	 *            the name of the route to display the name
 	 * @return
 	 */
 	public static GraphicalView getNewInstance(Context context, List<Result> results, String nameOfRoute)
 	{
 		if (results.size() != 1)
 		{
 			int resultMaxTime = Collections.max(results).getTime() / 60;
 			maxTime = resultMaxTime >= maxTime ? resultMaxTime : maxTime;
 		}
		minTimeStamp = results.get(0).getTimestamp() * 1000;
		maxTimeStamp = results.get(results.size() - 1).getTimestamp() * 1000;
 		String title = "Route: " + nameOfRoute;
 		renderer = createRendere();
 		setChartSettings();
 
 		SimpleSeriesRenderer seriesRenderer = renderer.getSeriesRendererAt(0);
 		seriesRenderer.setDisplayChartValues(true);
 		return ChartFactory.getTimeChartView(context, createDateTimeSet(title, results), renderer, DATE_FORMAT);
 	}
 
 	/**
 	 * This method creates a XYMultipleSeriesDataset which contains pair of time
 	 * passed and date (the X and Y value)
 	 * 
 	 * @param title
 	 *            the title of the route
 	 * @param results
 	 *            a list of results wich should be added to the series
 	 * @return XYMultipleSeriesDataset 
 	 * 			  the dataset with the data that should be drawn up
 	 */
 	protected static XYMultipleSeriesDataset createDateTimeSet(String title, List<Result> results)
 	{
 		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
 		TimeSeries series = new TimeSeries(title);
 		for (Result result : results)
 		{
 			Date date = new Date(result.getTimestamp() * 1000);
 			series.add(date, result.getTime() / 60);
 		}
 		dataset.addSeries(series);
 		return dataset;
 	}
 
 	/**
 	 * 
 	 * @return XYMultipleSeriesRenderer
 	 */
 	protected static XYMultipleSeriesRenderer createRendere()
 	{
 
 		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
 		renderer.setAxisTitleTextSize(16);
 		renderer.setXLabelsColor(Color.BLUE);
 		renderer.setBackgroundColor(Color.WHITE);
 		renderer.setShowGridX(true);
 		renderer.setGridColor(Color.BLACK);
 		renderer.setChartTitleTextSize(20);
 		renderer.setLabelsTextSize(15);
 		renderer.setLegendTextSize(15);
 		renderer.setPointSize(5f);
 		renderer.setYLabelsAlign(Align.RIGHT);
 		renderer.setYLabelsColor(VISIBLE, Color.BLUE);
 		renderer.setMargins(new int[] { 20, 50, 15, 20 });
 		XYSeriesRenderer xySerieRenderer = new XYSeriesRenderer();
 		xySerieRenderer.setColor(Color.BLUE);
 		xySerieRenderer.setPointStyle(PointStyle.CIRCLE);
 		renderer.addSeriesRenderer(xySerieRenderer);
 
 		return renderer;
 	}
 
 	/**
 	 * Sets the settings of the chart.
 	 */
 	protected static void setChartSettings()
 	{
 		renderer.setXLabels(5);
 		renderer.setYLabels(10);
 		renderer.setChartTitle("TimePassed");
 		renderer.setYTitle("Minutes");
 		renderer.setXAxisMin(minTimeStamp);
 		renderer.setXAxisMax(maxTimeStamp);
 		renderer.setYAxisMin(minTime);
 		renderer.setYAxisMax(maxTime);
 		renderer.setAxesColor(Color.BLACK);
 		renderer.setLabelsColor(Color.BLACK);
 		renderer.setBackgroundColor(Color.WHITE);
 		renderer.setMarginsColor(Color.WHITE);
 		renderer.setApplyBackgroundColor(true);
 	}
 
 }
