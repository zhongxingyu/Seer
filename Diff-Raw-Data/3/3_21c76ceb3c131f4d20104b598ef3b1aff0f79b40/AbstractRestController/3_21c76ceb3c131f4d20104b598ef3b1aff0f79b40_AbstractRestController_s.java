 package org.inigma.lwrest.webapp;
 
 import java.io.IOException;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Array;
 import java.lang.reflect.Method;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.inject.Inject;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.inigma.lwrest.InjectionHolder;
 import org.inigma.lwrest.logger.Logger;
 import org.inigma.lwrest.message.Message;
 import org.inigma.lwrest.message.MessageDaoTemplate;
 import org.inigma.lwrest.webapp.Response.Error;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONWriter;
 
 /**
  * Base controller providing access to a common set of functionality.
  * 
  * @author <a href="mailto:sejal@inigma.org">Sejal Patel</a>
  */
 public abstract class AbstractRestController extends HttpServlet {
     protected final Logger logger = Logger.getLogger(getClass());
 
     @Inject
     private MessageDaoTemplate messageTemplate;
 
     private Set<RequestMapping> deleteMappings = new HashSet<RequestMapping>();
     private Set<RequestMapping> getMappings = new HashSet<RequestMapping>();
     private Set<RequestMapping> postMappings = new HashSet<RequestMapping>();
     private Set<RequestMapping> putMappings = new HashSet<RequestMapping>();
 
     private ThreadLocal<Response> response;
 
     public AbstractRestController() {
         InjectionHolder.injectFields(this);
     }
 
     @Override
     public void init() throws ServletException {
         super.init();
         this.response = new ThreadLocal<Response>();
         for (Method method : getClass().getMethods()) {
             for (Annotation annotation : method.getAnnotations()) {
                 if (GET.class.isInstance(annotation)) {
                     GET g = (GET) annotation;
                     for (String value : g.value()) {
                         getMappings.add(new RequestMapping(method, value));
                     }
                 } else if (POST.class.isInstance(annotation)) {
                     POST g = (POST) annotation;
                     for (String value : g.value()) {
                         postMappings.add(new RequestMapping(method, value));
                     }
                 } else if (PUT.class.isInstance(annotation)) {
                     PUT g = (PUT) annotation;
                     for (String value : g.value()) {
                         putMappings.add(new RequestMapping(method, value));
                     }
                 } else if (DELETE.class.isInstance(annotation)) {
                     DELETE g = (DELETE) annotation;
                     for (String value : g.value()) {
                         deleteMappings.add(new RequestMapping(method, value));
                     }
                 }
             }
         }
     }
 
     @Override
     protected final void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
             IOException {
         processMapping(req, resp, deleteMappings);
     }
 
     @Override
     protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         processMapping(req, resp, getMappings);
     }
 
     @Override
     protected final void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         super.doHead(req, resp);
     }
 
     @Override
     protected final void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
             IOException {
         super.doOptions(req, resp);
     }
 
     @Override
     protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         processMapping(req, resp, postMappings);
     }
 
     @Override
     protected final void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         processMapping(req, resp, putMappings);
     }
 
     @Override
     protected final void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         super.doTrace(req, resp);
     }
 
     protected Response getResponse() {
         return this.response.get();
     }
 
     @Override
     protected final void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         Response responseBean = new Response();
         this.response.set(responseBean);
         try {
             super.service(req, resp);
         } catch (Exception e) {
             logger.error("Unhandled Exception: ", e);
             responseBean.reject(e);
         }
         response(req, resp);
         this.response.remove();
     }
 
     private void processMapping(HttpServletRequest req, HttpServletResponse resp, Set<RequestMapping> mappings) {
         Method method = null;
         for (RequestMapping mapping : mappings) {
             PathParameters pp = mapping.matches(req.getPathInfo());
             if (pp != null) {
                 try {
                     method = mapping.getMethod();
                     Class<?>[] parameterTypes = method.getParameterTypes();
                     Object[] parameters = new Object[parameterTypes.length];
                     for (int i = 0; i < parameterTypes.length; i++) {
                         if (parameterTypes[i].isAssignableFrom(PathParameters.class)) {
                             parameters[i] = pp;
                         } else if (parameterTypes[i].isAssignableFrom(HttpServletRequest.class)) {
                             parameters[i] = req;
                         } else if (parameterTypes[i].isAssignableFrom(HttpServletResponse.class)) {
                             parameters[i] = resp;
                         } else {
                             logger.warn("Parameter type '%s' is not handled!", parameterTypes[i].getClass());
                         }
                     }
 
                     method.invoke(this, parameters);
                 } catch (Exception e) {
                     logger.error("Something bad happened", e);
                     getResponse().reject(e);
                 }
             }
         }
         if (method == null) {
            req.setAttribute("_exception",
                    new IllegalAccessException("Invalid mapping: " + req.getMethod() + " " + req.getPathInfo()));
         }
     }
 
     private void response(HttpServletRequest request, HttpServletResponse response) {
         response.setContentType("application/json");
         try {
             Response responseBean = this.response.get();
             Object data = responseBean.getData();
             JSONWriter writer = new JSONWriter(response.getWriter()).object();
             writer.key("data");
             if (data == null) {
                 writer.value(null);
             } else if (data instanceof String || data instanceof Number || data instanceof Boolean
                     || data instanceof Map<?, ?>) {
                 writer.value(data);
             } else if (data instanceof Collection<?> || data instanceof Array) {
                 writer.value(new JSONArray(data));
             } else {
                 writer.value(new JSONObject(data));
             }
             writer.key("success").value(responseBean.isSuccess());
             writer.key("errors").array();
             for (Error error : responseBean.getErrors()) {
                 writer.object();
                 if (error.getParameter() != null) {
                     writer.key("field").value(error.getParameter());
                 }
                 writer.key("code").value(error.getCode());
                 if (messageTemplate != null) {
                     Message message = messageTemplate.findById(error.getCode(), null);
                     if (message != null) {
                         writer.key("message").value(message.getValue());
                     } else {
                         writer.key("message").value(error.getMessage());
                     }
                 } else {
                     writer.key("message").value(error.getMessage());
                 }
                 writer.endObject();
             }
             writer.endArray();
             writer.endObject();
         } catch (JSONException e) {
             logger.warn("Unable to generate response", e);
             throw new RuntimeException("Error responding with errors", e);
         } catch (IOException e) {
             logger.warn("Unable to generate response", e);
             throw new RuntimeException("Error responding with errors", e);
         }
     }
 }
