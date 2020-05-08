 package com.server.cx.webservice.rs.server;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import com.cl.cx.platform.dto.DataPage;
 import com.cl.cx.platform.dto.OperationDescription;
 import com.server.cx.exception.InvalidParameterException;
 import com.server.cx.exception.MoneyNotEnoughException;
 import com.server.cx.exception.UserHasSubscribedException;
 import com.server.cx.service.cx.HolidayTypeService;
 import com.server.cx.service.cx.StatusTypeService;
 import com.server.cx.service.cx.UserCustomTypeService;
 import com.server.cx.service.cx.UserSubscribeGraphicItemService;
 import com.server.cx.service.cx.UserSubscribeTypeService;
 import com.server.cx.util.ObjectFactory;
 import com.server.cx.util.business.ValidationUtil;
 
 @Component
 @Path("{imsi}/subscribe")
 @Consumes({MediaType.APPLICATION_JSON})
 @Produces({MediaType.APPLICATION_JSON})
 public class SubscribeResource {
     private static final Logger LOGGER = LoggerFactory.getLogger(SubscribeResource.class);
 
     @Autowired
     private HolidayTypeService holidayTypeService;
 
     @Autowired
     private StatusTypeService statusTypeService;
 
     @Autowired
     private UserCustomTypeService userCustomTypeService;
 
     @Autowired
     private UserSubscribeTypeService userSubscribeTypeService;
     
     @Autowired
     private UserSubscribeGraphicItemService userSubscribeGraphicItemService;
 
     @PUT
     public Response subscribeFunctionType(@PathParam("imsi") String imsi, @QueryParam("type") String type) {
         LOGGER.info("Into subscribeFunctionType imsi = " + imsi + "  type = " + type);
 
         try {
             ValidationUtil.checkParametersNotNull(imsi);
             if ("holiday".equals(type)) {
                 DataPage dataPage = holidayTypeService.subscribeAndQueryHoliayTypes(imsi);
                 return Response.ok(dataPage).build();
 
             } else if ("status".equals(type)) {
                 DataPage dataPage = statusTypeService.subscribeAndQueryStatusTypes(imsi);
                 return Response.ok(dataPage).build();
 
             } else if ("custom".equals(type)) {
                 DataPage dataPage = userCustomTypeService.subscribeAndQueryCustomTypes(imsi);
                 return Response.ok(dataPage).build();
             }
             OperationDescription operationDescription = ObjectFactory.buildErrorOperationDescription(
                 Response.Status.BAD_REQUEST.getStatusCode(), "subscribeFunctionType", "请求参数异常");
             return Response.ok(operationDescription).build();
 
         } catch (InvalidParameterException e) {
             LOGGER.error("subscribeFunctionType InvalidParameterException error", e);
             OperationDescription operationDescription = ObjectFactory.buildErrorOperationDescription(
                Response.Status.BAD_REQUEST.getStatusCode(), "subscribeFunctionType", e.getMessage());
             return Response.ok(operationDescription).build();
 
         } catch (MoneyNotEnoughException e) {
             LOGGER.info("subscribeFunctionType MoneyNotEnoughException", e);
             OperationDescription operationDescription = ObjectFactory.buildErrorOperationDescription(
                Response.Status.NOT_ACCEPTABLE.getStatusCode(), "subscribeFunctionType", e.getMessage());
             return Response.ok(operationDescription).build();
 
         } catch (UserHasSubscribedException e) {
             LOGGER.error("subscribeFunctionType error", e);
             OperationDescription operationDescription = ObjectFactory.buildErrorOperationDescription(
                Response.Status.NOT_ACCEPTABLE.getStatusCode(), "subscribeFunctionType", e.getMessage());
             return Response.ok(operationDescription).build();
 
         } catch (Exception e) {
             LOGGER.error("subscribeFunctionType error", e);
             OperationDescription operationDescription = ObjectFactory.buildErrorOperationDescription(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "subscribeFunctionType", e.getMessage());
             return Response.ok(operationDescription).build();
         }
 
     }
 
     @DELETE
     public Response cancelSubscribeFunctionType(@PathParam("imsi") String imsi, @QueryParam("type") String type) {
         LOGGER.info("Into cancelSubscribeFunctionType imsi = " + imsi + " type = " + type);
         try {
             if ("holiday".equals(type)) {
                 ValidationUtil.checkParametersNotNull(imsi);
                 holidayTypeService.cancelSubscribeHolidayType(imsi);
                 
             } else if ("status".equals(type)) {
                 ValidationUtil.checkParametersNotNull(imsi);
                 statusTypeService.cancelSubscribeStatusType(imsi);
                 
             } else if ("custom".equals(type)) {
                 ValidationUtil.checkParametersNotNull(imsi);
                 userSubscribeTypeService.cancelSubscribeType(imsi, "custom");
             }
 
         } catch (Exception e) {
             LOGGER.error("cancelSubscribeCustomType error", e);
             OperationDescription operationDescription = ObjectFactory.buildErrorOperationDescription(
                 Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "cancelSubscribeCustomType", "服务器内部错误");
             return Response.ok(operationDescription).build();
         }
         OperationDescription operationDescription = ObjectFactory.buildOperationDescription(
             Response.Status.NO_CONTENT.getStatusCode(), "cancelSubscribeCustomType", "成功取消订购");
         return Response.ok(operationDescription).build();
 
     }
     
     //test method, it just provide convient method to cancel subscribe graphic item
     @DELETE
     @Path("/graphicInfo/{graphicInfoId}")
     public Response cancelSubscribeGraphicItem(@PathParam("imsi") String imsi,
                                                @PathParam("graphicInfoId") String graphicInfoId) {
         try {
             userSubscribeGraphicItemService.deleteSubscribeItem(imsi, graphicInfoId);
             OperationDescription operationDescription = ObjectFactory.buildOperationDescription(
                 HttpServletResponse.SC_NO_CONTENT, "cancelSubscribeGraphicItem");
             return Response.ok(operationDescription).build();
         } catch (Exception e) {
             OperationDescription operationDescription = ObjectFactory.buildErrorOperationDescription(500,
                 "cancelSubscribeGraphicItem", "取消订购失败");
             return Response.ok(operationDescription).build();
         }
 
     }
 
 }
