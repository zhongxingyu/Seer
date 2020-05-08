 package com.kinnack.nthings;
 
 import java.util.List;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.chart.BarChart.Type;
 import org.achartengine.model.CategorySeries;
 import org.achartengine.model.XYMultipleSeriesDataset;
 import org.achartengine.renderer.SimpleSeriesRenderer;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Paint.Align;
 
 import com.kinnack.nthings.model.History;
 import com.kinnack.nthings.model.Logg;
 
 public class ProgressChart {
     final static int DARK_COLOR = Color.argb(255, 51, 51, 51);
     final static int APPROVED_GREEN = Color.rgb(0, 204, 0);
     
     public Intent progressChart(History history_, Context context_) {
         XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
         SimpleSeriesRenderer seriesRenderer = new SimpleSeriesRenderer();
         seriesRenderer.setColor(APPROVED_GREEN);
         renderer.addSeriesRenderer(seriesRenderer);
         renderer.setOrientation(Orientation.HORIZONTAL);
         
         
         //settings
         renderer.setChartTitle(history_.getType()+" Progress");
         renderer.setXTitle("Workout");
 
         renderer.setYTitle("Count");
         renderer.setYAxisMin(0);
         renderer.setYLabelsAngle(270f);
         renderer.setYLabelsAlign(Align.RIGHT);
         
         renderer.setAxesColor(DARK_COLOR);
         renderer.setLabelsColor(DARK_COLOR);
         renderer.setLabelsTextSize(24f);
 
         renderer.setChartTitleTextSize(24f);
         
         renderer.setShowLegend(false);
         
         renderer.setChartValuesTextSize(24f);
         renderer.setDisplayChartValues(true);
 
         renderer.setMargins(new int[]{30,40,50,0});
         renderer.setMarginsColor(Color.WHITE);
         
         renderer.setBarSpacing(0.5);
         
         XYMultipleSeriesDataset data = new XYMultipleSeriesDataset();
         CategorySeries series = new CategorySeries("Progress");
         List<Logg> logs = history_.getLogs();
         int maxCount = 50;
         for(int i = 0,len = logs.size(); i < len; i++) {
             Logg log = logs.get(i);
             int total = log.getTotalCount();
             if (maxCount < total) {maxCount = total;}
             series.add(total);
         }
         int numberOfWorkouts = Math.max(logs.size()+1,10);
         int xMin = numberOfWorkouts - 13;
       
        renderer.setXAxisMin(Math.max(1,xMin));
         renderer.setXAxisMax(numberOfWorkouts);
         renderer.setXLabels(numberOfWorkouts - xMin);
         renderer.setYAxisMax(maxCount+10);
         renderer.setYLabels(15);
         data.addSeries(series.toXYSeries());
         
         return ChartFactory.getBarChartIntent(context_, data, renderer, Type.DEFAULT);
     }
     
     
 }
