 package org.siraya.rent.rest;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.Path;
 
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.core.Response;
 
 import org.siraya.rent.pojo.User;
 import org.siraya.rent.pojo.Device;
 import org.siraya.rent.user.service.IMobileAuthService;
 import org.siraya.rent.user.service.IUserService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import java.util.Map;
 import javax.ws.rs.core.NewCookie;
 @Component
 @Path("/user")
 public class UserRestApi {
 	@Autowired
 	private IUserService userService;
 	@Autowired
 	private IMobileAuthService mobileAuthService;
     private static Logger logger = LoggerFactory.getLogger(UserRestApi.class);
 
 
    
 	/**
      * create new device and assign a device id for it.
      * @param cc
      * @param mobilePhone
      * @return
      */
 	@POST
 	@Consumes("application/json")
 	@Path("/new_device")
 	public Response newDevice(Map<String,String> request){
 		try {
 			String cc=request.get("country_code");
 			String mobilePhone=request.get("mobile_phone");
 			User user =userService.newUserByMobileNumber(Integer.parseInt(cc), mobilePhone);
 			Device device = new Device();
 			device.setUser(user);
 			userService.newDevice(device);
 			
 			//
 			// set device id into cookie
 			//
 			NewCookie deviceCookie = new NewCookie(
 					"D",
 					device.getId(), 
 					"/", 
 					null,
 					1,
 					"no comment",
 					1073741823, // maxAge max int value/2
 					false);
 			return Response.status(200).entity(device.getId()).cookie(deviceCookie).build();
 		}catch(java.lang.NumberFormatException e){
 			logger.error("country code or mobile number must be number",e);
 			return Response.status(401).entity("country code or mobile number must be number").build();						
 		}catch(Exception e) {
 			logger.error("exception "+e.getMessage(),e);
 			return Response.status(500).entity(e.getMessage()).build();			
 		}
 	}
 	
 	/**
 	 * send auth message
 	 * @param deviceId
 	 * @return
 	 */
 	@POST
 	@Consumes("application/json")
 	@Path("/send_mobile_auth_message")
 	public Response sendMobileAuthMessage(@HeaderParam("DEVICE_ID") String deviceId,Map<String,String> request){
 		try {
 			mobileAuthService.sendAuthMessage(deviceId);
 			return Response.status(200).entity("OK").build();
 		}catch(Exception e) {
 			logger.error("exception "+e.getMessage(),e);
 			return Response.status(500).entity(e.getMessage()).build();		
 		}
 	}
 	
 	/**
 	 * verify auth code
 	 * @param deviceId
 	 * @param authCode
 	 * @return
 	 */
 	@POST
 	@Consumes("application/json")
 	@Path("/verify_mobile_auth_code")
 	public Response verifyMobileAuthCode(@HeaderParam("DEVICE_ID") String deviceId,Map<String,String> request) {
 		try {
 			String authCode = request.get("auth_code");
 			mobileAuthService.verifyAuthCode(deviceId, authCode);
 			return Response.status(200).entity("OK").build();
 		} catch (Exception e) {
 			logger.error("exception " + e.getMessage(), e);
 			return Response.status(500).entity(e.getMessage()).build();
 		}
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
 		return Response.status(200).entity("OK").cookie(lastVisited).build();
 	}
 
 	void setUserService(IUserService userService) {
 		this.userService = userService;
 	}
 
 
 	void setMobileAuthService(IMobileAuthService mobileAuthService) {
 		this.mobileAuthService = mobileAuthService;
 	}
 }
