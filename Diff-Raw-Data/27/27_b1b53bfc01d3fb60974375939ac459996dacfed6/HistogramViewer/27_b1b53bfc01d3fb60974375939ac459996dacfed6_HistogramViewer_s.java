 package com.ingemark.requestage.plugin.ui;
 
 import static com.ingemark.requestage.StressTester.HIST_SIZE;
 import static com.ingemark.requestage.StressTester.TIMESLOTS_PER_SEC;
 import static com.ingemark.requestage.Util.color;
 import static com.ingemark.requestage.Util.now;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.threeDigitFormat;
 import static java.lang.Math.log10;
 import static java.lang.Math.max;
 import static java.lang.Math.min;
 import static java.lang.Math.pow;
 import static java.util.concurrent.TimeUnit.MILLISECONDS;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.FontMetrics;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 
 import com.ingemark.requestage.Stats;
 
 public class HistogramViewer implements PaintListener
 {
   private static final int
     TICKMARK_LEN = 5,
     TICKMARK_X = 15,
     HIST_BAR_WIDTH = 1,
     HIST_XOFFSET = 50,
     TOTAL_REQS_OFFSET = HIST_XOFFSET + HIST_SIZE*HIST_BAR_WIDTH,
     FULL_TOTAL_BAR_HEIGHT = 100,
     HIST_HEIGHT_SCALE = 1,
     METER_SCALE = 50;
   static final int DESIRED_HEIGHT = 285;
   static int minDesiredWidth;
   private static int histYoffset;
   final Canvas canvas;
   Stats stats = new Stats();
   private final Color colReqBar, colRespPlusBar, colRespMinusBar, colFailBar, colHist, colTotalReq;
   private GC gc;
   private Image backdrop;
   private long numbersLastUpdated;
   private int printedReqsPerSec, printedPendingReqs;
 
   HistogramViewer(Composite parent) {
     final Display d = Display.getCurrent();
     colReqBar = new Color(d, 12, 12, 240);
     colRespPlusBar = new Color(d, 0, 170, 12);
     colRespMinusBar = new Color(d, 255, 204, 0);
     colFailBar = color(SWT.COLOR_RED);
     colHist = color(SWT.COLOR_BLUE);
     colTotalReq = new Color(d, 200, 170, 170);
     canvas = new Canvas(parent, SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED);
     canvas.addPaintListener(this);
     canvas.addListener(SWT.Resize, new Listener() { public void handleEvent(Event event) {
       recreateBackdrop();
     }});
     if (minDesiredWidth == 0) {
       final GC gc = new GC(canvas);
       final FontMetrics fm = gc.getFontMetrics();
       histYoffset = 10 + fm.getHeight();
       minDesiredWidth = TOTAL_REQS_OFFSET + 6*fm.getAverageCharWidth();
       gc.dispose();
     }
   }
 
   private void recreateBackdrop() {
     final Rectangle area = canvas.getClientArea();
     if (backdrop != null) backdrop.dispose();
     backdrop = new Image(Display.getCurrent(), area);
     gc = new GC(backdrop);
     paintRect(color(SWT.COLOR_WHITE), area.x, area.y, area.width, area.height);
     final FontMetrics m = gc.getFontMetrics();
     final int labelOffset = m.getAscent()/2 - 1;
     loop: for (int exp = 0;; exp++)
       for (int i = 2; i <= 10; i += 2) {
         final int label = (int)pow(10, exp)*i;
         final int y = histYoffset + tickMarkY(exp, i);
         if (y >= area.height + 1) break loop;
         drawHorLine(color(SWT.COLOR_BLACK), TICKMARK_X, y, TICKMARK_LEN);
         if (i == 10) printString(threeDigitFormat(label, false), 8+TICKMARK_X, y-labelOffset);
       }
     for (int i = 0;; i++) {
       final int barIndex = i*TIMESLOTS_PER_SEC;
       final int xPos = HIST_XOFFSET + barIndex*HIST_BAR_WIDTH;
       drawVerLine(color(SWT.COLOR_BLACK), xPos, m.getHeight()+3, 5);
       if (i % 2 == 0) {
         final String label = String.valueOf(i);
         final Point ext = gc.stringExtent(label);
         printString(label, xPos - ext.x/2, 2);
       }
       if (barIndex >= HIST_SIZE) break;
     }
     gc.dispose();
     gc = null;
   }
 
   void statsUpdate(Stats stats) {
     final long now = now();
     if (now-numbersLastUpdated > MILLISECONDS.toNanos(200)) {
       numbersLastUpdated = now;
       printedReqsPerSec = stats.reqsPerSec;
       printedPendingReqs = stats.pendingReqs;
     }
     this.stats = stats;
   }
 
   @Override public void paintControl(PaintEvent e) {
     gc = e.gc;
     gc.drawImage(backdrop, 0, 0);
     paintRespTime();
     paintMeterBars();
     paintHistogram();
     paintTotalReqs();
     printChartName();
     gc = null;
   }
 
   private void printChartName() {
     final Point ext = gc.stringExtent(stats.name);
     printString(stats.name,
         HIST_XOFFSET + max((HIST_SIZE*HIST_BAR_WIDTH-ext.x)/2, 0),
         5 + tickMarkY(3, 9), true);
   }
 
   private void paintHistogram() {
     final char[] hist = stats.histogram;
     int i;
     for (i = 0; i < hist.length; i++) paintBar(colHist, i, 1, hist[i]);
   }
 
   private void paintRespTime() {
     final int avg = toMeter(stats.avgServIntensty),
               highBound = toMeter(stats.avgServIntensty + stats.stdevServIntensty),
               lowBound = toMeter(stats.avgServIntensty - stats.stdevServIntensty);
     paintRect(80, color(SWT.COLOR_GRAY),
         TICKMARK_X, histYoffset+lowBound, TICKMARK_LEN, highBound-lowBound);
     drawHorLine(color(SWT.COLOR_BLUE), TICKMARK_X, histYoffset+avg, TICKMARK_LEN);
     drawHorLine(color(SWT.COLOR_BLUE), TICKMARK_X, histYoffset+avg+1, TICKMARK_LEN);
   }
 
   private void paintMeterBars() {
     printString(threeDigitFormat(printedReqsPerSec, false), 2, 0);
     int a = toMeter(stats.reqsPerSec), b = toMeter(stats.succRespPerSec + stats.failsPerSec);
     paintRect(colReqBar, 0, histYoffset, 5, a);
     final Color color;
     if (b < a) {
       final int tmp = a; a = b; b = tmp;
       color = colRespMinusBar;
     } else color = colRespPlusBar;
     paintRect(color, 8, histYoffset+a, 5, b-a);
     final int failsHeight = toMeter(stats.failsPerSec);
     paintRect(colFailBar, 8, histYoffset, 5, failsHeight);
   }
 
   private void paintTotalReqs() {
     final int
       maxTotalBars = 1 +
         (canvas.getClientArea().width - TOTAL_REQS_OFFSET) / HIST_BAR_WIDTH,
     fullTotalBars = min(stats.pendingReqs/FULL_TOTAL_BAR_HEIGHT, maxTotalBars),
     lastTotalBarHeight = stats.pendingReqs%FULL_TOTAL_BAR_HEIGHT;
     paintBar(colTotalReq, HIST_SIZE, fullTotalBars, FULL_TOTAL_BAR_HEIGHT);
     paintBar(colTotalReq, HIST_SIZE+ fullTotalBars, 1, lastTotalBarHeight);
     final String s = threeDigitFormat(printedPendingReqs, false);
     final int totalReqsCenter = TOTAL_REQS_OFFSET + ((fullTotalBars+1)*HIST_BAR_WIDTH) / 2;
     final Point ext = gc.stringExtent(s);
     final int labelBase = histYoffset+FULL_TOTAL_BAR_HEIGHT+5;
     printString(s, max(TOTAL_REQS_OFFSET, totalReqsCenter - ext.x/2), labelBase);
   }
 
   private Point printString(String s, int x, int y) {
     return printString(s, x, y, false);
   }
   private Point printString(String s, int x, int y, boolean fitHeight) {
     gc.setForeground(color(SWT.COLOR_BLACK));
     gc.setBackground(color(SWT.COLOR_WHITE));
     final FontMetrics m = gc.getFontMetrics();
     y = histHeight()-m.getLeading()-m.getAscent()-y;
     if (fitHeight) y = max(y, 0);
     gc.drawString(s, x, y);
     return new Point(x, y);
   }
   private void drawHorLine(Color color, int x, int y, int len) {
     gc.setForeground(color);
     final int height = histHeight();
     gc.drawLine(x, height-y, x+len-1, height-y);
   }
   private void drawVerLine(Color color, int x, int y, int len) {
     gc.setForeground(color);
     final int height = histHeight();
     gc.drawLine(x, max(0, height-y-1), x, max(0, height-y-len));
   }
   private void paintRect(Color color, int x, int y, int width, int height) {
     paintRect(255, color, x, y, width, height);
   }
   private void paintRect(int alpha, Color color, int x, int y, int width, int height) {
     if (height == 0 || width == 0) return;
     gc.setAlpha(alpha);
     try {
       if (width == 1) {drawVerLine(color, x, y, height); return;}
       if (height == 1) {drawHorLine(color, x, y, width); return;}
       final int histHeight = histHeight();
       gc.setBackground(color);
       height = min(height, histHeight-y);
       gc.fillRectangle(x, max(0, histHeight-y-height), width, height);
     } finally { gc.setAlpha(255); }
   }
   private void paintBar(Color color, int pos, int barCount, int barHeight) {
     paintRect(color, HIST_XOFFSET+pos*HIST_BAR_WIDTH, histYoffset,
         barCount*HIST_BAR_WIDTH, barHeight*HIST_HEIGHT_SCALE);
   }
   private int histHeight() { return Math.min(DESIRED_HEIGHT, canvas.getClientArea().height); }
   private static int toMeter(int in) { return (int) (METER_SCALE * max(0d, log10(in))); }
   private static int toMeter(double d) { return toMeter((int)min(Integer.MAX_VALUE, max(0, d))); }
   private static int tickMarkY(int exp, int i) { return (int) (METER_SCALE * (exp + log10(i))); }
 }
