 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package repository;
 
 import java.util.ArrayList;
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
        ArrayList<String> expResult = new ArrayList<String>();
        ArrayList result = instance.getImageList();
         if(result.size()>0){
            String firstElement = result.get(0).toString();
             assertTrue(firstElement.contains("http"));
             assertTrue(firstElement.contains(".jpg"));
         }else{
             String element = result.toString();
             assertTrue(element.equals("[]"));
         }
     }
}
