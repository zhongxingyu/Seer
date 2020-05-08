 package capstone;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.PersistenceUnit;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  * Servlet implementation class MechanicServlet
  */
 @WebServlet("/MechanicServlet")
 public class MechanicServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	@PersistenceUnit
 	EntityManagerFactory emf;
 	String userType, userName, password, retypePass, emailAddy, firstName,
 			lastName, address, city, province, postalCode, phone, fax;
 
 	ArrayList<User> newUserList = new ArrayList<User>();
 	HttpSession session;
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public MechanicServlet() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		PrintWriter out = response.getWriter();
 		if (request.getParameter("submit") != null) {
 			String user = request.getParameter("userName");
 			String pass = request.getParameter("password");
 
 			// User u = (User) emf.createEntityManager()
 			// .createQuery("SELECT u  FROM User u").getResultList()
 			// .get(0);
 
			User u = (User) emf
					.createEntityManager()
					.createQuery(
							"SELECT u  FROM User u WHERE u.username='" + user
									+ "' AND u.password='" + pass + "'")
					.getResultList().get(0);
 
 			if (null != u) {
 				if (user.equals(u.getUsername())
 						&& pass.equals(u.getPassword())) {
 					// response.sendRedirect("http://localhost:8080/Capstone/mech_view.jsp");
 					out.println("WORKED!");
 				} else {
 					// response.sendRedirect("http://localhost:8080/Capstone/Login.xhtml");
 					out.println("NONON");
 				}
 			} else {
 				// response.sendRedirect("http://localhost:8080/Capstone/Login.xhtml");
 				out.println("Null");
 			}
 
 		} else if (request.getParameter("vehicleButton") != null)
 			response.sendRedirect("http://localhost:8080/Capstone/add_edit.jsp");
 
 		else if (request.getParameter("changeVehicle") != null)
 			response.sendRedirect("http://localhost:8080/Capstone/mech_view.jsp");
 
 		else if (request.getParameter("checkSubmit") != null)
 			response.sendRedirect("http://localhost:8080/Capstone/Vehicle_Invoice.jsp");
 
 		else if (request.getParameter("createUser") != null) {
 			userType = request.getParameter("userTypeDD");
 			userName = request.getParameter("userName");
 			password = request.getParameter("password");
 			retypePass = request.getParameter("retypePass");
 			emailAddy = request.getParameter("emailAddy");
 			firstName = request.getParameter("firstName");
 			lastName = request.getParameter("lastName");
 			address = request.getParameter("address");
 			city = request.getParameter("city");
 			province = request.getParameter("province");
 			postalCode = request.getParameter("postalCode");
 			phone = request.getParameter("phone");
 			fax = request.getParameter("fax");
 
 			session = request.getSession(true);
 
 			/*
 			 * String firstname, String lastname, String address, String city,
 			 * String province, String postal, String phone, String fax, String
 			 * email, String username, String password, String usertype
 			 */
 
 			User newUser = new User(firstName, lastName, address, city,
 					province, postalCode, phone, fax, emailAddy, userName,
 					password, userType);
 			if (!session.isNew()) {
 				newUserList = (ArrayList<User>) session
 						.getAttribute("statename");
 			} else {
 				newUserList = new ArrayList<User>();
 			}
 			if (newUserList == null) {
 				newUserList = new ArrayList<User>();
 			}
 			newUserList.add(newUser);
 			session.setAttribute("statename", newUserList);
 			session.setAttribute("buttonPressed", "createUser");
 
 			RequestDispatcher rd = request.getRequestDispatcher("newUser.jsp");
 			rd.forward(request, response);
 
 		}
 	}
 }
