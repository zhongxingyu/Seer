 package com.ingemark.requestage.plugin.ui;
 
 import static com.ingemark.requestage.Util.color;
 import static com.ingemark.requestage.Util.gridData;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.EVT_HISTORY_UPDATE;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.EVT_INIT_HIST;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.globalEventHub;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.okButton;
 import static com.ingemark.requestage.plugin.ui.RequestAgeView.requestAgeView;
 import static java.util.concurrent.TimeUnit.MINUTES;
 import static org.eclipse.swt.SWT.CENTER;
 import static org.eclipse.swt.SWT.FILL;
 import static org.swtchart.ISeries.SeriesType.LINE;
 
 import java.util.Date;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
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
 import org.swtchart.LineStyle;
 import org.swtchart.Range;
 
 import com.ingemark.requestage.InitInfo;
 import com.ingemark.requestage.plugin.ui.History.RespTimeHistory;
 
 public class HistoryView extends ViewPart implements Listener
 {
   private static final long MIN_HIST_RANGE = MINUTES.toMillis(2);
   private static final int[] colors = { SWT.COLOR_BLUE, SWT.COLOR_RED,
     SWT.COLOR_MAGENTA, SWT.COLOR_DARK_BLUE, SWT.COLOR_DARK_RED, SWT.COLOR_GREEN,
     SWT.COLOR_DARK_GREEN, SWT.COLOR_DARK_CYAN, SWT.COLOR_DARK_MAGENTA, SWT.COLOR_DARK_YELLOW,
     SWT.COLOR_GRAY, SWT.COLOR_CYAN, SWT.COLOR_BLACK, SWT.COLOR_YELLOW };
   private static final String RESP_SCATTER_TITLE = "resp_time_scatter";
   public static final String[] yTitles = {"pending_reqs","resp_time","reqs/sec","fails/sec"};
  private static final double[] DUMMY_SERIES = {1}, EMPTY_SERIES = {};
   private final Color gridColor = new Color(Display.getCurrent(), 240, 240, 240);
   private long start;
   private Chart chart;
   private Composite radios;
   private String histKey;
   private Composite chooser;
   private boolean chooserShown;
 
   @Override public void createPartControl(Composite parent) {
     final Display disp = parent.getDisplay();
     parent.setLayout(new GridLayout(1, false));
     parent.setBackground(color(SWT.COLOR_WHITE));
     radios = new Composite(parent, SWT.NONE);
     gridData().align(CENTER, FILL).applyTo(radios);
     radios.setBackground(parent.getBackground());
     radios.setLayout(new RowLayout());
     chart = new Chart(parent, SWT.NONE);
     chart.setBackground(disp.getSystemColor(SWT.COLOR_WHITE));
     gridData().align(FILL, FILL).grab(true, true).applyTo(chart);
     boolean selected = true;
     for (int i = 0; i < History.keys.length; i++) {
       final String key = History.keys[i], title = yTitles[i];
       final Button radio = newRadio(key, title);
       radio.setSelection(selected);
       if (selected) radio.notifyListeners(SWT.Selection, null);
       selected = false;
     }
     newRadio(RESP_SCATTER_TITLE, RESP_SCATTER_TITLE);
     newSeriesChooser(disp);
 
     chart.getTitle().setVisible(false);
     final IAxisSet axes = chart.getAxisSet();
     final IAxis y = axes.getYAxis(0);
     y.getTick().setForeground(color(SWT.COLOR_BLACK));
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
 
   private void newSeriesChooser(final Display d) {
     final Shell s = new Shell(d, SWT.TITLE | SWT.ON_TOP);
     s.setText("Choose visible data series");
     s.setBackground(d.getSystemColor(SWT.COLOR_WHITE));
     s.setLayout(new GridLayout(1, false));
     chooser = new Composite(s, SWT.NONE);
     chooser.setBackground(d.getSystemColor(SWT.COLOR_WHITE));
     chooser.setLayout(new RowLayout(SWT.VERTICAL));
     okButton(s, false);
     s.pack();
     final Button choose = new Button(radios, SWT.PUSH);
     choose.setText("Choose series");
     choose.addSelectionListener(new SelectionListener() {
       @Override public void widgetSelected(SelectionEvent e) {
         if (!chooserShown) {
           chooserShown = true;
           s.setLocation(d.map(choose, null, 0, 0));
         }
         s.setVisible(!s.isVisible());
       }
       @Override public void widgetDefaultSelected(SelectionEvent e) {}
     });
   }
 
   private Button newRadio(final String key, final String title) {
     final Button radio = new Button(radios, SWT.RADIO);
     radio.setBackground(radios.getBackground());
     radio.setText(title);
     radio.addSelectionListener(new SelectionListener() {
       @Override public void widgetSelected(SelectionEvent e) {
         histKey = key;
         chart.getAxisSet().getYAxis(0).getTitle().setText(title);
        for (ISeries s : chart.getSeriesSet().getSeries()) {
          s.setYSeries(DUMMY_SERIES); s.setYSeries(EMPTY_SERIES);
        }
         if (requestAgeView != null)
           for (History h : requestAgeView.histories) update(h);
       }
       @Override public void widgetDefaultSelected(SelectionEvent e) {}
     });
     return radio;
   }
 
   @Override public void handleEvent(Event event) {
     switch (event.type) {
     case EVT_INIT_HIST:
       final ISeriesSet ss = chart.getSeriesSet();
       for (ISeries s : ss.getSeries()) ss.deleteSeries(s.getId());
       for (Control c : chooser.getChildren()) c.dispose();
       int color = 0;
       start = System.currentTimeMillis();
      for (final String name : ((InitInfo) event.data).reqNames) {
         final ILineSeries ser = (ILineSeries) ss.createSeries(LINE, name);
         final Color c = color(colors[color++ % colors.length]);
         ser.setLineColor(c);
         ser.setSymbolColor(c);
         final Button check = new Button(chooser, SWT.CHECK);
         check.setBackground(check.getDisplay().getSystemColor(SWT.COLOR_WHITE));
         check.setText(name);
         check.setSelection(true);
         check.addSelectionListener(new SelectionListener() {
           @Override public void widgetSelected(SelectionEvent e) {
             ss.getSeries(name).setVisible(check.getSelection());
           }
           @Override public void widgetDefaultSelected(SelectionEvent e) {}
         });
         chooser.getParent().pack();
       }
       break;
     case EVT_HISTORY_UPDATE:
       update((History) event.data);
       break;
     }
   }
 
   private void update(History h) {
     final String name = h.name;
     if (name == null) return;
     final ISeriesSet ss = chart.getSeriesSet();
     final ILineSeries ser = (ILineSeries) ss.getSeries(name);
     final Date[] xs;
     final long maxTimestamp;
     if (histKey.equals(RESP_SCATTER_TITLE)) {
       ser.setLineStyle(LineStyle.NONE);
       ser.setSymbolType(PlotSymbolType.CIRCLE);
       ser.setSymbolSize(2);
       final RespTimeHistory hist = h.respTimeHistory();
       xs = hist.timestamps;
       maxTimestamp = hist.maxTimestamp;
       ser.setYSeries(hist.times);
      chart.getAxisSet().getYAxis(0).enableLogScale(true);
     } else {
       ser.setLineStyle(LineStyle.SOLID);
       ser.setSymbolType(PlotSymbolType.NONE);
       xs = h.timestamps();
       maxTimestamp = xs.length > 0? xs[xs.length-1].getTime() : 0;
       ser.setYSeries(h.history(histKey));
       chart.getAxisSet().getYAxis(0).enableLogScale(false);
     }
     ser.setXDateSeries(xs);
     final IAxisSet axes = chart.getAxisSet();
     axes.getYAxis(0).adjustRange();
     final IAxis x = axes.getXAxis(0);
     if (maxTimestamp-start >= MIN_HIST_RANGE) x.adjustRange();
     else x.setRange(new Range(start, start + MIN_HIST_RANGE));
     chart.redraw();
   }
 
   @Override public void setFocus() { radios.setFocus(); }
 
   @Override public void dispose() {
     globalEventHub().removeListener(EVT_INIT_HIST, this);
     globalEventHub().removeListener(EVT_HISTORY_UPDATE, this);
     super.dispose();
   }
 }
