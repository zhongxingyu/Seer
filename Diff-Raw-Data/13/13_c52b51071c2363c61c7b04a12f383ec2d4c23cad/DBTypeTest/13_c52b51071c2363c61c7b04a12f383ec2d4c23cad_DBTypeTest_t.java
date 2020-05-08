 package timeSheet.database;
 
 import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import timeSheet.util.PaySystemProperties;
import timeSheet.util.PropertyName;
 
 import static junit.framework.Assert.assertTrue;
 
 /**
  * User: John Lawrence
  * Date: 3/13/11
  * Time: 8:39 PM
  */
@RunWith(PowerMockRunner.class)
@PrepareForTest(PaySystemProperties.class)
 public class DBTypeTest {
     @Test
     public void testGetOptions() throws Exception {
        PowerMockito.mockStatic(PaySystemProperties.class);
        Mockito.when(PaySystemProperties.getProperty(PropertyName.DB_TYPE)).thenReturn("H2");
         String options = DBType.getSelection();
         for (DBType type : DBType.values()) {
             assertTrue(options.contains(type.getDisplayName()));
         }
     }
 }
