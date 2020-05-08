 /**
  * Copyright (C) 2009, 2010 SC 4ViewSoft SRL
  *  
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *  
  *      http://www.apache.org/licenses/LICENSE-2.0
  *  
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.gadoworld.mathman;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.GraphicalView;
 import org.achartengine.chart.PointStyle;
 import org.achartengine.model.XYMultipleSeriesDataset;
 import org.achartengine.model.XYSeries;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 import org.achartengine.renderer.XYSeriesRenderer;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.LinearLayout;
 
 public class GraphActivity extends Activity {
   /** The main dataset that includes all the series that go into a chart. */
   private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
   /** The main renderer that includes all the renderers customizing a chart. */
   private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
   /** The most recently added series. */
   private XYSeries mCurrentSeries;
   /** The most recently created renderer, customizing the current series. */
   private XYSeriesRenderer mCurrentRenderer;
   /** The chart view that displays the data. */
   private GraphicalView mChartView;
 
   @Override
   protected void onSaveInstanceState(Bundle outState) {
     super.onSaveInstanceState(outState);
     // save the current data, for instance when changing screen orientation
     outState.putSerializable("dataset", mDataset);
     outState.putSerializable("renderer", mRenderer);
     outState.putSerializable("current_series", mCurrentSeries);
     outState.putSerializable("current_renderer", mCurrentRenderer);
   }
 
   @Override
   protected void onRestoreInstanceState(Bundle savedState) {
     super.onRestoreInstanceState(savedState);
     // restore the current data, for instance when changing the screen
     // orientation
     mDataset = (XYMultipleSeriesDataset) savedState.getSerializable("dataset");
     mRenderer = (XYMultipleSeriesRenderer) savedState.getSerializable("renderer");
     mCurrentSeries = (XYSeries) savedState.getSerializable("current_series");
     mCurrentRenderer = (XYSeriesRenderer) savedState.getSerializable("current_renderer");
   }
 
  
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     SharedPreferences prefs = getSharedPreferences("theme", 0);
 	switch (prefs.getInt("theme", 0)) {
 	case 0:
 		setTheme(R.style.AppTheme_Blue_Dark);
 		break;
 	case 1:
 		setTheme(R.style.AppTheme_Blue_Light);
 		break;
 	case 2:
 		setTheme(R.style.AppTheme_Green_Dark);
 		break;
 	case 3:
 		setTheme(R.style.AppTheme_Green_Light);
 		break;
 	case 4:
 		setTheme(R.style.AppTheme_Red_Dark);
 		break;
 	case 5:
 		setTheme(R.style.AppTheme_Red_Light);
 		break;
 	case 6:
 		setTheme(R.style.AppTheme_Orange_Dark);
 		break;
 	case 7:
 		setTheme(R.style.AppTheme_Orange_Light);
 		break;
 	case 8:
 		setTheme(R.style.AppTheme_Purple);
 		break;
 	}
     setContentView(R.layout.activity_graph);
     // set some properties on the main renderer
     mRenderer.setApplyBackgroundColor(true);
     mRenderer.setBackgroundColor(Color.BLACK);
     mRenderer.setAxisTitleTextSize(16);
     mRenderer.setChartTitleTextSize(0);
     mRenderer.setLabelsTextSize(15);
     mRenderer.setLegendTextSize(20);
     mRenderer.setLegendHeight(22);
     mRenderer.setPointSize(5f);
     mRenderer.setAxesColor(Color.BLACK);
     mRenderer.setXLabels(20);
     mRenderer.setYLabels(20);
     mRenderer.setPanEnabled(true);
     mRenderer.setZoomEnabled(true);
     mRenderer.setShowGrid(true);
     mRenderer.setXAxisBold(true);
     mRenderer.setYAxisBold(true);
     mRenderer.setZoomButtonsVisible(true);
    mRenderer.setZoomEnabled(true);
   }
 
   @Override
   protected void onResume() {
     super.onResume();
     if (mChartView == null) {
       LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
      layout.setVisibility(View.INVISIBLE);
       mChartView = ChartFactory.getLineChartView(this, mDataset, mRenderer);
       // enable the chart click events
       mRenderer.setClickEnabled(true);
       mRenderer.setSelectableBuffer(10);
      mChartView.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
            
          }
        });
       layout.addView(mChartView, new LayoutParams(LayoutParams.MATCH_PARENT,
           LayoutParams.MATCH_PARENT));
     } else {
       mChartView.repaint();
     }
   }
   
   public void addSegment(View v) {
 	  mChartView.setOnClickListener(new View.OnClickListener() {
 	        public void onClick(View v) {
 	          
 	        }
 	      });
 	  mRenderer.setZoomEnabled(true);
	  mRenderer.setZoomRate(mRenderer.getZoomRate() + 2);
 	  String seriesTitle = "Series " + (mDataset.getSeriesCount() + 1);
       // create a new series of data
       XYSeries series = new XYSeries(seriesTitle);
       mDataset.addSeries(series);
       mCurrentSeries = series;
       // create a new renderer for the new series
       XYSeriesRenderer renderer = new XYSeriesRenderer();
       mRenderer.addSeriesRenderer(renderer);
       //mRenderer.setZoomButtonsVisible(true);
       // set some renderer properties
       renderer.setPointStyle(PointStyle.CIRCLE);
       renderer.setFillPoints(true);
       renderer.setDisplayChartValues(true);
       renderer.setDisplayChartValuesDistance(40);
       mCurrentRenderer = renderer;
       mChartView.repaint();
       CustomDialog cdd=new CustomDialog(this);
       cdd.show(); 
   }
   
   public void add(Double x1, Double x2, Double y1, Double y2) {
       mCurrentSeries.add(x1, y1);
       // repaint the chart such as the newly added point to be visible
       mChartView.repaint();
       mCurrentSeries.add(x2, y2);
       mChartView.repaint();
      LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
      layout.setVisibility(View.VISIBLE);
   }
 }
