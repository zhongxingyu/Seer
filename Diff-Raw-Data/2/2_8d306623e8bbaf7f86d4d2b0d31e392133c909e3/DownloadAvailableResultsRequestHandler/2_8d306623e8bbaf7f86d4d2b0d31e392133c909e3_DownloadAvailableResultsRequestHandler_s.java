 package icloude.frontend_backend.request_handlers;
 
 import icloude.frontend_backend.requests.BaseRequest;
 import icloude.frontend_backend.requests.DownloadAvailableResultsRequest;
 import icloude.frontend_backend.requests.DownloadProjectListRequest;
 import icloude.frontend_backend.responses.BaseResponse;
 import icloude.frontend_backend.responses.ResultsResponse;
 import icloude.frontend_backend.responses.StandartResponse;
 
 import java.util.ArrayList;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import storage.Database;
 import storage.DatabaseException;
 import storage.StoringType;
 import storage.taskqueue.BuildAndRunTask;
 import storage.taskqueue.TaskStatus;
 
 import com.google.gson.JsonSyntaxException;
 
 /**
  * @author DimaTWL 
  * Handling all requests on "rest/downloadavailableresults" 
  * URL: rest/downloadavailableresults 
  * Method: GET 
  * Required response: Project list
  */
 @Path("/downloadavailableresults")
 public class DownloadAvailableResultsRequestHandler extends BaseRequestHandler {
 
 	/**
 	 * This method used to handle all GET request on "rest/downloadavailableresults"
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
		return GSON.fromJson(json, DownloadProjectListRequest.class);
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
 		return "downloadavailableresults".equals(requestType);
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
 		BaseResponse response = null;
 		DownloadAvailableResultsRequest castedRequest = (DownloadAvailableResultsRequest) request;
 		BuildAndRunTask task = null;
 		try {
 			task = (BuildAndRunTask) Database.get(
 					StoringType.BUILD_AND_RUN_TASK, TaskStatus.FINISHED);
 			if (task != null) {
 				response = new ResultsResponse(request.getRequestID(), true,
 						"Result available.", task.getResult());
 			} else {
 				response = new ResultsResponse(request.getRequestID(), true,
 						"Result unavailable.", new ArrayList<String>());
 			}
 		} catch (DatabaseException e) {
 			response = new StandartResponse(request.getRequestID(), false,
 					"DB error. " + e.getMessage());
 		}
 		return response;
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
 	protected Boolean concreteRequestNullCheck(BaseRequest request){
 		return true;
 	}
 }
