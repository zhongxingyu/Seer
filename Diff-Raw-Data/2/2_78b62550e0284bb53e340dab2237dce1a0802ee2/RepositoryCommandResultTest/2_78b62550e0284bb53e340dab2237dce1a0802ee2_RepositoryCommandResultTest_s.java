package au.com.dius.resilience.test.unit.persistence.test.unit.repository.impl;
 
 import static junit.framework.Assert.assertFalse;
 import static junit.framework.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 
 import org.junit.Test;
 
 import au.com.dius.resilience.persistence.repository.RepositoryCommandResult;
 
 /**
  * @author georgepapas
  */
 public class RepositoryCommandResultTest {
 
   @Test
   public void hasResultsShouldReturnFalseWhenNoZeroResults() {
     assertFalse(new RepositoryCommandResult<String>(false, Collections.EMPTY_LIST).hasResults());
   }
 
   @Test
   public void hasResultsShouldReturnFalseWhenNull() {
     assertFalse(new RepositoryCommandResult<String>(false, new ArrayList<String>()).hasResults());
   }
 
   @Test
   public void hasResultsShouldReturnTrue() {
     assertTrue(new RepositoryCommandResult<String>(false, Arrays.asList(new String[]{"foo"})).hasResults());
   }
 }
