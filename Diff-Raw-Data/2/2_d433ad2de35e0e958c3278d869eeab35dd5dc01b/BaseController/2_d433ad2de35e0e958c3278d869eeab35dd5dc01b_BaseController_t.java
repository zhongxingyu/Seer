 package org.inigma.shared.webapp;
 
 import java.io.IOException;
 import java.lang.reflect.Array;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONWriter;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.validation.Errors;
 import org.springframework.validation.FieldError;
 import org.springframework.validation.ObjectError;
 import org.springframework.web.context.request.RequestAttributes;
 import org.springframework.web.context.request.RequestContextHolder;
 
 /**
  * Base controller providing access to a common set of functionality.
  * 
  * @author <a href="mailto:sejal@inigma.org">Sejal Patel</a>
  */
 public abstract class BaseController {
     protected final Log logger = LogFactory.getLog(getClass());
     @Autowired
     private MessageSource messageSource;
 
     protected void error(HttpServletResponse response, String code) {
         getErrors().reject(code);
         response(response, null);
     }
 
     protected void error(HttpServletResponse response, String code, String field) {
         getErrors().rejectValue(field, code);
         response(response, null);
     }
 
     protected Authentication getAuthentication() {
         return SecurityContextHolder.getContext().getAuthentication();
     }
 
     protected Errors getErrors() {
         Errors errors = (Errors) RequestContextHolder.getRequestAttributes().getAttribute("errors",
                 RequestAttributes.SCOPE_REQUEST);
         if (errors == null) {
             throw new IllegalStateException("Errors not properly initialized in the request attributes!");
         }
         return errors;
     }
 
     protected boolean hasNoErrors() {
         return !getErrors().hasErrors();
     }
 
     protected void response(HttpServletResponse response, Object data) {
         // protected void response(Writer w, Object data) {
         response.setContentType("application/json");
         List<ObjectError> errors = getErrors().getAllErrors();
         try {
             JSONWriter writer = new JSONWriter(response.getWriter()).object();
             if (hasNoErrors()) {
                 writer.key("data");
                 if (data == null) {
                     writer.value(null);
                 } else if (data instanceof String || data instanceof Number || data instanceof Boolean || data instanceof Map<?, ?>) {
                     writer.value(data);
                 } else if (data instanceof Collection<?> || data instanceof Array) {
                     writer.value(new JSONArray(data));
                 } else {
                     writer.value(new JSONObject(data));
                 }
             }
             writer.key("success").value(hasNoErrors());
             writer.key("errors").array();
             for (ObjectError error : errors) {
                 writer.object();
                 writer.key("code").value(error.getCode());
                writer.key("message").value(messageSource.getMessage(error.getCode(), error.getArguments(), error.getDefaultMessage(), null));
                 if (error instanceof FieldError) {
                     writer.key("field").value(((FieldError) error).getField());
                 }
                 writer.endObject();
             }
             writer.endArray();
             writer.endObject();
         } catch (JSONException e) {
             logger.error("Unable to generate response", e);
             throw new RuntimeException("Error responding with errors", e);
         } catch (IOException e) {
             logger.error("Unable to generate response", e);
             throw new RuntimeException("Error responding with errors", e);
         }
     }
 }
