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
 import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary;
 import edu.stanford.cfuller.imageanalysistools.meta.parameters.Parameter;
 import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterType;
 import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadata;
 import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadataXMLWriter;
 import edu.stanford.cfuller.imageanalysistools.image.Image;
 import edu.stanford.cfuller.imageanalysistools.image.io.ImageReader;
 import edu.stanford.cfuller.imageanalysistools.method.Method;
 import edu.stanford.cfuller.imageanalysistools.metric.Measurement;
 import edu.stanford.cfuller.imageanalysistools.metric.Quantification;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.ObjectOutputStream;
 import java.io.PrintWriter;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Controls analysis done on the local machine, including routines for threading analysis, and data input and output.
  * 
  * @author Colin J. Fuller
  */
 
 public class LocalAnalysis {
 
     protected LocalAnalysis() {}
 
     private static java.util.Deque<ImageSetThread> threadPool = new java.util.LinkedList<ImageSetThread>();
 
     private static final int threadWaitTime_ms = 5000;
 
     static final String DATA_OUTPUT_DIR=AnalysisController.DATA_OUTPUT_DIR;
     static final String SERIALIZED_DATA_SUFFIX=AnalysisController.SERIALIZED_DATA_SUFFIX;
     static final String IMAGE_OUTPUT_DIR=AnalysisController.IMAGE_OUTPUT_DIR;
     static final String PARAMETER_OUTPUT_DIR=AnalysisController.PARAMETER_OUTPUT_DIR;
     static final String PARAMETER_EXTENSION = AnalysisController.PARAMETER_EXTENSION;
 
 
     /**
      * Runs the analysis on the local machine.
      *
      * The current implementation is multithreaded if specified in the parameter dictionary (and currently the default value
      * specifies as many threads as processor cores on the machine), so analysis methods should be thread safe.
      *
      * Each thread uses {@link #processFileSet(AnalysisMetadata)} to do the processing.
      *
      * @param am    The AnalysisMetadata specifying the options for the analysis.
      */
     public static void run(AnalysisMetadata am) {
 
         java.util.List<ImageSet> namedFileSets = null;
 
 		ParameterDictionary params = am.getOutputParameters();
 		
 		java.util.List<ImageSet> imageSets = null;
 
         if (params.hasKeyAndTrue("multi_wavelength_file") || ! params.hasKey("multi_wavelength_file")) {
             imageSets = DirUtils.makeMultiwavelengthFileSets(params);
         } else {
 
             imageSets = DirUtils.makeSetsOfMatchingFiles(params);
         }
 
         int maxThreads = 1;
 
         if (params.hasKey("max_threads")) {
 
             maxThreads = params.getIntValueForKey("max_threads");
 
         }
 
         for (ImageSet images : imageSets) {
 
 			AnalysisMetadata singleSetMeta = am.makeCopy();
 
 			singleSetMeta.setInputImages(images);
 
             ImageSetThread nextSet = new ImageSetThread(singleSetMeta);
 
             if (threadPool.size() < maxThreads) {
 	
                 LoggingUtilities.getLogger().info("Processing " + images.getImageNameForIndex(0));
 
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
 
                 LoggingUtilities.getLogger().info("Processing " + images.getImageNameForIndex(0));
 
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
      * This will write the output to disk to subdirectories of the directory containing the images.
      *
      * @param am    The AnalysisMetadata specifying the options for the analysis and containing the ImageSet of input images.
      * @throws java.io.IOException  if the images cannot be read or the output cannot be written to disk.
      */
     public static void processFileSet(AnalysisMetadata am) throws java.io.IOException {
 
 		ParameterDictionary params = am.getOutputParameters();
 
 		params.setValueForKey("filename", am.getInputImages().getImageNameForIndex(0));
 
 		loadImages(am);
 
         //set the numberOfChannels and channelName parameters appropriately for the multi-wavelength file
         
         params.addIfNotSet("number_of_channels", new Parameter("number_of_channels", "number_of_channels", ParameterType.INTEGER_T, am.getInputImages().getImageCount(), null));
 
         if (params.hasKeyAndTrue("process_max_intensity_projections")) {
 	
 			ImageSet images = am.getInputImages();
 
             ImageSet newImages = new ImageSet(params);
 
             for (int i = 0; i < images.size(); i++) {
 
                 Image toProject = images.getImageForIndex(i);
 
                 if (params.hasKeyAndTrue("swap_z_t")) {
                     toProject = DimensionFlipper.flipZT(toProject);
                 }
 
                 Image proj = MaximumIntensityProjection.projectImage(toProject);
 
                 newImages.addImageWithImageAndName(proj, images.getImageNameForIndex(i));
 
             }
             
             newImages.setMarkerImage(images.getMarkerIndex());
 
 			am.getInputImages().disposeImages();
 
             am.setInputImages(newImages);
         }
 
 
         Method methodToRun = Method.loadMethod(params.getValueForKey("method_name"));
 
 		am.setMethod(methodToRun);
 		
 		methodToRun.setAnalysisMetadata(am);
 
         methodToRun.go();
 
 		am.timestamp();
 
         writeDataOutput(am);
 
         try {
             writeImageOutput(am);
             writeParameterOutput(am);
         } catch (java.io.IOException e) {
             LoggingUtilities.getLogger().severe("Error while writing output masks to file; skipping write and continuing.");
             e.printStackTrace();
         }
 
         am.getInputImages().disposeImages();
         am.getOutputImages().disposeImages();
 
     }
 
     private static void loadImages(AnalysisMetadata am) throws java.io.IOException {
 
 		am.validateInputImages(true);
 
        if (am.getInputParameters().hasKeyAndTrue("multi_wavelength_file")) {
 			ImageSet split = loadSplitMutliwavelengthImages(am.getInputImages());
 			am.getInputImages().disposeImages();
 			am.setInputImages(split);
 		} else {
 			am.getInputImages().loadAllImages();
 		}
 				
 		if (am.getInputParameters().hasKey("marker_channel_index")) {
 			am.getInputImages().setMarkerImage(am.getInputParameters().getIntValueForKey("marker_channel_index"));
 		}
 
     }
 
     private static synchronized ImageSet loadSplitMutliwavelengthImages(ImageSet fileSet) throws java.io.IOException {
 
         fileSet.loadAllImages();
 
         Image multiwavelength = fileSet.getImageForIndex(0);
 
         
         java.util.List<Image> split = multiwavelength.splitChannels();
         
 
         ImageSet splitSet = new ImageSet(fileSet.getParameters());
 
         for (Image i : split) {
             splitSet.addImageWithImageAndName(i, fileSet.getImageNameForIndex(0));
         }
 		
 		splitSet.setCombinedImage(multiwavelength);
         
         return splitSet;
 
 
     }
 
     public static String generateDataOutputString(Quantification data, ParameterDictionary p) {
     	
     	StringBuilder output = new StringBuilder();
     	  
         if (data == null) {return "";}
                 
         java.util.Set<Long> regions = data.getAllRegions();
         
         List<Long> sortedRegions = new java.util.ArrayList<Long>();
         
         sortedRegions.addAll(regions);
         
         java.util.Collections.sort(sortedRegions);
         
         final String backgroundSuffix = "_background";
         
         List<String> columnHeadings = new java.util.ArrayList<String>();
         
         Map<Long, List<Measurement> > allOrderedMeasurements = new java.util.HashMap<Long, List<Measurement> >();
         
         for (Long label : sortedRegions) {
         	
         	List<Measurement> measurements = data.getAllMeasurementsForRegion(label);
         	
         	List<Measurement> intensityMeasurements = new java.util.ArrayList<Measurement>();
         	
         	List<Measurement> backgroundMeasurements = new java.util.ArrayList<Measurement>();
         	
         	List<Measurement> otherMeasurements = new java.util.ArrayList<Measurement>();
         	
         	for (Measurement m : measurements) {
         		
         		if (m.getMeasurementType() == Measurement.TYPE_INTENSITY) {
         			intensityMeasurements.add(m);
         		} else if (m.getMeasurementType() == Measurement.TYPE_BACKGROUND) {
         			backgroundMeasurements.add(m);
         		} else {
         			otherMeasurements.add(m);
         		}
         		
         	}
         	
         	java.util.Collections.sort(intensityMeasurements, new Comparator<Measurement>() {
         		public int compare(Measurement o1, Measurement o2) {
         			return o1.getMeasurementName().compareTo(o2.getMeasurementName());
         		}
         	});
         	
         	List<Measurement> orderedMeasurements = new java.util.ArrayList<Measurement>();
         	
         	for (int i = 0; i < measurements.size(); i++) {
         		orderedMeasurements.add(null);
         	}
         	
         	for (Measurement m : intensityMeasurements) {
         		
         		int i = columnHeadings.indexOf(m.getMeasurementName());
         		
         		if (i == -1) {// not found in list
         			
         			columnHeadings.add(m.getMeasurementName());
         			i = columnHeadings.size() -1;
         			
         		}
         		
         		if (i >= orderedMeasurements.size()) {
         			for (int j = orderedMeasurements.size(); j <= i; j++) {
         				orderedMeasurements.add(null);
         			}
         		}
         		
     			orderedMeasurements.set(i, m);
 
         		        		
         	}
         	
         	
         	for (Measurement m : intensityMeasurements) {
         	
         		Measurement bkg = null;
         		        		
         		for (Measurement b : backgroundMeasurements) {
         			        			
         			if (b.getMeasurementName().equals(m.getMeasurementName())) {
         				
         				bkg = b;
         				
         				break;
         				
         			}
         			
         		}
         		
         		if (bkg == null) continue;
         		
         		String backgroundName = bkg.getMeasurementName() + backgroundSuffix;
         		
         		int i = columnHeadings.indexOf(backgroundName);
         		
         		if (i == -1) {// not found in list
         			
         			columnHeadings.add(backgroundName);
         			i = columnHeadings.size() -1;
         			
         		}
         		
         		if (i >= orderedMeasurements.size()) {
         			for (int j = orderedMeasurements.size(); j <= i; j++) {
         				orderedMeasurements.add(null);
         			}
         		}
         		
     			orderedMeasurements.set(i, bkg);
         	
         	}
         	
         	for (Measurement m : otherMeasurements) {
         		
         		int i = columnHeadings.indexOf(m.getMeasurementName());
         		
         		if (i == -1) {// not found in list
         			
         			columnHeadings.add(m.getMeasurementName());
         			i = columnHeadings.size() -1;
         			
         		}
         		
         		if (i >= orderedMeasurements.size()) {
         			for (int j = orderedMeasurements.size(); j <= i; j++) {
         				orderedMeasurements.add(null);
         			}
         		}
         		
     			orderedMeasurements.set(i, m);
         		
         	}
         	
         	allOrderedMeasurements.put(label, orderedMeasurements);
         	
         	
         }
         
         output.append("region ");
         
         for (String s : columnHeadings) {
         	output.append(s);
         	output.append(" ");
         }
         output.append("\n");
         
         for (Long l : sortedRegions) {
         	List<Measurement> orderedMeasurements = allOrderedMeasurements.get(l);
         	
         	output.append("" + l + " ");
         	
         	for (Measurement m : orderedMeasurements) {
         		if (m == null) {
         			output.append("N/A ");
         		} else {
         			output.append(m.getMeasurement());
         			output.append(" ");
         		}
         	}
         	
         	output.append("\n");
         	
         }
         
         return output.toString();
     }
 
 	private static String getOutputMethodNameString(AnalysisMetadata am) {
 		
 		String longMethodName = am.getMethod().getDisplayName();
 
 		if (longMethodName == null) {
 			longMethodName = am.getMethod().getClass().getName();
 		}
 
         String[] splitMethodName = longMethodName.split("\\.");
 
         String shortMethodName = splitMethodName[splitMethodName.length - 1];
 		
 		return shortMethodName;
 		
 	}
 
 	private static String getOutputDataFileSuffix(AnalysisMetadata am) {
 		
 		String shortMethodName = getOutputMethodNameString(am);
 
         String outputSuffix= "." + shortMethodName + ".out.txt";
 
 		return outputSuffix;
 		
 	}
 	
 	private static String getOutputImageFileSuffix(AnalysisMetadata am) {
 		
 		String shortMethodName = getOutputMethodNameString(am);
 
         String outputSuffix= "." + shortMethodName + ".out.ome.tif";
 
 		return outputSuffix;
 		
 	}
 
 	private static String getOutputParameterFileSuffix(AnalysisMetadata am) {
 		
 		String shortMethodName = getOutputMethodNameString(am);
 
         String outputSuffix= "." + shortMethodName + PARAMETER_EXTENSION;
 
 		return outputSuffix;
 		
 	}
 
     private static void writeDataOutput(AnalysisMetadata am) throws java.io.IOException {
 
 		ParameterDictionary outputParams = am.getOutputParameters();
 
         final String output_dir_suffix = DATA_OUTPUT_DIR;
 
         java.io.File outputPath =  new java.io.File(outputParams.getValueForKey("local_directory") + java.io.File.separator + output_dir_suffix);
 
         if (!outputPath.exists()) {outputPath.mkdir();}
         
         java.io.File serializedOutputPath = new java.io.File(outputPath.getAbsolutePath() + java.io.File.separator + SERIALIZED_DATA_SUFFIX);
 
         if (!serializedOutputPath.exists()) {serializedOutputPath.mkdir();}
         
 		String outputFilename = ((new java.io.File(am.getInputImages().getImageNameForIndex(0))).getName()) + getOutputDataFileSuffix(am);
 
         String relativeOutputFilename = outputPath.getName() + File.separator + outputFilename;
         		
         String dataOutputFilename = outputPath.getParent() + File.separator + relativeOutputFilename;
         
         String serializedOutputFilename = serializedOutputPath.getAbsolutePath() + File.separator + outputFilename;
 		
         PrintWriter output = new PrintWriter(new FileOutputStream(dataOutputFilename));
         
         ObjectOutputStream serializedOutput = new ObjectOutputStream(new FileOutputStream(serializedOutputFilename));
 
     	Quantification data = am.getMethod().getStoredDataOutput();
         
         serializedOutput.writeObject(data);
         
         serializedOutput.close();
 
         output.write(generateDataOutputString(data, outputParams));
 
         output.close();
 
 		am.addOutputFile(dataOutputFilename);
 
     }
 
     private static void writeImageOutput(AnalysisMetadata am) throws java.io.IOException {
 
         final String output_dir_suffix = IMAGE_OUTPUT_DIR;
 
 		ParameterDictionary outputParams = am.getOutputParameters();
 
         java.io.File outputPath = new java.io.File(outputParams.getValueForKey("local_directory") + java.io.File.separator + output_dir_suffix);
 
         if (!outputPath.exists()) {outputPath.mkdir();}
 
         String[] splitMethodName = outputParams.getValueForKey("method_name").split("\\.");
 
         String shortMethodName = splitMethodName[splitMethodName.length - 1];
 
         String relativeOutputFilename = outputPath.getName() + File.separator + ((new java.io.File(am.getInputImages().getImageNameForIndex(0))).getName()) + getOutputImageFileSuffix(am);
 
         String maskOutputFilename = outputPath.getParent() + File.separator + relativeOutputFilename;
         
 		ImageSet outputImages = new ImageSet(outputParams);
 
         if (am.getMethod().getStoredImages().size() == 1) {
 
             am.getMethod().getStoredImage().writeToFile(maskOutputFilename);
 
 			outputImages.addImageWithImageAndName(am.getMethod().getStoredImage(), maskOutputFilename);
 
         } else {
 	
 			int imageCounter = 0;
 	
             for (Image i : am.getMethod().getStoredImages()) {
 	
                 String multiMaskOutputFilename = relativeOutputFilename.replace(".out.ome.tif", ".out." + Integer.toString(imageCounter) + ".ome.tif");
 				
 				String fullFilename = outputPath.getParent() + File.separator + multiMaskOutputFilename;
 				
                 i.writeToFile(fullFilename);
 
 				outputImages.addImageWithImageAndName(i, fullFilename);
 				
 				++imageCounter;
 
             }
         }
 
 		am.setOutputImages(outputImages);
 
     }
 
     private static void writeParameterOutput(AnalysisMetadata am) throws java.io.IOException{
 
         final String parameterDirectory = PARAMETER_OUTPUT_DIR;
 
 		ParameterDictionary pd = am.getOutputParameters();
 
         File outputPath = new File(pd.getValueForKey("local_directory") + File.separator + parameterDirectory);
 
         if (!outputPath.exists() ) {
             outputPath.mkdir();
         }
 
         String[] splitMethodName = pd.getValueForKey("method_name").split("\\.");
 
         String shortMethodName = splitMethodName[splitMethodName.length - 1];
 
         String parameterOutputFilename = outputPath.getAbsolutePath() + File.separator + (new File(am.getInputImages().getImageNameForIndex(0))).getName() + getOutputParameterFileSuffix(am);
 
 		(new AnalysisMetadataXMLWriter()).writeAnalysisMetadataToXMLFile(am, parameterOutputFilename);
 
     }
 
     private static class ImageSetThread extends Thread {
 
         private AnalysisMetadata am;
 
         public ImageSetThread(AnalysisMetadata am) {
             this.am = am;
         }
 
         public void run() {
             try {
                 processFileSet(am);
             } catch (java.io.IOException e) {
                 LoggingUtilities.getLogger().severe("while processing " + am.getInputImages().getImageNameForIndex(0) + ": " + e.toString());
                 e.printStackTrace();
             }
         }
 
     }
 
 
 
 }
