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
        int a = 0;
        int b = 0;
         ArithmeticLogicUnit instance = new ArithmeticLogicUnit();
        int expResult = 0;
         int result = instance.add(a, b);
         assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
     }
 }
