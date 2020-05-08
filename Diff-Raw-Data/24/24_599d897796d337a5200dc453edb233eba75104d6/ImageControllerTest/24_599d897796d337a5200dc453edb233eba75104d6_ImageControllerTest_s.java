 package org.sukrupa.platform.web;
 
 
 import org.junit.Test;
 import org.sukrupa.app.services.FileFactory;
 import org.sukrupa.app.services.ImageLoaderService;
 import org.sukrupa.app.students.StudentImageView;
 import org.sukrupa.platform.config.AppConfiguration;
 
 import javax.swing.text.html.ImageView;
 
 import java.io.FileNotFoundException;
 
 import static org.junit.Assert.assertTrue;
 
 public class ImageControllerTest {
 
     private ImageController imageController;
     private String imageRepositoryLocation;
 
     public ImageControllerTest() {
         imageRepositoryLocation = new AppConfiguration().properties().getProperty("app.image.dir");
        this.imageController = new ImageController(new ImageLoaderService(new FileFactory(),imageRepositoryLocation));
     }
 
     @Test
     public void testRequestForAnImageMustReturnAnImageView() throws FileNotFoundException {
         assertTrue(imageController.getImage("image") instanceof StudentImageView);
     }
 }
