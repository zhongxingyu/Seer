 package async.net.http.impl;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 import org.mockito.Mockito;
 
 import async.net.http.HttpResponse;
 
 public class DefaulHttpResponseTest {
 	@Test
 	public void test_default() throws Exception {
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		HttpResponse respone = new DefaulHttpResponse(out);
 		respone.setReturnCode(200);
 		OutputStream stream = respone.getOutputStream();
 		stream.write("test".getBytes());
 		stream.close();
 		String string = out.toString();
 		int i = string.indexOf("\r\n\r\n");
 		List<String> list = Arrays.asList(string.substring(0, i).split("\r\n"));
 		String string2 = string.substring(i + 4);
		String[] ex = { "HTTP/1.0 200", "Content-Type: text/html", "Content-Length: 4" };
 		List<String> exList = Arrays.asList(ex);
 
 		List<String> more = new ArrayList<String>(list);
 		more.removeAll(exList);
 
 		List<String> less = new ArrayList<String>(exList);
 		less.removeAll(list);
 
 		Assert.assertTrue("Less: " + less, less.isEmpty());
 		Assert.assertTrue("More: " + more, more.isEmpty());
 		Assert.assertEquals(string2, "test");
 	}
 
 	@Test
 	public void testClose() throws Exception {
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		DefaulHttpResponse response = new DefaulHttpResponse(out);
 		response.close();
 		response.responseOut = Mockito.mock(HttpResponseOutputStream.class);
 		response.close();
 		Mockito.verify(response.responseOut).close();
 	}
 
 	@Test
 	public void testIsFlush() throws Exception {
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		DefaulHttpResponse response = new DefaulHttpResponse(out);
 		Assert.assertEquals(false, response.isFlush());
 		OutputStream out2 = response.getOutputStream();
 		Assert.assertEquals(false, response.isFlush());
 		response.setReturnCode(200);
 		out2.flush();
 		Assert.assertEquals(true, response.isFlush());
 	}
 
 	@Test
 	public void testSendError() throws Exception {
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		DefaulHttpResponse response = new DefaulHttpResponse(out);
 		response.sendError();
 		System.out.println(out.toString());
 	}
 
 	@Test(expected = IOException.class)
 	public void testSendError_Exception() throws Exception {
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		DefaulHttpResponse response = new DefaulHttpResponse(out);
 		response.setReturnCode(200);
 		response.getOutputStream().close();
 		response.sendError();
 		System.out.println(out.toString());
 	}
 
 	@Test
 	public void testGetWriter() throws Exception {
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		DefaulHttpResponse response = new DefaulHttpResponse(out);
 		response.setReturnCode(200);
 		response.getWriter().print("test");
 		System.out.println(out.toString());
 	}
 
 }
