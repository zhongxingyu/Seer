 package edu.northwestern.bioinformatics.studycalendar.osgi.datasources;
 
 import edu.northwestern.bioinformatics.studycalendar.osgi.OsgiLayerIntegratedTestCase;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 import org.osgi.framework.Constants;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 
 import javax.sql.DataSource;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 
 import static edu.northwestern.bioinformatics.studycalendar.osgi.OsgiLayerIntegratedTestHelper.*;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.junit.Assert.assertThat;
 
 /**
  * @author Rhett Sutphin
  */
 @RunWith(JUnit4.class)
 public class DataSourcesIntegrationTest extends OsgiLayerIntegratedTestCase {
     private static String DATASOURCES_BUNDLE =
         "edu.northwestern.bioinformatics.psc-osgi-layer-datasources";
 
     private static final String PSC_DATASOURCE_PID =
         "edu.northwestern.bioinformatics.studycalendar.database.PSC_DATASOURCE";
     private static final String CSM_DATASOURCE_PID =
         "edu.northwestern.bioinformatics.studycalendar.database.CSM_DATASOURCE";
 
     @Before
     public void before() throws Exception {
         waitForService(DATASOURCES_BUNDLE, DataSource.class.getName());
     }
 
     @Test
     public void itGivesThePscDataSourceAsTheDefault() throws Exception {
         ServiceReference reference =
             getBundleContext().getServiceReference(DataSource.class.getName());
         assertEquals(PSC_DATASOURCE_PID, reference.getProperty(Constants.SERVICE_PID));
     }
 
     @Test
     public void itCanRetrieveThePscDataSource() throws Exception {
         assertNotNull(getDataSourceReference(PSC_DATASOURCE_PID));
     }
 
     @Test
     public void itCanRetrieveTheCsmDataSource() throws Exception {
         assertNotNull(getDataSourceReference(CSM_DATASOURCE_PID));
     }
 
     @Test
     public void itCanUseAConnectionFromThePscDataSource() throws Exception {
         DataSource forPsc = (DataSource) getBundleContext().
             getService(getDataSourceReference(PSC_DATASOURCE_PID));
         Connection conn = forPsc.getConnection();
         try {
             int rowCount = 0;
             ResultSet resultSet = conn.createStatement().executeQuery("SELECT COUNT(*) FROM studies");
             while (resultSet.next()) { rowCount++; }
             assertThat(1, equalTo(rowCount));
         } finally {
             conn.close();
         }
     }
 
     @Test
     public void itCanUseAConnectionFromTheCsmDataSource() throws Exception {
         DataSource forCsm = (DataSource) getBundleContext().
            getService(getDataSourceReference(CSM_DATASOURCE_PID));
         Connection conn = forCsm.getConnection();
         try {
             int rowCount = 0;
             ResultSet resultSet = conn.createStatement().executeQuery("SELECT COUNT(*) FROM csm_role");
             while (resultSet.next()) { rowCount++; }
             assertThat(1, equalTo(rowCount));
         } finally {
             conn.close();
         }
     }
 
     private ServiceReference getDataSourceReference(
         String pid
     ) throws InvalidSyntaxException, IOException {
         ServiceReference[] refs = getBundleContext().getServiceReferences(DataSource.class.getName(),
             String.format("(%s=%s)", Constants.SERVICE_PID, pid));
         assertEquals(pid + " not available", 1, refs.length);
         return refs[0];
     }
 }
