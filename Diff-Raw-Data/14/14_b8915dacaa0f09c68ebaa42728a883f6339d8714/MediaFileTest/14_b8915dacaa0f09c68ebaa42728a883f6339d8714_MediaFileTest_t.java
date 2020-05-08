 package es.jacascom.hmi.data;
 
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: fcastro
  * Date: 2/22/12
  * Time: 8:18 AM
  * To change this template use File | Settings | File Templates.
  */
 public class MediaFileTest {
 
     private MediaFile mediaFile;
 
     @Before
     public void setUp() throws Exception {
         mediaFile = new MediaFile("1111", "FileName", "Path", "Title", 1024);
     }
 
     @Test
     public void testEqualsSameCRC() throws Exception {
         MediaFile other = new MediaFile("1111", "other", "other", "other", 0);
 
         assertEquals("Same CRC but equals fails", mediaFile, other);
     }
 
     @Test
     public void testEqualsDifferentCRC() throws Exception {
         MediaFile other = new MediaFile("2222", "FileName", "Path", "Title", 1024);
 
         assertFalse("Different CRC but equals fails", mediaFile.equals(other));
     }
 
     @Test
     public void testEqualsWithNull() throws Exception {
         assertFalse("equals with null fails", mediaFile.equals(null));
     }
 
     @Test
     public void testEqualsWithNullInternalCRC() throws Exception {
         mediaFile.setCrc(null);
         MediaFile other = new MediaFile("1111", "FileName", "Path", "Title", 1024);
         assertFalse("equals with null CRC fails", mediaFile.equals(null));
     }
 
     @Test
     public void testEqualsWithNullCRC() throws Exception {
         MediaFile other = new MediaFile(null, "FileName", "Path", "Title", 1024);
         assertFalse("equals with null CRC fails", mediaFile.equals(null));
     }
 
     @Test
     public void testEqualsWithBothCRCNull() throws Exception {
         MediaFile other = new MediaFile(null, "FileName", "Path", "Title", 0);
         mediaFile.setCrc(null);
 
         assertFalse("equals with null CRCs fails", mediaFile.equals(null));
     }
 }
