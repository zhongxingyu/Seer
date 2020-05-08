 package ddth.dasp.common.utils;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ddth.dasp.common.DaspGlobal;
 import ddth.dasp.common.api.IApiGroupHandler;
 import ddth.dasp.common.api.IApiHandler;
 import ddth.dasp.common.osgi.IOsgiBootstrap;
 
 public class ApiUtils {
 
 	private final static Logger LOGGER = LoggerFactory
 			.getLogger(ApiUtils.class);
 
 	/**
 	 * Convenient method to create an API's result map.
 	 * 
 	 * @param status
 	 * @param message
 	 * @return
 	 */
 	public static Map<Object, Object> createApiResult(int status, Object message) {
 		Map<Object, Object> result = new HashMap<Object, Object>();
 		result.put(IApiHandler.RESULT_FIELD_STATUS, status);
 		result.put(IApiHandler.RESULT_FIELD_MESSAGE, message);
 		return result;
 	}
 
 	/**
 	 * Convenient to execute an API.
 	 * 
 	 * @param module
 	 * @param apiName
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static Object executeApi(String moduleName, String functionName,
 			Object apiParams, String authKey, String remoteAddr) {
 		long timestampStart = System.currentTimeMillis();
 		IOsgiBootstrap osgiBootstrap = DaspGlobal.getOsgiBootstrap();
 		Object result;
 		try {
 			Map<String, String> filter = new HashMap<String, String>();
 			filter.put(IApiHandler.PROP_MODULE, moduleName);
 			filter.put(IApiHandler.PROP_API, functionName);
 			IApiHandler apiHandler = osgiBootstrap.getService(
 					IApiHandler.class, filter);
 			if (apiHandler != null) {
 				result = apiHandler.callApi(apiParams, authKey, remoteAddr);
 			} else {
 				filter.remove(IApiHandler.PROP_API);
 				IApiGroupHandler apiGroupHandler = osgiBootstrap.getService(
 						IApiGroupHandler.class, filter);
 				if (apiGroupHandler != null) {
 					result = apiGroupHandler.handleApiCall(functionName,
 							apiParams, authKey, remoteAddr);
 				} else {
 					if ("stats".equals(moduleName) && functionName == null) {
 						// special handler for displaying api statistic
 						// TODO
 						Map<Object, Object> res = ApiUtils.createApiResult(
								IApiHandler.RESULT_CODE_NOT_IMPLEMETED,
 								"No handler for [" + moduleName + "/"
 										+ functionName + "]!");
 						result = res;
 					} else {
 						Map<Object, Object> res = ApiUtils.createApiResult(
								IApiHandler.RESULT_CODE_NOT_IMPLEMETED,
 								"No handler for [" + moduleName + "/"
 										+ functionName + "]!");
 						result = res;
 					}
 				}
 			}
 		} catch (Exception ex) {
 			LOGGER.error(ex.getMessage(), ex);
 			Map<Object, Object> res = ApiUtils.createApiResult(
 					IApiHandler.RESULT_CODE_ERROR, ex.getMessage());
 			Writer writer = new StringWriter();
 			PrintWriter printWriter = new PrintWriter(writer);
 			ex.printStackTrace(printWriter);
 			res.put("stacktrace", writer.toString());
 			result = res;
 			printWriter.close();
 		} finally {
 		}
 		if (result instanceof Map) {
 			Map<Object, Object> temp = (Map<Object, Object>) result;
 			Map<Object, Object> debug = new HashMap<Object, Object>();
 			temp.put("debug", debug);
 
 			long timestampEnd = System.currentTimeMillis();
 			debug.put("execution_time", (timestampEnd - timestampStart));
 		}
 		return result;
 	}
 }
