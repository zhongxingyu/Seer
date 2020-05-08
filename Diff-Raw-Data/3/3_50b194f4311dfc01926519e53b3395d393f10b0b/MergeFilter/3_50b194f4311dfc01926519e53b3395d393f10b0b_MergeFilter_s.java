 package org.gvlabs.image.utils.filter;
 
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorModel;
 import java.awt.image.MemoryImageSource;
 import java.awt.image.PixelGrabber;
 
 /**
  * Merge images
  * 
  * @author Thiago Galbiatti Vespa
  * @version 3.0
  */
 public class MergeFilter implements ImageFilter {
 	protected double weigth;
 	protected Image original;
 
 	protected int resultArray[];
 
 	/**
 	 * Construtor. Default weigth is 0.5
 	 * 
 	 * @param original
 	 *            original image
 	 */
 	public MergeFilter(Image original) {
 		this.original = original;
 		weigth = 0.5;
 		resultArray = null;
 	}
 
 	/**
 	 * Set the weight to apply the filter
 	 * 
 	 * @param weigth
 	 *            weight to apply the filter
 	 */
 	public void setWeight(double weigth) {
 		this.weigth = weigth;
 	}
 
 	/**
 	 * Do the merge of the original image and the toMerge image
 	 * 
 	 * @param toMerge
 	 *            image to merge with the original
 	 * @return merged image
 	 */
 	public Image merge(Image toMerge) {
 
 		int widOriginal = original.getWidth(null);
 		int widToMerge = toMerge.getWidth(null);
 		int hgtOriginal = original.getHeight(null);
 		int hgtToMerge = toMerge.getHeight(null);
 
 		int resultWid = Math.max(widOriginal, widToMerge);
 		int resultHgt = Math.max(hgtOriginal, hgtToMerge);
 
 		resultArray = new int[resultWid * resultHgt];
 
 		int[] pxOriginal = new int[resultWid * resultHgt];
 		int[] pxToMerge = new int[resultWid * resultHgt];
 
 		PixelGrabber pgOriginal = new PixelGrabber(original, 0, 0, widOriginal, hgtOriginal, pxOriginal, 0,
 				resultWid);
 		try {
 			pgOriginal.grabPixels();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 		PixelGrabber pgToMerge = new PixelGrabber(toMerge, 0, 0, widToMerge, hgtToMerge, pxToMerge, 0,
 				resultWid);
 
 		try {
 			pgToMerge.grabPixels();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 		int y, x, resultPx, rpi;
 		int redOriginal, redToMerge, redResult;
 		int greenOriginal, greenToMerge, greenResult;
 		int blueOriginal, blueToMerge, blueResult;
 		int alphaOriginal, alphaToMerge, alphaResult;
 		double wgtOriginal, wgtToMerge;
 
 		// Merge
 		for (y = 0; y < resultHgt; y++) {
 			for (x = 0; x < resultWid; x++) {
 				rpi = y * resultWid + x;
 				resultPx = 0;
 				
 				// Get pixel Color
 				blueOriginal = pxOriginal[rpi] & 0x00ff;
 				blueToMerge = pxToMerge[rpi] & 0x00ff;
 				greenOriginal = (pxOriginal[rpi] >> 8) & 0x00ff;
 				greenToMerge = (pxToMerge[rpi] >> 8) & 0x00ff;
 				redOriginal = (pxOriginal[rpi] >> 16) & 0x00ff;
 				redToMerge = (pxToMerge[rpi] >> 16) & 0x00ff;
 				alphaOriginal = (pxOriginal[rpi] >> 24) & 0x00ff;
 				alphaToMerge = (pxToMerge[rpi] >> 24) & 0x00ff;
 
 				// Calculates weigth
 				wgtOriginal = weigth * (alphaOriginal / 255.0);
 				wgtToMerge = (1.0 - weigth) * (alphaToMerge / 255.0);
 
 				// Apply weigth
 				redResult = (int) (redOriginal * wgtOriginal + redToMerge * wgtToMerge);
 				redResult = (redResult < 0) ? (0) : ((redResult > 255) ? (255) : (redResult));
 				greenResult = (int) (greenOriginal * wgtOriginal + greenToMerge * wgtToMerge);
 				greenResult = (greenResult < 0) ? (0) : ((greenResult > 255) ? (255)
 						: (greenResult));
 				blueResult = (int) (blueOriginal * wgtOriginal + blueToMerge * wgtToMerge);
 				blueResult = (blueResult < 0) ? (0) : ((blueResult > 255) ? (255) : (blueResult));
 				alphaResult = 255;
 
 				// Result pixel
 				resultPx = (((((alphaResult << 8) + (redResult & 0x0ff)) << 8) + (greenResult & 0x0ff)) << 8)
 						+ (blueResult & 0x0ff);
 
 				resultArray[rpi] = resultPx;
 			}
 		}
 
 		// TODO: Optimize
 		Image ret;
 		MemoryImageSource mis;
		if (resultArray == null) {
			return null;
		}
 		mis = new MemoryImageSource(resultWid, resultHgt, ColorModel.getRGBdefault(), resultArray, 0,
 				resultWid);
 		ret = Toolkit.getDefaultToolkit().createImage(mis);
 		resultArray = null;
 		return ret;
 
 	}
 
 	@Override
 	public BufferedImage applyTo(BufferedImage src) {
 		Image merged = this.merge(src);
 		
 		// Create a Buffered Image from a generic image
 		// TODO: Optimize
 		BufferedImage bImage = new BufferedImage(merged.getWidth(null),
 				merged.getHeight(null), BufferedImage.TYPE_INT_RGB);
 
 		Graphics2D bImageGraphics = bImage.createGraphics();
 
 		bImageGraphics.drawImage(merged, null, null);
 
 		return bImage;
 	}
 }
