 /*
  * $Id: ContrastStretchPane.java,v 1.1 2007/04/19 10:41:38 norman Exp $
  *
  * Copyright (C) 2002 by Brockmann Consult (info@brockmann-consult.de)
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the
  * Free Software Foundation. This program is distributed in the hope it will
  * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
  * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.esa.beam.visat.toolviews.imageinfo;
 
 import com.bc.ceres.binding.ValueContainer;
 import com.bc.ceres.binding.ValueModel;
 import com.bc.ceres.binding.ValueRange;
 import com.bc.ceres.binding.swing.BindingContext;
 import com.jidesoft.combobox.ColorChooserPanel;
 import com.jidesoft.popup.JidePopup;
 import org.esa.beam.framework.datamodel.ColorPaletteDef;
 import org.esa.beam.util.math.Histogram;
 import org.esa.beam.util.math.MathUtils;
 import org.esa.beam.util.math.Range;
 
 import javax.swing.JComponent;
 import javax.swing.JFormattedTextField;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.SwingUtilities;
 import javax.swing.text.NumberFormatter;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.GeneralPath;
 import java.awt.geom.Rectangle2D;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.text.Format;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 
 
 class ImageInfoEditor extends JPanel {
     public static final String PROPERTY_NAME_MODEL = "model";
 
     public static final String NO_DISPLAY_INFO_TEXT = "No image information available.";
     public static final String FONT_NAME = "Verdana";
     public static final int FONT_SIZE = 9;
     public static final int INVALID_INDEX = -1;
     public static final int PALETTE_HEIGHT = 16;
     public static final int SLIDER_WIDTH = 12;
     public static final int SLIDER_HEIGHT = 10;
     public static final int SLIDER_VALUES_AREA_HEIGHT = 40;
     public static final int HOR_BORDER_SIZE = 10;
     public static final int VER_BORDER_SIZE = 4;
     public static final int PREF_HISTO_WIDTH = 256; //196;
     public static final int PREF_HISTO_HEIGHT = 196; //128;
     public static final Dimension PREF_COMPONENT_SIZE
             = new Dimension(PREF_HISTO_WIDTH + 2 * HOR_BORDER_SIZE,
                             PREF_HISTO_HEIGHT + PALETTE_HEIGHT + SLIDER_HEIGHT / 2
                                     + 2 * HOR_BORDER_SIZE + FONT_SIZE);
     public static final BasicStroke STROKE_1 = new BasicStroke(1.0f);
     public static final BasicStroke STROKE_2 = new BasicStroke(2.0f);
     public static final BasicStroke DASHED_STROKE = new BasicStroke(0.75F, BasicStroke.CAP_SQUARE,
                                                                     BasicStroke.JOIN_MITER, 1.0F, new float[]{5.0F},
                                                                     0.0F);
 
     private ImageInfoEditorModel model;
 
     private Font labelFont;
     private final Shape sliderShape;
     private int sliderTextBaseLineY;
     private final Rectangle sliderBaseLineRect;
     private final Rectangle paletteRect;
     private final Rectangle histoRect;
     private double roundFactor;
     private final InternalMouseListener internalMouseListener;
     private double[] factors;
     private Color[] palette;
     private ModelCL modelCL;
     private JidePopup popup;
 
     public ImageInfoEditor() {
         labelFont = createLabelFont();
         sliderShape = createSliderShape();
         sliderBaseLineRect = new Rectangle();
         paletteRect = new Rectangle();
         histoRect = new Rectangle();
         internalMouseListener = new InternalMouseListener();
         modelCL = new ModelCL();
         addChangeListener(new RepaintCL());
     }
 
     public ImageInfoEditorModel getModel() {
         return model;
     }
 
     public void setModel(final ImageInfoEditorModel model) {
         final ImageInfoEditorModel oldModel = this.model;
         if (oldModel != model) {
             this.model = model;
             deinstallMouseListener();
             if (oldModel != null) {
                 oldModel.removeChangeListener(modelCL);
             }
             if (this.model != null) {
                 roundFactor = MathUtils.computeRoundFactor(this.model.getMinSample(), this.model.getMaxSample(), 2);
                 installMouseListener();
                 model.addChangeListener(modelCL);
             }
             firePropertyChange(PROPERTY_NAME_MODEL, oldModel, this.model);
             fireStateChanged();
         }
         if (isShowing()) {
             repaint();
         }
     }
 
     public void addChangeListener(ChangeListener l) {
         listenerList.add(ChangeListener.class, l);
     }
 
     public void removeChangeListener(ChangeListener l) {
         listenerList.remove(ChangeListener.class, l);
     }
 
     public ChangeListener[] getChangeListeners() {
         return listenerList.getListeners(ChangeListener.class);
     }
 
 
     protected void fireStateChanged() {
         final ChangeEvent event = new ChangeEvent(this);
         Object[] listeners = listenerList.getListenerList();
         for (int i = listeners.length - 2; i >= 0; i -= 2) {
             if (listeners[i] == ChangeListener.class) {
                 ((ChangeListener) listeners[i + 1]).stateChanged(event);
             }
         }
     }
 
     public void compute95Percent() {
         final Histogram histogram = new Histogram(getModel().getHistogramBins(),
                                                   scaleInverse(getModel().getMinSample()),
                                                   scaleInverse(getModel().getMaxSample()));
         final Range autoStretchRange = histogram.findRangeFor95Percent();
         computeFactors();
         setFirstSliderSample(scale(autoStretchRange.getMin()));
         setLastSliderSample(scale(autoStretchRange.getMax()));
         computePartitioning(false);
         computeZoomInToSliderLimits();
     }
 
     public void compute100Percent() {
         computeFactors();
         setFirstSliderSample(getModel().getMinSample());
         setLastSliderSample(getModel().getMaxSample());
         computePartitioning(false);
         computeZoomInToSliderLimits();
     }
 
     public void distributeSlidersEvenly() {
         final double firstSliderPos = scaleInverse(getFirstSliderSample());
         final double lastSliderPos = scaleInverse(getLastSliderSample());
         final double maxDistance = lastSliderPos - firstSliderPos;
         final double evenSpace = maxDistance / (getSliderCount() - 2 + 1);
         for (int i = 0; i < getSliderCount(); i++) {
             final double value = scale(firstSliderPos + evenSpace * i);
             setSliderSample(i, value, false);
         }
     }
 
     private void computePartitioning(boolean adjusting) {
         final double firstPS = scaleInverse(getFirstSliderSample());
         final double lastPS = scaleInverse(getLastSliderSample());
         final double dsn = lastPS - firstPS;
         for (int i = 0; i < (getSliderCount() - 1); i++) {
             final double value = scale(firstPS + factors[i] * dsn);
             setSliderSample(i, value, adjusting);
         }
     }
 
     private void computeFactors() {
         factors = new double[getSliderCount()];
         final double firstPS = scaleInverse(getFirstSliderSample());
         final double lastPS = scaleInverse(getLastSliderSample());
         double dsn = lastPS - firstPS;
         if (dsn == 0 || Double.isNaN(dsn)) {
             dsn = Double.MIN_VALUE;
         }
         for (int i = 0; i < getSliderCount(); i++) {
             final double sample = scaleInverse(getSliderSample(i));
             final double dsi = sample - firstPS;
             factors[i] = dsi / dsn;
         }
     }
 
     @Override
     public Dimension getPreferredSize() {
         return PREF_COMPONENT_SIZE;
     }
 
     @Override
     public void setBounds(int x, int y, int width, int heigth) {
         super.setBounds(x, y, width, heigth);
         computeSizeAttributes();
     }
 
     public void computeZoomInToSliderLimits() {
         final double firstSliderValue;
         final double lastSliderValue;
         firstSliderValue = scaleInverse(getFirstSliderSample());
         lastSliderValue = scaleInverse(getLastSliderSample());
         final double tenPercentOffset = (lastSliderValue - firstSliderValue) / 80 * 10;
         final double minViewSample;
         final double maxViewSample;
         minViewSample = scale(firstSliderValue - tenPercentOffset);
         maxViewSample = scale(lastSliderValue + tenPercentOffset);
 
         getModel().setMinHistogramViewSample(Math.max(minViewSample, getMinSample()));
         getModel().setMaxHistogramViewSample(Math.min(maxViewSample, getMaxSample()));
         repaint();
     }
 
     public void computeZoomOutToFullHistogramm() {
         getModel().setMinHistogramViewSample(getMinSample());
         getModel().setMaxHistogramViewSample(getMaxSample());
         repaint();
     }
 
     @Override
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
         if (getModel() == null || getWidth() == 0 || getHeight() == 0) {
             return;
         }
 
         Graphics2D g2d = (Graphics2D) g;
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
         g2d.setFont(labelFont);
         computeSizeAttributes();
 
         if (getModel() != null && isValidRange()) {
             drawPalette(g2d);
             drawSliders(g2d);
             if (isHistogramAvailable()) {
                 drawHistogramPane(g2d);
             }
         } else {
             FontMetrics fontMetrics = g2d.getFontMetrics();
             drawMissingBasicDisplayInfoMessage(g2d, fontMetrics);
         }
     }
 
     private boolean isHistogramAvailable() {
         return model.isHistogramAvailable();
     }
 
     private boolean isValidRange() {
         return model.getMinSample() <= model.getMaxSample();
     }
 
     public void computeZoomOutVertical() {
         getModel().setHistogramViewGain(getModel().getHistogramViewGain() * (1.0f / 1.4f));
         repaint();
     }
 
     public void computeZoomInVertical() {
         getModel().setHistogramViewGain(getModel().getHistogramViewGain() * (1.4f));
         repaint();
     }
 
     private void drawMissingBasicDisplayInfoMessage(Graphics2D g2d, FontMetrics fontMetrics) {
         int totWidth = getWidth();
         int totHeight = getHeight();
         g2d.drawString(NO_DISPLAY_INFO_TEXT,
                        (totWidth - fontMetrics.stringWidth(NO_DISPLAY_INFO_TEXT)) / 2,
                        (totHeight + fontMetrics.getHeight()) / 2);
     }
 
     private void drawPalette(Graphics2D g2d) {
         long paletteX1 = paletteRect.x + Math.round(getRelativeSliderPos(getFirstSliderSample()));
         long paletteX2 = paletteRect.x + Math.round(getRelativeSliderPos(getLastSliderSample()));
         g2d.setStroke(STROKE_1);
         Color[] palette = getColorPalette();
         if (palette != null) {
             for (int x = paletteRect.x; x < paletteRect.x + paletteRect.width; x++) {
                 long divisor = paletteX2 - paletteX1;
                 int palIndex;
                 if (divisor == 0) {
                     palIndex = x < paletteX1 ? 0 : palette.length - 1;
                 } else {
                     palIndex = (int) ((palette.length * (x - paletteX1)) / divisor);
                 }
                 if (palIndex < 0) {
                     palIndex = 0;
                 }
                 if (palIndex > palette.length - 1) {
                     palIndex = palette.length - 1;
                 }
                 g2d.setColor(palette[palIndex]);
                 g2d.drawLine(x, paletteRect.y, x, paletteRect.y + paletteRect.height);
             }
         }
         g2d.setStroke(STROKE_1);
         g2d.setColor(Color.darkGray);
         g2d.draw(paletteRect);
     }
 
     private Color[] getColorPalette() {
         if (palette == null) {
             palette = getModel().createColorPalette();
         }
         return palette;
     }
 
     private void drawSliders(Graphics2D g2d) {
         g2d.translate(sliderBaseLineRect.x, sliderBaseLineRect.y);
         g2d.setStroke(STROKE_1);
         for (int i = 0; i < getSliderCount(); i++) {
             double sliderPos = getRelativeSliderPos(getSliderSample(i));
 
             g2d.translate(sliderPos, 0.0);
 
             final Color sliderColor = getSliderColor(i);
             g2d.setPaint(sliderColor);
             g2d.fill(sliderShape);
 
             int gray = (sliderColor.getRed() + sliderColor.getGreen() + sliderColor.getBlue()) / 3;
             g2d.setColor(gray < 128 ? Color.white : Color.black);
             g2d.draw(sliderShape);
 
             String text = String.valueOf(round(getSliderSample(i)));
             g2d.setColor(Color.black);
             // save the old transformation
             final AffineTransform oldTransform = g2d.getTransform();
             g2d.transform(AffineTransform.getRotateInstance(Math.PI / 2));
             g2d.drawString(text, 3 + 0.5f * SLIDER_HEIGHT, 0.35f * FONT_SIZE);
             // restore the old transformation
             g2d.setTransform(oldTransform);
 
             g2d.translate(-sliderPos, 0.0);
         }
 
         g2d.translate(-sliderBaseLineRect.x, -sliderBaseLineRect.y);
     }
 
     private void drawHistogramPane(Graphics2D g2d) {
         Shape oldClip = g2d.getClip();
         g2d.setClip(histoRect.x - 1, histoRect.y - 1, histoRect.width + 2, histoRect.height + 2);
 
         drawHistogram(g2d);
         drawGradationCurve(g2d);
         drawHistogramText(g2d);
         drawHistogramBorder(g2d);
 
         g2d.setClip(oldClip);
     }
 
     private void drawHistogramBorder(Graphics2D g2d) {
         g2d.setStroke(STROKE_1);
         g2d.setColor(Color.darkGray);
         g2d.draw(histoRect);
     }
 
     private void drawHistogramText(Graphics2D g2d) {
         final FontMetrics metrics = g2d.getFontMetrics();
         final String sMin = "min:";
         final String sMax = "max:";
         final String sUnit = "unit:";
         final int sMinWidth = metrics.stringWidth(sMin);
         final int sMaxWidth = metrics.stringWidth(sMax);
         final int sUnitWidth = metrics.stringWidth(sUnit);
 
         final String sMinValue = String.valueOf(round(getMinSample()));
         final String sMaxValue = String.valueOf(round(getMaxSample()));
         final int sMinValueWidth = metrics.stringWidth(sMinValue);
         final int sMaxValueWidth = metrics.stringWidth(sMaxValue);
 
         int maxPrefixWidth = Math.max(sMinWidth, sMaxWidth);
         int maxValueWidth = Math.max(sMinValueWidth, sMaxValueWidth);
 
         final String sUnitValue;
 
         String unit = getModel().getUnit();
         if (unit != null && unit.length() > 0) {
             sUnitValue = unit;
             maxPrefixWidth = Math.max(maxPrefixWidth, sUnitWidth);
             final int sUnitValueWidth = metrics.stringWidth(sUnitValue);
             maxValueWidth = Math.max(maxValueWidth, sUnitValueWidth);
         } else {
             sUnitValue = "";
         }
         final int xStartPosPrefix = histoRect.x + histoRect.width - 5 - maxPrefixWidth - maxValueWidth;
         final int xStartPosValues = histoRect.x + histoRect.width - 2 - maxValueWidth;
 
         String[] strings = new String[]{sMin, sMinValue, sMax, sMaxValue, sUnit, sUnitValue};
 
         final int yStartPos = histoRect.y + 1;
 
         g2d.setColor(this.getBackground());
         drawStrings(g2d, strings, xStartPosPrefix - 1, xStartPosValues - 1, yStartPos);
         drawStrings(g2d, strings, xStartPosPrefix - 1, xStartPosValues - 1, yStartPos - 1);
         drawStrings(g2d, strings, xStartPosPrefix - 1, xStartPosValues - 1, yStartPos + 1);
         drawStrings(g2d, strings, xStartPosPrefix + 1, xStartPosValues + 1, yStartPos);
         drawStrings(g2d, strings, xStartPosPrefix + 1, xStartPosValues + 1, yStartPos - 1);
         drawStrings(g2d, strings, xStartPosPrefix + 1, xStartPosValues + 1, yStartPos + 1);
         drawStrings(g2d, strings, xStartPosPrefix, xStartPosValues, yStartPos - 1);
         drawStrings(g2d, strings, xStartPosPrefix, xStartPosValues, yStartPos + 1);
         g2d.setColor(this.getForeground());
         drawStrings(g2d, strings, xStartPosPrefix, xStartPosValues, yStartPos);
     }
 
     private void drawStrings(Graphics2D g2d, String[] strings, int xStartPosPrefix, int xStartPosValues,
                              int yStartPos) {
         int line = 1;
         g2d.drawString(strings[0], xStartPosPrefix, yStartPos + FONT_SIZE * line);
         g2d.drawString(strings[1], xStartPosValues, yStartPos + FONT_SIZE * line);
         line++;
         g2d.drawString(strings[2], xStartPosPrefix, yStartPos + FONT_SIZE * line);
         g2d.drawString(strings[3], xStartPosValues, yStartPos + FONT_SIZE * line);
         if (!strings[5].equals("")) {
             line++;
             g2d.drawString(strings[4], xStartPosPrefix, yStartPos + FONT_SIZE * line);
             g2d.drawString(strings[5], xStartPosValues, yStartPos + FONT_SIZE * line);
         }
     }
 
     private void drawGradationCurve(Graphics2D g2d) {
         g2d.setColor(Color.white);
         g2d.setStroke(STROKE_2);
 
         int x1, y1, x2, y2;
 
         x1 = histoRect.x;
         y1 = histoRect.y + histoRect.height - 1;
         x2 = (int) getAbsoluteSliderPos(getFirstSliderSample());
         y2 = y1;
         g2d.drawLine(x1, y1, x2, y2);
 
         x1 = x2;
         y1 = y2;
         x2 = (int) getAbsoluteSliderPos(getLastSliderSample());
         y2 = histoRect.y + 1;
         final byte[] gammaCurve = getModel().getGammaCurve();
         if (gammaCurve != null) {
             int xx = x1;
             int yy = y1;
             for (int x = x1 + 1; x <= x2; x++) {
                 int i = MathUtils.roundAndCrop((255.9f * (x - x1)) / (x2 - x1), 0, 255);
                 int y = y1 + ((y2 - y1) * (gammaCurve[i] & 0xff)) / 256;
                 g2d.drawLine(xx, yy, x, y);
                 //System.out.println("x=" + x + ", y=" + y);
                 xx = x;
                 yy = y;
             }
         } else {
             g2d.drawLine(x1, y1, x2, y2);
         }
 
         x1 = x2;
         y1 = y2;
         x2 = histoRect.x + histoRect.width;
         y2 = histoRect.y + 1;
         g2d.drawLine(x1, y1, x2, y2);
 
         // Vertical lines
         g2d.setStroke(DASHED_STROKE);
         for (int i = 0; i < getSliderCount(); i++) {
             x1 = (int) getAbsoluteSliderPos(getSliderSample(i));
             y1 = histoRect.y + histoRect.height - 1;
             x2 = x1;
             y2 = histoRect.y + 1;
             g2d.drawLine(x1, y1, x2, y2);
         }
     }
 
     private void drawHistogram(Graphics2D g2d) {
         g2d.setColor(Color.black);
         g2d.setPaint(Color.black);
 
         if (getModel().isHistogramAvailable()) {
             final int[] histogramBins = getModel().getHistogramBins();
             final double maxHistogramCounts = getMaxVisibleHistogramCounts(histogramBins, 1.0 / 16.0);
             final double gain = getModel().getHistogramViewGain();
             final double firstVisibleBinIndexFloat = getFirstHistogramViewBinIndex();
             final int firstVisibleBinIndex = MathUtils.floorAndCrop(firstVisibleBinIndexFloat, 0,
                                                                     histogramBins.length - 1);
             final double indexDelta = firstVisibleBinIndexFloat - firstVisibleBinIndex;
             final double maxHistoRectHeight = 0.9 * histoRect.height;
             double numVisibleBins = getHistogramViewBinCount();
             if (numVisibleBins > 0 && maxHistogramCounts > 0) {
                 g2d.setStroke(new BasicStroke(1.0f));
                 final double binWidth = histoRect.width / numVisibleBins;
                 final double pixelOffs = indexDelta * binWidth;
                 final double countsScale = (gain * maxHistoRectHeight) / maxHistogramCounts;
                 if ((numVisibleBins + firstVisibleBinIndex) < histogramBins.length) {
                     numVisibleBins++;
                 }
                 Rectangle2D.Double r = new Rectangle2D.Double();
                 for (int i = 0; i < (int) numVisibleBins; i++) {
                     final double counts = histogramBins[i + firstVisibleBinIndex];
                     double binHeight = countsScale * counts;
                     if (binHeight >= histoRect.height) {
                         // must crop here because on highly centered histograms this value is FAR beyond the rectangle
                         // and then triggers an exception when trying to draw it.
                         binHeight = histoRect.height - 1;
                     }
                     final double y1 = histoRect.y + histoRect.height - 1 - binHeight;
                     final double x1 = histoRect.x + binWidth * i - pixelOffs - 0.5 * binWidth;
                     r.setRect(x1, y1, binWidth, binHeight);
                     g2d.fill(r);
                 }
             }
         }
     }
 
     private double getMaxVisibleHistogramCounts(final int[] histogramBins, double ratio) {
         double totalHistogramCounts = 0.0;
         for (int histogramBin : histogramBins) {
             totalHistogramCounts += histogramBin;
         }
         final double limitHistogramCounts = totalHistogramCounts * ratio;
         double maxHistogramCounts = 0.0;
         for (int histogramBin : histogramBins) {
             if (histogramBin < limitHistogramCounts) {
                 maxHistogramCounts = Math.max(maxHistogramCounts, histogramBin);
             }
         }
         return maxHistogramCounts;
     }
 
     private void installMouseListener() {
         addMouseListener(internalMouseListener);
         addMouseMotionListener(internalMouseListener);
     }
 
     private void deinstallMouseListener() {
         removeMouseListener(internalMouseListener);
         removeMouseMotionListener(internalMouseListener);
     }
 
     private double getMaxSample() {
         return getModel().getMaxSample();
     }
 
     private double getMinSample() {
         return getModel().getMinSample();
     }
 
     private int getSliderCount() {
         return getModel().getSliderCount();
     }
 
     private double getMinSliderSample(int sliderIndex) {
         if (sliderIndex == 0) {
             return getMinSample();
         } else {
             return getSliderSample(sliderIndex - 1);
         }
     }
 
     private double getMaxSliderSample(int sliderIndex) {
         if (sliderIndex == getSliderCount() - 1) {
             return getMaxSample();
         } else {
             return getSliderSample(sliderIndex + 1);
         }
     }
 
     private double getFirstSliderSample() {
         return getSliderSample(0);
     }
 
     private void setFirstSliderSample(double v) {
         setSliderSample(0, v);
     }
 
     private double getLastSliderSample() {
         return getSliderSample(getModel().getSliderCount() - 1);
     }
 
     private void setLastSliderSample(double v) {
         setSliderSample(getModel().getSliderCount() - 1, v);
     }
 
     private double getSliderSample(int index) {
         return getModel().getSliderSample(index);
     }
 
     private void setSliderSample(int index, double v) {
         getModel().setSliderSample(index, v);
     }
 
     private void setSliderSample(int index, double newValue, boolean adjusting) {
         // todo - WHAT IS THIS??!?!? (nf - 27.06.2008)
         // {{
         if (adjusting) {
             double minValue = Double.NEGATIVE_INFINITY;
             if (index > 0 && index < getSliderCount() - 1) {
                 minValue = getSliderSample(index - 1) + Float.MIN_VALUE;
             }
             if (newValue < minValue) {
                 newValue = minValue;
             }
         }
         // }}
 
         setSliderSample(index, newValue);
     }
 
     private Color getSliderColor(int index) {
         return getModel().getSliderColor(index);
     }
 
     private void setSliderColor(int index, Color c) {
         getModel().setSliderColor(index, c);
     }
 
     private static Font createLabelFont() {
         return new Font(FONT_NAME, Font.PLAIN, FONT_SIZE);
     }
 
     public static Shape createSliderShape() {
         GeneralPath path = new GeneralPath();
         path.moveTo(0.0F, -0.5F * SLIDER_HEIGHT);
         path.lineTo(+0.5F * SLIDER_WIDTH, 0.5F * SLIDER_HEIGHT);
         path.lineTo(-0.5F * SLIDER_WIDTH, 0.5F * SLIDER_HEIGHT);
         path.closePath();
         return path;
     }
 
     private double computeSliderValueForX(int sliderIndex, int x) {
         final double minVS = scaleInverse(getModel().getMinHistogramViewSample());
         final double maxVS = scaleInverse(getModel().getMaxHistogramViewSample());
         final double value = scale(minVS + (x - sliderBaseLineRect.x) * (maxVS - minVS) / sliderBaseLineRect.width);
         if (isFirstSliderIndex(sliderIndex)) {
             return Math.min(value, getLastSliderSample());
         }
         if (isLastSliderIndex(sliderIndex)) {
             return Math.max(value, getFirstSliderSample());
         }
         return computeAdjustedSliderValue(sliderIndex, value);
     }
 
     private double computeAdjustedSliderValue(int sliderIndex, double value) {
         double valueD = value;
         double minSliderValue = getMinSliderSample(sliderIndex);
         minSliderValue += minSliderValue * 1e-8;
         double maxSliderValue = getMaxSliderSample(sliderIndex);
         maxSliderValue -= maxSliderValue * 1e-8;
         if (valueD < minSliderValue) {
             valueD = minSliderValue;
         }
         if (valueD > maxSliderValue) {
             valueD = maxSliderValue;
         }
         return valueD;
     }
 
     private boolean isFirstSliderIndex(int sliderIndex) {
         return sliderIndex == 0;
     }
 
     private boolean isLastSliderIndex(int sliderIndex) {
         return (getSliderCount() - 1) == sliderIndex;
     }
 
     private double round(double value) {
         return MathUtils.round(value, roundFactor);
     }
 
     private double getAbsoluteSliderPos(double sample) {
         return sliderBaseLineRect.x + getRelativeSliderPos(sample);
     }
 
     private double getRelativeSliderPos(double sample) {
         return getNormalizedHistogramViewSampleValue(sample) * sliderBaseLineRect.width;
     }
 
     private void computeSizeAttributes() {
         int totWidth = getWidth();
         int totHeight = getHeight();
 
         int imageWidth = totWidth - 2 * HOR_BORDER_SIZE;
 
         sliderTextBaseLineY = totHeight - VER_BORDER_SIZE - SLIDER_VALUES_AREA_HEIGHT;
 
         sliderBaseLineRect.x = HOR_BORDER_SIZE;
         sliderBaseLineRect.y = sliderTextBaseLineY - SLIDER_HEIGHT / 2;
         sliderBaseLineRect.width = imageWidth;
         sliderBaseLineRect.height = 1;
 
         paletteRect.x = HOR_BORDER_SIZE;
         paletteRect.y = sliderBaseLineRect.y - PALETTE_HEIGHT;
         paletteRect.width = imageWidth;
         paletteRect.height = PALETTE_HEIGHT;
 
         histoRect.x = HOR_BORDER_SIZE;
         histoRect.y = VER_BORDER_SIZE;
         histoRect.width = imageWidth;
         histoRect.height = paletteRect.y - histoRect.y - 3;
     }
 
     public double getHistogramViewBinCount() {
         if (!isHistogramAvailable()) {
             return -1;
         }
         if (getMinSample() != getModel().getMinHistogramViewSample() || getMaxSample() != getModel().getMaxHistogramViewSample()) {
             return getModel().getHistogramBins().length
                     / (scaleInverse(getMaxSample()) - scaleInverse(getMinSample()))
                     * (scaleInverse(getModel().getMaxHistogramViewSample()) - scaleInverse(getModel().getMinHistogramViewSample()));
         }
         return getModel().getHistogramBins().length;
     }
 
     public double getFirstHistogramViewBinIndex() {
         if (!isHistogramAvailable()) {
             return -1;
         }
         if (getMinSample() != getModel().getMinHistogramViewSample()) {
             return (getModel().getHistogramBins().length - 1)
                     / (scaleInverse(getMaxSample()) - scaleInverse(getMinSample()))
                     * (scaleInverse(getModel().getMinHistogramViewSample()) - scaleInverse(getMinSample()));
         }
         return 0;
     }
 
 
     public double getNormalizedHistogramViewSampleValue(double sample) {
         final double minVisibleSample;
         final double maxVisibleSample;
         minVisibleSample = scaleInverse(getModel().getMinHistogramViewSample());
         maxVisibleSample = scaleInverse(getModel().getMaxHistogramViewSample());
         sample = scaleInverse(sample);
         double delta = maxVisibleSample - minVisibleSample;
         if (delta == 0 || Double.isNaN(delta)) {
             delta = 1;
         }
         return (sample - minVisibleSample) / delta;
     }
 
     private void editSliderColor(MouseEvent evt, final int sliderIndex) {
         final ColorChooserPanel panel = new ColorChooserPanel(ColorChooserPanel.PALETTE_COLOR_40,
                                                               true,     // allow more colors
                                                               true);    // allow default color
         final Color selectedColor = getSliderColor(sliderIndex);
         if (selectedColor.equals(new Color(0, 0, 0, 0))) {
             panel.setSelectedColor(null);
         } else {
             panel.setSelectedColor(selectedColor);
         }
 
         popup = new JidePopup();
         popup.setOwner(ImageInfoEditor.this);
         popup.setDefaultFocusComponent(panel);
         popup.setContentPane(panel);
         popup.setAttachable(true);
         popup.setMovable(true);
         popup.showPopup(evt.getXOnScreen(), evt.getYOnScreen());
 
         panel.addItemListener(new ItemListener() {
             public void itemStateChanged(ItemEvent e) {
                 popup.hidePopup();
                 final Color selectedColor = panel.getSelectedColor();
                 if (selectedColor != null) {
                     setSliderColor(sliderIndex, selectedColor);
                 } else {
                     setSliderColor(sliderIndex, new Color(0, 0, 0, 0));
                 }
 
             }
         });
     }
 
     private void editSliderSample(MouseEvent evt, final int sliderIndex) {
         hidePopup();
 
         final ValueContainer vc = new ValueContainer();
         vc.addModel(ValueModel.createModel("sample", getSliderSample(sliderIndex)));
         vc.getValueDescriptor("sample").setDisplayName("sample");
         vc.getValueDescriptor("sample").setValueRange(new ValueRange(getMinSliderSample(sliderIndex), getMaxSliderSample(sliderIndex)));
         vc.getValueDescriptor("sample").setUnit(getModel().getUnit());
 
         final BindingContext ctx = new BindingContext(vc);
         final NumberFormatter formatter = new NumberFormatter(new DecimalFormat("#0.0#"));
         formatter.setValueClass(Double.class); // to ensure that double values are returned
         final JFormattedTextField field = new JFormattedTextField(formatter);
         field.setColumns(11);
         field.setHorizontalAlignment(JFormattedTextField.RIGHT);
         ctx.bind("sample", field);
 
         showPopup(evt, field);
 
         ctx.addPropertyChangeListener("sample", new PropertyChangeListener() {
 
             public void propertyChange(PropertyChangeEvent evt) {
                popup.hidePopup();
                 setSliderSample(sliderIndex, (Double) ctx.getBinding("sample").getValue());
             }
         });
     }
 
     private void showPopup(MouseEvent evt, JComponent component) {
         popup = new JidePopup();
         popup.getContentPane().add(component);
         popup.setOwner(ImageInfoEditor.this);
         popup.setDefaultFocusComponent(component);
         popup.setAttachable(true);
         popup.showPopup(evt.getXOnScreen(), evt.getYOnScreen());
     }
 
     private void hidePopup() {
         if (popup != null && popup.isVisible()) {
             popup.hidePopupImmediately();
             popup = null;
         }
     }
 
     private class InternalMouseListener implements MouseListener, MouseMotionListener {
 
         private int draggedSliderIndex;
         private boolean dragging;
 
         public InternalMouseListener() {
             resetState();
         }
 
         public boolean isDragging() {
             return dragging;
         }
 
         public void setDragging(boolean dragging) {
             this.dragging = dragging;
         }
 
         public void mousePressed(MouseEvent mouseEvent) {
             hidePopup();
             resetState();
             setDraggedSliderIndex(getNearestSliderIndex(mouseEvent.getX(), mouseEvent.getY()));
             if (isFirstSliderDragged() || isLastSliderDragged()) {
                 computeFactors();
             }
         }
 
         public void mouseReleased(MouseEvent evt) {
             if (!isDragging()) {
                 maybeShowSliderActions(evt);
                 if (popup == null && SwingUtilities.isLeftMouseButton(evt)) {
                     int mode = 0;
                     int sliderIndex = getSelectedSliderIndex(evt);
                     if (sliderIndex != INVALID_INDEX && getModel().isColorEditable()) {
                         mode = 1;
                     }
                     if (mode == 0) {
                         if (sliderIndex == INVALID_INDEX) {
                             sliderIndex = getSelectedSliderTextIndex(evt);
                         }
                         if (sliderIndex != INVALID_INDEX) {
                             mode = 2;
                         }
                     }
                     if (mode == 1) {
                         editSliderColor(evt, sliderIndex);
                     } else if (mode == 2) {
                         editSliderSample(evt, sliderIndex);
                     }
                 }
             }
             if (popup == null) {
                 doDragSlider(evt, false);
                 setDragging(false);
                 setDraggedSliderIndex(INVALID_INDEX);
             }
         }
 
         public void mouseClicked(MouseEvent evt) {
             maybeShowSliderActions(evt);
         }
 
         public void mouseEntered(MouseEvent mouseEvent) {
             resetState();
         }
 
         public void mouseExited(MouseEvent mouseEvent) {
             resetState();
         }
 
         public void mouseDragged(MouseEvent mouseEvent) {
             setDragging(true);
             doDragSlider(mouseEvent, true);
         }
 
         private void doDragSlider(MouseEvent mouseEvent, boolean adjusting) {
             if (getDraggedSliderIndex() != INVALID_INDEX) {
                 int x = mouseEvent.getX();
                 x = Math.max(x, sliderBaseLineRect.x);
                 x = Math.min(x, sliderBaseLineRect.x + sliderBaseLineRect.width);
                 final double newSample = computeSliderValueForX(getDraggedSliderIndex(), x);
                 setSliderSample(getDraggedSliderIndex(), newSample, adjusting);
                 if (isFirstSliderDragged() || isLastSliderDragged()) {
                     computePartitioning(adjusting);
                 }
             }
         }
 
         public void mouseMoved(MouseEvent mouseEvent) {
             if (isDragging()) {
                 mouseDragged(mouseEvent);
             }
         }
 
         private void maybeShowSliderActions(MouseEvent mouseEvent) {
             if (getModel().isColorEditable() && mouseEvent.isPopupTrigger()) {
                 final int sliderIndex = getSelectedSliderIndex(mouseEvent);
                 showSliderActions(mouseEvent, sliderIndex);
             }
         }
 
         private void setDraggedSliderIndex(final int draggedSliderIndex) {
             if (this.draggedSliderIndex != draggedSliderIndex) {
                 // Debug.trace("ContrastStretchPane.setDraggedSliderIndex(" + draggedSliderIndex + ") called");
                 this.draggedSliderIndex = draggedSliderIndex;
             }
         }
 
         public int getDraggedSliderIndex() {
             return draggedSliderIndex;
         }
 
         private void showSliderActions(MouseEvent evt, final int sliderIndex) {
             popup = new JidePopup(); /* I18N */
             popup.setOwner(ImageInfoEditor.this);
             final JMenu jMenu = new JMenu();
             JMenuItem menuItem = createMenuItemAddNewSlider(sliderIndex, evt);
             if (menuItem != null) {
                 jMenu.add(menuItem);
             }
             if (getSliderCount() > 3 && sliderIndex != INVALID_INDEX) {
                 menuItem = createMenuItemDeleteSlider(sliderIndex);
                 jMenu.add(menuItem);
             }
             if (getSliderCount() > 2 && sliderIndex > 0 && sliderIndex < getSliderCount() - 1) {
                 menuItem = createMenuItemCenterSampleValue(sliderIndex);
                 jMenu.add(menuItem);
                 menuItem = createMenuItemCenterColorValue(sliderIndex);
                 jMenu.add(menuItem);
             }
             if (jMenu.getItemCount() > 0) {
                 final JPopupMenu menu = jMenu.getPopupMenu();
                 menu.setVisible(true);
                 popup.add(menu.getComponent());
                 popup.showPopup(evt.getXOnScreen(), evt.getYOnScreen());
             } else {
                 popup = null;
             }
         }
 
         private JMenuItem createMenuItemCenterColorValue(final int sliderIndex) {
             JMenuItem menuItem;
             menuItem = new JMenuItem();
             menuItem.setText("Center slider colour"); /* I18N */
             menuItem.setMnemonic('c');
             menuItem.addActionListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent actionEvent) {
                     final Color newColor = ColorPaletteDef.getCenterColor(getSliderColor(sliderIndex - 1), getSliderColor(sliderIndex + 1));
                     setSliderColor(sliderIndex, newColor);
                     hidePopup();
                 }
             });
             return menuItem;
         }
 
         private JMenuItem createMenuItemCenterSampleValue(final int sliderIndex) {
             JMenuItem menuItem = new JMenuItem();
             menuItem.setText("Center slider position"); /* I18N */
             menuItem.setMnemonic('s');
             menuItem.addActionListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent actionEvent) {
                     final double center = scale(0.5d * (scaleInverse(getSliderSample(sliderIndex - 1)) + scaleInverse(getSliderSample(sliderIndex + 1))));
                     setSliderSample(sliderIndex, center, false);
                     hidePopup();
                 }
             });
             return menuItem;
         }
 
         private JMenuItem createMenuItemDeleteSlider(final int removeIndex) {
             JMenuItem menuItem = new JMenuItem("Remove slider");
             menuItem.setMnemonic('D');
             menuItem.addActionListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent e) {
                     getModel().removeSlider(removeIndex);
                     hidePopup();
                 }
             });
             return menuItem;
         }
 
         private JMenuItem createMenuItemAddNewSlider(int insertIndex, final MouseEvent evt) {
             if (insertIndex == getModel().getSliderCount() - 1) {
                 return null;
             }
             if (insertIndex == INVALID_INDEX && isClickOutsideExistingSliders(evt.getX())) {
                 return null;
             }
             if (insertIndex == INVALID_INDEX && !isVerticalInColorBarArea(evt.getY())) {
                 return null;
             }
             if (insertIndex == INVALID_INDEX) {
                 insertIndex = getNearestLeftSliderIndex(evt.getX());
             }
             if (insertIndex == INVALID_INDEX) {
                 return null;
             }
             final int index = insertIndex;
             JMenuItem menuItem = new JMenuItem("Add new slider");
             menuItem.setMnemonic('A');
             menuItem.addActionListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent e) {
                     assert getModel() != null;
                     if (index != INVALID_INDEX && index < getModel().getSliderCount() - 1) {
                         getModel().createSliderAfter(index);
                     }
                     hidePopup();
                 }
             });
             return menuItem;
         }
 
         private boolean isClickOutsideExistingSliders(int x) {
             return x < getAbsoluteSliderPos(getFirstSliderSample()) || x > getAbsoluteSliderPos(getLastSliderSample());
         }
 
         private boolean isFirstSliderDragged() {
             return getDraggedSliderIndex() == 0;
         }
 
         private boolean isLastSliderDragged() {
             return getDraggedSliderIndex() == (getSliderCount() - 1);
         }
 
         private boolean isVerticalInColorBarArea(int y) {
             final int dy = Math.abs(paletteRect.y + PALETTE_HEIGHT / 2 - y);
             return dy < PALETTE_HEIGHT / 2;
         }
 
         private boolean isVerticalInSliderArea(int y) {
             final int dy = Math.abs(sliderBaseLineRect.y - y);
             return dy < SLIDER_HEIGHT / 2;
         }
 
 
         private int getSelectedSliderIndex(MouseEvent evt) {
             if (isVerticalInSliderArea(evt.getY())) {
                 final int sliderIndex = getNearestSliderIndex(evt.getX());
                 final double dx = Math.abs(getAbsoluteSliderPos(getSliderSample(sliderIndex)) - evt.getX());
                 if (dx < SLIDER_WIDTH / 2) {
                     return sliderIndex;
                 }
             }
             return INVALID_INDEX;
         }
 
         private int getSelectedSliderTextIndex(MouseEvent evt) {
             double dy = Math.abs(sliderTextBaseLineY + SLIDER_VALUES_AREA_HEIGHT - evt.getY());
             if (dy < SLIDER_VALUES_AREA_HEIGHT) {
                 final int sliderIndex = getNearestSliderIndex(evt.getX());
                 final double dx = Math.abs(getAbsoluteSliderPos(getSliderSample(sliderIndex)) - evt.getX());
                 if (dx < FONT_SIZE / 2) {
                     return sliderIndex;
                 }
             }
             return INVALID_INDEX;
         }
 
         private int getNearestSliderIndex(int x, int y) {
             if (isVerticalInSliderArea(y)) {
                 return getNearestSliderIndex(x);
             }
             return INVALID_INDEX;
         }
 
         private int getNearestLeftSliderIndex(int x) {
             final int index = getNearestSliderIndex(x);
             final double pos = getRelativeSliderPos(getSliderSample(index));
             if (pos > x) {
                 if (index > 0) {
                     return index - 1;
                 }
                 return INVALID_INDEX;
             }
             return index;
         }
 
         private int getNearestSliderIndex(int x) {
             int nearestIndex = INVALID_INDEX;
             double minDx = Float.MAX_VALUE;
             double dx = 0.0;
             for (int i = 0; i < getSliderCount(); i++) {
                 dx = getAbsoluteSliderPos(getSliderSample(i)) - x;
                 if (Math.abs(dx) <= minDx) {
                     nearestIndex = i;
                     minDx = Math.abs(dx);
                 }
             }
             //  Find correct index for two points at the same, last position
             if (nearestIndex == getSliderCount() - 1) {
                 final int i = getSliderCount() - 1;
                 if (getAbsoluteSliderPos(getSliderSample(i - 1)) == getAbsoluteSliderPos(getSliderSample(i))) {
                     nearestIndex = (dx <= 0.0) ? i : i - 1;
                 }
             }
             return nearestIndex;
         }
 
         private void resetState() {
             setDraggedSliderIndex(INVALID_INDEX);
             factors = null;
         }
 
     }
 
     private double scale(double value) {
         assert model != null;
         return model.getScaling().scale(value);
     }
 
     private double scaleInverse(double value) {
         assert model != null;
         return model.getScaling().scaleInverse(value);
     }
 
     private class RepaintCL implements ChangeListener {
         public void stateChanged(ChangeEvent e) {
             palette = null;
             repaint();
         }
     }
 
     private class ModelCL implements ChangeListener {
         public void stateChanged(ChangeEvent e) {
             fireStateChanged();
         }
     }
 }
