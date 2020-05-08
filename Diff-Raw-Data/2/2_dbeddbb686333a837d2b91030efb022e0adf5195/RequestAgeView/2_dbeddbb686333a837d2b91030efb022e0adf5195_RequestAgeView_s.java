 package com.ingemark.perftest.plugin.ui;
 
 import static com.ingemark.perftest.Message.EXCEPTION;
 import static com.ingemark.perftest.Util.gridData;
 import static com.ingemark.perftest.Util.sneakyThrow;
 import static com.ingemark.perftest.plugin.StressTestPlugin.EVT_ERROR;
 import static com.ingemark.perftest.plugin.StressTestPlugin.EVT_INIT_HIST;
 import static com.ingemark.perftest.plugin.StressTestPlugin.EVT_RUN_SCRIPT;
 import static com.ingemark.perftest.plugin.StressTestPlugin.STATS_EVTYPE_BASE;
 import static com.ingemark.perftest.plugin.StressTestPlugin.STRESSTEST_VIEW_ID;
 import static com.ingemark.perftest.plugin.StressTestPlugin.stressTestPlugin;
 import static com.ingemark.perftest.plugin.ui.HistogramViewer.DESIRED_HEIGHT;
 import static com.ingemark.perftest.plugin.ui.HistogramViewer.minDesiredWidth;
 import static java.lang.Math.max;
 import static java.lang.Math.min;
 import static java.lang.Math.signum;
 import static org.eclipse.ui.PlatformUI.getWorkbench;
 import static org.slf4j.LoggerFactory.getLogger;
 
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.resource.ImageDescriptor;
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
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Scale;
 import org.eclipse.ui.part.ViewPart;
 import org.slf4j.Logger;
 
 import com.ingemark.perftest.DialogInfo;
 import com.ingemark.perftest.IStressTestServer;
 import com.ingemark.perftest.Message;
 import com.ingemark.perftest.Stats;
 import com.ingemark.perftest.StressTestServer;
 
 public class RequestAgeView extends ViewPart
 {
   static final Logger log = getLogger(RequestAgeView.class);
   private static final int MIN_THROTTLE = 10, MAX_THROTTLE = 100, THROTTLE_SCALE_FACTOR = 3;
   private static final Runnable DO_NOTHING = new Runnable() { public void run() {} };
   public static RequestAgeView instance;
   public Composite statsParent;
   private volatile IStressTestServer testServer = StressTestServer.NULL;
   private Composite viewParent;
   private ProgressDialog pd;
   private Scale throttle;
   private Action stopAction;
 
   public void createPartControl(Composite p) {
     this.viewParent = new Composite(p, SWT.NONE);
     final Color colWhite = p.getDisplay().getSystemColor(SWT.COLOR_WHITE);
     viewParent.setBackground(colWhite);
     instance = this;
     viewParent.setLayout(new GridLayout(2, false));
     stopAction = new Action() {
       final ImageDescriptor img = stressTestPlugin().imageDescriptor("stop.gif");
       @Override public ImageDescriptor getImageDescriptor() { return img; }
       @Override public void run() { shutdownAndNewStatsParent(); }
     };
     stopAction.setEnabled(false);
     getViewSite().getActionBars().getToolBarManager().add(stopAction);
     throttle = new Scale(viewParent, SWT.VERTICAL);
//    throttle.setBackground(colWhite);
     throttle.setMinimum(MIN_THROTTLE);
     throttle.setMaximum(MAX_THROTTLE);
     throttle.addSelectionListener(new SelectionAdapter() {
       @Override public void widgetSelected(SelectionEvent e) { applyThrottle(); }
     });
     newStatsParent();
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
               throttle.setSelection(MIN_THROTTLE);
               applyThrottle();
               final List<Integer> indices = (List<Integer>)event.data;
               Collections.sort(indices);
               for (int i : indices) {
                 final HistogramViewer histogram = new HistogramViewer(statsParent);
                 gridData().grab(true, true).applyTo(histogram.canvas);
                 statsParent.addListener(STATS_EVTYPE_BASE + i, new Listener() {
                   public void handleEvent(Event e) { histogram.statsUpdate((Stats) e.data); }
                 });
                 histogram.canvas.addMouseListener(new MouseListener() {
                   @Override public void mouseDoubleClick(MouseEvent e) {
                     testServer.send(new Message(EXCEPTION, histogram.stats.name));
                   }
                   @Override public void mouseUp(MouseEvent e) {}
                   @Override public void mouseDown(MouseEvent e) {}
                 });
               }
               statsParent.addControlListener(new ControlListener() {
                 @Override public void controlResized(ControlEvent e) {
                   final Rectangle bounds = statsParent.getBounds();
                   final int
                     availRows = max(1, bounds.height/DESIRED_HEIGHT),
                     maxCols = indices.size()/availRows + (int)signum(indices.size() % availRows),
                     desiredCols = max(1, min(maxCols, bounds.width / minDesiredWidth));
                   if (desiredCols == statsParentLayout.numColumns) return;
                   statsParentLayout.numColumns = desiredCols;
                   statsParent.setLayout(statsParentLayout);
                 }
                 @Override public void controlMoved(ControlEvent e) {}
               });
               viewParent.layout(true);
               statsParent.notifyListeners(SWT.Resize, new Event());
               viewParent.notifyListeners(SWT.Paint, new Event());
               show();
           }});
           statsParent.addListener(EVT_ERROR, new Listener() {
             @Override public void handleEvent(Event e) {
               if (pd != null) pd.close();
               stopAction.setEnabled(false);
               InfoDialog.show(new DialogInfo("Stress testing error", ((Throwable)e.data)));
             }
           });
           shutdownAndThen(new Runnable() { @Override public void run() {
             testServer = new StressTestServer(statsParent, (String)event.data)
               .progressMonitor(pd.pm());
             testServer.start();
             stopAction.setEnabled(true);
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
     shutdownAndThen(new Runnable() {@Override public void run() { pd.pm().done(); }});
   }
 
   private void shutdownAndThen(Runnable andThen) {
     testServer.progressMonitor(pd.pm());
     stopAction.setEnabled(false);
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
 
   public static void show() {
     try {
       getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(STRESSTEST_VIEW_ID);
     } catch (CoreException e) { sneakyThrow(e); }
   }
 }
