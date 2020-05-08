 package parkservice.client;
 
 import java.net.URI;
 import java.util.Date;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriBuilder;
 
 import parkservice.model.AuthRequest;
 import parkservice.model.RefillRequest;
 import parkservice.model.UnparkRequest;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 //blah
 public class TestUnpark {
 		public static void main(String[] args) {
 			ClientConfig config = new DefaultClientConfig();
 			Client client = Client.create(config);
 			WebResource service = client.resource(getBaseURI());
 			
 			AuthRequest in = new AuthRequest();
			in.setEmail("miguel@parqme.com");
			in.setPassword("a");
 			
 			UnparkRequest pr = new UnparkRequest();
 			
			pr.setParkingReferenceNumber("12:71:1325022511");
 			pr.setSpotId(111); //works with wrong unpark
			pr.setUid(12);
 			pr.setUserInfo(in);
 			
 			
 			String outstring = service.path("unpark").type(MediaType.APPLICATION_JSON).post(String.class, pr);
 			System.out.println(outstring);
 			
 		}
 			
 
 		private static URI getBaseURI() {
 			return UriBuilder.fromUri(
					"http://localhost:8080/parkservice.park").build();
 		}
 	
 
 }
