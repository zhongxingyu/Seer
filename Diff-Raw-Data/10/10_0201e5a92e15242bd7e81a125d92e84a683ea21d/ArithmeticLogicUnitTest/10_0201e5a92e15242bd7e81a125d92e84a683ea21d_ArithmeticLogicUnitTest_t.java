 package cactus;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 
 
 public class ArithmeticLogicUnitTest {
     
     public ArithmeticLogicUnitTest() {
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
      * Test of add method, of class ArithmeticLogicUnit.
      */
     @Test
     public void testAdd() {
         System.out.println("add");
        int a = 3;
        int b = 4;
         ArithmeticLogicUnit instance = new ArithmeticLogicUnit();
        int expResult = 7;
         int result = instance.add(a, b);
         assertEquals(expResult, result);
     }
 }
