 package com.pagesociety.web.gateway;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Enumeration;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 
 import com.pagesociety.util.RandomGUID;
 import com.pagesociety.web.ApplicationBootstrap;
 import com.pagesociety.web.UserApplicationContext;
 import com.pagesociety.web.WebApplication;
 import com.pagesociety.web.config.UrlMapInitParams;
 import com.pagesociety.web.config.UrlMapInitParams.UrlMapInfo;
 import com.pagesociety.web.exception.WebApplicationException;
 import com.pagesociety.web.module.WebModule;
 
 public class HttpRequestRouter extends HttpServlet
 {
 	private static final long serialVersionUID = 454337901877827397L;
 	private static final Logger logger = Logger.getLogger(HttpRequestRouter.class);
 	//
 	private static final String HTTP = "http";
 	private static final int SESSION_TIMEOUT = 30 * 60 * 1000;
 	public static final String USER_AGENT_UCTX_KEY = "USER-AGENT";
 	//
 	private ServletConfig _servlet_config;
 	private WebApplication _web_application;
 	private String _web_url;
 	private String _web_url_secure;
 	private boolean _is_closed;
 	//
 	private StaticHttpGateway static_gateway;
 	private AmfGateway amf_gateway;
 	private JsonGateway json_gateway;
 	private FreemarkerGateway freemarker_gateway;
 	private FormGateway form_gateway;
 	private RawGateway  raw_gateway;
 	private JavaGateway java_gateway;
 	private String	_session_cookie_domain;
 
 	public void init(ServletConfig cfg) throws ServletException
 	{
 		_servlet_config = cfg;
 		_web_application = (WebApplication)cfg.getServletContext().getAttribute(ApplicationBootstrap.APPLICATION_ATTRIBUTE_NAME);
 
 		if (_web_application == null)
 			throw new ServletException("WebApplication was not initialized. Make sure ApplicationBootstrap has been loaded.");
 
 
 			// _web_application.setGateway(this);
 		_web_application.getSessionManager(HTTP).setTimeout(SESSION_TIMEOUT);
 		_web_url = _web_application.getConfig().getWebRootUrl();
 		_web_url_secure = _web_application.getConfig().getWebRootUrlSecure();
 		_is_closed = false;
 		set_session_cookie_domain();
 		//
 		static_gateway = new StaticHttpGateway();
 		amf_gateway = new AmfGateway(_web_application);
 		json_gateway = new JsonGateway(_web_application);
 		freemarker_gateway = new FreemarkerGateway(_web_application);
 		form_gateway = new FormGateway(_web_application);
 		raw_gateway  = new RawGateway(_web_application);
 		java_gateway  = new JavaGateway(_web_application);
 		//
 		logger.info("ServletGateway init complete");
 	}
 
 
 	private void set_session_cookie_domain() throws ServletException
 	{
 		String url = _web_application.getConfig().getWebRootUrl();
 		String[] protocol_parts = url.split("//");
 		if(protocol_parts.length < 2)
 		{
 			throw new ServletException("PROTOCOL SHOULD BE PART OF PROPERTY WEB ROOT URL");
 		}
 		url = "";
 		for(int i = 1;i < protocol_parts.length;i++)
 			url+=protocol_parts[i];
 
 		String[] domain_parts = url.split("\\.");
 		if(domain_parts.length < 2)
 		{
 			//throw new ServletException("SEEMS LIKE YOU HAVE A BAD WEB ROOT URL: "+_web_application.getConfig().getWebRootUrl());
 			_session_cookie_domain = null;
 		}
 		else
 			_session_cookie_domain = "."+domain_parts[domain_parts.length-2]+'.'+domain_parts[domain_parts.length-1];
 		System.out.println("BASE COOKIE DOMAIN IS "+_session_cookie_domain);
 	}
 
 	public void open()
 	{
 		_is_closed = false;
 	}
 
 	public void close()
 	{
 		_is_closed = true;
 		// TODO
 		// while(_is_serving)
 		// sleep;
 	}
 
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 			throws IOException, ServletException
 	{
 		logger.debug("GETTING " + request.getRequestURI() + " from " + request.getRemoteHost());
 		long t = System.currentTimeMillis();
 		try
 		{
 			doService(request, response);
 		}
 		catch (WebApplicationException e)
 		{
 			System.err.println("ERROR CALLER IP WAS: "+request.getRemoteAddr());
 			e.printStackTrace();
 			throw new ServletException(e);
 		}
 		logger.debug("GET " + request.getRequestURI() + " from " + request.getRemoteHost() + " took " + (System.currentTimeMillis() - t) + "ms");
 	}
 
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws IOException, ServletException
 	{
 		long t = System.currentTimeMillis();
 		logger.debug("POSTING " + request.getRequestURI() + " from " + request.getRemoteHost());
 		try
 		{
 			doService(request, response);
 		}
 		catch (WebApplicationException e)
 		{
 			System.err.println("ERROR CALLER IP WAS: "+request.getRemoteAddr());
 			e.printStackTrace();
 			throw new ServletException(e);
 		}
 		logger.debug("POST " + request.getRequestURI() + " from " + request.getRemoteHost() + " took " + (System.currentTimeMillis() - t) + "ms");
 	}
 
 	private void doService(HttpServletRequest request, HttpServletResponse response)
 			throws IOException, ServletException, WebApplicationException
 	{
 		if (_is_closed)
 		{
 			// TODO
 			// if html/freemarker: serve_file(_maintenance_page, request, response);
 			// if javascript/amf: throw new ClosedException();
 			PrintWriter out = response.getWriter();
 			out.write("CLOSED");
 			out.close();
 			return;
 		}
 		// STATIC RESOURCE SERVING
 		String requestPath = request.getRequestURI().substring(request.getContextPath().length());
 		String completeUrl = getUrl(request);
 		String mime_type = _servlet_config.getServletContext().getMimeType(requestPath);
 
 //		System.out.println("RequestPath is "+requestPath);
 //		System.out.println("completeUrl is "+completeUrl);
 //		System.out.println("mime_type is "+mime_type);
 		
 		response.setHeader("Access-Control-Allow-Origin", "*");
 		response.setHeader("Access-Control-Allow-Methods", "GET, POST");
 		response.setHeader("Access-Control-Allow-Headers", "Content-Type");
 		response.setHeader("Access-Control-Max-Age", "86400");
 
 		if (mime_type != null)
 		{
 			for (int i = 0; i < GatewayConstants.MIME_TYPE_PREFIXES.length; i++)
 			{
 				if (mime_type.startsWith(GatewayConstants.MIME_TYPE_PREFIXES[i]))
 				{
 					File request_file = new File(_web_application.getConfig().getWebRootDir(), requestPath);
 					static_gateway.serveFile(request_file, mime_type, request, response);
 					return;
 				}
 			}
 		}
 		UserApplicationContext uctx = get_user_context(request, response);
 		try{
 			_web_application.setCallingUserContext(uctx);
 			set_uctx_system_info(uctx,request,response);
 			//System.out.println("!!!!>>"+uctx.getId()+" "+completeUrl);
 			// FORM first, because sometimes it uses the ps_session_id and doesn't want the redirect to occur
 			if (requestPath.endsWith(GatewayConstants.SUFFIX_FORM))
 			{
 				form_gateway.doService(uctx, request, response);
 				return;
 			}
 			// redirect if the session id was included in requests for the following...
 			else if (request.getParameter(GatewayConstants.SESSION_ID_KEY)!=null)
 			{
 				response.sendRedirect( getPathWithoutSessionId(request) );
 				return;
 			}
 			// AMF
 			else if (requestPath.endsWith(GatewayConstants.SUFFIX_AMF))
 			{
 				amf_gateway.doService(uctx, request, response);
 				return;
 			}
 			// JSON
 			else if (requestPath.endsWith(GatewayConstants.SUFFIX_JSON))
 			{
 				json_gateway.doService(uctx, request, response);
 				return;
 			}
 			//RAW
 			else if (requestPath.endsWith(GatewayConstants.SUFFIX_RAW))
 			{
 				raw_gateway.doService(uctx, request, response);
 				return;
 			}
 
 			//JAVA
 			else if (requestPath.endsWith(GatewayConstants.SUFFIX_JAVA))
 			{
 				java_gateway.doService(uctx, request, response);
 				return;
 			}
 			//registered gateways
 
 
 			// MAPPED from config file or module//
 			Object[] url_mapped_request = _web_application.getMapping(completeUrl);
 
 
 			if (url_mapped_request != null)
 			{
 				UrlMapInfo url_map_info = (UrlMapInfo)url_mapped_request[0];
 				//TODO here we are hackinginto the gateway and letting a module i.e. SiteConfigModule
 				//handle the whole request. need to refactor gateways and session managers into modules
 
 				if(url_map_info.getHandler() != null)
 				{
 					_web_application.INFO("MATCHED HANDLER "+((WebModule)url_map_info.getHandler()).getName());
 					if(url_map_info.getHandler().handleRequest(uctx, request, response))
 					{
 						_web_application.INFO("HANDLER HANDLED");
 						return;
 					}
 					_web_application.INFO("HANDLER PASSED");
 				}
 				else
 				{
 					String path = (String)url_mapped_request[1];
 					_web_application.INFO("MATCHED "+path);
 					if (url_map_info.isSecure()==UrlMapInitParams.SECURE && !completeUrl.startsWith(_web_url_secure))
 					{
 						response.sendRedirect( get_path(_web_url_secure,getContextPathEtc(request),uctx) );
 						return;
 					}
 					else if (url_map_info.isSecure()==UrlMapInitParams.NOT_SECURE && !completeUrl.startsWith(_web_url))
 					{
 						response.sendRedirect( get_path(_web_url,getContextPathEtc(request),uctx) );
 						return;
 					}
 		//TODO
 		//this might work, but it might match make everything forward forever, too!
 					else
 					{
 
 
 						RequestDispatcher dispatcher = request.getRequestDispatcher(path);
 						dispatcher.forward(request, response);
 						return;
 					}
 				}
 			}
 
 			// FREEMARKER
 			if (is_freemarker_path(requestPath))
 			{
 				freemarker_gateway.doService(uctx, requestPath, request, response);
 				return;
 			}
 
 			// UNKNOWN
 			File request_file = new File(_web_application.getConfig().getWebRootDir(), requestPath);
 			if(request_file.exists())
 			{
 				static_gateway.serveFile(request_file, mime_type, request, response);
 				return;
 			}
 			else
 			{
				response.sendError(404);
 				return;
 			}
 
 		}finally
 		{
 			_web_application.removeCallingUserContext();
 		}
 	}
 
 
 	private void set_uctx_system_info(UserApplicationContext uctx,HttpServletRequest request,HttpServletResponse response)
 	{
 		set_uctx_user_agent_info(uctx, request, response);
 
 	}
 
 	private void set_uctx_user_agent_info(UserApplicationContext uctx,HttpServletRequest request,HttpServletResponse response)
 	{
 		String agent_string = request.getHeader("user-agent");
 		uctx.setProperty(USER_AGENT_UCTX_KEY, agent_string);
 
 		//TODO: check out the UserAgentTools below and put that crap
 		//in the usercontext
 
 //		String[] os_info 	  = UserAgentTools.getOS(agent_string);
 //		String[] browser_info = UserAgentTools.getBrowser(agent_string);
 	}
 
 	private void dump_http_headers(HttpServletRequest request,HttpServletResponse response)
 	{
 		Enumeration<String> header_names = (Enumeration<String>)request.getHeaderNames();
 		while(header_names.hasMoreElements())
 		{
 			String header_name  = header_names.nextElement();
 			System.out.println("\t"+header_name+" = "+request.getHeader(header_name));
 		}
 
 	}
 
 	private boolean is_freemarker_path(String path)
 	{
 		for (int i = 0; i < GatewayConstants.SUFFIXES_FREEMARKER.length; i++)
 		{
 			if (path.endsWith(GatewayConstants.SUFFIXES_FREEMARKER[i]))
 				return true;
 		}
 		return false;
 
 	}
 
 	private String get_path(String root, String path, UserApplicationContext uctx)
 	{
 		StringBuilder b = new StringBuilder();
 		b.append(root);
 		b.append(path);
 		if (path.indexOf(GatewayConstants.SESSION_ID_KEY)==-1)
 		{
 			if (path.indexOf("?")==-1)
 				b.append("?");
 			else
 				b.append("&");
 			b.append(GatewayConstants.SESSION_ID_KEY);
 			b.append("=");
 			b.append(uctx.getId());
 		}
 		return b.toString();
 	}
 
 	///////////////////////////////////////////////////////////////////////////////////////////
 	///////////////////////////////////////////////////////////////////////////////////////////
 	private String getPathWithoutSessionId(HttpServletRequest req)
 	{
 		String scheme = req.getScheme();             // http
         String serverName = req.getServerName();     // hostname.com
         int serverPort = req.getServerPort();        // 80
         String contextPath = req.getContextPath();   // /mywebapp
         String servletPath = req.getServletPath();   // /servlet/MyServlet
         String pathInfo = req.getPathInfo();         // /a/b;c=123
         String queryString = req.getQueryString();    // d=789
 
         // Reconstruct original requesting URL
         StringBuilder b = new StringBuilder();
         b.append(scheme);
         b.append("://");
         b.append(serverName);
         if (serverPort != 80 && serverPort != 443)
         {
         	b.append(":");
             b.append(serverPort);
         }
         b.append(contextPath);
         b.append(servletPath);
         if (pathInfo != null)
         {
         	b.append(pathInfo);
         }
         if (queryString != null)
         {
         	StringBuilder bb = new StringBuilder();
         	bb.append("?");
         	String[] qp = queryString.split("&");
         	for (int i=0; i<qp.length; i++)
         	{
         		if (!qp[i].startsWith(GatewayConstants.SESSION_ID_KEY))
         		{
         			bb.append(qp[i]);
         			bb.append("&");
         		}
         	}
         	if (bb.length()!=1)
         		b.append(bb);
         }
         return b.toString();
 	}
 
 	private String getContextPathEtc(HttpServletRequest req)
 	{
         String contextPath = req.getContextPath();   // /mywebapp
         String servletPath = req.getServletPath();   // /servlet/MyServlet
         String pathInfo = req.getPathInfo();         // /a/b;c=123
         String queryString = req.getQueryString();          // d=789
 
         StringBuilder b = new StringBuilder();
 		b.append(contextPath);
         b.append(servletPath);
         if (pathInfo != null)
         {
         	b.append(pathInfo);
         }
         if (queryString != null)
         {
         	b.append("?");
             b.append(queryString);
         }
         return b.toString();
 	}
 
 	public static String getUrl(HttpServletRequest req)
 	{
         String scheme = req.getScheme();             // http
         String serverName = req.getServerName();     // hostname.com
         int serverPort = req.getServerPort();        // 80
         String contextPath = req.getContextPath();   // /mywebapp
         String servletPath = req.getServletPath();   // /servlet/MyServlet
         String pathInfo = req.getPathInfo();         // /a/b;c=123
         String queryString = req.getQueryString();   // d=789
 
         // Reconstruct original requesting URL
         StringBuilder b = new StringBuilder();
         b.append(scheme);
         b.append("://");
         b.append(serverName);
         if (serverPort != 80 && serverPort != 443)
         {
         	b.append(":");
             b.append(serverPort);
         }
         b.append(contextPath);
         b.append(servletPath);
         if (pathInfo != null)
         {
         	b.append(pathInfo);
         }
         if (queryString != null)
         {
         	b.append("?");
             b.append(queryString);
         }
         return b.toString();
 	}
 
 
 
 	private UserApplicationContext get_user_context(HttpServletRequest request,
 			HttpServletResponse response)
 	{
 
 		String http_sess_id = null;
 		Cookie cookie = null;
 		Cookie[] cookies = request.getCookies();
 
 		if (cookies != null)
 		{
 
 			int s = cookies.length;
 			for (int i=0; i<s; i++)
 			{
 				if (cookies[i].getName().equals(GatewayConstants.SESSION_ID_KEY))
 				{
 					cookie = cookies[i];
 					break;
 				}
 			}
 		}
 		//when we are switching domains and for upload we pass jessionid in request paramters//
 		if (request.getParameter(GatewayConstants.SESSION_ID_KEY) != null)
 			http_sess_id = set_session_cookie(response,request.getParameter(GatewayConstants.SESSION_ID_KEY));
 		else
 		{
 			if (cookie==null)
 				http_sess_id = set_session_cookie(response,null);
 			else
 				http_sess_id = cookie.getValue();
 		}
 		return _web_application.getUserContext(HTTP, http_sess_id);
 	}
 
 	private String set_session_cookie(HttpServletResponse response,String http_session_id)
 	{
 		if(http_session_id == null)
 			http_session_id = RandomGUID.getGUID();
 		Cookie c = new Cookie(GatewayConstants.SESSION_ID_KEY, http_session_id);
 		c.setMaxAge(-1);
 		c.setPath("/");
 		if(_session_cookie_domain != null)
 			c.setDomain(_session_cookie_domain);
 		response.addCookie(c);
 		return http_session_id;
 	}
 
 /* USER AGENT UTIL STUFF */
 
 static class UserAgentTools {
 
 		  public static String getFirstVersionNumber(String a_userAgent, int a_position, int numDigits) {
 		    String ver = getVersionNumber(a_userAgent, a_position);
 		    if (ver==null) return "";
 		    int i = 0;
 		    String res="";
 		    while (i<ver.length() && i<numDigits) {
 		      res+=String.valueOf(ver.charAt(i));
 		      i++;
 		    }
 		    return res;
 		  }
 
 		  public static String getVersionNumber(String a_userAgent, int a_position) {
 		      if (a_position<0) return "";
 		      StringBuffer res = new StringBuffer();
 		      int status = 0;
 
 		      while (a_position < a_userAgent.length()) {
 		          char c = a_userAgent.charAt(a_position);
 		          switch (status) {
 		            case 0:
 		              if (c == ' ' || c=='/') break;
 		              if (c == ';' || c==')') return "";
 		              status = 1;
 		            case 1:
 		              if (c == ';' || c=='/' || c==')' || c=='(' || c=='[') return res.toString().trim();
 		              if (c == ' ') status = 2;
 		              res.append(c);
 		              break;
 		            case 2:
 		              if ((Character.isLetter(c) &&
 		                   Character.isLowerCase(c)) ||
 		                  Character.isDigit(c)) {
 		                  res.append(c);
 		                  status=1;
 		              } else
 		                  return res.toString().trim();
 		              break;
 		          }
 		          a_position++;
 		      }
 		      return res.toString().trim();
 		  }
 
 		  public static String[]getArray(String a, String b, String c) {
 		    String[]res = new String[3];
 		    res[0]=a;
 		    res[1]=b;
 		    res[2]=c;
 		    return res;
 		  }
 
 		  public static String[] getBotName(String userAgent) {
 		    userAgent = userAgent.toLowerCase();
 		    int pos=0;
 		    String res=null;
 		    if ((pos=userAgent.indexOf("help.yahoo.com/"))>-1) {
 		        res= "Yahoo";
 		        pos+=7;
 		    } else
 		    if ((pos=userAgent.indexOf("google/"))>-1) {
 		        res= "Google";
 		        pos+=7;
 		    } else
 		    if ((pos=userAgent.indexOf("msnbot/"))>-1) {
 		        res= "MSNBot";
 		        pos+=7;
 		    } else
 		    if ((pos=userAgent.indexOf("googlebot/"))>-1) {
 		        res= "Google";
 		        pos+=10;
 		    } else
 		    if ((pos=userAgent.indexOf("webcrawler/"))>-1) {
 		        res= "WebCrawler";
 		        pos+=11;
 		    } else
 		    //<SPAN class="codecomment"> The following two bots don't have any version number in their User-Agent strings.</span>
 		    if ((pos=userAgent.indexOf("inktomi"))>-1) {
 		        res= "Inktomi";
 		        pos=-1;
 		    } else
 		    if ((pos=userAgent.indexOf("teoma"))>-1) {
 		        res= "Teoma";
 		        pos=-1;
 		    }
 		    if (res==null) return null;
 		    return getArray(res,res,res + getVersionNumber(userAgent,pos));
 		  }
 
 
 		  public static String[] getOS(String userAgent) {
 			    if (getBotName(userAgent)!=null) return getArray("Bot","Bot","Bot");
 			    String[]res = null;
 			    int pos;
 			    if ((pos=userAgent.indexOf("Windows-NT"))>-1) {
 			        res = getArray("Win","WinNT","Win"+getVersionNumber(userAgent,pos+8));
 			    } else
 			    if (userAgent.indexOf("Windows NT")>-1) {
 			        // The different versions of Windows NT are decoded in the verbosity level 2
 			        // ie: Windows NT 5.1 = Windows XP
 			        if ((pos=userAgent.indexOf("Windows NT 5.1"))>-1) {
 			            res = getArray("Win","WinXP","Win"+getVersionNumber(userAgent,pos+7));
 			        } else
 			        if ((pos=userAgent.indexOf("Windows NT 6.0"))>-1) {
 			            res = getArray("Win","Vista","Vista"+getVersionNumber(userAgent,pos+7));
 			        } else
 			        if ((pos=userAgent.indexOf("Windows NT 5.0"))>-1) {
 			            res = getArray("Win","Seven","Seven "+getVersionNumber(userAgent,pos+7));
 			        } else
 			        if ((pos=userAgent.indexOf("Windows NT 5.0"))>-1) {
 			            res = getArray("Win","Win2000","Win"+getVersionNumber(userAgent,pos+7));
 			        } else
 			        if ((pos=userAgent.indexOf("Windows NT 5.2"))>-1) {
 			            res = getArray("Win","Win2003","Win"+getVersionNumber(userAgent,pos+7));
 			        } else
 			        if ((pos=userAgent.indexOf("Windows NT 4.0"))>-1) {
 			            res = getArray("Win","WinNT4","Win"+getVersionNumber(userAgent,pos+7));
 			        } else
 			        if ((pos=userAgent.indexOf("Windows NT)"))>-1) {
 			            res = getArray("Win","WinNT","WinNT");
 			        } else
 			        if ((pos=userAgent.indexOf("Windows NT;"))>-1) {
 			            res = getArray("Win","WinNT","WinNT");
 			        } else
 			        res = getArray("Win","<B>WinNT?</B>","<B>WinNT?</B>");
 			    } else
 			    if (userAgent.indexOf("Win")>-1) {
 			        if (userAgent.indexOf("Windows")>-1) {
 			            if ((pos=userAgent.indexOf("Windows 98"))>-1) {
 			                res = getArray("Win","Win98","Win"+getVersionNumber(userAgent,pos+7));
 			            } else
 			            if ((pos=userAgent.indexOf("Windows_98"))>-1) {
 			                res = getArray("Win","Win98","Win"+getVersionNumber(userAgent,pos+8));
 			            } else
 			            if ((pos=userAgent.indexOf("Windows 2000"))>-1) {
 			                res = getArray("Win","Win2000","Win"+getVersionNumber(userAgent,pos+7));
 			            } else
 			            if ((pos=userAgent.indexOf("Windows 95"))>-1) {
 			                res = getArray("Win","Win95","Win"+getVersionNumber(userAgent,pos+7));
 			            } else
 			            if ((pos=userAgent.indexOf("Windows 9x"))>-1) {
 			                res = getArray("Win","Win9x","Win"+getVersionNumber(userAgent,pos+7));
 			            } else
 			            if ((pos=userAgent.indexOf("Windows ME"))>-1) {
 			                res = getArray("Win","WinME","Win"+getVersionNumber(userAgent,pos+7));
 			            } else
 			            if ((pos=userAgent.indexOf("Windows 3.1"))>-1) {
 			                res = getArray("Win","Win31","Win"+getVersionNumber(userAgent,pos+7));
 			            }
 			            // If no version was found, rely on the following code to detect "WinXX"
 			            // As some User-Agents include two references to Windows
 			            // Ex: Mozilla/5.0 (Windows; U; Win98; en-US; rv:1.5)
 			        }
 			        if (res == null) {
 			            if ((pos=userAgent.indexOf("Win98"))>-1) {
 			                res = getArray("Win","Win98","Win"+getVersionNumber(userAgent,pos+3));
 			            } else
 			            if ((pos=userAgent.indexOf("Win31"))>-1) {
 			                res = getArray("Win","Win31","Win"+getVersionNumber(userAgent,pos+3));
 			            } else
 			            if ((pos=userAgent.indexOf("Win95"))>-1) {
 			                res = getArray("Win","Win95","Win"+getVersionNumber(userAgent,pos+3));
 			            } else
 			            if ((pos=userAgent.indexOf("Win 9x"))>-1) {
 			                res = getArray("Win","Win9x","Win"+getVersionNumber(userAgent,pos+3));
 			            } else
 			            if ((pos=userAgent.indexOf("WinNT4.0"))>-1) {
 			                res = getArray("Win","WinNT4","Win"+getVersionNumber(userAgent,pos+3));
 			            } else
 			            if ((pos=userAgent.indexOf("WinNT"))>-1) {
 			                res = getArray("Win","WinNT","Win"+getVersionNumber(userAgent,pos+3));
 			            }
 			        }
 			        if (res == null) {
 			            if ((pos=userAgent.indexOf("Windows"))>-1) {
 			              res = getArray("Win","<B>Win?</B>","<B>Win?"+getVersionNumber(userAgent,pos+7)+"</B>");
 			            } else
 			            if ((pos=userAgent.indexOf("Win"))>-1) {
 			              res = getArray("Win","<B>Win?</B>","<B>Win?"+getVersionNumber(userAgent,pos+3)+"</B>");
 			            } else
 			              // Should not happen at this point
 			              res = getArray("Win","<B>Win?</B>","<B>Win?</B>");
 			        }
 			    } else
 			    if ((pos=userAgent.indexOf("Mac OS X"))>-1) {
 			        if ((userAgent.indexOf("iPhone"))>-1) {
 			            pos = userAgent.indexOf("iPhone OS");
 			            if ((userAgent.indexOf("iPod"))>-1) {
 			                res = getArray("iOS","iOS-iPod","iOS-iPod "+((pos<0)?"":getVersionNumber(userAgent,pos+9)));
 			            } else {
 			                res = getArray("iOS","iOS-iPhone","iOS-iPhone "+((pos<0)?"":getVersionNumber(userAgent,pos+9)));
 			            }
 			        } else
 			        if ((userAgent.indexOf("iPad"))>-1) {
 			            pos = userAgent.indexOf("CPU OS");
 			            res = getArray("iOS","iOS-iPad","iOS-iPad "+((pos<0)?"":getVersionNumber(userAgent,pos+6)));
 			        } else
 			            res = getArray("Mac","MacOSX","MacOS "+getVersionNumber(userAgent,pos+8));
 			    } else
 			    if ((pos=userAgent.indexOf("Android"))>-1) {
 			        res = getArray("Linux","Android","Android "+getVersionNumber(userAgent,pos+8));
 			    } else
 			    if ((pos=userAgent.indexOf("Mac_PowerPC"))>-1) {
 			        res = getArray("Mac","MacPPC","MacOS "+getVersionNumber(userAgent,pos+3));
 			    } else
 			    if ((pos=userAgent.indexOf("Macintosh"))>-1) {
 			        if (userAgent.indexOf("PPC")>-1)
 			            res = getArray("Mac","MacPPC","MacOS?");
 			        else
 			            res = getArray("Mac?","Mac?","MacOS?");
 			    } else
 			    if ((pos=userAgent.indexOf("FreeBSD"))>-1) {
 			        res = getArray("*BSD","*BSD FreeBSD","FreeBSD "+getVersionNumber(userAgent,pos+7));
 			    } else
 			    if ((pos=userAgent.indexOf("OpenBSD"))>-1) {
 			        res = getArray("*BSD","*BSD OpenBSD","OpenBSD "+getVersionNumber(userAgent,pos+7));
 			    } else
 			    if ((pos=userAgent.indexOf("Linux"))>-1) {
 			        String detail = "Linux "+getVersionNumber(userAgent,pos+5);
 			        String med = "Linux";
 			        if ((pos=userAgent.indexOf("Ubuntu/"))>-1) {
 			            detail = "Ubuntu "+getVersionNumber(userAgent,pos+7);
 			            med+=" Ubuntu";
 			        }
 			        res = getArray("Linux",med,detail);
 			    } else
 			    if ((pos=userAgent.indexOf("CentOS"))>-1) {
 			        res = getArray("Linux","Linux CentOS","CentOS");
 			    } else
 			    if ((pos=userAgent.indexOf("NetBSD"))>-1) {
 			        res = getArray("*BSD","*BSD NetBSD","NetBSD "+getVersionNumber(userAgent,pos+6));
 			    } else
 			    if ((pos=userAgent.indexOf("Unix"))>-1) {
 			        res = getArray("Linux","Linux","Linux "+getVersionNumber(userAgent,pos+4));
 			    } else
 			    if ((pos=userAgent.indexOf("SunOS"))>-1) {
 			        res = getArray("Unix","SunOS","SunOS"+getVersionNumber(userAgent,pos+5));
 			    } else
 			    if ((pos=userAgent.indexOf("IRIX"))>-1) {
 			        res = getArray("Unix","IRIX","IRIX"+getVersionNumber(userAgent,pos+4));
 			    } else
 			    if ((pos=userAgent.indexOf("SonyEricsson"))>-1) {
 			        res = getArray("SonyEricsson","SonyEricsson","SonyEricsson"+getVersionNumber(userAgent,pos+12));
 			    } else
 			    if ((pos=userAgent.indexOf("Nokia"))>-1) {
 			        res = getArray("Nokia","Nokia","Nokia"+getVersionNumber(userAgent,pos+5));
 			    } else
 			    if ((pos=userAgent.indexOf("BlackBerry"))>-1) {
 			        res = getArray("BlackBerry","BlackBerry","BlackBerry"+getVersionNumber(userAgent,pos+10));
 			    } else
 			    if ((pos=userAgent.indexOf("SymbianOS"))>-1) {
 			        res = getArray("SymbianOS","SymbianOS","SymbianOS"+getVersionNumber(userAgent,pos+10));
 			    } else
 			    if ((pos=userAgent.indexOf("BeOS"))>-1) {
 			        res = getArray("BeOS","BeOS","BeOS");
 			    } else
 			    if ((pos=userAgent.indexOf("Nintendo Wii"))>-1) {
 			        res = getArray("Nintendo Wii","Nintendo Wii","Nintendo Wii"+getVersionNumber(userAgent,pos+10));
 			    } else
 			    res = getArray("<b>?</b>","<b>?</b>","<b>?</b>");
 			    return res;
 			  }
 
 
 		  public static String []getBrowser(String userAgent) {
 		    String []botName;
 		    if ((botName=getBotName(userAgent))!=null) return botName;
 		    String[]res = null;
 		    int pos;
 		    if ((pos=userAgent.indexOf("Lotus-Notes/"))>-1) {
 		        res = getArray("LotusNotes","LotusNotes","LotusNotes"+getVersionNumber(userAgent,pos+12));
 		    } else
 		    if ((pos=userAgent.indexOf("Opera"))>-1) {
 		        res = getArray("Opera","Opera"+getFirstVersionNumber(userAgent,pos+5,1),"Opera"+getVersionNumber(userAgent,pos+5));
 		    } else
 		    if (userAgent.indexOf("MSIE")>-1) {
 		        if ((pos=userAgent.indexOf("MSIE 6.0"))>-1) {
 		            res = getArray("MSIE","MSIE6","MSIE"+getVersionNumber(userAgent,pos+4));
 		        } else
 		        if ((pos=userAgent.indexOf("MSIE 5.0"))>-1) {
 		            res = getArray("MSIE","MSIE5","MSIE"+getVersionNumber(userAgent,pos+4));
 		        } else
 		        if ((pos=userAgent.indexOf("MSIE 5.5"))>-1) {
 		            res = getArray("MSIE","MSIE5.5","MSIE"+getVersionNumber(userAgent,pos+4));
 		        } else
 		        if ((pos=userAgent.indexOf("MSIE 5."))>-1) {
 		            res = getArray("MSIE","MSIE5.x","MSIE"+getVersionNumber(userAgent,pos+4));
 		        } else
 		        if ((pos=userAgent.indexOf("MSIE 4"))>-1) {
 		            res = getArray("MSIE","MSIE4","MSIE"+getVersionNumber(userAgent,pos+4));
 		        } else
 		        if ((pos=userAgent.indexOf("MSIE 7"))>-1 && userAgent.indexOf("Trident/4.0")<0) {
 		            res = getArray("MSIE","MSIE7","MSIE"+getVersionNumber(userAgent,pos+4));
 		        } else
 		        if ((pos=userAgent.indexOf("MSIE 8"))>-1 || userAgent.indexOf("Trident/4.0")>-1) {
 		            res = getArray("MSIE","MSIE8","MSIE"+getVersionNumber(userAgent,pos+4));
 		        } else
 		        res = getArray("MSIE","MSIE?","MSIE?"+getVersionNumber(userAgent,userAgent.indexOf("MSIE")+4)+"</B>");
 		    } else
 		    if ((pos=userAgent.indexOf("Gecko/"))>-1) {
 		        res = getArray("Gecko","Gecko","Gecko"+getFirstVersionNumber(userAgent,pos+5,4));
 		        if ((pos=userAgent.indexOf("Camino/"))>-1) {
 		            res[1]+="(Camino)";
 		            res[2]+="(Camino"+getVersionNumber(userAgent,pos+7)+")";
 		        } else
 		        if ((pos=userAgent.indexOf("Chimera/"))>-1) {
 		            res[1]+="(Chimera)";
 		            res[2]+="(Chimera"+getVersionNumber(userAgent,pos+8)+")";
 		        } else
 		        if ((pos=userAgent.indexOf("Firebird/"))>-1) {
 		            res[1]+="(Firebird)";
 		            res[2]+="(Firebird"+getVersionNumber(userAgent,pos+9)+")";
 		        } else
 		        if ((pos=userAgent.indexOf("Phoenix/"))>-1) {
 		            res[1]+="(Phoenix)";
 		            res[2]+="(Phoenix"+getVersionNumber(userAgent,pos+8)+")";
 		        } else
 		        if ((pos=userAgent.indexOf("Galeon/"))>-1) {
 		            res[1]+="(Galeon)";
 		            res[2]+="(Galeon"+getVersionNumber(userAgent,pos+7)+")";
 		        } else
 		        if ((pos=userAgent.indexOf("Firefox/"))>-1) {
 		            res[1]+="(Firefox)";
 		            res[2]+="(Firefox"+getVersionNumber(userAgent,pos+8)+")";
 		        } else
 		        if ((pos=userAgent.indexOf("Netscape/"))>-1) {
 		            if ((pos=userAgent.indexOf("Netscape/6"))>-1) {
 		                res[1]+="(NS6)";
 		                res[2]+="(NS"+getVersionNumber(userAgent,pos+9)+")";
 		            } else
 		            if ((pos=userAgent.indexOf("Netscape/7"))>-1) {
 		                res[1]+="(NS7)";
 		                res[2]+="(NS"+getVersionNumber(userAgent,pos+9)+")";
 		            } else {
 		                res[1]+="(NS?)";
 		                res[2]+="(NS?"+getVersionNumber(userAgent,userAgent.indexOf("Netscape/")+9)+")";
 		            }
 		        }
 		    } else
 		    if ((pos=userAgent.indexOf("Netscape/"))>-1) {
 		        if ((pos=userAgent.indexOf("Netscape/4"))>-1) {
 		            res = getArray("NS","NS4","NS"+getVersionNumber(userAgent,pos+9));
 		        } else
 		            res = getArray("NS","NS?","NS?"+getVersionNumber(userAgent,pos+9));
 		    } else
 		    if ((pos=userAgent.indexOf("Chrome/"))>-1) {
 		        res = getArray("KHTML","KHTML(Chrome)","KHTML(Chrome"+getVersionNumber(userAgent,pos+6)+")");
 		    } else
 		    if ((pos=userAgent.indexOf("Safari/"))>-1) {
 		        res = getArray("KHTML","KHTML(Safari)","KHTML(Safari"+getVersionNumber(userAgent,pos+6)+")");
 		    } else
 		    if ((pos=userAgent.indexOf("Konqueror/"))>-1) {
 		        res = getArray("KHTML","KHTML(Konqueror)","KHTML(Konqueror"+getVersionNumber(userAgent,pos+9)+")");
 		    } else
 		    if ((pos=userAgent.indexOf("KHTML"))>-1) {
 		        res = getArray("KHTML","KHTML?","KHTML?("+getVersionNumber(userAgent,pos+5)+")");
 		    } else
 		    if ((pos=userAgent.indexOf("NetFront"))>-1) {
 		        res = getArray("NetFront","NetFront","NetFront "+getVersionNumber(userAgent,pos+8));
 		    } else
 		    //<SPAN class="codecomment"> We will interpret Mozilla/4.x as Netscape Communicator is and only if x</span>
 		    //<SPAN class="codecomment"> is not 0 or 5</span>
 		    if (userAgent.indexOf("Mozilla/4.")==0 &&
 		        userAgent.indexOf("Mozilla/4.0")<0 &&
 		        userAgent.indexOf("Mozilla/4.5 ")<0) {
 		        res = getArray("Communicator","Communicator","Communicator"+getVersionNumber(userAgent,pos+8));
 		    } else
 		    	return getArray("?","?","?");
 		    return res;
 		  }
 		}
 
 }
