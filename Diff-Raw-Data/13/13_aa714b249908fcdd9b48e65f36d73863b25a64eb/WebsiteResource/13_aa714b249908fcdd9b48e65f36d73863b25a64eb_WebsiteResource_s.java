 package org.oregami.resources;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.oregami.data.WebsiteDao;
 import org.oregami.entities.Website;
 import org.oregami.util.WebsiteHelper;
 
 import com.google.inject.Inject;
 
 @Path("/website")
 @Produces(MediaType.APPLICATION_JSON)
 public class WebsiteResource {
 
 	@Inject
 	private WebsiteDao websiteDao;
 
 	@POST
 	public Response createScreenshot(@QueryParam("url") String url,
 			@QueryParam("size") String size) {
 		WebsiteHelper websiteHelper = WebsiteHelper.instance();
 		try {
 			Map<String, String> result = websiteHelper.createWebsite(url, size);
 			Website website = new Website();
 			byte[] image = WebsiteHelper.instance().readFile(result.get("filename"));
 			website.setImage(image);
 			website.setThumbnail(WebsiteHelper.instance().resize(image, 200, null));
 			String id = websiteDao.save(website);
 			result.put("id", id.toString());
 			return Response.ok(result).build();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return Response.status(Status.NOT_ACCEPTABLE).type("text/plain").entity(e.getMessage()).build();
 		}
 
 	}
 
 	@GET
 	@Path("/raw/{id}")
 	@Produces({ "image/png" })
 	public Response getRawWebsite(@PathParam("id") String id) {
 		Website w = websiteDao.findOne(id);
 		if (w==null) {
 			return Response.status(Response.Status.NOT_FOUND).build();
 		}
 		
 //		if (!w.isApproved()) {
 //			return Response.status(Response.Status.PRECONDITION_FAILED).build();
 //		}
 
 		return Response.ok(w.getImage()).build();
 	}
 	
 	@GET
 	@Path("/rawthumb/{id}")
 	@Produces({ "image/png" })
 	public Response getRawWebsiteThumbnail(@PathParam("id") String id) {
 		Website w = websiteDao.findOne(id);
 		if (w==null) {
 			return Response.status(Response.Status.NOT_FOUND).build();
 		}
 		
 //		if (!w.isApproved()) {
 //			return Response.status(Response.Status.PRECONDITION_FAILED).build();
 //		}
 
 		return Response.ok(w.getThumbnail()).build();
 	}	
 	
 	@GET
 	public Response getList() {
 		List<String> result = new ArrayList<String>();
 		
 		List<Website> list = websiteDao.findAll();
 		for (Website website : list) {
 			result.add(website.getId());
 		}
 		return Response.ok(result).build();
 	}
 
 
 }
