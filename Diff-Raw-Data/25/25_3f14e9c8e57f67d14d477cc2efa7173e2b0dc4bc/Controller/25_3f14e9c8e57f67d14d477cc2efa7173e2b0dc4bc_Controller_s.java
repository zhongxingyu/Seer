 package controller;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.logging.Logger;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.AddressException;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import exceptions.ServiceLocatorException;
 import jdbc.MailSender;
 import exceptions.MailSenderException;
 import jdbc.DBConnectionFactory;
 
 /**
  * Servlet implementation class Controller
  */
 @WebServlet("/Controller")
 public class Controller extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	static Logger logger = Logger.getLogger(Controller.class.getName());
        
     /**
      * @throws ServletException 
      * @see HttpServlet#HttpServlet()
      */
     public Controller() throws ServletException {
         super();
 		Connection con;
 		try {
 			con = DBConnectionFactory.getConnection();
 		} catch (ServiceLocatorException e) {
 			logger.severe("Trouble connecting to database "+e.getStackTrace());
 			throw new ServletException();
 		} catch (SQLException e) {
 			logger.severe("Trouble connecting to database "+e.getStackTrace());
 			throw new ServletException();
 		}
 		Statement stmnt;
 		try {
 			stmnt = con.createStatement();
 			stmnt.setFetchSize(1000);
 			String query_cast = "SELECT id, username, password, email FROM Users";
 			ResultSet res = stmnt.executeQuery(query_cast);
 			logger.info("The result set size is "+res.getFetchSize());
 			while(res.next()){
 				int id = res.getInt("id");
 				logger.info(" "+id);
 				String name = res.getString("username");
 				logger.info(name);
 				String pass = res.getString("password");
 				logger.info(pass);
 				String email = res.getString("email");
 				logger.info(email);
 				logger.info(name+" "+pass+" "+email);
 			}
 			
 			res.close();
 			stmnt.close();
 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println("foo");
		try {
 			MailSender m = MailSender.getMailSender();
 			StringBuffer b = new StringBuffer();
 			b.append("test email body");
 			try {
 				m.sendMessage("onlineauctionnotifier@gmail.com", "mathewchong7@gmail.com", "notification", b);
 			} catch (AddressException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (MessagingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} catch (ServiceLocatorException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (MailSenderException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
		System.out.println("bar");
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/WEB-INF/index.jsp");
 		dispatcher.forward(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 }
