 package logbook.server;
 
 import static logbook.shared.scaffold.LogBookConstant.UNIQUE_ID;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 
 public class AuthenticationFilter implements Filter {
 	
 	private static Logger Log = Logger.getLogger(AuthenticationFilter.class);
 	
 	@Override
 	public void destroy() {		
 		Log.info("Inside destroy");
 	}
 
 	@Override
 	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
 			FilterChain filterChain) throws IOException, ServletException {
 		Log.info("Inside doFilter");
 		HttpServletRequest request = (HttpServletRequest)servletRequest;
 		//Enumeration<String> names = request.getHeaderNames();
 		/*Log.info("Headers are:");
 		while(names.hasMoreElements())
 		{
 			String headerName = names.nextElement();
 			Log.info(headerName + " : " + request.getHeader(headerName));
 		}
 		
 		Log.info("Attributes are:");
 		names = request.getAttributeNames();
 		while(names.hasMoreElements())
 		{
 			String attributeName = names.nextElement();
 			Log.info(attributeName + " : " + request.getAttribute(attributeName));
 		}
 		
 	
 		
 		Cookie[] cookies = request.getCookies();
 		Log.info("Cookies are:");
 		int index = 0;
 		while(index > cookies.length)
 		{			
 			Log.info(cookies[index].getName() + " : " + cookies[index].getValue());
 		}
 		*/
 
 		/*Administrator administrator = Administrator.findAdministratorsByEmail("foo@bar.com");
 		
 		Log.info("Administrator : " + administrator);
 		
 		if(administrator != null)
 		{
 			Log.info("Login successfully" );
 			filterChain.doFilter(servletRequest, servletResponse);
 		}*/
 		
 		/* for production uniqueID and for testing uid */
 		//String userId = request.getHeader("uniqueID");
 		//String userId = request.getHeader("uid");
		String uniqueID = "1";
 		Log.info("UNIQUE_ID : " + UNIQUE_ID);
 		boolean flag = false;
 		// Session Management
 		HttpSession session = request.getSession(false);
 		
 		if(session != null) {
 			String sessionUserId = (String) session.getAttribute(UNIQUE_ID);
 			if(uniqueID.equals(sessionUserId)) {
 				Log.info("----> Authenticated using session");
 				flag = true;
 			}else {
 				flag = authenticationUsingDB(servletResponse, uniqueID);
 				if(flag == true) {
 					session.setAttribute(UNIQUE_ID, uniqueID);
 					Log.info("----> Authenticated using DB");
 				}
 			}
 		}else {
 			Log.info("----> Authenticated using New Session");
 			flag = authenticationUsingDB(servletResponse, uniqueID);
 			if(flag == true){
 				session = request.getSession();
 				session.setAttribute(UNIQUE_ID, uniqueID);
 			}		
 		}
 		
 		
 		if(flag)
 			filterChain.doFilter(servletRequest, servletResponse);
 		else
 			((HttpServletResponse) servletResponse).sendError(HttpServletResponse.SC_UNAUTHORIZED, uniqueID + " is not authorized to oscemanager.");
 		
 		/*Log.info("User Id : " + userId);
 		if(userId == null || !userId.trim().equals("myself"))		
 			((HttpServletResponse) servletResponse).sendError(HttpServletResponse.SC_UNAUTHORIZED, userId + " is not authorized to oscemanager.");
 		else
 			filterChain.doFilter(servletRequest, servletResponse);*/
 	}
 
 	private boolean authenticationUsingDB(ServletResponse servletResponse,
 			String userId) {
 		boolean flag = true;
 		
 		/*try{
 			List<Administrator> listAdministrator = Administrator.findAllAdministrators();
 			if(userId !=null && userId.equals("210760@vho-switchaai.ch"))
 			{
 				Log.info("Login successfully by 210760@vho-switchaai.ch" );
 				flag=true;
 			}
 			else if(userId !=null && userId.equals("myself")){
 				Log.info("Login successfully by myself" );
 				flag=true;
 			}
 			else if(listAdministrator != null)
 			{
 				Log.info("listAdministrator : " + listAdministrator.size());
 				for(Administrator administrator:listAdministrator)
 				{
 					if(administrator.getEmail().equals(userId))
 					{
 						Log.info("Login successfully" );
 						flag=true;
 						break;
 						
 					}
 				}
 			}
 			else
 			{
 				Log.info("Login failes");
 				((HttpServletResponse) servletResponse).sendError(HttpServletResponse.SC_UNAUTHORIZED, userId + " is not authorized to oscemanager.");
 			}
 		}catch(Exception e)
 		{
 			flag=false;
 		}*/
 		return flag;
 	}
 
 	@Override
 	public void init(FilterConfig arg0) throws ServletException {
 		Log.info("Inside init");		
 	}
 	
 
 }
