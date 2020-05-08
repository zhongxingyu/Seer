 package de.anycook.graph;
 
 import java.io.File;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.Response;
 import de.anycook.session.Session;
 import de.anycook.upload.RecipeUploader;
 import de.anycook.upload.UploadHandler;
 import de.anycook.upload.UserUploader;
 import de.anycook.user.User.Userfields;
 
 
 @Path("upload")
 public class UploadGraph {
 	
 	@POST
 	@Path("image/{type}")
 	public Response uploadRecipeImage(@Context HttpServletRequest request,
 			@Context HttpHeaders hh,
 			@PathParam("type") String type){
 		
 		UploadHandler upload = null;
 		Session session = Session.init(request.getSession());
 		try{
 			switch (type) {
 			case "recipe":
 				upload = new RecipeUploader();
 				break;
			case "user":
				
 				session.checkLogin(hh.getCookies());
 				upload = new UserUploader();
 				break;
 			default:
 				throw new WebApplicationException(400);
 			}
 			File tempfile = upload.uploadFile(request);		
 			if(tempfile!=null){
 				String newFilename = upload.saveFile(tempfile);
 				if(type.equals("user"))
 					session.getUser().changeSetting(Userfields.IMAGE, newFilename);
 				
 				return  Response.ok("{success:\""+newFilename+"\"}").build();									
 			}
 			else
 				return Response.status(400).entity("{error:\"upload failed\"}").build();
 		}catch(WebApplicationException e){
 			throw new WebApplicationException(401);
 		}
 		
 	}
 	
 	
 }
