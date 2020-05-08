 package com.jackpf.csstats.view;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.GraphicalView;
 import org.achartengine.chart.AbstractChart;
 import org.achartengine.model.CategorySeries;
 import org.achartengine.renderer.DefaultRenderer;
 import org.achartengine.renderer.SimpleSeriesRenderer;
 
 import android.content.Context;
 import android.graphics.Color;
 
 public class UIGraph extends GraphicalView
 {
 
 	private UIGraph(Context context, AbstractChart arg1)
 	{
 		super(context, arg1);
 	}
  
 	public static GraphicalView getNewInstance(Context context, HashMap<String, Integer> mapData)
 	{
 		return ChartFactory.getPieChartView(context, getDataSet(mapData), getRenderer(mapData));
 	}
  
 	
 	private static DefaultRenderer getRenderer(HashMap<String, Integer>mapData)
 	{
 		DefaultRenderer defaultRenderer = new DefaultRenderer();
 		
 		for (int i = 0; i < mapData.size(); i++)
 		{
 			Random rnd = new Random();
 			
 			SimpleSeriesRenderer simpleRenderer = new SimpleSeriesRenderer();
 			simpleRenderer.setColor(
 				Color.argb(
 					255,
 					rnd.nextInt(256),
 					rnd.nextInt(256),
 					rnd.nextInt(256)
 				)
 			);
 			defaultRenderer.addSeriesRenderer(simpleRenderer);
 		}
 		
 		defaultRenderer.setShowLabels(true);
 		defaultRenderer.setShowLegend(false);
 		defaultRenderer.setLabelsTextSize(20);
 		
 		return defaultRenderer;
 	}
  
 	private static CategorySeries getDataSet(HashMap<String, Integer> mapData)
 	{
 		CategorySeries series = new CategorySeries("Chart");
 		
 		Iterator<Entry<String, Integer>> iterator = mapData.entrySet().iterator();
 		while (iterator.hasNext()) {
 		    Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>) iterator.next();
 
 		    String key = pair.getKey();
 		    int value =  pair.getValue();
 		    
 		    series.add(key, value);
 		}
 		
 		return series;
 	}
 }
