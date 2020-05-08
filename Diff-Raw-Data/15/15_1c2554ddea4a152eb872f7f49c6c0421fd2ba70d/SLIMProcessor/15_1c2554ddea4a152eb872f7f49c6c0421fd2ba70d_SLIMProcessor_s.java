 //
 // SLIMProcessor.java
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
 
 package loci;
 
 import loci.colorizer.DataColorizer;
 
import fiji.util.gui.GenericDialogPlus;
 
 import ij.*;
 import ij.gui.*;
 import ij.gui.GenericDialog;
 import ij.gui.NonBlockingGenericDialog;
 import ij.gui.ProgressBar;
 import ij.gui.Roi;
 import ij.plugin.PlugIn;
 import ij.plugin.frame.RoiManager;
 import ij.process.*;
 
 
 import java.awt.AWTEvent;
 import java.awt.Color;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.prefs.*;
 
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import loci.common.DataTools;
 import loci.curvefitter.*;
 import loci.formats.ChannelSeparator;
 import loci.formats.FormatException;
 import loci.formats.FormatTools;
 import loci.formats.IFormatReader;
 
 /**
  * TODO
  *
  * <dl><dt><b>Source code:</b></dt>
  * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/slim-plugin/src/main/java/loci/SLIMProcessor.java">Trac</a>,
  * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/slim-plugin/src/main/java/loci/SLIMProcessor.java">SVN</a></dd></dl>
  *
  * @author Aivar Grislis grislis at wisc.edu
  */
 public class SLIMProcessor {
     // this affects how lifetimes are colorized:
     private static final double MAXIMUM_LIFETIME = 0.075; // for fitting fake with Jaolho // for fitting brian with barber triple integral 100.0f X tau vs lambda issue here
 
     // this affects how many pixels we process at once
     private static final int PIXEL_COUNT = 32;//16;
 
     // Unicode special characters
     private static final Character CHI = '\u03c7';
     private static final Character SQUARE = '\u00b2';
     private static final Character TAU = '\u03c4';
     private static final Character LAMBDA = '\u03bb';
     private static final Character SIGMA = '\u03c3';
 
     //TODO total kludge; just to get started
     private boolean m_fakeData = false;
 
     private static final String FILE_KEY = "file";
     private String m_file;
 
     IFormatReader m_reader;
 
     // Actual data values, dimensioned [channel][row][column][bin]
     protected int[][][][] m_data;
 
     private ImageProcessor m_grayscaleImageProcessor;
 
     // data parameters //TODO Curtis has these as protected
     private int m_width;
     private int m_height;
     private int[] m_cLengths;
     private int m_timeBins;
     private int m_channels;
     private int m_lifetimeIndex;
     private int m_spectraIndex;
 
     private boolean m_little;
     private int m_pixelType;
     private int m_bpp;
     private boolean m_floating;
     private float m_timeRange;
     private int m_minWave, m_waveStep, m_maxWave;
 
     // fit parameters
     private int m_numExp;
     private int m_binRadius;
     private int m_cutBins;
     private int m_maxPeak;
 
 
     public enum FitRegion {
         SUMMED, ROI, POINT, EACH
     }
 
     public enum FitAlgorithm { //TODO not really algorithm, usu. LMA
        JAOLHO, /*AKUTAN,*/ BARBER_RLD, BARBER_LMA, MARKWARDT, BARBER2_RLD, BARBER2_LMA
     }
 
     public enum FitFunction {
         SINGLE_EXPONENTIAL, DOUBLE_EXPONENTIAL, TRIPLE_EXPONENTIAL, STRETCHED_EXPONENTIAL
     }
 
     private FitRegion m_region;
     private FitAlgorithm m_algorithm;
     private FitFunction m_function;
 
     private int m_x;
     private int m_y;
 
     private double m_fitA1;
     private double m_fitT1;
     private double m_fitA2;
     private double m_fitT2;
     private double m_fitA3;
     private double m_fitT3;
     private double m_fitC;
 
     private boolean m_fitA1fixed;
     private boolean m_fitT1fixed;
     private boolean m_fitA2fixed;
     private boolean m_fitT2fixed;
     private boolean m_fitA3fixed;
     private boolean m_fitT3fixed;
     private boolean m_fitCfixed;
 
     private int m_startBin;
     private int m_stopBin;
     private int m_startX;
     private int m_threshold;
     private float m_chiSqTarget;
 
     public void run(String arg) {
         // ask for which file to load
         if (showFileDialog(getFileFromPreferences())) {
             // load the file
             if (loadFile(m_file)) {
                 if (m_fakeData) {
                     fakeData();
                 }
                 else {
                     saveFileInPreferences(m_file);
                 }
                 // show parameters from file
                 if (showParamsDialog()) {
                     if (!m_fakeData) {
                         loadData();
                     }
                     // create a grayscale image from the data
                     createGlobalGrayScale();
                     while (true) {
                         // ask what kind of fit
                         if (!showFitDialog()) {
                             break;
                         }
                         // ask for fit parameters
                         if (!showFitParamsDialog()) {
                             break;
                         }
                         fitData();
                     }
                 }
             }
             else {
                 //TODO shouldn't UI be separate?
                 IJ.error("File Error", "Unable to load file.");
             }
         }
     }
 
     private boolean showFileDialog(String defaultFile) {
         //TODO shouldn't UI be in separate class?
        GenericDialogPlus dialog = new GenericDialogPlus("Load Data");
        dialog.addFileField("File:", defaultFile, 24);
         dialog.addCheckbox("Fake data", m_fakeData);
         dialog.showDialog();
         if (dialog.wasCanceled()) {
             return false;
         }
 
         m_file = dialog.getNextString();
         m_fakeData = dialog.getNextBoolean();
 
         return true;
     }
 
     // based on loci.slim.SlimData constructor
     private boolean loadFile(String file) {
         if (m_fakeData) return true;
         boolean status = false;
         try {
             // read file header
             m_reader = new ChannelSeparator();
             m_reader.setId(file);
             m_width = m_reader.getSizeX();
             m_height = m_reader.getSizeY();
             m_cLengths = m_reader.getChannelDimLengths();
             String[] cTypes = m_reader.getChannelDimTypes();
             m_timeBins = m_channels = 1;
             m_lifetimeIndex = m_spectraIndex = -1;
             for (int i=0; i<cTypes.length; i++) {
                 if (cTypes[i].equals(FormatTools.LIFETIME)) {
                     m_timeBins = m_cLengths[i];
                     m_lifetimeIndex = i;
                 }
                 else if (cTypes[i].equals(FormatTools.SPECTRA)) {
                     m_channels = m_cLengths[i];
                     m_spectraIndex = i;
                 }
                 else if (m_lifetimeIndex < 0 && cTypes[i].equals(FormatTools.CHANNEL)) {
                     m_timeBins = m_cLengths[i];
                     m_lifetimeIndex = i;
                 }
             }
             m_little = m_reader.isLittleEndian();
             m_pixelType = m_reader.getPixelType();
             m_bpp = FormatTools.getBytesPerPixel(m_pixelType);
             m_floating = FormatTools.isFloatingPoint(m_pixelType);
 
      //TODO won't compile with my version of the jar: Number timeBase = (Number) m_reader.getGlobalMetadata().get("time base");
      //TODO fix:
             Number timeBase = null;
             m_timeRange = timeBase == null ? Float.NaN : timeBase.floatValue();
             if (m_timeRange != m_timeRange) m_timeRange = 10.0f;
             m_minWave = 400;
             m_waveStep = 10;
             m_binRadius = 3;
             status = true;
         }
         catch (Exception e) {
 
         }
         return status;
     }
 
    private boolean fakeData() {
         m_width = 50;
         m_height = 50;
         m_timeBins = 20;
         m_channels = 1;
         m_timeRange = 10.0f;
         m_minWave = 400;
         m_waveStep = 10;
 
         double A;
         double lambda;
         double b = 1.0;
 
         // show colorized lifetimes
         DataColorizer dataColorizer = new DataColorizer(m_width, m_height, "Fake Data");
         //ImageProcessor imageProcessor = new ColorProcessor(m_width, m_height);
         //ImagePlus imagePlus = new ImagePlus("Fake Data", imageProcessor);
 
         m_data = new int[m_channels][m_height][m_width][m_timeBins];
         for (int y = 0; y < m_height; ++y) {
             A = 1000.0 + y  * 10000.0; // was 1000.0; bumped up Tuesday July 27 trying to get Barber LMA to work - didn't help.
             for (int x = 0; x < m_width; ++x) {
                 double tmpX = x;
                 lambda = 0.05 + x * 0.0005d; //0.0001 + x * .001; //0.5 + x * 0.01; // .002500 + x * .01;
                 //System.out.println("lambda " + lambda + " color " + lambdaColorMap(MAXIMUM_LAMBDA, lambda));
                 dataColorizer.setData(x, y, lambda);
                 //imageProcessor.setColor(lifetimeColorMap(MAXIMUM_LIFETIME, lambda));
                 //imageProcessor.drawPixel(x, y);
                 for (int t = 0; t < m_timeBins; ++t) {
                     m_data[0][y][x][t] = (int)(A * Math.exp(-lambda * m_timeRange * t) + b);
                 }
                 //System.out.print(" " + m_data[0][y][x][0]);
                 if (5 == x && 5 == y) System.out.println("at (5, 5) A is " + A + " lambda " + lambda + " b " + b);
                 if (10 == x && 10 == y) System.out.println("at (10, 10) A is " + A + " lambda " + lambda + " b " + b);
                 if (49 == x && 49 == y) System.out.println("at (49, 49) A is " + A + " lambda " + lambda + " b " + b);
             }
             //System.out.println();
         }
         dataColorizer.update();
         return true;
     }
 
     private String getFileFromPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        return prefs.get(FILE_KEY, "");
     }
 
     private void saveFileInPreferences(String file) {
         Preferences prefs = Preferences.userNodeForPackage(this.getClass());
         prefs.put(FILE_KEY, file);
     }
 
     private boolean showParamsDialog() {
         //TODO shouldn't UI be in separate class?
         GenericDialog dialog = new GenericDialog("Parameters");
         dialog.addNumericField("Image width: ",         m_width,     0, 8, "pixels");
         dialog.addNumericField("Image height: ",        m_height,    0, 8, "pixels");
         dialog.addNumericField("Time bins: ",           m_timeBins,  0, 8, "");
         dialog.addNumericField("Channel count: ",       m_channels,  0, 8, "");
         dialog.addNumericField("Time range: ",          m_timeRange, 0, 8, "nanoseconds");
         dialog.addNumericField("Starting wavelength: ", m_minWave,   0, 8, "nanometers");
         dialog.addNumericField("Channel width: ",       m_waveStep,  0, 8, "nanometers");
         dialog.showDialog();
         if (dialog.wasCanceled()) {
             return false;
         }
         return true;
     }
 
     // based on loci.slim.SlimData constructor
     private boolean loadData() {
         boolean success = false;
         try {
             byte[] plane = new byte[m_bpp * m_height * m_width];
             m_data = new int[m_channels][m_height][m_width][m_timeBins];
             int imageCount = m_reader.getImageCount();
             for (int i=0; i<imageCount; i++) {
                 int[] zct = m_reader.getZCTCoords(i);
                 if (zct[0] != 0 || zct[2] != 0) {
                     continue; // process only first Z and T
                 }
                 int[] sub = FormatTools.rasterToPosition(m_cLengths, zct[1]);
                 int c = m_spectraIndex < 0 ? 0 : sub[m_spectraIndex];
                 int t = m_lifetimeIndex < 0 ? 0 : sub[m_lifetimeIndex];
                 m_reader.openBytes(i, plane, 0, 0, m_width, m_height);
                 for (int y=0; y<m_height; y++) {
                     for (int x=0; x<m_width; x++) {
                         int index = m_bpp * (y * m_width + x);
                         int val;
                         if (m_pixelType == FormatTools.FLOAT) {
                             val = (int) DataTools.bytesToFloat(plane, index, m_bpp, m_little);
                         }
                         else if (m_pixelType == FormatTools.DOUBLE) {
                             val = (int) DataTools.bytesToDouble(plane, index, m_bpp, m_little);
                         }
                         else if (!m_floating) {
                             val = DataTools.bytesToInt(plane, index, m_bpp, m_little);
                         }
                         else {
                             throw new FormatException("Unsupported pixel type: " +
                                 FormatTools.getPixelTypeString(m_pixelType));
                         }
                         m_data[c][y][x][t] = val;
                     }
                 }
             }
             m_reader.close();
             success = true;
         }
         catch (Exception e) {
         }
         return success;
     }
 
     private boolean createGlobalGrayScale() {
         int[][] pixels = new int[m_width][m_height];
 
         int maxPixel = 0;
         for (int x = 0; x < m_width; ++x) {
             for (int y = 0; y < m_height; ++y) {
                 pixels[x][y] = 0;
                 for (int c = 0; c < m_channels; ++c) {
                     for (int b = 0; b < m_timeBins; ++b) {
                         //System.out.println("x " + x + " y " + y + " c " + c + " b " + b);
                         pixels[x][y] += m_data[c][y][x][b];
                     }
                 }
                 if (pixels[x][y] > maxPixel) {
                     maxPixel = pixels[x][y];
                 }
             }
         }
 
         ImageProcessor imageProcessor = new ByteProcessor(m_width, m_height);
         ImagePlus imagePlus = new ImagePlus("Global GrayScale", imageProcessor);
         byte[] outPixels = (byte[]) imageProcessor.getPixels();
         for (int x = 0; x < m_width; ++x) {
             for (int y = 0; y < m_height; ++y) {
                 // flip y axis to correspond with Slim Plotter image
                 outPixels[y * m_width + x] = (byte) (pixels[x][m_height - y - 1] * 255 / maxPixel);
             }
         }
         imagePlus.show();
         m_grayscaleImageProcessor = imageProcessor; //TODO for now
         return true;
     }
 
     private boolean showFitDialog() {
         NonBlockingGenericDialog dialog = new NonBlockingGenericDialog("Fit Type");
         dialog.addChoice(
             "Region",
             new String[] { "Sum all", "Sum each ROI", "Single pixel", "Each pixel" },
             "Summed");
         dialog.addChoice(
             "Algorithm",
             new String[] { "Jaolho", /*"Akutan",*/ "old Barber RLD", "old Barber LMA", "Markwardt", "Barber NR RLD", "Barber NR LMA" },
             "Jaolho");
         dialog.addChoice(
             "Function",
             new String[] { "Single Exponential", "Double Exponential", "Triple Exponential", "Stretched Exponential" },
             "Single Exponential");
         dialog.showDialog();
         if (dialog.wasCanceled()) {
             return false;
         }
         m_region = FitRegion.class.getEnumConstants()[dialog.getNextChoiceIndex()];
         m_algorithm = FitAlgorithm.class.getEnumConstants()[dialog.getNextChoiceIndex()];
         m_function = FitFunction.class.getEnumConstants()[dialog.getNextChoiceIndex()];
         return true;
     }
 
 /*    private class MyDialogListener implements DialogListener {
         public boolean dialogItemChanged(GenericDialog dialog, AWTEvent e) {
             if (dialog.equals(m_fitDialog)) {
                 if (dialog.wasCanceled()) {
                     System.out.println("Dialgo canceled");
                     m_done = true;
                 }
                 else if (dialog.wasOKed()) {
                     System.out.println("Dialog okayed");
                     m_region = FitRegion.class.getEnumConstants()[dialog.getNextChoiceIndex()];
                     m_algorithm = FitAlgorithm.class.getEnumConstants()[dialog.getNextChoiceIndex()];
                     m_function = FitFunction.class.getEnumConstants()[dialog.getNextChoiceIndex()];
 
                     // ask for fit parameters
                     if (showFitParamsDialog()) {
                         System.out.println("ABOUT TO FIT");
                         fitData();
                         showFitDialog(); // probably not the best way to do this
                     }
                     else System.out.println("FIT PARAMS DIALOG CACNELLDE");
                 }
                 else System.out.println("NOT CANCELLED OR OKAYED<, SOMETHING ELSE");
             }
             return true;
         }
     }*///TODO unfortunately NonBlockingGenericDialog doesn't call this for OK or Cancel button!
 
     private boolean showFitParamsDialog() {
         GenericDialog dialog = new GenericDialog("Fit Params");
         if (FitRegion.POINT == m_region) {
             dialog.addNumericField("X", m_x, 0);
             dialog.addNumericField("Y", m_y, 0);
         }
         switch (m_function) {
             case SINGLE_EXPONENTIAL:
                 dialog.addNumericField("A", m_fitA1, 5);
                 dialog.addCheckbox("Fix", m_fitA1fixed);
                 dialog.addNumericField("" + LAMBDA, m_fitT1, 5);
                 dialog.addCheckbox("Fix", m_fitT1fixed);
                 dialog.addNumericField("C", m_fitC, 5);
                 dialog.addCheckbox("Fix", m_fitCfixed);
                 break;
             case DOUBLE_EXPONENTIAL:
                 dialog.addNumericField("A1", m_fitA1, 5);
                 dialog.addCheckbox("Fix", m_fitA1fixed);
                 dialog.addNumericField("" + LAMBDA + "1", m_fitT1, 5);
                 dialog.addCheckbox("Fix", m_fitT1fixed);
                 dialog.addNumericField("A2", m_fitA2, 5);
                 dialog.addCheckbox("Fix", m_fitA2fixed);
                 dialog.addNumericField("" + LAMBDA + "2", m_fitT2, 5);
                 dialog.addCheckbox("Fix", m_fitT2fixed);
                 dialog.addNumericField("C", m_fitC, 5);
                 dialog.addCheckbox("Fix", m_fitCfixed);
                 break;
             case TRIPLE_EXPONENTIAL:
                 dialog.addNumericField("A1", m_fitA1, 5);
                 dialog.addCheckbox("Fix", m_fitA1fixed);
                 dialog.addNumericField("" + LAMBDA + "1", m_fitT1, 5);
                 dialog.addCheckbox("Fix", m_fitT1fixed);
                 dialog.addNumericField("A2", m_fitA2, 5);
                 dialog.addCheckbox("Fix", m_fitA2fixed);
                 dialog.addNumericField("" + LAMBDA + "2", m_fitT2, 5);
                 dialog.addCheckbox("Fix", m_fitT2fixed);
                 dialog.addNumericField("A3", m_fitA3, 5);
                 dialog.addCheckbox("Fix", m_fitA3fixed);
                 dialog.addNumericField("" + LAMBDA + "3", m_fitT3, 5);
                 dialog.addCheckbox("Fix", m_fitT3fixed);
                 dialog.addNumericField("C", m_fitC, 5);
                 dialog.addCheckbox("Fix", m_fitCfixed);
                 break;
             case STRETCHED_EXPONENTIAL:
                 break;
         }
         if (0 == m_stopBin) {
             m_stopBin = m_timeBins - 1;
         }
         dialog.addNumericField("Start", m_startBin, 0, 2, "bins");
         dialog.addNumericField("Stop", m_stopBin, 0, 2, "bins");
         dialog.addNumericField("Start X", m_startX, 0, 2, "bins");
         dialog.addNumericField("Threshold", m_threshold, 0, 2, "photons");
         dialog.addNumericField("" + CHI + SQUARE + " Target", m_chiSqTarget, 0, 2, null);
         dialog.showDialog();
         if (dialog.wasCanceled()) {
             return false;
         }
         if (FitRegion.POINT == m_region) {
             m_x = (int) dialog.getNextNumber();
             m_y = (int) dialog.getNextNumber();
         }
         switch (m_function) {
             case SINGLE_EXPONENTIAL:
                 m_fitA1 = dialog.getNextNumber();
                 m_fitA1fixed = dialog.getNextBoolean();
                 m_fitT1 = dialog.getNextNumber();
                 m_fitT1fixed = dialog.getNextBoolean();
                 m_fitC = dialog.getNextNumber();
                 m_fitCfixed = dialog.getNextBoolean();
                 break;
             case DOUBLE_EXPONENTIAL:
                 m_fitA1 = dialog.getNextNumber();
                 m_fitA1fixed = dialog.getNextBoolean();
                 m_fitT1 = dialog.getNextNumber();
                 m_fitT1fixed = dialog.getNextBoolean();
                 m_fitA2 = dialog.getNextNumber();
                 m_fitA2fixed = dialog.getNextBoolean();
                 m_fitT2 = dialog.getNextNumber();
                 m_fitT2fixed = dialog.getNextBoolean();
                 m_fitC = dialog.getNextNumber();
                 m_fitCfixed = dialog.getNextBoolean();
                 break;
             case TRIPLE_EXPONENTIAL:
                 m_fitA1 = dialog.getNextNumber();
                 m_fitA1fixed = dialog.getNextBoolean();
                 m_fitT1 = dialog.getNextNumber();
                 m_fitT1fixed = dialog.getNextBoolean();
                 m_fitA2 = dialog.getNextNumber();
                 m_fitA2fixed = dialog.getNextBoolean();
                 m_fitT2 = dialog.getNextNumber();
                 m_fitT2fixed = dialog.getNextBoolean();
                 m_fitA3 = dialog.getNextNumber();
                 m_fitA3fixed = dialog.getNextBoolean();
                 m_fitT3 = dialog.getNextNumber();
                 m_fitT3fixed = dialog.getNextBoolean();
                 m_fitC = dialog.getNextNumber();
                 m_fitCfixed = dialog.getNextBoolean();
                break;
             case STRETCHED_EXPONENTIAL:
                 break;
         }
         m_startBin = (int) dialog.getNextNumber();
         m_stopBin  = (int) dialog.getNextNumber();
         m_startX = (int) dialog.getNextNumber();
         m_threshold = (int) dialog.getNextNumber();
       //  m_chiSqTarget = (double) dialog.getNextNumber();
         return true;
     }
 
     private void fitData() {
         if (m_region.EACH == m_region) {
             fitEachPixel();
             return;
         }
 
         // build the params
         double params[] = null;
          switch (m_function) {
             case SINGLE_EXPONENTIAL:
                 params = new double[3];
                 params[0] = m_fitA1;
                 params[1] = m_fitT1;
                 params[2] = m_fitC;
                 break;
             case DOUBLE_EXPONENTIAL:
                 params = new double[5];
                 params[0] = m_fitA1;
                 params[1] = m_fitT1;
                 params[2] = m_fitA2;
                 params[3] = m_fitT2;
                 params[4] = m_fitC;
                 break;
             case TRIPLE_EXPONENTIAL:
                 params = new double[7];
                 params[0] = m_fitA1;
                 params[1] = m_fitT1;
                 params[2] = m_fitA2;
                 params[3] = m_fitT2;
                 params[4] = m_fitA3;
                 params[5] = m_fitT3;
                 params[6] = m_fitC;
                 break;
             case STRETCHED_EXPONENTIAL:
                 break;
          }
          params[0] = m_fitA1;
          params[1] = m_fitT1;
          params[2] = m_fitC;
 
         // build the data
         ArrayList<ICurveFitData> curveFitDataList = new ArrayList<ICurveFitData>();
         ICurveFitData curveFitData;
         double yDataArray[];
         double yFitted[];
         switch (m_region) {
             case SUMMED:
                 // sum up all the photons
                 curveFitData = new CurveFitData();
                 curveFitData.setParams(params);
                 yDataArray = new double[m_timeBins];
                 for (int b = 0; b < m_timeBins; ++b) {
                     yDataArray[b] = 0.0;
                 }
                 int photons = 0;
                 for (int y = 0; y < m_height; ++y) {
                     for (int x = 0; x < m_width; ++x) {
                         for (int b = 0; b < m_timeBins; ++b) {
                             yDataArray[b] += m_data[0][y][x][b];
                             photons += m_data[0][y][x][b];
                         }
                     }
                 }
                 System.out.println("SUMMED photons " + photons);
                 curveFitData.setYData(yDataArray);
                 yFitted = new double[m_timeBins];
                 curveFitData.setYFitted(yFitted);
                 curveFitDataList.add(curveFitData);
                 break;
             case ROI:
                 int roiNumber = 0;
                 for (Roi roi: getRois()) {
                     ++roiNumber;
                     curveFitData = new CurveFitData();
                     curveFitData.setParams(params.clone());
                     yDataArray = new double[m_timeBins];
                     for (int b = 0; b < m_timeBins; ++b) {
                         yDataArray[b] = 0.0;
                     }
                     Rectangle bounds = roi.getBounds();
                     for (int x = 0; x < bounds.width; ++x) {
                         for (int y = 0; y < bounds.height; ++y) {
                             if (roi.contains(bounds.x + x, bounds.y + y)) {
                                 System.out.println("roi " + roiNumber + " x " + x + " Y " + y);
                                 for (int b = 0; b < m_timeBins; ++b) {
                                     yDataArray[b] += m_data[0][y][x][b];
                                 }
                             }
                         }
                     }
                     curveFitData.setYData(yDataArray);
                     yFitted = new double[m_timeBins];
                     curveFitData.setYFitted(yFitted);
                     curveFitDataList.add(curveFitData);
                 }
                 break;
             case POINT:
                 curveFitData = new CurveFitData();
                 curveFitData.setParams(params);
                 yDataArray = new double[m_timeBins];
                 for (int b = 0; b < m_timeBins; ++b) {
                     yDataArray[b] = m_data[0][m_height - m_y - 1][m_x][b];
                 }
                 curveFitData.setYData(yDataArray);
                 yFitted = new double[m_timeBins];
                 curveFitData.setYFitted(yFitted);
                 curveFitDataList.add(curveFitData);
                 break;
             case EACH: //TODO this is handled in fitEachPixel below
                 Roi[] rois = getRois();
                 if (0 < rois.length) {
                     for (Roi roi: rois) {
                         Rectangle bounds = roi.getBounds();
                         for (int x = 0; x < bounds.width; ++x) {
                             for (int y = 0; y < bounds.height; ++y) {
                                 if (roi.contains(bounds.x + x, bounds.y + y)) {
                                     curveFitData = new CurveFitData();
                                     curveFitData.setParams(params.clone()); //TODO if you don't clone here each pixel fit uses results of previous fit to start
                                     yDataArray = new double[m_timeBins];
                                     for (int b = 0; b < m_timeBins; ++b) {
                                         yDataArray[b] = m_data[0][y][x][b];
                                     }
                                     curveFitData.setYData(yDataArray);
                                     yFitted = new double[m_timeBins];
                                     curveFitData.setYFitted(yFitted);
                                     curveFitDataList.add(curveFitData);
                                 }
                             }
                         }
                     }
                 }
                 else {
                     for (int y = 0; y < m_height; ++y) {
                         for (int x = 0; x < m_width; ++x) {
                             curveFitData = new CurveFitData();
                             curveFitData.setParams(params.clone()); //TODO if you don't clone here each pixel fit uses results of previous fit to start
                             yDataArray = new double[m_timeBins];
                             for (int b = 0; b < m_timeBins; ++b) {
                                 yDataArray[b] = m_data[0][y][x][b];
                             }
                             curveFitData.setYData(yDataArray);
                             yFitted = new double[m_timeBins];
                             curveFitData.setYFitted(yFitted);
                             curveFitDataList.add(curveFitData);
                         }
                     }
                 }
                 break;
         }
         ICurveFitData dataArray[] = curveFitDataList.toArray(new ICurveFitData[0]);
 
         // do the fit
         ICurveFitter curveFitter = null;
         switch (m_algorithm) {
             case JAOLHO:
                 curveFitter = new JaolhoCurveFitter();
                 break;
            /* case AKUTAN:
                 curveFitter = new AkutanCurveFitter();
                 break; */
             case BARBER_RLD:
                 curveFitter = new GrayCurveFitter(0);
                 break;
             case BARBER_LMA:
                 curveFitter = new GrayCurveFitter(1);
                 break;
             case MARKWARDT:
                 curveFitter = new MarkwardtCurveFitter();
                 break;
             case BARBER2_RLD:
                 curveFitter = new GrayNRCurveFitter(0);
                 break;
             case BARBER2_LMA:
                 curveFitter = new GrayNRCurveFitter(1);
                 break;
         }
         curveFitter.setXInc(m_timeRange);
         int startBin = m_startBin + (256 * m_startX);
         curveFitter.fitData(dataArray, startBin, m_stopBin);
 
         if (0 < dataArray.length) {
             //TODO need to be able to examine any fitted pixel; for now just show the last fitted pixel. // first!
             DecayGraph decayGraph = new DecayGraph(curveFitter.getXInc(), m_startBin, m_stopBin, dataArray[0]); //dataArray.length - 1]);
             decayGraph.setStartStopListener(new MyListener());
                     JFrame window = new JFrame("SLIM");
                     JComponent component = decayGraph.getComponent();
                     window.getContentPane().add(component);
                     window.setSize(450, 450);
                     window.pack();
                     window.setVisible(true);
         }
 
         switch (m_region) {
             case SUMMED:
                 //TODO display results for summed?
                 IJ.showMessage("Summed " + dataArray[0].getParams()[0] + " " + dataArray[0].getParams()[1] + " " + dataArray[0].getParams()[2]);
                 break;
             case ROI: {
                 // show colorized lifetimes
                 ImageProcessor imageProcessor = new ColorProcessor(m_width, m_height);
                 ImagePlus imagePlus = new ImagePlus("Fitted Lifetimes", imageProcessor);
 
                 int i = 0;
                 for (Roi roi: getRois()) {
                     IJ.showMessage("Roi " + i + " " + dataArray[i].getParams()[0] + " " + dataArray[i].getParams()[1] + " " + dataArray[i].getParams()[2]);
                     double lifetime = dataArray[i++].getParams()[1];
                     imageProcessor.setColor(lifetimeColorMap(MAXIMUM_LIFETIME, lifetime));
 
                     Rectangle bounds = roi.getBounds();
                     for (int x = 0; x < bounds.width; ++x) {
                         for (int y = 0; y < bounds.height; ++y) {
                             if (roi.contains(bounds.x + x, bounds.y + y)) {
                                 imageProcessor.drawPixel(bounds.x + x, bounds.y + y);
                             }
                         }
                     }
                 }
                 imagePlus.show();
                 break; }
             case POINT:
                 //TODO display results for single point?
                 IJ.showMessage("Point A " + dataArray[0].getParams()[0] + " " + LAMBDA + " " + dataArray[0].getParams()[1] + " b " + dataArray[0].getParams()[2]);
                 break;
             case EACH: //TODO DEFUNCT CODE this is handled in fitEachPixel below
                 //TODO new version uses "chunky pixel effect" drawing.
                 // show colorized lifetimes
                 ImageProcessor imageProcessor = new ColorProcessor(m_width, m_height);
                 ImagePlus imagePlus = new ImagePlus("Fitted Lambdas", imageProcessor);
 
                 int i = 0;
                 Roi[] rois = getRois();
                 if (0 < rois.length) {
                     for (Roi roi: rois) {
                         Rectangle bounds = roi.getBounds();
                         for (int x = 0; x < bounds.width; ++x) {
                             for (int y = 0; y < bounds.height; ++y) {
                                 if (roi.contains(bounds.x + x,  bounds.y + y)) {
                                     double lambda = dataArray[i++].getParams()[1];
                                     imageProcessor.setColor(lifetimeColorMap(MAXIMUM_LIFETIME, lambda));
                                     imageProcessor.drawPixel(bounds.x + x, m_height - bounds.y - y - 1);
                                 }
                             }
                         }
 
                     }
                 }
                 else {
                     for (int y = 0; y < m_height; ++y) {
                         for (int x = 0; x < m_width; ++x) {
                             double lambda = dataArray[i++].getParams()[1];
                             imageProcessor.setColor(lifetimeColorMap(MAXIMUM_LIFETIME, lambda));
                             imageProcessor.drawPixel(x, m_height - y - 1);
                         }
                     }
                 }
                 imagePlus.show();
                 break;
         }
 
         if (0 < dataArray.length) {
             params = dataArray[0].getParams();
             switch (m_function) {
                 case SINGLE_EXPONENTIAL:
                     m_fitA1 = params[0];
                     m_fitT1 = params[1];
                     m_fitC  = params[2];
                     break;
                 case DOUBLE_EXPONENTIAL:
                     m_fitA1 = params[0];
                     m_fitT1 = params[1];
                     m_fitA2 = params[2];
                     m_fitT2 = params[3];
                     m_fitC  = params[4];
                     break;
                 case TRIPLE_EXPONENTIAL:
                     m_fitA1 = params[0];
                     m_fitT1 = params[1];
                     m_fitA2 = params[2];
                     m_fitT2 = params[3];
                     m_fitA3 = params[4];
                     m_fitT3 = params[5];
                     m_fitC  = params[6];
                     break;
                 case STRETCHED_EXPONENTIAL:
                     break;
             }
         }
     }
 
     int m_maxX = 0;
     int m_maxY = 0;
     int m_maxValue = -1;
 
     private void fitEachPixel() {
         // build the params
         double params[] = null;
          switch (m_function) {
             case SINGLE_EXPONENTIAL:
                 params = new double[3];
                 params[0] = m_fitA1;
                 params[1] = m_fitT1;
                 params[2] = m_fitC;
                 break;
             case DOUBLE_EXPONENTIAL:
                 params = new double[5];
                 params[0] = m_fitA1;
                 params[1] = m_fitT1;
                 params[2] = m_fitA2;
                 params[3] = m_fitT2;
                 params[4] = m_fitC;
                 break;
             case TRIPLE_EXPONENTIAL:
                 params = new double[7];
                 params[0] = m_fitA1;
                 params[1] = m_fitT1;
                 params[2] = m_fitA2;
                 params[3] = m_fitT2;
                 params[4] = m_fitA3;
                 params[5] = m_fitT3;
                 params[6] = m_fitC;
                 break;
             case STRETCHED_EXPONENTIAL:
                 break;
         }
         //TODO problem: only use predetermined params for a fixed fit?
          for (int i = 0; i < params.length; ++i) {
              params[i] = 1.0;
         }
          params[0] = m_fitA1;
          params[1] = m_fitT1;
          params[2] = m_fitC;
 
         // show colorized lifetimes
         //ImageProcessor imageProcessor = new ColorProcessor(m_width, m_height);
         //ImagePlus imagePlus = new ImagePlus("Fitted Lifetimes", imageProcessor);
         //imagePlus.show();
         DataColorizer dataColorizer = new DataColorizer(m_width, m_height, m_algorithm + " Fitted Lifetimes");
 
         // build the data
         ArrayList<ICurveFitData> curveFitDataList = new ArrayList<ICurveFitData>();
         ArrayList<ChunkyPixel> pixelList = new ArrayList<ChunkyPixel>();
         ICurveFitData curveFitData;
         double yDataArray[];
         double yFitted[];
 
         ChunkyPixelEffectIterator pixelIterator = new ChunkyPixelEffectIterator(new ChunkyPixelTableImpl(), m_width, m_height);
         
         int pixelCount = 0;
         int pixelsToProcessCount = 0;
 
         while (pixelIterator.hasNext()) {
             ++pixelCount;
             IJ.showProgress(pixelCount, m_height * m_width);
             ChunkyPixel pixel = pixelIterator.next();
             if (fitThisPixel(pixel.getX(), pixel.getY())) {
                 curveFitData = new CurveFitData();
                 curveFitData.setParams(params.clone());
                 yDataArray = new double[m_timeBins];
                 for (int b = 0; b < m_timeBins; ++b) {
                     yDataArray[b] = m_data[0][pixel.getY()][pixel.getX()][b];
                 }
                 curveFitData.setYData(yDataArray);
                 yFitted = new double[m_timeBins];
                 curveFitData.setYFitted(yFitted);
                 curveFitDataList.add(curveFitData);
                 pixelList.add(pixel);
 
 
                 if (++pixelsToProcessCount >= PIXEL_COUNT) {
                     //processPixels(imagePlus, imageProcessor, curveFitDataList.toArray(new ICurveFitData[0]), pixelList.toArray(new ChunkyPixel[0]));
                     processPixels(dataColorizer, m_height, curveFitDataList.toArray(new ICurveFitData[0]), pixelList.toArray(new ChunkyPixel[0]));
                     curveFitDataList.clear();
                     pixelList.clear();
                     pixelsToProcessCount = 0;
                 }
             }
         }
         if (0 < pixelsToProcessCount) {
             //processPixels(imagePlus, imageProcessor, curveFitDataList.toArray(new ICurveFitData[0]), pixelList.toArray(new ChunkyPixel[0]));
             processPixels(dataColorizer, m_height, curveFitDataList.toArray(new ICurveFitData[0]), pixelList.toArray(new ChunkyPixel[0]));
         }
         System.out.println("m_maxX, " + m_maxX + " m_maxY " + m_maxY + " m_maxVaue " + m_maxValue);
     }
 
 //    void processPixels(ImagePlus imagePlus, ImageProcessor imageProcessor, ICurveFitData data[], ChunkyPixel pixels[]) {
     void processPixels(DataColorizer dataColorizer, int height, ICurveFitData data[], ChunkyPixel pixels[]) {
         // do the fit
         ICurveFitter curveFitter = null;
         switch (m_algorithm) {
             case JAOLHO:
                 curveFitter = new JaolhoCurveFitter();
                 break;
             /* case AKUTAN:
                 curveFitter = new AkutanCurveFitter();
                 break; */
             case BARBER_RLD:
                 curveFitter = new GrayCurveFitter(0);
                 break;
             case BARBER_LMA:
                 curveFitter = new GrayCurveFitter(1);
                 break;
             case MARKWARDT:
                 curveFitter = new MarkwardtCurveFitter();
                 break;
             //TODO ARG this same switch statement is in 2 places!!!
             case BARBER2_RLD:
                 curveFitter = new GrayNRCurveFitter(0);
                 break;
             case BARBER2_LMA:
                 curveFitter = new GrayNRCurveFitter(1);
                 break;
         }
         curveFitter.setXInc(m_timeRange);
         int startBin = m_startBin + (256 * m_startX);
         curveFitter.fitData(data, startBin, m_stopBin);
 
         // draw as you go
         for (int i = 0; i < pixels.length; ++i) {
             ChunkyPixel pixel = pixels[i];
             double lifetime = data[i].getParams()[1];
             //TODO BUG:
             // With the table as is, you can get
             //   x   y   w   h
             //   12  15  2   1
             //   14  15  2   1
             // all within the same drawing cycle.
             // So it looks like a 4x1 slice gets drawn (it
             // is composed of two adjacent 2x1 slices with
             // potentially two different colors).
             //if (pixel.getWidth() == 2) {
             //    System.out.println("x " + pixel.getX() + " y " + pixel.getY() + " w " + pixel.getWidth() + " h " + pixel.getHeight());
             //}
             //System.out.println("w " + pixel.getWidth() + " h " + pixel.getHeight());
             //System.out.println("lifetime is " + lifetime);
             //Color color = lifetimeColorMap(MAXIMUM_LIFETIME, lifetime);
             //imageProcessor.setColor(color);
 
             boolean firstTime = true;
             for (int x = pixel.getX(); x < pixel.getX() + pixel.getWidth(); ++x) {
                 for (int y = pixel.getY(); y < pixel.getY() + pixel.getHeight(); ++y) {
                     if (fitThisPixel(x, y)) { //TODO WAS if (isIncluded(x, y))  {
                         //imageProcessor.drawPixel(x, height - y - 1);
                         dataColorizer.setData(firstTime, x, height - y - 1 , lifetime);
                         firstTime = false;
                     }
                 }
             }
         }
         dataColorizer.update();
     }
 
     boolean fitThisPixel(int x, int y) {
         if (m_threshold <= m_grayscaleImageProcessor.getPixel(x,m_height - y - 1)) {
             if (m_grayscaleImageProcessor.getPixel(x, m_height - y - 1) > m_maxValue) {
                 m_maxValue = m_grayscaleImageProcessor.getPixel(x, m_height - y - 1);
                 m_maxX = x;
                 m_maxY = y;
             }
             return isIncluded(x,y);
         }
         else {
             // System.out.println("dont fit " + m_grayscaleImageProcessor.getPixel(x, y) + " x " + x + " y " + y);
             return false;
         }
     }
 
     boolean isIncluded(int x, int y) {
         Roi[] rois = getRois();
         if (0 < rois.length) {
             for (Roi roi: rois) {
                 if (roi.contains(x, y)) {
                     return true;
                 }
             }
             return false;
         }
         else {
             return true;
         }
     }
 
     private Roi[] getRois() {
         Roi[] rois = {};
         RoiManager manager = RoiManager.getInstance();
         if (null != manager) {
             rois = manager.getRoisAsArray();
         }
         return rois;
     }
 
     private Color lifetimeColorMap(double max, double lifetime) {
         Color returnColor = Color.BLACK;
         if (lifetime > 0.0) {
             if (lifetime < max/2.0) {
                 returnColor = interpolateColor(Color.BLUE, Color.GREEN, 2.0 * lifetime / max);
             }
             else if (lifetime < max) {
                 returnColor = interpolateColor(Color.GREEN, Color.RED, 2.0 * (lifetime - max / 2.0) / max);
             }
             else returnColor = Color.RED;
         }
         return returnColor;
     }
 
     private Color interpolateColor(Color start, Color end, double blend) {
         int startRed   = start.getRed();
         int startGreen = start.getGreen();
         int startBlue  = start.getBlue();
         int endRed   = end.getRed();
         int endGreen = end.getGreen();
         int endBlue  = end.getBlue();
         int red   = interpolateColorComponent(startRed, endRed, blend);
         int green = interpolateColorComponent(startGreen, endGreen, blend);
         int blue  = interpolateColorComponent(startBlue, endBlue, blend);
         return new Color(red, green, blue);
     }
 
     private int interpolateColorComponent(int start, int end, double blend) {
         return (int)(blend * (end - start) + start);
     }
 
     private class MyListener implements IStartStopListener {
      /**
      * Listens for changes to the start and stop indices of the fit.
      *
      * @param start index
      * @param stop index inclusive
      */
        public void setStartStop(int start, int stop) {
            System.out.println("start " + start + " stop " + stop);
        }
     }
     
    /* private class MyDialogListener implements DialogListener {
         public boolean dialogItemChanged(GenericDialog dialog, AWTEvent e) {
             boolean showNextDialog = false;
             System.out.println("dialogItemChanged");
             if (null == e) {
                 showNextDialog = true;
             }
             else if (e instanceof ActionEvent) {
                 System.out.println("!>" + ((ActionEvent) e).getActionCommand() + "<");
                 System.out.println("!OK button pressed");
                 showNextDialog = true;
             }
             else {
                 
                 System.out.println("!Event " + e);
                 System.out.println(dialog.getChoices().get(0));
             }
             return true;
         }
     }*/
 }
