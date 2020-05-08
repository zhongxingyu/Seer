 package org.dawnsci.jexl.internal;
 
 import java.util.List;
 
 import org.dawb.common.services.IImageProcessingService;
 import org.dawb.common.services.ServiceManager;
 
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 
 /**
  * Class to wrap the methods used to IDataset object with BoofCV algorithms
  * allowing them to be called in a manner consistent with the other Jexl functions
  * i.e. namespace:methodName()
  * <p>
  * Methods only for use in the DawnJexlEngine which can be obtained from JexlUtils
  * 
  * @author wqk87977
  *
  */
 public class JexlImageProcessingFunctions {
 
 	private static IImageProcessingService service;
 
 	private static void createService() throws Exception {
 		if (service == null)
 			service = (IImageProcessingService) ServiceManager.getService(IImageProcessingService.class);
 	}
 
 	/**
 	 * Applies Gaussian blur.
 	 *
 	 * @param input Input image.  Not modified.
 	 * @param sigma Gaussian distribution's sigma.  If <= 0 then will be selected based on radius.
 	 * @param radius Radius of the Gaussian blur function. If <= 0 then radius will be determined by sigma.
 	 * @return Output blurred image.
 	 * @throws Exception 
 	 */
 	public static IDataset filterGaussianBlur(IDataset input, double sigma, int radius) throws Exception {
 		createService();
 		return service.filterGaussianBlur(input, sigma, radius);
 	}
 
 	/**
 	 * Computes the derivative in the X and Y direction using an integer Sobel edge detector.
 	 *
 	 * @param orig   Input image.  Not modified.
	 * @param isY true to return the Y derivative, false to return the x.
 	 * @return Output list containing the image derivative along the x-axis and y-axis 
 	 * @throws Exception 
 	 */
	public static IDataset filterDerivativeSobel(IDataset orig, boolean isY) throws Exception {
 		createService();
		List<IDataset> ret = service.filterDerivativeSobel(orig);
		if (isY) {
			return ret.get(1);
		} else {
			return ret.get(0);
		}
 	}
 
 	/**
 	 * Applies a global threshold across the whole image.  If 'down' is true, then pixels with values <=
 	 * to 'threshold' are set to 1 and the others set to 0.  If 'down' is false, then pixels with values >=
 	 * to 'threshold' are set to 1 and the others set to 0.
 	 *
 	 * @param input Input image. Not modified.
 	 * @param threshold threshold value.
 	 * @param down If true then the inequality <= is used, otherwise if false then >= is used.
 	 * @param isBinary if true will convert to a binary image
 	 * @return Output image.
 	 * @throws Exception 
 	 */
 	public static IDataset filterThreshold(IDataset input, float threshold , boolean down, boolean isBinary) throws Exception {
 		createService();
 		return service.filterThreshold(input, threshold, down, isBinary);
 	}
 
 	/**
 	 * <p>
 	 * Erodes an image according to a 8-neighbourhood.  Unless a pixel is connected to all its neighbours its value
 	 * is set to zero.
 	 * </p>
 	 *
 	 * @param input  Input image. Not modified.
 	 * @param isBinary if true will convert to a binary image
 	 * @return Output image.
 	 * @throws Exception 
 	 */
 	public static IDataset filterErode(IDataset input, boolean isBinary) throws Exception {
 		createService();
 		return service.filterErode(input, isBinary);
 	}
 
 	/**
 	 * <p>
 	 * Dilates an image according to a 8-neighbourhood.  If a pixel is connected to any other pixel then its output
 	 * value will be one.
 	 * </p>
 	 *
 	 * @param input  Input image. Not modified.
 	 * @param isBinary if true will convert to a binary image
 	 * @return Output image.
 	 * @throws Exception 
 	 */
 	public static IDataset filterDilate(IDataset input, boolean isBinary) throws Exception {
 		createService();
 		return service.filterDilate(input, isBinary);
 	}
 
 	/**
 	 * <p>
 	 * Erodes and dilates an image. See filterErode and filetDilate
 	 * </p>
 	 *
 	 * @param input  Input image. Not modified.
  	 * @param isBinary if true will convert to a binary image
 	 * @return Output image.
 	 * @throws Exception 
 	 */
 	public static IDataset filterErodeAndDilate(IDataset input, boolean isBinary) throws Exception {
 		createService();
 		return service.filterErodeAndDilate(input, isBinary);
 	}
 
 	/**
 	 * <p>
 	 * Given a binary image, connect together pixels to form blobs/clusters using the specified connectivity rule.
 	 * The found blobs will be labelled in an output image and also described as a set of contours.  Pixels
 	 * in the contours are consecutive order in a clockwise or counter-clockwise direction, depending on the
 	 * implementation.
 	 * </p>
 	 *
 	 * <p>
 	 * The returned contours are traces of the object.  The trace of an object can be found by marking a point
 	 * with a pen and then marking every point on the contour without removing the pen.  It is possible to have
 	 * the same point multiple times in the contour.
 	 * </p>
 	 *
 	 * @param input Input binary image.  Not modified.
 	 * @param rule Connectivity rule.  Can be 4 or 8.  8 is more commonly used.
 	 * @param colorExternal RGB colour
 	 * @param colorInternal RGB colour
 	 * @return Dataset contours for each blob.
 	 * @throws Exception 
 	 */
 	public static IDataset filterContour(IDataset input, int rule, int colorExternal, int colorInternal) throws Exception {
 		createService();
 		return service.filterContour(input, rule, colorExternal, colorInternal);
 	}
 }
