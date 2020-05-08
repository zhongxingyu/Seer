 /*
  * Signal Visualization Tools for Make Sense Platform
  * Copyright (C) 2012 Robert Moore
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *  
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *  
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package org.makesense.sigvis.panels;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Composite;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.RenderingHints;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.font.FontRenderContext;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.GeneralPath;
 import java.awt.geom.Line2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 
 import org.makesense.sigvis.DataCache2;
 import org.makesense.sigvis.ImageResources;
 import org.makesense.sigvis.DataCache2.ValueType;
 import org.makesense.sigvis.structs.ChartItem;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class LineChart extends JComponent implements DisplayPanel,
     MouseListener, MouseMotionListener, MouseWheelListener {
 
   private static final Logger log = LoggerFactory.getLogger(LineChart.class);
 
   protected static final long MAX_TIME_GAP = 2000l;
 
   protected static final int DEFAULT_MARGIN = 20;
 
   protected static final int MARGIN_TOP = 0;
   protected static final int MARGIN_RIGHT = 1;
   protected static final int MARGIN_BOTTOM = 2;
   protected static final int MARGIN_LEFT = 3;
 
   protected int[] margins = { DEFAULT_MARGIN * 2, DEFAULT_MARGIN,
       DEFAULT_MARGIN * 2, DEFAULT_MARGIN * 2 };
 
   // protected int maxElements = 100;
 
   // Default to 30 seconds so as not to draw too much
   protected long maxAge = 30000;
 
   // Minimum of 10 seconds
   protected static final long MIN_DISPLAY_AGE = 10000;
 
   protected static final long DISPAY_AGE_STEP = 5000;
 
   public long getMaxAge() {
     return maxAge;
   }
 
   public void setMaxAge(long maxAge) {
     this.maxAge = maxAge;
    if (this.timeOffset < 0) {
      this.timeOffset = 0;
    } else if (this.timeOffset > (this.cache.getMaxCacheAge() - this.maxAge)) {
      this.timeOffset = this.cache.getMaxCacheAge() - this.maxAge;
    }
   }
 
   protected float maxValue = 1f;
 
   protected float currValMax = 1f;
 
   protected float minValue = 0f;
 
   protected boolean scrolling = true;
 
   protected long nextMinCheck = System.currentTimeMillis();
 
   protected long lastRepaint = 0l;
 
   protected boolean selfAdjustMax = false;
 
   protected boolean drawPoints = true;
 
   protected boolean displayLegend = false;
 
   protected float legendHeight = 0;
 
   protected boolean drawHorizontalLines = true;
 
   protected boolean drawVerticalLines = true;
 
   protected Color axisTextColor = Color.LIGHT_GRAY;
 
   protected Color backgroundColor = Color.BLACK;
 
   protected Color chartGridColor = Color.DARK_GRAY;
 
   protected Color borderColor = Color.LIGHT_GRAY;
 
   protected boolean enableAntiAliasing = true;
 
   protected boolean useTransparency = false;
 
   protected float minFps = 8;
 
   protected float currFps = 30f;
 
   protected int slowFrames = 0;
 
   protected Composite fillUnderAlpha = AlphaComposite.getInstance(
       AlphaComposite.SRC_OVER, 0.2f);
 
   protected String lastStream = null;
 
   protected long highlightTime = 2000l;
 
   protected float yAdjustValue = 0f;
 
   protected float adjustInterval = 1.0f;
 
   protected BufferedImage downArrowImg = null;
 
   protected BufferedImage upArrowImg = null;
 
   protected BufferedImage playImg = null;
 
   protected BufferedImage pauseImg = null;
 
   protected boolean amRaiseMaxY = false;
 
   protected boolean amLowerMaxY = false;
 
   protected DataCache2 cache;
 
   protected String displayedId = null;
 
   protected boolean deviceIsTransmitter = false;
 
   protected ValueType type;
 
   public float getMinFps() {
     return this.minFps;
   }
 
   public void setMinFps(float minFps) {
     this.minFps = minFps;
   }
 
   @Override
   public boolean isAntiAlias() {
     return enableAntiAliasing;
   }
 
   @Override
   public void setAntiAlias(boolean enableAntiAliasing) {
     this.enableAntiAliasing = enableAntiAliasing;
   }
 
   public boolean isLegendDisplayed() {
     return displayLegend;
   }
 
   public void setDisplayLegend(boolean drawLegend) {
     this.displayLegend = drawLegend;
     if (!this.displayLegend) {
       this.legendHeight = 0f;
     }
   }
 
   public boolean isSelfAdjustMax() {
     return selfAdjustMax;
   }
 
   public void setSelfAdjustMax(boolean selfAdjustMax) {
     this.selfAdjustMax = selfAdjustMax;
   }
 
   protected boolean selfAdjustMin = false;
 
   public boolean isScrolling() {
     return scrolling;
   }
 
   public void setScrolling(boolean scrolling) {
     this.scrolling = scrolling;
   }
 
   public LineChart(final ValueType type, final DataCache2 cache) {
     super();
     this.cache = cache;
     this.type = type;
     this.playImg = ImageResources.IMG_PLAY;
     this.pauseImg = ImageResources.IMG_PAUSE;
     this.upArrowImg = ImageResources.IMG_UP_ARROW;
     this.downArrowImg = ImageResources.IMG_DOWN_ARROW;
     // TODO: Uncomment the below to support dragging x-axis (time)
     // this.addMouseMotionListener(this);
     this.addMouseListener(this);
     this.addMouseWheelListener(this);
     if (this.cache.isClone()) {
       this.scrolling = false;
       this.lastRepaint = this.cache.getCreationTs();
     } else {
       this.lastRepaint = System.currentTimeMillis();
     }
 
     this.setDoubleBuffered(true);
 
   }
 
   public void clear() {
     // TODO: Anything to do here?
   }
 
   @Override
   public void paintComponent(Graphics graphics) {
     super.paintComponent(graphics);
 
     if (this.scrolling) {
       this.lastRepaint = System.currentTimeMillis();
     }
 
     Graphics2D g2 = (Graphics2D) graphics;
     if (this.enableAntiAliasing) {
       g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
           RenderingHints.VALUE_ANTIALIAS_ON);
     }
 
     int screenWidth = this.getWidth();
     int screenHeight = this.getHeight();
 
     Color origColor = g2.getColor();
     Composite origComposite = g2.getComposite();
 
     this.drawBackground(g2, screenWidth, screenHeight);
 
     if (this.displayedId == null) {
       return;
     }
 
     List<String> devices = this.deviceIsTransmitter ? this.cache
         .getReceiverIds() : this.cache.getFiduciaryTransmitterIds();
     if (devices == null) {
       log.warn("No complimentary devices for {}", this.displayedId);
       return;
     }
     Collections.sort(devices);
 
     if (this.displayLegend) {
       this.drawLegend(g2, devices, screenWidth, screenHeight);
     }
 
     g2.setColor(Color.DARK_GRAY);
     this.drawChartGrid(g2, screenWidth, screenHeight,
         (float) (this.maxAge / 10.0), (this.maxValue - this.minValue) / 10f);
 
     String[] streamIdArray = devices.toArray(new String[] {});
     Color drawColor = Color.BLUE;
     int streamNum = 0;
     float newMax = this.minValue;
     for (String streamId : streamIdArray) {
       float adjusted = 0.9f * streamNum / streamIdArray.length;
       float hue = adjusted;
       float sat = 0.95f;
       float bright = 0.95f;
       drawColor = new Color(Color.HSBtoRGB(hue, sat, bright));
       g2.setColor(drawColor);
 
       // TODO: Grab variance list instead
       Collection<ChartItem<Float>> sampleList = this.generateDisplayedData(
           this.deviceIsTransmitter ? this.displayedId : streamId,
           this.deviceIsTransmitter ? streamId: this.displayedId);
 
       ++streamNum;
       if (sampleList == null || sampleList.isEmpty()) {
 
         continue;
       }
 
       float tempMax = this.deviceIsTransmitter ? this.drawStream(g2, streamId,
           this.displayedId, sampleList, screenWidth, screenHeight) : this
           .drawStream(g2, this.displayedId, streamId, sampleList, screenWidth,
               screenHeight);
       if (tempMax > newMax) {
         newMax = tempMax;
       }
     }
 
     if (this.selfAdjustMax) {
       this.adjustMaxY(newMax, this.maxValue - this.minValue);
     }
 
     this.drawAdjustInfo(g2, screenWidth, screenHeight);
     this.drawPauseInfo(g2, screenWidth, screenHeight);
 
     // if (highlightStream != null) {
     // g2.setColor(Color.WHITE);
     // this.drawStream(g2, highlightStream, screenWidth, screenHeight);
     // }
 
     g2.setColor(Color.LIGHT_GRAY);
     this.drawChartBorders(g2, screenWidth, screenHeight);
     this.drawStatsValues(g2, screenWidth, screenHeight);
     this.drawTimestamp(g2, screenWidth, screenHeight);
     g2.setColor(origColor);
     g2.setComposite(origComposite);
 
     long renderTime = System.currentTimeMillis() - this.lastRepaint;
     this.currFps = this.currFps * 0.875f + (1000f / renderTime) * 0.125f;
 
     // if (this.enableAntiAliasing && (this.currFps < this.minFps * 0.9f)) {
     // ++this.slowFrames;
     // if (this.slowFrames > 3) {
     // this.enableAntiAliasing = false;
     // log.warn("FPS: {} Disabling Anti-Aliasing.", this.currFps);
     // }
     // } else if (this.enableAntiAliasing) {
     // this.slowFrames = 0;
     // }
   }
 
   protected Collection<ChartItem<Float>> generateDisplayedData(String txer,
       String rxer) {
     if (this.type == ValueType.RSSI) {
       return this.cache.getRssiList(rxer, txer);
     } else if (this.type == ValueType.VARIANCE) {
       return this.cache.getVarianceList(rxer, txer);
     }
     return null;
   }
   
   protected void drawBackground(Graphics2D g, int screenWidth, int screenHeight){
     g.setColor(Color.BLACK);
     g.fillRect(0, 0, screenWidth, screenHeight);
   }
 
   protected void drawAdjustInfo(Graphics g, int screenWidth, int screenHeight) {
     if (this.amLowerMaxY && this.downArrowImg != null) {
       g.drawImage(this.downArrowImg, screenWidth - this.margins[MARGIN_RIGHT],
           this.margins[MARGIN_TOP] + (int) this.legendHeight + 5
               + this.margins[MARGIN_RIGHT], screenWidth,
           this.margins[MARGIN_TOP] + (int) this.legendHeight + 5 + 2
               * this.margins[MARGIN_RIGHT], 0, 0, this.downArrowImg.getWidth(),
           this.downArrowImg.getHeight(), null);
     } else if (this.amRaiseMaxY && this.upArrowImg != null) {
       g.drawImage(this.upArrowImg, screenWidth - this.margins[MARGIN_RIGHT],
           this.margins[MARGIN_TOP] + (int) this.legendHeight + 5
               + this.margins[MARGIN_RIGHT], screenWidth,
           this.margins[MARGIN_TOP] + (int) this.legendHeight + 5 + 2
               * this.margins[MARGIN_RIGHT], 0, 0, this.upArrowImg.getWidth(),
           this.upArrowImg.getHeight(), null);
     }
   }
 
   protected void drawPauseInfo(Graphics g, int screenWidth, int screenHeight) {
     if (this.scrolling && this.playImg != null) {
       g.drawImage(this.playImg, screenWidth - this.margins[MARGIN_RIGHT],
           this.margins[MARGIN_TOP] + (int) this.legendHeight, screenWidth,
           this.margins[MARGIN_TOP] + (int) this.legendHeight
               + this.margins[MARGIN_RIGHT], 0, 0, this.playImg.getWidth(),
           this.playImg.getHeight(), null);
     } else if (this.pauseImg != null) {
       g.drawImage(this.pauseImg, screenWidth - this.margins[MARGIN_RIGHT],
           this.margins[MARGIN_TOP] + (int) this.legendHeight, screenWidth,
           this.margins[MARGIN_TOP] + (int) this.legendHeight
               + this.margins[MARGIN_RIGHT], 0, 0, this.pauseImg.getWidth(),
           this.pauseImg.getHeight(), null);
     }
   }
 
   protected void adjustMaxY(float tempMax, float yRange) {
     float adjustBy = 0f;
     // Check to see if we need to adjust...
     if (tempMax < (this.maxValue - yRange * 0.15)) {
       float diff = this.maxValue - tempMax;
 
       if (this.yAdjustValue < diff) {
         this.yAdjustValue = diff;
       }
 
       // If not adjusting, then set up the adjustment interval
       // if (!this.amLowerMaxY) {
       // this.yAdjustValue = this.maxYValue - tempMax;
       this.amLowerMaxY = true;
       this.amRaiseMaxY = false;
       // }
       adjustBy = this.yAdjustValue * (1f / (this.adjustInterval * this.minFps));
 
       if (adjustBy > diff) {
         adjustBy = diff;
       }
       this.maxValue -= adjustBy;
       if (this.maxValue < this.minValue + 1) {
         this.maxValue = this.minValue + 1;
       }
     } else if (tempMax > this.maxValue) {
       float diff = tempMax * 1.1f - this.maxValue;
 
       if (diff > this.yAdjustValue) {
         this.yAdjustValue = diff;
       }
       //
       // if(!this.amRaiseMaxY){
       // this.yAdjustValue = tempMax*1.1f - this.maxYValue;
       this.amRaiseMaxY = true;
       this.amLowerMaxY = false;
       // }
       adjustBy = this.yAdjustValue * (1f / (this.adjustInterval * this.minFps));
 
       if (adjustBy > diff) {
         adjustBy = diff;
       }
       this.maxValue += adjustBy;
     }
 
     else {
       this.yAdjustValue = 0f;
       this.amRaiseMaxY = false;
       this.amLowerMaxY = false;
       return;
     }
 
     /*
      * else if (tempMax > this.maxYValue - yRange * 0.1f) { float increaseBy =
      * (tempMax - this.maxYValue) (1f / this.minFps);
      * log.info("MFPS: {}, Increase by {}", this.minFps, increaseBy);
      * this.maxYValue += increaseBy; }
      */
   }
 
   protected void drawLegend(final Graphics g, final List<String> devices,
       final int screenWidth, final int screenHeight) {
     Graphics2D g2 = (Graphics2D) g;
     if (devices == null && devices.size() == 0) {
       return;
     }
     FontMetrics fontMetrics = g2.getFontMetrics();
 
     float stringHeight = (float) fontMetrics.getStringBounds("TEST", g)
         .getHeight();
 
     float rowHeight = stringHeight + 10;
     ArrayList<Integer> lineBreakIndexes = new ArrayList<Integer>();
 
     // Determine the number of rows and columns
     // Assume a square box for color display at string height, 5px spacing,
     // then text
     // 10px spacing between rows and columns
 
     String[] streamIdArray = devices.toArray(new String[] {});
 
     int streamIndex = 0;
     float availableWidth = screenWidth - this.margins[MARGIN_LEFT]
         - this.margins[MARGIN_RIGHT];
     float currentRowWidth = 0f;
     for (String id : streamIdArray) {
       float entryWidth = fontMetrics.stringWidth(id.toString()) + 10;
       currentRowWidth += entryWidth;
       if (currentRowWidth > availableWidth) {
         lineBreakIndexes.add(streamIndex);
         currentRowWidth = entryWidth;
       }
       ++streamIndex;
     }
 
     streamIndex = 0;
 
     float currentYLine = this.margins[MARGIN_TOP] + stringHeight;
     float currentXStart = this.margins[MARGIN_LEFT];
 
     Color drawColor = Color.BLUE;
     for (String streamId : streamIdArray) {
       if (lineBreakIndexes.contains(streamIndex)) {
         currentYLine += rowHeight;
         currentXStart = this.margins[MARGIN_LEFT];
       }
 
       // Random coloring if nothing is set
       float adjusted = 0.9f * streamIndex / streamIdArray.length;
       float hue = adjusted;
       float sat = 0.95f;
       float bright = Float.NaN;
       if (this.type == ValueType.RSSI) {
         bright = this.deviceIsTransmitter ? this.cache.getRssiAt(
             this.displayedId, streamId, this.timeOffset, this.maxAge)
             : this.cache.getRssiAt(streamId, this.displayedId, this.timeOffset,
                 this.maxAge);
       } else if (this.type == ValueType.VARIANCE) {
         bright = this.deviceIsTransmitter ? this.cache.getVarianceAt(
             this.displayedId, streamId, this.timeOffset, this.maxAge)
             : this.cache.getVarianceAt(streamId, this.displayedId,
                 this.timeOffset, this.maxAge);
       }
 
       if (bright >= this.minValue) {
         bright = 0.95f;
       } else {
         bright = 0.35f;
       }
       drawColor = new Color(Color.HSBtoRGB(hue, sat, bright));
       g2.setColor(drawColor);
       g2.drawString(streamId.toString(), currentXStart, currentYLine);
 
       currentXStart += fontMetrics.stringWidth(streamId.toString()) + 10;
       ++streamIndex;
     }
 
     this.legendHeight = currentYLine - this.margins[MARGIN_TOP] + stringHeight;
   }
 
   protected void drawTimestamp(final Graphics2D g2, int screenWidth,
       int screenHeight) {
     FontRenderContext frc = g2.getFontRenderContext();
     Font currentFont = g2.getFont();
 
     Color origColor = g2.getColor();
     g2.setColor(this.axisTextColor);
     // Draw current timestamp
     long timestamp = this.cache.isClone() ? this.cache.getCreationTs() : System
         .currentTimeMillis();
     timestamp -= this.timeOffset;
 
     String dateString = SimpleDateFormat.getDateTimeInstance(
         SimpleDateFormat.MEDIUM, SimpleDateFormat.MEDIUM).format(
         new Date(timestamp));
     Rectangle2D bounds = currentFont.getStringBounds(dateString, frc);
 
     g2.drawString(dateString, screenWidth - this.margins[MARGIN_RIGHT]
         - (float) bounds.getWidth(),
         this.margins[MARGIN_TOP] - (float) bounds.getHeight() - 2);
     g2.setColor(origColor);
   }
 
   protected float drawStream(final Graphics g, final String rxer,
       final String txer, final Collection<ChartItem<Float>> streamValues,
       final int screenWidth, final int screenHeight) {
 
     if (streamValues == null) {
 
       return this.minValue;
     }
     boolean containsMax = false;
 
     // ChartItem<Float>[] items = streamValues.toArray(new ChartItem[] {});
     if (streamValues.size() == 0) {
 
       return this.minValue;
     }
 
     // long startTime = items[0].getCreationTime();
     // long endTime = items[items.length - 1].getCreationTime();
     //
     // long timeRange = endTime - startTime;
     // if (timeRange == 0)
     // timeRange = 1;
 
     int baseYLevel = screenHeight - this.margins[MARGIN_BOTTOM];
 
     float usableWidth = screenWidth - this.margins[MARGIN_LEFT]
         - this.margins[MARGIN_RIGHT];
 
     float timeScale = usableWidth / this.maxAge;
 
     float valueRange = this.maxValue - this.minValue;
     if (valueRange < 0.01f) {
       valueRange = 1;
     }
 
     float valueScale = (baseYLevel - this.margins[MARGIN_TOP] - this.legendHeight)
         / valueRange;
 
     float itemXLocation = this.margins[MARGIN_LEFT];
     float previousXLocation = -1;
     ChartItem<Float> previousItem = null;
     Graphics2D g2 = (Graphics2D) g;
 
     Composite origComposite = g2.getComposite();
 
     ArrayList<Integer> xValues = new ArrayList<Integer>();
     ArrayList<Integer> yValues = new ArrayList<Integer>();
 
     // g2.setColor(Color.GREEN);
     int itemNumber = 0;
 
     boolean isFirst = true;
 
     float maxValue = this.minValue;
 
     long youngestItem = this.lastRepaint - this.timeOffset;
     if (this.cache.isClone()) {
       youngestItem = this.cache.getCreationTs() - this.timeOffset;
     }
     long oldestItem = youngestItem - this.maxAge;
 
     // long youngestAgeDraw = this.lastRepaint - (long) this.timeOffset;
     // if(this.cache.isClone()){
     // youngestAgeDraw= this.cache.getCreationTs() - (long)this.timeOffset;
     // }
     // long oldestAgeDraw = youngestAgeDraw - this.maxAge;
     long lastItemTime = 0l;
     GeneralPath itemPath = new GeneralPath();
     for (ChartItem<Float> item : streamValues) {
 
       // if (this.scrolling) {
       if (item.getCreationTime() < oldestItem) {
         continue;
       }
       if (item.getCreationTime() > youngestItem) {
         break;
       }
       // System.out.println("Just right!");
       if (item.getValue() > maxValue) {
         maxValue = item.getValue();
       }
       // }
       itemXLocation = screenWidth - this.margins[MARGIN_RIGHT]
           - (youngestItem - item.getCreationTime()) * timeScale;
       float itemYLocation = (baseYLevel - (item.getValue() - this.minValue)
           * valueScale);
 
       if ((item.getCreationTime() - lastItemTime) > MAX_TIME_GAP) {
         isFirst = true;
 
         xValues.add(Integer.valueOf((int) previousXLocation));
         yValues.add(Integer.valueOf((int) baseYLevel));
         previousXLocation = -1;
       }
       lastItemTime = item.getCreationTime();
 
       if (this.useTransparency) {
         if (isFirst) {
 
           isFirst = false;
           xValues.add(Integer.valueOf((int) itemXLocation));
           yValues.add(Integer.valueOf(baseYLevel));
         }
         xValues.add(Integer.valueOf((int) itemXLocation));
         yValues.add(Integer.valueOf((int) itemYLocation));
 
       }
 
       // Draw a line from the previous point
       if (previousXLocation >= 0) {
 
         itemPath.lineTo(itemXLocation,
             (baseYLevel - (item.getValue() - this.minValue) * valueScale));
 
         // g2.drawLine((int) previousXLocation,
         // (int) (baseYLevel - (previousItem.getValue() - this.minValue)
         // * valueScale), (int) itemXLocation,
         // (int) (baseYLevel - (item.getValue() - this.minValue) * valueScale));
       } else {
         itemPath.moveTo(itemXLocation,
             (baseYLevel - (item.getValue() - this.minValue) * valueScale));
       }
 
       // Draw the point if we should
       // if (this.drawPoints) {
       // Ellipse2D point = new Ellipse2D.Float(itemXLocation - 1f, baseYLevel
       // - (item.getValue() - this.minValue) * valueScale - 1f, 2, 2);
       // g2.fill(point);
       // }
 
       // Prepare for next iteration
       previousXLocation = itemXLocation;
 
       previousItem = item;
       ++itemNumber;
     }
 
     g2.draw(itemPath);
 
     if (this.useTransparency && xValues.size() > 0) {
       xValues.add(Integer.valueOf((int) previousXLocation));
       yValues.add(Integer.valueOf(baseYLevel));
 
       Integer[] xIntegers = xValues.toArray(new Integer[] {});
       Integer[] yIntegers = yValues.toArray(new Integer[] {});
 
       int[] xInts = new int[xIntegers.length];
       int[] yInts = new int[yIntegers.length];
       for (int i = 0; i < xIntegers.length; ++i) {
         xInts[i] = xIntegers[i].intValue();
       }
       for (int i = 0; i < yIntegers.length; ++i) {
         yInts[i] = yIntegers[i].intValue();
       }
 
       g2.setComposite(this.fillUnderAlpha);
       g2.fillPolygon(xInts, yInts, xInts.length);
       g2.setComposite(origComposite);
     }
     // if (this.selfAdjustMax
     // && previousItem != null
     // && previousItem.getValue() < (this.maxValue - valueRange * .35f)) {
     //
     // this.adjustMaxY(previousItem.getValue(), valueRange);
     // }
 
     return maxValue;
   }
 
   protected void drawChartBorders(Graphics g, final int screenWidth,
       final int screenHeight) {
     Graphics2D g2 = (Graphics2D) g;
     g2.setColor(this.borderColor);
     // Draw left border
     Line2D.Float borderLine = new Line2D.Float();
     borderLine.setLine(this.margins[MARGIN_LEFT], this.margins[MARGIN_TOP]
         + this.legendHeight, this.margins[MARGIN_LEFT], screenHeight
         - this.margins[MARGIN_BOTTOM]);
     g2.draw(borderLine);
 
     // Draw right border
     borderLine.setLine(screenWidth - this.margins[MARGIN_RIGHT],
         this.margins[MARGIN_TOP] + this.legendHeight, screenWidth
             - this.margins[MARGIN_RIGHT], screenHeight
             - this.margins[MARGIN_BOTTOM]);
     g2.draw(borderLine);
 
     // Draw the bottom border
     borderLine.setLine(this.margins[MARGIN_LEFT], screenHeight
         - this.margins[MARGIN_BOTTOM],
         screenWidth - this.margins[MARGIN_RIGHT], screenHeight
             - this.margins[MARGIN_BOTTOM]);
     g2.draw(borderLine);
     FontMetrics metrics = g2.getFontMetrics();
     g2.drawString("Seconds Ago",
         screenWidth / 2 - metrics.stringWidth("Seconds Ago") / 2,
         screenHeight - 5);
   }
 
   protected void drawChartGrid(Graphics g, final int screenWidth,
       final int screenHeight, float xStep, float yStep) {
     Graphics2D g2 = (Graphics2D) g;
     g2.setColor(this.chartGridColor);
     float xScale = (float) (screenWidth - this.margins[MARGIN_LEFT] - this.margins[MARGIN_RIGHT])
         / (this.maxAge);
     float yScale = (screenHeight - this.margins[MARGIN_TOP]
         - this.margins[MARGIN_BOTTOM] - this.legendHeight)
         / (this.maxValue - this.minValue);
 
     if (this.drawHorizontalLines) {
       float horizontalLine = this.minValue;
       float screenY = 0;
 
       for (; horizontalLine < this.maxValue + 0.01f; horizontalLine += yStep) {
         g2.setColor(this.chartGridColor);
         screenY = this.margins[MARGIN_TOP] + this.legendHeight
             + (this.maxValue - horizontalLine) * yScale;
         g2.drawLine(this.margins[MARGIN_LEFT], (int) screenY, screenWidth
             - this.margins[MARGIN_RIGHT], (int) screenY);
         g2.setColor(this.axisTextColor);
         g2.drawString(String.format("%03.1f", horizontalLine), 0, screenY);
       }
     }
     if (this.drawVerticalLines) {
       long earliestTime = this.lastRepaint - (long) this.timeOffset
           - this.maxAge;
       long verticalLine = earliestTime;
       float screenX = 0;
       for (; verticalLine <= this.lastRepaint; verticalLine += (long) xStep) {
         g2.setColor(this.chartGridColor);
         screenX = screenWidth - this.margins[MARGIN_RIGHT]
             - (verticalLine - earliestTime) * xScale;
         g2.drawLine((int) screenX, screenHeight - this.margins[MARGIN_BOTTOM],
             (int) screenX, (int) (this.margins[MARGIN_TOP] + this.legendHeight));
         g2.setColor(this.axisTextColor);
         g2.drawString(String.format("%d",
             (long) (verticalLine - earliestTime + this.timeOffset) / 1000),
             screenX, screenHeight - this.margins[MARGIN_BOTTOM] / 2f);
       }
     }
   }
 
   protected void drawStatsValues(Graphics g, final int screenWidth,
       final int screenHeight) {
     Graphics2D g2 = (Graphics2D) g;
     // g2.setColor(Color.WHITE);
 
     if (!this.drawHorizontalLines) {
       // Draw the max value
       String valueString = String.format("%03.1f", this.maxValue);
       g2.drawString(valueString, 0, this.margins[MARGIN_TOP]
           + this.legendHeight);
 
       // Draw the min value
       valueString = String.format("%03.1f", this.minValue);
       g2.drawString(valueString, 0,
           (float) (screenHeight - this.margins[MARGIN_BOTTOM]));
     }
   }
 
   protected void drawStatsValues(Graphics g, final int screenWidth,
       final int screenHeight, final ChartItem currItem) {
     Graphics2D g2 = (Graphics2D) g;
     g2.setColor(Color.WHITE);
 
     // Draw the max value
     String valueString = String.format("%03.1f", this.maxValue);
     g2.drawString(valueString, 0, this.margins[MARGIN_TOP] + this.legendHeight);
 
     // Draw the min value
     valueString = String.format("%03.1f", this.minValue);
     g2.drawString(valueString, 0,
         (float) (screenHeight - this.margins[MARGIN_BOTTOM]));
 
     valueString = String.format("%03.1f", currItem.getValue());
     FontMetrics metrics = g.getFontMetrics();
     int fontWidth = metrics.stringWidth(valueString);
 
     // Draw the current value
     g2.drawString(valueString, screenWidth - fontWidth
         - this.margins[MARGIN_RIGHT] / 2, this.margins[MARGIN_TOP]
         + this.legendHeight);
   }
 
   public float getMaxValue() {
     return maxValue;
   }
 
   public void setMaxValue(float maxValue) {
     this.maxValue = maxValue;
   }
 
   public float getMinValue() {
     return minValue;
   }
 
   public void setMinValue(float minValue) {
     this.minValue = minValue;
   }
 
   public boolean isSelfAdjustMin() {
     return selfAdjustMin;
   }
 
   public void setSelfAdjustMin(boolean selfAdjustMin) {
     this.selfAdjustMin = selfAdjustMin;
   }
 
   public boolean isDrawPoints() {
     return drawPoints;
   }
 
   public void setDrawPoints(boolean drawPoints) {
     this.drawPoints = drawPoints;
   }
 
   public boolean isDrawHorizontalLines() {
     return drawHorizontalLines;
   }
 
   public void setDrawHorizontalLines(boolean drawHorizontalLines) {
     this.drawHorizontalLines = drawHorizontalLines;
   }
 
   public boolean isDrawVerticalLines() {
     return drawVerticalLines;
   }
 
   public void setDrawVerticalLines(boolean drawVerticalLines) {
     this.drawVerticalLines = drawVerticalLines;
   }
 
   @Override
   public String getDisplayedId() {
     return displayedId;
   }
 
   public void setDisplayedId(String displayedDevice) {
     this.displayedId = displayedDevice;
   }
 
   public boolean isDeviceTransmitter() {
     return deviceIsTransmitter;
   }
 
   public void setDeviceIsTransmitter(boolean deviceIsTransmitter) {
     this.deviceIsTransmitter = deviceIsTransmitter;
   }
 
   @Override
   public void setDeviceIcon(BufferedImage icon) {
     // TODO Auto-generated method stub
 
   }
 
   protected Point mouseDragStart = null;
 
   protected long timeOffset = 0l;
 
   protected long desiredTimeOffset = 0l;
 
   @Override
   public void mouseDragged(MouseEvent arg0) {
     if (this.mouseDragStart == null) {
       return;
     }
     Point currentPoint = arg0.getPoint();
 
     float usableWidth = this.getWidth() - this.margins[MARGIN_LEFT]
         - this.margins[MARGIN_RIGHT];
     float timeScale = this.maxAge / usableWidth;
     float mouseChange = (float) (currentPoint.getX() - this.mouseDragStart
         .getX());
 
     float timeChangeMillis = mouseChange * timeScale;
     this.timeOffset += timeChangeMillis;
     this.mouseDragStart = currentPoint;
     if (this.timeOffset < 0) {
       this.timeOffset = 0;
     } else if (this.timeOffset > (this.cache.getMaxCacheAge() - this.maxAge)) {
       this.timeOffset = this.cache.getMaxCacheAge() - this.maxAge;
     }
 
   }
 
   @Override
   public void mouseMoved(MouseEvent arg0) {
     // TODO Auto-generated method stub
 
   }
 
   @Override
   public void mouseClicked(MouseEvent arg0) {
     if (!this.cache.isClone() && arg0.getButton() == MouseEvent.BUTTON1) {
       this.scrolling = !this.scrolling;
     }
   }
 
   @Override
   public void mouseEntered(MouseEvent arg0) {
     // TODO Auto-generated method stub
 
   }
 
   @Override
   public void mouseExited(MouseEvent arg0) {
     // TODO Auto-generated method stub
 
   }
 
   @Override
   public void mousePressed(MouseEvent arg0) {
     this.mouseDragStart = arg0.getPoint();
   }
 
   @Override
   public void mouseReleased(MouseEvent arg0) {
     this.mouseDragStart = null;
   }
 
   @Override
   public boolean supportsAntiAlias() {
     return true;
   }
 
   @Override
   public void setTransparency(boolean transparency) {
     this.useTransparency = transparency;
   }
 
   @Override
   public boolean isTransparency() {
     return this.useTransparency;
   }
 
   @Override
   public boolean supportsTransparency() {
     return this.type == DataCache2.ValueType.VARIANCE;
   }
 
   @Override
   public void mouseWheelMoved(MouseWheelEvent arg0) {
     // Negative is up/away from user -> zoom in
     // Positive value is down/toward user -> zoom out
     int clicks = arg0.getWheelRotation();
 
     long cacheAge = this.cache.getMaxCacheAge();
 
     long historyChange = clicks * DISPAY_AGE_STEP;
     long maxAge = this.maxAge;
     maxAge += historyChange;
     if (maxAge > cacheAge) {
       maxAge = cacheAge;
     } else if (maxAge < this.MIN_DISPLAY_AGE) {
       maxAge = this.MIN_DISPLAY_AGE;
     }
 
     this.maxAge = maxAge;
     this.timeOffset = this.desiredTimeOffset;
     if (this.timeOffset > (this.cache.getMaxCacheAge() - this.maxAge)) {
       this.timeOffset = this.cache.getMaxCacheAge() - this.maxAge;
     }
   }
 
   public long getTimeOffset() {
     return timeOffset;
   }
 
   public void setTimeOffset(long timeOffset) {
     this.timeOffset = this.desiredTimeOffset = timeOffset;
     if (this.timeOffset < 0) {
       this.timeOffset = 0;
     } else if (this.timeOffset > (this.cache.getMaxCacheAge() - this.maxAge)) {
       this.timeOffset = this.cache.getMaxCacheAge() - this.maxAge;
     }
   }
 
   public void setCache(DataCache2 cache) {
     this.cache = cache;
   }
 
 }
