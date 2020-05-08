 package com.tenfood.api.resource;
 
 import com.tenfood.api.utils.Context;
 import org.testng.annotations.Test;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import static org.testng.Assert.*;
 
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
 
     @Test
     public void testGetPhotos() throws Exception {
         PhotoResource resource = new PhotoResource();
         Context context = new Context();
         Map<String, String[]> queryMap = new HashMap<String, String[]>();
         queryMap.put("text", (new String[0]));
         assertNull(resource.getPhotos(queryMap, context));
 
         String[] text = new String[1];
        text[0] = "";
         queryMap.put("text", text);
         assertNotNull(resource.getPhotos(queryMap, context));
     }

    @Test
    public void testGetPhotosWithOtherParams() throws Exception {
        PhotoResource resource = new PhotoResource();
        Context context = new Context();
        Map<String, String[]> queryMap = new HashMap<String, String[]>();

        String[] text = new String[1];
        text[0] = "";
        queryMap.put("a", text);
        assertNull(resource.getPhotos(queryMap, context));
    }
 }
