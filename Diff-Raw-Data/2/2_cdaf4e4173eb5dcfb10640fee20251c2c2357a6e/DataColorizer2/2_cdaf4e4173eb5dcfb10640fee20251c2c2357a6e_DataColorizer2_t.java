 //
 // DataColorizer.java
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
 
 package loci.slim.colorizer;
 
 import ij.ImagePlus;
 import ij.plugin.LutLoader;
 import ij.process.LUT;
 import ij.process.ColorProcessor;
 import ij.process.FloatProcessor;
 import ij.process.ImageProcessor;
 
 import java.awt.Color;
 import java.awt.image.IndexColorModel;
 import java.io.IOException;
 
 /**
  * Displays a colorized lifetime image and a UI that shows the lifetime
  * histogram and controls the colorization.
  *
  * <dl><dt><b>Source code:</b></dt>
  * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/colorizer/DataColorizer.java">Trac</a>,
  * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/colorizer/DataColorizer.java">SVN</a></dd></dl>
  *
  * @author Aivar Grislis grislis at wisc.edu
  */
 public class DataColorizer2 implements IColorizeRangeListener {
     final Object m_synchObject = new Object();
     int m_width;
     int m_height;
     boolean m_auto;
     double m_start;
     double m_stop;
     double m_min;
     double m_max;
     double m_workMin;
     double m_workMax;
     ImagePlus m_imagePlus;
     //////ImageProcessor m_imageProcessor;
     FloatProcessor m_imageProcessor;
     double m_histogramData[];
     int m_histogramDataIndex;
     double m_imageData[][];
     IColorize m_colorize;
     DataColorizerUI m_ui;
 
     /**
      * Constructor, creates a new imagePlus.
      *
      * @param width
      * @param height
      * @param title
      */
     public DataColorizer2(int width, int height, String title) {
         m_width = width;
         m_height = height;
         m_imageProcessor = new FloatProcessor(width, height);
         m_imagePlus = new ImagePlus(title, m_imageProcessor);
         init();
     }
 
     /**
      * Constructor, uses an existing imagePlus.
      *
      * @param imagePlus
      */
     public DataColorizer2(ImagePlus imagePlus, int foofle) {
         m_imagePlus = imagePlus;
         m_imageProcessor = (FloatProcessor) imagePlus.getProcessor();
         m_width = imagePlus.getWidth();
         m_height = imagePlus.getHeight();
         init();
     }
 
     /**
      * Common initialization.
      *
      */
     private void init() {
         IndexColorModel colorModel = null;
         try {
             colorModel = LutLoader.open("/Applications/ImageJ/luts/16_colors.lut");
         }
         catch (IOException e) {
             System.out.println("Error opening LUT " + e.getMessage());
         }
         
         m_imageProcessor.setColorModel(colorModel);
         m_imagePlus.show();
         m_histogramData = new double[m_width * m_height];
         m_histogramDataIndex = 0;
         m_imageData = new double[m_width][m_height];
         
         m_auto = true;
         m_start = m_min = m_workMin = Double.MAX_VALUE;
         m_stop = m_max = m_workMax = 0.0;
 
         //TODO just use a 256-color LUT
         m_colorize = null; //new FiveColorColorize(Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW, Color.RED); //ThreeColorColorize(Color.GREEN, Color.YELLOW, Color.RED);
         m_ui = new DataColorizerUI(m_colorize, this);
         
//TODO compile error here 12/14 when I made HistogramTool a singleton.        imagej.slim.histogram.HistogramTool histogram = new imagej.slim.histogram.HistogramTool();
         
         
     }
 
     /**
      * During the fit, sets the data for a given x, y and updates
      * the colorized version also.
      *
      * @param firstTime first pixel to represent this data value?
      * @param x
      * @param y
      * @param datum
      */
     public void setData(boolean firstTime, int x, int y, double datum) {
         synchronized (m_synchObject) {
             // we only want to count data once per actual, final pixel
             if (firstTime) {
                 //dumpInfo("before", datum);
                 // add to histogram
                 m_histogramData[m_histogramDataIndex++] = datum;
 
                 // keep track of minimum
                 if (datum < m_workMin) {
                     m_workMin = datum;
                 }
 
                 // keep track of maximum
                 if (datum > m_workMax) {
                     m_workMax = datum;
                 }
                 //dumpInfo("after", datum);
             }
 
             // save value for possible recolorization as color map changes
             m_imageData[x][y] = datum;
 
             // are we live?  (past the initial update cycle)
             if (m_max > 0.0) {
                 m_imageProcessor.putPixelValue(x, y, datum);
                 System.out.println("x " + x + " y " + y + " " + datum);
                 //m_imagePlus.setDisplayRange(m_min, m_max);
                 ////m_imageProcessor.setMinAndMax(m_min, m_max);
                 System.out.println("FloatProcessor min " + m_imageProcessor.getMin() + " max " + m_imageProcessor.getMax());
                 // show colorized pixel
                //////// m_imageProcessor.setColor(lookUpColor(datum));
                ///// m_imageProcessor.drawPixel(x, y);
             }
         }
     }
 
     private void dumpInfo(String title, double datum) {
         System.out.println(title + " " + datum + " wMin " + m_workMin + " wMax " + m_workMax);
     }
 
     /**
      * During the fit, signals the end of an update cycle.  The
      * histogram is recalculated and the colorized version is
      * redrawn as necessary.
      */
     public void update() {
         synchronized (m_synchObject) {
             // did minimum or maximum change?
             if (m_workMin < m_min || m_workMax > m_max) {
                 dumpInfo2("BEFORE ");
                 // first update or on automatic?
                 if (0.0 == m_max || m_auto) {
                     m_min = m_workMin;
                     m_max = m_workMax;
 
                     // handle automatic colorization
                     if (m_auto) {
                         m_start = m_min;
                         m_stop  = m_max;
                     }
 
                     // recolorize, based on new min and max, colors have shifted
                     ///////////recolorize();
                 }
                 else {
                     m_min = m_workMin;
                     m_max = m_workMax;
                 }
                 dumpInfo2("AFTER");
                 m_imageProcessor.resetMinAndMax();
                 m_imagePlus.setProcessor(m_imageProcessor.duplicate());
 
                 
                    ///////// m_imagePlus.getProcessor().setMinAndMax(m_min, m_max);  
             }
             m_ui.updateData(m_histogramData, m_min, m_max);
             /////m_imagePlus.draw();
            //////m_imageProcessor.noise(100.0);
            /////m_imageProcessor.invertLut(); m_imageProcessor.invertLut();
             System.out.println("SHOW");
             m_imagePlus.show();
         }
     }
 
     private void dumpInfo2(String title) {
         System.out.println(title + " m_min " + m_min + " m_max " + m_max);
     }
     /**
      * Listens for external changes to the range settings.
      *
      * @param auto
      * @param start
      * @param stop
      * @param min
      * @param max
      */
     public void setRange(boolean auto, double start, double stop, double min, double max) {
         boolean redo = false;
         if (auto != m_auto) {
             m_auto = auto;
         }
         if (start != m_start) {
             redo = true;
             m_start = start;
         }
         if (stop != m_stop) {
             redo = true;
             m_stop = stop;
         }
        // if (max != m_max) {
        //     redo = true;
        //     m_max = max;
        // }
         System.out.println("set display range " + redo + " " + m_start + " " + m_stop);
         if (redo) {
            // m_imagePlus.setDisplayRange(m_start, m_stop);
             /////////m_imageProcessor.setMinAndMax(m_start, m_stop);
                 System.out.println("FloatProcessor min " + m_imageProcessor.getMin() + " max " + m_imageProcessor.getMax());           
             
             
             
            // m_imageProcessor.autoThreshold();
             //m_imageProcessor.resetMinAndMax();
            ///////// m_imagePlus.createLut();
           //////////////TODO mine, no?  recolorize();
             m_imagePlus.draw();
         }
     }
 
     /**
      * Called when a fit is cancelled.
      */
     public void quit() {
         m_imagePlus.close();
         m_ui.quit();
     }
 
     /**
      * Redraws the entire colorized image.
      */
     private void recolorizeX() {
         for (int y = 0; y < m_height; ++y) {
             for (int x = 0; x < m_width; ++x) {
                 if (m_imageData[x][y] > 0.0) {
                     m_imageProcessor.setColor(lookUpColor(m_imageData[x][y]));
                     m_imageProcessor.drawPixel(x, y);
                 }
             }
         }
     }
 
     /**
      * Colorizes, converts a lifetime to a color.
      *
      * @param datum
      * @return color
      */
     private Color lookUpColor(double datum) {
         return m_colorize.colorize(m_start, m_stop, datum);
     }
 }
