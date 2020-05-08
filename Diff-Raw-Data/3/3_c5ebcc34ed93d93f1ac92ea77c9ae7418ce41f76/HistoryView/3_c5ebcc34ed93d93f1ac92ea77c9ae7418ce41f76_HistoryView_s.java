 package com.ingemark.requestage.plugin.ui;
 
 import static com.ingemark.requestage.Util.color;
 import static com.ingemark.requestage.Util.gridData;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.EVT_HISTORY_UPDATE;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.EVT_INIT_HIST;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.globalEventHub;
 import static com.ingemark.requestage.plugin.ui.RequestAgeView.requestAgeView;
 import static java.util.concurrent.TimeUnit.MINUTES;
 import static org.eclipse.swt.SWT.FILL;
 import static org.swtchart.ISeries.SeriesType.LINE;
 
 import java.util.Date;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.part.ViewPart;
 import org.swtchart.Chart;
 import org.swtchart.IAxis;
 import org.swtchart.IAxisSet;
 import org.swtchart.ILineSeries;
 import org.swtchart.ILineSeries.PlotSymbolType;
 import org.swtchart.ISeries;
 import org.swtchart.ISeriesSet;
 import org.swtchart.ITitle;
 import org.swtchart.Range;
 
 public class HistoryView extends ViewPart implements Listener
 {
   private static final long MIN_HIST_RANGE = MINUTES.toMillis(2);
   private static final int[] colors = { SWT.COLOR_BLUE, SWT.COLOR_GREEN, SWT.COLOR_RED,
     SWT.COLOR_CYAN, SWT.COLOR_MAGENTA, SWT.COLOR_YELLOW };
   public static final String[] yTitles = {"1/resp_time", "pending_reqs", "reqs_per_sec"};
   private final Color gridColor = new Color(Display.getCurrent(), 240, 240, 240);
   private int color;
   private Chart chart;
   private String histKey;
 
   @Override public void createPartControl(Composite parent) {
     final Display disp = parent.getDisplay();
     parent.setLayout(new GridLayout(2, false));
     chart = new Chart(parent, SWT.NONE);
     chart.setBackground(disp.getSystemColor(SWT.COLOR_WHITE));
     gridData().align(FILL, FILL).grab(true, true).applyTo(chart);
     final Group radios = new Group(parent, SWT.NONE);
     radios.setLayout(new GridLayout(1,false));
     boolean selected = true;
     for (int i = 0; i < History.keys.length; i++) {
       final String key = History.keys[i], title = yTitles[i];
       final Button radio = new Button(radios, SWT.RADIO);
       gridData().align(FILL, FILL).grab(true, true).applyTo(radio);
      radio.setBackground(color(SWT.COLOR_WHITE));
       radio.setText(title);
       radio.addSelectionListener(new SelectionListener() {
         @Override public void widgetSelected(SelectionEvent e) {
           histKey = key;
           chart.getAxisSet().getYAxis(0).getTitle().setText(title);
           pullHistories();
         }
         @Override public void widgetDefaultSelected(SelectionEvent e) {}
       });
       radio.setSelection(selected);
       if (selected) radio.notifyListeners(SWT.Selection, null);
       selected = false;
     }
 
     chart.getTitle().setVisible(false);
     final IAxisSet axes = chart.getAxisSet();
     final IAxis y = axes.getYAxis(0);
     y.getTick().setForeground(color(SWT.COLOR_BLACK));
     y.enableLogScale(true);
     final ITitle yTitle = y.getTitle();
     yTitle.setFont(disp.getSystemFont());
     yTitle.setForeground(color(SWT.COLOR_BLACK));
     y.getGrid().setForeground(gridColor);
     final IAxis x = axes.getXAxis(0);
     x.getTick().setForeground(color(SWT.COLOR_BLACK));
     x.getTitle().setVisible(false);
     x.getGrid().setForeground(gridColor);
     globalEventHub().addListener(EVT_INIT_HIST, this);
     globalEventHub().addListener(EVT_HISTORY_UPDATE, this);
   }
 
   @Override public void handleEvent(Event event) {
     switch (event.type) {
     case EVT_INIT_HIST:
       final ISeriesSet ss = chart.getSeriesSet();
       for (ISeries s : ss.getSeries()) ss.deleteSeries(s.getId());
       color = 0;
       break;
     case EVT_HISTORY_UPDATE:
       update((History) event.data);
       break;
     }
   }
 
   void pullHistories() {
     if (requestAgeView == null) return;
     for (History h : requestAgeView.histories) update(h);
   }
 
   private void update(History h) {
     if (h.name == null) return;
     final ISeriesSet ss = chart.getSeriesSet();
     ILineSeries ser = (ILineSeries) ss.getSeries(h.name);
     if (ser == null) {
       ser = (ILineSeries) ss.createSeries(LINE, h.name);
       ser.setLineColor(color(colors[color++ % colors.length]));
     }
     ser.setSymbolType(PlotSymbolType.NONE);
     ser.setYSeries(h.history(histKey));
     final Date[] xs = h.timestamps();
     ser.setXDateSeries(xs);
     final IAxisSet axes = chart.getAxisSet();
     axes.getYAxis(0).adjustRange();
     final IAxis x = axes.getXAxis(0);
     if (xs.length == 0 || xs[xs.length-1].getTime()-xs[0].getTime() >= MIN_HIST_RANGE)
       x.adjustRange();
     else {
       final long start = xs[0].getTime();
       x.setRange(new Range(start, start + MIN_HIST_RANGE));
     }
     chart.redraw();
   }
 
   @Override public void setFocus() { }
 
   @Override public void dispose() {
     globalEventHub().removeListener(EVT_INIT_HIST, this);
     globalEventHub().removeListener(EVT_HISTORY_UPDATE, this);
   }
 
   public static void main(String[] args) {
     final Display d = Display.getDefault();
     final Shell s = new Shell(d);
     s.setLayout(new GridLayout(1,true));
     new HistoryView().createPartControl(s);
     s.open();
     while (!s.isDisposed()) if (!d.readAndDispatch()) d.sleep();
     d.dispose();
   }
 }
