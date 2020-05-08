 package com.epam.lab.buyit.controller.web.servlet.user.login;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 
 import com.epam.lab.buyit.controller.service.user.UserService;
 import com.epam.lab.buyit.controller.service.user.UserServiceImpl;
 import com.epam.lab.buyit.model.Status;
 import com.epam.lab.buyit.model.User;
 
 public class LoginServlet extends HttpServlet {
 	private static final Logger LOGGER = Logger.getLogger(LoginServlet.class);
 	private static final long serialVersionUID = 1L;
 	private UserService userService;
 	
 	public void init() {
 		userService = new UserServiceImpl();
 	}
 	
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String login = request.getParameter("login").trim();
 		String password = request.getParameter("password").trim();
 		User user = userService.getUser(login, password);
 		if (user != null) {
 			LOGGER.info(user.getLogin() + " sign in");
 			HttpSession session = request.getSession(true);
 			session.setAttribute("user", user);
 			if (user.getBan().equals(Status.BANNED.getStatus())) {
 				sendToBannPage(request, response);
 			} else {
 				successLogin(request, response);
 			}
 		} else {
 			failLogin(request, response);
 		}
 
 	}
 
 	private void sendToBannPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		request.setAttribute("message", "Sorry, you are banned. Write email to administrator for more information");
 		request.setAttribute("alert", "error");
 		request.setAttribute("messageHeader", "Banned");
 		request.getRequestDispatcher("message_page").forward(request, response);
 	}
 	
 	private void successLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
 		String returnTo = request.getParameter("returnTo");
 		if(returnTo != null)
 			response.sendRedirect(returnTo);
		else response.sendRedirect("homePage");
 	}
 	
 	private void failLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		request.setAttribute("message", "Wrong login or password");
 		request.setAttribute("returnTo", request.getParameter("returnTo"));
 		request.getRequestDispatcher("login_form").forward(request, response);
 	}
 }
