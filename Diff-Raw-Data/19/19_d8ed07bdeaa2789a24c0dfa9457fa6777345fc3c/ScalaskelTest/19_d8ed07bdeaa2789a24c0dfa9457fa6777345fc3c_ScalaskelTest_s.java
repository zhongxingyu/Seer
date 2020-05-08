 package net.binout.codestory2013.scalaskel;
 
 import com.google.gson.Gson;
 import org.testng.Assert;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import java.util.List;
 
 public class ScalaskelTest {
 
     private Scalaskel scalaskel = new Scalaskel();
 
     @DataProvider
     public Object[][] response_scalaskel() {
         return new Object[][] {
                 new Object[] {1,  "[{\"foo\":1}]"},
                 new Object[] {7,  "[{\"foo\":7},{\"bar\":1}]"},
                 new Object[] {8,  "[{\"foo\":8},{\"foo\":1,\"bar\":1}]"},
                 new Object[] {14, "[{\"foo\":14},{\"foo\":7,\"bar\":1},{\"bar\":2},{\"foo\":3,\"qix\":1}]"},
                 new Object[] {15, "[{\"foo\":15},{\"foo\":8,\"bar\":1},{\"foo\":1,\"bar\":2},{\"foo\":4,\"qix\":1}]"},
         };
     }
 
     @Test(dataProvider = "response_scalaskel")
     public void compute_scalaskel_change(int number, String expected) {
         List<ScalaskelResult> result = scalaskel.change(number);
         String json = new Gson().toJson(result);
         Assert.assertEquals(json, expected);
     }
 
     @Test
     public void compute_scalaskel_change_for_100() {
         List<ScalaskelResult> result = scalaskel.change(100);
         String json = new Gson().toJson(result);
         Assert.assertNotNull(json);
     }
 }
