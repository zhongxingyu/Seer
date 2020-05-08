 package com.ingemark.perftest.plugin.ui;
 
 import static com.ingemark.perftest.StressTester.HIST_SIZE;
 import static com.ingemark.perftest.StressTester.TIMESLOTS_PER_SEC;
 import static com.ingemark.perftest.Util.now;
 import static java.lang.Math.log10;
 import static java.lang.Math.max;
 import static java.lang.Math.min;
 import static java.lang.Math.pow;
 import static org.eclipse.core.runtime.Platform.OS_MACOSX;
 import static org.eclipse.core.runtime.Platform.getOS;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.FontMetrics;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.graphics.Region;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 
 import com.ingemark.perftest.Stats;
 
 public class HistogramViewer implements PaintListener
 {
   private static final int FULL_TOTAL_BAR_HEIGHT = 100;
   static final int
     HIST_HEIGHT_SCALE = 1, HIST_BAR_WIDTH = 1, HIST_XOFFSET = 50, METER_SCALE = 50,
     TOTAL_REQS_OFFSET = HIST_XOFFSET + HIST_SIZE*HIST_BAR_WIDTH;
   final int histYoffset;
   Stats stats = new Stats();
   final Canvas canvas;
   GC ownGc;
   GC gc;
   Region clipping = new Region();
   boolean numbersWillBePrinted;
   long numbersLastPrinted;
 
   HistogramViewer(Composite parent) {
     canvas = new Canvas(parent, SWT.NONE);
     canvas.setBackground(color(SWT.COLOR_WHITE));
     canvas.addPaintListener(this);
     ownGc = new GC(canvas);
     histYoffset = 10 + ownGc.getFontMetrics().getHeight();
     // On OS X the overhead of GC create/dispose is huge; so reuse one GC
     if (!getOS().equals(OS_MACOSX)) {
       ownGc.dispose();
       ownGc = null;
     }
   }
 
   void statsUpdate(Stats stats) {
     final boolean printName = !this.stats.name.equals(stats.name);
     this.stats = stats;
     final long now = now();
     numbersWillBePrinted = now-numbersLastPrinted > 200_000_000;
     try {
       gc = ownGc != null? ownGc : new GC(canvas);
       if (printName) printChartName();
       drawStats();
     } finally { if (ownGc == null) gc.dispose(); }
     gc = null;
     if (numbersWillBePrinted) {
       numbersLastPrinted = now;
       // OSX hack: under certain conditions the screen doesn't update until GC is disposed
       if (ownGc != null) {
         ownGc.dispose();
         ownGc = new GC(canvas);
        gc.setClipping(clipping);
       }
     }
   }
 
   @Override public void paintControl(PaintEvent e) {
     gc = e.gc;
     printChartName();
     gc.setClipping(clipping);
     final Rectangle area = canvas.getClientArea();
     paintRect(SWT.COLOR_WHITE, area.x, area.y, area.width, area.height);
     numbersWillBePrinted = true;
     drawStaticStuff(15);
     drawStats();
     gc = null;
   }
 
   void drawStats() {
     gc.setClipping(clipping);
     paintMeterBars();
     paintHistogram();
     paintTotalReqs();
   }
 
   void paintMeterBars() {
     paintBar(SWT.COLOR_WHITE, 8, histYoffset, 5, 0, Integer.MAX_VALUE);
     final Point maxReqsPerSecExtent = gc.stringExtent("9999");
     if (numbersWillBePrinted) {
       paintRect(SWT.COLOR_WHITE, 0, 0, maxReqsPerSecExtent.x, 2+maxReqsPerSecExtent.y);
       printString(String.valueOf(stats.reqsPerSec), 2, 0);
     }
     int a = toMeter(stats.reqsPerSec), b = toMeter(stats.succRespPerSec + stats.failsPerSec);
     paintBar(SWT.COLOR_DARK_BLUE, 0, histYoffset, 5, a, Integer.MAX_VALUE);
     final int color;
     if (b < a) {
       int tmp = a; a = b; b = tmp;
       color = SWT.COLOR_RED;
     } else color = SWT.COLOR_DARK_GREEN;
     paintBar(color, 8, histYoffset+a, 5, b-a, b-a);
     final int failsHeight = toMeter(stats.failsPerSec);
     paintBar(SWT.COLOR_RED, 8, histYoffset, 5, failsHeight, failsHeight);
   }
 
   void paintHistogram() {
     final char[] hist = stats.histogram;
     int i;
     for (i = 0; i < hist.length; i++) histBar(SWT.COLOR_BLUE, i, 1, hist[i]);
     histBar(SWT.COLOR_BLUE, i, (HIST_SIZE-i), 0);
   }
 
   void paintTotalReqs() {
     final int
       maxTotalBars = 1 +
         (canvas.getClientArea().width - HIST_XOFFSET - HIST_SIZE*HIST_BAR_WIDTH) / HIST_BAR_WIDTH,
     fullTotalBars = min(stats.pendingReqs/FULL_TOTAL_BAR_HEIGHT, maxTotalBars),
     lastTotalBarHeight = stats.pendingReqs%FULL_TOTAL_BAR_HEIGHT;
     final int totalBarColor = SWT.COLOR_GRAY;
     totalBar(totalBarColor, HIST_SIZE, fullTotalBars, FULL_TOTAL_BAR_HEIGHT);
     totalBar(totalBarColor, HIST_SIZE+ fullTotalBars, 1, lastTotalBarHeight);
     totalBar(totalBarColor, HIST_SIZE+ fullTotalBars+ 1, maxTotalBars-fullTotalBars-1, 0);
     if (numbersWillBePrinted) {
       final String s = String.valueOf(stats.pendingReqs);
       final int totalReqsCenter = TOTAL_REQS_OFFSET + ((fullTotalBars+1)*HIST_BAR_WIDTH) / 2;
       final Point ext = gc.stringExtent(s);
       final int labelBase = histYoffset+FULL_TOTAL_BAR_HEIGHT+5;
       paintRect(SWT.COLOR_WHITE,
           TOTAL_REQS_OFFSET, labelBase, maxTotalBars*HIST_BAR_WIDTH, ext.y);
       printString(s, max(TOTAL_REQS_OFFSET, totalReqsCenter - ext.x/2), labelBase);
     }
   }
 
   void printChartName() {
     final Point ext = gc.stringExtent(stats.name);
     final Point p = printString(stats.name,
         HIST_XOFFSET + max((HIST_SIZE*HIST_BAR_WIDTH-ext.x)/2, 0),
         5 + tickMarkY(3, 2), true);
     clipping.dispose();
     clipping = new Region();
     gc.getClipping(clipping);
     clipping.subtract(new Rectangle(p.x, p.y, ext.x, ext.y));
     gc.setClipping(clipping);
   }
 
   void drawStaticStuff(int xOffset) {
     final FontMetrics m = gc.getFontMetrics();
     final int labelOffset = m.getAscent()/2 - 1;
     loop: for (int exp = 0;; exp++)
       for (int i = 2; i <= 10; i += 2) {
         final int label = (int)pow(10, exp)*i;
         if (label >= 3000) break loop;
         final int y = histYoffset + tickMarkY(exp, i);
         drawHorLine(SWT.COLOR_BLACK, xOffset, y, 5);
         if (i == 2 || i == 10)
           printString(String.valueOf(label), 8+xOffset, y-labelOffset);
       }
     for (int i = 0;; i++) {
       final int barIndex = i*TIMESLOTS_PER_SEC;
       final int xPos = HIST_XOFFSET + barIndex*HIST_BAR_WIDTH;
       drawVerLine(SWT.COLOR_BLACK, xPos, m.getHeight()+3, 5);
       final String label = String.valueOf(i);
       final Point ext = gc.stringExtent(label);
       printString(label, xPos - ext.x/2, 2);
       if (barIndex >= HIST_SIZE) break;
     }
   }
 
   Point printString(String s, int x, int y) {
     return printString(s, x, y, false);
   }
   Point printString(String s, int x, int y, boolean fitHeight) {
     gc.setForeground(color(SWT.COLOR_BLACK));
     gc.setBackground(color(SWT.COLOR_WHITE));
     final FontMetrics m = gc.getFontMetrics();
     y = canvas.getClientArea().height-m.getLeading()-m.getAscent()-y;
     if (fitHeight) y = max(y, 0);
     gc.drawString(s, x, y);
     return new Point(x, y);
   }
   void drawHorLine(int color, int x, int y, int len) {
     gc.setForeground(color(color));
     final int height = canvas.getClientArea().height;
     gc.drawLine(x, height-y, x+len-1, height-y);
   }
   void drawVerLine(int color, int x, int y, int len) {
     gc.setForeground(color(color));
     final int height = canvas.getClientArea().height;
     gc.drawLine(x, max(0, height-y-1), x, max(0, height-y-len));
   }
   void paintRect(int color, int x, int y, int width, int height) {
     if (height == 0 || width == 0) return;
     if (width == 1) {drawVerLine(color, x, y, height); return;}
     if (height == 1) {drawHorLine(color, x, y, width); return;}
     final Rectangle area = canvas.getClientArea();
     gc.setBackground(color(color));
     height = min(height, area.height-y);
     gc.fillRectangle(x, max(0, area.height-y-height), width, height);
   }
   void paintBar(int color, int x, int y, int width, int height, int maxHeight) {
     paintRect(color, x, y, width, height);
     paintRect(SWT.COLOR_WHITE, x, y+height, width, maxHeight-height);
   }
   void histBar(int color, int pos, int barCount, int barHeight) {
     paintBar(color, HIST_XOFFSET+pos*HIST_BAR_WIDTH, histYoffset,
         barCount*HIST_BAR_WIDTH, barHeight*HIST_HEIGHT_SCALE, Integer.MAX_VALUE);
   }
   void totalBar(int color, int pos, int barCount, int barHeight) {
     paintBar(color, HIST_XOFFSET+pos*HIST_BAR_WIDTH, histYoffset,
         barCount*HIST_BAR_WIDTH, barHeight*HIST_HEIGHT_SCALE,
         FULL_TOTAL_BAR_HEIGHT*HIST_HEIGHT_SCALE);
   }
   Color color(int id) { return Display.getCurrent().getSystemColor(id); }
 
   static int toMeter(int in) { return (int) (METER_SCALE * max(0d, log10(in))); }
 
   static int tickMarkY(int exp, int i) { return (int) (METER_SCALE * (exp + log10(i))); }
 }
