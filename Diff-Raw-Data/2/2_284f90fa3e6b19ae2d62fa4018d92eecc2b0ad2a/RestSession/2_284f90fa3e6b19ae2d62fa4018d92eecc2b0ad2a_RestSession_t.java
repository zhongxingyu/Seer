 package nz.ac.otago.orest;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import nz.ac.otago.orest.enums.HttpMethod;
 import java.util.Collection;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import nz.ac.otago.orest.util.PropertyUtils;
 
 import nz.ac.otago.orest.controller.RestController;
 import nz.ac.otago.orest.formats.RestFormat;
 import nz.ac.otago.orest.resource.RestResource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class RestSession {
 
    private final static Logger logger = LoggerFactory.getLogger(RestSession.class);
 
    private RestConfiguration config;
 
    public RestConfiguration getConfiguration() {
       return config;
    }
 
    public void setConfiguration(RestConfiguration config) {
       this.config = config;
    }
 
    void processRequest(String path, HttpMethod method, HttpServletRequest request, HttpServletResponse response, String contentType) throws Exception {
 
       RestFormat format = config.getFormat(contentType);
       if (format == null) {
          format = config.getDefaultFormat();
       }
 
       String[] pathElements = path.split("/");
       String root = pathElements[1];
 
       String configurationErrors = config.checkConfiguration(root);
       if (configurationErrors != null) {
          response.sendError(412, configurationErrors);
 //            throw new RuntimeException();
          return;
       }
 
       RestController<?> controller = config.getController(root);
 
       RestRequest restRequest = new RestRequest(root, controller, request, response, format, config);
 
       if (pathElements.length > 2) {
 
          String id = pathElements[2];
 
          logger.debug("Using resource ID '{}'", id);
          restRequest.setResourceId(id);
 
          if (method == HttpMethod.DELETE) {
             deleteResource(restRequest);
          } else if (method == HttpMethod.POST) {
             response.sendError(405, String.format("Method '%1s' can not be used with path '%2s'.", method, path));
          } else if (method == HttpMethod.PUT) {
             updateResource(restRequest);
          } else if (method == HttpMethod.GET) {
             requestResource(restRequest, id);
          }
 
       } else if (method == HttpMethod.GET) {
          // if root selected (since there is no ID
          requestRoot(restRequest);
       } else if (method == HttpMethod.POST) {
          createResource(restRequest);
       } else {
          logger.error("Method '{}' and Path '{}' do not make sense.", method, path);
          response.sendError(405, String.format("Method '%1s' can not be used with path '%2s'", method, path));
       }
    }
 
    private void requestRoot(RestRequest restRequest) throws Exception {
       logger.debug("Getting all resources IDs from controller");
       Collection<? extends RestResource> col = restRequest.getController().getAll();
 
       RestFormat format = restRequest.getFormat();
       HttpServletResponse response = restRequest.getServletResponse();
 
       response.setContentType(format.getContentType());
       response.getWriter().println(format.serialiseCollection(col, restRequest));
    }
 
    private void requestResource(RestRequest restRequest, String id) throws Exception {
       logger.debug("Getting single resource with ID '{}' from controller", id);
       RestResource resource = restRequest.getController().get(id);
       if (resource != null) {
          RestFormat format = restRequest.getFormat();
          HttpServletResponse response = restRequest.getServletResponse();
          String responseString = format.serialiseResource(resource, restRequest);
          response.setContentType(format.getContentType());
          response.getWriter().println(responseString);
       } else {
          restRequest.getServletResponse().sendError(404, String.format("Resource with id '%1s' does not exist", id));
       }
    }
 
    private void createResource(RestRequest request) throws Exception {
       String data = readBody(request);
 
       RestResource resource = request.getFormat().deserialiseResource(data, request);
 
       RestController controller = request.getController();
 
       logger.debug("Calling create on controller");
       controller.create(resource);
    }
 
    private void updateResource(RestRequest request) throws Exception {
       RestController controller = request.getController();
       String id = request.getResourceId();
 
       // does resource already exist?
       if (controller.get(id) != null) {
          // if so update it
 
          RestResource original = controller.get(id);
 
          String body = readBody(request);
 
          RestResource resource = request.getFormat().deserialiseResource(body, request);
 
          Collection<String> fields = PropertyUtils.getAllProperties(resource.getClass());
 
          for (String field : fields) {
             Object value = PropertyUtils.getProperty(resource, field);
             if (value != null) {
                logger.debug("Updating field '{}' for resource '{}'", field, id);
                PropertyUtils.setProperty(original, field, value);
             }
          }
 
          logger.debug("Calling update on controller for resource '{}'", id);
         controller.update(id, original);
       } else {
          // does not exist so create it
          logger.debug("Creating resource via PUT");
          createResource(request);
       }
    }
 
    private void deleteResource(RestRequest request) {
       RestController controller = request.getController();
       String id = request.getResourceId();
 
       logger.debug("Calling delete on controller");
       controller.delete(id);
    }
 
    private String readBody(RestRequest request) throws Exception {
       BufferedReader reader = new BufferedReader(new InputStreamReader(request.getServletRequest().getInputStream()));
       StringBuilder builder = new StringBuilder();
       String line = reader.readLine();
       while (line != null) {
          builder.append(line);
          line = reader.readLine();
       }
       String body = builder.toString();
 
       return body;
    }
 }
