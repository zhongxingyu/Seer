 package nz.co.searchwellington.filters;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

 import nz.co.searchwellington.controllers.LoggedInUserFilter;
 
@Component
 public class RequestObjectLoadingFilter implements Filter {
 	
 	private RequestFilter requestFilter;
 	private LoggedInUserFilter loggedInUserFilter;
 	
 	public RequestObjectLoadingFilter() {
 	}
 	
	@Autowired
 	public RequestObjectLoadingFilter(RequestFilter requestFilter, LoggedInUserFilter loggedInUserFilter) {	
 		this.requestFilter = requestFilter;
 		this.loggedInUserFilter = loggedInUserFilter;
 	}
 
 	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
 		requestFilter.loadAttributesOntoRequest((HttpServletRequest) request);
 		loggedInUserFilter.loadLoggedInUser((HttpServletRequest) request);
 		chain.doFilter (request, response);
 	}
 	
 	
 	public void destroy() {
 		// TODO Auto-generated method stub		
 	}
 	
 	public void init(FilterConfig arg0) throws ServletException {
 		// TODO Auto-generated method stub	
 	}
 	
 }
