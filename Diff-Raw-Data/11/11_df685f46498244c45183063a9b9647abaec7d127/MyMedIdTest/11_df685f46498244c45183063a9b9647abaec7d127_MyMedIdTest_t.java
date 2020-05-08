 /**
  *
  */
 package com.mymed.tests.unit.bean;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.mymed.controller.core.exception.WrongFormatException;
 import com.mymed.model.data.id.MyMedId;
 
 /**
  * Unit test for the {@link MyMedId} class, even if it is not a bean, we keep it
  * here.
  * 
  * @author Milo Casagrande
  * 
  */
 public class MyMedIdTest {
 
   private static MyMedId myMedId;
  private static MyMedId nullActual = null;
   private static String myMedIdString;
 
   private static final char TYPE = 'T';
   private static final String USER_ID = "USER_ID";
   private static final long TIME = System.currentTimeMillis();
 
   /**
    * @throws java.lang.Exception
    */
   @BeforeClass
  public static void setUpBeforeClass() {
     myMedId = new MyMedId(TYPE, TIME, USER_ID);
     myMedIdString = myMedId.toString();
   }
 
   /**
    * @throws java.lang.Exception
    */
   @AfterClass
  public static void tearDownAfterClass() {
     myMedId = null; // NOPMD
     myMedIdString = null; // NOPMD
   }
 
   @Test
   public void equalsTest() {
     final MyMedId actual = myMedId.clone();
     assertEquals("The objects are not the same!", myMedId, actual);
   }
 
   @Test
   public void notEqualsTest() {
     final MyMedId actual = new MyMedId('S', TIME, USER_ID);
     assertFalse("The objects are the same!", myMedId.equals(actual));
   }
 
   @Test
   public void nullEqualsTest() {
    assertFalse("The objects are the same!", myMedId.equals(nullActual)); // NOPMD
   }
 
   @Test
   public void hashCodeTest() {
     final MyMedId actual = new MyMedId('S', System.currentTimeMillis(), USER_ID);
     assertTrue("The hash codes are the same!", myMedId.hashCode() != actual.hashCode());
   }
 
   @Test
   public void parseMyMedId() {
     try {
       final MyMedId actual = MyMedId.parseString(myMedIdString);
       assertEquals("The objects are not the same!", myMedId, actual);
     } catch (final WrongFormatException ex) {
       fail(ex.getMessage());
     }
   }
 }
