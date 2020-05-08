 package com.seitenbau.testing.dbunit;
 
 import static com.seitenbau.testing.dbunit.PersonDatabaseRefs.SAT;
 import static org.fest.assertions.Assertions.assertThat;
 
 import org.dbunit.dataset.DataSetException;
 import org.fest.assertions.Fail;
 import org.junit.Test;
 
 import com.seitenbau.testing.dbunit.dataset.DemoGroovyDataSet;
 import com.seitenbau.testing.dbunit.dataset.ExtendedDemoGroovyDataSet;
 import com.seitenbau.testing.dbunit.dsl.DataSetRegistry;
 
 public class DataSetCompositionTest
 {
 
   @Test
   public void checkExtendedDemoGroovyDataSet() throws DataSetException
   {
    // prepare
     ExtendedDemoGroovyDataSet dataSet = new ExtendedDemoGroovyDataSet();

    // verify
     assertThat(dataSet.teamsTable.getRowCount()).isEqualTo(2);
     assertThat(dataSet.jobsTable.getRowCount()).isEqualTo(4);
     assertThat(dataSet.personsTable.getRowCount()).isEqualTo(3);
   }
 
   @Test
   public void checkValidRefUsage() throws DataSetException
   {
    // prepare
     DataSetRegistry.use(new ExtendedDemoGroovyDataSet());

    // verify
     assertThat(SAT.getTitle()).isEqualTo("Software Architect");
   }
 
   @Test(expected=RuntimeException.class)
   public void checkInvalidRefUsage() throws DataSetException
   {
    // prepare
     DataSetRegistry.use(new DemoGroovyDataSet());

    // verify
    // throws a RuntimeException
     assertThat(SAT.getTitle()).isEqualTo("Software Architect");
 
     Fail.fail();
   }
   
 }
