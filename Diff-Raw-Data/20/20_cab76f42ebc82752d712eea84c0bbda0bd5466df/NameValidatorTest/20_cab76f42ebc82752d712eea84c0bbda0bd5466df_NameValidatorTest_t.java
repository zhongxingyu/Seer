 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.abf.utilities;
 
 import org.junit.Ignore;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 
 /**
  * DOCUMENT ME!
  *
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 public class NameValidatorTest {
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new NameValidatorTest object.
      */
     public NameValidatorTest() {
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     /**
      * DOCUMENT ME!
      */
     @Before
     public void setUp() {
     }
 
     /**
      * DOCUMENT ME!
      */
     @After
     public void tearDown() {
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private String getCurrentMethodName() {
         return new Throwable().getStackTrace()[1].getMethodName();
     }
 
    @Test
    public void testIsPackage(){
        final NameValidator val = new NameValidator(NameValidator.NAME_PACKAGE);
        assertTrue(val.isValid("jfioaekl"));
        assertTrue(val.isValid("fj____ekaljfaoei_fjoarijfioadjf_ioajeiojafioaefijroaubnasd_vjnx"));
        assertTrue(val.isValid("fdjaskldjfla.djfkaljdkflae"));
        assertTrue(val.isValid("fjdkasld.jfoiejdfkl.aj___kdlfjaekal.jadk_lfjkajlej.jfkd_lajfeklajfl"));
        assertTrue(val.isValid("fjd4532sld.jfoiejdfkl953.aj___kdlfjaekal.jadk_lfjkajlej.jfkd_lajfe75e6lajfl"));
        assertFalse(val.isValid("fjdkaäfejklfjd"));
        assertFalse(val.isValid("39jkdl"));
        assertFalse(val.isValid("fejkalsdf.9jfdklsa"));
    }

     /**
      * DOCUMENT ME!
      */
     @Test
     public void testIsValid_String() {
 //        assertTrue(NameValidator.isValid("ab", 8));
         testNameHighValidator();
         testSchemaHighValidator();
     }
 
     /**
      * DOCUMENT ME!
      */
     private void testNameHighValidator() {
         final NameValidator val = new NameValidator(NameValidator.NAME_HIGH);
         assertTrue(val.isValid("abcd9234"));
         assertTrue(val.isValid("Ackls9ewp_dal"));
         assertFalse(val.isValid("_dasjkl902"));
         assertFalse(val.isValid("9dascioas"));
         assertFalse(val.isValid("aDsdf924j5l-sdas"));
         assertFalse(val.isValid("aDsdf924j5l.sdas"));
     }
 
     /**
      * DOCUMENT ME!
      */
     private void testSchemaHighValidator() {
         final NameValidator val = new NameValidator(NameValidator.SCHEMA_HIGH);
         assertTrue(val.isValid("abcd9234"));
         assertTrue(val.isValid("Ackls9ewp_dal"));
         assertFalse(val.isValid("_dasjkl902"));
         assertFalse(val.isValid("9dascioas"));
         assertFalse(val.isValid("aDsdf924j5l-sdas"));
         assertTrue(val.isValid("aDsdf924j5l.adf"));
         assertFalse(val.isValid("aDs.df924j5l.sdas"));
         assertFalse(val.isValid("aDsdf924j5l.5sdas"));
     }
 
     // we don't need this test as it would test the very same stuff as the other method;
 // public void testIsValid_String_int()
 }
