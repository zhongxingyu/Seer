 package keysmith.server.resources;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import keysmith.server.core.Keystore;
 import keysmith.server.core.SimpleKey;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.yammer.dropwizard.hibernate.UnitOfWork;
 import com.yammer.metrics.annotation.Timed;
 
 @Consumes(MediaType.APPLICATION_JSON)
 @Produces(MediaType.APPLICATION_JSON)
 @Path("/keysmith")
 public class KeysmithResource {
 
 	private static final Logger log = LoggerFactory
 			.getLogger(KeysmithResource.class);
 
 	private final Keystore keyStore;
 
 	public KeysmithResource(Keystore keyStore) {
 		super();
 		this.keyStore = keyStore;
 	}
 
 	@GET
 	@Timed
 	@Path("/publicKey/{keyId}")
 	@UnitOfWork
 	public Response getPublicKey(@PathParam("keyId") String keyId) {
 		SimpleKey key = keyStore.get(keyId);
 		if (key == null) {
 			return Response.noContent().build();
 		}
 		return Response.ok(key.getData()).build();
 	}
 
 	@POST
 	@Timed
 	@Path("/publicKey")
 	@UnitOfWork
 	public Response postPublicKey(String keyData) {
 		String keyId = keyStore.put(new SimpleKey(null, keyData));
 		if (keyId == null) {
 			return Response.noContent().build();
 		}
 		return Response.ok(keyId).build();
 	}
 
 	@POST
 	@Timed
 	@Path("/publicKey/{keyId}")
	//@UnitOfWork
 	public Response updatePublicKey(@PathParam("keyId") String keyId, String keyData) {
 		SimpleKey key = new SimpleKey(keyId, keyData);
 		log.info("updatePublicKey.address : " + keyId);
 		log.info("updatePublicKey.message : " + key);
 		String oldKeyId = keyStore.put(key);
 		if (oldKeyId == null) {
 			return Response.noContent().build();
 		}
 		return Response.ok(oldKeyId).build();
 	}
 
 	@DELETE
 	@Timed
 	@Path("/publicKey/{keyId}")
 	@UnitOfWork
 	public Response removePublicKey(@PathParam("keyId") String keyId) {
 		SimpleKey key = keyStore.removeKey(keyId);
 		if (key == null) {
 			return Response.noContent().build();
 		}
 		return Response.ok(key.getData()).build();
 	}
 
 }
