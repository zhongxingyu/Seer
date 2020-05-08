 /**
  * 
  */
 package icloude.frontend_backend.request_handlers;
 
 import icloude.frontend_backend.requests.BaseRequest;
 import icloude.frontend_backend.responses.BaseResponse;
 import icloude.frontend_backend.responses.StandartResponse;
 import icloude.helpers.Logger;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonSyntaxException;
 
 /**
  * @author DimaTWL 
  * Represents some base behavior and interface for all request
  *         handlers
  */
 public abstract class BaseRequestHandler {
 	/**
 	 * This field used to do all JSON staff.
 	 */
 	public final static Gson GSON = new Gson();
 	
 	/**
 	 * This field used to determine current protocol version.
 	 */
 	public final static Integer PROTOCOL_VERSION = 4;
 	
 	/**
 	 * This field used to determine size for all buffers.
 	 */
 	public final static Integer DEFAULT_BUFFER_SIZE = 1024;
 
 	/**
 	 * Realization of this method expected to convert JSON representation to
 	 * concrete request object.
 	 * 
 	 * @param json
 	 *            is JSON string from client.
 	 * @return concrete request object.
 	 */
 	protected abstract BaseRequest jsonToRequest(String json)
 			throws JsonSyntaxException;
 
 	/**
 	 * Realization of this method expected to check if 'requestType' in request
 	 * is allowed on some address.
 	 * 
 	 * @param requestType
 	 *            is 'requestType' field from request.
 	 * @return 'true' if request is allowed and 'false' otherwise.
 	 */
 	protected abstract Boolean requestTypeCheck(String requestType);
 
 	/**
 	 * Realization of this method expected to do all specific staff (save/read
 	 * DB) and generate some response witch will be sent to client.
 	 * 
 	 * @param request
 	 *            is concrete request object.
 	 * @return response witch will be sent to client.
 	 */
 	protected abstract BaseResponse handleRequest(BaseRequest request);
 	
 	/**
 	 * Realization of this method expected to check all specific fields
 	 * in concrete request for not null. Check of BaseRequest field is redundant. 
 	 * 
 	 * @param request
 	 *            is concrete request object.
 	 * @return True if ALL specific fields != null
 	 * 		   False otherwise.
 	 */
 	protected abstract Boolean concreteRequestNullCheck(BaseRequest request);
 	
 
 	/**
 	 * This method should be called when request received.
 	 * 
 	 * @param json
 	 *            is JSON string from client.
 	 * @return response in JSON witch will be sent to client.
 	 */
 	protected final String getResponce(String json) {
 		BaseResponse response;
 		Logger.toLog("From:" + json);
 		if (null == json) {
 			response = new StandartResponse("Error", false,
 					"No 'json' parameter in http request.");
 		} else
 			try {
 				BaseRequest fromJSON = jsonToRequest(json);
 				if (! requestNullCheck(fromJSON)){
 					response = new StandartResponse(fromJSON.getRequestID(),
 							false, "Some fields in request are not presented.");
 				} else if (! protocolVersionCheck(fromJSON.getProtocolVersion())){
 					response = new StandartResponse(fromJSON.getRequestID(),
 							false, "Protocol version mismatch. Current version is " + PROTOCOL_VERSION.toString());
 				} else if (! requestTypeCheck(fromJSON.getRequestType())) {
 					response = new StandartResponse(fromJSON.getRequestID(),
 							false, "Request type mismatch.");
 				} else {
 					response = handleRequest(fromJSON);
 				}
 			} catch (JsonSyntaxException e) {
 				response = new StandartResponse("error", false,
 						"Bad JSON syntax." + e.getMessage());
 			} catch (Exception e) {
 				response = new StandartResponse("error", false,
						"Internal error: " + e.getMessage());
 			}
 		Logger.toLog("To:" + GSON.toJson(response));
 		return GSON.toJson(response);
 	}
 	
 	
 	protected Boolean protocolVersionCheck(Integer version) {
 		return PROTOCOL_VERSION.equals(version);
 	}
 	
 	private Boolean baseRequestNullCheck(BaseRequest request) {
 		return (null != request.getProtocolVersion()) &&
 				(null != request.getRequestID()) &&
 				(null != request.getRequestType()) &&
 				(null != request.getUserID());
 	}
 	
 	protected Boolean requestNullCheck(BaseRequest request) {
 		return baseRequestNullCheck(request) && 
 				concreteRequestNullCheck(request);
 	}
 
 }
