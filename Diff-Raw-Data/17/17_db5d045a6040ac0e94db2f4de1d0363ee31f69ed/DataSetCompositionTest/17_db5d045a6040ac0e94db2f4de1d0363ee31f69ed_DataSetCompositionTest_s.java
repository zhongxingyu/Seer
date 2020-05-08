 package com.seitenbau.testing.dbunit;
 
 import static com.seitenbau.testing.dbunit.PersonDatabaseRefs.SAT;
 import static org.fest.assertions.Assertions.assertThat;
 
 import org.dbunit.dataset.DataSetException;
 import org.fest.assertions.Fail;
 import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;
 
 import com.seitenbau.testing.dbunit.dataset.DemoGroovyDataSet;
 import com.seitenbau.testing.dbunit.dataset.ExtendedDemoGroovyDataSet;
 import com.seitenbau.testing.dbunit.dsl.DataSetRegistry;
 
 public class DataSetCompositionTest
 {
 
   @Test
   public void checkExtendedDemoGroovyDataSet() throws DataSetException
   {
     ExtendedDemoGroovyDataSet dataSet = new ExtendedDemoGroovyDataSet();
     assertThat(dataSet.teamsTable.getRowCount()).isEqualTo(2);
     assertThat(dataSet.jobsTable.getRowCount()).isEqualTo(4);
     assertThat(dataSet.personsTable.getRowCount()).isEqualTo(3);
   }
 
   @Test
   public void checkValidRefUsage() throws DataSetException
   {
     DataSetRegistry.use(new ExtendedDemoGroovyDataSet());
     assertThat(SAT.getTitle()).isEqualTo("Software Architect");
   }
 
   @Test(expected=RuntimeException.class)
   public void checkInvalidRefUsage() throws DataSetException
   {
     DataSetRegistry.use(new DemoGroovyDataSet());
     assertThat(SAT.getTitle()).isEqualTo("Software Architect");
 
     Fail.fail();
   }
   
 }
