 package com.github.tbertell.openchannel;
 
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.GenericEntity;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriInfo;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.github.tbertell.openchannel.channel.model.ChannelVariabilityModel;
 import com.github.tbertell.openchannel.response.ChannelResponse;
 import com.github.tbertell.openchannel.response.ListChannelsResponse;
 
 @Path("/")
 public class ChannelResource {
 
 	@Autowired
 	private ChannelManager channelManager;
 
 	@Context
 	UriInfo uriInfo;
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(ChannelResource.class);
 
 	@GET
 	@Path("/{channelId}")
 	@Produces(MediaType.APPLICATION_XML)
 	public String getChannel(@PathParam("channelId") String channelId) {
 
 		ChannelVariabilityModel model = channelManager.getChannel(channelId);
 
 		if (model == null) {
 			throw new WebApplicationException(Status.NOT_FOUND);
 		}
 		JAXBContext context;
 		try {
 			context = JAXBContext.newInstance(ChannelVariabilityModel.class);
 			Marshaller marshaller = context.createMarshaller();
 			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 			marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, createSchemaLocation(channelId));
 			Writer writer = new StringWriter();
 			marshaller.marshal(model, writer);
 
 			return writer.toString();
 		} catch (JAXBException e) {
 			LOGGER.error("getChannel failed", e);
 			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
 		}
 	}
 
 	@GET
 	@Path("/")
 	@Produces(MediaType.APPLICATION_XML)
 	public Response listChannels() {
 
 		List<ChannelVariabilityModel> modelList = channelManager.listChannels();
 
 		if (modelList == null || modelList.isEmpty()) {
 			return Response.status(Status.NOT_FOUND).build();
 		}
 		ListChannelsResponse listResponse = convertListToListChannelResponse(modelList);
 
 		final GenericEntity<ListChannelsResponse> entity = new GenericEntity<ListChannelsResponse>(listResponse) {
 		};
 
 		return Response.ok().entity(entity).build();
 	}
 
 	@PUT
 	@Produces(MediaType.APPLICATION_XML)
 	@Consumes(MediaType.APPLICATION_XML)
 	@Path("/{channelId}")
 	public Response updateChannel(@PathParam("channelId") String channelId, ChannelVariabilityModel model) {
 
 		try {
 			channelManager.updateChannel(channelId, model);
 		} catch (IllegalArgumentException iae) {
 			LOGGER.error("non valid channel " + model, iae);
			throw new WebApplicationException(Status.BAD_REQUEST);
 		} catch (Exception e) {
 			LOGGER.error("updateChannel failed", e);
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
 		}
 
 		return Response.ok().build();
 	}
 
 	private ListChannelsResponse convertListToListChannelResponse(List<ChannelVariabilityModel> list) {
 
 		ListChannelsResponse response = new ListChannelsResponse();
 
 		for (ChannelVariabilityModel model : list) {
 			response.addChannelResponse(new ChannelResponse(uriInfo.getAbsolutePath().toString() + model.getId(), model
 					.getDescription()));
 		}
 		return response;
 	}
 
 	private String createSchemaLocation(String channelId) {
 
 		String baseUrl = uriInfo.getBaseUri().toString();
 		// replace /channels/ with /schemas/ +channelId.xsd
 		String schemaLocation = baseUrl.replace("channels", "schemas/" + channelId + ".xsd");
 
 		return schemaLocation;
 	}
 
 }
