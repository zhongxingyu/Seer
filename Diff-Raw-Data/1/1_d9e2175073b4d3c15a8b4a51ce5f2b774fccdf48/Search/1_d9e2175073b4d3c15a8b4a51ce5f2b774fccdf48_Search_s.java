 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 public class Search extends HttpServlet {
 	
 	private String searchResult;
	private 
 
 	public void init() {
 		searchResult = getServletConfig().getInitParameter("searchResult");
 	}
 
 	private void doGetOrPost(HttpServletRequest req,
 			HttpServletResponse resp) throws ServletException, IOException {
 		
 		forwardReq(searchResult, req, resp);
 	}
 
 	public void doGet(HttpServletRequest req,
 			HttpServletResponse resp) throws ServletException, IOException {
 		doGetOrPost(req, resp);
 	}
 
 	public void doPost(HttpServletRequest req,
 			HttpServletResponse resp) throws ServletException, IOException {
 		doGetOrPost(req, resp);
 	}
 
 	private void forwardReq(String resource, HttpServletRequest req,
 			HttpServletResponse resp) throws ServletException, IOException {
 		req.getRequestDispatcher(resource).forward(req, resp);
 	}
 
 
 }
 		
 
