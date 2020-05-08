 package net.cheney.motown.server.middleware;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.net.URISyntaxException;
 import java.nio.ByteBuffer;
 
 import junit.framework.Assert;
 
 import net.cheney.motown.common.api.MimeType;
 import net.cheney.motown.common.api.Request;
 import net.cheney.motown.common.api.Response;
 import net.cheney.motown.common.api.Status;
 import net.cheney.motown.common.api.Version;
 import net.cheney.motown.common.api.Request.Method;
 import net.cheney.motown.server.api.Application;
 import net.cheney.motown.server.api.Environment;
 
 import org.junit.Test;
 
 
 public class CommonLoggerTest {
 
 	@Test public void testCommonLogger() throws URISyntaxException {
 		Application app = new Application() {
 			
 			@Override
 			public Response call(Environment env) {
 				return Response.success(MimeType.TEXT_PLAIN, ByteBuffer.wrap("Hello World!".getBytes()));
 			}
 		}; 
 		
 		StringWriter logger = new StringWriter();
 		
 		// wrap application in logger
 		app = new CommonLogger(app, logger);
 		
 		Request request = new Request(Method.GET, "/foo", Version.HTTP_1_0);
 		Environment env = Environment.fromRequest(request);
 		
 		Response res = app.call(env);
 		
 		Assert.assertEquals(res.status(), Status.SUCCESS_OK);
		Assert.assertEquals("foo", logger.toString());
 	}
 }
