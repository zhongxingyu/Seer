 package icloude.request_handlers;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.Map;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import icloude.requests.BaseRequest;
 import icloude.requests.DownloadCodeRequest;
 import icloude.requests.DownloadProjectStructureRequest;
 import icloude.responses.BaseResponse;
 import icloude.responses.StandartResponse;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import storage.DatabaseException;
import storage.ProjectItem;
 import storage.project.Project;
 import storage.sourcefile.SourceFile;
 import storage.sourcefile.SourceFileReader;
 
 import com.google.gson.JsonSyntaxException;
 
 /**
  * @author DimaTWL 
  * Handling all requests on "rest/downloadcode" 
  * URL: rest/downloadcode 
  * Method: GET 
 * Required response: Project
  */
 @Path("/downloadcode")
 public class DownloadCodeRequestHandler extends BaseRequestHandler {
 
 	/**
 	 * This method used to handle all GET request on
 	 * "rest/downloadcode"
 	 * 
 	 * @return the StandartResponse witch will be sent to client
 	 */
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public String post(@QueryParam("json") String json) {
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
 		return GSON.fromJson(json, DownloadCodeRequest.class);
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
 		return "downloadcode".equals(requestType);
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
 		return new StandartResponse(request.getRequestID(), true,
 				"Request 'Download code' recieved.");
 	}
 	
 	
	private void zipProject(Map<String, ProjectItem> project) throws IOException, DatabaseException {
		ZipOutputStream zipOut = new ZipOutputStream(new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE));
 		for (String key : project.keySet()) {
 			addToZip(key, project, zipOut);
 		} 
 		zipOut.flush();
 		zipOut.close();
 
		System.out.println("Successfully created ");
 	}
 
 	private void addToZip(String key, Map<String, ProjectItem> project, ZipOutputStream zipOut) throws IOException, DatabaseException {
 		ProjectItem currentItem = project.get(key);
 		String fullPath = getFullPath(key, project);
 		zipOut.putNextEntry(new ZipEntry(fullPath));
 		if (currentItem instanceof SourceFile) {
 			SourceFileReader reader = ((SourceFile)currentItem).openForReading();
 			char[] buf = new char[DEFAULT_BUFFER_SIZE];
 			int charsReaded;
 			while ((charsReaded = reader.read(buf)) >= 0) {
 				zipOut.write(new String(buf).getBytes(), 0, charsReaded);
 			}
 			reader.close();
 		}
 
 	}
 	
 	private String getFullPath(String key, Map<String, ProjectItem> project) {
 		String currentItemKey = key;
 		ProjectItem currentItem = project.get(currentItemKey);
 		StringBuilder fullPath = new StringBuilder();
 		if (! (currentItem instanceof SourceFile)) {
 			fullPath.insert(0, '/');
 		}
 		while (! (currentItem instanceof Project)) {
 			fullPath.insert(0, currentItem.getName());
 			fullPath.insert(0, '/');
			currentItemKey = currentItem.getParent();
 			currentItem = project.get(currentItemKey);
 		}
 		fullPath.insert(0, currentItem.getName());
 		fullPath.insert(0, '/');
 		return fullPath.toString();
 	}
 
 }
