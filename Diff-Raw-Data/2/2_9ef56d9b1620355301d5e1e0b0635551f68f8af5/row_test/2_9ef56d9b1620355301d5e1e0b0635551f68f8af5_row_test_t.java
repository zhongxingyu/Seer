 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import java.util.ArrayList;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import org.junit.*;
 
 /**
  *
  * @author david
  */
 public class row_test {
 
     double test_row[] = { 2, 4, 4.5, 3};
     double row_length = 13.5;
     int array_length = 4;
     row test_object;
     row test_object2;
     
     public row_test() {}
     
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
     
     @Before
     public void setUp() {
         test_object = new row(test_row,row_length,array_length);
         test_object2 = new row(test_row,row_length,array_length+1);
     }
     
     @After
     public void tearDown() {
     }
     // TODO add test methods here.
     // The methods must be annotated with annotation @Test. For example:
     //
     // @Test
     // public void hello() {}
     
     @Test
     public void test_get_row(){
         double expected[] = test_row;
         double result [] = test_object.get_row();
         
         for (int index =0; index < array_length; index++)
             assertEquals(expected[index], result[index], .00001);
         
         
         expected = test_row;
         result = test_object2.get_row();
         
         for (int index =0; index < array_length; index++){
             assertEquals(expected[index], result[index], .00001);
             System.out.print(result[index]);
         }
         
         
         
         
     }
     @Test
     public void test_get_row_size(){
         String input = null;
         double expected = row_length;
         double result = test_object.get_row_size();
         
         assertEquals(expected, result, .0001);        
     }
     
     @Test
     public void test_get_row_length(){
         String input = null;
         double expected = row_length;
        double result = test_object.get_row_size();
         
         assertEquals(expected, result, .0001);
     }
     
     @Test
     public void test_get_hash(){
         String input = null;
         double expected[] = {  2, 6, 10.5, 13.5 };
         ArrayList result = test_object.get_hash();
         
         // TODO: Create Hash Test
         for(int index = 0; index < array_length; index++){
             assertTrue(result.contains(new Double(expected[index])));
         }
     }
 }
