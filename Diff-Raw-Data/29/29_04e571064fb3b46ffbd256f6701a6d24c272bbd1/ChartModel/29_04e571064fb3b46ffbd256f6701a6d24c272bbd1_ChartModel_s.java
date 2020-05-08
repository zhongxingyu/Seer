 package org.pentaho.chart.model;
 
 import java.io.Serializable;
 import java.util.HashMap;
 
 import org.pentaho.chart.model.ChartLegend;
 
 public class ChartModel implements Serializable {
  public class ChartTheme{
    public static final int THEME_NULL = -1;
    public static final int THEME1 = 0;
    public static final int THEME2 = 1;
    };
   HashMap<String, String> styles = new HashMap<String, String>();
   
   String title;
   String subtitle;
   ChartLegend chartLegend;
   Integer backgroundColor;
   Plot plot;
   
  int theme;
 
  public int getTheme() {
     return theme;
   }
 
  public void setTheme(int theme) {
     this.theme = theme;
   }
 
   public String getTitle() {
     return title;
   }
 
   public void setTitle(String title) {
     this.title = title;
   }
 
   public ChartLegend getChartLegend() {
     return chartLegend;
   }
 
   public void setChartLegend(ChartLegend chartLegend) {
     this.chartLegend = chartLegend;
   }
 
   public Integer getBackgroundColor() {
     return backgroundColor;
   }
 
   public void setBackgroundColor(Integer backgroundColor) {
     this.backgroundColor = backgroundColor;
   }
 
   public Plot getPlot() {
     return plot;
   }
 
   public void setPlot(Plot plot) {
     this.plot = plot;
   }
 
   public String getSubtitle() {
     return subtitle;
   }
 
   public void setSubtitle(String subtitle) {
     this.subtitle = subtitle;
   }
 
   public HashMap<String, String> getStyles() {
     return styles;
   }
 }
