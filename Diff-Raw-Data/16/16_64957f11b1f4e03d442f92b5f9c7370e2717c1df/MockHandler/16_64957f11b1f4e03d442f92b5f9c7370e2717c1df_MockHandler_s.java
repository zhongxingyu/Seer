 package org.hoydaa.restmock.server.handler.manager;
 
 import org.apache.commons.io.IOUtils;
 import org.hoydaa.restmock.client.IRequest;
 import org.hoydaa.restmock.client.Request;
 import org.hoydaa.restmock.server.handler.AbstractRequestHandler;
 import org.hoydaa.restmock.server.util.Utils;
 import org.springframework.util.Assert;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 /**
  * @author Umut Utkan
  */
 public class MockHandler extends AbstractRequestHandler {
 
     private RequestRepository requestRepository;
 
 
     @Override
     public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
         Assert.notNull(requestRepository, "RequestRepository cannot be null!");
 
         IRequest mockRequest = null;
         try {
             mockRequest = requestRepository.getRequest(request);
         } catch (RequestRepositoryException e) {
            sendResponse(request, response, e.getStatus().getExplanation(), e.getStatus().getCode());
             return;
         }
 
         if (null == mockRequest) {
            sendResponse(request, response, ResponseStatus.UNEXPECTED_CALL.getExplanation(), ResponseStatus.UNEXPECTED_CALL.getCode());
             return;
         }
 
         ResponseStatus status = null;
         if (null != (status = Utils.equals(mockRequest, request))) {
             sendResponse(request, response, status.getExplanation(), status.getCode());
             return;
         }
 
         Request mockkRequest = (Request) mockRequest;
         response.setStatus(mockkRequest.getResponse().getStatus());
         response.setContentType(mockkRequest.getResponse().getType());
         ((org.mortbay.jetty.Request) request).setHandled(true);
         IOUtils.copy(mockkRequest.getResponse().getStream(), response.getWriter());
     }
 
     public void setRequestRepository(RequestRepository requestRepository) {
         this.requestRepository = requestRepository;
     }
 
 }
