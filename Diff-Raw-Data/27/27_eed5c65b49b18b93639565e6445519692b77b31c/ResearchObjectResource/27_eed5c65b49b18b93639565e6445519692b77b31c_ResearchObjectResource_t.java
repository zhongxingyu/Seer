 package pl.psnc.dl.wf4ever;
 
 import java.io.IOException;
 import java.net.URI;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriInfo;
 import javax.xml.transform.TransformerException;
 
 import org.apache.log4j.Logger;
 
 import pl.psnc.dl.wf4ever.dlibra.DLibraDataSource;
 import pl.psnc.dlibra.metadata.PublicationInfo;
 import pl.psnc.dlibra.service.DLibraException;
 
 import com.sun.jersey.core.header.ContentDisposition;
 
 /**
  * 
  * @author nowakm
  * 
  */
 @Path(Constants.WORKSPACES_URL_PART + "/{W_ID}/"
 		+ Constants.RESEARCH_OBJECTS_URL_PART + "/{RO_ID}")
 public class ResearchObjectResource
 {
 
 	@SuppressWarnings("unused")
 	private final static Logger logger = Logger
 			.getLogger(ResearchObjectResource.class);
 
 	@Context
 	HttpServletRequest request;
 
 	@Context
 	UriInfo uriInfo;
 
 
 	/**
 	 * Returns list of versions of this research object.
 	 * 
 	 * @param workspaceId
 	 *            identifier of a workspace in the RO SRS
 	 * @param researchObjectId
 	 *            RO identifier - defined by the user
 	 * @return 200 (OK) response code with a rdf file in response body
 	 *         containing OAI-ORE aggreagates tags.
 	 * @throws RemoteException
 	 * @throws DLibraException
 	 * @throws TransformerException
 	 */
 	@GET
 	@Produces("application/rdf+xml")
 	public Response getListOfVersions(@PathParam("W_ID") String workspaceId,
 			@PathParam("RO_ID") String researchObjectId)
 		throws RemoteException, DLibraException, TransformerException
 	{
 		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
 				.getAttribute(Constants.DLIBRA_DATA_SOURCE);
 		List<PublicationInfo> list = dLibraDataSource.getPublicationsHelper()
 				.listPublicationsInGroup(researchObjectId);
 
 		List<String> links = new ArrayList<String>(list.size());
 
 		for (PublicationInfo info : list) {
 			links.add(uriInfo.getAbsolutePath().resolve(info.getLabel())
 					.toString());
 		}
 
 		String responseBody = RdfBuilder.serializeResource(RdfBuilder
 				.createCollection(uriInfo.getAbsolutePath().toString(), links));
 
 		ContentDisposition cd = ContentDisposition.type("application/rdf+xml")
 				.fileName(researchObjectId + ".rdf").build();
 
 		return Response.ok().entity(responseBody)
 				.header(Constants.CONTENT_DISPOSITION_HEADER_NAME, cd).build();
 	}
 
 
 	/**
 	 * Creates new version. Input is RO_VERSION_ID and optional URI of the base
 	 * version that should be used to create a new version.
 	 * 
 	 * @param workspaceId
 	 * @param researchObjectId
 	 * @param data
 	 *            Input format is text/plain with RO_VERSION_ID in first line
 	 *            and base version URI in second (optional).
 	 * @return 201 (Created) if the version was created, 409 (Conflict) if
 	 *         version with given RO_VERSION_ID already exists
 	 * @throws DLibraException
 	 * @throws IOException
 	 * @throws TransformerException
 	 * @throws SAXException 
 	 */
 	@POST
 	@Consumes("text/plain")
 	public Response createVersion(@PathParam("W_ID") String workspaceId,
 			@PathParam("RO_ID") String researchObjectId, String data)
 		throws DLibraException, IOException, TransformerException
 	{
 		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
 				.getAttribute(Constants.DLIBRA_DATA_SOURCE);
 
 		String lines[] = data.split("[\\r\\n]+");
 		if (lines.length < 1) {
 			return Response.status(Status.BAD_REQUEST)
 					.entity("Content is shorter than 2 lines")
 					.header(Constants.CONTENT_TYPE_HEADER_NAME, "text/plain")
 					.build();
 		}
 		String version = lines[0];
 		String baseVersionUri = (lines.length > 1 ? lines[1] : null);
 		String baseVersion = null;
 		if (baseVersionUri != null && !baseVersionUri.isEmpty()) {
 			// remove "/" from the end of URI
 			if (baseVersionUri.lastIndexOf("/") == baseVersionUri.length() - 1) {
 				baseVersionUri = baseVersionUri.substring(0,
 					baseVersionUri.length() - 1);
 			}
 
 			// check if this is a correct URI
 			if (!baseVersionUri.contains(uriInfo.getPath())) {
 				return Response
 						.status(Status.BAD_REQUEST)
 						.entity("Bad base version URI")
 						.header(Constants.CONTENT_TYPE_HEADER_NAME,
 							"text/plain").build();
 			}
 
 			baseVersion = baseVersionUri.substring(baseVersionUri
 					.indexOf(uriInfo.getPath())
 					+ uriInfo.getPath().length()
 					+ 1);
 		}
 
		String versionUri = uriInfo.getAbsolutePath().resolve(version).toString();
 		dLibraDataSource.getPublicationsHelper().createPublication(
 			researchObjectId, version, baseVersion, versionUri);
 
 		return Response.created(
			URI.create(uriInfo.getAbsolutePath().resolve(version).toString())).build();
 	}
 
 
 	/**
 	 * Deletes the research object.
 	 * 
 	 * @param workspaceId
 	 *            identifier of a workspace in the RO SRS
 	 * @param researchObjectId
 	 *            RO identifier - defined by the user
 	 * @throws RemoteException
 	 * @throws DLibraException
 	 */
 	@DELETE
 	public void deleteResearchObject(@PathParam("W_ID") String workspaceId,
 			@PathParam("RO_ID") String researchObjectId)
 		throws RemoteException, DLibraException
 	{
 		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
 				.getAttribute(Constants.DLIBRA_DATA_SOURCE);
 
 		dLibraDataSource.getPublicationsHelper().deleteGroupPublication(
 			researchObjectId);
 
 	}
 }
