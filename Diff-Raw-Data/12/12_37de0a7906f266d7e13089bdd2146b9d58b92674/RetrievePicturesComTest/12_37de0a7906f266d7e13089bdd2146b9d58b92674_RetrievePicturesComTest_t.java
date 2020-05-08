 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package repository;
 
 import java.util.ArrayList;
import model.Picture;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Emil
  */
 public class RetrievePicturesComTest {
     /**
      * Test of getImageList method, of class RetrievePicturesCom.
      */
     @Test
     public void testGetImageList() throws Exception {
         System.out.println("getImageList");
         RetrievePicturesCom instance = new RetrievePicturesCom();
        ArrayList<Picture> result = instance.getImageList();
         if(result.size()>0){
            String firstElement = result.get(0).getLargeUrl();
             assertTrue(firstElement.contains("http"));
             assertTrue(firstElement.contains(".jpg"));
         }else{
             String element = result.toString();
             assertTrue(element.equals("[]"));
         }
     }
}
