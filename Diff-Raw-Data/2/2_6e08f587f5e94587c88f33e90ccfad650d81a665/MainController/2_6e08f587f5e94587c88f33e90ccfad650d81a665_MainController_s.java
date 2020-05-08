 package com.zazzercode.doctorhere.controllers;
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  * Servlet implementation class MainController
  */
 public class MainController extends HttpServlet {
 	private Logger logger = Logger.getLogger(MainController.class.getName());
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public MainController() {
 		super();
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		HttpSession session = req.getSession(false);
 		logger.info("Checking session : " + session);
		if (null != session.getAttribute("userName")) {
 			logger.info("session user : " + session.getAttribute("userName"));
 			req.setAttribute("name", session.getAttribute("userName"));
 			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/main.jsp");
 			dispatcher.include(req, resp);
 		} else {
 			logger.info("sessoin not found");
 			resp.sendRedirect("index.jsp");
 		}
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 }
