 package digby.status;
 
 import java.util.ArrayList;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import com.sun.jersey.api.json.JSONConfiguration;
 
 import digby.fe.DigitalAsset;
 import digby.fe.UpdateFe;
 
 @Path("/v1/assets")
 public class V1_asset {
 
 	
 @GET
 	@Path("{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public ArrayList<DigitalAsset> updateDigitalAsset1(@PathParam("id") String id) {
 		String result = "Identifier" + id;
 		// System.out.println("Size "+digitalasset.size());
 		
 	
 		
 		System.out.println("http://projects.cs.dal.ca:8080/digby/api/v1/update/all/"
 						+ id);
 	
 		
 		ClientConfig config= new DefaultClientConfig();
 		config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
 		Client client=Client.create(config);
 		ClientResponse resStatus =client.resource("http://projects.cs.dal.ca:8080/digby/api/v1/update/all/"
 						+ id).get(ClientResponse.class);
 		UpdateFe uf= resStatus.getEntity(UpdateFe.class);
 		
 		/*System.out.println(res.getAr());
 		
 		V1_status.digitalasset=res.getAr();
 		*/
		if(V1_status.digitalasset!=null){
 		V1_status.digitalasset.clear();
		}
 		System.out.println(uf.getAr());
 		V1_status.digitalasset=uf.getAr();
 		return V1_status.digitalasset;
 		
 	}
 	
 	
 }
