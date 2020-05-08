 package myservs;
 
 import java.io.*;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 /**
  * Servlet implementation class Register
  */
 public class Register extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public Register() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		
 		Auxiliary auxPoint = new Auxiliary();
 		String name=request.getParameter("name");
 		String surname=request.getParameter("surname");
 		String email=request.getParameter("email");
 		String password=request.getParameter("password");
 		String repassword=request.getParameter("repass");
 		String telephone=request.getParameter("telephone");
 		String address=request.getParameter("address");
 		String postcode=request.getParameter("postcode");
 		HttpSession userSession=request.getSession(true);
		userSession.setMaxInactiveInterval(86400);
 		DatabaseMethods point = new DatabaseMethods();
 
 		//Unfilled required fields
 		if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()){
 			userSession.setAttribute("error", "1");
 		}else if (!password.equals(repassword)){
 			userSession.setAttribute("error", "2");
 		}else{
 			//Concatenate name, surname, email
 			String id=name.concat(surname).concat(email);
 			String idHash=auxPoint.shaDigest(id);
 			if (point.uniqueUser(idHash,email)!=1){
 				String passHash=auxPoint.shaDigest(password);
 				//Is Administrator
 				int isadmin=1;
 				int intTelephone=auxPoint.integerize(telephone);
 				int intPostCode=auxPoint.integerize(postcode);
				System.out.println("Register Name: "+name);
 				int val=point.registerUser(name,surname,email,passHash,idHash,intTelephone,address,intPostCode,isadmin);
 				
 				if (val!=0){
 					//Everything went fabulous
 					userSession.setAttribute("error", "0");
 				}else{
 					//Write to database error
 					userSession.setAttribute("error", "3");
 				}
 			}else{
 				userSession.setAttribute("error","4");
 			}
 			
 		}
 		
 		userSession.setAttribute("name", name);
 		userSession.setAttribute("surname", surname);
 		userSession.setAttribute("email", email);
 		userSession.setAttribute("telephone", telephone);
 		userSession.setAttribute("address", address);
 		userSession.setAttribute("postcode", postcode);
 		
 		RequestDispatcher rd=getServletContext().getRequestDispatcher("/register.jsp");
 		if(rd!=null){
 			rd.forward(request, response);
 		}
 	}
 
 }
