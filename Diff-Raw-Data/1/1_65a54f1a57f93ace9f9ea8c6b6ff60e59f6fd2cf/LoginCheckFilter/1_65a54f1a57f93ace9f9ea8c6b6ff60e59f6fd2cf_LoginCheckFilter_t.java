 package com.ctb.pilot.common.filter.login;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import com.ctb.pilot.chat.dao.UserDao;
 import com.ctb.pilot.chat.dao.jdbc.JdbcUserDao;
 import com.ctb.pilot.chat.model.User;
 import com.ctb.pilot.common.util.HttpUtils;
 
 public class LoginCheckFilter implements Filter {
 
 	private UserDao userDao = new JdbcUserDao();
 
 	@Override
 	public void init(FilterConfig filterConfig) throws ServletException {
 	}
 
 	@Override
 	public void doFilter(ServletRequest request, ServletResponse response,
 			FilterChain chain) throws IOException, ServletException {
 		HttpServletRequest httpRequest = (HttpServletRequest) request;
 		HttpServletResponse httpResponse = (HttpServletResponse) response;
 		HttpSession session = httpRequest.getSession();
 		Object sequence = session.getAttribute("seq");
 		if (sequence == null) {
 			String sequenceInCookie = HttpUtils.getCookie(httpRequest, "seq");
 			if (sequenceInCookie == null) {
 				httpResponse.sendRedirect("/pilot/login/login.html");
 				return;
 			}
 			Integer userSequence = Integer.valueOf(sequenceInCookie);
 			User user = userDao.getUserBySequence(userSequence);
			System.out.println("user: " + user);
 			session.setAttribute("user", user);
 		}
 		chain.doFilter(request, response);
 	}
 
 	@Override
 	public void destroy() {
 	}
 
 }
