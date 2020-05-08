 package org.playerbot.ai;
 
 import java.io.FileInputStream;
 
 import org.dbunit.DefaultDatabaseTester;
 import org.dbunit.IDatabaseTester;
 import org.dbunit.database.IDatabaseConnection;
 import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
 import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
 import org.dbunit.operation.CompositeOperation;
 import org.dbunit.operation.DatabaseOperation;
 import org.junit.Before;
 
 public abstract class AbstractDbUnitTest extends AbstractTest {
     protected IDatabaseConnection sourceConnection;
     protected IDatabaseConnection destinationConnection;
     protected IDataSet sourceDataSet;
     protected IDataSet destinationDataSet;
 
     @Before
     public void before() throws Exception {
         loadConfig();
 
         sourceConnection = getConnection(getSourceConnectionName());
         IDatabaseTester source = new DefaultDatabaseTester(sourceConnection);
         destinationConnection = getConnection(getDestinationConnectionName());
         IDatabaseTester destination = new DefaultDatabaseTester(destinationConnection);
 
         sourceDataSet = getDataSet(getSourceDataSetName());
         source.setDataSet(sourceDataSet);
         source.setSetUpOperation(new CompositeOperation(DatabaseOperation.DELETE, DatabaseOperation.INSERT));
 
        ReplacementDataSet replacementDataSet = new ReplacementDataSet(getDataSet(getDestinationDataSetName()));
        replacementDataSet.addReplacementObject("[null]", null);
        destinationDataSet = replacementDataSet;
         destination.setDataSet(destinationDataSet);
         destination.setSetUpOperation(DatabaseOperation.NONE);
 
         source.onSetup();
         destination.onSetup();
     }
 
 
 
     protected IDataSet getDataSet(String resourceName) throws Exception {
         return new FlatXmlDataSetBuilder().build(new FileInputStream("src/test/resources/" + resourceName));
     }
 
     protected abstract String getDestinationDataSetName();
     protected abstract String getSourceDataSetName();
     protected abstract String getDestinationConnectionName();
     protected abstract String getSourceConnectionName();
 }
