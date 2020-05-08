 package edu.psu.sweng.kahindu.image.io;
 
 
 import java.io.File;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 
 import edu.psu.sweng.kahindu.image.KahinduImage;
 
 public class JPGReaderTest
 {
     private KahinduImage image = null;
 
     @Before
     public void setUp() throws Exception
     {
         ImageReader reader = new DefaultImageReader();
         image = reader.read(new File("gifs/baboon.jpg"));
     }
     
     @Test
     public void testImageReader()
     {
        assertNull("JPGReaderTest failed to load JPG Image", image);
     }
 }
