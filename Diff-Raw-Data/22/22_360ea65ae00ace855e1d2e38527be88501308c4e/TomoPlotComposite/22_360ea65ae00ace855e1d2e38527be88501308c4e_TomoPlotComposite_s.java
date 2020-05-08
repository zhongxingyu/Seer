 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.gda.client.tomo.composites;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
 import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
 import org.eclipse.dawnsci.analysis.api.roi.IROI;
 import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
 import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
 import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
 import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalFitROI;
 import org.eclipse.dawnsci.analysis.dataset.roi.PolygonalROI;
 import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
 import org.eclipse.dawnsci.plotting.api.PlotType;
 import org.eclipse.dawnsci.plotting.api.PlottingFactory;
 import org.eclipse.dawnsci.plotting.api.region.IROIListener;
 import org.eclipse.dawnsci.plotting.api.region.IRegion;
 import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
 import org.eclipse.dawnsci.plotting.api.region.MouseEvent;
 import org.eclipse.dawnsci.plotting.api.region.MouseListener;
 import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
 import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
 import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
 import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.PointStyle;
 import org.eclipse.dawnsci.plotting.api.trace.ITrace;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.resource.FontRegistry;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.part.PageBook;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.io.DataHolder;
 import uk.ac.diamond.scisoft.analysis.io.TIFFImageLoader;
 import uk.ac.gda.client.tomo.DoublePointList;
 import uk.ac.gda.client.tomo.DoublePointList.DoublePoint;
 import uk.ac.gda.client.tomo.TiltPlotPointsHolder;
 
 /**
  *
  */
 public class TomoPlotComposite extends Composite {
 	private static final Double DEFAULT_HISTOGRAM_FACTOR = 1.0;
 
 	private List<ITomoPlotListener> tomoPlotListeners = new ArrayList<TomoPlotComposite.ITomoPlotListener>();
 
 	private FontRegistry fontRegistry;
 	private static final String LOG_lbl = "Log";
 
 	private static final String Y_lbl = "y";
 	private static final String X_lbl = "x";
 
 	private Label lblYValue;
 	private Label lblXValue;
 	private Label lblProfileIntensityValue;
 	private Button btnLogData;
 
 	private static final String SET_EXPOSURE_TIME = "Apply Exposure Time";
 
 	private static final String INTENSITIES_lbl = "Intensities";
 
 	private static final String HISTOGRAM_DATASET_lbl = "Histogram Dataset";
 
 	private static final String HISTOGRAM_PLOT_TITLE_lbl = "Histogram Plot";
 
 	private static final String HISTOGRAM_TRACE = "Histogram";
 
 	private static final String DARK = "DARK";
 
 	private static final String INTENSITY = "INTENSITY";
 
 	private static final String INTENSITY_PLOT = "Intensity plot";
 
 	private static final String BOLD_TEXT_11 = "bold-text_11";
 
 	private static final String BOLD_TEXT_9 = "bold-text_9";
 
 	public enum MODE {
 		HISTOGRAM_SINGLE, HISTOGRAM_STREAM, PROFILE, TILT, NONE;
 	}
 
 	private IRegion xHair;
 
 	private MODE mode = MODE.NONE;
 
	private static final String REGION_FIT2 = "RegionFit2";

 	private static final Logger logger = LoggerFactory.getLogger(TomoPlotComposite.class);
 
 	private IPlottingSystem plottingSystem;
 
 	private Dataset rawDataSlice;
 
 	private long timeSinceLastUpdate = 0;
 
 	private Dataset rawImgDs;
 
 	private Dataset darkImgDs;
 
 	private uk.ac.diamond.scisoft.analysis.dataset.function.Histogram histogram;
 
 	private boolean shouldUpdatePlot = true;
 
 	private IROI xBounds;
 
 	private ILineTrace histogramTrace;
 
 	private ILineTrace profileLineTrace;
 
 	private boolean streamLog = false;
 
 	private boolean singleLog = false;
 
 	private Composite pg_plotinfo_histo;
 
 	private Composite pg_plotinfo_tilt;
 
 	private Composite pg_plotinfo_none;
 
 	public interface PlottingSystemActionListener {
 
 		/**
 		 * Informs the listener when the profile line is moved to a certain location
 		 * 
 		 * @param xVal
 		 *            - value to which the profile mouse has been moved to
 		 * @param intensity
 		 *            - the intensity at the current x
 		 */
 		void profileLineMovedTo(double xVal, long intensity);
 
 		/**
 		 * Informs the listener when the histogram roi is changed.
 		 * 
 		 * @param minValue
 		 * @param maxValue
 		 * @param factor
 		 */
 		void histogramChangedRoi(double minValue, double maxValue, double factor);
 
 	}
 
 	public interface ITomoPlotListener {
 		/**
 		 * Initiate the logic to apply the histogram to the exposure time
 		 */
 		void applyExposureButtonClicked(double histogramFactor);
 
 		/**
 		 * Informs listeners that the log button has been pressed.
 		 * 
 		 * @param isSwitchedOn
 		 * @throws Exception
 		 */
 		void log(boolean isSwitchedOn) throws Exception;
 	}
 
 	public void addTomoPlotListener(ITomoPlotListener tpl) {
 		tomoPlotListeners.add(tpl);
 	}
 
 	public void removeTomoPlotListener(ITomoPlotListener tpl) {
 		tomoPlotListeners.remove(tpl);
 	}
 
 	/**
 	 * @param parent
 	 * @param style
 	 * @throws Exception
 	 */
 	public TomoPlotComposite(Composite parent, int style) throws Exception {
 		super(parent, style);
 		if (Display.getCurrent() != null) {
 			fontRegistry = new FontRegistry(Display.getCurrent());
 			String fontName = Display.getCurrent().getSystemFont().getFontData()[0].getName();
 			fontRegistry.put(BOLD_TEXT_11, new FontData[] { new FontData(fontName, 11, SWT.BOLD) });
 			fontRegistry.put(BOLD_TEXT_9, new FontData[] { new FontData(fontName, 9, SWT.BOLD) });
 		}
 		this.setBackground(ColorConstants.white);
 
 		GridLayout layout = getGridLayoutZeroSettings();
 		this.setLayout(layout);
 		Composite plotComposite = new Composite(this, SWT.None);
 		plotComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 		plotComposite.setLayout(new FillLayout());
 
 		plottingSystem = PlottingFactory.createPlottingSystem();
 		// FIXME - DAWN API Changed and cannot map to old API.
 		// plottingSystem.createPlotPart(plotComposite, "", null, PlotType.PT1D_MULTI, null);
 		plottingSystem.createPlotPart(plotComposite, "", null, PlotType.XY_STACKED, null);
 		plottingSystem.setShowLegend(true);//false);
 		lineListeners = new ArrayList<TomoPlotComposite.PlottingSystemActionListener>();
 		//
 
 		pgBook_plotinfo = new PageBook(this, SWT.None);
 		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
 		layoutData.heightHint = 30;
 		pgBook_plotinfo.setLayoutData(layoutData);
 
 		pg_plotinfo_profile = new Composite(pgBook_plotinfo, SWT.None);
 		pg_plotinfo_profile.setBackground(ColorConstants.white);
 		GridLayout layout3 = new GridLayout(6, true);
 		layout3.marginHeight = 2;
 		layout3.marginWidth = 2;
 		layout3.horizontalSpacing = 2;
 		layout3.verticalSpacing = 2;
 		pg_plotinfo_profile.setLayout(layout3);
 
 		Label lblY = new Label(pg_plotinfo_profile, SWT.RIGHT);
 		lblY.setText(Y_lbl);
 		lblY.setBackground(ColorConstants.white);
 		lblY.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		lblYValue = new Label(pg_plotinfo_profile, SWT.LEFT);
 		lblYValue.setText("0");
 		lblYValue.setFont(fontRegistry.get(BOLD_TEXT_11));
 		lblYValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		lblYValue.setBackground(ColorConstants.white);
 
 		Label lblX = new Label(pg_plotinfo_profile, SWT.RIGHT);
 		lblX.setText(X_lbl);
 		lblX.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		lblX.setBackground(ColorConstants.white);
 
 		lblXValue = new Label(pg_plotinfo_profile, SWT.LEFT);
 		lblXValue.setText("0");
 		lblXValue.setFont(fontRegistry.get(BOLD_TEXT_11));
 		lblXValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		lblXValue.setBackground(ColorConstants.white);
 
 		Label lblIntensity = new Label(pg_plotinfo_profile, SWT.RIGHT);
 		lblIntensity.setText("Intensity:");
 		lblIntensity.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		lblIntensity.setBackground(ColorConstants.white);
 
 		lblProfileIntensityValue = new Label(pg_plotinfo_profile, SWT.LEFT);
 		lblProfileIntensityValue.setText("0");
 		lblProfileIntensityValue.setFont(fontRegistry.get(BOLD_TEXT_11));
 		GridData ld2 = new GridData(GridData.FILL_HORIZONTAL);
 		lblProfileIntensityValue.setLayoutData(ld2);
 		lblProfileIntensityValue.setBackground(ColorConstants.white);
 
 		pg_plotinfo_histo = new Composite(pgBook_plotinfo, SWT.None);
 		pg_plotinfo_histo.setBackground(ColorConstants.white);
 		layout3 = new GridLayout(4, true);
 		layout3.marginHeight = 0;
 		layout3.marginWidth = 0;
 		layout3.horizontalSpacing = 0;
 		layout3.verticalSpacing = 0;
 		pg_plotinfo_histo.setLayout(layout3);
 
 		Label lblEmpty = new Label(pg_plotinfo_histo, SWT.None);
 		GridData layoutData2 = new GridData(GridData.FILL_HORIZONTAL);
 		layoutData2.horizontalSpan = 2;
 		lblEmpty.setLayoutData(layoutData2);
 		lblEmpty.setBackground(ColorConstants.white);
 
 		btnApplyExposureSettings = new Button(pg_plotinfo_histo, SWT.PUSH);
 		btnApplyExposureSettings.setText(SET_EXPOSURE_TIME);
 		btnApplyExposureSettings.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				for (ITomoPlotListener tpl : tomoPlotListeners) {
 					tpl.applyExposureButtonClicked(getHistogramFactor());
 				}
 				resetHistogramFactor();
 			}
 		});
 		btnApplyExposureSettings.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		btnLogData = new Button(pg_plotinfo_histo, SWT.PUSH);
 		btnLogData.setText(LOG_lbl);
 
 		btnLogData.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (e.getSource().equals(btnLogData) && !ButtonSelectionUtil.isButtonSelected(btnLogData)) {
 					try {
 						ButtonSelectionUtil.setButtonSelected(btnLogData);
 						for (ITomoPlotListener tpl : tomoPlotListeners) {
 							tpl.log(true);
 						}
 					} catch (Exception e1) {
 						logger.error("Problem evaluating log", e1);
 						loadErrorInDisplay("Problem evaluating log of histogram data",
 								"Problem evaluating log of histogram data:" + e1.getMessage());
 					}
 				} else {
 					try {
 						ButtonSelectionUtil.setButtonDeselected(btnLogData);
 						for (ITomoPlotListener tpl : tomoPlotListeners) {
 							tpl.log(false);
 						}
 					} catch (Exception e1) {
 						logger.error("Problem evaluating log", e1);
 						loadErrorInDisplay("Problem evaluating log of histogram data",
 								"Problem evaluating log of histogram data:" + e1.getMessage());
 					}
 				}
 			}
 		});
 		btnLogData.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		pg_plotinfo_tilt = new Composite(pgBook_plotinfo, SWT.None);
 		pg_plotinfo_tilt.setBackground(ColorConstants.white);
 
 		pg_plotinfo_none = new Composite(pgBook_plotinfo, SWT.None);
 		pg_plotinfo_none.setBackground(ColorConstants.white);
 
 		pgBook_plotinfo.showPage(pg_plotinfo_none);
 	}
 
 	private GridLayout getGridLayoutZeroSettings() {
 		GridLayout layout = new GridLayout();
 
 		layout.marginWidth = 0;
 		layout.marginHeight = 0;
 		layout.verticalSpacing = 0;
 		layout.horizontalSpacing = 0;
 
 		return layout;
 	}
 
 	public void loadErrorInDisplay(final String dialogTitle, final String errorMsg) {
 		if (!this.getDisplay().isDisposed()) {
 			this.getDisplay().asyncExec(new Runnable() {
 
 				@Override
 				public void run() {
 					MessageDialog.openError(TomoPlotComposite.this.getShell(), dialogTitle,
 							"Problem with tomography alignment \n" + errorMsg);
 				}
 			});
 		}
 	}
 
 	private ArrayList<PlottingSystemActionListener> lineListeners;
 
 	public boolean addOverlayLineListener(PlottingSystemActionListener overlayLineListener) {
 		return lineListeners.add(overlayLineListener);
 	}
 
 	public boolean removeOverlayLineListener(PlottingSystemActionListener overlayLineListener) {
 		return lineListeners.remove(overlayLineListener);
 	}
 
 	public void updateProfilePlots(final IProgressMonitor monitor, final int y) {
 		mode = MODE.PROFILE;
 		if (getDisplay() != null && !getDisplay().isDisposed()) {
 			getDisplay().syncExec(new Runnable() {
 
 				@Override
 				public void run() {
 					pgBook_plotinfo.showPage(pg_plotinfo_profile);
 					if (xHair != null) {
 						removeOtherRegions(xHair.getName());
 					} else {
 						removeOtherRegions();
 					}
 				}
 			});
 		}
 
 		if (rawImgDs != null) {
 			long currentTimeMillis = System.currentTimeMillis();
 			if (currentTimeMillis - timeSinceLastUpdate < 100) {
 				// ignore the request
 				return;
 			}
 			timeSinceLastUpdate = currentTimeMillis;
 			final ArrayList<Dataset> plotDataSets = new ArrayList<Dataset>();
 
 			if (rawImgDs != null) {
 				int[] shape = rawImgDs.getShape();
 				rawDataSlice = rawImgDs.getSlice(new int[] { y - 1, 0 }, new int[] { y, shape[1] }, new int[] { 1, 1 });
 				rawDataSlice.squeeze();
 				rawDataSlice.setName("raw");
 				plotDataSets.add(rawDataSlice);
 			}
 			if (darkImgDs != null) {
 				int[] shape = rawImgDs.getShape();
 				Dataset darkDataSlice = darkImgDs.getSlice(new int[] { y - 1, 0 }, new int[] { y,  shape[1]  },
 						new int[] { 1, 1 });
 				darkDataSlice.squeeze();
 				darkDataSlice.setName("dark");
 				plotDataSets.add(darkDataSlice);
 			}
 
 			if (!getDisplay().isDisposed()) {
 				getDisplay().syncExec(new Runnable() {
 					@Override
 					public void run() {
 						final List<ITrace> profileLineTraces = plottingSystem.updatePlot1D(null, plotDataSets, monitor);
 
 						if (!profileLineTraces.isEmpty()) {
 							profileLineTrace = (ILineTrace) profileLineTraces.get(0);
 							profileLineTrace.setTraceColor(ColorConstants.blue);
 
 							if (profileLineTraces.size() > 1) {
 								ILineTrace darkLineTrace = (ILineTrace) profileLineTraces.get(1);
 								darkLineTrace.setTraceColor(ColorConstants.gray);
 
 							}
 						}
 
 						plottingSystem.setTitle(INTENSITY_PLOT);
 						plottingSystem.getSelectedYAxis().setFormatPattern("######.#");
 						// plottingSystem.getSelectedYAxis().setRange(0, 5);
 						// plottingSystem.getSelectedXAxis().setRange(0, 4008);
 						createMouseFollowLineRegion();
 
 					}
 				});
 			}
 		}
 
 	}
 
 	public void setImagesToPlot(String rawPlotImageFilename, String darkImgFileName) {
 		if (rawPlotImageFilename != null) {
 			rawImgDs = loadDatasetForTiffImg(rawPlotImageFilename, INTENSITY);
 		} else {
 			rawImgDs = null;
 			clearPlots();
 		}
 		if (darkImgFileName != null) {
 			darkImgDs = loadDatasetForTiffImg(darkImgFileName, DARK);
 		} else {
 			darkImgDs = null;
 		}
 	}
 
 	private Dataset loadDatasetForTiffImg(String fileName, String dsName) {
 		TIFFImageLoader tiffImageLoader = new TIFFImageLoader(fileName);
 		Dataset dataset = null;
 		try {
 			DataHolder dataHolder = tiffImageLoader.loadFile();
 			dataset = dataHolder.getDataset(0);
 			dataset.setName(dsName);
 
 		} catch (ScanFileHolderException e) {
 			logger.error("Tiff loading problem", e);
 		} catch (Exception e) {
 			logger.error("profile loading exception", e);
 		}
 		return dataset;
 	}
 
 	public void updatePlotPoints(final IProgressMonitor progress, final TiltPlotPointsHolder tiltPoints) {
 		mode = MODE.TILT;
 		getDisplay().syncExec(new Runnable() {
 
 			@Override
 			public void run() {
 				pgBook_plotinfo.showPage(pg_plotinfo_tilt);
 
 				plottingSystem.clearRegions();
 
 				final DoublePointList centers1 = tiltPoints.getCenters1();
 				final DoublePointList centers2 = tiltPoints.getCenters2();
 
 				final DoublePointList line2 = tiltPoints.getLine2();
 
 				if (line2 != null) {
 					DoubleDataset y3 = new DoubleDataset(line2.getYDoubleArray());
 					y3.setName("Line1");
 					DoubleDataset x3 = new DoubleDataset(line2.getXDoubleArray());
 					ArrayList<Dataset> singletonList = new ArrayList<Dataset>(1);
 					singletonList.add(y3);
 					plottingSystem.updatePlot1D(x3, singletonList, progress);
 				}
 				plottingSystem.getSelectedYAxis().setFormatPattern("######.#");
 
 				try {
 					if (centers1 != null) {
 						ITrace centers1Trace = plottingSystem.getTrace("Before Alignment");
 						if (centers1Trace != null) {
 							plottingSystem.removeTrace(centers1Trace);
 						}
 						DoubleDataset centers1Yds = new DoubleDataset(centers1.getYDoubleArray());
 						centers1Yds.setName("Before Alignment");
 						DoubleDataset centers1Xds = new DoubleDataset(centers1.getXDoubleArray());
 						ArrayList<Dataset> dsList = new ArrayList<Dataset>();
 						dsList.add(centers1Yds);
 						centers1Yds.setName("Before Alignment");
 						List<ITrace> traces1 = plottingSystem.updatePlot1D(centers1Xds, dsList, progress);
 						ILineTrace beforeTilt = (ILineTrace) traces1.get(0);
 						beforeTilt.setTraceColor(ColorConstants.lightBlue);
 						beforeTilt.setUserTrace(true);
 						((ILineTrace) traces1.get(0)).setPointStyle(PointStyle.CROSS);
 					}
 					if (centers2 != null) {
 						ITrace centers2Trace = plottingSystem.getTrace("After Alignment");
 						if (centers2Trace != null) {
 							plottingSystem.removeTrace(centers2Trace);
 						}
 						centers2Trace = plottingSystem.createLineTrace("After Alignment");
 						centers2Trace.setUserTrace(true);
 						centers2Trace.setVisible(true);
 						DoubleDataset centers2Yds = new DoubleDataset(centers2.getYDoubleArray());
 						centers2Yds.setName("After Alignment");
 						DoubleDataset centers2Xds = new DoubleDataset(centers2.getXDoubleArray());
 						ArrayList<Dataset> dsList = new ArrayList<Dataset>();
 						dsList.add(centers2Yds);
 						List<ITrace> traces2 = plottingSystem.updatePlot1D(centers2Xds, dsList, progress);
 						ILineTrace afterTilt = (ILineTrace) traces2.get(0);
 						afterTilt.setTraceColor(ColorConstants.red);
 						afterTilt.setUserTrace(true);
 						((ILineTrace) traces2.get(0)).setPointStyle(PointStyle.CROSS);
 					}
 
 					plottingSystem.clearRegions();
 					if (tiltPoints.getEllipse1() != null) {
						IRegion region1 = plottingSystem.createRegion(
								RegionUtils.getUniqueName("RegionFit1", plottingSystem), RegionType.POLYGON);
 						region1.setLineWidth(1);
 						region1.setRegionColor(ColorConstants.black);
 
 						PolygonalROI roi1 = new PolygonalROI();
 						for (DoublePoint dp : tiltPoints.getEllipse1().getDoublePointList()) {
 							roi1.insertPoint(dp.getX(), dp.getY());
 						}
 
 						plottingSystem.addRegion(region1);
 //						region1.setROI(new EllipticalFitROI(roi1));
 						region1.setROI(roi1);
 						region1.setMobile(false);
						region1.setName("Pre Tilt");
 					}
 					if (tiltPoints.getEllipse2() != null) {
 						//
						IRegion region2 = plottingSystem.createRegion(
								RegionUtils.getUniqueName(REGION_FIT2, plottingSystem), RegionType.ELLIPSEFIT);
 						region2.setLineWidth(1);
 						region2.setRegionColor(ColorConstants.orange);
 						PolygonalROI roi2 = new PolygonalROI();
 						for (DoublePoint dp : tiltPoints.getEllipse2().getDoublePointList()) {
 							roi2.insertPoint(dp.getX(), dp.getY());
 						}
 
 						plottingSystem.addRegion(region2);
 						region2.setROI(new EllipticalFitROI(roi2));
 						region2.setMobile(false);
						region2.setName("Post Tilt");
 					}
 					if (tiltPoints.getTiltPointsTitle() != null) {
 						plottingSystem.setTitle(tiltPoints.getTiltPointsTitle());
 					}
 				} catch (Exception e) {
 					logger.error("Problem plotting tilt points.", e);
 				}
 
 			}
 		});
 	}
 
 	private synchronized void setShouldUpdatePlot(boolean updatePlot) {
 		this.shouldUpdatePlot = updatePlot;
 	}
 
 	/**
 	 * Updates the histogram data for stream
 	 * 
 	 * @param histogramFromStats
 	 */
 	public void updateHistogramData(final double[] histogramFromStats) {
 		if (shouldUpdatePlot && !this.isDisposed()) {
 			this.getDisplay().syncExec(new Runnable() {
 
 				@Override
 				public void run() {
 					if (mode != MODE.HISTOGRAM_STREAM) {
 						histogramTrace = null;
 						pgBook_plotinfo.showPage(pg_plotinfo_histo);
 						if (!btnApplyExposureSettings.isDisposed()) {
 							btnApplyExposureSettings.setVisible(true);
 						}
 						removeXHairListeners();
 						removeOtherRegions();
 						xHair = null;
 						streamLog = false;
 					}
 					// show the apply button if streaming is happening in the left window.
 					mode = MODE.HISTOGRAM_STREAM;
 					createMouseFollowLineRegion();
 					if (!TomoPlotComposite.this.isDisposed() && plottingSystem.getTrace(HISTOGRAM_TRACE) == null) {
 						plottingSystem.clear();
 					}
 
 					double[] subarray = ArrayUtils.subarray(histogramFromStats, 0, 1024);
 
 					if (streamLog) {
 						int count = 0;
 						for (double d : histogramFromStats) {
 							if (d != 0) {
 								subarray[count] = Math.log10(d);
 							}
 							count++;
 						}
 					}
 
 					DoubleDataset ds = new DoubleDataset(subarray, 1024);
 
 					int min = 0;
 					ds.setName(HISTOGRAM_DATASET_lbl);
 
 					IntegerDataset xaxisRange = IntegerDataset.createRange(min, 65536, 64);
 					xaxisRange.setName(INTENSITIES_lbl);
 
 					if (histogramTrace == null) {
 						histogramTrace = plottingSystem.createLineTrace(HISTOGRAM_TRACE);
 					}
 
 					histogramTrace.setData(xaxisRange, ds);
 					histogramTrace.setTraceColor(ColorConstants.blue);
 					if (plottingSystem.getTrace(HISTOGRAM_TRACE) == null) {
 						plottingSystem.addTrace(histogramTrace);
 					}
 					plottingSystem.repaint();
 
 					plottingSystem.setTitle(HISTOGRAM_PLOT_TITLE_lbl);
 					plottingSystem.getSelectedYAxis().setFormatPattern("#####");
 					plottingSystem.getSelectedXAxis().setFormatPattern("#####");
 				}
 			});
 		}
 	}
 
 	public void applyLog(boolean log, ImageData imgData) {
 		singleLog = log;
 		updateHistogramData(imgData);
 	}
 
 	public void applyLog(boolean log) {
 		streamLog = log;
 	}
 
 	/**
 	 * Update histogram data for single.
 	 * 
 	 * @param imageData
 	 */
 	public void updateHistogramData(final ImageData imageData) {
 		if (shouldUpdatePlot) {
 			getDisplay().syncExec(new Runnable() {
 
 				@Override
 				public void run() {
 					if (mode != MODE.HISTOGRAM_SINGLE) {
 						histogramTrace = null;
 						removeXHairListeners();
 						removeOtherRegions();
 						xHair = null;
 						singleLog = false;
 						pgBook_plotinfo.showPage(pg_plotinfo_histo);
 						btnApplyExposureSettings.setVisible(false);
 					}
 					// show the apply button if streaming is happening in the left window.
 					mode = MODE.HISTOGRAM_SINGLE;
 					createMouseFollowLineRegion();
 					if (plottingSystem.getTrace(HISTOGRAM_TRACE) == null) {
 						plottingSystem.clear();
 					}
 
 					// removeOtherRegions(xHair.getName());
 					if (histogram == null) {
 						histogram = new uk.ac.diamond.scisoft.analysis.dataset.function.Histogram(256);
 					}
 
 					ImageData imgD = (ImageData) imageData.clone();
 					int intensities[] = new int[imgD.width * imgD.height];
 					int i = 0;
 					if (imageData.depth > 16) {
 						for (int h = 0; h < imgD.height; h++) {
 							for (int w = 0; w < imgD.width; w++) {
 								int pixel = imgD.getPixel(w, h);
 
 								// to equate it to a 10 bit number of 1024 shift it right by 14
 								intensities[i++] = pixel >> 8;
 							}
 						}
 					} else {
 						for (int h = 0; h < imgD.height; h++) {
 							for (int w = 0; w < imgD.width; w++) {
 								int pixel = imgD.getPixel(w, h);
 
 								// to equate it to a 10 bit number of 1024 shift it right by 14
 								intensities[i++] = pixel;
 							}
 						}
 
 					}
 
 					IDataset ds = new IntegerDataset(intensities, imgD.width, imgD.height);
 
 					int max = ds.max().intValue();
 					int min = ds.min().intValue();
 					histogram.setMinMax(min, max);
 					ds.setName(HISTOGRAM_DATASET_lbl);
 
 					Dataset histogramDs = histogram.value(ds).get(0);
 					histogramDs.setName(HISTOGRAM_DATASET_lbl);
 					if (singleLog) {
 						int shape = histogramDs.getShape()[0];
 						double[] logData = new double[shape];
 						int count = 0;
 						if (histogramDs instanceof IntegerDataset) {
 							IntegerDataset iDs = (IntegerDataset) histogramDs;
 							for (int intensity : iDs.getData()) {
 								if (intensity != 0) {
 									logData[count] = Math.log10(intensity);
 								}
 								count++;
 							}
 							histogramDs = new DoubleDataset(logData, shape);
 						}
 
 					}
 
 					IntegerDataset xaxisRange = IntegerDataset.createRange(min, max, 255);
 					xaxisRange.setName(INTENSITIES_lbl);
 
 					if (histogramTrace == null) {
 						histogramTrace = plottingSystem.createLineTrace(HISTOGRAM_TRACE);
 					}
 
 					histogramTrace.setData(xaxisRange, histogramDs);
 					histogramTrace.setTraceColor(ColorConstants.darkBlue);
 					if (plottingSystem.getTrace(HISTOGRAM_TRACE) == null) {
 						plottingSystem.addTrace(histogramTrace);
 					}
 					plottingSystem.repaint();
 
 					// plottingSystem.updatePlot1D(xaxisRange, dsHisto, null);
 					plottingSystem.setTitle(HISTOGRAM_PLOT_TITLE_lbl);
 
 					plottingSystem.getSelectedYAxis().setFormatPattern("#####");
 					plottingSystem.getSelectedXAxis().setFormatPattern("#####");
 				}
 			});
 		}
 	}
 
 	/**
 	 * Removes other regions than the one provided.
 	 * 
 	 * @param regionName
 	 */
 	private void removeOtherRegions(String... regionName) {
 		Collection<IRegion> regions = plottingSystem.getRegions();
 		for (IRegion iRegion : regions) {
 			if (regionName.length > 0) {
 				for (String r : regionName) {
 					if (!r.equals(iRegion.getName())) {
 						plottingSystem.removeRegion(iRegion);
 					}
 				}
 			} else {
 				plottingSystem.removeRegion(iRegion);
 			}
 		}
 	}
 
 	@Override
 	public void dispose() {
 		if (!plottingSystem.isDisposed()) {
 			plottingSystem.clear();
 		}
 		super.dispose();
 	}
 
 	private void createMouseFollowLineRegion() {
 
 		if (plottingSystem == null)
 			return;
 		try {
 			if (xHair == null || plottingSystem.getRegion(xHair.getName()) == null) {
 				this.xHair = plottingSystem.createRegion(RegionUtils.getUniqueName("DragLine", plottingSystem),
 						IRegion.RegionType.XAXIS_LINE);
 
 				xHair.addROIListener(mouseFollowRoiListener);
 
 				addMouseFollowLineRegion(xHair);
 				if (mode == MODE.HISTOGRAM_SINGLE || mode == MODE.HISTOGRAM_STREAM) {
 					xHair.addMouseListener(mouseFollowRegionMouseListner);
 				}
 			}
 
 		} catch (Exception ne) {
 			logger.error("Cannot create cross-hairs!", ne);
 		}
 	}
 
 	private IROIListener mouseFollowRoiListener = new IROIListener() {
 
 		private void update(ROIEvent evt) {
 			final IRegion region = (IRegion) evt.getSource();
 			IROI roi = region.getROI();
 			xBounds = roi;
 
 		}
 
 		@Override
 		public void roiDragged(ROIEvent evt) {
 			update(evt);
 			if (mode == MODE.PROFILE) {
 				for (PlottingSystemActionListener lis : lineListeners) {
 					int[] intPoint = evt.getROI().getIntPoint();
 					lis.profileLineMovedTo(intPoint[0], profileLineTrace.getYData().getInt(intPoint[0]));
 				}
 			}
 		}
 
 		@Override
 		public void roiChanged(ROIEvent evt) {
 			update(evt);
 		}
 
 		@Override
 		public void roiSelected(ROIEvent evt) {
 
 		}
 	};
 
 	private MouseListener mouseFollowRegionMouseListner = new MouseListener.Stub() {
 
 		@Override
 		public void mousePressed(MouseEvent me) {
 
 			if (mode == MODE.HISTOGRAM_SINGLE || mode == MODE.HISTOGRAM_STREAM) {
 				try {
 
 					final Color snapShotColor = ColorConstants.blue;
 					createStaticRegion("DragLine", xBounds, snapShotColor, xHair.getRegionType());
 
 					setShouldUpdatePlot(false);
 
 					removeXHairListeners();
 					plottingSystem.removeRegion(xHair);
 					xHair = null;
 
 				} catch (Exception ne) {
 					logger.error(ne.getMessage(), ne);
 				}
 			}
 		}
 	};
 
 	private void removeXHairListeners() {
 		if (xHair != null) {
 			xHair.removeMouseListener(mouseFollowRegionMouseListner);
 		}
 	}
 
 	private Double histogramFactor = DEFAULT_HISTOGRAM_FACTOR;
 
 	private Composite pg_plotinfo_profile;
 
 	private PageBook pgBook_plotinfo;
 
 	private Button btnApplyExposureSettings;
 
 	private IRegion createStaticRegion(String nameStub, final IROI bounds, final Color snapShotColor,
 			final RegionType regionType) throws Exception {
 
 		final IRegion region = plottingSystem.createRegion(RegionUtils.getUniqueName(nameStub, plottingSystem),
 				regionType);
 		region.setRegionColor(snapShotColor);
 		plottingSystem.addRegion(region);
 		region.setROI(bounds);
 		region.addROIListener(new IROIListener() {
 
 			Double histogramFrom = null;
 
 			@Override
 			public void roiDragged(ROIEvent evt) {
 				if (histogramFrom == null) {
 					histogramFrom = evt.getROI().getPointX();
 					logger.debug("distance started:{}", histogramFrom);
 				}
 			}
 
 			@Override
 			public void roiChanged(ROIEvent evt) {
 				// need to introspect the movement and propagate the change to hardware.
 				double minValue = histogramTrace.getXData().min().doubleValue();
 				double maxValue = histogramTrace.getXData().max().doubleValue();
 				double histogramTo = evt.getROI().getPointX();
 				logger.debug("distance ended:{}", histogramTo);
 
 				plottingSystem.removeRegion(region);
 				region.removeROIListener(this);
 				setShouldUpdatePlot(true);
 
 				histogramFactor = histogramFactor * histogramFrom / histogramTo;
 				for (PlottingSystemActionListener lis : lineListeners) {
 					lis.histogramChangedRoi(minValue, maxValue, histogramFactor);
 				}
 
 			}
 
 			@Override
 			public void roiSelected(ROIEvent evt) {
 
 			}
 		});
 
 		return region;
 	}
 
 	private void addMouseFollowLineRegion(IRegion region) {
 		region.setVisible(true);
 		region.setTrackMouse(true);
 		region.setRegionColor(ColorConstants.red);
 		region.setUserRegion(false); // They cannot see preferences or change it!
 		plottingSystem.addRegion(region);
 	}
 
 	public void clearPlots() {
 		histogramFactor = DEFAULT_HISTOGRAM_FACTOR;
 		getDisplay().syncExec(new Runnable() {
 
 			@Override
 			public void run() {
 				mode = MODE.NONE;
 				plottingSystem.setTitle("");
 				pgBook_plotinfo.showPage(pg_plotinfo_none);
 				plottingSystem.reset();
 				histogramTrace = null;
 			}
 		});
 
 	}
 
 	public void setYLabelValue(final String yLblValue) {
 		if (!getDisplay().isDisposed()) {
 			getDisplay().asyncExec(new Runnable() {
 
 				@Override
 				public void run() {
 					lblYValue.setText(yLblValue);
 				}
 			});
 		}
 	}
 
 	public void setXLabelValue(final String formattedXVal) {
 		if (!getDisplay().isDisposed()) {
 			getDisplay().asyncExec(new Runnable() {
 
 				@Override
 				public void run() {
 					lblXValue.setText(formattedXVal);
 				}
 			});
 		}
 	}
 
 	public void setProfileIntensityValue(final String profileIntensityValue) {
 		if (!getDisplay().isDisposed()) {
 			getDisplay().asyncExec(new Runnable() {
 
 				@Override
 				public void run() {
 					lblProfileIntensityValue.setText(profileIntensityValue);
 				}
 			});
 		}
 	}
 
 	public Double getHistogramFactor() {
 		if (mode == MODE.HISTOGRAM_STREAM) {
 			return histogramFactor;
 		}
 		return DEFAULT_HISTOGRAM_FACTOR;
 	}
 
 	public void resetHistogramFactor() {
 		histogramFactor = DEFAULT_HISTOGRAM_FACTOR;
 	}
 }
