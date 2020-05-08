 package com.siolabs.facegae.registration;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.omg.CORBA.UserException;
 
 import com.siolabs.facegae.model.User;
 
 import static com.siolabs.facegae.model.OfyService.ofy;
 
 /**
  * Servlet implementation class LoginServlet
  */
 
 public class LoginServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public LoginServlet() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 
 		//Get all the data here
 
 		/*
 		 * Method data 
 		 * 0 = manual
 		 * 1 = facebook 
 		 * 2 = google
 		 */
 		int method = Integer.parseInt(request.getParameter("method"));
 		String email = request.getParameter("email");
 		String name = request.getParameter("name");
 		String token = null;
 		HttpSession session = request.getSession();
		
		System.out.println(name+"\n"+email+"\n"+ token);
 
 		boolean userExists = false;
 		User u = ofy().load().type(User.class).id(email).now();
 
 		if(null != u)
 			userExists = true;
 
 
 
 
 
 		/* 
 		 * FB/Google Login and uses already exists
 		 */
 
 		if( (method > 0) && userExists ){
 			/* then get the token here and save it in the session
 			 * Why I don't know
 			 */
 			token = request.getParameter("token");
 			session.setAttribute("email", email);
 			session.setAttribute(token, token);
 			session.setAttribute("name", name);
 
 			response.sendRedirect("index.jsp");
 
 		}		
 		/*
 		 * FB/Google login and user doesnot exist 
 		 * so create an user account and persist to the database
 		 */		
 		else if( (method>0) && (!userExists) ){
 			User newUser = new User();
 			newUser.setEmail(email);
 			newUser.setName(name);
 
 			ofy().save().entity(newUser);
 
 			session.setAttribute("email", email);
 			session.setAttribute(token, token);
 			session.setAttribute("name", name);
 
 			response.sendRedirect("index.jsp");
 
 		}
 
 		/*
 		 * Manaul login and uses exists
 		 * check the password and credentials and do as 
 		 */
 		else if( (method ==0 ) && userExists){
 
 			String password = request.getParameter("pwd");
 			if(u.getPassword().equals(password)){
 				//now set the session values;
 				session.setAttribute("email", email);
 				session.setAttribute("name", name);
 
 				response.sendRedirect("index.jsp");
 			}
 
 		}
 		/*
 		 * method = manual and user does not exist
 		 * return to error page 
 		 */		
 		else{
 
 			response.sendRedirect("index.jsp?error=2");
 		}
 
 
 
 
 
 
 
 
 	}
 
 
 
 }
