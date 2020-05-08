 /**
  * 
  */
 package tk.c4se.halt.ih31.nimunimu.filter;
 
 import java.io.IOException;
 
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 
 import lombok.val;
 import tk.c4se.halt.ih31.nimunimu.dto.Member;
 
 /**
  * @author ne_Sachirou
  */
 public class AccessLogFilter implements java.io.Serializable,
 		javax.servlet.Filter {
	private static final long serialVersionUID = -8966715137100711011L;
 
 	@Override
 	public void destroy() {
 	}
 
 	@Override
 	public void doFilter(ServletRequest req, ServletResponse resp,
 			FilterChain chain) throws IOException, ServletException {
 		val uri = ((HttpServletRequest) req).getRequestURI();
 		val method = ((HttpServletRequest) req).getMethod();
 		val memberId = ((Member) req.getAttribute("currentMember")).getId();
 		val protocol = ((HttpServletRequest) req).getProtocol();
 		// TODO: Implement.
 		chain.doFilter(req, resp);
 	}
 
 	@Override
 	public void init(FilterConfig config) throws ServletException {
 	}
 }
