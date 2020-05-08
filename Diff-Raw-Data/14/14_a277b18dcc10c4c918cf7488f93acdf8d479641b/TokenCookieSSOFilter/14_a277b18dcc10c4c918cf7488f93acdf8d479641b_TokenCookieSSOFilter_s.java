 package org.openxava.ex.tools;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang3.StringEscapeUtils;
 import org.openxava.util.UserInfo;
 import org.openxava.util.Users;
 
 /**
  * SSO support based on "token" mechanism, the token is stored in cookie.
  */
 public class TokenCookieSSOFilter implements Filter {
 	/** Filter init-param, specify the url to check the SSO token. for example: http://localhost:8080/main/bridge.jsp?token= */
 	public static final String TOKEN_CHECK_URL_INIT_PARAME = "TOKEN_CHECK_URL";
 	
 	/** The cookie name which store the token */
 	public static final String SSO_TOKEN_COOKIE = "SSO_TOKEN";
 	/** Session attribute name to store the UserInfo Object */
 	public static final String SSO_USERINFO_IN_SESSION = "SSO_USERINFO_IN_SESSION";
 	
 	private String checkUrl;
 	
 	public void init(FilterConfig cfg) throws ServletException {
 		checkUrl = cfg.getInitParameter(TOKEN_CHECK_URL_INIT_PARAME);
 	}
 	public void destroy() {
 		//Do nothing
 	}
 
 	public void doFilter(final ServletRequest request, final ServletResponse response, FilterChain chain) throws IOException, ServletException {
 		HttpServletRequest req = (HttpServletRequest)request;
 		HttpServletResponse resp = (HttpServletResponse)response;
 		try{
 			if (null!=this.checkUrl){
 				doCookieSSO(req, resp);
 			}
 			//Integrate UserInfo with OpenXava
 			UserInfo ui = (UserInfo)req.getSession().getAttribute(SSO_USERINFO_IN_SESSION);
 			if (null!=ui && null!=ui.getId()){
 				//ref: org.openxava.util.Users#setCurrent(HttpServletRequest)
 				req.getSession().setAttribute("xava.user", ui.getId());
 				req.getSession().setAttribute("xava.portal.user", ui.getId());
 				req.getSession().setAttribute("xava.portal.userinfo", ui);
 				Users.setCurrentUserInfo(ui);
 			}
 			
 			chain.doFilter(request, response);
 		}finally{
 			//Do nothing;
 		}
 
 	}
 	private void doCookieSSO(HttpServletRequest req, HttpServletResponse resp) throws MalformedURLException, IOException {
 		Cookie[] cookies = req.getCookies();
 		for (int i = 0; i < cookies.length; i++) {
 			Cookie cookie = cookies[i];
 			if (null!=cookie && SSO_TOKEN_COOKIE.equalsIgnoreCase(cookie.getName()) && cookie.getMaxAge()!=0 ){
				String localUrl = checkUrl + cookie.getValue();
 				//Remove cookie after read it
 				cookie.setMaxAge(0);	//if zero, deletes the cookie
 				resp.addCookie(cookie);
 				//Read output into Properties object
 				URL url = new URL(localUrl);
 				Properties p = new Properties();
 				BufferedReader in = null;
 				try{
 					in = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
 			        StringBuffer buf = new StringBuffer();
 			        String inputLine;
 			        while ((inputLine = in.readLine()) != null){
 			        	buf.append(inputLine);
 			        }
 			        String xml = buf.toString();
 					p.loadFromXML(new ByteArrayInputStream(xml.getBytes("UTF-8")));
 					//Fix the unicode characters
 					for (Entry<Object, Object> e: p.entrySet()){
 						String v = (String)e.getValue();
 						v = StringEscapeUtils.unescapeXml(v);
 						e.setValue(v);
 					}
 				}finally{
 					if (null!=in) in.close();
 				}
 				//Fill UserInfo Bean with Properties
 				//TODO - complete all fields of UserInfo
 				UserInfo ui = new UserInfo();
 				ui.setId(p.getProperty("id"));
 				ui.setGivenName(p.getProperty("givenName"));
 				//Remember UserInfo into Session
 				if (null!=ui.getId()){
 					req.getSession().setAttribute(SSO_USERINFO_IN_SESSION, ui);
 				}
 				
 				break;
 			}
 		}
 	}
 
 }
