 package com.gentics.cr.rest;
 
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
 import com.gentics.cr.util.BeanWrapper;
 import com.gentics.cr.util.CRRequestBuilder;
 import com.gentics.cr.util.HttpSessionWrapper;
import com.gentics.cr.util.response.ServletResponseTypeSetter;
 
 
 /**
  * @author haymo
  * 
  * Used to render the Rest xml.
  * 
  */
 public class RESTServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 0002L;
 	private Logger log;
 	private CRServletConfig crConf;
 	private RESTSimpleContainer container;
 	
 	public void init(ServletConfig config) throws ServletException {
 
 		super.init(config);
 		this.log = Logger.getLogger("com.gentics.cr");
 		this.crConf = new CRServletConfig(config);
 		container = new RESTSimpleContainer(crConf);
 
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
 
 		this.log.debug("Request:" + request.getQueryString());
 		
 		// starttime
 		long s = new Date().getTime();
 		// get the objects
 		
 		HashMap<String,Resolvable> objects = new HashMap<String,Resolvable>();
 		objects.put("request", new BeanWrapper(request));
 		objects.put("session", new HttpSessionWrapper(request.getSession()));
 		CRRequestBuilder rB = new CRRequestBuilder(request);
 		//response.setContentType(rB.getContentRepository(this.crConf.getEncoding()).getContentType()+"; charset="+this.crConf.getEncoding());
		container.processService(rB, objects, response.getOutputStream(), new ServletResponseTypeSetter(response));
 		response.getOutputStream().flush();
 		response.getOutputStream().close();
 		// endtime
 		long e = new Date().getTime();
 		this.log.info("Executiontime for " + request.getQueryString() + ":" + (e - s));
 
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
