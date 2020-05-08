 package tw.com.dsc.web.util;
 
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 import tw.com.dsc.to.User;
 import tw.com.dsc.util.ThreadLocalHolder;
 
 public class SecurityFilter implements Filter {
 	
 	private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);
 	
	protected static final Pattern pattern = Pattern.compile("^/\\w*/(\\w*)!\\w*\\.action.*");
 	
 	@Override
 	public void destroy() {
 
 	}
 
 	@Override
 	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
 			ServletException {
 		HttpServletRequest httpRequest = (HttpServletRequest) request;
 		User op = ThreadLocalHolder.getOperator();
 		Matcher matcher = pattern.matcher(httpRequest.getRequestURI());
 		String action = "";
 		if (matcher.matches()) {
 			action = matcher.group(1);
 		}
 		
 		if (op.isGuest() && (StringUtils.isEmpty(action) || !("system".equals(action) || "ajax".equals(action) || "searchArticle".equals(action)))) {
 			((HttpServletResponse) response).sendRedirect(httpRequest.getContextPath()+"/index.jsp");
 		} else {
 			chain.doFilter(request, response);
 		}
 	}
 
 	@Override
 	public void init(FilterConfig filterConfig) throws ServletException {
 		WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
 	}
 
 }
