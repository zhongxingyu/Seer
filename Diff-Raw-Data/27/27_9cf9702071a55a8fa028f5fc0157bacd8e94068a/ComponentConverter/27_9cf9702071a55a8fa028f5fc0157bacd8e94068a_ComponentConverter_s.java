 package de.hswt.hrm.scheme.service;
 
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorModel;
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
 
 import de.hswt.hrm.component.model.Component;
 import de.hswt.hrm.scheme.model.RenderedComponent;
 import de.hswt.hrm.scheme.model.ThumbnailImage;
 
 /**
  * This class converts Components to RenderedComponents
  * 
  * @author Michael Sieger
  * 
  */
 public class ComponentConverter {
     
     private static final ImageObserver observer = new ImageObserver(){
 
         @Override
         public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
             return true;
         }
         
     };
 
     /**
      * Pixel per grid for the images
      */
 	private static final int PPG = 100;
 	
 	/**
 	 * Size of the Thumbnails (Is always the same)
 	 */
 	private static final int THUMB_WIDTH = 50,
 								THUMB_HEIGHT = 50;
 
 	/**
 	 * Converts a GridImage to a RenderedGridImage
 	 * 
 	 * @param display
 	 * @param gridImage
 	 * @return
 	 * @throws IOException
 	 */
 	public static RenderedComponent convert(Display display, Component component)
 			throws IOException {
 		if(!component.getCategory().isPresent()){
 			throw new IllegalArgumentException("The category must be present here");
 		}
 		final int w = component.getCategory().get().getWidth();
 		final int h = component.getCategory().get().getHeight();
 		// FIXME: check if we must handle null values here...
 		return new RenderedComponent(component, 
				createImage(display, component.getLeftRightImage().get().getBlob(), h, w), //Is Swapped because width and height
				createImage(display, component.getRightLeftImage().get().getBlob(), h, w), //refer to the upstanding image
				createImage(display, component.getUpDownImage().get().getBlob(), w, h), 
				createImage(display, component.getDownUpImage().get().getBlob(), w, h));
 	}
 	
 	private static ThumbnailImage createImage(Display display, byte[] data, int w, int h) throws IOException{
 		if(data == null){
 			return null;
 		}
 		ByteBuffer buf = ByteBuffer.wrap(data);
 		PDFFile pdffile = new PDFFile(buf);
 		if(pdffile.getNumPages() != 1){
 			throw new NotSinglePageException(
 					String.format("An image must be single page but has %d", pdffile.getNumPages()));
 		}
 		PDFPage page = pdffile.getPage(0);
 		return new ThumbnailImage(
 				getSWTImage(
 						display,
 						renderImage(page, w * PPG,
 								h * PPG)),
 				getSWTImage(
 					display,
 					renderImage(page, THUMB_WIDTH,
 							THUMB_HEIGHT)));
 	}
 
 	public static BufferedImage renderImage(final PDFPage page, final int w,
 			final int h) {
 	    return convertToBufferedImage(page.getImage(w, h, page.getBBox(), null, true, true));
 	}
 	
 	private static BufferedImage convertToBufferedImage(Image img){
 	    if(img.getClass() == BufferedImage.class){
 	        return (BufferedImage) img;
 	    }
 	    BufferedImage result = new BufferedImage(img.getWidth(observer), img.getHeight(observer), 
 	            BufferedImage.TYPE_4BYTE_ABGR);
 	    //Custom ImageObserver that loads the entire Image. That means drawImage is synchronous.
 	    result.getGraphics().drawImage(img, 0, 0, observer);
 	    return result;
 	}
 
 	public static org.eclipse.swt.graphics.Image getSWTImage(Display display,
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
 	    ColorModel colorModel = bufferedImage.getColorModel();
 
 	    PaletteData palette = new PaletteData(0x0000FF, 0x00FF00,0xFF0000);
 	    ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), 
 	    		colorModel.getPixelSize(), palette);
 	    int whitepixel = palette.getPixel(new RGB(255, 255, 255));
 	    data.transparentPixel = whitepixel;
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
