 
 package at.roadrunner.android.test.sensor;
 
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import junit.framework.TestCase;
import at.roadrunner.android.model.HttpSensor;
 import at.roadrunner.android.sensor.SensorConnectionFactory;
 import at.roadrunner.android.sensor.SensorType;
 
 
 public class HttpSensorTest extends TestCase {
 
 	private HttpSensor _httpSensor;
 
 	protected SensorConnectionFactory _cf ;
 
 	static protected int _pos = 0;
 	
 	
 	
 	private class MockHttpURLConnection extends HttpURLConnection {
 	
 		protected MockHttpURLConnection(URL url) {
 			super(url);
 		}
 
 		protected InputStream stream = new InputStream() {
 			@Override
 			public int read() throws IOException {
 				char[] data = { '1', '7' };
 				if (_pos >= data.length) {
 					return -1;
 				}
 				return data[_pos++];
 			}
 		};
 		
 		public InputStream getInputStream() throws IOException {return stream;};
 		
 		@Override
 		public void disconnect() {}
 
 		@Override
 		public boolean usingProxy() {
 			return false;
 		}
 
 		@Override
 		public void connect() throws IOException {}
 	}
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		URL url  = new URL("http://172.16.102.224:4711");
 
 		_httpSensor = new HttpSensor(url, SensorType.Temperature,_cf);
 	}
 	
 	/**
 	 * Tests a HttpSensor at location URI
 	 * If Node at location URI is not running, this test will not succeed 
 	 * @throws IOException 
 	 */
 	public void testHttpSensor() throws IOException {
 
 		assertTrue(_httpSensor.getData().compareTo("17") == 0);
 		assertTrue(_httpSensor.getData().compareTo("17") == 0);
 		assertTrue(_httpSensor.getData().compareTo("17") == 0);
 		assertTrue(_httpSensor.getData().compareTo("17") == 0);
 		assertTrue(_httpSensor.getData().compareTo("17") == 0);
 
 	}
 	
 	@Override
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		_httpSensor = null;
 	}
 
 	
 }
