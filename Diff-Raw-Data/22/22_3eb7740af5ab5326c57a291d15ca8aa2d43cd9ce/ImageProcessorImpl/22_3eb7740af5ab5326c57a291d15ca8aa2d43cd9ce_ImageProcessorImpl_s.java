 package edu.uib.info323.image;
 
 import java.awt.image.WritableRaster;
 import java.io.InputStream;
 import java.net.URL;
 import java.security.InvalidParameterException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.annotation.Scope;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.context.support.FileSystemXmlApplicationContext;
 import org.springframework.core.io.FileSystemResource;
 import org.springframework.stereotype.Component;
 import org.springframework.test.context.ContextConfiguration;
 
 import edu.uib.info323.model.ColorFreq;
 import edu.uib.info323.model.ColorFreqFactory;
 import edu.uib.info323.model.Image;
 import edu.uib.info323.model.ImageImpl;
 
 @Scope("prototype")
 @Component
 public class ImageProcessorImpl implements ImageProcessor {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(ImageProcessorImpl.class);
 	
 	@Autowired
 	private CompressedColorFactory colorFactory;
 	
 	@Autowired
 	private ColorFreqFactory freqFactory;
 	
 	private int imageHeight;
 	private int imageWidth;
 	
 	private Integer numberOfPixels;
 	private Image image;
 	private int threshold = 5;
 	
	private WritableRaster raster;
 
 	/* (non-Javadoc)
 	 * @see edu.uib.info323.image.ImageProcessor#readColors()
 	 */
 	private HashMap<CompressedColor, Integer> getColorFrequencies() {
 		HashMap<CompressedColor, Integer> colorFreq = new HashMap<CompressedColor, Integer>();
 
		for(int x = raster.getMinX(); x < this.imageWidth; x++){
			for(int y = raster.getMinY(); y < this.imageHeight; y++){
				int[] pixel = raster.getPixel(x, y, new int[4]);
				CompressedColor color = colorFactory.createCompressedColor(pixel[0], pixel[1], pixel[2]);
 				Integer count = colorFreq.containsKey(color) ? colorFreq.get(color) : 0;
 				colorFreq.put(color,++count);
 			}
 		}
 		return colorFreq;
 	}
 
 
 
 	/**
 	 * Sets the compressionrate of the RGB color space.
 	 * Accepted values are � 4
 	 * @param imageCompressionRate
 	 */
 	public void setImageCompression(int imageCompressionRate) {
 		if(imageCompressionRate >= 4) {
 			colorFactory.setDefaultCompression(imageCompressionRate);
 		}
 		else {
 			throw new InvalidParameterException("Compression rate must be �4 but was " + imageCompressionRate );
 		}
 	}
 
 	/**
 	 * Sets the compressionrate of the RGB color space.
 	 * Accepted values are � 4
 	 * @param imageCompressionRate
 	 */
 	public int getImageCompression() {
 		return colorFactory.getDefaultCompression();
 	}
 
 
 
 	public CompressedColorFactory getColorFactory() {
 		return colorFactory;
 	}
 
 
 
 	public void setColorFactory(CompressedColorFactory colorFactory) {
 		this.colorFactory = colorFactory;
 	}
 
 
 
 	public void setImage(Image image) {
 		try {
 			this.image = image;
 			InputStream inputStream = new URL(image.getImageUri()).openStream();
			raster = ImageIO.read(inputStream).getRaster();
			this.imageHeight = raster.getHeight();
			this.imageWidth = raster.getWidth();
 			this.numberOfPixels = this.imageHeight * this.imageWidth;
 		}catch (Exception e) {
 			throw new InvalidParameterException("Could not open stream for image " + image);
 		}
 	
 	}
 
 
 
 	public ArrayList<ColorFreq> getColorFreqs() {
 		Map<CompressedColor, Integer> colorMap = getColorFrequencies();
 		ArrayList<ColorFreq> colorFreqs = new ArrayList<ColorFreq>();
 		for(CompressedColor color : colorMap.keySet()) {
 			Integer freq = colorMap.get(color);
 			int relativeFreq = (int) ((freq.doubleValue() / numberOfPixels.doubleValue()) * 100);
 			if(relativeFreq > threshold) {
 				if(relativeFreq > 100) {
 					LOGGER.error("Relative freq > 100 for color in " + image.getImageUri());
 				}
 				colorFreqs.add(freqFactory.createColorFreq(image, color.getColor(), relativeFreq));
 			}
 
 		}
 		return colorFreqs;
 	}
 
 	
 	public static void main(String... args) {
 	    ApplicationContext context = new FileSystemXmlApplicationContext(new String[] {"src/main/webapp/WEB-INF/spring/app/model-context.xml", "src/main/webapp/WEB-INF/spring/app/db-context.xml"});   
 		ImageProcessor imageProcessor = context.getBean(ImageProcessor.class);
 		Image image = new ImageImpl("https://lh6.googleusercontent.com/-1fCBWsC9btk/AAAAAAAAAAI/AAAAAAAAAAA/G8aMBLX2ie0/s24-c/photo.jpg", Arrays.asList(new String[] {"http://pritisprettyblog.wordpress.com/2012/01/08/ying-yang/"}));
 		imageProcessor.setImage(image);
 		imageProcessor.setColorFactory(new CompressedColorFactoryImpl());
 		List<ColorFreq> colors = imageProcessor.getColorFreqs();
 		for(ColorFreq color : colors) {
 			System.out.println(color);
 		}
 	}
 
 
 
 	public int getImageHeight() {
 		return this.imageHeight;
 	}
 
 
 
 	public int getImageWidth() {
 		return this.imageWidth;
 	}
 }
