 //
 // Display.java
 //
 
 /*
 SLIMPlugin for combined spectral-lifetime image analysis.
 
 Copyright (c) 2010, UW-Madison LOCI
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
     * Neither the name of the UW-Madison LOCI nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
 
 package loci.slim.analysis.plugins;
 
 import ij.ImagePlus;
 import ij.gui.MessageDialog;
 import ij.process.ColorProcessor;
 import java.awt.Color;
 
 import java.math.BigDecimal;
 import java.math.MathContext;
 import java.math.RoundingMode;
 
 import loci.slim.SLIMProcessor.FitFunction;
 import loci.slim.SLIMProcessor.FitRegion;
 import loci.slim.analysis.ISLIMAnalyzer;
 import loci.slim.analysis.SLIMAnalyzer;
 import loci.slim.colorizer.FiveColorColorize;
 import loci.slim.colorizer.IColorize;
 
 import mpicbg.imglib.cursor.LocalizableByDimCursor;
 import mpicbg.imglib.image.Image;
 import mpicbg.imglib.type.numeric.RealType;
 import mpicbg.imglib.type.numeric.real.DoubleType;
 
 /**
  * A plugin within a plugin, this is used to display the fit results.
  *
  * @author Aivar Grislis
  */
 @SLIMAnalyzer(name="Display Fit Results")
 public class Display implements ISLIMAnalyzer {
     private static final Character TAU = 'T'; //TODO IJ doesn't handle Unicode, was: = '\u03c4';
     private static final String T = "" + TAU;
     private static final String T1 = "" + TAU + '1';
     private static final String T2 = "" + TAU + '2';
     private static final String T3 = "" + TAU + '3';
     private static final String T1_T2 = "" + TAU + "1/" + TAU + '2';
     private static final String T2_T1 = "" + TAU + "2/" + TAU + '1';
     private static final String T1_T3 = "" + TAU + "1/" + TAU + '3';
     private static final String T3_T1 = "" + TAU + "3/" + TAU + '1';
     private static final String T2_T3 = "" + TAU + "2/" + TAU + '3';
     private static final String T3_T2 = "" + TAU + "3/" + TAU + '2';
     private static final String A = "A";
     private static final String A1 = "A1";
     private static final String A2 = "A2";
     private static final String A3 = "A3";
     private static final String A1_A2 = "A1/A2";
     private static final String A2_A1 = "A2/A1";
     private static final String A1_A3 = "A1/A3";
     private static final String A3_A1 = "A3/A1";
     private static final String A2_A3 = "A2/A3";
     private static final String A3_A2 = "A3/A2";
     private static final String C = "C";
 
     private static final boolean MIN = true;
     private static final boolean MAX = false;
 
     private static final int TOP_OFFSET = 20;
     private static final int SIDE_OFFSET = 10;
     private static final int BOTTOM_OFFSET = 20;
     private static final int TEXT_OFFSET = 18;
     private static final int BAR_HEIGHT = 3;
 
     private Color m_bar[];
 
     /**
      * Enum that contains the possible formulas for the values to be displayed.
     * This table is very specifically tied to the order of parameter indices.
      */
     private static enum Formula {
         T_FORMULA(T, 2),
         T1_FORMULA(T1, 2),
         T2_FORMULA(T2, 4),
         T3_FORMULA(T3, 6),
        T1_T2_FORMULA(T1_T2, 2, 4), // specifies T1/T2, parameter index 2 divided by parameter index 4
         T2_T1_FORMULA(T2_T1 ,4, 2),
         T1_T3_FORMULA(T1_T3, 2, 6),
         T3_T1_FORMULA(T3_T1, 6, 2),
         T2_T3_FORMULA(T2_T3, 4, 6),
         T3_T2_FORMULA(T3_T2, 6, 4),
         A_FORMULA(A, 1),
         A1_FORMULA(A1, 1),
         A2_FORMULA(A2, 3),
         A3_FORMULA(A3, 5),
         A1_A2_FORMULA(A1_A2, 1, 3),
         A2_A1_FORMULA(A2_A1, 3, 1),
         A1_A3_FORMULA(A1_A3, 1, 5),
         A3_A1_FORMULA(A3_A1, 5, 1),
         A2_A3_FORMULA(A2_A3, 3, 5),
         A3_A2_FORMULA(A3_A2, 5, 3),
         C_FORMULA(C, 0);
 
         // This contains the displayable name
         private final String m_name;
 
         // This contains the indices into the fitted parameters for the formula.
         private final int m_indices[];
 
         /**
          * Constructor.  Simple formula, just use a given parameter, specified
          * by index.
          *
          * @param index
          */
         private Formula(String name, int index) {
             m_name = name;
             m_indices = new int[1];
             m_indices[0] = index;
         }
 
         /**
          * Constructor.  Divisor formula, divide first parameter specified by
          * index by second parameter specified by index.
          *
          * @param dividendIndex
          * @param divisorIndex
          */
         private Formula(String name, int dividendIndex, int divisorIndex) {
             m_name = name;
             m_indices = new int[2];
             m_indices[0] = dividendIndex;
             m_indices[1] = divisorIndex;
         }
 
         /**
          * Returns the displayable String name.
          *
          * @return
          */
         private String getName() {
             return m_name;
         }
 
         /**
          * Returns an array of indices to be used in the formula.  An array
          * of one index specifies a parameter to be used as is.  An array with
          * two indices means the first parameter is divided by the second.
          *
          * @return
          */
         private int[] getIndices() {
             return m_indices;
         }
     }
 
     /**
      * Main method of ISLIMAnalyzer.
      *
      * @param image
      * @param region
      * @param function
      */
     public void analyze(Image<DoubleType> image, FitRegion region, FitFunction function) {
        boolean combineMinMax = false; //TODO needs to be set from UI.
 
         // is this plugin appropriate for current data?
         if (FitRegion.EACH != region) {
             // not appropriate
             MessageDialog dialog = new MessageDialog(null, "Display Fit Results", "Requires each pixel be fitted.");
             return;
         }
 
         // look at image dimensions
         int dimensions[] = image.getDimensions();
         //TODO for debugging only
         for (int i = 0; i < dimensions.length; ++i) {
             System.out.println("dim " + i + " " + dimensions[i]);
         }
         int xIndex = 0;
         int yIndex = 1;
         int cIndex = 2;
         int pIndex = 3;
         int width    = dimensions[xIndex];
         int height   = dimensions[yIndex];
         int channels = dimensions[cIndex];
         int params   = dimensions[pIndex];
 
         // get appropriate formula for image dimensions
         Formula formulas[] = null;
         switch (function) {
             case SINGLE_EXPONENTIAL:
                 formulas = new Formula[] { Formula.A_FORMULA, Formula.T_FORMULA, Formula.C_FORMULA }; //TODO these three formulas are just for testing.
                 break;
             case DOUBLE_EXPONENTIAL:
                 formulas = new Formula[] { Formula.A1_A2_FORMULA, Formula.T1_T2_FORMULA };
                 break;
             case TRIPLE_EXPONENTIAL:
                 formulas = new Formula[] { Formula.T1_T2_FORMULA, Formula.T1_T3_FORMULA };
                 break;
             case STRETCHED_EXPONENTIAL:
                 break;
         }
 
         // build display cells
         DisplayCell cells[][] = new DisplayCell[channels][formulas.length];
         int cellX = 0, cellY = 0;
         int cellWidth = width + 2 * SIDE_OFFSET;
         int cellHeight = height + TOP_OFFSET + BOTTOM_OFFSET;
         for (int c = 0; c < channels; ++c) {
             cellY = 0;
             for (int f = 0; f < formulas.length; ++f) {
                 cells[c][f] = new DisplayCell("" + (c + 1), formulas[f], cellX, cellY, width, height);
                 cellY += cellHeight;
             }
             cellX += cellWidth;
         }
         int totalWidth = cellX;
         int totalHeight = cellY;
 
         // traverse the image
         final LocalizableByDimCursor<?> cursor = image.createLocalizableByDimCursor();
         int dimForCursor[] = new int[4];
         double paramArray[] = new double[params];
 
         // keep track of minimum and maximum values per formula
         double minValue[] = new double[formulas.length];
         double maxValue[] = new double[formulas.length];
         initMinMax(minValue, maxValue);
 
         // get the parameters for each pixel,
         for (int c = 0; c < channels; ++c) {
             dimForCursor[cIndex] = c;
 
             for (int y = 0; y < height; ++y) {
                 dimForCursor[yIndex] = y;
 
                 for (int x = 0; x < width; ++x) {
                     dimForCursor[xIndex] = x;
 
                     // get the fitted parameters for c, y, x
                     for (int p = 0; p < params; ++p) {
                         dimForCursor[pIndex] = p;
 
                         // get the fitted parameter
                         cursor.moveTo(dimForCursor);
                         paramArray[p] = ((RealType) cursor.getType()).getRealFloat();
                     }
 
                     // calculate for this c, y, x pixel
                     for (int f = 0; f < formulas.length; ++f) {
                         // accumulate minimum and maximum results for all pixels
                         cells[c][f].setMin(minValue[f]);
                         cells[c][f].setMax(maxValue[f]);
 
                         // do the calculation
                         cells[c][f].calculate(x, y, paramArray);
 
                         minValue[f] = cells[c][f].getMin();
                         maxValue[f] = cells[c][f].getMax();
 
                     } // f loop
 
                 } // x loop
 
             } // y loop
 
             if (!combineMinMax) {
                 // save minimum and maximum results for each formula for each channel
                 for (int f = 0; f < formulas.length; ++f) {
                     Converter minConverter = new Converter(MIN, minValue[f]);
                     Converter maxConverter = new Converter(MAX, maxValue[f]);
                     double minimumValue = minConverter.getValue();
                     double maximumValue = maxConverter.getValue();
                     String minimumText = minConverter.getText();
                     String maximumText = maxConverter.getText();
 
                     cells[c][f].setMin(minimumValue);
                     cells[c][f].setMax(maximumValue);
                     cells[c][f].setMinText(minimumText);
                     cells[c][f].setMaxText(maximumText);
                 }
                 // reset minimum and maximum for each formula
                 initMinMax(minValue, maxValue);
             }
         }
 
         // display cell data
         ColorProcessor outputProcessor = new ColorProcessor(totalWidth, totalHeight);
         outputProcessor.setAntialiasedText(true);
         outputProcessor.setColor(Color.BLACK);
         outputProcessor.fill();
         ImagePlus imp = new ImagePlus("Display Results", outputProcessor);
 
         IColorize colorizer = new FiveColorColorize(Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW, Color.RED);
         for (int f = 0; f < formulas.length; ++f) {
             if (combineMinMax) {
                 // save minimum and maximum results for all channels
                 Converter minConverter = new Converter(MIN, minValue[f]);
                 Converter maxConverter = new Converter(MAX, maxValue[f]);
                 double minimumValue = minConverter.getValue();
                 double maximumValue = maxConverter.getValue();
                 String minimumText = minConverter.getText();
                 String maximumText = maxConverter.getText();
                 for (int c = 0; c < channels; ++c) {
                     cells[c][f].setMin(minimumValue);
                     cells[c][f].setMax(maximumValue);
                     cells[c][f].setMinText(minimumText);
                     cells[c][f].setMaxText(maximumText);
                     cells[c][f].display(outputProcessor, colorizer);
                 }
 
             }
             else {
                 // minimum and maximum results already saved
                 for (int c = 0; c < channels; ++c) {
                     cells[c][f].display(outputProcessor, colorizer);
                 }
             }
         }
         imp.show();
     }
 
     private void initMinMax(double minValue[], double maxValue[]) {
         for (int i = 0; i < minValue.length; ++i) {
             minValue[i] = Double.MAX_VALUE;
             maxValue[i] = 0.0;
         }
     }
 
     /**
      * Inner class.  Builds a maximum or minimum value, rounding and
      * formatting appropriately.
      */
     private class Converter {
         double m_value;
         String m_text;
 
         private Converter(boolean min, double value) {
             MathContext context = new MathContext(2, min ? RoundingMode.FLOOR : RoundingMode.CEILING);
             BigDecimal bigDecimalValue = BigDecimal.valueOf(value).round(context);
             m_value = bigDecimalValue.doubleValue();
             m_text = bigDecimalValue.toEngineeringString();
         }
 
         /**
          * Gets the rounded value.
          *
          * @return rounded value
          */
         private double getValue() {
             return m_value;
         }
 
         /**
          * Gets the formatted string representation of value.
          *
          * @return formatted string
          */
         private String getText() {
             return m_text;
         }
     }
 
 
     /**
      * Inner class.  Calculates and accumulates data for a given cell of the display.
      */
     private class DisplayCell {
         private String m_label;
         private Formula m_formula;
         private int m_x;
         private int m_y;
         private int m_width;
         private int m_height;
         private double m_value[][];
         private double m_max = 0.0;
         private double m_min = Double.MAX_VALUE;
        private String m_minText;
        private String m_maxText;
 
         /**
          * Creates a display cell.
          *
          * @param label for this channel
          * @param formula to use for calculations
          * @param x leftmost coordinate of the cell
          * @param y uppermost coordinate of the cell
          * @param width of the colorized data image
          * @param height of the colorized data image
          */
         private DisplayCell(String label, Formula formula, int x, int y, int width, int height) {
             m_label   = label;
             m_formula = formula;
             m_x       = x;
             m_y       = y;
             m_width   = width;
             m_height  = height;
             m_value = new double[width][height];
         }
 
         /**
          * Applies the cell's formula to a set of parameters for a given pixel.
          *
          * @param x of current colorized data image pixel
          * @param y of current colorized data image pixel
          * @param parameters data for this pixel
          */
         private void calculate(int x, int y, double[] parameters) {
             double result = 0.0;
             int indices[] = m_formula.getIndices();
             if (1 == indices.length) {
                 result = parameters[indices[0]];
             }
             else {
                 result = parameters[indices[0]] / parameters[indices[1]];
             }
             m_value[x][y] = result;
             if (result < m_min) {
                 m_min = result;
             }
             if (result > m_max) {
                 m_max = result;
             }
         }
 
         /**
          * Gets minimum calculated value.
          *
          * @return
          */
         private double getMin() {
             return m_min;
         }
 
         /**
          * Sets minimum value.  Used to initialize the minimum value and to
          * set the final minimum value.
          *
          * @param min
          */
         private void setMin(double min) {
             m_min = min;
         }
 
         /**
          * Sets the formatted String representation of the final minimum value.
          *
          * @param minText
          */
         private void setMinText(String minText) {
             m_minText = minText;
         }
 
         /**
          * Gets maximum calculated value.
          *
          * @return
          */
         private double getMax() {
             return m_max;
         }
 
         /**
          * Sets maximum value.  Used to initialize the maximum value and to
          * set the final maximum value.
          *
          * @param max
          */
         private void setMax(double max) {
             m_max = max;
         }
 
         /**
          * Sets the formatted String representation of the final maximum value.
          *
          * @param maxText
          */
         private void setMaxText(String maxText) {
             m_maxText = maxText;
         }
 
         /**
          * Displays the colorized data image, color bar, and labelling.
          * 
          * @param processor 
          * @param colorize
          */
         private void display(ColorProcessor processor, IColorize colorize) {
            // set up color bar in outer class one time
             if (null == m_bar) {
                m_bar = colorize.bar(m_width);
             }
 
             // label at top
             processor.setColor(Color.WHITE);
             processor.moveTo(m_x + SIDE_OFFSET, m_y + TEXT_OFFSET);
             processor.drawString(m_formula.getName());
             int width = processor.getStringWidth(m_label);
             processor.moveTo(m_x + SIDE_OFFSET + m_width - width, m_y + TEXT_OFFSET);
             processor.drawString(m_label);
 
             // draw image
             for (int x = 0; x < m_width; ++x) {
                 for (int y = 0; y < m_height; ++y) {
                     processor.setColor(colorize.colorize(m_min, m_max, m_value[x][y]));
                     processor.drawPixel(m_x + SIDE_OFFSET + x, m_y + TOP_OFFSET + y);
                 }
             }
 
             // draw color bar
             int barX = m_x + SIDE_OFFSET;
             int barY = m_y + TOP_OFFSET + m_height;
             for (int x = barX; x < barX + m_width; ++x) {
                 processor.setColor(m_bar[x - barX]);
                 for (int y = barY; y < barY + BAR_HEIGHT; ++y) {
                     processor.drawPixel(x, y);
                 }
             }
 
             // label color bar
             processor.setColor(Color.WHITE);
             processor.moveTo(barX, barY + TEXT_OFFSET);
             processor.drawString(m_minText);
             width = processor.getStringWidth(m_maxText);
             processor.moveTo(barX + m_width - width, barY + TEXT_OFFSET);
             processor.drawString(m_maxText);
         }
     }
 }
