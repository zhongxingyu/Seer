 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package uk.ac.ebi.metabolomes.util.inchi;
 
 import java.util.ArrayList;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import org.openscience.cdk.interfaces.IAtomContainer;
 import uk.ac.ebi.metabolomes.webservices.ChEBIWebServiceConnection;
 
 /**
  *
  * @author pmoreno
  */
 public class InChIProducerBinary102betaTest {
 
     public InChIProducerBinary102betaTest() {
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
      * Test of checkBinaries method, of class InChIProducerBinary102beta.
      */
     @Test
     public void testCheckBinaries() {
         System.out.println("checkBinaries");
         InChIProducerBinary102beta instance = new InChIProducerBinary102beta();
         boolean expResult = true;
         boolean result = instance.checkBinaries();
         assertEquals(expResult, result);
     }
 
     /**
      * Test of calculateInChI method, of class InChIProducerBinary102beta.
      */
     @Test
     public void testCalculateInChI() {
        System.out.println("calculateInChI");
        ChEBIWebServiceConnection cwsc = new ChEBIWebServiceConnection();
        String[] ids = new String[] {"195280"};
        ArrayList<IAtomContainer> mols = cwsc.downloadMolsToCDKObject(ids);
        IAtomContainer mol = mols.get(0);
        InChIProducerBinary102beta instance = new InChIProducerBinary102beta();
        InChIResult result = instance.calculateInChI(mol);
        assertNotNull( result);
        System.out.println("InChI:"+result.getInchi());
        InChIResult expected = new InChIResult();
        expected.setInchi("InChI=1/C16H26O5/c1-9-5-6-12-10(2)13(17-4)18-14-16(12)11(9)7-8-15(3,19-14)20-21-16/h9-14H,5-8H2,1-4H3/t9-,10-,11+,12+,13+,14-,15-,16-/m1/s1");
        expected.setInchiKey("InChIKey=SXYIRMFQILZOAM-HVNFFKDJBA");
        System.out.println( "WARNING TEST SKIPPED!" );
         //assertEquals(result.getInchi(), expected.getInchi());
         //assertEquals(result.getInchiKey(), expected.getInchiKey());
     }
 
 }
