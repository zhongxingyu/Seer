 package edu.umn.robotcontrol.servlet;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import com.google.gson.Gson;
 
 import edu.umn.robotcontrol.domain.RobotCommand;
 
 /**
  * @author Tyler
  * 
  */
 @Path("/control")
 public class RobotControlResource {
 	
 
   @GET
   @Path("/hello")
   @Produces(MediaType.TEXT_HTML)
   public String sayHello() {
     return "Hello";
   }
   
   @GET
   @Path("/photo")
   @Produces("image/jpeg")
   public Response getRecentImage(){
 	  File imageFile = ImageHolder.getInstance().getCurrentImageFile();
 	  if(imageFile != null && imageFile.exists()){
 		  System.out.println("Returning image " + imageFile);
 		  return Response.ok(imageFile, "image/jpeg").build();		  
 	  }
 	  if(imageFile == null){
 		  System.out.println("Image file was null");
 	  } else {
 		  System.out.println("Image file didn't exist");
 	  }
 	  return Response.ok().build();
   }
 
   @POST
   @Path("/photo")
  @Produces(MediaType.TEXT_HTML)
   @Consumes(MediaType.APPLICATION_OCTET_STREAM)
   public String handlePhoto(byte[] data) throws IOException {
     System.out.println("photo was posted, " + data.length + " bytes");
     File tmp = File.createTempFile("image", ".jpeg");
     FileOutputStream fos = new FileOutputStream(tmp);
     fos.write(data);
     fos.close();
     System.out.println("Wrote bytes to " + tmp);
     ImageHolder.getInstance().setCurrentImageFile(tmp);
    return "Photo posted";
   }
 
   @POST
   @Path("/command")
  @Produces(MediaType.TEXT_HTML)
   @Consumes(MediaType.APPLICATION_JSON)
   public String handleCommand(String command) {
     Gson gson = new Gson();
     RobotCommand rc = gson.fromJson(command, RobotCommand.class);
     CommandHolder.getInstance().pushCommand(rc);
    return "Command Posted " + rc;
   }
   
   @GET
   @Path("/command")
   @Produces(MediaType.APPLICATION_JSON)
   public Response getRecentCommand(){
 	  RobotCommand rc = CommandHolder.getInstance().popCommand();
 	  if(rc != null){
 		Gson gson = new Gson();
 		String json = gson.toJson(rc);
 		return Response.ok(json).build();
 	  }
 	  return Response.noContent().build();
   }
 
 }
