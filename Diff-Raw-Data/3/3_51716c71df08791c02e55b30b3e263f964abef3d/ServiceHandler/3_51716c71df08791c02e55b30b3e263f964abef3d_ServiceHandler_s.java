 package com.orange.place.api.service;
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.sf.json.JSONException;
 
 import me.prettyprint.hector.api.exceptions.HectorException;
 
 import com.orange.common.cassandra.CassandraClient;
 import com.orange.place.api.PlaceAPIServer;
 import com.orange.place.constant.DBConstants;
 import com.orange.place.constant.ErrorCode;
 import com.orange.place.constant.ServiceConstant;
 
 public class ServiceHandler {
 
 	public static final CassandraClient cassandraClient = new CassandraClient(
 			DBConstants.SERVER, DBConstants.CLUSTERNAME, DBConstants.KEYSPACE);
 
 	private static final Logger log = Logger.getLogger(PlaceAPIServer.class
 			.getName());
 
 	public static ServiceHandler getServiceHandler() {
 		ServiceHandler handler = new ServiceHandler();
 		return handler;
 	}
 
 	public void handlRequest(HttpServletRequest request,
 			HttpServletResponse response) {
 
 		printRequest(request);
 
 		String method = request.getParameter(ServiceConstant.METHOD);
 		CommonService obj = null;
 		
 		try {
 			obj = CommonService.createServiceObjectByMethod(method);
 		} catch (InstantiationException e1) {
 			log
 					.severe("<handlRequest> but exception while create service object for method("
 							+ method + "), exception=" + e1.toString());
 			e1.printStackTrace();
 		} catch (IllegalAccessException e1) {
 			log
 					.severe("<handlRequest> but exception while create service object for method("
 							+ method + "), exception=" + e1.toString());
 			e1.printStackTrace();
 		}
 
 		try {
 
 			if (obj == null) {
 				sendResponseByErrorCode(response,
 						ErrorCode.ERROR_PARA_METHOD_NOT_FOUND);
 				return;
 			}
 
 			obj.setCassandraClient(cassandraClient);
 			obj.setRequest(request);
 			
 			if (!obj.validateSecurity(request)) {
 				sendResponseByErrorCode(response,
 						ErrorCode.ERROR_INVALID_SECURITY);
 				return;
 			}
 
 			// parse request parameters
			sendResponseByErrorCode(response, obj.resultCode);
 			if (!obj.setDataFromRequest(request)) {
 				return;
 			}
 
 			// print parameters
 			obj.printData();
 
 			// handle request
 			obj.handleData();
 		} catch (HectorException e) {
 			obj.resultCode = ErrorCode.ERROR_CASSANDRA;
 			log.severe("catch DB exception=" + e.toString());
 			e.printStackTrace();
 		} catch (JSONException e) {
 			obj.resultCode = ErrorCode.ERROR_JSON;
 			log.severe("catch JSON exception=" + e.toString());
 			e.printStackTrace();
 		} catch (Exception e) {
 			obj.resultCode = ErrorCode.ERROR_SYSTEM;
 			log.severe("catch general exception=" + e.toString());
 			e.printStackTrace();
 		} finally {
 		}
 
 		String responseData = obj.getResponseString();
 
 		// send back response
 		sendResponse(response, responseData);
 
 	}
 
 	void printRequest(HttpServletRequest request) {
 		log.info("[RECV] request = " + request.getQueryString());
 	}
 
 	void printResponse(HttpServletResponse reponse, String responseData) {
 		log.info("[SEND] response data = " + responseData);
 	}
 
 	void sendResponse(HttpServletResponse response, String responseData) {
 		printResponse(response, responseData);
 		response.setContentType("application/json; charset=utf-8");
 		try {
 			response.getWriter().write(responseData);
 			response.getWriter().flush();
 		} catch (IOException e) {
 			log.severe("sendResponse, catch exception=" + e.toString());
 		}
 	}
 
 	void sendResponseByErrorCode(HttpServletResponse response, int errorCode) {
 		String resultString = ErrorCode.getJSONByErrorCode(errorCode);
 		sendResponse(response, resultString);
 	}
 }
