 package com.tenfood.api.resource;
 
 import com.tenfood.api.utils.Context;
 import org.testng.annotations.Test;
 
 import java.util.HashMap;
 
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
 
 /**
  * Created with IntelliJ IDEA.
  * User: hungadai
  * Date: 7/25/13
  * Time: 10:26 下午
  * To change this template use File | Settings | File Templates.
  */
 public class PhotoResourceTest {
     @Test
     public void testGetEmptyPhotos() throws Exception {
         PhotoResource resource = new PhotoResource();
         Context context = new Context();
         assertNull(resource.getPhotos(null, context));
         assertNull(resource.getPhotos(new HashMap<String, String[]>(), context));
     }
 }
