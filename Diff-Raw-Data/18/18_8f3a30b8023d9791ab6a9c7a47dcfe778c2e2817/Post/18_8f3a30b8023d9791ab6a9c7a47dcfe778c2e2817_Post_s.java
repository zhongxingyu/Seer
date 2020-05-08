 package org.fubme.servlets;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.fubme.factories.PostFactory;
 import org.fubme.models.User;
 import org.fubme.persistency.mappings.PostMapper;
 
 /**
  * Servlet implementation class Post
  */
 public class Post extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public Post() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		if (request.getSession() == null) {
 			request.getRequestDispatcher("login.jsp")
 					.forward(request, response);
 			return;
 		} else {
			String postBody = request.getParameter("post_body");
			PostMapper.createPost(PostFactory.getPost(((User) request
					.getSession().getAttribute("loggedUser")).getId(),
					postBody, null, org.fubme.models.Post.TEXT));
			request.getRequestDispatcher("home.jsp").forward(request, response);
 			return;
 		}
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		if (request.getSession() == null) {
 			request.getRequestDispatcher("login.jsp")
 					.forward(request, response);
 			return;
 		} else {
 			String post_body = request.getParameter("post_body");
 			PostMapper.createPost(PostFactory.getPost(((User) request
 					.getSession().getAttribute("loggedUser")).getId(),
 					post_body, null, org.fubme.models.Post.TEXT));
 
 			request.getRequestDispatcher("Home").forward(request, response);
 			return;
 		}
 	}
 
 }
