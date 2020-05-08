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
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.PaletteData;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.services.AbstractServiceFactory;
 import org.eclipse.ui.services.IServiceLocator;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 
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
 	/**
 	 * getImageData(...) provides an image in a given palette data and origin.
 	 * Faster than getting a resolved image
 	 */
 	public ImageData getImageData(ImageServiceBean bean) {
         
 		final AbstractDataset image    = bean.getImage();
 		final ImageOrigin     origin   = bean.getOrigin();
 		final PaletteData     palette  = bean.getPalette();
 		
 		
 		final int[]   shape = image.getShape();
 		if (bean.isCancelled()) return null;
 		
  		final float[] stats  = getStatistics(bean); 
 		if (bean.isCancelled()) return null;
 		// Above seems to be faster than using stats in AbstractDataset
 		
 		float min = bean.getMin()!=null ? bean.getMin().floatValue() : stats[0];
 		float max = bean.getMax()!=null ? bean.getMax().floatValue() : 3*stats[2];
 		if (max > stats[1])	max = stats[1];
 				
 		int len = image.getSize();
 		if (len == 0) return null;
 
 		float scale_8bit;
 		float maxPixel;
 		if (max > min) {
 			scale_8bit = 255f / (max - min);
 			maxPixel = max - min;
 		} else {
 			scale_8bit = 1f;
 			maxPixel = 0xFF;
 		}
 		
 		if (bean.isCancelled()) return null;
 		byte[] scaledImageAsByte = new byte[len];
 
 		ImageData imageData = null;
 		if (origin==ImageOrigin.TOP_LEFT) { // Fastest - also the default which is nice.
 			
			int byteIndex = 0;
			// This loop is usually the same as the image is read in but not always depending on loader.
			for (int i = 0; i<shape[0]; ++i) {
				for (int j = 0; j<shape[1]; ++j) {
					
					if (bean.isCancelled()) return null;
					final float val = image.getFloat(i, j);
					addByte(val, min, max, scale_8bit, maxPixel, scaledImageAsByte, byteIndex);
					++byteIndex;
				}
 			}
 			imageData = new ImageData(shape[1], shape[0], 8, palette, 1, scaledImageAsByte);
 		
 		} else if (origin==ImageOrigin.BOTTOM_LEFT) {
 			int byteIndex = 0;
 			// This loop is slower than looping over all data and using image.getElementDoubleAbs(...)
 			// However it reorders data for the axes
 			for (int i = shape[1]-1; i>=0; --i) {
 				for (int j = 0; j<shape[0]; ++j) {
 					
 					if (bean.isCancelled()) return null;
 					final float val = image.getFloat(j, i);
 					addByte(val, min, max, scale_8bit, maxPixel, scaledImageAsByte, byteIndex);
 					++byteIndex;
 				}
 			}
 			imageData = new ImageData(shape[0], shape[1], 8, palette, 1, scaledImageAsByte);
 			
 		} else if (origin==ImageOrigin.BOTTOM_RIGHT) {
 			int byteIndex = 0;
 			// This loop is slower than looping over all data and using image.getElementDoubleAbs(...)
 			// However it reorders data for the axes
 			for (int i = shape[0]-1; i>=0; --i) {
 			    for (int j = shape[1]-1; j>=0; --j) {
 					
 					if (bean.isCancelled()) return null;
 					final float val = image.getFloat(i, j);
 					addByte(val, min, max, scale_8bit, maxPixel, scaledImageAsByte, byteIndex);
 					++byteIndex;
 				}
 			}
 			imageData = new ImageData(shape[1], shape[0], 8, palette, 1, scaledImageAsByte);
 			
 		} else if (origin==ImageOrigin.TOP_RIGHT) {
 			int byteIndex = 0;
 			// This loop is slower than looping over all data and using image.getElementDoubleAbs(...)
 			// However it reorders data for the axes
 			for (int i = 0; i<shape[1]; ++i) {
 				for (int j = shape[0]-1; j>=0; --j) {
 					
 					if (bean.isCancelled()) return null;
 					final float val = image.getFloat(j, i);
 					addByte(val, min, max, scale_8bit, maxPixel, scaledImageAsByte, byteIndex);
 					++byteIndex;
 				}
 			}
 			imageData = new ImageData(shape[0], shape[1], 8, palette, 1, scaledImageAsByte);
 		}
 		
 		if (bean.isCancelled()) return null;
 		return imageData;
 
 	}
 
 	/**
 	 * private finals inline well by the compiler.
 	 * @param val
 	 * @param min
 	 * @param max
 	 * @param scale_8bit
 	 * @param maxPixel
 	 * @param scaledImageAsByte
 	 */
 	private final void addByte( final float  val, 
 								final float  min, 
 								final float  max, 
 								final float  scale_8bit, 
 								final float  maxPixel, 
 								final byte[] scaledImageAsByte,
 								final int    index) {
 		
 		float scaled_pixel;
 		if (val < min) {
 			scaled_pixel = 0;
 		} else if (val >= max) {
 			scaled_pixel = maxPixel;
 		} else {
 			scaled_pixel = val - min;
 		}
 		scaled_pixel = scaled_pixel * scale_8bit;
 		// Keep it in bounds
 		final byte pixel = (byte) (0x000000FF & ((int) scaled_pixel));
 
 		scaledImageAsByte[index] = pixel;
 	}
 	
 	private static float[] getStatistics(ImageServiceBean bean) {
 		
 		final AbstractDataset image    = bean.getImage();
 
 		float min = Float.MAX_VALUE;
 		float max = -Float.MAX_VALUE;
 		float sum = 0.0f;
 		final int size = image.getSize();
 		
 		for (int index = 0; index<size; ++index) {
 				
 			if (bean.isCancelled()) return null;
 			final float val = (float)image.getElementDoubleAbs(index);
 			sum += val;
 			if (val < min) min = val;
 			if (val > max) max = val;
 			
 		}
 		float mean = sum / (image.getShape()[0] * image.getShape()[1]);
 		return new float[] { min, max, mean };
 	}
 	
 
 	@Override
 	public Object create(Class serviceInterface, IServiceLocator parentLocator,
 			IServiceLocator locator) {
 		
 		if (serviceInterface==IImageService.class) {
 			return new ImageService();
 		} 
 		return null;
 	}
 
 }
