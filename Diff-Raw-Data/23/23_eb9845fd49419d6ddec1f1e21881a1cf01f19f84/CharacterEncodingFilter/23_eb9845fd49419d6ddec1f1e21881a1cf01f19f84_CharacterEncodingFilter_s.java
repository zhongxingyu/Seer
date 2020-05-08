 package app.filter;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.annotation.WebFilter;
 
 /**
  * implements {@link Filter} to use this servlet as filter to do before loading
  * xhtml
  * 
  * <BR />
  * <BR />
  * DON't FORGET to mapping by following 2 ways
  * <ol>
  * 	<li>using annotation (like this class)</li>
  * 	<li>add following filter to <B>web.xml</B></li>
  * </ol>
  * <pre>
  * {@code
  * <filter>
  * 	<filter-name>CharacterEncodingFilter</filter-name>
  * 	<filter-class>app.util.CharacterEncodingFilter</filter-class>
  * </filter>
  * <filter-mapping>
  * 	<filter-name>CharacterEncodingFilter</filter-name>
  * 	<url-pattern>/*</url-pattern>
  * </filter-mapping> 
  * }
  * </pre>
  * 
  * @author Gift
  * 
  */
 @WebFilter(filterName = "CharacterEncodingFilter", urlPatterns = "/*")
 public class CharacterEncodingFilter implements Filter {
 
 	public void init(FilterConfig config) throws ServletException {
 		// No-op
 	}
 
 	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
 		request.setCharacterEncoding("UTF-8");
 		response.setCharacterEncoding("UTF-8");
 		chain.doFilter(request, response);
 	}
 
 	public void destroy() {
 		// No-op
 	}
 }
