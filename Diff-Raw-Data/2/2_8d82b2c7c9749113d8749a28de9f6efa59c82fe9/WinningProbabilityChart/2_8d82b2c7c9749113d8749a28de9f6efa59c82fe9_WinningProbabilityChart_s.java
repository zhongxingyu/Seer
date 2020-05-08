 package de.thm.ateam.memory;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.chart.PointStyle;
 import org.achartengine.chart.BarChart.Type;
 import org.achartengine.model.CategorySeries;
 import org.achartengine.model.XYMultipleSeriesDataset;
 import org.achartengine.renderer.SimpleSeriesRenderer;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 import org.achartengine.renderer.XYSeriesRenderer;
 
 import de.thm.ateam.memory.engine.MemoryPlayerDAO;
 import de.thm.ateam.memory.engine.type.Player;
 import de.thm.ateam.memory.game.PlayerList;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Paint.Align;
 import android.util.Log;
 
 
 public class WinningProbabilityChart {
 
   
 
   public Intent getIntent(Context context) {
     PlayerList.getInstance().players = (ArrayList<Player>)MemoryPlayerDAO.getInstance(context).getAllPlayers();
     String[] titles = new String[]{ "# of games played", "# of wins"};
     
     List<double[]> values = new ArrayList<double[]>();
     values.add(new double[PlayerList.getInstance().players.size()]);
     for(int i = 0;i < PlayerList.getInstance().players.size(); i++){
       values.get(0)[i] = PlayerList.getInstance().players.get(i).getGameNumber();
     }
     values.add(new double[PlayerList.getInstance().players.size()]);
     for(int i = 0;i < PlayerList.getInstance().players.size(); i++){
       values.get(1)[i] = PlayerList.getInstance().players.get(i).win;
 
     }
     int[] colors = new int[] { Color.BLUE, Color.CYAN };
     XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
     setChartSettings(renderer, "Win probability", "Player", "# of Games", 0.5,
        12.5, 0, Collections.max(PlayerList.getInstance().players).win + 10, Color.GRAY, Color.LTGRAY);
     renderer.getSeriesRendererAt(0).setDisplayChartValues(true);
     renderer.getSeriesRendererAt(1).setDisplayChartValues(true);
     for(int i = 0; i < PlayerList.getInstance().players.size(); i++){
       renderer.addXTextLabel(i, PlayerList.getInstance().players.get(i).toString());
     }
     renderer.setXLabels(0);
     renderer.setYLabels(10);
     renderer.setXLabelsAlign(Align.CENTER);
     renderer.setYLabelsAlign(Align.CENTER);
     renderer.setPanEnabled(true, false);
     // renderer.setZoomEnabled(false);
     renderer.setZoomRate(1.1f);
     renderer.setBarSpacing(0.5f);
     return ChartFactory.getBarChartIntent(context, buildBarDataset(titles, values), renderer,
         Type.STACKED);
   }
 
   protected XYMultipleSeriesRenderer buildBarRenderer(int[] colors) {
     XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
     renderer.setAxisTitleTextSize(16);
     renderer.setChartTitleTextSize(20);
     renderer.setLabelsTextSize(15);
     renderer.setLegendTextSize(15);
     int length = colors.length;
     for (int i = 0; i < length; i++) {
       SimpleSeriesRenderer r = new SimpleSeriesRenderer();
       r.setColor(colors[i]);
       renderer.addSeriesRenderer(r);
     }
     return renderer;
   }
 
   protected XYMultipleSeriesDataset buildBarDataset(String[] titles, List<double[]> values) {
     XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
     int length = titles.length;
     for (int i = 0; i < length; i++) {
       CategorySeries series = new CategorySeries(titles[i]);
       double[] v = values.get(i);
       int seriesLength = v.length;
       for (int k = 0; k < seriesLength; k++) {
         series.add(v[k]);
       }
       dataset.addSeries(series.toXYSeries());
     }
     return dataset;
   }
   
   protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
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
 
   protected void setRendererProperties(XYMultipleSeriesRenderer renderer, int[] colors,
       PointStyle[] styles) {
     renderer.setAxisTitleTextSize(16);
     renderer.setChartTitleTextSize(20);
     renderer.setLabelsTextSize(15);
     renderer.setLegendTextSize(15);
     renderer.setPointSize(5f);
     renderer.addXTextLabel(0, "foo");
     renderer.setMargins(new int[] { 20, 30, 15, 20 });
     int length = colors.length;
     for (int i = 0; i < length; i++) {
       XYSeriesRenderer r = new XYSeriesRenderer();
       r.setColor(colors[i]);
       r.setPointStyle(styles[i]);
       renderer.addSeriesRenderer(r);
     }
   }
 }
