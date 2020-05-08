 package servlets;
 
 import java.io.IOException;
 import java.sql.SQLException;
 
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.sql.DataSource;
 
 import dao.UsersDAO;
 
 /**
  * Servlet implementation class Test
  */
 public class Test extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private DataSource ds = null;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public Test() {
         super();
         // TODO Auto-generated constructor stub
     }
     
     public void init(ServletConfig config) throws ServletException {
 		try
 		{
			
 			InitialContext ctx = new InitialContext();
			ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/WebsiteProject");
 		}
 		catch (NamingException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
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
 		String firstName = request.getParameter("firstName");
 		try
 		{
 			String result = UsersDAO.selectAllUsers(ds);
 			response.getWriter().println("Wellcome " +result+ " !");
 		}
 		catch (SQLException e)
 		{
			response.getWriter().println(e);
 			e.printStackTrace();
 		}
 	}
 
 }
