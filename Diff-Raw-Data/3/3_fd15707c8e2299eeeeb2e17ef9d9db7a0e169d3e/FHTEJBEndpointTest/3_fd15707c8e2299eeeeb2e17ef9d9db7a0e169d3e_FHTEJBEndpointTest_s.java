 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.imagejdev.beans;
 
 import com.caucho.hessian.client.HessianProxyFactory;
 import java.net.MalformedURLException;
 import org.imagejdev.api.FHTEJBService;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  * @author rick
  */
 public class FHTEJBEndpointTest {
 
     private FHTEJBService fhtejbservice;
 
     @Before
     public void initProxy() throws MalformedURLException {
         String url = "http://144.92.92.76:8080/EJBHessianFHT/FHTEJBService";
         HessianProxyFactory factory = new HessianProxyFactory();
         this.fhtejbservice = (FHTEJBService) factory.create(FHTEJBService.class,url);
         assertNotNull(fhtejbservice);
     }
 
     @Test
     public void testProfile() {
         long time = System.currentTimeMillis();
 
         float[][] expResult = new float[512][6];  //33MB
 
         for(int i = 0; i < 100; i++)
         {
             float[][] result = this.fhtejbservice.getProfileData( expResult );
             assertArrayEquals( expResult, result );
             System.out.println(i + " Time is " + (System.currentTimeMillis() - time) );
         }
     }
     /**
      * Test of fht method, of class FHTEJBEndpoint.
      */
     @Test
     public void testFht() {
         System.out.println("fht");
         int width = 8;
         int height = 8;
         int depth = 8;
         float[][] data = {  {0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f},
                             {0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f},
                             {0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f},
                             {0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f},
                             {0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f},
                             {0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f},
                             {0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f},
                             {0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f},
         };
         boolean inverse = true;
        
         float[][] expResult = {  {0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f},
                             {0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f},
                             {0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f},
                             {0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f},
                             {0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f},
                             {0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f},
                             {0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f},
                             {0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f,0.0f,0.1f},
         };
         
             long time = System.currentTimeMillis();
          for(int i = 0; i < 100; i++)
         {
             System.out.println(i + " FHT, mSec: " + (System.currentTimeMillis() - time) );
             float[][] result = this.fhtejbservice.fht(width, height, depth, data, inverse);
         }
         //assertArrayEquals(expResult, result);
     }
}
