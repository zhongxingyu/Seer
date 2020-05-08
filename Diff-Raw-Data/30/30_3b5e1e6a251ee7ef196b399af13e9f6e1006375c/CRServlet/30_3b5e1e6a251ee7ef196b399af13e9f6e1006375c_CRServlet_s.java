 package com.gentics.cr.servlet;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 
 import com.gentics.api.lib.resolving.Resolvable;
 import com.gentics.cr.CRServletConfig;
 import com.gentics.cr.rest.RESTBinaryContainer;
 import com.gentics.cr.util.BeanWrapper;
 import com.gentics.cr.util.CRBinaryRequestBuilder;
 import com.gentics.cr.util.HttpSessionWrapper;
 
 
 /**
  * @author haymo
  * 
  * Use to render content from a contentrepository.
  * 
  */
 public class CRServlet extends HttpServlet {
 
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -6943138512221124880L;
 	private Logger log;
 	private RESTBinaryContainer container;
 
 	public void init(ServletConfig config) throws ServletException {
 
 		super.init(config);
 		this.log = Logger.getLogger("com.gentics.cr");
 		CRServletConfig crConf = new CRServletConfig(config);
 		container = new RESTBinaryContainer(crConf);
 
 	}
 
 	/**
 	 * Wrapper Method for the doGet and doPost Methods
 	 * 
 	 * @param request
 	 * @param response
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	public void doService(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 
 		// URI and Query String
 		String requestID = request.getRequestURI();
 		if (request.getQueryString() != null) {
 			requestID += "?" + request.getQueryString();
 		}
 		String contentDisposition = request.getParameter("contentdisposition");
 		
 		this.log.debug("Starting request: " + requestID);
 
 		// starttime
 		long s = new Date().getTime();
 		
 		HashMap<String,Resolvable> objects = new HashMap<String,Resolvable>();
 		objects.put("request", new BeanWrapper(request));
 		objects.put("session", new HttpSessionWrapper(request.getSession()));
		container.processService(new CRBinaryRequestBuilder(request), objects, response.getOutputStream());
		response.setContentType(container.getContentType());
		
 		
 		if(contentDisposition!=null && contentDisposition!="")
 		{
 			response.addHeader("Content-Disposition","attachment; filename=\""+contentDisposition+"\"");
 		}
 		
 		response.getOutputStream().flush();
 		response.getOutputStream().close();
 
 		// endtime
 		long e = new Date().getTime();
 		this.log.debug("Executiontime for " + requestID + ":" + (e - s));
 
 	}
 	
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 		doService(request, response);
 	}
 
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 		doService(request, response);
 	}
 
 }
