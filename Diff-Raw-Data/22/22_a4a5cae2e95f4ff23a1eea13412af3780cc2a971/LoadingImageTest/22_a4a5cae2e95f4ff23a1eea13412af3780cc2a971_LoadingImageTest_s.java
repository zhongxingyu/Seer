 package test;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 
 import org.junit.*;
 
 
 
 public class LoadingImageTest {
 
     @Test
     public void testImportNullImage() {
     	application.ImageLoader imageLoader = new application.ImageLoader();    	
     	gui.ApplicationWindow appw = new gui.ApplicationWindow();    	
    	assertEquals(imageLoader.ImportImageAction(appw), -1);
     	try{
     		imageLoader.importImages(appw);
     	}catch(Exception e){
     		assertTrue(true);
     	}
     }
 	
 }
