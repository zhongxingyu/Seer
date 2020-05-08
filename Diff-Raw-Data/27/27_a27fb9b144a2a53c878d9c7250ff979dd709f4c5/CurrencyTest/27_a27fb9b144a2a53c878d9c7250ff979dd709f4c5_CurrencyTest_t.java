 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package exchangeRate;
 
 import junit.framework.Assert;
 import org.junit.AfterClass;
 import org.junit.Before;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  *
  * @author ronald
  */
 public class CurrencyTest {
     
     public CurrencyTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
     
     @Before
     public void setUp() {
     }
 
     
     @Test
     public void testGetName() throws CurrencyException{
         String given = "USD";
         String expected = given;
         
         Currency currency = new Currency(given);
         String actual = currency.getName();
         
         Assert.assertEquals(expected, actual);
     }
     
     @Test(expected = CurrencyException.class)
     public void testEmptyConstruction() throws CurrencyException{
         String given = null;
         String expected = null;
         
         Currency currency = new Currency(given);
     }
     
     @Test(expected = CurrencyException.class)
     public void testTooShortConstruction() throws CurrencyException{
         String given = "AA";
         String expected = null;
         
         Currency currency = new Currency(given);
     }   
     
     @Test(expected = CurrencyException.class)
     public void testTooLongConstruction() throws CurrencyException{
         String given = "BBBBBB";
         String expected = null;
         
         Currency currency = new Currency(given);
     }    
     
     @Test(expected = CurrencyException.class)
     public void testNotACharStringConstruction() throws CurrencyException{
         String given = "AA1";
         String expected = null;
         
         Currency currency = new Currency(given);
    }  
    
        @Test
    public void testUpperCharConstruction() throws CurrencyException{
        String given = "aaa";
        String expected = "AAA";
        
        Currency currency = new Currency(given);
        String actual = currency.getName();
        
        Assert.assertEquals(expected, actual);
    } 
 }
