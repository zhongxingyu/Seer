 package com.javaapps.legaltracker.receiver;
 
 import static org.junit.Assert.*;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.ProtocolVersion;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.message.BasicHttpResponse;
 import org.easymock.IMocksControl;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.javaapps.legaltracker.pojos.Config;
 import com.javaapps.legaltracker.pojos.LegalTrackerLocation;
 import com.javaapps.legaltracker.utils.MockHttpClientFactory;
import com.javaapps.legaltracker.utils.MockHttpClientFactory;
 import com.xtremelabs.robolectric.RobolectricTestRunner;
 
 @RunWith(RobolectricTestRunner.class)
 public class LocationDataUploaderHandlerTest {
 
 	private List<LegalTrackerLocation> locationDataList = new ArrayList<LegalTrackerLocation>();
 	private static File testFileDir = new File("unitTestDir");
 	private static File testFile;
     private ProtocolVersion protocolVersion=new ProtocolVersion("HTTP",1,2);
 	
 	@BeforeClass
 	public static void setupBeforeClass() {
 		try {
 			if (!testFileDir.exists()) {
 				testFileDir.mkdir();
 			}
 			testFile=new File(testFileDir.getPath()+ "/unittest.obj");
 			ObjectOutputStream oos = new ObjectOutputStream(
 					new FileOutputStream(testFile));
 			for (int ii = 0; ii < 100; ii++) {
 				LegalTrackerLocation location = new LegalTrackerLocation(40.0,
 						-80.0, 10.f, 20.0f, 10.0f, System.currentTimeMillis());
 				oos.writeObject(location);
 			}
 			oos.flush();
 			oos.close();
 			Config.getConfig()
 					.setLocationDataEndpoint("http://boguswebsite.go");
 		} catch (Exception ex) {
 			fail("LocationDataUploaderHandlerTest setup failed because "
 					+ ex.getMessage());
 		}
 	}
 
 	@Before
 	public void setup() {
 		try {
 		} catch (Exception ex) {
 			fail("Unable to setup test because " + ex.getMessage());
 		}
 	}
 
 
 	@Test
 	public void uploadDataResultMapSizeTest() throws ClientProtocolException, IOException {
 		LocationDataUploaderHandler locationDataUploaderHandler = new LocationDataUploaderHandler(
 				testFileDir);
 		locationDataUploaderHandler.setHttpClientFactory(new MockHttpClientFactory(protocolVersion,new int[]{400},"URL not found"));
 		Config.getConfig().setUploadBatchSize(13);
 		locationDataUploaderHandler.uploadData("unittest");
 		Map<Integer,Integer>resultMap=locationDataUploaderHandler.fileResultMaps.get(testFile.getAbsolutePath()).getResultMap();
 		assertTrue("expecting 8 but was "+ resultMap.size(),resultMap.size() == 8);
 		Config.getConfig().setUploadBatchSize(10);
 		locationDataUploaderHandler.uploadData("unittest");
 		assertTrue("expecting 10 but was "+ resultMap.size(),resultMap.size() == 10);
 	}
 	
 	@Test
 	public void uploadDataResultMapWithBadStatusTest() throws ClientProtocolException, IOException {
 		LocationDataUploaderHandler locationDataUploaderHandler = new LocationDataUploaderHandler(
 				testFileDir);
 		locationDataUploaderHandler.setHttpClientFactory(new MockHttpClientFactory(protocolVersion,new int[]{400},"URL not found"));
 		Config.getConfig().setUploadBatchSize(10);
 		locationDataUploaderHandler.uploadData("unittest");
 		Map<Integer,Integer>resultMap=locationDataUploaderHandler.fileResultMaps.get(testFile.getAbsolutePath()).getResultMap();
 	     assertTrue("resultMap is empty",resultMap.size()>0);
 		try {
 			Thread.sleep(500);
 		} catch (InterruptedException e) {
 		}
 		System.out.println(resultMap);
 		for (Entry<Integer, Integer> entry : resultMap
 				.entrySet()) {
 			assertTrue("expecting 400 status code but was " + entry.getValue(),
 					entry.getValue() == 400);
 		}
 		locationDataUploaderHandler.cleanUpExistingFiles();
 		assertNotNull("Could not find fileResultMap",locationDataUploaderHandler.fileResultMaps.get(testFile.getAbsolutePath()));
 		File file=new File(testFile.getAbsolutePath());
 		assertTrue("file should not have been deleted because all status codes are bad",file.exists());
 
 	}
 
 	/**
 	 This test must be run last because it deletes the test file
 	 */
 	@Test
 	public void uploadDataResultMapWithGoodStatusLastTest() throws ClientProtocolException, IOException {
 		LocationDataUploaderHandler locationDataUploaderHandler = new LocationDataUploaderHandler(
 				testFileDir);
 		locationDataUploaderHandler.setHttpClientFactory(new MockHttpClientFactory(protocolVersion,new int[]{201},"URL not found"));
 		Config.getConfig().setUploadBatchSize(10);
 		locationDataUploaderHandler.uploadData("unittest");
 		Map<Integer,Integer>resultMap=locationDataUploaderHandler.fileResultMaps.get(testFile.getAbsolutePath()).getResultMap();
        assertTrue("resultMap is empty",resultMap.size()>0);
 		try {
 			Thread.sleep(500);
 		} catch (InterruptedException e) {
 		}
 		System.out.println(resultMap);
 		for (Entry<Integer, Integer> entry : resultMap
 				.entrySet()) {
 			assertTrue("expecting 201 status code but was " + entry.getValue(),
 					entry.getValue() == 201);
 		}
 		locationDataUploaderHandler.cleanUpExistingFiles();
 		assertNull("Could not find fileResultMap",locationDataUploaderHandler.fileResultMaps.get(testFile.getAbsolutePath()));
 		File file=new File(testFile.getAbsolutePath());
 		assertFalse("file should have been deleted because all status codes are good",file.exists());
 	}
 	
 }
