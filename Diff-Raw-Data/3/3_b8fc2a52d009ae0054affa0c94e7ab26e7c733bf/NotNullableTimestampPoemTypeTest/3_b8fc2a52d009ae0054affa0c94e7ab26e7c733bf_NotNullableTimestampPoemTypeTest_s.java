 /**
  * 
  */
 package org.melati.poem.test;
 
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.text.DateFormat;
 
 import org.melati.poem.ParsingPoemException;
 import org.melati.poem.PoemLocale;
 import org.melati.poem.SQLPoemType;
 import org.melati.poem.TimestampPoemType;
 
 /**
  * @author timp
  * @since 21 Dec 2006
  *
  */
 public class NotNullableTimestampPoemTypeTest extends SQLPoemTypeSpec {
 
   /**
    * 
    */
   public NotNullableTimestampPoemTypeTest() {
   }
 
   /**
    * @param name
    */
   public NotNullableTimestampPoemTypeTest(String name) {
     super(name);
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.test.SQLPoemTypeSpec#setObjectUnderTest()
    */
   void setObjectUnderTest() {
     it = new TimestampPoemType(false);
   }
 
   public void testStringOfCooked() {
     super.testStringOfCooked();
     long now = System.currentTimeMillis();
     Timestamp nowStamp = new Timestamp(now);
    // Hmm not a real test
    //assertTrue(it.stringOfCooked(nowStamp, PoemLocale.HERE, DateFormat.MEDIUM) instanceof String);
   }
 
   /**
    * Test method for {@link org.melati.poem.SQLType#quotedRaw(java.lang.Object)}.
    */
   public void testQuotedRaw() {
     long now = System.currentTimeMillis();
     Timestamp nowStamp = new Timestamp(now);
     assertEquals("'" + TimestampPoemType.format.format(nowStamp) + "'", 
         ((SQLPoemType)it).quotedRaw(nowStamp));
 
   }
 
   public void testAssertValidCooked() {
     super.testAssertValidCooked();
   }
 
   public void testPossibleRaws() {
     super.testPossibleRaws();
     
   }
   public void testRawOfString() {
     super.testRawOfString();
     try{
       it.rawOfString("kk");
       fail("Should have blown up");
     } catch (ParsingPoemException e) {
       e = null;
     }
     
   }
 
   public void testRawOfCooked() {
     super.testRawOfCooked();
   }
 
   /**
    * Test the full constructor. 
    */
   public void testFullConstructor() {
     TimestampPoemType it2 = new MyTimestampPoemType(it.getNullable());
     assertEquals(it.getNullable(),it2.getNullable());
   }
   class MyTimestampPoemType extends TimestampPoemType {
 
     /**
      * @param sqlTypeCode
      * @param sqlTypeName
      * @param nullable
      */
     public MyTimestampPoemType(boolean nullable) {
       super(Types.TIMESTAMP, "TIMESTAMP", nullable);
     }
     
   }
 }
