 package craig_proj;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Hashtable;
 import java.util.Date;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.catalina.Session;
 import org.omg.CosNaming.IstringHelper;
 
 @WebServlet("/Login")
 public class LoginServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
     
 	Hashtable users = new Hashtable();
 	
     public LoginServlet() {
         super();
 
     }
 
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		getAction(request, response);
 	}
 
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		getAction(request, response);
 	}
 	
 	private void getAction(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		String usrname = request.getParameter("username");
 		String pswrd = request.getParameter("password");
 		Cookie[] cke = request.getCookies();
 
 		if (pswrd.equals(users.get(usrname))) {
 			storeName(request, usrname);
 			setHeader(response, usrname, cke);
 			getTime(request);
 			response.sendRedirect("./welcome.jsp"); 
 		}
 		
 		else {
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Not recognized.");
 		}	
 	}
 
 	private void getTime(HttpServletRequest request) {
 		HttpSession session = request.getSession(true);
 		Date login = new Date();
 		session.setAttribute("login", login);
 	}
 
 	private void setHeader(HttpServletResponse response, String usrname, Cookie[] cke) {
		Date lastlogin = (Date)getServletContext().getAttribute(usrname + "-lastlogin");
		if (lastlogin == null) {
			lastlogin = new Date();		
		}
		getServletContext().setAttribute(usrname + "-lastlogin", lastlogin);
		response.setHeader("X-Last-Login","" + lastlogin);
 	}
 
 	private void storeName(HttpServletRequest request, String usrname) {
 		HttpSession session = request.getSession(true);
 		session.setAttribute("username", usrname);
 	}
 
 	private java.util.Date getDate() {
 		Calendar cal = Calendar.getInstance();
 		return cal.getTime();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void init() {
 		users.put("drew", "pass1");
 		users.put("webuser", "pass2");
 		users.put("amir", "pass3");
 	}
 }
