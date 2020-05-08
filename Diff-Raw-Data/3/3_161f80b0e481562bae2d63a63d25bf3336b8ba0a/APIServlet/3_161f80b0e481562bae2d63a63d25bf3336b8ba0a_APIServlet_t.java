 /*
  * $Id$
  */
 package org.xins.server;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.xins.util.servlet.ServletUtils;
 
 /**
  * Servlet that forwards request to an <code>API</code>.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public final class APIServlet
 extends HttpServlet {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>APIServlet</code> object.
     */
    public APIServlet() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The API that this servlet forwards requests to.
     */
    private API _api;
 
    /**
     * The logging category used by this servlet. This field is initialised by
     * {@link #init(ServletConfig)} to a non-<code>null</code> value.
     */
    private Logger _log;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    public void init(ServletConfig config)
    throws ServletException {
       String apiClass = config.getInitParameter("api.class");
       if (apiClass == null || apiClass.equals("")) {
          throw new ServletException("Unable to initialize servlet \"" + config.getServletName() + "\", API class should be set in init parameter \"api.class\".");
       }
 
       // TODO: Better error handling
 
       Properties settings = ServletUtils.settingsAsProperties(config);
 
       // Initialise Log4J
       PropertyConfigurator.configure(settings);
       _log = Logger.getLogger(getClass().getName());
 
       // Create an API instance
       try {
          _api = (API) Class.forName(apiClass).newInstance();
       } catch (Exception e) {
          throw new ServletException("Unable to initialize servlet \"" + config.getServletName() + "\", unable to instantiate an object of type " + apiClass + ", or unable to convert it to an API instance.");
       }
 
       // Initialise the API
       try {
          _api.init(settings);
       } catch (Throwable e) {
          throw new ServletException("Unable to initialize servlet \"" + config.getServletName() + "\", the initialisation performed by API of type " + apiClass + " failed.");
       }
    }
 
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
       handleRequest(req, resp);
    }
 
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
       handleRequest(req, resp);
    }
 
    /**
     * Handles all HTTP GET and POST requests.
     *
     * @param request
     *    the HTTP request, not <code>null</code>.
     *
     * @param response
     *    the HTTP response, not <code>null</code>.
     *
     * @throws ServletException
     *    this servlet encountered an error.
     *
     * @throws IOException
     *    there was an I/O problem.
     */
    private void handleRequest(HttpServletRequest request,
                               HttpServletResponse response)
    throws ServletException, IOException {
 
       // TODO: Be less memory-intensive
 
       // Set the content output type to XML
       response.setContentType("text/xml");
 
       // Call the API
      PrintWriter out = response.getWriter(); 
      _api.handleCall(request, out);
 
       // Flush
       out.flush();
    }
 }
