 package test.model.productIdentification;
 
 import static org.easymock.EasyMock.expect;
 import static org.junit.Assert.assertTrue;
 import model.productIdentification.UpcSearchApi;
 
 import org.easymock.EasyMock;
 import org.easymock.EasyMockSupport;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import common.util.IHttpClient;
 
 public class UpcSearchApiTest extends EasyMockSupport {
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testGetDescriptionForProduct() {
		IHttpClient c = createNiceMock(IHttpClient.class);
		String expectedJson = "";
		expect(c.getHttpRequest(EasyMock.anyObject(String.class))).andStubReturn(expectedJson);

		UpcSearchApi usa = new UpcSearchApi(c);
		String desc = usa.getDescriptionForProduct("035000053640");
		assertTrue(desc.equals("Ajax with Bleach Poweder Cleaner"));
 	}
 }
