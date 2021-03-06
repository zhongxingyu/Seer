 /*
   JWildfire - an image and animation processor written in Java 
   Copyright (C) 1995-2011 Andreas Maschke
 
   This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser 
   General Public License as published by the Free Software Foundation; either version 2.1 of the 
   License, or (at your option) any later version.
  
   This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
   even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
   Lesser General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License along with this software; 
   if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
   02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 package org.jwildfire.create.tina.render;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jwildfire.base.Tools;
 import org.jwildfire.create.tina.base.Flame;
 import org.jwildfire.create.tina.base.RasterPoint;
 import org.jwildfire.create.tina.base.XForm;
 import org.jwildfire.create.tina.base.XYZPoint;
 import org.jwildfire.create.tina.palette.RenderColor;
 import org.jwildfire.create.tina.random.RandomNumberGenerator;
 import org.jwildfire.create.tina.random.SimpleRandomNumberGenerator;
 import org.jwildfire.create.tina.swing.ProgressUpdater;
 import org.jwildfire.create.tina.variation.FlameTransformationContext;
 import org.jwildfire.create.tina.variation.Variation;
 import org.jwildfire.image.Pixel;
 import org.jwildfire.image.SimpleImage;
 import org.jwildfire.transform.ScaleAspect;
 import org.jwildfire.transform.ScaleTransformer;
 
 public class FlameRenderer implements FlameTransformationContext {
   // constants
   private final static int MAX_FILTER_WIDTH = 25;
   // init in initRaster
   private int imageWidth;
   private int imageHeight;
   int rasterWidth;
   int rasterHeight;
   private int rasterSize;
   private int borderWidth;
   private int maxBorderWidth;
   LogDensityFilter logDensityFilter;
   GammaCorrectionFilter gammaCorrectionFilter;
   RasterPoint[][] raster;
   RasterPoint[][] pass1Raster;
   // init in initView
   double cosa;
   double sina;
   double camW;
   double camH;
   double rcX;
   double rcY;
   double bws;
   double bhs;
   // init in createColorMap
   RenderColor[] colorMap;
   double paletteIdxScl;
   RandomNumberGenerator random = new SimpleRandomNumberGenerator();
   // 3D stuff
   private boolean doProject3D = false;
   // init in init3D()
   private double cameraMatrix[][] = new double[3][3];
   //
   private AffineZStyle affineZStyle = AffineZStyle.FLAT;
   private ProgressUpdater progressUpdater;
 
   private final Flame flame;
 
   public FlameRenderer(Flame pFlame) {
     flame = pFlame;
   }
 
   private void init3D() {
     double yaw = -flame.getCamYaw() * Math.PI / 180.0;
     double pitch = flame.getCamPitch() * Math.PI / 180.0;
     cameraMatrix[0][0] = Math.cos(yaw);
     cameraMatrix[1][0] = -Math.sin(yaw);
     cameraMatrix[2][0] = 0;
     cameraMatrix[0][1] = Math.cos(pitch) * Math.sin(yaw);
     cameraMatrix[1][1] = Math.cos(pitch) * Math.cos(yaw);
     cameraMatrix[2][1] = -Math.sin(pitch);
     cameraMatrix[0][2] = Math.sin(pitch) * Math.sin(yaw);
     cameraMatrix[1][2] = Math.sin(pitch) * Math.cos(yaw);
     cameraMatrix[2][2] = Math.cos(pitch);
     doProject3D = Math.abs(flame.getCamYaw()) > Tools.EPSILON || Math.abs(flame.getCamPitch()) > Tools.EPSILON || Math.abs(flame.getCamPerspective()) > Tools.EPSILON || Math.abs(flame.getCamDOF()) > Tools.EPSILON;
   }
 
   private void initRaster(SimpleImage pImage) {
     imageWidth = pImage.getImageWidth();
     imageHeight = pImage.getImageHeight();
     logDensityFilter = new LogDensityFilter(flame);
     gammaCorrectionFilter = new GammaCorrectionFilter(flame);
     maxBorderWidth = (MAX_FILTER_WIDTH - 1) / 2;
     borderWidth = (logDensityFilter.getNoiseFilterSize() - 1) / 2;
     rasterWidth = imageWidth + 2 * maxBorderWidth;
     rasterHeight = imageHeight + 2 * maxBorderWidth;
     rasterSize = rasterWidth * rasterHeight;
     raster = new RasterPoint[rasterHeight][rasterWidth];
     for (int i = 0; i < rasterHeight; i++) {
       for (int j = 0; j < rasterWidth; j++) {
         raster[i][j] = new RasterPoint();
       }
     }
   }
 
   private void createPass2Raster() {
     pass1Raster = raster;
     raster = new RasterPoint[rasterHeight][rasterWidth];
     for (int i = 0; i < rasterHeight; i++) {
       for (int j = 0; j < rasterWidth; j++) {
         raster[i][j] = new RasterPoint();
       }
     }
   }
 
   public void project(RenderPass pRenderPass, XYZPoint pPoint) {
     if (!doProject3D || !pRenderPass.equals(RenderPass.FINAL)) {
       return;
     }
     double z = pPoint.z;
     double px = cameraMatrix[0][0] * pPoint.x + cameraMatrix[1][0] * pPoint.y;
     double py = cameraMatrix[0][1] * pPoint.x + cameraMatrix[1][1] * pPoint.y + cameraMatrix[2][1] * z;
     double pz = cameraMatrix[0][2] * pPoint.x + cameraMatrix[1][2] * pPoint.y + cameraMatrix[2][2] * z;
     double zr = 1.0 - flame.getCamPerspective() * pz;
     if (Math.abs(flame.getCamDOF()) > Tools.EPSILON) {
       double a = 2.0 * Math.PI * random.random();
       double dsina = Math.sin(a);
       double dcosa = Math.cos(a);
       double zdist = (flame.getCamZ() - pz);
       double dr;
       if (zdist > 0.0) {
         dr = random.random() * flame.getCamDOF() * 0.1 * zdist;
       }
       else {
         dr = 0.0;
       }
       pPoint.x = (px + dr * dcosa) / zr;
       pPoint.y = (py + dr * dsina) / zr;
 
     }
     else {
       pPoint.x = px / zr;
       pPoint.y = py / zr;
     }
   }
 
   public void renderFlame(SimpleImage pImage, int pThreads) {
     if (flame.getXForms().size() == 0) {
       pImage.fillBackground(flame.getBGColorRed(), flame.getBGColorGreen(), flame.getBGColorBlue());
       return;
     }
     int spatialOversample = flame.getSampleDensity() >= 100.0 ? flame.getSpatialOversample() : 1;
     if (spatialOversample < 1 || spatialOversample > 6) {
       throw new IllegalArgumentException(String.valueOf(spatialOversample));
     }
     int colorOversample = flame.getSampleDensity() >= 100.0 ? flame.getColorOversample() : 1;
     if (colorOversample < 1 || colorOversample > 10) {
       throw new IllegalArgumentException(String.valueOf(colorOversample));
     }
 
     double origZoom = flame.getCamZoom();
     try {
       SimpleImage images[] = new SimpleImage[colorOversample];
       for (int i = 0; i < colorOversample; i++) {
         SimpleImage img;
         // spatial oversampling: create enlarged image
         if (spatialOversample > 1) {
           img = new SimpleImage(pImage.getImageWidth() * spatialOversample, pImage.getImageHeight() * spatialOversample);
           if (i == 0) {
             flame.setCamZoom((double) spatialOversample * flame.getCamZoom());
           }
         }
         else {
           if (colorOversample > 1) {
             img = new SimpleImage(pImage.getImageWidth(), pImage.getImageHeight());
           }
           else {
             img = pImage;
           }
         }
         images[i] = img;
         initRaster(img);
         init3D();
         createColorMap();
         initView();
         createModWeightTables();
 
         boolean twoPasses = false;
         for (XForm xForm : flame.getXForms()) {
           for (Variation var : xForm.getSortedVariations()) {
             if (var.getFunc().requiresTwoPasses()) {
               twoPasses = true;
               break;
             }
           }
         }
         if (!twoPasses && flame.getFinalXForm() != null) {
           for (Variation var : flame.getFinalXForm().getSortedVariations()) {
             if (var.getFunc().requiresTwoPasses()) {
               twoPasses = true;
               break;
             }
           }
         }
 
         if (twoPasses) {
           iterate(RenderPass.FLAT, i, colorOversample * 2, pThreads);
           createPass2Raster();
           iterate(RenderPass.FINAL, i + 1, colorOversample * 2, pThreads);
         }
         else {
           iterate(RenderPass.FINAL, i, colorOversample, pThreads);
         }
         if (flame.getSampleDensity() <= 10.0) {
           renderImageSimple(img);
         }
         else {
           renderImage(img);
         }
       }
 
       // color oversampling
       SimpleImage img;
       if (colorOversample == 1) {
         img = images[0];
       }
       else {
         int width = images[0].getImageWidth();
         int height = images[0].getImageHeight();
         if (spatialOversample > 1) {
           img = new SimpleImage(width, height);
         }
         else {
           img = pImage;
         }
         Pixel toolPixel = new Pixel();
         for (int i = 0; i < height; i++) {
           for (int j = 0; j < width; j++) {
             int r = 0, g = 0, b = 0;
             for (int k = 0; k < colorOversample; k++) {
               toolPixel.setARGBValue(images[k].getARGBValue(j, i));
               r += toolPixel.r;
               g += toolPixel.g;
               b += toolPixel.b;
             }
             toolPixel.r = Tools.roundColor((double) r / (double) colorOversample);
             toolPixel.g = Tools.roundColor((double) g / (double) colorOversample);
             toolPixel.b = Tools.roundColor((double) b / (double) colorOversample);
             img.setARGB(j, i, toolPixel.getARGBValue());
           }
         }
       }
 
       // spatial oversampling: scale down
       if (spatialOversample > 1) {
         ScaleTransformer scaleT = new ScaleTransformer();
         scaleT.setScaleWidth(pImage.getImageWidth());
         scaleT.setScaleHeight(pImage.getImageHeight());
         scaleT.setAspect(ScaleAspect.IGNORE);
         scaleT.transformImage(img);
         pImage.setBufferedImage(img.getBufferedImg(), pImage.getImageWidth(), pImage.getImageHeight());
       }
     }
     finally {
       flame.setCamZoom(origZoom);
     }
   }
 
   private void renderImage(SimpleImage pImage) {
     LogDensityPoint logDensityPnt = new LogDensityPoint();
     GammaCorrectedRGBPoint rbgPoint = new GammaCorrectedRGBPoint();
     logDensityFilter.setRaster(raster, rasterWidth, rasterHeight, pImage);
     for (int i = 0; i < pImage.getImageHeight(); i++) {
       for (int j = 0; j < pImage.getImageWidth(); j++) {
         logDensityFilter.transformPoint(logDensityPnt, j, i);
         gammaCorrectionFilter.transformPoint(logDensityPnt, rbgPoint);
         pImage.setRGB(j, i, rbgPoint.red, rbgPoint.green, rbgPoint.blue);
       }
     }
   }
 
   private void renderImageSimple(SimpleImage pImage) {
     LogDensityPoint logDensityPnt = new LogDensityPoint();
     GammaCorrectedRGBPoint rbgPoint = new GammaCorrectedRGBPoint();
     logDensityFilter.setRaster(raster, rasterWidth, rasterHeight, pImage);
     for (int i = 0; i < pImage.getImageHeight(); i++) {
       for (int j = 0; j < pImage.getImageWidth(); j++) {
         logDensityFilter.transformPointSimple(logDensityPnt, j, i);
         gammaCorrectionFilter.transformPointSimple(logDensityPnt, rbgPoint);
         pImage.setRGB(j, i, rbgPoint.red, rbgPoint.green, rbgPoint.blue);
       }
     }
   }
 
   private void iterate(RenderPass pRenderPass, int pPart, int pParts, int pThreads) {
     long nSamples = (long) ((flame.getSampleDensity() * (double) rasterSize + 0.5));
     //    if (flame.getSampleDensity() > 50) {
     //      System.err.println("SAMPLES: " + nSamples);
     //    }
     int PROGRESS_STEPS = 100;
     if (progressUpdater != null && pPart == 0) {
       progressUpdater.initProgress(PROGRESS_STEPS * pParts);
     }
     long sampleProgressUpdateStep = nSamples / 100;
     long nextProgressUpdate = sampleProgressUpdateStep;
     List<FlameRenderThread> threads = new ArrayList<FlameRenderThread>();
     for (int i = 0; i < pThreads; i++) {
       FlameRenderThread t = new FlameRenderThread(pRenderPass, this, flame, nSamples / (long) pThreads, affineZStyle);
       threads.add(t);
       new Thread(t).start();
     }
     boolean done = false;
     while (!done) {
       try {
         Thread.sleep(1);
       }
       catch (InterruptedException e) {
         e.printStackTrace();
       }
       done = true;
       long currSamples = 0;
       for (FlameRenderThread t : threads) {
         if (!t.isFinished()) {
           done = false;
         }
         currSamples += t.getCurrSample();
       }
       if (currSamples >= nextProgressUpdate) {
         if (progressUpdater != null) {
           int currProgress = (int) ((currSamples * PROGRESS_STEPS) / nSamples);
           progressUpdater.updateProgress(currProgress + pPart * PROGRESS_STEPS);
           nextProgressUpdate = (currProgress + 1) * sampleProgressUpdateStep;
         }
       }
 
     }
   }
 
   private void initView() {
     double pixelsPerUnit = flame.getPixelsPerUnit() * flame.getCamZoom();
     double corner_x = flame.getCentreX() - (double) imageWidth / pixelsPerUnit / 2.0;
     double corner_y = flame.getCentreY() - (double) imageHeight / pixelsPerUnit / 2.0;
     double t0 = borderWidth / pixelsPerUnit;
     double t1 = borderWidth / pixelsPerUnit;
     double t2 = (2 * maxBorderWidth - borderWidth) / pixelsPerUnit;
     double t3 = (2 * maxBorderWidth - borderWidth) / pixelsPerUnit;
 
     double camX0 = corner_x - t0;
     double camY0 = corner_y - t1;
     double camX1 = corner_x + (double) imageWidth / pixelsPerUnit + t2;
     double camY1 = corner_y + (double) imageHeight / pixelsPerUnit + t3;
 
     camW = camX1 - camX0;
     double Xsize, Ysize;
     if (Math.abs(camW) > 0.01)
       Xsize = 1.0 / camW;
     else
       Xsize = 1.0;
     camH = camY1 - camY0;
     if (Math.abs(camH) > 0.01)
       Ysize = 1.0 / camH;
     else
       Ysize = 1;
     bws = (rasterWidth - 0.5) * Xsize;
     bhs = (rasterHeight - 0.5) * Ysize;
 
     cosa = Math.cos(-Math.PI * (flame.getCamRoll()) / 180.0);
     sina = Math.sin(-Math.PI * (flame.getCamRoll()) / 180.0);
     rcX = flame.getCentreX() * (1 - cosa) - flame.getCentreY() * sina - camX0;
     rcY = flame.getCentreY() * (1 - cosa) + flame.getCentreX() * sina - camY0;
   }
 
   private void createModWeightTables() {
     double tp[] = new double[100];
     int n = flame.getXForms().size();
 
     for (XForm xForm : flame.getXForms()) {
       xForm.initTransform();
       for (Variation var : xForm.getSortedVariations()) {
         var.getFunc().init(this, xForm);
       }
     }
     if (flame.getFinalXForm() != null) {
       XForm xForm = flame.getFinalXForm();
       xForm.initTransform();
       for (Variation var : xForm.getSortedVariations()) {
         var.getFunc().init(this, xForm);
       }
     }
     //
     for (int k = 0; k < n; k++) {
       double totValue = 0;
       XForm xform = flame.getXForms().get(k);
       for (int l = 0; l < xform.getNextAppliedXFormTable().length; l++) {
         xform.getNextAppliedXFormTable()[l] = new XForm();
       }
       for (int i = 0; i < n; i++) {
         tp[i] = flame.getXForms().get(i).getWeight() * flame.getXForms().get(k).getModifiedWeights()[i];
         totValue = totValue + tp[i];
       }
 
       if (totValue > 0) {
         double loopValue = 0;
         for (int i = 0; i < xform.getNextAppliedXFormTable().length; i++) {
           double totalProb = 0;
           int j = -1;
           do {
             j++;
             totalProb = totalProb + tp[j];
           }
           while (!((totalProb > loopValue) || (j == n - 1)));
           xform.getNextAppliedXFormTable()[i] = flame.getXForms().get(j);
           loopValue = loopValue + totValue / (double) xform.getNextAppliedXFormTable().length;
         }
       }
       else {
         for (int i = 0; i < xform.getNextAppliedXFormTable().length - 1; i++) {
           xform.getNextAppliedXFormTable()[i] = null;
         }
       }
     }
   }
 
   private void createColorMap() {
     colorMap = flame.getPalette().createRenderPalette(flame.getWhiteLevel());
     paletteIdxScl = colorMap.length - 1;
   }
 
   public void setRandomNumberGenerator(RandomNumberGenerator random) {
     this.random = random;
   }
 
   @Override
   public RandomNumberGenerator getRandomNumberGenerator() {
     return random;
   }
 
   public void setAffineZStyle(AffineZStyle affineZStyle) {
     this.affineZStyle = affineZStyle;
   }
 
   public void setProgressUpdater(ProgressUpdater pProgressUpdater) {
     progressUpdater = pProgressUpdater;
   }
 
   public RasterPoint getRasterPoint(double pX, double pY) {
     int xIdx = (int) (bws * pX + 0.5);
     int yIdx = (int) (bhs * pY + 0.5);
     if (xIdx >= 0 && xIdx < rasterWidth && yIdx >= 0 && yIdx < rasterHeight) {
       return raster[yIdx][xIdx];
     }
     else {
       return null;
     }
   }
 
   @Override
   public RasterPoint getPass1RasterPoint(double pQX, double pQY) {
     if (pass1Raster == null) {
       return null;
     }
     double px = pQX * cosa + pQY * sina + rcX;
    if (px < 0) {
      px += camW;
    }
    if (px > camW) {
      px = 2 * camW - px;
    }
     if ((px < 0) || (px > camW))
       return null;
     double py = pQY * cosa - pQX * sina + rcY;
    if (py < 0) {
      py += camH;
    }
    if (py > camH) {
      py = 2 * camH - py;
    }
     if ((py < 0) || (py > camH))
       return null;
 
     int xIdx = (int) (bws * px + 0.5);
     int yIdx = (int) (bhs * py + 0.5);
     return pass1Raster[yIdx][xIdx];
   }
 }
