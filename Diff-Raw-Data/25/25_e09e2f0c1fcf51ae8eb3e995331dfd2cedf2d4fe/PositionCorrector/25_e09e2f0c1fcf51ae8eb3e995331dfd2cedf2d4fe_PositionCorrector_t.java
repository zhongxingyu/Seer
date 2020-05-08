 /* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2012 Colin J. Fuller
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the Software), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * ***** END LICENSE BLOCK ***** */
 
 package edu.stanford.cfuller.colocalization3d.correction;
 
 import edu.stanford.cfuller.imageanalysistools.fitting.ImageObject;
 import edu.stanford.cfuller.imageanalysistools.fitting.BisquareLinearFit;
 import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary;
 
 import edu.stanford.cfuller.colocalization3d.FileUtils;
 
 import java.util.Comparator;
 import java.util.List;
 import java.util.PriorityQueue;
 
 import org.apache.commons.math3.analysis.function.Abs;
 import org.apache.commons.math3.analysis.function.Power;
 import org.apache.commons.math3.linear.RealVector;
 import org.apache.commons.math3.linear.ArrayRealVector;
 import org.apache.commons.math3.linear.RealMatrix;
 import org.apache.commons.math3.linear.Array2DRowRealMatrix;
 import org.apache.commons.math3.linear.DecompositionSolver;
 import org.apache.commons.math3.linear.QRDecomposition;
 
 
 /**
  * This class implements methods for determining chromatic aberration corrections and applying them given sets of fitted objects in an Image.
  *
  * The correction is performed using a locally weighted mean mapping whose component values are determined by fitting quadratic functions to neighboring sets
  * of image objects.  The number of objects used for this fitting is controlled by the mandatory parameter "num_params" in the parameters file.  This
  * can be set to any value greater than or equal to the class constant numberOfCorrectionParameters, and less than the total number of objects used to
  * generate the correction.
  * 
  * @author Colin J. Fuller
  *
  */
 
 public class PositionCorrector {
 
 
 	/**
 	 * Required parameters
 	 */
 	final static String PIXELSIZE_PARAM = "pixelsize_nm";
 	final static String SECTIONSIZE_PARAM = "z_sectionsize_nm";
 	final static String NUM_POINT_PARAM = "num_params";
 	final static String REF_CH_PARAM = "reference_channel";
 	final static String CORR_CH_PARAM = "channel_to_correct";
 
 	/**
 	 * Optional parameters
 	 */
 	
 	static final String DET_CORR_PARAM = "determine_correction";
 	static final String THREAD_COUNT_PARAM = "max_threads";
 	static final String IN_SITU_ABERR_SECOND_CH_PARAM = "in_situ_aberr_corr_channel";
	static final String INVERT_Z_PARAM = "inverted_z_axis";
 	
 
 	ParameterDictionary parameters;
 
 	RealVector pixelToDistanceConversions;
 
 	final static int numberOfCorrectionParameters = 6;
 
 	/**
 	* Constructs a new PositionCorrector from a given set of analysis parameters.
 	* @param p The ParameterDictionary containing the parameters used for the analysis.
 	*/
 	public PositionCorrector(ParameterDictionary p) {
 		this.parameters = p;
 
 		this.pixelToDistanceConversions = new ArrayRealVector(3, 0.0);
 
 		pixelToDistanceConversions.setEntry(0, this.parameters.getDoubleValueForKey(PIXELSIZE_PARAM));
 		pixelToDistanceConversions.setEntry(1, this.parameters.getDoubleValueForKey(PIXELSIZE_PARAM));
 		pixelToDistanceConversions.setEntry(2, this.parameters.getDoubleValueForKey(SECTIONSIZE_PARAM));
 	}
 
 
 	/**
 	* Creates a correction from a set of objects whose positions should be the same in each channel.
 	* 
 	* @param imageObjects                  A Vector containing all the ImageObjects to be used for the correction
 	*                                      or in the order it appears in a multiwavelength image file.
 	* @return                              A Correction object that can be used to correct the positions of other objects based upon the standards provided.
 	*/
 	public Correction getCorrection(java.util.List<ImageObject> imageObjects) {
 		
 		int referenceChannel = this.parameters.getIntValueForKey(REF_CH_PARAM);
 
 		int channelToCorrect = this.parameters.getIntValueForKey(CORR_CH_PARAM);
 		
 		if (!this.parameters.hasKeyAndTrue(DET_CORR_PARAM)) {
 			try {
 				return Correction.readFromDisk(FileUtils.getCorrectionFilename(this.parameters));
 			} catch (java.io.IOException e) {
 				
 				java.util.logging.Logger.getLogger(edu.stanford.cfuller.colocalization3d.Colocalization3DMain.LOGGER_NAME).severe("Exception encountered while reading correction from disk: ");
 				e.printStackTrace();
 
 			} catch (ClassNotFoundException e) {
 
 				java.util.logging.Logger.getLogger(edu.stanford.cfuller.colocalization3d.Colocalization3DMain.LOGGER_NAME).severe("Exception encountered while reading correction from disk: ");
 				e.printStackTrace();
 
 			}
 
 			return null;
 		}
 
 
 		int numberOfPointsToFit = this.parameters.getIntValueForKey(NUM_POINT_PARAM);
 
 		RealMatrix correctionX = new Array2DRowRealMatrix(imageObjects.size(), numberOfCorrectionParameters);
 		RealMatrix correctionY = new Array2DRowRealMatrix(imageObjects.size(), numberOfCorrectionParameters);
 		RealMatrix correctionZ = new Array2DRowRealMatrix(imageObjects.size(), numberOfCorrectionParameters);
 
 		RealVector distanceCutoffs = new ArrayRealVector(imageObjects.size(), 0.0);
 
 		RealVector ones = new ArrayRealVector(numberOfPointsToFit, 1.0);
 
 
 		RealVector distancesToObjects = new ArrayRealVector(imageObjects.size(), 0.0);
 
 		RealMatrix allCorrectionParametersMatrix = new Array2DRowRealMatrix(numberOfPointsToFit, numberOfCorrectionParameters);
 
 
 		for (int i = 0; i < imageObjects.size(); i++) {
 
 			RealVector ithPos = imageObjects.get(i).getPositionForChannel(referenceChannel);
 
 			for (int j = 0; j < imageObjects.size(); j++) {
 
 				double d = imageObjects.get(j).getPositionForChannel(referenceChannel).subtract(ithPos).getNorm();
 
 				distancesToObjects.setEntry(j, d);
 
 			}
 
 			//the sorting becomes a bottleneck once the number of points gets large
 
 
 			//reverse comparator so we can use the priority queue and get the max element at the head
 
 			Comparator<Double> cdReverse = new Comparator<Double>(){
 
 				public int compare(Double o1, Double o2) {
 
 					if (o1.equals(o2)) return 0;
 					if (o1 > o2) return -1;
 					return 1;
 				}
 
 
 			};
 
 			PriorityQueue<Double> pq = new PriorityQueue<Double>(numberOfPointsToFit+2, cdReverse);
 
 			double maxElement = Double.MAX_VALUE;
 
 			for (int p = 0; p< numberOfPointsToFit+1; p++) {
 
 				pq.add(distancesToObjects.getEntry(p));
 
 			}
 
 			maxElement = pq.peek();
 
 			for (int p = numberOfPointsToFit+1; p < distancesToObjects.getDimension(); p++) {
 
 				double value = distancesToObjects.getEntry(p);
 
 				if (value < maxElement) {
 
 					pq.poll();
 
 					pq.add(value);
 
 					maxElement = pq.peek();
 
 				}
 
 			}
 
 
 
 			double firstExclude = pq.poll();
 			double lastDist = pq.poll();
 
 
 			double distanceCutoff = (lastDist + firstExclude)/2.0;
 
 			distanceCutoffs.setEntry(i, distanceCutoff);
 
 			RealVector xPositionsToFit = new ArrayRealVector(numberOfPointsToFit, 0.0);
 			RealVector yPositionsToFit = new ArrayRealVector(numberOfPointsToFit, 0.0);
 			RealVector zPositionsToFit = new ArrayRealVector(numberOfPointsToFit, 0.0);
 
 			RealMatrix differencesToFit = new Array2DRowRealMatrix(numberOfPointsToFit, imageObjects.get(0).getPositionForChannel(referenceChannel).getDimension());
 
 
 			int toFitCounter = 0;
 
 
 			for (int j = 0; j < imageObjects.size(); j++) {
 				if (distancesToObjects.getEntry(j) < distanceCutoff) {
 					xPositionsToFit.setEntry(toFitCounter, imageObjects.get(j).getPositionForChannel(referenceChannel).getEntry(0));
 					yPositionsToFit.setEntry(toFitCounter, imageObjects.get(j).getPositionForChannel(referenceChannel).getEntry(1));
 					zPositionsToFit.setEntry(toFitCounter, imageObjects.get(j).getPositionForChannel(referenceChannel).getEntry(2));
 
 					differencesToFit.setRowVector(toFitCounter, imageObjects.get(j).getVectorDifferenceBetweenChannels(referenceChannel, channelToCorrect));
 
 					toFitCounter++;
 				}
 			}
 
 			RealVector x = xPositionsToFit.mapSubtractToSelf(ithPos.getEntry(0));
 			RealVector y = yPositionsToFit.mapSubtractToSelf(ithPos.getEntry(1));
 
 
 			allCorrectionParametersMatrix.setColumnVector(0, ones);
 			allCorrectionParametersMatrix.setColumnVector(1, x);
 			allCorrectionParametersMatrix.setColumnVector(2, y);
 			allCorrectionParametersMatrix.setColumnVector(3, x.map(new Power(2)));
 			allCorrectionParametersMatrix.setColumnVector(4, y.map(new Power(2)));
 			allCorrectionParametersMatrix.setColumnVector(5, x.ebeMultiply(y));
 
 			DecompositionSolver solver = (new QRDecomposition(allCorrectionParametersMatrix)).getSolver();
 
 
 
 			RealVector cX = solver.solve(differencesToFit.getColumnVector(0));
 			RealVector cY = solver.solve(differencesToFit.getColumnVector(1));
 			RealVector cZ = solver.solve(differencesToFit.getColumnVector(2));
 
 			correctionX.setRowVector(i, cX);
 			correctionY.setRowVector(i, cY);
 			correctionZ.setRowVector(i, cZ);
 
 
 		}
 
 		Correction c =  new Correction(correctionX, correctionY, correctionZ, distanceCutoffs, imageObjects, referenceChannel, channelToCorrect);
 
 		return c;
 
 	}
 
 	/**
 	     * Applies an existing correction to the positions of a set of objects, using a specified reference
 	     * and correction channel.
 	     * 
 	     * @param imageObjects                  A Vector containing all the ImageObjects to be corrected.
 	     * @param c                             The Correction object to be used, which could have been generated with determineCorrection, or loaded from disk.
 	     * @return                              A RealVector with one entry per ImageObject, containing the corrected scalar distance between the object in the reference channel and the channel being corrected.
 	     */
 	public RealVector applyCorrection(Correction c, java.util.List<ImageObject> imageObjects) {
 		
 		int referenceChannel = this.parameters.getIntValueForKey(REF_CH_PARAM);
 
 		int channelToCorrect = this.parameters.getIntValueForKey(CORR_CH_PARAM);
 		
 		RealVector diffs = new ArrayRealVector(imageObjects.size(), 0.0);
 		RealVector averageVectorDiffs = null;
 
         for (int i = 0; i < imageObjects.size(); i++) {
 
         	diffs.setEntry(i, imageObjects.get(i).getScalarDifferenceBetweenChannels(referenceChannel, channelToCorrect, this.pixelToDistanceConversions));
 
         	if (i==0) {
         		averageVectorDiffs = imageObjects.get(i).getVectorDifferenceBetweenChannels(referenceChannel, channelToCorrect).copy();
 
         	} else {
         		averageVectorDiffs = averageVectorDiffs.add(imageObjects.get(i).getVectorDifferenceBetweenChannels(referenceChannel, channelToCorrect).map(new Abs()));
         	}
 
         }
 
 		averageVectorDiffs.mapDivideToSelf(imageObjects.size());
         averageVectorDiffs = averageVectorDiffs.ebeMultiply(this.pixelToDistanceConversions);
 
 		java.util.logging.Logger.getLogger("edu.stanford.cfuller.colocalization3d").info("mean separation uncorrected = " + diffs.getL1Norm()/diffs.getDimension());
 		java.util.logging.Logger.getLogger("edu.stanford.cfuller.colocalization3d").info("mean separation components uncorrected = " + averageVectorDiffs.toString());
 		
 		RealVector newDiffs = new ArrayRealVector(imageObjects.size(), 0.0);
 
         if (this.parameters.getBooleanValueForKey("correct_images")) {
 
 
 
             for (int i =0; i< imageObjects.size(); i++) {
 
             	try {
 
 	                newDiffs.setEntry(i, this.correctSingleObjectVectorDifference(c, imageObjects.get(i), referenceChannel, channelToCorrect).getNorm());
 
 	                imageObjects.get(i).setCorrectionSuccessful(true);
 
             	} catch (UnableToCorrectException e) {
 
             		newDiffs.setEntry(i, -1.0*Double.MAX_VALUE);
 
             		imageObjects.get(i).setCorrectionSuccessful(false);
 
             	}
 
 
             }
 
             java.util.logging.Logger.getLogger("edu.stanford.cfuller.colocalization3d").info("mean separation corrected = " + newDiffs.getL1Norm()/newDiffs.getDimension());
 
         } else {
             newDiffs = diffs;
         }
 
 
 
         return newDiffs;
 	}
 
 	private RealVector correctSingleObjectVectorDifference(Correction c, ImageObject obj, int referenceChannel, int correctionChannel) throws UnableToCorrectException {
 		
 		RealVector corr = c.correctPosition(obj.getPositionForChannel(referenceChannel).getEntry(0), obj.getPositionForChannel(referenceChannel).getEntry(1));
         boolean flip = this.parameters.getBooleanValueForKey("flip_channels_at_end");
         if (flip) corr.mapMultiplyToSelf(-1.0);
		boolean invert_z = this.parameters.hasKeyAndTrue(INVERT_Z_PARAM);
		if (invert_z) corr.setEntry(2, -1.0*corr.getEntry(2));
 		obj.applyCorrectionVectorToChannel(correctionChannel, corr);
         RealVector correctedVectorDiff = obj.getCorrectedVectorDifferenceBetweenChannels(referenceChannel, correctionChannel).ebeMultiply(this.pixelToDistanceConversions);
 		return correctedVectorDiff;
 		
 	}
 
 	/**
 	* Creates an in situ cellular aberration correction.  This will take a specified group of datasets from the parameters file and perform a robust linar fit between
 	* the aberrations in two pairs of channels (also specified in the parameters file).
 	* 
 	* @return a RealVector with three elements: the slope of the linear fit in the x, y, and z directions.
 	*/
 	public RealVector determineInSituAberrationCorrection() {
 	
 		int referenceChannel = this.parameters.getIntValueForKey(REF_CH_PARAM);
 		int inSituAberrCorrChannel = this.parameters.getIntValueForKey(IN_SITU_ABERR_SECOND_CH_PARAM);
 		int measurementChannel = this.parameters.getIntValueForKey(CORR_CH_PARAM);
 
 		List<ImageObject> iobjsForInSituAberrCorr = null;
 		
 		try {
 			iobjsForInSituAberrCorr = FileUtils.readInSituAberrCorrPositionData(this.parameters); 
 		} catch (java.io.IOException e) {
 			
 			java.util.logging.Logger.getLogger(edu.stanford.cfuller.colocalization3d.Colocalization3DMain.LOGGER_NAME).severe("Exception encountered while reading data for in situ aberration correction from disk: " + e.getMessage());
 
 		} catch (ClassNotFoundException e) {
 
 			java.util.logging.Logger.getLogger(edu.stanford.cfuller.colocalization3d.Colocalization3DMain.LOGGER_NAME).severe("Exception encountered while reading data for in situ aberration correction from disk: " + e.getMessage());
 
 		}
 		
 		RealVector xForCorr = new org.apache.commons.math3.linear.ArrayRealVector(iobjsForInSituAberrCorr.size(), 0.0);
 		RealVector xForExpt = new org.apache.commons.math3.linear.ArrayRealVector(iobjsForInSituAberrCorr.size(), 0.0);
 		RealVector yForCorr = new org.apache.commons.math3.linear.ArrayRealVector(iobjsForInSituAberrCorr.size(), 0.0);
 		RealVector yForExpt = new org.apache.commons.math3.linear.ArrayRealVector(iobjsForInSituAberrCorr.size(), 0.0);
 		RealVector zForCorr = new org.apache.commons.math3.linear.ArrayRealVector(iobjsForInSituAberrCorr.size(), 0.0);
 		RealVector zForExpt = new org.apache.commons.math3.linear.ArrayRealVector(iobjsForInSituAberrCorr.size(), 0.0);
 				
 		for (int i = 0; i < iobjsForInSituAberrCorr.size(); i++) {
 			
 		
 			ImageObject currObj = iobjsForInSituAberrCorr.get(i);
 			RealVector corrDiff = currObj.getCorrectedVectorDifferenceBetweenChannels(referenceChannel, inSituAberrCorrChannel).ebeMultiply(this.pixelToDistanceConversions);
 			RealVector exptDiff = currObj.getCorrectedVectorDifferenceBetweenChannels(referenceChannel, measurementChannel).ebeMultiply(this.pixelToDistanceConversions);
 		
 			xForCorr.setEntry(i, corrDiff.getEntry(0));
 			yForCorr.setEntry(i, corrDiff.getEntry(1));
 			zForCorr.setEntry(i, corrDiff.getEntry(2));
 			
 			xForExpt.setEntry(i, exptDiff.getEntry(0));
 			yForExpt.setEntry(i, exptDiff.getEntry(1));
 			zForExpt.setEntry(i, exptDiff.getEntry(2));
 			
 		}
 		
 		BisquareLinearFit bslf = new BisquareLinearFit();
 		
 		bslf.disableIntercept();
 		
 		RealVector xFit = bslf.fit(xForCorr, xForExpt);
 		RealVector yFit = bslf.fit(yForCorr, yForExpt);
 		RealVector zFit = bslf.fit(zForCorr, zForExpt);
 		
 		RealVector allSlopes = new org.apache.commons.math3.linear.ArrayRealVector(3, 0.0);
 		
 		allSlopes.setEntry(0, xFit.getEntry(0));
 		allSlopes.setEntry(1, yFit.getEntry(0));
 		allSlopes.setEntry(2, zFit.getEntry(0));
 		
 		return allSlopes;
 		
 		
 	}
 
 	/**
 	* Applies an in situ cellular aberration correction to a dataset.
 	* 
 	* @param toCorrect a list of ImageObjects to be corrected.
 	* @param slopes the slopes of a linear fit between the channel pair used to correct and the channel pair being corrected (as calculated by {@link #determineInSituAberrationCorrection()}, for instance).
 	* @return a List of RealVectors that are the corrected vector distances between each object's images in the channel pair being corrected.
 	*/
 	public java.util.List<RealVector> applyInSituAberrationCorrection(java.util.List<ImageObject> toCorrect, RealVector slopes) {
 		
 		int referenceChannel = this.parameters.getIntValueForKey(REF_CH_PARAM);
 		int inSituAberrCorrChannel = this.parameters.getIntValueForKey(IN_SITU_ABERR_SECOND_CH_PARAM);
 		int measurementChannel = this.parameters.getIntValueForKey(CORR_CH_PARAM);
 		
 		java.util.List<RealVector> correctedDifferences = new java.util.ArrayList<RealVector>();
 		
 		for (ImageObject iobj : toCorrect) {
 			RealVector corrDiff = iobj.getCorrectedVectorDifferenceBetweenChannels(referenceChannel, inSituAberrCorrChannel).ebeMultiply(this.pixelToDistanceConversions);
 			RealVector exptDiff = iobj.getCorrectedVectorDifferenceBetweenChannels(referenceChannel, measurementChannel).ebeMultiply(this.pixelToDistanceConversions);
 			
 			RealVector correction  = corrDiff.ebeMultiply(slopes);
 			
 			correctedDifferences.add(exptDiff.subtract(correction));
 		}
 		
 		return correctedDifferences;
 		
 	}
 
 
 	 /**
      * Determines the target registration error for a correction by successively leaving out each ImageObject in a set used to make a correction,
      * calculating a correction from the remaining objects, and assessing the error in correcting the object left out.
      * 
      * @param imageObjects                  A Vector containing all the ImageObjects to be used for the correction
      *                                      or in the order it appears in a multiwavelength image file.
      * @return                              The average value of the error over all objects.
      */
 	public double determineTRE(java.util.List<ImageObject> imageObjects) {
 
 		int referenceChannel = this.parameters.getIntValueForKey(REF_CH_PARAM);
 
 		int channelToCorrect = this.parameters.getIntValueForKey(CORR_CH_PARAM);
 		
 		RealVector treVector = new ArrayRealVector(imageObjects.size(), 0.0);
         RealVector treXYVector = new ArrayRealVector(imageObjects.size(), 0.0);
 
         java.util.Deque<TREThread> startedThreads = new java.util.LinkedList<TREThread>();
         int maxThreads = 1;
 		if (this.parameters.hasKey(THREAD_COUNT_PARAM)) {
 			maxThreads = this.parameters.getIntValueForKey(THREAD_COUNT_PARAM);
 		}
         final int threadWaitTime_ms = 1000;
 
         for (int removeIndex = 0; removeIndex < imageObjects.size(); removeIndex++) {
 
             if (removeIndex % 10 == 0) {
 
                 java.util.logging.Logger.getLogger(edu.stanford.cfuller.colocalization3d.Colocalization3DMain.LOGGER_NAME).finer("calulating TRE: point " + (removeIndex+1) + " of " + imageObjects.size());
             }
 
             TREThread nextFit = new TREThread(imageObjects, referenceChannel, channelToCorrect, removeIndex, this);
 
             if (startedThreads.size() < maxThreads) {
                 startedThreads.add(nextFit);
                 nextFit.start();
             } else {
                 TREThread next = startedThreads.poll();
                 try {
 
                     next.join(threadWaitTime_ms);
 
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
 
                 while (next.isAlive()) {
                     startedThreads.add(next);
                     next = startedThreads.poll();
 
                     try {
 
                         next.join(threadWaitTime_ms);
 
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                 }
 
                 treVector.setEntry(next.getRemoveIndex(), next.getTre());
 
                 treXYVector.setEntry(next.getRemoveIndex(), next.getTreXY());
 
                 startedThreads.add(nextFit);
                 nextFit.start();
             }
 
 
 
         }
 
         java.util.List<Integer> unsuccessful_TRE = new java.util.ArrayList<Integer>();
 
         while(startedThreads.size() > 0) {
             TREThread next = startedThreads.poll();
             try {
                 next.join();
                 if (next.getSuccess()) {
                 	treVector.setEntry(next.getRemoveIndex(), next.getTre());
                 } else {
                 	unsuccessful_TRE.add(next.getRemoveIndex());
                 }
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
 
         RealVector treVector_mod = new ArrayRealVector(treVector.getDimension() - unsuccessful_TRE.size());
         RealVector treXYVector_mod = new ArrayRealVector(treVector_mod.getDimension());
 
         int c = 0;
 
         //unsuccessful TRE calculation results when there is incomplete coverage in the correction dataset
         for (int i = 0; i < treVector.getDimension(); ++i) {
         	if (! unsuccessful_TRE.contains(i)) {
         		treVector_mod.setEntry(c, treVector.getEntry(i));
         		treXYVector_mod.setEntry(c, treXYVector.getEntry(i));
         		++c;
         	}
         }
 
         treVector = treVector_mod;
         treXYVector = treXYVector_mod;
 
 		double tre = treVector.getL1Norm()/treVector.getDimension();
 		double xy_tre = (treXYVector.getL1Norm()/treXYVector.getDimension());
 
 		java.util.logging.Logger.getLogger(edu.stanford.cfuller.colocalization3d.Colocalization3DMain.LOGGER_NAME).info("TRE: " + tre);
 		java.util.logging.Logger.getLogger(edu.stanford.cfuller.colocalization3d.Colocalization3DMain.LOGGER_NAME).info("x-y TRE: " + xy_tre);
 		
         return tre;
 
     }
 
     private static class TREThread extends Thread {
 
         java.util.List<ImageObject> imageObjects;
         int referenceChannel;
         int channelToCorrect;
         int removeIndex;
         double tre;
         double treXY;
         PositionCorrector parentCorrector;
         boolean success;
 
         public TREThread(java.util.List<ImageObject> imageObjects, int referenceChannel, int channelToCorrect, int removeIndex, PositionCorrector parentCorrector) {
 
             this.imageObjects = imageObjects;
             this.channelToCorrect = channelToCorrect;
             this.referenceChannel = referenceChannel;
             this.removeIndex = removeIndex;
             this.parentCorrector = parentCorrector;
             this.success = false;
 
         }
 
         public void run() {
 
             java.util.List<ImageObject> tempObjects = new java.util.ArrayList<ImageObject>();
 
             for (ImageObject iobj : imageObjects) {
             	tempObjects.add(iobj);
             }
 
             tempObjects.remove(removeIndex);
 
             Correction c = parentCorrector.getCorrection(tempObjects);
 
             RealVector pos = this.imageObjects.get(removeIndex).getPositionForChannel(referenceChannel);
 
             RealVector corr = null;
             try {
             	corr = c.correctPosition(pos.getEntry(0), pos.getEntry(1));
             	this.success = true;
             } catch (UnableToCorrectException e) {
             	this.success = false;
             	this.tre = -1.0*Double.MAX_VALUE;
             	this.treXY = -1.0*Double.MAX_VALUE;
             	return;
             }
 
             RealVector treVecRealUnits =  this.imageObjects.get(removeIndex).getVectorDifferenceBetweenChannels(referenceChannel, channelToCorrect).subtract(corr).ebeMultiply(parentCorrector.pixelToDistanceConversions);
 
             this.tre = treVecRealUnits.getNorm();
 
             this.treXY = Math.sqrt(Math.pow(treVecRealUnits.getEntry(0),2) + Math.pow(treVecRealUnits.getEntry(1),2));
 
 
         }
 
         public double getTre() {
             return tre;
         }
 
         public double getTreXY() {
         	return treXY;
         }
 
         public int getRemoveIndex() {
             return removeIndex;
         }
 
         public boolean getSuccess() {
         	return success;
         }
 
     }
 
 
 }
