 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ejemploexcepciones;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author oquinter
  */
 public class CuentaTest {
     
     public CuentaTest() {
     }
     
     @BeforeClass
     public static void setUpClass() {
     }
     
     @AfterClass
     public static void tearDownClass() {
     }
     
     @Before
     public void setUp() {
     }
     
     @After
     public void tearDown() {
     }
     @Test
     public void saldoIgual() {
         Cuenta cuenta = new Cuenta("1","Activo",100f);
        assertEquals(cuenta.getSaldo(),new Float(100));        
     }
 }
