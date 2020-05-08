 package test.cli.cloudify;
 
 import java.io.IOException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.testng.annotations.Test;
 
 import framework.utils.LogUtils;
 
 /**
  * Local cloud test to verify rest API is present.
  * @author yael
  *
  */
 public class RestApiDocumentationIsPresentTest extends AbstractLocalCloudTest {
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void test() {
 		final DefaultHttpClient httpClient = new DefaultHttpClient();
		String url = restUrl + "/resources/restdoclet/restdoclet.html";
 		HttpGet get = new HttpGet(url);
 		HttpResponse response = null;
 		LogUtils.log("Validating that rest documentation exists - sending get request to " + url);
 		try {
 			response = httpClient.execute(get);
 		} catch (IOException e) {
 			AssertFail("Failed to send request to " + url , e);
 		}
 		assertNotNull(response);
 		assertNotNull(response.getStatusLine());
 		assertEquals(200, response.getStatusLine().getStatusCode());
 	}
 }
