 /**
  * EFTEMj - Processing of Energy Filtering TEM images with ImageJ
  * 
  * Copyright (c) 2013, Michael Epping
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1. Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package sr_eels;
 
 import gui.ExtendedWaitForUserDialog;
 import ij.IJ;
 import ij.ImagePlus;
 import ij.gui.GenericDialog;
 import ij.gui.Line;
 import ij.gui.Roi;
 import ij.gui.Toolbar;
 import ij.measure.Calibration;
 import ij.plugin.Duplicator;
 import ij.plugin.RGBStackMerge;
 import ij.plugin.filter.ExtendedPlugInFilter;
 import ij.plugin.filter.PlugInFilterRunner;
 import ij.plugin.filter.RankFilters;
 import ij.plugin.frame.RoiManager;
 import ij.process.FloatProcessor;
 import ij.process.ImageProcessor;
 import ij.process.ImageStatistics;
 
 import java.util.Arrays;
 
 import tools.EFTEMjLogTool;
 
 /**
  * This plugin is used to correct SR-EELS data that shows a geometric aberration. This aberration is visible in SR-EELS
  * data recorded with a Zeiss in-column Omega filter. Using a Gatan post-column filter the aberration may be corrected
  * by the filter itself.
  * 
  * The correction consists of two steps. The first is to identify the inclined border of the spectrum. An automatic and
  * a manual method are available. The second step is to process the SR-EELS data to correct the aberration. The plugin
  * {@link SR_EELS_CorrectionOnlyPlugin} will perform this step.
  * 
  * For faster processing the energy loss direction has to be the y-axis. Images with the energy loss direction at the
  * x-axis will be rotated during automatic processing. To satisfy the NO_CHANGES flag a copy of the input image is
  * Rotated. The resulting image has always the orientation of the input image.
  * 
  * @author Michael Epping <michael.epping@uni-muenster.de>
  * 
  */
 public class SR_EELS_CorrectionPlugin implements ExtendedPlugInFilter {
 
     /**
      * The automatic mode will be used.
      */
     private final int AUTOMATIC = 2;
     /**
      * The manual mode will be used.
      */
     private final int MANUAL = 4;
     /**
      * The plugin will be aborted.
      */
     private final int CANCEL = 0;
     /**
      * The plugin will continue with the next step.
      */
     private final int OK = 1;
     /**
      * <code>DOES_32 | NO_CHANGES | FINAL_PROCESSING</code>
      */
     private final int FLAGS = DOES_32 | NO_CHANGES | FINAL_PROCESSING;
     /**
      * The commend that was used t run the plugin. This is used as a prefix for all dialog titles.
      */
     private String command;
     /**
      * An instance of {@link EFTEMjLogTool}.
      */
     private EFTEMjLogTool logTool;
     /**
      * The uncorrected spectrum image. No changes will be done to this {@link ImagePlus}, except a temporary rotation by
      * 90 that will be undone at the end.
      */
     private ImagePlus input;
     /**
      * An {@link ImagePlus} (this is a composite image) containing a copy of the uncorrected SR-EELS data and 1(2)
      * channels that show(s) the spectrum border (unprocessed and/or optional a linear fit).
      */
     private ImagePlus result;
     /**
      * The radius of the kernel used for the median filter. Select "Process > Filters > Show Circular Masks..." at
      * ImageJ to see all possible kernels.
      */
     private float kernelRadius = 2;
     /**
      * This is the range, in pixel, that is used to identify a local maximum.
      */
     private int localMaxRadius;
     /**
      * The number of local maximums (borders) that are regarded to find the left and right border of the Spectrum.
      */
     private int maxCount = 10;
     /**
      * This {@link Boolean} turns the linear fit of the left and right border on or off.
      */
     private boolean useLinearFit = false;
     /**
      * Each border is split into this number of intervals. At each interval a separate linear fit is done.
      */
     private int linearFitIntervals = 1;
     /**
      * If a linear fit is used, the unprocessed border can be added to the composite image.
      */
     private boolean showUnprocessedBorder = true;
     /**
      * When this value is true, an iterative process is used to automatically optimize the parameters
      * <code>kernelRadius</code> and <code>maxCount</code>.
      */
     private boolean optimizeMaxCount;
     /**
      * Determines if the image has to be rotated before and after processing.
      */
     private boolean rotate;
     private Line firstLine;
     private Line secondLine;
     private int[] firstBorder;
     private int[] secondBorder;
     private int mode;
     private boolean horizontalOrientation;
     private boolean skipCorrection;
     private String inputTitle;
     private boolean canceled;
     /**
      * the {@link Calibration} of the input stack.
      */
     private Calibration calibration;
 
     /*
      * (non-Javadoc)
      * 
      * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
      */
     @Override
     public int setup(String arg, ImagePlus imp) {
 	// When starting the plugin this method is only used to set the flags.
 	if (arg.equals("final")) {
 	    if (canceled) {
 		return DONE;
 	    }
 	    result.setTitle(inputTitle);
 	    result.setCalibration(calibration);
 	    // Select the grayscale channel.
 	    if (result.getStackSize() == 2) {
 		result.setC(2);
 	    } else if (result.getStackSize() == 3) {
 		result.setC(3);
 	    }
 	    if (rotate == true) {
 		IJ.run(result, "Rotate 90 Degrees Left", "");
 	    }
 	    // If a rotation was necessary input is a copy of the original image and is not needed any more.
 	    input = (rotate) ? null : input;
 	    result.show();
 	    logTool.showLogDialog();
 	    if (!skipCorrection) {
 		// If no rotation was necessary the input image is still locked. We have to unlock it before another
 		// plugin is started.
 		if (rotate) {
 		    input.unlock();
 		}
 		IJ.run("correct SR-EELS (no detection)");
 	    }
 	    return DONE;
 	}
 	return FLAGS;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see ij.plugin.filter.ExtendedPlugInFilter#showDialog(ij.ImagePlus, java.lang.String,
      * ij.plugin.filter.PlugInFilterRunner)
      */
     @Override
     public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
 	// I assume that the axis of the energy loss is the longer one.
 	kernelRadius = (float) (0.5 * Math.ceil(2.0 * Math.min(imp.getWidth(), imp.getHeight()) / 200));
 	maxCount = (int) Math.ceil(1.0 * Math.min(imp.getWidth(), imp.getHeight()) / 75);
 	localMaxRadius = (int) Math.floor(kernelRadius);
 	linearFitIntervals = (int) Math.ceil(1.0 * Math.max(imp.getWidth(), imp.getHeight()) / 100);
 	this.command = command;
 	logTool = new EFTEMjLogTool(command);
 	input = imp;
 	calibration = imp.getCalibration();
 	inputTitle = imp.getTitle();
 	String message;
 	switch (showModeDialog(command)) {
 	case AUTOMATIC:
 	    message = "Automatic SR-EELS correction has been selected.";
 	    logTool.println(message);
 	    IJ.showStatus(message);
 	    if (showParameterDialog(command) == CANCEL) {
 		cancel();
 		return DONE;
 	    }
 	    mode = AUTOMATIC;
 	    break;
 	case MANUAL:
 	    message = "Manual SR-EELS correction has been selected.";
 	    logTool.println(message);
 	    IJ.showStatus(message);
 	    mode = MANUAL;
 	    break;
 	default:
 	    cancel();
 	    return DONE;
 	}
 	if (rotate == true) {
 	    ImagePlus temp = input;
 	    input = new Duplicator().run(input);
 	    temp.unlock();
 	    IJ.run(input, "Rotate 90 Degrees Right", "");
 	}
 	return FLAGS;
     }
 
     /**
      * The user is asked to select the SR-EELS correction mode. A {@link GenericDialog} is used for this purpose. The
      * Buttons <code>Ok</code> and <code>Cancel</code> are labelled <code>Automatic</code> and <code>Manual</code>.
      * 
      * @param title
      * @return the selected mode for the SR-EELS correction
      */
     private int showModeDialog(String title) {
 	GenericDialog gd = new GenericDialog(title + " - detection mode", IJ.getInstance());
 	gd.addMessage("Select the mode of the SR-EELS correction.");
 	gd.setOKLabel("Automatic");
 	gd.setCancelLabel("Manual");
 	// TODO write the description
 	String help = "<html><h3>Automatic mode</h3>" + "<p>description</p>" + "<h3>Manual mode</h3>"
 		+ "<p>description</p></html>";
 	gd.addHelp(help);
 	gd.showDialog();
 	if (gd.wasOKed()) {
 	    return AUTOMATIC;
 	} else if (gd.wasCanceled()) {
 	    return MANUAL;
 	}
 	return CANCEL;
     }
 
     /**
      * This dialog is used to setup the parameter for the automatic drift detection.
      * 
      * @param title
      * @return OK or CANCEL
      */
     private int showParameterDialog(String title) {
 	GenericDialog gd = new GenericDialog(title + " - set detection parameters", IJ.getInstance());
 	String[] items = { "x-axis", "y-axis" };
 	// Try to make a good default selection.
 	String selectedItem = ((input.getWidth() >= input.getHeight()) ? items[0] : items[1]);
 	gd.addChoice("Energy loss on...", items, selectedItem);
 	gd.addNumericField("Median filter radius:", kernelRadius, 1, 3, "pixel");
 	gd.addNumericField("Maxima per row:", maxCount, 0, 3, null);
 	gd.addNumericField("Radius of a local maxima:", localMaxRadius, 0, 2, "pixel");
 	gd.addCheckbox("Use a linear fit:", false);
 	gd.addNumericField("Linear fit steps:", linearFitIntervals, 0, 2, null);
 	gd.addCheckbox("Show unprocessed border", true);
 	gd.addCheckbox("Optimise maxCount:", false);
 	gd.addCheckbox("Skip corrction", false);
 	// TODO write the description
 	String help = "<html><h3>Detection parameters</h3><p>description</p></html>";
 	gd.addHelp(help);
 	gd.showDialog();
 	if (gd.wasCanceled() == true) {
 	    return CANCEL;
 	}
 	// for faster processing the energy loss axis has to be the y-axis.
 	switch (gd.getNextChoice()) {
 	case "x-axis":
 	    logTool.println("Energy loss on x-axis.");
 	    rotate = true;
 	    break;
 	case "y-axis":
 	    logTool.println("Energy loss on y-axis.");
 	    rotate = false;
 	    break;
 	default:
 	    break;
 	}
 	kernelRadius = (float) gd.getNextNumber();
 	maxCount = (int) gd.getNextNumber();
 	localMaxRadius = (int) gd.getNextNumber();
 	useLinearFit = gd.getNextBoolean();
 	linearFitIntervals = (int) gd.getNextNumber();
 	if ((int) Math.floor(1.0 * input.getHeight() / linearFitIntervals) < 3) {
 	    linearFitIntervals = (int) Math.floor(1.0 * input.getHeight() / 3);
 	    IJ.showMessage("Linear fit steps", "The number of fit steps has been reduced to " + linearFitIntervals
 		    + ".\n " + "You need an intervall of at least 3 pixel tobenefit from a linear fit.");
 	}
 	showUnprocessedBorder = gd.getNextBoolean();
 	optimizeMaxCount = gd.getNextBoolean();
 	skipCorrection = gd.getNextBoolean();
 	logTool.println(String.format("Median filter radius: %.1f", kernelRadius));
 	logTool.println(String.format("Maxima per row: %d", maxCount));
 	logTool.println(String.format("Radius of a local maxima: %d", localMaxRadius));
 	logTool.println(String.format("Use a linear fit: %b", useLinearFit));
 	if (useLinearFit) {
 	    logTool.println(String.format("Linear fit steps: %d", linearFitIntervals));
 	    logTool.println(String.format("Show unprocessed border: %b", showUnprocessedBorder));
 	}
 	logTool.println(String.format("Optimise maxCount: %b", optimizeMaxCount));
 	logTool.println(String.format("Skip corrction: %b%n", skipCorrection));
 	return OK;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
      */
     @Override
     public void run(ImageProcessor ip) {
 	switch (mode) {
 	case AUTOMATIC:
 	    runAutomaticDetection();
 	    break;
 	case MANUAL:
 	    runManualDetection();
 	    break;
 	default:
 	    break;
 	}
     }
 
     /**
      * Code of the automatic spectrum border detection. This code uses an image with the y-axis representing the energy
      * loss. The image input has to be rotated before running this method.
      */
     private void runAutomaticDetection() {
 	// The input image is not changed. The program only changes values at a duplicate or at new images.
 	FloatProcessor fp_filtered = (FloatProcessor) input.getProcessor().duplicate();
 	// Create a median filter and apply it.
 	RankFilters rf = new RankFilters();
 	rf.rank(fp_filtered, kernelRadius, RankFilters.MEDIAN);
	FloatProcessor fp_simplifiedSobel = new FloatProcessor(input.getWidth(), input.getHeight());
 	// The filter matrix (-1, 0, 1) is applied.
 	for (int y = 0; y < fp_filtered.getHeight(); y++) {
 	    // left border: (-1;y) = (0;y)
 	    float value = Math.abs(fp_filtered.getf(1, y) - fp_filtered.getf(0, y));
 	    fp_simplifiedSobel.setf(0, y, value);
 	    for (int x = 1; x < fp_filtered.getWidth() - 1; x++) {
 		value = Math.abs(fp_filtered.getf(x + 1, y) - fp_filtered.getf(x - 1, y));
 		fp_simplifiedSobel.setf(x, y, value);
 	    }
 	    // right border: (width;y) = (width-1;y)
 	    value = Math.abs(fp_filtered.getf(input.getWidth() - 1, y) - fp_filtered.getf(input.getWidth() - 2, y));
 	    fp_simplifiedSobel.setf(fp_filtered.getWidth() - 1, y, value);
 	}
 	int[] borderPositionLeft = new int[input.getHeight()];
 	int[] borderPositionRight = new int[input.getHeight()];
 	int[] linearFitLeft = new int[input.getHeight()];
 	int[] linearFitRight = new int[input.getHeight()];
 	int[] borderPositionLeftTemp = new int[input.getHeight()];
 	int[] borderPositionRightTemp = new int[input.getHeight()];
 	int[] linearFitLeftTemp = new int[input.getHeight()];
 	int[] linearFitRightTemp = new int[input.getHeight()];
 	// If no optimisation is done only one variance is calculated.
 	long[] variances = (optimizeMaxCount) ? new long[2 * maxCount] : new long[1];
 	variances[0] = Long.MAX_VALUE;
 	// This index is 0 based.
 	int minVarianceIndex = 0;
 	// This index is 1 based.
 	int currentMaxCount = (optimizeMaxCount) ? 1 : maxCount;
 	// ProcessBar
 	int finalIndex = (variances.length - 1);
 	int progress = 0;
 	IJ.showProgress(0, finalIndex);
 	// I use a do-while-loop to optimise maxCount. If the optimisation is not selected, the loop will stop after
 	// the first execution.
 	do {
 	    // This variable is used to count the empty places at the arrays "maxPos" and "maxValues".
 	    int counterEmptyPositions;
 	    // This is the smallest value at the array "maxValues".
 	    float smallestMaxValue;
 	    // The next 2 arrays will be accessed by the same index.
 	    int[] maxPositions = new int[currentMaxCount];
 	    float[] maxValues = new float[currentMaxCount];
 	    // A loop over all image lines y.
 	    for (int y = 0; y < input.getHeight(); y++) {
 		counterEmptyPositions = currentMaxCount;
 		Arrays.fill(maxPositions, 0);
 		Arrays.fill(maxValues, 0);
 		maxValues[0] = fp_simplifiedSobel.getf(0, y);
 		smallestMaxValue = fp_simplifiedSobel.getf(0, y);
 		counterEmptyPositions--;
 		// A loops over all pixels (x,y) of the current image line y.
 		for (int x = 1; x < input.getWidth(); x++) {
 		    // 1. Check if this pixel is a local maximum.
 		    if (isLocalMaximum(fp_simplifiedSobel, x, y)) {
 			// 2. Check if there are empty places at maxPos.
 			if (counterEmptyPositions > 0) {
 			    maxPositions[currentMaxCount - counterEmptyPositions] = x;
 			    maxValues[currentMaxCount - counterEmptyPositions] = fp_simplifiedSobel.getf(x, y);
 			    counterEmptyPositions--;
 			    // Skip all pixels that can't be a local maximum.
 			    x += localMaxRadius + 1;
 			    // When the arrays are filled you have to find the smallest maximum. This is necessary for
 			    // the successful further iterations of this loop over all x-values.
 			    if (counterEmptyPositions == 0) {
 				// A temporary array is used for sorting the content of "maxValues".
 				float[] temp = Arrays.copyOf(maxValues, maxValues.length);
 				Arrays.sort(temp);
 				smallestMaxValue = temp[0];
 			    }
 			}
 			// else part of 2.
 			else {
 			    // 3. Check if the local maximum is larger than the current smallest value.
 			    if (fp_simplifiedSobel.getf(x, y) > smallestMaxValue) {
 				// Find out the position of the current limit.
 				// TODO What to do if there are two maxima with the same value?
 				int index = searchArray(maxValues, smallestMaxValue);
 				// Replace the old smallest value by the value of the new local maximum.
 				maxPositions[index] = x;
 				maxValues[index] = fp_simplifiedSobel.getf(x, y);
 				// The new local maximum might not be the smallest.
 				// A temporary array is used for sorting the content of "maxValues".
 				float[] temp = Arrays.copyOf(maxValues, maxValues.length);
 				Arrays.sort(temp);
 				smallestMaxValue = temp[0];
 				// Skip all pixels that can't be a local maximum.
 				x += localMaxRadius + 1;
 			    } else {
 				// else part of 3.
 			    }
 			}
 		    } else {
 			// else part of 1.
 			// Nothing is done if the pixel is no local maximum.
 		    }
 		}
 		// Get the most left and the most right position of the largest local maxima.
 		Arrays.sort(maxPositions);
 		// If count is not zero you have to skip the first entries of the array.
 		borderPositionLeft[y] = maxPositions[0 + counterEmptyPositions];
 		borderPositionRight[y] = maxPositions[maxPositions.length - 1];
 		// Continue at next image line (next y).
 	    }
 	    linearFitLeft = applyLinearFit(borderPositionLeft);
 	    linearFitRight = applyLinearFit(borderPositionRight);
 	    variances[(optimizeMaxCount ? currentMaxCount - 1 : 0)] = calculateVariance(borderPositionLeft,
 		    linearFitLeft) + calculateVariance(borderPositionRight, linearFitRight);
 	    logTool.println(String.format("Variance (maxCount = %d): %d", currentMaxCount,
 		    variances[(optimizeMaxCount ? currentMaxCount - 1 : 0)]));
 	    if (optimizeMaxCount && variances[currentMaxCount - 1] < variances[minVarianceIndex]) {
 		minVarianceIndex = currentMaxCount - 1;
 		borderPositionLeftTemp = Arrays.copyOf(borderPositionLeft, borderPositionLeft.length);
 		borderPositionRightTemp = Arrays.copyOf(borderPositionRight, borderPositionRight.length);
 		linearFitLeftTemp = Arrays.copyOf(linearFitLeft, linearFitLeft.length);
 		linearFitRightTemp = Arrays.copyOf(linearFitRight, linearFitRight.length);
 	    }
 	    IJ.showProgress(++progress, finalIndex);
 	    currentMaxCount++;
 	    optimizeMaxCount = (optimizeMaxCount & currentMaxCount <= 2 * maxCount) ? true : false;
 	} while (optimizeMaxCount);
 	if (variances.length > 1) {
 	    logTool.println(String.format("The optimal maxCount is %d.", minVarianceIndex + 1));
 	    borderPositionLeft = borderPositionLeftTemp;
 	    borderPositionRight = borderPositionRightTemp;
 	    linearFitLeft = linearFitLeftTemp;
 	    linearFitRight = linearFitRightTemp;
 	}
 	// ImageJ can combine 32-bit greyscale images to an RGB image. A stack with the size 7 is used to handle the
 	// 32-bit images and ImageJ shows a RGB image. If you open the saved file with Digital Micrograph it's a regular
 	// 32-bit stack.
 	ImagePlus[] images = new ImagePlus[7];
 	// index 0 = red
 	images[0] = (useLinearFit) ? new ImagePlus("Borders", paintBorders(linearFitLeft, linearFitRight))
 		: new ImagePlus("borders", paintBorders(borderPositionLeft, borderPositionRight));
 	// index 1 = green
 	images[1] = (useLinearFit & showUnprocessedBorder) ? new ImagePlus("Unprocessed Borders", paintBorders(
 		borderPositionLeft, borderPositionRight)) : null;
 	// index 2 = blue
 	// index 3 = white
 	images[3] = input;
 	// This class creates the RGB image.
 	RGBStackMerge rgbMerge = new RGBStackMerge();
 	ImagePlus composite = rgbMerge.mergeHyperstacks(images, true);
 	result = composite;
     }
 
     /**
      * Searches the array for the given value and returns the index. In contrast to <code>Arrays.binarySearch()</code>
      * the array has not to be sorted.
      * 
      * @param array
      *            An array that can be unsorted.
      * @param value
      *            The value to search for.
      * @return The index of the first occurrence.
      */
     private int searchArray(float[] array, float value) {
 	int i;
 	for (i = 0; i < array.length; i++) {
 	    if (array[i] == value) {
 		break;
 	    }
 	}
 	return i;
     }
 
     /**
      * Creates a {@link FloatProcessor} that shows both borders as white (255) on black (0).
      * 
      * @param firstBorder
      * @param secondBorder
      * @return A new {@link FloatProcessor}
      */
     private FloatProcessor paintBorders(int[] firstBorder, int[] secondBorder) {
 	FloatProcessor result = new FloatProcessor(input.getWidth(), input.getHeight());
 	result.setValue(0);
 	result.fill();
 	for (int index = 0; index < ((horizontalOrientation) ? input.getWidth() : input.getHeight()); index++) {
 	    if (horizontalOrientation) {
 		if (firstBorder[index] < 0) {
 		    result.setf(index, 0, 255);
 		} else if (firstBorder[index] >= result.getHeight()) {
 		    result.setf(index, result.getHeight() - 1, 255);
 		} else {
 		    result.setf(index, firstBorder[index], 255);
 		}
 		if (secondBorder[index] < 0) {
 		    result.setf(index, 0, 255);
 		} else if (secondBorder[index] >= result.getHeight()) {
 		    result.setf(index, result.getHeight() - 1, 255);
 		} else {
 		    result.setf(index, secondBorder[index], 255);
 		}
 	    } else {
 		if (firstBorder[index] < 0) {
 		    result.setf(0, index, 255);
 		} else if (firstBorder[index] >= result.getWidth()) {
 		    result.setf(result.getWidth() - 1, index, 255);
 		} else {
 		    result.setf(firstBorder[index], index, 255);
 		}
 		if (secondBorder[index] < 0) {
 		    result.setf(0, index, 255);
 		} else if (secondBorder[index] >= result.getWidth()) {
 		    result.setf(result.getWidth() - 1, index, 255);
 		} else {
 		    result.setf(secondBorder[index], index, 255);
 		}
 	    }
 	}
 	return result;
     }
 
     /**
      * Applies a linear fit to the given array of int values. This method considers the configured number of fit
      * intervals.
      * 
      * @param input
      *            An array of int values.
      * @return A new array of {@link Integer}s that represent a linear fit to the input data.
      */
     private int[] applyLinearFit(int[] input) {
 	int[] result = new int[input.length];
 	int intervalWidth = (int) Math.floor(1.0 * this.input.getHeight() / linearFitIntervals);
 	int remainder = this.input.getHeight() % linearFitIntervals;
 	int start = 0;
 	int end;
 	for (int index = 1; index <= linearFitIntervals; index++) {
 	    end = start + intervalWidth - 1;
 	    // the reminder is distributed to the first intervals
 	    if (remainder > 0) {
 		end++;
 	    }
 	    float meanX;
 	    float meanY;
 	    int count = 0;
 	    float sumX = 0;
 	    float sumY = 0;
 	    for (int i = start; i <= end; i++) {
 		if (input[i] != 0 & input[i] != this.input.getWidth()) {
 		    sumX += i;
 		    sumY += input[i];
 		    count++;
 		}
 	    }
 	    meanX = sumX / count;
 	    meanY = sumY / count;
 	    float sumCovar = 0;
 	    float sumX2 = 0;
 	    for (int i = start; i <= end; i++) {
 		if (input[i] != 0) {
 		    sumCovar += (i - meanX) * (input[i] - meanY);
 		    sumX2 += Math.pow(i - meanX, 2);
 		}
 	    }
 	    float m = sumCovar / sumX2;
 	    float b = meanY - m * meanX;
 	    for (int i = start; i <= end; i++) {
 		result[i] = Math.round(m * i + b);
 	    }
 
 	    remainder--;
 	    start = end + 1;
 	}
 
 	return result;
     }
 
     /**
      * Calculates the variance of the given arrays. Both must have the same length.
      * 
      * @param values
      *            1st array with length N.
      * @param fit
      *            2nd array with length N.
      * @return The variance.
      */
     private long calculateVariance(int[] values, int[] fit) {
 	long variance = 0;
 	for (int i = 0; i < values.length; i++) {
 	    variance += Math.pow(values[i] - fit[i], 2);
 	}
 	return variance;
     }
 
     /**
      * Checks if the pixel is a local maximum. Only the x-direction is considered.
      * 
      * @param input
      *            A {@link FloatProcessor}
      * @param x
      *            The x-axis coordinate.
      * @param y
      *            The y-axis coordinate.
      * @return True if the Point (x,y) is a local maximum.
      */
     private boolean isLocalMaximum(FloatProcessor input, int x, int y) {
 	boolean isMax = true;
 	float testValue = input.getf(x, y);
 	// all neighbouring pixels are stored at an array
 	float[] neighbor = new float[2 * localMaxRadius + 1];
 	for (int i = -localMaxRadius; i <= localMaxRadius; i++) {
 	    if (x + i >= 0 & x + i < input.getWidth()) {
 		neighbor[i + localMaxRadius] = input.getf(x + i, y);
 	    } else {
 		if (x + 1 < 0) {
 		    neighbor[i + localMaxRadius] = input.getf(0, y);
 		} else {
 		    if (x + 1 >= input.getWidth()) {
 			neighbor[i + localMaxRadius] = input.getf(input.getWidth() - 1, y);
 		    }
 		}
 	    }
 	}
 	// check if no neighbour is larger than the given pixel (x,y)
 	for (int i = 0; i < neighbor.length; i++) {
 	    if (neighbor[i] > testValue) {
 		isMax = false;
 		break;
 	    }
 	}
 	return isMax;
     }
 
     /**
      * Code of the manual spectrum border detection.
      */
     private void runManualDetection() {
 	// Store some settings for resetting them later on.
 	double oldDisplayRangeMax = input.getDisplayRangeMax();
 	double oldDisplayRangeMin = input.getDisplayRangeMin();
 	String oldToolName = IJ.getToolName();
 	// Set a display limit that enhances the visibility of the spectrum border.
 	// I assume, that the image contains pixel values between 0 and 2^16.
 	int[] histogram = input.getStatistics(ImageStatistics.MIN_MAX, 256, 0.0, Math.pow(2, 16)).histogram;
 	int displayRangeLimit = 0;
 	int sumOfPixels = 0;
 	while (sumOfPixels < 0.01 * input.getWidth() * input.getHeight()) {
 	    sumOfPixels += histogram[displayRangeLimit];
 	    displayRangeLimit++;
 	}
 	input.setDisplayRange(0, Math.pow(2, 16) / 256 * displayRangeLimit);
 	input.updateAndDraw();
 	// Prepare ImageJ for manually marking the spectrum borders.
 	IJ.setTool(Toolbar.LINE);
 	RoiManager roiManager = new RoiManager();
 	roiManager.runCommand("show all with labels");
 	roiManager.setVisible(true);
 	// TODO Add some controls (e.g. Zoom) to the ExtendedWaitForUserDialog.
 	// TODO Add a checkbox for "Skip correction".
 	ExtendedWaitForUserDialog waitDLG = new ExtendedWaitForUserDialog(command + " - manual mode",
 		"Place two lines at the borders of the spectrum\n" + "One line at each site of the spectrum.\n"
 			+ "Press OK when you have added both lines to the RoiManager", null);
 	// Wait for the user to place two lines as ROIs.
 	waitDLG.show();
 	// The user has pressed Ok.
 	if (waitDLG.escPressed()) {
 	    cancel();
 	} else {
 	    boolean roiError = false;
 	    Roi[] rois = roiManager.getRoisAsArray();
 	    if (rois.length == 2) {
 		if (rois[0].isLine() & rois[1].isLine()) {
 		    firstLine = (Line) rois[0];
 		    secondLine = (Line) rois[1];
 		    System.out.println(firstLine.toString());
 		    System.out.println(firstLine.x1 + "; " + firstLine.x2 + "; " + firstLine.y1 + "; " + firstLine.y2);
 		    System.out.println(secondLine.toString());
 		    System.out.println(secondLine.x1 + "; " + secondLine.x2 + "; " + secondLine.y1 + "; "
 			    + secondLine.y2);
 		    // TODO Add log entries.
 		    firstBorder = lineToArray(firstLine);
 		    secondBorder = lineToArray(secondLine);
 		    FloatProcessor input_borders;
 		    input_borders = paintBorders(firstBorder, secondBorder);
 		    ImagePlus ipBorders = new ImagePlus("Borders", input_borders);
 		    // A composite image can contain up to 7 channels.
 		    ImagePlus[] images = new ImagePlus[7];
 		    // index 0 = red
 		    images[0] = ipBorders;
 		    // index 1 = green
 		    // index 2 = blue
 		    // index 3 = grey
 		    images[3] = input;
 		    // It is easier to reset the display limits before creating the composite images.
 		    input.setDisplayRange(oldDisplayRangeMin, oldDisplayRangeMax);
 		    // This class creates the RGB image.
 		    RGBStackMerge rgbMerge = new RGBStackMerge();
 		    ImagePlus composite = rgbMerge.mergeHyperstacks(images, true);
 		    composite.setTitle(input.getTitle());
 		    result = composite;
 		} else {
 		    // If one of the ROIs is no line:
 		    roiError = true;
 		}
 	    } else {
 		// If there is a number of ROIs that is not equal to 2:
 		roiError = true;
 	    }
 	    if (roiError) {
 		String message = "The manual mode has been aborded.\nYou have to add exacly 2 line ROIs to the ROI Manager.";
 		IJ.showMessage(command + " - error", message);
 		logTool.println(message);
 		cancel();
 	    }
 	}
 
 	// Reset some settings.
 	roiManager.runCommand("show none");
 	roiManager.close();
 	input.setDisplayRange(oldDisplayRangeMin, oldDisplayRangeMax);
 	input.updateAndDraw();
 	IJ.setTool(oldToolName);
     }
 
     /**
      * This method calculates slope and intercept of the given line. Both values are used to fill the array. The index
      * of the array is the y-axis of the input image. The array is filled by evaluating the equation
      * <code>f(y) = Math.round((y - intercept) / slope)</code>.
      * 
      * @param line
      *            A line object.
      * @return An array representing the line.
      */
     private int[] lineToArray(Line line) {
 	int[] border;
 	// ImageJ uses angles between 0 and 180 degree. Negative values are used for a clockwise rotation and positive
 	// values for a counterclockwise rotation.
 	System.out.println(String.format("angle: %s", Math.abs(line.getAngle(line.x1, line.y1, line.x2, line.y2))));
 	if (Math.abs(line.getAngle(line.x1, line.y1, line.x2, line.y2)) > 45
 		& Math.abs(line.getAngle(line.x1, line.y1, line.x2, line.y2)) < 135) {
 	    border = new int[input.getHeight()];
 	} else {
 	    border = new int[input.getWidth()];
 	    horizontalOrientation = true;
 	}
 	double slope = (line.y2d - line.y1d) / (line.x2d - line.x1d);
 	System.out.println(String.format("slope: %s", slope));
 	double intercept = -line.x1d * slope + line.y1d;
 	System.out.println(String.format("intercept: %s", intercept));
 	for (int index = 0; index < border.length; index++) {
 	    if (horizontalOrientation) {
 		border[index] = (int) Math.round(slope * index + intercept);
 	    } else {
 		border[index] = (int) Math.round((index - intercept) / slope);
 	    }
 	}
 	return border;
     }
 
     /**
      * Cancel the plugin and show a status message.
      */
     private void cancel() {
 	canceled = true;
 	String message = "Drift detection has been canceled.";
 	logTool.println(message);
 	IJ.showStatus(message);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see ij.plugin.filter.ExtendedPlugInFilter#setNPasses(int)
      */
     @Override
     public void setNPasses(int nPasses) {
 	// This method is not used.
     }
 }
