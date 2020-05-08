 package com.sound.service.impl;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.io.IOUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.sound.model.file.LocalFile;
 import com.sound.service.file.itf.FileService;
 import com.sound.service.sound.itf.SoundService;
 import com.sun.jersey.core.header.FormDataContentDisposition;
 import com.sun.jersey.multipart.FormDataParam;
 
 @Component
 @Path("/sound")
 public class SoundServiceEndpoint {
 
 	@Autowired
 	FileService fileService;
 
 	@Autowired
 	SoundService soundService;
 
 	@PUT
 	@Path("/save")
 	@Consumes(MediaType.MULTIPART_FORM_DATA)
 	public Response saveProfile(
 			@FormDataParam("file") InputStream uploadedInputStream,
 			@FormDataParam("file") FormDataContentDisposition fileDetail,
 			@FormDataParam("objectId") String objectId, 
 			@FormDataParam("soundAlias") String soundAlias, 
 			@FormDataParam("description") String description, 
 			@FormDataParam("ownerAlias") String ownerAlias, 
 			@FormDataParam("status") String status)
 	{
 
 		if (null == objectId)
 		{
 			return Response.status(500).entity("sound object id can't be null").build(); 
 		}
 
 		if (null == soundAlias)
 		{
 			return Response.status(500).entity("sound alias can't be null").build(); 
 		}
 		
 		byte[] soundData = null;
 		try 
 		{
			soundData = IOUtils.toByteArray(uploadedInputStream);
 		} 
 		catch (IOException e) {
 			e.printStackTrace();
 			return Response.status(500).entity("poster upload failed.").build(); 
 		}
 
 		LocalFile poster = new LocalFile();
 		poster.setContent(soundData);
		poster.setType(fileDetail.getType());
		poster.setFileName(fileDetail.getName());
 		
 		try
 		{
 			soundService.saveProfile(objectId, soundAlias, description, ownerAlias, status, poster);
 		}
 		catch(Exception e)
 		{
 			return Response.status(500).entity(e.getMessage()).build();
 		}
 		return Response.status(200).entity("true").build();
 	}
 
 	@PUT
 	@Path("/addToSet")
 	public Response addToSet(@FormParam("userId") String userId,
 			@FormParam("soundId") String soundId,
 			@FormParam("SetId") String setId) {
 		soundService.addToSet(soundId, setId);
 
 		return null;
 	}
 
 	@DELETE
 	@Path("/{soundId}")
 	public Response delete(@PathParam("soundId") String soundId) {
 		soundService.delete(soundId);
 
 		return null;
 	}
 
 	@GET
 	@Path("/{soundId}")
 	public Response load(@PathParam("soundId") String soundId) {
 		soundService.load(soundId);
 		
 		Response.status(200).entity(null).build();
 
 		return null;
 	}
 
 }
