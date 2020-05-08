 package edu.psu.sweng.kahindu.image.io;
 
 
import static org.junit.Assert.assertNull;
 
 import java.io.File;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.psu.sweng.kahindu.image.KahinduImage;
 
 public class PPMReaderTest
 {
     private KahinduImage image = null;
 
     @Before
     public void setUp() throws Exception
     {
         ImageReader reader = new AdvancedImageReader();
         image = reader.read(new File("gifs/baboon.ppm"));
     }
     
     @Test
     public void testImageReader()
     {
        assertNull("PPMReaderTest failed to load PPM Image", image);
     }
 }
