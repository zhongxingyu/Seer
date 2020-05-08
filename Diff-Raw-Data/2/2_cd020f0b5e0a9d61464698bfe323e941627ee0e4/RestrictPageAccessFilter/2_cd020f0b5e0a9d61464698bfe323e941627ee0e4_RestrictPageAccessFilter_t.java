 package org.mdissjava.mdisscore.filter;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.mdissjava.mdisscore.model.dao.UserDao;
 import org.mdissjava.mdisscore.model.dao.impl.UserDaoImpl;
 import org.mdissjava.mdisscore.model.pojo.User;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 import com.ocpsoft.pretty.PrettyContext;
 import com.ocpsoft.pretty.faces.config.mapping.UrlMapping;
 import com.ocpsoft.pretty.faces.util.PrettyURLBuilder;
 
 public class RestrictPageAccessFilter implements Filter{
 
 	final Logger logger = LoggerFactory.getLogger(this.getClass());
 	
 	@Override
 	public void destroy() {
 		
 	}
 
 	@Override
 	public void doFilter(ServletRequest request, ServletResponse response,
 			FilterChain chain) throws IOException, ServletException {
 		
 		//Get the current logged user's username
 		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		String loggedUser = auth.getName();
 		
 		//get the user access page
 		String username = request.getParameter("user");
 	    //TODO: Check if the requested user exists in DB. If it doesn't exist send to 404 Error page?
 	    
 		UserDao udao = new UserDaoImpl();
 		User requestedUser = udao.getUserByNick(username);
 		User userLogged = udao.getUserByNick(loggedUser); 
 
 		
 		if(requestedUser != null)
 		{
 			
 			//boolean isFollowing = udao.followsUser(username, userLogged);
 			boolean isFollowing = udao.followsUser(loggedUser, requestedUser);
			if((!isFollowing)&&(requestedUser.getConfiguration().isPrivate())&&(requestedUser.getNick() != loggedUser))
 			{
 				//If they don't match send the naughty user to error page.
 				this.logger.error("FORBIDDEN ACCESS EVENT: User {} tried to access restricted area.", loggedUser);   	
 				PrettyContext context = PrettyContext.getCurrentInstance((HttpServletRequest)request);
 				PrettyURLBuilder builder = new PrettyURLBuilder();
 				
 				UrlMapping mapping = context.getConfig().getMappingById("restricted-error");
 				String targetURL = builder.build(mapping, true, new HashMap<String, String[]>());
 		    	
 		    	HttpServletResponse httpResponse=(HttpServletResponse)response;
 		    	httpResponse.sendRedirect("/mdissphoto" + targetURL);
 			}
 			else
 			{
 			    chain.doFilter(request,response);
 			}
 
 		}
 		else
 		{
 	    	this.logger.error("FORBIDDEN ACCESS EVENT: User {} does not exists.", loggedUser);   	
 	    	PrettyContext context = PrettyContext.getCurrentInstance((HttpServletRequest)request);
 			PrettyURLBuilder builder = new PrettyURLBuilder();
 			
 			UrlMapping mapping = context.getConfig().getMappingById("restricted-error");
 			String targetURL = builder.build(mapping, true, new HashMap<String, String[]>());
 	    	
 	    	HttpServletResponse httpResponse=(HttpServletResponse)response;
 	    	//TODO: delete "/mdissphoto" when moving to custom subdomain
 	    	httpResponse.sendRedirect("/mdissphoto" + targetURL);
 		}
 
 	    	    
 	}
 
 	@Override
 	public void init(FilterConfig arg0) throws ServletException {
 		this.logger.info("APPLYING RestrictPageAccessFilter");
 		
 	}
 
 }
