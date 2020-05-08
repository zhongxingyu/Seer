 package com.innovatrics.iseglib;
 
 import java.awt.Dimension;
 
 /**
  * Holds a serialized version of BMP image as produced by the SegLib library routines. The image is always in a RGB format.
  * @author Martin Vysny
  */
 public final class RawBmpImage {
 
     public final byte[] bmp;
     public final int width;
     public final int height;
     public final int offset;
 
     /**
      * Constructs a new BMP image.
      * @param bmp the serialized BMP true-color data.
      * @param width the width of the BMP
      * @param height the height of the BMP
      */
     public RawBmpImage(byte[] bmp, int width, int height) {
 	this.bmp = bmp;
 	this.width = width;
 	this.height = height;
 	this.offset = getOffset(width);
     }
 
     static int getOffset(final int width) {
 	int offset = (width * 3) & 0x03;
 	if (offset != 0) {
 	    offset = 4 - offset;
 	}
 	return offset;
     }
 
     public Dimension getDimension() {
 	return new Dimension(width, height);
     }
 
     public int getScanlineWidth() {
	return width + offset;
     }
 
     /**
      * Returns a RGB pixel at given position.
      * @param x the x coordinate
      * @param y the y coordinate
      * @return RGB pixel: 0x00rrggbb
      */
     public int getPixel(int x, int y) {
 	final int off = (y * getScanlineWidth() + x) * 3 + BMP_HEADER_LENGTH;
 	final int r = bmp[off + 2] & 0xFF;
 	final int g = bmp[off + 1] & 0xFF;
 	final int b = bmp[off] & 0xFF;
 	return r * 65536 + g * 256 + b;
     }
     public static final int BMP_HEADER_LENGTH = 54;
 
     static Dimension getColorBmpDimension(int width, int height, int resolution) {
 	if (resolution != 500) {
 	    int ratio = 256 * resolution / 500;
 	    height = (height << 8) / ratio;
 	    width = (width << 8) / ratio;
 	}
 	return new Dimension(width, height);
     }
     // toBufferedImage will not work properly as DataBufferByte contains a bug - it ignores the 'offset' parameter which is required to skip the BMP header.
 //    public BufferedImage toBufferedImage() {
 //	final DataBufferByte db = new DataBufferByte(bmp, bmp.length, BMP_HEADER_LENGTH);
 //	final WritableRaster raster = Raster.createInterleavedRaster(db, width, height, width * 3 + offset, 3, TRUECOLOR_BMP_BAND_OFFSETS, null);
 //	return new BufferedImage(RGB_COLOR_MODEL, raster, false, null);
 //    }
 //    private static final ColorModel RGB_COLOR_MODEL = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8}, false, true, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
 //    private static final int[] TRUECOLOR_BMP_BAND_OFFSETS = new int[]{2, 1, 0};
 }
