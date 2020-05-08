 package ncl.military.controller;
 
 import ncl.military.controller.handle.Handlable;
 import ncl.military.controller.handle.HandlerFactory;
 import ncl.military.dao.DAO;
 import ncl.military.dao.DAOFactory;
 import ncl.military.dao.exceptions.DataAccessException;
 import org.apache.log4j.Logger;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * User: Silvan
  * Date: 18.04.12
  * Time: 7:44
  */
 public class ControllerServlet extends HttpServlet {
 
     public static final Logger log = Logger.getLogger("controller");
 
     private DAO dao = null;
 
     public void init(ServletConfig servletConfig) throws ServletException {
         super.init(servletConfig);
         HandlerFactory.init(getServletConfig(), getServletContext());
         Map<String, String> initParams = new HashMap<String, String>();
         Enumeration names = servletConfig.getServletContext().getInitParameterNames();
         while (names.hasMoreElements()) {
             String parameter = (String) names.nextElement();
             initParams.put(parameter, servletConfig.getServletContext().getInitParameter(parameter));
         }
 
         try {
             dao = DAOFactory.getDao(getServletContext());
             dao.init(initParams);
             log.info("ControllerServlet initialized");
         } catch (DataAccessException e) {
             log.fatal("DAO-module wasn't initialized, see log to find out.", e);
             throw new ServletException("DAO-module wasn't initialized, see log to find out.", e);
         }
     }
 
     public void perfromAction(HttpServletRequest req, HttpServletResponse res) {
         String userPath = req.getServletPath();
         req.setAttribute("userPath", userPath);
 
         // Copying parameters from request scope to params-Map which will be feeded
         Map<String, Object> params = new HashMap<String, Object>();
         Iterator entryIt = req.getParameterMap().entrySet().iterator();
         log.debug(" request params => Map of params for executrors");
         while (entryIt.hasNext()) {
             Map.Entry entry = (Map.Entry) entryIt.next();
             if (entry.getValue() instanceof Object[] && ((Object[]) entry.getValue()).length > 1) {
                 log.debug((String) entry.getKey() + " => " + entry.getValue());
                 params.put((String) entry.getKey(), entry.getValue());
             } else {
                 if (log.isDebugEnabled()) {
                     StringBuilder str4log = new StringBuilder();
                     for (Object o : ((Object[]) entry.getValue())) {
                         str4log.append(o);
                         str4log.append(".");
                     }
                     log.debug((String) entry.getKey() + " => " + str4log.toString());
                 }
                 params.put((String) entry.getKey(), ((Object[]) entry.getValue())[0]);
             }
         }
 
         params.put("userPath", userPath);
         log.debug(" Puted into result Map: userPath => " + userPath);
 
         Handlable handle = HandlerFactory.getHandler(dao, params);
         Map<String, ? extends Object> result = handle.execute(params);
 
         for (String key : result.keySet()) {
             req.setAttribute(key, result.get(key));
         }
 
         try {
             if (result.get("success") != null && !(Boolean) result.get("success")) {
                //req.setAttribute("errorCause", "Some actions caused error, check your input data.");
                 log.debug("forwarding to error page");
                 req.getRequestDispatcher(getServletContext().getInitParameter("view=error")).forward(req, res);
             } else {
                 log.debug("forwarding to " + handle.getView());
                 req.getRequestDispatcher(handle.getView()).forward(req, res);
             }
         } catch (Exception ex) {
             log.error(ex.getMessage(), ex);
         }
     }
 
     /**
      * Handles the HTTP <code>GET</code> method.
      *
      * @param request  servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException {
         perfromAction(request, response);
     }
 
     /**
      * Handles the HTTP <code>POST</code> method.
      *
      * @param request  servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException {
         perfromAction(request, response);
     }
 }
