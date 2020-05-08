 package com.shico.stats.chartengine;
 
 import java.util.List;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.GraphicalView;
 import org.achartengine.chart.BarChart.Type;
 import org.achartengine.model.CategorySeries;
 import org.achartengine.model.XYMultipleSeriesDataset;
 import org.achartengine.renderer.DefaultRenderer;
 import org.achartengine.renderer.SimpleSeriesRenderer;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.Paint.Align;
 import android.graphics.Typeface;
 import android.util.Log;
 
 import com.shico.stats.R;
 import com.shico.stats.loaders.ChartDataLoader;
 
 public class ChartUtil {
 
 	public static GraphicalView createGroupedBarChartViewForChannels(Context context, List<List<String>> rows, 
 			int valueIdx, ChartTitles titles){
 		return createGroupedBarChartView(context, rows, valueIdx, titles, false);
 	}
 
 	public static GraphicalView createGroupedBarChartViewForPrograms(Context context, List<List<String>> rows, 
 			int valueIdx, ChartTitles titles){
 		return createGroupedBarChartView(context, rows, valueIdx, titles, true);
 	}
 
 	public static GraphicalView createPieChartViewForPrograms(Context context, List<List<String>> rows, int valueIdx, String title){
 		return createPieChartView(context, rows, valueIdx, title, true);
 	}
 	
 	public static GraphicalView createPieChartView(Context context, List<List<String>> rows, int valueIdx, String title){
 		return createPieChartView(context, rows, valueIdx, title, false);
 	}
 	
 	private static GraphicalView createGroupedBarChartView(Context context, List<List<String>> rows, 
 			int valueIdx, ChartTitles titles, boolean forProgram){
 		GroupedData data = transform(rows, valueIdx, forProgram);
	    XYMultipleSeriesRenderer renderer = buildBarRenderer(data.getColors(context));
 	    setChartSettings(renderer, titles.title, titles.xTitle, titles.yTitle, 0d,
 	        data.allXLabels.size()+1, 0d, data.getMaxValue()+(data.getMaxValue()/3), Color.GRAY, Color.LTGRAY);
 	    renderer.setDisplayValues(false);
 	    renderer.setXLabels(data.allXLabels.size()+1);
 	    renderer.setYLabels(Math.min(10, data.allTitles.size()));
 	    double i=1;				
 	    for(String label : data.allXLabels){
 	    	renderer.addXTextLabel(i++, label);
 	    }
 	    renderer.setXLabelsAlign(Align.LEFT);
 	    renderer.setYLabelsAlign(Align.LEFT);
 	    renderer.setXLabelsAngle(45f);
 	    renderer.setXLabelsPadding(10f);
 	    renderer.setXLabels(0);
 	    renderer.setMargins(new int[]{45,45,45,25});
 	    renderer.setLegendHeight(100);
 	    Log.d("ChartUtil", ">>>> data.allTitles.size = "+(data.allTitles == null ?  0 : data.allTitles.size()));
 	    renderer.setBarWidth((data.allTitles != null && data.allTitles.size() >= 10) ? 2f : 5f);
 	    renderer.setBarSpacing(0.2f);
 	    renderer.setFitLegend(true);
 	    renderer.setApplyBackgroundColor(true);
 	    renderer.setBackgroundColor(context.getResources().getColor(R.color.DarkSlateGray));
 	    renderer.setMarginsColor(context.getResources().getColor(R.color.DarkSlateGray));
 	    
 	    GraphicalView barChartView = ChartFactory.getBarChartView(context, buildBarDataset(data), renderer, Type.DEFAULT);
 	    return barChartView;
 	}
 
 	private static GraphicalView createPieChartView(Context context, List<List<String>> rows, int valueIdx, String title, boolean forPrograms){
 		// build category-renderer
 	    DefaultRenderer renderer = new DefaultRenderer();
 	    renderer.setLabelsTextSize(15);
 	    renderer.setLegendTextSize(15);
 	    renderer.setChartTitle(title);
 	    renderer.setChartTitleTextSize(20);
 	    renderer.setMargins(new int[] { 20, 30, 15, 0 });
 	    for (int i=0; i<rows.size(); i++) {
 	      SimpleSeriesRenderer r = new SimpleSeriesRenderer();
 	      r.setColor(ChartUtil.getColorScheme(context)[i]);
 	      renderer.addSeriesRenderer(r);
 	    }
 	    if(!rows.isEmpty()){	    	
 	    	SimpleSeriesRenderer r = renderer.getSeriesRendererAt(0);
 	    	r.setHighlighted(true);
 	    }
 	    renderer.setPanEnabled(false);	    
 	    renderer.setDisplayValues(true);
 	    renderer.setShowLabels(true);
 	    renderer.setFitLegend(true);
 	    renderer.setApplyBackgroundColor(true);
 	    renderer.setBackgroundColor(context.getResources().getColor(R.color.DarkSlateGray));
 
 	    // dataset	    
 	    CategorySeries series = new CategorySeries(title);
 	    for (List<String> row : rows) {
 	    	if(forPrograms){
 	    		series.add(row.get(ChartDataLoader.titleIdx), Double.parseDouble(row.get(valueIdx)));
 	    	}else{
 	    		series.add(row.get(ChartDataLoader.nameIdx), Double.parseDouble(row.get(valueIdx)));
 	    	}
 		}
 	    GraphicalView pieChartView = ChartFactory.getPieChartView(context, series, renderer);
 	    return pieChartView;
 	}
 	
 	private static XYMultipleSeriesDataset buildBarDataset(GroupedData groupedData) {
 		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
 		for (String title : groupedData.allTitles) {
 			CategorySeries series = new CategorySeries(title);
 			for (String xLabel : groupedData.allXLabels) {
 				series.add(groupedData.getValue(xLabel, title));
 			}
 			dataset.addSeries(series.toXYSeries());
 		}
 		return dataset;
 	}
 
 	private static GroupedData transform(List<List<String>> rows, int valueIdx, boolean forProgram){
 		GroupedData groupedData = new GroupedData(valueIdx);
 		for (List<String> row : rows) {
 			String time = row.get(ChartDataLoader.timeIdx);
 			String name = null;
 			if(forProgram){
 				name = row.get(ChartDataLoader.titleIdx);
 			}else{
 				name = row.get(ChartDataLoader.nameIdx);
 			}
 			groupedData.addXLabel(time);
 			groupedData.addTitle(name);
 			groupedData.addValue(time, name, Double.valueOf(row.get(ChartDataLoader.viewersIdx)));
 		}	
 		return groupedData;
 	}
 
 	/**
 	 * Builds a bar multiple series renderer to use the provided colors.
 	 * 
 	 * @param colors the series renderers colors
 	 * @return the bar multiple series renderer
 	 */
 	private static XYMultipleSeriesRenderer buildBarRenderer(int[] colors) {
 		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
 		renderer.setTextTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
 		renderer.setAxisTitleTextSize(20);
 		renderer.setChartTitleTextSize(20);
 		renderer.setLabelsTextSize(15);
 		renderer.setLegendTextSize(15);
 		int length = colors.length;
 		for (int i = 0; i < length; i++) {
 			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
 			r.setColor(colors[i]);
 			renderer.addSeriesRenderer(r);
 		}
 		renderer.setPanEnabled(false, false);
 		return renderer;
 	}
 
 	private static void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
 			String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor,
 			int labelsColor) {
 		renderer.setChartTitle(title);
 		renderer.setXTitle(xTitle);
 		renderer.setYTitle(yTitle);
 		renderer.setXAxisMin(xMin);
 		renderer.setXAxisMax(xMax);
 		renderer.setYAxisMin(yMin);
 		renderer.setYAxisMax(yMax);
 		renderer.setAxesColor(axesColor);
 		renderer.setLabelsColor(labelsColor);
 	}
 
 	private static int[] colorScheme;
 	public static int[] getColorScheme(Context ctx){
 		if(colorScheme == null){
 			colorScheme = new int[]{
 					ctx.getResources().getColor(R.color.AliceBlue),
 					ctx.getResources().getColor(R.color.AntiqueWhite),
 					ctx.getResources().getColor(R.color.Aqua),
 					ctx.getResources().getColor(R.color.Beige),
 					ctx.getResources().getColor(R.color.Black),
 					ctx.getResources().getColor(R.color.Brown),
 					ctx.getResources().getColor(R.color.Coral),
 					ctx.getResources().getColor(R.color.Crimson),
 					ctx.getResources().getColor(R.color.Cyan),
 					ctx.getResources().getColor(R.color.DarkGoldenrod),
 					ctx.getResources().getColor(R.color.DarkGray),
 					ctx.getResources().getColor(R.color.DarkKhaki),
 					ctx.getResources().getColor(R.color.DarkMagenta),
 					ctx.getResources().getColor(R.color.DarkOrange),
 					ctx.getResources().getColor(R.color.Gold),
 					ctx.getResources().getColor(R.color.LightBlue),
 					ctx.getResources().getColor(R.color.LightCoral),
 					ctx.getResources().getColor(R.color.LightYellow),
 					ctx.getResources().getColor(R.color.MistyRose),
 					ctx.getResources().getColor(R.color.Moccasin),
 					ctx.getResources().getColor(R.color.Navy),
 					ctx.getResources().getColor(R.color.Plum),
 					ctx.getResources().getColor(R.color.Salmon),
 					ctx.getResources().getColor(R.color.Violet),
 					ctx.getResources().getColor(R.color.White),
 					// More colors
 					ctx.getResources().getColor(R.color.GreenYellow),
 					ctx.getResources().getColor(R.color.Goldenrod),
 					ctx.getResources().getColor(R.color.DeepSkyBlue),
 					ctx.getResources().getColor(R.color.FireBrick),
 					ctx.getResources().getColor(R.color.Ivory),
 					ctx.getResources().getColor(R.color.Khaki),
 					ctx.getResources().getColor(R.color.LightBlue),
 					ctx.getResources().getColor(R.color.MediumVioletRed),
 					// More colors
 					ctx.getResources().getColor(R.color.Magenta),
 					ctx.getResources().getColor(R.color.Linen),
 					ctx.getResources().getColor(R.color.MistyRose),
 					ctx.getResources().getColor(R.color.WhiteSmoke),
 					ctx.getResources().getColor(R.color.MediumTurquoise),
 					ctx.getResources().getColor(R.color.Seashell)					
 			};
 		}
 		return colorScheme;
 	}
 	
 }
