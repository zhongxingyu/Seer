 package ddth.dasp.common.api;
 
 public interface IApiHandler {
 
 	public final static String PROP_MODULE = "Module";
 	public final static String PROP_API = "Api";
 
 	public final static String REMOTE_THRIFT = "THRIFT";
 	
 	public final static String RESULT_FIELD_STATUS = "status";
 	public final static String RESULT_FIELD_MESSAGE = "message";
 
 	/* Commons Result Codes */
 	public final static int RESULT_CODE_OK = 200;
 	public final static int RESULT_CODE_INVALID_REQUEST = 400;
 	public final static int RESULT_CODE_DENIED = 403;
 	public final static int RESULT_CODE_NOT_FOUND = 404;
 	public final static int RESULT_CODE_ERROR = 500;
 
 	/**
 	 * Handles an API call.
 	 * 
 	 * @param params
 	 * @param authKey
 	 * @param remoteAddr
 	 * @return
 	 * @throws ApiException
 	 */
 	public Object callApi(Object params, String authKey, String remoteAddr)
 			throws ApiException;
 }
