 package mikera.gui;
 
 import java.awt.image.BufferedImage;
 
 import org.junit.Test;
 
 public class ImageUtilsTest {
 
 	@Test public void testResource() {
 		
 		BufferedImage b=ImageUtils.loadImage("yin-yang.png");
 	}
 	
 	
 	public static void main(String... args) {
 		BufferedImage b=ImageUtils.loadImage("yin-yang.png");
 		ImageUtils.display(b);
 	}
 }
