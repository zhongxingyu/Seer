 package com.cnnic.whois.web;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import com.cnnic.whois.execption.QueryException;
 import com.cnnic.whois.util.DataFormat;
 import com.cnnic.whois.util.WhoisProperties;
 import com.cnnic.whois.util.WhoisUtil;
 
 public class WhoisFilter implements Filter {
 
 	/**
 	 * Called by the web container to indicate to a filter that it is being
 	 * taken out of service. This method is only called once all threads within
 	 * the filter's doFilter method have exited or after a timeout period has
 	 * passed. After the web container calls this method, it will not call the
 	 * doFilter method again on this instance of the filter.
 	 */
 	@Override
 	public void destroy() {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * The doFilter method of the Filter is called by the container each time a
 	 * request/response pair is passed through the chain due to a client request
 	 * for a resource at the end of the chain. The FilterChain passed in to this
 	 * method allows the Filter to pass on the request and response to the next
 	 * entity in the chain.
 	 */
 	@Override
 	public void doFilter(ServletRequest arg0, ServletResponse arg1,
 			FilterChain chain) throws IOException, ServletException {
 		HttpServletRequest request = (HttpServletRequest) arg0;
 		HttpServletResponse response = (HttpServletResponse) arg1;
 
 		String role = "anonymous";
 		if (request.isUserInRole("authenticated"))
 			role = "authenticated";
 		if (request.isUserInRole("root"))
 			role = "root";
 		long currentTime = new Long(System.currentTimeMillis());
 
 		long accessTime = currentTime;
 		boolean isQueryOverTime = queryControl(request.getRemoteAddr(), accessTime, role);
 		
 		String userAgent = "";
 		try{
 			userAgent = request.getHeader("user-agent").toLowerCase();
 		}
 		catch(Exception e){
 			userAgent = "";
 		}
 		
 		String format = WhoisUtil.getFormatCookie(request);
 
 		CharSequence ie = "msie";
 		CharSequence firefox = "firefox";
 		CharSequence chrome = "chrome";
 		CharSequence safiri = "safiri";
 		CharSequence opera = "opera";
 		if (format == null && (userAgent.contains(ie) || userAgent.contains(firefox) ||
 				userAgent.contains(chrome) || userAgent.contains(safiri) || userAgent.contains(opera)))
 			format = "application/html";
 		if (format == null){
 			format = request.getHeader("Accept"); 
 			if (format == null){
 				format = "application/json";
 			}
 
 			CharSequence sqhtml = "html";			
 			if(format.contains(sqhtml))
 				format = "application/html";
 		}
		if(format == null || !(format.equals("application/html") || format.equals("application/json") 
				|| format.equals("application/rdap+json")|| format.equals("application/xml"))){
 			format = "application/html";
 		}
 		String queryInfo = "";
 		String queryType = "";
 		
 		String path = request.getRequestURI();
 		
 		if(!path.equals("")){
 			queryInfo = path.substring(request.getContextPath().length() + 1);
 			
 			if(queryInfo.equals("") && (userAgent.contains(ie) || userAgent.contains(firefox) ||
 					userAgent.contains(chrome) || userAgent.contains(safiri) || userAgent.contains(opera))){
 				format = "application/html";
 				WhoisUtil.clearFormatCookie(request, response);
 			}
 			if(queryInfo.indexOf("/") != -1){				
 				queryType = queryInfo.substring(0, queryInfo.indexOf("/"));
 			}
 		}
 		
 		if (isQueryOverTime) {
 			chain.doFilter(request, response);
 			
 		} else {
 			request.setCharacterEncoding("utf-8");
 			response.setCharacterEncoding("utf-8");
 			
 			Map<String, Object> map = new LinkedHashMap<String, Object>();
 			
 			try {
 				map = WhoisUtil.processError(WhoisUtil.RATELIMITECODE, role, format);
 			} catch (QueryException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			PrintWriter out = response.getWriter();
 			request.setAttribute("queryFormat", format);
 			response.setHeader("Access-Control-Allow-Origin", "*");
 			response.setStatus(429);
 			
 			if(format.equals("application/html")){
 				response.sendError(429);
 			}else if(format.equals("application/json") || format.equals("application/rdap+json") || format.equals("application/rdap+json;application/json")){
 				response.setHeader("Content-Type", "application/json");
 				out.print(DataFormat.getJsonObject(map));
 			}else if(format.equals("application/xml")){
 				response.setHeader("Content-Type", "application/xml");
 				out.write(DataFormat.getXmlString(map));
 			}else{
 				response.setHeader("Content-Type", "text/plain");
 				out.write(DataFormat.getPresentation(map));
 			}
 		}
 	}
 	
 	/**
 	 * Called by the web container to indicate to a filter that it is being
 	 * placed into service. The servlet container calls the init method exactly
 	 * once after instantiating the filter. The init method must complete
 	 * successfully before the filter is asked to do any filtering work.
 	 */
 	@Override
 	public void init(FilterConfig arg0) throws ServletException {
 
 	}
 
 	/**
 	 * Control call this method when the number of queries in a certain period
 	 * of time
 	 * 
 	 * @param accessTime
 	 * @param role
 	 * @param userIp
 	 * @return If you meet certain requirements, return true, otherwise returns
 	 *         false
 	 */
 	private boolean queryControl(String userIp, long accessTime, String role) {
 		if (WhoisUtil.queryRemoteIPMap.get(userIp) == null) {
 			WhoisUtil.queryRemoteIPMap.put(userIp, accessTime);
 			return true;
 		} else {
 		
 			long time = accessTime - WhoisUtil.queryRemoteIPMap.get(userIp);
 			boolean isOverTime = true;
 			
 			if (role.equals(WhoisUtil.ANONYMOUS)) {
 				if (time <= WhoisProperties.getAnonymousExpireTime())
 					isOverTime = false;
 			} else if (role.equals(WhoisUtil.AUTHENTICATED)) {
 				if (time <= WhoisProperties.getAuthenticatedExpireTime())
 					isOverTime = false;
 			} else if(role.equals(WhoisUtil.ROOT)){
 				if (time <= WhoisProperties.getRootExpireTime())
 					isOverTime = false;
 			}
 			
 			WhoisUtil.queryRemoteIPMap.put(userIp, accessTime);
 			if (time >= WhoisProperties.getExpireTime())
 				WhoisUtil.queryRemoteIPMap.remove(userIp);
 			return isOverTime;
 		}
 	}
 }
