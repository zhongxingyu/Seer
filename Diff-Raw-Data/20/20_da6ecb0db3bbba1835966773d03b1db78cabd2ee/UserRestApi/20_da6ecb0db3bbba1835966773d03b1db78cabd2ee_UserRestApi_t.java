 package org.siraya.rent.rest;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.Produces;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.Path;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.core.Response;
 
 import org.siraya.rent.pojo.User;
 import org.siraya.rent.utils.RentException;
 import org.siraya.rent.utils.RentException.RentErrorCode;
 import org.siraya.rent.pojo.Device;
 import org.siraya.rent.user.service.IMobileAuthService;
 import org.siraya.rent.user.service.IUserService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import java.util.Map;
 import java.util.HashMap;
 import javax.ws.rs.core.NewCookie;
 import java.net.HttpURLConnection;
 @Component
 @Path("/user")
 public class UserRestApi {
 	@Autowired
 	private IUserService userService;
 	@Autowired
 	private IMobileAuthService mobileAuthService;
     private static Logger logger = LoggerFactory.getLogger(UserRestApi.class);
     private Device device;
     private static Map<String,String> OK;
     public UserRestApi (){
     	if (OK == null) {
     		OK = new HashMap<String,String>();
     		OK.put("status", "SUCCESS");
     	}
     }
     @POST
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response post(@HeaderParam("DEVICE_ID") String deviceId,
 			@HeaderParam("USER_ID") String userId,
			Map<String,Object> request) throws Exception{
 		logger.debug("call new device");
 		Response response =  this.newDevice(deviceId,userId,request);
 		if (response.getStatus() == HttpURLConnection.HTTP_OK) {
 			//
 			// send mobile auth message
 			//
 			Response response2 = this.sendMobileAuthMessage(device.getId(),device.getUserId());
 			logger.debug("rebuild response");
 			response = Response.fromResponse(response).status(response2.getStatus()).build();
 		}
 		return response;
 	}
 
 	@DELETE
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response delete(@HeaderParam("DEVICE_ID") String deviceId,
 			@HeaderParam("USER_ID") String userId) throws Exception{
 		device = new Device();
 		device.setUserId(userId);
 		device.setId(deviceId);
 		userService.removeDevice(device);
 		return Response.status(HttpURLConnection.HTTP_OK).entity(OK).build();		
 	}
 	
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response get(@HeaderParam("DEVICE_ID") String deviceId,
 			@HeaderParam("USER_ID") String userId) throws Exception{
 		logger.debug("call new device");
 		device = new Device();
 		if (deviceId == null || userId == null) {	
 			throw new RentException(RentErrorCode.ErrorNotFound, "device id or user id is null");
 		}
 
 		device.setUserId(userId);
 		device.setId(deviceId);
 		device = userService.getDevice(device);
 		return Response.status(HttpURLConnection.HTTP_OK).entity(device).build();
 	}
 	/**
      * create new device and assign a device id for it.
      * @param cc
      * @param mobilePhone
      * @return
      */
 	@POST
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/new_device")
 	public Response newDevice(@HeaderParam("DEVICE_ID") String deviceId,
 			@HeaderParam("USER_ID") String userId,
			Map<String,Object> request) throws Exception{
 		HashMap<String,String> response = new HashMap<String,String>();
 		try {
			String cc=(String)request.get("countryCode");
			String mobilePhone=(String)request.get("mobilePhone");
 
 			User user =userService.newUserByMobileNumber(Integer.parseInt(cc), mobilePhone);
 			device = new Device();
 			device.setId(deviceId);
 			device.setUser(user);
 			device = userService.newDevice(device);
 			
 			//
 			// set device id into cookie
 			//
 
 			NewCookie deviceCookie = this.createDeviceCookie(device);
 			
 			return Response.status(HttpURLConnection.HTTP_OK).entity(device).cookie(deviceCookie)
 					.build();
 		}catch(java.lang.NumberFormatException e){
 			throw new RentException(RentErrorCode.ErrorInvalidParameter, "country code or mobile number must be number");
 		}
 	}
 
 	/**
 	 * create device cookie
 	 * @param device
 	 * @return
 	 */
 	private NewCookie createDeviceCookie(Device device) {
 		String value= device.getId()+":"+device.getUserId();
 		NewCookie deviceCookie = new NewCookie("D", value, "/",
 				null, 1, "no comment", 1073741823, // maxAge max int value/2
 				false);
 		return deviceCookie;
 	}
 	
 
 	/**
 	 * send auth message
 	 * @param deviceId
 	 * @return
 	 */
 	@POST
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/send_mobile_auth_message")
 	public Response sendMobileAuthMessage(@HeaderParam("DEVICE_ID") String deviceId,
 			@HeaderParam("USER_ID") String userId) throws Exception{
 		mobileAuthService.sendAuthMessage(deviceId,userId);
 		return Response.status(HttpURLConnection.HTTP_OK).entity(OK).build();
 
 	}
 	
 	/**
 	 * verify auth code
 	 * @param deviceId
 	 * @param authCode
 	 * @return
 	 */
 	@PUT
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/verify_mobile_auth_code")
 	public Response verifyMobileAuthCode(@HeaderParam("DEVICE_ID") String deviceId,
 			@HeaderParam("USER_ID") String userId,
 			Map<String,Object> request) throws Exception{
 		String authCode = (String)request.get("authCode");
 		mobileAuthService.verifyAuthCode(deviceId, userId, authCode);
 		return Response.status(HttpURLConnection.HTTP_OK).entity(OK).build();
 	}
 	
 	@GET
 	@Path("/test")
 	public Response test(@HeaderParam("code") String code){
 		System.out.println("code is "+code);
 		javax.ws.rs.core.NewCookie lastVisited = new javax.ws.rs.core.NewCookie(
 				"lastVisited",
 				"testvalue", 
 				"/", 
 				null,
 				1,
 				"no comment",
 				1073741823, // maxAge max int value/2
 				false);
 		return Response.status(HttpURLConnection.HTTP_OK).entity("OK").cookie(lastVisited).build();
 	}
 
 	void setUserService(IUserService userService) {
 		this.userService = userService;
 	}
 
 
 	void setMobileAuthService(IMobileAuthService mobileAuthService) {
 		this.mobileAuthService = mobileAuthService;
 	}
 }
