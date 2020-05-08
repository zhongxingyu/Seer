 package com.ingemark.requestage.plugin.ui;
 
 import static com.ingemark.requestage.Message.EXCEPTION;
 import static com.ingemark.requestage.Util.gridData;
 import static com.ingemark.requestage.Util.showView;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.EVT_ERROR;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.EVT_INIT_HIST;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.EVT_REPORT;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.EVT_RUN_SCRIPT;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.REQUESTAGE_VIEW_ID;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.STATS_EVTYPE_BASE;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.requestAgePlugin;
 import static com.ingemark.requestage.plugin.ui.HistogramViewer.DESIRED_HEIGHT;
 import static com.ingemark.requestage.plugin.ui.HistogramViewer.minDesiredWidth;
 import static java.lang.Math.max;
 import static java.lang.Math.min;
 import static java.lang.Math.signum;
 import static org.slf4j.LoggerFactory.getLogger;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Scale;
 import org.eclipse.ui.part.ViewPart;
 import org.slf4j.Logger;
 
 import com.ingemark.requestage.DialogInfo;
 import com.ingemark.requestage.IStressTestServer;
 import com.ingemark.requestage.Message;
 import com.ingemark.requestage.Stats;
 import com.ingemark.requestage.StressTestServer;
 
 public class RequestAgeView extends ViewPart
 {
   static final Logger log = getLogger(RequestAgeView.class);
   private static final int MIN_THROTTLE = 10, MAX_THROTTLE = 135, THROTTLE_SCALE_FACTOR = 3;
   private static final Runnable DO_NOTHING = new Runnable() { public void run() {} };
   public static RequestAgeView requestAgeView;
   public Composite statsParent;
   private volatile IStressTestServer testServer = StressTestServer.NULL;
   volatile History[] histories = {};
   private Composite viewParent;
   private ProgressDialog pd;
   private Scale throttle;
   private Action stopAction, reportAction;
 
   public void createPartControl(Composite p) {
     this.viewParent = new Composite(p, SWT.NONE);
     final Color colWhite = p.getDisplay().getSystemColor(SWT.COLOR_WHITE);
     viewParent.setBackground(colWhite);
     requestAgeView = this;
     viewParent.setLayout(new GridLayout(2, false));
     stopAction = new Action() { public void run() { shutdownAndNewStatsParent(); } };
     stopAction.setImageDescriptor(requestAgePlugin().imageDescriptor("stop.gif"));
     stopAction.setDisabledImageDescriptor(requestAgePlugin().imageDescriptor("stop_disabled.gif"));
     reportAction = new Action() { public void run() {
       statsParent.notifyListeners(EVT_REPORT, null);
     }};
     reportAction.setImageDescriptor(requestAgePlugin().imageDescriptor("report.gif"));
     enableActions(false);
     final IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
     toolbar.add(stopAction);
     toolbar.add(reportAction);
     throttle = new Scale(viewParent, SWT.VERTICAL);
     throttle.setBackground(colWhite);
     throttle.setMinimum(MIN_THROTTLE);
     throttle.setMaximum(MAX_THROTTLE);
     throttle.addSelectionListener(new SelectionAdapter() {
       @Override public void widgetSelected(SelectionEvent e) { applyThrottle(); }
     });
     newStatsParent();
   }
 
   public History[] histories() { return histories; }
 
   private void enableActions(boolean state) {
     stopAction.setEnabled(state);
     reportAction.setEnabled(state);
   }
 
   void newStatsParent() {
     if (statsParent != null) statsParent.dispose();
     statsParent = new Composite(viewParent, SWT.NONE);
     gridData().grab(true, true).applyTo(statsParent);
     final GridLayout l = new GridLayout(2, false);
     l.marginHeight = l.marginWidth = 0;
     final GridLayout statsParentLayout = l;
     statsParent.setLayout(statsParentLayout);
     statsParent.addListener(EVT_RUN_SCRIPT, new Listener() {
       public void handleEvent(final Event event) {
         try {
           pd = new ProgressDialog("Starting Stress Test", 203, new Runnable() {
             @Override public void run() { shutdownAndThen(DO_NOTHING); }
           });
           newStatsParent();
           statsParent.addListener(EVT_INIT_HIST, new Listener() {
             @Override public void handleEvent(Event event) {
               log.debug("Init histogram");
               final int size = (Integer)event.data;
               histories = new History[size];
               final HistogramViewer[] hists = new HistogramViewer[size];
               for (int i = 0; i < size; i++) {
                 final HistogramViewer histogram = hists[i] = new HistogramViewer(statsParent);
                 final History history = histories[i] = new History();
                 gridData().grab(true, true).applyTo(histogram.canvas);
                 statsParent.addListener(STATS_EVTYPE_BASE + i, new Listener() {
                   public void handleEvent(Event e) {
                     final Stats stats = (Stats) e.data;
                     histogram.statsUpdate(stats);
                     history.statsUpdate(stats);
                   }
                 });
                 histogram.canvas.addMouseListener(new MouseListener() {
                   @Override public void mouseDoubleClick(MouseEvent e) {
                     testServer.send(new Message(EXCEPTION, histogram.stats.name));
                   }
                   @Override public void mouseUp(MouseEvent e) {}
                   @Override public void mouseDown(MouseEvent e) {}
                 });
               }
               statsParent.addListener(EVT_REPORT, new Listener() {
                 @Override public void handleEvent(Event event) {
                   final List<Stats> statsList = new ArrayList<Stats>();
                   for (HistogramViewer hist : hists) statsList.add(hist.stats);
                   ReportDialog.show(testServer.testName(), statsList);
                 }
               });
               statsParent.addControlListener(new ControlListener() {
                 @Override public void controlResized(ControlEvent e) {
                   final Rectangle bounds = statsParent.getBounds();
                   final int
                     availRows = max(1, bounds.height/DESIRED_HEIGHT),
                     maxCols = size/availRows + (int)signum(size % availRows),
                     desiredCols = max(1, min(maxCols, bounds.width / minDesiredWidth));
                   if (desiredCols == statsParentLayout.numColumns) return;
                   statsParentLayout.numColumns = desiredCols;
                   statsParent.setLayout(statsParentLayout);
                 }
                 @Override public void controlMoved(ControlEvent e) {}
               });
               throttle.setSelection(MIN_THROTTLE);
               applyThrottle();
              showView(REQUESTAGE_VIEW_ID);
               Display.getCurrent().asyncExec(new Runnable() { public void run() {
                viewParent.layout(true);
                viewParent.redraw();
               }});
           }});
           statsParent.addListener(EVT_ERROR, new Listener() {
             @Override public void handleEvent(Event e) {
               enableActions(false);
               if (pd != null) pd.close();
               InfoDialog.show(new DialogInfo("Stress testing error", ((String)e.data)));
             }
           });
           shutdownAndThen(new Runnable() { public void run() {
             testServer = new StressTestServer(statsParent, (String)event.data)
               .progressMonitor(pd.pm());
             testServer.start();
             enableActions(true);
           }});
         }
         catch (Throwable t) {
           if (pd != null) pd.close();
           InfoDialog.show(new DialogInfo("Stress test init error", t));
         }
       }});
   }
 
   public void shutdownAndNewStatsParent() {
     newStatsParent();
     pd = new ProgressDialog("Shutting down Stress Test", 15, DO_NOTHING).cancelable(false);
     shutdownAndThen(new Runnable() { public void run() { pd.pm().done(); }});
   }
 
   private void shutdownAndThen(Runnable andThen) {
     testServer.progressMonitor(pd != null? pd.pm() : null);
     enableActions(false);
     final IStressTestServer ts = testServer;
     testServer = StressTestServer.NULL;
     ts.shutdown(andThen);
   }
 
   static String joinPath(String[] ps) {
     final StringBuilder b = new StringBuilder(128);
     for (String p : ps) b.append(p).append(":");
     return b.toString();
   }
 
   static int pow(int in) { return (int)Math.pow(10, in/100d); }
 
   @Override public void dispose() { shutdownAndThen(DO_NOTHING);}
 
   @Override public void setFocus() { throttle.setFocus(); }
 
   private void applyThrottle() {
     testServer.intensity(pow(THROTTLE_SCALE_FACTOR*throttle.getSelection()));
   }
 }
