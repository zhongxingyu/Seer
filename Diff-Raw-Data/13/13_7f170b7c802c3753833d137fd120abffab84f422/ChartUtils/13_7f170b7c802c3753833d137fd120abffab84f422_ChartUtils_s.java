 /**
  * 
  */
 package uk.ac.imperial.dws04.Presage2Experiments.Analysis;
 
 import java.awt.Color;
 
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.LogarithmicAxis;
 import org.jfree.chart.plot.CategoryPlot;
 import org.jfree.chart.plot.Plot;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.data.Range;
 import org.jfree.data.xy.XYSeriesCollection;
 import org.jfree.ui.tabbedui.VerticalLayout;
 
 /**
  * @author dws04
  *
  */
 public abstract class ChartUtils {
 	
	public static void tweak(JFreeChart chart, Boolean removeLegend, Boolean resize){
 		Class plotClass = chart.getPlot().getClass();
 		final Plot plot = chart.getPlot();
 	    plot.setBackgroundPaint(Color.white);
 		if (plotClass.isAssignableFrom(XYPlot.class)) {
 			final XYPlot xyPlot = (XYPlot)plot;
 		    xyPlot.setDomainGridlinePaint(Color.lightGray);
 		    xyPlot.setRangeGridlinePaint(Color.lightGray);
 			if (resize) {
 				Range range = ((XYSeriesCollection)(xyPlot.getDataset())).getRangeBounds(false);
 				xyPlot.getRangeAxis().setRange(range);
 			}
 			
 		}
 		else if (plotClass.isAssignableFrom(CategoryPlot.class)) {
 			final CategoryPlot cPlot = (CategoryPlot)plot;
 			cPlot.setDomainGridlinePaint(Color.lightGray);
 			cPlot.setRangeGridlinePaint(Color.lightGray);
 		}
		chart.getLegend().setVisible(!removeLegend);
 	}
 
 	public static void removeLegendForBAWPlots(Chart chart) {
		if (chart.getClass().isAssignableFrom(DefaultBoxAndWhiskerChart.class)) {
			chart.getChart().removeLegend();
 		}	
 	}
 	
 	public static void makeLogRange(TimeSeriesChart chart) {
 		XYPlot plot = chart.getXYPlot();
 		LogarithmicAxis logAxis = new LogarithmicAxis(plot.getRangeAxis().getLabel());
 		logAxis.setAutoRange(true);
 		logAxis.setAllowNegativesFlag(true);
 		plot.setRangeAxis(logAxis);
 	}
 
 }
