 package org.siraya.rent.rest;
 
 import javax.ws.rs.ext.ExceptionMapper;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.ext.Provider;
 
 import org.siraya.rent.filter.UserAuthorizeData;
 import org.siraya.rent.pojo.Device;
 import org.siraya.rent.utils.RentException;
 import org.siraya.rent.utils.RentException.RentErrorCode;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.util.HashMap;
 import javax.ws.rs.core.MediaType;
 
 @Provider
 @Component
 public class RentExceptionExceptionMapper implements ExceptionMapper<RentException> {
     private static Logger logger = LoggerFactory.getLogger(RentExceptionExceptionMapper.class);
 	@Autowired
 	private CookieUtils cookieUtils;
 	@Autowired
 	private UserAuthorizeData userAuthorizeData;
     public RentExceptionExceptionMapper(){
 
 	}
 	
     /**
      * map RentException to standard error message.
      */
 	public  Response toResponse(RentException exception) {
 		logger.debug("extract exception to response");
 		HashMap<String, String> response = new HashMap<String, String>();
 		RentErrorCode code = exception.getErrorCode();
 		response.put("errorMsg", exception.getMessage());
 		response.put("errorCode",
 				Integer.toString(code.getStatus()));
 
 		Response.Status status = mapToHttpStatusCode(code);
 		logger.error("error",exception);
 		Response.ResponseBuilder responseBuilder = Response.status(status).type(MediaType.APPLICATION_JSON);
 
 		if (code == RentException.RentErrorCode.ErrorDeviceNotFound){
 			// remove user part of device cookie.
 			this.userAuthorizeData.signOff();
 		}
 		return responseBuilder.entity(response).build();
 	}
 	
 	/**
 	 * map rent error code to http status code
 	 * @param code
 	 * @return
 	 */
 	private static Response.Status mapToHttpStatusCode(RentErrorCode code){
 		switch (code.getStatus()) {
 		case 4:// ErrorNotFound
 		case 8: // ErrorRemoved
 		case 13: // ErrorNullDeviceId
 		case 18: // ErrorDeviceNotFound
 			return Response.Status.NOT_FOUND;
 		case 12://ErrorInvalidParameter
 			return Response.Status.BAD_REQUEST;
 		case 15: //ErrorPermissionDeny
 			return Response.Status.FORBIDDEN;
 		default:
 			return Response.Status.INTERNAL_SERVER_ERROR;
 		}
 	}
 }
