 package digby.status;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 
 import digby.fe.Application;
 
 
 @Path("/v1/app")
 public class V1_app {
 
 	static ArrayList<Application> applicationAccess = new ArrayList<Application>();
 
 	@POST
 	@Path("/post")
 	@Consumes("application/json")
 	public Response setDigitalAsset(Application app) {
 		System.out.println(app);
 		String result = "digitalassetsaved" + app;
 
 		Application initial = null;
 		Iterator<Application> it = applicationAccess.iterator();
 
 		while (it.hasNext()) {
 			initial = it.next();
 			if ((app.getApplicationId()) == initial.getApplicationId()) {
 				return Response.status(503).entity(result).build();
 			}
 		}
 		applicationAccess.add(app);
 		System.out.println(app.getApplicationId());
 		return Response.status(201).entity(result).build();
 	}
 
 	
 	
 	
 	@GET
 	@Path("{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Application returnDigitalAsset(@PathParam("id") String id)
 			throws IOException {
 		System.out.println(id);
 		Application initial = null;
 		Iterator<Application> it = applicationAccess.iterator();
 		// System.out.println("Size "+digitalasset.size());
 
 		while (it.hasNext()) {
 			initial = it.next();
 			if (Integer.parseInt(id) == initial.getApplicationId()) {
 				return initial;
 			} else {
 				initial = null;
 			}
 
 		}
 
 		return initial;
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 
 	@PUT
 	@Path("/put")
 	@Consumes("application/json")
 	public Response updateDigitalAsset(Application app) {
 		System.out.println(app);
 		String result = "digitalassetsaved" + app;
 		// System.out.println("Size "+digitalasset.size());
 		Application initial = null;
 		Iterator<Application> it = applicationAccess.iterator();
 
 		while (it.hasNext()) {
 			initial = it.next();
 			if ((app.getApplicationId()) == initial.getApplicationId()) {
 				it.remove();
 			} else {
 				initial = null;
 			}
 
 		}
 		applicationAccess.add(app);
 		System.out.println(app.getApplicationId());
 		return Response.status(201).entity(result).build();
 
 	}
 
 	@DELETE
 	@Path("/delete/{id}")
 	public void deleteDigitalAsset(@PathParam ("id") String id){
 		String remove = "digitalassetremoved" + id;
 		// System.out.println("Size "+digitalasset.size());
 		//webResource.put(da);
 		Application initial = null;
 		Iterator<Application> it = applicationAccess.iterator();
 
 		while (it.hasNext()) {
 			initial = it.next();
 			if ((Integer.parseInt(id)) == initial.getApplicationId()) {
 				it.remove();
 			} else {
 				initial = null;
 			}
 
 		}
		V1_status.digitalasset=null;
 		System.out.println((Integer.parseInt(id)));
 	
 	}
 	
 }
