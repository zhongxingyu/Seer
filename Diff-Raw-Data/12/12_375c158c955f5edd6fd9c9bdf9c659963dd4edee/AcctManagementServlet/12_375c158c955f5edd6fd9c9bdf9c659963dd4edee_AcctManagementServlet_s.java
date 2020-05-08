 package Servlets;
 
 import java.io.IOException;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import Accounts.Account;
 import Accounts.AccountManager;
 
 /**
  * Servlet implementation class AcctManagementServlet
  */
 @WebServlet("/AcctManagementServlet")
 public class AcctManagementServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
     
     /**
      * @see HttpServlet#HttpServlet()
      */
     public AcctManagementServlet() {
         super();
     }
 
     /**
      * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
      */
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {}
 
     /**
      * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
      */
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     	String action = request.getParameter("Action");
     	String name = request.getParameter("User");
     	String pass = request.getParameter("Pass");
 
     	System.out.println(action);
     	
     	ServletContext sc = request.getServletContext();
 		AccountManager am = (AccountManager) sc.getAttribute("accounts");
     	String destination = "HomePage";
 		
     	if (action.equals("Create")) {
     		Account acct = am.createAccount(name, pass);
     		if (acct == null) {
     			request.getSession().setAttribute("user", name);
     			destination = "/NameTaken.jsp";
     			//request.getRequestDispatcher("/NameTaken.jsp").forward(request, response);
     		} else {
     			request.getSession().setAttribute("user", name);
     			request.getSession().setAttribute("account", acct);
     			destination = "ProfileServlet?user=" + name;
     			//request.getRequestDispatcher("/UserHome.jsp").forward(request, response);
     		}
     	} else if (action.equals("Delete")) {
     		am.deleteAccount(name);
     		destination = "/GuestHome.jsp";
     	}  else if (action.equals("Logout")) {
     		am.logoutAccount((Account) request.getSession().getAttribute("account"));
     		request.getSession().setAttribute("account", null);
     		Cookie[] cookies = request.getCookies();
     		if (cookies!=null) {
     		Cookie cookie;
     		 for(int i=0; i<cookies.length; i++) {
     		      cookie = cookies[i];
     		      if (cookie.getName().equals("name")){
     		        cookie.getValue();
     		        cookie.setMaxAge(0);
    	    		cookie.setPath("/");
     	    		cookie.setComment("EXPIRING COOKIE at " + System.currentTimeMillis());
     	    		response.addCookie(cookie);
     		        break;
     		      }
     		 }  
     		}
     		destination = "GuestHome.jsp";
     		//request.getRequestDispatcher("/GuestHome.jsp").forward(request, response);
     	} else if (action.equals("Promote")) {
     		Account acct = am.getAccount(request.getParameter("name"));
     		am.setRank(acct, true);
     		destination = "ProfileServlet?user=" + acct.getName();
     	} else if (action.equals("Demote")) {
     		Account acct = am.getAccount(request.getParameter("name"));
     		am.setRank(acct, false);
     		destination = "ProfileServlet?user=" + acct.getName();
     	} else if (action.equals("Ban")) {
     		Account acct = am.getAccount(request.getParameter("name"));
     		if (acct == null) System.out.println("no ban" + request.getParameter("name"));
     		am.banUser(acct, true);
     		destination = "ProfileServlet?user=" + acct.getName();
     	} else if (action.equals("Pardon")) {
     		Account acct = am.getAccount(request.getParameter("name"));
     		am.banUser(acct, false);
     		destination = "ProfileServlet?user=" + acct.getName();
     	} else if (action.equals("Privacy")) {
     		Account acct = am.getAccount(request.getParameter("name"));
     		am.updatePrivacy(acct);
     		destination = "ProfileServlet?user=" + acct.getName();
     	}
     	request.getRequestDispatcher(destination).forward(request, response);
     	//request.getRequestDispatcher("/ProfileServlet?user="+request.getParameter("name")).forward(request, response);
     }
 }
