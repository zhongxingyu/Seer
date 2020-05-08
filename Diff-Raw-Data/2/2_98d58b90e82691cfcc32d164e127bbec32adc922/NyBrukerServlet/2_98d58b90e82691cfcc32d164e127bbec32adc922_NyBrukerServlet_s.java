 package no.mesan.ejafjallajokull.servlets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import no.mesan.ejafjallajokull.utils.ServletUtil;
 
 @SuppressWarnings("serial")
 public class NyBrukerServlet extends HttpServlet {
 
 	private Connection connection;
 	private Statement stm;
 	private ResultSet rs;
 
 	public NyBrukerServlet() {
 		connection = ServletUtil.initializeDBCon();
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		String nextJSP = "/ny.jsp";
 		RequestDispatcher dispatcher = request.getRequestDispatcher(nextJSP);
 		dispatcher.forward(request, response);
 		
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		String navn = request.getParameter("navn");
 		String brukerid = request.getParameter("brukerid");
 		String passord = request.getParameter("passord");
 		
		String sql = "INSERT INTO bruker VALUES('" + navn + "', '" + brukerid + "', '" + passord + "', '" + 0 + "')";
 		System.out.println(sql);
 		try {
 			stm = connection.createStatement();
 			stm.executeUpdate(sql);
 			connection.close();
 		} catch (Exception e) {
 			request.setAttribute("feilmelding", "Kunne ikke registrere bruker: " + e.getMessage());
 			e.printStackTrace();
 			ServletUtil.gotoFeilSide(request, response);
 		}
 		String nextJSP = "/index.jsp";
 		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(nextJSP);
 		dispatcher.forward(request, response);
 		ServletUtil.cleanupDBConn(rs, connection);
 		
 	}
 }
