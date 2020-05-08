 package com.ctb.pilot.chat.service;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import com.ctb.pilot.chat.dao.MessageDao;
 import com.ctb.pilot.chat.dao.jdbc.JdbcMessageDao;
 import com.ctb.pilot.chat.model.Message;
 import com.ctb.pilot.chat.model.User;
 
 public class ChatServlet extends HttpServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private MessageDao messageDao = new JdbcMessageDao();
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		String requestURI = req.getRequestURI();
 		System.out.println("In doGet(), requestURI: " + requestURI);
 
 		int rowCount = 100;
 		List<Message> messages = messageDao.getMessagesWithRowCount(rowCount);
 		req.setAttribute("messages", messages);
 		req.setAttribute("maxRowCount", rowCount);
 
 		String viewUri = "/chat/chat_view.jsp";
 
 		// FIXME: Handle a deploy problem temporarily.
 		ServletContext servletContext = req.getServletContext();
 		String contextPath = servletContext.getContextPath();
 		System.out.println("contextPath: " + contextPath);
		if (!contextPath.equals("/pilot")) {
 			viewUri = "/pilot" + viewUri;
 		}
 
 		RequestDispatcher dispatcher = req.getRequestDispatcher(viewUri);
 		dispatcher.forward(req, resp);
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		String requestURI = req.getRequestURI();
 		System.out.println("In doPost(), requestURI: " + requestURI);
 
 		HttpSession session = req.getSession();
 		User user = (User) session.getAttribute("user");
 		int userSequence = user.getSequence();
 
 		req.setCharacterEncoding("utf8");
 		String message = req.getParameter("message");
 		if (message == null || message.isEmpty()) {
 			throw new ServletException("Message is null or empty.");
 		}
 
 		messageDao.insertMessage(userSequence, message);
 
 		resp.sendRedirect("chat");
 	}
 
 }
