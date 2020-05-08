 package com.ingemark.requestage.plugin.ui;
 
 import static com.ingemark.requestage.Util.DIVS_PER_DECADE;
 import static com.ingemark.requestage.Util.color;
 import static com.ingemark.requestage.Util.now;
 import static com.ingemark.requestage.plugin.ui.RequestAgeView.gridColor;
 import static java.lang.Math.max;
 import static java.util.concurrent.TimeUnit.SECONDS;
 import gnu.trove.map.hash.TIntIntHashMap;
 import gnu.trove.procedure.TIntIntProcedure;
 import gnu.trove.procedure.TLongObjectProcedure;
 
 import java.util.Arrays;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.swtchart.Chart;
 import org.swtchart.IAxis;
 import org.swtchart.IAxisSet;
 import org.swtchart.ILineSeries;
 import org.swtchart.ILineSeries.PlotSymbolType;
 import org.swtchart.ISeries;
 import org.swtchart.ISeries.SeriesType;
 import org.swtchart.ISeriesSet;
 import org.swtchart.ITitle;
 import org.swtchart.Range;
 
 import com.ingemark.requestage.Stats;
 
 public class RespDistributionViewer
 {
   private static final long REFRESH_INTERVAL = SECONDS.toNanos(2);
   private static final double CHART_BOTTOM = 0.01;
   Chart chart;
   private long[] dist = {};
   private double[] xSeries = {};
   private long totalCount, lastUpdate;
   boolean cumulative;
   private boolean dirty;
 
   private final TLongObjectProcedure<TIntIntHashMap> mergeIntoDistribution =
       new TLongObjectProcedure<TIntIntHashMap>() {
         @Override public boolean execute(long start, TIntIntHashMap times) {
           times.forEachEntry(new TIntIntProcedure() {
             @Override public boolean execute(int encodedElapsedMillis, int count) {
               ensureCapacity(encodedElapsedMillis);
               dist[encodedElapsedMillis] += count;
               totalCount += count;
               return true;
           }});
           return true;
         }
   };
 
   RespDistributionViewer(String name, Composite parent) {
     final Display disp = parent.getDisplay();
     chart = new Chart(parent, SWT.NONE);
     final Color black = color(SWT.COLOR_BLACK), white = disp.getSystemColor(SWT.COLOR_WHITE);
     chart.setBackground(white);
     chart.getLegend().setVisible(false);
     formatTitle(chart.getTitle());
     chart.getTitle().setText(name);
     final IAxisSet axes = chart.getAxisSet();
     for (IAxis axis : new IAxis[] {axes.getXAxis(0), axes.getYAxis(0)}) {
       axis.getTick().setForeground(black);
       final ITitle title = axis.getTitle();
       formatTitle(title);
       axis.getGrid().setForeground(gridColor);
     }
     axes.getXAxis(0).getTitle().setText("log(resp_time)");
     axes.getYAxis(0).setRange(new Range(CHART_BOTTOM, 100));
     final ISeriesSet ss = chart.getSeriesSet();
     final ILineSeries ser = (ILineSeries) ss.createSeries(SeriesType.LINE, "elapsedMillisDist");
     ser.setSymbolType(PlotSymbolType.NONE);
     ser.enableArea(true);
     ensureCapacity(60);
     chart.addPaintListener(new PaintListener() {
      @Override public void paintControl(PaintEvent e) {
        if (dirty && now() - lastUpdate > REFRESH_INTERVAL) updateChart();
      }});
   }
 
   private void formatTitle(ITitle title) {
     title.setFont(Display.getCurrent().getSystemFont());
     title.setForeground(color(SWT.COLOR_BLACK));
   }
 
   void statsUpdate(Stats stats) {
     dirty = true;
     stats.respHistory.forEachEntry(mergeIntoDistribution);
   }
 
   void setCumulative(boolean cumulative) {
     if (this.cumulative == cumulative) return;
     this.cumulative = cumulative;
     chart.getAxisSet().getYAxis(0).getTitle().setText(cumulative ? "% > x" : "%");
     updateChart();
   }
 
   private void updateChart() {
     dirty = false;
     lastUpdate = now();
     final double[] ySeries = new double[dist.length];
     final double dblTotalCount = totalCount;
     if (cumulative) {
       long remaining = totalCount;
       for (int i = 0; i < ySeries.length; i++) {
         ySeries[i] = max(CHART_BOTTOM, 100L*remaining/dblTotalCount);
         remaining -= dist[i];
       }
     } else for (int i = 0; i < ySeries.length; i++)
       ySeries[i] = max(CHART_BOTTOM, 100L*dist[i]/dblTotalCount);
     chart.getSeriesSet().getSeries()[0].setYSeries(ySeries);
     chart.getAxisSet().getYAxis(0).enableLogScale(true);
     adjustXSeries();
    chart.redraw();
   }
 
   private void ensureCapacity(int requestedIndex) {
     if (requestedIndex >= dist.length)
       dist = Arrays.copyOf(dist, (requestedIndex/DIVS_PER_DECADE + 1) * DIVS_PER_DECADE);
   }
 
   private void adjustXSeries() {
     final ISeries ser = chart.getSeriesSet().getSeries()[0];
     if (xSeries.length >= dist.length) return;
     xSeries = new double[dist.length];
     for (int i = 0; i < xSeries.length; i++) xSeries[i] = i/(double)DIVS_PER_DECADE - 3;
     ser.setXSeries(xSeries);
     chart.getAxisSet().getXAxis(0).setRange(new Range(xSeries[0], xSeries[xSeries.length-1]));
   }
 }
 
