 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package internet;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Fedor Uvarychev
  */
 public class ComputerTest {
     
     public ComputerTest() {
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
      * Test of infestation method, of class Computer.
      */
     @Test
     public void testInfestation() {
         System.out.println("infestation");
         inst = new Computer(0, 2, 0);
         Randomizer rand = new Randomizer() {
            @Override
             public int nextInt(int bound) {
                 return 3;
             }
         };
         inst.infestation(rand);
         assertEquals(true, inst.getState());
     }
     private Computer inst;
 }
