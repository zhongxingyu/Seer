 package no.ntnu.idi.simplebank.filters;
 
 import java.io.IOException;
 import java.net.HttpCookie;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 
 import org.owasp.appsensor.AppSensorIntrusion;
 import org.owasp.appsensor.errors.AppSensorException;
 
 public class AppSensorFilter implements Filter {
 
 	public void destroy() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void doFilter(ServletRequest req, ServletResponse res,
 			FilterChain chain) throws IOException, ServletException {
 		
 		HttpServletRequest request = (HttpServletRequest)req;
 		
 		if (!(request.getMethod().equalsIgnoreCase("GET") || request.getMethod().equalsIgnoreCase("POST"))) {
 			
 			new AppSensorIntrusion(new AppSensorException("RE1", "AppSensor RE1 message", "Attacker is using illeagl HTTP method " + request.getMethod() ));
 		}
 		
 		Cookie[] cookies = request.getCookies();
 		if (cookies != null) {
 			for (Cookie cookie : cookies) {
 				if (!(cookie.getName().equals("logged_in_user") || cookie.getName().equals("JSESSIONID"))) {
 					new AppSensorException("SE2", "User adding new cookies", "User added ned cookie " + cookie.getName());
 				}
 			}
 		}
 		
 		chain.doFilter(request, res);
 
 	}
 
 	public void init(FilterConfig config) throws ServletException {
 		// TODO Auto-generated method stub
 
 	}
 
 }
