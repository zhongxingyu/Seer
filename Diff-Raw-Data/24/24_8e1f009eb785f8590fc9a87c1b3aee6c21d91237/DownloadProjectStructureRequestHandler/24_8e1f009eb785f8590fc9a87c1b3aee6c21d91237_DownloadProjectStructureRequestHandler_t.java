 package icloude.request_handlers;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import icloude.contents.FileContent;
 import icloude.contents.FileTree;
 import icloude.contents.ProjectContent;
 import icloude.requests.AutocompleteRequest;
 import icloude.requests.BaseRequest;
 import icloude.requests.DownloadFileRequest;
 import icloude.requests.DownloadProjectStructureRequest;
 import icloude.responses.BaseResponse;
 import icloude.responses.FileResponse;
 import icloude.responses.ProjectResponse;
 import icloude.responses.StandartResponse;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import storage.Child;
 import storage.Database;
 import storage.DatabaseException;
 import storage.StoringType;
 import storage.project.Project;
 import storage.projectitem.CompositeProjectItem;
 import storage.sourcefile.SourceFile;
 import storage.sourcefile.SourceFileReader;
 
 import com.google.gson.JsonSyntaxException;
 
 /**
  * @author DimaTWL Handling all requests on "rest/downloadprojectstructure" URL:
  *         rest/downloadprojectstructure Method: GET Required response: Project
  */
 @Path("/downloadprojectstructure")
 public class DownloadProjectStructureRequestHandler extends BaseRequestHandler {
 
 	/**
 	 * This method used to handle all GET request on
 	 * "rest/downloadprojectstructure"
 	 * 
 	 * @return the StandartResponse witch will be sent to client
 	 */
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public String get(@QueryParam("json") String json) {
 		return getResponce(json);
 	}
 
 	/**
 	 * Realization of this method expected to convert JSON representation to
 	 * concrete request object.
 	 * 
 	 * @param json
 	 *            is JSON string from client.
 	 * @return concrete request object.
 	 */
 	@Override
 	protected BaseRequest jsonToRequest(String json) throws JsonSyntaxException {
 		return GSON.fromJson(json, DownloadProjectStructureRequest.class);
 	}
 
 	/**
 	 * Realization of this method expected to check if 'requestType' in request
 	 * is allowed on some address.
 	 * 
 	 * @param requestType
 	 *            is 'requestType' field from request.
 	 * @return 'true' if request is allowed and 'false' otherwise.
 	 */
 	@Override
 	protected Boolean requestTypeCheck(String requestType) {
 		return "downloadprojectstructure".equals(requestType);
 	}
 
 	/**
 	 * Realization of this method expected to do all specific staff (save/read
 	 * DB) and generate some response witch will be sent to client.
 	 * 
 	 * @param request
 	 *            is concrete request object.
 	 * @return response witch will be sent to client.
 	 */
 	@Override
 	protected BaseResponse handleRequest(BaseRequest request) {
 		BaseResponse response;
		DownloadProjectStructureRequest castedRequest = (DownloadProjectStructureRequest) request;
 		try {
 			Project project = (Project) Database.get(StoringType.PROJECT,
 					castedRequest.getProjectID());
 			CompositeProjectItem compositeItem = (CompositeProjectItem) Database
 					.get(StoringType.COMPOSITE_PROJECT_ITEM, project.getRootKey());
 			FileTree root = new FileTree(compositeItem.getName(), compositeItem
 					.getItemType().toString(), compositeItem.getKey(),
 					compositeItem.getParentKey(), null, null, null, null,
 					new ArrayList<FileTree>());
 			FileTree tree = buildTree(root, compositeItem);
 			ProjectContent projectcontent = new ProjectContent(project.getKey(), project.getName(), "HARDCODED", new Date(), "HARDCODED", tree);
 			response = new ProjectResponse(request.getRequestID(), true,
 					"Here is your project. Do not use for evil.", projectcontent);
 		} catch (DatabaseException e) {
 			response = new StandartResponse(request.getRequestID(), false,
 					"DB error. " + e.getMessage());
 		} 
 		return response;
 	}
 	
 	private FileTree buildTree(FileTree root, CompositeProjectItem item) throws DatabaseException {
 		List<FileTree> children = new ArrayList<FileTree>();
 		for (Child child : item.getChildren()) {
 			FileTree branch;
 			if (child.getType().equals(StoringType.COMPOSITE_PROJECT_ITEM)) {
 				CompositeProjectItem compositeItem = (CompositeProjectItem) Database
 						.get(StoringType.COMPOSITE_PROJECT_ITEM, child.getKey());
 				branch = new FileTree(compositeItem.getName(), compositeItem
 						.getItemType().toString(), compositeItem.getKey(),
 						compositeItem.getParentKey(), null, null, null, null,
 						new ArrayList<FileTree>());
 				branch = buildTree(branch, compositeItem);
 			} else {
 				SourceFile file = (SourceFile) Database.get(
 						StoringType.SOURCE_FILE, child.getKey());
 				branch = new FileTree(file.getName(), "SOURCE_FILE", file.getKey(),
 						file.getParentKey(), "HARDCODED", new Date(), new Date(), new Long(667),
 						new ArrayList<FileTree>());
 			}
 			children.add(branch);
 		}
 		root.setChildren(children);
 		return root;
 	}
 	
 
 	/**
 	 * Realization of this method expected to check all specific fields
 	 * in concrete request for not null. Check of BaseRequest field is redundant. 
 	 * 
 	 * @param request
 	 *            is concrete request object.
 	 * @return True if ALL specific fields != null
 	 * 		   False otherwise.
 	 */
 	@Override
 	protected Boolean concreteRequestNullCheck(BaseRequest request) {
 		DownloadProjectStructureRequest castedRequest = (DownloadProjectStructureRequest) request;
 		return (null != castedRequest.getProjectID());
 	}
 
 }
