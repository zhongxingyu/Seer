 package org.cotrix.security;
 
 import java.io.IOException;
 
 import javax.inject.Inject;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 
 import org.cotrix.common.cdi.BeanSession;
 import org.cotrix.user.PredefinedUsers;
 import org.cotrix.user.User;
 
 public class AuthBarrier implements Filter {
 
 	@Inject
 	private BeanSession session;
 	
 	@Override
 	public void init(FilterConfig filterConfig) throws ServletException {
 	}
 
 	@Override
 	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
 			ServletException {
 		
 		try {
 			session.get(User.class);
 		}
 		catch(IllegalStateException e) {
			
 			//defaults to guest for unauthenticated users
 			session.add(User.class, PredefinedUsers.guest);
 			
 		}
 		
 		chain.doFilter(request, response);
 	}
 
 	@Override
 	public void destroy() {
 	}
 }
