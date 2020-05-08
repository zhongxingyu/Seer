 package com.innovatrics.iseglib;
 
 import java.awt.Dimension;
 import java.awt.Transparency;
 import java.awt.color.ColorSpace;
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorModel;
 import java.awt.image.ComponentColorModel;
 import java.awt.image.DataBuffer;
 import java.awt.image.DataBufferByte;
 import java.awt.image.Raster;
 import java.awt.image.SampleModel;
 import java.awt.image.WritableRaster;
 
 /**
 * Holds a raw image.
  * @author Martin Vysny
  */
 public class RawImage {
 
     /**
      * Creates a new raw image.
      * @param width The number of pixels indicating the width of the image
      * @param height The number of pixels indicating the height of the image
      * @param rawImage raw image in 8-bit grayscale raw format (0=black, 127=gray, 255=white),
      */
     public RawImage(int width, int height, byte[] rawImage) {
 	this.width = width;
 	this.height = height;
 	this.image = rawImage;
     }
     /**
      * The number of pixels indicating the width of the image
      */
     public final int width;
     /**
      *The number of pixels indicating the height of the image
      */
     public final int height;
     /**
      *  raw image in 8-bit raw format,
      */
     public final byte[] image;
 
     @Override
     public String toString() {
 	return "RawImage{" + width + "x" + height + '}';
     }
 
     public BufferedImage toBufferedImage() {
 	final SampleModel sm = DEFAULT_COLOR_MODEL.createCompatibleSampleModel(width, height);
 	final DataBufferByte db = new DataBufferByte(image, width * height);
 	final WritableRaster raster = Raster.createWritableRaster(sm, db, null);
 	final BufferedImage result = new BufferedImage(DEFAULT_COLOR_MODEL, raster, false, null);
 	return result;
     }
     private static final ColorModel DEFAULT_COLOR_MODEL = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), new int[]{8}, false, true, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
 
     /**
      * Returns a pixel located on x,y coordinates. 
      * @param x the x coordinate
      * @param y the y coordinate
      * @return the pixel value, 0 = black, 127 = gray, 255 = white.
      */
     public byte getPixel(int x, int y) {
 	return image[y * width + x];
     }
 
     /**
      * Returns size of the image.
      * @return the dimension object.
      */
     public Dimension getDimension() {
 	return new Dimension(width, height);
     }
 }
