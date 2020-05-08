 package test;
 
 import static org.junit.Assert.assertTrue;
 import java.sql.Date;
 import java.util.ArrayList;
 import org.junit.Test;
 import dal.admin.IImageStore;
 import dal.admin.Image;
 import dal.admin.StoreFactory;
 
 /**
  * 
  * An integration test for our database operations
  * @author Stian Sandve
  *
  */
 
 public class ImageStoreTest {
 	
 	IImageStore store = StoreFactory.getImageStore();
 	
 	@Test
 	public void insertTest() {
 		java.util.Date currentTime = new java.util.Date();
		Image img = new Image("insertUrl", 1234L, "insertTest", new Date(currentTime.getTime()));
 		assertTrue("A valid insert should return true", store.insert(img));
 	}
 	
 	@Test
 	public void getLastHundredImagesTest() {
 		ArrayList<Image> images = store.getLast(10);
 		assertTrue("Should return maximum 10 images", images.size() <= 10);
 	}
 }
