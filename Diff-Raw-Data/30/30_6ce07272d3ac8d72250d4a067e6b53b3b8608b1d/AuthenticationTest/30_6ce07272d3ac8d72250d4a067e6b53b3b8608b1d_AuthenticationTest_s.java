 package org.openrdf.http.object;
 
 import java.util.Map;
 
 import org.apache.http.HttpMessage;
 import org.apache.http.HttpResponse;
 import org.apache.http.message.BasicHttpRequest;
 import org.openrdf.http.object.annotations.cacheControl;
 import org.openrdf.http.object.annotations.method;
 import org.openrdf.http.object.annotations.realm;
 import org.openrdf.http.object.annotations.type;
 import org.openrdf.http.object.base.MetadataServerTestCase;
 import org.openrdf.http.object.traits.Realm;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.object.ObjectConnection;
 import org.openrdf.repository.object.annotations.iri;
 import org.openrdf.repository.object.annotations.matches;
 
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 
 public class AuthenticationTest extends MetadataServerTestCase {
 
 	@matches("urn:test:my_realm")
 	public static class MyRealm implements Realm {
 
 		public String protectionDomain() {
 			return null;
 		}
 
 		public String allowOrigin() {
 			return "*";
 		}
 
 		public boolean withAgentCredentials(String origin) {
 			return false;
 		}
 
 		public Object authenticateRequest(String method, Object resource,
 				Map<String, String[]> request) throws RepositoryException {
 			if (request.containsKey("authorization") && !"bad".equals(request.get("authorization")[0]))
 				return "me";
 			return null;
 		}
 
 		public boolean authorizeCredential(Object credential, String method,
 				Object resource, Map<String, String[]> request) {
 			return true;
 		}
 
 		public HttpMessage authenticationInfo(String method, Object resource,
 				Map<String, String[]> request) {
 			BasicHttpRequest msg = new BasicHttpRequest("GET", "/");
 			msg
					.addHeader("Authorization-Info", request
 							.get("authorization")[0]);
 			return msg;
 		}
 
 		public HttpResponse forbidden(String method, Object resource,
 				Map<String, String[]> request) throws Exception {
 			return null;
 		}
 
 		public HttpResponse unauthorized(String method, Object resource,
 				Map<String, String[]> request) throws Exception {
 			return null;
 		}
 
 	}
 
 	@iri("urn:test:MyProtectedResource")
 	public static class MyProtectedResource {
 		public static String body = "body";
 
 		@method("GET")
 		@realm("urn:test:my_realm")
 		@type("text/plain")
 		@cacheControl("max-age=5")
 		public String getResponse() {
 			return body;
 		}
 	}
 
 	public void setUp() throws Exception {
 		config.addConcept(MyProtectedResource.class);
 		config.addConcept(MyRealm.class);
 		super.setUp();
 		ObjectConnection con = repository.getConnection();
 		try {
 			String uri = client.path("/protected").getURI().toASCIIString();
 			con.addDesignation(con.getObject(uri), MyProtectedResource.class);
 		} finally {
 			con.close();
 		}
 	}
 
 	public void testOnce() throws Exception {
 		ClientResponse resp;
 		WebResource web = client.path("/protected");
 		MyProtectedResource.body = "first";
 		resp = web.header("Authorization", "one").get(ClientResponse.class);
 		assertNotNull(resp.getHeaders().get("ETag"));
		assertEquals("one", resp.getHeaders().get("Authorization-Info").get(0));
 		assertEquals("first", resp.getEntity(String.class));
 	}
 
 	public void testTwice() throws Exception {
 		ClientResponse resp;
 		WebResource web = client.path("/protected");
 		MyProtectedResource.body = "first";
 		resp = web.header("Authorization", "one").get(ClientResponse.class);
 		MyProtectedResource.body = "second";
 		resp = web.header("Authorization", "two").get(ClientResponse.class);
 		assertNotNull(resp.getHeaders().get("ETag"));
 		assertEquals("first", resp.getEntity(String.class)); // body should be cached
		assertEquals("two", resp.getHeaders().get("Authorization-Info").get(0));
 	}
 
 	public void testBadAuth() throws Exception {
 		ClientResponse resp;
 		WebResource web = client.path("/protected");
 		MyProtectedResource.body = "first";
 		resp = web.header("Authorization", "one").get(ClientResponse.class);
 		resp = web.header("Authorization", "bad").get(ClientResponse.class);
 		assertFalse("first".equals(resp.getEntity(String.class)));
		assertNull(resp.getHeaders().get("Authorization-Info"));
 	}
 
 	public void testBadAndGoddAuth() throws Exception {
 		ClientResponse resp;
 		WebResource web = client.path("/protected");
 		MyProtectedResource.body = "first";
 		resp = web.header("Authorization", "one").get(ClientResponse.class);
 		resp = web.header("Authorization", "bad").get(ClientResponse.class);
 		MyProtectedResource.body = "second";
 		resp = web.header("Authorization", "two").get(ClientResponse.class);
 		assertNotNull(resp.getHeaders().get("ETag"));
 		assertEquals("first", resp.getEntity(String.class)); // body should still be cached
		assertEquals("two", resp.getHeaders().get("Authorization-Info").get(0));
 	}
 
 }
