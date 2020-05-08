 package image.marvin;
 
 import image.ImageProcessor;
 import image.CannotProcessImageException;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import org.apache.commons.io.FilenameUtils;
 
 import marvin.image.MarvinImage;
 import marvin.image.MarvinImageMask;
 import marvin.io.MarvinImageIO;
 import marvin.plugin.MarvinImagePlugin;
 import marvin.util.MarvinPluginLoader;
 
 public class MarvinImageProcessor implements ImageProcessor {
 
 	@Override
 	public File resize(File image, int newWidth, int newHeight) {
 		MarvinImage targetImage = MarvinImageIO.loadImage(image.getPath());
 		MarvinImage orginalImage = targetImage.clone();
 		
 		int width = targetImage.getWidth();
 		int height = targetImage.getHeight();
 
 		double scaleRatio = calculateScaleRatio(width, height, newWidth, newHeight);
 		newWidth = (int)(width*scaleRatio);
 		newHeight = (int)(height*scaleRatio);
 		
 		targetImage.setDimension(newWidth, newHeight);
 		copyColors(targetImage, orginalImage, newWidth, newHeight, width, height);
 
 		targetImage.update();
         writeImage(targetImage, image);
         return image;
 	}
 
 	private void writeImage(MarvinImage source, File target) {
 		String extension = FilenameUtils.getExtension(target.getPath());
 		try {
 			ImageIO.write(source.getBufferedImage(), extension, target);
 		} catch (IOException e) {
 			throw new CannotProcessImageException();
 		}
 	}
 
 	private double calculateScaleRatio(int width, int height, int newWidth, int newHeight) {
 		if (width > height) {
 			return (double)newHeight/(double)height;
 		} else {
 			return (double)newWidth/(double)width;
 		}
 	}
 
 	private void copyColors(MarvinImage targetImage, MarvinImage orginalImage, int newWidth, int newHeight, int width, int height) {
 		int x_ratio = (int)((width<<16)/newWidth);
 	    int y_ratio = (int)((height<<16)/newHeight);
 	    int x2, y2 ;
 	    for (int i=0;i<newHeight;i++) {
 	        for (int j=0;j<newWidth;j++) {
 	            x2 = ((j*x_ratio)>>16) ;
 	            y2 = ((i*y_ratio)>>16) ;
 	            targetImage.setIntColor(j,i, orginalImage.getIntColor(x2,y2));
 	        }
 	    }
 	}
 
 	@Override
 	public File crop(File image, int newWidth, int newHeight) {
 		MarvinImage marvinImage = MarvinImageIO.loadImage(image.getPath());
 		int width = marvinImage.getWidth();
 		int height = marvinImage.getHeight();
 		
		int cropX = (width-newWidth)/2;
		int cropY = (height-newHeight)/2;
 		marvinImage = marvinImage.crop(cropX, cropY, newWidth, newHeight);
 		
 		writeImage(marvinImage, image);
 		return image;
 	}
 
 }
