 package ch.nomoresecrets.mediastopf.client.utils;
 
 import static org.junit.Assert.assertEquals;
 
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class RotateImageTest {
 	
 	private BufferedImage image;
 
 	@Before
 	public void setUp() throws Exception {
 	    int width = 100;
 	    int height = 200;
 	    int[] data = new int[width * height];
 	    int i = 0;
 	    for (int y = 0; y < height; y++) {
 	      int red = (y * 255) / (height - 1);
 	      for (int x = 0; x < width; x++) {
 	        int green = (x * 255) / (width - 1);
 	        int blue = 128;
 	        data[i++] = (red << 16) | (green << 8) | blue;
 	      }
 	    }
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
 		image.setRGB(0, 0, width, height, data, 0, width);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		image.flush();
 	}
 
 	@Test
 	public void testRotateByAngle() throws IOException {
 		BufferedImage rotImage = RotateImage.rotate(image, 0);
 		assertEquals("Width", rotImage.getWidth(), 100);
 		assertEquals("Height", rotImage.getHeight(), 200);
 		
 		rotImage = RotateImage.rotate(image, 45);
 		assertEquals("Width", rotImage.getWidth(), 213);
 		assertEquals("Height", rotImage.getHeight(), 213);
 		
 		rotImage = RotateImage.rotate(image, 90);
 		assertEquals("Width", rotImage.getWidth(), 200);
 		assertEquals("Height", rotImage.getHeight(), 100);
 		
 		rotImage = RotateImage.rotate(image, 135);
 		assertEquals("Width", rotImage.getWidth(), 213);
 		assertEquals("Height", rotImage.getHeight(), 213);
 		
 		rotImage = RotateImage.rotate(image, 180);
 		assertEquals("Width", rotImage.getWidth(), 100);
 		assertEquals("Height", rotImage.getHeight(), 200);
 		
 		rotImage = RotateImage.rotate(image, 225);
 		assertEquals("Width", rotImage.getWidth(), 213);
 		assertEquals("Height", rotImage.getHeight(), 213);
 		
 		rotImage = RotateImage.rotate(image, 270);
 		assertEquals("Width", rotImage.getWidth(), 200);
 		assertEquals("Height", rotImage.getHeight(), 100);
 		
 		rotImage = RotateImage.rotate(image, 315);
 		assertEquals("Width", rotImage.getWidth(), 213);
 		assertEquals("Height", rotImage.getHeight(), 213);
 		
 		rotImage = RotateImage.rotate(image, 360);
 		assertEquals("Width", rotImage.getWidth(), 100);
 		assertEquals("Height", rotImage.getHeight(), 200);
 	}
 }
