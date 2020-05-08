 package org.motechproject.ananya.kilkari.web.views;
 
 import org.motechproject.ananya.kilkari.web.HttpConstants;
 import org.motechproject.ananya.kilkari.web.response.BaseResponse;
 import org.motechproject.web.message.converters.CustomJaxb2RootElementHttpMessageConverter;
 import org.springframework.http.MediaType;
 import org.springframework.http.server.ServletServerHttpResponse;
 import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
 import org.springframework.web.servlet.view.AbstractView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.Map;
 
 public class ExceptionView extends AbstractView {
 
     @Override
     protected void renderMergedOutputModel(
             Map<String, Object> model,
             HttpServletRequest request,
             HttpServletResponse response) throws Exception {
 
         Exception exceptionObject = (Exception) model.get(SimpleMappingExceptionResolver.DEFAULT_EXCEPTION_ATTRIBUTE);
 
         String acceptHeader = request.getHeader("accept");
 
        if (MediaType.APPLICATION_XML.toString().equals(acceptHeader))
             new CustomJaxb2RootElementHttpMessageConverter().write(BaseResponse.failure(exceptionObject.getMessage()), MediaType.APPLICATION_XML, new ServletServerHttpResponse(response));
        else
             response.getOutputStream().print(BaseResponse.failure(exceptionObject.getMessage()).toJson());

        HttpConstants httpConstants = HttpConstants.forRequest(request);
        response.setStatus(httpConstants.getHttpStatusError());
         response.setContentType(httpConstants.getResponseContentType(acceptHeader));
     }
 }
