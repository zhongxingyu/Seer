 /**
  * 
 $Revision: 1.25 $"
 $Id: DBSServlet.java,v 1.25 2006/12/05 16:07:50 afaq Exp $"
 
  */
 package dbs;
 import java.io.PrintWriter;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.ServletException;
 import dbs.api.DBSApi;
 import dbs.util.DBSUtil;
 
 /**
 * A Servlet that inherits from <code>javax.servlet.http.HttpServlet</code>. This is the gateway for invoking DBS API. All remote requets pass through this servlet. This class simple implements doPost and doGet method and involes the DBS api that handles the rest.
 * @author sekhri
 */
 public class DBSServlet extends HttpServlet{
 	
          //We must have a better way of responding in XML    ANZAR
          //following is interim solution
 	 
 	/**
 	 * a method that takes the servlet request and packs all the parameters passed in the request into a <code>java.util.Hashtable</code> and then calls the <code>dbs.api.DBSApi</code> object that handles all the client requests. It also makes a <code>java.util.Hashtable</code> that contains user information. Eventually this user infomation will come from the security context established between the client and the server. The tomcat contaier provides api to retrive the user DN from this context. This table is aslo passed to the DBSApi that uses it for bookekeeping purposes.
 	 * {@inheritDoc}
 	 */
 	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
 		PrintWriter out = null;
 		try {
 			//Another interim solution no user DN so make one up, 
 			//will not work for Fresh DB deployments, unless a DN is inserted by hand   ANZAR
 			Hashtable userDN = new Hashtable();
 			userDN.put("user_dn", "ANZARDN");
 			DBSUtil.writeLog("SERIOUS WARNING HARD CODED DN USED ????");
 			DBSUtil.writeLog("DN of the user is " + request.getAttribute("org.globus.gsi.authorized.user.dn"));
 
 			response.setContentType("text/xml");
 			out = response.getWriter();
 		
			DBSApi api = new DBSApi();
 			api.call(out, getTable(request), userDN);
 		} catch(Exception e) {
 			throw new ServletException(e);
 		} finally {
 			if (out != null) out.close();
 		}
 	}
 
 	/**
 	 * Simply calls the doPoset method
 	 * {@inheritDoc}
 	 */
 	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
 		doPost(request, response);
 	}
 
 	/**
 	 * This method packs all the parameters passed in the request into a <code>java.util.Hashtable</code>
 	 * @param request an HttpServletRequest object that contains the request the client has made of the servlet
 	 * @return a <code>java.util.Hashtable</code> that contains all the parameter that the client has passed into the servlet.
 	 */
 	private Hashtable getTable(HttpServletRequest request) {
 		Hashtable table = new Hashtable();
 		Enumeration e = request.getParameterNames();
 		while (e.hasMoreElements()) {
 			String key = (String)e.nextElement();
 			table.put(key, request.getParameter(key));
 		}
 		return table;
 	}
 	
 }
