 package org.dawnsci.plotting.tools.diffraction;
 
 import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.trace.IImageTrace;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.swt.widgets.Display;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionMetadataUtils;
 import uk.ac.diamond.scisoft.analysis.diffraction.PeakFittingEllipseFinder;
 import uk.ac.diamond.scisoft.analysis.diffraction.PowderRingsUtils;
 import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
 import uk.ac.diamond.scisoft.analysis.io.ILoaderService;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.roi.EllipticalFitROI;
 import uk.ac.diamond.scisoft.analysis.roi.EllipticalROI;
 import uk.ac.diamond.scisoft.analysis.roi.IParametricROI;
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 import uk.ac.diamond.scisoft.analysis.roi.PolylineROI;
 
 public class DiffractionUtils {
 	
 	private static final Logger logger = LoggerFactory.getLogger(DiffractionUtils.class);
 
 	/**
 	 * Fetch diffraction metadata, doesn't add it to the IDataset
 	 * @param image
 	 * @param altPath alternative for file path if metadata is null or does not hold it
 	 * @param service
 	 * @param statusText returned message (can be null)
 	 * @return diffraction metadata
 	 */
 	public static IDiffractionMetadata getDiffractionMetadata(IDataset image, String altPath, ILoaderService service, String[] statusText) {
 		// Now always returns IDiffractionMetadata to prevent creation of a new
 		// metadata object after listeners have been added to the old metadata
 		//TODO improve this section- it's pretty horrible
 		IDiffractionMetadata lockedMeta = service.getLockedDiffractionMetaData();
 		
 		if (image == null)
 			return lockedMeta;
 
 		int[] shape = image.getShape();
 		IMetaData mdImage = null;
 		try {
 			mdImage = image.getMetadata();
 		} catch (Exception e1) {
 			// do nothing
 		}
 		if (lockedMeta != null) {
 			if (mdImage instanceof IDiffractionMetadata) {
 				IDiffractionMetadata dmd = (IDiffractionMetadata) mdImage;
 				if (!dmd.getDiffractionCrystalEnvironment().equals(lockedMeta.getDiffractionCrystalEnvironment()) ||
 						!dmd.getDetector2DProperties().equals(lockedMeta.getDetector2DProperties())) {
 					try {
 						DiffractionMetadataUtils.copyNewOverOld(lockedMeta, (IDiffractionMetadata)mdImage);
 					} catch (IllegalArgumentException e) {
 						if (statusText != null)
 							statusText[0] = "Locked metadata does not match image dimensions!";
 					}
 				}
 			} else {
 				//TODO what if the image is rotated?
 				
 				if (shape[0] == lockedMeta.getDetector2DProperties().getPx() &&
 					shape[1] == lockedMeta.getDetector2DProperties().getPy()) {
 				} else {
 					IDiffractionMetadata clone = lockedMeta.clone();
 					clone.getDetector2DProperties().setPx(shape[0]);
 					clone.getDetector2DProperties().setPy(shape[1]);
 					if (statusText != null)
 						statusText[0] = "Locked metadata does not match image dimensions!";
 				}
 			}
 			if (statusText != null && statusText[0] == null) {
 				statusText[0] = "Metadata loaded from locked version";
 			}
 			return lockedMeta;
 		}
 
 		//If not see if the trace has diffraction meta data
 		if (mdImage instanceof IDiffractionMetadata) {
 			if (statusText != null && statusText[0] == null) {
 				statusText[0] = "Metadata loaded from image";
 			}
 			return (IDiffractionMetadata) mdImage;
 		}
 		
 		//Try and get the filename here, it will help later on
 		String filePath = mdImage == null ? null : mdImage.getFilePath();
 		
 		if (filePath == null) {
 			filePath = altPath;
 		}
 		
 		if (filePath != null) {
 			//see if we can read diffraction info from nexus files
 			NexusDiffractionMetaCreator ndmc = new NexusDiffractionMetaCreator(filePath);
 			IDiffractionMetadata difMet = ndmc.getDiffractionMetadataFromNexus(shape);
 			if (difMet !=null) {
				//TODO comment out
				//image.setMetadata(difMet);
 				if (statusText != null && statusText[0] == null) {
 					if (ndmc.isCompleteRead())
 						statusText[0] = "Metadata completely loaded from nexus tree";
 					else if (ndmc.isPartialRead())
 						statusText[0] = "Required metadata loaded from nexus tree";
 					else if (ndmc.anyValuesRead())
 						statusText[0] = "Partial metadata loaded from nexus tree";
 					else
 						statusText[0] = "No metadata in nexus tree, metadata loaded from preferences";
 				}
 				return difMet;
 			}
 		}
 
 		// if it is null try and get it from the loader service
 		if (mdImage == null && filePath != null) {
 			IMetaData md = null;
 			try {
 				md = service.getMetaData(filePath, null);
 			} catch (Exception e) {
 				logger.error("Cannot read meta data from part", e);
 			}
 			
 			//If it is there and diffraction data return it
 			if (md instanceof IDiffractionMetadata) {
 				if (statusText != null && statusText[0] == null) {
 					statusText[0] = "Metadata loaded from file";
 				}
 				return (IDiffractionMetadata) md;
 			}
 		}
 		
 		// if there is no meta or is not nexus or IDiff create default IDiff and put it in the dataset
 		mdImage = DiffractionDefaultMetadata.getDiffractionMetadata(filePath, shape);
 		image.setMetadata(mdImage);
 		if (statusText != null && statusText[0] == null) {
 			statusText[0] = "No metadata found. Values loaded from preferences:";
 		}
 		return (IDiffractionMetadata) mdImage;
 	}
 
 	public static IROI runConicPeakFit(final IProgressMonitor monitor, Display display,
 			final IPlottingSystem plotter, IImageTrace t, IParametricROI roi, IParametricROI[] innerOuter, int nPoints) {
 	
 		if (roi == null)
 			return null;
 
 		final ProgressMonitorWrapper mon = new ProgressMonitorWrapper(monitor);
 		monitor.subTask("Find POIs near initial ellipse");
 		AbstractDataset image = (AbstractDataset) t.getData();
 		BooleanDataset mask = (BooleanDataset) t.getMask();
 		PolylineROI points;
 		monitor.subTask("Fit POIs");
 		
 		points = PeakFittingEllipseFinder.findPointsOnConic(image, mask,roi, innerOuter,nPoints, mon);
 		
 		if (monitor.isCanceled())
 			return null;
 		
 		if (points == null) return null;
 		
 		if (roi instanceof EllipticalROI) {
 			if (points.getNumberOfPoints() < 3) {
 				throw new IllegalArgumentException("Could not find enough points to trim");
 			}
 
 			monitor.subTask("Trim POIs");
 			EllipticalFitROI efroi = PowderRingsUtils.fitAndTrimOutliers(mon, points, 5, false);
 			logger.debug("Found {}...", efroi);
 			monitor.subTask("");
 			return efroi;
 		}
 		
 		return points;
 	}
 	
 	
 //	public static IROI runEllipsePeakFit(final IProgressMonitor monitor, Display display,
 //			final IPlottingSystem plotter, IImageTrace t, IROI roi, double innerRadius, double outerRadius, int nPoints) {
 //		
 //		if (roi == null)
 //			return null;
 //
 //		final ProgressMonitorWrapper mon = new ProgressMonitorWrapper(monitor);
 //		monitor.subTask("Find POIs near initial ellipse");
 //		AbstractDataset image = (AbstractDataset) t.getData();
 //		BooleanDataset mask = (BooleanDataset) t.getMask();
 //		PolylineROI points;
 //		EllipticalFitROI efroi;
 //		monitor.subTask("Fit POIs");
 //		
 //		points = PeakFittingEllipseFinder.findPointsOnEllipse(image, mask, (EllipticalROI) roi, innerRadius, outerRadius,nPoints, mon);
 //		
 //		if (monitor.isCanceled())
 //			return null;
 //		
 //		if (points == null) return null;
 //		
 //		if (points.getNumberOfPoints() < 3) {
 //			throw new IllegalArgumentException("Could not find enough points to trim");
 //		}
 //
 //		monitor.subTask("Trim POIs");
 //		efroi = PowderRingsUtils.fitAndTrimOutliers(mon, points, 5, false);
 //		logger.debug("Found {}...", efroi);
 //		monitor.subTask("");
 //
 //		return efroi;
 //	}
 
 }
