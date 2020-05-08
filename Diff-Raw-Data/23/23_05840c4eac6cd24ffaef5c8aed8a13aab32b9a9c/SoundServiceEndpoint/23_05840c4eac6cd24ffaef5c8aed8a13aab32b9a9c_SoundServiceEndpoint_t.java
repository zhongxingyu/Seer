 package com.sound.service.impl;
 
 import java.util.List;
 
 import javax.validation.constraints.NotNull;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.sound.exception.SoundException;
 import com.sound.model.Sound;
 import com.sound.model.SoundActivity.SoundRecord;
 import com.sound.service.file.itf.FileService;
 import com.sound.service.sound.itf.SoundService;
 import com.sound.util.JsonHandler;
 
 @Component
 @Path("/sound")
 public class SoundServiceEndpoint {
 
 	Logger logger = Logger.getLogger(SoundServiceEndpoint.class);
 	
 	@Autowired
 	FileService fileService;
 
 	@Autowired
 	SoundService soundService;
 
 	@GET
 	@Path("/{soundAlias}")
 	public Response loadSound(
 			@NotNull @PathParam("soundAlias") String soundAlias
 			)
 	{
 		Sound sound = null;
 		try
 		{
 			sound = soundService.load(soundAlias);
 		}
 		catch (Exception e)
 		{
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to load sound " + soundAlias).build();
 		}
 		
 		return Response.status(Status.OK).entity(sound.toString()).build();
 	}
 	
 	@PUT
	@Path("/{soundAlias}")
 	public Response saveProfile(
 			@NotNull @FormParam("objectId") String objectId, 
			@NotNull @PathParam("soundAlias") String soundAlias, 
 			@FormParam("description") String description, 
 			@NotNull @FormParam("ownerAlias") String ownerAlias, 
 			@NotNull @FormParam("status") String status,
 			@FormParam("posterId") String posterId)
 	{
 		try
 		{
 			soundService.saveProfile(objectId, soundAlias, description, ownerAlias, status, posterId);
 		}
 		catch(SoundException e)
 		{
 			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
 		}
 		catch(Exception e)
 		{
 			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(("Failed to save sound " + soundAlias)).build();
 		}
 		return Response.status(Status.OK).entity("true").build();
 	}
 
 	@PUT
 	@Path("/addToSet")
 	public Response addToSet(
 			@NotNull @FormParam("userId") String userId,
 			@NotNull @FormParam("soundId") String soundId,
 			@FormParam("SetId") String setId
 			) {
 		soundService.addToSet(soundId, setId);
 
 		return Response.status(Status.OK).entity("true").build();
 	}
 
 	@DELETE
 	@Path("/{soundAlias}")
 	public Response delete(
 			@NotNull @PathParam("soundAlias") String soundAlias
 			) {
 		try
 		{
 			soundService.delete(soundAlias);
 		}
 		catch(Exception e)
 		{
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(("Failed to delete sound " + soundAlias)).build();
 		}
 		return Response.status(Status.OK).entity("true").build();
 	}
 	
 	@GET
 	@Path("/streams/{userAlias}")
 	public Response listUsersSounds(
 			@NotNull @PathParam("userAlias") String userAlias,
 			@QueryParam("pageNum")  Integer pageNum,
 			@QueryParam("soundsPerPage")  Integer soundsPerPage
 			)
 	{
 		pageNum = (null == pageNum)? 0 : pageNum;
 		soundsPerPage = (null == soundsPerPage)? 15 : soundsPerPage;
 		
 		List<SoundRecord> sounds = null;
 		try {
 			sounds = soundService.getSoundsByUser(userAlias, pageNum, soundsPerPage);
 		} catch (SoundException e) {
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
 		}
 		catch(Exception e)
 		{
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to load sound streams.").build();
 		}
 		
 		return Response.status(Status.OK).entity(JsonHandler.toJson(sounds)).build();
 	}
 	
 	@GET
 	@Path("/streams")
 	public Response listObservingSounds(
			@NotNull @QueryParam("userAlias") String userAlias,
			@QueryParam("pageNum")  Integer pageNum,
			@QueryParam("soundsPerPage")  Integer soundsPerPage
 			)
 	{
 		pageNum = (null == pageNum)? 0 : pageNum;
 		soundsPerPage = (null == soundsPerPage)? 15 : soundsPerPage;
 		
 		List<SoundRecord> sounds = null;
 		try {
 			sounds = soundService.getObservingSounds(userAlias, pageNum, soundsPerPage);
 		} catch (SoundException e) 
 		{
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
 		}
 		catch(Exception e)
 		{
 			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(("Failed to load sounds for user " + userAlias)).build();
 		}
 		return Response.status(Status.OK).entity(JsonHandler.toJson(sounds)).build();
 	}
 
 }
