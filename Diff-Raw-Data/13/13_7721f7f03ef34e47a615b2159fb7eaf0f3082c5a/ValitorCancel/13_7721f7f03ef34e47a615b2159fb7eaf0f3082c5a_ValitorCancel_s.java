 package is.idega.idegaweb.pheidippides.servlet;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class ValitorCancel extends HttpServlet {
 
 	private static final long serialVersionUID = -5479791076359839694L;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		doPost(req, resp);
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		resp.setStatus(200);
 		resp.setContentType("text/plain");
 
 		PrintWriter out = resp.getWriter();
 		out.println("The Valitor cancellation response has been processed...");
 		out.close();
 	}
 
 }
