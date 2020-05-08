 package org.swtchart.examples.ext;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
 import org.eclipse.ui.part.ViewPart;
import org.swtchart.Chart;
 import org.swtchart.IBarSeries;
 import org.swtchart.ILineSeries;
 import org.swtchart.ISeries.SeriesType;
 import org.swtchart.ext.InteractiveChart;
 
 /**
  * An example view to show InteractiveChart.
  */
 public class InteractiveChartExample extends ViewPart {
 
     private static final String[] categorySeries = { "Mon", "Tue", "Wed",
             "Thu", "Fri" };
     private static final double[] yLineSeries1 = { 4.6, 5.4, 6.9, 5.6, 7.1 };
     private static final double[] yLineSeries2 = { 6.0, 5.1, 4.9, 5.3, 4.2 };
     private static final double[] yBarSeries1 = { 1.1, 2.9, 3.3, 4.4, 3.5 };
     private static final double[] yBarSeries2 = { 4.3, 3.4, 2.8, 2.1, 1.9 };
 
     /** the chart */
     private InteractiveChart chart;
 
     /*
      * @see WorkbenchPart#createPartControl(Composite)
      */
     @Override
     public void createPartControl(Composite parent) {
         parent.setLayout(new FillLayout());
 
         // create an interactive chart
         chart = new InteractiveChart(parent, SWT.NONE);
 
         // set title
         chart.getTitle().setText("Sample Interactive Chart");
 
         // set category series
         chart.getAxisSet().getXAxis(0).enableCategory(true);
         chart.getAxisSet().getXAxis(0).setCategorySeries(categorySeries);
 
         // create line series 1
         ILineSeries lineSeries1 = (ILineSeries) chart.getSeriesSet()
                 .createSeries(SeriesType.LINE, "line series 1");
         lineSeries1.setYSeries(yLineSeries1);
 
         // create line series 2
         ILineSeries lineSeries2 = (ILineSeries) chart.getSeriesSet()
                 .createSeries(SeriesType.LINE, "line series 2");
         lineSeries2.setYSeries(yLineSeries2);
         lineSeries2.setLineColor(Display.getDefault().getSystemColor(SWT.COLOR_RED));
 
         // create bar series 1
         IBarSeries barSeries1 = (IBarSeries) chart.getSeriesSet().createSeries(
                 SeriesType.BAR, "bar series 1");
         barSeries1.setYSeries(yBarSeries1);
 
         // create bar series 2
         IBarSeries barSeries2 = (IBarSeries) chart.getSeriesSet().createSeries(
                 SeriesType.BAR, "bar series 2");
         barSeries2.setYSeries(yBarSeries2);
         barSeries2.setBarColor(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
 
         // adjust the axis range
         chart.getAxisSet().adjustRange();
 
         chart.getPlotArea().addMouseListener(new MouseListener() {
 
 			private boolean toolTipEnable = true;
 
         	@Override
 			public void mouseUp(MouseEvent e) {}
 
 			@Override
 			public void mouseDown(MouseEvent e) {}
 
 			@Override
 			public void mouseDoubleClick(MouseEvent e) {
 				toolTipEnable = chart.enableDataPilot(toolTipEnable);
 			}
 		});
     }
 
     /*
      * @see WorkbenchPart#setFocus()
      */
     @Override
     public void setFocus() {
         chart.getPlotArea().setFocus();
     }
 
     /*
      * @see WorkbenchPart#dispose()
      */
     @Override
     public void dispose() {
         super.dispose();
         chart.dispose();
     }
 }
