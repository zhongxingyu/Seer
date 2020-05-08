 package filters;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import services.UserService;
 import transfer.bussiness.User;
 
 public class LoggedFilter implements Filter{
 
 	public void destroy() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void doFilter(ServletRequest request, ServletResponse arg1,
 			FilterChain arg2) throws IOException, ServletException {
 		HttpServletResponse resp = (HttpServletResponse) arg1;
 		HttpServletRequest req = (HttpServletRequest) request;
 		if (req.getSession().getAttribute("user")==null) {
 			Cookie[] cs =req.getCookies();
 			User u = null;
 			for(Cookie c:cs){
 				if("userid".equals(c.getName()) && c.getValue()!=null){
 					u = UserService.getInstance().getUser(Integer.valueOf(c.getValue()));
 					System.out.println(u.getId() + u.getName());
 				}
 			}
 			if (u!=null){
 				req.getSession().setAttribute("user",u);
				if(req.getRequestURI().contains("login")){ //This is optional but will be probably useful to redirect to the main page.
 					resp.sendRedirect("publicationList");
 					return;
 				}
 			}
 			arg2.doFilter(request, arg1);
 		} else {
 			arg2.doFilter(request, arg1);
 		}
 		
 	}
 
 	public void init(FilterConfig arg0) throws ServletException {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
