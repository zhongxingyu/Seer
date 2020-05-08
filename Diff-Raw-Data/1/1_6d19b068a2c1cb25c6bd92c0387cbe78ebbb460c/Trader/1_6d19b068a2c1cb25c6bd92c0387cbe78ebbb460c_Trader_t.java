 package lendlib;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import oracle.jdbc.pool.OracleDataSource;
 
 /**
  * Servlet implementation class Trader
  */
 @WebServlet(description = "Initiates a trade between two people", urlPatterns = { "/trade" })
 public class Trader extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static final String connect_string = "jdbc:oracle:thin:ma2799/EiVQBUGs@//w4111c.cs.columbia.edu:1521/ADB";
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public Trader() {
 		super();
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	@Override
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		PrintWriter out = new PrintWriter(response.getOutputStream());
 		response.setContentType("text/html");
 		// Create a OracleDataSource instance and set URL if it doesn't already
 		// exist
 		Connection conn = null;
 		OracleDataSource ods;
 		try {
 			ods = new OracleDataSource();
 			ods.setURL(connect_string);
 			conn = ods.getConnection();
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 			MakeUser.closeConn(conn, out);
 			return;
 		}
 		// Make sure someone is logged on
 		Object suid = request.getSession().getAttribute("suid");
 		if (suid == null) {
 			request.getSession().setAttribute("nologon", true);
 			MakeUser.closeConn(conn, out);
 			response.sendRedirect(request.getHeader("referer"));
 			return;
 		}
 		// Insert the appropriate value into the pendingtrade table
 		String query = "insert into PendingTrade (OwnerID, BorrowerID, BookID) values ("
 				+ request.getParameter("ouid")
 				+ ", "
 				+ request.getSession().getAttribute("suid")
 				+ ", "
 				+ request.getParameter("bid") + ")";
 		try {
 			Statement stmt = conn.createStatement();
 			stmt.executeUpdate(query);
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 			request.getSession().setAttribute("alreadyPending", true);
 			MakeUser.closeConn(conn, out);
 			response.sendRedirect(request.getHeader("referer"));
 			return;
 		}
 		// If we're here, then the pending trade went fine
 		request.getSession().setAttribute("tradeSet", true);
 		MakeUser.closeConn(conn, out);
 		response.sendRedirect(request.getHeader("referer"));
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	@Override
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 }
