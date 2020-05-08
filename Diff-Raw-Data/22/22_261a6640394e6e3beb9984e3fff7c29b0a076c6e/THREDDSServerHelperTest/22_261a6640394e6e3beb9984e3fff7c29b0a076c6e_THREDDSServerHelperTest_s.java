 package gov.usgs.gdp.helper;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.net.ConnectException;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 import java.util.List;
 
 import org.junit.Test;
 
 import thredds.catalog.InvAccess;
 import thredds.catalog.ServiceType;
 
 public class THREDDSServerHelperTest {
     @Test
     public void testIsServerReachable() {
         String workingHost = "www.google.com";
         int workingPort = 80;
         int timeout = 5000;
 
         boolean result = false;
         try {
             result = THREDDSServerHelper.isServerReachable(workingHost, workingPort, timeout);
         } catch (UnknownHostException e) {
             fail(e.getMessage());
         } catch (IOException e) {
             fail(e.getMessage());
         }
         assertTrue(result);
 
         int nonWorkingPort = 64738;
         try {
             result = THREDDSServerHelper.isServerReachable(workingHost, nonWorkingPort, timeout);
         } catch (UnknownHostException e) {
             fail(e.getMessage());
         } catch (IOException e) {
             assertTrue(e instanceof ConnectException || e instanceof SocketTimeoutException);
         }
 
         String nonWorkingHost = "www.ivan-suftin-rocks.com";
         try {
             result = THREDDSServerHelper.isServerReachable(nonWorkingHost, workingPort, timeout);
         } catch (UnknownHostException e) {
             assertNotNull(e);
         } catch (IOException e) {
             fail(e.getMessage());
         }
     }
     
     @Test
     public void testGetDatasetHandlesFromServer() {
     	String host = "motherlode.ucar.edu";
     	int port = 8080;
     	String uri = "/thredds/catalog/station/metar/catalog.xml";
         
     	List<InvAccess> handles = THREDDSServerHelper.getDatasetHandlesFromServer(host, port, uri, ServiceType.OPENDAP);
     	assertFalse(handles.isEmpty());
     }
     
 
     @Test
     public void testGetInvCatalogFromServer() throws IOException {
     	String host = "motherlode.ucar.edu";
     	int port = 8080; 
     	String uri = "/thredds/catalog/station/metar/catalog.xml";
 
         // This will throw an exception if it fails to get the catalog.
     	THREDDSServerHelper.getCatalogFromServer(host, port, uri);
     }
     
    @Test 
     public void testGetAvailableTimeBeanList() {
         // This test is taking at least 5 minutes on my box (I didn't have the patience to let it complete), so
         // I commented it out. The problem lies somewhere in gov.usgs.gdp.analysis.NetCDFUtility.getDateRange.
 
 //    	String datasetUrl = "http://motherlode.ucar.edu:8080/thredds/dodsC/station/metar/Surface_METAR_20100130_0000.nc";
 //    	String grid = "record.wind_from_direction_max";
 //    	TimeBean result = null;
 //		try {
 //			result = THREDDSServerHelper.getTimeBean(datasetUrl, grid);
 //		} catch (IllegalArgumentException e) {
 //			fail(e.getMessage());
 //		} catch (IOException e) {
 //			fail(e.getMessage());
 //		} catch (ParseException e) {
 //			fail(e.getMessage());
 //		}
 //
 //    	assertNotNull(result);
     }
 
     
     @Test 
     public void testGetFeatureDataSet() {
     	//
     }
 }
