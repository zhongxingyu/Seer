 package de.hswt.hrm.scheme.service;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
import java.awt.Label;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
 import java.awt.image.ComponentColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageObserver;
 import java.awt.image.WritableRaster;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.PaletteData;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.widgets.Display;
 
 import com.sun.pdfview.PDFFile;
 import com.sun.pdfview.PDFPage;
 import com.sun.pdfview.PDFRenderer;
 
 import de.hswt.hrm.scheme.model.GridImage;
 import de.hswt.hrm.scheme.model.RenderedGridImage;
 
 /**
  * This class converts GridImages to RenderedGridImages
  * 
  * @author Michael Sieger
  * 
  */
 public class GridImageConverter {
 
 	private static final int PPG = 100, // Pixel per grid
 			PPG_THUMBNAIL = 10;
 
 	/**
 	 * Converts a GridImage to a RenderedGridImage
 	 * 
 	 * @param display
 	 * @param gridImage
 	 * @return
 	 * @throws IOException
 	 */
 	public static RenderedGridImage convert(Display display, GridImage gridImage)
 			throws IOException {
 		ByteBuffer buf = ByteBuffer.wrap(gridImage.getImage());
 		PDFFile pdffile = new PDFFile(buf);
 		if(pdffile.getNumPages() != 1){
 			throw new NotSinglePageException(
 					String.format("An image must be single page but has %d", pdffile.getNumPages()));
 		}
 		PDFPage page = pdffile.getPage(0);
 		return new RenderedGridImage(
 				getSWTImage(
 					display,
 					renderImage(page, gridImage.getWidth() * PPG_THUMBNAIL,
 							gridImage.getHeight() * PPG_THUMBNAIL)), 
 				getSWTImage(
 					display,
 					renderImage(page, gridImage.getWidth() * PPG,
 							gridImage.getHeight() * PPG)), gridImage);
 	}
 
 	private static BufferedImage renderImage(final PDFPage page, final int w,
 			final int h) {
	    return (BufferedImage) page.getImage(w, h, page.getBBox(), null, true, true);
 	}
 
 	private static org.eclipse.swt.graphics.Image getSWTImage(Display display,
 			BufferedImage awtImage) {
 		return new org.eclipse.swt.graphics.Image(display, getSWTData(awtImage));
 	}
 
 	/**
 	 * Converts the AWT BufferedImage into an SWT ImageData object.
 	 * 
 	 * @param bufferedImage
 	 * @return
 	 */
 	private static ImageData getSWTData(final BufferedImage bufferedImage) {
	    //ComponentColorModel colorModel = (ComponentColorModel)bufferedImage.getColorModel();
	    ColorModel colorModel = bufferedImage.getColorModel();
 	    //ASSUMES: 3 BYTE BGR IMAGE TYPE
 
 	    PaletteData palette = new PaletteData(0x0000FF, 0x00FF00,0xFF0000);
 	    ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
 
 	    //This is valid because we are using a 3-byte Data model with no transparent pixels
 	    data.transparentPixel = -1;
 
 	    WritableRaster raster = bufferedImage.getRaster();
	    int[] pixelArray = new int[4];
 	    for (int y = 0; y < data.height; y++) {
 	        for (int x = 0; x < data.width; x++) {
 	            raster.getPixel(x, y, pixelArray);
 	            int pixel = palette.getPixel(new RGB(pixelArray[0], pixelArray[1], pixelArray[2]));
 	            data.setPixel(x, y, pixel);
 	        }
 	    }
 	    return data;
 	}
 
 }
