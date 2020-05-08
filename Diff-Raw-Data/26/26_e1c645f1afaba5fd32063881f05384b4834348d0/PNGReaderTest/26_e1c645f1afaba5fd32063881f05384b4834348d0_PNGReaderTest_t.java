 package edu.psu.sweng.kahindu.image.io;
 
 
 import java.io.File;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 
 import edu.psu.sweng.kahindu.image.KahinduImage;
 
 public class PNGReaderTest
 {
     private KahinduImage image = null;
 
     @Before
     public void setUp() throws Exception
     {
         ImageReader reader = new DefaultImageReader();
         image = reader.read(new File("gifs/baboon.png"));
     }
     
     @Test
     public void testImageReader()
     {
        assertNotNull("PNGReaderTest failed to load PNG Image", image);
     }
 }
