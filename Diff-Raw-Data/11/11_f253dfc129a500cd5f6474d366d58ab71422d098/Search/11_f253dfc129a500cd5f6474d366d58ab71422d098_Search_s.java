 package servlets;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import dao.Dao;
 import db.Unit;
 /**
  * Servlet implementation class Search
  */
 public class Search extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doThings(request);
 		request.getRequestDispatcher("WEB-INF/search.jsp").forward(request, response);
 	}
 
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 	
 	private void doThings(HttpServletRequest request){
 		String behaviour = request.getParameter("do");
 		String searchString = request.getParameter("searchString");
 		List<Unit> displayedUnits = new ArrayList<Unit>();
 
 		if("delete".equals(behaviour)){
 			Dao uDao = new Dao();
 			try {
 				uDao.deleteById(Integer.parseInt(request.getParameter("id")));
 			} catch (NumberFormatException e) {
 				e.printStackTrace();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		if(searchString == null){
 			displayedUnits = getAllUnits(request);
 		}else{
 			displayedUnits = searchUnits(request);
 		}		
 		request.setAttribute("displayedUnits", displayedUnits);
 	}
 
 	private List<Unit> searchUnits(HttpServletRequest request){
 		String paramText = "searchString";
 		HttpSession session = request.getSession();
 		String param = request.getParameter(paramText);
 		String sessionParam = (String)session.getAttribute(paramText);
 		if(param != null){
 			request.getSession().setAttribute(paramText, param);
 			sessionParam = param;
 		}
 		List<Unit> foundUnits = new ArrayList<Unit>();
 		Dao uDao = new Dao();
 		try {
 			foundUnits = uDao.searchUnit(sessionParam);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return foundUnits;
 	}
 
 	private List<Unit> getAllUnits(HttpServletRequest request){
 		List<Unit> allUnits = new ArrayList<Unit>();
 		Dao uDao = new Dao();
 		try {
 			allUnits = uDao.getAllUnits();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return allUnits;
 	}
 }
