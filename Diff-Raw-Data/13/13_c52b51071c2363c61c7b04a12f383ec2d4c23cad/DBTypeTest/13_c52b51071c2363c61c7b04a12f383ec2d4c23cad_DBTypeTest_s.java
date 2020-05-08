 package timeSheet.database;
 
 import org.junit.Test;
 
 import static junit.framework.Assert.assertTrue;
 
 /**
  * User: John Lawrence
  * Date: 3/13/11
  * Time: 8:39 PM
  */
 public class DBTypeTest {
     @Test
     public void testGetOptions() throws Exception {
         String options = DBType.getSelection();
         for (DBType type : DBType.values()) {
             assertTrue(options.contains(type.getDisplayName()));
         }
     }
 }
