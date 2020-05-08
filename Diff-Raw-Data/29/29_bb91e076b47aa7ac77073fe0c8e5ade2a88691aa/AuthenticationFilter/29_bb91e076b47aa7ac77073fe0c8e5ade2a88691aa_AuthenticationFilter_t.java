 package com.cnnic.whois.web;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.cnnic.whois.bean.Authentication;
 import com.cnnic.whois.util.AuthenticationHolder;
 import com.cnnic.whois.util.WhoisUtil;
 /**
  * auth filter ,store role
  * @author nic
  *
  */
 public class AuthenticationFilter implements Filter {
 
 	@Override
 	public void destroy() {
 	}
 
 	@Override
 	public void doFilter(ServletRequest arg0, ServletResponse arg1,
 			FilterChain chain) throws IOException, ServletException {
 		HttpServletRequest request = (HttpServletRequest) arg0;
 		HttpServletResponse response = (HttpServletResponse) arg1;
 		String role = WhoisUtil.getUserRole(request);
 		AuthenticationHolder.setAuthentication(new Authentication(role));
 		chain.doFilter(request, response);
		AuthenticationHolder.remove();
 	}
 
 	@Override
 	public void init(FilterConfig arg0) throws ServletException {
 	}
 }
