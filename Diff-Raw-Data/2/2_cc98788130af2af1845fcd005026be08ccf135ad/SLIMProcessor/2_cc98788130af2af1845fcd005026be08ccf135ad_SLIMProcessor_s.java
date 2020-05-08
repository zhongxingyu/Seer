 /*
  * #%L
  * SLIM plugin for combined spectral-lifetime image analysis.
  * %%
  * Copyright (C) 2010 - 2014 Board of Regents of the University of
  * Wisconsin-Madison.
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/gpl-3.0.html>.
  * #L%
  */
 
 package loci.slim;
 
 import ij.IJ;
 import ij.ImagePlus;
 import ij.gui.GenericDialog;
 import ij.gui.Roi;
 import ij.plugin.frame.RoiManager;
 import ij.process.ColorProcessor;
 import ij.process.ImageProcessor;
 import io.scif.Format;
 import io.scif.Metadata;
 import io.scif.Reader;
 import io.scif.SCIFIO;
 import io.scif.config.SCIFIOConfig;
 import io.scif.img.ImgOpener;
 import io.scif.img.axes.SCIFIOAxes;
 
 import java.awt.Color;
 import java.awt.Rectangle;
 import java.awt.image.IndexColorModel;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.prefs.Preferences;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.filechooser.FileFilter;
 
 import loci.curvefitter.CurveFitData;
 import loci.curvefitter.ICurveFitData;
 import loci.curvefitter.ICurveFitter;
 import loci.curvefitter.ICurveFitter.FitAlgorithm;
 import loci.curvefitter.ICurveFitter.FitFunction;
 import loci.curvefitter.ICurveFitter.FitRegion;
 import loci.curvefitter.IFitterEstimator;
 import loci.curvefitter.JaolhoCurveFitter;
 import loci.curvefitter.SLIMCurveFitter;
 import loci.slim.analysis.SLIMAnalysis;
 import loci.slim.analysis.batch.ExportBatchHistogram;
 import loci.slim.analysis.batch.ExportSummaryToText;
 import loci.slim.analysis.batch.ui.BatchHistogramListener;
 import loci.slim.analysis.plugins.ExportHistogramsToText;
 import loci.slim.analysis.plugins.ExportPixelsToText;
 import loci.slim.fitted.FittedValue;
 import loci.slim.fitted.FittedValueFactory;
 import loci.slim.fitting.ErrorManager;
 import loci.slim.fitting.FitInfo;
 // Kludge in the new stuff:
 import loci.slim.fitting.IDecayImage;
 import loci.slim.fitting.IFittedImage;
 import loci.slim.fitting.config.Configuration;
 import loci.slim.fitting.cursor.FittingCursor;
 import loci.slim.fitting.cursor.FittingCursorHelper;
 import loci.slim.fitting.cursor.IFittingCursorListener;
 import loci.slim.fitting.engine.IFittingEngine;
 import loci.slim.fitting.images.FittedImageFitter;
 import loci.slim.fitting.images.FittedImageFitter.FittedImageType;
 import loci.slim.fitting.images.FittedImageParser;
 import loci.slim.fitting.params.GlobalFitParams;
 import loci.slim.fitting.params.IFitResults;
 import loci.slim.fitting.params.IGlobalFitParams;
 import loci.slim.fitting.params.ILocalFitParams;
 import loci.slim.fitting.params.LocalFitParams;
 import loci.slim.heuristics.CursorEstimator;
 import loci.slim.heuristics.Estimator;
 import loci.slim.heuristics.FitterEstimator;
 import loci.slim.heuristics.IEstimator;
 import loci.slim.histogram.HistogramTool;
 import loci.slim.preprocess.IProcessor;
 import loci.slim.preprocess.ISLIMBinner;
 import loci.slim.preprocess.RoiProcessor;
 import loci.slim.preprocess.SLIMBinning;
 import loci.slim.preprocess.Threshold;
 import loci.slim.ui.DecayGraph;
 import loci.slim.ui.ExcitationPanel;
 import loci.slim.ui.IDecayGraph;
 import loci.slim.ui.IUserInterfacePanel;
 import loci.slim.ui.IUserInterfacePanelListener;
 import loci.slim.ui.UserInterfacePanel;
 import net.imglib2.Cursor;
 import net.imglib2.RandomAccess;
 import net.imglib2.meta.ImgPlus;
 import net.imglib2.type.numeric.RealType;
 import net.imglib2.type.numeric.real.DoubleType;
 
 /**
  * SLIMProcessor is the main class of the SLIM Plugin. It was originally just
  * thrown together to get something working, with some code/techniques borrowed
  * from SLIM Plotter. Parts of this code are ugly and experimental.
  *
  * <dl><dt><b>Source code:</b></dt>
  * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/SLIMProcessor.java">Trac</a>,
  * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/SLIMProcessor.java">SVN</a></dd></dl>
  *
  * @author Aivar Grislis
  */
 public class SLIMProcessor <T extends RealType<T>> {
 	private static boolean OLD_STYLE = false;
 
 	private IFittingEngine _fittingEngine;
 	private FittingCursor _fittingCursor;
 	private int _fittingOrdinal = 0;
 
 	private static final String SDT_SUFFIX = ".sdt";
 	private static final String ICS_SUFFIX = ".ics";
 	private static final String CSV_SUFFIX = ".csv";
 	private static final String TSV_SUFFIX = ".tsv";
 	private static final String X = "X";
 	private static final String Y = "Y";
 	private static final String LIFETIME = "Lifetime";
 	private static final String CHANNELS = "Channels";
 	private static final boolean TABBED = true;
 	private static final boolean USE_TAU = true;
 	private static final boolean USE_LAMBDA = false;
 
 	// this affects how many pixels we process at once
 	private static final int PIXEL_COUNT = 128;
 
 	// Unicode special characters
 	private static final Character CHI    = '\u03c7';
 	private static final Character SQUARE = '\u00b2';
 	private static final Character TAU    = '\u03c4';
 	private static final Character LAMBDA = '\u03bb';
 	private static final Character SIGMA  = '\u03c3';
 	private static final Character SUB_1  = '\u2081';
 	private static final Character SUB_2  = '\u2082';
 	private static final Character SUB_3  = '\u2083';
 
 	private static final File[] NO_FILES_SELECTED = new File[0];
 
 	private IUserInterfacePanel _uiPanel;
 
 	private final Object _synchFit = new Object();
 	private volatile boolean _quit;
 	private volatile boolean _cancel;
 	private volatile boolean _fitInProgress;
 	private volatile boolean _fitted;
 	private volatile boolean _summed;
 	private volatile boolean _refit;
 
 	private static final String FILE_KEY = "file";
 	private static final String PATH_KEY = "path";
 	private String _file;
 	private String _path;
 	private Map<String, Object> _globalMetadata;
 
 	private static final String EXPORT_PIXELS_KEY = "exportpixels";
 	private static final String PIXELS_FILE_KEY = "pixelsfile";
 	private static final String EXPORT_HISTOS_KEY = "exporthistograms";
 	private static final String HISTOS_FILE_KEY = "histogramsfile";
 	private static final String EXPORT_SUMMARY_KEY = "exportsummary";
 	private static final String SUMMARY_FILE_KEY = "summaryfile";
 	private static final String CSV_KEY = "csv";
 
 	private static final String GAUSSIAN_A_KEY = "a";
 	private static final String GAUSSIAN_B_KEY = "b";
 	private static final String GAUSSIAN_C_KEY = "c";
 
 	private static final char TAB = '\t';
 
 	private ImgPlus<T> _image;
 	private RandomAccess<T> _cursor;
 
 	private ImgPlus<DoubleType> _fittedImage = null;
 	private int _fittedParameterCount = 0;
 	boolean _visibleFit = true;
 
 	// data parameters
 	private boolean _hasChannels;
 	private int _channels;
 	private int _channelIndex;
 	private int _width;
 	private int _height;
 	private int[] _cLengths;
 	private int _bins;
 	private int _binIndex;
 
 	private double _timeRange;
 	private int _increment;
 	private double _minNonZeroPhotonCount;
 
 	private FitRegion _region;
 	private FitAlgorithm _algorithm;
 	private FitFunction _function;
 
 	private SLIMAnalysis _analysis;
 	private SLIMBinning _binning;
 
 	private ExcitationPanel _excitationPanel = null;
 	private IGrayScaleImage _grayScaleImage;
 	// user sets this from the grayScalePanel control
 	private int _channel;
 	private boolean _fitAllChannels;
 
 	// current x, y
 	private int _x;
 	private int _y;
 
 	private double[] _param = new double[7];
 	private boolean[] _free = { true, true, true, true, true, true, true };
 
 	private int _startBin;
 	private int _stopBin;
 	private int _startX;
 	private int _threshold;
 	private float _chiSqTarget;
 
 	private FitInfo _fitInfo;
 
 	private int _fitOrdinal = 1;
 	private int _debug = 0;
 
 	private boolean _firstBatch;
 	private int _batchBins;
 	private boolean _batchError;
 
 	public SLIMProcessor() {
 		_analysis = new SLIMAnalysis();
 		_binning = new SLIMBinning();
 		_quit = false;
 		_cancel = false;
 		_fitInProgress = false;
 		_fitted = false;
 		_refit = false;
 	}
 
 	public void processImage(ImgPlus<T> image) {
 		boolean success = false;
 
 		_image = image;
 		if (getImageInfo(image)) {
 			// show the UI; do fits
 			doFits();
 		}
 	}
 
 	/**
 	 * Run method for the plugin.  Throws up a file dialog.
 	 *
 	 * @param arg
 	 */
 	public void process(String arg) {
 
 		// Load initial image
 		boolean success = false;
 		File[] files = showFileDialog(getPathFromPreferences());
 		if (files.length > 1) {
 			showError("Error in Batch Processing", "Need to fit a sample image first");
 			GenericDialog dialog = new GenericDialog("Error in Batch Processing");
 		}
 		else {
 			String[] pathAndFile = getPathAndFile(files[0]);
 			_path = pathAndFile[0];
 			_file = pathAndFile[1];
 
 			_image = loadImage(_path, _file);
 			if (null == _image) {
 				showError("Error", "Could not load image");
 			}
 			else {
 				if (getImageInfo(_image)) {
 					savePathInPreferences(_path);
 					success = true;
 				}
 			}
 		}
 
 		if (success) {
 			// show the UI; do fits
 			doFits();
 		}
 	}
 
 	static final private void showError(String title, String message) {
 		GenericDialog dialog = new GenericDialog(title);
 		dialog.addMessage(message);
 		dialog.hideCancelButton();
 		dialog.showDialog();
 	}
 
 	static final private String[] getPathAndFile(File file) {
 		String absolutePath = file.getAbsolutePath();
 		int index = absolutePath.lastIndexOf(File.separator);
 		String pathName = absolutePath.substring(0, index);
 		String fileName = absolutePath.substring(index);
 		if (!pathName.endsWith(File.separator)) {
 			pathName += File.separator;
 		}
 		return new String[] { pathName, fileName };
 	}
 
 	/**
 	 * Start batch processing. (vestigial macro code)
 	 * 
 	 * @return 
 	 */
 	public boolean startBatch() {
 		_uiPanel.disable();
 		_firstBatch = true;
 		_batchError = false;
 		IJ.log("start SLIM Plugin batch processing");
 		return true;
 	}
 
 	/**
 	 * Batch process a single file.
 	 * 
 	 * @param input
 	 * @param output
 	 * @param exportPixels
 	 * @param exportHistograms 
 	 */
 	public void batch(String input, String output, boolean exportPixels, boolean exportHistograms) {
 		// first time through, delete existing output text file
 		if (_firstBatch) {
 			_firstBatch = false;
 			_batchBins = _bins;
 			try {
 				FileWriter fileWriter = new FileWriter(output);
 				fileWriter.close();
 			}
 			catch (IOException e) {
 				GenericDialog dialog = new GenericDialog("Error in Batch Processing");
 				dialog.addMessage("Problem writing to file: " + output);
 				dialog.hideCancelButton();
 				dialog.showDialog();
 				_batchError = true;
 			}
 		}
 
 		// avoid further crashes; error already reported
 		if (_batchError) {
 			return;
 		}
 
 		if (input.endsWith(SDT_SUFFIX) || input.endsWith(ICS_SUFFIX)) {
 			try {
 				// load batched image
 				_image = loadImage(input);
 				getImageInfo(_image);
 
 				if (_batchBins != _bins) {
 					GenericDialog dialog = new GenericDialog("Error in Batch Processing");
 					String imageName = input.substring(input.lastIndexOf(File.separatorChar) + 1);
 					dialog.addMessage("Settings are for " + _batchBins + " bins, " + imageName + " has " + _bins + " bins.");
 					dialog.hideCancelButton();
 					dialog.showDialog();
 					if (dialog.wasCanceled()) {
 						// Cancel cancels rest of batch; OK continues
 						_batchError = true;
 					}
 					return;
 				}
 
 				// fit batched image with current UI settings
 				ImgPlus<DoubleType> fittedImage = fitImage(_uiPanel, 1, true);
 
 				if (null != fittedImage) {
 					// export to text
 					if (exportPixels) {
 						ExportPixelsToText exportPixelsToText = new ExportPixelsToText();
 						exportPixelsToText.export(output, true, fittedImage, FitRegion.EACH, _function, _uiPanel.getFittedImages(), TAB);
 					}
 					if (exportHistograms) {
 						ExportHistogramsToText exportHistogramsToText = new ExportHistogramsToText();
 
 						exportHistogramsToText.export(output, true, fittedImage, _function, _uiPanel.getFittedImages(), TAB);
 					}
 				}
 
 				IJ.log(input);
 			}
 			catch (Exception e) {
 				IJ.handleException(e);
 			}
 		}
 	}
 
 	/**
 	 * End batch processing.
 	 */
 	public void endBatch() {
 		IJ.log("end SLIM Plugin batch processing");
 
 		// restore current file
 		_image = loadImage(_path, _file);
 		getImageInfo(_image);
 
 		// enable UI
 		_uiPanel.reset();
 	}
 
 	boolean _firstBatchHisto;
 	int _batchHistoBins;
 	ExportBatchHistogram _exportBatchHistogram;
 	String _exportOutput;
 
 	//TODO ARG EXPERIMENTAL: (vestigial macro code)
 	public boolean startBatchHisto() {
 		_uiPanel.disable();
 		IJ.log("start SLIM Plugin batch histogram processing");
 		_firstBatchHisto = true;
 		_exportBatchHistogram = new ExportBatchHistogram();
 		_exportBatchHistogram.start();
 		return true;
 	}
 
 	//TODO ARG EXPERIMENTAL
 	public void batchHisto(String input, String output) {
 		// first time through, delete existing output text file
 		if (_firstBatchHisto) {
 			_firstBatchHisto = false;
 			_batchHistoBins = _bins;
 			try {
 				FileWriter fileWriter = new FileWriter(output);
 				fileWriter.close();
 			}
 			catch (IOException e) {
 				GenericDialog dialog = new GenericDialog("Error in Batch Histogram Processing");
 				dialog.addMessage("Problem writing to file: " + output);
 				dialog.hideCancelButton();
 				dialog.showDialog();
 				_batchError = true;
 			}
 			_exportOutput = output;
 		}
 
 		// avoid further crashes; error already reported
 		if (_batchError) {
 			return;
 		}
 
 		if (input.endsWith(SDT_SUFFIX) || input.endsWith(ICS_SUFFIX)) {
 			try {
 
 				// load batched image
 				_image = loadImage(input);
 				getImageInfo(_image);
 
 				if (_batchHistoBins != _bins) {
 					GenericDialog dialog = new GenericDialog("Error in Batch Processing");
 					String imageName = input.substring(input.lastIndexOf(File.separatorChar) + 1);
 					dialog.addMessage("Settings are for " + _batchHistoBins + " bins, " + imageName + " has " + _bins + " bins.");
 					dialog.hideCancelButton();
 					dialog.showDialog();
 					if (dialog.wasCanceled()) {
 						// Cancel cancels rest of batch; OK continues
 						_batchError = true;
 					}
 					return;
 				}
 
 				// fit batched image with current UI settings
 				ImgPlus<DoubleType> fittedImage = fitImage(_uiPanel, 1, true);
 
 				if (null != fittedImage) {
 					_exportBatchHistogram.export(fittedImage, _function);
 				}
 
 				IJ.log(input);
 			}
 			catch (Exception e) {
 				IJ.handleException(e);
 			}
 		}
 	}
 
 	//TODO ARG EXPERIMENTAL
 	public void endBatchHisto() {
 		IJ.log("end SLIM Plugin batch histogram processing");
 
 		_exportBatchHistogram.end(_exportOutput);
 
 		// restore current image
 		_image = loadImage(_path, _file);
 		getImageInfo(_image);
 
 		IJ.showProgress(0, 0);
 
 		// enable UI
 		_uiPanel.reset();
 	}
 
 	/**
 	 * Creates a user interface panel.  Shows a grayscale
 	 * version of the image.
 	 *
 	 * Loops until quitting time and handles fit requests.
 	 * Fitting is driven by a button on the UI panel which
 	 * sets the global _fitInProgress.
 	 *
 	 * @param uiPanel
 	 */
 	private void doFits() {
 		// heuristics
 		IEstimator estimator = new Estimator();
 		IFitterEstimator fitterEstimator = new FitterEstimator();
 
 		// cursor support
 		//IJ.log("doFits opens new FittingCursor");
 		_fittingCursor = new FittingCursor(_timeRange, _bins, fitterEstimator);
 		_fittingCursor.addListener(new FittingCursorListener());
 
 		// show the UI; do fits
 		FittingCursorHelper fittingCursorHelper = new FittingCursorHelper();
 		fittingCursorHelper.setFittingCursor(_fittingCursor);
 		final IUserInterfacePanel uiPanel = new UserInterfacePanel(TABBED,
 			USE_TAU, _bins, _timeRange, _analysis.getChoices(),
 			_binning.getChoices(), fittingCursorHelper, fitterEstimator);
 		_uiPanel = uiPanel; //TODO almost got by having it just be a local variable
 		uiPanel.setX(0);
 		uiPanel.setY(0);
 		uiPanel.setThreshold(estimator.getThreshold());
 		uiPanel.setChiSquareTarget(estimator.getChiSquareTarget());
 		uiPanel.setFunctionParameters(0, estimator.getParameters(1, false));
 		uiPanel.setFunctionParameters(1, estimator.getParameters(2, false));
 		uiPanel.setFunctionParameters(2, estimator.getParameters(3, false));
 		uiPanel.setFunctionParameters(3, estimator.getParameters(0, true));
 		uiPanel.setListener(
 			new IUserInterfacePanelListener() {
 				/**
 				 * Triggers a fit.
 				 */
 				@Override
 				public void doFit() {
 					_cancel = false;
 					_fitInProgress = true;
 				}
 
 				/**
 				 * Triggers a refit.
 				 */
 				@Override
 				public void reFit() {
 					_cancel = false;
 					_fitInProgress = true;
 					_refit = true;
 				}
 
 				/**
 				 * Cancels ongoing fit.
 				 */
 				@Override
 				public void cancelFit() {
 					_cancel = true;
 					if (null != _fitInfo) {
 						_fitInfo.setCancel(true);
 					}
 				}
 
 				/**
 				 * Quits running plugin.
 				 */
 				@Override
 				public void quit() {
 					_grayScaleImage.close();
 					_quit = true;
 				}
 
 				/**
 				 * Open new file(s).
 				 */
 				@Override
 				public void openFile() {
 					File[] files = showFileDialog(getPathFromPreferences());
 					// were multiple files opened?
 					if (1 < files.length) {
 						batchProcessingWithUI(files);
 					}
 					// was a single file opened? (skip cancellations)
 					else if (1 == files.length) {
 						String savePath = _path;
 						String saveFile = _file;
 
 						String[] pathAndFile = getPathAndFile(files[0]);
 						_path = pathAndFile[0];
 						_file = pathAndFile[1];
 
 						_image = loadImage(_path, _file);
 						if (null == _image) {
 							showError("Error", "Could not load image");
 						}
 						else {
 							if (getImageInfo(_image)) {
 								savePathInPreferences(_path);
 
 								// close existing grayscale and hook up a new one
 								_uiPanel.setThresholdListener(null);
 								_grayScaleImage.close();
 								_grayScaleImage = null;
 								showGrayScaleAndFit(uiPanel);
 							}
 							else {
 								// kludgy way to reset
 								_path = savePath;
 								_file = saveFile;
 								_image = loadImage(_path, _file);
 								getImageInfo(_image);
 							}
 						}
 					}
 				}
 
 				/**
 				 * Loads an excitation curve from file.
 				 *
 				 * @param fileName
 				 * @return whether successful
 				 */
 				@Override
 				public boolean loadExcitation(String fileName) {
 					Excitation excitation = ExcitationFileUtility.loadExcitation(fileName, _timeRange);
 					return updateExcitation(uiPanel, excitation);
 				}
 
 				/**
 				 * Creates an excitation curve from current X, Y and saves to file.
 				 *
 				 * @param fileName
 				 * @return whether successful
 				 */
 				@Override
 				public boolean createExcitation(String fileName) {
 					int channel = 0;
 					if (null != _grayScaleImage) {
 						channel = _grayScaleImage.getChannel();
 					}
 					int x = uiPanel.getX();
 					int y = uiPanel.getY();
 					double[] values = new double[_bins];
 					for (int b = 0; b < _bins; ++b) {
 						values[b] = getData(_cursor, channel, x, y, b);
 					}
 					Excitation excitation = ExcitationFileUtility.createExcitation(fileName, values, _timeRange);
 					return updateExcitation(uiPanel, excitation);
 				}
 
 				/**
 				 * Estimates an excitation curve from current X, Y and saves to file.
 				 *
 				 * @param fileName
 				 * @return whether successful
 				 */
 				@Override
 				public boolean estimateExcitation(String fileName) {
 					// get the data
 					int channel = 0;
 					if (null != _grayScaleImage) {
 						channel = _grayScaleImage.getChannel();
 					}
 					int x = uiPanel.getX();
 					int y = uiPanel.getY();
 					double[] inValues = new double[_bins];
 					for (int b = 0; b < _bins; ++b) {
 						inValues[b] = getData(_cursor, channel, x, y, b);
 					}
 
 					// find the peak value and bin
 					double peak = -Double.MAX_VALUE;
 					int peakBin = 0;
 					for (int b = 0; b < _bins; ++b) {
 						if (inValues[b] > peak) {
 							peak = inValues[b];
 							peakBin = b;
 						}
 					}
 
 					double maxSlope = -Double.MAX_VALUE;
 					int maxSlopeBin = 0;
 					double[] firstDerivative = new double[_bins];
 					for (int b = 0; b < peakBin; ++b) {
 						firstDerivative[b] = inValues[b + 1] - inValues[b];
 						if (firstDerivative[b] > maxSlope) {
 							maxSlope = firstDerivative[b];
 							maxSlopeBin = b;
 						}
 					}
 
 					//TODO WHY? _fittingCursor = null;
 
 					double a = peak;
 					double b = peakBin;
 					double c = (double) (peakBin - maxSlopeBin) / 2;
 
 					IJ.log("max slope estimated GAUSSIAN a " + a + " b " + b + " c " + c);
 
 					double[] outValues = new double[_bins];
 					for (int i = 0; i < _bins; ++i) {
 						outValues[i] = gaussian(a, b, c, i);
 					}
 
 					IJ.log("PEAK VALUE " + peak + " BIN " + peakBin);
 					IJ.log("MAX SLOPE " + maxSlope + " BIN " + maxSlopeBin);
 					IJ.log("GAUSSIAN a " + a + " b " + b + " c " + c);
 					//TODO END EXPERIMENTAL
 					for (double oV : outValues) {
 						if (0.0 != oV) IJ.log(" " + oV);
 					}
 
 					Excitation excitation = ExcitationFileUtility.createExcitation(fileName, outValues, _timeRange);
 					return updateExcitation(uiPanel, excitation);
 				}
 
 				@Override
 				public boolean gaussianExcitation(String fileName) {
 					Preferences prefs = Preferences.userNodeForPackage(this.getClass());
 					double a = prefs.getDouble(GAUSSIAN_A_KEY, 30.0);
 					double b = prefs.getDouble(GAUSSIAN_B_KEY, 20.0);
 					double c = prefs.getDouble(GAUSSIAN_C_KEY, 2.0);
 
 					GenericDialog dialog = new GenericDialog("Gaussian Excitation");
 					dialog.addNumericField("height", a, 5);
 					dialog.addNumericField("position", b, 5);
 					dialog.addNumericField("width", c, 5);
 					dialog.showDialog();
 					if (dialog.wasCanceled()) {
 						return false;
 					}
 					a = dialog.getNextNumber();
 					b = dialog.getNextNumber();
 					c = dialog.getNextNumber();
 
 					prefs.putDouble(GAUSSIAN_A_KEY, a);
 					prefs.putDouble(GAUSSIAN_B_KEY, b);
 					prefs.putDouble(GAUSSIAN_C_KEY, c);
 
 					double[] outValues = new double[_bins];
 					for (int i = 0; i < _bins; ++i) {
 						outValues[i] = gaussian(a, b, c, i);
 					}
 
 					Excitation excitation = ExcitationFileUtility.createExcitation(fileName, outValues, _timeRange);
 					return updateExcitation(uiPanel, excitation);
 				}
 
 				/**
 				 * Cancels the current excitation curve, if any.
 				 *
 				 */
 				@Override
 				public void cancelExcitation() {
 					if (null != _excitationPanel) {
 						_excitationPanel.quit();
 						_excitationPanel = null;
 						updateExcitation(null, null);
 						//TODO redo stop/start cursors on decay curve?
 					}
 				}
 
 				/**
 				 * Estimates prompt and decay cursors.
 				 */
 				@Override
 				public void estimateCursors() {
 					double xInc = _timeRange;
 
 					double[] prompt = null;
 					if (null != _excitationPanel) {
 						prompt = _excitationPanel.getRawValues();
 					}
 					double[] decay = new double[_bins];
 					for (int b = 0; b < _bins; ++b) {
 						decay[b] = getData(_cursor, _channel, _x, _y, b);
 					}
 
 					double chiSqTarget = _uiPanel.getChiSquareTarget();
 //					IJ.log("chiSqTarget is " + chiSqTarget);
 //					IJ.log("prompt is " + prompt + " and fitting cursor thinks prompt " + _fittingCursor.getHasPrompt());
 					if (null != prompt && _fittingCursor.getHasPrompt()) {
 						double[] results = CursorEstimator.estimateCursors
 								(xInc, prompt, decay, chiSqTarget);
 
 						// want all the fitting cursor listeners to get everything at once
 						_fittingCursor.suspendNotifications();
 						_fittingCursor.setHasPrompt(true);
 						_fittingCursor.setPromptStartBin
 						((int) results[CursorEstimator.PROMPT_START]);
 						_fittingCursor.setPromptStopBin
 						((int) results[CursorEstimator.PROMPT_STOP]);
 						_fittingCursor.setPromptBaselineValue
 						(results[CursorEstimator.PROMPT_BASELINE]);
 						_fittingCursor.setTransientStartBin
 						((int) results[CursorEstimator.TRANSIENT_START]);
 						_fittingCursor.setDataStartBin
 						((int) results[CursorEstimator.DATA_START]);
 						_fittingCursor.setTransientStopBin
 						((int) results[CursorEstimator.TRANSIENT_STOP]);
 						_fittingCursor.sendNotifications();
 					}
 					else
 					{
 						int[] results = CursorEstimator.estimateDecayCursors
 								(xInc, decay);
 
 						// want all the fitting cursor listeners to get everything at once
 						_fittingCursor.suspendNotifications();
 						_fittingCursor.setHasPrompt(false);
 						_fittingCursor.setTransientStartBin(results[CursorEstimator.TRANSIENT_START]);
 						_fittingCursor.setDataStartBin(results[CursorEstimator.DATA_START]);
 						_fittingCursor.setTransientStopBin(results[CursorEstimator.TRANSIENT_STOP]);
 						_fittingCursor.sendNotifications();
 					}
 				}
 			}
 				);
 		uiPanel.getFrame().setLocationRelativeTo(null);
 		uiPanel.getFrame().setVisible(true);
 
 		showGrayScaleAndFit(uiPanel);
 
 		// processing loop; waits for UI panel input
 		while (!_quit) {
 			while (!_fitInProgress) {
 				try {
 					Thread.sleep(1000);
 				}
 				catch (InterruptedException e) {
 
 				}
 				if (_quit) {
 					hideUIPanel(uiPanel);
 					return;
 				}
 			}
 
 			//uiPanel.enable(false); //TODO this might be better to be same as grayScalePanel
 			_grayScaleImage.enable(false);
 
 			// get settings of requested fit
 			getFitSettings(_grayScaleImage, uiPanel, _fittingCursor);
 
 			if (_refit) {
 				if (_summed) {
 					fitSummed(_uiPanel, _fittingCursor);
 				}
 				else {
 					fitPixel(_uiPanel, _fittingCursor);
 				}
 				_refit = false;
 			}
 			else {
 				// do the fit
 				fitData(uiPanel);
 				_summed = uiPanel.getRegion() == FitRegion.SUMMED;
 			}
 
 			_fitInProgress = false;
 			//uiPanel.enable(true);
 			_grayScaleImage.enable(true);
 			uiPanel.reset();
 		}
 		hideUIPanel(uiPanel);
 	}
 
 	private double gaussian(double a, double b, double c, double x) {
 		//return a * Math.exp(-((x - b) * (x - b) / (2 * c * c)));
 		double tmp = (x - b) / c;
 		//return a * Math.exp(-(tmp * tmp) / 2);
 		double mean = b;
 		double stdDeviation = c;
 		double variance = stdDeviation * stdDeviation;
 
 		return a * Math.pow(Math.exp(-(((x - mean) * (x - mean)) / ((2 * variance)))), 1 / (stdDeviation * Math.sqrt(2 * Math.PI)));
 	}
 
 	private void showGrayScaleAndFit(final IUserInterfacePanel uiPanel) {
 		// create a grayscale image from the data
 		_grayScaleImage = new GrayScaleImage(_image);
 		_grayScaleImage.setListener(
 			new ISelectListener() {
 				@Override
 				public void selected(int channel, int x, int y) {
 					// just ignore clicks during a fit
 					if (!_fitInProgress) {
 						// ignore clicks when in summed mode
 						if (!_summed) {
 							synchronized (_synchFit) {
 								_x = x;
 								_y = y;
 
 								uiPanel.setX(x);
 								uiPanel.setY(y);
 								getFitSettings(_grayScaleImage, uiPanel, _fittingCursor);
 								// fit on the pixel clicked
 								fitPixel(uiPanel, _fittingCursor);
 							}
 						}
 					}
 				}
 			}
 				);
 		// get a correction factor for photon counts //TODO this is available in metadata
 		_minNonZeroPhotonCount = _grayScaleImage.getMinNonZeroPhotonCount();
 
 		// get estimated threshold value
 		int threshold = _grayScaleImage.estimateThreshold();
 		uiPanel.setThreshold(threshold);
 
 		// show threshold updates from UI panel on gray image
 		uiPanel.setThresholdListener(_grayScaleImage);
 
 		// what is the brightest point in the image?
 		int[] brightestPoint = _grayScaleImage.getBrightestPoint();
 		_x = brightestPoint[0];
 		_y = brightestPoint[1];
 		uiPanel.setX(_x);
 		uiPanel.setY(_y);
 
 		// set start and stop for now; will be updated if we load an excitation curvce
 		updateDecayCursors(uiPanel);
 
 		// fit on the brightest pixel
 		getFitSettings(_grayScaleImage, uiPanel, _fittingCursor);
 		fitPixel(uiPanel, _fittingCursor);
 	}
 
 	/**
 	 * Handles UI for batch processing.  Invokes batch processing.
 	 * 
 	 * @param files 
 	 */
 	private void batchProcessingWithUI(final File[] files) {
 		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
 
 		boolean defExportPixels = prefs.getBoolean(EXPORT_PIXELS_KEY, true);
 		String defPixelsFile = prefs.get(PIXELS_FILE_KEY, "pixels");
 		boolean defExportHistograms = prefs.getBoolean(EXPORT_HISTOS_KEY, true);
 		String defHistogramsFile = prefs.get(HISTOS_FILE_KEY, "histograms");
 		boolean defExportSummary = prefs.getBoolean(EXPORT_SUMMARY_KEY, true);
 		String defSummaryFile = prefs.get(SUMMARY_FILE_KEY, "summary");
 		boolean defCSV = prefs.getBoolean(CSV_KEY, false);
 
 		GenericDialog dialog = new GenericDialog("Batch Processing");
 		dialog.addCheckbox("Export Pixels", defExportPixels);
 		dialog.addStringField("Pixels File", defPixelsFile);
 		dialog.addCheckbox("Export Histograms", defExportHistograms);
 		dialog.addStringField("Histogram File", defHistogramsFile);
 		dialog.addCheckbox("Export Summary Histogram", defExportSummary);
 		dialog.addStringField("Summary File", defSummaryFile);
 		dialog.addCheckbox("Comma Separated", defCSV);
 		dialog.showDialog();
 		if (dialog.wasCanceled()) {
 			return;
 		}
 
 		final boolean exportPixels = dialog.getNextBoolean();
 		String tmpPixelsFile = dialog.getNextString();
 		final boolean exportHistograms = dialog.getNextBoolean();
 		String tmpHistogramsFile = dialog.getNextString();
 		final boolean exportSummary = dialog.getNextBoolean();
 		String tmpSummaryFile = dialog.getNextString();
 		final boolean csv = dialog.getNextBoolean();
 
 		// make sure output file suffix is appropriate
 		final String pixelsFile = checkSuffix(tmpPixelsFile, csv);
 		final String histogramsFile = checkSuffix(tmpHistogramsFile, csv);
 		final String summaryFile = checkSuffix(tmpSummaryFile, csv);
 
 		prefs.putBoolean(EXPORT_PIXELS_KEY, exportPixels);
 		prefs.put(PIXELS_FILE_KEY, pixelsFile);
 		prefs.putBoolean(EXPORT_HISTOS_KEY, exportHistograms);
 		prefs.put(HISTOS_FILE_KEY, histogramsFile);
 		prefs.putBoolean(EXPORT_SUMMARY_KEY, exportSummary);
 		prefs.put(SUMMARY_FILE_KEY, summaryFile);
 
 		new Thread() {
 			public void run() {
 				batchProcessing(exportPixels, pixelsFile,
 					exportHistograms, histogramsFile,
 					exportSummary, summaryFile,
 					files, csv);
 			}
 		}.start();
 	}
 
 	/**
 	 * Use appropriate file name suffix for comma- and tab-separated values.
 	 */
 	private String checkSuffix(String file, boolean csv) {
 		String suffix = csv ? CSV_SUFFIX : TSV_SUFFIX;
 		String otherSuffix = csv ? TSV_SUFFIX : CSV_SUFFIX;
 		if (!file.endsWith(suffix)) {
 			if (file.endsWith(otherSuffix)) {
 				int i = file.indexOf(otherSuffix);
 				file = file.substring(0, i);
 			}
 			file += suffix;
 		}
 		return file;
 	}
 
 	/**
 	 * Does the batch processing.
 	 * 
 	 * @param exportPixels
 	 * @param pixelsFile
 	 * @param exportHistograms
 	 * @param histogramsFile
 	 * @param exportSummary
 	 * @param summaryFile
 	 * @param files
 	 * @param csv
 	 */
 	private void batchProcessing(boolean exportPixels, String pixelsFile,
 		boolean exportHistograms, String histogramsFile,
 		boolean exportSummary, String summaryFile,
 		File[] files, boolean csv)
 	{
 		ExportPixelsToText pixels = null;
 		ExportHistogramsToText histograms = null;
 		ExportSummaryToText summary = null;
 
 		_uiPanel.disable();
 		_uiPanel.disableButtons();
 
 		// validate file names
 		if (exportPixels) {
 			if (!checkFileName(pixelsFile)) {
 				return;
 			}
 			pixels = new ExportPixelsToText();
 		}
 		if (exportHistograms) {
 			if (!checkFileName(histogramsFile)) {
 				return;
 			}
 			histograms = new ExportHistogramsToText();
 		}
 		if (exportSummary) {
 			if (!checkFileName(summaryFile)) {
 				return;
 			}
 			summary = new ExportSummaryToText();
 			BatchHistogramListener listener = new BatchHistogramListener() {
 				@Override
 				public void swapImage(String filePath) {
 					// load image
 					_image = loadImage(filePath);
 
 					// get metadata
 					getImageInfo(_image);
 
 					// save new path and file names
 					int index = filePath.lastIndexOf(File.separator);
 					_path = filePath.substring(0, index);
 					_file = filePath.substring(index + 1);
 
 					// turn off old threshold listener
 					_uiPanel.setThresholdListener(null);
 
 					// close existing grayscale image
 					_grayScaleImage.close();
 					_grayScaleImage = null;
 
 					// show new grayscale and fit brightest
 					showGrayScaleAndFit(_uiPanel);
 
 					// set up new threshold listener
 					_uiPanel.setThresholdListener(_grayScaleImage);
 				}
 			};
 			int components = 0;
 			switch (_function) {
 				case SINGLE_EXPONENTIAL:
 					components = 1;
 					break;
 				case DOUBLE_EXPONENTIAL:
 					components = 2;
 					break;
 				case TRIPLE_EXPONENTIAL:
 					components = 3;
 					break;
 				case STRETCHED_EXPONENTIAL:
 					components = 1;
 					break;
 			}
 			FittedValue[] values = FittedValueFactory.createFittedValues(_uiPanel.getFittedImages(), components);
 			summary.init(_function, values, listener);
 		}
 
 		// ugly use of globals, leftover from batch macro kludge
 		_batchBins = _bins;
 
 		try {
 			char separator = '\t';
 			if (csv) {
 				separator = ',';
 			}
 
 			for (int i = 0; i < files.length; ++i) {
 				File file = files[i];
 
 				//TODO if (i > 0) break; //TODO just process a single image
 
 
 				// show progress bar
 				IJ.showProgress(i, (files.length + 1));
 
 				// load batched image
 				_image = loadImage(file.getCanonicalPath());
 				getImageInfo(_image);
 
 				if (_batchBins != _bins) {
 					GenericDialog dialog = new GenericDialog("Error in Batch Processing");
 					String imageName = file.getCanonicalPath();
 					imageName = imageName.substring(imageName.lastIndexOf(File.separatorChar) + 1);
 					dialog.addMessage("Settings are for " + _batchBins + " bins, " + imageName + " has " + _bins + " bins.");
 					//dialog.hideCancelButton();
 					dialog.showDialog();
 					if (dialog.wasCanceled()) {
 						// Cancel cancels rest of batch; OK continues
 						_batchError = true;
 						break;
 					}
 				}
 
 				// fit batched image with current UI settings, channel 1, batch mode
 				ImgPlus<DoubleType> fittedImage = fitImage(_uiPanel, 1, true);
 
 				if (null != fittedImage) {
 					if (exportPixels) {
 						//IJ.log("Export pixels");
 						pixels.export(pixelsFile, true, fittedImage, _region, _function, _uiPanel.getFittedImages(), separator);
 					}
 					if (exportHistograms) {
 						//IJ.log("Export histograms");
 						histograms.export(histogramsFile, true, fittedImage, _function, _uiPanel.getFittedImages(), separator);
 					}
 					if (exportSummary) {
 						//IJ.log("Export summary");
 						summary.process(file.getCanonicalPath(), fittedImage);
 					}
 				}
 			}
 
 			if (exportSummary) {
 				// export summary to text file
 				IJ.log("exportSummary time in SP.java and sep is " + separator);
 				summary.export(summaryFile, separator);
 			}
 		}
 		catch (Exception e) {
 			IJ.handleException(e);
 		}
 
 		// restore current image
 		_image = loadImage(_path, _file);
 		getImageInfo(_image);
 
 		IJ.showProgress(0,0);
 
 		// enable UI
 		_uiPanel.reset();
 		_uiPanel.resetButtons();
 	}
 
 	private boolean checkFileName(String fileName) {
 		try {
 			// open and truncate
 			FileWriter fileWriter = new FileWriter(fileName, false);
 			fileWriter.flush();
 			fileWriter.close();
 
 			//TODO
 			File file = new File(fileName);
 			IJ.log("file is " + file.getCanonicalPath());
 			return true;
 		}
 		catch (IOException e) {
 			GenericDialog dialog = new GenericDialog("Error in Batch Processing");
 			dialog.addMessage("Problem writing to file: " + fileName);
 			dialog.hideCancelButton();
 			dialog.showDialog();
 			return false;
 		}
 	}
 
 	private void hideUIPanel(IUserInterfacePanel uiPanel) {
 		_grayScaleImage.setListener(null);
 		//TODO uiPanel is still hooked up as start stop listeners to decay curves!
 		uiPanel.getFrame().setVisible(false);
 	}
 
 	/**
 	 * This method gives an initial estimate of the decay cursors (start and
 	 * stop values).
 	 * 
 	 * @param uiPanel 
 	 */
 	private void updateDecayCursors(IUserInterfacePanel uiPanel) {
 		// get selected channel
 		int channel = 0;
 		if (null != _grayScaleImage) {
 			channel = _grayScaleImage.getChannel();
 		}
 		double[] decay = new double[_bins];
 		for (int b = 0; b < _bins; ++b) {
 			decay[b] = getData(_cursor, channel, _x, _y, b);
 		}
 		int[] results = CursorEstimator.estimateDecayCursors(_timeRange, decay);
 		int transientStart = results[CursorEstimator.TRANSIENT_START];
 		int dataStart = results[CursorEstimator.DATA_START];
 		int transientStop = results[CursorEstimator.TRANSIENT_STOP];
 
 		// want to batch all of the fitting cursor notifications to listeners
 		_fittingCursor.suspendNotifications();
 		_fittingCursor.setTransientStartBin(transientStart);
 		_fittingCursor.setDataStartBin(dataStart);
 		_fittingCursor.setTransientStopBin(transientStop);
 		_fittingCursor.sendNotifications();
 	}
 
 	/**
 	 * This method sums the decay for all channels of all pixels.
 	 * 
 	 * @return 
 	 */
 	//TODO not used
 	private double[] getSummedDecay() {
 		double[] decay = new double[_bins];
 		for (int i = 0; i < decay.length; ++i) {
 			decay[i] = 0.0;
 		}
 		for (int y = 0; y < _height; ++y) {
 			for (int x = 0; x < _width; ++x) {
 				for (int c = 0; c < _channels; ++c) {
 					for (int b = 0; b < _bins; ++b) {
 						decay[b] += getData(_cursor, c, x, y, b);
 					}
 				}
 			}
 		}
 		return decay;
 	}
 
 	/*
 	 * This method is called when a new excitation is loaded.
 	 */
 	private boolean updateExcitation(IUserInterfacePanel uiPanel, Excitation excitation) {
 		boolean success = false;
 		if (null != excitation) {
 			if (null != _excitationPanel) {
 				_excitationPanel.quit();
 			}
 
 			// get selected channel
 			int channel = 0;
 			if (null != _grayScaleImage) {
 				channel = _grayScaleImage.getChannel();
 			}
 			double[] decay = new double[_bins];
 			for (int b = 0; b < _bins; ++b) {
 				decay[b] = getData(_cursor, channel, _x, _y, b);
 			}
 
 			double chiSqTarget = uiPanel.getChiSquareTarget();
 			double[] results = CursorEstimator.estimateCursors
 					(_timeRange, excitation.getValues(), decay, chiSqTarget);
 
 			// want all the fitting cursor listeners to get everything at once
 			if (null == _fittingCursor) {
 				IJ.log("fittingCursor is null");
 			}
 			_fittingCursor.suspendNotifications();
 			_fittingCursor.setHasPrompt(true);
 			_fittingCursor.setPromptStartBin   ((int) results[CursorEstimator.PROMPT_START]);
 			_fittingCursor.setPromptStopBin    ((int) results[CursorEstimator.PROMPT_STOP]);
 			_fittingCursor.setPromptBaselineValue    (results[CursorEstimator.PROMPT_BASELINE]);
 			_fittingCursor.setTransientStartBin((int) results[CursorEstimator.TRANSIENT_START]);
 			_fittingCursor.setDataStartBin     ((int) results[CursorEstimator.DATA_START]);
 			_fittingCursor.setTransientStopBin ((int) results[CursorEstimator.TRANSIENT_STOP]);
 			_fittingCursor.sendNotifications();
 
 			_excitationPanel = new ExcitationPanel(excitation, _fittingCursor); //TODO ARG excitation cursor change refit problem here; get new values before excitation ready for refit
 
 			success = true;
 		}
 		else {
 			_fittingCursor.setHasPrompt(false);
 		}
 		return success;
 	}
 
 	private void getFitSettings(IGrayScaleImage grayScalePanel, IUserInterfacePanel uiPanel, FittingCursor cursor) {
 		_channel        = grayScalePanel.getChannel();
 
 		_region         = uiPanel.getRegion();
 		_algorithm      = uiPanel.getAlgorithm();
 		_function       = uiPanel.getFunction();
 		_fitAllChannels = uiPanel.getFitAllChannels();
 
 		_x              = uiPanel.getX();
 		_y              = uiPanel.getY();
 		_threshold      = uiPanel.getThreshold();
 
 		_param          = uiPanel.getParameters();
 		_free           = uiPanel.getFree();
 
 		_startBin       = cursor.getDataStartBin(); //TODO ARG 9/28/12 was getTrans.StartBin
 		_stopBin        = cursor.getTransientStopBin();
 	}
 
 	/**
 	 * Prompts for a FLIM file.
 	 *
 	 * @param default path
 	 * @return non-null array of Files
 	 */
 	final private File[] showFileDialog(String defaultPath) {
 		JFileChooser chooser = new JFileChooser();
 		chooser.setCurrentDirectory(new java.io.File(defaultPath));
 		chooser.setDialogTitle("Open Lifetime Image(s)");
 		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 		chooser.setMultiSelectionEnabled(true);
 		chooser.setFileFilter(new showFileDialogFilter());
 
 		if (chooser.showOpenDialog(ij.ImageJ.getFrames()[0]) == JFileChooser.APPROVE_OPTION) {
 			File[] files = chooser.getSelectedFiles();
 			List<File> fileList = new ArrayList<File>();
 			for (File file : files) {
 				if (file.isDirectory()) {
 					for (File f : file.listFiles()) {
 						if (f.getName().endsWith(ICS_SUFFIX)
 								|| f.getName().endsWith(SDT_SUFFIX))
 						{
 							fileList.add(f);
 						}
 					}
 				}
 				else {
 					fileList.add(file);
 				}
 			}
 			return fileList.toArray(new File[fileList.size()]);
 		}
 		return NO_FILES_SELECTED;
 	}
 
 	private class showFileDialogFilter extends FileFilter {
 		@Override
 		public boolean accept(File f) {
 			if (f.getName().endsWith(ICS_SUFFIX)) {
 				return true;
 			}
 			if (f.getName().endsWith(SDT_SUFFIX)) {
 				return true;
 			}
 			if (f.isDirectory()) {
 				return true;
 			}
 			return false;
 		}
 		@Override
 		public String getDescription() {
 			return "Lifetime .ics & .sdt";
 		}
 	}
 
 	private ImgPlus<T> loadImage(String path, String file) {
 		return loadImage(path + file);
 	}
 
 	@SuppressWarnings("rawtypes")
 	private ImgPlus loadImage(final String filePath) {
 		ImgPlus image = null;
 		try {
 			// determine file format
 			final SCIFIO scifio = new SCIFIO();
 			final Format format = scifio.format().getFormat(filePath);
 
 			// NB: Would be nice if Metadata were attached to the SCIFIOImgPlus
 			// directly, and then we won't need to go through this rigamarole here.
 			// See: https://github.com/scifio/scifio/issues/135
 
 			// parse metadata
 			final Metadata meta = format.createParser().parse(filePath);
 			_globalMetadata = meta.getTable();
 
 			// create reader
 			final ImgOpener imgOpener = new ImgOpener(scifio.getContext());
 			final Reader reader = format.createReader();
 			reader.setMetadata(meta);
 
 			// open the image
 			image = imgOpener.openImg(reader, new SCIFIOConfig());
 		}
 		catch (final Exception e) {
 			IJ.handleException(e);
 			return null;
 		}
 		if (image == null) {
 			IJ.error("imageOpener returned null image");
 		}
 
 		return image;
 	}
 
 	private boolean getImageInfo(ImgPlus<T> image) {
 		long[] dimensions = new long[0];
 		try {
 			dimensions = new long[image.numDimensions()];
 			image.dimensions(dimensions);
 			//IJ.log("dimensions size is " + dimensions.length);
 			//for (int i : dimensions) {
 			//    System.out.print("" + i + " ");
 			//}
 			//IJ.log();
 		}
 		catch (NullPointerException e) {
 			IJ.log("can't detect channels");
 			IJ.handleException(e);
 			return false;
 		}
 		Integer xIndex, yIndex, lifetimeIndex, channelIndex;
 		_width = (int) ImageUtils.getWidth(image);
 		_height = (int) ImageUtils.getHeight(image);
 		_channels = (int) ImageUtils.getNChannels(image);
 		//TODO this is broken; returns 1 when there are 16 channels; corrected below
 		//IJ.log("ImageUtils.getNChannels returns " + _channels);
 		_hasChannels = false;
 		if (dimensions.length > 3) {
 			_hasChannels = true;
 			_channelIndex = 3;
 			_channels = (int) dimensions[_channelIndex];
 		}
 		//IJ.log("corrected to " + _channels);
 		_bins = (int) ImageUtils.getDimSize(image, SCIFIOAxes.LIFETIME);
 		_binIndex = 2;
 		//IJ.log("width " + _width + " height " + _height + " timeBins " + _bins + " channels " + _channels);
 		_cursor = image.randomAccess();
 
 		_timeRange = 10.0f;
 		_increment = 1;
 		if (null != _globalMetadata) {
 			Number timeBase = (Number) _globalMetadata.get("time base");
 			if (null != timeBase) {
 				_timeRange = timeBase.floatValue();
 			}
 			Number increment = (Number) _globalMetadata.get("MeasureInfo.incr");
 			if (null != increment) {
 				_increment = increment.intValue();
 				//IJ.log("MeasureInfo.incr is " + _increment);
 			}
 		}
 		_timeRange /= _bins;
 
 		return true;
 	}
 
 	/**
 	 * Restores path name from Java Preferences.
 	 *
 	 * @return String with path name
 	 */
 	private String getPathFromPreferences() {
 		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
 		return prefs.get(PATH_KEY, "");
 	}
 
 	/**
 	 * Saves the path name to Java Preferences.
 	 *
 	 * @param path
 	 */
 	private void savePathInPreferences(String path) {
 		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
 		prefs.put(PATH_KEY, path);
 	}
 
 	/*
 	 * Fits the data as requested by UI.
 	 */
 	private void fitData(IUserInterfacePanel uiPanel) {
 		ImgPlus<DoubleType> fittedImage = null;
 		// only one fit at a time
 		synchronized (_synchFit) {
 
 			switch (_region) {
 				case SUMMED:
 					// sum all pixels
 					fittedImage = fitSummed(uiPanel);
 					break;
 				case ROI:
 					// fit summed ROIs
 					fittedImage = fitROIs(uiPanel);
 					break;
 				case POINT:
 					// fit single pixel
 					fittedImage = fitPixel(uiPanel, _x, _y);
 					break;
 				case EACH:
 					// fit every pixel
 					fittedImage = fitImage(uiPanel, _grayScaleImage.getChannel(), false);
 					break;
 			}
 		}
 		if (null != fittedImage) {
 			for (String analysis : uiPanel.getAnalysisList()) {
 				_analysis.doAnalysis(analysis, fittedImage, uiPanel.getRegion(), uiPanel.getFunction(), uiPanel.getFittedImages());
 			}
 		}
 	}
 
 	private FitInfo getFitInfo(
 		IUserInterfacePanel uiPanel,
 		int channel,
 		FittingCursor fittingCursor) {
 		FitInfo fitInfo = new FitInfo();
 		fitInfo.setChannel(channel);
 		fitInfo.setRegion(uiPanel.getRegion());
 		fitInfo.setAlgorithm(uiPanel.getAlgorithm());
 		fitInfo.setFunction(uiPanel.getFunction());
 		fitInfo.setNoiseModel(uiPanel.getNoiseModel());
 		fitInfo.setFittedImages(uiPanel.getFittedImages());
 		fitInfo.setColorizeGrayScale(uiPanel.getColorizeGrayScale());
 		fitInfo.setAnalysisList(uiPanel.getAnalysisList());
 		fitInfo.setFitAllChannels(uiPanel.getFitAllChannels());
 		fitInfo.setTransientStart(fittingCursor.getTransientStartBin());
 		fitInfo.setDataStart(fittingCursor.getDataStartBin());
 		fitInfo.setTransientStop(fittingCursor.getTransientStopBin());
 		fitInfo.setThreshold(uiPanel.getThreshold());
 		fitInfo.setChiSquareTarget(uiPanel.getChiSquareTarget());
 		fitInfo.setBinning(uiPanel.getBinning());
 		fitInfo.setX(uiPanel.getX());
 		fitInfo.setY(uiPanel.getY());
 		fitInfo.setParameterCount(uiPanel.getParameterCount());
 		fitInfo.setParameters(uiPanel.getParameters());
 		fitInfo.setFree(translateFree(uiPanel.getFunction(), uiPanel.getFree()));
 		fitInfo.setRefineFit(uiPanel.getRefineFit());
 		return fitInfo;
 	}
 
 	/**
 	 * Fits all the pixels in the image.  Gets fit settings from the UI panel
 	 * and various globals.
 	 * 
 	 * @param uiPanel
 	 * @param channel
 	 * @param batch whether or not batch processing is in effect
 	 * @return 
 	 */
 	private ImgPlus<DoubleType> fitImage(IUserInterfacePanel uiPanel, int channel, boolean batch) {
 		// get fit settings from the UI panel
 		FitInfo fitInfo = getFitInfo(uiPanel, channel, _fittingCursor);
 		fitInfo.setXInc(_timeRange);
 		if (_fittingCursor.getHasPrompt() && null != _excitationPanel) {
 			int startIndex = _fittingCursor.getPromptStartBin();
 			int stopIndex  = _fittingCursor.getPromptStopBin();
 			double base  = _fittingCursor.getPromptBaselineValue();
 			double[] values = _excitationPanel.getValues(startIndex, stopIndex, base);
 			fitInfo.setPrompt(values);
 		}
 		IndexColorModel indexColorModel = HistogramTool.getIndexColorModel();
 		fitInfo.setIndexColorModel(indexColorModel);
 		_fitInfo = fitInfo;
 
 		// set up images
 		IDecayImage decayImage = new DecayImageWrapper(_image, _width, _height, _channels, _bins, _binIndex, _increment);
 		IFittedImage previousImage = null;
 		int width = decayImage.getWidth();
 		int height = decayImage.getHeight();
 		int channels = 1;
 		if (fitInfo.getFitAllChannels()) {
 			channels = decayImage.getChannels();
 		}
 		int parameters = fitInfo.getParameterCount();
 		//TODO ARG image names currently coded like "gpl1.sdt [X Y Lifetime]"
 		String title = _image.getName();
 		int index = title.indexOf("[");
 		if (-1 != index) {
 			title = title.substring(0, index - 1);
 		}
 		String fitTitle = fitInfo.getFitTitle();
 		IFittedImage newImage = new OutputImageWrapper(title, fitTitle, width, height, channels, parameters);
 
 		// set up preprocessor chain
 		IProcessor processor = decayImage;
 		if (null != getRois() && getRois().length > 0) {
 			// skip pixels out of Rois
 			IProcessor roiProcessor = new RoiProcessor(getRois());
 			roiProcessor.chain(processor);
 			processor = roiProcessor;
 		}
 		if (fitInfo.getThreshold() > 0) {
 			// skip pixels below threshold
 			IProcessor threshold = new Threshold(fitInfo.getThreshold());
 			threshold.chain(processor);
 			processor = threshold;
 		}
 		ISLIMBinner binner = _binning.getBinner(uiPanel.getBinning());
 		if (null != binner) {
 			// do binning
 			binner.init(_width, _height);
 			binner.chain(processor);
 			processor = binner;
 		}
 
 		// create a fitting engine to use
 		IFittingEngine fittingEngine = Configuration.getInstance().getFittingEngine();
 		ICurveFitter curveFitter = getCurveFitter(uiPanel); //TODO ARG shouldn't all UI panel info go into FitInfo???
 		fittingEngine.setCurveFitter(curveFitter);
 
 		return fitImage(fittingEngine, fitInfo, decayImage, processor, previousImage, newImage, batch);
 
 	}
 
 	/**
 	 * Fits all the pixels in the image.
 	 * 
 	 * @param fittingEngine fitting code to use
 	 * @param fitInfo fit settings
 	 * @param decayImage contains the decay data
 	 * @param previousImage previous fit results, may be null
 	 * @param newImage results of this fit
 	 * @param batch whether or not batch processing is in effect
 	 * @return 
 	 */
 	private ImgPlus<DoubleType> fitImage(
 		IFittingEngine fittingEngine,
 		FitInfo fitInfo,
 		IDecayImage decayImage,
 		IProcessor processor, //TODO ARG really need both decayImage & processor?  Processor is a poor name
 		IFittedImage previousImage,
 		IFittedImage newImage,
 		boolean batch)
 		{
 
 		// get commonly-used items in local variables
 		int width = decayImage.getWidth();
 		int height = decayImage.getHeight();
 		int channels = decayImage.getChannels();
 		int bins = decayImage.getBins();
 		int channel = fitInfo.getChannel();
 		boolean fitAllChannels = fitInfo.getFitAllChannels();
 
 		// needed to display progress bar
 		int pixelCount = 0;
 		int totalPixelCount = totalPixelCount(width, height, channels, fitAllChannels);
 		int pixelsToProcessCount = 0;
 
 		// show errors on grayscale
 		ErrorManager errorManager = new ErrorManager(width, height, channels);
 		errorManager.setListener(_grayScaleImage);
 
 		// handle optionally producing fitted images during the fit
 		int fittedChannels = 1;
 		if (fitAllChannels) {
 			fittedChannels = channels;
 		}
 		int[] dimension = new int[] { width, height, fittedChannels };
 		FittedImageFitter fitter = null;
 		String outputs = fitInfo.getFittedImages();
 		if (!batch && null != outputs) {
 			int channelNumber = -1;
 			if (channels > 1 && !fitAllChannels) {
 				channelNumber = channel + 1;
 			}
 			int components = fitInfo.getComponents();
 			boolean stretched = fitInfo.getStretched();
 			FittedImageParser parser =
 					new FittedImageParser(outputs, components, stretched,
 						fitInfo.getFree());
 			FittedImageType[] outputImages = parser.getFittedImages();
 			fitter = new FittedImageFitter();
 			fitter.setUpFit(
 				_file,
 				outputImages,
 				channelNumber,
 				++_fittingOrdinal,
 				dimension,
 				fitInfo.getIndexColorModel(),
 				components,
 				fitInfo.getColorizeGrayScale(),
 				_grayScaleImage);
 			fitter.beginFit();
 		}
 
 		// set up global, image-wide fit parameters
 		//TODO revisit all of these
 		IGlobalFitParams globalFitParams = new GlobalFitParams();
 		globalFitParams.setEstimator(new FitterEstimator());
 		globalFitParams.setFitAlgorithm(fitInfo.getAlgorithm());
 		globalFitParams.setFitFunction(fitInfo.getFunction());
 		globalFitParams.setNoiseModel(fitInfo.getNoiseModel());
 		globalFitParams.setTransientStart(fitInfo.getTransientStart());
 		globalFitParams.setDataStart(fitInfo.getDataStart());
 		globalFitParams.setTransientStop(fitInfo.getTransientStop());
 		globalFitParams.setXInc(fitInfo.getXInc());
 		globalFitParams.setPrompt(fitInfo.getPrompt());
 		globalFitParams.setStartPrompt(fitInfo.getStartPrompt());
 		globalFitParams.setStopPrompt(fitInfo.getStopPrompt());
 		globalFitParams.setChiSquareTarget(fitInfo.getChiSquareTarget());
 		globalFitParams.setFree(fitInfo.getFree());
 
 		// initialize class used for 'chunky pixel' effect
 		IChunkyPixelTable chunkyPixelTable = new ChunkyPixelTableImpl();
 
 		List<ChunkyPixel> pixelList = new ArrayList<ChunkyPixel>();
 		List<ILocalFitParams> localFitParamsList = new ArrayList<ILocalFitParams>();
 
 		// loop over all channels or just the current one
 		for (int c : getChannelIndices(fitAllChannels, channel, channels)) {
 			// 'chunky pixel' effect: draw staggered pixels, not sequential
 			ChunkyPixelEffectIterator pixelIterator =
 					new ChunkyPixelEffectIterator(chunkyPixelTable, width, height);
 
 			while (!fitInfo.getCancel() && pixelIterator.hasNext()) {
 				if (!batch) {
 					IJ.showProgress(++pixelCount, totalPixelCount);
 				}
 				ChunkyPixel pixel = pixelIterator.next();
 
 				// compute full location information
 				int x = pixel.getX();
 				int y = pixel.getY();
 				int[] inputLocation = new int[] { x, y, c };
 				int[] outputLocation = new int[] { x, y, fitAllChannels ? c : 0 };
 
 				double[] decay = processor.getPixel(inputLocation);
 
 				// fit this pixel?
 				if (null != decay) {
 					// set up local, pixel fit parameters
 					ILocalFitParams localFitParams = new LocalFitParams();
 					localFitParams.setY(decay);
 					localFitParams.setSig(null);
 					localFitParams.setParams(fitInfo.getParameters());
 					double[] yFitted = new double[bins];
 					localFitParams.setYFitted(yFitted);
 
 					pixel.setInputLocation(inputLocation);
 					pixel.setOutputLocation(outputLocation);
 					pixelList.add(pixel);
 					localFitParamsList.add(localFitParams);
 
 					if (++pixelsToProcessCount >= PIXEL_COUNT) {
 						pixelsToProcessCount = 0;
 
 						ChunkyPixel[] pixelArray = pixelList.toArray(new ChunkyPixel[0]);
 						pixelList.clear();
 						ILocalFitParams[] localFitParamsArray = localFitParamsList.toArray(new ILocalFitParams[0]);
 						localFitParamsList.clear();
 
 						processPixels(fittingEngine, pixelArray, globalFitParams, localFitParamsArray, errorManager, fitter, newImage, batch);
 					}
 				}
 			}
 		}
 
 		if (fitInfo.getCancel()) {
 			IJ.showProgress(0, 0);
 			cancelImageFit();
 			if (null != fitter) {
 				fitter.cancelFit();
 			}
 			return null;
 		}
 
 		//IJ.log("fitImage pixelsToProcessCount leftover " + pixelsToProcessCount);
 		if (pixelsToProcessCount > 0) {
 			ChunkyPixel[] pixelArray = pixelList.toArray(new ChunkyPixel[0]);
 			//IJ.log("process remainder " + pixelArray.length);
 			ILocalFitParams[] localFitParamsArray = localFitParamsList.toArray(new ILocalFitParams[0]);
 			processPixels(fittingEngine, pixelArray, globalFitParams, localFitParamsArray, errorManager, fitter, newImage, batch);
 		}
 		if (null != fitter) {
 			fitter.endFit();
 		}
 
 		return newImage.getImage();
		}
 
 	/**
 	 * Helper function that processes an array of pixels.  When creating
 	 * colorized images from fit parameters, the histogram and images are
 	 * updated at the end of this function.
 	 *
 	 * @param fittingEngine
 	 * @param pixels
 	 * @param globalFitParams
 	 * @param localFitParams
 	 * @param errorManager
 	 * @param imageColorizer
 	 * @param fittedImage
 	 * @param batch whether or not batch processing is in effect
 	 */
 	private void processPixels(
 		IFittingEngine fittingEngine,
 		ChunkyPixel[] pixels,
 		IGlobalFitParams globalFitParams,
 		ILocalFitParams[] localFitParams,
 		ErrorManager errorManager,
 		FittedImageFitter imageColorizer,
 		IFittedImage fittedImage,
 		boolean batch)
 	{
 
 		//TODO use Lists or just arrays? This just converts from array to List.
 		List<ILocalFitParams> localFitParamsList = new ArrayList<ILocalFitParams>();
 		for (ILocalFitParams lFP : localFitParams) {
 			localFitParamsList.add(lFP);
 		}
 
 		List<IFitResults> resultsList = new ArrayList<IFitResults>();
 		try {
 			resultsList = fittingEngine.fit(globalFitParams, localFitParamsList);
 		}
 		catch (Exception e) {
 			IJ.log("Exception " + e.getMessage());
 		}
 
 		for (int i = 0; i < resultsList.size(); ++i) {
 			IFitResults result = resultsList.get(i);
 			double[] params = result.getParams();
 			ChunkyPixel p = pixels[i];
 			int[] location = p.getOutputLocation();
 
 			// check for errors
 			if (Double.isNaN(params[0])) {
 				if (!batch && null != errorManager) {
 					int x = location[0];
 					int y = location[1];
 					int channel = 0;
 					if (location.length > 2) {
 						channel = location[2];
 					}
 					errorManager.noteError(x, y, channel);
 				}
 				//TODO ARG need to draw a NaN here over any prior chunky pixels (when chunky pixels are working right)
 			}
 			else {
 				// if producing colorized images, feed this pixel to colorizer
 				if (null != imageColorizer) {
 					imageColorizer.updatePixel(location, params);
 				}
 				fittedImage.setPixel(location, params);
 			}
 		}
 
 		if (null != imageColorizer) {
 			// update any fitted images
 			imageColorizer.updateLUTRange();
 		}
 	}
 
 	//TODO ARG
 	// this variant of fitSummed is based on a similar one for fitPixel
 	// both of these should really be getting the start/stopBin info from the
 	// uiPanel.  There is a race however, one FittingCursorListener triggers the
 	// fit and one FittingCursorListener updates the uiPanel (when dragging the
 	// cursors; if you type in new ones the uiPanel will be up-to-date).
 
 	// added kludge to make moving cursors in DecayGraph do a refit.
 	private void fitSummed(IUserInterfacePanel uiPanel, FittingCursor fittingCursor) {
 		_startBin = fittingCursor.getDataStartBin();
 		_stopBin = fittingCursor.getTransientStopBin();
 		fitSummed(uiPanel);
 	}
 
 	/*
 	 * Sums all pixels and fits the result.
 	 */
 	private ImgPlus<DoubleType> fitSummed(IUserInterfacePanel uiPanel) {
 		ImgPlus<DoubleType> fittedPixels = null;
 
 		_grayScaleImage.hideCursor();
 
 		double params[] = uiPanel.getParameters(); //TODO go cumulative; i.e. refit with last fit results as estimate
 
 		//IJ.log("FIT SUMMED startBin " + _startBin + " stopBin " + _stopBin);
 
 		// set up the source
 		IDecayImage decayImage = new DecayImageWrapper(_image, _width, _height, _channels, _bins, _binIndex, _increment);
 		IProcessor processor = decayImage;
 		if (null != getRois() && getRois().length > 0) {
 			// add input processor to skip pixels out of Rois
 			IProcessor roiProcessor = new RoiProcessor(getRois());
 			roiProcessor.chain(processor);
 			processor = roiProcessor;
 		}
 		if (uiPanel.getThreshold() > 0) {
 			// add input processor to skip pixels below threshold
 			IProcessor threshold = new Threshold(uiPanel.getThreshold());
 			threshold.chain(processor);
 			processor = threshold;
 		}
 
 		// build the data
 		ArrayList<ICurveFitData> curveFitDataList = new ArrayList<ICurveFitData>();
 		ICurveFitData curveFitData;
 		double yCount[];
 		double yFitted[];
 		int photons = 0;
 
 		// loop over all channels or just the current one
 		int[] inputLocation = new int[] { 0, 0, 0 };
 		for (int channel : getChannelIndices(_fitAllChannels, _channel, _channels)) {
 			inputLocation[2] = channel;
 			curveFitData = new CurveFitData();
 			curveFitData.setParams(params.clone()); //TODO NO NO NO s/b either from UI or fitted point or fitted whole image
 			yCount = new double[_bins];
 			for (int b = 0; b < _bins; ++b) {
 				yCount[b] = 0.0;
 			}
 
 			// count photons and pixels
 			int pixels = 0;
 
 			// sum this channel
 			for (int y = 0; y < _height; ++y) {
 				for (int x = 0; x < _width; ++x) {
 					inputLocation[0] = x;
 					inputLocation[1] = y;
 					double[] decay = processor.getPixel(inputLocation);
 					if (null != decay) {
 						for (int b = 0; b < _bins; ++b) {
 							yCount[b] += decay[b];
 							photons += (int) decay[b];
 						}
 						++pixels;
 					}
 				}
 			}
 			curveFitData.setYCount(yCount);
 			curveFitData.setTransStartIndex(0);
 			curveFitData.setDataStartIndex(_startBin);
 			curveFitData.setTransEndIndex(_stopBin);
 			yFitted = new double[_bins];
 			curveFitData.setYFitted(yFitted);
 
 			// use zero for current channel if it's the only one
 			int nominalChannel = _fitAllChannels ? channel : 0;
 			curveFitData.setChannel(nominalChannel);
 			curveFitData.setX(0);
 			curveFitData.setY(0);
 			curveFitData.setPixels(pixels);
 			curveFitDataList.add(curveFitData);
 		}
 
 		// do the fit
 		ICurveFitData dataArray[] = curveFitDataList.toArray(new ICurveFitData[0]);
 		getCurveFitter(uiPanel).fitData(dataArray);
 
 		// show decay and update UI parameters
 		int visibleChannel = _fitAllChannels ? _channel : 0;
 		String title = "Summed ";
 		if (1 < _channels) {
 			title += "Channel " + (_channel + 1);
 		}
 		title += _file.substring(0, _file.lastIndexOf('.'));
 		showDecayGraph(title, uiPanel, _fittingCursor,
 			dataArray[visibleChannel], photons);
 		//TODO AIC experimental code; second parameter is actually AIC
 		uiPanel.setParameters(dataArray[visibleChannel].getParams(), dataArray[visibleChannel].getChiSquare());
 
 		// get the results
 		int channels = _fitAllChannels ? _channels : 1;
 		//fittedPixels = makeImage(channels, 1, 1, uiPanel.getParameterCount()); //TODO ImgLib bug if you use 1, 1, 1, 4; see "imglibBug()" below.
 		fittedPixels = makeImage(channels + 1, 2, 2, uiPanel.getParameterCount()); //TODO this is a workaround; unused pixels will remain NaNs
 		RandomAccess<DoubleType> resultsCursor = fittedPixels.randomAccess();
 		setFittedParamsFromData(resultsCursor, dataArray);
 		return fittedPixels;
 	}
 
 	/*
 	 * Sums and fits each ROI.
 	 */
 	private ImgPlus<DoubleType> fitROIs(IUserInterfacePanel uiPanel) {
 		ImgPlus<DoubleType> fittedPixels = null;
 		double params[] = uiPanel.getParameters();
 
 		// build the data
 		ArrayList<ICurveFitData> curveFitDataList = new ArrayList<ICurveFitData>();
 		ICurveFitData curveFitData;
 		double yCount[];
 		double yFitted[];
 		int[] photons = new int[getRois().length];
 		for (int i = 0; i < photons.length; ++i) {
 			photons[i] = 0;
 		}
 
 		// loop over all channels or just the current one
 		for (int channel : getChannelIndices(_fitAllChannels, _channel, _channels)) {
 			int roiNumber = 1;
 			for (Roi roi: getRois()) {
 				curveFitData = new CurveFitData();
 				curveFitData.setParams(params.clone());
 				yCount = new double[_bins];
 				for (int b = 0; b < _bins; ++b) {
 					yCount[b] = 0.0;
 				}
 				Rectangle bounds = roi.getBounds();
 				int pixels = 0;
 				for (int x = 0; x < bounds.width; ++x) {
 					for (int y = 0; y < bounds.height; ++y) {
 						if (roi.contains(bounds.x + x, bounds.y + y)) {
 							++pixels;
 							for (int b = 0; b < _bins; ++b) {
 								double count = getData(_cursor, channel, x, y, b);
 								yCount[b] += count;
 								photons[roiNumber - 1] += count;
 							}
 						}
 					}
 				}
 				curveFitData.setYCount(yCount);
 				curveFitData.setTransStartIndex(0);
 				curveFitData.setDataStartIndex(_startBin);
 				curveFitData.setTransEndIndex(_stopBin);
 
 				yFitted = new double[_bins];
 				curveFitData.setYFitted(yFitted);
 
 				// use zero for current channel if it's the only one
 				int nominalChannel = _fitAllChannels ? channel : 0;
 				curveFitData.setChannel(nominalChannel);
 				curveFitData.setX(roiNumber - 1);
 				curveFitData.setY(0);
 				curveFitData.setPixels(pixels);
 				curveFitDataList.add(curveFitData);
 
 				++roiNumber;
 			}
 		}
 
 		// do the fit
 		ICurveFitData dataArray[] = curveFitDataList.toArray(new ICurveFitData[0]);
 		getCurveFitter(uiPanel).fitData(dataArray);
 
 		// show the decay graphs
 		double min = Double.MAX_VALUE;
 		double max = -Double.MAX_VALUE;
 		int roiNumber = 1;
 		for (Roi roi: getRois()) {
 			int nominalChannel = _fitAllChannels ? _channel : 0;
 			int dataIndex = nominalChannel * getRois().length + (roiNumber - 1);
 
 			String title = "Roi " + roiNumber;
 			if (1 < _channels) {
 				title += " Channel " + (_channel + 1);
 			}
 			showDecayGraph(title, uiPanel, _fittingCursor,
 				dataArray[dataIndex], photons[roiNumber - 1]);
 			double lifetime = dataArray[dataIndex].getParams()[3];
 			if (lifetime < min) {
 				min = lifetime;
 			}
 			if (lifetime > max) {
 				max = lifetime;
 			}
 			++roiNumber;
 		}
 
 		// show colorized lifetimes
 		ImageProcessor imageProcessor = new ColorProcessor(_width, _height);
 		ImagePlus imagePlus = new ImagePlus("ROIs Fitted Lifetimes", imageProcessor);
 		roiNumber = 1;
 		for (Roi roi: getRois()) {
 			int nominalChannel = _fitAllChannels ? _channel : 0;
 			int dataIndex = nominalChannel * getRois().length + (roiNumber - 1);
 			double lifetime = dataArray[dataIndex].getParams()[3];
 
 			imageProcessor.setColor(lifetimeColorMap(min, max, lifetime));
 
 			Rectangle bounds = roi.getBounds();
 			for (int x = 0; x < bounds.width; ++x) {
 				for (int y = 0; y < bounds.height; ++y) {
 					if (roi.contains(bounds.x + x, bounds.y + y)) {
 						imageProcessor.drawPixel(bounds.x + x, bounds.y + y);
 					}
 				}
 			}
 			++roiNumber;
 		}
 		imagePlus.show();
 
 		// update UI parameters
 		//TODO AIC experimental code; second parameter is actually AIC
 		uiPanel.setParameters(dataArray[0].getParams(), dataArray[0].getChiSquare()); //TODO, just picked first ROI here!
 
 		// get the results
 		int channels = _fitAllChannels ? _channels : 1;
 		//fittedPixels = makeImage(channels, 1, 1, uiPanel.getParameterCount()); //TODO ImgLib bug if you use 1, 1, 1, 4; see "imglibBug()" below.
 		fittedPixels = makeImage(channels + 1, getRois().length + 1, 2, uiPanel.getParameterCount()); //TODO this is a workaround; unused pixels will remain NaNs
 		RandomAccess<DoubleType> resultsCursor = fittedPixels.randomAccess();
 		setFittedParamsFromData(resultsCursor, dataArray);
 		return fittedPixels;
 	}
 
 	// added kludge to make moving cursors in DecayGraph do a refit. //TODO this has to change FittingCursor will know whenever cursors change.
 	private ImgPlus<DoubleType> fitPixel(
 		IUserInterfacePanel uiPanel,
 		FittingCursor fittingCursor) {
 		int x = uiPanel.getX();
 		int y = uiPanel.getY();
 		_startBin = fittingCursor.getDataStartBin();
 		_stopBin = fittingCursor.getTransientStopBin();
 //		IJ.log("_startBin is " + _startBin + " _stopBin " + _stopBin);
 //		IJ.log("FYI FWIW prompt delay is " + _fittingCursor.getPromptDelay());
 //		IJ.log("prompt start is " + _fittingCursor.getPromptStartValue() + " stop " + _fittingCursor.getPromptStopValue());
 //		IJ.log("_fittingCursor start value " + _fittingCursor.getTransientStartValue() + " bin " + _fittingCursor.getTransientStartBin() + " stop value " + _fittingCursor.getTransientStopValue() + " bin " + _fittingCursor.getTransientStopBin());
 		return fitPixel(uiPanel, x, y);
 	}
 
 	/*
 	 * Fits a given pixel.
 	 * 
 	 * @param x
 	 * @param y
 	 */
 	private ImgPlus<DoubleType> fitPixel(IUserInterfacePanel uiPanel, int x, int y) {
 		ImgPlus<DoubleType> fittedPixels = null;
 
 		_grayScaleImage.showCursor(_x, _y);
 
 		// set up the source
 		IDecayImage decayImage = new DecayImageWrapper(_image, _width, _height, _channels, _bins, _binIndex, _increment);
 		IProcessor processor = decayImage;
 		ISLIMBinner binner = _binning.getBinner(uiPanel.getBinning());
 		if (null != binner) {
 			binner.init(_width, _height);
 			binner.chain(processor);
 			processor = binner;
 		}
 
 		// set up the location
 		int[] location = new int[] { _x, _y, _channel };
 
 		// build the data
 		ArrayList<ICurveFitData> curveFitDataList = new ArrayList<ICurveFitData>();
 		double params[] = uiPanel.getParameters(); //TODO wrong; params should possibly come from already fitted data
 		double chiSquareTarget = uiPanel.getChiSquareTarget();
 		ICurveFitData curveFitData;
 		double yCount[];
 		double yFitted[];
 		int photons = 0;
 
 		//SCATTER
 		ICurveFitter curveFitter = getCurveFitter(uiPanel);
 
 		// loop over all channels or just the current one
 		for (int channel : getChannelIndices(_fitAllChannels, _channel, _channels)) {
 			curveFitData = new CurveFitData();
 			curveFitData.setParams(params.clone()); //TODO NO NO NO s/b either from UI or fitted point or fitted whole image
 
 			location[2] = channel;
 			yCount = processor.getPixel(location);
 
 			//SCATTER
 			double scatter = uiPanel.getScatter();
 			if (scatter != 0.0) {
 				IJ.log("scatter " + scatter);
 				yCount = correctForScatter(yCount, curveFitter.getInstrumentResponse(1), _fittingCursor, scatter);
 			}
 
 			curveFitData.setYCount(yCount);
 			int transStartIndex = _fittingCursor.getTransientStartBin();
 			int dataStartIndex = _fittingCursor.getDataStartBin();
 			int transStopIndex = _fittingCursor.getTransientStopBin();
 			curveFitData.setTransStartIndex(transStartIndex);
 			curveFitData.setDataStartIndex(dataStartIndex);
 			curveFitData.setTransEndIndex(transStopIndex);
 
 			//TODO ARG this photon counting needs to be channel specific and also part of summed and ROI fits.
 			photons = 0;
 			for (int c = dataStartIndex; c < transStopIndex; ++c) {
 				if (c < yCount.length) {
 					photons += yCount[c];
 				}
 			}
 
 			yFitted = new double[_bins];
 			curveFitData.setYFitted(yFitted);
 			curveFitData.setChiSquareTarget(chiSquareTarget);
 
 			// use zero for current channel if it's the only one
 			int nominalChannel = _fitAllChannels ? channel : 0;
 			curveFitData.setChannel(nominalChannel);
 			curveFitData.setX(0);
 			curveFitData.setY(0);
 			curveFitData.setPixels(1);
 			curveFitDataList.add(curveFitData);
 		}
 
 		// do the fit
 		ICurveFitData dataArray[] = curveFitDataList.toArray(new ICurveFitData[0]);
 
 		//TODO some test code for RLD fitting experiments:
 		/*
 		RapidLifetimeDetermination rld = new RapidLifetimeDetermination();
 		IJ.log("***TRI2 COMPATIBLE****");
 		rld.rldFit(curveFitter, dataArray[0]);
 		IJ.log("****TRIAL*****");
 		rld.trialRldFit(curveFitter, dataArray[0]); */
 
 		int returnValue = getCurveFitter(uiPanel).fitData(dataArray);
 
 		// show decay graph for visible channel
 		int index = _file.lastIndexOf('.');
 		String title = "Pixel (" + x + "," + y + ") " + _file.substring(0, index);
 		if (1 < _channels) {
 			title += " Channel " + (_channel + 1);
 		}
 		int visibleChannel = 0;
 		if (_fitAllChannels) {
 			visibleChannel = _channel;
 		}
 		showDecayGraph(title, uiPanel, _fittingCursor,
 			dataArray[visibleChannel], photons); //TODO ARG this s/b the photon count for the appropriate channel; currently it will sum all channels.
 
 		// update UI parameters
 		//TODO experimental AIC code; second parameter is actually AIC
 		uiPanel.setParameters(dataArray[visibleChannel].getParams(), dataArray[visibleChannel].getChiSquare());
 
 		// get the results
 		int channels = _fitAllChannels ? _channels : 1;
 		//fittedPixels = makeImage(channels, 1, 1, uiPanel.getParameterCount()); //TODO ImgLib bug if you use 1, 1, 1, 4; see "imglibBug()" below.
 		fittedPixels = makeImage(channels + 1, 2, 2, uiPanel.getParameterCount()); //TODO this is a workaround; unused pixels will remain NaNs
 		RandomAccess<DoubleType> resultsCursor = fittedPixels.randomAccess();
 		setFittedParamsFromData(resultsCursor, dataArray);
 		return fittedPixels;
 	}
 
 	private double[] correctForScatter(double[] decay, double[] prompt, FittingCursor fittingCursor, double scatter) {
 		if (null == prompt) {
 			IJ.log("NO PROMPT LOADED");
 			return decay;
 		}
 		double decayPeak = -Double.MAX_VALUE;
 		for (int i = 0; i < decay.length; ++i) {
 			if (decay[i] > decayPeak) {
 				decayPeak = decay[i];
 			}
 		}
 		double promptPeak = -Double.MAX_VALUE;
 		for (int i = 0; i < prompt.length; ++i) {
 			if (prompt[i] > promptPeak) {
 				promptPeak = prompt[i];
 			}
 		}
 
 		double factor = scatter * decayPeak / promptPeak;
 		int startIndex = fittingCursor.getPromptStartBin();
 		int stopIndex = fittingCursor.getPromptStopBin();
 		IJ.log("factor is " + factor + " start " + startIndex + " stop " + stopIndex);
 
 		double[] corrected = new double[decay.length];
 		for (int i = 0; i < decay.length; ++i) {
 			if (startIndex <= i && i <= stopIndex && i < prompt.length) {
 				corrected[i] = decay[i] - factor * prompt[i];
 				IJ.log("" + decay[i] + " -> " + corrected[i] + " factor was " + factor + " prompt was " + prompt[i]);
 			}
 			else {
 				corrected[i] = decay[i];
 			}
 		}
 		return corrected;
 	}
 
 	/*
 	 * Demonstrates a bug with ImgLib:
 	 * //TODO fix it!
 	 */
 	private void imglibBug() {
 		long[] dim = { 1, 1, 1, 4 };
 		ImgPlus<DoubleType> image = ImageUtils.create("Test", dim);
 
 		// initialize image
 		Cursor<DoubleType> cursor = image.cursor();
 		while (cursor.hasNext()) {
 			IJ.log("fwd");
 			cursor.fwd();
 			cursor.get().set(Double.NaN);
 		}
 	}
 
 	/**
 	 * Gets an array of channel indices to iterate over.
 	 *
 	 * @param fitAllChannels
 	 * @param channel current channel
 	 * @param channels number of channels
 	 * @return
 	 */
 	private int[] getChannelIndices(boolean fitAllChannels, int channel, int channels) {
 		if (fitAllChannels) {
 			int[] channelIndices = new int[channels];
 			for (int c = 0; c < channels; ++c) {
 				channelIndices[c] = c;
 			}
 			return channelIndices;
 		}
 		else {
 			return new int[] { channel };
 		}
 	}
 
 	/**
 	 * Calculates the total number of pixels to fit.  Used for
 	 * progress bar.
 	 *
 	 * @param channels
 	 * @param fitAll
 	 * @return
 	 */
 	private int totalPixelCount(int x, int y, int channels, boolean fitAll) {
 		int count = x * y;
 		if (fitAll) {
 			count *= channels;
 		}
 		return count;
 	}
 
 	/**
 	 * Calculates an array of channel indices to iterate over.
 	 *
 	 * @param channel
 	 * @param channels
 	 * @param visibleFit
 	 * @param fitAll
 	 * @return
 	 */
 	private int[] channelIndexArray(int channel, int channels, boolean visibleFit, boolean fitAll) {
 		int returnValue[] = { };
 		if (fitAll) {
 			returnValue = new int[visibleFit ? channels - 1 : channels];
 			int i = 0;
 			for (int c = 0; c < channels; ++c) {
 				// skip visible; already processed
 				if (c != channel || !visibleFit) {
 					returnValue[i++] = c;
 				}
 			}
 		}
 		else if (!visibleFit) {
 			// single channel, not processed yet
 			returnValue = new int[1];
 			returnValue[0] = channel;
 		}
 		return returnValue;
 	}
 
 	private double getData(RandomAccess<T> cursor, int channel, int x, int y, int bin) {
 		int dim[];
 		if (_hasChannels) {
 			dim = new int[] { x, y, bin, channel }; //TODO ARG is this order guaranteed?
 		}
 		else {
 			dim = new int[] { x, y, bin };
 		}
 		cursor.setPosition(dim);
 		return cursor.get().getRealFloat() / _minNonZeroPhotonCount;
 	}
 
 	/**
 	 * Helper routine to create imglib.Image to store fitted results.
 	 *
 	 * @param width
 	 * @param height
 	 * @param components
 	 * @return
 	 */
 	private ImgPlus<DoubleType> makeImage(int channels, int width, int height, int parameters) {
 		ImgPlus<DoubleType> image = null;
 
 //		IJ.log("channels width height params " + channels + " " + width + " " + height + " " + parameters);
 
 		// create image object
 		long[] dim = { width, height, channels, parameters }; //TODO when we keep chi square in image  ++parameters };
 		image = ImageUtils.create("Fitted", dim);
 
 		// initialize image
 		Cursor<DoubleType> cursor = image.cursor();
 		while (cursor.hasNext()) {
 			cursor.fwd();
 			cursor.get().set(Double.NaN);
 		}
 
 		return image;
 	}
 
 	private double[] getFittedParams(RandomAccess<DoubleType> cursor, int channel, int x, int y, int count) {
 		double params[] = new double[count];
 		int position[] = new int[4];
 		position[0] = x;
 		position[1] = y;
 		position[2] = channel;
 		for (int i = 0; i < count; ++i) {
 			position[3] = i;
 			cursor.setPosition(position);
 			params[i] = cursor.get().getRealDouble();
 		}
 		return params;
 	}
 
 	private void setFittedParamsFromData(RandomAccess<DoubleType> cursor, ICurveFitData dataArray[]) {
 		int x, y;
 		double[] params;
 		for (ICurveFitData data : dataArray) {
 			setFittedParams(cursor, data.getChannel(), data.getX(), data.getY(), data.getParams());
 		}
 	}
 
 	private void setFittedParams(RandomAccess<DoubleType> cursor, int channel, int x, int y, double[] params) {
 		int position[] = new int[4];
 		position[0] = x;
 		position[1] = y;
 		position[2] = channel;
 		for (int i = 0; i < params.length; ++i) {
 			position[3] = i;
 			cursor.setPosition(position);
 			cursor.get().set(params[i]);
 		}
 	}
 
 	private void cancelImageFit() {
 		_fittedImage = null;
 		_fittedParameterCount = 0;
 	}
 
 	/**
 	 * Checks whether a given pixel is included in ROIs.  If no ROIs are
 	 * selected then all pixels are included.
 	 *
 	 * @param x
 	 * @param y
 	 * @return whether or not included in ROIs
 	 */
 	boolean isInROIs(int x, int y) {
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
 
 	/**
 	 * Gets a list of ROIs (may be empty).
 	 *
 	 * @return array of ROIs.
 	 */
 	private Roi[] getRois() {
 		Roi[] rois = {};
 		RoiManager manager = RoiManager.getInstance();
 		if (null != manager) {
 			rois = manager.getRoisAsArray();
 		}
 		return rois;
 	}
 
 	/**
 	 * Colorizes a given lifetime value.
 	 *
 	 * Note this is much cruder than the DataColorizer that is
 	 * used in fitEachPixel.
 	 *
 	 * @param min
 	 * @param max
 	 * @param lifetime
 	 * @return
 	 */
 	//TODO make consistent with fitEachPixel's DataColorizer
 	//TODO this needs to use LUTs
 	private Color lifetimeColorMap(double min, double max, double lifetime) {
 		// adjust for minimum
 		max -= min;
 		lifetime -= min;
 
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
 		else if (lifetime == 0.0) {
 			returnColor = Color.BLUE;
 		}
 		return returnColor;
 	}
 
 	/**
 	 * Interpolates between two colors based on a blend factor.
 	 *
 	 * @param start color
 	 * @param end color
 	 * @param blend factor
 	 * @return interpolated color
 	 */
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
 
 	/**
 	 * Interpolates a single RGB component between two values based on
 	 * a blend factor.
 	 *
 	 * @param start component value
 	 * @param end component value
 	 * @param blend factor
 	 * @return interpolated component value
 	 */
 	private int interpolateColorComponent(int start, int end, double blend) {
 		return (int)(blend * (end - start) + start);
 	}
 
 	/*
 	 * Gets the appropriate curve fitter for the current fit.
 	 *
 	 * @param uiPanel has curve fitter selection
 	 */
 	private ICurveFitter getCurveFitter(IUserInterfacePanel uiPanel) {
 		ICurveFitter curveFitter = null;
 		switch (uiPanel.getAlgorithm()) {
 			case JAOLHO:
 				curveFitter = new JaolhoCurveFitter();
 				break;
 			case SLIMCURVE_RLD:
 				curveFitter = new SLIMCurveFitter();
 				curveFitter.setFitAlgorithm(FitAlgorithm.SLIMCURVE_RLD);
 				break;
 			case SLIMCURVE_LMA:
 				curveFitter = new SLIMCurveFitter();
 				curveFitter.setFitAlgorithm(FitAlgorithm.SLIMCURVE_LMA);
 				break;
 			case SLIMCURVE_RLD_LMA:
 				curveFitter = new SLIMCurveFitter();
 				curveFitter.setFitAlgorithm(FitAlgorithm.SLIMCURVE_RLD_LMA);
 				break;
 		}
 		ICurveFitter.FitFunction fitFunction = null;
 		switch (uiPanel.getFunction()) {
 			case SINGLE_EXPONENTIAL:
 				fitFunction = FitFunction.SINGLE_EXPONENTIAL;
 				break;
 			case DOUBLE_EXPONENTIAL:
 				fitFunction = FitFunction.DOUBLE_EXPONENTIAL;
 				break;
 			case TRIPLE_EXPONENTIAL:
 				fitFunction = FitFunction.TRIPLE_EXPONENTIAL;
 				break;
 			case STRETCHED_EXPONENTIAL:
 				fitFunction = FitFunction.STRETCHED_EXPONENTIAL;
 				break;
 		}
 		curveFitter.setEstimator(new FitterEstimator());
 		curveFitter.setFitFunction(fitFunction);
 		curveFitter.setNoiseModel(uiPanel.getNoiseModel());
 		curveFitter.setXInc(_timeRange);
 		curveFitter.setFree(translateFree(uiPanel.getFunction(), uiPanel.getFree()));
 		if (null != _excitationPanel) {
 			double[] excitation = null;
 			int startIndex = _fittingCursor.getPromptStartBin();
 			int stopIndex  = _fittingCursor.getPromptStopBin();
 			double base    = _fittingCursor.getPromptBaselineValue();
 			excitation = _excitationPanel.getValues(startIndex, stopIndex, base);
 			curveFitter.setInstrumentResponse(excitation);
 		}
 		return curveFitter;
 	}
 
 	/*
 	 * Handles reordering the array that describes which fit parameters are
 	 * free (vs. fixed).
 	 */
 	private boolean[] translateFree(FitFunction fitFunction, boolean free[]) {
 		boolean translated[] = new boolean[free.length];
 		switch (fitFunction) {
 			case SINGLE_EXPONENTIAL:
 				// incoming UI order is A, T, Z
 				// SLIMCurve wants Z, A, T
 				translated[0] = free[2];
 				translated[1] = free[0];
 				translated[2] = free[1];
 				break;
 			case DOUBLE_EXPONENTIAL:
 				// incoming UI order is A1 T1 A2 T2 Z
 				// SLIMCurve wants Z A1 T1 A2 T2
 				translated[0] = free[4];
 				translated[1] = free[0];
 				translated[2] = free[1];
 				translated[3] = free[2];
 				translated[4] = free[3];
 				break;
 			case TRIPLE_EXPONENTIAL:
 				// incoming UI order is A1 T1 A2 T2 A3 T3 Z
 				// SLIMCurve wants Z A1 T1 A2 T2 A3 T3
 				translated[0] = free[6];
 				translated[1] = free[0];
 				translated[2] = free[1];
 				translated[3] = free[2];
 				translated[4] = free[3];
 				translated[5] = free[4];
 				translated[6] = free[5];
 				break;
 			case STRETCHED_EXPONENTIAL:
 				// incoming UI order is A T H Z
 				// SLIMCurve wants Z A T H
 				translated[0] = free[3];
 				translated[1] = free[0];
 				translated[2] = free[1];
 				translated[3] = free[2];
 				break;
 		}
 		return translated;
 	}
 
 	/*
 	 * Helper function for the fit.  Shows the decay curve.
 	 *
 	 * @param title
 	 * @param uiPanel gets updates on dragged/start stop
 	 * @param data fitted data
 	 */
 	private void showDecayGraph(final String title,
 		final IUserInterfacePanel uiPanel,
 		final FittingCursor fittingCursor,
 		final ICurveFitData data,
 		int photons)
 	{
 		IDecayGraph decayGraph = DecayGraph.getInstance();
 		JFrame frame = decayGraph.init(uiPanel.getFrame(), _bins, _timeRange, _grayScaleImage);
 		decayGraph.setTitle(title);
 		decayGraph.setFittingCursor(fittingCursor);
 		double transStart = fittingCursor.getTransientStartValue();
 		double dataStart  = fittingCursor.getDataStartValue();
 		double transStop  = fittingCursor.getTransientStopValue();
 		decayGraph.setStartStop(transStart, dataStart, transStop);
 		double[] prompt = null;
 		int startIndex = 0;
 		if (null != _excitationPanel) {
 			startIndex = _fittingCursor.getPromptStartBin();
 			int stopIndex  = _fittingCursor.getPromptStopBin();
 			double base  = _fittingCursor.getPromptBaselineValue();
 			prompt = _excitationPanel.getValues(startIndex, stopIndex, base);
 		}
 		decayGraph.setData(startIndex, prompt, data);
 		decayGraph.setChiSquare(data.getParams()[0]);
 		decayGraph.setPhotons(photons);
 	}
 
 	/**
 	 * Inner class that listens for changes in the cursor that should trigger
 	 * a refit.
 	 */
 	private class FittingCursorListener implements IFittingCursorListener {
 		private Integer _transStart    = null;
 		private Integer _dataStart     = null;
 		private Integer _transStop     = null;
 		private Integer _promptStart   = null;
 		private Integer _promptStop    = null;
 		private Double _promptBaseline = null;
 
 		public void cursorChanged(FittingCursor cursor) {
 			// get current cursor values
 			int transStart        = cursor.getTransientStartBin();
 			int dataStart         = cursor.getDataStartBin();
 			int transStop         = cursor.getTransientStopBin();
 			int promptStart       = cursor.getPromptStartBin();
 			int promptStop        = cursor.getPromptStopBin();
 			double promptBaseline = cursor.getPromptBaselineValue();
 
 			// look for changes, current vs. saved cursor values
 			if (null == _transStart
 					|| null == _dataStart
 					|| null == _transStop
 					|| null == _promptStart
 					|| null == _promptStop
 					|| null == _promptBaseline
 					|| transStart     != _transStart
 					|| dataStart      != _dataStart
 					|| transStop      != _transStop
 					|| promptStart    != _promptStart
 					|| promptStop     != _promptStop
 					|| promptBaseline != _promptBaseline) {
 
 				// trigger refit
 
 				// update saved cursor values for next time
 				_transStart     = transStart;
 				_dataStart      = dataStart;
 				_transStop      = transStop;
 				_promptStart    = promptStart;
 				_promptStop     = promptStop;
 				_promptBaseline = promptBaseline;
 
 				if (null == _uiPanel) IJ.log("UI PANEL IS NULL");
 				if (null != _uiPanel) { // initial update comes during UI construction
 					if (_summed) {
 						fitSummed(_uiPanel, _fittingCursor);
 					}
 					else {
 						fitPixel(_uiPanel, _fittingCursor);
 					}
 				}
 			}
 		}
 	}
 
 }
