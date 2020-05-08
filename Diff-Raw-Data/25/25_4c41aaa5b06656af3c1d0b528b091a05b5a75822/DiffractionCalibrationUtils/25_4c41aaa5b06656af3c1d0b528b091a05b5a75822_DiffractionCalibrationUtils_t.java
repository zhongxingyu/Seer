 /*-
  * Copyright 2013 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.dawb.workbench.ui.views;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.vecmath.Vector3d;
 
 import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.region.IRegion;
 import org.dawnsci.plotting.api.region.IRegion.RegionType;
 import org.dawnsci.plotting.api.region.RegionUtils;
 import org.dawnsci.plotting.api.trace.IImageTrace;
 import org.dawnsci.plotting.api.trace.ITrace;
 import org.dawnsci.plotting.tools.diffraction.DiffractionImageAugmenter;
 import org.dawnsci.plotting.tools.diffraction.DiffractionTool;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.swt.widgets.Display;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationFactory;
 import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationStandards;
 import uk.ac.diamond.scisoft.analysis.crystallography.HKL;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
 import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
 import uk.ac.diamond.scisoft.analysis.diffraction.PowderRingsUtils;
 import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;
 import uk.ac.diamond.scisoft.analysis.fitting.Fitter;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.Polynomial;
 import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
 import uk.ac.diamond.scisoft.analysis.roi.EllipticalROI;
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 
 /**
  * Class containing static methods used in Diffraction calibration views
  *
  */
 public class DiffractionCalibrationUtils {
 
 	private static Logger logger = LoggerFactory.getLogger(DiffractionCalibrationUtils.class);
 
 	enum ManipulateMode {
 		LEFT, RIGHT, UP, DOWN, ENLARGE, SHRINK, ELONGATE, SQUASH, CLOCKWISE, ANTICLOCKWISE
 	}
 
 	private static String REGION_PREFIX = "Pixel peaks";
 
 	/**
 	 * Create a job to calibrate images 
 	 * @param display
 	 * @param plottingSystem
 	 * @param model
 	 * @param currentData
 	 * @param useFixedWavelength if true then fit a global environment else local
 	 * @param postFixedWavelengthFit if true and useFixedWavelength true then fit wavelength afterwards
 	 * @return job that needs to be scheduled
 	 */
 	public static Job calibrateImages(final Display display,
 									   final IPlottingSystem plottingSystem,
 									   final List<DiffractionTableData> model,
 									   final DiffractionTableData currentData,
 									   final boolean useFixedWavelength,
 									   final boolean postFixedWavelengthFit) {
 		Job job = new Job("Calibrate detector") {
 			@Override
 			protected IStatus run(final IProgressMonitor monitor) {
 				IStatus stat = Status.OK_STATUS;
 				final ProgressMonitorWrapper mon = new ProgressMonitorWrapper(monitor);
 				monitor.beginTask("Calibrate detector", IProgressMonitor.UNKNOWN);
 				List<HKL> spacings = CalibrationFactory.getCalibrationStandards().getCalibrant().getHKLs();
 				List<List<? extends IROI>> lROIs = new ArrayList<List<? extends IROI>>();
 				List<DetectorProperties> dps = new ArrayList<DetectorProperties>();
 				DiffractionCrystalEnvironment env = null;
 				for (DiffractionTableData data : model) {
 					IDiffractionMetadata md = data.md;
 					if (!data.use || data.nrois <= 0 || md == null) {
 						continue;
 					}
 					if (env == null) {
 						env = md.getDiffractionCrystalEnvironment();
 					}
 					data.q = null;
 
 					DetectorProperties dp = md.getDetector2DProperties();
 					if (dp == null) {
 						continue;
 					}
 					dps.add(dp);
 					lROIs.add(data.rois);
 				}
 				List<QSpace> qs = null;
 				if (useFixedWavelength) {
 					monitor.subTask("Fitting all rings");
 					try {
 						qs = PowderRingsUtils.fitAllEllipsesToAllQSpacesAtFixedWavelength(mon, dps, env, lROIs, spacings, postFixedWavelengthFit);
 					} catch (IllegalArgumentException e) {
 						logger.debug("Problem in calibrating all image: {}", e);
 					}
 				} else {
 					try {
 						qs = PowderRingsUtils.fitAllEllipsesToAllQSpaces(mon, dps, env, lROIs, spacings);
 					} catch (IllegalArgumentException e) {
 						logger.debug("Problem in calibrating all image: {}", e);
 					}
 				}
 
 				int i = 0;
 				for (DiffractionTableData data : model) {
 					IDiffractionMetadata md = data.md;
 					if (!data.use || data.nrois <= 0 || md == null) {
 						continue;
 					}
 
 					DetectorProperties dp = md.getDetector2DProperties();
 					if (dp == null) {
 						continue;
 					}
 					data.q = qs.get(i++);
 					logger.debug("Q-space = {}", data.q);
 				}
 
 				display.syncExec(new Runnable() {
 					@Override
 					public void run() {
 						for (DiffractionTableData data : model) {
 							IDiffractionMetadata md = data.md;
 							if (data.q == null || !data.use || data.nrois <= 0 || md == null) {
 								continue;
 							}
 							DetectorProperties dp = md.getDetector2DProperties();
 							DiffractionCrystalEnvironment ce = md.getDiffractionCrystalEnvironment();
 							if (dp == null || ce == null) {
 								continue;
 							}
 
 							DetectorProperties fp = data.q.getDetectorProperties();
 							dp.setGeometry(fp);
 							ce.setWavelength(data.q.getWavelength());
 						}
 
 						if (currentData == null || currentData.md == null || currentData.q == null)
 							return;
 
 						hideFoundRings(plottingSystem);
 						drawCalibrantRings(currentData.augmenter);
 					}
 				});
 				return stat;
 			}
 		};
 		job.setPriority(Job.SHORT);
 		return job;
 	}
 
 	/**
 	 * 
 	 * @param plottingSystem
 	 */
 	public static void hideFoundRings(IPlottingSystem plottingSystem) {
 		for (IRegion r : plottingSystem.getRegions()) {
 			String n = r.getName();
 			if (n.startsWith(REGION_PREFIX)) {
 				r.setVisible(false);
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @param plottingSystem
 	 */
 	private static void clearFoundRings(IPlottingSystem plottingSystem) {
 		for (IRegion r : plottingSystem.getRegions()) {
 			String n = r.getName();
 			if (n.startsWith(REGION_PREFIX)) {
 				plottingSystem.removeRegion(r);
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @param currentData
 	 */
 	public static void drawCalibrantRings(DiffractionImageAugmenter aug) {
 
 		if (aug == null)
 			return;
 
 		CalibrationStandards standards = CalibrationFactory.getCalibrationStandards();
 		aug.drawCalibrantRings(true, standards.getCalibrant());
 		aug.drawBeamCentre(true);
 	}
 
 	public static IStatus drawFoundRing(final IProgressMonitor monitor, Display display, final IPlottingSystem plotter, final IROI froi, final boolean circle) {
 		final boolean[] status = {true};
 		display.syncExec(new Runnable() {
 
 			public void run() {
 				try {
 					IRegion region = plotter.createRegion(RegionUtils.getUniqueName(REGION_PREFIX, plotter), circle ? RegionType.CIRCLEFIT : RegionType.ELLIPSEFIT);
 					region.setROI(froi);
 					region.setRegionColor(circle ? ColorConstants.cyan : ColorConstants.orange);
 					monitor.subTask("Add region");
 					region.setUserRegion(false);
 					plotter.addRegion(region);
 					monitor.worked(1);
 				} catch (Exception e) {
 					status[0] = false;
 				}
 			}
 		});
 		return status[0] ? Status.OK_STATUS : Status.CANCEL_STATUS;
 	}
 
 	/**
 	 * 
 	 * @param currentData
 	 * @param mode
 	 * @param fast
 	 */
 	public static void changeRings(DiffractionTableData currentData, ManipulateMode mode, boolean fast) {
 		if (currentData == null || currentData.md == null)
 			return;
 
 		DetectorProperties detprop = currentData.md.getDetector2DProperties();
 		if (detprop == null)
 			return;
 
 		if (mode == ManipulateMode.UP) {
 			Vector3d orig = detprop.getOrigin();
 			Vector3d col = detprop.getPixelColumn();
 			if (fast)
 				col.scale(10);
 			orig.add(col);
 		} else if (mode == ManipulateMode.DOWN) {
 			Vector3d orig = detprop.getOrigin();
 			Vector3d col = detprop.getPixelColumn();
 			if (fast)
 				col.scale(10);
 			orig.sub(col);
 		} else if (mode == ManipulateMode.LEFT) {
 			Vector3d orig = detprop.getOrigin();
 			Vector3d row = detprop.getPixelRow();
 			if (fast)
 				row.scale(10);
 			orig.add(row);
 		} else if (mode == ManipulateMode.RIGHT) {
 			Vector3d orig = detprop.getOrigin();
 			Vector3d row = detprop.getPixelRow();
 			if (fast)
 				row.scale(10);
 			orig.sub(row);
 		} else if (mode == ManipulateMode.ENLARGE) {
 			Vector3d norm = new Vector3d(detprop.getNormal());
 			norm.scale((fast ? 15 : 1)*detprop.getHPxSize());
 			double[] bc = detprop.getBeamCentreCoords();
 			Vector3d orig = detprop.getOrigin();
 			orig.sub(norm);
 			if (!Double.isNaN(bc[0])) { // fix on beam centre
 				detprop.setBeamCentreCoords(bc);
 			}
 		} else if (mode == ManipulateMode.SHRINK) {
 			Vector3d norm = new Vector3d(detprop.getNormal());
 			norm.scale((fast ? 15 : 1)*detprop.getHPxSize());
 			double[] bc = detprop.getBeamCentreCoords();
 			Vector3d orig = detprop.getOrigin();
 			orig.add(norm);
 			if (!Double.isNaN(bc[0])) { // fix on beam centre
 				detprop.setBeamCentreCoords(bc);
 			}
 		} else if (mode == ManipulateMode.ELONGATE) {
 			double tilt = Math.toDegrees(detprop.getTiltAngle());
 			double[] angle = detprop.getNormalAnglesInDegrees();
 			tilt += fast ? 2 : 0.2;
 			if (tilt > 90)
 				tilt = 90;
 			detprop.setNormalAnglesInDegrees(tilt, 0, angle[2]);
 			logger.trace("p: {}", tilt);
 		} else if (mode == ManipulateMode.SQUASH) {
 			double tilt = Math.toDegrees(detprop.getTiltAngle());
 			double[] angle = detprop.getNormalAnglesInDegrees();
 			tilt -= fast ? 2 : 0.2;
 			if (tilt < 0)
 				tilt = 0;
 			detprop.setNormalAnglesInDegrees(tilt, 0, angle[2]);
 			logger.trace("o: {}", tilt);
 		} else if (mode == ManipulateMode.ANTICLOCKWISE) {
 			double[] angle = detprop.getNormalAnglesInDegrees();
 			angle[2] -= fast ? 2 : 0.5;
 			if (angle[2] < 0)
 				angle[2] += 360;
 			detprop.setNormalAnglesInDegrees(angle[0], angle[1], angle[2]);
 			logger.trace("a: {}", angle[2]);
 		} else if (mode == ManipulateMode.CLOCKWISE) {
 			double[] angle = detprop.getNormalAnglesInDegrees();
 			angle[2] += fast ? 2 : 0.5;
 			if (angle[2] > 360)
 				angle[2] = 360;
 			detprop.setNormalAnglesInDegrees(angle[0], angle[1], angle[2]);
 			logger.trace("c: {}", angle[2]);
 		}
 		drawCalibrantRings(currentData.augmenter);
 	}
 
 	/**
 	 * 
 	 * @param display
 	 * @param model
 	 * @param currentData
 	 */
 	public static void calibrateWavelength(final Display display, final List<DiffractionTableData> model, final DiffractionTableData currentData) {
 		Job job = new Job("Calibrate wavelength") {
 			@Override
 			protected IStatus run(final IProgressMonitor monitor) {
 				IStatus stat = Status.OK_STATUS;
 				monitor.beginTask("Calibrate wavelength", IProgressMonitor.UNKNOWN);
 				List<Double> odist = new ArrayList<Double>();
 				List<Double> ndist = new ArrayList<Double>();
 				for (DiffractionTableData data : model) {
 					if (data.q == null || !data.use || data.nrois <= 0 || data.md == null) {
 						continue;
 					}
 
 					if (Double.isNaN(data.od)) {
 						continue;
 					}
 					odist.add(data.od);
 					ndist.add(data.q.getDetectorProperties().getDetectorDistance());
 				}
 				if (odist.size() < 3) {
 					logger.warn("Need to use three or more images");
 					return Status.CANCEL_STATUS;
 				}
 				Polynomial p = new Polynomial(1);
 				try {
 					Fitter.polyFit(new AbstractDataset[] {AbstractDataset.createFromList(odist)}, AbstractDataset.createFromList(ndist), 1e-15, p);
 				} catch (Exception e) {
 					logger.error("Problem with fit", e);
 					return Status.CANCEL_STATUS;
 				}
 				logger.debug("Straight line fit: {}", p);
 
 				final double f = p.getParameterValue(0);
 				display.asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						for (final DiffractionTableData data : model) {
 							if (!data.use || data.nrois <= 0 || data.md == null) {
 								continue;
 							}
 
 							final DiffractionCrystalEnvironment ce = data.md.getDiffractionCrystalEnvironment();
 							if (ce != null) {
 								double ow = ce.getWavelength();
 								ce.setWavelength(ow * f);
 							}
 						}
 					}
 				});
 
 				display.asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						drawCalibrantRings(currentData.augmenter);
 					}
 				});
 				return stat;
 			}
 		};
 		job.setPriority(Job.SHORT);
 		job.schedule();
 	}
 
 	/**
 	 * 
 	 * @param display
 	 * @param plottingSystem
 	 * @param currentData
 	 * @return findRing job
 	 */
 	public static Job findRings(final Display display, final IPlottingSystem plottingSystem, final DiffractionTableData currentData) {
 		if (currentData == null)
 			return null;
 
 		DiffractionImageAugmenter aug = currentData.augmenter;
 		if (aug == null)
 			return null;
 
 		final List<IROI> resROIs = aug.getResolutionROIs();
 		final IImageTrace image = getImageTrace(plottingSystem);
 		if (currentData.rois == null) {
 			currentData.rois = new ArrayList<IROI>();
 		} else {
 			currentData.rois.clear();
 		}
 		currentData.use = false;
 		currentData.nrois = 0;
 		clearFoundRings(plottingSystem);
 		Job job = new Job("Ellipse rings finding") {
 			@Override
 			protected IStatus run(final IProgressMonitor monitor) {
 				IStatus stat = Status.OK_STATUS;
 				double last = -1;
 				int n = 0;
 				for (final IROI r : resROIs) {
 					IROI roi = null;
 					try {
 						if (!(r instanceof EllipticalROI)) { // cannot cope with other conic sections for now
 							continue;
 						}
 						EllipticalROI e = (EllipticalROI) r;
 						double major = e.getSemiAxis(0);
 						double delta = last < 0 ? 0.1*major : 0.2*(major - last);
 						if (delta > 50)
 							delta = 50;
 						last = major;
 						try {
 							roi = DiffractionTool.runEllipseFit(monitor, display, plottingSystem, image, e, false, delta);
 						} catch (NullPointerException ex) {
 							stat = Status.CANCEL_STATUS;
 							n = -1; // indicate problem with getting image or other issues
 							return stat;
 						}
 						if (roi == null) {
 							stat = Status.CANCEL_STATUS;
 						}
 
 						double[] ec = e.getPointRef();
 						double[] c = roi.getPointRef();
 						if (Math.hypot(c[0] - ec[0], c[1] - ec[1]) > delta) {
 							logger.trace("Dropping as too far from centre: {} cf {}", roi, e);
 							roi = null;
 							continue;
 						}
 						n++;
 
 						stat = drawFoundRing(monitor, display, plottingSystem, roi, false);
 						if (!stat.isOK())
 							break;
 					} catch (IllegalArgumentException ex) {
 						logger.trace("Could not find ellipse with {}: {}", r, ex);
 					} finally {
 						if (n >= 0) {
 							currentData.rois.add(roi); // can include null placeholder
 						} else {
 							currentData.rois.clear();
 						}
 					}
 				}
 				currentData.nrois = n;
 				if (currentData.nrois > 0) {
 					currentData.use = true;
 				}
 				return stat;
 			}
 		};
 		job.setPriority(Job.SHORT);
 		return job;
 	}
 
 	/**
 	 * 
 	 * @param system
 	 * @return an IImageTrace
 	 */
 	public static IImageTrace getImageTrace(IPlottingSystem system) {
 		Collection<ITrace> traces = system.getTraces();
 		if (traces != null && traces.size() > 0) {
 			ITrace trace = traces.iterator().next();
 			if (trace instanceof IImageTrace) {
 				return (IImageTrace) trace;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Saves to a csv file a table of arrays
 	 * @param filename
 	 *           The filename to save the data to
 	 * @param names
 	 *           names of each column
 	 * @param values
 	 *           values for each columns and rows
 	 */
 	public static void saveToCsvFile(String filename, String[] names, Object[][] values) {
 		if (names.length != values[0].length) {
 			logger.error("The names and values arrays don't have the same size");
 			return;
 		}
 		try {
 			FileWriter writer = new FileWriter(filename);
 			// write names
 			for (int i = 0; i < names.length; i++) {
 				writer.append(names[i]);
 				writer.append(';');
 			}
 			writer.append('\n');
 			// write values
			for (int i = 0; i < values.length; i++) {
				for (int j = 0; j < values[0].length; j++) {
 					writer.append((String)values[i][j]);
 					writer.append(';');
 				}
 				writer.append('\n');
 			}
 			writer.flush();
 			writer.close();
			logger.debug("Metadata saved to file");
 		} catch (IOException e) {
 			e.printStackTrace();
			logger.error("Error saving metadata:"+e);
 		}
 	}
 }
