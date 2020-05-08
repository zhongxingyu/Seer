 package org.sukrupa.platform.web;
 
 
 import org.junit.Test;
 import org.sukrupa.app.services.FileFactory;
 import org.sukrupa.app.services.ImageLoaderService;
 import org.sukrupa.app.students.StudentImageView;
 import org.sukrupa.platform.config.AppConfiguration;
 
 import javax.swing.text.html.ImageView;
 
import java.io.File;
 import java.io.FileNotFoundException;
 
 import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
 
 public class ImageControllerTest {
 
     private ImageController imageController;
     private String imageRepositoryLocation;
    private FileFactory imageFactory;
 
     public ImageControllerTest() {
         imageRepositoryLocation = new AppConfiguration().properties().getProperty("app.image.dir");
        imageFactory = mock(FileFactory.class);
        this.imageController = new ImageController(new ImageLoaderService(imageFactory,imageRepositoryLocation));
     }
 
     @Test
     public void testRequestForAnImageMustReturnAnImageView() throws FileNotFoundException {
        File mockImage = mock(File.class);
        when(imageFactory.create("image")).thenReturn(mockImage);
         assertTrue(imageController.getImage("image") instanceof StudentImageView);
     }
 }
