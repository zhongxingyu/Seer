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
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Composite;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.Stroke;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.font.FontRenderContext;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.GeneralPath;
 import java.awt.geom.Line2D;
 import java.awt.geom.Path2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import javax.imageio.ImageIO;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 
 import org.apache.mina.util.ConcurrentHashSet;
 import org.grailrtls.libcommon.util.HashableByteArray;
 import org.makesense.sigvis.DataCache2;
 import org.makesense.sigvis.ImageResources;
 import org.makesense.sigvis.DataCache2.ValueType;
 import org.makesense.sigvis.structs.ChartItem;
 import org.makesense.sigvis.structs.SimpleChartItem;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class HeatStripes extends JComponent implements DisplayPanel,
     MouseListener, MouseWheelListener {
 
   private static final Logger log = LoggerFactory.getLogger(HeatStripes.class);
 
   protected static final int DEFAULT_MARGIN = 20;
 
   protected static final int MARGIN_TOP = 0;
   protected static final int MARGIN_RIGHT = 1;
   protected static final int MARGIN_BOTTOM = 2;
   protected static final int MARGIN_LEFT = 3;
 
   protected int[] margins = { DEFAULT_MARGIN * 2, DEFAULT_MARGIN,
       DEFAULT_MARGIN * 2, DEFAULT_MARGIN * 2 };
 
   protected static final long MAX_GAP_FILL = 2000l;
 
   // Default to 30 seconds so as not to draw too much
   protected long maxAge = 30000;
 
   protected static final long MIN_DISP_AGE = 10000;
 
   protected static final long DISPLAY_AGE_STEP = 5000;
 
   protected final ValueType type;
 
   protected float thresholdValue = .5f;
 
   public float getThresholdValue() {
     return thresholdValue;
   }
 
   public void setThresholdValue(float thresholdValue) {
     this.thresholdValue = thresholdValue;
   }
 
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
 
   protected long lastRepaint = 0l;
 
   protected boolean drawLegend = false;
 
   protected float legendHeight = 0;
 
   protected boolean drawVerticalLines = true;
 
   protected Color axisTextColor = Color.LIGHT_GRAY;
 
   protected Color backgroundColor = Color.BLACK;
 
   protected Color chartGridColor = Color.DARK_GRAY;
 
   protected Color borderColor = Color.LIGHT_GRAY;
 
   protected boolean enableAntiAliasing = true;
 
   protected float minFps = 15;
 
   protected float currFps = 30f;
 
   protected int slowFrames = 0;
 
   protected DataCache2 cache;
 
   protected boolean scrolling = true;
 
   protected BufferedImage playImg = null;
 
   protected BufferedImage pauseImg = null;
 
   protected long timeOffset = 0l;
 
   protected long desiredTimeOffset = 0l;
 
   public float getMinFps() {
     return minFps;
   }
 
   public void setMinFps(final float minFps) {
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
 
   @Override
   public void setDisplayLegend(boolean drawLegend) {
     this.drawLegend = drawLegend;
     if (!this.drawLegend) {
       this.legendHeight = 0f;
     }
   }
 
   public HeatStripes(final ValueType type, final DataCache2 cache) {
     super();
     this.cache = cache;
     this.type = type;
 
     this.playImg = ImageResources.IMG_PLAY;
     this.pauseImg = ImageResources.IMG_PAUSE;
 
     if (this.cache.isClone()) {
       this.scrolling = false;
       this.lastRepaint = this.cache.getCreationTs();
     } else {
       this.lastRepaint = System.currentTimeMillis();
     }
 
     this.addMouseListener(this);
     this.addMouseWheelListener(this);
 
   }
 
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
     Stroke origStroke = g2.getStroke();
 
     g2.setColor(Color.BLACK);
     g2.fillRect(0, 0, screenWidth, screenHeight);
     float valueRange = this.maxValue - this.minValue;
 
     List<String> receivers = this.cache.getReceiverIds();
     List<String> devices = this.cache.getFiduciaryTransmitterIds();
 
     int numStreams = receivers.size() * devices.size();
 
     int usableWidth = (screenWidth - this.margins[MARGIN_LEFT] - this.margins[MARGIN_RIGHT]);
     int usableHeight = screenHeight - this.margins[MARGIN_TOP]
         - this.margins[MARGIN_BOTTOM] - (int) this.legendHeight;
     float millisHeight = usableHeight / (float) this.maxAge;
     float itemWidth = (float) usableWidth / (float) numStreams;
 
     if (this.drawLegend) {
       this.drawLegend(g2, screenWidth, screenHeight);
     }
 
     g2.setColor(Color.DARK_GRAY);
     this.drawChartGrid(g2, screenWidth, screenHeight, this.maxAge / 10.0f);
     this.drawChartBorders(g2, screenWidth, screenHeight);
     this.drawPauseInfo(g2, screenWidth, screenHeight);
 
     long youngestItem = this.lastRepaint - this.timeOffset;
     if (this.cache.isClone()) {
       youngestItem = this.cache.getCreationTs() - this.timeOffset;
     }
     long oldestItem = youngestItem - this.maxAge;
 
     BasicStroke stroke = new BasicStroke(itemWidth < .76f ? 1 : itemWidth,
         BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
     g2.setStroke(stroke);
     int itemIndex = -1;
     for (String recHash : receivers) {
       long lastItemAge = 0l;
       for (String devHash : devices) {
         ++itemIndex;
         // Get the history of data points
         List<ChartItem<Float>> devItems = (this.type == ValueType.RSSI ? this.cache
             .getRssiList(recHash, devHash) : this.cache.getVarianceList(
             recHash, devHash));
 
         // No data, then skip this pairing
         if (devItems == null) {
 
           continue;
         }
 
         // If the previous was skipped, then don't draw a connecting line...
         boolean skippedPrevious = true;
         float xOnScreen = this.margins[MARGIN_LEFT] + itemIndex * itemWidth;
         float prevYOnScreen = 0f;
         float previousValue = 0f;
         for (Iterator<ChartItem<Float>> iter = devItems.iterator(); iter
             .hasNext();) {
 
           ChartItem<Float> item = iter.next();
           long timeOffset = item.getCreationTime() - oldestItem;
           float yOnScreen = this.margins[MARGIN_TOP] + this.legendHeight
               + millisHeight * timeOffset;
 
           if (item.getCreationTime() < oldestItem) {
             skippedPrevious = true;
             continue;
           } else if (item.getCreationTime() > youngestItem) {
 
             break;
           }
           // FIXME: Use a variable/constant for time diff
 
           if ((item.getCreationTime() - lastItemAge > MAX_GAP_FILL)) {
             skippedPrevious = true;
           }
           lastItemAge = item.getCreationTime();
           float normalValue = item.getValue().floatValue() / valueRange;
           if (normalValue > 1.0f) {
             normalValue = 1.0f;
           }
 
           g2.setColor(Color.getHSBColor(previousValue * .9f, 0.9f, 0.9f));
           if (!skippedPrevious) {
             g2.drawLine((int) xOnScreen, (int) prevYOnScreen, (int) xOnScreen,
                 (int) yOnScreen);
           }
           previousValue = normalValue;
           prevYOnScreen = yOnScreen;
 
           if (item.getValue().floatValue() < this.thresholdValue) {
             if (!skippedPrevious) {
               g2.setColor(Color.getHSBColor(previousValue * .9f, 0.9f, 0.9f));
 
               g2.drawLine((int) xOnScreen, (int) prevYOnScreen,
                   (int) xOnScreen, (int) yOnScreen);
 
             }
             skippedPrevious = true;
             continue;
           }
           skippedPrevious = false;
 
         }
         // g2.draw(path);
       }
     }
 
     g2.setColor(Color.LIGHT_GRAY);
 
     // this.drawStatsValues(g2, screenWidth, screenHeight);
     this.drawTimestamp(g2, screenWidth, screenHeight);
     g2.setColor(origColor);
     g2.setComposite(origComposite);
     g2.setStroke(origStroke);
 
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
 
   protected void drawLegend(final Graphics g, final int screenWidth,
       final int screenHeight) {
     Graphics2D g2 = (Graphics2D) g;
     ArrayList<String> streamIdSet = new ArrayList<String>();
     streamIdSet.addAll(this.cache.getReceiverIds());
     Collections.sort(streamIdSet);
     if (streamIdSet == null && streamIdSet.size() == 0) {
       return;
     }
     FontMetrics fontMetrics = g2.getFontMetrics();
 
     float stringHeight = (float) fontMetrics.getStringBounds("Wjq", g)
         .getHeight();
 
     float rowHeight = stringHeight + 10;
     ArrayList<Integer> lineBreakIndexes = new ArrayList<Integer>();
 
     // Determine the number of rows and columns
     // Assume a square box for color display at string height, 5px spacing,
     // then text
     // 10px spacing between rows and columns
 
     String[] streamIdArray = streamIdSet.toArray(new String[] {});
 
     for (int i = 0; i < streamIdArray.length; ++i) {
       String drawString = streamIdArray[i];
       if (drawString.length() > 10) {
         int lastDot = drawString.lastIndexOf('.');
         if (lastDot > 0 && lastDot < drawString.length()) {
           streamIdArray[i] = drawString.substring(lastDot + 1);
         }
       }
     }
 
     int streamIndex = 0;
     float availableWidth = screenWidth - this.margins[MARGIN_LEFT]
         - this.margins[MARGIN_RIGHT];
     float currentRowWidth = 0f;
     for (String id : streamIdArray) {
       float entryWidth = fontMetrics.stringWidth(id) + 10;
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
       float bright = 0.95f;
       drawColor = new Color(Color.HSBtoRGB(hue, sat, bright));
       g2.setColor(drawColor);
       g2.drawString(streamId, currentXStart, currentYLine);
 
       currentXStart += fontMetrics.stringWidth(streamId) + 10;
       ++streamIndex;
     }
 
     this.legendHeight = currentYLine - this.margins[MARGIN_TOP] + stringHeight;
 
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
 
   }
 
   protected void drawChartGrid(Graphics g, final int screenWidth,
       final int screenHeight, float yStep) {
     Graphics2D g2 = (Graphics2D) g;
     g2.setColor(this.chartGridColor);
     List<String> receivers = this.cache.getReceiverIds();
     List<String> devices = this.cache.getFiduciaryTransmitterIds();
     int numItems = receivers.size() * devices.size();
     int usableWidth = screenWidth - this.margins[MARGIN_LEFT]
         - this.margins[MARGIN_RIGHT];
     int usableHeight = screenHeight - this.margins[MARGIN_TOP]
         - this.margins[MARGIN_BOTTOM] - (int) this.legendHeight;
 
     float yScale = (float) (usableHeight) / this.maxAge;
     FontMetrics metrics = g2.getFontMetrics();
 
     if (this.drawVerticalLines) {
       // Draw lines separating the receiver "lanes"
       String[] recArray = receivers.toArray(new String[] {});
       float xScale = ((float) usableWidth) / recArray.length;
 
       float verticalLine = this.margins[MARGIN_LEFT];
 
       int laneIndex = 0;
 
       int textOffset = 0;
 
       for (; verticalLine <= screenWidth - this.margins[MARGIN_RIGHT]; verticalLine += xScale) {
         g2.setColor(this.chartGridColor);
         g2.drawLine((int) verticalLine, screenHeight
             - this.margins[MARGIN_BOTTOM], (int) verticalLine,
             (int) (this.margins[MARGIN_TOP] + this.legendHeight));
         g2.setColor(this.axisTextColor);
         if (laneIndex < recArray.length) {
           String drawString = recArray[laneIndex].toString();
 
           if (drawString.length() > 10) {
             int lastDot = drawString.lastIndexOf('.');
             if (lastDot > 0 && lastDot < drawString.length()) {
               drawString = drawString.substring(lastDot + 1);
             }
           }
 
           float stringWidth = metrics.stringWidth(drawString);
 
           g2.drawString(String.format("%s", drawString), verticalLine + xScale
               / 2 - stringWidth / 2, screenHeight - this.margins[MARGIN_BOTTOM]
               / 2f + textOffset);
         }
         if (textOffset != 0) {
           textOffset = 0;
         } else {
           textOffset = this.margins[MARGIN_BOTTOM] / 2 - 1;
         }
         ++laneIndex;
       }
     }
 
     // Draw the time units on the left
 
     for (int atTime = 0; atTime <= this.maxAge; atTime += yStep) {
       float screenY = screenHeight - this.margins[MARGIN_BOTTOM]
           - (atTime * yScale);
 
       g2.setColor(this.chartGridColor);
 
       g2.drawLine((int) this.margins[MARGIN_LEFT], (int) screenY,
           (int) screenWidth - this.margins[MARGIN_RIGHT], (int) screenY);
       g2.setColor(this.axisTextColor);
       g2.drawString(String.format("%d",
           Integer.valueOf(atTime + (int) this.timeOffset) / 1000), 0, screenY);
     }
 
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
 
   public boolean isDrawVerticalLines() {
     return drawVerticalLines;
   }
 
   public void setDrawVerticalLines(boolean drawVerticalLines) {
     this.drawVerticalLines = drawVerticalLines;
   }
 
   @Override
   public String getDisplayedId() {
     return "";
   }
 
   @Override
   public void setDisplayedId(String ignored) {
     // ignored
   }
 
   @Override
   public boolean supportsAntiAlias() {
     return true;
   }
 
   @Override
   public void setDeviceIcon(BufferedImage ignored) {
     // Not used.
   }
 
   @Override
   public void setSelfAdjustMin(boolean ignored) {
     // Not used.
   }
 
   @Override
   public void setDeviceIsTransmitter(boolean isTransmitter) {
     // TODO Auto-generated method stub
 
   }
 
   @Override
   public void setSelfAdjustMax(boolean selfAdjustMax) {
     // TODO Auto-generated method stub
 
   }
 
   @Override
   public void setTransparency(boolean transparency) {
     // TODO Auto-generated method stub
 
   }
 
   @Override
   public boolean isTransparency() {
     // TODO Auto-generated method stub
     return false;
   }
 
   @Override
   public boolean supportsTransparency() {
     // TODO Auto-generated method stub
     return false;
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
     // TODO Auto-generated method stub
 
   }
 
   @Override
   public void mouseReleased(MouseEvent arg0) {
     // TODO Auto-generated method stub
 
   }
 
   @Override
   public void mouseWheelMoved(MouseWheelEvent arg0) {
     // Negative is up/away from user -> zoom in
     // Positive value is down/toward user -> zoom out
     int clicks = arg0.getWheelRotation();
 
     long cacheAge = this.cache.getMaxCacheAge();
 
     long historyChange = clicks * DISPLAY_AGE_STEP;
     long maxAge = this.maxAge;
     maxAge += historyChange;
     if (maxAge > cacheAge) {
       maxAge = cacheAge;
     } else if (maxAge < this.MIN_DISP_AGE) {
       maxAge = this.MIN_DISP_AGE;
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
