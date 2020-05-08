 package org.pentaho.chart;
 
 import org.pentaho.chart.core.ChartDocument;
 import org.pentaho.chart.model.ChartModel.ChartTheme;
 
 public interface ChartThemeFactory {
  ChartDocument getThemeDocument(ChartTheme chartTheme);
 }
