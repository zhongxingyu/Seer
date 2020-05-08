 package com.jellymold.engine;
 
 import com.jellymold.kiwi.auth.AuthService;
 import com.jellymold.kiwi.auth.AuthUtils;
 import com.jellymold.utils.MediaTypeUtils;
 import com.jellymold.utils.ThreadBeanHolder;
 import com.jellymold.utils.skin.FreeMarkerConfigurationService;
 import freemarker.template.Configuration;
 import org.restlet.Application;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.ext.freemarker.TemplateRepresentation;
 import org.restlet.resource.Representation;
 import org.restlet.resource.StringRepresentation;
 import org.restlet.service.StatusService;
 import org.springframework.context.ApplicationContext;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 
 public class EngineStatusService extends StatusService {
 
     public EngineStatusService(boolean enabled) {
         super(enabled);
     }
 
     @Override
     public Representation getRepresentation(Status status, Request request, Response response) {
         if (MediaTypeUtils.isStandardWebBrowser(request)) {
 
             Configuration configuration;
             ApplicationContext springContext = (ApplicationContext) request.getAttributes().get("springContext");
 
             FreeMarkerConfigurationService freeMarkerConfigurationService =
                     (FreeMarkerConfigurationService) springContext.getBean("freeMarkerConfigurationService");
             configuration = freeMarkerConfigurationService.getConfiguration();
 
             if ((configuration != null) && (springContext != null)) {
                 Map<String, Object> values = new HashMap<String, Object>();
                 values.put("status", status);
                 // values below are mirrored in BaseResource and SkinRenderResource
                 values.put("authService", springContext.getBean("authService"));
                 values.put("activeUser", AuthService.getUser());
                 values.put("activeGroup", ThreadBeanHolder.get("group"));
                 values.put("activeSite", ThreadBeanHolder.get("site"));
                 values.put("activeApp", ThreadBeanHolder.get("app"));
                 values.put("activeSiteApp", ThreadBeanHolder.get("siteApp"));
                 // find a Template Representation
                 return getTemplateRepresentation(status, request, configuration, values);
             } else {
                 return super.getRepresentation(status, request, response);
             }
 
         } else {
             // just return status code for API calls
             return new StringRepresentation("");
         }
     }
 
     protected Representation getTemplateRepresentation(
             Status status,
             Request request,
             Configuration configuration,
             Map<String, Object> values) {
 
        if (request.getResourceRef().getPath().equals("/")) {
             return new TemplateRepresentation("default.ftl", configuration, values, MediaType.TEXT_HTML);
         } else if (status.equals(Status.CLIENT_ERROR_UNAUTHORIZED)) {
             values.put("next", AuthUtils.getNextUrl(request));
             return new TemplateRepresentation("401.ftl", configuration, values, MediaType.TEXT_HTML);
         } else if (status.equals(Status.CLIENT_ERROR_FORBIDDEN)) {
             values.put("next", AuthUtils.getNextUrl(request));
             return new TemplateRepresentation("403.ftl", configuration, values, MediaType.TEXT_HTML);
         } else if (status.equals(Status.CLIENT_ERROR_NOT_FOUND)) {
             return new TemplateRepresentation("404.ftl", configuration, values, MediaType.TEXT_HTML);
         } else if (status.equals(Status.SERVER_ERROR_INTERNAL)) {
             return new TemplateRepresentation("500.ftl", configuration, values, MediaType.TEXT_HTML);
         } else {
             return null;
         }
     }
 
     @Override
     public Status getStatus(Throwable throwable, Request request, Response response) {
         Application.getCurrent().getLogger().log(Level.SEVERE, "Unhandled exception or error intercepted", throwable);
         return new Status(Status.SERVER_ERROR_INTERNAL.getCode(), throwable);
     }
 }
