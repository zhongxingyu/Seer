 /* ***** BEGIN LICENSE BLOCK *****
  * 
  * Copyright (c) 2011 Colin J. Fuller
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  * 
  * ***** END LICENSE BLOCK ***** */
 
 package edu.stanford.cfuller.imageanalysistools.frontend;
 
 import edu.stanford.cfuller.imageanalysistools.image.DimensionFlipper;
 import edu.stanford.cfuller.imageanalysistools.image.ImageSet;
 import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;
 import edu.stanford.cfuller.imageanalysistools.image.Image;
 import edu.stanford.cfuller.imageanalysistools.image.io.ImageReader;
 import edu.stanford.cfuller.imageanalysistools.method.Method;
 import org.apache.commons.math.linear.RealMatrix;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.PrintWriter;
 
 /**
  * Controls analysis done on the local machine, including routines for threading analysis, and data input and output.
  * 
  * @author Colin J. Fuller
  */
 
 public class LocalAnalysis {
 
     protected LocalAnalysis() {}
 
     private static edu.stanford.cfuller.imageanalysistools.image.io.ImageReader reader;
 
     private static java.util.Deque<ImageSetThread> threadPool = new java.util.LinkedList<ImageSetThread>();
 
     private static final int threadWaitTime_ms = 5000;
 
     static final String DATA_OUTPUT_DIR=AnalysisController.DATA_OUTPUT_DIR;
     static final String IMAGE_OUTPUT_DIR=AnalysisController.IMAGE_OUTPUT_DIR;
     static final String PARAMETER_OUTPUT_DIR=AnalysisController.PARAMETER_OUTPUT_DIR;
     static final String PARAMETER_EXTENSION = AnalysisController.PARAMETER_EXTENSION;
 
     static{
         synchronized(LocalAnalysis.class) {
             reader = null;
         }
     }
 
     /**
      * Runs the analysis on the local machine.
      *
      * The current implementation is multithreaded if specified in the parameter dictionary (and currently the default value
      * specifies as many threads as processor cores on the machine), so analysis methods should be thread safe.
      *
      * Each thread uses {@link #processFileSet(ParameterDictionary,ImageSet)} to do the processing.
      *
      * @param params    The parameter dictionary specifying the options for the analysis.
      */
 
     public static void run(ParameterDictionary params) {
 
         java.util.List<ImageSet> namedFileSets = null;
 
        if (params.hasKeyAndTrue("multi_wavelength_file") || ! params.hasKey("multi_wavelength_file")) {
             namedFileSets = DirUtils.makeMultiwavelengthFileSets(params);
         } else {
 
             namedFileSets = DirUtils.makeSetsOfMatchingFiles(params);
         }
 
         int maxThreads = 1;
 
         if (params.hasKey("max_threads")) {
 
             maxThreads = params.getIntValueForKey("max_threads");
 
         }
 
         for (ImageSet namedFileSet : namedFileSets) {
 
 
             ImageSetThread nextSet = new ImageSetThread(namedFileSet, new ParameterDictionary(params));
 
             if (threadPool.size() < maxThreads) {
                 LoggingUtilities.getLogger().info("Processing " + namedFileSet.getImageNameForIndex(0));
 
                 threadPool.add(nextSet);
                 nextSet.start();
 
             } else  {
 
                 ImageSetThread nextInPool = threadPool.poll();
 
                 try {
                     nextInPool.join(threadWaitTime_ms);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
 
                 while(nextInPool.isAlive()) {
 
                     threadPool.add(nextInPool);
                     nextInPool = threadPool.poll();
                     try {
                         nextInPool.join(threadWaitTime_ms);
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
 
 
 
                 }
 
                 LoggingUtilities.getLogger().info("Processing " + namedFileSet.getImageNameForIndex(0));
 
                 threadPool.add(nextSet);
                 nextSet.start();
 
             }
 
 
         }
 
         while (!threadPool.isEmpty()) {
             try {
                 ImageSetThread ist = threadPool.poll();
                 ist.join();
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Processes a single set of image files, corresponding to all the wavelengths of a single 3D (or 4D XYZT image).
      *
      * If multiple wavelengths are in a single file, this needs to be specified appropriately in the parameter dictionary,
      * and an array containing the single file name should be passed as the String array parameter.  This will write the output to disk
      * to subdirectories of the directory containing the images.
      *
      * @param params    The parameter dictionary specifying the options for the analysis.
      * @param namedFileSet   An ImageSet containing the images of each color channel for a particular image. If no reference channel is given in the parameters, then the first image will be used as the reference.
      * @throws java.io.IOException  if the images cannot be read or the output cannot be written to disk.
      */
 
     public static void processFileSet(ParameterDictionary params, ImageSet namedFileSet) throws java.io.IOException {
 
 //        String[] fileSet = new String[namedFileSet.length/2];
 //        String[] fileDisplayNames = new String[namedFileSet.length/2];
 //
 //        for (int s = 0; s < fileSet.length; s++) {
 //            fileSet[s] = namedFileSet[s];
 //            fileDisplayNames[s] = namedFileSet[s + fileSet.length];
 //        }
 
 
         namedFileSet.loadAllImages();
 
         params.setValueForKey("filename", namedFileSet.getImageNameForIndex(0));
 
         //generate the images to pass to the analysis routine
 
         ImageSet images = null;
 
        if (params.hasKeyAndTrue("multi_wavelength_file") || ! params.hasKey("multi_wavelength_file")) {
 
             int markerIndex = 0;
 
             images = loadSplitMutliwavelengthImages(namedFileSet, markerIndex);
 
 
             if (params.hasKey("marker_channel_index")) {
                 markerIndex = params.getIntValueForKey("marker_channel_index");
                 images.setMarkerImage(markerIndex);
 
             }
 
 
             String channelNames = "";
 
             for (Image i : images) {
                 channelNames += i.getMetadata().getChannelName(0,0) + " ";
             }
 
             params.setValueForKey("channel_name", channelNames);
 
         } else {
 
             images = loadImagesFromFileSet(namedFileSet);
 
         }
 
         //set the numberOfChannels and channelName parameters appropriately for the multi-wavelength file
 
         if (images == null) {throw new java.io.IOException("Unable to load Image Set: " + namedFileSet.getImageNameForIndex(0));}
         
         params.addIfNotSet("number_of_channels", Integer.toString(images.getImageCount()));
 
 
         if (params.hasKeyAndTrue("process_max_intensity_projections")) {
 
             ImageSet newImages = new ImageSet(params);
 
             for (int i = 0; i < images.size(); i++) {
 
                 Image toProject = images.getImageForIndex(i);
 
                 if (params.hasKeyAndTrue("swap_z_t")) {
                     toProject = DimensionFlipper.flipZT(toProject);
                 }
 
                 Image proj = MaximumIntensityProjection.projectImage(toProject);
 
                 newImages.addImageWithImageAndName(proj, images.getImageNameForIndex(i));
 
             }
 
             images = newImages;
         }
 
 
         Method methodToRun = getMethod(params);
 
         methodToRun.setParameters(params);
         methodToRun.setImages(images);
 
         methodToRun.go();
 
         writeDataOutput(methodToRun, params, images);
 
         try {
             writeImageOutput(methodToRun,params,images);
             writeParameterOutput(params, images);
         } catch (java.io.IOException e) {
             LoggingUtilities.getLogger().severe("Error while writing output masks to file; skipping write and continuing.");
             e.printStackTrace();
         }
 
         namedFileSet.disposeImages();
         images.disposeImages();
 
     }
 
     private static ImageSet loadImagesFromFileSet(ImageSet fileSet) throws java.io.IOException {
 
         loadImagesFromFileSetWithSeriesCount(fileSet);
 
         return fileSet;
 
     }
 
     /**
      * Load the images from a specified set of filenames; if these images contain multiple XYZCT series only the first will be read in.
      * @param fileSet   An ImageSet of the images for each color channel; the reference channel for segmentation must be first (or already specified in the ImageSet).
      * @return          1.  Currently multi-series image files are not supported.
      * @throws java.io.IOException      if and error is encountered while reading the images from disk.
      */
 
     public static synchronized int loadImagesFromFileSetWithSeriesCount(ImageSet fileSet) throws java.io.IOException  {
 //
 //        if (reader == null) {
 //            reader = new ImageReader();
 //        }
 //
 //        for (String filename : fileSet) {
 //
 //            Image i = reader.read(filename);
 //
 //
 //            images.add(i);
 //        }
 //
 //        return reader.getSeriesCount(fileSet[0]);
 
         fileSet.loadAllImages();
 
         return 1;
 
     }
 
 
     private static synchronized ImageSet loadSplitMutliwavelengthImages(ImageSet fileSet, int markerIndex) throws java.io.IOException {
 
         if (reader == null) {
             reader = new ImageReader();
         }
 
         fileSet.loadAllImages();
 
         Image multiwavelength = fileSet.getImageForIndex(0);
 
         
         java.util.List<Image> split = multiwavelength.splitChannels();
         
 
         ImageSet splitSet = new ImageSet(fileSet.getParameters());
 
         for (Image i : split) {
             splitSet.addImageWithImageAndName(i, fileSet.getImageNameForIndex(0));
         }
 
         if (fileSet.getParameters().hasKey("marker_channel_index")) {
             splitSet.setMarkerImage(fileSet.getParameters().getIntValueForKey("marker_channel_index"));
         } else {
             splitSet.setMarkerImage(markerIndex);
         }
         
         return splitSet;
 
 
     }
 
 
     /**
      * Retrieves the method to run from a parameter dictionary.
      * 
      * @param params	The parameter dictionary containing information about the method to run.
      * @return			A Method object of the type specified in the parameter dictionary.
      */
     public static Method getMethod(ParameterDictionary params) {
 
         String methodName = params.getValueForKey("method_name");
         Method method = null;
 
         //methodName might be fully qualified, in which case we want to use that; otherwise, we should use the value of the parameter method_package_name
         //or fall back on a default value for that, which we will add here.
 
         final String defaultMethodPackageName = "edu.stanford.cfuller.imageanalysistools.method";
 
         if (! methodName.contains(".")) {
 
             params.addIfNotSet("method_package_name", defaultMethodPackageName);
 
             methodName = params.getValueForKey("method_package_name") + "." + methodName;
 
         }
 
         try {
             method = (Method) Class.forName(methodName).newInstance();
         } catch (ClassNotFoundException e) {
             LoggingUtilities.getLogger().severe("Could not find method: " + methodName);
             e.printStackTrace();
         } catch (InstantiationException e) {
             LoggingUtilities.getLogger().severe("Could not instantiate method: " + methodName);
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             LoggingUtilities.getLogger().severe("Could not access method constructor for: " + methodName);
             e.printStackTrace();
         }
 
         return method;
 
     }
 
     private static void writeDataOutput(Method finishedMethod, ParameterDictionary p, ImageSet fileSet) throws java.io.IOException {
 
         final String output_dir_suffix = DATA_OUTPUT_DIR;
 
         java.io.File outputPath=  new java.io.File(finishedMethod.getParameters().getValueForKey("local_directory") + java.io.File.separator + output_dir_suffix);
 
         if (!outputPath.exists()) {outputPath.mkdir();}
 
         String[] splitMethodName = finishedMethod.getParameters().getValueForKey("method_name").split("\\.");
 
         String shortMethodName = splitMethodName[splitMethodName.length - 1];
 
 
         String relativeOutputFilename = outputPath.getName() + File.separator + ((new java.io.File(fileSet.getImageNameForIndex(0))).getName()) + "." + shortMethodName + ".out.txt";
 
         String dataOutputFilename = outputPath.getParent() + File.separator + relativeOutputFilename;
 
         p.addIfNotSet("data_output_filename", relativeOutputFilename);
 
         PrintWriter output = new PrintWriter(new FileOutputStream(dataOutputFilename));
 
         RealMatrix data = finishedMethod.getStoredDataOutput();
 
         if (data == null) {output.close(); return;}
 
         for (int i = 0; i < data.getRowDimension(); i++) {
             for (int j = 0; j < data.getColumnDimension(); j++) {
                 output.print(data.getEntry(i,j));
                 output.print(" ");
             }
             output.println("");
         }
 
         output.close();
 
 
     }
 
     private static void writeImageOutput(Method finishedMethod, ParameterDictionary p, ImageSet fileSet) throws java.io.IOException {
 
         final String output_dir_suffix = IMAGE_OUTPUT_DIR;
 
         java.io.File outputPath=  new java.io.File(finishedMethod.getParameters().getValueForKey("local_directory") + java.io.File.separator + output_dir_suffix);
 
         if (!outputPath.exists()) {outputPath.mkdir();}
 
         String[] splitMethodName = finishedMethod.getParameters().getValueForKey("method_name").split("\\.");
 
         String shortMethodName = splitMethodName[splitMethodName.length - 1];
 
         String relativeOutputFilename = outputPath.getName() + File.separator + ((new java.io.File(fileSet.getImageNameForIndex(0))).getName()) + "." + shortMethodName + ".out.ome.tif";
 
         String maskOutputFilename = outputPath.getParent() + File.separator + relativeOutputFilename;
         
         if (finishedMethod.getStoredImages().size() == 1) {
 
             finishedMethod.getStoredImage().writeToFile(maskOutputFilename);
 
             p.addIfNotSet("mask_output_filename", relativeOutputFilename);
 
         } else {
             int imageCounter = 0;
             for (Image i : finishedMethod.getStoredImages()) {
                 String multiMaskOutputFilename = relativeOutputFilename.replace(".out.ome.tif", ".out." + Integer.toString(imageCounter) + ".ome.tif");
                 p.addIfNotSet("mask_output_filename", multiMaskOutputFilename);
 
                 if (imageCounter != 0) {
                     p.addIfNotSet("secondary_mask_output_filename", multiMaskOutputFilename);
                 }
                 i.writeToFile(outputPath.getParent() + File.separator + multiMaskOutputFilename);
 
                 imageCounter++;
             }
         }
 
     }
 
     private static void writeParameterOutput(ParameterDictionary p, ImageSet fileSet) throws java.io.IOException{
 
         final String parameterDirectory = PARAMETER_OUTPUT_DIR;
         final String parameterExtension = PARAMETER_EXTENSION;
 
         File outputPath = new File(p.getValueForKey("local_directory") + File.separator + parameterDirectory);
 
         if (!outputPath.exists() ) {
             outputPath.mkdir();
         }
 
         String[] splitMethodName = p.getValueForKey("method_name").split("\\.");
 
         String shortMethodName = splitMethodName[splitMethodName.length - 1];
 
         String parameterOutputFilename = outputPath.getAbsolutePath() + File.separator + (new File(fileSet.getImageNameForIndex(0))).getName() + "." + shortMethodName + parameterExtension;
 
         //java.io.ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(parameterOutputFilename));
 
         //out.writeObject(p);
 
         p.writeParametersToFile(parameterOutputFilename);
 
 
     }
 
     private static class ImageSetThread extends Thread {
 
         private ImageSet fileSet;
         private ParameterDictionary p;
 
         public ImageSetThread(ImageSet fileSet, ParameterDictionary p) {
             this.fileSet = fileSet;
             this.p = p;
         }
 
         public void run() {
             try {
                 processFileSet(p, fileSet);
             } catch (java.io.IOException e) {
                 LoggingUtilities.getLogger().severe("while processing " + fileSet.getImageNameForIndex(0) + ": " + e.toString());
                 e.printStackTrace();
             }
         }
 
     }
 
 
 
 }
