 package br.com.boltframework;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.lang.reflect.Method;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import br.com.boltframework.config.BoltConfiguration;
 import br.com.boltframework.config.DefaultConfiguration;
 import br.com.boltframework.core.ControllerDecorator;
 import br.com.boltframework.core.ClassFinder;
 import br.com.boltframework.core.ControllerMapping;
 import br.com.boltframework.error.BoltException;
 import br.com.boltframework.http.HttpMethod;
 import br.com.boltframework.util.ClassUtils;
 import br.com.boltframework.util.Constants;
 import br.com.boltframework.util.ControllerUtils;
 import br.com.boltframework.util.StringUtils;
 
 public class Bolt extends HttpServlet {
 
   private static final long serialVersionUID = -6569963555600736301L;
   private static Logger logger = Logger.getLogger(Bolt.class.getName());
 
   private List<ControllerMapping> controllerList;
   private BoltConfiguration configuration;
 
   @Override
   public void init() throws ServletException {
     configuration = getConfigurationInstance();
     controllerList = ClassFinder.createInstance().loadAllControllerMappings(getServletConfig(), getServletContext());
   }
 
   private String getCustomConfiguration() {
     return getServletConfig().getInitParameter(Constants.CONFIGURATION_INIT_PARAMETER);
   }
 
   private BoltConfiguration getConfigurationInstance() {
     if (StringUtils.isNotBlank(getCustomConfiguration())) {
       try {
         return (BoltConfiguration) ClassUtils.createClassInstance(getCustomConfiguration());
       }
       catch (BoltException e) {
         logger.log(Level.SEVERE, "Error to initialize the Custom Bolt Configuration class, please see your class. Using the Default Bolt Configuration class.", e);
       }
     }
     return new DefaultConfiguration();
   }
 
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
     action(req, resp, HttpMethod.GET);
   }
 
   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
     action(req, resp, HttpMethod.POST);
   }
 
   @Override
   protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
     action(req, resp, HttpMethod.DELETE);
   }
 
   @Override
   protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
     action(req, resp, HttpMethod.PUT);
   }
 
   protected void action(HttpServletRequest request, HttpServletResponse response, HttpMethod httpMethod) throws ServletException, IOException {
     String dispatch = null;
     String pathInfo = request.getPathInfo();
     String applicationContext = ControllerUtils.getApplicationContext(request, getServletConfig());
     request.setAttribute(Constants.APPLICATION_CONTEXT, applicationContext);
 
     try {
       if (StringUtils.isBlank(pathInfo)) {
         response.sendRedirect(request.getRequestURI() + Constants.FORWARD_SLASH);
       }
 
       ControllerMapping controllerMapping = ControllerUtils.findMapping(controllerList, pathInfo, httpMethod);
 
       if (controllerMapping == null) {
         throw new ClassNotFoundException("Controller mapping not found: " + pathInfo);
       }
 
       Class<Object> controllerClass = (Class<Object>) controllerMapping.getController();
       if (controllerClass == null) {
         throw new ClassNotFoundException("Controller class not found: " + pathInfo);
       }
 
       Object controller = controllerClass.newInstance();
       Method action = controllerMapping.getAction();
       Method runBeforeAction = controllerMapping.getRunBeforeAction();
       ControllerDecorator controllerDecorator = new ControllerDecorator(controller);
 
       dispatch = (String) controllerDecorator.executeAction(request, response, runBeforeAction, action);
     }
     catch (Exception e) {
       request.setAttribute(Constants.ERROR_ATTRIBUTE_NAME, e);
       dispatch = configuration.getErrorPage();
       DefaultConfiguration defaultConfiguration = new DefaultConfiguration();
 
       if (defaultConfiguration.getErrorPage().equals(dispatch)) {
         PrintWriter out = response.getWriter();
         String message = (e.getMessage() == null) ? e.getCause().getClass().getName() : e.getMessage();
         String content = ControllerUtils.obtainDefaultErrorPageWithMessage(message);
         out.print(content);
         return;
       }
     }
 
    if (StringUtils.isNotBlank(dispatch) && !dispatch.endsWith(Constants.JSP_FILE_EXTENSION)) {
       RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(configuration.getViewsPath() + dispatch);
       dispatcher.forward(request, response);
     }
     else {
       response.sendRedirect(dispatch);
     }
   }
 
 }
