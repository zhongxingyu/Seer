 package providers;
 
 import org.junit.Before;
 import org.junit.Test;
 import com.ece.superkids.ui.providers.ImageProvider;
 import com.ece.superkids.ui.providers.ResourceProviderFactory;
 
 import javax.swing.*;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Properties;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 import static junit.framework.Assert.assertTrue;
 
 public class FilePathImageProviderTests {
 
     private static final String KNOWN_KEY = "circle";
     private static final String KNOWN_PATH = "/question/pictures/circle.png";
     private static final String PATH_FILE = "/providers/image_paths.properties";
 
     private ImageProvider imageProvider;
 
     @Before
     public void setup() {
         imageProvider = ResourceProviderFactory.anImageProvider();
     }
 
     @Test
     public void providerCanRetrieveCorrectImages() {
         ImageIcon icon =  imageProvider.getImage(KNOWN_KEY);
         ImageIcon expected = new ImageIcon(getClass().getResource(KNOWN_PATH));
 
         BufferedImage expectedImage = convertToBufferedImage(expected);
         BufferedImage actualImage = convertToBufferedImage(icon);
 
         assertImagesAreIdentical(expectedImage, actualImage);
     }
 
     @Test
     public void allImagesExist() throws IOException {
         Properties props = new Properties();
         props.load(getClass().getResourceAsStream(PATH_FILE));
         List<String> keys = imageProvider.getAllKeys();
 
 
         for (String key : keys) {
             File image = null;
             String path = props.getProperty(key);
            if (path == null) continue;
             try{
                 image = new File(getClass().getResource(path).getFile());
             } catch (NullPointerException e) {
                 throw new NullPointerException(key + " image is missing");
             }

             assertNotNull(image);
            assertTrue(image.getAbsolutePath().contains(path));
             assertTrue(image.exists());
         }
     }
 
     private void assertImagesAreIdentical(final BufferedImage expectedImage, final BufferedImage actualImage) {
         assertEquals(expectedImage.getHeight(), actualImage.getHeight());
         assertEquals(expectedImage.getWidth(), actualImage.getWidth());
         for (int h = 0 ; h < expectedImage.getHeight(); h++) {
             for (int w = 0; w < expectedImage.getWidth(); w++) {
                 assertEquals("Pixel w:" + w + " h:" + h + " do not match", expectedImage.getRGB(w, h),
                         actualImage.getRGB(w, h));
             }
         }
     }
 
     private BufferedImage convertToBufferedImage(final ImageIcon expected) {
         BufferedImage expectedImage = new BufferedImage(expected.getIconWidth(), expected.getIconHeight(),
                 BufferedImage.TYPE_INT_RGB);
         expectedImage.getGraphics().drawImage(expected.getImage(), 0 ,0 , null);
         return expectedImage;
     }
 }
