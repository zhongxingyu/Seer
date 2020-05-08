 package com.server.cx.webservice.rs.server;
 
 import com.cl.cx.platform.dto.OperationDescription;
 import com.cl.cx.platform.dto.SMSDTO;
 import com.server.cx.model.OperationResult;
 import com.server.cx.exception.SystemException;
 import com.server.cx.service.cx.SmsMessageService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 /**
  * User: yanjianzou
  * Date: 12-8-9
  * Time: 上午9:29
  * FileName:SMSResources
  */
 @Component
 @Path("{imsi}/sms")
 @Consumes({MediaType.APPLICATION_JSON})
 @Produces({MediaType.APPLICATION_JSON})
 public class SMSResources extends OperationResources {
 
     public static final Logger LOGGER = LoggerFactory.getLogger(SMSResources.class);
     @Autowired
     private SmsMessageService smsMessageService;
 
     @POST
     public Response create(@PathParam("imsi")String imsi,SMSDTO smsdto){
         operationDescription = new OperationDescription();
         try {
             OperationResult operationResult;
             operationResult = smsMessageService.inviteFriends(imsi,smsdto.getPhoneNos(),smsdto.getContent());
             updateOperationDescription(operationResult);
         } catch (SystemException e) {
             errorMessage(e);
             actionName = "inviteFriends";
             operationDescription.setActionName(actionName);
             operationDescription.setErrorCode(403);
             return Response.ok(operationDescription).build();
         }
         return Response.ok(operationDescription).build();
     }
 
 }
