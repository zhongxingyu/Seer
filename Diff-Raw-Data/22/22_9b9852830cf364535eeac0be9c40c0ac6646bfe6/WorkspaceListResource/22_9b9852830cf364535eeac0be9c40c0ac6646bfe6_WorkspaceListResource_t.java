 /**
  * 
  */
 package pl.psnc.dl.wf4ever;
 
import java.net.URI;
 import java.rmi.RemoteException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.core.Response.Status;
 
 import pl.psnc.dl.wf4ever.connection.DLibraDataSource;
 import pl.psnc.dlibra.service.DLibraException;
 
 /**
  * @author Piotr Hołubowicz
  * 
  */
 @Path(Constants.WORKSPACES_URL_PART)
 public class WorkspaceListResource {
 
 	@Context
 	HttpServletRequest request;
 
 	@Context
 	private UriInfo uriInfo;
 
 	/**
 	 * Creates new workspace with given WORKSPACE_ID. input: WORKSPACE_ID and
 	 * password. 
 
 	 * @param data text/plain with id in first line and password
 	 * in second.
 	 * @return 201 (Created) when the workspace was successfully created, 400
 	 *         (Bad Request) if the content is malformed 409 (Conflict) if the
 	 *         WORKSPACE_ID is already used
 	 * @throws RemoteException
 	 * @throws DLibraException
 	 */
 	@POST
 	@Consumes("text/plain")
 	public Response createWorkspace(String data) throws RemoteException,
 			DLibraException {
 		DLibraDataSource dLibraDataSource = (DLibraDataSource) request
 				.getAttribute(Constants.DLIBRA_DATA_SOURCE);
 
 		String lines[] = data.split("[\\r\\n]+");
 		if (lines.length < 2) {
 			return Response.status(Status.BAD_REQUEST)
 					.entity("Content is shorter than 2 lines")
 					.header(Constants.CONTENT_TYPE_HEADER_NAME, "text/plain")
 					.build();
 		}
 		String workspaceId = lines[0];
 		String password = lines[1];
 		if (workspaceId.isEmpty()) {
 			return Response.status(Status.BAD_REQUEST)
 					.entity("Workspace id is empty")
 					.header(Constants.CONTENT_TYPE_HEADER_NAME, "text/plain")
 					.build();
 		}
 		// password can be empty
 		dLibraDataSource.createUser(workspaceId, password);
 
		return Response.created(URI.create(uriInfo.getAbsolutePath() + "/" + workspaceId)).build();
 	}
 }
