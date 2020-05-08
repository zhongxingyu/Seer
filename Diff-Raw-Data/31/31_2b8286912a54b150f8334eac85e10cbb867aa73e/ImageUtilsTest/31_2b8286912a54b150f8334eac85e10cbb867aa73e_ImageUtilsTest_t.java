 package mikera.gui;
 
import static org.junit.Assert.*;

 import java.awt.image.BufferedImage;
 
 import org.junit.Test;
 
 public class ImageUtilsTest {
 
 	@Test public void testResource() {
 		
 		BufferedImage b=ImageUtils.loadImage("yin-yang.png");
		assertEquals(64,b.getWidth());
 	}
 	
 	
 	public static void main(String... args) {
 		BufferedImage b=ImageUtils.loadImage("yin-yang.png");
 		ImageUtils.display(b);
 	}
 }
