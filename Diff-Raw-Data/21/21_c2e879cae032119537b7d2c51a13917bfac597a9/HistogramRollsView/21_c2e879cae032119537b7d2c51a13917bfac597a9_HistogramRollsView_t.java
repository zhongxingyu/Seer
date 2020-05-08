 package com.bigtheta.ragedice;
 
 import java.util.HashMap;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.GraphicalView;
 import org.achartengine.chart.BarChart;
 import org.achartengine.model.XYMultipleSeriesDataset;
 import org.achartengine.model.XYSeries;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 import org.achartengine.renderer.XYSeriesRenderer;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.view.View;
 
 public class HistogramRollsView extends View {
     private XYMultipleSeriesDataset m_dataset = new XYMultipleSeriesDataset();
     private XYMultipleSeriesRenderer m_renderer = new XYMultipleSeriesRenderer();
     private GraphicalView m_chartView;
 
     public HistogramRollsView(Context context, AttributeSet attrs) {
         super(context, attrs);
 
         m_dataset = new XYMultipleSeriesDataset();
         m_renderer = new XYMultipleSeriesRenderer();
         XYSeriesRenderer renderer = new XYSeriesRenderer();
         renderer.setColor(getResources().getColor(R.color.histogram_bar));
         m_renderer.addSeriesRenderer(renderer);
         m_renderer.setBarSpacing(0.1);
         m_renderer.setYAxisMin(0.0);
         m_renderer.setBackgroundColor(getResources().getColor(R.color.histogram_background));
         m_renderer.setApplyBackgroundColor(true);
         m_renderer.setLabelsTextSize(20);
         m_renderer.setXLabelsColor(getResources().getColor(R.color.histogram_labels));
         m_renderer.setYLabelsColor(0, getResources().getColor(R.color.histogram_labels));
         m_renderer.setYLabelsAlign(Paint.Align.RIGHT);
         updateDataset();
         m_chartView = ChartFactory.getBarChartView(context, m_dataset, m_renderer, BarChart.Type.DEFAULT);
     }
 
     public HistogramRollsView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
     }

     private void updateDataset() {
         XYSeries series = new XYSeries("Rolls");
         for (int i = 0; i < m_dataset.getSeriesCount(); i++) {
            m_dataset.removeSeries(0);
         }

         Game game = MainActivity.getGame();
         if (game != null) {
             HashMap<Integer, Double> pmf = DieDescription.getPMF(game.getId());
             HashMap<Integer, Integer> observedRolls = DiceRoll.getObservedRolls(game.getId());
             Integer min = null;
             Integer max = null;
            Integer tallest = null;
             for (Integer key : pmf.keySet()) {
                 Integer val = observedRolls.get(key);
                 if (val == null) {
                     series.add((double)key, 0.0);
                 } else {
                     series.add((double)key, (double)val);
                 }
                 if (min == null || key < min) {
                     min = key;
                 }
                 if (max == null || key > max) {
                     max = key;
                 }
                if (val != null && (tallest == null || val > tallest)) {
                    tallest = val;
                }
             }
             m_renderer.setXLabels(pmf.size());
             m_renderer.setXAxisMin((double)min - 0.5);
             m_renderer.setXAxisMax((double)max + 0.5);
            if (tallest != null) {
                m_renderer.setYAxisMax((double)tallest + 2.0);
            }
         }
         m_dataset.addSeries(series);
     }

     @Override
     public void onDraw(Canvas canvas) {
         super.onDraw(canvas);
         updateDataset();
         m_chartView.draw(canvas);
     }
 }
