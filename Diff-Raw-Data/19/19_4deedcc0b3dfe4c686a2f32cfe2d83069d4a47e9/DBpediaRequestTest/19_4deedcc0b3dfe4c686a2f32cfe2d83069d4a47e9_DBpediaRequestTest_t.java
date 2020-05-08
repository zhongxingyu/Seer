 package extractor;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 public class DBpediaRequestTest {
     
     public DBpediaRequestTest() {
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
     
     @After
     public void tearDown() {
     }
 
     /**
      * Test of getCityNameFromURI method, of class DBpediaRequest.
      */
     @Test
     public void testGetCityNameFromURI_1() {
         System.out.println("getCityNameFromURI");
         String uri = "http://dbpedia.org/page/House_of_Lorraine";
         String expResult = "Maison de Lorraine";
         String result = DBpediaRequest.getCityNameFromURI(uri);
         assertEquals(expResult, result);
     }
     @Test
     public void testGetCityNameFromURI_2() {
         System.out.println("getCityNameFromURI");
         String uri = "http://dbpedia.org/page/Petit-Couronne";
         String expResult = "Grand-Couronne";
         String result = DBpediaRequest.getCityNameFromURI(uri);
         assertEquals(expResult, result);
     }
     @Test
     public void testGetCityNameFromURI_3() {
         System.out.println("getCityNameFromURI");
         String uri = "http://www.dbpedia.org/resource/Pau,_Pyr%C3%A9n%C3%A9es-Atlantiques";
         String expResult = "Pau";
         String result = DBpediaRequest.getCityNameFromURI(uri);
         assertEquals(expResult, result);
     }
     @Test
     public void testGetCityNameFromURI_4() {
         System.out.println("getCityNameFromURI");
         String uri = "http://www.dbpedia.org/resource/Pau";
         String expResult = "Pau";
         String result = DBpediaRequest.getCityNameFromURI(uri);
         assertEquals(expResult, result);
     }
     @Test
     public void testGetCityNameFromURI_5() {
         System.out.println("getCityNameFromURI");
         String uri = "http://dbpedia.org/page/Pau,_Pyr%C3%A9n%C3%A9es-Atlantiques";
         String expResult = "Pau";
         String result = DBpediaRequest.getCityNameFromURI(uri);
         assertEquals(expResult, result);
     }
     
     @Test
     public void testGetCityNameFromURI_6() {
         System.out.println("getCityNameFromURI");
         String uri = "http://www.dbpedia.org/resource/Antwerp";
        String expResult = "Anvers";
         String result = DBpediaRequest.getCityNameFromURI(uri);
         assertEquals(expResult, result);
     }
 }
