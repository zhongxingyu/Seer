 package com.photon.phresco.framework.rest.api;
 //import java.io.IOException;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 
 import org.apache.commons.io.IOUtils;
 import org.json.simple.JSONObject;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStreamWriter;
 
 import org.apache.commons.codec.binary.Base64;
 
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.commons.model.Customer;
 import com.photon.phresco.framework.commons.ClientIdentifyFilter;
 import com.photon.phresco.service.client.api.ServiceManager;
 import com.photon.phresco.util.ServiceConstants;
 import com.sun.jersey.api.client.ClientResponse.Status;
 
 @Path(ServiceConstants.REST_API_CUSTOMER)
 public class CustomerService extends RestBase implements ServiceConstants, FrameworkConstants{
 	
 	@GET
 	@Path (REST_API_CUSTOMER_THEME)
 	@Produces (MediaType.APPLICATION_JSON)
 	public Response findCustomer(@QueryParam(REST_QUERY_USERID) String userId, @QueryParam(REST_QUERY_CUSTOMERID) String customerId) {
 		ResponseInfo<JSONObject> responseData = new ResponseInfo<JSONObject>();
 		InputStream fileInputStream = null;
 		try {
 			ClientIdentifyFilter filter = new ClientIdentifyFilter();
 	        ServiceManager serviceManager = CONTEXT_MANAGER_MAP.get(userId);
 			if (serviceManager == null) {
 			ResponseInfo<JSONObject> finalOutput = responseDataEvaluation(responseData, null,
 					UNAUTHORIZED_USER, null);
 				return Response.status(Status.BAD_REQUEST).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER).build();
 			}
 			Customer customer = serviceManager.getCustomer(customerId);
 			//JSONObject jsonObject = filter.processCustomerProperties(customer);
 			fileInputStream = serviceManager.getIcon(customerId);
 			byte[] imgByte = null;
 			imgByte = IOUtils.toByteArray(fileInputStream);
 			byte[] encodedImage = Base64.encodeBase64(imgByte);
 			String encodeImgStr = new String(encodedImage);
 
 			JSONObject themeJsonObject = filter.processCustomerProperties(customer);
 			JSONObject jsonObject = new JSONObject();
 			jsonObject.put(THEME, themeJsonObject);
 			jsonObject.put(LOGO, encodeImgStr);
 			ResponseInfo<JSONObject> finalOutput = responseDataEvaluation(responseData, null, CUSTOMER_THEME_SUCCESS_STATUS, jsonObject);
 			return Response.status(Response.Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 		}
 		catch (Exception e) {
 			ResponseInfo<Customer> finalOutput = responseDataEvaluation(responseData, e, UNABLE_FETCH_CUTOMER_FAILURE_THEME, null);
 			return Response.status(Response.Status.BAD_REQUEST).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 		}
 	}
 	
 	@GET
 	@Path (REST_API_FAVOURITE_ICON)
 	@Produces (MediaType.APPLICATION_JSON)
 	public Response favIcon(@QueryParam(REST_QUERY_USERID) String userId, @QueryParam(REST_QUERY_CUSTOMERID) String customerId) {
 		ResponseInfo<JSONObject> responseData = new ResponseInfo<JSONObject>();
 		InputStream fileInputStream = null;
 		try {
 			ClientIdentifyFilter filter = new ClientIdentifyFilter();
 	        ServiceManager serviceManager = CONTEXT_MANAGER_MAP.get(userId);
 	        if (serviceManager == null) {
 	        	ResponseInfo<JSONObject> finalOutput = responseDataEvaluation(responseData, null,
 					UNAUTHORIZED_USER, null);
 				return Response.status(Status.BAD_REQUEST).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER).build();
 			}
 			Customer customer = serviceManager.getCustomer(customerId);
 			fileInputStream = serviceManager.getFavIcon(customerId);
 			byte[] imgByte = null;
 			imgByte = IOUtils.toByteArray(fileInputStream);
 			byte[] encodedImage = Base64.encodeBase64(imgByte);
 			String encodeImgStr = new String(encodedImage);
 
 			JSONObject themeJsonObject = filter.processCustomerProperties(customer);
 			JSONObject jsonObject = new JSONObject();
 			jsonObject.put(FAV_ICON, encodeImgStr);
 			ResponseInfo<JSONObject> finalOutput = responseDataEvaluation(responseData, null, CUSTOMER_FAVOURITE_ICON_SUCCESS_STATUS, jsonObject);
 			return Response.status(Response.Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 		} catch (Exception e) {
 			ResponseInfo<Customer> finalOutput = responseDataEvaluation(responseData, e, UNABLE_FETCH_FAVOURITE_ICON, null);
 			return Response.status(Response.Status.BAD_REQUEST).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 		}
 	}
 	
 	@GET
	@Path ("REST_API_LOGIN_ICON")
 	@Produces (MediaType.APPLICATION_JSON)
 	public Response loginIcon(@QueryParam(REST_QUERY_USERID) String userId, @QueryParam(REST_QUERY_CUSTOMERID) String customerId) {
 		ResponseInfo<JSONObject> responseData = new ResponseInfo<JSONObject>();
 		InputStream fileInputStream = null;
 		try {
 			ClientIdentifyFilter filter = new ClientIdentifyFilter();
 	        ServiceManager serviceManager = CONTEXT_MANAGER_MAP.get(userId);
 	        if (serviceManager == null) {
 	        	ResponseInfo<JSONObject> finalOutput = responseDataEvaluation(responseData, null,
 					UNAUTHORIZED_USER, null);
 				return Response.status(Status.BAD_REQUEST).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER).build();
 			}
 			Customer customer = serviceManager.getCustomer(customerId);
 			fileInputStream = serviceManager.getLoginIcon(customerId);
 			byte[] imgByte = null;
 			imgByte = IOUtils.toByteArray(fileInputStream);
 			byte[] encodedImage = Base64.encodeBase64(imgByte);
 			String encodeImgStr = new String(encodedImage);
 
 			JSONObject themeJsonObject = filter.processCustomerProperties(customer);
 			JSONObject jsonObject = new JSONObject();
 			jsonObject.put(LOGIN_ICON, encodeImgStr);
 			ResponseInfo<JSONObject> finalOutput = responseDataEvaluation(responseData, null, CUSTOMER_LOGIN_ICON_SUCCESS_STATUS, jsonObject);
 			return Response.status(Response.Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 		} catch (Exception e) {
 			ResponseInfo<Customer> finalOutput = responseDataEvaluation(responseData, e, UNABLE_FETCH_MAIN_LOGO_ICON, null);
 			return Response.status(Response.Status.BAD_REQUEST).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN, ALL_HEADER).build();
 		}
 	}
 	
 }
