 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawb.gda.extensions.util;
 
 import org.dawb.common.services.IImageService;
 import org.dawb.common.services.ImageServiceBean;
 import org.dawb.common.services.ImageServiceBean.HistoType;
 import org.dawb.common.services.ImageServiceBean.ImageOrigin;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.PaletteData;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.services.AbstractServiceFactory;
 import org.eclipse.ui.services.IServiceLocator;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.Stats;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.utils.SWTImageUtils;
 
 /**
  * 
  
    Histogramming Explanation
    ---------------------------
    Image intensity distribution:
 
                 ++----------------------**---------------
                 +                      *+ *              
                 ++                    *    *             
                 |                     *    *             
                 ++                    *     *            
                 *                    *       *            
                 +*                   *       *            
                 |*                  *        *            
                 +*                  *        *           
                 |                  *          *         
                 ++                 *          *          
                 |                  *           *        
                 ++                 *           *        
                 |                 *            *        
                 ++                *            *       
                                  *              *      
         Min Cut           Min    *              *      Max                     Max cut
  Red <- |   (min colour)  |    (color range, palette)  |      (max color)      | -> Blue
                                 *                 *  
                 |              *        +         *  
 ----------------++------------**---------+----------**----+---------------**+---------------++
 
  
  * @author fcp94556
  *
  */
 public class ImageService extends AbstractServiceFactory implements IImageService {
 	
 	
 	static {
 		// We just use file extensions
 		LoaderFactory.setLoaderSearching(false); 
 		// This now applies for the whole workbench
 	}
 
 	public ImageService() {
 		
 	}
 	
 	/**
 	 * 
 	 */
 	public Image getImage(ImageServiceBean bean) {
 		final ImageData data = getImageData(bean);
 		return new Image(Display.getCurrent(), data);
 	}
 	
 	private static final int MIN_PIX_INDEX = 253;
 	private static final int NAN_PIX_INDEX = 254;
 	private static final int MAX_PIX_INDEX = 255;
 	
 	private static final byte MIN_PIX_BYTE = (byte)(MIN_PIX_INDEX & 0xFF);
 	private static final byte NAN_PIX_BYTE = (byte)(NAN_PIX_INDEX & 0xFF);
 	private static final byte MAX_PIX_BYTE = (byte)(MAX_PIX_INDEX & 0xFF);
 	
 	/**
 	 * getImageData(...) provides an image in a given palette data and origin.
 	 * Faster than getting a resolved image
 	 */
 	public ImageData getImageData(ImageServiceBean bean) {
         
 		final AbstractDataset image    = bean.getImage();
 		final ImageOrigin     origin   = bean.getOrigin();
 		PaletteData     palette  = bean.getPalette();
 		
 		int depth = bean.getDepth();
 		final int size  = (int)Math.round(Math.pow(2, depth));
 		
 		createMaxMin(bean);
		final float max = bean.getMax().floatValue();
		final float min = bean.getMin().floatValue();
 		
 		if (bean.getFunctionObject()!=null && bean.getFunctionObject() instanceof FunctionContainer) {
 			final FunctionContainer fc = (FunctionContainer)bean.getFunctionObject();
 			// TODO This does not support masking or cut bounds for zingers and dead pixels.
 			return SWTImageUtils.createImageData(image, min, max, fc.getRedFunc(), 
 					                                              fc.getGreenFunc(), 
 					                                              fc.getBlueFunc(), 
 					                                              fc.isInverseRed(), 
 					                                              fc.isInverseGreen(), 
 					                                              fc.isInverseBlue());
 		}
 		
 		if (depth>8) { // Depth > 8 will not work properly at the moment.
 			throw new RuntimeException(getClass().getSimpleName()+" only supports 8-bit images unless we a FunctionContainer has been set!");
 			//if (depth == 16) palette = new PaletteData(0x7C00, 0x3E0, 0x1F);
 			//if (depth == 24) palette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
 			//if (depth == 32) palette = new PaletteData(0xFF00, 0xFF0000, 0xFF000000);
 		}
 		
 		final int[]   shape = image.getShape();
 		if (bean.isCancelled()) return null;	
 				
 		int len = image.getSize();
 		if (len == 0) return null;
 
 		// The last three indices of the palette are always taken up with bound colours
 		createCutColours(bean); // Modifys the palette data and sets the withheld indices
 		
 		float scale;
 		float maxPixel;
 		if (max > min) {
 			// 4 because 1 less than size and then 1 for each bound color is lost.
 			scale = Float.valueOf(size-4) / (max - min);
 			maxPixel = max - min;
 		} else {
 			scale = 1f;
 			maxPixel = 0xFF;
 		}
 		if (bean.isCancelled()) return null;
 		
 		BooleanDataset mask = bean.getMask()!=null
 				            ? (BooleanDataset)DatasetUtils.cast(bean.getMask(), AbstractDataset.BOOL)
 				            : null;
 				            
  		ImageData imageData = null;
  		
  		// We use a byte array directly as this is faster than using setPixel(...)
  		// on image data. Set pixel does extra floating point operations. The downside
  		// is that by doing this we certainly have to have 8 bit as getPixelColorIndex(...)
  		// forces the use of on byte.
 		final byte[] scaledImageAsByte = new byte[len];
 
 		// NOTE: get1DIndex(...) is slow
 		if (mask!=null) mask.setExtendible(false);
 		image.setExtendible(false);
 
 		if (origin==ImageOrigin.TOP_LEFT) { 
 			
 			int index = 0;
 			// This loop is usually the same as the image is read in but not always depending on loader.
 			for (int i = 0; i<shape[0]; ++i) {
 				for (int j = 0; j<shape[1]; ++j) {
 					
 					if (bean.isCancelled()) return null;				
 					// This saves a value lookup when the pixel is certainly masked.
 					int dIndex = image.get1DIndex(i, j);
 					scaledImageAsByte[index] = mask==null || mask.getElementBooleanAbs(dIndex)
 						            ? getPixelColorIndex(image.getElementDoubleAbs(dIndex), min, max, scale, maxPixel, bean)
 					                : NAN_PIX_BYTE;
 					index++;
 				}
 			}
 			imageData = new ImageData(shape[1], shape[0], 8, palette, 1, scaledImageAsByte);
 	
 		} else if (origin==ImageOrigin.BOTTOM_LEFT) {
 
 			int index = 0;
 			// This loop is slower than looping over all data and using image.getElementDoubleAbs(...)
 			// However it reorders data for the axes
 			for (int i = shape[1]-1; i>=0; --i) {
 				for (int j = 0; j<shape[0]; ++j) {
 					
 					if (bean.isCancelled()) return null;
 					// This saves a value lookup when the pixel is certainly masked.
 					int dIndex = image.get1DIndex(j, i);
 					scaledImageAsByte[index]  = mask==null || mask.getElementBooleanAbs(dIndex)
 				                    ? getPixelColorIndex(image.getElementDoubleAbs(dIndex), min, max, scale, maxPixel, bean)
 			                        : NAN_PIX_BYTE;
 					index++;
 				}
 			}
 			imageData = new ImageData(shape[0], shape[1], 8, palette, 1, scaledImageAsByte);
 			
 		} else if (origin==ImageOrigin.BOTTOM_RIGHT) {
 
 			int index = 0;
 			// This loop is slower than looping over all data and using image.getElementDoubleAbs(...)
 			// However it reorders data for the axes
 			for (int i = shape[0]-1; i>=0; --i) {
 			    for (int j = shape[1]-1; j>=0; --j) {
 				
 					if (bean.isCancelled()) return null;
 
 					// This saves a value lookup when the pixel is certainly masked.
 					int dIndex = image.get1DIndex(i, j);
 					scaledImageAsByte[index] = mask==null || mask.getElementBooleanAbs(dIndex)
 						            ? getPixelColorIndex(image.getElementDoubleAbs(dIndex), min, max, scale, maxPixel, bean)
 					                : NAN_PIX_BYTE;
 						index++;
 				}
 			}
 			imageData = new ImageData(shape[1], shape[0], 8, palette, 1, scaledImageAsByte);
 			
 		} else if (origin==ImageOrigin.TOP_RIGHT) {
 
 			int index = 0;
 			// This loop is slower than looping over all data and using image.getElementDoubleAbs(...)
 			// However it reorders data for the axes
 			for (int i = 0; i<shape[1]; ++i) {
 				for (int j = shape[0]-1; j>=0; --j) {
 					
 					if (bean.isCancelled()) return null;
 					scaledImageAsByte[index]  = mask==null || mask.getBoolean(j,i)
 		                            ? getPixelColorIndex(image.getFloat(j, i), min, max, scale, maxPixel, bean)
 	                                : NAN_PIX_BYTE;
 		        	index++;
 				}
 			}
 			imageData = new ImageData(shape[0], shape[1], 8, palette, 1, scaledImageAsByte);
 		}
 		
 		if (bean.isCancelled()) return null;
 		
 		return imageData;
 	}
 
 	/**
 	 * Calling this wipes out the last three RGBs. Even if you set max
 	 * @param bean
 	 */
 	private void createCutColours(ImageServiceBean bean) {
 		
 		// We *DO NOT* copy the palete here so up to 3 of the original
 		// colors can be changed. Instead whenever a palette is given to an
 		// ImageService bean it should be original.
 		
 		
 		// We have three special values, those which are greater than the max cut,
 		// less than the min cut and the NaN number. For these we use special pixel
 		// values in the palette as defined by the cut bound if it is set.
 		if (bean.getMinimumCutBound()!=null && bean.getMinimumCutBound().getColor()!=null) {
 			bean.getPalette().colors[MIN_PIX_INDEX] = bean.getMinimumCutBound().getColor();
 		}
 		
 		if (bean.getNanBound()!=null && bean.getNanBound().getColor()!=null) {
 			bean.getPalette().colors[NAN_PIX_INDEX] = bean.getNanBound().getColor();
 		}
 		
 		if (bean.getMaximumCutBound()!=null && bean.getMaximumCutBound().getColor()!=null) {
 			bean.getPalette().colors[MAX_PIX_INDEX] = bean.getMaximumCutBound().getColor();
 		}
 		
 	}
 
 	private void createMaxMin(ImageServiceBean bean) {
 		
 		float[] stats  = null;
 		if (bean.getMin()==null) {
 			if (stats==null) stats = getFastStatistics(bean); // do not get unless have to
 			bean.setMin(stats[0]);
 		}
 		
 		if (bean.getMax()==null) {
 			if (stats==null) stats = getFastStatistics(bean); // do not get unless have to
 		    bean.setMax(stats[1]);
 		}		
 	}
 
 	/**
 	 * private finals inline well by the compiler.
 	 * @param val
 	 * @param min
 	 * @param max
 	 * @param scale
 	 * @param maxPixel
 	 * @param scaledImageAsByte
 	 */
 	private final static byte getPixelColorIndex(final double  val, 
 												 final float  min, 
 												 final float  max, 
 												 final float  scale, 
 												 final float  maxPixel,
 												 final ImageServiceBean bean) {
 	    
 		// Deal with bounds
 		if (!bean.isInsideMinCut(val)) {
 			return MIN_PIX_BYTE;
 		}
 		if (!bean.isValidNumber(val))  {
 			return NAN_PIX_BYTE;
 		}
 		if (!bean.isInsideMaxCut(val)) {
 			return MAX_PIX_BYTE;
 		}
 		
 		float scaled_pixel;
 		if (val < min) {
 			scaled_pixel = 0;
 		} else if (val >= max) {
 			scaled_pixel = maxPixel;
 		} else {
 			scaled_pixel = (float)val - min;
 		}
 		scaled_pixel = scaled_pixel * scale;
 		
 		return (byte)((int)scaled_pixel & 0xFF);	
 	}
 	
 	/**
 	 * Fast statistcs as a rough guide - this is faster than AbstractDataset.getMin()
 	 * and getMax() which may cache but slows the opening of images too much.
 	 * 
 	 * @param bean
 	 * @return [0] = min [1] = max
 	 */
 	public float[] getFastStatistics(ImageServiceBean bean) {
 		
 		final AbstractDataset image    = bean.getImage();
 		float min = Float.MAX_VALUE;
 		float max = -Float.MAX_VALUE;
 		float sum = 0.0f;
 		final int size = image.getSize();
 		
 		BooleanDataset mask = bean.getMask()!=null
 	                        ? (BooleanDataset)DatasetUtils.cast(bean.getMask(), AbstractDataset.BOOL)
 	                        : null;
 
 	    // Big loop warning:
 		for (int index = 0; index<size; ++index) {
 			
 			final double dv = image.getElementDoubleAbs(index);
 			if (mask!=null && !mask.getElementBooleanAbs(index)) continue; // Masked!
 			
 			if (Double.isNaN(dv))      continue;
 			if (!bean.isInBounds(dv))  continue;
 						
 			final float val = (float)dv;
 			
 			sum += val;
 			if (val < min) min = val;
 			if (val > max) max = val;
 			
 		}
 		
 		float retMin = min;
 		float retMax = Float.NaN;
 		
 		if (bean.getHistogramType()==HistoType.MEAN) {
 			float mean = sum / size;
 			retMax = ((float)Math.E)*mean; // Not statistical, E seems to be better than 3...
 			
 		} else if (bean.getHistogramType()==HistoType.MEDIAN) { 
 			
 			float median = Float.NaN;
 			try {
 				median = ((Number)Stats.median(image)).floatValue(); // SLOW
 			} catch (Exception ne) {
 				median = ((Number)Stats.median(image.cast(AbstractDataset.INT16))).floatValue();// SLOWER
 			}
 			retMax = 2f*median;
 		}
 		
 		if (retMax > max)	retMax = max;
 		
 		return new float[]{retMin, retMax};
 
 	}
 
 	@Override
 	public Object create(Class serviceInterface, IServiceLocator parentLocator, IServiceLocator locator) {
 		
 		if (serviceInterface==IImageService.class) {
 			return new ImageService();
 		} 
 		return null;
 	}
 	
 	public static final class SDAFunctionBean {
 		
 	}
 
 }
